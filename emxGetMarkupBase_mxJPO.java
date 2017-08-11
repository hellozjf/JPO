/*
 **  emxGetMarkupBase.java
 **
 **  Copyright (c) 1992-2016 Dassault Systemes.
 **
 **  All Rights Reserved.
 **  This program contains proprietary and trade secret information of
 **  MatrixOne, Inc.  Copyright notice is precautionary only and does
 **  not evidence any actual or intended publication of such program.
 **
 **  static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.11 Wed Oct 22 16:02:29 2008 przemek Experimental przemek $
 */

import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.BusinessObject;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import com.matrixone.apps.domain.util.XSSUtil;

/**
*
* @exclude
*/
public class emxGetMarkupBase_mxJPO {

    public emxGetMarkupBase_mxJPO (Context context, String[] args)
      throws Exception
    {

    }

    /**
     * Getting Markup link.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing one String entry for key "objectId"
     * @throws Exception if the operation fails
     * @since AEF
     */
    public static Vector getMarkupLink(Context context, String[] args)
       throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        HashMap paramMap = (HashMap)programMap.get("paramList");
        Vector returnList = new Vector();

        if(relBusObjPageList != null)
        {
            for (int i = 0; i < relBusObjPageList.size(); i++) {
                String id = (String)((HashMap)relBusObjPageList.get(i)).get("id");
                BusinessObject bo = new BusinessObject(id);
                String format = getFormat(context, id);
                String linkString = "";
                try {
                    bo.open(context);
                    String name = bo.getName();
                    bo.close(context);
                    String servlet = getServlet(context, format);
                    String servletAlt = getServletAlt(context, format);
                    linkString = "<A HREF='/servlet/" +
                      servlet  +
                      "?mid=" +
                      XSSUtil.encodeForURL(context, id) +
                      "' TARGET='_blank'>" +
                      "<IMG SRC='images/iconActionCreateMarkup.gif' BORDER='0' ALT='" +
                      servletAlt +
                      "'>" + XSSUtil.encodeForURL(context,name) + "</A>";
                }
                catch (Exception e)
                {
                }
                returnList.add(linkString);
            }
        }
        return returnList;
    }

    /**
     * Getting Format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param id - hold object id.
     * @since AEF
     */
    protected static String getFormat(Context context, String id)
    {
        String strCmd       = "print businessobject $1 select format dump $2";
        String result       = "";
        try {
            result                          = MqlUtil.mqlCommand(context, strCmd, id, "|");
            StringTokenizer formatTokenizer = new StringTokenizer(new StringTokenizer(result, "\n").nextToken(), "|");
            return formatTokenizer.nextToken();
        }
        catch(Exception e)
        {
            return "";
        }
    }

    /**
     * Getting Servlet
     *
     * @param context the eMatrix <code>Context</code> object
     * @param format - holds format.
     * @throws Exception if the operation fails
     * @since AEF
     */
    protected static String getServlet(Context context, String format) throws Exception
    {
        String cmd                          = "exec program $1 $2";
        String result                       = MqlUtil.mqlCommand(context, cmd, "eServicecommonGetViewers.tcl", format);
        StringTokenizer servletTokenizer    = new StringTokenizer(result , "|");
        servletTokenizer.nextToken();
        return servletTokenizer.nextToken();
    }

    /**
     * Getting Servlet
     *
     * @param context the eMatrix <code>Context</code> object
     * @param format - holds format.
     * @throws Exception if the operation fails
     * @since AEF
     */
    protected static String getServletAlt(Context context, String format) throws Exception
    {
        String cmd                          = "exec program $1 $2";
        String result                       = MqlUtil.mqlCommand(context, cmd, "eServicecommonGetViewers.tcl", format);
        StringTokenizer servletTokenizer    = new StringTokenizer(result, "|");
        servletTokenizer.nextToken();
        servletTokenizer.nextToken();
        return servletTokenizer.nextToken();
    }
}
