import Utils.updateMap
import scala.util.Random
import java.awt.Color

/**
 * Represents the information privately owned by the referee for this game.
 *
 * @param publicState   the public information available to anyone related to the game
 * @param privateStates a map of each player's name to the state available to them
 * @param cards    the remaining colored cards that the referee can pass out to players
 * @param firstPlayerFinalRound   the name of the first player in the final round (none if not the final round yet)
 * @param firstPlayerNoCards      the name of the first player who requested cards and was not given any (or None if
 *                                this hasn't happened yet or a legal move was made since the last time it happened)
 */
case class RefereeState(publicState: PublicState,
                        privateStates: Map[String, PrivateState],
                        cards: List[Color],
                        orderCards: Function[List[Color], List[Color]] = Utils.defaultOrderCards,
                        firstPlayerFinalRound: Option[String] = None,
                        firstPlayerNoCards: Option[String] = None) {
  assert(publicState.playerNames.toSet == privateStates.keySet, "All players must have a playerState, and there can be no " +
    "PlayerStates with nonexistent players.")

  /**
   * Construct the PlayerState for the current player from the their PrivateState and the PublicState available to
   * anyone in the game.
   *
   * @return the PlayerState of currently active player
   */
  def getCurrentPlayerState: PlayerState = PlayerState(publicState, privateStates(publicState.currentTurn))

  /**
   * Check if the player whose turn is in progress can acquire the given connection.
   *
   * @param connection the connection to check
   * @return true if the player can has the resources to acquire it and it hasn't been acquired yet (and it is on the board
   */
  def canCurrentPlayerAcquire(connection: Connection): Boolean =
    (publicState.board.connections contains connection) && getCurrentPlayerState.canAcquire(connection)

  /**
   * Get a new RefereeState with updated PlayerStates after the given destinations have been
   * chosen by the given player.
   *
   * @param destinations the 2 destinations that the given player chose
   * @param player the name of the player who chose the given destinations
   * @return the updated RefereeState if the player can acquire the connection, or the current state otherwise
   */
  def chooseDestinations(destinations: Set[PlacePair], player: String): RefereeState = {
    assert(destinations.size == 2)
    // update the resources of the given player after they pick 2 destinations
    val newPrivateStates: Map[String, PrivateState] = updateMap(privateStates, player,
      (state) => state.copy(destinations = destinations))


    this.copy(privateStates = newPrivateStates)
  }

  /**
   * Get a new RefereeState with updated PublicState and PlayerStates after the given connection has been
   * acquired by the current player.
   *
   * @param connection the connection to be acquired
   * @return the updated RefereeState if the player can acquire the connection, or the current state otherwise
   */
  def acquireConnection(connection: Connection): RefereeState = {
    if (canCurrentPlayerAcquire(connection)) {

      // update the resources of the current player after they acquire the given connection
      val newPrivateStates: Map[String, PrivateState] = updateMap(privateStates, publicState.currentTurn,
        (state) => privateStateAcquireConnection(connection))

      // get the updated public state with an advanced turn and an acquired connection for the current player
      val newPublicState = publicStateAdvanceTurn(publicStateAcquireConnection(connection))

      checkFinalRound.copy(publicState = newPublicState, privateStates = newPrivateStates, firstPlayerNoCards = None)
    } else this.ejectPlayer()
  }

  /**
   * If the current player's rails drop below 3 and the final round has not already started,
   * track the first player who took their turn in the final round.
   * @return updated referee state
   */
  private def checkFinalRound: RefereeState = {
    if (firstPlayerFinalRound != None && privateStates(publicState.currentTurn).rails < 3) {
      this.copy(firstPlayerFinalRound = Some(publicState.currentTurn))
    } else this
  }

  /**
   * Get a new RefereeState with updated PublicState, PlayerStates, and CardCounts after the current player requests cards
   *
   * @return the updated RefereeState, or same state if there are no cards left
   */
  def requestCards: RefereeState = {
    if (cards.size >= 2) {
      // randomly select 2 colored cards
      val givenCards = orderCards(cards).take(2).toList

      // subtract 1 from ref card counts for each color that will be handed to the player
      val newCards = orderCards(cards).slice(2, cards.size)

      // update the resources of the current player after they receive new cards
      val newPrivateStates: Map[String, PrivateState] = updateMap(privateStates, publicState.currentTurn,
        (state) => privateStateAddCards(givenCards))

      // get the updated public state with an advanced turn
      val newPublicState = publicStateAdvanceTurn(publicState)

      this.copy(publicState = newPublicState, privateStates = newPrivateStates, cards = newCards, firstPlayerNoCards = None)
    } else {
        this.copy(publicState = publicStateAdvanceTurn(publicState), firstPlayerNoCards = if (firstPlayerNoCards.isEmpty) Some(publicState.currentTurn) else firstPlayerNoCards)
    }
  }


  /**
   * Creates a new RefereeState in which the current player is ejected from the game.
   * When we eject the player, we have to:
   * - remove their entry from privateStates
   * - remove them from the public state
   * @return the updated RefereeState
   */
  def ejectPlayer(name: String = publicState.currentTurn): RefereeState = {
    val newPublicState = publicStateEjectPlayer(name)

    val currentPrivateState = privateStates(name)

    this.copy(publicState = newPublicState, privateStates = privateStates - name)
  }

  /**
   * Creates a new PublicState with the current player removed.
   * When we eject a player from the PublicState, we have to:
   * - remove them from the map of acquired connections
   * - remove them from the list of player names
   * - update the current turn to the next player, if there is one, else it is set to an empty string
   * @return the updated PublicState
   */
  private def publicStateEjectPlayer(name: String) = {
    val nextTurn = if (publicState.playerNames.size <= 1) "" else publicState.nextPlayer

    publicState.copy(
      acquiredConnections = publicState.acquiredConnections - name,
      playerNames = publicState.playerNames diff List(name),
      currentTurn = if (publicState.currentTurn == name) nextTurn else publicState.currentTurn
    )
  }

  /**
   * Determines whether the game is over.
   * @return true if it is the last round and it is the turn of the player who went first
   *         in that round, or all players have requested cards and received none,
   *         or all players have been ejected from the game.
   */
  def isGameOver: Boolean = {
    val currentTurn = publicState.currentTurn
    publicState.playerNames.isEmpty ||
      (!firstPlayerFinalRound.isEmpty && (firstPlayerFinalRound.get == currentTurn)) ||
      (!firstPlayerNoCards.isEmpty && (firstPlayerNoCards.get == currentTurn))
  }

  /**
   * Get a new PublicState with the current turn advanced to the next player, according to the
   * specified turn ordering.
   *
   * @param publicState the public state to update with the new currentTurn
   * @return the state with the changed turn
   */
  private def publicStateAdvanceTurn(publicState: PublicState): PublicState =
    publicState.copy(currentTurn = publicState.nextPlayer)

  /** Gets a new PublicState, with the given player having acquired the given connection
   *
   * @param playerName the player to acquire the connection
   * @param connection the connection to acquire
   * @return the new PublicState
   */
  private def publicStateAcquireConnection(connection: Connection): PublicState = {
    // add the connection in the acquired connections map to the set of the current player's connections
    val newAcquired = updateMap(publicState.acquiredConnections, publicState.currentTurn,
      (connections) => connections + connection)

    publicState.copy(acquiredConnections = newAcquired)
  }

  /**
   * Gets the new state of the current player with updated resources after they acquire a connection.
   *
   * @param connection the connection to be acquired
   * @return the new PrivateState
   */
  private def privateStateAcquireConnection(connection: Connection): PrivateState = {
    val currPlayer = privateStates(publicState.currentTurn)

    // update the card counts by subtracting a number of cards equal to the connection length
    val newCounts = updateMap(currPlayer.cardCounts, connection.color, (count) => count - connection.length)
    currPlayer.copy(rails = currPlayer.rails - connection.length, cardCounts = newCounts)
  }

  /**
   * Get a new PlayerState for the current player with updated colored cards after they request more from the referee.
   *
   * @param cards the two cards to be added to the current player's resources
   * @return the new PrivateState
   */
  private def privateStateAddCards(cards: List[Color]): PrivateState = {
    val currPlayer = privateStates(publicState.currentTurn)

    // fold over the list of cards to add to the cardCounts, updating the count map
    // each time by adding 1 to the color of the current card
    val newCounts = cards.foldRight(currPlayer.cardCounts)((color, counts) =>
      updateMap(counts, color, (count) => count + 1))

    currPlayer.copy(cardCounts = newCounts)
  }


  /**
   * Sorts the players by score and returns the names of all players who share the highest score.
   * @return the set of names of the winning players
   */
  def getWinners: Set[String] = {
    assert(isGameOver, "no winners before the game ends")
    val ranking = getRanking
    if (ranking.isEmpty) Set() else ranking(0)
  }

  /**
   * Get the ranking as a list of sets of strings, with each set containing the players tied in that place in the
   * rankings
   * @return the list of sets
   */
  def getRanking: List[Set[String]] = {
    val sortedRanking = getScores.toList.sortBy(_._2)
      .foldRight(Map[Int, Set[String]]())((entry, currMap) => entry match {
        case (player, score) => {
          if (currMap.contains(score)) {
            updateMap(currMap, score, currEntry => currEntry + player)
          } else currMap + (score -> Set(player))
        }
      })
    sortedRanking.toList.sortBy(_._1).map(_._2).reverse
  }

  /**
   * Get the current scores of all players in the game.
   * @return a hashmap from player name to their current score
   */
  def getScores: Map[String, Int] = {
    // the current score is the count of acquired segments plus 20 if that player holds a longest route
    // plus 10 for each acquired destination and minus 10 for each unaquired destination
    publicState.playerNames.map(name => {
      val longestRouteBonus = if (publicState.whoHasLongestRoute contains name) 20 else 0
      (name, (getAcquiredScore(name) + longestRouteBonus + getDestinationScore(name)))
    }).toMap
  }

  /**
   * Calculates the destination score for the player with the given name by adding 10 points for every destination they
   * have acquired and subtracting 10 points for every destination they haven't acquired.
   * @param name the player to check the score of
   * @return the player's destination score
   */
  def getDestinationScore(name: String): Int = {
    privateStates(name).destinations.foldRight(0)((destination, sum) =>
      if (publicState.isDestinationAcquired(destination, name)) sum + 10 else sum - 10)
  }

  /**
   * Sums up the lengths of all of the connections acquired by the player with the given name
   * @param name the name of the player
   * @return the acquired connection score
   */
  def getAcquiredScore(name: String): Int = {
    publicState.acquiredConnections(name).toList.map(_.length).sum
  }

}