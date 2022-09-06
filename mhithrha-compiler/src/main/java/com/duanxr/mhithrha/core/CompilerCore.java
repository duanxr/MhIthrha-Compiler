package com.duanxr.mhithrha.core;

import com.duanxr.mhithrha.RuntimeJavaFileManager;
import com.duanxr.mhithrha.component.JavaMemoryClass;
import com.duanxr.mhithrha.loader.RuntimeClassLoader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/5
 */
@Getter
public class CompilerCore {

  private final RuntimeClassLoader classLoader;
  private final Map<String, Class<?>> classesCache;
  private final JavaCompiler compiler;
  private final RuntimeJavaFileManager fileManager;

  public CompilerCore(RuntimeClassLoader classLoader, JavaCompiler compiler,
      RuntimeJavaFileManager fileManager) {
    this.classLoader = classLoader;
    this.compiler = compiler;
    this.fileManager = fileManager;
    this.classesCache = Collections.synchronizedMap(
        new WeakHashMap<>());
  }

  @SneakyThrows
  public Class<?> load(String className) {
    Class<?> clazz = null;
    synchronized (classesCache) {
      clazz = classesCache.get(className);
    }
    if (clazz != null) {
      return clazz;
    }
    synchronized (classesCache) {
      classesCache.put(className, clazz = classLoader.loadClass(className));
    }
    return clazz;
  }

  public boolean compile(List<JavaFileObject> compilationUnits, PrintWriter printWriter,
      DiagnosticListener<? super JavaFileObject> diagnosticListener, List<String> options) {
    Boolean success = compiler.getTask(printWriter, fileManager, diagnosticListener, options, null,
        compilationUnits).call();
    if (success) {
      defineClasses(fileManager.getOutputClasses());
    }
    return success;
  }

  @SneakyThrows
  private void defineClasses(Map<String, JavaMemoryClass> outputClasses) {
    synchronized (classesCache) {
      List<Map.Entry<String, JavaMemoryClass>> entries = new ArrayList<>();
      for (Map.Entry<String, JavaMemoryClass> entry : outputClasses.entrySet()) {
        String className = entry.getKey();
        className = className.replaceAll("/", ".");
        if (!classesCache.containsKey(className)) {
          entries.add(entry);
        }
      }
      if (entries.size() == 1) {
        Entry<String, JavaMemoryClass> entry = entries.get(0);
        Class<?> clazz = classLoader.defineClass(entry.getKey(), entry.getValue().getClassBytes());
        classesCache.put(entry.getKey(), clazz);
      } else if (entries.size() > 1) {
        Map<String, byte[]> collect = entries.stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getClassBytes()));
        Map<String, Class<?>> stringClassMap = classLoader.defineClasses(collect);
        classesCache.putAll(stringClassMap);
      }
    }
  }

}
