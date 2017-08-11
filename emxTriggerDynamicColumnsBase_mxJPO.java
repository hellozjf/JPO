/** emxTriggerDynamicColumnsBase

**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**/



import java.util.*;
import matrix.db.*;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.DomainObject;
import matrix.util.*;



public class emxTriggerDynamicColumnsBase_mxJPO
{


    public emxTriggerDynamicColumnsBase_mxJPO (Context context, String args[])throws Exception
    {

    }

    public int mxMain(Context context,String args[])throws Exception
    {
    	return 0;
    }

    public MapList getDynamicColumns(Context context, String args[])throws Exception
    {
    	HashMap hash  = (HashMap) JPO.unpackArgs(args);
    	HashMap paramList = (HashMap) hash.get("requestMap");
    	MapList ColumnsList = (MapList) hash.get("tableColumns");
    	MapList objList = (MapList)hash.get("objectList");
        String subHeader = (String) paramList.get("subHeader");
        MapList returnMap = new MapList();
        HashMap hashfornewColumn = null;
        HashMap hashColumnSetting = null;

    	try
    	{
            if(subHeader!=null && "emxFramework.TriggerValidationReport.Header.Multiple_Objects".equalsIgnoreCase(subHeader))
            {
                hashColumnSetting = new HashMap();
                hashfornewColumn = new HashMap();
                hashColumnSetting.put("Registered Suite","Framework");
                hashColumnSetting.put("Show Type Icon","true");
                hashfornewColumn.put("settings",hashColumnSetting);
                hashfornewColumn.put("label","emxFramework.Basic.Name");
                hashfornewColumn.put("name","name");
                hashfornewColumn.put("expression_businessobject","name");
                hashfornewColumn.put("href","${COMMON_DIR}/emxTree.jsp");
                returnMap.add(hashfornewColumn);

                hashColumnSetting = new HashMap();
                hashfornewColumn = new HashMap();
                hashColumnSetting.put("Registered Suite","Framework");
                hashColumnSetting.put("Admin Type","Type");
                hashfornewColumn.put("settings",hashColumnSetting);
                hashfornewColumn.put("label","emxFramework.Basic.Type");
                hashfornewColumn.put("name","type");
                hashfornewColumn.put("expression_businessobject","type");
                returnMap.add(hashfornewColumn);

                hashColumnSetting = new HashMap();
                hashfornewColumn = new HashMap();
                hashColumnSetting.put("Registered Suite","Framework");
                hashColumnSetting.put("Admin Type","State");
                hashfornewColumn.put("settings",hashColumnSetting);
                hashfornewColumn.put("label","emxFramework.Basic.Current");
                hashfornewColumn.put("name","state");
                hashfornewColumn.put("expression_businessobject","current");
                returnMap.add(hashfornewColumn);

            }
            hashColumnSetting = new HashMap();
            hashfornewColumn = new HashMap();
            hashColumnSetting.put("Registered Suite","Framework");
            hashColumnSetting.put("Column Type","program");
            hashColumnSetting.put("program","emxTriggerValidationResults");
            hashColumnSetting.put("function","getDescription");
            hashColumnSetting.put("Width","200");
            hashColumnSetting.put("Sortable","false");
            hashfornewColumn.put("settings",hashColumnSetting);
            hashfornewColumn.put("label","emxFramework.TriggerValidationReport.Label.RuleDescription");
            hashfornewColumn.put("name","RuleDescription");
            returnMap.add(hashfornewColumn);
    	}
    	catch(Exception e)
    	{
    		throw e;
    	}
	    return returnMap;
    }
}
