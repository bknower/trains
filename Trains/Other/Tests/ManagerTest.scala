import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.*
import org.mockito.Mockito.{clearInvocations, doReturn, spy, times, verify, when}
import org.mockito.ArgumentMatchers.*
import org.mockito.invocation.*

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters.*
class ManagerTest extends AnyFunSuite with Matchers with MockitoSugar {
//  implicit def toAnswerWithArgs[T](f: InvocationOnMock => T) = new Answer[T] {
//    override def answer(i: InvocationOnMock): T = f(i)
//  }

  val ref = mock[Referee]

  test("construct a valid Manager") {
    noException should be thrownBy Manager(Set(AIPlayer("player", "BuyNow", "BuyNow", 0),
      AIPlayer("player1", "BuyNow", "BuyNow", 1)))
  }

  test("Manager constructed with invalid number of players") {
    var caught = intercept[AssertionError] {
      Manager(Set(AIPlayer("player", "BuyNow", "BuyNow", 0)))
    }
    assert(caught.getMessage == "assertion failed: there must be at least 2 players to create a tournament")

  }


  test("Manager sets up all players") {
    val playerMocks = List.fill(3)(mock[AIPlayer])
    //this "calls" start the first time on each player. meant to define return value of ``start`` on mock Player
    playerMocks.foreach(player => when(player.start).thenReturn(Future(Utils.defaultBoard)))
    //playerMocks.foreach(player => reset(player))
    val manager = Manager(playerMocks.toSet)
    manager.setup


    // called 2 times for each player because of the first call that happens when mocking
    playerMocks.foreach(player => verify(player, times(2)).start)
  }

  test("Manager supplies board when players don't supply feasible boards") {
    var caught = intercept[AssertionError] {
      val playerMocks = List.fill(3)(mock[AIPlayer])
      playerMocks.foreach(player => when(player.start).thenReturn(Future(Utils.smallBoard)))

      val manager = Manager(playerMocks.toSet)
      manager.setup
    }
    assert(caught.getMessage == "assertion failed: No submitted board was valid")
  }

  test("Manager chooses a feasible board supplied by a player") {
    val playerMocks = List.fill(3)(mock[AIPlayer])
    playerMocks.foreach(player => when(player.start).thenReturn(Future(Utils.normalBoard)))

    val manager = Manager(playerMocks.toSet)
    manager.setup

    assert(manager.board == Utils.normalBoard)
  }

  test("Manager creates a correctly sized match for a round of a tournament where there" +
    "are less than max number of players") {
    val playerMocks = List.fill(3)(mock[AIPlayer])
    playerMocks.foreach(player => when(player.start).thenReturn(Future(Utils.normalBoard)))

    val manager = Manager(playerMocks.toSet)
    assert(manager.matchmaker(playerMocks.toSet) == Set(playerMocks))

  }

  test("getGameSizes gives the right sized games") {
    val playerMocks = (0 until 9).map(i => AIPlayer(i.toString, "BuyNow", "BuyNow", i)).toSet

    val manager = Manager(playerMocks.toSet)
    assert(manager.getGameSizes(6) == List(6))
    assert(manager.getGameSizes(9) == List(7, 2))
    assert(manager.getGameSizes(20) == List(8, 8, 4))
    assert(manager.getGameSizes(24) == List(8, 8, 8))
    assert(manager.getGameSizes(25) == List(8, 8, 7, 2))

  }

  test("Manager creates correctly sized matches") {
    var players: Set[Player] = (0 until 9).map(i => AIPlayer(i.toString, "BuyNow", "BuyNow", i)).toSet
    var manager = Manager(players)
    assert(manager.matchmaker(players) ==
      Set(players.toList.sortBy(_.joinIndex).take(7), players.toList.sortBy(_.joinIndex).takeRight(2)))

    players = (0 until 15).map(i => AIPlayer(i.toString, "BuyNow", "BuyNow", i)).toSet
    manager = Manager(players)
    assert(manager.matchmaker(players) ==
      Set(players.toList.sortBy(_.joinIndex).take(8), players.toList.sortBy(_.joinIndex).takeRight(7)))
  }

  test("a round of a tournament with no cheaters is correctly managed") {
    var players = (0 until 9).map(i => AIPlayer(i.toString, "BuyNow", "BuyNow", i)).toList
    var manager = spy(Manager(players.toSet))
    when(manager.playGameInTournament(players.take(7))).thenReturn((players.take(2), Set()))
    when(manager.playGameInTournament(players.takeRight(2))).thenReturn((players.takeRight(1), Set()))
    when(manager.playGameInTournament(players.take(2) ++ players.takeRight(1))).thenReturn((players.takeRight(1), Set()))


    assert(manager.playTournament == (players.takeRight(1).toSet, Set()))
    
  }


  test("a round of a tournament with cheaters is correctly managed") {
    var players = (0 until 8).map(i => AIPlayer(i.toString, "BuyNow", "BuyNow", i)).toList
    val badPlayer = AIPlayer("8", "Cheat", "Cheat", 8)
    players = players :+ badPlayer

    var manager = spy(Manager(players.toSet))
    when(manager.playGameInTournament(players.take(7))).thenReturn((players.take(2), Set()))
    when(manager.playGameInTournament(players.takeRight(2))).thenReturn((Set(players(7)), Set(badPlayer)))
    when(manager.playGameInTournament(players.take(2) ++ List(players(7)))).thenReturn((players.take(1), Set()))


    assert(manager.playTournament == (players.take(1).toSet, Set(badPlayer)))
  }

  test("Manager tells all players whether they won or lost") {
    val playerMocks = List.fill(3)(mock[AIPlayer])
    //this "calls" start the first time on each player. meant to define return value of ``start`` on mock Player
    playerMocks.foreach(player => when(player.start).thenReturn(Future(Utils.defaultBoard)))
    //playerMocks.foreach(player => reset(player))
    val manager = Manager(playerMocks.toSet)
    manager.finalOutcome(playerMocks.take(1).toSet)


    // called 2 times for each player because of the first call that happens when mocking
    verify(playerMocks.head, times(1)).end(true)
    playerMocks.takeRight(2).foreach(player => verify(player, times(1)).end(false))
  }
}
