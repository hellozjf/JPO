/*
 * emxResourcePoolBase
 *
 * Copyright (c) 1999-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 */

/**
Change History:
Date       Change By  Release        Bug/Functionality        Details
-----------------------------------------------------------------------------------------------------------------------------
20-Aug-09   wqy        V6R2010x     IR-012043V6R2010x        modify function assignPeoplesToResourceRequest to change the logic 
															 of adding resource Request     
                       
*/


import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Relationship;
import matrix.db.RelationshipWithSelect;
import matrix.db.RelationshipWithSelectItr;
import matrix.db.RelationshipWithSelectList;
import matrix.db.Role;
import matrix.db.UserItr;
import matrix.db.UserList;
import matrix.util.MatrixException;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.common.Organization;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PolicyUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.FTE;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;

public class emxResourcePoolBase_mxJPO extends emxDomainObjectBase_mxJPO {
     
    private static final String STRING_CAPACITY = "Capacity";

	final short nObjectLimit = 0;
    /**
     * Constructor 
     * 
     * @param context The Matrix Context object
     * @param args The arguments array
     * @throws Exception if operation fails
     */
    public emxResourcePoolBase_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
    }

    /**
     * <method description>
     *
     * @param context The Matrix Context object
     * @param args The ARgument String Array
     * @returns int  It returns integer
     * @throws Exception if the operation fails
     */
    public int mxMain(Context context, String[] args) throws MatrixException {
        throw new MatrixException("This JPO cannot be run stand alone.");
    }
    
    /**
     * Returns comma separated names of the Resource Managers. This method is used for getting value of Resource Manager field on web form. 
     * It is used for 
     * -Field ResourceManager in form type_BusinessUnit
     * -Field ResourceManager in form type_Company
     * 
     * @param context The Matrix Context object
     * @param args Packed programMap
     * @returns String the comma separated names of Resource Managers
     * @throws MatrixException if context is null / objectId parameter is invalid / the operation fails 
     * @since PRG R207
     */
    public String getFieldResourceManagersData (Context context, String[] args) throws MatrixException 
    {
    	try
    	{
    		emxOrganization_mxJPO organization = new emxOrganization_mxJPO(context, args);
    		return organization.getFieldResourceManagersData(context, args);
    	}
    	catch (Exception exp)
    	{
    		exp.printStackTrace();
    		throw new MatrixException(exp);
    	}
    }
 
    /**
     * Finds the Resource Managers information. This method should not be used for table & form fields.
     *
     * @param context The Matrix Context object
     * @param args String array of selectables
     * @returns MapList information about Resource Managers
     * @throws MatrixException if context is null / the operation fails 
     * @since PRG R207
     */
    public MapList getResourceManagers (Context context, String[] args) throws MatrixException {
        
        try {
            // Check method arguments
            if (context == null) {
                throw new IllegalArgumentException("context");
            }
            
			emxOrganization_mxJPO organization = new emxOrganization_mxJPO(context, new String[]{this.getId()});
            return organization.getResourceManagers (context, args);
        }
        catch (Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
    
    
    /**
     * This method assigns selected persons as Resource managers to the Business Units field when selected from edit form mode.
     *  It is used for 
     * -Field ResourceManager in form type_BusinessUnit
     * -Field ResourceManager in form type_Company
     * 
     * @param context The Matrix Context object
     * @param args Packed ProgramMap
     * @throws MatrixException if context is null / objectId parameter is invalid / the operation fails 
     * @since PRG R207
     */
    public void updateFieldResourceManagerData(Context context, String[] args) throws MatrixException
    {
    	try
    	{
    		emxOrganization_mxJPO organization = new emxOrganization_mxJPO(context, new String[]{this.getId()});
    		organization.updateFieldResourceManagerData(context,args);
    	}
    	catch(Exception exe)
    	{
    		exe.printStackTrace();
    		throw new MatrixException(exe);
    	}

    }
    
    /**
     * Gets the data for the column "ResourceManagers" for table "PMCResourcePoolSummary"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnResourceManagersData(Context context, String[] args) throws Exception
    {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            Map paramList = (Map) programMap.get("paramList");
            MapList objectList = (MapList) programMap.get("objectList");
            boolean isExport = false;
            boolean isPrinterFriendly = false;
            String strReport= (String)paramList.get("reportFormat");    
            String strExport= (String)paramList.get("exportFormat");
            if((ProgramCentralUtil.isNotNullString(strExport) && "CSV".equalsIgnoreCase(strExport)) || 
            		(ProgramCentralUtil.isNotNullString(strReport) && "CSV".equalsIgnoreCase(strReport))) 
            {
            	isExport = true;
            }
            if((ProgramCentralUtil.isNotNullString(strExport) && "HTML".equalsIgnoreCase(strExport)) || 
            		(ProgramCentralUtil.isNotNullString(strReport) && "HTML".equalsIgnoreCase(strReport))) 
            {
            	isPrinterFriendly = true;
            }
            Map mapRowData = null;
            for (Iterator itrRowData = objectList.iterator(); itrRowData.hasNext();)
 {
                mapRowData = (Map) itrRowData.next();
               String strResourceManager = (String) mapRowData.get("Resource_Managers");
    			StringList slResourceManagers = FrameworkUtil.split(strResourceManager,",");
    			StringList slResourceManagersReturned = new StringList();
    			String strResourceManagerFullName = null;
    			String strResourceManagerId = null;
    			String imageStr = "../common/images/iconSmallPerson.gif";

    			for (int i = 0; i < slResourceManagers.size(); i++)
    			{
    				strResourceManagerFullName = PersonUtil.getFullName(context,(String) slResourceManagers.get(i));
            		if(isExport)
            		{
            			slResourceManagersReturned.add(XSSUtil.encodeForHTML(context,strResourceManagerFullName));
            		}
            		else if (isPrinterFriendly)
            		{
            			StringBuffer sbSubstanceLink = new StringBuffer();
            			sbSubstanceLink.append("<img border=\"0\" src=\""+ imageStr + "\" title=\"\"></img>");
            			sbSubstanceLink.append(XSSUtil.encodeForHTML(context, strResourceManagerFullName));
            			slResourceManagersReturned.add(sbSubstanceLink.toString());
            		}
            		else
            		{
    				strResourceManagerId = PersonUtil.getPersonObjectID(context, (String) slResourceManagers.get(i));
    				StringBuffer sbSubstanceLink = new StringBuffer();
    				sbSubstanceLink.append("<img border=\"0\" src=\""+ imageStr + "\" title=\"\"></img>");
    				sbSubstanceLink.append("<a href='../common/emxTree.jsp?objectId=").append(strResourceManagerId);
    				sbSubstanceLink.append("' >");                    
    				sbSubstanceLink.append(XSSUtil.encodeForHTML(context, strResourceManagerFullName));
    				sbSubstanceLink.append("</a>");
    				slResourceManagersReturned.add(sbSubstanceLink.toString());
    			}
            	}
    			vecResult.add(FrameworkUtil.join(slResourceManagersReturned,";"));
            }
            return vecResult;
    	} catch (Exception exp)
    	{
            exp.printStackTrace();
            throw exp;
        }
    }
   
    /**
     * Gets the data for the column "Capacity" for table "PMCResourcePoolSummary"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing no. of peoples available to particular Resource Pool
     * @throws Exception if operation fails
     */
    public Vector getColumnCapacityData(Context context, String[] args)
            throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");

            Map mapRowData = null;
            for (Iterator itrRowData = objectList.iterator(); itrRowData.hasNext();) {
                mapRowData = (Map) itrRowData.next();
                
                vecResult.add(mapRowData.get(STRING_CAPACITY));
            }
            
            return vecResult;

        } catch (Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * This is common method used for finding the resource pool data for My Desk > Program > Resource Pools table.
     * This method is supposed to be used only for the Resource Pool table methods.
     * 
     * 
     * @param context The Matrix Context object
     * @param strPMCResourcePoolFilter  Resource Pool Table filter for e.g. MyResourcePools/ AllResourcePools
     *              If this is "AllResourcePools", then strObjectId, strRelPattern, getTo & getFrom parameters are ignored.
     * @param strSelectedResourceManager Selected Resource Manager for which 
     * @param strObjectId The object id of business object from which expansion is to be performed to get Organization objects
     * @param strRelPattern The command separated relationship patterns to be used for expansion
     * @param getTo true id given object lies on to side of the relationships else false
     * @param getFrom true id given object lies on from side of the relationships else false
     * @param isExpandFunction true if this call is being used to find the objects when an object in table is expanded. This parameter 
     *              is only used when strPMCResourcePoolFilter = "AllResourcePools" to return no data in expand mode, otherwise it is ignored.
     * @return MapList containing Organization information for rendering the table. This map will have values against following keys
     *              DomainConstants.SELECT_ID
     *              "SELECT_ORGANIZATION_MEMBERS_ID"
     *              "SELECT_RESOURCE_MANAGERS_NAME"
     *              "Capacity"
     *              "Resource_Managers"
     * @throws MatrixException if operation fails
     */
    protected MapList getTableResourcePoolsData (Context context, String strPMCResourcePoolFilter,String strSelectedResourceManager, String strObjectId, String strRelPattern, boolean getTo, boolean getFrom, boolean isExpandFunction) throws MatrixException 
    {
        try
        {
            if(context == null)
            {
                throw new MatrixException("Null Context !");
            }
            
            MapList mlResult = new MapList();
            
            final String RESOURCE_REQUEST_STATE_REQUESTED = PropertyUtil.getSchemaProperty(context, "Policy", POLICY_RESOURCE_REQUEST, "state_Requested");
            final String RESOURCE_REQUEST_STATE_PROPOSED = PropertyUtil.getSchemaProperty(context, "Policy", POLICY_RESOURCE_REQUEST, "state_Proposed");
            final String RESOURCE_REQUEST_STATE_REJECTED = PropertyUtil.getSchemaProperty(context, "Policy", POLICY_RESOURCE_REQUEST, "state_Rejected");
            //
            // Depending on the filter selected decide how to find the resource pools
            //
        	String sAll = EnoviaResourceBundle.getProperty(context, ProgramCentralConstants.PROGRAMCENTRAL,
        			"emxProgramCentral.Common.All", context.getSession().getLanguage());
            DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
            String strLoggedInRM = dmoObject.getInfo(context,SELECT_NAME);
            
            StringList slPMCUsers = new StringList();
        
    	    	String strPMCSymbolicRoles = EnoviaResourceBundle.getProperty(context, "eServiceSuiteProgramCentral.Roles");
    	        StringList slPMCSymbolicRoles = FrameworkUtil.split(strPMCSymbolicRoles, ",");
    	        
    	        String strRole = "";
    	        Role matrixRole = null;
    	        UserList assignments = null;
    	        String personObjId = "";
    	        for (StringItr itrRoles = new StringItr(slPMCSymbolicRoles); itrRoles.next();) 
    	        {
    	            strRole = PropertyUtil.getSchemaProperty(context, itrRoles.obj().trim());
    	            
    	            matrixRole = new Role(strRole);
    	            matrixRole.open(context);
    	            
    	            StringList projectUsers = new StringList();
    	            assignments = matrixRole.getAssignments(context);
    	            
    			if(assignments.size() > 0){
    	            UserItr userItr = new UserItr(assignments);
    	            
    	            while(userItr.next()) {
    	                if (userItr.obj() instanceof matrix.db.Person) {
    	                	try {
	    	                	personObjId = PersonUtil.getPersonObjectID(context, userItr.obj().getName());
	    	                    if (!slPMCUsers.contains(personObjId)){
	    	                        slPMCUsers.add(personObjId);
	    	                    }
    	                	} catch(Exception exception) {
    			          		//PersonUtil.getPersonObjectID() Throws exception when Person Admin
    			          		//object exists and Business object does not.
    	     			   }
    	                }
    	            }
    	            matrixRole.close(context);
    	        }
    			else{ // to include OCDX users along with CSE users
    				String sMqlCommand = "print role $1 select $2 dump $3;";
    				String result = MqlUtil.mqlCommand(context, sMqlCommand, strRole, "person", "|" );
    				StringList slUser = FrameworkUtil.split(result, "|");
    				Iterator<String> itr = slUser.iterator();
    				while(itr.hasNext())
    				{
    					String userName = itr.next();
    					try
    					{
    						//Creating person bean itself will throw error if Business Object does not exist
    						personObjId = PersonUtil.getPersonObjectID(context, userName);
    						if (!slPMCUsers.contains(personObjId))
    							slPMCUsers.add(personObjId);
    					}
    					catch(Exception e)
    					{
            
    					}
    				}
    			}
    		}
          
            if ("MyResourcePools".equals(strPMCResourcePoolFilter) && !isExpandFunction)
            {
                final String SELECT_ORGANIZATION_MEMBERS_ID = "from["+RELATIONSHIP_MEMBER+"].to.id";
                final String SELECT_RESOURCE_MANAGERS_NAME = "from["+RELATIONSHIP_RESOURCE_MANAGER+"].to.name";
                final String SELECT_RESOURCE_REQUEST_ID = "to["+RELATIONSHIP_RESOURCE_POOL+"].from.id";
                final String SELECT_RESOURCE_REQUEST_STATE = "to["+RELATIONSHIP_RESOURCE_POOL+"].from.current";
                
                String strTypePattern = TYPE_ORGANIZATION;
                
                StringList slBusSelect = new StringList();
                slBusSelect.add(DomainConstants.SELECT_ID);
                slBusSelect.add(SELECT_ORGANIZATION_MEMBERS_ID);//Reqd to calculate Capacity
                slBusSelect.add(SELECT_RESOURCE_REQUEST_ID);//Reqd to calculate Open requests
                slBusSelect.add(SELECT_RESOURCE_REQUEST_STATE);//Reqd to calculate Open requests
                slBusSelect.add(SELECT_RESOURCE_MANAGERS_NAME);//Reqd to find resource managers
                
                StringList slRelSelect = new StringList();
                short recurseToLevel = 1;
                String strBusWhere = null;

        	if (sAll.equalsIgnoreCase(strSelectedResourceManager))
                {
                     strBusWhere = "";
                }
                else
                {
                    strBusWhere = ""+ SELECT_RESOURCE_MANAGERS_NAME +" smatch  \""+ strSelectedResourceManager +"\"";
                }
                
                String strRelWhere = "";
                MapList mlContextResourcePools = dmoObject.getRelatedObjects(context,
                                                                                                        strRelPattern, //pattern to match relationships
                                                                                                        strTypePattern, //pattern to match types
                                                                                                        slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                                                                                        slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                                                                                        getTo, //get To relationships
                                                                                                        getFrom, //get From relationships
                                                                                                        recurseToLevel, //the number of levels to expand, 0 equals expand all.
                                                                                                        strBusWhere, //where clause to apply to objects, can be empty ""
                                                                                                        strRelWhere); //where clause to apply to relationship, can be empty ""

                
                // Consolidated this data against consistent keys so that it will be easy to process later
                Map mapContextResourcePool = null;
                Map mapConsolidatedInfo = null;
                for (Iterator itrContextResourcePools = mlContextResourcePools.iterator(); itrContextResourcePools.hasNext();) {
                    mapContextResourcePool = (Map) itrContextResourcePools.next();
                    
                    mapConsolidatedInfo = new HashMap();
                    mapConsolidatedInfo.put(DomainConstants.SELECT_ID, mapContextResourcePool.get(DomainConstants.SELECT_ID));
					mapConsolidatedInfo.put("level", mapContextResourcePool.get("level"));
                    mapConsolidatedInfo.put("SELECT_ORGANIZATION_MEMBERS_ID", mapContextResourcePool.get(SELECT_ORGANIZATION_MEMBERS_ID));
                    mapConsolidatedInfo.put("SELECT_RESOURCE_MANAGERS_NAME", mapContextResourcePool.get(SELECT_RESOURCE_MANAGERS_NAME));
                    mapConsolidatedInfo.put("SELECT_RESOURCE_REQUEST_ID", mapContextResourcePool.get(SELECT_RESOURCE_REQUEST_ID));
                    mapConsolidatedInfo.put("SELECT_RESOURCE_REQUEST_STATE", mapContextResourcePool.get(SELECT_RESOURCE_REQUEST_STATE));
                    
                    mlResult.add(mapConsolidatedInfo);
                }
            }
            else if ("AllResourcePools".equals(strPMCResourcePoolFilter) && !isExpandFunction)
            {
                final String SELECT_RESOURCE_POOLS_ID = "from.id";
                final String SELECT_ORGANIZATION_MEMBERS_ID = "from.from["+RELATIONSHIP_MEMBER+"].to.id";
                final String SELECT_RESOURCE_REQUEST_ID = "to["+RELATIONSHIP_RESOURCE_POOL+"].from.id";
                final String SELECT_RESOURCE_REQUEST_STATE = "to["+RELATIONSHIP_RESOURCE_POOL+"].from.current";
                final String SELECT_RESOURCE_MANAGERS_NAME = "to.name";
                
                strRelPattern = RELATIONSHIP_RESOURCE_MANAGER;
                String strVaultPattern = context.getVault().getName();
                String strRelExpression = null;
                //Modified:Jan 10, 2011:HP5:R211:PRG:IR-080992V6R2012                 
                strRelExpression = "";
                MapList tempResult = new MapList();
                //End:R207:PRG:IR-080992V6R2012   	 
                short nObjectLimit = 0;// TODO for all Objects ??
                StringList slSelectStmts = new StringList();
                slSelectStmts.add(SELECT_RESOURCE_POOLS_ID);
                slSelectStmts.add(SELECT_ORGANIZATION_MEMBERS_ID);
                slSelectStmts.add(SELECT_RESOURCE_REQUEST_ID);
                slSelectStmts.add(SELECT_RESOURCE_REQUEST_STATE);
                slSelectStmts.add(SELECT_RESOURCE_MANAGERS_NAME);
                
                StringList slOrderBy = new StringList();
                slOrderBy.add(SELECT_RESOURCE_POOLS_ID);
                
                RelationshipWithSelectList relationshipWithSelectList = Relationship.query(context,
                                                                                                                    strRelPattern,
                                                                                                                    strVaultPattern,
                                                                                                                    strRelExpression,
                                                                                                                    nObjectLimit,
                                                                                                                    slSelectStmts,
                                                                                                                    slOrderBy);
                RelationshipWithSelect relationshipWithSelect = null;
                String strResourcePoolId = null;
                MapList mlResourcePools = new MapList();
                Map mapConsolidatedInfo = null;
                StringList slAlreadyFoundResourcePools = new StringList();
                
                for (RelationshipWithSelectItr relationshipWithSelectItr = new RelationshipWithSelectItr(relationshipWithSelectList);relationshipWithSelectItr.next();)
                { 
                	try{
						// Logged in Resource Manager can access not information of other resourcePools thats why pushing context.
                		ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
                	
	                    relationshipWithSelect = relationshipWithSelectItr.obj();
	                    strResourcePoolId = relationshipWithSelect.getSelectData(SELECT_RESOURCE_POOLS_ID);
	                    
	                    if (slAlreadyFoundResourcePools.contains(strResourcePoolId)) {
	                    	//Added:Jan 10, 2011:HP5:R211:PRG:IR-080992V6R2012  
	                    	for(int i=0; i<tempResult.size();i++)
	                    	{
	                    		Map tempMapConsolidatedInfo = (Map)tempResult.get(i);
	                    		String strTempResourcePoolId = (String)tempMapConsolidatedInfo.get(DomainConstants.SELECT_ID);
	                    		if(strResourcePoolId.equals(strTempResourcePoolId))
	                    		{
	                    			StringList slResourceManagersId = new StringList();
	                    			StringList slMembersId  = new StringList();
	                    			if(null!=tempMapConsolidatedInfo.get("SELECT_ORGANIZATION_MEMBERS_ID"))
	                    			{
	                    				slMembersId = (StringList)tempMapConsolidatedInfo.get("SELECT_ORGANIZATION_MEMBERS_ID");
	                    			}
	                    			if(null!=tempMapConsolidatedInfo.get("SELECT_RESOURCE_MANAGERS_NAME"))
	                    			{
	                    				slResourceManagersId = (StringList)tempMapConsolidatedInfo.get("SELECT_RESOURCE_MANAGERS_NAME");
	                    			}
	                    			if(null!=relationshipWithSelect.getSelectDataList(SELECT_ORGANIZATION_MEMBERS_ID))
	                    			{
	                    				slMembersId.addAll(relationshipWithSelect.getSelectDataList(SELECT_ORGANIZATION_MEMBERS_ID));
	                    			}
	                    			if(null!=relationshipWithSelect.getSelectDataList(SELECT_RESOURCE_MANAGERS_NAME))
	                    			{
	                    				slResourceManagersId.addAll(relationshipWithSelect.getSelectDataList(SELECT_RESOURCE_MANAGERS_NAME));
	                    			}
	                    			tempMapConsolidatedInfo.put("SELECT_ORGANIZATION_MEMBERS_ID", slMembersId);
	                    			tempMapConsolidatedInfo.put("SELECT_RESOURCE_MANAGERS_NAME", slResourceManagersId);
	                    		}
	                    	}
	                    	//End:Jan 10, 2011:HP5:R211:PRG:IR-080992V6R2012  
	                        continue;
	                    }
	                    slAlreadyFoundResourcePools.add(strResourcePoolId);
	                    
	                    mapConsolidatedInfo = new HashMap();
	                    mapConsolidatedInfo.put(DomainConstants.SELECT_ID, strResourcePoolId);
	                    mapConsolidatedInfo.put("SELECT_ORGANIZATION_MEMBERS_ID", relationshipWithSelect.getSelectDataList(SELECT_ORGANIZATION_MEMBERS_ID));
	                    mapConsolidatedInfo.put("SELECT_RESOURCE_MANAGERS_NAME", relationshipWithSelect.getSelectDataList(SELECT_RESOURCE_MANAGERS_NAME));	                    
	                    DomainObject dmoResourcePoolObject = DomainObject.newInstance(context,strResourcePoolId);
	                    StringList slRequestIds = dmoResourcePoolObject.getInfoList(context,SELECT_RESOURCE_REQUEST_ID);
	                    StringList slRequestStates = dmoResourcePoolObject.getInfoList(context,SELECT_RESOURCE_REQUEST_STATE);
	                    mapConsolidatedInfo.put("SELECT_RESOURCE_REQUEST_ID", slRequestIds);
	                    mapConsolidatedInfo.put("SELECT_RESOURCE_REQUEST_STATE", slRequestStates);                    
	                    //Modified:Jan 10, 2011:HP5:R211:PRG:IR-080992V6R2012  
                            tempResult.add(mapConsolidatedInfo);
	                    //End:Jan 10, 2011:HP5:R211:PRG:IR-080992V6R2012  
                	}catch(Exception e){
                		e.printStackTrace();
                	}finally{
                        //Set the context back to the context user
                        ContextUtil.popContext(context);
                   }
                }
                //Added:Jan 10, 2011:HP5:R211:PRG:IR-080992V6R2012  
        		if (!sAll.equalsIgnoreCase(strSelectedResourceManager))
                {
                	for(int i=0; i<tempResult.size();i++)
                	{
                		Map tempMapConsolidatedInfo = (Map)tempResult.get(i);
                		String strTempResourcePoolId = (String)tempMapConsolidatedInfo.get(DomainConstants.SELECT_ID);
                		StringList slResourceManagersId = new StringList();
                		StringList slMembersId  = new StringList();
                		if(null!=tempMapConsolidatedInfo.get("SELECT_RESOURCE_MANAGERS_NAME"))
                		{
                			slResourceManagersId = (StringList)tempMapConsolidatedInfo.get("SELECT_RESOURCE_MANAGERS_NAME");
                		}
                		if(slResourceManagersId.contains(strSelectedResourceManager))
                		{
                			mlResult.add(tempMapConsolidatedInfo);
                		}
                	}
                }
                else
                {
                	mlResult.addAll(tempResult);
                }
              //End:Jan 10, 2011:HP5:R211:PRG:IR-080992V6R2012 
              //Modified:6-Jan-2010:ixe:R209:PRG:IR-023305
            }else if (isExpandFunction){
                final String SELECT_ORGANIZATION_MEMBERS_ID = "from["+RELATIONSHIP_MEMBER+"].to.id";
                final String SELECT_RESOURCE_MANAGERS_NAME = "from["+RELATIONSHIP_RESOURCE_MANAGER+"].to.name";
                final String SELECT_RESOURCE_REQUEST_ID = "to["+RELATIONSHIP_RESOURCE_POOL+"].from.id";
                final String SELECT_RESOURCE_REQUEST_STATE = "to["+RELATIONSHIP_RESOURCE_POOL+"].from.current";
                
                String strTypePattern = TYPE_ORGANIZATION;
                
                StringList slBusSelect = new StringList();
                slBusSelect.add(DomainConstants.SELECT_ID);
                slBusSelect.add(SELECT_ORGANIZATION_MEMBERS_ID);//Reqd to calculate Capacity
                slBusSelect.add(SELECT_RESOURCE_REQUEST_ID);//Reqd to calculate Open requests
                slBusSelect.add(SELECT_RESOURCE_REQUEST_STATE);//Reqd to calculate Open requests
                slBusSelect.add(SELECT_RESOURCE_MANAGERS_NAME);//Reqd to find resource managers
                
                StringList slRelSelect = new StringList();
                short recurseToLevel = 1;
                String strBusWhere ="";
                //Added:Jan 13, 2011:HP5:R211:PRG:IR-038462V6R2012
        	if (sAll.equalsIgnoreCase(strSelectedResourceManager))
                {
                     strBusWhere = "";
                }
                else
                {
                    strBusWhere = ""+ SELECT_RESOURCE_MANAGERS_NAME +" smatch  \""+ strSelectedResourceManager +"\"";
                }
                MapList tempResult = new MapList();
                //End:Jan 13, 2011:HP5:R211:PRG:IR-038462V6R2012
                String strRelWhere = "";
                MapList mlContextResourcePools = dmoObject.getRelatedObjects(context,
                                                                                                        strRelPattern, //pattern to match relationships
                                                                                                        strTypePattern, //pattern to match types
                                                                                                        slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                                                                                        slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                                                                                        getTo, //get To relationships
                                                                                                        getFrom, //get From relationships
                                                                                                        recurseToLevel, //the number of levels to expand, 0 equals expand all.
                                                                                                        strBusWhere, //where clause to apply to objects, can be empty ""
                                                                                                        strRelWhere); //where clause to apply to relationship, can be empty ""

                
                // Consolidated this data against consistent keys so that it will be easy to process later
                Map mapContextResourcePool = null;
                Map mapConsolidatedInfo = null;
                for (Iterator itrContextResourcePools = mlContextResourcePools.iterator(); itrContextResourcePools.hasNext();) {
                    mapContextResourcePool = (Map) itrContextResourcePools.next();
                    mapConsolidatedInfo = new HashMap();
                    //Added:Jan 13, 2011:HP5:R211:PRG:IR-038462V6R2012
                    StringList slResourceManager = new StringList();
                    Object obj = (Object)mapContextResourcePool.get(SELECT_RESOURCE_MANAGERS_NAME);
                    if(null != obj)
                    {
                    	if(obj instanceof String)
                    	{
                    		slResourceManager.add((String)obj);
                    	}
                    	else
                    	{
                    		slResourceManager.addAll((StringList)obj);
                    	}
                    }
                    //End:Jan 13, 2011:HP5:R211:PRG:IR-038462V6R2012
                    mapConsolidatedInfo.put(DomainConstants.SELECT_ID, mapContextResourcePool.get(DomainConstants.SELECT_ID));
					mapConsolidatedInfo.put("level", mapContextResourcePool.get("level"));
                    mapConsolidatedInfo.put("SELECT_ORGANIZATION_MEMBERS_ID", mapContextResourcePool.get(SELECT_ORGANIZATION_MEMBERS_ID));
                    //Modified:Jan 13, 2011:HP5:R211:PRG:IR-038462V6R2012
                    mapConsolidatedInfo.put("SELECT_RESOURCE_MANAGERS_NAME", slResourceManager);
                    //End:Jan 13, 2011:HP5:R211:PRG:IR-038462V6R2012
                    mapConsolidatedInfo.put("SELECT_RESOURCE_REQUEST_ID", mapContextResourcePool.get(SELECT_RESOURCE_REQUEST_ID));
                    mapConsolidatedInfo.put("SELECT_RESOURCE_REQUEST_STATE", mapContextResourcePool.get(SELECT_RESOURCE_REQUEST_STATE));
                    //Modified:Jan 13, 2011:HP5:R211:PRG:IR-038462V6R2012
                    tempResult.add(mapConsolidatedInfo);
                    //End:Jan 13, 2011:HP5:R211:PRG:IR-038462V6R2012
                }
                //Added:Jan 13, 2011:HP5:R211:PRG:IR-038462V6R2012
        	if (sAll.equalsIgnoreCase(strSelectedResourceManager))
                {
                	for(int i=0; i<tempResult.size();i++)
                	{
                		Map tempMapConsolidatedInfo = (Map)tempResult.get(i);
                		StringList slResourceManagersName = (StringList)tempMapConsolidatedInfo.get("SELECT_RESOURCE_MANAGERS_NAME");
                		if(!slResourceManagersName.isEmpty())
                		{
                			mlResult.add(tempMapConsolidatedInfo);
                		}
                	}
                }
                else
                {
                	mlResult.addAll(tempResult);
                }
                //End:Jan 13, 2011:HP5:R211:PRG:IR-038462V6R2012
            }
          //End Modified:6-Jan-2010:ixe:R209:PRG:IR-023305
            
            // Calculate the Capacity,Open Requests & resource managers here itself
            Map mapResourcePool = null;    
            Object objValue = null;
            StringList slValue = null;
            String strValue = null;
            int nOpenRequests = 0;

            for (Iterator itrResourcePools = mlResult.iterator(); itrResourcePools.hasNext();) 
            {
                mapResourcePool = (Map) itrResourcePools.next();
                
                // Capacity
                objValue = mapResourcePool.get("SELECT_ORGANIZATION_MEMBERS_ID");
                
                StringList slCapacityValue = new StringList();
                if (objValue == null) 
                {
                    mapResourcePool.put(STRING_CAPACITY, "0");
                }
                else if (objValue instanceof String) 
                {
                    mapResourcePool.put(STRING_CAPACITY, "1");
                }
                else if (objValue instanceof StringList) 
                {
                    slValue = (StringList)objValue;

                	if(slPMCUsers.size()>0)
                	{
	                	for(int i = 0; i < slValue.size(); i++)
	                	{
	                		if(slPMCUsers.contains(slValue.get(i)))
	                		{
	                			slCapacityValue.add(slValue.get(i));
	                		}
	                	}
                	}
                	mapResourcePool.put(STRING_CAPACITY, String.valueOf(slCapacityValue.size()));
                }
                
                //Open Requests
                String strRequestID = "";
              
                StringList slRequestId = new StringList();
                objValue = mapResourcePool.get("SELECT_RESOURCE_REQUEST_ID");
                
                if (objValue == null) 
                {
                    mapResourcePool.put("Open_Requests", "0");
                }
                else if (objValue instanceof String) 
                {
                	//Modidfied by ixe bfor IR-019137V6R2011
                	strRequestID = (String) mapResourcePool.get("SELECT_RESOURCE_REQUEST_ID");
                	if(null != strRequestID){
                		DomainObject dmoRequest =DomainObject.newInstance(context, strRequestID);
                		
                		String strProjectState = DomainObject.EMPTY_STRING;
                		try{
                			ProgramCentralUtil.pushUserContext(context);
                			strProjectState = dmoRequest.getInfo(context, "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_CURRENT);
                		}finally{
                			ProgramCentralUtil.popUserContext(context);
                		}
                		
                		//Modified:23-June-2010:s4e:R210 PRG:ARP
                		//Added null check for request related to Resource plan template
                		if(null!=strProjectState && (!strProjectState.equals(DomainConstants.STATE_PROJECT_SPACE_COMPLETE) || !strProjectState.equals(DomainConstants.STATE_PROJECT_SPACE_REVIEW)|| !strProjectState.equals(DomainConstants.STATE_PROJECT_SPACE_ARCHIVE))){
                			strValue = (String) mapResourcePool.get("SELECT_RESOURCE_REQUEST_STATE");
                			if(RESOURCE_REQUEST_STATE_REQUESTED.equals(strValue) || RESOURCE_REQUEST_STATE_PROPOSED.equals(strValue))
                			{
                				mapResourcePool.put("Open_Requests", "1");
                			}
                			else
                			{
                				mapResourcePool.put("Open_Requests", "0");
                			}
                		}
                	}
                }
                else if (objValue instanceof StringList) 
                {
                	//Modidfied by ixe for IR-019137V6R2011
                	slRequestId = (StringList)mapResourcePool.get("SELECT_RESOURCE_REQUEST_ID");
                	String[] slRequestIdList =new String[slRequestId.size()];
                	slRequestId.copyInto(slRequestIdList);
                	//slValue = (StringList)mapResourcePool.get("SELECT_RESOURCE_REQUEST_STATE");
                	nOpenRequests = 0;
                	// for(StringItr stringItr = new StringItr(slValue); stringItr.next();)

                	BusinessObjectWithSelectList resourceRequestObjWithSelectList = null;
                	BusinessObjectWithSelect bows = null;
                	final String SELECT_REL_RESOURCE_PLAN_PROJECT_ID = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_ID;
                	StringList slProjectSelectList = new StringList();
                	slProjectSelectList.add(SELECT_REL_RESOURCE_PLAN_PROJECT_ID);
                	slProjectSelectList.add("to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_CURRENT);
                	
                	try{
            			ProgramCentralUtil.pushUserContext(context);
                	resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,slRequestIdList,slProjectSelectList);
            		}finally{
            			ProgramCentralUtil.popUserContext(context);
            		}
                	
                	for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
                	{
                		String strProjectId = "";
                		String strCurrentState = "";
                		bows = itr.obj();
                		strProjectId = bows.getSelectData(SELECT_REL_RESOURCE_PLAN_PROJECT_ID);
                		String strProjectState = bows.getSelectData("to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].from."+DomainConstants.SELECT_CURRENT);
                		//Modified:23-June-2010:s4e:R210 PRG:ARP
                		//Added null check for request related to Resource plan template
                			if(null!=strProjectState && (!strProjectState.equals(DomainConstants.STATE_PROJECT_SPACE_CREATE) && !strProjectState.equals(DomainConstants.STATE_PROJECT_SPACE_ASSIGN)&& !strProjectState.equals(DomainConstants.STATE_PROJECT_SPACE_ACTIVE))){
                				slRequestId.remove(bows.getObjectId()) ;
                			}
                	}
                	for(StringItr stringItr = new StringItr(slRequestId); stringItr.next();)
                	{
                		strRequestID = stringItr.obj();
                		if(null != strRequestID){
                			DomainObject dmoRequest =DomainObject.newInstance(context, strRequestID);



                			strValue =  dmoRequest.getInfo(context,SELECT_CURRENT);
                			if(RESOURCE_REQUEST_STATE_REQUESTED.equals(strValue) || RESOURCE_REQUEST_STATE_PROPOSED.equals(strValue))
                			{
                				nOpenRequests++;
                			}


                		}
                	}
                	mapResourcePool.put("Open_Requests", String.valueOf(nOpenRequests));
                }
                
                
                // Resource managers
                objValue = mapResourcePool.get("SELECT_RESOURCE_MANAGERS_NAME");
                if (objValue == null) {
                    mapResourcePool.put("Resource_Managers", "");
                }
                else if (objValue instanceof String) {
                    strValue = (String)objValue;
                    
                    mapResourcePool.put("Resource_Managers", strValue);
                }
                else if (objValue instanceof StringList) {
                    slValue = (StringList)objValue;
                    
                    mapResourcePool.put("Resource_Managers", FrameworkUtil.join(slValue, ","));
                }
            }
            
            return mlResult;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new MatrixException(ex);
        }

    }
    /**
     * This Method Return Resource Pools related to the Context user.
     * 
     * @param context Matrix Context Object
     * @param args String array
     * @return MapList
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableMyResourcePoolsData(Context context, String[] args) throws MatrixException {
        try
        {
            if(context == null)
            {
                throw new MatrixException("Null Context !");
            }
            Map programMap = (Map) JPO.unpackArgs(args);
            String strPMCResourcePoolFilter = (String)programMap.get("PMCResourcePoolFilter");
            
            String strRelPattern = RELATIONSHIP_RESOURCE_MANAGER;
            String strObjectId = PersonUtil.getPersonObjectID(context);
            
           StringList slAllResourceManagers = getAllResourceManagers(context,args);
           String strSelectedResourceManager = (String)programMap.get("PMCResourcePoolResourceManagerFilter");
          
           return getTableResourcePoolsData(context, strPMCResourcePoolFilter, strSelectedResourceManager,strObjectId, strRelPattern, true, false, false);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new MatrixException(ex);
        }
    }

    /**
     * This Method Return Child Resource Pools when a Resource Pool is expanded in PMCResourcePoolSummary table.
     * 
     * @param context Matrix Context Object
     * @param args String array
     * @return MapList
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableExpandChildResourcePoolsData(Context context, String[] args) throws MatrixException {
        try
        {
            if(context == null)
            {
                throw new MatrixException("Null Context !");
            }
            
            Map programMap = (Map)JPO.unpackArgs(args);
            String strPMCResourcePoolFilter = (String)programMap.get("PMCResourcePoolFilter");
            String strPMCResourcePoolResourceManagerFilter = (String)programMap.get("PMCResourcePoolResourceManagerFilter");
            
            String strParentResourcePoolId = (String)programMap.get("objectId");
            String strRelPattern = RELATIONSHIP_DIVISION + "," + RELATIONSHIP_COMPANY_DEPARTMENT + "," + RELATIONSHIP_ORGANIZATION_PLANT + "," + RELATIONSHIP_SUBSIDIARY;
            
            return getTableResourcePoolsData(context, strPMCResourcePoolFilter, strPMCResourcePoolResourceManagerFilter,strParentResourcePoolId, strRelPattern, false, true, true);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new MatrixException(ex);
        }
    }

    /**
     * Gets the data for the column "Open Requests" for table "PMCResourcePoolSummary"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing no. of Open Requests available to particular Resource Pool
     * @throws Exception if operation fails
     */
    public Vector getColumnOpenRequestsData(Context context, String[] args) throws Exception 
    {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");

            Map mapRowData = null;
            for (Iterator itrRowData = objectList.iterator(); itrRowData.hasNext();) {
                mapRowData = (Map) itrRowData.next();
                
                vecResult.add(mapRowData.get("Open_Requests"));
            }
            
            return vecResult;

        } catch (Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    
    /**
     * Provides data for Resource Pool people table "PMCResourcePoolPeople"
     * 
     * @param context The Matrix Context object
     * @param args packed arguments by emxIndentedTable.jsp
     * @return MapList of the table data
     * @throws Exception if operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableResourcePoolPeopleData(Context context, String[] args) throws MatrixException {
        try {
            
            Map programMap = (Map)JPO.unpackArgs(args);
            String strResourcePoolId = (String)programMap.get("objectId");
            
            final String SELECT_ATTRIBUTE_EMAIL_ADDRESS = "attribute[" + ATTRIBUTE_EMAIL_ADDRESS + "]";
           final String SELECT_REL_ATTRIBUTE_PROJECT_ROLE = "from["+RELATIONSHIP_LEAD_RESPONSIBILITY+"].attribute[" + ATTRIBUTE_PROJECT_ROLE + "]";
           final String SELECT_REL_ATTRIBUTE_PROJECT_ROLE_ID = "from["+RELATIONSHIP_LEAD_RESPONSIBILITY+"].fromtype.id";
            final String SELECT_PERSON_BUSINESS_SKILL = "from[" + RELATIONSHIP_HAS_BUSINESS_SKILL + "].to.name";
            // 
            // Following code find the connected members recursively 
            //
            DomainObject dmoResourcePool = DomainObject.newInstance(context, strResourcePoolId);
    
           String strRelationshipPattern = RELATIONSHIP_LEAD_RESPONSIBILITY + "," + RELATIONSHIP_MEMBER + ","+ RELATIONSHIP_DIVISION + "," + RELATIONSHIP_COMPANY_DEPARTMENT + "," + RELATIONSHIP_ORGANIZATION_PLANT + "," + RELATIONSHIP_SUBSIDIARY;
            String strTypePattern = TYPE_PERSON;
    
            StringList slBusSelect = new StringList();
            slBusSelect.add(SELECT_ID);
            slBusSelect.add(SELECT_NAME);
            slBusSelect.add(SELECT_ATTRIBUTE_EMAIL_ADDRESS);
            slBusSelect.add(SELECT_PERSON_BUSINESS_SKILL);
    
            StringList slRelSelect = new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);
           slRelSelect.add(SELECT_REL_ATTRIBUTE_PROJECT_ROLE);
            
            boolean getTo = false;
            boolean getFrom = true;
            short recurseToLevel = 0;
            String strBusWhere = "";
            String strRelWhere = "";
            //String strRelWhere = " "SELECT_REL_ATTRIBUTE_PROJECT_ROLE_ID == "+strResourcePoolId+\";
          // String strRelWhere = ""+SELECT_REL_ATTRIBUTE_PROJECT_ROLE_ID+" == \"" + strResourcePoolId + "\"";
            MapList mlPersons = dmoResourcePool.getRelatedObjects(context,
                                                                                                    strRelationshipPattern, //pattern to match relationships
                                                                                                    strTypePattern, //pattern to match types
                                                                                                    slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                                                                                    slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                                                                                    getTo, //get To relationships
                                                                                                    getFrom, //get From relationships
                                                                                                    recurseToLevel, //the number of levels to expand, 0 equals expand all.
                                                                                                    strBusWhere, //where clause to apply to objects, can be empty ""
                                                                                                    strRelWhere); //where clause to apply to relationship, can be empty ""
            
            //
            // Find PMC users
            //            
            String strPMCSymbolicRoles = EnoviaResourceBundle.getProperty(context, "eServiceSuiteProgramCentral.Roles");
            StringList slPMCSymbolicRoles = FrameworkUtil.split(strPMCSymbolicRoles, ",");            
            String strRole = null;
            Role matrixRole = null;
            UserList assignments = null;
                
			StringList slPMCRoles = new StringList(slPMCSymbolicRoles.size());
			Set<String> roleSet = new HashSet<String>();
			for (Iterator itrPMCSymbolicRoles = slPMCSymbolicRoles.iterator(); itrPMCSymbolicRoles.hasNext();) {
				String strPMCSymbolicRole = (String) itrPMCSymbolicRoles.next();
				String strPMCRole = PropertyUtil.getSchemaProperty(context, strPMCSymbolicRole);
				StringList slChildRoles = ProgramCentralUtil.getRoleHierarchy(context, strPMCRole);
				roleSet.add(strPMCRole);
				roleSet.addAll(slChildRoles);
            }
			String loggedInUserName = context.getUser();
            MapList mlResult = new MapList();
			try{
				for (Iterator itrPerson = mlPersons.iterator(); itrPerson.hasNext();) {
					Map personInfoMap = (Map) itrPerson.next();
					String personName = (String)personInfoMap.get(SELECT_NAME);
					context.setUser(personName);
					//Here for each person getAssignment is being called. This could be performance
					//issue. Need refactoring
					Vector<String> roles = PersonUtil.getAssignments(context);
					for (Iterator itrRole = roles.iterator(); itrRole.hasNext();) {
						String role = (String) itrRole.next();
						if(roleSet.contains(role)){
							mlResult.add(personInfoMap);
							break;
						}
                    }
                }
			}finally{
				context.setUser(loggedInUserName);
            }
            return mlResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }

    /**
     * Gets the data for the column "FullName" for table "PMCResourcePoolPeople"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnFullNameData(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();
    
            // Get object list information from packed arguments
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map paramList = (Map) programMap.get("paramList");
    
            Map mapObjectInfo = null;
    
            // Do for each object
            String strPersonName = null;
            String strPersonFullName = null;
            String strObjectId="";
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) {
                mapObjectInfo = (Map) itrObjects.next();
                //[MODIFIED::Dec 30, 2010:s4e:R211:IR-020546V6R2012::Start] 
                //Modified to get fullname only for type Person, because the column also displays Project name.
                strObjectId= (String)mapObjectInfo.get(SELECT_ID);
                if(null!=strObjectId && !"null".equalsIgnoreCase(strObjectId) && !"".equals(strObjectId))
                {       
                	DomainObject objectDo = DomainObject.newInstance(context,strObjectId);
                	strPersonName = (String)mapObjectInfo.get(SELECT_NAME);
                    if(objectDo.isKindOf(context, TYPE_PERSON))
                    {                
                    	strPersonFullName = PersonUtil.getFullName(context, strPersonName);
                    }
                    else{
                    	strPersonFullName=strPersonName;
                    }
                    vecResult.add(strPersonFullName);
                }
              //[MODIFIED::Dec 30, 2010:s4e:R211:IR-020546V6R2012::End] 
            }
            return vecResult;
        } catch (Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets the data for the column "Email" for table "PMCResourcePoolPeople"
     * 
     * @param context The matrix context object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getColumnEmailData(Context context, String[] args) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();
    
            // Get object list information from packed arguments
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map paramList = (Map) programMap.get("paramList");
    
            final String SELECT_ATTRIBUTE_EMAIL_ADDRESS = "attribute[" + ATTRIBUTE_EMAIL_ADDRESS + "]";
            
            Map mapObjectInfo = null;
    
            // Do for each object
            StringBuffer strEmailHTML = null;
            String strEmail = null;
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();)
            {
                mapObjectInfo = (Map) itrObjects.next();
                strEmail = (String)mapObjectInfo.get(SELECT_ATTRIBUTE_EMAIL_ADDRESS);                
                
                strEmailHTML = new StringBuffer(64);
                if(null == strEmail  || "null".equals(strEmail))
                {
                    strEmail = "";
                }
                strEmailHTML.append("<a href=\"mailto:").append(strEmail).append("\">").append(strEmail).append("</a>");
                
                vecResult.add(strEmailHTML.toString());
            }
            return vecResult;
        } catch (Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }
    
    /**
     * This Method is used to show the Resource Pool Category(Tree)- Business Unit Data.
     * Provides data for Resource Pool people table "PMCResourcePoolBusinessUnits"
     * This Method is also used for expanding the "PMCResourcePoolBusinessUnits" table.
     * @param context
     * @param args
     * @return MapList 
     * @throws MatrixException
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableResourcePoolBusinessUnitData(Context context, String[] args) throws MatrixException 
    {
        try
        {          
            if(context == null)
            {
                throw new MatrixException("Null Context !");
            }
            Map programMap = (Map)JPO.unpackArgs(args);
            String strParentResourcePoolId = (String)programMap.get("objectId");
            String strRelPattern = RELATIONSHIP_DIVISION;
            boolean getTo = false;
            boolean getFrom = true;
            
            final String SELECT_RESOURCE_MANAGERS_NAME = "from["+DomainConstants.RELATIONSHIP_RESOURCE_MANAGER+"].to.name";
            
            String strTypePattern = TYPE_BUSINESS_UNIT;
           
            StringList slBusSelect = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            slBusSelect.add(SELECT_RESOURCE_MANAGERS_NAME);//Reqd to find resource managers
                 
            StringList slRelSelect = new StringList();
            
            short recurseToLevel = 1;
            String strBusWhere = "";
            String strRelWhere = "";
         
            DomainObject dmoResourcePool = DomainObject.newInstance(context, strParentResourcePoolId);
            MapList mlResourcePoolBusinessUnits = dmoResourcePool.getRelatedObjects(context,
                                                                          strRelPattern, //pattern to match relationships
                                                                          strTypePattern, //pattern to match types
                                                                          slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                                                          slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                                                          getTo, //get To relationships
                                                                          getFrom, //get From relationships
                                                                          recurseToLevel, //the number of levels to expand, 0 equals expand all.
                                                                          strBusWhere, //where clause to apply to objects, can be empty ""
                                                                          strRelWhere); //where clause to apply to relationship, can be empty ""
           Map mapResourcePoolBusinessUnits = null;    
           Object objValue = null;
           StringList slValue = null;
           String strValue = "";
           MapList mlFilteredResourcePoolBusinessUnits = new MapList();
           for (Iterator itrResourcePools = mlResourcePoolBusinessUnits.iterator(); itrResourcePools.hasNext();) 
           {
               mapResourcePoolBusinessUnits = (Map) itrResourcePools.next();
               objValue = mapResourcePoolBusinessUnits.get(SELECT_RESOURCE_MANAGERS_NAME);
               //check if resource managers are present then only show this business unit
               if (objValue != null) 
               {
                   if (objValue instanceof String) {
                       strValue = (String)objValue;
                       mapResourcePoolBusinessUnits.put("Resource_Managers", strValue);
                   }
                   else if (objValue instanceof StringList) {
                       slValue = (StringList)objValue;
                       mapResourcePoolBusinessUnits.put("Resource_Managers", FrameworkUtil.join(slValue, ","));
                   }
                   mlFilteredResourcePoolBusinessUnits.add(mapResourcePoolBusinessUnits);
               }
           }
           return mlFilteredResourcePoolBusinessUnits;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new MatrixException(ex);
        }
    }
    
    /**
     * This Method is used to show the Resource Pool Category(Tree)- Department Data.
     * Provides data for Resource Pool people table "PMCResourcePoolDepartments"
     * @param context Matrix context Object
     * @param args String array
     * @return maplist  information about resource pool departments
     * @throws MatrixException
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableResourcePoolDepartmentData(Context context, String[] args) throws MatrixException 
    {
        try
        {
            if(context == null)
            {
                throw new MatrixException("Null Context !");
            }
            
            Map programMap = (Map)JPO.unpackArgs(args);
            String strParentResourcePoolId = (String)programMap.get("objectId");
            String strRelPattern = RELATIONSHIP_COMPANY_DEPARTMENT;
            boolean getTo = false;
            boolean getFrom = true;
            
            final String SELECT_RESOURCE_MANAGERS_NAME = "from["+RELATIONSHIP_RESOURCE_MANAGER+"].to.name";
            
            String strTypePattern = TYPE_DEPARTMENT;
           
            StringList slBusSelect = new StringList();
            slBusSelect.add(DomainConstants.SELECT_ID);
            slBusSelect.add(SELECT_RESOURCE_MANAGERS_NAME);//Reqd to find resource managers
                 
            StringList slRelSelect = new StringList();
            
            short recurseToLevel = 1;
            String strBusWhere = "";
            String strRelWhere = "";
         
            DomainObject dmoObject = DomainObject.newInstance(context, strParentResourcePoolId);
            MapList mlResourcePoolDepartments = dmoObject.getRelatedObjects(context,
                                                                          strRelPattern, //pattern to match relationships
                                                                          strTypePattern, //pattern to match types
                                                                          slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                                                          slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                                                          getTo, //get To relationships
                                                                          getFrom, //get From relationships
                                                                          recurseToLevel, //the number of levels to expand, 0 equals expand all.
                                                                          strBusWhere, //where clause to apply to objects, can be empty ""
                                                                          strRelWhere); //where clause to apply to relationship, can be empty ""
            Map mapResourcePoolDepartments = null;    
            Object objValue = null;
            String strValue = "";
            StringList slValue = null;
            MapList mlFilteredResourcePoolDepartments = new MapList();
            for (Iterator itrResourcePools = mlResourcePoolDepartments.iterator(); itrResourcePools.hasNext();) 
            {
                mapResourcePoolDepartments = (Map) itrResourcePools.next();
                objValue = mapResourcePoolDepartments.get(SELECT_RESOURCE_MANAGERS_NAME);
                //check if resource managers are present then only show this Department
                if (objValue != null) 
                {
                    if (objValue instanceof String) {
                        strValue = (String)objValue;
                        mapResourcePoolDepartments.put("Resource_Managers", strValue);
                    }
                    else if (objValue instanceof StringList) {
                        slValue = (StringList)objValue;
                        mapResourcePoolDepartments.put("Resource_Managers", FrameworkUtil.join(slValue, ","));
                    }
                    mlFilteredResourcePoolDepartments.add(mapResourcePoolDepartments);
                }
            }
            return mlFilteredResourcePoolDepartments;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new MatrixException(ex);
        }
    }
    
   /**
    * Returns info about all peoples associated to resource Request when navigates to Request Tree
    * @param context Matrix context object
    * @param args String array
    * @return MapList of allocated peoples to request
    * @throws MatrixException
     */
    
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableResourceRequestPeopleData(Context context,String[] args) throws MatrixException
    {
        try{
            if(context == null)
            {
                throw new MatrixException("Null Context !");
            }
            
            Map programMap = (Map)JPO.unpackArgs(args);
            String strResourceRequestId = (String)programMap.get("objectId");
            DomainObject dmoResourceRequest = DomainObject.newInstance(context, strResourceRequestId);
            
            final String SELECT_ATTRIBUTE_EMAIL_ADDRESS = "attribute[" + ATTRIBUTE_EMAIL_ADDRESS + "]";
            
            String strRelationshipPattern = DomainConstants.RELATIONSHIP_ALLOCATED;
            String strTypePattern = DomainConstants.TYPE_PERSON;
            
            StringList slBusSelect = new StringList();
            slBusSelect.add(SELECT_ID);
            slBusSelect.add(SELECT_NAME);
            slBusSelect.add(SELECT_ATTRIBUTE_EMAIL_ADDRESS);
            
            StringList slRelSelect = new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);
            boolean getTo = false; 
            boolean getFrom = true; 
            short recurseToLevel = 1;
            String strBusWhere = "";
            String strRelWhere = "";
            
            MapList mlResourceRequestPeoples = dmoResourceRequest.getRelatedObjects(context,
                                                                                            strRelationshipPattern, //pattern to match relationships
                                                                                            strTypePattern, //pattern to match types
                                                                                            slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                                                                            slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                                                                            getTo, //get To relationships
                                                                                            getFrom, //get From relationships
                                                                                            recurseToLevel, //the number of levels to expand, 0 equals expand all.
                                                                                            strBusWhere, //where clause to apply to objects, can be empty ""
                                                                                            strRelWhere); //where clause to apply to relationship, can be empty ""
                
          
            return mlResourceRequestPeoples;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new MatrixException(ex);
        }
    }
    
  /**
   * Returns skill associated to resource Request
   * @param context
   * @param args
   * @return
   * @throws MatrixException
    */
    
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getResourceRequestSkills(Context context,String[] args) throws MatrixException
    {
        try
        {
            if(context == null)
            {
                throw new MatrixException("Null Context !");
            }
            
            Map programMap = (Map)JPO.unpackArgs(args);
            String strResourceRequestId = (String)programMap.get("objectId");
            DomainObject dmoObject = DomainObject.newInstance(context, strResourceRequestId);
            
            String strRelationshipPattern = DomainConstants.RELATIONSHIP_RESOURCE_REQUEST_SKILL;
            String strTypePattern = DomainConstants.TYPE_BUSINESS_SKILL;
            
            StringList slBusSelect = new StringList();
            slBusSelect.add(DomainObject.SELECT_ID);
            slBusSelect.add(DomainObject.SELECT_NAME);
            
            StringList slRelSelect = new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);
            
            boolean getTo = false; 
            boolean getFrom = true; 
            short recurseToLevel = 1;
            String strBusWhere = "";
            String strRelWhere = "";
            
            MapList mlRelatedObjects = dmoObject.getRelatedObjects(context,
                                                                                            strRelationshipPattern, //pattern to match relationships
                                                                                            strTypePattern, //pattern to match types
                                                                                            slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                                                                            slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                                                                            getTo, //get To relationships
                                                                                            getFrom, //get From relationships
                                                                                            recurseToLevel, //the number of levels to expand, 0 equals expand all.
                                                                                            strBusWhere, //where clause to apply to objects, can be empty ""
                                                                                            strRelWhere); //where clause to apply to relationship, can be empty ""
            Map mapRelatedObjectInfo = null;
            String strBusinessSkill = null;
            StringList slBusinessSkills = new StringList();
            for (Iterator itrRelatedObjects = mlRelatedObjects.iterator(); itrRelatedObjects.hasNext();)
            {
                mapRelatedObjectInfo = (Map) itrRelatedObjects.next();
                strBusinessSkill = (String)mapRelatedObjectInfo.get(DomainObject.SELECT_NAME);
                slBusinessSkills.add(strBusinessSkill);
            }
            return mlRelatedObjects;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new MatrixException(ex);
        }
    }
    
  /**This will list all active projects(create,assign,active,review) for users in resource pool
     * 
   * @param context The Matrix Context Object.
   * @param args String array
   * @return MapList of all active projects
   * @throws MatrixException
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getTableExpandChildPeopleProjectData(Context context,String[] args)throws MatrixException
    {
        try
        {
            if(context == null)
            {
                throw new MatrixException("Null Context !");
            }
            
            Map programMap = (Map)JPO.unpackArgs(args);
            String strResourceId = (String)programMap.get("objectId");
            DomainObject dmoObject = DomainObject.newInstance(context, strResourceId);
            
            String strRelationshipPattern = RELATIONSHIP_MEMBER ;
            String strTypePattern = TYPE_PROJECT_SPACE;
            
            StringList slBusSelect = new StringList();
            slBusSelect.add(SELECT_ID);
            slBusSelect.add(SELECT_NAME);
            slBusSelect.add(SELECT_CURRENT);
            
            StringList slRelSelect = new StringList();
            slRelSelect.add(DomainRelationship.SELECT_ID);
            
            boolean getTo = true; 
            boolean getFrom = false; 
            short recurseToLevel = 1;
          //String strBusWhere = "DomainObject.SELECT_CURRENT~~DomainConstants.STATE_PROJECT_SPACE_ACTIVE";
            String strBusWhere = "";
            String strRelWhere = "";
            
            MapList mlPeoplesProject = dmoObject.getRelatedObjects(context,
                                                                                            strRelationshipPattern, //pattern to match relationships
                                                                                            strTypePattern, //pattern to match types
                                                                                            slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                                                                            slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                                                                            getTo, //get To relationships
                                                                                            getFrom, //get From relationships
                                                                                            recurseToLevel, //the number of levels to expand, 0 equals expand all.
                                                                                            strBusWhere, //where clause to apply to objects, can be empty ""
                                                                                            strRelWhere); //where clause to apply to relationship, can be empty ""
            Map mapRelatedObjectInfo = null;
            String strProjectName = null;
            String strProjectId = null;
            String strCurrentState = null;
            boolean isInActiveProject = false;
            for (Iterator itrProjects = mlPeoplesProject.iterator(); itrProjects.hasNext();)
            {
                mapRelatedObjectInfo = (Map) itrProjects.next();
                strCurrentState = (String)mapRelatedObjectInfo.get(SELECT_CURRENT);
                strProjectName = (String)mapRelatedObjectInfo.get(SELECT_NAME);
                strProjectId = (String) mapRelatedObjectInfo.get(SELECT_ID); 
                isInActiveProject = PolicyUtil.checkState(context,strProjectId,STATE_PROJECT_SPACE_COMPLETE,PolicyUtil.GE);
                if (isInActiveProject)
                {
                    itrProjects.remove();
                }
            }
            return mlPeoplesProject;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new MatrixException(ex);
        }
        
    }
    /**
     * Returns the Organizational Roles for Resource Pool Peoples
     *
     * @param context The Matrix Context.
     * @param args holds object id list and parameter list
     * @return a Vector of Organizational Role values
     * 
     */
    public Vector getColumnOrganizationalRolesData(Context context, String[] args)throws Exception
    { 
        try
        {
            
            // Create result vector
            Vector vecResult = new Vector();
            
            // Get object list information from packed arguments
            Map programMap = (Map) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map paramList = (Map) programMap.get("paramList");
            
            Map mapObjectInfo = null;
            
            // Do for each object
            String strPersonName = null;
            String strPersonRoleName = null;
            String strRelId =  null;
            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) 
            {
                mapObjectInfo = (Map) itrObjects.next();
                strPersonName = (String)mapObjectInfo.get(SELECT_NAME);
                strRelId = (String)mapObjectInfo.get(DomainRelationship.SELECT_ID);
                
                strPersonRoleName = Organization.getMemberRolesAsString(context,strRelId);
                vecResult.add(strPersonRoleName);
            }
            
            return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
        }
      
    
/**
 *  This will return Project roles to the persons in resource Pool.
 *  
 * @param context Matrix Context Object
 * @param args String array
 * @return vector corresponding Project roles
 * @throws MatrixException
 */
    
    public Vector getColumnProjectRolesData(Context context,String[] args) throws Exception
    {
    	Map programMap = (Map) JPO.unpackArgs(args);
    	MapList objectList = (MapList) programMap.get("objectList");

    	Map mapObjectInfo = null;

    	// Do for each object
    	String strProjectRole = null;
    	String strRelId =  null;
    	String strObjectId =  null;
    	Vector vecResult = new Vector();

    	
    	for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) 
    	{
    		mapObjectInfo = (Map) itrObjects.next();
    		strObjectId = (String)mapObjectInfo.get(SELECT_ID);
    		strRelId = (String)mapObjectInfo.get(DomainRelationship.SELECT_ID);
    		DomainRelationship dmoRelationship = new DomainRelationship(strRelId);
    		strProjectRole = dmoRelationship.getAttributeValue(context, ATTRIBUTE_PROJECT_ROLE);
            DomainObject dmoObject = newInstance(context,strObjectId);
            
            String roleTranslated = i18nNow.getRoleI18NString(strProjectRole, context.getSession().getLanguage());
            
    		if (dmoObject.isKindOf(context, TYPE_PERSON))
    		{
    			vecResult.add(EMPTY_STRING);
    		}
    		else if(dmoObject.isKindOf(context,TYPE_PROJECT_SPACE))
    		{
    			vecResult.add(roleTranslated);
    		}
    	}
    	return vecResult;

    }
    
    
    /**
     * Returns the Lead roles for Resource Pool Peoples in that Organization
     *
     * @param context The Matrix Context.
     * @param args holds object id list and parameter list
     * @return a Vector of Lead role values
     * 
     */
    public Vector getColumnLeadRolesData(Context context, String[] args)throws Exception
    { 
        try
        {
    		Map programMap = (Map) JPO.unpackArgs(args);

    		MapList objectList = (MapList) programMap.get("objectList");
    		Map mapParamMap = (Map) programMap.get("paramList");
    		String strOrganizationId = (String) mapParamMap.get("objectId");
    		Map mapObjectInfo = null;
    		final String SELECT_REL_ATTRIBUTE_LEAD_ROLE = "from["+RELATIONSHIP_LEAD_RESPONSIBILITY+"].attribute[" + ATTRIBUTE_PROJECT_ROLE + "]";
    		final String SELECT_REL_ATTRIBUTE_PERSON_ID = "from["+RELATIONSHIP_LEAD_RESPONSIBILITY+"].to.id";
    		// Do for each object
    		Vector vecResult = new Vector();

    		Map mapLeadRolePersonId = null;

    		String strObjectId = null;
    		String [] strOrganizationIds = new String[1];
    		strOrganizationIds[0] = strOrganizationId;
    		DomainObject dmoObject;
    		StringList slSelectList = new StringList();
    		slSelectList.add(SELECT_REL_ATTRIBUTE_LEAD_ROLE);
    		slSelectList.add(SELECT_REL_ATTRIBUTE_PERSON_ID);

    		BusinessObjectWithSelectList bos = BusinessObject.getSelectBusinessObjectData(context, strOrganizationIds, slSelectList);
    		BusinessObjectWithSelect bows = null;
    		Map mapQueryWithDataList = new HashMap();
            StringList slPersonId = new StringList();
    		StringList slLeadRole = new StringList();
    		
            for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(bos); itr.next();)
            {
                bows = itr.obj();
                slPersonId = (StringList)bows.getSelectDataList(SELECT_REL_ATTRIBUTE_PERSON_ID);
                slLeadRole = (StringList)bows.getSelectDataList(SELECT_REL_ATTRIBUTE_LEAD_ROLE);
            }
    		for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) 
    		{
    			mapObjectInfo = (Map) itrObjects.next();
    			strObjectId = (String)mapObjectInfo.get(SELECT_ID);
    			dmoObject = newInstance(context,strObjectId);

    			if (dmoObject.isKindOf(context,TYPE_PERSON))
    			{
    				if(null!=slPersonId && slPersonId.contains(strObjectId))
    				{
    					int nIndex = slPersonId.indexOf(strObjectId);
    					String strLeadRole = (String)slLeadRole.get(nIndex);
    					StringList slSplitLeadRole = FrameworkUtil.split(strLeadRole, "~");
    					StringList sli18LeadRole = new StringList();
    					for(int i=0;i<slSplitLeadRole.size();i++)
    					{
    						String strNLeadRole = "";
    						String roleSymbolic = (String)slSplitLeadRole.get(i);
						String roleName = PropertyUtil.getSchemaProperty(context,roleSymbolic);
    	                   
    	                    String roleTranslated = i18nNow.getRoleI18NString(roleName, context.getSession().getLanguage());
    	                    if (roleTranslated != null && roleTranslated.length() > 0) 
    	                    {
    	                    	sli18LeadRole.add(roleTranslated);
    	                    }
    	                    else 
    	                    {
    	                    	sli18LeadRole.add(roleName);
    	                    }
    					}
    					strLeadRole = FrameworkUtil.join(sli18LeadRole, ",");
    					vecResult.add(strLeadRole);
    				}
    				else
    				{
    					vecResult.add(EMPTY_STRING);
    				}
    			}
    			else
    			{
    				vecResult.add(EMPTY_STRING);
    			}
    		}                

    		return vecResult;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
        }
      
   /**Returns all peoples assigned as Resource managers to Resource Pools in Resource pool summary table.
    * 
    * @param context Matrix context Object
    * @param args array of String
    * @return StringList of Resource managers
    * @throws Exception if operation fails
     */
    public StringList getAllResourceManagers(Context context,String[] args)throws Exception
    { 
        try
        {
            final String SELECT_RESOURCE_MANAGERS_NAME = "to.name";
            String strRelPattern = RELATIONSHIP_RESOURCE_MANAGER;
            String strVaultPattern = context.getVault().getName();
            String strRelExpression = "";
            short nObjectLimit = 0;
            StringList slSelectStmts = new StringList(); 
            StringList slOrderBy = new StringList();
            String strContextUser = context.getUser();
            slSelectStmts.add(SELECT_RESOURCE_MANAGERS_NAME);
           
            RelationshipWithSelectList relationshipWithSelectList = Relationship.query(context,
                                                                                                                strRelPattern,
                                                                                                                strVaultPattern,
                                                                                                                strRelExpression,
                                                                                                                nObjectLimit,
                                                                                                                slSelectStmts,
                                                                                                                slOrderBy);
            RelationshipWithSelect relationshipWithSelect = null;
            String strResourceManager = null;
            
            StringList slResourceManager = new StringList();
            
            for (RelationshipWithSelectItr relationshipWithSelectItr = new RelationshipWithSelectItr(relationshipWithSelectList);relationshipWithSelectItr.next();)
            {
                relationshipWithSelect = relationshipWithSelectItr.obj();
                strResourceManager = relationshipWithSelect.getSelectData(SELECT_RESOURCE_MANAGERS_NAME);
                //strResourceManager = PersonUtil.getFullName(context, strResourceManager);
                if(!slResourceManager.contains(strResourceManager))
                {
                    slResourceManager.add(strResourceManager);
                }
            }
            if (slResourceManager.contains(strContextUser))
            {
                slResourceManager.remove(strContextUser);
                slResourceManager.insertElementAt(strContextUser,0);
            }
            String allProjects = ProgramCentralUtil.i18nStringNow("emxProgramCentral.Common.All",context.getSession().getLanguage());
            slResourceManager.add(allProjects);
            return slResourceManager;
        } 
        catch (Exception exp) 
        {
            exp.printStackTrace();
            throw exp;
        }
        
        
    }
    
    /**
     * Access method to show Resource Manager filed on Company /Business Unit properties webform
     *
     * @param context
     * @param args 
     * @return boolean value
     * @throws Exception if the operation fails
     * @since V6R2009x
     */
    public boolean isPMCInstalled(Context context, String[] args) throws Exception
    { 
        return FrameworkUtil.isSuiteRegistered(context,"appVersionProgramCentral",false,null,null);
    }
    
  
/**
     * Access method to show/hide Column "Lead Role" in Table PMCResourcePoolPeople
 * 
 * @param context Matrix Context Object
 * @param args
 * @return boolean value
 * @throws Exception if operation fails
     */
   public boolean isResourcePoolPeopleTable(Context context, String[] args) throws Exception
   {
       Map programMap = (Map)JPO.unpackArgs(args);
       String strObjId = (String) programMap.get("objectId");
       String strRelId = (String) programMap.get("relId");
       DomainObject dmoObject = DomainObject.newInstance(context, strObjId);
       return dmoObject.isKindOf(context, TYPE_ORGANIZATION);
      
   }
   
   /**used to avoid allocated peoples in search while adding to Resource Request
    * 
    * @param context Matrix Context Object
    * @param args String array
    * @return StringList for person objects to be excluded
    * @throws Exception
    */
   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   public StringList getExcludeOIDForAddResourcesSearch(Context context, String[] args) throws Exception {  
           try 
       {   /*
       		* Exclude list will contain all the person who are having Non PMC role and who
       		* are already allocated to the list.
       		*/
           Map programMap = (Map)JPO.unpackArgs(args);
           //String strResourcePoolId = (String)programMap.get("objectId");
           
           String strFieldValue = (String)programMap.get("field");
           String strResourcePoolId = (String) FrameworkUtil.split(strFieldValue, "=").lastElement();
           
           String strRequestIdInfo = (String)programMap.get("requestId");
			DomainObject resourcePoolObject = DomainObject.newInstance(context,strResourcePoolId);
			DomainObject requestObject = null;
			if(ProgramCentralUtil.isNotNullString(strRequestIdInfo)){
           StringList slRequestTokens = FrameworkUtil.split(strRequestIdInfo, "|");
           String strRequestId = (String) slRequestTokens.get(0);
				requestObject = DomainObject.newInstance(context,strRequestId);        	   
			}
   
           String strRelationshipPatternAllocated = DomainConstants.RELATIONSHIP_ALLOCATED;
           String strRelationshipPatternMember = DomainConstants.RELATIONSHIP_MEMBER;
           String strTypePattern = DomainConstants.TYPE_PERSON;
   
			String relAssignedSecurityContext = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_AssignedSecurityContext);
			String relSecurityContextRole = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_SecurityContextRole);
           StringList slBusSelect = new StringList();
           slBusSelect.add(DomainObject.SELECT_ID);
			slBusSelect.add(DomainObject.SELECT_NAME);
			slBusSelect.add(DomainObject.SELECT_CURRENT);
			String roleSelectExpression = "from[" + relAssignedSecurityContext + "].to.from[" + relSecurityContextRole + "].to.name";
			slBusSelect.add(roleSelectExpression);
           StringList slRelSelect = new StringList();
           slRelSelect.add(DomainRelationship.SELECT_ID);
   
           boolean getTo = false; 
           boolean getFrom = true; 
           short recurseToLevel = 1;
			//Exclude Inactive users
			String strBusWhere = DomainObject.EMPTY_STRING;
           String strRelWhere = DomainObject.EMPTY_STRING;
           
           // To get All the people in pool
			MapList resourceInfoList = resourcePoolObject.getRelatedObjects(context,
        		   										   strRelationshipPatternMember, //pattern to match relationships
										                   strTypePattern, //pattern to match types
										                   slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
										                   slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
										                   getTo, //get To relationships
										                   getFrom, //get From relationships
										                   recurseToLevel, //the number of levels to expand, 0 equals expand all.
										                   strBusWhere, //where clause to apply to objects, can be empty ""
										                   strRelWhere, //where clause to apply to relationship, can be empty ""
										                   0); 
                   
           StringList slNonPMCPersonIds = new StringList();
			String personId = DomainConstants.EMPTY_STRING;
			String current = DomainConstants.EMPTY_STRING;
			// All the people who are in pool without PMC role put in slNonPMCPersonIds.these will be excluded
			StringList validRoles = new StringList();
			validRoles.add(ProgramCentralConstants.ROLE_PROJECT_LEAD);
			validRoles.add(ProgramCentralConstants.ROLE_PROJECT_USER);
			validRoles.add(ProgramCentralConstants.ROLE_VPLM_PROJECT_LEADER);
			validRoles.add(ProgramCentralConstants.ROLE_VPLM_VIEWER);
			
			for (Iterator itrResourceInfoList = resourceInfoList.iterator(); itrResourceInfoList.hasNext();){
				Map resourceInfo = (Map)itrResourceInfoList.next();
				personId = (String)resourceInfo.get(ProgramCentralConstants.SELECT_ID);
				current = (String)resourceInfo.get(ProgramCentralConstants.SELECT_CURRENT);
           
				//If a Person is Inactive, exclude it.
				if(!ProgramCentralConstants.STATE_PERSON_ACTIVE.equals(current)){
					slNonPMCPersonIds.add(personId);
					continue;
				}
           
				Object objRoles = resourceInfo.get(roleSelectExpression);
				if (objRoles instanceof String) {
					String role = (String) objRoles;
					if(!validRoles.contains(role)){
						slNonPMCPersonIds.add(personId);
					}
				}else if (objRoles instanceof StringList) {
					StringList roles = (StringList) objRoles;
					boolean isValidUser = false;
					for (Iterator itrRoles = roles.iterator(); itrRoles.hasNext();) {
						String role = (String) itrRoles.next();
						if(validRoles.contains(role)){
							isValidUser = true;
							break;
						}
					}
					if(!isValidUser){
						slNonPMCPersonIds.add(personId);
					}
			   }
           }
           
			//Find already-connected resource to Resource request
			if(requestObject == null){
				return slNonPMCPersonIds;
			}
           MapList mlRelatedObjects = requestObject.getRelatedObjects(context,
															                   strRelationshipPatternAllocated, //pattern to match relationships
															                   strTypePattern, //pattern to match types
															                   slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
															                   slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
															                   getTo, //get To relationships
															                   getFrom, //get From relationships
															                   recurseToLevel, //the number of levels to expand, 0 equals expand all.
															                   strBusWhere, //where clause to apply to objects, can be empty ""
															                   strRelWhere, //where clause to apply to relationship, can be empty ""
															                   0); 

           //all those who are already allocated will also be excluded.
			Map mapRelatedObjectInfo = null;
			for (Iterator itrRelatedObjects = mlRelatedObjects.iterator(); itrRelatedObjects.hasNext();){
               mapRelatedObjectInfo = (Map) itrRelatedObjects.next();
				personId = (String)mapRelatedObjectInfo.get(DomainObject.SELECT_ID);
				slNonPMCPersonIds.add(personId);
           }
           return slNonPMCPersonIds;
   }
   catch(Exception exp) 
   {
       exp.printStackTrace();
       throw new MatrixException(exp);
   }
   
   }
   
   /**
    * Gets the data for the column "Business Skill" for table "PMCResourcePoolPeople"
    * 
    * @param context The matrix context object
    * @param args The arguments, it contains objectList and paramList maps
    * @return The Vector object containing Business Skill of selected peoples
    * @throws Exception if operation fails
    */
   public Vector getColumnPeopleSkillData(Context context, String[] args) throws Exception
   {
       try {
           // Create result vector
           Vector vecResult = new Vector();
           final String SELECT_PERSON_BUSINESS_SKILL = "from[" + RELATIONSHIP_HAS_BUSINESS_SKILL + "].to.name";
           // Get object list information from packed arguments
           Map programMap = (Map) JPO.unpackArgs(args);
           MapList objectList = (MapList) programMap.get("objectList");
           Map paramList = (Map) programMap.get("paramList");
           
           Map mapObjectInfo = null;
           
           // Do for each object
           String strPersonSkill = null;
           StringList slPersonSkill = null;
           Object objSkill = null;
           for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();)
           {
               mapObjectInfo = (Map) itrObjects.next();
               objSkill = mapObjectInfo.get(SELECT_PERSON_BUSINESS_SKILL);
               
               if(objSkill == null ||"".equals(objSkill) ||"null".equals(objSkill))
               {
                   vecResult.add(DomainConstants.EMPTY_STRING);
               }
               
               else if (objSkill instanceof String)
               {
                   strPersonSkill  = (String)mapObjectInfo.get(SELECT_PERSON_BUSINESS_SKILL);
                   vecResult.add(strPersonSkill);
               }
               else if (objSkill instanceof StringList)
               {
                   StringList slAllSkills = new StringList();
                   slPersonSkill = (StringList) mapObjectInfo.get(SELECT_PERSON_BUSINESS_SKILL);
                   for (int i = 0; i < slPersonSkill.size(); i++)
                   {
                       strPersonSkill = (String)slPersonSkill.get(i);
                       slAllSkills.add(strPersonSkill);
                   }
                   vecResult.add(FrameworkUtil.join(slAllSkills, ", "));
               }
           }
           return vecResult;
       } 
       catch (Exception exp)
       {
           exp.printStackTrace();
           throw exp;
       }
   }
  /**
   * Assign (connect) selected Peoples from Resource Pool  to given project
   * 
   * @param context Matrix Context object.
   * @param args packed String array which will contain ProjectId,Project Role of Person and stringList of peoples to be assigned .
   * @throws MatrixException if operation fails.
   */
   public void assignPeoplesToProjectSpace(Context context,String[] args) throws MatrixException
   {
       try
       {
           if (context == null)
           {
               throw new MatrixException("Illegal Argument Exception context");
           }
           Map mapPeoplesToConnect = (Map)JPO.unpackArgs(args);
           
           StringList strProjectIds = (StringList) mapPeoplesToConnect.get("ProjectIds");

           String strProjectRole = (String) mapPeoplesToConnect.get("ProjectRole");
           StringList slPeoplesToAssign = (StringList) mapPeoplesToConnect.get("PeoplesToassign");
           
           final String SELECT_PROJECT_MEMBER_IDS ="from["+RELATIONSHIP_MEMBER+"].to.id";
           
           StringList slProjectMemberIds = new StringList();
           
           DomainObject dmoProject;
           String strProjectId = "";
           boolean IfAppropriateState = false;
           StringList slTobeMembers = null;
           for (int i = 0; i < strProjectIds.size(); i++)
           {
        	   strProjectId = (String) strProjectIds.get(i);
        	   slTobeMembers = new StringList();
        	   IfAppropriateState = PolicyUtil.checkState(context,strProjectId,STATE_PROJECT_SPACE_COMPLETE,PolicyUtil.LE);

        	   if (IfAppropriateState)
        	   {
        		   dmoProject = DomainObject.newInstance(context,strProjectId);
        		   slProjectMemberIds= dmoProject.getInfoList(context, SELECT_PROJECT_MEMBER_IDS);
        		   
        		   for(int j=0; j<slPeoplesToAssign.size(); j++)
        		   {
        			   	if(!slProjectMemberIds.contains((String)slPeoplesToAssign.get(j)))
        			   	{
        			   		slTobeMembers.add((String)slPeoplesToAssign.get(j));
        			   	}
        		   }

        		   String[] strTobeMembers = (String[]) slTobeMembers.toArray(new String[slTobeMembers.size()]);
        		   
        		   DomainRelationship.connect(context,dmoProject,RELATIONSHIP_MEMBER,true,strTobeMembers);

        //            commented for not overriding the existing roles for earlier Request
        		   //        	   for (int i = 0; i < strPeoplesToAssign.length; i++)
        		   //        	   {
        		   //        		   String strRelId = (String) mapConnectionInfo.get(strPeoplesToAssign[i]);
        		   //        		   DomainRelationship.setAttributeValue(context, strRelId, ATTRIBUTE_PROJECT_ROLE, strProjectRole);
        		   //        	   }
        	   }
        	   else
        	   {
        		   String strTxtNotice = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
        					"emxProgramCentral.ProjectSpace.Notice.CantAddPersons.NotInAppropriateState", context.getSession().getLanguage());
        		   throw new MatrixException(strTxtNotice);
        	   }
           }

           
       }
       catch(Exception ex)
       {
           ex.printStackTrace();
           throw new MatrixException(ex);
       }
       
   }
   /**
    * Assign (connect) selected Peoples from Resource Pool People to given Resource Request
    * 
    * @param context Matrix Context object.
    * @param args packed String array which will contain ResourceRequestId and Stringlist of peoples to be assigned.
    * @throws MatrixException if operation fails.
    */
    public void assignPeoplesToResourceRequest(Context context,String[] args) throws MatrixException
    {
        try
        {
            if (context == null)
            {
                throw new MatrixException("Illegal Argument Exception context");
            }
            Map mapPeoplesToConnect = (Map)JPO.unpackArgs(args);
            final String SELECT_REL_FROM_ALLOCATED_PERSON_ID = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].businessobject."+DomainConstants.SELECT_ID;
            final String SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE = "to["+DomainConstants.RELATIONSHIP_RESOURCE_PLAN+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
            final String SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE = "from["+DomainConstants.RELATIONSHIP_ALLOCATED+"].attribute["+DomainConstants.ATTRIBUTE_FTE+"].value";
                        
            String strResourceRequestId = (String) mapPeoplesToConnect.get("ResourceRequestId");
            StringList slPeoplesToAssign = (StringList) mapPeoplesToConnect.get("PeoplesToAssign");
            StringList slPeopleToAssignListCheck = new StringList();
            slPeopleToAssignListCheck.addAll(slPeoplesToAssign);
            
            StringList slSelectList = new StringList(); 
            slSelectList.add(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
            slSelectList.add(SELECT_CURRENT);
            slSelectList.add(SELECT_REL_FROM_ALLOCATED_PERSON_ID);
            slSelectList.add(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
            String[] strRequestIds = new String[1];
            strRequestIds[0] = strResourceRequestId;
            String strRequestState = ""; 
            BusinessObjectWithSelectList resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,slSelectList);
            BusinessObjectWithSelect bows = null;
            Map mapPlanFTEValue = new HashMap();
            Map mapAllocatedFTEValue = new HashMap();
            for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
            {
            	bows = itr.obj();
            	String strFTEPlanValue = bows.getSelectData(SELECT_REL_RESOURCE_PLAN_ATTRIBUTE_FTE);
            	strRequestState = bows.getSelectData(SELECT_CURRENT);
                mapPlanFTEValue = getCalculatedMonthFTEMap(strFTEPlanValue, mapPlanFTEValue);
            	StringList slPersonIdList = bows.getSelectDataList(SELECT_REL_FROM_ALLOCATED_PERSON_ID);
            	StringList slFTEAllocatedValueList = bows.getSelectDataList(SELECT_REL_FROM_ALLOCATED_ATTRIBUTE_FTE);
            	if(null!=slPersonIdList && slPersonIdList.size()>0)
            	{
            		for(int i=0; i<slPersonIdList.size(); i++)
            		{
            			String strPerosnId = (String)slPersonIdList.get(i);
	            		if(slPeoplesToAssign.contains(strPerosnId))
	            		{
	            			slPeoplesToAssign.remove(strPerosnId);
	            		}
	            		String strFTEAllocatedValue = (String)slFTEAllocatedValueList.get(i);
	            		mapAllocatedFTEValue = getCalculatedMonthFTEMap(strFTEAllocatedValue, mapAllocatedFTEValue);
	            	}
            	}
            }
            Map mapResultantFTEValue = new HashMap();
            String strFTEValue = EnoviaResourceBundle.getProperty(context,"emxProgramCentral.ResourceRequest.FTE");
            Double nFTEValidateValue = Task.parseToDouble(strFTEValue); 
            for (Iterator iterator = mapPlanFTEValue.keySet().iterator(); iterator.hasNext();) 
            {
				String strTimeLine = (String) iterator.next();
				Double dPlanFTEValue = (Double)mapPlanFTEValue.get(strTimeLine);
				Double dAllocatedFTEValue = 0d;
				if(null!=mapAllocatedFTEValue.get(strTimeLine))
				{
					dAllocatedFTEValue = (Double)mapAllocatedFTEValue.get(strTimeLine);
				}
				Double dResultantFTEValue = dPlanFTEValue - dAllocatedFTEValue;
				Double dPerResultantFTEValue = 0d;
				
				if(dResultantFTEValue > 0)
				{
					dPerResultantFTEValue = dResultantFTEValue/slPeoplesToAssign.size();
					if(dPerResultantFTEValue>nFTEValidateValue)
					{
						dPerResultantFTEValue = nFTEValidateValue;
					}
				}
				else
				{
	               
					dPerResultantFTEValue = nFTEValidateValue;
				}
				NumberFormat numberFormat = ProgramCentralUtil.getNumberFormatInstance(2, true);
				mapResultantFTEValue.put(strTimeLine, numberFormat.format(dPerResultantFTEValue));
			}
            MapList mlResources = new MapList();
            Map mapResourceMap = null; 
            for(int i=0; i<slPeoplesToAssign.size(); i++)
            {
            	mapResourceMap = new HashMap();
            	String slResourceId = (String)slPeoplesToAssign.get(i);
                mapResourceMap.put("Resource_Id",slResourceId);
                MapList mapResultantFTEList = new MapList();
                mapResultantFTEList.add(mapResultantFTEValue);
                mapResourceMap.put("FTE",mapResultantFTEList);
                mapResourceMap.put("ResourceState",strRequestState);
                mapResourceMap.put("RequestId", strResourceRequestId);
                mlResources.add(mapResourceMap);
            }
            String[] strMethodArgs = JPO.packArgs(mlResources);
            emxResourceRequestBase_mxJPO resourceRequestObj = new emxResourceRequestBase_mxJPO(context, strMethodArgs);
    		ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
            resourceRequestObj.addResourcesToRequest(context, strMethodArgs);
            ContextUtil.popContext(context);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new MatrixException(ex);
        }
        
    }
    
    protected static Map getCalculatedMonthFTEMap(String strFTEValue, Map mapAllFTEValue) throws MatrixException
    {
        
        String strMonthYearValue = "";
        Double dFTEValue = null;
        
        if (strFTEValue != null && !"".equals(strFTEValue) && !"null".equals(strFTEValue)) 
        {
            FTE fteRequest = FTE.getInstance(strFTEValue);
            Map mapMonthYearFTEValue = fteRequest.getAllFTE();
            
            if(null!=mapMonthYearFTEValue)
            {
                for (Iterator iter = mapMonthYearFTEValue.keySet().iterator(); iter.hasNext();)
                {
                    strMonthYearValue = (String)iter.next();
                    dFTEValue = new Double(0);
                    if(null!=mapAllFTEValue.get(strMonthYearValue))
                    {
                        dFTEValue = (Double)mapAllFTEValue.get(strMonthYearValue);
                    }
                    dFTEValue = new Double(((Double)mapMonthYearFTEValue.get(strMonthYearValue)).doubleValue()+dFTEValue.doubleValue());
                    mapAllFTEValue.put(strMonthYearValue,dFTEValue);
                }
            }
        }
        return mapAllFTEValue;
    }
    /**
     * @param context
     * @return
     * @throws MatrixException
     */
    public StringList getPMCUser(Context context) throws MatrixException
    {
    	StringList slPMCUsers = new StringList();
    	try
    	{
	    	String strPMCSymbolicRoles = EnoviaResourceBundle.getProperty(context, "eServiceSuiteProgramCentral.Roles");
	        StringList slPMCSymbolicRoles = FrameworkUtil.split(strPMCSymbolicRoles, ",");
	        
	        
	           StringList slAllProjectUserList = null;
	           String sCommandStatement = "print role $1 select $2 $3 dump $4";
	           String strAllProjectUsers =  MqlUtil.mqlCommand(context, sCommandStatement,ProgramCentralConstants.ROLE_PROJECT_USER, "person", "person.inactive", ",");
	           slAllProjectUserList = FrameworkUtil.split(strAllProjectUsers, ",");
				
	           int size = slAllProjectUserList != null ? slAllProjectUserList.size() : 0;
	           int mid = size/2;
	    	   for (int i = 0, k=mid; i < mid && k <size; i++, k++)
	    	   {
	    		   String sUserName = (String)slAllProjectUserList.get(i);
	    		   if(!sUserName.equalsIgnoreCase("User Agent")){
	    			   String sIsInActive = (String)slAllProjectUserList.get(k);
	    			   String personObjId = PersonUtil.getPersonObjectID(context, sUserName);
	    			   if("false".equalsIgnoreCase(sIsInActive) && !slPMCUsers.contains(personObjId))
	    			   {
	    				   slPMCUsers.add(personObjId);
	    			   }
	    		   }
	    	   }
	    	   
	        String strRole = DomainObject.EMPTY_STRING;
	        Role matrixRole = null;
	        UserList assignments = null;
	        String personObjId = DomainObject.EMPTY_STRING;
	        for (StringItr itrRoles = new StringItr(slPMCSymbolicRoles); itrRoles.next();) 
	        {
	            strRole = PropertyUtil.getSchemaProperty(context, itrRoles.obj().trim());
	            
	            matrixRole = new Role(strRole);
	            matrixRole.open(context);
	            
	            StringList projectUsers = new StringList();
	            assignments = matrixRole.getAssignments(context);
	            
	            UserItr userItr = new UserItr(assignments);
	            
	            while(userItr.next())
	            {
	                if (userItr.obj() instanceof matrix.db.Person)
	                {
	                	personObjId = PersonUtil.getPersonObjectID(context, userItr.obj().getName());
	                    if (!slPMCUsers.contains(personObjId)) 
	                    {
	                        slPMCUsers.add(personObjId);
	                    }
	                }
	            }
	            matrixRole.close(context);
	        }
    	}
    	catch(FrameworkException fwe)
    	{
    		throw new MatrixException(fwe);
    	}
    	return slPMCUsers;
    }
    
    
    
    protected MapList getAllRelatedResourcePools (Context context, String strPMCResourcePoolFilter,String strSelectedResourceManager, String strObjectId, String strRelPattern, boolean getTo, boolean getFrom, boolean isExpandFunction) throws MatrixException
    {

        try
        {
            if(context == null)
            {
                throw new MatrixException("Null Context !");
            }
            
            MapList mlResult = new MapList();
            
            final String RESOURCE_REQUEST_STATE_REQUESTED = PropertyUtil.getSchemaProperty(context, "Policy", POLICY_RESOURCE_REQUEST, "state_Requested");
            final String RESOURCE_REQUEST_STATE_PROPOSED = PropertyUtil.getSchemaProperty(context, "Policy", POLICY_RESOURCE_REQUEST, "state_Proposed");
            final String RESOURCE_REQUEST_STATE_REJECTED = PropertyUtil.getSchemaProperty(context, "Policy", POLICY_RESOURCE_REQUEST, "state_Rejected");
            //
            // Depending on the filter selected decide how to find the resource pools
            //
            DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
            String strLoggedInRM = dmoObject.getInfo(context,SELECT_NAME);
            
            StringList slPMCUsers = new StringList();
        
    	    	String strPMCSymbolicRoles = EnoviaResourceBundle.getProperty(context, "eServiceSuiteProgramCentral.Roles");
    	        StringList slPMCSymbolicRoles = FrameworkUtil.split(strPMCSymbolicRoles, ",");
    	        
    	        String strRole = "";
    	        Role matrixRole = null;
    	        UserList assignments = null;
    	        String personObjId = "";
    	        for (StringItr itrRoles = new StringItr(slPMCSymbolicRoles); itrRoles.next();) 
    	        {
    	            strRole = PropertyUtil.getSchemaProperty(context, itrRoles.obj().trim());
    	            
    	            matrixRole = new Role(strRole);
    	            matrixRole.open(context);
    	            
    	            StringList projectUsers = new StringList();
    	            assignments = matrixRole.getAssignments(context);
    	            
    	            UserItr userItr = new UserItr(assignments);
    	            
    	            while(userItr.next()) {
    	                if (userItr.obj() instanceof matrix.db.Person) {
    	                	try {
	    	                	personObjId = PersonUtil.getPersonObjectID(context, userItr.obj().getName());
	    	                    if (!slPMCUsers.contains(personObjId)) {
	    	                        slPMCUsers.add(personObjId);
	    	                    }
    	                	} catch(Exception exception) {
    			          		//PersonUtil.getPersonObjectID() Throws exception when Person Admin
    			          		//object exists and Business object does not.
    	     			   }
    	                }
    	            }
    	            matrixRole.close(context);
    	        }
            
                final String SELECT_ORGANIZATION_MEMBERS_ID = "from["+RELATIONSHIP_MEMBER+"].to.id";
                final String SELECT_RESOURCE_MANAGERS_NAME = "from["+RELATIONSHIP_RESOURCE_MANAGER+"].to.name";
                final String SELECT_RESOURCE_REQUEST_ID = "to["+RELATIONSHIP_RESOURCE_POOL+"].from.id";
                final String SELECT_RESOURCE_REQUEST_STATE = "to["+RELATIONSHIP_RESOURCE_POOL+"].from.current";
                
                String strTypePattern = TYPE_ORGANIZATION;
                
                StringList slBusSelect = new StringList();
                slBusSelect.add(DomainConstants.SELECT_ID);
                slBusSelect.add(SELECT_ORGANIZATION_MEMBERS_ID);//Reqd to calculate Capacity
                slBusSelect.add(SELECT_RESOURCE_REQUEST_ID);//Reqd to calculate Open requests
                slBusSelect.add(SELECT_RESOURCE_REQUEST_STATE);//Reqd to calculate Open requests
                slBusSelect.add(SELECT_RESOURCE_MANAGERS_NAME);//Reqd to find resource managers
                
                StringList slRelSelect = new StringList();
                short recurseToLevel = 0;
                String strBusWhere ="";

               // if ("All".equalsIgnoreCase(strSelectedResourceManager))
              //  {
               //      strBusWhere = "";
              //  }
             //   else
             //   {
            //        strBusWhere = ""+ SELECT_RESOURCE_MANAGERS_NAME +" smatch  \""+ strSelectedResourceManager +"\"";
            //    }
                
                String strRelWhere = "";
                MapList mlContextResourcePools = dmoObject.getRelatedObjects(context,
                                                                                                        strRelPattern, //pattern to match relationships
                                                                                                        strTypePattern, //pattern to match types
                                                                                                        slBusSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Business Obejcts.
                                                                                                        slRelSelect, //the eMatrix StringList object that holds the list of select statement pertaining to Relationships.
                                                                                                        getTo, //get To relationships
                                                                                                        getFrom, //get From relationships
                                                                                                        recurseToLevel, //the number of levels to expand, 0 equals expand all.
                                                                                                        strBusWhere, //where clause to apply to objects, can be empty ""
                                                                                                        strRelWhere); //where clause to apply to relationship, can be empty ""

                
                // Consolidated this data against consistent keys so that it will be easy to process later
                Map mapContextResourcePool = null;
                Map mapConsolidatedInfo = null;
                for (Iterator itrContextResourcePools = mlContextResourcePools.iterator(); itrContextResourcePools.hasNext();) {
                    mapContextResourcePool = (Map) itrContextResourcePools.next();
                    
                    mapConsolidatedInfo = new HashMap();
                    mapConsolidatedInfo.put(DomainConstants.SELECT_ID, mapContextResourcePool.get(DomainConstants.SELECT_ID));
					mapConsolidatedInfo.put("level", mapContextResourcePool.get("level"));
                    mapConsolidatedInfo.put("SELECT_ORGANIZATION_MEMBERS_ID", mapContextResourcePool.get(SELECT_ORGANIZATION_MEMBERS_ID));
                    mapConsolidatedInfo.put("SELECT_RESOURCE_MANAGERS_NAME", mapContextResourcePool.get(SELECT_RESOURCE_MANAGERS_NAME));
                    mapConsolidatedInfo.put("SELECT_RESOURCE_REQUEST_ID", mapContextResourcePool.get(SELECT_RESOURCE_REQUEST_ID));
                    mapConsolidatedInfo.put("SELECT_RESOURCE_REQUEST_STATE", mapContextResourcePool.get(SELECT_RESOURCE_REQUEST_STATE));
                    
                    mlResult.add(mapConsolidatedInfo);
                }
            
          //End Modified:6-Jan-2010:ixe:R209:PRG:IR-023305
            
           
            
            return mlResult;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new MatrixException(ex);
        }

    
    }
    /**
     * This method returns Maplist which are connected to specific company.
     * 
     * @param 	context 	
     * 			The Matrix Context object.
     * @param 	stringArray	
     * 			It contains id of company of which resource managers will be returned.
     * 			
	 * @throws 	MatrixException		
	 * 			MatrixException can be thrown in case of method fail to execute.
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCompanyResourceManagerList(Context context, String[] stringArray) throws MatrixException {
    	
    	MapList resourceManagerMapList = null;
    	String[] resourceManagerSelectableArray = new String[5];
    	resourceManagerSelectableArray[0] = SELECT_ID;
    	resourceManagerSelectableArray[1] = SELECT_NAME;
    	resourceManagerSelectableArray[2] = Person.SELECT_FIRST_NAME;
    	resourceManagerSelectableArray[3] = Person.SELECT_LAST_NAME;
    	resourceManagerSelectableArray[4] = Person.SELECT_EMAIL_ADDRESS;
    	
    	try {
    		Map paramterMap  = JPO.unpackArgs(stringArray);
    		String companyId = (String)paramterMap.get("objectId");
    		
    		emxOrganization_mxJPO orgJPO = new emxOrganization_mxJPO(context,stringArray);
    		orgJPO.setId(companyId);
    		resourceManagerMapList = orgJPO.getResourceManagers(context,resourceManagerSelectableArray);
    		
    	} catch (Exception exp) {
    		throw new MatrixException(exp);
    	}
    	
    	return resourceManagerMapList;
    }
    /**
     * This method connects resource managers from Company.
     * 
     * @param 	context 	
     * 			The Matrix Context object.
     * @param 	stringArray	
     * 			It contains ids of resource managers those should get connected.
     * 			
	 * @throws 	MatrixException		
	 * 			MatrixException can be thrown in case of method fail to execute.
     */
    public void addResourceManagerToCompany(Context context, String[] stringArray) throws MatrixException {

        try {
        	Map paramMap = JPO.unpackArgs(stringArray);
    		String companyId = (String)paramMap.get("objectId");
			List<String> resourceManagerIdList = (List<String>)paramMap.get("resourceManagerIdList");
			
	    	if (resourceManagerIdList.size() > 0) {
	    		
		    	//Get already connected resource managers and remove them from new connection process.
	    		MapList resourceManagerMapList = getCompanyResourceManagerList(context,stringArray);
	    		if (resourceManagerMapList != null && resourceManagerMapList.size()  > 0) {
	    			
	    			for (int i=0;i<resourceManagerMapList.size();i++) {
	    				Map map = (Map)resourceManagerMapList.get(i); 
	    				String existingConnectedId = (String)map.get(SELECT_ID);
	    				resourceManagerIdList.remove(existingConnectedId);
	    			}
	    		}
	    		//Connect new resource managers to company.
	    		if (resourceManagerIdList.size() > 0) {
	    			String[] newResourceManagerIdArray = new String[resourceManagerIdList.size()];
	    			resourceManagerIdList.toArray(newResourceManagerIdArray);
	    			
					ContextUtil.startTransaction(context,true);
		
		            DomainObject domainObject = DomainObject.newInstance (context,companyId);
		            DomainRelationship.connect (context,domainObject,RELATIONSHIP_RESOURCE_MANAGER,true,newResourceManagerIdArray);
		            
		            ContextUtil.commitTransaction(context);
	    		}
            }
        } catch (Exception exp) {
            ContextUtil.abortTransaction(context);
            throw new MatrixException(exp);
        }
    }
    /**
     * This method disconnects resource managers from Company.
     * 
     * @param 	context 	
     * 			The Matrix Context object.
     * @param 	stringArray	
     * 			It contains ids of resource managers those should get disconnected.
     * 			
	 * @throws 	MatrixException		
	 * 			MatrixException can be thrown in case of method fail to execute.
     */
    public void removeResourceManagerFromCompany(Context context, String[] stringArray) throws MatrixException {

        try {
        	boolean getTo = false;
        	boolean getFrom = true;
        	short recurseToLevel = 1;
        	String strBusWhere = EMPTY_STRING;
        	String strRelWhere = EMPTY_STRING;
        	
        	Map paramMap = JPO.unpackArgs(stringArray);
    		String companyId = (String)paramMap.get("objectId");
			List<String> resourceManagerIdList = (List<String>)paramMap.get("resourceManagerIdList");
			
			StringList removeRelIdList = new StringList();
			
			StringList busSelectList   = new StringList();
			busSelectList.add(DomainObject.SELECT_ID);
			
			StringList relSectableList = new StringList();
			relSectableList.add(DomainRelationship.SELECT_ID);
			
			if (ProgramCentralUtil.isNotNullString(companyId) && resourceManagerIdList.size() > 0) {
		            
	            DomainObject companyObject = DomainObject.newInstance(context,companyId);
	            
				MapList resourceManagerDataMapList = 
						companyObject.getRelatedObjects(context,
														RELATIONSHIP_RESOURCE_MANAGER,
														TYPE_PERSON,
														busSelectList,
														relSectableList,
									                    getTo,
									                    getFrom,
									                    recurseToLevel,
									                    strBusWhere,
									                    strRelWhere,0);
	            
				 
				 for (int i=0;i<resourceManagerDataMapList.size();i++) {
					 
					 Map relatedObjectInfoMap = (Map) resourceManagerDataMapList.get(i);

					 String resourceManagerId = (String)relatedObjectInfoMap.get(DomainObject.SELECT_ID);
					 String relationshipId = (String)relatedObjectInfoMap.get(DomainRelationship.SELECT_ID);
	                
					 if (resourceManagerIdList.contains(resourceManagerId)) {
						 removeRelIdList.add(relationshipId);
					 }
		        }
                 
                ContextUtil.startTransaction(context,true);
                
                String[] removeRelIdArray = (String[])removeRelIdList.toArray(new String[removeRelIdList.size()]);
                DomainRelationship.disconnect(context, removeRelIdArray);
                
                ContextUtil.commitTransaction(context);
            }
        } catch (Exception exception) {
            ContextUtil.abortTransaction(context);
            throw new MatrixException(exception);
        }
    }
    /**
     * This Function returns only those person who have the role Resource Manager,
     * VPLMProjectAdministrator, VPLMProjectLeader & are Active,
     * 
     * @param 	context 
     * 			Matrix Context Object.
     * @param 	args 
     * 			holds input packed arguments send by autonomy search framework.
     * @throws 	Exception 
     * 			if the operation fails.
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getExcludeOIDForResourceManagerSearch(Context context,String args[]) throws Exception {
        try 
        {
            Map programMap = (Map) JPO.unpackArgs(args);
            String strOrganizationId = (String) programMap.get("objectId");

            String strSearchAllResourceManager = EnoviaResourceBundle.getProperty(context,"emxComponents.ResourcePool.ResourceManager.SearchInAllOrganization") ;
            boolean isSearchingAllResourceManager =  ("true").equalsIgnoreCase(strSearchAllResourceManager)?true:false;

            final String SELECT_ORGANIZATION_ID = "from.id";
            final String SELECT_MEMBER_NAME = "to.name";
            final String SELECT_MEMBER_ID = "to.id";
            final String SELECT_MEMBER_STATE = "to.current";
            String strRelPattern = DomainConstants.RELATIONSHIP_MEMBER;
            String strRelExpression = EMPTY_STRING;
            String strVaultPattern = "*";

            if(isSearchingAllResourceManager) {
                strRelExpression = EMPTY_STRING;
            }else {
                strRelExpression = " "+ SELECT_ORGANIZATION_ID +" == "+strOrganizationId+" ";
            }

            short nObjectLimit = 0;
            StringList slSelectStmts = new StringList();
            StringList slOrderBy = new StringList();

            slSelectStmts.add(SELECT_ORGANIZATION_ID);
            slSelectStmts.add(SELECT_MEMBER_NAME);
            slSelectStmts.add(SELECT_MEMBER_ID);
            slSelectStmts.add(SELECT_MEMBER_STATE);
            
            RelationshipWithSelectList relationshipWithSelectList = Relationship.query(context,
                                                                                        strRelPattern,
                                                                                        strVaultPattern,
                                                                                        strRelExpression,
                                                                                        nObjectLimit,
                                                                                        slSelectStmts,
                                                                                        slOrderBy);
            RelationshipWithSelect relationshipWithSelect = null;
            String strMemberName = EMPTY_STRING;
            String strMemberId = EMPTY_STRING;
            String strMemberState = EMPTY_STRING;
            	 
            StringList slMemberNames = new StringList();
            StringList slMemberIds = new StringList();
            StringList slMemberStates = new StringList();
            MapList mlMemberList = new MapList();

            for (RelationshipWithSelectItr relationshipWithSelectItr = new RelationshipWithSelectItr(relationshipWithSelectList);relationshipWithSelectItr.next();)
            {
                relationshipWithSelect = relationshipWithSelectItr.obj();
                
                strMemberName = relationshipWithSelect.getSelectData(SELECT_MEMBER_NAME);
                strMemberId = relationshipWithSelect.getSelectData(SELECT_MEMBER_ID);
                strMemberState = relationshipWithSelect.getSelectData(SELECT_MEMBER_STATE);
                HashMap mapMemberInfo = new HashMap();
                mapMemberInfo.put("MemberName",strMemberName);
                mapMemberInfo.put("MemberId",strMemberId);
                mapMemberInfo.put("MemberState",strMemberState);
                
         	   if (!mlMemberList.contains(mapMemberInfo))
         	   {
                mlMemberList.add(mapMemberInfo);
         	   }
                }

            // Existing code
            StringList  slActiveUserIds = new StringList();
            StringList  slActiveUserNames = new StringList();
            int mSize = mlMemberList.size();
            
            for(int i = 0; i < mSize; i++)
            {
                Map mapUser = (Map) mlMemberList.get(i);
                String strUserId = (String) mapUser.get("MemberId");
                String strUserName= (String) mapUser.get("MemberName");
                String strCurrentState = (String) mapUser.get("MemberState");
                if(strCurrentState.equalsIgnoreCase(DomainConstants.STATE_PERSON_ACTIVE))
                {
                    slActiveUserNames.add(strUserName);
                    slActiveUserIds.add(strUserId);
         	   }
            }

            //End all Users
            StringList slFinalUsersWithRole = new StringList();
            String strResourceManager = null;
            String sExtProjectUserRole = null;
            StringList queryResultList = new StringList();
            	
            strResourceManager = PropertyUtil.getSchemaProperty(context,"role_ResourceManager");
            matrix.db.Role matrixRole = new matrix.db.Role(strResourceManager);
            matrixRole.open(context);
                    
            StringList projectUsers = new StringList();
            UserList assignments    = matrixRole.getAssignments(context);
            UserList slActiveRMList = new UserList();
            StringList slRMList = new StringList();
            // get all active Resource Managers
            int aSize = assignments.size();
            for (int i = 0; i < aSize; i++)
            {
                if (slActiveUserNames.contains(((matrix.db.Person)assignments.get(i)).getName())) {
                    slActiveRMList.add (((matrix.db.Person)assignments.get(i)).getName());
                } else if(!slActiveUserNames.contains(((matrix.db.Person)assignments.get(i)).getName())){
                	//TODO Also get the members which do not have "Resource Manager" Role
                    String strPersonId  = PersonUtil.getPersonObjectID(context,((matrix.db.Person)assignments.get(i)).getName());
                    slRMList.add(strPersonId);
                	}
                }
 		   // get all active VPLMProjectAdministrator.
            String strVPMProjectAdmin = PropertyUtil.getSchemaProperty(context,"role_VPLMProjectAdministrator");
            StringList slVPLMProjectAdminUserList = null;
            if(null != strVPMProjectAdmin && !"null".equalsIgnoreCase(strVPMProjectAdmin.trim()) && !"".equalsIgnoreCase(strVPMProjectAdmin.trim())) {
         	   String sCommandStatement = "print role $1 select $2 dump $3";
         	   String strVPLMProjectAdminUsers =  MqlUtil.mqlCommand(context, sCommandStatement,strVPMProjectAdmin, "person",",");
         	   slVPLMProjectAdminUserList = FrameworkUtil.split(strVPLMProjectAdminUsers, ",");
            }
            
           int size = slVPLMProjectAdminUserList != null ? slVPLMProjectAdminUserList.size() : 0;
     	   for (int i = 0; i < size; i++)
     	   {
     		   String sUserName = (String)slVPLMProjectAdminUserList.get(i);
     		   if (slActiveUserNames.contains(sUserName)) {
     			   slActiveRMList.add (sUserName);
     		   }else {
     			   String strPersonId  = PersonUtil.getPersonObjectID(context,(sUserName));
     			   slRMList.add(strPersonId);
     		   }
            }
 			
 		   // get all active VPLM Project Leaders.
 		   String strVPMProjectLeader = PropertyUtil.getSchemaProperty(context,"role_VPLMProjectLeader");
 		   StringList slVPLMProjectLeaderUserList = null;
 		   if(null != strVPMProjectLeader && !"null".equalsIgnoreCase(strVPMProjectLeader.trim()) && !"".equalsIgnoreCase(strVPMProjectLeader.trim())){
 			   String sCommandStatement = "print role $1 select $2 dump $3";
 			   String strVPLMProjectLeadUsers =  MqlUtil.mqlCommand(context, sCommandStatement,strVPMProjectLeader, "person",",");
 			   slVPLMProjectLeaderUserList = FrameworkUtil.split(strVPLMProjectLeadUsers, ",");
     			   }
 		   int listSize = slVPLMProjectLeaderUserList != null ? slVPLMProjectLeaderUserList.size() : 0;
 		   for (int i = 0; i < listSize; i++){
 			   String sUserName = (String)slVPLMProjectLeaderUserList.get(i);
 			   if (slActiveUserNames.contains(sUserName)){
 				   slActiveRMList.add (sUserName);
 			   }else {
 				   String strPersonId  = PersonUtil.getPersonObjectID(context,(sUserName));
 				   slRMList.add(strPersonId);
     		   }
     	   }

            // Exclude OID for Person
            String strPerson = null;
            StringList slExcludePersonList = new StringList();
            int memberListSize = mlMemberList.size();
            for (int i = 0;i < memberListSize; i++)
            {
                Map mapUser = (Map) mlMemberList.get(i);
	            String strUserId = (String) mapUser.get("MemberId");
	            String strUserName= (String) mapUser.get("MemberName");
	
                if (!slActiveRMList.contains(strUserName)) {
                    slExcludePersonList.add(strUserId);
	            }
	        }
            slExcludePersonList.addAll(slRMList);

            // to exclude the person which are removed from company but active or inactive in database
            String strWhere="to["+DomainConstants.RELATIONSHIP_EMPLOYEE+"]==False";

            StringList slTypeSelects = new StringList();
            slTypeSelects.add(DomainConstants.SELECT_ID);
            slTypeSelects.add(DomainConstants.SELECT_CURRENT);

            MapList mlDBActiveUsers = DomainObject.findObjects(context, DomainConstants.TYPE_PERSON, "*", strWhere, slTypeSelects);

            String strPersonId = EMPTY_STRING;
            String strPersonState = EMPTY_STRING;
            for (Iterator iterator = mlDBActiveUsers.iterator(); iterator.hasNext();)
            {
         	   Map mapPerson = (Map) iterator.next();
         	   strPersonId = (String) mapPerson.get(DomainConstants.SELECT_ID);
         	   strPersonState = (String) mapPerson.get(DomainConstants.SELECT_CURRENT);

         	   if(!slExcludePersonList.contains(strPersonId) || !DomainConstants.STATE_PERSON_ACTIVE.equalsIgnoreCase(strPersonState))
         	   {
         		   slExcludePersonList.add(strPersonId);
         	   }
            }

            if(ProgramCentralUtil.isNotNullString(strOrganizationId)){ 
                DomainObject busOrganization = new DomainObject(strOrganizationId);
                StringList personeSelect = new StringList();
                personeSelect.add(SELECT_ID);

                MapList mlResourceManagers = busOrganization.getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_RESOURCE_MANAGER,
                        DomainConstants.TYPE_PERSON,
                        personeSelect,
                        EMPTY_STRINGLIST,
                        false,
                        true,
                        (short) 1,
                        null,
                        null,
                        0);
            	
                for (Iterator iterator = mlResourceManagers.iterator(); iterator.hasNext();)
                {
             	   Map mapPerson = (Map) iterator.next();
             	   strPersonId = (String) mapPerson.get(DomainConstants.SELECT_ID);

             	   if(!slExcludePersonList.contains(strPersonId) || !DomainConstants.STATE_PERSON_ACTIVE.equalsIgnoreCase(strPersonState)){
             		   slExcludePersonList.add(strPersonId);
         	   }
            }
        }

            return slExcludePersonList;
        }
        catch (Exception exp)
        {
            exp.printStackTrace();
            throw exp;
        }

    }
 }

