package com.tukorea.bus.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.net.http.WebSocketHandshakeException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

/** 기사 앱(WebSocket 업링크) → 서버 → 학생 앱(REST 스냅샷, SSE 푸시) 흐름을 실제 서버로 검증한다. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RealtimeLocationIntegrationTest {

    private static final String DRIVER_TOKEN = "test-driver-token";

    @LocalServerPort
    private int port;

    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void 토큰이_없거나_틀리면_WebSocket_연결이_거절된다() {
        assertThatThrownBy(() -> connect(null).get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(WebSocketHandshakeException.class);
        assertThatThrownBy(() -> connect("wrong-token").get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(WebSocketHandshakeException.class);
    }

    @Test
    void 기사_위치가_REST로_조회되고_SSE로_푸시된다() throws Exception {
        String busId = "bus-sse";
        BlockingQueue<String> sseLines = openSse("/api/bus/" + busId + "/location/stream");
        WebSocket driver = connect(DRIVER_TOKEN).get(5, TimeUnit.SECONDS);

        driver.sendText("{\"id\":\"" + busId + "\",\"lat\":37.3398,\"lng\":126.7330}", true)
                .get(5, TimeUnit.SECONDS);

        String data = awaitLine(sseLines, "data:");
        assertThat(data).contains("\"id\":\"" + busId + "\"").contains("37.3398").contains("126.733");

        HttpResponse<String> snapshot = get("/api/bus/" + busId + "/location");
        assertThat(snapshot.statusCode()).isEqualTo(200);
        assertThat(snapshot.body()).contains("37.3398");

        HttpResponse<String> all = get("/api/bus/locations");
        assertThat(all.statusCode()).isEqualTo(200);
        assertThat(all.body()).contains(busId);

        driver.sendClose(WebSocket.NORMAL_CLOSURE, "done");
    }

    @Test
    void 잘못된_메시지는_오류를_알려주고_연결은_유지된다() throws Exception {
        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        WebSocket driver = connect(DRIVER_TOKEN, received).get(5, TimeUnit.SECONDS);

        driver.sendText("{\"id\":\"bus-bad\",\"lat\":0,\"lng\":0}", true).get(5, TimeUnit.SECONDS);
        assertThat(received.poll(5, TimeUnit.SECONDS)).contains("error");

        driver.sendText("not json", true).get(5, TimeUnit.SECONDS);
        assertThat(received.poll(5, TimeUnit.SECONDS)).contains("error");

        driver.sendText("{\"id\":\"bus-bad\",\"lat\":37.33,\"lng\":126.73}", true).get(5, TimeUnit.SECONDS);
        awaitStatus("/api/bus/bus-bad/location", 200);

        driver.sendClose(WebSocket.NORMAL_CLOSURE, "done");
    }

    @Test
    void 기사_앱이_보내는_accuracy_status와_모르는_필드를_처리한다() throws Exception {
        WebSocket driver = connect(DRIVER_TOKEN).get(5, TimeUnit.SECONDS);

        driver.sendText("{\"id\":\"bus-extra\",\"lat\":37.33,\"lng\":126.73,\"accuracy\":4.5,"
                + "\"status\":\"WAITING\",\"unknown\":1}", true).get(5, TimeUnit.SECONDS);
        awaitStatus("/api/bus/bus-extra/location", 200);

        String body = get("/api/bus/bus-extra/location").body();
        assertThat(body).contains("\"accuracy\":4.5").contains("\"status\":\"WAITING\"");
        driver.sendClose(WebSocket.NORMAL_CLOSURE, "done");
    }

    @Test
    void 알_수_없는_status는_거절된다() throws Exception {
        BlockingQueue<String> received = new LinkedBlockingQueue<>();
        WebSocket driver = connect(DRIVER_TOKEN, received).get(5, TimeUnit.SECONDS);

        driver.sendText("{\"id\":\"bus-st\",\"lat\":37.33,\"lng\":126.73,\"status\":\"FLYING\"}", true)
                .get(5, TimeUnit.SECONDS);
        assertThat(received.poll(5, TimeUnit.SECONDS)).contains("status");
        assertThat(get("/api/bus/bus-st/location").statusCode()).isEqualTo(404);
        driver.sendClose(WebSocket.NORMAL_CLOSURE, "done");
    }

    @Test
    void 위치가_없는_버스는_404() throws Exception {
        assertThat(get("/api/bus/never-seen/location").statusCode()).isEqualTo(404);
    }

    @Test
    void 관리자_API는_토큰_없이는_접근할_수_없다() throws Exception {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri("/api/admin/schedule/refresh"))
                .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(401);
    }

    // ---- helpers ----

    private CompletableFuture<WebSocket> connect(String token) {
        return connect(token, new LinkedBlockingQueue<>());
    }

    private CompletableFuture<WebSocket> connect(String token, BlockingQueue<String> received) {
        WebSocket.Builder builder = client.newWebSocketBuilder();
        if (token != null) {
            builder.header("X-Driver-Token", token);
        }
        return builder.buildAsync(URI.create("ws://localhost:" + port + "/ws"), new WebSocket.Listener() {
            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                received.add(data.toString());
                webSocket.request(1);
                return null;
            }

            @Override
            public void onOpen(WebSocket webSocket) {
                webSocket.request(1);
            }
        });
    }

    private BlockingQueue<String> openSse(String path) {
        BlockingQueue<String> lines = new LinkedBlockingQueue<>();
        HttpRequest request = HttpRequest.newBuilder(uri(path)).header("Accept", "text/event-stream").GET().build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body().forEach(lines::add));
        return lines;
    }

    private String awaitLine(BlockingQueue<String> lines, String prefix) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            String line = lines.poll(200, TimeUnit.MILLISECONDS);
            if (line != null && line.startsWith(prefix)) {
                return line;
            }
        }
        throw new AssertionError("SSE에서 '" + prefix + "' 줄을 받지 못했습니다.");
    }

    private void awaitStatus(String path, int status) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (get(path).statusCode() == status) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError(path + " 가 " + status + " 이 되지 않았습니다.");
    }

    private HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(uri(path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
