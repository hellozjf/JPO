/*
**   emxTriggerManagerBase
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.util.*;

import com.matrixone.apps.cache.CacheManager;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;

/**
 * The <code>emxTriggerManagerBase</code> jpo contains policy utility methods.
 *
 * @version EC 10.0.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxTriggerManagerBase_mxJPO extends emxUtil_mxJPO
{

    /** Create Object for synchronization. */
    static final protected Object _lock = new Object();
    /** programMap holds treemap values. */
//    static final protected Hashtable programMap = new Hashtable();
    /** isJavaMap holds program name values. */
//    static final protected Hashtable isJavaMap = new Hashtable();
    /** isJavaMap holds state name values. */
    static final protected Hashtable stateMap = new Hashtable();
    /** sl is a StringList holds attribute values. */
    static protected StringList sl = null;
    /** stateActive,vaultPattern are String holds state and pattern values. */
    static protected String stateActive, vaultPattern;
    /** triggerType, progName, sequenceNo, methodName, adminVault, constructorParam, targetStates are Strings holds values. */
    static protected String triggerType, progName, sequenceNo, methodName, adminVault, constructorParam, targetStates, errorType;
    /** _componentAge is a long value. */
    static long _componentAge = 3600 * 1000;
    /** envVarsStack is a Stack holds environment values. */
    protected Stack envVarsStack = new Stack();
    /** _restoreRPE is a boolean value. */
    static boolean _restoreRPE = true;

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */

    public emxTriggerManagerBase_mxJPO (Context context, String[] args) throws Exception
    {
        super(context, args);

        synchronized(_lock) {
            if (sl == null) {
                // Get admin names that are used in this program from property names.
                String[] s = new String[23];
                s[0]  = "type_eServiceTriggerProgramParameters";
                s[1]  = "attribute_eServiceProgramName";
                s[2]  = "attribute_eServiceSequenceNumber";
                s[3]  = "attribute_eServiceProgramArgument1";
                s[4]  = "attribute_eServiceProgramArgument2";
                s[5]  = "attribute_eServiceProgramArgument3";
                s[6]  = "attribute_eServiceProgramArgument4";
                s[7]  = "attribute_eServiceProgramArgument5";
                s[8]  = "attribute_eServiceProgramArgument6";
                s[9]  = "attribute_eServiceProgramArgument7";
                s[10] = "attribute_eServiceProgramArgument8";
                s[11] = "attribute_eServiceProgramArgument9";
                s[12] = "attribute_eServiceProgramArgument10";
                s[13] = "attribute_eServiceProgramArgument11";
                s[14] = "attribute_eServiceProgramArgument12";
                s[15] = "attribute_eServiceProgramArgument13";
                s[16] = "attribute_eServiceProgramArgument14";
                s[17] = "attribute_eServiceProgramArgument15";
                s[18] = "vault_eServiceAdministration";
                s[19] = "attribute_eServiceMethodName";
                s[20] = "attribute_eServiceConstructorArguments";
                s[21] = "attribute_eServiceTargetStates";
                s[22] = "attribute_eServiceErrorType";
                ArrayList adminNames = getAdminNameFromProperties(context, s);

                triggerType = (String)adminNames.get(0);
                progName = (String)adminNames.get(1);
                sequenceNo = (String)adminNames.get(2);
                String progArg1 = (String)adminNames.get(3);
                String progArg2 = (String)adminNames.get(4);
                String progArg3 = (String)adminNames.get(5);
                String progArg4 = (String)adminNames.get(6);
                String progArg5 = (String)adminNames.get(7);
                String progArg6 = (String)adminNames.get(8);
                String progArg7 = (String)adminNames.get(9);
                String progArg8 = (String)adminNames.get(10);
                String progArg9 = (String)adminNames.get(11);
                String progArg10 = (String)adminNames.get(12);
                String progArg11 = (String)adminNames.get(13);
                String progArg12 = (String)adminNames.get(14);
                String progArg13 = (String)adminNames.get(15);
                String progArg14 = (String)adminNames.get(16);
                String progArg15 = (String)adminNames.get(17);
                adminVault = (String)adminNames.get(18);
                methodName = (String)adminNames.get(19);
                constructorParam = (String)adminNames.get(20);
                targetStates = (String)adminNames.get(21);
                errorType = (String)adminNames.get(22);

                // Get all the information about trigger objects found.
                sl = new StringList();
                sl.addElement("current");
                sl.addElement("attribute[" +  progName + "].value");
                sl.addElement("attribute[" +  sequenceNo + "].value");
                sl.addElement("attribute[" +  progArg1 + "].value");
                sl.addElement("attribute[" +  progArg2 + "].value");
                sl.addElement("attribute[" +  progArg3 + "].value");
                sl.addElement("attribute[" +  progArg4 + "].value");
                sl.addElement("attribute[" +  progArg5 + "].value");
                sl.addElement("attribute[" +  progArg6 + "].value");
                sl.addElement("attribute[" +  progArg7 + "].value");
                sl.addElement("attribute[" +  progArg8 + "].value");
                sl.addElement("attribute[" +  progArg9 + "].value");
                sl.addElement("attribute[" +  progArg10 + "].value");
                sl.addElement("attribute[" +  progArg11 + "].value");
                sl.addElement("attribute[" +  progArg12 + "].value");
                sl.addElement("attribute[" +  progArg13 + "].value");
                sl.addElement("attribute[" +  progArg14 + "].value");
                sl.addElement("attribute[" +  progArg15 + "].value");
                sl.addElement("attribute[" +  methodName + "].value");
                sl.addElement("attribute[" +  constructorParam + "].value");
                sl.addElement("attribute[" +  targetStates + "].value");
                sl.addElement("attribute[" +  errorType + "].value");
                sl = sl.unmodifiableCopy();

                vaultPattern = adminVault;
                if (vaultPattern == null || vaultPattern.length() == 0) {
                     vaultPattern = "*";
                }

                // Get state names from properties
                s = new String[2];
                s[0]  = "policy_eServiceTriggerProgramPolicy";
                s[1]  = "state_Active";
                ArrayList stateNames = getStateNamesFromProperties(context, s);
                stateActive = (String)stateNames.get(0);

                // Get cache age from properties file.
                String propValue = MessageUtil.getMessage(context, "emxNavigator.UICache.ComponentAge", null, null, null, "emxSystem");
                try  {
                    _componentAge = Long.parseLong(propValue) * 1000;
                } catch (Exception e) {
                    _componentAge = 3600 * 1000;
                }

                // Get RPE restore flag from properties files.
                propValue = EnoviaResourceBundle.getProperty(context, "emxFramework.RestoreRPE");
                if (propValue.equalsIgnoreCase("false")) {
                    _restoreRPE = false;
                }
            }
        }
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        i - a String contains time value
     * @return an int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     */


    public int mxMain(Context context, String []args)  throws Exception {
        for (int i = 0; i < args.length; i++) {
            TreeMap tm = CacheManager.getInstance().getValue(context, CacheManager._entityNames.TRIGGEROBJECTS, args[i]);

            // For each trigger object found execute trigger program
            Iterator it = (tm.keySet()).iterator();

/*Start Cahnges for Performance */
            String[] s = new String[1];
            ArrayList al;
            String invocation = "";
            if( it.hasNext() ) {
            	if (_restoreRPE) {
                  loadEvironmentVars(context);
              }

              // Set ORIGINAL_INVOCATION env variable to current INVOCATION
	            // as this variable gets set to program when tcl trigger program is executed
              s[0] = "get env INVOCATION";
              al = executeMQLCommands(context, s);
              invocation = (String)al.get(0);
              s[0] = "set env ORIGINAL_INVOCATION '" + invocation + "'";
              executeMQLCommands(context, s);
            }

/*End Of Changes */

            while (it.hasNext()) {
                BusinessObjectWithSelect bows = (BusinessObjectWithSelect)tm.get(it.next());

                // Get Target States attribute
                String targetStatesValue = bows.getSelectData("attribute[" +  targetStates + "].value");

                // If Target States attribute is set then
                if (targetStatesValue.length() != 0) {
                    // Get event type
                    s = new String[1];
                    s[0] = "get env EVENT";
                    al = executeMQLCommands(context, s);
                    String event = (String)al.get(0);

                    // if event = Promote or if event = Demote then
                    if (event.equals("Promote") || event.equals("Demote")) {
                        // Get next state and Get policy
                        s = new String[2];
                        s[0] = "get env NEXTSTATE";
                        s[1] = "get env POLICY";
                        al = executeMQLCommands(context, s);
                        String nextState = (String)al.get(0);
                        String policy = (String)al.get(1);

                        // Get Target States From Attribute vales
                        StringTokenizer st = new StringTokenizer(targetStatesValue, ",");

                        boolean targetStateFound = false;

                        // For each state property
                        while (st.hasMoreTokens()) {
                            String stateProp = st.nextToken();
                            // Get state name
                            String state = (String)stateMap.get(policy.concat(stateProp));
                            if (state == null) {
                                s = new String[2];
                                s[0] = policy;
                                s[1] = stateProp;
                                al = getStateNamesFromProperties(context, s);
                                state = (String)al.get(0);
                                stateMap.put(policy.concat(stateProp),state);
                            }
                            
                            if (state.equals(nextState)) {
                                targetStateFound = true;
                                break;
                            }
                        }

                        // if target state not found then continue.
                        if (!targetStateFound) {
                            continue;
                        }
                    }
                }

                // Get program name and type of program
                String prog = bows.getSelectData("attribute[" +  progName + "].value");
                String result = CacheManager.getInstance().getValue(context, CacheManager._entityNames.TRIGGERPROGRAM, prog);

                // If program is JPO then invoke it
                // else execute the program.
                if (result.compareTo("TRUE") == 0) {
                    String[] arguments = new String[15];
                    for (int k = 0; k < 15; k++) {
                        arguments[k] = bows.getSelectData((String)sl.elementAt(3 + k));
                    }

                    String method = bows.getSelectData("attribute[" +  methodName + "].value");
                    String constructorStr = bows.getSelectData("attribute[" +  constructorParam + "].value");

                    // read the constructor args and build a string array to hold them
                    String[] constructor = new String[1];
                    if (constructorStr != null && !constructorStr.equals("")) {
                      StringTokenizer constTok = new StringTokenizer(constructorStr);
                      if (constTok.countTokens() > 1) {
                          constructor = new String[constTok.countTokens()];
                      }
                      
                      // load the constructor args string array
                      int index = 0;
                      while (constTok.hasMoreTokens()) {
                          constructor[index++] = (constTok.nextToken()).trim();
                      }
                    }
                    // substitue the enviroment variables
                    subEnvironmentVars(context, constructor);
                    subEnvironmentVars(context, arguments);

                    if (method == null || method.length() == 0) {
                        method = "mxMain";
                    }

                    int retValue = JPO.invoke(context, prog, constructor, method, arguments);
                    if (retValue == 1) {
                        if (invocation.compareTo("action") == 0) {
                            String keys[] = new String[2];
                            keys[0] = "emxFrameworkStringResource";
                            keys[1] = "emxFramework.Trigger.emxTriggerManagerBase.TriggerFail";
                            Exception e = new Exception(getI18NString(context, keys));
                            throw e;
                        }
                            return 1;
                        }
                } else {
                    s = new String[1];
                    StringBuffer sBuffer = new StringBuffer(50);
                    sBuffer.append("execute program emxTriggerWrapper.tcl ");
                    for (int k = 0; k < 15; k++) {
                        sBuffer.append("'");
                        sBuffer.append(bows.getSelectData((String)sl.elementAt(3 + k)));
                        sBuffer.append("' ");
                    }
                    // append program name
                    sBuffer.append("'");
                    sBuffer.append(prog);
                    sBuffer.append("' ");
                    s[0] = sBuffer.toString();
                    ArrayList retArr = executeMQLCommands(context, s);
                    result = (String)retArr.get(0);

                    if (result.startsWith("1")) {
                        if (invocation.compareTo("action") == 0) {
                            String keys[] = new String[2];
                            keys[0] = "emxFrameworkStringResource";
                            keys[1] = "emxFramework.Trigger.emxTriggerManagerBase.TriggerFail";
                            Exception e = new Exception(getI18NString(context, keys));
                            throw e;
                        }
                            return 1;
                        }
                    }
                }
            }

        if (_restoreRPE) {
            if (!envVarsStack.empty()) {
                envVarsStack.pop();
            }
            HashMap envVarsLocal = null;
            if (!envVarsStack.empty()) {
                envVarsLocal = (HashMap)envVarsStack.peek();
            }

            // Reset environment variables with one stored at the begining
            if (envVarsLocal != null) {
                Iterator itr = (envVarsLocal.keySet()).iterator();
                String mqlCmds[] = new String[envVarsLocal.size()];
                int j = 0;
                while (itr.hasNext()) {
                    String envVarName = (String)itr.next();
                    String envVarVal = (String)envVarsLocal.get(envVarName);
                    mqlCmds[j] = "set env " + envVarName + " '" + envVarVal + "'";
                    j++;
                }
                executeMQLCommands(context, mqlCmds);
            }
        }

        return 0;
    }


    /**
     * subEnvironmentVars method substitute any ${} macros.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        i - a String Contains environment variable value
     * @throws Exception if the operation fails
     */


    protected void subEnvironmentVars(Context context, String[] args) throws Exception
    {
        String startStr = "${";
        String endStr = "}";
        String[] s = new String[1];

        for (int i = 0; i < args.length; i++) {
          String temp = args[i];
          if(temp != null) {
            int startIndex = temp.indexOf(startStr);
            int endIndex = temp.indexOf(endStr);

            // if the start and end delimiters where found, then extract the
            // key and look its value up
            if (startIndex != -1 && endIndex != -1) {
                String key = temp.substring(startIndex + startStr.length(), endIndex);
                s[0] = "get env " + key;
                ArrayList al = executeMQLCommands(context, s);
                args[i] = (String)al.get(0);
            }
          }
        }
    }

    /**
     * loadEvironmentVars method load the env variables and store them locally.
     *
     * @param context the eMatrix <code>Context</code> object
     * @throws Exception if the operation fails
     */

    protected void loadEvironmentVars(Context context) throws Exception
    {
        // Make a copy of all the environment variables
        String arguments[] = new String[1];

        // Get all the environment variable names
        arguments[0] = "listnames env";
        ArrayList cmdResultArr = executeMQLCommands(context, arguments);
        String cmdResult = (String)cmdResultArr.get(0);
        StringTokenizer strTok1 = new StringTokenizer(cmdResult, "\n");
        HashMap envVars = new HashMap(strTok1.countTokens());
        while (strTok1.hasMoreTokens())
        {
            String token = (strTok1.nextToken()).trim();
            StringBuffer argBuffer = new StringBuffer(30);
            argBuffer.append("get env \"");
            argBuffer.append(token);
            argBuffer.append("\"");
            arguments[0] = argBuffer.toString();
            cmdResultArr = executeMQLCommands(context, arguments);
            cmdResult = (String)cmdResultArr.get(0);
            envVars.put(token, cmdResult);
        }

        envVarsStack.push(envVars);
    }

    /**
     * resetCache.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an int 0 for success and non-zero for failure
     */

    public int resetCache(Context context, String[] args)
    {
    	// To clear the tenant specific cache
        CacheManager.getInstance().clearTenant(context, CacheManager._entityNames.TRIGGEROBJECTS);
        CacheManager.getInstance().clearTenant(context, CacheManager._entityNames.TRIGGERPROGRAM);
//         sl = null;
         return 0;
    }

    /**
     * Specifies the component age value.
     *
     * @param age is a long value
     */


    public void setComponentAge(long age)
    {
         _componentAge = age;
    }

    /**
     * Specifies the component age value.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - Contains Long value
     * @return an int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     */

    public int setComponentAge(Context context, String[] args) throws Exception
    {
        Long l = new Long(args[0]);
        _componentAge = l.longValue();
        return 0;
    }

    /**
     * Identifies this object as restore RPEFlag.
     *
     * @param flag is a boolean value
     */

    public void setRestoreRPEFlag(boolean flag)
    {
         _restoreRPE = flag;
    }

    /**
     * Specifies the restore RPEFlag value.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - a String contains boolean value
     * @return an int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     */

    public int setRestoreRPEFlag(Context context, String[] args) throws Exception
    {
        Boolean b = Boolean.valueOf(args[0]);
        _restoreRPE = b.booleanValue();
        return 0;
    }
}
