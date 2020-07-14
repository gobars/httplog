package com.github.gobars.httplog.snack.to;

import com.github.gobars.httplog.snack.Odata;
import com.github.gobars.httplog.snack.Onode;
import com.github.gobars.httplog.snack.Ovalue;
import com.github.gobars.httplog.snack.core.Cnf;
import com.github.gobars.httplog.snack.core.Ctx;
import com.github.gobars.httplog.snack.core.Feature;
import com.github.gobars.httplog.snack.core.exts.ThData;
import com.github.gobars.httplog.snack.core.utils.IOUtil;
import com.github.gobars.httplog.snack.core.utils.TypeUtil;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Iterator;

/**
 * Json 序列化
 *
 * <p>将ONode 转为 json string
 */
public class JsonToer implements Toer {
  private static final ThData<StringBuilder> tlBuilder =
      new ThData<>(() -> new StringBuilder(1024 * 5));

  @Override
  public void handle(Ctx ctx) {
    Onode o = (Onode) ctx.source;

    if (null != o) {
      StringBuilder sb = tlBuilder.get();
      sb.setLength(0);

      analyse(ctx.config, o, sb);

      ctx.target = sb.toString();
    }
  }

  public void analyse(Cnf cnf, Onode o, StringBuilder sb) {
    if (o == null) {
      return;
    }

    switch (o.nodeType()) {
      case Value:
        writeValue(cnf, sb, o.nodeData());
        break;

      case Array:
        writeArray(cnf, sb, o.nodeData());
        break;

      case Object:
        writeObject(cnf, sb, o.nodeData());
        break;

      default:
        sb.append("null");
        break;
    }
  }

  private void writeArray(Cnf cnf, StringBuilder sBuf, Odata d) {
    sBuf.append("[");
    Iterator<Onode> iterator = d.array.iterator();
    while (iterator.hasNext()) {
      Onode sub = iterator.next();
      analyse(cnf, sub, sBuf);
      if (iterator.hasNext()) {
        sBuf.append(",");
      }
    }
    sBuf.append("]");
  }

  private void writeObject(Cnf cnf, StringBuilder sBuf, Odata d) {
    sBuf.append("{");
    Iterator<String> itr = d.object.keySet().iterator();
    while (itr.hasNext()) {
      String k = itr.next();
      writeName(cnf, sBuf, k);
      sBuf.append(":");
      analyse(cnf, d.object.get(k), sBuf);
      if (itr.hasNext()) {
        sBuf.append(",");
      }
    }
    sBuf.append("}");
  }

  private void writeValue(Cnf cnf, StringBuilder sBuf, Odata d) {
    Ovalue v = d.value;
    switch (v.type()) {
      case Null:
        sBuf.append("null");
        break;

      case String:
        writeValString(cnf, sBuf, v.getRawString(), true);
        break;

      case DateTime:
        writeValDate(cnf, sBuf, v.getRawDate());
        break;

      case Boolean:
        writeValBool(cnf, sBuf, v.getRawBoolean());
        break;

      case BigNumber:
        writeValBignum(cnf, sBuf, v.getRawBignumber()); // 添加对大数字的处理
        break;

      case Integer:
        sBuf.append(v.getRawInteger());
        break;

      case Decimal:
        sBuf.append(v.getRawDecimal());
        break;

      default:
        sBuf.append(v.getString());
        break;
    }
  }

  private void writeName(Cnf cnf, StringBuilder sBuf, String val) {
    if (cnf.hasFeature(Feature.QuoteFieldNames)) {
      if (cnf.hasFeature(Feature.UseSingleQuotes)) {
        sBuf.append("'").append(val).append("'");
      } else {
        sBuf.append("\"").append(val).append("\"");
      }
    } else {
      sBuf.append(val);
    }
  }

  private void writeValDate(Cnf cnf, StringBuilder sBuf, Date val) {
    if (cnf.hasFeature(Feature.WriteDateUseTicks)) {
      sBuf.append(val.getTime());
    } else if (cnf.hasFeature(Feature.WriteDateUseFormat)) {
      writeValString(cnf, sBuf, cnf.dateToString(val), false);
    } else {
      sBuf.append("new Date(").append(val.getTime()).append(")");
    }
  }

  private void writeValBool(Cnf cnf, StringBuilder sBuf, Boolean val) {
    if (cnf.hasFeature(Feature.WriteBoolUse01)) {
      sBuf.append(val ? 1 : 0);
    } else {
      sBuf.append(val ? "true" : "false");
    }
  }

  private void writeValBignum(Cnf cnf, StringBuilder sBuf, Number val) {
    String sVal = val.toString();

    if (val instanceof BigInteger) {
      BigInteger v = (BigInteger) val;
      // 数字太大时，可用string来表示；
      if (sVal.length() > 16
          && (v.compareTo(TypeUtil.INT_LOW) < 0 || v.compareTo(TypeUtil.INT_HIGH) > 0)
          && cnf.hasFeature(Feature.BrowserCompatible)) {
        writeValString(cnf, sBuf, sVal, false);
      } else {
        sBuf.append(sVal);
      }
      return;
    }

    if (val instanceof BigDecimal) {
      BigDecimal v = (BigDecimal) val;
      // 数字太大时，可用string来表示；
      if (sVal.length() > 16
          && (v.compareTo(TypeUtil.DEC_LOW) < 0 || v.compareTo(TypeUtil.DEC_HIGH) > 0)
          && cnf.hasFeature(Feature.BrowserCompatible)) {
        writeValString(cnf, sBuf, sVal, false);
      } else {
        sBuf.append(sVal);
      }
      return;
    }

    sBuf.append(sVal);
  }

  private void writeValString(Cnf cnf, StringBuilder sBuf, String val, boolean isStr) {
    // 引号开始
    boolean useSingleQuotes = cnf.hasFeature(Feature.UseSingleQuotes);
    char quote = (useSingleQuotes ? '\'' : '\"');
    sBuf.append(quote);

    // 内容
    if (isStr) {
      boolean isCompatible = cnf.hasFeature(Feature.BrowserCompatible);
      boolean isSecure = cnf.hasFeature(Feature.BrowserSecure);
      for (int i = 0, len = val.length(); i < len; i++) {
        char c = val.charAt(i);

        // 引号转义处理 + 特殊字符必须码
        if (c == quote
            || c == '\\'
            || c == '\n'
            || c == '\r'
            || c == '\t'
            || c == '\f'
            || c == '\b'
            || c <= '\7') {
          sBuf.append("\\");
          sBuf.append(IOUtil.CHARS_MARK[c]);
          continue;
        }
        if (isSecure) {
          if (c == '(' || c == ')' || c == '<' || c == '>') {
            sBuf.append('\\');
            sBuf.append('u');
            sBuf.append(IOUtil.DIGITS[(c >>> 12) & 15]);
            sBuf.append(IOUtil.DIGITS[(c >>> 8) & 15]);
            sBuf.append(IOUtil.DIGITS[(c >>> 4) & 15]);
            sBuf.append(IOUtil.DIGITS[c & 15]);
            continue;
          }
        }

        if (isCompatible) {
          // 对不可见ASC码，进行编码处理
          if (c < 32) {
            sBuf.append('\\');
            sBuf.append('u');
            sBuf.append('0');
            sBuf.append('0');
            sBuf.append(IOUtil.DIGITS[(c >>> 4) & 15]);
            sBuf.append(IOUtil.DIGITS[c & 15]);
            continue;
          }
          // 对大码，进行编码处理
          if (c >= 127) {
            sBuf.append('\\');
            sBuf.append('u');
            sBuf.append(IOUtil.DIGITS[(c >>> 12) & 15]);
            sBuf.append(IOUtil.DIGITS[(c >>> 8) & 15]);
            sBuf.append(IOUtil.DIGITS[(c >>> 4) & 15]);
            sBuf.append(IOUtil.DIGITS[c & 15]);
            continue;
          }
        }

        sBuf.append(c);
      }
    } else {
      // 非字符串直接添加
      sBuf.append(val);
    }

    // 引号结束
    sBuf.append(quote);
  }
}
