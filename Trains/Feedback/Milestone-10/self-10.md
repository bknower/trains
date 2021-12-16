## Self-Evaluation Form for Milestone 10

Indicate below each bullet which file/unit takes care of each task.

The `remote proxy patterns` and `server-client` implementation calls for several
different design-implementation tasks. Point to each of the following: 

1. the implementation of the `remote-proxy-player`

	With one sentence explain how it satisfies the player interface. 
		- https://github.ccs.neu.edu/CS4500-F21/lolo/blob/96a800719f0391a1cc37dfb18d2f1f7b7d979479/Trains/Remote/PlayerProxy.scala#L22
	
	Our `PlayerProxy` implements the `Player` trait, with each method delegating to the `PlayerProxy`'s `ServerProxy` to send a JSON method call over the socket 
	to the remote player.

2. the unit tests for the `remote-proxy-player` 
	- we do not unit test our `PlayerProxy` Class


3. the `server` and especially the following two pieces of factored-out
   functionality: 
   
   -[Server](https://github.ccs.neu.edu/CS4500-F21/lolo/blob/96a800719f0391a1cc37dfb18d2f1f7b7d979479/Trains/Remote/Server.scala)

   - [signing up enough players in at most two rounds of waiting](https://github.ccs.neu.edu/CS4500-F21/lolo/blob/96a800719f0391a1cc37dfb18d2f1f7b7d979479/Trains/Remote/Server.scala#L67) 
   - [signing up a single player (connect, check name, create proxy)](https://github.ccs.neu.edu/CS4500-F21/lolo/blob/96a800719f0391a1cc37dfb18d2f1f7b7d979479/Trains/Remote/Server.scala#L128)



4. the `remote-proxy-manager-referee`
This is part of our ```PlayerProxy``` class here: https://github.ccs.neu.edu/CS4500-F21/lolo/blob/96a800719f0391a1cc37dfb18d2f1f7b7d979479/Trains/Remote/PlayerProxy.scala#L22

	With one sentence, explain how it deals with all calls from the manager and referee on the server side.  
	
	Because `PlayerProxy` implements `Player`, the `Manager` and `Referee` are able to act on the `PlayerProxy` as they normally would with any other type of `Player`.




The ideal feedback for each of these three points is a GitHub
perma-link to the range of lines in a specific file or a collection of
files.

A lesser alternative is to specify paths to files and, if files are
longer than a laptop screen, positions within files are appropriate
responses.

You may wish to add a sentence that explains how you think the
specified code snippets answer the request.

If you did *not* realize these pieces of functionality, say so.

