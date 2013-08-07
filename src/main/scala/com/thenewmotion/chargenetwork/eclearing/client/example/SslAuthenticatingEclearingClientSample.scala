package com.thenewmotion.chargenetwork.eclearing.client.example

import com.thenewmotion.chargenetwork.eclearing.client.ssl.SslCertificateData
import com.thenewmotion.chargenetwork.eclearing.client.EclearingClient

object SslAuthenticatingEclearingClientSample extends App {
  if (args.size != 6) {
    System.err.println("Usage: SslAuthenticatingEclearingClientSample <client certificate keystore file> " +
                       "<client certificate keystore password> <root certificate keystore file> " +
                       "<root certificate keystore password> <eclearing username> <eclearing password>")
    System.exit(1)
  }

  val certData = SslCertificateData(args(0), args(1),
                                    args(2), args(3))
  val client = new EclearingClient(args(4), args(5), Some(certData))

  println(client.chargepointList())
}
