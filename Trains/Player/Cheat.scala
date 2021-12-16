import BuyNow.constants
import java.awt.Color

object Cheat extends DumbStrategy {

  /**
   * Choose the first DestinationsPerPlayer destinations from the reversed lexicographic ordering.
   * @param privateState the PrivateState to consider
   * @param destinations the destinations to choose from
   *  @return the chosen destinations
   */
  override def chooseDestinations(privateState: PrivateState, board: Board, destinations: Set[PlacePair]): Set[PlacePair] = {
    assert(privateState.destinations.size == 0, "destinations already chosen")
    Utils.sortDestinations(destinations).takeRight(constants.DestinationsPerPlayer).toSet
  }

  /**
   * Makes a decision on how to play out a turn based on the chooseConnection and shouldAcquireConnection methods.
   *
   * @param playerState the PlayerState to consider
   * @return None if the action for this turn was chosen to pick more cards, or the connection to acquire if
   */
  override def takeTurn(playerState: PlayerState): Option[Connection] = {
    val p1 = Place(System.currentTimeMillis().toString, playerState.publicState.board.dimensions)
    val p2 = Place(System.currentTimeMillis().toString + "2", playerState.publicState.board.dimensions)
    Some(Connection(PlacePair(p1, p2), Color.RED, 4))
  }
}
