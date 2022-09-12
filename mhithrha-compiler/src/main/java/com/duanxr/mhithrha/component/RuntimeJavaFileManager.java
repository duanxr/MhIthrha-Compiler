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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuntimeJavaFileManager implements JavaFileManager {
  private static final Set<Location> LOCATIONS = Set.of(SOURCE_PATH, CLASS_PATH, CLASS_OUTPUT,
      MODULE_PATH);
  private final ClassLoader classLoader;
  private final long compilationTimeout;
  private final Map<String, JavaMemoryClass> compiledClasses = new LinkedHashMap<>();
  private final Map<String, JavaMemoryCode> compiledCodes = new LinkedHashMap<>();
  private final Set<JavaArchive> extraArchives = new HashSet<>();
  private final Map<String, JavaFileClass> extraClasses = new LinkedHashMap<>();
  private final StandardJavaFileManager fileManager;
  private final Set<JavaModuleLocation> moduleLocations = new LinkedHashSet<>();
  private final Map<String, JavaMemoryClass> outputClasses = new LinkedHashMap<>();
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
  public synchronized Iterable<JavaFileObject> list(Location location, String packageName,
      Set<Kind> kinds, boolean recurse) throws IOException {
    Iterable<JavaFileObject> list;
    try {
      list = fileManager.list(location, packageName, kinds, recurse);
    } catch (Exception e) {
      list = Collections.emptyList();
    }
    if (LOCATIONS.contains(location)) {
      String uri = JavaNameUtil.toURI(packageName);
      String javaPackageName = JavaNameUtil.toJavaName(packageName);
      List<RuntimeJavaFileObject> runtimeList = new ArrayList<>();
      List<RuntimeJavaFileObject> synchronizedList = Collections.synchronizedList(runtimeList);
      if (location == MODULE_PATH) {
        moduleLocations.parallelStream().map(JavaModuleLocation::getFile)
            .forEach(file -> resourcesLoader.loadJavaFiles(
                file, uri, kinds, recurse, synchronizedList));
      } else if (location == CLASS_OUTPUT) {
        compiledClasses.values().parallelStream()
            .filter(javaMemoryClass ->
                javaMemoryClass.inPackage(javaPackageName))
            .forEach(synchronizedList::add);
      } else if (location == SOURCE_PATH) {
        if (kinds.contains(Kind.CLASS)) {
          compiledClasses.values().parallelStream()
              .filter(javaMemoryClass ->
                  recurse ? javaMemoryClass.inPackages(javaPackageName)
                      : javaMemoryClass.inPackage(javaPackageName))
              .forEach(synchronizedList::add);
        }
        if (kinds.contains(Kind.SOURCE)) {
          compiledCodes.values().parallelStream()
              .filter(javaMemoryCode ->
                  recurse ? javaMemoryCode.inPackages(javaPackageName)
                      : javaMemoryCode.inPackage(javaPackageName))
              .forEach(synchronizedList::add);
        }
      } else if (location == CLASS_PATH) {
        if (kinds.contains(Kind.CLASS)) {
          compiledClasses.values().parallelStream()
              .filter(javaMemoryClass ->
                  recurse ? javaMemoryClass.inPackages(javaPackageName)
                      : javaMemoryClass.inPackage(javaPackageName))
              .forEach(synchronizedList::add);
          extraArchives.parallelStream().map(JavaArchive::getFile)
              .forEach(file -> resourcesLoader.loadJavaFiles(file, uri, kinds, recurse,
                  synchronizedList));
          extraClasses.values().parallelStream()
              .filter(javaFileClass ->
                  recurse ? javaFileClass.inPackages(javaPackageName)
                      : javaFileClass.inPackage(javaPackageName))
              .forEach(synchronizedList::add);
        }
      }
      if (!synchronizedList.isEmpty()) {
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
  public synchronized boolean handleOption(String current, Iterator<String> remaining) {
    return fileManager.handleOption(current, remaining);
  }


  @Override
  public boolean hasLocation(Location location) {
    return LOCATIONS.contains(location) || fileManager.hasLocation(location);
  }

  @Override
  public synchronized JavaFileObject getJavaFileForInput(Location location, String className,
      Kind kind) {
    if (LOCATIONS.contains(location)) {
      List<RuntimeJavaFileObject> list = Collections.synchronizedList(new ArrayList<>());
      if (location == MODULE_PATH) {
        RuntimeJavaFileObject javaFileObject = moduleLocations.parallelStream()
            .map(JavaModuleLocation::getFile)
            .map(file -> resourcesLoader.loadJavaFile(file, className, kind))
            .filter(Objects::nonNull)
            .findAny().orElse(null);
        if (javaFileObject != null) {
          return javaFileObject;
        }
      } else {
        String javaClassName = JavaNameUtil.toJavaName(className);
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
            Optional<JavaMemoryClass> memoryClass = compiledClasses.values().parallelStream()
                .filter(javaMemoryClass ->
                    javaMemoryClass.getClassName().equals(javaClassName))
                .findAny();
            if (memoryClass.isPresent()) {
              return memoryClass.get();
            }
            Optional<JavaFileClass> extraClass = extraClasses.values().parallelStream()
                .filter(javaMemoryClass ->
                    javaMemoryClass.getClassName().equals(javaClassName))
                .findAny();
            if (extraClass.isPresent()) {
              return extraClass.get();
            }
            Optional<RuntimeJavaFileObject> extraArchiveClass = extraArchives.parallelStream()
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
  public JavaFileObject getJavaFileForOutput(Location location, final String className, Kind kind,
      FileObject sibling) {
    if (location == CLASS_OUTPUT && kind == Kind.CLASS) {
      synchronized (compiledClasses) {
        JavaMemoryClass javaMemoryClass = compiledClasses.get(JavaNameUtil.toJavaName(className));
        if (javaMemoryClass == null) {
          javaMemoryClass = new JavaMemoryClass(className, compilationTimeout);
          compiledClasses.put(javaMemoryClass.getClassName(), javaMemoryClass);
          synchronized (outputClasses) {
            outputClasses.put(javaMemoryClass.getClassName(), javaMemoryClass);
          }
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
  public FileObject getFileForOutput(Location location, String packageName, String relativeName,
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

  public synchronized String inferModuleName(final Location location) throws IOException {
    if (location instanceof JavaModuleLocation javaModuleLocation) {
      return javaModuleLocation.getModuleName();
    }
    return fileManager.inferModuleName(location);
  }

  public synchronized Iterable<Set<Location>> listLocationsForModules(final Location location)
      throws IOException {
    if (location == MODULE_PATH) {
      return Collections.singletonList(new HashSet<>(moduleLocations));
    }
    return fileManager.listLocationsForModules(location);
  }

  @Override
  public boolean contains(Location location, FileObject fo) throws IOException {
    if (fo instanceof RuntimeJavaFileObject) {
      return true;
    }
    return fileManager.contains(location, fo);
  }


  @Override
  public int isSupportedOption(String option) {
    return fileManager.isSupportedOption(option);
  }

  public Map<String, JavaMemoryClass> getCompiledClasses() {
    LinkedHashMap<String, JavaMemoryClass> map = new LinkedHashMap<>();
    synchronized (outputClasses) {
      for (Entry<String, JavaMemoryClass> entry : outputClasses.entrySet()) {
        map.put(entry.getValue().getClassName(), entry.getValue());
      }
      outputClasses.clear();
    }
    return map;
  }


  public void addModule(Module module) {
    if (module.isNamed()) {
      List<JavaModuleLocation> locations = module.getLayer().configuration().modules().stream()
          .map(JavaModuleLocation::new).filter(JavaModuleLocation::notJrt)
          .filter(JavaModuleLocation::isExist)
          .toList();
      synchronized (moduleLocations) {
        moduleLocations.addAll(locations);
      }
    }
  }

  public void addCompileCode(List<JavaMemoryCode> javaMemoryCodes) {
    synchronized (compiledCodes) {
      for (JavaMemoryCode javaMemoryCode : javaMemoryCodes) {
        compiledCodes.put(javaMemoryCode.getClassName(), javaMemoryCode);
      }
    }
  }

  @SneakyThrows
  public void addExtraArchive(File file) {
    if (!file.exists()) {
      throw new IOException("file not found: " + file);
    }
    JavaArchive javaArchive = ResourcesLoader.loadJavaArchive(file);
    synchronized (extraArchives) {
      extraArchives.add(javaArchive);
    }
  }

  @SneakyThrows
  public void addExtraClass(String name, File file) {
    if (!file.exists()) {
      throw new IOException("file not found: " + file);
    }
    JavaFileClass javaFileClass = new JavaFileClass(name, file);
    synchronized (extraClasses) {
      extraClasses.put(javaFileClass.getClassName(), javaFileClass);
    }
  }


}