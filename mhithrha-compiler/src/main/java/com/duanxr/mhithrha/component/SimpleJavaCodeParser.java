package com.duanxr.mhithrha.component;

import com.duanxr.mhithrha.RuntimeCompilerException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 段然 2022/9/5
 */
public class SimpleJavaCodeParser {

  private static final String CLASS_NAME_PATTERN_TEMPLATE = "(?<=(\\b)(%s)(\\b))(\\s+[^\\s{]+)(?=[\\s{])";
  private static final Pattern CLASS_NAME_PATTERN = Pattern.compile(
      CLASS_NAME_PATTERN_TEMPLATE.formatted(String.join("|",
          "class", "interface", "enum", "@interface", "record")));
  private static final Pattern PACKAGE_NAME_PATTERN = Pattern.compile(
      "(?<=package(\\b))([^;]+)(?=;)");

  public static String getFullClassName(String javaCode) {
    String packageName = findPackageName(javaCode);
    String className = findClassName(javaCode);
    return className == null || className.isEmpty() ? null
        : packageName == null || packageName.isEmpty() ? className : packageName + "." + className;
  }
  private static String findPackageName(String javaCode) {
    try {
      Matcher matcher = PACKAGE_NAME_PATTERN.matcher(javaCode);
      if (!matcher.find()) {
        return null;
      }
      return matcher.group().replaceAll("\\s", "");
    } catch (Exception e) {
      throw new RuntimeCompilerException("can't find package name", e);
    }
  }

  private static String findClassName(String javaCode) {
    try {
      Matcher matcher = CLASS_NAME_PATTERN.matcher(javaCode);
      if (!matcher.find()) {
        return null;
      }
      return matcher.group().replaceAll("\\s", "");
    } catch (Exception e) {
      throw new RuntimeCompilerException("can't find class name", e);
    }
  }

}
