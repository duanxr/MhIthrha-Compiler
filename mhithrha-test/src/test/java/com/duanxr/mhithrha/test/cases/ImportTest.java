package com.duanxr.mhithrha.test.cases;

import static org.junit.Assert.fail;

import com.duanxr.mhithrha.RuntimeCompiler;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.junit.Assert;

/**
 * @author 段然 2022/9/10
 */
@AllArgsConstructor
@SuppressWarnings("unchecked")
public class ImportTest {
  private RuntimeCompiler compiler;

  public void testImportFromExtraClassFile() {
    String code = """
        import java.util.function.Function;
        public class ImportTest1 implements Function<String,String>{
            @Override
            public String apply(String str) {
              return new CustomClass().get(str);
            }
          }
        """;
    String className = "ImportTest1";
    try {
      Class<?> compiledClass = compiler.compile(className, code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertEquals(object.apply(className), "custom:ReferenceTestClass4");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testImportFromExtraJarFile() {
    String code = """
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
      Class<?> compiledClass = compiler.compile(className, code);
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertEquals(object.apply(className), "custom:ReferenceTestClass4");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }
}
