package com.duanxr.mhithrha.test;

import static org.junit.Assert.fail;

import com.duanxr.mhithrha.exports.RuntimeCompiler;
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
        package com.duanxr.mhithrha.test.runtime;
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
      Class<?> compiledClass = compiler.compile(className, code);
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
        package com.duanxr.mhithrha.test.runtime;
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
      Class<?> compiledClass = compiler.compile(code);
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

}
