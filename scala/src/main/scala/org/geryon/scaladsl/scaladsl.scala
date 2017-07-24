package org.geryon

import java.util.concurrent.CompletableFuture
import java.util.function

import org.geryon.RequestHandlersHolder.addHandler

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
package object scaladsl {
  private def init() = {
    HttpServerHandler.httpServer = new HttpServer(HttpServerHandler.port, HttpServerHandler.eventLoopThreadNumber)
    HttpServerHandler.httpServer.start()
  }

  def get(path: String)(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    get(path, HttpServerHandler.defaultContentType, null)(handler)
  }

  def get(path: String, matcher: Function[Request, Boolean])(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    get(path, HttpServerHandler.defaultContentType, matcher)(handler)
  }

  def get(path: String, produces: String, matcher: Function[Request, Boolean])(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, produces, "GET", matcher, handler)
  }

  def post(path: String, produces: String, matcher: Function[Request, Boolean])(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, produces, "POST", matcher, handler)
  }

  def post(path: String, matcher: Function[Request, Boolean])(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "POST", matcher, handler)
  }

  def post(path: String)(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "POST", null, handler)
  }

  def put(path: String, produces: String, matcher: Function[Request, Boolean])(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, produces, "PUT", matcher, handler)
  }

  def put(path: String, matcher: Function[Request, Boolean])(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "PUT", matcher, handler)
  }

  def put(path: String)(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "PUT", null, handler)
  }

  def patch(path: String, produces: String, matcher: Function[Request, Boolean])(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, produces, "PATCH", matcher, handler)
  }

  def patch(path: String, matcher: Function[Request, Boolean])(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "PATCH", matcher, handler)
  }

  def patch(path: String)(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "PATCH", null, handler)
  }

  def delete(path: String, produces: String, matcher: Function[Request, Boolean])(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, produces, "DELETE", matcher, handler)
  }

  def delete(path: String, matcher: Function[Request, Boolean])(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "DELETE", matcher, handler)
  }

  def delete(path: String)(handler: Function[Request, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "DELETE", null, handler)
  }

  def handle(path: String, produces: String, method: String, matcher: Function[Request, Boolean], handler: Function[Request, Future[_ >: Any]])(implicit ec: ExecutionContext): Unit = {
    if (HttpServerHandler.httpServer == null) init()

    val javaFunc: function.Function[Request, CompletableFuture[_ <: Any]] = (t: Request) => {
      val promise = new CompletableFuture[Any]()

      handler
        .apply(t)
        .andThen {
          case Success(result) => promise.complete(result)
          case Failure(e) => promise.completeExceptionally(e)
        }

      promise
    }

    val javaMatcher: function.Function[Request, java.lang.Boolean] =
      if (matcher == null) null else (t: Request) => matcher.apply(t)

    addHandler(new RequestHandler(method, path, produces, javaFunc, javaMatcher))
  }

  def response = new Response.Builder

  def ok(body: String): Response = response.httpStatus(200).body(body).build

  def ok: Response = response.httpStatus(200).build

  def noContent: Response = response.httpStatus(204).build

  def accepted: Response = response.httpStatus(202).build

  def accepted(body: String): Response = response.httpStatus(202).body(body).build

  def created(uri: String): Response = response.httpStatus(201).headers(Maps.newMap[String, String].put("Location", uri).build).build

  def created(uri: String, body: String): Response = response.httpStatus(201).body(body).headers(Maps.newMap[String, String].put("Location", uri).build).build

  def notFound(body: String): Response = response.httpStatus(404).body(body).build

  def notFound: Response = response.httpStatus(404).build

  def conflict: Response = response.httpStatus(419).build

  def conflict(body: String): Response = response.httpStatus(419).body(body).build

  def internalServerError: Response = response.httpStatus(500).build

  def unauthorized: Response = response.httpStatus(401).body("unauthorized").build

  def internalServerError(body: String): Response = response.httpStatus(500).body(body).build

  def port(port: Integer): Unit = {
    HttpServerHandler.port = port
  }

  def eventLoopThreadNumber(eventLoopThreadNumber: Integer): Unit = {
    HttpServerHandler.eventLoopThreadNumber = eventLoopThreadNumber
  }

  def stop(): Unit = {
    HttpServerHandler.httpServer.shutdown()
    HttpServerHandler.httpServer = null
  }

  def defaultContentType(defaultContentType: String): Unit = {
    HttpServerHandler.defaultContentType = defaultContentType
  }

  object HttpServerHandler {
    var defaultContentType = "text/plain"
    var port = 8080
    var eventLoopThreadNumber = 1
    var httpServer: HttpServer = _
  }

}
