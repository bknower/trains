## Self-Evaluation Form for Milestone 8

Indicate below each bullet which file/unit takes care of each task.

The `manager` performs five completely distinct tasks, with one
closely related sub-task. Point to each of them:  

1. inform players of the beginning of the game, retrieve maps
https://github.ccs.neu.edu/CS4500-F21/lolo/blob/46ad00695a14e0dc99253c7ceee8f97fadb84184/Trains/Admin/Manager.scala#L41

2. pick a map with enough destinations
	- including the predicate that decides "enough destinations"

https://github.ccs.neu.edu/CS4500-F21/lolo/blob/46ad00695a14e0dc99253c7ceee8f97fadb84184/Trains/Admin/Manager.scala#L27-L34

3. allocating players to a bunch of games per round
https://github.ccs.neu.edu/CS4500-F21/lolo/blob/46ad00695a14e0dc99253c7ceee8f97fadb84184/Trains/Admin/Manager.scala#L50


4. run the tournament and its two major pieces of functionality:
   - run a  round of games
   - run all rounds, discover termination conditions
https://github.ccs.neu.edu/CS4500-F21/lolo/blob/46ad00695a14e0dc99253c7ceee8f97fadb84184/Trains/Admin/Manager.scala#L92
Our while loop runs all rounds of the game

5. inform survining players at the very end whether they won the tournament
https://github.ccs.neu.edu/CS4500-F21/lolo/blob/46ad00695a14e0dc99253c7ceee8f97fadb84184/Trains/Admin/Manager.scala#L121

Next point to unit tests for:

- testing the `manager` on the same inputs as the `referee`, because
  you know the outcome
https://github.ccs.neu.edu/CS4500-F21/lolo/blob/46ad00695a14e0dc99253c7ceee8f97fadb84184/Trains/Other/Tests/ManagerTest.scala#L102
https://github.ccs.neu.edu/CS4500-F21/lolo/blob/46ad00695a14e0dc99253c7ceee8f97fadb84184/Trains/Other/Tests/ManagerTest.scala#L115

Instead of using the same inputs we used on referee, we mocked the output of the referee, so we could test the overall tournament logic without caring about the games

- testing the allocation of players to the games of one round
- https://github.ccs.neu.edu/CS4500-F21/lolo/blob/46ad00695a14e0dc99253c7ceee8f97fadb84184/Trains/Other/Tests/ManagerTest.scala#L78-L100

Finally, the specification of the `cheat` strategy says "like BuyNow",
which suggests (F II) to derive (`extend`) the base class or re-use some
functionality:

- point to the cheat strategy and how it partially reusess existing code
https://github.ccs.neu.edu/CS4500-F21/lolo/blob/master/Trains/Player/Cheat.scala
Reuses DumbStrategy but does not reuse BuyNow code

- point to a unit test that makes sure the requested acquisition is impossible
https://github.ccs.neu.edu/CS4500-F21/lolo/blob/46ad00695a14e0dc99253c7ceee8f97fadb84184/Trains/Other/Tests/ManagerTest.scala#L115
Not a unit test but shows the player getting ejected for choosing an illegal connection


The ideal feedback for each of these three points is a GitHub
perma-link to the range of lines in a specific file or a collection of
files.

A lesser alternative is to specify paths to files and, if files are
longer than a laptop screen, positions within files are appropriate
responses.

You may wish to add a sentence that explains how you think the
specified code snippets answer the request.

If you did *not* realize these pieces of functionality, say so.
