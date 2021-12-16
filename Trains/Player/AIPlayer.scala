import org.json4s.JsonAST.JString
import java.io.File
import java.net.URLClassLoader
import org.json4s.*
import org.json4s.jackson.JsonMethods.*

import java.awt.Color
import scala.io.Source
import scala.concurrent.*
import concurrent.ExecutionContext.Implicits.global
/**
 * Represents a player component which uses a Strategy from the given filepath.
 * @param filePath the path to the Strategy that this player should use
 *                 the filePath should be to (should be in the Player folder)
 * @param name the name of the player that this Player represents
 * @param className the name of the class in the file
 */
class AIPlayer(override val name: String,
               val strategy: Strategy,
               override val joinIndex: Int,
               override val chooseBoard: Board = Utils.defaultBoard) extends Player(name, joinIndex, chooseBoard) {
  implicit val constants: Constants = Constants()



  //val fileContents = Source.fromFile("Trains/Player/" + filePath).getLines().mkString("\n")
  //val className = """(?<=object\s).*(?=\sextends.*Strategy)""".r.findFirstIn(fileContents).get
  //println(className)

  var board: Option[Board] = None
  var privateState: Option[PrivateState] = None

  /**
   * Assign fields to be used during setup to choose destinations.
   */
  def setup(initBoard: Board, rails: Int, cards: Map[Color, Int]): Unit = {
    board = Some(initBoard)
    privateState = Some(PrivateState(cards, rails, Set()))
  }

  /**
   * Pick 2 destinations from the set of Destinations given according to the strategy.
   * @param destinations the destinations to choose from
   * @return the chosen destinations
   */
  def pick(destinations: Set[PlacePair]): Future[Set[PlacePair]] = Future {
    assert(!board.isEmpty && !privateState.isEmpty, "setup must be called before pick")
    assert(destinations.size == constants.DestinationChoices, "Player was not given the right number of destinations to choose from")
    destinations diff strategy.chooseDestinations(privateState.get, board.get, destinations)
  }

  /**
   * Ask the strategy to make a turn decision based on the given PlayerState.
   * @param playerState the state of the current Player
   * @return Right(connection) if we are acquiring a connection, or Left("more cards") otherwise
   */
  def play(playerState: PlayerState): Future[Option[Connection]] = Future {
    strategy.takeTurn(playerState)
  }

  /**
   * Method that was part of the Logical Interface spec, but does nothing  because the player will be updated
   * with the new card counts the next time the play method is called. (No point in writing redundant code.)
   * @param cards the cards that are handed to this player from the referee
   */
  def more(cards: Map[Color, Int]): Unit = {}

  /**
   * Tells the player if they have won the game.
   * @param hasWon true if the player has won the game, false if they did not win
   */
  def win(hasWon: Boolean): Unit = {}

  def start: Future[Board] = Future(chooseBoard)

  def end(won: Boolean): Unit = {}

}


object AIPlayer {
  def apply(name: String, filePath: String, className: String, joinIndex: Int, chooseBoard: Board = Utils.defaultBoard): AIPlayer = {

    val myFolder = new File(filePath)
    val classLoader = new URLClassLoader(Array(myFolder.toURI.toURL), Thread.currentThread.getContextClassLoader)

    val PlayerStrategy = classLoader.loadClass(className + "$").getField("MODULE$").get(null)

    val strategy = PlayerStrategy.asInstanceOf[Strategy]
    new AIPlayer(name, strategy, joinIndex, chooseBoard)
  }
}

/**
 * Test for reading in a strategy from a file.
 */
object TryStrategy {
  def main(args: Array[String]): Unit = {
    // list of JValues containing the map, playerstate, and acquired
    val input = Utils.readMultipleJValues

    val board = Utils.jsonToBoard(input(0))

    val playerState = Utils.jsonToPlayerState(input(1), board)

    val filePath = "StratTest"

    //println(AIPlayer(filePath, "person", filePath).play(playerState))
  }

}