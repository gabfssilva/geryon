package org.geryon.scaladsl

import org.geryon.Request

import scala.collection.JavaConverters._

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
trait RequestParameters {
  def body(implicit request: ScalaDslRequest): Option[String] = request.body
  def url(implicit request: ScalaDslRequest): String = request.url
  def contentType(implicit request: ScalaDslRequest): Option[String] = request.contentType
  def method(implicit request: ScalaDslRequest): String = request.method
  def headers(implicit request: ScalaDslRequest): Map[String, String] = request.headers
  def queryParameters(implicit request: ScalaDslRequest): Map[String, String] = request.queryParameters
  def pathParameters(implicit request: ScalaDslRequest): Map[String, String] = request.pathParameters

  def header(header: String)(implicit request: ScalaDslRequest): String = {
    headers.get(header).orNull
  }

  def param(param: String)(implicit request: ScalaDslRequest): String = {
    pathParameters
      .get(param)
      .orElse(queryParameters.get(param))
      .orNull
  }

  def pathParam(param: String)(implicit request: ScalaDslRequest): String = {
    pathParameters
      .get(param)
      .orNull
  }

  def queryParam(param: String)(implicit request: ScalaDslRequest): String = {
    queryParameters
      .get(param)
      .orNull
  }
}

object ScalaDslRequest {
  def apply(request: Request): ScalaDslRequest =
    new ScalaDslRequest(
      original = request,
      url = request.url(),
      body = if (request.body() == null || request.body().isEmpty) None else Some(request.body()),
      contentType = Option(request.contentType()),
      method = request.method(),
      headers = if (request.headers() == null) Map.empty else request.headers().asScala.toMap,
      queryParameters = if (request.queryParameters() == null) Map.empty else request.queryParameters().asScala.toMap,
      pathParameters = if (request.pathParameters() == null) Map.empty else request.pathParameters().asScala.toMap
    )
}

case class ScalaDslRequest(url: String,
                           body: Option[String],
                           contentType: Option[String],
                           method: String,
                           headers: Map[String, String],
                           queryParameters: Map[String, String],
                           pathParameters: Map[String, String],
                           original: Request)


