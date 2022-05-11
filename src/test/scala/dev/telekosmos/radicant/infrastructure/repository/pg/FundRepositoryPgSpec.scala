package dev.telekosmos.radicant.infrastructure.repository.pg

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dev.telekosmos.radicant.{Config, RadicantConfig, StoreConfig}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FundRepositoryPgSpec extends AnyWordSpec with Matchers {
  val sectorsConfig: RadicantConfig = RadicantConfig(List("fund_sector_communication_services"))
  val storeConfig: StoreConfig = StoreConfig("localhost", "postgresql", 5432, "radicant", "postgres", "p0stgr3s", "org.postgresql.Driver", sectorsConfig)

  "Repo interpreter for postgres" should {
    "query database returning multiple results" in {
      val resources = for {
        xa <- Database.createTransactor[IO](storeConfig)
        config <- Config.loadResource[IO]()
      } yield (xa, config)



      val program = resources.use( resources => {
        val (xa, config) = resources
        val repo = new FundRepositoryPgInterpreter(config.storage.radicant, xa)
        val result = repo.findBySizeAndSector(1685480652, "fund_sector_communication_services")
        result
      })

      val resultSet = program.unsafeRunSync()
      resultSet.size should equal(3)
      val fundSizes: List[Long] = resultSet.map(_.size).map(_.getOrElse(0))
      fundSizes.forall(f => f > 1685480652) should be(true)
    }

    "query database returning no results" in {
      val resources = for {
        xa <- Database.createTransactor[IO](storeConfig)
        config <- Config.loadResource[IO]()
      } yield (xa, config)



      val program = resources.use( resources => {
        val (xa, config) = resources
        val repo = new FundRepositoryPgInterpreter(config.storage.radicant, xa)

        val result = repo.findBySizeAndSector("99999999998".toLong, "fund_sector_communication_services")
        result
      })

      val resultSet = program.unsafeRunSync()
      resultSet.size should equal(0)
    }
  }

}
