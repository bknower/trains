/**
 * Implements the Buy-Now Strategy described on the course website.
 */
object StratTest extends DumbStrategy {

  /**
   * Choose the first two destinations from the reversed lexicographic ordering.
   * @param privateState the PrivateState to consider
   * @param destinations the destinations to choose from
   *  @return the chosen destinations
   */
  override def chooseDestinations(privateState: PrivateState, board: Board, destinations: Set[PlacePair]): Set[PlacePair] = {
    assert(privateState.destinations.size == 0, "destinations already chosen")
    Utils.sortDestinations(destinations).takeRight(2).toSet
  }

}