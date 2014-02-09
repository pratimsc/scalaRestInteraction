package org.maikalal.dailymotion.video

import play.api.libs.json.Json
import org.maikalal.dailymotion.oauth._
import scala.concurrent._
import scala.concurrent.duration._
import dispatch._
import dispatch.Defaults._
import scala.language.postfixOps
import java.io.File
import scala.io.Source
import java.nio.charset.StandardCharsets
import org.maikalal.seccam.utils.Util
import com.typesafe.config.ConfigFactory
import org.maikalal.seccam.videos.SecCamVideoUploadSettings

object VideoUploadTest extends App {
  implicit val codec = StandardCharsets.UTF_8
  implicit val conf = SecCamVideoUploadSettings(ConfigFactory.load("ref-video-upload.config"))

  val oauthJsonFile = new File("""/tmp/oauth2.json""")
  val apiKey = new DailymotionAPICredential(apiKey = conf.dailymotionApiKey, apiSecret = conf.dailymotionApiSecret)
  val video = new File("""/tmp/VID_20140203_013501.mp4""")  
  val oauthOld = Json.parse(Util.readFromFile(oauthJsonFile).mkString(" ")).as[DailymotionOauthToken]
  //Refresh token
  println("Original Access Token is ->\n" -> Json.prettyPrint(Json.toJson(oauthOld)))
  val oauthF = DailymotionOAUTH2.renewAccessToken(apiKey, oauthOld)
  oauthF onSuccess {
    case oauth =>
      //Store the new JSON Approval key
      Util.writeToFile(oauthJsonFile)(out => out.write(Json.stringify(Json.toJson(oauth))))
  }
  val videoId = oauthF.flatMap(oauth => DailymotionVideo.uploadSingleVideoToDailymotion(oauth, video))

  videoId onSuccess {
    case data => println("SUCCESS -> Video Id ->" + Json.prettyPrint(data))
  }
  videoId onFailure {
    case err => println("FAILURE -> Video Id Error->" + err.getMessage())
  }
}