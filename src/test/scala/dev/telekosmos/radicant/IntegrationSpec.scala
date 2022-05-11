package dev.telekosmos

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.http4s.{DecodeFailure, HttpApp, InvalidMessageBodyFailure, Method, QueryParamEncoder, Request, Status, Uri}
import cats.effect._
import cats.implicits._
import cats.effect.unsafe.implicits.global
import dev.telekosmos.radicant.{Config, EndpointsConfig, FundsEndpointConfig, Main, RadicantConfig, StoreConfig}
import dev.telekosmos.radicant.domain.funds.{Fund, FundService}
import dev.telekosmos.radicant.infrastructure.api.Endpoints
import dev.telekosmos.radicant.infrastructure.repository.pg.{Database, FundRepositoryPgInterpreter}
import io.circe.Json
import io.circe.generic.auto._
import org.http4s.circe.jsonDecoder
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax

class IntegrationSpec extends AnyWordSpec with Matchers {

  "Server with database" should {
    "get the result straight from database" in {
      val resources = for {
        config <- Config.loadResource[IO]()
        xa <- Database.createTransactor[IO](config.storage)
      } yield (config, xa)

      val test = resources.use { resources =>
        val (config, xa) = resources
        val repo = FundRepositoryPgInterpreter[IO](config.storage.radicant, xa)
        val service = FundService(repo)
        val httpApp = Endpoints.routes(config.endpoints, service).orNotFound

        val fundSize = 1685480652
        val sector = "communication"
        val uri = uri"/funds"
        val uriWithQueryParams = uri.withQueryParam("size", fundSize).withQueryParam("sector", sector)

        val request: Request[IO] = Request(method = Method.GET, uri = uriWithQueryParams)

        val client: Client[IO] = Client.fromHttpApp(httpApp)
        client.expect[Json](request)
      }

      val result = test.unsafeRunSync()
      val listResults = result.as[List[Fund]]
      val expectedSymbols = List("FNGU", "HYS", "XLC")
      for {
        funds <- listResults
      } yield {
        funds.size == 3 shouldBe(true)
        val symbols = funds.map(p => p.fundSymbol)
        expectedSymbols.forall(symbols.contains(_)) shouldBe(true)
      }
    }

    "get empty resultset from database" in {
      val resources = for {
        config <- Config.loadResource[IO]()
        xa <- Database.createTransactor[IO](config.storage)
      } yield (config, xa)

      val test = resources.use { resources =>
        val (config, xa) = resources
        val repo = FundRepositoryPgInterpreter[IO](config.storage.radicant, xa)
        val service = FundService(repo)
        val httpApp = Endpoints.routes(config.endpoints, service).orNotFound

        val fundSize = "99999999999".toLong
        val sector = "communication"
        val uri = uri"/funds"
        val uriWithQueryParams = uri.withQueryParam("size", fundSize).withQueryParam("sector", sector)

        val request: Request[IO] = Request(method = Method.GET, uri = uriWithQueryParams)

        val client: Client[IO] = Client.fromHttpApp(httpApp)
        client.expect[Json](request)
      }

      val result = test.unsafeRunSync()
      val listResults = result.as[List[Fund]]
      for {
        funds <- listResults
      } yield {
        funds.size == 0 shouldBe(true)
      }
    }
  }
  // val fundService: FundService[IO] = FundService(FundRepositoryTestInterpreter[IO]())
}
