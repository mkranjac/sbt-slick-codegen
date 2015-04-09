package com.github.tototoshi.sbt.slick

import sbt._
import Keys._
import scala.slick.codegen.SourceCodeGenerator
import scala.slick.driver.JdbcProfile
import scala.slick.{ model => m }

object CodegenPlugin extends sbt.Plugin {

  lazy val slickCodegen: TaskKey[Seq[File]] = taskKey[Seq[File]]("Command to run codegen")

  lazy val slickCodegenDatabaseUrl: SettingKey[String] =
    settingKey[String]("URL of database used by codegen")

  lazy val slickCodegenDatabaseUser: SettingKey[String] =
    settingKey[String]("User of database used by codegen")

  lazy val slickCodegenDatabasePassword: SettingKey[String] =
    settingKey[String]("Password of database used by codegen")

  lazy val slickCodegenDriver: SettingKey[JdbcProfile] =
    settingKey[JdbcProfile]("Slick driver used by codegen")

  lazy val slickCodegenJdbcDriver: SettingKey[String] =
    settingKey[String]("Jdbc driver used by codegen")

  lazy val slickCodegenOutputPackage: SettingKey[String] =
    settingKey[String]("Package of generated code")

  lazy val slickCodegenOutputFile: SettingKey[String] =
    settingKey[String]("Generated file")

  lazy val slickCodegenOutputContainer: SettingKey[String] =
    settingKey[String]("Container of generated source code")

  lazy val slickCodegenCodeGenerator: SettingKey[m.Model => SourceCodeGenerator] =
    settingKey[m.Model => SourceCodeGenerator]("Function to create CodeGenerator to be used")

  lazy val slickCodegenExcludedTables: SettingKey[Seq[String]] =
    settingKey[Seq[String]]("Tables that should be excluded")

  lazy val defaultSourceCodeGenerator: m.Model => SourceCodeGenerator = (model: m.Model) =>
    new SourceCodeGenerator(model)

  private def gen(
    generator: m.Model => SourceCodeGenerator,
    driver: JdbcProfile,
    jdbcDriver: String,
    url: String,
    user: String,
    password: String,
    outputDir: String,
    pkg: String,
    fileName: String,
    container: String,
    excluded: Seq[String],
    s: TaskStreams): Unit = {

    s.log.info(s"Generate source code with slick-codegen: url=${url}, user=${user}")

    val database = driver.simple.Database.forURL(url = url, driver = jdbcDriver, user = user, password = password)
    val model = database.withSession {
      implicit session =>
        val tables = driver.defaultTables.filterNot(t => excluded contains t.name.name)
        driver.createModel(Some(tables))
    }

    generator(model).writeToFile(
      profile = "scala.slick.driver." + driver.toString,
      folder = outputDir,
      pkg = pkg,
      container = container,
      fileName = fileName
    )

    s.log.info(s"Source code has generated in ${outputDir}/${fileName}")
  }

  lazy val slickCodegenSettings: Seq[Setting[_]] = Seq(
    slickCodegenDriver := scala.slick.driver.PostgresDriver,
    slickCodegenJdbcDriver := "org.postgresql.Driver",
    slickCodegenDatabaseUrl := "Database url is not set",
    slickCodegenDatabaseUser := "Database user is not set",
    slickCodegenDatabasePassword := "Database password is not set",
    slickCodegenOutputPackage := "com.example",
    slickCodegenOutputFile := "Tables.scala",
    slickCodegenOutputContainer := "Tables",
    slickCodegenExcludedTables := Seq(),
    slickCodegenCodeGenerator := defaultSourceCodeGenerator,
    slickCodegen := {
      val outDir = (sourceManaged in Compile).value.getPath
      val outPkg = (slickCodegenOutputPackage in Compile).value
      val outFile = (slickCodegenOutputFile in Compile).value
      gen(
        (slickCodegenCodeGenerator in Compile).value,
        (slickCodegenDriver in Compile).value,
        (slickCodegenJdbcDriver in Compile).value,
        (slickCodegenDatabaseUrl in Compile).value,
        (slickCodegenDatabaseUser in Compile).value,
        (slickCodegenDatabasePassword in Compile).value,
        outDir,
        outPkg,
        outFile,
        (slickCodegenOutputContainer in Compile).value,
        (slickCodegenExcludedTables in Compile).value,
        streams.value
      )
      Seq(file(outDir + "/" + outPkg.replaceAllLiterally(".", "/") + "/" + outFile))
    }
  )

}
