package com.thenewmotion.chargenetwork.eclearing
package client

import scalaxb.Soap11Clients
import java.net.URI

trait CustomUriEchsSOAPBindings extends EchsSOAPBindings {
  this: Soap11Clients =>

  override def baseAddress = uriOverride getOrElse super.baseAddress

  def uriOverride: Option[URI]
}
