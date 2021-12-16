import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import matchers.should.*
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import Utils.*
import scala.io.Source
import java.awt.Color
import matchers.should.Matchers.*
import java.awt.image.BufferedImage


/** Test object for XMap using the scalatest library */
class MapEditorTest extends AnyFunSuite with Matchers {
  val newline = sys.props("line.separator")
  val testDir = "3/Tests/"

  val A = Place("Apple", Posn(100, 100))
  val B = Place("Boston", Posn(300, 100))
  val C = Place("Campfire", Posn(100, 800))
  val D = Place("Detroit", Posn(500, 500))
  val E = Place("Ello", Posn(100, 500))
  val F = Place("Firefighter", Posn(200, 500))
  val G = Place("Goonies", Posn(600, 600))

  val ABGreen = Connection(PlacePair(A, B), Color.GREEN, 3)
  val ABWhite = Connection(PlacePair(A, B), Color.WHITE, 4)
  val ABRed = Connection(PlacePair(A, B), Color.RED, 5)
  val ABBlue = Connection(PlacePair(A, B), Color.BLUE, 4)
  val ADRed = Connection(PlacePair(A, D), Color.RED, 4)
  val BDRed = Connection(PlacePair(B, D), Color.RED, 3)
  val CDBlue = Connection(PlacePair(C, D), Color.BLUE, 5)
  val EFWhite = Connection(PlacePair(E, F), Color.WHITE, 4)

  val Places = Set(A, B, C, D, E, F, G)
  val Connections = Set(ABGreen, ABWhite, ADRed, BDRed, CDBlue, EFWhite, ABRed, ABBlue)

  val board = new Board(Places, Connections, Posn(800, 800))
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
    assert(Color(image.getRGB(180, 106)) == Color.BLACK)
  }

  test("Backround drawn in black") {
    assert(Color(image.getRGB(380, 190)) == Color.BLACK)
  }

  test("Name of place written on the screen in white"){
    assert(Color(image.getRGB(84, 118)) == Color.WHITE)
    assert(Color(image.getRGB(68, 785)) == Color.WHITE)
  }

}
