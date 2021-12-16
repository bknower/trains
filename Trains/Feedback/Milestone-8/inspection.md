Pair: lolo

Commit: [`46ad00`](https://github.ccs.neu.edu/CS4500-F21/lolo/tree/46ad00695a14e0dc99253c7ceee8f97fadb84184) *(Multiple hashes found: `46ad00`, `master`. Using `46ad00` instead.)*

Score: 130/155

Grader: Satwik Kumar

Score Distribution:
-------------------------------------------------
### Self Evaluation (20/20)
- (20/20) Accuracy
--------------------------------------------------
### Code Inspection (90/115)

#### Manager Inspection: Total(80/100)
- manager performs five totally distinct tasks:(existence of separate methods/functions and well-chosen names and/or purpose statements):
    - (10/10) inform players of the beginning of the tournament, retrieve maps
    - check that there are maps with enough destinations and pick one of those for the games
      - (10/10) picks the "good" map 
      - (0/10) the same predicate should be used in both the manager and the referee for checking enough destinations
    - (10/10) allocating players to a bunch of games per round
    - running the tournament, separate two functions
       - (5/15) run a round of games (No separate function)
       - (15/15) run all rounds
    - (10/10) inform survining players at the very end whether they won the tournament
- Testing the manager:
    - (10/10) testing a single game 
    - (10/10) testing the allocation of players to games per round

#### Cheat Strategy Inspection : Total(10/15)
- (8/10) proper design (derive (extend) the BuyNow class and override the turn method) (Expected to extend/reuse BuyNow)
- (2/5)  new turn decision maker should come with a unit test that makes sure that the requested acquisition is not on the map 

### Remote.md (20/20)
- (5/5) diagrams that mention for the exact same scenarios as in Logical Interactions and Logical Interactions 2 
- (5/5) JSON format definitions, for each call and return in these diagrams;
- (10/10) a "helpful" English explanation
