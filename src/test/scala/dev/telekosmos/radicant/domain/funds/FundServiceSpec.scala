package dev.telekosmos.radicant.domain.funds

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import dev.telekosmos.radicant.infrastructure.repository.memory.FundRepositoryTestInterpreter
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FundServiceSpec extends AnyWordSpec with Matchers {
  val testRepo = FundRepositoryTestInterpreter[IO]()
  val fundService = FundService[IO](testRepo)

  "Fund service" should {
    "return an empty list" in {
      val call: IO[List[Fund]] = fundService.findBySizeAndSector(0, "metals")
      val result = call.unsafeRunSync()

      result.isEmpty shouldBe true
    }

    "return only one result" in {
      val call: IO[List[Fund]] = fundService.findBySizeAndSector(123456, "healthcare")
      val result = call.unsafeRunSync()

      result.size shouldBe(1)
      val fund = result(0)
      fund.fundSymbol should equal("AAUD")
    }

    "return multiple results" in {
      val call: IO[List[Fund]] = fundService.findBySizeAndSector(0, "financial")
      val result = call.unsafeRunSync()

      result.size shouldBe(2)
      val fundSymbols = result.map(_.fundSymbol)
      fundSymbols should equal(List("HEDJ", "INV"))
    }
  }
}
