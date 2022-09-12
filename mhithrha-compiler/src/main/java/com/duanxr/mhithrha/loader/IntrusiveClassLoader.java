package com.duanxr.mhithrha.loader;

import com.duanxr.mhithrha.RuntimeCompilerException;
import com.google.common.base.Functions;
import com.google.common.base.Strings;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

/**
 * @author 段然 2022/9/5
 */
@Slf4j
public final class IntrusiveClassLoader extends RuntimeClassLoader {
  private final Map<String, byte[]> defineTaskMap = new HashMap<>();
  private final ClassLoader parent;
  public IntrusiveClassLoader(ClassLoader parent) {
    super(parent);
    this.parent = parent;
    if (!UnsafeDefineClassHelper.support() && !LookupDefineClassHelper.support()) {
      throw new RuntimeCompilerException("intrusive class loader not supported");
    }
  }

  public Class<?> loadClass(String name) throws ClassNotFoundException {
    try {
      return parent.loadClass(name);
    } catch (Throwable e) {
      Class<?> clazz = super.loadClass(name);
      resolveClass0(clazz);
      return parent.loadClass(name);
    }
  }

  private final Set<String> findClassSet = new HashSet<>();

  private Class<?> loadParentClass(String name) throws ClassNotFoundException {
    try {
      return parent.loadClass(name);
    } catch (Exception e) {
      String notFoundClass = findNotFoundClass(e);
      Class<?> clazz = super.loadClass(notFoundClass);
      resolveClass0(clazz);
      return parent.loadClass(name);
    }
  }

  @SneakyThrows
  public Class<?> defineTask(String name) {
    synchronized (defineTaskMap) {
      byte[] bytes = defineTaskMap.remove(name);
      if (bytes == null) {
        return parent.loadClass(name);
      }
      try {
        return defineClass(name, bytes);
      } catch (Exception e) {
        String dependency = findNotFoundClass(e);
        if (dependency == null) {
          throw e;
        }
        defineTask(dependency);
        return defineClass(name, bytes);
      }
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

  public Map<String, Class<?>> defineClasses(Map<String, byte[]> classBytes) {
    Map<String, Class<?>> classes = new HashMap<>(classBytes.size());
    List<String> classNames = classBytes.keySet().stream().toList();
    synchronized (defineTaskMap) {
      defineTaskMap.putAll(classBytes);
      Map<String, Class<?>> classMap = classNames.stream()
          .collect(Collectors.toMap(Functions.identity(), this::defineTask));
      classBytes.keySet().forEach(defineTaskMap::remove);
    }
    return classes;
  }

  @Override
  public Class<?> defineReloadableClass(String name, byte[] bytes) {
    return new IsolatedClassLoader(parent).defineClass(name, bytes);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    try {
      return parent.loadClass(name);
    } catch (Throwable e) {
      Class<?> clazz = super.findClass(name);
      resolveClass0(clazz);
      return parent.loadClass(name);
    }
  }


  @Override
  public Class<?> defineClass(String name, byte[] bytes) {
    if (UnsafeDefineClassHelper.support()) {
      return UnsafeDefineClassHelper.defineClass(parent, name, bytes);
    }
    //if (LookupDefineClassHelper.support()) {return LookupDefineClassHelper.defineClass(parent, name, bytes);}
    throw new RuntimeCompilerException("intrusive class loader not supported");
  }

  private void resolveClass0(Class<?> c) {
    if (UnsafeDefineClassHelper.support()) {
      UnsafeDefineClassHelper.resolveClass(parent, c);
      return;
    }
    //if (LookupDefineClassHelper.support()) {return LookupDefineClassHelper.defineClass(parent, name, bytes);}
    throw new RuntimeCompilerException("intrusive class loader not supported");
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
        log.debug("LookupDefineClassHelper not supported", e);
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
    private static final Method RESOLVE_CLASS_METHOD = getResolveClassMethod();

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

  private String defineClassSourceLocation(ProtectionDomain pd) {
    CodeSource cs = pd.getCodeSource();
    String source = null;
    if (cs != null && cs.getLocation() != null) {
      source = cs.getLocation().toString();
    }
    return source;
  }
}
