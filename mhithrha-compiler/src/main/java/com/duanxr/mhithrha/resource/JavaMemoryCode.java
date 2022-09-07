package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.NameConvertor;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;

/**
 * @author 段然 2022/9/5
 */
public class JavaMemoryCode extends SimpleJavaFileObject implements RuntimeJavaFileObject {
  private final String code;

  private final String className;
  public JavaMemoryCode(String name, String code) {
    super(createURI(name), Kind.SOURCE);
    this.code = code;
    this.className = name;
  }
  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return code;
  }

  private static URI createURI(String name) {
    return URI.create("string:///" + NameConvertor.normalize(name) + Kind.SOURCE.extension);
  }


  public boolean inPackage(String packageName) {
    return className.startsWith(packageName);
  }
}
