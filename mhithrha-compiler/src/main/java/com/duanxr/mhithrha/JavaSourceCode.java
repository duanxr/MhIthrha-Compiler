package com.duanxr.mhithrha;

import lombok.Data;

/**
 * @author 段然 2022/9/12
 */
@Data
public class JavaSourceCode {

  private String code;
  private String name;
  private JavaSourceCode() {
  }

  public static JavaSourceCode of(String code) {
    JavaSourceCode javaSourceCode = new JavaSourceCode();
    javaSourceCode.setCode(code);
    return javaSourceCode;
  }

  public static JavaSourceCode of(String name, String code) {
    JavaSourceCode javaSourceCode = new JavaSourceCode();
    javaSourceCode.setName(name);
    javaSourceCode.setCode(code);
    return javaSourceCode;
  }
}
