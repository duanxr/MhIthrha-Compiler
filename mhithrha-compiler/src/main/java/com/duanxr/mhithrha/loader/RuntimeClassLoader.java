package com.duanxr.mhithrha.loader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Map;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/7
 */
public sealed abstract class RuntimeClassLoader extends URLClassLoader permits
    StandaloneClassLoader, IntrusiveClassLoader {
  public RuntimeClassLoader(ClassLoader parent) {
    super(new URL[0], parent);
  }
  public abstract Class<?> defineClass(String name, byte[] bytes);
  @SneakyThrows
  public void addExtraArchive(File file) {
    addURL(file.toURI().toURL());
  }
  @Override
  public void close() {
  }
  @SneakyThrows
  public void closeForReal() {
    super.close();
  }

  public abstract Map<String, Class<?>> defineCompiledClass(Collection<String> compiledClassNames);
}
