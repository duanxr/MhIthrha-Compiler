package com.duanxr.mhithrha.component;

import lombok.experimental.UtilityClass;

/**
 * @author 段然 2022/9/7
 */
@UtilityClass
public class NameConvertor {

  public static String denormalize(String className) {
    char[] classNameChars = className.toCharArray();
    for (int i = 0, max = classNameChars.length; i < max; i++) {
      if (classNameChars[i] == '/') {
        classNameChars[i] = '.';
      }
    }
    return new String(classNameChars);
  }

  public static String normalize(String className) {
    char[] classNameChars = className.toCharArray();
    for (int i = 0, max = classNameChars.length; i < max; i++) {
      switch (classNameChars[i]) {
        case '\\', '.' -> classNameChars[i] = '/';
      }
    }
    return new String(classNameChars);
  }
}
