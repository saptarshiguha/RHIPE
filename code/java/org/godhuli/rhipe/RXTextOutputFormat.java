/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.godhuli.rhipe;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FSDataOutputStream;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.util.*;





public class RXTextOutputFormat extends TextOutputFormat<RHBytesWritable,RHBytesWritable> {

  static class RXTextRecordWriter extends RecordWriter<RHBytesWritable,RHBytesWritable> {
    private static final byte[] newLine = "\r\n".getBytes();
    private static  byte[] keyvaluesep = " ".getBytes();
    // private static  byte[] fsep = " ".getBytes();
    private static final String utf8 = "UTF-8";
    protected DataOutputStream out;

    public RXTextRecordWriter(DataOutputStream out,
			      String keyValueSeparator,String fieldSep) {
	this.out=out;
	try{
	    keyvaluesep =keyValueSeparator.getBytes(utf8);;
	    RHBytesWritable.setFieldSep(fieldSep);
	}catch (UnsupportedEncodingException uee) {
	    throw new IllegalArgumentException("can't find " + utf8 + " encoding");
	}

    }

    public synchronized void write(RHBytesWritable key, 
                                   RHBytesWritable value) throws IOException {
	    out.write(key.writeAsString().getBytes(utf8));
	    out.write(keyvaluesep);
	    out.write(value.writeAsString().getBytes(utf8));
	    out.write(newLine, 0, newLine.length);
	}
 

    public synchronized 
	void close(TaskAttemptContext context) throws IOException {
	    out.close();
    }


  }


  public RecordWriter<RHBytesWritable,RHBytesWritable>
      getRecordWriter(TaskAttemptContext job
		      ) throws IOException, InterruptedException {
      Configuration conf = job.getConfiguration();
      boolean isCompressed = getCompressOutput(job);
      String keyValueSeparator= conf.get("mapred.textoutputformat.separator",
					 "\t");
      String fieldSeparator= conf.get("mapred.field.separator",
				    " ");
      CompressionCodec codec = null;
      String extension = "";
      if (isCompressed) {
	  Class<? extends CompressionCodec> codecClass = 
	      getOutputCompressorClass(job, GzipCodec.class);
	  codec = (CompressionCodec) ReflectionUtils.newInstance(codecClass, conf);
	  extension = codec.getDefaultExtension();
      }
      Path file = getDefaultWorkFile(job, extension);
      FileSystem fs = file.getFileSystem(conf);
      if (!isCompressed) {
	  FSDataOutputStream fileOut = fs.create(file, false);
	  return new RXTextRecordWriter(fileOut, keyValueSeparator,fieldSeparator);
      } else {
	  FSDataOutputStream fileOut = fs.create(file, false);
	  return new RXTextRecordWriter(new DataOutputStream
					    (codec.createOutputStream(fileOut)),
					keyValueSeparator,fieldSeparator);
    }

  }
}