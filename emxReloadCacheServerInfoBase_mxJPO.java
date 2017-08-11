/*   emxReloadCacheServerInfoBase
**
**   Copyright (c) 2004-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the properties to remotely reload cache
**
*/

import java.util.Locale;

import matrix.db.Context;
import com.matrixone.apps.domain.util.*;

/**
 * The <code>emxReloadCacheServerInfoBase</code> class contains implementation
 * code for properties to remotely reload cache.
 *
 * @version 10.5.SP1 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxReloadCacheServerInfoBase_mxJPO
{

   /**
    * List of App Server stored in a String array.
    */
   public String[] APP_SERVER_LIST = null;

   /**
    * List of RMI Server stored in a String array.
    */
   public String[] RMI_SERVER_LIST = null;

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.5.SP1
     */
    public emxReloadCacheServerInfoBase_mxJPO (Context context, String[] args)
        throws Exception
    {
       // super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 10.5.SP1
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            String languageStr = context.getSession().getLanguage();
            String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.Invocation", new Locale(languageStr));            
            exMsg += EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.ReloadCacheServerInfoBase", new Locale(languageStr));           
            throw new Exception(exMsg);
        }
        return 0;
    }

    /**
    * Get the list of Application servers configured in properties file.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return String array which contains the list of App servers
    * @throws Exception if the operation fails
    * @since AEF 10.5.SP1
    */
    public String[] getAppServerList(Context context, String[] args)
        throws Exception
    {
        return APP_SERVER_LIST;
    }

    /**
    * Return the list of RMI gateway servers entered here.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return String array which contains the list of RMI gateway servers
    * @throws Exception if the operation fails
    * @since AEF 10.5.SP1
    */
    public String[] getRMIServerList(Context context, String[] args)
        throws Exception
    {
        return RMI_SERVER_LIST;
    }

}
