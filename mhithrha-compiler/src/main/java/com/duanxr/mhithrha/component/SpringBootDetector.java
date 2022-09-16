package com.duanxr.mhithrha.component;

import java.util.Set;

/**
 * @author 段然 2022/9/16
 */
public class SpringBootDetector {

  private static final Set<String> NAMES = Set.of(
      "org.springframework.boot.loader.LaunchedURLClassLoader");
  public static ClassLoader findSpringBootClassLoader() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    while (classLoader != null) {
      String name = classLoader.getClass().getName();
      if (NAMES.contains(name)) {
        return classLoader;
      }
      classLoader = classLoader.getParent();
    }
    return null;
  }
}
