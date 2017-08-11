/*
 *  emxTeamAccessBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.common.*;

/**
 * @version Team 10-5 Release - Copyright (c) 2004, MatrixOne, Inc.
 */

public class emxTeamAccessBase_mxJPO extends emxDomainObject_mxJPO
{
  protected static final String sDirectory = ".Directory";


    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Team 10-5
     */
    public emxTeamAccessBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

  /**
   * This method will be called when ever we invoke the JPO without 
   * calling any method.explicitly
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no value
   * @return int
   * @throws Exception if the operation fails
   * @since Team 10-5
   */
  public int mxMain(Context context, String[] args)
    throws Exception
  {
    if (!context.isConnected())
      throw new Exception(ComponentsUtil.i18nStringNow("emxTeamCentral.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
      return 0;
  }

  protected StringList getInstalledSuites(Context context, String[] args)
    throws Exception
  {
    try {
        StringList slInstalledSuites = new StringList(10);

      String sResults = MqlUtil.mqlCommand(context, "execute program $1", "eServiceHelpAbout.tcl");
      sResults = sResults.trim();
      
      String[] splitResultArray = sResults.split("\\|", -1); //get trailing empty strings also
      
      String sErrorCode = splitResultArray[0].trim();
      if (sErrorCode.equals("1")){  // internal failure of tcl program
         String sError = sResults.substring(sResults.indexOf("|") + 1);
         throw new MatrixException(sError);
      }

      for(int i=1; i<splitResultArray.length; i+=2){
    	  slInstalledSuites.addElement(splitResultArray[i].trim().toLowerCase());
      }
      
    return slInstalledSuites;
    } catch (Exception e){
      throw new MatrixException(e.toString());
    }

  }

  /**
   * isSourcingInstalled - This method is used to determine if SourcingCentral
   *                       is installed.
   * @param context the eMatrix <code>Context</code> object
   * @param args empty
   * @return boolean
   * @throws Exception if the operation fails
   * @since Team 10-5
   */
  public boolean isSourcingInstalled(Context context, String[] args)
    throws Exception
  {
      StringList slInstalledSuites = getInstalledSuites(context, args);

      String suiteSourcingCentral  = EnoviaResourceBundle.getProperty(context,"emxFramework.UISuite.SourcingCentral");

      StringBuffer suiteSourcingCentralDir  = new StringBuffer(64);
      suiteSourcingCentralDir.append(suiteSourcingCentral);
      suiteSourcingCentralDir.append(sDirectory);

      return slInstalledSuites.contains(EnoviaResourceBundle.getProperty(context,suiteSourcingCentralDir.toString()));
  }

  /**
   * isEngineeringInstalled - This method is used to determine if
   *                          EngineeringCentral is installed.
   * @param context the eMatrix <code>Context</code> object
   * @param args empty
   * @return boolean
   * @throws Exception if the operation fails
   * @since Team 10-5
   */
  public boolean isEngineeringInstalled(Context context, String[] args)
    throws Exception
  {
      StringList slInstalledSuites = getInstalledSuites(context, args);

      String suiteEngineeringCentral = EnoviaResourceBundle.getProperty(context,"emxFramework.UISuite.EngineeringCentral");

      StringBuffer suiteEngineeringCentralDir = new StringBuffer(64);
      suiteEngineeringCentralDir.append(suiteEngineeringCentral);
      suiteEngineeringCentralDir.append(sDirectory);
  
      return slInstalledSuites.contains(EnoviaResourceBundle.getProperty(context,suiteEngineeringCentralDir.toString()));
  }

}
