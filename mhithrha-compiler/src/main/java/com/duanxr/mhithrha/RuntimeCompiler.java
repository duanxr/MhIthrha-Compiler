package com.duanxr.mhithrha;


import com.duanxr.mhithrha.component.CompileDiagnosticListener;
import com.duanxr.mhithrha.component.CompiledClassSupplier;
import com.duanxr.mhithrha.component.JavaCodeParser;
import com.duanxr.mhithrha.component.JavaCompilerFactory;
import com.duanxr.mhithrha.component.ResourcesLoader;
import com.duanxr.mhithrha.component.RuntimeJavaFileManager;
import com.duanxr.mhithrha.component.RuntimeJavaFileManager.CompilationInterceptor;
import com.duanxr.mhithrha.loader.IntrusiveClassLoader;
import com.duanxr.mhithrha.loader.RuntimeClassLoader;
import com.duanxr.mhithrha.loader.StandaloneClassLoader;
import com.duanxr.mhithrha.resource.JavaMemoryCode;
import com.google.common.base.Strings;
import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/8/29
 */
public class RuntimeCompiler {

  private static final long DEFAULT_COMPILATION_TIMEOUT = 30000L;
  private static final List<String> DEFAULT_OPTIONS = List.of("-g", "-nowarn");
  private static final PrintWriter DEFAULT_WRITER = new PrintWriter(System.err);

  private static final JavaCompileSetting DEFAULT_COMPILE_SETTING = JavaCompileSetting.builder()
      .options(DEFAULT_OPTIONS).writer(DEFAULT_WRITER).build();
  @Getter
  private final Configuration configuration;
  private final RuntimeClassLoader runtimeClassLoader;
  private final RuntimeJavaFileManager runtimeJavaFileManager;

  private RuntimeCompiler(JavaCompiler javaCompiler, ClassLoader classLoader, Charset charset,
      boolean intrusive, long compilationTimeout) {
    classLoader = classLoader == null ?
        Thread.currentThread().getContextClassLoader() : classLoader;
    charset = charset == null ?
        StandardCharsets.UTF_8 : charset;
    compilationTimeout = compilationTimeout < 0L ?
        DEFAULT_COMPILATION_TIMEOUT : compilationTimeout;
    this.configuration =
        new Configuration(javaCompiler, classLoader, charset, intrusive, compilationTimeout);
    ResourcesLoader resourcesLoader = new ResourcesLoader();//todo if springboot
    CompiledClassSupplier compiledClassSupplier = new CompiledClassSupplier();
    this.runtimeClassLoader = intrusive ?
        new IntrusiveClassLoader(classLoader, compiledClassSupplier) :
        new StandaloneClassLoader(classLoader, compiledClassSupplier);
    this.runtimeJavaFileManager = new RuntimeJavaFileManager(
        javaCompiler.getStandardFileManager(null, null, charset),
        runtimeClassLoader, resourcesLoader, compilationTimeout);
    compiledClassSupplier.setSupplier(runtimeJavaFileManager::getCompiledClass);
  }

  public static Builder builder() {
    return new Builder();
  }
  public void addModule(Module module) {
    runtimeJavaFileManager.addModule(module);
  }

  @SneakyThrows
  public void addExtraArchive(File file) {
    runtimeClassLoader.addExtraArchive(file);
    runtimeJavaFileManager.addExtraArchive(file);
  }

  @SneakyThrows
  public void addExtraClass(File file) {
    byte[] classContent = Files.readAllBytes(file.toPath());
    Class<?> clazz = runtimeClassLoader.defineClass(null, classContent);
    runtimeJavaFileManager.addExtraClass(clazz.getName(), file);
  }

  public Class<?> compile(JavaSourceCode sourceCode) {
    return compile(Collections.singletonList(sourceCode), null).get(sourceCode.getName());
  }

  private Map<String, Class<?>> compile(List<JavaSourceCode> sourceCodes, JavaCompileSetting compileSetting) {
    List<JavaMemoryCode> javaMemoryCodes = getSourceCode(sourceCodes);
    List<String> options = getOptions(compileSetting);
    Writer writer = getWriter(compileSetting);
    CompileDiagnosticListener diagnosticListener = new CompileDiagnosticListener();
    Map<String, Class<?>> classMap = compile(javaMemoryCodes, writer,
        diagnosticListener, options);
    if (classMap == null) {
      throw new RuntimeCompilerException(diagnosticListener.getErrorMessage());
    }
    return classMap;
  }

  private List<JavaMemoryCode> getSourceCode(List<JavaSourceCode> sourceCodes) {
    Optional<String> emptyCode = sourceCodes.parallelStream().map(JavaSourceCode::getCode)
        .filter(Strings::isNullOrEmpty).findAny();
    if (emptyCode.isPresent()) {
      throw new IllegalArgumentException("source code is empty");
    }
    Optional<String> codeWithOutName = sourceCodes.parallelStream().peek(sourceCode -> {
      if (Strings.isNullOrEmpty(sourceCode.getName())) {
        sourceCode.setName(JavaCodeParser.getFullClassName(sourceCode.getCode()));
      }
    }).map(JavaSourceCode::getCode).filter(Strings::isNullOrEmpty).findAny();
    if (codeWithOutName.isPresent()) {
      throw new IllegalArgumentException(
          "class name is empty and can't find class name by source code, please set class name manually");
    }
    return sourceCodes.parallelStream()
        .map(sourceCode -> new JavaMemoryCode(sourceCode.getName(), sourceCode.getCode()))
        .toList();
  }

  private List<String> getOptions(JavaCompileSetting compileSetting) {
    return compileSetting == null || compileSetting.getOptions() == null ? DEFAULT_OPTIONS
        : compileSetting.getOptions();
  }

  private Writer getWriter(JavaCompileSetting compileSetting) {
    return compileSetting == null || compileSetting.getWriter() == null ? DEFAULT_WRITER
        : compileSetting.getWriter();
  }

  private Map<String, Class<?>> compile(List<JavaMemoryCode> compilationUnits,
      Writer writer, DiagnosticListener<? super JavaFileObject> diagnosticListener, List<String> options) {
    CompilationInterceptor interceptor = runtimeJavaFileManager.createInterceptor();
    if (configuration.javaCompiler
        .getTask(writer, interceptor, diagnosticListener, options, null, compilationUnits)
        .call()) {
      runtimeJavaFileManager.addCompileCode(compilationUnits);
      return runtimeClassLoader.defineCompiledClass(interceptor.getCompiledClassNames());
    }
    return null;
  }

  public Class<?> compile(JavaSourceCode sourceCode, JavaCompileSetting compileSetting) {
    return compile(Collections.singletonList(sourceCode), compileSetting).get(sourceCode.getName());

  }


  public Map<String, Class<?>> compile(List<JavaSourceCode> sourceCodes) {
    return compile(sourceCodes, null);
  }

  public ClassLoader getClassLoader() {
    return this.runtimeClassLoader;
  }
  public record Configuration(JavaCompiler javaCompiler, ClassLoader classLoader, Charset charset,
                              boolean intrusive, long compilationTimeout) {

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
