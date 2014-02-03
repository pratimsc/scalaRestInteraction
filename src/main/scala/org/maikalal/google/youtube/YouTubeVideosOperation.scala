package org.maikalal.google.youtube

import org.maikalal.google.oauth.GoogleOAUTH2AccessCode
import java.io.File
import java.net.URI
import org.maikalal.google.oauth.GoogleOAUTH2ClientSecret
import play.api.libs.json.Json
import org.joda.time.DateTime
import dispatch._
import dispatch.Defaults._

object YouTubeVideosOperation {
  val _YoutubeVideoInsertURI = "https://www.googleapis.com/upload/youtube/v3/videos"
  val _YoutubeVideoCategoriesURI = "https://www.googleapis.com/youtube/v3/videoCategories"

  def getBespokeInsertVideoLocation(auth: GoogleOAUTH2AccessCode, apiKey: String, video: File) = {
    val header = Map(
      "Authorization" -> (s"""Bearer ${auth.access_token}"""),
      "Host" -> "accounts.google.com",
      "Content-Type" -> """application/json""")

    val query = Map(
      "uploadType" -> "resumable",
      "part" -> "snippet, status",
      "key" -> apiKey)

    val requestBody = Json.obj(
      "snippet" -> Json.obj(
        "title" -> video.getName(),
        "description" -> s"Security Camera file created at ${new DateTime(video.lastModified())}",
        "tags" -> Json.arr("Security", "Cam", "SK11 9AU"),
        "channelId" -> "UC2v8f3Pu7Cc4s-J1U-anIJA"),
      "status" -> Json.obj(
        "privacyStatus" -> "private"))

    val req = url(_YoutubeVideoInsertURI).POST
    val completeReq = req <:< header <<? query << Json.prettyPrint(requestBody)

    println("Request Headers ->\n" + header.mkString("\n"))
    println("Request Query ->\n" + query.mkString("\n"))
    println("Request Body ->\n" + Json.prettyPrint(requestBody))

    Http(completeReq.setFollowRedirects(true))
  }

  def getYouTuveVideoCategories(auth: GoogleOAUTH2AccessCode, apiKey: String, video: File) = {
    val header = Map(
      "Authorization" -> (s"""Bearer ${auth.access_token}"""),
      "Content-Type" -> """application/json""")

    val query = Map(
      "part" -> "id, snippet",
      "regionCode" -> "GB",
      "key" -> apiKey)

    val req = url(_YoutubeVideoCategoriesURI).GET
    val completeReq = req <:< header <<? query

    println("Request Headers ->\n" + header.mkString("\n"))
    println("Request Query ->\n" + query.mkString("\n"))

    Http(completeReq.setFollowRedirects(true))
  }
}

