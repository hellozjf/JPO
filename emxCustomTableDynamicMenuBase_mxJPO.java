/** emxCustomTableDynamicMenuBase.java
**
** Created on Jun 28, 2007
**
** Copyright Dassault Systemes, 1993 ? 2007. All rights reserved.
** All Rights Reserved
** This program contains proprietary and trade secret information of
** Dassault Systemes.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
*/


import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.framework.ui.UITableCustom;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.FrameworkException;



public class  emxCustomTableDynamicMenuBase_mxJPO {


    /**
     * Constructor
     * @param context
     * @param args
     * @throws Exception
     */
    public emxCustomTableDynamicMenuBase_mxJPO(Context context, String[] args) throws Exception
    {

    }

    /**
     * @param context
     *            The eMatrix <code>Context</code> object
     * @param args
     * @return HashMap
     * @throws Exception
     * @throws FrameworkException
     *
     */
    public HashMap getCustomCommands(Context context, String[] args)
            throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        // Modified for bug no 345326
        String strlanguage = (String)requestMap.get("languageStr");
        // Till here
        HashMap paramMap = (HashMap)programMap.get("paramMap");
        boolean isUserTable = ((Boolean)requestMap.get("userTable")).booleanValue();
        String timeStamp = (String)paramMap.get("timeStamp");
        String uiType = (String) paramMap.get("uiType");

        String objectId = (String)requestMap.get("objectId");
        String tableName = "";
		String programName="";
        String sysTableName = "";
        String selectedProgramLabel ="";
        List derivedTableNameslist = new ArrayList();
        TreeMap tableFiltersMap = new TreeMap();
        TreeMap programFiltersMap = new TreeMap();
        MapList programMenuFiltersMap = new MapList();
        String strCustomize = EnoviaResourceBundle.getProperty(context, "emxFramework.UITable.Customization");
        String customize = (String)requestMap.get("customize");
        try {
            if (uiType.equalsIgnoreCase("table")) {
                tableName = (String) requestMap.get("table");
                programFiltersMap = (TreeMap)requestMap.get("ProgramFilter");
                programMenuFiltersMap = (MapList)requestMap.get("ProgramMenuFilter");
            } else if (uiType.equalsIgnoreCase("structureBrowser")) {
            	tableFiltersMap = (TreeMap)requestMap.get("TableFilter");
            	programFiltersMap = (TreeMap)requestMap.get("ProgramFilter");
            	programMenuFiltersMap = (MapList)requestMap.get("ProgramMenuFilter");
                tableName = (String) requestMap.get("selectedTable");
				programName = (String) requestMap.get("selectedProgram");
				selectedProgramLabel =(String) requestMap.get("selectedProgramLabel");
            }
            if (isUserTable) {
                sysTableName = UITableCustom.getSystemTableName(context,tableName);
                derivedTableNameslist = (List)UITableCustom.getDerivedTableNames(context, sysTableName);
               } else {
                sysTableName=tableName;
                derivedTableNameslist = (List) UITableCustom.getDerivedTableNames(context, tableName);

            }

        }

        catch (Exception ex) {
            ex.printStackTrace();
        }

        HashMap parentMap = new HashMap();
        MapList childrenMapList = new MapList();
        HashMap moreMap = new HashMap();
        MapList moreMenuchildrenList = new MapList();

        HashMap settings = new HashMap();
        settings.put("Registered Suite", "Framework");
        String strCmdLabel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage), "emxFramework.CustomTable.DynamicCommand.View");
        String parentHref = "../common/emxCustomizedTableToggle.jsp?uiType="+uiType;
        parentMap.put("type", "command");
        parentMap.put("label", strCmdLabel);
        parentMap.put("description", "Dynamic Table Menu");
        parentMap.put("roles", new StringList("all"));
        parentMap.put("href",parentHref);
        parentMap.put("settings", settings);

        int derivedTableListSize = derivedTableNameslist.size();

        if(derivedTableListSize>5)
        {

            moreMap.put("type", "menu");
            moreMap.put("label", "More");
            moreMap.put("description", "Dynamic More Menu");
            moreMap.put("roles", new StringList("all"));
            moreMap.put("settings", settings);

        }

        for (int i = 0; i < derivedTableListSize; i++) {
            String derivedTableName = (String) derivedTableNameslist.get(i);
            HashMap dynamicSettingMap = new HashMap();
            dynamicSettingMap.put("Registered Suite", "Framework");
            String derivedTableLabel = derivedTableName;
            try
            {
                derivedTableLabel = derivedTableName.substring(0,derivedTableName.lastIndexOf("~"));
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            if(tableName.equals(derivedTableName))
                dynamicSettingMap.put("Image", "../common/images/iconActionChecked.gif");
            else
                dynamicSettingMap.put("Image", "");


            if(i<5)
            {
                HashMap childrenMap = new HashMap();
                childrenMap.put("type", "command");
                childrenMap.put("label", derivedTableLabel);
                childrenMap.put("description", "Dynamic Table Command");
                if(uiType!=null && "table".equalsIgnoreCase(uiType))
                {
                    childrenMap.put("href", "javascript:refreshTable('"+ derivedTableName+"','','','"+timeStamp+"','"+uiType+"')");
                }
                else
                {
                    if(uiType!=null && "structureBrowser".equalsIgnoreCase(uiType))
                        childrenMap.put("href", "javascript:refreshSBTable('"+ derivedTableName+"','','','true','"+timeStamp+"','"+uiType+"')");
                }
                childrenMap.put("roles", new StringList("all"));
                childrenMap.put("settings", dynamicSettingMap);
                childrenMapList.add(childrenMap);
            }

            else
            {
                HashMap moreMenuChildrenMap = new HashMap();
                moreMenuChildrenMap.put("type", "command");
                moreMenuChildrenMap.put("label", derivedTableLabel);
                moreMenuChildrenMap.put("description", "More Menu Table Command");
                if(uiType!=null && "table".equalsIgnoreCase(uiType))
                {
                   moreMenuChildrenMap.put("href", "javascript:refreshTable('"+ derivedTableName+"','','','"+timeStamp+"','"+uiType+"')");
                }
                else
                {
                    if(uiType!=null && "structureBrowser".equalsIgnoreCase(uiType))
                        moreMenuChildrenMap.put("href", "javascript:refreshSBTable('"+ derivedTableName+"','','','true','"+timeStamp+"','"+uiType+"')");
                }
                moreMenuChildrenMap.put("roles", new StringList("all"));
                moreMenuChildrenMap.put("settings", dynamicSettingMap);

                moreMenuchildrenList.add(moreMenuChildrenMap);

            }


        }

        if(derivedTableListSize>5)
        {
            moreMap.put("Children", moreMenuchildrenList);
            childrenMapList.add(moreMap);
        }
        String strSysLabel =  EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage),"emxFramework.CustomizeTable.SystemTable");
        HashMap systemMap = new HashMap();
        HashMap systemSettingMap = new HashMap();
        systemSettingMap.put("Registered Suite", "Framework");
        systemMap.put("type", "command");
        systemMap.put("label", strSysLabel);
        systemMap.put("description", "System Table View");
        if(tableName.equals(sysTableName))
        {
            systemSettingMap.put("Image", "../common/images/iconActionChecked.gif");
        }
        systemMap.put("settings", systemSettingMap);
        if(uiType!=null && uiType.equalsIgnoreCase("table"))
        systemMap.put("href", "javascript:refreshTable('"+ sysTableName+"','','','"+timeStamp+"','"+uiType+"')");
        else
        {
          if(uiType!=null && uiType.equalsIgnoreCase("structureBrowser"))
                systemMap.put("href", "javascript:refreshSBTable('"+ sysTableName+"','','','true','"+timeStamp+"','"+uiType+"')");

        }

        HashMap separatorMap = new HashMap();
        HashMap separatorSettingMap = new HashMap();
        separatorSettingMap.put("Registered Suite", "Framework");
        separatorSettingMap.put("Action Type", "Separator");
        separatorMap.put("type", "command");
        separatorMap.put("Name", "AEFSeparator");
        separatorMap.put("label", "AEFSeparator");
        separatorMap.put("description", "Use as separator for toolbar buttons");
        separatorMap.put("alt", "Separator");
        separatorMap.put("settings", separatorSettingMap);
        if(derivedTableListSize>0)
        {
            childrenMapList.add(systemMap);
        }

        HashMap imageEditMap = new HashMap();
        imageEditMap.put("Image","../common/images/iconActionEdit.gif");
        imageEditMap.put("Registered Suite","Framework");
        imageEditMap.put("Window Height","658");
        imageEditMap.put("Window Width","750");
        imageEditMap.put("Target Location", "popup");
        imageEditMap.put("Popup Modal", "true");

        StringBuffer editURL = new StringBuffer(100);
        editURL.append("../common/emxCustomizeTablePopup.jsp?timeStamp=");
        editURL.append(timeStamp);
        editURL.append("&uiType=");
        editURL.append(uiType);
        editURL.append("&objectId=");
        editURL.append(objectId);
        editURL.append("&expandLevelFilter=false");
        editURL.append("&mode=Edit");

        String strEditLabel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage),"emxFramework.CustomizeTable.Edit");

        HashMap editMap = new HashMap();
        editMap.put("type", "command");
        editMap.put("label", strEditLabel);
        editMap.put("description", "Edit Current Table View");
        editMap.put("href",editURL.toString());
        editMap.put("settings",imageEditMap);
        editMap.put("roles",new StringList("all"));

        if(isUserTable)
        {
            childrenMapList.add(separatorMap);
            childrenMapList.add(editMap);
        }

        HashMap imageCreateMap = new HashMap();
        imageCreateMap.put("Image","../common/images/iconActionCreate.gif");
        imageCreateMap.put("Registered Suite","Framework");
        imageCreateMap.put("Window Height","658");
        imageCreateMap.put("Window Width","750");
        imageCreateMap.put("Target Location", "popup");
        imageCreateMap.put("Popup Modal", "true");

        StringBuffer createURL = new StringBuffer(100);
        createURL.append("../common/emxCustomizeTablePopup.jsp?timeStamp=");
        createURL.append(timeStamp);
        createURL.append("&uiType=");
        createURL.append(uiType);
        createURL.append("&objectId=");
        createURL.append(objectId);
        createURL.append("&mode=New");

        String strCreateLabel =  EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage),"emxFramework.CustomizeTable.CreateNew");
        HashMap createMap = new HashMap();
        createMap.put("type", "command");
        createMap.put("settings",imageCreateMap);
        createMap.put("label", strCreateLabel);
        createMap.put("description", "Create New Table View");
        createMap.put("href", createURL.toString());

        //If mobile or customize=false customize=disable then no need to show create table command
        if(!("false".equalsIgnoreCase(customize) || "disable".equalsIgnoreCase(strCustomize))){
        childrenMapList.add(createMap);
        }
        String strDeleteView =  EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage),"emxFramework.Table.Delete.Confirmation.Message");

        HashMap imageDeleteMap = new HashMap();
        imageDeleteMap.put("Registered Suite","Framework");
        imageDeleteMap.put("Image","../common/images/iconActionDelete.gif");
        imageDeleteMap.put("Confirm Message",strDeleteView);
        imageDeleteMap.put("Submit","true");
        imageDeleteMap.put("Target Location", "listHidden");


        StringBuffer deleteURL = new StringBuffer(100);
        deleteURL.append("../common/emxCustomizedTableDelete.jsp?uiType=");
        deleteURL.append(uiType);
        deleteURL.append("&objectId=");
        deleteURL.append(objectId);


        String strDeleteLabel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage),"emxFramework.CustomizeTable.Delete");

        HashMap deleteMap = new HashMap();
        deleteMap.put("type", "command");
        deleteMap.put("label", strDeleteLabel);
        deleteMap.put("description", "Delete Current Table View");
        deleteMap.put("href", deleteURL.toString());
        deleteMap.put("settings",imageDeleteMap);
        deleteMap.put("roles",new StringList("all"));

        if(isUserTable)
            childrenMapList.add(deleteMap);

		//Adding Table Filters to view menu
        if(uiType!=null && "structureBrowser".equalsIgnoreCase(uiType) && tableFiltersMap != null){
        	if(childrenMapList.size() > 0){
        	childrenMapList.add(separatorMap);
        	}
        	HashMap tableFilterSettingMap = new HashMap();
        	tableFilterSettingMap.put("Image", "");
        	tableFilterSettingMap.put("Registered Suite","Framework");
        	
        	Collection tableFilterKeySet = tableFiltersMap.keySet();
        	Iterator tableFilterKeyIter = tableFilterKeySet.iterator();
        	while(tableFilterKeyIter.hasNext()){
        		String strLable = (String) tableFilterKeyIter.next();
        		HashMap tableFilterMap = new HashMap();
        		tableFilterMap.put("type", "command");
        		tableFilterMap.put("label", strLable);
        		tableFilterMap.put("description", "Table Filters");
        		tableFilterMap.put("href", "javascript:filterPage('','true','TableFilter','"+ tableFiltersMap.get(strLable) +"')");
        		tableFilterMap.put("settings",tableFilterSettingMap);
        		tableFilterMap.put("roles",new StringList("all"));
            	childrenMapList.add(tableFilterMap);
        	}
        }
        
		//Adding Program Filters to view menu
        if(uiType!=null &&  programFiltersMap != null){
        	if(childrenMapList.size() > 0){
        	childrenMapList.add(separatorMap);
        	}
        	
        	
        	Collection programFilterKeySet = programFiltersMap.keySet();
        	Iterator programFilterIter = programFilterKeySet.iterator();
        	while(programFilterIter.hasNext()){
				HashMap programFilterSettingMap = new HashMap();
        		programFilterSettingMap.put("Registered Suite","Framework");
        		String strLable = (String) programFilterIter.next();
				if(programFiltersMap.get(strLable).equals(programName)){
        			
        			programFilterSettingMap.put("Image", "../common/images/iconActionChecked.gif");
        		}else {
        			
        			programFilterSettingMap.put("Image", "");
        		}
        		HashMap programFilterMap = new HashMap();
        		programFilterMap.put("type", "command");
        		programFilterMap.put("label", strLable);
        		programFilterMap.put("description", "Program Filters");
        		if(uiType.equalsIgnoreCase("table"))
        		{
        			programFilterMap.put("href", "javascript:onFilterOptionChange('','','ProgramFilter','"+ programFiltersMap.get(strLable) +"')");
        		}
        		else
				{
        			programFilterMap.put("href", "javascript:filterPage('','','ProgramFilter','"+ programFiltersMap.get(strLable) +"')");
				}	
        		programFilterMap.put("settings",programFilterSettingMap);
        		programFilterMap.put("roles",new StringList("all"));
            	childrenMapList.add(programFilterMap);
        	}
        }
        
		//Adding Program Menu Filters to view menu
        if(uiType!=null &&  programMenuFiltersMap != null){
        	if(childrenMapList.size() > 0){
        	childrenMapList.add(separatorMap);
        	}
        	
        	
        	ArrayList programsList = new ArrayList();
        	ArrayList lablesList = new ArrayList();
        	ArrayList keyLabelsList = new ArrayList();
        	Iterator programMenuFilterIter1 = programMenuFiltersMap.iterator();
        	while(programMenuFilterIter1.hasNext()){
        		HashMap programMenuFilter = (HashMap) programMenuFilterIter1.next();
        		Collection keyMap = programMenuFilter.keySet();
        		Iterator keyMapIter = keyMap.iterator();
        		keyLabelsList.add((String)keyMapIter.next());
        		Collection valueMap = programMenuFilter.values();
        		Iterator valueMapIter = valueMap.iterator();
        		String selectedProgramMenuValue = (String) valueMapIter.next();
        		int sepIndex1 = selectedProgramMenuValue.indexOf("|");
        		programsList.add(selectedProgramMenuValue.substring(0, sepIndex1));
        		lablesList.add(selectedProgramMenuValue.substring(sepIndex1+1, selectedProgramMenuValue.length()));
        	}
        	
        	for(int i=0; i<programsList.size(); i++){
        		
        		HashMap programMenuFilterSettingMap = new HashMap();
                programMenuFilterSettingMap.put("Registered Suite","Framework");
        		
        		
        		
        		String selectedProgram = (String) programsList.get(i);
        		
        		
        		if(UIUtil.isNotNullAndNotEmpty(selectedProgramLabel)&&selectedProgram.equals(selectedProgramLabel.substring(0,selectedProgramLabel.indexOf("|")))){
    				programMenuFilterSettingMap.put("Image", "../common/images/iconActionChecked.gif");
    			}else {
    				programMenuFilterSettingMap.put("Image", "");
    			}
        		
        		
        		String selectedLabel = (String) lablesList.get(i);
        		
        		StringBuffer otherProgramsList = new StringBuffer();
        		StringBuffer otherLabelsList = new StringBuffer();
        		for(int j=0; j<programsList.size(); j++){
        			if(j != i){
        				otherProgramsList.append(",");
        				otherProgramsList.append((String) programsList.get(j));
        				otherLabelsList.append(",");
        				otherLabelsList.append((String) lablesList.get(j));
        			}
        		}
        		
        		String programs = selectedProgram + otherProgramsList.toString();
        		String labels = selectedLabel + otherLabelsList.toString();
        		String programMenuFilterParams = programs + "|" + labels;
        		
        		HashMap programMenuFilterMap = new HashMap();
        		programMenuFilterMap.put("type", "command");
        		programMenuFilterMap.put("label", keyLabelsList.get(i));
        		programMenuFilterMap.put("description", "Program Menu Filters");
        		if(uiType.equalsIgnoreCase("table"))
				{
        			programMenuFilterMap.put("href", "javascript:onFilterOptionChange('','','ProgramMenuFilter','"+ programMenuFilterParams +"')");
				}	
        		else	
				{
        		  programMenuFilterMap.put("href", "javascript:filterPage('','','ProgramMenuFilter','"+ programMenuFilterParams +"')");
				} 
        		programMenuFilterMap.put("settings",programMenuFilterSettingMap);
        		programMenuFilterMap.put("roles",new StringList("all"));
        		childrenMapList.add(programMenuFilterMap);
        	}
        	
        	
        }
        
        parentMap.put("Children", childrenMapList);
        return parentMap;
    }
}
