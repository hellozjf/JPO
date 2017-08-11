/*
** emxSpecStructureHistoryBase
**
** Copyright (c) 2007 MatrixOne, Inc.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
* @quickreview qyg     15:01:22 IR-349953-3DEXPERIENCER2014x HTML code is displayed in  TO object  column in Spec Structure history  report. 
*/

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;

/**
 * This JPO class has some methods pertaining to the Spec Structure History report.
 * @author Brian Casto
 * @version RequirementManagement V6R2009x - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxSpecStructureHistoryBase_mxJPO extends emxDomainObject_mxJPO
{
   MapList tableData;

        
   /**
    * Create a new emxSpecStructureHistoryBase object.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return a emxSpecStructureHistoryBase object.
    * @throws Exception if the operation fails
    * @since RequirementManagement V6R2009x
    * @grade 0
    */
   public emxSpecStructureHistoryBase_mxJPO(Context context, String[] args) throws Exception
   {
      super(context, args);
   }


   /**
    * Main entry point.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return an integer status code (0 = success)
    * @throws Exception if the operation fails
    * @since RequirementManagement V6R2009x
    * @grade 0
    */
   public int mxMain (Context context, String[] args) throws Exception
   {
      if (!context.isConnected())
      {
         String language = context.getSession().getLanguage();
         String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.FeaturesCheckFailed");
         throw  new Exception(strContentLabel);
      }
      return(0);
   }


   /**
    *  Get objects for Spec Structure History Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of html code to
    *        construct the Path Column.
    *  @throws Exception if the operation fails
    * @since RequirementManagement V6R2009x
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getSpecStructureHistoryTableData(Context context, String[] args)
      throws Exception
   {
		//unpacking the Arguments from variable args
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String objectId = (String) programMap.get("objectId");
				
		//object selects for spec
		StringList objectSelectsForSpec = new StringList();
		objectSelectsForSpec.addElement(DomainConstants.SELECT_ID);
		objectSelectsForSpec.addElement(DomainConstants.SELECT_TYPE);
		objectSelectsForSpec.addElement(DomainConstants.SELECT_NAME);
		objectSelectsForSpec.addElement(DomainConstants.SELECT_REVISION);
		objectSelectsForSpec.addElement("history");
		String typeSelectForExpand = "*"; 
		
		DomainObject doSpec = DomainObject.newInstance(context,objectId);
		doSpec.open(context);
		HashMap objectMap = new HashMap();
        objectMap.put(SELECT_ID, doSpec.getId());
        objectMap.put(SELECT_TYPE, doSpec.getTypeName());
        objectMap.put(SELECT_NAME, doSpec.getName());
        objectMap.put(SELECT_REVISION, doSpec.getRevision());
		objectMap.put("history", doSpec.getInfoList(context, "history"));
		doSpec.close(context);

		MapList mapObjsOfSpec = new MapList();
		mapObjsOfSpec = doSpec.getRelatedObjects(context, ReqSchemaUtil.getSpecStructureRelationship(context),
											   typeSelectForExpand, objectSelectsForSpec,
											   null, false, true, (short)0, null, null);
											   
		HashMap alaisMap = new HashMap();
		for (int i = 0; i < mapObjsOfSpec.size();i++)
		{
			Map mapT2 = (Map) mapObjsOfSpec.get(i);
			String type = (String)mapT2.get(DomainConstants.SELECT_TYPE);
			String name = (String)mapT2.get(DomainConstants.SELECT_NAME);
			String rev = (String)mapT2.get(DomainConstants.SELECT_REVISION);
			String alais = type + " " + name + " " + rev;
			alaisMap.put(alais, mapT2);
		}

		mapObjsOfSpec.add(objectMap);
		tableData = new MapList();
		for (int i = 0; i < mapObjsOfSpec.size();i++)
		{
			Map mapT2 = (Map) mapObjsOfSpec.get(i);
			StringList histories = (StringList) mapT2.get("history");
			for (int j=0; j < histories.size(); j++)
			{
				String thisHistory = (String)histories.get(j);
				if (thisHistory.startsWith("connect Specification Structure to ") || thisHistory.startsWith("disconnect Specification Structure to "))
				{
					Map thisMap = new HashMap(mapT2);
					thisMap.remove("level");
				
					String thisTNR = "";
					String thisAction = "";
					if (thisHistory.startsWith("connect"))
					{
						thisTNR = thisHistory.substring(34, thisHistory.indexOf(" - "));
						thisAction = "connect";
					}
					else
					{
						thisTNR = thisHistory.substring(37, thisHistory.indexOf(" - "));
						thisAction = "disconnect";
					}
					String thisUser = thisHistory.substring(thisHistory.indexOf(" user:") + 6, thisHistory.indexOf(" time:"));
					String thisTime = thisHistory.substring(thisHistory.indexOf(" time:") + 6, thisHistory.indexOf(" state:"));
					
					thisMap.put("toObjectPlain", thisTNR.trim());
					Map toObjectMap = (Map)alaisMap.get(thisTNR.trim());
					if (toObjectMap == null)
					{
						thisMap.put("toObject", thisTNR.trim());
					}
					else
					{
						String type = (String)toObjectMap.get(DomainConstants.SELECT_TYPE);
						String name = (String)toObjectMap.get(DomainConstants.SELECT_NAME);
						String rev = (String)toObjectMap.get(DomainConstants.SELECT_REVISION);
						String id = (String)toObjectMap.get(DomainConstants.SELECT_ID);
			
						String attIcon = UINavigatorUtil.getTypeIconProperty(context, type);
						String aHref = "<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=common&amp;parentOID=null&amp;jsTreeID=null&amp;suiteKey=Framework&amp;objectId=" + id + "', '700', '600', 'false', 'popup', '')\" class=\"object\">";
						String attHtml = "<table border=\"0\">";
						attHtml = attHtml + "<tr>";
						attHtml = attHtml + "<td rmb=\"\" valign=\"top\" rmbID=\"" + id + "\">" + aHref + "<img src='../common/images/" +attIcon+ "' border=\"0\" /></a></td>";
						attHtml = attHtml + "<td rmb=\"\" rmbID=\"" + id + "\">" + aHref  +  XSSUtil.encodeForHTML(context, name + " " + rev) + "</a></td>";
						attHtml = attHtml + "</tr>";
						attHtml = attHtml + "</table>";
						thisMap.put("toObject", attHtml);
						
						String attHTMLReport = "<img src='../common/images/" +attIcon+ "' border=\"0\" /> " + name + " " + rev;
						thisMap.put("toObjectHTMLReport", attHTMLReport);
					}

					thisMap.put("user", thisUser.trim());
					thisMap.put("date", thisTime.trim());
					thisMap.put("action", thisAction.trim());
					
					tableData.add(thisMap);
				}
			}
         }
		return(tableData);
   }


   private List getTableColumnData(MapList dataList, String attrName)
   {
      int       rowCount = (dataList == null? 0: dataList.size());
      Vector    colData = new Vector(rowCount);
      
      for (int ii = 0; ii < rowCount; ii++)
      {
         Map    rowData = (Map) dataList.get(ii);
         String strData = (String) rowData.get(attrName);
         
         colData.add(strData == null? "": strData);
      }
      //System.out.println("\n" + attrName + " => " + colData);
      return(colData);
   }

   /**
    * Method is to return the list of object table column data
    * @param context The Matrix Context object
    * @param args JPO arguments
    * @return List of table Column data
    * @throws Exception if operation fails
    */
   public List getTableColumnData(Context context, String[] args)
      throws Exception
   {
      Map programMap = (HashMap) JPO.unpackArgs(args);

      HashMap columnMap = (HashMap) programMap.get("columnMap");
      //System.out.println("columnMap = " + columnMap);
      String attName = (String) (columnMap.containsKey("expression_businessobject")?
                                 columnMap.get("expression_businessobject"):
                                 columnMap.get("expression_relationship"));

      // Auto-filter and Column-Sort pass the table data in an objectList param?
      MapList theData = (programMap.containsKey("objectList")? (MapList) programMap.get("objectList"): tableData);
      List attList = getTableColumnData(theData, attName);

      return(attList);
   }
   
	
   /**
    * Method is to return the list of get To object Column data
    * @param context The matrix context
    * @param args JPO argument 
    * @return List of objectId
    * @throws Exception if operation fails.
    */
	public List getToObjectColumnData(Context context, String[] args)
	throws Exception
	{
		Map programMap = (HashMap) JPO.unpackArgs(args);
		MapList theData = (programMap.containsKey("objectList")? (MapList) programMap.get("objectList"): tableData);
		
		Map paramMap = (Map) programMap.get("paramList");
		String reportFormat = (String) paramMap.get("reportFormat");
		
		List attList = null; 
		if (reportFormat != null && "HTML".equals(reportFormat))
			attList = getTableColumnData(theData, "toObjectHTMLReport");
		else if (reportFormat != null && "CSV".equals(reportFormat))
			attList = getTableColumnData(theData, "toObjectPlain");
		else
			attList = getTableColumnData(theData, "toObject");
		
		return(attList);
	}

}
