package com.duanxr.mhithrha.resource;

import java.io.File;
import java.nio.charset.Charset;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.eclipse.jdt.internal.compiler.tool.Archive;

/**
 * @author 段然 2022/9/7
 */
@EqualsAndHashCode(callSuper = false)
public class JavaArchive extends Archive {

  @Getter
  private final File file;

  public JavaArchive(File file) throws Exception {
    super(file);
    this.file = file;
  }

  protected JavaArchive() {
    this.file = null;
  }

  public RuntimeJavaFileObject createJavaArchiveFile(String fileName, Charset charset) {
    return new JavaArchiveFile(fileName, file, charset);
  }

}
