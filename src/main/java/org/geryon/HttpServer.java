package org.geryon;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
 */
class HttpServer {
    private ChannelFuture channel;
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;
    private Integer port;

    private static Map<RequestIdentifier, Function<Request, CompletableFuture<Response>>> handlers = new HashMap<>();

    public HttpServer(Integer port) {
        masterGroup = new NioEventLoopGroup();
        slaveGroup = new NioEventLoopGroup();
        this.port = port;
    }

    public void addHandler(String path, String method, Function<Request, CompletableFuture<Response>> handler) {
        handlers.put(new RequestIdentifier(method, path), handler);
    }

    public Function<Request, CompletableFuture<Response>> notFoundHandler(){
        return request -> CompletableFuture.supplyAsync(() -> new Response.Builder().httpStatus(404).body("not found").build());
    }

    public void handleHttpRequest(FullHttpRequest httpRequest, ChannelHandlerContext ctx) {
        final String uri = httpRequest.uri().split("\\?")[0];
        final Function<Request, CompletableFuture<Response>> handler = handlers.getOrDefault(new RequestIdentifier(httpRequest.method().name(), uri), notFoundHandler());

        final Map<String, String> headers = httpRequest.headers()
                                                       .entries()
                                                       .stream()
                                                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        final Request request = new Request.Builder().body(httpRequest.content().toString(Charset.forName("UTF-8")))
                                                     .contentType(headers.get("Content-Type"))
                                                     .headers(headers)
                                                     .method(httpRequest.method().name())
                                                     .queryParameters(new QueryStringDecoder(httpRequest.uri()).parameters().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().get(0))))
                                                     .url(uri)
                                                     .build();

        handler.apply(request).thenAcceptAsync((r) -> {
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(r.getHttpStatus()), copiedBuffer(r
                    .getBody()
                    .getBytes()));

            if (HttpUtil.isKeepAlive(httpRequest)) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, r.getContentType());

            if(r.getBody() != null){
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, r.getBody().length());
            }

            ctx.writeAndFlush(response);
        });
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        try {
            // #3
            final ServerBootstrap bootstrap = new ServerBootstrap().group(masterGroup, slaveGroup)
                                                                   .channel(NioServerSocketChannel.class)
                                                                   .childHandler(new ChannelInitializer<SocketChannel>() {
                                                                       @Override
                                                                       public void initChannel(final SocketChannel ch) throws Exception {
                                                                           ch.pipeline().addLast("codec", new HttpServerCodec());
                                                                           ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512 * 1024));
                                                                           ch.pipeline().addLast("request", new ChannelInboundHandlerAdapter() {
                                                                                 @Override
                                                                                 public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                                                                     if (msg instanceof FullHttpRequest) {
                                                                                         handleHttpRequest((FullHttpRequest) msg, ctx);
                                                                                     } else {
                                                                                         super.channelRead(ctx, msg);
                                                                                     }
                                                                                 }

                                                                                 @Override
                                                                                 public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
                                                                                     ctx.flush();
                                                                                 }

                                                                                 @Override
                                                                                 public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                                                                                     ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, copiedBuffer(cause
                                                                                             .getMessage()
                                                                                             .getBytes())));
                                                                                 }
                                                                             });
                                                                       }
                                                                   })
                                                                   .option(ChannelOption.SO_BACKLOG, 128)
                                                                   .childOption(ChannelOption.SO_KEEPALIVE, true);
            channel = bootstrap.bind(port).sync();
        } catch (final InterruptedException e) {
        }
    }

    public void shutdown() {
        slaveGroup.shutdownGracefully();
        masterGroup.shutdownGracefully();

        try {
            channel.channel().closeFuture().sync();
        } catch (InterruptedException e) {
        }
    }
}