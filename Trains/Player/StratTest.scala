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
    Utils.sortDestinations(destinations).reverse
      // take the last 2 destinations from this ordering
      .take(constants.DestinationsPerPlayer).toSet
  }

}


///**
// * Implements the Buy-Now Strategy described on the course website.
// */
//object StratTest extends Strategy {
//
//  override def takeTurn(playerState: PlayerState): Option[Connection] =
//    Some(Connection(PlacePair(Place("1", Posn(100, 100)), Place("2", Posn(200, 100))), java.awt.Color.RED, 4))
//
//  /**
//   * Choose the first two destinations from the reversed lexicographic ordering.
//   * @param privateState the PrivateState to consider
//   * @param destinations the destinations to choose from
//   *  @return the chosen destinations
//   */
//  override def chooseDestinations(privateState: PrivateState, board: Board, destinations: Set[PlacePair]): Set[PlacePair] = {
//    assert(privateState.destinations.size == 0, "destinations already chosen")
//    Utils.sortDestinations(destinations).reverse
//      // take the last 2 destinations from this ordering
//      .take(2).toSet
//  }
//
//}
