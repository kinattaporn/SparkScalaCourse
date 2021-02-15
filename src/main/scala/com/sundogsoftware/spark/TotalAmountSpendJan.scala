package com.sundogsoftware.spark

import org.apache.log4j._
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._

object TotalAmountSpendJan {
  def main(args: Array[String]) {

    Logger.getLogger("org").setLevel(Level.ERROR)

    val sc = new SparkContext("local[*]", "TotalAmountSpend")

    val lines = sc.textFile("data/customer-orders.csv")
    println("--------------------------- lines")
    lines.take(5).foreach(println)

    val fields = lines.map(x => {
      val xs = x.split(",")
      val xs0 = xs(0).toInt
      val xs2 = xs(2).toFloat
      (xs0, xs2)
    })
    println("--------------------------- fields")
    fields.take(5).foreach(println)

    println("---------------------------------------------------------- GroupByKey")
    val amountGroupByKey = fields.groupByKey()
    println("--------------------------- amountGroupByKey")
    amountGroupByKey.take(5).foreach(println)

    val sumAmountGroupByKey = amountGroupByKey.map(x => (x._1, x._2.sum))
    println("--------------------------- sumAmountGroupByKey")
    sumAmountGroupByKey.take(5).foreach(println)

    val sumAmountGroupByKeySort = sumAmountGroupByKey.sortBy(- _._2)
    println("--------------------------- sumAmountGroupByKeySort")
    sumAmountGroupByKeySort.take(5).foreach(println)

    println("---------------------------------------------------------- ReduceByKey")
    val sumAmountReduceByKey = fields.reduceByKey((x, y) => x + y)
    println("--------------------------- sumAmountReduceByKey")
    sumAmountReduceByKey.take(5).foreach(println)

    val sumAmountReduceByKeySort = sumAmountReduceByKey.sortBy(- _._2)
    println("--------------------------- sumAmountReduceByKeySort")
    sumAmountReduceByKeySort.take(5).foreach(println)
  }
}
