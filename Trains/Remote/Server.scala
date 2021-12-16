import java.awt.Color
import java.io.{BufferedReader, InputStreamReader}
import java.net.{ServerSocket, Socket}
import java.util.concurrent.{Callable, ExecutorService, Executors, FutureTask}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.{Future, blocking}
import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationLong
import org.json4s.*
import org.json4s.jackson.JsonMethods.*
import org.json4s.JsonDSL.seq2jvalue
import scala.language.postfixOps
/**
 * The Server that our Trains! game uses to communicate with remote players. The Server handles
 * sign-ups and starting a tournament.
 * @param port The port on which this Server is listening for input
 * @param poolSize The max number of threads a pool can handle
 * @param deck The deck of cards to be used for all games of Trains!
 * @param orderDestinations The method in which to hand out destinations for players to choose from
 * @param orderCards The method in which to arrange and distribute cards
 */
class Server(val port: Int,
             val poolSize: Int,
             deck: List[Color],
             orderDestinations: Function[Set[PlacePair], List[PlacePair]] = Utils.defaultOrderDestinations,
             orderCards: Function[List[Color], List[Color]] = Utils.defaultOrderCards) extends Runnable {
  val serverSocket = new ServerSocket(port)
  val pool: ExecutorService = Executors.newFixedThreadPool(poolSize)
  implicit val constants: Constants = Constants()
  //var serverProxiesMap = Map[String, ServerProxy]()


  /**
   * Starts the Server. Server will accept sign ups until both wait periods are over or there are
   * enough players to start a tournament. Plays the tournament and prints results.
   */
  def run(): Unit = {
    var sockets = ListBuffer[(String, Socket)]()
    acceptSignUps(constants.WaitPeriod, sockets)

    if (sockets.size < constants.MinFirstWaitPlayers)
      acceptSignUps(constants.WaitPeriod, sockets)

    val clients = sockets.zipWithIndex.map{
      case ((name, socket), i) =>
        PlayerProxy(name, i, socket)
    }

    if (sockets.size <= 1) {
      clients.map(player => player.end(false))
      println("[ [], [] ]")
    } else {
      Utils.playTournament(clients.toSet, deck, Utils.sortDestinations, d => d)
    }

    pool.shutdown()
    sockets.toMap
  }

  /**
   * Waits for clients to send their name to sign up for a tournament. Accepts new sign-ups as long
   * as the maximum number of players has not been reached.
   * @param timeout the amount of time to wait before we stop accepting signups
   * @param sockets the map of sockets to add to
   */
  def acceptSignUps(timeout: Int, sockets: ListBuffer[(String, Socket)]): Unit = {
    val startTime = System.currentTimeMillis()
    serverSocket.setSoTimeout(timeout)
    val potentialClients = ListBuffer[(Socket, java.util.concurrent.Future[Option[String]])]()
    try {
      while (sockets.size < constants.MaxSignedUpPlayers) {
        val socket = serverSocket.accept()
        // update the serverSocket timeout after serverSocket.accept has finished blocking
        // so we will only wait for a total of timeout milliseconds
        serverSocket.setSoTimeout(startTime.toInt + timeout - System.currentTimeMillis().toInt)
        val result = pool.submit(new SignUp(socket))
        potentialClients.addOne((socket, result))
      }
    } catch {
      case e => {}
    }

    addSignedUpPlayers(potentialClients, sockets)

  }

  /**
   * Adds all the clients that successfully signed up by sending their name to the players that are
   * signed up for the next tournament.
   * @param potentialClients All clients that connected to the server during the waiting period
   * @param sockets The running roster of clients that are signed up for the upcoming tournament.
   *                      Mutates this list with the potentialClients that did successfully sign
   *                      up.
   */
  def addSignedUpPlayers(potentialClients: ListBuffer[(Socket, java.util.concurrent.Future[Option[String]])],
                         sockets: ListBuffer[(String, Socket)]): Unit = {
    // adds name and ServerProxy to serverProxies for each client that successfully signed up
    // in time. If a client connected but did not sign up (by sending their name), they are skipped
    potentialClients.foreach{ case (socket, result) => {
      try {
        //this errors if client did not send name
        val name = result.get.get
        var JString(nameResult) = parse(name)
        assert(1 to 50 contains nameResult.length, "invalid length of name")
        assert(nameResult.matches("[a-zA-Z]+"), "invalid character(s) in name")

        // append join index to duplicate names
        if (sockets.map(_._1) contains nameResult) {
          val indexOfNameResult = sockets.map(_._1).indexOf(nameResult)
          nameResult = nameResult + "-" + indexOfNameResult
        }
        sockets.addOne((nameResult, socket))
      } catch {
        // skip client that did not send name or sent an invalid name
        e => {}
      }
    }}
  }
}

/**
 * A callable that accepts a socket to wait for input on, anticipating
 * a Player signing up. If they sign up within constants.SignUpTimeout, Some(their name)
 * is returned. Otherwise, None is returned.
 * @param socket the socket to listen on
 */
class SignUp(val socket: Socket) extends Callable[Option[String]] {
  implicit val constants: Constants = Constants()

  override def call() =  {
    Utils.waitForInputThenReadAll(socket.getInputStream, constants.SignUpTimeout)
  }
}