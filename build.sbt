import AssemblyKeys._ // put this at the top of the file

assemblySettings 

name := "SecCamVideoProcessor"

version := "0.1.0"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-deprecation", "-feature","-target:jvm-1.6")

libraryDependencies ++={
        val sprayVersion = "1.2.0"
        val akkaVersion = "2.2.3"
        val logbackVersion = "1.1.1"
        val jodaVersion = "2.3"
        val junitVersion = "4.11"
        val scalaTestVersion = "2.1.0-RC2"
        val playVersion = "2.2.1"
        val bouncycastleVersion = "1.50"
         Seq(
                "ch.qos.logback"            %	"logback-classic"        	% logbackVersion withSources(),
                "joda-time"                 %	"joda-time"                 % jodaVersion withSources(),
                "org.joda"                  %	"joda-convert"              % "1.6" withSources(),
                "junit"                     %	"junit"                     % junitVersion withSources(),
                "org.scalatest"             %%	"scalatest"                 % scalaTestVersion withSources(),                
                "com.typesafe"              %%	"scalalogging-slf4j" 		% "1.0.1" withSources(),
                "com.typesafe.play" 		%% 	"play-json"	 				% playVersion withSources(),
                "net.databinder.dispatch"	%%	"dispatch-core" 			% "0.11.0" withSources(),
                "org.bouncycastle"			%	"bcprov-jdk14"				% bouncycastleVersion withSources(),
                "org.bouncycastle" 			%	"bcpkix-jdk14" 				% bouncycastleVersion withSources()
        )
}

resolvers ++=Seq(
        "Spray repository" at "http://repo.spray.io",
        // The Typesafe repository 
		"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"        
)

jarName in assembly := "seccamvideouploadV0.1.0.jar"

mainClass in assembly := Some("org.maikalal.seccam.videos.StartSecCamVideoUploadProcess")
