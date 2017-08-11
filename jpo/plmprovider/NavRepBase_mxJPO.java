package jpo.plmprovider;
// NavRepBase.java
//
// Created on Oct 3, 2006
//
// Copyright (c) 2006-2016 by Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectProxy;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.TicketWrapper;

import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.client.fcs.FcsClient;
import com.matrixone.fcs.mcs.Checkout;

import com.matrixone.apps.plmprovider.NavRepRequest;
import com.matrixone.apps.plmprovider.NavRepResponse;
import com.matrixone.apps.plmprovider.NavRepResult;
import com.matrixone.apps.plmprovider.NodeType;
import com.matrixone.apps.plmprovider.PlmProviderUtil;
/**
 * @author bucknam
 *
 * The <code>NavRepBase</code> class provides web services associated with providing
 * graphical representations of a tree structure.
 *
 * @version AEF 10.7.1.0 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class NavRepBase_mxJPO extends jpo.plmprovider.Mat3DLive_mxJPO {

public boolean doNotCheckout;

    /**
     * Default constructor.
     *
     * @since AEF 10.7.1.0
     */

    public NavRepBase_mxJPO()
    {
              doNotCheckout = false;
    }

    /**
     * This function will get FCS urls for all the objects in the request object
     * given as an array of objIds.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request input list of object ids
     * @return NavRepResponse
     * @since AEF 10.7.1.0
     */

    //for rest 
    public NavRepResponse getUrls(Context context, String[] args) throws Exception{
    	Map inputMap = JPO.unpackArgs(args);
    	NavRepRequest req = (NavRepRequest)inputMap.get("request");
    	return getUrls(context,req);
    }
    
   
    
    //for rest
    public NavRepResponse getUrls(Context context, NavRepRequest request)
    {
       
        return getAllUrls(context, request);
    }
    
    //for rest
    public NavRepResponse getStaticUrls(Context context,String args[]) throws Exception{
    	
    	Map inputMap = JPO.unpackArgs(args);
    	NavRepRequest request = (NavRepRequest)inputMap.get("request");
    	
    	
 		doNotCheckout = true;
 		return getUrls(context, request);
  }
    

    //for rest
    protected NavRepResponse getAllUrls(Context context, NavRepRequest request)
    {
    	
        log("NavRep.getAllUrls start");
        NavRepResponse response = new NavRepResponse();

       

        String[] objectIds = request.getObjectIds();

        // image type can now be given as a semicolon separated list of types (eg. "jpg;cgr;")
        String imageType = request.getImageType();
        // if necessary, strip off last semicolon so split works correctly
        if (imageType.endsWith(";"))
        {
            imageType = imageType.substring(0,imageType.length()-1);
        }
        StringList imageTypeList = FrameworkUtil.split(imageType,";");

        try
        {
            String mcsURL = request.getMcsURL();

            // return requested formats for each object id in the list
            NavRepResult[] results = new NavRepResult[imageTypeList.size()*objectIds.length];
            int count = 0;
            StringList objectSelects    = new StringList(1);
            objectSelects.add(DomainConstants.SELECT_TYPE);
            
            //in case of DEC data , separate minor id in the request
            String[] majorIds = new String[objectIds.length];
            String[] minorIds = new String[objectIds.length];
            
            for(int i=0;i<objectIds.length;i++){
                StringList splitObjectId = FrameworkUtil.split(objectIds[i], ":");

                majorIds[i] = (String)splitObjectId.get(0);

                if(splitObjectId.size()>1){
                    minorIds[i] = (String)splitObjectId.get(1);
                }else{
                    minorIds[i] = "";
                }
            }

            MapList mlObjectType        = DomainObject.getInfo(context, objectIds, objectSelects);

            for (int i = 0; i < majorIds.length; i++)
            {
                  String objectId             = majorIds[i];
                Map mObjectType             = (Map)mlObjectType.get(i);
                String domainType           = (String)mObjectType.get(DomainConstants.SELECT_TYPE);
                String symbolicDomainType   = FrameworkUtil.getAliasForAdmin(context, "type", domainType, true);
                NodeType typeNode           = (NodeType)nodeMap.get(symbolicDomainType);
                boolean isPartFromVPLMSync  = isPartFromVPLMSync(context,objectId);

                log("NavRep.getAllUrls objectId :" + objectId);

                try{

                    Iterator itr            = imageTypeList.iterator();
                    while(itr.hasNext())
                    {
                        String imgType      = (String)itr.next();
                        log("NavRep.getAllUrls imageType :" + imgType);
                        if (isPartFromVPLMSync && ! "CATData".equalsIgnoreCase(imgType)) {
                            results[count++]    = getResultForVPLMParts(context, mcsURL, objectId, imgType, typeNode);
                        }else {
                            results[count++]    = getResult(context, mcsURL, objectIds[i], imgType, typeNode);
                        }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace(System.out);
                    log("NavRep.getAllUrls exception " + e);
                }
            }
            response.setResults(results);
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.out);
            log("NavRep.getAllUrls exception " + ex);
        }

        log("NavRep.getAllUrls complete");
        return (response);
    }
    
    
    //for soap
    public NavRepResponse getUrls(String username, String password, NavRepRequest request)
    {
        return getAllUrls(username, password, request);
    }

    /**
     * This function will get FCS urls for all the objects in the request object
     * given as an array of objIds.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request input list of object ids
     * @return NavRepResponse
     * @since AEF 10.7.1.0
     */

    protected NavRepResponse getAllUrls(String username, String password, NavRepRequest request)
    {
        log("NavRep.getAllUrls start");
        NavRepResponse response = new NavRepResponse();

        Context context = null;
        try
        {
            initContext(username, password);
            context = getContext();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            log("NavRep.getAllUrls exception " + e);
        }

        String[] objectIds = request.getObjectIds();

        // image type can now be given as a semicolon separated list of types (eg. "jpg;cgr;")
        String imageType = request.getImageType();
        // if necessary, strip off last semicolon so split works correctly
        if (imageType.endsWith(";"))
        {
            imageType = imageType.substring(0,imageType.length()-1);
        }
        StringList imageTypeList = FrameworkUtil.split(imageType,";");

        try
        {
            String mcsURL = request.getMcsURL();

            // return requested formats for each object id in the list
            NavRepResult[] results = new NavRepResult[imageTypeList.size()*objectIds.length];
            int count = 0;
            StringList objectSelects    = new StringList(1);
            objectSelects.add(DomainConstants.SELECT_TYPE);
            
            //in case of DEC data , separate minor id in the request
            String[] majorIds = new String[objectIds.length];
            String[] minorIds = new String[objectIds.length];
            
            for(int i=0;i<objectIds.length;i++){
                StringList splitObjectId = FrameworkUtil.split(objectIds[i], ":");

                majorIds[i] = (String)splitObjectId.get(0);

                if(splitObjectId.size()>1){
                    minorIds[i] = (String)splitObjectId.get(1);
                }else{
                    minorIds[i] = "";
                }
            }

            MapList mlObjectType        = DomainObject.getInfo(context, objectIds, objectSelects);

            for (int i = 0; i < majorIds.length; i++)
            {
                  String objectId             = majorIds[i];
                Map mObjectType             = (Map)mlObjectType.get(i);
                String domainType           = (String)mObjectType.get(DomainConstants.SELECT_TYPE);
                String symbolicDomainType   = FrameworkUtil.getAliasForAdmin(context, "type", domainType, true);
                NodeType typeNode           = (NodeType)nodeMap.get(symbolicDomainType);
                boolean isPartFromVPLMSync  = isPartFromVPLMSync(context,objectId);

                log("NavRep.getAllUrls objectId :" + objectId);

                try{

                    Iterator itr            = imageTypeList.iterator();
                    while(itr.hasNext())
                    {
                        String imgType      = (String)itr.next();
                        log("NavRep.getAllUrls imageType :" + imgType);
                        if (isPartFromVPLMSync && ! "CATData".equalsIgnoreCase(imgType)) {
                            results[count++]    = getResultForVPLMParts(context, mcsURL, objectId, imgType, typeNode);
                        }else {
                            results[count++]    = getResult(context, mcsURL, objectIds[i], imgType, typeNode);
                        }
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace(System.out);
                    log("NavRep.getAllUrls exception " + e);
                }
            }
            response.setResults(results);
        }
        catch (Exception ex)
        {
            ex.printStackTrace(System.out);
            log("NavRep.getAllUrls exception " + ex);
        }

        log("NavRep.getAllUrls complete");
        return (response);
    }

    /**
     * This function will get a NavRepResult for the given object and given image type.
     * it is called from an JSP page used for launching 3DLiveEmbed player
     * arguments are packed in the string array args
     *
     * @param context the eMatrix <code>Context</code> object
     * @param mcsURL the inital portion of the URL
     * @param objectId the object to process
     * @param imageType the type of image to return (cgr or jpg)
     * @return NavRepResult the FCS url and timestamp of the resulting image file
     * @throws MatrixException
     * @since AEF 10.7.1.0
     */

    public String getResult(Context context, String[] args) throws Exception
    {
        HashMap programMap= (HashMap)JPO.unpackArgs(args);

        String mcsURL = (String)programMap.get("mcsURL");
        String objectId = (String)programMap.get("objectId");
        String imageType = (String)programMap.get("imageType");

        jpo.plmprovider.MetaDataBase_mxJPO metaInfo = new jpo.plmprovider.MetaDataBase_mxJPO();

        // this call is necassary to make sure, matadata is loaded at least once before calling Embed player
        if (nodeMap == null || nodeMap.size() == 0)
        {
            String[] arguments = new String[1];
            arguments[0] = "en";
            metaInfo.getMetaData(context, arguments);
        }
        String imageURL = null;
        NavRepResult imageNavRep = null;
        imageNavRep = getResult(context, mcsURL, objectId, imageType);
        imageURL = imageNavRep.getUrl();
        return imageURL;
    }

    /**
     * This function will get the file name for the given object and given image type.
     * it is called from an JSP page used for launching 3DVIA viewer
     * arguments are packed in the string array args
     *
     * @param context the eMatrix <code>Context</code> object
     * @param mcsURL the inital portion of the URL
     * @param objectId the object to process
     * @param imageType the type of image to return (cgr or jpg)
     * @return file name of the resulting image file
     * @throws MatrixException
     * @since R207
     */

    public String getFileName(Context context, String[] args) throws Exception
    {
        HashMap programMap= (HashMap)JPO.unpackArgs(args);

        String mcsURL = (String)programMap.get("mcsURL");
        String objectId = (String)programMap.get("objectId");
        String imageType = (String)programMap.get("imageType");

        jpo.plmprovider.MetaDataBase_mxJPO metaInfo = new jpo.plmprovider.MetaDataBase_mxJPO();

        // this call is necassary to make sure, matadata is loaded at least once before calling Embed player
        if (nodeMap == null || nodeMap.size() == 0)
        {
            String[] arguments = new String[1];
            arguments[0] = "en";
            metaInfo.getMetaData(context, arguments);
        }
        String fileName = null;
        NavRepResult imageNavRep = null;
        imageNavRep = getResult(context, mcsURL, objectId, imageType);
        fileName = imageNavRep.giveFileName();
        return fileName;
    }

    /**
     * This function will get a NavRepResult for the given object and given image type.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param mcsURL the inital portion of the URL
     * @param objectId the object to process
     * @param imageType the type of image to return (cgr or jpg)
     * @return NavRepResult the FCS url and timestamp of the resulting image file
     * @throws MatrixException
     * @since AEF 10.7.1.0
     */

    protected NavRepResult getResult(Context context, String mcsURL, String objectId, String imageType)
    throws Exception
    {
        DomainObject domainObject   = DomainObject.newInstance(context, objectId);
        String domainType           = (String)domainObject.getInfo(context, DomainConstants.SELECT_TYPE);
        String symbolicDomainType   = FrameworkUtil.getAliasForAdmin(context, "type", domainType, true);
        NodeType typeNode           = (NodeType)nodeMap.get(symbolicDomainType);
        boolean isPartFromVPLMSync  = isPartFromVPLMSync(context,objectId);
        if (isPartFromVPLMSync && ! "CATData".equalsIgnoreCase(imageType)) {
            return getResultForVPLMParts(context, mcsURL, objectId, imageType, typeNode);
        }else {
             return getResult(context, mcsURL, objectId, imageType, typeNode);
        }
    }


    /**
     * This function will get a NavRepResult for the given object and given image type.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param mcsURL the initial portion of the URL
     * @param objectId the object to process
     * @param imageType the type of image to return (cgr or jpg)
     * @param typeNode the Node type for the corresponding object ID
     * @return NavRepResult the FCS url and timestamp of the resulting image file
     * @throws MatrixException
     * @since R209
     */

    private NavRepResult getResult(Context context,
                                   String mcsURL,
                                   String objectId,
                                   String imageType,
                                   NodeType typeNode)

    throws MatrixException
    {
        log("NavRep.getResult start");

        String symbolicPath             = "";               // symbolic path
        String symbolicPaths            = "";               // symbolic path list
        String symbolicFormat           = "";               // symbolic format
        String symbolicFormats          = "";               // symbolic format list
        String path                     = "";               // file path
        String format                   = "";               // file format
        String subPath                  = "";               // Sub string of Path
        StringList symbolicPathList     = new StringList(); // symbolic path list
        StringList symbolicFormatList   = new StringList(); // symbolic format list
        StringList pathList             = new StringList(); // path list
        StringList formatList           = new StringList(); // format list
        StringList fileNameSelects      = new StringList(); // file name list
        StringList fileTimestampSelects = new StringList(); // file time stamp list
        StringList objectSelects        = new StringList(); // object selects
        NavRepResult result             = new NavRepResult();// NavRep result

        try
        {
            ContextUtil.startTransaction(context, true);
              DomainObject domainObject   = new DomainObject(objectId);

            if (imageType.equalsIgnoreCase("cgr")) {
                symbolicPaths   = typeNode.getImagePath();
                symbolicFormats = typeNode.getImageFormat();
            }
            else if (imageType.equalsIgnoreCase("jpg") || imageType.equalsIgnoreCase("png"))
            {
                symbolicPaths   = typeNode.getThumbnailPath();
                symbolicFormats = typeNode.getThumbnailFormat();
                if("png".equals(imageType))
                {
                    symbolicFormats = "format_PNG";
                }
            }
            else if(imageType.equalsIgnoreCase("CATData"))
            {
                symbolicPaths   = "from[Markup].to.id,from[Active Version].to.from[Markup].to.id,to[Active Version].from.from[Markup].to.id";
                symbolicFormats = "format_Markup,format_Markup,format_Markup";
            }
            else if (imageType.equalsIgnoreCase("CGM"))
            {
                symbolicPaths   = cgmFilePath;
                symbolicFormats = cgmFileFormat;
            }
            else if (imageType.equalsIgnoreCase("CATIA"))
            {
                if(domainObject.isKindOf(context, "MCAD Assembly"))
                {
                    symbolicPaths   = "from[Active Version].to.id,id,from[Active Version].to.from[CAD Representation].to.id,from[CAD Representation].to.id";
                    symbolicFormats = "format_asm,format_asm,format_prt,format_prt";
                
				}
                else if(domainObject.isKindOf(context, "MCAD Component"))
                {
                    symbolicPaths   = "from[Active Version].to.id,id";
                    symbolicFormats = "format_prt,format_prt";
                }
                else if(domainObject.isKindOf(context, "MCAD Representation"))
                {
                    symbolicPaths   = "from[Active Version].to.id,id";
                    symbolicFormats = "format_prt,format_prt";
                }
                else if(domainObject.isKindOf(context, "MCAD Versioned Assembly"))
                {
                    symbolicPaths   = "id,to[Active Version].from.id";
                    symbolicFormats = "format_asm,format_asm";
                }
                else if(domainObject.isKindOf(context, "MCAD Versioned Component"))
                {
                    symbolicPaths   = "id,to[Active Version].from.id";
                    symbolicFormats = "format_prt,format_prt";
                }
                else if(domainObject.isKindOf(context, "MCAD Versioned Representation"))
                {
                    symbolicPaths   = "id,to[Active Version].from.id";
                    symbolicFormats = "format_prt,format_prt";
                }
                else if(domainObject.isKindOf(context, "MCAD Drawing"))
                {
                    symbolicPaths   = "from[Active Version].to.id,id";
                    symbolicFormats = "format_drw,format_drw";
                }
                else if(domainObject.isKindOf(context, "MCAD Versioned Drawing"))
                {
                    symbolicPaths   = "id,to[Active Version].from.id";
                    symbolicFormats = "format_drw,format_drw";
                }
            }

            symbolicPathList        = FrameworkUtil.split(symbolicPaths, ",");
            symbolicFormatList      = FrameworkUtil.split(symbolicFormats, ",");
            log("NavRep.getResult symbolicPathList  <<" +symbolicPathList + ">>");
            log("NavRep.getResult symbolicFormatList << " +symbolicFormatList +">>");

            // proceed only if the number of paths and formats are the same
            if(symbolicPathList != null && symbolicPathList.size() > 0
                && symbolicFormatList != null && symbolicFormatList.size() > 0
                && ((symbolicPathList.size() == symbolicFormatList.size()
                    || imageType.equalsIgnoreCase("CGM") || imageType.equalsIgnoreCase("png"))))
            {

                for(int pathItr =0; pathItr < symbolicPathList.size(); pathItr++)
                {
                    symbolicPath    = (String)symbolicPathList.get(pathItr);
                    path            = PlmProviderUtil.substituteValues(context, symbolicPath);

                    if(imageType.equalsIgnoreCase("CGM") || imageType.equalsIgnoreCase("png") )
                    {//CGM and PNG formats have only one format in the list
                        symbolicFormat  = (String)symbolicFormatList.get(0);
                    }
                    else
                    {
                        symbolicFormat  = (String)symbolicFormatList.get(pathItr);
                    }

                    format              = PropertyUtil.getSchemaProperty(context, symbolicFormat);

                    if (path != null && !"".equals(path) && format != null && !"".equals(format) && path.indexOf("id") != -1)
                    {
                        subPath         = path.substring(0,path.lastIndexOf("id"));
                        pathList.add(path);
                        formatList.add(format);
                        fileNameSelects.add(subPath + "format["+format+"].file.name");
                        fileTimestampSelects.add(subPath + "format["+format+"].file.modified");
                    }
                }

                objectSelects.addAll(pathList);
                objectSelects.addAll(fileNameSelects);
                objectSelects.addAll(fileTimestampSelects);

                String[] objectIds  = new String[] {objectId};

                String[] majorAndMinorIds = objectId.split(":");
                if(majorAndMinorIds.length == 2){
              	  // if the minor Id is specified , then look for the file in both major and minor Objects
              	  // else if only major Id is specified, then file is looked in major and its active version
              	  if(UIUtil.isNotNullAndNotEmpty(majorAndMinorIds[0]) &&
            			  UIUtil.isNotNullAndNotEmpty(majorAndMinorIds[1]) &&
             			  !majorAndMinorIds[1].trim().equals("0.0.0.0")){
                  	  objectIds = majorAndMinorIds;
              	  }
                }
                
                MapList fullMl      = DomainObject.getInfo(context, objectIds, objectSelects);

               if (fullMl != null && fullMl.size() > 0)
                  {
                	  Map selectsMap  = new HashMap();
                      selectsMap.put("pathSelects", pathList);
                      selectsMap.put("fileNameSelects", fileNameSelects);
                      selectsMap.put("fileTimestampSelects", fileTimestampSelects);
                      selectsMap.put("formatList", formatList);                	  
                	  
                	  for(int fullMlItr = fullMl.size() -1 ;fullMlItr >= 0 ;fullMlItr--){
                		  
                		  NavRepResult resultTemp;
                		  
                		  resultTemp = getResult(context, objectId, mcsURL,(Map)fullMl.get(fullMlItr), selectsMap);                		  
                		
                		  
                		  if(UIUtil.isNotNullAndNotEmpty(resultTemp.getObjectId())){
                			  result.setObjectId(resultTemp.getObjectId());
                			  
                		  }
                		  
                		
                		  if(UIUtil.isNotNullAndNotEmpty(resultTemp.getUrl())){
                			  result.setUrl((resultTemp.getUrl()));
                			  
                		  }
                		  
                		 if(resultTemp.getTimestamp()!=0){
                			  result.setTimestamp((resultTemp.getTimestamp()));
                	  }
                			  

                    		  if(UIUtil.isNotNullAndNotEmpty(resultTemp.giveFileName())){
                    			  result.putFileName((resultTemp.giveFileName()));
                    			  
                    		  }
                    		  
                	  }
                  }
            }
            ContextUtil.commitTransaction(context);
        }
        catch (Exception e)
        {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            log("NavRep.getResult exception " + e);
        }
        log("NavRep.getResult complete");
        return result;
    }

    /**
     * This function will get a NavRepResult for the
     * given object (if it is VPLM Part) and given image type.
     *
     * If a select contains any relationship,the getInfo method will query for all
     * derived relationships and if it finds any object at any one of these relationships
     * it will return result with corresponding key. (i.e the key in the return result may
     * differs from the key in selects). We can't get the actual value from map if the key
     * in the result and selects is different.
     *
     * The method getResult will not work for VPLM data, since we have to query for abstract
     * relationship but not the implemented one. This method can be removed if core provides
     * some way to identify the modified key in the result MapList
     *
     * @param context the eMatrix <code>Context</code> object
     * @param mcsURL the initial portion of the URL
     * @param objectId the object to process
     * @param imageType the type of image to return (cgr or jpg)
     * @param typeNode the Node type for the corresponding object ID
     * @return NavRepResult the FCS url and timestamp of the resulting image file
     * @throws MatrixException
     * @since R211
     */
    private NavRepResult getResultForVPLMParts(Context context,
                                                String mcsURL,
                                                String objectId,
                                                String imageType,
                                                NodeType typeNode)
    throws MatrixException
    {
        context.setApplication(APPLICATION_VPLM);
        log("NavRep.getResultForVPLMParts start");
        NavRepResult result = new NavRepResult();// NavRep result
        try {
            ContextUtil.startTransaction(context, true);
            if("cgm".equalsIgnoreCase(imageType) || "cgr".equalsIgnoreCase(imageType) || "jpg".equalsIgnoreCase(imageType) || "png".equalsIgnoreCase(imageType)) {
                StringBuffer sbVPMImagePath = new StringBuffer();
                sbVPMImagePath.append("from[").append(DomainConstants.RELATIONSHIP_PART_SPECIFICATION).append("].to.from[");
                sbVPMImagePath.append(REL_VPM_REP_INSTANCE).append("].to[").append(TYPE_VPM_REP_REFERENCE);

                if ("cgm".equalsIgnoreCase(imageType)) {
                    sbVPMImagePath.append("|(attribute[").append(ATTR_VPM_VUSAGE).append("]=='DraftingRepresentation')");
                } else {
                    sbVPMImagePath.append("|(attribute[").append(ATTR_VPM_VUSAGE).append("]=='3DPart'");
                    sbVPMImagePath.append(" || attribute[").append(ATTR_VPM_VUSAGE).append("]=='3DShape')");
                }
                sbVPMImagePath.append("]");

                String imagPath     = sbVPMImagePath.toString()+".id";
                String imageFormats = "2,1,4";

                if ("jpg".equalsIgnoreCase(imageType) || "png".equalsIgnoreCase(imageType)) {
                    imageFormats    = "6";
                }

                StringList formatList           = FrameworkUtil.split(imageFormats, ",");
                StringList pathSelects          = new StringList();
                StringList fileNameSelects      = new StringList();
                StringList fileTimestampSelects = new StringList();

                for (int i = 0 ; i < formatList.size() ; i++) {
                    pathSelects.add(imagPath);
                    String format   = (String) formatList.get(i);
                    fileNameSelects.add(sbVPMImagePath.toString() + ".format["+format+"].file.name");
                    fileTimestampSelects.add(sbVPMImagePath.toString() + ".format["+format+"].file.modified");
                }

                StringList objectSelects        = new StringList();

                objectSelects.addAll(pathSelects);
                objectSelects.addAll(fileNameSelects);
                objectSelects.addAll(fileTimestampSelects);
                log("NavRep.getResultForVPLMParts object selects " +objectSelects);
                String[] objectIds  = new String[]{objectId};
                MapList fullMl      = DomainObject.getInfo(context, objectIds, objectSelects);
                log("NavRep.getResultForVPLMParts image results " +fullMl);

                //DomainObject.getinfo might chnage the keys. i.e it will search for sub relations and return the results,

                if (fullMl != null && fullMl.size() > 0) {
                    Map resultsmap      = (Map)fullMl.get(0);
                    Iterator itr        = resultsmap.keySet().iterator();
                    String key          = "";
                    while (itr.hasNext()) {
                        key             = (String)itr.next();
                        if (key.endsWith(".id")) {
                            key         = key.substring(0,key.lastIndexOf(".id"));
                            break;
                        }
                    }
                    for (int i = 0 ; i < pathSelects.size(); i++) {
                        pathSelects.set(i, ((String)pathSelects.get(i)).replace(sbVPMImagePath.toString(), key));
                        fileNameSelects.set(i, ((String)fileNameSelects.get(i)).replace(sbVPMImagePath.toString(), key));
                        fileTimestampSelects.set(i, ((String)fileTimestampSelects.get(i)).replace(sbVPMImagePath.toString(), key));
                    }
                    Map selectsMap  = new HashMap();
                    selectsMap.put("pathSelects", pathSelects);
                    selectsMap.put("fileNameSelects", fileNameSelects);
                    selectsMap.put("fileTimestampSelects", fileTimestampSelects);
                    selectsMap.put("formatList", formatList);
                    result = getResult(context, objectId, mcsURL , resultsmap ,selectsMap );
                }

            }
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            e.printStackTrace();
            log("NavRep.getResult exception " + e);
        }
        log("NavRep.getResult complete");
        return result;
    }


    private NavRepResult getResult (Context context,
                                    String objectId,
                                    String mcsURL,
                                    Map resultsmap,
                                    Map selectsMap)
    throws MatrixException
    {
        NavRepResult result = new NavRepResult();// NavRep result

        if (resultsmap != null && resultsmap.size() > 0 &&
            selectsMap != null && selectsMap.size() > 0) {
            try {
                String imageId                  = "";
                String fileFormat               = "";
                String fcsUrl                   = "";
                String fileName                 = "";
                String fileTimestamp            = "";
                int timestampIntValue           = 0;
                StringList pathSelects          = (StringList)selectsMap.get("pathSelects");
                StringList fileNameSelects      = (StringList)selectsMap.get("fileNameSelects");
                StringList fileTimestampSelects = (StringList)selectsMap.get("fileTimestampSelects");
                StringList formatList           = (StringList)selectsMap.get("formatList");
       OUTERMOST: for (int pathItr = 0; pathItr < pathSelects.size(); pathItr++)
                {
                    log("NavRep.getResult path"+(pathItr+1)+"  <<" +pathSelects.get(pathItr) +">>");

                    StringList imageIdList      = new StringList();
                    StringList fileNamesList    = new StringList();
                    StringList fileTimeStampList= new StringList();

                    // DomainObject.getInfo will return the values as either String or StringList.
                    // Also the string might have multiple values with the separator bell.
                    try {
                        imageIdList = (StringList)resultsmap.get(pathSelects.get(pathItr));
                    } catch (Exception ex) {                        
                        imageIdList = new StringList(((String)resultsmap.get(pathSelects.get(pathItr))).split("\\a"));
                    }

                    try {
                        fileNamesList   = (StringList)resultsmap.get(fileNameSelects.get(pathItr));
                    } catch (Exception ex) {                        
                        fileNamesList   = new StringList(((String)resultsmap.get(fileNameSelects.get(pathItr))).split("\\a"));
                    }

                    try {
                        fileTimeStampList   = (StringList)resultsmap.get(fileTimestampSelects.get(pathItr));
                    } catch (Exception ex) {                        
                        fileTimeStampList   = new StringList(((String)resultsmap.get(fileTimestampSelects.get(pathItr))).split("\\a"));
                    }

                    if (imageIdList != null && imageIdList.size() > 0
                            && fileNamesList != null && fileNamesList.size() > 0
                            && fileTimeStampList != null && fileTimeStampList.size() > 0)
                    {
                        for (int imageIdItr = 0; imageIdItr < imageIdList.size(); imageIdItr++ ) {
                            imageId     = (String)imageIdList.get(imageIdItr);

                            for (int fileItr = 0; fileItr < fileNamesList.size(); fileItr++) {
                                fileName        = (String)fileNamesList.get(fileItr);
                                fileTimestamp   = (String)fileTimeStampList.get(fileItr);
                                fileFormat      = (String)formatList.get(pathItr);

                                if (imageId != null && !"".equals(imageId)
                                    && fileName != null && !"".equals(fileName)
                                    && fileTimestamp != null && !"".equals(fileTimestamp))  {
                                    fcsUrl      = getUrl(context, mcsURL, imageId, fileName, fileFormat);
                                    if (fcsUrl.length() > 0) {
                                        Date date = eMatrixDateFormat.getJavaDate(fileTimestamp);
                                        timestampIntValue = new Double((date.getTime() / 1000)).intValue();
                                        break OUTERMOST;
                                    }
                                }

                            }// end of fileNamesList loop
                        }// end of imageIdList loop
                    }
                } // end of pathList loop

                log("NavRep.getResult Image Id <<" + imageId+ ">> file <<" + fileName + ">> of format <<" + fileFormat +
                        ">> with timestamp <<" + fileTimestamp + ">>");
                log("NavRep.getResult resulting url " + fcsUrl);
                result.setObjectId(objectId);
                result.setUrl(fcsUrl);
                result.putFileName(fileName);
                result.setTimestamp(timestampIntValue);

            } catch (Exception e) {
                e.printStackTrace();
                log("NavRep.getResult exception " + e);
            }
        }
        log("NavRep.getResult complete");
        return result;
    }

    protected NavRepResult getMarkupURL(Context context, String objectId, String mcsURL)
    throws Exception
    {
        log("NavRep.getMarkupURL start");
        NavRepResult result = new NavRepResult();

        DomainObject parentObject = new DomainObject(objectId);

        // object selectables
        StringList objectSelects = new StringList(1);
        objectSelects.add(DomainObject.SELECT_ID);

        Map map = parentObject.getRelatedObject(context, "Markup", true,objectSelects, null);
        String markupID = null;
        if(map != null)
        {
            markupID = (String) map.get(DomainObject.SELECT_ID);
        }

        log("NavRep.getMarkupURL markupID :" + markupID);
        if(markupID != null)
        {
            HashMap paramMap = new HashMap();
            paramMap.put("objectId", markupID);
            String[] args = JPO.packArgs(paramMap);
            MapList listOfFiles =(MapList)JPO.invoke(context, "emxCommonFileUI", null, "getFiles", args, MapList.class);

            if(listOfFiles != null && listOfFiles.size() > 0)
            {
                Map markupMap     = (Map)listOfFiles.get(0);
                String fileName   = (String) markupMap.get(CommonDocument.SELECT_FILE_NAME);
                String fileFormat = (String) markupMap.get(CommonDocument.SELECT_FILE_FORMAT);
                String timestampValue = (String) markupMap.get(CommonDocument.SELECT_FILE_MODIFIED);

                log("NavRep.getMarkupURL fileName :" + fileName);
                log("NavRep.getMarkupURL fileFormat :" + fileFormat);
                log("NavRep.getMarkupURL timestampValue :" + timestampValue);
                String fcsUrl = getUrl(context, mcsURL, markupID, fileName, fileFormat);

                log("NavRep.getMarkupURL fcsUrl :" + fcsUrl);
                if(fcsUrl.length() > 0 )
                {
                result.setObjectId((String)markupMap.get(markupID));
                result.setUrl(fcsUrl);
                Date date = eMatrixDateFormat.getJavaDate(timestampValue);
                int timestampIntValue = new Double((date.getTime() / 1000)).intValue();
                result.setTimestamp(timestampIntValue);
                }
            }
        }

        log("NavRep.getMarkupURL complete");
        return result;
    }
    /**
     * Returns FCS url for the file checked into objects whose id are passed as String array oids.
     * @param context the eMatrix <code>Context</code> object
     * @param mcsURL
     * @param oid
     * @param fileName
     * @param format
     * @return String[] - Returns FCS url for the file checked into objects whose id are passed as String array oids
     * @throws MatrixException
     * @since AEF 10.7.1.0
     */

    protected String getUrl(Context context, String mcsURL, String oid, String fileName, String format)
    throws MatrixException
    {
    	 /* Depending on flag flagForgetURLs avoided checkout code by generating one static url
         * Generating URL is required, because client is doing some logic with parsing of URL
         * In new mechanism client need - ObjectId, File Format and FileName
         */
    	
    	  if (doNotCheckout){
    	        try
    	         {
    	    		 StringBuffer fcsUrl = new StringBuffer();
    	    		 fcsUrl.append("http://staticurl");
    	    		 fcsUrl.append("&name=");
    	             fcsUrl.append(fileName);
    	             fcsUrl.append("&CATCacheKey=");
    	             //fcsUrl.append(oid + format + fileName);
    	             fcsUrl.append(oid);
    	             fcsUrl.append("&format=");
    	             fcsUrl.append(format);
    	             fcsUrl.append("&fileName=");
    	             fcsUrl.append(fileName);
    	             
    	             return (fcsUrl.toString());
    	         } catch (Exception e)
    	    	        {
    	    	            e.printStackTrace();
    	    	            return "";
    	    	        }
    	 }else {
        BusinessObject bo = new BusinessObject(oid);
        try
        {
            bo.open(context);

            ArrayList bops = new ArrayList();
            BusinessObjectProxy bproxy = new BusinessObjectProxy(oid,
                    format,
                    fileName,
                    false,
                    false);
            bops.add(bproxy);
            TicketWrapper ticket = Checkout.doIt(context, mcsURL, bops);
            StringBuffer fcsUrl = new StringBuffer();
            fcsUrl.append(ticket.getActionURL());
            fcsUrl.append("?");
            fcsUrl.append(FcsClient.resolveFcsParam("jobTicket"));
            fcsUrl.append("=");
            fcsUrl.append(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(ticket.getExportString()));
            fcsUrl.append("&name=");
            fcsUrl.append(fileName);
            fcsUrl.append("&CATCacheKey=");
            fcsUrl.append(oid + format + fileName);

            return (fcsUrl.toString());
        }
        catch(MatrixException mxExp)
        {
            log("NavRep.getUrl exception " + mxExp);
            return "";
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
     		 
    	 }
    	  
    }

    /**
     * hack to test things out.
     *
     * @param username the matrix user name
     * @param password password of the user
     * @param request
     * @return NavRepResponse
     * @since AEF 10.7.1.0
     */
    protected NavRepResponse getFakeUrls(String username, String password, NavRepRequest request)
    {
        log("NavRep start");
        NavRepResponse response = new NavRepResponse();

        String[] objectIds = request.getObjectIds();

        try {
            initContext(username, password);

            NavRepResult results[] = new NavRepResult[objectIds.length];
            for (int i = 0; i < objectIds.length; ++i) {
                BusinessObject bo = new BusinessObject(objectIds[i]);
                bo.open(getContext());
                String url = "s:\\cgr\\" + bo.getName() + ".cgr";
                results[i] = new NavRepResult();
                results[i].setObjectId(objectIds[i]);
                results[i].setUrl(url);
            }
            response.setResults(results);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            log("NavRep.getFakeUrls exception " + e);
        }

        return (response);
    }

    /**
    * This method is used to return the extension of the given file name.
    *
    * @param strFileName the complete name of the file
    * @return file extension
    * @since AEF 10.7.1.0
    */
   static private String getFileExtension(String strFileName) {
       int index = strFileName.lastIndexOf('.');

       if (index == -1)
       {
           return strFileName;
       } else
       {
           return strFileName.substring(index + 1, strFileName.length());
       }
   }

   /**
    * This method checks whether cross highlight should be enabled or not
    * returns true if all all the following conditions are satisfied
    *   1)VPM/TBE or DEC installed
    *   2)User preference is On
    *
    * @param context the matrix context
    * @param args
    * @return true if all the above mentioned conditions are satisfied
    * @throws Exception
    */
    public boolean showCrossHighlight (Context context, String[] args) throws Exception {
    	return PlmProviderUtil.showCrossHighlight(context);
    }

   /**
    * Access program to check for NavRep/Expected relationship is available or not
    * @param context the matrix context
    * @param args
    * @return true if NavRep/Expected relationship is available
    * @throws Exception
    */
    public boolean isNavRepExists(Context context, String[] args) throws Exception {
    	return showCrossHighlight(context, args);
    }


    /**
     * @param args
     */
    public static void main(String args[])
    {
        NavRepBase_mxJPO nr = new NavRepBase_mxJPO();
        NavRepRequest nrr = new NavRepRequest();
        String[] objIds = {"18406.54862.13050.30466","18406.54862.13093.60333"};
        nrr.setObjectIds(objIds);
        nrr.setMcsURL("http://localhost:8081/emxatrix");
        NavRepResponse nresp = nr.getUrls("Test Everything","",nrr);
        NavRepResult results[] = nresp.getResults();
        for(int i=0; i<results.length; i++)
        {
            System.out.println("Result["+i+"]: "+results[i]);
        }

        System.exit(0);
    }
}
