package org.jobimtext.api;

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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Martin Riedl (riedl@cs.tu-darmstadt.de) This interface is used to
 *         retrieve all contents that are needed for the distributional
 *         thesaurus and the contextual thesaurus. We therefore need to specify
 *         the value of a KEY and VALUE. Furthermore the similar words are
 *         specified with ORDER2
 * @param <TERM>
 * @param <CONTEXTS>
 * @param <ORDER2LIST>
 */
public interface IThesaurus<TERM, CONTEXTS, ORDER2LIST, ORDER1LIST, ISAS, SENSES,  SENSE_CUIS> {
	public boolean connect();

	public void destroy();
	public Double getSimilarTermScore(TERM t1, TERM t2);
	public ORDER2LIST getSimilarTerms(TERM key);
	public ORDER2LIST getSimilarTerms(TERM key, int numberOfEntries);
	public ORDER2LIST getSimilarTerms(TERM key, double threshold);
	
	public ORDER2LIST getSimilarContexts(CONTEXTS values);
	public ORDER2LIST getSimilarContexts(CONTEXTS values, int numberOfEntries);
	public ORDER2LIST getSimilarContexts(CONTEXTS values, double threshold);
	
	public Long getTermCount(TERM key);
	public Long getContextsCount(CONTEXTS key);
	public Long getTermContextsCount(TERM key, CONTEXTS val);
	public Double getTermContextsScore(TERM key, CONTEXTS val);
    public Map<String, Double> getBatchTermContextsScore(TERM expandedTerm, CONTEXTS context);
    public Double getAverageContextsScore();

    public ORDER2LIST getContextTermsScores(CONTEXTS feature);
	
	public ORDER1LIST getTermContextsScores(TERM key);
	public ORDER1LIST getTermContextsScores(TERM key, int numberOfEntries);
	public ORDER1LIST getTermContextsScores(TERM key, double threshold);

	public SENSES getSenses(TERM key) ;
	public ISAS getIsas(TERM key) ;
	public SENSE_CUIS getSenseCUIs(TERM key);

}
