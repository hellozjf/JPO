/*
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 */
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FormatUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxFileUtilBase</code> class contains utility methods for
 * getting data using configurable table APPFileSummary
 *
 * @version Common 10.5 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxCommonFileUIBase_mxJPO
{
    /**
     * Constructor
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */
    public emxCommonFileUIBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        return 0;
    }


    /**
     * This method return true if user have permissions on the command
     * otherwise return false.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId and param values.
     * @return Boolean true if FCS is enabled and property set to true othewise return false.
     * @throws Exception If the operation fails.
     * @since EC 10-5.
     */
    public Boolean isInTreeContent(Context context, String[] args)
         throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String isPopup = (String)paramMap.get("popup");
        if ( "true".equalsIgnoreCase(isPopup) )
        {
            return Boolean.valueOf(false);
        } else {
            return Boolean.valueOf(true);
        }

    }
    /**
     * This method return true if user have permissions on the command
     * otherwise return false.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId and param values.
     * @return Boolean true if FCS is enabled and property set to true othewise return false.
     * @throws Exception If the operation fails.
     * @since EC 10-5.
     */
    public Boolean isPopup(Context context, String[] args)
         throws Exception
    {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String isPopup = (String)paramMap.get("popup");
        if ( "true".equalsIgnoreCase(isPopup) )
        {
            return Boolean.valueOf(true);
        } else {
            return Boolean.valueOf(false);
        }

    }



    /**
     * This method is used to get the list of files in
     * master (i.e. document holder) object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getNonVersionableFiles(Context context, String[] args) throws Exception
    {
        try
        {
            HashMap programMap         = (HashMap) JPO.unpackArgs(args);
            String  objectId     = (String) programMap.get("objectId");
            String  appendFileName = (String) programMap.get("AppendFileName");
            DomainObject object  = DomainObject.newInstance(context, objectId);

            //Added to make a single database call to
            StringList selectList = new StringList(12);
            selectList.add(CommonDocument.SELECT_ID);
            selectList.add(CommonDocument.SELECT_FILE_NAME);
            selectList.add(CommonDocument.SELECT_FILE_FORMAT);
            selectList.add(CommonDocument.SELECT_FILE_SIZE);
            selectList.add(CommonDocument.SELECT_FILE_MODIFIED);
            selectList.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            selectList.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
            selectList.add(CommonDocument.SELECT_HAS_LOCK_ACCESS);
            selectList.add(CommonDocument.SELECT_HAS_UNLOCK_ACCESS);
            selectList.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
            selectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            selectList.add(CommonDocument.SELECT_LOCKED);
            selectList.add(CommonDocument.SELECT_LOCKER);

            // get the Master Object data
            Map objectMap = object.getInfo(context,selectList);
            StringList fileList = (StringList)objectMap.get(CommonDocument.SELECT_FILE_NAME);
            StringList formatList = (StringList)objectMap.get(CommonDocument.SELECT_FILE_FORMAT);
            StringList fileSizeList = (StringList)objectMap.get(CommonDocument.SELECT_FILE_SIZE);
            StringList fileModifiedList = (StringList)objectMap.get(CommonDocument.SELECT_FILE_MODIFIED);
            MapList fileMapList = new MapList();
            Iterator fileItr = fileList.iterator();
            Iterator formatItr = formatList.iterator();
            Iterator fileSizeItr = fileSizeList.iterator();
            Iterator fileModifiedItr = fileModifiedList.iterator();
            String file = "";
            String format = "";
            String fileSize   = "";
            String fileModified   = "";
            String canCheckout = (String) objectMap.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            String canCheckin  = (String) objectMap.get(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
            String canLock     = (String) objectMap.get(CommonDocument.SELECT_HAS_LOCK_ACCESS);
            String canUnLock     = (String) objectMap.get(CommonDocument.SELECT_HAS_UNLOCK_ACCESS);
            String locked     = (String) objectMap.get(CommonDocument.SELECT_LOCKED);
            String locker     = (String) objectMap.get(CommonDocument.SELECT_LOCKER);

            while( fileItr.hasNext() && formatItr.hasNext() && fileSizeItr.hasNext() )
            {
                file = (String)fileItr.next();
                format = (String)formatItr.next();
                fileSize = (String)fileSizeItr.next();
                fileModified = (String)fileModifiedItr.next();
                if ( file != null && !"".equals(file) )
                {
                    Map fileMap = new HashMap();
                    if ( appendFileName != null && "true".equalsIgnoreCase(appendFileName) )
                    {
                       fileMap.put("id", objectId+"~"+file+"~"+format);
                    }
                    else
                    {
                       fileMap.put("id", objectId);
                    }
                    fileMap.put("objectId", objectId);
                    fileMap.put(CommonDocument.SELECT_FILE_NAME, file);
                    fileMap.put(CommonDocument.SELECT_FILE_FORMAT, format);
                    fileMap.put(CommonDocument.SELECT_FILE_SIZE, fileSize);
                    fileMap.put(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS, canCheckout);
                    fileMap.put(CommonDocument.SELECT_HAS_CHECKIN_ACCESS, canCheckin);
                    fileMap.put(CommonDocument.SELECT_HAS_LOCK_ACCESS, canLock);
                    fileMap.put(CommonDocument.SELECT_HAS_UNLOCK_ACCESS, canUnLock);
                    fileMap.put(CommonDocument.SELECT_LOCKED, locked);
                    fileMap.put(CommonDocument.SELECT_LOCKER, locker);
                    fileMap.put(CommonDocument.SELECT_FILE_MODIFIED, fileModified);

                    fileMapList.add(fileMap);
                }
            }
            return fileMapList;
        } catch (Exception ex) {
          throw ex;
        }
    }

    /**
     * This method is used to show the Lock image.
     *                This method is called from the Column Lock Image.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */
    public Vector getNonVersionableLockIcon(Context context, String[] args) throws Exception
    {
        Vector showLock= new Vector();
        String statusImageString = "";
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            
            StringList lockinfo = new StringList(2);
            lockinfo.add(CommonDocument.SELECT_LOCKED);
            lockinfo.add(CommonDocument.SELECT_LOCKER);
            objectList = getUpdatedColumnValues(context, objectList, lockinfo);
            
            Map objectMap = null;

            String fileLocked = "";
            String fileLocker = "";

            int objectListSize = 0 ;
            if(objectList != null)
            {
                objectListSize = objectList.size();
            }
            for(int i=0; i< objectListSize; i++)
            {
                objectMap = (Map) objectList.get(i);
                statusImageString = "";
                fileLocked = (String) objectMap.get(CommonDocument.SELECT_LOCKED);
                fileLocker = PersonUtil.getFullName(context,(String) objectMap.get(CommonDocument.SELECT_LOCKER));

                if ("TRUE".equalsIgnoreCase(fileLocked)){
                    statusImageString = "<img border=\"0\" src=\"../common/images/iconStatusLocked.gif\" alt=\"" + XSSUtil.encodeForHTMLAttribute(context, fileLocker) + "\" title=\"" + XSSUtil.encodeForHTMLAttribute(context, fileLocker) + "\"/>";
                }else{
                	statusImageString = "<img border=\"0\" />";
                }
                showLock.add(statusImageString);
            }
            return  showLock;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
      *  method to get file format info from master object
      *  Used in File Summary, Version Summary Page
      *
      *  @param context the eMatrix <code>Context</code> object
      *  @param args an array of String arguments for this method
      *  @returns Vector of file formats
      *  @throws Exception if the operation fails
      *
      *  @since Common 10.5
      */
    public Vector getNonVersionableFileName(Context context, String[] args) throws Exception
    {
        Vector fileNameVector = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Iterator objectListItr = objectList.iterator();
            String fileName = null;
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                fileName = (String) objectMap.get(CommonDocument.SELECT_FILE_NAME);
                fileNameVector.add(fileName);
            }
            return fileNameVector;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
      *  method to get file format info from master object
      *  Used in File Summary, Version Summary Page
      *
      *  @param context the eMatrix <code>Context</code> object
      *  @param args an array of String arguments for this method
      *  @returns Vector of file formats
      *  @throws Exception if the operation fails
      *
      *  @since Common 10.5
      */
    public Vector getNonVersionableFileFormat(Context context, String[] args) throws Exception
    {
        Vector fileFormatVector = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Iterator objectListItr = objectList.iterator();
            String fileFormat = null;
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                fileFormat = (String) objectMap.get(CommonDocument.SELECT_FILE_FORMAT);
                fileFormat = i18nNow.getFormatI18NString(fileFormat, context.getSession().getLanguage());
                fileFormatVector.add(fileFormat);
            }
            return fileFormatVector;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
    *  method to get file size info from master object
    *  Used in File Summary, Version Summary Page
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @returns Vector of file size
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    */
    public Vector getNonVersionableFileSize(Context context, String[] args) throws Exception
    {
        Vector fileSizeVector = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Iterator objectListItr = objectList.iterator();
            String fileSize = null;
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                fileSize = (String) objectMap.get(CommonDocument.SELECT_FILE_SIZE);
                fileSizeVector.add(fileSize);
            }
            return fileSizeVector;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
    *  Get Vector of Strings for Action Icons
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of html code to
    *        construct the Actions Column.
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    *  @grade 0
    */
    public static Vector getNonVersionableFileActions(Context context, String[] args)
        throws Exception
    {

        Vector fileActionsVector = new Vector();

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList      = (Map)programMap.get("paramList");

            boolean isprinterFriendly = false;
            if(paramList.get("reportFormat") != null)
            {
                isprinterFriendly = true;
            }

            Iterator objectListItr = objectList.iterator();
            String languageStr = (String)paramList.get("languageStr");
            Locale strLocale = context.getLocale();



            StringList lockinfo = new StringList(2);
            lockinfo.add(CommonDocument.SELECT_LOCKED);
            lockinfo.add(CommonDocument.SELECT_LOCKER);
            lockinfo.add(CommonDocument.SELECT_ID);

            MapList lockInfo = getUpdatedColumnValues(context, objectList, lockinfo);
            Map fileLockInfoGroypById = new HashMap();
            for (Iterator iter = lockInfo.iterator(); iter.hasNext();) {
                Map element = (Map) iter.next();
                String[] lock = new String[2];
                lock[0] = (String) element.get(CommonDocument.SELECT_LOCKED);
                lock[1] = (String) element.get(CommonDocument.SELECT_LOCKER);
                fileLockInfoGroypById.put(element.get(CommonDocument.SELECT_ID), lock);
            }

            String objectId        = null;
            String fileActions     = null;
            String fileName        = null;
            String fileFormat      = null;
            boolean canCheckout    = false;
            boolean canCheckin     = false;
            boolean canViewAndDownload = false;
            boolean canUnlock = false;

            String strViewerURL = null;
            String downloadURL  = null;
            String checkoutURL  = null;
            String checkinURL   = null;
            String unlockURL = null;

            String sTipDownload = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipDownload");
            String sTipCheckout = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipCheckout");
            String sTipCheckin  = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipCheckin");
            String sTipUnlock  = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipUnlock");
            String sTipLock  = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipLock");
            String viewerTip = "Default";
            String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip, context.getSession().getLanguage());
            while(objectListItr.hasNext())
            {
                fileName    = "";
                fileActions = "";
                StringBuffer fileActionsStrBuff = new StringBuffer();

                Map objectMap = (Map) objectListItr.next();
                objectId = (String) objectMap.get("objectId");
                fileName = (String) objectMap.get(CommonDocument.SELECT_FILE_NAME);
                fileFormat = (String) objectMap.get(CommonDocument.SELECT_FILE_FORMAT);


                String[] fileLockInfo = (String[]) fileLockInfoGroypById.get(objectId);
                String fileLocked = fileLockInfo != null ? fileLockInfo[0] : (String)objectMap.get(CommonDocument.SELECT_LOCKED);
                String fileLockedBy = fileLockInfo != null ? fileLockInfo[1] : (String)objectMap.get(CommonDocument.SELECT_LOCKER);

                canCheckout = "true".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_HAS_LOCK_ACCESS)) &&
                                  "true".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS)) &&
                                  "false".equalsIgnoreCase(fileLocked);

                canCheckin = "true".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_HAS_CHECKIN_ACCESS)) &&
                                  "true".equalsIgnoreCase(fileLocked) &&
                context.getUser().equals(fileLockedBy);

                canViewAndDownload = "true".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS));

                canUnlock = "true".equalsIgnoreCase(fileLocked)&& (context.getUser().equals(fileLockedBy) || "true".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_HAS_UNLOCK_ACCESS)));

// Modified for bug 354871 by replacing &nbsp; with &#160, & with &amp; and ensuring the proper closing tags to support XHTML compatibility
                if ( canViewAndDownload )
                {
                    if ( !isprinterFriendly )
                    {
                      downloadURL = "javascript:callCheckout('"+ XSSUtil.encodeForJavaScript(context, objectId) +"','download', '"+ XSSUtil.encodeForJavaScript(context, fileName)+ "', '" + XSSUtil.encodeForJavaScript(context, fileFormat) +"');";
                      String viewerURL = getViewerURL(context, objectId, fileFormat, fileName);
                      fileActionsStrBuff.append(viewerURL);
                      fileActionsStrBuff.append("<a href=\"" + downloadURL+"\">");
                      fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"></img></a>&#160;");
                    } else {
                      fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionView.gif' alt=\""+i18nViewerTip+"\" title=\""+i18nViewerTip+"\"></img></a>&#160;");
                      fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"></img></a>&#160;");
                    }
                }
                if ( canCheckout )
                {
                  if ( !isprinterFriendly )
                  {
                    checkoutURL = "javascript:findFrame(parent,'listFilter').callCheckout('"+ XSSUtil.encodeForJavaScript(context, objectId) +"','checkout', '"+ XSSUtil.encodeForJavaScript(context, fileName)+ "', '" + XSSUtil.encodeForJavaScript(context, fileFormat) +"');";
                    fileActionsStrBuff.append("<a href=\"" + checkoutURL+"\">");
                    fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionCheckOut.gif' alt=\""+sTipCheckout+"\" title=\""+sTipCheckout+"\"></img></a>&#160;");
                  } else {
                    fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionCheckOut.gif' alt=\""+sTipCheckout+"\" title=\""+sTipCheckout+"\"></img></a>&#160;");
                  }
                }
                else if ( canCheckin )
                {
                  if ( !isprinterFriendly )
                  {
                    checkinURL = "../components/emxCommonDocumentPreCheckin.jsp?override=false&amp;showComments=false&amp;refreshTable=true"
                            + "&amp;objectId="+ XSSUtil.encodeForJavaScript(context, objectId) + "&amp;append=true&amp;objectAction="+ CommonDocument.OBJECT_ACTION_CHECKIN_WITHOUT_VERSION;
                    // Added double url encoding because IE and NS 6 and up need double encoding
                    // and in 10.5 supported Browsers are IE 6 and NS7.1 and UP
                    // so this is ok to have double encoding with out any checks.

                    //IR-012594V6R2010x Start
                    // commented as the &amp; getting encoded which should not be.

                    //checkinURL = FrameworkUtil.encodeURLParamValues(checkinURL);
                    //checkinURL = FrameworkUtil.encodeURLParamValues(checkinURL);
                    //IR-012594V6R2010x End

                    fileActionsStrBuff.append("<a href='javascript:showModalDialog(\"" + checkinURL + "\",730,450)'>");
                    fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionAppend.gif' alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>&#160;");
                  } else {
                    fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionAppend.gif' alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>&#160;");
                  }
                }
                if(canUnlock)
                {
                    if ( !isprinterFriendly )
                    {
                      unlockURL = "../components/emxCommonDocumentUnlock.jsp?&amp;objectId="+ XSSUtil.encodeForJavaScript(context, objectId);
                      fileActionsStrBuff.append("<a href=\"javascript:submitWithCSRF('"+unlockURL+"',findFrame(getTopWindow(),'listHidden'))\">");
                      fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionUnlock.gif\" alt=\""+sTipUnlock+"\" title=\""+sTipUnlock+"\"></img></a>&#160;");
                    } else {
                      fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionUnlock.gif\" alt=\""+sTipUnlock+"\" title=\""+sTipUnlock+"\"></img></a>&#160;");
                    }
                }

                if ( "true".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_HAS_LOCK_ACCESS)) && "false".equalsIgnoreCase(fileLocked))
                {
                    if ( !isprinterFriendly )
                    {
                        unlockURL = "../components/emxCommonDocumentLock.jsp?&amp;objectId="+ XSSUtil.encodeForJavaScript(context, objectId);
                        fileActionsStrBuff.append("<a href=\"javascript:submitWithCSRF('"+unlockURL+"', findFrame(getTopWindow(),'listHidden'))\">");
                        fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionLock.gif' alt=\""+sTipLock+"\" title=\""+sTipLock+"\"></img></a>&#160;");
                    } else {
                        fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionLock.gif' alt=\""+sTipLock+"\" title=\""+sTipLock+"\"></img></a>&#160;");
                    }
                }

                fileActions = fileActionsStrBuff.toString();

                fileActionsVector.add(fileActions);
            }
            return fileActionsVector;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * This method is used to get the list of files in
     * master (i.e. document holder) object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getFiles(Context context, String[] args) throws Exception
    {
        try
        {
            HashMap programMap         = (HashMap) JPO.unpackArgs(args);
            String  masterObjectId     = (String) programMap.get("objectId");
            DomainObject masterObject  = DomainObject.newInstance(context, masterObjectId);

            //Added to make a single database call to
            StringList masterObjectSelectList = new StringList(12);
            masterObjectSelectList.add(CommonDocument.SELECT_ID);
            masterObjectSelectList.add(CommonDocument.SELECT_TYPE);
            masterObjectSelectList.add(CommonDocument.SELECT_NAME);
            masterObjectSelectList.add(CommonDocument.SELECT_REVISION);
            masterObjectSelectList.add(CommonDocument.SELECT_FILE_NAME);
            masterObjectSelectList.add(CommonDocument.SELECT_FILE_FORMAT);
            masterObjectSelectList.add(CommonDocument.SELECT_FILE_MODIFIED);
            masterObjectSelectList.add(CommonDocument.SELECT_FILE_SIZE);
            masterObjectSelectList.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            masterObjectSelectList.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
            masterObjectSelectList.add(CommonDocument.SELECT_HAS_LOCK_ACCESS);
            masterObjectSelectList.add(CommonDocument.SELECT_HAS_UNLOCK_ACCESS);
            masterObjectSelectList.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
            masterObjectSelectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            masterObjectSelectList.add(CommonDocument.SELECT_LATEST_REVISION);
			masterObjectSelectList.add(CommonDocument.SELECT_HAS_UNLOCK_ACCESS);

            // get the Master Object data
            Map masterObjectMap = masterObject.getInfo(context,masterObjectSelectList);
            // Version Object seletcs
            StringList versionSelectList = new StringList(9);
            versionSelectList.add(CommonDocument.SELECT_ID);
            versionSelectList.add(CommonDocument.SELECT_REVISION);
            versionSelectList.add(CommonDocument.SELECT_DESCRIPTION);
            versionSelectList.add(CommonDocument.SELECT_LOCKED);
            versionSelectList.add(CommonDocument.SELECT_LOCKER);
            versionSelectList.add(CommonDocument.SELECT_TITLE);
            versionSelectList.add(CommonDocument.SELECT_FILE_NAME);
            versionSelectList.add(CommonDocument.SELECT_FILE_FORMAT);
            versionSelectList.add(CommonDocument.SELECT_FILE_MODIFIED);
            versionSelectList.add(CommonDocument.SELECT_FILE_SIZE);
            versionSelectList.add(CommonDocument.SELECT_OWNER);
      versionSelectList.add(DomainConstants.SELECT_ORIGINATED);
      versionSelectList.add(DomainConstants.SELECT_TYPE);
      versionSelectList.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
      versionSelectList.add(CommonDocument.SELECT_IS_VERSION_OBJECT);


            // get the file (Version Object) data
            MapList versionList = masterObject.getRelatedObjects(context,
                                                                  CommonDocument.RELATIONSHIP_ACTIVE_VERSION,
                                                                  CommonDocument.TYPE_DOCUMENTS,
                                                                  versionSelectList,
                                                                  null,
                                                                  false,
                                                                  true,
                                                                  (short)1,
                                                                  null,
                                                                  null,
                                                                  null,
                                                                  null,
                                                                  null);

            // get all the files in the Master Object
            StringList fileList = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_NAME);
            StringList fileFormatList = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_FORMAT);
            StringList fileSizeList   = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_SIZE);
            StringList fileModifiedList   = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_MODIFIED);

            StringList tempfileFormatList = new StringList();
            StringList tempfileList  = new StringList();
            for(int ii =0; ii< fileFormatList.size(); ii++){
            	String format = (String)fileFormatList.get(ii);
            	if(!DomainObject.FORMAT_MX_MEDIUM_IMAGE.equalsIgnoreCase(format)){
            		tempfileFormatList.add(format);
            		tempfileList.add(fileList.get(ii));
            	}
            }
            fileFormatList =tempfileFormatList;
            fileList =tempfileList;

            // get the Master Object meta data
            String masterId    = (String) masterObjectMap.get(CommonDocument.SELECT_ID);
            String canCheckout = (String) masterObjectMap.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            String canCheckin  = (String) masterObjectMap.get(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
            String canLock     = (String) masterObjectMap.get(CommonDocument.SELECT_HAS_LOCK_ACCESS);
            String canUnLock     = (String) masterObjectMap.get(CommonDocument.SELECT_HAS_UNLOCK_ACCESS);
            boolean isLatestRevision=((String)masterObjectMap.get(CommonDocument.SELECT_REVISION)).equalsIgnoreCase((String)masterObjectMap.get(CommonDocument.SELECT_LATEST_REVISION));
            String suspendVersioning     = (String) masterObjectMap.get(CommonDocument.SELECT_SUSPEND_VERSIONING);
            boolean moveFilesToVersion = (Boolean.valueOf((String) masterObjectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();

            // to store the object ID of the object where file resides
            // this can be either master object Id or version object Id depending on the "Moves Files To Version" attribute value
            String fileId    = (String) masterObjectMap.get(CommonDocument.SELECT_ID);

            // loop thru each file to build MapList, each Map corresponds to one file
            MapList fileMapList = new MapList();
            String fileFormat = null;
            String fileSize   = null;
            String fileModified   = null;
            Iterator versionItr  = versionList.iterator();
            while(versionItr.hasNext())
            {
                Map fileVersionMap     = (Map)versionItr.next();
                String versionFileName = (String)fileVersionMap.get(CommonDocument.SELECT_TITLE);
				//Added information for MSF
				String locker = (String)fileVersionMap.get(CommonDocument.SELECT_LOCKER);
        String versionFileRevision = (String)fileVersionMap.get(CommonDocument.SELECT_REVISION);
                fileFormat = CommonDocument.FORMAT_GENERIC;
                fileSize   = "";
                fileModified = "";
                if( moveFilesToVersion )
                {
                    fileId = (String)fileVersionMap.get(CommonDocument.SELECT_ID);
                    try
                    {
                        String versionFiles = (String)fileVersionMap.get(CommonDocument.SELECT_FILE_NAME);
                        fileFormat = (String) fileVersionMap.get(CommonDocument.SELECT_FILE_FORMAT);
                        fileSize = (String) fileVersionMap.get(CommonDocument.SELECT_FILE_SIZE);
                        fileModified = (String) fileVersionMap.get(CommonDocument.SELECT_FILE_MODIFIED);
                    } catch (ClassCastException cex) {
                        StringList versionFilesList = (StringList)fileVersionMap.get(CommonDocument.SELECT_FILE_NAME);
                        StringList versionFileSize = (StringList)fileVersionMap.get(CommonDocument.SELECT_FILE_SIZE);
                        StringList versionFileFormat = (StringList)fileVersionMap.get(CommonDocument.SELECT_FILE_FORMAT);
                        StringList versionFileModified = (StringList)fileVersionMap.get(CommonDocument.SELECT_FILE_MODIFIED);

                        // get the file corresponding to this Version by filtering the above fileList
                        int index = versionFilesList.indexOf(versionFileName.trim());

                        // get the File Format
                        if (index != -1 && versionFileFormat != null && versionFileFormat.size() >= index )
                        {
                            fileFormat = (String)versionFileFormat.get(index);
                        }

                        // get the File Size
                        if (index != -1 && versionFileSize != null && versionFileSize.size() >= index )
                        {
                            fileSize = (String)versionFileSize.get(index);
                        }

                        // get the File Modified date
                        if (index != -1 && versionFileModified != null && versionFileModified.size() >= index )
                        {
                            fileModified = (String)versionFileModified.get(index);
                        }
                    }
                } else {
                    // get the file corresponding to this Version by filtering the above fileList
                    int index = fileList.indexOf(versionFileName);

                    // get the File Format
                    if (index != -1 && fileFormatList != null && fileFormatList.size() >= index )
                    {
                        fileFormat = (String)fileFormatList.get(index);
                    }

                    // get the File Size
                    if (index != -1 && fileSizeList != null && fileSizeList.size() >= index )
                    {
                        fileSize = (String)fileSizeList.get(index);
                    }

                    // get the File Modified date
                    if (index != -1 && fileModifiedList != null && fileModifiedList.size() >= index )
                    {
                        fileModified = (String)fileModifiedList.get(index);
                    }
                }
                fileVersionMap.put("masterId", masterId);
                fileVersionMap.put("fileId", fileId);
                fileVersionMap.put(CommonDocument.SELECT_FILE_FORMAT, fileFormat);
                fileVersionMap.put(CommonDocument.SELECT_FILE_MODIFIED, fileModified);
        fileVersionMap.put(CommonDocument.SELECT_FILE_NAME, versionFileName);
        fileVersionMap.put(CommonDocument.SELECT_REVISION, versionFileRevision);
                fileVersionMap.put(CommonDocument.SELECT_FILE_SIZE, fileSize);
                fileVersionMap.put(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS, canCheckout);
                fileVersionMap.put(CommonDocument.SELECT_HAS_CHECKIN_ACCESS, canCheckin);
                fileVersionMap.put(CommonDocument.SELECT_HAS_LOCK_ACCESS, canLock);
                fileVersionMap.put(CommonDocument.SELECT_HAS_UNLOCK_ACCESS, canUnLock);
                fileVersionMap.put(CommonDocument.SELECT_SUSPEND_VERSIONING, suspendVersioning);
                fileVersionMap.put("isLatestRevision", isLatestRevision);
				//Added information for MSF
				fileVersionMap.put(CommonDocument.SELECT_LOCKER, locker);
                fileMapList.add(fileVersionMap);
            }
            return fileMapList;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * This method is used to show the Lock image.
     *                This method is called from the Column Lock Image.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */
    public Vector getLockIcon(Context context, String[] args) throws Exception
    {
        Vector showLock= new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            if(objectList.isEmpty())
                return showLock;

            StringList lockinfo = new StringList(2);
            lockinfo.add(CommonDocument.SELECT_LOCKED);
            lockinfo.add(CommonDocument.SELECT_LOCKER);
            objectList = getUpdatedColumnValues(context, objectList, lockinfo);

            for(int i=0; i< objectList.size(); i++)
            {
                Map objectMap = (Map) objectList.get(i);
                String statusImageString = "";
                String fileLocked = (String) objectMap.get(CommonDocument.SELECT_LOCKED);
                String fileLocker = PersonUtil.getFullName(context,(String) objectMap.get(CommonDocument.SELECT_LOCKER));

                if ("TRUE".equalsIgnoreCase(fileLocked)){
                    statusImageString = "<img border=\"0\" src=\"../common/images/iconStatusLocked.gif\" alt=\"" + XSSUtil.encodeForHTMLAttribute(context, fileLocker) + "\" title=\"" + XSSUtil.encodeForHTMLAttribute(context, fileLocker) + "\"/>";
                }
                showLock.add(statusImageString);
            }
            return  showLock;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }


    /**
      *  method to get file format info from master object
      *  Used in File Summary, Version Summary Page
      *
      *  @param context the eMatrix <code>Context</code> object
      *  @param args an array of String arguments for this method
      *  @returns Vector of file formats
      *  @throws Exception if the operation fails
      *
      *  @since Common 10.5
      */
    public Vector getFileFormat(Context context, String[] args) throws Exception
    {
        Vector fileFormatVector = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Iterator objectListItr = objectList.iterator();
            String fileFormat = null;

            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                fileFormat = (String) objectMap.get(CommonDocument.SELECT_FILE_FORMAT);
                fileFormat = i18nNow.getFormatI18NString(fileFormat, context.getSession().getLanguage());
                fileFormatVector.add(fileFormat);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            return fileFormatVector;
        }
    }


/**
      *  Method to get the file names.
      *  Used in File Summary, Version Summary Page.
      *
      *  @param context the eMatrix <code>Context</code> object
    *  @param args holds a packed HashMap of the following entries:
    *   objectList- a MapList of objects information.
    *     paramList- a HashMap of parameter values including reportFormat, relId, and trackUsagePartId
      *  @returns Vector of File Names
      *  @throws Exception if the operation fails
      *
      *  @since Common 11.0
      */
  public Vector getFileName(Context context, String[] args)throws Exception
    {
        Vector fileRevisionVector = new Vector();

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
      Map paramList = (Map)programMap.get("paramList");
      boolean isPopup = "true".equalsIgnoreCase((String)paramList.get("popup"));
      String reportFormat = (String) paramList.get("reportFormat");
    boolean bExport = "ExcelHTML".equals(reportFormat)  || "CSV".equals(reportFormat) ;
    boolean bPrintMode = "HTML".equals(reportFormat);

    String masterId = "";
    
      String strDocumentPartRel = (String)paramList.get("relId");
      String strPartId = (String)paramList.get("trackUsagePartId");
      try
      {
        if(strPartId == null && strDocumentPartRel != null)
        {
          String[] relIds = {strDocumentPartRel};
          StringList slRelSelect = new StringList("from.id");
          MapList mlPart = DomainRelationship.getInfo(context, relIds, slRelSelect);

          if(mlPart.size()>0)
          {
            strPartId = (String) ((Map)mlPart.get(0)).get("from.id");
          }
        }
      }catch(Exception e)
      {
      }

      String strLink = "<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?mode=insert&amp;emxSuiteDirectory=components&amp;relId=null&amp;jsTreeID="+(String)paramList.get("jsTreeID");

    //Begin:Addition:Form Single Page Properties and Files
    //
    // The parameter 'treePopup' will indicate, when the file summary table is invoked from the document properties page, and the hyperlinks to the file name
    // should open the file tree in new window.
    //

    boolean treePopup = "true".equalsIgnoreCase((String)paramList.get("treePopup"));
    if (treePopup) {
        strLink = "<a href=\"JavaScript:showModalDialog('../common/emxTree.jsp?emxSuiteDirectory=components&amp;relId=null&amp;jsTreeID="+(String)paramList.get("jsTreeID");
    }

    //End:Addition:Form Single Page Properties and Files

      String[] strArrayIds = new String[objectList.size()];

            for(int i=0; i<objectList.size(); i++)
            {
                Map objectMap = (Map) objectList.get(i);
                strArrayIds[i] = (String) objectMap.get(DomainConstants.SELECT_ID);
                masterId = (String) objectMap.get("masterId");
            }
    StringList sl = new StringList(3);
      sl.add("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
      sl.add(DomainConstants.SELECT_TYPE);
    sl.add(DomainConstants.SELECT_ID);

      MapList ml = DomainObject.getInfo(context, strArrayIds, sl);
    for(int i=0; i<ml.size(); i++)
      {

      Map objectMap = (Map) ml.get(i);
      String strTypeSymName = FrameworkUtil.getAliasForAdmin(context, "type", (String)objectMap.get(DomainConstants.SELECT_TYPE), true);
      String typeIcon = "";
      try{
          typeIcon = EnoviaResourceBundle.getProperty(context,"emxFramework.smallIcon." + strTypeSymName);
      }catch(Exception e){
          typeIcon  = EnoviaResourceBundle.getProperty(context,"emxFramework.smallIcon.defaultType");
      }
      String defaultTypeIcon = "<img src=\"../common/images/"+typeIcon+"\" border=\"0\"></img>";
      //ADDED BUG: 347008
      String strFileName = (String)objectMap.get("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]");
      String str = strFileName.trim();
      //END BUG: 347008
      if(bExport)
        {
          fileRevisionVector.add(XSSUtil.encodeForHTML(context, (String)objectMap.get("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]")));
      }
      else if(isPopup || bPrintMode)
      {
        fileRevisionVector.add("<nobr>"+defaultTypeIcon+"&#160;"+XSSUtil.encodeForHTML(context,(String)objectMap.get("attribute["+DomainConstants.ATTRIBUTE_TITLE+"]"))+"</nobr>");
      }
      else
      {//changed for 366568 : parameters for width n height are given 700 X 600
        //String sLink = strLink + "&parentOID="+objectMap.get("masterId")+"&objectId="+objectMap.get(DomainConstants.SELECT_ID)+"&AppendParameters=true&trackUsagePartId="+strPartId+"', '700', '600', 'false', 'popup', '')\">";

          String sLink = strLink + "&amp;parentOID="+XSSUtil.encodeForJavaScript(context,masterId)+"&amp;objectId="+objectMap.get(DomainConstants.SELECT_ID)+"&amp;AppendParameters=true&amp;trackUsagePartId="+strPartId+"', '700', '600', 'false', 'content', '');\">";

        String strURL = sLink + defaultTypeIcon + "</a> ";
        strURL += sLink + XSSUtil.encodeForXML(context,strFileName) + "</a>&#160;";
        fileRevisionVector.add("<nobr>"+strURL+"</nobr>");
      }
    }
            return fileRevisionVector;
        }

    /**
      *  Method to get the file revisions.
      *  Used in File Summary, Version Summary Page.
      *
      *  @param context the eMatrix <code>Context</code> object
    *  @param args holds a packed HashMap of the following entries:
    *   objectList- a MapList of objects information.
    *     paramList- a HashMap of parameter values including reportFormat, relId, and trackUsagePartId
      *  @returns Vector of File Revisions
      *  @throws Exception if the operation fails
      *
      *  @since Common 11.0
      */
  public Vector getFileRevision(Context context, String[] args)throws Exception
    {
        Vector fileRevisionVector = new Vector();

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
      Map paramList = (Map)programMap.get("paramList");
      String strDocumentPartRel = (String)paramList.get("relId");
      String strPartId = (String)paramList.get("trackUsagePartId");
      String reportFormat = (String) paramList.get("reportFormat");
      boolean bPrintMode = "ExcelHTML".equals(reportFormat)  || "CSV".equals(reportFormat) ;
      boolean bExport = "HTML".equals(reportFormat);

      try
      {

        if(strPartId == null && strDocumentPartRel != null)
        {
          String[] relIds = {strDocumentPartRel};
          StringList slRelSelect = new StringList("from.id");
          MapList mlPart = DomainRelationship.getInfo(context, relIds, slRelSelect);

          if(mlPart.size()>0)
          {
            strPartId = (String) ((Map)mlPart.get(0)).get("from.id");
          }
        }
      }catch(Exception e)
    {//do nothing...
      }

      String strLink = "<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTable.jsp?program=emxCommonFileUI%253AgetVersions&amp;table=APPFileVersions&amp;sortColumnName=Version&amp;sortDirection=descending&amp;header=emxComponents.Common.VersionsPageHeading&amp;FilterFramePage=..%252Fcomponents%252FemxCommonDocumentCheckoutUtil.jsp&amp;FilterFrameSize=1&amp;HelpMarker=emxhelpdocumentfileversions&amp;CancelButton=true&amp;CancelLabel=emxComponents.Common.Close&amp;emxSuiteDirectory=components&amp;relId=null&amp;jsTreeID="+paramList.get("jsTreeID")+"&amp;suiteKey=Components&amp;trackUsagePartId="+strPartId;
      String[] strArrayIds = new String[objectList.size()];
            for(int i=0; i<objectList.size(); i++)
            {
                Map objectMap = (Map) objectList.get(i);
                strArrayIds[i] = (String) objectMap.get(DomainConstants.SELECT_ID);
            }
    StringList sl = new StringList(2);
      sl.add(DomainConstants.SELECT_REVISION);
    sl.add(DomainConstants.SELECT_ID);
      MapList ml = DomainObject.getInfo(context, strArrayIds, sl);

      for(int i=0; i<objectList.size(); i++)
      {
                Map objectMap = (Map) objectList.get(i);
        if(bExport || bPrintMode)
        {
          fileRevisionVector.add((String)objectMap.get(DomainConstants.SELECT_REVISION));
        }else
        {
          String sLink = strLink + "&amp;parentOID="+objectMap.get("masterId")+"&amp;objectId="+objectMap.get("id")+"', '700', '600', 'false', 'popup', '')\">"+(String)objectMap.get(DomainConstants.SELECT_REVISION)+"</a>";
          fileRevisionVector.add(sLink);
        }
      }
            return fileRevisionVector;
    }

    /**
    *  method to get file size info from master object
    *  Used in File Summary, Version Summary Page
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @returns Vector of file size
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    */
    public Vector getFileSize(Context context, String[] args) throws Exception
    {
        Vector fileSizeVector = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Iterator objectListItr = objectList.iterator();
            String fileSize = null;

            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                fileSize = (String) objectMap.get(CommonDocument.SELECT_FILE_SIZE);
                fileSizeVector.add(formatFileSize(fileSize));
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            return fileSizeVector;
        }
    }

    /**
     * Method to format file size close to two decimal place
     * 512 Bytes --> 512 B
     * 748 Bytes --> 0.73 KB
     * @param fileSize
     * @return formated file size string
     */
    private String formatFileSize(String fileSize) {
    	String retStr = "";
    	try{
    		double dblFileSize = Double.parseDouble(fileSize);
    		DecimalFormat decFormat = new DecimalFormat("0.00");
    		if(dblFileSize <= 512){
    			retStr = fileSize + " B";
    		} else if(dblFileSize > 512 && dblFileSize <= (1024*512)){
    			retStr = decFormat.format(dblFileSize / 1024) + " KB";
    		}else if(dblFileSize > (1024*512) && dblFileSize <= (1024*1024*512)){
    			retStr = decFormat.format(dblFileSize / (1024*1024)) + " MB";
    		}else if(dblFileSize > (1024*1024*512)){
    			retStr = decFormat.format(dblFileSize / (1024*1024*1024)) + " GB";
    		}else{
    			retStr = fileSize;
    		}
    	}catch(NumberFormatException ex){
    		retStr = fileSize;
    	}
		return retStr;
	}

	/**
    *  Get Vector of Strings for Action Icons
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of html code to
    *        construct the Actions Column.
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    *  @grade 0
    */
    public static Vector getFileActions(Context context, String[] args)
        throws Exception
    {
        Vector fileActionsVector = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            if(objectList.isEmpty())
                return fileActionsVector;
            Map paramList      = (Map)programMap.get("paramList");

      String strDocumentPartRel = (String)paramList.get("relId");
      String strPartId = (String)paramList.get("trackUsagePartId");
      try
      {

        if(strPartId == null && strDocumentPartRel != null)
        {
          String[] relIds = {strDocumentPartRel};
          StringList slRelSelect = new StringList("from.id");
          MapList mlPart = DomainRelationship.getInfo(context, relIds, slRelSelect);

          if(mlPart.size()>0)
          {
            strPartId = (String) ((Map)mlPart.get(0)).get("from.id");
          }
        }
      }catch(Exception e)
      {//do nothing...
      }
            boolean isprinterFriendly = false;
            if(paramList.get("reportFormat") != null)
            {
                isprinterFriendly = true;
            }

            StringList lockinfo = new StringList(2);
            lockinfo.add(CommonDocument.SELECT_LOCKED);
            lockinfo.add(CommonDocument.SELECT_LOCKER);
            lockinfo.add(CommonDocument.SELECT_ID);
            MapList lockInfo = getUpdatedColumnValues(context, objectList, lockinfo);
            Map fileLockInfoGroypById = new HashMap();
            for (Iterator iter = lockInfo.iterator(); iter.hasNext();) {
                Map element = (Map) iter.next();
                String[] lock = new String[2];
                lock[0] = (String) element.get(CommonDocument.SELECT_LOCKED);
                lock[1] = (String) element.get(CommonDocument.SELECT_LOCKER);
                fileLockInfoGroypById.put(element.get(CommonDocument.SELECT_ID), lock);
            }


            Iterator objectListItr = objectList.iterator();
            String languageStr = (String)paramList.get("languageStr");
            Locale strLocale = context.getLocale();

            String masterId        = null;
            String versionId        = null;
            String revision        = null;
            String fileActions     = null;
            String fileName        = null;
            String encodedFileName = null;
            String encodedFormat   = null;
            String fileFormat      = null;

            String strViewerURL = null;
            String downloadURL  = null;
            String checkoutURL  = null;
            String checkinURL   = null;
            String unlockURL = null;
            String s3DViaURL    = null;
            String sFileExt     = null;

            String sTipDownload = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipDownload");
            String sTipCheckout = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipCheckout");
            String sTipCheckin  = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipCheckin");
            String sTipUnlock  = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipUnlock");
            String sTipLock  = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipLock");
            String sTip3DVIAViewer = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTip3DLiveExamine");

            String suspendVersioning     = "False";
            String viewerTip = "Default";
            String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip, context.getSession().getLanguage());
			// Start MSF
				String msfRequestData = "";
			// End MSF
            
            
            
            while(objectListItr.hasNext())
            {
				// Start MSF
				msfRequestData = "";
				// End MSF
                fileName    = "";
                fileActions = "";            
                
                StringBuffer fileActionsStrBuff = new StringBuffer();

                Map objectMap = (Map) objectListItr.next();
                suspendVersioning = (String) objectMap.get(CommonDocument.SELECT_SUSPEND_VERSIONING);
                masterId = (String) objectMap.get("fileId");

                versionId = (String) objectMap.get("id");
                revision = (String) objectMap.get("revision");//seeta

                String[] fileLockInfo = (String[]) fileLockInfoGroypById.get(versionId);
                String fileLocked = fileLockInfo != null ? fileLockInfo[0] : (String)objectMap.get(CommonDocument.SELECT_LOCKED);
                String fileLockedBy = fileLockInfo != null ? fileLockInfo[1] : (String)objectMap.get(CommonDocument.SELECT_LOCKER);

                fileName = (String) objectMap.get(CommonDocument.SELECT_TITLE);
                encodedFileName = FrameworkUtil.findAndReplace(fileName,"+","%252b"); // Added to support + character in file names
				encodedFileName = FrameworkUtil.findAndReplace(encodedFileName, "&", "%26");
                fileFormat = (String) objectMap.get(CommonDocument.SELECT_FILE_FORMAT);
                if ("".equals(fileFormat))
                {
                    fileFormat = CommonDocument.FORMAT_GENERIC;
                }
              
                encodedFormat = fileFormat;

                int fileCount = 0;
                String vcInterface = null;
                boolean vcDocument = false;
                boolean sOwnerWorkspace = false;
               fileCount = CommonDocument.getFileCount(context,objectMap);
               vcInterface = (String)objectMap.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
               vcDocument = "TRUE".equalsIgnoreCase(vcInterface)?true:false;
               objectMap.put(CommonDocument.SELECT_LOCKED,fileLocked);
               objectMap.put(CommonDocument.SELECT_LOCKER,fileLockedBy);
               boolean isLatestRevision=(Boolean)objectMap.get("isLatestRevision");
                if ( CommonDocument.canView(context, objectMap) )
                {

                    if (is3DViaSupported(context, encodedFileName))
                    {
                        if ( !isprinterFriendly )
                        {
                            s3DViaURL       = "../components/emxLaunch3DLiveExamine.jsp?objectId="+ masterId +"&amp;mode=fileBased&amp;fileName="+encodedFileName+"&amp;fileFormat="+encodedFormat;
                            fileActionsStrBuff.append("<a href='javascript:showModalDialog(\"" + s3DViaURL + "\",600,600)'>");
                            fileActionsStrBuff.append("<img border='0' src='../common/images/iconSmallShowHide3D.gif' alt=\""+sTipDownload+"\" title=\""+sTip3DVIAViewer+"\"></img></a>&#160;");
                        }
                        else
                        {
                            fileActionsStrBuff.append("<img border='0' src='../common/images/iconSmallShowHide3D.gif' alt=\""+sTipDownload+"\" title=\""+sTip3DVIAViewer+"\"></img></a>&#160;");
                        }
                    }
                }
                if(CommonDocument.canDownload(context, objectMap) ){
                    if ( !isprinterFriendly )
                    {
                      downloadURL = "javascript:getTopWindow().callCheckout('"+ XSSUtil.encodeForJavaScript(context, masterId) +"','download', '"+ XSSUtil.encodeForJavaScript(context, fileName)+ "', '" + XSSUtil.encodeForJavaScript(context, fileFormat) +"', null, null, null, null, '"+XSSUtil.encodeForJavaScript(context, strPartId)+"', '" + revision +"',null,null, null,null, null,null ,null,null, null, '"+ versionId + "');";
                      String viewerURL = getViewerURL(context, masterId, fileFormat, fileName, strPartId);
                      fileActionsStrBuff.append(viewerURL);
                      fileActionsStrBuff.append("<a href=\"" + downloadURL+"\">");
                      fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"></img></a>&#160;");
                    } else {
                      fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionView.gif' alt=\""+i18nViewerTip+"\" title=\""+i18nViewerTip+"\"></img></a>&#160;");
                      fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"></img></a>&#160;");
                    }
                }
				boolean canCheckin =false;
                    if ( CommonDocument.canCheckout(context, objectMap) && isLatestRevision)
                    {
                      if ( !isprinterFriendly )
                      {
                    	checkoutURL = "javascript:getTopWindow().callCheckout('"+ XSSUtil.encodeForJavaScript(context, masterId) +"','checkout', '"+ XSSUtil.encodeForJavaScript(context, fileName)+ "', '" + XSSUtil.encodeForJavaScript(context, fileFormat) +"', null, null, null, null, '"+XSSUtil.encodeForJavaScript(context, strPartId)+"', '"+ revision +"',null,null, null,null, null,null ,null,null, null, '"+ versionId + "');";                        
                        fileActionsStrBuff.append("<a href=\"" + checkoutURL+"\">");
                        fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionCheckOut.gif\" alt=\""+sTipCheckout+"\" title=\""+sTipCheckout+"\"></img></a>&#160;");

                        unlockURL = "../components/emxCommonDocumentLock.jsp?objectId="+ XSSUtil.encodeForJavaScript(context, versionId);
                        fileActionsStrBuff.append("<a href=\"javascript:submitWithCSRF('"+unlockURL+"', findFrame(getTopWindow(),'listHidden'))\">");
                        fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionLock.gif\" alt=\""+sTipLock+"\" title=\""+sTipLock+"\"></img></a>&#160;");
                      } else {
                        fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionCheckOut.gif\" alt=\""+sTipCheckout+"\" title=\""+sTipCheckout+"\"></img></a>&#160;");

                        fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionLock.gif\" alt=\""+sTipLock+"\" title=\""+sTipLock+"\"></img></a>&#160;");
                      }
                    }
                    else if ( CommonDocument.canCheckin(context, objectMap))
                    {
						canCheckin = true;
                      if ( !isprinterFriendly )
                      {
                   
					  checkinURL = "../components/emxCommonDocumentPreCheckin.jsp?showComments=required&amp;refreshTable=true&amp;deleteFromTree="+XSSUtil.encodeForJavaScript(context, versionId)+"&amp;objectId="+ XSSUtil.encodeForURL(context, masterId) +"&amp;showFormat=readonly&amp;append=true&amp;objectAction="+CommonDocument.OBJECT_ACTION_UPDATE_MASTER +"&amp;format="+ encodedFormat+"&amp;oldFileName="+XSSUtil.encodeForJavaScript(context, encodedFileName);
					  // Start MSF
					  msfRequestData = "{RequestType: 'CheckIn', DocumentID: '" + XSSUtil.encodeForJavaScript(context, masterId) + "', PartId: '" +XSSUtil.encodeForJavaScript(context, strPartId)+ "', MSFFileFormatDetails:[{FileName: '" + XSSUtil.encodeForJavaScript(context, fileName) + "', Format: '" + XSSUtil.encodeForJavaScript(context, fileFormat)+ "', VersionId: '" + XSSUtil.encodeForJavaScript(context, versionId) + "'}]}";
					  fileActionsStrBuff.append("<a href=\"javascript:processModalDialog(" + msfRequestData + ", '" + checkinURL + "',730,450)\">");
					  // End MSF
					  fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionCheckIn.gif\" alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>");
                      } else {
                        fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionCheckIn.gif\" alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>&#160;");
                     }
                }
                if("true".equalsIgnoreCase(fileLocked) && fileCount<=1 && ("true".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_HAS_UNLOCK_ACCESS)) || canCheckin)){
                       
                if ( !isprinterFriendly )
                    {                
                      unlockURL = "../components/emxCommonDocumentUnlock.jsp?objectId="+ XSSUtil.encodeForJavaScript(context, versionId);
                      fileActionsStrBuff.append("<a href=\"javascript:submitWithCSRF('"+unlockURL+"',findFrame(getTopWindow(),'listHidden'))\">");
                      fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionUnlock.gif\" alt=\""+sTipUnlock+"\" title=\""+sTipUnlock+"\"></img></a>");
                    } else {
                      fileActionsStrBuff.append("<img border=\"0\" src=\"../common/images/iconActionUnlock.gif\" alt=\""+sTipUnlock+"\" title=\""+sTipUnlock+"\"></img></a>&#160;");
                    }
                }

                fileActions = fileActionsStrBuff.toString();

                fileActionsVector.add(fileActions);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            return fileActionsVector;
        }
    }

    /**
    *  returns Maplist containing Versions Info for the file
    *  Used for Version Summary Page
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return MapList containing Versions Info
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public static MapList getVersions(Context context, String[] args)
        throws Exception
    {
        HashMap map = (HashMap) JPO.unpackArgs(args);

        String objectId = (String) map.get("objectId");
        String masterId = (String) map.get("parentOID");
        MapList revisionInfoList = new MapList();

        DomainObject versionObject = DomainObject.newInstance(context, objectId);
        String lastId = versionObject.getInfo(context, "last.id");
        versionObject.setId(lastId);
        //TODO this is a temp fix.
        //we are getting objectId and parentOID same from AEF
        //so getting the masterId from minor obj 
        masterId = UIUtil.isNullOrEmpty(masterId) || objectId.equals(masterId) ?
        		(UIComponent.hasReadAccess (context, objectId) ? versionObject.getInfo(context,CommonDocument.SELECT_MASTER_ID) : "") : masterId;
        if(!UIUtil.isNullOrEmpty(masterId)){
        DomainObject masterObject = DomainObject.newInstance(context, masterId);

        //Added to make a single database call to
        StringList masterObjectSelectList = new StringList(5);
        masterObjectSelectList.add(CommonDocument.SELECT_FILE_NAME);
        masterObjectSelectList.add(CommonDocument.SELECT_FILE_FORMAT);
        masterObjectSelectList.add(CommonDocument.SELECT_FILE_SIZE);
        masterObjectSelectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
        masterObjectSelectList.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);

        // get the Master Object data
        Map masterMap = masterObject.getInfo(context,masterObjectSelectList);

        String canCheckout = (String) masterMap.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
        String moveFile    = (String) masterMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);

        StringList masterFileNameList = (StringList)masterMap.get(CommonDocument.SELECT_FILE_NAME);
        StringList masterFormatList   = (StringList)masterMap.get(CommonDocument.SELECT_FILE_FORMAT);
        StringList masterFileSizeList = (StringList)masterMap.get(CommonDocument.SELECT_FILE_SIZE);

        String versionFileName = versionObject.getInfo(context,CommonDocument.SELECT_TITLE);

        String fileFormat = null;
        String fileSize   = null;

        // get the file corresponding to this Version by filtering the file name List
        int index = masterFileNameList.indexOf(versionFileName);

        // get the File Format
        if (index != -1 && masterFormatList != null && masterFormatList.size() >= index )
        {
          fileFormat = (String)masterFormatList.get(index);
        }

        // get the File Size
        if (index != -1 && masterFileSizeList != null && masterFileSizeList.size() >= index )
        {
          fileSize = (String)masterFileSizeList.get(index);
        }

        StringList busSelects = new StringList(7);
        busSelects.add(CommonDocument.SELECT_ID);
        busSelects.add(CommonDocument.SELECT_MASTER_ID);
        busSelects.add(CommonDocument.SELECT_FILE_NAME);
        busSelects.add(CommonDocument.SELECT_FILE_FORMAT);
        busSelects.add(CommonDocument.SELECT_FILE_SIZE);
        busSelects.add(CommonDocument.SELECT_TITLE);
        busSelects.add(CommonDocument.SELECT_OWNER);
        
        StringList versionMultiSelectList = new StringList(6);
        versionMultiSelectList.add(CommonDocument.SELECT_FILE_NAME);
        versionMultiSelectList.add(CommonDocument.SELECT_FILE_FORMAT);
        versionMultiSelectList.add(CommonDocument.SELECT_FILE_SIZE);
        // for the Id passed, get revisions Info
        //
        MapList revisionsList = versionObject.getRevisionsInfo(context,busSelects,
                                                          versionMultiSelectList);


        Iterator revisionsListItr = revisionsList.iterator();
        while(revisionsListItr.hasNext())
        {
            Map revisionMap = (Map) revisionsListItr.next();

            revisionMap.put(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS, canCheckout);
            revisionMap.put(CommonDocument.SELECT_MOVE_FILES_TO_VERSION, moveFile);
            String verTitle = (String) revisionMap.get(CommonDocument.SELECT_TITLE);

            String verFileFormat =  "";
            String verFileSize   =  "";
            try
            {
                verFileFormat =  (String)revisionMap.get(CommonDocument.SELECT_FILE_FORMAT);
                verFileSize   =  (String)revisionMap.get(CommonDocument.SELECT_FILE_SIZE);
            } catch(ClassCastException cex) {
                StringList verFileList = (StringList)revisionMap.get(CommonDocument.SELECT_FILE_NAME);
                StringList verFileFormatList = (StringList)revisionMap.get(CommonDocument.SELECT_FILE_FORMAT);
                StringList verFileSizeList  =  (StringList )revisionMap.get(CommonDocument.SELECT_FILE_SIZE);

                // get the file corresponding to this Version by filtering the above fileList
                index = verFileList.indexOf(verTitle);

                // get the File Format
                if (index != -1 && verFileFormatList != null && verFileFormatList.size() >= index )
                {
                    verFileFormat = (String)verFileFormatList.get(index);
                }

                // get the File Size
                if (index != -1 && verFileSizeList != null && verFileSizeList.size() >= index )
                {
                    verFileSize = (String)verFileSizeList.get(index);
                }
            }

            // format, file size will be null for active version object
            // read from master object
            if(verFileFormat == null || "".equals(verFileFormat) || "null".equals(verFileFormat))
            {
                revisionMap.put(CommonDocument.SELECT_FILE_FORMAT, fileFormat);
                revisionMap.put(CommonDocument.SELECT_FILE_SIZE, fileSize);
            } else {
                revisionMap.put(CommonDocument.SELECT_FILE_FORMAT, verFileFormat);
                revisionMap.put(CommonDocument.SELECT_FILE_SIZE, verFileSize);
            }
            revisionInfoList.add(revisionMap);
	        }
        }
        return revisionInfoList;
    }


    /**
    *  method to get file format info from Version objects
    *  Used in File Summary, Version Summary Page
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @returns Vector of file formats
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    */

    public Vector getVersionFileFormat(Context context, String[] args) throws Exception
    {
        Vector fileFormatVector = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Iterator objectListItr = objectList.iterator();
            String fileFormat   = null;

            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                fileFormat = (String) objectMap.get(CommonDocument.SELECT_FILE_FORMAT);
                fileFormat = i18nNow.getFormatI18NString(fileFormat, context.getSession().getLanguage());
                fileFormatVector.add(fileFormat);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            return fileFormatVector;
        }
    }

    /**
    *  method to get file size info from Version objects
    *  Used in File Summary, Version Summary Page
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @returns Vector of file size
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    */
    public Vector getVersionFileSize(Context context, String[] args) throws Exception
    {
        Vector fileSizeVector = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Iterator objectListItr = objectList.iterator();
            String fileSize = null;

            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                fileSize = (String) objectMap.get(CommonDocument.SELECT_FILE_SIZE);
                fileSizeVector.add(formatFileSize(fileSize));
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            return fileSizeVector;
        }
    }

    /**
    *  Get Vector of Strings for Action Icons for file version
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return Vector object that contains a vector of html code to
    *        construct the Actions Column.
    *  @throws Exception if the operation fails
    *
    *  @since Common 10.5
    *  @grade 0
    */

    public static Vector getVersionFileActions(Context context, String[] args)
                            throws Exception
    {
        Vector fileActionsVector = new Vector();
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList      = (Map)programMap.get("paramList");
            boolean isprinterFriendly = false;
            if(paramList.get("reportFormat") != null)
            {
                isprinterFriendly = true;
            }
      String strDocumentPartRel = (String)paramList.get("relId");
      String strPartId = (String)paramList.get("trackUsagePartId");
      try
      {
        if(strPartId == null && strDocumentPartRel != null)
        {
          String[] relIds = {strDocumentPartRel};
          StringList slRelSelect = new StringList("from.id");
          MapList mlPart = DomainRelationship.getInfo(context, relIds, slRelSelect);

          if(mlPart.size()>0)
          {
            strPartId = (String) ((Map)mlPart.get(0)).get("from.id");
          }
        }
      }catch(Exception e)
      {
      }
            Iterator objectListItr = objectList.iterator();
            String languageStr = (String)paramList.get("languageStr");
            Locale strLocale = context.getLocale();

            String masterId        = null;
            String versionId       = null;
            String fileActions     = null;
            String fileName        = null;
            String encodedFileName = null;
            String encodedFormat   = null;
            String fileFormat      = null;
            String docObjId = "";
            boolean canViewAndDownload = false;
            boolean docHasFiles = false;
            

            String viewerURL    = null;
            String downloadURL  = null;
            String s3DViaURL    = null;
            String sFileExt     = null;
            Map objectMap = new HashMap();
            String moveFile = "";

            String sTipDownload = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTipDownload");
            String sTip3DVIAViewer = EnoviaResourceBundle.getProperty(context,  "emxComponentsStringResource", strLocale,"emxComponents.DocumentSummary.ToolTip3DLiveExamine");

            String viewerTip = "Default";
            String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip, context.getSession().getLanguage());
            while(objectListItr.hasNext())
            {
                fileName    = "";
                fileActions = "";
                StringBuffer fileActionsStrBuff = new StringBuffer();

                objectMap = (Map) objectListItr.next();

                moveFile  = (String) objectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
                masterId  = (String) objectMap.get(CommonDocument.SELECT_MASTER_ID);
                versionId = (String) objectMap.get("id");
                if (masterId == null || "".equals(masterId) || "null".equals(masterId) || "true".equalsIgnoreCase(moveFile))
                {
                    masterId = versionId;
                }

                fileName = (String) objectMap.get(CommonDocument.SELECT_TITLE);
                encodedFileName = fileName;
           

                fileFormat = (String) objectMap.get(CommonDocument.SELECT_FILE_FORMAT);
                encodedFormat = fileFormat;

                canViewAndDownload = "true".equalsIgnoreCase((String)objectMap.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS));
                docObjId = (String) objectMap.get(CommonDocument.SELECT_MASTER_ID);
                
                if(UIUtil.isNullOrEmpty(docObjId)){
                	docObjId = (String) objectMap.get("id");
                }
                DomainObject docObject = new DomainObject(docObjId);
                docObject.open(context);             
                MapList fileList = docObject.getAllFiles(context);
                if(fileList.size()>0)docHasFiles=true;
          
                if ( canViewAndDownload && !UINavigatorUtil.isMobile(context) && docHasFiles)
                {
                    if (is3DViaSupported(context, encodedFileName))
                    {
                        if ( !isprinterFriendly )
                        {
                            s3DViaURL   = "../components/emxLaunch3DLiveExamine.jsp?objectId="+ XSSUtil.encodeForJavaScript(context, masterId) +"&amp;mode=fileBased&amp;fileName="+encodedFileName+"&amp;fileFormat="+encodedFormat;
                            fileActionsStrBuff.append("<a href='javascript:showModalDialog(\"" + s3DViaURL + "\",600,600)'>");
                            fileActionsStrBuff.append("<img border='0' src='../common/images/iconSmallShowHide3D.gif' alt=\""+sTipDownload+"\" title=\""+sTip3DVIAViewer+"\"></img></a>&#160;");
                        }
                        else
                        {
                            fileActionsStrBuff.append("<img border='0' src='../common/images/iconSmallShowHide3D.gif' alt=\""+sTipDownload+"\" title=\""+sTip3DVIAViewer+"\"></img></a>&#160;");
                        }
                    }
                    if ( !isprinterFriendly )
                    {
                      downloadURL = "javascript:callCheckout('"+ masterId +"','download', '"+ fileName+ "', '" + fileFormat +"', null, null, null, null, '"+strPartId+"');";
                      viewerURL = getViewerURL(context, masterId, fileFormat, fileName, strPartId);

                      fileActionsStrBuff.append(viewerURL);
                      fileActionsStrBuff.append("<a href=\"" + downloadURL +"\">");
                      fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"></img></a>&#160;");
                    } else {
                      fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionView.gif' alt=\""+i18nViewerTip+"\" title=\""+i18nViewerTip+"\"></img></a>&#160;");
                      fileActionsStrBuff.append("<img border='0' src='../common/images/iconActionDownload.gif' alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"></img></a>&#160;");
                    }
                }

                fileActions = fileActionsStrBuff.toString();
                fileActionsVector.add(fileActions);
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            return fileActionsVector;
        }
    }

    /**
     * This method is used to get the list of files and their
     * versions in master (i.e. document holder) object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getFileVersions(Context context, String[] args) throws Exception
    {
        try
        {
            HashMap programMap         = (HashMap) JPO.unpackArgs(args);
            String  masterObjectId     = (String) programMap.get("objectId");
            DomainObject masterObject = DomainObject.newInstance(context, masterObjectId);

            //Added to make a single database call to
            StringList masterObjectSelectList = new StringList(7);
            masterObjectSelectList.add(CommonDocument.SELECT_ID);
            masterObjectSelectList.add(CommonDocument.SELECT_FILE_NAME);
            masterObjectSelectList.add(CommonDocument.SELECT_FILE_FORMAT);
            masterObjectSelectList.add(CommonDocument.SELECT_FILE_SIZE);
            masterObjectSelectList.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            masterObjectSelectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);

            // get the Master Object data
            Map masterObjectMap = masterObject.getInfo(context,masterObjectSelectList);
            // Version Object seletcs
            StringList versionSelectList = new StringList(6);
            versionSelectList.add(CommonDocument.SELECT_ID);
            versionSelectList.add(CommonDocument.SELECT_FILE_NAME);
            versionSelectList.add(CommonDocument.SELECT_FILE_FORMAT);
            versionSelectList.add(CommonDocument.SELECT_FILE_SIZE);
            versionSelectList.add(CommonDocument.SELECT_TITLE);
            versionSelectList.add(CommonDocument.SELECT_MASTER_ID);
 			versionSelectList.add(CommonDocument.SELECT_LAST_ID);

            StringList versionMultiSelectList = new StringList(6);
            versionMultiSelectList.add(CommonDocument.SELECT_FILE_NAME);
            versionMultiSelectList.add(CommonDocument.SELECT_FILE_FORMAT);
            versionMultiSelectList.add(CommonDocument.SELECT_FILE_SIZE);

            // get the file (Version Object) data
            MapList versionList = masterObject.getRelatedObjects(context,
                                                                  CommonDocument.RELATIONSHIP_ACTIVE_VERSION,
                                                                  CommonDocument.TYPE_DOCUMENTS,
                                                                  versionSelectList,
                                                                  null,
                                                                  false,
                                                                  true,
                                                                  (short)1,
                                                                  null,
                                                                  null,
                                                                  null,
                                                                  null,
                                                                  null);
            Iterator versionItr  = versionList.iterator();

            // get all the file data from the Master Object
            StringList fileList       = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_NAME);
            StringList fileFormatList = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_FORMAT);
            StringList fileSizeList   = (StringList) masterObjectMap.get(CommonDocument.SELECT_FILE_SIZE);

            String fileFormat = null;
            String fileSize   = null;

            // get the Master Object meta data
            String masterId    = (String) masterObjectMap.get(CommonDocument.SELECT_ID);
            String canCheckout = (String) masterObjectMap.get(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            String moveFile    = (String) masterObjectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);

            // loop thru each file to build MapList, each Map corresponds to one file
            MapList fileMapList = new MapList();
            while(versionItr.hasNext())
            {
                Map fileActiveVersionMap     = (Map)versionItr.next();
                String versionFileName = (String)fileActiveVersionMap.get(CommonDocument.SELECT_TITLE);

                fileFormat = "";
                fileSize   = "";

                // get the file corresponding to this Version by filtering the above fileList
                int index = fileList.indexOf(versionFileName);

                // get the File Format
                if (index != -1 && fileFormatList != null && fileFormatList.size() >= index )
                {
                  fileFormat = (String)fileFormatList.get(index);
                }

                // get the File Size
                if (index != -1 && fileSizeList != null && fileSizeList.size() >= index )
                {
                  fileSize = (String)fileSizeList.get(index);
                }

                // if all versions are to be included, get the revisions of
                // each version object found
                String versionId = (String) fileActiveVersionMap.get(CommonDocument.SELECT_ID);
                DomainObject versionObj = DomainObject.newInstance(context, versionId);

                // for the Id passed, get revisions Info
                MapList revisionsList = versionObj.getRevisionsInfo(context,versionSelectList,
                                        versionMultiSelectList);

                // get all the file Versions corresponding to this file
                for (int j=0; j<revisionsList.size(); j++)
                {
                    Map fileVersionMap = (Map)revisionsList.get(j);

                    fileVersionMap.put(CommonDocument.SELECT_MOVE_FILES_TO_VERSION, moveFile);
                    fileVersionMap.put(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS, canCheckout);
                    String verTitle = (String) fileVersionMap.get(CommonDocument.SELECT_TITLE);
                    String verFileFormat =  "";
                    String verFileSize   =  "";
                    try
                    {
                        verFileFormat =  (String)fileVersionMap.get(CommonDocument.SELECT_FILE_FORMAT);
                        verFileSize   =  (String)fileVersionMap.get(CommonDocument.SELECT_FILE_SIZE);
                    } catch(ClassCastException cex) {
                        StringList verFileList = (StringList)fileVersionMap.get(CommonDocument.SELECT_FILE_NAME);
                        StringList verFileFormatList = (StringList)fileVersionMap.get(CommonDocument.SELECT_FILE_FORMAT);
                        StringList verFileSizeList  =  (StringList )fileVersionMap.get(CommonDocument.SELECT_FILE_SIZE);

                        // get the file corresponding to this Version by filtering the above fileList
                        index = verFileList.indexOf(verTitle);

                        // get the File Format
                        if (index != -1 && verFileFormatList != null && verFileFormatList.size() >= index )
                        {
                            verFileFormat = (String)verFileFormatList.get(index);
                        }

                        // get the File Size
                        if (index != -1 && verFileSizeList != null && verFileSizeList.size() >= index )
                        {
                            verFileSize = (String)verFileSizeList.get(index);
                        }
                    }

                    if(verFileFormat == null || "".equals(verFileFormat))
                    {
                        fileVersionMap.put(CommonDocument.SELECT_FILE_FORMAT, fileFormat);
                        fileVersionMap.put(CommonDocument.SELECT_FILE_SIZE, fileSize);
                    } else {
                        fileVersionMap.put(CommonDocument.SELECT_FILE_FORMAT, verFileFormat);
                        fileVersionMap.put(CommonDocument.SELECT_FILE_SIZE, verFileSize);
                    }

                    fileMapList.add(fileVersionMap);
                }

            }

            return fileMapList;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * This method is used to get the viewer URL for all viewers for given format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the objectid from which file need to be checked out
     * @param format the format from which file need to be checked out
     * @param fileName the fileName to be checked out
     * @returns String URL for all viewers
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */
    public static String getViewerURL(Context context, String objectId, String format, String fileName) throws Exception
    {
    return getViewerURL(context, objectId, format, fileName, null, false);
  }

    /**
     * This method is used to get the viewer URL for all viewers for given format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the objectid from which file need to be checked out
     * @param format the format from which file need to be checked out
     * @param fileName the fileName to be checked out
     * @param bUIType to check the request is from SB or Table page
     * @returns String URL for all viewers
     * @throws Exception if the operation fails
     * @since Common 10.5
     * @grade 0
     */
    public static String getViewerURL(Context context, String objectId, String format, String fileName,boolean bUIType) throws Exception
    {
    return getViewerURL(context, objectId, format, fileName, null, bUIType);
  }


  /**
     * This method is used to get the viewer URL for all viewers for given format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the objectid from which file need to be checked out
     * @param format the format from which file need to be checked out
     * @param fileName the fileName to be checked out
     * @param partId the partId for which document belongs to
     * @returns String URL for all viewers
     * @throws Exception if the operation fails
     * @since Common 11.0
     * @grade 0
     */
    public static String getViewerURL(Context context, String objectId, String format, String fileName, String partId) throws Exception
    {
        return getViewerURL(context,objectId,format,fileName,partId,false);
    }

    /**
     * This method is used to get the viewer URL for all viewers for given format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId the objectid from which file need to be checked out
     * @param format the format from which file need to be checked out
     * @param fileName the fileName to be checked out
     * @param partId the partId for which document belongs to
     * @param bUIType to check the request is from SB or Table page
     * @returns String URL for all viewers
     * @throws Exception if the operation fails
     * @since Common 11.0
     * @grade 0
     */
    public static String getViewerURL(Context context, String objectId, String format, String fileName, String partId,boolean bUIType) throws Exception
    {
        try
        {
            Map formatViewerMap = FormatUtil.getViewerCache(context);
            String returnURL = "";
            String URLParameters = "?action=view&amp;id="+objectId+"&amp;objectId="+objectId+"&amp;format="+format+"&amp;file="+fileName+"&amp;fileName="+fileName;
            Map formatDetailsMap = (Map)formatViewerMap.get(format);
            if ( formatDetailsMap == null )
            {
                FormatUtil.loadViewerCache(context);
            }
            formatDetailsMap = (Map)formatViewerMap.get(format);
            String viewerURL = "";
            String servletPreFix = EnoviaResourceBundle.getProperty(context,"emxFramework.Viewer.ServletPreFix");
            String viewerTip = "Default";
            String i18nViewerTip = i18nNow.getViewerI18NString(viewerTip, context.getSession().getLanguage());
            String servletURL = "";
            StringBuffer fileViewerURL = new StringBuffer(256);
            String aliasFormat = FrameworkUtil.getAliasForAdmin(context,"format", format ,true);
            FormatUtil formatUtil = new FormatUtil(aliasFormat);
            String viewer = formatUtil.getViewerPreference(context, null);
            if ( formatDetailsMap == null )
            {
                if(!bUIType)
                    viewerURL = "<a href=\"javascript:callCheckout('"+ XSSUtil.encodeForJavaScript(context, objectId) +"','view', '"+ XSSUtil.encodeForJavaScript(context, fileName)+ "', '" + XSSUtil.encodeForJavaScript(context, format) +"', null, null, null, null, '"+XSSUtil.encodeForJavaScript(context, partId)+"');\"><img src=\"../common/images/iconActionView.png\" border=\"0\" alt=\""+i18nViewerTip+"\" title=\""+i18nViewerTip+"\"></img></a>";
				else
                    viewerURL = "<a href=\"javascript:openViewer('"+ XSSUtil.encodeForJavaScript(context, objectId) +"','view', '"+ XSSUtil.encodeForJavaScript(context, fileName)+ "', '" + XSSUtil.encodeForJavaScript(context, format) +"', null, null, null, null, '"+XSSUtil.encodeForJavaScript(context, partId)+"');\"><img src=\"../common/images/iconActionView.png\" border=\"0\" alt=\""+i18nViewerTip+"\" title=\""+i18nViewerTip+"\"></img></a>";
				returnURL = viewerURL;
            } else {
                java.util.Set set = formatDetailsMap.keySet();
                Iterator itr = set.iterator();
                boolean needDefaultViewer = false;
                while (itr.hasNext())
                {
                    viewerURL = (String)itr.next();
                    if ( viewer == null || "".equals(viewer) || "null".equals(viewer) || viewerURL.equals(viewer) )
                    {
                        needDefaultViewer = true;
                        viewerTip = ((String)formatDetailsMap.get(viewerURL));
                        i18nViewerTip = i18nNow.getViewerI18NString(viewerTip, context.getSession().getLanguage());
                        if ( viewerTip.equalsIgnoreCase("Default") )
                        {
                            if(!bUIType)
                                viewerURL = "<a href=\"javascript:getTopWindow().callCheckout('"+XSSUtil.encodeForJavaScript(context, objectId)+"','view', '"+XSSUtil.encodeForJavaScript(context, fileName)+"', '"+XSSUtil.encodeForJavaScript(context, format)+"', null, null, null, null, '"+XSSUtil.encodeForJavaScript(context, partId)+"');\"><img src=\"../common/images/iconActionView.png\" border=\"0\" alt=\""+i18nViewerTip+"\" title=\""+i18nViewerTip+"\"></img></a>";
                            else
                                viewerURL = "<a href=\"javascript:openViewer('"+ XSSUtil.encodeForJavaScript(context, objectId) +"','view', '"+ XSSUtil.encodeForJavaScript(context, fileName)+ "', '" + XSSUtil.encodeForJavaScript(context, format) +"', null, null, null, null, '"+XSSUtil.encodeForJavaScript(context, partId)+"');\"><img src=\"../common/images/iconActionView.png\" border=\"0\" alt=\""+i18nViewerTip+"\" title=\""+i18nViewerTip+"\"></img></a>";
						} else {
                            viewerURL = servletPreFix + viewerURL + "?action=view&amp;";
                            viewerURL = "<a href=\"javascript:callViewer('"+ XSSUtil.encodeForJavaScript(context, objectId) +"','view', '"+ XSSUtil.encodeForJavaScript(context, fileName)+ "', '" + XSSUtil.encodeForJavaScript(context, format) +"', '"+ XSSUtil.encodeForJavaScript(context, viewerURL) +"', '"+ XSSUtil.encodeForJavaScript(context, partId)+"');\"><img src=\"../common/images/iconActionView.png\" border=\"0\" alt=\""+i18nViewerTip+"\" title=\""+i18nViewerTip+"\"></img></a>";
					}
                    }
                }
                if ( !needDefaultViewer )
                {
                    if(!bUIType)
                        viewerURL = "<a href=\"javascript:callCheckout('"+XSSUtil.encodeForJavaScript(context, objectId)  +"','view', '"+ XSSUtil.encodeForJavaScript(context, fileName)+ "', '" + XSSUtil.encodeForJavaScript(context, format) +"', null, null, null, null, '"+XSSUtil.encodeForJavaScript(context, partId)+"');\"><img src=\"../common/images/iconActionView.png\" border=\"0\" alt=\""+i18nViewerTip+"\" title=\""+i18nViewerTip+"\"></img></a>";
					else
                        viewerURL = "<a href=\"javascript:openViewer('"+ XSSUtil.encodeForJavaScript(context, objectId) +"','view', '"+ XSSUtil.encodeForJavaScript(context, fileName)+ "', '" + XSSUtil.encodeForJavaScript(context, format) +"', null, null, null, null, '"+XSSUtil.encodeForJavaScript(context, partId)+"');\"><img src=\"../common/images/iconActionView.png\" border=\"0\" alt=\""+i18nViewerTip+"\" title=\""+i18nViewerTip+"\"></img></a>";
          		}
                returnURL = viewerURL;
            }
            return returnURL;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    private static MapList getUpdatedColumnValues(Context context, MapList objectList, StringList selectables) throws Exception {
        String oidsArray[] = new String[objectList.size()];
        for (int i = 0; i < objectList.size(); i++)
        {
            String objId = (String)((Map)objectList.get(i)).get("id");
            oidsArray[i] = (String)(FrameworkUtil.split(objId, "~").get(0));

        }
        selectables.add(CommonDocument.SELECT_ID);
        return DomainObject.getInfo(context, oidsArray, selectables);
    }

    /**
     * This method is used to get the file extension
     *
     * @param sFileName is the file name
     * @returns file extension
     * @since BPS R210
     */
    public static String getFileExtension(String sFileName)
    {
        int index = sFileName.lastIndexOf('.');

        if (index == -1)
        {
            return sFileName;
        } else
        {
            return sFileName.substring(index + 1, sFileName.length());
        }
    }

    /**
     * Checks whether 3DVIA icon should be displayed for a given file or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @param sFileName the file Name
     * @return true if the file extension is available in properties file
     * @throws Exception
     */
    private static boolean is3DViaSupported(Context context, String sFileName)
    throws Exception
    {
        boolean is3DViaSupported        = false;
        String sFileExtn                = getFileExtension(sFileName);
        String s3DVIASuppFileExtns      = EnoviaResourceBundle.getProperty(context,"emxComponents.3DVIAViewer.SupportedFileExtensions");

        if (s3DVIASuppFileExtns != null && !"".equals(s3DVIASuppFileExtns))
        {
            StringList sl3DVIASuppExtns = FrameworkUtil.split(s3DVIASuppFileExtns.toLowerCase(), ",");
            if (sFileExtn != null && !"".equals(sFileExtn) && sl3DVIASuppExtns.contains(sFileExtn.toLowerCase()))
            {
                is3DViaSupported        = true;

            }
        }
        return is3DViaSupported;

    }
	
    /**
     * Returns format of the given file object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *                0 - HashMap programMap
     * @return format of the file object as a String
     * @throws Exception
     * 
     */
    public String showFileFormat(Context context, String[] args) throws Exception
    {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	Map requestMap = (HashMap)programMap.get("requestMap");
    	String fileObjectId = (String) requestMap.get("objectId");
    	String masterObjectId = (String) requestMap.get("parentOID");
    	if(UIUtil.isNullOrEmpty(masterObjectId))
    	masterObjectId = getMasterObject(context, fileObjectId);
    	HashMap newMap = new HashMap(); 
    	newMap.put("objectId", masterObjectId);
    	MapList fieldsMapList = (MapList) getFiles(context, JPO.packArgs(newMap));
    	Iterator mapListItr = fieldsMapList.iterator();
    	String fileFormat = "";
    	while(mapListItr.hasNext()){
    		Hashtable fieldMap = (Hashtable) mapListItr.next();
    		String curFileId = (String) fieldMap.get("id");
    		if(fileObjectId.equals(curFileId)){
    			fileFormat =  (String) fieldMap.get(CommonDocument.SELECT_FILE_FORMAT);
    			break;
    		}
    	}
        String languageStr = (String) requestMap.get("languageStr");
    	languageStr = languageStr == null ? (String)((HashMap)programMap.get("requestMap")).get("languageStr") : languageStr;
    	if(!"".equals(fileFormat)){
    		fileFormat =   i18nNow.getFormatI18NString(fileFormat, languageStr);
    	}
    	return fileFormat;
    }
 
    
    /**
     * Returns size of the given file object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *                0 - HashMap programMap
     * @return size of the file object as a String
     * @throws Exception
     * @since BPS R214
     */
    public String showFileSize(Context context, String[] args) throws Exception
	{
	   	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	Map requestMap = (HashMap)programMap.get("requestMap");
    	String fileObjectId = (String) requestMap.get("objectId");
    	String masterObjectId = (String) requestMap.get("parentOID");
    	if(UIUtil.isNullOrEmpty(masterObjectId))
    	masterObjectId = getMasterObject(context, fileObjectId);
    	HashMap newMap = new HashMap(); 
    	newMap.put("objectId", masterObjectId);
    	MapList fieldsMapList = (MapList) getFiles(context, JPO.packArgs(newMap));
    	Iterator mapListItr = fieldsMapList.iterator();
    	String fileSize = "";
    	while(mapListItr.hasNext()){
    		Hashtable fieldMap = (Hashtable) mapListItr.next();
    		String curFileId = (String) fieldMap.get("id");
    		if(fileObjectId.equals(curFileId)){
    			fileSize =  (String) fieldMap.get(CommonDocument.SELECT_FILE_SIZE);
    			return formatFileSize(fileSize);
    		}
    	}
    	return fileSize;
    	
    }
    /**
     * Returns master ObjectId for the given file object
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId is file objectId
     * @return master objectId of the file object as a String
     * @throws Exception
     * @since BPS R214 HF19
     */
    private String getMasterObject(Context context, String objectId) throws Exception
	{    	
    	DomainObject versionObject = DomainObject.newInstance(context, objectId);
    	String masterId = versionObject.getInfo(context,CommonDocument.SELECT_MASTER_ID);
    	if(UIUtil.isNullOrEmpty(masterId)){
    		masterId = objectId;
    	}
		return masterId;
    }
}

