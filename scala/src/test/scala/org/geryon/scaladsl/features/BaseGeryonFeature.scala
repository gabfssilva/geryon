package org.geryon.scaladsl.features

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.async.Callback
import com.mashape.unirest.http.exceptions.UnirestException
import com.mashape.unirest.request.HttpRequest
import org.geryon.scaladsl._
import org.geryon.{ExceptionHandlers, RequestHandlers}
import org.scalatest.{AsyncFeatureSpec, BeforeAndAfter, FeatureSpec, Matchers}

import scala.concurrent.{Future, Promise}

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
class BaseGeryonFeature
  extends AsyncFeatureSpec
    with Matchers
    with BeforeAndAfter
    with ScalaHttp {

  val port = 16978

  before {
    port(port)
  }

  after {
    RequestHandlers.requestHandlers().clear()
    ExceptionHandlers.handlers().clear()
  }

  implicit class HttpRequestImplicit(val httpRequest: HttpRequest) {
    val asFuture: Future[HttpResponse[String]] = {
      val promise = Promise[HttpResponse[String]]()

      httpRequest.asStringAsync(new Callback[String] {
        override def failed(e: UnirestException): Unit = promise.failure(e.getCause)
        override def completed(response: HttpResponse[String]): Unit = promise.success(response)
        override def cancelled(): Unit = promise.failure(new RuntimeException("cancelled"))
      })

      promise.future
    }
  }

}
