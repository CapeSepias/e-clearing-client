package com.thenewmotion.chargenetwork.eclearing

import scalax.StringOption

object RoamingAuthorisationMock {
  lazy val CSV = {
    val is = getClass.getResourceAsStream("roaming-authorisation-list.csv")
    val lines = io.Source.fromInputStream(is, "UTF-8").getLines()
    lines.map {
      line =>
        val values = line.split(",").map(StringOption(_).getOrElse(""))
        RoamingAuthorisationInfo(values(0),
          values(1).toInt,
          values(2).toInt,
          values(3),
          values(4),
          values(5),
          values(6).toInt,
          values(7).toInt,
          values(8).toInt,
          if (values.length == 8) values(9) else ""
        )
    }.toArray.toSeq
  }
}