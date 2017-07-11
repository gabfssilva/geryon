package org.geryon;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.netty.buffer.Unpooled.copiedBuffer;

/**
 * @author Gabriel Francisco <gabfssilva@gmail.com>
 */
class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private RequestDispatcher requestDispatcher;
    private SingleThreadEventLoop singleThreadEventLoop;
    private ChannelFuture future;
    private final EventLoopGroup masterGroup;
    private Integer port;
    private Integer threads = 1;

    public HttpServer(Integer port) {
        masterGroup = new NioEventLoopGroup(threads, Executors.newSingleThreadExecutor());
        singleThreadEventLoop = new DefaultEventLoop();
        this.port = port;
        this.requestDispatcher = new RequestDispatcher();
    }

    public void start() {
        logger.info("Starting server on port " + port);
        logger.info("Event loop will run on " + threads + " thread(s)");

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        try {
            final ServerBootstrap bootstrap = new ServerBootstrap().group(masterGroup)
                                                                   .channel(NioServerSocketChannel.class)
                                                                   .childHandler(new ChannelInitializer<SocketChannel>() {
                                                                       @Override
                                                                       public void initChannel(final SocketChannel ch) throws Exception {
                                                                           ch.pipeline().addLast("codec", new HttpServerCodec());
                                                                           ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512 * 1024));
                                                                           ch.pipeline().addLast(singleThreadEventLoop, "request", new ChannelInboundHandlerAdapter() {
                                                                                 @Override
                                                                                 public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                                                                     if (msg instanceof FullHttpRequest) {
                                                                                         requestDispatcher.accept((FullHttpRequest) msg, ctx);
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
            future = bootstrap.bind(port).sync();
            logger.info("Netty server started");
        } catch (final InterruptedException e) {
        }
    }

    public void shutdown() {
        try {
            final long init = System.currentTimeMillis();
            masterGroup.shutdownGracefully(0, 10, TimeUnit.SECONDS).get();
            logger.info("Netty server stopped in " + (System.currentTimeMillis() - init) + " ms");
        } catch (InterruptedException ignored) {
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}