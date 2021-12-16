
/**
 * Represents the public information about the state of the current game.
 * @param board the game board (renamed from Map)
 * @param acquiredConnections map of player names to set of connections that player has acquired
 * @param currentTurn name of player whose turn is in progress
 * @param playerNames list of all players in the game (in turn order)
 */
case class PublicState(board: Board, acquiredConnections: Map[String, Set[Connection]],
                       currentTurn: String, playerNames: List[String]) {

  assert((playerNames contains currentTurn) || playerNames.size == 0, "currentTurn must be a valid player")
  assert(playerNames.toSet == acquiredConnections.keySet,
    "the players in the turn order and the acquired connections map must be the same")
  assert(playerNames.toSet.size == playerNames.size, "all player names must be unique")
  
  /**
   * Returns the name of the player(s) who hold the longest route.
   * @return a set containing the names of all such players
   */
  def whoHasLongestRoute: Set[String] = {
    val ordering = acquiredConnections.toList.map{case (name, connections) =>
      // create map of player to the length of their longest path
      // we find the length of that path by creating a new board containing only the connections acquired by
      // that player and calculating the longest path there
      (name, board.copy(connections = connections).getLengthOfLongestPath)}

      // sort in order of descending score
      .sortBy(_._2).reverse

    // take all players with the highest score
    ordering.filter(_._2 == ordering(0)._2).toMap.keySet
  }

  /**
   * Check if the player has acquired the destination.
   * @param destination the PlacePair to check
   * @param playerName the player to check
   * @return true if the player has acquired the given destination
   */
  def isDestinationAcquired(destination: PlacePair, playerName: String): Boolean = {
    val helperBoard = Board(this.board.places, this.acquiredConnections(playerName), this.board.dimensions)
    helperBoard.hasPath(destination)
  }

  /**
   * Find the name of the player who will take their turn after the current player.
   * @return name of next player
   */
  def nextPlayer: String = playerNames((playerNames.indexOf(currentTurn) + 1) % playerNames.size)

}
