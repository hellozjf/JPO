
/**
 * emxTypeAheadVaultSearch.java
 *
 * Copyright (c) 2006-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of MatrixOne,
 * Inc.  Copyright notice is precautionary only
 * and does not evidence any actual or intended publication of such program
 */

import matrix.db.Context;
import matrix.db.JPO;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.OrganizationUtil;
import com.matrixone.apps.domain.util.i18nNow;

import java.util.Map;
import java.util.Iterator;

/**
 * Provides type ahead search for Vaults.
 *  
 * @author Mike Keirstead
 * @since AEF 10.6.SP2
 */
public class emxTypeAheadVaultSearch_mxJPO extends emxTypeAhead_mxJPO
{ 
	/**
	 *
	 */
	public emxTypeAheadVaultSearch_mxJPO ()
	{ 
	} 

	/**
	 *
	 */
	public emxTypeAheadVaultSearch_mxJPO (Context context, String[] args) throws Exception 
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
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.TypeAheadVaultSearch.SpecifyMethodOnTypeAheadVaultSearchInvocation", context.getLocale().getLanguage()));
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
	public String getVaultsIncludeCollaborationPartners(Context context, String[] args)
		throws Exception
	{
		// ignore the filter and return the entire list
		
		// include collaboration partners
		MapList vaults = translate(OrganizationUtil.getAllVaultsDetails(context, true));
		
		// generate the xml		
        Map map;
    	Iterator itr = vaults.iterator();
        
        while (itr.hasNext())
        {
			map = (Map) itr.next();
       		addValue((String) map.get("display"),
       				 (String) map.get(DomainConstants.SELECT_NAME));
  		}
	
		setAllDataSentAttribute(true);
		return (toXML());
	}
	
	/**
	 * Translate the vault names given the language passed
	 * with the constructor.
	 *
	 * @param vaults the list of vault maps
	 * @return the same list with translated value in the map
	 * @since AEF 10.6.SP2
	 */
	private MapList translate(MapList vaults)
		throws Exception
	{
      	//build translated vault names string
      	Iterator itr = vaults.iterator();
  		Map map;
      	String vault;
      	
      	while (itr.hasNext())
      	{
          	map = (Map) itr.next();
          	vault = (String) map.get(DomainConstants.SELECT_NAME);
          	map.put("display", i18nNow.getAdminI18NString("Vault", vault, getLanguage()));
      	}
	
		return (vaults);
	}
}


