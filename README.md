# sbt-slick-codegen

slick-codegen compile hook for sbt

## Install

```scala
// plugins.sbt

addSbtPlugin("com.github.tototoshi" % "sbt-slick-codegen" % "0.1.0-SNAPSHOT")

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// Database driver
// For example, when you are using PostgreSQL
libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1201-jdbc41"
```

## Configuration

```scala
// build.sbt
import scala.slick.codegen.SourceCodeGenerator
import scala.slick.{ model => m }

// required
slickCodegenSettings

// required
// Register codegen hook
sourceGenerators in Compile <+= slickCodegen

// required
slickCodegenDatabaseUrl in Compile := "jdbc:postgresql://localhost/example"

// required
slickCodegenDatabaseUser in Compile := "dbuser"

// required
slickCodegenDatabasePassword in Compile := "dbpassword"

// required (If not set, postgresql driver is choosen)
slickCodegenDriver in Compile := scala.slick.driver.PostgresDriver

// required (If not set, postgresql driver is choosen)
slickCodegenJdbcDriver in Compile := "org.postgresql.Driver"

// optional but maybe you want
slickCodegenOutputPackage in Compile := "com.example.models"

// optional, pass your own custom source code generator
slickCodegenCodeGenerator in Compile := { (model: m.Model) => new SourceCodeGenerator(model) }

// optional
// For example, to exclude flyway's schema_version table from the target of codegen
slickCodegenExcludedTables in Compile := Seq("schema_version")
```

## Example

https://github.com/tototoshi/sbt-slick-codegen-example


## License

Apache 2.0
