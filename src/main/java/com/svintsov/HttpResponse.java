package com.svintsov;

import static java.lang.String.format;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpResponse {

    public static String STATUS_OK = "200 OK";
    public static String STATUS_SERVICE_UNAVAILABLE = "503 Service Unavailable";

    private String status;
    private int contentLength;
    private String contentType;
    private String body;

    public static HttpResponseBuilder builder() {
        return new HttpResponseBuilder();
    }

    public String toResponse() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(format("HTTP/1.1 %s", status));
        stringBuilder.append("\n");
        stringBuilder.append(format("Content-Length: %d", contentLength));
        stringBuilder.append("\n");
        stringBuilder.append(format("Content-Type: %s", contentType));
        stringBuilder.append("\n");
        stringBuilder.append("\n");
        stringBuilder.append(body);
        return stringBuilder.toString();
    }

    public static class HttpResponseBuilder {

        private String status;
        private int contentLength;
        private String contentType;
        private String body;

        public HttpResponseBuilder status(String status) {
            this.status = status;
            return this;
        }

        public HttpResponseBuilder contentLength(int contentLength) {
            this.contentLength = contentLength;
            return this;
        }

        public HttpResponseBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public HttpResponseBuilder body(String body) {
            this.body = body;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(status, contentLength, contentType, body);
        }
    }

}
