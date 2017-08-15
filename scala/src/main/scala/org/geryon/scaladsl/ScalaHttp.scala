package org.geryon.scaladsl

import java.util.concurrent.Executors

import org.geryon._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
trait ScalaHttp extends RequestParameters
  with HandlerRegisterSupport
  with ModelConversions
  with ResponseSupport {

  override lazy private[geryon] val defaultThreadExecutor = ExecutionContext.global

  protected[geryon] def init(): Unit = {
    HttpServerInfoHolder.httpServer = new HttpServer(HttpServerInfoHolder.port, HttpServerInfoHolder.eventLoopThreadNumber)
    HttpServerInfoHolder.httpServer.start()
  }

  def supply[T](supplier: => T)(implicit ec: ExecutionContext = ExecutionContext.global): Future[T] = Future {
    supplier
  }

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
}

object HttpServerInfoHolder {
  var defaultContentType = "text/plain"
  var port = 8080
  var eventLoopThreadNumber = 1
  var defaultHeaders: mutable.Map[String, String] = mutable.Map[String, String]()
  var httpServer: HttpServer = _
}
