import Utils.*
import org.json4s.*
import org.json4s.jackson.JsonMethods.*
import org.scalatest.*
import org.scalatest.funsuite.AnyFunSuite

import java.awt.Color
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import scala.io.Source
import org.scalatest.matchers.should.Matchers


/** Test object for XMap using the scalatest library */
class XMapTest extends AnyFunSuite with Matchers  {
  val newline = sys.props("line.separator")
  val testDir = "3/Tests/"

  /**
   * Execute the test of a given number that corresponds to input and expected output files for XJson
   * @param testNumber the number to use when searching for test input / output data
   */
  def execute_test(testNumber: Int): Unit =
    val json_input = Source.fromFile(testDir + testNumber + "-in.json").mkString
    val json_output = Source.fromFile(testDir + testNumber + "-out.json").mkString
    val output = testCLI(Array(""), json_input, XMap.main).toString
    assert(json_output == output)

  // run each test case by calling execute_test
  test("Single connection is present") {execute_test(1)}
  test("Connection present, but not the one in question") {execute_test(2)}
  test("Complex map, places do not have a connection, but do have a path") {execute_test(3)}


  // --- UNIT TESTS ---

  // getPlaces tests
  val jplaces0 = List(
    JArray(List(JString("Boston"),JArray(List(JInt(50), JInt(100))))),
    JArray(List(JString("New York"),JArray(List(JInt(300), JInt(300))))))
  val places0 = Set(Place("Boston", Posn(50,100)), Place("New York", Posn(300,300)))
  test("getPlaces test") {
    assert(Utils.getPlaces(jplaces0) == places0)
  }

  // getConnections tests
  val boston = Place("Boston", Posn(100, 400))
  val newYork = Place("New York", Posn(300, 300))
  val places1 = Set(boston, newYork)
  val jConnections1 = List(
    JField("Boston", JObject((
        "New York", JObject((
        "blue", JInt(4))
      ))
      ))
    )
  val connections1 = Set(Connection(PlacePair(boston, newYork), Color.BLUE, 4))
  test("getConnections works") {
    assert(Utils.getConnections(jConnections1, places1) == connections1)
  }

  // getColor tests
  test("getColor works on assigned colors") {
    assert(Utils.getColor("RED") == Color.RED)
    assert(Utils.getColor("BLUE") == Color.BLUE)
    assert(Utils.getColor("GREEN") == Color.GREEN)
    assert(Utils.getColor("WHITE") == Color.WHITE)
  }
}
