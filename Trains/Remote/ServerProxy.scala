import Utils.anyToJValue
import org.json4s.JsonAST.JValue
import org.json4s.*
import org.json4s.jackson.JsonMethods.*
import org.json4s.JsonDSL.seq2jvalue
import scala.language.postfixOps
import concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.Success
import java.net.Socket

/**
 * Translates json input into method calls on a player and then translates the player's response back into
 * json to be output again. Acts as the server from the perspective of one player.
 * @param player the player to act as the server for
 * @param socket the socket to send over
 */
class ServerProxy(val player: Player, socket: Socket) {
  implicit val constants: Constants = Constants()
  var board: Option[Board] = None
  var ended = false

  /**
   * Waits for input from the server and translates it into calling a method on the player whenever it is received.
   * Sends the player's response back over the network.
   */
  def run: Unit = {
    while (!ended) {
      val input = Utils.waitForInputThenReadAll(socket.getInputStream, Integer.MAX_VALUE).get
      val result = callMethod(parse(input))
      Console.withOut(socket.getOutputStream) {
        println(result)
      }
    }
  }

  /**
   * Takes in json in the form of a JValue, translating it to our internal
   * data representations, passing it to some player, and translating their response back to a JSON string.
   * @param json the json in the form of a JValue that the method call is passed in as
   * @return the string of JSON representing the player's response
   */
  def callMethod(json: JValue): String = {
    json match {
      case JArray(methodCall) =>
        val JArray(args) = methodCall(1)
        methodCall(0) match {
          case JString("setup") => {
            val JInt(rails) = args(1)
            board = Some(Utils.jsonToBoard(args(0)))
            player.setup(board.get, rails.toInt, Utils.cardListToCardCounts(Utils.jsonToDeck(args(2))))
            "\"void\""
          }
          case JString("pick") => {
            awaitAndRenderPlayerResponse(
              () => player.pick(Utils.jsonToPlacePairSet(args(0), board.get)))
          }
          case JString("play") =>
            val playerState = Utils.jsonToPlayerState(args(0), board.get)
            awaitAndRenderPlayerResponse(
              () => player.play(playerState))
          case JString("more") => {
            player.more(Utils.cardListToCardCounts(Utils.jsonToDeck(args(0))))
            "\"void\""
          }
          case JString("win") => {
            val JBool(bool) = args(0)
            player.win(bool)
            "\"void\""
          }
          case JString("start") => {
            awaitAndRenderPlayerResponse(() => player.start)
          }
          case JString("end") => {
            val JBool(bool) = args(0)
            player.end(bool)
            ended = true
            "\"void\""
          }
        }
    }
  }

  /**
   * Calls some method on a player, waits for their response, and then renders it to a JSON string when they
   * respond. (A timeout error will be thrown if they do not respond in time.
   * @return the player's response as a JSON string,
   */
  private def awaitAndRenderPlayerResponse(toCall: () => Future[Any]): String = {
    val result = Await.ready(toCall(), (constants.ClientResponseTimeout / 1000) seconds).value.get.get
    pretty(render(
      Utils.anyToJValue( result)))
  }
  

}
