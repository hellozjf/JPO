/*
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 */

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.VCDocument;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * @version Team 10-0-1-0
 */
public class emxTeamContentBase_mxJPO extends emxDomainObject_mxJPO
{
	private static final long serialVersionUID = 6466194743260143557L;
	static final String sAttributeBracket        = "attribute[";
	static final String sCloseBracket            = "]";
	static final String sToBracket               = "to[";
	static final String sBracketFromId           = "].from.id";
	static final String sBracketToId             = "].to.id";
	static final String sBracketFromName         = "].from.name";
	static final String hasReadStr               = "current.access[read,checkout]";
	static final String sFromBracket             = "from[";
	static final String sBracketToToBracket      = "].to.to.[";
	static final String hasReadWriteStr          = "current.access[checkin,modify,lock,unlock,revise]";
	static final String hasRemoveStr             = "current.access[delete,todisconnect,fromdisconnect]";
	// Designer Central Changes
	static final String SELECT_ACTIVE_VERSION_ID = "relationship[Active Version].to.id";
	// sort default by content title
	protected static final String sbSortKey = sAttributeBracket + DomainObject.ATTRIBUTE_TITLE + sCloseBracket;
	// String for selecting workspaceid through RouteScope rel
	protected static final String sbSelWsId = sToBracket+DomainObject.RELATIONSHIP_ROUTE_SCOPE+sBracketFromId;
	protected static final String sbSelMsgId  = sToBracket+DomainObject.RELATIONSHIP_MESSAGE_ATTACHMENTS+sBracketFromId;
	protected static final String sbSelMeetId = sToBracket+DomainObject.RELATIONSHIP_MEETING_ATTACHMENTS+sBracketFromId;
	protected static final String sbSelFolId  = sToBracket+DomainObject.RELATIONSHIP_VAULTED_DOCUMENTS+sBracketFromId;
	protected static final String sbSelRtId   = sFromBracket+DomainObject.RELATIONSHIP_OBJECT_ROUTE+sBracketToId;
	protected static final String sbSelRelActVerDesc = "relationship["+CommonDocument.RELATIONSHIP_ACTIVE_VERSION+"].to.description";
	protected static final String sbSelRelActVerRev = "relationship["+CommonDocument.RELATIONSHIP_ACTIVE_VERSION+"].to.revision";
	protected static final String sbSelRelActVerId = "relationship["+CommonDocument.RELATIONSHIP_ACTIVE_VERSION+sBracketToId;
	protected static final String sbLockedSelect = "relationship["+CommonDocument.RELATIONSHIP_ACTIVE_VERSION+"].to.locked";
	protected static final String sbLockerSelect = "relationship["+CommonDocument.RELATIONSHIP_ACTIVE_VERSION+"].to.locker";
	
  /**
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public emxTeamContentBase_mxJPO (Context context, String[] args) throws Exception
  {
    super(context, args);
  }

  /**
   * This method will be called when ever we invoke the JPO with out calling any method explicitly
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no value
   * @return int
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public int mxMain(Context context, String[] args)
    throws Exception
  {
    if (!context.isConnected())
      throw new Exception(ComponentsUtil.i18nStringNow("emxTeamCentral.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
      return 0;
  }

  /**
  * Determine if Document contains multiple files.
  * @param context the eMatrix <code>Context</code> object.
  * @param docOID - the Object ID of the Document.
  * @return boolean true if the document contains more than one file. Otherwise, false.
  * @exception Exception if the operation fails.
  */
  private static boolean containsMultipleFiles(Context context, String docOID) throws Exception
  {
    boolean multipleFiles = false;
    DomainObject docObject = DomainObject.newInstance(context, docOID);
    docObject.open(context);
    StringList tmpList = docObject.getInfoList(context, sbSelRelActVerId.toString());
    if (tmpList != null && tmpList.size() > 1) {
      multipleFiles = true;
    }

    docObject.close(context);
    return multipleFiles;
  }

  /**
   * This method is used to show the Lock image in column "Lock Image" of table TMCContentSummary (table_TMCContentSummary).
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - objectList MapList
   * @return Object of type Vector
   * @throws Exception if the operation fails
   * @since V10 Patch1
   */
  public Vector getLockImage(Context context, String[] args) throws Exception
  {
    Vector showLock= new Vector();
    
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList)programMap.get("objectList");
      
      int objectListSize = 0 ;
      if(objectList != null) {
        objectListSize = objectList.size();
      }
      for(int i=0; i< objectListSize; i++)
      {
    	Map objectMap = (Hashtable) objectList.get(i);
    	boolean documentLocked = false;
    	boolean boolRoute = false;
    	
    	String docRouteId = "";
        if (objectMap.get(sbSelRtId) != null) {
            docRouteId  = (String) objectMap.get(sbSelRtId);
        }
        if (docRouteId != null && !"".equals(docRouteId) && !"null".equals(docRouteId)) {
          boolRoute = true;
        }
        
        // Check for Documents from other Apps that contain multiple files.
        String documentId = (String)objectMap.get(DomainConstants.SELECT_ID);
        boolean multiFile = containsMultipleFiles(context, documentId);
        String docLock = "";
        if (!multiFile) {
          docLock = (String)objectMap.get(sbLockedSelect.toString());
          if (docLock == null) {
            docLock = "";
          }
        }
        
        if (docLock.equalsIgnoreCase("TRUE")) {
        	documentLocked = true;
        }
        
        String statusImage = "";
        boolean bIsTypeWorkspaceVault = ((Boolean)objectMap.get("bIsTypeWorkspaceVault")).booleanValue();
        
        if (boolRoute && bIsTypeWorkspaceVault) {           
        	statusImage = "<img style='border:0; padding: 2px;' src='../common/images/iconStatusRouteLocked.gif' alt=''>";           
        } else if (documentLocked) {        	
        	statusImage = "<img style='border:0; padding: 2px;' src='../common/images/iconStatusLocked.gif' alt=''>";
        }
        showLock.add(statusImage);
      }

    return  showLock;
  }


    /**
     * getVersion - This method is used to get the Version based on the
     *              Type of the Content.
     * For Documents, it shows the Version and For Type like Part, RFQ, etc.
     * it shows ""
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Object of type Vector
     * @throws Exception if the operation fails
     * @since Team 10-0-1-0
     */
    public Vector getVersion(Context context, String[] args)throws Exception{
      Vector version= new Vector();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map paramList = (Map)programMap.get("paramList");
        MapList objectList = (MapList)programMap.get("objectList");

        //Added for IR-071890V6R2012 starts
        boolean isprinterFriendly = false;
        if(paramList.get("reportFormat") != null) {
           isprinterFriendly = true;
        }
        
        Iterator objectListItr = objectList.iterator();

        while(objectListItr.hasNext())
        {
          Map objectMap = (Map) objectListItr.next();
          String docType = (String) objectMap.get(DomainConstants.SELECT_TYPE);        
          String documentId = (String)objectMap.get(DomainConstants.SELECT_ID);
          DomainObject documentObject = DomainObject.newInstance(context, documentId);
          if(UIUtil.isNullOrEmpty(docType)){
        	  docType=documentObject.getInfo(context, "type");
          }
          StringBuilder sbURL = new StringBuilder();
          
          if (docType.equals(DomainConstants.TYPE_DOCUMENT))
          {
            boolean isVersionable = false;
            if(CommonDocument.TYPE_DOCUMENTS.equals(CommonDocument.getParentType(context, docType)) ) {
              if (UIUtil.isNotNullAndNotEmpty(documentId))
              {
                isVersionable = CommonDocument.allowFileVersioning(context, documentId);
              } else {
                isVersionable = CommonDocument.checkVersionableType(context, docType);
              }
            }

            if (isVersionable)
            {
              boolean multiFile = containsMultipleFiles(context, documentId);
              if (!multiFile) {
                String docVer = (String)objectMap.get(sbSelRelActVerRev);
                if(UIUtil.isNullOrEmpty(docVer)) {
                	docVer =  documentObject.getInfo(context, sbSelRelActVerRev);
                }
                //The below code is written to handle non-versioned documents
                if(docVer == null || docVer.length() == 0 ) {
                	docVer = "";
                }
                docVer.trim();
                if(isprinterFriendly) {
                	sbURL.append(XSSUtil.encodeForHTML(context, docVer));
                } else {
                	String sFileTableHeader = EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",
        					context.getSession().getLocale(), "emxComponents.Common.Versions");
                    StringBuilder sbNextURL = new StringBuilder();
                    sbNextURL.append("../common/emxTable.jsp?HelpMarker=emxhelpversions&amp;program=emxCommonFileUI:getFileVersions&amp;table=APPFileVersions&amp;sortColumnName=Version&amp;sortDirection=descending&amp;header=");
                    sbNextURL.append(sFileTableHeader);
                    sbNextURL.append("&amp;objectId=");
                    sbNextURL.append(documentId);
                    sbNextURL.append("&amp;parentOID=");
                    sbNextURL.append(documentId);
                    
                	sbURL.append("<a href =\"javascript:showModalDialog('");
                    sbURL.append(sbNextURL.toString());
                    sbURL.append("',575,575)\">");
                    sbURL.append(docVer).append("</a>");
                }
              }
            }
          } else {
            sbURL.append("&#160;");
          }
          version.add(sbURL.toString());
        }
      return version;
   }


     /**
     * getRevison - This method is used to get the Revision  basing on the Type of the Content.
     *              For Documents It shows "" and For Type like Part, RFQ etc etc it Shows Revision
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @return Object of type Vector
     * @throws Exception if the operation fails
     * @since Team 10-0-1-0
     */
	public Vector getRevision(Context context, String[] args) throws Exception{
		Vector revision= new Vector();
		Map programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList)programMap.get("objectList");
		Iterator objectListItr = objectList.iterator();

		while(objectListItr.hasNext()){
			Map objectMap = (Map) objectListItr.next();
			String docType = (String) objectMap.get(DomainConstants.SELECT_TYPE);

			if (docType.equals(DomainConstants.TYPE_DOCUMENT)) {
				revision.add("");
			}else{
				revision.add(objectMap.get(DomainConstants.SELECT_REVISION));
			}
		}

		return revision;
	}

    // Designer Central
    private HashMap getViewerURLInfo(Context context, String objectId, String fileName) throws Exception
    {
      HashMap viewerInfoMap = new HashMap();;
      DomainObject obj = DomainObject.newInstance(context, objectId);
      MapList associatedFileList = obj.getAllFormatFiles(context);
      for (int i = 0; i < associatedFileList.size(); i++)
      {
    	  Map associatedFile = (Map)associatedFileList.get(i);
    	  if (fileName.equals((String)associatedFile.get("filename")))
    	  {
    		  viewerInfoMap.put("fileName", fileName);
    		  viewerInfoMap.put("format", associatedFile.get("format"));
    		  viewerInfoMap.put("id", objectId);
    		  break;
    	  }
      }
      return viewerInfoMap;
    }

  // Designer Central


  /**
   * getContentActions - This method will be called to get the Actions that
   *                     can be performed on the Content
   *                     This is called in the Actions Column of the Table
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - objectList MapList
   * @return Object of type Vector
   * @throws Exception if the operation fails
   * @since V10 Patch1
   */
  public Vector getContentActions(Context context, String[] args)
    throws Exception
  {
     Vector vActions = new Vector();
     try
     {
		// Start MSF
		String msfRequestData = "";
		// End MSF
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");
        Map paramList = (Map)programMap.get("paramList");
        String uiType = (String)paramList.get("uiType");
        String customSortColumns = (String)paramList.get("customSortColumns");
        String customSortDirections = (String)paramList.get("customSortDirections");
        String table = (String)paramList.get("table");
		// Start MSF
		String msfFileFormatDetails = "";
		boolean isAdded = false;
		// End MSF
        if(objectList == null || objectList.size() <= 0) {
            return vActions;
        }
        boolean isprinterFriendly = false;
        if (paramList.get("reportFormat") != null)
        {
            isprinterFriendly = true;

        }

        //boolean isprinterFriendly = false;
        Locale locale = context.getSession().getLocale();
        String sTipDownload = EnoviaResourceBundle.getProperty(context,  "emxTeamCentralStringResource", locale, "emxTeamCentral.ContentSummary.ToolTipDownload");
        String sTipSubscription = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", locale, "emxTeamCentral.ContentSummary.ToolTipSubscription");
        String sTipCheckout = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", locale, "emxTeamCentral.ContentSummary.ToolTipCheckout");
        String sTipCheckin = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", locale, "emxTeamCentral.ContentSummary.ToolTipCheckin");
        String sTipAddFiles = EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", locale, "emxTeamCentral.ContentSummary.ToolTipAddFiles");

        StringList 	selectTypeStmts = new StringList(1);
			        selectTypeStmts.add(DomainConstants.SELECT_ID);
			        selectTypeStmts.add(DomainConstants.SELECT_TYPE);
			        selectTypeStmts.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
			        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
			        selectTypeStmts.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
			        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
			        selectTypeStmts.add(CommonDocument.SELECT_FILE_NAME);
			        selectTypeStmts.add(CommonDocument.SELECT_FILE_FORMAT);
			        selectTypeStmts.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
			        selectTypeStmts.add(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
			        selectTypeStmts.add("vcfile");
			        selectTypeStmts.add("vcmodule");
			        selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
			        selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
			        selectTypeStmts.add(CommonDocument.SELECT_HAS_TOCONNECT_ACCESS);
			        selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
			        selectTypeStmts.add(CommonDocument.SELECT_OWNER);
			        selectTypeStmts.add(CommonDocument.SELECT_LOCKED);
			        selectTypeStmts.add(CommonDocument.SELECT_LOCKER);
			        selectTypeStmts.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION);
					selectTypeStmts.add(CommonDocument.SELECT_HAS_UNLOCK_ACCESS);

        //Getting all the content ids
        String oidsArray[] = new String[objectList.size()];
        for (int i = 0; i < objectList.size(); i++) {
        	oidsArray[i] = (String)((Map)objectList.get(i)).get("id");
        }

        MapList objList = DomainObject.getInfo(context, oidsArray, selectTypeStmts);
        Iterator objectListItr = objList.iterator();
        
        while(objectListItr.hasNext()){
		  // Start MSF
		  msfFileFormatDetails = "";
		  // End MSF
          Map contentObjectMap = (Map)objectListItr.next();
          
		  // Start MSF
          StringList activeVersionIDList = (StringList) contentObjectMap.get(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
		  // End MSF
          StringList fileList = (StringList) contentObjectMap.get(CommonDocument.SELECT_FILE_NAME);
		  // Start MSF
          StringList activeVersionFileList = (StringList) contentObjectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
		  // End MSF
          StringList fileFormatList = (StringList) contentObjectMap.get(CommonDocument.SELECT_FILE_FORMAT);
          StringList tempfileList  = new StringList();
		  // Start MSF
		  isAdded = false;
		  int nbActiveVersionFileList = 0;
		  if (activeVersionFileList != null)
			  nbActiveVersionFileList = activeVersionFileList.size();
		  // End MSF
          for(int ii =0; ii< fileFormatList.size(); ii++){
          	String format = (String)fileFormatList.get(ii);
          	if(!DomainObject.FORMAT_MX_MEDIUM_IMAGE.equalsIgnoreCase(format)){
				// Start MSF
				String temFileName = (String)fileList.get(ii);
			    // End MSF
          		tempfileList.add(fileList.get(ii));
			    // Start MSF
				if(nbActiveVersionFileList > ii)
				{
					if(!msfFileFormatDetails.isEmpty())
						msfFileFormatDetails += ", ";

					msfFileFormatDetails += "{FileName: '" + XSSUtil.encodeForJavaScript(context, (String)activeVersionFileList.get(ii)) + 
						"', Format: '" + XSSUtil.encodeForJavaScript(context, format) + 
						"', VersionId: '" + XSSUtil.encodeForJavaScript(context, (String)activeVersionIDList.get(ii)) + "'}";
				}
			    // End MSF
          	}
          }
		  // Start MSF
		  msfFileFormatDetails = "MSFFileFormatDetails:[" + msfFileFormatDetails + "]";
		  // End MSF
          fileList = tempfileList;
          int fileCount = 0;
          String vcInterface = "";
          boolean vcDocument = false;
          boolean vcFile = false;
          String docType = "";
          String activeFileVersionID = "";
          String sFileName = "";
          String newURL = "&#160;";
          String docLocker = "";

          try{
              docLocker = (String)contentObjectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKER);
              if(docLocker==null)
                  docLocker = (String)contentObjectMap.get(CommonDocument.SELECT_LOCKER);
              }catch(ClassCastException ex){
                  docLocker = ((StringList)contentObjectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKER)).elementAt(0).toString();
                  if(docLocker==null)
                      docLocker = ((StringList)contentObjectMap.get(CommonDocument.SELECT_LOCKER)).elementAt(0).toString();
              }

          boolean moveFilesToVersion = (Boolean.valueOf((String) contentObjectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION))).booleanValue();
          String documentId = (String)contentObjectMap.get(DomainConstants.SELECT_ID);
          String strFileFormat  = null;
          DomainObject docObject = DomainObject.newInstance(context,documentId);
          strFileFormat = CommonDocument.getFileFormat(context,docObject);

          //   For getting the count of files
          HashMap filemap = new HashMap();
          filemap.put(CommonDocument.SELECT_MOVE_FILES_TO_VERSION, contentObjectMap.get(CommonDocument.SELECT_MOVE_FILES_TO_VERSION));
          filemap.put(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION, contentObjectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION));
          filemap.put(CommonDocument.SELECT_FILE_NAME, fileList);
          fileCount = CommonDocument.getFileCount(context,filemap);
          contentObjectMap.put("fileCount",String.valueOf(fileCount));// Integer.toString(fileCount));

          vcInterface = (String)contentObjectMap.get(CommonDocument.SELECT_IS_KIND_OF_VC_DOCUMENT);
                vcDocument = "TRUE".equalsIgnoreCase(vcInterface)?true:false;

          
          docType    = (String)contentObjectMap.get(DomainConstants.SELECT_TYPE);
          String parentType = CommonDocument.getParentType(context, docType);
          if (CommonDocument.TYPE_DOCUMENTS.equals(parentType))
          {
	        	// Can View
		          if(CommonDocument.canView(context, contentObjectMap)){
		
		              Object fileObj = contentObjectMap.get(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
		              if (fileObj instanceof String) {
		                  sFileName = (String)fileObj;
		              } else if(fileObj instanceof StringList) {
		                  sFileName = ((StringList)fileObj).elementAt(0).toString();
		              }
	
		              if (moveFilesToVersion){
		                      Object obj = contentObjectMap.get(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
		                      if (obj instanceof String) {
		                          activeFileVersionID = (String)obj;
		                      } else if(obj instanceof StringList) {
		                          activeFileVersionID = ((StringList)obj).elementAt(0).toString();
		                }
		                     // get the format that the Active version object contains the file
		                  HashMap viewerURLMap = getViewerURLInfo(context, activeFileVersionID, sFileName);
		                  if (!viewerURLMap.isEmpty()){
		                          //XSSOK
		                          newURL = emxCommonFileUI_mxJPO.getViewerURL(context,
		                                              activeFileVersionID, (String)viewerURLMap.get("format"), sFileName);
		                     }
		                  } else { // Designer Central Changes
		                     //XSSOK
		                     newURL = emxCommonFileUI_mxJPO.getViewerURL(context, documentId, strFileFormat, sFileName);
		                  }
	
		          }
		          //Can Download
		          if(CommonDocument.canDownload(context, contentObjectMap)){
	
	              	newURL+="<a href=\"javascript:callCheckout('"+XSSUtil.encodeForJavaScript(context, documentId)+"','download','','','"+XSSUtil.encodeForJavaScript(context, customSortColumns)+"','"+XSSUtil.encodeForJavaScript(context, uiType)+"','"+XSSUtil.encodeForJavaScript(context, table)+"');\"><img style=\"border:0; padding: 2px;\" src=\"../common/images/iconActionDownload.gif\" alt=\""+sTipDownload+ "\" title=\""+sTipDownload+"\"></img></a>";
	
		        	  // Changes for CLC start here..
		        	  //Show Download Icon for ClearCase Linked Objects
	                  //DomainObject ccLinkedObject  = DomainObject.newInstance(context, documentId);
		        	  String linkAttrName = PropertyUtil.getSchemaProperty(context,"attribute_MxCCIsObjectLinked");
		        	  String isObjLinked = null;
		        	  if(linkAttrName!=null && !linkAttrName.equals(""))
		        	  {
		        		  isObjLinked = docObject.getAttributeValue(context,linkAttrName);
		        	  }
	
		        	  if(isObjLinked!=null && !isObjLinked.equals(""))
		        	  {
		        		  if(isObjLinked.equalsIgnoreCase("True"))
		        		  {
		        			  //show download icon for Linked Objects
		        			  newURL+="<a href=\"../servlet/MxCCCS/MxCCCommandsServlet.java?commandName=downloadallfiles&objectId="+XSSUtil.encodeForURL(context, documentId)+"\"><img style=\"border:0; padding: 2px;\" src=\"../common/images/iconActionDownload.gif\" alt=\""+sTipDownload+"\" title=\""+sTipDownload+"\"></img></a>";
		        		  }
	
		        	  }
		          }
		          //show subscription link
		          StringBuffer strBuf = new StringBuffer(1256);
	        	  strBuf.append("../components/emxSubscriptionDialog.jsp?objectId=");
	        	  strBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
	        	  newURL+="<a href=\"javascript:showModalDialog('" +strBuf.toString()+ "','575','575')\"><img style=\"border:0; padding: 2px;\" src=\"../common/images/iconSmallSubscription.gif\" alt=\""+sTipSubscription+"\" title=\""+sTipSubscription+"\"></img></a>";
	
		          // Can Checkout
		          if(CommonDocument.canCheckout(context, contentObjectMap)){
		        	  newURL+="<a href=\"javascript:callCheckout('"+XSSUtil.encodeForJavaScript(context, documentId)+"','checkout','','','"+XSSUtil.encodeForJavaScript(context, customSortColumns)+"','"+XSSUtil.encodeForJavaScript(context, customSortDirections)+"', '"+XSSUtil.encodeForJavaScript(context, uiType)+"', '"+XSSUtil.encodeForJavaScript(context, table)+"');\"><img style=\"border:0; padding: 2px;\" src=\"../common/images/iconActionCheckOut.gif\" alt=\""+sTipCheckout+ "\" title=\""+sTipCheckout+"\"></img></a>";
		          }
	          // Can Checkin
			  boolean canCheckin = false;
	          if(CommonDocument.canCheckin(context, contentObjectMap) || VCDocument.canVCCheckin(context, contentObjectMap)){
			      canCheckin = true;
				  // MSF
				  msfRequestData = "{RequestType: 'CheckIn', DocumentID: '" + documentId + "', " + msfFileFormatDetails + "}";
				  // MSF
	        	  vcFile =(Boolean.valueOf((String) contentObjectMap.get("vcfile"))).booleanValue();
	
	    		  StringBuffer checkIn = new StringBuffer(1256);
	    		  if (!vcDocument)
	    		  {
	    			  	  checkIn.append("'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
	                      checkIn.append(XSSUtil.encodeForJavaScript(context, documentId));
	                      checkIn.append("&amp;folderId=");
	                      checkIn.append("null");
	                      checkIn.append("&amp;customSortColumns="); //Added for Bug #371651 starts
	                      checkIn.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
	                      checkIn.append("&amp;customSortDirections=");
	                      checkIn.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
	                      checkIn.append("&amp;uiType=");
	                      checkIn.append(XSSUtil.encodeForJavaScript(context, uiType));
	                      checkIn.append("&amp;table=");
	                      checkIn.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
	                      checkIn.append("&amp;showFormat=true&amp;showComments=required&amp;objectAction=update&amp;JPOName=emxTeamDocumentBase&amp;appDir=teamcentral&amp;appProcessPage=emxTeamPostCheckinProcess.jsp&amp;refreshTableContent=true','730','450'");
	                      newURL+="<a href=\"javascript:processModalDialog(" + msfRequestData + "," + checkIn.toString()+");\"><img style=\"border:0; padding: 2px;\" src=\"../common/images/iconActionCheckIn.gif\" alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>";
	    		  } else {
	    			  if(vcFile){

	    				  checkIn.append("'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
	                      checkIn.append(XSSUtil.encodeForJavaScript(context, documentId));
	                      checkIn.append("&amp;folderId=");
	                      checkIn.append("null");
	                      checkIn.append("&amp;customSortColumns=");         //Added for Bug #371651 starts
	                      checkIn.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
	                      checkIn.append("&amp;customSortDirections=");
	                      checkIn.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
	                      checkIn.append("&amp;uiType=");
	                      checkIn.append(XSSUtil.encodeForJavaScript(context, uiType));
	                      checkIn.append("&amp;table=");
	                      checkIn.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
	                      checkIn.append("&amp;showFormat=false&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=false&amp;noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate&amp;refreshTableContent=true','730','450'");
	                      newURL+="<a href=\"javascript:processModalDialog(" + msfRequestData + "," + checkIn.toString()+");\"><img style=\"border:0; padding: 2px;\" src=\"../common/images/iconActionCheckIn.gif\" alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>";
	    			  } else {
						  checkIn.append("'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
						  checkIn.append(XSSUtil.encodeForJavaScript(context, documentId));
						  checkIn.append("&amp;folderId=");
						  checkIn.append("null");
						  checkIn.append("&amp;customSortColumns=");     //Added for Bug #371651 starts
						  checkIn.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
						  checkIn.append("&amp;customSortDirections=");
						  checkIn.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
						  checkIn.append("&amp;uiType=");
						  checkIn.append(XSSUtil.encodeForJavaScript(context, uiType));
						  checkIn.append("&amp;table=");
						  checkIn.append(XSSUtil.encodeForJavaScript(context, table));                 //Added for Bug #371651 ends
						  checkIn.append("&amp;override=false&amp;showFormat=false&amp;showComments=required&amp;objectAction=checkinVCFile&amp;allowFileNameChange=true&amp;noOfFiles=1&amp;JPOName=emxVCDocument&amp;methodName=checkinUpdate&amp;refreshTableContent=true','730','450'");
						  newURL+="<a href=\"javascript:processModalDialog(" + msfRequestData + "," + checkIn.toString()+");\"><img style=\"border:0; padding: 2px;\" src=\"../common/images/iconActionCheckIn.gif\" alt=\""+sTipCheckin+"\" title=\""+sTipCheckin+"\"></img></a>";
	                     }
	    		  }
	    		  if(contentObjectMap.containsKey(CommonDocument.SELECT_ACTIVE_FILE_LOCKED))
	    		  {
	    		  //can unlock document 
		          boolean fileLocked = ((StringList)contentObjectMap.get(CommonDocument.SELECT_ACTIVE_FILE_LOCKED)).contains("FALSE");
		          if(!fileLocked && fileCount<=1  && ("true".equalsIgnoreCase((String)contentObjectMap.get(CommonDocument.SELECT_HAS_UNLOCK_ACCESS)) || canCheckin)){
		        	  if( !isprinterFriendly){
	            		  StringBuffer strngBuf = new StringBuffer(1256);
	                      strngBuf.append("'../teamcentral/emxTeamUnlockDocument.jsp?docId=");
	                      strngBuf.append(XSSUtil.encodeForJavaScript(context, documentId));
	                      strngBuf.append("&amp;customSortColumns=");
	                      strngBuf.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
	                      strngBuf.append("&amp;customSortDirections=");
	                      strngBuf.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
	                      strngBuf.append("&amp;uiType=");
	                      strngBuf.append(XSSUtil.encodeForJavaScript(context, uiType));
	                      strngBuf.append("&amp;table=");
	                      strngBuf.append(XSSUtil.encodeForJavaScript(context, table));
	                      strngBuf.append("'");// Modified for Bug #371651 ends
	                      newURL+="<a href=\"javascript:submitWithCSRF("+strngBuf.toString()+", findFrame(getTopWindow(),'hiddenFrame'));\"><img style=\"border:0; padding: 2px;\" src=\"../common/images/iconActionUnlock.gif\" alt=\""+docLocker+"\" title=\""+docLocker+"\"></img></a>";
	                    }else{
						newURL+="<img style=\"border:0; padding: 2px;\" src=\"../common/images/iconActionUnlock.png\" alt=\""+docLocker+"\" title=\""+docLocker+"\"></img>";
					}
	    		  }
	          }
	          // Can Add Files
	          if(CommonDocument.canAddFiles(context, contentObjectMap)){
				  // MSF
				  msfRequestData = "{RequestType: 'AddFiles', DocumentID: '" + documentId + "'}";
				  // MSF
	    		  StringBuffer canAddFiles = new StringBuffer(1256);
		    		  canAddFiles.append("'../components/emxCommonDocumentPreCheckin.jsp?objectId=");
		    		  canAddFiles.append(XSSUtil.encodeForJavaScript(context, documentId));
		    		  canAddFiles.append("&amp;folderId=");
		    		  canAddFiles.append("null");
		    		  canAddFiles.append("&amp;customSortColumns=");       //Added for Bug #371651 starts
		    		  canAddFiles.append(XSSUtil.encodeForJavaScript(context, customSortColumns));
		    		  canAddFiles.append("&amp;customSortDirections=");
		    		  canAddFiles.append(XSSUtil.encodeForJavaScript(context, customSortDirections));
		    		  canAddFiles.append("&amp;uiType=");
		    		  canAddFiles.append(XSSUtil.encodeForJavaScript(context, uiType));
		    		  canAddFiles.append("&amp;table=");
		    		  canAddFiles.append(XSSUtil.encodeForJavaScript(context, table));                   //Added for Bug #371651 ends
		    		  canAddFiles.append("&amp;showFormat=true&amp;showDescription=required&amp;objectAction=checkin&amp;showTitle=true&amp;JPOName=emxTeamDocumentBase&amp;appDir=teamcentral&amp;appProcessPage=emxTeamPostCheckinProcess.jsp&amp;refreshTableContent=true','730','450'");
	                    newURL+="<a href=\"javascript:processModalDialog(" + msfRequestData + "," + canAddFiles.toString()+");\"><img style=\"border:0; padding: 2px;\" src=\"../common/images/iconActionAppend.gif\" alt=\""+sTipAddFiles+"\" title=\""+sTipAddFiles+"\"></img></a>";
	    	  
	          }
          }
         
        }
          vActions.add(newURL);
    } 
    } catch(Exception ex){
		ex.printStackTrace();
      throw ex;
    }
    finally{
     return vActions;
    }
  }

  // The following method are called internally in the main methods.

 /**
   * CheckActionBarLinkAccess - global method for checking the conditions for top and bottom action bar links
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since V10 Pacth1
   */
  private static boolean checkActionBarLinkAccess(Context context, String args[]) throws Exception
  {
    HashMap programMap         = (HashMap) JPO.unpackArgs(args);
    String objectId            = (String) programMap.get("objectId");

    String  workspaceId = null;
    boolean boolChecking = false;
    boolean hasProject = false;
    DomainObject BaseObject   = DomainObject.newInstance(context,objectId);
    BaseObject.open(context);
    StringList  selects = new StringList(2);
			    selects.add(DomainConstants.SELECT_TYPE);
			    selects.add(DomainConstants.SELECT_CURRENT);
    Map baseTypeMap = BaseObject.getInfo(context, selects);
    String baseType = (String) baseTypeMap.get(DomainConstants.SELECT_TYPE);
    String routeState = (String) baseTypeMap.get(DomainConstants.SELECT_CURRENT);
    String stateComplete=FrameworkUtil.lookupStateName(context,DomainConstants.POLICY_ROUTE,"state_Complete");

    // if workspaceId is not passed, get the workspaceId
    if (baseType.equals(DomainConstants.TYPE_WORKSPACE_VAULT)) {
       workspaceId = getWorkspaceId(context,objectId);
    }
    if(baseType.equals(DomainConstants.TYPE_ROUTE)) {
      String selectWorkspaceId = "from["+DomainConstants.RELATIONSHIP_MEMBER_ROUTE+"].to.to["+DomainConstants.RELATIONSHIP_PROJECT_MEMBERS+"].from.id";
      workspaceId = BaseObject.getInfo(context,selectWorkspaceId);
    }
    if(!(null == workspaceId || "#DENIED!".equals(workspaceId) || "".equals(workspaceId) || "null".equals(workspaceId))) {
      hasProject = true;
    }
    if(hasProject && !routeState.equalsIgnoreCase(stateComplete)){
         boolChecking = true;
    }
   return boolChecking;
  }


  // The following methods are called for Topaction bars display.
  /**
   * hasWorkspaceAndFileUpload - to check whether to display the Add Workspace Content and Upload External File.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public static boolean hasWorkspaceAndFileUpload(Context context, String args[]) throws Exception
  {
    HashMap programMap         = (HashMap) JPO.unpackArgs(args);
    String folderObjectId            = (String) programMap.get("objectId");
    boolean boolChecking = false;
    DomainObject folder   = DomainObject.newInstance(context, folderObjectId);
    Access access = folder.getAccessMask(context);
    if(checkActionBarLinkAccess(context,args)) {
      boolChecking = AccessUtil.hasAddAccess(access);
    }
   return boolChecking;
  }

  /**
   * isVCCommandsEnabled - This method is used to determine if the VC Commands in the Attachments
   *                       Summary can be enabled or not.
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - object Id
   * @return boolean
   * @throws Exception if the operation fails
   * @since Sourcing 11-0
   */
  public boolean isVCCommandsEnabled(Context context, String[] args) throws Exception
  {
    boolean access = false;
    emxVCDocumentUI_mxJPO vcDocUI = new emxVCDocumentUI_mxJPO(context, null);
	boolean dsServer = vcDocUI.hasDesignSyncServer(context,args);
	access = dsServer && hasWorkspaceAndFileUpload(context,args);
	
	return access;

  }
  /**
   * hasMultipleFiles - to check whether to display the Multiple Upload File and Multiple Update File.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public static boolean hasMultipleFiles(Context context, String args[]) throws Exception
  {
    boolean boolChecking = false;
    HashMap programMap         = (HashMap) JPO.unpackArgs(args);
    String objectId            = (String) programMap.get("objectId");
    DomainObject BaseObject   = DomainObject.newInstance(context,objectId);
    String baseType = BaseObject.getInfo(context, BaseObject.SELECT_TYPE);
    Access access = BaseObject.getAccessMask(context);
    if(checkActionBarLinkAccess(context,args))
    {
      if(AccessUtil.hasAddAccess(access)){
        if(!baseType.equals(DomainConstants.TYPE_ROUTE)){
          boolChecking= true;
        }
      }
    }
   return boolChecking;
  }

  /**
   * isJTEnabled - to check whether to display the Link UploadExternalJTFile.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public static boolean isJTEnabled(Context context, String args[]) throws Exception
  {
    boolean boolChecking = false;
    HashMap programMap         = (HashMap) JPO.unpackArgs(args);
    String objectId            = (String) programMap.get("objectId");
    DomainObject BaseObject   = DomainObject.newInstance(context,objectId);
    String baseType = BaseObject.getInfo(context, BaseObject.SELECT_TYPE);
    String strEAIVismarkViewerEnabled=EnoviaResourceBundle.getProperty(context,"emxTeamCentral.EAIVismarkViewerEnabled");
    Access access = BaseObject.getAccessMask(context);
    if(checkActionBarLinkAccess(context,args))
    {
      if(AccessUtil.hasAddAccess(access)){
        if( strEAIVismarkViewerEnabled != null && "true".equals(strEAIVismarkViewerEnabled)){
          boolChecking= true;
        }
      }
    }
   return boolChecking;
  }

  /**
   * hasRemoveContent - to check whether to display the Link Remove Selected.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public static boolean hasRemoveContent(Context context, String args[]) throws Exception
  {
      HashMap programMap         = (HashMap) JPO.unpackArgs(args);
      String objectId            = (String) programMap.get("objectId");
      com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
      boolean boolChecking = false;
      DomainObject BaseObject   = DomainObject.newInstance(context,objectId);
      BaseObject.open(context); // Need to open and close the  Domain Object as we need o get the owner Name.
      String routeState = BaseObject.getInfo(context, BaseObject.SELECT_CURRENT);
      String sOwner     = BaseObject.getOwner(context).getName();
      BaseObject.close(context);
      person = person.getPerson(context);
      String strPerson = person.getName();
      String stateComplete=FrameworkUtil.lookupStateName(context,DomainConstants.POLICY_ROUTE,"state_Complete");
      Access access = BaseObject.getAccessMask(context);
      if(sOwner.equals(strPerson) || (!routeState.equalsIgnoreCase(stateComplete))) {
        if(AccessUtil.hasRemoveAccess(access)){
          boolChecking = true;
        }
      }
     return boolChecking;
  }

  /**
   * hasMoveContent - to check whether to display the Link Move Selected.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public static boolean hasMoveContent(Context context, String args[]) throws Exception
  {
      HashMap programMap         = (HashMap) JPO.unpackArgs(args);
      String objectId            = (String) programMap.get("objectId");
      boolean boolChecking = false;
      DomainObject BaseObject   = DomainObject.newInstance(context,objectId);
      String baseType = BaseObject.getInfo(context, BaseObject.SELECT_TYPE);
      if(hasRemoveContent(context,args) && baseType.equals(DomainConstants.TYPE_WORKSPACE_VAULT)) {
          boolChecking = true;
      }
     return boolChecking;
  }

  /**
   * hasEditBlockLifeCycle - to check whether to display the Link EditLifeCycle Blocks.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public static boolean hasEditBlockLifeCycle(Context context, String args[]) throws Exception
  {
      HashMap programMap         = (HashMap) JPO.unpackArgs(args);
      String objectId            = (String) programMap.get("objectId");
      boolean boolChecking = false;
      DomainObject BaseObject   = DomainObject.newInstance(context,objectId);
      String baseType = BaseObject.getInfo(context, BaseObject.SELECT_TYPE);
      if(checkActionBarLinkAccess(context,args) && baseType.equals(DomainConstants.TYPE_ROUTE) && (context.getUser().equals(BaseObject.getInfo(context, BaseObject.SELECT_OWNER)))) {
          boolChecking = true;
      }
     return boolChecking;
  }

  /**
   * hasRouteSelected - to check whether to display the Link RouteSelected .
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */

  public static boolean hasRouteSelected(Context context, String args[]) throws Exception
  {
      HashMap programMap         = (HashMap) JPO.unpackArgs(args);
      String objectId            = (String) programMap.get("objectId");
      com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
      person = person.getPerson(context);
      boolean boolChecking = false;
      String workspaceId =null;
      DomainObject BaseObject   = DomainObject.newInstance(context,objectId);
      String baseType = BaseObject.getInfo(context, BaseObject.SELECT_TYPE);
      if (baseType.equals(DomainConstants.TYPE_WORKSPACE_VAULT)) {
         workspaceId = getWorkspaceId(context,objectId);
      }
      if(baseType.equals(DomainConstants.TYPE_ROUTE)) {
        String selectWorkspaceId = "from["+DomainConstants.RELATIONSHIP_MEMBER_ROUTE+"].to.to["+DomainConstants.RELATIONSHIP_PROJECT_MEMBERS+"].from.id";
        workspaceId = BaseObject.getInfo(context,selectWorkspaceId);
      }
      String sCreateRoute = "";
      StringList objectSelects = new StringList();
      objectSelects.add("attribute["+DomainConstants.ATTRIBUTE_CREATE_ROUTE+"]");
      String objectWhere = "(to["+BaseObject.RELATIONSHIP_PROJECT_MEMBERS+"].from.id == '"+workspaceId+"')";
      MapList mapList = person.getRelatedObjects(context,
                                         BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP,
                                         BaseObject.TYPE_PROJECT_MEMBER,
                                         false,
                                         true,
                                         (short)1,
                                         objectSelects,
                                         null,
                                         objectWhere,
                                         null,
                                         0,
                                         "",
                                         "",
                                         null);
      Iterator memberItr  = mapList.iterator();
      Map map=null;
      if(memberItr.hasNext()){
        map =(Map)memberItr.next();
        sCreateRoute = (String)map.get("attribute["+DomainConstants.ATTRIBUTE_CREATE_ROUTE+"]");
      }

      if(checkActionBarLinkAccess(context,args) && baseType.equals(DomainConstants.TYPE_WORKSPACE_VAULT) && "Yes".equals(sCreateRoute)) {
          boolChecking = true;
      }

    return boolChecking;
  }

  /**
   * getWorkspaceId - this Method will be called Internally by Other methods to get the Project Id by passing the Document Id.
   * @param context the eMatrix <code>Context</code> object
   * @param folderId  The Object id of the Document
   * @return String type
   * @throws Exception detailed in the log file if the operation fails
   * @since Team 10-0-1-0
   */

public static String getWorkspaceId(Context context, String  folderId)
{
      String workspaceId="";
      try{
        String strProjectVault = DomainConstants.RELATIONSHIP_PROJECT_VAULTS;
        String strSubVaultsRel = DomainConstants.RELATIONSHIP_SUB_VAULTS;
        String strProjectType  = DomainConstants.TYPE_PROJECT;
        String strProjectVaultType  = DomainConstants.TYPE_PROJECT_VAULT;
        DomainObject domainObject = DomainObject.newInstance(context);
        domainObject.setId(folderId);
        Pattern relPattern  = new Pattern(strProjectVault);
        relPattern.addPattern(strSubVaultsRel);
        Pattern typePattern = new Pattern(strProjectType);
        typePattern.addPattern(strProjectVaultType);

        Pattern includeTypePattern = new Pattern(strProjectType);

        StringList objSelects = new StringList();
        objSelects.addElement(domainObject.SELECT_ID);
        //need to include Type as a selectable if we need to filter by Type
        objSelects.addElement(domainObject.SELECT_TYPE);
        MapList mapList = domainObject.getRelatedObjects(context,
                                               relPattern.getPattern(),
                                               typePattern.getPattern(),
                                               objSelects,
                                               null,
                                               true,
                                               false,
                                               (short)0,
                                               "",
                                               "",
                                               0,
                                               includeTypePattern,
                                               null,
                                               null);

        Iterator mapItr = mapList.iterator();
        while(mapItr.hasNext())
        {
            Map map = (Map)mapItr.next();
            workspaceId = (String) map.get(domainObject.SELECT_ID);
        }
      }catch(Exception e) {
    	  e.printStackTrace();
      }

      return workspaceId;
   }

   protected static String removeSpace(String formatStr) {

       int flag = 1;
       int strLength = 0;
       int index = 0;

       while  (flag != 0)  {
         strLength = formatStr.length();
         index = 0;
         index = formatStr.indexOf(' ');
         if (index == -1) {
           flag = 0;
           break;
         }

         String tempStr1 = formatStr.substring(0,index);
         String tempStr2 = formatStr.substring(index+1,strLength);
         formatStr = tempStr1 + tempStr2;
       }
       return formatStr;
   }

  /**
    *  This function gets the Icon file name for any given type
    *  from the emxSystem.properties file
    *
    * @param context  the eMatrix <code>Context</code> object
    * @param type     object type name
    * @return         String - icon name
    * @since          TC 10-6
    */

    public static String getTypeIconProperty(Context context, String type)
    {
        String icon = DomainConstants.EMPTY_STRING;
        String typeRegistered = DomainConstants.EMPTY_STRING;

        try
        {
            if (type != null && type.length() > 0 )
            {
                String propertyKey = DomainConstants.EMPTY_STRING;
                String propertyKeyPrefix = "emxFramework.smallIcon.";
                String defaultPropertyKey = "emxFramework.smallIcon.defaultType";

                // Get the symbolic name for the type passed in
                typeRegistered = FrameworkUtil.getAliasForAdmin(context, "type", type, true);

                if (typeRegistered != null && typeRegistered.length() > 0 )
                {
                    propertyKey = propertyKeyPrefix + typeRegistered.trim();

                    try {
                        icon = EnoviaResourceBundle.getProperty(context,propertyKey);
                    } catch (Exception e1) {
                        icon = DomainConstants.EMPTY_STRING;
                    }
                    if( icon == null || icon.length() == 0 )
                    {
                        // Get the parent types' icon
                        BusinessType busType = new BusinessType(type, context.getVault());
                        if (busType != null)
                        {
                            String parentBusType = busType.getParent(context);
                            if (parentBusType != null)
                                icon = getTypeIconProperty(context, parentBusType);
                        }

                        // If no icons found, return a default icon for propery file.
                        if (icon == null || icon.trim().length() == 0 )
                            icon = EnoviaResourceBundle.getProperty(context,defaultPropertyKey);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error getting type icon name : " + ex.toString());
        }

        return icon;
    }

    public static String getParentTypeIconProperty(Context context, String type)
    {
        String icon = DomainConstants.EMPTY_STRING;
        String typeRegistered = DomainConstants.EMPTY_STRING;

        try
        {
            if (type != null && type.length() > 0 )
            {
                String propertyKey = DomainConstants.EMPTY_STRING;
                String propertyKeyPrefix = "emxFramework.smallIcon.";

                // Get the symbolic name for the type passed in
                typeRegistered = FrameworkUtil.getAliasForAdmin(context, "type", type, true);

                if (typeRegistered != null && typeRegistered.length() > 0 )
                {
                    propertyKey = propertyKeyPrefix + typeRegistered.trim();

                    icon = EnoviaResourceBundle.getProperty(context,propertyKey);
                    
                    if( icon == null || icon.length() == 0 )
                    {
                        // Get the parent types' icon
                        BusinessType busType = new BusinessType(type, context.getVault());
                        if (busType != null)
                        {
                            String parentBusType = busType.getParent(context);
                            if (parentBusType != null)
                                icon = getTypeIconProperty(context, parentBusType);
                        }

                        // If no icons found, return a default icon for propery file.
                        if (icon == null || icon.trim().length() == 0 )
                            icon = "iconSmall"+removeSpace(type)+".gif";
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error getting type icon name : " + ex.toString());
        }

        return icon;
    }


  /**
   * getFolderContentIds - This method is used to get content ids in the folder
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        objectId is in the paramList
   * Due to performance reasons when used in a configurable table, this method return only folder content ids.
   * @return MapList
   * @throws Exception if the operation fails
   * @since Team 10-7-SP1
   */
   @com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getFolderContentIds(Context context, String[] args) throws Exception {
	   MapList contentMapList = new MapList();
	   Map programMap = (HashMap) JPO.unpackArgs(args);
	   String folderObjectId = (String)programMap.get("objectId");
	   DomainObject domainObject = DomainObject.newInstance(context,folderObjectId);

	   StringList  selectTypeStmts = new StringList(3);
	   				selectTypeStmts.add(DomainConstants.SELECT_ID);
	   				selectTypeStmts.add(DomainConstants.SELECT_TYPE);
	   				selectTypeStmts.add(DomainConstants.SELECT_NAME);
					selectTypeStmts.add(DomainConstants.SELECT_CURRENT);
			 
					
        String relationshipPattern = DomainObject.RELATIONSHIP_VAULTED_DOCUMENTS+","+ DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2;
		//Removed "&& revision ~~ last" for bug 352726 in the where clause
		contentMapList = domainObject.getRelatedObjects(context,
		                                                 relationshipPattern,
		                                                 "*",
		                                                 selectTypeStmts,
		                                                 null,
		                                                 false,
		                                                 true,
		                                                 (short)1,
		                                                 "current.access[read] == TRUE",
		                                                 null,
		                                                 0,
		                                                 null,
		                                                 null,
		                                                 null);

		return contentMapList;
  }

  /**
   * hasMoveAccess - to check whether to display the Link Move Selected.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the objectId
   * @return boolean type
   * @throws Exception if the operation fails
   * @since Team 10-0-1-0
   */
  public static boolean hasMoveAccess(Context context, String args[]) throws Exception
  {
      HashMap programMap         = (HashMap) JPO.unpackArgs(args);
      String objectId            = (String) programMap.get("objectId");
      boolean boolChecking = false;

      DomainObject BaseObject   = DomainObject.newInstance(context,objectId);
      String baseType = BaseObject.getInfo(context, BaseObject.SELECT_TYPE);
      if( hasRemoveContent(context,args) && baseType.equals(DomainConstants.TYPE_WORKSPACE_VAULT)) {
          boolChecking = true;
      }
     return boolChecking;
  }

  /**
   * getNewWindowIcon - to display the new window icon using programHTMLOutput in table TMCContentSummary (type_TMCContentSummary)
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the programMap
   * @return Vector type
   * @throws Exception if the operation fails
   * @since Team V6R2010
   */
  public Vector getNewWindowIcon(Context context, String[] args) throws Exception {

	  Vector vecShowNewWindowIcon  = new Vector();
	  HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList)programMap.get("objectList");
      Map paramList = (Map)programMap.get("paramList");

      StringBuffer prefixLinkBuffer = new StringBuffer();
      prefixLinkBuffer.append("'../common/emxTree.jsp?mode=insert");

      StringBuffer tempLinkBuffer = new StringBuffer();
      tempLinkBuffer.append("&amp;relId=");
      tempLinkBuffer.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("relId")));
      tempLinkBuffer.append("&amp;parentOID=");
      tempLinkBuffer.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("parentOID")));
      tempLinkBuffer.append("&amp;jsTreeID=");
      tempLinkBuffer.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("jsTreeID")));
      tempLinkBuffer.append("&amp;objectId=");
      String sContextType = "";
      String docType = "";
      //Bug 318463. End: Added above variables.

      Iterator objectListItr = objectList.iterator();
      while(objectListItr.hasNext())
      {
    	  String newURL = "";
    	  String strTreeName = "type_DOCUMENTS";
    	  Map objectMap = (Map) objectListItr.next();
    	  docType = (String)objectMap.get(DomainConstants.SELECT_TYPE);

    	  // Get the treeMenu of the Type.
    	  if (docType.equals(DomainConstants.TYPE_DOCUMENT)) {
    		  strTreeName = "TMCtype_Document";
    	  } else if (docType.equals(DomainConstants.TYPE_PACKAGE) ) {
    		  strTreeName = "TMCtype_Package";
    	  } else if (docType.equals(DomainConstants.TYPE_RFQ) ) {
    		  strTreeName = "TMCtype_RequestToSupplier";
    	  } else if (docType.equals(DomainConstants.TYPE_RTS_QUOTATION) ) {
    		  strTreeName = "TMCtype_RTSQuotation";
    	  } else {
    		  strTreeName = "";
    	  }

    	  StringBuffer    finalURL = new StringBuffer();
				    	  finalURL.append(prefixLinkBuffer.toString());
				    	  finalURL.append(tempLinkBuffer.toString());
				    	  finalURL.append(XSSUtil.encodeForJavaScript(context, (String)objectMap.get(DomainConstants.SELECT_ID)));
				    	  finalURL.append("&amp;treeMenu=");
				    	  finalURL.append(strTreeName);

    	  if(strTreeName.length() != 0){
    		  finalURL.append("&amp;suiteKey=");
    		  finalURL.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("suiteKey")));
    		  finalURL.append("&amp;emxSuiteDirectory=");
    		  finalURL.append(XSSUtil.encodeForJavaScript(context, (String)paramList.get("SuiteDirectory")));
    	  }

        newURL += "<a href=\"javascript:emxTableColumnLinkClick("+finalURL.toString()+"', '875', '550', 'false', 'popup', '');\"><img border=\"0\" src=\"images/iconNewWindow.gif\"></img></a>";

        vecShowNewWindowIcon.add(newURL);
      }
      return vecShowNewWindowIcon;
}
   
}

