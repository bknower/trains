import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import matchers.should.Matchers.*
import org.scalatest.matchers.should.Matchers
import java.awt.Color
import java.util.Date
import scala.io.Source

/** Test RefereeState using the scalatest library */
class RefereeStateTest extends AnyFunSuite with Matchers {
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
  val publicState = PublicState(board, acquiredConnections, currentTurn, playerNames)

  val cardCountsJoe = Map(Color.BLUE -> 5, Color.GREEN -> 2, Color.RED -> 1, Color.WHITE -> 0)
  val cardCountsBob = Map(Color.BLUE -> 10, Color.GREEN -> 0, Color.RED -> 2, Color.WHITE -> 4)
  val cardCountsSam = Map(Color.BLUE -> 1, Color.GREEN -> 5, Color.RED -> 3, Color.WHITE -> 0)
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
  val refereeState = RefereeState(publicState, playerStates, cardCountsRef, a => a)

  // --- UNIT TESTS ---
  // Tests for PublicState
  // Tests for getPlayerState
  test("getCurrentPlayerState returns correct PlayerState") {
    assert(refereeState.getCurrentPlayerState == PlayerState(publicState, playerStateJoe))
  }

  // Tests for canCurrentPlayerAcquire
  test("canCurrentPlayerAcquire returns correct boolean value") {
    assert(refereeState.canCurrentPlayerAcquire(CDBlue)) // should be true, Joe has 5 blue cards
    assert(!refereeState.canCurrentPlayerAcquire(EFWhite)) // should be false, Joe has 0 white cards
  }

  // Tests for acquireConnection
  test("acquireConnection returns correct RefereeState") {
    val acquiredConnectionsJoeAcquiredCDBlue = Map("Joe" -> Set(CDBlue), "Bob" -> Set(), "Sam" -> Set(ABWhite))
    val pubStateJoeAcquiredCDBlue = PublicState(board, acquiredConnectionsJoeAcquiredCDBlue, "Bob", playerNames)
    val cardCountsJoeAcquiredCDBlue = Map(Color.BLUE -> 0, Color.GREEN -> 2, Color.RED -> 1, Color.WHITE -> 0)
    val playerStateJoeUpdated = PrivateState(cardCountsJoeAcquiredCDBlue,40,destinationsJoe)
    val playerStateBobUpdated = PrivateState(cardCountsBob,rails,destinationsBob)
    val playerStateSamUpdated = PrivateState(cardCountsSam,railsSam,destinationsSam)
    val playerStatesJoeAcquiredCDBlue = Map("Joe" -> playerStateJoeUpdated, "Bob" -> playerStateBobUpdated, "Sam" -> playerStateSamUpdated)
    val refStateJoeAcquiredCDBlue = RefereeState(pubStateJoeAcquiredCDBlue, playerStatesJoeAcquiredCDBlue, cardCountsRef)
    println(refereeState.privateStates("Bob"))
    refereeState.copy(publicState = refereeState.publicState.copy(currentTurn= "Bob")).acquireConnection(CDBlue) should matchPattern {
      case RefereeState(pubStateJoeAcquiredCDBlue, playerStatesJoeAcquiredCDBlue, cardCountsRef, _, _, _) =>
    }
  }

  // Tests for requestCards
  test("requestCards returns correct RefereeState") {
    // randomly chosen colored cards makes testing less simple
    val nextRefState = refereeState.requestCards
    val nextJoeState = nextRefState.privateStates("Joe")
    val nextPubState = PublicState(board, acquiredConnections, "Bob", playerNames)
    // check that Joe has 2 more cards (random colors)
    assert(nextJoeState.cardCounts.foldLeft(0)(_+_._2) == playerStateJoe.cardCounts.foldLeft(0)(_+_._2)+2)
    // check that the public state was updated (just the current turn)
    assert(nextRefState.publicState == nextPubState)
  }

  //cities
  val city1 = Place("boston", Posn(10,10))
  val city2 = Place("seattle", Posn(20, 10))
  val city3 = Place("miami", Posn(5, 15))
  val city4 = Place("houston", Posn(30, 20))
  //connections
  val c12 = Connection(PlacePair(city1, city2), Color.RED, 3)
  val c23 = Connection(PlacePair(city2, city3), Color.WHITE, 4)
  val c13 = Connection(PlacePair(city1, city3), Color.BLUE, 5)
  val c24 = Connection(PlacePair(city2, city4), Color.RED, 3)
  val pcs = Set(city1, city2, city3, city4)
  val cns = Set(c12, c13, c23, c24)
  //board
  val bd = new Board(pcs, cns, Posn(50, 50))

  //state info
  val aConnections = Map("bill" -> Set(c12, c23), "steve" -> Set(c13, c24), "mark" -> Set())
  val cTurn = "bill"
  val pNames = List("bill", "steve", "mark")
  val pState = PublicState(bd, aConnections, cTurn, pNames)
  //bill
  val billCards = Map(Color.BLUE -> 1, Color.GREEN -> 5, Color.RED -> 3, Color.WHITE -> 0)
  val billRails = 38
  val billDestinations = Set(PlacePair(city1, city2), PlacePair(city1, city3))
  val billState = PrivateState(billCards, billRails, billDestinations)
  //steve
  val steveCards = Map(Color.BLUE -> 2, Color.GREEN -> 0, Color.RED -> 3, Color.WHITE -> 1)
  val steveRails = 37
  val steveDestinations = Set(PlacePair(city1, city4), PlacePair(city2, city4))
  val steveState = PrivateState(steveCards, steveRails, steveDestinations)
  //mark
  val markCards = Map(Color.BLUE -> 1, Color.GREEN -> 5, Color.RED -> 3, Color.WHITE -> 0)
  val markRails = 45
  val markDestinations = Set[PlacePair]()
  val markState = PrivateState(markCards, markRails, markDestinations)

  val refCards = Utils.cardCountsToCardList(Map(Color.BLUE -> 50, Color.GREEN -> 60, Color.RED -> 70, Color.WHITE -> 40))
  val allPStates = Map("bill" -> billState, "steve" -> steveState, "mark" -> markState)
  val refState = RefereeState(pState, allPStates, refCards, a => a)


  test("getScores returns the correct score for the sum of all acquired segments") {
    assert(refState.getScores.get("bill").getOrElse(throw new IllegalStateException()) == 47)
    assert(refState.getScores.get("steve").getOrElse(throw new IllegalStateException()) == 8)
  }

  test("getAcquiredScore returns the correct score for acquired connections") {
    assert(refState.getAcquiredScore("bill") == 7)
    assert(refState.getAcquiredScore("steve") == 8)


//    val acquired = Map("ben" -> Set(Connection(PlacePair(Set(Place("0",Posn(0,0)), Place("3",Posn(3,3)))),Color.WHITE,3)
//    Connection(PlacePair(Set(Place("12",Posn(12,12)), Place("2",Posn(2,2)))),Color.WHITE,3)
//    Connection(PlacePair(Set(Place("0",Posn(0,0)), Place("19",Posn(19,19)))),Color.WHITE,3)
//    Connection(PlacePair(Set(Place("18",Posn(18,18)), Place("9",Posn(9,9)))),Color.WHITE,3)
//    Connection(PlacePair(Set(Place("15",Posn(15,15)), Place("9",Posn(9,9)))),Color.WHITE,3)),
//    "cameron" -> Set(
//      Connection(PlacePair(Set(Place("1",Posn(1,1)), Place("11",Posn(11,11)))),Color.WHITE,3)
//    Connection(PlacePair(Set(Place("12",Posn(12,12)), Place("6",Posn(6,6)))),Color.WHITE,3)
//    Connection(PlacePair(Set(Place("18",Posn(18,18)), Place("6",Posn(6,6)))),Color.WHITE,3)
//    Connection(PlacePair(Set(Place("5",Posn(5,5)), Place("6",Posn(6,6)))),Color.WHITE,3)
//    )
//    )
//    val cities = (0 to 19).map(n => Place(n.toString, Posn(n, n)) ).toList

//    val publicState = PublicState(bd, acquired, "cameron", List("cameron", "ben"))
//    val benPrivateState =
//    val cameronPrivateState =
//    val refereeState = RefereeState()

  }

  test("chooseDestinations enforces that a player chose two destinations") {
    val d = Set(PlacePair(city1, city2))
    assertThrows[AssertionError](refState.chooseDestinations(d, "steve"))
  }

  test("chooseDestination updates a player's chosen destinations") {
    assert(refState.chooseDestinations(Set(PlacePair(city2, city3),
      PlacePair(city3, city4)), "mark").privateStates.get("mark")
      .getOrElse(throw new IllegalStateException()).destinations ==
      Set(PlacePair(city2, city3),PlacePair(city3, city4)))
  }

  // Need to Test:
  /*
  chooseDestinations:
    assertion
    updates


  */
  
}