/*
 ** emxConfigurationRuleBase
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 ** static const char RCSID[] = $Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/${CLASSNAME}.java 1.6.2.1.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$
 */

import java.util.HashMap;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.dassault_systemes.enovia.configuration.modeler.Model;
import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.effectivity.EffectivityFramework;

/**
 * This JPO class has some method pertaining to Configuration Rule type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class ConfigurationRuleBase_mxJPO extends emxDomainObject_mxJPO
{

/**
  * Default Constructor.
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
  * @throws Exception if the operation fails
  * @since R418
  * @grade 0
  */
  ConfigurationRuleBase_mxJPO (Context context, String[] args) throws Exception
  {
    super(context, args);
  }

 /**
  * Main entry point.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
  * @return an integer status code (0 = success)
  * @throws Exception if the operation fails
  * @since R418
  * @grade 0
  */
  public int mxMain(Context context, String[] args) throws Exception
  {
    if (!context.isConnected()){
         String strContentLabel =EnoviaResourceBundle.getProperty(context,
        	        "Configuration","emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
         throw  new Exception(strContentLabel);
        }
    return 0;
  }

  
    
    /**
     * Method call to get all the configuration Rules in the data base in given context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing one String entry for key "objectId"
     * @return Object - MapList containing the id of Configuration Rule objects
     * @throws Exception if the operation fails
     * @since R418
     * @grade 0
     */
     @com.matrixone.apps.framework.ui.ProgramCallable
     public MapList getAllConfigurationRuleObjects(Context context, String[] args) throws Exception
     {
       		/* obtain the Object id of the Product */
       	HashMap CFRMap = (HashMap) JPO.unpackArgs(args);
       	String strObjectId = (String)CFRMap.get("objectId");
       	strObjectId =  strObjectId.trim();

  	    StringList selList = new StringList();
  	    selList.add(DomainObject.SELECT_ID);
  	    selList.add(DomainObject.SELECT_NAME);
	  	selList.add(DomainObject.SELECT_REVISION);
  	    selList.add("to[" + ConfigurationConstants.RELATIONSHIP_PRODUCTS + "].from.id");

 		DomainObject domProduct  = new DomainObject(strObjectId);
 		Map ModelId = domProduct.getInfo(context,selList);
 		String physicalId ="";
 		if(ModelId != null && ModelId.size() > 0){
 			physicalId = (String)ModelId.get("to[" + ConfigurationConstants.RELATIONSHIP_PRODUCTS + "].from.id");
  	        if (physicalId== null)
  	           	physicalId = (String)ModelId.get("to[" + ConfigurationConstants.RELATIONSHIP_MAIN_PRODUCT + "].from.id");
 		}

 		 String Name = (String)ModelId.get("name");
	     String Revision = (String)ModelId.get("revision");
	        
       	StringList objSelects =new StringList(DomainConstants.SELECT_ID);
       	objSelects.add(DomainConstants.SELECT_TYPE);
       	StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
       	relSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_MANDATORYRULE);

       	
       	DomainObject domProduct1  = new DomainObject(physicalId);
        
        StringList selList1 = new StringList();
	    selList1.add(DomainObject.SELECT_NAME);
	    Map ModelMAP = domProduct1.getInfo(context,selList1);
	    String ModelName = (String)ModelMAP.get("name");
	    
       	MapList ConfiurationRulesMapList = new MapList();
       	String sFilterExpression = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><CfgFilterExpression xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"  xmlns=\"urn:com:dassault_systemes:config\" xs:schemaLocation=\"urn:com:dassault_systemes:config CfgFilterExpression.xsd\"><FilterSelection SelectionMode=\"Strict\"><Context  HolderType=\"Model\" HolderName=\""+ModelName+"\"><TreeSeries Type=\"ProductState\" Name=\""+ModelName+"\"><Single Name=\""+Name+"\" Revision=\""+Revision+"\"></Single></TreeSeries></Context></FilterSelection></CfgFilterExpression>";
       	String strFilterBinary;
       	
       	EffectivityFramework EFF = new EffectivityFramework();
		Map effMap = EFF.getFilterCompiledBinary(context, sFilterExpression, null, new StringList(), EFF.CURRENT_VIEW);
		strFilterBinary = (String)effMap.get(EffectivityFramework.COMPILED_BINARY_EXPR);
		
       	Model mdl = new Model(physicalId);
       	ConfiurationRulesMapList = mdl.getCfgRules(context, objSelects, relSelects, strFilterBinary);     	
       	
       	return ConfiurationRulesMapList;
       }

}
