package com.duanxr.mhithrha;


import com.duanxr.mhithrha.component.CompileDiagnosticListener;
import com.duanxr.mhithrha.component.CompilerCore;
import com.duanxr.mhithrha.component.JavaCodeParser;
import com.duanxr.mhithrha.component.JavaCompilerFactory;
import com.duanxr.mhithrha.component.ResourcesLoader;
import com.duanxr.mhithrha.component.RuntimeJavaFileManager;
import com.duanxr.mhithrha.loader.IntrusiveClassLoader;
import com.duanxr.mhithrha.loader.RuntimeClassLoader;
import com.duanxr.mhithrha.loader.StandaloneClassLoader;
import com.duanxr.mhithrha.resource.JavaMemoryCode;
import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.tools.JavaCompiler;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/8/29
 */
public class RuntimeCompiler {

  private static final List<String> DEFAULT_OPTIONS = Arrays.asList("-g", "-nowarn");
  private static final PrintWriter DEFAULT_WRITER = new PrintWriter(System.err);
  private final CompilerCore compilerCore;
  @Getter
  private final Configuration configuration;
  private final RuntimeClassLoader runtimeClassLoader;
  private final RuntimeJavaFileManager runtimeJavaFileManager;

  private RuntimeCompiler(JavaCompiler javaCompiler, ClassLoader classLoader, Charset charset,
      boolean intrusive, long compilationTimeout) {
    classLoader =
        classLoader != null ? classLoader : Thread.currentThread().getContextClassLoader();
    charset = charset != null ? charset : StandardCharsets.UTF_8;
    this.configuration = new Configuration(javaCompiler, classLoader, charset, intrusive,
        compilationTimeout);
    this.runtimeClassLoader =
        intrusive ? new IntrusiveClassLoader(classLoader) : new StandaloneClassLoader(classLoader);
    ResourcesLoader resourcesLoader = new ResourcesLoader();//todo if springboot
    this.runtimeJavaFileManager = new RuntimeJavaFileManager(
        javaCompiler.getStandardFileManager(null, null, charset),
        runtimeClassLoader, resourcesLoader);
    this.compilerCore = new CompilerCore(runtimeClassLoader, javaCompiler, runtimeJavaFileManager,
        resourcesLoader);
  }

  public static Builder builder() {
    return new Builder();
  }

  public void addModule(Module module) {
    runtimeJavaFileManager.addModule(module);
  }

  @SneakyThrows
  public void addExtraArchive(File file) {
    runtimeClassLoader.addArchive(file);
    Class<?> aClass = runtimeClassLoader.loadClass("com.alibaba.fastjson.JSONObject");
    runtimeJavaFileManager.addExtraArchive(file);
  }

  @SneakyThrows
  public void addExtraClass(File file) {
    byte[] classContent = Files.readAllBytes(file.toPath());
    Class<?> clazz = runtimeClassLoader.defineClass(null, classContent);
    String name = clazz.getName();
    runtimeJavaFileManager.addExtraClass(name, file);
  }

  public Class<?> compile(String className, String javaCode, PrintWriter writer,
      List<String> optionList) {
    return compile(compilerCore, className, javaCode, writer, optionList);
  }

  @SneakyThrows
  private Class<?> compile(CompilerCore compilerCore, String className, String javaCode,
      PrintWriter writer, List<String> optionList) {
    if (javaCode == null || javaCode.isEmpty()) {
      throw new IllegalArgumentException("java code is empty");
    }
    if (className == null || className.isEmpty()) {
      className = JavaCodeParser.getFullClassName(javaCode);
    }
    if (writer == null) {
      writer = DEFAULT_WRITER;
    }
    if (optionList == null || optionList.isEmpty()) {
      optionList = DEFAULT_OPTIONS;
    }
    CompileDiagnosticListener diagnosticListener = new CompileDiagnosticListener();
    List<JavaMemoryCode> javaFileObjects = Collections.singletonList(
        new JavaMemoryCode(className, javaCode));
    Map<String, Class<?>> classMap = compilerCore.compile(javaFileObjects, writer,
        diagnosticListener, optionList);
    if (classMap == null) {
      throw new RuntimeCompilerException(diagnosticListener.getError());
    }
    Class<?> clazz = classMap.get(className);
    return clazz == null ? compilerCore.load(className) : clazz;
  }

  public Class<?> compile(String className, String javaCode, List<String> optionList) {
    return compile(compilerCore, className, javaCode, null, optionList);
  }

  public Class<?> compile(String className, String javaCode, PrintWriter writer) {
    return compile(compilerCore, className, javaCode, writer, null);
  }

  public Class<?> compile(String javaCode, PrintWriter writer) {
    return compile(compilerCore, null, javaCode, writer, null);
  }

  public Class<?> compile(String className, String javaCode) {
    return compile(compilerCore, className, javaCode, null, null);
  }

  public Class<?> compile(String javaCode, PrintWriter writer, List<String> optionList) {
    return compile(compilerCore, null, javaCode, writer, optionList);
  }

  public Class<?> compile(String javaCode, List<String> optionList) {
    return compile(compilerCore, null, javaCode, null, optionList);
  }

  public Class<?> compile(String javaCode) {
    return compile(compilerCore, null, javaCode, null, null);
  }

  public ClassLoader getRuntimeClassLoader() {
    return this.runtimeClassLoader;
  }

  public record Configuration(JavaCompiler javaCompiler, ClassLoader classLoader,
                              Charset charset, boolean intrusive,
                              long compilationTimeout) {

  }

  public static class Builder {

    private Charset charset;
    private ClassLoader classLoader;
    private long compilationTimeout;
    private boolean intrusive;

    private Builder() {
      this.charset = null;
      this.classLoader = null;
      this.intrusive = false;
      this.compilationTimeout = -1;
    }

    public Builder withClassLoader(ClassLoader classLoader) {
      this.classLoader = Objects.requireNonNull(classLoader);
      return this;
    }

    public Builder withCharset(Charset charset) {
      this.charset = Objects.requireNonNull(charset);
      return this;
    }

    public Builder intrusive(boolean intrusive) {
      this.intrusive = intrusive;
      return this;
    }

    public Builder compilationTimeout(long milliseconds) {
      this.compilationTimeout = milliseconds;
      return this;
    }

    public RuntimeCompiler withEclipseCompiler() {
      return new RuntimeCompiler(JavaCompilerFactory.getEclipseCompiler(), classLoader, charset,
          intrusive, compilationTimeout);
    }

    public RuntimeCompiler withJdkCompiler() {
      return new RuntimeCompiler(JavaCompilerFactory.getJdkCompiler(), classLoader, charset,
          intrusive, compilationTimeout);
    }

    public RuntimeCompiler withJavacCompiler() {
      return new RuntimeCompiler(JavaCompilerFactory.getJavacCompiler(), classLoader, charset,
          intrusive, compilationTimeout);
    }

    public RuntimeCompiler withCustomCompiler(JavaCompiler javaCompiler) {
      return new RuntimeCompiler(javaCompiler, classLoader, charset, intrusive,
          compilationTimeout);
    }

  }
}
