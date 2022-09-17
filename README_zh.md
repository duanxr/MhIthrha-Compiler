![](mhithrha.png)

# MhIthrha-Compiler

[README](README.md) | [中文文档](README_zh.md)

Mh'Ithrha编译器是一个Java运行时编译器，它允许你在运行时编译Java源代码然后加载类。
它支持Java 17，你可以在没有`JDK`的情况下使用它。
你可以一次编译多个类，以防它们相互依赖。
你也可以在运行时添加外部的`.class`和`.jar`文件作为编译依赖项。
它还支持从`Spring Boot Maven Plugin`的 fat jar 中加载`.class`和`.jar`文件作为编译依赖项。

## 通过Maven导入

你可以通过Maven在你的项目中添加依赖

```xml
<dependency>
  <groupId>com.duanxr</groupId>
  <artifactId>mhithrha-compiler</artifactId>
  <version>1.0.0</version>
</dependency>
```


## 简单示例
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

## 创建编译器

Mh'Ithrha编译器基于基础java编译器，其中有三个默认选项。

1. [Eclipse Compiler](https://wiki.eclipse.org/JDT_Core_Programmer_Guide/ECJ)
2. [Javac Compiler](https://docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html)
3. [JDK Compiler](https://en.wikipedia.org/wiki/Java_Development_Kit)

你可以使用编译器构建方法来选择。

```java
    RuntimeCompiler withEclipseCompiler = RuntimeCompiler.builder().withEclipseCompiler();
    RuntimeCompiler withJavacCompiler = RuntimeCompiler.builder().withJavacCompiler();
    RuntimeCompiler withJdkCompiler = RuntimeCompiler.builder().withJdkCompiler();
    RuntimeCompiler withCustomCompiler = RuntimeCompiler.builder().withCustomCompiler(YourCompiler);
```

我们推荐使用``Eclipse Compiler``，因为你不能确保程序在``JDK``上运行，而且你必须添加``tools.jar``和jvm参数``--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED``来使用``Javac Compiler``，因为[JEP 403]（https://openjdk.org/jeps/403）。

## 添加编译依赖项
有几种方法可以解决编译时的类依赖关系。
1. 继承类的路径，如果你的运行编译器的类能找到某个类，编译中的类也能找到。
2. 加载SpringBoot类路径，第一种方式对来自`Spring Boot Maven Plugin`的fat jar不起作用，你需要调用`loadSpringBootArchives()`来手动加载SpringBoot依赖。
3. 编译过的类，所有编译中的类都可以访问同一个编译器中的所有编译过的类，如果你想编译两个相互依赖的类，就用`JavaSourceCode`的``List``一起编译。
4. 外部的类，你可以用``addExtraClass()``和``addExtraArchive()``来增加外部编译依赖项。

```java
compiler.loadSpringBootArchives();
compiler.addExtraClass(new File("../mhithrha-test/src/test/resources/ImportClass.class"));
compiler.addExtraArchive(new File("../mhithrha-test/src/test/resources/lombok-1.18.24.jar"));
```
## Lombok支持

为了支持lombok的编译，你需要:
1. Eclipse Compiler :  添加jvm参数``-javaagent:\<PATH\>/lombok.jar=ECJ``。
2. Javac&JDK Compiler : 用``compiler.addExtraArchive(new File("<PATH\>/lombok.jar"))``将``lombok.jar``添加到类路径中。

更多信息见[这里](https://projectlombok.org/setup/)


## 更多用法

见[测试用例](mhithrha-test\src\test\java\com\duanxr\mhithrha\test)

## 贡献

这是一个免费且开源的项目，我们欢迎任何人为其开发和进步贡献力量。

* 在使用过程中出现任何问题，可以通过 [issues](https://github.com/duanxr/MhIthrha-Compiler/issues) 来反馈。
* Bug 的修复可以直接提交 Pull Request 。
* 如果是增加新的功能特性，请先创建一个 issue 并做简单描述以及大致的实现方法，提议被采纳后，就可以创建一个实现新特性的 Pull Request。
* 欢迎对说明文档做出改善。
* 如果你有任何其他方面的问题或，欢迎发送邮件至 admin@duanxr.com 。


## 感谢
这个项目是在以下项目的基础上开发的:
- [Eclipse-Java-Compiler](https://www.eclipsecon.org/session-tags/eclipse-java-compiler-ecj)
- [Java-Runtime-Compiler](https://github.com/OpenHFT/Java-Runtime-Compiler)
