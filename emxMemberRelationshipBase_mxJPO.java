// emxMemberRelationshipBase.java
//
// Copyright (c) 2002-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
// static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.9.2.2 Thu Dec  4 07:55:08 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9.2.1 Thu Dec  4 01:53:17 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9 Wed Oct 22 15:49:48 2008 przemek Experimental przemek $
//

import java.util.Arrays;
import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.program.ProgramCentralConstants;

/**
 * The <code>emxMemberRelationshipBase</code> class represents the
 * Member relationship JPO functionality for the AEF type.
 *
 * @version AEF 9.5.1.3 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxMemberRelationshipBase_mxJPO extends com.matrixone.apps.common.MemberRelationship
{
    /** The "Project Lead" range item on the "Project Access" attribute. */
	protected static final String RANGE_PROJECT_LEAD = ProgramCentralConstants.PROJECT_ROLE_PROJECT_LEAD; //PRG:RG6:R212:29-July-2011::Code Review

    /** The "Project Lead" range item on the "Project Access" attribute. */
    protected static final String RANGE_PROJECT_OWNER = "Project Owner";

    /** The "Project Assessor" range item on the "Project Access" attribute. */
    protected static final String RANGE_PROJECT_ASSESSOR = ProgramCentralConstants.PROJECT_ROLE_PROJECT_ASSESSOR;//PRG:RG6:R212:29-July-2011::Code Review 

    /** The "Financial Reviewer" range item on the "Project Access" attribute.*/
    protected static final String RANGE_FINANCIAL_REVIEWER =
            "Financial Reviewer";

    /** The project object for this connection. */
    protected emxProjectSpace_mxJPO _project = null;

    /**
     * Constructs a new emxMemberRelationship JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the name
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     */
    public emxMemberRelationshipBase_mxJPO (Context context, String[] args)
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
     * This Method get the access list object for this Project.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param projectId String for retrieving the access list object
     * @return DomainObject access object
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     */
    protected DomainObject getAccessListObject(Context context,
                                               String projectId)
        throws Exception
    {
        if (_project == null)
        {
            _project = new emxProjectSpace_mxJPO(projectId);
        }
        else
        {
            _project.setId(projectId);
        }
        return _project.getAccessListObject(context);
    }

    /**
     * When a Member relationship is created, grant the new member the proper
     * permissions to the "Project Access List" Object.
     * Note: RELID is not needed for this trigger.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the from object id
     *        1 - String containing the to object name
     * @throws Exception if operation fails
     */
    public void triggerCreateAction(Context context, String[] args) throws Exception {
    	DebugUtil.debug("Entering MemberRelationship triggerCreateAction");
        
        String fromObjectId = args[0]; // project space ID
        String personName = args[1];
        
        StringList selects = new StringList(3);
        selects.add(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
        selects.add(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
        selects.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
        
        DomainObject domParentObj = DomainObject.newInstance(context,fromObjectId);
        Map projectInfo = domParentObj.getInfo(context, selects);
        
        String isKindOfProjectSpace = (String)projectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
        String isKindOfProjectConcept =(String)projectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
        String isKindOfProjectTemplate =(String)projectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
        
        String access = Boolean.valueOf(isKindOfProjectTemplate) ? "Project Lead" : "Project Member";  
        
        if(Boolean.valueOf(isKindOfProjectSpace) || Boolean.valueOf(isKindOfProjectConcept) 
        										 || Boolean.valueOf(isKindOfProjectTemplate)) {
            //Person person = Person.getPerson(context, personName);
            //String personId = person.getId();
            String personId = PersonUtil.getPersonObjectID(context, personName);
            DomainAccess.createObjectOwnership(context, fromObjectId, personId, access, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
        }

        DebugUtil.debug("Exiting MemberRelationship triggerCreateAction");
    }


    /**
     * When a Member relationship is deleted, remove the member from the
     * Project Access List object.
     * Note: No RELID is required here.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the from object id
     *        1 - String containing the to object name
     * @throws Exception if operation fails
     */
    public void triggerDeleteAction(Context context, String[] args) throws Exception {
        DebugUtil.debug("Entering MemberRelationship triggerDeleteAction");

        String fromObjectId = args[0];
        String toName = args[1];

        DomainObject domParentObj = DomainObject.newInstance(context,fromObjectId);
        StringList selects = new StringList(3);
        selects.add(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
        selects.add(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
        selects.add(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);
        
        Map projectInfo = domParentObj.getInfo(context, selects);
        String isKindOfProjectSpace = (String)projectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
        String isKindOfProjectConcept =(String)projectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_CONCEPT);
        String isKindOfProjectTemplate =(String)projectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_TEMPLATE);

        if("true".equalsIgnoreCase(isKindOfProjectSpace) || "true".equalsIgnoreCase(isKindOfProjectConcept)
        												 || "true".equalsIgnoreCase(isKindOfProjectTemplate)) {
            StringList busSelect = new StringList(2);
            busSelect.add(DomainConstants.SELECT_ID);
            busSelect.add("ownership");
        	
        	String objectWhere = "ownership.project == \"" + toName+"_PRJ" + "\"";
          
        	String strRelPattern = ProgramCentralConstants.RELATIONSHIP_PROJECT_VAULTS + "," + ProgramCentralConstants.RELATIONSHIP_SUB_VAULTS;
        	MapList connectedFolderlList= domParentObj.getRelatedObjects(context, 
        			         strRelPattern,
        			         ProgramCentralConstants.TYPE_WORKSPACE_VAULT,
        			         busSelect, 
        			         DomainConstants.EMPTY_STRINGLIST, 
        			         false, 
        			         true, 
        			         (short)0, 
        			         objectWhere, 
        			         DomainConstants.EMPTY_STRING, 
        			         (short)0);

        	int connectedFolderlListSize=connectedFolderlList.size();
        	for(int k=0;k<connectedFolderlListSize;k++)
        	{
        	  Map mapWorkSpaceVault=(Map)connectedFolderlList.get(k);
        	  String strFolderID  =(String) mapWorkSpaceVault.get(DomainConstants.SELECT_ID);
          	  DomainAccess.deleteObjectOwnership(context, strFolderID, null, toName+"_PRJ",DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
        		
        	}

           // DomainAccess.deleteObjectOwnership(context, fromObjectId, null, toName+"_PRJ", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
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
     * @param args holds the following input arguments:
     *        0 - String containing the from object id
     *        1 - String containing the from to object name
     *        2 - String containing the attribute name
     *        3 - String containing the attribute value
     *        4 - String containing the new attribute value
     * @throws Exception if operation fails
     */
    public void triggerModifyAttributeAction(Context context, String[] args)
        throws Exception
    {
        DebugUtil.debug("Entering MemberRelationship triggerModifyAttributeAction");
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

            DebugUtil.debug("Exiting triggerModifyAttributeAction_new - not Project Access attribute");
        }
        else
        {
            DomainObject domParentObj = DomainObject.newInstance(context,fromObjectId);
            if(domParentObj.isKindOf(context,DomainConstants.TYPE_PROJECT_SPACE)
            		|| domParentObj.isKindOf(context,DomainConstants.TYPE_PROJECT_CONCEPT))
            	
            {
                Person person = Person.getPerson(context, toName);
                String personId = person.getId();

                if (RANGE_PROJECT_LEAD.equals(newAttrValue)) // user is a lead
                {
                    DomainAccess.createObjectOwnership(context, fromObjectId, personId, "Project Lead", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                }
                else if (RANGE_PROJECT_ASSESSOR.equals(newAttrValue)) // user is a project assessor
                {
                    // do nothing for now
                    // DomainAccess.createObjectOwnership(context, fromObjectId, personId, "Project Assessor", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                }
                else if (RANGE_FINANCIAL_REVIEWER.equals(newAttrValue)) // user is a financial reviewer
                {
                    // do nothing for now
                    // DomainAccess.createObjectOwnership(context, fromObjectId, personId, "Financial Reviewer", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                }
                else if (RANGE_PROJECT_OWNER.equals(newAttrValue)) // user is an owner
                {
                    DomainAccess.createObjectOwnership(context, fromObjectId, personId, "Project Lead", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                }
                else // user is an project member
                {
                    DomainAccess.createObjectOwnership(context, fromObjectId, personId, "Project Member", DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
                }
            }
        }

        DebugUtil.debug("Exiting MemberRelationship triggerModifyAttributeAction");
    }

}
