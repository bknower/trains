import java.awt.{Color, Dimension, Graphics, Graphics2D, Image, Insets, Rectangle}
import scala.io.StdIn
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import java.util.{Timer, TimerTask}
import javax.swing.{JFrame}
import scala.swing.GridBagPanel.*
import scala.swing.*
import scala.swing.event.*
import event.Key.*

/** Stores data from the json in the proper format
 *
 * @param nodes the list of points to be displayed on the graph
 * @param size  the width and height of the graph in pixels
 */
case class Graph(nodes: List[List[Int]], size: Int) {
  assert(100 to 800 contains size, "invalid size")
  nodes.map(point => {
    assert(point.length == 2, "invalid posn")
    point.map(n => assert(0 to size contains n, "posn off of map"))
  })
}

/**
 * Singleton object to run GUI program using SimpleSwingApplication
 */
object XGui extends SimpleSwingApplication {

  // read in the json input
  var line = ""
  var input = ""
  while ( {
    line = StdIn.readLine();
    line != null
  }) {
    input += line
  }

  // deserialize json into graph
  val graph: Graph = try {
    decode[Graph](input).getOrElse(throw new IllegalArgumentException)
  } catch {
    case e => {
      print(e)
      sys.exit(0)
    }
  }

  // SimpleSwingApplication's version of a main method
  def top = new MainFrame {
    title = "xgui"
    contents = mainPanel
  }

  // the main panel to be displayed on the screen for the gui program
  def mainPanel = new Panel {
    // set window size
    preferredSize = new Dimension(graph.size, graph.size)
    focusable = true

    // close after 3 seconds
    new Timer().schedule(new TimerTask() {
      override def run(): Unit = {
        quit()
      }
    }, 3000)


    override def paint(g: Graphics2D) = {
      // draw background
      g.setColor(Color.ORANGE)
      g.fillRect(0, 0, graph.size, graph.size)

      // draw lines
      g.setColor(Color.RED)
      graph.nodes.combinations(2).toList.map(pair => g.drawLine(pair(0)(0), pair(0)(1), pair(1)(0), pair(1)(1)))

      // draw circles
      g.setColor(Color.BLACK)
      graph.nodes.map(node => g.fillOval(node(0) - 5, node(1) - 5, 10, 10))
    }
  }
}
