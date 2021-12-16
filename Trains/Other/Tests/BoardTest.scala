import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import matchers.should.Matchers.*
import org.scalatest.matchers.should.Matchers

import java.awt.Color
import scala.io.Source

/** Test Board using the scalatest library */
class BoardTest extends AnyFunSuite with Matchers {
  val A = Place("A", Posn(10, 10))
  val B = Place("B", Posn(30, 10))
  val C = Place("C", Posn(10, 80))
  val D = Place("D", Posn(50, 50))
  val E = Place("E", Posn(10, 50))
  val F = Place("F", Posn(20, 50))
  val G = Place("G", Posn(60, 60))

  val ABGreen = Connection(PlacePair(A, B), Color.GREEN, 3)
  val ABWhite = Connection(PlacePair(A, B), Color.WHITE, 4)
  val ADRed = Connection(PlacePair(A, D), Color.RED, 4)
  val BDRed = Connection(PlacePair(B, D), Color.RED, 3)
  val CDBlue = Connection(PlacePair(C, D), Color.BLUE, 5)
  val EFWhite = Connection(PlacePair(E, F), Color.WHITE, 4)

  val Places = Set(A, B, C, D, E, F, G)
  val Connections = Set(ABGreen, ABWhite, ADRed, BDRed, CDBlue, EFWhite)

  val board = new Board(Places, Connections, Posn(100, 100))

  val allDestinations = Set(
    PlacePair(A, B),
    PlacePair(A, D),
    PlacePair(A, C),
    PlacePair(B, D),
    PlacePair(B, C),
    PlacePair(D, C),
    PlacePair(E, F))

  val placeNamesList = List("A", "B", "C", "D", "E", "F", "G")


  /**
   * Input validation for each type
   * Board
   * - Valid
   * - invalid coordinate of a place
   * - connection with place not in set of places
   * - color used multiple times between 2 places
   * Methods:
   * - getPlaceNames
   * - getAllDestinations
   * - getConnectedPlaces
   * - hasConnection
   *
   * Posn
   * - valid
   * - negative
   *
   * Connection
   * - valid
   * - invalid number of places
   * - invalid length
   * - invalid color
   */

  // Tests for Board Case Class

  test("Test getPlaceNames") {
    assert(board.getPlaceNames === placeNamesList)
  }
  test("Test getAllDestinations") {
    assert(board.getAllDestinations === allDestinations)
  }
  test("Test getConnectedPlaces") {
    assert(board.getConnectedPlaces(E) === Set(F))
    assert(board.getConnectedPlaces(A) === Set(B, D))
    assert(board.getConnectedPlaces(D) === Set(A, B, C))
    assert(board.getConnectedPlaces(G) === Set())
    assert(board.getConnectedPlaces(null) === Set())
  }
  test("Test hasConnection") {
    assert(board.hasConnection(A, B) === true) // connected directly
    assert(board.hasConnection(A, C) === false) // not connected directly, but path exists
    assert(board.hasConnection(F, G) === false) // not connected directly, no path exists
  }
  test("Test hasPath") {
    assert(board.hasPath(PlacePair(A, C))) // has path
    assert(board.hasPath(PlacePair(B, E)) === false) // doesn't have 
  }

  // Test Board assertions

  test("board dimensions validation") {
    var caught = intercept[AssertionError] {
      Board(Places, Connections, Posn(900, 100))
    }
    assert(caught.getMessage == "assertion failed: invalid dimensions")
    caught = intercept[AssertionError] {
      Board(Places, Connections, Posn(500, 801))
    }
    assert(caught.getMessage == "assertion failed: invalid dimensions")
    caught = intercept[AssertionError] {
      Board(Places, Connections, Posn(10, 8))
    }
    assert(caught.getMessage == "assertion failed: invalid dimensions")
  }
  test("Test a place that is out of the map bounds") {
    val badPlace = Place("A", Posn(69420, 30))
    val badPlaces = Set(badPlace)
    var caught = intercept[AssertionError] {
      Board(badPlaces, Set(), Posn(100, 100))
    }
    assert(caught.getMessage == "assertion failed: place out of bounds of map")
  }
  test("test a board with two places that have the same name") {
    val samePlaceNames = Set(Place("A", Posn(40, 30)), Place("A", Posn(50, 30)))
    var caught = intercept[AssertionError] {
      Board(samePlaceNames, Set(), Posn(100, 100))
    }
    assert(caught.getMessage == "assertion failed: city names must be unique")
  }
  test("board with 2 places in the same location") {
    val R = Place("R", Posn(10, 10))
    var caught = intercept[AssertionError] {
      Board(Places + R, Connections, Posn(100, 100))
    }
    assert(caught.getMessage ==
      "assertion failed: city locations must be unique")
  }
  test("Test connection with place not in set of places") {
    val R = Place("R", Posn(50, 70))
    val badConnection = Connection(PlacePair(R, B), Color.GREEN, 3)
    var caught = intercept[AssertionError] {
      Board(Places, Set(badConnection), Posn(100, 100))
    }
    assert(caught.getMessage ==
      "assertion failed: all connections must contain only places within the given set of places")
  }
  test("Test color used multiple times between same two places") {
    val ABGreen2 = Connection(PlacePair(A, B), Color.GREEN, 5)
    val badConnections = Set(ABGreen, ABGreen2)
    var caught = intercept[AssertionError] {
      Board(Places, badConnections, Posn(100, 100))
    }
    assert(caught.getMessage == "assertion failed: no connections between the same two places may share a color")
  }

  // Tests for Posn Case Class

  test("Test creating invalid coordinates") {
    var caught = intercept[AssertionError] {
      Posn(-1, 0)
    }
    assert(caught.getMessage == "assertion failed: coordinates must be non-negative")
    caught = intercept[AssertionError] {
      Posn(0, -1)
    }
    assert(caught.getMessage == "assertion failed: coordinates must be non-negative")
  }
  test("Valid coordinates") {
    noException should be thrownBy Posn(1, 0)
    noException should be thrownBy Posn(0, 0)
    noException should be thrownBy Posn(100, 0)
    noException should be thrownBy Posn(1, 909)
  }

  // Tests for Place Case Class

  test("Place names follow constraints") {
    noException should be thrownBy Place("New York.,238469", Posn(100,100))
    var caught = intercept[AssertionError] {
      Place("HelloWorld!",Posn(50,50))
    }
    assert(caught.getMessage == "assertion failed: invalid character(s) in name")
    caught = intercept[AssertionError] {
      Place("HelloWorldHelloWorldHelloWorldHelloWorldHelloWorld",Posn(50,50))
    }
    assert(caught.getMessage == "assertion failed: invalid length of name")
  }
  
  // Tests for Connection Case Class

  test("Invalid Connections") {
    var caught = intercept[AssertionError] {
      Connection(PlacePair(A, B), Color.ORANGE, 3)
    }
    assert(caught.getMessage == "assertion failed: invalid color")
    caught = intercept[AssertionError] {
      Connection(PlacePair(A, B), Color.RED, 2)
    }
    assert(caught.getMessage == "assertion failed: invalid length")
    caught = intercept[AssertionError] {
      Connection(PlacePair(A, B), Color.RED, 6)
    }
    assert(caught.getMessage == "assertion failed: invalid length")
  }

  // Tests for PlacePair Case Class

  test("Invalid PlacePairs") {
    var caught = intercept[AssertionError] {
      PlacePair(A, A)
    }
    assert(caught.getMessage == "assertion failed: invalid number of places")
    caught = intercept[AssertionError] {
      PlacePair(Set(A, B, C))
    }
    assert(caught.getMessage == "assertion failed: invalid number of places")
    caught = intercept[AssertionError] {
      PlacePair(Set())
    }
    assert(caught.getMessage == "assertion failed: invalid number of places")
  }
  
}