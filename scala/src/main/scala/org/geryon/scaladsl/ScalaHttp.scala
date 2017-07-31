package org.geryon.scaladsl

import java.util.concurrent.{CompletableFuture, Executors}
import java.util.function.{Function => JavaFunction}

import org.geryon.RequestHandlers._
import org.geryon._

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success}

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
trait ScalaHttp extends RequestParameters{
  private val singleThreadExecutor = ExecutionContext.fromExecutor(Executors.newSingleThreadExecutor())

  protected[geryon] def init(): Unit = {
    HttpServerInfoHolder.httpServer = new HttpServer(HttpServerInfoHolder.port, HttpServerInfoHolder.eventLoopThreadNumber)
    HttpServerInfoHolder.httpServer.start()
  }

  implicit def asJavaRequest(request: ScalaDslRequest): Request = request.original

  implicit def asScalaDslRequest(request: Request): ScalaDslRequest = ScalaDslRequest(request)

  implicit def asJavaResponse(response: ScalaDslResponse): Response = response.asJavaResponse

  def supply[T](supplier: => T)(implicit ec: ExecutionContext = ExecutionContext.global): Future[T] = {
    Future {
      supplier
    }
  }

  def handlerFor[T <: Throwable](handler: (T, ScalaDslRequest) => ScalaDslResponse)(implicit m: Manifest[T]): Unit = {
    val clazz: Class[T] = m.runtimeClass.asInstanceOf[Class[T]]
    ExceptionHandlers.addHandler(clazz, (t: T, r: Request) => handler(t, r))
  }

  def get(path: String)(handler: ScalaDslRequest => Future[_]): Unit = {
    get(path, HttpServerInfoHolder.defaultContentType, null)(handler)
  }

  def get(path: String, matcher: ScalaDslRequest => Boolean)(handler: ScalaDslRequest => Future[_]): Unit = {
    get(path, HttpServerInfoHolder.defaultContentType, matcher)(handler)
  }

  def get(path: String, produces: String, matcher: ScalaDslRequest => Boolean)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, produces, "GET", matcher, handler)
  }

  def post(path: String, produces: String, matcher: ScalaDslRequest => Boolean)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, produces, "POST", matcher, handler)
  }

  def post(path: String, matcher: ScalaDslRequest => Boolean)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, HttpServerInfoHolder.defaultContentType, "POST", matcher, handler)
  }

  def post(path: String)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, HttpServerInfoHolder.defaultContentType, "POST", null, handler)
  }

  def put(path: String, produces: String, matcher: ScalaDslRequest => Boolean)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, produces, "PUT", matcher, handler)
  }

  def put(path: String, matcher: ScalaDslRequest => Boolean)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, HttpServerInfoHolder.defaultContentType, "PUT", matcher, handler)
  }

  def put(path: String)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, HttpServerInfoHolder.defaultContentType, "PUT", null, handler)
  }

  def patch(path: String, produces: String, matcher: ScalaDslRequest => Boolean)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, produces, "PATCH", matcher, handler)
  }

  def patch(path: String, matcher: ScalaDslRequest => Boolean)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, HttpServerInfoHolder.defaultContentType, "PATCH", matcher, handler)
  }

  def patch(path: String)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, HttpServerInfoHolder.defaultContentType, "PATCH", null, handler)
  }

  def delete(path: String, produces: String, matcher: ScalaDslRequest => Boolean)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, produces, "DELETE", matcher, handler)
  }

  def delete(path: String, matcher: ScalaDslRequest => Boolean)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, HttpServerInfoHolder.defaultContentType, "DELETE", matcher, handler)
  }

  def delete(path: String)(handler: ScalaDslRequest => Future[_]): Unit = {
    handle(path, HttpServerInfoHolder.defaultContentType, "DELETE", null, handler)
  }

  def handle(path: String, produces: String, method: String, matcher: ScalaDslRequest => Boolean, handler: ScalaDslRequest => Future[_ >: Any])(implicit ec: ExecutionContext = singleThreadExecutor): Unit = {
    if (HttpServerInfoHolder.httpServer == null) init()

    val javaFunc: JavaFunction[Request, CompletableFuture[_ <: Any]] = (t: Request) => {
      val promise = new CompletableFuture[Any]()

      handler
        .apply(t)
        .onComplete {
          case Success(response: ScalaDslResponse) => promise.complete(response.asJavaResponse)
          case Success(anyResponse) => promise.complete(anyResponse)
          case Failure(e) => promise.completeExceptionally(e)
        }

      promise
    }

    val javaMatcher: JavaFunction[Request, java.lang.Boolean] = if (matcher == null) null else (t: Request) => matcher.apply(t)

    addHandler(new RequestHandler(method, path, produces, javaFunc, javaMatcher, HttpServerInfoHolder.defaultHeaders.asJava))
  }

  def response = new ScalaDslResponseBuilder

  def ok(body: String): ScalaDslResponse = response.httpStatus(200).body(body).build

  def ok: ScalaDslResponse = response.httpStatus(200).build

  def noContent: ScalaDslResponse = response.httpStatus(204).build

  def accepted: ScalaDslResponse = response.httpStatus(202).build

  def accepted(body: String): ScalaDslResponse = response.httpStatus(202).body(body).build

  def created(uri: String): ScalaDslResponse = response.httpStatus(201).headers("Location" -> uri).build

  def created(uri: String, body: String): ScalaDslResponse = response.httpStatus(201).body(body).headers("Location" -> uri).build

  def notFound(body: String): ScalaDslResponse = response.httpStatus(404).body(body).build

  def notFound: ScalaDslResponse = response.httpStatus(404).build

  def conflict: ScalaDslResponse = response.httpStatus(419).build

  def conflict(body: String): ScalaDslResponse = response.httpStatus(419).body(body).build

  def internalServerError: ScalaDslResponse = response.httpStatus(500).build

  def unauthorized: ScalaDslResponse = response.httpStatus(401).body("unauthorized").build

  def internalServerError(body: String): ScalaDslResponse = response.httpStatus(500).body(body).build

  def port(port: Integer): Unit = {
    HttpServerInfoHolder.port = port
  }

  def eventLoopThreadNumber(eventLoopThreadNumber: Integer): Unit = {
    HttpServerInfoHolder.eventLoopThreadNumber = eventLoopThreadNumber
  }

  def stop(): Unit = {
    HttpServerInfoHolder.httpServer.shutdown()
    HttpServerInfoHolder.httpServer = null
  }

  def defaultContentType(defaultContentType: String): Unit = {
    HttpServerInfoHolder.defaultContentType = defaultContentType
  }

  def defaultHeader(header: (String, String)): Unit = {
    val (name, value) = header
    HttpServerInfoHolder.defaultHeaders(name) = value
  }

  def serverInfo = HttpServerInfoHolder

  object HttpServerInfoHolder {
    var defaultContentType = "text/plain"
    var port = 8080
    var eventLoopThreadNumber = 1
    var defaultHeaders: mutable.Map[String, String] = mutable.Map[String, String]()
    var httpServer: HttpServer = _
  }

}
