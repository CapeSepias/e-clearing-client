package com.thenewmotion.chargenetwork.eclearing

import api.{EclearingApi, Card}
import client.EclearingClient


/**
 * @author Yaroslav Klymko
 */
trait EclearingService extends EclearingApi {
  def client: EclearingClient

  import Converters._
  def addCard(card: Card) { client.setRoamingAuthorisationList(Seq(card)) }
  def removeCard(card: Card) { client.setRoamingAuthorisationList(Seq(card.copy(tokenActivated = false))) }
  def cards() = client.roamingAuthorisationList().map(implicitly[Card](_))
}