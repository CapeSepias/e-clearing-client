package com.thenewmotion.chargenetwork.eclearing.api

import com.thenewmotion.time.Imports._

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
                serviceProvider: Option[String],
                printedNumber: String,
                expiryDate: Option[DateTime],
                pin: Int,
                pinMandatory: Boolean,
                tokenActivated: Boolean,
                hash: Option[String])

object RoamingHubId {
  val Known = Seq(BlueCorner, ELaad, LadenetzDe, Other)
  def apply(id: Int): Value = Known.find(_.id == id) getOrElse Unknown(id)
  def withName(name: String): Value = Known.find(_.toString == name) getOrElse Other

  sealed abstract class Value {
    def id: Int
  }

  case object Other extends Value {
    val id = 0
     override val toString = "Other"
  }

  case object BlueCorner extends Value {
    val id = 1
     override val toString = "Blue Corner"
  }

  case object ELaad extends Value {
    val id = 2
     override val toString = "e-laad"
  }

  case object LadenetzDe extends Value {
    val id = 3
     override val toString = "ladenetz.de"
  }

  case class Unknown(id: Int) extends Value
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
