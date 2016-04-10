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
 * The SimTableReader provides a reader for similarity files.
 * The table files are tab-separated text data files.
 */
public class SimTableReader extends TableReader {

  protected void processLine(String line, DSDictionary dict,
      IntObjectOpenHashMap<IntArrayList> map, int max) {

    String[] fields = line.split("\\t");

    if ((fields.length < 3)) {
      throw new RuntimeException("Wrong format in data input file");
    }

    String term1 = fields[0].trim();
    String term2 = fields[1].trim();
    double score = Double.parseDouble(fields[2].trim());

    int i1 = dict.addTerm(term1);
    if (!map.containsKey(i1)) {
      map.put(i1, new IntArrayList());
    }

    int i2 = dict.addTerm(term2);
    if (!map.containsKey(i2)) {
      map.put(i2, new IntArrayList());
    }

    IntArrayList list = map.get(i1);
    if (max < 1 || list.size() < max*2) {
      list.add(i2);
      list.add((int) score); // FIXME assumes int
    }    

  }
	
}
