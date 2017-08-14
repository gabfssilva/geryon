package org.geryon.scaladsl

import java.util.concurrent.CompletableFuture

import org.geryon.{ExceptionHandlers, Request, RequestHandler}
import org.geryon.RequestHandlers.addHandler
import java.util.function.{Function => JavaFunction}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
  */
trait HandlerRegisterSupport extends ModelConversions {
  private[geryon] val defaultThreadExecutor: ExecutionContext

  def handlerFor[T <: Throwable](handler: ScalaDslRequest => T => ScalaDslResponse)(implicit m: Manifest[T]): Unit = {
    val clazz: Class[T] = m.runtimeClass.asInstanceOf[Class[T]]
    ExceptionHandlers.addHandler(clazz, (t: T, r: Request) => handler(r)(t))
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

  def handle(path: String, produces: String, method: String, matcher: ScalaDslRequest => Boolean, handler: ScalaDslRequest => Future[_ >: Any])(implicit ec: ExecutionContext = defaultThreadExecutor): Unit = {
    if (HttpServerInfoHolder.httpServer == null) init()

    val javaFunc: JavaFunction[Request, CompletableFuture[_ <: Any]] = (t: Request) => {
      val promise = new CompletableFuture[Any]()

      try {
        handler
          .apply(t)
          .onComplete {
            case Success(response: ScalaDslResponse) => promise.complete(response.asJavaResponse)
            case Success(anyResponse) => promise.complete(anyResponse)
            case Failure(e) => promise.completeExceptionally(e)
          }
      } catch {
        case t: Throwable => promise.completeExceptionally(t)
      }

      promise
    }

    val javaMatcher: JavaFunction[Request, java.lang.Boolean] = if (matcher == null) null else (t: Request) => matcher.apply(t)

    addHandler(new RequestHandler(method, path, produces, javaFunc, javaMatcher, HttpServerInfoHolder.defaultHeaders.asJava))
  }
}
