/*
**  emxMetricsObjectDetailsSummaryBase.java
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
import matrix.db.User;
import matrix.db.WebReport;
import matrix.util.StringList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.metrics.MetricsReports;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectList;
import matrix.util.IntList;
import matrix.util.IntItr;

/**
 * The <code>emxMetricsObjectDetailsSummarybase</code> class contains methods related to Object Details Summary Page.
 * @version  - BusinessMetrics 10.6 Copyright (c) 2005, MatrixOne, Inc.
 */
public class emxMetricsObjectDetailsSummaryBase_mxJPO
{
    /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    * @since BusinessMetrics 10.6
    * @grade 0
    */
    public emxMetricsObjectDetailsSummaryBase_mxJPO(Context context, String[] args) throws Exception
    {

    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since BusinessMetrics 10.6
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        return 0;
    }

    /**
     * This method is to get all the objects in the cell.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return MapList contains list of Business Object Ids
     * @throws Exception if the operation fails
     * @since BusinessMetrics 10.6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllObjects(Context context, String[] args)
        throws Exception
    {
        BusinessObjectWithSelectList objectList = null;
        BusinessObjectList busObjectList = null;
        MapList objectIDList = new MapList();
        BusinessObjectList objList = null;
        StringList selects = new StringList();
        WebReport webreportObj = null;
        String strContextUser = context.getUser();
        int cellNo = 0;
        int startIndex = 1;
        int endIndex = 0;
        int archIndex = 0;
        try
        {
            HashMap programMap   = (HashMap) JPO.unpackArgs(args);
            String strCellNo     = (String) programMap.get("cellNumber");
            String strStartIndex = (String) programMap.get("startindex");
            String strEndIndex   = (String) programMap.get("endindex");
            String strReportType = (String) programMap.get("reportType");
            String strReportName = (String) programMap.get("reportName");
            String strTimeStamp  = (String) programMap.get("timeStamp");
            String strOwner  = (String) programMap.get("owner");
            selects.add(DomainConstants.SELECT_ID);

            if(strCellNo != null && (!"".equals(strCellNo)))
            {
                cellNo = Integer.parseInt(strCellNo);
            }
            if(strStartIndex != null && (!"".equals(strStartIndex)))
            {
                startIndex = Integer.parseInt(strStartIndex);
            }
            if(strEndIndex != null && (!"".equals(strEndIndex)))
            {
                endIndex = Integer.parseInt(strEndIndex);
            }

            if(strReportName == null || ("".equals(strReportName)))
            {
                if(strReportType != null && (!"".equals(strReportType)))
                {
                    StringBuffer sbemxWebReport = new StringBuffer();
                    sbemxWebReport.append(MetricsReports.DOT_EMX_PREFIX);
                    sbemxWebReport.append(strReportType);
                    sbemxWebReport.append(strTimeStamp);
                    webreportObj = new WebReport(sbemxWebReport.toString(),strContextUser);
                    webreportObj.open(context);
                    IntList intList = webreportObj.getArchivesByLabel(context,MetricsReports.DOT_EMX_PREFIX + strTimeStamp,true);
                    if(intList.size()>0)
                    {
                        IntItr intItr = new IntItr(intList);
                        while(intItr.next())
                        {
                            Integer intObject = (Integer)intItr.value();
                            archIndex = intObject.intValue();
                        }
                        objectList = webreportObj.getArchiveBusinessObjectsWithSelect(context, archIndex, selects, cellNo, startIndex, endIndex);
                    }
                }
            }
            else
            {
                webreportObj = new WebReport(strReportName,strOwner);
                webreportObj.open(context);
                String resultsXML = "";
                IntList intList = webreportObj.getArchivesByLabel(context,MetricsReports.DOT_EMX_PREFIX + strTimeStamp,true);

                if(intList.size()>0)
                {
                    IntItr intItr = new IntItr(intList);
                    while(intItr.next())
                    {
                        Integer intObject = (Integer)intItr.value();
                        archIndex = intObject.intValue();
                    }
                    objectList = webreportObj.getArchiveBusinessObjectsWithSelect(context, archIndex, selects, cellNo, startIndex, endIndex);
                }
                else if(MetricsReports.hasResult(context,strReportName,strOwner))
                {
                    objectList = webreportObj.getResultBusinessObjectsWithSelect(context, selects, cellNo, startIndex, endIndex);
                }
            }

            objectIDList = getObjectIDfromSelect(context, objectList);
            webreportObj.close(context);

        }catch (Exception ex) {
            throw (new FrameworkException(ex.toString()));
        }
       return objectIDList;
    }

    /**
    * Method to get the Object IDs from select.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param BusinessObjectWithSelectList holds no arguments
    * @return MapList contains list of Business Object Ids
    * @throws Exception if the operation fails
    * @since BusinessMetrics 10.6
    */
    protected MapList getObjectIDfromSelect(Context context,
        BusinessObjectWithSelectList objectList) throws Exception
    {
        MapList objectIDList = new MapList();
        BusinessObjectWithSelect objectWithSelect = null;
        String objectId = "";
        try{
           for (int i=0;i<objectList.size();i++){
               HashMap objectIDMap = new HashMap();
               objectWithSelect = objectList.getElement(i) ;
               objectId = (String)objectWithSelect.getSelectData(DomainConstants.SELECT_ID);
               objectIDMap.put("id",objectId);
               objectIDList.add(objectIDMap);
           }
        }catch(Exception ex){
          throw (new FrameworkException(ex.toString()));
        }
        return objectIDList;
    }

    /**
    * Method to get the Object IDs from select.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param BusinessObjectWithSelectList holds no arguments
    * @return MapList contains list of Business Object Ids
    * @throws Exception if the operation fails
    * @since BusinessMetrics 10.6
    */
    protected MapList getObjectIDfromSelect(Context context,
        BusinessObjectList objectList) throws Exception
    {
        MapList objectIDList = new MapList();
        BusinessObject busObject = null;
        String objectId = "";
        try{
           for (int i=0;i<objectList.size();i++){
               HashMap objectIDMap = new HashMap();
               busObject = objectList.getElement(i) ;
               busObject.open(context);
               objectId = (String)busObject.getObjectId();
               objectIDMap.put("id",objectId);
               objectIDList.add(objectIDMap);
           }
        }catch(Exception ex){
          throw (new FrameworkException(ex.toString()));
        }
        return objectIDList;
    }
}
