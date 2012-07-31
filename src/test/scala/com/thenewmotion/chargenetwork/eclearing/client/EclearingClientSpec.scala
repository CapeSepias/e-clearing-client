package com.thenewmotion.chargenetwork.eclearing.client

import org.specs2.mutable.SpecificationWithJUnit

/**
 * @author Yaroslav Klymko
 */
class EclearingClientSpec extends SpecificationWithJUnit {
  args(sequential = true)

  val client = new EclearingClient("", "")

  "EclearingClient" should {
    "receive token" >> {
      client.authToken() must not beEmpty
    }

    "receive cdrs" >> {
      client.cdrs()
      success
    }

    "receive roamingAuthorisationList" >> {
      client.roamingAuthorisationList()
      success
    }

    "receive chargepointList" >> {
      client.chargepointList()
      success
    }
  }
}