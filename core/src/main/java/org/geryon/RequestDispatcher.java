package org.geryon;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
public class RequestDispatcher implements BiConsumer<FullHttpRequest, ChannelHandlerContext> {
    private final List<RequestHandler> requestHandlers = RequestHandlers.requestHandlers();

    @Override
    public void accept(FullHttpRequest httpRequest, ChannelHandlerContext ctx) {
        final RequestExecution execution = getHandler(httpRequest);
        final RequestHandler handler = execution.handler;

        handler.func().apply(execution.request).thenAcceptAsync((r) -> {
            FullHttpResponse response;

            if (r instanceof Response) {
                response = standardResponse(httpRequest, handler, (Response) r);
            } else {
                response = rawResponse(httpRequest, handler, r);
            }

            ctx.writeAndFlush(response);
        }).exceptionally(e -> {
            Throwable ex = (e instanceof CompletionException ? e.getCause() : e);
            final BiFunction<Throwable, Request, Response> exceptionHandler = ExceptionHandlers.getHandler(ex.getClass());
            final Response response = exceptionHandler.apply(ex, execution.request);
            FullHttpResponse httpResponse = standardResponse(httpRequest, handler, response);
            ctx.writeAndFlush(httpResponse);
            return null;
        }).thenRun(httpRequest::release);
    }

    private FullHttpResponse rawResponse(FullHttpRequest httpRequest, RequestHandler handler, Object r) {
        FullHttpResponse response;
        String resp = null;

        if (r != null) {
            resp = r.toString();
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, copiedBuffer(resp.getBytes()));
        } else {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
        }

        if (HttpUtil.isKeepAlive(httpRequest)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, handler.produces());
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp == null ? 0 : resp.length());

        if (handler.defaultHeaders() != null) {
            setHeaders(response, handler.defaultHeaders());
        }

        return response;
    }

    private FullHttpResponse standardResponse(FullHttpRequest httpRequest, RequestHandler handler, Response resp) {
        ByteBuf body = copiedBuffer(resp.getBody() == null ? new byte[]{} : resp.getBody().getBytes());
        HttpResponseStatus status = HttpResponseStatus.valueOf(resp.getHttpStatus());
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, body);

        if (HttpUtil.isKeepAlive(httpRequest)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        String produces = resp.getContentType() != null ? resp.getContentType() : handler.produces();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, produces);

        int contentLength = resp.getBody() == null ? 0 : resp.getBody().length();
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, contentLength);

        if (handler.defaultHeaders() != null) {
            setHeaders(response, handler.defaultHeaders());
        }

        if (resp.getHeaders() != null) {
            setHeaders(response, resp.getHeaders());
        }

        return response;
    }


    private void setHeaders(FullHttpResponse response, Map<String, String> headers) {
        headers.forEach((k, v) -> response.headers().set(k, v));
    }

    class UrlMatrixParameterLazyEval {
        private String rawPath;
        private String uri;

        public UrlMatrixParameterLazyEval(String uri) {
            this.uri = uri;
        }

        public String rawPath() {
            if (rawPath == null) {
                rawPath = uri.replaceAll(";(.)+/", "/").replaceAll(";(.)+", "");
            }

            return rawPath;
        }
    }

    public RequestExecution getHandler(FullHttpRequest httpRequest) {
        final String uri = httpRequest.uri().split("\\?")[0];
        final String method = httpRequest.method().name();

        List<RequestExecution> candidates = null;

        final UrlMatrixParameterLazyEval matrixParameterLazyEval = new UrlMatrixParameterLazyEval(uri);

        for (RequestHandler handler : requestHandlers) {
            if (handler.path().equals(uri) || handler.path().equals(matrixParameterLazyEval.rawPath())) {
                final Request request = getRequest(httpRequest, uri, matrixParameterLazyEval.rawPath(), getHeaders(httpRequest), null);

                if (!handler.matcher().apply(request)) {
                    continue;
                }

                if (candidates == null) candidates = new ArrayList<>();
                candidates.add(new RequestExecution(handler, request));
                continue;
            }

            if (handler.path().split("/").length != uri.split("/").length) {
                continue;
            }

            final List<String> wantedFields = handler.wantedPathParameters();

            if (wantedFields.isEmpty()) {
                continue;
            }

            final Matcher m = Pattern.compile(handler.pathAsPattern()).matcher(uri);

            if (!m.find() || !m.group().equals(uri)) {
                continue;
            }

            final Maps.MapBuilder<String, String> mapBuilder = Maps.newMap();

            for (int i = 1; i <= m.groupCount(); i++) {
                mapBuilder.put(wantedFields.get(i - 1), m.group(i).replace("/", ""));
            }

            final Request request = getRequest(httpRequest, uri, matrixParameterLazyEval.rawPath(), getHeaders(httpRequest), mapBuilder.build());

            if (!handler.matcher().apply(request)) {
                continue;
            }

            if (candidates == null) candidates = new ArrayList<>();
            candidates.add(new RequestExecution(handler, request));
        }

        if (candidates == null || candidates.isEmpty()) {
            return new RequestExecution(notFoundHandler(), null);
        }

        RequestExecution mainCandidate = null;

        for (RequestExecution candidate : candidates) {
            final RequestHandler handler = candidate.handler;

            final boolean sameMethod = Objects.equals(handler.method(), method);
            if (!sameMethod && (mainCandidate == null || mainCandidate.handledInternally)) {
                mainCandidate = new RequestExecution(methodNotAllowed(), candidate.request, true);
                continue;
            }

            if (sameMethod && (handler.path().equals(uri) || handler.path().equals(matrixParameterLazyEval.rawPath()))) {
                mainCandidate = candidate;
                continue;
            }

            if (mainCandidate == null || mainCandidate.handledInternally) {
                mainCandidate = candidate;
            }
        }

        return mainCandidate;
    }

    public RequestHandler notFoundHandler() {
        return new RequestHandler("text/plain", r -> CompletableFuture.completedFuture(new Response.Builder().httpStatus(404)
                                                                                                             .body("not found")
                                                                                                             .build()));
    }

    public RequestHandler methodNotAllowed() {
        return new RequestHandler("text/plain", r -> CompletableFuture.completedFuture(new Response.Builder().httpStatus(405)
                                                                                                             .body("method not allowed")
                                                                                                             .build()));
    }

    private Request getRequest(FullHttpRequest httpRequest, String uri, String rawPath, Map<String, String> headers, Map<String, String> pathParameters) {
        return new Request.Builder().body(httpRequest.content().toString(Charset.forName("UTF-8")))
                                    .contentType(headers.get("Content-Type"))
                                    .rawPath(rawPath)
                                    .headers(headers)
                                    .method(httpRequest.method().name())
                                    .pathParameters(pathParameters)
                                    .queryParameters(new QueryStringDecoder(httpRequest.uri()).parameters()
                                                                                              .entrySet()
                                                                                              .stream()
                                                                                              .collect(Collectors.toMap(Map.Entry::getKey, e -> e
                                                                                                      .getValue()
                                                                                                      .get(0))))
                                    .url(uri)
                                    .build();
    }

    private Map<String, String> getHeaders(FullHttpRequest httpRequest) {
        return httpRequest.headers()
                          .entries()
                          .stream()
                          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static class RequestExecution {
        private RequestHandler handler;
        private Request request;
        private Boolean handledInternally;

        public RequestExecution(RequestHandler handler, Request request) {
            this.handler = handler;
            this.request = request;
            this.handledInternally = false;
        }

        public RequestExecution(RequestHandler handler, Request request, Boolean handledInternally) {
            this.handler = handler;
            this.request = request;
            this.handledInternally = handledInternally;
        }
    }
}
