package dev.telekosmos.radicant.domain.funds

class FundService[F[_]](fundRepo: FundRepostoryAlgebra[F]) {

  def findBySizeAndSector(size: Long, sector: String): F[List[Fund]] =
    fundRepo.findBySizeAndSector(size, sector)
}

object FundService {
  def apply[F[_]](fundRepo: FundRepostoryAlgebra[F]): FundService[F] =
    new FundService[F](fundRepo)
}