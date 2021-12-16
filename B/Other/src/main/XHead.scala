object XHead {
  import scala.util.CommandLineParser as CLP
  def main(args: Array[String]): Unit =
    try
      val lines = CLP.parseArgument[Int](args, 0)
      if (!args(0).startsWith("-") || args.size > 1) throw new CLP.ParseError(0, "error") else xhead(lines)
    catch
      case error: CLP.ParseError => println("error")

  def xhead(linesArg: Int): Unit = {
    // the input value starts with a - so it is interpreted as a negative int. Otherwise, we will run into an error and print "error"
    try
      val lines = -linesArg
      for (_ <- 1 to lines) {
        val line = scala.io.StdIn.readLine()
        if (line != null) println(line) else return
      }
    catch
      case error: CLP.ParseError => println("error")
  }
}
