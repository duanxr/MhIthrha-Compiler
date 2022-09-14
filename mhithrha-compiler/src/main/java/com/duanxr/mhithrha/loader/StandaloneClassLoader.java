package com.duanxr.mhithrha.loader;

import com.google.common.base.Functions;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/5
 */
public final class StandaloneClassLoader extends RuntimeClassLoader {
  private final Set<String> findClassSet = ConcurrentHashMap.newKeySet();
  private final Function<String, byte[]> compiledClasses;
  public StandaloneClassLoader(ClassLoader parent, Function<String, byte[]> compiledClasses) {
    super(parent);
    this.compiledClasses = compiledClasses;
  }
  public synchronized Map<String, Class<?>> defineCompiledClasses(List<String> classNames) {
    synchronized (this) {
      return classNames.stream()
          .collect(Collectors.toMap(Functions.identity(), this::defineCompiledClass));
    }
  }
  @SneakyThrows
  public Class<?> defineCompiledClass(String name) {
    synchronized (this) {
      byte[] compiled = compiledClasses.apply(name);
      return compiled != null ? defineClass(name, compiled) : loadClass(name);
    }
  }

  @Override
  public Class<?> defineClass(String name, byte[] bytes) {
    return defineClass(name, bytes, 0, bytes.length);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    synchronized (this) {
      byte[] compiled = compiledClasses.apply(name);
      if (compiled != null) {
        return defineClass(name, compiled);
      }
      if (!findClassSet.add(name)) {
        throw new ClassNotFoundException(name);
      }
      try {
        return super.findClass(name);
      } finally {
        findClassSet.remove(name);
      }
    }
  }
}
