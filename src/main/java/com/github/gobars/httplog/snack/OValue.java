package com.github.gobars.httplog.snack;

import com.github.gobars.httplog.snack.core.Defaults;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/** 节点值 */
public class OValue {
  protected long _integer;
  protected double _decimal;
  protected String _string;
  protected boolean _bool;
  protected Date _date;
  protected Number _bignumber;
  protected ONode _n;
  private OValueType _type = OValueType.Null;

  public OValue(ONode n) {
    _n = n;
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
  public OValueType type() {
    return _type;
  }

  /** 设置值 */
  public void set(Object val) {
    if (val == null) {
      _type = OValueType.Null;
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
    _type = OValueType.Null;
  }

  public void setInteger(long val) {
    _type = OValueType.Integer;
    _integer = val;
  }

  public void setDecimal(double val) {
    _type = OValueType.Decimal;
    _decimal = val;
  }

  public void setBigNumber(Number val) {
    _type = OValueType.BigNumber;
    _bignumber = val;
  }

  public void setBool(boolean val) {
    _type = OValueType.Boolean;
    _bool = val;
  }

  // ==================
  public Object getRaw() {
    switch (_type) {
      case String:
        return _string;
      case Integer:
        return _integer;
      case DateTime:
        return _date;
      case Boolean:
        return _bool;
      case Decimal:
        return _decimal;
      case BigNumber:
        return _bignumber;
      default:
        return null;
    }
  }

  /** 获取原始的整型值 */
  public long getRawInteger() {
    return _integer;
  }

  /** 获取真实的小数值 */
  public double getRawDecimal() {
    return _decimal;
  }

  /** 获取真实的字符串 */
  public String getRawString() {
    return _string;
  }

  /** 获取真实的布尔值 */
  public boolean getRawBoolean() {
    return _bool;
  }

  /** 获取真实的日期 */
  public Date getRawDate() {
    return _date;
  }

  public Number getRawBignumber() {
    return _bignumber;
  }

  public boolean isNull() {
    return _type == OValueType.Null;
  }

  // ==================

  /** 获取值为 char 类型（为序列化提供支持） */
  public char getChar() {
    switch (_type) {
      case Integer:
        return (char) _integer;
      case Decimal:
        return (char) _decimal;
      case BigNumber:
        return (char) _bignumber.longValue();
      case String:
        if (_string == null || _string.length() == 0) {
          return 0;
        }

        return _string.charAt(0);
      case Boolean:
        return _bool ? '1' : '0';
      case DateTime:
        return 0;
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
    switch (_type) {
      case Integer:
        return _integer;
      case Decimal:
        return (long) _decimal;
      case BigNumber:
        return _bignumber.longValue();
      case String:
        if (_string == null || _string.length() == 0) {
          return 0;
        }

        return Long.parseLong(_string);
      case Boolean:
        return _bool ? 1 : 0;
      case DateTime:
        return _date.getTime();
      default:
        return 0;
    }
  }

  public float getFloat() {
    return (float) getDouble();
  }

  /** 获取值为 double 类型 */
  public double getDouble() {
    switch (_type) {
      case Decimal:
        return _decimal;
      case Integer:
        return _integer;
      case BigNumber:
        return _bignumber.doubleValue();
      case String:
        if (_string == null || _string.length() == 0) {
          return 0;
        }

        return Double.parseDouble(_string);
      case Boolean:
        return _bool ? 1 : 0;
      case DateTime:
        return _date.getTime();
      default:
        return 0;
    }
  }

  /** 获取值为 string 类型 */
  public String getString() {
    switch (_type) {
      case String:
        return _string;
      case Integer:
        return String.valueOf(_integer);
      case Decimal:
        return String.valueOf(_decimal);
      case BigNumber:
        return String.valueOf(_bignumber);
      case Boolean:
        return String.valueOf(_bool);
      case DateTime:
        return String.valueOf(_date);
      default:
        return _n.co.nullString();
    }
  }

  public void setString(String val) {
    _type = OValueType.String;
    _string = val;
  }

  /** 获取值为 boolean 类型 */
  public boolean getBoolean() {
    switch (_type) {
      case Boolean:
        return _bool;
      case Integer:
        return _integer > 0;
      case Decimal:
        return _decimal > 0;
      case BigNumber:
        return _bignumber.longValue() > 0;
      default:
        return false;
    }
  }

  /** 获取值为 date 类型 */
  public Date getDate() {
    switch (_type) {
      case DateTime:
        return _date;
      case String:
        return parseDate(_string);
      case Integer:
        return new Date(_integer);
      default:
        return null;
    }
  }

  public void setDate(Date val) {
    _type = OValueType.DateTime;
    _date = val;
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

    if (o instanceof OValue) {
      OValue o2 = (OValue) o;
      switch (_type) {
        case String:
          return _string.equals(o2._string);
        case Integer:
          return _integer == o2._integer;
        case DateTime:
          return _date.equals(o2._date);
        case Boolean:
          return _bool == o2._bool;
        case Decimal:
          return _decimal == o2._decimal;
        case BigNumber:
          return _bignumber.equals(o2._bignumber);
        default:
          return isNull() && o2.isNull();
      }
    } else {

      switch (_type) {
        case String:
          return _string.equals(o);
        case Integer:
          if (o instanceof Number) {
            return ((Number) o).longValue() == _integer;
          } else {
            return false;
          }
        case DateTime:
          return _date.equals(o);
        case Boolean:
          if (o instanceof Boolean) {
            return _bool == (Boolean) o;
          } else {
            return false;
          }
        case Decimal:
          if (o instanceof Number) {
            return ((Number) o).doubleValue() == _decimal;
          } else {
            return false;
          }
        case BigNumber:
          return _bignumber.equals(o);
        default:
          return false;
      }
    }
  }

  @Override
  public int hashCode() {
    switch (_type) {
      case String:
        return _string.hashCode();
      case Integer:
        return Long.hashCode(_integer);
      case DateTime:
        return _date.hashCode();
      case Boolean:
        return Boolean.hashCode(_bool);
      case Decimal:
        return Double.hashCode(_decimal);
      case BigNumber:
        return _bignumber.hashCode();
      default:
        return 0;
    }
  }
}
