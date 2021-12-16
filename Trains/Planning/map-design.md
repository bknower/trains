## Board Case Class
A Board is the central game piece of the Trains game. It stores the places and connections for a game.

Fields:
- ```Set[Place]```
  - The coordinates of the places must be between 0 and the dimensions of the map
- ```Set[Connection]```
  - No connections between the same two places may share a color
  - Connections must be formed between two places in the set of places
- ```Posn dimensions```

Methods:
- ```getPlaceNames(): List[String]```
  - returns an alphabetized list of all places on the map. Using a list allows for duplicate place names
- ```getAllDestinations(): Set[Destination]```
  - returns a set of all feasible destinations on the map. Destination A->B is considered equal to Destination B->A
- ```getConnectedPlaces(Place): Set[Place]```
  - returns a set of all places that a given place is connected to
- ```hasConnection(Place, Place): Boolean```
  - returns true if a direct connection exists between the two places

## Posn Case Class
A Posn represents a non-negative X,Y coordinate on the map, counting in pixels down and right from the origin in the top left.

Fields:
- ```Int x```
- ```Int y```

## Place Case Class
A Place represents a city on the map.

Fields:
- ```String name```
- ```Posn posn```

## Connection Case Class
A Connection represents a direct connection between two Places.

Fields:
- ```Destination destination```
  - The two places that a connection is between form a destination
- ```java.awt.Color color```
  - Must be one of Red, Blue, Green, or White
- ```Int length```
  - Must be 3, 4, or 5

## PlacePair Case Class
A PlacePair contains two places. 

Fields:
- ```Set[Place]```
  - Must contain exactly two places