package com.duanxr.mhithrha.loader;

import com.duanxr.mhithrha.RuntimeCompilerException;
import com.duanxr.mhithrha.component.CompiledClassSupplier;
import com.google.common.base.Functions;
import com.google.common.base.Strings;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

  private final CompiledClassSupplier compiledClassSupplier;
  private final ClassLoader parent;
  @SneakyThrows
  private void hijackClassLoader(ClassLoader classLoader) {
    Field field = classLoader.getClass().getDeclaredField("parent");
    Object parent = field.get(classLoader);
    Object dynamicClassLoaderProxy = createDynamicClassLoaderProxy(parent);
    field.set(classLoader, dynamicClassLoaderProxy);
  }
  private Class<?>[] getInterfaces(Class<?> clazz) {
    if (!clazz.isInterface()) {
      return clazz.getInterfaces();
    }
    Class<?>[] interfaces = clazz.getInterfaces();
    Class<?>[] interfacesWithItself = new Class[interfaces.length + 1];
    System.arraycopy(interfaces, 0, interfacesWithItself, 0, interfaces.length);
    interfacesWithItself[interfaces.length] = clazz;
    return interfacesWithItself;
  }
  @SneakyThrows
  private Object createDynamicClassLoaderProxy(Object classLoader) {
    DynamicInvocationHandler dynamicInvocationHandler = new DynamicInvocationHandler(classLoader,
        this);
    dynamicInvocationHandler.getProxyMethods()
        .add(ClassLoader.class.getDeclaredMethod("findClass", String.class));
    return Proxy.newProxyInstance(classLoader.getClass().getClassLoader(),
        getInterfaces(classLoader.getClass()), dynamicInvocationHandler);
  }
  public IntrusiveClassLoader(ClassLoader parent, CompiledClassSupplier compiledClasses) {
    super(parent);
    this.parent = parent;
    if (UnsafeDefineClassHelper.notSupport()) {
      throw new RuntimeCompilerException("intrusive class loader not supported");
    }
    hijackClassLoader(parent);
    compiledClassSupplier = null;
  }

  public synchronized Map<String, Class<?>> defineCompiledClass(List<String> classNames) {
    synchronized (this) {
      return classNames.stream()
          .collect(Collectors.toMap(Functions.identity(), this::defineCompiledClass));
    }
  }

  @Override
  @SneakyThrows
  public Class<?> defineClass(String name, byte[] bytes) {
    if (UnsafeDefineClassHelper.notSupport()) {
      throw new RuntimeCompilerException("intrusive class loader not supported");
    }
    return UnsafeDefineClassHelper.defineClass(parent, name, bytes);
  }

  @Override
  public Map<String, Class<?>> defineCompiledClass(Collection<String> compiledClassNames) {
    return null;
  }

  private Set<String> findClassSet = ConcurrentHashMap.newKeySet();
  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    synchronized (this) {
      byte[] compiled = compiledClassSupplier.getCompiledClass(name);
      if (compiled != null) {
        return defineClass(name, compiled);
      }
      if (!findClassSet.add(name)) {
        throw new ClassNotFoundException(name);
      }
      try {
        return super.findClass(name);
      } finally {
        findClassSet.remove(name);
      }
    }
  }

  @SneakyThrows
  public Class<?> defineCompiledClass(String name) {
    synchronized (this) {
      byte[] compiled = compiledClassSupplier.getCompiledClass(name);
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
    return parent.loadClass(name);
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
    if (UnsafeDefineClassHelper.notSupport()) {
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

    public static boolean notSupport() {
      return DEFINE_CLASS_METHOD == null || RESOLVE_CLASS_METHOD == null;
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


  private class HijackedClassloader extends ClassLoader {

    private final static String CLASS_FILE_SUFFIX = ".class";

    private HijackedClassloader(ClassLoader classLoader) {

    }
  }
}
