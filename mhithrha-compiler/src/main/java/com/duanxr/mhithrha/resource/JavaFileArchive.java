package com.duanxr.mhithrha.resource;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.internal.compiler.apt.util.ArchiveFileObject;

/**
 * @author 段然 2022/9/5
 */
public class JavaFileArchive extends ArchiveFileObject  implements RuntimeJavaFileObject {

  public JavaFileArchive(String name, File file) {
    super(file, name, StandardCharsets.UTF_8);
  }
  public JavaFileArchive(String name, File file, Charset charset) {
    super(file, name, charset);
  }
  public JavaFileArchive(String name, URI uri) {
    super(new File(uri), name, StandardCharsets.UTF_8);
  }
}
