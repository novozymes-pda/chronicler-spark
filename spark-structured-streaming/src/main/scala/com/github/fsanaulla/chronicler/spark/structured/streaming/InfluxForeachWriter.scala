package com.github.fsanaulla.chronicler.spark.structured.streaming

import com.github.fsanaulla.chronicler.core.enums.{Consistencies, Consistency, Precision, Precisions}
import com.github.fsanaulla.chronicler.core.model.{InfluxConfig, InfluxWriter}
import com.github.fsanaulla.chronicler.urlhttp.Influx
import com.github.fsanaulla.chronicler.urlhttp.api.Measurement
import com.github.fsanaulla.chronicler.urlhttp.clients.UrlIOClient
import org.apache.spark.sql.ForeachWriter

import scala.reflect.ClassTag

/**
  * Influx foreach writer for structured streaming
  *
  * @param dbName          - influxdb name
  * @param measName        - measurement name
  * @param wr              - implicit influx writer
  * @param conf            - chronicler influx config
  * @tparam T
  */
private[streaming] final class InfluxForeachWriter[T: ClassTag](dbName: String,
                                                                measName: String,
                                                                consistency: Consistency = Consistencies.ONE,
                                                                precision: Precision = Precisions.NANOSECONDS,
                                                                retentionPolicy: Option[String] = None)
                                                               (implicit wr: InfluxWriter[T], conf: InfluxConfig) extends ForeachWriter[T] {

  var influx: UrlIOClient = _
  var meas: Measurement[T] = _

  override def open(partitionId: Long, version: Long): Boolean = {
    influx = Influx.io(conf)
    meas = influx.measurement[T](dbName, measName)
    true
  }
  override def process(value: T): Unit = meas.write(value).map(_ => {})
  override def close(errorOrNull: Throwable): Unit = influx.close()
}