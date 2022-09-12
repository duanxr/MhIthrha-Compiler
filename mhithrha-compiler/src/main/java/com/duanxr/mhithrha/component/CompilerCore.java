package com.duanxr.mhithrha.component;

import com.duanxr.mhithrha.loader.RuntimeClassLoader;
import com.duanxr.mhithrha.resource.JavaMemoryClass;
import com.duanxr.mhithrha.resource.JavaMemoryCode;
import java.io.Writer;
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
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/5
 */
public class CompilerCore {

  private final RuntimeClassLoader classLoader;
  private final Map<String, Class<?>> classesCache;
  private final JavaCompiler compiler;
  private final RuntimeJavaFileManager fileManager;

  public CompilerCore(RuntimeClassLoader classLoader, JavaCompiler compiler,
      RuntimeJavaFileManager fileManager, ResourcesLoader resourcesLoader) {
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

  public Map<String, Class<?>> compile(List<JavaMemoryCode> compilationUnits,
      Writer writer, DiagnosticListener<? super JavaFileObject> diagnosticListener,
      List<String> options) {
    fileManager.addCompileCode(compilationUnits);
    Boolean success = compiler.getTask(writer, fileManager, diagnosticListener, options, null,
        compilationUnits).call();
    if (!success) {
      return null;
    }
    return defineClasses(fileManager.getCompiledClasses());
  }

  @SneakyThrows
  private Map<String, Class<?>> defineClasses(Map<String, JavaMemoryClass> outputClasses) {
    synchronized (classesCache) {
      List<Map.Entry<String, JavaMemoryClass>> entries = new ArrayList<>();
      for (Map.Entry<String, JavaMemoryClass> entry : outputClasses.entrySet()) {
        String className = entry.getKey();
        if (!classesCache.containsKey(className)) {
          entries.add(entry);
        }
      }
      Map<String, byte[]> collect = entries.stream()
          .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getBytes()));
      Map<String, Class<?>> defineClasses = classLoader.defineClasses(collect);
      classesCache.putAll(defineClasses);
      return defineClasses;
    }
  }

}
