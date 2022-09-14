package com.duanxr.mhithrha.component;

import com.duanxr.mhithrha.loader.RuntimeClassLoader;
import com.duanxr.mhithrha.resource.JavaMemoryClass;
import com.duanxr.mhithrha.resource.JavaMemoryCode;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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
  private final JavaCompiler compiler;
  private final RuntimeJavaFileManager fileManager;
  public CompilerCore(RuntimeClassLoader classLoader, JavaCompiler compiler,
      RuntimeJavaFileManager fileManager, ResourcesLoader resourcesLoader) {
    this.classLoader = classLoader;
    this.compiler = compiler;
    this.fileManager = fileManager;
  }
  public Map<String, Class<?>> compile(List<JavaMemoryCode> compilationUnits,
      Writer writer, DiagnosticListener<? super JavaFileObject> diagnosticListener,
      List<String> options) {
    fileManager.addCompileCode(compilationUnits);
    if (compiler.getTask(writer, fileManager, diagnosticListener, options, null, compilationUnits)
        .call()) {
      return classLoader.defineCompiledClasses(compilationUnits.stream().map(JavaMemoryCode::getClassName).collect(Collectors.toList()));
    }
    return null;
  }
}
