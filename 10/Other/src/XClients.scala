import java.net.Socket
import java.util.concurrent.{Callable, ExecutorService, Executors}
import org.json4s.*
import org.json4s.jackson.JsonMethods.*
import org.json4s.JsonDSL.seq2jvalue

/**
 * reads in input from command line; creates internal representations of board, players and deck
 * from json representation. Creates and runs a Client for each player.
 */
object XClients {
  implicit val constants: Constants = Constants()

  def main(args: Array[String]): Unit = {
    val port = args(0).toInt
    val ip = if (args.length > 1) args(1) else "127.0.0.1"


    val input = Utils.readMultipleJValues

    val board = Utils.jsonToBoard(input(0))
    val players = Utils.jsonToPlayerInstanceWithBoard(input(1), board)
    val deck = Utils.jsonToDeck(input(2))

    val pool: ExecutorService = Executors.newFixedThreadPool(constants.ClientThreadPoolSize)
    players.foreach(p =>
      pool.execute(new Client(new Socket(ip, port), p))
    )
    pool.shutdown()
  }


}


