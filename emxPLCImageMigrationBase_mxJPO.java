/*
** emxPLCImageMigrationBase
**
** Copyright (c) 1992-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
*
*/

import  java.io.FileOutputStream;
import  java.io.PrintStream;
import  java.util.Map;
import  java.util.HashMap;
import  com.matrixone.apps.domain.*;
import  com.matrixone.apps.domain.util.*;
import com.matrixone.apps.productline.Image;
import com.matrixone.apps.productline.ProductLineConstants;

import  matrix.db.*;
import  matrix.util.*;
import  java.util.*;


/**
 * The <code>emxPLCImageMigrationBase</code> class contains migration script
 * @author Mayukh,Enovia MatrixOne
 * @version ProductCentral 10.6SP2 - Copyright (c) 2005, MatrixOne, Inc.
 *
 */
public class emxPLCImageMigrationBase_mxJPO extends emxImageManager_mxJPO
{
    // The operator symbols
    /** A string constant with the value &&. */
    protected static final String SYMB_AND = " && ";
    /** A string constant with the value ||. */
    protected static final String SYMB_OR = " || ";
    /** A string constant with the value ==. */
    protected static final String SYMB_EQUAL = " == ";
    /** A string constant with the value !=. */
    protected static final String SYMB_NOT_EQUAL = " != ";
    /** A string constant with the value >. */
    protected static final String SYMB_GREATER_THAN = " > ";
    /** A string constant with the value <. */
    protected static final String SYMB_LESS_THAN = " < ";
    /** A string constant with the value >=. */
    protected static final String SYMB_GREATER_THAN_EQUAL = " >= ";
    /** A string constant with the value <=. */
    protected static final String SYMB_LESS_THAN_EQUAL = " <= ";
    /** A string constant with the value ~~. */
    protected static final String SYMB_MATCH = " ~~ ";
    /** A string constant with the value '. */
    protected static final String SYMB_QUOTE = "'";
    /** A string constant with the value (. */
    protected static final String SYMB_OPEN_PARAN = "(";
    /** A string constant with the value ). */
    protected static final String SYMB_CLOSE_PARAN = ")";
    /** A string constant with the value attribute. */
    protected static final String SYMB_ATTRIBUTE = "attribute";
    /** A string constant with the value [. */
    protected static final String SYMB_OPEN_BRACKET = "[";
    /** A string constant with the value ]. */
    protected static final String SYMB_CLOSE_BRACKET = "]";
    /** A string constant with the value to. */
    protected static final String SYMB_TO = "to";
    /** A string constant with the value from. */
    protected static final String SYMB_FROM = "from";
    /** A string constant with the value ".". */
    protected static final String SYMB_DOT = ".";
    /** A string constant with the value "null". */
    protected static final String SYMB_NULL = "null";
    /** A string constant with the value "!". */
    protected static final String SYMB_NOT= "!";
    /** A string constant with the value ",". */
    protected static final String SYMB_COMMA= ",";

    protected static final String CONTEXT_OBJECT_DETAILS = "Context Object(s) Details:";
    protected static final String CONTEXT_OBJECT = "Context Object: ";
    protected static final String IMAGE_OBJECT = "Image Object: ";
    protected static final String REL_NAME = "rel name";

    private static final String FILLLINE = "emxProduct.Migration.Log.FillLine";
    private static final String MIGRATION_LOG_FILE = "emxProduct.Migration.Log.ImageMigrationInformation";
    private static final String MIGRATION_ERR_FILE = "emxProduct.Migration.Log.ImageMigrationError";
    private static final String END_LINE = "\n";
    private static final String MIGRATING_IMAGE = "emxProduct.Migration.Log.MigratingImageString";
    private static final String MIGRATING_ERROR = "emxProduct.Migration.Err.MigratingErrorString";
    private static final String DISCONNECTING_REL = "emxProduct.Migration.Log.DisconnectingRelString";
    private static final String ORPHAN_IMAGE = "emxProduct.Migration.Log.OrphanImage";
    private static final String IMAGE_WITH_REL = "emxProduct.Migration.Log.ImageWithRel";
    private static final String MIGRATE_SUCCESS = "emxProduct.Migration.Log.MigrateSuccess";
    private static final String MAKE_PRIMARY = "emxProduct.Migration.Log.MakePrimary";

    private static FileOutputStream foLogFileInfo;
    private static FileOutputStream foErrFile;
    private static PrintStream psLogWriter;
    private static PrintStream psErrWriter;
    public static  MQLCommand mqlCommand = null;
    private static String strFileURI= DomainConstants.EMPTY_STRING;

    private HashMap uploadParamsMap;
    private Map objectMap;

    /**
    * Create a new emxPLCImageMigrationBase object from a given id.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments.
    * @return a emxPLCImageMigrationBase Object
    * @throws Exception if the operation fails
    * @since ProductCentral 10.6.SP2
    */
    public emxPLCImageMigrationBase_mxJPO (Context context, String[] args) throws Exception
    {
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6.SP2
     */
    public int mxMain (Context context, String[] args)
    throws Exception
    {
        if (!context.isConnected())
        {
            String strContentLabel = EnoviaResourceBundle.getProperty(context, "ProductLine",
                    "emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
            throw  new Exception(strContentLabel);
        }
        migrate(context,args);
        return  0;
     }

    /**
     * this method migartes the data.
     * @param context the eMatrix <code>Context</code> object
     * @param args  - is a string array
     * @return void
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6SP2
     */

    public void migrate(Context context,String[] args)
    throws Exception
    {
        MQLCommand mqlCommand = new MQLCommand();
        //set the trigger off
        mqlCommand.executeCommand(context,"trigger off");
	try {

        //open file for logging
        try
        {
            strFileURI = EnoviaResourceBundle.getProperty(context,MIGRATION_LOG_FILE);
            foLogFileInfo = new FileOutputStream(strFileURI,false);
            psLogWriter = new PrintStream(foLogFileInfo);
        }
        catch(Exception e)
        {

            Exception ex=new Exception("Migration Log File Entry Not Present in the Properties File");
            throw ex;
        }

        //open file for error logging
        try
        {
            strFileURI = EnoviaResourceBundle.getProperty(context,MIGRATION_ERR_FILE);
            foErrFile = new FileOutputStream(strFileURI,false);
            psErrWriter = new PrintStream(foErrFile);
        }
        catch(Exception e)
        {
            Exception ex=new Exception("Migration Error File Entry Not Present in the Properties File");
            throw ex;
        }

        // Migrate all the Image objects to checkin them to appropriate formats
        migrateImageObjects(context, args);

        // Connect an proper image connected to an object as primary image
        connectPrimaryImageToObjects(context, args);

        //close the log file
        foLogFileInfo.close();
        foLogFileInfo = null;

        foErrFile.close();
        foErrFile = null;

	} finally {
	    //set the trigger on
	    mqlCommand.executeCommand(context,"trigger on");
	}
    }

    /**
     * this method migrates all the Image objects to the present data definition
     * @param context the eMatrix <code>Context</code> object
     * @param args  - is a string array
     * @return void
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6SP2
     */

    protected void migrateImageObjects(Context context,String[] args) throws Exception
    {
        try
        {
            StringList objSelect = new StringList(4);
            StringList relSelect = new StringList(2);
            MapList mapList = null;
            MapList mapObjectList = null;
            uploadParamsMap =  new HashMap();

            String strType=ProductLineConstants.TYPE_IMAGE;

            StringBuffer sbWhereExp = new StringBuffer(150);
            String strWhereExp="";

            String strImageId = DomainConstants.EMPTY_STRING;
            String strImageName = DomainConstants.EMPTY_STRING;
            String strFileName = DomainConstants.EMPTY_STRING;
            String strImageFormat = PropertyUtil.getSchemaProperty(context,
                                                                 DomainSymbolicConstants.SYMBOLIC_format_generic);

            String[] strRelIds;
            String[] strRelNames;

            String[] strContextObjIds;
            String[] strContextObjTypes;
            String[] strContextObjNames;
            String[] strContextObjRevisions;
            //StringList fileName = new StringList(1);

            StringBuffer stbRelSelect = new StringBuffer(50);
            stbRelSelect = stbRelSelect.append(ProductLineConstants.RELATIONSHIP_IMAGES)
                                       .append(SYMB_COMMA)
                                       .append(ProductLineConstants.RELATIONSHIP_PRIMARY_IMAGE);

            //object selects
            objSelect.addElement(DomainConstants.SELECT_ID);
            objSelect.addElement(DomainConstants.SELECT_TYPE);
            objSelect.addElement(DomainConstants.SELECT_NAME);
            objSelect.addElement(DomainConstants.SELECT_REVISION);
            //rel selects
            relSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
            relSelect.addElement(DomainConstants.SELECT_RELATIONSHIP_NAME);

            // fetching all the image objects in the database
            mapList = DomainObject.findObjects(context,strType,DomainConstants.QUERY_WILDCARD,strWhereExp,objSelect);

            migrationInfoWriter(getString(context,MIGRATING_IMAGE));
            migrationInfoWriter(getString(context,FILLLINE));

            migrationErrWriter(getString(context,MIGRATING_ERROR));
            migrationErrWriter(getString(context,FILLLINE));

            for(int i=0;i<mapList.size();i++)
            {
                strImageId = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_ID);
                strImageName = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_NAME);

                DomainObject domImage = DomainObject.newInstance(context, strImageId);

                strFileName = domImage.getInfo(context, "format[" +strImageFormat+ "].file.name");
                StringList fileName = new StringList(1);
                fileName.addElement(strFileName);

                objectMap = new HashMap();

                try
                {
                    // Check in the supported file in all other formats
                    uploadParamsMap.put("objectId", strImageId);
                    uploadParamsMap.put("fcsEnabled", "true");
                    objectMap.put("fileName", fileName);

                    generateTransformatedImages(context, uploadParamsMap, (HashMap)objectMap);

                    migrationInfoWriter(getString(context,FILLLINE));
                    StringBuffer sbTemp=new StringBuffer(10);
                    sbTemp.append(IMAGE_OBJECT);
                    sbTemp.append(DomainConstants.SELECT_NAME);
                    sbTemp.append(SYMB_EQUAL);
                    sbTemp.append(strImageName);
                    sbTemp.append(SYMB_OR);
                    sbTemp.append(DomainConstants.SELECT_ID);
                    sbTemp.append(SYMB_EQUAL);
                    sbTemp.append(strImageId);
                    migrationInfoWriter(sbTemp.toString());
                    migrationInfoWriter(getString(context,MIGRATE_SUCCESS));

                }
                catch(Exception ex)
                {
                    // If Image object is connected to any object
                    mapObjectList = domImage.getRelatedObjects(context,
                                                               stbRelSelect.toString(),
                                                               DomainConstants.QUERY_WILDCARD,
                                                               objSelect,
                                                               relSelect,
                                                               true,
                                                               false,
                                                               (short)1,
                                                               DomainConstants.EMPTY_STRING,
                                                               DomainConstants.EMPTY_STRING);

                    if(mapObjectList != null && !mapObjectList.isEmpty() && mapObjectList.size() != 0)
                    {
                        //Drop all the Image relationships on the Image object.
                        //Also drop all Primary Image relationships (if any).
                        //Log these actions.
                        strRelIds = new String[mapObjectList.size()];
                        strRelNames = new String[mapObjectList.size()];
                        strContextObjIds = new String[mapObjectList.size()];
                        strContextObjTypes = new String[mapObjectList.size()];
                        strContextObjNames = new String[mapObjectList.size()];
                        strContextObjRevisions = new String[mapObjectList.size()];

                        for(int j = 0; j < mapObjectList.size(); j++)
                        {
                            strRelIds[j] = (String)( (Map)mapObjectList.get(j) ).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                            strRelNames[j] = (String)( (Map)mapObjectList.get(j) ).get(DomainConstants.SELECT_RELATIONSHIP_NAME);

                            strContextObjIds[j] = (String)( (Map)mapObjectList.get(j) ).get(DomainConstants.SELECT_ID);
                            strContextObjTypes[j] = (String)( (Map)mapObjectList.get(j) ).get(DomainConstants.SELECT_TYPE);
                            strContextObjNames[j] = (String)( (Map)mapObjectList.get(j) ).get(DomainConstants.SELECT_NAME);
                            strContextObjRevisions[j] = (String)( (Map)mapObjectList.get(j) ).get(DomainConstants.SELECT_REVISION);
                        }

                        DomainRelationship.disconnect(context, strRelIds);

                        migrationErrWriter(getString(context,FILLLINE));
                        migrationErrWriter(getString(context,IMAGE_WITH_REL));

                        StringBuffer sbTemp = new StringBuffer(10);

                        sbTemp.append(IMAGE_OBJECT);
                        sbTemp.append(DomainConstants.SELECT_NAME);
                        sbTemp.append(SYMB_EQUAL);
                        sbTemp.append(strImageName);
                        sbTemp.append(SYMB_OR);
                        sbTemp.append(DomainConstants.SELECT_ID);
                        sbTemp.append(SYMB_EQUAL);
                        sbTemp.append(strImageId);
                        migrationErrWriter(sbTemp.toString());

                        migrationErrWriter(CONTEXT_OBJECT_DETAILS);

                        for(int j = 0; j < mapObjectList.size(); j++)
                        {
                            StringBuffer sbTemp1 = new StringBuffer((mapObjectList.size()*25));
                            sbTemp1.append(DomainConstants.SELECT_TYPE);
                            sbTemp1.append(SYMB_EQUAL);
                            sbTemp1.append(strContextObjTypes[j]);
                            sbTemp1.append(SYMB_OR);
                            sbTemp1.append(DomainConstants.SELECT_NAME);
                            sbTemp1.append(SYMB_EQUAL);
                            sbTemp1.append(strContextObjNames[j]);
                            sbTemp1.append(SYMB_OR);
                            sbTemp1.append(DomainConstants.SELECT_REVISION);
                            sbTemp1.append(SYMB_EQUAL);
                            sbTemp1.append(strContextObjRevisions[j]);
                            sbTemp1.append(SYMB_OR);
                            sbTemp1.append(DomainConstants.SELECT_ID);
                            sbTemp1.append(SYMB_EQUAL);
                            sbTemp1.append(strContextObjIds[j]);
                            sbTemp1.append(SYMB_OR);
                            sbTemp1.append(REL_NAME);
                            sbTemp1.append(SYMB_EQUAL);
                            sbTemp1.append(strRelNames[j]);
                            migrationErrWriter(sbTemp1.toString());
                        }

                        migrationErrWriter(getString(context,DISCONNECTING_REL));

                    }
                    else // Image is an orphan
                    {
                        // Just log it in the Log file
                        migrationErrWriter(getString(context,FILLLINE));
                        migrationErrWriter(getString(context,ORPHAN_IMAGE));

                        StringBuffer sbTemp = new StringBuffer(10);
                        sbTemp.append(IMAGE_OBJECT);
                        sbTemp.append(DomainConstants.SELECT_NAME);
                        sbTemp.append(SYMB_EQUAL);
                        sbTemp.append(strImageName);
                        sbTemp.append(SYMB_OR);
                        sbTemp.append(DomainConstants.SELECT_ID);
                        sbTemp.append(SYMB_EQUAL);
                        sbTemp.append(strImageId);
                        migrationErrWriter(sbTemp.toString());
                    }
                }
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
    }

    /**
     * this method ensures that if any object contains Image(s) then one of them will be a primary Image
     * @param context the eMatrix <code>Context</code> object
     * @param args  - is a string array
     * @return void
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6SP2
     */

    protected void connectPrimaryImageToObjects(Context context,String[] args) throws Exception
    {
         try
        {
            StringList objSelect = new StringList(4);
            MapList mapList = null;
            MapList mlImageIds = null;

            StringBuffer stbTypeSelect = new StringBuffer(50);
            stbTypeSelect = stbTypeSelect.append(ProductLineConstants.TYPE_PRODUCT_LINE)
                            .append(SYMB_COMMA)
                            .append(ProductLineConstants.TYPE_MODEL)
                            .append(SYMB_COMMA)
                            .append(ProductLineConstants.TYPE_FEATURES)
                            .append(SYMB_COMMA)
                            .append(ProductLineConstants.TYPE_PRODUCTS);

            StringBuffer sbWhereExp = new StringBuffer(150);
            String strWhereExp="";

            //object selects
            objSelect.addElement(DomainConstants.SELECT_ID);
            objSelect.addElement(DomainConstants.SELECT_TYPE);
            objSelect.addElement(DomainConstants.SELECT_NAME);
            objSelect.addElement(DomainConstants.SELECT_REVISION);

            //where expression
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(SYMB_FROM);
            sbWhereExp.append(SYMB_OPEN_BRACKET);
            sbWhereExp.append(ProductLineConstants.RELATIONSHIP_IMAGES);
            sbWhereExp.append(SYMB_CLOSE_BRACKET);
            sbWhereExp.append(SYMB_DOT);
            sbWhereExp.append(SYMB_TO);
            sbWhereExp.append(SYMB_DOT);
            sbWhereExp.append(DomainConstants.SELECT_ID);
            sbWhereExp.append(SYMB_NOT_EQUAL);
            sbWhereExp.append(SYMB_NULL);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
            sbWhereExp.append(SYMB_AND);
            sbWhereExp.append(SYMB_NOT);
            sbWhereExp.append(SYMB_OPEN_PARAN);
            sbWhereExp.append(SYMB_FROM);
            sbWhereExp.append(SYMB_OPEN_BRACKET);
            sbWhereExp.append(ProductLineConstants.RELATIONSHIP_PRIMARY_IMAGE);
            sbWhereExp.append(SYMB_CLOSE_BRACKET);
            sbWhereExp.append(SYMB_DOT);
            sbWhereExp.append(SYMB_TO);
            sbWhereExp.append(SYMB_DOT);
            sbWhereExp.append(DomainConstants.SELECT_ID);
            sbWhereExp.append(SYMB_NOT_EQUAL);
            sbWhereExp.append(SYMB_NULL);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
            sbWhereExp.append(SYMB_CLOSE_PARAN);
            sbWhereExp.append(SYMB_CLOSE_PARAN);

            strWhereExp=sbWhereExp.toString();

            // return the context objects, which has images connected but no primary image
            mapList = DomainObject.findObjects(context,
                                               stbTypeSelect.toString(),
                                               DomainConstants.QUERY_WILDCARD,
                                               strWhereExp,
                                               objSelect);

            String strObjectId = DomainConstants.EMPTY_STRING;
            String strObjectType = DomainConstants.EMPTY_STRING;
            String strObjectName = DomainConstants.EMPTY_STRING;
            String strObjectRevision = DomainConstants.EMPTY_STRING;
            String strImageId = DomainConstants.EMPTY_STRING;
            String strImageName = DomainConstants.EMPTY_STRING;
            Image image = new Image();

            migrationInfoWriter(getString(context,FILLLINE));
            migrationInfoWriter(getString(context,FILLLINE));
            migrationInfoWriter(getString(context,IMAGE_WITH_REL));

            for(int i=0;i<mapList.size();i++)
            {
                // expand each to get Images connected.
                // Make the first available Image the primary Image of the object

                strObjectId = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_ID);
                strObjectType = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_TYPE);
                strObjectName = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_NAME);
                strObjectRevision = (String)( (Map)mapList.get(i) ).get(DomainConstants.SELECT_REVISION);

                DomainObject domainObject = DomainObject.newInstance(context, strObjectId);

                mlImageIds = domainObject.getRelatedObjects(context,
                                                            ProductLineConstants.RELATIONSHIP_IMAGES,
                                                            ProductLineConstants.TYPE_IMAGE,
                                                            objSelect,
                                                            null,
                                                            false,
                                                            true,
                                                            (short)1,
                                                            DomainConstants.EMPTY_STRING,
                                                            DomainConstants.EMPTY_STRING);

                strImageId = (String)((Map)mlImageIds.get(0)).get(DomainConstants.SELECT_ID);
                strImageName = (String)((Map)mlImageIds.get(0)).get(DomainConstants.SELECT_NAME);

                image.setImageAsPrimary(context,
                                        strObjectId,
                                        ProductLineConstants.TYPE_IMAGE,
                                        ProductLineConstants.RELATIONSHIP_PRIMARY_IMAGE,
                                        strImageId);

                migrationInfoWriter(getString(context,FILLLINE));

                StringBuffer sbTemp = new StringBuffer(10);
                sbTemp.append(CONTEXT_OBJECT);
                sbTemp.append(DomainConstants.SELECT_TYPE);
                sbTemp.append(SYMB_EQUAL);
                sbTemp.append(strObjectType);
                sbTemp.append(SYMB_OR);
                sbTemp.append(DomainConstants.SELECT_NAME);
                sbTemp.append(SYMB_EQUAL);
                sbTemp.append(strObjectName);
                sbTemp.append(SYMB_OR);
                sbTemp.append(DomainConstants.SELECT_REVISION);
                sbTemp.append(SYMB_EQUAL);
                sbTemp.append(strObjectRevision);
                sbTemp.append(SYMB_OR);
                sbTemp.append(DomainConstants.SELECT_ID);
                sbTemp.append(SYMB_EQUAL);
                sbTemp.append(strObjectId);
                migrationInfoWriter(sbTemp.toString());

                StringBuffer sbTemp1 = new StringBuffer(10);
                sbTemp1.append(IMAGE_OBJECT);
                sbTemp1.append(DomainConstants.SELECT_NAME);
                sbTemp1.append(SYMB_EQUAL);
                sbTemp1.append(strImageName);
                sbTemp1.append(SYMB_OR);
                sbTemp1.append(DomainConstants.SELECT_ID);
                sbTemp1.append(SYMB_EQUAL);
                sbTemp1.append(strImageId);
                migrationInfoWriter(sbTemp1.toString());

                migrationInfoWriter(getString(context,MAKE_PRIMARY));
            }
        }
        catch(Exception ex)
        {
            throw ex;
        }
    }

    /**
    * This method writes to the log file using the Printwriter object
    * @param strLogEnrty - Message to write
    * @return void
    * @throws Exception if the operation fails
    * @since ProductCentral 10-6-SP2
    * @grade 0
    */
    protected void migrationInfoWriter(String strLogEnrty)
    throws Exception
    {
        if (strLogEnrty!=null && strLogEnrty.length()>0)
        {
            psLogWriter.println(strLogEnrty);

        }
    }

    /**
    * This method writes to the ERR file using the Printwriter object
    * @param strLogEnrty - Message to write
    * @return void
    * @throws Exception if the operation fails
    * @since ProductCentral 10-6-SP2
    * @grade 0
    */
    protected void migrationErrWriter(String strLogEnrty)
    throws Exception
    {
        if (strLogEnrty!=null && strLogEnrty.length()>0)
        {
            psErrWriter.println(strLogEnrty);
        }
    }

    /**
    * This method returns internalized value for the passed key in
    * ProductCentral string resouce file.
    * @param context - The eMatrix <code>Context</code> object
    * @param strKey - Property file entry
    * @since ProductCentral 10-6-SP2
    * @grade 0
    */
    private String getString(Context context,String strKey)throws FrameworkException
    {
        String strLocale = context.getSession().getLanguage();
        return EnoviaResourceBundle.getProperty(context,"ProductLine",strLocale,strKey);
    }

}
