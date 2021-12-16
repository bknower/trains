import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import matchers.should.Matchers.*
import org.scalatest.matchers.should.Matchers


class TestingTaskTests extends AnyFunSuite with Matchers {

  val testDirs = List(3, 5, 6,
    8,
    9).map(n => s"${n}/Tests/")
    .zip(List(
      XMap.main,
      XLegal.main,
      XStrategy.main,
      XRef.main,
      XManager.main))

  test("Run instructor tests") {
    (1 to 3).foreach(test =>
      Utils.execute_test("8/ForStudents/", test, XRef.main))
  }

  test("Run all testing task tests") {
    testDirs.foreach{case (dir, main) =>
      (1 to 3).foreach(test =>
        Utils.execute_test(dir, test, main))}

  }

  test("9") {
    (3 to 3).foreach(test =>
      Utils.execute_test("9/Tests/", test, XManager.main))
  }

  test("9 Instructor Tests") {
    (1 to 4).foreach(test =>
      Utils.execute_test("9/ForStudents/", test, XManager.main))
  }

}
