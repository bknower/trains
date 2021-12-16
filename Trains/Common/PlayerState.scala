import java.awt.Color

/**
 * Represents the  private information related to a specific Player and the public information related to the game.
 * @param publicState the public information about the game
 * @param privateState the private information about the game available to this player
 */
case class PlayerState(publicState: PublicState, privateState: PrivateState) {
  

  /**
   * Check if this Player can acquire the given connection.
   * @param connection the connection to check
   * @return true if this player has enough resources to acquire the connection and it has not been acquired yet
   */
  def canAcquire(connection: Connection): Boolean = {
    privateState.cardCounts.getOrElse(connection.color, 0) >= connection.length && 
      privateState.rails >= connection.length &&
      !(publicState.acquiredConnections.values.flatten.toSet contains connection)
  }

  /**
   * Get a set of all connections that this player is able to acquire still.
   * @return the set of connections
   */
  def getAcquirableConnections: Set[Connection] = publicState.board.connections.filter(canAcquire)
}