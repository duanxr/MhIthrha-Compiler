package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.JavaNameUtil;
import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import org.eclipse.jdt.internal.compiler.apt.util.EclipseFileObject;

/**
 * @author 段然 2022/9/5
 */
public class JavaFileClass extends EclipseFileObject implements RuntimeJavaFileObject {

  @Getter
  private final String className;
  @Getter
  private final String packageName;

  public JavaFileClass(String name, File file) {
    super(name, file.toURI(), Kind.CLASS, StandardCharsets.UTF_8);
    this.className = JavaNameUtil.toJavaName(name);
    this.packageName = JavaNameUtil.toPackageName(name);
  }

  public JavaFileClass(String name, URI uri) {
    super(name, uri, Kind.CLASS, StandardCharsets.UTF_8);
    this.className = JavaNameUtil.toJavaName(name);
    this.packageName = JavaNameUtil.toPackageName(name);
  }

  public JavaFileClass(String name, URI uri, Kind kind, Charset charset) {
    super(name, uri, kind, charset);
    this.className = JavaNameUtil.toJavaName(name);
    this.packageName = JavaNameUtil.toPackageName(name);
  }
  public boolean inPackage(String targetPackageName) {
    return JavaNameUtil.inPackage(this.packageName, targetPackageName);
  }

  public boolean inPackages(String targetPackageName) {
    return JavaNameUtil.inPackages(this.packageName, targetPackageName);
  }
}
