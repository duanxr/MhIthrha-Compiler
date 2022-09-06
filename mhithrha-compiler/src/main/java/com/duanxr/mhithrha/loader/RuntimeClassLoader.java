package com.duanxr.mhithrha.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/5
 */
public class RuntimeClassLoader extends ClassLoader {

  public RuntimeClassLoader(ClassLoader parent) {
    super(parent);
  }

  public RuntimeClassLoader() {
    super();
  }

  public Class<?> defineClass(String name, byte[] bytes) {
    return defineClass(name, bytes, 0, bytes.length);
  }

  public Class<?> defineReloadableClass(String name, byte[] bytes) {
    ReloadableClassLoader reloadableClassLoader = new ReloadableClassLoader(this);
    return reloadableClassLoader.defineClass(name, bytes);
  }

  @SneakyThrows
  private synchronized void define(Map<String, Class<?>> classes, String name, byte[] bytes) {
    if (classByteCache.containsKey(name)) {
      classes.put(name, defineClass(name, bytes));
    } else {
      classes.put(name, findClass(name));
    }
  }


  public Map<String, Class<?>> defineClasses(Map<String, byte[]> classBytes) {
    Map<String, Class<?>> classes = new HashMap<>(classBytes.size());
    List<Runnable> runnableList = new ArrayList<>(classBytes.size());
    classBytes.entrySet().stream()
        .map(entry -> (Runnable) () -> define(classes, entry.getKey(), entry.getValue()))
        .forEach(runnableList::add);
    synchronized (this) {
      this.classByteCache = classBytes;
      for (Runnable runnable : runnableList) {
        runnable.run();
      }
      this.classByteCache = null;
    }
    return classes;
  }

  private volatile Map<String, byte[]> classByteCache = null;

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    synchronized (this) {
      if (classByteCache != null) {
        byte[] bytes = classByteCache.remove(name);
        if (bytes != null) {
          return defineClass(name, bytes);
        }
      }
    }
    return loadClass(name);
  }

}
