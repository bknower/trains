## ```class Player```
API Specified ```Player``` methods

```setup(numPlayers: Int, board: Board, rails: Int, cards: Map[Color, Int]): Unit```

```pick(destinations: Set[PlacePair]): Set[PlacePair]```

```play(playerState: PlayerState): Option[Connection]```

```more(cards: Map[Color, Int]): Unit```

```win(hasWon: Boolean): Unit```

## Game Phases:
(handled by referee in the order shown below)

1. Setup
- initialize ```RefereeState```
- on each player
  - call ```setup``` with the initial game data
  - call ```pick``` with a set of ```PlacePair```s to choose from
  - add the chosen destinations to the ```RefereeState```


2. Steady-State
- players take turns in order (acquire ``Connection`` or request cards)
- Turn loop:
  - Check if the game is over. If so, move into teardown.
  - call ```play``` on the current player
    - if the move is illegal, eject them from the game
      - if they are ejected from the game: 
        - the acquired connections are removed from the ```PublicState```
        - the cards and rails in their inventory are discarded
        - they are removed from the ```RefereeState```'s map of ```PrivateState```s
        - they are removed from the ```PublicState```
          - removed from map of acquired ```Connection```s
          - removed from list of player names
          - current turn updated to next player's name, or the empty string if no players remain
    - if the move is legal
      - if they requested more cards, the referee calls ```more``` with the new cards (if any) and removes these cards from the ```RefereeState```
      - if they acquired a connection, update the ```RefereeState``` with the connection
      - increment current turn to the next player
      - go back to beginning of turn loop

3. Teardown
- calculate scores and rankings
- call ```win``` on each player
- update observers and tournament manager with rankings
