package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.JavaClassNameUtil;
import java.io.File;
import java.nio.charset.Charset;
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
    String javaName = JavaClassNameUtil.removeSuffix(name);
    this.className = JavaClassNameUtil.toJavaName(javaName);
    this.packageName = JavaClassNameUtil.toPackageName(javaName);
  }
  public boolean inPackage(String targetPackageName) {
    return JavaClassNameUtil.inPackage(this.packageName, targetPackageName);
  }

  public boolean inPackages(String targetPackageName) {
    return JavaClassNameUtil.inPackages(this.packageName, targetPackageName);
  }
}
