package com.thenewmotion.chargenetwork.eclearing

import api._

/**
 * @author Yaroslav Klymko
 */
object Converters{
  implicit def roamingAuthorisationInfo2Card(rai: RoamingAuthorisationInfo): Card = {
    import rai._
    Card(StringOption(evcoId).getOrElse(""),
      RoamingHubId(roamingHubId),
      TokenType(tokenType),
      tokenId.toUpperCase,
      printedNumber,
      ExpiryDate(expiryDate),
      pin,
      PinMandatory(pinMandatory),
      TokenActivated(tokenActivated),
      StringOption(hash)
    )
  }

  implicit def card2RoamingAuthorisationInfo(card: Card): RoamingAuthorisationInfo = {
    import card._
    RoamingAuthorisationInfo(
      evcoId,
      roamingHubId.id,
      tokenType.id,
      tokenId,
      printedNumber,
      ExpiryDate.unapply(expiryDate),
      pin,
      PinMandatory.unapply(pinMandatory),
      TokenActivated.unapply(tokenActivated),
      hash.getOrElse("")
    )
  }
}