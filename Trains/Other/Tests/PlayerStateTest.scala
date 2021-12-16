import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import matchers.should.Matchers.*
import org.scalatest.matchers.should.Matchers
import java.awt.Color
import scala.io.Source
import java.util.Date

/** Test Board using the scalatest library */
class PlayerStateTest extends AnyFunSuite with Matchers {
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

  val acquiredConnections = Map("Joe" -> Set(), "Bob" -> Set(), "Sam" -> Set(ABWhite))
  val currentTurn = "Joe"
  val playerNames = List("Joe", "Bob", "Sam")
  val turnMap = Map("Joe" -> "Bob", "Bob" -> "Sam", "Sam" -> "Joe")
  val publicState = PublicState(board, acquiredConnections, currentTurn, playerNames)

  val cardCountsJoe = Map(Color.BLUE -> 5, Color.GREEN -> 2, Color.RED -> 1, Color.WHITE -> 0)
  val cardCountsBob = Map(Color.BLUE -> 2, Color.GREEN -> 0, Color.RED -> 2, Color.WHITE -> 4)
  val cardCountsSam = Map(Color.BLUE -> 1, Color.GREEN -> 0, Color.RED -> 3, Color.WHITE -> 0)
  val rails = 45
  val railsSam = 41
  val destinationsJoe = Set(PlacePair(B, C), PlacePair(E, F))
  val destinationsBob = Set(PlacePair(B, D), PlacePair(A, D))
  val destinationsSam = Set(PlacePair(A, B), PlacePair(A, C))
  val playerStateJoe = PlayerState(publicState, PrivateState(cardCountsJoe, rails, destinationsJoe))
  val playerStateBob = PlayerState(publicState, PrivateState(cardCountsBob, rails, destinationsBob))
  val playerStateSam = PlayerState(publicState, PrivateState(cardCountsSam, railsSam, destinationsSam))

  // --- UNIT TESTS ---

  // case class PlayerState Tests
  // Tests for PlayerState assertions
  test("invalid number of rails not allowed") {
    var caught = intercept[AssertionError] {
      PlayerState(publicState, PrivateState(cardCountsJoe, 75, destinationsJoe))
    }
    assert(caught.getMessage == "assertion failed: invalid number of rails")
    caught = intercept[AssertionError] {
      PlayerState(publicState, PrivateState(cardCountsJoe, -2, destinationsJoe))
    }
    assert(caught.getMessage == "assertion failed: invalid number of rails")
  }

  test("invalid cardCounts not allowed") {
    val cardCountsTooManyBlue = Map(Color.BLUE -> 300, Color.GREEN -> 2, Color.RED -> 1, Color.WHITE -> 0)
    var caught = intercept[AssertionError] {
      PlayerState(publicState, PrivateState(cardCountsTooManyBlue, rails, destinationsJoe))
    }
    assert(caught.getMessage == "assertion failed: invalid number of colored cards")
    val cardCountsNegativeRed = Map(Color.BLUE -> 3, Color.GREEN -> 2, Color.RED -> -1, Color.WHITE -> 0)
    caught = intercept[AssertionError] {
      PlayerState(publicState, PrivateState(cardCountsNegativeRed, rails, destinationsJoe))
    }
    assert(caught.getMessage == "assertion failed: invalid number of colored cards")
    val cardCountsTooManyTotal = Map(Color.BLUE -> 70, Color.GREEN -> 130, Color.RED -> 50, Color.WHITE -> 40)
    caught = intercept[AssertionError] {
      PlayerState(publicState, PrivateState(cardCountsTooManyTotal, rails, destinationsJoe))
    }
    assert(caught.getMessage == "assertion failed: invalid total number of cards")
  }

  test("player state must have 0 to 2 destinations") {
    val destinationsEmpty = Set[PlacePair]()
    noException should be thrownBy PlayerState(publicState, PrivateState(cardCountsJoe, rails, destinationsEmpty))
    val destinationsTooMany = Set(PlacePair(B, C), PlacePair(E, F), PlacePair(A, B))
    var caught = intercept[AssertionError] {
      PlayerState(publicState, PrivateState(cardCountsJoe, rails, destinationsTooMany))
    }
    assert(caught.getMessage == "assertion failed: invalid number of destination cards")
  }

  // Tests for canAcquire
  test("canAcquire returns correct boolean value") {
    assert(!playerStateJoe.canAcquire(EFWhite)) // should be false, Joe has no white cards
    assert(playerStateJoe.canAcquire(CDBlue)) // should be true, Joe has 5 blue cards
    assert(!playerStateBob.canAcquire(ABWhite)) // should be false, the connection has already been acquired
    assert(playerStateSam.canAcquire(BDRed)) // should be true, Sam has 3 red cards
  }

  // Tests for getAcquirableConnections
  test("getAcquirableConnections returns the correct set of Connections") {
    // can acquire 1 connection
    assert(playerStateJoe.getAcquirableConnections == Set(CDBlue))
    assert(playerStateBob.getAcquirableConnections == Set(EFWhite))
    assert(playerStateSam.getAcquirableConnections == Set(BDRed))
    // can acquire no connections
    val cardCount2 = Map(Color.BLUE -> 1, Color.GREEN -> 1, Color.RED -> 0, Color.WHITE -> 0)
    val playerStateWith2Cards = PlayerState(publicState, PrivateState(cardCount2, rails, destinationsJoe))
    assert(playerStateWith2Cards.getAcquirableConnections == Set())
    // can acquire multiple connections
    val cardCount16 = Map(Color.BLUE -> 5, Color.GREEN -> 3, Color.RED -> 4, Color.WHITE -> 4)
    val playerStateWith16Cards = PlayerState(publicState, PrivateState(cardCount16, rails, destinationsJoe))
    assert(playerStateWith16Cards.getAcquirableConnections == Set(ABGreen, ADRed, BDRed, CDBlue, EFWhite))
  }

 
}