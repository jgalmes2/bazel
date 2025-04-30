// Copyright 2014 The Bazel Authors. All rights reserved.
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
package com.google.devtools.build.lib.vfs;

import com.google.devtools.build.lib.concurrent.ThreadSafety.ThreadSafe;
import com.google.devtools.build.lib.profiler.Profiler;
import com.google.devtools.build.lib.profiler.ProfilerTask;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;

/**
 * A FileSystem that uses extended file attributes to obtain a files's digest.
 */
@ThreadSafe
public class CasFileSystem extends JavaIoFileSystem {
  public CasFileSystem(DigestHashFunction hashFunction) {
    super(hashFunction);
  }

  @Override
  protected byte[] getDigest(PathFragment path) throws IOException {
    String name = path.toString();
    long startTime = Profiler.nanoTimeMaybe();

    UserDefinedFileAttributeView userAttributes =
            Files.getFileAttributeView(getNioPath(path),
                    UserDefinedFileAttributeView.class);
    if (userAttributes != null) {
        String attributeName = "casfs_hash";
        int size = userAttributes.size(attributeName);
        ByteBuffer buffer = ByteBuffer.allocate(size);
        userAttributes.read(attributeName, buffer);
        buffer.flip();
        return buffer.array();
    } else {
        try {
            return super.getDigest(path);
        } finally {
            profiler.logSimpleTask(startTime, ProfilerTask.VFS_MD5, name);
        }
    }
  }
}
