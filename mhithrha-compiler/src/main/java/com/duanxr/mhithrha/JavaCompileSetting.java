package com.duanxr.mhithrha;

import java.io.PrintWriter;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

/**
 * @author 段然 2022/9/12
 */
@Data
@Builder
public class JavaCompileSetting {
  private PrintWriter writer;

  private List<String> options;
}
