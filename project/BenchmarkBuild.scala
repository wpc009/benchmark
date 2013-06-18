import sbt._
import sbt.Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object BenchmarkBuild extends Build {

    lazy val benchmark = Project(
        id = "benchmark",
        base = file("."),
        settings = Project.defaultSettings ++ assemblySettings ++ Seq(
            name := "Benchmark",
            organization := "freestyle",
            version := "0.1-",
            scalaVersion := "2.10.1",
            resolvers += "spray repo" at "http://repo.spray.io",
            mainClass in assembly := Some("freestyle.Benchmark"),
            externalPom()
        )

    )
}
