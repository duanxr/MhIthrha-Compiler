package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.JavaClassNameUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.tools.SimpleJavaFileObject;
import lombok.Getter;

/**
 * @author 段然 2022/9/5
 */
public class JavaMemoryCode extends SimpleJavaFileObject implements RuntimeJavaFileObject {
  @Getter
  private final String code;
  @Getter
  private final String className;
  @Getter
  private final String packageName;

  public JavaMemoryCode(String name, String code) {
    super(createURI(JavaClassNameUtil.toURI(name)), Kind.SOURCE);
    this.code = code;
    this.className = JavaClassNameUtil.toJavaName(name);
    this.packageName = JavaClassNameUtil.toPackageName(name);
  }
  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return code;
  }
  @Override
  public InputStream openInputStream() {
    return new ByteArrayInputStream(code.getBytes(StandardCharsets.UTF_8));
  }
  private static URI createURI(String path) {
    return URI.create("string:///" + path + Kind.SOURCE.extension);
  }
  public boolean inPackage(String targetPackageName) {
    return JavaClassNameUtil.inPackage(this.packageName, targetPackageName);
  }
  public boolean inPackages(String targetPackageName) {
    return JavaClassNameUtil.inPackages(this.packageName, targetPackageName);
  }
}
