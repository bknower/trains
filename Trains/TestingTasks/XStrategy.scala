object XStrategy {
  def main(args: Array[String]): Unit = {
    // list of JValues containing the map, playerstate, and acquired
    val input = Utils.readMultipleJValues

    val board = Utils.jsonToBoard(input(0))

    val playerState = Utils.jsonToPlayerState(input(1), board)

    val turn = Hold10.takeTurn(playerState)

    turn match {
      case Some(connection) => println(Utils.connectionToAcquired(connection))
      case None => println("\"more cards\"")
    }
  }

}
