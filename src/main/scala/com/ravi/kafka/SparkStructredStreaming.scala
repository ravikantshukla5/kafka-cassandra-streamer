package com.ravi.kafka

import com.datastax.spark.connector.cql.CassandraConnector
import org.apache.spark.SparkConf
import org.apache.spark.sql.{ForeachWriter, SparkSession}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.functions.from_json


object SparkStructredStreaming extends  SparkSessionBuilder  {
  def main(args: Array[String]): Unit = {

    val spark = buildSparkSession
    import spark.implicits._
    // Read incoming stream
    val dfraw = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "cas-topic")
      .load()

    /*val query3 = dfraw.writeStream
      .format("console")
      .start()
    query3.awaitTermination()*/
    val schema = StructType(
      Seq(
        StructField("id", IntegerType, false),
        StructField("name", StringType, false),
        StructField("value", StringType, false),
        StructField("other", StringType, false)
      )
    )
    /*val df = dfraw
      .selectExpr("CAST(value AS STRING)").as[String]
      .flatMap(_.split("\n"))*/
    val df = dfraw
      .selectExpr("CAST(value AS STRING)").as[String]
      .map(x => splitToTuple(" ",x))
    val casDf = df.toDF()
      .withColumnRenamed("_1","id")
      .withColumnRenamed("_2","name")
      .withColumnRenamed("_3","value")
      .withColumnRenamed("_4","other")
    //val jsons = df.select(from_json($"value", schema) as "data").select("data.*")
    // Process data. Create a new date column
   // val parsed = jsons
    //  .withColumn("id", $"id" cast "Int")

   /*val query3 = casDf.writeStream
      .format("console")
      .start()
    casDf.printSchema()
    query3.awaitTermination()*/

    // Output results into a database
    val sink = casDf
      .writeStream
      .queryName("KafkaToCassandraForeach")
      .outputMode("update")
      .option("failOnDataLoss",false)
      .foreach(new CassandraSinkForeach())
      .start()
    sink.awaitTermination()

  }
  def splitToTuple(regex: String,str:String): (Int, String,String,String) = {
    str.split(regex) match {
      case Array(str1, str2,str3,str4) => (str1.toInt, str2,str3,str4)
      case Array(str1,str2,str3) => (str1.toInt,str2,str3, "")
      case Array(str1,str2) => (str1.toInt,str2, "","")
      case _ => sys.error("too many colons")
    }
  }

}

class SparkSessionBuilder extends Serializable {
  // Build a spark session. Class is made serializable so to get access to SparkSession in a driver and executors.
  // Note here the usage of @transient lazy val
  def buildSparkSession: SparkSession = {
    @transient lazy val conf: SparkConf = new SparkConf()
      .setMaster("local[*]")
      .setAppName("Structured Streaming from Kafka to Cassandra")
      .set("spark.cassandra.connection.host", "127.0.0.1")
      .set("spark.sql.streaming.checkpointLocation", "checkpoint")
    @transient lazy val spark = SparkSession
      .builder()
      .config(conf)
      .getOrCreate()
    spark
  }
}
class CassandraDriver extends SparkSessionBuilder {
  // This object will be used in CassandraSinkForeach to connect to Cassandra DB from an executor.
  // It extends SparkSessionBuilder so to use the same SparkSession on each node.
  val spark = buildSparkSession
  import spark.implicits._
  val connector = CassandraConnector(spark.sparkContext.getConf)
  // Define Cassandra's table which will be used as a sink
  /* For this app I used the following table:
       CREATE TABLE fx.spark_struct_stream_sink (
       fx_marker text,
       timestamp_ms timestamp,
       timestamp_dt date,
       primary key (fx_marker));
  */
  val namespace = "test_keyspace"
  val foreachTableSink = "kafka_topics"
}


class CassandraSinkForeach() extends ForeachWriter[org.apache.spark.sql.Row] {
  // This class implements the interface ForeachWriter, which has methods that get called
  // whenever there is a sequence of rows generated as output
  val cassandraDriver = new CassandraDriver();
  def open(partitionId: Long, version: Long): Boolean = {
    // open connection
    println(s"Open connection")
    true
  }
  def process(record: org.apache.spark.sql.Row) = {
    println(s"Process new $record")

    if(record.get(0)!= null) {
      cassandraDriver.connector.withSessionDo(session =>
        session.execute(
          s"""
       insert into ${cassandraDriver.namespace}.${cassandraDriver.foreachTableSink} (id,name,value,other)
       values(${record(0)}, '${record(1)}', '${record(2)}','${record(3)}')""")
      )
    }
  }
  def close(errorOrNull: Throwable): Unit = {
    // close the connection
    println(s"Close connection")
  }
}
