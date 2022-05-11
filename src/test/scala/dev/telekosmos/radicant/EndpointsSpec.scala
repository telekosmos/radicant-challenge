package dev.telekosmos.radicant

import cats.effect._
import cats.effect.unsafe.implicits.global
import dev.telekosmos.radicant.domain.funds.FundService
import dev.telekosmos.radicant.infrastructure.api.Endpoints
import dev.telekosmos.radicant.infrastructure.repository.memory.FundRepositoryTestInterpreter
import io.circe.Json
import io.circe.syntax.KeyOps
import org.http4s.circe.jsonDecoder
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{HttpApp, Method, Request, Status}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EndpointsSpec extends AnyWordSpec with Matchers {

  val fundService: FundService[IO] = FundService(FundRepositoryTestInterpreter[IO]())

  "Http server" should {
    "return status" in {
      val httpApp: HttpApp[IO] = Endpoints.routes[IO](fundService).orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uri"/status")
      val expectedPayload = Json.obj("status" := "Normal")

      val client: Client[IO] = Client.fromHttpApp(httpApp)
      val response = client.expect[Json](request)
      response.unsafeRunSync() should equal(expectedPayload)
    }

    "return single result" in {
      val fundSize = 30
      val sector = "financial"
      val uri = uri"/funds"
      val uriWithQueryParams = uri.withQueryParam("size", fundSize).withQueryParam("sector", sector)
      val httpApp: HttpApp[IO] = Endpoints.routes[IO](fundService).orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uriWithQueryParams)
      val expectedJson = Json.arr(Json.obj("fundSymbol" := "GNR", "size" := Json.Null, "fundShortName":= "Invesco Greater China Fund Cl Y"))

      val client: Client[IO] = Client.fromHttpApp(httpApp)
      val response = client.expect[Json](request)
      val result = response.unsafeRunSync()
      result should equal(expectedJson)
    }

    "request wrong param type" in {
      val fundSize = "10.5"
      val sector = "financial"
      val uri = uri"/funds"
      val uriWithQueryParams = uri.withQueryParam("size", fundSize).withQueryParam("sector", sector)
      val httpApp: HttpApp[IO] = Endpoints.routes[IO](fundService).orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uriWithQueryParams)

      val resp = httpApp.run(request).unsafeRunSync()
      resp.status == Status.BadRequest should be(true)
      val body = resp.body.compile.toList.unsafeRunSync().map(_.toChar).mkString("")
      body should (include("Wrong") and include("size"))
    }

    "request with no params" in {
      val httpApp: HttpApp[IO] = Endpoints.routes[IO](fundService).orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uri"/funds")

      val resp = httpApp.run(request).unsafeRunSync()
      resp.status == Status.BadRequest should be(true)
      val body = resp.body.compile.toList.unsafeRunSync().map(_.toChar).mkString("")
      body should (include("required") and include("size"))
    }
  }
}
