import org.json4s.*
import org.json4s.jackson.JsonMethods.*
import org.json4s.JsonDSL.seq2jvalue

import java.io.{BufferedReader, ByteArrayInputStream, ByteArrayOutputStream, InputStream, InputStreamReader}
import scala.collection.mutable.ListBuffer
import scala.io.{Source, StdIn}
import org.json4s.jackson.JsonMethods.*

import java.awt.Color
import scala.collection.mutable
import scala.concurrent.{Await, Future}
import scala.util.Random
import org.json4s.jackson.Serialization.writePretty

import javax.print.attribute.standard.Destination
import scala.language.postfixOps
import concurrent.duration.DurationInt

object Utils {
  implicit val constants: Constants = Constants()


  /**
   * Use artificial input and output stream to simulate standard in and out
   *
   * @param args  command line args to run the program with
   * @param stdin the string to be sent to the program's stdin
   * @return returns the output to stdout
   */
  def testCLI(args: Array[String], stdin: String, function: Function[Array[String], Unit]): ByteArrayOutputStream = {
    val out = new ByteArrayOutputStream()
    val in = new ByteArrayInputStream(stdin.getBytes())
    Console.withOut(out) {
      Console.withIn(in) {
        function(args)
        return out
      }
    }
  }

  /**
   * Execute the test of a given number that corresponds to input and expected output files for XJson
   *
   * @param testNumber the number to use when searching for test input / output data
   */
  def execute_test(testDir: String, testNumber: Int, function: Function[Array[String], Unit]): Unit =
    val json_input = Source.fromFile(testDir + testNumber + "-in.json").mkString
    val json_output = Source.fromFile(testDir + testNumber + "-out.json").mkString
    val output = testCLI(Array(""), json_input, function).toString
    assert(removeWhitespace(json_output) == removeWhitespace(output), s"${testDir} test ${testNumber} didn't work \nexpected: ${json_output}\nactual: ${output}")

  def removeWhitespace(string: String) = string.filterNot(char => char.isWhitespace)

  /**
   * Reads lines from stdin until there are no more available.
   *
   * @return the string containing all lines
   */
  def readToEOFFromStdIn(): String = {
    // read in the json input
    var line = ""
    var input = ""
    while ( {
      line = StdIn.readLine()
      line != null
    }) {
      input += line + "\n"
    }
    return input
  }

  /**
   * Reads input waits to read from an input stream for a given amount of time.
   * @param input the input stream
   * @param timeout the amount of time to wait for input on the given input stream
   * @return the input found if there was any
   */
  def waitForInputThenReadAll(input: InputStream, timeout: Int): Option[String] = {
    val startTime = System.currentTimeMillis()

    val in: BufferedReader = new BufferedReader(new InputStreamReader(input))
    var line: String = null;
    while (line == null) {
      if (System.currentTimeMillis() - startTime > timeout)
        return None
      while (in.ready())
        val newLine = in.readLine()
        if (line == null) {
          line = newLine
        } else {
          line += newLine
        }
    }
    Some(line)
  }

  /**
   * Reads
   *
   * @return
   */
  def readMultipleJValues: List[JValue] = {
    // list of the JValues that are parsed in
    var parsedInput = ListBuffer[JValue]()

    val input = readToEOFFromStdIn()

    // words we've collected to try to parse so far
    var currentInput = ""

    // splits on whitespace without removing it
    val words = input.split("""(?=\s+)""")
    words.foreach(word => {
      currentInput += word
      // attempt to parse the input so far
      try {
        val parsedSoFar = parse(currentInput)
        parsedSoFar match {
          // if the input so far parses as a JNothing, that means we haven't finished receiving
          // the JValue yet
          case JNothing => {}
          // otherwise, we have parsed the entire first JValue, so we add it to the parsed input
          // list and reset the input in preparation to read the next JValue
          case _ => {
            parsedInput += parsedSoFar
            currentInput = ""
          }
        }
      }
      catch {
        case e =>
      }
    })
    parsedInput.toList
  }


  /**
   * Convert given json input into a Board.
   *
   * @param input json Map to be converted
   * @return a Board
   */
  def jsonToBoard(input: JValue): Board = {
    var width = 0
    var height = 0
    var places = Set[Place]()
    var connections = Set[Connection]()
    // extract obj stored in input
    val JObject(obj) = input
    // iterate through the json Map and extract values from each field
    obj.map(entry => {
      entry match {
        case ("width", JInt(w)) => width = w.toInt
        case ("height", JInt(h)) => height = h.toInt
        case ("cities", JArray(c)) => {
          places = getPlaces(c)
        }
        case ("connections", JObject(c)) => {
          connections = getConnections(c, places)
        }
      }
    })
    val dimension = Posn(width, height)
    Board(places, connections, dimension)
  }

  /**
   * Creates a list of Players from json input.
   * @param input a JValue representing a list of Players that matches the remote protocol for the project
   * @return a list of Players
   */
  def jsonToPlayerInstance(input: JValue): List[Player] = {
    input match {
      case JArray(players) => players.zipWithIndex.map {
        case (JArray(player: List[JString]), i) =>
          AIPlayer(player(0).s, player(1).s.replace("-", ""), player(1).s.replace("-", ""), i)
      }
    }
  }

  /**
   * creates Players from json representation that will return the given board when "start" is called
   * @param input the list of players as json
   * @param board the default board that all players should return when "start" is called.
   * @return a list of Players configured to all return the given board.
   */
  def jsonToPlayerInstanceWithBoard(input: JValue, board: Board): List[Player] = {
    input match {
      case JArray(players) => players.zipWithIndex.map {
        case (JArray(player: List[JString]), i) =>
          AIPlayer(player(0).s, player(1).s.replace("-", ""), player(1).s.replace("-", ""), i, board)
      }
    }
  }

  /**
   * Creates a deck of cards in order based on json representation
   * @param input json representation of a deck of cards as defined by the project spec
   * @return a list of colors which represent the ordering of the deck of cards
   */
  def jsonToDeck(input: JValue): List[Color] = {
    input match {
      case JArray(array) => array.map {
        case JString(color) => getColor(color)
      }
    }
  }

  /**
   * Creates a PlacePair from a json representation
   * @param input the json representation of a pair of places
   * @param board the game board that these places exist on
   * @return a PlacePair that represents the given pair of places from json
   */
  def jsonToPlacePair(input: JValue, board: Board): PlacePair = {
    input match {
      case JArray(a) => PlacePair(a.map(p =>
        p match {
          case JString(name) => board.getPlace(name).get
        }
      ).toSet)
    }
  }

  /**
   * Creates a set of PlacePairs from an input list of destinations in JSON by retrieving them from the given board.
   * @param input the list of destinations
   * @param board the board to get from
   * @return the set of PlacePairs
   */
  def jsonToPlacePairSet(input: JValue, board: Board): Set[PlacePair] = {
    input match {
      case JArray(array) => array.map(dest =>
        dest match {
          case destination: JArray => jsonToPlacePair(destination, board)
        }).toSet
    }
  }

  /**
   * Creates a Connection object from JSON input
   * @param input the json representation of a Connection as defined in the project spec
   * @param board the board on which these places exist & a game is being played
   * @return a Connection from JSON
   */
  def jsonToConnection(input: JValue, board: Board): Connection = {
    input match {
      case JArray(array) => {
        val JString(place1) = array(0)
        val JString(place2) = array(1)
        val JString(color) = array(2)
        val JInt(length) = array(3)
        Connection(PlacePair(board.getPlace(place1).get, board.getPlace(place2).get), getColor(color), length.toInt)
      }
    }
  }

  /**
   * Converts an action from JSON to an option of connection by looking up the Connection on the board if
   * that was chosen, or returning None otherwise.
   * @param input the input action
   * @param board the board for the game
   * @return an Option[Connection] representing the action taken by a player
   */
  def jsonToOptionConnection(input: JValue, board: Board): Option[Connection] = {
    input match {
      case array: JArray => Some(jsonToConnection(array, board))
      case JString("more cards") => None
    }
  }

  /**
   * Creates the String output of the rankings of a tournament
   * @param ranking the ranking of players
   * @param cheaters the list of players who cheated
   * @return the rankings message as defined by the project spec.
   */
  def convertRankings(ranking: List[Set[Player]], cheaters: Set[Player]): String = {
    pretty(render(
      List(
        JArray(ranking
          // avoids an array of array when no players were in the ranking
          .filterNot(_.isEmpty)
          .map(rank => convertRank(rank))),
        convertRank(cheaters))))
  }

  /**
   * Formats the results of a tournament into a string according to the winners and cheaters
   * @param winners the winners of the tournament
   * @param cheaters the cheaters in the tournament
   * @return the result of the tournament
   */
  def formatManagerResults(winners: Set[Player], cheaters: Set[Player]): String = {
    pretty(render(
      JArray(List(convertRank(winners), convertRank(cheaters)))))
  }

  /**
   * Converts an individual rank within the tournament to JSON, by sorting the names alphabetically.
   * @param rank the set of players at that rank.
   * @return the JValue of the rank
   */
  def convertRank(rank: Set[Player]): JArray = {
    JArray(rank.toList.sortBy(_.name).map(player => JString(player.name)))
  }

  /**
   * Get the set of places from the json array of cities.
   *
   * @param cities JArray of cities
   * @return set of places
   */
  def getPlaces(cities: List[JValue]): Set[Place] = {
    // convert each city to a place
    cities.map { case JArray(city) => {
      val JString(cityName) = city(0)
      val JArray(posn) = city(1)
      val JInt(x) = posn(0)
      val JInt(y) = posn(1)
      Place(cityName, Posn(x.toInt, y.toInt))
    }
    }.toSet
  }

  /**
   * Get the set of connections from the json object connection (with the
   *
   * @param connections JObject of connection
   * @return set of connections
   */
  def getConnections(connections: List[JField], places: Set[Place]): Set[Connection] = {
    // iterate over all name-target pairs
    // flatten list to return one list of connections
    connections.flatMap { case (fromName, JObject(target)) =>
      val place1 = places.find(_.name == fromName).get
      // iterate over all name-target pairs
      // flatten list to return one list of connections
      target.flatMap { case (toName, JObject(segment)) => {
        val place2 = places.find(_.name == toName).get
        // iterate over all segments, adding a connection between the two places
        segment.map { case (color, JInt(length)) =>
          Connection(PlacePair(place1, place2), Utils.getColor(color), length.toInt)
        }
      }
      }
    }.toSet
  }


  /**
   * Convert given json input into a PlayerState.
   *
   * @param input json PlayerState to be converted
   * @return a PlayerState
   */
  def jsonToPlayerState(input: JValue, board: Board): PlayerState = {
    var cardCounts = mutable.Map[Color, Int]()
    var rails = 0
    var destinations = mutable.Set[PlacePair]()
    var player = "0"
    // extract obj stored in input
    val JObject(obj) = input

    var acquiredConnections = mutable.Map[String, Set[Connection]]()

    // iterate through the json Map and extract values from each field
    obj.map(entry => {
      entry match {
        case ("this", JObject(thisPlayer)) => {
          thisPlayer.map(field =>
            field match {
              case ("destination1" | "destination2", JArray(arr: List[JString])) => {
                destinations.add(PlacePair(board.getPlace(arr(0).s).get, board.getPlace(arr(1).s).get))
              }
              case ("rails", JInt(n)) => rails = n.toInt
              case ("cards", JObject(cards)) => {
                cards.foreach(card => cardCounts(Utils.getColor(card._1)) = card._2.asInstanceOf[JInt].num.toInt)
              }
              case ("acquired", JArray(acquireds)) => acquiredConnections("0") = acquireds.map(acquired => acquiredToConnection(acquired, board)).toSet
            })
        }
        case ("acquired", JArray(players)) => players.zipWithIndex.foreach {
          case (JArray(acquireds), i) => acquiredConnections((i + 1).toString) = acquireds.map(acquired => acquiredToConnection(acquired, board)).toSet
        }
      }
    })

    val publicState = PublicState(board, acquiredConnections.toMap, "0", acquiredConnections.keySet.toList)
    PlayerState(publicState, PrivateState(cardCounts.toMap, rails, destinations.toSet))
  }


  /**
   * Turns the List of JValues representing one connection a player has acquired to a Connection
   *
   * @param acquired the list of JValues
   * @param board    the board that the Connection is from
   * @return the Connection
   */
  def acquiredToConnection(acquiredValue: JValue, board: Board): Connection = {
    val JArray(acquired) = acquiredValue.asInstanceOf[JArray]
    val d1 = board.getPlace(acquired(0).asInstanceOf[JString].s).get
    val d2 = board.getPlace(acquired(1).asInstanceOf[JString].s).get
    val color = Utils.getColor(acquired(2).asInstanceOf[JString].s)
    val length = acquired(3).asInstanceOf[JInt].num.toInt
    Connection(PlacePair(d1, d2), color, length)
  }

  /**
   * Given an immutable map, returns a new version of itself with the value
   * at the given key modified according to the supplied function.
   *
   * @param map  the map to update
   * @param key  the key corresponding to the value that should be updated
   * @param func the function to apply to the value that should be updated
   */
  def updateMap[K, V](map: Map[K, V], key: K, func: V => V): Map[K, V] = {
    map + (key -> func(map(key)))
  }

  /**
   * Sort the given set of destinations lexicographically.
   *
   * @param destinations the set of destinations to be sorted
   * @return the sorted list
   */
  def sortDestinations(destinations: Set[PlacePair]): List[PlacePair] = {
    destinations.toList.sortBy(dest =>
      // each destination is converted to an alphabetized sequence of its city names, which are then concatenated together
      // we sort the destinations list according to this string
      dest.places.map(_.name).toList.sorted.mkString(""))
  }

  /**
   * Given a map of card counts, return the total number of cards.
   *
   * @param cardCounts the cards to be counted
   * @return total number of cards
   */
  def countAllCards(cardCounts: Map[Color, Int]): Int = {
    cardCounts.foldRight(0)((entry, sum) => entry._2 + sum)
  }

  /**
   * Sort the given set of connections lexicographically by place name, lenghth, and color.
   *
   * @param connections the connections to be sorted
   * @return the sorted list of connections
   */
  def sortConnections(connections: Set[Connection]): List[Connection] = {
    // sort connections by the string created from its elements
    connections.toList.sortBy(connection =>
      // concatenate the sorted place names and then the length and color of the connection
      connection.placePair.places.map(_.name).toList.sorted.mkString("") + connection.length + connection.color.toString)
  }

  /**
   * Sort a set of places by their names and return them as a list, ordered lexicographically by name.
   *
   * @param places the set of places to be sorted
   * @return the sorted list of places
   */
  def sortPlaces(places: Set[Place]): List[Place] = places.toList.sortBy(_.name)

  /**
   * Get the java.awt color object from a string.
   *
   * @param color the name of the color
   * @return the Color object
   */
  def getColor(color: String): Color = {
    val upperColor = color.toUpperCase
    upperColor match {
      case "RED" => Color.RED
      case "BLUE" => Color.BLUE
      case "GREEN" => Color.GREEN
      case "WHITE" => Color.WHITE
      case _ => throw new IllegalArgumentException
    }
  }

  /**
   * Get the name of the given java.awt color object.
   *
   * @param color the color to get the name of
   * @return the name of the given color
   */
  def getColorName(color: Color): String = {
    color match {
      case Color.RED => "red"
      case Color.BLUE => "blue"
      case Color.GREEN => "green"
      case Color.WHITE => "white"
      case _ => throw new IllegalArgumentException
    }
  }


  /**
   * Turn a connection into the acquired structure used in the instructor implementation.
   *
   * @param connection the connection to convert
   * @return the String representation of the acquired
   */
  def connectionToAcquired(connection: Connection): String = {
    val places = Utils.sortPlaces(connection.placePair.places)
    "[\"" +
      places(0).name + "\", \"" +
      places(1).name + "\", \"" +
      Utils.getColorName(connection.color) + "\", " +
      connection.length + "]"
  }

  /**
   * Convert the given cards from a map representation to a list.
   *
   * @param cardCounts map of Color to Int (representing cards)
   * @return list of Color (representing cards)
   */
  def cardCountsToCardList(cardCounts: Map[Color, Int]): List[Color] =
    cardCounts.flatMap { case (color, count) => List.fill(count)(color) }.toList

  /**
   * Convert the given cards from a list representation to a map.
   *
   * @param cards list of Color (representing cards)
   * @return map of Color to Int (representing cards)
   */
  def cardListToCardCounts(cards: List[Color]) = {
    val startMap = Map(Color.RED -> 0, Color.BLUE -> 0, Color.GREEN -> 0, Color.WHITE -> 0)
    cards.foldRight(startMap)((color, counts) => counts.updatedWith(color)(_.map(_ + 1)))
  }

  /**
   * Randomly shuffle all possible destinations in the board.
   *
   * @return a randomly shuffled list of all possible destinations
   */
  def defaultOrderDestinations(destinations: Set[PlacePair]): List[PlacePair] = Random.shuffle(destinations.toList)

  /**
   * Default starting card counts.
   */
  def defaultCards = cardCountsToCardList(Map(Color.BLUE -> 65, Color.GREEN -> 65, Color.RED -> 60, Color.WHITE -> 60))

  /**
   * Convert the deck of colored cards from a map to a list representation and randomly shuffle the list.
   *
   * @return a randomly shuffled list of Color (representing the colored cards)
   */
  def defaultOrderCards(cards: List[Color]): List[Color] = Random.shuffle(cards)


  val defaultBoard: Board = {
    val A = Place("A", Posn(100, 100))
    val B = Place("B", Posn(300, 100))
    val C = Place("C", Posn(100, 800))
    val D = Place("D", Posn(500, 500))
    val E = Place("E", Posn(100, 500))
    val F = Place("F", Posn(200, 500))
    val G = Place("G", Posn(600, 600))
    val H = Place("H", Posn(100, 60))
    val I = Place("I", Posn(400, 200))
    val J = Place("J", Posn(200, 400))
    val K = Place("K", Posn(700, 700))

    val ABGreen = Connection(PlacePair(A, B), Color.GREEN, 3)
    val ABWhite = Connection(PlacePair(A, B), Color.WHITE, 4)
    val ADRed = Connection(PlacePair(A, D), Color.RED, 4)
    val BDRed = Connection(PlacePair(B, D), Color.RED, 3)
    val CDBlue = Connection(PlacePair(C, D), Color.BLUE, 5)
    val EFWhite = Connection(PlacePair(E, F), Color.WHITE, 4)
    val AHRed = Connection(PlacePair(A, H), Color.RED, 4)
    val HIWhite = Connection(PlacePair(H, I), Color.WHITE, 4)
    val IJBlue = Connection(PlacePair(I, J), Color.BLUE, 5)
    val JKGreen = Connection(PlacePair(J, K), Color.GREEN, 3)

    val Places = Set(A, B, C, D, E, F, G, H, I, J, K)
    val Connections = Set(ABGreen, ABWhite, ADRed, BDRed, CDBlue, EFWhite,
      AHRed, HIWhite, IJBlue, JKGreen)

    Board(Places, Connections, Posn(800, 800))
  }

  val smallBoard: Board = {
    val A = Place("A", Posn(100, 100))
    Board(Set(), Set(), Posn(800, 800))
  }

  val normalBoard: Board = {
    val A = Place("A", Posn(100, 100))
    val B = Place("B", Posn(300, 100))
    val C = Place("C", Posn(100, 800))
    val D = Place("D", Posn(500, 500))
    val E = Place("E", Posn(100, 500))
    val F = Place("F", Posn(200, 500))
    val G = Place("G", Posn(600, 600))
    val H = Place("H", Posn(100, 60))
    val I = Place("I", Posn(400, 200))
    val J = Place("J", Posn(200, 400))
    val K = Place("K", Posn(700, 700))
    val L = Place("L", Posn(100, 670))

    val ABGreen = Connection(PlacePair(A, B), Color.GREEN, 3)
    val ABWhite = Connection(PlacePair(A, B), Color.WHITE, 4)
    val ADRed = Connection(PlacePair(A, D), Color.RED, 4)
    val BDRed = Connection(PlacePair(B, D), Color.RED, 3)
    val CDBlue = Connection(PlacePair(C, D), Color.BLUE, 5)
    val EFWhite = Connection(PlacePair(E, F), Color.WHITE, 4)
    val AHRed = Connection(PlacePair(A, H), Color.RED, 4)
    val HIWhite = Connection(PlacePair(H, I), Color.WHITE, 4)
    val IJBlue = Connection(PlacePair(I, J), Color.BLUE, 5)
    val JKGreen = Connection(PlacePair(J, K), Color.GREEN, 3)

    val Places = Set(A, B, C, D, E, F, G, H, I, J, K, L)
    val Connections = Set(ABGreen, ABWhite, ADRed, BDRed, CDBlue, EFWhite,
      AHRed, HIWhite, IJBlue, JKGreen)

    Board(Places, Connections, Posn(800, 800))
  }

  /**
   * determines whether the provided board has enough destinations for a game with the given
   * number of players
   *
   * @param board      the provided board
   * @param numPlayers the number of players that need to choose destinations.
   * @return true if there are enough destinations for each player to choose from 5 options
   */
  def isBoardValid(board: Board, numPlayers: Int): Boolean = {
    board.getAllDestinations.size >=
      numPlayers * constants.DestinationsPerPlayer +
        (constants.DestinationChoices - constants.DestinationsPerPlayer)
  }


  /**
   * Takes in an anonymous void function that involves interaction with the player of the given name. If any exception
   * occurs while communicating with them, eject them according to the implicitly defined ejectAction
   *
   * @param action      the action to perform
   * @param player      the player to possibly eject
   * @param ejectAction the action to perform if the player has to be ejected
   */
  def communicateWithPlayer[T](action: () => Future[T], player: Player)(implicit ejectAction: Function[Player, Unit]): Option[T] = {
    try {
      Some(Await.ready(action(), (constants.ClientResponseTimeout / 1000) seconds).value.get.get)
    } catch {
      case e => {
        //e.printStackTrace()
        ejectAction(player)
        None
      }
    }
  }

  /**
   * Creates a method call in the format specified by the assignment to be sent over the network.
   * @param methodName the name of the method to call
   * @param arguments the arguments for the method
   * @return the string in JSON form
   */
  def createJSONMethodCall(methodName: String, arguments: List[Any]): String = {
    pretty(render(JArray(List(JString(methodName),
      JArray(arguments.map(anyToJValue))))))
  }

  /**
   * Converts any type to a JValue.
   * @param thing the thing to convert
   * @return the JValue
   */
  def anyToJValue(thing: Any): JValue = {
    thing match {
      case b: Boolean => JBool(b)
      case m: Map[String, Any] => JObject(m.toList.map { case (k, v) => JField(k, anyToJValue(v)) })
      case l: Iterable[Any] => JArray(l.map(anyToJValue).toList)
      case i: Int => JInt(i)
      case c: Color => JString(getColorName(c))
      case b: Board => boardToJValue(b)
      case p: PlayerState => playerStateToJSON(p)
      case d: PlacePair => destinationToJSON(d)
      case u: Unit => JString("void")
      case p: Place => JArray(List(JString(p.name), JArray(List(JInt(p.posn.x), JInt(p.posn.y)))))
      case Some(connection: Connection) => parse(connectionToAcquired(connection))
      case None => JString("more cards")
    }
  }

  /**
   * Converts a board to a JValue.
   * @param board the board to convert
   * @return the JValue
   */
  def boardToJValue(board: Board): JValue = {
    JObject(List(
      JField("width", anyToJValue(board.dimensions.x)),
      JField("height", anyToJValue(board.dimensions.y)),
      JField("cities", anyToJValue(sortPlaces(board.places))),
      JField("connections", connectionsToJValueFromBoard(board))
    ))
  }

  /**
   * Converts all of the connections on the given board to their JValue form.
   * @param board the board to convert the connections from.
   * @return the JValue of the connections
   */
  def connectionsToJValueFromBoard(board: Board): JValue = {
    val cities = sortPlaces(board.places)
    val connsSets = cities.map(city => board.connections
      .filter(_.placePair.places contains city)
      .filter(_.placePair.places.exists(_.name > city.name))
      .map(connection =>
        board.connections.filter(pair => pair.placePair.places == connection.placePair.places))
    )

    JObject(cities.zip(connsSets).map { case (city, cityConnsSets) =>
      JField(city.name,
        JObject(cityConnsSets.toList.map(connsSet =>
          JField(connsSet.toList(0).placePair.getOtherPlace(city).name,
            JObject(connsSet.toList.map(c => JField(getColorName(c.color), JInt(c.length))))))))
    })
  }

  /**
   * Converts a PlayerState to a JValue.
   * @param playerState the playerState to convert
   * @return the JValue form of the PlayerState.
   */
  def playerStateToJSON(playerState: PlayerState): JObject = {
    val destinations = Utils.sortDestinations(playerState.privateState
      .destinations).map(dest => destinationToJSON(dest))
    val playerAcquired = playerState.publicState
        .acquiredConnections(playerState.publicState.currentTurn)
      .map(connection => parse(connectionToAcquired(connection)))
    val otherAcquired =
      (playerState.publicState.playerNames diff List(playerState.publicState.currentTurn)).map(playerName =>
        JArray(playerState.publicState.acquiredConnections(playerName)
          .map(connection => parse(connectionToAcquired(connection))).toList))
    JObject(List(
      JField("this", JObject(List(
        JField("destination1", destinations(0)),
        JField("destination2", destinations(1)),
        JField("rails", JInt(playerState.privateState.rails)),
        JField("cards", anyToJValue(playerState.privateState.cardCounts.map{case (color, num) => (getColorName(color), num)}.toMap)),
        JField("acquired", playerAcquired)
      ))),
      JField("acquired", JArray(otherAcquired))
    ))
  }


  /**
   * Converts a destination to a JValue
   * @param dest the destination to convert
   * @return the JValue output
   */
  def destinationToJSON(dest: PlacePair): JArray = {
    JArray(sortPlaces(dest.places).map(place => JString(place.name)))
  }

  /**
   * Creates a manager with the given parameters and plays out a tournament, printing the result
   * if it was played out successfully, or "error: not enough destinations" otherwise.
   * @param players the players to play the tournament with
   * @param deck the deck to use
   * @param orderDestinations the order to hand out destinations in (random by default)
   * @param orderCards the order to hand out cards in (random by default)
   */
  def playTournament(players: Set[Player],
                     deck: List[Color] = Utils.defaultCards,
                     orderDestinations: Function[Set[PlacePair], List[PlacePair]] = Utils.defaultOrderDestinations,
                     orderCards: Function[List[Color], List[Color]] = Utils.defaultOrderCards): Unit = {
    try {
      val manager = new Manager(players, deck, orderDestinations, orderCards)
      val result = manager.playTournament
      result match {
        case (winners, cheaters) => println(Utils.formatManagerResults(winners, cheaters))
      }
    } catch {
      case e => {
        e.printStackTrace()
        println("\"error: not enough destinations\"")
      }
    }
  }

}
