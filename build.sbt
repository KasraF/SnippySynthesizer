name := "partialCorrectness"

version := "0.1"

scalaVersion := "2.11.12"

// https://mvnrepository.com/artifact/org.python/jython-standalone
libraryDependencies += "org.python" % "jython-standalone" % "2.7.2b2"
// https://mvnrepository.com/artifact/junit/junit
libraryDependencies += "junit" % "junit" % "4.13-rc-1" % Test
// https://mvnrepository.com/artifact/org.scalatest/scalatest
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.2.0-SNAP7" % Test
