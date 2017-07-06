package org.geryon;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class Http {
    private static String defaultContentType = "text/plain";
    private static Integer port;
    private static HttpServer httpServer;

    private Http() {
    }

    private static void init() {
        int port = Http.port == null ? 8080 : Http.port;
        httpServer = new HttpServer(port);
        httpServer.start();
    }

    public static Response.Builder response() {
        return new Response.Builder();
    }

    public static void get(String path, Function<Request, CompletableFuture<?>> handler) {
        get(path, defaultContentType, handler);
    }

    public static void get(String path, String produces, Function<Request, CompletableFuture<?>> handler) {
        handle(path, produces, "GET", handler);
    }

    public static void post(String path, String produces, Function<Request, CompletableFuture<?>> handler) {
        handle(path, produces, "POST", handler);
    }

    public static void put(String path, String produces, Function<Request, CompletableFuture<?>> handler) {
        handle(path, produces, "PUT", handler);
    }

    public static void patch(String path, String produces, Function<Request, CompletableFuture<?>> handler) {
        handle(path, produces, "PATCH", handler);
    }

    public static void delete(String path, String produces, Function<Request, CompletableFuture<?>> handler) {
        handle(path, produces, "DELETE", handler);
    }

    public static void handle(String path, String produces, String method, Function<Request, CompletableFuture<?>> handler) {
        if (httpServer == null) init();
        httpServer.addHandler(path, method, new RequestHandler(handler, produces));
    }

    public static CompletableFuture<?> ok(Supplier<String> bodySupplier) {
        return supplyAsync(bodySupplier).thenApply(r -> response().httpStatus(200).body(r).build());
    }

    public static CompletableFuture<?> created(Supplier<String> uriSupplier) {
        return supplyAsync(uriSupplier).thenApply(r -> response().httpStatus(201).headers(Maps.<String, String>newMap().put("Location", r).build()).build());
    }

    public static CompletableFuture<?> notFound(Supplier<String> bodySupplier) {
        return supplyAsync(bodySupplier).thenApply(r -> response().httpStatus(200).body(r).build());
    }

    public static CompletableFuture<?> internalServerError(Supplier<String> bodySupplier) {
        return supplyAsync(bodySupplier).thenApply(r -> response().httpStatus(200).body(r).build());
    }

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable);
    }

    public static void port(Integer port) {
        Http.port = port;
    }

    public static void defaultContentType(String defaultContentType) {
        Http.defaultContentType = defaultContentType;
    }

}
