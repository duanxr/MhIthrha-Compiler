package com.duanxr.mhithrha;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author 段然 2022/9/5
 */
public class ClassPromise implements Supplier<Class<?>> {

  @Getter
  private final String name;
  private final Function<String, Class<?>> function;

  @Getter(lazy = true, value = AccessLevel.PRIVATE)
  private final Class<?> clazz = loadClass();

  public ClassPromise(String name, Function<String, Class<?>> function) {
    this.name = name;
    this.function = function;
  }

  private Class<?> loadClass() {
    return Objects.requireNonNull(function).apply(name);
  }

  @Override
  public Class<?> get() {
    return getClazz();
  }


}
