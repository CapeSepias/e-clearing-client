package com.thenewmotion.chargenetwork.eclearing.client

import org.specs2.mutable.SpecificationWithJUnit
import com.thenewmotion.chargenetwork.eclearing.client.ssl.SslCertificateData
import java.net.URI
import org.specs2.specification.Scope
import com.thenewmotion.chargenetwork.eclearing.{CDRInfo, ChargepointInfo}

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

    "receive cdrs" >> {
      client.cdrs()
      success
    }.pendingUntilFixed

    "add CDRs" >> {
      client.addCdrs(Seq(CDRInfo(
        cdrId = "01001851_139038927377890",
        startDatetime = "2014-01-22 10:16:38",
        endDatetime = "2014-01-22 12:14:33",
        duration = "7074",
        volume = "4.6400",
        chargePointAddress = "Roggestraat 111-163",
        chargePointZip = "7311",
        chargePointCity = "Apeldoorn",
        chargePointCountry = "NLD",
        chargePointType = "2",
        productType = "0",
        tariffType = "A0",
        authenticationId = "",
        evcoId = "NL-ANW-133094-6",
        meterId = "01000851",
        obisCode = "",
        chargePointId = "01000851",
        serviceProviderId = "ANWB",
        infraProviderId = "TheNewMotion",
        evseId = "01000851")))
      success
    }.pendingUntilFixed("CDR operations giving inexplicable errors")

    "receive roamingAuthorisationList" >> {
      client.roamingAuthorisationList()
      success
    }

    "receive chargepointList" >> {
      val cps = client.chargepointList()
      System.out.println(cps.mkString("\n"))
      success
    }

    "set charge point list" >> {
      client.setChargepointList(Seq(ChargepointInfo(evseId = "02000001",
        locationName = "The New Motion Office",
        locationNameLang = "en",
        houseNumber = "452",
        streetName = "Keizersgracht",
        city = "Amsterdam",
        postalCode = "1016 GD",
        taLat = "52.0",
        taLon = "5.0",
        taLatEntranceExit = "",
        taLonEntranceExit = "",
        openingTimes = "",
        powerOutletStatus = "in use",
        energyProviderId = "GreenChoice",
        roamingHubId = "The New Motion",
        telephoneNumber = "",
        floorLevel = "0",
        paymentMethod = "",
        evChargingReceptacleType = "Mennekes Type II"
      )))
      success
    }

    "use another URL if given" >> {
      val client = new EclearingClient("", "", None, Some(new URI("http://example.org/test_override")))

      client.soapBindings.baseAddress.toString mustEqual "http://example.org/test_override"
    }
  }
}
