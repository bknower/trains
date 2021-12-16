import java.net.Socket

/**
 * Signs up for a game of Trains with the server by sending name. Creates a ServerProxy to handle
 * all remote communications & method calls on the player
 * @param socket the socket to communicate over
 * @param player the player that this client is representing
 */
class Client(val socket: Socket, player: Player) extends Runnable {
  def run() = {
    val serverProxy = new ServerProxy(player, socket)
    
    // sign up with the name
    Console.withOut(socket.getOutputStream) {
      println("\"" + player.name + "\"")
    }
    
    serverProxy.run
    socket.close()
  }
}