import java.awt.Color
import scala.collection.mutable


/** Note: Scala automatically creates getters for all fields of case classes. Case classes are immutable,
 * so we are fine with exposing all fields. */

/**
 * A Board is the central game piece of the Trains game. It stores the places and connections for a game.
 * @param places the set of places in this map
 * @param connections the set of connections between places on this map
 * @param dimensions the height and width of the map
 */
case class Board(places: Set[Place], connections: Set[Connection], dimensions: Posn) {
  val MinBoardSize = 10
  val MaxBoardSize = 800
  assert((MinBoardSize to MaxBoardSize contains dimensions.x) && (MinBoardSize to MaxBoardSize contains dimensions.y),
    "invalid dimensions")

  places.foreach(place =>
    assert((0 to dimensions.x contains place.posn.x)
      && (0 to dimensions.y contains place.posn.y), "place out of bounds of map"))

  assert(places.map(_.name).size == places.size, "city names must be unique")
  assert(places.map(_.posn).size == places.size, "city locations must be unique")

  connections.foreach(connection => assert(connection.placePair.places subsetOf places,
    "all connections must contain only places within the given set of places"))

  // iterate over every pair of places
  connections.map(connection =>
    // filter for all connections between this pair of places
    connections.filter(pair => pair.placePair.places == connection.placePair.places).
      // get the color of each connection between these places
      toList.map(_.color)).
    // ensure that there are no duplicate colors for this pair of places
    foreach(colors => assert(colors.distinct.size == colors.size,
      "no connections between the same two places may share a color"))

  /**
   * Get the place with the given name.
   * @param name name of place that we are looking for
   * @return the place with given name, if it exists
   */
  def getPlace(name: String): Option[Place] = this.places.find(_.name == name)

  /**
   * Gets a list of the names of all places on the map. List allows for potential duplicate city names.
   * The places are sorted in alphabetical order.
   * @return the list of names
   */
  def getPlaceNames: List[String] = this.places.toList.map(place => place.name).sorted

  /**
   * Find the length of the longest path starting at each place, and return the maximum of any of those lengths
   * @return the length of the longest simple, acyclic path on the board
   */
  def getLengthOfLongestPath: Int = {
    (places.map(lengthOfLongestPathFrom)).max
  }

  /**
   * Get the length of the longest path on the board starting from a given place 
   * @param place the starting place to look from
   * @return the longest simple, acyclic path from the given place to any other place on the board. 0 if the place is
   *         not connected to any other place
   */
  def lengthOfLongestPathFrom(place: Place): Int = {
    // copy this board, removing the current place and its connections
    val newBoard = this.copy(places = places - place,
      connections = connections diff connections.filter(_.placePair.places contains place))
    
    // find the maximum of:
    // - the longest connection from the given place to a connected place plus the longest path 
    //   from that connected place to any other place in a board without the given place
    // - 0 if there are no connected places (base case)
    (getConnectedPlaces(place).map(connectedPlace =>
      // the longest connection between place and connectedPlace
      val connection: Connection = connections.toList.filter(Set(place, connectedPlace) subsetOf _.placePair.places)
        .sortBy(_.length).toList(0)
        
      newBoard.lengthOfLongestPathFrom(connectedPlace) + connection.length
    ) + 0).max
  }

  /**
   * Gets all the destinations within the map by getting every destination starting at each place and putting them
   * into a single set. A destination is a PlacePair with two places that are connected via some path.
   *
   * @return the set of destinations
   */
  def getAllDestinations: Set[PlacePair] = {
    unionFindToDisjointedPlaces(buildConnectedComponentMap(buildUnionFind)).flatMap(component => {
      component.flatMap(place1 => component.map(place2 => (place1, place2)))
        .filter(places => places._1 != places._2)
        .map(places => (PlacePair(places._1, places._2)))
    })
  }

  /**
   * Given a map from each place to its representative (in a union-find data structure), get each connected component
   * in the form of a set of sets of connected places.
   * @param representatives union-find representation of the board's places
   * @return connected components Set(Set(Place))
   */
  def unionFindToDisjointedPlaces(representatives: Map[Place, Place]): Set[Set[Place]] = {
    var repMap = mutable.Map() ++ representatives.map(value => (value._2, mutable.Set[Place]()))
    representatives.foreach{case (place, rep) => repMap(rep) += place}
    repMap.values.map(_.toSet).toSet
  }

  /**
   * Find the final representative of the given place by traversing down the representatives map until a place
   * is found whose representative is itself.
   * @param place the place to find the representative for
   * @param representatives the representatives map
   * @return the final representative of the given place
   */
  def findRep(place: Place, representatives: Map[Place, Place]): Place = {
    if (representatives(place) == place)
      place
    else
      findRep(representatives(place), representatives)
  }

  /**
   * Given a representative map, create a map from each place to it's final representative.
   * @param representatives union-find representation of the board's places
   * @return the new map of places to their final representative
   */
  def buildConnectedComponentMap(representatives: Map[Place, Place]): Map[Place, Place] = {
    representatives.map(places => (places._1, findRep(places._1, representatives)))
  }

  /**
   * Create a map from each place on the board to their representative based on the union-find data structure
   * discussed in Fundies 2.
   * @return the union-find data structure for the board
   */
  def buildUnionFind: Map[Place, Place] = {
    // Based on the union/find data structure example from Fundies 2
    var representatives = mutable.Map() ++ places.map(place => (place, place)).toMap
    var worklist = mutable.ListBuffer[PlacePair]()
    connections.foreach(c => worklist += c.placePair)
    // initialize representative for each place to itself
    while(!worklist.isEmpty) { // while there's more PlacePairs to look through
      val pList = worklist.remove(0).places.toList.sortBy(_.name)
      if (findRep(pList(0), representatives.toMap) != findRep(pList(1), representatives.toMap))
        // union the representatives for places in pList
        representatives(findRep(pList(0),representatives.toMap)) = findRep(pList(1), representatives.toMap)
    }
    representatives.toMap
  }

  /**
   * Gets a set of all places that the given place is connected to.
   *
   * @param place the place to find the connections of
   * @return the set of connected places
   */
  def getConnectedPlaces(place: Place): Set[Place] = {
    // filter to only connections that contain the given place
    this.connections.filter(connection => {
      connection.placePair.places contains place
    })
      // converts set of connections containing given place to a set of the other place within each connection
      .map(connection => (connection.placePair.places - place).head)
  }

  /**
   * Checks whether there is a direct connection between the two given places
   *
   * @param p1 the first place
   * @param p2 the second place
   * @return true if p1 and p2 have a connection, otherwise false
   */
  def hasConnection(p1: Place, p2: Place): Boolean =
    this.connections.exists(connection => connection.placePair.places == Set(p1, p2))

  /**
   * Check whether there is a path between the given PlacePair.
   * @param places the PlacePair to check
   * @return true if there is a path between the places, otherwise false
   */
  def hasPath(places: PlacePair): Boolean = this.getAllDestinations contains places
}

/**
 * A Posn represents an X,Y coordinate on the map,
 * counting in pixels down and right from the origin in the top left.
 * @param x the x coordinate
 * @param y the y coordinate
 */
case class Posn(x: Int, y: Int) {
  assert(x >= 0 && y >= 0, "coordinates must be non-negative")

  /**
   * Finds the distance
   * @param posn
   */
  def distance(posn: Posn): Double = Math.sqrt((Math.pow((posn.x - this.x), 2) + Math.pow((posn.y - this.y), 2)))
}

/**
 * A Place represents a city on the map.
 * @param name the name of the city
 * @param posn the city's location on the map
 */
case class Place(name: String, posn: Posn) {
  assert(name.matches("[a-zA-Z0-9\\ \\.\\,]+"), "invalid character(s) in name")
  assert(name.length <= 25, "invalid length of name")
}

/**
 * A Connection represents a direct connection between two Places.
 * @param placePair the PlacePair formed by the two Places connected by the connection
 * @param color the color required for the connection (red, blue, green, or white)
 * @param length the number of segments required for the connection, between MinLength and MaxLength
 */
case class Connection(placePair: PlacePair, color: Color, length: Int) {
  val MinLength = 3
  val MaxLength = 5
  assert(MinLength to MaxLength contains length, "invalid length")
  assert(Set(Color.RED, Color.BLUE, Color.GREEN, Color.WHITE) contains color, "invalid color")
}

/**
 * A PlacePair is a set of 2 places.
 * @param places a set of the 2 places that make up the place pair
 */
case class PlacePair(places: Set[Place]) {
  assert(places.size == 2, "invalid number of places")

  /**
   * Gets the other place in the PlacePair, assuming that the given
   * place was also in it.
   */
  def getOtherPlace(place: Place): Place = {
    assert(places contains place, "place not in places")
    (places - place).toList(0)
  }
}

/**
 * Companion object for the PlacePair class, used to add a convenience constructor.
 */
object PlacePair {
  /**
   * Convenience constructor for adding the 2 places to the places set
   * @param place1 the first place
   * @param place2 the second place
   */
  def apply(place1: Place, place2: Place): PlacePair = PlacePair(Set(place1, place2))
}
