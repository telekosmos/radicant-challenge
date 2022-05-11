package dev.telekosmos.radicant

import cats.effect.{Async, Resource}
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect.syntax._

case class HttpServer(host: String, port: Int)
case class StoreConfig(server: String, `type`: String, port: Int, dbname: String, user: String, password: String, driver: String, radicant: RadicantConfig)
case class RadicantConfig(sectorColumns: List[String])
case class EndpointsConfig(funds: FundsEndpointConfig)
case class FundsEndpointConfig(validSectors: Map[String, String])
case class ServiceConfig(server: HttpServer, endpoints: EndpointsConfig, storage: StoreConfig)

object Config {
  def loadResource[F[_]: Async](): Resource[F, ServiceConfig] = Resource.liftK(ConfigSource.default.loadF[F, ServiceConfig]())
}
