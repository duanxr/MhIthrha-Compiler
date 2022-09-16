package com.duanxr.mhithrha.springboot.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author 段然 2022/9/16
 */
@SpringBootApplication(scanBasePackages = {"com.duanxr.mhithrha.springboot.test"})
public class SpringBootApp {

  public static void main(String[] args) {
    try {
      SpringApplication.run(SpringBootApp.class, args);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
