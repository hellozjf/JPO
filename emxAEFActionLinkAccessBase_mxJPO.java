/*
**  emxAEFActionLinkAccessBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;
import matrix.db.JPO;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkLicenseUtil;
import com.matrixone.apps.domain.util.FrameworkException;

/**
 * The <code>emxAEFActionLinkAccessBase</code> class contains methods for ActionLinkAccess.
 *
 * @version AEF 10.0.Patch1.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxAEFActionLinkAccessBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0.Patch1.0
     */

    public emxAEFActionLinkAccessBase_mxJPO(Context context, String[] args)
      throws Exception
    {

    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 10.0.Patch1.0
     */

    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);
            Map paramList = (Map)paramMap.get("paramList");
            String languageStr = (String)paramList.get("languageStr");
            String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.DesktopClient", new Locale(languageStr));
            throw new Exception(exMsg);
        }
        return 0;
    }

    /**
     * This method checks whether Suite has Access to Action Link.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    SETTINGS - HashMap containing settings details
     *    AEFMyDeskSuiteDir - String containing AEFMyDeskSuiteDir name
     * @return <code>Boolean</code> object true if the Suite has Access to Action Link and false otherwise.
     * @throws Exception if the operation fails
     * @since AEF 10.0.Patch1.0
     */
    
  public boolean displayAEFFreezeExpandlevelFilterMenu(Context context,String[] args) throws Exception {

    	HashMap requestMap  = (HashMap)JPO.unpackArgs(args);
    	boolean displayAEFFreezeExpandlevelFilterMenu= false;
    	String displayView = (String)requestMap.get("displayView");
    	String uiType = (String)requestMap.get("uiType");
    	String expandFilter = (String)requestMap.get("expandFilter");
    	if((requestMap.containsKey("expandProgram") || requestMap.containsKey("expandProgramMenu")) && !("thumbnail".equalsIgnoreCase(displayView)) && "structureBrowser".equalsIgnoreCase(uiType) && !"false".equalsIgnoreCase(expandFilter)) {
    		displayAEFFreezeExpandlevelFilterMenu =true;
    	} 
    	return  displayAEFFreezeExpandlevelFilterMenu;
    }


  public Boolean hasAccessToActionLink(Context context,String[] args)
    throws Exception
  {

       HashMap paramMap  = (HashMap)JPO.unpackArgs(args);
       Boolean emxAccess = Boolean.valueOf(true);
       HashMap allSettings    = (HashMap) paramMap.get("SETTINGS");
       String registeredSuite =  "";
       String registeredDir   = "";

       try {
          String emxAEFMyDeskSuite = (String)paramMap.get("AEFMyDeskSuiteDir");

            if(allSettings != null)
            {
                 registeredSuite= (String)allSettings.get("Registered Suite");
                if (registeredSuite != null && registeredSuite.length() > 0)
                {
                    registeredDir = getRegisteredDirectory(context, registeredSuite);
                }
            }

            if(emxAEFMyDeskSuite != null && registeredDir != null && !emxAEFMyDeskSuite.equalsIgnoreCase(registeredDir))
            {
                emxAccess = Boolean.valueOf(false);
            }

      }catch (Exception e) {
          throw new Exception(e.toString());
       }

      return emxAccess;
  }

    /**
     * This method gets the Registered Directory of the suite name.
     *
     * @param suiteName String holding the suite name
     * @return String containing the name of the registered directory
     * @throws FrameworkException if the operation fails
     * @since AEF 10.0.Patch1.0
     * @deprecated since V6R2014 for Function_026045. use getRegisteredDirectory(Context context, String suiteName)
     */

    public static String getRegisteredDirectory(String suiteName)
        throws FrameworkException{
    	return getRegisteredDirectory(null, suiteName);
    }
    
    public static String getRegisteredDirectory(Context context, String suiteName) throws FrameworkException{
        String regDirectory = "";

        try {
            if ((suiteName != null) && (suiteName.length() > 0)){
                StringBuffer PROPERTYKEY = new StringBuffer(50);
                PROPERTYKEY.append(suiteName);
                PROPERTYKEY.append(".Directory");                

                try {
                    regDirectory = EnoviaResourceBundle.getProperty(context, PROPERTYKEY.toString());
                } catch (Exception e1) {
                    PROPERTYKEY = new StringBuffer(50);
                    PROPERTYKEY.append("eServiceSuite");
                    PROPERTYKEY.append(suiteName);
                    PROPERTYKEY.append(".Directory");;
                    regDirectory = EnoviaResourceBundle.getProperty(context, PROPERTYKEY.toString());
                }
            }
        } catch (Exception ex) {
            throw (new FrameworkException("getRegisteredDirectory : " + ex.toString()) );
        }

        return regDirectory;
    }
    
    public  boolean isX3DCSMAUser(Context context, String args[]) throws FrameworkException{
    	boolean cpfUser = false;
		try {
		    FrameworkLicenseUtil.checkLicenseReserved(context, "ENO_BPS_TP");
		    cpfUser = true;
		}
		catch (Exception e)
		{
		    cpfUser = false;
		}

    	if(cpfUser){
    		return false;
    	}
    	return FrameworkLicenseUtil.isX3DCSMAUser(context);    	
    }

}
