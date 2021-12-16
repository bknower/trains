import org.json4s.JString
import org.json4s.*
import org.json4s.jackson.JsonMethods.*
import scala.io.StdIn

/**
 * Singleton object to run the XMap program.
 */
object XMap {
  def main(args: Array[String]): Unit = {

    // read in place names
    val JString(place1) = parse(StdIn.readLine())
    val JString(place2) = parse(StdIn.readLine())

    val input = Utils.readToEOFFromStdIn()

    // create a JValue from json string input
    val jsonMap = parse(input)

    // convert jsonMap to a Map
    val map = Utils.jsonToBoard(jsonMap)

    // print out true if the map has a connection between the two given places
    println(map.hasPath(PlacePair(map.getPlace(place1).get, map.getPlace(place2).get)))

  }


}
