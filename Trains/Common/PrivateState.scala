import java.awt.Color

/**
 * Represents the private information related to a player.
 * @param cardCounts colored cards that this player has
 * @param rails rail segments that this player has
 * @param destinations destinations chosen by this player
 */
case class PrivateState(cardCounts: Map[Color, Int], rails: Int, destinations: Set[PlacePair]) {
  implicit val constants: Constants = Constants()
  assert(0 to constants.DefaultRails contains rails, "invalid number of rails")
  assert(cardCounts.values.forall(count => 0 to constants.TotalCards contains count), "invalid number of colored cards")
  assert(cardCounts.values.sum < constants.TotalCards, "invalid total number of cards")
  assert(0 to constants.DestinationsPerPlayer contains destinations.size, "invalid number of destination cards")
}
