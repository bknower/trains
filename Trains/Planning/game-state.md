## ```case class PublicState```
Represents the public information about the current game state.

Fields:
- ```val board: Board```
- ```val acquiredConnections: Map[String, Set[Connection]]``` - map from player name to set of connections they have acquired
- ```val currentTurn: String``` - the name of the ```Player``` whose turn it currently is
- ```val playerNames: Set[String]```
- ```val turnMap: Map[String, String]``` - map of player name to the name of the player who goes after them

Methods:
- ```getScores: Map[String->Int]```
  - What are the current scores of all players in the game?
- ```whoHasLongestRoute: Set[String]``` 
  - Which ```Player```(s) hold the longest route?
- ```isDestinationAcquired(PlacePair, String): Boolean```
  - Has the given ```Player``` acquired a path between the places in the given ```PlacePair```?

## ```case class RefereeState```
Represents the information that only the Referee has access to in the game. 

Fields:
- ```publicState: PublicState```
- ```playerStates: Map[String, PlayerState]``` - map from player name to the ```PlayerState``` available to them
- ```cardCounts: Map[java.awt.Color, Int]``` - each count is in [0-200]
  - remaining cards to be passed out

Methods:
- ```getPlayerState(String): PlayerState```
  - What is the ```PlayerState``` of the player with the given name?
- ```canCurrentPlayerAcquire(Connection): Boolean``` 
  - Can the player whose turn is in progress acquire the given connection?
- ```acquireConnection(Connection): RefereeState```
  - What is the new ```RefereeState``` after the player whose turn is in progress acquires the given connection?
- ```requestCards: RefereeState```
  - What is the new ```RefereeState``` after the player whose turn is in progress requests cards?

## ```case class PlayerState```
Represents the information that a specific ```Player``` and the referee may access
related to that specific player's game pieces.

Fields:
- ```publicState: PublicState```
- ```cardCounts: Map[java.awt.Color, Int]``` - each count is in [0-200]
- ```rails: Int``` [0-45]
- ```destinations: Set[PlacePair]``` - must be a set of 2 ```PlacePair```s
- ```player: Player``` - the player associated with this state

Methods:
- ```canAcquire(Connection): Boolean```
  - Can the player associated with this state acquire the given ```Connection```?
- ```getAcquirableConnections: Set[Connection]```
  - What is the set of all ```Connection```s that the player associated with this state can acquire?

## ```case class Player```
Represents an internal player in a game.
- ```name: String``` - the unique name of the player (uniqueness enforced by the class that creates the player instance)
