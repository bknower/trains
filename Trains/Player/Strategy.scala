import Utils.*

/**
 * The logical interface for what a Strategy must be able to do
 */
trait Strategy {
  /**
   * Given a set of destinations and a PlayerState, return the DestinationsPerPlayer chosen destinations.
   * @param privateState the PrivateState to consider
   * @param board the board for this game
   * @param destinations the destinations to choose from
   * @return the DestinationsPerPlayer chosen destinations
   */
  def chooseDestinations(privateState: PrivateState, board: Board, destinations: Set[PlacePair]): Set[PlacePair]


  /**
   * Makes a decision on how to play out a turn based on the given PlayerState.
   * @param playerState the PlayerState to consider
   * @return None if the action for this turn was chosen to pick more cards, or the Connection to acquire
   *         if the action was chosen to be acquiring a connection.
   */
  def takeTurn(playerState: PlayerState): Option[Connection]
}


/**
 * Trait that abstracts out the shared functionality between the 2 dumb strategies defined in the
 * assignment.
 */
trait DumbStrategy extends Strategy {
  implicit val constants: Constants = Constants()

  /**
   * By default, decides to acquire a connection if the player has enough resources to acquire any connection.
   * Otherwise, it decides to request cards.
   *
   * @param playerState the PlayerState to consider
   * @return true if the Strategy decides to acquire a connection, false if it decides to request cards
   */
  protected def shouldAcquireConnection(playerState: PlayerState): Boolean = {
    playerState.publicState.board.connections.exists(playerState.canAcquire)
  }

  /**
   * Default implementation sorts the connections according to the lexicographic ordering defined on the course website
   * and takes the first connection in the ordering.
   *
   * @param playerState the PlayerState to consider
   * @return the chosen Connection
   */
  private def chooseConnection(playerState: PlayerState): Connection = {
    assert(shouldAcquireConnection(playerState), "can't choose a connection if we didn't decide to acquire one")
    //println(playerState.publicState.board.connections)
    Utils.sortConnections(playerState.publicState.board.connections.filter(playerState.canAcquire))(0)
  }

  /**
   * Makes a decision on how to play out a turn based on the chooseConnection and shouldAcquireConnection methods.
   * @param playerState the PlayerState to consider
   * @return None if the action for this turn was chosen to pick more cards, or the connection to acquire if
   */
  def takeTurn(playerState: PlayerState): Option[Connection] = {
    if (shouldAcquireConnection(playerState))
      Some(chooseConnection(playerState))
    else
      None
  }

}


