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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntObjectOpenHashMap;

/**
 * This abstract class provides methods for reading 'table' files containing the Distributional Thesaurus
 *
 */
public abstract class TableReader {

	private static enum FILETYPE {PLAIN, GZIP};
			
	public void loadMapFromFileOrDir(String iFileOrDirName, DSDictionary dict, 
	    IntObjectOpenHashMap<IntArrayList> map, int maxEntries, String ext) {
		File f = new File(iFileOrDirName);
		File[] fileList;
		if (f.isDirectory()) {
			if ((ext != null) && !ext.equals("") && !ext.equals("NONE")) {
				fileList = f.listFiles(new FileExtensionFilter(ext));
			} else {
				fileList = f.listFiles();
			}
		} else {
			fileList = new File[1];
			fileList[0] = f;
		}
		for (int i = 0; i < fileList.length; i++) {
			loadMapFromFile(fileList[i], dict, map, maxEntries);
		}
	}
	
	public void loadMapFromFile(File iFile, DSDictionary dict,
	    IntObjectOpenHashMap<IntArrayList> map, int max) {
		try {
			String iFileName = iFile.getCanonicalPath();
			FILETYPE fileType = FILETYPE.PLAIN;
			if (iFileName.endsWith(".gz")) fileType = FILETYPE.GZIP;
			System.out.println("Reading : "+iFileName);
			
			BufferedReader bfr;
			if (fileType.equals(FILETYPE.GZIP)) {
				bfr = new BufferedReader(
				          new InputStreamReader(
				              new GZIPInputStream(
				                  new FileInputStream(iFileName))));
			} else {
				bfr = new BufferedReader(
				          new FileReader(iFileName));
			}
			
			long count = 0;
			String line = bfr.readLine();
			while (line != null) {
				count++;
				if (count % 1000000 == 0) {
				  System.out.println("Reading Line: "+count);
				}
				processLine(line, dict, map, max);
				line = bfr.readLine();
			}
			bfr.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	protected abstract void processLine(String line, DSDictionary dict, 
	    IntObjectOpenHashMap<IntArrayList> map, int max);

	private class FileExtensionFilter implements FilenameFilter {
	  private String extension;   
	  public FileExtensionFilter(String extension) {
	    this.extension = ("." + extension).toLowerCase();
	  }       
	  public boolean accept(File dir, String name) {
	    return name.toLowerCase().endsWith(extension);
	  }
	}
	
}
