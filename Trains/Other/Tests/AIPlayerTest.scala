import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import matchers.should.Matchers.*
import org.json4s.JsonAST.JString
import org.scalatest.matchers.should.Matchers

import java.awt.Color
import scala.concurrent.{Await, ExecutionException}
import scala.language.postfixOps
import scala.concurrent.duration.DurationInt



class AIPlayerTest extends AnyFunSuite with Matchers {
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
  val playerStateJoe = PlayerState(publicState, PrivateState(cardCountsJoe, rails, destinationsJoe))
  val playerStateBob = PlayerState(publicState, PrivateState(cardCountsBob, rails, destinationsBob))
  val playerStateSam = PlayerState(publicState, PrivateState(cardCountsSam, railsSam, Set()))

  val playerJoe = AIPlayer("Joe", "StratTest", "StratTest", 1)
  val playerHue = AIPlayer("Hue", "StratTest", "StratTest", 2)
  val playerSam = AIPlayer("Sam", "StratTest", "StratTest", 3)

  // Tests for class Player
  // test pick method
  test("test pick assertions") {
    var caught = intercept[ExecutionException] {
      val pickFuture = playerHue.pick(board.getAllDestinations.take(5).toSet)
      Await.ready(pickFuture, 1 seconds).value.get.get
    }
    assert(caught.getCause.getMessage == "assertion failed: setup must be called before pick")
  }

  test("test pick chooses correct destinations"){
    playerSam.setup(board, 45, Map())
    val pick = Await.ready(playerSam.pick(board.getAllDestinations.take(5).toSet), 1 seconds).value.get.get
    assert(pick === Set(PlacePair(A, B), PlacePair(A, C), PlacePair(A, D)))
  }

  // test play method
  test("test play method") {
    val play = Await.ready(playerJoe.play(playerStateJoe), 1 seconds).value.get.get
    assert(play == Some(CDBlue))
  }


}
