Pair: lolo

Commit: [`6dae05`](https://github.ccs.neu.edu/CS4500-F21/lolo/tree/6dae05b365c072bcfa080d25b32198564af5c6a2) *(Multiple hashes found: `master`, `6dae05`. Using `6dae05` instead.)*

Score: 302/290

Grader: Chukwurah Somtoo


20/20: self eval

`git log inspection`

60/60

You got full points if the answer to "Did you copy code?" appeared to be "no".

`Improved Project Code`

202/210

For each of the sections below, criteria aliases and descriptions:

| Criteria Alias         | Description  | Points |                                                    
| :--------------------- | :-------------------- | :----------------------------------------------------------------------------: | 
| A                      | the item in your `todo` file or an explanation that about why it was not in your `todo` file (you already had the functionality prior to this milestone, you fixed an issue "immediately", etc. But you can't get these points if you didn't implement the functionality.)  | 5 | 
| B                      | a link to a git commit (or set of) and/or git diffs or files that contain the functionality | 5 | 
| C                      | quick-check accuracy (1. matches 2. date of fix (2 is relevant for git commits)) | 5 | 
| D                      | quality (see below) | 15 | 

**Scores**


| Category                                  | A  | B | C | D |                                                    
| :--------------------- | :-------------------- | :----------------------------------------------------------------------------: | :--------: | :---------: | 
| Game Map                                   | 5 | 5 | 5 | 15 |
| Game States                                | 5 | 5 | 5 | 12 |
| Strategy                                   | 5 | 5 | 5 | 15 |
| Referee's Scoring (Connection Points)      | 0 | 5 | 5 | 15 |
| Referee's Scoring (Destinations Connected) | 5 | 5 | 5 | 15 |
| Referee's Scoring (Longest Path)           | 5 | 5 | 5 | 15 |
| Referee's Scoring (Ranking)                | 5 | 5 | 5 | 15 |

`Game Map Notes & Quality Criteria`
- You received a 0 for criteria "A" because if you didn't have the item in the TODO, you were required to have some explanation of why this isn't the case, something as simple as "We already did this, no need for it in the TODO". Technically you did this for all other items in the self eval but I am only giving a 0 for this one item.
- Quality
  - how are connections between two cities represented?
    - example: if you constructed a map with BOS and NYC on it, connected with 4 red segments, how would the data representation express this?
  - is every connection between two cities represented once or twice?
    - example: if you have a 3-blue BOS--NYC connection, does it show up once or twice?
  - does the data representation say how to translate it into a graphic layout?
    - example: how large is the map? Where would NYC show up (in the above example)?

`Game States Notes & Quality Criteria`
- Quality
  - a proper data definition including an interpretation for the player game state
  - a purpose statement for the "legality" functionality on player game states and connections
  - two unit tests for the "legality" functionality on states and connections
     - Missing purpose statement (2/5)

`Strategy Notes & Quality Criteria`
- Quality
  - do the purpose statements of the strategies' methods/functions express how it makes decisions?
  - are common pieces abstracted now?

`Referee Notes & Quality Criteria`
- Quality
  - separate method; clear naming and/or good purpose statement; unit tests
    - For each, 5 pts
    - Didn't mention anything about todo so lost 5 points but, this was only taken off once.

`Bonus`

20/20

- 10/10 for quick-check accuracy (1. matches 2: date or fix)
- 10/10 for quality if it is convincing that your "favorite debt removal action" removed a critical technical debt
