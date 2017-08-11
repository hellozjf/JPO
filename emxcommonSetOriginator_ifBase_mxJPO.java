/*
 **   emxcommonSetOriginator_ifBase
 **
 **   Copyright (c) 1992-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 */


import matrix.db.BusinessObject;
import matrix.db.Context;
import java.util.ArrayList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

/**
 * The <code>emxcommonSetOriginator_ifBase</code> class contains Action program to set Originator attribute.
 *
 * @version AEF 10.0.1.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxcommonSetOriginator_ifBase_mxJPO
{

	/**
	 * Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since EC 10.0.0.0
	 */

	public emxcommonSetOriginator_ifBase_mxJPO (Context context, String[] args)
	throws Exception
	{
		// if (!context.isConnected())
		// throw new Exception("not supported no desktop client");
	}

	/**
	 * This is an initialization procedure used to set the Originator
	 * attribute value to the name of the current user and
	 * This procedure is designed to be called from the check manager via a creation check
	 * The actual Originator attribute name must be looked up as the customer
	 * could change it to a different string
	 *
	 * Because creation is often done as the Shadow Agent,
	 * the environment variable APPREALUSER is checked
	 * If set, it is used instead of USER for setting the originator.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args hold the following input arguments:
	 *    0 - String containing Property name of attribute to be set.
	 * @return  int 0, status code
	 * @throws Exception if the operation fails
	 * @since AEF 10.0.1.0
	 */

	public int mxMain(Context context, String []args)
	throws Exception
	{
		// Create an instant of emxUtil JPO
		emxUtil_mxJPO utilityClass = new emxUtil_mxJPO(context, null);

		// Get admin names that are used from property names.
		String arguments[] = new String[1];
		arguments[0]  = args[0];
		//        ArrayList adminNames = utilityClass.getAdminNameFromProperties(context, arguments);

		//Get information from the cache
		String attOriginator = PropertyUtil.getSchemaProperty(context, args[0]);

		// Get Required Environment Variables
		arguments = new String[8];
		arguments[0] = "get env EVENT";
		arguments[1] = "get env USER";
		arguments[2] = "get env APPREALUSER";
		arguments[3] = "get env OBJECTID";
		ArrayList cmdResults = utilityClass.executeMQLCommands(context, arguments);
		String sEvent = (String)cmdResults.get(0);
		String sUser = (String)cmdResults.get(1);
		String sAppRealUser = (String)cmdResults.get(2);
		String sObjectId = (String)cmdResults.get(3);

		// Check if shadow agent is used
		// If yes then use real user name
		if (sAppRealUser.length() != 0)
		{
			sUser = sAppRealUser;
		}

		// modify specified attribute to populate originator.
		// use mql command class to modify bus as
		// there is no constructor of businessobject with TNR only.
		// Note : There is a possibility of new revision to be in different vault.
		arguments = new String[1];
		StringBuffer argBuffer = new StringBuffer(100);
		if ((!UIUtil.isNullOrEmpty(sObjectId)) && (sEvent.compareTo("Revision") == 0 || sEvent.compareTo("MinorRevision") == 0))
		{
			//get the object id of newly revised object
			String mqlCommand = "print bus $1 select $2 dump;";			
			String mqlResult = MqlUtil.mqlCommand(context, mqlCommand, sObjectId, "last.id");
			sObjectId = mqlResult;
		}
		else if(!UIUtil.isNullOrEmpty(sObjectId) && sEvent.compareTo("MajorRevision") == 0)
		{
			String mqlCommand = "print bus $1 select $2 dump;";
			String mqlResult = MqlUtil.mqlCommand(context, mqlCommand, sObjectId, "majorid.nextmajorid.bestsofar.id");
			sObjectId = mqlResult;
		}
		argBuffer.append("modify bus ");
		argBuffer.append(sObjectId);
		argBuffer.append(" \"");
		argBuffer.append(attOriginator);
		argBuffer.append("\" \"");
		argBuffer.append(sUser);
		argBuffer.append("\"");
		arguments[0] = argBuffer.toString();

		utilityClass.executeMQLCommands(context, arguments);
		return 0;
	}

}
