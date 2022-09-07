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

import com.duanxr.mhithrha.loader.IntrusiveClassLoader;
import com.duanxr.mhithrha.resource.JavaArchive;
import com.duanxr.mhithrha.resource.JavaFileClass;
import com.duanxr.mhithrha.resource.JavaMemoryClass;
import com.duanxr.mhithrha.resource.JavaMemoryCode;
import com.duanxr.mhithrha.resource.JavaModuleLocation;
import com.duanxr.mhithrha.resource.RuntimeJavaFileObject;
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
      MODULE_PATH);//todo
  private final ClassLoader classLoader;
  private final Map<String, JavaMemoryClass> compiledClasses = new LinkedHashMap<>();
  private final Set<JavaArchive> extraArchives = new HashSet<>();
  private final Map<String, JavaFileClass> extraClasses = new LinkedHashMap<>();
  private final StandardJavaFileManager fileManager;
  private final Map<String, JavaMemoryCode> compiledCodes = new LinkedHashMap<>();
  private final Set<JavaModuleLocation> moduleLocations = new LinkedHashSet<>();
  private final Map<String, JavaMemoryClass> outputClasses = new LinkedHashMap<>();
  private final ResourcesLoader resourcesLoader;

  public RuntimeJavaFileManager(StandardJavaFileManager fileManager,
      ClassLoader classLoader, ResourcesLoader resourcesLoader) {
    this.fileManager = fileManager;
    this.classLoader = classLoader;
    this.resourcesLoader = resourcesLoader;
  }

  public ClassLoader getClassLoader(Location location) {
    return classLoader instanceof IntrusiveClassLoader ? classLoader.getParent() : classLoader;
  }


  @SuppressWarnings("unchecked")
  public synchronized Iterable<JavaFileObject> list(Location location, String packageName,
      Set<Kind> kinds, boolean recurse) throws IOException {
    if (!LOCATIONS.contains(location)) {
      return fileManager.list(location, packageName, kinds, recurse);
    }
    List<RuntimeJavaFileObject> list = Collections.synchronizedList(new ArrayList<>());
    if (location == MODULE_PATH) {
      String normalize = NameConvertor.normalize(packageName);
      moduleLocations.parallelStream().map(JavaModuleLocation::getFile)
          .forEach(file -> resourcesLoader.loadJavaFiles(
              file, normalize, kinds, recurse, list));
    } else if (location == CLASS_OUTPUT) {
      compiledClasses.values().parallelStream()
          .filter(javaMemoryClass -> javaMemoryClass.inPackage(packageName))
          .forEach(list::add);
    } else if (location == SOURCE_PATH) {
      if (kinds.contains(Kind.CLASS)) {
        compiledCodes.values().parallelStream()
            .filter(javaMemoryCode -> javaMemoryCode.inPackage(packageName))
            .forEach(list::add);
      }
      if (kinds.contains(Kind.SOURCE)) {
        String denormalize = NameConvertor.denormalize(packageName);
        compiledCodes.values().parallelStream()
            .filter(javaMemoryCode -> javaMemoryCode.inPackage(denormalize)).forEach(list::add);
      }
    } else if (location == CLASS_PATH) {
      if (kinds.contains(Kind.CLASS)) {
        String normalize = NameConvertor.normalize(packageName);
        compiledClasses.values().parallelStream()
            .filter(javaMemoryClass -> javaMemoryClass.inPackage(packageName))
            .forEach(list::add);
        extraArchives.parallelStream().map(JavaArchive::getFile)
            .forEach(file -> resourcesLoader.loadJavaFiles(
                file, normalize, kinds, recurse, list));
        extraClasses.values().parallelStream()
            .filter(javaMemoryClass -> javaMemoryClass.inPackage(packageName))
            .forEach(list::add);
      }
    }
    return (List<JavaFileObject>) (List<? extends JavaFileObject>) list;
  }


  public String inferBinaryName(Location location, JavaFileObject file) {
    if (file instanceof RuntimeJavaFileObject runtimeJavaFileObject) {
      return runtimeJavaFileObject.getName();
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
  public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind)
      throws IOException {
    if (LOCATIONS.contains(location)) {
      List<RuntimeJavaFileObject> list = Collections.synchronizedList(new ArrayList<>());
      if (location == MODULE_PATH) {
        return moduleLocations.parallelStream()
            .map(JavaModuleLocation::getFile)
            .map(file -> resourcesLoader.loadJavaFile(file, className, kind))
            .filter(Objects::nonNull)
            .findAny().orElse(null);
      } else if (location == CLASS_OUTPUT && kind == Kind.CLASS) {
        return compiledClasses.get(className);
      } else if (location == SOURCE_PATH) {
        if (kind == Kind.SOURCE) {
          return compiledCodes.get(NameConvertor.denormalize(className));
        }
        if (kind == Kind.CLASS) {
          return compiledClasses.get(className);
        }
      } else if (location == CLASS_PATH) {
        if (kind == Kind.CLASS) {
          String normalize = NameConvertor.normalize(className);
          Optional<JavaMemoryClass> memoryClass = compiledClasses.values().parallelStream()
              .filter(javaMemoryClass -> javaMemoryClass.inPackage(className))
              .findAny();
          if (memoryClass.isPresent()) {
            return memoryClass.get();
          }
          Optional<JavaFileClass> extraClass = extraClasses.values().parallelStream()
              .filter(javaMemoryClass -> javaMemoryClass.inPackage(className))
              .findAny();
          if (extraClass.isPresent()) {
            return extraClass.get();
          }
          Optional<RuntimeJavaFileObject> extraArchiveClass = extraArchives.parallelStream()
              .map(JavaArchive::getFile)
              .map(file -> resourcesLoader.loadJavaFile(file, normalize, kind)).findAny();
          if (extraArchiveClass.isPresent()) {
            return extraArchiveClass.get();
          }
        }

      }
    }
    return fileManager.getJavaFileForInput(location,className,kind);
}

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, final String className, Kind kind,
      FileObject sibling) {
    if (location == CLASS_OUTPUT && kind == Kind.CLASS) {
      synchronized (compiledClasses) {
        JavaMemoryClass javaMemoryClass = compiledClasses.get(className);
        if (javaMemoryClass == null) {
          javaMemoryClass = new JavaMemoryClass(className, 3000);
          compiledClasses.put(className, javaMemoryClass);
          synchronized (outputClasses) {
            outputClasses.put(className, javaMemoryClass);
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
        map.put(NameConvertor.denormalize(entry.getKey()), entry.getValue());
      }
      outputClasses.clear();
    }
    return map;
  }


  public void addModule(Module module) {
    if (module.isNamed()) {
      List<JavaModuleLocation> locations = module.getLayer().configuration().modules().stream()
          .map(JavaModuleLocation::new).filter(JavaModuleLocation::notJrt)
          .filter(JavaModuleLocation::isFile)
          .toList();
      synchronized (moduleLocations) {
        moduleLocations.addAll(locations);
      }
    }
  }

  public void addCompileCode(List<JavaMemoryCode> javaMemoryCodes) {
    synchronized (compiledCodes) {
      for (JavaMemoryCode javaMemoryCode : javaMemoryCodes) {
        compiledCodes.put(javaMemoryCode.getName(), javaMemoryCode);
      }
    }
  }

  @SneakyThrows
  public void addExtraJar(File file) {//todo use it!
    JavaArchive javaArchive = new JavaArchive(file);
    synchronized (extraArchives) {
      extraArchives.add(javaArchive);
    }
  }

  public void addExtraClass(File file) {//todo use it!
    JavaFileClass javaFileClass = new JavaFileClass(file.getAbsolutePath(), file);
    synchronized (extraClasses) {
      extraClasses.put(javaFileClass.getName(), javaFileClass);
    }
  }


}