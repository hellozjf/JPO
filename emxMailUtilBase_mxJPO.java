/*
**   emxMailUtilBase
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.IconMail;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.MatrixWriter;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;


/**
 * The <code>emxMailUtilBase</code> class contains static methods for sending mail.
 *
 * @version AEF 9.5.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxMailUtilBase_mxJPO
{

    private static final String NOTIFICATION_AGENT_NAME = "emxMailUtilBase.NotificationAgentName";
    
    /** Holds the base URL for notification messages. */
    protected static String _baseURL = "";

    /** Holds the agent name for notification messages. 
     * @deprecated agent name will be stored inside context 
     * */
    protected static String _agentName = "";

    /** Holds the languages for notification messages. */
    protected static String _languages = "";

    /** Holds the locales for notification messages. */
    protected static Vector _locales = null;
    
    
    private static boolean mailConfigurationsLoaded = false;
    
    private static final Object _lock = new Object();
    
    private static String _configuredNotificationAgentName = null;
    

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */

    public emxMailUtilBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        /*if (!context.isConnected())
            throw new Exception("not supported on desktop client");
        */
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an int 0, status code.
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */

    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            String languageStr = context.getSession().getLanguage();
            String exMsg = i18nNow.getI18nString("emxFramework.Message.Invocation","emxFrameworkStringResource",languageStr);
            exMsg += i18nNow.getI18nString("emxFramework.MailUtilBase","emxFrameworkStringResource",languageStr);
            throw new Exception(exMsg);
        }
        return 0;
    }

    /**
     * This method set base URL string used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - a String entry containing an url
     * @return an int 0
     * @throws Exception if the operation fails
     * @see #getBaseURL
     * @since AEF 9.5.0.0
     */

    public static int setBaseURL(Context context, String[] args)
        throws Exception
    {
        if (args != null && args.length > 0)
        {
            _baseURL = args[0];
        }
        return 0;
    }

    /**
     * This method get base URL string used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return a string url
     * @throws Exception if the operation fails
     * @see #setBaseURL
     * @since AEF 9.5.0.0
     */

    public static String getBaseURL(Context context, String[] args)
        throws Exception
    {
        return _baseURL;
    }

    /**
     * This method get Stream base URL string used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an int 0 status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */

    public static int getStreamBaseURL(Context context, String[] args)
        throws Exception
    {
        BufferedWriter writer = new BufferedWriter(new MatrixWriter(context));
        writer.write(_baseURL);
        writer.flush();
        return 0;
    }

    /**
     * This method set agent name used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - a String containing the agent name
     * @return an int 0 status code
     * @throws Exception if the operation fails
     * @see #getAgentName
     * @since AEF 9.5.1.0
     */

    public static int setAgentName(Context context, String[] args)
        throws Exception
    {
        if (args != null && args.length > 0)
        {
           //No need to insert blank or null, just remove it from context.
            if(args[0] != null && !args[0].equals("")) {
                context.setCustomData(NOTIFICATION_AGENT_NAME, args[0]);    
            }  else {
                context.removeFromCustomData(NOTIFICATION_AGENT_NAME);
            }
        }
        return 0;
    }

    /**
     * This method get agent name used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return a String specifying the name of the person to use in "from" field
     * @throws Exception if the operation fails
     * @see #setAgentName
     * @since AEF 9.5.0.0
     */

    public static String getAgentName(Context context, String[] args)
        throws Exception
    {
        /**
         * If context user has set Notification Agent Name into Agent Name, return that value
         * other wise return agent name configured by the Administrator.
         */
        String agentName = context.getCustomData(NOTIFICATION_AGENT_NAME);
        return agentName != null ? agentName : 
               mailConfigurationsLoaded ? _configuredNotificationAgentName : EnoviaResourceBundle.getProperty(context, "emxFramework.NotificationAgent");
    }

    /**
     * This method set languages used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - a String contains a space-delimited list of locals
     * @return an int 0 status code
     * @throws Exception if the operation fails
     * @see #getLanguages
     * @since AEF 9.5.1.0
     */

    public static int setLanguages(Context context, String[] args)
        throws Exception
    {
        if (args != null && args.length > 0)
        {
            _languages = args[0];
            _locales = getLocales(_languages.trim());
        }
        return 0;
    }

    /**
     * This method get languages used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return a String contains information regarding the languages
     * @throws Exception if the operation fails
     * @see #setLanguages
     * @since AEF 9.5.0.0
     */

    public static String getLanguages(Context context, String[] args)
        throws Exception
    {
        return _languages;
    }

    /**
     * This method get locales used in notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return a Vector contains information regarding the Locale
     * @throws Exception if the operation fails
     * @see #setLanguages
     * @since AEF 9.5.0.0
     */

    public static Vector getLocales(Context context, String[] args)
        throws Exception
    {
        return _locales;
    }

    /**
     * This method sends an icon mail notification to a single specified user and
     * Appends a url to the message if an objectId is given.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - a String that holds the user name to be notified
     *        1 - a String containing the notification subject
     *        2 - a String containing the notification message
     *        3 - a String containing the id of the object to be included in the notification url
     * @return an int 0 status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     * @deprecated use sendNotificationToUser()
     */

    public static int sendMail(Context context, String[] args)
        throws Exception
    {
        if (args == null || args.length < 4)
        {
            throw (new IllegalArgumentException());
        }
        String toUser = args[0];
        String subject = args[1];
        String message = args[2];
        String objectId = args[3];
        String url = _baseURL;
        StringList toList = new StringList(1);
        toList.addElement(toUser);

        if (objectId != null && url.length() > 0)
        {
            url += "?objectId=";
            url += objectId;
            message += "\n\n";
            message += url;
        }

        sendMessage(context,
                    toList,
                    null,
                    null,
                    subject,
                    message,
                    null);
        return 0;
    }

    /**
     * This method sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *        toList - a StringList containing the list of users to notify
     *        ccList - a StringList containing the list of users to cc
     *        bccList - a StringList containing the list of users to bcc
     *        subject - a String that contains the notification subject
     *        message - a String that contains the notification message
     *        objectIdList - a StringList containing the ids of objects to send with the notification
     * @return an int 0 status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */

    public static int sendMessage(Context context, String[] args)
        throws Exception
    {
        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
        Map map = (Map) JPO.unpackArgs(args);

        sendMessage(context,
                    (StringList) map.get("toList"),
                    (StringList) map.get("ccList"),
                    (StringList) map.get("bccList"),
                    (String) map.get("subject"),
                    (String) map.get("message"),
                    (StringList) map.get("objectIdList"));

        return 0;
    }

    /**
     * This method sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *        toList - a StringList containing the list of users to notify
     *        ccList - a StringList containing the list of users to cc
     *        bccList - a StringList containing the list of users to bcc
     *        subjectKey - a String that contains the notification subject key
     *        subjectKeys - a String array of subject place holder keys
     *        subjectValues - a String array of subject place holder values
     *        messageKey - a String that contains the notification message key
     *        messageKeys - a String array of message place holder keys
     *        messageValues - a String array of message place holder values
     *        objectIdList - a StringList containing the ids of objects to send with the notification
     *        companyName - a String that is used for company-based messages
     *        basePropFileName - an optional String, the property file to search for keys
     * @return an int 0 status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public static int sendNotification(Context context, String[] args)
        throws Exception
    {

        if (args == null || args.length < 1)
        {
            throw (new IllegalArgumentException());
        }
        Map map = (Map) JPO.unpackArgs(args);

        // If property file name is passed in, call
        // other sendNotification that takes this as a parameter
        //
        if (map.get("basePropFileName") != null)
        {

            sendNotification(context,
                        (StringList) map.get("toList"),
                        (StringList) map.get("ccList"),
                        (StringList) map.get("bccList"),
                        (String) map.get("subjectKey"),
                        (String[]) map.get("subjectKeys"),
                        (String[]) map.get("subjectValues"),
                        (String) map.get("messageKey"),
                        (String[]) map.get("messageKeys"),
                        (String[]) map.get("messageValues"),
                        (StringList) map.get("objectIdList"),
                        (String) map.get("companyName"),
                        (String) map.get("basePropFileName"));
        }
        else
        {
            sendNotification(context,
                        (StringList) map.get("toList"),
                        (StringList) map.get("ccList"),
                        (StringList) map.get("bccList"),
                        (String) map.get("subjectKey"),
                        (String[]) map.get("subjectKeys"),
                        (String[]) map.get("subjectValues"),
                        (String) map.get("messageKey"),
                        (String[]) map.get("messageKeys"),
                        (String[]) map.get("messageValues"),
                        (StringList) map.get("objectIdList"),
                        (String) map.get("companyName"));
        }
        return 0;
    }

    /**
     * This method sends an icon mail notification to a single specified user.
     * Appends a url to the message if an objectId is given.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        toList - a comma separated list of users to notify
     *        subjectKey - a String that contains the notification subject key
     *        subjectSubCount - a int indicating the number of subject key/subject value pairs for subject substitution
     *        subjectKey1 - the first subject key
     *        subjectValue1 - the first subject value
     *        messageKey - a String that contains the notification message key
     *        messageSubCount - a int indicating the number of key/value pairs for message substitution
     *        messageKey1 - the first message key
     *        messageValue1 - the first message value
     *        objectIdList - a comma separated list of objectids to include in the notification url
     *        companyName - a String used for company-based messages
     *        basePropName - the property file to search for keys.
     * @return an int 0 status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public static int sendNotificationToUser(Context context, String[] args)
        throws Exception
    {
        if (args == null || args.length < 3)
        {
            throw (new IllegalArgumentException());
        }
        int index = 0;
        StringTokenizer tokens = new StringTokenizer(args[index++], ",");
        StringList toList = new StringList();
        while (tokens.hasMoreTokens())
        {
            toList.addElement(tokens.nextToken().trim());
        }

        String subjectKey = args[index++];
        int subCount = Integer.parseInt(args[index++]);
        String[] subjectKeys = new String[subCount];
        String[] subjectValues = new String[subCount];
        if (args.length < 3+(subCount*2))
        {
            throw (new IllegalArgumentException());
        }
        for (int i=0; i < subCount ;i++)
        {
            subjectKeys[i] = args[index++];
            subjectValues[i] = args[index++];
        }

        String messageKey = args[index++];
        subCount = Integer.parseInt(args[index++]);
        String[] messageKeys = new String[subCount];
        String[] messageValues = new String[subCount];
        for (int i=0; i < subCount ;i++)
        {
            messageKeys[i] = args[index++];
            messageValues[i] = args[index++];
        }

        StringList objectIdList = null;
        if (args.length > index)
        {
            tokens = new StringTokenizer(args[index++], ",");
            objectIdList = new StringList();
            while (tokens.hasMoreTokens())
            {
                objectIdList.addElement(tokens.nextToken().trim());
            }
        }

        String companyName = null;
        if (args.length > index)
        {
            companyName = args[index++];
        }

        String basePropName = MessageUtil.getBundleName(context);
        if (args.length > index)
        {
            basePropName = args[index];
        }

        sendNotification(context,
                         toList,
                         null,
                         null,
                         subjectKey,
                         subjectKeys,
                         subjectValues,
                         messageKey,
                         messageKeys,
                         messageValues,
                         objectIdList,
                         companyName,
                         basePropName);
        return 0;
    }

    /**
     * This method can be used to get a processed and translated message for a given key.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        messageKey - String contains the message key
     *        messageSubCount - int count indicating the number of subject key/subject value pairs for subject substitution
     *        messageKey1 - the first message key
     *        messageValue1 - the first message value
     *        companyName - String that is used for company-based messages
     *        basePropFileName - String that holds the property file name to be used
     * @return a String containing the message
     * @throws Exception if the operation fails
     * @since AEF 9.5.3.0
     */

    public static String getMessage(Context context, String[] args)
        throws Exception
    {
        if (args == null || args.length < 2)
        {
          throw (new IllegalArgumentException());
        }

        int index = 0;
        String messageKey = args[index++];
        int subCount = Integer.parseInt(args[index++]);
        String[] messageKeys = new String[subCount];
        String[] messageValues = new String[subCount];

        if (args.length < 2+(subCount*2))
        {
          throw (new IllegalArgumentException());
        }

        for (int i=0; i < subCount ;i++)
        {
          messageKeys[i] = args[index++];
          messageValues[i] = args[index++];
        }

        String companyName = null;
        if (args.length > index)
        {
          companyName = args[index++];
        }

        String basePropFileName = MessageUtil.getBundleName(context);
        if (args.length > index)
        {
          basePropFileName = args[index];
        }

        String message = MessageUtil.getMessage(context,
                 messageKey,
                 messageKeys,
                 messageValues,
                 companyName,
                 basePropFileName);

        BufferedWriter writer = new BufferedWriter(new MatrixWriter(context));
        writer.write(message);
        writer.flush();
        return message;
    }

    /**
     * This method returns a processed and translated message for a given key.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param messageKey String that contains the notification message key
     * @param messageKeys String array of message place holder keys
     * @param messageValues String  array of message place holder values
     * @param companyName String that is used for company-based messages
     * @return a String contains the message
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     * @deprecated AEF 10.5.0.1, use MessageUtil.getMessage
     */

    public static String getMessage(Context context,
                                     String messageKey,
                                     String[] messageKeys,
                                     String[] messageValues,
                                     String companyName)
        throws Exception
    {
        return MessageUtil.getMessage(context,messageKey,messageKeys,messageValues,companyName);
    }

    /**
     * This method returns a processed and translated message for a given key.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param messageKey String that contains the notification message key
     * @param messageKeys String array of message place holder keys
     * @param messageValues String array of message place holder values
     * @param companyName String that is used for company-based messages
     * @param basePropFileName String that is used for name of property file
     * @return a String containing the message
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     * @deprecated AEF 10.5.0.1, use MessageUtil.getMessage
     */

    public static String getMessage(Context context,
                                     String messageKey,
                                     String[] messageKeys,
                                     String[] messageValues,
                                     String companyName,
                                     String basePropFileName)
        throws Exception
    {
        return MessageUtil.getMessage(context,messageKey,messageKeys,messageValues,companyName,basePropFileName);
    }

    /**
     * This method returns a processed and translated message for a given key.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId String that contains id of the object
     * @param messageKey String that contains the notification message key
     * @param messageKeys String array of message place holder keys
     * @param messageValues String array of message place holder values
     * @param companyName String that is used for company-based messages
     * @param basePropFileName String that used for name of property file
     * @return a String containing the message
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     * @deprecated AEF 10.5.0.1, use MessageUtil.getMessage
     */

    public static String getMessage(Context context,
                                     String objectId,
                                     String messageKey,
                                     String[] messageKeys,
                                     String[] messageValues,
                                     String companyName,
                                     String basePropFileName)
        throws Exception
    {
        return MessageUtil.getMessage(context,objectId,messageKey,messageKeys,messageValues,companyName,basePropFileName);
    }

    /**
     * This method sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param toList String that contains the list of users to notify
     * @param ccList String that contains the list of users to cc
     * @param bccList String that contains the list of users to bcc
     * @param subject String that contains the notification subject
     * @param message String that contains the notification message
     * @param objectIdList StringList that contains the ids of objects to send with the notification
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */

    protected static void sendMessage(Context context,
                                    StringList toList,
                                    StringList ccList,
                                    StringList bccList,
                                    String subject,
                                    String message,
                                    StringList objectIdList)
        throws Exception
    {

        // If there is no subject, then return without sending the notification.
        if (subject == null || "".equals(subject))
        {
            return;
        }

        MQLCommand mql = new MQLCommand();
        mql.open(context);
        String mqlCommand = "get env global MX_TREE_MENU";
        mql.executeCommand(context, mqlCommand);
        String paramSuffix = mql.getResult();

        if (paramSuffix != null && !"".equals(paramSuffix) && !"\n".equals(paramSuffix)){
            mqlCommand = "unset env global MX_TREE_MENU";
            mql.executeCommand(context, mqlCommand);
        }

        // If the base URL and object id list are available,
        // then add urls to the end of the message.
        if ( (_baseURL != null && ! "".equals(_baseURL)) &&
             (objectIdList != null && objectIdList.size() != 0) )
        {
            // Prepare the message for adding urls.
            message += "\n";

            Iterator i = objectIdList.iterator();
            while (i.hasNext())
            {
                // Add the url to the end of the message.
                message += "\n" + _baseURL + "?objectId=" + (String) i.next();
                if (paramSuffix != null && !"".equals(paramSuffix) && !"null".equals(paramSuffix) && !"\n".equals(paramSuffix)){
                  message += "&treeMenu=" + paramSuffix;
                }

            }

        }

        // Send the mail message.
        sendMail(context,
                 toList,
                 ccList,
                 bccList,
                 subject,
                 message,
                 objectIdList);
    }

    /**
     * This method sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param toList StringList that contains the list of users to notify
     * @param ccList StringList that contains the list of users to cc
     * @param bccList StringList that contains the list of users to bcc
     * @param subjectKey String that contains the notification subject key
     * @param subjectKeys String array of subject place holder keys
     * @param subjectValues String array of subject place holder values
     * @param messageKey String that contains the notification message key
     * @param messageKeys String array of message place holder keys
     * @param messageValues String array of message place holder values
     * @param objectIdList StringList that contains the ids of objects to send with the notification
     * @param companyName String that is used for company-based messages
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public static void sendNotification(Context context,
                                    StringList toList,
                                    StringList ccList,
                                    StringList bccList,
                                    String subjectKey,
                                    String[] subjectKeys,
                                    String[] subjectValues,
                                    String messageKey,
                                    String[] messageKeys,
                                    String[] messageValues,
                                    StringList objectIdList,
                                    String companyName)
        throws Exception
    {
        sendNotification(context,
                         toList, ccList, bccList,
                         subjectKey, subjectKeys, subjectValues,
                         messageKey, messageKeys, messageValues,
                         objectIdList,
                         companyName,
                         MessageUtil.getBundleName(context));
    }

    /**
     * This method sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param toList StringList that contains the list of users to notify
     * @param ccList StringList that contains the list of users to cc
     * @param bccList StringList that contains the list of users to bcc
     * @param subjectKey String that contains the notification subject key
     * @param subjectKeys String array of subject place holder keys
     * @param subjectValues String array of subject place holder values
     * @param messageKey String that contains the notification message key
     * @param messageKeys String array of message place holder keys
     * @param messageValues String array of message place holder values
     * @param objectIdList StringList that contains the ids of objects to send with the notification
     * @param companyName String that is used for company-based messages
     * @param basePropFile String that is used for the name of property files to search for keys.
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public static void sendNotification(Context context,
                                        StringList toList,
                                        StringList ccList,
                                        StringList bccList,
                                        String subjectKey,
                                        String[] subjectKeys,
                                        String[] subjectValues,
                                        String messageKey,
                                        String[] messageKeys,
                                        String[] messageValues,
                                        StringList objectIdList,
                                        String companyName,
                                        String basePropFile)
        throws Exception
    {
        sendNotification(context,
                         null,
                         toList,
                         ccList,
                         bccList,
                         subjectKey,
                         subjectKeys,
                         subjectValues,
                         messageKey,
                         messageKeys,
                         messageValues,
                         objectIdList,
                         companyName,
                         basePropFile);
    }

    /**
     * This method sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId String contains id of the object
     * @param toList StringList that contains the list of users to notify
     * @param ccList StringList that contains the list of users to cc
     * @param bccList StringList that contains the list of users to bcc
     * @param subjectKey String that contains the notification subject key
     * @param subjectKeys String array of subject place holder keys
     * @param subjectValues String array of subject place holder values
     * @param messageKey String that contains the notification message key
     * @param messageKeys String array of message place holder keys
     * @param messageValues String array of message place holder values
     * @param objectIdList StringList that contains the ids of objects to send with the notification
     * @param companyName String that is used for company-based messages
     * @param basePropFile String that is used for the name of property files to search for keys.
     * @throws Exception if the operation fails
     * @since AEF 9.5.5.0
     */

    public static void sendNotification(Context context,
                                        String objectId,
                                        StringList toList,
                                        StringList ccList,
                                        StringList bccList,
                                        String subjectKey,
                                        String[] subjectKeys,
                                        String[] subjectValues,
                                        String messageKey,
                                        String[] messageKeys,
                                        String[] messageValues,
                                        StringList objectIdList,
                                        String companyName,
                                        String basePropFile)
        throws Exception
    {
        //
        // "names" holds the user names and language preferences
        //
        HashMap names = new HashMap();
        //
        // "languages" holds the unique languages
        //
        HashMap languages = new HashMap();
        getNamesAndLanguagePreferences(context, names, languages, toList);
        getNamesAndLanguagePreferences(context, names, languages, ccList);
        getNamesAndLanguagePreferences(context, names, languages, bccList);

        //
        // send one message per language
        //
        
        //Added for bug 344780
        //add them to the message, do it here for localization purpose
        MapList objInfoMapList = null;
        boolean hasBusObjInfo = false;
        if (objectIdList != null && objectIdList.size() != 0)
        {            
            StringList busSels = new StringList(3);
            busSels.add(DomainObject.SELECT_TYPE);
            busSels.add(DomainObject.SELECT_NAME);
            busSels.add(DomainObject.SELECT_REVISION);

            objInfoMapList = DomainObject.getInfo(context, (String[])objectIdList.toArray(new String[]{}), busSels);
            hasBusObjInfo = objInfoMapList != null && objInfoMapList.size() > 0;
        }        
        Iterator itr = languages.keySet().iterator();
        while (itr.hasNext())
        {
            String language = (String) itr.next();
            Locale userPrefLocale = language.length() > 0 ? MessageUtil.getLocale(language) : null;
            Locale locale = userPrefLocale == null ? getLocale(context) : userPrefLocale;

            //
            // build the to, cc and bcc lists for this language
            //
            StringList to = getNamesForLanguage(toList, names, language);
            StringList cc = getNamesForLanguage(ccList, names, language);
            StringList bcc = getNamesForLanguage(bccList, names, language);

            String subject = MessageUtil.getMessage(context, objectId, subjectKey, subjectKeys, subjectValues, companyName, locale, basePropFile);
            StringBuffer messageBuffer = new StringBuffer();
            if(hasBusObjInfo)
            {
                messageBuffer.append(MessageUtil.getString("emxFramework.IconMail.ObjectDetails.CheckBusinessObjects", "", locale)).append("\n");
                for(int i=0; i < objInfoMapList.size() ; i++)
                {
                    Map objInfoMap = (Map)objInfoMapList.get(i);
                    String type = (String)objInfoMap.get(DomainObject.SELECT_TYPE);

                    messageBuffer.append("\n\"");
                    messageBuffer.append(UINavigatorUtil.getAdminI18NString("Type", type, locale.getLanguage()));
                    messageBuffer.append("\" \"");
                    messageBuffer.append(objInfoMap.get(DomainObject.SELECT_NAME));
                    messageBuffer.append("\" \"");
                    messageBuffer.append(objInfoMap.get(DomainObject.SELECT_REVISION));
                    messageBuffer.append("\"\n");
                }

                messageBuffer.append("\n");
            }            
            
            // if this is the no language preference group
            if (userPrefLocale == null && _locales != null)
            {
                // generate a message containing multiple languages
                for (int i = 0; i < _locales.size(); i++)
                {
                    // Define the mail message.
                    messageBuffer.append(MessageUtil.getMessage(context,
                            objectId,
                            messageKey,
                            messageKeys,
                            messageValues,
                            companyName,
                            (Locale) _locales.elementAt(i),
                            basePropFile));
                    
                    // separate the different language strings
                    messageBuffer.append("\n\n");
                }
            }
            // otherwise get message based on language
            else
            {
                messageBuffer.append(MessageUtil.getMessage(context, objectId, messageKey, messageKeys, messageValues, companyName, locale, basePropFile));
            }

            MQLCommand mql = new MQLCommand();
            mql.open(context);
            mql.executeCommand(context, "get env global MX_TREE_MENU");
            String paramSuffix = mql.getResult();
            if (paramSuffix != null &&
                !"".equals(paramSuffix) &&
                !"\n".equals(paramSuffix))
            {
                mql.executeCommand(context, "unset env global MX_TREE_MENU");
            }

             String inBoxTaskId =null;
             String sTempObjId = null;
             DomainObject doTempObj = DomainObject.newInstance(context);

            // If the base URL and object id list are available,
            // then add urls to the end of the message.
            if ( (_baseURL != null && ! "".equals(_baseURL)) &&
                (objectIdList != null && objectIdList.size() != 0) )
            {
                // Prepare the message for adding urls.
                messageBuffer.append("\n");
                Iterator i = objectIdList.iterator();
                while (i.hasNext())
                {
                    sTempObjId= (String)i.next();

                    if( (sTempObjId != null) && (!sTempObjId.equals("")))
                    {
                        try{

                                doTempObj.setId(sTempObjId);

                                if( (doTempObj.getInfo(context,DomainConstants.SELECT_TYPE)).equals(DomainConstants.TYPE_INBOX_TASK)){

                                    inBoxTaskId = sTempObjId;
                                    break;
                                }
                        }catch(Exception ex){System.out.println("exception in box sendNotification "+ex); }
                    }
                    // Add the url to the end of the message.
                    messageBuffer.append("\n").append(_baseURL).append("?objectId=").append(sTempObjId);
					//commented for the bug 332857 
					/*
                    if (paramSuffix != null && !"".equals(paramSuffix) && !"null".equals(paramSuffix) && !"\n".equals(paramSuffix)){
                    message += "&treeMenu=" + paramSuffix;
                    } 
					*/
					//commented for the bug 332857 

                }
            }

            // If is inbox task the message has to be modified accordingly.
            if(inBoxTaskId != null && !inBoxTaskId.equals("")){
                messageBuffer.append("\n").append(getInboxTaskMailMessage(context, inBoxTaskId, locale, basePropFile, _baseURL, paramSuffix));
            }
            //Till here
            // Send the mail message.
            sendMail(context,
                     to,
                     cc,
                     bcc,
                     subject,
					 //Added for bug 344780
                     messageBuffer.toString(),
                     objectIdList);
        }
    }


    /**
     * This method sends an icon mail notification to the specified users.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param toList StringList that contains the list of users to notify
     * @param ccList StringList that contains the list of users to cc
     * @param bccList StringList that contains the list of users to bcc
     * @param subject String that contains the notification subject
     * @param message String that contains the notification message
     * @param objectIdList StringList that contains the ids of objects to send with the notification
     * @throws Exception if the operation fails
     * @since AEF 9.5.0.0
     */

    protected static void sendMail(Context context,
                                 StringList toList,
                                 StringList ccList,
                                 StringList bccList,
                                 String subject,
                                 String message,
                                 StringList objectIdList)
        throws Exception
    {

        // viewing Icon mail in application '<' and '>' are read as tags by html
        // These char's need to be eliminated before sending the mail message.
        subject = subject.replace('<', ' ');
        subject = subject.replace('>', ' ');
        message = message.replace('<', ' ');
        message = message.replace('>', ' ');


        // Create iconmail object.
        IconMail mail = new IconMail();
        mail.create(context);
        
        StringList modifiedToList = new StringList(toList);
        String isaPerson = "";
        String iconMailEnabled="";
        String personEmail = "";
        Iterator itr = toList.iterator();
        while (itr.hasNext())
        {
            String name = (String)itr.next();
            try{
                  isaPerson = MqlUtil.mqlCommand(context, "print User $1 select $2 dump",name,"isaperson");
                   if("TRUE".equalsIgnoreCase(isaPerson)){
                       iconMailEnabled = MqlUtil.mqlCommand(context, "print person $1 select $2 dump",name,"iconmailenabled");
                    }
             }
            catch(MatrixException e){
            }
            personEmail = PersonUtil.getEmail(context, name);
            if((personEmail== null || "null".equals(personEmail) || "".equals(personEmail) ) && "FALSE".equals(iconMailEnabled) )
            {
                try{
                	modifiedToList.removeElement(name);
                }
                catch(Exception e){
                }
            }
         }

        if(modifiedToList.size()<0)
        {
            return;
        }

        // Set the "to" list.
          mail.setToList(modifiedToList);


        // Set the "cc" list.
        if (ccList != null)
        {
            mail.setCcList(ccList);
        }

        // Set the "bcc" list.
        if (bccList != null)
        {
            mail.setBccList(bccList);
        }

        // Set the object list.  If the object id list is available,
        // then send the objects along with the notification.
        if (objectIdList != null && objectIdList.size() != 0)
        {
            BusinessObjectList bol =
                    new BusinessObjectList(objectIdList.size());

            Iterator i = objectIdList.iterator();
            while (i.hasNext())
            {
                String id = (String) i.next();
                BusinessObject bo = new BusinessObject(id);
                bo.open(context);
                bol.addElement(bo);
            }

            mail.setObjects(bol);
        }

        // Set the message.
        mail.setMessage(message);

        boolean isContextPushed = false;
        emxContextUtil_mxJPO utilityClass = new emxContextUtil_mxJPO(context, null);
        // Test if spoofing should be performed on the "from" field.
        String agentName = getAgentName(context, null);
        if (agentName != null && !agentName.equals("") && !context.getUser().equals(agentName))
        {
            try
            {
                // Push Notification Agent
                String[] pushArgs = {agentName};
                utilityClass.pushContext(context, pushArgs);
                isContextPushed = true;
            }
            catch (Exception ex)
            {
            }
        }

        // Set the subject and send the iconmail.
        mail.send(context, subject);

        if (isContextPushed == true)
        {
            // Pop Notification Agent
            utilityClass.popContext(context, null);
        }
    }

    /**
     * This method gets a string for the specified key and company.
     *
     * @param basePropFileName String that contains the property file to search
     * @param key String that contains the key to use in the search
     * @param companyName String that contains the company name for the given key
     * @param locale <code>Locale</code> used to identify the bundle to use
     * @return a string matching the key
     * @since AEF 9.5.3.0
     * @deprecated AEF 10.5.0.1, use MessageUtil.getString
     */

    static public String getString(String basePropFileName, String key, String companyName, Locale locale)
    {
        return (MessageUtil.getString(basePropFileName, key, companyName, locale));
    }

    /**
     * This method gets a string for the specified key and company.
     *
     * @param key String that contains the key to use in the search
     * @param companyName String that contains the company name for the given key
     * @param locale <code>Locale</code> used to identify the bundle to use
     * @return a string matching the key
     * @since AEF 9.5.1.0
     * @deprecated AEF 10.5.0.1, use MessageUtil.getString
     */

    static public String getString(String key, String companyName, Locale locale)
    {
        return (MessageUtil.getString(key, companyName, locale));
    }

    /**
     * This method gets the locale for given context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @return locale object
     * @since AEF 9.5.1.1
     */

    public static Locale getLocale(Context context)
    {

        String result = _languages.trim();

        if (result.length() == 0)
        {
            result = context.getSession().getLanguage();
        }
        else
        {
            // in case they put more than one language in the property,
            // only use the first one
            int index = result.indexOf(' ');

            if (index != -1)
            {
                result = result.substring(0, index);
            }
        }

        return (getLocale(result));
    }

    /**
     * This method gets a locale given a language string.
     *
     * @param language String contains a portion of the language property
     * @return the locale object
     * @since AEF 9.5.1.3
     * @deprecated AEF 10.5.0.1, use MessageUtil.getLocale
     */

    public static Locale getLocale(String language)
    {
      return (MessageUtil.getLocale(language));
    }

    /**
     * This method gets the locales for a given language string.
     *
     * @param languages String contains the languages, space separated
     * @return a vector of Locale objects
     * @since AEF 9.5.1.3
     */

    public static Vector getLocales(String languages)
    {

        // use a hashtable to prevent duplicates
        Hashtable localeMap = null;
        // need this list to maintain the order of the languages
        StringList localeNames = null;
        // vector of locales
        Vector locales = null;

        if (languages.length() > 0)
        {
            StringTokenizer st1 = new StringTokenizer(languages);
            int count = st1.countTokens();

            if (count > 0)
            {
                localeMap = new Hashtable(count);
                localeNames = new StringList(count);

                while (st1.hasMoreTokens())
                {
                    String locale = st1.nextToken();
                    localeMap.put(locale, getLocale(locale));
                    localeNames.addElement(locale);
                }
            }
        }

        if (localeNames != null)
        {
            locales = new Vector(localeNames.size());

            StringItr itr = new StringItr(localeNames);
            while (itr.next())
            {
                locales.add(localeMap.get(itr.obj()));
            }
        }

        return (locales);
    }

    /**
     * This method returns a processed and translated message for a given key.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param messageKey String that contains the notification message key
     * @param messageKeys String array of message place holder keys
     * @param messageValues String array of message place holder values
     * @param companyName String that is used for company-based messages
     * @param locale the <code>Locale</code> used to determine language
     * @param basePropFileName String that is used to identify property file.
     * @return a String containing the message
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     * @deprecated AEF 10.5.0.1, use MessageUtil.getMessage
     */

    public static String getMessage(Context context,
                                     String messageKey,
                                     String[] messageKeys,
                                     String[] messageValues,
                                     String companyName,
                                     Locale locale,
                                     String basePropFileName)
        throws Exception
    {
      return MessageUtil.getMessage(context,messageKey,messageKeys,messageValues,companyName,locale,basePropFileName);
    }

    /**
     * This method returns a processed and translated message for a given key.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId String contains id of the object
     * @param messageKey String that contains the notification message key
     * @param messageKeys String array of message place holder keys
     * @param messageValues String array of message place holder values
     * @param companyName String that is used for company-based messages
     * @param locale the <code>Locale</code> used to determine language
     * @param basePropFileName String that is used to identify property file.
     * @return a String containing the message
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     * @deprecated AEF 10.5.0.1, use MessageUtil.getMessage
     */

    public static String getMessage(Context context,
                                     String objectId,
                                     String messageKey,
                                     String[] messageKeys,
                                     String[] messageValues,
                                     String companyName,
                                     Locale locale,
                                     String basePropFileName)
        throws Exception
    {

        return MessageUtil.getMessage(context,objectId,messageKey,messageKeys,messageValues,companyName,locale,basePropFileName);
    }

    /**
     * This method returns a processed and translated message for a given key.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param messageKey String contains the notification message key
     * @param messageKeys String array of message place holder keys
     * @param messageValues String array of message place holder values
     * @param companyName String that is used for company-based messages
     * @param locale the <code>Locale</code> used to determine language
     * @return a String containing the message
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     * @deprecated AEF 10.5.0.1, use MessageUtil.getMessage
     */

    public static String getMessage(Context context,
                                     String messageKey,
                                     String[] messageKeys,
                                     String[] messageValues,
                                     String companyName,
                                     Locale locale)
        throws Exception
    {
        return MessageUtil.getMessage(context,messageKey,messageKeys,messageValues,companyName,locale);
    }

    /**
     * This method sets the Global RPE variable for TreeMenu parameter used in
     * mail notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param treeMenuArgs String array
            0 - a String containing the tree menu name to be set
     * @throws Exception if the operation fails
     * @see #getTreeMenuName
     * @since AEF 9.5.2.0
     */

    public static void setTreeMenuName(Context context, String[] treeMenuArgs)
        throws Exception
    {

        String treeMenu = null;
        if( treeMenuArgs != null && treeMenuArgs.length > 0 )
        {
            treeMenu = treeMenuArgs[0];
        }

        if (treeMenu != null && !"null".equals(treeMenu) && !"".equals(treeMenu))
        {
            MQLCommand mql = new MQLCommand();
            mql.open(context);
            String mqlCommand = "set env global $1 $2";
            mql.executeCommand(context, mqlCommand,"MX_TREE_MENU", treeMenu);
            mql.close(context);
        }
    }

    /**
     * This method unsets the Global RPE variable for TreeMenu parameter used in
     * mail notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 9.5.2.0
     */

    public static void unsetTreeMenuName(Context context, String[] args)
        throws Exception
    {
        MQLCommand mql = new MQLCommand();
        mql.open(context);
        String mqlCommand = "unset env global MX_TREE_MENU";
        mql.executeCommand(context, mqlCommand);
        mql.close(context);
    }


    /**
     * This method returns the Global RPE variable for TreeMenu parameter used in
     * mail notifications.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return a String contains treeMenu, the value of TreeMenu parameter to be set
     * @throws Exception if the operation fails
     * @see #setTreeMenuName
     * @since AEF 9.5.2.0
     */

    public static String getTreeMenuName(Context context, String[] args)
        throws Exception
    {
        MQLCommand mql = new MQLCommand();
        mql.open(context);
        String mqlCommand = "get env global MX_TREE_MENU";
        mql.executeCommand(context, mqlCommand);
        String treeMenu = mql.getResult();
        mql.close(context);
        return treeMenu;
    }

    /**
     * This method gets the names and language preferences from the list.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param names from the list along with language preference will be added to this map
     * @param languages language preferences will be added to this map
     * @param list the list of recipients
     * @since AEF 9.5.3.0
     */

    protected static void getNamesAndLanguagePreferences(Context context,
                                                HashMap names,
                                                HashMap languages,
                                                StringList list)
    {

        if (list != null)
        {
            StringItr itr = new StringItr(list);
            while (itr.next())
            {
                if (!names.containsKey(itr.obj()))
                {
                    String language = "";
                    try
                    {
                        language = PropertyUtil.getAdminProperty(context, "user", itr.obj(), PersonUtil.PREFERENCE_ICON_MAIL_LANGUAGE);

                        if(UIUtil.isNullOrEmpty(language))
                        {
                          language = PersonUtil.getLanguageDefault(context);
                        }
                    } catch (Exception ex) {
                      language = PersonUtil.getLanguageDefault(context);
                    }
                    names.put(itr.obj(), language);

                    languages.put(language, "");
                }
            }
        }

    }

    /**
     * This method gets names from the list with given language preference.
     *
     * @param list the list of recipients
     * @param namesAndLanguages map of key names and value language
     * @param language String holds the language we are filtering on
     * @return a StringList contains list of recipients expecting a message in the given language
     * @since AEF 9.5.3.0
     */

    protected static StringList getNamesForLanguage(StringList list,
                                           HashMap namesAndLanguages,
                                           String language)
    {
        StringList names = null;

        if (list != null)
        {
            names = new StringList(list.size());

            StringItr itr = new StringItr(list);
            while (itr.next())
            {
                if (language.equals(namesAndLanguages.get(itr.obj())))
                {
                    names.addElement(itr.obj());
                }
            }
        }

        return (names);
    }


    /**
     * This method is used to format the Notification message of an InBox task.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId String containing id of the object
     * @param locale the <code>Locale</code>  used to determine language
     * @param Bundle String contains resource Bundle value
     * @param baseURL String contains url
     * @param paramSuffix formatted String which is to be appended to the return string
     * @return a String of formated message of an Inbox Task
     * @throws Exception if the operation fails
     * @since AEF 9.5.3.0
     */

    public static String getInboxTaskMailMessage(Context context,String objectId, Locale locale,String Bundle,String baseURL, String paramSuffix) throws Exception
    {
        StringBuffer msg = new StringBuffer();
        StringBuffer contentURL = new StringBuffer();
        String sAttrScheduledCompletionDate   = PropertyUtil.getSchemaProperty(context,"attribute_ScheduledCompletionDate");
        String strAttrCompletionDate ="attribute["+sAttrScheduledCompletionDate+"]";
        String sRelRouteTask                  = PropertyUtil.getSchemaProperty(context,"relationship_RouteTask");
        String routeIdSelectStr="from["+sRelRouteTask+"].to.id";

        String lang=null;
        String rsBundle=null;
        try{
            DomainObject task = DomainObject.newInstance(context,objectId);

            lang = locale.getLanguage();
            rsBundle = Bundle;

            StringList selectstmts = new StringList();
            selectstmts.add("attribute[" + DomainConstants.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]");
            selectstmts.add(strAttrCompletionDate);
            selectstmts.add(routeIdSelectStr);
            Map taskMap = task.getInfo(context,selectstmts);
            String routeId = (String)taskMap.get(routeIdSelectStr);
			if(routeId == null) { 
				routeId = (String)taskMap.get("id");
			}
            Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
            StringList selectBusStmts    = new StringList();
            selectBusStmts.add(DomainConstants.SELECT_ID);
            selectBusStmts.add(DomainConstants.SELECT_TYPE);
            selectBusStmts.add(DomainConstants.SELECT_NAME);
            selectBusStmts.add(DomainConstants.SELECT_REVISION);
            StringList selectRelStmts    = new StringList();


            DomainObject route = DomainObject.newInstance(context,routeId);
            MapList contentMapList = route.getRelatedObjects(context,
                                                            relPattern.getPattern(),
                                                            "*",
                                                             selectBusStmts,
                                                             selectRelStmts,
                                                             true,
                                                             true,
                                                             (short)1,
                                                             "",
                                                             "",
                                                             null,
                                                             null,
                                                             null);

            int size=contentMapList.size();

            if(size > 0)
            {
                msg.append("\n"+getTranslatedMessage(rsBundle,lang,"emxFramework.InboxTask.MailNotification.WhereContent.Message"));
                contentURL.append("\n"+getTranslatedMessage(rsBundle,lang,"emxFramework.InboxTask.MailNotification.ContentFindMoreURL"));
                Map contentMap=null;
                for(int i=0;i<size;i++)
                {
                    contentMap = (Map)contentMapList.get(i);
                    // Bug fix 292537 - added quotes and spaces
                    msg.append("'");
                    msg.append(contentMap.get(DomainConstants.SELECT_TYPE));
                    msg.append("' '");
                    msg.append(contentMap.get(DomainConstants.SELECT_NAME));
                    msg.append("' ");
                    msg.append(contentMap.get(DomainConstants.SELECT_REVISION));
                    msg.append("\n");
                    contentURL.append("\n" + baseURL + "?objectId=" +contentMap.get(DomainConstants.SELECT_ID));
                    //if (paramSuffix != null && !"".equals(paramSuffix) && !"null".equals(paramSuffix) && !"\n".equals(paramSuffix)){
                    //contentURL.append("&treeMenu=" + paramSuffix);
                    //}
                }
            }

            if(size <= 0)
                msg.append("\n");

            msg.append(getTranslatedMessage(rsBundle,lang,"emxFramework.InboxTask.MailNotification.TaskInstructions"));
            msg.append("\n");
            msg.append(taskMap.get("attribute[" + DomainConstants.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]"));
            msg.append("\n");
            msg.append(getTranslatedMessage(rsBundle,lang,"emxFramework.InboxTask.MailNotification.TaskDueDate"));
            msg.append("\n");
            msg.append(taskMap.get(strAttrCompletionDate));
            msg.append("\n");
            msg.append(getTranslatedMessage(rsBundle,lang,"emxFramework.InboxTask.MailNotification.TaskFindMoreURL"));
            msg.append("\n" + baseURL + "?objectId=" + task.getObjectId());
            if (paramSuffix != null && !"".equals(paramSuffix) && !"null".equals(paramSuffix) && !"\n".equals(paramSuffix)){
                msg.append("&treeMenu=" + paramSuffix);
            }
            msg.append("\n");
            msg.append(getTranslatedMessage(rsBundle,lang,"emxFramework.InboxTask.MailNotification.RouteFindMoreURL"));
            msg.append("\n" + baseURL + "?objectId=" +routeId);
            //if (paramSuffix != null && !"".equals(paramSuffix) && !"null".equals(paramSuffix) && !"\n".equals(paramSuffix)){
            //msg.append("&treeMenu=" + paramSuffix);
            //}
            //msg.append(contentURL.toString());
        }catch(Exception ex){ System.out.println(" error  in getInboxTaskMailMessage "+ex);}

        return msg.toString();
    }
    
    /**
     * This method is used to format the Notification message of an InBox task.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId String containing id of the object
     * @param locale the <code>Locale</code>  used to determine language
     * @param Bundle String contains resource Bundle value
     * @param baseURL String contains url
     * @param paramSuffix formatted String which is to be appended to the return string
     * @return a String of formated message of an Inbox Task
     * @throws Exception if the operation fails
     * @since AEF 9.5.3.0
     */

    public static String getInboxTaskMailInfo(Context context,String objectId, Locale locale,String Bundle,String baseURL, String paramSuffix) throws Exception
    {
        StringBuffer msg = new StringBuffer();
        StringBuffer contentURL = new StringBuffer();
        String sAttrScheduledCompletionDate   = PropertyUtil.getSchemaProperty(context,"attribute_ScheduledCompletionDate");
        String strAttrCompletionDate ="attribute["+sAttrScheduledCompletionDate+"]";
        String sRelRouteTask                  = PropertyUtil.getSchemaProperty(context,"relationship_RouteTask");
        String routeIdSelectStr="from["+sRelRouteTask+"].to.id";

        String lang=null;
        String rsBundle=null;
        try{
            DomainObject task = DomainObject.newInstance(context,objectId);

            lang = locale.getLanguage();
            rsBundle = Bundle;

            StringList selectstmts = new StringList();
            selectstmts.add(routeIdSelectStr);
            Map taskMap = task.getInfo(context,selectstmts);
            String routeId = (String)taskMap.get(routeIdSelectStr);
			if(routeId == null) { 
				routeId = (String)taskMap.get("id");
			}
            Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_OBJECT_ROUTE);
            StringList selectBusStmts    = new StringList();
            selectBusStmts.add(DomainConstants.SELECT_ID);
            selectBusStmts.add(DomainConstants.SELECT_TYPE);
            selectBusStmts.add(DomainConstants.SELECT_NAME);
            selectBusStmts.add(DomainConstants.SELECT_REVISION);
            StringList selectRelStmts    = new StringList();


            DomainObject route = DomainObject.newInstance(context,routeId);
            MapList contentMapList = route.getRelatedObjects(context,
                                                            relPattern.getPattern(),
                                                            "*",
                                                             selectBusStmts,
                                                             selectRelStmts,
                                                             true,
                                                             true,
                                                             (short)1,
                                                             "",
                                                             "",
                                                             null,
                                                             null,
                                                             null);

            int size=contentMapList.size();

            if(size > 0)
            {
                msg.append("<br>"+getTranslatedMessage(rsBundle,lang,"emxFramework.InboxTask.MailNotification.WhereContent.Message"));
                contentURL.append("<br>"+getTranslatedMessage(rsBundle,lang,"emxFramework.InboxTask.MailNotification.ContentFindMoreURL"));
                Map contentMap=null;
                for(int i=0;i<size;i++)
                {
                    contentMap = (Map)contentMapList.get(i);
                    // Bug fix 292537 - added quotes and spaces
                    msg.append("'");
                    msg.append(contentMap.get(DomainConstants.SELECT_TYPE));
                    msg.append("' '");
                    msg.append(contentMap.get(DomainConstants.SELECT_NAME));
                    msg.append("' ");
                    msg.append(contentMap.get(DomainConstants.SELECT_REVISION));
                    msg.append("<br>");
                    contentURL.append("<br>" + baseURL + "?objectId=" +contentMap.get(DomainConstants.SELECT_ID));
                    //if (paramSuffix != null && !"".equals(paramSuffix) && !"null".equals(paramSuffix) && !"\n".equals(paramSuffix)){
                    //contentURL.append("&treeMenu=" + paramSuffix);
                    //}
                }
            }

            if(size <= 0)
                msg.append("<br>");

            msg.append(getTranslatedMessage(rsBundle,lang,"emxFramework.InboxTask.MailNotification.TaskFindMoreURL"));
            msg.append("<br>" + baseURL + "?objectId=" + task.getObjectId());
            if (paramSuffix != null && !"".equals(paramSuffix) && !"null".equals(paramSuffix) && !"\n".equals(paramSuffix)){
                msg.append("&treeMenu=" + paramSuffix);
            }
            msg.append("<br>");
            msg.append(getTranslatedMessage(rsBundle,lang,"emxFramework.InboxTask.MailNotification.RouteFindMoreURL"));
            msg.append("<br>" + baseURL + "?objectId=" +routeId);
            //if (paramSuffix != null && !"".equals(paramSuffix) && !"null".equals(paramSuffix) && !"\n".equals(paramSuffix)){
            //msg.append("&treeMenu=" + paramSuffix);
            //}
            //msg.append(contentURL.toString());
        }catch(Exception ex){ System.out.println(" error  in getInboxTaskMailMessage "+ex);}

        return msg.toString();
    }

    /**
     * This method returns the translated message value.
     *
     * @param rsBundle String containing resouce bundle value
     * @param lang String specifying the language name
     * @param text String that holds the message
     * @return String that contains translated message value
     * @throws Exception if the operation fails
     * @since AEF 9.5.3.0
     * @deprecated AEF 10.5.0.1, use MessageUtil.getTranslatedMessage
     */

    public static String getTranslatedMessage(String rsBundle, String lang, String text) throws Exception
    {
        return MessageUtil.getTranslatedMessage(rsBundle, lang, text);
    }
    
 /**
     * Setting 
     * args[0] is Notification Agent Name, from feild configured to send notification
     * args[1] is _baseURL used to append in message body to provide hyperlink for objects
     * args[2] list of languages configured, message body will be in all these languages.
     *         (If user has no preference configured).  
     * 
     * @param context
     * @param args
     */    
    public void setMailConfigurations(Context context, String[] args) {
        if(args != null && args.length == 3) {
            synchronized (_lock) {
                if(!mailConfigurationsLoaded) {
                    _configuredNotificationAgentName = args[0];
                    _baseURL = args[1];
                    _languages = args[2];
                    _locales = getLocales(_languages.trim());
                    mailConfigurationsLoaded = true;
                }
            }
        }
    }
}
