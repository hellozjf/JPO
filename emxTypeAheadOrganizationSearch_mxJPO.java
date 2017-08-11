
/**
 * emxTypeAheadOrganizationSearch.java
 *
 * Copyright (c) 2006-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of MatrixOne,
 * Inc.  Copyright notice is precautionary only
 * and does not evidence any actual or intended publication of such program
 */

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.SelectList;

import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.OrganizationUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;

import java.util.Map;
import java.util.Iterator;

/**
 * Provides type ahead search for Organizations.
 *  
 * @author Mike Keirstead
 * @since AEF 10.6.SP2
 */
public class emxTypeAheadOrganizationSearch_mxJPO extends emxTypeAhead_mxJPO
{ 
	/**
	 *
	 */
	public emxTypeAheadOrganizationSearch_mxJPO ()
	{ 
	} 

	/**
	 *
	 */
	public emxTypeAheadOrganizationSearch_mxJPO (Context context, String[] args) throws Exception 
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
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.TypeAheadOrganizationSearch.SpecifyMethodOnTypeAheadOrganizationSearchInvocation", context.getLocale().getLanguage()));
        }
        
        return (0);
	} 

	/**
	 * Get the vaults including those from our collaboration
	 * partners matching the filter.
	 *
	 * @param context the matrix user context
	 * @param args one argument, the filter value
	 * @return xml representing the values 
	 * @since AEF 10.6.SP2
	 */
	public String getResponsibleDesignOrganizations(Context context, String[] args)
		throws Exception
	{
		// ignore the filter and return the entire list
		
		MapList organizations = search(context, "");
		
		// generate the xml		
        Map map;
    	Iterator itr = organizations.iterator();
        
        while (itr.hasNext())
        {
			map = (Map) itr.next();
       		addValue((String) map.get(DomainConstants.SELECT_NAME),
					 (String) map.get(DomainObject.SELECT_ID));
  		}
	
		setAllDataSentAttribute(true);
		return (toXML());
	}
	
	/**
	 * Generic search for organization, filtered by name.
	 *
	 * @param context the matrix user context
	 * @param filter the name filter
	 * @return MapList of organizations
	 * @since AEF 10.6.SP2
	 */
	private MapList search(Context context, String filter)
		throws Exception
	{
    	SelectList selects = new SelectList(2);
    	selects.add(DomainObject.SELECT_ID);
    	selects.add(DomainObject.SELECT_NAME);

    	com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
    	String vaultPattern = person.getCompany(context).getAllVaults(context, true);
		String typePattern = PropertyUtil.getSchemaProperty(context, "type_Organization");
		
        return (DomainObject.findObjects(context,
        								 typePattern,
                                         "*",
                                         "*",
                                         "*",
                                         vaultPattern,
                                         null,
                                         null,
                                         true,
                                         selects,
                                         (short) 0));
    
    }
}


