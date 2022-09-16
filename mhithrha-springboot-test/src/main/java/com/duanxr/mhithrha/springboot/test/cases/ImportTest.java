package com.duanxr.mhithrha.springboot.test.cases;

import static org.junit.Assert.fail;

import com.duanxr.mhithrha.JavaSourceCode;
import com.duanxr.mhithrha.RuntimeCompiler;
import java.io.File;
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
        import com.duanxr.mhithrha.test.component.ImportClass;
        public class ImportTest1 implements Function<String,String>{
            @Override
            public String apply(String str) {
              return str+':'+new ImportClass().get();
            }
          }
        """;
    String className = "ImportTest1";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertEquals(object.apply(className), "ImportTest1:I'm ImportClass");
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

  public void testImportFromExtraJarFile() {
    String code = """
        import com.alibaba.fastjson.JSONObject;
        import java.util.function.Function;
        public class ImportTest2 implements Function<String,String>{
               @Override
               public String apply(String str) {
                 JSONObject jsonObject = new JSONObject();
                 jsonObject.put("apply", str);
                 jsonObject.put("import", true);
                 jsonObject.put("status", "working");
                 return jsonObject.toJSONString();
               }
             }
        """;
    String className = "ImportTest2";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Function<String, String> object = (Function<String, String>) compiledClass.getConstructor()
          .newInstance();
      Assert.assertEquals(object.apply(className), "{\"import\":true,\"apply\":\"ImportTest2\",\"status\":\"working\"}");
    } catch (Throwable e) {
      e.printStackTrace();
      fail();
    }
  }
}
