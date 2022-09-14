package com.duanxr.mhithrha.component;

import com.duanxr.mhithrha.resource.JavaMemoryClass;
import java.util.function.Function;
import lombok.Setter;

/**
 * @author 段然 2022/9/14
 */
public class CompiledClassConverter implements Function<String, byte[]> {

  @Setter
  private volatile Function<String, JavaMemoryClass> function;

  @Override
  public byte[] apply(String name) {
    if (function != null) {
      JavaMemoryClass javaMemoryClass = function.apply(name);
      if (javaMemoryClass != null) {
        return javaMemoryClass.getBytes();
      }
    }
    return null;
  }
}
