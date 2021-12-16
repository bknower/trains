## PlayerManagerInterface

construct a player
- ```Player(name: String, filePath: String)```
  - name uniqueness is handled by the server when players register

```beginTournament: Board```
- called on the player at the beginning of the tournament, and the player responds with a Board

tournament results
- ```winTournament(rankings: List[String])```
  - list of players from first to last


## class TournamentManager

construct tournament manager
- ```TournamentManager(players: Set[Player], matchmaker: Function[Set[Player], Set[Set[Player]]])```
  - in the constructor, all players are created using their name and the file path to their strategy
  - the matchmaker function will determine which sets of players will play in the same games

plays out a game with all the given players. This will create a referee to supervise the game, and
  return an ordered list of sets of player names at each rank, and a set of any player names who cheated during the game.
- ```playGame(board: Board, players: List[Player], cardCounts: Map[Color, Int]):  (ranking: List[Set[String]], cheaters: Set[String])```

play out the all games tournament according to the matchmaker function and 
return a ranked list of all players who competed in the tournament, and a set of the players who cheated during the 
tournament
- ```playTournament: (ranking: List[Set[String]], cheaters: Set[String])```

inform tournament observers of on-going actions
- ```updateObservers: Unit```

inform players of tournament outcome (their ranking)
- ```tournamentOutcome(rank: Int)```

eject player from tournament
- ```ejectPlayer(player: String)```