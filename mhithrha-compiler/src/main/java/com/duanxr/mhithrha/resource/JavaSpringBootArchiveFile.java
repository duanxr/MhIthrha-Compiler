package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.JavaClassNameUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import lombok.Getter;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.tool.ArchiveFileObject;
import org.eclipse.jdt.internal.compiler.tool.Util;

/**
 * @author 段然 2022/9/5
 */
public class JavaSpringBootArchiveFile implements RuntimeJavaFileObject {

  private final Charset charset;
  @Getter
  private final String className;
  private final String entryName;
  private final JarFile jarFile;
  @Getter
  private final String packageName;
  private final URL url;

  public JavaSpringBootArchiveFile(URL url, JarFile jarFile, String entryName, Charset charset) {
    super();
    this.url = url;
    this.charset = charset;
    this.jarFile = jarFile;
    this.entryName = entryName;
    this.className = JavaClassNameUtil.toPackageName(entryName);
    this.packageName = JavaClassNameUtil.toPackageName(className);
  }

  public boolean inPackage(String targetPackageName) {
    return JavaClassNameUtil.inPackage(this.packageName, targetPackageName);
  }

  public boolean inPackages(String targetPackageName) {
    return JavaClassNameUtil.inPackages(this.packageName, targetPackageName);
  }

  @Override
  public int hashCode() {
    return this.toUri().hashCode();
  }

  @Override
  public Modifier getAccessLevel() {
    if (getKind() != Kind.CLASS) {
      return null;
    }
    ClassFileReader reader = getClassReader();
    if (reader == null) {
      return null;
    }
    final int accessFlags = reader.accessFlags();
    if ((accessFlags & ClassFileConstants.AccPublic) != 0) {
      return Modifier.PUBLIC;
    }
    if ((accessFlags & ClassFileConstants.AccAbstract) != 0) {
      return Modifier.ABSTRACT;
    }
    if ((accessFlags & ClassFileConstants.AccFinal) != 0) {
      return Modifier.FINAL;
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ArchiveFileObject archiveFileObject)) {
      return false;
    }
    return archiveFileObject.toUri().equals(this.toUri());
  }

  protected ClassFileReader getClassReader() {
    ClassFileReader reader = null;
    try {
      reader = ClassFileReader.read(jarFile, this.entryName);
    } catch (ClassFormatException | IOException ignored) {
    }
    return reader;
  }

  @Override
  public URI toUri() {
    try {
      return new URI(url.toString() + this.entryName); //$NON-NLS-1$//$NON-NLS-2$
    } catch (URISyntaxException e) {
      return null;
    }
  }

  @Override
  public Kind getKind() {
    String name = this.entryName.toLowerCase();
    if (name.endsWith(Kind.CLASS.extension)) {
      return Kind.CLASS;
    } else if (name.endsWith(Kind.SOURCE.extension)) {
      return Kind.SOURCE;
    } else if (name.endsWith(Kind.HTML.extension)) {
      return Kind.HTML;
    }
    return Kind.OTHER;
  }

  @Override
  public String getName() {
    return this.entryName;
  }

  @Override
  public NestingKind getNestingKind() {
    switch (getKind()) {
      case SOURCE:
        return NestingKind.TOP_LEVEL;
      case CLASS:
        ClassFileReader reader = getClassReader();
        if (reader == null) {
          return null;
        }
        if (reader.isAnonymous()) {
          return NestingKind.ANONYMOUS;
        }
        if (reader.isLocal()) {
          return NestingKind.LOCAL;
        }
        if (reader.isMember()) {
          return NestingKind.MEMBER;
        }
        return NestingKind.TOP_LEVEL;
      default:
        return null;
    }
  }

  @Override
  public InputStream openInputStream() throws IOException {
    try (InputStream inputStream = jarFile.getInputStream(jarFile.getEntry(this.entryName))) {
      return new ByteArrayInputStream(inputStream.readAllBytes());
    }
  }

  @Override
  public boolean isNameCompatible(String simpleName, Kind kind) {
    return this.entryName.endsWith(simpleName + kind.extension);
  }

  @Override
  public OutputStream openOutputStream() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Reader openReader(boolean ignoreEncodingErrors) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
    if (getKind() == Kind.SOURCE) {
      ZipEntry zipEntry = jarFile.getEntry(this.entryName);
      return Util.getCharContents(this, ignoreEncodingErrors,
          org.eclipse.jdt.internal.compiler.util.Util.getZipEntryByteContent(zipEntry, jarFile),
          this.charset.name());
    }
    return null;
  }

  @Override
  public Writer openWriter() {
    throw new UnsupportedOperationException();
  }

  @Override
  public long getLastModified() {
    ZipEntry zipEntry = jarFile.getEntry(this.entryName);
    return zipEntry.getTime();
  }

  @Override
  public boolean delete() {
    throw new UnsupportedOperationException();
  }


  @Override
  public String toString() {
    return jarFile.toString() + "[" + entryName + "]";
  }
}
