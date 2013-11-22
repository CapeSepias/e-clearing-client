package com.thenewmotion.chargenetwork.eclearing
package client.example

import dispatch._
import client.ssl.{SslCertificateData, SslAuthenticatingHttp}
import scala.concurrent.ExecutionContext.Implicits.global

object SslAuthenticatingHttpClientsSample extends App {
  if (args.size != 4) {
    System.err.println("Usage: SslAuthenticatingHttpClientsSample <client certificate keystore file> " +
                       "<client certificate keystore password> <root certificate keystore file> " +
                       "<root certificate keystore password>")
    System.exit(1)
  }

  val http = new SslAuthenticatingHttp(SslCertificateData(args(0), args(1), args(2), args(3)))

  val req = url("https://q.ochp.e-clearing.net/ochp/service/index.php?wsdl")
  val res: Future[String] = http(req > as.String)
  val s = res()
  println(s)
}
