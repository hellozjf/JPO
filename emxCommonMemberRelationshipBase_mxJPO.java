// emxCommonMemberRelationshipBase.java
//
// Copyright (c) 2002-2017 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
//

import java.io.*;
import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.common.util.*;

/**
 * The <code>emxMemberRelationshipBase</code> class represents the
 * Member relationship JPO functionality for the AEF type.
 *
 * @version AEF 9.5.1.3 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonMemberRelationshipBase_mxJPO extends com.matrixone.apps.common.MemberRelationship
{
    /** The "Project Lead" range item on the "Project Access" attribute. */
    protected static final String RANGE_PROJECT_LEAD = "Project Lead";

    /** The "Project Lead" range item on the "Project Access" attribute. */
    protected static final String RANGE_PROJECT_OWNER = "Project Owner";

    /** The "Project Assessor" range item on the "Project Access" attribute. */
    protected static final String RANGE_PROJECT_ASSESSOR = "Project Assessor";

    /** The "Financial Reviewer" range item on the "Project Access" attribute.*/
    protected static final String RANGE_FINANCIAL_REVIEWER =
            "Financial Reviewer";

    /** select the Project Access List ID from Project */
    public static final String SELECT_PROJECT_ACCESS_LIST_ID_FOR_PROJECT =
            "to[" + RELATIONSHIP_PROJECT_ACCESS_LIST + "].from.id";

    /**
     * Constructs a new emxMemberRelationship JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args an array of String arguments for this method
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     */
    public emxCommonMemberRelationshipBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        // Call the super constructor
        super();
        if (args != null && args.length > 0)
        {
            setName(args[0]);
        }
    }

    /**
     * Get the access list object for this Project.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param projectId the project id for retrieving the access list object
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    protected DomainObject getAccessListObject(Context context,
                                               String projectId)
        throws Exception
    {
        //get Project Access List object
        DomainObject accessListObject = null;
        DomainObject dmObj = DomainObject.newInstance(context);
        dmObj.setId(projectId);
        StringList palSels = new StringList();
        palSels.add(SELECT_PROJECT_ACCESS_LIST_ID_FOR_PROJECT); //for Project

        Map palMap = dmObj.getInfo(context, palSels);
        String accessListObjectId =
            (String) palMap.get(SELECT_PROJECT_ACCESS_LIST_ID_FOR_PROJECT);

      if(accessListObjectId != null && !"".equals(accessListObjectId) &&
       !"null".equals(accessListObjectId))
        {
        accessListObject =
           (DomainObject) DomainObject.newInstance(context,
                                                 accessListObjectId);
    }

        return accessListObject;//_project.getAccessListObject(context);
    }

    /**
     * When a Member relationship is created, grant the new member the proper
     * permissions to the "Project Access List" Object.
     * Note: RELID is not needed for this trigger.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args FROMOBJECTID TONAME
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    public void triggerCreateAction(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("Entering MemberRelationship triggerCreateAction");

        // get values from args.
        String fromObjectId = args[0];
        String toName = args[1];

        // grant all new members to the project access object
        DomainObject projectAccessObject = getAccessListObject(context,
                                                               fromObjectId);

        if (projectAccessObject != null)
        {
            // Build grant access mask for the new members.
            Access accessMask = new Access();

            // Set default access mask as everyony is initially added as member.
            accessMask.setReadAccess(true);
            accessMask.setShowAccess(true);
            accessMask.setExecuteAccess(true);
            accessMask.setUser(toName);

            // grant all new members to the project access object
            BusinessObjectList objects = new BusinessObjectList(1);
            objects.add(projectAccessObject);

            ContextUtil.pushContext(context,
                                    PERSON_WORKSPACE_ACCESS_GRANTOR,
                                    null,
                                    null);
            try
            {
                BusinessObject.grantAccessRights(context,
                                                 objects,
                                                 accessMask);
            }
            catch (Exception e)
            {
                DebugUtil.debug("Exception MemberRelationship connect - ",
                                e.getMessage());
                throw e;
            }
            finally
            {
                ContextUtil.popContext(context);
            }
        }

        DebugUtil.debug("Exiting MemberRelationship triggerCreateAction");
    }

    /**
     * When a Member relationship is deleted, remove the member from the
     * Project Access List object.
     * Note: No RELID is required here.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args FROMOBJECTID TONAME
     * @throws Exception if operation fails
     * @since AEF 9.5.1.3
     * @grade 0
     */
    public void triggerDeleteAction(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("Entering MemberRelationship triggerDeleteAction");

        // get values from args.
        String fromObjectId = args[0];
        String toName = args[1];

        // Revoke members from the project access list object.
        DomainObject projectAccessObject = getAccessListObject(context,
                                                               fromObjectId);

        if (projectAccessObject != null)
        {
            // Build grant access list for all members
            StringList userList = new StringList(1);
            userList.add(toName);

            BusinessObjectList objects = new BusinessObjectList(1);
            objects.add(projectAccessObject);

            ContextUtil.pushContext(context,
                                    PERSON_WORKSPACE_ACCESS_GRANTOR,
                                    null,
                                    null);
            try
            {
                BusinessObject.revokeAccessRights(context,
                                                  objects,
                                                  userList);
            }
            catch (Exception e)
            {
                DebugUtil.debug(
                    "Exception MemberRelationship triggerDeleteAction- ",
                     e.getMessage());
                throw e;
            }
            finally
            {
                ContextUtil.popContext(context);
            }
        }

        DebugUtil.debug("Exiting MemberRelationship triggerDeleteAction");
    }

    /**
     * This trigger is invoked when "Project Access" attribute is modified
     * on the Member relationship.  The trigger is on the relationship
     * it self which means it will fire for all attribute on this relationship.
     * Note: No RELID is required here.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args FROMOBJECTID TONAME ATTRNAME ATTRVALUE NEWATTRVALUE
     * @throws Exception if operation fails
     * @since AEF 9.5.1.0
     * @grade 0
     */
    public void triggerModifyAttributeAction(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("Entering triggerModifyAttributeAction");

        // get values from args.
        String fromObjectId = args[0];
        String toName = args[1];
        String attrName = args[2];
        String attrValue = args[3];
        String newAttrValue = args[4];

        if (! attrName.equals(ATTRIBUTE_PROJECT_ACCESS))
        {
            // nothing to do for other attributes.
            DebugUtil.debug("Args: " + Arrays.asList(args));

            DebugUtil.debug("Exiting triggerModifyAttributeAction - " +
                            "not Project Access attribute");
        }
        else
        {
            DomainObject projectAccessObject = getAccessListObject(
                                                    context, fromObjectId);

            if (projectAccessObject != null)
            {
                // create a new accessMak
                Access accessMask = new Access();
                accessMask.setUser(toName);
                accessMask.setReadAccess(true);
                accessMask.setShowAccess(true);
                accessMask.setExecuteAccess(true);

                if (RANGE_PROJECT_LEAD.equals(newAttrValue))
                {
                    // user is a lead
                    accessMask.setModifyAccess(true);
                }
                else if (RANGE_PROJECT_ASSESSOR.equals(newAttrValue))
                {
                    // user is a project assessor
                    accessMask.setViewFormAccess(true);
                }
                else if (RANGE_FINANCIAL_REVIEWER.equals(newAttrValue))
                {
                    // user is a financial reviewer
                    accessMask.setModifyFormAccess(true);
                }
                else if (RANGE_PROJECT_OWNER.equals(newAttrValue))
                {
                    // user is an owner
                    accessMask.setModifyAccess(true);
                    //accessMask.setViewFormAccess(true);
                    //accessMask.setModifyFormAccess(true);
                    accessMask.setOverrideAccess(true);
                }

                // set context to grantor
                ContextUtil.pushContext(context,
                                        PERSON_WORKSPACE_ACCESS_GRANTOR,
                                        null,
                                        null);
                try
                {
                    BusinessObjectList objects = new BusinessObjectList(1);
                    objects.add(projectAccessObject);
                    BusinessObject.grantAccessRights(context,
                                                     objects,
                                                     accessMask);
                }
                catch (Exception e)
                {
                    DebugUtil.debug(
                            "Exception triggerModifyAttributeAction - ",
                            e.getMessage());
                    throw e;
                }
                finally
                {
                    ContextUtil.popContext(context);
                }
            }
        }
        DebugUtil.debug("Exiting triggerModifyAttributeAction");
    }
	public void removeFromBUDepAndSub(Context context, String[] args)
            throws Exception
    {
    	String orgId = args[0];
        String personId = args[1];
        
        DomainObject person = DomainObject.newInstance(context, personId);
        
        DomainObject currOrg = DomainObject.newInstance(context,orgId);
        
    	StringList objSelects = new StringList();
        objSelects.add(DomainConstants.SELECT_ID);

        StringList relSelects =new StringList();
        relSelects.add(SELECT_RELATIONSHIP_ID);
        
        String relWhereClause="from[Member].to.id==\""+personId+"\"";
        
        //get the BU, Dep and Subsidaries
        MapList ml = currOrg.getRelatedObjects(
                context,                            // matrix context
                RELATIONSHIP_SUBSIDIARY + "," +
                RELATIONSHIP_DIVISION + "," +
                RELATIONSHIP_COMPANY_DEPARTMENT,// relationship pattern
                TYPE_ORGANIZATION,                 // object pattern
                objSelects,                   // object selects
                relSelects,                         // relationship selects
                false,                              // to direction
                true,                               // from direction
                (short) 1,                          // recursion level
                relWhereClause,                       // object where clause
                EMPTY_STRING,0);                      // relationship where clause
        
        Iterator itr = ml.iterator();
        while (itr.hasNext())
        {
            Map map = (Map) itr.next();
            String locOrgId = (String) map.get(DomainConstants.SELECT_ID);
            DomainObject locOrg = DomainObject.newInstance(context,locOrgId);
            locOrg.disconnect(context, new RelationshipType(RELATIONSHIP_MEMBER), true, person);
        }
    }

    public void removeSecurityContext(Context context, String[] args)
            throws Exception
    {
    	String orgId = args[0];
        String personId = args[1];
        
        DomainObject person = DomainObject.newInstance(context, personId);
        
        DomainObject domOrg = DomainObject.newInstance(context, orgId);
        if(domOrg.isKindOf(context, TYPE_ORGANIZATION)){
        String currentRole=context.getRole();
    	String currentApplication=context.getApplication();
    	String companyName = PropertyUtil.getSchemaProperty(context, "role_CompanyName");
    	ContextUtil.pushContext(context, null, null, null);
         DomainAccess.VPLMADMIN_COMPANYNAME_DEFAULT_SC = "ctx::VPLMAdmin."+ companyName +".Default";
         String result = MqlUtil.mqlCommand(context, "list role $1;", DomainAccess.VPLMADMIN_COMPANYNAME_DEFAULT_SC);
         if( result != null && !"".equals(result))
         {
             String userAgentAssignments = MqlUtil.mqlCommand(context, "print person $1 select  assignment dump;", context.getUser());
             if( !userAgentAssignments.contains(result) )
             {
                 MqlUtil.mqlCommand(context, "mod person $1 assign role $2;", context.getUser(), DomainAccess.VPLMADMIN_COMPANYNAME_DEFAULT_SC);
             }
         }
    	context.resetRole(DomainAccess.VPLMADMIN_COMPANYNAME_DEFAULT_SC);
    	context.setApplication("VPLM");
    	StringList relSelects = new StringList();
        relSelects.add(SELECT_RELATIONSHIP_ID);
        String relWhereClause="to.from[Security Context Organization].to.id==\""+orgId+"\"";
    	 MapList ml = person.getRelatedObjects(
                 context,                            // matrix context
                 "Assigned Security Context",                // relationship pattern
                 TYPE_SECURITYCONTEXT,                                // object pattern
                 EMPTY_STRINGLIST,                   // object selects
                 relSelects,                         // relationship selects
                 false,                              // to direction
                 true,                               // from direction
                 (short) 1,                          // recursion level
                 EMPTY_STRING,                       // object where clause
                 relWhereClause,0); 
    	 Iterator itr = ml.iterator();
         while (itr.hasNext())
         {
             Map map = (Map) itr.next();
             String connectionId = (String) map.get(SELECT_RELATIONSHIP_ID);
             DomainRelationship.disconnect(context, connectionId);
         }
         context.resetRole(currentRole);
         context.setApplication(currentApplication);
         MqlUtil.mqlCommand(context, "mod person $1 remove assign role $2;", context.getUser(), DomainAccess.VPLMADMIN_COMPANYNAME_DEFAULT_SC);
         ContextUtil.popContext(context);
        }
    }
}
