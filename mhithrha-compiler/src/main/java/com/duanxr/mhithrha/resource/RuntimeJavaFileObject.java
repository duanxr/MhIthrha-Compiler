package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.JavaClassNameUtil;
import javax.tools.JavaFileObject;

/**
 * @author 段然 2022/9/5
 */
public interface RuntimeJavaFileObject extends JavaFileObject {
  private String toClassName(String name) {
    return JavaClassNameUtil.toJavaName(name);
  }

  private String toPackageName(String name) {
    return JavaClassNameUtil.toJavaName(name);
  }

}
