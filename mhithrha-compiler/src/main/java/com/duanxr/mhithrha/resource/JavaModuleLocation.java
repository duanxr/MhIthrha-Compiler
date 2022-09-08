package com.duanxr.mhithrha.resource;

import static javax.tools.StandardLocation.MODULE_PATH;

import java.io.File;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.net.URI;
import java.util.Optional;
import javax.tools.JavaFileManager.Location;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @author 段然 2022/9/7
 */
@EqualsAndHashCode
public class JavaModuleLocation implements Location {
  @Getter
  private final String moduleName;
  @Getter
  private final ModuleReference reference;
  @Getter
  private final File file;

  public JavaModuleLocation(ResolvedModule module) {
    this.moduleName = module.name();
    this.reference = module.reference();
    this.file = gerReferenceFile();
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
  public boolean notEmpty() {
    return reference != null && reference.location().isPresent();
  }
  public boolean notJrt() {
    if (reference == null) {
      return false;
    }
    Optional<URI> location = reference.location();
    return location.isPresent() && !"jrt".equalsIgnoreCase(
        location.get().getScheme());
  }

  public boolean isExist() {
    return file != null && file.exists();
  }

  private File gerReferenceFile() {
    if (reference != null) {
      Optional<URI> location = reference.location();
      if (location.isPresent()) {
        URI uri = location.get();
        if ("file".equalsIgnoreCase(uri.getScheme())) {
          return new File(uri);
        }
      }
    }
    return null;
  }

}
