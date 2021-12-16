/**
 * Singleton object to run the XLegal program.
 */
object XLegal {
  def main(args: Array[String]): Unit = {

    // list of JValues containing the map, playerstate, and acquired
    val input = Utils.readMultipleJValues

    // convert jsonMap to a Map
    val board = Utils.jsonToBoard(input(0))

    val playerState = Utils.jsonToPlayerState(input(1), board)

    val connection = Utils.acquiredToConnection(input(2), board)

    // print out true if the map has a connection between the two given places
    println(playerState.canAcquire(connection))
  }

}
