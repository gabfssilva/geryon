package org.geryon.features

import com.mashape.unirest.http.Unirest
import io.kotlintest.Spec
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.FeatureSpec
import org.geryon.Http.*

class GetHttpFeature : FeatureSpec({
    feature("http get request") {
        scenario("with path parameter") {
            val body = Unirest.get("http://localhost:8888/test/gabriel").asString().body
            body shouldBe "hello, gabriel"
        }

        scenario("with query parameter") {
            val body = Unirest.get("http://localhost:8888/test/withQueryParameter?queryParameterName=gabriel").asString().body
            body shouldBe "hello, gabriel"
        }

        scenario("success with matcher") {
            val response = Unirest.get("http://localhost:8888/test/withMatcher/versionTest").header("X-Version", "1").asString()

            response.status shouldBe 202
            response.body shouldBe "accepted, with version X-Version = 1 ;)"
        }

        scenario("invalid with matcher") {
            val response = Unirest.get("http://localhost:8888/test/withMatcher/versionTest").header("X-Version", "2").asString()

            response.status shouldBe 404 //since the matcher returned false
        }
    }
}) {
    override fun interceptSpec(context: Spec, spec: () -> Unit) {
        port(8888)
        defaultContentType("text/plain")
        eventLoopThreadNumber(1)

        get("/test/:name") {
            supply { "hello, ${it.pathParameters()["name"]}" }
        }

        get("/test/withQueryParameter") {
            supply { "hello, ${it.queryParameters()["queryParameterName"]}" }
        }

        get("/test/withMatcher/versionTest", { it.headers()["X-Version"] == "1"}) {
            supply { accepted("accepted, with version X-Version = 1 ;)") }
        }

        spec()
    }
}