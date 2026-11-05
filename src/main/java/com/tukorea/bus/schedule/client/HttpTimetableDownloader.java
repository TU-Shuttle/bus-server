package com.tukorea.bus.schedule.client;

import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HttpTimetableDownloader implements TimetableDownloader {

    private static final int MAX_BYTES = 20 * 1024 * 1024;
    private static final byte[] PDF_MAGIC = "%PDF".getBytes(StandardCharsets.US_ASCII);

    private final RestClient restClient;

    public HttpTimetableDownloader() {
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(30));
        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.USER_AGENT, "tukorea-shuttle-server")
                .build();
    }

    @Override
    public DownloadedFile download(String url) {
        ResponseEntity<byte[]> response;
        try {
            response = restClient.get().uri(url).retrieve().toEntity(byte[].class);
        } catch (RestClientException e) {
            throw new TimetableDownloadException("시간표 다운로드 실패: " + url, e);
        }

        byte[] body = response.getBody();
        if (body == null || body.length == 0) {
            throw new TimetableDownloadException("시간표 응답이 비어 있습니다: " + url);
        }
        if (body.length > MAX_BYTES) {
            throw new TimetableDownloadException("시간표 파일이 너무 큽니다: " + body.length);
        }
        if (!startsWithPdfMagic(body)) {
            throw new TimetableDownloadException("PDF 형식이 아닙니다: " + url);
        }
        return new DownloadedFile(body, fileName(response.getHeaders()));
    }

    private boolean startsWithPdfMagic(byte[] body) {
        if (body.length < PDF_MAGIC.length) {
            return false;
        }
        for (int i = 0; i < PDF_MAGIC.length; i++) {
            if (body[i] != PDF_MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    /** 학교 서버는 filename을 퍼센트 인코딩한 채 보내므로 직접 디코딩한다. */
    private String fileName(HttpHeaders headers) {
        ContentDisposition disposition = headers.getContentDisposition();
        String name = disposition.getFilename();
        if (name == null) {
            return null;
        }
        try {
            return URLDecoder.decode(name, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return name;
        }
    }
}
