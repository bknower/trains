import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

class XHeadTest extends AnyFunSuite {
  val newline = sys.props("line.separator")

  // Use artificial input and output stream to simulate standard in and out
  def testCLI(args: Array[String], stdin: String): ByteArrayOutputStream = {
    val out = new ByteArrayOutputStream()
    val in = new ByteArrayInputStream(stdin.getBytes())
    Console.withOut(out) {
      Console.withIn(in) {
        XHead.main(args)
        return out
      }
    }
  }

  test("Standard use case") {
    val output = testCLI(Array("-3"), "1\n2\n3")
    assert(output.toString() === "1" + newline + "2" + newline + "3" + newline)
  }

  test("Standard use case higher number than lines") {
    val output = testCLI(Array("-10"), "1\n2\n3")
    assert(output.toString() === "1" + newline + "2" + newline + "3" + newline)
  }

  test("Standard use case more lines than numbers") {
    val output = testCLI(Array("-3"), "1\n2\n3\n4\n5")
    assert(output.toString() === "1" + newline + "2" + newline + "3" + newline)
  }

  test("Number without dash") {
    val output = testCLI(Array("3"), "1\n2\n3\n4\n5")
    assert(output.toString() === "error" + newline)
  }

  test("Multiple arguments") {
    val output = testCLI(Array("-3 5"), "1\n2\n3\n4\n5")
    assert(output.toString() === "error" + newline)
  }

  test("Multiple dashes") {
    val output = testCLI(Array("--3"), "1\n2\n3\n4\n5")
    assert(output.toString() === "error" + newline)
  }

  test("Plus sign instead of dash") {
    val output = testCLI(Array("+3"), "1\n2\n3\n4\n5")
    assert(output.toString() === "error" + newline)
  }

  test("Head of 0 lines prints nothing") {
    val output = testCLI(Array("-0"), "1\n2\n3\n4\n5")
    assert(output.toString() === "" )
  }

  test("No Arguments") {
    val output = testCLI(Array(""), "1\n2\n3\n4\n5")
    assert(output.toString() === "error" + newline)
  }



}