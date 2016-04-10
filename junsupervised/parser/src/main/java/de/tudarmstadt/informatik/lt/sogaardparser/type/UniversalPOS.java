

/* First created by JCasGen Thu Oct 29 15:29:12 CET 2015 */
package de.tudarmstadt.informatik.lt.sogaardparser.type;

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

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Thu Oct 29 15:29:12 CET 2015
 * XML source: C:/Users/seldon/Documents/Informatik/unsupervised_parsing/junsupervised/src/main/resources/de/tudarmstadt/informatik/lt/junsupervised/type/UniversalPOS.xml
 * @generated */
public class UniversalPOS extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(UniversalPOS.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected UniversalPOS() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public UniversalPOS(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public UniversalPOS(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public UniversalPOS(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: posValue

  /** getter for posValue - gets Universal POS string
   * @generated
   * @return value of the feature 
   */
  public String getPosValue() {
    if (UniversalPOS_Type.featOkTst && ((UniversalPOS_Type)jcasType).casFeat_posValue == null)
      jcasType.jcas.throwFeatMissing("posValue", "de.tudarmstadt.informatik.lt.junsupervised.type.UniversalPOS");
    return jcasType.ll_cas.ll_getStringValue(addr, ((UniversalPOS_Type)jcasType).casFeatCode_posValue);}
    
  /** setter for posValue - sets Universal POS string 
   * @generated
   * @param v value to set into the feature 
   */
  public void setPosValue(String v) {
    if (UniversalPOS_Type.featOkTst && ((UniversalPOS_Type)jcasType).casFeat_posValue == null)
      jcasType.jcas.throwFeatMissing("posValue", "de.tudarmstadt.informatik.lt.junsupervised.type.UniversalPOS");
    jcasType.ll_cas.ll_setStringValue(addr, ((UniversalPOS_Type)jcasType).casFeatCode_posValue, v);}    
  }

    
