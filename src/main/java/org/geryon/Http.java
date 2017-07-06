package org.geryon;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public class Http {
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

    public static void get(String path, Function<Request, CompletableFuture<Response>> handler) {
        handle(path, "GET", handler);
    }

    public static void post(String path, Function<Request, CompletableFuture<Response>> handler) {
        handle(path, "POST", handler);
    }

    public static void put(String path, Function<Request, CompletableFuture<Response>> handler) {
        handle(path, "PUT", handler);
    }

    public static void patch(String path, Function<Request, CompletableFuture<Response>> handler) {
        handle(path, "PATCH", handler);
    }

    public static void delete(String path, Function<Request, CompletableFuture<Response>> handler) {
        handle(path, "DELETE", handler);
    }

    public static void handle(String path, String method, Function<Request, CompletableFuture<Response>> handler) {
        if (httpServer == null) init();
        httpServer.addHandler(path, method, handler);
    }

    public static <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }

    public static void port(Integer port) {
        Http.port = port;
    }
}
