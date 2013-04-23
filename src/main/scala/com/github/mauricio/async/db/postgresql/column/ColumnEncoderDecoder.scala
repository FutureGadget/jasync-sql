/*
 * Copyright 2013 Maurício Linhares
 *
 * Maurício Linhares licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.github.mauricio.async.db.postgresql.column

import org.joda.time._
import scala.Some

object ColumnEncoderDecoder {

  val Bigserial = 20
  val Char = 18
  val CharArray = 1002
  val Smallint = 21
  val SmallintArray = 1005
  val Integer = 23
  val IntegerArray = 1007
  val Numeric = 1700
  // Decimal is the same as Numeric on PostgreSQL
  val NumericArray = 1231
  val Real = 700
  val RealArray = 1021
  val Double = 701
  val DoubleArray = 1022
  val Serial = 23
  val Bpchar = 1042
  val BpcharArray = 1014
  val Varchar = 1043
  // Char is the same as Varchar on PostgreSQL
  val VarcharArray = 1015
  val Text = 25
  val TextArray = 1009
  val Timestamp = 1114
  val TimestampArray = 1115
  val TimestampWithTimezone = 1184
  val TimestampWithTimezoneArray = 1185
  val Date = 1082
  val DateArray = 1182
  val Time = 1083
  val TimeArray = 1183
  val TimeWithTimezone = 1266
  val TimeWithTimezoneArray = 1270
  val Boolean = 16
  val BooleanArray = 1000

  val OIDArray = 1028
  val MoneyArray = 791
  val NameArray = 1003
  val UUIDArray = 2951
  val XMLArray = 143

  private val classes = Map[Class[_], Int](
    classOf[Int] -> Integer,
    classOf[Short] -> Integer,
    classOf[java.lang.Integer] -> Integer,
    classOf[java.lang.Short] -> Integer,

    classOf[Long] -> Bigserial,
    classOf[java.lang.Long] -> Bigserial,

    classOf[String] -> Varchar,
    classOf[java.lang.String] -> Varchar,

    classOf[Float] -> Real,
    classOf[java.lang.Float] -> Real,

    classOf[Double] -> Double,
    classOf[java.lang.Double] -> Double,

    classOf[BigDecimal] -> Numeric,
    classOf[java.math.BigDecimal] -> Numeric,

    classOf[LocalDate] -> Date,
    classOf[LocalTime] -> Time,
    classOf[ReadablePartial] -> Time,
    classOf[ReadableDateTime] -> Timestamp,
    classOf[ReadableInstant] -> Date,
    classOf[DateTime] -> Timestamp,

    classOf[java.util.Date] -> Timestamp,
    classOf[java.sql.Date] -> Date,
    classOf[java.sql.Time] -> Time,
    classOf[java.sql.Timestamp] -> Timestamp,
    classOf[java.util.Calendar] -> Timestamp,
    classOf[java.util.GregorianCalendar] -> Timestamp
  )

  def decoderFor(kind: Int): ColumnEncoderDecoder = {
    kind match {
      case Boolean => BooleanEncoderDecoder
      case BooleanArray => new ArrayEncoderDecoder(BooleanEncoderDecoder)
      case Char => CharEncoderDecoder
      case CharArray => new ArrayEncoderDecoder(CharEncoderDecoder)
      case Bigserial => LongEncoderDecoder
      case Smallint => ShortEncoderDecoder
      case SmallintArray => new ArrayEncoderDecoder(ShortEncoderDecoder)
      case Integer => IntegerEncoderDecoder
      case IntegerArray => new ArrayEncoderDecoder(IntegerEncoderDecoder)
      case Numeric => BigDecimalEncoderDecoder
      case NumericArray => new ArrayEncoderDecoder(BigDecimalEncoderDecoder)
      case Real => FloatEncoderDecoder
      case RealArray => new ArrayEncoderDecoder(FloatEncoderDecoder)
      case Double => DoubleEncoderDecoder
      case DoubleArray => new ArrayEncoderDecoder(DoubleEncoderDecoder)
      case Text => StringEncoderDecoder
      case TextArray => new ArrayEncoderDecoder(StringEncoderDecoder)
      case Varchar => StringEncoderDecoder
      case VarcharArray => new ArrayEncoderDecoder(StringEncoderDecoder)
      case Bpchar => StringEncoderDecoder
      case BpcharArray => new ArrayEncoderDecoder(StringEncoderDecoder)
      case Timestamp => TimestampEncoderDecoder.Instance
      case TimestampArray => new ArrayEncoderDecoder(TimestampEncoderDecoder.Instance)
      case TimestampWithTimezone => TimestampWithTimezoneEncoderDecoder
      case TimestampWithTimezoneArray => new ArrayEncoderDecoder(TimestampWithTimezoneEncoderDecoder)
      case Date => DateEncoderDecoder
      case DateArray => new ArrayEncoderDecoder(DateEncoderDecoder)
      case Time => TimeEncoderDecoder.Instance
      case TimeArray => new ArrayEncoderDecoder(TimeEncoderDecoder.Instance)
      case TimeWithTimezone => TimeWithTimezoneEncoderDecoder
      case TimeWithTimezoneArray => new ArrayEncoderDecoder(TimeWithTimezoneEncoderDecoder)

      case OIDArray => new ArrayEncoderDecoder(StringEncoderDecoder)
      case MoneyArray => new ArrayEncoderDecoder(StringEncoderDecoder)
      case NameArray => new ArrayEncoderDecoder(StringEncoderDecoder)
      case UUIDArray => new ArrayEncoderDecoder(StringEncoderDecoder)
      case XMLArray => new ArrayEncoderDecoder(StringEncoderDecoder)

      case _ => StringEncoderDecoder
    }
  }

  def kindFor(clazz: Class[_]): Int = {
    this.classes.get(clazz).getOrElse {
      this.classes.find(entry => entry._1.isAssignableFrom(clazz)) match {
        case Some(parent) => parent._2
        case None => 0
      }
    }
  }

  def decode(kind: Int, value: String): Any = {
    if (value == null || "NULL" == value) {
      null
    } else {
      decoderFor(kind).decode(value)
    }
  }

  def encode(value: Any): String = {
    if (value == null) {
      "NULL"
    } else {
      decoderFor(kindFor(value.getClass)).encode(value)
    }
  }

}

trait ColumnEncoderDecoder {

  def decode(value: String): Any = value

  def encode(value: Any): String = value.toString

}

/*

    public static final int UNSPECIFIED = 0;
    public static final int INT2 = 21;
    public static final int INT2_ARRAY = 1005;
    public static final int INT4 = 23;
    public static final int INT4_ARRAY = 1007;
    public static final int INT8 = 20;
    public static final int INT8_ARRAY = 1016;
    public static final int TEXT = 25;
    public static final int TEXT_ARRAY = 1009;
    public static final int NUMERIC = 1700;
    public static final int NUMERIC_ARRAY = 1231;
    public static final int FLOAT4 = 700;
    public static final int FLOAT4_ARRAY = 1021;
    public static final int FLOAT8 = 701;
    public static final int FLOAT8_ARRAY = 1022;
    public static final int BOOL = 16;
    public static final int BOOL_ARRAY = 1000;
    public static final int DATE = 1082;
    public static final int DATE_ARRAY = 1182;
    public static final int TIME = 1083;
    public static final int TIME_ARRAY = 1183;
    public static final int TIMETZ = 1266;
    public static final int TIMETZ_ARRAY = 1270;
    public static final int TIMESTAMP = 1114;
    public static final int TIMESTAMP_ARRAY = 1115;
    public static final int TIMESTAMPTZ = 1184;
    public static final int TIMESTAMPTZ_ARRAY = 1185;
    public static final int BYTEA = 17;
    public static final int BYTEA_ARRAY = 1001;
    public static final int VARCHAR = 1043;
    public static final int VARCHAR_ARRAY = 1015;
    public static final int OID = 26;
    public static final int OID_ARRAY = 1028;
    public static final int BPCHAR = 1042;
    public static final int BPCHAR_ARRAY = 1014;
    public static final int MONEY = 790;
    public static final int MONEY_ARRAY = 791;
    public static final int NAME = 19;
    public static final int NAME_ARRAY = 1003;
    public static final int BIT = 1560;
    public static final int BIT_ARRAY = 1561;
    public static final int VOID = 2278;
    public static final int INTERVAL = 1186;
    public static final int INTERVAL_ARRAY = 1187;
    public static final int CHAR = 18; // This is not char(N), this is "char" a single byte type.
    public static final int CHAR_ARRAY = 1002;
    public static final int VARBIT = 1562;
    public static final int VARBIT_ARRAY = 1563;
    public static final int UUID = 2950;
    public static final int UUID_ARRAY = 2951;
    public static final int XML = 142;
    public static final int XML_ARRAY = 143;

*/