import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.util.AttributeUtil;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.program.ProgramCentralUtil;

import matrix.db.Context;
import matrix.util.StringList;


public class emxSecurityMigrationMigrateProgramPersonObjectsBase_mxJPO extends
emxSecurityMigrationMigratePersonObjectsBase_mxJPO {
  
  private  static final StringList programRoleList = new StringList(15);
  private  static final StringList programRoleToMigrateList = new StringList(15);
  private  static final StringList programLegacyRoleList = new StringList(5);
  private static final Map<String,String> programRoleMapping = new HashMap<String,String>(6);
  private static final List<String> excludeRoleList = new ArrayList<String>(1);
  
  static{
	  programRoleList.addElement("External Program Lead");
      programRoleList.addElement("External Project Administrator");
      programRoleList.addElement("External Project Lead");
      programRoleList.addElement("External Project User");
      programRoleList.addElement("Financial Reviewer");
      programRoleList.addElement("Project Administrator");
      programRoleList.addElement("Project Assessor");
      programRoleList.addElement("Program Lead");
      programRoleList.addElement("Project Lead");
      programRoleList.addElement("Project User");
      programRoleList.addElement("Resource Manager");
      
      
      programRoleToMigrateList.addElement("Program Lead");
      programRoleToMigrateList.addElement("Project Lead");
      programRoleToMigrateList.addElement("Project User");
      programRoleToMigrateList.addElement("Resource Manager");
      
      programLegacyRoleList.addElement("External Program Lead");
      programLegacyRoleList.addElement("External Project Administrator");
      programLegacyRoleList.addElement("External Project Lead");
      programLegacyRoleList.addElement("External Project User");
      programLegacyRoleList.addElement("Financial Reviewer");
      programLegacyRoleList.addElement("Project Assessor");
      
      programRoleMapping.put("External Program Lead", "Program Lead");
	  programRoleMapping.put("External Project Administrator", "Project Administrator");
	  programRoleMapping.put("External Project Lead", "Project Lead");
	  programRoleMapping.put("External Project User", "Project User");
	  programRoleMapping.put("Project Assessor", "Project Lead");
	  programRoleMapping.put("Financial Reviewer", "Project Lead");
	  
	excludeRoleList.add("Project Administrator");
  }
  /**
   * @param context
   * @param args
   * @throws Exception
   */
  public emxSecurityMigrationMigrateProgramPersonObjectsBase_mxJPO(Context context,
      String[] args) throws Exception {
    super(context, args);
  }
    @SuppressWarnings({ "unchecked", "deprecation" })
    public void migrateObjects(Context context, StringList objectList) throws Exception
    {
      mqlLogRequiredInformationWriter("--In emxSecurityMigrationMigrateProgramPersonObjects 'migrateObjects' method "+"\n");
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
      String memberRelId         = "to[" + relationshipMember +"].id";
      
        DomainObject.MULTI_VALUE_LIST.add(MemberName);
        DomainObject.MULTI_VALUE_LIST.add(MemberId);
        DomainObject.MULTI_VALUE_LIST.add(MemberType);
        DomainObject.MULTI_VALUE_LIST.add(MemberRevision);
        DomainObject.MULTI_VALUE_LIST.add(MemberProjectRole);
        DomainObject.MULTI_VALUE_LIST.add(SecurityContextName);
        DomainObject.MULTI_VALUE_LIST.add(memberRelId);
        
      mxObjectSelects.addElement(EmployeeName);
      mxObjectSelects.addElement(MemberName);
      mxObjectSelects.addElement(MemberId);
      mxObjectSelects.addElement(MemberProjectRole);
      mxObjectSelects.addElement(MemberType);
      mxObjectSelects.addElement(MemberRevision);
      mxObjectSelects.addElement("id");
      mxObjectSelects.addElement("name");
      mxObjectSelects.addElement(SecurityContextName);
      mxObjectSelects.addElement(memberRelId);
      
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
            // done by default in 2014 and in person migration
            /*if(companyName != null && !"".equals(companyName) )
            {
              assignPersonalSecurityContext(context, personName, companyName);
            } else {
              unconverted = true;
              comment = "Person "+ personName + " is not connected to any organization with Employee relationship. \n";
              comment += "Person "+ personName + " can't be used for new Security model Grants. \n";
            }*/

            StringList memberCompanyNames = (StringList)m.get(MemberName);
            StringList memberCompanyIds = (StringList)m.get(MemberId);
            StringList memberProjectRoleList = (StringList)m.get(MemberProjectRole);
            StringList memberTypes = (StringList)m.get(MemberType);
            StringList memberRevisions = (StringList)m.get(MemberRevision);
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
                String memberRelObjId = (String)memberRelIds.get(i);
                    /*if(memberCompanyName != null && memberProjectRoles != null && !"".equals(memberCompanyName) && !"".equals(memberProjectRoles))
                    {
                      assignSecurityContext(context, memberCompanyId, personId, personName, memberProjectRoles, memberCompanyName, memberType, memberRevision, securityContextNames);
                    }*/
                if(isOrganization(context,memberCompanyId))
                {
              	  if(memberCompanyName != null && memberProjectRoles != null && !"".equals(memberCompanyName) && !"".equals(memberProjectRoles))
                    {
              		 mapProgramRolesForPerson(context,personId, personName, memberProjectRoles,memberCompanyId, memberCompanyName, companyName, memberRelObjId); 
              		assignProgramSecurityContext(context, memberCompanyId, personId, personName, memberProjectRoles, memberCompanyName, memberType, memberRevision, securityContextNames);
                    }
                }else {
                    mqlLogWriter("The object "+  companyName +" is not Organization kind of obejct");
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
    
    private boolean isOrganization(Context context, String companyId) throws Exception{
    	// Verify that from type of the object is kind of Organization if not this trigger doesn't need to be run.
    	String typeOrganization = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_Organization);
         String sResult = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump",companyId,"type.kindof["+  typeOrganization +"]");
         if("TRUE".equalsIgnoreCase(sResult)){
        	 return Boolean.TRUE;
         }
        	
         return Boolean.FALSE;
    }
    
    private void mapProgramRolesForPerson(Context context,  String personId, String personName,String attrValue,String memberCompanyId, String memberCompanyName, String hostCompany, String memberRelId) throws Exception{
    	try
    	{
    		mqlLogRequiredInformationWriter("---started mapping of roles for person= "+personName + "  id ="+personId);

    		StringList personLegacyRoleList = new StringList(5); 

    		StringList addedRoles = new StringList(1);
    		if( attrValue != null )
    			addedRoles = StringUtil.split(attrValue, "~");

    		mqlLogRequiredInformationWriter("after split"+addedRoles);
    		mqlLogRequiredInformationWriter("Stared mapping roles for User: "+ personName+"  "+personId);
    		mqlLogRequiredInformationWriter("Person Roles on Member Relationship "+ addedRoles);
    		Map symbolicRoleNameMapping = new HashMap(5);	
    		Iterator<?> addItr = addedRoles.iterator();
    		while(addItr.hasNext())
    		{
    			String symbolicRoleName = (String)addItr.next();
    			String roleName = PropertyUtil.getSchemaProperty(context, symbolicRoleName);
    			if(programRoleList.contains(roleName)) {
    				symbolicRoleNameMapping.put(roleName, symbolicRoleName);
    				if(programLegacyRoleList.contains(roleName)){
    					// remove the role assignment for the person
    					mqlLogRequiredInformationWriter("Person Roles  "+ roleName + "  is LEGACY Role----");	
    					if(!personLegacyRoleList.contains(roleName))
    						personLegacyRoleList.addElement(roleName);
    				}
    			}
    		}

    		mqlLogRequiredInformationWriter("personLegacyRoleList ="+personLegacyRoleList);
    		Person person = new Person(personId);
    		// remove the legacy roles
    		// add the corrspodning mapped roles to nwRoleList

    		StringList nwRoleList = new StringList();
    		if(!personLegacyRoleList.isEmpty()){
    			mqlLogRequiredInformationWriter("Removing Person LEGACY Roles  "+ personLegacyRoleList);
    			String[] oidsArray = new String [personLegacyRoleList.size()];
    			oidsArray = (String[])personLegacyRoleList.toArray(oidsArray);
    			try{
    				person.open(context);
    				person.removeRoles(context, oidsArray);   // remove the legacy roles
    			}catch(Exception e){
    				throw e;
    			}finally{
    				person.close(context);
    			}

    			Iterator<?> roleItr = personLegacyRoleList.iterator();
    			while(roleItr.hasNext())
    			{
    				String roleName = (String)roleItr.next();
    				String mappedRoleName = programRoleMapping.get(roleName);
    				if(mappedRoleName != null && ! nwRoleList.contains(mappedRoleName)){
    					nwRoleList.add(mappedRoleName);           //create the list
    				}
    			}
    			mqlLogRequiredInformationWriter("Removed Person LEGACY Roles  mapped to "+ nwRoleList);
    		}


    		// assign the mapped roles
    		if(!nwRoleList.isEmpty()){
    			mqlLogRequiredInformationWriter("Assigning Person Mapped Roles  "+ nwRoleList);
    			mqlLogRequiredInformationWriter("Symbolic LEGACY Roles  "+ symbolicRoleNameMapping);
    			try{
    				//person.open(context);
    				//DomainObject pObj = PersonUtil.getPersonObject(context, personName);
    				//Person personobj = new Person(pObj);
    				com.matrixone.apps.common.Person personID = (com.matrixone.apps.common.Person)DomainObject.newInstance(context,
    						DomainConstants.TYPE_PERSON);
    				personID.setId(personId);
    				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_TestEverything"),
    						DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

    				personID.addRoles(context, nwRoleList);
    				// if not a host company then add the new role list to attribute project role
    				if(!hostCompany.equalsIgnoreCase(memberCompanyName)){
    					mqlLogRequiredInformationWriter("Started mapping for no host member company"+ memberCompanyName);
    					// get the relationship of the person with company
    					mqlLogRequiredInformationWriter("Assigning Person Mapped Roles  "+ nwRoleList);
    					StringList slNwSymbolicRoles = new StringList();
    					Iterator it = nwRoleList.iterator();
    					while(it.hasNext()){
    						String nwRole = (String)it.next();
    						String nwSymbolicRoleName = PropertyUtil.getAliasForAdmin(context, "Role", nwRole, true);
    						slNwSymbolicRoles.add(nwSymbolicRoleName);
    					}
    					mqlLogRequiredInformationWriter("Final Symbolic Roles List "+ slNwSymbolicRoles);
    					if(!slNwSymbolicRoles.isEmpty()){
    					DomainRelationship memberRel = new DomainRelationship(memberRelId);
    					AttributeUtil.setAttributeList(context, memberRel,
    							DomainConstants.ATTRIBUTE_PROJECT_ROLE,
    							slNwSymbolicRoles,
    							false, "~");
    					}
    				}

    			}catch(Exception e){
    				throw e;
    			}finally{
    				//	person.close(context);
    				ContextUtil.popContext(context);
    			}
    		}
    	} catch (Exception ex)
        {
            ex.printStackTrace();
            throw new Exception(ex);
        }
    }
    
    
    public void assignProgramSecurityContext(Context context, String companyId, String personId, String personName, String attrValue, String companyName, String companyType, String companyRev , StringList securityContextNames) throws Exception
    {
        try
        {
          if( securityContextNames == null)
          {
            securityContextNames = new StringList();
          }
                StringList addedRoles = new StringList(1);
                if( attrValue != null )
                  addedRoles = StringUtil.split(attrValue, "~");
                mqlLogRequiredInformationWriter("Stared adding Security Contexts for User: "+ personName);
                mqlLogRequiredInformationWriter("Person Roles on Member Relationship "+ addedRoles);
                String relationshipAssignedSecurityContext = PropertyUtil.getSchemaProperty(context, "relationship_AssignedSecurityContext");
                Iterator<?> addItr = addedRoles.iterator();
                while(addItr.hasNext())
                {
                    String symbolicRoleName = (String)addItr.next();
                    String roleName = PropertyUtil.getSchemaProperty(context, symbolicRoleName);
                    mqlLogRequiredInformationWriter("roleName= "+roleName);
                    if(programRoleToMigrateList.contains(roleName) )
                    {
                      mqlLogRequiredInformationWriter("Adding Security Context with Default Project, "+ symbolicRoleName +" Role and " + companyName + " Organization for user "+ personName);
                      String scId = DomainAccess.createSecurityContext(context, "Default", symbolicRoleName, companyName, companyType, companyRev);
                      DomainObject scObj = DomainObject.newInstance(context, scId);
                      String scName = scObj.getInfo(context, DomainObject.SELECT_NAME);
                      if( !securityContextNames.contains(scName))
                      {
                    	  mqlLogRequiredInformationWriter(" 1:: scName= "+scName);
                        DomainRelationship.connect(context, personId, relationshipAssignedSecurityContext, scId, true);
                      }
                      String assignedRoles = MqlUtil.mqlCommand(context, "print person $1 select assignment dump", true, personName);
                      if( assignedRoles.indexOf(scName) < 0)
                      {
                    	  mqlLogRequiredInformationWriter(" 2:: scName= "+assignedRoles);
                        MqlUtil.mqlCommand(context, "mod person $1 assign role $2", true, personName, "ctx::"+ scName);
                      }
                    }
                }
           
        } catch (Exception ex)
        {
            ex.printStackTrace();
            throw new Exception(ex);
        }
    }

    
    
}
