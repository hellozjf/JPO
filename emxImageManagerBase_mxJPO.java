/*
 * emxImageManagerBase
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 *
 */

import java.awt.Container;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectProxy;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.FileList;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.db.Vault;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonImageConverterRemoteExec;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.common.util.ImageConversionUtil;
import com.matrixone.apps.common.util.ImageManagerUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.Job;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.fcs.common.ImageRequestData;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.Namespace;
//import com.sun.image.codec.jpeg.JPEGCodec;
//import com.sun.image.codec.jpeg.JPEGEncodeParam;
//import com.sun.image.codec.jpeg.JPEGImageEncoder;

/**
 * This JPO class has some methods pertaining to Image Holder type.
 * @author schakravarthy
 * @version ProductCentral 10.6.1.0  - Copyright (c) 2005, MatrixOne, Inc.
 */
public class emxImageManagerBase_mxJPO extends emxCommonDocument_mxJPO
{

    protected static String ATTRIBUTE_TRAVERSE_ALTPATH = PropertyUtil.getSchemaProperty("attribute_TraverseAltPath");
    private static final String SELECT_ATTRIBUTE_TRAVERSE_ALTPATH = "attribute["+ATTRIBUTE_TRAVERSE_ALTPATH+"]";
    protected static String PRIMARY_IMAGE_FROM_ALTPATH = PropertyUtil.getSchemaProperty("attribute_PrimaryImageFromAltPath");
    private static final String SELECT_ATTRIBUTE_PRIMARY_IMAGE_FROM_ALTPATH = "attribute["+PRIMARY_IMAGE_FROM_ALTPATH+"]";

    protected static String TYPE_3DXML;
    protected static String TYPE_3DXMLCGR;
    protected static String TYPE_CGR;
    protected static String TYPE_THUMBNAIL;
    protected static String TYPE_VIEWABLE;

    protected static String REL_VIEWABLE;
    protected static String FORMAT_GENERIC;
    private static String SELECT_GENERIC_FORMAT_FILES;
    protected static String FORMAT_3DXML;
    private static String SELECT_3DXML_FORMAT_FILES;
    protected static String FORMAT_CGR;
    protected static String FORMAT_THUMBNAIL;
    protected static String POLICY_VIEWABLE;
    protected static String UTILITY_NAME = null;
    protected static final String NCONVERT = "nConvert";
    protected static final String IMAGICK = "ImageMagick";
    public static final String TYPE_VPM_REFERENCE       = PropertyUtil.getSchemaProperty("type_VPMReference");
    public static final String RELATIONSHIP_PARTSPECIFICATION = PropertyUtil.getSchemaProperty("relationship_PartSpecification");    
	private static final String _3DDRIVE = "3DDrive"; 
	private static final String SOLIDWORKSPRT= "sldprt";
	private static final String SOLIDWORKSASM = "sldasm";
	private static final String SOLIDWORKSDRW = "slddrw";
	private static final String SOLIDWORKS_UTILITY="swx";

	// AB6 - For CATVIAV5 File
	private static final String CATIAV5PART= "CATPart";
	private static final String CATIAV5MATERIAL = "CATMaterial";
	private static final String CATIAV5DRAWING = "CATDrawing";
	private static final String CATIAV5PRODUCT = "CATProduct";
	private static final String CATIAV5SHAPE = "CATShape";
	private static final String CATIAV5ANALYSIS= "CATAnalysis";
	private static final String CATIAV5SYSTEM = "CATSystem";
	private static final String CATIAV5PROCESS = "CATProcess";
	private static final String CATIAV5_UTILITY="CATV5FileExtractThumbnail";

    // Added for MCAD Performance
    private static HashMap _properties = new HashMap();
    String defaultThumbnailUrl="";
   // private static String ICON_3D_IMAGE = "../common/images/icon3ds.gif";
    private static String ICON_3D_IMAGE ="../common/images/iconActionShowHide3D.png";

    //352000
    private boolean CREATE_IMAGE = false;

    //Added for performing Image conversion in background
    private static String ENABLE_BACKGROUND_IMAGE_CONVERSION = "false";
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since VCP 10.5.0.0
     * @grade 0
     */
    public emxImageManagerBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
        TYPE_3DXML = PropertyUtil.getSchemaProperty(context, "type_3dxmlViewable");
        TYPE_3DXMLCGR = PropertyUtil.getSchemaProperty(context, "type_3dxmlcgrViewable");
        TYPE_CGR = PropertyUtil.getSchemaProperty(context, "type_CgrViewable");
        TYPE_THUMBNAIL = PropertyUtil.getSchemaProperty(context, "type_ThumbnailViewable");
        TYPE_VIEWABLE = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_Viewable);

        REL_VIEWABLE = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_Viewable);
        FORMAT_GENERIC = PropertyUtil.getSchemaProperty(context,"format_generic");
        FORMAT_3DXML = PropertyUtil.getSchemaProperty(context, "format_3DXML");
        FORMAT_CGR = PropertyUtil.getSchemaProperty(context, "format_CGR");
        FORMAT_THUMBNAIL = PropertyUtil.getSchemaProperty(context, "format_THUMBNAIL");
        POLICY_VIEWABLE = PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_policy_ViewablePolicy);
        SELECT_3DXML_FORMAT_FILES = "format[" + FORMAT_3DXML + "].file.name";
        SELECT_GENERIC_FORMAT_FILES = "format[" + FORMAT_GENERIC + "].file.name";
        try{
            defaultThumbnailUrl="../components/images/"+EnoviaResourceBundle.getProperty(context,"emxComponents.3DXML.DefaultImage");
        }catch(Exception e){
            defaultThumbnailUrl="";
        }

        objectMap = new HashMap();
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since VCP 10.5.0.0
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.MethodOnCommonFile", context.getLocale().getLanguage()));
        }
        return 0;
    }
    //this is called from derived classes
    //352000
    public void setCreateImage(Context context, boolean createImage) throws Exception
    {
        CREATE_IMAGE = createImage;
    }

  // nconvert change start

  static public String addSeparator(String path)
  {
    if(path.lastIndexOf(java.io.File.separatorChar) != path.length()-1 ||
       path.lastIndexOf('/') != path.length()-1)
    path += java.io.File.separatorChar;

    return path;
  }
  // nconvert change end

    public Map associateImage(Context context, String[] args) throws Exception  {
        CREATE_IMAGE = true; //352000
        try {
            ContextUtil.startTransaction(context, true);
            if (args == null || args.length < 1) {
                throw (new IllegalArgumentException());
            }

            HashMap uploadParamsMap = (HashMap)JPO.unpackArgs(args);

            String objectId       = (String) uploadParamsMap.get("objectId");
            String strCounnt = (String) uploadParamsMap.get("noOfFiles");
            int count = new Integer(strCounnt).intValue();

            // Defining the ObjectMap parameters and putting them into the Map.
            StringList formats = new StringList(count);
            StringList fileNames = new StringList(count);
            objectMap.put("format", formats);
            objectMap.put("fileName", fileNames);

            for( int i=0; i<count; i++ ) {
                String formatI  = (String)uploadParamsMap.get("format" + i);
                String fileNameI  = (String)uploadParamsMap.get("fileName" + i);

                if (!UIUtil.isNullOrEmpty(fileNameI)) {
                    formats.addElement(formatI);
                    fileNames.addElement(fileNameI);
                }
            }


            // Master Object Parameters
            String type = (String) uploadParamsMap.get("type");
            type = UIUtil.isNullOrEmpty(type) ? TYPE_IMAGE_HOLDER : type;

            String policy = (String) uploadParamsMap.get("policy");
            policy = UIUtil.isNullOrEmpty(policy) ? POLICY_IMAGE_HOLDER : policy;

            DomainObject object = DomainObject.newInstance(context, objectId);
            String imageHolderId = object.getInfo(context, SELECT_IMAGE_HOLDER_ID);

            boolean setPrimaryImage = false;

            DomainObject imageHolder = UIUtil.isNullOrEmpty(imageHolderId) ? null : DomainObject.newInstance(context, imageHolderId);
            if (imageHolder == null) {
                imageHolder = createImageHolderObject(context, object);
                imageHolderId = imageHolder.getId(context);
                setPrimaryImage = true;
            } else {
                String primImage = imageHolder.getAttributeValue(context, ATTRIBUTE_PRIMARY_IMAGE);
                setPrimaryImage = UIUtil.isNullOrEmpty(primImage);
            }
            if(setPrimaryImage) {
                String fileName = (String)fileNames.get(0);
                fileName = ImageManagerUtil.getPrimaryImageFileNameForImageManager(fileName);
                imageHolder.setAttributeValue(context, ATTRIBUTE_PRIMARY_IMAGE, fileName);
            }

            uploadParamsMap.put("objectId", imageHolderId);
            objectCheckin(context, uploadParamsMap, (HashMap)objectMap);

            generateTransformatedImages(context, uploadParamsMap, (HashMap)objectMap);

            ContextUtil.commitTransaction(context);
            return objectMap;
		} catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            ex.printStackTrace();
            objectMap.put("errorMessage", ex.getMessage());
            return objectMap;
        }
    }

    protected DomainObject createImageHolderObject(Context context, DomainObject parentObject) throws FrameworkException {
        try {
            ContextUtil.pushContext(context);
            DomainObject imageObject = DomainObject.newInstance(context, TYPE_IMAGE_HOLDER);
            imageObject.createAndConnect(context, TYPE_IMAGE_HOLDER, RELATIONSHIP_IMAGE_HOLDER, parentObject, false);
            return imageObject;
        } finally {
            ContextUtil.popContext(context);
        }
    }
    /**
     * This method actually checks if the property for background job is set or not and accordingly invokes
     * method generateTransformatedImagesInBackground or method generateTransformatedImages
     *
     * method generateTransformatedImagesInBackground again invokes generateTransformatedImages method
     * through background job
     *
     * @param context The ematrix context of the request.
     * @param uploadParamsMap holds object id
     * @param objMap holds fileName
     * @returns nothing
     * @throws Exception
     * @since R213
     */
    public void generateTransformatedImages(Context context, HashMap uploadParamsMap, HashMap objMap) throws Exception {
        try {
			ENABLE_BACKGROUND_IMAGE_CONVERSION = EnoviaResourceBundle.getProperty(context, "emxComponents.ImageManager.EnableBackgroundConversion");
			ENABLE_BACKGROUND_IMAGE_CONVERSION = ENABLE_BACKGROUND_IMAGE_CONVERSION.toLowerCase();
		} catch(Exception ex) {
	      //Do Nothing use the default value for background image conversion
		}

		if( "true".equals(ENABLE_BACKGROUND_IMAGE_CONVERSION) ) {
        	generateTransformatedImagesInBackground(context, uploadParamsMap, (HashMap)objMap);
        } else {
            Map objectMap = new HashMap();
            String objectId  = (String) uploadParamsMap.get("objectId");
            StringList fileNames = (StringList) objMap.get("fileName");
            objMap.put("objectId", objectId);
            objMap.put("fileName",fileNames);
            generateTransformatedImages(context, objMap);
        }
    }
    /**
     * This method creates the background Job and invokes the method generateTransformatedImagesInBackground
     *
     * @param context The ematrix context of the request.
     * @param uploadParamsMap holds object id
     * @param objMap holds fileName
     * @returns nothing
     * @throws Exception
     * @since R213
     */
    private void generateTransformatedImagesInBackground(Context context, HashMap uploadParamsMap, HashMap objMap) throws Exception {
      try {
          String jponame        = "emxImageManagerBase";
          String methodName     = "generateTransformatedImagesInBackground";
          String objectId       = (String) uploadParamsMap.get("objectId");
          StringList fileNames = (StringList) objMap.get("fileName");
          String fileName = FrameworkUtil.join(fileNames, "~");
          String[] args = {objectId, fileName};
          Job job = new Job(jponame, methodName, args);
          job.setActionOnCompletion("Delete");
          job.createAndSubmit(context);
      } catch (Exception ex) {
          ex.printStackTrace();
      }
    }
    /**
     * This method is being invoked from background Job
     *
     * @param context The ematrix context of the request.
     * @param args holds context object id,fileName
     * @returns nothing
     * @throws Exception
     * @since R213
     */
    public void generateTransformatedImagesInBackground(Context context, String[] args) throws Exception {
      try {
          Map objMap = new HashMap();
          if( args.length >= 2 ) {
              objMap.put("objectId", args[0]);
              String fileName = args[1];
              StringList fileNames = FrameworkUtil.split(fileName, "~");
              objMap.put("fileName",fileNames);
          }
          generateTransformatedImages(context, objMap);

      } catch (Exception ex)
      {
          ex.printStackTrace();
      }
    }

    private void generateTransformatedImages(Context context, Map objMap) throws Exception {
        try {
            String objectId  = (String) objMap.get("objectId");
            StringList fileNames = (StringList) objMap.get("fileName");
            List lstFilesToTransform = new ArrayList();
            StringList CADFormatList = ImageManagerUtil.getCADFormatFileExtensions(context);
            for (int i=0; i<fileNames.size(); i++ ) {
                String strFileName = (String)fileNames.get(i);
                String fileExt = getFileExtension(strFileName);
                /* Commented for IR-047886V6R2011x, this check is not required after the fix is done for IR-047901V6R2011x
                 if(strImageFormat.equalsIgnoreCase(fileExt)){
                 strImageFormat = fileExt;
                 }*/
                if(!CADFormatList.contains(fileExt)) {
                    Hashtable htCCIHInfo = new Hashtable();
                    htCCIHInfo.put("Oid", objectId);
                    htCCIHInfo.put("File", strFileName);
                    htCCIHInfo.put("Format", emxImageManager_mxJPO.FORMAT_GENERIC);
                    lstFilesToTransform.add(htCCIHInfo);
                }  // end of if loop cgr and 3dxml check.
            }
            String gotMcsURL = MqlUtil.mqlCommand(context, "get env $1 $2","global", "MCSURL");
            new CommonImageConverterRemoteExec().convertImageAndCheckinSameObject(context, gotMcsURL, lstFilesToTransform, null, ".jpg");

        } catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

    }


 /**
     * This method is used to get image
     *
     * @param String filename
     * @ returns image object
     * @since AppsCommon
     */

    public Image loadImage(String fileName) throws Exception
    {
        try
        {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Image image;
            //352000
            if(CREATE_IMAGE) {
                image = toolkit.createImage(fileName);
                CREATE_IMAGE = false;
            } else
            {
                image = toolkit.getImage(fileName);
            }
            MediaTracker mediaTracker = new MediaTracker(new Container());
            mediaTracker.addImage(image,0);
            mediaTracker.waitForID(0);

            return image;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

   /**
     * This method is used to generate the new thumbnail image and places in
     * workspace path based on the drawImage param
     *
     * Assumption: Only JPEG format will be the output irrespective of the image format inputs.
     *
     * @param context The ematrix context of the request.
     * @param strWorkspacePath The wordspace path.
     * @param strSourceFileName Source file name.
     * @param strNewFileName Thumbnail file name.
     * @param imageWidth
     * @param imageHeight
     * @param strImageUtilityLoc Location of the image magick.
     * @param drawImage
     * @throws Exception
     * @since AppsCommon
     */

    public void generateImageThumbnail(Context context, String strWorkspacePath,
                                         String strSourceFileName, String strNewFileName,
                                         int imageWidth, int imageHeight, Image image, boolean drawImage) throws Exception
    {

    	try
    	{

    		StringBuffer newFileName = new StringBuffer(150);
    		newFileName.append(strWorkspacePath);
    		newFileName.append(java.io.File.separatorChar);
    		newFileName.append(strNewFileName);

    		StringBuffer sourceFileName = new StringBuffer(150);
    		sourceFileName.append(strWorkspacePath);
    		sourceFileName.append(java.io.File.separatorChar);
    		sourceFileName.append(strSourceFileName);
    		if(image == null) {
    			image = loadImage(sourceFileName.toString());

    			imageWidth = image.getWidth(null);
    			imageHeight = image.getHeight(null);
    		}



    		if(imageWidth < 0  || imageHeight < 0) {

    			objectMap.put("errorMessage", 
    					EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Image.InvalidImageFormat" ));
    			throw new Exception(
    					EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Image.InvalidImageFormat" ));
    		}

    		ImageConversionUtil.scaleImage(context, sourceFileName, newFileName, imageWidth, imageHeight, drawImage);
    	}
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }


   /**
     * This method is used to generate the new thumbnail image.
     * @param context The ematrix context of the request.
     * @param strWorkspacePath The wordspace path.
     * @param strSourceFileName Source file name.
     * @param strNewFileName Thumbnail file name.
     * @param imageWidth
     * @param imageHeight
     * @param strImageUtilityLoc Location of the image magick.
     * @throws Exception
     * @since AppsCommon
     */
    
    public void generateImageThumbnail(Context context, String strWorkspacePath,
            String strSourceFileName, String strNewFileName,
            int imageWidth, int imageHeight, Image image) throws Exception
            {
    			generateImageThumbnail( context,  strWorkspacePath,
                 strSourceFileName,  strNewFileName,
                 imageWidth,  imageHeight,  image, false);
            }

    /**
     * This method is used to generate the thumbnail from the image
     * file using either image magick or nConvert.
     *
     * @param context The ematrix context of the request.
     * @param strWorkspacePath The wordspace path.
     * @param strSourceFileName Source file name.
     * @param strNewFileName Thumbnail file name.
     * @param imageWidth
     * @param imageHeight
     * @param strImageUtilityLoc Location of the image magick.
     * @throws Exception
     * @since AppsCommon 10.6.SP1
     */
    public void generateImageUtilityThumbnail(Context context,
                                         String strWorkspacePath,
                                         String strSourceFileName,
                                         String strNewFileName,
                                         int imageWidth,
                                         int imageHeight,
                                         String strImageUtilityLoc)
            throws Exception
    {
        if ( UTILITY_NAME == null || "null".equals(UTILITY_NAME) || "".equals(UTILITY_NAME) ) {
          UTILITY_NAME = (String)EnoviaResourceBundle.getProperty(context,"emxComponents.ImageManager.ImageUtility.Name");
        }
        // nconvert code starts
        if(UTILITY_NAME.equalsIgnoreCase(NCONVERT))
        {
            imageWidth = imageHeight;
            generateImageUtilityThumbnailnConvert(context,
                                          strWorkspacePath,
                                          strSourceFileName,
                                          strNewFileName,
                                          imageWidth,
                                          imageHeight,
                                          strImageUtilityLoc);
            return;
        }
        // nconvert code ends

        //Image Magick code starts
        else if(UTILITY_NAME.equalsIgnoreCase(IMAGICK))
        {
            generateImageUtilityThumbnailImagick(context,
                                          strWorkspacePath,
                                          strSourceFileName,
                                          strNewFileName,
                                          imageWidth,
                                          imageHeight,
                                          strImageUtilityLoc);
            return;
		
		}
	}
	
	// AB6 - For CATVIAV5 File
	public void generateImageUtilityThumbnailCATIAV5(Context context,
			String strWorkspacePath,
			String strSourceFileName,
			String strNewFileName,
			int imageWidth,
			int imageHeight,
			String strImageUtilityLoc)
	throws Exception
	{
		String newFilePath="";
		
		java.io.File workingDirectory = new java.io.File(strWorkspacePath);
		StringList command = new StringList();
		strImageUtilityLoc = addSeparator(strImageUtilityLoc);

		command.add(strImageUtilityLoc + CATIAV5_UTILITY);

		command.add(strWorkspacePath + java.io.File.separatorChar + strSourceFileName);
		newFilePath=strWorkspacePath + java.io.File.separatorChar + strNewFileName;
		command.add(newFilePath);

		//String key = EnoviaResourceBundle.getProperty(context,"emxComponents.SolidWorks.ImageUtility.Key");
		//command.add(key);
		boolean bSucess = false;
		try
		{
			bSucess = startImageConversionProcess(context, (String[])command.toArray(new String []{}), workingDirectory);
			File newFile = new File(newFilePath);
			if(newFile.length()==0){
				bSucess=false;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();

		}
		if (!bSucess) {
			String errorMessage = 
				EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Image.ImageConversionFail");
			objectMap.put("errorMessage",errorMessage );
			throw new Exception(errorMessage);
        }
    }
	
	public void generateImageUtilityThumbnailswx(Context context,
			String strWorkspacePath,
			String strSourceFileName,
			String strNewFileName,
			int imageWidth,
			int imageHeight,
			String strImageUtilityLoc)
	throws Exception
	{
		String newFilePath="";
		
		java.io.File workingDirectory = new java.io.File(strWorkspacePath);
		StringList command = new StringList();
		strImageUtilityLoc = addSeparator(strImageUtilityLoc);

		command.add(strImageUtilityLoc + SOLIDWORKS_UTILITY);

		command.add(strWorkspacePath + java.io.File.separatorChar + strSourceFileName);
		newFilePath=strWorkspacePath + java.io.File.separatorChar + strNewFileName;
		command.add(newFilePath);

		String key = EnoviaResourceBundle.getProperty(context,"emxComponents.SolidWorks.ImageUtility.Key");
		command.add(key);
		boolean bSucess = false;
		try
		{
			bSucess = startImageConversionProcess(context, (String[])command.toArray(new String []{}), workingDirectory);
			File newFile = new File(newFilePath);
			if(newFile.length()==0){
				bSucess=false;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();

		}
		if (!bSucess) {
			String errorMessage = 
				EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Image.ImageConversionFail");
			objectMap.put("errorMessage",errorMessage );
			throw new Exception(errorMessage);
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
     * @param imageWidth
     * @param imageHeight
     * @param strImageUtilityLoc Location of the image magick.
     * @throws Exception
     * @since AppsCommon V6R2008-1
     */

     public void generateImageUtilityThumbnailImagick(Context context,
                                         String strWorkspacePath,
                                         String strSourceFileName,
                                         String strNewFileName,
                                         int imageWidth,
                                         int imageHeight,
                                         String strImageUtilityLoc)
            throws Exception
     {
        java.io.File workingDirectory = new java.io.File(strWorkspacePath);

        boolean bIsPDF = false;

        if (getFileExtension(strSourceFileName).equalsIgnoreCase("pdf")) {
            bIsPDF = true;
        }

        java.io.File newFile = new java.io.File(strWorkspacePath, strNewFileName);
        /* IR-047901V6R2011x  For jpg image
        if (newFile.exists()){
            newFile.delete();
        }
        */

        ArrayList cmdArray = new ArrayList();

        // Build command string (full path to ImageMagick convert utility)
        //
        StringBuffer strCmd = new StringBuffer(128);
        strCmd.append(strImageUtilityLoc);
        strCmd.append(java.io.File.separatorChar);
        strCmd.append("convert");

        cmdArray.add("\""+strCmd.toString()+"\"");

        // Build argument for input file (input file name and path)
        //
        StringBuffer strInputFile = new StringBuffer(128);
        strInputFile.append(strWorkspacePath);
        strInputFile.append(java.io.File.separatorChar);
        strInputFile.append(strSourceFileName);
        if (bIsPDF) {
            strInputFile.append("[0]");
        }

        cmdArray.add("\""+strInputFile.toString()+"\"");

        // Build thumbnail option (for generating various size images)
        //
        if(imageWidth >=0 && imageHeight >= 0) {
            cmdArray.add("-thumbnail");
            cmdArray.add(imageWidth + "x" + imageHeight);
        }

        // Build output string (output file name and path)
        //
        StringBuffer strOutputFile = new StringBuffer(128);
        strOutputFile.append(strWorkspacePath);
        strOutputFile.append(java.io.File.separatorChar);
        strOutputFile.append(strNewFileName);

        cmdArray.add("\""+strOutputFile.toString()+"\"");

        // Need to pass string array to exec
        //
        boolean bSucess = false;
        String[] cmds = (String[])cmdArray.toArray(new String[] {});
        try{
            bSucess = startImageConversionProcess(context, cmds, workingDirectory);
        }
        // A non windows operating system throws IOException if path or source/destination directory are quoted
        //That exception is caught for handling the strings without quotations
        catch(IOException e)
        {
            cmdArray.clear();
            cmdArray.add(strCmd.toString());
            cmdArray.add(strInputFile.toString());
            if(imageWidth >=0 && imageHeight >= 0) {
                cmdArray.add("-thumbnail");
                cmdArray.add(imageWidth + "x" + imageHeight);
            }
            cmdArray.add(strOutputFile.toString());
            cmds = (String[])cmdArray.toArray(new String[] {});
            bSucess = startImageConversionProcess(context, cmds, workingDirectory);
        }

        if (!bSucess) {
            String errorMessage = 
            	EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Image.ImageConversionFail");
            objectMap.put("errorMessage",errorMessage );
            throw new Exception(errorMessage);
        }
    }

    /**
     * This method is used to generate the thumbnail from the image
     * file using nConvert.
     *
     * @param context The ematrix context of the request.
     * @param strWorkspacePath The wordspace path.
     * @param strSourceFileName Source file name.
     * @param strNewFileName Thumbnail file name.
     * @param imageWidth
     * @param imageHeight
     * @param strImageUtilityLoc Location of the image magick.
     * @throws Exception
     * @since AppsCommon V6R2008-1
     */
     public void generateImageUtilityThumbnailnConvert(Context context,
                                         String strWorkspacePath,
                                         String strSourceFileName,
                                         String strNewFileName,
                                         int imageWidth,
                                         int imageHeight,
                                         String strImageUtilityLoc)
            throws Exception
    {
        java.io.File workingDirectory = new java.io.File(strWorkspacePath);


        String fileExt = getFileExtension(strSourceFileName);
        //Fix Image error on JPG - rfischer - 051509
        String strImageType = getFileExtension(strNewFileName).toLowerCase();

        if ("jpg".equalsIgnoreCase(strImageType)) {
          strImageType = "jpeg";
        }

        java.io.File newFile = new java.io.File(strWorkspacePath, strNewFileName);

       /* IR-047901V6R2011x  For jpg image
        if (newFile.exists()){
            newFile.delete();
        }
        */

        StringList command = new StringList();
        StringList commandArgs = new StringList();

        strImageUtilityLoc = addSeparator(strImageUtilityLoc);

        command.add("\"" + strImageUtilityLoc + "\"nconvert\"");

        commandArgs.add("-out");
        commandArgs.add(strImageType);

        if ("gif".equalsIgnoreCase(strImageType))
        {
            commandArgs.add("-colors");
            commandArgs.add("256");
        } else {
            commandArgs.add("-truecolors");
        }

        if(imageWidth >=0 && imageHeight >= 0)
        {
            String height = Integer.toString(imageHeight);
            String width = Integer.toString(imageWidth);
            commandArgs.add("-bgcolor");
            commandArgs.add("255");
            commandArgs.add("255");
            commandArgs.add("255");
            commandArgs.add("-ratio");
            commandArgs.add("-resize");
            commandArgs.add(width);
            commandArgs.add(height);
            commandArgs.add("-canvas");
            commandArgs.add(width);
            commandArgs.add(height);
            commandArgs.add("center");
        }

        commandArgs.add("-o");
        command.addAll(commandArgs);
        command.add("\"" + strWorkspacePath + java.io.File.separatorChar + strNewFileName + "\"");
        command.add("\"" + strWorkspacePath + java.io.File.separatorChar + strSourceFileName + "\"");
        boolean bSucess = false;
        try
        {
            bSucess = startImageConversionProcess(context, (String[])command.toArray(new String []{}), workingDirectory);
        }
        catch(IOException e)
        {
            // A non windows operating system throws IOException if path or source/destination directory are quoted
            //That exception is caught for handling the strings without quotations
            command.clear();
            command.add(strImageUtilityLoc + "nconvert");
            command.addAll(commandArgs);
            command.add(strWorkspacePath + java.io.File.separatorChar + strNewFileName);
            command.add(strWorkspacePath + java.io.File.separatorChar + strSourceFileName);
            bSucess = startImageConversionProcess(context, (String[])command.toArray(new String []{}), workingDirectory);
        }
        if (!bSucess) {
            String errorMessage = 
            	EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(context.getSession().getLanguage()),"emxComponents.Image.ImageConversionFail");
            objectMap.put("errorMessage",errorMessage );
            throw new Exception(errorMessage);
        }
    }


     private boolean startImageConversionProcess(Context context, String[] commandArray, File workingDir) throws IOException {
         Process proc = null;
         try {
             int intervals = 0;
             int maxIntervals = 600;
             try {
                 maxIntervals = Integer.parseInt(EnoviaResourceBundle.getProperty(context, "emxComponents.ImageManager.MaxTimeToWaitForImageConversion")) / 100;
             } catch (Exception e) {}
             maxIntervals = maxIntervals <= 0 ? 600 : maxIntervals;
             //Do check whether the process has completed succssfully or not,
             //run this check for maxIntervals, sleep the thread for 600ms in each interval
             proc = Runtime.getRuntime().exec(commandArray, null, workingDir);

             while (intervals < maxIntervals) {
                 try {
                     //Making this thread to sleep for 100ms, to process the image conversion.
                     try { Thread.sleep(100);} catch (InterruptedException e1) {}
                     //Process completed and its sucsses
                     //If the process still continuing exitValue throws IllegalThreadStateException
                     //If the exit code is 0 process completed sucssesfully else there is some error
                     return (proc.exitValue() == 0);
                 } catch (IllegalThreadStateException e) {
                     //Process still not completed increase the intervals value by 1
                     intervals++;
                 }
             }
         } catch (IOException e) {
             throw e;
         } finally {
             if(proc != null) {
                 proc.destroy(); // Destroy the process finally even if completed or not.
    }
         }
         return false; // If the process is not completed within time just return false.
     }
    // nconvert change end

    /**
     * This method is used to get the base name of the file from the
     * complete file name.
     *
     * @param context The ematrix context of the request.
     * @param strFileName Thie complete name of the file.
     * @return Base file name.
     * @since AppsCommon 10.6.SP1
     */
    static public String getFileBaseName(String strFileName) {
        int index = strFileName.lastIndexOf('.');

        if (index == -1) {
            return strFileName;
        } else {
            return strFileName.substring(0, index);
        }
    }

    /**
     * This method is used to get the extension of the file from the
     * complete file name.
     *
     * @param context The ematrix context of the request.
     * @param strFileName Thie complete name of the file.
     * @return file extension.
     * @since AppsCommon 10.6.SP1
     */
    static public String getFileExtension(String strFileName) {
        int index = strFileName.lastIndexOf('.');

        if (index == -1) {
            return strFileName;
        } else {
            return strFileName.substring(index + 1, strFileName.length());
        }
    }
    /**
     * This method is used copy the images on clone
     *
     * @param context The ematrix context of the request.
     * @param args holds context object id,new clone name,new close revision,vault
     * @return nothing
     * @since AppsCommon
     */
    public void copyImagesOnClone(Context context, String[] args) throws Exception
    {
        try
        {
            String objectId = args[0];
            String newName = args[1];
            String newRev = args[2];
            String vault = args[3];

            if(objectId == null || objectId.equals("null") || objectId.equals("") ||
               newName == null || newName.equals("null") || newName.equals("") ||
               newRev == null || newRev.equals("null") || newRev.equals("") ) {
                   return;
            }

            DomainObject object = DomainObject.newInstance(context, objectId);
            com.matrixone.apps.domain.Image image = object.getImageObject(context);
            if(image == null || image.equals("null")) {
                return;
            }
            image = new com.matrixone.apps.domain.Image(image.cloneObject(context, getUniqueName(context)));
            BusinessObject busObj = new BusinessObject(object.getInfo(context,SELECT_TYPE), newName, newRev, vault);
            String newObjectId = busObj.getObjectId(context);
            image.addToObject(context, new RelationshipType(image.RELATIONSHIP_IMAGE_HOLDER), newObjectId);

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * This method is used copy the images on revise
     *
     * @param context The ematrix context of the request.
     * @param args holds context object id
     * @return nothing
     * @since AppsCommon
     */
    public void copyImagesOnRevise(Context context, String[] args) throws Exception
    {
        try
        {
            String objectId = args[0];
            if(objectId == null || objectId.equals("null")) {
                return;
            }
            DomainObject object = DomainObject.newInstance(context, objectId);
            if(object == null || object.equals("null")) {
                return;
            }
            com.matrixone.apps.domain.Image image = object.getImageObject(context);
            if(image == null || image.equals("null")) {
                return;
            }
            image = new com.matrixone.apps.domain.Image(image.cloneObject(context, getUniqueName(context)));
            if(image == null || image.equals("null")) {
                return;
            }
            String newObjectId = object.getInfo(context, "next.id");
            if(newObjectId == null || newObjectId.equals("null")) {
                return;
            }
            image.addToObject(context, new RelationshipType(image.RELATIONSHIP_IMAGE_HOLDER), newObjectId);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }
    /**
     * This method is used to get the image thumbnailURLs for the Images in Thumbnail format
     *
     * @param context The ematrix context of the request.
     * @param Packed arguments which has objectList, paramlist and imagedata
     * @return Vector containing ThumbnailURLs
     * @since AppsCommon
     */
    public Vector getImageURLs(Context context, String[] args) throws Exception {
        Vector finalImageURLs = new Vector();

        StringList selects = new StringList(7);
        selects.add(SELECT_IMAGE_HOLDER_ID);
        selects.add(SELECT_IMAGE_HOLDER_PRIMARY_IMAGE_NAME);
        selects.add(SELECT_PRIMARY_IMAGE_ID);
        selects.add(SELECT_IMAGE_PRIMARY_IMAGE_NAME);
        selects.add(SELECT_ID);
        selects.add(SELECT_TYPE);
		selects.add(com.matrixone.apps.domain.Image.SELECT_HAS_MODIFY_ACCESS);
		selects.add("from["+RELATIONSHIP_PARTSPECIFICATION+"].to.type");

        StringList imageSelects = new StringList(6);
        imageSelects.add(SELECT_ATTRIBUTE_PRIMARY_IMAGE_FROM_ALTPATH);
        imageSelects.add(SELECT_ATTRIBUTE_TRAVERSE_ALTPATH);
        imageSelects.add(SELECT_MX_SMALL_IMAGE_FILE_NAMES);
        imageSelects.add(SELECT_3DXML_FORMAT_FILES);
        imageSelects.add(SELECT_GENERIC_FORMAT_FILES);

        DomainConstants.MULTI_VALUE_LIST.add(SELECT_3DXML_FORMAT_FILES);
        DomainConstants.MULTI_VALUE_LIST.add(SELECT_GENERIC_FORMAT_FILES);

        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            boolean generateHTML = programMap.get(UIComponent.IMAGE_MANAGER_GENERATE_HTML_FLAG) == null ||
                                   "true".equalsIgnoreCase((String) programMap.get(UIComponent.IMAGE_MANAGER_GENERATE_HTML_FLAG));
            MapList objectList = (MapList)programMap.get("objectList");
            HashMap requestMap = (HashMap)programMap.get("paramList");

            HashMap imageData = (HashMap)requestMap.get("ImageData");
            HashMap fieldMap = (HashMap)programMap.get("fieldMap");
            fieldMap = fieldMap == null ? (HashMap)programMap.get("columnMap") : fieldMap;

            String format = PropertyUtil.getSchemaProperty(context,(String)programMap.get("format"));
            String imageSize = PropertyUtil.getAdminProperty(context, "format", format, "mxImageSize");
            imageSelects.add("format["+format+"].file.name");

            String oidsArray[] = new String[objectList.size()];
            StringList slAlthPathValues = new StringList();
            DomainObject domImageObj = null;
            String parentType = "";
            StringList slChildTypesList = new StringList();
            for (int i = 0; i < objectList.size(); i++)  {
                oidsArray[i] = (String)((Map)objectList.get(i)).get("id");
                parentType = (String)((Map)objectList.get(i)).get("type");
                if (UIUtil.isNullOrEmpty(parentType)) {
                	domImageObj = new DomainObject(oidsArray[i]);
                	parentType = domImageObj.getType(context);
                }
                if(!slChildTypesList.contains(parentType)){
                	slAlthPathValues = getAltPathDefinitions(context, parentType);
                	slChildTypesList.add(parentType);
                	selects.addAll(slAlthPathValues);
                }
            }

            MapList objListMaplist = DomainObject.getInfo(context, oidsArray, selects);


            String imageHolderNullIndexArray[] = new String[objListMaplist.size()];
            StringList slImageHolderObjId = new StringList();
            for (int j = 0; j < objListMaplist.size(); j++) {
                Map objMap1 = (Map) objListMaplist.get(j);
                String imageHolderId = (String)objMap1.get(SELECT_IMAGE_HOLDER_ID);
                if(imageHolderId != null && !"#DENIED!".equals(imageHolderId)) {
                    slImageHolderObjId.add(imageHolderId);
                    imageHolderNullIndexArray[j]="";
                } else{
                    imageHolderNullIndexArray[j]=null;
                }
            }
            String imgHolderObjArray[] = new String[slImageHolderObjId.size()];
            slImageHolderObjId.toArray(imgHolderObjArray);
            MapList imgHolderObjMaplist = DomainObject.getInfo(context, imgHolderObjArray, imageSelects);

            // To maintain the same sequence of the elements in imgHolderObjMaplist with respect to objListMaplist
            // fill the remaining place with null fo which there is no imageholder object connected.
            for (int k = 0; k < imageHolderNullIndexArray.length; k++) {
                if(imageHolderNullIndexArray[k] == null) {
                    imgHolderObjMaplist.add(k, imageHolderNullIndexArray[k]);
                }
            }

            StringList primaryImageNames = new StringList();
            StringList allCADImages = new StringList();
            StringList objectTypeList = new StringList();
            StringList prdImageUrls=new StringList();
            ArrayList bopArrayList=new ArrayList();
            ArrayList indexList=new ArrayList();
            String imageHolderId = "";
            String primaryImageId = "";
            String fileName = "";
            boolean CADImagesFlag;
            boolean hasReadAccess;
            Map imgData = null;
            String primImgFromAltPath = "";
            String traverseAltPath = "";
            String specType = "";
            HashMap altMap = null;
            Map objMap = null;
            for (int i = 0; i < objListMaplist.size(); i++) {
                objMap = (Map) objListMaplist.get(i);
                specType = (String)objMap.get("from["+RELATIONSHIP_PARTSPECIFICATION+"].to.type");
                objectTypeList.add(objMap.get(SELECT_TYPE));
                 CADImagesFlag = false;
                 imageHolderId = (String)objMap.get(SELECT_IMAGE_HOLDER_ID);
                 primaryImageId = (String)objMap.get(DomainObject.SELECT_PRIMARY_IMAGE_ID);
                 fileName = (String) objMap.get(SELECT_IMAGE_HOLDER_PRIMARY_IMAGE_NAME);
                 hasReadAccess = !"#DENIED!".equals(imageHolderId);
                if(imageHolderId != null && hasReadAccess) {
                    // added for 3d Xml plyaer integration
                    imgData =(Map)imgHolderObjMaplist.get(i);
                     primImgFromAltPath =(String)imgData.get(SELECT_ATTRIBUTE_PRIMARY_IMAGE_FROM_ALTPATH);
                     traverseAltPath = (String)imgData.get(SELECT_ATTRIBUTE_TRAVERSE_ALTPATH);
                    //Primary Image is from altpath
                    if("Yes".equalsIgnoreCase(primImgFromAltPath)) {
                        if("Yes".equalsIgnoreCase(traverseAltPath)) {
                            altMap = getImagesFromAltPath(context, oidsArray[i], fileName, imageData, true, objMap,specType);
                            String tempString = (String)altMap.get("fcsurl");
                            if(tempString != null && !"".equals(tempString.trim())) {
                                fileName=(String)altMap.get("fileName");
                                prdImageUrls.add((String)altMap.get("fcsurl"));
                                allCADImages.add(fileName);
                            } else {
                                 //Handling deleted AltPath Primary Image situations
                                 fileName="";
                                StringList smallImageFileNames;
                                StringList genericFormatImageNames;
                                Object obj = imgData.get(DomainObject.SELECT_MX_SMALL_IMAGE_FILE_NAMES);
                                if(obj instanceof String) {
                                    smallImageFileNames = new StringList((String)imgData.get(DomainObject.SELECT_MX_SMALL_IMAGE_FILE_NAMES));
                                    genericFormatImageNames = new StringList((String)imgData.get(SELECT_GENERIC_FORMAT_FILES));
                                } else if(obj instanceof StringList) {
                                    smallImageFileNames = new StringList((StringList)imgData.get(DomainObject.SELECT_MX_SMALL_IMAGE_FILE_NAMES));
                                    genericFormatImageNames =new StringList((StringList)imgData.get(SELECT_GENERIC_FORMAT_FILES));
                                } else {
                                    smallImageFileNames = new StringList();
                                    genericFormatImageNames = new StringList();
                                }

                                if ( genericFormatImageNames != null && genericFormatImageNames.size() >0) {
                                    try{
                                        fileName =(String)genericFormatImageNames.get(0);
                                    } catch(Exception e) {
                                        fileName="";
                                    }
                                }
                                if(!UIUtil.isNullOrEmpty(fileName))  {
                                    tempString = searchFileBaseNameIgnoreCase(smallImageFileNames, fileName, "jpg");
                                    if(tempString != null) {
                                        BusinessObjectProxy bop = new BusinessObjectProxy(imageHolderId, format, tempString, false, false);
                                        bopArrayList.add(bop);
                                        indexList.add(i);
                                    } else {
                                        prdImageUrls.add(defaultThumbnailUrl);
                                    }

                                    //Updating ImageHolder Attributes
                                    tempString=getFileExtension(fileName);
                                    if("3dxml".equalsIgnoreCase(tempString) || "cgr".equalsIgnoreCase(tempString)) {
                                       allCADImages.add(fileName);
                                    }
                                } else {
                                  altMap=getImagesFromAltPath (context, oidsArray[i], fileName, imageData,true,objMap,specType);
                                  prdImageUrls.add((String)altMap.get("fcsurl"));
                                  fileName=(String)altMap.get("fileName");
                                  allCADImages.add(fileName);
                                }
                            }
                        } else {
                            StringList smallImageFileNames;
                            StringList genericFormatImageNames;
                            Object obj = imgData.get(DomainObject.SELECT_MX_SMALL_IMAGE_FILE_NAMES);
                            if(obj instanceof String) {
                                smallImageFileNames = new StringList((String)imgData.get(DomainObject.SELECT_MX_SMALL_IMAGE_FILE_NAMES));
                                genericFormatImageNames = new StringList((String)imgData.get(SELECT_GENERIC_FORMAT_FILES));
                            } else if(obj instanceof StringList) {
                                smallImageFileNames = new StringList((StringList)imgData.get(DomainObject.SELECT_MX_SMALL_IMAGE_FILE_NAMES));
                                genericFormatImageNames =new StringList((StringList)imgData.get(SELECT_GENERIC_FORMAT_FILES));
                            } else {
                                smallImageFileNames = new StringList();
                                genericFormatImageNames = new StringList();
                            }

                            if ( genericFormatImageNames != null && genericFormatImageNames.size() >0) {
                               try{
                                    fileName =(String)genericFormatImageNames.get(0);
                                } catch(Exception e) {
                                        fileName="";
                                    }
                                if(!UIUtil.isNullOrEmpty(fileName)) {
                                    String tempString = searchFileBaseNameIgnoreCase(smallImageFileNames,fileName,"jpg");
                                    if(tempString!=null) {
                                        BusinessObjectProxy bop = new BusinessObjectProxy(imageHolderId, format, tempString, false, false);
                                        bopArrayList.add(bop);
                                        indexList.add(i);
                                    } else {
                                        prdImageUrls.add(defaultThumbnailUrl);
                                     }

                                     tempString=getFileExtension(fileName);
                                    if("cgr".equalsIgnoreCase(tempString) || "3dxml".equalsIgnoreCase(tempString)) {
                                        fileName=getFileBaseName(fileName)+".3dxml";
                                        allCADImages.add(fileName);
                                     }
                                 }
                              }
                        }
                    } else {
                        if(!UIUtil.isNullOrEmpty(fileName)) {
                            // (Primary Image)fileNmae != "" means objects in the Holder Object.
                            StringList tempStringList;
                            String tempString;
                            Object obj = imgData.get("format["+format+"].file.name");
                            if (obj instanceof String) {
                                tempStringList = new StringList((String)imgData.get("format["+format+"].file.name"));
                            } else if(obj instanceof StringList) {
                                tempStringList = new StringList((StringList)imgData.get("format["+format+"].file.name"));
                            } else {
                                tempStringList = new StringList();
                            }
                            tempString = searchFileBaseNameIgnoreCase(tempStringList,fileName,"jpg");
                            if(tempString!=null) {
                                BusinessObjectProxy bop = new BusinessObjectProxy(imageHolderId, format, tempString, false, false);
                                bopArrayList.add(bop);
                                indexList.add(i);
                            } else  {
                                prdImageUrls.add(defaultThumbnailUrl);
                            }

                            if("3dxml".equalsIgnoreCase(getFileExtension(fileName))|| (UIUtil.isNotNullAndNotEmpty(specType) && mxType.isOfParentType(context, specType, TYPE_VPM_REFERENCE))) {
                               allCADImages.add(fileName);
                            }
                        } else {
                            CADImagesFlag = "Yes".equalsIgnoreCase(traverseAltPath);
                        }
                    }
                    //till here
                } else if(imageHolderId == null && primaryImageId != null && hasReadAccess) {
                    //This is for product central Images --
                    imageHolderId = (String)objMap.get(DomainObject.SELECT_PRIMARY_IMAGE_ID);
                   fileName = (String)objMap.get(DomainObject.SELECT_IMAGE_PRIMARY_IMAGE_NAME);
                    if (!UIUtil.isNullOrEmpty(fileName)) {
                        BusinessObjectProxy bop = new BusinessObjectProxy(imageHolderId, format, fileName, false, false);
                        bopArrayList.add(bop);
                        indexList.add(i);
                   } else {
                       prdImageUrls.add(defaultThumbnailUrl);
                   }
                } else  if(imageHolderId == null ) {
                        CADImagesFlag = true;
                }else if(!hasReadAccess){
                	fileName="";
                	prdImageUrls.add("");
                }
                if(CADImagesFlag) {
                    //No image holder exist them get First image from Alt path as primary image
                    fileName="";
                    altMap=getImagesFromAltPath (context, oidsArray[i], fileName, imageData,true, objMap,specType);
                    prdImageUrls.add((String)altMap.get("fcsurl"));
                    fileName=(String)altMap.get("fileName");
                    allCADImages.add(fileName);

                }

                if (!UIUtil.isNullOrEmpty(fileName) && hasReadAccess) {
                    primaryImageNames.add(fileName);
                } else {
                    primaryImageNames.add(null);
                }
            }

            String href = (String) programMap.get("href");
            if(!UIUtil.isNullOrEmpty(href)) {
                StringBuffer strbuffer=new StringBuffer(href);
                strbuffer.append((href.indexOf('?') == -1 ? '?' : '&'));
                strbuffer.append("objectId=");
                href = strbuffer.toString();
                }
            String imageViewerURL = !UIUtil.isNullOrEmpty(href) ? encodeURL(href) :
                                    "../components/emxImageManager.jsp?HelpMarker=emxhelpimagesview&amp;objectId=";

            int count = 0;
            if(! bopArrayList.isEmpty()){
                String[] tmpImageUrls = ImageRequestData.getImageURLS(context, bopArrayList, imageData);
                for (int i = 0; i < tmpImageUrls.length; i++) {
                    prdImageUrls.insertElementAt(tmpImageUrls[i], (Integer)indexList.get(i));
                }
            }

            String[] fcsImageURLs = new String[prdImageUrls.size()];
            prdImageUrls.toArray(fcsImageURLs);

            Iterator imageItr = primaryImageNames.iterator();
            while( imageItr.hasNext()) {
                String imageViewerLink = imageViewerURL + oidsArray[count];
                finalImageURLs.add(getImageColumnInfo(context, imageSize, allCADImages, fcsImageURLs[count], count, (String)imageItr.next(), oidsArray[count], imageViewerLink, generateHTML, (String)objectTypeList.get(count),specType));
                count ++;
            }
            return  finalImageURLs;
        } catch(Exception ex ) {
            ex.printStackTrace();
            throw ex;
            }
        finally {
            synchronized(DomainConstants.MULTI_VALUE_LIST) {
                DomainConstants.MULTI_VALUE_LIST.remove(SELECT_3DXML_FORMAT_FILES);
                DomainConstants.MULTI_VALUE_LIST.remove(SELECT_GENERIC_FORMAT_FILES);
                }
            }
                }

    protected Object getImageColumnInfo(Context context, String imageSize, StringList allCADImages, String fcsImageURL, int count, String imageName, String oid, String imageViewerURL, boolean generateHTML, String objectType, String specType) throws Exception {
        if(UIUtil.isNullOrEmpty(imageName)) {
        	Map imageMap = new HashMap(2);
        	imageMap.put(SELECT_ID, oid);
	        imageMap.put(SELECT_TYPE, objectType);
            return generateHTML ? "" : imageMap;
        }

        
        boolean is3dImage= allCADImages.contains(imageName) && ("3dxml".equals(getFileExtension(imageName))|| "cgr".equals(getFileExtension(imageName) )|| (UIUtil.isNotNullAndNotEmpty(specType) && mxType.isOfParentType(context, specType, TYPE_VPM_REFERENCE)));

        if(generateHTML) {
            StringBuffer strbuffer = new StringBuffer();
            if(is3dImage) {
                strbuffer.append("<table height=\"100%\" border=\"0\" class=\"mx_thumbnail-image\" cellpadding=\"0\" cellspacing=\"1\">");
                strbuffer.append("<tr>");
                strbuffer.append("<td valign=\"top\" class=\"mx_badge-image\">");
                strbuffer.append("<img border=\"0\" src=\"" + ICON_3D_IMAGE + "\" class=\"mx_image-type-badge\" alt=\""+imageName+"\" title=\""+imageName+"\"/>");
                strbuffer.append(" </td><td valign=\"top\" class=\"mx_thumbnail-image\">");
                strbuffer.append("<a href='javascript:showModalDialog(\"");
                strbuffer.append(imageViewerURL);
                strbuffer.append("\",850,650,false,false,\"Large\")'>");
                strbuffer.append("<img border=\"0\" src=\"" + fcsImageURL + "\" height=\"" + imageSize + "\" alt=\""+imageName+"\" title=\""+imageName+"\"/>");
                strbuffer.append("</a>");
                strbuffer.append("</td></tr></table>");
            } else {
                strbuffer.append("<a href='javascript:showModalDialog(\"");
                strbuffer.append(imageViewerURL);
                strbuffer.append("\",850,650,false,false,\"Large\")'>");
                strbuffer.append("<img border=\"0\" src=\"" + fcsImageURL + "\" height=\"" + imageSize + "\" alt=\""+imageName+"\" title=\""+imageName+"\"/>");
                strbuffer.append("</a>");
            }
            return strbuffer.toString();
        } else {
            Map imageMap = new HashMap(10);
            imageMap.put(UIComponent.IMAGE_MANAGER_FILE_NAME, imageName);
            imageMap.put(UIComponent.IMAGE_MANAGER_IMAGE_URL, fcsImageURL);
            imageMap.put(UIComponent.IMAGE_MANAGER_IMAGE_SIZE, imageSize);
            imageMap.put(UIComponent.IMAGE_MANAGER_URL, imageViewerURL);
            imageMap.put(UIComponent.IMAGE_MANAGER_IS3D_IMAGE, is3dImage ? "true" : "false");
            imageMap.put(UIComponent.IMAGE_MANAGER_3D_IMAGE_ICON, ICON_3D_IMAGE);
            imageMap.put(SELECT_TYPE, objectType);
            imageMap.put(SELECT_ID, oid);
            
            return imageMap;
        }

    }
    
    
    
    protected Object getImageColumnInfo(Context context, String imageSize, StringList allCADImages, String fcsImageURL, int count, String imageName, String oid, String imageViewerURL, boolean generateHTML) throws Exception{
    	return getImageColumnInfo(context, imageSize,  allCADImages,  fcsImageURL,  count,  imageName,  oid,  imageViewerURL,  generateHTML , null,null);
    }

    private Map objectMap;

     /**
     * This method is used to get the URLs for the Images in Thumbnail format
     *  and mxImage foramt in addition to the fileNames and Sequence Numbers.
     *
     * @param context The ematrix context of the request.
     * @param Packed arguments which has Context ObjectId, Image Object Id connected via PRIMARY IMAGE Relationship to the Context Object and Imagedata Map
     * @return Map containing ThumbnailURLs and ImageURLs, ImageNames and Sequence Numbers.
     * @since AppsCommon 10.7.SP1
     */
    public Map getImagesFromImageObjects(Context context, String[] args) throws Exception
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strPrimaryImageObjectId = (String) programMap.get("primaryImageId");
            String objectId= (String) programMap.get("objectId");
            HashMap imageData = (HashMap) programMap.get("imageData");
            ArrayList arraylist = new ArrayList();
            ArrayList arraylist1 = new ArrayList();
            StringList imageActualNames = new StringList();
            StringList imageNames = new StringList();
            StringList sequenceOrders = new StringList();
            String strSequenceNumber = "";
            Map imageInfoMap = new HashMap();
            DomainObject object = DomainObject.newInstance(context, objectId);
            if(strPrimaryImageObjectId !=null)
            {

             StringList objselects = new StringList();
             StringList relselects = new StringList();

             objselects.add(DomainConstants.SELECT_ID);
             objselects.add(DomainConstants.SELECT_NAME);
             objselects.add(DomainObject.SELECT_MX_IMAGE_FILE_NAMES);

             relselects.add(DomainConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);

             String strNames = "";
             String strId = "";
             MapList mlImageList = object.getRelatedObjects(context,
                                                          DomainConstants.RELATIONSHIP_IMAGES,
                                                          DomainConstants.TYPE_IMAGE,
                                                          objselects,
                                                          relselects,
                                                          false,
                                                          true,
                                                          (short)1,
                                                          DomainConstants.EMPTY_STRING,
                                                          DomainConstants.EMPTY_STRING);
            BusinessObjectProxy businessobjectproxy;

            String VIEWER_THUMBNAIL_FORMAT = "";
            if("".equals(VIEWER_THUMBNAIL_FORMAT))
            {
                String s1 = EnoviaResourceBundle.getProperty(context,"emxComponents.ImageManager.ViewerThumbnailFormat");
                VIEWER_THUMBNAIL_FORMAT = PropertyUtil.getSchemaProperty(context,s1);
            }

            for(int loopindex=0;loopindex<mlImageList.size();loopindex++)
            {
               strId = (String) ((Map) mlImageList.get(loopindex)).get(DomainConstants.SELECT_ID);
               strNames = (String) ((Map) mlImageList.get(loopindex)).get(DomainObject.SELECT_MX_IMAGE_FILE_NAMES);
               strSequenceNumber = (String) ((Map) mlImageList.get(loopindex)).get(DomainConstants.SELECT_ATTRIBUTE_SEQUENCE_ORDER);
               businessobjectproxy = new BusinessObjectProxy(strId, DomainObject.FORMAT_MX_IMAGE, strNames, false, false);
               arraylist1.add(businessobjectproxy);
               businessobjectproxy = new BusinessObjectProxy(strId, VIEWER_THUMBNAIL_FORMAT, strNames, false, false);
               arraylist.add(businessobjectproxy);
               imageNames.add(strNames);
               strNames = strNames.substring(0, strNames.lastIndexOf("."));
               imageActualNames.add(strNames);
               sequenceOrders.add(strSequenceNumber);
            }

            String asURls[] = ImageRequestData.getImageURLS(context, arraylist1, imageData);
            String asThumbnailURLs[] = ImageRequestData.getImageURLS(context, arraylist, imageData);

            imageInfoMap.put("sequenceOrders",sequenceOrders);
            imageInfoMap.put("imageNames",imageNames);
            imageInfoMap.put("imageActualNames",imageActualNames);
            imageInfoMap.put("ImageURLs", asURls);
            imageInfoMap.put("ThumbnailURLs", asThumbnailURLs);
            }
            return imageInfoMap;
    }



    /**
     *This method is used in the trigger and is called on checkin of the file.
     * Gets the Image Holder Id, Name of the file being checked in and format of the file as input.
     * First the method checks if the format is generic and the file extension is cgr or 3dxml and then it proceeds to execute the * desired logic.
     *
     * @param context The ematrix context of the request.
     * @param args holds ImageHolderId,checkinfilename,format
     * @return nothing
     * @since AppsCommon 10.7.SP1
     */
  public void uploadProcessCADFiles (matrix.db.Context context, String[] args)
    throws Exception
    {

        String imageUtility = EnoviaResourceBundle.getProperty(context,"emxComponents.ImageManager.ImageUtility.Directory");
//            Properties objProp = context.getProperties();
//            String gotMcsURL = (String) objProp.get("MCSUrl");
        String gotMcsURL = MqlUtil.mqlCommand(context,"get env $1 $2", "global", "MCSURL");

        String imageHolderId = args[0];
        String strFileName = args[1];
        String fileFormat = args[2];
        String genericFormat = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_format_generic);
        String fileExtn = getFileExtension(strFileName);

        if ((genericFormat.equalsIgnoreCase(fileFormat) && strFileName != null && !"".equals(strFileName) && !"null".equals(strFileName) && imageHolderId != null && !"".equals(imageHolderId) && !"null".equals(imageHolderId)) && ("3dxml".equalsIgnoreCase(fileExtn) || "cgr".equalsIgnoreCase(fileExtn)))
        {
            String strWorkspacePath = context.createWorkspace();
            String strImageFormat = "jpg";
            StringList fileNames = new StringList(1);
            FileList files = new FileList();
            matrix.db.File file = new matrix.db.File(strFileName, genericFormat);
            files.addElement(file);
            DomainObject object = DomainObject.newInstance(context, imageHolderId);
            object.checkoutFiles(context, false, genericFormat, files, strWorkspacePath);

            StringBuffer sbNewFileName = new StringBuffer();
            sbNewFileName.append(getFileBaseName(strFileName));
            sbNewFileName.append(".");
            sbNewFileName.append(strImageFormat);
            String strNewFileName = sbNewFileName.toString();
            String strFirstFileName = "";
            if("3dxml".equalsIgnoreCase(fileExtn))
            {
                try{
                    strFirstFileName = extractImageFrom3DXMLFile(context, strWorkspacePath, strFileName);
                }catch(Exception e){

                }
                fileNames.addElement(getFileBaseName(strFileName)+".3dxml");
                object.checkinFromServer(context, true, true, PropertyUtil.getSchemaProperty(context,"format_3DXML"), null, fileNames);
            }

            int imgWidth = -1;
            int imgHeight = -1;

            if(strFirstFileName != null && !"".equals(strFirstFileName.trim()))
            {
                StringList extractedFiles = new StringList(1);
                if (!"jpg".equals(getFileExtension(strFirstFileName).toLowerCase()))
                {
                    if (imageUtility != null && !"".equals(imageUtility) && !"null".equals(imageUtility))
                    {
                        generateImageUtilityThumbnail(context, strWorkspacePath,
                                strFirstFileName, strNewFileName, imgWidth, imgHeight, imageUtility);
                    }
                    else
                    {
                        generateImageThumbnail(context, strWorkspacePath,
                                strFirstFileName, strNewFileName, imgWidth, imgHeight, null);
                    }
                    extractedFiles.addElement(strNewFileName);
                    object.checkinFromServer(context, true, true, FORMAT_MX_IMAGE, null, extractedFiles);
                }
                else
                {
                    extractedFiles.addElement(strFirstFileName);
                    object.checkinFromServer(context, true, true, FORMAT_MX_IMAGE, null, extractedFiles);
                }

                List lstFilesToTransform = new ArrayList();
                Hashtable htCCIHInfo = new Hashtable();
                htCCIHInfo.put("Oid", imageHolderId);
                htCCIHInfo.put("File", (String) extractedFiles.get(0));
                htCCIHInfo.put("Format", FORMAT_MX_IMAGE);

                lstFilesToTransform.add(htCCIHInfo);

                new CommonImageConverterRemoteExec().convertImageAndCheckinSameObject(context, gotMcsURL, lstFilesToTransform, null, ".jpg");
            }

/*
            if(strFirstFileName != null && !"".equals(strFirstFileName.trim()))
            {
                checkinFilesInImagesFormats(context, imageHolderId, strWorkspacePath, strFirstFileName, strNewFileName);
            }

            java.io.File tmpFile = new java.io.File(strWorkspacePath, strFileName);
            tmpFile.delete();
            tmpFile = new java.io.File(strWorkspacePath, strFirstFileName);
            tmpFile.delete();
            if (!strFirstFileName.equalsIgnoreCase(strNewFileName))
            {
                tmpFile = new java.io.File(strWorkspacePath, strNewFileName);
                tmpFile.delete();
            }
            if ("cgr".equalsIgnoreCase(fileExtn))
            {
                tmpFile = new java.io.File(strWorkspacePath, getFileBaseName(strFileName)+".3dxml");
                tmpFile.delete();
            }
  */
        }
    }
	private boolean isSolidwork(String iFileName) throws Exception {
		boolean lReturn = false;
        String sFileExtn = getFileExtension( iFileName );
		lReturn = (SOLIDWORKSPRT.equalsIgnoreCase(sFileExtn) || SOLIDWORKSASM.equalsIgnoreCase(sFileExtn) ||  SOLIDWORKSDRW.equalsIgnoreCase(sFileExtn));
		return lReturn;
	}
  
    // AB6 - For CATVIAV5 File
	private boolean isCATIAV5(String iFileName) throws Exception {
		boolean lReturn = false;
        String sFileExtn = getFileExtension( iFileName );
		lReturn = (CATIAV5PART.equalsIgnoreCase(sFileExtn) || CATIAV5MATERIAL.equalsIgnoreCase(sFileExtn) ||  CATIAV5DRAWING.equalsIgnoreCase(sFileExtn)||  CATIAV5PRODUCT.equalsIgnoreCase(sFileExtn)||  CATIAV5SHAPE.equalsIgnoreCase(sFileExtn)||  CATIAV5ANALYSIS.equalsIgnoreCase(sFileExtn)||  CATIAV5SYSTEM.equalsIgnoreCase(sFileExtn) ||  CATIAV5PROCESS.equalsIgnoreCase(sFileExtn));
		return lReturn;
	}
  
  /**
   *This method is used in the trigger and is called on checkin of the file.
   * Gets the Document Id, Name of the file being checked in and format of the file as input.
   * First the method checks if the format is generic and the file extension is ImageHolder Supported Format and then it proceeds to execute the * desired logic.
   *
   * @param context The ematrix context of the request.
   * @param args holds ImageHolderId,strFileName,fileFormat
   * @return nothing
   * @since AppsCommon R216
   */
  
  public void checkinThumbnailImagetoDocument(Context context, String args[]) throws Exception {	  

      String docObjectID = args[0];
      String strFileName = args[1];
      String fileFormat = args[2];
      StringList fileList = new StringList(1);
      String[] supportedFormat =  {strFileName};
	  boolean is3dDrive = _3DDRIVE.equals(PropertyUtil.getEnvironmentProperty(context, "PodDefinitionName"));
      // AB6 - For CATVIAV5 File
	  if(checkImageFormat(context, supportedFormat) || (isSolidwork(strFileName)&& is3dDrive) || (isCATIAV5(strFileName)&& is3dDrive)){ 
      try{
		  String imageUtility = EnoviaResourceBundle.getProperty(context,"emxComponents.ImageManager.ImageUtility.Directory");
      
    	  DomainObject object = DomainObject.newInstance(context, docObjectID);
    	  String strImageFormat = "jpg";
    	      
    	  String genericFormat = PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_format_generic);
    	  String fileExtn = getFileExtension(strFileName);

    	  if ((genericFormat.equalsIgnoreCase(fileFormat) && strFileName != null && !"".equals(strFileName) && !"null".equals(strFileName) && docObjectID != null && !"".equals(docObjectID) && !"null".equals(docObjectID)&& !("3dxml".equalsIgnoreCase(fileExtn) || "cgr".equalsIgnoreCase(fileExtn))))
    	  {
    		  String strWorkspacePath = context.createWorkspace();
    		  FileList files = new FileList();
    		  matrix.db.File file = new matrix.db.File(strFileName, genericFormat);
    		  files.addElement(file);
    		  //to prevent the checkout notification, setting MX_FROM_CHECKIN env var which is validated in checkout notification filter
    		  MqlUtil.mqlCommand(context, "set env $1  $2", "MX_FROM_CHECKIN", "true");
    		  object.checkoutFiles(context, false, genericFormat, files, strWorkspacePath);
    		  MqlUtil.mqlCommand(context, "unset env $1", "MX_FROM_CHECKIN");
    		  String strNewFileName = ImageManagerUtil.getFilewithextension(context, getFileBaseName(strFileName),".", strImageFormat);   
    		  strNewFileName=XSSUtil.encodeForURL(context, strNewFileName);
    		  deletePreviousVersionThumbnailImages(context,docObjectID, strNewFileName);
    			  
    		  String sourceFileName =ImageManagerUtil.getCompletePath(context, strWorkspacePath, strFileName);
    		  String format = FORMAT_MX_MEDIUM_IMAGE;
    		  String strImageHeight = PropertyUtil.getAdminProperty(context, "format", format, "mxImageSize");
    		  if (strImageHeight != null && !"".equals(strImageHeight))
    		  {
    			  strImageHeight = strImageHeight.toLowerCase();
    			  if (strImageHeight.indexOf("x") > 0)
    			  {
    				  strImageHeight = strImageHeight.substring(strImageHeight.indexOf("x")+1, strImageHeight.length());
    			  }
    				  
    			  Image image = loadImage(sourceFileName);
    			  int imageHeight = Integer.parseInt(strImageHeight);    				  
    			  int imageWidth = ImageManagerUtil.getImageHeightAndWidth(context, imageHeight,image );        	    	       	    			
    			  
    			  Long timeStamp = System.currentTimeMillis();
				  String filename = getFileBaseName(strFileName)+timeStamp;
				  String tempFileName = ImageManagerUtil.getFilewithextension(context, filename, ".", fileExtn);
				  java.io.File tmpFile = new java.io.File(strWorkspacePath, strFileName);
			      tmpFile.renameTo(new java.io.File(strWorkspacePath, tempFileName));
    			 // AB6 - For CATVIAV5 File
				 if (isCATIAV5(strFileName))
    			  {
							String swxImageUtility = EnoviaResourceBundle.getProperty(context,"emxComponents.CATIAV5.ImageUtility.Directory");
							  generateImageUtilityThumbnailCATIAV5(context,
		                                          strWorkspacePath,
		                                          tempFileName,
		                                          strNewFileName,
		                                          imageWidth,
		                                          imageHeight,
		                                          swxImageUtility);
    			  
		    				  tmpFile = new java.io.File(strWorkspacePath,tempFileName);
		    				  tmpFile.delete();
						}
						else if (isSolidwork(strFileName))
    			  {
							String swxImageUtility = EnoviaResourceBundle.getProperty(context,"emxComponents.SolidWorks.ImageUtility.Directory");
							  generateImageUtilityThumbnailswx(context,
		                                          strWorkspacePath,
		                                          tempFileName,
		                                          strNewFileName,
		                                          imageWidth,
		                                          imageHeight,
		                                          swxImageUtility);
		    				  
		    				  tmpFile = new java.io.File(strWorkspacePath,tempFileName);
		    				  tmpFile.delete();
						}
						else if (imageUtility != null && !"".equals(imageUtility) && !"null".equals(imageUtility))
    			  {
    				  generateImageUtilityThumbnail(context, strWorkspacePath, tempFileName,
        	    				strNewFileName, imageWidth, imageHeight, imageUtility);
    				  
    				  tmpFile = new java.io.File(strWorkspacePath,tempFileName);
    				  tmpFile.delete();    				  
    			  }
    			  else
    			  {
    				  image = loadImage(sourceFileName);
    				  int imgWidth = image.getWidth(null);
    				  int imgHeight = image.getHeight(null); 
        	    			
    				  String scaledImageName = redrawOriginalImage(context, strWorkspacePath, tempFileName,
        	    						strNewFileName, imgWidth, imgHeight, image);        	    				
    				  //Delete the original File from the workspace folder
    				  tmpFile = new java.io.File(strWorkspacePath, tempFileName);
        		      tmpFile.delete();	  
    				  sourceFileName = ImageManagerUtil.getCompletePath(context, strWorkspacePath, scaledImageName);
    				  image = loadImage(sourceFileName);
    				  imageWidth = ImageManagerUtil.getImageHeightAndWidth(context, imageHeight, image);
        	    			
    				  generateImageThumbnail(context, strWorkspacePath, scaledImageName,
    							  strNewFileName, imageWidth, imageHeight, image, true);
    				  //Delete the scaled Images from the Workspace folder
    				  tmpFile = new java.io.File(strWorkspacePath, scaledImageName);
        		      tmpFile.delete();
    			  }
    			  fileList.add(strNewFileName);
    			  object.checkinFromServer(context, true, true, format, null, fileList);
    				  tmpFile = new java.io.File(strWorkspacePath, strNewFileName);
        		      tmpFile.delete();
    			  }
    	  }
      }catch(Exception e){
    	  e.printStackTrace();
      }
      }
  	
  }

      /**
     *     This method is used to copy the contents from the given
     *  Inputstream to output stream
     *
     * @param InputStream for source,OutputStream for destination
     * @return nothing
     * @since AppsCommon
     */

    public static void copyFiles(InputStream inStr, OutputStream outStr)
    throws IOException
    {
        byte[] buffer = new byte[1024];
        int len;
        while((len = inStr.read(buffer)) >= 0)
        {
            outStr.write(buffer, 0, len);
        }
        inStr.close();
        outStr.close();
    }



      /**
     *     This method is called in "uploadProcessCADFiles" method i.e. the trigger method.
     *  It gets the name of the 3dxml file and the path where the file exists as input.
     *  It extracts the image file from the 3dxml file if any exists, and copies it to
     *  the workspace path and returns the name of the copied file.
     *
     * @param String workspace path,String filename of the 3dxml
     * @return extraced image name from 3dxml image
     * @since AppsCommon 10.7.SP1
     * @deprecated since V6R2014 for Function_026045. Use extractImageFrom3DXMLFile(Context context, String workspacePath, String fileName)
     */
    public static String extractImageFrom3DXMLFile(String workspacePath, String fileName)
    throws Exception {
    	return extractImageFrom3DXMLFile(null, workspacePath, fileName);
    }
    
    
    /**
     *  This method is called in "checkinThumbnailImagetoDocument" method i.e. the trigger method.
     *  It gets the name of the Image file and the path where the file exists as input.
     *  It extracts the image file from the file and scales down to 0.80% of the Image and copies it to 
     *  the workspace path and returns the name of the copied file.
     * @param String strWorkspacePath,String filename of the Image
     * @return extracted image name from Original image
     * @since AppsCommon R216
     */
    
	private String redrawOriginalImage(Context context, String strWorkspacePath,
			String strSourceFileName, String strNewFileName, int imageWidth,
			int imageHeight, Image image) throws Exception {
		try{
			String newFilePath = "";
			Long timeStamp = System.currentTimeMillis()+1;
			String filename = getFileBaseName(strNewFileName)+timeStamp;
				strNewFileName = ImageManagerUtil.getFilewithextension(context, filename, ".", "jpg");
				newFilePath = ImageManagerUtil.getCompletePath(context, strWorkspacePath, strNewFileName);
    		
			for(int count=0;count<5;count++){
    		
				generateImageThumbnail( context,  strWorkspacePath, strSourceFileName,  strNewFileName, imageWidth,  imageHeight,  null, true);
				image = loadImage(newFilePath);
				int imgHeight = image.getHeight(null);
				int imgWidth = image.getWidth(null);
				double imageHeight1 =  imageHeight * 0.8;
				if(imageHeight1 <100){
					break;
				}
				imageHeight = (int)imageHeight1;
				double imgRatio = (double)imgWidth / (double)imgHeight;
				long lSize = Math.round(imgRatio * (imageHeight));
				imageWidth = (new Long(lSize)).intValue();
				
				strSourceFileName = strNewFileName;
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
			throw ex;
		}
    	
		return strNewFileName;
	}


    /**
     *  This method is called in "uploadProcessCADFiles" method i.e. the trigger method.
     *  It converts any image file which is obtained as input to the .jpg file and also
     *  converts it in different formats like thumbnail, small, medium etc. and then checks
     * in those files in the respective formats.
     *
     * @param context The ematrix context of the request.
     * @param String imageholder object id,String workspace path,String childfilename,String newfilename
     * @return nothing
     * @ Checkin the thmnbnail format files to object
     * @since AppsCommon
     */


    public void checkinFilesInImagesFormats (Context context, String imageHolderId,
        String strWorkspacePath, String strChildFileName, String sbNewFileName) throws Exception
    {
        DomainObject object = DomainObject.newInstance(context, imageHolderId);
        StringList fileList = new StringList(1);
        String policy = object.getInfo(context, "policy");
        String formats = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3", policy, "format", "|");
        String strImageFormat = "jpg";
        StringList formatList = FrameworkUtil.split(formats, "|");
        String imageUtility = EnoviaResourceBundle.getProperty(context,"emxComponents.ImageManager.ImageUtility.Directory");
        fileList.add(sbNewFileName);

        StringBuffer sbPrimaryFileName = new StringBuffer(getFileBaseName(strChildFileName));
        sbPrimaryFileName.append("_primary.");
        sbPrimaryFileName.append(strImageFormat);

        java.io.File tmpFile = new java.io.File(strWorkspacePath, strChildFileName);
        int imgWidth = -1;
        int imgHeight = -1;

        //Fix Image error on JPG - rfischer - 051509
        if (!"jpg".equals(getFileExtension(strChildFileName).toLowerCase()))
        {
            if (imageUtility != null && !"".equals(imageUtility) && !"null".equals(imageUtility))
            {
                generateImageUtilityThumbnail(context, strWorkspacePath,
                strChildFileName, sbNewFileName, imgWidth, imgHeight, imageUtility);
            }
            else
            {
                generateImageThumbnail(context, strWorkspacePath,
                strChildFileName, sbNewFileName, imgWidth, imgHeight, null);
            }
            object.checkinFromServer(context, true, true, FORMAT_MX_IMAGE, null, fileList);
        }
        else
        {
            object.checkinFromServer(context, true, true, FORMAT_MX_IMAGE, null, fileList);
        }

        tmpFile = new java.io.File(strWorkspacePath, sbNewFileName);
        tmpFile.renameTo(new java.io.File(strWorkspacePath, sbPrimaryFileName.toString()));

        StringBuffer sourceFileName = new StringBuffer(150);
        sourceFileName.append(strWorkspacePath);
        sourceFileName.append(java.io.File.separatorChar);
        sourceFileName.append(sbPrimaryFileName.toString());
        Image image = loadImage(sourceFileName.toString());
        imgWidth = image.getWidth(null);
        imgHeight = image.getHeight(null);

        Iterator formatItr = formatList.iterator();
        while(formatItr.hasNext())
        {
            String format = (String)formatItr.next();
            if (!format.equals(FORMAT_MX_IMAGE) && !format.equals(PropertyUtil.getSchemaProperty(context,"format_3DXML")))
            {
                String strImageHeight = PropertyUtil.getAdminProperty(context, "format", format, "mxImageSize");
                if (strImageHeight != null && !"".equals(strImageHeight))
                {
                    strImageHeight = strImageHeight.toLowerCase();
                    if (strImageHeight.indexOf("x") > 0)
                    {
                        strImageHeight = strImageHeight.substring(strImageHeight.indexOf("x")+1, strImageHeight.length());
                    }
                    int imageHeight = Integer.parseInt(strImageHeight);
                    double imgRatio = (double)imgWidth / (double)imgHeight;
                    long lSize = Math.round(imgRatio * (imageHeight));
                    int imageWidth = (new Long(lSize)).intValue();
                    if (imageUtility != null && !"".equals(imageUtility) && !"null".equals(imageUtility))
                    {
                        generateImageUtilityThumbnail(context, strWorkspacePath, sbPrimaryFileName.toString(),
                        sbNewFileName, imageWidth, imageHeight, imageUtility);
                    }
                    else
                    {
                        generateImageThumbnail(context, strWorkspacePath, sbPrimaryFileName.toString(),
                        sbNewFileName, imageWidth, imageHeight, image);
                    }
                    object.checkinFromServer(context, true, true, format, null, fileList);
                }
            }
        }
        tmpFile = new java.io.File(strWorkspacePath, sbPrimaryFileName.toString());
        tmpFile.delete();
    }

    /**
     * This method is used to get thumbnail urls, Image urls of the alt path images (cgr and 3dxml)
     *  to support the 3dxml integration feature.
     *
     * @param context The ematrix context of the request.
     * @param Packed arguments which has Context ObjectId,imageData using UINavigatorUtil.getImageData(context,pageContext)
     * @return Map containing Thumbnailurls,imageurls,modifyaccess on the parent object.
     * @since AppsCommon 10.7.SP1
     */
    public HashMap getAltPathImages(Context context, String[] args)throws Exception
    {

        ArrayList args1=(ArrayList)JPO.unpackArgs(args);
        String parentId = (String)args1.get(0);
        HashMap imageData= (HashMap)args1.get(1);
        String specType = "";
        try {
        specType = (String)args1.get(2);
        }catch(Exception e) {
        	
        }
        HashMap imageMap = new HashMap();
        imageMap = getImagesFromAltPath (context, parentId, null, imageData,false, new HashMap(),specType);
        return imageMap;
    }
    /**
     * This method is called from getImageURLS and getAltPathImages. All the CAD Images and 2D images connected to alt path are
     * obtained by this method and are returned through the Hash map, along with the URLs.
     * This is used two cases 1) To obtain the primary image from alt path 2) obtain all the alt path images and their modify access.
     *
     * @param context The ematrix context of the request.
     * @param parentId to hold Parent object Id
     * @param primaryImage to hold Primary Image Name
     * @param imageData to hold imageData object
     * @param forPrimaryImage to hold if for Primary Image
     * @return Map containing Thumbnailurls,imageurls,modifyaccess on the parent object.
     * @since AppsCommon 10.7.SP1
     */
    public HashMap getImagesFromAltPath(Context context,String parentId,String primaryImage,HashMap imageData,boolean forPrimaryImage, Map imageInfo, String specType)
    throws Exception
    {
        StringList CADImages;
        StringList CGRImages;
        String[] CADImageURLs           = {};
        String[] CADThumbnailURLs       = {};

        String strWorkspacePath         = context.createWorkspace();
        HashMap imageMap                = new HashMap();

        StringList CheckforDuplicate    = new StringList();

        StringList altPathList          = null;
        StringList altPathFormatList    = null;
        StringList Image2dFormatList    = null;
        String imagePath                = "";
        String tempString               = "";
        ArrayList proxylistForImageURLs = new ArrayList();

        String modifyAccess             = "";
        String altPathFormat            = "";
        String altPathObjectId          = "";
        String SELECT_FORMAT_FILES      = "";
        String CADImageID               = "";
        String fileName                 = "";
        DomainObject altPathObject;
        BusinessObject altPathObject_Latest_Rev;
        DomainObject cgrViewable        = null;
        DomainObject xmlViewable        = null;
        DomainObject thumbnailViewable  = null;
        StringList cgrFiles             = null;
        StringList xmlFiles             = null;
        StringList thumbnailFiles       = null;
        String fcsThumbnailUrl          = null;
        StringList images2DList         = null;
        StringList altCgrFormatList     = null;
        StringList altXmlFormatList     = null;
        StringList altThumbnailFormatList= null;

        int altCgrFormatListLength      = 0;
        int altXmlFormatListLength      = 0;
        int altThumbnailFormatListLength= 0;
        int altPathListLength           = 0;

        boolean returnToCaller          = false;
        BusinessObjectProxy bop;

        StringList fcsImageurls         = new StringList();
        StringList fcsThumbnails        = new StringList();
		String parentType = "";
		DomainObject parentObject = null;

        try{
			if(imageInfo.size() == 0){
        		parentObject   = new DomainObject(parentId);
            //Get Altpath settings for 2D images, context type if any
            	parentType = parentObject.getType(context);
			}else{
				parentType = (String)imageInfo.get(SELECT_TYPE);
			}
            parentType              = FrameworkUtil.getAliasForAdmin(context,"type",parentType,true);

            //Modified for Performance Improvement
            String XMLAltPath       = "emxComponents.3DXML.AltPath."+parentType;
            String ImageFormats     = "emxComponents.ImageManager.2DImageFormats"+parentType;
            if(!_properties.containsKey(XMLAltPath))
            {
                try{
                    altPathList      = FrameworkUtil.split(getResourceProperty(context, parentType,"", "emxComponents.3DXML.AltPath.", ""),",");
                    _properties.put(XMLAltPath,altPathList);
                    Image2dFormatList= FrameworkUtil.split(EnoviaResourceBundle.getProperty(context,"emxComponents.ImageManager.2DImageFormats"),",");
                    _properties.put(ImageFormats,Image2dFormatList);
                }
                catch(Exception e)
                {
                }
            }
            else
            {
                altPathList         = (StringList) _properties.get(XMLAltPath);
                Image2dFormatList   = (StringList) _properties.get(ImageFormats);
            }
            altPathListLength       = altPathList.size();
            String CGRFormat        = "emxComponents.3DXML.AltPathCGRFormat."+parentType;
            String XMLFormat        = "emxComponents.3DXML.AltPath3DXMLFormat."+parentType;
            String ImageFormat      = "emxComponents.3DXML.AltPathImageFormat."+parentType;
            if(!_properties.containsKey(CGRFormat))
            {
                try{
                    String strCGRFormats    = getResourceProperty(context, parentType,"", "emxComponents.3DXML.AltPathCGRFormat.", "");
                    String str3dxmlFormats  = getResourceProperty(context, parentType,"", "emxComponents.3DXML.AltPath3DXMLFormat.", "");
                    String strThumnailFormats = getResourceProperty(context, parentType,"", "emxComponents.3DXML.AltPathImageFormat.", "");
                    altCgrFormatList        = FrameworkUtil.split(strCGRFormats,",");
                    altXmlFormatList        = FrameworkUtil.split(str3dxmlFormats,",");
                    altThumbnailFormatList  = FrameworkUtil.split(strThumnailFormats,",");
                    if(strCGRFormats.startsWith(","))
                    {
                        if(altCgrFormatList == null)
                        {
                            altCgrFormatList = new StringList(1);
                        }
                        altCgrFormatList.add(0, "");
                    }

                    if(str3dxmlFormats.startsWith(","))
                    {
                        if(altXmlFormatList == null)
                        {
                            altXmlFormatList = new StringList(1);
                        }
                        altXmlFormatList.add(0, "");
                    }

                    if(strThumnailFormats.startsWith(","))
                    {
                        if(altThumbnailFormatList == null)
                        {
                            altThumbnailFormatList = new StringList(1);
                        }
                        altThumbnailFormatList.add(0, "");
                    }
                    _properties.put(CGRFormat,altCgrFormatList);
                    _properties.put(XMLFormat,altXmlFormatList);
                    _properties.put(ImageFormat,altThumbnailFormatList);
                }
                catch(Exception e)
                {
                }
            }
            else
            {
                altCgrFormatList        = (StringList) _properties.get(CGRFormat);
                altXmlFormatList        = (StringList) _properties.get(XMLFormat);
                altThumbnailFormatList  = (StringList) _properties.get(ImageFormat);
            }


            altCgrFormatListLength  = altCgrFormatList.size();
            altXmlFormatListLength  = altXmlFormatList.size();
            altThumbnailFormatListLength = altThumbnailFormatList.size();


            
            
			//add default thumbnail or primary-image in image column for all v6 data. - ID: IR-458819-3DEXPERIENCER2017x
           if(UIUtil.isNotNullAndNotEmpty(specType) && mxType.isOfParentType(context, specType, TYPE_VPM_REFERENCE)) {
         	 
        	   StringList prdImageUrls=new StringList();
        	   
        	   prdImageUrls =  getPrdImageUrls(context,parentId,imageData);
        	
        	   //image manager checks for primaryImage extension type CGR or 3dxml for displaying 3d. New image manager code doesn't use file type.
        	   fcsThumbnailUrl = (String)prdImageUrls.get(0);
        	   fcsImageurls.add(prdImageUrls.get(0));
        	   fcsThumbnails.add(prdImageUrls.get(0));
        	   primaryImage = "emxDefaultThumbnail.cgr";
        	   CheckforDuplicate.add(primaryImage);
      	 
        	   
           }else {
            

            // Check the formats and number of formats and exprs should be same
            if(altPathList != null && altPathList.size() > 0
               && altPathListLength == altCgrFormatListLength
               && altPathListLength == altXmlFormatListLength
               && altPathListLength == altThumbnailFormatListLength)
            {
                StringList strlPathList = new StringList();
                if(!_properties.containsKey(parentType))
                {
                    for(int i = 0; i < altPathList.size(); i++)
                    {
                      StringList objectsList= new StringList();
                      altPathObjectId       = (String)altPathList.get(i);
                      altPathObjectId       = com.matrixone.apps.domain.util.MessageUtil.substituteValues(context, altPathObjectId);
                      strlPathList.add(altPathObjectId);
                    }
                    _properties.put(parentType,strlPathList);
                }
                else
                {
                    strlPathList = (StringList) _properties.get(parentType);
                }
                if(imageInfo.size() == 0){
                	imageInfo = parentObject.getInfo(context,strlPathList);
                }
                StringList objectsList = new StringList();

                for(int i = 0; i < altPathList.size(); i++)
                {
                    if(objectsList != null && objectsList.size() > 0)
                        objectsList.removeAllElements();
                    String strKey   = (String) strlPathList.get(i);
                    try {
                        objectsList = (StringList) imageInfo.get(strKey);
                    } catch(ClassCastException e) {
                        strKey  = (String) imageInfo.get(strKey);
                        objectsList.add(strKey);
                    }

                    if(objectsList != null && objectsList.size() > 0 )
                    {
                        StringList processedObjects = new StringList();
                        for(int j = 0; j < objectsList.size(); j++)
                        {

                            CADImageID        = (String)objectsList.get(j);
                            altPathObject = new DomainObject(CADImageID);
                            StringList selList = new StringList();
                            selList.add("type.kindof["+ TYPE_3DXML +"]");
                            selList.add("type.kindof["+ TYPE_CGR +"]");
                            selList.add("type.kindof["+ TYPE_THUMBNAIL +"]");
                            Map altObjectMap = altPathObject.getInfo(context,selList);

                            
                            if(("true".equalsIgnoreCase((String)altObjectMap.get("type.kindof["+ TYPE_3DXML +"]"))
                                    || "true".equalsIgnoreCase((String)altObjectMap.get("type.kindof["+ TYPE_CGR +"]"))
                                    || "true".equalsIgnoreCase((String)altObjectMap.get("type.kindof["+ TYPE_THUMBNAIL +"]")))
                               && !processedObjects.contains(CADImageID))
                            {

                            	cgrFiles          = null;
                                xmlFiles          = null;
                                thumbnailFiles    = new StringList();
                                cgrViewable       = null;
                                xmlViewable       = null;
                                thumbnailViewable = null;
                                StringList busSelects   = new StringList();
                                busSelects.add(DomainConstants.SELECT_ID);
                                busSelects.add(DomainConstants.SELECT_TYPE);
                                altPathObject           = new DomainObject(CADImageID);
                                String viewParentId     = altPathObject.getInfo(context,"to["+REL_VIEWABLE+"].from.id");
                                DomainObject ViewParentObj  = null;
                                MapList viewbleList     = new MapList();
                                if(viewParentId != null && !"".equals(viewParentId.trim()))
                                {
                                    ViewParentObj = new DomainObject(viewParentId);
                                    viewbleList   = ViewParentObj.getRelatedObjects(context,REL_VIEWABLE,TYPE_VIEWABLE,busSelects,null,false,true,(short)1,null,null,null,null,null);
                                }
                                Iterator itr    = viewbleList.iterator();
                                Map viewablesMap;
                                while(itr.hasNext())
                                {
                                    viewablesMap    = (Map)itr.next();

                                    if(TYPE_3DXML.equals((String)viewablesMap.get("type")))
                                    {
                                        xmlViewable = new DomainObject((String)viewablesMap.get("id"));
                                        xmlFiles    = xmlViewable.getInfoList(context,SELECT_3DXML_FORMAT_FILES);
                                        processedObjects.add((String)viewablesMap.get("id"));
                                    }
                                    else if(TYPE_CGR.equals((String)viewablesMap.get("type")))
                                    {
                                        cgrViewable = new DomainObject((String)viewablesMap.get("id"));
                                        cgrFiles    = cgrViewable.getInfoList(context,"format["+FORMAT_CGR+"].file.name");
                                        processedObjects.add((String)viewablesMap.get("id"));
                                    }
                                    else if(TYPE_THUMBNAIL.equals((String)viewablesMap.get("type")))
                                    {
                                        thumbnailViewable   = new DomainObject((String)viewablesMap.get("id"));
                                        thumbnailFiles      = thumbnailViewable.getInfoList(context,"format["+FORMAT_THUMBNAIL+"].file.name");
                                        processedObjects.add((String)viewablesMap.get("id"));
                                    }
                                }

                                ArrayList templist;
                                String[] tempArray;
                                if(xmlViewable != null && xmlFiles != null && xmlFiles.size() >0)
                                {
                                    for(int k = 0; k < xmlFiles.size(); k++)
                                    {
                                        fileName    = (String)xmlFiles.get(k);
                                        if(fileName != null && !"".equals(fileName.trim())
                                           && !CheckforDuplicate.contains(fileName))
                                        {
                                            if (returnToCaller == false && forPrimaryImage)
                                            {
                                                fcsThumbnailUrl = getThumbnailFCSUrl(context, thumbnailViewable, thumbnailFiles, imageData, fileName, primaryImage);
                                                if(fcsThumbnailUrl!=null )
                                                {
                                                    returnToCaller  = true;
                                                    primaryImage    = fileName;
                                                    break;
                                                }
                                            }
                                            else
                                            {
                                                CheckforDuplicate.add(fileName);
                                                templist    = new ArrayList();
                                                bop         = new BusinessObjectProxy(xmlViewable.getId(),FORMAT_3DXML, fileName, false, false);
                                                templist.add(bop);
                                                tempArray   = ImageRequestData.getImageURLS(context, templist, imageData);
                                                fcsImageurls.add(tempArray[0]);
                                                fcsThumbnailUrl = getThumbnailFCSUrl(context, thumbnailViewable, thumbnailFiles, imageData, fileName, primaryImage);
                                                if(fcsThumbnailUrl!=null)
                                                {
                                                    fcsThumbnails.add(fcsThumbnailUrl);
                                                }
                                            }
                                        }
                                    }
                                    if(returnToCaller)
                                    {
                                        break;
                                    }
                                }

                                if(cgrViewable != null && cgrFiles != null && cgrFiles.size() > 0)
                                {
                                    for(int k = 0; k < cgrFiles.size(); k++)
                                    {
                                        fileName    = (String)cgrFiles.get(k);
                                        if(fileName != null && !"".equals(fileName.trim())
                                            && !CheckforDuplicate.contains(fileName))
                                        {
                                            if (returnToCaller == false && forPrimaryImage)
                                            {
                                                fcsThumbnailUrl = getThumbnailFCSUrl(context, thumbnailViewable, thumbnailFiles, imageData, fileName, primaryImage);
                                                if(fcsThumbnailUrl!=null )
                                                {
                                                    returnToCaller  = true;
                                                    primaryImage    = fileName;
                                                    break;
                                                }
                                            }
                                            else
                                            {
                                                CheckforDuplicate.add(fileName);
                                                templist    = new ArrayList();
                                                bop         = new BusinessObjectProxy(cgrViewable.getId(),FORMAT_CGR, fileName, false, false);
                                                templist.add(bop);
                                                tempArray   = ImageRequestData.getImageURLS(context, templist, imageData);
                                                fcsImageurls.add(tempArray[0]+"&name="+fileName);
                                                fcsThumbnailUrl = getThumbnailFCSUrl(context, thumbnailViewable, thumbnailFiles, imageData, fileName, primaryImage);
                                                if(fcsThumbnailUrl !=null)
                                                {
                                                    fcsThumbnails.add(fcsThumbnailUrl);
                                                }
                                            }
                                        }
                                    }
                                    if(returnToCaller)
                                    {
                                        break;
                                    }
                                }

                                if(thumbnailViewable != null && thumbnailFiles != null && thumbnailFiles.size() > 0)
                                {
                                    for(int k = 0; k < thumbnailFiles.size(); k++)
                                    {
                                        fileName    =(String)thumbnailFiles.get(k);
                                        if(fileName != null && !"".equals(fileName.trim())
                              
                                            && !CheckforDuplicate.contains(fileName))
                                        {
                                            CheckforDuplicate.add(fileName);
                                            templist    = new ArrayList();
                                            bop         = new BusinessObjectProxy(thumbnailViewable.getId(),FORMAT_THUMBNAIL, fileName, false, false);
                                            templist.add(bop);
                                            tempArray   = ImageRequestData.getImageURLS(context, templist, imageData);
                                            fcsImageurls.add(tempArray[0]);
                                            fcsThumbnails.add(tempArray[0]);
                                            if (returnToCaller == false && forPrimaryImage
                                                && (primaryImage != null && !"".equals(primaryImage.trim()) && fileName.equals(primaryImage))
                                                      || (primaryImage == null || "".equals(primaryImage.trim())) )
                                            {
                                                fcsThumbnailUrl = tempArray[0];
                                                if(primaryImage == null || "".equals(primaryImage.trim()))
                                                {
                                                    primaryImage = fileName;
                                                }
                                                returnToCaller=true;
                                                break;
                                            }
                                        }
                                    }
                                    if(returnToCaller)
                                    {
                                        break;
                                    }
                                }
                            }
                        }

                        objectsList.removeAll(processedObjects);

                        if(objectsList.size() > 0 && (!returnToCaller))
                        {
                            for(int j = 0; j< objectsList.size(); j++)
                            {
                                CADImageID      = (String)objectsList.get(j);
								altPathObject = new DomainObject(CADImageID);

                                // Following code is finding the latest version of the object.
                                // The code is modified to only find the latest version object if it is not the parent object
                                // Code modified for Bug: 351394

                                if (parentId != null && !parentId.equals(CADImageID)
                                    && !altPathObject.isLastRevision(context))
                                {
                                    altPathObject_Latest_Rev= altPathObject.getLastRevision(context);
                                    altPathObject           = new DomainObject(altPathObject_Latest_Rev);
                                    CADImageID              = altPathObject.getId();
                                }
                                StringList formatList       = altPathObject.getInfoList(context,"policy.format");
                                String altCgrFormat         = "";
                                String alt3dxmlFormat       = "";
                                String altThumbnailFormat   = "";
                                if(altCgrFormatList != null && altXmlFormatList != null && altThumbnailFormatList != null
                                   && altPathListLength == altCgrFormatListLength
                                   && altPathListLength == altXmlFormatListLength
                                   && altPathListLength == altThumbnailFormatListLength)
                                {
                                    altCgrFormat        = PropertyUtil.getSchemaProperty(context,(String)altCgrFormatList.get(i));
                                    alt3dxmlFormat      = PropertyUtil.getSchemaProperty(context,(String)altXmlFormatList.get(i));
                                    altThumbnailFormat  = PropertyUtil.getSchemaProperty(context,(String)altThumbnailFormatList.get(i));
                                }
                                formatList.add("");

                                if(formatList != null && formatList.size() > 0 && (formatList.contains(alt3dxmlFormat) || formatList.contains(altCgrFormat) || formatList.contains(altThumbnailFormat)))
                                {
                                    StringList argList  = new StringList();
                                    argList.add("3dxml");
                                    StringList xmlFileList = filterImagesStringList(altPathObject.getInfoList(context,"format["+alt3dxmlFormat+"].file.name"),argList);
                                    argList.removeAllElements();
                                    argList.add("cgr");
                                    StringList cgrFileList      = filterImagesStringList(altPathObject.getInfoList(context,"format["+altCgrFormat+"].file.name"),argList);
                                    StringList thumbnailFileList= filterImagesStringList(altPathObject.getInfoList(context,"format["+altThumbnailFormat+"].file.name"),Image2dFormatList);

                                    if ( (xmlFileList != null && xmlFileList.size() > 0)
                                          ||(cgrFileList != null && cgrFileList.size() > 0)
                                          ||(thumbnailFileList != null && thumbnailFileList.size() > 0))
                                    {
                                        if(forPrimaryImage)
                                        {
                                            ArrayList templist  = new ArrayList();
                                            String[] tempArray;
                                            String hasImageHolder = primaryImage;
                                            if(primaryImage == null || "".equals(primaryImage.trim()))
                                            {
                                                if(xmlFileList != null && xmlFileList.size() > 0)
                                                {
                                                    primaryImage    = (String)xmlFileList.get(0);
                                                }
                                                else if(cgrFileList != null && cgrFileList.size()>0)
                                                {
                                                    primaryImage    = (String)cgrFileList.get(0);
                                                }
                                                else if(thumbnailFileList != null && thumbnailFileList.size()>0)
                                                {
                                                    primaryImage    = (String)thumbnailFileList.get(0);
                                                }
                                            }
    
                                            String fileExtension    = getFileExtension(primaryImage);
                                            if( !UIUtil.isNullOrEmpty(hasImageHolder) &&( "3dxml".equalsIgnoreCase(fileExtension) || "cgr".equalsIgnoreCase(fileExtension))) {

                                                if(!UIUtil.isNullOrEmpty(primaryImage))
                                                {
                                                    tempString = searchFileBaseNameIgnoreCase(thumbnailFileList, primaryImage,"jpg");
    
                                                    if(tempString!=null)
                                                    {
                                                        bop     = new BusinessObjectProxy(CADImageID, altThumbnailFormat, tempString, false, false);
                                                        templist.add(bop);
                                                        tempArray   = ImageRequestData.getImageURLS(context, templist, imageData);
                                                        fcsThumbnailUrl = tempArray[0];
                                                    }
                                                    else
                                                    {
                                                        fcsThumbnailUrl = defaultThumbnailUrl;
                                                    }
                                                    returnToCaller  = true;
                                                    break;
                                                }
                                            	
                                            } else {
                                            if("3dxml".equalsIgnoreCase(fileExtension))
                                            {
                                                if(searchFileBaseNameIgnoreCase(xmlFileList, primaryImage,"3dxml")!= null)
                                                {
                                                    tempString = searchFileBaseNameIgnoreCase(thumbnailFileList, primaryImage,"jpg");

                                                    if(tempString!=null)
                                                    {
                                                        bop     = new BusinessObjectProxy(CADImageID, altThumbnailFormat, tempString, false, false);
                                                        templist.add(bop);
                                                        tempArray   = ImageRequestData.getImageURLS(context, templist, imageData);
                                                        fcsThumbnailUrl = tempArray[0];
                                                    }
                                                    else
                                                    {
                                                        fcsThumbnailUrl = defaultThumbnailUrl;
                                                    }
                                                    returnToCaller  = true;
                                                    break;
                                                }
                                            }
                                            else if("cgr".equalsIgnoreCase(fileExtension))
                                            {
                                                if(searchFileBaseNameIgnoreCase(cgrFileList, primaryImage,"cgr")!= null)
                                                {
                                                    tempString = searchFileBaseNameIgnoreCase(thumbnailFileList, primaryImage,"jpg");

                                                    if(tempString != null)
                                                    {
                                                        bop     = new BusinessObjectProxy(CADImageID, altThumbnailFormat, tempString, false, false);
                                                        templist.add(bop);
                                                        tempArray   = ImageRequestData.getImageURLS(context, templist, imageData);
                                                        fcsThumbnailUrl = tempArray[0];
                                                    }
                                                    else
                                                    {
                                                        fcsThumbnailUrl = defaultThumbnailUrl;
                                                    }
                                                    returnToCaller  = true;
                                                    break;
                                                }
                                            }
                                            else
                                            {
                                                if(thumbnailFileList.contains(primaryImage))
                                                {
                                                    bop         = new BusinessObjectProxy(CADImageID, altThumbnailFormat, primaryImage, false, false);
                                                    templist.add(bop);
                                                    tempArray   = ImageRequestData.getImageURLS(context, templist, imageData);
                                                    fcsThumbnailUrl = tempArray[0];
                                                    returnToCaller  = true;
                                                    break;
                                                }
                                            }
                                            }
                                       }
                                       else
                                       {
                                           Iterator xmlItr = xmlFileList.iterator();
                                           while(xmlItr.hasNext())
                                           {
                                               fileName    = (String)xmlItr.next();
                                               if(fileName != null && !"".equals(fileName.trim()))
                                               {
                                                   if(!CheckforDuplicate.contains(fileName))
                                                   {
                                                       CheckforDuplicate.add(fileName);
                                                       ArrayList templist = new ArrayList();
                                                       String[] tempArray;
                                                       bop         = new BusinessObjectProxy(CADImageID, alt3dxmlFormat, fileName, false, false);
                                                       templist.add(bop);
                                                       tempString  = searchFileBaseNameIgnoreCase(thumbnailFileList,fileName,"jpg");
                                                       if(tempString != null)
                                                       {
                                                           bop         = new BusinessObjectProxy(CADImageID, altThumbnailFormat, tempString, false, false);
                                                           templist.add(bop);
                                                           tempArray   = ImageRequestData.getImageURLS(context, templist, imageData);
                                                           fcsImageurls.add(tempArray[0]);
                                                           fcsThumbnails.add(tempArray[1]);
                                                           //thumbnailFileList.remove(tempString);
                                                       }
                                                       else
                                                       {
                                                           tempArray   = ImageRequestData.getImageURLS(context, templist, imageData);
                                                           fcsImageurls.add(tempArray[0]);
                                                           fcsThumbnails.add(defaultThumbnailUrl);
                                                       }
                                                   }
                                               }
                                            }

                                            Iterator cgrItr = cgrFileList.iterator();
                                            while(cgrItr.hasNext())
                                            {
                                                fileName    = (String)cgrItr.next();
                                                if(fileName != null && !"".equals(fileName.trim()))
                                                {
                                                    if(!CheckforDuplicate.contains(fileName))
                                                    {
                                                        CheckforDuplicate.add(fileName);
                                                        ArrayList templist = new ArrayList();
                                                        String[] tempArray;
                                                        bop         = new BusinessObjectProxy(CADImageID, altCgrFormat, fileName, false, false);
                                                        templist.add(bop);
                                                        tempString  = searchFileBaseNameIgnoreCase(thumbnailFileList,fileName,"jpg");
                                                        if(tempString != null)
                                                        {
                                                            bop         = new BusinessObjectProxy(CADImageID, altThumbnailFormat, tempString, false, false);
                                                            templist.add(bop);
                                                            tempArray   = ImageRequestData.getImageURLS(context, templist, imageData);
                                                            fcsImageurls.add(tempArray[0]+"&name="+fileName);
                                                            fcsThumbnails.add(tempArray[1]);
                                                            //thumbnailFileList.remove(tempString);
                                                        }
                                                        else
                                                        {
                                                            tempArray   = ImageRequestData.getImageURLS(context, templist, imageData);
                                                            fcsImageurls.add(tempArray[0]+"&name="+fileName);
                                                            fcsThumbnails.add(defaultThumbnailUrl);
                                                        }
                                                    }
                                                }
                                            }



                                            Iterator thumbnailItr  = thumbnailFileList.iterator();
                                            while(thumbnailItr.hasNext())
                                            {
                                                fileName    = (String)thumbnailItr.next();
                                                if(fileName != null && !"".equals(fileName.trim())
                                                    && !((xmlFileList != null && searchFileBaseNameIgnoreCase(xmlFileList,fileName,FORMAT_3DXML) != null)
                                                         ||(cgrFileList != null && searchFileBaseNameIgnoreCase(cgrFileList,fileName,FORMAT_CGR) != null)))
                                                {
                                                    String fileExt  = getFileExtension(fileName);
                                                    if(Image2dFormatList.contains(fileExt.toLowerCase())
                                                       && altPathListLength == altThumbnailFormatListLength)
                                                    {
                                                         CheckforDuplicate.add(fileName);
                                                         ArrayList templist = new ArrayList();
                                                         String[] tempArray;
                                                         bop        = new BusinessObjectProxy(CADImageID, altThumbnailFormat, fileName, false, false);
                                                         templist.add(bop);
                                                         tempArray  = ImageRequestData.getImageURLS(context, templist, imageData);
                                                         fcsImageurls.add(tempArray[0]);
                                                         fcsThumbnails.add(tempArray[0]);
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(returnToCaller)
                        {
                            break;
                        }
                    }
                    else
                    {
                      objectsList = new StringList();
                    }
                }
            }
            
           }
            if (forPrimaryImage)
            {
                imageMap.put("fcsurl",fcsThumbnailUrl);
                imageMap.put("fileName",primaryImage);
            }
            else
            {
                //process here , building image maps
                CADImageURLs        = new String[fcsImageurls.size()];
                CADThumbnailURLs    = new String[fcsThumbnails.size()];

                for(int i = 0; i < fcsImageurls.size(); i++)
                {
                    CADImageURLs[i]     = (String)fcsImageurls.get(i);
                    CADThumbnailURLs[i] = (String)fcsThumbnails.get(i);
                }

				if(imageInfo.size() == 0){
                modifyAccess    = parentObject.getInfo(context, com.matrixone.apps.domain.Image.SELECT_HAS_MODIFY_ACCESS);
				}else{
				 	modifyAccess =(String) imageInfo.get(com.matrixone.apps.domain.Image.SELECT_HAS_MODIFY_ACCESS);
				}

                imageMap.put("CADImages", CheckforDuplicate);
                imageMap.put("CADImageURLs", CADImageURLs);
                imageMap.put("CADThumbnailURLs", CADThumbnailURLs);
                imageMap.put("modifyAccess",modifyAccess);
            }
        }
        catch(Exception e)
        {
            System.out.println("Exception in method getImagesFromAltPath() of JPO emxImageManagerBase: "+e.toString());
        }
        return imageMap;

}
    private StringList getPrdImageUrls(Context context, String parentId, HashMap imageData) throws Exception {
    	StringList prdImageUrls = new StringList();
		StringList selects = new StringList(7);
        selects.add(SELECT_IMAGE_HOLDER_ID);
        selects.add(SELECT_IMAGE_HOLDER_PRIMARY_IMAGE_NAME);
        selects.add(SELECT_PRIMARY_IMAGE_ID);
        selects.add(SELECT_IMAGE_PRIMARY_IMAGE_NAME);
        selects.add(SELECT_ID);
        selects.add(SELECT_TYPE); 
        
        String formatImg = "format_mxSmallImage";              
        String format = PropertyUtil.getSchemaProperty(context,formatImg);

        
        
        StringList imageSelects = new StringList(6);
        imageSelects.add(SELECT_ATTRIBUTE_PRIMARY_IMAGE_FROM_ALTPATH);
        imageSelects.add(SELECT_ATTRIBUTE_TRAVERSE_ALTPATH);
        imageSelects.add(SELECT_MX_SMALL_IMAGE_FILE_NAMES);
        imageSelects.add(SELECT_3DXML_FORMAT_FILES);
        imageSelects.add(SELECT_GENERIC_FORMAT_FILES);
        imageSelects.add("format["+format+"].file.name");
        
        ArrayList bopArrayList=new ArrayList();
        ArrayList indexList=new ArrayList();
        
       
        String[] objectIds= {parentId};
		
      
	     MapList objListMaplist = DomainObject.getInfo(context, objectIds, selects);
  	  
	    Map objMap = (Map) objListMaplist.get(0);
	    
	    

      String imageHolderNullIndexArray[] = new String[objListMaplist.size()];
      StringList slImageHolderObjId = new StringList();
      for (int j = 0; j < objListMaplist.size(); j++) {
          Map objMap1 = (Map) objListMaplist.get(j);
          String imageHolderId = (String)objMap1.get(SELECT_IMAGE_HOLDER_ID);
          if(imageHolderId != null && !"#DENIED!".equals(imageHolderId)) {
              slImageHolderObjId.add(imageHolderId);
              imageHolderNullIndexArray[j]="";
          } else{
              imageHolderNullIndexArray[j]=null;
          }
      }
      String imgHolderObjArray[] = new String[slImageHolderObjId.size()];
      slImageHolderObjId.toArray(imgHolderObjArray);
      MapList imgHolderObjMaplist = DomainObject.getInfo(context, imgHolderObjArray, imageSelects);

      // To maintain the same sequence of the elements in imgHolderObjMaplist with respect to objListMaplist
      // fill the remaining place with null fo which there is no imageholder object connected.
      for (int k = 0; k < imageHolderNullIndexArray.length; k++) {
          if(imageHolderNullIndexArray[k] == null) {
              imgHolderObjMaplist.add(k, imageHolderNullIndexArray[k]);
          }
      }
	    
	    
	    
	    
	   String imageHolderId = (String)objMap.get(SELECT_IMAGE_HOLDER_ID);
     String primaryImageId = (String)objMap.get(DomainObject.SELECT_PRIMARY_IMAGE_ID);
     String holderFileName = (String) objMap.get(SELECT_IMAGE_HOLDER_PRIMARY_IMAGE_NAME);
		
     if (imageHolderId !=  null)
		{
			Map imgData =(Map)imgHolderObjMaplist.get(0);
			String primImgFromAltPath =(String)imgData.get(SELECT_ATTRIBUTE_PRIMARY_IMAGE_FROM_ALTPATH);
			String traverseAltPath = (String)imgData.get(SELECT_ATTRIBUTE_TRAVERSE_ALTPATH);
			
			
			
			StringList holderTempStringList;
			String holderTempString;
                
              
                Object obj = imgData.get("format["+format+"].file.name");
                if (obj instanceof String) {
                    holderTempStringList = new StringList((String)imgData.get("format["+format+"].file.name"));
                } else if(obj instanceof StringList) {
                    holderTempStringList = new StringList((StringList)imgData.get("format["+format+"].file.name"));
                } else {
                    holderTempStringList = new StringList();
                }
                holderTempString = searchFileBaseNameIgnoreCase(holderTempStringList,holderFileName,"jpg");
                if(holderTempString!=null) {
                    BusinessObjectProxy bop1 = new BusinessObjectProxy(imageHolderId, format, holderTempString, false, false);
                    bopArrayList.add(bop1);
                    indexList.add(0);
                } else  {
                    prdImageUrls.add(defaultThumbnailUrl);
                }
              

                
                if(! bopArrayList.isEmpty()){
                    String[] tmpImageUrls = ImageRequestData.getImageURLS(context, bopArrayList, imageData);
                    for (int i = 0; i < tmpImageUrls.length; i++) {
                        prdImageUrls.insertElementAt(tmpImageUrls[i], (Integer)indexList.get(i));
                    }
                }
                
			
			} else if(imageHolderId  == null) {
				prdImageUrls.add(defaultThumbnailUrl);
			}  
     
     return prdImageUrls;
	}

    private String getThumbnailFCSUrl(Context context, DomainObject thumbnailViewable, StringList thumbnailFiles, HashMap imageData, String fileName, String primaryImage)
    {
        String fcsThumbnailUrl = null;
        //String defaultThumbnailUrl = "";
        String imagePath = "";

        try
        {
            if( (primaryImage != null && !"".equals(primaryImage.trim()) && fileName.equals(primaryImage)) || (primaryImage==null || "".equals(primaryImage.trim())) )
            {
                if( thumbnailFiles!=null && thumbnailFiles.size() > 0)
                {
                    String tempString = searchFileBaseNameIgnoreCase(thumbnailFiles,fileName,"jpg");
                     if(tempString!=null)
                     {
                        BusinessObjectProxy bop = new BusinessObjectProxy(thumbnailViewable.getId(),FORMAT_THUMBNAIL, tempString, false, false);
                        ArrayList templist=new ArrayList();
                        templist.add(bop);
                        String[] tempArray=ImageRequestData.getImageURLS(context, templist, imageData);
                        fcsThumbnailUrl = tempArray[0];
                     }
                     else
                     {
                        fcsThumbnailUrl=defaultThumbnailUrl;
                     }
                }
                else
                {
                    fcsThumbnailUrl=defaultThumbnailUrl;
                }
            }
        }
        catch(Exception e)
        {
             System.out.println("Exception in getThumbnailFCSUrl method of emxImageManagerBase"+e.toString());
        }
        return fcsThumbnailUrl;
    }


   /**
     * This method is used to search and get the altpath expression for
     * context type
     *
     * @param context The ematrix context of the request.
     * @param args holds type,vault,altpath setting string,optional suffix
     * @return String alt path expression
     * @since AppsCommon 10.7.SP1
     */

    public String getAltPathFromParent(Context context, String[] args)throws Exception
    {
        String symbolicType="",vault="",prefix="",suffix="";
        if(args.length > 0)
        {
          symbolicType=args[0];
          vault=args[1];
          prefix=args[2];
          suffix=args[3];
        }
     return(getResourceProperty(context,symbolicType,vault,prefix,suffix));
    }

 /**
     * This method is called by getAltPathFromParent() method to
     * get the altpath expression for specified type
     *
     * @param context The ematrix context of the request.
     * @param symbolicType String holds Symbolic name of the type
     * @param vault String holds vault name
     * @param prefix String holds prefix to be added to form the key
     * @param suffix String holds suffix to be added to form the key
     * @return String alt path expression
     * @since AppsCommon 10.7.SP1
     */
    String getResourceProperty(Context context, String symbolicType, String vault, String prefix, String suffix) throws Exception
    {
        String strAltPathExpression = null;

        try
        {
            strAltPathExpression = EnoviaResourceBundle.getProperty(context,prefix+symbolicType+suffix);
        }
        catch(FrameworkException fe)
        {
            if(symbolicType != null && !"".equals(symbolicType))
            {
                String type = PropertyUtil.getSchemaProperty(context,symbolicType);
                BusinessType busType = new BusinessType(type, new Vault(vault));
                if(context != null)
                {
                    String parentBusType = busType.getParent(context);
                    if(!"".equals(parentBusType))
                    {
                        String parentTypeSymbolicName = FrameworkUtil.getAliasForAdmin(context,"type",parentBusType,true);
                        strAltPathExpression = getResourceProperty(context, parentTypeSymbolicName, vault, prefix, suffix);
                    }
                }
                else
                {
                    throw new FrameworkException(getClass().getName()+".getImgMgrProperty(context, symbolicType, vault, prefix, suffix): context is null");
                }
            }
            else
            {
                throw new FrameworkException(getClass().getName()+".getImgMgrProperty(context, symbolicType, vault, prefix, suffix): symbolicType is null or ''");
            }
        }
        return strAltPathExpression;
    }



/**
     * This method is used to prepare the manifest.xml contents
     * which is used in 3dxml file creation
     *
     * @param String fileName of the 3dxml
     * @return Document which contains manitest file contenst in xml
     * @since AppsCommon 10.7.SP1
     */

    public  Document prepareManifest(String fileName)
    {
        Vector contentVector = new Vector();
        Element root = new Element("Manifest", "", "http://www.3ds.com/xsd/3DXML");
        contentVector.add(root);

        Element elmRoot = new Element("Root");
        elmRoot.addContent(fileName);
        root.addContent(elmRoot);
        return new Document(contentVector);
    }

/**
     * This method is used to prepare the 3dxml file contents
     *for the creation of 3dxml file.
     *
     * @param String encoded cgr contents and String filename of the 3dxml
     * @return Document which contains 3dxml file contents
     * @since AppsCommon 10.7.SP1
     */

    public static Document prepare3DXMLDocument(String fileName,String user)throws Exception
    {
       Vector contentVector = new Vector();
        Element root = new Element("Model_3dxml", "", "http://www.3ds.com/xsd/3DXML");
        contentVector.add(root);

        //add header
        Element elmHdr = new Element("Header");

        Element elmSV = new Element("SchemaVersion");
        elmSV.addContent("4.0");

        Element elmTitle = new Element("Title");
        elmTitle.addContent(fileName);

        Element elmAuthor = new Element("Author");
        elmAuthor.addContent(user);

        Element elmGenerator = new Element("Generator");
        elmGenerator.addContent("CATIA V5");

        Element elmCreated = new Element("Created");
        elmCreated.addContent((new java.util.Date()).toString());

        elmHdr.addContent(elmSV);
        elmHdr.addContent(elmTitle);
        elmHdr.addContent(elmAuthor);
        elmHdr.addContent(elmGenerator);
        elmHdr.addContent(elmCreated);
         // add DefaultSessionProperties
        Element elmDSP = new Element("DefaultSessionProperties");

        Element elmBGC = new Element("BackgroundColor");
        elmBGC.setAttribute("alpha","0.");
        elmBGC.setAttribute("red", "0.2");
        elmBGC.setAttribute("green", "0.2");
        elmBGC.setAttribute("blue", "0.4");

        Element elmRS = new Element("RenderingStyle");
        elmRS.addContent("SHADING");

        elmDSP.addContent(elmBGC);
        elmDSP.addContent(elmRS);

        // add Product Structure
        Element elmPS = new Element("ProductStructure");
        elmPS.setAttribute("root", "1");


        Element elmR3D = new Element("Reference3D");
        elmR3D.setAttribute("type", "Reference3DType", Namespace.getNamespace("xsi", "http://www.matrixone.com/xsi"));
        elmR3D.setAttribute("id", "10");
        elmR3D.setAttribute("name", fileName);

        Element elmRR = new Element("ReferenceRep");
        elmRR.setAttribute("type", "ReferenceRepType", Namespace.getNamespace("xsi", "http://www.matrixone.com/xsi"));
        elmRR.setAttribute("id", "12");
        elmRR.setAttribute("name", fileName+"_ReferenceRep");
        elmRR.setAttribute("format", "TESSELLATED");
        elmRR.setAttribute("associatedFile", "urn:3DXML:" + fileName +".3DRep");
        elmRR.setAttribute("version", "2.2");

        Element elmIR = new Element("InstanceRep");
        elmIR.setAttribute("type", "InstanceRepType", Namespace.getNamespace("xsi", "http://www.matrixone.com/xsi"));
        elmIR.setAttribute("id", "11");
        elmIR.setAttribute("name", fileName+"_InstanceRep");

        Element elmIAB = new Element("IsAggregatedBy");
        elmIAB.addContent("10");
        Element elmIIO = new Element("IsInstanceOf");
        elmIIO.addContent("12");
        elmIR.addContent(elmIAB);
        elmIR.addContent(elmIIO);

        elmPS.addContent(elmR3D);
        elmPS.addContent(elmRR);
        elmPS.addContent(elmIR);


        root.addContent(elmHdr);
        root.addContent(elmDSP);
        root.addContent(elmPS);
        return new Document(contentVector);
    }



    /**
     * This method does used to check whether fileNameCollection contains searchString. It does case insensitive search
     *
     * @param fileNameCollection collection of String objects
     * @param searchBaseFileName string to be searched in sourceColletion
     * @return String the Filename whose base name mathhes searchString in this list of the first occurrence of the specified element, or -1 if this list does
     * not contain this element
     * @since AppsCommon 10.7.SP1
     */

    public static String searchFileBaseNameIgnoreCase(Collection fileNameCollection, String FileName, String expectedFormatExt)
    {
        try
        {
            if(fileNameCollection!=null && FileName!=null && expectedFormatExt != null && !"".equals(FileName.trim()) && !"".equals(expectedFormatExt.trim()))
            {
                Iterator itr = fileNameCollection.iterator();
                for(int i=0;itr.hasNext();i++)
                {
                    String collElement = (String)itr.next();
                    if((getFileBaseName(FileName)+"."+expectedFormatExt).equalsIgnoreCase(collElement))
                    {
                        return collElement;
                    }
                }
            }
        }catch(ClassCastException e)
        {
            System.out.println("Exception in searchFileBaseNameIgnoreCase() of emxImageManagerBase JPO "+e.toString());
        }
        return null;
    }

     /**
     * This method is used to filter the StringList based on specific format list(file extension list)
     *
     * @param imageList StringList of image names
     * @param forFormatList StringList of file extensions list
     * @return StringList , which is filtered based on extension list , returns empty stringList if any arguments or null or empty
     * @since AppsCommon 10.7.SP1
     */

    public static StringList filterImagesStringList(StringList imageList,StringList forFormatList)
    {
        try
        {
            if(imageList != null && forFormatList != null && imageList.size()>0 &&  forFormatList.size()>0)
            {
                StringList returnList=new StringList();
                Iterator itr=imageList.iterator();
                String fileExt;
                String fileName;
                String format;
                while(itr.hasNext())
               {
                    fileName=(String)itr.next();
                    fileExt=getFileExtension(fileName);
                    for(int i=0;i<forFormatList.size();i++)
                   {
                        format=(String)forFormatList.get(i);
                        if(format.equalsIgnoreCase(fileExt))
                       {
                         returnList.add(fileName);
                         break;
                       }
                   }
               }
               return returnList;
            }
        }catch(Exception e)
        {
            System.out.println("Exception in filterImagesStringList() of emxImageManagerBase JPO "+e.toString());
        }
        return (new StringList());
    }

      /**
    *  Take a url and return it encoded.
    *
    * @param url  the given URL
    * @return     the encoded URL
    * @since      AEF 9.5.0.0
    */

    public String encodeURL(String url)
    {
        int pos = url.indexOf('?');
        if (pos == -1)
        {
            return url;
        }

        String uri = url.substring(0, pos);
        String queryString = url.substring(pos + 1);

        StringBuffer encodedURL = new StringBuffer(url.length());
        encodedURL.append(uri);
        encodedURL.append('?');

        if (queryString.indexOf('&') > 0)
        {
            StringTokenizer st = new StringTokenizer(queryString, "&");

            for (int i = 0; st.hasMoreTokens(); i++)
            {
                String token = st.nextToken();

                if (i > 0)
                {
                    encodedURL.append("&amp;");
                }

                int index = token.indexOf('=');
                if (index > 0)
                {
                    encodedURL.append(token.substring(0, index + 1));
                    encodedURL.append(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(token.substring(index + 1)));
                }
                else
                {
                    encodedURL.append(token);
                }
            }
        }
        else
        {
            int index = queryString.indexOf("=");
            if (index > 0)
            {
                encodedURL.append(queryString.substring(0, index + 1));
                encodedURL.append(com.matrixone.apps.domain.util.XSSUtil.encodeForURL(queryString.substring(index + 1)));
            }
            else
            {
                encodedURL.append(queryString);
            }
        }

        return (encodedURL.toString());
    }

    /**
    * Deletes Image Holder object once the object that is associated with Image Holder is deleted
    * @param context the eMatrix <code>Context</code> object
    * @param args holds one argument
    *   args[0] - imageHolderID - id of the from Object
    * @return  0 if operation is Success.
    * @throws Exception if the operation fails
    * @since Common V6R2008-1
    */
    public int deleteImageHolder(Context context, String[] args) throws Exception
    {
        String imageHolderID =args[0];
        boolean contextPushed = false;
        if(imageHolderID != null && !"".equals(imageHolderID) && !"null".equalsIgnoreCase(imageHolderID))
        {

            DomainObject objImageHolder = new DomainObject(imageHolderID);

            if(objImageHolder.exists(context))
            {
                try
                {
                    ContextUtil.pushContext(context);
                    contextPushed = true;
                    String IsHolderconnected = (String)objImageHolder.getInfo(context, "from["+DomainObject.RELATIONSHIP_IMAGE_HOLDER+"]");
                    if("True".equalsIgnoreCase(IsHolderconnected)){
                    	return 0;
                    }
                    objImageHolder.deleteObject(context, true);
                }
                catch(Exception e)
                {
                    String[] formatArgs = {imageHolderID};
                    String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.ImageManagerBase.UnableToDeleteObject",formatArgs);
                    throw new FrameworkException(message);
                }
                finally
                {
                    if(contextPushed)
                    {
                        ContextUtil.popContext(context);
                    }
                }
            }
        }
        return 0;
    }


    /**
    * Method verifies if the format of the file
    * to be checked-in is supported.
    * If the format is supported, it returns true.
    *
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args contains the filename.
    * @returns Boolean
    * @throws Exception if the operation fails
    * @since Common V6R2011
    */
    public Boolean checkImageFormat(Context context, String[] args) throws Exception {

        String sImageUtilityFormats = (String) EnoviaResourceBundle.getProperty( context, "emxComponents.ImageManager.AllowedFormats" );
        String sNativeFormats       = (String) EnoviaResourceBundle.getProperty( context, "emxComponents.ImageManager.2DImageFormats" );
        String sImageUtilityPath    = (String) EnoviaResourceBundle.getProperty( context, "emxComponents.ImageManager.ImageUtility.Directory" );
        if( sImageUtilityPath == null || "null".equals( sImageUtilityPath ) ) sImageUtilityPath = EMPTY_STRING;

        StringItr formatsItr;
        if( EMPTY_STRING.equals( sImageUtilityPath ) ) {
            formatsItr = new StringItr( FrameworkUtil.split( sNativeFormats, "," ) );
        } else {
            formatsItr = new StringItr( FrameworkUtil.split( sImageUtilityFormats, "," ) );
        }

        Boolean bReturn = Boolean.valueOf( false );
        String sFileName = args[0];
        String sFileExtn = getFileExtension( sFileName );
        while( formatsItr.next() ) {
            if( sFileExtn.equalsIgnoreCase( formatsItr.obj() ) ) {
                bReturn = Boolean.valueOf( true );
                break;
            }
        }

        return bReturn;
    }

    public String getDefaultThumbnailUrl(Context context, String[] args) {
        return defaultThumbnailUrl;
    }
    
    public static String extractImageFrom3DXMLFile(Context context, String workspacePath, String fileName) throws Exception{
        String childFileExtn = "";
        ZipFile zipFile = new ZipFile(new java.io.File(workspacePath, fileName));
        Enumeration entries = zipFile.entries();
        StringBuffer sbFileName = new StringBuffer();
        while(entries.hasMoreElements())
        {
            ZipEntry entry = (ZipEntry)entries.nextElement();
            // Assuming the Image files not under any directory but directly in the Zipped parent directory
            if(!entry.isDirectory())
            {
                childFileExtn = getFileExtension(entry.getName());
                StringList extList = FrameworkUtil.split(EnoviaResourceBundle.getProperty(context, "emxComponents.ImageManager.2DImageFormats"), ",");
                if (extList.contains(childFileExtn))
                {
                    StringBuffer sourceFileName = new StringBuffer(150);
                    sbFileName.append(getFileBaseName(fileName));
                    sbFileName.append(".");
                    sbFileName.append(childFileExtn);
                    sourceFileName.append(workspacePath);
                    sourceFileName.append(java.io.File.separatorChar);
                    sourceFileName.append(sbFileName);
                    copyFiles(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(sourceFileName.toString())));
                    break;
                }
            }
        }
        zipFile.close();
        return sbFileName.toString();
    }
    
    //This method checks Document Object for Thumbnails format images and will delete if any Images were checked in. 
    
    public void deletePreviousVersionThumbnailImages(Context context, String objectid,String newfile ) throws Exception
    {
    	String filename = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump $3",
    	        		objectid, "format[mxMedium Image].file.name",  "|");

    	if(!UIUtil.isNullOrEmpty(filename)){
    		String command = "delete bus $1 format $2 file $3";
    		MqlUtil.mqlCommand(context,command,objectid,"mxMedium Image",filename);
    	}


    }
    
    public StringList getAltPathDefinitions(Context context, String parentType) throws Exception{
    	try{
        StringList Image2dFormatList    = null;
        StringList altPathList          = null;
        String altPathObjectId          = "";	
        parentType              = FrameworkUtil.getAliasForAdmin(context,"type",parentType,true);
        
        //Modified for Performance Improvement
        String XMLAltPath       = "emxComponents.3DXML.AltPath."+parentType;
        String ImageFormats     = "emxComponents.ImageManager.2DImageFormats"+parentType;
        if(!_properties.containsKey(XMLAltPath))
        {
            try{
                altPathList      = FrameworkUtil.split(getResourceProperty(context, parentType,"", "emxComponents.3DXML.AltPath.", ""),",");
                _properties.put(XMLAltPath,altPathList);
                Image2dFormatList= FrameworkUtil.split(EnoviaResourceBundle.getProperty(context,"emxComponents.ImageManager.2DImageFormats"),",");
                _properties.put(ImageFormats,Image2dFormatList);
            }
            catch(Exception e)
            {
            }
        }
        else
        {
            altPathList         = (StringList) _properties.get(XMLAltPath);
            Image2dFormatList   = (StringList) _properties.get(ImageFormats);
        }
        
        StringList strlPathList = new StringList();
        if(!_properties.containsKey(parentType))
        {
            if(altPathList != null){
				for(int j = 0; j < altPathList.size(); j++)
				{
					StringList objectsList= new StringList();
					altPathObjectId       = (String)altPathList.get(j);
					altPathObjectId       = com.matrixone.apps.domain.util.MessageUtil.substituteValues(context, altPathObjectId);
					strlPathList.add(altPathObjectId);
				}
				_properties.put(parentType,strlPathList);
			}
        }
        else
        {
            strlPathList = (StringList) _properties.get(parentType);
        }
        return strlPathList;
    } catch(Exception ex ) {
             ex.printStackTrace();
             throw ex;
    }
		
}



}//end of class
