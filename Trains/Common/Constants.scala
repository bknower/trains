import scala.language.postfixOps
import scala.concurrent.duration.DurationInt

case class Constants() {
  val DefaultRails = 45
  val CardsPerPlayer = 4
  val DestinationChoices = 5
  val DestinationsPerPlayer = 2
  val MinPlayers = 2
  val MaxPlayers = 8
  val TotalCards = 250
  val SignUpTimeout = 3000
  val WaitPeriod = 20000
  val ClientResponseTimeout = 2000
  val MinFirstWaitPlayers = 5
  val MaxSignedUpPlayers = 50
  val ServerThreadPoolSize = 100
  val ClientThreadPoolSize = 100
}
