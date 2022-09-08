package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.JavaNameUtil;
import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import org.eclipse.jdt.internal.compiler.apt.util.ArchiveFileObject;

/**
 * @author 段然 2022/9/5
 */
public class JavaFileArchive extends ArchiveFileObject  implements RuntimeJavaFileObject {

  @Getter
  private final String className;
  @Getter
  private final String packageName;
  public JavaFileArchive(String name, File file) {
    super(file, name, StandardCharsets.UTF_8);
    this.className = JavaNameUtil.toJavaName(name);
    this.packageName = JavaNameUtil.toPackageName(name);
  }
  public JavaFileArchive(String name, File file, Charset charset) {
    super(file, name, charset);
    this.className = JavaNameUtil.toJavaName(name);
    this.packageName = JavaNameUtil.toPackageName(name);
  }
  public JavaFileArchive(String name, URI uri) {
    super(new File(uri), name, StandardCharsets.UTF_8);
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
