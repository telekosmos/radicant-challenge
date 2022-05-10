package dev.telekosmos

import cats.effect.{Async, ExitCode, IO, IOApp, Resource}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.{Server => H4Server}


object Main extends IOApp {
  private def createResources[F[_]: Async]: Resource[F, H4Server] = { // Resource[F, (ServiceConfig, CarPassingBusClient[F], Handler[F, IncomingMessage, OutgoingMessage])] = {
    val httpApp = Endpoints.getHttpApp()
    val resources = for {
      server <- BlazeServerBuilder[F]
        .bindHttp(11111, "localhost")
        .withHttpApp(httpApp)
        .resource
    } yield server

    resources
  }

  override def run(args: List[String]): IO[ExitCode] = createResources[IO].use { resources => IO.never }.as(ExitCode.Success)
}
