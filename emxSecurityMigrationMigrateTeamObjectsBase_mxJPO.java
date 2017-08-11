import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import matrix.db.Access;
import matrix.db.Context;
import matrix.util.IntList;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.framework.ui.UIUtil;

public class emxSecurityMigrationMigrateTeamObjectsBase_mxJPO extends emxCommonMigration_mxJPO
{

    /**
     *
     */
    private static final long serialVersionUID = -5029177381386073045L;
    static private  Map<String, Integer> _accessMasksConstMapping = new HashMap<String, Integer>(35);
    static {
        _accessMasksConstMapping.put("read", Access.cRead);
        _accessMasksConstMapping.put("modify", Access.cModify);
        _accessMasksConstMapping.put("delete", Access.cDelete);
        _accessMasksConstMapping.put("checkout", Access.cCheckout);
        _accessMasksConstMapping.put("checkin", Access.cCheckin);
        _accessMasksConstMapping.put("lock", Access.cLock);
        _accessMasksConstMapping.put("unlock", Access.cUnLock);
        _accessMasksConstMapping.put("grant", Access.cGrant);
        _accessMasksConstMapping.put("revoke", Access.cRevoke);
        _accessMasksConstMapping.put("changeowner", Access.cChangeOwner);
        _accessMasksConstMapping.put("create", Access.cCreate);
        _accessMasksConstMapping.put("promote", Access.cPromote);
        _accessMasksConstMapping.put("demote", Access.cDemote);
        _accessMasksConstMapping.put("enable", Access.cEnable);
        _accessMasksConstMapping.put("disable", Access.cDisable);
        _accessMasksConstMapping.put("override", Access.cOverride);
        _accessMasksConstMapping.put("schedule", Access.cSchedule);
        _accessMasksConstMapping.put("revise", Access.cRevise);
        _accessMasksConstMapping.put("changevault", Access.cChangeLattice);
        _accessMasksConstMapping.put("changename", Access.cChangeName);
        _accessMasksConstMapping.put("changepolicy", Access.cChangePolicy);
        _accessMasksConstMapping.put("changetype", Access.cChangeType);
        _accessMasksConstMapping.put("fromconnect", Access.cFromConnect);
        _accessMasksConstMapping.put("toconnect", Access.cToConnect);
        _accessMasksConstMapping.put("fromdisconnect", Access.cFromDisconnect);
        _accessMasksConstMapping.put("todisconnect", Access.cToDisconnect);
        _accessMasksConstMapping.put("freeze", Access.cFreeze);
        _accessMasksConstMapping.put("thaw", Access.cThaw);
        _accessMasksConstMapping.put("execute", Access.cExecute);
        _accessMasksConstMapping.put("modifyform", Access.cModifyForm);
        _accessMasksConstMapping.put("viewform", Access.cViewForm);
        _accessMasksConstMapping.put("show", Access.cShow);
        _accessMasksConstMapping.put("majorrevise", Access.cMajorRevise);
        _accessMasksConstMapping.put("all", Access.cAll);
        _accessMasksConstMapping.put("none", Access.cNone);
    }

    public emxSecurityMigrationMigrateTeamObjectsBase_mxJPO(Context context, String[] args) throws Exception {
      super(context, args);
    }

    public static StringList mxObjectSelects = new StringList(15);
    public static String parentWorkspaceId = "";
    public static String parentFolderId = "";
    public static String projectUserId = "";
    public static String projectUserName="";
    public static String contentId = "";
    public static String parentWorkspaceGrantee = "";
    public static String parentFolderGrantee = "";
    public static String parentWorkspaceGrantGranteeAccess = "";
    public static String parentFolderGrantGranteeAccess = "";
    public static String contentParentId = "";
    public static String contentGrantGranteeAccess = "";
    public static String relationshipDataVaults = "";
    public static String relationshipSubVaults = "";
    public static String RELATIONSHIP_PROJECT_MEMBERS = "";
    public static String RELATIONSHIP_PROJECT_MEMBERSHIP = "";
    public static String RELATIONSHIP_VAULTED_OBJECTS = "";
    public static String TYPE_WORKSPACE = "";
    public static String TYPE_WORKSPACE_VAULT = "";
    public static String relationship_WorkspaceMember = "";
    public static String relationship_WorkspaceTemplateMember = "";
    public static String attribute_ProjectAccess ="";
    public static String templateMemberName = "";
    public static String templateMemberId = "";
    public static String templateMemberRole = "";
    public static String TYPE_WORKSPACE_TEMPLATE = "";
    public static String workspaceMemnerIds = "";
    public static StringList USER_PROJECTS = new StringList();
    public static boolean unconvertable = false;
    public static String unConvertableComments = "";
    public static String contentIdRev2 = "";
    public static String contentParentIdRev2 = "";

    public static void init(Context context) throws FrameworkException
    {
        relationshipDataVaults = PropertyUtil.getSchemaProperty(context, "relationship_ProjectVaults");
        relationshipSubVaults = PropertyUtil.getSchemaProperty(context, "relationship_SubVaults");
        RELATIONSHIP_PROJECT_MEMBERS = PropertyUtil.getSchemaProperty(context, "relationship_ProjectMembers");
        RELATIONSHIP_PROJECT_MEMBERSHIP = PropertyUtil.getSchemaProperty(context, "relationship_ProjectMembership");
        RELATIONSHIP_VAULTED_OBJECTS = PropertyUtil.getSchemaProperty(context, "relationship_VaultedDocuments");
        TYPE_WORKSPACE = PropertyUtil.getSchemaProperty(context, "type_Project");
        TYPE_WORKSPACE_VAULT =  PropertyUtil.getSchemaProperty(context, "type_ProjectVault");
        TYPE_WORKSPACE_TEMPLATE =  PropertyUtil.getSchemaProperty(context, "type_WorkspaceTemplate");
        relationship_WorkspaceMember = PropertyUtil.getSchemaProperty(context, "relationship_WorkspaceMember");
        relationship_WorkspaceTemplateMember = PropertyUtil.getSchemaProperty(context, "relationship_WorkspaceTemplateMember");
        attribute_ProjectAccess = PropertyUtil.getSchemaProperty(context, "attribute_ProjectAccess");
        parentWorkspaceId = "to[" + relationshipDataVaults + "].from.id";
        parentFolderId = "to[" + relationshipSubVaults + "].from.id";
        projectUserId = "from[" + RELATIONSHIP_PROJECT_MEMBERS + "].to.to[" + RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.id";
        projectUserName = "from[" + RELATIONSHIP_PROJECT_MEMBERS + "].to.to[" + RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.name";
        contentId = "from[" + RELATIONSHIP_VAULTED_OBJECTS + "].to.id";
        parentWorkspaceGrantee = "to[" + relationshipDataVaults + "].from.grantee";
        parentFolderGrantee = "to[" + relationshipSubVaults + "].from.grantee";
        parentWorkspaceGrantGranteeAccess = "to[" + relationshipDataVaults + "].from.grant.granteeaccess";
        parentFolderGrantGranteeAccess = "to[" + relationshipSubVaults + "].from.grant.granteeaccess";
        contentParentId = "to[" + RELATIONSHIP_VAULTED_OBJECTS + "].from.id";
        contentGrantGranteeAccess = "from[" + RELATIONSHIP_VAULTED_OBJECTS + "].to.grant.granteeaccess";
        templateMemberRole = "from["+ relationship_WorkspaceTemplateMember +"].attribute["+ attribute_ProjectAccess +"]";
        templateMemberName = "from["+ relationship_WorkspaceTemplateMember +"].to.name";
        templateMemberId = "from["+ relationship_WorkspaceTemplateMember +"].to.id";
        workspaceMemnerIds = "from[Workspace Member].to.id";
        contentIdRev2 = "from[" + RELATIONSHIP_VAULTED_OBJECTS_REV2 + "].to.id";
        contentParentIdRev2 = "to[" + RELATIONSHIP_VAULTED_OBJECTS_REV2 + "].from.id";

        DomainObject.MULTI_VALUE_LIST.add(projectUserId);
        DomainObject.MULTI_VALUE_LIST.add(projectUserName);
        DomainObject.MULTI_VALUE_LIST.add(contentId);
        DomainObject.MULTI_VALUE_LIST.add(parentWorkspaceGrantGranteeAccess);
        DomainObject.MULTI_VALUE_LIST.add(contentGrantGranteeAccess);
        DomainObject.MULTI_VALUE_LIST.add(parentFolderGrantGranteeAccess);
        DomainObject.MULTI_VALUE_LIST.add(contentParentId);
        DomainObject.MULTI_VALUE_LIST.add(parentWorkspaceGrantee);
        DomainObject.MULTI_VALUE_LIST.add(parentFolderGrantee);
        DomainObject.MULTI_VALUE_LIST.add(templateMemberRole);
        DomainObject.MULTI_VALUE_LIST.add(templateMemberName);
        DomainObject.MULTI_VALUE_LIST.add(templateMemberId);
        DomainObject.MULTI_VALUE_LIST.add(workspaceMemnerIds);
        DomainObject.MULTI_VALUE_LIST.add(contentIdRev2);
        DomainObject.MULTI_VALUE_LIST.add(contentParentIdRev2);
        String command = "list role *_PRJ";
        String result = MqlUtil.mqlCommand(context, command, true);
        USER_PROJECTS = StringUtil.split(result, "\n");
    }

    public void migrateObjects(Context context, StringList objectList) throws Exception
    {
        mqlLogRequiredInformationWriter("In emxSecurityMigrationMigrateTeamObjects 'migrateObjects' method "+"\n");
        init(context);
        mxObjectSelects.addElement(parentWorkspaceId);
        mxObjectSelects.addElement(parentFolderId);
        mxObjectSelects.addElement(parentFolderId);
        mxObjectSelects.addElement(projectUserId);
        mxObjectSelects.addElement(contentId);
        mxObjectSelects.addElement(parentWorkspaceGrantee);
        mxObjectSelects.addElement(parentFolderGrantee);
        mxObjectSelects.addElement("id");
        mxObjectSelects.addElement("type");
        mxObjectSelects.addElement("name");
        mxObjectSelects.addElement("policy");
        mxObjectSelects.addElement("grantee");
        mxObjectSelects.addElement("grant.granteeaccess");
        mxObjectSelects.addElement("owner");
        mxObjectSelects.addElement(DomainConstants.SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);
        mxObjectSelects.addElement(contentParentId);
        mxObjectSelects.addElement(parentWorkspaceGrantGranteeAccess);
        mxObjectSelects.addElement(parentFolderGrantGranteeAccess);
        mxObjectSelects.addElement(templateMemberRole);
        mxObjectSelects.addElement(templateMemberName);
        mxObjectSelects.addElement(templateMemberId);
        mxObjectSelects.addElement(workspaceMemnerIds);
        mxObjectSelects.addElement(contentIdRev2);
        mxObjectSelects.addElement(contentParentIdRev2);
        String[] oidsArray = new String[objectList.size()];
        oidsArray = (String[])objectList.toArray(oidsArray);
        MapList mapList = DomainObject.getInfo(context, oidsArray, mxObjectSelects);
        Iterator<?> itr = mapList.iterator();
        mqlLogRequiredInformationWriter("=================================================================================================================================" + "\n");
        while(itr.hasNext())
        {
            unconvertable = false;
            unConvertableComments = "";
            Map<?, ?> m = (Map<?, ?>)itr.next();
            String type = (String)m.get("type");
            String oid = (String)m.get("id");
            mqlLogRequiredInformationWriter("Started Migrating Object Type '" +type + "'  Name '"+ (String)m.get("name") + "' with Object Id "+(String)m.get("id"));
            if(type.equals(TYPE_WORKSPACE) )
            {
              migrateWorkspace(context, m);
            } else if( type.equals(TYPE_WORKSPACE_VAULT)) {
              migrateWorkspaceFolder(context, m);
            } else if (type.equals(TYPE_WORKSPACE_TEMPLATE) ) {
              migrateWorkspaceTemplate(context, m);
            } else {
              mqlLogRequiredInformationWriter("Un-Excepected type " + type + " found during team central migration .... " +"\n");
              unconvertable = true;
              unConvertableComments += "Un-Excepected type " + type + " found during team central migration ....  \n";
            }
            if( unconvertable )
            {
                writeUnconvertedOID(unConvertableComments, oid);
            } else {
                loadMigratedOids(oid);
            }
            mqlLogWriter("-------------------------------------------" + "\n");
            mqlLogWriter(m.toString() +"\n");
            mqlLogWriter("#################################################################################################################################" + "\n");
        }
    }

    private void migrateWorkspaceTemplate(Context context, Map<?, ?> m) throws Exception
    {
        String po = "Project Owner";
        String pl = "Project Lead";
        String templateId = (String)m.get("id");

        StringList templateMemberIds = (StringList)m.get(templateMemberId);
        StringList templateMemberNames = (StringList)m.get(templateMemberName);
        StringList templateMemberRoles = (StringList)m.get(templateMemberRole);
        if( (templateMemberIds.size() == templateMemberNames.size()) && (templateMemberIds.size() == templateMemberRoles.size()) )
        {
            for(int i=0; i<templateMemberIds.size(); i++)
            {
                String memberName = (String)templateMemberNames.get(i);
                String memberRole = (String)templateMemberRoles.get(i);
                String access = "Read";
                if(po.equals(memberRole) || pl.equals(memberRole))
                {
                  access = "Add Remove";
                }

                if( USER_PROJECTS.contains(memberName+"_PRJ"))
                {
                    mqlLogRequiredInformationWriter("Adding Multiple Ownership for Member '" + memberName + "' with access "+ access +"  .... " );
                    if(!DomainAccess.hasObjectOwnership(context, templateId, null, memberName+"_PRJ", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP))
                    {
                        DomainAccess.createObjectOwnership(context, templateId, null, memberName+"_PRJ" , access, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                    }
                } else {
                    mqlLogRequiredInformationWriter("Adding Multiple Ownership for Member '"+ memberName + "' is not supported due to two reason 1. The member is a Role or Group 2. The member is not Migrated to be added...." );
                    unconvertable = true;
                    unConvertableComments += "Adding Multiple Ownership for Member '"+ memberName + "' is not supported due to two reason 1. The member is a Role or Group 2. The member is not Migrated to be added.... \n" ;
                }
            }
        }
    }

    private void migrateWorkspaceFolder(Context context, Map<?, ?> m) throws Exception
    {
    	String parentId = (String) m.get(parentWorkspaceId);
    	if( parentId == null || "".equals(parentId))
    	{
    		parentId =  (String) m.get(parentFolderId);
    	}
    	if(UIUtil.isNullOrEmpty(parentId)){
    		mqlLogRequiredInformationWriter("Folder '"+ (String)m.get("name") + "' is not migrated because it is not having any parent....");
    		unconvertable = true;
    		unConvertableComments += "Folder '"+ (String)m.get("name") + "' is not migrated because it is not having any parent.... \n";
    		return;
    	}
    	Map<String, Collection> objectAccessMap = (Map<String, Collection>)((Map<String, Map>)getTotalAccessMap(context, m)).get("OBJECTACCESSMAP");
    	Map<String, Collection> parentAccessMap = (Map<String, Collection>)((Map<String, Map>)getTotalAccessMap(context, m)).get("PARENTACCESSMAP");
    	StringList grantees = (StringList)m.get("grantee");
    	grantees = removeDuplicates(grantees);
    	
        mqlLogRequiredInformationWriter("Started Migrating Object Access for Grantees = "+ grantees);
        String contentParentId = (String)m.get("id");
        StringList contentIds = (StringList)m.get(contentId);
        StringList contentIdsRev2 = (StringList)m.get(contentIdRev2);
        StringList contentList = new StringList();
        if(null != contentIds )
        {
          contentList.addAll(contentIds);
        }

        if(null != contentIdsRev2 )
        {
          contentList.addAll(contentIdsRev2);
        }

        migrateObjectAccess(context, objectAccessMap, parentAccessMap, grantees, m, parentId);
        mqlLogRequiredInformationWriter("SuccessFully Migrated Workspace Folder = "+ contentParentId);
        mqlLogRequiredInformationWriter("Started Migrating content Objects = "+ contentList);
        if( contentList != null && contentList.size() > 0 )
        {
            String[] oidsArray = new String[contentList.size()];
            oidsArray = (String[])contentList.toArray(oidsArray);
            MapList mapList = DomainObject.getInfo(context, oidsArray, mxObjectSelects);
            mqlLogWriter("Content MapList = "+ mapList);
            Iterator<?> itr = mapList.iterator();
            while(itr.hasNext())
            {
                Map cMap = (Map)itr.next();
                
                // Read the property key "emxFramework.FolderContentTypesThatRequireGrants"
            	// and migrate only those Folder contents which are listed as part of this propery key in emxSystem.properties
            	String contentTypes = EnoviaResourceBundle.getProperty(context, "emxFramework.FolderContentTypesThatRequireGrants");
            	String symbolicType = FrameworkUtil.getAliasForAdmin(context, "type", (String)cMap.get("type"), true);
                if(!contentTypes.contains(symbolicType) ){
                	mqlLogRequiredInformationWriter("Object of type '"+ (String)cMap.get("type") + "' is not migrated because it does not require any grant....");
            		String unConvertableFolderContentComments = "Object of type '"+ (String)cMap.get("type") + "' is not migrated because it does not require any grant.... \n";
            		writeUnconvertedOID(unConvertableFolderContentComments, (String)cMap.get("id"));
            		continue;
                }
                
                Map<String, Collection> contetntAccessMap = (Map<String, Collection>)((Map<String, Map>)getTotalAccessMap(context, cMap)).get("OBJECTACCESSMAP");
                StringList contetnGrantees = (StringList)cMap.get("grantee");
                contetnGrantees = removeDuplicates(contetnGrantees);
                mqlLogRequiredInformationWriter("Started Migrating Object "+ (String)cMap.get("type") + "  " + (String)cMap.get("name") + "  " + (String)cMap.get("revision") +" Access for Grantees = "+ contetnGrantees);
                migrateObjectAccess(context, contetntAccessMap, objectAccessMap, contetnGrantees, cMap, contentParentId);
                
                //The below method modifyPolicyForDocumentObject will be called 
                //only for the Document object which had been created with policy Document                
                String sPolicy = (String)cMap.get("policy");
                if("Document".equals(sPolicy)){
                	modifyPolicyForDocumentObject(context, cMap);
                }
                
            }
        }
    }

    private void migrateObjectAccess(Context context,  Map<String, Collection> objectAccessMap, Map<String, Collection> parentAccessMap, StringList grantees, Map<?, ?> m, String parentId) throws Exception
    {
        if( grantees != null )
        {
            Iterator gItr = grantees.iterator();
            boolean parentAccessAssigned = false;
            String objectId = (String)m.get("id");
            String objectName = (String)m.get("name");
            
            
            // In 13x, default value of below attribute is None; but in 2015x its default value is Read.
            // So while migrating we have to set the value of those objects which are created on 13x stack
        	String defaultUserAccess = (String)m.get(SELECT_ATTRIBUTE_DEFAULT_USER_ACCESS);        	
        	if("None".equals(defaultUserAccess)){
        		mqlLogRequiredInformationWriter("Modify value of Attribute Default User Access for folder " + objectId);
        		defaultUserAccess = "Read";
        		DomainObject folderObj = DomainObject.newInstance(context, objectId);       		
        		folderObj.setAttributeValue(context, ATTRIBUTE_DEFAULT_USER_ACCESS, defaultUserAccess);
        	}
        	
        	
            while(gItr.hasNext())
            {
                String grantee = (String)gItr.next();
                if( grantee!= null && !"".equals(grantee.trim()) )
                {
                    mqlLogRequiredInformationWriter("grantee == "+ grantee);
                    HashSet accessSet = (HashSet)objectAccessMap.get(grantee);
                    mqlLogRequiredInformationWriter("accessSet == "+ objectAccessMap.get(grantee));
                    StringList accessList = new StringList();
                    if( accessSet != null )
                      accessList.addAll(accessSet);
                    long objectAccessLong = getAccessFlag(accessList);
                    HashSet parentAccessSet = (HashSet)parentAccessMap.get(grantee);
                    StringList parentAccessList = new StringList();
                    if( parentAccessSet != null )
                      parentAccessList.addAll(parentAccessSet);
                    long parentAccessLong = getAccessFlag(parentAccessList);
                    if(parentAccessLong ==  objectAccessLong)
                    {
                        if(!parentAccessAssigned && !hasObjectOwnership(context, objectId, parentId, ""))
                        {
                            mqlLogRequiredInformationWriter("------------------------------------------------------");
                            mqlLogRequiredInformationWriter("Folder Access for " + objectName + "is same as Parent " + parentId + " Access");
                            mqlLogRequiredInformationWriter("Modify Folder " + objectId + " Add objectOwnership of parent object " + parentId);
                            mqlLogRequiredInformationWriter("------------------------------------------------------");
                            DomainAccess.createObjectOwnership(context, objectId, parentId, "");
                            parentAccessAssigned = true;
                        }
                    } else {
                        mqlLogRequiredInformationWriter("------------------------------------------------------");
                        mqlLogRequiredInformationWriter("Folder Access for " + objectName + "is NOT same as Parent " + parentId + " Access");
                        if(!parentAccessAssigned && !hasObjectOwnership(context, objectId, parentId, ""))
                        {
                            mqlLogRequiredInformationWriter("Modify Folder " + objectId + " Add objectOwnership of parent Object " + parentId );
                            DomainAccess.createObjectOwnership(context, objectId, parentId, "");
                            parentAccessAssigned = true;
                        }
                        if( USER_PROJECTS.contains(grantee+"_PRJ"))
                        {
                            if(!DomainAccess.hasObjectOwnership(context, objectId, null, grantee + "_PRJ", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP))
                            {
                                mqlLogRequiredInformationWriter("Modify Folder " + objectId + " Add objectOwnership for user " + grantee +" with access as "+ accessList);
                                DomainAccess.createObjectOwnership(context, (String)m.get("id"), null, grantee + "_PRJ", StringUtil.join(accessList, ","), DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                            }
                        } else {
                            mqlLogRequiredInformationWriter("Modify Folder " + objectId + " Add objectOwnership for user '" + grantee +"' is not supported due to two reason 1. The member is a Role or Group 2. The member is not Migrated to be added...." );
                            unconvertable = true;
                            unConvertableComments += "Modify Folder " + objectId + " Add objectOwnership for user '" + grantee +"' is not supported due to two reason 1. The member is a Role or Group 2. The member is not Migrated to be added.... \n" ;
                        }
                        mqlLogRequiredInformationWriter("------------------------------------------------------");
                    }
                }
            }
        }
    }

    public static boolean hasObjectOwnership(Context context, String objectId,  String parentId, String comment) throws FrameworkException {
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

    public void mqlLogRequiredInformationWriter(String command) throws Exception
    {
        super.mqlLogRequiredInformationWriter(command +"\n");
    }

    public void mqlLogWriter(String command) throws Exception
    {
        super.mqlLogWriter(command +"\n");
    }

    private Map<String, Map> getTotalAccessMap(Context context, Map<?, ?> m) throws Exception
    {
        Set keySet = m.keySet();
        Iterator keyItr = keySet.iterator();
        Map<String, Map> totalAccessMap = new HashMap<String, Map>();
        Map<String, Collection> objectAccessMap = new HashMap<String, Collection>();
        Map<String, Collection> parentAccessMap = new HashMap<String, Collection>();
        totalAccessMap.put("OBJECTACCESSMAP", objectAccessMap);
        totalAccessMap.put("PARENTACCESSMAP", parentAccessMap);
        //Collection accessBits = new HashSet(56);
        while(keyItr.hasNext())
        {
            String key = (String)keyItr.next();
            if( key.indexOf("grant[") >= 0)
            {
                int startBindex = key.indexOf("[", key.indexOf("grant"));
                int closeBindex = key.indexOf("]", key.indexOf("grant"));
                String gratorGrantee = key.substring(startBindex+1, closeBindex);
                if( gratorGrantee.indexOf(",") > 0)
                {
                    String grantee = gratorGrantee.substring(gratorGrantee.indexOf(",")+1, gratorGrantee.length());
                    String keyValue = (String)m.get(key);
                    keyValue = keyValue.replace("majorrevise,", "");
                    if(keyValue.contains("grant") )
                    {
                        keyValue = keyValue.replace("grant,", "");
                        keyValue = keyValue.replace("revoke,", "changeowner,");
                    }
                    Map<String, Collection> localAccessMap = new HashMap<String, Collection>();
                    if( key.startsWith("grant[") )
                    {
                        localAccessMap = totalAccessMap.get("OBJECTACCESSMAP");
                    } else {
                        localAccessMap = totalAccessMap.get("PARENTACCESSMAP");
                    }
                    StringList accessList = StringUtil.split(keyValue, ",");
                    if(localAccessMap.containsKey(grantee))
                    {
                        Collection accessBits = (Collection)localAccessMap.get(grantee);
                        accessBits.addAll(accessList);
                    } else {
                        Collection accessBits = new HashSet(56);
                        accessBits.addAll(accessList);
                        localAccessMap.put(grantee, accessBits);
                    }
                }
            }
        }
        return totalAccessMap;
    }

    private void migrateWorkspace(Context context, Map<?, ?> m) throws Exception
    {
        Map<String, Collection> objectAccessMap = (Map<String, Collection>)((Map<String, Map>)getTotalAccessMap(context, m)).get("OBJECTACCESSMAP");
        StringList projectMemberIds = (StringList)m.get(projectUserId);
        if( projectMemberIds != null )
        {
            Iterator memberIdItr = projectMemberIds.iterator();
            StringList grantees = (StringList)m.get("grantee");
            grantees = removeDuplicates(grantees);
            Iterator gItr = grantees.iterator();
            while(gItr.hasNext())
            {
                String grantee = (String)gItr.next();
                HashSet accessSet = (HashSet)objectAccessMap.get(grantee);
                StringList accessList = new StringList();
                accessList.addAll(accessSet);
                if( USER_PROJECTS.contains(grantee+"_PRJ"))
                {
                    mqlLogRequiredInformationWriter("------------------------------------------------------");
                    mqlLogRequiredInformationWriter("Modify Workspace " + m.get("name") + " for user "+ grantee +" with access as "+ StringUtil.join(accessList, ",") + " with comment as " + DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                    mqlLogRequiredInformationWriter("------------------------------------------------------");
                    if(!DomainAccess.hasObjectOwnership(context, (String)m.get("id"), null, grantee+"_PRJ", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP))
                    {
                        DomainAccess.createObjectOwnership(context, (String)m.get("id"), null, grantee + "_PRJ", StringUtil.join(accessList, ","), DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                    }
                } else {
                    mqlLogRequiredInformationWriter("Modify Workspace " +  (String)m.get("id") + " adding Multiple Ownership for user '" + grantee +"' is not supported due to two reason 1. The member is a Role or Group 2. The member is not Migrated to be added...." );
                    unconvertable = true;
                    unConvertableComments += "Modify Workspace " +  (String)m.get("id") + " adding Multiple Ownership for user '" + grantee +"' is not supported due to two reason 1. The member is a Role or Group 2. The member is not Migrated to be added.... \n" ;
                }
            }
            StringList addedUsers = (StringList)m.get(workspaceMemnerIds);
            while(memberIdItr.hasNext())
            {
                String memberId = (String)memberIdItr.next();
                if( addedUsers == null || !addedUsers.contains(memberId))
                {
                    DomainRelationship.connect(context, (String)m.get("id"), "Workspace Member", memberId, true);
                    mqlLogRequiredInformationWriter("Connect Workspace " + m.get("name") + " with user id "+ memberId +" as Workspace Member");
                }
            }
        } else {
            mqlLogRequiredInformationWriter("The Workspace '"+ m.get("name") +"' doesn't have any members connected to migrate to new security Model");
            unconvertable = true;
            unConvertableComments += "The Workspace '"+ m.get("name") +"' doesn't have any members connected to migrate to new security Model \n" ;

        }
    }

    static protected Long getAccessFlag(StringList masks) throws Exception
    {
        IntList intList = new IntList(masks.size());
        Access access = new Access();
        for (int i=0; i < masks.size(); i++) {
            String mask = ((String) masks.get(i)).trim().toLowerCase();
            Object maskValue = _accessMasksConstMapping.get(mask);
            if (maskValue != null) {
                int cMask = (Integer) maskValue;
                intList.addElement(cMask);
            }
        }
        access.processIntList(intList);
        return Long.valueOf(access.getLongAccessFlag());
    }

    private Map<String, Collection> getObjectAccessMap(Context context, Map<?, ?> m)
    {
        Set keySet = m.keySet();
        Iterator keyItr = keySet.iterator();
        Map<String, Collection> accessMap = new HashMap<String, Collection>();
        //Collection accessBits = new HashSet(56);
        while(keyItr.hasNext())
        {
            String key = (String)keyItr.next();
            if( key.startsWith("grant["))
            {
                int startBindex = key.indexOf("[");
                int closeBindex = key.indexOf("]");
                String gratorGrantee = key.substring(startBindex+1, closeBindex);
                if( gratorGrantee.indexOf(",") > 0)
                {
                    String grantee = gratorGrantee.substring(gratorGrantee.indexOf(",")+1, gratorGrantee.length());
                    String keyValue = (String)m.get(key);
                    keyValue = keyValue.replace("majorrevise,", "");
                    if(keyValue.contains("grant") )
                    {
                        keyValue = keyValue.replace("grant,", "");
                        keyValue = keyValue.replace("revoke,", "changeowner,");
                    }
                    StringList accessList = StringUtil.split(keyValue, ",");
                    if(accessMap.containsKey(grantee))
                    {
                        Collection accessBits = (Collection)accessMap.get(grantee);
                        accessBits.addAll(accessList);
                    } else {
                        Collection accessBits = new HashSet(56);
                        accessBits.addAll(accessList);
                        accessMap.put(grantee, accessBits);
                    }
                }
            }
        }
        return accessMap;
    }

    /**
     * This method is used to modify the policy of Document objects
     * In migration we are modifying the policy to Document Release if it is Document
     * @param context
     * @param m
     * @throws Exception
     */
    private void modifyPolicyForDocumentObject(Context context, Map<?, ?> m) throws Exception
    {        
    	String objectId = (String)m.get(SELECT_ID);
    	mqlLogRequiredInformationWriter("------------------------------------------------------");
    	mqlLogRequiredInformationWriter("Modify policy for Document object "+objectId);
    	String mqlCommand = "modify bus $1 policy $2";
    	MqlUtil.mqlCommand(context, mqlCommand, objectId, POLICY_DOCUMENT);
    	mqlCommand = "promote bus $1";
    	MqlUtil.mqlCommand(context, mqlCommand, objectId);
    	mqlLogRequiredInformationWriter("------------------------------------------------------");
    }

    public static void removeTeamCentralGrants(Context context, String[] args) throws Exception
    {
        String cmd = "modify policy 'Workspace' state 'Create' remove user 'Workspace Lead Grantor' all ";
        MqlUtil.mqlCommand(context, cmd, true);
        cmd = "modify policy 'Workspace' state 'Create' remove user 'Access Grantor' all ";
        MqlUtil.mqlCommand(context, cmd, true);

        cmd = "modify policy 'Workspace' state 'Active' remove user 'Workspace Lead Grantor' all ";
        MqlUtil.mqlCommand(context, cmd, true);
        cmd = "modify policy 'Workspace' state 'Active' remove user 'Access Grantor' all ";
        MqlUtil.mqlCommand(context, cmd, true);

        cmd = "modify policy 'Workspace' state 'Archive' remove user 'Workspace Lead Grantor' all ";
        MqlUtil.mqlCommand(context, cmd, true);

        cmd = "modify policy 'Workspace Vaults' state 'Exists' remove user 'Workspace Lead Grantor' all ";
        MqlUtil.mqlCommand(context, cmd, true);
        cmd = "modify policy 'Workspace Vaults' state 'Exists' remove user 'Access Grantor' all ";
        MqlUtil.mqlCommand(context, cmd, true);
    }
}
