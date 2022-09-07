package com.duanxr.mhithrha.test;

import com.duanxr.mhithrha.RuntimeCompiler;
import org.junit.Test;

/**
 * @author 段然 2022/9/6
 */
public class TestLauncher {

  @Test
  public void t1() {
    for (int i = 0; i < 100; i++) {
      new PackageTest(RuntimeCompiler.withEclipseCompiler()).testPackageClass();
    }
  }

  @Test
  public void test() {
    Module module = this.getClass().getModule();
    RuntimeCompiler withEclipse = RuntimeCompiler.withEclipseCompiler();
    RuntimeCompiler withJdk = RuntimeCompiler.withJdkCompiler();//need jdk
    RuntimeCompiler withJavac = RuntimeCompiler.withJavacCompiler();//need jvm option: --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED

    withEclipse.addModule(module);
    withJdk.addModule(module);
    withJavac.addModule(module);

    new ReferenceTest(withEclipse).testReferenceMaven();

    doTest(new SimpleTest(withEclipse));
    doTest(new SimpleTest(withJdk));
    doTest(new SimpleTest(withJavac));

    doTest(new ReferenceTest(withEclipse));
    doTest(new ReferenceTest(withJdk));
    doTest(new ReferenceTest(withJavac));

    doTest(new JavaLevelTest(withEclipse));
    doTest(new JavaLevelTest(withJdk));
    doTest(new JavaLevelTest(withJavac));

    doTest(new PackageTest(withEclipse));
    doTest(new PackageTest(withJdk));
    doTest(new PackageTest(withJavac));
  }

  private void doTest(PackageTest packageTest) {
    packageTest.testPackageClass();
    packageTest.testPackageClassWithoutName();
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
