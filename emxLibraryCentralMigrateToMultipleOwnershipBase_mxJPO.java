import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;

import matrix.db.Context;
import matrix.util.StringList;


public class emxLibraryCentralMigrateToMultipleOwnershipBase_mxJPO extends emxCommonMigration_mxJPO{

	protected String type_Libraries = null;
	protected String type_Classification = null;
	protected String type_GenericDocument = null;
	protected String type_WorkspaceVault = null;

	protected String person_CommonAccessGrantor = null;
	protected String person_WorkspaceAccessGrantor = null;
	
	protected String relationship_SubVaults = null;
	protected String relationship_VaultedDocumentsRev2 = null;
	
    public    StringList USER_PROJECTS = new StringList();
    
    public boolean unconvertable = false;
    public String unConvertableComments = "";

	private void init(Context context) throws FrameworkException{
		type_Libraries           = PropertyUtil.getSchemaProperty(context,"type_Libraries");
		type_Classification      = PropertyUtil.getSchemaProperty(context,"type_Classification");
		type_GenericDocument     = PropertyUtil.getSchemaProperty(context,"type_GenericDocument");
		type_WorkspaceVault      = PropertyUtil.getSchemaProperty(context,"type_ProjectVault");
		
		person_CommonAccessGrantor    = PropertyUtil.getSchemaProperty(context,"person_CommonAccessGrantor");
		person_WorkspaceAccessGrantor = PropertyUtil.getSchemaProperty(context,"person_WorkspaceAccessGrantor");
		
		relationship_SubVaults            = PropertyUtil.getSchemaProperty(context,"relationship_SubVaults");
		relationship_VaultedDocumentsRev2 = PropertyUtil.getSchemaProperty(context,"relationship_VaultedDocumentsRev2");
		
		
		String command = "list role *_PRJ";
        String result = MqlUtil.mqlCommand(context, command, true);
        USER_PROJECTS = StringUtil.split(result, "\n");
	}
	
	public emxLibraryCentralMigrateToMultipleOwnershipBase_mxJPO(
			Context context, String[] args) throws Exception {
		super(context, args);
		init(context);
	}
	
	
	/**
	 * this method filters the Workspace Vault Object to Include only Top Level Folders in Library Central
	 */
	public String writeObjectId(Context context, String[] args) throws Exception{
		
		String type = args[1];
		
		String workspaceVaultType = PropertyUtil.getSchemaProperty(context,"type_ProjectVault");
		
		boolean includeThisObjectId = true;
		if(type.equals(workspaceVaultType)){
			String result = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 dump $4",args[0],"to[Sub Vaults]","to[Data Vaults]","|");
			if(!result.equals("False|False")){
				includeThisObjectId = false;
			}
		}
		
		if(includeThisObjectId){
			return super.writeObjectId(context, args);
		}
		return null;
	}
	
	
	public void migrateObjects(Context context, StringList objectList) throws Exception{
        mqlLogRequiredInformationWriter("In emxLibraryCentralMigrateToMultipleOwnershipBase 'migrateObjects' method "+"\n");
        
        String _kindOfLibraries      = "type.kindof["+type_Libraries+"]";
        String _kindOfClassification = "type.kindof["+type_Classification+"]";
        String _kindOfWorkspaceVault = "type.kindof["+type_WorkspaceVault+"]";
        String _kindOfGenericDocument = "type.kindof["+type_GenericDocument+"]";
        
        StringList objectSelectables = new StringList();
        objectSelectables.add("id");
        objectSelectables.add("type");
        objectSelectables.add("name");
        objectSelectables.add("revision");
        objectSelectables.add(_kindOfLibraries);
        objectSelectables.add(_kindOfClassification);
        objectSelectables.add(_kindOfWorkspaceVault);
        objectSelectables.add(_kindOfGenericDocument);
        objectSelectables.add("grant.granteeaccess");
        
        String[] oidsArray = new String[objectList.size()];
        oidsArray = (String[])objectList.toArray(oidsArray);
        MapList objectInfoList = DomainObject.getInfo(context, oidsArray, objectSelectables);
        mqlLogRequiredInformationWriter("=================================================================================================================================" + "\n");
        
        for(Iterator itr = objectInfoList.iterator();itr.hasNext();){
        	unconvertable = false;
            unConvertableComments = "";
            
        	Map objectInfo = (Map)itr.next();
        	String objectId = (String)objectInfo.get("id");
        	String type = (String)objectInfo.get("type");
        	String name = (String)objectInfo.get("name");
        	
        	mqlLogRequiredInformationWriter("Started Migrating Object Type '" +type + "'  Name '"+ name + "' with Object Id "+objectId+"\n");
            
        	if(objectInfo.get(_kindOfLibraries).equals("TRUE") || objectInfo.get(_kindOfClassification).equals("TRUE") || objectInfo.get(_kindOfGenericDocument).equals("TRUE")){
        		migrateLibraryOrClassOrGenericDocument(context, objectInfo);
        	}else if(objectInfo.get(_kindOfWorkspaceVault).equals("TRUE")){
        		migrateLibraryFolder(context, objectInfo);
        	}else{
        	    mqlLogRequiredInformationWriter("Migration for "+type+" type is not supported... \n" );
                unconvertable = true;
                unConvertableComments += "Migration for "+type+" type is not supported... \n" ;
            }
        	
        	if( unconvertable ){
                writeUnconvertedOID(unConvertableComments,objectId);
            } else {
                loadMigratedOids(objectId);
            }
        	
        	mqlLogRequiredInformationWriter("=================================================================================================================================" + "\n");
        }
    }
	
	protected void migrateLibraryOrClassOrGenericDocument(Context context,Map migratingObj) throws Exception{
		
		mqlLogRequiredInformationWriter("------------------------------------------------------\n");
        
		Map granteeAccessMap = getGranteeAccesses(migratingObj,person_CommonAccessGrantor);
		migrateObject(context, migratingObj, granteeAccessMap);
		
		mqlLogRequiredInformationWriter("------------------------------------------------------\n");
        
	}
	
	/**
	 * This method migrates the all Top Folders , and Folder Content in all Folders
	 * 
	 * !Important : 
	 *      1) All sub Folders are migrated From BPS Migration Utility in 2013x emxSecurityMigrationMigrateTeamObjects
	 *      2) But the Content is not migrated except Part and Documents,
	 *      So this method migrates Top Folder and All content
	 * 
	 * @param context
	 * @param migratingObj
	 * @throws Exception 
	 */
	protected void migrateLibraryFolder(Context context,Map migratingObj) throws Exception{
		try{
			ContextUtil.startTransaction(context, true);
			
			mqlLogRequiredInformationWriter("------------------------------------------------------\n");
            
			Map granteeAccessMap = getGranteeAccesses(migratingObj,person_WorkspaceAccessGrantor);
			// migrate Folder
			migrateObject(context, migratingObj, granteeAccessMap);
			
			// find the content
			StringList objectSelectables = new StringList();
	        objectSelectables.add("id");
	        objectSelectables.add("type");
	        objectSelectables.add("name");
	        objectSelectables.add("revision");
	        objectSelectables.add("to["+relationship_VaultedDocumentsRev2+"].from.id");
	        
			DomainObject folderObj = new DomainObject((String)migratingObj.get("id"));
			MapList folderContents = folderObj.getRelatedObjects(context,
					                                            relationship_SubVaults+","+relationship_VaultedDocumentsRev2,
											                    "*",
											                    objectSelectables,
											                    null,
											                    false,
											                    true,
											                    (short)0, //expand all
											                    "",
											                    "",
																0);
			
			// migrate the content
			for(Iterator folderContentItr = folderContents.iterator();folderContentItr.hasNext();){
				Map folderContent = (Map)folderContentItr.next();
				
				if(!folderContent.get("type").equals(type_WorkspaceVault)){
					String objectId = (String)folderContent.get("id");
					String type = (String)folderContent.get("type");
					Object oParentId = folderContent.get("to["+relationship_VaultedDocumentsRev2+"].from.id");
					StringList parentIds = new StringList();
					
					if(oParentId !=null){
						if(oParentId instanceof String){
							parentIds.add(oParentId);
						}else if(oParentId instanceof StringList){
							parentIds.addAll((StringList)oParentId);
						}
					}
					
					for(Iterator parentIdItr = parentIds.iterator();parentIdItr.hasNext();){
						String parentId = (String)parentIdItr.next();
						if(!hasObjectOwnership(context, objectId, parentId, "")){
							mqlLogRequiredInformationWriter("Modify "+type+" " + objectId + " Add objectOwnership of parent Object " + parentId );
                            DomainAccess.createObjectOwnership(context, objectId, parentId, "");
						}
					}
				}
			}
			
			ContextUtil.commitTransaction(context);
		}catch(Exception e){
			ContextUtil.abortTransaction(context);
		}finally{
			mqlLogRequiredInformationWriter("------------------------------------------------------\n");
        }
		
	}
	
	private void migrateObject(Context context,Map migratingObj,Map granteeAccessMap) throws Exception{
		
		String objectId   = (String)migratingObj.get("id");
		String type       = (String)migratingObj.get("type");
		
		Set grantees = granteeAccessMap.keySet();
		
		for(Iterator<String> granteeItr = grantees.iterator();granteeItr.hasNext();){
			String grantee = granteeItr.next();
			StringList accessList = (StringList)granteeAccessMap.get(grantee);
			
			accessList = removeDuplicates(accessList);
			
			if( USER_PROJECTS.contains(grantee+"_PRJ"))
            {
                if(!DomainAccess.hasObjectOwnership(context, objectId, null, grantee + "_PRJ", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP))
                {
                    mqlLogRequiredInformationWriter("Modify "+type+" " + objectId + " Add objectOwnership for user " + grantee +" with access as "+ accessList+ "\n");
                    DomainAccess.createObjectOwnership(context, objectId, null, grantee + "_PRJ", StringUtil.join(accessList, ","), DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                }
            } else {
                mqlLogRequiredInformationWriter("Modify "+type+" " + objectId + " Add objectOwnership for user '" + grantee +"' is not supported due to two reason 1. The member is a Role or Group 2. The member is not Migrated to be added....\n" );
                unconvertable = true;
                unConvertableComments += "Modify "+type+" " + objectId + " Add objectOwnership for user '" + grantee +"' is not supported due to two reason 1. The member is a Role or Group 2. The member is not Migrated to be added.... \n" ;
            }
		}
	}
	
	private Map getGranteeAccesses(Map migratingObj, String accessGrantor){
		HashMap accessMap = new HashMap<String, StringList>();
		
		Set keySet = migratingObj.keySet();
		for(Iterator<String> keyItr = keySet.iterator();keyItr.hasNext();){
			String key = keyItr.next();
			if(key.indexOf("grant[") == 0){
				int startBindex = key.indexOf("[", key.indexOf("grant"));
                int closeBindex = key.indexOf("]", key.indexOf("grant"));
                String gratorGrantee = key.substring(startBindex+1, closeBindex);
                if( gratorGrantee.indexOf(",") > 0)
                {
                    String grantee = gratorGrantee.substring(gratorGrantee.indexOf(",")+1, gratorGrantee.length());
                    String grantor = gratorGrantee.substring(0,gratorGrantee.indexOf(","));
                    if(!grantor.equals(accessGrantor)){
                    	continue;
                    }
                    String strAccesses = (String)migratingObj.get(key);
                    StringList accesses = FrameworkUtil.split(strAccesses,",");
                    while(accesses.contains("grant")){
                    	accesses.remove("grant");
                    }
                    while(accesses.contains("revoke")){
                    	accesses.remove("revoke");
                    	accesses.add("changeowner");
                    }
                    
                    if(accessMap.containsKey(grantee)){
                    	StringList existingAccesses = (StringList)accessMap.get(grantee);
                    	existingAccesses.addAll(accesses);
                    	accessMap.put(grantee, existingAccesses);
                    }else{
                    	accessMap.put(grantee, accesses);
                    }
                }
			}
		}
		return accessMap;
	}

	private static StringList removeDuplicates(StringList list)
    {
        HashSet<String> hashSet = new HashSet<String>();
        for(int i = 0; i < list.size(); i++)
        {
            hashSet.add((String) list.get(i));
        }
        list.clear();
        list.addAll(hashSet);
        return list;
    }
	
	private static boolean hasObjectOwnership(Context context, String objectId,  String parentId, String comment) throws FrameworkException {
        comment = (comment == null) ? DomainConstants.EMPTY_STRING : comment;
        String result = MqlUtil.mqlCommand(context, 
                                           "print bus $1 select $2 dump",objectId,"ownership[" + parentId + "|" + comment + "]");
        if("true".equalsIgnoreCase(result) )
        {
            return true;
        } else {
            return false;
        }
    }

}
