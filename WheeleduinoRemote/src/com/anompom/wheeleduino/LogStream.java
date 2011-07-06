package com.anompom.wheeleduino;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class LogStream extends OutputStream {
  private final String TAG;

  public LogStream(String tag) {
    this.TAG = tag;
  }

  public void write(int b) throws IOException {
    Log.v(TAG, Character.toString( (char) b));
  }

  public void write(int[] b) throws IOException {
    /* Do nothing! */
  }

  public void write(byte[] b, int off, int len) throws IOException {
    /* Do nothing! */
  }

  public void close() throws IOException {
    /* Do nothing! */
  }

  public void flush() throws IOException {
    /* Do nothing! */
  }
}

