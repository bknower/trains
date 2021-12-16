- Added chooseBoard parameter to Player and AIPlayer [here](https://github.ccs.neu.edu/CS4500-F21/lolo/commit/0c19c1127921946849f1d14e69b1f51cb754529d)
  - File: AIPlayer, Line: 18; File: Player, Line: 8
  - abstracts the behavior of submitting a board to the tournament manager
- Changed Referee representation of deck from `Map[Color, Int]` to `List[Color]` [here](https://github.ccs.neu.edu/CS4500-F21/lolo/commit/f07b3119bd0b0be1318ec1aad9e17e7289b085bc)
  - File: RefereeState, Line 17
  - Could not maintain order in the Map representation and had to repeatedly convert back and forth between List and Map

- Following changes on all occur on [this commit](https://github.ccs.neu.edu/CS4500-F21/lolo/commit/b1a4215b42bab85b6a95ace553d398fa9cdac425)

  - Updated criteria for what a valid board is for a tournament 
    - File: Manager, Line: 30
    - Logic was incorrect in previous version

  - Abstracted board validity check
    - File: Utils, Line: 493
    - Were previously repeating the same check in multiple places with duplicated code

  - Fixed `getAcquiredScore` method
    - File: RefereeState, Line 276
    - Was not correctly calculating the sum of all segments in each player's acquired connections.

- Fixed Manager to terminate tournament under the correct conditions [here](https://github.ccs.neu.edu/CS4500-F21/lolo/commit/60ba7ae6985f26e2482a3e3900d716de7a69deeb)
  - Manager was running an extra round of tournament when should've been terminating 
- Changed method signature of `setup` to not include unused `numPlayers` argument [here](https://github.ccs.neu.edu/CS4500-F21/lolo/commit/1f610fd57e9911b7c5321c140cf0224edc2448b1)
  - `numPlayers` was never being used. 
