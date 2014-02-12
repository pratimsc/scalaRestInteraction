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

  def getListOfVideos(f: File)(implicit conf: SecCamVideoUploadSettings): Future[List[File]] = {
    val promise = Promise[List[File]]
    future {
      val files = Util.extractFilesFromFolder(f)
      val videoFiles = files.filter(isVideoFile(_))
      promise success videoFiles
      
      /*
       * Delete the files that are not video files
       * Its best to do it here, so that when the function is called again next time,
       * it does not have to re-process the non-video files.
       */
      val nonVideoFile = files.filter(!isVideoFile(_))
      future {
        nonVideoFile.map(_.delete())
      }
    }
    promise.future
  }

  /**
   * Check whether file is video file or not
   * The binary is not checked, the decision is made on the file extensions only.
   */
  def isVideoFile(f: File)(implicit conf: SecCamVideoUploadSettings): Boolean = {
    conf.approvedVideoFileExtensions.filter(f.getName().toLowerCase().endsWith(_)) match {
      case h :: tail =>
        //Has extension that matches with most common video formats. So probably a video file.
        true
      case _ => false
    }
  }

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
