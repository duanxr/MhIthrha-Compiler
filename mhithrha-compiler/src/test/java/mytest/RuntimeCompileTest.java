/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mytest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.duanxr.mhithrha.exports.RuntimeCompiler;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import org.junit.Test;

public class RuntimeCompileTest {

  private static final RuntimeCompiler RUNTIME_COMPILER = new RuntimeCompiler(
      new EclipseCompiler());
  static String code = "package mytest;\n" +
      "public class Test implements IntConsumer {\n" +
      "    public void accept(int num) {\n" +
      "        if ((byte) num != num)\n" +
      "            throw new IllegalArgumentException();\n" +
      "    }\n" +
      "}\n";

  @Test
  public void outOfBounds() throws Exception {
    ClassLoader cl = new URLClassLoader(new URL[0]);
    Class<?> aClass = RUNTIME_COMPILER.compile("mytest.Test", code);
    IntConsumer consumer = (IntConsumer) aClass.getDeclaredConstructor().newInstance();
    consumer.accept(1); // ok
    try {
      consumer.accept(128); // no ok
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }

  //@Ignore("see https://teamcity.chronicle.software/viewLog.html?buildId=639347&tab=buildResultsDiv&buildTypeId=OpenHFT_BuildAll_BuildJava11compileJava11")
  @Test
  public void testMultiThread() throws Exception {
    StringBuilder largeClass = new StringBuilder("\n" +
        "package allahuaa; "
        + "public class TestAC implements mytest.IntConsumer, java.util.function.IntSupplier {\n" +
        "    static final java.util.concurrent.atomic.AtomicInteger called = new java.util.concurrent.atomic.AtomicInteger(0);\n"
        +
        "    public int getAsInt() { return called.get(); }\n" +
        "    public void accept(int num) {\n" +
        "        called.incrementAndGet();\n" +
        "    }\n");
    for (int j = 0; j < 1_000; j++) {
      largeClass.append("    public void accept" + j + "(int num) {\n" +
          "        if ((byte) num != num)\n" +
          "            throw new IllegalArgumentException();\n" +
          "    }\n");
    }
    largeClass.append("}\n");
    final String code2 = largeClass.toString();
    final ClassLoader cl = new URLClassLoader(new URL[0]);
    final int nThreads = Runtime.getRuntime().availableProcessors();
    System.out.println("nThreads = " + nThreads);
    final AtomicInteger started = new AtomicInteger(0);
    final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
    final List<Future<?>> futures = new ArrayList<>();
    for (int i = 0; i < 0; i++) {
      final int value = i;
      futures.add(executor.submit(() -> {
        started.incrementAndGet();
        while (started.get() < 1)
          ;
        try {
          Class<?> aClass = RUNTIME_COMPILER.compile("allahuaa.TestAC", code2);
          IntConsumer consumer = (IntConsumer) aClass.getDeclaredConstructor().newInstance();
          consumer.accept(value);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }));
    }
    executor.shutdown();
    for (Future<?> f : futures) {
      f.get(10000, TimeUnit.SECONDS);
    }
    Class<?> aClass = RUNTIME_COMPILER.compile("allahuaa.TestAC", code2);
    IntSupplier consumer = (IntSupplier) aClass.getDeclaredConstructor().newInstance();
    assertEquals(nThreads, consumer.getAsInt());
  }
}

