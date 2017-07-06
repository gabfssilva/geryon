package org.geryon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class Response {
    private String body;
    private int httpStatus;
    private Map<String, String> headers;
    private String contentType;

    public Response(String body, Integer httpStatus, Map<String, String> headers, String contentType) {
        this.body = body;
        this.httpStatus = httpStatus;
        this.headers = headers;
        this.contentType = contentType;
    }

    public String getBody() {
        return body;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public String toString() {
        return "{" + "response.body='" + body + '\'' + ", response.httpStatus=" + httpStatus + ", response.headers=" + headers + ", response.contentType='" + contentType + '\'' + '}';
    }

    public static class Builder {
        private String body;
        private int httpStatus = 200;
        private Map<String, String> headers = new HashMap<>();
        private String contentType;

        private Builder self = this;

        public Builder body(String body) {
            this.body = body;
            return self;
        }

        public Builder httpStatus(Integer httpStatus) {
            this.httpStatus = httpStatus;
            return self;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return self;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return self;
        }

        public Response build() {
            return new Response(body, httpStatus, headers, contentType);
        }
    }
}
