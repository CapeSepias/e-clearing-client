package com.thenewmotion.chargenetwork.eclearing
package client.ssl

import java.io.{InputStream, FileInputStream}
import dispatch.Http
import com.ning.http.client.{AsyncHttpClient, AsyncHttpClientConfig}
import javax.net.ssl.{TrustManagerFactory, KeyManagerFactory, SSLContext}
import java.security.KeyStore
import scalaxb.HttpClients
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait SslAuthenticatingHttpClients extends HttpClients {
  def certData: SslCertificateData

  private val http = new SslAuthenticatingHttp(certData)

  // trait below copy-pasted with small modifications from DispatchHttpClients which is among the generated sources
  val httpClient = new SslAuthenticatingHttpClient {}

  trait SslAuthenticatingHttpClient extends HttpClient {
    import dispatch._

    def request(in: String, address: java.net.URI, headers: Map[String, String]): String = {
      val req = url(address.toString) << in <:< headers
      val future = http(req OK as.String)
      Await.result(future, 10.seconds)
    }
  }
}

class SslAuthenticatingHttp(certData: SslCertificateData) extends Http {
  override val client = new AsyncHttpClient(
    (new AsyncHttpClientConfig.Builder).setSSLContext(buildSslContext(certData)).build
  )

  private def buildSslContext(certData: SslCertificateData): SSLContext = {
    import certData._

    val clientCertStore = loadKeyStore(clientCertificateStream, clientCertificatePassword)
    val rootCertStore = loadKeyStore(rootCertificateStream, rootCertificatePassword)

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

  private def loadKeyStore(keyStoreStream: () => InputStream, password: String): KeyStore = {
    val store = KeyStore.getInstance(KeyStore.getDefaultType)
    val inputStream = keyStoreStream()
    try {
      store.load(keyStoreStream(), password.toCharArray)
    } finally {
      inputStream.close
    }
    store
  }
}

case class SslCertificateData (
  clientCertificateStream: () => InputStream,
  clientCertificatePassword: String,
  rootCertificateStream: () => InputStream,
  rootCertificatePassword: String)

object SslCertificateData {
  def apply(clientCertFilename: String, clientCertPassword: String,
            rootCertFilename: String, rootCertPassword: String): SslCertificateData = {
    SslCertificateData(lazyOpen(clientCertFilename), clientCertPassword,
                       lazyOpen(rootCertFilename), rootCertPassword)
  }

  private def lazyOpen(filename: String): () => InputStream =
    () =>  new FileInputStream(filename)
}
