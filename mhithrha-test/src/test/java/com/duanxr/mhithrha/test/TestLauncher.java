package com.duanxr.mhithrha.test;

import com.duanxr.mhithrha.RuntimeCompiler;
import com.duanxr.mhithrha.RuntimeCompiler.Builder;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.junit.Test;

/**
 * @author 段然 2022/9/6
 */
public class TestLauncher {

  @Test
  @SneakyThrows
  public void test() {
    //Only test one at a time for IntrusiveClassLoader define class on current ClassLoader which cause LinkageError when define same class again.
    //add jvm arg "--add-opens=java.base/java.lang=ALL-UNNAMED" to enable intrusively define class
    //testWithEclipseCompiler();
    //testWithJavacCompiler();
    testWithJdkCompiler();
  }

  private void testWithJdkCompiler() {
    testCustomClassLoader(Builder::withJdkCompiler);
    testDefaultClassLoader(Builder::withJdkCompiler);
  }

  private void testWithJavacCompiler() {
    testCustomClassLoader(Builder::withJavacCompiler);
    testDefaultClassLoader(Builder::withJavacCompiler);
  }

  private void testWithEclipseCompiler() {
    testCustomClassLoader(Builder::withEclipseCompiler);
    testDefaultClassLoader(Builder::withEclipseCompiler);
  }

  private void testDefaultClassLoader(Function<Builder, RuntimeCompiler> compilerBuildFunction) {
    testStandalone(compilerBuildFunction, Thread.currentThread().getContextClassLoader());
    testIntrusive(compilerBuildFunction, Thread.currentThread().getContextClassLoader());
  }

  private void testCustomClassLoader(Function<Builder, RuntimeCompiler> compilerBuildFunction) {
    testStandalone(compilerBuildFunction, new ClassLoader() {
    });
    testIntrusive(compilerBuildFunction, new ClassLoader() {
    });
  }

  public void testStandalone(Function<Builder, RuntimeCompiler> compilerBuildFunction,
      ClassLoader classLoader) {
    doTest(compilerBuildFunction.apply(
        RuntimeCompiler.builder().withClassLoader(classLoader).intrusive(false)));
  }

  public void testIntrusive(Function<Builder, RuntimeCompiler> compilerBuildFunction,
      ClassLoader classLoader) {
    doTest(compilerBuildFunction.apply(
        RuntimeCompiler.builder().withClassLoader(classLoader).intrusive(true)));
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
    packageTest.testPackageAccessCompiledClass();
    packageTest.testPackageAccessClassFromAnotherClassLoader();
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
