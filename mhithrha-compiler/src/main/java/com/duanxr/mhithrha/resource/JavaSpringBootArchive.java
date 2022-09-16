package com.duanxr.mhithrha.resource;


import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * @author 段然 2022/9/5
 */
public class JavaSpringBootArchive extends JavaArchive {

  private final JarFile jarFile;
  private final URL url;
  private Hashtable<String, ArrayList<String[]>> packagesCache;

  public JavaSpringBootArchive(org.springframework.boot.loader.archive.Archive archive)
      throws IOException {
    url = archive.getUrl();
    URLConnection connection = url.openConnection();
    jarFile = ((JarURLConnection) connection).getJarFile();
    initialize();
  }

  private void initialize() {
    this.packagesCache = new Hashtable<>();
    for (Enumeration<? extends ZipEntry> e = this.jarFile.entries(); e.hasMoreElements(); ) {
      String fileName = e.nextElement().getName();
      int last = fileName.lastIndexOf('/');
      String packageName = fileName.substring(0, last + 1);
      String typeName = fileName.substring(last + 1);
      if (typeName.length() == 0) {
        continue;
      }
      cacheTypes(packageName, typeName);
    }
  }

  @Override
  protected void cacheTypes(String packageName, String typeName) {
    ArrayList<String[]> types = this.packagesCache.get(packageName);
    if (typeName == null) {
      return;
    }
    if (types == null) {

      types = new ArrayList<>();
      types.add(new String[]{typeName, null});
      this.packagesCache.put(packageName, types);
    } else {
      types.add(new String[]{typeName, null});
    }
  }

  @Override
  public boolean contains(String entryName) {
    return this.jarFile.getEntry(entryName) != null;
  }

  @Override
  public Set<String> allPackages() {
    if (this.packagesCache == null) {
      this.initialize();
    }
    return this.packagesCache.keySet();
  }

  @Override
  public List<String[]> getTypes(String packageName) {
    return this.packagesCache.get(packageName);
  }

  @Override
  public void flush() {
    this.packagesCache = null;
  }

  @Override
  public void close() {
    this.packagesCache = null;
    try {
      if (this.jarFile != null) {
        this.jarFile.close();
      }
    } catch (IOException ignored) {
    }
  }

  @Override
  public RuntimeJavaFileObject createJavaArchiveFile(String fileName, Charset charset) {
    return new JavaSpringBootArchiveFile(url, jarFile, fileName, charset);
  }

  @Override
  public String toString() {
    return "Archive: " + (jarFile.toString());
  }
}
