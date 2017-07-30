package org.geryon.scaladsl

import org.geryon.Response

import scala.collection.JavaConverters._

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
class ScalaDslResponseBuilder {
  var body: Option[String] = None
  var status: Int = 200
  var headers: Map[String, String] = _
  var contentType: String = _

  def body(body: String): ScalaDslResponseBuilder = {
    this.body = Some(body)
    this
  }

  def httpStatus(status: Int): ScalaDslResponseBuilder = {
    this.status = status
    this
  }

  def headers(headers: (String, String)*): ScalaDslResponseBuilder = {
    this.headers = this.headers ++ headers
    this
  }

  def contentType(contentType: String): ScalaDslResponseBuilder = {
    this.contentType = contentType
    this
  }

  def build = ScalaDslResponse(body, status, headers, contentType)
}

case class ScalaDslResponse(body: Option[String],
                            status: Int,
                            headers: Map[String, String],
                            contentType: String) {
  def asJavaResponse: Response = new Response(body.orNull, status, headers.asJava, contentType)
}

