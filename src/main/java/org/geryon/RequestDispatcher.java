package org.geryon;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.geryon.exceptions.AmbiguousRoutingException;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static java.util.concurrent.CompletableFuture.supplyAsync;

/**
 * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
 */
public class RequestDispatcher implements BiConsumer<FullHttpRequest, ChannelHandlerContext> {
    public static final Pattern PATTERN_PATH_PARAM = Pattern.compile("^?(:(.)+/|:(.)+)");
    private final List<RequestHandler> requestHandlers = RequestHandlersHolder.requestHandlers();

    @Override
    public void accept(FullHttpRequest httpRequest, ChannelHandlerContext ctx) {
        final RequestExecution execution = getHandler(httpRequest);
        final RequestHandler handler = execution.handler;

        handler.func().apply(execution.request).thenAcceptAsync((r) -> {
            if (r instanceof Response) {
                Response resp = (Response) r;

                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(resp
                        .getHttpStatus()), copiedBuffer(resp.getBody() == null ? new byte[]{} : resp.getBody()
                                                                                                    .getBytes()));

                if (HttpUtil.isKeepAlive(httpRequest)) {
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                }

                response.headers()
                        .set(HttpHeaderNames.CONTENT_TYPE, resp.getContentType() != null ? resp.getContentType() : handler
                                .produces());
                response.headers()
                        .set(HttpHeaderNames.CONTENT_LENGTH, resp.getBody() == null ? 0 : resp.getBody().length());

                resp.getHeaders().forEach((k, v) -> response.headers().set(k, v));

                ctx.writeAndFlush(response);
            } else {
                FullHttpResponse response;
                String resp = null;

                if (r != null) {
                    resp = r.toString();
                    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, copiedBuffer(resp
                            .getBytes()));
                } else {
                    response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
                }

                if (HttpUtil.isKeepAlive(httpRequest)) {
                    response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
                }

                response.headers().set(HttpHeaderNames.CONTENT_TYPE, handler.produces());

                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, resp == null ? 0 : resp.length());

                ctx.writeAndFlush(response);
            }
        }).thenRun(httpRequest::release);
    }

    public RequestExecution getHandler(FullHttpRequest httpRequest) {
        final String uri = httpRequest.uri().split("\\?")[0];
        final String method = httpRequest.method().name();

        List<RequestExecution> candidates = null;

        for (RequestHandler handler : requestHandlers) {
            if (handler.path().equals(uri)) {
                final Request request = getRequest(httpRequest, uri, getHeaders(httpRequest), null);

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

            final Request request = getRequest(httpRequest, uri, getHeaders(httpRequest), mapBuilder.build());

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

            if (!Objects.equals(handler.method(), method) && mainCandidate != null) {
                mainCandidate = new RequestExecution(methodNotAllowed(), candidate.request, true);
                continue;
            }

            if (handler.path().equals(uri)) {
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

    private Request getRequest(FullHttpRequest httpRequest, String uri, Map<String, String> headers, Map<String, String> pathParameters) {
        return new Request.Builder().body(httpRequest.content().toString(Charset.forName("UTF-8")))
                                    .contentType(headers.get("Content-Type"))
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
