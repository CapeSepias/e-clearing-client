package com.thenewmotion.chargenetwork.eclearing.client

import org.specs2.mutable.SpecificationWithJUnit
import com.thenewmotion.chargenetwork.eclearing.client.ssl.SslCertificateData

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

    "use HTTPS if certificate data given" >> {
      val client = new EclearingClient("", "", Some(SslCertificateData(() => this.getClass.getResourceAsStream("/test-clientcert.jks"), "dagdag",
                                                                       () => this.getClass.getResourceAsStream("/test-rootcert.jks"), "hoihoi")))
      client.soapBindings.baseAddress.toString must startWith("https://")
    }
  }
}