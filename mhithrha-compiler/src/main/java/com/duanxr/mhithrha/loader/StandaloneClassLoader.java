package com.duanxr.mhithrha.loader;

/**
 * @author 段然 2022/9/5
 */
public final class StandaloneClassLoader extends RuntimeClassLoader {
  public StandaloneClassLoader(ClassLoader parent) {
    super(parent);
  }
  public StandaloneClassLoader() {
    super();
  }
  @Override
  public Class<?> defineClass(String name, byte[] bytes) {
    return defineClass(name, bytes, 0, bytes.length);
  }

}
