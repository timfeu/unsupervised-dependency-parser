
/* First created by JCasGen Thu Oct 29 15:29:12 CET 2015 */
package de.tudarmstadt.informatik.lt.junsupervised.type;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.impl.FSGenerator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Thu Oct 29 15:29:12 CET 2015
 * @generated */
public class UniversalPOS_Type extends Annotation_Type {
  /** @generated 
   * @return the generator for this type
   */
  @Override
  protected FSGenerator getFSGenerator() {return fsGenerator;}
  /** @generated */
  private final FSGenerator fsGenerator = 
    new FSGenerator() {
      public FeatureStructure createFS(int addr, CASImpl cas) {
  			 if (UniversalPOS_Type.this.useExistingInstance) {
  			   // Return eq fs instance if already created
  		     FeatureStructure fs = UniversalPOS_Type.this.jcas.getJfsFromCaddr(addr);
  		     if (null == fs) {
  		       fs = new UniversalPOS(addr, UniversalPOS_Type.this);
  			   UniversalPOS_Type.this.jcas.putJfsFromCaddr(addr, fs);
  			   return fs;
  		     }
  		     return fs;
        } else return new UniversalPOS(addr, UniversalPOS_Type.this);
  	  }
    };
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = UniversalPOS.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("de.tudarmstadt.informatik.lt.junsupervised.type.UniversalPOS");
 
  /** @generated */
  final Feature casFeat_posValue;
  /** @generated */
  final int     casFeatCode_posValue;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getPosValue(int addr) {
        if (featOkTst && casFeat_posValue == null)
      jcas.throwFeatMissing("posValue", "de.tudarmstadt.informatik.lt.junsupervised.type.UniversalPOS");
    return ll_cas.ll_getStringValue(addr, casFeatCode_posValue);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setPosValue(int addr, String v) {
        if (featOkTst && casFeat_posValue == null)
      jcas.throwFeatMissing("posValue", "de.tudarmstadt.informatik.lt.junsupervised.type.UniversalPOS");
    ll_cas.ll_setStringValue(addr, casFeatCode_posValue, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public UniversalPOS_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_posValue = jcas.getRequiredFeatureDE(casType, "posValue", "uima.cas.String", featOkTst);
    casFeatCode_posValue  = (null == casFeat_posValue) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_posValue).getCode();

  }
}



    