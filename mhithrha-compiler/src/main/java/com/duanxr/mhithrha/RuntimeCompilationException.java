package com.duanxr.mhithrha;

/**
 * @author 段然 2022/8/29
 */
public class RuntimeCompilationException extends RuntimeException {

  public RuntimeCompilationException(String message, Throwable cause) {
    super(message, cause);
  }

  public RuntimeCompilationException(String message) {
    super(message);
  }

  public RuntimeCompilationException(Throwable cause) {
    super(cause);
  }

  public RuntimeCompilationException() {
    super();
  }

}
