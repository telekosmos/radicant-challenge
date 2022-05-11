package dev.telekosmos.radicant.infrastructure.repository.pg

import cats.effect.{Async, Resource}
import doobie.Transactor
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object Database {

  def createTransactor[F[_]: Async](): Resource[F, Transactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](32) // our connect EC
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        s"jdbc:postgresql://localhost:5432/radicant",   // connect URL
        "postgres",                                   // username
        "p0stgr3s",                                     // password
        ce                                      // await connection here
      )
    } yield xa
}
