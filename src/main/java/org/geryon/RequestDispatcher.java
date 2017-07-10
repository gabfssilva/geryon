package org.geryon;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
        });
    }

    public RequestExecution getHandler(FullHttpRequest httpRequest) {
        final String uri = httpRequest.uri().split("\\?")[0];
        final String method = httpRequest.method().name();

        for (RequestHandler handler : requestHandlers) {
            if (!Objects.equals(handler.method(), method)) {
                continue;
            }

            if (handler.path().equals(uri)) {
                final Request request = getRequest(httpRequest, uri, getHeaders(httpRequest), null);

                if (!handler.condition().apply(request)) {
                    continue;
                }

                return new RequestExecution(handler, request);
            }

            if (handler.path().split("/").length != uri.split("/").length) {
                continue;
            }

            final List<String> wantedFields = getFields(handler.path());

            if (wantedFields.isEmpty()) {
                continue;
            }

            String pathAsPattern = getPathAsPattern(wantedFields, handler.path());
            final Matcher m = Pattern.compile(pathAsPattern).matcher(uri);

            if (!m.find() || !m.group().equals(uri)) {
                continue;
            }

            final Maps.MapBuilder<String, String> mapBuilder = Maps.newMap();

            for (int i = 1; i <= m.groupCount(); i++) {
                mapBuilder.put(wantedFields.get(i - 1), m.group(i).replace("/", ""));
            }

            final Request request = getRequest(httpRequest, uri, getHeaders(httpRequest), mapBuilder.build());

            if (!handler.condition().apply(request)) {
                continue;
            }

            return new RequestExecution(handler, request);
        }

        return new RequestExecution(notFoundHandler(), null);
    }

    public RequestHandler notFoundHandler() {
        return new RequestHandler("text/plain", r -> supplyAsync(() -> new Response.Builder().httpStatus(404)
                                                                                             .body("not found")
                                                                                             .build()));
    }

    private static Map<String, String> getPathParameters(List<String> fields, String path, String uri) {
        String pathAsPattern = getPathAsPattern(fields, path);
        final Matcher m = Pattern.compile(pathAsPattern).matcher(uri);

        final Maps.MapBuilder<String, String> mapBuilder = Maps.newMap();

        if (m.find()) {
            for (int i = 1; i <= m.groupCount(); i++) {
                mapBuilder.put(fields.get(i - 1), m.group(i).replace("/", ""));
            }
        }

        return mapBuilder.build();
    }

    private static String getPathAsPattern(List<String> fields, String path) {
        String pathAsPattern = path;

        for (String f : fields) {
            pathAsPattern = pathAsPattern.replace("/:" + f, "(/.+)");
        }
        return pathAsPattern;
    }

    private static List<String> getFields(String path) {
        final Matcher matcher = PATTERN_PATH_PARAM.matcher(path);

        List<String> fields = new ArrayList<>();

        while (matcher.find()) {
            String group;

            if (matcher.group(1).contains("/")) {
                group = matcher.group(1).substring(0, matcher.group(1).indexOf("/")).replace(":", "");
            } else {
                group = matcher.group(1).replace(":", "");
            }

            fields.add(group);
        }
        return fields;
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

        public RequestExecution(RequestHandler handler, Request request) {
            this.handler = handler;
            this.request = request;
        }
    }
}
