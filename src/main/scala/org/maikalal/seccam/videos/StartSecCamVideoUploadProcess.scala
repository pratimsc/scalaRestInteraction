package org.maikalal.seccam.videos

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import java.io.File
import org.maikalal.dailymotion.oauth.DailymotionOauthToken
import play.api.libs.json.Json
import scala.language.postfixOps
import org.maikalal.seccam.utils.Util
import java.nio.charset.StandardCharsets
import org.maikalal.dailymotion.oauth.DailymotionOAUTH2
import org.maikalal.dailymotion.oauth.DailymotionEndUser
import org.maikalal.dailymotion.oauth.DailymotionAPICredential

object StartSecCamVideoUploadProcess extends Logging {

  def main(args: Array[String]): Unit = {
    //require(args.size > 1)
    //Read the configuration file 
    val configFile = args(0)
    logger.info(s"Configuration file being read is [${configFile}]")
    implicit val conf = SecCamVideoUploadSettings(ConfigFactory.load(configFile))
    implicit val codec = StandardCharsets.UTF_8
    logger.info(s"All videos in the folder[${conf.securityCameraVideosSourceFolder}] will be attempted for publishing to Internet.")

    //Get the initial OAUTH token
    val oauthJsonFile = new File(conf.dailymotionApiOauth2AccessTokenDownloadJsonFile)
    val oauth = if (oauthJsonFile.exists()) {
      logger.info(s"Reading the oauth2 information from file [${oauthJsonFile}]")
      val json = Util.readFromFile(oauthJsonFile).mkString("")
      Json.parse(json).as[DailymotionOauthToken]
    } else {
      logger.info(s"Requesting a new OAUTH token from daily motion")
      val api = DailymotionAPICredential(conf.dailymotionApiKey, conf.dailymotionApiSecret)
      val user = DailymotionEndUser(conf.dailymotionAccountUserId, Some(conf.dailymotionAccountUserPassword))
      val newOauth = Await.result(DailymotionOAUTH2.borrowAccessToken(api, conf.dailymotionApiVideoUploadScope, user), 1 minute)
      logger.info(s"Recieved a new OAUTH token from daily motion")
      logger.info(s"Persist the a new OAUTH token at location [${conf.dailymotionApiOauth2AccessTokenDownloadJsonFile}]")
      Util.writeToFile(oauthJsonFile)(p => p.write(Json.stringify(Json.toJson(newOauth))))
      newOauth
    }

    logger.info(s"Start PUBLISHING videos to Internet.")
    publishToInternet(oauth)

  }

  def publishToInternet(oauth: DailymotionOauthToken, sleepTime: Duration = 60 seconds)(implicit conf: SecCamVideoUploadSettings) {    
    logger.info(s"Checking for new videos for upload at [${conf.securityCameraVideosSourceFolder}].")
    val videos = Await.result(SecCamVideoProcessor.getListOfVideos(new File(conf.securityCameraVideosSourceFolder)), 5 seconds)
    
    if (videos.size > 0) {
      logger.info(s"Recieved new batch of [${videos.size}]videos to upload from [${conf.securityCameraVideosSourceFolder}].")
      val uploadActivity = future {
        val results = SecCamVideoProcessor.uploadVideosToDailymotion(oauth, videos)
        results.map {
          case (v, pubF) =>
            SecCamVideoProcessor.deleteVideoFileAlreadyUploaded(v, pubF)
            pubF onSuccess {
              case pub => logger.info(s"Successfully published video [${v.getCanonicalPath()}] and the published video metadata is [${Json.stringify(pub)}]")
            }
            pubF onFailure {
              case err => logger.error(s"Some error occured during upload of video [${v.getCanonicalPath()}]", err)
            }
        }
        logger.info(s"Request for processing the batch finished.")
      }
      Await.result(uploadActivity, 5 minutes)
      logger.info(s"Processing the batch FINISHED.")
    }else{
      logger.info(s"NO new batch of videos are present at [${conf.securityCameraVideosSourceFolder}].")
    }
    logger.info(s"Waiting for [${sleepTime.toMillis}] millis or [${sleepTime}] before polling for next batch of videos.")
    Thread.sleep(sleepTime.toMillis)
    publishToInternet(oauth, sleepTime)
  }

}