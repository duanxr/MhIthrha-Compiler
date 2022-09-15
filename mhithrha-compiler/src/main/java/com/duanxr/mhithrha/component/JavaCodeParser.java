package com.duanxr.mhithrha.component;

import com.google.common.base.Strings;

/**
 * @author 段然 2022/9/5
 */
public class JavaCodeParser {
  public static String getFullClassName(String javaCode) {
    String className = SimpleJavaCodeParser.getFullClassName(javaCode);
    if (!Strings.isNullOrEmpty(className)) {
      return className;
    }
    return null;
  }

}
