package org.maikalal.dailymotion.video

import org.maikalal.dailymotion.oauth.DailymotionOauthToken
import dispatch._
import dispatch.Defaults._
import play.api.libs.json.Json
import java.io.File
import com.ning.http.client.AsyncHttpClient
import com.ning.http.multipart.FilePart
import com.ning.http.client.Response
import scala.util.Try
import scala.concurrent.future
import scala.concurrent.Promise

object DailymotionVideo {

  val _VideoUploadURI = "https://api.dailymotion.com/file/upload"
  val _VideoListURI = "https://api.dailymotion.com/videos"
  def _VideoSingleURI(id: String) = s"https://api.dailymotion.com/video/${id}"

  /**
   * Fetch the video upload url from Dailymotion.
   * The videos have to be POSTed to the upload url.
   */
  def fetchVideoUploadUrls(oauth: DailymotionOauthToken): Future[Either[Throwable, Map[String, String]]] = {
    val req = url(_VideoUploadURI).GET
    val params = Map("access_token" -> oauth.access_token)
    val res = Http((req <<? params) OK as.String)
    res map { r =>
      val json = Json.parse(r)
      val upload_url = json \ "upload_url"
      val progress_url = json \ "progress_url"
      Right(Map("upload_url" -> upload_url.as[String], "progress_url" -> progress_url.as[String]))
    } recover {
      case err => Left(err)
    }
  }

  /**
   * Post the video to the Dailymotion.
   * Post upload, Dailymotion returns an approval url.
   * Unless a POST request is sent to the url, the video does not get registered with the user's library.
   * The POST request to the approval url must follow immediately.
   */

  def uploadVideo(uploadUrl: String, video: File): Future[String] = {
    val promise = Promise[String]
    future {
      val header = Map("Content-Type" -> "multipart/form-data")
      val body = Map("file" -> video)
      promise complete Try {
        val asyncHttpClient = new AsyncHttpClient
        val postRequest = asyncHttpClient.preparePost(uploadUrl);
        for ((k, v) <- header) postRequest.addHeader(k, v)
        postRequest.addBodyPart(new FilePart("file", video));
        val res = postRequest.execute().get();
        val json = Json.parse(res.getResponseBody())
        val url = (json \ "url").as[String]
        url
      }
    }
    promise.future
  }

  /**
   * Approve the video uploaded to Dailymotion
   * This is achived by sending POST request with video metadata to approval url returned by Dailymotion post upload.
   */
  def approveUploadedVideo(approvalUrl: String) = {
    val req = url(approvalUrl).POST
    //Send the response as Json value
    Http(req OK as.String) map (Json.parse(_))
  }
}
