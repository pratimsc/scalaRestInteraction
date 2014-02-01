package org.maikalal.google.oauth

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

object GoogleOauth2 {
  val _SecurityProvider = new BouncyCastleProvider
  val _SignatureAlgorithm = "SHA256withRSA"
  val _base64Encoder = new BASE64Encoder

  /**
   * Generate the JWT header as required by Google.
   * Its basically a constant string of -
   * {"alg":"RS256","typ":"JWT"}
   */
  val generateJwtHeader = Json.obj(
    "alg" -> "RS256",
    "typ" -> "JWT")

  /**
   * Generate the JWT claim set.
   * This one will vary depending on the permission requested by the requester.
   *
   */
  def generateJwtClaimSet(jwtClaimSet: GoogleJwtClaimSet) = Json.obj(
    "iss" -> jwtClaimSet.iss,
    "scope" -> jwtClaimSet.scope.map(_.toASCIIString()).mkString(" "),
    "aud" -> jwtClaimSet.aud.toASCIIString(),
    "exp" -> (jwtClaimSet.iat.getMillis() / 1000 + 3600),
    "iat" -> jwtClaimSet.iat.getMillis() / 1000)

  /**
   * Generate the JWT for signing
   */
  def generateFullJWTwithHeader(jwtClaimSet: GoogleJwtClaimSet) = generateJwtHeader.toString + "." + generateJwtClaimSet(jwtClaimSet).toString

  /**
   * Generate Signed JWT string
   */
  def generateSignedJWTwithBase64Encoding(jwtClaimSet: GoogleJwtClaimSet, keyStore: File, keyStoreType: String = "pkcs12", keyStorePassword: String = "notasecret") = {
    val jwtHeader = generateJwtHeader.toString
    val jwtBody = generateJwtClaimSet(jwtClaimSet).toString
    val dataToBeSigned = _base64Encoder.encode(jwtHeader.getBytes(StandardCharsets.UTF_8)) + "." + _base64Encoder.encode(jwtBody.getBytes(StandardCharsets.UTF_8))
    val signature = digitallySignData(dataToBeSigned.getBytes(), extractPrivateKey(keyStore, keyStoreType, keyStorePassword))
    _base64Encoder.encode(jwtHeader.getBytes()) + "." + _base64Encoder.encode(jwtBody.getBytes()) + "." + _base64Encoder.encode(signature)
  }

  /**
   * Extracts the private key from PKCS12 file
   */
  def extractPrivateKey(keyStore: File, keyStoreType: String = "pkcs12", keyStorePassword: String = "notasecret") = {
    val ks = KeyStore.getInstance(keyStoreType, new BouncyCastleProvider)
    ks.load(new FileInputStream(keyStore), keyStorePassword.toCharArray())
    val alias = ks.aliases().nextElement()
    val pvtKey = ks.getKey(alias, keyStorePassword.toCharArray())
    pvtKey.asInstanceOf[PrivateKey]
  }

  /**
   * Extracts the Public Cert from PKCS12 file
   */
  def extractPublicCert(keyStore: File, keyStoreType: String = "pkcs12", keyStorePassword: String = "notasecret") = {
    val ks = KeyStore.getInstance(keyStoreType, new BouncyCastleProvider)
    ks.load(new FileInputStream(keyStore), keyStorePassword.toCharArray())
    val alias = ks.aliases().nextElement()
    val cert = ks.getCertificate(alias)
    cert.asInstanceOf[X509Certificate]
  }

  /**
   * Digitally Sign data using
   */
  def digitallySignData(data: Array[Byte], privateKey: PrivateKey) = {
    val sig = Signature.getInstance(_SignatureAlgorithm, _SecurityProvider)
    sig.initSign(privateKey)
    sig.update(data)
    sig.sign()
  }

  /**
   * Generate CMS data from signature
   */
  def generateCMSSignedData(signature: Array[Byte], cert: X509Certificate, privateKey: PrivateKey) = {
    val jcaContSigBuilder = new JcaContentSignerBuilder(_SignatureAlgorithm).setProvider(_SecurityProvider)
    val sha256Signer: ContentSigner = jcaContSigBuilder.build(privateKey)
    val jcaDigCalProvider = new JcaDigestCalculatorProviderBuilder().setProvider(_SecurityProvider).build()
    val cmsGen = new CMSSignedDataGenerator
    cmsGen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(jcaDigCalProvider).build(sha256Signer, cert))
    cmsGen.addCertificates(new JcaCertStore(List(cert)))
    val cmsSignature = cmsGen.generate(new CMSProcessableByteArray(signature), false)
    cmsSignature
  }

  /**
   * Get Authorization code from Google
   */
  def getGoogleOAUTH2AuthorizationCode(jwtClaimSet: GoogleJwtClaimSet, keyStore: File, keyStoreType: String = "pkcs12", keyStorePassword: String = "notasecret") = {
    val grant_type = """urn:ietf:params:oauth:grant-type:jwt-bearer"""
    val assertion = generateSignedJWTwithBase64Encoding(jwtClaimSet, keyStore, keyStoreType, keyStorePassword)
    val googleOauth2Url = """https://accounts.google.com/o/oauth2/token"""
    val req = url(googleOauth2Url.toString()).POST
    val queryMap = Map("grant_type" -> grant_type, "assertion" -> assertion)
    val header = Map("Host" -> "accounts.google.com", "Content-Type" -> """application/x-www-form-urlencoded""")

    Http((req <:< header << queryMap))
  }

}

case class GoogleJwtClaimSet(val iss: String,
  val scope: List[URI],
  val aud: URI = new URI("https://accounts.google.com/o/oauth2/token"),
  val iat: DateTime = new DateTime())