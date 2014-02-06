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
import org.maikalal.dailymotion.oauth.DailymotionAPICredential
import play.api.libs.json.JsValue
import org.joda.time.DateTime
import org.maikalal.seccam.utils.Util

object DailymotionVideo {

  val _VideoUploadRequestURI = "https://api.dailymotion.com/file/upload"
  val _VideoUploadApprovalURI = "https://api.dailymotion.com/me/videos"
  val _VideoListURI = "https://api.dailymotion.com/videos"
  def _VideoPublishURI(id: String) = s"https://api.dailymotion.com/video/${id}"

  /**
   * Fetch the video upload url from Dailymotion.
   * The videos have to be POSTed to the upload url.
   */
  def fetchVideoUploadUrls(oauth: DailymotionOauthToken): Future[Map[String, String]] = {
    val req = url(_VideoUploadRequestURI).GET
    val params = Map("access_token" -> oauth.access_token)
    val res = Http(req <<? params)
    val json = Util.extractJsonFromResponse(res)
    json map { j =>
      val upload_url = (j \ "upload_url").as[String]
      val progress_url = (j \ "progress_url").as[String]
      Map("upload_url" -> upload_url, "progress_url" -> progress_url)
    }
  }

  /**
   * Post the video to the Dailymotion.
   * Post upload, Dailymotion returns an approval url.
   * Unless a POST request is sent to the url, the video does not get registered with the user's library.
   * The POST request to the approval url must follow immediately.
   */

  def uploadVideoForApproval(uploadUrl: String, video: File): Future[String] = {
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
        println("Post upload the reponse is ->\n" + Json.prettyPrint(json))
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
    val res = Http(url(approvalUrl).POST)
    Util.extractJsonFromResponse(res)
  }

  /**
   * Upload a single video to Dailymotion
   */
  def uploadSingleVideoToDailymotion(oauth: DailymotionOauthToken, video: File): Future[JsValue] = {
    val videoMetadataJson = for {
      uploadUrl <- fetchVideoUploadUrls(oauth)
      approveUrl <- uploadVideoForApproval(uploadUrl.get("upload_url").get, video)
      approval <- approveTheUploadedVideo(approveUrl, oauth)
      publish <- publishVideo(video, (approval \ "id").as[String], oauth)
    } yield {
      println("OATH2 Value used ->\n" + Json.prettyPrint(Json.toJson(oauth)))
      println("The Upload and Progress urls are ->\n" + Json.prettyPrint(Json.toJson(uploadUrl)))
      println("The approval url is -> \n" + approveUrl)
      println("The approvals is -> \n" + Json.prettyPrint(approval))
      println("The published information is -> \n" + Json.prettyPrint(publish))
      publish
      approval
    }
    videoMetadataJson
  }

  /**
   * Approve the video
   */
  def approveTheUploadedVideo(approveUrl: String, oauth: DailymotionOauthToken) = {
    val res = Http(url(_VideoUploadApprovalURI).POST << Map("url" -> approveUrl, "access_token" -> oauth.access_token))
    Util.extractJsonFromResponse(res)
  }

  /**
   * Publish the video
   */
  def publishVideo(video: File, videoId: String, oauth: DailymotionOauthToken) = {
    val res = Http(url(_VideoPublishURI(videoId)).POST << Map("access_token" -> oauth.access_token) ++ generateVideoMetadata(video))
    Util.extractJsonFromResponse(res)
  }

  /**
   * Generate video metata for Dailymotion
   */
  def generateVideoMetadata(video: File) = Map(
    "title" -> (DateTime.now() + "_" + video.getName()),
    "channel" -> "webcam",
    "tags" -> (new DateTime).toString())

  /**
   * Class to represent a Daily motion video
   */
  case class DailymotionVideo()
}

