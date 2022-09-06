package com.duanxr.mhithrha;

/**
 * @author 段然 2022/8/29
 */
public class RuntimeCompilerException extends RuntimeException {

  public RuntimeCompilerException(String message, Throwable cause) {
    super(message, cause);
  }

  public RuntimeCompilerException(String message) {
    super(message);
  }

  public RuntimeCompilerException(Throwable cause) {
    super(cause);
  }

  public RuntimeCompilerException() {
    super();
  }

}
