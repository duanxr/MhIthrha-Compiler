package com.duanxr.mhithrha.loader;

import com.duanxr.mhithrha.component.CompiledClassSupplier;
import com.google.common.base.Functions;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/5
 */
public final class StandaloneClassLoader extends RuntimeClassLoader {

  private final CompiledClassSupplier compiledClassSupplier;
  private final Set<String> findClassSet;
  private final Map<String, Class<?>> loadedClasses;

  public StandaloneClassLoader(ClassLoader parent, CompiledClassSupplier compiledClassSupplier) {
    super(parent);
    this.compiledClassSupplier = compiledClassSupplier;
    this.findClassSet = ConcurrentHashMap.newKeySet();
    this.loadedClasses = new ConcurrentHashMap<>();
  }

  @Override
  public Class<?> loadClass(String name) throws ClassNotFoundException {
    Class<?> loadedClass = loadedClasses.get(name);
    return loadedClass == null ? super.loadClass(name) : loadedClass;
  }

  @Override
  public void closeForReal() {
    super.closeForReal();
    findClassSet.clear();
    loadedClasses.clear();
  }


  @Override
  public Class<?> defineClass(String name, byte[] bytes) {
    return name == null ? super.defineClass(null, bytes, 0, bytes.length)
        : loadedClasses.computeIfAbsent(name, n -> super.defineClass(n, bytes, 0, bytes.length));
  }

  @Override
  @SneakyThrows
  protected Class<?> findClass(String name) {
    synchronized (this) {
      byte[] compiled = compiledClassSupplier.getCompiledClass(name);
      if (compiled != null) {
        return defineClass(name, compiled);
      }
      if (!findClassSet.add(name)) {
        throw new ClassNotFoundException(name);
      }
      try {
        return super.findClass(name);
      } finally {
        findClassSet.remove(name);
      }
    }
  }


  @Override
  public Map<String, Class<?>> defineCompiledClass(Collection<String> compiledClassNames) {
    synchronized (this) {
      return compiledClassNames.stream()
          .collect(Collectors.toMap(Functions.identity(), this::findClass));
    }
  }


}
