
/**
 * Implements the Hold-10 Strategy described on the course website.
 */
object Hold10 extends DumbStrategy {

  /**
   * Choose the first DestinationsPerPlayer destinations from the lexicographic ordering.
   * @param privateState the PrivateState to consider
   * @param destinations the destinations to choose from
   * @return the chosen destinations
   */
  override def chooseDestinations(privateState: PrivateState, board: Board, destinations: Set[PlacePair]): Set[PlacePair] = {
    assert(privateState.destinations.size == 0, "destinations already chosen")
    Utils.sortDestinations(destinations)
      // take the first DestinationsPerPlayer destinations from this ordering
      .take(constants.DestinationsPerPlayer).toSet
  }

  /**
   * Decides to acquire a connection if the player has more than 10 colored cards and some connection can be acquired.
   * Otherwise, it decides to request cards.
   * @param playerState the PlayerState to consider
   *  @return true if the Strategy decides to acquire a connection, false if it decides to request cards
   */
  override def shouldAcquireConnection(playerState: PlayerState): Boolean =
    super.shouldAcquireConnection(playerState) && Utils.countAllCards(playerState.privateState.cardCounts) > 10

}
