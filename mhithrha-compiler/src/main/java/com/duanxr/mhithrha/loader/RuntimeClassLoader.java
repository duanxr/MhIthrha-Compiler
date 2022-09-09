package com.duanxr.mhithrha.loader;

import java.util.Map;

/**
 * @author 段然 2022/9/7
 */
public sealed abstract class RuntimeClassLoader extends ClassLoader permits
    StandaloneClassLoader, IntrusiveClassLoader {

  public RuntimeClassLoader(ClassLoader parent) {
    super(parent);
  }
  public abstract Map<String, Class<?>> defineClasses(Map<String, byte[]> classBytes);
  public abstract Class<?> defineReloadableClass(String name, byte[] bytes);
  public abstract Class<?> defineClass(String name, byte[] bytes);


}
