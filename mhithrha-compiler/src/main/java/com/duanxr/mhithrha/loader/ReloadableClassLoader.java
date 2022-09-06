package com.duanxr.mhithrha.loader;

/**
 * @author 段然 2022/9/5
 */
public class ReloadableClassLoader extends ClassLoader {

  public ReloadableClassLoader(ClassLoader parent) {
    super(parent);
  }

  public ReloadableClassLoader() {
    super();
  }

  public Class<?> defineClass(String name, byte[] bytes) {
    return defineClass(name, bytes, 0, bytes.length);
  }

}
