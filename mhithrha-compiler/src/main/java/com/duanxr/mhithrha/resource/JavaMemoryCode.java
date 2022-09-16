package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.JavaClassNameUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.tools.SimpleJavaFileObject;
import lombok.Getter;

/**
 * @author 段然 2022/9/5
 */
public class JavaMemoryCode extends SimpleJavaFileObject implements RuntimeJavaFileObject {

  @Getter
  private final String className;
  @Getter
  private final String code;
  @Getter
  private final String packageName;

  @Getter
  private final Charset charset;

  public JavaMemoryCode(String name, String code,Charset charset) {
    super(createURI(JavaClassNameUtil.toURI(name)), Kind.SOURCE);
    this.code = code;
    this.charset = charset;
    this.className = JavaClassNameUtil.toJavaName(name);
    this.packageName = JavaClassNameUtil.toPackageName(name);
  }

  private static URI createURI(String path) {
    return URI.create("string:///" + path + Kind.SOURCE.extension);
  }

  @Override
  public InputStream openInputStream() {
    return new ByteArrayInputStream(
        code.getBytes(charset != null ? charset : StandardCharsets.UTF_8));
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    return code;
  }

  public boolean inPackage(String targetPackageName) {
    return JavaClassNameUtil.inPackage(this.packageName, targetPackageName);
  }

  public boolean inPackages(String targetPackageName) {
    return JavaClassNameUtil.inPackages(this.packageName, targetPackageName);
  }
}
