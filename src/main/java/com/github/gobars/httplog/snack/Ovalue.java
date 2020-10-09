package com.github.gobars.httplog.snack;

import com.github.gobars.httplog.Str;
import com.github.gobars.httplog.snack.core.Defaults;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** 节点值 */
public class Ovalue {
  protected long intValue;
  protected double decimalValue;
  protected String strValue;
  protected boolean boolValue;
  protected Date dateValue;
  protected Number bigNumValue;
  protected Onode onode;
  private Otype otype = Otype.Null;

  public Ovalue(Onode n) {
    onode = n;
  }

  /** 尝试解析时间 */
  private static Date parseDate(String dateString) {
    try {
      return new SimpleDateFormat(Defaults.DEF_DATE_FORMAT_STRING).parse(dateString);
    } catch (ParseException ex) {
      return null;
    }
  }

  /** 获取值类型 */
  public Otype type() {
    return otype;
  }

  /** 设置值 */
  public void set(Object val) {
    if (val == null) {
      otype = Otype.Null;
      return;
    }

    if (val instanceof String) {
      setString((String) val);
      return;
    }

    if (val instanceof Date) {
      setDate((Date) val);
      return;
    }

    if (val instanceof Integer) {
      setInteger((Integer) val);
      return;
    }

    if (val instanceof Long) {
      setInteger((Long) val);
      return;
    }

    if (val instanceof Double) {
      setDecimal((Double) val);
      return;
    }

    if (val instanceof Float) {
      setDecimal((Float) val);
      return;
    }

    if (val instanceof Boolean) {
      setBool((Boolean) val);
      return;
    }

    if (val instanceof BigInteger || val instanceof BigDecimal) {
      setBigNumber((Number) val);
      return;
    }

    throw new RuntimeException("不支持类型:" + val.getClass().getName());
  }

  public void setNull() {
    otype = Otype.Null;
  }

  public void setInteger(long val) {
    otype = Otype.Integer;
    intValue = val;
  }

  public void setDecimal(double val) {
    otype = Otype.Decimal;
    decimalValue = val;
  }

  public void setBigNumber(Number val) {
    otype = Otype.BigNumber;
    bigNumValue = val;
  }

  public void setBool(boolean val) {
    otype = Otype.Boolean;
    boolValue = val;
  }

  public Object getRaw() {
    switch (otype) {
      case String:
        return tryAbbreviate(strValue);
      case Integer:
        return intValue;
      case DateTime:
        return dateValue;
      case Boolean:
        return boolValue;
      case Decimal:
        return decimalValue;
      case BigNumber:
        return bigNumValue;
      default:
        return null;
    }
  }

  private String tryAbbreviate(String s) {
    return Str.abbreviate(s, onode.co.abbrevMaxSize);
  }

  /** 获取原始的整型值 */
  public long getRawInteger() {
    return intValue;
  }

  /** 获取真实的小数值 */
  public double getRawDecimal() {
    return decimalValue;
  }

  /** 获取真实的字符串 */
  public String getRawString() {
    return tryAbbreviate(strValue);
  }

  /** 获取真实的布尔值 */
  public boolean getRawBoolean() {
    return boolValue;
  }

  /** 获取真实的日期 */
  public Date getRawDate() {
    return dateValue;
  }

  public Number getRawBignumber() {
    return bigNumValue;
  }

  public boolean isNull() {
    return otype == Otype.Null;
  }

  /** 获取值为 char 类型（为序列化提供支持） */
  public char getChar() {
    switch (otype) {
      case Integer:
        return (char) intValue;
      case Decimal:
        return (char) decimalValue;
      case BigNumber:
        return (char) bigNumValue.longValue();
      case String:
        if (strValue == null || strValue.length() == 0) {
          return 0;
        }

        return strValue.charAt(0);
      case Boolean:
        return boolValue ? '1' : '0';
      case DateTime:
      default:
        return 0;
    }
  }

  /** 获取值为 short 类型 */
  public short getShort() {
    return (short) getLong();
  }

  /** 获取值为 int 类型 */
  public int getInt() {
    return (int) getLong();
  }

  /** 获取值为 long 类型 */
  public long getLong() {
    switch (otype) {
      case Integer:
        return intValue;
      case Decimal:
        return (long) decimalValue;
      case BigNumber:
        return bigNumValue.longValue();
      case String:
        return (strValue == null || strValue.length() == 0) ? 0 : Long.parseLong(strValue);
      case Boolean:
        return boolValue ? 1 : 0;
      case DateTime:
        return dateValue.getTime();
      default:
        return 0;
    }
  }

  public float getFloat() {
    return (float) getDouble();
  }

  /** 获取值为 double 类型 */
  public double getDouble() {
    switch (otype) {
      case Decimal:
        return decimalValue;
      case Integer:
        return intValue;
      case BigNumber:
        return bigNumValue.doubleValue();
      case String:
        return (strValue == null || strValue.length() == 0) ? 0 : Double.parseDouble(strValue);
      case Boolean:
        return boolValue ? 1 : 0;
      case DateTime:
        return dateValue.getTime();
      default:
        return 0;
    }
  }

  /** 获取值为 string 类型 */
  public String getString() {
    switch (otype) {
      case String:
        return tryAbbreviate(strValue);
      case Integer:
        return String.valueOf(intValue);
      case Decimal:
        return String.valueOf(decimalValue);
      case BigNumber:
        return String.valueOf(bigNumValue);
      case Boolean:
        return String.valueOf(boolValue);
      case DateTime:
        return String.valueOf(dateValue);
      default:
        return onode.co.nullString();
    }
  }

  public void setString(String val) {
    otype = Otype.String;
    strValue = val;
  }

  /** 获取值为 boolean 类型 */
  public boolean getBoolean() {
    switch (otype) {
      case Boolean:
        return boolValue;
      case Integer:
        return intValue > 0;
      case Decimal:
        return decimalValue > 0;
      case BigNumber:
        return bigNumValue.longValue() > 0;
      default:
        return false;
    }
  }

  /** 获取值为 date 类型 */
  public Date getDate() {
    switch (otype) {
      case DateTime:
        return dateValue;
      case String:
        return parseDate(strValue);
      case Integer:
        return new Date(intValue);
      default:
        return null;
    }
  }

  public void setDate(Date val) {
    otype = Otype.DateTime;
    dateValue = val;
  }

  @Override
  public String toString() {
    return getString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null) {
      return isNull();
    }

    if (o instanceof Ovalue) {
      Ovalue o2 = (Ovalue) o;
      switch (otype) {
        case String:
          return strValue.equals(o2.strValue);
        case Integer:
          return intValue == o2.intValue;
        case DateTime:
          return dateValue.equals(o2.dateValue);
        case Boolean:
          return boolValue == o2.boolValue;
        case Decimal:
          return decimalValue == o2.decimalValue;
        case BigNumber:
          return bigNumValue.equals(o2.bigNumValue);
        default:
          return isNull() && o2.isNull();
      }
    }

    switch (otype) {
      case String:
        return strValue.equals(o);
      case Integer:
        return o instanceof Number && ((Number) o).longValue() == intValue;
      case DateTime:
        return dateValue.equals(o);
      case Boolean:
        return o instanceof Boolean && boolValue == (Boolean) o;
      case Decimal:
        return o instanceof Number && ((Number) o).doubleValue() == decimalValue;
      case BigNumber:
        return bigNumValue.equals(o);
      default:
        return false;
    }
  }

  @Override
  public int hashCode() {
    switch (otype) {
      case String:
        return strValue.hashCode();
      case Integer:
        return Long.hashCode(intValue);
      case DateTime:
        return dateValue.hashCode();
      case Boolean:
        return Boolean.hashCode(boolValue);
      case Decimal:
        return Double.hashCode(decimalValue);
      case BigNumber:
        return bigNumValue.hashCode();
      default:
        return 0;
    }
  }
}
