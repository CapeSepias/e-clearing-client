package com.thenewmotion.chargenetwork.eclearing
package client.ssl

import scalaxb.Soap11Clients

class SslEchsSOAPBindings(certData: SslCertificateData) extends SslAuthenticatingHttpClients(certData)
                                                   with Soap11Clients with EchsSOAPBindings {
  override def baseAddress = new java.net.URI("https://www.evclearinghouse.eu/service/index.php")
}
