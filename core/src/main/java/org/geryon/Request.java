package org.geryon;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class Request {
    private String url;
    private String rawPath;
    private String body;
    private String contentType;
    private String method;
    private Map<String, String> headers;
    private Map<String, String> queryParameters;
    private Map<String, String> pathParameters;
    private Map<String, Map<String, String>> matrixParameters;

    Request(String url,
            String rawPath,
            String body,
            String contentType,
            String method,
            Map<String, String> headers,
            Map<String, String> queryParameters,
            Map<String, String> pathParameters) {
        this.url = url;
        this.rawPath = rawPath;
        this.body = body;
        this.contentType = contentType;
        this.method = method;
        this.headers = headers;
        this.queryParameters = queryParameters;
        this.pathParameters = pathParameters;
    }

    public String url() {
        return url;
    }

    public String rawPath() {
        return rawPath;
    }

    public String body() {
        return body;
    }

    public String contentType() {
        return contentType;
    }

    public String method() {
        return method;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Map<String, String> queryParameters() {
        return queryParameters;
    }

    public Map<String, String> pathParameters() {
        return pathParameters;
    }

    public Map<String, Map<String, String>> matrixParameters() {
        if (matrixParameters == null) {
            matrixParameters = new HashMap<>();

            final String[] split = url.split("/");

            for (String s : split) {
                if (!s.contains(";")) continue;

                final String[] result = s.split(";");

                final String path = result[0];

                for (int i = 1; i < result.length; i++) {
                    final String[] keyValue = result[i].split("=");

                    final String key = keyValue[0];
                    final String value = keyValue[1];

                    Map<String, String> params = matrixParameters.computeIfAbsent(path, k -> new HashMap<>());

                    params.put(key, value);
                }
            }
        }

        return matrixParameters;
    }

    static class Builder {
        private String url;
        private String rawPath;
        private String body;
        private String contentType;
        private String method;
        private Map<String, String> headers;
        private Map<String, String> queryParameters;
        private Map<String, String> pathParameters;

        private Builder self = this;

        Builder url(String url) {
            this.url = url;
            return self;
        }

        Builder rawPath(String rawPath) {
            this.rawPath = rawPath;
            return self;
        }

        Builder contentType(String contentType) {
            this.contentType = contentType;
            return self;
        }

        Builder body(String body) {
            this.body = body;
            return self;
        }

        Builder method(String method) {
            this.method = method;
            return self;
        }

        Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return self;
        }

        Builder queryParameters(Map<String, String> queryParameters) {
            this.queryParameters = queryParameters;
            return self;
        }

        Builder pathParameters(Map<String, String> pathParameters) {
            this.pathParameters = pathParameters;
            return self;
        }

        Request build() {
            return new Request(url, rawPath, body, contentType, method, headers, queryParameters, pathParameters);
        }
    }
}

