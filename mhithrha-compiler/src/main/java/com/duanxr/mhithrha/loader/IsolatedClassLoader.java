package com.duanxr.mhithrha.loader;

/**
 * @author 段然 2022/9/5
 */
public class IsolatedClassLoader extends ClassLoader {
  public IsolatedClassLoader(ClassLoader parent) {
    super(parent);
  }
  public IsolatedClassLoader() {
    super();
  }
  public Class<?> defineClass(String name, byte[] bytes) {
    return defineClass(name, bytes, 0, bytes.length);
  }

}
