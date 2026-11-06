package com.tukorea.bus.schedule.parser;

import com.tukorea.bus.schedule.entity.ShuttleDayType;
import com.tukorea.bus.schedule.entity.ShuttleDirection;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.springframework.stereotype.Component;

/**
 * 한국공대 셔틀버스 시간표 PDF(1페이지)를 좌표 기반으로 해석한다.
 * <ul>
 *   <li>상단: 정왕역 ↔ 본교 평일 시간표. 가운데 '시간' 열을 기준으로 왼쪽은 학교 출발(하교), 오른쪽은 정왕역 출발(등교)의 분(minute)이다.</li>
 *   <li>하단: 제2캠퍼스 시간표. 평일 2열(본교→2캠, 2캠→본교)과 토요일 2열(정왕역→본교→2캠, 2캠→본교→정왕역)이다.</li>
 * </ul>
 * 레이아웃이 달라져 검증을 통과하지 못하면 {@link TimetableParseException}을 던진다.
 */
@Component
public class PdfShuttleTimetableParser implements ShuttleTimetableParser {

    private static final String OIDO = "오이도";

    private static final Pattern NUMBER = Pattern.compile("\\d{1,2}");
    private static final Pattern CLOCK = Pattern.compile("^(\\d{1,2}):(\\d{2}),?$");
    private static final Pattern HOUR_ONLY = Pattern.compile("^(\\d{1,2})시,?$");
    private static final Pattern MINUTE_LABEL = Pattern.compile("^(\\d{1,2})분$");
    private static final Pattern WINDOW = Pattern.compile("^\\((\\d{1,2}):(\\d{2})~(\\d{1,2}):(\\d{2})\\)$");
    private static final Pattern MONTH_DAY = Pattern.compile("^(\\d{1,2})\\.(\\d{1,2})$");

    /** 하단 표의 열 경계(페이지 폭 595pt 기준 비율). 평일 2열 | 토요일 2열. */
    private static final float WEEKDAY_SATURDAY_SPLIT = 0.47f;
    private static final float SATURDAY_COLUMN_SPLIT = 0.72f;

    private static final String ON_DEMAND = "수시운행";

    @Override
    public ParsedTimetable parse(byte[] content, int fallbackYear) {
        List<Token> tokens = extractTokens(content);
        float pageWidth = tokens.stream().map(Token::x1).max(Float::compare).orElse(0f);
        if (pageWidth <= 0) {
            throw new TimetableParseException("PDF에서 텍스트를 읽지 못했습니다.");
        }

        Token section2 = tokens.stream().filter(t -> t.text().startsWith("•『제")).findFirst()
                .orElseThrow(() -> new TimetableParseException("제2캠퍼스 시간표 구간을 찾지 못했습니다."));

        List<ParsedDeparture> departures = new ArrayList<>();
        parseStationGrid(tokens, section2.y(), departures);
        parseCampus2Table(tokens, section2.y(), departures);
        validate(departures);

        return new ParsedTimetable(
                parseTitle(tokens),
                parseValidFrom(tokens, fallbackYear),
                parseValidTo(tokens, fallbackYear),
                List.copyOf(departures)
        );
    }

    // ---------- 정왕역 ↔ 본교 (평일 격자) ----------

    private void parseStationGrid(List<Token> tokens, float limitY, List<ParsedDeparture> out) {
        Token header = tokens.stream().filter(t -> t.text().equals("시간")).findFirst()
                .orElseThrow(() -> new TimetableParseException("'시간' 열 머리글을 찾지 못했습니다."));
        float hourX = header.cx();

        List<Token> hours = tokens.stream()
                .filter(t -> NUMBER.matcher(t.text()).matches())
                .filter(t -> Math.abs(t.cx() - hourX) <= 12 && t.y() > header.y() && t.y() < limitY)
                .filter(t -> Integer.parseInt(t.text()) >= 5 && Integer.parseInt(t.text()) <= 23)
                .sorted(Comparator.comparing(Token::y))
                .toList();
        if (hours.size() < 10) {
            throw new TimetableParseException("시간 행을 충분히 찾지 못했습니다: " + hours.size());
        }

        LocalTime lastBus = null;
        Float lastBusY = null;
        for (Token hourToken : hours) {
            int hour = Integer.parseInt(hourToken.text());
            for (Token t : tokens) {
                if (Math.abs(t.y() - hourToken.y()) > 7 || Math.abs(t.cx() - hourX) <= 12) {
                    continue;
                }
                boolean left = t.cx() < hourX;
                ShuttleDirection direction = left
                        ? ShuttleDirection.CAMPUS1_TO_STATION : ShuttleDirection.STATION_TO_CAMPUS1;
                if (NUMBER.matcher(t.text()).matches()) {
                    out.add(departure(ShuttleDayType.WEEKDAY, direction, time(hour, Integer.parseInt(t.text())),
                            null, null));
                    continue;
                }
                Matcher minute = MINUTE_LABEL.matcher(t.text());
                if (!left && minute.matches()) {
                    boolean isLast = tokens.stream().anyMatch(o -> o.text().contains("막차")
                            && Math.abs(o.y() - hourToken.y()) <= 7);
                    lastBus = time(hour, Integer.parseInt(minute.group(1)));
                    lastBusY = hourToken.y();
                    out.add(departure(ShuttleDayType.WEEKDAY, direction, lastBus, null, isLast ? "막차" : null));
                }
            }
        }

        // 수시운행 구간: "(08:40~10:00)"
        for (Token t : tokens) {
            Matcher w = WINDOW.matcher(t.text());
            if (w.matches() && t.y() < limitY) {
                ShuttleDirection direction = t.cx() < hourX
                        ? ShuttleDirection.CAMPUS1_TO_STATION : ShuttleDirection.STATION_TO_CAMPUS1;
                out.add(departure(ShuttleDayType.WEEKDAY, direction,
                        time(Integer.parseInt(w.group(1)), Integer.parseInt(w.group(2))),
                        time(Integer.parseInt(w.group(3)), Integer.parseInt(w.group(4))), ON_DEMAND));
            }
        }

        // 17시 이후 정왕역 출발 칸의 안내문(도착 버스 탑승): 17시 행부터 막차 행 직전까지
        Token hour17 = hours.stream().filter(h -> h.text().equals("17")).findFirst().orElse(null);
        if (hour17 != null) {
            float endY = lastBusY != null ? lastBusY - 7 : limitY;
            String guide = tokens.stream()
                    .filter(t -> t.cx() > hourX + 20 && t.y() >= hour17.y() - 7 && t.y() < endY)
                    .filter(t -> !NUMBER.matcher(t.text()).matches())
                    .sorted(Comparator.comparing((Token t) -> Math.round(t.y() / 4)).thenComparing(Token::x0))
                    .map(Token::text)
                    .reduce((a, b) -> a + " " + b).orElse("");
            if (!guide.isBlank()) {
                out.add(departure(ShuttleDayType.WEEKDAY, ShuttleDirection.STATION_TO_CAMPUS1, time(17, 0),
                        lastBus, truncate(guide, 200)));
            }
        }
    }

    // ---------- 제2캠퍼스 (평일/토요일 목록) ----------

    private void parseCampus2Table(List<Token> tokens, float section2Y, List<ParsedDeparture> out) {
        Token col2Header = tokens.stream()
                .filter(t -> t.text().equals("제2캠퍼스") && t.y() > section2Y)
                .findFirst()
                .orElseThrow(() -> new TimetableParseException("제2캠퍼스 표 머리글을 찾지 못했습니다."));
        float pageWidth = tokens.stream().map(Token::x1).max(Float::compare).orElseThrow();
        float weekdaySplit = col2Header.x0() - 3;
        float saturdayStart = pageWidth * WEEKDAY_SATURDAY_SPLIT;
        float saturdaySplit = pageWidth * SATURDAY_COLUMN_SPLIT;
        float dataTop = col2Header.y() + 8;

        // 표 아래 각주(★ ...)에도 시각이 적혀 있으므로, 평일 첫 열의 첫 ★ 위까지만 본문으로 본다.
        float footnoteY = tokens.stream()
                .filter(t -> t.text().equals("★") && t.cx() < weekdaySplit && t.y() > dataTop)
                .map(Token::y).min(Float::compare).orElse(Float.MAX_VALUE);
        List<Token> dataTokens = tokens.stream().filter(t -> t.y() > dataTop && t.y() < footnoteY - 4).toList();
        float firstRowY = dataTokens.stream().filter(t -> isClock(t.text()))
                .map(Token::y).min(Float::compare)
                .orElseThrow(() -> new TimetableParseException("제2캠퍼스 시간을 찾지 못했습니다."));

        for (Token t : dataTokens) {
            LocalTime time = clock(t.text());
            if (time == null) {
                continue;
            }
            float cx = t.cx();
            if (cx < weekdaySplit) {
                boolean viaStation = dataTokens.stream().anyMatch(o -> o.text().startsWith("(정왕역출발")
                        && o.cx() < weekdaySplit && Math.abs(o.y() - firstRowY) <= 5)
                        && Math.abs(t.y() - firstRowY) <= 5;
                out.add(viaStation
                        ? departure(ShuttleDayType.WEEKDAY, ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1,
                        time, null, "정왕역 출발, 본교 서문 건너편 정류장 경유")
                        : departure(ShuttleDayType.WEEKDAY, ShuttleDirection.CAMPUS1_TO_CAMPUS2, time, null, null));
            } else if (cx < saturdayStart) {
                String note = trailingNote(dataTokens, t, saturdayStart);
                // "17:40 (오이도역 도착)"은 본교로 가지 않고 오이도역으로 바로 가는 차다(제2캠퍼스 학사 공지 기준).
                if (note != null && note.contains(OIDO)) {
                    out.add(departure(ShuttleDayType.WEEKDAY, ShuttleDirection.CAMPUS2_TO_OIDO_STATION, time, null,
                            "본교를 거치지 않아요"));
                } else {
                    out.add(departure(ShuttleDayType.WEEKDAY, ShuttleDirection.CAMPUS2_TO_CAMPUS1, time, null, note));
                }
            } else if (cx < saturdaySplit) {
                out.add(departure(ShuttleDayType.SATURDAY, ShuttleDirection.STATION_TO_CAMPUS2_VIA_CAMPUS1,
                        time, null, null));
            } else {
                out.add(departure(ShuttleDayType.SATURDAY, ShuttleDirection.CAMPUS2_TO_STATION_VIA_CAMPUS1,
                        time, null, null));
            }
        }
    }

    /** 시간 오른쪽 같은 줄에 붙은 괄호 안내, 예: "17:40 (오이도역 도착)". */
    private String trailingNote(List<Token> tokens, Token time, float rightLimit) {
        String note = tokens.stream()
                .filter(t -> Math.abs(t.y() - time.y()) <= 6 && t.x0() > time.x1() && t.x0() < rightLimit - 10)
                .filter(t -> !isClock(t.text()))
                .sorted(Comparator.comparing(Token::x0))
                .map(Token::text)
                .reduce((a, b) -> a + " " + b).orElse("");
        String cleaned = note.replace("(", "").replace(")", "").trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    // ---------- 제목/적용 기간 ----------

    private String parseTitle(List<Token> tokens) {
        float topY = tokens.stream().map(Token::y).min(Float::compare).orElse(0f);
        String title = tokens.stream()
                .filter(t -> Math.abs(t.y() - topY) <= 6 && !t.text().equals("◈"))
                .sorted(Comparator.comparing(Token::x0))
                .map(Token::text)
                .reduce((a, b) -> a + " " + b).orElse("");
        return title.isBlank() ? "셔틀버스 시간표" : truncate(title, 100);
    }

    private LocalDate parseValidFrom(List<Token> tokens, int year) {
        return validRange(tokens, year, true);
    }

    private LocalDate parseValidTo(List<Token> tokens, int year) {
        return validRange(tokens, year, false);
    }

    /** "9.1 ~ 12.22" 형태. 못 찾으면 null(적용 기간 미상). */
    private LocalDate validRange(List<Token> tokens, int year, boolean from) {
        List<Token> sorted = tokens.stream()
                .sorted(Comparator.comparing((Token t) -> Math.round(t.y() / 4)).thenComparing(Token::x0)).toList();
        for (int i = 0; i + 2 < sorted.size(); i++) {
            Matcher a = MONTH_DAY.matcher(sorted.get(i).text());
            Matcher b = MONTH_DAY.matcher(sorted.get(i + 2).text());
            if (a.matches() && sorted.get(i + 1).text().equals("~") && b.matches()) {
                try {
                    LocalDate start = LocalDate.of(year, Integer.parseInt(a.group(1)), Integer.parseInt(a.group(2)));
                    LocalDate end = LocalDate.of(year, Integer.parseInt(b.group(1)), Integer.parseInt(b.group(2)));
                    if (end.isBefore(start)) {
                        end = end.plusYears(1);
                    }
                    return from ? start : end;
                } catch (DateTimeException e) {
                    return null;
                }
            }
        }
        return null;
    }

    // ---------- 검증 ----------

    private void validate(List<ParsedDeparture> departures) {
        long stationRoutes = departures.stream()
                .filter(d -> d.direction() == ShuttleDirection.STATION_TO_CAMPUS1
                        || d.direction() == ShuttleDirection.CAMPUS1_TO_STATION)
                .count();
        if (stationRoutes < 40) {
            throw new TimetableParseException("정왕역↔본교 시간이 너무 적습니다: " + stationRoutes);
        }
        for (ShuttleDirection direction : ShuttleDirection.values()) {
            long count = departures.stream().filter(d -> d.direction() == direction).count();
            if (count == 0) {
                throw new TimetableParseException("방향에 시간이 없습니다: " + direction);
            }
        }
    }

    // ---------- 유틸 ----------

    private ParsedDeparture departure(ShuttleDayType dayType, ShuttleDirection direction, LocalTime time,
                                      LocalTime end, String note) {
        return new ParsedDeparture(dayType, direction, time, end, note);
    }

    private LocalTime time(int hour, int minute) {
        try {
            return LocalTime.of(hour, minute);
        } catch (DateTimeException e) {
            throw new TimetableParseException("잘못된 시각: " + hour + ":" + minute, e);
        }
    }

    private boolean isClock(String text) {
        return CLOCK.matcher(text).matches() || HOUR_ONLY.matcher(text).matches();
    }

    private LocalTime clock(String text) {
        Matcher c = CLOCK.matcher(text);
        if (c.matches()) {
            return time(Integer.parseInt(c.group(1)), Integer.parseInt(c.group(2)));
        }
        Matcher h = HOUR_ONLY.matcher(text);
        if (h.matches()) {
            return time(Integer.parseInt(h.group(1)), 0);
        }
        return null;
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max);
    }

    private List<Token> extractTokens(byte[] content) {
        try (PDDocument document = Loader.loadPDF(content)) {
            TokenCollector collector = new TokenCollector();
            collector.setSortByPosition(true);
            collector.setStartPage(1);
            collector.setEndPage(1);
            collector.getText(document);
            return collector.tokens;
        } catch (IOException e) {
            throw new TimetableParseException("PDF를 읽지 못했습니다.", e);
        }
    }

    /** 공백으로 구분된 단어 하나와 그 위치. y는 글자 중앙(위→아래 방향). */
    private record Token(String text, float x0, float x1, float y) {

        float cx() {
            return (x0 + x1) / 2;
        }
    }

    private static final class TokenCollector extends PDFTextStripper {

        private final List<Token> tokens = new ArrayList<>();

        private TokenCollector() throws IOException {
            super();
        }

        /** PDFBox는 여러 단어가 든 텍스트 런을 넘겨주므로, 글자 위치 단위로 공백/간격에서 단어를 나눈다. */
        @Override
        protected void writeString(String text, List<TextPosition> positions) {
            StringBuilder word = new StringBuilder();
            TextPosition first = null;
            TextPosition last = null;
            for (TextPosition position : positions) {
                String unicode = position.getUnicode();
                boolean blank = unicode == null || unicode.isBlank();
                boolean gap = last != null
                        && position.getXDirAdj() - (last.getXDirAdj() + last.getWidthDirAdj())
                        > Math.max(position.getWidthOfSpace(), 1f) * 0.6f;
                if (blank || gap) {
                    flush(word, first, last);
                    first = null;
                    last = null;
                    word.setLength(0);
                    if (blank) {
                        continue;
                    }
                }
                if (first == null) {
                    first = position;
                }
                word.append(unicode);
                last = position;
            }
            flush(word, first, last);
        }

        private void flush(StringBuilder word, TextPosition first, TextPosition last) {
            if (word.isEmpty() || first == null || last == null) {
                return;
            }
            tokens.add(new Token(
                    word.toString(),
                    first.getXDirAdj(),
                    last.getXDirAdj() + last.getWidthDirAdj(),
                    first.getYDirAdj() - first.getHeightDir() / 2
            ));
        }
    }
}
