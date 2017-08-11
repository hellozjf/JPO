/*
**  emxAPPQuickFileBase
** Created on Jun 28, 2007
** Dassault Systemes, 1993  2007. All rights reserved.
** All Rights Reserved
** This program contains proprietary and trade secret information of
** Dassault Systemes.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
*/
import matrix.db.*;
import matrix.util.*;

import java.util.*;

import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.common.*;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.PropertyUtil;


/**
 * The <code>emxAPPQuickFileBase</code> class contains methods for the "Defualt RMB Menu" Common Component.
 *
 * @version AEF 10.0.Patch SP3 - Copyright (c) 2003, MatrixOne, Inc.
 */

 public class emxAPPQuickFileBase_mxJPO{


    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.7 SP3
     */


	public emxAPPQuickFileBase_mxJPO(Context context, String[] args)	throws Exception
	{

	}

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return int 0, status code.
     * @throws Exception if the operation fails
     * @since AEF 10.7 SP3
     */

    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        return 0;
    }


    /**
     * This method gets the List of Files Checjed into the Related Object
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return HashMap contains list of Files
     * @throws Exception if the operation fails
     * @since AEF 10.7 SP3
     */


    public HashMap listReferenceDocuments(Context context ,
            String[] args )throws Exception
    {
        HashMap hmpInput  = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) hmpInput.get("paramMap");
        HashMap commandMap = (HashMap) hmpInput.get("commandMap");
        HashMap settingsMap = (HashMap) commandMap.get("settings");
        HashMap requestMap = (HashMap) hmpInput.get("requestMap");
        String strlanguage = (String) requestMap.get("languageStr");;
        String strAlwdFormat = (String) settingsMap.get("Allowed formats");
        StringList strFormatList = FrameworkUtil.split(strAlwdFormat,",");
        MapList mapListChildren = new MapList();
        HashMap hmpDummy = new HashMap();
        hmpDummy.put("type","menu");
        hmpDummy.put("label","I am dummy map");
        hmpDummy.put("description","get all the files checked into the object");
        hmpDummy.put("roles",new StringList("all"));
        hmpDummy.put("settings",null);
        MapList mapContent = new MapList();
        String strRelName = (String) settingsMap.get("Relationship Filter");
        StringList relList = null;

        Hashtable hmpChild = new Hashtable();
        int intSize = 0;
        String strTypeId = "";
        HashMap hmpNew  = null;
        HashMap  settings  = new HashMap();
        settings.put("Registered Suite","Components");
        settings.put("Image","../common/images/iconSmallDocumentGray.gif");
        settings.put("Registered Suite","Framework");
        settings.put("Pull Right","false");
        StringList selectList = new StringList(2);
        selectList.add(DomainConstants.SELECT_TYPE);
        selectList.add(DomainConstants.SELECT_NAME);

        if (strRelName != null && !"".equals(strRelName))
        {
            //Split the relationship which is been passed as comma separated.
            relList = FrameworkUtil.split(strRelName,",");
            StringList relSymbolicList = new StringList();
            StringList selects = new StringList();
            Hashtable resultList = null;
            StringList idsList  = new StringList();
            //Get the Actual name of the relationships
            if(relList != null)
            {
                for(int i = 0; i < relList.size(); i++)
                {
                    String sRel = (String) relList.get(i);
                    sRel  = replaceSymbolicTokens(context, sRel);
                    relSymbolicList.add(sRel);
                    selects.add(sRel);
                }
            }
            String strObjId = "";
            String rmbTableRowId = (String) paramMap.get("rmbTableRowId");
            StringList sList = FrameworkUtil.split(rmbTableRowId,"|");
            if(sList.size() == 3){
                strObjId = (String)sList.get(0);
            }else if(sList.size() == 4){
                strObjId = (String)sList.get(1);
            }else if(sList.size() == 2){
                strObjId = (String)sList.get(1);
            }else{
                strObjId = rmbTableRowId;
            }
            DomainObject  doObj  = new DomainObject(strObjId);
            String sId = doObj.getInfo(context,"id");
			boolean versionable = CommonDocument.allowFileVersioning(context, sId);
            boolean contextObjectId = false;
            String sExpr = "";
            for(int k=0;k<selects.size();k++) {
                sExpr = (String)selects.get(k);
                idsList.addAll(FrameworkUtil.split(MqlUtil.mqlCommand(context,"print bus $1 select $2 dump",true, sId, sExpr),","));

                // added for bug 347031
				if(versionable && sExpr != null && sExpr.indexOf("Active Version") != -1){
                    contextObjectId = true;
                }
            }

			if(versionable && contextObjectId && !idsList.contains(sId)){
                    idsList.add(sId);
            }

            if (idsList != null && idsList.size() > 0)
            {
                for(int j = 0; j < idsList.size() ; j++)
                {

                    HashMap hmpDocument = null;
                    strTypeId = (String) idsList.get(j);
                    hmpNew = new HashMap();
                    hmpNew.put("objectId",strTypeId);
                    hmpNew.put("Allowed_Formats",strFormatList);
                    String[] args2 = JPO.packArgs(hmpNew);
                    MapList listOfFiles = (MapList) getCheckedInFiles(context,args2);
                    DomainObject doDocument  = new DomainObject(strTypeId);
                    if (listOfFiles != null && listOfFiles.size() > 0 )
                    {
                        Map ObjectDetails = doDocument.getInfo(context,selectList);
                        hmpDocument = new HashMap();
                        hmpDocument.put("type","menu");
                        hmpDocument.put("label",ObjectDetails.get(DomainConstants.SELECT_TYPE)+" "+ObjectDetails.get(DomainConstants.SELECT_NAME));
                        hmpDocument.put("description","Documents");
                        hmpDocument.put("roles",new StringList("all"));
                        hmpDocument.put("settings",settings);
                        hmpDocument.put("Children",listOfFiles);
                    }
                    if (hmpDocument != null)
                    {
                        mapListChildren.add(hmpDocument);

                    }

                }

            }

        }
        else
        {
            strRelName = CommonDocument.SYMBOLIC_relationship_ReferenceDocument;

            paramMap.put("parentRelName",strRelName);
            String rmbTableRowId = (String) paramMap.get("rmbTableRowId");
            String strObjId = "";
            StringList sList = FrameworkUtil.split(rmbTableRowId,"|");
            if(sList.size() == 3){
                strObjId = (String)sList.get(0);
            }else if(sList.size() == 4){
                strObjId = (String)sList.get(1);
            }else if(sList.size() == 2){
                strObjId = (String)sList.get(1);
            }else{
                strObjId = rmbTableRowId;
            }
            paramMap.put("objectId",strObjId);
            String[] args1 = JPO.packArgs(paramMap);
            emxCommonDocumentUI_mxJPO docUI = new emxCommonDocumentUI_mxJPO(context, null);
            MapList documents = (MapList) docUI.getDocuments(context,args1);

            if (documents != null && documents.size() > 0)
            {
                intSize = documents.size();
                for (int i = 0; i < intSize; i++)
                {
                    HashMap hmpDocument = null;
                    try
                    {
                        hmpChild = (Hashtable) documents.get(i);
                    }
                    catch(Exception e)
                    {
                        System.out.println("exception"+e);
                    }
                    strTypeId = (String) hmpChild.get("id");
                    strRelName = (String) hmpChild.get("relationship");
                    hmpNew = new HashMap();
                    hmpNew.put("objectId",strTypeId);
                    hmpNew.put("Allowed_Formats",strFormatList);
                    String[] args2 = JPO.packArgs(hmpNew);
                    MapList listOfFiles = (MapList) getCheckedInFiles(context,args2);
                    DomainObject doDocument  = new DomainObject(strTypeId);
                    if (listOfFiles != null && listOfFiles.size() > 0 )
                    {
                        Map ObjectDetails = doDocument.getInfo(context,selectList);
                        hmpDocument = new HashMap();
                        hmpDocument.put("type","menu");
                        hmpDocument.put("label",ObjectDetails.get(DomainConstants.SELECT_TYPE)+" "+ObjectDetails.get(DomainConstants.SELECT_NAME));
                        hmpDocument.put("description","Documents");
                        hmpDocument.put("roles",new StringList("all"));
                        hmpDocument.put("settings",settings);
                        hmpDocument.put("Children",listOfFiles);
                    }
                    if (hmpDocument != null)
                    {
                        mapListChildren.add(hmpDocument);

                    }
                }
            }
        }
        if (mapListChildren.size() == 0)
        {
            HashMap hmpDummyChild = new HashMap();
            hmpDummyChild.put("type","command");
            //hmpDummyChild.put("href","javascript:showLabel()");
			Locale strLocale = new Locale(strlanguage);
            hmpDummyChild.put("label",EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.RMBMenu.NoItemFound"));
            hmpDummyChild.put("description","Incase there are no related files");
            hmpDummyChild.put("roles",new StringList("all"));
            hmpDummyChild.put("settings",settings);
            mapListChildren.add(hmpDummyChild);

        }
        commandMap.remove("Children");
        commandMap.remove("type");
        settingsMap.remove("Dynamic Command Function");
        settingsMap.remove("Dynamic Command Program");
        commandMap.put("settings",settingsMap);
        commandMap.put("type","menu");
        commandMap.put("Children",mapListChildren);
        mapContent.add(commandMap);
        hmpDummy.put("Children",mapContent);
        return hmpDummy;
    }

    /**
    * This method is used to get the checked in files for the  given object
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    * @returns MapList
    * @throws Exception if the operation fails
    * @since AEF 10.7 SP3
    */

    public MapList getCheckedInFiles(Context context ,
                String[] args )throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String partId = (String) paramMap.get("objectId");
        StringList strFormatList = (StringList) paramMap.get("Allowed_Formats");
        boolean bolNoFormatchk  = false;
        if (strFormatList == null || strFormatList.size() == 0)
        {
            bolNoFormatchk = true;
        }
        DomainObject object = new DomainObject(partId);
        MapList CheckedInFiles = new MapList();

        StringList selectList = new StringList(3);
        selectList.add(CommonDocument.SELECT_FILE_NAME);
        selectList.add(CommonDocument.SELECT_FILE_FORMAT);
        selectList.add(CommonDocument.SELECT_FILE_SIZE);

        String file = "";
        String format = "";
        String fileSize   = "";
        int i  = 0 ;
        int fileSizeKB =  0;
        int intdivFactor = 1024;

        HashMap  settings  = new HashMap();
        settings.put("Registered Suite","Components");
        settings.put("Image","../common/images/iconSmallDocumentGray.gif");
        settings.put("Registered Suite","Framework");
        settings.put("Pull Right","false");

        boolean versionable = CommonDocument.allowFileVersioning(context, partId);
        MapList fileInDoc  = null;
        String[] args1 = JPO.packArgs(paramMap);
        // To get the Versioned file
        if(versionable)
        {
            emxCommonFileUI_mxJPO fileUI = new emxCommonFileUI_mxJPO(context, null);
            fileInDoc = (MapList)fileUI.getFiles(context, args1);
        }
        else
        {
            // To get the non Versioned file
            emxCommonFileUI_mxJPO fileUI = new emxCommonFileUI_mxJPO(context, null);
            fileInDoc = (MapList)fileUI.getNonVersionableFiles(context, args1);
        }
        if (fileInDoc != null && fileInDoc.size() > 0 )
        {
            Map fileMap = null;
            int noOfFiles  = fileInDoc.size();
            for(int j = 0; j < noOfFiles ; j++ )
            {
                fileMap = (Map) fileInDoc.get(j);
                format = (String) fileMap.get("format.file.format");
                file  = (String) fileMap.get("format.file.name");
                if ((bolNoFormatchk || strFormatList.contains(format)) && !"".equals(file))
                {
                    HashMap hmpChildMap = new HashMap();
                    fileSize = (String) fileMap.get("format.file.size");
                    fileSizeKB = 0;
                    if(fileSize != null && !"".equals(fileSize))
                    {
                        fileSizeKB = Integer.parseInt(fileSize)/1024;
                    }
                    hmpChildMap.put("type","command");
                    hmpChildMap.put("label",file+" ("+fileSizeKB+" KB)");
                    hmpChildMap.put("description","file details");
                    hmpChildMap.put("roles",new StringList("all"));
                    hmpChildMap.put("href","javascript:callCheckout('"+partId+"','download','"+ file+"', '"+format+"', null, null, null, null, null,null)");
                    hmpChildMap.put("settings",settings);
                    CheckedInFiles.add(hmpChildMap);
                }
            }
        }
        return CheckedInFiles;

    }
    /**
    * This method is used to get the display the checked in files
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    * @returns HashMap
    * @throws Exception if the operation fails
    * @since AEF 10.7 SP3
    */
    public HashMap listQuickFiles(Context context ,
                    String[] args )throws Exception
    {
        HashMap hmpInput  = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) hmpInput.get("paramMap");
        HashMap commandMap = (HashMap) hmpInput.get("commandMap");
        HashMap hmpSettings = (HashMap) commandMap.get("settings");
        HashMap requestMap = (HashMap) hmpInput.get("requestMap");
        String strlanguage = (String) requestMap.get("languageStr");
        String strAlwdFormat = (String) hmpSettings.get("Allowed formats");
        StringList strFormatList = FrameworkUtil.split(strAlwdFormat,",");
        paramMap.put("Allowed_Formats",strFormatList);
        String strObjId = "";
        String rmbTableRowId = (String) paramMap.get("rmbTableRowId");
        StringList sList = FrameworkUtil.split(rmbTableRowId,"|");
        if(sList.size() == 3){
            strObjId = (String)sList.get(0);
        }else if(sList.size() == 4){
            strObjId = (String)sList.get(1);
        }else if(sList.size() == 2){
            strObjId = (String)sList.get(1);
        }else{
            strObjId = rmbTableRowId;
        }
        paramMap.put("objectId",strObjId);
        String[] args1 = JPO.packArgs(paramMap);
        HashMap hmpDummy = new HashMap();
        hmpDummy.put("type","menu");
        hmpDummy.put("label","I am dummy map");
        hmpDummy.put("description","get all the files checked into the object");
        hmpDummy.put("roles",new StringList("all"));
        hmpDummy.put("settings",null);
        MapList mapContent = new MapList();
        MapList mapListChildren = getCheckedInFiles(context,args1);
        if (mapListChildren.size() == 0)
        {
            HashMap hmpDummyChild = new HashMap();
            hmpDummyChild.put("type","command");
			Locale strLocale = new Locale(strlanguage);
            hmpDummyChild.put("label",EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.RMBMenu.NoItemFound"));
            //hmpDummyChild.put("href","javascript:showLabel()");
            hmpDummyChild.put("description","Incase there are no related files");
            hmpDummyChild.put("roles",new StringList("all"));
            hmpDummyChild.put("settings",null);
            mapListChildren.add(hmpDummyChild);

        }
        commandMap.remove("Children");
        commandMap.remove("type");
        hmpSettings.remove("Dynamic Command Function");
        hmpSettings.remove("Dynamic Command Program");
        commandMap.put("settings",hmpSettings);
        commandMap.put("Children",mapListChildren);
        commandMap.put("type","menu");
        mapContent.add(commandMap);
        hmpDummy.put("Children",mapContent);
        return hmpDummy;
    }
    /**
    * This method is used to get the display the related files
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    * @returns HashMap
    * @throws Exception if the operation fails
    * @since AEF 10.7 SP3
    */
    public HashMap listRelatedFiles(Context context ,
                        String[] args )throws Exception
    {

        HashMap hmpInput  = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) hmpInput.get("paramMap");
        HashMap commandMap = (HashMap) hmpInput.get("commandMap");
        HashMap settingsMap = (HashMap) commandMap.get("settings");
        HashMap requestMap = (HashMap) hmpInput.get("requestMap");
        String strlanguage = (String) requestMap.get("languageStr");
        String strAlwdFormat = (String) settingsMap.get("Allowed formats");
        StringList strFormatList = FrameworkUtil.split(strAlwdFormat,",");
        MapList mapListChildren = new MapList();
        HashMap hmpDummy = new HashMap();
        hmpDummy.put("type","menu");
        hmpDummy.put("label","I am dummy map");
        hmpDummy.put("description","get all the files checked into the object");
        hmpDummy.put("roles",new StringList("all"));
        hmpDummy.put("settings",null);
        MapList mapContent = new MapList();
        String strRelName = UIUtil.getQuickFileRelPath(context);
        paramMap.put("parentRelName",strRelName);
        String strObjId = "";
        String rmbTableRowId = (String) paramMap.get("rmbTableRowId");
        StringList sList = FrameworkUtil.split(rmbTableRowId,"|");
        if(sList.size() == 3){
            strObjId = (String)sList.get(0);
        }else if(sList.size() == 4){
            strObjId = (String)sList.get(1);
        }else if(sList.size() == 2){
            strObjId = (String)sList.get(1);
        }else{
            strObjId = rmbTableRowId;
        }
        paramMap.put("objectId",strObjId);
        String[] args1 = JPO.packArgs(paramMap);
        emxCommonDocumentUI_mxJPO docUI = new emxCommonDocumentUI_mxJPO(context, null);
        MapList documents = (MapList) docUI.getDocuments(context,args1);
        Hashtable hmpChild = new Hashtable();
        int intSize = 0;
        String strTypeId = "";
        HashMap hmpNew  = null;
        HashMap  settings  = new HashMap();
        settings.put("Registered Suite","Components");
        settings.put("Image","../common/images/iconSmallDocumentGray.gif");
        settings.put("Registered Suite","Framework");
        settings.put("Pull Right","false");
        StringList selectList = new StringList(2);
        selectList.add(DomainConstants.SELECT_TYPE);
        selectList.add(DomainConstants.SELECT_NAME);
        if (documents != null && documents.size() > 0)
        {
            intSize = documents.size();
            for (int i =0; i < intSize; i++)
            {
                HashMap hmpDocument = null;
                try
                {
                    hmpChild = (Hashtable) documents.get(i);
                }
                catch(Exception e)
                {
                    System.out.println("exception"+e);
                }
                strTypeId = (String) hmpChild.get("id");
                strRelName = (String) hmpChild.get("relationship");
                hmpNew = new HashMap();
                hmpNew.put("objectId",strTypeId);
                hmpNew.put("Allowed_Formats",strFormatList);
                String[] args2 = JPO.packArgs(hmpNew);
                MapList listOfFiles = (MapList) getCheckedInFiles(context,args2);
                DomainObject doDocument  = new DomainObject(strTypeId);
                if (listOfFiles != null && listOfFiles.size() > 0 )
                {
                    Map ObjectDetails = doDocument.getInfo(context,selectList);
                    hmpDocument = new HashMap();
                    hmpDocument.put("type","menu");
                    hmpDocument.put("label",ObjectDetails.get(DomainConstants.SELECT_TYPE)+" "+ObjectDetails.get(DomainConstants.SELECT_NAME)+"("+strRelName+")");
                    hmpDocument.put("description","Documents");
                    hmpDocument.put("roles",new StringList("all"));
                    hmpDocument.put("href","../common/emxSearchGeneral.jsp");
                    hmpDocument.put("settings",settings);
                    hmpDocument.put("Children",listOfFiles);
                }
                if (hmpDocument != null)
                {
                    mapListChildren.add(hmpDocument);

                }
            }
        }
        if (mapListChildren.size() == 0)
        {
            HashMap hmpDummyChild = new HashMap();
            hmpDummyChild.put("type","command");
            //hmpDummyChild.put("href","javascript:showLabel()");
            Locale strLocale = new Locale(strlanguage);
            hmpDummyChild.put("label",EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.RMBMenu.NoItemFound"));
            hmpDummyChild.put("description","Incase there are no related files");
            hmpDummyChild.put("roles",new StringList("all"));
            hmpDummyChild.put("settings",null);
            mapListChildren.add(hmpDummyChild);
        }
        commandMap.remove("Children");
        commandMap.remove("type");
        HashMap hmpSettings = (HashMap)commandMap.get("settings");
        hmpSettings.remove("Dynamic Command Function");
        hmpSettings.remove("Dynamic Command Program");
        commandMap.put("settings",hmpSettings);
        commandMap.put("type","menu");
        commandMap.put("Children",mapListChildren);
        mapContent.add(commandMap);
        hmpDummy.put("Children",mapContent);
        return hmpDummy;
    }
    /**
    * This method is used to get the display the properties page of object
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    * @returns HashMap
    * @throws Exception if the operation fails
    * @since AEF 10.7 SP3
    */
    public HashMap getProperties(Context context ,
                        String[] args )throws Exception
    {

        HashMap hmpDummy = new HashMap();
        hmpDummy.put("type","menu");
        hmpDummy.put("label","I am dummy map");
        hmpDummy.put("description","get all the files checked into the object");
        hmpDummy.put("roles",new StringList("all"));
        hmpDummy.put("settings",null);
        MapList mapContent = new MapList();
        HashMap hmpInput  = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) hmpInput.get("paramMap");
        HashMap commandMap = (HashMap) hmpInput.get("commandMap");
        HashMap hmpSettings = (HashMap)commandMap.get("settings");
        hmpSettings.remove("Dynamic Command Function");
        hmpSettings.remove("Dynamic Command Program");
        String strObjId = "";
        String rmbTableRowId = (String) paramMap.get("rmbTableRowId");
        StringList sList = FrameworkUtil.split(rmbTableRowId,"|");
        if(sList.size() == 3){
            strObjId = (String)sList.get(0);
        }else if(sList.size() == 4){
            strObjId = (String)sList.get(1);
        }else if(sList.size() == 2){
            strObjId = (String)sList.get(1);
        }else{
            strObjId = rmbTableRowId;
        }
        commandMap.put("settings",hmpSettings);
        commandMap.put("href","../common/emxDynamicAttributes.jsp?objectId="+strObjId);
        mapContent.add(commandMap);
        hmpDummy.put("Children",mapContent);
        return hmpDummy;
    }
        /**
        * This method is used to get the list of files and their
        * versions in master (i.e. document holder) object
        * @param context the eMatrix <code>Context</code> object
        * @param args holds the following input arguments:
        * @returns HashMap
        * @throws Exception if the operation fails
        * @since AEF 10.7 SP3
        */
        public HashMap listFileVersion(Context context ,
            String[] args )throws Exception
        {
            HashMap hmpInput   = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap) hmpInput.get("paramMap");
            HashMap commandMap = (HashMap) hmpInput.get("commandMap");
            HashMap requestMap = (HashMap) hmpInput.get("requestMap");
            String strlanguage = (String) requestMap.get("languageStr");
            MapList mapListChildren = new MapList();
            HashMap hmpSetting = new HashMap();
            hmpSetting.put("Action Type","Separator");
            hmpSetting.put("Registered Suite","Framework");
            commandMap.remove("Children");
            commandMap.remove("type");
            String strName  = "";
            String strFormat = "";
            String strFileSize = "";
            String strID = "";
            int fileSizeKB =  0;
            int intdivFactor = 1024;
            String strObjId = "";
            String rmbTableRowId = (String) paramMap.get("rmbTableRowId");
            StringList sList = FrameworkUtil.split(rmbTableRowId,"|");
            if(sList.size() == 3){
                strObjId = (String)sList.get(0);
            }else if(sList.size() == 4){
                strObjId = (String)sList.get(1);
            }else if(sList.size() == 2){
                strObjId = (String)sList.get(1);
            }else{
                strObjId = rmbTableRowId;
            }
            paramMap.put("objectId",strObjId);
            String[] args1 = JPO.packArgs(paramMap);
            emxCommonFileUI_mxJPO fileUI = new emxCommonFileUI_mxJPO(context, null);
            MapList fileListMap = (MapList)fileUI.getFileVersions(context, args1);
            for (int i = 0 ; i < fileListMap.size() ; i++)
            {
                HashMap fileMap  = (HashMap) fileListMap.get(i);
                if (!"".equals(strName) && !strName.equals(fileMap.get("attribute[Title]")))
                {
                    HashMap hmpSep = new HashMap();
                    hmpSep.put("type","command");
                    hmpSep.put("label","AEFSeparator");
                    hmpSep.put("type","command");
                    hmpSep.put("settings",hmpSetting);
                    mapListChildren.add(hmpSep);
                }
                strName = (String) fileMap.get("attribute[Title]");
                strFormat = (String) fileMap.get("format.file.format");
                strFileSize = (String) fileMap.get("format.file.size");
                fileSizeKB = 0;
                if(strFileSize !=null && !"".equals(strFileSize))
                {
                    fileSizeKB = (Integer.parseInt(strFileSize))/1024;
                }
                String lastVersionId = (String) fileMap.get(CommonDocument.SELECT_LAST_ID);
                String versionId = (String) fileMap.get("id");
                strID = versionId.equals(lastVersionId) ? strObjId : versionId;
                HashMap hmpDocument = new HashMap();
                hmpDocument.put("type","command");
                hmpDocument.put("label",fileMap.get("attribute[Title]")+" rev "+fileMap.get("revision")+"("+fileSizeKB+" KB)");
                hmpDocument.put("description","Documents");
                hmpDocument.put("roles",new StringList("all"));
                hmpDocument.put("href","javascript:callCheckout('"+strID+"','download','"+ strName+"', '"+strFormat+"', null, null, null, null, null,null)");
                hmpDocument.put("settings",null);
                mapListChildren.add(hmpDocument);

            }
            if (mapListChildren.size() == 0)
            {
                HashMap hmpDummyChild = new HashMap();
                hmpDummyChild.put("type","command");
                //hmpDummyChild.put("href","javascript:showLabel()");
                Locale strLocale = new Locale(strlanguage);
            hmpDummyChild.put("label",EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",strLocale, "emxFramework.RMBMenu.NoItemFound"));
                hmpDummyChild.put("description","Incase there are no related files");
                hmpDummyChild.put("roles",new StringList("all"));
                hmpDummyChild.put("settings",null);
                mapListChildren.add(hmpDummyChild);
            }
            commandMap.put("Children",mapListChildren);
            commandMap.put("type","menu");
            HashMap hmpSettings = (HashMap)commandMap.get("settings");
            hmpSettings.remove("Dynamic Command Function");
            hmpSettings.remove("Dynamic Command Program");
            commandMap.remove("settings");
            commandMap.put("settings",hmpSettings);
            HashMap hmpDummy = new HashMap();
            hmpDummy.put("type","menu");
            hmpDummy.put("label","I am dummy map");
            hmpDummy.put("description","get all the files checked into the object");
            hmpDummy.put("roles",new StringList("all"));
            hmpDummy.put("settings",null);
            MapList mapContent = new MapList();
            mapContent.add(commandMap);
            hmpDummy.put("Children",mapContent);
            return hmpDummy;

    }
        /**
        * This method is used to get the list of files and their
        * versions in master (i.e. document holder) object
        * @param context the eMatrix <code>Context</code> object
        * @param args holds the following input arguments:
        * @returns HashMap
        * @throws Exception if the operation fails
        * @since
        */
        public HashMap getQuickFileTable(Context context ,
            String[] args )throws Exception
        {
            HashMap hmpInput   = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap) hmpInput.get("paramMap");
            HashMap commandMap = (HashMap) hmpInput.get("commandMap");
            String partId = "";
            String rmbTableRowId = (String) paramMap.get("rmbTableRowId");
            StringList sList = FrameworkUtil.split(rmbTableRowId,"|");
            if(sList.size() == 3){
                partId = (String)sList.get(0);
            }else if(sList.size() == 4){
                partId = (String)sList.get(1);
            }else if(sList.size() == 2){
                partId = (String)sList.get(1);
            }else{
                partId = rmbTableRowId;
            }
            String strURL = "../common/emxTable.jsp?program=emxQuickFileAccess:getQuickFileObjects&table=APPQuickFileSummary&sortColumnName=ContainedIn&emxSuiteDirectory=components&suiteKey=Components&sortDirection=ascending&header=emxComponents.Common.FileDetails&CancelButton=true&CancelLabel=emxComponents.Button.Close&HelpMarker=emxhelpquickfileview&FilterFramePage=../components/emxCommonDocumentCheckoutUtil.jsp&FilterFrameSize=1";
            String onselectAction = "";
            String sRelation = UIUtil.getQuickFileRelPath(context);
            String strImg  ="";
            String popupModal = "true";
            String targetLocation = "popup";
            StringBuffer sbfItem = new StringBuffer();
            //sbfItem.append("javascript:showNonModalDialog('");
            sbfItem.append("javascript:emxTableColumnLinkClick('");
            sbfItem.append(strURL);
            sbfItem.append("&objectId=");
            sbfItem.append(partId);
            sbfItem.append("&relationship=");
            sbfItem.append(sRelation);
            sbfItem.append("', '700', '600', '");
            sbfItem.append(popupModal);
            sbfItem.append("', '");
            sbfItem.append(targetLocation);
            sbfItem.append("', '");
            sbfItem.append(onselectAction);
            sbfItem.append("')");
            //sbfItem.append(strImg);
            HashMap hmpDummy = new HashMap();
            hmpDummy.put("type","menu");
            hmpDummy.put("label","I am dummy map");
            hmpDummy.put("description","get all the files checked into the object");
            hmpDummy.put("roles",new StringList("all"));
            MapList mapContent = new MapList();
            HashMap hmpSettings = (HashMap)commandMap.get("settings");
            hmpSettings.remove("Dynamic Command Function");
            hmpSettings.remove("Dynamic Command Program");
            hmpSettings.put("Image","../common/images/iconSmallDocumentGray.gif");
            commandMap.remove("settings");
            commandMap.remove("href");
            commandMap.put("href", sbfItem.toString());
            commandMap.remove("Image");
            commandMap.put("settings",hmpSettings);
            mapContent.add(commandMap);
            hmpDummy.put("Children",mapContent);
            return hmpDummy;

        }
        protected static String replaceSymbolicTokens(Context context, String expression)
        throws FrameworkException
    {
        String newExpression = expression;

        if (expression.indexOf("_") > -1)
        {
            StringTokenizer st = new StringTokenizer(expression, " [],\'\"", true);
            StringBuffer buffer = new StringBuffer();
            String token;

            while (true)
            {
                try
                {
                    token = st.nextToken();

                    if (token.indexOf("_") > -1)
                    {
                        buffer.append(lookupSymbolicToken(context, token));
                    }
                    else
                    {
                        buffer.append(token);
                    }
                }
                catch (NoSuchElementException e)
                {
                    break;
                }
            }

            newExpression = buffer.toString();
        }

        return (newExpression);
    }
        protected static String lookupSymbolicToken(Context context, String symbolicToken)
        {
            String token = null;

            try
            {
                token = PropertyUtil.getSchemaProperty(context,symbolicToken);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (token == null || token.length() == 0)
            {
                token = symbolicToken;
            }

			return (token);
		}

}
