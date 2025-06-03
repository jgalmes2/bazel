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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import com.google.common.hash.Funnel;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/** A {@link HashFunction} for BLAKE3. */
public final class GitSha1HashFunction implements HashFunction {
  public static final HashFunction INSTANCE = new GitSha1HashFunction();
  private static final HashFunction SHA1 = Hashing.sha1();
  private static byte header[] = {'b', 'l', 'o', 'b', ' '};

  private Hasher newInitializedHasher(int blobSize) {
    Hasher hasher = SHA1.newHasher();
    hasher.putBytes(header);
    hasher.putBytes(Integer.toString(blobSize).getBytes());
    hasher.putByte((byte) 0);
    return hasher;
  }
  
  @Override
  public int bits() {
    return 160;
  }

  @Override
  public Hasher newHasher() {
    // return new GitSha1Hasher();
    // return new MessageDigestHashFunction();
    return null;
  }

  @Override
  public Hasher newHasher(int expectedInputSize) {
    checkArgument(
        expectedInputSize >= 0, "expectedInputSize must be >= 0 but was %s", expectedInputSize);
    // return newHasher();
    return newInitializedHasher(expectedInputSize);
  }

  /* The following methods implement the {HashFunction} interface. */

  @Override
  public <T> HashCode hashObject(T instance, Funnel<? super T> funnel) {
    return newHasher().putObject(instance, funnel).hash();
  }

  @Override
  public HashCode hashUnencodedChars(CharSequence input) {
    int len = input.length();
    return newHasher(len * 2).putUnencodedChars(input).hash();
  }

  @Override
  public HashCode hashString(CharSequence input, Charset charset) {
    return newHasher().putString(input, charset).hash();
  }

  @Override
  public HashCode hashInt(int input) {
    return newHasher(4).putInt(input).hash();
  }

  @Override
  public HashCode hashLong(long input) {
    return newHasher(8).putLong(input).hash();
  }

  @Override
  public HashCode hashBytes(byte[] input) {
    return hashBytes(input, 0, input.length);
  }

  @Override
  public HashCode hashBytes(byte[] input, int off, int len) {
    checkPositionIndexes(off, off + len, input.length);
    return newHasher(len).putBytes(input, off, len).hash();
  }

  @Override
  public HashCode hashBytes(ByteBuffer input) {
    return newHasher(input.remaining()).putBytes(input).hash();
  }
}
