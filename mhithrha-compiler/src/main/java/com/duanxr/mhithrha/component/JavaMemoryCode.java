package com.duanxr.mhithrha.component;

import java.net.URI;
import javax.tools.SimpleJavaFileObject;

/**
 * @author 段然 2022/9/5
 */
public class JavaMemoryCode extends SimpleJavaFileObject implements RuntimeJavaFileObject{

  private final String code;

  public JavaMemoryCode(String name, String code) {
    super(createURI(name), Kind.SOURCE);
    this.code = code;
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return code;
  }

  private static URI createURI(String name) {
    return URI.create("mhithrha:///" + name.replace('.', '/') + Kind.SOURCE.extension);
  }


}
