/*
 * Copyright 2014 Higher Frequency Trading
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

package com.duanxr.mhithrha.component;

import java.io.ByteArrayOutputStream;
import java.util.function.Consumer;

public class CallbackByteArrayOutputStream extends ByteArrayOutputStream {
  private final Consumer<CallbackByteArrayOutputStream> callback;
  public CallbackByteArrayOutputStream(Consumer<CallbackByteArrayOutputStream> callback) {
    this.callback = callback;
  }

  @Override
  public void close() {
    if (callback != null) {
      callback.accept(this);
    }
  }

}
