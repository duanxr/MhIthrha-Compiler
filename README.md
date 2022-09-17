![](mhithrha.png)

# MhIthrha-Compiler

[README](README.md) | [中文文档](README_zh.md)

Mh'Ithrha Compiler is a Java runtime compiler, it allows you to compile Java source code and then load the class at runtime.

It support Java 17 and you can use it without `JDK`. 

You can compile multiple classes at a time in case they depend on each other.

You can also add external `.class` and `.jar` file as compilation dependencies at runtime.

It also supports loading `.class` and `.jar` file from fat jar from `Spring Boot Maven Plugin`.

## On Maven

You can include in your project with

```xml
<dependency>
  <groupId>com.duanxr</groupId>
  <artifactId>mhithrha-compiler</artifactId>
  <version>1.0.0</version>
</dependency>
```

## Simple example

```java
 RuntimeCompiler compiler = RuntimeCompiler.builder().withEclipseCompiler();
 String code = """
        public class SimpleClass implements java.util.function.Supplier<String>{
          @Override
          public String get() {
            return "Hello World";
          }
        }
        """;
 Class<?> compiledClass = compiler.compile(JavaSourceCode.of(className, code));
 Supplier<String> simpleClass = (Supplier<String>) compiledClass.getConstructor().newInstance();
 System.out.println(simpleClass.get());
```

## Create a compiler

Mh'Ithrha Compiler is based on the base java compiler, which there are three default options:

1. [Eclipse Compiler](https://www.eclipsecon.org/session-tags/eclipse-java-compiler-ecj)
2. [Javac Compiler](https://docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html)
3. [JDK Compiler](https://en.wikipedia.org/wiki/Java_Development_Kit)

You can use compiler builder methods to choose.

```java
    RuntimeCompiler withEclipseCompiler = RuntimeCompiler.builder().withEclipseCompiler();
    RuntimeCompiler withJavacCompiler = RuntimeCompiler.builder().withJavacCompiler();
    RuntimeCompiler withJdkCompiler = RuntimeCompiler.builder().withJdkCompiler();
    RuntimeCompiler withCustomCompiler = RuntimeCompiler.builder().withCustomCompiler(YourCompiler);
```

We recommend using ``Eclipse Compiler`` for you can't make sure the program runs on ``JDK`` and you have to add ``tools.jar`` and jvm arg ``--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED`` to use ``Javac Compiler`` due to [JEP 403](https://openjdk.org/jeps/403).

## Add compilation dependencies
There are several ways to resolve class dependencies for compilation:

1. Inherit class path, if your class that runs the compiler can find some class, so do the compiling class.
2. Load SpringBoot class path, the first way doesn't work for fat jar from `Spring Boot Maven Plugin`, you need to call ``loadSpringBootArchives()`` to load SpringBoot archives manually.
3. Compiled class, all compiling classes can access all compiled classes in the same compiler, If you want to compile two classes that depend on each other, compile them together with a list of ``JavaSourceCode``;
4. External class, you can add external dependencies with ``addExtraClass()`` and ``addExtraArchive()``.

```java
compiler.loadSpringBootArchives();
compiler.addExtraClass(new File("../mhithrha-test/src/test/resources/ImportClass.class"));
compiler.addExtraArchive(new File("../mhithrha-test/src/test/resources/lombok-1.18.24.jar"));
```

## Lombok support

In order to support lombok for compilation, you need to:
1. Eclipse Compiler :  add jvm arg ``-javaagent:\<PATH\>/lombok.jar=ECJ`` to your program.
2. Javac&JDK Compiler : add ``lombok.jar`` to class path with ``compiler.addExtraArchive(new File("\<PATH\>/lombok.jar"));``
For more information see [here](https://projectlombok.org/setup/)

## More usage

See [test cases](mhithrha-test\src\test\java\com\duanxr\mhithrha\test)

## Contributing

Interested in getting involved? We would like to help you!

- Take a look at our [issues](https://github.com/duanxr/MhIthrha-Compiler/issues) list and consider sending a Pull Request.
- If you want to add a new feature, please create an issue first to describe the new feature, as well as the implementation approach. Once a proposal is accepted, create an implementation of the new features and submit it as a pull request.
- Sorry for my poor English. Improvements for this document are welcome, even some typo fixes.
- If you have great ideas, send an email to admin@duanxr.com .

- Note: We prefer you to give your advise in issues, so others with a same question can search it quickly and we don't need to answer them repeatedly.


## Thanks
This project was developed based on the following projects:
- [Eclipse-Java-Compiler](https://www.eclipsecon.org/session-tags/eclipse-java-compiler-ecj)
- [Java-Runtime-Compiler](https://github.com/OpenHFT/Java-Runtime-Compiler)
