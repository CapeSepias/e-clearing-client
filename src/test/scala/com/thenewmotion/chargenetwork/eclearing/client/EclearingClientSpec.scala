package com.thenewmotion.chargenetwork.eclearing.client

import org.specs2.mutable.SpecificationWithJUnit
import com.thenewmotion.chargenetwork.eclearing.client.ssl.SslCertificateData
import java.net.URI
import org.specs2.specification.Scope

/**
 * @author Yaroslav Klymko
 */
class EclearingClientSpec extends SpecificationWithJUnit {
  args(sequential = true)

  val client = new EclearingClient("", "", Some(SslCertificateData("", "", "", "")))

  "EclearingClient" should {

    "receive token" >> {
      client.authToken() must not beEmpty
    }

    /* Seems to be disallowed for our account as of 2013-11-22 -- Reinier Lamers
    "receive cdrs" >> {
      client.cdrs()
      success
    }
    */

    "receive roamingAuthorisationList" >> {
      client.roamingAuthorisationList()
      success
    }

    "receive chargepointList" >> {
      client.chargepointList()
      success
    }

    "use another URL if given" >> {
      val client = new EclearingClient("", "", None, Some(new URI("http://example.org/test_override")))

      client.soapBindings.baseAddress.toString mustEqual "http://example.org/test_override"
    }
  }
}