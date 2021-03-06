- "Need to break up score calculation with separate functions per task. (-5)"
  - refactored calculation of players scores into multiple methods
  - https://github.ccs.neu.edu/CS4500-F21/lolo/commit/423f79829cddfb1b3c18b70db904cd0072e1dedb#diff-1e4b163ea2e561fba9e553c8c47db79e51de5c2a898de51697d36921b8fb3d86L141
  - Trains/Admin/RefereeState.scala
- "No tests for score calculation. (-18)"
  - added tests for score calculation
  - https://github.ccs.neu.edu/CS4500-F21/lolo/commit/dabd6ee9ba067fc14ca4040a459355f31b15823b
- added a Player trait and renamed Player class to AIPlayer, since players might work differently over the network
  - https://github.ccs.neu.edu/CS4500-F21/lolo/commit/423f79829cddfb1b3c18b70db904cd0072e1dedb#diff-1e4b163ea2e561fba9e553c8c47db79e51de5c2a898de51697d36921b8fb3d86L141
  - Trains/Player/Player.scala
  - Trains/Player/AIPlayer.scala
- moved JSON parsing to Utils
  - https://github.ccs.neu.edu/CS4500-F21/lolo/commit/0e2648dbee327bbd6ce322e6c7d21a8c7acc492a
- moved various numbers into constants
  - https://github.ccs.neu.edu/CS4500-F21/lolo/commit/8c6714e047e162d16e35624f9e771d5ae7f2efbf
- "Does not handle abnormally behaving players that, for example, don't respond in a reasonable amount of time or throw an exception. (-10)"
  - referee will just wait for a player to respond forever
    - we added a max time to wait for a player's response
    - https://github.ccs.neu.edu/CS4500-F21/lolo/commit/423f79829cddfb1b3c18b70db904cd0072e1dedb#diff-1e4b163ea2e561fba9e553c8c47db79e51de5c2a898de51697d36921b8fb3d86L141
    - Trains/Admin/Referee.scala
    - Test in Trains/Other/Tests/RefereeTest.scala with TimeoutStrategy
  - referee will crash if the player's code throws an exception
      - we wrapped all calls to the player API in a try catch that will eject the player if any exception happens
      - https://github.ccs.neu.edu/CS4500-F21/lolo/commit/e339557b093b256fe7aea51f7e43210642028635
