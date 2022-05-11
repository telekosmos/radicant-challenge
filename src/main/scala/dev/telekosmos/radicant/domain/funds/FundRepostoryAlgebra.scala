package dev.telekosmos.radicant.domain.funds

trait FundRepostoryAlgebra[F[_]] {
  def findBySizeAndSector(size: Long, sector: String): F[List[Fund]]
}
