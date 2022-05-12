package dev.telekosmos.radicant

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
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
import org.scalatest.freespec.AsyncFreeSpec


class EndpointsSpec extends AsyncFreeSpec with Matchers with AsyncIOSpec {

  val config: EndpointsConfig = EndpointsConfig(FundsEndpointConfig(Map("financial" -> "fund_sector_financial_services")))
  val fundService: FundService[IO] = FundService(FundRepositoryTestInterpreter[IO]())

  "Endpoints should" - {
    "return status" in {
      val httpApp: HttpApp[IO] = Endpoints.routes[IO](config, fundService).orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uri"/status")
      val expectedPayload = Json.obj("status" := "Normal")

      val client: Client[IO] = Client.fromHttpApp(httpApp)
      val response = client.expect[Json](request)
      response.asserting(_ should equal(expectedPayload))
    }

    "return single result" in {
      val fundSize = 30
      val sector = "financial"
      val uri = uri"/funds"
      val uriWithQueryParams = uri.withQueryParam("size", fundSize).withQueryParam("sector", sector)
      val httpApp: HttpApp[IO] = Endpoints.routes[IO](config, fundService).orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uriWithQueryParams)
      val expectedJson = Json.arr(Json.obj("fundSymbol" := "GNR", "size" := Json.Null, "fundShortName" := "Invesco Greater China Fund Cl Y"))

      val client: Client[IO] = Client.fromHttpApp(httpApp)
      val response = client.expect[Json](request)
      response.asserting(_ should equal(expectedJson))
    }

    "request wrong param type" in {
      val fundSize = "10.5"
      val sector = "financial"
      val uri = uri"/funds"
      val uriWithQueryParams = uri.withQueryParam("size", fundSize).withQueryParam("sector", sector)
      val httpApp: HttpApp[IO] = Endpoints.routes[IO](config, fundService).orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uriWithQueryParams)

      val test = for {
        resp <- httpApp.run(request)
        bodyList <- resp.body.compile.toList
        body = bodyList.map(_.toChar).mkString("")
      } yield (resp.status, body)

      test.asserting(result => {
        result._1 == Status.BadRequest should be(true)
        result._2 should (include("Wrong") and include("size"))
      })
    }

    "request wrong param value" in {
      val fundSize = "10"
      val sector = "sports"
      val uri = uri"/funds"
      val uriWithQueryParams = uri.withQueryParam("size", fundSize).withQueryParam("sector", sector)
      val httpApp: HttpApp[IO] = Endpoints.routes[IO](config, fundService).orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uriWithQueryParams)

      val test = for {
        resp <- httpApp.run(request)
        bodyList <- resp.body.compile.toList
        body = bodyList.map(_.toChar).mkString("")
      } yield (resp.status, body)

      test.asserting(result => {
        result._1 == Status.BadRequest should be(true)
        result._2 should (include("Wrong") and include("sector"))
      })
    }

    "request with no params" in {
      val httpApp: HttpApp[IO] = Endpoints.routes[IO](config, fundService).orNotFound
      val request: Request[IO] = Request(method = Method.GET, uri = uri"/funds")

      val test = for {
        resp <- httpApp.run(request)
        bodyList <- resp.body.compile.toList
        body = bodyList.map(_.toChar).mkString("")
      } yield (resp.status, body)

      test.asserting(result => {
        result._1 == Status.BadRequest should be(true)
        result._2 should (include("required") and include("size"))
      })
    }

  }
}
