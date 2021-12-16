import java.awt.Color
import java.util.Date
import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import matchers.should.Matchers.*
import org.scalatest.matchers.should.Matchers

class StrategyTest extends AnyFunSuite with Matchers {

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

  var acquiredConnections = Map("Joe" -> Set(), "Bob" -> Set(), "Sam" -> Set(ABWhite))
  var currentTurn = "Joe"
  val playerNames = List("Joe", "Bob", "Sam")
  var publicState = PublicState(board, acquiredConnections, currentTurn, playerNames)

  var cardCountsJoe = Map(Color.BLUE -> 5, Color.GREEN -> 2, Color.RED -> 1, Color.WHITE -> 5)
  val cardCountsBob = Map(Color.BLUE -> 2, Color.GREEN -> 0, Color.RED -> 2, Color.WHITE -> 4)
  val cardCountsSam = Map(Color.BLUE -> 1, Color.GREEN -> 0, Color.RED -> 3, Color.WHITE -> 0)
  val rails = 45
  val railsSam = 41
  val destinationsJoe = Set(PlacePair(B, C), PlacePair(E, F))
  val destinationsBob = Set(PlacePair(B, D), PlacePair(A, D))
  val destinationsSam = Set(PlacePair(A, B), PlacePair(A, C))
  var playerStateJoe = PlayerState(publicState, PrivateState(cardCountsJoe, rails, destinationsJoe))
  val playerStateBob = PlayerState(publicState, PrivateState(cardCountsBob, rails, destinationsBob))
  val playerStateSam = PlayerState(publicState, PrivateState(cardCountsSam, railsSam, destinationsSam))

  // UNIT TESTS for Strategy

  // Tests for shouldAcquireConnection

  test("Hold10 should decide to acquire a connection " +
    "when the current player has more than 10 cards") {
    assert(Hold10.shouldAcquireConnection(playerStateJoe))
  }

  test("Hold10 should decide not to acquire a connection " +
    "when the current player has less than or equal to 10 cards") {
    cardCountsJoe = Map(Color.BLUE -> 5, Color.GREEN -> 2, Color.RED -> 1, Color.WHITE -> 0)
    playerStateJoe = PlayerState(publicState, PrivateState(cardCountsJoe, rails, destinationsJoe))

    assert(!Hold10.shouldAcquireConnection(playerStateJoe))
  }

  test("Hold10 should decide not to acquire a connection " +
    "when there are no acquirable connections") {
    val boardWithNoConnections = new Board(Places, Set(), Posn(100, 100))
    val acquiredConnectionsEmpty = Map("Joe" -> Set[Connection](), "Bob" -> Set(), "Sam" -> Set())
    val publicStateWithNoConns = PublicState(boardWithNoConnections, acquiredConnectionsEmpty, currentTurn, playerNames)
    var playerStateJoeNoConns = PlayerState(publicStateWithNoConns, PrivateState(cardCountsJoe, rails, destinationsJoe))
    assert(!Hold10.shouldAcquireConnection(playerStateJoeNoConns))
  }


  // Tests for chooseConnection

  test("Hold10 should decided to request cards if they have less than 10") {
    // Joe has less than 10 cards and shouldn't acquire according to Hold10
    cardCountsJoe = Map(Color.BLUE -> 5, Color.GREEN -> 2, Color.RED -> 1, Color.WHITE -> 0)
    playerStateJoe = PlayerState(publicState, PrivateState(cardCountsJoe, rails, destinationsJoe))

    assert(Hold10.takeTurn(playerStateJoe) == None)
  }

  test("chooseConnection chooses right connection for Hold10") {
    cardCountsJoe = Map(Color.BLUE -> 5, Color.GREEN -> 2, Color.RED -> 1, Color.WHITE -> 5)
    playerStateJoe = PlayerState(publicState, PrivateState(cardCountsJoe, rails, destinationsJoe))
    assert(Hold10.takeTurn(playerStateJoe) == Some(CDBlue))
  }

  test("chooseConnection chooses right connection for BuyNow") {
    cardCountsJoe = Map(Color.BLUE -> 5, Color.GREEN -> 2, Color.RED -> 1, Color.WHITE -> 5)
    playerStateJoe = PlayerState(publicState, PrivateState(cardCountsJoe, rails, destinationsJoe))
    assert(BuyNow.takeTurn(playerStateJoe) == Some(CDBlue))
  }

  // Tests for chooseDestination

  test("Hold10 chooses the right destinations") {
    acquiredConnections = Map("Joe" -> Set(), "Bob" -> Set(), "Sam" -> Set())
    val newPublicState = PublicState(board, acquiredConnections, currentTurn, playerNames)
    playerStateJoe = PlayerState(newPublicState, PrivateState(cardCountsJoe, rails, Set()))
    var destinations = Set(PlacePair(A, C), PlacePair(E, F), PlacePair(B, D), PlacePair(A, D), PlacePair(C, D))
    assert(Hold10.chooseDestinations(playerStateJoe.privateState, board, destinations) == Set(PlacePair(A, C), PlacePair(A, D)))
  }

  test("BuyNow chooses the right destinations") {
    acquiredConnections = Map("Joe" -> Set(), "Bob" -> Set(), "Sam" -> Set())
    val newPublicState = PublicState(board, acquiredConnections, currentTurn, playerNames)
    playerStateJoe = PlayerState(newPublicState, PrivateState(cardCountsJoe, rails, Set()))
    var destinations = Set(PlacePair(A, C), PlacePair(E, F), PlacePair(B, D), PlacePair(A, D), PlacePair(C, D))
    assert(BuyNow.chooseDestinations(playerStateJoe.privateState, board, destinations) == Set(PlacePair(E, F), PlacePair(C, D)))
  }

  test("Hold10 should throw error if the destinations are already chosen" +
    "when chooseDestinations is called") {
    var caught = intercept[AssertionError] {
      Hold10.chooseDestinations(playerStateSam.privateState, board, Set())
    }
    assert(caught.getMessage == "assertion failed: destinations already chosen")

  }

  test("BuyNow should throw error if the destinations are already chosen" +
    "when chooseDestinations is called") {
    var caught = intercept[AssertionError] {
      BuyNow.chooseDestinations(playerStateSam.privateState, board, Set())
    }
    assert(caught.getMessage == "assertion failed: destinations already chosen")

  }

}
