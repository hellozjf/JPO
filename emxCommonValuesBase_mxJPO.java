/*
**	emxCommonValuesBase
**
**	Copyright (c) 1993-2016 Dassault Systemes.
**	All Rights Reserved.
**  This program contains proprietary and trade secret information of
**  Dassault Systemes.
**  Copyright notice is precautionary only and does not evidence any actual
**  or intended publication of such program
*/

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.LogicalFeature;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.ProductLineUtil;

public class emxCommonValuesBase_mxJPO extends emxDomainObject_mxJPO
{

	String strCommonGroupIcon = "iconSmallCommonGroup.gif";
	String strConfigurableFeatureIcon = "iconSmallConfigurableFeature.gif";
	String strConfigurationOptionsIcon = "iconSmallConfigurationOption.gif";
	String strConfigurationFeatureIcon = "iconSmallConfigurationfeature.gif";
	String strProductIcon = "iconSmallProduct.gif";
	// The operator symbols
	/** A string constant with the value &&. */
	protected static final String SYMB_AND = " && ";
	/** A string constant with the value ||. */
	protected static final String SYMB_OR = " || ";
	/** A string constant with the value ==. */
	protected static final String SYMB_EQUAL = " == ";
	/** A string constant with the value !=. */
	protected static final String SYMB_NOT_EQUAL = " != ";
	/** A string constant with the value >. */
	protected static final String SYMB_GREATER_THAN = " > ";
	/** A string constant with the value <. */
	protected static final String SYMB_LESS_THAN = " < ";
	/** A string constant with the value >=. */
	protected static final String SYMB_GREATER_THAN_EQUAL = " >= ";
	/** A string constant with the value <=. */
	protected static final String SYMB_LESS_THAN_EQUAL = " <= ";
	/** A string constant with the value ~~. */
	protected static final String SYMB_MATCH = " ~~ ";  // Short term fix for Bug #243366, was " ~~ "
	/** A string constant with the value '. */
	protected static final String SYMB_QUOTE = "'";
	/** A string constant with the value *. */
	protected static final String SYMB_WILD = "*";
	/** A string constant with the value (. */
	protected static final String SYMB_OPEN_PARAN = "(";
	/** A string constant with the value ). */
	protected static final String SYMB_CLOSE_PARAN = ")";
	/** A string constant with the value attribute. */
	protected static final String SYMB_ATTRIBUTE = "attribute";
	/** A string constant with the value [. */
	protected static final String SYMB_OPEN_BRACKET = "[";
	/** A string constant with the value ]. */
	protected static final String SYMB_CLOSE_BRACKET = "]";
	/** A string constant with the value to. */
	protected static final String SYMB_TO = "to";
	/** A string constant with the value from. */
	protected static final String SYMB_FROM = "from";
	/** A string constant with the value ".". */
	protected static final String SYMB_DOT = ".";
	/** A string constant with the value "null". */
	protected static final String SYMB_NULL = "null";

	protected static final String FIELD_DISPLAY_CHOICES = "field_display_choices";
     /** A string constant with the value field_choices. */
    protected static final String FIELD_CHOICES = "field_choices";
    
    public static final String SELECT_PHYSICALID ="physicalid";
    public static final String SUITE_KEY ="Configuration";

    /**
    * Default Constructor.
    *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    public emxCommonValuesBase_mxJPO(Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
    * Main entry point into the JPO class. This is the default method that will be excuted for this class.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return int - An integer status code (0 = success)
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    */
    public int mxMain(Context context, String[] args) throws Exception
    {
        if (!context.isConnected())
        {
           //  throw new Exception(strContentLabel);
		           return 1;

        }
        return 0;
    }

private String executeMqlCommand(Context context  , String strMqlCommand ) throws Exception{

	String strResult = MqlUtil.mqlCommand(context , strMqlCommand ,true );
	if (strResult == null)
	{
		strResult = "";
	}
	return strResult ; 
}

	/**
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * 
	 */
 public Map getRangeValuesForGlobalCommonGroup(Context context, String[] args) throws Exception
{
	
	String strAttributeName = ConfigurationConstants.ATTRIBUTE_GLOBAL_COMMON_GROUP;
	HashMap rangeMap = new HashMap();
	matrix.db.AttributeType attribName = new matrix.db.AttributeType(strAttributeName);
	attribName.open(context);

	List attributeRange = attribName.getChoices();
	List attributeDisplayRange = i18nNow.getAttrRangeI18NStringList(ConfigurationConstants.ATTRIBUTE_GLOBAL_COMMON_GROUP,
					   (StringList)attributeRange,context.getSession().getLanguage());


	rangeMap.put(FIELD_CHOICES , attributeRange);
	rangeMap.put(FIELD_DISPLAY_CHOICES , attributeDisplayRange);
	return  rangeMap;
}
/**
 * 
 * @param context
 * @param args
 * @return
 * @throws Exception
 * 
 */
public StringList isColumnEditable(Context context, String[] args )throws Exception
{
    try {
        HashMap inputMap = (HashMap)JPO.unpackArgs(args);
        MapList objectMap = (MapList) inputMap.get("objectList");
        StringList returnStringList = new StringList (objectMap.size());
        
        for (int i = 0; i < objectMap.size(); i++) {
            Map table = (Map) objectMap.get(i);
            String strRelId = (String)table.get("id[connection]");
            
        	if (strRelId != null && !"".equals(strRelId)) {
        		String strMqlCommand = "print connection \""+strRelId+"\" select frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_GROUP+"].id dump | "; 
        		String strCommonValues = executeMqlCommand(context , strMqlCommand) ;
        		if (strCommonValues != null && "".equals(strCommonValues)) {
                	returnStringList.add(Boolean.valueOf(true));
        		} else {
                	returnStringList.add(Boolean.valueOf(false));
        		}
        	} else {
            	returnStringList.add(Boolean.valueOf(true));
        	}
        }

        return returnStringList;

        
    } catch(Exception e) {
        e.printStackTrace();
        throw new FrameworkException(e.getMessage());
    }
}
 /**
  * 
  * @param context
  * @param args
  * @return
  * @throws Exception
  * 
  */
public Boolean updateGlobalCommonGroupValue(Context context,String[] args) throws Exception
{

	HashMap programMap = (HashMap)JPO.unpackArgs(args);
	HashMap paramMap = (HashMap)programMap.get("paramMap");

	String strRelId = (String)paramMap.get("relId");
	String strNewAttribval  = (String)paramMap.get("New Value");

    String language = context.getSession().getLanguage();
    String strAlertMessage =EnoviaResourceBundle.getProperty(context,SUITE_KEY,language,"emxProduct.Alert.GlobalCommonGroupModify");

	
	if (strRelId != null && !"".equals(strRelId))
	{
		String strMqlCommand = "print connection \""+strRelId+"\" select frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_GROUP+"].id dump | "; 

		String strCommonValues = executeMqlCommand(context , strMqlCommand) ;

		DomainRelationship domRel = new DomainRelationship(strRelId);

		if (strCommonValues != null && "".equals(strCommonValues)){
			domRel.setAttributeValue(context ,ConfigurationConstants.ATTRIBUTE_GLOBAL_COMMON_GROUP , strNewAttribval  );	
		}else{
            emxContextUtilBase_mxJPO contextUtil = new  emxContextUtilBase_mxJPO(context,null) ; 
			contextUtil.mqlNotice(context , strAlertMessage);
		}
	}

	return Boolean.valueOf(true);
  }


////////////////////////////////////////////////////////////////////R212 Code//////////////////////////////////////////////////

/**
* This method is used to get the Common Values attached to the Common Group
* @param context     the eMatrix <code>Context</code> object
* @param strCGId			Contains the Rel id of the Common Group
* @return					 Vector containing the Common Values 
* @exception				 Exception if operation fails
* @since					 Feature Configuration V6R2008-1
*/
private Vector getCommonValuesDataForCG(Context context , String strCGRelId) throws Exception
{
	//Use getRelationshipData and get the CG Rel Ids
	Vector vecCommonValues = new Vector();
	DomainRelationship domCGRelId = new DomainRelationship(strCGRelId);
	StringList sLRelSelect = new StringList("frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_VALUES+"].torel."+DomainConstants.SELECT_ID+"");
	sLRelSelect.addElement("frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_VALUES+"].torel.to."+DomainConstants.SELECT_ID+"");
	sLRelSelect.addElement("frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_VALUES+"].torel.to."+DomainConstants.SELECT_NAME+"");
	
	Hashtable htCGInfo = domCGRelId.getRelationshipData(context,sLRelSelect);
        
	StringList slConfigOptRelIds = (StringList)htCGInfo.get("frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_VALUES+"].torel."+DomainConstants.SELECT_ID+"");
	StringList slConfigOptIds = (StringList)htCGInfo.get("frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_VALUES+"].torel.to."+DomainConstants.SELECT_ID+"");
	StringList slConfigOptNames = (StringList)htCGInfo.get("frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_VALUES+"].torel.to."+DomainConstants.SELECT_NAME+"");
	
	HashMap hsCGMap =  new HashMap();	
	hsCGMap.put("Common Group Id" ,strCGRelId);
	hsCGMap.put("Config Options RelIds" ,slConfigOptRelIds);
	hsCGMap.put("Config Options Ids" ,slConfigOptIds);
	hsCGMap.put("Config Options Names" ,slConfigOptNames);
	vecCommonValues.add(hsCGMap);	
	
	return vecCommonValues ; 
	
}	
	/*
	String strMqlCommand = "print connection \""+strCGId+"\" select frommid["+ConfigurationConstants.RELATIONSHIP_COMMON_VALUES+"].to."+DomainConstants.SELECT_ID+"  dump |";
	String strCommonValues = executeMqlCommand(context , strMqlCommand ) ;
	StringTokenizer strTokens = new StringTokenizer(strCommonValues,"|");
	
	while(strTokens.hasMoreTokens()){
		vecCommonValues.add((String)strTokens.nextToken());	
	}
	return vecCommonValues ; 
}*/

public Vector populateCommonGroupValuesInViewCGListPage(Context context , String[] args) throws Exception{
	
	HashMap programMap = (HashMap)JPO.unpackArgs(args);
	MapList objectList =  (MapList)programMap.get("objectList");
    String languageStr = context.getSession().getLanguage();
	String i18CG=EnoviaResourceBundle.getProperty(context,SUITE_KEY, languageStr,"emxProduct.Tooltip.CommonGroup");
	String i18CV=EnoviaResourceBundle.getProperty(context,SUITE_KEY, languageStr,"emxProduct.Tooltip.CommonValues");
	String i18CGContext=EnoviaResourceBundle.getProperty(context,SUITE_KEY, languageStr,"emxProduct.Tooltip.CGContext");
	Vector vNames = new Vector(objectList.size());
	String strName = ""; 
	HashMap ctxCGInfo = null ;
	StringBuffer sbBuffer  = new StringBuffer(400);
	String strType  = "" ;
	String exportFormat = null;
	boolean exportToExcel = false;     
	HashMap requestMap = (HashMap)programMap.get("paramList");
	if(requestMap!=null && requestMap.containsKey("reportFormat")){
		exportFormat = (String)requestMap.get("reportFormat");
	}
	if("CSV".equals(exportFormat)){
		exportToExcel = true;
	}
	
	for ( int i = 0; i <  objectList.size() ; i++ )
	{
		sbBuffer = sbBuffer.delete(0,sbBuffer.length());
		ctxCGInfo = (HashMap)objectList.get(i) ; 
		strName = (String)ctxCGInfo.get("name") ; 
		String tempStrName = strName;
		strType = (String)ctxCGInfo.get("type") ; 
		if (strName == null){		
			strName = "" ;
		}
		 // Start - Specia Character - Added HTML equivalent code for &,<," & > - Bug No. 361962
		if(strName.indexOf("&") != -1 || strName.indexOf("<") != -1 || strName.indexOf(">") != -1 || strName.indexOf("\"") != -1 ){
			strName = FrameworkUtil.findAndReplace(strName , "&","&amp;");	
			strName = FrameworkUtil.findAndReplace(strName , "<", "&lt;");	
			strName = FrameworkUtil.findAndReplace(strName , ">", "&gt;");	
			strName = FrameworkUtil.findAndReplace(strName , "\"", "&quot;");	
		}
		 // End - Bug No. 361962
		if(exportToExcel && strName != null){
			vNames.add(tempStrName);			
		}
		else{
		if (strType != null && !"".equals(strType) && strType.equals("CommonGroup")){
				sbBuffer = sbBuffer.append("<img src=\"../common/images/")
                                .append(strCommonGroupIcon)
                                .append("\" border=\"0\"  align=\"middle\" ")
                                .append("TITLE=\"")
                                .append(" ")
                                .append(i18CG)
                                .append("\"")
                                .append("/><B>")
								.append(strName)
								.append("</B>") ; 
				strName = sbBuffer.toString();

			} else if (strType != null
					&& !"".equals(strType)
					&& (strType.equals("CommonValues") || mxType
							.isOfParentType(
									context,
									strType,
									ConfigurationConstants.TYPE_LOGICAL_STRUCTURES))) {
				//TODO- check when type will be logical Feature
				String strObjId = (String) ctxCGInfo.get("id");
				String i18Title=i18CGContext;
				String strConfigurableFeatureIcon = "";
				if (strType
						.equalsIgnoreCase(ConfigurationConstants.TYPE_SOFTWARE_FEATURE)
						|| ProductLineUtil.getChildrenTypes(context,
								ConfigurationConstants.TYPE_SOFTWARE_FEATURE)
								.contains(strType)) {
					strConfigurableFeatureIcon = "iconSmallSoftwareFeature.gif";
				} else {
					strConfigurableFeatureIcon = "iconSmallLogicalFeature.gif";
				}
				
				if (!strObjId.contains("*") && !strObjId.contains("&")) {
					DomainRelationship domRelId = new DomainRelationship(
							strObjId);
					StringList sLRelSelect = new StringList(
							ConfigurationConstants.SELECT_TO_TYPE);
					Hashtable htCGInfo = domRelId.getRelationshipData(context,
							sLRelSelect);
					StringList toType = (StringList) htCGInfo
							.get(ConfigurationConstants.SELECT_TO_TYPE);
					strType = (String) toType.get(0);
					if (mxType.isOfParentType( context , strType ,ConfigurationConstants.TYPE_CONFIGURATION_FEATURE))
						strConfigurableFeatureIcon = strConfigurationFeatureIcon;
					else
						strConfigurableFeatureIcon = strConfigurationOptionsIcon;
					i18Title=i18CV;
				}
				sbBuffer = sbBuffer.append("<img src=\"../common/images/")
						.append(strConfigurableFeatureIcon).append(
								"\" border=\"0\"  align=\"middle\" ").append(
								"TITLE=\"").append(" ").append(i18Title)
						.append("\"").append("/><B>").append(strName).append(
								"</B>");
				strName = sbBuffer.toString();
			}else if (strType != null && !"".equals(strType) && mxType.isOfParentType( context , strType ,ConfigurationConstants.TYPE_PRODUCTS) ){
			
				sbBuffer = sbBuffer.append("<img src=\"../common/images/")
                                .append(strProductIcon)
                                .append("\" border=\"0\"  align=\"middle\" ")
                                .append("TITLE=\"")
                                .append(" ")
                                .append(i18CGContext)
                                .append("\"")
                                .append("/><B>")
								.append(strName)
								.append("</B>");
			strName = sbBuffer.toString();
		}
		
		vNames.add(strName) ; 		
	}
	}
	 return vNames ; 
}

/**
* This method is to display "Type" Column value in View Common Group List Page Table 
* @param context     the eMatrix <code>Context</code> object
* @param args 		 Contains String array containing all the required parameters 
* @return			 Vector containing values of "Type"
* @throws FrameworkException
* @since R212
* @author IXH
* @category NON API
*/

public Vector getTypeInViewCGListPage(Context context , String[] args) throws Exception{


	HashMap programMap = (HashMap)JPO.unpackArgs(args);
	MapList objectList =  (MapList)programMap.get("objectList");
	Vector vTypes = new Vector(objectList.size());
	String strObjId = ""; 
	HashMap ctxCGInfo = null ;

	StringBuffer sbBuffer  = new StringBuffer(400);
	String strType = "" ;
	String strI18Type = "" ;
	for ( int i = 0; i <  objectList.size() ; i++ )
	{
		sbBuffer = sbBuffer.delete(0,sbBuffer.length());
		strType = "" ; 
		strI18Type = "" ; 
		ctxCGInfo = (HashMap)objectList.get(i) ; 

		strObjId = (String)ctxCGInfo.get("id") ; 
		if (!strObjId.contains("*") && !strObjId.contains("&") ){ 
			
			DomainRelationship domRelId = new DomainRelationship(strObjId);
			StringList sLRelSelect = new StringList("to."+ConfigurationConstants.SELECT_TYPE+"");
			Hashtable htCGInfo = domRelId.getRelationshipData(context,sLRelSelect);
			
			StringList slType = (StringList) htCGInfo.get("to."+ConfigurationConstants.SELECT_TYPE+"");
			strType = (String)slType.get(0);

			if (strType == null ){
				strType = "";				
				}
			if(!strType.isEmpty())
			 strI18Type = ConfigurationUtil.geti18FrameworkString(
					context, strType);
			}
		vTypes.add(strI18Type) ; 
	}
	return vTypes ; 
}

/**
* This method is to display "State" Column value in View Common Group List Page Table 
* @param context     the eMatrix <code>Context</code> object
* @param args 		 Contains String array containing all the required parameters 
* @return			 Vector containing values of "State"
* @throws FrameworkException
* @since R212
* @author IXH
* @category NON API
*/
public Vector getStateInViewCGListPage(Context context , String[] args) throws Exception{


	HashMap programMap = (HashMap)JPO.unpackArgs(args);
	MapList objectList =  (MapList)programMap.get("objectList");
	Vector vStates = new Vector(objectList.size());
	String strObjId = ""; 
	HashMap ctxCGInfo = null ;

	StringBuffer sbBuffer  = new StringBuffer(400);
	String strI18State = "" ;
	String strState = "" ;
	String strPolicy = "" ;
	String strLanguage = context.getSession().getLanguage();
	for ( int i = 0; i <  objectList.size() ; i++ )
	{
		sbBuffer = sbBuffer.delete(0,sbBuffer.length());
		strState = "" ;
		strPolicy = "" ;
		strI18State = "" ;
		ctxCGInfo = (HashMap)objectList.get(i) ; 

		strObjId = (String)ctxCGInfo.get("id") ; 
		if (!strObjId.contains("*") && !strObjId.contains("&") ){ 

			DomainRelationship domRelId = new DomainRelationship(strObjId);
			StringList sLRelSelect = new StringList("to."+ConfigurationConstants.SELECT_CURRENT);
			sLRelSelect.addElement("to."+DomainConstants.SELECT_POLICY);
			Hashtable htCGInfo = domRelId.getRelationshipData(context,sLRelSelect);

			StringList slState = (StringList)htCGInfo.get("to."+ConfigurationConstants.SELECT_CURRENT);
			StringList slPolicy = (StringList)htCGInfo.get("to."+ConfigurationConstants.SELECT_POLICY);
			strState = (String)slState.get(0);
			strPolicy = (String)slPolicy.get(0);
            String policy_Name= strPolicy.replace(' ', '_');
            String state_Name= strState.replace(' ', '_');
			String  stateKey = "emxFramework.State."+policy_Name+"."+state_Name;
			strI18State=EnoviaResourceBundle.getProperty(context,"Framework",stateKey,strLanguage);
			if (strI18State == null ){
				strI18State = "";				
			}
		}
		vStates.add(strI18State) ; 
	}
	return vStates ; 
}


/**
* This method is to display "Name" Column value in View Common Group List Page Table 
* @param context     the eMatrix <code>Context</code> object
* @param args 		 Contains String array containing all the required parameters 
* @return			 Vector containing values of "Name"
* @throws FrameworkException
* @since R212
* @author IXH
* @category NON API
*/

public Vector getNameInViewCGListPage(Context context , String[] args) throws Exception{


	HashMap programMap = (HashMap)JPO.unpackArgs(args);
	MapList objectList =  (MapList)programMap.get("objectList");
	Vector vNames = new Vector(objectList.size());
	String strObjId = ""; 
	HashMap ctxCGInfo = null ;

	StringBuffer sbBuffer  = new StringBuffer(400);
	String strName = "" ;

	for ( int i = 0; i <  objectList.size() ; i++ )
	{
		sbBuffer = sbBuffer.delete(0,sbBuffer.length());
		strName = "" ; 
		ctxCGInfo = (HashMap)objectList.get(i) ; 

		strObjId = (String)ctxCGInfo.get("id") ; 
		if (!strObjId.contains("*") && !strObjId.contains("&") ){ 

			DomainRelationship domRelId = new DomainRelationship(strObjId);
			StringList sLRelSelect = new StringList("to."+ConfigurationConstants.SELECT_NAME+"");
			Hashtable htCGInfo = domRelId.getRelationshipData(context,sLRelSelect);
		        
			StringList slName = (StringList)htCGInfo.get("to."+ConfigurationConstants.SELECT_NAME+"");
			strName = (String)slName.get(0);

			if (strName == null ){
				strName = "";				
				}
			}
		vNames.add(strName) ; 
	}
	return vNames ; 
}


/**
* This method is to display "Default Selection" attribute value in View Common Group List Page Table 
* @param context     the eMatrix <code>Context</code> object
* @param args 		 Contains String array containing all the required parameters 
* @return			 Vector containing values of "Default Selection" Yes /No
* @throws FrameworkException
* @since R212
* @author IXH
* @category NON API
*/

public Vector getDefaultSelectionInViewCGListPage(Context context , String[] args) throws Exception{

	HashMap programMap = (HashMap)JPO.unpackArgs(args);
	MapList objectList =  (MapList)programMap.get("objectList");
	Vector vDefaultSels = new Vector(objectList.size());
	String strObjId = ""; 
	HashMap ctxCGInfo = null ;

	StringBuffer sbBuffer  = new StringBuffer(400);
	String strDefaultSel = "" ;
	String i18StrDefaultSel = "" ;
	String strLanguage = context.getSession().getLanguage();

	for ( int i = 0; i <  objectList.size() ; i++ )	{
		sbBuffer = sbBuffer.delete(0,sbBuffer.length());
		strDefaultSel = "" ; 
		i18StrDefaultSel="";
		ctxCGInfo = (HashMap)objectList.get(i) ; 

		strObjId = (String)ctxCGInfo.get("id") ; 
		if (!strObjId.contains("*") && !strObjId.contains("&") ){ 

			DomainRelationship domRelConfOpt =  new DomainRelationship(strObjId);
			strDefaultSel= domRelConfOpt.getAttributeValue(context , ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION) ; 
			if (strDefaultSel == null ){
				strDefaultSel = "";				
			}
			if(!strDefaultSel.isEmpty()){
				String attributeName= ConfigurationConstants.ATTRIBUTE_DEFAULT_SELECTION.replace(' ', '_');
				String  rangeKey = "emxFramework.Range."+attributeName+"."+strDefaultSel;
				i18StrDefaultSel=EnoviaResourceBundle.getProperty(context,"Framework",rangeKey,strLanguage);
			}
		}
		vDefaultSels.add(i18StrDefaultSel) ; 
	}
	return vDefaultSels ; 
}

/**
* This method is to get "Variant Option" Column value in View Common Group List Page Table 
* @param context     the eMatrix <code>Context</code> object
* @param args 		 Contains String array containing all the required parameters 
* @return			 Vector containing values of Variant option
* @throws FrameworkException
* @since R212
* @author IXH
* @category NON API
*/
public Vector getVariantOptionInViewCGListPage(Context context , String[] args) throws Exception{

	//XSSOK Deprecated 
	HashMap programMap = (HashMap)JPO.unpackArgs(args);
	HashMap requestMap = (HashMap) programMap.get("paramList");
	String strRprtFrmt = (String) requestMap.get("reportFormat");
	 String exportFormat = null;
     boolean exportToExcel = false;
     if(requestMap!=null && requestMap.containsKey("reportFormat")){
    	 exportFormat = (String)requestMap.get("reportFormat");
     }
     if("CSV".equals(exportFormat)){
    	 exportToExcel = true;
     }
	String strstart = "";
	String strEnd = "";
	if(strRprtFrmt != null){
		strstart = "<a>";
		strEnd = "</a>";
	}
	MapList objectList =  (MapList)programMap.get("objectList");
	Vector vDVNames = new Vector(objectList.size());
	String strConfRelId = ""; 
	HashMap ctxCGInfo = null ;

	StringBuffer sbBuffer  = new StringBuffer(400);
	String strDVName = "" ;
    String languageStr = context.getSession().getLanguage();
	String i18VariantOption=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Tooltip.VariantOption",languageStr);
	
	for ( int i = 0; i <  objectList.size() ; i++ )
	{
		sbBuffer = sbBuffer.delete(0,sbBuffer.length());
		strDVName = "" ; 
		ctxCGInfo = (HashMap)objectList.get(i) ; 

		strConfRelId = (String)ctxCGInfo.get("id") ; 
		if (!strConfRelId.contains("*") && !strConfRelId.contains("&") ){ 

			DomainRelationship domRelId = new DomainRelationship(strConfRelId);
			StringList sLRelSelect = new StringList("from."+ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME+"");
			sLRelSelect.addElement(DomainObject.SELECT_FROM_TYPE);
			Hashtable htCGInfo = domRelId.getRelationshipData(context,sLRelSelect);
		        
			StringList slVariantOption = (StringList)htCGInfo.get("from."+ConfigurationConstants.SELECT_ATTRIBUTE_DISPLAY_NAME+"");
			strDVName = (String)slVariantOption.get(0);
			
			String strFromType="";
			if (!((StringList) htCGInfo.get(DomainObject.SELECT_FROM_TYPE)).isEmpty())
				strFromType = (String) ((StringList) htCGInfo.get(DomainObject.SELECT_FROM_TYPE)).get(0);
			
            String strConfigurableFeatureIcon="";						
			if (mxType.isOfParentType( context , strFromType ,ConfigurationConstants.TYPE_CONFIGURATION_FEATURE))
				strConfigurableFeatureIcon = strConfigurationFeatureIcon;
			else
				strConfigurableFeatureIcon = strConfigurationOptionsIcon;
			
		    if (strDVName!= null && !"".equals(strDVName)){
		    if(exportToExcel){
		    	sbBuffer.append(strDVName);
		    }else{
		    	sbBuffer.append(strstart);
		    	sbBuffer = sbBuffer.append("<img src=\"../common/images/")
		    	.append(strConfigurableFeatureIcon)
		    	.append("\" border=\"0\"  align=\"middle\" ")
		    	.append("TITLE=\"")
		    	.append(" ")
		    	.append(i18VariantOption)
		    	.append(" ")
		    	.append("\"")
		    	.append("/>")
		    	.append(strDVName);
		    	sbBuffer.append(strEnd);
		    }
			strDVName = sbBuffer.toString();
		}
		}
        if(strDVName == null)strDVName="";
        vDVNames.add(strDVName) ; 
	}
	return vDVNames ; 
}
}//end of class



