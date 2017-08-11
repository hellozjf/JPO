/*
 *  emxLibraryCentralUtilBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 * static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.28 Tue Oct 28 18:58:15 2008 przemek Experimental przemek $
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UISearch;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryCentralCommon;
import com.matrixone.apps.library.LibraryCentralConstants;



/**
 * The <code>emxLibraryCentralUtilBase</code> class contains utility methods for
 * getting data using configurable tables  in Library Central.
 *
 */
public class emxLibraryCentralUtilBase_mxJPO
{
   /**
     * Creates emxLibraryCentralUtilBase object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxLibraryCentralUtilBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns in
     * @throws Exception if the operation fails
     * @exclude
     */
public int mxMain(Context context, String[] args) throws Exception
{
    if (!context.isConnected())
        throw new Exception("not supported on desktop client");
    return 0;
}

/**
     * This Method returns the MapList of Part Families related to either the Part Library or Part Family.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0 - paramMap contains the objectId
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getSubclasses(Context context, String[] args) throws Exception
{
       MapList result = new MapList();
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String objectId    = (String)paramMap.get("objectId");
        DomainObject domObj = DomainObject.newInstance(context,objectId);
        SelectList selectStmts = new SelectList(1);
        selectStmts.addElement(DomainObject.SELECT_ID);
        MapList ResultMapList = new MapList();
        try{
           ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
           result= (MapList)domObj.getRelatedObjects(context,LibraryCentralConstants.RELATIONSHIP_SUBCLASS,LibraryCentralConstants.QUERY_WILDCARD,selectStmts,null,false,true,(short)1,null,null);
           int iSize = result.size();
           Map hashIDMap = null;
           for (int k=0;k<iSize;k++ )
           {
                hashIDMap = new HashMap();
                Map tempMap = (Map)result.get(k);
                String strObjectId = (String)tempMap.get("id");
                hashIDMap.put("id",strObjectId);
                ResultMapList.add(hashIDMap);
           }
       }catch(Exception exp){
            throw new Exception(exp.toString());
       }
        return ResultMapList;
}

  /**
     * This Method returns the MapList of All In Process Libraries (Which are not Approved).
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllInprocessLibraries(Context context, String[] args) throws Exception
{

    StringBuffer strBuffer = new StringBuffer();
    strBuffer.append("(type == '");
    strBuffer.append(LibraryCentralConstants.TYPE_DOCUMENT_LIBRARY);
    strBuffer.append("' && (");
    strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
    strBuffer.append(" == '");
    strBuffer.append(LibraryCentralConstants.STATE_CONTAINER_REV2_CREATE);
    strBuffer.append("' || ");
    strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
    strBuffer.append(" == '");
    strBuffer.append(LibraryCentralConstants.STATE_CONTAINER_REV2_REVIEW);
    strBuffer.append("')) || (type != '");
    strBuffer.append(LibraryCentralConstants.TYPE_DOCUMENT_LIBRARY);
    strBuffer.append("' && (");
    strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
    strBuffer.append(" == '");
    strBuffer.append(LibraryCentralConstants.STATE_LIBRARIES_INACTIVE);
    strBuffer.append("'))");
    MapList result = new MapList();
    try{
        ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
        StringBuffer buf = new StringBuffer();
        buf.append(LibraryCentralConstants.TYPE_LIBRARIES);
        buf.append(",");
        buf.append(LibraryCentralConstants.TYPE_DOCUMENT_LIBRARY);
        result= (MapList)getResult(context,buf.toString(),strBuffer.toString());
    }catch(Exception exp){
        throw new Exception(exp.toString());
    }
    return result;
}

    /**
     * This Method returns the MapList of In Process Document Libraries(Which are not Approved).
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllInprocessDocumentLibraries(Context context, String[] args) throws Exception
{
    StringBuffer strBuffer = new StringBuffer();
    strBuffer.append("(");
    strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
    strBuffer.append(" == '");
    strBuffer.append(LibraryCentralConstants.STATE_CONTAINER_REV2_CREATE);
    strBuffer.append("' || ");
    strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
    strBuffer.append(" == '");
    strBuffer.append(LibraryCentralConstants.STATE_CONTAINER_REV2_REVIEW);
    strBuffer.append("')");
    MapList result = new MapList();
    try{
         ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
         result= (MapList)getResult(context,LibraryCentralConstants.TYPE_DOCUMENT_LIBRARY,strBuffer.toString());

    }catch(Exception exp){
        throw new Exception(exp.toString());
    }
    return result;
}

/**
     * This Method returns the MapList of In Process General Libraries(Which are not Approved).
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
    */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllInprocessGeneralLibraries(Context context, String[] args) throws Exception
{
    StringBuffer buf = new StringBuffer();
    buf.append(LibraryCentralConstants.SELECT_CURRENT);
    buf.append(" == '");
    buf.append(LibraryCentralConstants.STATE_LIBRARIES_INACTIVE);
    buf.append("'");
    MapList result = new MapList();
    try{
        ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
        result= (MapList)getResult(context,LibraryCentralConstants.TYPE_GENERAL_LIBRARY,buf.toString());

    }catch(Exception exp){
        throw new Exception(exp.toString());
    }
    return result;
}
/**
     * This Method returns the MapList of In Process Part Libraries(Which are not Approved).
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
    */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllInprocessPartLibraries(Context context, String[] args) throws Exception
{
    StringBuffer buf = new StringBuffer();
    buf.append(LibraryCentralConstants.SELECT_CURRENT);
    buf.append(" == '");
    buf.append(LibraryCentralConstants.STATE_LIBRARIES_INACTIVE);
    buf.append("'");
    MapList result = new MapList();
    try{
        ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
        result= (MapList)getResult(context,LibraryCentralConstants.TYPE_PART_LIBRARY,buf.toString());

    }catch(Exception exp){
        throw new Exception(exp.toString());
    }
    return result;
}

   /**
     * This Method returns the MapList of All Libraries in all states.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllLibraries(Context context, String[] args) throws Exception
{
    String strWhere = null;
    MapList result = new MapList();
    try{

        ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
        StringBuffer buf = new StringBuffer();
        buf.append(LibraryCentralConstants.TYPE_LIBRARIES);
        buf.append(",");
        buf.append(LibraryCentralConstants.TYPE_DOCUMENT_LIBRARY);
        result= (MapList)getResult(context,buf.toString(),strWhere);
    }catch(Exception exp){
        throw new Exception(exp.toString());
    }
    return result;
}

   /**
     * This Method returns the MapList of All Document Libraries in all states.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllDocumentLibraries(Context context, String[] args) throws Exception
{
    String strWhere = null;
    MapList result = new MapList();
    try{
        ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
        result= (MapList)getResult(context,LibraryCentralConstants.TYPE_DOCUMENT_LIBRARY,strWhere);
    }catch(Exception exp){
        throw new Exception(exp.toString());
    }
    return result;
}
/**
     * This Method returns the MapList of All Part Libraries in all states.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
    */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllPartLibraries(Context context, String[] args) throws Exception
{
    String strWhere = null;
    MapList result = new MapList();
    try{
        ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
        result= (MapList)getResult(context,LibraryCentralConstants.TYPE_PART_LIBRARY,strWhere);
    }catch(Exception exp){
        throw new Exception(exp.toString());
    }
    return result;
}

   /**
     * This Method returns the MapList of All General Libraries in all states.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllGeneralLibraries(Context context, String[] args) throws Exception
{
    String strWhere = null;
    MapList result = new MapList();
    HashMap paramMap = (HashMap) JPO.unpackArgs(args);
    try{
        ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
        result= (MapList)getResult(context,LibraryCentralConstants.TYPE_GENERAL_LIBRARY,strWhere);

    }catch(Exception exp){
        throw new Exception(exp.toString());
    }
    return result;
}
/**
 * This method returns a MapList of all the types of Libraries in  "Active & Approved" state.
 * @param context
 * @param args
 * @since R216
 * @return a MapList containing object Ids of all the Libraries available in the database
 * @throws Exception
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllActiveLibraries(Context context, String[] args) throws Exception
{
    StringBuffer buf = new StringBuffer();
    buf.append(LibraryCentralConstants.TYPE_LIBRARIES);
    buf.append(",");
    buf.append(LibraryCentralConstants.TYPE_DOCUMENT_LIBRARY);
    StringBuffer whereBuf=new StringBuffer();
    whereBuf.append(LibraryCentralConstants.SELECT_CURRENT);
    whereBuf.append(" == '");
    whereBuf.append(LibraryCentralConstants.STATE_LIBRARIES_ACTIVE);
    whereBuf.append("' || ");
    whereBuf.append(LibraryCentralConstants.SELECT_CURRENT);
    whereBuf.append(" == '");
    whereBuf.append(LibraryCentralConstants.STATE_CONTAINER_REV2_APPROVED);
    whereBuf.append("'");
    MapList result = new MapList();
    try{
        ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
        result= (MapList)getResult(context,buf.toString(),whereBuf.toString());

    }catch(Exception exp){
        throw new Exception(exp.toString());
    }
    return result;
}
   /**
     * This Method returns the MapList of Parts/End Items related to Part Family/Class.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0 - paramMap contains objectId
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getClassifiedItems(Context context, String[] args) throws Exception
{
    MapList resultList = new MapList();
    MapList result = new MapList();

    String _STR_THRESHOLD_LIMIT = null;    

    try
    {
    	if (_STR_THRESHOLD_LIMIT == null)
    	{
    		_STR_THRESHOLD_LIMIT = FrameworkProperties.getProperty(context, "emxLibraryCentral.Search.ThresholdLimit");
    	}
    	//IR-241897V6R2012x
    	// ECL_R212_HF6 add limit
    	//set the default limit on the query
    	int iQueryLimit = Integer.valueOf(_STR_THRESHOLD_LIMIT);
    	int tempQueryLimit = iQueryLimit;
		if(iQueryLimit >= Short.MAX_VALUE)
		{
			tempQueryLimit = 0;
		}
    	SelectList selectStmts = new SelectList(1);
    	selectStmts.addElement(DomainObject.SELECT_ID);
    	HashMap paramMap = (HashMap) JPO.unpackArgs(args);
    	String objectId    = (String)paramMap.get("objectId");
    	DomainObject doObj = DomainObject.newInstance(context, objectId);

    	//Fix 375995 If this is classification type get the objects from 'Classified Item' relation
    	//Added condition to check for type and expand for bugs 348923 and 353143
    	String strType = doObj.getInfo(context,DomainConstants.SELECT_TYPE);
    	//Added OR condition to check for Type General class also for bug 355436
    	if(LibraryCentralCommon.isClassificationType(context, strType))
    	{
    		String classInterface = doObj.getInfo(context, "attribute[mxsysInterface].value");
			
			StringBuffer interfaceWhereClause = new StringBuffer();
			interfaceWhereClause.append("interface == ");
			interfaceWhereClause.append(classInterface);
			interfaceWhereClause.append(" && (!(interface matchlist \"");
			interfaceWhereClause.append(LibraryCentralConstants.INTERFACE_CLASSIFICATION_SEARCH_FILTER);
			interfaceWhereClause.append("\" \",\" ))");
			result = DomainObject.findObjects(context, "*", 
					"*", 
					"*", 
					"*",
					"*", 
					interfaceWhereClause.toString(), 
					"", 
					true, 
					selectStmts, 
					(short)tempQueryLimit);

			int iSize = result.size();
			if (iSize >= iQueryLimit)
			{
				String languageStr = context.getSession().getLanguage();
				String limitMessage=UINavigatorUtil.getI18nString("emxComponents.Warning.ObjectFindLimit","emxComponentsStringResource",languageStr);
				String limitReached=UINavigatorUtil.getI18nString("emxComponents.Warning.Reached","emxComponentsStringResource",languageStr);
				emxContextUtil_mxJPO.mqlNotice(context,limitMessage + String.valueOf(iQueryLimit) + limitReached);
			}
    	}
    	else
    	{
    		//Modified for bug 353143 - changed relationship to Subclass for expand
    		result = (MapList)doObj.getRelatedObjects(context,
    				LibraryCentralConstants.RELATIONSHIP_SUBCLASS,
    				LibraryCentralConstants.QUERY_WILDCARD,
    				selectStmts,
    				new StringList(),
    				true, 
    				false, 
    				(short)1, 
    				null, 
    				null,
    				iQueryLimit);
    	}
    	int iSize = result.size();
    	Map hashIDMap = null;
    	for (int k=0;k<iSize;k++ )
    	{
    		hashIDMap = new HashMap();
    		Map tempMap = (Map)result.get(k);
    		String strObjectId = (String)tempMap.get("id");
    		String typeName=new DomainObject(strObjectId).getInfo(context, LibraryCentralConstants.SELECT_TYPE);
    		hashIDMap.put("id",strObjectId);
			hashIDMap.put("type",typeName); //QBQ + sal3
    		if(typeName.equalsIgnoreCase(LibraryCentralConstants.TYPE_LIBRARY_FEATURE_PORT))
    			hashIDMap.put("disableSelection","true");
    		resultList.add(hashIDMap);
    	}

        }catch(Exception exp)
        {
            throw new Exception(exp.toString());
        }
        return resultList;
}

/**
     * This Method returns the MapList of objects where this end item is connected. This method
     * will be called in the Href of menu type_Part Where Used command.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0 - paramMap contains objectId
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getClassifiedItemWhereUsed(Context context, String[] args) throws Exception
{
       MapList resultList = new MapList();
        MapList result = new MapList();
        try{
            SelectList selectStmts = new SelectList(1);
            selectStmts.addElement(DomainObject.SELECT_ID);
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            String objectId    = (String)paramMap.get("objectId");
            DomainObject doObj = new DomainObject(objectId);
            StringBuffer strBuffer = new StringBuffer();
            strBuffer.append(" !(type matchlist '");
            strBuffer.append(PropertyUtil.getSchemaProperty(context, "type_GBOM"));
            strBuffer.append(",");
            strBuffer.append(PropertyUtil.getSchemaProperty(context, "type_Download"));
            strBuffer.append("' ',') ");
            StringBuffer strRelBuffer = new StringBuffer();
            strRelBuffer.append(" name != '");
            strRelBuffer.append(DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY);
            strRelBuffer.append("'");

            //Excluding GBOM objects from the list
            result = (MapList)doObj.getRelatedObjects(context,LibraryCentralConstants.QUERY_WILDCARD,
                                                      LibraryCentralConstants.QUERY_WILDCARD,
                                                      selectStmts,new StringList(),true, false, (short)1,
                                                      strBuffer.toString(), strRelBuffer.toString());
            int iSize = result.size();
            Map hashIDMap = null;
            for (int k=0;k<iSize;k++ )
            {
                hashIDMap = new HashMap();
                Map tempMap = (Map)result.get(k);
                String strObjectId = (String)tempMap.get("id");
                hashIDMap.put("id",strObjectId);
                resultList.add(hashIDMap);
            }

        }catch(Exception exp)
        {
            throw exp;
        }
        return resultList;
}
/**
 * This method returns all the objects of TYPE_CLASSIFICATION, Invoked when Classes
 * summary page is used.
 * @param context
 * @param args
 * @since R216
 * @return a MapList containing object Ids of all the Classes available in the database
 * @throws Exception
 */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllClasses(Context context, String[] args) throws Exception
{
        MapList result = new MapList();
        try{
            StringBuffer buf = new StringBuffer();
            buf.append(LibraryCentralConstants.TYPE_CLASSIFICATION);
            result = (MapList)getResult(context,buf.toString(),null);
             }catch(Exception exp){
                    throw new Exception(exp.toString());
            }
        return result;
}
/***
 * This method returns all the objects of TYPE_GENERAL_CLASS, Invoked from Classes
 * summary page, on choosing Filter General.
 * @param context
 * @param args
 * @since R216
 * @return a MapList containing object Ids of all the General Classes available in the database
 * @throws Exception
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllGeneralClasses(Context context, String[] args) throws Exception
{
        MapList result = new MapList();
        try{
            StringBuffer buf = new StringBuffer();
            buf.append(LibraryCentralConstants.TYPE_GENERAL_CLASS);
            result = (MapList)getResult(context,buf.toString(),null);
             }catch(Exception exp){
                    throw new Exception(exp.toString());
            }
        return result;
}
/***
 * This method returns all the objects of TYPE_PART_FAMILY, Invoked from Classes
 * summary page, on choosing Filter Part.
 * @param context
 * @param args
 * @return a MapList containing object Ids of all the Part Families available in the database
 * @since R216
 * @throws Exception
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllPartFamilies(Context context, String[] args) throws Exception
{
        MapList result = new MapList();
        try{
            StringBuffer buf = new StringBuffer();
            buf.append(LibraryCentralConstants.TYPE_PART_FAMILY);
            result = (MapList)getResult(context,buf.toString(),null);
             }catch(Exception exp){
                    throw new Exception(exp.toString());
            }
        return result;
}
/***
 * This method returns all the objects of TYPE_DOCUMENT_FAMILY, Invoked from Classes
 * summary page, on choosing Filter Document
 * @param context
 * @param args
 * @since R216
 * @return a MapList containing object Ids of all the Document Families available in the database
 * @throws Exception
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllDocumentFamilies(Context context, String[] args) throws Exception
{
        MapList result = new MapList();
        try{
            StringBuffer buf = new StringBuffer();
            buf.append(LibraryCentralConstants.TYPE_DOCUMENT_FAMILY);
            result = (MapList)getResult(context,buf.toString(),null);
             }catch(Exception exp){
                    throw new Exception(exp.toString());
            }
        return result;
}
/***
 * This method returns all the objects of TYPE_CLASSIFICATION in "Active & Approved" state. Invoked from Classes
 * summary page, on choosing Filter Active.
 * @param context
 * @param args
 * @since R216
 * @return a MapList containing object Ids of all the Classification types available in the database
 * @throws Exception
 */
@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllActiveClasses(Context context, String[] args) throws Exception
{
        MapList result = new MapList();
        StringBuffer strBuffer = new StringBuffer();
        try{
            StringBuffer buf = new StringBuffer();
            buf.append(LibraryCentralConstants.TYPE_CLASSIFICATION);
            strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
            strBuffer.append(" == '");
            strBuffer.append(LibraryCentralConstants.STATE_CLASSIFICATION_ACTIVE);
            strBuffer.append("' || ");
            strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
            strBuffer.append(" == '");
            strBuffer.append(LibraryCentralConstants.STATE_CONTAINER_REV2_APPROVED);
            strBuffer.append("'");
            result = (MapList)getResult(context,buf.toString(),strBuffer.toString());
             }catch(Exception exp){
                    throw new Exception(exp.toString());
            }
        return result;
}
 /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns MapList with objectIds
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllInProcessClasses(Context context, String[] args) throws Exception
{
        MapList result = new MapList();
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
        strBuffer.append(" == '");
        strBuffer.append(LibraryCentralConstants.STATE_CONTAINER_REV2_CREATE);
        strBuffer.append("' || ");
        strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
        strBuffer.append(" == '");
        strBuffer.append(LibraryCentralConstants.STATE_CONTAINER_REV2_REVIEW);
        strBuffer.append("' || ");
        strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
        strBuffer.append(" == '");
        strBuffer.append(LibraryCentralConstants.STATE_CLASSIFICATION_INACTIVE);
        strBuffer.append("'");
        try{
            ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
            StringBuffer buf = new StringBuffer();
            buf.append(LibraryCentralConstants.TYPE_CLASSIFICATION);
            result = (MapList)getResult(context,buf.toString(),strBuffer.toString());
             }catch(Exception exp){
                    throw new Exception(exp.toString());
            }
        return result;
}

 /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns MapList with objectIds
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllInProcessDocumentFamilies(Context context, String[] args) throws Exception
{
        MapList result = new MapList();
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
        strBuffer.append("  == '");
        strBuffer.append(LibraryCentralConstants.STATE_CONTAINER_REV2_CREATE);
        strBuffer.append("' || ");
        strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
        strBuffer.append(" == '");
        strBuffer.append(LibraryCentralConstants.STATE_CONTAINER_REV2_REVIEW);
        strBuffer.append("'");
        try{
                ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
                result = (MapList)getResult(context,LibraryCentralConstants.TYPE_DOCUMENT_FAMILY,strBuffer.toString());
             }catch(Exception exp){
                    throw new Exception(exp.toString());
            }
        return result;
}


 /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns MapList with objectIds
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllInProcessPartFamilies(Context context, String[] args) throws Exception
{
        MapList result = new MapList();
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
        strBuffer.append(" == '");
        strBuffer.append(LibraryCentralConstants.STATE_CLASSIFICATION_INACTIVE);
        strBuffer.append("'");
        try{
                ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
                result = (MapList)getResult(context,LibraryCentralConstants.TYPE_PART_FAMILY,strBuffer.toString());
             }catch(Exception exp){
                    throw new Exception(exp.toString());
            }
        return result;
}

 /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns MapList with objectIds
     * @throws Exception if the operation fails
     */

@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getAllInProcessGeneralClasses(Context context, String[] args) throws Exception
{
        MapList result = new MapList();
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append(LibraryCentralConstants.SELECT_CURRENT);
        strBuffer.append(" == '");
        strBuffer.append(LibraryCentralConstants.STATE_CLASSIFICATION_INACTIVE);
        strBuffer.append("'");
        try{
                ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
                result = (MapList)getResult(context,LibraryCentralConstants.TYPE_GENERAL_CLASS,strBuffer.toString());
             }catch(Exception exp){
                    throw new Exception(exp.toString());
            }
        return result;
}


    /**
     * This Method is a generic Method which uses the findObjects method of Domain Object. This method is called in all the methods which performs the
     * find objects functionality. i.e. in all the above specified methods.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strType the Type of Object on which the find objects method is called.
     * @param strWhere the Object where condition to be used while performing the find objects.
     * @returns MapList of Object IDs.
     * @throws Exception if the operation fails
     */

    public static MapList getResult(Context context,String strType,String strWhere)throws Exception
    {
        MapList result = new MapList();
        try{
            DomainObject domObj = new DomainObject();
            SelectList selectStmts = new SelectList(1);
            selectStmts.addElement(DomainObject.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_CURRENT);
            result= (MapList)domObj.findObjects(context,strType,LibraryCentralConstants.QUERY_WILDCARD,strWhere,selectStmts);
        }catch(Exception exp){
        	exp.printStackTrace();
            throw new Exception(exp.toString());
        }
        return result;
    }
    /***
     * 
     * @param context
     * @param args
     * @return
     * @throws Exception
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getClassifiedItemsSearch(Context context, String[] args)throws Exception
    {
        try
        {

            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            String  parentRel         = "relationship_ClassifiedItem";

        //Retrieve Search criteria
        //HashMap paramMap = (HashMap)programMap.get("paramMap");
        String  parentId          = (String) programMap.get("objectId");

        int intMatchlistLimit   =  Integer.parseInt((String)programMap.get("matchlistLimit"));
        StringList strlistInterfaceWhereClause = new StringList();

        String selType          = (String)programMap.get("txtTypeActual");
        String txtName          = (String)programMap.get("txtName");
        String txtRev           = (String)programMap.get("txtRev");
        String levelSelction    = (String)programMap.get("levelSelction");
        String txtVault   ="";
        String strVaults  ="";
        StringList strListVaults=new StringList();

        String txtVaultOption = (String)programMap.get("vaultSelction");
        if(txtVaultOption == null) {
          txtVaultOption = PersonUtil.SEARCH_ALL_VAULTS;
        }
        //get the vaults based upon vault option selection.
        txtVault = PersonUtil.getSearchVaults(context, true, txtVaultOption);


        //trimming
        txtVault = txtVault.trim();

        if("".equals(txtVault)) {
            txtVault = PersonUtil.getDefaultVault(context);
        }

        String queryLimit = (String)programMap.get("QueryLimit");
        String latestRevision = (String)programMap.get("latestRevision");
        StringBuffer strBuffer = new StringBuffer();
        strBuffer.append("");


        if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")){
           queryLimit = "0";
        }

        if (txtName == null || txtName.equalsIgnoreCase("null") || txtName.length() <= 0){
           txtName = LibraryCentralConstants.QUERY_WILDCARD;
        }

        if (txtRev == null || txtRev.equalsIgnoreCase("null") || txtRev.length() <= 0){
          txtRev = LibraryCentralConstants.QUERY_WILDCARD;
        }

        String txtOwner  = (String)programMap.get("txtOwner");
        String txtOriginator  = LibraryCentralConstants.QUERY_WILDCARD;
        String txtFormat = LibraryCentralConstants.QUERY_WILDCARD;
        String txtSearch = "";

        if(latestRevision != null) {
            strBuffer.append("(revision == last)");
        }

        //Start of "Interface Where condition" block

        DomainObject parentObj = new DomainObject(parentId);
        String strInterface = parentObj.getAttributeValue(context,LibraryCentralConstants.ATTRIBUTE_MXSYS_INTERFACE);
        if (!"".equals(strInterface))
        {
           if(!"".equals(strBuffer.toString())) {
                strBuffer.append(" && ");
            }

            if (levelSelction.equals("searchThisLevel"))
            {
                strBuffer.append("(interface == \"");
                strBuffer.append(strInterface);
                strBuffer.append("\")");

            }
            else if (levelSelction.equals("searchSubLevel"))
            {
                    String  strInterfaces = getInterfaceList(context,parentId);
                    if (!"".equals(strInterfaces))
                    {
                        strlistInterfaceWhereClause = chunkMatchlist(strInterfaces + "," +strInterface,
                                                                                        ",",
                                                                                        ",",
                                                                                        intMatchlistLimit);
                    }
                    else
                    {
                        strBuffer.append("(interface == \"");
                        strBuffer.append(strInterface);
                        strBuffer.append("\")");
                    }
            }

        }

        if (strBuffer.toString()!= null && !strBuffer.toString().equals(""))
        {
             strBuffer.append(" && ");
        }

        strBuffer.append(" (!(interface matchlist \"");
        strBuffer.append(LibraryCentralConstants.INTERFACE_CLASSIFICATION_SEARCH_FILTER);
        strBuffer.append("\" \",\" ))");

        //End of "Interface Where condition" block
        String advWhereExp = UISearch.getAdvanceSearchWhereExpression(context, programMap);
        if(!"".equals(advWhereExp))
        {
                if(!"".equals(strBuffer.toString())) {
                    strBuffer.append(" && ");
                }
                 strBuffer.append("(" + advWhereExp + ")");
        }

        StringList selectStmts = new StringList(1);
        selectStmts.addElement(DomainObject.SELECT_ID);

        //
            // For each of the short interface matchlist condition find the result
            // and then append it to previous result found.
            //
          MapList totalresultList       = new MapList();
          int intCountOfInterfaceWhereClause = strlistInterfaceWhereClause.size();

          if (intCountOfInterfaceWhereClause <= 0)
          {
                totalresultList = DomainObject.findObjects(context,
                                                       selType,
                                                       txtName,
                                                       txtRev,
                                                       txtOwner,
                                                       txtVault,
                                                       strBuffer.toString(),
                                                       null,
                                                       true,
                                                       selectStmts,
                                                       Short.parseShort(queryLimit),
                                                       txtFormat,
                                                       txtSearch);
          }
          else
          {
              ArrayList arrObjectIds = new ArrayList();
              String strOriginalWhereClause = strBuffer.toString();
              strBuffer = new StringBuffer();
              for (int i=0; i<intCountOfInterfaceWhereClause; i++)
              {
                  String strInterfaceWhereClause = (String)strlistInterfaceWhereClause.get(i);
                  strBuffer.append(strOriginalWhereClause);
                  strBuffer.append(" && ");
                  strBuffer.append(strInterfaceWhereClause);

                  MapList maplistResult = DomainObject.findObjects(context,
                                                       selType,
                                                       txtName,
                                                       txtRev,
                                                       txtOwner,
                                                       txtVault,
                                                       strBuffer.toString(),
                                                       null,
                                                       true,
                                                       selectStmts,
                                                       Short.parseShort(queryLimit),
                                                       txtFormat,
                                                       txtSearch);
                    int iResultsCount = maplistResult.size();
                    for(int k=0;k<iResultsCount;k++){
                        HashMap objectMap = (HashMap)maplistResult.get(k);
                        String strObjectId = (String)objectMap.get(DomainObject.SELECT_ID);
                         if(strObjectId != null && !arrObjectIds.contains(strObjectId)){
                             arrObjectIds.add(strObjectId);
                         }else{
                            maplistResult.remove(k);
                            k--;
                            iResultsCount --;

                         }
                    }
                 if (maplistResult!=null)
                 {
                     totalresultList.addAll(maplistResult);
                 }
              }//for !
          }//else !

          return totalresultList;

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }
 /**
 * This method gets the interface List
 *
 * @param context the eMatrix <code>Context</code> object
 * @param oid the objectId
 * @return String the Interfaces as a comma seperated string
 * @throws Exception if the operation fails
 */
   private static String getInterfaceList(Context context, String oid)throws Exception
    {
        // Use this approach rather than the "select derivative" one because this one
        // filters out classes that are not accessible to the user e.g. due to being
        // Inactive or Obsolete

        String strQuery     = "expand bus $1 from relationship $2 recurse to all select bus $3 dump $4";
        String expandData   = MqlUtil.mqlCommand(context, strQuery,
                                                    oid,
                                                    LibraryCentralConstants.RELATIONSHIP_SUBCLASS,
                                                    "attribute["+LibraryCentralConstants.ATTRIBUTE_MXSYSINTERFACE+"]",
                                                    ","
                                                );
        StringList rows     = FrameworkUtil.split(expandData, "\n");
        Iterator rowIter    = rows.iterator();
        StringList lst      = new StringList();
        while (rowIter.hasNext()) {
            String row = (String) rowIter.next();
            // something like: 2,Subclass,to,Part Family,BOLT,-,BOLT.1119550075029
            StringList fields = FrameworkUtil.split(row, ",");
            if (fields.size() != 7) {
                StringBuffer buf = new StringBuffer();
                buf.append("getInterfacesList: unexpected row data: ");
                buf.append(row);
                buf.append("for ");
                buf.append(oid);
                continue;
            }
            lst.add(fields.get(6));
        }
        String result = FrameworkUtil.join(lst, ",");
        return result;

    }


    /**
     * Breaks the bigger tokenlist of interface where clause into shorter tokenlists of multiple interface matchlist
     * where clauses.
     *
     * @param strTokenList The list of tokens separated with the delimiter given in strGivenTokenDelimiter parameter.
     *        If this parameter is null or "" then result is empty StringList object.
     * @param strGivenTokenDelimiter The delimiter used for separating the indivisual tokens in strTokenList.
     *        If this parameter is null or "" then result is empty StringList object.
     * @param strRequiredTokenDelimiter The delimiter to be used for separating the indivisual tokens in the result.
     *        If this parameter is null then it is considered as "" while concatinating the tokens in result.
     * @param intRequiredTokenCount The maximum no. of tokens present in each of the token list.
     *        If this parameter is <= 0 then result is empty StringList object.
     * @return Where clause containing multiple shorter matchlist conditions
     * @exclude
     */

    public static StringList chunkMatchlist(String strTokenList,
                                                           String strGivenTokenDelimiter,
                                                           String strRequiredTokenDelimiter,
                                                           int intRequiredTokenCount)
    {
        StringList strlistResult = new StringList();

    // Check the passed parameters
        if (strTokenList == null || "".equals(strTokenList))
        {
            return strlistResult; // Empty
        }
        if (strGivenTokenDelimiter == null || "".equals(strGivenTokenDelimiter))
        {
            System.err.println("Error : breakTokenList() : strGivenTokenDelimiter parameter is not given.");
            return strlistResult;
        }
        if (strRequiredTokenDelimiter == null)
        {
            strRequiredTokenDelimiter = "";
        }
        if (intRequiredTokenCount <= 0)
        {
            System.err.println("Error : breakTokenList() : intRequiredTokenCount parameter is invalid \""+intRequiredTokenCount+"\".");
            return strlistResult; // Empty
        }

    //Find the count of tokens in the given list
        ArrayList arrlistTokens = new ArrayList();
        StringTokenizer st = new StringTokenizer(strTokenList, strGivenTokenDelimiter);

        while (st.hasMoreTokens())
        {
            arrlistTokens.add(st.nextToken());
        }//while !

    // Form the resultant string of limited tokens
        StringBuffer sbuf = new StringBuffer(256);
        StringList strlistInterfaces = new StringList();
        int intTokenCount = 0;
        for(int i=0; i<arrlistTokens.size(); i++)
        {
            String strToken = (String)arrlistTokens.get(i);
            intTokenCount ++;

            if (intTokenCount <= intRequiredTokenCount)
            {
                if (sbuf.length() > 0)
                {
                    sbuf.append(strRequiredTokenDelimiter);
                }
                sbuf.append(strToken);
            }//if !
            else
            {
                strlistInterfaces.add(sbuf.toString());
                sbuf = new StringBuffer(256);
                intTokenCount = 1;
                sbuf.append(strToken);
            }//else !
        }//for !

        if (sbuf.length() > 0)
        {
            strlistInterfaces.add(sbuf.toString());
            sbuf = null;
        }

    //Form the matchlist condition

        for(int i=0; i<strlistInterfaces.size(); i++)
        {
            String strInterfaceList = (String)strlistInterfaces.get(i);
            sbuf = new StringBuffer(256);
            sbuf.append("(interface matchlist \"");
            sbuf.append(strInterfaceList);
            sbuf.append("\" \"");
            sbuf.append(strRequiredTokenDelimiter);
            sbuf.append("\")");
            strlistResult.add(sbuf.toString());

        }//for !
        strlistInterfaces.clear();
        strlistInterfaces = null;


        return strlistResult;
    }// chunkMatchlist(..) !

    /**
    * This method checks if the Classification paths has to be shown or not.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following list of arguments:
    *       0 - paramMap contains objectID
    * @return boolean true to show Classification path
    * @throws Exception if the operation fails
    */
    public boolean showClassificationPath(Context context,String[] args) throws Exception
    {
            boolean flDisplayField = false;
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            String strObjectId    = (String)paramMap.get("objectId");
            DomainObject doObj = new DomainObject(strObjectId);
            String strObjectType = doObj.getInfo(context,DomainConstants.SELECT_TYPE);
            BusinessType busType = new BusinessType(strObjectType,context.getVault());
            String strParentType = busType.getParent(context);
            if(strParentType != null && strParentType.equalsIgnoreCase(LibraryCentralConstants.TYPE_CLASSIFICATION))
            {
                flDisplayField = true;
            }
        return flDisplayField;
    }

    /**
     * commaPipeQueryToMap and commaPipeQueryToMapSkipTNR are used to run an
     * MQL query, which is expected to return records separated by pipe (|)
     * characters, and wherein fields are separated by commas (,). Each record
     * is expected to have in the first position (or Nth position for SkipN
     * variant) some kind of unique key (unique within the result set, anyway).
     * The values in this key position are then saved as keys in the outgoing
     * mapOut map. Values in the query result data in positions to the right of
     * that key become the value at that map position, stored as a StringList.
     * In the SkipTNR variant, the query is expected to be a temp query bus;
     * the T,N,R values in each record are discarded, and whatever is the first
     * slecect item is treated as the unique key.
     * The list of key values, in the order they came from the db, are returned
     * as a StringList. There is an option to run the query as superuser, by
     * passing in true for asRoot.
     *
     *  Example 1
     *  One way to use it is to select a single selectable that returns multiple
     *  values, e.g. "interfaces":
     *    HashMap map = new HashMap();
     *    StringList ids =
     *       commaPipeQueryToMapSkipTNR(context,
     *          "temp query bus Part * * select id interfaces dump , recordsep |",
     *          false, map, 3);
     *  The above builds a map of Part id's, wherein map.get(id) is a StringList
     *  (possibly empty, never null) of the interfaces on that particular Part.
     *  The SkipTNR variant is used here, as is generally always the case with
     *  temp query bus queries.
     *
     *  Example 2
     *  Another use is to select multiple selectables, in which case all but
     *  the last selectable must return exactly one value (e.g. name):
     *    HashMap map = new HashMap();
     *       commaPipeQueryToMap(context,
     *          "list interface * " +
     *           " select name kindof[Classification Taxonomies] derived" +
     *           " dump , recordsep |",
     *          true, map);
     *    This fills map with key=interface-name, value=TRUE/FALSE[,interface names]
     *    Each value is a StringList where the first element is always "TRUE" or "FALSE",
     *    and the remaining elements, if any, are the names of parent interfaces
     *
     * @param context - as named
     * @param cmd     - an MQL query to run
     * @param asRoot  - whether the query should run as superuser
     * @param mapOut  - a Map through which the results are returned
     * @param skip    - number of initial fields per record to discard
     * @return        - a StringList of the "key" names
     * @throws FrameworkException if the operation fails
     * @exclude
     **/

    public static  StringList commaPipeQueryToMapSkipTNR(Context context, String cmd,
            boolean asRoot, Map mapOut, String... vArgs) throws FrameworkException {
        return commaPipeQueryToMapSkipN(context, cmd, asRoot, mapOut, 3, vArgs);
    }

    /**
     *
     * See commaPipeQueryToMapSkipTNR
     *
     * @param context - as named
     * @param cmd     - an MQL query to run
     * @param asRoot  - whether the query should run as superuser
     * @param mapOut  - a Map through which the results are returned
     * @return        - a StringList of the "key" names
     * @throws FrameworkException
     * @exclude
     */
   public static  StringList commaPipeQueryToMap(Context context, String cmd,
           boolean asRoot, Map mapOut, String... vArgs) throws FrameworkException {
       return commaPipeQueryToMapSkipN(context, cmd, asRoot, mapOut, 0, vArgs);
   }

    /**
     * Called by commaPipeQueryToMap and commaPipeQueryToMapSkipTNR.
     * See those.  The actual work is done in this function, though.
     *
     * @param context - as named
     * @param cmd     - an MQL query to run
     * @param asRoot  - whether the query should run as superuser
     * @param mapOut  - a Map through which the results are returned
     * @param skip    - number of initial fields per record to discard
     * @return        - a StringList of the "key" names
     * @throws FrameworkException
     * @exclude
    */
    public static  StringList commaPipeQueryToMapSkipN(Context context, String cmd,
            boolean asRoot, Map mapOut, int skip, String... vArgs) throws FrameworkException {
        StringList names = new StringList();
        String qryData = MqlUtil.mqlCommand(context, cmd, asRoot, vArgs).trim();
        StringList recordsDataList = FrameworkUtil.split(qryData, "|");
        Iterator recordsDataIter = recordsDataList.iterator();
        while (recordsDataIter.hasNext()) {
            String recordData = (String) recordsDataIter.next();
            StringList fields = FrameworkUtil.split(recordData, ",");
            String name = (String)fields.get(skip);
            names.add(name);
            if (mapOut != null) {
                fields.subList(0, skip+1).clear();
                mapOut.put(name, fields);
            }
        }
        return names;
    }


    /**
     * This method gets For a given item the interfaces implemented by that Item which pertain to
     * Classification.  Any other interfaces are ignored.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param endItemId the objectId
     * @return StringList of interface names
     * @throws Exception if the operation fails
     */
    public static StringList getClassificationInterfaces(Context context,
            String endItemId) throws Exception {
        StringList classificationInterfaces = new StringList();
        // First, get all of the object's interfaces, presumably at least
        // one of which will be classification related, and possibly
        // others not.
        String strCmd           = "print bus $1 select interface dump $2";
        String interfacesData   = MqlUtil.mqlCommand(context, strCmd, true, endItemId, ",").trim();

        if (interfacesData.length() == 0) {
            // Guess the item is not classified at all.
            return classificationInterfaces;  // which is empty at this time
        }

        StringList interfaces = FrameworkUtil.split(interfacesData, ",");

        // Now determine which of those interfaces are classification related
        HashMap clsif2kindof        = new HashMap();
        strCmd                      = "list interface '$1' select name $2 dump $3 recordsep $4";
        StringList kindofDataList   = commaPipeQueryToMap(context,strCmd, true, clsif2kindof,
                                                            FrameworkUtil.join(interfaces, ","),
                                                            "kindof",
                                                            ",",
                                                            "|"
                                                         );

        // Now examine each record, see if it pertains to classification;
        // all classification interfaces are kindof TAXONOMIES.
        Iterator kindofDataIter = kindofDataList.iterator();
        while (kindofDataIter.hasNext()) {
            String clsIf = (String) kindofDataIter.next();
            StringList kindofList = (StringList) clsif2kindof.get(clsIf);
            if (kindofList.contains(DomainConstants.INTERFACE_CLASSIFICATION_TAXONOMIES) ||
                kindofList.contains(DomainConstants.INTERFACE_CLASSIFICATION_ORPHANS))
            {
                classificationInterfaces.addElement(clsIf);
            }
        }
        return classificationInterfaces;
    }


    /**
     * This method gets the Part name.
     * Name column should be value of a specific attribute for certain types of objects
     * for the rest it is "name"
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0 - contains objectId
     * @return Vector of html string that is link to the object
     * @throws Exception if the operation fails
     */
    public Vector getName(Context context, String[] args) throws Exception
    {
        Vector vName = new Vector();
        try
        {
            HashMap programMap          = (HashMap) JPO.unpackArgs(args);
            MapList objectList          = (MapList)programMap.get("objectList");
            Map objectMap               = null;
            String type                 = "";
            String plmexternalID        = "";
            String objID                = "";
            String displayValue         = "";

            for(int i = 0 ; i < objectList.size()  ; i++)
            {
                objectMap               = (Map)objectList.get(i);
                objID                   = (String) objectMap.get(DomainConstants.SELECT_ID);
                DomainObject domObj     = DomainObject.newInstance(context,objID);

                SelectList selectStmts  = new SelectList(3);
                selectStmts.addElement(DomainObject.SELECT_NAME);
                selectStmts.addElement(DomainObject.SELECT_TYPE);
                selectStmts.addElement("attribute[PLMEntity.PLM_ExternalID]");
                selectStmts.addElement("attribute[PLMEntity.V_Name]");

                Map objectSelectMap     = domObj.getInfo(context,selectStmts);
                type                    = (String) objectSelectMap.get(DomainObject.SELECT_TYPE);
                displayValue            = (String) objectSelectMap.get(DomainObject.SELECT_NAME);
                plmexternalID           = (String) objectSelectMap.get("attribute[PLMEntity.PLM_ExternalID]");
                if (type != null && !"".equals(type) && !"null".equals(type) && type.equals("CATComponentsFamilyProxyToElement"))
                {
                  // For a Document the Name will be in the Title object
                    displayValue = plmexternalID;
                }
                //For Port V_Name would be used
                if(UIUtil.isNotNullAndNotEmpty(type) && type.equalsIgnoreCase(LibraryCentralConstants.TYPE_LIBRARY_FEATURE_PORT))
                {
                	displayValue=(String) objectSelectMap.get("attribute[PLMEntity.V_Name]");
                }
                vName.addElement(displayValue);
            }

           return vName;
        }
        catch(Exception e)
        {
           e.printStackTrace();
           throw e;
        }
    }

    /**
     * This method checks for the LIB or LBC licences and gets the documents in specified Book
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following list of arguments:
     *      0 - contains objectId
     * @return MapList Object of ObjectIds of Documents
     * @throws Exception if the operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getRelatedDocuments(Context context, String[] args)throws Exception{
        try{
            ComponentsUtil.checkLicenseReserved(context,LibraryCentralConstants.LIB_LBC_PRODUCT_TRIGRAM);
            emxCommonDocumentUI_mxJPO commonDocumentUIJPO = new emxCommonDocumentUI_mxJPO(context,args);
            return commonDocumentUIJPO.getDocuments(context, args);
        }catch(Exception exp){
            throw new Exception(exp.toString());
        }
    }
    
    /***
     * This method is used to color the state column in Libraries & Classes Summary Page
     * State column would be painted with following colors
     * --Yellow--Inactive, Create, Review
     * --Green--Active
     * --Red--Locked,Obsolete.
     * Invoked when All Filter is chosen in Libraries & Classes summary page
     * @param context
     * @param args
     * @since R216
     * @return StringList containing the color from default stylesheet
     * @throws FrameworkException
     */
    
    public StringList getStyleForState(Context context,String args[]) throws FrameworkException{
    	StringList styleSheet=new StringList();
    	try{
    		HashMap programMap=JPO.unpackArgs(args);
    		MapList objectList=(MapList)programMap.get("objectList");
    		HashMap paramListMap=(HashMap)programMap.get("paramList");
			HashMap requestValuesMap=(HashMap)paramListMap.get("RequestValuesMap");
			//IR-245521V6R2014x, Initially it was decied to show colors only When All Filter
			//was choosen by the user, Later on it was decided that colors should be displayed
			//when all types of Filters are chosen, hence removing the check for All Filters
			applyStyleSheet(context,styleSheet, objectList,paramListMap.containsKey("firstTime"));

		}catch (Exception e) {
			e.printStackTrace();
		}
		return styleSheet;
	}

	/***
	 * This method applies the style sheet based on state of the object.
	 * @param context
	 * @param styleSheet
	 * @param objectList
	 * @param isMassPromoteDemote
	 * @throws FrameworkException
	 * @throws Exception
	 */
	private void applyStyleSheet(Context context,StringList styleSheet, MapList objectList, boolean isMassPromoteDemote)throws FrameworkException,Exception {
		Iterator itr=objectList.iterator();
		while(itr.hasNext()){
			Map objectMap=(HashMap)itr.next();
			//IR-243352V6R2014x, when user chooses Mass promote/demote, the state was changed, but color
			//was not getting reflected because, for the new changed state appropriate stylesheet was not applied.
			//Hence getting the new state for the selected objects and applying style sheet
			if(isMassPromoteDemote){
				String current=new DomainObject((String)objectMap.get(DomainConstants.SELECT_ID)).getInfo(context, DomainConstants.SELECT_CURRENT);
				setStyleonMassPromoteDemote(styleSheet, current);
			}
			else{
				if(objectMap.get(DomainConstants.SELECT_CURRENT).equals(LibraryCentralConstants.STATE_CLASSIFICATION_INACTIVE)
						||objectMap.get(DomainConstants.SELECT_CURRENT).equals(LibraryCentralConstants.STATE_CONTAINER_REV2_CREATE)
						||objectMap.get(DomainConstants.SELECT_CURRENT).equals(LibraryCentralConstants.STATE_CONTAINER_REV2_REVIEW))
					styleSheet.add("ResourcePlanningYellowBackGroundColor");
				else if(objectMap.get(DomainConstants.SELECT_CURRENT).equals(LibraryCentralConstants.STATE_CLASSIFICATION_ACTIVE)
						||objectMap.get(DomainConstants.SELECT_CURRENT).equals(LibraryCentralConstants.STATE_CONTAINER_REV2_APPROVED))
					styleSheet.add("ResourcePlanningGreenBackGroundColor");
				else if(objectMap.get(DomainConstants.SELECT_CURRENT).equals(LibraryCentralConstants.STATE_CONTAINER_REV2_OBSOLETE)
						||objectMap.get(DomainConstants.SELECT_CURRENT).equals(LibraryCentralConstants.STATE_CONTAINER_REV2_LOCKED))
					styleSheet.add("ResourcePlanningRedBackGroundColor");
				else
					styleSheet.add("");

			}
		}
	}

	/***
	 * This method would set the style when Mass Promote/Demote is performed
	 * @param styleSheet
	 * @param current
	 */
	private void setStyleonMassPromoteDemote(StringList styleSheet,
			String current) {
		if(current.equals(LibraryCentralConstants.STATE_CLASSIFICATION_INACTIVE)
				||current.equals(LibraryCentralConstants.STATE_CONTAINER_REV2_CREATE)
				||current.equals(LibraryCentralConstants.STATE_CONTAINER_REV2_REVIEW))
			styleSheet.add("ResourcePlanningYellowBackGroundColor");
		else if(current.equals(LibraryCentralConstants.STATE_CLASSIFICATION_ACTIVE)
				||current.equals(LibraryCentralConstants.STATE_CONTAINER_REV2_APPROVED))
			styleSheet.add("ResourcePlanningGreenBackGroundColor");
		else if(current.equals(LibraryCentralConstants.STATE_CONTAINER_REV2_OBSOLETE)
				||current.equals(LibraryCentralConstants.STATE_CONTAINER_REV2_LOCKED))
			styleSheet.add("ResourcePlanningRedBackGroundColor");
	}

	
	/**	RESTful Web Service Method
     * 
     * @param context	:	
     * @param args		:	contains Object ID
     * 
     * @return			:	Map containing Object details
     * @throws Exception
     */
    public Map<String, String> getLBCObject(Context context, String[] args) throws Exception
    {
        try
        {
            StringList objectSelects = new StringList();
    		objectSelects.addElement(DomainObject.SELECT_TYPE);
    		objectSelects.addElement(DomainObject.SELECT_NAME);
    		objectSelects.addElement(DomainObject.SELECT_REVISION);
    		objectSelects.addElement(DomainObject.SELECT_OWNER);
    		objectSelects.addElement(DomainObject.SELECT_VAULT);
    		objectSelects.addElement(DomainObject.SELECT_CURRENT);
    		objectSelects.addElement(DomainObject.ATTRIBUTE_TITLE);
    		objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
    		objectSelects.addElement(LibraryCentralConstants.SELECT_PHYSICALID);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.ATTRIBUTE_COUNT+"].value");
    		
    		DomainObject obj = new DomainObject(args[0].trim());
    		Map<String,String> objInfo = obj.getInfo(context, objectSelects);
    		return objInfo;
        }
        catch(Exception exp)
        {
            throw new Exception(exp.toString());
        }
    }
    
    /**	RESTful Web Service Method
     * 
     * @param context	:	
     * @param args		:
     * 
     * @return			:	MapList containing all the Libraries with their corresponding details
     * @throws Exception
     */
    public MapList getLibraries(Context context, String[] args) throws Exception
    {
        try
        {
            String strWhere = null;
            MapList result = new MapList();
            MapList libList = new MapList();
            
            StringBuffer buf = new StringBuffer();
            buf.append(LibraryCentralConstants.TYPE_LIBRARIES);
            buf.append(",");
            buf.append(LibraryCentralConstants.TYPE_DOCUMENT_LIBRARY);
            result= (MapList)getResult(context,buf.toString(),strWhere);

    		HashMap<String,String> libMap = new HashMap<String,String>();
    		Map<String,String> libInfo = null;
    		StringList objectSelects = new StringList();
    		objectSelects.addElement(DomainObject.SELECT_TYPE);
    		objectSelects.addElement(DomainObject.SELECT_NAME);
    		objectSelects.addElement(DomainObject.SELECT_REVISION);
    		objectSelects.addElement(DomainObject.SELECT_OWNER);
    		objectSelects.addElement(DomainObject.SELECT_VAULT);
    		objectSelects.addElement(DomainObject.SELECT_CURRENT);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.SELECT_TITLE+"].value");
    		objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
    		objectSelects.addElement(LibraryCentralConstants.SELECT_PHYSICALID);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.ATTRIBUTE_COUNT+"].value");
    		
    		Iterator<?> mapListIter = result.iterator();
    		String id = "";
    		DomainObject obj = null;
    		while(mapListIter.hasNext())
    		{
    			libMap = (HashMap)mapListIter.next();
    			id = libMap.get("id");
    			obj = new DomainObject(id);
    			libInfo = obj.getInfo(context, objectSelects);
    			
    			libList.add(libInfo);
    		}
    	    return libList;
        }
        catch(Exception exp)
        {
            throw new Exception(exp.getMessage());
        }
    }
    
    /**	RESTful Web Service Method
     * 
     * @param context	:	
     * @param args		:
     * 
     * @return			:	MapList containing all the Classes with their corresponding details
     * @throws Exception
     */
    public MapList getAllClassesUsingREST(Context context, String[] args) throws Exception
    {
        try
        {
            String strWhere = null;
            MapList result = new MapList();
            MapList classesList = new MapList();
            
            StringBuffer buf = new StringBuffer();
            buf.append(LibraryCentralConstants.TYPE_CLASSIFICATION);
            result= (MapList)getResult(context,buf.toString(),strWhere);

    		HashMap<String,String> classesMap = new HashMap<String,String>();
    		Map<String,String> classInfo = null;
    		StringList objectSelects = new StringList();
    		objectSelects.addElement(DomainObject.SELECT_TYPE);
    		objectSelects.addElement(DomainObject.SELECT_NAME);
    		objectSelects.addElement(DomainObject.SELECT_REVISION);
    		objectSelects.addElement(DomainObject.SELECT_OWNER);
    		objectSelects.addElement(DomainObject.SELECT_VAULT);
    		objectSelects.addElement(DomainObject.SELECT_CURRENT);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.SELECT_TITLE+"].value");
    		objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
    		objectSelects.addElement(LibraryCentralConstants.SELECT_PHYSICALID);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.ATTRIBUTE_COUNT+"].value");
    		
    		Iterator<?> mapListIter = result.iterator();
    		String id = "";
    		DomainObject obj = null;
    		while(mapListIter.hasNext())
    		{
    			classesMap = (HashMap)mapListIter.next();
    			id = classesMap.get("id");
    			obj = new DomainObject(id);
    			classInfo = obj.getInfo(context, objectSelects);
    			
    			classesList.add(classInfo);
    		}
    	    return classesList;
        }
        catch(Exception exp)
        {
            throw new Exception(exp.getMessage());
        }
    }
    
    /**	RESTful Web Service Method
     * 
     * @param context	:	
     * @param args		:	contains Object ID
     * 
     * @return			:	MapList containing Classes and their details, in the given Object (Library/Class)
     * @throws Exception
     */
    public MapList getSubClassesUsingREST(Context context, String[] args) throws Exception
    {
        try
        {
            MapList classList = new MapList();
            DomainObject obj = new DomainObject(args[0].trim());

    		StringList objectSelects = new StringList();
    		objectSelects.addElement(DomainObject.SELECT_TYPE);
    		objectSelects.addElement(DomainObject.SELECT_NAME);
    		objectSelects.addElement(DomainObject.SELECT_REVISION);
    		objectSelects.addElement(DomainObject.SELECT_OWNER);
    		objectSelects.addElement(DomainObject.SELECT_VAULT);
    		objectSelects.addElement(DomainObject.SELECT_CURRENT);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.SELECT_TITLE+"].value");
    		objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
    		objectSelects.addElement(LibraryCentralConstants.SELECT_PHYSICALID);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.ATTRIBUTE_COUNT+"].value");
    		
    		classList = obj.getRelatedObjects(context, LibraryCentralConstants.RELATIONSHIP_SUBCLASS, LibraryCentralConstants.QUERY_WILDCARD, objectSelects, null, false, true, (short)1, "", "", 0);
    		
    	    return classList;
        }
        catch(Exception exp)
        {
            throw new Exception(exp.getMessage());
        }
    }
    
    /**	RESTful Web Service Method
     * 
     * @param context	:	
     * @param args		:	contains Object ID
     * 
     * @return			:	MapList containing End Items and their details, in a given Class
     * @throws Exception
     */
    public MapList getEndItems(Context context, String[] args) throws Exception
    {
        try
        {
            MapList endItemList = new MapList();
            DomainObject obj = new DomainObject(args[0].trim());

    		StringList objectSelects = new StringList();
    		objectSelects.addElement(DomainObject.SELECT_TYPE);
    		objectSelects.addElement(DomainObject.SELECT_NAME);
    		objectSelects.addElement(DomainObject.SELECT_REVISION);
    		objectSelects.addElement(DomainObject.SELECT_OWNER);
    		objectSelects.addElement(DomainObject.SELECT_VAULT);
    		objectSelects.addElement(DomainObject.SELECT_CURRENT);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.SELECT_TITLE+"].value");
    		objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
    		objectSelects.addElement(LibraryCentralConstants.SELECT_PHYSICALID);
    		
    		endItemList = obj.getRelatedObjects(context, LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM, LibraryCentralConstants.QUERY_WILDCARD, objectSelects, null, false, true, (short)1, "", "", 0);
    		
    	    return endItemList;
        }
        catch(Exception exp)
        {
            throw new Exception(exp.getMessage());
        }
    }
    
    
    /**	RESTful Web Service Method
     * 
     * @param context	:	
     * @param args		:	contains Object ID
     * 
     * @return			:	Name of the given Object (Library/Class/EndItem)
     * @throws Exception
     */
    public String getObjectName (Context context, String[] args) throws Exception
    {
        try
        {
        	DomainObject obj = new DomainObject(args[0].trim());
        	String objName = obj.getInfo(context, DomainObject.SELECT_NAME);
        	
        	return objName;
        }
        catch(Exception exp)
        {
            throw new Exception(exp.getMessage());
        }
    }
    
    /**	RESTful Web Service Method
     * 
     * @param context	:	
     * @param args		:	contains Object ID
     * 
     * @return			:	Map containing details of the immediate Parent, of the given object.
     * @throws Exception
     */
    public Map<String, String> getParent(Context context, String[] args) throws Exception
    {
        try
        {
        	DomainObject obj = new DomainObject(args[0].trim());
        	
        	MapList parentList = new MapList();
        	StringList objectSelects = new StringList();
    		objectSelects.addElement(DomainObject.SELECT_TYPE);
    		objectSelects.addElement(DomainObject.SELECT_NAME);
    		objectSelects.addElement(DomainObject.SELECT_REVISION);
    		objectSelects.addElement(DomainObject.SELECT_OWNER);
    		objectSelects.addElement(DomainObject.SELECT_VAULT);
    		objectSelects.addElement(DomainObject.SELECT_CURRENT);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.SELECT_TITLE+"].value");
    		objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
    		objectSelects.addElement(LibraryCentralConstants.SELECT_PHYSICALID);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.ATTRIBUTE_COUNT+"].value");
    		
    		if(obj.isKindOf(context, LibraryCentralConstants.TYPE_CLASSIFICATION))
    		{
    			parentList = obj.getRelatedObjects(context, LibraryCentralConstants.RELATIONSHIP_SUBCLASS, LibraryCentralConstants.QUERY_WILDCARD, objectSelects, null, true, false, (short)1, "", "", 0);
    		}
    		else
    		{
    			parentList = obj.getRelatedObjects(context, LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM, LibraryCentralConstants.QUERY_WILDCARD, objectSelects, null, true, false, (short)1, "", "", 0);
    		}
    		
    		Iterator iter = parentList.iterator();
    		Map<String, String> parentMap = null;
    		while(iter.hasNext())
    		{
    			parentMap = (Map)iter.next();
    			String id = parentMap.get(LibraryCentralConstants.SELECT_PHYSICALID);
    			DomainObject domainObj = new DomainObject(id);
    			if(domainObj.isKindOf(context, LibraryCentralConstants.TYPE_CLASSIFICATION))
    			{
    				parentMap.put("isLibrary", "false");
    			}
    			else
    			{
    				parentMap.put("isLibrary", "true");
    			}
    		}
    		
    		return parentMap;
        }
        catch(Exception exp)
        {
            throw new Exception(exp.toString());
        }
    } 
    
    /**	RESTful Web Service Method
     * 
     * @param context	:	
     * @param args		:	contains Object ID
     * 
     * @return			:	MapList containing Classes and their details, connected to the given end item
     * @throws Exception
     */
    public MapList getClasses(Context context, String[] args) throws Exception
    {
        try
        {
        	DomainObject obj = new DomainObject(args[0].trim());
        	
        	MapList parentList = new MapList();
        	StringList objectSelects = new StringList();
    		objectSelects.addElement(DomainObject.SELECT_TYPE);
    		objectSelects.addElement(DomainObject.SELECT_NAME);
    		objectSelects.addElement(DomainObject.SELECT_REVISION);
    		objectSelects.addElement(DomainObject.SELECT_OWNER);
    		objectSelects.addElement(DomainObject.SELECT_VAULT);
    		objectSelects.addElement(DomainObject.SELECT_CURRENT);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.SELECT_TITLE+"].value");
    		objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
    		objectSelects.addElement(LibraryCentralConstants.SELECT_PHYSICALID);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.ATTRIBUTE_COUNT+"].value");
    		
    		parentList = obj.getRelatedObjects(context, LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM, LibraryCentralConstants.QUERY_WILDCARD, objectSelects, null, true, false, (short)1, "", "", 0);

    		return parentList;
        }
        catch(Exception exp)
        {
            throw new Exception(exp.toString());
        }
    } 
    
    /**	RESTful Web Service Method
     * 
     * @param context	:	
     * @param args		:	contains Object ID
     * 
     * @return			:	MapList containing Documents connected to objects as "Reference Document" 
     * @throws Exception
     */
    public MapList getReferenceDocument(Context context, String[] args) throws Exception
    {
        try
        {
        	DomainObject obj = new DomainObject(args[0].trim());
        	
        	MapList documentList = new MapList();
        	StringList objectSelects = new StringList();
    		objectSelects.addElement(LibraryCentralConstants.SELECT_PHYSICALID);
    		objectSelects.addElement(LibraryCentralConstants.SELECT_ID);
    		Pattern relPattern        = new Pattern("");
    		relPattern.addPattern(PropertyUtil.getSchemaProperty(context, CommonDocument.SYMBOLIC_relationship_ReferenceDocument));
    		documentList = obj.getRelatedObjects(context, relPattern.getPattern(), "*", objectSelects, null, false, true, (short)1, "", "",0);

    		return documentList;
        }
        catch(Exception exp)
        {
            throw new Exception(exp.toString());
        }
    } 

	
    public Map<String, String> getLibForClass(Context context, String[] args) throws Exception
    {
        try
        {
            MapList parentList = new MapList();
            Map<String, String> parentLibMap = null;
            DomainObject obj = new DomainObject(args[0].trim());

    		StringList objectSelects = new StringList();
    		objectSelects.addElement(DomainObject.SELECT_TYPE);
    		objectSelects.addElement(DomainObject.SELECT_NAME);
    		objectSelects.addElement(DomainObject.SELECT_REVISION);
    		objectSelects.addElement(DomainObject.SELECT_OWNER);
    		objectSelects.addElement(DomainObject.SELECT_VAULT);
    		objectSelects.addElement(DomainObject.SELECT_CURRENT);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.SELECT_TITLE+"].value");
    		objectSelects.addElement(DomainObject.SELECT_DESCRIPTION);
    		objectSelects.addElement(LibraryCentralConstants.SELECT_PHYSICALID);
    		objectSelects.addElement("attribute["+LibraryCentralConstants.ATTRIBUTE_COUNT+"].value");
    		
    		if(obj.isKindOf(context, LibraryCentralConstants.TYPE_CLASSIFICATION))
    		{
    			parentList = obj.getRelatedObjects(context, LibraryCentralConstants.RELATIONSHIP_SUBCLASS, LibraryCentralConstants.QUERY_WILDCARD, objectSelects, null, true, false, (short)-1, "", "", 0);
    			if(parentList.size()>=1)
				{
    				Map<String, String> parentMap = (Map<String, String>) parentList.get(0);
					String id = parentMap.get(LibraryCentralConstants.SELECT_PHYSICALID);
					
					DomainObject libObject = new DomainObject(id);
		    		if(libObject.isKindOf(context, LibraryCentralConstants.TYPE_LIBRARIES))
						return parentMap; 
    				
					}
				}

    	    return parentLibMap;
        }
        catch(Exception exp)
        {
            throw new Exception(exp.getMessage());
        }
    }

    /**	RESTful Web Service Method
     * 
     * @param context	:	
     * @param args		:	contains Object ID
     * 
     * @return			:	MapList containing libraries and their details, in the given Object (Classified Item or Class)
     * @throws Exception
     */

	public MapList getParentLibrary(Context context, String[] args) throws Exception
    {
        try
        {
			System.out.println("Start getParentLibrary");
            MapList parentList = new MapList();
			MapList parentLibList = new MapList();
			Map<String, String> parentLibMap = null;
            DomainObject obj = new DomainObject(args[0].trim());
			StringList objectSelects = new StringList();
    		objectSelects.addElement(LibraryCentralConstants.SELECT_PHYSICALID);

    		if(obj.isKindOf(context, LibraryCentralConstants.TYPE_CLASSIFICATION))
    		{
    			 parentLibMap= getLibForClass(context,args);
    			 parentLibList.add(parentLibMap);
    			
    		}
    		else
    		{
    			parentList = obj.getRelatedObjects(context, LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM, LibraryCentralConstants.QUERY_WILDCARD, objectSelects, null, true, false, (short)1, "", "", 0);
				Iterator iter = parentList.iterator();
				Map<String, String> parentMap = null;
				while(iter.hasNext())
				{
					parentMap = (Map)iter.next();
					String type = parentMap.get(LibraryCentralConstants.SELECT_TYPE);
					String id = parentMap.get(LibraryCentralConstants.SELECT_PHYSICALID);
					String[] ids = {id};
					Map<String, String> LibMap = null;
					LibMap= getLibForClass(context,ids);
					parentLibList.add(LibMap);	
						
					}	
				}
    	    return parentLibList;
        }
        catch(Exception exp)
        {
            throw new Exception(exp.getMessage());
        }
    }
    
    
    /**
     * This method deletes the Object of type LibraryFeaturePort when unclassified, provided the Object is not classified in any other Class
     * This method is called from a action trigger when Classified Item Relationship is deleted
     * @param context 
     * @param args String Array having objectId of the unclassified Object at index 0
     * @return 0 on success
     * @throws Exception when failure
     */
    public static int deleteUnusedPortObject(Context context, String[] args) throws Exception {
    	if (args == null || args.length < 2) {
    		throw (new IllegalArgumentException());
    	}
    	String objectId = args[0];
    	
    	DomainObject classifiedObj = new DomainObject(objectId);
    	if(classifiedObj.isKindOf(context, LibraryCentralConstants.TYPE_LIBRARY_FEATURE_PORT)){
    		try{
				ContextUtil.pushContext(context);
    			ContextUtil.startTransaction(context, true);
	    		//find the classes where this port is classified
	    		MapList classes = classifiedObj.getRelatedObjects(context,
	                    LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM,
	                    "*",
	                    new StringList("id"),
	                    null,
	                    true,
	                    false,
	                    new Short("1"),
	                    "",
	                    "",
	                    0);
	    		if(classes.size() == 0){
	    			//if Port is not classified anywhere, delete it
	    				classifiedObj.deleteObject(context);
	    		}
    			ContextUtil.commitTransaction(context);
				ContextUtil.popContext(context);
    		}catch(Exception e){
    			ContextUtil.abortTransaction(context);
				ContextUtil.popContext(context);
			}
    	}

    	return 0;
    }

     /*** Added function for IR-489754-3DEXPERIENCER2017x.
     * This method decide the visibility of commands used to create definition data like document,part based on roles 
     * @param context	:	Security Context Information
     * @param args		:	
     * 
     * @return			:	True if command should be seen else false
     * @throws Exception
     */
    public boolean canDisplayCreateDefinitionCommand(Context context , String[] args)throws Exception {
        boolean value=true;
        String loggedInRole = PersonUtil.getDefaultSecurityContext(context);
        String roleOwner =   PropertyUtil.getSchemaProperty(context,"role_VPLMProjectAdministrator");
        String roleAdmin =   PropertyUtil.getSchemaProperty(context,"role_VPLMAdmin");        
        if (loggedInRole.contains(roleOwner) || loggedInRole.contains(roleAdmin))  {        
               value=false;         
        }
        return value;
    }
}
