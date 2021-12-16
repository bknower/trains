object XManager {
  def main(args: Array[String]): Unit = {
    // list of JValues containing the map, playerstate, and acquired
    val input = Utils.readMultipleJValues

    val board = Utils.jsonToBoard(input(0))

    val players = Utils.jsonToPlayerInstanceWithBoard(input(1), board)
    val deck = Utils.jsonToDeck(input(2))

    Utils.playTournament(players.toSet, deck, Utils.sortDestinations, d => d)

  }
}
