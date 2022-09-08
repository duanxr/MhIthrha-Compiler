package com.duanxr.mhithrha.resource;

import com.duanxr.mhithrha.component.JavaNameUtil;
import javax.tools.JavaFileObject;

/**
 * @author 段然 2022/9/5
 */
public interface RuntimeJavaFileObject extends JavaFileObject {
  private String toClassName(String name) {
    return JavaNameUtil.toJavaName(name);
  }

  private String toPackageName(String name) {
    return JavaNameUtil.toJavaName(name);
  }

}
