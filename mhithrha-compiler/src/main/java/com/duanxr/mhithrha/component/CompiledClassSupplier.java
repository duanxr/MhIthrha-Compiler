package com.duanxr.mhithrha.component;

import com.duanxr.mhithrha.resource.JavaMemoryClass;
import java.util.function.Function;
import lombok.Setter;

/**
 * @author 段然 2022/9/14
 */
public class CompiledClassSupplier {
  @Setter
  private volatile Function<String, JavaMemoryClass> supplier;
  public byte[] getCompiledClass(String name) {
    if (supplier != null) {
      JavaMemoryClass javaMemoryClass = supplier.apply(name);
      if (javaMemoryClass != null) {
        return javaMemoryClass.getBytes();
      }
    }
    return null;
  }
}
