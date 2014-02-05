package org.maikalal.ams.sim.utils

import java.io.File

import scala.util.Try

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

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

}
