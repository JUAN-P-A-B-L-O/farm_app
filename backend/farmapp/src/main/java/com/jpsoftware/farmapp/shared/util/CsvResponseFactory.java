package com.jpsoftware.farmapp.shared.util;

import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class CsvResponseFactory {

    private static final MediaType CSV_MEDIA_TYPE = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private CsvResponseFactory() {
    }

    public static ResponseEntity<byte[]> buildDownload(String fileName, String csvContent) {
        byte[] body = csvContent.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(CSV_MEDIA_TYPE);
        headers.setContentLength(body.length);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }
}
