/* package com.ravi.kafka

import kafka.serializer.{DefaultDecoder, StringDecoder}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}
object AppKafka {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
      .setAppName("Kafka-Spark").setMaster("local[*]")
     // .set("spark.cassandra.connection.host","127.0.0.1");
     val sc = new SparkContext(sparkConf)
    val ssc = new StreamingContext(sparkConf,Seconds(2));
    val sqlCtx = new SQLContext(sc)
    ssc.checkpoint("checkpoint")

    val kafkaConf = Map(
      "zookeeper.connect" -> "localhost:2181",
      "group.id" -> "test-consumer-group",
      "zookeeper.connection.timeout.ms" -> "5000"
    );
    val lines = KafkaUtils.createStream[Array[Byte],String,DefaultDecoder,StringDecoder](ssc, kafkaConf, Map("cas-topic"->1), StorageLevel.MEMORY_ONLY).map(_._2);

    //val word = line.flatMap{case(x,y) => y.split(" ")}

    //val lines = KafkaUtils.createStream(ssc, kafkaConf, "test-consumer-group", Map("my-topic"->1)).map(_._2)
    /*val words = lines.flatMap(_.split(" "))
    val wordCounts = words.map(x => (x, 1L))
      .reduceByKeyAndWindow(_ + _, _ - _, Minutes(10), Seconds(2), 2)
    wordCounts.print()*/

    val casCluster = lines.map(x => splitToTuple(" ",x))
    casCluster.print()
    //casCluster.saveToCassandra("test_keyspace","kafka_topics",SomeColumns("id","name","value","other"));
    ssc.start()
    ssc.awaitTermination()

  }

  def splitToTuple(regex: String,str:String): (Int, String,String,String) = {
    str.split(regex) match {
      case Array(str1, str2,str3,str4) => (str1.toInt, str2,str3,str4)
      case Array(str1,str2,str3) => (str1.toInt,str2,str3, "")
      case Array(str1,str2) => (str1.toInt,str2, "","")
      case _ => sys.error("too many colons")
    }
  }

} */
