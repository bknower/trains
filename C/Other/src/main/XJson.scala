import scala.io.StdIn
import org.json4s.*
import org.json4s.native.JsonMethods.*
import java.lang.String
import scala.collection.mutable.ListBuffer

/** Singleton object to run JSON program using the main method */
object XJson {

  /** Entry point for the program
   *
   * @param args command line arguments
   *
   *             expects no command line arguments
   */
  def main(args: Array[String]): Unit =
    // list of the JValues that are parsed in
    var parsedInput = ListBuffer[JValue]()

    var input = ""
    var line = ""
    while ( {
      line = StdIn.readLine();
      line != null
    }) {
      // splits on whitespace without removing it
      val words = line.split("""(?=\s+)""")
      words.foreach(word => {
        input += word

        // attempt to parse the input so far
        try {
          val inputSoFar = parse(input)
          inputSoFar match {
            // if the input so far parses as a JNothing, that means we haven't finished receiving
            // the JValue yet
            case JNothing => {}
            // otherwise, we have parsed the entire first JValue, so we add it to the parsed input
            // list and reset the input in preparation to read the next JValue
            case _ => {
              parsedInput += inputSoFar
              input = ""
            }
          }
        }
        catch {
          case e =>
        }
      })
    }

    // iterate over the parsed json inout and reverse everything
    parsedInput.foreach(json => println(pretty(render(reverse(json)))))

  /** Reverses a JSON value as defined in the assignment.
   *
   * @param json a json4s JValue parsed from stdin
   * @return the reversed value
   */
  def reverse(json: JValue): JValue =
    json match {
      case JArray(list: List[JValue]) => return JArray(list.reverse.map(entry => reverse(entry)))
      case JObject(map: List[JField]) => return JObject(map.map(entry => JField(entry._1, reverse(entry._2))))
      case JString(string: String) => return JString(string.reverse)
      case JBool(boolean: Boolean) => return JBool(!boolean)
      case JInt(number: Number) => return JInt(-number)
      case JDouble(number: Double) => return JDouble(-number)
      case JDecimal(number: Number) => return JDecimal(-number)
      case JLong(number: Long) => return JLong(-number)
      case JNull => return JNull
      case _ => return JNothing
    }
}
