package com.thenewmotion.chargenetwork.eclearing.api

import com.thenewmotion.time.Imports._
import scalax.StringOption

/**
 * @author Yaroslav Klymko
 */
trait EclearingApi {
  def cards(): Seq[Card]
  def addCard(card: Card)
  def removeCard(card: Card)
}


case class Card(evcoId: String,
                roamingHubId: RoamingHubId.Value,
                tokenType: TokenType.Value,
                tokenId: String,
                printedNumber: String,
                expiryDate: Option[DateTime],
                pin: String,
                pinMandatory: Boolean,
                tokenActivated: Boolean,
                hash: Option[String])

object RoamingHubId {
  val Known = Seq(BlueCorner, ELaad, LadenetzDe, BallastNedam, TheNewMotion, Other)
  def apply(id: Int): Value = Known.find(_.id == id) getOrElse Unknown(id)
  def withName(name: String): Value = Known.find(_.toString == name) getOrElse Other

  sealed abstract class Value(val id: Int, name: String) {
    override val toString = name
  }

  case object Other extends Value(0, "Other")
  case object BlueCorner extends Value(1, "Blue Corner")
  case object ELaad extends Value(2, "e-laad")
  case object LadenetzDe extends Value(3, "ladenetz.de")
  case object BallastNedam extends Value(4, "Ballast Nedam")
  case object TheNewMotion extends Value(5, "TheNewMotion")
  case class Unknown(override val id: Int) extends Value(id, "Unknown")
}

object ExpiryDate {
  val formatter = DateTimeFormat.forPattern("dd/MM/yyyy")
  def apply(s: String) = StringOption(s).map(formatter.parseDateTime)
  def unapply(dt: Option[DateTime]): String = dt.map(_.toString(formatter)).getOrElse("")
}

object TokenType {
  val Known = Seq(MifareCl, MifareDes, Msisdn, Other)
  def apply(id: Int): Value = Known.find(_.id == id) getOrElse Unknown(id)
  def withName(name: String): Value = Known.find(_.toString == name) getOrElse Other

  def forRfid(rfid: String): TokenType.Value = {
    require(rfid.matches("[0-9A-Fa-f]+"), "Rfid '%s' doesn't comply pattern '[0-9A-Fa-f]+'".format(rfid))
    rfid.size match {
      case 8 => MifareCl
      case 10 => MifareDes
      case _ => Other
    }
  }

  sealed abstract class Value {
    def id: Int
  }

  case object MifareCl extends Value {
    val id = 1
    override val toString = "MIFARE_CL"
  }

  case object MifareDes extends Value {
    val id = 2
    override val toString = "MIFARE_DES"
  }

  case object Msisdn extends Value {
    val id = 3
    override val toString = "MSISDN"
  }

  case object Other extends Value {
    val id = 0
    override val toString = "Other"
  }

  case class Unknown(id: Int) extends Value
}

trait BooleanId {
  def unapply(b: Boolean): Int = if (b) 1 else 0

  def apply(id: Int): Boolean = id match {
    case 0 => false
    case 1 => true
    case _ => false
  }
}

object PinMandatory extends BooleanId
object TokenActivated extends BooleanId
