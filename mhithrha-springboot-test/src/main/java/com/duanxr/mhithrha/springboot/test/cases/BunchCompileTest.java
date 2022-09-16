package com.duanxr.mhithrha.springboot.test.cases;

import static org.junit.Assert.fail;

import com.duanxr.mhithrha.JavaSourceCode;
import com.duanxr.mhithrha.RuntimeCompiler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.junit.Assert;

/**
 * @author 段然 2022/9/10
 */
@AllArgsConstructor
@SuppressWarnings("unchecked")
public class BunchCompileTest {

  private RuntimeCompiler compiler;

  public void testBunchCompile() {
    String code = """
        package com.duanxr.mhithrha.springboot.test.runtime;
        @lombok.ToString
        public class BunchCompileTest%s{
            private final String value;
            public BunchCompileTest%s()
            {
              this.value = this.getClass().getName();
            }
          }
        """;
    String className = "com.duanxr.mhithrha.springboot.test.runtime.BunchCompileTest%s";
    try {
      final int count = 100;
      List<JavaSourceCode> list = new ArrayList<>(count);
      for (int i = 0; i < count; i++) {
        String codeI = String.format(code, i, i);
        String classNameI = String.format(className, i);
        list.add(JavaSourceCode.of(classNameI, codeI));
      }
      Map<String, Class<?>> compile = compiler.compile(list);
      Assert.assertEquals(compile.size(), count);
      compile.entrySet().parallelStream().forEach(entry -> {
        try {
          Object object = entry.getValue().getConstructor().newInstance();
          Assert.assertEquals(entry.getValue().getSimpleName() + "(value=" + entry.getKey() + ")",
              object.toString());
        } catch (Exception e) {
          fail(e.getMessage());
        }
      });
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }


  public void testBunchCompileWithDependency() {
    String code0 = """
        package com.duanxr.mhithrha.springboot.test.runtime;
          public class BunchCompileWithDependencyTest0 implements java.util.function.Supplier<String> {
               @Override
               public String get() {
                 return "0";
               }
           
             }
        """;
    String className0 = "com.duanxr.mhithrha.springboot.test.runtime.BunchCompileWithDependencyTest0";
    String code1 = """
        package com.duanxr.mhithrha.springboot.test.runtime;
          public class BunchCompileWithDependencyTest%s implements java.util.function.Supplier<String> {
               @Override
               public String get() {
                 return new BunchCompileWithDependencyTest%s().get()+"|"+%s;
               }
             }
        """;
    String className1 = "com.duanxr.mhithrha.springboot.test.runtime.BunchCompileWithDependencyTest%s";
    try {
      final int count = 101;
      List<JavaSourceCode> list = new ArrayList<>(count);
      list.add(JavaSourceCode.of(className0, code0));
      for (int i = 1; i < count; i++) {
        String codeI = String.format(code1, i, i - 1, i);
        String classNameI = String.format(className1, i);
        list.add(JavaSourceCode.of(classNameI, codeI));
      }
      Map<String, Class<?>> compile = compiler.compile(list);
      Assert.assertEquals(compile.size(), count);
      Class<?> test100 = compile.get(
          "com.duanxr.mhithrha.springboot.test.runtime.BunchCompileWithDependencyTest100");
      Supplier<String> instance = (Supplier<String>) test100.getDeclaredConstructor().newInstance();
      Assert.assertEquals(instance.get(),
          "0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59|60|61|62|63|64|65|66|67|68|69|70|71|72|73|74|75|76|77|78|79|80|81|82|83|84|85|86|87|88|89|90|91|92|93|94|95|96|97|98|99|100");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }


  public void testBunchCompileWithDependencyLoop() {
    String code = """
        package com.duanxr.mhithrha.springboot.test.runtime;
          public class BunchCompileWithDependencyLoop%s implements java.util.function.Supplier<String> {
               @Override
               public String get() {
                 return get%s()+"|"+
                 %s
                 get%s();
               }
               
               public String get%s() {
                 return "%s";
               }
           
             }
        """;
    String className = "com.duanxr.mhithrha.springboot.test.runtime.BunchCompileWithDependencyLoop%s";
    try {
      final int count = 101;
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < count; i++) {
        sb.append("new BunchCompileWithDependencyLoop").append(i).append("().get").append(i)
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
}
