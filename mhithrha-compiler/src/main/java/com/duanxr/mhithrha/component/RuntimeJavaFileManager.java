/*
 * Copyright 2014 Higher Frequency Trading
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duanxr.mhithrha.component;

import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.tools.StandardLocation.CLASS_PATH;
import static javax.tools.StandardLocation.MODULE_PATH;
import static javax.tools.StandardLocation.SOURCE_PATH;

import com.duanxr.mhithrha.resource.JavaArchive;
import com.duanxr.mhithrha.resource.JavaFileArchive;
import com.duanxr.mhithrha.resource.JavaFileClass;
import com.duanxr.mhithrha.resource.JavaMemoryClass;
import com.duanxr.mhithrha.resource.JavaMemoryCode;
import com.duanxr.mhithrha.resource.JavaModuleLocation;
import com.duanxr.mhithrha.resource.RuntimeJavaFileObject;
import com.google.common.collect.Iterators;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuntimeJavaFileManager implements JavaFileManager {
  private static final Set<Location> LOCATIONS = Set.of(SOURCE_PATH, CLASS_PATH, CLASS_OUTPUT,
      MODULE_PATH);
  private final ClassLoader classLoader;
  private final long compilationTimeout;
  private final Map<String, JavaMemoryClass> compiledClasses = new ConcurrentHashMap<>();
  private final Map<String, JavaMemoryCode> compiledCodes = new ConcurrentHashMap<>();
  private final Set<JavaArchive> extraArchives = ConcurrentHashMap.newKeySet();
  private final Map<String, JavaFileClass> extraClasses = new ConcurrentHashMap<>();
  private final StandardJavaFileManager fileManager;
  private final Set<JavaModuleLocation> moduleLocations = ConcurrentHashMap.newKeySet();
  @SuppressWarnings("unchecked")
  private final List<Set<Location>> moduleLocationList = Collections.singletonList(
      (Set<Location>) (Set<? extends Location>) moduleLocations);
  private final ResourcesLoader resourcesLoader;

  public RuntimeJavaFileManager(StandardJavaFileManager fileManager,
      ClassLoader classLoader, ResourcesLoader resourcesLoader, long compilationTimeout) {
    this.fileManager = fileManager;
    this.classLoader = classLoader;
    this.resourcesLoader = resourcesLoader;
    this.compilationTimeout = compilationTimeout;
  }

  public ClassLoader getClassLoader(Location location) {
    return classLoader;
  }

  @SuppressWarnings("unchecked")
  public Iterable<JavaFileObject> list(Location location, String packageName,
      Set<Kind> kinds, boolean recurse) throws IOException {
    Iterable<JavaFileObject> list;
    try {
      synchronized (fileManager) {
        list = fileManager.list(location, packageName, kinds, recurse);
      }
    } catch (Exception e) {
      list = Collections.emptyList();
    }
    if (LOCATIONS.contains(location)) {
      String uri = JavaClassNameUtil.toURI(packageName);
      String javaPackageName = JavaClassNameUtil.toJavaName(packageName);
      List<RuntimeJavaFileObject> runtimeList = new ArrayList<>();
      List<RuntimeJavaFileObject> synchronizedRuntimeList = Collections.synchronizedList(
          runtimeList);
      if (location == MODULE_PATH) {
        moduleLocations.stream().map(JavaModuleLocation::getFile).forEach(
            file -> resourcesLoader.loadJavaFiles(file, uri, kinds, recurse,
                synchronizedRuntimeList));
      } else if (location == CLASS_OUTPUT) {
        if (kinds.contains(Kind.CLASS)) {
          compiledClasses.values().stream()
              .filter(javaMemoryClass -> javaMemoryClass.inPackage(javaPackageName))
              .forEach(synchronizedRuntimeList::add);
        }
      } else if (location == SOURCE_PATH) {
        if (kinds.contains(Kind.CLASS)) {
          compiledClasses.values().stream()
              .filter(javaMemoryClass ->
                  recurse ? javaMemoryClass.inPackages(javaPackageName)
                      : javaMemoryClass.inPackage(javaPackageName))
              .forEach(synchronizedRuntimeList::add);
        }
        if (kinds.contains(Kind.SOURCE)) {
          compiledCodes.values().stream()
              .filter(javaMemoryCode ->
                  recurse ? javaMemoryCode.inPackages(javaPackageName)
                      : javaMemoryCode.inPackage(javaPackageName))
              .forEach(synchronizedRuntimeList::add);
        }
      } else if (location == CLASS_PATH) {
        if (kinds.contains(Kind.CLASS)) {
          compiledClasses.values().stream()
              .filter(javaMemoryClass ->
                  recurse ? javaMemoryClass.inPackages(javaPackageName)
                      : javaMemoryClass.inPackage(javaPackageName))
              .forEach(synchronizedRuntimeList::add);
          extraArchives.stream().map(JavaArchive::getFile)
              .forEach(file -> resourcesLoader.loadJavaFiles(file, uri, kinds, recurse,
                  synchronizedRuntimeList));
          extraClasses.values().stream()
              .filter(javaFileClass ->
                  recurse ? javaFileClass.inPackages(javaPackageName)
                      : javaFileClass.inPackage(javaPackageName))
              .forEach(synchronizedRuntimeList::add);
        }
      }
      if (!synchronizedRuntimeList.isEmpty()) {
        Iterator<JavaFileObject> runtimeIterator = Iterators.concat(
            ((List<JavaFileObject>) (List<? extends JavaFileObject>) runtimeList).iterator(),
            list.iterator());
        return () -> runtimeIterator;
      }
    }
    return list;
  }


  public String inferBinaryName(Location location, JavaFileObject file) {
    if (file instanceof JavaMemoryCode javaMemoryCode) {
      return javaMemoryCode.getClassName();
    }
    if (file instanceof JavaMemoryClass javaMemoryClass) {
      return javaMemoryClass.getClassName();
    }
    if (file instanceof JavaFileArchive javaFileArchive) {
      return javaFileArchive.getClassName();
    }
    if (file instanceof JavaFileClass javaFileClass) {
      return javaFileClass.getClassName();
    }
    return fileManager.inferBinaryName(location, file);
  }

  @Override
  public boolean isSameFile(FileObject a, FileObject b) {
    return a == b || fileManager.isSameFile(a, b);
  }

  @Override
  public boolean handleOption(String current, Iterator<String> remaining) {
    return fileManager.handleOption(current, remaining);
  }


  @Override
  public boolean hasLocation(Location location) {
    return LOCATIONS.contains(location) || fileManager.hasLocation(location);
  }

  @Override
  public JavaFileObject getJavaFileForInput(Location location, String className,
      Kind kind) {
    if (LOCATIONS.contains(location)) {
      List<RuntimeJavaFileObject> list = Collections.synchronizedList(new ArrayList<>());
      if (location == MODULE_PATH) {
        RuntimeJavaFileObject javaFileObject = moduleLocations.stream()
            .map(JavaModuleLocation::getFile)
            .map(file -> resourcesLoader.loadJavaFile(file, className, kind))
            .filter(Objects::nonNull)
            .findAny().orElse(null);
        if (javaFileObject != null) {
          return javaFileObject;
        }
      } else {
        String javaClassName = JavaClassNameUtil.toJavaName(className);
        if (location == CLASS_OUTPUT && kind == Kind.CLASS) {
          JavaMemoryClass javaMemoryClass = compiledClasses.get(javaClassName);
          if (javaMemoryClass != null) {
            return javaMemoryClass;
          }
        } else if (location == SOURCE_PATH) {
          if (kind == Kind.SOURCE) {
            JavaMemoryCode javaMemoryCode = compiledCodes.get(javaClassName);
            if (javaMemoryCode != null) {
              return javaMemoryCode;
            }
          }
          if (kind == Kind.CLASS) {
            JavaMemoryClass javaMemoryClass = compiledClasses.get(javaClassName);
            if (javaMemoryClass != null) {
              return javaMemoryClass;
            }
          }
        } else if (location == CLASS_PATH) {
          if (kind == Kind.CLASS) {
            Optional<JavaMemoryClass> memoryClass = compiledClasses.values().stream()
                .filter(javaMemoryClass ->
                    javaMemoryClass.getClassName().equals(javaClassName))
                .findAny();
            if (memoryClass.isPresent()) {
              return memoryClass.get();
            }
            Optional<JavaFileClass> extraClass = extraClasses.values().stream()
                .filter(javaMemoryClass ->
                    javaMemoryClass.getClassName().equals(javaClassName))
                .findAny();
            if (extraClass.isPresent()) {
              return extraClass.get();
            }
            Optional<RuntimeJavaFileObject> extraArchiveClass = extraArchives.stream()
                .map(JavaArchive::getFile)
                .map(file -> resourcesLoader.loadJavaFile(file, javaClassName, kind))
                .filter(Objects::nonNull)
                .findAny();
            if (extraArchiveClass.isPresent()) {
              return extraArchiveClass.get();
            }
          }
        }
      }
    }
    try {
      return fileManager.getJavaFileForInput(location, className, kind);
    } catch (Exception ignored) {
      return null;
    }
  }

  @Override
  @SneakyThrows
  public JavaFileObject getJavaFileForOutput(Location location, final String className, Kind kind,
      FileObject sibling) {
    if (location == CLASS_OUTPUT && kind == Kind.CLASS) {
      String javaName = JavaClassNameUtil.toJavaName(className);
      synchronized (compiledClasses) {
        JavaMemoryClass javaMemoryClass = compiledClasses.get(javaName);
        if (javaMemoryClass == null) {
          javaMemoryClass = new JavaMemoryClass(className, compilationTimeout);
          compiledClasses.put(javaMemoryClass.getClassName(), javaMemoryClass);
        }
        return javaMemoryClass;
      }
    }
    return null;
  }

  @Override
  public FileObject getFileForInput(Location location, String packageName, String relativeName)
      throws IOException {
    return fileManager.getFileForInput(location, packageName, relativeName);
  }

  @Override
  public FileObject getFileForOutput(Location location, String packageName, String
      relativeName,
      FileObject sibling) throws IOException {
    return fileManager.getFileForOutput(location, packageName, relativeName, sibling);
  }

  @Override
  public void flush() {
  }

  @Override
  public void close() throws IOException {
    fileManager.close();
  }

  public String inferModuleName(final Location location) throws IOException {
    return location instanceof JavaModuleLocation javaModuleLocation
        ? javaModuleLocation.getModuleName() : fileManager.inferModuleName(location);
  }

  public Iterable<Set<Location>> listLocationsForModules(final Location location)
      throws IOException {
    return location == MODULE_PATH ? moduleLocationList
        : fileManager.listLocationsForModules(location);
  }

  @Override
  public boolean contains(Location location, FileObject fileObject) throws IOException {
    return fileObject instanceof RuntimeJavaFileObject || fileManager.contains(location,
        fileObject);
  }

  @Override
  public int isSupportedOption(String option) {
    return fileManager.isSupportedOption(option);
  }

  public void addModule(Module module) {
    if (module.isNamed()) {
      module.getLayer().configuration().modules().stream()
          .filter(Objects::nonNull).map(JavaModuleLocation::new)
          .filter(JavaModuleLocation::notJrt)
          .filter(JavaModuleLocation::isExist).forEach(moduleLocations::add);
    }
  }

  public void addCompileCode(List<JavaMemoryCode> javaMemoryCodes) {
    for (JavaMemoryCode javaMemoryCode : javaMemoryCodes) {
      compiledCodes.put(javaMemoryCode.getClassName(), javaMemoryCode);
    }
  }

  @SneakyThrows
  public void addExtraArchive(File file) {
    JavaArchive javaArchive = ResourcesLoader.loadJavaArchive(file);
    extraArchives.add(javaArchive);
  }

  @SneakyThrows
  public void addExtraClass(String name, File file) {
    JavaFileClass javaFileClass = new JavaFileClass(name, file);
    extraClasses.put(javaFileClass.getClassName(), javaFileClass);
  }

  public CompilationInterceptor createInterceptor() {
    return new CompilationInterceptor();
  }

  public JavaMemoryClass getCompiledClass(String className) {
    return compiledClasses.get(className);
  }

  public class CompilationInterceptor implements JavaFileManager {

    @Getter
    private final Set<String> compiledClassNames = new HashSet<>();

    @Override
    public ClassLoader getClassLoader(Location location) {
      return RuntimeJavaFileManager.this.getClassLoader(location);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds,
        boolean recurse) throws IOException {
      return RuntimeJavaFileManager.this.list(location, packageName, kinds, recurse);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
      return RuntimeJavaFileManager.this.inferBinaryName(location, file);
    }

    @Override
    public boolean isSameFile(FileObject a, FileObject b) {
      return RuntimeJavaFileManager.this.isSameFile(a, b);
    }

    @Override
    public boolean handleOption(String current, Iterator<String> remaining) {
      return RuntimeJavaFileManager.this.handleOption(current, remaining);
    }

    @Override
    public boolean hasLocation(Location location) {
      return RuntimeJavaFileManager.this.hasLocation(location);
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) {
      return RuntimeJavaFileManager.this.getJavaFileForInput(location, className, kind);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
        FileObject sibling) {
      if (location == CLASS_OUTPUT && kind == Kind.CLASS) {
        String javaName = JavaClassNameUtil.toJavaName(className);
        synchronized (compiledClassNames) {
          compiledClassNames.add(javaName);
        }
      }
      return RuntimeJavaFileManager.this.getJavaFileForOutput(location, className, kind, sibling);
    }

    @Override
    public FileObject getFileForInput(Location location, String packageName,
        String relativeName)
        throws IOException {
      return RuntimeJavaFileManager.this.getFileForInput(location, packageName, relativeName);
    }

    @Override
    public FileObject getFileForOutput(Location location, String packageName,
        String relativeName,
        FileObject sibling) throws IOException {
      return RuntimeJavaFileManager.this.getFileForOutput(location, packageName, relativeName,
          sibling);
    }

    @Override
    public void flush() {
      RuntimeJavaFileManager.this.flush();
    }

    @Override
    public void close() throws IOException {
      RuntimeJavaFileManager.this.close();
    }

    @Override
    public Location getLocationForModule(Location location, String moduleName)
        throws IOException {
      return RuntimeJavaFileManager.this.getLocationForModule(location, moduleName);
    }

    @Override
    public Location getLocationForModule(Location location, JavaFileObject fo)
        throws IOException {
      return RuntimeJavaFileManager.this.getLocationForModule(location, fo);
    }

    @Override
    public <S> ServiceLoader<S> getServiceLoader(Location location, Class<S> service)
        throws IOException {
      return RuntimeJavaFileManager.this.getServiceLoader(location, service);
    }

    @Override
    public String inferModuleName(Location location) throws IOException {
      return RuntimeJavaFileManager.this.inferModuleName(location);
    }

    @Override
    public Iterable<Set<Location>> listLocationsForModules(Location location)
        throws IOException {
      return RuntimeJavaFileManager.this.listLocationsForModules(location);
    }

    @Override
    public boolean contains(Location location, FileObject fo) throws IOException {
      return RuntimeJavaFileManager.this.contains(location, fo);
    }

    @Override
    public int isSupportedOption(String option) {
      return RuntimeJavaFileManager.this.isSupportedOption(option);
    }
  }
}