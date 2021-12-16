import Utils.anyToJValue
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.*
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.*
import org.mockito.Mockito.{clearInvocations, doReturn, spy, times, verify, when}
import org.mockito.ArgumentMatchers.*
import org.mockito.invocation.*

import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters.*
import org.json4s.*

import java.awt.Color

class PlayerMethodTranslatorTests extends AnyFunSuite with Matchers with MockitoSugar {

  val player = mock[Player]

  test("start should be called once on a player") {
    when(player.start).thenReturn(Future(Utils.defaultBoard))
    val translator = new ServerProxy(player)
    translator.callMethod(JArray(List(
      JString("start"), JArray(List())
    )))
    verify(player).start
  }

  test("setup should be called once on a player") {
    val translator = new ServerProxy(player)
    translator.callMethod(JArray(List(
      JString("setup"),
      JArray(List(
        Utils.boardToJValue(Utils.defaultBoard),
        JInt(45),
        JArray(List(JString("red"), JString("red"), JString("red"), JString("red")))))
    )))
    verify(player).setup
  }

  test("pick should be called once on a player") {
    val translator = new ServerProxy(player)
    translator.callMethod(JArray(List(
      JString("setup"),
      JArray(List(
        Utils.boardToJValue(Utils.defaultBoard),
        JInt(45),
        JArray(List(JString("red"), JString("red"), JString("red"), JString("red")))))
    )))

    val destinations = Utils.defaultBoard.getAllDestinations.toList.take(5).toSet
    val returned = destinations.take(3)
    when(player.pick(destinations)).thenReturn(Future(returned))
    translator.callMethod(JArray(List(
      JString("pick"),
      JArray(List(
        Utils.anyToJValue(destinations)
      ))

    )))
    verify(player).setup(Utils.defaultBoard, 45, Utils.cardListToCardCounts(List.fill(4)(Color.RED)))
    verify(player).pick(destinations)
  }
}
