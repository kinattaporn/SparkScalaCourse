package com.sundogsoftware.spark

import org.apache.spark.sql._
import org.apache.log4j._
import org.apache.spark.sql.types.{DoubleType, IntegerType, StructType}
import org.apache.spark.sql.functions.{sum, round}

object TotalAmountSpendDatasetJan {

  case class AmountSpend(id: Int, item: Int, amount: Double)

  def main(args: Array[String]) {
    Logger.getLogger("org").setLevel(Level.ERROR)

    val spark = SparkSession
      .builder
      .appName("TotalAmountSpendDatasetJan")
      .master("local[*]")
      .getOrCreate()

    val amountSpendSchema = new StructType() // at run time
      .add("id", IntegerType, nullable = true)
      .add("item", IntegerType, nullable = true)
      .add("amount", DoubleType, nullable = true)

    import spark.implicits._
    val ds = spark.read
        .schema(amountSpendSchema)
        .csv("data/customer-orders.csv")
        .as[AmountSpend] // at compile time - good to do this
    println("--------------------------- ds")
    ds.show(5)

    val sumAmount = ds.groupBy("id").agg(round(sum("amount"), 2).alias("sum_amount")).sort("sum_amount")
    println("--------------------------- sumAmount")
    sumAmount.show(sumAmount.count.toInt) // show entire ds
  }
}
