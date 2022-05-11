package dev.telekosmos.radicant.infrastructure.repository.pg

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FundRepositoryPgSpec extends AnyWordSpec with Matchers {

  "Repo pg" should {
    "query database" in {
      val transactor = Database.createTransactor[IO]()
      val program = transactor.use(xa => {
        val repo = new FundRepositoryPgInterpreter(xa)
        val result = repo.findBySizeAndSector(1685480652, "communication")
        result
      })

      val resultSet = program.unsafeRunSync()
      resultSet.size should equal(3)
    }
  }
}
