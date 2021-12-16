import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest._
import matchers.should._
import org.json4s.*
import org.json4s.native.JsonMethods.*
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import Utils.*


import scala.io.Source

/** Test object for XJson using the scalatest library */
class XJsonTest extends AnyFunSuite with Matchers {
  val newline = sys.props("line.separator")
  val testDir = "C/Other/Tests/"

  /**
   * Execute the test of a given number that corresponds to input and expected output files for XJson
   * @param testNumber the number to use when searching for test input / output data
   */
  def execute_test(testNumber: Int): Unit =
    val json_input = Source.fromFile(testDir + testNumber + "-in.json").mkString
    val json_output = Source.fromFile(testDir + testNumber + "-out.json").mkString
    val output = testCLI(Array(""), json_input, XJson.main).toString

    println(output)
    assert(parse(output) === parse(json_output))
    assert(parse(output) != JNothing)
    assert(parse(json_output) != JNothing)

  // run each test case by calling execute_test
  test("Standard use case") {execute_test(1)}
  test("Unreversable items use case") {execute_test(2)}
  test("Multiple JSON / JSON Array use case") {execute_test(3)}
}
