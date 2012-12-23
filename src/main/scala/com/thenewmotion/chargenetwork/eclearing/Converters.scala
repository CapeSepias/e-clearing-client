package com.thenewmotion.chargenetwork.eclearing

import api._
import scalax.StringOption

/**
 * @author Yaroslav Klymko
 */
object Converters{
  implicit def roamingAuthorisationInfoToCard(rai: RoamingAuthorisationInfo): Card = {
    import rai._
    Card(StringOption(evcoId).getOrElse(""),
      RoamingHubId(roamingHubId),
      TokenType(tokenType),
      tokenId.toUpperCase,
      StringOption(serviceProvider),
      printedNumber,
      ExpiryDate(expiryDate),
      pin,
      PinMandatory(pinMandatory),
      TokenActivated(tokenActivated),
      StringOption(hash)
    )
  }

  implicit def cardToRoamingAuthorisationInfo(card: Card): RoamingAuthorisationInfo = {
    import card._
    RoamingAuthorisationInfo(
      evcoId,
      roamingHubId.id,
      tokenType.id,
      tokenId,
      serviceProvider getOrElse "",
      printedNumber,
      ExpiryDate.unapply(expiryDate),
      pin,
      PinMandatory.unapply(pinMandatory),
      TokenActivated.unapply(tokenActivated),
      hash getOrElse ""
    )
  }
}