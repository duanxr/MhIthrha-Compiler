package com.duanxr.mhithrha.component;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.internal.compiler.apt.util.ArchiveFileObject;

/**
 * @author 段然 2022/9/5
 */
public class JavaJarFile extends ArchiveFileObject  implements RuntimeJavaFileObject{

  public JavaJarFile(String name, File file) {
    super(file, name, StandardCharsets.UTF_8);
  }

  public JavaJarFile(String name, URI uri) {
    super(new File(uri), name, StandardCharsets.UTF_8);
  }
}
