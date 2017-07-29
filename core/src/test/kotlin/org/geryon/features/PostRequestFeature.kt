package org.geryon.features

import com.mashape.unirest.http.Unirest
import io.kotlintest.Spec
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FeatureSpec
import org.geryon.Http.*

class PostHttpFeature : FeatureSpec({
    feature("http post request") {
        scenario("with path parameter") {
            val body = Unirest.post("http://localhost:8888/test/post").asString().body
            body shouldBe "hello, post"
        }

        scenario("with query parameter") {
            val body = Unirest.post("http://localhost:8888/test/withQueryParameter?queryParameterName=post").asString().body
            body shouldBe "hello, post"
        }

        scenario("with body") {
            val body = Unirest.post("http://localhost:8888/test/withBody").body("post").asString().body
            body shouldBe "hello, post"
        }

        scenario("with custom http status") {
            val response = Unirest.post("http://localhost:8888/test/withBody").body("post").asString()

            response.status shouldBe 202
            response.body shouldBe "hello, post"
        }

        scenario("with custom response") {
            val response = Unirest.post("http://localhost:8888/test/customResponse").body("post").asString()

            response.status shouldBe 201
            response.headers["Location"]!![0] shouldBe "/test/post"
            response.body shouldBe "hello, post"
        }

        scenario("success with matcher") {
            val response = Unirest.post("http://localhost:8888/test/withMatcher/versionTest").header("X-Version", "1").body("post").asString()

            response.status shouldBe 202
            response.body shouldBe "accepted, post, with version X-Version = 1 ;)"
        }

        scenario("invalid with matcher") {
            val response = Unirest.post("http://localhost:8888/test/withMatcher/versionTest").header("X-Version", "2").body("post").asString()

            response.status shouldBe 404 //since the matcher returned false
        }

        scenario("method not allowed") {
            val response = Unirest.post("http://localhost:8888/test/post/notAllowed").body("post").asString()

            response.status shouldBe 405
        }
    }
}) {
    override fun interceptSpec(context: Spec, spec: () -> Unit) {
        port(8888)
        defaultContentType("text/plain")
        eventLoopThreadNumber(1)

        post("/test/:name") {
            supply { "hello, ${it.pathParameters()["name"]}" }
        }

        post("/test/withQueryParameter") {
            supply { "hello, ${it.queryParameters()["queryParameterName"]}" }
        }

        post("/test/withBody") {
            supply { accepted("hello, ${it.body()}") }
        }

        post("/test/customResponse") {
            supply {
                response()
                        .httpStatus(201)
                        .header("Location", "/test/${it.body()}")
                        .body("hello, ${it.body()}")
                        .build()
            }
        }

        post("/test/withMatcher/versionTest", { it.headers()["X-Version"] == "1" }) {
            supply { accepted("accepted, ${it.body()}, with version X-Version = 1 ;)") }
        }

        put("/test/post/notAllowed") {
            supply { accepted("accepted, ${it.body()}") }
        }

        spec()
    }
}