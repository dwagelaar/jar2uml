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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.verifier.structurals.ExceptionHandlers;

import be.ac.vub.jar2uml.cflow.ControlFlow.InstructionFlow;

/**
 * Provides per-instruction access to an ordered list of
 * exception handlers.
 * Adapted from {@link ExceptionHandlers}.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 */
public class OrderedExceptionHandlers {

	protected static final List<ExceptionHandler> EMPTY = Collections.EMPTY_LIST;

	protected final Map<InstructionFlow, List<ExceptionHandler>> exceptionHandlers = new HashMap<InstructionFlow, List<ExceptionHandler>>();

	/**
	 * Creates a new {@link OrderedExceptionHandlers}.
	 * @param cflow
	 */
	public OrderedExceptionHandlers(final ControlFlow cflow) {
		super();
		//fill exceptionHandlers
		final CodeExceptionGen[] cegs = cflow.getMethod().getExceptionHandlers();
		for (int i=0; i<cegs.length; i++){
			ExceptionHandler eh = new ExceptionHandler(cegs[i].getCatchType(), cflow.getFlowOf(cegs[i].getHandlerPC()));
			for (InstructionHandle ih=cegs[i].getStartPC(); ih != cegs[i].getEndPC().getNext(); ih=ih.getNext()) {
				InstructionFlow iflow = cflow.getFlowOf(ih);
				List<ExceptionHandler> hs = exceptionHandlers.get(iflow);
				if (hs == null) {
					hs = new ArrayList<ExceptionHandler>();
					exceptionHandlers.put(iflow, hs);
				}
				hs.add(eh);
			}
		}
		//make entries read-only
		for (Entry<InstructionFlow, List<ExceptionHandler>> entry : exceptionHandlers.entrySet()) {
			entry.setValue(Collections.unmodifiableList(entry.getValue()));
		}
	}

	/**
	 * @param iflow
	 * @return all {@link ExceptionHandler} instances that protect iflow
	 */
	public List<ExceptionHandler> getExceptionHandlers(InstructionFlow iflow) {
		final List<ExceptionHandler> hs = exceptionHandlers.get(iflow);
		return hs == null ? EMPTY : hs;
	}

}
