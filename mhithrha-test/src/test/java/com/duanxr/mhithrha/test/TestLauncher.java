package com.duanxr.mhithrha.test;

import com.duanxr.mhithrha.RuntimeCompiler;
import com.duanxr.mhithrha.RuntimeCompiler.Builder;
import com.duanxr.mhithrha.test.cases.BunchCompileTest;
import com.duanxr.mhithrha.test.cases.ImportTest;
import com.duanxr.mhithrha.test.cases.JavaLevelTest;
import com.duanxr.mhithrha.test.cases.OtherTest;
import com.duanxr.mhithrha.test.cases.PackageTest;
import com.duanxr.mhithrha.test.cases.ReferenceTest;
import com.duanxr.mhithrha.test.cases.SimpleTest;
import com.duanxr.mhithrha.test.cases.TheadSafeTest;
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
    testWithCustomClassLoader(Builder::withJdkCompiler);
    testWithDefaultClassLoader(Builder::withJdkCompiler);
  }

  /**
   * Add jvm arg "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED" to load Javac
   * compiler
   */
  @Test
  @SneakyThrows
  public void testWithJavacCompiler() {
    testWithCustomClassLoader(Builder::withJavacCompiler);
    testWithDefaultClassLoader(Builder::withJavacCompiler);
  }

  /**
   * add jvm arg "-javaagent:src/test/resources/lombok-1.18.24.jar" to enable lombok support
   */
  @Test
  @SneakyThrows
  public void testWithEclipseCompiler() {
    testWithCustomClassLoader(Builder::withEclipseCompiler);
    testWithDefaultClassLoader(Builder::withEclipseCompiler);
  }

  private void testWithDefaultClassLoader(Function<Builder, RuntimeCompiler> compilerBuildFunction) {
    testStandaloneMode(compilerBuildFunction, Thread.currentThread().getContextClassLoader());
    testIntrusiveMode(compilerBuildFunction, Thread.currentThread().getContextClassLoader());
  }

  private void testWithCustomClassLoader(Function<Builder, RuntimeCompiler> compilerBuildFunction) {
    testStandaloneMode(compilerBuildFunction, new ClassLoader() {
    });
    testIntrusiveMode(compilerBuildFunction, new ClassLoader() {
    });
  }

  public void testStandaloneMode(Function<Builder, RuntimeCompiler> compilerBuildFunction,
      ClassLoader classLoader) {
    doTest(compilerBuildFunction.apply(
        RuntimeCompiler.builder().withClassLoader(classLoader).intrusive(false)));
  }

  public void testIntrusiveMode(Function<Builder, RuntimeCompiler> compilerBuildFunction,
      ClassLoader classLoader) {
    //doTest(compilerBuildFunction.apply(RuntimeCompiler.builder().withClassLoader(classLoader).intrusive(true)));
  }

  private void doTest(RuntimeCompiler compiler) {
    doTest(new SimpleTest(compiler));
    doTest(new ReferenceTest(compiler));
    doTest(new JavaLevelTest(compiler));
    doTest(new PackageTest(compiler));
    doTest(new ImportTest(compiler));
    doTest(new BunchCompileTest(compiler));
    doTest(new TheadSafeTest(compiler));
    doTest(new OtherTest(compiler));
  }

  private void doTest(BunchCompileTest bunchCompileTest) {
    bunchCompileTest.testBunchCompile();
    bunchCompileTest.testBunchCompileWithDependency();
    bunchCompileTest.testBunchCompileWithDependencyLoop();
  }

  private void doTest(TheadSafeTest theadSafeTest) {
    theadSafeTest.testSimpleConcurrencySafe();
    theadSafeTest.testSimpleConcurrencySafe();
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
