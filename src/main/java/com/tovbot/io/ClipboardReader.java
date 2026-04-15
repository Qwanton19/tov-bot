package com.tovbot.io;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

// adapted from ninjabrain bot.
public class ClipboardReader {

	public static String readClipboard() {
		try {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Object data = clipboard.getData(DataFlavor.stringFlavor);
			if (data instanceof String) {
				return (String) data;
			}
		} catch (UnsupportedFlavorException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (IllegalStateException e) {
			return null;
		}
		return null;
	}
}
