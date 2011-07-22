/**
 * PCL Language PCLParser.
 *
 * User: ivan
 * Date: 2-Feb-2005
 * Time: 3:23:58 PM
 */

package org.pcl.parser;

import org.apache.log4j.Logger;
import org.pcl.parser.tools.CustomBufferedInputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * PCL file PCLParser.
 * <p/>
 * <p><i><strong>Note:</strong> this class is not thread safe.</i></p>
 */
public class PCLParser {

  /**
   * Logger for the PCLParser
   */
  protected Logger logger = Logger.getLogger(this.getClass().getName());

  /**
   * Two byte escape sequence range
   */
  public static final int tbyte_low = 48;
  public static final int tbyte_high = 126;

  /**
   * Parameterized characters range
   */
  public static final int param_low = 33;
  public static final int param_high = 47;

  /**
   * Group characters range
   */
  public static final int group_low = 96;
  public static final int group_high = 126;

  /**
   * Termination characters range
   */
  public static final int term_low = 64;
  public static final int term_hight = 94;

  /**
   * Form feed character
   */
  public static final byte CH_FORM_FEED = 0xC;

  /**
   * List of known commands that required data section
   */
  protected static String[] dataCommands = new String[]{"&pX", "(sW", ")sW", "*bW"};

  /**
   * Offset to start parsing from
   */
  protected long parsingOffset = 0;

  /**
   * Length to parse
   */
  protected long parsingLength = 0;

  /**
   * Static initialization
   */
  static {
    // array MUST be sorted for binary search
    Arrays.sort(dataCommands);
  }

  /**
   * Return array of characters for given range
   *
   * @param low  range start
   * @param high range stop
   * @return array of characters
   */
  public static char[] getChars(int low, int high) {
    if (high < low) throw new IllegalArgumentException("high must be >= low.");
    char[] chars = new char[high - low + 1];
    for (int i = low; i <= high; i++) chars[i - low] = (char) i;
    return chars;
  }

  /**
   * Check if given PCL command require a data section
   *
   * @param cmd command
   * @return <code>true</code> if data section expecting, <code>false</code> otherwise
   */
  public static boolean isDataExpected(PCLCommand cmd) {
    return Arrays.binarySearch(dataCommands, "" + cmd.getSecond_char() + cmd.getThird_char() + cmd.getBreak_char()) > 0;
  }

  /**
   * Return HEX String representation of given byte array
   *
   * @param bytes byte array
   * @return HEX String representation
   */
  public static String toHexString(byte[] bytes) {
    String res = "";
    for (byte aByte : bytes) {
      String hex = Integer.toHexString(aByte & 0xff).toUpperCase();
      res += (hex.length() < 2 ? "0" + hex : hex) + " ";
    }
    return res;
  }

  /**
   * File
   */
  protected File file = null;

  /**
   * Buffered input stream with offset counter
   */
  protected CustomBufferedInputStream in = null;

  /**
   * PCLParser listeners
   */
  protected List<ParserListener> listeners = new ArrayList<ParserListener>();

  /**
   * Return offset in a file
   *
   * @return file offset
   */
  public long getOffset() {
    long inOffset = -1;
    if (null != in) {
      inOffset = in.getOffset();
    }
    return inOffset;
  }

  /**
   * Return parsing length
   *
   * @return length
   */
  public long getParsingLength() {
    return parsingLength;
  }

  /**
   * Set number of bytes to parse
   *
   * @param parsingLength parsing length
   */
  public void setParsingLength(long parsingLength) {
    this.parsingLength = parsingLength;
  }

  /**
   * Return parsing offset, a position from to parse parse
   *
   * @return parsing offset
   */
  public long getParsingOffset() {
    return parsingOffset;
  }

  /**
   * Set parsing offset, a position from to parse
   *
   * @param parsingOffset parsing offset
   */
  public void setParsingOffset(long parsingOffset) {
    this.parsingOffset = parsingOffset;
  }

  /**
   * Construct a new PCLParser.
   */
  public PCLParser() {
    if (logger.isDebugEnabled()) {
      logger.debug("Two byte commands [" + String.valueOf(getChars(tbyte_low, tbyte_high)) + "]");
      logger.debug("Parameterized characters [" + String.valueOf(getChars(param_low, param_high)) + "]");
      logger.debug("Grouping characters [" + String.valueOf(getChars(group_low, group_high)) + "]");
      logger.debug("Terminating characters [" + String.valueOf(getChars(term_low, term_hight)) + "]");
    }
  }

  /**
   * Parse given file
   *
   * @param file file to parse
   */
  public void parse(File file) throws IOException {
    parse(file, 0, file.length());
  }

	public void parse(File file, long offset, long length) throws IOException {
		this.file = file;
		try {
			in = new CustomBufferedInputStream(new FileInputStream(this.file));
			this.parsingOffset = offset;
			this.parsingLength = length;
			doParse();
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public void parseByteArray(byte[] data) throws IOException {
		parseInputStream(new ByteArrayInputStream(data), 0, data.length);
	}

	public void parseInputStream(InputStream is, long offset, long length) throws IOException {
		try {
			in = new CustomBufferedInputStream(is);
			this.parsingOffset = offset;
			this.parsingLength = length;
			doParse();
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

  /**
   * Start parsing
   *
   * @throws IOException
   */
  private void doParse() throws IOException {
    /** PCL command */
    final PCLCommand cmd = new PCLCommand();
    /** Data */
    final Data dat = new Data();
    // start time
    long start = System.currentTimeMillis();
    // file position
    long position = in.getOffset();

    // skip bytes when required
    if (parsingOffset > 0) {
      long skipped = in.skip(parsingOffset);
      // if skipped les than required throw an error
      if (skipped != parsingOffset) {
        throw new IOException("Unable to skip {" + parsingOffset + "} bytes, skipped only {" + skipped + "}");
      }
    }

    // read first byte
    int bt = in.read();

    // until the end of the stream
    while (bt != -1 && position < (parsingOffset + parsingLength)) {
      // update offset
      position = in.getOffset();
      // we should start from ESC character
      if (bt == 0x1B) {
        // clear command
        cmd.clear();
        // clear data
        dat.clear();

        // read next character after ESC
        bt = in.read();
        // check for parameterized sequence
        if (bt != -1 && bt >= param_low && bt <= param_high) {
          // Parameterized character found
          // set command type
          cmd.setType(PCLCommand.TYPE_PARAMETERIZED);
          // ser second character
          cmd.setSecond_char((char) bt);
          // read third character
          bt = in.read();
          if (bt != -1 && bt >= group_low && bt <= group_high) {
            // group character found
            cmd.setThird_char((char) bt);
            bt = in.read();
          }
          // read parameter value
          String value = "";
          while (bt != -1 && (bt < term_low || bt > term_hight) && bt != 0x1B) {
            value += (char) bt;
            bt = in.read();
          }
          // set parameter value
          cmd.setValue(value);
          // read break character
          if (bt >= term_low && bt <= term_hight) {
            cmd.setBreak_char((char) bt);
          }
          // check if this command has a data section
          if (isDataExpected(cmd)) {
            // check if value contain a letter
            String _value = cmd.getValue();
            int dataSize = 0;
            // value may contain the compresion method
            // for instance ESC*b2m3W
            //   sets compression method=2 and sends 3 bytes of data
            if (_value.indexOf("m")!=-1) {
              dataSize = Integer.valueOf(_value.substring(_value.indexOf("m")+1));
            } else {
              dataSize = Integer.valueOf(_value);
            }
            // read data section
            byte[] dataSection = new byte[dataSize];
            int read = in.read(dataSection);
            if (read < dataSize) {
              logger.error("Expected " + dataSize + " bytes, read " + read + " bytes instead.");
            }
            cmd.setData(dataSection);
          }
          // read next character
          bt = in.read();
          logger.debug("Parameterized sequence at [" + position + "]> " + cmd.toString());
        } else
          // check for two byte sequence
          if (bt != -1 && bt >= tbyte_low && bt <= tbyte_high) {
            cmd.setType(PCLCommand.TYPE_TWO_CHARACTERS);
            cmd.setSecond_char((char) bt);
            logger.debug("Two byte sequence at [" + position + "] > " + cmd.toString());
            // read next character
            bt = in.read();
          }
          // Unknown character
          else {
            logger.error("Unknown character [" + Integer.toHexString(bt) + "|" + Integer.toString(bt) + "] {" + (char) bt + "}");
          }
        // notify listeners about parsed PCL command
        notifyCommand(position, cmd);
      } else {
        // data section
        while (bt != -1 && bt != 0x1B) {
          dat.addByte((byte) bt);
          bt = in.read();
        }
        if (logger.isDebugEnabled()) {
          logger.debug(dat.size() + " characters read as data at [" + position + "] [" + toHexString(dat.getBytes()) + "] {" + Arrays.toString(dat.getData().toArray()) + "}");
        }
        // notify listeners about PCLParser data section
        notifyData(position, dat);
      }
    }
    logger.info("Parsing took " + (System.currentTimeMillis() - start) + "ms.");
    logger.info("File position " + in.getOffset());
  }

  /**
   * Notify listeners about PCL command
   *
   * @param cmd command
   */
  protected void notifyCommand(long offset, PCLCommand cmd) {
    for (ParserListener listener : listeners) {
      listener.command(offset, cmd);
    }
  }

  /**
   * Notify all listeners about PCL data
   *
   * @param dat PCL data
   */
  protected void notifyData(long offset, Data dat) {
    for (ParserListener listener : listeners) {
      listener.data(offset, dat);
    }
  }

  public void addListener(ParserListener l) {
    listeners.add(l);
  }

  public void removeListener(ParserListener l) {
    listeners.remove(l);
  }

}
