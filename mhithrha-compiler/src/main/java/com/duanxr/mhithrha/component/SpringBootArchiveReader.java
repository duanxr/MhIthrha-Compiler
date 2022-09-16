package com.duanxr.mhithrha.component;

import com.duanxr.mhithrha.resource.JavaSpringBootArchive;
import java.io.File;
import java.net.URI;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
  public SpringBootArchiveReader(ClassLoader classLoader) {
    ProtectionDomain protectionDomain = classLoader.getClass().getProtectionDomain();
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
  public List<JavaSpringBootArchive> loadArchives() {
    Iterator<Archive> archives = archive.getNestedArchives(null,
        entry ->
            entry.isDirectory() ?
                entry.getName().equals("BOOT-INF/classes/")
                : entry.getName().startsWith("BOOT-INF/lib/")
                    && entry.getName().endsWith(".jar"));
    List<JavaSpringBootArchive> archiveFiles = new ArrayList<>();
    while (archives.hasNext()) {
      Archive archive = archives.next();
      archiveFiles.add(new JavaSpringBootArchive(archive));
    }
    return archiveFiles;
  }

}
