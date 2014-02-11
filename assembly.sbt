import AssemblyKeys._ // put this at the top of the file

assemblySettings 

jarName in assembly := {name.value + "V" + version.value + ".jar"}

mainClass in assembly := Some("org.maikalal.seccam.videos.StartSecCamVideoUploadProcess")

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case "application.conf" => MergeStrategy.concat    
    case "logback.xml"     => MergeStrategy.discard    
    case "*.log"     => MergeStrategy.discard
    case x => old(x)
  }
}
