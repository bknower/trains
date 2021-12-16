
## class Player

- ```setup(numPlayers: Int, board: Board, rails: Int, cards: Map[Color, Int]): Unit```
  - sets up this player with the basic game pieces

- ```pick(destinations: Set[PlacePair]): Set[PlacePair]```
  - asks this player to pick some destinations for the game and return those not chosen

- ```play(playerState: PlayerState): Option[Connection]```
  - grants this player a turn

- ```more(cards: Map[Color, Int]): Unit```
  - hands this player some cards

- ```win(hasWon: Boolean): Unit```
  - did this player win?


## class Referee
Calls the ```Player``` methods throughout the setup, steady state, and teardown of the game.

- ```Referee(board: Board, players: List[Player], cardCounts: Map[Color, Int])```
  - construct a referee with the given information
- ```playGame: List[Set[String]]```
  - run a full game of Trains and return the result in the form of an ordered list of rankings, where each ranking in the list is a set of players

-----------------------------------------------------

### Game Phases:

setup
- pass out initial PlayerState and 5 destinations to each player
- get 3 non-chosen destinations from each player

steady state
- players take turns in order (acquire connection or request cards)    

tear down
- calculate scores and rankings
- update observers, players, and tournament manager