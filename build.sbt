scalaVersion in ThisBuild := "2.11.7"

lazy val extruder = (project in file(".")).
  aggregate(extruderReflect, extruderDemo)

lazy val extruderReflect = (project in file("extruder-reflect"))

lazy val extruderDemo = (project in file("extruder-demo")).
  dependsOn(extruderReflect)
