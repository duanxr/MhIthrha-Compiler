package com.duanxr.mhithrha.test;

import com.duanxr.mhithrha.RuntimeCompiler;
import com.duanxr.mhithrha.RuntimeCompiler.Builder;
import com.duanxr.mhithrha.test.cases.ImportTest;
import com.duanxr.mhithrha.test.cases.JavaLevelTest;
import com.duanxr.mhithrha.test.cases.OtherTest;
import com.duanxr.mhithrha.test.cases.PackageTest;
import com.duanxr.mhithrha.test.cases.ReferenceTest;
import com.duanxr.mhithrha.test.cases.SimpleTest;
import java.util.function.Function;
import lombok.SneakyThrows;
import org.junit.Test;

/**
 * @author 段然 2022/9/6
 *
 * Only test one at a time for IntrusiveClassLoader define class on current ClassLoader which cause
 * LinkageError when define same class again.
 *
 * Add jvm arg "--add-opens=java.base/java.lang=ALL-UNNAMED" to enable intrusively define class
 */
public class TestLauncher {

  @Test
  @SneakyThrows
  public void testWithJdkCompiler() {
    testCustomClassLoader(Builder::withJdkCompiler);
    testDefaultClassLoader(Builder::withJdkCompiler);
  }

  /**
   * Add jvm arg "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED" to load Javac
   * compiler
   */
  @Test
  @SneakyThrows
  public void testWithJavacCompiler() {
    testCustomClassLoader(Builder::withJavacCompiler);
    testDefaultClassLoader(Builder::withJavacCompiler);
  }

  /**
   * add jvm arg "-javaagent:src/test/resources/lombok-1.18.24.jar" to enable lombok support
   */
  @Test
  @SneakyThrows
  public void testWithEclipseCompiler() {
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
    //doTest(compilerBuildFunction.apply(RuntimeCompiler.builder().withClassLoader(classLoader).intrusive(true)));
  }

  private void doTest(RuntimeCompiler compiler) {
    doTest(new SimpleTest(compiler));//todo interface abstract
    doTest(new ReferenceTest(compiler));
    doTest(new JavaLevelTest(compiler));
    doTest(new PackageTest(compiler));
    doTest(new ImportTest(compiler));
    doTest(new OtherTest(compiler));
  }

  private void doTest(OtherTest otherTest) {
    otherTest.testLombok();
  }

  private void doTest(ImportTest importTest) {
    importTest.testImportFromExtraClassFile();
    importTest.testImportFromExtraJarFile();
  }

  private void doTest(PackageTest packageTest) {
    packageTest.testPackageClass();
    packageTest.testPackageClassWithoutName();
    packageTest.testPackageAccessCompiledClass();
    packageTest.testPackageAccessClassFromAnotherClassLoader();
    packageTest.testNonAsciiPackageClass();
    packageTest.testNonAsciiPackageClassWithoutName();
  }

  private void doTest(ReferenceTest referenceTest) {
    referenceTest.testReferenceJavaBase();
    referenceTest.testReferenceInnerClass();
    referenceTest.testReferenceMavenDependency();
    referenceTest.testReferenceParentClassLoaderLoadedClass();
    referenceTest.testReferenceCompiledClass();
    referenceTest.testReferenceEachOtherWithOneCompilation();
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
    simpleTest.testClassWithCatchingException();
    simpleTest.testClassWithCatchingExceptionWithoutName();
    simpleTest.testClassWithAnnotation();
    simpleTest.testInterface();
    simpleTest.testAbstractClass();
    simpleTest.testNonAsciiClass();
    simpleTest.testNonAsciiClassWithoutName();
  }
}
