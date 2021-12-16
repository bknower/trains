import java.awt.{BasicStroke, Color, Dimension, Graphics, Graphics2D, Image, Insets, Rectangle}
import scala.io.StdIn
import javax.swing.JFrame
import scala.swing.*
import scala.swing.event.*
import java.awt.BasicStroke
import org.json4s.native.JsonMethods.*

//Note: WE HAVE RENAMED MAP TO BOARD IN OUR DATA REPRESENTATION!

// TODO scale posn/size values so that we can see everything on a small board

/**
 * Singleton object to run Board GUI program using SimpleSwingApplication
 */
class MapEditor(val board: Board) extends SimpleSwingApplication {
  // constants that determine the appearance of the drawing
  private val lineWidth = 4
  private val lineSpacing = 6
  private val segmentSpacing = 10
  private val fontName = "Arial"
  private val fontSize = 16

  // radius of the city circle
  private val radius = 10

  private val placeColor = Color.RED
  private val nameColor = Color.WHITE
  private val backgroundColor = Color.BLACK

  /** SimpleSwingApplication's version of a main method */
  def top = new MainFrame {
    title = "Board GUI"
    contents = mainPanel
  }

  /** the main panel to be displayed on the screen for the gui program */
  def mainPanel = new Panel {
    // set window size
    preferredSize = new Dimension(board.dimensions.x, board.dimensions.y)
    focusable = true

    listenTo(mouse.clicks)
    reactions += {
      case e: MouseClicked =>
        println("Mouse clicked at " + e.point)
    }
    /**
     * Called automatically by Swing whenever the canvas needs to be repainted
     *
     * @param g the graphics object to draw on
     */
    override def paint(g: Graphics2D) = {
      this.drawBackground(g)
      this.drawConnections(g)
      this.drawPlaces(g)
    }

    /**
     * Draw all connections in the list of connections
     *
     * @param g the graphics object to draw on
     */
    def drawConnections(g: Graphics2D): Unit = {
      // splits the connections into a set of sets of connections between the same two places
      val connectionSets = board.connections.map(connection =>
        board.connections.filter(pair => pair.placePair.places == connection.placePair.places))

      // draw each connection set
      connectionSets.foreach(drawConnectionSet(_, g))
    }

    /**
     * Draw a set of connections between the same 2 places
     *
     * @param connections the connections to draw
     * @param g           the graphics object to draw on
     */
    def drawConnectionSet(connections: Set[Connection], g: Graphics2D): Unit = {
      // sort connections by the name of their color so they always appear in the same order
      val connectionList = connections.toList.sortBy(_.color.toString)

      // To draw the connections, start with the leftmost connection. The position of this line depends on how many
      // connections there are between the two places. The offset of each connection line will be calculated such that
      // the connections are centered between the two places with some space between them.
      val startingLeftOffset = (connectionList.size - 1) * (lineWidth + lineSpacing) / 2

      // draw each connection in the connection list
      connectionList.zipWithIndex.foreach { case (connection: Connection, i: Int) => {
        drawConnection(g, connection, i, startingLeftOffset)
      }}
    }

    /**
     * Draw a line representing an individual connection between 2 places.
     * @param g the graphics object to draw on
     * @param connection the connection to draw
     * @param i the index of this connection within the list of connections between these 2 places
     * @param startingLeftOffset how far the first connection in the list of connections between these 2 places was
     *                           offset to the left
     */
    def drawConnection(g: Graphics2D, connection: Connection, i: Int, startingLeftOffset: Int): Unit = {
      val places = connection.placePair.places.toList
      val pos0: Posn = places(0).posn
      val pos1: Posn = places(1).posn

      // the distance to draw this line away from the center line between the 2 points
      val distance = (lineWidth + lineSpacing) * i - startingLeftOffset

      // the angle of the line perpendicular to the line between the 2 points
      // 180 degrees minus the output of atan because atan can output between - 90 and 90, so this shifts it to be all
      // positive angles in the second or third quadrants
      var perpLineAngle = Math.PI - Math.atan((pos1.x - pos0.x).toFloat / (pos1.y - pos0.y).toFloat)

      // the distance along the x and y axis to shift the line to draw a parallel line
      val dx = (distance * Math.cos(perpLineAngle)).toInt
      val dy = (distance * Math.sin(perpLineAngle)).toInt

      // the length of each segment of the current line
      // we get the distance between the lines (plus segmentSpacing, since the last part of the dash will be a space)
      // then we divide this distance into a number of segments equal to the connection length and subtract
      // segment spacing, so the length only signifies the length of the segment itself
      val segmentLength = (pos0.distance(pos1) + segmentSpacing) / connection.length.toDouble - segmentSpacing

      // set the stroke of the line so it is displayed with dashes. The dash pattern and the line width are the only
      // important parts
      g.setStroke(
        new BasicStroke(lineWidth.toFloat, // Width
          BasicStroke.CAP_SQUARE, // End cap
          BasicStroke.JOIN_MITER, // Join style
          10, // Miter limit
          Array(segmentLength.toFloat, segmentSpacing.toFloat), // Dash pattern
          0))

      g.setColor(connection.color)
      g.drawLine(pos0.x + dx, pos0.y + dy, pos1.x + dx, pos1.y + dy)
    }

    /**
     * Draw the background of the map in the background color onto the given graphics object
     *
     * @param g the grophics object to draw on
     */
    def drawBackground(g: Graphics2D) = {
      // draw background
      g.setColor(backgroundColor)
      g.fillRect(0, 0, board.dimensions.x, board.dimensions.y)
    }

    /**
     * Draw each place as a circle and a name on the Board
     * @param g the graphics object to draw on
     */
    def drawPlaces(g: Graphics2D): Unit = {
      // draw circles
      g.setColor(placeColor)
      board.places.foreach(place => g.fillOval(place.posn.x - radius, place.posn.y - radius, radius * 2, radius * 2))

      // draw names
      g.setColor(nameColor)
      g.setFont(new Font(fontName, 1, fontSize))
      board.places.foreach(place => drawName(g, place))
    }

    /**
     * Draw the name of an individual place onto the given graphics object, making sure
     * that it will be shown onscreen
     * @param g the graphics object to draw onto
     * @param place the place whose name will be drawn
     */
    def drawName(g: Graphics2D, place: Place): Unit = {
      val width = g.getFontMetrics().stringWidth(place.name)
      var height = g.getFontMetrics().getHeight()

      // the initial location to draw the place so it is centered just below the circle
      var x = place.posn.x - width / 2
      var y = place.posn.y + height + radius

      // constrain on right border
      if (board.dimensions.x - (x + width) < 0) {
        x = board.dimensions.x - width
      }
      // constrain on left border
      if (x < 0) {
        x = 0
      }
      // constrain on bottom border
      if (board.dimensions.y - y < 0) {
        y = board.dimensions.y - radius
      }
      g.drawString(place.name, x, y)
    }
  }
}

/**
 * A test entry point into MapEditor.
 */
object RunMap {
  def main(args: Array[String]): Unit = {

    val A = Place("Apple", Posn(100, 100))
    val B = Place("Boston", Posn(300, 100))
    val C = Place("Campfire", Posn(100, 800))
    val D = Place("Detroit", Posn(500, 500))
    val E = Place("Ello", Posn(100, 500))
    val F = Place("Firefighter", Posn(200, 500))
    val G = Place("Goonies", Posn(600, 600))

    val ABGreen = Connection(PlacePair(A, B), Color.GREEN, 3)
    val ABWhite = Connection(PlacePair(A, B), Color.WHITE, 4)
    val ABRed = Connection(PlacePair(A, B), Color.RED, 5)
    val ABBlue = Connection(PlacePair(A, B), Color.BLUE, 4)
    val ACRed = Connection(PlacePair(A, C), Color.RED, 5)
    val ACBlue = Connection(PlacePair(A, C), Color.BLUE, 4)
    val ADRed = Connection(PlacePair(A, D), Color.RED, 4)
    val BDRed = Connection(PlacePair(B, D), Color.RED, 3)
    val CDBlue = Connection(PlacePair(C, D), Color.BLUE, 5)
    val EFWhite = Connection(PlacePair(E, F), Color.WHITE, 4)

    val Places = Set(A, B, C, D, E, F, G)
    val Connections = Set(ABGreen, ABWhite, ADRed, BDRed, CDBlue, EFWhite, ABRed, ABBlue, ACBlue, ACRed)

    val board = new Board(Places, Connections, Posn(800, 800))

    val mapEditor = MapEditor(board)
    mapEditor.main(args)
  }
}