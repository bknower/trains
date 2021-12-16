/**
 * Runs the XServer test harness by reading in the input deck and creating a server
 * to run tournaments with that deck.
 */
object XServer {
  implicit val constants: Constants = Constants()

  def main(args: Array[String]): Unit = {
    val port = args(0).toInt
    val input = Utils.readMultipleJValues

    val deck = Utils.jsonToDeck(input(2))

    val server = new Server(
      port,
      constants.ServerThreadPoolSize,
      deck,
      Utils.sortDestinations,
      d => d)
    server.run()
  }
}
