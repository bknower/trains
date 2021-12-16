import java.util.{Timer, TimerTask}
import org.json4s.*
import org.json4s.jackson.JsonMethods.*
/**
 * Singleton object to run XVisualize through the main method
 */
object XVisualize {

  def main(args: Array[String]): Unit = {
    // read until EOF from stdin
    val input = Utils.readToEOFFromStdIn()

    // create a JValue from json string input
    val jsonMap = parse(input)

    // convert jsonMap to a Map
    val board = Utils.jsonToBoard(jsonMap)

    // create and run an instance of MapEditor, displaying the map to the screen
    val mapEditor = MapEditor(board)
    mapEditor.main(args)

    // close after 10 seconds
    new Timer().schedule(new TimerTask() {
      override def run(): Unit = {
        mapEditor.quit()
      }
    }, 10000)
  }
}
