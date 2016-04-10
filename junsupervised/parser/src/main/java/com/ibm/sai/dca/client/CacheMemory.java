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

package com.ibm.sai.dca.client;

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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class CacheMemory<K, V> {

	static final float LOAD_FACTOR = (float) 0.75;
	static final boolean ACCESS_ORDER = true;
	
	private Map<K, V> map;
	
	protected int cacheMemorySize;

	@SuppressWarnings("unused")
	private CacheMemory() {
	}

	public CacheMemory(int cacheMemorySize) {
		this.cacheMemorySize = cacheMemorySize;
		map = Collections.synchronizedMap(new CacheMap(cacheMemorySize, LOAD_FACTOR, ACCESS_ORDER));
	}
	
	public V get(K key) {
		return map.get(key);
	}
	
	public void put(K key, V value) {
		map.put(key,  value);
	}
	
	@SuppressWarnings("serial")
	private class CacheMap extends LinkedHashMap<K, V>
	{
		private int maxEntries;
		
		public CacheMap(int maxEntries, float loadFactor, boolean accessOrder) {
			super(maxEntries, loadFactor, accessOrder);
			this.maxEntries = maxEntries;
		}
		
		protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
			return size() > maxEntries;
		}
	}
}