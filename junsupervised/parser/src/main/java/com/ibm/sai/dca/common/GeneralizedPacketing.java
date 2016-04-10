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

package com.ibm.sai.dca.common;

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
import java.util.Vector;

public class GeneralizedPacketing {

	private static final String LINE_SEP = "###SEP###";
	private static final String EOF = "EOF";
	
	private static final int PACK_SIZE = 16000;
	
	public static void sendData(Vector<String> lines, DataOutputStream dos) throws IOException {

		String outString = "";
		if (lines != null) {
			for (String line : lines) {
				outString += line+LINE_SEP;
			}
		}
		outString += EOF;
		
		int num_packets = (int) Math.ceil((double) outString.length() / (double) PACK_SIZE);
		for (int i = 0; i < num_packets; i++) {
			dos.writeUTF(outString.substring(i*PACK_SIZE, Math.min( ((i+1)*PACK_SIZE), outString.length() )));
		}
	}
	
	public static Vector<String> receiveData(DataInputStream dis) throws IOException {

		Vector<String> resultLines = new Vector<String>(); 

		String data = "";
		while (true) {
			data += dis.readUTF();
			if (data.endsWith(EOF)) break;
		}
		String[] lines = data.split(LINE_SEP);
		for (String line : lines) {
			if (!line.equals("EOF")) {
				resultLines.add(line);
			}
		}
		return resultLines;
	}
	
}
