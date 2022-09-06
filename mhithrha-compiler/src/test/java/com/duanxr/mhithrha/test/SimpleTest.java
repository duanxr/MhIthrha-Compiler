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
@SuppressWarnings("unchecked")
public class SimpleTest {

  private RuntimeCompiler compiler;

  public void testClass() {
    String code = """
        public class SimpleTestClass1{
        }
        """;
    String className = "SimpleTestClass1";
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
        public class SimpleTestClass2{
        }
        """;
    String className = "SimpleTestClass2";
    try {
      Class<?> compiledClass = compiler.compile(code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testClassWithInterface() {
    String code = """
        public class SimpleTestClass3 implements java.util.function.Supplier<String>{
          @Override
          public String get() {
            return "SimpleTestClass3";
          }
        }
        """;
    String className = "SimpleTestClass3";
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

  public void testClassWithInterfaceWithoutName() {
    String code = """
        public class SimpleTestClass4 implements java.util.function.Supplier<String>{
          @Override
          public String get() {
            return "SimpleTestClass4";
          }
        }
        """;
    String className = "SimpleTestClass4";
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

  public void testClassWithExceptionInterface() {
    String code = """
        public class SimpleTestClass5 implements java.util.function.Function<String,String>{
          @Override
          public String apply(String s) {
            if(!getClass().getSimpleName().equals(s)){
              throw new RuntimeException("SimpleTestClass5");
            }
            return getClass().getSimpleName();
          }
        }
        """;
    String className = "SimpleTestClass5";
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

  public void testClassWithExceptionInterfaceWithoutName() {
    String code = """
        public class SimpleTestClass6 implements java.util.function.Function<String,String>{
          @Override
          public String apply(String s) {
            if(!getClass().getSimpleName().equals(s)){
              throw new RuntimeException("SimpleTestClass6");
            }
            return getClass().getSimpleName();
          }
        }
        """;
    String className = "SimpleTestClass6";
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
