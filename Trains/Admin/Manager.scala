import java.awt.Color
import scala.concurrent.{Await, Future}
import scala.util.Random
import scala.language.implicitConversions

/**
 * The component responsible for managing the tournament. Handles all logical interactions with Players and Referees.
 * @param players the set of all players in the game
 * @param makeDeck an optional deck of cards to use in the game
 * @param orderDestinations an optional ordering for the destinations in the game
 * @param orderCards
 */
class Manager(var players: Set[Player],
              makeDeck: List[Color] = Utils.defaultCards, 
              orderDestinations: Function[Set[PlacePair], List[PlacePair]] = Utils.defaultOrderDestinations, 
              orderCards: Function[List[Color], List[Color]] = Utils.defaultOrderCards) {
  implicit val constants: Constants = Constants()


  assert(players.size >= constants.MinPlayers, s"there must be at least ${constants.MinPlayers} players to create a tournament")
  implicit def ejectAction(player: Player): Unit = {
    players = players - player
    cheaters = cheaters + player
  }
  var cheaters = Set[Player]()

  val board = {
    val boards = setup
    val validBoards = boards.toList.filter(board => Utils.isBoardValid(board, constants.MaxPlayers))

    assert(!validBoards.isEmpty, "No submitted board was valid")
    validBoards(Random.nextInt(validBoards.size))
  }

  /**
   * Call start on each player, and return a set of all boards that they returned.
   * @return the set of boards provided by all players
   */
  def setup: Set[Board] = players.map(player => Utils.communicateWithPlayer(() => player.start, player))
    .filter(!_.isEmpty).map(_.get)

  /**
   * Given a set of all Players in the tournament, organize them into a set of games to be played according to the
   * assignment specifications.
   * @param players all players in the game
   * @return a set of Lists of players in turn order
   */
  def matchmaker(players: Set[Player]): Set[List[Player]] = {
    assert(players.size >= constants.MinPlayers,
      s"there must be at least ${constants.MinPlayers} players to create a game")

    val playerOrder = players.toList.sortBy(_.joinIndex)

    var playersAllocated = 0
    val teams = getGameSizes(players.size).map{size => {
      val team = playerOrder.slice(playersAllocated, playersAllocated + size)
      playersAllocated += size
      team
    }}.toSet

    teams
  }

  /**
   * Generates the number of players that will play in each game during a round
   * of a tournament with the given number of players.
   * @param players the number of players that need to be broken up into games
   * @return a list of the size of each game.
   */
  def getGameSizes(players: Int): List[Int] = {
    def gameSizesRecursive(remainingPlayers: Int, soFar: List[Int]): List[Int] = {
      if (remainingPlayers == 0) {
        return soFar
      }
      if (remainingPlayers >= constants.MinPlayers) {
        val toAdd = Math.min(constants.MaxPlayers, remainingPlayers)
        return gameSizesRecursive(remainingPlayers - toAdd, soFar :+ toAdd)
      } else {
        return gameSizesRecursive(remainingPlayers + 1, soFar.slice(0, soFar.size - 1) :+ soFar.last - 1)
      }
    }
    gameSizesRecursive(players, List())
  }


  /**
   * Plays all games in the tournament, returning the final outcome.
   * @return ranking, cheaters
   */
  def playTournament: (Set[Player], Set[Player]) = {
    
    var remainingPlayers = players
    var lastWinners = Set[Player]()

    def playRound: Set[Player] = {
      val result = matchmaker(remainingPlayers).map(playGameInTournament).toList.unzip
      val (winners, ejected) = (result._1.toSet.flatten, result._2.toSet.flatten)
      remainingPlayers = winners
      cheaters = cheaters union ejected
      winners
    }

    /**
     * Plays rounds until there are fewer than the maximum number of players
     * in a single game remaining, or the result of two rounds in a row is the
     * same.
     */
    def roundLoop: Unit = {
      while(remainingPlayers.size > constants.MaxPlayers) {
        val winners = playRound
        if (lastWinners == winners) {
          return
        }
        lastWinners = winners
      }
    }
    roundLoop
    
    if (remainingPlayers.size >= constants.MinPlayers) {
      playRound
    }
    
    finalOutcome(remainingPlayers)
    (remainingPlayers, cheaters)
  }
  


  /**
   * Plays a single game in the tournament by setting up a referee and having them play out the game.
   * @param playersInGame The players that are to play against each other in a game of Trains!
   * @return the winners and cheaters of a game of Trains!
   */
  def playGameInTournament(playersInGame: List[Player]): (Set[Player], Set[Player]) = {
    val ref = new Referee(board, playersInGame, makeDeck, orderDestinations, orderCards)
    val (ranking, cheaters) = ref.playGame
    (if (ranking.isEmpty) Set() else ranking(0), cheaters)
  }

  /**
   * Tells each player that competed in this tournament whether they won or not.
   * @param winners the players that will be told that they won.
   */
  def finalOutcome(winners: Set[Player]): Unit =
    players.map(player => player.end(winners contains player))





}
