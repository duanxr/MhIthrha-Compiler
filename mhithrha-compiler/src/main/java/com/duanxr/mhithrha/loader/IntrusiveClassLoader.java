package com.duanxr.mhithrha.loader;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

/**
 * @author 段然 2022/9/5
 */
@Slf4j
public final class IntrusiveClassLoader extends RuntimeClassLoader {
  private final ClassLoader parent;
  public IntrusiveClassLoader(ClassLoader parent) {
    super(parent);
    this.parent = parent;
  }
  public IntrusiveClassLoader() {
    super();
    this.parent = getClass().getClassLoader();
  }
  @Override
  @SneakyThrows
  public Class<?> defineClass(String name, byte[] bytes) {
    return UnsafeDefineClassHelper.support() ?
        UnsafeDefineClassHelper.defineClass(parent, name, bytes) : null;
  }


  private static class UnsafeDefineClassHelper {
    private static final Method DEFINE_CLASS_METHOD = getDefineClassMethod();
    private static Method getDefineClassMethod() {
      try {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class,
            byte[].class, int.class, int.class);
        try {
          Field field = AccessibleObject.class.getDeclaredField("override");
          long offset = unsafe.objectFieldOffset(field);
          unsafe.putBoolean(defineClassMethod, offset, true);
        } catch (NoSuchFieldException e) {
          defineClassMethod.setAccessible(true);
        }
        return defineClassMethod;
      } catch (NoSuchMethodException | IllegalAccessException | NoSuchFieldException ignored) {
      }
      return null;
    }

    public static boolean support() {
      return DEFINE_CLASS_METHOD != null;
    }

    @SneakyThrows
    public static Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
      if (DEFINE_CLASS_METHOD != null) {
        return (Class<?>) UnsafeDefineClassHelper.DEFINE_CLASS_METHOD.invoke(classLoader, name,
            bytes, 0, bytes.length);
      }
      return null;
    }

  }
}
