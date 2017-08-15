package org.geryon.scaladsl

/**
  * @author Gabriel Francisco <peo_gfsilva@uolinc.com>
  */
trait ResponseSupport {
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
}
