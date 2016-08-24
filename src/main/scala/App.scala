package example
import java.nio.file.Paths
import java.io.{BufferedWriter, File, FileNotFoundException, FileWriter}
import java.text.SimpleDateFormat
import java.util.Date

import scala.io.Source
object RepDirWathcer extends  App{

  val RepRoot = Paths get "d:/UFO报表/"
  val ExistsFiles = Paths get "d:/data/record.txt"
  val DataRoot=Paths get "d:/data/"
  lazy val repSet:Set[String]=try(
      Source fromFile(ExistsFiles toFile) getLines() toSet
    ) catch {
    case e:FileNotFoundException => ExistsFiles.toFile.createNewFile()
      Set()
  }

  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }
  def now(): String ={
    val df=new SimpleDateFormat("yyyyMMddHHmmss")
    df.format(new Date)
  }

  def mkCmd(repFile:String): String={
    s"""
       |OPEN REPORT "$repFile"
       |
     |SAVE AS "${repFile.replace(RepRoot.toString,DataRoot.toString)}.mdb"
       |
     |CLOSE ALL
       |
   """.stripMargin.replaceAll("\n","\r\n")
  }


  val reps=recursiveListFiles(RepRoot.toFile).filter(_.toString.endsWith("rep")).filter(x=> !repSet(x.toString))

  val batCmd=reps  map (_.toString) map(mkCmd) mkString("\r\n")

  if(!batCmd.trim.equals("")){
    val writer = new BufferedWriter(new FileWriter( new File(s"${DataRoot.toString}/${now}.shl"),true))
    writer.write(batCmd)
    writer.close
  }else{
    println("No File")
  }
  val EFWriter = new BufferedWriter(new FileWriter(ExistsFiles.toFile.toString,true))
  for( s<-(reps map (_.toString))){
    EFWriter.write(s+"\r\n")
  }
  EFWriter.close()

}
