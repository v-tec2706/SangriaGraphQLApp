name := "SangriaGraphQLApp"

version := "0.1"

scalaVersion := "2.13.5"
val circeVersion = "0.12.3"
val sangriaVersion = "2.0.1"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % sangriaVersion,
  "org.sangria-graphql" %% "sangria-slowlog" % sangriaVersion,
  "org.sangria-graphql" %% "sangria-circe" % "1.3.1",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.typesafe.slick" %% "slick" % "3.3.2",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.2",
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",
  "com.github.tminglei" %% "slick-pg" % "0.19.5"
)

mainClass in(Compile, run) := Some("benchmark.Run")