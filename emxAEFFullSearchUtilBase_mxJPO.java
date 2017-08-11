/*   AEFFullSearchUtilBase.java
**
**   Copyright (c) 2002-2016 Dassault Systemes.
**   All Rights Reserved.
**
**   This JPO contains the implementation of Full Search Saved queries and collections
**
*/

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.util.StringList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.MqlUtil;

import com.matrixone.apps.framework.ui.UITableCustom;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.XSSUtil;

public class emxAEFFullSearchUtilBase_mxJPO
{
    public emxAEFFullSearchUtilBase_mxJPO(Context context, String[] args) throws Exception
    {
    }

    public HashMap getSavedSearchList(Context context, String[] args) throws Exception {
        HashMap argMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) argMap.get("paramMap");
//        HashMap commandMap = (HashMap) argMap.get("commandMap");
//        HashMap settingsMap = (HashMap) commandMap.get("settings");
        HashMap requestMap = (HashMap) argMap.get("requestMap");
        // LQA: Fix for the bug 375532
        String strlanguage =null;
        if(requestMap!=null && requestMap.size()>0){
            strlanguage = (String) requestMap.get("languageStr");
        }else{
            strlanguage = (String) paramMap.get("languageStr");
        }
        // Fix ends.
        String[] args1 = JPO.packArgs(paramMap);
        MapList childrenMapList = new MapList();

        HashMap saveMenuSettings = new HashMap();
        saveMenuSettings.put("Registered Suite", "Framework");
        HashMap parentMap = new HashMap();
        parentMap.put("type", "command");
        parentMap.put("label", "Parent Menu");
        parentMap.put("description", "Parent Menu");
        parentMap.put("roles", new StringList("all"));
        parentMap.put("settings", saveMenuSettings);

        StringBuffer saveURL = new StringBuffer(100);
        saveURL.append("javascript:void(window.parent.FullSearch.save())");
        String strSaveLabel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage), "emxFramework.GlobalSearch.Save");
        HashMap saveSettings = new HashMap();
        saveSettings.put("Registered Suite", "Framework");
        saveSettings.put("Image", "../common/images/iconActionCreate.gif");
        HashMap saveMap = new HashMap();
        saveMap.put("type", "command");
        saveMap.put("label", strSaveLabel);
        saveMap.put("description", "Save");
        saveMap.put("href",saveURL.toString());
        saveMap.put("settings",saveSettings);
        saveMap.put("roles",new StringList("all"));
        childrenMapList.add(saveMap);

        StringBuffer saveAsURL = new StringBuffer(100);
        saveAsURL.append("javascript:void(window.parent.FullSearch.showSaveAsDialog())");
        String strSaveAsLabel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage), "emxFramework.GlobalSearch.SaveAs");
        HashMap saveAsSettings = new HashMap();
        saveAsSettings.put("Registered Suite", "Framework");
        saveAsSettings.put("Image", "../common/images/iconActionCreate.gif");
        HashMap saveAsMap = new HashMap();
        saveAsMap.put("type", "command");
        saveAsMap.put("label", strSaveAsLabel);
        saveAsMap.put("description", "Save As");
        saveAsMap.put("href",saveAsURL.toString());
        saveAsMap.put("settings",saveAsSettings);
        saveAsMap.put("roles",new StringList("all"));
        childrenMapList.add(saveAsMap);

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
        childrenMapList.add(separatorMap);

        MapList mapListChildren = getSavedSearches(context, args1);
        if (mapListChildren.size() == 0) {
            HashMap dummyChildMap = new HashMap();
            dummyChildMap.put("type", "command");
            dummyChildMap.put("label",EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage), "emxFramework.FullSearch.NoItemFound"));
            dummyChildMap.put("href", "javascript:void(0)");
            dummyChildMap.put("description", "In case there are no related saved queries");
            dummyChildMap.put("roles", new StringList("all"));
            dummyChildMap.put("settings", null);
            childrenMapList.add(dummyChildMap);
        }
        else {
            for(int i=0;i<mapListChildren.size();i++) {
                HashMap childMap =(HashMap) mapListChildren.get(i);
                HashMap childernMap = new HashMap();
                childernMap.put("type", "command");
                childernMap.put("label", XSSUtil.encodeForHTML(context, (String)childMap.get("label")));
                childernMap.put("href", childMap.get("href"));
                childernMap.put("settings", childMap.get("settings"));
                childernMap.put("roles", new StringList("all"));
                childrenMapList.add(childernMap);
            }
        }
        parentMap.put("Children", childrenMapList);
        return parentMap;
    }

public static final String FULL_SEARCH_NAME_PREFIX = ".emx";
public MapList getSavedSearches(Context context, String[] args) throws Exception {
        MapList res = new MapList();
        HashMap settings = new HashMap();
        settings.put("Registered Suite", "Framework");
        settings.put("Pull Right", "false");
        String data = MqlUtil.mqlCommand(context, "LIST QUERY $1",FULL_SEARCH_NAME_PREFIX + "*");
        StringList names = FrameworkUtil.split(data, "\n");
        Iterator it = names.iterator();
        int pos = FULL_SEARCH_NAME_PREFIX.length();
        while (it.hasNext()) {
            String name = (String) it.next();
            String queryData = MqlUtil.mqlCommand(context, "PRINT QUERY $1 SELECT $2 dump",name,"description").trim();
            
         //   String q1 = MqlUtil.mqlCommand(context, "PRINT QUERY $1 SELECT $2 DUMP",dquote(name),"description");
         //   System.out.println("q1--- "+ q1);
            String encData = com.matrixone.apps.domain.util.XSSUtil.encodeForURL(queryData);
            String strippedName = name.substring(pos);
            String href = "javascript:window.parent.FullSearch.loadSavedSearch("
                + squote(strippedName) + ", " + squote(encData)
                + ")";
            HashMap child = new HashMap();
            child.put("type", "command");
            child.put("label", strippedName);
            child.put("roles", new StringList("all"));
            child.put("href", href);
            child.put("settings", settings);
            res.add(child);
        }
        return res;
    }

    public static String dquote(String str) {
        return "\"" + str + "\"";
    }
    public static String squote(String str) {
        return "'" + str + "'";
    }

    public HashMap getCollectionCommands(Context context, String[] args)throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        //LQA: Fix for the bug -- 375532
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        String strlanguage = (String)paramMap.get("languageStr");
        if(requestMap!=null && requestMap.size()>0){
            strlanguage = (String) requestMap.get("languageStr");
        }else{
            strlanguage = (String) paramMap.get("languageStr");
        }
        // Fix Ends.
        
        String addToClipboardLabel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage), "emxFramework.Collections.AddToClipboardCollection");
        String addToCollectionsLabel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage), "emxFramework.Collections.NewAddToCollections");
        MapList childrenMapList = new MapList();
        HashMap clipBoardMap = new HashMap();

        HashMap parentMap = new HashMap();
        HashMap settings = new HashMap();
        String strCmdLabel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage), "emxFramework.Consolidate.Collections");
        settings.put("Registered Suite","Framework");
        parentMap.put("type", "command");
        parentMap.put("label",strCmdLabel);
        parentMap.put("description","Dynamic Collection Menu");
        parentMap.put("roles", new StringList("all"));
        parentMap.put("settings", settings);
        try {
            //Settings for static command AEFAddToClipboardCollection
            HashMap addToClipMap = new HashMap();
            HashMap addToClipSettingMap = new HashMap();
            addToClipSettingMap.put("Image","../common/images/iconSmallClipboardCollections.gif");
            addToClipSettingMap.put("Registered Suite", "Framework");
            addToClipSettingMap.put("Row Select", "multi");
            addToClipSettingMap.put("Submit", "true");
            addToClipSettingMap.put("Target Location", "listHidden");
            addToClipMap.put("type", "command");
            addToClipMap.put("Name", "AEFAddToClipboardCollection");
            addToClipMap.put("label",addToClipboardLabel);
            addToClipMap.put("href","../common/emxCollectionsAddToProcess.jsp?mode=Clipboard");
            addToClipMap.put("description", "Adding the selected items to System Clipboard Collections");
            addToClipMap.put("settings", addToClipSettingMap);
            childrenMapList.add(addToClipMap);

            //Settings for static command AEFNewAddToCollections
            HashMap addToCollectMap = new HashMap();
            HashMap addToCollectSettingMap = new HashMap();
            addToCollectSettingMap.put("Image","../common/images/iconActionAdd.gif");
            addToCollectSettingMap.put("Registered Suite", "Framework");
            addToCollectSettingMap.put("Row Select", "multi");
            addToCollectSettingMap.put("Submit", "true");
            addToCollectSettingMap.put("Target Location", "listHidden");
            addToCollectMap.put("type", "command");
            addToCollectMap.put("Name", "AEFNewAddToCollections");
            addToCollectMap.put("label",addToCollectionsLabel);
            addToCollectMap.put("href","../common/emxCollectionsAddToProcess.jsp");
            addToCollectMap.put("description", "Creating a collection or adding the selected items to Collections");
            addToCollectMap.put("settings", addToCollectSettingMap);
            childrenMapList.add(addToCollectMap);

            //Settings for Toolbar seperator
            HashMap separatorMap = new HashMap();
            HashMap separatorSettingMap = new HashMap();
            separatorSettingMap.put("Registered Suite", "Framework");
            separatorSettingMap.put("Action Type", "Separator");
            separatorMap.put("type", "command");
            separatorMap.put("Name", "AEFSeparator");
            separatorMap.put("label", "AEFSeparator");
            separatorMap.put("description", "Use as separator for toolbar buttons");
            separatorMap.put("alt", "AEFSeparator");
            separatorMap.put("settings", separatorSettingMap);
            childrenMapList.add(separatorMap);
            String contxtUser = context.getUser() ;
            String strRes  = MqlUtil.mqlCommand(context, "list set user $1 select $2 $3 $4 dump $5",contxtUser,"name","originated","hidden","|");
            
            MapList collectList=new MapList();
            if(!"".equalsIgnoreCase(strRes) && !"null".equalsIgnoreCase(strRes)){
            StringList st = FrameworkUtil.split(strRes, "\n");            
            for(int i=0;i<st.size();i++) {
                StringList indList= FrameworkUtil.split((String)st.get(i),"|");
                String strCollectName = (String)indList.get(0);
                String strOriginDate = (String)indList.get(1);
				String isHidden = (String)indList.get(2);
                String strSystemGeneratedCollection = EnoviaResourceBundle.getProperty(context, "emxFramework.ClipBoardCollection.Name");
                if(strCollectName.equalsIgnoreCase(strSystemGeneratedCollection)) {
                    boolean isEmptyCollection = isEmptyCollection(context, strCollectName);
                    strCollectName = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale(strlanguage), "emxFramework.ClipBoardCollection.NameLabel");
                    HashMap clipBoardMapSettingMap = new HashMap();
                    clipBoardMapSettingMap.put("Registered Suite", "Framework");

                    /* added for IR-010620V6R2010x */
                    if(isEmptyCollection){
                    	clipBoardMapSettingMap.put("Access Behavior", "disable");
                    	clipBoardMapSettingMap.put("Access Expression", "false");
                    }
                    //end
                    clipBoardMap.put("type", "command");
                    clipBoardMap.put("label", strCollectName);
                    clipBoardMap.put("description","Dynamic Collection Command");
                    clipBoardMap.put("href", "javascript:window.parent.searchWithinCollection('"+(strSystemGeneratedCollection.contains("'")?strSystemGeneratedCollection.replace("'", "\\\\'"):strSystemGeneratedCollection+"')"));
                    clipBoardMap.put("roles", new StringList("all"));
                    clipBoardMap.put("settings", clipBoardMapSettingMap);
                    childrenMapList.add(clipBoardMap);
                }
                //Added for bug 350266  - modified condition to NOT
                 else if((!strCollectName.startsWith(".") && !strCollectName.startsWith("autoset.pool:") ) && isHidden.equalsIgnoreCase("false"))  {
                    HashMap collectMap = new HashMap();
                    collectMap.put("name",strCollectName);
                    collectMap.put("date",strOriginDate);
                    collectList.add(collectMap);
                }
            }
            }
            if(collectList.size() > 0) {
                collectList.sort("date", "decending","date");
            }
            for (int i=0;i<collectList.size();i++) {
                //if(i < 9) {
                    HashMap collMap = (HashMap)collectList.get(i);
                    String sCollectionName  = (String)collMap.get("name");
                    HashMap dynamicSettingMap = new HashMap();

                    /* added for IR-010620V6R2010x */
                    boolean isEmptyCollection = isEmptyCollection(context, sCollectionName);
                    if(isEmptyCollection){
                    	dynamicSettingMap.put("Access Behavior", "disable");
                    	dynamicSettingMap.put("Access Expression", "false");
                    }
                    //end

                    dynamicSettingMap.put("Registered Suite", "Framework");
                    HashMap childrenMap = new HashMap();
                    childrenMap.put("type", "command");
                    childrenMap.put("label", sCollectionName);
                    childrenMap.put("description", "Dynamic Collection Command");
                    childrenMap.put("href", "javascript:window.parent.searchWithinCollection('"+(sCollectionName.contains("'")?sCollectionName.replace("'", "\\\\'"):sCollectionName)+"')");
                    childrenMap.put("roles", new StringList("all"));
                    childrenMap.put("settings", dynamicSettingMap);
                    childrenMapList.add(childrenMap);
                //}
            }
        }
        catch (Exception e) {
            throw new Exception(e.toString());
        }
        parentMap.put("Children", childrenMapList);
        return parentMap;
    }


    protected boolean isEmptyCollection(Context context, String collectionsCsl) {
    	boolean isEmptyCollection = false ;
    	try
    	{
    		StringList allOids = new StringList();
    		StringList setList = new StringList(1);
    		setList.addElement(DomainObject.SELECT_ID);
    		StringTokenizer strTokens = new StringTokenizer(collectionsCsl, ",");
    		while (strTokens.hasMoreTokens()) {
    			String colName = strTokens.nextToken();
    			if (canUse(colName)) {
     				String oids;
    				oids= MqlUtil.mqlCommand(context, "print set $1 select $2 dump recordsep $3",colName,"id","|");
    				allOids.addAll(FrameworkUtil.split(oids, "|"));
    			}
    		}

    		isEmptyCollection = allOids.size()>0 ?  false : true;
    	}
    	catch (FrameworkException e)
    	{
    		e.printStackTrace();
    	}
    	return isEmptyCollection;
    }

	protected boolean canUse(String paramvalue) {
		if (paramvalue != null && !"".equals(paramvalue.trim()) && !"null".equals(paramvalue)) {
			return true;
		} else {
			return false;
		}
	}
}
