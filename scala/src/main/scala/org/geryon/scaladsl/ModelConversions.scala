package org.geryon.scaladsl

import org.geryon.{Request, Response}

import scala.concurrent.Future
import scala.language.implicitConversions

/**
  * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
  */
private [geryon] trait ModelConversions {
  implicit def asJavaRequest(request: ScalaDslRequest): Request = request.original
  implicit def asScalaDslRequest(request: Request): ScalaDslRequest = request.asScala
  implicit def asJavaResponse(response: ScalaDslResponse): Response = response.asJava

  implicit class RequestImplicitClass(val request: Request) {
    val asScala: ScalaDslRequest = ScalaDslRequest(request)
  }

  implicit class ResponseImplicitClass(val response: ScalaDslResponse) {
    val asJava: Response = response.asJavaResponse
  }
}