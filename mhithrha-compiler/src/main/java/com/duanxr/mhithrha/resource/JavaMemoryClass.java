package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.CallbackByteArrayOutputStream;
import com.duanxr.mhithrha.component.JavaNameUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;
import lombok.Getter;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/5
 */
public class JavaMemoryClass extends SimpleJavaFileObject implements RuntimeJavaFileObject {

  @Getter
  private final String className;
  @Getter
  private final String packageName;
  private final long timeout;
  private volatile byte[] bytes;

  public JavaMemoryClass(String name, long timeout) {
    super(createURI(JavaNameUtil.toURI(name)), Kind.CLASS);
    this.className = JavaNameUtil.toJavaName(name);
    this.packageName = JavaNameUtil.toPackageName(name);
    this.timeout = timeout;
  }

  private static URI createURI(String path) {
    return URI.create("string:///" + path + Kind.CLASS.extension);
  }

  @Override
  public InputStream openInputStream() {
    return new ByteArrayInputStream(getBytes());
  }

  @Override
  public OutputStream openOutputStream() {
    return new CallbackByteArrayOutputStream(this::finish);
  }

  private void finish(CallbackByteArrayOutputStream stream) {
    synchronized (this) {
      if (bytes == null) {
        bytes = stream.toByteArray();
      }
      this.notifyAll();
    }
  }

  @SneakyThrows
  public byte[] getBytes() {
    synchronized (this) {
      if (bytes == null) {
        this.wait(timeout);
      }
    }
    return bytes;
  }

  public boolean inPackage(String targetPackageName) {
    return JavaNameUtil.inPackage(this.packageName, targetPackageName);
  }

  public boolean inPackages(String targetPackageName) {
    return JavaNameUtil.inPackages(this.packageName, targetPackageName);
  }
}
