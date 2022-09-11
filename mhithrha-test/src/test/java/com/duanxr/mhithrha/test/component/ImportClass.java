package com.duanxr.mhithrha.test.component;

import java.util.function.Supplier;

/**
 * @author 段然 2022/9/10
 */
public class ImportClass implements Supplier<String> {

  @Override
  public String get() {
    return "I'm ImportClass";
  }
}
