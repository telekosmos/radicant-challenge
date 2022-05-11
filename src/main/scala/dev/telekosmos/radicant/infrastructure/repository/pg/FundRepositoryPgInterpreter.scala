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
    private val sector2Fields = Map(
      "materials" -> "fund_sector_basic_materials",
      "communication" -> "fund_sector_communication_services",
      "cyclical" -> "fund_sector_consumer_cyclical",
      "defensive" -> "fund_sector_consumer_defensive",
      "energy" -> "fund_sector_energy",
      "financial" -> "fund_sector_financial_services",
      "healthcare" -> "fund_sector_healthcare",
      "industrials" -> "fund_sector_industrials",
      "real_estate" -> "fund_sector_real_estate",
      "technology" -> "fund_sector_technology",
      "utilities" -> "fund_sector_utilities"
    )

    def getBySizeAndSector(size: Long, sectorColumn: String) = {
      val sectorField = sectorColumn
      val restOfColumns = config.sectorColumns
        .filter(_ != sectorColumn)
        .map(f => fr0"coalesce(" ++ Fragment.const(f) ++ fr0", 0)")
        .intercalate(fr",")
      /*
      val restOfSectors = sector2Fields
        .filter(elem => elem._1 != sector)
        .values
        .map(f => fr0"coalesce(" ++ Fragment.const(f) ++ fr0", 0)")
        .toList
        .intercalate(fr",")
      */
      val coalesNoMaterials = "coalesce(fund_sector_consumer_defensive, 0),coalesce(fund_sector_real_estate, 0),coalesce(fund_sector_energy, 0),coalesce(fund_sector_consumer_cyclical, 0),coalesce(fund_sector_technology, 0),coalesce(fund_sector_utilities, 0),coalesce(fund_sector_financial_services, 0),coalesce(fund_sector_healthcare, 0),coalesce(fund_sector_communication_services, 0),coalesce(fund_sector_industrials, 0)"
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