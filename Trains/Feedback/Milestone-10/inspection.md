Pair: lolo

Commit: [`96a800`](https://github.ccs.neu.edu/CS4500-F21/lolo/tree/96a800719f0391a1cc37dfb18d2f1f7b7d979479) *(Multiple (or no) hashes found: `96a800`, `master`. Using `96a800` instead.)*

Score: 64/100

Grader: Darpan

18/20: accurate self eval
  - -2 for `remote-proxy-manager-referee` used to mark player proxy

48/80

1. 20/20 pts for `remote-proxy-player` implementation satisfying the player interface

2. 8/20 pts for unit tests of `remote-proxy-player`:

   - Does it come with unit tests for all methods
     (start, setup, pick, play, more, win, end)?
      - 40% for accepting you do not have the tests. It is important to have unit test along with your integration tests.

3. 20/20 pts for separating the `server` function (at least) into the following two pieces of functionality:
   - signing up enough players in at most two rounds of waiting, with a different requirement for a min number of players
   - signing up a single player: which requires three steps: connect, check name, create remote-proxy player

4. 0/20 pts for implementing `remote-proxy-manager-referee` to the manager and referee interfaces.
   - I do not think you have a `remote-manager-referee` "implement" the interface between the "manager"/"referee" and the player
