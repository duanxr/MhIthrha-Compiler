package com.duanxr.mhithrha.test;

import com.duanxr.mhithrha.exports.RuntimeCompiler;
import org.junit.Test;

/**
 * @author 段然 2022/9/6
 */
public class TestLauncher {

  @Test
  public void t1() {
    for (int i = 0; i < 100; i++) {
      new PackageTest(RuntimeCompiler.withEclipse()).testPackageClass();
    }
  }

  @Test
  public void test() {
    RuntimeCompiler withEclipse = RuntimeCompiler.withEclipse();
    RuntimeCompiler withJdk = RuntimeCompiler.withJdk();//need jdk
    RuntimeCompiler withJavac = RuntimeCompiler.withJavac();//need jvm option: --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED

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
