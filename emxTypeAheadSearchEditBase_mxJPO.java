/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.Iterator;
import java.util.Map;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.OrganizationUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

/**
 * Provides type ahead search all columns in the Parts/Specifications Search Edit pages.
 *  
 * @author Syam rao
 * @since AEF 10.6.SP2
 */
public class emxTypeAheadSearchEditBase_mxJPO extends emxTypeAhead_mxJPO
{ 
	/**
	 *
	 */
	public emxTypeAheadSearchEditBase_mxJPO ()
	{
		//Constructor
	} 

	/**
	 *
	 */
	public emxTypeAheadSearchEditBase_mxJPO (Context context, String[] args) throws Exception 
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
            throw new Exception("You must specify a method on emxTypeAheadSearchEdit invocation.");
        }
        
        return (0);
	} 

	/**
	 * Generic search filtered by name and typePattren
	 *
	 * @param context the matrix user context
	 * @param filter the name filter
	 * @param typePattern the Type Pattern filter
	 * @return MapList of Type Pattern objects
	 * @since AEF 10.6.SP2
	 */
	private MapList search(Context context, String filter,String typePattern)
		throws Exception
	{  		 
   		filter += "*";
  		String personActiveState = PropertyUtil.getSchemaProperty(context, "policy", DomainConstants.POLICY_PERSON, "state_Active");
    	String whereClause = "(" + "name" + " ~= " + "\"" + filter + "\")";
        
        if(typePattern.indexOf("Person") >=0)
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
		return (objects);
	}
    
	/**
	 * Generic search for Vaults, filtered by name 
	 *
	 * @param context the matrix user context
     * @param args one argument, the filter value
     * @return xml representing the values 
	 * @since AEF 10.6.SP2
	 */
    public String getVaults(Context context, String[] args) throws Exception
    {
//        String filter = (args != null) ? (String) JPO.unpackArgs(args) : "*";       
        MapList vaultsList = OrganizationUtil.getAllVaultsDetails(context, true);
        
        // generate the xml     
        Map map;
        String name;
        Iterator itr = vaultsList.iterator();
        
        while (itr.hasNext())
        {
            map = (Map) itr.next();
            name = (String) map.get(DomainConstants.SELECT_NAME);
            addValue(name, name);
        }
    
       return (toXML());       
    }

	 /**
     * Get the Active Persons.
     *
     * @param context the matrix user context
     * @param args one argument, the filter value
     * @return xml representing the values 
     * @since AEF 10.6.SP2
     */
    public String getActivePersons(Context context, String[] args)
        throws Exception
    {
        String filter = (args != null) ? (String) JPO.unpackArgs(args) : "*";
        MapList rdos = search(context, filter,PropertyUtil.getSchemaProperty(context,"type_Person"));
        
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
     * Verifies if the object exist by the id passed if not, returns the object id of the passed name.
     *
     * @param context the matrix user context
     * @param args one argument, the filter value
     * @return objectId as string
     * @since AEF 10.6.SP2
     */
    public String isObject(Context context, String[] args)
        throws Exception
    {
        String objName = args[0];
        String retId=objName;
        BusinessObject newObj = new BusinessObject(objName);
        boolean exists=newObj.exists(context);
        if(!exists){
            StringList objectSelects= new StringList();
            objectSelects.addElement("id");
            MapList mapval=DomainObject.findObjects( context,
                    null, 
                    objName,
                    null,
                    null,
                    null,
                    null,
                    false,
                    objectSelects);
            if(mapval != null && !mapval.equals("") && !mapval.isEmpty())
            {
            retId=(String)((Map)mapval.get(0)).get("id");            
        }
            else 
            	retId = "" ;
        }
        return retId;    
    }
}
