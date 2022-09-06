package com.duanxr.mhithrha.component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
 * @author 段然 2022/9/5
 */
public class CompileDiagnosticListener implements DiagnosticListener<JavaFileObject> {
  private final StringWriter out;
  private final PrintWriter writer;

  public CompileDiagnosticListener() {
    this.out = new StringWriter();
    this.writer = new PrintWriter(out);
  }

  @Override
  public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
    if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
      printError(diagnostic);
    }
  }

  private void printError(Diagnostic<? extends JavaFileObject> diagnostic) {
    try {
      Field exceptionField = diagnostic.getClass().getDeclaredField("exception");
      exceptionField.setAccessible(true);
      Exception exception = (Exception) exceptionField.get(diagnostic);
      exception.printStackTrace(writer);
    } catch (Exception ignored) {
    }
    writer.println(diagnostic.getMessage(Locale.ENGLISH));
  }

  public String getError() {
    return out.toString();
  }
}