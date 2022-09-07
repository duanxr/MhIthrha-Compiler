package com.duanxr.mhithrha.resource;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.eclipse.jdt.internal.compiler.apt.util.EclipseFileObject;

/**
 * @author 段然 2022/9/5
 */
public class JavaFileClass extends EclipseFileObject implements RuntimeJavaFileObject {

  public JavaFileClass(String name, File file) {
    super(name, file.toURI(), Kind.CLASS, StandardCharsets.UTF_8);
  }

  public JavaFileClass(String name, URI uri) {
    super(name, uri, Kind.CLASS, StandardCharsets.UTF_8);
  }
  public JavaFileClass(String name, URI uri, Kind kind, Charset charset) {
    super(name, uri, kind, charset);
  }
}
