package com.thenewmotion.chargenetwork.eclearing
package client

import scalaxb._
import org.slf4j.LoggerFactory
import scalaxb.Soap11Fault

/**
 * @author Yaroslav Klymko
 */
class EclearingClient(user: String, password: String) {

  import EclearingClient._

  val log = LoggerFactory.getLogger(getClass)
  private lazy val service = (new EchsSOAPBindings with Soap11Clients with FixedDispatchHttpClients).service
  lazy val authToken = new AuthToken()

  def addCdrs(cdrs: Seq[CDRInfo]) {
    send(service.addCDRs(AddCDRsRequest(cdrs: _*), _))
  }

  def clearCdrs() {
    send(service.clearCDRs(ClearCDRsRequest(), _))
  }

  def cdrs(): Seq[CDRInfo] = {
    receive(service.getCDRs(GetCDRsRequest(), _)).cdrInfoArray
  }

  def setChargepointList(list: Seq[ChargepointInfo]) {
    log.debug("Set charge point list: " + list.mkString(", "))
    send(service.setChargepointList(SetChargepointListRequest(list: _*), _))
  }

  def chargepointList(): Seq[ChargepointInfo] = {
    receive(service.getChargepointList(GetChargepointListRequest(), _)).chargepointInfoArray
  }

  def setRoamingAuthorisationList(list: Seq[RoamingAuthorisationInfo]) {
    log.debug("Set roaming authorisation list: " + list.mkString(", "))
    send(service.setRoamingAuthorisationList(SetRoamingAuthorisationListRequest(list: _*), _))
  }

  def roamingAuthorisationList(): Seq[RoamingAuthorisationInfo] = {
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
          log.info("Auth token %s denied, retrying".format(token))
          authToken.receiveNewToken()
          loop(retry = false)
        case AuthDenied => sys.error("EclearingClient: Auth tooken %s is incorrect".format(token))
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
      log.warn(left.toString)
      sys.error("EclearingClient: " + left.original.toString)
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
          case Some(t) =>
            log.debug("Received new auth token: " + t)
            t
          case None => sys.error("EclearingClient: no token received")
        }
        case AuthDenied => sys.error("EclearingClient: login or password is incorrect")
      }
    }

    def receiveNewToken() {
      log.debug("Receiving new auth token")
      _token = authenticate()
    }
  }

}

object EclearingClient {

  object AuthResultType {
    def apply(code: Int): AuthResultType = code match {
      case 0 => AuthAccepted
      case 1 => AuthDenied
      case x => sys.error("EclearingClient: Illegal AuthResultType: " + x)
    }
  }

  sealed trait AuthResultType
  case object AuthAccepted extends AuthResultType
  case object AuthDenied extends AuthResultType
}