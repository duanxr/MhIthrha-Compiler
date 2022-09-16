package com.duanxr.mhithrha.springboot.test.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.duanxr.mhithrha.JavaSourceCode;
import com.duanxr.mhithrha.RuntimeCompiler;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.junit.Assert;

/**
 * @author 段然 2022/9/6
 */
@AllArgsConstructor
@SuppressWarnings("unchecked")
public class TheadSafeTest {

  private RuntimeCompiler compiler;

  public void testSimpleConcurrencySafe() {
    StringBuilder largeCode = new StringBuilder("""
        public class ThreadSafeTestClass0 implements java.util.function.Supplier<Integer>{
          static final java.util.concurrent.atomic.AtomicInteger called = new java.util.concurrent.atomic.AtomicInteger(0);
          @Override
          public Integer get() {
            return called.getAndIncrement();
          }
        """);
    for (int i = 0; i < 999; i++) {
      largeCode.append("public String get").append(i)
          .append("() {return \"ThreadSafeTestClass0\";}\n");
    }
    largeCode.append('}');
    String className = "ThreadSafeTestClass";
    String code = largeCode.toString();
    try {
      final int nThreads = Runtime.getRuntime().availableProcessors() * 4;
      final AtomicInteger started = new AtomicInteger(0);
      final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
      final List<Future<Integer>> futures = new ArrayList<>();
      for (int i = 0; i < nThreads; i++) {
        futures.add(executor.submit(() -> {
          started.incrementAndGet();
          while (started.get() < nThreads)
          {}
          try {
            Class<?> compiledClass = compiler.compile(JavaSourceCode.of(code));
            Supplier<Integer> supplier = (Supplier<Integer>) compiledClass.getDeclaredConstructor()
                .newInstance();
            return supplier.get();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }));
      }
      executor.shutdown();
      Set<Integer> integerSet = new HashSet<>(nThreads);
      for (Future<Integer> f : futures) {
        Integer i = f.get(10, TimeUnit.SECONDS);
        integerSet.add(i);
      }
      assertEquals(nThreads, integerSet.size());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

}
