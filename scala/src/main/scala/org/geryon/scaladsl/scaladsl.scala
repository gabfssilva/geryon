package org.geryon

import scala.collection.JavaConverters.mapAsJavaMap
import java.util.concurrent.CompletableFuture
import java.util.function

import org.geryon.RequestHandlersHolder.addHandler
import org.geryon.scaladsl.model.{ScalaDslRequest, ScalaDslResponse}

import scala.collection.mutable
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

  implicit def asJavaRequest(request: ScalaDslRequest): Request = request.original
  implicit def asScalaDslRequest(request: Request): ScalaDslRequest = ScalaDslRequest(request)
  implicit def asJavaResponse(response: ScalaDslResponse): Response = response.asJavaResponse

  def handlerFor[T <: Throwable](handler: (T, ScalaDslRequest) => ScalaDslResponse)(implicit m: Manifest[T]): Unit = {
    val clazz: Class[T] = m.runtimeClass.asInstanceOf[Class[T]]
    ExceptionHandlers.addHandler(clazz, (t: T, r: Request) => handler(t, r))
  }

  def get(path: String)(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    get(path, HttpServerHandler.defaultContentType, null)(handler)
  }

  def get(path: String, matcher: Function[ScalaDslRequest, Boolean])(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    get(path, HttpServerHandler.defaultContentType, matcher)(handler)
  }

  def get(path: String, produces: String, matcher: Function[ScalaDslRequest, Boolean])(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, produces, "GET", matcher, handler, defaultHeaders())
  }

  def post(path: String, produces: String, matcher: Function[ScalaDslRequest, Boolean])(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, produces, "POST", matcher, handler, defaultHeaders())
  }

  def post(path: String, matcher: Function[ScalaDslRequest, Boolean])(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "POST", matcher, handler, defaultHeaders())
  }

  def post(path: String)(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "POST", null, handler, defaultHeaders())
  }

  def put(path: String, produces: String, matcher: Function[ScalaDslRequest, Boolean])(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, produces, "PUT", matcher, handler, defaultHeaders())
  }

  def put(path: String, matcher: Function[ScalaDslRequest, Boolean])(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "PUT", matcher, handler, defaultHeaders())
  }

  def put(path: String)(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "PUT", null, handler, defaultHeaders())
  }

  def patch(path: String, produces: String, matcher: Function[ScalaDslRequest, Boolean])(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, produces, "PATCH", matcher, handler, defaultHeaders())
  }

  def patch(path: String, matcher: Function[ScalaDslRequest, Boolean])(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "PATCH", matcher, handler, defaultHeaders())
  }

  def patch(path: String)(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "PATCH", null, handler, defaultHeaders())
  }

  def delete(path: String, produces: String, matcher: Function[ScalaDslRequest, Boolean])(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, produces, "DELETE", matcher, handler, defaultHeaders())
  }

  def delete(path: String, matcher: Function[ScalaDslRequest, Boolean])(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "DELETE", matcher, handler, defaultHeaders())
  }

  def delete(path: String)(handler: Function[ScalaDslRequest, Future[_]])(implicit ec: ExecutionContext): Unit = {
    handle(path, HttpServerHandler.defaultContentType, "DELETE", null, handler, defaultHeaders())
  }

  def handle(path: String, produces: String, method: String, matcher: Function[ScalaDslRequest, Boolean], handler: Function[ScalaDslRequest, Future[_ >: Any]], defaultHeaders: Map[String, String])(implicit ec: ExecutionContext): Unit = {
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

    addHandler(new RequestHandler(method, path, produces, javaFunc, javaMatcher, mapAsJavaMap(defaultHeaders)))
  }

  def response = new model.ScalaDslResponseBuilder

  def ok(body: String): ScalaDslResponse = response.httpStatus(200).body(body).build

  def ok: ScalaDslResponse = response.httpStatus(200).build

  def noContent: ScalaDslResponse = response.httpStatus(204).build

  def accepted: ScalaDslResponse = response.httpStatus(202).build

  def accepted(body: String): ScalaDslResponse = response.httpStatus(202).body(body).build

  def created(uri: String): ScalaDslResponse = response.httpStatus(201).headers(Map("Location" -> uri)).build

  def created(uri: String, body: String): ScalaDslResponse = response.httpStatus(201).body(body).headers(Map("Location" -> uri)).build

  def notFound(body: String): ScalaDslResponse = response.httpStatus(404).body(body).build

  def notFound: ScalaDslResponse = response.httpStatus(404).build

  def conflict: ScalaDslResponse = response.httpStatus(419).build

  def conflict(body: String): ScalaDslResponse = response.httpStatus(419).body(body).build

  def internalServerError: ScalaDslResponse = response.httpStatus(500).build

  def unauthorized: ScalaDslResponse = response.httpStatus(401).body("unauthorized").build

  def internalServerError(body: String): ScalaDslResponse = response.httpStatus(500).body(body).build

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

  def defaultHeader(name: String, value: String): Unit = {
    HttpServerHandler.defaultHeaders(name) = value
  }

  def defaultHeaders(): Map[String, String] = {
    HttpServerHandler.defaultHeaders.toMap[String, String]
  }

  object HttpServerHandler {
    var defaultContentType = "text/plain"
    var port = 8080
    var eventLoopThreadNumber = 1
    var defaultHeaders: mutable.Map[String, String] = mutable.Map[String, String]()
    var httpServer: HttpServer = _
  }

}
