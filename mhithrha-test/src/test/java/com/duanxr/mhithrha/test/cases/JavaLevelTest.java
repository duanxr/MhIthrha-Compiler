package com.duanxr.mhithrha.test.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.duanxr.mhithrha.RuntimeCompiler;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.junit.Assert;

/**
 * @author 段然 2022/9/6
 */
@AllArgsConstructor
public class JavaLevelTest {
  private RuntimeCompiler compiler;

  @SuppressWarnings("unchecked")
  public void testJava8() {
    String code = """
        public class Java8TestClass implements java.util.function.Function<java.util.List<String>,java.util.List<String>>{
          @Override
          public java.util.List<String> apply(java.util.List<String> list) {
            java.util.List<String> result = new java.util.ArrayList<>();
            list.forEach(result::add);
            return result;
          }
        }
        """;
    String className = "Java8TestClass";
    try {
      Class<?> compiledClass = compiler.compile(className, code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<List<String>, List<String>> object = (Function<List<String>, List<String>>) compiledClass.getConstructor()
          .newInstance();
      ArrayList<String> list = new ArrayList<>(List.of("1", "2", "3"));
      List<String> result = object.apply(list);
      assertEquals(list, result);
      assertNotSame(list, result);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @SuppressWarnings("unchecked")
  public void testJava11() {
    String code = """
        public class Java11TestClass implements java.util.function.Function<String,Boolean>{
          @Override
          public Boolean apply(String str) {
            return str.isBlank();
          }
        }
        """;
    String className = "Java11TestClass";
    try {
      Class<?> compiledClass = compiler.compile(className, code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, Boolean> object = (Function<String, Boolean>) compiledClass.getConstructor()
          .newInstance();
      assertTrue(object.apply(""));
      assertTrue(object.apply(" "));
      assertTrue(object.apply("   "));
      assertFalse(object.apply("0"));
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }


  @SuppressWarnings("unchecked")
  public void testJava17() {
    String code = """
        public class Java17TestClass implements java.util.function.Function<String, String> {
             @Override
             public String apply(String str) {
               return new SealedInterfaceImpl1().apply(str);
             }
             private sealed interface SealedInterface extends
                 java.util.function.Function<String, String> permits SealedInterfaceImpl1 {
             }
             private final class SealedInterfaceImpl1 implements SealedInterface {
               @Override
               public String apply(String str) {
                 return switch (str) {
                   case "1" -> "1";
                   case "2" -> "2";
                   default -> "3";
                 };
               }
             }
           }
        """;
    String className = "Java17TestClass";
    try {
      Class<?> compiledClass = compiler.compile(className, code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      assertEquals(object.apply("1"), "1");
      assertEquals(object.apply("2"), "2");
      assertEquals(object.apply("3"), "3");
      assertEquals(object.apply("4"), "3");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }


  }
}
