package org.maikalal.seccam.videos

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.File
import org.maikalal.seccam.utils.Util
import org.maikalal.dailymotion.oauth.DailymotionOauthToken
import org.maikalal.dailymotion.oauth.DailymotionAPICredential
import play.api.libs.json.JsValue
import org.maikalal.dailymotion.video.DailymotionVideo
import play.api.libs.json.Json

object SecCamVideoProcessor {

  /**
   * Get the file of videos is the given folder
   */

  def getListOfVideos(f: File): Future[List[File]] = {
    val promise = Promise[List[File]]
    future {
      val files = Util.extractFilesFromFolder(f)
      val videoFiles = files.filter(isVideoFile(_))
      promise success videoFiles
    }
    promise.future
  }

  /**
   * Check whether file is video file or not
   */
  def isVideoFile(f: File): Boolean = true

  /**
   * Upload a Single video files to Internet website Dailymotion.com
   */

  def uploadSingleVideosToDailymotion(oauth: DailymotionOauthToken, video: File)(implicit conf: SecCamVideoUploadSettings) =
    (video, DailymotionVideo.uploadSingleVideoToDailymotion(oauth, video))

  /**
   * Upload all video files to Internet website Dailymotion.com
   */

  def uploadVideosToDailymotion(oauth: DailymotionOauthToken, videos: List[File])(implicit conf: SecCamVideoUploadSettings) =
    videos.map(uploadSingleVideosToDailymotion(oauth, _))

  /**
   * Check whether a video file is already uploaded.
   * If uploaded delete the file, else leave it for next attempt
   */

  def deleteVideoFileAlreadyUploaded(video: File, videoUploadAttempt: Future[JsValue]) = videoUploadAttempt.map(j => Util.deleteFile(video))

}