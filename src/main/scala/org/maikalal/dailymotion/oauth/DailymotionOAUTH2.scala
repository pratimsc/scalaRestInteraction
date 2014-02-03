package org.maikalal.dailymotion.oauth

import scala.collection.JavaConversions._

import dispatch._
import dispatch.Defaults._
import play.api.libs.json.Json

object DailymotionOAUTH2 {
  val _DailymotionAccessTokenURI = """https://api.dailymotion.com/oauth/token"""

  /**
   * Get Authorization code from Dailymotion
   */
  def borrowAccessToken(api: DailymotionAPICredential, scopes: List[String] = Nil, user: DailymotionEndUser) = {
    val req = url(_DailymotionAccessTokenURI).POST
    val header = Map("Content-Type" -> """application/x-www-form-urlencoded""")
    val query = Map("grant_type" -> "password",
      "client_id" -> api.apiKey,
      "client_secret" -> api.apiSecret,
      "username" -> user.username,
      "password" -> user.password.getOrElse(""),
      "scope" -> scopes.mkString(","))

    Http((req <:< header << query) OK as.String)
  }

  /**
   * Get Access token using a Refresh token from Daimotion
   */
  def renewAccessToken(api: DailymotionAPICredential, oauthToken: DailymotionOauthToken) = {
    val req = url(_DailymotionAccessTokenURI).POST
    val header = Map("Content-Type" -> """application/x-www-form-urlencoded""")
    val query = Map("grant_type" -> "refresh_token",
      "client_id" -> api.apiKey,
      "client_secret" -> api.apiSecret,
      "refresh_token" -> oauthToken.refresh_token)

    val res = Http((req <:< header << query) OK as.String)
    val renewedToken = for (r <- res) yield {
      val t = Json.parse(r).as[DailymotionOauthToken](DailymotionOauthToken.fmt)
      oauthToken.copy(access_token = t.access_token, refresh_token = t.refresh_token)
    }
    renewedToken
  }
}

/**
 * Class for Dailymotion OAUTH response
 */
case class DailymotionOauthToken(val access_token: String,
  val expires_in: Int,
  val refresh_token: String,
  val scope: Option[String],
  val uid: Option[String])

/**
 * Companion object for JSON
 */
object DailymotionOauthToken {
  implicit val fmt = Json.format[DailymotionOauthToken]
}

/**
 * Class for Dailymotion API Secret
 */
case class DailymotionAPICredential(val apiKey: String, val apiSecret: String)
/**
 * Companion object for JSON
 */
object DailymotionAPICredential {
  implicit val fmt = Json.format[DailymotionAPICredential]
}

/**
 * End user data
 */
case class DailymotionEndUser(val username: String, val password: Option[String])
object DailymotionEndUser {
  implicit val fmt = Json.format[DailymotionEndUser]
}


