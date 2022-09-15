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
  private final StringWriter out = new StringWriter();
  private final PrintWriter writer = new PrintWriter(out);
  private Locale locale = Locale.ENGLISH;
  public CompileDiagnosticListener() {
  }
  public CompileDiagnosticListener(Locale locale) {
    this.locale = locale;
  }

  @Override
  public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
    if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
      writeErrorMessage(diagnostic);
    }
  }

  private void writeErrorMessage(Diagnostic<? extends JavaFileObject> diagnostic) {
    try {
      Field exceptionField = diagnostic.getClass().getDeclaredField("exception");
      exceptionField.setAccessible(true);
      Exception exception = (Exception) exceptionField.get(diagnostic);
      exception.printStackTrace(writer);
    } catch (Exception ignored) {
    }
    writer.println(diagnostic.getMessage(locale));
  }

  public String getErrorMessage() {
    return out.toString();
  }
}