import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import matchers.should.Matchers.*
import org.scalatest.matchers.should.Matchers

import java.awt.Color

object TimeoutStrategy extends Strategy {
  def chooseDestinations(privateState: PrivateState, board: Board, destinations: Set[PlacePair]): Set[PlacePair] = {
    Thread.sleep(1100)
    Set()
  }

  def takeTurn(playerState: PlayerState): Option[Connection] = {
    Thread.sleep(1100)
    None
  }
}

object DestExceptionStrategy extends Strategy {
  override def chooseDestinations(privateState: PrivateState, board: Board, destinations: Set[PlacePair]): Set[PlacePair] =
    throw new IllegalStateException("bad player")

  override def takeTurn(playerState: PlayerState): Option[Connection] =
    None
}

object TurnExceptionStrategy extends Strategy {
  override def chooseDestinations(privateState: PrivateState, board: Board, destinations: Set[PlacePair]): Set[PlacePair] =
    Hold10.chooseDestinations(privateState, board, destinations)

  override def takeTurn(playerState: PlayerState): Option[Connection] =
    throw new IllegalStateException("bad player")
}

class RefereeTest extends AnyFunSuite with Matchers {
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
  val board = Board(Places, Connections, Posn(100, 100))

  val playerJoe = AIPlayer("Joe", "StratTest", "StratTest", 1)
  val playerBob = AIPlayer("Bob", "StratTest", "StratTest", 2)
  val playerSam = AIPlayer("Sam", "StratTest", "StratTest", 3)

  val cardCounts = Utils.cardCountsToCardList(Map(Color.BLUE -> 50, Color.GREEN -> 40, Color.RED -> 45, Color.WHITE -> 41)) // 176 left


  test("Referee plays game with two players and it ends with Bob winning") {
    val ref = Referee(board, List(playerJoe, playerBob), cardCounts, Utils.sortDestinations, a => a)
    val results = ref.playGame
    assert(results._1.map(_.map(_.name)) == List(Set("Joe"), Set("Bob")))
    assert(ref.refereeState.publicState.whoHasLongestRoute == Set("Joe"))
  }

  test("Referee can't start game without a map that has enough destinations for all players") {
    var caught = intercept[AssertionError] {
      Referee(board, List(playerJoe, playerBob, playerSam), cardCounts, Utils.sortDestinations, a => a)
    }
    assert(caught.getMessage == "assertion failed: there must be enough destinations for all players to be able to choose from the maximum number of options")
  }

  test("Referee plays game with two players and one is ejected for taking too long") {
    val playerSleep = new AIPlayer("sleep", TimeoutStrategy, 4)
    val ref = Referee(board, List(playerJoe, playerSleep), cardCounts, Utils.sortDestinations, a => a)
    assert(ref.playGame == (List(Set(playerJoe)), Set(playerSleep)))
    assert(ref.refereeState.publicState.whoHasLongestRoute == Set("Joe"))
  }

  test("referee should eject a player that throws an exception on their turn") {
    val playerDestException = new AIPlayer("bad", DestExceptionStrategy, 5)
    val playerTurnException = new AIPlayer("bad2", TurnExceptionStrategy, 6)
    val ref = Referee(board, List(playerDestException,
          playerTurnException), cardCounts, Utils.sortDestinations, a => a)
    assert(ref.playGame == (List(), Set(playerDestException, playerTurnException)))
  }


  test("something that fails") {
    val cities = (0 to 19).map(n => Place(n.toString, Posn(n, n)) ).toList

    val from0 = List(
      Connection(PlacePair(cities(0), cities(13)), Color.GREEN, 3),
      Connection(PlacePair(cities(0), cities(19)), Color.WHITE, 3),
      Connection(PlacePair(cities(0), cities(3)), Color.WHITE, 3),
      Connection(PlacePair(cities(0), cities(7)), Color.RED, 3)
    )
    val from1 = List(
      Connection(PlacePair(cities(1), cities(10)), Color.BLUE, 3),
      Connection(PlacePair(cities(1), cities(11)), Color.WHITE, 3),
      Connection(PlacePair(cities(1), cities(11)), Color.GREEN, 3),
      Connection(PlacePair(cities(1), cities(15)), Color.GREEN, 3),
      Connection(PlacePair(cities(1), cities(3)), Color.BLUE, 3),

    )
    val from10 = List(
      Connection(PlacePair(cities(10), cities(13)), Color.GREEN, 3),
      Connection(PlacePair(cities(10), cities(2)), Color.BLUE, 3),
      Connection(PlacePair(cities(10), cities(7)), Color.GREEN, 3),
    )
    val from11 = List(
      Connection(PlacePair(cities(11), cities(13)), Color.GREEN, 3),
      Connection(PlacePair(cities(11), cities(8)), Color.RED, 3),
    )
    val from12 = List(
      Connection(PlacePair(cities(12), cities(16)), Color.GREEN, 3),
      Connection(PlacePair(cities(12), cities(18)), Color.GREEN, 3),
      Connection(PlacePair(cities(12), cities(19)), Color.RED, 3),
      Connection(PlacePair(cities(12), cities(2)), Color.WHITE, 3),
      Connection(PlacePair(cities(12), cities(6)), Color.GREEN, 3),
      Connection(PlacePair(cities(12), cities(6)), Color.WHITE, 3),

    )
    val from13 = List(
      Connection(PlacePair(cities(13), cities(4)), Color.GREEN, 3),
    )
    val from15 = List(
      Connection(PlacePair(cities(15), cities(7)), Color.BLUE, 3),
      Connection(PlacePair(cities(15), cities(9)), Color.WHITE, 3),

    )
    val from16 = List(
      Connection(PlacePair(cities(16), cities(17)), Color.GREEN, 3),
      Connection(PlacePair(cities(16), cities(2)), Color.GREEN, 3),
    )
    val from17 = List(
      Connection(PlacePair(cities(17), cities(3)), Color.BLUE, 3),
      Connection(PlacePair(cities(17), cities(5)), Color.RED, 3),
      Connection(PlacePair(cities(17), cities(7)), Color.RED, 3),
      Connection(PlacePair(cities(17), cities(9)), Color.BLUE, 3),
    )
    val from18 = List(
      Connection(PlacePair(cities(18), cities(6)), Color.WHITE, 3),
      Connection(PlacePair(cities(18), cities(9)), Color.WHITE, 3),
    )
    val from19 = List(
      Connection(PlacePair(cities(19), cities(6)), Color.BLUE, 3),
      Connection(PlacePair(cities(19), cities(9)), Color.RED, 3),
    )
    val from2 = List(
      Connection(PlacePair(cities(2), cities(5)), Color.GREEN, 3),
    )
    val from3 = List(
      Connection(PlacePair(cities(3), cities(9)), Color.BLUE, 3),
    )
    val from5 = List(
      Connection(PlacePair(cities(5), cities(6)), Color.WHITE, 3),
      Connection(PlacePair(cities(5), cities(7)), Color.GREEN, 3),
      Connection(PlacePair(cities(5), cities(8)), Color.BLUE, 3),
    )

    val connections =
      from0 ++ from1 ++ from2 ++ from3 ++ from5 ++
        from10 ++ from11 ++ from12 ++ from13 ++ from15 ++ from16 ++
        from17 ++ from18 ++ from19
    val board = Board(cities.toSet, connections.toSet, Posn(200, 800))
    val cards = List.fill(250)(Color.WHITE)
    val cameron = AIPlayer("cameron", "Hold10", "Hold10", 0, board)
    val ben = AIPlayer("ben", "BuyNow", "BuyNow", 1, board)
    val matthias = AIPlayer("matthias", "Cheat", "Cheat", 2, board)

    val ref = Referee(board, List(matthias, cameron, ben), cards, Utils.sortDestinations, d => d)
    val results = ref.playGame

    
    ref.refereeState.publicState.acquiredConnections.foreach{case (k, v) => {
      println(k + ": ")
      ref.refereeState.privateStates(k).destinations.foreach(println)
      v.foreach(println)
      println(f"getDestinationScore for ${k}: ${ref.refereeState.getDestinationScore(k)}")
      println(f"getAcquiredScore for ${k}: ${ref.refereeState.getAcquiredScore(k)}")
    }}

    println(f"longest route: ${ref.refereeState.publicState.whoHasLongestRoute}")

    //assert(ref.refereeState.getScores == Map())

    assert(results == (List(Set(ben), Set(cameron)), Set(matthias)))

    

  }
}

