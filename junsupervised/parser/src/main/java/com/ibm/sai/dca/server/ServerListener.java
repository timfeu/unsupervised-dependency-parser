/**
 *   Copyright (c) 2012 IBM Corp.
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *         
 *   http://www.apache.org/licenses/LICENSE-2.0
 *               	
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *               	               
 *   @author: Bonaventura Coppola (coppolab@gmail.com)
 *   
 */

package com.ibm.sai.dca.server;

/*
 * #%L
 * JUnsupervisedParser
 * %%
 * Copyright (C) 2016 Tim Feuerbach
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.lang.Class;

public class ServerListener<D> extends Thread {

	private int listenPort;
	private String tableID;
	private Class<D> serverThreadClass;
	
	public ServerListener(int listenPort, String tableID, Class<D> serverThreadClass) {
		this.listenPort = listenPort;
		this.tableID = tableID;
		this.serverThreadClass = serverThreadClass;
	}
	
	public void run() {

		ServerSocket listenSocket = null;
		try {
			listenSocket = new ServerSocket(listenPort);
		} catch(IOException e) {
			System.out.println("Server Init Failed");
			e.printStackTrace();
			System.exit(1);
		}

		try {
			while(true) {
				Socket clientSocket = listenSocket.accept();
				System.out.println("\nAccepted "+serverThreadClass.getName()+" Connection from: "+clientSocket.getInetAddress());
				ServerThread thread = (ServerThread) serverThreadClass.getConstructor(java.net.Socket.class, java.lang.String.class).newInstance(clientSocket, tableID);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

