package org.geryon.scaladsl.model

import org.geryon.Request

import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
object ScalaDslRequest {
  def apply(request: Request): ScalaDslRequest =
    new ScalaDslRequest(
      original = request,
      url = request.url(),
      body = if(request.body() == null || request.body().isEmpty) None else Some(request.body()),
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


