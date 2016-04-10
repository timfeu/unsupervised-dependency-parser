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
 *   @author: Matthew Hatem (mhatem@us.ibm.com)
 *   
 */

package com.ibm.sai.distributional_similarity.api.data;

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

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectOpenHashMap;

/**
 * The SenseTableReader provides reader for sense cluster files.
 * The table files are tab-separated text data files.
 */
public class SenseTableReader extends TableReader {

  protected void processLine(String line, DSDictionary dict,
      IntObjectOpenHashMap<IntArrayList> map, int max) {
    
    // ensure terms in dictionary
    String[] fields = line.split("\\t");
    if(fields.length<3)return;
    String term = fields[0].trim();
        
    int index = dict.addTerm(term);
    if (!map.containsKey(index)) {
      map.put(index, new IntArrayList());
    }
    String[] terms = fields[2].split(",");
    for (int i=0; i<terms.length; i++) {
      String sim = terms[i].trim();
      index = dict.addTerm(sim);
      if (!map.containsKey(index)) {
        map.put(index, new IntArrayList());
      }
    }

    // add senses
    index = dict.getIndex(term);
    IntArrayList list = map.get(index);
    list.add(Integer.parseInt(fields[1].trim()));
    if (max < 0 || list.size() < max) {
      for (int i=0; i<terms.length; i++) {
        String sim = terms[i].trim();
        list.add(dict.getIndex(sim));
      }
      list.add(-1);
    }

  }
	
}
