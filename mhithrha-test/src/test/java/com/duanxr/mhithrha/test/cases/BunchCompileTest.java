package com.duanxr.mhithrha.test.cases;

import static org.junit.Assert.fail;

import com.duanxr.mhithrha.JavaSourceCode;
import com.duanxr.mhithrha.RuntimeCompiler;
import java.io.File;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
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
        package com.duanxr.mhithrha.test.runtime;
        @lombok.ToString
        public class BunchCompileTest%s{
                
            private String value = null;
            
            public BunchCompileTest()
            {
              this.value = this.getClass().getName();
            }
          }
        """;
    String className = "BunchCompileTest%s";
    try {
      for (int i = 0; i < 100; i++) {
        String codeI = String.format(code, i);
        String classNameI = String.format(className, i);
      }
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
