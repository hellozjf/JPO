
/**
 * emxTypeAheadPersonSearch.java
 *
 * Copyright (c) 2006-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of MatrixOne,
 * Inc.  Copyright notice is precautionary only
 * and does not evidence any actual or intended publication of such program
 */

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.JPO;
import matrix.db.Role;
import matrix.db.UserItr;

import matrix.util.SelectList;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

import java.util.Map;
import java.util.Iterator;

/**
 * Provides type ahead search for Persons.
 *  
 * @author Mike Keirstead
 * @since AEF 10.6.SP2
 */
public class emxTypeAheadPersonSearch_mxJPO extends emxTypeAhead_mxJPO
{ 
	/**
	 *
	 */
	public emxTypeAheadPersonSearch_mxJPO ()
	{ 
	} 

	/**
	 *
	 */
	public emxTypeAheadPersonSearch_mxJPO (Context context, String[] args) throws Exception 
	{ 
		super(context, args);
	} 

	/**
	 *
	 */
	public int mxMain(Context context, String []args) throws Exception 
	{ 
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.TypeAheadPersonSearch.SpecifyMethod", context.getLocale().getLanguage()));
        }
        
        return (0);
	} 

	/**
	 * Get the ECR Evaluators matching the filter.
	 *
	 * @param context the matrix user context
	 * @param args one argument, the filter value
	 * @return xml representing the values 
	 * @since AEF 10.6.SP2
	 */
	public String getECREvaluators(Context context, String[] args)
		throws Exception
	{
		String filter = (args != null) ? (String) JPO.unpackArgs(args) : "*";
		MapList evaluators = search(context, filter, "role_ECREvaluator");
		
		// generate the xml		
        Map map;
    	String name;
    	Iterator itr = evaluators.iterator();
        
        while (itr.hasNext())
        {
			map = (Map) itr.next();
       		name = (String) map.get("name");
       		addValue(name, name);
  		}
	
		return (toXML());
	}
	
	/**
	 * Get the Responsible Design Engineers matching the filter.
	 *
	 * @param context the matrix user context
	 * @param args one argument, the filter value
	 * @return xml representing the values 
	 * @since AEF 10.6.SP2
	 */
	public String getResponsibleDesignEngineers(Context context, String[] args)
		throws Exception
	{
		String filter = (args != null) ? (String) JPO.unpackArgs(args) : "*";
		MapList rdos = search(context, filter, "role_SeniorDesignEngineer");
		
		// generate the xml		
        Map map;
    	String name;
    	Iterator itr = rdos.iterator();
        
        while (itr.hasNext())
        {
			map = (Map) itr.next();
       		name = (String) map.get("name");
       		addValue(name, name);
  		}
	
		return (toXML());
	}
	
	/**
	 * Generic search for person, filtered by name and role,
	 *
	 * @param context the matrix user context
	 * @param filter the name filter
	 * @param role the role filter
	 * @return MapList of persons
	 * @since AEF 10.6.SP2
	 */
	private MapList search(Context context, String filter, String role)
		throws Exception
	{
		MapList results = new MapList();
  		String typePattern = PropertyUtil.getSchemaProperty(context, "type_Person");
		role = PropertyUtil.getSchemaProperty(context, role);
		
  		// adjust the filter
  		filter += "*";

  		String personActiveState = PropertyUtil.getSchemaProperty(context, "policy", DomainConstants.POLICY_PERSON, "state_Active");

    	String whereClause = "(" + "name" + " ~= " + "\"" + filter + "\")";
    	whereClause += " && " + "(" + "current" + " ~= " + "\"" + personActiveState + "\")";
		    
		SelectList selects = new SelectList(1);
    	selects.addElement("name");

		MapList objects = DomainObject.findObjects(context,
                                                   typePattern,
                                                   null,
                                                   null,
                                                   null,
                                                   null,
                                                   whereClause,
                                                   null,
                                                   true,
                                                   selects,
                                                   (short) 0);

      	// filter by role
      	if (role.length() > 0)
      	{
	        Role matrixRole = new Role(role);
	        matrixRole.open(context);
	        UserItr userItr = new UserItr(matrixRole.getAssignments(context));
	        matrixRole.close(context);
			
	        StringList assignments = new StringList();
	        while(userItr.next())
	        {
          		assignments.addElement(userItr.obj().getName());
        	}

        	Iterator itr = objects.iterator();
	        Map map;
	        
	        while (itr.hasNext())
	        {
				map = (Map) itr.next();
          		if (assignments.contains((String) map.get("name")))
          		{
               		results.add(map);
            	}
      		}
		}
			
		return (results);
	}
    
    /**
     * Get the Approvers for adding new Inbox Tasks over Lifecycle states.
     * The names of Persons, Roles and Groups will be searched.
     *
     * @param context the matrix user context
     * @param args one argument, the filter value
     * @return xml representing the values 
     * @since AEF V6R2008-2.0LA
     */
    public String getApproversIncludePersonsRolesGroups(Context context, String[] args)
        throws Exception
    {
        String strFilter = (args != null) ? ((String) JPO.unpackArgs(args) + "*") : "*";
        //
        // Search Persons
        //
        final String POLICY_PERSON_STATE_ACTIVE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_PERSON, "state_Active");
        MapList mlPersons = DomainObject.findObjects(context,
                                 DomainObject.TYPE_PERSON, 
                                 "*", 
                                 "name ~~ const \"" + strFilter + "\" && current ~~ const \"" + POLICY_PERSON_STATE_ACTIVE + "\"", 
                                 new StringList(DomainObject.SELECT_NAME));
        // Add these persons into final result
        Map mapPersonInfo = null;
        String strName = null;
        for (Iterator itrPersons = mlPersons.iterator(); itrPersons.hasNext();) {
            mapPersonInfo = (Map) itrPersons.next();
            strName = (String)mapPersonInfo.get(DomainObject.SELECT_NAME);
            
            addValue(strName, "Person");
        }
        
        //
        // Search Role
        //
        String strResult = MqlUtil.mqlCommand(context, "list role '" + strFilter + "'", true);
        StringList slResult = FrameworkUtil.split(strResult, "\n");
        for (StringItr stringItr = new StringItr(slResult); stringItr.next();) {
            strName = stringItr.obj();
            
            addValue(strName, "Role");
        }
        
        //
        // Search Group
        //
        strResult = MqlUtil.mqlCommand(context, "list group '" + strFilter + "'", true);
        slResult = FrameworkUtil.split(strResult, "\n");
        for (StringItr stringItr = new StringItr(slResult); stringItr.next();) {
            strName = stringItr.obj();
            
            addValue(strName, "Group");
        }
        return (toXML());
    }
}


