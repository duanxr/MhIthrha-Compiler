package com.duanxr.mhithrha.loader;

import com.duanxr.mhithrha.RuntimeCompilerException;
import com.google.common.base.Functions;
import com.google.common.base.Strings;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

/**
 * @author 段然 2022/9/5
 */
@Slf4j
public final class IntrusiveClassLoader extends RuntimeClassLoader {

  private final Function<String, byte[]> compiledClasses;
  private final ClassLoader parent;

  public IntrusiveClassLoader(ClassLoader parent, Function<String, byte[]> compiledClasses) {
    super(parent);
    this.parent = parent;
    this.compiledClasses = compiledClasses;
    if (!UnsafeDefineClassHelper.support()) {
      throw new RuntimeCompilerException("intrusive class loader not supported");
    }
  }

  public synchronized Map<String, Class<?>> defineCompiledClasses(List<String> classNames) {
    synchronized (this) {
      return classNames.stream()
          .collect(Collectors.toMap(Functions.identity(), this::defineCompiledClass));
    }
  }

  @Override
  @SneakyThrows
  public Class<?> defineClass(String name, byte[] bytes) {
    try {
      if (!UnsafeDefineClassHelper.support()) {
        throw new RuntimeCompilerException("intrusive class loader not supported");
      }
      return UnsafeDefineClassHelper.defineClass(parent, name, bytes);
    } catch (Throwable e) {
      String dependency = findNotFoundClass(e);
      if (dependency != null) {
        Class<?> urlClass = super.loadClass(dependency);
        if (urlClass != null) {
          resolveParentClass(urlClass);
          return defineClass(name, bytes);
        }
      }
      throw e;
    }
  }

  @SneakyThrows
  public Class<?> defineCompiledClass(String name) {
    synchronized (this) {
      byte[] compiled = compiledClasses.apply(name);
      if (compiled == null) {
        return loadClass(name);
      }
      try {
        return defineClass(name, compiled);
      } catch (Exception e) {
        String dependency = findNotFoundClass(e);
        if (dependency == null) {
          throw e;
        }
        defineCompiledClass(dependency);
        return defineClass(name, compiled);
      }
    }
  }

  public Class<?> loadClass(String name) throws ClassNotFoundException {
    try {
      return parent.loadClass(name);
    } catch (Throwable e) {
      Class<?> clazz = super.loadClass(name);
      resolveParentClass(clazz);
      return parent.loadClass(name);
    }
  }

  private String findNotFoundClass(Throwable e) {
    if (e == null) {
      return null;
    }
    if (e instanceof ClassNotFoundException) {
      return Strings.emptyToNull(Strings.nullToEmpty(e.getMessage()).trim());
    }
    return findNotFoundClass(e.getCause());
  }

  private void resolveParentClass(Class<?> c) {
    if (!UnsafeDefineClassHelper.support()) {
      throw new RuntimeCompilerException("intrusive class loader not supported");
    }
    UnsafeDefineClassHelper.resolveClass(parent, c);
  }

  private static class UnsafeDefineClassHelper {

    private static final Method DEFINE_CLASS_METHOD = getDefineClassMethod();
    private static final Method RESOLVE_CLASS_METHOD = getResolveClassMethod();

    private static Method getDefineClassMethod() {
      try {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass",
            String.class,
            byte[].class, int.class, int.class);
        try {
          Field field = AccessibleObject.class.getDeclaredField("override");
          long offset = unsafe.objectFieldOffset(field);
          unsafe.putBoolean(defineClassMethod, offset, true);
        } catch (NoSuchFieldException e) {
          defineClassMethod.setAccessible(true);
        }
        return defineClassMethod;
      } catch (Exception e) {
        log.debug("UnsafeDefineClassHelper not support", e);
      }
      return null;
    }

    private static Method getResolveClassMethod() {
      try {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        Method resolveClassMethod = ClassLoader.class.getDeclaredMethod("resolveClass",
            Class.class);
        try {
          Field field = AccessibleObject.class.getDeclaredField("override");
          long offset = unsafe.objectFieldOffset(field);
          unsafe.putBoolean(resolveClassMethod, offset, true);
        } catch (NoSuchFieldException e) {
          resolveClassMethod.setAccessible(true);
        }
        return resolveClassMethod;
      } catch (Exception e) {
        log.debug("UnsafeDefineClassHelper not support", e);
      }
      return null;
    }

    public static boolean support() {
      return DEFINE_CLASS_METHOD != null && RESOLVE_CLASS_METHOD != null;
    }

    @SneakyThrows
    public static Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
      if (DEFINE_CLASS_METHOD == null) {
        return null;
      }
      return (Class<?>) DEFINE_CLASS_METHOD.invoke(classLoader, name, bytes, 0, bytes.length);
    }

    @SneakyThrows
    public static void resolveClass(ClassLoader classLoader, Class<?> c) {
      if (RESOLVE_CLASS_METHOD == null) {
        return;
      }
      RESOLVE_CLASS_METHOD.invoke(classLoader, c);
    }
  }
}
