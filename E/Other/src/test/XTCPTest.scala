
import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import matchers.should.*
import org.json4s.*
import org.json4s.native.JsonMethods.*

import java.io.{BufferedReader, ByteArrayInputStream, ByteArrayOutputStream, InputStreamReader, PrintWriter}
import Utils.*

import java.net.Socket
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import scala.io.Source




class XTCPTest extends AnyFunSuite with Matchers {

  /**
   * Different port inputs
   * - out of bounds
   * - not numbers
   * - valid input
   *
   * Multiple JSON values successfully sent through
   *
   * Bad JSON input
   *
   *
   */

  class ServerThread extends Thread
    def run: Unit = {
      Xtcp.main(Array("30001"))
    }

  val newline = sys.props("line.separator")
  val testDir = "C/Other/Tests/"

  /**
   * Execute the test of a given number that corresponds to input and expected output files for XJson
   * @param testNumber the number to use when searching for test input / output data
   */
  def execute_test(testNumber: Int): Unit =

    val json_input = Source.fromFile(testDir + testNumber + "-in.json").mkString
    val json_output = Source.fromFile(testDir + testNumber + "-out.json").mkString

    new ServerThread().start()

    TimeUnit.SECONDS.sleep(1);

    //val output = testCLI(Array("30000"), json_input, Xtcp.main).toString
    val socket = new Socket("127.0.0.1", 30001)
    val output = new BufferedReader(new InputStreamReader(socket.getInputStream))

    val printWriter = new PrintWriter(socket.getOutputStream, true)
    printWriter.println(json_input)

    val input = output.lines().collect(Collectors.joining("\n"))
//    var input = ""
//    var line = ""
//    while ( {
//      line = output.readLine();
//      line != null
//    }) {
//      input += line
//    }

    assert(parse(input) === parse(json_output))
    assert(parse(input) != JNothing)
    assert(parse(json_output) != JNothing)
    println("test successful")


  // run each test case by calling execute_test
  test("Standard use case") {execute_test(1)}
  test("Unreversable items use case") {execute_test(2)}
  test("Multiple JSON / JSON Array use case") {execute_test(3)}
}
