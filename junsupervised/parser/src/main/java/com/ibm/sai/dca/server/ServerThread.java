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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.EOFException;
import java.net.Socket;
import java.util.Vector;

import com.ibm.sai.dca.common.GeneralizedPacketing;

public abstract class ServerThread extends Thread {

	private DataInputStream in;
	private DataOutputStream out;
	private Socket clientSocket;
	private String tableID;
	
	public ServerThread(Socket clientSocket, String tableID) {
		try {
			this.clientSocket = clientSocket;
			this.tableID = tableID;
			this.in = new DataInputStream(clientSocket.getInputStream());
			this.out = new DataOutputStream(clientSocket.getOutputStream());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public void run() {
		
		try {
			int count = 0;
			while(true) {
				String data = this.in.readUTF();
				//long startTime = System.currentTimeMillis();
				//Long frameId = Long.parseLong(data);
				//Vector<String> lines = Server.frameContentMap.get(frameId);
				Vector<String> lines = getContent(data);
				//long contTime = System.currentTimeMillis();
				GeneralizedPacketing.sendData(lines, this.out);
				//long endTime = System.currentTimeMillis();
				//System.out.println("Server: contTime: "+(contTime-startTime)+"; sendTime: "+(endTime-contTime));
				if (count > 1000000000) count = 0;
				if (count % 1000 == 0)	printProgress();
				count++;
			}
		} catch (EOFException eofe) {
			System.out.println("\nServerThread got EOFException, closing connection to "+clientSocket.getInetAddress());
			//eofe.printStackTrace();
			try {
				this.clientSocket.close();
			} catch (IOException ioe) {
				System.out.println("\nFailed to close connection. Terminating Thread.");
				ioe.printStackTrace();
			}
		} catch (IOException ioe) {
			System.out.println("\nServerThread got non-EOF IOException, closing connection to "+clientSocket.getInetAddress());
			//ioe.printStackTrace();
			try {
				this.clientSocket.close();
			} catch (IOException ioe2) {
				System.out.println("\nFailed to close connection. Terminating Thread.");
				ioe2.printStackTrace();
			}
		}
	}
	
	protected String getTableID() {
		return tableID;
	}
	
	protected abstract Vector<String> getContent(String query);
	protected abstract void printProgress();
	
}
