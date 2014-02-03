package org.maikalal.dailymotion.oauth

import dispatch._
import dispatch.Defaults._
import java.net.URI
import org.joda.time.DateTime
import play.api.libs.json.Json
import java.security.Security
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jcajce.provider.keystore.PKCS12
import org.bouncycastle.jce.PKCS12Util
import java.io.File
import java.security.KeyStore
import java.io.FileInputStream
import org.bouncycastle.asn1.pkcs.Pfx
import java.security.Signature
import java.security.PrivateKey
import java.security.cert.X509Certificate
import org.bouncycastle.cms.CMSProcessableByteArray
import org.bouncycastle.util.Store
import org.bouncycastle.cert.jcajce.JcaCertStore
import collection.JavaConversions._
import org.bouncycastle.cms.CMSSignedDataGenerator
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder
import org.bouncycastle.operator.ContentSigner
import sun.misc.BASE64Encoder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object DailymotionAuthHelper {
  val _DailymotionAccessTokenURI = """https://api.dailymotion.com/oauth/token"""

  /**
   * Get Authorization code from Dailymotion
   */
  def borrowAccessToken(clientId: String, clientSecret: String, username: String, password: String) = {
    val req = url(_DailymotionAccessTokenURI).POST
    val header = Map("Content-Type" -> """application/x-www-form-urlencoded""")
    val query = Map("grant_type" -> "password",
      "client_id" -> clientId,
      "client_secret" -> clientSecret,
      "username" -> username,
      "password" -> password)
    Http((req <:< header << query) OK as.String)
  }
}

/**
 * Class for Dailymotion OAUTH response
 */
case class DailymotionOauthToken(val access_token: String,
  val expires_in: Int,
  val refresh_token: String,
  val scope: String,
  val uid: String)

/**
 * Companion object for JSON
 */
object DailymotionOauthToken {
  implicit val fmt = Json.format[DailymotionOauthToken]
}
