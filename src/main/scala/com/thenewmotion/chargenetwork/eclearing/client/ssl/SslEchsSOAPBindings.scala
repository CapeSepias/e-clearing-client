package com.thenewmotion.chargenetwork.eclearing
package client.ssl

import scalaxb.Soap11Clients

trait HttpsEchsSOAPBindings extends EchsSOAPBindings {
  this: Soap11Clients =>

  override def baseAddress = new java.net.URI("https://www.evclearinghouse.eu/service/index.php")
}
