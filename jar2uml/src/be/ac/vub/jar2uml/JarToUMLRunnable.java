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
package be.ac.vub.jar2uml;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * Abstract base class for Jar2UML {@link Runnable}s.
 * @author Dennis Wagelaar <dennis.wagelaar@vub.ac.be>
 *
 */
public abstract class JarToUMLRunnable implements Runnable {

	private IProgressMonitor monitor = null;
	private long jobStartTime;
	private boolean runComplete = false;

	public JarToUMLRunnable() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		final IProgressMonitor monitor = getMonitor();
		try {
			setRunComplete(false);
			runWithMonitor(monitor);
			setRunComplete(true);
		} catch (OperationCanceledException e) {
			JarToUMLResources.logger.info(JarToUMLResources.getString("JarToUML.cancelled")); //$NON-NLS-1$
		} finally {
			done(monitor, JarToUMLResources.getString("JarToUML.finished")); //$NON-NLS-1$
		}
	}

	/**
	 * Performs the actual work.
	 * @param monitor
	 */
	protected abstract void runWithMonitor(IProgressMonitor monitor);

	/**
	 * Starts a new task with the progress monitor, if not null.
	 * @param monitor The progress monitor.
	 * @param name
	 * @param totalWork
	 * @see IProgressMonitor#beginTask(String, int)
	 */
	protected void beginTask(IProgressMonitor monitor, String name, int totalWork) {
		if (name != null) {
			JarToUMLResources.logger.info(name);
		}
		if (monitor != null) {
			setJobStartTime(System.currentTimeMillis());
			monitor.beginTask(name, totalWork);
		}
	}

	/**
	 * Logs and starts a new task on the progress monitor
	 * @param monitor
	 * @param message
	 */
	protected void subTask(IProgressMonitor monitor, String message) {
		if (message != null) {
			JarToUMLResources.logger.info(message);
		}
		if (monitor != null) {
			monitor.subTask(message);
		}
	}

	/**
	 * Increases the progressmonitor by 1, if not null.
	 * @param monitor
	 * @throws OperationCanceledException if user pressed cancel button.
	 */
	protected void worked(IProgressMonitor monitor, String message)
			throws OperationCanceledException {
				if (message != null) {
					final long time = System.currentTimeMillis()-getJobStartTime();
					JarToUMLResources.logger.info(String.format(
							JarToUMLResources.getString("JarToUML.logAt"), 
							message, time, time, time)); //$NON-NLS-1$
				}
				if (monitor != null) {
					monitor.worked(1);
					checkCancelled(monitor);
				}
			}

	/**
	 * Finishes progress monitor task.
	 * @param monitor
	 * @param message
	 */
	protected void done(IProgressMonitor monitor, String message) {
		if (message != null) {
			JarToUMLResources.logger.info(message);
		}
		if (monitor != null) {
			monitor.done();
		}
	}

	/**
	 * Handles cancelled progress monitor
	 * @param monitor
	 * @throws OperationCanceledException
	 */
	protected void checkCancelled(IProgressMonitor monitor)
			throws OperationCanceledException {
				if ((monitor != null) && monitor.isCanceled()) {
					throw new OperationCanceledException(JarToUMLResources.getString("operationCancelledByUser")); //$NON-NLS-1$
				}
			}

	/**
	 * @return The progress monitor object used to check for cancellations.
	 */
	public IProgressMonitor getMonitor() {
		return monitor;
	}

	/**
	 * Sets the progress monitor object used to check for cancellations.
	 * @param monitor
	 */
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	/**
	 * @return the jobStartTime
	 */
	public long getJobStartTime() {
		return jobStartTime;
	}

	/**
	 * @param jobStartTime the jobStartTime to set
	 */
	protected void setJobStartTime(long jobStartTime) {
		this.jobStartTime = jobStartTime;
	}

	/**
	 * @return the runComplete
	 */
	public boolean isRunComplete() {
		return runComplete;
	}

	/**
	 * @param runComplete the runComplete to set
	 */
	public void setRunComplete(boolean runComplete) {
		this.runComplete = runComplete;
	}

}