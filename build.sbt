name := "spark-pro"

version := "0.1"

scalaVersion := "2.11.11"

/*libraryDependencies += "org.apache.spark" %% "spark-core" % "1.6.2"
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "1.6.2"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.6.2"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.1.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka" % "1.6.2"
libraryDependencies += ("com.datastax.spark" %% "spark-cassandra-connector" % "2.0.1").exclude("org.jboss.netty", "netty")*/

//scalaVersion := "2.12.8"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.1.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "2.4.0"
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "2.4.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "2.4.0"
libraryDependencies += ("com.datastax.spark" %% "spark-cassandra-connector" % "2.0.0-M3").exclude("org.jboss.netty", "netty")
