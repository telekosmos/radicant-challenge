package dev.telekosmos.radicant.domain.funds

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.unsafe.implicits.global
import dev.telekosmos.radicant.infrastructure.repository.memory.FundRepositoryTestInterpreter
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class FundServiceSpec extends AsyncFreeSpec with Matchers with AsyncIOSpec {
  val testRepo = FundRepositoryTestInterpreter[IO]()
  val fundService = FundService[IO](testRepo)

  "Fund service" - {
    "return an empty list of funds" in {
      val call: IO[List[Fund]] = fundService.findBySizeAndSector(0, "metals")
      call.asserting(_.isEmpty shouldBe true)
    }

    "return only one fund result" in {
      val call: IO[List[Fund]] = fundService.findBySizeAndSector(123456, "healthcare")
      call.asserting(resp => {
        resp.size shouldBe(1)
        val fund = resp(0)
        fund.fundSymbol should equal("AAUD")
      })
      /*
      val result = call.unsafeRunSync()

      result.size shouldBe(1)
      val fund = result(0)
      fund.fundSymbol should equal("AAUD")
       */
    }

    "return multiple fund results" in {
      val call: IO[List[Fund]] = fundService.findBySizeAndSector(0, "fund_sector_financial_services")
      // val result = call.unsafeRunSync()

      call.asserting(result => {
        result.size shouldBe(2)
        val fundSymbols = result.map(_.fundSymbol)
        fundSymbols should equal(List("HEDJ", "INV"))
      })
    }
  }
}
