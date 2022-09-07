package com.duanxr.mhithrha.springboot0;

import com.duanxr.mhithrha.component.RuntimeJavaFileManager;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 段然 2022/8/27
 */
public class SpringBootJavaFileManager extends RuntimeJavaFileManager {

  public SpringBootJavaFileManager(StandardJavaFileManager fileManager) {
    super(fileManager,null, null);
    try {
      springBootLauncher = new SpringBootLauncher();
      springBootLauncher.launch();
    } catch (Exception e) {
      logger.error("", e);
    }
  }
  private static final Logger logger = LoggerFactory.getLogger(SpringBootJavaFileManager.class);
  SpringBootLauncher springBootLauncher;

  @Override
  public Iterable<JavaFileObject> list(Location location, String packageName, Set set, boolean recurse) throws IOException {
    String packagePath = packageName.replaceAll("\\.", "/");
    List<SpringBootArchiveEntry> entries = springBootLauncher.getEntries(packagePath);

    List<JavaFileObject> list = entries.stream().map(it -> new JarJavaFileObject(it, JavaFileObject.Kind.CLASS)).collect(
        Collectors.toList());

    Iterable<JavaFileObject> superList = super.list(location, packageName, set, recurse);
    if (superList == null) {
      return list;
    }

    for (JavaFileObject o : superList) {
      list.add(o);
    }

    return list;
  }

  @Override
  public String inferBinaryName(Location location, JavaFileObject file) {
    if (file instanceof JarJavaFileObject) {
      return file.getName();
    } else {
      return super.inferBinaryName(location, file);
    }
  }

  @Override
  public ClassLoader getClassLoader(Location location) {
    return ClassLoaderFactory.getClassLoader();
  }


}
