import java.net.{InetSocketAddress, ServerSocket, Socket, SocketTimeoutException}

/** Singleton object to run TCP program. */
object Xtcp {

  /** Entry point for the program
   *
   * @param args command line arguments
   *
   *             expects 1 integer command line argument between 2048 and 63555
   */
  def main(args: Array[String]): Unit =
    // catches exception if connection times out
    try {
      val serverSocket = getServerSocket(args(0).toInt)

      // get client socket to send and receive input with
      val socket = serverSocket.accept()

      // run xjson with input and output streams replaced by the socket's input and output streams
      Console.withIn(socket.getInputStream()) {
        Console.withOut(socket.getOutputStream()) {
          XJson.main(args)
        }
      }

      // close sockets before finishing
      socket.close()
      serverSocket.close()
    } catch {
      case e: SocketTimeoutException => println("Connection timed out.")
      case e: ArrayIndexOutOfBoundsException => println("Please enter a port.")
      case e: AssertionError => println("Invalid Port.")
    }

    /**
     * Sets up a server socket at the given port, making sure that the port is valid
     *
     * @param port the port to listen for connections on
     * @return the set-up server socket
     */
    def getServerSocket(port: Int): ServerSocket =
      // validate port
      val port = args(0).toInt
      assert(2048 to 63555 contains port, "Invalid port.")

      // start TCP server and set timeout to 3 seconds
      val serverSocket = new ServerSocket(port)
      serverSocket.setSoTimeout(3000)

      return serverSocket
}