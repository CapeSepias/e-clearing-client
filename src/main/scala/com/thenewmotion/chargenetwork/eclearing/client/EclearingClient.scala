package com.thenewmotion.chargenetwork.eclearing
package client

import scalaxb._
import scalaxb.Soap11Fault
import com.typesafe.scalalogging.slf4j.Logging
import ssl.{SslAuthenticatingHttpClients, SslCertificateData}
import java.net.URI

/**
 * @param user The username for authenticating to e-clearing
 * @param password The password for authenticating to e-clearing
 * @param certificateData The SSL client certificate and root certificate data for authenticating to e-clearing. Will
 *                        only be used if the URI has https as the protocol. The URI has https, but if it is overridden
 *                        with an http URI the certificate data will not be used. If None is passed, no client
 *                        certificate is offered to the server.
 * @param uri The URI of the e-clearing web service. Pass None to use the default from the WSDL file.
 */
class EclearingClient(user: String, password: String,
                      certificateData: Option[SslCertificateData] = None,
                      uri: Option[URI] = None) extends Logging {

  import EclearingClient._

  private[client] lazy val soapBindings ={

    val sb = certificateData match {
      case None           => new CustomUriEchsSOAPBindings with Soap11Clients with DispatchHttpClients {
        def uriOverride = uri
      }
      case Some(data) => new CustomUriEchsSOAPBindings with Soap11Clients with SslAuthenticatingHttpClients {
        def uriOverride = uri
        def certData = data
      }
    }

    logger.debug("SOAP bindings created; base address: {}", sb.baseAddress)
    sb
  }

  private lazy val service = soapBindings.service
  lazy val authToken = new AuthToken()

  def addCdrs(cdrs: Seq[CDRInfo]) {
    logger.debug("Adding cdrs: %s".format(cdrs.mkString("\n", "\n", "\n")))
    send(service.addCDRs(AddCDRsRequest(cdrs: _*), _))
  }

  def clearCdrs() {
    logger.debug("Clearing cdrs")
    send(service.clearCDRs(ClearCDRsRequest(), _))
  }

  def cdrs(): Seq[CDRInfo] = {
    logger.debug("Get cdrs")
    receive(service.getCDRs(GetCDRsRequest(), _)).cdrInfoArray
  }

  def setChargepointList(list: Seq[ChargepointInfo]) {
    logger.debug("Set charge point list: " + list.mkString(", "))
    send(service.setChargepointList(SetChargepointListRequest(list: _*), _))
  }

  def chargepointList(): Seq[ChargepointInfo] = {
    logger.debug("Get chargepoint list")
    receive(service.getChargepointList(GetChargepointListRequest(), _)).chargepointInfoArray
  }

  def setRoamingAuthorisationList(list: Seq[RoamingAuthorisationInfo]) {
    logger.debug("Set roaming authorisation list: " + list.mkString(", "))
    send(service.setRoamingAuthorisationList(SetRoamingAuthorisationListRequest(list: _*), _))
  }

  def roamingAuthorisationList(): Seq[RoamingAuthorisationInfo] = {
    logger.debug("Get roaming authorisation list")
    val roamingAuthorisation = service.getRoamingAuthorisationList(GetRoamingAuthorisationListRequest(), _: String)
    receive(roamingAuthorisation).roamingAuthorisationInfoArray
  }

  private def authorized[T](func: String => T)(result: T => Result): T = {
    def loop(retry: Boolean): T = {
      val token = authToken()
      val res = func(token)
      val code = result(res).resultCode
      AuthResultType(code) match {
        case AuthAccepted => res
        case AuthDenied if retry =>
          logger.debug("Auth token %s denied, retrying".format(token))
          authToken.receiveNewToken()
          loop(retry = false)
        case AuthDenied => throw new EclearingException("Auth token %s is incorrect".format(token))
      }
    }
    loop(retry = true)
  }

  type Response = {def result: Result}

  private def receive[T <: Response](func: String => Either[Soap11Fault[Any], T]): T = {
    authorized {
      token => rightOrError(func(token))
    }(_.result)
  }

  private def send(func: String => Either[Soap11Fault[Any], Result]) {
    authorized {
      token => rightOrError(func(token))
    }(identity)
  }

  @throws(classOf[RuntimeException])
  private def rightOrError[T](func: => Either[Soap11Fault[Any], T]): T = func match {
    case Right(right) => right
    case Left(left) =>
      logger.warn("SOAP error: {}, details: {}", left, left.detail)
      throw new EclearingException(left.original.toString)
  }

  class AuthToken {
    private var _token = authenticate()

    def apply() = _token

    def authenticate(): String = {
      val res = rightOrError {
        service.authenticate(user, password)
      }
      AuthResultType(res.resultCode) match {
        case AuthAccepted => res.authToken match {
          case Some(t) => logger.debug("Received new auth token: " + t); t
          case None => throw new EclearingException("No token received")
        }
        case AuthDenied => throw new EclearingException("Login or password is incorrect")
      }
    }

    def receiveNewToken() {
      logger.debug("Receiving new auth token")
      _token = authenticate()
    }
  }

}

object EclearingClient {

  object AuthResultType {
    def apply(code: Int): AuthResultType = code match {
      case 0 => AuthAccepted
      case 1 => AuthDenied
      case x => throw new EclearingException("Illegal AuthResultType: " + x)
    }
  }

  sealed trait AuthResultType
  case object AuthAccepted extends AuthResultType
  case object AuthDenied extends AuthResultType
}

class EclearingException(msg: String) extends RuntimeException(msg)