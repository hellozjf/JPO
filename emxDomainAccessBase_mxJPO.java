/*
**  emxDomainAccess
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program.
**
*/

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;


/**

 */
public class emxDomainAccessBase_mxJPO {


    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation
     * @since V6R2011x
     * @grade 0
     */
    public emxDomainAccessBase_mxJPO(Context context, String[] args)
      throws Exception
    {
        //super(context, args);
    }

    @SuppressWarnings("deprecation")
    public void assignSecurityContext(Context context, String[] args) throws Exception
    {
        boolean transaction = false;
        try
        {
            // Verify that enough parameter are available to avoid array index out of bound exceptions.
            if( args.length >= 6 )
            {
                String relType = args[0];
                String relMember = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_Member);
                // Verify that relationship type is Member if not this trigger doesn't need to be run.
                if( relMember.equals(relType) )
                {
                    String typePerson = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_Person);
                    String toType = args[1];
                    // Verify that to type of the object is Person if not this trigger doesn't need to be run.
                    if( typePerson.equals(toType) )
                    {
                        String companyId = args[2];
                        String typeOrganization = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_Organization);
                        String sResult = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump",companyId,"type.kindof["+  typeOrganization +"]");
                        // Verify that from type of the object is kind of Organization if not this trigger doesn't need to be run.
                        if ("TRUE".equalsIgnoreCase(sResult))
                        {
                            ContextUtil.startTransaction(context, true);
                            transaction = true;
                            String currentRole = context.getRole();
                            String currentApplication = context.getApplication();
                            try
                            {
                                ContextUtil.pushContext(context);
                                if( DomainAccess.init(context) )
                                {
                                    context.resetRole(DomainAccess.VPLMADMIN_COMPANYNAME_DEFAULT_SC);
                                    context.setApplication("VPLM");
                                }
                                String personId = args[3];
                                String attrValue = args[4];
                                String oldAttrValue = args[5];
                                StringList oldRoles = new StringList(1);
                                StringList newRoles = new StringList(1);
                                if( attrValue != null )
                                newRoles = StringUtil.split(attrValue, "~");
                                if( oldAttrValue != null )
                                oldRoles = StringUtil.split(oldAttrValue, "~");

                                StringList addedRoles = new StringList(newRoles.size());
                                StringList removedRoles = new StringList(oldRoles);
                                Iterator<?> itr = newRoles.iterator();
                                while(itr.hasNext())
                                {
                                    String addedRole = (String)itr.next();
                                    if( oldRoles.contains(addedRole) )
                                    {
                                        removedRoles.remove(addedRole);
                                    } else if(DomainAccess.BusinessRoles.contains(PropertyUtil.getSchemaProperty(context, addedRole))) {
                                        addedRoles.addElement(addedRole);
                                    }
                                }
                                DomainObject company = DomainObject.newInstance(context, companyId);
                                StringList selects = new StringList(2);
                                selects.addElement(DomainObject.SELECT_NAME);
                                selects.addElement(DomainObject.SELECT_TYPE);
                                selects.addElement(DomainObject.SELECT_REVISION);
                                selects.addElement(DomainObject.SELECT_VAULT);
                                Map<?, ?> companyMap =  company.getInfo(context, selects);
                                String companyName =  (String)companyMap.get(DomainObject.SELECT_NAME);
                                String companyType =  (String)companyMap.get(DomainObject.SELECT_TYPE);
                                String orgRev =  (String)companyMap.get(DomainObject.SELECT_REVISION);
                                //String orgVault =  (String)companyMap.get(DomainObject.SELECT_VAULT);
                                DomainObject person = DomainObject.newInstance(context, personId);
                                String personName = person.getInfo(context, DomainObject.SELECT_NAME);
                                boolean needOperationalOrg = DomainAccess.needOperationalOrg(context, personId, companyId);
                                String scCompanyName = companyName;
                                if( needOperationalOrg )
                                {
                                    //scCompanyName = companyName + "_OPERATIONAL";
                                    System.out.println("Warning:: Person " + personName + " is getting added to " + scCompanyName);
                                }
                                String relationshipAssignedSecurityContext = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_AssignedSecurityContext);
                                Iterator<?> addItr = addedRoles.iterator();
                                while(addItr.hasNext())
                                {
                                    String roleName = (String)addItr.next();
                                    String scId = DomainAccess.createSecurityContext(context, DomainAccess.getDefaultProject(context), roleName, scCompanyName, companyType, orgRev);
                                    DomainObject scObj = DomainObject.newInstance(context, scId);
                                    String scName = scObj.getInfo(context, DomainObject.SELECT_NAME);
                                    DomainRelationship.connect(context, personId, relationshipAssignedSecurityContext, scId, true);
                                    String assignedRoles = MqlUtil.mqlCommand(context, "print person $1 select assignment dump", true, personName);
                                    if( assignedRoles.indexOf(scName) < 0)
                                    {
                                        MqlUtil.mqlCommand(context, "mod person $1 assign role $2", true, personName, "ctx::"+ scName);
                                    }
                                }
                                Iterator<?> rmItr = removedRoles.iterator();
                                while(rmItr.hasNext())
                                {
                                    //mod person u1 remove assign role ctx::role_ExchangeUser.BU1.default;
                                    String roleName = (String)rmItr.next();
                                    String scId = DomainAccess.createSecurityContext(context, DomainAccess.getDefaultProject(context), roleName, scCompanyName, companyType, orgRev);
                                    DomainObject scObj = DomainObject.newInstance(context, scId);
                                    String scName = scObj.getInfo(context, DomainObject.SELECT_NAME);
                                    String personSCcmd = "print bus $1 select $2 dump";
                                    String select = "from[" + relationshipAssignedSecurityContext + "|to.name== '" + scName + "'].id";
                                    String relId = MqlUtil.mqlCommand(context, personSCcmd, person.getObjectId(), select);
                                    if( relId != null && !"".equals(relId))
                                    {
                                        DomainRelationship.disconnect(context, relId, true);
                                    }
                                    String assignedRoles = MqlUtil.mqlCommand(context, "print person $1 select assignment dump", true, personName);
                                    if( assignedRoles.indexOf(scName) >= 0)
                                    {
                                        MqlUtil.mqlCommand(context, "mod person $1 remove assign role $2", true, personName, "ctx::"+ scName);
                                    }
                                }
                                if( currentRole != null && !"".equals(currentRole) )
                                {
                                    context.resetRole(currentRole);
                                }
                                if( currentApplication != null && !"".equals(currentApplication) )
                                {
                                    context.setApplication(currentApplication);
                                }
                            }
                            catch(Exception ex )
                            {
                                ex.printStackTrace();
                                throw ex;
                            }
                            finally
                            {
                                ContextUtil.popContext(context);
                            }
                            ContextUtil.commitTransaction(context);
                        }
                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            if( transaction )
                ContextUtil.abortTransaction(context);
            throw new Exception(ex);
        }
    }

    public void removePrimaryOwnership(Context context, String[] args) throws Exception
    {
        try
        {
            String busId = args[0];
            DomainObject obj = DomainObject.newInstance(context, busId);
            String project = obj.getInfo(context, DomainAccess.SELECT_PROJECT);
            if(DomainAccess.getDefaultProject(context).equals(project))
            {
                obj.removePrimaryOwnership(context);
            }
        } catch(Exception ex)
        {
            throw ex;
        }
    }
    public void assignSecurityContextCheck(Context context, String[] args) throws Exception
    {
        PropertyUtil.setGlobalRPEValue(context, "OLDATTRVALUE", args[0]);
    }

    @SuppressWarnings("deprecation")
    public void assignPersonalSecurityContext(Context context, String[] args) throws Exception
    {
        try
        {
            String companyId = args[0];
            String personId = args[1];
            //DebugUtil.debug("companyId = " + companyId);
            //DebugUtil.debug("personId  = " + personId);
            DomainObject person = DomainObject.newInstance(context, personId);
            String personName = person.getInfo(context, DomainObject.SELECT_NAME);
            DomainObject org = DomainObject.newInstance(context, companyId);
            String orgName = org.getInfo(context, DomainObject.SELECT_NAME);
            String SCName = "Grant."+ orgName +"."+ personName + "_PRJ";
            String result=MqlUtil.mqlCommand(context, "list role $1",person.getName()+"_PRJ");
            if(UIUtil.isNullOrEmpty(result))
            {
            MqlUtil.mqlCommand(context, "add role $1 asaproject parent $2 hidden", true, personName + "_PRJ","User Projects" );
            }
            result=MqlUtil.mqlCommand(context,"list role $1",SCName);
            if(UIUtil.isNullOrEmpty(result)){
            MqlUtil.mqlCommand(context, "add role $1 parent $2,$3,$4 assign person $5 hidden", true,
                               SCName,"Grant",orgName, personName + "_PRJ",personName);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }
    public void deletePersonalSecurityContext(Context context, String[] args) throws Exception
    {
        try {
            String personName = args[0];
            String personalSecurityContexts = MqlUtil.mqlCommand(context, "print person $1 select $2 $3 dump", personName, "assignment[Grant*].name", "assignment[Grant*].project" );

            StringList securityContextList = StringUtil.split(personalSecurityContexts, ",");
            String personalSecurityContext = (String) securityContextList.get(0);
            String personRole = (String) securityContextList.get(1);

            String strPersonalWorkspaceType = PropertyUtil.getSchemaProperty(context, "type_PersonalWorkspace");
            MqlUtil.mqlCommand(context, "delete role $1", true, personalSecurityContext);
            
            String strCommand = "temp query bus $1 $2 $3  where $4 select $5 dump $6";
            String sResult = MqlUtil.mqlCommand(context, strCommand, true, strPersonalWorkspaceType, "*", "*","project=='"+personRole+"'", "id","|");
            
            StringList resultList = StringUtil.split(sResult, "\n");
            if(resultList.size()>0 && resultList.size() == 1){
                    StringList objectList = StringUtil.split((String)resultList.get(0), "|");
                    MqlUtil.mqlCommand(context, "delete bus $1", true, (String)objectList.get(3));
            }
            
            MqlUtil.mqlCommand(context, "delete role $1", true, personRole);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
    /************ APIs related to trigger manager ***************/

    public boolean createObjectOwnershipInheritance(Context context, String[] args) throws Exception  {
        String fromId = args[0];
        String toId = args[1];
        String relId = args[2];
        String relType = args[3];
        String fromType = args[4];
        String toType = args[5];
        return createObjectOwnershipInheritance(context, fromId, toId, relId, relType, fromType, toType, null, false);
    }

    public boolean deleteObjectOwnershipInheritance(Context context, String[] args) throws Exception  {
        String fromId = args[0];
        String toId = args[1];
        String relId = args[2];
        String relType = args[3];
        String fromType = args[4];
        String toType = args[5];
        return deleteObjectOwnershipInheritance(context, fromId, toId, relId, relType, fromType, toType, null, false);

    }
    public boolean createObjectOwnershipInheritanceByForce(Context context, String[] args) throws Exception  {
        String fromId = args[0];
        String toId = args[1];
        String relId = args[2];
        String relType = args[3];
        String fromType = args[4];
        String toType = args[5];
        return createObjectOwnershipInheritance(context, fromId, toId, relId, relType, fromType, toType, null, true);
    }

    public boolean deleteObjectOwnershipInheritanceByForce(Context context, String[] args) throws Exception  {
        String fromId = args[0];
        String toId = args[1];
        String relId = args[2];
        String relType = args[3];
        String fromType = args[4];
        String toType = args[5];
        if ("1".equals(PropertyUtil.getGlobalRPEValue(context, "RPE_DELETE_FOLDER"))) {
			return true;
		} else {
        	return deleteObjectOwnershipInheritance(context, fromId, toId, relId, relType, fromType, toType, null, true);
		}
    }

    public boolean createObjectOwnershipInheritance(Context context, String fromId, String toId, String relId, String relType, String fromType, String toType, String comment, boolean runAsUserAgent) throws Exception  {
        String attrAccessTypeSelect = DomainObject.getAttributeSelect(PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_attribute_AccessType));
        DomainObject obj = DomainObject.newInstance(context, toId);
        String attrAccessType = obj.getInfo(context, attrAccessTypeSelect);
        boolean needGrants = true;
        boolean inheritSpecificFolderForTemplateFolder = false;

        if( DomainObject.RELATIONSHIP_VAULTED_OBJECTS.equals(relType))
        {
            needGrants = false;
            DomainObject fromObject = DomainObject.newInstance(context, fromId);
            if( fromType == null || "".equals(fromType) )
            {
              fromType = fromObject.getInfo(context, DomainObject.SELECT_TYPE);
            }
            if(fromType.equals(DomainObject.TYPE_PROJECT_VAULT) )
            {
                String attribute_FOLDER_CLASSIFICATION = PropertyUtil.getSchemaProperty(context, "attribute_FolderClassification");
                String selectFolderClassification = "attribute["+ attribute_FOLDER_CLASSIFICATION +"]";
                String folderClassification = fromObject.getInfo(context, selectFolderClassification);
                if("Shared".equals(folderClassification) ) {
                    String contentTypes = "";
                    try
                    {
                        contentTypes = EnoviaResourceBundle.getProperty(context, "emxFramework.FolderContentTypesThatRequireGrants");
                    } catch(Exception ex) {
                        needGrants = true;
                    }
                    if( !needGrants )
                    {
                        if( toType == null || "".equals(toType) )
                        {
                            DomainObject toObject = DomainObject.newInstance(context, toId);
                            toType = toObject.getInfo(context, DomainObject.SELECT_TYPE);
                        }
                        String symbolicType = FrameworkUtil.getAliasForAdmin(context, "type", toType, true);
                        StringList contentTypesList = FrameworkUtil.split(contentTypes, ",");
                        if(contentTypesList.indexOf(symbolicType) > -1)
                        {
                            needGrants = true;
                        }
                    }
                }
            }
        }

        if((attrAccessType != null) && "Specific".equals(attrAccessType))
        {
            String strInterfaceName = PropertyUtil.getSchemaProperty(context,"interface_TemplateFolder");
            String sCommandPrintStatement = "print bus $1 select $2 dump";
            String sIsInterFacePresent = MqlUtil.mqlCommand(context, sCommandPrintStatement,fromId,"interface[" + strInterfaceName + "]");
            // If there is an Project Template interface on the parent we want to inherit even Specific Folders
            if("true".equalsIgnoreCase(sIsInterFacePresent)){
                inheritSpecificFolderForTemplateFolder = true;
            }

            //  Add the user creating the folder to SOV with Full access for Specific Folders
    		String personName = context.getUser()+"_PRJ";
            String result=MqlUtil.mqlCommand(context, "list role $1",personName);
            if(!UIUtil.isNullOrEmpty(result))
            {
				//DomainAccess.createObjectOwnership(context, toId, "", personName, "Full", comment);
                DomainAccess.createObjectOwnership(context, toId, PersonUtil.getPersonObjectID(context), "Full", comment, true, false);
            }
        }

        if( needGrants && (attrAccessType == null || "".equals(attrAccessType) || !"Specific".equals(attrAccessType) || inheritSpecificFolderForTemplateFolder))
        {
          DomainAccess.createObjectOwnership(context, toId, fromId, comment, runAsUserAgent);
        }

        return true;
    }

    public boolean deleteObjectOwnershipInheritance(Context context, String fromId, String toId, String relId, String relType, String fromType, String toType, String comment, boolean runAsUserAgent) throws Exception  {
        String attrAccessTypeSelect = DomainObject.getAttributeSelect(PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_attribute_AccessType));
        DomainObject obj = DomainObject.newInstance(context, toId);
        String attrAccessType = obj.getInfo(context, attrAccessTypeSelect);
        if( attrAccessType == null || "".equals(attrAccessType) || !"Specific".equals(attrAccessType))
        {
          DomainAccess.deleteObjectOwnership(context, toId, fromId, comment, runAsUserAgent);
        }
        return true;
    }

public boolean createObjectOwnershipInheritanceForReferenceDocuments(Context context, String[] args) throws Exception  {
        String fromId = args[0];
        String toId = args[1];
        String relId = args[2];
        String relType = args[3];
        String fromType = args[4];
        String toType = args[5];

        String typeDoc = PropertyUtil.getSchemaProperty("type_DOCUMENTS");

        DomainObject documentObj = new DomainObject();
        documentObj.setId(toId);

        StringList busSel =  new StringList("type.kindof["+ typeDoc + "]");
        busSel.add("current.access[changeowner]");
        Map busMap = documentObj.getInfo(context, busSel);
        if ("true".equalsIgnoreCase((String)busMap.get("type.kindof["+ typeDoc + "]")) && "true".equalsIgnoreCase((String)busMap.get("current.access[changeowner]"))) {
             return createObjectOwnershipInheritance(context, fromId, toId, relId, relType, fromType, toType, null, false);
        }else{
             return true;
        }
    }
public boolean createObjectOwnershipInheritanceForRoutes(Context context, String[] args) throws Exception  {
    String fromId = args[0];
    String toId = args[1];
    String relId = args[2];
    String relType = args[3];
    String fromType = args[4];
    String toType = args[5];

    String typeDoc = PropertyUtil.getSchemaProperty("type_Route");

    DomainObject documentObj = new DomainObject();
    documentObj.setId(toId);

    StringList busSel =  new StringList("type.kindof["+ typeDoc + "]");
    busSel.add("current.access[changeowner]");
    Map busMap = documentObj.getInfo(context, busSel);
    if ("true".equalsIgnoreCase((String)busMap.get("type.kindof["+ typeDoc + "]")) && "true".equalsIgnoreCase((String)busMap.get("current.access[changeowner]"))) {
         return createObjectOwnershipInheritance(context, fromId, toId, relId, relType, fromType, toType, null, false);
    }else{
         return true;
    }
}

public boolean createObjectOwnershipInheritanceForIssues(Context context, String[] args) throws Exception  {

    String fromId = args[0];
    String toId = args[1];
    String relId = args[2];
    String relType = args[3];
    String fromType = args[4];
    String toType = args[5];

    String typeDoc = PropertyUtil.getSchemaProperty("type_Issue");

    DomainObject documentObj = new DomainObject();
    documentObj.setId(toId);

    StringList busSel =  new StringList("type.kindof["+ typeDoc + "]");
    busSel.add("current.access[changeowner]");
    Map busMap = documentObj.getInfo(context, busSel);
    if ("true".equalsIgnoreCase((String)busMap.get("type.kindof["+ typeDoc + "]")) && "true".equalsIgnoreCase((String)busMap.get("current.access[changeowner]"))) {
         return createObjectOwnershipInheritance(context, fromId, toId, relId, relType, fromType, toType, null, false);
    }else{
         return true;
    }
}

public void createObjectOwnershipInheritanceOnRevise(Context context, String[] args) throws Exception  {

	 String oldObjectId = args[0];
	 String typeDoc = PropertyUtil.getSchemaProperty(context, "type_Document");
	 String documentPolicy=PropertyUtil.getSchemaProperty(context, "policy_Document");
	 StringList busSel =  new StringList("type.kindof["+ typeDoc + "]");
	 busSel.add("next.current.access[changeowner]");
	 busSel.add("next.id");
	 busSel.add("next.owner");
	 busSel.add(DomainConstants.SELECT_POLICY);
	 DomainObject documentObj = new DomainObject();
	 documentObj.setId(oldObjectId);
	 Map busMap = documentObj.getInfo(context, busSel);
	 String changeowner=(String)busMap.get("next.current.access[changeowner]");
    String objectId = (String) busMap.get("next.id");
    String owner = (String) busMap.get("next.owner");
    String kindOf=(String) busMap.get("type.kindof["+ typeDoc + "]");
    String policy=(String) busMap.get(DomainConstants.SELECT_POLICY);
    if(("true".equalsIgnoreCase(kindOf) && "true".equalsIgnoreCase(changeowner)) && policy.equalsIgnoreCase(documentPolicy))
    	{
   	 String personId=PersonUtil.getPersonObjectID(context,owner);
   	 StringList accessNames = DomainAccess.getLogicalNames(context, objectId);
   	 String ownerAccess = (String)accessNames.get(accessNames.size()-1);
   	 String user=context.getUser();
   	 DomainAccess.createObjectOwnership(context, objectId, personId, ownerAccess, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP,true);
    	}
}

public boolean createObjectOwnershipInheritanceWithChangeSOVCheck(Context context, String[] args) throws Exception  {
    String fromId = args[0];
    String toId = args[1];
    String relId = args[2];
    String relType = args[3];
    String fromType = args[4];
    String toType = args[5];

    DomainObject documentObj = new DomainObject();
    documentObj.setId(toId);

    String hasSOV = documentObj.getInfo(context, "current.access[changesov]");
    if ("true".equalsIgnoreCase(hasSOV)) {
         return createObjectOwnershipInheritance(context, fromId, toId, relId, relType, fromType, toType, null, false);
    }else{
         return true;
    }
}

    /************ APIs related to UI SB table component. ***************/
    @com.matrixone.apps.framework.ui.ProgramCallable
    static public MapList getObjectAccessList(Context context, String[] args) throws Exception {
        Map<?, ?> programMap = (Map<?, ?>) JPO.unpackArgs(args);
        String id = (String) programMap.get("objectId");
        if (id == null) {
            Map<?, ?> paramList = (Map<?, ?>) programMap.get("paramList");
            id = (String) paramList.get("objectId");
        }
        if (id.indexOf(':') != -1) { //if id is one of the access masks, then ignore.
            return new MapList();
        }
        MapList results = DomainAccess.getAccessSummaryList(context, id);
        DomainObject domainObject = new DomainObject(id);
        if(domainObject.isKindOf(context, DomainConstants.TYPE_WORKSPACE_VAULT)){
            String wVaultOwner = domainObject.getInfo(context, DomainConstants.SELECT_OWNER);
            String[] param = {id};
            String workspaceId ="";
            workspaceId= (String)JPO.invoke(context, "emxWorkspaceFolder", null, "getProjectId", JPO.packArgs(param), Object.class);
            if(UIUtil.isNotNullAndNotEmpty(workspaceId)){
            DomainObject workspaceObj = new DomainObject(workspaceId);
            String wsOwner = workspaceObj.getInfo(context, DomainConstants.SELECT_OWNER);
            wsOwner += "_PRJ";
            wVaultOwner += "_PRJ";
            Iterator resultsItr = results.iterator();
            while(resultsItr.hasNext()){
                Map mapObjects = (Map) resultsItr.next();
                String member = (String)mapObjects.get(DomainConstants.SELECT_NAME);
                if(UIUtil.isNotNullAndNotEmpty(member)&& (wsOwner.equals(member) || wVaultOwner.equals(member))){
                    mapObjects.put("disableSelection","true");
                }else{
                    mapObjects.put("disableSelection","false");
                }
            }
        }
        }
        //MapList results = getAccessSummaryList(context, id);
        return results;
    }

    static public void deleteTableOwnershipData(Context context, String[] args) throws Exception {
        HashMap<?, ?> programMap = (HashMap<?, ?>) JPO.unpackArgs(args);
        //String objectId = (String) programMap.get("objectId");
        Map<?, ?> requestValuesMap = (Map<?, ?>) programMap.get("RequestValuesMap");
        String[] emxTableRowId = (String[]) requestValuesMap.get("emxTableRowId");

        if (emxTableRowId != null)
        {
            for(int i=0; i<emxTableRowId.length ; i++)
            {
                //each row contains the relationship id, object id, parent, etc.
                String ownershipRow = emxTableRowId[i];
                StringList ownershipRowInfo = FrameworkUtil.split(ownershipRow, "|");
                String ownershipId = (String) ownershipRowInfo.get(1);

                //the ownership id is a colon delimited busId, organization, project & comment.
                StringList ownershipInfo = FrameworkUtil.split(ownershipId, ":");
                String busId = (String) ownershipInfo.get(0);
                String organization = (String) ownershipInfo.get(1);
                String project = (String) ownershipInfo.get(2);
                String comment = (String) ownershipInfo.get(3);

                DomainAccess.deleteObjectOwnership(context, busId, organization, project, comment);
            }
        }
    }

    static private StringList getTableData(Context context, String[] args, String key) throws Exception {
        Map<?, ?> programMap = (Map<?, ?>) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        StringList results = new StringList(objectList.size());
        for (int i=0; i < objectList.size(); i++) {
            Map<?, ?> map = (Map<?, ?>) objectList.get(i);
            String value = (String) map.get(key);
            results.addElement(value);
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    static public Map<String, StringList> getObjectLogicalAccessRange(Context context, String[] args) throws Exception {
        Map<?, ?> programMap = (Map<?, ?>) JPO.unpackArgs(args);
        Map<?, ?> requestMap = (HashMap<?, ?>) programMap.get("requestMap");
        String busId = (String) requestMap.get("objectId");
        HashMap<String, StringList> rangeMap = new HashMap<String, StringList>();
        List<?> fieldChoices = DomainAccess.getObjectLogicalAccessRange(context, busId);
        StringList fieldChoicesDisplay = new StringList(fieldChoices.size());
        fieldChoicesDisplay.addAll(fieldChoices);
        rangeMap.put("field_choices", fieldChoicesDisplay);
        rangeMap.put("field_display_choices", fieldChoicesDisplay);

        return rangeMap;
    }

    static public StringList getObjectAccessIds(Context context, String[] args) throws Exception {
        return getTableData(context, args, DomainAccess.KEY_ACCESS_ID);
    }

    static public StringList getObjectAccessNames(Context context, String[] args) throws Exception {
        return getTableData(context, args, DomainAccess.KEY_ACCESS_NAME);
    }

    static public StringList getOrganizations(Context context, String[] args) throws Exception {
        return getTableData(context, args, DomainAccess.KEY_ACCESS_ORG);
    }

    static public StringList getProjects(Context context, String[] args) throws Exception {
        return getTableData(context, args, DomainAccess.KEY_ACCESS_PROJECT);
    }

    static public Vector getAccesses(Context context, String[] args) throws Exception {
        return getAccessDisplayValues(context,args, DomainAccess.KEY_ACCESS_GRANTED);
    }

    static public StringList getInheritedName(Context context, String[] args) throws Exception {
        return getTableData(context, args, DomainAccess.KEY_ACCESS_INHERITED_NAME);
    }

    static public StringList getComments(Context context, String[] args) throws Exception {

      Map<?, ?> programMap        = (Map<?, ?>) JPO.unpackArgs(args);
        HashMap<?, ?> requestMap    = (HashMap<?, ?>) programMap.get("paramList");
        String languageStr      = (String)requestMap.get("languageStr");

      StringList commentList = getTableData(context, args, DomainAccess.KEY_ACCESS_COMMENT);
      StringList commentListDisp = new StringList();
      String translatedComments = "";
	  String translatedPrimaryComment = "";
      try
      {
          translatedComments = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(languageStr), "emxFramework.MultipleOwnership.Comments");
		  translatedPrimaryComment = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(languageStr), "emxFramework.MultipleOwnership.Primary");
      } catch (Exception ex)
      {
          translatedComments = DomainAccess.COMMENT_MULTIPLE_OWNERSHIP;
      }
      for (int i = 0; i < commentList.size(); i++) {
        String comment = (String)commentList.get(i);
        if(DomainAccess.COMMENT_MULTIPLE_OWNERSHIP.equals(comment)){
          commentListDisp.addElement(translatedComments);
		  }else if("Primary".equalsIgnoreCase(comment)){
                commentListDisp.addElement(translatedPrimaryComment);
        } else {
          commentListDisp.addElement(comment);
        }
    }
      return commentListDisp;
    }

    static public Vector getInheritedAccess(Context context, String[] args) throws Exception {
       return getAccessDisplayValues(context,args, DomainAccess.KEY_ACCESS_INHERITED_ACCESS);
    }
    private static Vector getAccessDisplayValues(Context context, String[] args, String column) throws Exception
    {
        Map<?, ?> programMap        = (Map<?, ?>) JPO.unpackArgs(args);
        HashMap<?, ?> requestMap    = (HashMap<?, ?>) programMap.get("paramList");
        String languageStr      = (String)requestMap.get("languageStr");
        Vector returnVector= new Vector();

        StringList columnList = getTableData(context, args,column);
        StringList columnDisplayList = new StringList();
        for(int i=0; i<columnList.size(); i++){
            HashMap cellMap = new HashMap();
            String access = (String)columnList.get(i);
            String accessDisp = getAccessDisplayValue(context, access, languageStr);
            columnDisplayList.addElement(accessDisp);
            cellMap.put("ActualValue",access);
            cellMap.put("DisplayValue",accessDisp);
            returnVector.add(cellMap);
        }
        return returnVector;
    }

    /* Based on policy, determine access rights */
    static public StringList getAccessRights(Context context, String[] args) throws Exception {
        return null;
    }

    @SuppressWarnings({ "static-access", "unchecked" })
    public StringList removeLesserAccess(Context context, StringList accessList, String inheritedAccess) throws Exception {

      AccessUtil accessUtil = new AccessUtil();

        StringList accessListSequence = new StringList();
        accessListSequence.add(0, accessUtil.BASIC);
        accessListSequence.add(1, accessUtil.READ);
        accessListSequence.add(2, accessUtil.READ_WRITE);
        accessListSequence.add(3, accessUtil.ADD);
        accessListSequence.add(4, accessUtil.REMOVE);
        accessListSequence.add(5, accessUtil.ADD_REMOVE);
        accessListSequence.add(6, accessUtil.FULL);

        int accessIndex = accessListSequence.indexOf(inheritedAccess);

      for(int i=0; i<accessIndex; i++){
        accessList.remove(accessListSequence.get(i));
      }
      return accessList;
    }

    public static String getAccessDisplayValue(Context context, String accesses, String languageStr) throws Exception {
        if(UIUtil.isNotNullAndNotEmpty(accesses)){
            StringBuffer accessDisps = null;
            StringTokenizer tok = new StringTokenizer(accesses,"|");
            while(tok.hasMoreTokens()){
                String access = tok.nextToken();
                if(access.contains(" ")){
                    access =FrameworkUtil.findAndReplace(access, " ", "");
                }
                String accessDisp = access;
                try
                {
                    String key = "emxFramework.Access."+access;
                    accessDisp = EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(languageStr), key);
                    if( key.equals(accessDisp))
                    {
                        accessDisp = access;
                    }
                } catch (Exception ex)
                {
                    //Do Nothing
                }
                if(accessDisps == null){
                    accessDisps = new StringBuffer(accessDisp);
                }else {
                    accessDisps.append("|").append(accessDisp);
                }
            }

            return accessDisps.toString();
        }
        return accesses;
    }


    /**To get the Accesses for Add Ownership
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked" })
    public Map<String, StringList> getAccessRange(Context context, String[] args)
    throws Exception
    {
        Map<?, ?> programMap        = (Map<?, ?>) JPO.unpackArgs(args);
        HashMap<?, ?> requestMap    = (HashMap<?, ?>) programMap.get("requestMap");
        HashMap<?, ?> columnValues =(HashMap<?, ?>) programMap.get("columnValues");
        HashMap<String, StringList> rangeMap = new HashMap<String, StringList>();
        String languageStr      = (String)requestMap.get("languageStr");
        if(columnValues!=null && columnValues.size()>0){
        String inheritedAccess = (String)columnValues.get("Inherited Access");
        String currentAccess = (String)columnValues.get("Access");
        String objectId             = (String) requestMap.get("objectId");

        StringList tempList = FrameworkUtil.split(inheritedAccess, "|");
        inheritedAccess = (tempList.size()>0)? tempList.get(0).toString().trim():inheritedAccess;

        StringList accessList       = DomainAccess.getLogicalNames(context, objectId);
        StringList accessListCopy = new StringList();
        StringList accessListDisp   = new StringList();
        accessListCopy.addAll(accessList);

        HashMap<?, ?> rowValues =(HashMap<?, ?>) programMap.get("rowValues");
        String inheritedOId = (String) rowValues.get("objectId");
    	inheritedOId = inheritedOId.substring(0,inheritedOId.indexOf(":") > -1 ? inheritedOId.indexOf(":") : inheritedOId.length());

        if(UIUtil.isNotNullAndNotEmpty(inheritedAccess) && currentAccess.equals(inheritedAccess)){

        	String inheritedPhysicalMasks = DomainAccess.getPhysicalAccessMasks(context,inheritedOId, inheritedAccess);
        	accessListCopy = removeLesserAccess_new(context, accessListCopy, inheritedAccess, inheritedPhysicalMasks.length(),objectId, true, inheritedOId);
        }else if(UIUtil.isNotNullAndNotEmpty(currentAccess)){
        	String physicalMasks = "";
			//to restrict the less access bits, we need the parent access bits information.
        	if(objectId.equals(inheritedOId)){

        		String cmd = "print bus $1 select $2 $3 dump $4";
        		String mqlOutput = MqlUtil.mqlCommand(context, cmd, objectId, "ownership.businessobject","id","|");
        		StringList tokens = FrameworkUtil.splitString(mqlOutput, String.valueOf("|"));
        		while (true) {
        			String id = (String) tokens.remove(0);
        			if (objectId.equals(id)) {
        				break;
        			}
        			if(id != null && !"".equals(id) )
        			{
        				inheritedOId = id;
        			}
        		}
        	}

        if(UIUtil.isNotNullAndNotEmpty(inheritedAccess)){
        		physicalMasks = DomainAccess.getPhysicalAccessMasks(context,inheritedOId, inheritedAccess);
        		accessListCopy = removeLesserAccess_new(context, accessListCopy, inheritedAccess, physicalMasks.length(),objectId, true, inheritedOId);
        	}else{
        		accessListCopy = removeLesserAccess_new(context, accessListCopy, currentAccess, physicalMasks.length(),objectId);
        	}
    }

        for(int i=0; i<accessListCopy.size(); i++){
          String access = (String)accessListCopy.get(i);
          access =  access.contains("|")?access.substring(0, access.indexOf("|")):access;
          String accessDisp = getAccessDisplayValue(context, access, languageStr);
          accessListDisp.add(accessDisp);
        }

        rangeMap.put("RangeValues",accessListCopy);
        rangeMap.put("RangeDisplayValue", accessListDisp);
        }

        return rangeMap;
    }


    /**To update the access value for Add Ownership
     * @param context
     * @param args
     * @throws Exception
     */
    public void updateAccessValue(Context context, String[] args)throws Exception {
        Map<?, ?> programMap        = (Map<?, ?>) JPO.unpackArgs(args);
        HashMap<?, ?> requestMap    = (HashMap<?, ?>) programMap.get("requestMap");
        String objectId     = (String) requestMap.get("objectId");
        HashMap<?, ?> paramMap  = (HashMap<?, ?>) programMap.get("paramMap");
        String access     = (String)paramMap.get("New Value");
        String dataDetails    = (String)paramMap.get("objectId");
        StringList valueList = StringUtil.split(dataDetails, ":");
        if( valueList.size() >= 4)
        {
          String org = (String)valueList.get(1);
          String project = (String)valueList.get(2);
          String comment = (String)valueList.get(3);
          String physicalMasks = DomainAccess.getPhysicalAccessMasks(context, objectId, access);
          if(access.equals(physicalMasks)&& access.contains("|") ){
        	  if( DomainAccess.hasObjectOwnership(context, objectId, org, project, comment)){
        		  org = (org == null || DomainConstants.EMPTY_STRING.equals(org)|| DomainAccess.MULTIPLWOWNERSHIP_ADMINISTRATION.equals(org)) ? "-" : org;
        		  project = (project == null || DomainConstants.EMPTY_STRING.equals(project)|| DomainAccess.MULTIPLWOWNERSHIP_ADMINISTRATION.equals(project)) ? "-" : project;
        		  comment = (comment == null) ? DomainConstants.EMPTY_STRING : comment;
	        	  String command = "modify bus $1 remove ownership $2 $3 for $4";
	          	  MqlUtil.mqlCommand(context, command, false, objectId, org, project, comment);
        	  }
        	  //DomainAccess.createObjectOwnership(context, objectId, access.substring(access.indexOf("|")+1, access.length()), comment, false);
          }else{
        DomainAccess.createObjectOwnership(context, objectId, org, project, access, comment, true);
        }
    }
    }

    /**To get the Organizations and Projects unique combination
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public MapList getObjectList(Context context, String[] args) throws Exception
    {
        HashMap programMap  = (HashMap) JPO.unpackArgs(args);
        String userName     = (String) programMap.get("userName");
        MapList mapList     = PersonUtil.getSecurityContexts(context, userName, null);
        HashSet<Map>  hashSet = new HashSet<Map>();
        for(int i = 0; i < mapList.size(); i++){
            hashSet.add((Map)mapList.get(i));
        }
        mapList.clear();
        mapList.addAll(hashSet);
        return mapList;
    }

    /**To get the Organization
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getOrganizationToDisplay(Context context, String[] args) throws Exception
    {
        StringList orgList = new StringList();
        HashMap<?, ?> programMap = (HashMap<?, ?>)JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        for(int i = 0; i < objectList.size(); i++)
        {
            Map<?, ?> map         = (Map<?, ?>)objectList.get(i);
            String orgName  = (String) map.get("from[Security Context Organization].to.name");
            orgList.addElement(orgName);
        }
        return orgList;
    }

     /** To get the projects
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getProjectsToDisplay(Context context, String[] args) throws Exception
    {
        StringList projList = new StringList();
        HashMap<?, ?> programMap = (HashMap<?, ?>)JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        for(int i = 0; i < objectList.size(); i++)
        {
            Map<?, ?> map         = (Map<?, ?>)objectList.get(i);
            String projects = (String) map.get("from[Security Context Project].to.name");
            projList.addElement(projects);
        }
        return projList;
    }

     /**To reload the Oraganizations on change of Person with
       * Default Organization highlighted
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, StringList> reloadOrganizations(Context context, String[] args) throws Exception
    {
        HashMap<String, StringList> orgMap        = new HashMap<String, StringList>();
        HashMap<?, ?> fieldMap      = (HashMap<?, ?>)JPO.unpackArgs(args);
        HashMap<?, ?> fieldValue    = (HashMap<?, ?>) fieldMap.get("fieldValues");
        String userName       = ((String) fieldValue.get("Name")).trim();

        //to get the Default Organization
        String defaultOrg     = PersonUtil.getDefaultOrganization(context, userName);
        StringList resultList = PersonUtil.getOrganizations(context, userName, "");
        //to remove the duplicate entries from the list
        resultList            = removeDuplicates(resultList);
        orgMap.put("RangeValues", displayDefaultValueOnTop(defaultOrg, resultList));
        return orgMap;
    }

      /**To reload the projects on change of the Person
       * with default Project highlighted
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, StringList> reloadProjects(Context context, String[] args) throws Exception
    {
        HashMap<String, StringList> projMap       = new HashMap<String, StringList>();
        HashMap<?, ?> fieldMap      = (HashMap<?, ?>)JPO.unpackArgs(args);
        HashMap<?, ?> fieldValue    = (HashMap<?, ?>) fieldMap.get("fieldValues");
        String userName       = ((String) fieldValue.get("Name")).trim();
        String Organization   = (String) fieldValue.get("Organization");

        //to get the default Project
        String defualtProj    = PersonUtil.getDefaultProject(context, userName);
        StringList resultList = PersonUtil.getProjects(context, userName, Organization);
        //to remove the duplicate entries from the list
        resultList = removeDuplicates(resultList);

        projMap.put("RangeValues", displayDefaultValueOnTop(defualtProj, resultList));

        return projMap;
    }



    /**To update the ownership of the workspace
     * @param context
     * @param args
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.PostProcessCallable
    public void updateOwnership(Context context, String[] args) throws Exception
    {
        HashMap<?, ?> fieldMap      = (HashMap<?, ?>)JPO.unpackArgs(args);
        HashMap<?, ?> requestMap    = (HashMap<?, ?>) fieldMap.get("requestMap");
        String objectId       = (String)requestMap.get("objectId");
        String owner          = (String) requestMap.get("Name");
        String Organization   = (String) requestMap.get("Organization");
        String project        = (String) requestMap.get("Project");
        try{
            DomainObject domainObject = new DomainObject(objectId);
            domainObject.TransferOwnership(context, owner, project, Organization);
        } catch(Exception e){
            emxContextUtil_mxJPO.mqlNotice(context,e.getMessage());
        }
    }

    /**To remove the duplicate entries from the list
     * @param list
     * @return
     */
    @SuppressWarnings("unchecked")
    private static StringList removeDuplicates(StringList list)
    {
        HashSet<String> hashSet = new HashSet<String>();
        for(int i = 0; i < list.size(); i++)
        {
            hashSet.add((String)list.get(i));
        }
        list.clear();
        list.addAll(hashSet);
        return list;
    }

    /**To add  the members for a workspace for security context with access
         * @param context
         * @param args
         * @throws Exception
         */
        public void addMember(Context context, String[] args) throws Exception
        {
              try
              {
            HashMap<?, ?> paramMap      = (HashMap<?, ?>)JPO.unpackArgs(args);
            //String objectId = (String)paramMap.get("busObjId");
            String access = (String)paramMap.get("access");
            //String comment = "Multiple Ownership For Object";
            String[] ids = (String[])paramMap.get("emxTableRowIds");
            for(int i =0; i<ids.length;i++)
            {
                StringList idList = com.matrixone.apps.domain.util.StringUtil.split(ids[i], "|");
                if( idList.size() >2 )
                {
                    String busId = (String)idList.get(1);
                    String personId = (String)idList.get(0);
                    DomainAccess.createObjectOwnership(context, busId, personId, access, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);

                }
            }
        }
              catch(Exception e)
              {
                emxContextUtil_mxJPO.mqlNotice(context,e.getMessage());
              }
          }
        /**To add  the organization for a workspace for security context with access
         * @param context
         * @param args
         * @throws Exception
         */

        public void addOrganization(Context context, String[] args) throws Exception
        {
              try
              {
            HashMap<?, ?> paramMap      = (HashMap<?, ?>)JPO.unpackArgs(args);
            //String objectId = (String)paramMap.get("busObjId");
            String access = (String)paramMap.get("access");
            //String comment = "Multiple Ownership For Object";
            String[] ids = (String[])paramMap.get("emxTableRowIds");
            for(int i =0; i<ids.length;i++)
            {
                StringList idList = com.matrixone.apps.domain.util.StringUtil.split(ids[i], "|");
                if( idList.size() >2 )
                {
                    String busId = (String)idList.get(1);
                    String orgId = (String)idList.get(0);
                    String orgName = new DomainObject(orgId).getInfo(context, DomainObject.SELECT_NAME);
                    DomainAccess.createObjectOwnership(context, busId, orgName,null, access, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                }
            }
        }
              catch(Exception e)
              {
                emxContextUtil_mxJPO.mqlNotice(context,e.getMessage());
              }
          }
        /**To add  the project for a workspace for security context with access
         * @param context
         * @param args
         * @throws Exception
         */
        public void addProject(Context context, String[] args) throws Exception
        {
              try
              {
            HashMap<?, ?> paramMap      = (HashMap<?, ?>)JPO.unpackArgs(args);
            //String objectId = (String)paramMap.get("busObjId");
            String access = (String)paramMap.get("access");
            //String comment = "Multiple Ownership For Object";
            String[] ids = (String[])paramMap.get("emxTableRowIds");
            for(int i =0; i<ids.length;i++)
            {
                StringList idList = com.matrixone.apps.domain.util.StringUtil.split(ids[i], "|");
                if( idList.size() >2 )
                {
                    String busId = (String)idList.get(1);
                    String projectId = (String)idList.get(0);
                    String projectName = new DomainObject(projectId).getInfo(context, DomainObject.SELECT_NAME);
                    DomainAccess.createObjectOwnership(context, busId, null,projectName, access, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                }
            }
        }
              catch(Exception e)
              {
                emxContextUtil_mxJPO.mqlNotice(context,e.getMessage());
              }
          }
        /**To add  the project and organization for a workspace for security context with access
         * @param context
         * @param args
         * @throws Exception
         */
        public void addSecurityContext(Context context, String[] args) throws Exception
        {
              try
              {
            HashMap<?, ?> paramMap      = (HashMap<?, ?>)JPO.unpackArgs(args);
            //String objectId = (String)paramMap.get("busObjId");
            String access = (String)paramMap.get("access");
            //String comment = "Multiple Ownership For Object";
            String[] ids = (String[])paramMap.get("emxTableRowIds");

            for(int i =0; i<ids.length;i++)
            {
                StringList idList = com.matrixone.apps.domain.util.StringUtil.split(ids[i], "|");
                if( idList.size() >2 )
                {
                    String busId = (String)idList.get(1);
                    String secContextId = (String)idList.get(0);
                    String orgName = new DomainObject(secContextId).getInfo(context, DomainConstants.SELECT_RELATIONSHIP_SECURITY_CONTEXT_ORGANIZATION_TO_NAME);
                    String projectName = new DomainObject(secContextId).getInfo(context, DomainConstants.SELECT_RELATIONSHIP_SECURITY_CONTEXT_PROJECT_TO_NAME);
                    DomainAccess.createObjectOwnership(context, busId, orgName,projectName, access, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                }
            }
        }
              catch(Exception e)
              {
                emxContextUtil_mxJPO.mqlNotice(context,e.getMessage());
              }
          }
        /**To remove the access for a workspace for security context with access
         * @param context
         * @param args
         * @throws Exception
         */

        @SuppressWarnings("deprecation")
        public void deleteAccess(Context context, String[] args) throws Exception
        {

            try
            {
            HashMap<?, ?> paramMap      = (HashMap<?, ?>)JPO.unpackArgs(args);
            String[] ids = (String[])paramMap.get("emxTableRowIds");
            for(int i =0; i<ids.length;i++)
            {
                StringList idList = com.matrixone.apps.domain.util.StringUtil.split(ids[i], "|");
                if( idList.size() >2 )
                {
                    String busId = (String)idList.get(1);
                        String ownerName = "";
                    String strProject = "";
                        String strOrganization = "";
                        if (UIUtil.isNotNullAndNotEmpty(busId))
                        {
                              ownerName = new DomainObject(busId).getOwner(context).getName();
                        }
                    String secContextId = (String)idList.get(0);
                        int fIndex = -1,sIndex = -1,tIndex = -1;
                        fIndex =secContextId.indexOf(":");
                        if (fIndex != -1)
                        {
                          sIndex =secContextId.indexOf(":", fIndex+1);
                        }
                        if (sIndex != -1)
                        {
                          tIndex =secContextId.indexOf(":", sIndex+1);
                        }
                        if (fIndex != -1 && sIndex != -1)
                        {
                      strOrganization = secContextId.substring(fIndex+1, sIndex);
                        }
                        if (sIndex != -1 && tIndex != -1)
                        {
                      strProject = secContextId.substring(sIndex+1, tIndex);
                        }
                    boolean ownerCheck = false;
                    if (strProject.length() !=0)
                    {
                        if(strProject.indexOf("_")>1)
                        {
                            ownerCheck = strProject.substring(0, strProject.lastIndexOf("_")).equals(ownerName);
                        }
                    }
                    if(UIUtil.isNotNullAndNotEmpty(strProject))
                    {
                        String cmd = "print role $1 select person dump";
                        String result = MqlUtil.mqlCommand(context, cmd, strProject);
                        if( context.getUser().equals(result))
                        {
                          ownerCheck = true;
                      }
                    }
                    if (ownerCheck)
          {
            String languageStr = context.getSession().getLanguage();
            String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.CannotDeletetheOwner",new Locale(languageStr));
            throw new Exception(exMsg);
                    }
                    String strComments = secContextId.substring(tIndex+1);
                    boolean hasOwnershipAccess = DomainAccess.hasObjectOwnership(context, busId, strOrganization, strProject, strComments);
                    if (strComments.length()!= 0 && !ownerCheck && hasOwnershipAccess)
                    {
                        DomainAccess.deleteObjectOwnership(context, busId, strOrganization,strProject,strComments);
                    } else {
                        String sErrorMessage = EnoviaResourceBundle.getProperty(context, "emxFramework.Common.DomainAccessDeleteAccessErrorMessage");
                        emxContextUtil_mxJPO.mqlNotice(context,sErrorMessage);
                    }
                }
                }
            }
            catch(Exception e)
            {
              emxContextUtil_mxJPO.mqlNotice(context,e.getMessage());
            }
        }


    @SuppressWarnings("unchecked")
    public static StringList getCellLevelEditAccess(Context context, String args[])throws Exception
    {
        HashMap<?, ?> inputMap = (HashMap<?, ?>)JPO.unpackArgs(args);
        MapList objectMap = (MapList) inputMap.get("objectList");
        //List<Boolean> returnStringList = new ArrayList<Boolean>(objectMap.size());
        StringList returnStringList = new StringList(objectMap.size());
        Iterator<?> objectItr = objectMap.iterator();
        String owner = "";
        String objectProject = "";
        String objectOrg = "";
        String changeOwnerAccess = "";
        String changeSovAccess = "";
        while (objectItr.hasNext()) {
            Map<?,?> curObjectMap = (Map<?,?>) objectItr.next();
            String curObjectID = (String) curObjectMap.get("id");
            String disableSelection = (String)curObjectMap.get("disableSelection");
            StringList valueList = StringUtil.split(curObjectID, ":");
            if(valueList.size() > 1 ) {
                DomainObject obj = new DomainObject((String)valueList.get(0));
                StringList selects = new StringList(3);
                selects.addElement(DomainConstants.SELECT_OWNER);
                selects.addElement("current.access[changeowner]");
                selects.addElement("organization");
                selects.addElement("project");
                selects.addElement("current.access[changesov]");
                Map objMap = obj.getInfo(context, selects);
                if("".equals(owner)) {
                    owner = (String)objMap.get(DomainConstants.SELECT_OWNER);
                    changeOwnerAccess = (String)objMap.get("current.access[changeowner]");
                    changeSovAccess = (String)objMap.get("current.access[changesov]");
            }
                objectOrg = (String)objMap.get("organization");
                objectProject = (String)objMap.get("project");
            }
            if( !changeOwnerAccess.equals( changeSovAccess ) )
            {
                if( "true".equalsIgnoreCase(changeOwnerAccess) || "true".equalsIgnoreCase(changeSovAccess) )
                {
                    changeOwnerAccess = "true";
                }
            }
            if("false".equalsIgnoreCase(changeOwnerAccess)) {
                returnStringList.add(Boolean.valueOf(false));
            } else if( valueList.size() >= 4 ) {
                String project = (String)valueList.get(2);
                if( project.contains("_PRJ")) {
                    String cmd = "print role $1 select person dump";
                    String result = MqlUtil.mqlCommand(context, cmd, project);
                    if( context.getUser().equals(result) || result.equals(owner) || "true".equalsIgnoreCase(disableSelection)) {
                        returnStringList.add(Boolean.valueOf(false));
                    } else {
                        returnStringList.add(Boolean.valueOf(true));
                    }
                } else if(!DomainConstants.EMPTY_STRING.equals(curObjectMap.get("org")) || !DomainConstants.EMPTY_STRING.equals(curObjectMap.get("project"))) {
                    boolean org = ((String) curObjectMap.get("org")).equals(objectOrg);
                    boolean proj = ((String) curObjectMap.get("project")).equals(objectProject);
                    if(org && proj) {
                        returnStringList.add(Boolean.valueOf(false));
                    } else {
                        returnStringList.add(Boolean.valueOf(true));
                    }
                } else {
                    returnStringList.add(Boolean.valueOf(true));
                }
            } else {
                returnStringList.add(Boolean.valueOf(true));
            }
        }
        return returnStringList;
    }
    @SuppressWarnings("deprecation")
    public void clearMultipleOwnership(Context context, String[] args) throws Exception  {
        String objectId = args[0];
        if( objectId != null && !"".equals(objectId))
        {
          try
          {
            ContextUtil.pushContext(context);
            DomainAccess.clearMultipleOwnership(context, objectId);
          } finally
          {
            ContextUtil.popContext(context);
          }
        }
    }


    public boolean clearInheritedOwnership(Context context, String[] args) throws Exception  {
        String objectId = args[0];
        context.printTrace("logwriter","In emxDomainAccessBase_mxJPO:clearInheritedOwnership args[0] is="+objectId);
        if(FrameworkUtil.isObjectId(context, objectId))
        {
            DomainAccess.clearInheritedOwnership(context, objectId);
        }

        return true;
    }

    /** To get the logged in user
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    public String getContextUser(Context context, String[] args) throws Exception {
        return context.getUser();
    }


    /** To exclude the members already added to the object using 'Add Member' command in Multi Ownership Access page
     * @param context
     * @param args
     * @returns a StringList: List of ObjectIds to be excluded from the search
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getExcludePersonList(Context context, String args[]) throws Exception {
        return getExclusionList(context, args, "Person");
        }

    /** To exclude the Collaborative Space already added to the object using 'Add Collaborative Space' command in Multi Ownership Access page
     * @param context
     * @param args
     * @returns a StringList: List of ObjectIds to be excluded from the search
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getExcludeCollabSpaceList(Context context, String args[]) throws Exception {
        return getExclusionList(context, args, "CollabSpace");
        }


    /** To exclude the Collaborative Space already added to the object using 'Add Organization' command in Multi Ownership Access page
     * @param context
     * @param args
     * @returns a StringList: List of ObjectIds to be excluded from the search
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getExcludeOrgList(Context context, String args[]) throws Exception {
        return getExclusionList(context, args, "Org");
    }


    public StringList getInclusionList(Context context, String args[]) throws Exception{
        String objectId = args[0];
        String commandType = args[1];
        return getInclusionList(context,objectId,commandType);

    }

    /** This method returns the list of members that are available at the parent and can added to the WorkspaceVault with
     *  AccessType == 'Specific' in Multi Ownership Access page
     * @param context
     * @param objectId Workspace Vault with AccessType=='Specific'
     * @param commandType search object type :- Person,Org,CollabSpace
     * @returns a StringList: List of ObjectIds to be included in the search
     * @throws Exception
     */
    public static StringList getInclusionList(Context context, String objectId, String commandType) throws Exception {
        MapList accessList = new MapList();
        if(UIUtil.isNotNullAndNotEmpty(objectId)){
            accessList = DomainAccess.getAccessSummaryList(context, objectId);
        }
        return getOwnershipIdList(context,accessList,commandType);
    }


    /** To return the actual exclude object Id's list that are already added to the object
     * @param context
     * @param args
     * @param commandType search object type :- Person,Org,CollabSpace
     * @returns a StringList: List of ObjectIds to be excluded from the search
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public StringList getExclusionList(Context context, String args[], String commandType) throws Exception {
        MapList accessList = getObjectAccessList(context, args);
        return getOwnershipIdList(context,accessList,commandType);
    }

    /**This method returns the objectIds of the type 'commandType' from the ownership details passed as 'accessList' param.
     * @param context
     * @param accessList: Ownership details of the object
     * @param commandType search object type :- Person,Org,CollabSpace
     * @returns a StringList: List of ObjectIds
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static StringList getOwnershipIdList(Context context,MapList accessList,String commandType) throws Exception {

        StringList ids = new StringList(accessList.size());
        String type = "";
        String rev = "";
        if("CollabSpace".equals(commandType)){
            type = DomainConstants.TYPE_PNOPROJECT;
            rev = "-";
        } else if("Org".equals(commandType)){
            type = DomainConstants.TYPE_ORGANIZATION;
            rev = "*";
        }
        String queryName = "";
        for (int i = 0; i < accessList.size(); i++) {
            Map accessMap = (Map) accessList.get(i);
            String tempName = (String)accessMap.get("name");
            if("Person".equals(commandType)){
                if(tempName.indexOf("_PRJ") > 0) {
                    String personName = tempName.substring(0, tempName.indexOf("_PRJ"));
                    ids.add(PersonUtil.getPersonObjectID(context, personName));
                }
            } else {
                if("".equals(queryName)){
                    queryName = tempName;
                } else {
                    queryName += "," + tempName;
                }
            }
                }

        if(!"".equals(queryName)) {
            String result = MqlUtil.mqlCommand(context, "temp query bus $1 $2 $3 select $4 dump $5", type, queryName, rev, "id", "|");
            StringList resultList = StringUtil.split(result, "\n");

                if(resultList.size()>0){
                for(int i=0;i<resultList.size();i++){
                    StringList objectList = StringUtil.split((String)resultList.get(i), "|");
                    ids.add(objectList.get(3));
                }
            }
        }
        return ids;
    }


    /** This method returns the list of SecurityContext objects that are available at the parent and can added to the WorkspaceVault with
     *  AccessType == 'Specific' in Multi Ownership Access page
     * @param context
     * @param objectId Workspace Vault with AccessType=='Specific'
     * @returns a StringList: List of ObjectIds to be included in the search
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static StringList getInclusionSecurityContextList(Context context,String objectId)throws Exception{
        MapList accessList = new MapList();
        if(!"".equals(objectId) && objectId!=null){
            accessList = DomainAccess.getAccessSummaryList(context, objectId);
        }
        return getSecurityContextOIDsList(context,accessList);
    }

    /** To exclude the Security Context objects already added to the object using 'Add Security Context' command in Multi Ownership Access page
     * @param context
     * @param args
     * @returns a StringList: List of ObjectIds to be excluded from the search
     * @throws Exception
     */

    /** To exclude the Security Context objects already added to the object using 'Add Security Context' command in Multi Ownership Access page
     * @param context
     * @param args
     * @returns a StringList: List of ObjectIds to be excluded from the search
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getExcludeSecurityContextList(Context context, String args[]) throws Exception {

        MapList accessList = getObjectAccessList(context, args);

        return getSecurityContextOIDsList(context,accessList);
    }
    /**This method returns the objectIds of the Security Context objects from the ownership details passed as 'accessList' param.
     * @param context
     * @param accessList: Ownership details of the object
     * @returns a StringList: List of ObjectIds to be excluded from the search
     * @throws Exception
     */

    public static StringList getSecurityContextOIDsList(Context context, MapList accessList) throws Exception {

        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);


        objectSelects.add(DomainConstants.SELECT_RELATIONSHIP_SECURITY_CONTEXT_PROJECT_TO_NAME);
        objectSelects.add(DomainConstants.SELECT_RELATIONSHIP_SECURITY_CONTEXT_ORGANIZATION_TO_NAME);

        MapList securityContextList = DomainObject.findObjects(context,
                                                                DomainConstants.TYPE_SECURITYCONTEXT,
                                                                 "*",
                                                                 "*",
                                                                 "*",
                                                                 "*",
                                                                 null,
                                                                 null,
                                                                 true,
                                                                 objectSelects,
                                                                 (short) 0);
        StringList excludeSecContextIdList = new StringList();
        StringList uniqueOrgProjComboList = new StringList();


        // This map will be used to get the objectId of the already added Security Context object
        Map idMap = new HashMap();

        // To get rid off the duplicate ids
        for (int i = 0; i < securityContextList.size(); i++) {
            Map secContextMap = (Map) securityContextList.get(i);
            String id = (String) secContextMap.get(DomainConstants.SELECT_ID);
            String org = (String) secContextMap.get(DomainConstants.SELECT_RELATIONSHIP_SECURITY_CONTEXT_ORGANIZATION_TO_NAME);
            String proj = (String) secContextMap.get(DomainConstants.SELECT_RELATIONSHIP_SECURITY_CONTEXT_PROJECT_TO_NAME);
            String orgProjCombo = org.concat(".").concat(proj);


            if(uniqueOrgProjComboList.contains(orgProjCombo)){
                excludeSecContextIdList.add(id);
            } else {
                uniqueOrgProjComboList.add(orgProjCombo);
                idMap.put(orgProjCombo, id);
            }
        }


        // AccessList is already passed as the parameter
        // MapList accessList = getObjectAccessList(context, args);

        for (int i = 0; i < accessList.size(); i++) {
            Map accessMap = (Map) accessList.get(i);
            String tempName = (String)accessMap.get("name");

            if(UIUtil.isNotNullAndNotEmpty((String)idMap.get(tempName))){
                excludeSecContextIdList.add(idMap.get(tempName));
            }
        }


        return excludeSecContextIdList;
    }




    /** This method is used to display the default Organization / Project on top of the list
     * @param Organization
     * @param defaultOrg
     * @param resultList
     */
    private StringList displayDefaultValueOnTop(String defaultValue, StringList resultList) throws Exception {
      if(resultList.size() > 0 ) {
        //to remove the duplicate entries from the list
          resultList = removeDuplicates(resultList);
            for(int i = 0; i < resultList.size(); i++) {
                if(resultList.get(i).equals(defaultValue)){
                    resultList.remove(i);
                    resultList.add(0,defaultValue);
                    break;
                }
          }
            return resultList;
        }
      return resultList;
      }

    /** To get the Range of Organizations for the context user with default Org on top
     * @param context
     * @param args
     * @return HashMap
     * @throws Exception
     */
     public Object getOrgRangeValues(Context context, String[] args) throws Exception {
        HashMap tempMap = new HashMap();
        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();

        //to get the Default Organization
        String defaultOrg     = PersonUtil.getDefaultOrganization(context, context.getUser());
        StringList resultList = PersonUtil.getOrganizations(context, context.getUser(), "");

        //to remove the duplicate entries from the list
        resultList = displayDefaultValueOnTop(defaultOrg, resultList);

        for(int j = 0; j < resultList.size(); j++) {
            fieldRangeValues.addElement(resultList.get(j));
            fieldDisplayRangeValues.addElement(resultList.get(j));
        }

        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);
        return tempMap;
    }

        /** To get the Range of project for the context user with default Project on top
         * @param context
         * @param args
         * @return HashMap
         * @throws Exception
         */
        public Object getProjectRangeValues(Context context, String[] args) throws Exception {
            HashMap tempMap = new HashMap();
            StringList fieldRangeValues = new StringList();
            StringList fieldDisplayRangeValues = new StringList();

            //to get the default Project
            String defualtProj    = PersonUtil.getDefaultProject(context, context.getUser());
            String defaultOrg     = PersonUtil.getDefaultOrganization(context, context.getUser());
            StringList resultList = PersonUtil.getProjects(context, context.getUser(), defaultOrg);

            resultList = displayDefaultValueOnTop(defualtProj, resultList);

            for(int j = 0; j < resultList.size(); j++) {
                fieldRangeValues.addElement(resultList.get(j));
                fieldDisplayRangeValues.addElement(resultList.get(j));
            }

            tempMap.put("field_choices", fieldRangeValues);
            tempMap.put("field_display_choices", fieldDisplayRangeValues);
            return tempMap;
        }
    public boolean isNotARaceProject(Context context,String[] args) throws Exception
    {
        boolean isRaceProject = false;
        try
        {
            isRaceProject = PersonUtil.isRaceProject(context);
        } catch(Exception ex)
        {
            // Do nothing as default value is set in definetion of variable.
        }
        return !isRaceProject;
    }

    /**
    *
    * Set the Interface TemplateFolder on Project Template and its Folders.
    *
    * @param context The ENOVIA <code>Context</code> object.
    * @param args holds information about objects.
    * @throws Exception
    */
   public boolean setTemplateFolderInheritance(Context context, String[] args) throws Exception
   {
      try
      {
          String fromId = args[0];  // id of from object
          String toId = args[1];    // id of to object
          String objectId = args[2];    // id of to object
          String strInterfaceName = PropertyUtil.getSchemaProperty(context,"interface_TemplateFolder");
          String sCommandModifyStatement = "modify bus $1 add interface $2";

          // if fromId is null then this is the Project Template, it has no parent
          if(UIUtil.isNullOrEmpty(fromId)) {
              // add the interface to the template
              MqlUtil.mqlCommand(context, sCommandModifyStatement,objectId,strInterfaceName);
          }
          else
          {
              String sCommandPrintStatement = "print bus $1 select $2 dump";
              String sIsInterFacePresent = MqlUtil.mqlCommand(context, sCommandPrintStatement,fromId,"interface[" + strInterfaceName + "]");

              // If there is an interface on the parent add it to the new folder
              if("true".equalsIgnoreCase(sIsInterFacePresent)){
            	  String existingInterface = MqlUtil.mqlCommand(context, sCommandPrintStatement,toId,"interface[" + strInterfaceName + "]");
                  //Add only if already not added.
            	  if("false".equalsIgnoreCase(existingInterface)) {
                	  MqlUtil.mqlCommand(context, sCommandModifyStatement,toId,strInterfaceName);
                  }
              }

          }
      }
      catch(Exception ex)
      {
          ex.printStackTrace();
          throw ex;
      }
      return true;
   }

   /** To remove lesser access
    * @param context
    * @param Access bits of the context object
    * @param Inherited Access
    * @param inherited objects physical bits length
    * @param Object Id
    * @return String
    * @throws Exception
    */
   @SuppressWarnings({ "static-access", "unchecked" })
   public StringList removeLesserAccess_new(Context context, StringList accessList, String inheritedAccess, int inheritedAccessLen, String objectID) throws Exception {

   	return removeLesserAccess_new(context, accessList, inheritedAccess, inheritedAccessLen, objectID, false,"");
   }

   /** To remove lesser access
    * @param context
    * @param Access bits of the context object
    * @param Inherited Access
    * @param inherited objects physical bits length
    * @param Object Id
    * @param to show Inherited Access as first element in drop down
    * @param Inherited Object Id
    * @return String
    * @throws Exception
    */
   @SuppressWarnings({ "static-access", "unchecked" })
   public StringList removeLesserAccess_new(Context context, StringList accessList, String inheritedAccess, int inheritedAccessLen, String objectID, boolean showInheritedAccess, String InheritedObjId) throws Exception {

       StringList accessListSequence = new StringList();
       String policyName = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump", objectID, "policy");
       if(showInheritedAccess){
       	accessListSequence.add(inheritedAccess+"|"+InheritedObjId);
       }

       for(int i=0; i<accessList.size(); i++){
       		String access = DomainAccess.getPhysicalAccessMasksForPolicy(context, policyName, (String)accessList.get(i));
       		if(access.length() > inheritedAccessLen){
       			accessListSequence.add((String)accessList.get(i));
       		}
       }
       return  accessListSequence ;
   }

}
