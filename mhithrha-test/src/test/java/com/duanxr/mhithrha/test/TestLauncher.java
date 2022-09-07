package com.duanxr.mhithrha.test;

import com.duanxr.mhithrha.RuntimeCompiler;
import com.duanxr.mhithrha.loader.StandaloneClassLoader;
import lombok.SneakyThrows;
import org.junit.Test;

/**
 * @author 段然 2022/9/6
 */
public class TestLauncher {

  @Test
  @SneakyThrows
  public void test() {
    ClassLoader classLoader = this.getClass().getClassLoader();
    Class<?> aClass = classLoader.loadClass("com.duanxr.mhithrha.test.component.PackageClass");

    ClassLoader classLoader1 = new StandaloneClassLoader(classLoader);
    Class<?> aClass1 = classLoader1.loadClass("com.duanxr.mhithrha.test.component.PackageClass");

    RuntimeCompiler withEclipse = RuntimeCompiler.withEclipseCompiler();
    RuntimeCompiler withJdk = RuntimeCompiler.withJdkCompiler();//requires jdk
    RuntimeCompiler withJavac = RuntimeCompiler.withJavacCompiler();//requires jvm option: --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED

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
    //TODO packageTest.testPackageAccessClass();
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
