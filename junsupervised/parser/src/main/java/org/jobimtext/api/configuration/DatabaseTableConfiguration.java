/*******************************************************************************
* Copyright 2012
* Copyright (c) 2012 IBM Corp.
* 
* and
* 
* FG Language Technologie
* Technische Universitaet Darmstadt
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package org.jobimtext.api.configuration;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DatabaseTableConfiguration {
	String tableSimilarTerms;
	String tableSimilarContexts;
	String tableTermContextsScore;
	String tableContextsCount;
	String tableTermCount;
	String tableSenses;
	String tableIsas;
	String tableSenseCUIs;

	public String getTableSenses() {
		return tableSenses;
	}

	public void setTableSenses(String tableSenses) {
		this.tableSenses = tableSenses;
	}

	public String getTableIsas() {
		return tableIsas;
	}

	public void setTableIsas(String tableIsas) {
		this.tableIsas = tableIsas;
	}

	public DatabaseTableConfiguration() {
	}

	public String getTableSimilarTerms() {
		return tableSimilarTerms;
	}

	public void setTableSimilarTerms(String tableSimilarTerms) {
		this.tableSimilarTerms = tableSimilarTerms;
	}

	public String getTableTermContextsScore() {
		return tableTermContextsScore;
	}

	public void setTableTermContextsScore(String tableTermContextsScore) {
		this.tableTermContextsScore = tableTermContextsScore;
	}

	public String getTableContextsCount() {
		return tableContextsCount;
	}

	public void setTableContextsCount(String tableContextsCount) {
		this.tableContextsCount = tableContextsCount;
	}

	public String getTableTermCount() {
		return tableTermCount;
	}

	public void setTableTermCount(String tableKey) {
		this.tableTermCount = tableKey;
	}
	public void saveAsXml(PrintStream ps) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(this.getClass());
		Marshaller m = context.createMarshaller();
		m.marshal(this,ps);
	}
	
	public void saveAsXml(File file) throws JAXBException, FileNotFoundException{
		saveAsXml(new PrintStream(file));
	}
	public static DatabaseTableConfiguration getFromXmlFile(File name) throws JAXBException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		JAXBContext jaxbContext = JAXBContext.newInstance(DatabaseTableConfiguration.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		DatabaseTableConfiguration conf = (DatabaseTableConfiguration) jaxbUnmarshaller.unmarshal(name);
		return conf;
	}
	public static DatabaseTableConfiguration getFromXmlFile(String name) throws JAXBException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		return getFromXmlFile(new File(name));
	}

	public String getTableSimilarContexts() {
		return tableSimilarContexts;
	}

	public void setTableSimilarContexts(String tableSimilarContexts) {
		this.tableSimilarContexts = tableSimilarContexts;
	}

	public void setTableSenseCUIs(String tableSenseCUIs) {
		this.tableSenseCUIs = tableSenseCUIs;
		
	}
}
