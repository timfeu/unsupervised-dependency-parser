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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.carrotsearch.hppc.IntObjectOpenHashMap;
import com.carrotsearch.hppc.ObjectIntOpenHashMap;

/**
 * The DSDictionary is used to store and access the DT in memory, using a HashMap.
 */
public class DSDictionary {

  IntObjectOpenHashMap<String> intToTerm;
  ObjectIntOpenHashMap<String> termToInt;
  int size = 1;
  
  public DSDictionary() {
    intToTerm = new IntObjectOpenHashMap<String>();
    termToInt = new ObjectIntOpenHashMap<String>();
  }

  public int getIndex(String term) {
    return termToInt.get(term);
  }
  
  public String getTerm(int index) {
    return intToTerm.get(index);
  }
  
  public int addTerm(String term) {
    if (!termToInt.containsKey(term)) {
      termToInt.put(term, size);
      intToTerm.put(size, term);
      size++;
      if (size == Integer.MAX_VALUE) {
        throw new IllegalStateException();
      }
    }
    return termToInt.get(term);
  }
  
  public void putTerm(int index, String term) {
    termToInt.put(term, index);
    intToTerm.put(index, term);
  }

  public int getSize() {
    return size;
  }
  
  public static DSDictionary create(String file) {
    DSDictionary dict = new DSDictionary();
    try {
      String line;
      BufferedReader reader = new BufferedReader(new FileReader(file));
      int num = 0;
      while ((line = reader.readLine()) != null) {
    	  ++num;
        if (dict.size % 1000000 == 0) {
          System.out.println("Dictionary read "+dict.intToTerm.size()+" terms");
        }
        String[] tokens = line.split("\t");
        if ( 2 <= tokens.length ) { 
        	dict.putTerm(Integer.parseInt(tokens[0]), tokens[1]);
        } else { 
        	System.out.println(file + " line " + num + ": two tab-separated tokens not found in \"" + line + "\"");
        }
      }
      reader.close();
      System.out.println("Dictionary read "+dict.intToTerm.size()+" terms");
    } catch (IOException e) {
      e.printStackTrace();
    }
    dict.size = dict.termToInt.size();
    
    return dict;
  }
}
