package com.duanxr.mhithrha.component;

import java.lang.reflect.Method;
import java.util.Objects;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

/**
 * @author 段然 2022/9/8
 */
@UtilityClass
public class JavaCompilerFactory {

  public static JavaCompiler getEclipseCompiler() {
    return new EclipseCompiler();
  }

  @SneakyThrows
  @SuppressWarnings("")
  public static JavaCompiler getJavacCompiler() {
    Class<?> javacTool = Class.forName("com.sun.tools.javac.api.JavacTool");
    Method create = javacTool.getMethod("create");
    return (JavaCompiler) create.invoke(null);
  }

  public static JavaCompiler getJdkCompiler() {
    return Objects.requireNonNull(ToolProvider.getSystemJavaCompiler(),
        "Can't get JDK compiler , please use JDK instead of JRE , or use Eclipse compiler");
  }

}
