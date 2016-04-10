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

import java.net.Socket;
import java.util.Vector;

import com.ibm.sai.dca.common.ContentValue;
import com.ibm.sai.dca.common.ContentValue_Table;
import com.ibm.sai.dca.common.ContentValue_Score;

public class ServerThread_GeneralInstance extends ServerThread {

	public ServerThread_GeneralInstance(Socket clientSocket, String tableID) {
		super(clientSocket, tableID);
	}
	
	public Vector<String> getContent(String query) {
		ContentValue.VALTYPE type = Server.clusterConfig.getTableValType(getTableID());
		//System.out.println("Server got query: "+query);
		ContentValue innerValue;
		//if (type.equals(ContentValue.VALTYPE.TABLE)) {
			innerValue = ((ContentTable_HashMap) Server.contentTables.get(getTableID())).get(query);
		//} else {
		//	innerValue = ((ContentTable_SuffixTree) Server.contentTables.get(getTableID())).get(query);
		//}
		Vector<String> outV = new Vector<String>();
		if (innerValue == null) return outV;
		if (type.equals(ContentValue.VALTYPE.TABLE)) {
			for (String innerKey : ((ContentValue_Table) innerValue).keySet()) {
				outV.add(innerKey+"\t"+((ContentValue_Table) innerValue).get(innerKey).toString());
			}
		} else if (type.equals(ContentValue.VALTYPE.SCORE)) {
			outV.add(((ContentValue_Score) innerValue).getScore().toString());
		}
		//System.out.println("Server built answer counting of lines "+outV.size()+"\n");
		return outV;
	}
	
	protected void printProgress() {
		System.out.print(".");
	}
}
