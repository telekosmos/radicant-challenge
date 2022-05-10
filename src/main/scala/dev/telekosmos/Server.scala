package dev.telekosmos

import cats.effect.{Async, IO}
import cats.implicits.toSemigroupKOps
import io.circe.Json
import io.circe.literal.JsonStringContext
import org.http4s.{HttpApp, HttpRoutes, Response, Status}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.{ErrorHandling, Logger}
import org.http4s.circe._

class Endpoints[F[_]: Async] extends Http4sDsl[F] {
  object SizeQueryParamMatcher extends ValidatingQueryParamDecoderMatcher[Long]("size")
  object OpQueryParamMatcher extends ValidatingQueryParamDecoderMatcher[String]("op")

  val statusRoute = HttpRoutes.of[F] {
    case GET -> Root / "status" => Ok(json"""{"status": "Normal"}""") // Async[F].pure(Response(Status.Ok))
  }

  val fundsRoute = HttpRoutes.of[F] {
    case GET -> Root / "funds" :? SizeQueryParamMatcher(sizeValidated) +& OpQueryParamMatcher(operatorValidated) => {
      sizeValidated.fold(
        _ => BadRequest(json"""{ "error": "Wrong parameter value: size" }"""),
        size => {
          val badRequestOp = BadRequest(json"""{ "error": "Wrong parameter value: op" }""")
          operatorValidated.fold(
            _ => badRequestOp,
            operator => {
              val validOp = List("gt", "ge", "eq", "le", "lt").contains(operator)

              if (!validOp) badRequestOp else {
                val response: Json = json"""{ "size": $size, "op": $operator }"""
                Ok(response)
              }
            }
          )
        }
      )
    }
  }

  val routes = statusRoute <+> fundsRoute

  private val httpAppWithLog: HttpApp[F] = Logger.httpApp(true, true)(routes.orNotFound)
  val errorHandlingApp: HttpApp[F] = ErrorHandling(httpAppWithLog)
}

object Endpoints {
  def routes[F[_]: Async](): HttpRoutes[F] =
    new Endpoints().routes

  def getHttpApp[F[_]: Async](): HttpApp[F] =
    new Endpoints().errorHandlingApp
}

