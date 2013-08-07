package com.thenewmotion.chargenetwork.eclearing
package client.ssl

import java.io.{BufferedInputStream, FileInputStream, ByteArrayInputStream}
import dispatch.Http
import com.ning.http.client.{AsyncHttpClient, AsyncHttpClientConfig}
import javax.net.ssl.{TrustManagerFactory, KeyManagerFactory, SSLContext}
import java.security.KeyStore
import scalaxb.HttpClients

trait SslAuthenticatingHttpClients extends HttpClients {
  def certData: SslCertificateData

  private val http = new SslAuthenticatingHttp(certData)

  // trait below copy-pasted with small modifications from DispatchHttpClients which is among the generated sources
  val httpClient = new SslAuthenticatingHttpClient {}

  trait SslAuthenticatingHttpClient extends HttpClient {
    import dispatch._

    def request(in: String, address: java.net.URI, headers: Map[String, String]): String = {
      val req = url(address.toString) << in <:< headers
      val s = http(req OK as.String)
      s()
    }
  }
}

class SslAuthenticatingHttp(certData: SslCertificateData) extends Http {
  override val client = new AsyncHttpClient(
    (new AsyncHttpClientConfig.Builder).setSSLContext(buildSslContext(certData)).build
  )

  private def buildSslContext(certData: SslCertificateData): SSLContext = {
    import certData._

    val clientCertStore = loadKeyStore(clientCertificateData, clientCertificatePassword)
    val rootCertStore = loadKeyStore(rootCertificateData, rootCertificatePassword)

    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(clientCertStore, clientCertificatePassword.toCharArray)
    val keyManagers = keyManagerFactory.getKeyManagers()

    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(rootCertStore)
    val trustManagers = trustManagerFactory.getTrustManagers()

    val context = SSLContext.getInstance("TLS")
    context.init(keyManagers, trustManagers, null)

    context
  }

  private def loadKeyStore(keyStoreData: Array[Byte], password: String): KeyStore = {
    val store = KeyStore.getInstance(KeyStore.getDefaultType)
    store.load(new ByteArrayInputStream(keyStoreData), password.toCharArray)
    store
  }
}

case class SslCertificateData (
  clientCertificateData: Array[Byte],
  clientCertificatePassword: String,
  rootCertificateData: Array[Byte],
  rootCertificatePassword: String)

object SslCertificateData {
  def apply(clientCertFilename: String, clientCertPassword: String,
            rootCertFilename: String, rootCertPassword: String): SslCertificateData = {
    SslCertificateData(fileBytes(clientCertFilename), clientCertPassword,
                       fileBytes(rootCertFilename), rootCertPassword)
  }

  private def fileBytes(filename: String) = {
    val is = new BufferedInputStream(new FileInputStream(filename))
    try {
       Stream.continually(is.read).takeWhile(-1 !=).map(_.toByte).toArray
    } finally {
      is.close
    }
  }
}
