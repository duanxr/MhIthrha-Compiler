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
        UnsafeDefineClassHelper.defineClass(parent, name, bytes) :
        LookupDefineClassHelper.support() ?
            LookupDefineClassHelper.defineClass(parent, name, bytes) :
            null;
  }

  private static class LookupDefineClassHelper {

    private static final Method[] DEFINE_CLASS_METHODS = getDefineClassMethods();

    private static Method[] getDefineClassMethods() {
      try {
        Class<?> methodHandles = Class.forName("java.lang.invoke.MethodHandles");
        Class<?> methodHandle = Class.forName("java.lang.invoke.MethodHandle");
        Class<?> methodType = Class.forName("java.lang.invoke.MethodType");
        Class<?> methodHandlesLookup = Class.forName("java.lang.invoke.MethodHandles$Lookup");
        Method[] methods = new Method[4];
        methods[0] = methodHandles.getDeclaredMethod("lookup", null);
        methods[1] = methodType.getDeclaredMethod("methodType", Class.class, Class[].class);
        methods[2] = methodHandlesLookup.getDeclaredMethod("findVirtual", Class.class, String.class,
            methodType);
        methods[3] = methodHandle.getDeclaredMethod("invokeWithArguments", Object[].class);
        return methods;
      } catch (Exception e) {
        log.debug("LookupDefineClassHelper not support", e);
      }
      return null;
    }

    public static boolean support() {
      return DEFINE_CLASS_METHODS != null
          && DEFINE_CLASS_METHODS[0] != null && DEFINE_CLASS_METHODS[1] != null
          && DEFINE_CLASS_METHODS[2] != null && DEFINE_CLASS_METHODS[3] != null;
    }

    @SneakyThrows
    public static Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
      if (DEFINE_CLASS_METHODS == null ||
          DEFINE_CLASS_METHODS[0] == null || DEFINE_CLASS_METHODS[1] == null ||
          DEFINE_CLASS_METHODS[2] == null || DEFINE_CLASS_METHODS[3] == null) {
        return null;
      }
      Object lookup = DEFINE_CLASS_METHODS[0].invoke(null, null);
      Object type = DEFINE_CLASS_METHODS[1].invoke(null, Class.class,
          new Class[]{String.class, byte[].class, int.class, int.class});
      Object method = DEFINE_CLASS_METHODS[2].invoke(lookup, ClassLoader.class, "defineClass",
          type);
      return (Class<?>) DEFINE_CLASS_METHODS[3].invoke(method, classLoader, name, bytes, 0,
          bytes.length);
    }
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
        }        if(true)return null;
        return defineClassMethod;
      } catch (Exception e) {
        e.printStackTrace();
        log.debug("UnsafeDefineClassHelper not support", e);
      }
      return null;
    }

    public static boolean support() {
      return DEFINE_CLASS_METHOD != null;
    }

    @SneakyThrows
    public static Class<?> defineClass(ClassLoader classLoader, String name, byte[] bytes) {
      if (DEFINE_CLASS_METHOD == null) {
        return null;
      }
      return (Class<?>) DEFINE_CLASS_METHOD.invoke(classLoader, name, bytes, 0, bytes.length);
    }

  }
}
