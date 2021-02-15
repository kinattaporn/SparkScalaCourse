package com.sundogsoftware.spark

import org.apache.spark.sql._
import org.apache.log4j._
import org.apache.spark.sql.functions.{avg, round}

object FriendsByAgeDatasetJan {

  case class FakeFriends(id: Int, name: String, age: Int, friends: Long) // define schema

  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder
      .appName("FriendsByAgeDatasetJan")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._
    val ds = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("data/fakefriends.csv")      // DataFrame
      .as[FakeFriends]
    ds.show(5)

    println("------------------------------ filter age 18")
    ds.filter(ds("age") === 18).show()

    ds.groupBy("age").sum("friends").sort("age").show(5)
    ds.groupBy("age").count().sort("age").show(5)
    ds.groupBy("age").agg(round(avg("friends"), 2).alias("friends_avg")).sort("age").show(5)

    spark.stop()
  }
}
