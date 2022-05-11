package dev.telekosmos.radicant.infrastructure.repository.pg

import cats.effect.{Async, Resource}
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import dev.telekosmos.radicant.StoreConfig

object Database {

  def createTransactor[F[_]: Async](config: StoreConfig): Resource[F, Transactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](32) // our connect EC
      driver = config.driver
      db = config.`type`
      server = config.server
      port = config.port
      user = config.user
      password = config.password
      dbName = config.dbname
      xa <- HikariTransactor.newHikariTransactor[F](
        driver,
        s"jdbc:$db://$server:$port/$dbName",
        user,
        password,
        ce
      )
    } yield xa
}
