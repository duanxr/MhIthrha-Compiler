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
public class PackageTest {

  private RuntimeCompiler compiler;

  public void testPackageClass() {
    String code = """
        package   com.duanxr.mhithrha.test.runtime  ;
        import java.io.ByteArrayInputStream;
        import java.io.InputStream;
        import java.nio.charset.StandardCharsets;
        import java.util.function.Function;
        public class PackageTestClass1 implements Function<String,InputStream>{
            @Override
            public InputStream apply(String str) {
              InputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
              return stream;
            }
          }
        """;
    String className = "com.duanxr.mhithrha.test.runtime.PackageTestClass1";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
      Assert.assertEquals(compiledClass.getName(), className);
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

  public void testPackageClassWithoutName() {
    String code = """
        package   com.
                    duanxr.
                    mhithrha.test.runtime  ;
        import java.io.ByteArrayInputStream;
        import java.io.InputStream;
        import java.nio.charset.StandardCharsets;
        import java.util.function.Function;
        public class PackageTestClass2 implements Function<String,InputStream>{
            @Override
            public InputStream apply(String str) {
              InputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
              return stream;
            }
          }
        """;
    String className = "com.duanxr.mhithrha.test.runtime.PackageTestClass2";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(code));
      Assert.assertEquals(compiledClass.getName(), className);
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

  public void testPackageAccessClassFromAnotherClassLoader() {
    //you can not access the same package class from another class loader
    boolean shouldSucceed = false;
    String code = """
        package com.duanxr.mhithrha.test.component;
        import com.duanxr.mhithrha.test.runtime.PackageTestClass2;
        import java.io.ByteArrayInputStream;
        import java.io.InputStream;
        import java.nio.charset.StandardCharsets;
        import java.util.function.Function;
        public class PackageTestClass3 implements Function<String,InputStream>{
            @Override
            public InputStream apply(String str) {
              PackageClass packageClass = new PackageClass(str);
              PackageTestClass2 packageTestClass2 = new PackageTestClass2();
              String result = packageClass.name;
              packageClass.name = packageClass.getClass().getPackageName();
              return packageTestClass2.apply(packageClass.getName()+"."+result);
            }
          }
        """;
    String className = "com.duanxr.mhithrha.test.component.PackageTestClass3";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
      Assert.assertEquals(compiledClass.getName(), className);
      Function<String, InputStream> object = (Function<String, InputStream>) compiledClass.getConstructor()
          .newInstance();
      InputStream inputStream = object.apply(compiledClass.getSimpleName());
      InputStreamReader isReader = new InputStreamReader(inputStream);
      char[] charArray = new char[inputStream.available()];
      int read = isReader.read(charArray);
      String contents = new String(charArray);
      Assert.assertEquals(contents, className);
      if (!shouldSucceed) {
        fail();
      }
    } catch (Throwable e) {
      if (shouldSucceed) {
        e.printStackTrace();
        fail();
      }
    }
  }

  public void testPackageAccessCompiledClass() {
    String code0 = """
        package com.duanxr.mhithrha.test.component;
        import com.duanxr.mhithrha.test.component.CustomClass;
        import java.util.function.Function;
        class PackageTestClass4{
            String apply0(String str) {
              return new CustomClass().get(str);
            }
          }
        """;
    String className0 = "com.duanxr.mhithrha.test.component.PackageTestClass4";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className0, code0));
      Assert.assertEquals(compiledClass.getName(), className0);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
    String code1 = """
        package com.duanxr.mhithrha.test.component;
        import java.util.function.Function;
        public class PackageTestClass5 implements Function<String,String>{
            @Override
            public String apply(String str) {
              return new PackageTestClass4().apply0(str);
            }
          }
        """;
    String className1 = "com.duanxr.mhithrha.test.component.PackageTestClass5";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className1, code1));
      Assert.assertEquals(compiledClass.getName(), className1);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertEquals(object.apply(className1),
          "custom:com.duanxr.mhithrha.test.component.PackageTestClass5");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testNonAsciiPackageClass() {
    String code = """
        package   com.duanxr.
                  mhithrha.test.runtime.
                    测试包;
        import java.io.ByteArrayInputStream;
        import java.io.InputStream;
        import java.nio.charset.StandardCharsets;
        import java.util.function.Function;
        public class 包测试类6 implements Function<String,InputStream>{
            @Override
            public InputStream apply(String str) {
              InputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
              return stream;
            }
          }
        """;
    String className = "com.duanxr.mhithrha.test.runtime.测试包.包测试类6";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
      Assert.assertEquals(compiledClass.getName(), className);
      Function<String, InputStream> object = (Function<String, InputStream>) compiledClass.getConstructor()
          .newInstance();
      InputStream inputStream = object.apply(className);
      InputStreamReader isReader = new InputStreamReader(inputStream);
      char[] charArray = new char[inputStream.available()];
      int read = isReader.read(charArray);
      String contents = new String(charArray).trim();
      Assert.assertEquals(className,contents);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testNonAsciiPackageClassWithoutName() {
    String code = """
        package   com.duanxr.
                  mhithrha.test.runtime.
                    测试包;
        import java.io.ByteArrayInputStream;
        import java.io.InputStream;
        import java.nio.charset.StandardCharsets;
        import java.util.function.Function;
        public class 包测试类7 implements Function<String,InputStream>{
            @Override
            public InputStream apply(String str) {
              InputStream stream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
              return stream;
            }
          }
        """;
    String className = "com.duanxr.mhithrha.test.runtime.测试包.包测试类7";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(code));
      Assert.assertEquals(compiledClass.getName(), className);
      Function<String, InputStream> object = (Function<String, InputStream>) compiledClass.getConstructor()
          .newInstance();
      InputStream inputStream = object.apply(className);
      InputStreamReader isReader = new InputStreamReader(inputStream);
      char[] charArray = new char[inputStream.available()];
      int read = isReader.read(charArray);
      String contents = new String(charArray).trim();
      Assert.assertEquals(contents, className);
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
