/*
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 */
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.CommonImageConverterRemoteExec;
import com.matrixone.apps.common.SubscriptionManager;
import com.matrixone.apps.common.Workspace;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.common.util.ImageManagerUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.fileupload.FileItem;

import matrix.db.Access;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;

public class emxDnDBase_mxJPO {

    public emxDnDBase_mxJPO(Context context, String[] args) throws Exception {}

    // Image Column providing check in capabilities by Drag&Drop
    public Vector columnImage(Context context, String[] args) throws Exception {

        Vector vResult      = new Vector();
        Map paramMap        = (Map) JPO.unpackArgs(args);
        Map paramList       = (Map)paramMap.get("paramList");
        MapList mlObjects   = (MapList) paramMap.get("objectList");
        String sMCSURL      = emxUtil_mxJPO.getMCSURL(context, args);                
        String sLang        = (String)paramList.get("languageStr");

        String sLabel        = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.DropImagesHere", sLang);        
        String sLabelNoImage = EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.String.NoImage", sLang);  
		boolean useInPlaceManager = false;
		try{
			useInPlaceManager = "true".equalsIgnoreCase(EnoviaResourceBundle.getProperty(context, "emxFramework.InPlaceImageManager")); 
		}catch(Exception e){
		}
		boolean excluded = false;
		StringList contentTypesList = new StringList();
		try{
			String excludedTypes = EnoviaResourceBundle.getProperty(context, "emxFramework.InPlaceImageManager.TypeExclusionList"); 
            contentTypesList = FrameworkUtil.split(excludedTypes, ",");
		}catch(Exception e){
		}
        
        if (mlObjects.size() > 0) {
        
            for (int i = 0; i < mlObjects.size(); i++) {
            
                StringBuilder sbResult  = new StringBuilder();
                Map mObject             = (Map) mlObjects.get(i);                
                String sOID             = (String) mObject.get("id");
                String objectType = (String)mObject.get("type");
				String symbolicType = FrameworkUtil.getAliasForAdmin(context, "type", objectType, true);
                if(contentTypesList.indexOf(symbolicType) > -1)
                {
                	excluded = true;
                }
				else{
                	excluded = false;
				}
                DomainObject dObject    = new DomainObject(sOID);                
                String sURLImage        = emxUtil_mxJPO.getPrimaryImageURL(context, args, sOID, "mxThumbnail Image", sMCSURL, "NoImageFound");                 
                Access access           = dObject.getAccessMask(context);
                Boolean bAccess         = access.hasFromConnectAccess();                                            
                
                String sLevel           = (String) mObject.get("id[level]");
                String sFormId          = "formDrag"        + i + sLevel;
                String sDivId           = "divDrop"         + i + sLevel;
                String sDivIdImage      = "divDropImge"     + i + sLevel;
                        
                if(sURLImage.equals("NoImageFound")) {
                    if(bAccess && !excluded) {
                        sbResult.append("<form class='dropAreaInColumn' id='").append(sFormId).append("' action=\"../common/emxExtendedPageHeaderFileUploadImage.jsp?objectId=").append(sOID).append("\"  method='post'  enctype='multipart/form-data'>");
                        sbResult.append("<div id='").append(sDivId).append("' class='dropArea'");
                        if(useInPlaceManager){
                        	sbResult.append(" onClick=\"launchImageManager('" + sOID + "');\"");
                        }
                        sbResult.append("       ondrop=\"ImageDropColumn(event, '").append(sFormId).append("', '").append(sDivId).append("', '").append(sLevel).append("')\" ");
                        sbResult.append("   ondragover=\"ImageDragHover(event, '").append(sDivId).append("')\" ");
                        sbResult.append("  ondragleave=\"ImageDragHover(event, '").append(sDivId).append("')\">");                                
                        sbResult.append(sLabel);
                        sbResult.append("   </div>");
                        sbResult.append("</form>");  
                    } else {
                        sbResult.append("<div class='dropArea' style='border:none;white-space:normal;'>");
                        sbResult.append(sLabelNoImage);
                        sbResult.append("</div>");
                    }   
                } else {
                    if(bAccess && !excluded) {
                        sbResult.append("<form class='dropAreaInColumn' id='").append(sFormId).append("' action=\"../common/emxExtendedPageHeaderFileUploadImage.jsp?objectId=").append(sOID).append("\"  method='post'  enctype='multipart/form-data'>");
                        sbResult.append("<div id='").append(sDivId).append("' class='dropAreaWithImage' ");
                        sbResult.append("        ondrop=\"ImageDropOnImageColumn(event, '").append(sFormId).append("', '").append(sDivId).append("', '").append(sDivIdImage).append("', '").append(sLevel).append("')\" ");
                        sbResult.append("    ondragover=\"ImageDragHoverWithImage(event, '").append(sDivId).append("', '").append(sDivIdImage).append("')\" ");
                        sbResult.append("   ondragleave=\"ImageDragHoverWithImage(event, '").append(sDivId).append("', '").append(sDivIdImage).append("')\">");                                
                        sbResult.append("<img class='dropAreaImage' id='").append(sDivIdImage).append("' src='").append(sURLImage).append("' ");
                        if(useInPlaceManager){
                        	sbResult.append(" onClick=\"launchImageManager('" + sOID + "');\"");
                        }else{
	                        sbResult.append(" onClick=\"var posLeft=(screen.width/2)-(900/2);var posTop = (screen.height/2)-(650/2);");
	                        sbResult.append("window.open('../components/emxImageManager.jsp?isPopup=false&amp;toolbar=APPImageManagerToolBar&amp;header=emxComponents.Image.ImageManagerHeading&amp;HelpMarker=emxhelpimagesview&amp;");
	                        sbResult.append("objectId=").append(sOID).append("', '', 'height=650,width=850,top=' + posTop + ',left=' + posLeft + ',toolbar=no,directories=no,status=no,menubar=no;return false;')\"");
                        }
                        sbResult.append(" />");
                        sbResult.append("   </div>");
                        sbResult.append("</form>");                    
                    } else {
                        sbResult.append("<img class='dropAreaImage' id='").append(sDivIdImage).append("' src='").append(sURLImage).append("' ");
                    	if(!excluded){
	                        if(useInPlaceManager){
	                        	sbResult.append(" onClick=\"launchImageManager('" + sOID + "');\"");
	                        }else{
		                        sbResult.append(" onClick=\"var posLeft=(screen.width/2)-(850/2);var posTop = (screen.height/2)-(650/2);");
		                        sbResult.append("window.open('../components/emxImageManager.jsp?isPopup=false&amp;toolbar=APPImageManagerToolBar&amp;header=emxComponents.Image.ImageManagerHeading&amp;HelpMarker=emxhelpimagesview&amp;");
		                        sbResult.append("objectId=").append(sOID).append("', '', 'height=650,width=850,top=' + posTop + ',left=' + posLeft + ',toolbar=no,directories=no,status=no,menubar=no;return false;')\"");
	                        }
                    	}
                        sbResult.append(" />");                        
                    }
                }
                vResult.add(sbResult.toString());
            }
        }

        return vResult;

    }       
    public String checkInImage(Context context, String[] args) throws Exception {
    	
    	try {	    	
	        Map paramMap = (Map) JPO.unpackArgs(args);
	        String sLanguage = (String) paramMap.get("language");
	        String sOIDParent = (String) paramMap.get("objectId");
	        List files = (List) paramMap.get("files"); 
	        String sFolder = (String) paramMap.get("folder");
	        String sMCSURL = (String) paramMap.get("MCSURL");        
	        String imageRelType = (String) paramMap.get("relationship");
	        	
	        DomainObject imageHolder = new DomainObject();
	        DomainObject doParent = new DomainObject(sOIDParent);
	        String sIsProductLine = doParent.getInfo(context, "type.kindof["+DomainConstants.TYPE_PRODUCTLINE+"]");
	        String sIsModel = doParent.getInfo(context, "type.kindof["+PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_Model)+"]");
	        String sIsProducts = doParent.getInfo(context, "type.kindof["+PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_Products)+"]");
	        String sOIDImageHolder = doParent.getInfo(context, "to["+ PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_ImageHolder) +"].from.id"); 
	            
	        Iterator iter = files.iterator();
	        int index;
	    	String sFilename="";
	    	FileItem file = null;
	    	File outfile = null;
	    	ContextUtil.startTransaction(context, true);
	        while (iter.hasNext())
	        {
	        	file = (FileItem) iter.next();
	            sFilename 		= file.getName();        
	            if(sFilename.contains("/")) {
	        		index = sFilename.lastIndexOf("/");
	        		sFilename = sFilename.substring(index);
	        	}
	        	if(sFilename.contains("\\")) {
	        		index = sFilename.lastIndexOf("\\");
	        		sFilename = sFilename.substring(index+1);
	        	}        
	            outfile = new File(sFolder +  sFilename);
	        	file.write(outfile);
	        
		        //if(sIsProductLine.equalsIgnoreCase("TRUE") || sIsModel.equalsIgnoreCase("TRUE")|| sIsProducts.equalsIgnoreCase("TRUE")) {
		        if(DomainConstants.RELATIONSHIP_IMAGES.equals(imageRelType)){
		            String sName = DomainObject.getAutoGeneratedName(context, "type_Image", "");              
		            imageHolder.createObject(context, DomainConstants.TYPE_IMAGE, sName, "-", DomainConstants.POLICY_IMAGE, context.getVault().getName());
		            imageHolder.addRelatedObject(context, new RelationshipType(DomainConstants.RELATIONSHIP_IMAGES), true, sOIDParent);
		            sOIDImageHolder = imageHolder.getInfo(context, DomainConstants.SELECT_ID);
		            String sHasPrimaryImage = doParent.getInfo(context, "from["+ DomainConstants.RELATIONSHIP_PRIMARY_IMAGE + "]");
		              
		            if(sHasPrimaryImage.equalsIgnoreCase("FALSE")) {
		                imageHolder.addRelatedObject(context, new RelationshipType(DomainConstants.RELATIONSHIP_PRIMARY_IMAGE), true, sOIDParent);		
		            }
		        } else {                          
		        	String typeImageHolder = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_ImageHolder);
		        	String relImageHolder = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_ImageHolder);
		        	String attrPrimayImage = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_attribute_PrimaryImage);
		        	
		            if(sOIDImageHolder == null) {        
		                imageHolder = DomainObject.newInstance(context, typeImageHolder);
		                imageHolder.createAndConnect(context, typeImageHolder, relImageHolder, doParent, false);       
		                imageHolder.setAttributeValue(context, attrPrimayImage, ImageManagerUtil.getPrimaryImageFileNameForImageManager(context, sFilename));
		                sOIDImageHolder = imageHolder.getInfo(context, DomainConstants.SELECT_ID);
		            } else {        
		                imageHolder = new DomainObject(sOIDImageHolder);    
		            }
		        }
	        
		        imageHolder.checkinFile(context, true, true, "", "generic", sFilename, sFolder);      
		               
		        String gotMcsURL = MqlUtil.mqlCommand(context, "get env global MCSURL");
		 
		        List lstFilesToTransform = new ArrayList();        
		        Hashtable htCCIHInfo = new Hashtable();
		        
		        htCCIHInfo.put("Oid"    , sOIDImageHolder   );
		        htCCIHInfo.put("File"   , sFilename         );
		        htCCIHInfo.put("Format" , "generic"         );
		        
		        lstFilesToTransform.add(htCCIHInfo);	    
		        new CommonImageConverterRemoteExec().convertImageAndCheckinSameObject(context, gotMcsURL, lstFilesToTransform, null, ".jpg");
		        outfile.delete();
		    }
	        
	        ContextUtil.commitTransaction(context);
	        return emxExtendedHeader_mxJPO.genHeaderImage(context, args, sOIDParent, sLanguage, sMCSURL,imageRelType, false , "true");
    	}catch (Exception ex) {
                ContextUtil.abortTransaction(context);
                ex.printStackTrace();
                return "ERROR"+ex.getMessage();
        }
    }    
     
    
    // Drop column
    public String checkinFile(Context context, String[] args) throws Exception {

    	try{
	        Map paramMap = (Map) JPO.unpackArgs(args);
	        String sLanguage = (String) paramMap.get("language");
			String timeZone = (String) paramMap.get("timezone");
	        String sOID = (String) paramMap.get("objectId");
	        String sRelType = (String) paramMap.get("relationship");
	        String sFolder  = (String) paramMap.get("folder");
	        List files = (List) paramMap.get("files");        		
	        
	        HashMap uploadParamsMap=new HashMap();
	        String objectAction = (String) paramMap.get("objectAction"); 
	        uploadParamsMap.put("objectAction", objectAction);
	        uploadParamsMap.put("parentId", sOID); 
	        StringList ids=new StringList();
	        
	        String documentCommand  = (String) paramMap.get("documentCommand");        
	        DomainObject dObject    = new DomainObject(sOID);               
	        String strType     = PropertyUtil.getSchemaProperty(context,DomainObject.SYMBOLIC_type_DOCUMENTS);
	        String sIsDocument      = dObject.getInfo(context, "type.kindof["+strType+"]");
	        
	        if(UIUtil.isNullOrEmpty(sRelType)){
	        	sRelType = "Reference Document";
	        }
	        if( UIUtil.isNullOrEmpty(documentCommand)){
	        	documentCommand = "APPReferenceDocumentsTreeCategory";
	        }
	        
	        Iterator iter = files.iterator();
	        int index;
	    	String sFilename="";
	    	FileItem file = null;
	    	File outfile = null;
	    	StringBuffer errorMessage=new StringBuffer();
	    	String errorMsg = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",context.getLocale(),"emxComponents.CommonDocument.DocumentAlreadyExist");
	    	errorMessage.append(errorMsg);
	    	if(isDuplicateFile(context, sOID, files, errorMessage)){
	    		return "ERROR"+errorMessage;
	    	}
	    	ContextUtil.startTransaction(context, true);
	        while (iter.hasNext())
	        {
	        	file = (FileItem) iter.next();
	            sFilename 		= file.getName();        
	            if(sFilename.contains("/")) {
	        		index = sFilename.lastIndexOf("/");
	        		sFilename = sFilename.substring(index);
	        	}
	        	if(sFilename.contains("\\")) {
	        		index = sFilename.lastIndexOf("\\");
	        		sFilename = sFilename.substring(index+1);
	        	}        
	            outfile = new File(sFolder +  sFilename);
	        	file.write(outfile);
	        
		        if(sRelType.equals("Active Version") && sIsDocument.equalsIgnoreCase("TRUE")) {
		            CommonDocument cDoc = new CommonDocument(sOID);
		            cDoc.checkinFile(context, true, true, "", "generic", sFilename, sFolder);
		            cDoc.createVersion(context, sFilename, sFilename, null);
		            
		        } else {
		        	String sObjGeneratorName = UICache.getObjectGenerator(context, "type_Document", "");
		        	String sName = DomainObject.getAutoGeneratedName(context, sObjGeneratorName, "");		       
		            CommonDocument cDoc = new CommonDocument(); 
		            cDoc.createObject(context, DomainObject.TYPE_DOCUMENT, sName, "0", DomainObject.POLICY_DOCUMENT, context.getVault().getName());
		            cDoc.checkinFile(context, true, true, "", "generic", sFilename, sFolder); 
		            cDoc.createVersion(context, sFilename, sFilename, null);
		            cDoc.addRelatedObject(context, new RelationshipType(sRelType), true, sOID);
		            cDoc.setAttributeValue(context, "Title", sFilename);
		            ids.add(cDoc.getId(context));
		        }        
	        
		        outfile.delete();
	        }
	        HashMap params = new HashMap();
	        params.put("uploadParamsMap",uploadParamsMap);
	        params.put("objectIds",ids);
	        String initargs[] = {};
	        JPO.invoke(context, "emxTeamDocumentBase", initargs, "postCheckinDND", JPO.packArgs (params));
	        ContextUtil.commitTransaction(context);        
	        return emxExtendedHeader_mxJPO.genHeaderDocuments(context, sOID,sRelType, documentCommand, sLanguage, false,timeZone);       
        
    	}catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            ex.printStackTrace();
            return "ERROR"+ex.getMessage();
    	}
    }      
    private boolean isDuplicateFile(Context context,String objectID,List checkinFiles,StringBuffer error) throws Exception{
    	DomainObject domainObject = DomainObject.newInstance(context, objectID);
    	if(!(domainObject instanceof CommonDocument)){
    		return false;
    	}
    	StringList files = getFiles(context, domainObject);
    	return isFilePresent(context, files, checkinFiles,error);
    }
    private StringList getFiles(Context context,DomainObject domainObject) throws Exception{
    	CommonDocument object = (CommonDocument)domainObject;
    	StringList selectList = new StringList();
    	selectList.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
        selectList.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
        Map selectMap = object.getInfo(context, selectList);
        StringList selectFileList = new StringList();
        selectFileList.add(CommonDocument.SELECT_TITLE);
        selectFileList.add(CommonDocument.SELECT_LOCKER);
    	return (StringList) selectMap.get(CommonDocument.SELECT_ACTIVE_FILE_VERSION_ID);
    }
    private boolean isFilePresent(Context context,StringList files,List checkinFiles,StringBuffer error) throws Exception{
    	boolean isFileFound = false;
    	if(files!=null && !files.isEmpty()){
	    	Iterator fileItr = files.iterator();
	    	while(fileItr.hasNext()){
	    		Iterator revItr = getRevisionFiles(context,(String)fileItr.next()).iterator();
	    		while(revItr.hasNext()){
	    			Map fileMap = (Map)revItr.next();
	    			if(fileFound(context,fileMap,checkinFiles,error)){
	    				isFileFound=true;
	    			}
	    		}
	    	}
    	}
    	return isFileFound;
    }
    private MapList getRevisionFiles(Context context,String fileId) throws Exception{
    	DomainObject versionObj = DomainObject.newInstance(context, fileId);
    	StringList selectFileList = new StringList();
        selectFileList.add(CommonDocument.SELECT_TITLE);
        selectFileList.add(CommonDocument.SELECT_LOCKER);
        return versionObj.getRevisionsInfo(context,selectFileList,new StringList());
    	
    }
    private boolean fileFound(Context context,Map fileMap,List checkinFiles,StringBuffer error){
    	String fileTitle = (String)fileMap.get(CommonDocument.SELECT_TITLE);
    	Iterator chekingFileItr = checkinFiles.iterator();
    	boolean isFileFound = false;
    	while(chekingFileItr.hasNext()){
    		FileItem file = (FileItem) chekingFileItr.next();
            String sFilename 		= file.getName();
            String[] sFilenamePath=sFilename.split("\\\\");
            sFilename = sFilenamePath[sFilenamePath.length-1].trim();
            if(sFilename.equalsIgnoreCase(fileTitle)){
            	error.append(" \n" + sFilename);
            	isFileFound = true;
            	break;
            }
    	}
    	return isFileFound;
    	
    }
}
