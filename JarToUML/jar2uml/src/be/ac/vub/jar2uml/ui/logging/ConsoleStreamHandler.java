/*******************************************************************************
 * Copyright (c) 2007-2010 Dennis Wagelaar, Vrije Universiteit Brussel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dennis Wagelaar, Vrije Universiteit Brussel
 *******************************************************************************/
package be.ac.vub.jar2uml.ui.logging;

import java.io.OutputStream;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Flushes after every log and doesn't close output stream.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class ConsoleStreamHandler extends StreamHandler {

	/**
	 * Creates a ConsoleStreamHandler for out.
	 * @param out
	 */
	public ConsoleStreamHandler(OutputStream out) {
		super(out, JarToUMLLogFormatter.INSTANCE);
	}

	/**
	 * @see StreamHandler#publish(java.util.logging.LogRecord)
	 */
	public void publish(LogRecord record) {
		super.publish(record);
		flush();
	}

	/**
	 * Don't close output stream.
	 */
	public void close() {
		//don't close
	}
}
