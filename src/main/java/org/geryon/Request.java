package org.geryon;

import java.util.List;
import java.util.Map;

/**
 * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
 */
public class Request {
    private String url;
    private String body;
    private String contentType;
    private String method;
    private Map<String, String> headers;
    private Map<String, String> queryParameters;

    public Request(String url, String body, String contentType, String method, Map<String, String> headers, Map<String, String> queryParameters) {
        this.url = url;
        this.body = body;
        this.contentType = contentType;
        this.method = method;
        this.headers = headers;
        this.queryParameters = queryParameters;
    }

    public String getUrl() {
        return url;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    @Override
    public String toString() {
        return "{" + "request.url='" + url + '\'' + ", request.body='" + body + '\'' + ", request.method='" + method + '\'' + ", request.headers=" + headers + ", request.queryParameters=" + queryParameters + '}';
    }

    public static class Builder {
        private String url;
        private String body;
        private String contentType;
        private String method;
        private Map<String, String> headers;
        private Map<String, String> queryParameters;

        private Builder self = this;

        public Builder url(String url) {
            this.url = url;
            return self;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return self;
        }

        public Builder body(String body) {
            this.body = body;
            return self;
        }

        public Builder method(String method) {
            this.method = method;
            return self;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return self;
        }

        public Builder queryParameters(Map<String, String> queryParameters) {
            this.queryParameters = queryParameters;
            return self;
        }

        public Request build() {
            return new Request(url, body, contentType, method, headers, queryParameters);
        }
    }
}
