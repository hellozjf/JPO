import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.common.util.AttributeUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;

/**
 * @author WGI
 *
 */
public class emxSecurityMigrationMigratePersonObjectsBase_mxJPO extends
    emxCommonMigration_mxJPO {

  /**
   *
   */
  private static final long serialVersionUID = -8490323668001935723L;

  private static final Map<String,String> programRoleMapping = new HashMap<String,String>(6);

  static{
      programRoleMapping.put("External Program Lead", "Program Lead");
      programRoleMapping.put("External Project Administrator", "Project Administrator");
      programRoleMapping.put("External Project Lead", "Project Lead");
      programRoleMapping.put("External Project User", "Project User");
      programRoleMapping.put("Project Assessor", "Project Lead");
      programRoleMapping.put("Financial Reviewer", "Project Lead");
  }

  /**
   * @param context
   * @param args
   * @throws Exception
   */
  public emxSecurityMigrationMigratePersonObjectsBase_mxJPO(Context context,
      String[] args) throws Exception {
    super(context, args);
  }
    @SuppressWarnings({ "unchecked", "deprecation" })
    public void migrateObjects(Context context, StringList objectList) throws Exception
    {
      mqlLogRequiredInformationWriter("In emxSecurityMigrationMigratePersonObjects 'migrateObjects' method "+"\n");
      String relationshipAssignedSecurityContext = PropertyUtil.getSchemaProperty(context, "relationship_AssignedSecurityContext");
      String relationshipMember = PropertyUtil.getSchemaProperty(context, "relationship_Member");
      String relationshipEmployee = PropertyUtil.getSchemaProperty(context, "relationship_Employee");
      StringList mxObjectSelects = new StringList(5);
      String EmployeeName = "to[" + relationshipEmployee + "].from.name";
      String MemberName = "to[" + relationshipMember + "].from.name";
      String MemberId = "to[" + relationshipMember + "].from.id";
      String MemberType = "to[" + relationshipMember + "].from.type";
      String MemberRevision = "to[" + relationshipMember + "].from.revision";
      String MemberProjectRole = "to[" + relationshipMember +"].attribute[Project Role]";
      String SecurityContextName = "from[" + relationshipAssignedSecurityContext + "].to.name";
      String memberRelId         = "to[" + relationshipMember +"].id";  // added for PRG

        DomainObject.MULTI_VALUE_LIST.add(MemberName);
        DomainObject.MULTI_VALUE_LIST.add(MemberId);
        DomainObject.MULTI_VALUE_LIST.add(MemberType);
        DomainObject.MULTI_VALUE_LIST.add(MemberRevision);
        DomainObject.MULTI_VALUE_LIST.add(MemberProjectRole);
        DomainObject.MULTI_VALUE_LIST.add(SecurityContextName);
        DomainObject.MULTI_VALUE_LIST.add(memberRelId);     // added for PRG

      mxObjectSelects.addElement(EmployeeName);
      mxObjectSelects.addElement(MemberName);
      mxObjectSelects.addElement(MemberId);
      mxObjectSelects.addElement(MemberProjectRole);
      mxObjectSelects.addElement(MemberType);
      mxObjectSelects.addElement(MemberRevision);
      mxObjectSelects.addElement("id");
      mxObjectSelects.addElement("name");
      mxObjectSelects.addElement(SecurityContextName);
      mxObjectSelects.addElement(memberRelId);             //added for PRG
      String[] oidsArray = new String[objectList.size()];
        oidsArray = (String[])objectList.toArray(oidsArray);
        try
        {
          ContextUtil.pushContext(context);
          if( DomainAccess.init(context) )
          {
            context.setRole("ctx::VPLMAdmin.Company Name.Default");
            context.setApplication("VPLM");
          }
          MapList mapList = DomainObject.getInfo(context, oidsArray, mxObjectSelects);
          Iterator<?> itr = mapList.iterator();
          while(itr.hasNext())
          {
            Map<?, ?> m = (Map<?, ?>)itr.next();
            mqlLogWriter(m.toString());
            String personName = (String)m.get("name");
            mqlLogRequiredInformationWriter("==============================================================================");
            mqlLogRequiredInformationWriter("Start Migrating User == " + personName);
            String personId = (String)m.get("id");
            String companyName = (String)m.get(EmployeeName);
            boolean unconverted = false;
            String comment = "";
            if(companyName != null && !"".equals(companyName) )
            {
              assignPersonalSecurityContext(context, personName, companyName);
            } else {
              unconverted = true;
              comment = "Person "+ personName + " is not connected to any organization with Employee relationship. \n";
              comment += "Person "+ personName + " can't be used for new Security model Grants. \n";
            }

            StringList memberCompanyNames = (StringList)m.get(MemberName);
            StringList memberCompanyIds = (StringList)m.get(MemberId);
            StringList memberProjectRoleList = (StringList)m.get(MemberProjectRole);
            StringList memberTypes = (StringList)m.get(MemberType);
            StringList memberRevisions = (StringList)m.get(MemberRevision);
         // PRG specific Code
            StringList memberRelIds = (StringList)m.get(memberRelId);

            StringList securityContextNames = (StringList)m.get(SecurityContextName);
            if(memberCompanyNames != null )
            {
              for( int i=0; i<memberCompanyNames.size(); i++)
              {
                String memberCompanyName = (String)memberCompanyNames.get(i);
                String memberCompanyId = (String)memberCompanyIds.get(i);
                String memberProjectRoles = (String)memberProjectRoleList.get(i);
                String memberType = (String)memberTypes.get(i);
                String memberRevision = (String)memberRevisions.get(i);
                String memberRelationshipId = (String)memberRelIds.get(i);  // added for PRG
                    if(memberCompanyName != null && memberProjectRoles != null && !"".equals(memberCompanyName) && !"".equals(memberProjectRoles))
                    {
                      assignSecurityContext(context, memberCompanyId, personId, personName, memberProjectRoles, memberCompanyName, memberType, memberRevision, securityContextNames,memberRelationshipId);
                    }
              }
            } else {
              unconverted = true;
              comment += "Person "+ personName + " is not connected to any organization with Member relationship or Member relationship doesn't contain any value for Project Role attribute. \n";
            }
            if( unconverted )
            {
              writeUnconvertedOID(comment, personId);
            } else {
            loadMigratedOids(personId);
            }
          }
        } catch(Exception ex)
        {
          ex.printStackTrace();
            throw ex;
        }
        finally
        {
            ContextUtil.popContext(context);
        }
    }
    public void mqlLogRequiredInformationWriter(String command) throws Exception
    {
        super.mqlLogRequiredInformationWriter(command +"\n");
    }
    public void mqlLogWriter(String command) throws Exception
    {
        super.mqlLogWriter(command +"\n");
    }
    @SuppressWarnings("deprecation")
    public void assignPersonalSecurityContext(Context context, String personName, String companyName) throws Exception
    {
        try
        {
            String projectName = personName + "_PRJ";
            String prjResult = MqlUtil.mqlCommand(context, "list role $1", true, projectName);
            if( prjResult.indexOf(projectName) == -1)
            {
                MqlUtil.mqlCommand(context, "add role $1 asaproject parent $2 hidden", true, projectName, "User Projects");
                mqlLogRequiredInformationWriter("Personal Project Successfully created for "+ personName);
            } else {
                mqlLogRequiredInformationWriter("Personal Project is already created for "+ personName);
                mqlLogWriter("Personal Project is already created for "+ personName);
            }
            String SCName = "Grant."+ companyName +"."+ personName + "_PRJ";
            String result = MqlUtil.mqlCommand(context, "list role $1", true, SCName);
            if( result.indexOf(SCName) == -1 )
            {
                MqlUtil.mqlCommand(context,"add role $1 parent $2,$3,$4 assign person $5 hidden", true, SCName, "Grant", companyName, personName + "_PRJ", personName);
                mqlLogRequiredInformationWriter("Personal Security Context Successfully created for "+ personName);
            } else {
                mqlLogRequiredInformationWriter("Personal Security Context is already assigned for "+ personName);
                mqlLogWriter("Personal Security Context is already assigned for "+ personName);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    public void assignSecurityContext(Context context, String companyId, String personId, String personName, String attrValue, String companyName, String companyType, String companyRev , StringList securityContextNames, String memberRelId) throws Exception
    {
        try
        {
          if( securityContextNames == null)
          {
            securityContextNames = new StringList();
          }
            String typeOrganization = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_Organization);
            String sResult = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump",companyId,"type.kindof["+  typeOrganization +"]");
            // Verify that from type of the object is kind of Organization if not this trigger doesn't need to be run.
            if ("TRUE".equalsIgnoreCase(sResult))
            {
                StringList addedRoles = new StringList(1);
                if( attrValue != null )
                  addedRoles = StringUtil.split(attrValue, "~");
                // program central role mapping
                addedRoles = mapProgramCentralLegacyRoles(context, addedRoles, personName, companyId, memberRelId);
                //
                
                boolean addBasicUserRole = false;
                
                // To check if no role is added in case the person is created in an host company then 
                // create a security context with 'Basic User' role
                if(addedRoles.size()==1 && addedRoles.contains("role_Employee")) {
                	addBasicUserRole = true;
                }
                
                mqlLogRequiredInformationWriter("Stared adding Security Contexts for User: "+ personName);
                mqlLogWriter("Person Roles on Member Relationship "+ addedRoles);
                
                Iterator<?> addItr = addedRoles.iterator();
                                               
                while(addItr.hasNext()) {
                    String roleName = (String)addItr.next();
					
					// To check if the role is exchange User or a child of 'Exchange User' then 
                    // create a security context with 'Basic User' role
					if(roleName.equals("role_ExchangeUser") 
							|| ComponentsUtil.isChildRole(context, "Exchange User", PropertyUtil.getSchemaProperty(context, roleName))) {
						addBasicUserRole = true;
                      }
					
                      String assignedRoles = MqlUtil.mqlCommand(context, "print person $1 select assignment dump", true, personName);
                    
					if(DomainAccess.BusinessRoles.contains(PropertyUtil.getSchemaProperty(context, roleName))) {                    	
						DomainObject scObj = createAndAssignSecurityContext(context, personId, personName, roleName, companyType, companyName, companyRev);
						addRoleToPerson(context, scObj, personName, assignedRoles);                     
						
                      removeOldSCwithDefaultProject(context, roleName, companyName, personId, personName, assignedRoles);
					}
					
					// The security context for "Basic User" will be created and connected once for person if addBasicUserRole = true
					// If already exist and connected then skip
					if(addBasicUserRole){
						DomainObject basicUserScObj = createAndAssignSecurityContext(context, personId, personName, "role_BasicUser", companyType, companyName, companyRev);
						addRoleToPerson(context, basicUserScObj, personName, assignedRoles);

						mqlLogRequiredInformationWriter("Modifying person "+ personName +" by assigning the role 'Basic User'");
						MqlUtil.mqlCommand(context, "mod person $1 assign role $2", true, personName, PropertyUtil.getSchemaProperty(context, "role_BasicUser"));
                    }
                }
            } else {
              mqlLogWriter("The object "+ companyType +", "+ companyName +" is not Organization kind of obejct");
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            throw new Exception(ex);
        }
    }

    private DomainObject createAndAssignSecurityContext(Context context, String personId, String personName, String roleName, String companyType, String companyName, String companyRev) throws Exception{
    	mqlLogRequiredInformationWriter("Adding Security Context with GLOBAL Project, "+ roleName +" Role and " + companyName + " Organization for user "+ personName);
    	
    	String relationshipAssignedSecurityContext = PropertyUtil.getSchemaProperty(context, "relationship_AssignedSecurityContext");
    	DomainObject personObj = DomainObject.newInstance(context, personId);
    	String SecurityContextNameSelect = "from[" + relationshipAssignedSecurityContext + "].to.name";
    	Map securityContextNamesMap = personObj.getInfo(context, new StringList(SecurityContextNameSelect));
    	StringList securityContextNames = (StringList)securityContextNamesMap.get(SecurityContextNameSelect);
    	
    	String scId = DomainAccess.createSecurityContext(context, DomainAccess.getDefaultProject(context), roleName, companyName, companyType, companyRev);
        DomainObject scObj = DomainObject.newInstance(context, scId);
        String scName = scObj.getInfo(context, DomainConstants.SELECT_NAME);
                
        if( securityContextNames == null || !securityContextNames.contains(scName)) {
          DomainRelationship.connect(context, personId, relationshipAssignedSecurityContext, scId, true);
        }
        return scObj;        
    }
    
    private void addRoleToPerson(Context context, DomainObject scObj, String personName, String assignedRoles) throws Exception{
    	String scName = scObj.getInfo(context, DomainConstants.SELECT_NAME);
    	
        if( assignedRoles.indexOf("ctx::"+scName) < 0) {
      	  mqlLogRequiredInformationWriter("Modifying person "+ personName +" by assigning the role ctx::" + scName);
      	  MqlUtil.mqlCommand(context, "mod person $1 assign role $2", true, personName, "ctx::"+ scName);
        }
    }

    private void removeOldSCwithDefaultProject(Context context, String roleName, String companyName, String personId, String personName, String assignedRoles) {
    	try {
    		roleName = PropertyUtil.getSchemaProperty(context, roleName);
	    	String scOldName = roleName + "." +  companyName + ".Default";
	        String relationshipAssignedSecurityContext = PropertyUtil.getSchemaProperty(context, "relationship_AssignedSecurityContext");
	        DomainObject person = new DomainObject(personId);
	        StringList objectSelect = new StringList("from[" + relationshipAssignedSecurityContext +"|to.name==\""+scOldName +"\"].id");
			objectSelect.add("from[" + relationshipAssignedSecurityContext +"|to.name==\""+scOldName +"\"].to.id");
			Map scOldRelIdMap = person.getInfo(context, objectSelect);
			String scOldRelId = (String)scOldRelIdMap.get("from[Assigned Security Context].id");
			String scOldId = (String)scOldRelIdMap.get("from[Assigned Security Context].to.id");

	        if( scOldRelId != null && !"".equals(scOldRelId))
	        {
	          DomainRelationship.disconnect(context, scOldRelId);
	        }
	        if(assignedRoles.indexOf("ctx::"+scOldName) > -1)
	        {
	          MqlUtil.mqlCommand(context, "mod person $1 remove assign role $2", true, personName, "ctx::"+ scOldName);
			  if( scOldId != null && !"".equals(scOldId)) {
				MqlUtil.mqlCommand(context, "delete bus $1", true, scOldId);
			  }
	        }
    	} catch(Exception ex)
    	{
    		ex.printStackTrace();
    	}
	}
    @SuppressWarnings("unchecked")
    public StringList mapProgramCentralLegacyRoles(Context context, StringList addedRoles,String personName,String companyId, String memberRelId) throws Exception{
        mqlLogRequiredInformationWriter("\n -----Started Program Central Role Mapping for roles ---"+addedRoles);

        Set<String> nwSymbolicRoles = new HashSet<String>();  // assigned role list with symbolic names
        Set<String> programLegacyRoleList = programRoleMapping.keySet();
        boolean hasLegacyRole = false;
        String delimeter = ",";
        @SuppressWarnings("rawtypes")
        Iterator roleItr = addedRoles.iterator();
        while(roleItr.hasNext())
        {
            String symbolicRoleName = (String)roleItr.next();
            String legacyRole = PropertyUtil.getSchemaProperty(context, symbolicRoleName);

            if(programLegacyRoleList.contains(legacyRole))
            {
                if(!hasLegacyRole)
                {
                    hasLegacyRole = true;
                }
                mqlLogRequiredInformationWriter("\n ---Started Removing Assigned Legacy roles==="+legacyRole);
                String assignedRoles = MqlUtil.mqlCommand(context, "print person $1 select assignment dump $2", true, personName,delimeter);
                mqlLogRequiredInformationWriter("\n ---Person Assignment are ==="+assignedRoles);
                if(assignedRoles.indexOf(legacyRole) > -1 )
                {
                    MqlUtil.mqlCommand(context, "mod person $1 remove assign role $2", true, personName, legacyRole);
                    mqlLogRequiredInformationWriter("\n ---Completed Removing Legacy Role from Person assignment==="+legacyRole);
                }
                StringList assignedRoleList = FrameworkUtil.split(assignedRoles, delimeter);
                String mappedRole = programRoleMapping.get(legacyRole);
                if(! assignedRoleList.contains(mappedRole))
                {
                    MqlUtil.mqlCommand(context, "mod person $1 assign role $2", true, personName, mappedRole);
                    mqlLogRequiredInformationWriter("\n ---::::::Completed assigning New Mapped Role ==="+mappedRole);
                    mqlLogRequiredInformationWriter("\n ---::::::-----\n");
                }
                // create the symbolic role name list to set on project role attribute
                String nwSymbolicRoleName = PropertyUtil.getAliasForAdmin(context, "Role", mappedRole, true);
                if(!addedRoles.contains(nwSymbolicRoleName))
                { //if nw mapped role is not assigned to user on role attribute
                    nwSymbolicRoles.add(nwSymbolicRoleName);
                }
                roleItr.remove();  // remove the legacy role from original return list
            }
        }

        if(hasLegacyRole){
            addedRoles.addAll(nwSymbolicRoles);
            DomainRelationship memberRel = new DomainRelationship(memberRelId); //replace current values with new values
            AttributeUtil.setAttributeList(context, memberRel,
                    DomainConstants.ATTRIBUTE_PROJECT_ROLE,
                    addedRoles,
                    false, "~");
            mqlLogRequiredInformationWriter("\n ---Setting Attribute value on Project Role ==="+nwSymbolicRoles);
        }
        mqlLogRequiredInformationWriter("\n --------Completed Program Central Role Mapping--------------------------------------\n ");
        return addedRoles;
    }
}
