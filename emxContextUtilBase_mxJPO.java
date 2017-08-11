/*
**   emxContextUtilBase
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Stack;
import com.matrixone.apps.domain.util.*;


/**
 * The <code>emxContextUtilBase</code> class contains static methods for managing context.
 *
 * @version AEF 9.5.1.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxContextUtilBase_mxJPO
{

    /** Hashtable of Stack for user contexts. */
    protected static Hashtable _savedContextMap = new Hashtable(10);
    /** Create an instance of emxMailUtil JPO. */
    static protected emxMailUtil_mxJPO mailUtil = null;
    /** Declare boolean variable. */
    static protected boolean keepWorkAround = false;

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public emxContextUtilBase_mxJPO (Context context, String[] args)
      throws Exception
    {

        /*if (!context.isConnected())
            throw new Exception("not supported on desktop client");
        */
        mailUtil = new emxMailUtil_mxJPO(context, null);
       String propValue = MessageUtil.getMessage(context, "emxFramework.BeanPushPopContextWorkaround", null, null, null, "emxSystem");
        try
        {
            keepWorkAround = Boolean.valueOf(propValue).booleanValue();
        }
        catch (Exception e)
        {
            keepWorkAround = false;
        }

    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return  int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public int mxMain(Context context, String[] args)
      throws Exception
    {
        if (true)
        {
            String languageStr = context.getSession().getLanguage();
            String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.Invocation", new Locale(languageStr));            
            exMsg += EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.ContextUtilBase", new Locale(languageStr));   
            throw new Exception(exMsg);
        }
        return 0;
    }

    /**
     * This method gets the super user name.
     *
     * @param context the eMatrix <code>Context</code> object
     * @return a String contains the super user name
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    protected static String getSuperUserName(Context context)
        throws Exception
    {
        return PropertyUtil.getSchemaProperty(context, "person_UserAgent");
    }

    /**
     * This method gets password of the shadow agent.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return a String contains password
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public String getSuperUserPassword(Context context,  String[] args)
      throws Exception
    {
        return emxcommonPushPopShadowAgent_mxJPO.getShadowAgentPassword(context,args);
    }

    /**
     * This method sets the user context with the user name passed and
     *   if no user is passed, then the context will be set with the shadow agent user.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the user name
     *        1 - String containing the user password
     *        2 - String containing the user vault
     * @return  int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public int pushContext(Context context,  String[] args)
      throws Exception
    {

        String userName  = null;
        String superName = getSuperUserName(context);
        String password  = null;
        String vault = null;
        boolean noUserName = false;
        boolean noPassword = false;
        boolean noVault = false;

        if (args != null && args.length > 0 && args[0].length() > 0) {
            userName = args[0];
        } else {
      noUserName = true;
            // If user not passed then default to User Agent
            userName = superName;
        }

        if (args != null && args.length > 1 && args[1].length() > 0) {
            password = args[1];
        } else {
            noPassword = true;
            // get shadow agent password
            password = getSuperUserPassword(context, null);
        }

        if (args != null && args.length > 2 && args[2].length() > 0) {
            vault = args[2];
        } else {
            noVault = true;
            // get context vault
            vault = context.getVault().getName();
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

        // If no password is given, go through
        // super user to the new context.
        if (noUserName == false && noPassword == true)
        {
            context.resetContext(superName, password, vault);
            password = "";
        }

        // Reset the context name, password, and vault.
        context.setUser(userName);
        context.setPassword(password);
        context.resetContext(userName, password, vault);

        return 0;
    }

    /**
     * This method resets the user context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return  int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public int popContext(Context context, String[] args)
      throws Exception
    {

        // pop context from stack
        Stack contextStack = (Stack) _savedContextMap.get(context);
        String contextInfo[] = (String[])contextStack.pop();
        if ( contextStack.empty() )
        {
            _savedContextMap.remove(context);
        }

        // If the saved context had no password,  then reset the context
        // to super user before reseting to the previous user.
        if (contextInfo[1] == null || contextInfo[1].equals(""))
        {
            context.resetContext(
                    getSuperUserName(context),
                    getSuperUserPassword(context, null),
                    contextInfo[2]);
        }

        // Reset the context name, password, and vault.
        context.setUser(contextInfo[0]);
        context.setPassword(contextInfo[1]);
        context.resetContext(contextInfo[0],contextInfo[1],contextInfo[2]);

        return 0;
    }

    /**
     * This method sets the Bean context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *         0 - String containing the user name
     * @return  int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public int pushBeanContext(Context context, String[] args)
      throws Exception
    {
        if (keepWorkAround)
        {
            ContextUtil.pushContext(context, args[0], null, null);
        }
        return 0;
    }

    /**
     * This method resets Bean context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return  int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public int popBeanContext(Context context, String[] args)
      throws Exception
    {
        if (keepWorkAround)
        {
            ContextUtil.popContext(context);
        }
        return 0;
    }

    /**
     * This method displays mql error message.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param error String contains error message
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */

    public static void mqlError(Context context, String error)
        throws Exception
    {
    	MqlUtil.mqlCommand(context, "error $1",error); 

    }

    /**
     * This method displays mql notice message.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param notice String containing notice message
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */

    public static void mqlNotice(Context context, String notice)
        throws Exception
    {
        MqlUtil.mqlCommand(context, "notice $1", notice);
    }

    /**
     * This method displays mql warning message.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param warning String containing warning message
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public static void mqlWarning(Context context, String warning)
        throws Exception
    {
        MqlUtil.mqlCommand(context, "warning $1", warning);
    }
}
