package com.duanxr.mhithrha.component;

import com.duanxr.mhithrha.component.CallbackByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import javax.tools.SimpleJavaFileObject;
import lombok.SneakyThrows;

/**
 * @author 段然 2022/9/5
 */
public class JavaMemoryClass extends SimpleJavaFileObject  implements RuntimeJavaFileObject{
  private final long timeout;
  private volatile byte[] bytes;
  public JavaMemoryClass(String name, long timeout) {
    super(URI.create(name), Kind.CLASS);
    this.timeout = timeout < 0 ? 0 : timeout;
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
        this.wait(timeout);
      }
    }
    return bytes;
  }

  private static URI createURI(String name) {
    return URI.create("mhithrha:///" + name.replace('.', '/') + Kind.CLASS.extension);
  }
}
