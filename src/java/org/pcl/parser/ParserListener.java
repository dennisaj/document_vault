/**
 * User: ivan
 * Date: 3-Feb-2005
 * Time: 8:31:23 PM
 */

package org.pcl.parser;

public interface ParserListener {

  public void command(long position, final PCLCommand cmd);

  public void data(long position, final Data dat);

}
