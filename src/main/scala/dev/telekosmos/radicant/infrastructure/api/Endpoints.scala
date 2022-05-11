package dev.telekosmos.radicant.infrastructure.api

import cats.data.ValidatedNel
import cats.effect.Async
import cats.implicits._
import dev.telekosmos.radicant.domain.funds.{Fund, FundService}
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.literal.JsonStringContext
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.{ErrorHandling, Logger}
import org.http4s.{HttpApp, HttpRoutes, ParseFailure}

class Endpoints[F[_]: Async](fundService: FundService[F]) extends Http4sDsl[F] {
  object SizeQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Long]("size")
  object OpQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[String]("op")
  object SectorQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("sector")

  private def validate(sizeParam: ValidatedNel[ParseFailure, Long], sectorParam: String) = {
    sizeParam.fold(
      _ => BadRequest(json"""{ "error": "Wrong parameter value: size" }"""),
      size => {
        val sectors = Set("materials", "communication", "cyclical", "defensive", "energy", "financial", "healthcare", "industrials", "real_estate", "technology", "utilities")
        val validSector = sectors.contains(sectorParam)
        if (!validSector) BadRequest(json"""{ "error": "Wrong value for parameter sector" }""") else {
          for {
            retrieved <- fundService.findBySizeAndSector(size, sectorParam)
            asJson = retrieved.map(_.asJson)
            resp <- Ok(asJson)
          } yield resp
        }
      }
    )
  }

  val statusRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "status" => Ok(json"""{"status": "Normal"}""")
  }

  val fundsSectorRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "funds" :? SizeQueryParamMatcher(sizeValidated) +& SectorQueryParamMatcher(sector) => {
      sizeValidated match {
        case None => BadRequest(json"""{ "error": "Parameter required: size" }""")
        case Some(size) => sector match {
          case None => BadRequest(json"""{ "error": "Parameter required: sector" }""")
          case Some(validSector) => validate(size, validSector)
        }
      }
    }
  }

  val routes = statusRoute <+> fundsSectorRoute

  private val httpAppWithLog: HttpApp[F] = Logger.httpApp(true, true)(routes.orNotFound)
  val errorHandlingApp: HttpApp[F] = ErrorHandling(httpAppWithLog)
}

object Endpoints {
  def routes[F[_]: Async](fundService: FundService[F]): HttpRoutes[F] =
    new Endpoints(fundService).routes

  def getHttpApp[F[_]: Async](fundService: FundService[F]): HttpApp[F] =
    new Endpoints(fundService).errorHandlingApp
}

