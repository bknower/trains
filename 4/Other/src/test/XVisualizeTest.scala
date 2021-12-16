import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import java.awt.Color
import java.awt.image.BufferedImage
import scala.io.Source
import org.json4s._
import org.json4s.jackson.JsonMethods._

/** Test object for XVisualize using the scalatest library */
class XVisualizeTest extends AnyFunSuite with Matchers {
  val newline = sys.props("line.separator")
  val testDir = "4/Other/src/test/"
  val json_input = Source.fromFile(testDir + "maptest.json").mkString

  val board = XMap.jsonToBoard(parse(json_input))
  val mapEditor = MapEditor(board)
  val image = BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB)

  mapEditor.mainPanel.paint(image.createGraphics())


  // --- UNIT TESTS ---

  test("Test city location is painted the correct color") {
    assert(Color(image.getRGB(100, 100)) == Color.RED)
    assert(Color(image.getRGB(300, 100)) == Color.RED)
  }

  test("Test connections are drawn in correct colors between Places A and B") {
    assert(Color(image.getRGB(160, 86)) == Color.BLUE)
    assert(Color(image.getRGB(160, 96)) == Color.GREEN)
    assert(Color(image.getRGB(160, 106)) == Color.RED)
    assert(Color(image.getRGB(160, 116)) == Color.WHITE)
  }

  test("Space between segments is black") {
    assert(Color(image.getRGB(180, 102)) == Color.BLACK)
  }

  test("Backround drawn in black") {
    assert(Color(image.getRGB(380, 190)) == Color.BLACK)
  }

  test("Name of place written in white"){
    assert(Color(image.getRGB(92, 512)) == Color.WHITE)

  }

}