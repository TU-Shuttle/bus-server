package com.tukorea.bus.schedule.parser;

public interface ShuttleTimetableParser {

    /**
     * @param content      원본 파일 내용
     * @param fallbackYear 원본에서 연도를 알 수 없을 때 적용 기간에 쓸 연도
     * @throws TimetableParseException 원본을 해석할 수 없거나 검증에 실패한 경우
     */
    ParsedTimetable parse(byte[] content, int fallbackYear);
}
