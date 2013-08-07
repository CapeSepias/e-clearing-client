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
      def resourceBytes(resourceName: String): Array[Byte] = {
        val bs = this.getClass.getResourceAsStream(resourceName)
        Stream.continually(bs.read).takeWhile(-1 !=).map(_.toByte).toArray
      }

      val client = new EclearingClient("", "", Some(SslCertificateData(resourceBytes("/test-clientcert.jks"), "dagdag",
                                                                       resourceBytes("/test-rootcert.jks"), "hoihoi")))
      client.soapBindings.baseAddress.toString must startWith("https://")
    }
  }
}