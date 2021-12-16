import java.awt.Color
import scala.concurrent.*
/**
 * Represents the player API which the Referee can make calls to. Players must be identified with a name.
 * @param name the name of the player that this Player represents
 * @param joinIndex the index of when this Player registered
 */
trait Player(val name: String,
             val joinIndex: Int,
             val chooseBoard: Board = Utils.defaultBoard
            ) {

  /**
   * Assign fields to be used during setup to choose destinations.
   */
  def setup(initBoard: Board, rails: Int, cards: Map[Color, Int]): Unit

  /**
   * Pick 2 destinations from the set of Destinations given according to the strategy.
   * @param destinations the destinations to choose from
   * @return the chosen destinations
   */
  def pick(destinations: Set[PlacePair]): Future[Set[PlacePair]]
  /**
   * Ask the strategy to make a turn decision based on the given PlayerState.
   * @param playerState the state of the current Player
   * @return Right(connection) if we are acquiring a connection, or Left("more cards") otherwise
   */
  def play(playerState: PlayerState): Future[Option[Connection]]

  /**
   * Method that was part of the Logical Interface spec, but does nothing  because the player will be updated
   * with the new card counts the next time the play method is called. (No point in writing redundant code.)
   * @param cards the cards that are handed to this player from the referee
   */
  def more(cards: Map[Color, Int]): Unit

  /**
   * Tells the player if they have won the game.
   * @param hasWon true if the player has won the game, false if they did not win
   */
  def win(hasWon: Boolean): Unit

  /**
   * Gives the player an opportunity to submit a board to be used in the tournament when it begins.
   * @return the board
   */
  def start: Future[Board]

  /**
   * Tells this player whether they won the tournament
   * @param won whether they won
   */
  def end(won: Boolean): Unit

}