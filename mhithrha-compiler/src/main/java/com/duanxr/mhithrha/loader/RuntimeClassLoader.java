package com.duanxr.mhithrha.loader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/7
 */
public sealed abstract class RuntimeClassLoader extends ClassLoader permits
    StandaloneClassLoader, IntrusiveClassLoader {
  private final Map<String, byte[]> defineTaskMap = new HashMap<>();
  public RuntimeClassLoader(ClassLoader parent) {
    super(parent);
  }
  public RuntimeClassLoader() {
    super();
  }
  public Map<String, Class<?>> defineClasses(Map<String, byte[]> classBytes) {
    Map<String, Class<?>> classes = new HashMap<>(classBytes.size());
    List<DefineTask> tasks = classBytes.entrySet().stream().map(DefineTask::new).toList();
    synchronized (defineTaskMap) {
      defineTaskMap.putAll(classBytes);
      classes.putAll(
          tasks.stream()
              .collect(Collectors.toMap(DefineTask::getName, DefineTask::define)));
      classBytes.keySet().forEach(defineTaskMap::remove);
    }
    return classes;
  }
  public Class<?> defineIsolatedClass(String name, byte[] bytes) {
    return new IsolatedClassLoader(this).defineClass(name, bytes);
  }
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    synchronized (defineTaskMap) {
      byte[] bytes = defineTaskMap.remove(name);
      if (bytes != null) {
        return defineClass(name, bytes);
      }
    }
    return loadClass(name);
  }

  public abstract Class<?> defineClass(String name, byte[] bytes);

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
      synchronized (defineTaskMap) {
        return defineTaskMap.containsKey(name) ? defineClass(name, bytes) : findClass(name);
      }
    }
  }

}
