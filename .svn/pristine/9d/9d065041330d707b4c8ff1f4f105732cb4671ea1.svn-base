/**
 * APICloud Studio
 * Copyright (c) 2014-2015 by APICloud, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3.
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.apicloud.loader.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class APIProcessBuilder {
	private static APIProcessBuilder fAPIProcessBuilder = null;
	
	private ProcessBuilder m_processBuilder;
	private List<String> m_command;
	
	public synchronized static APIProcessBuilder getLocal() {
		if (fAPIProcessBuilder == null) {
			fAPIProcessBuilder = new APIProcessBuilder();
		}
		return fAPIProcessBuilder;
	}
	
	public APIProcessBuilder() {
		this.m_processBuilder = new ProcessBuilder();
		this.m_command = new ArrayList<String>();
	}
	
	public void clear() {
		m_command.clear();
	}
	
	public void setExecutable(String executable) {
		this.m_command.add(executable);
	}
	
	public void setParameters(String[] parameters) {
		this.m_command.addAll(Arrays.asList(parameters));
	}
	
	public Process call() throws IOException {
		this.m_processBuilder.command(this.m_command);
		m_processBuilder.redirectErrorStream(true);
		Process p = this.m_processBuilder.start();
		return p;
	}

	public ProcessBuilder getProcessBuilder() {
		return m_processBuilder;
	}

	public List<String> getCommand() {
		return m_command;
	}
}
