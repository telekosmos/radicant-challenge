package dev.telekosmos.radicant.infrastructure.repository.pg

import cats.effect.{Async, IO}
import cats.implicits._
import dev.telekosmos.radicant.RadicantConfig
import dev.telekosmos.radicant.domain.funds.{Fund, FundRepostoryAlgebra}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

class FundRepositoryPgInterpreter[F[_]: Async](config: RadicantConfig, xa: Transactor[F]) extends FundRepostoryAlgebra[F] {
  private object Statements {
    def getBySizeAndSector(size: Long, sectorColumn: String) = {
      val sectorField = sectorColumn
      val restOfColumns = config.sectorColumns
        .filter(_ != sectorColumn)
        .map(f => fr0"coalesce(" ++ Fragment.const(f) ++ fr0", 0)")
        .intercalate(fr",")
      val sectorFr = fr"and " ++ Fragment.const(sectorField) ++ fr" > greatest(" ++ restOfColumns ++ fr")"
      (sql"""
        SELECT fund_symbol, total_net_assets as size, fund_short_name
        FROM etfsbis
        WHERE coalesce(total_net_assets, 0)::bigint > $size
      """ ++ sectorFr).queryWithLogHandler[Fund](LogHandler.jdkLogHandler)
    }
  }

  override def findBySizeAndSector(size: Long, sector: String): F[List[Fund]] =
    Statements.getBySizeAndSector(size, sector).to[List].transact(xa)
}

object FundRepositoryPgInterpreter {
  def apply[F[_]: Async](config: RadicantConfig, transactor: Transactor[F]): FundRepositoryPgInterpreter[F] =
    new FundRepositoryPgInterpreter[F](config, transactor)
}