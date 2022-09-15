package com.duanxr.mhithrha.loader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;

/**
 * @author 段然 2022/9/14
 */
public class DynamicInvocationHandler implements InvocationHandler {
  private final Object target;
  private final Object proxy;
  @Getter
  private final Set<Method> proxyMethods;

  public DynamicInvocationHandler(Object target, Object proxy) {
    this.target = target;
    this.proxy = proxy;
    this.proxyMethods = ConcurrentHashMap.newKeySet();
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (proxyMethods.contains(method)) {
      try {
        Object invoke = method.invoke(this.proxy, args);
        if (invoke != null) {
          return invoke;
        }
      } catch (Exception ignored) {

      }
    }
    return method.invoke(target, args);
  }
}