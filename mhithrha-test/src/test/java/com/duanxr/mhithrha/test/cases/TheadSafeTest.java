package com.duanxr.mhithrha.test.cases;

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
            ;
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


  public void testBunchCompileWithDependencyLoop() {
    String commonInterface = """
        package com.duanxr.mhithrha.test.runtime;
        public interface ComplexConcurrencySafeInterface {
         default String apply(String str){
          return "CCSI|"+str;
         }
        }""";
    String commonDependency = """
        package com.duanxr.mhithrha.test.runtime;
        public class ComplexConcurrencySafeDependency {
          public String apply(String str){
            return "CCSD|"+str;
          }
        }""";
    String commonStaticDependency = """
        package com.duanxr.mhithrha.test.runtime;
        public class ComplexConcurrencySafeDependency {
          public static final java.util.concurrent.atomic.AtomicInteger called = new java.util.concurrent.atomic.AtomicInteger(0);
          public static AtomicInteger get() {
            return called;
          }
        }""";

    StringBuilder largeCode = new StringBuilder("""
        package com.duanxr.mhithrha.test.runtime;
        import com.google.common.base.Strings;
          public class ComplexConcurrencySafeClass%s implements ComplexConcurrencySafeInterface,java.util.function.Supplier<String> {
               @Override
               public String get() {
                 ComplexConcurrencySafeDependency.get().incrementAndGet();
                 return get%s()+"|"+
                 %s
                 get%s();
               }
               public String get%s() {
                 return new CustomClass().get(new ComplexConcurrencySafeDependency().apply(Strings.repeat("%s",2)));
               }
        """);
    for (int i = 0; i < 999; i++) {
      largeCode.append("public String getNum").append(i).append("() {return \"i\";}\n");
    }
    largeCode.append('}');
    String code = largeCode.toString();
    String className = "com.duanxr.mhithrha.test.runtime.ComplexConcurrencySafeClass%s";
    try {
      final int count = 101;
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < count; i++) {
        sb.append("new ComplexConcurrencySafeClass").append(i).append("().get").append(i)
            .append("()+\"|\"+");
      }
      String methods = sb.toString();
      List<JavaSourceCode> list = new ArrayList<>(count);
      for (int i = 0; i < count; i++) {
        String codeI = String.format(code, i, i, methods, i, i, i);
        String classNameI = String.format(className, i);
        list.add(JavaSourceCode.of(classNameI, codeI));
      }
      Map<String, Class<?>> compile = compiler.compile(list);
      Assert.assertEquals(compile.size(), count);
      compile.entrySet().parallelStream().forEach(entry -> {
        try {
          Supplier<String> object = (Supplier<String>) entry.getValue().getConstructor()
              .newInstance();
          String index = entry.getValue().getSimpleName().substring(30);
          Assert.assertEquals(index
                  + "|0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100|"
                  + index,
              object.get());
        } catch (Exception e) {
          fail(e.getMessage());
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testComplexConcurrencySafe() {

    StringBuilder largeCode = new StringBuilder("""
        package com.duanxr.mhithrha.test.runtime;
        import com.duanxr.mhithrha.test.runtime.ComplexConcurrencySafeInterface;
        @lombok.ToString
        public class ComplexConcurrencySafe%s_%s{
            private final String value;
            public BunchCompileTest%s()
            {
              this.value = this.getClass().getName();
            }
          }
         
        public class ThreadSafeTestClass0 implements ComplexConcurrencySafeInterface,java.util.function.Supplier<Integer>{
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
            ;
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
