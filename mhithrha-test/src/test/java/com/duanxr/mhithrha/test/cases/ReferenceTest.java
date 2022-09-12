package com.duanxr.mhithrha.test.cases;

import static org.junit.Assert.fail;

import com.duanxr.mhithrha.JavaSourceCode;
import com.duanxr.mhithrha.RuntimeCompiler;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.junit.Assert;

/**
 * @author 段然 2022/9/6
 */

@AllArgsConstructor
@SuppressWarnings("unchecked")
public class ReferenceTest {

  private RuntimeCompiler compiler;

  public void testReferenceJavaBase() {
    String code = """
        import java.io.ByteArrayInputStream;
        import java.io.InputStream;
        import java.nio.charset.StandardCharsets;
        import java.util.function.Function;
        public class ReferenceTestClass1 implements Function<String,InputStream>{
            @Override
            public InputStream apply(String str) {
              InputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
              return stream;
            }
          }
        """;
    String className = "ReferenceTestClass1";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, InputStream> object = (Function<String, InputStream>) compiledClass.getConstructor()
          .newInstance();
      InputStream inputStream = object.apply(className);
      InputStreamReader isReader = new InputStreamReader(inputStream);
      char[] charArray = new char[inputStream.available()];
      int read = isReader.read(charArray);
      String contents = new String(charArray);
      Assert.assertEquals(contents, className);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }


  public void testReferenceInnerClass() {
    String code = """
        import java.io.ByteArrayInputStream;
        import java.io.InputStream;
        import java.nio.charset.StandardCharsets;
        import java.util.function.Function;
        public class ReferenceTestClass2 implements Function<String, InputStream> {
            @Override
            public InputStream apply(String str) {
              ReferenceTestClass2Inner1 inner1 = new ReferenceTestClass2Inner1();
              ReferenceTestClass2Inner2 inner2 = new ReferenceTestClass2Inner2();
              byte[] bytes = inner1.apply(str);
              InputStream stream = inner2.apply(bytes);
              return stream;
            }
            private class ReferenceTestClass2Inner1 implements Function<String, byte[]> {
              @Override
              public byte[] apply(String str) {
                return str.getBytes(StandardCharsets.UTF_8);
              }
            }
            public static class ReferenceTestClass2Inner2 implements Function<byte[], InputStream> {
              @Override
              public InputStream apply(byte[] bytes) {
                return new ByteArrayInputStream(bytes);
              }
            }
          }
        """;
    String className = "ReferenceTestClass2";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, InputStream> object = (Function<String, InputStream>) compiledClass.getConstructor()
          .newInstance();
      InputStream inputStream = object.apply(className);
      InputStreamReader isReader = new InputStreamReader(inputStream);
      char[] charArray = new char[inputStream.available()];
      int read = isReader.read(charArray);
      String contents = new String(charArray);
      Assert.assertEquals(contents, className);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testReferenceMavenDependency() {
    String code = """
        import com.google.common.base.Strings;
        import java.util.function.Function;
        public class ReferenceTestClass3 implements Function<String,Boolean>{
            @Override
            public Boolean apply(String str) {
              return Strings.isNullOrEmpty(str);
            }
          }
        """;
    String className = "ReferenceTestClass3";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, Boolean> object = (Function<String, Boolean>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertTrue(object.apply(null));
      Assert.assertTrue(object.apply(""));
      Assert.assertFalse(object.apply(className));
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testReferenceParentClassLoaderLoadedClass() {
    String code = """
        import com.duanxr.mhithrha.test.component.CustomClass;
        import java.util.function.Function;
        public class ReferenceTestClass4 implements Function<String,String>{
            @Override
            public String apply(String str) {
              return new CustomClass().get(str);
            }
          }
        """;
    String className = "ReferenceTestClass4";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertEquals(object.apply(className), "custom:ReferenceTestClass4");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testReferenceCompiledClass() {
    String code0 = """
        import com.duanxr.mhithrha.test.component.CustomClass;
        import java.util.function.Function;
        public class ReferenceTestClass5 implements Function<String,String>{
            @Override
            public String apply(String str) {
              return new CustomClass().get(str);
            }
          }
        """;
    String className0 = "ReferenceTestClass5";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className0, code0));
      Assert.assertEquals(compiledClass.getSimpleName(), className0);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertEquals(object.apply(className0), "custom:ReferenceTestClass5");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
    String code1 = """
        import java.util.function.Function;
        public class ReferenceTestClass6 implements Function<String,String>{
            @Override
            public String apply(String str) {
              return new ReferenceTestClass5().apply(str);
            }
          }
        """;
    String className1 = "ReferenceTestClass6";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className1, code1));
      Assert.assertEquals(compiledClass.getSimpleName(), className1);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertEquals(object.apply(className1), "custom:ReferenceTestClass6");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }

  }


  public void testReferenceEachOtherWithOneCompilation() {
    String code = """
        import java.util.function.Function;
          public class ReferenceTestClass7 implements Function<String, String> {
               @Override
               public String apply(String str) {
                 ReferenceTestClass7Inner1 inner1 = new ReferenceTestClass7Inner1();
                 ReferenceTestClass7Inner2 inner2 = new ReferenceTestClass7Inner2();
                 return inner1.apply(str) + '|' + inner2.apply(str);
               }
               public String addPrefix(String str) {
                 return "out:" + str;
               }
               public String addSuffix(String str) {
                 return str + ":out";
               }
               public static class ReferenceTestClass7Inner1 implements Function<String, String> {
                 @Override
                 public String apply(String str) {
                   ReferenceTestClass7 referenceTestClass7 = new ReferenceTestClass7();
                   ReferenceTestClass7Inner2 inner2 = new ReferenceTestClass7Inner2();
                   return addPrefix(referenceTestClass7.addPrefix(inner2.addPrefix(str)));
                 }
                 public String addPrefix(String str) {
                   return "inner1:" + str;
                 }
                 public String addSuffix(String str) {
                   return str + ":inner1";
                 }
               }
               public static class ReferenceTestClass7Inner2 implements Function<String, String> {
                 @Override
                 public String apply(String str) {
                   ReferenceTestClass7 referenceTestClass7 = new ReferenceTestClass7();
                   ReferenceTestClass7Inner1 inner1 = new ReferenceTestClass7Inner1();
                   return addSuffix(referenceTestClass7.addSuffix(inner1.addSuffix(str)));
                 }
                 public String addPrefix(String str) {
                   return "inner2:" + str;
                 }
                 public String addSuffix(String str) {
                   return str + ":inner2";
                 }
               }
             }
        """;
    String className = "ReferenceTestClass7";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertEquals(object.apply(className), "inner1:out:inner2:ReferenceTestClass7|ReferenceTestClass7:inner1:out:inner2");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
