package com.thenewmotion.chargenetwork.eclearing

import api.{EclearingApi, Card}
import client.EclearingClient


/**
 * @author Yaroslav Klymko
 */
trait EclearingService extends EclearingApi {
  def client: EclearingClient

  import Converters._
  def sendCards(cards: Seq[Card]): Result = client.setRoamingAuthorisationList(cards.map(implicitly[RoamingAuthorisationInfo](_)))
  def cards() = client.roamingAuthorisationList().map(implicitly[Card](_))
}