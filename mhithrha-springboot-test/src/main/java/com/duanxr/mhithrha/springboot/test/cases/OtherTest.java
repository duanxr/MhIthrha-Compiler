package com.duanxr.mhithrha.springboot.test.cases;

import static org.junit.Assert.fail;

import com.duanxr.mhithrha.JavaSourceCode;
import com.duanxr.mhithrha.RuntimeCompiler;
import java.io.File;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.junit.Assert;

/**
 * @author 段然 2022/9/10
 */
@AllArgsConstructor
@SuppressWarnings("unchecked")
public class OtherTest {

  private RuntimeCompiler compiler;

  public void testLombok() {
    String code = """
        import java.util.function.Consumer;
        @lombok.ToString
        public class OtherTest1 implements Consumer<String>{
                
            private String value = null;
            
            @Override
            public void accept(String str) {
              this.value = str;
            }
          }
        """;
    String className = "OtherTest1";
    try {
      Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
      Assert.assertEquals(compiledClass.getSimpleName(), className);
      Consumer<String> object = (Consumer<String>) compiledClass.getConstructor()
          .newInstance();
      object.accept("Lombok is working!");
      Assert.assertEquals("OtherTest1(value=Lombok is working!)", object.toString());
    } catch (Exception e) {
      e.printStackTrace();
      fail();
    }
  }

}
