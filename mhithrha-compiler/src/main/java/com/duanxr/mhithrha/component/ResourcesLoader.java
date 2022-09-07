package com.duanxr.mhithrha.component;

import com.duanxr.mhithrha.resource.JavaArchive;
import com.duanxr.mhithrha.resource.JavaFileClass;
import com.duanxr.mhithrha.resource.RuntimeJavaFileObject;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject.Kind;
import lombok.extern.slf4j.Slf4j;

/**
 * @author 段然 2022/9/7
 */
@Slf4j
@SuppressWarnings("resource")
public class ResourcesLoader {

  private static final LoadingCache<File, JavaArchive> ARCHIVE_CACHE = Caffeine.newBuilder()
      .removalListener(ResourcesLoader::closeJavaArchive).build(ResourcesLoader::loadJavaArchive);
  private final Charset charset;

  public ResourcesLoader() {
    this.charset = StandardCharsets.UTF_8;
  }

  public ResourcesLoader(Charset charset) {
    this.charset = charset;
  }

  private static void closeJavaArchive(File file, JavaArchive javaArchive,
      RemovalCause cause) {
    if (javaArchive != null) {
      javaArchive.close();
    }
  }

  private static JavaArchive loadJavaArchive(File file) {
    if (file.exists()) {
      try {
        return new JavaArchive(file);
      } catch (Exception e) {
        log.error("load java archive error", e);
      }
    }
    return null;
  }

  private String getExtension(String name) {
    int index = name.lastIndexOf('.');
    if (index == -1) {
      return "";
    }
    return name.substring(index);
  }

  private Kind getKind(String extension) {
    if (Kind.CLASS.extension.equals(extension)) {
      return Kind.CLASS;
    } else if (Kind.SOURCE.extension.equals(extension)) {
      return Kind.SOURCE;
    } else if (Kind.HTML.extension.equals(extension)) {
      return Kind.HTML;
    }
    return Kind.OTHER;
  }

  private boolean isArchive(File file) {
    String extension = getExtension(file.getName());
    return extension.equalsIgnoreCase(".jar") ||
        extension.equalsIgnoreCase(".zip");
  }

  public RuntimeJavaFileObject loadJavaFile(File file, String className, Kind kind) {
    String normalizedFileName = NameConvertor.normalize(className);
    normalizedFileName += kind.extension;
    if (isArchive(file)) {
      JavaArchive javaArchive = ARCHIVE_CACHE.get(file);
      if (javaArchive != null && javaArchive.contains(normalizedFileName)) {
        return javaArchive.createJavaFileArchive(normalizedFileName, this.charset);
      }
    }
    if (file.isDirectory()) {
      File f = new File(file, normalizedFileName);
      if (f.exists()) {
        return new JavaFileClass(className, f.toURI(), kind, this.charset);
      }
    }
    return null;
  }

  public void loadJavaFiles(File file, String normalizedPackageName,
      Set<Kind> kinds, boolean recurse, List<RuntimeJavaFileObject> collector) {
    if (isArchive(file)) {
      JavaArchive archive = ARCHIVE_CACHE.get(file);
      if (archive != null) {
        String key = normalizedPackageName.endsWith("/") ?
            normalizedPackageName : normalizedPackageName + '/';
        if (recurse) {
          for (String packageName : archive.allPackages()) {
            if (packageName.startsWith(key)) {
              loadJavaFilesFromArchive(kinds, collector, archive, packageName);
            }
          }
        } else {
          loadJavaFilesFromArchive(kinds, collector, archive, key);
        }
      }
    } else {
      File currentFile = new File(file, normalizedPackageName);
      if (!currentFile.exists()) {
        return;
      }
      String path;
      try {
        path = currentFile.getCanonicalPath();
      } catch (IOException e) {
        return;
      }
      if (File.separatorChar == '/') {
        if (!path.endsWith(normalizedPackageName)) {
          return;
        }
      } else if (!path.endsWith(normalizedPackageName.replace('/', File.separatorChar))) {
        return;
      }
      File[] files = currentFile.listFiles();
      if (files != null) {
        for (File f : files) {
          if (f.isDirectory() && recurse) {
            loadJavaFiles(file, normalizedPackageName + '/' + f.getName(),
                kinds, true, collector);
          } else {
            final Kind kind = getKind(getExtension(f.getName()));
            if (kinds.contains(kind)) {
              collector.add(
                  new JavaFileClass(normalizedPackageName + f.getName(), f.toURI(), kind,
                      this.charset));
            }
          }
        }
      }
    }
  }

  private void loadJavaFilesFromArchive(Set<Kind> kinds, List<RuntimeJavaFileObject> collector,
      JavaArchive archive, String packageName) {
    List<String[]> types = archive.getTypes(packageName);
    if (types != null) {
      for (String[] entry : types) {
        final Kind kind = getKind(getExtension(entry[0]));
        if (kinds.contains(kind)) {
          collector.add(archive.createJavaFileArchive(packageName + entry[0], this.charset));
        }
      }
    }
  }
}