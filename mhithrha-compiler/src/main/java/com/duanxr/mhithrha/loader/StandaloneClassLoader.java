package com.duanxr.mhithrha.loader;

import com.google.common.base.Functions;
import java.lang.invoke.MethodHandles.Lookup;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/5
 */
public final class StandaloneClassLoader extends RuntimeClassLoader {
  private final Map<String, byte[]> defineTaskMap = new HashMap<>();
  public StandaloneClassLoader(ClassLoader parent) {
    super(parent);
  }

  public Map<String, Class<?>> defineClasses(Map<String, byte[]> classBytes) {
    Map<String, Class<?>> classes = new HashMap<>(classBytes.size());
    List<String> classNames = classBytes.keySet().stream().toList();
    synchronized (defineTaskMap) {
      defineTaskMap.putAll(classBytes);
      Map<String, Class<?>> classMap = classNames.stream()
          .collect(Collectors.toMap(Functions.identity(), this::defineTask));
      classBytes.keySet().forEach(defineTaskMap::remove);
    }
    return classes;
  }

  @Override
  public Class<?> defineReloadableClass(String name, byte[] bytes) {
    return new IsolatedClassLoader(this).defineClass(name, bytes);
  }
  @Override
  public Class<?> defineClass(String name, byte[] bytes) {
    return defineClass(name, bytes, 0, bytes.length);
  }
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    synchronized (defineTaskMap) {
      byte[] bytes = defineTaskMap.remove(name);
      if (bytes != null) {
        return defineClass(name, bytes);
      }
    }
    return loadClass(name);//todo java.lang.StackOverflowError
  }
  @SneakyThrows
  public Class<?> defineTask(String name) {
    synchronized (defineTaskMap) {
      byte[] bytes = defineTaskMap.remove(name);
      return bytes != null ? defineClass(name, bytes) : loadClass(name);
    }
  }
}
