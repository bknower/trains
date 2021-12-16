object XRef {
  def main(args: Array[String]): Unit = {
    // list of JValues containing the map, playerstate, and acquired
    val input = Utils.readMultipleJValues

    val board = Utils.jsonToBoard(input(0))

    val players = Utils.jsonToPlayerInstance(input(1))
    val deck = Utils.jsonToDeck(input(2))

    try {
      val ref = new Referee(board, players, deck, Utils.sortDestinations, d => d)
      val result = ref.playGame
      result match {
        case (ranking, cheaters) => println(Utils.convertRankings(ranking, cheaters))
      }

    } catch {
      case e => println("\"error: not enough destinations\"")
    }
  }
}
