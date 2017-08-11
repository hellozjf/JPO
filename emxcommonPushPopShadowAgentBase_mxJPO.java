/*
**  emxcommonPushPopShadowAgentBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;
import matrix.db.Program;
import java.util.*;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.servlet.FrameworkServlet;

import matrix.db.MatrixWriter;
import java.io.BufferedWriter;

/**
 * The <code>emxcommonPushPopShadowAgentBase</code> class contains Push Pop Shadow Agent methods.
 *
 * @version AEF 10.0.1.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxcommonPushPopShadowAgentBase_mxJPO
{


    /** Create an instant of emxUtil JPO. */
    protected emxUtil_mxJPO utilityClass = null;

    /** Hashtable of Stack for user contexts. */
    protected static Hashtable _savedContextMap = new Hashtable(10);

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.1.0
     */

    public emxcommonPushPopShadowAgentBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        //if (!context.isConnected())
            //throw new Exception("not supported no desktop client");
        // Create an instant of emxUtil JPO
        utilityClass = new emxUtil_mxJPO(context, null);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 10.0.1.0
     */

    public int mxMain(Context context, String[] args)
        throws Exception
    {
        //if (!context.isConnected())
            //throw new Exception("not supported no desktop client");
        return 0;
    }

    /**
     * This utility method gets password of the shadow agent.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return String object containing the password for the shadow agent
     * @throws Exception if the operation fails
     * @since AEF 10.0.1.0
     */

    public static String getShadowAgentPassword(Context context,  String[] args)
        throws Exception
    {
    	//System.out.println("getShadowAgentPassword");
        // password info program.
        String passwordFile = "emxcommonSessionInfo";

        // Get the code of the password info program.
        Program prog = new Program(passwordFile, false, false, false, false);
        prog.open(context);
        String passwd = prog.getCode(context);
        prog.close(context);

        String password = FrameworkUtil.decrypt(FrameworkUtil.decrypt(passwd));

        BufferedWriter writer = new BufferedWriter(new MatrixWriter(context));
        writer.write(password);
        writer.flush();

        return password;

    }

    private static void setAgentPassword(Context context,  String agentSymbolicName, String agentPassword) throws Exception
    {
    	//System.out.println("setAgentPassword");
        String agentName = PropertyUtil.getSchemaProperty(context, agentSymbolicName);
        if (agentName != null && agentName.length() > 0) {
            String strBuff="escape modify person $1 password $2";
            MqlUtil.mqlCommand(context, strBuff, agentName, escape(agentPassword));
        }
    }

    public static void setAgentPassword(Context context,  String[] args) throws Exception
    {
    	//System.out.println("setAgentPassword with args");
        // password info program.
        String passwordFile = "emxcommonSessionInfo";

        // Get the code of the password info program.
        Program prog = new Program(passwordFile, false, false, false, false);
        prog.open(context);
        String passwd = prog.getCode(context);
        prog.close(context);

        if (passwd != null && "shadowsecret".equals(passwd.trim())) {
            setAgentPassword(context, args[0], args[1]);
        }
    }

    /**
     * This utility method to set password of the shadow agent.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args[0] should be User Agent Password
     * @return nothing
     * @throws Exception if the operation fails
     * @since AEF 10.0.1.0
     */
    public static void setShadowAgentPassword(Context context,  String[] args)
        throws Exception
    {
    	//System.out.println("setShadowAgentPassword");
        try
        {
            ContextUtil.startTransaction(context, true);
            String agentPassword = args[0];

            // Setting the "User Agent" password
            setAgentPassword(context, "person_UserAgent", agentPassword);

            // Setting the "Common Access Grantor" password
            setAgentPassword(context, "person_CommonAccessGrantor", agentPassword);

            // Setting the "Project Space Access Grantor" password
            setAgentPassword(context, "person_ProjectSpaceAccessGrantor", agentPassword);

            // Setting the "Request Access Grantor" password
            setAgentPassword(context, "person_RequestAccessGrantor", agentPassword);

            // Setting the "Route Access Grantor" password
            setAgentPassword(context, "person_RouteAccessGrantor", agentPassword);

            // Setting the "Route Delegation Grantor" password
            setAgentPassword(context, "person_RouteDelegationGrantor", agentPassword);

            // Setting the "Service Creator" password
            setAgentPassword(context, "person_ServiceCreator", agentPassword);

            // Setting the "Unmanaged Document Grantor" password
            setAgentPassword(context, "person_UnmanagedDocumentGrantor", agentPassword);

            // Setting the "Workspace Access Grantor" password
            setAgentPassword(context, "person_WorkspaceAccessGrantor", agentPassword);

            // Setting the "Workspace Lead Grantor" password
            setAgentPassword(context, "person_WorkspaceLeadGrantor", agentPassword);

            // Setting the "Workspace Member Grantor" password
            setAgentPassword(context, "person_WorkspaceMemberGrantor", agentPassword);

            String strBuff = "modify program $1 code $2";
            agentPassword = FrameworkUtil.encrypt(FrameworkUtil.encrypt(agentPassword));
            MqlUtil.mqlCommand(context, strBuff.toString(), "emxcommonSessionInfo", agentPassword);
            ContextUtil.commitTransaction(context);
        }
        catch(Exception ex)
        {
            ContextUtil.abortTransaction(context);
            ex.printStackTrace();
            throw ex;
        }
    }

    private static String escape(String input)
    {
        input = input.replaceAll("'", "\'");
        input = input.replaceAll("\"", "\\\"");
        return input;
    }
    /**
     * This utility method pushes context to the shadow agent.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *      0 - a String which contains userName
     * @return  int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 10.0.1.0
     * @deprecated use emxContextUtil.pushContext
     */

    public int pushContext(Context context,  String[] args)
        throws Exception
    {
        String userName = null;

        // If user not passed then default to User Agent
        if (args == null) {
            userName = "person_UserAgent";
        } else {
            userName = args[0];
        }

        // If property name of user is defined then get user name from property.
        if (userName.startsWith("person_") == true) {
            String arguments[] = new String[1];
            arguments[0] = userName;
            ArrayList retArr = utilityClass.getAdminNameFromProperties(context, arguments);
            userName = (String)retArr.get(0);
        }

        // get shadow agent password
        String password = getShadowAgentPassword(context, args);

        // set env APPREALUSER to logged in person
        // this env is referenced by trigger programs to get logged in person
        // after changing context to shadow agent.
        String arguments[] = new String[1];
        StringBuffer argBuffer = new StringBuffer(30);
        argBuffer.append("set env APPREALUSER '");
        argBuffer.append(context.getUser());
        argBuffer.append("'");
        arguments[0] = argBuffer.toString();
        utilityClass.executeMQLCommands(context, arguments);
        String rpeUserName = PropertyUtil.getGlobalRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME);
        if(rpeUserName == null || "".equals(rpeUserName) || "null".equals(rpeUserName) )
		{
        	//RPE variable value passed to PropertyUtil.setGlobalRPEValue() is enclosed in qoutes, as 
        	//the method does not use parameterised MqlUtil.mqlCommand() to set RPE variable
        	//resulting in issues with values having space.
        	//TODO:change the method to use parameterised MqlUtil.mqlCommand().
        	PropertyUtil.setGlobalRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME, "\"" + context.getUser() + "\"");
        }

        // Store context information on stack.
        String contextInfo[] = new String[3];
        contextInfo[0] = context.getUser();
        contextInfo[1] = context.getPassword();
        contextInfo[2] = context.getVault().getName();

        Stack contextStack = (Stack) _savedContextMap.get(context);
        if ( contextStack == null )
        {
            contextStack = new Stack();
            _savedContextMap.put(context,contextStack);
        }
        contextStack.push(contextInfo);

        // reset the context to shadow agent
        context.resetContext(userName, password, contextInfo[2]);

        return 0;
    }

    /**
     * This utility method pops context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return  int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 10.0.1.0
     * @deprecated use emxContextUtil.popContext
     */

    public int popContext(Context context, String[] args)
        throws Exception
    {

        // retrive user from APPREALUSER
        String arguments[] = new String[1];
        arguments[0] = "get env APPREALUSER";
        ArrayList cmdResultArr = utilityClass.executeMQLCommands(context, arguments);
        String cmdResult = (String)cmdResultArr.get(0);

        // pop context from stack
        Stack contextStack = (Stack) _savedContextMap.get(context);
        String contextInfo[] = (String[])contextStack.pop();
        if ( contextStack.empty() )
        {
            _savedContextMap.remove(context);
        }

        context.resetContext(contextInfo[0], contextInfo[1], contextInfo[2]);

        // set USER env variable
        StringBuffer argBuffer = new StringBuffer(50);
        argBuffer.append("set env USER '");
        argBuffer.append(cmdResult);
        argBuffer.append("'");
        arguments[0] = argBuffer.toString();
        utilityClass.executeMQLCommands(context, arguments);

        // unset APPREALUSER
        arguments[0] = "unset env APPREALUSER";
        utilityClass.executeMQLCommands(context, arguments);

        return 0;
    }
	
}
