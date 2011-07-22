/**
 * User: ivan
 * Date: 7-Feb-2005
 * Time: 2:20:24 PM
 */

package org.pcl.parser.tools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomBufferedInputStream extends BufferedInputStream {

  /**
   * File offset
   */
  protected long offset = -1;

  public long getOffset() {
    return offset;
  }

  /**
   * See
   * the general contract of the <code>read</code>
   * method of <code>InputStream</code>.
   *
   * @return the next byte of data, or <code>-1</code> if the end of the
   *         stream is reached.
   * @throws java.io.IOException if an I/O error occurs.
   * @see java.io.FilterInputStream#in
   */
  @Override
  public synchronized int read() throws IOException {
    int nextByte = super.read();
    if (-1 != nextByte) offset++;
    return nextByte;
  }

  /**
   * Reads up to <code>byte.length</code> bytes of data from this
   * input stream into an array of bytes. This method blocks until some
   * input is available.
   * <p/>
   * This method simply performs the call
   * <code>read(b, 0, b.length)</code> and returns
   * the  result. It is important that it does
   * <i>not</i> do <code>in.read(b)</code> instead;
   * certain subclasses of  <code>FilterInputStream</code>
   * depend on the implementation strategy actually
   * used.
   *
   * @param b the buffer into which the data is read.
   * @return the total number of bytes read into the buffer, or
   *         <code>-1</code> if there is no more data because the end of
   *         the stream has been reached.
   * @throws java.io.IOException if an I/O error occurs.
   * @see java.io.FilterInputStream#read(byte[], int, int)
   */
  @Override
  public int read(byte b[]) throws IOException {
    int read = super.read(b);
    if (read != -1) offset += read;
    return read;
  }

  /**
   * Creates a <code>BufferedInputStream</code>
   * and saves its  argument, the input stream
   * <code>in</code>, for later use. An internal
   * buffer array is created and  stored in <code>buf</code>.
   *
   * @param in the underlying input stream.
   */
  public CustomBufferedInputStream(InputStream in) {
    super(in);
  }

  /**
   * Creates a <code>BufferedInputStream</code>
   * with the specified buffer size,
   * and saves its  argument, the input stream
   * <code>in</code>, for later use.  An internal
   * buffer array of length  <code>size</code>
   * is created and stored in <code>buf</code>.
   *
   * @param in   the underlying input stream.
   * @param size the buffer size.
   * @throws IllegalArgumentException if size <= 0.
   */
  public CustomBufferedInputStream(InputStream in, int size) {
    super(in, size);
  }


}
