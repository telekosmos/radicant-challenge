package dev.telekosmos.radicant.infrastructure.repository.memory

import cats.Applicative
import cats.implicits._
import dev.telekosmos.radicant.domain.funds.{Fund, FundRepostoryAlgebra}

class FundRepositoryTestInterpreter[F[_]: Applicative] extends FundRepostoryAlgebra[F] {

  override def findBySizeAndSector(size: Long, sector: String): F[List[Fund]] = {
    (size, sector) match {
      case (v, s) if v > 0 && s == "financial" => List(Fund("GNR", None, Some("Invesco Greater China Fund Cl Y"))).pure[F]
      case (v, _) if v > 0 => List(Fund("AAUD", Some(1685480652), Some("Great Fund"))).pure[F]
      case (_, s) if s == "financial" => List(Fund("HEDJ", Some(1685480652), None), Fund("INV", Some(1685480652), Some("Invention financial"))).pure[F]
      case (_, _) => List[Fund]().pure[F]
    }
  }
}

object FundRepositoryTestInterpreter {
  def apply[F[_]: Applicative](): FundRepositoryTestInterpreter[F] =
    new FundRepositoryTestInterpreter[F]()
}
