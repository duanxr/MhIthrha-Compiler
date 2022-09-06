package com.duanxr.mhithrha.test;

import static org.junit.Assert.fail;

import com.duanxr.mhithrha.exports.RuntimeCompiler;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import org.junit.Assert;

/**
 * @author 段然 2022/9/6
 */
@AllArgsConstructor
public class TheadSafeTest {

  private RuntimeCompiler compiler;

  public void testClass() {
    String code = """
        public class TestClass1{
        }
        """;
    String className = "TestClass1";
    try {
      Class<?> compiledClass = compiler.compile(className, code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testClassWithoutName() {
    String code = """
        public class TestClass2{
        }
        """;
    String className = "TestClass2";
    try {
      Class<?> compiledClass = compiler.compile(code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @SuppressWarnings("unchecked")
  public void testClassWithInterface() {
    String code = """
        public class TestClass3 implements java.util.function.Supplier<String>{
          @Override
          public String get() {
            return "TestClass3";
          }
        }
        """;
    String className = "TestClass3";
    try {
      Class<?> compiledClass = compiler.compile(className, code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Supplier<String> object = (Supplier<String>) compiledClass.getConstructor().newInstance();
      Assert.assertEquals(object.get(), className);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @SuppressWarnings("unchecked")
  public void testClassWithInterfaceWithoutName() {
    String code = """
        public class TestClass4 implements java.util.function.Supplier<String>{
          @Override
          public String get() {
            return "TestClass4";
          }
        }
        """;
    String className = "TestClass4";
    try {
      Class<?> compiledClass = compiler.compile(code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Supplier<String> object = (Supplier<String>) compiledClass.getConstructor().newInstance();
      Assert.assertEquals(object.get(), className);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @SuppressWarnings("unchecked")
  public void testClassWithExceptionInterface() {
    String code = """
        public class TestClass5 implements java.util.function.Function<String,String>{
          @Override
          public String apply(String s) {
            if(!getClass().getSimpleName().equals(s)){
              throw new RuntimeException("TestClass5");
            }
            return getClass().getSimpleName();
          }
        }
        """;
    String className = "TestClass5";
    try {
      Class<?> compiledClass = compiler.compile(className, code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertEquals(object.apply(className), className);
      try {
        object.apply(null);
        fail();
      } catch (RuntimeException e) {
        Assert.assertEquals(e.getMessage(), className);
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  @SuppressWarnings("unchecked")
  public void testClassWithExceptionInterfaceWithoutName() {
    String code = """
        public class TestClass6 implements java.util.function.Function<String,String>{
          @Override
          public String apply(String s) {
            if(!getClass().getSimpleName().equals(s)){
              throw new RuntimeException("TestClass6");
            }
            return getClass().getSimpleName();
          }
        }
        """;
    String className = "TestClass6";
    try {
      Class<?> compiledClass = compiler.compile(code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertEquals(object.apply(className), className);
      try {
        object.apply(null);
        fail();
      } catch (RuntimeException e) {
        Assert.assertEquals(e.getMessage(), className);
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
