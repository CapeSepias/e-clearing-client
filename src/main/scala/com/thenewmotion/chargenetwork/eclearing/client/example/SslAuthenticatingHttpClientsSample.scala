package com.thenewmotion.chargenetwork.eclearing.client.example

import dispatch._
import com.thenewmotion.chargenetwork.eclearing.client.ssl.{SslCertificateData, SslAuthenticatingHttp}

object SslAuthenticatingHttpClientsSample extends App {
  if (args.size != 4) {
    System.err.println("Usage: SslAuthenticatingHttpClientsSample <client certificate keystore file> " +
                       "<client certificate keystore password> <root certificate keystore file> " +
                       "<root certificate keystore password>")
    System.exit(1)
  }

  val http = new SslAuthenticatingHttp(SslCertificateData(args(0), args(1), args(2), args(3)))

  val req = url("https://www.evclearinghouse.eu/service/?wsdl")
  val res: Promise[String] = http(req > as.String)
  val s = res()
  println(s)
}
