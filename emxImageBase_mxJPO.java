/*
 * emxImageBase
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] = $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/base/${CLASSNAME}.java 1.4.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


import  matrix.db.Context;
import  matrix.db.JPO;
import  matrix.db.BusinessObjectWithSelectList;
import  matrix.util.StringList;
import  java.util.Map;
import  java.util.HashMap;
import  java.util.Hashtable;
import  java.util.Vector;
import  java.io.File;
import  com.matrixone.apps.domain.DomainConstants;
import  com.matrixone.apps.domain.DomainObject;
import  com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import  com.matrixone.apps.domain.util.FrameworkException;
import  com.matrixone.apps.domain.util.ContextUtil;
import  com.matrixone.apps.domain.util.MapList;
import  com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import  com.matrixone.apps.common.util.DocumentUtil;
import  com.matrixone.apps.domain.util.PersonUtil;
import  com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.productline.Image;
import com.matrixone.apps.productline.ProductLineConstants;
import  com.matrixone.fcs.common.ImageRequestData;
import  com.matrixone.apps.common.util.*;
import  java.util.*;
import  matrix.db.*;

/**
 * This JPO class has some methods pertaining to Image type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxImageBase_mxJPO extends emxImageManager_mxJPO
{
    /**
    * Alias for the string from.id.
    */
    protected static final String FROM_ID = "from.id";
    /**
    * Alias for the string mode.
    */
    protected static final String MODE = "mode";
    /**
    * Alias for the string No.
    */
    protected static final String NO = "No";
    /**
    * Alias for the string New Value.
    */
    protected static final String NEW_VALUE = "New Value";
    /**
    * Alias for the string objectId.
    */
    protected static final String OBJECTID = "objectId";
    /**
    * Alias for the string objectList.
    */
    protected static final String OBJECTLIST = "objectList";
    /**
    * Alias for the string Old value.
    */
    protected static final String OLD_VALUE = "Old value";
    /**
    * Alias for the string paramList.
    */
    protected static final String PARAMLIST = "paramList";
    /**
    * Alias for the string paramMap.
    */
    protected static final String PARAMMAP = "paramMap";
    /**
    * Alias for the string relId.
    */
    protected static final String RELID = "relId";
    /**
    * Alias for the string requestMap.
    */
    protected static final String REQUESTMAP = "requestMap";
    /**
    * Alias for the string true.
    */
    protected static final String TRUE = "true";
    /**
    * Alias for the string Yes.
    */
    protected static final String YES = "Yes";
    /**
    * Alias for the key emxProduct.Error.UnsupportedClient.
    */
    protected static final String ERROR_UNSUPPORTED_CLIENT = "emxProduct.Error.UnsupportedClient";
    /**
    * Alias for the key emxProduct.Error.NoObjects.
    */
    protected static final String ERROR_NO_OBJECTS = "emxProduct.Error.NoObjects";
    /**
    * Alias for the key emxProduct.PrimaryImage.
    */
    protected static final String PRIMARYIMAGE_VALUE_YES = "emxProduct.PrimaryImage.Yes";
    protected static final String PRIMARYIMAGE_VALUE_NO = "emxProduct.PrimaryImage.No";
    protected static final String SUITE_KEY = "ProductLine";
    /**
     * Default constructor.
     *
     * Create a new emxImageBase object from a given id.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @return a emxImageBase
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public emxImageBase_mxJPO (Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
     * Main entry point.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
            String sContentLabel = EnoviaResourceBundle.getProperty(context,SUITE_KEY,ERROR_UNSUPPORTED_CLIENT,
                    context.getSession().getLanguage());
            throw  new Exception(sContentLabel);
        }
        return  0;
    }

    /**
     * Get the list of all images connected to parent object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing one String entry for key "objectId"
     * @return Object of type MapList containing the objetc ids of images.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getImages (Context context, String args[]) throws Exception {
        String strRelationshipName = ProductLineConstants.RELATIONSHIP_IMAGES;
        // The expandForAll method is called with the required parameters.
        MapList relBusObjList = expandForAll(context, args, strRelationshipName);
        return  relBusObjList;
    }

    /**
     * Get the list of all images connected to parent by passed relationship.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing one String entry for key "objectId"
     * @param strRelationshipName - string containing relationship name
     * @return Object of type MapList
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    protected MapList expandForAll (Context context, String args[], String strRelationshipName) throws Exception {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get(OBJECTID);
        //short is initialized to store the value the level till which image object will be searched
        short sh = 1;
        //Stringlist for querying is formulated.
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //Domain Object is instantiated with the parent object id of the Image object
        DomainObject domainObject = newInstance(context, strObjectId);
        //String is initialized to store the value of type name
        String strType = ProductLineConstants.TYPE_IMAGE;
        //The associated object details are retreived onto a MapList
        MapList relBusObjPageList = domainObject.getRelatedObjects(context,
                strRelationshipName, strType, objectSelects, relSelects, false,
                true, sh, "", "");
        return  relBusObjPageList;
    }

    /**Method to get info of the given image object file.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the Maps which contains string for ObjectId and Mode.
     * @return string containing the HTML tag.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public String imageFile (Context context, String args[]) throws Exception {
        try {
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            String strObjectId = (String)((HashMap)programMap.get(PARAMMAP)).get(OBJECTID);
            String strMode = (String)((HashMap)programMap.get(REQUESTMAP)).get(MODE);
            DomainObject domfile = newInstance(context, strObjectId);
            String sFile = domfile.getInfo(context, DomainConstants.SELECT_FILE_NAME);
            //StringList vctfileTagList = new StringList(1);
            String strFileTag = "";
            StringList strImageFileList = new StringList();
            if (strMode != null) {
                strFileTag = "<input type=\"file\" name=\"FilePath\" size=\"20\">";
                strImageFileList.add(strFileTag);
                return  strFileTag;
            }
            strFileTag = sFile;
            strImageFileList.add(strFileTag);
            return  strFileTag;
        } catch (Exception e) {
            throw  new FrameworkException(e);
        }
    }

    /**
     * Gets the Primary icon based on relationship exist or not.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds Hashmap containing the ObjectId and OBJECTLIST.
     * @return vector of status icon images
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public Vector primaryImageIcon (Context context, String[] args) throws Exception {
        // Unpacking the arguments
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)((HashMap)programMap.get(PARAMLIST)).get(OBJECTID);
        MapList relBusObjPageList = (MapList)programMap.get(OBJECTLIST);
        if (!(relBusObjPageList != null)) {
            String sContentLabel = EnoviaResourceBundle.getProperty(context,SUITE_KEY,ERROR_NO_OBJECTS,
            context.getSession().getLanguage());
            throw  new Exception(sContentLabel);
        }
        //Get the number of objects in objectList
        int iNoOfObjects = relBusObjPageList.size();
        //Initialising a vector based on the number of objects.
        Vector vctStatusIconTagList = new Vector(iNoOfObjects);
        String strPrimaryRelationshipName = ProductLineConstants.RELATIONSHIP_PRIMARY_IMAGE;
        //Stringlist for querying is formulated.
        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
        //short is initialized to store the value the level till which image object will be searched
        short sh = 1;
        //Domain Object is instantiated with the parent object id of the Image object
        DomainObject domainObject = newInstance(context, strObjectId);
        //The associated object details are retreived onto a MapList
        MapList relBusObjPageList1 = domainObject.getRelatedObjects(context,
                strPrimaryRelationshipName, "*", objectSelects, relSelects,
                true, true, sh, "", "");
        //If image object is Primary add image tag in the string List return else add empty string
        for (int i = 0; i < iNoOfObjects; i++) {
            String strId = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
            String strStatusIconTag = null;
            strStatusIconTag = "";
            if (!(relBusObjPageList1.isEmpty())) {
                String strPrimaryId = (String)((Hashtable)relBusObjPageList1.get(0)).get(DomainConstants.SELECT_ID);
                if (strPrimaryId.equals(strId)) {
                    //XSSOK
                    strStatusIconTag = "<img src=\"images/iconSmallPrimaryImage.gif\" border=\"0\"  align=\"middle\" />";
                }
            }
            vctStatusIconTagList.add(strStatusIconTag);
        }
        //Returning the Status icon
        return  vctStatusIconTagList;
    }

    /**
     * Concatenate horizontal size and vertical size of a image to a string.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing one String entry for key "objectId"
     * @return vector of size
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public Vector imageSize (Context context, String[] args) throws Exception {
        // Unpacking the arguments
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList)programMap.get(OBJECTLIST);
        //Checking if objectList is null
        if (!(relBusObjPageList != null)) {
            String sContentLabel = EnoviaResourceBundle.getProperty(context,SUITE_KEY,ERROR_NO_OBJECTS,
                    context.getSession().getLanguage());
            throw  new Exception(sContentLabel);
        }
        //Get the number of objects in objectList
        int iNoOfObjects = relBusObjPageList.size();
        //Initialising a vector based on the number of objects.
        Vector vctImageSizeList = new Vector(iNoOfObjects);
        //Initialising a StringList and adding the attribute names as Strings to it
        StringList listSelect = new StringList(1);
        String strAttrb1 = ProductLineConstants.SELECT_IMAGE_HORIZONTAL_SIZE;
        String strAttrb2 = ProductLineConstants.SELECT_IMAGE_VERTICAL_SIZE;
        listSelect.addElement(strAttrb1);
        listSelect.addElement(strAttrb2);
        String arrObjId[] = new String[iNoOfObjects];
        //Getting the object ids for objects in the table
        try {
            for (int i = 0; i < iNoOfObjects; i++) {
                Object obj = relBusObjPageList.get(i);
                if (obj instanceof HashMap) {
                    arrObjId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                }
                else if (obj instanceof Hashtable) {
                    arrObjId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
                }
            }
        } catch (Exception ex) {
            throw  new FrameworkException(ex);
        }
        //Getting the attributes for the coresponding object ids
        //Instantiating BusinessObjectWithSelectList of matrix.db and initialising it to null
        BusinessObjectWithSelectList attributeList = null;
        //Fetching  attributes of the objectids
        attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);
        //Extracting the horizontal and vertical size values
        for (int i = 0; i < iNoOfObjects; i++) {
            String strHorizontalSize = attributeList.getElement(i).getSelectData(strAttrb1);
            String strVerticalSize = attributeList.getElement(i).getSelectData(strAttrb2);
            String strImageSize = "";
            if (!(strVerticalSize.equals("") || strHorizontalSize.equals(""))) {
                strImageSize = strHorizontalSize + "x" + strVerticalSize;
            }
            vctImageSizeList.add(strImageSize);
        }
        //Returning the Size
        return  vctImageSizeList;
    }

    /**
     * Returns the Range of Primary Image.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @return String List of Range
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public static Object getPrimaryImageRange (Context context, String[] args) throws Exception {
        // Initialize the return variable
        HashMap rangeMap = new HashMap();
        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();
        fieldDisplayRangeValues.addElement(EnoviaResourceBundle.getProperty(context,SUITE_KEY,PRIMARYIMAGE_VALUE_YES, context.getSession().getLanguage()));
        fieldDisplayRangeValues.addElement(EnoviaResourceBundle.getProperty(context,SUITE_KEY,PRIMARYIMAGE_VALUE_NO, context.getSession().getLanguage()));
        fieldRangeValues.addElement(YES);
        fieldRangeValues.addElement(NO);
        rangeMap.put( "field_choices" , fieldRangeValues);
        rangeMap.put( "field_display_choices" , fieldDisplayRangeValues);
        return  rangeMap;
    }

    /**
     * Updates the status of the primary image.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
            strObjectId - string containing the object id.
            strRelId - string containing the relationship id.
            strNewValue - string containing the new value.
            strOldValue - string containing the old value
     * @return Int value.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public static int updatePrimaryImageStatus (Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get(PARAMMAP);
        String strObjectId = (String)paramMap.get(OBJECTID);
        String strRelId = (String)paramMap.get(RELID);
        String strNewValue = (String)paramMap.get(NEW_VALUE);
        String strOldValue = (String)paramMap.get(OLD_VALUE);
        int iReturnValue = 0;
        String strRelationshipName = ProductLineConstants.RELATIONSHIP_IMAGES;
        String strParentObjectId = getParentObjectId(context, strObjectId,
                strRelId, strRelationshipName);
        if (strOldValue.equalsIgnoreCase(YES) && strNewValue.equalsIgnoreCase(NO)) {
            short sh = 1;
            //String is initialized to store the value of relationship name
            String strPrimaryRelationshipName = ProductLineConstants.RELATIONSHIP_PRIMARY_IMAGE;
            //Stringlist for querying is formulated.
            StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
            StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            DomainObject dom = newInstance(context, strParentObjectId);
            //The associated object details are retreived onto a MapList
            MapList relBusObjPageList = dom.getRelatedObjects(context, strPrimaryRelationshipName,
                    "*", objectSelects, relSelects, true, true, sh, "", "");
            String strRelPrimaryId = (String)((Hashtable)relBusObjPageList.get(0)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
            try {
                //Domain Relationship is instantiated with relationship id
                //disconnect method of DomainRelationship class is called to disconnect the relationship
                DomainRelationship.disconnect(context, strRelPrimaryId);
            } catch (Exception e) {
                // The exception with appropriate message is thrown to the caller.
                throw  new FrameworkException(e);
            }
        }
        else if (strOldValue.equalsIgnoreCase(NO) && strNewValue.equalsIgnoreCase(YES)) {
            Image imageBean = new Image(strParentObjectId);
            imageBean.setAsPrimaryImage(context, strObjectId,
                    strParentObjectId);
        }
        // Process the information to set the field values for the current object
        return  iReturnValue;
    }

    /**
     * Returns the Status of Primary Image.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
            strObjectId - string containing the object id.
            strRelId - string containing the relationship id.
     * @return StringList of Status
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public static Object getPrimaryImageStatus (Context context, String[] args) throws Exception {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      HashMap paramMap = (HashMap)programMap.get(PARAMMAP);
      HashMap requestMap = (HashMap)programMap.get(REQUESTMAP);
      StringList strStatusList = new StringList();
      String strObjectId = (String)paramMap.get(OBJECTID);
      String strRelId = (String)paramMap.get(RELID);
      String strMode = (String)requestMap.get(MODE);
      //Mar 20th 2006: Added by Enovia MatrixOne for bug 316933
      if(strMode == null)
        strMode = DomainConstants.EMPTY_STRING;
      // Initialize the variables
      if((strRelId == null) || strRelId.equals(null) || strRelId.equals(""))
        return  strStatusList;
      //String is initialized to store the value of relationship name
      String strRelationshipName = ProductLineConstants.RELATIONSHIP_IMAGES;
      String strParentObjectId = getParentObjectId(context, strObjectId,
      strRelId, strRelationshipName);
      Image imageBean = new Image(strParentObjectId);
      String strStatus = imageBean.isPrimaryImage(context, strObjectId, strParentObjectId);
      if (strMode.equalsIgnoreCase("Edit"))
      {
          if (strStatus.equalsIgnoreCase(TRUE)) {
            strStatusList.addElement(YES);
          }
          else {
            strStatusList.addElement(NO);
          }
      }
      else
      {
          if (strStatus.equalsIgnoreCase(TRUE)) {
            strStatusList.addElement(EnoviaResourceBundle.getProperty(context,SUITE_KEY,PRIMARYIMAGE_VALUE_YES,context.getSession().getLanguage()));
          }
          else {
            strStatusList.addElement(EnoviaResourceBundle.getProperty(context,SUITE_KEY,PRIMARYIMAGE_VALUE_NO,context.getSession().getLanguage()));
          }
      }
      return  strStatusList;
    }

    /**
     * Returns the Sequence Number.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
            strObjectId - string containing the object id.
            strRelId - string containing the relationship id.
     * @return String containing the sequence number.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
     public String getSequenceNumber (Context context, String[] args) throws Exception {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      HashMap paramMap = (HashMap)programMap.get(PARAMMAP);
      String strSeq_No = "";
      try {
        String strRelId = (String)paramMap.get(RELID);
        // If strRelId is not null
        if(!strRelId.equals(null) && !strRelId.equals(""))
        {
          DomainRelationship domRelImage = new DomainRelationship(strRelId);
          String strRelationshipName = ProductLineConstants.ATTRIBUTE_SEQUENCE_ORDER;
          strSeq_No = XSSUtil.encodeForHTML(context,domRelImage.getAttributeValue(context, strRelationshipName));
        }
      }
      catch (Exception e) {
        // Return Blank if Exception occurs
        return strSeq_No;
      }
      return  strSeq_No;
    }

    /**
     * Returns the URl of Primary Image.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a Map with the following entries:
            strObjectId - string containing the object id.
            strPrimaryUrl - string containing the Primary Image URl.
            attachment - string containing the attachment.
            file - string containing the file name
     * @return String containing the url
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public String primaryImageURL (Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strPrimaryUrl = (String)((HashMap)(programMap.get("requestMap"))).get("strPrimaryUrl");
        String strattachment = (String)((HashMap)(programMap.get("requestMap"))).get("attachment");
        String strfile = (String)((HashMap)(programMap.get("requestMap"))).get("file");
        String strShowPrimaryUrl = "<img src=" + strPrimaryUrl + "&attachment="
                + strattachment + "&file=" + strfile + " height=120>";
        return  strShowPrimaryUrl;
    }

    /**
     * Gets the Parent Object id.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strObjectId object id of the image.
     * @param strRelId relationship id.
     * @param strRelationshipName the relationship name.
     * @return string of parent object id.
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    protected static String getParentObjectId (Context context, String strObjectId,
            String strRelId, String strRelationshipName) throws Exception {
        //Stringlist for querying is formulated.
        StringList relSelects = new StringList();
        relSelects.addElement(FROM_ID);
        String strRelationshipWhere = DomainConstants.SELECT_ID + "==" + strRelId;
        //short is initialized to store the value the level till which object will be searched
        short sh = 1;
        DomainObject dom = newInstance(context, strObjectId);
        MapList relBusObjPageList = dom.getRelatedObjects(context, strRelationshipName,
                "*", null, relSelects, true, true, sh, "", strRelationshipWhere);
        String strParentObjectId = (String)((Hashtable)relBusObjPageList.get(0)).get(FROM_ID);
        return  strParentObjectId;
    }

    /**
     * Gets the format and file name.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the map containing following input arguments:
     *    objetlist - list of object ids
     *    objectid - the object id
     * @return vector containing the display details of image
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public Vector getDisplayDetails (Context context, String[] args) throws Exception {
         //Unpacking the args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //Gets the objectList from args
        MapList relBusObjPageList = (MapList)programMap.get(OBJECTLIST);
        Vector vctDisplayDetails = new Vector();
        //No of objects
        int iNoOfObjects = relBusObjPageList.size();
        String arrObjId[] = new String[iNoOfObjects];
        Object obj = null;
        //Getting the bus ids for objects in the table
        for (int i = 0; i < iNoOfObjects; i++) {
                obj = relBusObjPageList.get(i);
                if (obj instanceof HashMap) {
                        arrObjId[i] =
                                (String) ((HashMap) relBusObjPageList.get(i)).get(
                                        DomainConstants.SELECT_ID);
                } else if (obj instanceof Hashtable) {
                        arrObjId[i] =
                                (String) ((Hashtable) relBusObjPageList.get(i)).get(
                                        DomainConstants.SELECT_ID);
                }
        }
        StringList listSelect = new StringList(3);
        String strAttrb1 = DomainConstants.SELECT_ID;
        String strAttrb2 = DomainConstants.SELECT_FILE_NAME;
        String strAttrb3 = DomainConstants.KEY_FORMAT;
        listSelect.addElement(strAttrb1);
        listSelect.addElement(strAttrb2);
        listSelect.addElement(strAttrb3);

        //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
        BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

        try {
            for (int i = 0; i < iNoOfObjects; i++) {
                obj = relBusObjPageList.get(i);
                String strObjectId = attributeList.getElement(i).getSelectData(strAttrb1);
                String strFile = attributeList.getElement(i).getSelectData(strAttrb2);
                if (strFile != null) {
                    String strFormat = attributeList.getElement(i).getSelectData(strAttrb3);
                    String strURL = "<b><A HREF='../productline/ImageUtil.jsp?mode=Display&objectId="
                            + strObjectId + "&file=" + strFile + "&format="
                            + strFormat + "'>" + strFile + "</b>";
                    vctDisplayDetails.add(strURL);
                }
                else {
                    vctDisplayDetails.add("");
                }
            }
        } catch (Exception ex) {
            throw  new FrameworkException(ex);
        }
        return  vctDisplayDetails;
    }


    private Map objectMap;

    // Modified Bu Enovia MatrixOne for Bug# 318148
    /**
     * Creates an Image Object and checks in a file to it.
     *  Also sets the primary image it this is the one.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - HashMap programMap
     * @return string containing the new object id.
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     */
    public Map checkin (Context context, String[] args) throws Exception {
        try {

            HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);
            //Parameters obtained from the Common Check In/Out Dialog

            String parentId       = (String) uploadParamsMap.get("parentId");
            String objectId       = (String) uploadParamsMap.get("objectId");
            String fcsEnabled   = (String) uploadParamsMap.get("fcsEnabled");
            String strCounnt = (String) uploadParamsMap.get("noOfFiles");
            int count = new Integer(strCounnt).intValue();

            String strImageType = (String) uploadParamsMap.get("type");
            String revision = (String) uploadParamsMap.get("revision");
            String policy = (String) uploadParamsMap.get("policy");
            String owner = (String) uploadParamsMap.get("person");

            String strUnlock = (String)uploadParamsMap.get("unlock");
            String strAppend = (String)uploadParamsMap.get("append");

            if(strUnlock==null)
                strUnlock = "false";

            if(strAppend==null)
                strAppend = "false";

            objectMap = new HashMap();
            StringList formats = new StringList(count);
            StringList fileNames = new StringList(count);

            for( int i=0; i<count; i++ )
            {
                String formatI  = (String)uploadParamsMap.get("format"+i);
                String fileNameI  = (String)uploadParamsMap.get("fileName"+i);

                if (fileNameI != null && !"".equals(fileNameI) && !"null".equals(fileNameI))
                {
                    formats.addElement(formatI);
                    fileNames.addElement(fileNameI);
                }
            }
            objectMap.put("format", formats);
            objectMap.put("fileName", fileNames);

             String receiptValue = (String)uploadParamsMap.get(DocumentUtil.getJobReceiptParameterName(context));
            //Start of write transaction

            ContextUtil.startTransaction(context, true);

            /*The different values in the formBean is obtained by specifying the
             *key value
             */
            String strImageName = (String)uploadParamsMap.get("Imagename");

            String strImageDescription = (String)uploadParamsMap.get("description");
            String strImageHorizontalSize = (String)uploadParamsMap.get("Horsize");
            String strImageVerticalSize = (String)uploadParamsMap.get("Versize");

            String strImageUnitOfMeasure = (String)uploadParamsMap.get("UnitOfMeasure");
            String strImageOwner = (String)uploadParamsMap.get("owner");
            //String strprimaryImage = (String)uploadParamsMap.get("PrimaryImage");
            String strImageMarketingName = (String)uploadParamsMap.get("MarName");
            String strImageMarketingText = (String)uploadParamsMap.get("MarText");
            String strVaultName = (String)uploadParamsMap.get("ImageVault");
            String strPolicy = (String)uploadParamsMap.get("Imagepolicy");
            String strParentOId = (String)uploadParamsMap.get("parentId");

            /*
             *String are initialized to store the value of relationship names
             *that will be used in this method.
             */
            String strRelationshipName = ProductLineConstants.RELATIONSHIP_IMAGES;
            String strPrimaryRelationshipName = ProductLineConstants.RELATIONSHIP_PRIMARY_IMAGE;
            //Variable for Revision - initialized to empty String
            String strRevision = "";
            /*boolean varaible to specify to connect the new created
             *image object in the from side of relationship
             */
            boolean bIsFrom = true;
            /*HashMap to store the attribute name value pairs.
             *The attribute values to be set for the object are put in the map.
             */
            HashMap attributeMap = new HashMap();
            attributeMap.put(ProductLineConstants.ATTRIBUTE_IMAGE_VERTICAL_SIZE,
                    strImageVerticalSize);
            attributeMap.put(ProductLineConstants.ATTRIBUTE_IMAGE_HORIZONTAL_SIZE,
                    strImageHorizontalSize);
            attributeMap.put(ProductLineConstants.ATTRIBUTE_IMAGE_UOM,
                    strImageUnitOfMeasure);
            attributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_NAME,
                    strImageMarketingName);
            attributeMap.put(ProductLineConstants.ATTRIBUTE_MARKETING_TEXT,
                    strImageMarketingText);
            //String is initialized to store object id of created object
            String strNewObjId = null;
            /*
             * The create method is called with the required parameters.
             */
            Image image = new Image();
            strNewObjId = image.create(context, strImageType, strImageName,
                    strRevision, strImageDescription, strPolicy, strVaultName,
                    attributeMap, strImageOwner, strParentOId, strRelationshipName,
                    bIsFrom);

            image.setId(strNewObjId);
            setId(strNewObjId);

            //boolean varibale for setting sequence order
            /* The setSequenceOrder method is called with the required parameters
             * to set the attribute value of relationship
             */
            image.setSequenceOrder(context, strParentOId,
                    strImageType, strRelationshipName, strNewObjId);
            /*
             *If the Primary Image field in made "Yes" in Create Page then the Image object is connected
             *to the Parent object through relationship "Primary Image".
             */

            DomainObject domObject = DomainObject.newInstance(context, strParentOId);
            String strPrimaryImageId = domObject.getInfo(context, "from[" + strPrimaryRelationshipName + "].to.id");

            if (strPrimaryImageId == null) {
                // boolean varibale for connection the Image object
                /*
                 * The setImageAsPrimary method is called with the required parameters.
                 */
                image.setImageAsPrimary(context, strParentOId,
                        strImageType, strPrimaryRelationshipName, strNewObjId);

              }
            boolean unlock = "true".equalsIgnoreCase(strUnlock);
            boolean append = "true".equalsIgnoreCase(strAppend);

            StringList objectIds = new StringList();
            StringList appends = new StringList();
            StringList unlocks = new StringList();

            objectIds.addElement(strNewObjId);
            appends.addElement(strAppend);
            unlocks.addElement(strUnlock);

            uploadParamsMap.put("objectId", strNewObjId);
            uploadParamsMap.put("noOfFiles", "1");

            objectMap.put("appends", appends);
            objectMap.put("objectId", objectIds);
            objectMap.put("unlocks", unlocks);

            objectCheckin(context, uploadParamsMap, (HashMap)objectMap);
            generateTransformatedImages(context, uploadParamsMap, (HashMap)objectMap);


            ContextUtil.commitTransaction(context);

            //Begin of add for Bug# 300329 by Enovia MatrixOne on 16 Mar 05
            //Set the image object icon

            // Modified by Enovia MatrixOne for Bug # 311012 Date Oct 25, 2005
            //String[] methodArgs = new String[2];
            //methodArgs[0] = strNewObjId;
            //methodArgs[1] = format;

            //createPrimaryImage(context,methodArgs);
            //End of add for Bug# 300329 by Enovia MatrixOne on 16 Mar 05

            return  objectMap;
        } catch (Exception e) {
            //Transaction aborted in case of exception
            ContextUtil.abortTransaction(context);
            //The exception with appropriate message is thrown to the caller.
            throw  new FrameworkException(e.getMessage());
        }
    }

     /**Method to connect to the image object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param strFromObjectId string containing the object id of from side.
     * @param strToObjectId string containing the object id of to side.
     * @param strRelationshipName string containing the relationship name.
     * @throws FrameworkException if the operation fails
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    protected void imageConnect (Context context, String strFromObjectId, String strToObjectId,
            String strRelationshipName) throws FrameworkException, Exception {
        //Domain Object is instantiated with the from object id
        DomainObject fromObject = newInstance(context, strFromObjectId);
        //Domain Object is instantiated with the to object id
        DomainObject toObject = newInstance(context, strToObjectId);
        //DomainObject toObject = new DomainObject(strToObjectId);
        //The method connect of DomainRelationship class is called to connect the objects
        DomainRelationship.connect(context, fromObject, strRelationshipName,
                toObject);
    }

     /**Method to connect the existing image to given object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the Map containing the following elements
                    reqMap - Map containing the objectList
                    strNewObjIdArr - string array containing the Table row ids
                    strParentOIdArr - string array containing the parent id
     * @throws Exception if the operation fails
     * @since ProductCentral 10.0.0.0
     */
    public void addExisting (Context context, String[] args) throws Exception {
        try {
          //Start of write transaction
          ContextUtil.startTransaction(context, true);
          //Unpacking the args
          HashMap programMap = (HashMap)JPO.unpackArgs(args);
          //Gets the objectList from args
          HashMap reqMap = (HashMap)programMap.get("reqMap");
          //Get all TableRowIds
          String[] strNewObjIdArr = (String[])reqMap.get("emxTableRowId");
          //Get the parent ID
          String[] strParentOIdArr = (String[])reqMap.get("parentOID");
          String strParentOId = strParentOIdArr[0];
          //Image bean is instantiated
          Image image = new Image();
          //String is initialized to store the value of ImageType
          String strImageType = ProductLineConstants.TYPE_IMAGE;
          //String is initialized to store the value of relationship name
          String strRelationshipName = ProductLineConstants.RELATIONSHIP_IMAGES;
          //String is initialized to store the value of ObjectID
          String strNewObjId = "";
          DomainObject domParentObj = new DomainObject(strParentOId);
          MapList relBusObjPageList = domParentObj.getRelatedObjects(context, strRelationshipName, strImageType, null, null, false, true, (short)1, "", "");
          int iNoOfObjects = relBusObjPageList.size();
          String strAttributeName = ProductLineConstants.ATTRIBUTE_SEQUENCE_ORDER;
          for (int a = 0; a < strNewObjIdArr.length; a++) {
              //retriving individual ObjectID from the String Array
              strNewObjId = strNewObjIdArr[a];
            DomainRelationship domRel = DomainRelationship.connect(context, domParentObj, strRelationshipName, new DomainObject(strNewObjId));
            domRel.setAttributeValue(context,strAttributeName,""+(iNoOfObjects+a));
          }

          MapList imageList = new MapList();
          StringList objectSelects = new StringList(DomainConstants.SELECT_ID);

          StringBuffer sbRelWhereCondition = new StringBuffer(25);
          sbRelWhereCondition = sbRelWhereCondition.append("attribute["+ ProductLineConstants.ATTRIBUTE_SEQUENCE_ORDER +"]==1");
          String strRelWhereCondition = sbRelWhereCondition.toString();

          DomainObject domParent = DomainObject.newInstance(context, strParentOId);

          String strPrimaryImageId = domParent.getInfo(context,
                                                       "from[" + ProductLineConstants.RELATIONSHIP_PRIMARY_IMAGE + "].to.id");

          if(strPrimaryImageId == null)
          {
               imageList = domParent.getRelatedObjects(context,
                                                       strRelationshipName,
                                                       DomainConstants.QUERY_WILDCARD,
                                                       objectSelects,
                                                       null,
                                                       false,
                                                       true,
                                                       (short)1,
                                                       DomainConstants.EMPTY_STRING,
                                                       strRelWhereCondition);

               if(imageList.size() !=0)
               {
                   String strImageId = (String)((Map)imageList.get(0)).get(DomainConstants.SELECT_ID);

                   image.setImageAsPrimary(context,
                                           strParentOId,
                                           strImageType,
                                           ProductLineConstants.RELATIONSHIP_PRIMARY_IMAGE,
                                           strImageId);
               }
          }

        //End transaction
        ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            //Transaction aborted in case of exception
            ContextUtil.abortTransaction(context);
            //The exception with appropriate message is thrown to the caller.
            throw  new FrameworkException(e);
        }
    }

    /**
     * This trigger method is used to set the icon of Image object when a new
     * image file is checked into the Image Object. This method first converts
     * the image file checked into the object to gif icon of particular size and
     * then modifies the object to set the icon. To convert the image file into
     * icon a third party tool called image magick is used. The path of this
     * application is obtained from the system.properties. If there is no entry
     * present for image magick in system.properties then the image iteself is
     * set as icon of the object without any conversion.
     *
     * @param context The ematrix context of the request.
     * @param args This string array contains following elements:
     *                      0 - The id of the image object
     *                      1 - The workspace for image magick
     * @throws Exception
     * @throws FrameworkException
     * @since ProductCentral10.6
     */
    public void createPrimaryImage(
                                   Context context,
                                   String[] args) throws Exception, FrameworkException {

        try
        {
            //Get the workspace path
            //Modified by Enovia MatrixOne for Bug# 302221 on 21-Apr-05
            String strWorkspacePath = context.createWorkspace();

            //Get the object id, type and the workspace path from the arguments
            String strImageId = args[0];


            String strGenerateImage = "";

// Modified by Enovia MatrixOne for Bug # 311012 Date Oct 25, 2005
            String strImageFormat = args[1];
            String strImageSize = "";

            //Get the settings for generating the image for Image.
            try {
                strGenerateImage = EnoviaResourceBundle
                        .getProperty(context,"emxProduct.PrimaryImage.type_Image.GenerateImage");

                strImageSize = EnoviaResourceBundle
                        .getProperty(context,"emxProduct.PrimaryImage.type_Image.Size");
            } catch (Exception e) {
                strGenerateImage = "false";
            }

            //Get the name of the file checked into the image object
            DomainObject domImage = newInstance(
                                                context,
                                                strImageId);
// Modified by Enovia MatrixOne for Bug # 311012 Date Oct 25, 2005
            String strFileName = domImage.getInfo(
                                                  context,
                                                  "format["+strImageFormat+"].file.name");

// Modified by Enovia MatrixOne for Bug # 311012 Date Oct 25, 2005
            //Checkout the file into the workspace folder
            domImage.checkoutFile(
                                  context,
                                  false,
                                  strImageFormat,
                                  strFileName,
                                  strWorkspacePath);

            boolean bImageMagic = true;
            File tmpFile = null;

            if ("true".equals(strGenerateImage)) {

                //Generate the name for the new image file
                StringBuffer sbNewFileName = new StringBuffer(
                        getFileBaseName(strFileName));
                sbNewFileName.append("_primary.");

                if (strImageFormat == null || strImageFormat.length() == 0) {
                    sbNewFileName.append(getFileExtension(strFileName));
                } else {
                    sbNewFileName.append(strImageFormat);
                }

                try {
                    //Get the path of the Image Magick from property file
                    String imageMagick = EnoviaResourceBundle
                            .getProperty(context,"emxProduct.ImageMagick.Directory");
                    //Call the generate thumbnail method to conver the image into
                    //thumbnail
                    generateThumbnail(
                                      context,
                                      strWorkspacePath,
                                      strFileName,
                                      sbNewFileName.toString(),
                                      strImageSize,
                                      imageMagick);
                    //Set the newly generated image as icon for the object
                    setIconImage(
                                 context,
                                 strImageId,
                                 strWorkspacePath + File.separatorChar
                                         + sbNewFileName.toString());

                    //delete the temporary files
                    tmpFile = new File(strWorkspacePath, sbNewFileName.toString());
                    tmpFile.delete();
                } catch (Exception e) {
                    bImageMagic = false;
                }

            }
            if (!bImageMagic) {
                //If image magick is not present then set the checked out file
                //as icon
                setIconImage(
                             context,
                             strImageId,
                             strWorkspacePath + File.separatorChar
                                     + strFileName);
            }
            tmpFile = new File(strWorkspacePath, strFileName);
            tmpFile.delete();

            domImage = null;
            //Added by Enovia MatrixOne for Bug# 302221 on 21-Apr-05
            context.deleteWorkspace();
        }catch(Exception e){
            throw new FrameworkException(e.getMessage());
        }

    }

    /**
     * This method is used to get the base name of the file from the
     * complete file name.
     *
     * @param context The ematrix context of the request.
     * @param strFileName Thie complete name of the file.
     * @return Base file name.
     * @since ProductCentral10.6
     */
    static public String getFileBaseName(
                                         String strFileName) {
        int index = strFileName.lastIndexOf('.');

        if (index == -1) {
            return strFileName;
        } else {
            return strFileName.substring(
                                      0,
                                      index);
        }
    }

     /**
     * This method is used to get the extension of the file from the
     * complete file name.
     *
     * @param context The ematrix context of the request.
     * @param strFileName Thie complete name of the file.
     * @return file extension.
     * @since ProductCentral10.6
     */
    static public String getFileExtension(
                                          String strFileName) {
        int index = strFileName.lastIndexOf('.');

        if (index == -1) {
            return strFileName;
        } else {
            return strFileName.substring(
                                      index + 1,
                                      strFileName.length());
        }
    }

    /**
     * This method is used to generate the thumbnail from the image
     * file using image magick.
     *
     * @param context The ematrix context of the request.
     * @param strWorkspacePath The wordspace path.
     * @param strSourceFileName Source file name.
     * @param strNewFileName Thumbnail file name.
     * @param strImageSize Size of the thumbnail
     * @param strImageMagickLoc Location of the image magick.
     * @throws Exception
     * @since ProductCentral10.6
     */
    static private void generateThumbnail(
                                         Context context,
                                         String strWorkspacePath,
                                         String strSourceFileName,
                                         String strNewFileName,
                                         String strImageSize,
                                         String strImageMagickLoc)
            throws Exception {
        File workingDirectory = new File(strWorkspacePath);

        boolean bIsPDF = false;

        if (getFileExtension(
                             strSourceFileName).equalsIgnoreCase(
                                                              "pdf")) {
            bIsPDF = true;
        }

        File newFile = new File(strWorkspacePath, strNewFileName);
        if (newFile.exists()){
            newFile.delete();
        }

        StringBuffer strCmd = new StringBuffer("\"");
        strCmd.append(strImageMagickLoc);
        strCmd.append("convert\" \"");
        strCmd.append(strWorkspacePath);
        strCmd.append(File.separatorChar);
        strCmd.append(strSourceFileName);
        if (bIsPDF)
            strCmd.append("[0]");

        if (strImageSize != null && strImageSize.length() != 0) {
            strCmd.append("\" -thumbnail ");
            strCmd.append(strImageSize);
        }

        strCmd.append(" \"");
        strCmd.append(strWorkspacePath);
        strCmd.append(File.separatorChar);
        strCmd.append(strNewFileName);
        strCmd.append("\"");

        Runtime app = Runtime.getRuntime();
        Process proc = app.exec(
                                strCmd.toString(),
                                null,
                                workingDirectory);

        boolean bSucess = false;
        if (bIsPDF) {
            for (int counter = 0; !bSucess && (counter < 100); counter++) {
                bSucess = newFile.exists();

                if (!bSucess)
                    Thread.sleep(600);
            }
        } else {
            if (proc.waitFor() == 0)
                bSucess = true;
        }

        if (!bSucess) {
            String strErrorMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Error.ImageConvert",
                    context.getSession().getLanguage());
            throw new Exception(strErrorMessage);
        }
    }

    /**
     * This method is used to set the icon of the object.
     *
     * @param context The ematrix context of the request.
     * @param strObjectId The object id.
     * @param strFileLocation The icon file location..
     * @throws Exception
     * @since ProductCentral10.6
     */
    static public void setIconImage(
                                    Context context,
                                    String strObjectId,
                                    String strFileLocation) throws Exception {
        /*StringBuffer strCmd = new StringBuffer();
        strCmd.append("mod bus ");
        strCmd.append(strObjectId);
        strCmd.append(" image \"");
        strCmd.append(strFileLocation);
        strCmd.append("\"");*/

        //Begin of modify for Bug# 300329 by Enovia MatrixOne on 16 Mar 05
        try{
           /* MqlUtil.mqlCommand(
                           context,
                           strCmd.toString(),
                           true);*/
            MqlUtil.mqlCommand(  context,"mod bus $1 image $2" ,true,strObjectId,strFileLocation);
                  
                    
                 
        }catch(Exception e){
            String strErrorMsg =EnoviaResourceBundle.getProperty(context,SUITE_KEY,"emxProduct.Alert.ImageIconFailed",
                    context.getSession().getLanguage());
            emxContextUtil_mxJPO.mqlNotice(context,strErrorMsg);
        }
        //End of modify for Bug# 300329 by Enovia MatrixOne on 16 Mar 05
    }

     /**
     * This method is used to display the image in the image list page
     * @param context the eMatrix <code>Context</code> object
     * @param args contains the argument Map of the context object
     * @throws Exception if operation fails
     * @since ProductCentral 10-6-SP2
     */

     public Vector getImageFile (Context context, String[] args) throws Exception
     {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList mlObjectList = (MapList)programMap.get(OBJECTLIST);
        Vector vectImage = new Vector(mlObjectList.size());
        ArrayList arraylist = new ArrayList();
        BusinessObjectProxy businessobjectproxy;
        StringList imageURLs = new StringList();
        String strFile = "";
        HashMap requestMap = (HashMap)programMap.get("paramList");
        HashMap imageData = (HashMap)requestMap.get("ImageData");

        // Begin of add by Enovia MatrixOne for Bug # 316919 on March 29, 2006
        for(int i=0;i<mlObjectList.size();i++)
        {
            String strImageId = (String)((Hashtable)mlObjectList.get(i)).get(DomainConstants.SELECT_ID);
            // Modified by Enovia MatrixOne for Bug # 316685 Date- March 20, 2006
            String strSymbolicFormat = EnoviaResourceBundle.getProperty(context,"emxProduct.Image.Table.Format");
            String strFormat = PropertyUtil.getSchemaProperty(context, strSymbolicFormat);

            DomainObject domImage = DomainObject.newInstance(context, strImageId);
            strFile = domImage.getInfo(context, "format["+strFormat+"].file.name");

            if ( strFile != null && !"".equals(strFile) && !"null".equals(strFile) )
            {
                businessobjectproxy = new BusinessObjectProxy(strImageId, strFormat, strFile, false, false);
                arraylist.add(businessobjectproxy);
                imageURLs.add(strFile);
            }
            else {
                imageURLs.add("&nbsp;");
            }
        }

        String[] strImageURLs = ImageRequestData.getImageURLS(context, arraylist, imageData);
        String imageURL;
        int count = 0;
        Iterator imageItr = imageURLs.iterator();
        while( imageItr.hasNext())
        {
            imageURL = (String)imageItr.next();
            if (!imageURL.equalsIgnoreCase("&nbsp;") ) {
                if( strImageURLs.length > count )
                {
                    strFile = imageURL;
                    imageURL = strImageURLs[count];
                    count ++;
                    vectImage.add("<img border='0' align='absmiddle' src='" + XSSUtil.encodeForHTMLAttribute(context,imageURL) + "' title='"+XSSUtil.encodeForHTMLAttribute(context,strFile)+"'/>");

                } else {
                    vectImage.add("");
                }
            } else {
                vectImage.add("");
            }
        }
        return  vectImage;
        // End of add by Enovia MatrixOne for Bug # 316919 on March 29, 2006
     }

     /**
     * This method is used to display the image in the image properties page
     * @param context the eMatrix <code>Context</code> object
     * @param args contains the argument Map of the context object
     * @throws Exception if operation fails
     * @since ProductCentral 10-6-SP2
     */

     public String getImageInForm (Context context, String[] args) throws Exception
     {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        ArrayList arraylist = new ArrayList();
        HashMap requestMap = (HashMap)programMap.get("requestMap");
        HashMap imageData = (HashMap)requestMap.get("ImageData");
        String strImageId = (String)((HashMap)(programMap.get("requestMap"))).get("objectId");
        // Modified by Enovia MatrixOne for Bug # 316685 Date- March 20, 2006
        String strSymbolicFormat = EnoviaResourceBundle.getProperty(context,"emxProduct.Image.Form.Format");
        String strFormat = PropertyUtil.getSchemaProperty(context, strSymbolicFormat);
        DomainObject domImage = DomainObject.newInstance(context, strImageId);
        String strFile = domImage.getInfo(context, "format["+strFormat+"].file.name");

        String[] strImageURLs = new String[1];
        String imageURL = DomainConstants.EMPTY_STRING;
        String strImageURL = DomainConstants.EMPTY_STRING;

        if ( strFile != null && !"".equals(strFile) && !"null".equals(strFile) )
        {
            // Begin of add by Enovia MatrixOne for Bug # 316919 on March 29, 2006
            BusinessObjectProxy businessobjectproxy = new BusinessObjectProxy(strImageId, strFormat, strFile, false, false);
            arraylist.add(businessobjectproxy);

            strImageURLs = ImageRequestData.getImageURLS(context, arraylist, imageData);
            imageURL = strImageURLs[0];
        }

        if(strImageURLs.length>0 && strFile != null && !"".equals(strFile) && !"null".equals(strFile))
        {
            strImageURL = "<img border='0' align='absmiddle' src='" + XSSUtil.encodeForHTMLAttribute(context,imageURL) + "' title='"+XSSUtil.encodeForHTMLAttribute(context,strFile)+"'/>";
        }
        else
        {
            strImageURL = "";
        }

        return  strImageURL;
        // End of add by Enovia MatrixOne for Bug # 316919 on March 29, 2006
     }

}
