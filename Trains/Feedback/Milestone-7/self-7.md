## Self-Evaluation Form for Milestone 7

Please respond to the following items with

1. the item in your `todo` file that addresses the points below.

2. a link to a git commit (or set of commits) and/or git diffs the resolve
   bugs/implement rewrites: 

It is possible that you had "perfect" data definitions/interpretations
(purpose statement, unit tests, etc) and/or responded to feedback in a
timely manner. In that case, explain why you didn't have to add this
to your `todo` list.

These questions are taken from the rubric and represent some of 
critical elements of the project, though by no means all of them.

If there is anything special about any of these aspects below, you may also point to your `reworked.md` and/or `bugs.md` files. 

### Game Map 

- a proper data definition with an _interpretation_ for the game _map_
   - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Common/Board.scala
   - We didn't have to fix anything (apart from changes made before the partner swap)

### Game States 

- a proper data definition and an _interpretation_ for the player game state
   - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Common/PlayerState.scala
   - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Common/PrivateState.scala
   - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Common/PublicState.scala
   - We didn't have to fix anything (apart from changes made before the partner swap)


- a purpose statement for the "legality" functionality on states and connections
   - we're missing a purpose statement for the legality functionality

- at least _two_ unit tests for the "legality" functionality on states and connections 
   - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Other/Tests/RefereeTest.scala#L76
   - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Other/Tests/RefereeTest.scala#L82
   - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Other/Tests/RefereeStateTest.scala#L58

### Referee and Scoring a Game

The functionality for computing scores consists of 4 distinct pieces of functionality:

  - awarding players for the connections they connected
     - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Admin/RefereeState.scala#L289

  - awarding players for destinations connected
     - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Admin/RefereeState.scala#L279

  - awarding players for constructing the longest path(s)
     - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Admin/RefereeState.scala#L268
     - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Common/PublicState.scala#L21
     - Tests:
        - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Other/Tests/PublicStateTest.scala#L62-L78

  - ranking the players based on their scores 
     - https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Admin/RefereeState.scala#L248

We tested these all together instead of writing unit tests for each since we made the helper methods private. (except for whoHasLongestRoute, which we made public)
https://github.ccs.neu.edu/CS4500-F21/lolo/blob/6dae05b365c072bcfa080d25b32198564af5c6a2/Trains/Other/Tests/RefereeStateTest.scala#L133

Point to the following for each of the above: 

  - piece of functionality separated out as a method/function:
  - a unit test per functionality

### Bonus

Explain your favorite "debt removal" action via a paragraph with
supporting evidence (i.e. citations to git commit links, todo, `bug.md`
and/or `reworked.md`).

Our favorite debt removal action was the combination of allowing the referee to handle timeouts and players that throw exceptions. Before, we were not very confident in the abilities of our referee, but now we are sure that players cannot break the game in any way without the referee ejecting them. Additionally, this was the only real feedback that we had to respond to from previous milestones.

The way we fixed the timeout problem was by changing the return type of each method in the player interface to a Future of the previous type, indicating that that type might be returned at some point in the future. Then, we give the referee a maximum time to wait for the player to return a response. If they don't respond within that time, they are ejected from the game.

We fixed the problem where players could throw exceptions by wrapping any calls to the player in a function we created called ```communicateWithPlayer```, which takes in some anonymous function to call, and the name of the player in question. If any exception occurs within the function, the given player is ejected from the game.

The changes we made are in these commits:
- https://github.ccs.neu.edu/CS4500-F21/lolo/commit/423f79829cddfb1b3c18b70db904cd0072e1dedb#diff-1e4b163ea2e561fba9e553c8c47db79e51de5c2a898de51697d36921b8fb3d86L141
- https://github.ccs.neu.edu/CS4500-F21/lolo/commit/e339557b093b256fe7aea51f7e43210642028635



