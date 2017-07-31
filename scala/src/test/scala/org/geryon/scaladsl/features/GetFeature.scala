package org.geryon.scaladsl.features

import com.mashape.unirest.http.Unirest

import scala.concurrent.Future

/**
  * @author Gabriel Francisco <gabfssilva@gmail.com>
  */
class GetFeature extends BaseGeryonFeature {
  feature("GET feature") {
    scenario("Simple get") {
      get("/hello") { request =>
        Future {
          "hello, world!"
        }
      }

      val eventualResponse = Unirest.get(s"http://localhost:$port/hello").asFuture

      eventualResponse map { response =>
        response.getStatus shouldBe 200
        response.getBody shouldBe "hello, world!"
      }
    }

    scenario("Get with path parameter") {
      get("/hello/:name") { request =>
        Future {
          s"hello, ${request.pathParameters("name")}"
        }
      }

      val eventualResponse = Unirest.get(s"http://localhost:$port/hello/gabriel").asFuture

      eventualResponse map { response =>
        response.getStatus shouldBe 200
        response.getBody shouldBe "hello, gabriel"
      }
    }

    scenario("Get with query parameter") {
      get("/hello") { request =>
        Future {
          s"hello, ${request.queryParameters("name")}"
        }
      }

      val eventualResponse = Unirest.get(s"http://localhost:$port/hello?name=gabriel").asFuture

      eventualResponse map { response =>
        response.getStatus shouldBe 200
        response.getBody shouldBe "hello, gabriel"
      }
    }

    scenario("Get with header") {
      get("/hello") { request =>
        Future {
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

    scenario("Get with matcher") {
      get("/hello", _.headers("X-Version").equals("1")) { request =>
        Future {
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
  }
}
