name := """mita"""

version := "0.0.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    watchSources ++= (baseDirectory.value / "public/ui" ** "*").get
  )

resolvers ++= Resolver.sonatypeOssRepos("snapshots")
resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"

scalaVersion := "2.13.10"
scalacOptions ++= Seq(
  "-encoding",
  "UTF-8"
)
javaOptions += "-Dfile.encoding=UTF-8"

libraryDependencies += guice
libraryDependencies += "net.codingwell" %% "scala-guice" % "5.1.0"
libraryDependencies += "io.github.honeycomb-cheesecake" %% "play-silhouette" % "8.0.2"
libraryDependencies += "io.github.honeycomb-cheesecake" %% "play-silhouette-password-bcrypt" % "8.0.2"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test

addCommandAlias(
  "validateCode",
  "scalafmtSbtCheck; scalafmtCheckAll; uiCodeStyleCheck"
)
