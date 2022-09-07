package com.duanxr.mhithrha;


import com.duanxr.mhithrha.component.CompileDiagnosticListener;
import com.duanxr.mhithrha.component.JavaClassParser;
import com.duanxr.mhithrha.component.JavaMemoryCode;
import com.duanxr.mhithrha.component.RuntimeJavaFileManager;
import com.duanxr.mhithrha.core.CompilerCore;
import com.duanxr.mhithrha.loader.RuntimeClassLoader;
import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
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

  private RuntimeCompiler(JavaCompiler javaCompiler) {
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

  public void addModule(Module module) {
    compilerCore.getFileManager().addModule(module);
  }
  public void addExtraJar(File file) {
    compilerCore.getFileManager().addExtraJar(file);
  }
  public void addExtraClass(File file) {
    compilerCore.getFileManager().addExtraClass(file);
  }

  public RuntimeCompiler(JavaCompiler javaCompiler, ClassLoader classLoader) {
    this.javaCompiler = javaCompiler;
    this.classLoader = classLoader;
    this.compilerCore = create(javaCompiler, classLoader);
  }

  public static RuntimeCompiler withEclipseCompiler() {
    return new RuntimeCompiler(new EclipseCompiler());
  }

  public static RuntimeCompiler withEclipseCompiler(ClassLoader classLoader) {
    return new RuntimeCompiler(new EclipseCompiler(), classLoader);
  }

  public static RuntimeCompiler withJdkCompiler() {
    return new RuntimeCompiler(ToolProvider.getSystemJavaCompiler());
  }

  public static RuntimeCompiler withJdkCompiler(ClassLoader classLoader) {
    return new RuntimeCompiler(ToolProvider.getSystemJavaCompiler(), classLoader);
  }

  public static RuntimeCompiler withJavacCompiler(ClassLoader classLoader) {
    return new RuntimeCompiler(getJavacCompiler(), classLoader);
  }

  @SneakyThrows
  private static JavaCompiler getJavacCompiler() {
    Class<?> javacTool = Class.forName("com.sun.tools.javac.api.JavacTool");
    Method create = javacTool.getMethod("create");
    return (JavaCompiler) create.invoke(null);
  }

  public static RuntimeCompiler withJavacCompiler() {
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

  public ClassLoader getClassLoader() {
    return this.compilerCore.getClassLoader();
  }

}