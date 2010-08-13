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
package be.ac.vub.jar2uml.cflow;

import org.apache.bcel.generic.ObjectType;

import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;

/**
 * Exception handler table entry.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class ExceptionHandler {
	
	private final ObjectType exceptionType;
	private final InstructionFlow handlerStart;

	/**
	 * Creates a new {@link ExceptionHandler}.
	 * @param exceptionType
	 * @param handlerStart
	 */
	public ExceptionHandler(ObjectType exceptionType, InstructionFlow handlerStart) {
		super();
		this.exceptionType = exceptionType;
		this.handlerStart = handlerStart;
	}

	/**
	 * @return the exceptionType
	 */
	public ObjectType getExceptionType() {
		return exceptionType;
	}

	/**
	 * @return the handlerStart
	 */
	public InstructionFlow getHandlerStart() {
		return handlerStart;
	}

}