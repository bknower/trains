import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.funsuite
import org.scalatest.*
import matchers.should.Matchers.*
import java.awt.Color
import java.util.Date
import scala.io.Source

class UtilsTest extends AnyFunSuite with Matchers{
  val A = Place("A", Posn(10, 10))
  val B = Place("B", Posn(30, 10))
  val C = Place("C", Posn(10, 80))
  val D = Place("D", Posn(50, 50))
  val E = Place("E", Posn(10, 50))
  val F = Place("F", Posn(20, 50))
  val G = Place("G", Posn(60, 60))
  val ABGreen = Connection(PlacePair(A, B), Color.GREEN, 4)
  val ABWhite = Connection(PlacePair(A, B), Color.WHITE, 3)
  val ABRed = Connection(PlacePair(A, B), Color.RED, 3)
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
  val publicState = PublicState(board, acquiredConnections, currentTurn, playerNames)

  val cardCountsJoe = Map(Color.BLUE -> 5, Color.GREEN -> 2, Color.RED -> 1, Color.WHITE -> 0)
  val cardCountsBob = Map(Color.BLUE -> 2, Color.GREEN -> 0, Color.RED -> 2, Color.WHITE -> 4)
  val cardCountsSam = Map(Color.BLUE -> 1, Color.GREEN -> 0, Color.RED -> 3, Color.WHITE -> 0)
  val rails = 45
  val railsSam = 41
  val destinationsJoe = Set(PlacePair(B,C), PlacePair(E,F))
  val destinationsBob = Set(PlacePair(B,D), PlacePair(A,D))
  val destinationsSam = Set(PlacePair(A,B), PlacePair(A,C))
  val playerStateJoe = PrivateState(cardCountsJoe, rails, destinationsJoe)
  val playerStateBob = PrivateState(cardCountsBob, rails, destinationsBob)
  val playerStateSam = PrivateState(cardCountsSam, railsSam, destinationsSam)

  val playerStates = Map("Joe" -> playerStateJoe, "Bob" -> playerStateBob, "Sam" -> playerStateSam)
  val cardCountsRef = Utils.cardCountsToCardList(Map(Color.BLUE -> 50, Color.GREEN -> 40, Color.RED -> 45, Color.WHITE -> 41)) // 176 left
  val refereeState = RefereeState(publicState, playerStates, cardCountsRef)



  test("Sort destinations") {
    assert(Utils.sortDestinations(Set(PlacePair(F, E), PlacePair(C, B))) == List(PlacePair(B,C), PlacePair(E,F)))
    assert(Utils.sortDestinations(Set(PlacePair(A,C), PlacePair(A,B))) == List(PlacePair(A,B), PlacePair(A,C)))
  }


  test("Count all cards") {
    assert(Utils.countAllCards(cardCountsJoe) == 8)
    assert(Utils.countAllCards(cardCountsBob) == 8)
    assert(Utils.countAllCards(cardCountsSam) == 4)
  }

  test("Sort connections") {
    assert(Utils.sortConnections(Set(ABGreen, ABWhite, ADRed, BDRed, ABRed)) == List(ABRed, ABWhite, ABGreen, ADRed, BDRed))
  }

  test("Sort places") {
    assert(Utils.sortPlaces(Set(G, E, D, C, B, A, F)) == List(A, B, C, D, E, F, G))
  }

  test("Connection to acquired") {
    assert(Utils.connectionToAcquired(ABGreen) == "[\"A\", \"B\", \"green\", 4]")
    assert(Utils.connectionToAcquired(BDRed) == "[\"B\", \"D\", \"red\", 3]")
  }
  
  

}
