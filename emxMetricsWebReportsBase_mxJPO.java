/*
**  emxMetricsWebReportsBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

    import matrix.db.Context;
    import matrix.db.WebReport;
    import matrix.db.WebReportList;
    import matrix.db.JPO;

    import java.util.Vector;
    import java.util.HashMap;
    import java.util.Iterator;
    import java.util.Calendar;
    import java.util.Date;

    import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.XSSUtil;
    import com.matrixone.apps.metrics.MetricsReports;
    import com.matrixone.apps.domain.util.FrameworkException;
    /**
     * The <code>emxMetricsWebReportsBase</code> class contains methods for the "WebReport" Common Component.
     *
     * @version AEF 10.6 - Copyright (c) 2005, MatrixOne, Inc.
     */

    public class emxMetricsWebReportsBase_mxJPO
    {
        /** String to indicate webreport name */
        private static final String WEBREPORT_NAME = "name";

        /** String to indicate when the webreport was last executed */
        private static final String LAST_RUN = "lastrun";

        /** String to indicate the Completed In time of the webreport */
        private static final String COMPLETED_IN = "completedIn";

        /** String to indicate webreport Owner */
        private static final String OWNER = "owner";

        /** String to indicate webreports Owned by the logged in user */
        private static final String OWNED = "owned";

        /** String to indicate the last instance of the webreport  */
        private static final String LAST_INSTANCE = ".last";

        /** String to indicate Object List */
        private static final String OBJECT_LIST = "objectList";

        /** String to indicate webreport's 'reporttype' attribute */
        private static final String REPORT_TYPE = "reporttype";

        /** String to indicate the 'All' filter */
        private static final String FILTER_ALL = "All";

        /** String to indicate the 'Owned' filter */
        private static final String FILTER_OWNED = "Owned";

        /** String to indicate the 'Owned' filter */
        private static final String FILTER_SHARED = "Shared";

        /** String to indicate the 'SHARED' key */
        private static final String KEY_SHARED = "SHARED";

        /**
         * Constructor.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds no arguments
         * @throws Exception if the operation fails
         * @since AEF 10.0.Patch1.0
         */

        public emxMetricsWebReportsBase_mxJPO(Context context, String[] args)
          throws Exception
        {
            //super(context, args);
        }

        /**
         * This method is executed if a specific method is not specified.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds no arguments
         * @return int 0, status code.
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
         * This method serves a generic purpose of building a MapList of WebReport Objects
         * and their information based on the user selected filter.
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds no arguments
         * @param webReportList holds the list of webreport Names after
         * @param reportType of the webreport
         * filtering
         * @return MapList contains list of WebReports
         * @throws Exception if the operation fails
         */

         private MapList buildActualMapList(Context context,String[] args, WebReportList webReportList, MapList sharedList, String reportType)
         throws Exception
         {
            MapList webReportMapList = new MapList();
            WebReport webReport = null;

            try
            {
                String strCompletedIn = "";
                String strLastRun = "";
                String strLoggedUserName = context.getUser();
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                Iterator webReportListIter = webReportList.iterator();
                Iterator sharedListIter = sharedList.iterator();
                while(webReportListIter.hasNext()){
                    HashMap webReportMap = (HashMap)sharedListIter.next();
                    webReport = (WebReport)webReportListIter.next();
                    webReport.open(context);
                    String strReportType = webReport.getReportType();

                    if(strReportType != null && !strReportType.equalsIgnoreCase(reportType))
                    {
                        continue;
                    }
                    else if(strReportType == null)
                    {
                        continue;
                    }
                    String webReportName = webReport.getName();
                    if(webReportName.indexOf(LAST_INSTANCE) != -1)
                    {
                        continue;
                    }
                    String strOwner = webReport.getOwner();
                    long lDuration = 0;
                    try
                    {
                       lDuration = webReport.getResultDuration(context);
                    }
                    catch(Exception e)
                    {
                       lDuration = 0;
                    }
                    webReport.close(context);
                    Long llong = new Long(lDuration);
                    strCompletedIn = llong.toString();
                    try
                    {
                       strLastRun = webReport.getResultCreated(context);
                    }
                    catch(Exception e)
                    {
                       strLastRun = "";
                    }
                    webReportMap.put(WEBREPORT_NAME,webReportName);
                    webReportMap.put(LAST_RUN,strLastRun);
                    webReportMap.put(COMPLETED_IN,strCompletedIn);
                    webReportMap.put(OWNER,strOwner);
                    webReportMapList.add(webReportMap);
                }
                programMap.put(OWNED,webReportMapList);
                args = JPO.packArgs(programMap);
             }
             catch (Exception e){
                 throw new Exception("Cannot build actual Maplist of webreports" + e.toString());
             }
             return webReportMapList;
         }

         /**
          * This method gets the List of WebReports owned by the context user
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args holds no arguments
          * @return MapList contains list of WebReports
          * @throws Exception if the operation fails
          */

          @com.matrixone.apps.framework.ui.ProgramCallable
          public MapList getOwnedWebReports(Context context,String[] args)
             throws Exception
          {
              MapList webReportMapList = new MapList();
              MapList sharedMapList = new MapList();
              WebReport webReport = null;

              try {
                  HashMap programMap = (HashMap) JPO.unpackArgs(args);
                  WebReportList webReportsList = WebReport.getWebReports(context, false, false);

                  for(int k=0; k<webReportsList.size(); k++){
                    HashMap sharedMap = new HashMap();
                    webReport = (WebReport) webReportsList.get(k);
                    String owner = webReport.getOwner();
                    if(MetricsReports.isShared(context,webReport.getName(),owner))
                    {
                        sharedMap.put(KEY_SHARED, "yes");
                    }
                    else
                    {
                        sharedMap.put(KEY_SHARED, "no");
                    }

                    sharedMapList.add(sharedMap);
                  }

                  webReportMapList = buildActualMapList(context,args,webReportsList, sharedMapList, (String)programMap.get(REPORT_TYPE));
              }
              catch (Exception e){
                  throw new Exception("Cannot get the List of WebReports owned by the context user " + e.toString());
              }

              return webReportMapList;
          }

         /**
          * This method gets the List of WebReports shared with the context user
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args holds no arguments
          * @return MapList contains list of WebReports
          * @throws Exception if the operation fails
          */

          @com.matrixone.apps.framework.ui.ProgramCallable
          public MapList getSharedWebReports(Context context,String[] args)
              throws Exception
          {
              MapList webReportMapList = new MapList();
              WebReportList sharedReportList = new WebReportList();
              MapList sharedMapList = new MapList();
              //WebReportList tempWebReportList = new WebReportList();
              WebReport webReport = null;

              try {
                  HashMap programMap = (HashMap) JPO.unpackArgs(args);
                  WebReportList allWebReportsList = WebReport.getWebReports(context, true, false);
                  WebReportList ownedWebReportsList = WebReport.getWebReports(context, false, true);

                  Iterator  allWebReportsIter = allWebReportsList.iterator();

                  while(allWebReportsIter.hasNext())
                  {
                      webReport = (WebReport)allWebReportsIter.next();
                      String allWebReportOwner = webReport.getOwner();
                      if(ownedWebReportsList.size()!=0)
                      {
                          Iterator  ownedWebReportsIter = ownedWebReportsList.iterator();
                          while(ownedWebReportsIter.hasNext())
                          {
                              WebReport tempWebReport = (WebReport)ownedWebReportsIter.next();
                              String ownedWebReportOwner = tempWebReport.getOwner();
                              if(!(ownedWebReportOwner.equals(allWebReportOwner)) && !(sharedReportList.contains(webReport)))
                              {
                                  HashMap sharedMap = new HashMap();
                                  sharedMap.put(KEY_SHARED, "yes");
                                  sharedReportList.add(webReport);
                                  sharedMapList.add(sharedMap);
                              }
                          }
                      }
                      else
                      {
                          HashMap sharedMap = new HashMap();
                          sharedMap.put(KEY_SHARED, "yes");
                          sharedReportList.add(webReport);
                          sharedMapList.add(sharedMap);
                      }
                  }


                  webReportMapList = buildActualMapList(context, args, sharedReportList, sharedMapList, (String)programMap.get(REPORT_TYPE));
              }
              catch (Exception e){
                  throw new Exception("Cannot get the List of WebReports shared with the context user" + e.toString());
              }

              return webReportMapList;
          }

         /**
          * This method gets the List of All WebReports available in the matrix
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args holds no arguments
          * @return MapList contains list of WebReports
          * @throws Exception if the operation fails
          */

          @com.matrixone.apps.framework.ui.ProgramCallable
          public MapList getAllWebReports(Context context,String[] args)
              throws Exception
          {
              MapList webReportMapList = new MapList();
              MapList sharedMapList = new MapList();
              WebReport webReport = null;

              try {
                  HashMap programMap = (HashMap) JPO.unpackArgs(args);
                  WebReportList webReportsList = WebReport.getWebReports(context, true, false);
                  for(int k=0; k<webReportsList.size(); k++){
                    webReport = (WebReport) webReportsList.get(k);
                    String owner = webReport.getOwner();
                    HashMap sharedMap = new HashMap();
                    if(MetricsReports.isShared(context,webReport.getName(),owner))
                    {
                        sharedMap.put(KEY_SHARED, "yes");
                    }
                    else
                    {
                        sharedMap.put(KEY_SHARED, "no");
                    }
                    sharedMapList.add(sharedMap);
                  }
                  webReportMapList = buildActualMapList(context, args, webReportsList, sharedMapList, (String)programMap.get(REPORT_TYPE));
              }
              catch (Exception e){
                  throw new Exception("Cannot get the List of All WebReports available in the matrix" + e.toString());
              }

              return webReportMapList;
          }

         /**
          * This method gets the WebReport Object Names to populate the Table Column "Name".
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args contains a Map with the following entries:
          *    webReportList contains a MapList of Maps which contains object names
          * @return Vector contains list of WebReport Object names
          * @throws Exception if the operation fails
          */

          public Vector getName(Context context,String[] args)
              throws Exception
          {
             HashMap programMap = (HashMap) JPO.unpackArgs(args);
             MapList webReportList = (MapList)programMap.get(OBJECT_LIST);
             Vector vReport = new Vector(webReportList.size());

             try{
                for (int i=0; i < webReportList.size(); i++)
                {
                    HashMap webReportMap = (HashMap)webReportList.get(i);
                    String strName  = (String)webReportMap.get(WEBREPORT_NAME);
                    vReport.addElement(strName);
                }
             }
             catch (Exception e) {
                throw new Exception("Cannot get the WebReport Object Names to populate the Table Column Name" + e.toString());
             }

             return vReport;
          }

         /**
          * This method gets the WebReport object's Last Executed Time to populate
          * the Last Run Column of the table.
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args contains a Map with the following entries:
          *    webReportList contains a MapList of Maps which contains object names
          * @return Vector containing list of last run times of each WebReport
          * @throws Exception if the operation fails
          */

          public Vector getLastRun(Context context,String[] args)
              throws Exception
          {
              HashMap programMap = (HashMap) JPO.unpackArgs(args);
              MapList webReportList = (MapList)programMap.get(OBJECT_LIST);
              Vector vReport = new Vector(webReportList.size());

              try{
                 for (int i=0; i < webReportList.size(); i++)
                 {
                     HashMap webReportMap = (HashMap)webReportList.get(i);
                     String strLastRun  = (String)webReportMap.get(LAST_RUN);
                     vReport.addElement(strLastRun);
                 }
              }
              catch (Exception e)
              {
                 throw new Exception("Cannot get the lastrun webreport the Last Run Column of the table" + e.toString());
              }
              return vReport;
          }

        /**
         * This method gives the time taken to run a webreport
         *
         * @param context      the Ematrix <code>Context</code> object
         * @param args contains a Map with the following entries:
         *      contains the name of the webreport
         * @return Vector containing list of Completed-In times for each WebReport
         * @throws FrameworkException if the operation fails
         * @since BusinessMetrics 10.6
         */

        public Vector getCompletedIn(Context context,String[] args )
          throws Exception
        {
            WebReport webReport = null;
            String strDuration = "";
            String reportName = "";
            Vector vReport = null;
            try
            {
                HashMap programMap = (HashMap) JPO.unpackArgs(args);
                MapList webReportList = (MapList)programMap.get(OBJECT_LIST);
                vReport = new Vector(webReportList.size());
                for (int i=0; i < webReportList.size(); i++)
                {
                    HashMap webReportMap = (HashMap)webReportList.get(i);
                    reportName  = (String)webReportMap.get(WEBREPORT_NAME);
                    strDuration  = (String)webReportMap.get(COMPLETED_IN);
                    strDuration = getCorrectedFormat(strDuration);
                    vReport.addElement(strDuration);
                }
            }
            catch(Exception ex)
            {
                if(ex.toString() != null && (ex.toString().trim()).length()>0)
                {
                    throw new FrameworkException("MetricsReports.java unable to get the time taken to run a webreport:" + ex.toString().trim());
                }
            }

            return vReport;
        }

         /**
          * This method gets the WebReport object's Execution Time to populate
          * the Completed-In Column of the table.
          * WebReportList contains a MapList of Maps which contains object names
          * @return String containing the time taken to run a webreport.
          * @throws Exception if the operation fails
          */

           public static String getCorrectedFormat(String seconds)
              throws Exception
           {
                Date curDate = new Date();
                String correctFormat = "";
                int sec = Integer.parseInt(seconds);
                curDate.setHours(0);
                curDate.setMinutes(0);
                curDate.setSeconds(675);
                Calendar cl= Calendar.getInstance();
                cl.set(Calendar.HOUR,00);
                cl.set(Calendar.MINUTE,00);
                cl.set(Calendar.SECOND,00);
                cl.set(Calendar.SECOND,sec);
                StringBuffer hrString = new StringBuffer("");
                StringBuffer minString = new StringBuffer("");
                StringBuffer secString = new StringBuffer("");
                if (cl.get(Calendar.HOUR) < 10)
                {
                    hrString.append("0");
                    hrString.append(cl.get(Calendar.HOUR));
                }else{
                    hrString.append(cl.get(Calendar.HOUR));
                }

                if (cl.get(Calendar.MINUTE) < 10)
                {
                    minString.append("0");
                    minString.append(cl.get(Calendar.MINUTE));
                } else{
                    minString.append(cl.get(Calendar.MINUTE));
                }
                if (cl.get(Calendar.SECOND) < 10)
                {
                    secString.append("0");
                    secString.append(cl.get(Calendar.SECOND));
                } else {
                    secString.append(cl.get(Calendar.SECOND));
                }
                correctFormat = hrString+":"+minString+":"+secString;
                return correctFormat;
           }

         /**
          * This method gets the Owner of the WebReport Object.
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args contains a Map with the following entries:
          *    webReportList contains a MapList of Maps which contains objects count.
          * @return Vector containing list of Owners
          * @throws Exception if the operation fails
          */

          public Vector getOwner(Context context,String[] args)
             throws Exception
          {

             HashMap programMap = (HashMap) JPO.unpackArgs(args);
             MapList webReportList = (MapList)programMap.get(OBJECT_LIST);
             Vector vReport = new Vector(webReportList.size());

             try{
                for (int i=0; i < webReportList.size(); i++) {
                    HashMap webReportMap = (HashMap)webReportList.get(i);
                    String strOwner=(String)webReportMap.get(OWNER);
                    vReport.addElement(strOwner);
                }
             }
             catch (Exception e) {
                 throw new Exception("Cannot get the owner of webreport" + e.toString());
             }

             return vReport;

          }

         /**
          * This programHTMLOutput method to draw the a custom radio button for selection.
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args contains a Map with the following entries:
          *    webReportList contains a MapList of Maps which contains objects count.
          * @return Vector containing HTML of radio buttons to draw
          * @throws Exception if the operation fails
          */

          public Vector getWebReportRadioButton(Context context,String[] args)
            throws Exception
          {
             HashMap programMap = (HashMap) JPO.unpackArgs(args);
             MapList webReportList = (MapList)programMap.get(OBJECT_LIST);
             Vector vReport = new Vector(webReportList.size());
             String finalResult = "";

             try{
                for (int i=0; i < webReportList.size(); i++) {
                   HashMap webReportMap = (HashMap)webReportList.get(i);
                   String strName=(String)webReportMap.get(WEBREPORT_NAME);
                   String strOwner = (String)webReportMap.get(OWNER);
                   String strShared = (String)webReportMap.get(KEY_SHARED);
                   strName = strName + "|" + strOwner + "|" + strShared;
                   vReport.addElement("<input type=\"radio\" name=\"emxTableRowId\" value=\"" + XSSUtil.encodeForHTMLAttribute(context, strName) + "\" onClick=\"javascript:parent.ids = '~~~~';\">");
                }
             }
             catch (Exception e) {
                throw new Exception("Cannot draw radio buttons for selection" + e.toString());
             }

             return vReport;
         }

         /**
          * This refreshAccess method to show the Refresh Button
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args contains the Request Map
          * @return Boolean true or false
          */

          public static Boolean refreshAccess(Context context, String[] args)
            throws Exception
          {
             // Define a boolean to return
             Boolean bFieldAccess = Boolean.valueOf(false);
             HashMap requestMap = (HashMap) JPO.unpackArgs(args);

             try{
                String strName=(String)requestMap.get("showRefresh");
                if(strName != null && "true".equals(strName)) {
                    bFieldAccess = Boolean.valueOf(true);
                }
             }
             catch (Exception e) {
                throw new Exception("Cannot set access for refresh button" + e.toString());
             }

             return bFieldAccess;
         }


         /**
          * This showPieChart method to disable or enable Pie Chart command
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args contains the Request Map
          * @return Boolean true or false
          */

          public static Boolean showPieChart(Context context, String[] args)
            throws Exception
          {
             // Define a boolean to return
             Boolean bChartAccess = Boolean.valueOf(false);
             HashMap requestMap = (HashMap) JPO.unpackArgs(args);

             try{
                String strSupportedFormats = (String)requestMap.get("supportedReportFormats");
                if(strSupportedFormats != null && (strSupportedFormats.indexOf("pie") != -1)) {
                    bChartAccess = Boolean.valueOf(true);
                }
             }
             catch (Exception e) {
                throw new Exception("Cannot disable the pie chart" + e.toString());
             }
             return bChartAccess;
         }


         /**
          * This showStackBarChart method to disable or enable Stacked Bar Chart command
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args contains the Request Map
          * @return Boolean true or false
          */

          public static Boolean showStackBarChart(Context context, String[] args)
            throws Exception
          {
             // Define a boolean to return
             Boolean bChartAccess = Boolean.valueOf(false);
             HashMap requestMap = (HashMap) JPO.unpackArgs(args);

             try{
                String strSupportedFormats = (String)requestMap.get("supportedReportFormats");
                if(strSupportedFormats != null && (strSupportedFormats.indexOf("stackbar") != -1) ) {
                    bChartAccess = Boolean.valueOf(true);
                }
             }
             catch (Exception e) {
                throw new Exception("Cannot disable the stack bar chart" + e.toString());
             }

             return bChartAccess;
         }

         /**
          * This showTabularFormat method to disable or enable Tabular format command
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args contains the Request Map
          * @return Boolean true or false
          */

          public static Boolean showTabularFormat(Context context, String[] args)
            throws Exception
          {
             // Define a boolean to return
             Boolean bChartAccess = Boolean.valueOf(false);
             HashMap requestMap = (HashMap) JPO.unpackArgs(args);

             try{
                String strSupportedFormats = (String)requestMap.get("supportedReportFormats");
                if(strSupportedFormats != null && (strSupportedFormats.indexOf("tabular") != -1) ) {
                    bChartAccess = Boolean.valueOf(true);
                }
             }
             catch (Exception e) {
                throw new Exception("Cannot disable the Tabular format " + e.toString());
             }

             return bChartAccess;
         }

        /**
          * This showBarChart method to disable or enable Bar Chart command
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args contains the Request Map
          * @return Boolean true or false
          */

          public static Boolean showBarChart(Context context, String[] args)
            throws Exception
          {
             // Define a boolean to return
             Boolean bChartAccess = Boolean.valueOf(false);
             HashMap requestMap = (HashMap) JPO.unpackArgs(args);

             try{
                String strSupportedFormats = (String)requestMap.get("supportedReportFormats");

                if(strSupportedFormats != null )
                {
                    int iIndex = strSupportedFormats.indexOf("bar");
                    // check if there is a word "bar" in the supported format, but ensure
                    // it is not "stackbar" by checking previous letter of "bar" is not 'k'
                    if ( iIndex != -1 &&
                       ( iIndex == 0 || strSupportedFormats.charAt(iIndex -1) != 'k'))
                    {
                        bChartAccess = Boolean.valueOf(true);
                    }
                }
             }
             catch (Exception e) {
                throw new Exception("Cannot disable the Bar chart" + e.toString());
             }

             return bChartAccess;
         }

        /**
          * This showBarChart method to disable or enable Line command
          *
          * @param context the eMatrix <code>Context</code> object
          * @param args contains the Request Map
          * @return Boolean true or false
          */

          public static Boolean showLineChart(Context context, String[] args)
            throws Exception
          {
             // Define a boolean to return
             Boolean bChartAccess = Boolean.valueOf(false);
             HashMap requestMap = (HashMap) JPO.unpackArgs(args);

             try{
                String strSupportedFormats = (String)requestMap.get("supportedReportFormats");
                if(strSupportedFormats != null && (strSupportedFormats.indexOf("line") != -1) ) {
                    bChartAccess = Boolean.valueOf(true);
                }
             }
             catch (Exception e) {
                throw new Exception("Cannot disable the Line chart" + e.toString());
             }

             return bChartAccess;
         }

    }
