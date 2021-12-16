import org.json4s.*
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods.*

import java.awt.Color
import java.io.File
import java.net.URLClassLoader
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import scala.language.postfixOps
import scala.concurrent.duration.DurationInt
import java.net.{ServerSocket, Socket, SocketTimeoutException}

/**
 * A proxy class for a Player in a game of Trains that allows for communication over a network.
 * Translates method calls into JSON that conforms to the remote interactions spec
 * @param name the name of the player that this Player represents
 * @param joinIndex the index of when this Player registered
 * @param socket the socket to communicate over
 */
class PlayerProxy(override val name: String, override val joinIndex: Int, val socket: Socket)
  extends Player(name, joinIndex) {
  implicit val constants: Constants = Constants()

  var board: Option[Board] = None
  var privateState: Option[PrivateState] = None
  private var publicState: Option[PublicState] = None

  /**
   * Assign fields to be used during setup to choose destinations.
   *
   * Proxy calls "setup" method across the network. sends method name and arguments according
   * to outlined JSON protocol.
   */
  def setup(initBoard: Board, rails: Int, cards: Map[Color, Int]): Unit = {
    board = Some(initBoard)
    privateState = Some(PrivateState(cards, rails, Set()))
    callMethod(
      Utils.createJSONMethodCall(
        "setup",
        List(initBoard, rails, Utils.cardCountsToCardList(cards))))
  }

  /**
   * Pick 2 destinations from the set of Destinations given according to the strategy.
   * 
   * Proxy calls "pick" method across the network and converts JSON response back to game data.
   * @param destinations the destinations to choose from
   * @return the chosen destinations
   */
  def pick(destinations: Set[PlacePair]): Future[Set[PlacePair]] = Future {
    Utils.jsonToPlacePairSet(callMethod(
      Utils.createJSONMethodCall("pick", List(destinations))), board.get)
  }

  /**
   * 
   * Proxy converts args to JSON and sends method call across the network. Converts JSON response
   * back into game data.
   * @param playerState the state of the current Player
   * @return Some(connection) if we are acquiring a connection, or None otherwise
   */
  def play(playerState: PlayerState): Future[Option[Connection]] = Future {
    Utils.jsonToOptionConnection(callMethod(Utils.createJSONMethodCall(
      "play", List(playerState))),
      board.get)
  }

  /**
   * Gives the players the new cards they requested
   * @param cards the cards
   */
  def more(cards: Map[Color, Int]): Unit = callMethod(Utils.createJSONMethodCall("more", List(cards)))

  /**
   * Tells the player if they have won the game.
   * @param hasWon true if the player has won the game, false if they did not win
   */
  def win(hasWon: Boolean): Unit = callMethod(Utils.createJSONMethodCall("win", List(hasWon)))

  /**
   *  Lets the player know that the tournament is starting, expecting a Board submission in response
   *  @return a future of the submitted board, indicating that a board will be returned at some point.
   */
  def start: Future[Board] = Future {
    Utils.jsonToBoard(callMethod(Utils.createJSONMethodCall(
        "start", List())))
  }

  /**
   * Lets a player know that the tournament has ended, and whether they won.
   * @param won whether they won
   */
  def end(won: Boolean): Unit = callMethod(Utils.createJSONMethodCall("end", List(won)))

  /**
   * Sends a given JSON method call on a Player over the network
   * Conforms to the JSON communication protocol outlined for our system.
   * Waits for a response to the method call and sends it back through a PlayerProxy.
   *
   * Throws an exception if there is no response before timeout
   * @param json the already formatted JSON method call on a Player
   * @return the JSON response that the Player client sends in response to the method call
   */
  private def callMethod(json: String): JValue = {
    var response = ""
    val result = Console.withOut(socket.getOutputStream) {
      try {
        // DO NOT CHANGE - this sends the JSON method call
        println(json)
        //this should be the JSON response as String
        response = Utils.waitForInputThenReadAll(socket.getInputStream, constants.ClientResponseTimeout).get
        //this gives the JValue
        parse(response)
      } catch {
        e => throw new TimeoutException(f"Player failed to respond within ${constants.ClientResponseTimeout} milliseconds")
      }
    }
    result
  }

}