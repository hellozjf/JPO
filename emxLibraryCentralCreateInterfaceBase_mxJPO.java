/*
 *  emxLibraryCentralCreateInterfaceBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 * static const RCSID [] = "$Id: ${CLASSNAME}.java.rca 1.21 Wed Oct 22 16:02:31 2008 przemek Experimental przemek $";
 */

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.library.LibraryCentralConstants;

/**
 * The class ${CLASSNAME} provides API for Interface Creation
 *
 * @exclude
 */
public class emxLibraryCentralCreateInterfaceBase_mxJPO
{

    static final String SELECT_ACTIVE_VERSION_ID = "relationship[Active Version].to.id";

    static
    {
        DomainObject.MULTI_VALUE_LIST.add("interface");
    }

    /**
     * Creates ${CLASSNAME}  object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxLibraryCentralCreateInterfaceBase_mxJPO (Context context, String[] args)
        throws Exception
    {

    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int
     * @throws Exception if the operation fails
     * @exclude
     */
    public int mxMain(Context context, String[] args)
        throws FrameworkException
    {
        if (!context.isConnected())
            throw new FrameworkException("not supported on desktop client");
        return 0;
    }

    /**
     * Creates the interface Object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *      0 - objectId
     * @return int 0 or 1 for Success or failure
     * @throws Exception if the operation fails
     */
    public int createInterfaceObject(Context context, String[] args) throws
        Exception
    {

        if(args.length > 0)
        {
            String strObjectId = args[0];
            return createInterfaceObject(context, strObjectId);
        }
        return 1;
    }


    /**
     * Creates the interface Object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the objectId
     * @return int 0 or 1 for Success or failure
     * @throws Exception if the operation fails
     */
    public int createInterfaceObject(Context context, String objectId) throws
        Exception
    {
    	String strParentType="";
    	String strInterfaceName="";
    	String strClassType="";
        if(objectId != null && !"".equals(objectId) && !"null".equals(objectId))
        {
            DomainObject domainObj = new DomainObject();
            domainObj.setId(objectId);
            strClassType  =  domainObj.getInfo(context,DomainObject.SELECT_TYPE);
            BusinessType busType = new BusinessType(strClassType,context.getVault());
            strParentType= busType.getParent(context);            
            java.util.Date sysDate = new Date();
            long lTime = sysDate.getTime();
            strInterfaceName = domainObj.getInfo(context, "physicalid");
            try{
                String strMQL       =  "add interface $1 type all";
                ContextUtil.pushContext(context);

                MqlUtil.mqlCommand(context, strMQL, strInterfaceName);

                strMQL      = "modify bus $1 $2 $3";
                MqlUtil.mqlCommand(context, strMQL, objectId,LibraryCentralConstants.ATTRIBUTE_MXSYS_INTERFACE, strInterfaceName);

                if(strParentType != null && strParentType.equalsIgnoreCase(LibraryCentralConstants.TYPE_LIBRARIES))
                {
                    strMQL      = "modify interface $1 derived $2";
                    MqlUtil.mqlCommand(context, strMQL, strInterfaceName,LibraryCentralConstants.INTERFACE_CLASSIFICATION_TAXONOMIES);
                }

			}catch(Exception e)
			{
				e.printStackTrace();
				throw e;
			}finally{
				ContextUtil.popContext(context);
			}
		}
        try{
            if(UIUtil.isNotNullAndNotEmpty(strClassType) && strClassType.equalsIgnoreCase(LibraryCentralConstants.TYPE_GENERAL_LIBRARY)){
            	createLibraryFeatureReference(context,objectId,strInterfaceName);
           }

        }catch (Exception e) {
        	e.printStackTrace();
        	throw e;
			// TODO: handle exception
		}
		//TO invalidate VPLM Cache
		try{
			Map argsHash = new HashMap();
			String[] args = JPO.packArgs(argsHash);
			String mqlString="list program $1";
			String output=MqlUtil.mqlCommand(context, mqlString,"emxPLMDictionaryProgram");
			if(UIUtil.isNotNullAndNotEmpty(output)){
				JPO.invoke(context, "emxPLMDictionaryProgram", null, "invalidateCache", args,Integer.class);
			}
		}catch (MatrixException e) {
			throw e;
		}
		finally{
			return 0;
		}
	}

   /***
    *
    * @param context
    * @param libraryObjectId
    * @param libraryFeatureReferenceName
 * @throws Exception
    */
    private void createLibraryFeatureReference(Context context,String libraryObjectId, String libraryFeatureReferenceName) throws Exception {
		// TODO Auto-generated method stub
    	try{
    		emxLibraryCentralCommonBase_mxJPO commonBase=new emxLibraryCentralCommonBase_mxJPO(context, new String[]{});
    		commonBase.createAndConnect(context, LibraryCentralConstants.TYPE_LIBRARY_FEATURE_REFERENCE, libraryFeatureReferenceName,
    				new DomainObject().getDefaultRevision(context, LibraryCentralConstants.POLICY_LIBRARY_FEATURE_REFERENCE),
    				LibraryCentralConstants.POLICY_LIBRARY_FEATURE_REFERENCE, context.getVault().getName(),
    				LibraryCentralConstants.RELATIONSHIP_LIBRARY_REFERENCE_FEATURE,
    				new DomainObject(libraryObjectId), true);

    	}catch (Exception e) {
    		e.printStackTrace();
    		throw e;
			// TODO: handle exception
		}

	}

    /**
     * Creates the interface Object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the objectId
     * @return int 0 or 1 for Success or failure
     * @throws Exception if the operation fails
     */

    public int modifyInterfaceOnAddChildren(Context context, String[] args) throws Exception
    {

        if(args.length > 0)
        {
            String strToObjectId = args[0];
            String strFromObjectId = args[1];
            DomainObject childObj = new DomainObject(strToObjectId);
            DomainObject parentObj = new DomainObject(strFromObjectId);
            String strParentInterface = parentObj.getAttributeValue(context,LibraryCentralConstants.ATTRIBUTE_MXSYS_INTERFACE);
            String strChildInterface = childObj.getAttributeValue(context,LibraryCentralConstants.ATTRIBUTE_MXSYS_INTERFACE);


            // This block is to check whether the parent object, which the classification object is added with Subclass Relationship
            // has an interface associated with it, if it does not have an interface associated with it will create an interface and associate
            // with the object.  The Classifications(Part Family) created to prior to the installation will not have any interfaces associated
            // With them.
            if(strParentInterface == null || "".equals(strParentInterface) || "null".equals(strParentInterface)) {
                try {
                    if(createInterfaceObject(context, strFromObjectId) == 0) {
                        strParentInterface = parentObj.getAttributeValue(context,LibraryCentralConstants.ATTRIBUTE_MXSYS_INTERFACE);
                        //If this object is type of LIBRARIES, it will have interface, and will not come into this block.
                        //If this object is type of PartFamily and it is created without Library Central installed, that will fall into this block
                        //that is why inheriting this interface with INTERFACE_CLASSIFICATION_ORPHANS
                        String cmd = "modify interface $1 derived $2";
                        MqlUtil.mqlCommand(context, cmd, true, strParentInterface, LibraryCentralConstants.INTERFACE_CLASSIFICATION_ORPHANS);

                        //Get all classified items connected to it and implement the interface on all classified items
                        SelectList selectStmts = new SelectList(1);
                        selectStmts.addElement(DomainObject.SELECT_ID);
                        MapList result = new MapList();
                        result = (MapList)parentObj.getRelatedObjects(context,LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM,LibraryCentralConstants.QUERY_WILDCARD,selectStmts,new StringList(),false, true, (short)1, null, null);
                        int iSize = result.size();
                        Map tempMap;
                        String strObjectId;
                        for (int k=0;k<iSize;k++ )
                        {
                            implementInterfaceOnClassifiedItem(context, strFromObjectId, ((String)((Map)result.get(k)).get("id")));

                        }

                    }
                } catch(Exception ex) {
                    throw ex;
                }
            }
            // This block is to check whether the classification object,
            // has an interface associated with it, if it does not have an interface associated with it will create an interface and associate
            // with the object.  The Classifications(Part Family) created to prior to the installation of Library Central or created in
            // previous versions will not have any interfaces associated with them.
            if(strChildInterface == null || "".equals(strChildInterface) || "null".equals(strChildInterface)) {
                try {
                    if(createInterfaceObject(context, strToObjectId) == 0) {
                        strChildInterface = childObj.getAttributeValue(context,LibraryCentralConstants.ATTRIBUTE_MXSYS_INTERFACE);

                        //Get all classified items connected to it and implement the interface on all classified items
                        SelectList selectStmts = new SelectList(1);
                        selectStmts.addElement(DomainObject.SELECT_ID);
                        MapList result = new MapList();
                        result = (MapList)childObj.getRelatedObjects(context,LibraryCentralConstants.RELATIONSHIP_CLASSIFIED_ITEM,LibraryCentralConstants.QUERY_WILDCARD,selectStmts,new StringList(),false, true, (short)1, null, null);
                        int iSize = result.size();
                        for (int k=0;k<iSize;k++ )
                        {
                            implementInterfaceOnClassifiedItem(context, strToObjectId, ((String)((Map)result.get(k)).get("id")));
                        }

                    }
                } catch(Exception ex) {
                    throw ex;
                }
            }


            try{
                // The approach here is as follows:
                // Get everything that the child's interface is derived from
                // directly; To that list, add the new parent's interface;
                // remove mxsysLCOrphans.
                // The resulting set is what child's interface will be derived from.
                // This may seem over-complicated, but it is necessary in
                // conjunction with the optional Multiple Classification Module in
                // order to avoid attribute data loss.
                String cmd = "print interface $1 select derived dump $2";
                String currentParentsCSL = MqlUtil.mqlCommand(context,cmd,true, strChildInterface, ",").trim();

                StringList currentParentsList = FrameworkUtil.split(currentParentsCSL, ",");
                currentParentsList.addElement(strParentInterface);
                currentParentsList.remove(LibraryCentralConstants.INTERFACE_CLASSIFICATION_ORPHANS);
                String[] parentInterfaces   = new String[currentParentsList.size()+1];
                parentInterfaces[0]         = strChildInterface;
                StringBuffer sbQuery        = new StringBuffer("modify interface $1 derived");
                for (int i = 0 ; i < currentParentsList.size() ; i++) {
                    sbQuery.append(" $").append(i+2).append(",");
                    parentInterfaces[i+1] = (String)currentParentsList.get(i);
                }
                cmd = sbQuery.substring(0, sbQuery.lastIndexOf(","));

                String result = MqlUtil.mqlCommand(context, cmd, true, parentInterfaces);

                return 0;

            }catch(Exception e)
            {
                e.printStackTrace();
                throw e;
            }

        }

        return 1;

    }


   /**
     * Modifies the interface on remove of children
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *          0 -  FROMOBJECTID
     *          1 -  TOOBJECTID
     * @return int 0 or 1 for Success or failure
     * @throws Exception if the operation fails
     */

    public int modifyInterfaceOnRemoveChildren(Context context, String[] args)throws Exception
    {
        if(args.length > 0)
        {
            String strToObjectId = args[0];
            String strFromObjectId = args[1];
            DomainObject childObj = new DomainObject(strToObjectId);

            String strChildInterface = childObj.getAttributeValue(context,LibraryCentralConstants.ATTRIBUTE_MXSYS_INTERFACE);

            try{
                // The approach here is as follows:
                // Get everything that the child's interface is derived from,
                // directly or indirectly; subtract any ancestor interface that
                // is a Classificaiton interface, which leaves Attribute Groups;
                // remove mxsysLCTaxonomies and replace it with mxsysLCOrphans.
                // The resulting set is what child's interface will
                // be derived from.
                // This may seem over-complicated, but it is necessary in
                // conjunction with the optional Multiple Classification Module in
                // order to avoid attribute data loss.
            	
                
                String cmd                          = "print interface $1 select $2";
                String currentParentWithDerived      = MqlUtil.mqlCommand(context, cmd, true, strChildInterface, "allparents.derived").trim();
                StringList currentParentWithDerivedList = FrameworkUtil.split(currentParentWithDerived, "\n");
                StringList currentParentsList = new StringList();
                Iterator iii = currentParentWithDerivedList.iterator();
                while(iii.hasNext())
                {
                	String checkCurrentParent = (String)iii.next();
                	checkCurrentParent = checkCurrentParent.trim();
                	if(checkCurrentParent.endsWith(LibraryCentralConstants.INTERFACE_CLASSIFICATION_ATTRIBUTE_GROUPS))
                	{
                		int backRmove = ((String)("].derived = Classification Attribute Groups")).length();
                		int frontRemove = ((String)("allparents[")).length();
                		checkCurrentParent = checkCurrentParent.substring(frontRemove, checkCurrentParent.length() - backRmove);
                		currentParentsList.add(checkCurrentParent);
                	}
                }
                if(currentParentsList.size() > 0)
                {
                	currentParentsList.add(LibraryCentralConstants.INTERFACE_CLASSIFICATION_ATTRIBUTE_GROUPS);
                }
                currentParentsList.add(LibraryCentralConstants.INTERFACE_CLASSIFICATION_ORPHANS);
                
                String[] parentInterfaces   = new String[currentParentsList.size()+1];
                parentInterfaces[0]         = strChildInterface;
                StringBuffer sbQuery        = new StringBuffer("modify interface $1 derived");
                for (int i = 0 ; i < currentParentsList.size() ; i++) {
                    sbQuery.append(" $").append(i+2).append(",");
                    parentInterfaces[i+1] = (String)currentParentsList.get(i);
                }
                cmd = sbQuery.substring(0, sbQuery.lastIndexOf(","));
                String result = MqlUtil.mqlCommand(context, cmd, true, parentInterfaces);


                return 0;
            }catch(Exception e)
            {
                e.printStackTrace();
                throw e;
            }
        }

        return 1;

    }

   /**
     * Implements interface on Classified Items
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *          0 -  FROMOBJECTID
     *          1 -  TOOBJECTID
     * @return int 0 or 1 for Success or failure
     * @throws Exception if the operation fails
     * @throws FrameworkException if the operation fails
     */
    public static void implementInterfaceOnClassifiedItem(Context context, String[] args) throws
        Exception,FrameworkException
    {
        String strResult = "";
        if(args.length > 0)
        {
            String strToObjectId = args[0];
            String strFromObjectId = args[1];
            strResult = implementInterfaceOnClassifiedItem(context, strFromObjectId, strToObjectId);
        }
        //return strResult;
    }
   /**
     * Implements interface on Classified Items
     *
     * @param context the eMatrix <code>Context</code> object
     * @param parentId parrent objectID
     * @param objectId
     * @return int 0 or 1 for Success or failure
     * @throws Exception if the operation fails
     * @throws FrameworkException if the operation fails
     */
    public static String implementInterfaceOnClassifiedItem(Context context, String parentId, String objectId) throws
        Exception,FrameworkException
    {
        String strResult = "";
        DomainObject doObj = new DomainObject(parentId);
        String strInterface = doObj.getAttributeValue(context,LibraryCentralConstants.ATTRIBUTE_MXSYS_INTERFACE);

        try{
            String strMQL       = "print bus $1 select interface dump";
            strResult           = MqlUtil.mqlCommand(context, strMQL, objectId);
            ContextUtil.pushContext(context);

            if(strResult != null && strResult.indexOf(strInterface) == -1)
            {
                strMQL      = "modify bus $1 add interface $2";
                strResult   = MqlUtil.mqlCommand(context, strMQL, objectId,strInterface);

                if(strResult.indexOf(LibraryCentralConstants.INTERFACE_CLASSIFICATION_SEARCH_FILTER) != -1)
                {
                    strMQL      ="modify bus $1 remove interface $2";
                    strResult   = MqlUtil.mqlCommand(context, strMQL, objectId,LibraryCentralConstants.INTERFACE_CLASSIFICATION_SEARCH_FILTER);
                }

                //Changes Added for Designer Central - Integration
                DomainObject toObject = new DomainObject(objectId);
                String implementIntOnMinorObjs = EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.MinorObjects.Classify");

                if(CommonDocument.TYPE_DOCUMENTS.equals(CommonDocument.getParentType(context, toObject.getInfo(context, DomainObject.SELECT_TYPE)))
                    && (implementIntOnMinorObjs != null && implementIntOnMinorObjs.equalsIgnoreCase("true"))){
                    //Get all version objects (minor objects)connected with Active Version relationship
                    StringList selectStmts = new StringList(2);
                    selectStmts.addElement(DomainObject.SELECT_ID);
                    selectStmts.addElement(SELECT_ACTIVE_VERSION_ID);
                    selectStmts.addElement("interface");
                    MapList result = (MapList)toObject.getRelatedObjects(context,
                                                                         CommonDocument.RELATIONSHIP_ACTIVE_VERSION,
                                                                         "*", selectStmts,
                                                                         new StringList(),
                                                                         false, true, (short)1, null, null);

                    String activeVersionId = "";
                    String implementedInterfaces = "";
                    if (result != null && result.size() > 0) {
                        for(int lCnt = 0; lCnt < result.size(); lCnt ++) {
                           Map objMap = (Map)result.get(lCnt);
                           activeVersionId = (String)objMap.get(DomainObject.SELECT_ID);

                           //check if there are more than one interfaces
                           //and act accordingly
                           Object interfaces = (Object)objMap.get("interface");

                           String interfaceStr = "";
                           StringList interfacesList = null;

                           boolean moreThanOneInterfaces = false;
                           boolean addInterface = false;

                           if(interfaces instanceof String)
                           {
                               interfaceStr = (String)interfaces;
                           }
                           else if(interfaces instanceof StringList)
                           {
                               moreThanOneInterfaces = true;
                               interfacesList = new StringList();
                               interfacesList = (StringList)interfaces;
                           }

                           if(!moreThanOneInterfaces &&
                              interfaceStr != null &&
                              interfaceStr.indexOf(strInterface) == -1)
                           {
                               addInterface = true;
                           }
                           else if(moreThanOneInterfaces &&
                                   interfacesList != null &&
                                   !interfacesList.contains(strInterface))
                           {
                               addInterface = true;
                           }

                           //The interface is new
                           if(addInterface)
                           {
                               BusinessObject activeVersionObject = new BusinessObject(activeVersionId);
                               activeVersionObject.open(context);
                               BusinessObjectList minorObjectList = activeVersionObject.getRevisions(context);
                               activeVersionObject.close(context);

                               for (int i = 0; i < minorObjectList.size(); i++)
                               {
                                   BusinessObject minorObject = (BusinessObject)minorObjectList.get(i);
                                   String versionObjectId = minorObject.getObjectId();

                                   strMQL       = "modify bus $1 add interface $2";
                                   strResult    = MqlUtil.mqlCommand(context, strMQL, versionObjectId, strInterface);
                               }
                           }
                        }
                   }
                }

                //End Changes
            }else{
                strResult = "0.0";
            }
        }catch(Exception e)
        {
            e.printStackTrace();
            strResult = "0.0";
            e.printStackTrace();
            throw e;
        }finally{
            ContextUtil.popContext(context);
        }
        return strResult;
    }
   /**
     * Modifies interface on Classified Items
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments
     *          0 -  FROMOBJECTID
     *          1 -  TOOBJECTID
     * @return int 0 or 1 for Success or failure
     * @throws Exception if the operation fails
     * @throws FrameworkException if the operation fails
     */
    public static String modifyInterfaceOnClassifiedItem(Context context, String[] args) throws
        Exception,FrameworkException
    {
        String strResult = "";
        if(args.length > 0)
        {
            String strToObjectId = args[0];
            String strFromObjectId = args[1];
            DomainObject doObj = new DomainObject(strFromObjectId);
            String strInterface = doObj.getAttributeValue(context,LibraryCentralConstants.ATTRIBUTE_MXSYS_INTERFACE);
        try{
            String strMQL   = "print bus $1 select interface dump";
            strResult       = MqlUtil.mqlCommand(context, strMQL, strToObjectId);

            ContextUtil.pushContext(context);
            if(strResult != null && strInterface != null && strResult.length() > 0 && strInterface.length() > 0 && strResult.indexOf(strInterface) != -1)
            {
                //remove interface on master object - CDM model
                strMQL      = "modify bus $1 remove interface $2";
                strResult   = MqlUtil.mqlCommand(context, strMQL, strToObjectId, strInterface);

                //Design Central Changes
                DomainObject toObj = new DomainObject(strToObjectId);
                String implementIntOnMinorObjs = EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.MinorObjects.Classify");

                if(CommonDocument.TYPE_DOCUMENTS.equals(CommonDocument.getParentType(context, toObj.getInfo(context, DomainObject.SELECT_TYPE)))
                        && (implementIntOnMinorObjs != null && implementIntOnMinorObjs.equalsIgnoreCase("true"))){
//                  Get all version objects (minor objects)connected with Active Version relationship
                    StringList selectStmts = new StringList(2);
                    selectStmts.addElement(DomainObject.SELECT_ID);
                    selectStmts.addElement(SELECT_ACTIVE_VERSION_ID);
                    selectStmts.addElement("interface");
                    MapList result = (MapList)toObj.getRelatedObjects(context,
                                                                         CommonDocument.RELATIONSHIP_ACTIVE_VERSION,
                                                                         "*", selectStmts,
                                                                         new StringList(),
                                                                         false, true, (short)1, null, null);
                    String activeVersionId = "";
                    String implementedInterfaces = "";

                    //go through minor object and remove interface
                    if (result != null && result.size() > 0) {
                        for(int lCnt = 0; lCnt < result.size(); lCnt ++) {
                           Map objMap = (Map)result.get(lCnt);
                           activeVersionId = (String)objMap.get(DomainObject.SELECT_ID);

                           //check if there are more than one interfaces
                           //and act accordingly
                           Object interfaces = (Object)objMap.get("interface");

                           String interfaceStr = "";
                           StringList interfacesList = null;

                           boolean moreThanOneInterfaces = false;
                           boolean removeInterface = false;

                           if(interfaces instanceof String )
                           {
                               interfaceStr = (String)interfaces;
                           }
                           else if(interfaces instanceof StringList)
                           {
                               moreThanOneInterfaces = true;
                               interfacesList = new StringList();
                               interfacesList = (StringList)interfaces;
                           }

                           if(!moreThanOneInterfaces &&
                              interfaceStr != null &&
                              interfaceStr.length() > 0 &&
                              interfaceStr.indexOf(strInterface) != -1)
                           {
                              removeInterface = true;
                           }
                           else if(moreThanOneInterfaces &&
                                   interfacesList != null &&
                                   interfacesList.size() > 0 &&
                                   interfacesList.contains(strInterface))
                           {
                               removeInterface = true;
                           }

                           if(removeInterface)
                           {
                               //implementedInterfaces = (String)objMap.get("interface");
                               BusinessObject activeVersionObject = new BusinessObject(activeVersionId);
                               activeVersionObject.open(context);
                               BusinessObjectList minorObjectList = activeVersionObject.getRevisions(context);
                               activeVersionObject.close(context);

                               for (int i = 0; i < minorObjectList.size(); i++)
                               {
                                  BusinessObject minorObject = (BusinessObject)minorObjectList.get(i);
                                  String versionObjectId = minorObject.getObjectId();

                                  strMQL        = "modify bus $1 remove interface $2";
                                  strResult     = MqlUtil.mqlCommand(context, strMQL, versionObjectId, strInterface);
                               }
                           }
                        }
                   }
                }

                //End of changes
            }else{
                strResult = "0.0";
            }
        }catch(Exception e)
        {
            e.printStackTrace();
            strResult = "0.0";
        }finally{
            ContextUtil.popContext(context);
        }
        }
        return strResult;
    }

    /**
     * Update version (minor) objects
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following arguments:
     *          0 -  objectId
     *          1 -  minorObjId
     * @return int 0 or 1 for success or failure
     * @throws Exception if the operation fails
     */
    public int update(Context context, String[] args)
         throws FrameworkException
    {

        int ret = 0;
        if(args == null || args.length < 2)
        {
            throw new FrameworkException("ERROR - Invalid number of arguments");
        }

        try
        {
            String objectId = args[0];
            String minorObjId = args[1];
            DomainObject dmObj = (DomainObject) DomainObject.newInstance(context, objectId);
            String type = dmObj.getInfo(context, dmObj.SELECT_TYPE);

            //update minor objects only if an item is classified in Library Central
            String implementIntOnMinorObjs = EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.MinorObjects.Classify");
            if(CommonDocument.TYPE_DOCUMENTS.equals(CommonDocument.getParentType(context, type)) &&
               (implementIntOnMinorObjs != null && implementIntOnMinorObjs.equalsIgnoreCase("true")))
            {
                ret = updateMinorObjects(context, objectId, minorObjId);
            }
        }catch (Exception ex)
        {
            ex.printStackTrace();
            ret = 1;
        }

        return ret;
    }

    /**
     * Update Minor object and its revisions to add interface(s) if the property
     * MinorObjects.Classify is set to true and parent's object type is DOCUMENTS
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the id of the master object
     * @param minorObjectId the id of the minor object
     * @return int 0 or 1 for success or failure
     * @throws Exception if the operation fails
     */
    public static int updateMinorObjects(Context context,
                                         String objectId,
                                         String minorObjectId)
         throws FrameworkException
    {

        if(objectId == null || "null".equals(objectId) || objectId.length() <=0 ||
           minorObjectId == null || "null".equals(minorObjectId) || minorObjectId.length() <=0)
        {
            throw new FrameworkException("ERROR - No objectid is provided");
        }

        int ret = 0;
        boolean isContextPushed = false;
        ContextUtil.pushContext(context);
        isContextPushed = true;
        String strResult = "";
        try
        {
            //Changes Added for Designer Central - Integration
            DomainObject dmObj = new DomainObject(objectId);
            StringList busSels = new StringList();

            busSels.add("interface");
            busSels.add(dmObj.SELECT_TYPE);
            busSels.add(dmObj.SELECT_NAME);
            busSels.add(dmObj.SELECT_ID);
            busSels.add(dmObj.SELECT_REVISION);

            Map busMap = dmObj.getInfo(context, busSels);
            StringList interfacesListTmp = null;

            Object interfacesList = (Object)busMap.get("interface");
            if(interfacesList instanceof String)
            {
                interfacesListTmp = new StringList((String)interfacesList);
            }
            else if(interfacesList instanceof StringList)
            {

                interfacesListTmp = (StringList)interfacesList;
            }
            else
            {
                interfacesListTmp = new StringList();
            }

            if(interfacesListTmp == null || interfacesListTmp.size() < 1)
            {
                //no interface to add
                return 0;
            }

            String type = (String)busMap.get(dmObj.SELECT_TYPE);
            String implementIntOnMinorObjs = EnoviaResourceBundle.getProperty(context,"emxLibraryCentral.MinorObjects.Classify");

            if(CommonDocument.TYPE_DOCUMENTS.equals(CommonDocument.getParentType(context, type))
                && (implementIntOnMinorObjs != null && implementIntOnMinorObjs.equalsIgnoreCase("true"))){

                DomainObject minorObj =
                    (DomainObject) DomainObject.newInstance(context,minorObjectId);

                //Master's object interfaces must match Minor's object interfaces
                //if not, make it identical
                Object minObjInterfaces = (Object)minorObj.getInfo(context, "interface");
                StringList minObjInterfacesList = null;

                if(minObjInterfaces instanceof String &&
                   minObjInterfaces != null &&
                   ((String)minObjInterfaces).length() > 0)
                {
                    minObjInterfacesList = new StringList((String)minObjInterfaces);
                }
                else if(minObjInterfaces instanceof StringList)
                {
                    minObjInterfacesList = new StringList();
                    minObjInterfacesList = (StringList)minObjInterfaces;
                }
                else //no interface exists in Minor object
                {
                    minObjInterfacesList = new StringList();
                }

                //add the new interface to each object revision

                BusinessObject activeVersionObject = new BusinessObject(minorObjectId);
                activeVersionObject.open(context);
                BusinessObjectList revObjectList = activeVersionObject.getRevisions(context);
                activeVersionObject.close(context);

                //just make sure
                if(minObjInterfacesList == null)
                {
                    minObjInterfacesList = new StringList();
                }

                for (int i = 0; i < revObjectList.size(); i++)
                {
                    BusinessObject revObject = (BusinessObject)revObjectList.get(i);
                    String versionObjectId = revObject.getObjectId();

                    //implement all interfaces which are not yet in Minor object
                    for(int j = 0; j < interfacesListTmp.size(); j++)
                    {
                        String newInterfaceStr = (String)interfacesListTmp.get(j);
                        if(newInterfaceStr != null &&
                           newInterfaceStr.length() > 0 &&
                           !minObjInterfacesList.contains(newInterfaceStr))
                        {
                            String strMQL       = "modify bus $1 add interface $2";
                            strResult           = MqlUtil.mqlCommand(context, strMQL, versionObjectId, newInterfaceStr);
                        }
                    }
                }
            }

            //End Changes

        }catch (Exception ex)
        {
            ex.printStackTrace();
            ret = 1;
        }
        finally
        {
            if(isContextPushed)
            {
                ContextUtil.popContext(context);
            }
        }

        return ret;
    }

}
