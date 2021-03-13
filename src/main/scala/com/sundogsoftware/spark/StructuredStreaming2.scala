package com.sundogsoftware.spark

import org.apache.log4j._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._

/** Find the movies with the most ratings. */
object StructuredStreaming2 {


  /** Our main function where the action happens */
  def main(args: Array[String]) {
   
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder
      .appName("StructuredStreaming2")
      .master("local[*]")
      .getOrCreate()

    // Streaming source that monitors the data/logs directory for text files
    val accessLines = spark.readStream.text("data/logs")

    // Regular expressions to extract pieces of Apache access log lines
    val contentSizeExp = "\\s(\\d+)$"
    val statusExp = "\\s(\\d{3})\\s"
    val generalExp = "\"(\\S+)\\s(\\S+)\\s*(\\S*)\""
    val timeExp = "\\[(\\d{2}/\\w{3}/\\d{4}:\\d{2}:\\d{2}:\\d{2} -\\d{4})]"
    val hostExp = "(^\\S+\\.[\\S+\\.]+\\S+)\\s"

    // Apply these regular expressions to create structure from the unstructured text
    val logsDF = accessLines.select(regexp_extract(col("value"), hostExp, 1).alias("host"),
    regexp_extract(col("value"), timeExp, 1).alias("timestamp"),
    regexp_extract(col("value"), generalExp, 1).alias("method"),
    regexp_extract(col("value"), generalExp, 2).alias("endpoint"),
    regexp_extract(col("value"), generalExp, 3).alias("protocol"),
    regexp_extract(col("value"), statusExp, 1).cast("Integer").alias("status"),
    regexp_extract(col("value"), contentSizeExp, 1).cast("Integer").alias("content_size"))

    val logsDF2 = logsDF.withColumn("event_time", current_timestamp())

    // Keep a running count of endpoint (URL)
    val endpointCountsDF = logsDF2.groupBy(
      window(col("event_time"), windowDuration = "30 seconds", slideDuration = "10 seconds"),
      col("endpoint")
    ).count().orderBy(col("count").desc)

    // Display the stream to the console
    val query = endpointCountsDF.writeStream.outputMode("complete").format("console").queryName("counts").start()

    // Wait until we terminate the scripts
    query.awaitTermination()

    // Stop the session
    spark.stop()
  }
  
}

