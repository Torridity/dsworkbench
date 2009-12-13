/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.tor.tribes.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 *
 * @author Torridity
 */
public class DTransferable implements Transferable
{

  static final DataFlavor[] supportedFlavors = {null};

  static {
      try {
          supportedFlavors[0] = new
          DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
      } catch (Exception ex) {
          ex.printStackTrace();
      }
  }

  public static Object object;

  public DTransferable ()
  {

  }

  public DataFlavor[] getTransferDataFlavors ()
  {
    return supportedFlavors;
  }

  public boolean isDataFlavorSupported (DataFlavor flavor)
  {
	  return flavor.isMimeTypeEqual
      (DataFlavor.javaJVMLocalObjectMimeType);

  }

  public Object getTransferData (DataFlavor flavor) throws UnsupportedFlavorException, IOException
  {
	  if (flavor.isMimeTypeEqual
	          (DataFlavor.javaJVMLocalObjectMimeType)) {
	          return object;
	      } else {
	          return null;
	      }
  }
}
