package com.duanxr.mhithrha;

import java.io.File;
import java.net.URI;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Iterator;
import lombok.SneakyThrows;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.ExplodedArchive;
import org.springframework.boot.loader.archive.JarFileArchive;

/**
 * @author 段然 2022/9/5
 */
public class SpringBootArchiveReader {
  private final Archive archive;

  @SneakyThrows
  public SpringBootArchiveReader() {
    ProtectionDomain protectionDomain = getClass().getProtectionDomain();
    CodeSource codeSource = protectionDomain.getCodeSource();
    URI location = (codeSource != null) ? codeSource.getLocation().toURI() : null;
    String path = (location != null) ? location.getSchemeSpecificPart() : null;
    if (path == null) {
      throw new IllegalStateException("Unable to determine code source archive");
    }
    File root = new File(path);
    if (!root.exists()) {
      throw new IllegalStateException("Unable to determine code source archive from " + root);
    }
    archive = (root.isDirectory() ? new ExplodedArchive(root) : new JarFileArchive(root));
  }

  @SneakyThrows
  public void read() {
    Iterator<Archive> archives = archive.getNestedArchives(null, null);
    while (archives.hasNext()) {
      Archive archive = archives.next();
      if (archive instanceof JarFileArchive jarFileArchive) {

      } else {
        readEntries(archive);
      }
    }
  }

  private void readEntries(Archive archive) {

  }

  protected boolean isNestedArchive(Archive.Entry entry) {
    if (entry.isDirectory()) {
      return entry.getName().equals(BOOT_INF_CLASSES);
    }
    return entry.getName().startsWith(BOOT_INF_LIB);
  }

  static final String BOOT_INF_CLASSES = "BOOT-INF/classes/";
  static final String BOOT_INF_LIB = "BOOT-INF/lib/";

}
