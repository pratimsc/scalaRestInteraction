package org.maikalal.dailymotion.video

import org.maikalal.dailymotion.oauth.DailymotionOauthToken
import dispatch._
import dispatch.Defaults._
import play.api.libs.json.Json
import java.io.File
import com.ning.http.client.AsyncHttpClient
import com.ning.http.multipart.FilePart

object DailymotionVideo {

  val _VideoUploadURI = "https://api.dailymotion.com/file/upload"
  val _VideoListURI = "https://api.dailymotion.com/videos"
  def _VideoSingleURI(id: String) = s"https://api.dailymotion.com/video/${id}"

  def fetchVideoUploadUrls(oauth: DailymotionOauthToken) = {
    val req = url(_VideoUploadURI).GET
    val query = Map("access_token" -> oauth.access_token)

    val res = Http((req <<? query) OK as.String)
    for (r <- res) yield {
      val json = Json.parse(r)
      val upload_url = json \ "upload_url"
      val progress_url = json \ "progress_url"
      Map("upload_url" -> upload_url.as[String], "progress_url" -> progress_url.as[String])
    }
  }

  def uploadVideo(uploadUrl: String, video: File) = {
    val header = Map("Content-Type" -> "multipart/form-data")
    val body = Map("file" -> video)
    val req = url(uploadUrl)
    //Http((req <:< header <<* body) OK as.String)
    val asyncHttpClient = new AsyncHttpClient
    val postRequest = asyncHttpClient.preparePost(uploadUrl);
    for ((k, v) <- header) postRequest.addHeader(k, v)
    postRequest.addBodyPart(new FilePart("file", video));
    postRequest.execute();
  }

}

