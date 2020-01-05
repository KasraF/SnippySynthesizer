name := "partialcorrectness_sem"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0"

// https://mvnrepository.com/artifact/jline/jline
libraryDependencies += "jline" % "jline" % "2.14.6"


// https://mvnrepository.com/artifact/org.antlr/antlr4-runtime
libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.7.2"

// https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.9"

// https://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "commons-io" % "commons-io" % "2.6"

// https://mvnrepository.com/artifact/junit/junit
libraryDependencies += "junit" % "junit" % "4.13-rc-1" % Test
// https://mvnrepository.com/artifact/org.scalatest/scalatest
//libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % Test
// https://mvnrepository.com/artifact/org.scalatestplus/scalatestplus-junit
libraryDependencies += "org.scalatestplus" %% "scalatestplus-junit" % "1.0.0-SNAP9"

//Anlr command line:
//java -jar antlr-4.7.2-complete.jar -package "sygus" -visitor SyGuS.g4