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

package com.duanxr.mhithrha;

import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.tools.StandardLocation.CLASS_PATH;
import static javax.tools.StandardLocation.SOURCE_PATH;

import com.duanxr.mhithrha.component.JavaFileClass;
import com.duanxr.mhithrha.component.JavaMemoryClass;
import com.duanxr.mhithrha.component.RuntimeJavaFileObject;
import com.duanxr.mhithrha.loader.RuntimeClassLoader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RuntimeJavaFileManager implements JavaFileManager {

  private final StandardJavaFileManager fileManager;
  private final Map<String, JavaMemoryClass> outputClasses = new LinkedHashMap<>();
  private final Map<String, JavaFileClass> inputClasses = new LinkedHashMap<>();
  private final RuntimeClassLoader classLoader;

  public RuntimeJavaFileManager(StandardJavaFileManager fileManager,
      RuntimeClassLoader classLoader) {
    this.fileManager = fileManager;
    this.classLoader = classLoader;
  }

  public synchronized Iterable<Set<Location>> listLocationsForModules(final Location location)
      throws IOException {
    return fileManager.listLocationsForModules(location);
  }

  public synchronized String inferModuleName(final Location location) throws IOException {
    return fileManager.inferModuleName(location);
  }

  public ClassLoader getClassLoader(Location location) {
    return fileManager.getClassLoader(location);
  }

  public synchronized Iterable<JavaFileObject> list(Location location, String packageName,
      Set<Kind> kinds, boolean recurse) throws IOException {
    if (location == SOURCE_PATH) {
      synchronized (outputClasses) {
        return new ArrayList<>(outputClasses.values());
      }
    }
    return fileManager.list(location, packageName, kinds, recurse);
  }

  public static <T> Iterable<T> iterable(Iterable<T> iterable) {
    return iterable;
  }

  public String inferBinaryName(Location location, JavaFileObject file) {
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
    return location == SOURCE_PATH || location == CLASS_OUTPUT
        || location == CLASS_PATH || fileManager.hasLocation(location);
  }

  @Override
  public boolean contains(Location location, FileObject fo) throws IOException {
    return fo instanceof RuntimeJavaFileObject || fileManager.contains(location, fo);
  }

  @Override
  public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind)
      throws IOException {
    if (location == CLASS_OUTPUT && kind == Kind.CLASS) {
      synchronized (outputClasses) {
        return outputClasses.get(className);
      }
    }
    if (location == SOURCE_PATH && kind == Kind.CLASS) {
      synchronized (outputClasses) {
        return outputClasses.get(className);
      }
    }
    return fileManager.getJavaFileForInput(location, className, kind);
  }

  @Override
  public JavaFileObject getJavaFileForOutput(Location location, final String className, Kind kind,
      FileObject sibling) {
    if (location == CLASS_OUTPUT && kind == Kind.CLASS) {
      synchronized (outputClasses) {
        JavaMemoryClass javaMemoryClass = outputClasses.get(className);
        if (javaMemoryClass == null) {
          javaMemoryClass = new JavaMemoryClass(className, 3000);
          outputClasses.put(className, javaMemoryClass);
        }
        System.out.println(className);
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

  @Override
  public int isSupportedOption(String option) {
    return fileManager.isSupportedOption(option);
  }

  public Map<String, JavaMemoryClass> getOutputClasses() {
    LinkedHashMap<String, JavaMemoryClass> map;
    synchronized (outputClasses) {
      map = new LinkedHashMap<>(outputClasses);
    }
    map.values().forEach(JavaMemoryClass::getClassBytes);
    return map;
  }

}