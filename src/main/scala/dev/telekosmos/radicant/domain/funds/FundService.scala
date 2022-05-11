package dev.telekosmos.radicant.domain.funds

class FundService[F[_]](fundRepo: FundRepostoryAlgebra[F]) {

  def findBySizeAndSector(size: Long, sectorColumnName: String): F[List[Fund]] =
    fundRepo.findBySizeAndSector(size, sectorColumnName)
}

object FundService {
  def apply[F[_]](fundRepo: FundRepostoryAlgebra[F]): FundService[F] =
    new FundService[F](fundRepo)
}