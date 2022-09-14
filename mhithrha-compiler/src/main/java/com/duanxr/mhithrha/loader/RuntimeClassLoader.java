package com.duanxr.mhithrha.loader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/7
 */
public sealed abstract class RuntimeClassLoader extends URLClassLoader permits
    StandaloneClassLoader, IntrusiveClassLoader {
  public RuntimeClassLoader(ClassLoader parent) {
    super(new URL[0], parent);
  }
  public abstract Map<String, Class<?>> defineCompiledClasses(List<String> classNames);
  public abstract Class<?> defineClass(String name, byte[] bytes);
  @SneakyThrows
  public void addArchive(File file) {
    addURL(file.toURI().toURL());
  }
  @Override
  public void close() {
  }
  @SneakyThrows
  public void closeForReal() {
    super.close();
  }

}
