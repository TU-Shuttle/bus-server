package com.tukorea.bus.schedule.client;

public interface TimetableDownloader {

    /** @throws TimetableDownloadException 내려받지 못했거나 PDF가 아닌 경우 */
    DownloadedFile download(String url);

    /** @param fileName 서버가 알려준 파일명(없으면 null) */
    record DownloadedFile(byte[] content, String fileName) {
    }

    class TimetableDownloadException extends RuntimeException {

        public TimetableDownloadException(String message, Throwable cause) {
            super(message, cause);
        }

        public TimetableDownloadException(String message) {
            super(message);
        }
    }
}
