/**
 * @author 段然 2022/9/6
 */
module com.duanxr.mhithrha {
  exports com.duanxr.mhithrha;
  requires lombok;
    requires java.compiler;
    requires ecj;
    requires org.slf4j;
    requires com.google.common;
    requires spring.boot.loader;
    requires jdk.unsupported;
    requires jdk.compiler;
    requires com.github.benmanes.caffeine;
  requires java.management;
}