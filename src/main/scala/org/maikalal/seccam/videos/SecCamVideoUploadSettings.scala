package org.maikalal.seccam.videos

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConversions._

case class SecCamVideoUploadSettings(conf:Config) {
  
  conf.checkValid(ConfigFactory.load("ref-video-upload.config"),"dailymotion" )
  
  val dailymotionApiKey = conf.getString("dailymotion.developer.api.key")
  val dailymotionApiSecret = conf.getString("dailymotion.developer.api.secret")
  val dailymotionAccountUserId = conf.getString("dailymotion.account.user.id")
  val dailymotionAccountUserPassword = conf.getString("dailymotion.account.user.password")
  val dailymotionApiOauth2AccessTokenURI = conf.getString("dailymotion.api.oauth2.access.token.uri")
  val dailymotionApiOauth2AccessTokenPermittedAgeInSeconds = conf.getLong("dailymotion.api.oauth2.access.token.permitted.age.inSeconds")
  val dailymotionApiOauth2AccessTokenDownloadJsonFile = conf.getString("dailymotion.api.oauth2.access.token.download.json.file")
  val dailymotionApiVideoUploadScope = conf.getStringList("dailymotion.api.video.upload.scopes").toList
  val dailymotionApiVideoUploadRequestUri = conf.getString("dailymotion.api.video.upload.request.uri")
  val dailymotionApiVideoUploadApprovalUri = conf.getString("dailymotion.api.video.upload.approval.uri")
  val dailymotionApiVideoUploadPublishUri = conf.getString("dailymotion.api.video.upload.publish.uri")
  val dailymotionApiVideoListUri = conf.getString("dailymotion.api.video.list.uri")
  val securityCameraVideosSourceFolder = conf.getString("security.camera.video.source.folder")
  val securityCameraVideosArchiveFolder = conf.getString("security.camera.video.archive.folder")
  val approvedVideoFileExtensions = conf.getStringList("approved.video.file.extensions").toList.map(_.toLowerCase())
}
