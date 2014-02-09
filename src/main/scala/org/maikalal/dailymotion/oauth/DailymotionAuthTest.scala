package org.maikalal.dailymotion.oauth

import dispatch.Defaults.executor
import play.api.libs.json.Json
import java.io.File
import org.maikalal.seccam.utils.Util
import org.maikalal.seccam.videos.SecCamVideoUploadSettings
import com.typesafe.config.ConfigFactory

object DailymotonAuthTest extends App {
  implicit val conf = SecCamVideoUploadSettings(ConfigFactory.load("ref-video-upload.config")) 
  val api = new DailymotionAPICredential(apiKey = conf.dailymotionApiKey,
    apiSecret = conf.dailymotionApiSecret)
  val user = new DailymotionEndUser(username = conf.dailymotionAccountUserId, password = Option(conf.dailymotionAccountUserPassword))
  val scopes = conf.dailymotionApiVideoUploadScope
  val oauthF = DailymotionOAUTH2.borrowAccessToken(api = api, user = user, scopes = scopes)
  oauthF onSuccess {
    case oauth =>
      println(Json.prettyPrint(Json.toJson(oauth)))
      val f = new File("/tmp/oauth2.json")
      Util.writeToFile(f)(_.print(Json.stringify(Json.toJson(oauth))))
  }
  oauthF onFailure {
    case err =>
      println("Some error has occured ->" + err.getMessage())
  }
}
