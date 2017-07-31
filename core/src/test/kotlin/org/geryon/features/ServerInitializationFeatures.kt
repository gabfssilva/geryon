package org.geryon.features

import com.mashape.unirest.http.Unirest
import io.kotlintest.matchers.shouldBe
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.specs.FeatureSpec
import org.geryon.Http.*
import org.geryon.RequestHandlers
import org.geryon.exceptions.AmbiguousRoutingException

class ServerInitializationFeatures : FeatureSpec({
    feature("server initialization") {
        scenario("ambiguous handlers") {
            RequestHandlers.requestHandlers().clear()

            port(8888)
            defaultContentType("text/plain")
            eventLoopThreadNumber(1)

            post("/test/:name") {
                supply { "hello, ${it.pathParameters()["name"]}" }
            }

            val exception = shouldThrow<AmbiguousRoutingException> {
                post("/test/:id") {
                    supply { "hello, ${it.pathParameters()["id"]}" }
                }
            }

            exception.message shouldBe "There is more than one handler mapped for path /test(/.+)"

            RequestHandlers.requestHandlers().clear()
        }

        scenario("ambiguous handlers with matchers") {
            RequestHandlers.requestHandlers().clear()

            port(8888)
            defaultContentType("text/plain")
            eventLoopThreadNumber(1)

            get("/test/:name", { it.headers()["X-Version"] == "1" }) {
                supply { "hello, ${it.pathParameters()["name"]}, version 1" }
            }

            get("/test/:name", { it.headers()["X-Version"] == "2" }) {
                supply { "hello, ${it.pathParameters()["name"]}, version 2" }
            }

            val versionOneResult = Unirest.get("http://localhost:8888/test/gabriel").header("X-Version", "1").asString().body;
            val versionTwoResult = Unirest.get("http://localhost:8888/test/gabriel").header("X-Version", "2").asString().body;

            versionOneResult shouldBe "hello, gabriel, version 1"
            versionTwoResult shouldBe "hello, gabriel, version 2"

            RequestHandlers.requestHandlers().clear()
        }
    }
})