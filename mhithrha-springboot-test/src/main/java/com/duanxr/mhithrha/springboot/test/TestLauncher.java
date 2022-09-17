package com.duanxr.mhithrha.springboot.test;

import com.duanxr.mhithrha.RuntimeCompiler;
import com.duanxr.mhithrha.RuntimeCompiler.Builder;
import com.duanxr.mhithrha.springboot.test.cases.BunchCompileTest;
import com.duanxr.mhithrha.springboot.test.cases.ImportTest;
import com.duanxr.mhithrha.springboot.test.cases.JavaLevelTest;
import com.duanxr.mhithrha.springboot.test.cases.OtherTest;
import com.duanxr.mhithrha.springboot.test.cases.PackageTest;
import com.duanxr.mhithrha.springboot.test.cases.ReferenceTest;
import com.duanxr.mhithrha.springboot.test.cases.SimpleTest;
import com.duanxr.mhithrha.springboot.test.cases.TheadSafeTest;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import javax.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

/**
 * @author 段然 2022/9/6
 */
@Component
public class TestLauncher {

  /**
   * Add jvm arg "-javaagent:src/test/resources/lombok-1.18.24.jar=ECJ" to enable lombok support for
   * EclipseCompiler. Add jvm arg "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED" to
   * load Javac compiler
   */
  @PostConstruct
  public void test() {
    try {
      //testWithEclipseCompiler();
      testWithJdkCompiler();
      testWithJavacCompiler();
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @SneakyThrows
  private void testWithJdkCompiler() {
    testWithCustomClassLoader(Builder::withJdkCompiler);
    testWithDefaultClassLoader(Builder::withJdkCompiler);
  }

  @SneakyThrows
  private void testWithJavacCompiler() {
    testWithCustomClassLoader(Builder::withJavacCompiler);
    testWithDefaultClassLoader(Builder::withJavacCompiler);
  }

  @SneakyThrows
  private void testWithEclipseCompiler() {
    testWithCustomClassLoader(Builder::withEclipseCompiler);
    testWithDefaultClassLoader(Builder::withEclipseCompiler);
  }

  private void testWithDefaultClassLoader(
      Function<Builder, RuntimeCompiler> compilerBuildFunction) {
    testStandaloneMode(compilerBuildFunction, Thread.currentThread().getContextClassLoader());
  }

  private void testWithCustomClassLoader(Function<Builder, RuntimeCompiler> compilerBuildFunction) {
    testStandaloneMode(compilerBuildFunction, new ClassLoader(Thread.currentThread().getContextClassLoader()) {
    });
  }

  public void testStandaloneMode(Function<Builder, RuntimeCompiler> compilerBuildFunction,
      ClassLoader classLoader) {
    doTest(compilerBuildFunction.apply(RuntimeCompiler.builder().withCharset(StandardCharsets.UTF_8).withClassLoader(classLoader)));
  }


  private void doTest(RuntimeCompiler compiler) {
    compiler.loadSpringBootArchives();
    compiler.addExtraClass(new File("../mhithrha-test/src/test/resources/ImportClass.class"));
    compiler.addExtraArchive(new File("../mhithrha-test/src/test/resources/lombok-1.18.24.jar"));
    compiler.addExtraArchive(new File("../mhithrha-test/src/test/resources/fastjson-1.2.83.jar"));
    doTest(new SimpleTest(compiler));
    doTest(new ReferenceTest(compiler));
    doTest(new JavaLevelTest(compiler));
    doTest(new PackageTest(compiler));
    doTest(new ImportTest(compiler));
    doTest(new OtherTest(compiler));
    doTest(new BunchCompileTest(compiler));
    doTest(new TheadSafeTest(compiler));
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
