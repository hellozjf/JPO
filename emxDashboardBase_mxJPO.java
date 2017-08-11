/*
**  emxDashboardBase.java
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
**   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.8.2.1 Thu Dec  4 07:54:59 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.8 Wed Oct 22 15:50:36 2008 przemek Experimental przemek $
*/
import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.text.*;
import java.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

/*****************************************************************************************
*       New JPO for Config Table Conversion Task
*******************************************************************************************/
/**
 * The <code>emxDashboardBase</code> class represents the Dashboard JPO
 * functionality for the AEF type.
 *
 * @version PMC 10-6 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxDashboardBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public emxDashboardBase_mxJPO(Context context, String[] args)
    {
        // Call the super constructor
        super();
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0 for success and non-zero for failure
     * @throws Exception if the operation fails
     */
    public int mxMain(Context context, String[] args)
      throws Exception
    {
        if (!context.isConnected())
            throw new Exception("not supported on desktop client");
        return 0;
    }

    /**
     * This method gets the List of Dashboards depending on the context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return MapList contains list of Dashboards
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getDashboards(Context context, String[] args)
      throws Exception
    {
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        MapList dashboardMapList = new MapList();
        String dashboardName = "";
        long dashboardCount = 0L;
        int i = 0;

        try
        {
            SetList dashboardList = matrix.db.Set.getSets(context, true);
            String command = "";

            if (!dashboardList.isEmpty())
            {
                Iterator dbList = dashboardList.iterator();
                while (dbList.hasNext())// loop through each set owned by the user
                {
                    matrix.db.Set thisSet = (matrix.db.Set) dbList.next();
                    dashboardName = thisSet.toString();
                    String dashboardId = dashboardName;
                    if (dashboardName.startsWith(".dashboard-"))
                    {
                       //PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:start
                    	command = "list property on set $1";
                        String output =  MqlUtil.mqlCommand(context, command,dashboardName); 
                        //PRG:RG6:R213:Mql Injection:parameterized Mql:24-Oct-2011:End
                        
                        dashboardName = dashboardName.substring(11);
                        HashMap dashboardMap = new HashMap();
                        dashboardCount = thisSet.count(context);
                        Long countLong = new java.lang.Long(dashboardCount);

                        i = 0;
                        i = output.indexOf(".dashboard-", i);
                        String dbDesc = "";
                        if (i != -1)
                        {//if Description is present
                            i += 11;
                            int endNameIndex = output.indexOf("value", i);
                            int descBeginIndex = endNameIndex + 6;
                            dbDesc = output.substring(descBeginIndex,
                                    output.length());
                        }
                        else
                        {//if Description is not present
                            dbDesc = "";
                        }

                        //Add values to map and finally onto a maplist.  HTML will use this.
                        dashboardMap.put("id", dashboardId);
                        dashboardMap.put("name", dashboardName);
                        dashboardMap.put("dashboardCount", countLong.toString());
                        dashboardMap.put("dashboardDesc", dbDesc);
                        dashboardMapList.add((Map) dashboardMap);
                    }
                    //End if
                }
                // End while
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return dashboardMapList;
        }
    }

    /**
     * This method gets the Dashboard Names.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList contains a MapList.
     * @return Vector contains list of Dashboard names
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector getName(Context context, String[] args)
      throws Exception
    {
        HashMap programMap          = (HashMap) JPO.unpackArgs(args);
        Map paramList               = (Map) programMap.get("paramList");
        String strSuiteDir          = (String) paramList.get("SuiteDirectory");
        MapList relBusObjPageList   = (MapList) programMap.get("objectList");
        Vector vecName              = new Vector(relBusObjPageList.size());
        String strURL               = "../programcentral/emxProgramCentralDashboardsDetailsFS.jsp?dashboardName=";
        String imageStr             = "../common/images/iconSmallDashboard.gif";

        String strDashBoardName     = "";
        //Added for Bug#338897 - Start
        boolean isPrinterFriendly = false;
        String strPrinterFriendly = (String)paramList.get("reportFormat");
        if ( strPrinterFriendly != null ) {
            isPrinterFriendly = true;
        }
        //Added for Bug#338897 - End
        try
        {
            for (int i = 0; i < relBusObjPageList.size(); i++)
            {
                Map relBusObjMap = (Map) relBusObjPageList.get(i);
                String strName = (String) relBusObjMap.get("name");
                StringBuffer sbOutput = new StringBuffer();
                sbOutput.append("<img src=\"" + imageStr + "\" border=\"0\">");
                //Added for Bug#338897 - Start
                if(!isPrinterFriendly)
                {
                //Replace the ' with *, since * cannot be used in name field.bug# 310787
                  strDashBoardName      = FrameworkUtil.findAndReplace(strName,"'","*");
                  sbOutput.append("<a href=\"javascript:emxTableColumnLinkClick('" + strURL);
                                sbOutput.append(strDashBoardName);
                                sbOutput.append("&emxSuiteDirectory=");
                                sbOutput.append(strSuiteDir);
                                sbOutput.append("', '600', '600', 'false', 'content','')\"  class='object'>");
                                sbOutput.append(strName);
                                sbOutput.append("</a>");
                }
                else
                {
                  sbOutput.append(strName);
                }
                vecName.addElement(sbOutput.toString());
                //Added for Bug#338897 - End
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return vecName;
        }
    }

    /**
     * This method gets the Description of Dashboards.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList contains a MapList
     * @return Vector containing list of Dashboards description
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector getDescription(Context context, String[] args)
      throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get("objectList");
        Vector vecDesc = new Vector(relBusObjPageList.size());
        try
        {
            for (int i = 0; i < relBusObjPageList.size(); i++)
            {
                Map relBusObjMap = (Map) relBusObjPageList.get(i);
                String strDesc = (String) relBusObjMap.get("dashboardDesc");
                vecDesc.addElement(strDesc);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return vecDesc;
        }
    }

    /**
     * This method gets the Count of Dashboards.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    objectList contains a MapList
     * @return Vector containing list of Dashboards Count
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    public Vector getCount(Context context, String[] args)
      throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get("objectList");
        Vector vecCount = new Vector(relBusObjPageList.size());
        try
        {
            for (int i = 0; i < relBusObjPageList.size(); i++)
            {
                Map relBusObjMap = (Map) relBusObjPageList.get(i);
                String strCount = (String) relBusObjMap.get("dashboardCount");
                vecCount.addElement(strCount);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return vecCount;
        }
    }
}

