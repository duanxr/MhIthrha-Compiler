package com.duanxr.mhithrha.test;

import com.duanxr.mhithrha.RuntimeCompiler;
import com.google.common.base.Strings;
import lombok.SneakyThrows;
import org.junit.Test;

/**
 * @author 段然 2022/9/6
 */
public class TestLauncher {

  @Test
  @SneakyThrows
  public void test() {
    RuntimeCompiler withEclipse = RuntimeCompiler.withEclipseCompiler();
    RuntimeCompiler withJavac = RuntimeCompiler.withJavacCompiler();//requires jvm option: --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
    RuntimeCompiler withJdk = RuntimeCompiler.withJdkCompiler();//requires jdk
    withEclipse.addModule(this.getClass().getModule());
    new ReferenceTest(withEclipse).testReferenceMaven();
    if(true)return;

    doTest(withEclipse);
    doTest(withJavac);
    doTest(withJdk);
  }

  private void doTest(RuntimeCompiler compiler) {
    doTest(new SimpleTest(compiler));
    doTest(new ReferenceTest(compiler));
    doTest(new JavaLevelTest(compiler));
    doTest(new PackageTest(compiler));
  }

  private void doTest(PackageTest packageTest) {
    packageTest.testPackageClass();
    packageTest.testPackageClassWithoutName();
    //packageTest.testPackageAccessClassFromAnotherClassLoader();
    packageTest.testPackageAccessClass();
  }

  private void doTest(ReferenceTest referenceTest) {
    referenceTest.testReferenceJava();
    referenceTest.testReferenceInner();
    referenceTest.testReferenceMaven();
    referenceTest.testReferenceCustom();
    referenceTest.testReferenceAnother();
    referenceTest.testReferenceEachOther();
  }

  private void doTest(JavaLevelTest javaLevelTest) {
    javaLevelTest.testJava8();
    javaLevelTest.testJava11();
    javaLevelTest.testJava17();
  }

  private void doTest(SimpleTest simpleTest) {
    simpleTest.testClass();
    simpleTest.testClassWithoutName();
    simpleTest.testClassWithInterface();
    simpleTest.testClassWithInterfaceWithoutName();
    simpleTest.testClassWithExceptionInterface();
    simpleTest.testClassWithExceptionInterfaceWithoutName();
  }
}
