package dev.telekosmos

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import dev.telekosmos.Endpoints
import org.http4s.{HttpApp, InvalidMessageBodyFailure, Method, QueryParamEncoder, Request, Status, Uri}
import cats.effect._
import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.syntax.KeyOps
import org.http4s.circe.jsonDecoder
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax

class ServerSpec extends AnyWordSpec with Matchers {

  "Http server" should {
    "return status" in {
      val httpApp: HttpApp[IO] = Endpoints.routes[IO]().orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uri"/status")
      val expectedPayload = Json.obj("status" := "Normal")

      val client: Client[IO] = Client.fromHttpApp(httpApp)
      val response = client.expect[Json](request)
      response.unsafeRunSync() should equal(expectedPayload)
    }

    "return bigger size" in {
      val fundSize = 30
      val op = "gt"
      val uri = uri"/funds"
      val uriWithQueryParams = uri.withQueryParam("size", fundSize).withQueryParam("op", op)
      val httpApp: HttpApp[IO] = Endpoints.routes[IO]().orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uriWithQueryParams)
      val expectedJson = Json.obj("size" := fundSize, "op" := op)

      val client: Client[IO] = Client.fromHttpApp(httpApp)
      val response = client.expect[Json](request)
      val result = response.unsafeRunSync()
      result should equal(expectedJson)
    }

    "request wrong param type" in {
      val fundSize = "10.5"
      val op = "gt"
      val uri = uri"/funds"
      val uriWithQueryParams = uri.withQueryParam("size", fundSize).withQueryParam("op", op)
      val httpApp: HttpApp[IO] = Endpoints.routes[IO]().orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uriWithQueryParams)
      val expectedJson = Json.obj("size" := fundSize, "op" := op)

      val resp = httpApp.run(request).unsafeRunSync()
      resp.status == Status.BadRequest should be(true)
      val body = resp.body.compile.toList.unsafeRunSync().map(_.toChar).mkString("")
      body should (include("Wrong") and include("size"))
    }

  }
}
