package com.duanxr.mhithrha.component;

import java.util.Arrays;
import lombok.experimental.UtilityClass;

/**
 * @author 段然 2022/9/7
 */
@UtilityClass
public class JavaNameUtil {

  //including sub package
  public static boolean inPackages(String packageName, String targetPackageName) {
    return inPackage(packageName, targetPackageName)
        || (packageName.startsWith(targetPackageName)
            && packageName.charAt(targetPackageName.length()) == '.');
  }

  public static boolean inPackage(String packageName, String targetPackageName) {
    return packageName.equals(targetPackageName);
  }

  public static String toPackageName(String className) {
    char[] classNameChars = className.toCharArray();
    int packageIndex = -1;
    for (int i = classNameChars.length - 1; i >= 0; i--) {
      char c = classNameChars[i];
      if (packageIndex == -1) {
        if (c == '/' || c == '.') {
          packageIndex = i;
        }
      } else {
        if (c == '/') {
          classNameChars[i] = '.';
        }
      }
    }
    return packageIndex == -1 ? "" : new String(Arrays.copyOf(classNameChars, packageIndex));
  }

  public static String toJavaName(String name) {
    char[] classNameChars = name.toCharArray();
    for (int i = 0, max = classNameChars.length; i < max; i++) {
      if (classNameChars[i] == '/') {
        classNameChars[i] = '.';
      }
    }
    return new String(classNameChars);
  }

  public static String toURI(String name) {
    char[] classNameChars = name.toCharArray();
    for (int i = 0, max = classNameChars.length; i < max; i++) {
      switch (classNameChars[i]) {
        case '\\', '.' -> classNameChars[i] = '/';
      }
    }
    return new String(classNameChars);
  }

  public static String removeSuffix(String name) {
    int index = name.lastIndexOf('.');
    if (index == -1) {
      return name;
    }
    return name.substring(0, index);
  }
}
