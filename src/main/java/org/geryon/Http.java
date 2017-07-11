package org.geryon;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.geryon.RequestHandlersHolder.addHandler;

public class Http {
    private static String defaultContentType = "text/plain";
    private static Integer port;
    private static Integer eventLoopThreadNumber;
    private static HttpServer httpServer;

    private Http() {
    }

    private static void init() {
        int port = Http.port == null ? 8080 : Http.port;
        int eventLoopThreadNumber = Http.eventLoopThreadNumber == null ? 1 : Http.eventLoopThreadNumber;
        httpServer = new HttpServer(port, eventLoopThreadNumber);
        httpServer.start();
    }

    public static Response.Builder response() {
        return new Response.Builder();
    }

    public static void get(String path, Function<Request, CompletableFuture<?>> handler) {
        get(path, defaultContentType, null, handler);
    }

    public static void get(String path, Function<Request, Boolean> matcher, Function<Request, CompletableFuture<?>> handler) {
        get(path, defaultContentType, matcher, handler);
    }

    public static void get(String path, String produces, Function<Request, Boolean> matcher, Function<Request, CompletableFuture<?>> handler) {
        handle(path, produces, "GET", matcher, handler);
    }

    public static void post(String path, String produces, Function<Request, Boolean> matcher, Function<Request, CompletableFuture<?>> handler) {
        handle(path, produces, "POST", matcher, handler);
    }

    public static void post(String path, Function<Request, Boolean> matcher, Function<Request, CompletableFuture<?>> handler) {
        handle(path, defaultContentType, "POST", matcher, handler);
    }

    public static void post(String path, Function<Request, CompletableFuture<?>> handler) {
        handle(path, defaultContentType, "POST", null, handler);
    }

    public static void put(String path, String produces, Function<Request, Boolean> matcher, Function<Request, CompletableFuture<?>> handler) {
        handle(path, produces, "PUT", matcher, handler);
    }

    public static void put(String path, Function<Request, Boolean> matcher, Function<Request, CompletableFuture<?>> handler) {
        handle(path, defaultContentType, "PUT", matcher, handler);
    }

    public static void put(String path, Function<Request, CompletableFuture<?>> handler) {
        handle(path, defaultContentType, "PUT", null, handler);
    }

    public static void patch(String path, String produces, Function<Request, Boolean> matcher, Function<Request, CompletableFuture<?>> handler) {
        handle(path, produces, "PATCH", matcher, handler);
    }

    public static void patch(String path, Function<Request, Boolean> matcher, Function<Request, CompletableFuture<?>> handler) {
        handle(path, defaultContentType, "PATCH", matcher, handler);
    }

    public static void patch(String path, Function<Request, CompletableFuture<?>> handler) {
        handle(path, defaultContentType, "PATCH", null, handler);
    }

    public static void delete(String path, String produces, Function<Request, Boolean> matcher, Function<Request, CompletableFuture<?>> handler) {
        handle(path, produces, "DELETE", matcher, handler);
    }

    public static void delete(String path, Function<Request, Boolean> matcher, Function<Request, CompletableFuture<?>> handler) {
        handle(path, defaultContentType, "DELETE", matcher, handler);
    }

    public static void delete(String path, Function<Request, CompletableFuture<?>> handler) {
        handle(path, defaultContentType, "DELETE", null, handler);
    }

    public static void handle(String path, String produces, String method, Function<Request, Boolean> matcher, Function<Request, CompletableFuture<?>> handler) {
        if (httpServer == null) init();
        addHandler(new RequestHandler(method, path, produces, handler, matcher));
    }

    public static Response ok(String body) {
        return response().httpStatus(200).body(body).build();
    }

    public static Response ok() {
        return response().httpStatus(200).build();
    }

    public static Response noContent() {
        return response().httpStatus(204).build();
    }

    public static Response accepted() {
        return response().httpStatus(202).build();
    }

    public static Response accepted(String body) {
        return response().httpStatus(202).body(body).build();
    }

    public static Response created(String uri) {
        return response().httpStatus(201).headers(Maps.<String, String>newMap().put("Location", uri).build()).build();
    }

    public static Response created(String uri, String body) {
        return response().httpStatus(201).body(body).headers(Maps.<String, String>newMap().put("Location", uri).build()).build();
    }

    public static Response notFound(String body) {
        return response().httpStatus(404).body(body).build();
    }

    public static Response notFound() {
        return response().httpStatus(404).build();
    }

    public static Response conflict() {
        return response().httpStatus(419).build();
    }

    public static Response conflict(String body) {
        return response().httpStatus(419).body(body).build();
    }

    public static Response internalServerError() {
        return response().httpStatus(500).build();
    }

    public static Response unauthorized() {
        return response().httpStatus(401).body("unauthorized").build();
    }

    public static Response internalServerError(String body) {
        return response().httpStatus(500).body(body).build();
    }

    public static <T> CompletableFuture<T> supply(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }

    public static <T> CompletableFuture<T> supply(ExecutorService executor, Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    public static CompletableFuture<Void> run(Runnable runnable) {
        return CompletableFuture.runAsync(runnable);
    }

    public static void port(Integer port) {
        Http.port = port;
    }

    public static void eventLoopThreadNumber(Integer eventLoopThreadNumber) {
        Http.eventLoopThreadNumber = eventLoopThreadNumber;
    }

    public static void stop(){
        httpServer.shutdown();
    }

    public static void defaultContentType(String defaultContentType) {
        Http.defaultContentType = defaultContentType;
    }

    public static String defaultContentType() {
        return defaultContentType;
    }

    public static Integer port() {
        return port;
    }

    public static Integer eventLoopThreadNumber() {
        return eventLoopThreadNumber;
    }
}
