/*
** emxInstallUtilBase
**
**  Copyright (c) 1992-2015 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import java.util.TreeMap;
import java.util.StringTokenizer;
import java.io.PrintWriter;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.MatrixWriter;

import com.matrixone.jsystem.util.StringUtils;
import com.matrixone.apps.domain.util.MqlUtil;

/**
 * The <code>emxInstallUtilBase</code> class contains utility methods.
 *
 * @version AEF 10.Next - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxInstallUtilBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args[0] holds schema definition file name with full path
     * @throws Exception if the operation fails
     * @since AEF 10.Next
     */
    public emxInstallUtilBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * Executes MQL command
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param cmd                 mql command
     * @throws Exception          if operation fails
     * @since                     AEF 10.Next
     */
    public static String executeMQLCommand(Context context, String cmd) throws Exception
    {
        MQLCommand mql = new MQLCommand();
        mql.open(context);

        // throw exception if error
        if (!mql.executeCommand(context, cmd))
        {
            mql.close(context);
            throw (new Exception(mql.getError().trim()));
        }

        // return result string
        String sResult = mql.getResult().trim();
        mql.close(context);

        return sResult;
    }

    /**
     * Gets child object. If it doesn't exists then create one
     *
     * @param parent              parent TreeMap
     * @param key                 key
     * @param valueClass          class of value object
     * @since                     AEF 10.Next
     */
    public static Object getKeyValue(TreeMap parent, Object key, Class valueClass) throws Exception
    {
        Object child = null;
        if (!parent.containsKey(key))
        {
            child = valueClass.newInstance();
            parent.put(key, child);
        }
        else
        {
            child = parent.get(key);
        }

        return child;
    }


    /**
     * Find and Replace the String in a string
     *
     * @param sourceString   the string of the source.
     * @param findString     the string to be replaced with
     * @param replaceString  the string to be searched for
     * @return               StringList the list of registered suites for the user.
     * @since                AEF 9.5.5.0
     */
    public static String findAndReplace(
        String sourceString,
        String findString,
        String replaceString) {

        return StringUtils.replaceAll(sourceString, findString, replaceString);
    }

    /**
     * Find and Replace the String in a string
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sLine               line to be printed
     * @throws Exception          if operation fails
     * @since                     AEF 10.Next
     */
    public static void println(Context context,String sLine)
        throws Exception
    {
        PrintWriter writer = new PrintWriter(new MatrixWriter(context), true);
        writer.println(sLine);
        writer.close();
    }

    /**
     * Rename admin object
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param args[0]             symbolic name of admin object
     * @throws Exception          if operation fails
     * @since                     AEF 10.Next
     */
    public static void toggleExpressionValue(Context context, String[] args)
        throws Exception
    {
        // throw exception if invalid arguments passed
        if (args == null || args.length < 1)
        {
            throw (new Exception("Error:toggleExpressionValue:Invalid argument"));
        }

        // Get admin name
        String sName = emxAdminCache_mxJPO.getName(context, args[0]);
        if (sName == null || sName.length() == 0)
        {
            throw (new Exception("Error:toggleExpressionValue:Expression " + args[0] + " not found"));
        }

        // Get expression value
        String sCmd = "print expression \"" + sName + "\" select value dump";
        String sValue = executeMQLCommand(context, sCmd);
        // Toggle expression value
        String sNewValue = "true";
        if (sValue.equalsIgnoreCase("true")) {
            sNewValue = "false";
        }
        sCmd = "modify expression \"" + sName + "\" value " + sNewValue;
        executeMQLCommand(context, sCmd);
    }

    /**
     * Rename admin object
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param args[0]             symbolic name of admin object
     * @param args[1]             new name
     * @throws Exception          if operation fails
     * @since                     AEF 10.Next
     */
    public static void addAttributesToAdmin(Context context, String[] args)
        throws Exception
    {
        // throw exception if invalid arguments passed
        if (args == null || args.length < 4)
        {
            throw (new Exception("Error:addAttributesToAdmin:Invalid argument"));
        }

        // Get admin name
        String sName = emxAdminCache_mxJPO.getName(context, args[0]);
        if (sName == null || sName.length() == 0)
        {
            throw (new Exception("Error:addAttributesToAdmin: " + args[0] + " not found"));
        }
        // Get query type name
        String sQueryTypeName = emxAdminCache_mxJPO.getName(context, args[1]);

        // get limit
        int limit = 0;
        try {
            limit = Integer.parseInt(args[2]);
        } catch (NumberFormatException ex) {
            throw (new Exception("Error: addAttributesToAdmin: Limit '" +  limit + "' must be integer"));
        }

        int count = 0;
        if (sQueryTypeName != null && sQueryTypeName.length() != 0) {
            String sResult = executeMQLCommand(context, "temp query bus \"" + sQueryTypeName + "\" * * limit " + limit);
            StringTokenizer st = new StringTokenizer(sResult, "\n");
            count = st.countTokens();
	    }

        String sAdminType = args[0].split("_")[0];
		String sCmd = "modify " + sAdminType + " \"" + sName + "\" ";
		// get attributes to add
		StringTokenizer st1 = new StringTokenizer(args[3], ",");
		while(st1.hasMoreTokens())
		{
			String sAttName = emxAdminCache_mxJPO.getName(context, st1.nextToken());

			sCmd += "add attribute \"" + sAttName + "\" ";
		}
        if (count < limit)
        {
            executeMQLCommand(context, sCmd);
        } else {
			System.out.println("More than " + limit + " " + sQueryTypeName + "s in database");
			System.out.println("Skipping below command. Please execute manually after install");
			System.out.println(sCmd);
		}
    }

    /**
     * Rename admin object
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param args[0]             symbolic name of admin object
     * @param args[1]             new name
     * @throws Exception          if operation fails
     * @since                     AEF 10.Next
     */
    public static void renameAdminObject(Context context, String[] args)
        throws Exception
    {
        // throw exception if invalid arguments passed
        if (args == null || args.length < 2)
        {
            throw (new Exception("Error:renameAdminObject:Invalid argument"));
        }

        // Get old admin name
        String sOldName = emxAdminCache_mxJPO.getName(context, args[0]);
        if (sOldName == null || sOldName.length() == 0)
        {
            throw (new Exception("Error:renameAdminObject:Admin " + args[0] + " not found"));
        }

        // Modify admin name if it is different then new name
        String sAdminType = args[0].substring(0, args[0].indexOf('_'));
        if (!args[1].equals(sOldName))
        {
            writeSchemaLog(context, "Renaming", sAdminType, sOldName, args[1]);
            String sCmd = "modify " + sAdminType + " \"" + sOldName + "\" name \"" + args[1] + "\"";
            executeMQLCommand(context, sCmd);
            emxAdminCache_mxJPO.reloadSymbolicName(context, args[0]);
        }
    }

    /**
     * Add uniquekey admin object
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param args[0]             name of uniquekey admin object
     * @param args[1]             symbolic name of uniquekey admin object
     * @param args[2]             description of uniquekey admin object
     * @param args[3]             symbolic name of business type on which uniquekey needs to be applied
     * @param args[4]             comma seperated string of symbolic name of attributes on which uniquekey needs to be applied
     *                            ex: attribute_AttributeName1,attribute_AttributeName2,attribute_AttributeName3
     *                            Support for interface and field is not present at present
     * @throws Exception          if operation fails
     * @since                     R419
     */
    public static void addUniquekeyAdminType(Context context, String[] args)
        throws Exception
    {
        // throw exception if invalid arguments passed
        if (args == null || args.length < 5)
        {
            throw (new Exception("Error:addUniquekeyAdminType:Invalid argument"));
        }

        // Get data
        String uniquekeyName          = args[0];
        String symbloicUniquekeyName  = args[1];
        String uniquekeyDesc          = args[2];
        String symbloicTypeName       = args[3];
        String symbolicAttributeNames = args[4];

        if (uniquekeyName == null || uniquekeyName.length() == 0)
        {
            throw (new Exception("Error:addUniquekeyAdminType: uniquekey Name " + args[0] + " not passed"));
        }
        if (symbloicUniquekeyName == null || symbloicUniquekeyName.length() == 0)
		{
		    throw (new Exception("Error:addUniquekeyAdminType: Uniquekey symbolic Name " + args[1] + " not passed"));
        }
        if (uniquekeyDesc == null || uniquekeyDesc.length() == 0)
		{
		    throw (new Exception("Error:addUniquekeyAdminType: uniquekey Description " + args[2] + " not passed"));
        }
		if (symbloicTypeName == null || symbloicTypeName.length() == 0)
		{
			throw (new Exception("Error:addUniquekeyAdminType: Type symbloic Name " + args[3] + " not passed"));
        }

		// get the actual type name
		String type = emxAdminCache_mxJPO.getName(context, symbloicTypeName);
		if (type == null || type.length() == 0)
		{
			throw (new Exception("Error:addUniquekeyAdminType: actual name for  " + symbloicTypeName + " not found"));
        }

		if (symbolicAttributeNames == null || symbolicAttributeNames.length() == 0)
		{
			throw (new Exception("Error:addUniquekeyAdminType: attribute name " + args[4] + " not passed"));
        }

        // iterate the comma seperated string of attributes
        String arraySymbolicAttributeNames[] = StringUtils.split(symbolicAttributeNames, ",");
        StringBuffer attributeNamesBuf = new StringBuffer("");

        for (int itr = 0; itr < arraySymbolicAttributeNames.length; itr++)
        {
            String symbolicAttributeName = arraySymbolicAttributeNames[itr];
            // get the actual type name
            String attributeName         = emxAdminCache_mxJPO.getName(context, symbolicAttributeName);
            String enclosedAttrName      = new String();
            if (attributeName != null && attributeName.length() > 0)
            {
                enclosedAttrName  = "\"" + attributeName + "\"";
			}

            if (attributeNamesBuf.length() == 0) {
				attributeNamesBuf.append(enclosedAttrName);
			} else {
				attributeNamesBuf.append(",");
				attributeNamesBuf.append(enclosedAttrName);
			}
        }

		if (attributeNamesBuf.length() == 0)
		{
			throw (new Exception("Error:addUniquekeyAdminType: actual name for  " + symbolicAttributeNames + " not found"));
        }

		String sCmd = "add uniquekey " + uniquekeyName + " type"  + " \"" + type + "\" description \"" + uniquekeyDesc + "\" attribute " + attributeNamesBuf.toString() + " global";
		writeSchemaLog(context, "adding uniquekey ", uniquekeyName, type, sCmd);
		executeMQLCommand(context, sCmd);

		// enable the uniquekey
		sCmd = "enable uniquekey " + uniquekeyName;
		executeMQLCommand(context, sCmd);

		// register the uniquekey
		sCmd = "modify program eServiceSchemaVariableMapping.tcl add property " + symbloicUniquekeyName + " to uniquekey " + uniquekeyName;
		executeMQLCommand(context, sCmd);
    }

    /**
     * reloads Cache for specified admin object
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param args[0]             symbolic name of admin object
     * @throws Exception          if operation fails
     * @since                     AEF 10.Next
     */
    public static void reloadCache(Context context, String[] args)
        throws Exception
    {
        // throw exception if invalid arguments passed
        if (args == null || args.length < 1)
        {
            throw (new Exception("Error:addShowAccess:Invalid argument"));
        }

        // reloads cache
        emxAdminCache_mxJPO.reloadSymbolicName(context, args[0]);
    }

    /**
     * Writes schema log.
     *
     * @param context             the eMatrix <code>Context</code> object
     * @param sAdminType          Type of admin object
     * @param sAdminName          Name of admin object
     * @param sCmdVersion         Version
     * @throws Exception          if operation fails
     * @since                     AEF 10.Next
     */
    public static void writeSchemaLog(Context context,String sOperation,String sAdminType, String sAdminName, String sCmdVersion)
        throws Exception
    {
        // create operation buffer
        StringBuffer sOpBuf = new StringBuffer(">                    :");
        sOpBuf.replace(1, sOperation.length() + 1, sOperation);

        // create admin type buffer
        StringBuffer sAdminTypeBuf = new StringBuffer("             ");
        sAdminTypeBuf.replace(0, sAdminType.length() , sAdminType);

        // create admin name buffer
        StringBuffer sAdminNameBuf = new StringBuffer("                                                      ");
        sAdminNameBuf.replace(0, sAdminName.length(), sAdminName);

        // log a message
        println(context, sOpBuf.toString() + sAdminTypeBuf.toString() + sAdminNameBuf.toString() + sCmdVersion);
    }

    /**
     * Rename unit object in the dimension
     * This method will not copy the default setting value from old unit to new unit
     * @param context             the eMatrix <code>Context</code> object
     * @param args[0]             symbolic name of admin object
     * @param args[1]             old unit names as comma separated
       @param args[2]             new unit names as comma separated
     * @throws Exception          if operation fails
     * @since                     AEF V6R2008-1.0
     */
    public static void renameDimensionUnit(Context context, String[] args)
        throws Exception
    {
        // throw exception if invalid arguments passed
        if (args == null || args.length < 3)
        {
            throw (new Exception("Error:renameDimensionUnit:Invalid argument"));
        }

        // Get old admin name
        String sDimensionName = emxAdminCache_mxJPO.getName(context, args[0]);
        String sOldUnits = args[1];
        String sNewUnits = args[2];

        String arrOld[] = StringUtils.split(sOldUnits,",");
        String arrNew[] = StringUtils.split(sNewUnits,",");

        if (sDimensionName == null || sDimensionName.length() == 0)
        {
            throw (new Exception("Error:renameDimensionUnit:Admin " + args[0] + " not found"));
        }

        if (arrOld.length != arrNew.length)
        {
            throw (new Exception("Error:renameDimensionUnit:Old and New units count is not matching..."));
        }

        // Modify admin name if it is different then new name
        String sAdminType = args[0].substring(0, args[0].indexOf('_'));
        for (int itr = 0; itr < arrOld.length; itr++)
        {
            String sOldUnitName = arrOld[itr];
            String sNewUnitName = arrNew[itr];

            String sCmd = "print dimension \"" + sDimensionName + "\" select unit[" + sOldUnitName + "].* dump |";
            String sResult = executeMQLCommand(context, sCmd);

            if (sResult != null && !"".equals(sResult.trim()))
            {
                String[] sUnitDetails = StringUtils.split(sResult,"[|]");

                String sDes = sUnitDetails[1];
                String sHid = sUnitDetails[2];
                String sMul = sUnitDetails[7];
                String sOff = sUnitDetails[8];
                String sLab = sUnitDetails[9];
                try
                {
                    writeSchemaLog(context, "Removing", sAdminType, sDimensionName, sOldUnitName);
                    sCmd = "modify dimension \"" + sDimensionName + "\" remove unit \"" + sOldUnitName + "\"";
                    executeMQLCommand(context, sCmd);

                    writeSchemaLog(context, "Adding", sAdminType, sDimensionName, sNewUnitName);
                    sCmd = "modify dimension \"" + sDimensionName + "\" add unit \"" + arrNew[itr] + "\" description \"" + sDes + "\"";
                    executeMQLCommand(context, sCmd);
                    sCmd = "modify dimension \"" + sDimensionName + "\" mod unit \"" + arrNew[itr] + "\" multiplier \"" + sMul + "\"";
                    executeMQLCommand(context, sCmd);
                    sCmd = "modify dimension \"" + sDimensionName + "\" mod unit \"" + arrNew[itr] + "\" offset \"" + sOff + "\"";
                    executeMQLCommand(context, sCmd);
                    sCmd = "modify dimension \"" + sDimensionName + "\" mod unit \"" + arrNew[itr] + "\" label \"" + sLab + "\"";
                    executeMQLCommand(context, sCmd);

                    //${CLASS:emxAdminCache}.reloadSymbolicName(context, args[0]);
                }
                catch (Exception ex)
                {
                    //Do nothing
                }
            }
        }
    }



    /**
     * Migration routine for policy Country for state changes if the policy already installed by the accelearators, called by the policy_Country.xml schema file.
     *
     * Rename the state Exists as Active and add Inactive ,Archived states with the access
     *
     * @param context             the eMatrix <code>Context</code> object
     * @throws Exception          if operation fails
     * @since                     AEFR214
     */
    public void migrateCountryPolicySchema(Context context, String[] args)
    throws Exception
    {
        try {
            // return if policy country does not exists
            String sPolicyCountry = emxAdminCache_mxJPO.getName(context, "policy_Country");
            if(sPolicyCountry == null || "".equals(sPolicyCountry.trim()))
                return;

            // return if country policy do not have a Exists state to migrate
            String sExistsState = emxAdminCache_mxJPO.getStateName(context, "policy_Country", "state_Exists");
            if(sExistsState == null || "".equals(sExistsState.trim()))
                return;

            String sInactiveState = emxAdminCache_mxJPO.getStateName(context, "policy_Country", "state_Inactive");

            // Get all states of Country policy
            String cmd = "list policy " + sPolicyCountry + "  select state dump";
            StringBuilder sbQuery = new StringBuilder();

            String sResult = executeMQLCommand(context, cmd);
            if (sResult != null && !"".equals(sResult.trim())){
                String[] countryStates = StringUtils.split(sResult, ",");

                // Rename the Exists state as Active per new naming convention, move the Inactive state as first state
                println(context,"Modifying policy Country to rename existing states...");
                sbQuery.append("modify policy ").append(sPolicyCountry).append(" state ").append(sExistsState).append(" name Active" );
                if(countryStates.length >1)
                {
                    sbQuery.append(" state ").append(sInactiveState).append(" name ").append("Archived");
                }
                executeMQLCommand(context, sbQuery.toString());

                println(context,"Modifying policy Country to add states Inactive,Archived...");
                //Add Inactive and Archived state to policy Country in sequence Inactive -->Active-->Archived
                StringBuilder sb = new StringBuilder();
                sb.append("modify policy ").append(sPolicyCountry).append(" add state ").append("Inactive").append(" before ");
                sb.append("Active");
                if(countryStates.length == 1)
                {
                    sb.append(" add ");
                    sb.append(" state ").append("Archived");
                }

                if(countryStates.length >1)
                {
                    sb.append(" remove property state_Inactive");
                }
                sb.append(" remove property state_Exists");

                sb.append(" add property state_Inactive value Inactive");
                sb.append(" add property state_Active value Active");
                sb.append(" add property state_Archived value Archived");

                executeMQLCommand(context, sb.toString());

                String cmdallstate = "print policy " + sPolicyCountry + " select allstate dump";
                sResult = executeMQLCommand(context, cmdallstate);
                if(sResult == null || !"true".equalsIgnoreCase(sResult)) {
                    println(context,"Enabling All state in Policy Country");
                    StringBuilder sballstate = new StringBuilder();
                    sballstate.append("modify policy ").append(sPolicyCountry).append(" add allstate");
                    executeMQLCommand(context, sballstate.toString());
                }

                println(context,"Policy Country modified successfully....");
            }
        }catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * JPO method to cleanup i.e removing old VPLM roles given to various polcies in R214
     *
     * called from respective policy schema XML file
     *
     * @param context             the eMatrix <code>Context</code> object
     * args will contain symbolic names of policy, state, roles to remove
     * @throws Exception          if operation fails
     * @since                     R215
     */
    public static void removeOldVPLMRoles(Context context, String[] args) throws Exception
    {
        try
        {
             // get the actual policy name
            String symbolicPolicyName = args[0];
            String policy = emxAdminCache_mxJPO.getName(context, symbolicPolicyName    );

            int argsLength = args.length;
            String state = "";

            if(argsLength >  1)
            {
                for( int i = 1; i < argsLength; i++)
                {
                    String token =   args[i];
                    if (token.startsWith("state_"))
                    {
                        state = emxAdminCache_mxJPO.getStateName(context, symbolicPolicyName, token);
                    }
                    else
                    {
                        String user = emxAdminCache_mxJPO.getName(context, token);
                        MqlUtil.mqlCommand(context, "modify policy '"+ policy +"' state '"+ state + "' remove user '" + user + "' all single project single org");
                    }
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }
    /**
     * JPO method to add allstate to a policy, if it doesn't exist
     *
     * called from respective policy schema XML file
     *
     * @param context             the eMatrix <code>Context</code> object
     * args will contain symbolic names of policy
     * @throws Exception          if operation fails
     * @since                     R215
     */
    public static void addAllStateToPolicy(Context context, String[] args) throws Exception
    {
        try
        {
             // get the actual policy name
            String symbolicPolicyName = args[0];
            String policy = emxAdminCache_mxJPO.getName(context, symbolicPolicyName    );

            String hasAllState = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump", false, policy, "allstate");

            if(!"TRUE".equalsIgnoreCase(hasAllState)){
                MqlUtil.mqlCommand(context, "modify policy $1 add allstate owner none public none", false, policy);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    public static int ACTION_SKIP = 0x0;
    public static int ACTION_EXECUTE = 0x1;
    public static int ACTION_GENERATE_TCL = 0x10;
    public static String CLOUD_FLAG = "DS_IN_CLOUD";
    public static String LIVE_UPGRADE_FLAG = "liveupgrade";
    private static int liveUpgradeFlag = -1;
    public static int getActionForUnsafeChanges(Context context) throws Exception
    {
       if (liveUpgradeFlag < 0) {
	       if(!"YES".equalsIgnoreCase(System.getenv(CLOUD_FLAG))) { //on-premise
	    	   liveUpgradeFlag = ACTION_EXECUTE;
	       } else if(!context.isTenant()) {//global tenant
	    	   liveUpgradeFlag = ACTION_EXECUTE | ACTION_GENERATE_TCL;
	       } else if("YES".equalsIgnoreCase(System.getenv(LIVE_UPGRADE_FLAG))) { //live upgrade
	    	   liveUpgradeFlag = ACTION_SKIP;
	       } else {
	    	   liveUpgradeFlag = ACTION_EXECUTE; //live upgrade disabled
	       }
       }

       return liveUpgradeFlag;
    }

    public static void renameReviewAndAddInApproval(Context context, String[] args) throws Exception
    {
        try
        {
        	// get the actual policy name
            String symbolicPolicyName = args[0];
            String policy = emxAdminCache_mxJPO.getName(context, symbolicPolicyName    );
            String strReviewState = args[1];
            String strInWorkState = args[2];
            String strInApprovalState = args[3];
            String strApprovedState = args[4];
            //get list of state names from policy
            String strStateNames = MqlUtil.mqlCommand(context, "print policy $1 select state dump $2", true, policy,"\\|");
            String str;
            //check if 'Review' state exists
            if(strStateNames.contains(strReviewState)){
            	//rename it to 'In Work'
            	str = MqlUtil.mqlCommand(context, "modify policy $1 state $2 name $3", true, policy,strReviewState,strInWorkState);
            	//remove property for old state name
            	str = MqlUtil.mqlCommand(context, "mod policy $1 remove property $2", true, policy,"state_"+(strReviewState.replace(" ", "")));
            	//add property for new state name
            	str = MqlUtil.mqlCommand(context, "mod policy $1 add property $2 value $3", true, policy,"state_"+(strInApprovalState.replace(" ", "")),strInApprovalState);
            }
            //check if 'In Approval' state exists
            //if no, add it to the policy & add property for its symbolic name
            if(! strStateNames.contains(strInApprovalState)){
            	str = MqlUtil.mqlCommand(context, getMQLForInApprovalState(), true, policy, strInApprovalState, strApprovedState);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }
	private static String getMQLForInApprovalState() throws Exception {
    	StringBuilder sbMQL = new StringBuilder();
    	sbMQL.append("modify policy $1 ");
    	sbMQL.append("add state $2 before $3 ");
    	sbMQL.append("promote true ");
    	return sbMQL.toString();
    }

	public static void renameEFormType(Context context, String[] args) throws Exception
    {
        try
        {
        	// get the actual policy name
            String symbolicTypeName = args[0];
            String type = emxAdminCache_mxJPO.getName(context, symbolicTypeName);
            
            if(type.equals("eForm")){
            	String str = MqlUtil.mqlCommand(context, "modify type $1 name $2 property $3 value $4", true,"eForm","Impact Questionnaire","original name","Impact Questionnaire");
				str = MqlUtil.mqlCommand(context, "add property $1 on program $2 to type $3",true,"type_eForm","eServiceSchemaVariableMapping.tcl","Impact Questionnaire");
				emxAdminCache_mxJPO.putName(context, "type_eForm", "Impact Questionnaire");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }
	public static void renameEFormPolicy(Context context, String[] args) throws Exception
    {
        try
        {
        	// get the actual policy name
            String symbolicPolicyName = args[0];
            String policy = emxAdminCache_mxJPO.getName(context, symbolicPolicyName);

			if(symbolicPolicyName.equals("policy_eForm"))
			{
				if(! policy.equals("Impact Questionnaire"))
				{
					String str = MqlUtil.mqlCommand(context, "modify policy $1 name $2 property $3 value $4", true,"eForm","Impact Questionnaire","original name","Impact Questionnaire");
					str = MqlUtil.mqlCommand(context, "add property $1 on program $2 to policy $3",true,"policy_eForm","eServiceSchemaVariableMapping.tcl","Impact Questionnaire");
					emxAdminCache_mxJPO.putName(context, "policy_eForm", "Impact Questionnaire");
				}
			}
			else if(symbolicPolicyName.equals("policy_eFormTemplate"))
			{
				if(! policy.equals("Impact Questionnaire Template"))
				{
					String str = MqlUtil.mqlCommand(context, "modify policy $1 name $2 property $3 value $4", true,"eForm Template","Impact Questionnaire Template","original name","Impact Questionnaire Template");
					str = MqlUtil.mqlCommand(context, "add property $1 on program $2 to policy $3",true,"policy_eFormTemplate","eServiceSchemaVariableMapping.tcl","Impact Questionnaire Template");
					emxAdminCache_mxJPO.putName(context, "policy_eFormTemplate", "Impact Questionnaire Template");
				}
			}
        }
		
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }
	public static void addInWorkStateInEFormPolicy(Context context, String[] args) throws Exception
    {
        try
        {
			String symbolicPolicyName = args[0];
            String policy = emxAdminCache_mxJPO.getName(context, symbolicPolicyName);
			
			String strStateNames = MqlUtil.mqlCommand(context, "print policy $1 select state dump $2",true, policy, "\\|");
            String str;

            if(!strStateNames.contains("In Work")){
				MqlUtil.mqlCommand(context, "modify policy $1 add state $2 before $3", true,policy, "In Work", "Complete");
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
            throw ex;
		}
	}
}
