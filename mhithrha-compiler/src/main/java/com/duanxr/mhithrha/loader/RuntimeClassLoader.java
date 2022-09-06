package com.duanxr.mhithrha.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/5
 */
public class RuntimeClassLoader extends ClassLoader {

  private final Map<String, byte[]> defineCache = new HashMap<>();

  public RuntimeClassLoader(ClassLoader parent) {
    super(parent);
  }

  public RuntimeClassLoader() {
    super();
  }

  public Class<?> defineReloadableClass(String name, byte[] bytes) {
    return new ReloadableClassLoader(this).defineClass(name, bytes);
  }

  public Map<String, Class<?>> defineClasses(Map<String, byte[]> classBytes) {
    Map<String, Class<?>> classes = new HashMap<>(classBytes.size());
    List<DefineTask> tasks = classBytes.entrySet().stream().map(DefineTask::new).toList();
    synchronized (defineCache) {
      defineCache.putAll(classBytes);
      classes.putAll(
          tasks.stream().collect(Collectors.toMap(DefineTask::getName, DefineTask::define)));
      classBytes.keySet().forEach(defineCache::remove);
    }
    return classes;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    synchronized (defineCache) {
      byte[] bytes = defineCache.remove(name);
      if (bytes != null) {
        return defineClass(name, bytes);
      }
    }
    return loadClass(name);
  }

  public Class<?> defineClass(String name, byte[] bytes) {
    return defineClass(name, bytes, 0, bytes.length);
  }

  private class DefineTask {

    private final byte[] bytes;
    @Getter
    private final String name;

    public DefineTask(String name, byte[] bytes) {
      this.name = name;
      this.bytes = bytes;
    }

    public DefineTask(Map.Entry<String, byte[]> entry) {
      this.name = entry.getKey();
      this.bytes = entry.getValue();
    }

    @SneakyThrows
    public Class<?> define() {
      synchronized (defineCache) {
        return defineCache.containsKey(name) ? defineClass(name, bytes) : findClass(name);
      }
    }
  }

}
