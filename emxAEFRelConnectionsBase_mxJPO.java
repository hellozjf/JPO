/**
 *emxAEFRelConnectionsBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import matrix.db.*;
import matrix.util.*;
import java.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;


public class emxAEFRelConnectionsBase_mxJPO extends emxDomainObject_mxJPO {
	/**
	 * Constructor
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since Common 10.6
	 * @grade 0
	 */
	public emxAEFRelConnectionsBase_mxJPO (Context context, String[] args)
	throws Exception {
		super(context, args);
	}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @returns nothing
	 * @throws Exception if the operation fails
	 * @since Common 10.6
	 * @grade 0
	 */
	public int mxMain(Context context, String[] args) throws Exception {
		if (!context.isConnected())
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            Map paramList = (Map)paramMap.get("paramList");
            String languageStr = (String)paramList.get("languageStr");
            String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.DesktopClient", new Locale(languageStr));
            throw new Exception(exMsg);
        }
		return 0;
	}

	/**
	 * This method provides the generic code for update of relationship based columns. The column that is updated could be displaying 
	 * any object connected to object shown in the Structure browser row.
	 * eg. BOM power view can have column showing the Design Responsibility connected to the part in the row in one of the column.
	 * This column can be updated using this JPO.
	 * This method needs to be used along with 'Update Program Arguments' with value like 'relationship=<symbolic_name_of_relationship>,isFrom=true/false'
	 * 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains the 'Update Program Arguments'
	 * @returns true/false
	 * @throws Exception if the operation fails
	 * @since Common 10.6
	 * @grade 0
	 */    
	public boolean updateConnectedObject(Context context, String[] args) throws Exception {
		boolean isConnected=false;
		try
		{
			// un-pack the arguments 
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			// get the papamMap
			HashMap paramMap = (HashMap)programMap.get("paramMap");
			// get the columnMap    	 
			HashMap columnMap = (HashMap)programMap.get("columnMap");
			// get the settingsMap
			HashMap settingsMap = (HashMap)columnMap.get("settings");
			// get the Update Program Arguments  
		
			String strUpdateProgramArguments=(String)settingsMap.get("Update Program Arguments");
			if(strUpdateProgramArguments!=null && !strUpdateProgramArguments.equals("") && !strUpdateProgramArguments.equals("null") && strUpdateProgramArguments.length()!=0)
			{
				// Parse the string to get the required values
				String relationshipName=strUpdateProgramArguments.substring(strUpdateProgramArguments.indexOf("relationship=")+ "relationship=".length(), strUpdateProgramArguments.indexOf(","));
				relationshipName = PropertyUtil.getSchemaProperty(context,relationshipName); 

				int i = strUpdateProgramArguments.indexOf("isFrom=")+ "isFrom=".length();
				String strFrom=strUpdateProgramArguments.substring(i, i+"true".length());

				// get the existing connection id
				String relId  = "";
				// get the object id
				String sObjectId  = (String)paramMap.get("objectId");
				// get the new object id for connecting
				String newObjectId = (String)paramMap.get("New Value");

				DomainObject contextObject = new DomainObject(sObjectId);

				StringList slSelectList = new StringList();

				StringList selectList= new StringList();
				// check the direction of connection and connect accordingly 
				if(strFrom.equals("true"))
				{
					relId = contextObject.getInfo(context,"to[" + relationshipName + "].id");
					isConnected=connectedObject(context,newObjectId,relationshipName,sObjectId,relId,true) ;
				}
				else
				{
					relId = contextObject.getInfo(context,"from[" + relationshipName + "].id");
					isConnected=connectedObject(context,newObjectId,relationshipName,sObjectId,relId,false) ;
				}

			}
			else 
			{
				String error = "No Update Program Arguments setting on column.Please enter values as: relationship=<symbolic name of relationship>,isFrom=<true/false>";
				MqlUtil.mqlCommand(context,"notice $1",error);

			}

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return isConnected;
	}

	private boolean connectedObject(Context context,String newObjectId,String relationshipName,String sObjectId,String relId,boolean isFrom) throws Exception {
		
		if(relId==null ||relId.equals("") || relId.equals("null"))
		{
			if( newObjectId==null ||newObjectId.equals(" ") || newObjectId.equals("null"))
			{
				return false;
			}
			else
			{
				DomainRelationship.connect(context,newObjectId,relationshipName,sObjectId,true);  
				return  true;
			}
		}
		else
		{
			DomainObject newObj = new DomainObject(newObjectId);
			if(newObjectId.equals(" ") || newObjectId.equals("null") || newObjectId==null)
			{
				DomainRelationship.disconnect(context,relId);
				return  true;
			}  
			else
			{
				ContextUtil.startTransaction(context,true);
				if(isFrom){
					DomainRelationship.setFromObject(context, relId, newObj);
				}
				else{
					DomainRelationship.setToObject(context, relId, newObj);
				}
				ContextUtil.commitTransaction(context);
				return true;
			}
		}
	}
}
