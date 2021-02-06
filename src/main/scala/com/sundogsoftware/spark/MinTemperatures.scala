package com.sundogsoftware.spark

import org.apache.spark._
import org.apache.log4j._
import scala.math.min
import scala.math.max

/** Find the minimum temperature by weather station */
object MinTemperatures {
  
  def parseLine(line:String): (String, String, Float) = {
    val fields = line.split(",")
    val stationID = fields(0)
    val entryType = fields(2)
    val temperature = fields(3).toFloat * 0.1f * (9.0f / 5.0f) + 32.0f
    (stationID, entryType, temperature)
  }
    /** Our main function where the action happens */
  def main(args: Array[String]) {
   
    // Set the log level to only print errors
    Logger.getLogger("org").setLevel(Level.ERROR)
    
    // Create a SparkContext using every core of the local machine
    val sc = new SparkContext("local[*]", "MinTemperatures")
    
    // Read each line of input data
    val lines = sc.textFile("data/1800.csv")
    
    // Convert to (stationID, entryType, temperature) tuples
    val parsedLines = lines.map(parseLine)
    println("--------------------------- parsedLines")
    parsedLines.take(5).foreach(println)
    
    // Filter out all but TMIN entries
    val minTemps = parsedLines.filter(x => x._2 == "TMIN")
    println("--------------------------- minTemps")
    minTemps.take(5).foreach(println)
    
    // Convert to (stationID, temperature)
    val stationTemps = minTemps.map(x => (x._1, x._3.toFloat))
    println("--------------------------- stationTemps")
    stationTemps.take(5).foreach(println)

    // Group By stationID
    val tempsByStation = stationTemps.groupByKey()
    println("--------------------------- tempsByStation")
    tempsByStation.take(5).foreach(println)
    
    // Reduce by stationID retaining the minimum temperature found
    val minTempsByStation = stationTemps.reduceByKey((x,y) => min(x,y))
    println("--------------------------- minTempsByStation")
    minTempsByStation.collect().foreach(println)

    // Reduce by stationID retaining the maximum temperature found
    val maxTempsByStation = stationTemps.reduceByKey((x,y) => max(x,y))
    println("--------------------------- maxTempsByStation")
    maxTempsByStation.collect().foreach(println)
    
    // Collect, format, and print the results
    val results = minTempsByStation.collect()
    println("--------------------------- results")
    for (result <- results.sorted) {
       val station = result._1
       val temp = result._2
       val formattedTemp = f"$temp%.2f F"
       println(s"$station minimum temperature: $formattedTemp") 
    }
    val resultsMax = maxTempsByStation.collect()
    println("--------------------------- resultsMax")
    for (result <- resultsMax.sorted) {
      val station = result._1
      val temp = result._2
      val formattedTemp = f"$temp%.2f F"
      println(s"$station minimum temperature: $formattedTemp")
    }
  }
}