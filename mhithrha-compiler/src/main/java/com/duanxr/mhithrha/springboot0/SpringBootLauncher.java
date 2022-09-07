package com.duanxr.mhithrha.springboot0;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.loader.Launcher;
import org.springframework.boot.loader.archive.Archive;
import org.springframework.boot.loader.archive.Archive.Entry;
import org.springframework.boot.loader.archive.ExplodedArchive;
import org.springframework.boot.loader.archive.JarFileArchive;

public class SpringBootLauncher extends Launcher {

  private static final Logger logger = LoggerFactory.getLogger(SpringBootLauncher.class);

  Map<String, SpringBootArchiveEntry> entryCache = new HashMap<>();

  static final String BOOT_INF_CLASSES = "BOOT-INF/classes/";
  static final String BOOT_INF_LIB = "BOOT-INF/lib/";

  private final Archive archive;

  public SpringBootLauncher() {
    try {
      this.archive = createJrcArchive();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private Archive createJrcArchive() throws Exception {
    Class<?> launcherClass = Class.forName("org.springframework.boot.loader.Launcher");
    ProtectionDomain protectionDomain = launcherClass.getProtectionDomain();
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
    return (root.isDirectory() ? new ExplodedArchive(root) : new JarFileArchive(root));
  }

  @Override
  protected String getMainClass() throws Exception {
    Manifest manifest = this.archive.getManifest();
    String mainClass = null;
    if (manifest != null) {
      mainClass = manifest.getMainAttributes().getValue("Start-Class");
    }
    if (mainClass == null) {
      throw new IllegalStateException(
          "No 'Start-Class' manifest archiveEntry specified in " + this);
    }
    return mainClass;
  }

  @Override
  protected Iterator<Archive> getClassPathArchivesIterator() throws Exception {
    Iterator<Archive> archives = archive.getNestedArchives(null, this::isNestedArchive);
    postProcessClassPathArchives(archives);
    return archives;
  }


  private void handleJar(Archive av) throws IOException {
    Iterator<Archive> classArchives = av.getNestedArchives(null,
        entry -> entry.getName().endsWith(".jar"));
    while (classArchives.hasNext()) {
      Archive archive = classArchives.next();
      if (archive instanceof JarFileArchive) {
        logger.info("Found jar file archive: " + archive);
        handleJar(archive);
      } else {
        for (Entry entry : archive) {
          logger.info("Found class file archive: " + entry.getName());
          SpringBootArchiveEntry entryItem = new SpringBootArchiveEntry();
          entryItem.archiveEntry = entry;
          entryItem.archive = archive;
          entryCache.put(entry.getName(), entryItem);
        }
      }
    }
  }


  protected void postProcessClassPathArchives(Iterator<Archive> archives) throws Exception {
    while (archives.hasNext()) {
      Archive archive = archives.next();
      Iterator<Archive.Entry> ite = archive.iterator();
      while (ite.hasNext()) {
        Archive.Entry archiveEntry = ite.next();
        SpringBootArchiveEntry entryItem = new SpringBootArchiveEntry();
        entryItem.archiveEntry = archiveEntry;
        entryItem.archive = archive;
        entryCache.put(archiveEntry.getName(), entryItem);
      }
      postProcessClassPathArchives(archive.getNestedArchives(null,this::isNestedArchive));
    }
  }

  public List<SpringBootArchiveEntry> getEntries(String path) {
    List<SpringBootArchiveEntry> list = new ArrayList<>();
    for (Map.Entry<String, SpringBootArchiveEntry> stringEntryItemEntry : entryCache.entrySet()) {
      if (stringEntryItemEntry.getKey().startsWith(path)) {
        list.add(stringEntryItemEntry.getValue());
      }
    }
    return list;
  }

  protected boolean isNestedArchive(Archive.Entry entry) {
    if (entry.isDirectory()) {
      return entry.getName().equals(BOOT_INF_CLASSES);
    }
    return entry.getName().startsWith(BOOT_INF_LIB);
  }

  @Override
  protected void launch(String[] args, String mainClass, ClassLoader classLoader) throws Exception {
    Thread.currentThread().setContextClassLoader(classLoader);
  }

  public void launch() throws Exception {
    super.launch(new String[]{});
  }


}
