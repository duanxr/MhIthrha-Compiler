package com.duanxr.mhithrha.component;

import com.duanxr.mhithrha.RuntimeCompilerException;

/**
 * @author 段然 2022/9/5
 */
public class JavaClassParser {

  public static String getFullClassName(String javaCode) {
    String className = getFullClassNameSimple(javaCode);
    //Addmore
    return className;
  }

  private static String getFullClassNameSimple(String javaCode) {
    String packageName = findPackageName(javaCode);
    String className = findClassName(javaCode);
    return className == null || className.isEmpty() ? null
        : packageName == null || packageName.isEmpty() ? className : packageName + "." + className;
  }

  private static String findClassName(String javaCode) {
    try {
      int index0 = javaCode.indexOf("class ");
      if (index0 == -1) {
        return null;
      }
      int index1 = findFirst(javaCode, index0 + 6, ' ', '{');
      if (index1 == -1) {
        return null;
      }
      return javaCode.substring(index0 + 6, index1).trim();
    } catch (Exception e) {
      throw new RuntimeCompilerException("can't find class name", e);
    }
  }

  private static int findFirst(String javaCode, int fromIndex, char... chars) {
    int javaCodeLength = javaCode.length();
    for (int i = fromIndex; i < javaCodeLength; i++) {
      char c = javaCode.charAt(i);
      for (char aChar : chars) {
        if (c == aChar) {
          return i;
        }
      }
    }
    return -1;
  }

  private static String findPackageName(String javaCode) {
    try {
      int index = javaCode.indexOf("package");
      if (index == -1) {
        return null;
      }
      int endIndex = javaCode.indexOf(";", index);
      if (endIndex == -1) {
        return null;
      }
      return javaCode.substring(index + 8, endIndex).trim();
    } catch (Exception e) {
      throw new RuntimeCompilerException("can't find package name", e);
    }
  }

}
