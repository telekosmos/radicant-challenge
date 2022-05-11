package dev.telekosmos.radicant

import cats.effect._
import dev.telekosmos.radicant.domain.funds.FundService
import dev.telekosmos.radicant.infrastructure.api.Endpoints
import dev.telekosmos.radicant.infrastructure.repository.pg.{Database, FundRepositoryPgInterpreter}
import org.http4s.server.{Server => H4Server}
import org.http4s.blaze.server.BlazeServerBuilder

object Main extends IOApp {
  def createResources[F[_] : Async]: Resource[F, H4Server] = {
    val resources = for {
      xa <- Database.createTransactor[F]()
      repo = FundRepositoryPgInterpreter[F](xa)
      service = FundService(repo)
      httpApp = Endpoints.getHttpApp(service)
      server <- BlazeServerBuilder[F]
        .bindHttp(11111, "localhost")
        .withHttpApp(httpApp)
        .resource
    } yield server

    resources
  }

  override def run(args: List[String]): IO[ExitCode] = createResources[IO].use { resources => IO.never }.as(ExitCode.Success)
}
