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
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import com.google.common.flogger.GoogleLogger;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.io.PrintWriter;
import java.io.StringWriter;
import com.google.common.hash.HashCode;

/**
 * A FileSystem that uses extended file attributes to obtain a files's digest.
 */
@ThreadSafe
public class CasFileSystem extends JavaIoFileSystem {
  // private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final Logger logger = Logger.getLogger(CasFileSystem.class.getName());
  private final DigestHashFunction hashFunction;

  public CasFileSystem(DigestHashFunction hashFunction) {
    super(hashFunction);
    this.hashFunction = hashFunction;

    try {
        // FileHandler fileHandler = new FileHandler("/src/out/cas_file_system.log", true);
        FileHandler fileHandler = new FileHandler("/tmp/cas_file_system.log", true);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
        logger.addHandler(fileHandler);
    } catch (IOException e) {
    }
  }

  public static String getStackTraceAsString(Throwable t) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    t.printStackTrace(pw);
    return sw.toString();
  }

  @Override
  protected byte[] getFastDigest(PathFragment path) throws IOException {
    logger.info("getFastDigest: " + path);
    byte[] hexDigest = getDigest(path);
    int len = hexDigest.length;
    if (len == 41) {
        byte[] binDigest = new byte[len / 2];
        for (int i = 0; i < len - 1; i += 2) {
            binDigest[i / 2] = (byte) (
                (Character.digit(hexDigest[i], 16) << 4) +
                Character.digit(hexDigest[i+1], 16));
            
        }
        return binDigest;
    } else {
        return hexDigest;
    }
  }

  @Override
  protected byte[] getDigest(PathFragment path) throws IOException {
    logger.info("getDigest: path " + path);
    String name = path.toString();
    long startTime = Profiler.nanoTimeMaybe();

    try {
        UserDefinedFileAttributeView userAttributes =
                Files.getFileAttributeView(getNioPath(path),
                        UserDefinedFileAttributeView.class);
        if (userAttributes != null) {
            String attributeName = "casfs_hash";
            int size = userAttributes.size(attributeName);
            ByteBuffer buffer = ByteBuffer.allocate(size);
            userAttributes.read(attributeName, buffer);
            buffer.flip();

            Throwable t = new Throwable();
            logger.info("getDigest:" + getStackTraceAsString(t));

            // logger.atInfo().log(
            //     "getDigest(%s) = %s",
            //     getNioPath(path),
            //     Charset.defaultCharset().decode(buffer).toString());
            logger.info("getDigest: size: " + size);
            logger.info(
                "getDigest: " +
                getNioPath(path) + " " +
                Charset.defaultCharset().decode(buffer).toString());

            return buffer.array();
        } else {
            // logger.atSevere().log(
            //     "getFileAtrributeView(%s) failed",
            //     getNioPath(path));
            logger.info(
                "getFileAtrributeView(%s) failed: " +
                getNioPath(path));
        }
    } catch (Exception e) {
        logger.warning(e.toString());
    }

    InputStream is = null;

    try {
        byte[] fileData = new byte[(int) getFileSize(path, true)];
        is = Files.newInputStream(getNioPath(path));
        is.read(fileData);
        return hashFunction.getHashFunction().hashBytes(fileData).asBytes();
    } finally {
        if (is != null) {
            is.close();
        }
        profiler.logSimpleTask(startTime, ProfilerTask.VFS_MD5, name);
    }
  }
}
