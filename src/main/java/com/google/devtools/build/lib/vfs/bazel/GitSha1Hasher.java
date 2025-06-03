// Copyright 2023 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.lib.vfs.bazel;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/** A {@link Hasher} for GITSHA1. */
public final class GitSha1Hasher implements Hasher {
  private final Hasher sha1Hasher = null;
  private boolean isDone = false;
  // private final MessageDigest prototype = MessageDigest.getInstance("SHA-1");

  public GitSha1Hasher() {
    // sha1Hasher = new MessageDigestHasher(prototype.getAlgorithm());
  }

  /* The following methods implement the {Hasher} interface. */

  @Override
  @CanIgnoreReturnValue
  public Hasher putBytes(ByteBuffer b) {
    byte[] arr = new byte[b.remaining()];
    b.get(arr);
    return putBytes(arr);
  }

  @Override
  @CanIgnoreReturnValue
  public Hasher putBytes(byte[] bytes, int off, int len) {
    return sha1Hasher.putBytes(bytes, off, len);
  }

  @Override
  @CanIgnoreReturnValue
  public Hasher putBytes(byte[] bytes) {
    return sha1Hasher.putBytes(bytes, 0, bytes.length);
  }

  @Override
  @CanIgnoreReturnValue
  public Hasher putByte(byte b) {
    return sha1Hasher.putByte(b);
  }

  @Override
  public HashCode hash() {
    checkState(!isDone);
    isDone = true;

    return sha1Hasher.hash();
  }

  @Override
  @CanIgnoreReturnValue
  public final Hasher putBoolean(boolean b) {
    return sha1Hasher.putBoolean(b);
  }

  @Override
  @CanIgnoreReturnValue
  public final Hasher putDouble(double d) {
    return sha1Hasher.putDouble(d);
  }

  @Override
  @CanIgnoreReturnValue
  public final Hasher putFloat(float f) {
    return sha1Hasher.putFloat(f);
  }

  @Override
  @CanIgnoreReturnValue
  public Hasher putUnencodedChars(CharSequence charSequence) {
    return sha1Hasher.putUnencodedChars(charSequence);
  }

  @Override
  @CanIgnoreReturnValue
  public Hasher putString(CharSequence charSequence, Charset charset) {
    return sha1Hasher.putString(charSequence, charset);
  }

  @Override
  @CanIgnoreReturnValue
  public Hasher putShort(short s) {
    return sha1Hasher.putShort(s);
  }

  @Override
  @CanIgnoreReturnValue
  public Hasher putInt(int i) {
    return sha1Hasher.putInt(i);
  }

  @Override
  @CanIgnoreReturnValue
  public Hasher putLong(long l) {
    return sha1Hasher.putLong(l);
  }

  @Override
  @CanIgnoreReturnValue
  public Hasher putChar(char c) {
    return sha1Hasher.putChar(c);
  }

  @Override
  @CanIgnoreReturnValue
  public <T> Hasher putObject(T instance, Funnel<? super T> funnel) {
    return sha1Hasher.putObject(instance, funnel);
  }
}
