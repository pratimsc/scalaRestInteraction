name := "youtubeUploader"

version := "0.1.0"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-deprecation", "-feature","-target:jvm-1.6")

libraryDependencies ++={
        val sprayVersion = "1.2.0"
        val akkaVersion = "2.2.3"
        val slickVersion = "2.0.0-M3"
        val liftVersion = "2.5.1"
        val logbackVersion = "1.0.13"
        val jodaVersion = "2.3"
        val junitVersion = "4.11"
        val scalaTestVersion = "2.0"
        val newmanVersion = "1.3.5"
        val playVersion = "2.2.1"
         Seq(
                "com.typesafe.akka"			%%	"akka-actor"				% akkaVersion withSources(),
                "com.typesafe.akka"         %%  "akka-slf4j"                % akkaVersion withSources(),
                "com.typesafe.slick"        %%  "slick"                     % slickVersion withSources(),
                "net.liftweb"               %%	"lift-json"                 % liftVersion withSources(),
                "net.liftweb"               %%	"lift-json-ext"             % liftVersion withSources(),
                "ch.qos.logback"            %	"logback-classic"        	% logbackVersion withSources(),
                "joda-time"                 %	"joda-time"                 % jodaVersion withSources(),
                "org.joda"                  %	"joda-convert"              % "1.5" withSources(),
                "org.joda"                  %	"joda-money"                % "0.9" withSources(),
                "junit"                     %	"junit"                     % junitVersion withSources(),
                "org.scalatest"             %%	"scalatest"                 % scalaTestVersion withSources(),                
                "com.typesafe"              %%	"scalalogging-slf4j" 		% "1.0.1" withSources(),
                "com.typesafe.play" 		% 	"play_2.10" 				% playVersion withSources()
        )
}

resolvers ++=Seq(
        "Spray repository" at "http://repo.spray.io",
        // The Typesafe repository 
		"Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"        
)
