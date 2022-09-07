package com.duanxr.mhithrha.component;

import static javax.tools.StandardLocation.MODULE_PATH;

import javax.tools.JavaFileManager.Location;
import lombok.Getter;

/**
 * @author 段然 2022/9/7
 */
public class JavaModuleLocation implements Location {

  @Getter
  String moduleName;

  public JavaModuleLocation(Module module) {
    this.moduleName = module.getName();
  }


  @Override
  public String getName() {
    return MODULE_PATH.getName() + '[' + moduleName + ']';
  }

  @Override
  public boolean isOutputLocation() {
    return false;
  }

  @Override
  public boolean isModuleOrientedLocation() {
    return true;
  }
}
