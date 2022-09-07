package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.CallbackByteArrayOutputStream;
import com.duanxr.mhithrha.component.NameConvertor;
import com.google.common.base.Strings;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/5
 */
public class JavaMemoryClass extends SimpleJavaFileObject implements RuntimeJavaFileObject {
  private volatile byte[] bytes;
  public JavaMemoryClass(String name, long timeout) {
    super(createURI(name), Kind.CLASS);
  }
  @Override
  public InputStream openInputStream() {
    return new ByteArrayInputStream(getClassBytes());
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
  public byte[] getClassBytes() {
    synchronized (this) {
      if (bytes == null) {
        this.wait(30000);
      }
    }
    return bytes;
  }

  private static URI createURI(String name) {
    return URI.create("string:///" + NameConvertor.normalize(name) + Kind.CLASS.extension);
  }

  public boolean inPackage(String packageName) {
    return Strings.isNullOrEmpty(packageName) || getName().startsWith(packageName);
  }

}
