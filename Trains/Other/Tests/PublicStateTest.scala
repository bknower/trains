import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import matchers.should.Matchers.*
import org.scalatest.matchers.should.Matchers
import java.awt.Color
import scala.io.Source

/** Test PublicState using the scalatest library */
class PublicStateTest extends AnyFunSuite with Matchers {
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
  val noAcquiredConnections = Map("Joe" -> Set[Connection](), "Bob" -> Set(), "Sam" -> Set())
  val publicStateNoConnections = PublicState(board, noAcquiredConnections, currentTurn, playerNames)
  val publicStateOneConnection = PublicState(board, acquiredConnections, currentTurn, playerNames)
  
  // --- UNIT TESTS ---
  
  // Tests for PublicState assertions
  
  test("Current player is a valid player"){
    var caught = intercept[AssertionError] {
      PublicState(board, acquiredConnections, "Arnold", playerNames)
    }
    assert(caught.getMessage == "assertion failed: currentTurn must be a valid player")
  }
  
  test("acquiredConnections are owned by valid players") {
    val acquiredConnectionsWithInvalidPlayer = Map("Joe" -> Set(ABWhite), "Benjamin Scott Lerner" -> Set(BDRed, CDBlue))
    var caught = intercept[AssertionError] {
      PublicState(board, acquiredConnectionsWithInvalidPlayer, currentTurn, playerNames)
    }
    assert(caught.getMessage == "assertion failed: the players in the turn order and the acquired connections map must be the same")
  }

  // Tests for isDestinationAcquired

  test("isDestinationAcquired returns correct boolean value") {
    assert(publicStateOneConnection.isDestinationAcquired(PlacePair(A,B), "Sam"))
    assert(!publicStateOneConnection.isDestinationAcquired(PlacePair(A,B), "Bob"))
    assert(!publicStateNoConnections.isDestinationAcquired(PlacePair(A,B), "Joe"))
  }

  test("whoHasLongestRoute base cases") {
    assert(publicStateOneConnection.whoHasLongestRoute == Set("Sam"))
    assert(publicStateNoConnections.whoHasLongestRoute == Set("Sam", "Bob", "Joe"))
    val acquiredConnections1 = Map("Joe" -> Set(ABGreen, CDBlue, BDRed), "Bob" -> Set(ADRed, EFWhite), "Sam" -> Set(ABWhite))
    assert(publicStateNoConnections.copy(acquiredConnections = acquiredConnections1).whoHasLongestRoute == Set("Joe"))
  }

  test("whoHasLongestRoute where two people have the same number of connections") {
    val acquiredConnections2 = Map("Joe" -> Set(ABGreen, BDRed), "Bob" -> Set(ADRed, CDBlue), "Sam" -> Set(ABWhite))
    assert(publicStateNoConnections.copy(acquiredConnections = acquiredConnections2).whoHasLongestRoute == Set("Bob"))
  }


  test("whoHasLongestRoute where longest route is not greatest number of connections") {
    val acquiredConnections2 = Map("Joe" -> Set(ABGreen, ABWhite), "Bob" -> Set(ADRed), "Sam" -> Set(ABWhite))
    assert(publicStateNoConnections.copy(acquiredConnections = acquiredConnections2).whoHasLongestRoute == Set("Bob", "Sam"))
  }

  test("whoHasLongestRoute where ") {

  }
  
}