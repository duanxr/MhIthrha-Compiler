package com.duanxr.mhithrha.exports;


import com.duanxr.mhithrha.ClassPromise;
import com.duanxr.mhithrha.core.CompilerCore;
import com.duanxr.mhithrha.JavaClassParser;
import com.duanxr.mhithrha.RuntimeCompilerException;
import com.duanxr.mhithrha.RuntimeJavaFileManager;
import com.duanxr.mhithrha.component.CompileDiagnosticListener;
import com.duanxr.mhithrha.component.JavaMemoryCode;
import com.duanxr.mhithrha.loader.RuntimeClassLoader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

/**
 * @author 段然 2022/8/29
 */
@Slf4j
public class RuntimeCompiler {
  private static final List<String> DEFAULT_OPTIONS = Arrays.asList("-g", "-nowarn");
  private static final PrintWriter DEFAULT_WRITER = new PrintWriter(System.err);
  protected final JavaCompiler javaCompiler;
  protected final ClassLoader classLoader;
  protected final CompilerCore compilerCore;
  public RuntimeCompiler(JavaCompiler javaCompiler) {
    this.javaCompiler = javaCompiler;
    this.classLoader = getClass().getClassLoader();
    this.compilerCore = create(javaCompiler, classLoader);
  }
  private CompilerCore create(JavaCompiler javaCompiler, ClassLoader classLoader) {
    RuntimeClassLoader runtimeClassLoader = new RuntimeClassLoader(classLoader);
    //todo if springboot
    StandardJavaFileManager standardFileManager =
        javaCompiler.getStandardFileManager(null, null, null);
    RuntimeJavaFileManager runtimeJavaFileManager =
        new RuntimeJavaFileManager(standardFileManager, runtimeClassLoader);
    return new CompilerCore(runtimeClassLoader, javaCompiler, runtimeJavaFileManager);
  }

  public RuntimeCompiler(JavaCompiler javaCompiler, ClassLoader classLoader) {
    this.javaCompiler = javaCompiler;
    this.classLoader = classLoader;
    this.compilerCore = create(javaCompiler, classLoader);
  }

  public static RuntimeCompiler withEclipse() {
    System.setProperty("jdt.compiler.useSingleThread","true");
    return new RuntimeCompiler(new EclipseCompiler());
  }

  public static RuntimeCompiler withEclipse(ClassLoader classLoader) {
    System.setProperty("jdt.compiler.useSingleThread","true");
    return new RuntimeCompiler(new EclipseCompiler(), classLoader);
  }

  public static RuntimeCompiler withJdk() {
    return new RuntimeCompiler(ToolProvider.getSystemJavaCompiler());
  }

  public static RuntimeCompiler withJdk(ClassLoader classLoader) {
    return new RuntimeCompiler(ToolProvider.getSystemJavaCompiler(), classLoader);
  }

  public static RuntimeCompiler withJavac(ClassLoader classLoader) {
    return new RuntimeCompiler(getJavacCompiler(), classLoader);
  }

  @SneakyThrows
  private static JavaCompiler getJavacCompiler() {
    Class<?> javacTool = Class.forName("com.sun.tools.javac.api.JavacTool");
    Method create = javacTool.getMethod("create");
    return (JavaCompiler) create.invoke(null);
  }

  public static RuntimeCompiler withJavac() {
    return new RuntimeCompiler(getJavacCompiler());
  }

  public Class<?> compile(String className, String javaCode, PrintWriter writer,
      List<String> optionList) throws ClassNotFoundException {
    return compile(compilerCore, className, javaCode, writer, optionList);
  }

  private Class<?> compile(CompilerCore compilerCore, String className, String javaCode,
      PrintWriter writer, List<String> optionList) throws ClassNotFoundException {
    if (javaCode == null || javaCode.isEmpty()) {
      throw new IllegalArgumentException("javaCode is empty");
    }
    if (className == null || className.isEmpty()) {
      className = JavaClassParser.getFullClassName(javaCode);
    }
    if (writer == null) {
      writer = DEFAULT_WRITER;
    }
    if (optionList == null || optionList.isEmpty()) {
      optionList = DEFAULT_OPTIONS;
    }
    CompileDiagnosticListener diagnosticListener = new CompileDiagnosticListener();
    List<JavaFileObject> javaFileObjects = Collections.singletonList(
        new JavaMemoryCode(className, javaCode));
    boolean compile = compilerCore.compile(javaFileObjects, writer, diagnosticListener, optionList);
    if (!compile) {
      throw new RuntimeCompilerException(diagnosticListener.getError());
    }
    return compilerCore.load(className);
  }

  public Class<?> compile(String className, String javaCode, List<String> optionList)
      throws ClassNotFoundException {
    return compile(compilerCore, className, javaCode, null, optionList);
  }

  public Class<?> compile(String className, String javaCode, PrintWriter writer)
      throws ClassNotFoundException {
    return compile(compilerCore, className, javaCode, writer, null);
  }

  public Class<?> compile(String javaCode, PrintWriter writer) throws ClassNotFoundException {
    return compile(compilerCore, null, javaCode, writer, null);
  }

  public Class<?> compile(String className, String javaCode) throws ClassNotFoundException {
    return compile(compilerCore, className, javaCode, null, null);
  }

  public Class<?> compile(String javaCode, PrintWriter writer, List<String> optionList)
      throws ClassNotFoundException {
    return compile(compilerCore, null, javaCode, writer, optionList);
  }

  public Class<?> compile(String javaCode, List<String> optionList) throws ClassNotFoundException {
    return compile(compilerCore, null, javaCode, null, optionList);
  }

  public Class<?> compile(String javaCode) throws ClassNotFoundException {
    return compile(compilerCore, null, javaCode, null, null);
  }

  public CompileTask createTask() {
    return createTask(getClass().getClassLoader());
  }

  public CompileTask createTask(ClassLoader classLoader) {
    return new CompileTask(create(javaCompiler, classLoader));
  }

  public ClassLoader getClassLoader() {
    return this.compilerCore.getClassLoader();
  }

  public static class CompileTask {

    private final List<JavaFileObject> compilationUnits;
    private final CompilerCore compilerCore;

    public CompileTask(CompilerCore compilerCore) {
      this.compilerCore = compilerCore;
      this.compilationUnits = new ArrayList<>();
    }

    public ClassPromise compileJavaCode(String code) {
      return compileJavaCode(JavaClassParser.getFullClassName(code), code);
    }

    public ClassPromise compileJavaCode(String name, String code) {
      compilationUnits.add(new JavaMemoryCode(name, code));
      return new ClassPromise(name, compilerCore::load);
    }

    public void compile(PrintWriter writer, List<String> optionList) {
      if (writer == null) {
        writer = DEFAULT_WRITER;
      }
      if (optionList == null || optionList.isEmpty()) {
        optionList = DEFAULT_OPTIONS;
      }
      CompileDiagnosticListener diagnosticListener = new CompileDiagnosticListener();
      boolean compile = compilerCore.compile(compilationUnits, writer, diagnosticListener,
          optionList);
      if (!compile) {
        throw new RuntimeCompilerException(diagnosticListener.getError());
      }
    }
  }

}
