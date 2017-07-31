package org.geryon.scaladsl.features

import java.util.UUID

import com.mashape.unirest.http.Unirest

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
class GetFeature extends BaseGeryonFeature {
  feature("GET feature") {
    scenario("basic") {
      get("/hello") { request =>
        supply {
          "hello, world!"
        }
      }

      val eventualResponse = Unirest.get(s"http://localhost:$port/hello").asFuture

      eventualResponse map { response =>
        response.getStatus shouldBe 200
        response.getBody shouldBe "hello, world!"
      }
    }

    scenario("with path parameter") {
      get("/hello/:name") { request =>
        supply {
          s"hello, ${request.pathParameters("name")}"
        }
      }

      val eventualResponse = Unirest.get(s"http://localhost:$port/hello/gabriel").asFuture

      eventualResponse map { response =>
        response.getStatus shouldBe 200
        response.getBody shouldBe "hello, gabriel"
      }
    }

    scenario("with query parameter") {
      get("/hello") { request =>
        supply {
          s"hello, ${request.queryParameters("name")}"
        }
      }

      val eventualResponse = Unirest.get(s"http://localhost:$port/hello?name=gabriel").asFuture

      eventualResponse map { response =>
        response.getStatus shouldBe 200
        response.getBody shouldBe "hello, gabriel"
      }
    }

    scenario("with header") {
      get("/hello") { request =>
        supply {
          s"hello, world, version ${request.headers("X-Version")}"
        }
      }

      val eventualResponse =
        Unirest
          .get(s"http://localhost:$port/hello?name=gabriel")
          .header("X-Version", "1")
          .asFuture

      eventualResponse map { response =>
        response.getStatus shouldBe 200
        response.getBody shouldBe "hello, world, version 1"
      }
    }

    scenario("with matcher") {
      get("/hello", _.headers("X-Version").equals("1")) { request =>
        supply {
          s"hello, world, version ${request.headers("X-Version")}"
        }
      }

      val eventualResponse =
        Unirest
          .get(s"http://localhost:$port/hello")
          .header("X-Version", "1")
          .asFuture

      eventualResponse map { response =>
        response.getStatus shouldBe 200
        response.getBody shouldBe "hello, world, version 1"
      }

      val eventual404Response =
        Unirest
          .get(s"http://localhost:$port/hello")
          .header("X-Version", "2")
          .asFuture

      eventual404Response map { response =>
        response.getStatus shouldBe 404
        response.getBody shouldBe "not found"
      }
    }

    scenario("with default response header") {
      val (headerKey, headerValue)  = "custom-header" -> UUID.randomUUID().toString

      defaultHeader(headerKey -> headerValue)

      get("/hello") { _ => supply { "hello, world!" } }

      val eventualResponse = Unirest.get(s"http://localhost:$port/hello").asFuture

      eventualResponse map { response =>
        response.getStatus shouldBe 200
        response.getHeaders.getFirst(headerKey) shouldBe headerValue
      }
    }
  }
}
