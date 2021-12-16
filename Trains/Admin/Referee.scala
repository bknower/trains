
import java.awt.Color
import scala.concurrent.{Await, Future, TimeoutException}
import scala.util.Random
import scala.concurrent.duration.*
import Utils.*
import scala.language.implicitConversions
import concurrent.ExecutionContext.Implicits.global

/**
 * Represents a Referee in the Trains game, who manages the play of a single game.
 * 
 * Abnormal interactions that lead to ejection of a player currently:
 * - returning destinations that were not in the options, or not exactly 3 not-chosen destinations from pick
 * - attempting to acquire a connection that cannot be acquired or does not exist on the map
 * - taking too long to respond
 *
 * Abnormal interactions that may be of concern later:
 * - invalid JSON representation of our data when sent over network
 */
class Referee(board: Board, players: List[Player],
              cardCounts: List[Color] = Utils.defaultCards,
              orderDestinations: Function[Set[PlacePair], List[PlacePair]] = Utils.defaultOrderDestinations,
              orderCards: Function[List[Color], List[Color]] = Utils.defaultOrderCards) {
  implicit val constants: Constants = Constants()
  implicit def ejectAction(player: Player): Unit = {
    refereeState = refereeState.ejectPlayer(player.name)
  }

  assert(constants.MinPlayers to constants.MaxPlayers contains players.size, "referee handed invalid number of players")
  assert(players.map(_.name).distinct.size == players.size, "all players must have unique names")


  val turnOrder = players.map(_.name)
  var refereeState = initRefereeState
  val nameToPlayer = players.map(player => (player.name, player)).toMap

  assert(Utils.isBoardValid(board, players.size),
  "there must be enough destinations for all players to be able to choose from the maximum number of options")


  /**
   * Initialize the referee state before the setup for the game. Initialize each PlayerState with the correct number of
   * rails and randomly drawn cards.
   */
  private def initRefereeState: RefereeState = {
    val cards = orderCards(cardCounts)
    val cardsPerPlayer = (0 to players.size).map(i => cards.slice(i * (constants.CardsPerPlayer), (i + 1) * (constants.CardsPerPlayer))).toList


    val mutableCards = cards.toBuffer
    mutableCards.remove(0, players.size * constants.CardsPerPlayer)

    val privateStates: Map[String, PrivateState] = players.map(_.name)
      .zip(cardsPerPlayer.map(playerCards =>
        PrivateState(Utils.cardListToCardCounts(playerCards), constants.DefaultRails, Set()))).toMap

    RefereeState(
      PublicState(board, turnOrder.map((player) => (player, Set[Connection]())).toMap, turnOrder(0), turnOrder),
      privateStates,
      mutableCards.toList,
      orderCards
    )
  }

  /**
   * Runs through the logical sequence of the game, returning the final results.
   * @return the list of sets of players representing the ranking.
   */
  def playGame: (List[Set[Player]], Set[Player]) = {
    setup
    steadyState
    teardown
    (refereeState.getRanking.map(_.map(name => nameToPlayer(name))), getCheaters)
  }

  /**
   * Get a set of the names of any players who were ejected from the game.
   * @return set of cheaters
   */
  private def getCheaters = (players.map(_.name).toSet diff refereeState.publicState.playerNames.toSet)
    .map(name => nameToPlayer(name))
  
  /**
   * Perform the Setup for the game as specified by the Logical Interface on the course website.
   * (Give each player their initial info and ask them to choose destinations. Done in turn order.)
   */
  private def setup: Unit = {
    val destinations = orderDestinations(board.getAllDestinations).toBuffer
    players.foreach(player => {
      val privateState = refereeState.privateStates(player.name)


      // give the player their initial state
      communicateWithPlayer(() => 
        Future(player.setup(board, privateState.rails, privateState.cardCounts)), player)

      val options = destinations.slice(0, (constants.DestinationChoices)).toSet

      // ask the player to choose their destinations

      val response = communicateWithPlayer(() => player.pick(options), player)

      response match {
        case Some(notChosen) => {
          // if the player's choice was invalid, eject them
          refereeState = if (!((notChosen subsetOf options) && notChosen.size == 3)) {
            refereeState.ejectPlayer(player.name)
          } else {
            val chosen = options diff notChosen

            // remove the destinations from the list and update refereeState with the change
            destinations --= chosen
            refereeState.chooseDestinations(chosen, player.name)
          }
        }
        case None => {}
      }
    }
    )
  }


  /**
   * Execute player turns in order until the game is over.
   */
  private def steadyState: Unit = {
    while (!refereeState.isGameOver) {
      val currentPlayer = refereeState.publicState.currentTurn


      val response = communicateWithPlayer(() =>
        nameToPlayer(currentPlayer).play(refereeState.getCurrentPlayerState),
        nameToPlayer(currentPlayer)
      )

      response match {
        case Some(decision) => refereeState = decision match {
          case Some(connection) => refereeState.acquireConnection(connection)
          case None => refereeState.requestCards
        }
        case None => {}
      }
    }
  }


  /**
   * Send each player whether they won.
   */
  private def teardown: Unit = {
    assert(refereeState.isGameOver, "cannot enter teardown before the game is over")
    val winners = refereeState.getWinners
    players.foreach(player => communicateWithPlayer(() => Future(player.win(winners contains player.name)), player))
  }
}

//object Referee {
//  val timeout = 1 seconds
//  val DefaultRails = 45
//  val CardsPerPlayer = 4
//  val DestinationChoices = 5
//  val MinPlayers = 2
//  val MaxPlayers = 8
//}