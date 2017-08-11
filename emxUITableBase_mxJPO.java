/*
**   emxUITableBase
**
**   Copyright (c) 1992-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.taglib.FieldValueStore;
import com.matrixone.apps.framework.ui.UIFormCommon;
import com.matrixone.apps.framework.ui.UITableCommon;
import com.matrixone.apps.framework.ui.UITableIndented;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;

/**
 * The <code>emxUITableBase</code> class contains methods for UI Table Component.
 * @version AEF 10.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxUITableBase_mxJPO
{

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.0
     */

    public emxUITableBase_mxJPO (Context context, String[] args)
      throws Exception
    {
    }

    /**
     * This method used for sorting objects.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    comparatorName - a String of the comparator name used in comparision
     *    list - a MapList which contains a list of objects to be sorted
     * @return MapList of objects in sorting order
     * @throws Exception if the operation fails
     * @since AEF 10.0
     */

    public MapList sortMapList(Context context,String[] args) throws Exception
    {
        HashMap hm = (HashMap)JPO.unpackArgs(args);
        String comparatorJPO = (String)hm.get("comparatorName");
        MapList list = (MapList)hm.get("list");
        String compName  = (String)JPO.invoke(context,
                                              comparatorJPO,
                                              args,
                                              "getClassName",
                                              null,
                                              String.class);
        Class cls = Class.forName(compName);
        emxCommonBaseComparator_mxJPO cmp = (emxCommonBaseComparator_mxJPO)cls.newInstance();

        cmp.setSortKeys(hm);
        Collections.sort(list, cmp);
        return list;
    }
    
    /**
     * This method used for comparing objects.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    comparatorName - a String of the comparator name used in comparision
     *    first - firstobject  for comparison
     *    second - secondobject for comparison
     * @return Map of difference after sorting
     * @throws Exception if the operation fails
     */
    
    public HashMap compareObjects(Context context,String[] args) throws Exception
    {
        HashMap hm = (HashMap)JPO.unpackArgs(args);
        String comparatorJPO = (String)hm.get("comparatorName");
        Object first  = hm.get("firstObject");
        Object second = hm.get("secondObject");
        String compName  = (String)JPO.invoke(context,
                                              comparatorJPO,
                                              args,
                                              "getClassName",
                                              null,
                                              String.class);
        Class cls = Class.forName(compName);
        emxCommonBaseComparator_mxJPO cmp = (emxCommonBaseComparator_mxJPO)cls.newInstance();

        cmp.setSortKeys(hm);
        HashMap result = new HashMap();
        result.put("difference",new Integer(cmp.compare(first, second)));       
        return result;
    }

    /**
     * This method process the changes of the Table Edit component Objects.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    requestMap - a HashMap having the request object data
     *    timeZone - a String representing the Time zone
     *    tableData - a HashMap having the table data
     * @return HashMap
     * @throws Exception if the operation fails
     * @since AEF 10.0
     */

    public HashMap updateTableObjects(Context context,String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        String timeZone = (String)programMap.get("timeZone");
        HashMap tableData = (HashMap)programMap.get("tableData");
        Locale locale = (Locale)requestMap.get("localeObj");
        String objectBased = (String)requestMap.get("objectBased");
        if (objectBased != null && objectBased.equals("false"))
          return tableData;

        UIFormCommon uif = new UIFormCommon();
        UITableCommon uit = new UITableCommon();
        String objectId = "";
        String relId = "";
        String objCount = (String) requestMap.get("objCount");

        HashMap updateMap = new HashMap();
        String updatedfieldmap = (String) requestMap.get("updatedfieldmap");
        StringList updateFieldList = FrameworkUtil.split(updatedfieldmap, ",");
        for(int k =0; k < updateFieldList.size(); k++){
        	String updateRow = (String)updateFieldList.get(k);
        	StringList tmpList = FrameworkUtil.split(updateRow, ":");
        	updateMap.put(tmpList.get(0), tmpList.get(1));
        }

        int count = 0;
        if(objCount != null && !"".equals(objCount))
        {
            count = Integer.parseInt(objCount);
        }
        String allowKeyableDates = "false";
        try
        {
            allowKeyableDates = EnoviaResourceBundle.getProperty(context, "emxFramework.AllowKeyableDates");
        } catch(Exception e) {
            allowKeyableDates = "false";
        }

        String languageStr = (String) requestMap.get("languageStr");
        boolean errorFromDate = true;
        String invaliddate = "";
        try
        {
            ContextUtil.startTransaction(context, true);
            invaliddate = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Common.InvalidDate", new Locale(languageStr));            
            HashMap tableControlMap = uit.getControlMap(tableData);
            MapList fields = uit.getColumns(tableData);
            HashMap fieldsMap = new HashMap();

            HashMap phtmlcolumnsMap = new HashMap();
            for (int k = 0; k < fields.size(); k++)
            {
                HashMap field = (HashMap)fields.get(k);
                fieldsMap.put(uit.getName(field), field);
                if(uif.isDateField(field) && uif.isFieldEditable(field)) {
                    String fieldallowKeyableDates = (String)uif.getSetting(field, "Allow Manual Edit");
                    if((allowKeyableDates != null && "true".equalsIgnoreCase(allowKeyableDates)) || (fieldallowKeyableDates != null && "true".equalsIgnoreCase(fieldallowKeyableDates))) {
                        String columnName = uit.getName(field);
                        Iterator itr = updateMap.keySet().iterator();
                        //for(int i = 0; i < count; i++)
                        while(itr.hasNext())
                        {
                            String dateValue = (String) requestMap.get(columnName + itr.next());
                            if(dateValue != null && !"null".equalsIgnoreCase(dateValue) && !"".equalsIgnoreCase(dateValue)) {
                                java.text.DateFormat df = java.text.DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(), locale);
                                df.setLenient(false);
                                java.util.Date formattedDate = df.parse(dateValue);
                            }
                        }
                    }
                }

				String columnType = uif.getSetting(field,"Column Type");
				if (uif.isFieldEditable(field) && ("programHTMLOutput".equalsIgnoreCase(columnType)|| uif.isClassificationAttributesField(field) || uif.isClassificationPathsField(field))) {
					phtmlcolumnsMap.put(uit.getName(field), "");
				}
            }
            errorFromDate = false;

            HashMap newIdMap = new HashMap();
            if(phtmlcolumnsMap.size()>0){
				for(int i = 0; i < count; i++)
				{
					MapList tempFieldsList = new MapList();
					StringList updatedFieldsList = FrameworkUtil.split((String)updateMap.get(i + ""), "|");
					for(int l = 0; l < updatedFieldsList.size(); l++){
						String fldName = (String)updatedFieldsList.get(l);
						if(!phtmlcolumnsMap.containsKey(fldName)) {
							tempFieldsList.add((HashMap)fieldsMap.get(fldName));
						}
					}

					Iterator itr1 = phtmlcolumnsMap.keySet().iterator();
					while(itr1.hasNext()) {
						String fldName = (String)itr1.next();
						tempFieldsList.add((HashMap)fieldsMap.get(fldName));
					}

					objectId = (String) requestMap.get("objectId" + i);

					relId = (String) requestMap.get("relId" + i);
					String newid = uif.commitFields(context, requestMap, objectId, relId, timeZone, tempFieldsList, i);
					if(newid != null && !objectId.equalsIgnoreCase(newid)) {
						newIdMap.put(objectId, newid);
					}
				}
			} else {
            Iterator itr = updateMap.keySet().iterator();
            //for(int i = 0; i < count; i++)
            while(itr.hasNext())
            {
                String row = (String)itr.next();
            	int i = Integer.parseInt(row);
            	MapList tempFieldsList = new MapList();
            	StringList updatedFieldsList = FrameworkUtil.split((String)updateMap.get(row), "|");
            	for(int l = 0; l < updatedFieldsList.size(); l++){
            		tempFieldsList.add((HashMap)fieldsMap.get((String)updatedFieldsList.get(l)));
            	}


                objectId = (String) requestMap.get("objectId" + i);

                relId = (String) requestMap.get("relId" + i);
                String newid = uif.commitFields(context, requestMap, objectId, relId, timeZone, tempFieldsList, i);
                if(newid != null && !objectId.equalsIgnoreCase(newid)) {
                    newIdMap.put(objectId, newid);
					}
                }
            }

            if(newIdMap.size() > 0)
            {
                uit.changeObjectIdList(context, tableData, newIdMap);
            }
            tableControlMap.put("All Values Map", null);
			uif.resetAttributeCahe();
            ContextUtil.commitTransaction(context);
        } catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            if(errorFromDate) {
                throw (new FrameworkException(invaliddate));
            } else if (ex.toString() != null && (ex.toString().trim()).length() > 0) {
                throw (new FrameworkException(ex.toString()));
            }
        }
        return tableData;
    }

    /**
     * This method process the changes of the Table Edit component Objects.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
     *    timeZone - a String representing the Time zone
     *    document - the XML Document Object
     * @return void
     * @throws Exception if the operation fails
     * @since AEF 10.0
     */

    public HashMap updateIndentedTableObjects(Context context,String[] args)
        throws Exception
        {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	String timeZone = (String)programMap.get("timeZone");
    	Document doc = (Document)programMap.get("document");
    	Locale locale = (Locale)programMap.get("locale");
    	String sXmlReturn=null;
    	HashMap xmlMap=new HashMap();
    	String msg = "";
    	String action = null;
    	int i=0;
    	//MapList mlItems=new MapList();
    	UITableIndented uiti = new UITableIndented();

    	HashMap returnMap = new HashMap();
    	String xml=" ";

    	returnMap.put("action","success");
    	returnMap.put("status","commited");

    	Element root = doc.getRootElement();
    	try
    	{
    		ContextUtil.startTransaction(context, true);
    		HashMap requestMap = uiti.getRequestMapFromDocument(context, doc);
    		HashMap tempRequestMap = (HashMap)programMap.get("requestMap");
    		String jpoParamList = (String)tempRequestMap.get("jpoAppServerParamList");
    		if(jpoParamList != null)
    		{
    			StringTokenizer jpoParamListTokens = new StringTokenizer(jpoParamList, ",");
    			while(jpoParamListTokens.hasMoreTokens())
    			{
    				String jpoParamDetails = jpoParamListTokens.nextToken();

    				StringTokenizer jpoParamDetailsTokens = new StringTokenizer(jpoParamDetails, ":");
    				String jpoParamScope  = jpoParamDetailsTokens.nextToken();
    				String jpoParamName   = jpoParamDetailsTokens.nextToken();
    				Object jpoParamValue  = null;

    				jpoParamValue = tempRequestMap.get(jpoParamName);
    				if(jpoParamValue != null)
    				{
    					requestMap.put(jpoParamName, jpoParamValue);
    				}
    			}
    		}
    		String strSelectedTable = (String)requestMap.get("selectedTable");
    		HashMap columnsMap = uiti.getColumnsMapFromDocument(context, doc);
    		java.util.Iterator itr1= doc.getContent().iterator();
    		while(itr1.hasNext()){
    			Element elm = (Element)itr1.next();
    		}

    		String strConnProgram = (String)requestMap.get("connectionProgram");

    		java.util.List elmlist = root.getChildren("object");
    		java.util.Iterator itr = elmlist.iterator();
    		while(itr.hasNext()) {
    			String items="items"+i;
    			Element elm = (Element)itr.next();
    			String objectId = elm.getAttributeValue("objectId");
    			String relId = elm.getAttributeValue("relId");
    			String parentOID=elm.getAttributeValue("parentId");
    			String strMarkup = elm.getAttributeValue("markup");
    			String rowId=elm.getAttributeValue("rowId");
    			String level=elm.getAttributeValue("level");
    			requestMap.put("level", level);
    			HashMap allchgColumnMap = uiti.getChangedColumnMapFromElement(context, elm);
    			// added for typeahead
    			String[] columnvalues= new String[allchgColumnMap.size()];
    			Set columnSet=allchgColumnMap.keySet();
    			Iterator columnIterator=columnSet.iterator();
    			Map columnMapforTA= new HashMap();
    			int iIndex=0;


    			while(columnIterator.hasNext()){
    				String strCloumnName=(String)columnIterator.next();
    				String strColumnValue=allchgColumnMap.get(strCloumnName).toString();
    				columnvalues[iIndex]=strColumnValue;
    				iIndex++;
    				HashMap currentColumn=(HashMap)columnsMap.get(strCloumnName);
    				HashMap currentColumnSetting =(HashMap) currentColumn.get("settings");
    				String inputType = (String)currentColumnSetting.get("Input Type");
    				String addInputType = (String)currentColumnSetting.get("Add Input Type");
    				String lookUpInputType = (String)currentColumnSetting.get("Lookup Input Type");
    				String strTypeAheadFunction=(String)currentColumnSetting.get("TypeAhead Function");
    				String strTypeAheadSavedValuesLimit=(String)currentColumnSetting.get("TypeAhead Saved Values Limit");
    				Map currentColum= new HashMap();    	
    				HashMap temptColumnSetting =new HashMap(); 
    				temptColumnSetting.put("TypeAhead Function", strTypeAheadFunction);
    				temptColumnSetting.put("TypeAhead Saved Values Limit", strTypeAheadSavedValuesLimit);
    				currentColum.put("settings", currentColumnSetting);
    				columnMapforTA.put(strCloumnName, currentColum);
    			}                        
    			saveTypeAheadValues(context,strSelectedTable,columnMapforTA,columnvalues);

    			// added for typeahead

    			if("changed".equals(strMarkup)){
    				//for attribute changes of row(s).
    				HashMap chgColumnMap = uiti.getChangedColumnMapFromElement(context, elm);
    				uiti.updateTableData(context, requestMap, objectId, relId, timeZone, columnsMap, chgColumnMap, locale);
    				HashMap changedHS = new HashMap();
    				changedHS.put("oid", objectId);
    				changedHS.put("rowId", rowId);
    				changedHS.put("pid", parentOID);
    				changedHS.put("relid",relId);
    				changedHS.put("markup", strMarkup);
    				MapList mlChanged=new MapList();
    				mlChanged.add(changedHS);

    				if(mlChanged!=null){
    					returnMap.put(items,mlChanged);
    				}
    			}
    			//for other actions i.e add/delete/resequence
    			else{
    				if( strConnProgram== null || "".equals(strConnProgram) ){
    					MapList chgRowsMapList = uiti.getChangedRowsMapFromElement(context, elm);
    					MapList mlItem=(MapList)uiti.editTableRows(context, requestMap, objectId,chgRowsMapList,columnsMap,timeZone,locale);
    					if(mlItem!=null){
    						returnMap.put(items,mlItem);
    					}
    				}
    				else{
    					//Construct Hashmap to pass to custom JPO
    					HashMap hmCustom = new HashMap();
    					hmCustom.put("paramMap",requestMap );
    					hmCustom.put("parentOID",objectId );
    					hmCustom.put("objectId",objectId );
    					hmCustom.put("relId",relId );
    					hmCustom.put("action",strMarkup );
    					hmCustom.put("columnsMap",columnsMap);
    					//for resequencing or adding include the
    					//parent and all the children in the xml
    					hmCustom.put("contextData",elm );
    					hmCustom.put("localeObj", locale);

    					//Invoke the custom JPO
    					String[] methodargs = JPO.packArgs(hmCustom);
    					String strJPOName = strConnProgram.substring(0, strConnProgram.indexOf(":"));
    					String strMethodName = strConnProgram.substring (strConnProgram.indexOf(":") + 1, strConnProgram.length());
    					FrameworkUtil.validateMethodBeforeInvoke(context, strJPOName, strMethodName,"connectionProgram");
    					Map retMap =(HashMap)JPO.invoke(context, strJPOName, null, strMethodName, methodargs, HashMap.class);
    					// type ahead
    					MapList chgRowsMapList = uiti.getChangedRowsMapFromElement(context, elm);
    					for (int x = 0; x < chgRowsMapList.size(); x++) {
    						HashMap changedRowMap = (HashMap) chgRowsMapList.get(x);
    						String markup = (String) changedRowMap.get("markup");                            
    						if(markup != null && markup.compareToIgnoreCase("new")==0){
    							Map values=(HashMap)changedRowMap.get("columns");        
    							if(values != null && values.size() > 0){
    								String[] columnValues= new String[values.size()];
    								Iterator it=values.values().iterator();  
    								int l=0;
    								while(it.hasNext())  
    								{        
    									String strValue=it.next().toString();
    									columnValues[l]=strValue;
    									l++;
    								} 
    								saveTypeAheadValues(context,strSelectedTable,columnsMap,columnValues);
    							}	
    						}
    					}
    					// type ahead
    					if(retMap != null && retMap.size()>0){
    						//System.out.println("map"+retMap.toString());
    						//modified for Bug - 343790
    						action = (String)retMap.get("Action");
    						if(action!=null){
    							returnMap.put("action",action);
    						}
    						String message = (String)retMap.get("Message");
    						if("ERROR".equals(action)){
    							throw new Exception(message);
    						}
    						//modified for Bug - 347231    
    						MapList childrenItems = (MapList)retMap.get("changedRows");
    						if(childrenItems!=null){
    							returnMap.put(items,childrenItems);
    						}

    					}
    				}

    			}

    			i++;
    		}
    		ContextUtil.commitTransaction(context);
    	} catch (Exception ex) {
    		ContextUtil.abortTransaction(context);
    		msg = ex.getMessage();
    		returnMap.put("action","error");
    		returnMap.put("message", msg);
    		//throw (new FrameworkException(ex.toString()));
    	}
    	return returnMap;
        }
    
    /**
     * This method saves the type ahead values for a table in cue
     * @param context the <code>matrix.db.Context</code> for user logged in
     * @param args a String with id for an object
     * @return nothing
     * @throws Exception
     */
    private void saveTypeAheadValues(Context context,String strSelectedTable, Map columnsMap, String[] valuesMap)throws Exception{

       boolean storeTypeAhead=false;
       FieldValueStore fvs = new FieldValueStore(strSelectedTable,"table");
       int i=0;
       String limit="10";
       Set columnSet=columnsMap.keySet();
       Iterator columnIterator=columnSet.iterator();
       while(columnIterator.hasNext()){ 	
       	String strCloumnName=(String)columnIterator.next();
       	HashMap currentColumn=(HashMap)columnsMap.get(strCloumnName);
       	HashMap currentColumnSetting =(HashMap) currentColumn.get("settings");
       	String strTypeAheadFunction=(String)currentColumnSetting.get("TypeAhead Function");
       	String strTypeAheadSavedValuesLimit=(String)currentColumnSetting.get("TypeAhead Saved Values Limit");
       	String strTypeAhead=(String)currentColumnSetting.get("TypeAhead");
       	if(strTypeAheadSavedValuesLimit==null || strTypeAheadSavedValuesLimit.length()==0){
       		try
   			{
           		// get the character count from the system property
               	limit = EnoviaResourceBundle.getProperty(context, "emxFramework.TypeAhead.SavedValuesLimit");
   			}
           	catch (Exception e)
   			{
           		// system property not found
           		limit = "10";
           	}
       	}
       	else{
       		limit=strTypeAheadSavedValuesLimit;
       	}
       	if(strTypeAheadFunction==null && valuesMap[i].trim().length()!= 0 && "true".equalsIgnoreCase(strTypeAhead)){
       		storeTypeAhead=true;
               fvs.addSubmittedValue(context,strCloumnName,valuesMap[i],valuesMap[i],Integer.parseInt(limit));
       	}
       	i++;
       }
       if(storeTypeAhead){
          	fvs.save(context);
          }
    }
        /**
	     * This method returns related objects in HashMap
	     * @param context the <code>matrix.db.Context</code> for user logged in
	     * @param args a String with id for an object
	     * @return a <code>HashMap</code> of related objects
	     * @throws Exception
	     */

	    public HashMap getRelatedObjects(Context context,String[] args) throws Exception
	    {

	        HashMap hmpInput  = (HashMap)JPO.unpackArgs(args);
	        StringList idList = (StringList) hmpInput.get("id");
	        HashMap hmpMap = orderByPolicy(context, idList);
	        Set Plocykeys = hmpMap.keySet();
	        Iterator Keyitr = Plocykeys.iterator();
	        String id  = "";
	        HashMap hmpOutput  = new HashMap();
	        StringList relselects = new StringList();
	        StringList objectselects = new StringList();
	        objectselects.add(DomainConstants.SELECT_ID);
	        objectselects.add(DomainConstants.SELECT_NAME);

	        while(Keyitr.hasNext())
	        {
	            String strPol  = (String) Keyitr.next();	            
	            StringList ObjWidSamePol = (StringList) hmpMap.get(strPol);
	            String relationship = getRelationships(context,strPol);
	            for(int k = 0 ; ObjWidSamePol!=null && k < ObjWidSamePol.size() ; k++)
	            {
	                id = (String) ObjWidSamePol.get(k);
	                StringList relCheck  = new StringList();
	                String strFrom   = "";
	                boolean getFrom = false;
	                String relList  = "";
	                StringList children = new StringList();
	                if (relationship != null && !"".equals(relationship))
	                {
	                    relCheck = FrameworkUtil.split(relationship,"|");
	                    relList  = (String) relCheck.get(0);
	                    strFrom  = (String) relCheck.get(1);
	                    getFrom = ("from".equalsIgnoreCase(strFrom))? false : true;
	                    DomainObject domain = new DomainObject(id);
	                    MapList mplRelatedObjs = domain.getRelatedObjects(context,relList,"*",objectselects,relselects,getFrom,!getFrom,
	                                                                      (short)1,
	                                                                          "",
	                                                                          "",
	                                                                          0,
	                                                                          null,
	                                                                          null,
	                                                                          null);

	                    if(mplRelatedObjs!=null && mplRelatedObjs.size()>0)
	                    {
	                        Iterator itr = mplRelatedObjs.iterator();
	                        while(itr.hasNext())
	                        {
	                            Map relObjsmap = (Map)itr.next();
	                            String strChildObjId = (String)relObjsmap.get(DomainConstants.SELECT_ID);
	                            children.add(strChildObjId);
	                        }
	            }
	            	//System.out.println("CalculateSequenceNumber.getRelatedObjects() domain name"+domain.getName(context));
	            }

	            hmpOutput.put(id,children);
	            	//System.out.println("hmpOutput  : " + hmpOutput);
	            }

	        }
	        return hmpOutput;
	}

	 /**
	     * This method returns realtionships and direction of expansion <rel> | <getTo/getFrom>
	     * @param context  the <code>matrix.db.Context</code> for user logged in
	     * @param id a String with id for an object
	     * @return     String
	     * @throws  Exception
	     */

	    public String getRelationships(Context context,String strObjectPolicy)throws Exception
	    {
	        String strFromKey = "";
	        String strToKey = "";
	        StringBuffer result = new StringBuffer();
	        String strRelList ="";
	        if (strObjectPolicy != null && strObjectPolicy.length() > 0)
	        {
	            String policyRegName = FrameworkUtil.getAliasForAdmin(context,"policy", strObjectPolicy, true);
	            StringBuffer strBuffer = new StringBuffer();
	            strBuffer.append("emxFramework.Lifecycle.MassPromoteDemote.");
	            strBuffer.append(policyRegName);
	            strBuffer.append(".from.RelationshipList");
	            strFromKey = strBuffer.toString().trim();
	            strToKey   = strBuffer.toString().replace(".from.",".to.");
	        }
	        try
	        {
	            strRelList = EnoviaResourceBundle.getProperty(context, strFromKey);
	        }catch(Exception e)
	        {
	            strRelList = "";
	        }
	        if(strRelList != null && !"".equals(strRelList))
	        {
	            result.append(strRelList);
	            result.append("|");
	            result.append("from");
	            return result.toString();
	        }
	       try
	       {
	           strRelList = EnoviaResourceBundle.getProperty(context, strToKey);
	       }catch(Exception e)
	       {
	           strRelList = "";
	       }
	       if(strRelList != null && !"".equals(strRelList))
	       {
	           result.append(strRelList);
	           result.append("|");
	           result.append("to");
	       }
	        return result.toString();
	    }
	    /** This method returns HashMap with the list of objects ordered according to policy
	     * @param context  the <code>matrix.db.Context</code> for user logged in
	     * @param objectIds    ist of objects ids
	     * @return HashMap
	     * @throws FrameworkException
	     */
	    public HashMap orderByPolicy(Context context,StringList objectIds) throws FrameworkException
	    {
	        DomainObject doObj = new DomainObject();
	        HashMap orderedMap = new HashMap();
	        StringList listSamePolicy;
	        String objectId  = "";
	        for(int k = 0 ; k < objectIds.size() ; k++)
	        {
	            objectId = (String) objectIds.get(k);
	            doObj.setId(objectId);
	            String Policy  = doObj.getInfo(context,DomainConstants.SELECT_POLICY);
	            if (orderedMap.containsKey(Policy))
	            {
	                listSamePolicy = (StringList) orderedMap.get(Policy);
	                listSamePolicy.add(objectId);
	            }
	            else
	            {
	                listSamePolicy = new StringList();
	                listSamePolicy.add(objectId);
	                orderedMap.put(Policy,listSamePolicy);
	            }

	        }
	        return orderedMap;

    }
	  	  
}
