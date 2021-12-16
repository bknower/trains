# `Server`
- Waits for connections / signups
- When a player signs up, creates a `PlayerProxy` with the socket that they signed up over
- This `PlayerProxy` functions as a normal `Player` from the perspective of the `Referee` and `Manager`.

## `PlayerProxy`
- Implements `Player`, but also takes in a `Socket`
- When a `Player` method is called on it, it turns the method call into the JSON representation from the 
specifications and sends it over the socket.

# `Client`
- Created with a socket that has been connected to the `Server`
- Sends its name over the socket to sign up
- Creates a `ServerProxy` with some `Player` to handle the logic of playing the game.
- Runs the `ServerProxy`

## `ServerProxy`
- Inside `run`, waits for communications over the network, and interprets them as method calls
- When it gets a method call, it calls the appropriate method on the `Player`, converting the arguments of
the method call to our internal representation and converting the output from the `Player` back to JSON
- After getting the results from the `Player`, it sends them back to the actual `Server` over the network.