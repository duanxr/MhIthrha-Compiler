package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.JavaClassNameUtil;
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
    this.className = JavaClassNameUtil.toJavaName(name);
    this.packageName = JavaClassNameUtil.toPackageName(name);
  }

  public JavaFileClass(String name, URI uri) {
    super(name, uri, Kind.CLASS, StandardCharsets.UTF_8);
    this.className = JavaClassNameUtil.toJavaName(name);
    this.packageName = JavaClassNameUtil.toPackageName(name);
  }

  public JavaFileClass(String name, URI uri, Kind kind, Charset charset) {
    super(name, uri, kind, charset);
    this.className = JavaClassNameUtil.toJavaName(name);
    this.packageName = JavaClassNameUtil.toPackageName(name);
  }
  public boolean inPackage(String targetPackageName) {
    return JavaClassNameUtil.inPackage(this.packageName, targetPackageName);
  }

  public boolean inPackages(String targetPackageName) {
    return JavaClassNameUtil.inPackages(this.packageName, targetPackageName);
  }
}
