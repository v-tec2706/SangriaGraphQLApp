name := "SangriaGraphQLApp"

version := "0.1"

scalaVersion := "2.13.5"
val circeVersion = "0.12.3"
val sangriaVersion = "2.0.1"
val akkaVersion = "2.6.10"

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % sangriaVersion,
  "org.sangria-graphql" %% "sangria-slowlog" % sangriaVersion,
  "org.sangria-graphql" %% "sangria-circe" % "1.3.1",
  "org.sangria-graphql" %% "sangria-federated" % "0.0.2",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-optics" % "0.13.0",

  "com.typesafe.slick" %% "slick" % "3.3.2",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.2",
  "com.github.tminglei" %% "slick-pg" % "0.19.5",
  "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",

  "com.typesafe.akka" %% "akka-http" % "10.2.1",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.35.0",

  "org.slf4j" % "slf4j-nop" % "1.6.4",
)

mainClass in assembly := Some("benchmark.Run")
