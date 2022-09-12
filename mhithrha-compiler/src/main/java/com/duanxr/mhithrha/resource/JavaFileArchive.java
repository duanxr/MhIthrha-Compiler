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
  public JavaFileArchive(String name, File file, Charset charset) {
    super(file, name, charset);
    String javaName = JavaNameUtil.removeSuffix(name);
    this.className = JavaNameUtil.toJavaName(javaName);
    this.packageName = JavaNameUtil.toPackageName(javaName);
  }
  public boolean inPackage(String targetPackageName) {
    return JavaNameUtil.inPackage(this.packageName, targetPackageName);
  }

  public boolean inPackages(String targetPackageName) {
    return JavaNameUtil.inPackages(this.packageName, targetPackageName);
  }
}
