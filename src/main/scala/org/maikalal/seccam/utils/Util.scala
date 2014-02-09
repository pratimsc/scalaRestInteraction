package org.maikalal.seccam.utils

import java.io.File
import scala.util.Try
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.Writer
import java.io.FileWriter
import java.io.PrintWriter
import scala.io.Source
import java.nio.charset.StandardCharsets
import scala.io.Codec
import com.ning.http.client.Response
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.Json
import org.maikalal.dailymotion.oauth.DailymotionOauthToken

object Util {
  val DT_FORMAT_CCYYMMDD = "yyyyMMdd"
  val DT_FORMAT_CCYYMMDDHHmmssSSS = "yyyyMMddHHmmssSSS"
  val DT_FORMAT_DDMMYY = "ddMMYY"
  val DT_FORMAT_YYMMDD = "YYMMdd"
  val DT_FORMAT_YYDDD = "YYDDD"
  val DEFAULT_START_DATE = new DateTime(0, 1, 1, 0, 0)
  val DEFAULT_END_DATE = new DateTime(3599, 12, 31, 23, 59)
  val EMPTY_VALUE_STRING = ""

  def getDateInFormat(date: DateTime, outFormat: String): Try[String] =
    Try(date.toString(DateTimeFormat.forPattern(outFormat)))

  def getDateInFormat(date: Option[DateTime], outFormat: String): Try[String] =
    getDateInFormat(date.getOrElse(DEFAULT_END_DATE), outFormat)

  def getDateTime(dateStr: String, inputFormat: String): Try[DateTime] =
    Try(DateTimeFormat.forPattern(inputFormat).parseDateTime(dateStr))

  /*
   * Add empty spaces as filler.
   */
  def fillWithCharacter(count: Int, char: Char): String = (for (i <- 1 to count) yield char).mkString

  /*
   * Left justified text with size 
   */
  def leftJustfiedFormattedString(v: String, size: Int, truncate: Boolean = true, filler: Char = 0x20): String =
    if (v.size > size && truncate == true) v.substring(0, size) else String.format("%1$-" + size + "s", v)

  /*
   * Left justified text with size 
   */
  def rightJustfiedFormattedString(v: String, size: Int, truncate: Boolean = true, filler: Char = 0x20): String =
    if (v.size > size && truncate == true) v.substring(v.size - size) else String.format("%1$" + size + "s", v)

  /*
   * Helper function to extract all files from a folder
   */
  def extractFilesFromFolder(file: File): List[File] = {
    def recur(files: List[File], tree: List[File]): List[File] = files match {
      case h :: tail =>
        if (h.isDirectory()) {
          recur(h.listFiles().toList ::: tail, tree)
        } else {
          recur(tail, h :: tree)
        }
      case _ => tree
    }
    recur(List(file), List())
  }

  /**
   * Helper function to write data to a file.
   */
  def writeToFile(f: File)(p: PrintWriter => Unit) = {
    val out = new PrintWriter(f)
    try {
      p(out)
    } finally {
      out.close()
    }
  }

  /**
   * Helper function to read from a file
   */
  def readFromFile(input: File)(implicit codec: Codec) = {
    val f = Source.fromFile(input)(codec)
    val data = f.getLines.toList
    f.close
    data
  }

  /**
   * Helper function to delete a file
   */
  def deleteFile(f: File): Boolean = f.delete()

  /**
   * Helper function to print
   */
  def reportFailuresOfFuture: PartialFunction[Throwable, Unit] = {
    case err => println("Some error has occured->" + err.getMessage())
  }

  /**
   * Helper function to transform a Future[Response] to Future[JsValue]
   */
  def extractJsonFromResponse(res: Future[Response]) = {
    val json = res.map(r => Json.parse(r.getResponseBody()))
    res onFailure (Util.reportFailuresOfFuture)
    json
  }
  
  /**
   * Helper function to read Dailymotion OAUTH2 from file
   */
  def readDailymotionOauth2(f:File) = {
    val data = readFromFile(f).mkString(" ")
    Json.parse(data).as[DailymotionOauthToken]    
  }
}
