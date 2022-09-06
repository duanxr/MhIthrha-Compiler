package com.duanxr.mhithrha.loader;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import sun.misc.Unsafe;

/**
 * @author 段然 2022/9/5
 */
public class ProxyRuntimeClassLoader extends RuntimeClassLoader {

  private static final Method DEFINE_CLASS_METHOD;

  static {
    try {
      Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
      theUnsafe.setAccessible(true);
      Unsafe unsafe = (Unsafe) theUnsafe.get(null);
      DEFINE_CLASS_METHOD = ClassLoader.class.getDeclaredMethod("defineClass", String.class,
          byte[].class, int.class, int.class);
      try {
        Field field = AccessibleObject.class.getDeclaredField("override");
        long offset = unsafe.objectFieldOffset(field);
        unsafe.putBoolean(DEFINE_CLASS_METHOD, offset, true);
      } catch (NoSuchFieldException e) {
        DEFINE_CLASS_METHOD.setAccessible(true);
      }
    } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException e) {
      throw new AssertionError(e);
    }
  }

  private final ClassLoader parent;

  public ProxyRuntimeClassLoader(ClassLoader parent) {
    super(parent);
    this.parent = parent;
  }

  public ProxyRuntimeClassLoader() {
    super();
    this.parent = getClass().getClassLoader();
  }

  @Override
  @SneakyThrows
  public Class<?> defineClass(String name, byte[] bytes) {
    return (Class<?>) DEFINE_CLASS_METHOD.invoke(parent, name, bytes, 0, bytes.length);
  }

  @Override
  public Class<?> defineReloadableClass(String name, byte[] bytes) {
    ReloadableClassLoader reloadableClassLoader = new ReloadableClassLoader(parent);
    return reloadableClassLoader.defineClass(name, bytes);
  }
}
