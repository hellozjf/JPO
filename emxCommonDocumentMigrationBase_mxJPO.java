/*
 * emxCommonDocumentMigrationBase.java
 * program migrates the data into Common Document model i.e. to 10.5 schema.
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

  import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.MatrixWriter;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

  public class emxCommonDocumentMigrationBase_mxJPO
  {
      public static int _counter  = 0;
      public static int _sequence  = 1;
      public static java.io.File _oidsFile = null;
      public static BufferedWriter _fileWriter = null;
      public static StringList _objectidList = null;
      public static int _chunk = 0;
      public static String convertNonRevisionedDocument = "false";

      BufferedWriter writer = null;
      FileWriter errorLog = null;
      FileWriter warningLog = null;
      FileWriter convertedOidsLog = null;
      FileWriter statusLog = null;

      static String documentDirectory = "";
      static int minRange = 0;
      static int maxRange = 0;
      static String ATTRIBUTE_ISVERSIONOBJECT                           = PropertyUtil.getSchemaProperty("attribute_IsVersionObject");
      static String ATTRIBUTE_TITLE                                     = PropertyUtil.getSchemaProperty("attribute_Title");
      static String ATTRIBUTE_SUSPEND_VERSIONING                        = PropertyUtil.getSchemaProperty("attribute_SuspendVersioning");
      static String TYPE_DRAWING_PRINT                                  = PropertyUtil.getSchemaProperty("type_DrawingPrint");
      static String POLICY_VERSION                                      = PropertyUtil.getSchemaProperty("policy_Version");

      static String RELATIONSHIP_HAS_DOCUMENTS                          = PropertyUtil.getSchemaProperty("relationship_HasDocuments");
      static String RELATIONSHIP_VAULTED_OBJECTS_REV2                   = PropertyUtil.getSchemaProperty("relationship_VaultedDocumentsRev2");
      static String RELATIONSHIP_DOCUMENT_SHEETS                        = PropertyUtil.getSchemaProperty("relationship_DocumentSheets");
      static String RELATIONSHIP_REQUIREMENT_SPECIFICATION              = PropertyUtil.getSchemaProperty("relationship_RequirementSpecification");
      static String RELATIONSHIP_ORIGINATING_REQUIREMENT                = PropertyUtil.getSchemaProperty("relationship_Originating_Requirement");
      static String RELATIONSHIP_BUILD_SPECIFICATION                    = PropertyUtil.getSchemaProperty("relationship_BuildSpecification");
      static String RELATIONSHIP_PRODUCT_SPECIFICATION                  = PropertyUtil.getSchemaProperty("relationship_ProductSpecification");
      static String RELATIONSHIP_FEATURE_TEST_SPECIFICATION             = PropertyUtil.getSchemaProperty("relationship_FeatureTestSpecification");
      static String RELATIONSHIP_FEATURE_FUNCTIONAL_SPECIFICATION       = PropertyUtil.getSchemaProperty("relationship_FeatureFunctionalSpecification");
      static String RELATIONSHIP_FEATURE_DESIGN_SPECIFICATION           = PropertyUtil.getSchemaProperty("relationship_FeatureDesignSpecification");
      static String RELATIONSHIP_FEATURE_SPECIFICATION                  = PropertyUtil.getSchemaProperty("relationship_FeatureSpecification");
      public static String FORMAT_JT = PropertyUtil.getSchemaProperty("format_JT");
      /** type "Package". */
      public static String TYPE_PACKAGE =
              PropertyUtil.getSchemaProperty("type_Package");
      /** type "Part". */
      public static String TYPE_PART =
              PropertyUtil.getSchemaProperty("type_Part");
      /** type "Part Family". */
      public static String TYPE_PART_FAMILY =
              PropertyUtil.getSchemaProperty("type_PartFamily");
      /** type "Line Item". */
      public static String TYPE_LINE_ITEM =
              PropertyUtil.getSchemaProperty("type_LineItem");
      /** type "CAD Model". */
      public static String TYPE_CAD_MODEL =
              PropertyUtil.getSchemaProperty("type_CADModel");
      /** type "ECR". */
      public static String TYPE_ECR =
              PropertyUtil.getSchemaProperty("type_ECR");
        /** type "Markup". */
      public static String TYPE_MARKUP =
              PropertyUtil.getSchemaProperty("type_Markup");
      /** type "Meeting". */
      public static String TYPE_MEETING =
              PropertyUtil.getSchemaProperty("type_Meeting");
      /** type "Message". */
      public static String TYPE_MESSAGE =
              PropertyUtil.getSchemaProperty("type_Message");
      public static String TYPE_RFQ =
              PropertyUtil.getSchemaProperty("type_RequestToSupplier");
      /** type "RTS Quotation". */
      public static String TYPE_RTS_QUOTATION =
              PropertyUtil.getSchemaProperty("type_RTSQuotation");
      /** type "RTS Template". */
      public static String TYPE_RTS_TEMPLATE =
              PropertyUtil.getSchemaProperty("type_RTSTemplate");
      /** type "Sketch". */
      public static String TYPE_SKETCH =
              PropertyUtil.getSchemaProperty("type_Sketch");
      /** type "Supplier Line Item", renamed from "RTS Supplier Part". */
      public static String TYPE_SUPPLIER_LINE_ITEM =
              PropertyUtil.getSchemaProperty("type_RTSSupplierPart");
      /** type "CAD Drawing". */
      public static String TYPE_CAD_DRAWING =
              PropertyUtil.getSchemaProperty("type_CADDrawing");
      /** policy "RTS Quotation". */
      public static String POLICY_RTS_QUOTATION =
              PropertyUtil.getSchemaProperty("policy_RTSQuotation");
      /** policy "RTS Template". */
      public static String POLICY_RTS_TEMPLATE =
              PropertyUtil.getSchemaProperty("policy_RTSTemplate");

      /** The Workspace Vault type used to manage documents and other objects. */
      public static String TYPE_WORKSPACE_VAULT =
              PropertyUtil.getSchemaProperty("type_ProjectVault");
      public static String TYPE_TASK =
              PropertyUtil.getSchemaProperty("type_Task");
      /** state "Inactive" for the "RTS Template" policy. */
      public static String STATE_RTS_TEMPLATE_INACTIVE =
              PropertyUtil.getSchemaProperty("policy",
                                             POLICY_RTS_TEMPLATE,
                                             "state_Inactive");
      /** state "Open" for the "RTS Quotation" policy. */
      public static String STATE_RTS_QUOTATION_OPEN =
              PropertyUtil.getSchemaProperty("policy",
                                             POLICY_RTS_QUOTATION,
                                             "state_Open");
      /** policy "Request To Supplier". */
      public static String POLICY_REQUEST_TO_SUPPLIER =
              PropertyUtil.getSchemaProperty("policy_RequestToSupplier");

      public static String VAULT_PRODUCTION = PropertyUtil.getSchemaProperty("vault_eServiceProduction");

      public static String TYPE_TECHNICAL_SPECIFICATION           = PropertyUtil.getSchemaProperty("type_TechnicalSpecification");
      public static String TYPE_ASSESSMENT           = PropertyUtil.getSchemaProperty("type_Assessment");
      public static String TYPE_FINANCIALS           = PropertyUtil.getSchemaProperty("type_Financials");
      public static String TYPE_RISK           = PropertyUtil.getSchemaProperty("type_Risk");
      public static String TYPE_BUSINESS_GOALS           = PropertyUtil.getSchemaProperty("type_BusinessGoal");
      public static String TYPE_QUALITY           = PropertyUtil.getSchemaProperty("type_Quality");
      public static String TYPE_GENERIC_DOCUMENT           = PropertyUtil.getSchemaProperty("type_GenericDocument");
      public static String TYPE_DOCUMENT_SHEET           = PropertyUtil.getSchemaProperty("type_DocumentSheet");
      public static String TYPE_BUILDS                               = PropertyUtil.getSchemaProperty("type_Builds");
      public static String TYPE_FEATURES                             = PropertyUtil.getSchemaProperty("type_Features");
      public static String TYPE_PRODUCTS                             = PropertyUtil.getSchemaProperty("type_Products");
      public static String TYPE_INCIDENT                             = PropertyUtil.getSchemaProperty("type_Incident");
      public static String TYPE_REQUIREMENT                          = PropertyUtil.getSchemaProperty("type_Requirement");
      public static String TYPE_TEST_CASE                            = PropertyUtil.getSchemaProperty("type_TestCase");
      public static String TYPE_USE_CASE                             = PropertyUtil.getSchemaProperty("type_UseCase");
      public static String TYPE_ISSUE                                = PropertyUtil.getSchemaProperty("type_Issue");
      public static String TYPE_SPECIFICATION                        = PropertyUtil.getSchemaProperty("type_Specification");
      public static String RELATIONSHIP_LATEST_VERSION               = PropertyUtil.getSchemaProperty("relationship_LatestVersion");
      public static String RELATIONSHIP_MEETING_ATTACHMENTS               = PropertyUtil.getSchemaProperty("relationship_MeetingAttachments");
      public static String RELATIONSHIP_MESSAGE_ATTACHMENTS               = PropertyUtil.getSchemaProperty("relationship_MessageAttachments");
      public static String RELATIONSHIP_PART_SPECIFICATION = PropertyUtil.getSchemaProperty("relationship_PartSpecification");
      public static String RELATIONSHIP_PART_FAMILY_REFERENCE_DOCUMENT = PropertyUtil.getSchemaProperty("relationship_PartFamilyReferenceDocument");
      public static String RELATIONSHIP_REFERENCE_DOCUMENT = PropertyUtil.getSchemaProperty("relationship_ReferenceDocument");
      public static String RELATIONSHIP_TASK_DELIVERABLE = PropertyUtil.getSchemaProperty("relationship_TaskDeliverable");
      public static String RELATIONSHIP_VERSION               = PropertyUtil.getSchemaProperty("relationship_Version");
      public static String SELECT_NAME = "name";
      public static String SELECT_REVISION = "revision";
      /** select for "format.file.name". */
      public static String SELECT_FILE_NAME = "format.file.name";
      /** select for "format.file.format". */
      public static String SELECT_FILE_FORMAT = "format.file.format";
      /** select for "format.hasFile.". */
      public static String SELECT_FORMAT_HASFILE = "format.hasfile";
      /** select for "id". */
      public static String SELECT_ID = "id";
      /** select for "locked". */
      public static String SELECT_LOCKED = "locked";
      /** The user who has the object locked. */
      public static String SELECT_LOCKER =  "locker";
      /** select for "current". */
      public static String SELECT_CURRENT = "current";
      /** select for "type". */
      public static String SELECT_TYPE = "type";
      /** select for "policy". */
      public static String SELECT_POLICY = "policy";
      /** select for "vault". */
      public static String SELECT_VAULT = "vault";
      /** select for "originated". */
      public static String SELECT_ORIGINATED = "originated";
      /** select for "owner". */
      public static String SELECT_OWNER = "owner";
      /** select for "id[connection]". */
      public static String SELECT_RELATIONSHIP_ID = "id[connection]";

      /** relationship "Vaulted Objects". */
      public static String RELATIONSHIP_VAULTED_OBJECTS =
              PropertyUtil.getSchemaProperty("relationship_VaultedDocuments");
      /** The version of the Document. */
      public static String ATTRIBUTE_FILE_VERSION =
              PropertyUtil.getSchemaProperty("attribute_FileVersion");
      /** type "Document". */
      public static String TYPE_DOCUMENT =
              PropertyUtil.getSchemaProperty("type_Document");
      /** type "Document". */
      public static String TYPE_DOCUMENTS =
              PropertyUtil.getSchemaProperty("type_DOCUMENTS");

      /** attribute "Originator". */
      public static String ATTRIBUTE_ORIGINATOR =
              PropertyUtil.getSchemaProperty("attribute_Originator");
      static String  SELECT_FROM_LATEST_VERSION_RELATIONSHIP_ID          = "from[" + RELATIONSHIP_LATEST_VERSION + "].id";
      static String  SELECT_TO_LATEST_VERSION_RELATIONSHIP_ID            = "to[" + RELATIONSHIP_LATEST_VERSION + "].id";
      static String  SELECT_VERSION_RELATIONSHIP_ID                      = "from[" + RELATIONSHIP_VERSION + "].id";
      static String  SELECT_VERSION_OBJECT_ID                            = "from[" + RELATIONSHIP_VERSION + "].to.id";
      static String  SELECT_VERSION_OBJECT_NAME                          = "from[" + RELATIONSHIP_VERSION + "].to." + SELECT_NAME;
      static String  SELECT_VERSION_OBJECT_REVISION                      = "from[" + RELATIONSHIP_VERSION + "].to." + SELECT_REVISION;
      static String  SELECT_VERSION_OBJECT_TITLE                         = "from[" + RELATIONSHIP_VERSION + "].to.attribute[" + ATTRIBUTE_TITLE + "]";
      static String  SELECT_VERSION_OBJECT_FILE_VERSION                  = "from[" + RELATIONSHIP_VERSION + "].to.attribute[" + ATTRIBUTE_FILE_VERSION + "]";
      static String  SELECT_VERSION_OBJECT_FILE_NAME                     = "from[" + RELATIONSHIP_VERSION + "].to." + SELECT_FILE_NAME;
      static String  SELECT_VERSION_OBJECT_FILE_FORMAT                   = "from[" + RELATIONSHIP_VERSION + "].to." + SELECT_FILE_FORMAT;
      static String  SELECT_VERSION_OBJECT_HASFILES                      = "from[" + RELATIONSHIP_VERSION + "].to." + SELECT_FORMAT_HASFILE;
      static String  SELECT_TEAM_FOLDER_ID                               = "to[" + RELATIONSHIP_VAULTED_OBJECTS + "].from.id";
      static String  SELECT_FROM_ID                                      = "to.from.id";
      static String  SELECT_FROM_RELATIONSHIP_NAME                       = "to.name";
      static String  SELECT_FROM_TYPE                                    = "to.from.type";
      static String  SELECT_FILE_VERSION                                 = "attribute[" + ATTRIBUTE_FILE_VERSION + "]";
      static String  SELECT_TITLE                                        = "attribute[" + ATTRIBUTE_TITLE + "]";

      static String SELECT_RELATIONSHIP_PART_SPECIFICATION_FROM_TYPE = "to[" + RELATIONSHIP_PART_SPECIFICATION + "].from.type";
      static String SELECT_RELATIONSHIP_PART_FAMILY_REFERENCE_DOCUMENT_FROM_TYPE = "to[" + RELATIONSHIP_PART_FAMILY_REFERENCE_DOCUMENT + "].from.type";
      static String SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_TYPE = "to[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].from.type";
      static String SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_STATE = "to[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].from.current";


      static String SELECT_RELATIONSHIP_VAULTED_OBJECTS_FROM_TYPE = "to[" + RELATIONSHIP_VAULTED_OBJECTS + "].from.type";
      static String SELECT_RELATIONSHIP_MESSAGE_ATTACHMENTS_FROM_TYPE = "to[" + RELATIONSHIP_MESSAGE_ATTACHMENTS + "].from.type";
      static String SELECT_RELATIONSHIP_MEETING_ATTACHMENTS_FROM_TYPE = "to[" + RELATIONSHIP_MEETING_ATTACHMENTS + "].from.type";
      static String SELECT_RELATIONSHIP_TASK_DELIVERABLE_FROM_TYPE = "to[" + RELATIONSHIP_TASK_DELIVERABLE + "].from.type";
      static String SELECT_RELATIONSHIP_HAS_DOCUMENTS_FROM_TYPE = "to[" + RELATIONSHIP_HAS_DOCUMENTS + "].from.type";
      static String SELECT_RELATIONSHIP_VAULTED_OBJECTS_REV2_FROM_TYPE = "to[" + RELATIONSHIP_VAULTED_OBJECTS_REV2 + "].from.type";
      static String SELECT_TYPE_DOCUMENT_SHEET_FROM_TYPE = "to[" + TYPE_DOCUMENT_SHEET + "].from.type";
      /** relationship "ECR Reference". */
      public static String RELATIONSHIP_ECR_REFERENCE =
              PropertyUtil.getSchemaProperty("relationship_ECRReference");

      /** relationship "Line Item". */
      public static String RELATIONSHIP_LINE_ITEM =
              PropertyUtil.getSchemaProperty("relationship_LineItem");
      /** relationship "Line Item Split". */
      public static String RELATIONSHIP_LINE_ITEM_SPLIT =
              PropertyUtil.getSchemaProperty("relationship_LineItemSplit");
      // For getting state of RFQ when Document is connected to Line Item
      static String SELECT_LINE_ITEM_RFQ_STATE = "to[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].from.to["+ RELATIONSHIP_LINE_ITEM +"].from.current";
      /** relationship "Supplier Line Item". */
      public static String RELATIONSHIP_SUPPLIER_LINE_ITEM =
              PropertyUtil.getSchemaProperty("relationship_SupplierLineItem");
      // For getting state of RFQ when Document is connected to Line Item Split
      static String SELECT_LINE_ITEM_SPLIT_RFQ_STATE = "to[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].from.to["+ RELATIONSHIP_LINE_ITEM_SPLIT +"].from.to["+ RELATIONSHIP_LINE_ITEM +"].from.current";
      // For getting state of RFQ Quotation when Document is connected to Supplier Line Item
      static String SELECT_SUPPLIER_LINE_ITEM_RFQ_QUOTATION_STATE = "to[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].from.to["+ RELATIONSHIP_SUPPLIER_LINE_ITEM +"].from.current";
      // For getting state of RFQ Quotation when Document is connected to ECR
      static String SELECT_ECR_RFQ_QUOTATION_STATE = "to[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].from.to["+ RELATIONSHIP_ECR_REFERENCE +"].from.to["+ RELATIONSHIP_SUPPLIER_LINE_ITEM +"].from.current";

      /** Create new instance of emxIntegrationMigration class. */
      static protected emxIntegrationMigration_mxJPO iefMigration = null;

      int TEAM_SOURCING = 1;
      int PMC = 2;
      int DOCUMENT_PRODUCT_SPEC = 3;
      int EC = 4;
      int IEF = 5;
      int UNKNOWN_MODEL = 99;
      static StringList mxMainObjectSelects = new StringList(51);
      public static MQLCommand mqlCommand = null;
      static
      {
          DomainObject.MULTI_VALUE_LIST.add(SELECT_VERSION_RELATIONSHIP_ID);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_VERSION_OBJECT_ID);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_VERSION_OBJECT_NAME);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_VERSION_OBJECT_REVISION);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_VERSION_OBJECT_TITLE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_VERSION_OBJECT_FILE_VERSION);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_VERSION_OBJECT_FILE_NAME);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_VERSION_OBJECT_FILE_FORMAT);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_VERSION_OBJECT_HASFILES);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_FROM_ID);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_FROM_TYPE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_FROM_RELATIONSHIP_NAME);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_PART_SPECIFICATION_FROM_TYPE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_PART_FAMILY_REFERENCE_DOCUMENT_FROM_TYPE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_TYPE);

          DomainObject.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_VAULTED_OBJECTS_FROM_TYPE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_MESSAGE_ATTACHMENTS_FROM_TYPE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_MEETING_ATTACHMENTS_FROM_TYPE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_TASK_DELIVERABLE_FROM_TYPE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_HAS_DOCUMENTS_FROM_TYPE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_VAULTED_OBJECTS_REV2_FROM_TYPE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_TYPE_DOCUMENT_SHEET_FROM_TYPE);

          DomainObject.MULTI_VALUE_LIST.add(SELECT_LINE_ITEM_RFQ_STATE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_LINE_ITEM_SPLIT_RFQ_STATE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_SUPPLIER_LINE_ITEM_RFQ_QUOTATION_STATE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_ECR_RFQ_QUOTATION_STATE);
          DomainObject.MULTI_VALUE_LIST.add(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_STATE);


          mxMainObjectSelects.add(SELECT_FILE_FORMAT);
          mxMainObjectSelects.add(SELECT_FILE_NAME);
          mxMainObjectSelects.add(SELECT_FORMAT_HASFILE);
          mxMainObjectSelects.add(SELECT_TYPE);
          mxMainObjectSelects.add(SELECT_NAME);
          mxMainObjectSelects.add(SELECT_REVISION);
          mxMainObjectSelects.add(SELECT_VAULT);
          mxMainObjectSelects.add(SELECT_POLICY);
          mxMainObjectSelects.add(SELECT_OWNER);
          mxMainObjectSelects.add(SELECT_ID);
          mxMainObjectSelects.add(SELECT_LOCKED);
          mxMainObjectSelects.add(SELECT_LOCKER);
          mxMainObjectSelects.add(SELECT_ORIGINATED);
          mxMainObjectSelects.add("last.id");
          mxMainObjectSelects.add("modified");
          mxMainObjectSelects.add("previous.id");
          mxMainObjectSelects.add(SELECT_TITLE);

          mxMainObjectSelects.add(SELECT_FROM_RELATIONSHIP_NAME);
          mxMainObjectSelects.add(SELECT_FROM_ID);
          mxMainObjectSelects.add(SELECT_FROM_TYPE);

          mxMainObjectSelects.add(SELECT_VERSION_RELATIONSHIP_ID);
          mxMainObjectSelects.add(SELECT_VERSION_OBJECT_ID);
          mxMainObjectSelects.add(SELECT_VERSION_OBJECT_NAME);
          mxMainObjectSelects.add(SELECT_VERSION_OBJECT_REVISION);
          mxMainObjectSelects.add(SELECT_VERSION_OBJECT_TITLE);
          mxMainObjectSelects.add(SELECT_VERSION_OBJECT_FILE_VERSION);
          mxMainObjectSelects.add(SELECT_VERSION_OBJECT_FILE_NAME);
          mxMainObjectSelects.add(SELECT_VERSION_OBJECT_FILE_FORMAT);
          mxMainObjectSelects.add(SELECT_VERSION_OBJECT_HASFILES);

          mxMainObjectSelects.add(SELECT_RELATIONSHIP_PART_SPECIFICATION_FROM_TYPE);
          mxMainObjectSelects.add(SELECT_RELATIONSHIP_PART_FAMILY_REFERENCE_DOCUMENT_FROM_TYPE);
          mxMainObjectSelects.add(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_TYPE);
          mxMainObjectSelects.add(SELECT_RELATIONSHIP_VAULTED_OBJECTS_FROM_TYPE);
          mxMainObjectSelects.add(SELECT_RELATIONSHIP_MESSAGE_ATTACHMENTS_FROM_TYPE);
          mxMainObjectSelects.add(SELECT_RELATIONSHIP_MEETING_ATTACHMENTS_FROM_TYPE);
          mxMainObjectSelects.add(SELECT_RELATIONSHIP_TASK_DELIVERABLE_FROM_TYPE);
          mxMainObjectSelects.add(SELECT_RELATIONSHIP_HAS_DOCUMENTS_FROM_TYPE);
          mxMainObjectSelects.add(SELECT_RELATIONSHIP_VAULTED_OBJECTS_REV2_FROM_TYPE);
          mxMainObjectSelects.add(SELECT_TYPE_DOCUMENT_SHEET_FROM_TYPE);
          mxMainObjectSelects.add(SELECT_FROM_LATEST_VERSION_RELATIONSHIP_ID);
          mxMainObjectSelects.add(SELECT_TO_LATEST_VERSION_RELATIONSHIP_ID);


      }

      static public Map typeMapping = new HashMap();

      long startTime = System.currentTimeMillis();
      long migrationStartTime = System.currentTimeMillis();

      boolean isConverted = false;
      boolean isTeamSourcingLowerRevisionDocument = false;
      boolean scan = false;
      String error = null;

      //  Suspend Versiong attribute ***
      StringList sourcingDocumentsList  = new StringList();
      StringBuffer migratedOids = new StringBuffer(20000);
      boolean debug = false;
      static long unconvertedChunkSize = 50000;
      long unconvertedObjectCount = 0;
      int unconvertedFileCount = 1;
      StringBuffer statusBuffer = new StringBuffer(50000);
      String failureId = "";
      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxCommonDocumentMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
        writer     = new BufferedWriter(new MatrixWriter(context));
        mqlCommand = new MQLCommand();
      }


      public static String getParentType(Context context, String type) throws Exception
      {
          String parentType = (String)typeMapping.get(type);
          if ( parentType == null || "".equals(parentType))
          {
              setParentTypeMapping(context, type);
          }
          parentType = (String)typeMapping.get(type);
          if ( parentType != null )
          {
              return parentType;
          } else {
              return type;
          }

      }

      public static void setParentTypeMapping(Context context, String type) throws Exception
      {
          String currentType = type;
          BusinessType bType = new BusinessType(currentType, context.getVault());
          String parentType = bType.getParent(context);

          if ( parentType != null && !"".equals(parentType) )
          {
              while ( !parentType.equals(TYPE_DOCUMENTS) && !"".equals(parentType) )
              {
                  currentType = parentType;
                  bType = new BusinessType(currentType, context.getVault());
                  parentType = bType.getParent(context);
              }
          }
          typeMapping.put(type, currentType);
      }

      public void mqlLogWriter(String command) throws Exception
      {
          if(debug)
          {
              writer.write(command);
              writer.flush();
              logWarning(command);
          }
      }
      public void mqlLogRequiredInformationWriter(String command) throws Exception
      {
          writer.write(command);
          writer.flush();
          logWarning(command);
      }

      public void writeUnconvertedOID(String command) throws Exception
      {
          if(unconvertedObjectCount < unconvertedChunkSize)
          {
              errorLog.write(command);
              errorLog.flush();
              unconvertedObjectCount++;
          } else {
              errorLog.close();
              unconvertedObjectCount = 1;
              errorLog   = new FileWriter(documentDirectory + "unConvertedObjectIds_"+ unconvertedFileCount +".csv", true);
              unconvertedFileCount ++;
              errorLog.write("MASTER OID,TYPE,NAME,REVISION,CLASSIFICATION,VERSION OID,LOCKER\n");
              errorLog.write(command);
              errorLog.flush();
          }
      }

      /**
       * This method is executed if a specific method is not specified.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds no arguments
       * @returns nothing
       * @throws Exception if the operation fails
       */
      public int mxMain(Context context, String[] args) throws Exception
      {
          if(!context.isConnected())
          {
              throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
          }
          int argsLength = args.length;
          error = "";

          String command = "print program eServiceSystemInformation.tcl select property[appVersionIEF] dump |";
          String result  = MqlUtil.mqlCommand(context, mqlCommand,  command);

          boolean iefVersion = false;
          boolean iefStatus = false;

          if( result == null || "null".equals(result) || "".equals(result))
          {
              iefStatus = true;
          }
          else if ( result.length() > 0)
          {
              result = result.substring(result.indexOf(" value ") + 7);

              if(result != null && (result.startsWith("9") ||
                                 result.startsWith("10-0") ||
                                 result.startsWith("10-1") ||
                                 result.startsWith("10-2") ||
                                 result.startsWith("10-3") ||
                                 result.startsWith("10-4")))
              {
                  iefStatus = false;
              } else {
                  iefStatus = true;
              }
          }

          if(!iefStatus)
          {
              // throw new Exception("IEF data migration should completed before application data migration");
              writer.write("=================================================================\n");
              writer.write("IEF should be Upgraded to 10-5 before data migration\n");
              writer.write("Step 2 of Migration :     FAILED \n");
              writer.write("=================================================================\n");

              // if scan is true, writer will be closed by the caller
              if(!scan)
              {
                  writer.close();
              }
              return 0;
          }

          try
          {
              // writer     = new BufferedWriter(new MatrixWriter(context));
              if (args.length < 3 )
              {
                  error = "Wrong number of arguments";
                  throw new IllegalArgumentException();
              }
              documentDirectory = args[0];

              // documentDirectory does not ends with "/" add it
              String fileSeparator = java.io.File.separator;
              if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator))
              {
                documentDirectory = documentDirectory + fileSeparator;
              }

              minRange = Integer.parseInt(args[1]);

              if ("n".equalsIgnoreCase(args[2]))
              {
                maxRange = getTotalFilesInDirectory();
              } else {
                maxRange = Integer.parseInt(args[2]);
              }

              if (minRange > maxRange)
              {
                error = "Invalid range for arguments, minimum is greater than maximum range value";
                throw new IllegalArgumentException();
              }

              if (minRange == 0 || minRange < 1 || maxRange == 0 || maxRange < 1)
              {
                error = "Invalid range for arguments, minimum/maximum range value is 0 or negative";
                throw new IllegalArgumentException();
              }
          }
          catch (IllegalArgumentException iExp)
          {
              writer.write("====================================================================\n");
              writer.write(error + " \n");
              writer.write("Step 2 of Migration :     FAILED \n");
              writer.write("====================================================================\n");
              // if scan is true, writer will be closed by the caller
              if(!scan)
              {
                  writer.close();
              }
              return 0;
          }

          String debugString = "false";
          if( argsLength >= 4 )
          {
              convertNonRevisionedDocument = args[3];
              if ( "debug".equalsIgnoreCase(convertNonRevisionedDocument) )
              {
                  debug = true;
                  convertNonRevisionedDocument = "false";
              }
          }
          if( argsLength >= 5 )
          {
              debugString = args[4];
              if ( "debug".equalsIgnoreCase(debugString) )
              {
                  debug = true;
              }
          }
          try
          {
              errorLog   = new FileWriter(documentDirectory + "unConvertedObjectIds_"+ unconvertedFileCount +".csv", true);
              unconvertedFileCount ++;
              errorLog.write("MASTER OID,TYPE,NAME,REVISION,CLASSIFICATION,VERSION OID,LOCKER\n");
              errorLog.flush();
              convertedOidsLog    = new FileWriter(documentDirectory + "convertedIds.txt", true);
              warningLog = new FileWriter(documentDirectory + "migration.log", true);
          }
          catch(FileNotFoundException fExp)
          {
              // check if user has access to the directory
              // check if directory exists
              writer.write("=================================================================\n");
              writer.write("Directory does not exist or does not have access to the directory\n");
              writer.write("Step 2 of Migration :     FAILED \n");
              writer.write("=================================================================\n");
              // if scan is true, writer will be closed by the caller
              if(!scan)
              {
                  writer.close();
              }
              return 0;
          }

          int i = 0;
          try
          {
              ContextUtil.pushContext(context);
              String cmd = "trigger off";
              MqlUtil.mqlCommand(context, mqlCommand,  cmd);
              mqlLogRequiredInformationWriter("=======================================================\n\n");
              mqlLogRequiredInformationWriter("                Migrating Document Objects...\n");
              mqlLogRequiredInformationWriter("                File (" + minRange + ") to (" + maxRange + ")\n");
              mqlLogRequiredInformationWriter("                Reading files from: " + documentDirectory + "\n");
              mqlLogRequiredInformationWriter("                Objects which cannot be migrated will be written to:  unConvertedObjectIds.csv\n");
              mqlLogRequiredInformationWriter("                Logging of this Migration will be written to: migration.log\n\n");
              mqlLogRequiredInformationWriter("=======================================================\n\n");
              iefMigration = new emxIntegrationMigration_mxJPO(context, null);
              migrationStartTime = System.currentTimeMillis();
              statusBuffer.append("File Name, Status, Object Failed (OR) Time Taken in MilliSec\n");
              for( i = minRange;i <= maxRange; i++)
              {
                  try
                  {
                      ContextUtil.startTransaction(context,true);
                      mqlLogWriter("Reading file: " + i + "\n");
                      StringList objectList = new StringList();
                      migratedOids = new StringBuffer(20000);
                      try
                      {
                          objectList = readFiles(i);
                      }
                      catch(FileNotFoundException fnfExp)
                      {
                          // throw exception if file does not exists
                          throw fnfExp;
                      }
                      sourcingDocumentsList = new StringList();
                      startTime = System.currentTimeMillis();
                      identifyModel(context,objectList);
                      if(sourcingDocumentsList.size() > 0)
                      {
                           migrateSourcingDocuments(context, sourcingDocumentsList);
                      }
                      ContextUtil.commitTransaction(context);
                      logMigratedOids();
                      mqlLogRequiredInformationWriter("<<< Time taken for migration of objects & write ConvertedOid.txt for file in milliseconds :" + documentDirectory + "documentobjectids_" + i + ".txt"+ ":=" +(System.currentTimeMillis() - startTime) + ">>>\n");

                      // write after completion of each file
                      mqlLogRequiredInformationWriter("=================================================================\n");
                      mqlLogRequiredInformationWriter("Migration of Documents in file documentobjectids_" + i + ".txt COMPLETE \n");
                      statusBuffer.append("documentobjectids_");
                      statusBuffer.append(i);
                      statusBuffer.append(".txt,COMPLETE,");
                      statusBuffer.append((System.currentTimeMillis() - startTime));
                      statusBuffer.append("\n");
                      mqlLogRequiredInformationWriter("=================================================================\n");
                  }
                  catch(FileNotFoundException fnExp)
                  {
                      // log the error and proceed with migration for remaining files
                      mqlLogRequiredInformationWriter("=================================================================\n");
                      mqlLogRequiredInformationWriter("File documentobjectids_" + i + ".txt does not exist \n");
                      mqlLogRequiredInformationWriter("=================================================================\n");
                      ContextUtil.abortTransaction(context);
                  }
                  catch (Exception exp)
                  {
                      // abort if identifyModel or migration fail for a specific file
                      // continue the migration process for the remaining files
                      mqlLogRequiredInformationWriter("=======================================================\n");
                      mqlLogRequiredInformationWriter("Migration of Documents in file documentobjectids_" + i + ".txt FAILED \n");
                      mqlLogRequiredInformationWriter("=="+ exp.getMessage() +"==\n");
                      mqlLogRequiredInformationWriter("=======================================================\n");
                      statusBuffer.append("documentobjectids_");
                      statusBuffer.append(i);
                      statusBuffer.append(".txt,FAILED,");
                      statusBuffer.append(failureId);
                      statusBuffer.append("\n");
                      exp.printStackTrace();
                      ContextUtil.abortTransaction(context);

                  }
              }

              mqlLogRequiredInformationWriter("=======================================================\n");
              mqlLogRequiredInformationWriter("                Migrating Document Objects  COMPLETE\n");
              mqlLogRequiredInformationWriter("                Time: " + (System.currentTimeMillis() - migrationStartTime) + " ms\n");
              mqlLogRequiredInformationWriter(" \n");
              mqlLogRequiredInformationWriter("Step 2 of Migration :     SUCCESS \n");
              mqlLogRequiredInformationWriter(" \n");
              mqlLogRequiredInformationWriter("                Objects which cannot be migrated will be written to:  unConvertedObjectIds.csv\n");
              mqlLogRequiredInformationWriter("                Logging of this Migration will be written to: migration.log\n\n");
              mqlLogRequiredInformationWriter("=======================================================\n");
          }
          catch (FileNotFoundException fEx)
          {
              ContextUtil.abortTransaction(context);
          }
          catch (Exception ex)
          {
              // abort if identifyModel fail
              mqlLogRequiredInformationWriter("=======================================================\n");
              mqlLogRequiredInformationWriter("Migration of Documents in file documentobjectids_" + i + ".txt failed \n");
              mqlLogRequiredInformationWriter("Step 2 of Migration     : FAILED \n");
              mqlLogRequiredInformationWriter("=======================================================\n");
              ex.printStackTrace();
              ContextUtil.abortTransaction(context);
          }
          finally
          {
              mqlLogRequiredInformationWriter("<<< Total time taken for migration in milliseconds :=" + (System.currentTimeMillis() - migrationStartTime) + ">>>\n");
              String cmd = "trigger on";
              MqlUtil.mqlCommand(context, mqlCommand,  cmd);

              cmd = "modify program eServiceSystemInformation.tcl property Conversion10-5CommonDocumentMigration value Executed";
              MqlUtil.mqlCommand(context, mqlCommand,  cmd);
              statusLog   = new FileWriter(documentDirectory + "fileStatus.csv", true);
              statusLog.write(statusBuffer.toString());
              statusLog.flush();
              statusLog.close();

              ContextUtil.popContext(context);
              // if scan is true, writer will be closed by the caller
              if(!scan)
              {
                  writer.close();
              }
              errorLog.close();
              warningLog.close();
              convertedOidsLog.close();
          }

          // always return 0, even this gives an impression as success
          // this way, matrixWriter writes to console
          // else writer.write statements do not show up in Application console
          // but it works in mql console
          return 0;
      }

      /**
       * This method goes thru all the Objects in files
       * but does NOT migrate any, but finds all the unConvertable Objects to the file
       * written to provide a way to see all the unConvertable Objects before
       * running the migration
       *
       * @param context the eMatrix <code>Context</code> object
       * @param writer - MatrixWriter object sent from calling JPO.
       * @param args - Context, directory name where files exist, Minimum range, Maximum range
       * @throws Exception if the operation fails
       */
      public void scanObjects(Context context, Map map) throws Exception
      {
          writer = (BufferedWriter)map.get("writer");
          String[] args = (String[])map.get("args");

          scan = true;
          mxMain(context, args);

          return;
      }

      /**
       * This method returns the total number of files in the directory.
       *
       * @returns int of total files present in the directory
       * @throws Exception if the operation fails
       */
      public int getTotalFilesInDirectory() throws Exception
      {
          int totalFiles = 0;
          try
          {
              String[] fileNames = null;
              java.io.File file = new java.io.File(documentDirectory);
              if(file.isDirectory())
              {
                  fileNames = file.list();
              } else {
                  throw new IllegalArgumentException();
              }
              for (int i=0; i<fileNames.length; i++)
              {
                  if(fileNames[i].startsWith("documentobjectids_"))
                  {
                      totalFiles = totalFiles + 1;
                  }
              }
          }
          catch(Exception fExp)
          {
              // check if user has access to the directory
              // check if directory exists
              error = "Directory does not exist or does not have access to the directory";
              throw fExp;
          }

          return totalFiles;
      }

      /**
       * This method reads the contents of the file and puts in Arraylist.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args i holds the suffux of filename to identify the file.
       * @returns ArrayList of objectIds present in the file
       * @throws Exception if the operation fails
       */
      public StringList readFiles(int i) throws Exception
      {
          String objectId = "";
          StringList objectIds = new StringList();
          try
          {
              java.io.File file = new java.io.File(documentDirectory + "documentobjectids_" + i + ".txt");
              BufferedReader fileReader = new BufferedReader(new FileReader(file));
              while((objectId = fileReader.readLine()) != null)
              {
                objectIds.add(objectId);
              }
          }
          catch(FileNotFoundException fExp)
          {
              throw fExp;
          }
          return objectIds;
      }


      /**
       * This method reads the contents of the file and puts in Arraylist.
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args i holds the suffux of filename to identify the file.
       * @returns ArrayList of objectIds present in the file
       * @throws Exception if the operation fails
       */
      public StringList readFiles(String fileName) throws Exception
      {
          String objectId = "";
          StringList objectIds = new StringList();
          try
          {
              java.io.File file = new java.io.File(documentDirectory + fileName);
              BufferedReader fileReader = new BufferedReader(new FileReader(file));
              while((objectId = fileReader.readLine()) != null)
              {
                objectIds.add(objectId);
              }
          }
          catch(FileNotFoundException fExp)
          {
              throw fExp;
          }
          return objectIds;
      }


      /**
       * This method identifies the model and invokes the relevant module for migration
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args objectIdList has the list of Object Ids
       * @returns nothing
       * @throws Exception if the operation fails
       */
      public void identifyModel(Context context,StringList objectIdList) throws Exception
      {
          String[] oidsArray = new String[objectIdList.size()];
          oidsArray = (String[])objectIdList.toArray(oidsArray);
          StringList selects = iefMigration.getIEFSelectables(context);
          mxMainObjectSelects.addAll(selects);
          MapList mapList = DomainObject.getInfo(context, oidsArray, mxMainObjectSelects);
          Iterator itr = mapList.iterator();

          StringList sourcingObjectSelects = new StringList(20);


          // These are the variable used in following while look
          // Declared here to avoid declaring multiple times in loops.
          // not sure any performance we get or not??
          Map map = new HashMap();
          boolean isTeam_Sourcing_Doc = false;
          boolean isPMC_Doc = false;
          boolean isDoc_Spec_Product_Doc = false;
          boolean isEC_Doc = false;
          boolean isIEF_Doc = false;
          boolean isJT = false;

          int model = 0;
          String errorMessage = "";
          String modified = "";
          String modCmd = "";
          while( itr.hasNext())
          {
              isConverted = false;
              isTeamSourcingLowerRevisionDocument = false;
              map = (Map) itr.next();
              failureId = (String)map.get(SELECT_ID);
              StringList formats = (StringList)map.get(SELECT_FILE_FORMAT);
              if ( formats != null && formats.contains(FORMAT_JT) )
              {
                  isJT = true;
              } else {
                  // isConverted is class level variable, set to false in each loop i.e. for each object
                  // it can be set to true in any validate models
                  // if set to true in any model, then skip the remaining validate steps
                  if(!isConverted)
                  {
                      isTeam_Sourcing_Doc = validateTeamSourcingModel(context, map);
                  }
                  if(!isConverted)
                  {
                      isDoc_Spec_Product_Doc = validateDocSpecProductModel(context, map);
                  }
                  if(!isConverted)
                  {
                      isEC_Doc = validateECModel(context, map);
                  }
                  if(!isConverted)
                  {
                      isPMC_Doc = validatePMCModel(context, map);
                  }
                  if(!isConverted)
                  {
                      isIEF_Doc = iefMigration.validateIEFModel(context, map);
                  }
              }
              model = 0;
              if(isTeam_Sourcing_Doc )
              {
                  model = TEAM_SOURCING;
                  if(isDoc_Spec_Product_Doc )
                  {
                      errorMessage = "This Object is part of more than one model. The models are 'Team Sourcing' and 'Spec Product Document' model";
                      model = 0;
                  }
                  else if ( isEC_Doc )
                  {
                      // If Parent Type is Part and Document is connected with reference document then
                      // it could be from Sourcing. If that Document has more than one document then
                      // it need to be converted as EC model.
                      errorMessage = "This Object is part of more than one model. The models are 'Team Sourcing' and 'EC' model";
                      model = 0;
                  }
                  else if (isPMC_Doc)
                  {
                      errorMessage = "This Object is part of more than one model. The models are 'Team Sourcing' and 'PMC' model";
                      model = 0;
                  }
              }
              else if( isPMC_Doc )
              {
                  model = PMC;
                  if(isDoc_Spec_Product_Doc )
                  {
                      errorMessage = "This Object is part of more than one model. The models are 'PMC' and 'Spec Product Document' model";
                      model = 0;
                  } else if ( isEC_Doc ) {
                      errorMessage = "This Object is part of more than one model. The models are 'PMC' and 'EC' model";
                      model = 0;
                  } else if (isTeam_Sourcing_Doc) {
                      errorMessage = "This Object is part of more than one model. The models are 'PMC' and 'Team Sourcing' model";
                      model = 0;
                  }
              }
              else if (isDoc_Spec_Product_Doc )
              {
                  model = DOCUMENT_PRODUCT_SPEC;
                  if(isEC_Doc )
                  {
                      errorMessage = "This Object is part of more than one model. The models are 'Spec Product Document' and 'EC' model";
                      model = 0;
                  } else if ( isPMC_Doc ) {
                      errorMessage = "This Object is part of more than one model. The models are 'Spec Product Document' and 'PMC' model";
                      model = 0;
                  } else if (isTeam_Sourcing_Doc) {
                      errorMessage = "This Object is part of more than one model. The models are 'Spec Product Document' and 'Team Sourcing' model";
                      model = 0;
                  }
              }
              else if (isIEF_Doc)
              {
                  model = IEF;
              }
              else if (isEC_Doc)
              {
                  model = EC;
                  if(isDoc_Spec_Product_Doc )
                  {
                      errorMessage = "This Object is part of more than one model. The models are 'EC' and 'Spec Product Document' model";
                      model = 0;
                  } else if ( isPMC_Doc ) {
                      errorMessage = "This Object is part of more than one model. The models are 'EC' and 'PMC' model";
                      model = 0;
                  } else if (isTeam_Sourcing_Doc) {
                      errorMessage = "This Object is part of more than one model. The models are 'EC' and 'Team Sourcing' model";
                      model = 0;
                  }
              }
              else
              {
                  model = UNKNOWN_MODEL;
                  String lastId = (String)map.get("last.id");
                  String id = (String)map.get(SELECT_ID);
                  String prevId = (String)map.get("previous.id");

                  if ( !isJT && "true".equalsIgnoreCase(convertNonRevisionedDocument) && ("".equals(prevId) || "null".equals(prevId) || prevId == null) && id.equals(lastId) )
                  {
                      model = 4;
                  }
              }

              if(!isConverted)
              {
                  mqlLogWriter("\n");
                  mqlLogWriter("The Current Object "+ map.get(SELECT_ID) +" is in Model =" + model + "\n");
                  mqlLogWriter("Object TNR :" + map.get(SELECT_TYPE) + " " +map.get(SELECT_NAME) + " " + map.get(SELECT_REVISION) + "\n");
                  mqlLogWriter("Ref Doc From Type List      :" + (StringList)map.get(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_TYPE) + "\n");
                  mqlLogWriter("Val Doc From Type List      :" + (StringList)map.get(SELECT_RELATIONSHIP_VAULTED_OBJECTS_FROM_TYPE) + "\n");
                  mqlLogWriter("Meeting Doc From Type List  :" + (StringList)map.get(SELECT_RELATIONSHIP_MEETING_ATTACHMENTS_FROM_TYPE) + "\n");
                  mqlLogWriter("Message Doc From Type List  :" + (StringList)map.get(SELECT_RELATIONSHIP_MESSAGE_ATTACHMENTS_FROM_TYPE) + "\n");
                  mqlLogWriter("Part Family From Type List  :" + (StringList)map.get(SELECT_RELATIONSHIP_PART_FAMILY_REFERENCE_DOCUMENT_FROM_TYPE) + "\n");
                  mqlLogWriter("Val ObjRev2 From Type List  :" + (StringList)map.get(SELECT_RELATIONSHIP_VAULTED_OBJECTS_REV2_FROM_TYPE) + "\n");
                  mqlLogWriter("Task Delive From Type List  :" + (StringList)map.get(SELECT_RELATIONSHIP_TASK_DELIVERABLE_FROM_TYPE) + "\n");
                  mqlLogWriter("Format List  :" + (StringList)map.get(SELECT_FILE_FORMAT) + "\n");
                  mqlLogWriter("File List    :" + (StringList)map.get(SELECT_FILE_NAME) + "\n");
                  mqlLogWriter("\n");

                  switch (model)
                  {
                      case 0:
                          // if the object falls into more than one model
                          multipleModelObjectIds(errorMessage, map);
                          break;
                      case 1:
                          migrateTeamSourcingModel(context, map);
                          break;
                      case 2:
                          migratePMCModel(context, map);
                          break;
                      case 3:
                          migrateDocumentProductSpecModel(context, map);
                          break;
                      case 4:
                          migrateEcModel(context, map);
                          break;
                      case 5:
                          iefMigration.migrateIEFModel(context, map);
                          break;
                      default:
                          // if the object does not fall in any model
                          // and if it is a teamsourcing lower revision document, then do not write to log
                          // since they will be migrated as part of highest revision object
                          if(!isTeamSourcingLowerRevisionDocument)
                          {
                              noModelObjectIds("Not in any known models ", map);
                              break;
                          }
                  }
                  modified = (String)map.get("modified");
                  modCmd = "mod bus " + failureId + " modified '" + modified + "'";
                  MqlUtil.mqlCommand(context, mqlCommand,  modCmd);
              } else {
                  mqlLogWriter("\n");
                  mqlLogWriter("The Current Object "+ map.get(SELECT_ID) +" is already Converted \n");
                  mqlLogWriter("Object TNR :" + map.get(SELECT_TYPE) + " " +map.get(SELECT_NAME) + " " + map.get(SELECT_REVISION) + "\n");
                  mqlLogWriter("\n");
              }

          }
      }

      private void multipleModelObjectIds (String message, Map map) throws Exception
      {
          mqlLogWriter("!!! WARNING !!! Object TNRV = " + map.get(SELECT_TYPE) +
                             " " + map.get(SELECT_NAME) +
                             " " + map.get(SELECT_REVISION) +
                             " " + map.get(SELECT_VAULT) + "\n" +
                             " Id:" + map.get(SELECT_ID) + "\n");
          mqlLogWriter("Above Object could not been migrated to Common Document Model Because of Following Problem \n" + message + "\n");

          writeUnconvertedOID((String)map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + "," + "Multiple Model\n");

      }

      private void noModelObjectIds (String message, Map map) throws Exception
      {
          mqlLogWriter("!!! WARNING !!! Object TNRV = " + map.get(SELECT_TYPE) +
                             " " + map.get(SELECT_NAME) +
                             " " + map.get(SELECT_REVISION) +
                             " " + map.get(SELECT_VAULT) + "\n" +
                             " Id:" + map.get(SELECT_ID) + "\n");
          mqlLogWriter("Above Object could not been migrated to Common Document Model Because of Following Problem \n" + message + "\n");
          String lastId = (String)map.get("last.id");
          String prevId = (String)map.get("previous.id");
          String id = (String)map.get(SELECT_ID);
          StringList formats = (StringList)map.get(SELECT_FILE_FORMAT);
          if ( id.equals(lastId) && ("".equals(prevId) || "null".equals(prevId) || prevId == null))
          {
              writeUnconvertedOID((String)map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",No Model with out revisions\n");
          } else if ( formats != null && formats.contains(FORMAT_JT) ) {
              writeUnconvertedOID((String)map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Found JT file(s) \n");
          } else {
              writeUnconvertedOID((String)map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",No Model\n");
          }
      }

      private void logUnableToLockIds (String message) throws Exception
      {
          writeUnconvertedOID(message + "\n");
      }

      private void loadMigratedOids (String objectId) throws Exception
      {
          migratedOids.append(objectId + "\n");
      }

      private void logMigratedOids() throws Exception
      {
          convertedOidsLog.write(migratedOids.toString());
          convertedOidsLog.flush();
      }

      private void logWarning (String message) throws Exception
      {
          warningLog.write( message );
          warningLog.flush();
      }
      private void logWarning (String message, Map map) throws Exception
      {
          if ( debug)
          {
              writer.write("!!! WARNING !!! Object TNRV = " + map.get(SELECT_TYPE) +
                                 " " + map.get(SELECT_NAME) +
                                 " " + map.get(SELECT_REVISION) +
                                 " " + map.get(SELECT_VAULT) + "\n" +
                                 " Id:" + map.get(SELECT_ID) + "\n");
              writer.write("Above Object has Following warning \n" + message + "\n");
              writer.flush();

              warningLog.write("!!! WARNING !!! Object TNRV = " + map.get(SELECT_TYPE) +
                                 " " + map.get(SELECT_NAME) +
                                 " " + map.get(SELECT_REVISION) +
                                 " " + map.get(SELECT_VAULT) + "\n" +
                                 " Id:" + map.get(SELECT_ID) + "\n");
              warningLog.write("Above Object has Following warning \n" + message + "\n");
              warningLog.write("\n");
              warningLog.flush();
          }
      }

      private boolean validateTeamSourcingModel(Context context, Map map) throws Exception
      {
          try
          {
              boolean isTeamSourcing            = false;
              boolean isTeam                    = false;
              boolean isOther                   = false;
              boolean isSourcing                = false;

              String type = (String) map.get(SELECT_TYPE);
              String lastId = (String) map.get("last.id");
              String objectId = (String) map.get(SELECT_ID);
              type = getParentType(context, type);
              if ( type.equals(TYPE_DOCUMENT) && objectId.equals(lastId) )
              {
                  // Looking for Reference Document Relationship for Sourcing Documents.
                  StringList parentList = (StringList) map.get(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_TYPE);
                  if ( parentList != null )
                  {
                      StringList topLevelParentList = new StringList(parentList.size());
                      Iterator itr = parentList.iterator();
                      while(itr.hasNext())
                      {
                          String parentType = (String) itr.next();
                          parentType = getParentType(context, parentType);
                          topLevelParentList.add(parentType);
                      }

                      if( topLevelParentList.contains(TYPE_PACKAGE) || topLevelParentList.contains(TYPE_RFQ) ||
                          topLevelParentList.contains(TYPE_RTS_TEMPLATE) || topLevelParentList.contains(TYPE_RTS_QUOTATION) ||
                          topLevelParentList.contains(TYPE_LINE_ITEM) || topLevelParentList.contains(TYPE_SUPPLIER_LINE_ITEM) ||
                          topLevelParentList.contains(TYPE_PART) || topLevelParentList.contains(TYPE_ECR) )
                      {
                          isSourcing = true;
                      }

                      if( topLevelParentList.contains(TYPE_PRODUCTS) || topLevelParentList.contains(TYPE_BUILDS) ||
                          topLevelParentList.contains(TYPE_FEATURES) || topLevelParentList.contains(TYPE_INCIDENT) ||
                          topLevelParentList.contains(TYPE_REQUIREMENT) || topLevelParentList.contains(TYPE_TEST_CASE) ||
                          topLevelParentList.contains(TYPE_ISSUE) || topLevelParentList.contains(TYPE_USE_CASE) ||
                          topLevelParentList.contains(TYPE_TECHNICAL_SPECIFICATION) || topLevelParentList.contains(TYPE_FINANCIALS) ||
                          topLevelParentList.contains(TYPE_ASSESSMENT) || topLevelParentList.contains(TYPE_RISK) ||
                          topLevelParentList.contains(TYPE_BUSINESS_GOALS) || topLevelParentList.contains(TYPE_QUALITY) )
                      {
                          isOther = true;
                      }
                  }
                  StringList vaultedParents = (StringList)  map.get(SELECT_RELATIONSHIP_VAULTED_OBJECTS_FROM_TYPE);
                  if ( vaultedParents != null )
                  {
                      if( vaultedParents.contains(TYPE_WORKSPACE_VAULT) )
                      {
                          isTeam = true;
                      }
                  }
                  StringList meetings = (StringList)  map.get(SELECT_RELATIONSHIP_MEETING_ATTACHMENTS_FROM_TYPE);
                  if ( meetings != null )
                  {
                      if( meetings.contains(TYPE_MEETING) )
                      {
                          isTeam = true;
                      }
                  }
                  StringList discussions = (StringList)  map.get(SELECT_RELATIONSHIP_MESSAGE_ATTACHMENTS_FROM_TYPE);
                  if ( discussions != null )
                  {
                      if( discussions.contains(TYPE_MESSAGE) )
                      {
                          isTeam = true;
                      }
                  }
                  String latestVersionToId   = (String)map.get(SELECT_TO_LATEST_VERSION_RELATIONSHIP_ID);
                  String latestVersionFromId = (String)map.get(SELECT_FROM_LATEST_VERSION_RELATIONSHIP_ID);
                  if ( latestVersionToId != null || latestVersionFromId != null )
                  {
                       isConverted = true;
                       logWarning("Already Converted ", map);
                  }

                  if ( (isTeam || isSourcing) && (!isOther) && (!isConverted))
                  {
                      StringList formats = (StringList) map.get(SELECT_FILE_FORMAT);
                      StringList files = (StringList) map.get(SELECT_FILE_NAME);
                      if ( (files != null && files.size() == 1) )
                      {
                          if ( !formats.contains(FORMAT_JT) )
                          {
                              isTeamSourcing = true;

                // for Sourcing documents do not migrate right away, create a StringList
                // and convert them in one go for performance reasons
                // if we do in the same flow, select on each objects would be increased
                if (isSourcing)
                {
                    sourcingDocumentsList.add(objectId);
                  }
                          }
                      }
                  }
              }
              else if( type.equals(TYPE_DOCUMENT) && !objectId.equals(lastId) )
              {
               isTeamSourcingLowerRevisionDocument = true;
            }
              return isTeamSourcing;
          }
          catch(Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }
      }

      private boolean validateDocSpecProductModel(Context context, Map map) throws Exception
      {
          try
          {
              boolean isDocSpecProduct = false;
              boolean isDoc = false;
              boolean isSpec = false;
              boolean isProduct = false;
              boolean isOther = false;

              String type = (String) map.get(SELECT_TYPE);
              type = getParentType(context, type);

              // Document Central Documents are Generic Document or Document Sheets and Their Sub Types
              if ( type.equals(TYPE_GENERIC_DOCUMENT) || type.equals(TYPE_DOCUMENT_SHEET) )
              {
                  isDoc = true;
              }
              else  if ( type.equals(TYPE_SPECIFICATION) )
              {
                  isProduct = true;
              }

              // Spec Documents
              // Looking for Reference Document Relationship for Sourcing Documents.
              StringList parentList = (StringList) map.get(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_TYPE);
              if ( parentList != null )
              {
                  StringList topLevelParentList = new StringList(parentList.size());
                  Iterator itr = parentList.iterator();
                  while(itr.hasNext())
                  {
                      String parentType = (String) itr.next();
                      parentType = getParentType(context, parentType);
                      topLevelParentList.add(parentType);
                  }
                  if( topLevelParentList.contains(TYPE_TECHNICAL_SPECIFICATION) )
                  {
                      isSpec = true;
                  }

                  if( topLevelParentList.contains(TYPE_PRODUCTS) || topLevelParentList.contains(TYPE_BUILDS) ||
                      topLevelParentList.contains(TYPE_FEATURES) || topLevelParentList.contains(TYPE_INCIDENT) ||
                      topLevelParentList.contains(TYPE_REQUIREMENT) || topLevelParentList.contains(TYPE_TEST_CASE) ||
                      topLevelParentList.contains(TYPE_ISSUE) || topLevelParentList.contains(TYPE_USE_CASE) )
                  {
                      isProduct = true;
                  }

                  if( topLevelParentList.contains(TYPE_PACKAGE) || topLevelParentList.contains(TYPE_RFQ) ||
                      topLevelParentList.contains(TYPE_RTS_TEMPLATE) || topLevelParentList.contains(TYPE_RTS_QUOTATION) ||
                      topLevelParentList.contains(TYPE_LINE_ITEM) || topLevelParentList.contains(TYPE_SUPPLIER_LINE_ITEM) ||
                      topLevelParentList.contains(TYPE_PART) || topLevelParentList.contains(TYPE_ECR) ||
                      topLevelParentList.contains(TYPE_FINANCIALS) || topLevelParentList.contains(TYPE_ASSESSMENT) ||
                      topLevelParentList.contains(TYPE_RISK) || topLevelParentList.contains(TYPE_BUSINESS_GOALS) ||
                      topLevelParentList.contains(TYPE_QUALITY) )
                  {
                      isOther = true;
                  }

              }

              String latestVersionToId   = (String)map.get(SELECT_TO_LATEST_VERSION_RELATIONSHIP_ID);
              String latestVersionFromId = (String)map.get(SELECT_FROM_LATEST_VERSION_RELATIONSHIP_ID);
              if ( latestVersionToId != null || latestVersionFromId != null )
              {
                   isConverted = true;
                   logWarning("Already Converted ", map);
              }
              if ( (isDoc || isSpec || isProduct) && (!isOther) && (!isConverted))
              {
                  isDocSpecProduct = true;
              }
              return isDocSpecProduct;
          }
          catch(Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }
      }

      private boolean validateECModel(Context context, Map map) throws Exception
      {
          try
          {
              boolean isEC = false;
              boolean isOther = false;
              String type = (String) map.get(SELECT_TYPE);
              type = getParentType(context, type);
              StringList files = (StringList) map.get(SELECT_FILE_NAME);

              if ( type.equals(TYPE_CAD_MODEL) || type.equals(TYPE_CAD_DRAWING) ||
                   type.equals(TYPE_DRAWING_PRINT) || type.equals(TYPE_SKETCH) ||
                   type.equals(TYPE_MARKUP) )
              {
                  isEC = true;
              }
              else if ( type.equals(TYPE_DOCUMENT) )
              {
                  StringList parentPartList = (StringList) map.get(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_TYPE);
                  if ( parentPartList != null )
                  {
                      StringList topLevelParentList = new StringList(parentPartList.size());
                      Iterator itr = parentPartList.iterator();
                      while(itr.hasNext())
                      {
                          String parentType = (String) itr.next();
                          parentType = getParentType(context, parentType);
                          topLevelParentList.add(parentType);
                      }
                      if( topLevelParentList.contains(TYPE_PART) && (files != null && files.size() > 1))
                      {
                          isEC = true;
                      }


                      if( topLevelParentList.contains(TYPE_PACKAGE) || topLevelParentList.contains(TYPE_RFQ) ||
                          topLevelParentList.contains(TYPE_RTS_TEMPLATE) || topLevelParentList.contains(TYPE_RTS_QUOTATION) ||
                          topLevelParentList.contains(TYPE_LINE_ITEM) || topLevelParentList.contains(TYPE_SUPPLIER_LINE_ITEM) ||
                          (topLevelParentList.contains(TYPE_PART) && (files == null || files.size() <= 1) ) || topLevelParentList.contains(TYPE_ECR) ||
                          topLevelParentList.contains(TYPE_PRODUCTS) || topLevelParentList.contains(TYPE_BUILDS) ||
                          topLevelParentList.contains(TYPE_FEATURES) || topLevelParentList.contains(TYPE_INCIDENT) ||
                          topLevelParentList.contains(TYPE_REQUIREMENT) || topLevelParentList.contains(TYPE_TEST_CASE) ||
                          topLevelParentList.contains(TYPE_ISSUE) || topLevelParentList.contains(TYPE_USE_CASE) ||
                          topLevelParentList.contains(TYPE_TECHNICAL_SPECIFICATION) || topLevelParentList.contains(TYPE_FINANCIALS) ||
                          topLevelParentList.contains(TYPE_ASSESSMENT) || topLevelParentList.contains(TYPE_RISK) ||
                          topLevelParentList.contains(TYPE_BUSINESS_GOALS) || topLevelParentList.contains(TYPE_QUALITY) )
                      {
                          isOther = true;
                      }



                  }
                  StringList parentPartFamilyList = (StringList) map.get(SELECT_RELATIONSHIP_PART_FAMILY_REFERENCE_DOCUMENT_FROM_TYPE);
                  if ( parentPartFamilyList != null )
                  {
                      StringList topLevelParentList = new StringList(parentPartFamilyList.size());
                      Iterator itr = parentPartFamilyList.iterator();
                      while(itr.hasNext())
                      {
                          String parentType = (String) itr.next();
                          parentType = getParentType(context, parentType);
                          topLevelParentList.add(parentType);
                      }
                      if( topLevelParentList.contains(TYPE_PART_FAMILY) )
                      {
                          isEC = true;
                      }
                  }

              }

              String latestVersionToId   = (String)map.get(SELECT_TO_LATEST_VERSION_RELATIONSHIP_ID);
              String latestVersionFromId = (String)map.get(SELECT_FROM_LATEST_VERSION_RELATIONSHIP_ID);
              if ( latestVersionToId != null || latestVersionFromId != null )
              {
                   isConverted = true;
                   logWarning("Already Converted ", map);
              }
              return isEC && (!isOther) && (!isConverted);
          }
          catch(Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }
      }


      private boolean validatePMCModel(Context context, Map map) throws Exception
      {
          try
          {
              boolean isPMC = false;
              boolean isOther = false;
              String type = (String) map.get(SELECT_TYPE);
              type = getParentType(context, type);
              if ( type.equals(TYPE_DOCUMENT) )
              {
                  // Looking for Reference Document Relationship for Sourcing Documents.
                  StringList parentList = (StringList) map.get(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_TYPE);

                  if ( parentList != null )
                  {
                      StringList topLevelParentList = new StringList(parentList.size());
                      Iterator itr = parentList.iterator();
                      while(itr.hasNext())
                      {
                          String parentType = (String) itr.next();
                          parentType = getParentType(context, parentType);
                          topLevelParentList.add(parentType);
                      }
                      if( topLevelParentList.contains(TYPE_FINANCIALS) || topLevelParentList.contains(TYPE_ASSESSMENT) ||
                          topLevelParentList.contains(TYPE_RISK) || topLevelParentList.contains(TYPE_BUSINESS_GOALS) ||
                          topLevelParentList.contains(TYPE_QUALITY) )
                      {
                          isPMC = true;
                      }
                      if( topLevelParentList.contains(TYPE_PACKAGE) || topLevelParentList.contains(TYPE_RFQ) ||
                          topLevelParentList.contains(TYPE_RTS_TEMPLATE) || topLevelParentList.contains(TYPE_RTS_QUOTATION) ||
                          topLevelParentList.contains(TYPE_LINE_ITEM) || topLevelParentList.contains(TYPE_SUPPLIER_LINE_ITEM) ||
                          topLevelParentList.contains(TYPE_PART) || topLevelParentList.contains(TYPE_ECR) ||
                          topLevelParentList.contains(TYPE_PRODUCTS) || topLevelParentList.contains(TYPE_BUILDS) ||
                          topLevelParentList.contains(TYPE_FEATURES) || topLevelParentList.contains(TYPE_INCIDENT) ||
                          topLevelParentList.contains(TYPE_REQUIREMENT) || topLevelParentList.contains(TYPE_TEST_CASE) ||
                          topLevelParentList.contains(TYPE_ISSUE) || topLevelParentList.contains(TYPE_USE_CASE) ||
                          topLevelParentList.contains(TYPE_TECHNICAL_SPECIFICATION) )
                      {
                          isOther = true;
                   }
                  }

                  StringList vaultedParents = (StringList)  map.get(SELECT_RELATIONSHIP_VAULTED_OBJECTS_REV2_FROM_TYPE);
                  if ( vaultedParents != null )
                  {
                      if( vaultedParents.contains(TYPE_WORKSPACE_VAULT) )
                      {
                          isPMC = true;
                      }
                  }

                  StringList tasks = (StringList)  map.get(SELECT_RELATIONSHIP_TASK_DELIVERABLE_FROM_TYPE);
                  if ( tasks != null )
                  {
                      if( tasks.contains(TYPE_TASK) )
                      {
                          isPMC = true;
                      }
                  }

                  String latestVersionToId   = (String)map.get(SELECT_TO_LATEST_VERSION_RELATIONSHIP_ID);
                  String latestVersionFromId = (String)map.get(SELECT_FROM_LATEST_VERSION_RELATIONSHIP_ID);
                  if ( latestVersionToId != null || latestVersionFromId != null )
                  {
                       isConverted = true;
                       logWarning("Already Converted ", map);
                  }

                  StringList files = (StringList) map.get(SELECT_FILE_NAME);
                  if ( (files != null && files.size() > 1) )
                  {
                      isPMC = false;
                  }
              }

              return isPMC && (!isOther) && (!isConverted);
          }
          catch(Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }
      }

      /**
       * This method sets Suspend Versioiung attribute for
       * the Sourcing Documents
       *
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      private void  migrateSourcingDocuments(Context context, StringList objectIdList)
                                                          throws Exception
      {
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
          if(scan)
          {
              return;
          }

          try
          {
              StringList objectSelects = new StringList(15);

              String revisionableStates = EnoviaResourceBundle.getProperty(context,"emxQuoteCentral.DocumentRevisionableStatesforRFQ");
              StringList docRevisionableStates = new StringList();
              docRevisionableStates.add(STATE_RTS_TEMPLATE_INACTIVE);
              docRevisionableStates.add(STATE_RTS_QUOTATION_OPEN);

              StringTokenizer strToken = new StringTokenizer(revisionableStates,",");
              if(strToken.hasMoreTokens())
              {
		  docRevisionableStates.add(PropertyUtil.getSchemaProperty(context,"policy", POLICY_REQUEST_TO_SUPPLIER, strToken.nextToken()));
              }

              objectSelects.add(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_TYPE);
              objectSelects.add(SELECT_LINE_ITEM_RFQ_STATE);
              objectSelects.add(SELECT_LINE_ITEM_SPLIT_RFQ_STATE);
              objectSelects.add(SELECT_SUPPLIER_LINE_ITEM_RFQ_QUOTATION_STATE);
              objectSelects.add(SELECT_ECR_RFQ_QUOTATION_STATE);
              objectSelects.add(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_STATE);
              objectSelects.add(SELECT_ID);

              String[] oidsArray = new String[objectIdList.size()];
              oidsArray = (String[])objectIdList.toArray(oidsArray);
              MapList mapList = DomainObject.getInfo(context, oidsArray, objectSelects);
              Iterator itr = mapList.iterator();
              Map map = new HashMap();
              StringList allStates = new StringList();
              while( itr.hasNext())
              {
                  map = (Map) itr.next();
                  String objectId = (String) map.get(SELECT_ID);
                  DomainObject masterObject = DomainObject.newInstance(context, objectId);

                  StringList type = (StringList)map.get(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_TYPE);
                  if ( type != null && (type.contains(TYPE_RFQ) || type.contains(TYPE_RTS_QUOTATION) || type.contains(TYPE_RTS_TEMPLATE)))
                  {
                      StringList referenceDocState = (StringList)map.get(SELECT_RELATIONSHIP_REFERENCE_DOCUMENT_FROM_STATE);
                      if( referenceDocState != null )
                      {
                          allStates.addAll(referenceDocState);
                      }
                  }

                  StringList lineItemState = (StringList)map.get(SELECT_LINE_ITEM_RFQ_STATE);
                  StringList lineItemSplitState = (StringList)map.get(SELECT_LINE_ITEM_SPLIT_RFQ_STATE);
                  StringList supplierLineItemState = (StringList)map.get(SELECT_SUPPLIER_LINE_ITEM_RFQ_QUOTATION_STATE);
                  StringList ecrState = (StringList)map.get(SELECT_ECR_RFQ_QUOTATION_STATE);
                  if(lineItemState != null)
                  {
                      allStates.addAll(lineItemState);
                  }
                  if(lineItemSplitState != null)
                  {
                      allStates.addAll(lineItemSplitState);
                  }
                  if (supplierLineItemState != null )
                  {
                      allStates.addAll(supplierLineItemState);
                  }
                  if (ecrState != null )
                  {
                      allStates.addAll(ecrState);
                  }
                  allStates.removeAll(docRevisionableStates);

                  if ( allStates.size() > 0 )
                  {
                      setSuspendVersioning(context, masterObject);
                  }
              }
          }
          catch(Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }
      }

      /**
       * This method migrates the Sourcing Data to Common Document data model
       *
       * @param context the eMatrix <code>Context</code> object
       * @param map HashMap holds most of the required selects including object id.
       * @returns nothing
       * @throws Exception if the operation fails
       */
      private void migrateTeamSourcingModel(Context context , Map map) throws Exception
      {
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
          if(scan)
          {
              return;
          }
          // get the master Document object Id
          String objectId = (String)map.get(SELECT_ID);
          try
          {
              mqlLogWriter ( "Migrating object id :" + objectId + " in TeamSourcingModel :\n");
              DomainObject masterObj  = (DomainObject) DomainObject.newInstance(context,objectId);
              String masterObjectType = (String) map.get(SELECT_TYPE);
              String masterObjRev     = (String) map.get(SELECT_REVISION);
              String masterObjVault   = (String) map.get(SELECT_VAULT);
              String masterObjPolicy  = (String) map.get(SELECT_POLICY);
              String masterObjOwner   = (String) map.get(SELECT_OWNER);
              String masterObjOriginated   = (String) map.get(SELECT_ORIGINATED);

              String highestVersionObjId      = null;
              String command = null;
              String result  = null;
              setMasterDocumentTitle(context, map, masterObj);

              // clone w/o files the Master to create the highest version
              DomainObject highestVersionObj = new DomainObject( masterObj.clone(context, null, masterObj.getUniqueName(context), masterObjRev, masterObjVault, false));
              highestVersionObjId = highestVersionObj.getId();

              StringList objectSelects = new StringList();

              objectSelects.add(SELECT_ID);

              // Check if the Master has revisions
              MapList revisions = masterObj.getRevisions( context, objectSelects, true);
              String lastButOneVersionId   = null;

              if(revisions.size() > 1)
              {
                  // we get revisions in ascending order, get the revision prior to highest
                  Map lastButOneVersionMap = (Map)revisions.get(revisions.size() - 2);
                  lastButOneVersionId     = (String) lastButOneVersionMap.get(SELECT_ID);

                  // break the Master from core revision chain
                  command = "add bus Document dummy 0 policy Document vault \'"+ VAULT_PRODUCTION +"\'";
                  mqlLogWriter ( "Add dummy bus object = " + command + "\n");
                  result  = MqlUtil.mqlCommand(context, mqlCommand,  command);
                  mqlLogWriter ( "Add dummy bus object = " + result + "\n");

                  // Make the dummy as revision of Master by wiring the two using core revision mechanism
                  command = "revise bus Document dummy 0 bus \'" + objectId;
                  mqlLogWriter ( "break the Master from core revision chain = " + command + "\n");
                  result  = MqlUtil.mqlCommand(context, mqlCommand,  command);
                  mqlLogWriter ( "break the Master from core revision chain = " + result + "\n");

                  // delete the dummy
                  command = "delete bus Document dummy 0";
                  mqlLogWriter ( "delete the dummy command = " + command + "\n");
                  result  = MqlUtil.mqlCommand(context, mqlCommand,  command);
                  mqlLogWriter ( "delete the dummy result = " + result + "\n");

                  // Wire the cloned object into revision chain if Master has Revsions
                  command = "revise bus " + lastButOneVersionId + " " + " bus " + highestVersionObjId;

                  mqlLogWriter ( "Wire the cloned object into revision chain command = " + command + "\n");
                  result = MqlUtil.mqlCommand(context, mqlCommand,  command);
                  mqlLogWriter ( "Wire the cloned object into revision chain Result = " + result + "\n");
              }

              // Connecting the Master and Highest version using
              // Active Version and Latest Version relationship
              masterObj.addToObjects(context,CommonDocument.RELATIONSHIP_ACTIVE_VERSION,highestVersionObj);
              masterObj.addToObjects(context,CommonDocument.RELATIONSHIP_LATEST_VERSION,highestVersionObj);

              // Change the Master object revision to policy initial revision
              // Since revision can not be changed without changing the name, change the name
              String polictInitRev = (masterObj.getPolicy(context)).getFirstInSequence(context);

              masterObj.change(context, masterObjectType, masterObj.getUniqueName(context), polictInitRev, masterObjVault, masterObjPolicy);

              // Updating all revisions with Is Version Object attribute as True
              // modify the policy to Version
              revisions = highestVersionObj.getRevisions( context, objectSelects, true);

              Iterator revisionItr  = revisions.iterator();

              String versionId = null;
              // DomainObject objVer = null;

              int    versionPolictInitRev    = 1;
              String versionPolictInitRevStr = null;
              StringList previousVersionCheckinHistoryList = new StringList();

              // get the highest revision
              // we get revisions in ascending order, get the last in the list
              Map highestVersionMap = (Map)revisions.get(revisions.size() - 1);
              highestVersionObjId = (String) highestVersionMap.get(SELECT_ID);

              // set the highestVersionObj owner same as master object
              // set the highestVersionObj originated date same as master object
              command = "modify bus " + highestVersionObjId + " owner '" + masterObjOwner + "' originated '" + masterObjOriginated + "'";
              mqlLogWriter ( "Modify highest Version Object owner, originated dated command = " + command + "\n");
              result = MqlUtil.mqlCommand(context, mqlCommand,  command);
              mqlLogWriter ( "Modify highest Version Object owner, originated dated result = " + result + "\n");

              while(revisionItr.hasNext())
              {
                  Map versionMap = (Map)revisionItr.next();
                  versionId     = (String) versionMap.get(SELECT_ID);

                  // set the 'Is Version Object' attribute to True
                  // change the policy to 'Version' policy
                  // change the revision number to sync with version number
                  // Note: Version policy has rev sequence as 1,2,3,...
                  // Since revision can not be changed without changing the name, change the name
                  versionPolictInitRevStr = (new Integer(versionPolictInitRev)).toString();

                  command = "modify bus " + versionId + " name " + highestVersionObj.getUniqueName(context) + " revision " + versionPolictInitRev + " policy " + POLICY_VERSION + " '" + ATTRIBUTE_ISVERSIONOBJECT + "' True";
                  mqlLogWriter ( "Modify Version Objects policy command = " + command + "\n");
                  result = MqlUtil.mqlCommand(context, mqlCommand,  command);
                  mqlLogWriter ( "Modify Version Objects policy result = " + result + "\n");

                  // get the Checkin history of all Version objects, and add them as
                  // custom history records to the master object
                  command = "print bus " + versionId + " select history.checkin";
                  result = MqlUtil.mqlCommand(context, mqlCommand,  command);
                  mqlLogWriter("select history.checkin :" +result);

                  if (result.length() > 0)
                  {
                      if (result.indexOf("history.checkin = checkin - ") != -1)
                      {
                          previousVersionCheckinHistoryList.add(result.substring(result.indexOf("history.checkin = checkin - ") + 28));
                      }
                  }

                  versionPolictInitRev = versionPolictInitRev + 1;
              }

              // in the new Common Document model, master object is no longer locked
              // instead corrsponding version object for the file is locked
              // if the master is locked, then lock the corresponding version object with same locker
              lockVersionDocument(context, map, masterObj, highestVersionObj);

              // unlock the master object, if the master is locked,
              // since we locked the version objects
              unlockMasterDocument(context, map, masterObj);

              mqlLogWriter("previousVersionCheckinHistoryList :" +previousVersionCheckinHistoryList);

              // get the Checkin history of all Version objects, and add them as
              // custom history records to the master object
              // this is needed since in new model, master will have all the checkin history of all file versions
              // whereas in old model, each master objet revision has its own file version checkin history
              if(previousVersionCheckinHistoryList.size() > 0)
              {
                  Iterator versionCheckinHistoryItr = previousVersionCheckinHistoryList.iterator();
                  while(versionCheckinHistoryItr.hasNext())
                  {
                      String checkinHistory = (String)versionCheckinHistoryItr.next();
                      command = "modify bus " + objectId + " add history checkin comment '" + checkinHistory + "'";
                      mqlLogWriter ( "Modify History of master object command = " + command + "\n");
                      result = MqlUtil.mqlCommand(context, mqlCommand,  command);
                      mqlLogWriter ( "Modify History of master object result = " + result + "\n");
                  }
              }

              loadMigratedOids (objectId);
          }
          catch (Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }
      }

      /**
       * V10.5 Common Document Management functionality migration
       * Currently, all 'Version Document' objects are wired to master Document
       * with Version relationship. Now, only the latest 'Version Document' will be wired
       * using 'Active Version', 'Latest Version' relationships.
       * Lower versions will be linked to Latest Version Document using core
       * revision mechanism.
       * Also, object type 'Version Document' is changed to master objct type
       * policy is chnaged to 'Version'
       *
       * @param context the eMatrix <code>Context</code> object
       * @param map HashMap holds most of the required selects including object id.
       * @returns nothing
       * @throws Exception if the operation fails
       * @since AEF 10.5
       * @grade 0
       */
      public void migratePMCModel(Context context, Map map)
          throws Exception
      {
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
          if(scan)
          {
              return;
          }

          // get the master Document object Id
          String objectId = (String) map.get(SELECT_ID);
          try
          {
              mqlLogWriter ( "Migrating object id :" + objectId + " in PMCModel :\n");

              // get all the files in the object
              StringList fileList = (StringList) map.get(SELECT_FILE_NAME);
              Iterator   fileItr  = fileList.iterator();

              DomainObject masterObj  = (DomainObject) DomainObject.newInstance(context,objectId);
              String masterObjectType = (String) map.get(SELECT_TYPE);
              String masterObjectName = (String) map.get(SELECT_NAME);
              String masterObjectRev  = (String) map.get(SELECT_REVISION);
              String masterObjectVault= (String) map.get(SELECT_VAULT);
              setMasterDocumentTitle(context, map, masterObj);

              mqlLogWriter ("Master Object TNR = " + masterObjectType + " " + masterObjectName + " " + masterObjectRev + "\n");

              // get the Version object data from the map
              StringList versionRelIdList       = (StringList)map.get(SELECT_VERSION_RELATIONSHIP_ID);
              StringList versionIdList          = (StringList)map.get(SELECT_VERSION_OBJECT_ID);
              StringList versionNameList        = (StringList)map.get(SELECT_VERSION_OBJECT_NAME);
              StringList versionRevList         = (StringList)map.get(SELECT_VERSION_OBJECT_REVISION);
              StringList versionTitleList       = (StringList)map.get(SELECT_VERSION_OBJECT_TITLE);
              StringList versionFileVersionList = (StringList)map.get(SELECT_VERSION_OBJECT_FILE_VERSION);

              StringList versionFileList        = (StringList)map.get(SELECT_VERSION_OBJECT_FILE_NAME);
              StringList versionFormatList      = (StringList)map.get(SELECT_VERSION_OBJECT_FILE_FORMAT);
              StringList versionHasFileList     = (StringList)map.get(SELECT_VERSION_OBJECT_HASFILES);

              /**
              for each file in the object, get the versions in a Data Structure(Stack)
              either in ascending or descending order
              */
              while(fileItr.hasNext())
              {
                  //writer.write( "Number Of Files = " + fileList.size() + "\n");

                  String fileName = (String)fileItr.next();

                  if( !"".equals(fileName))
                  {
                       // first get the FileVersionsMap
                       MapList fileVersionList = new MapList();
                       if(versionRelIdList != null)
                       {
                           for (int i=0; i<versionRelIdList.size(); i++)
                           {
                               Map fileVersionMap     = new HashMap();

                               // PMC allows file name change on Versioning
                               // but it has Only one file per Master object
                               // i.e. all the Version Objects correspond to the file in Master object
                               // Also PMC does not used to store FileName in the title attribute
                               // so to get the real file name, we can not depend on the title attribute
                               // get the FileName by reading the file in the object
                               // in COMMON Document model, Title of Version Objects must be equal to FileName
                               fileVersionMap.put(SELECT_ID,              (String)versionIdList.get(i));
                               fileVersionMap.put(SELECT_RELATIONSHIP_ID, (String)versionRelIdList.get(i));
                               fileVersionMap.put(SELECT_NAME,            (String)versionNameList.get(i));
                               fileVersionMap.put(SELECT_REVISION,        (String)versionRevList.get(i));
                               fileVersionMap.put(SELECT_FILE_VERSION, (String)versionFileVersionList.get(i));
                               fileVersionMap.put(SELECT_VERSION_OBJECT_FILE_NAME, (String)versionFileList.get(i));

                               fileVersionList.add(fileVersionMap);
                           }
                       }

                       Stack decendingVersionStack = new Stack();
                       Stack versionStack = new Stack();
                       int initialFileVersion = 1;

                       mqlLogWriter ( "Master File Name = " + fileName + "\n");
                       mqlLogWriter ( "Number Of File Versions = " + fileVersionList.size() + "\n");

                       // process only if there is One or more file Versions
                       if(fileVersionList.size() > 0)
                       {
                           // arrange the Versions in Stack
                           if(fileVersionList.size() > 1)
                           {
                               while(fileVersionList.size() > 0)
                               {
                                   for (int i=0; i<fileVersionList.size(); i++)
                                   {
                                       Map tmpMap = (Map)fileVersionList.get(i);
                                       String fileVersion = (String)tmpMap.get(SELECT_FILE_VERSION);

                                       int tmpVersion = Integer.parseInt(fileVersion);

                                       if(tmpVersion == initialFileVersion)
                                       {
                                           decendingVersionStack.push(tmpMap);
                                           initialFileVersion = tmpVersion + 1;
                                           fileVersionList.remove(tmpMap);

                                           break;
                                       }
                                   }
                               }

                               // now we File Versions in a Stack in Descending order of verions
                               // we want them in acending order, otherwise core does not let revisioning
                               // all the objects into one rev sequence
                               while(!decendingVersionStack.empty())
                               {
                                   versionStack.push((Map)decendingVersionStack.pop());
                               }

                           }
                           else if (fileVersionList.size() == 1 )
                           {
                               Map tmpMap = (Map)fileVersionList.get(0);
                               versionStack.push(tmpMap);
                           }

                           mqlLogWriter ( "File Versions in Stack= " + versionStack.size() + "\n");

                           String command = "";
                           String result = "";
                           String highestVersion = "";
                           DomainObject versionObj  = null;

                           // now we have the Versions in a Stack in ascending order
                           // create a core revision chain from this stack
                           while(!versionStack.empty())
                           {
                               Map lowVersionMap =  (Map)versionStack.pop();
                               if( lowVersionMap != null)
                               {
                                   String lowVersionId    = (String)lowVersionMap.get(SELECT_ID);
                                   String lowVersionRev   = (String)lowVersionMap.get(SELECT_FILE_VERSION);
                                   String versionRelId    = (String)lowVersionMap.get(SELECT_RELATIONSHIP_ID);
                                   String versionFileName = (String)lowVersionMap.get(SELECT_VERSION_OBJECT_FILE_NAME);
                                   versionObj  = (DomainObject) DomainObject.newInstance(context,lowVersionId);

                                   // since there is chance of duplicate object existence, change the name of version "Document" objects
                                   String lowVersionName = versionObj.getUniqueName("VD_");

                                   if(lowVersionRev.equals(""))
                                   {
                                       lowVersionRev = "''";
                                   }

                                   DomainRelationship.disconnect(context, versionRelId);

                                   // change the version object type to master object type
                                   // change the policy to 'Version' policy
                                   // since there is chance of duplicate object existence, change the name of version "Document" objects
                                   command = "modify bus " + lowVersionId + " type '" + masterObjectType + "' policy '" + POLICY_VERSION + "' name '" + lowVersionName + "' revision " + lowVersionRev;
                                   mqlLogWriter ( "Modify Version Object type,policy command = " + command + "\n");
                                   result = MqlUtil.mqlCommand(context, mqlCommand,  command);
                                   mqlLogWriter ( "Modify Version Object type,policy result = " + result + "\n");

                                   // set the IsVersionObject attribute to true
                                   // set the Title attribute to Version FileName
                                   versionObj.setAttributeValue(context, ATTRIBUTE_ISVERSIONOBJECT, "True");
                                   versionObj.setAttributeValue(context, ATTRIBUTE_TITLE, versionFileName);

                                   if(!versionStack.empty())
                                   {
                                       Map highVersionMap =  (Map)versionStack.peek();

                                       String highVersionId = null;

                                       if( highVersionMap != null)
                                       {
                                           highVersionId = (String)highVersionMap.get(SELECT_ID);

                                           // wire the two using core revision mechanism
                                           command = "revise bus " + lowVersionId + " bus " + highVersionId;
                                           mqlLogWriter ( "Revision Version command = " + command + "\n");
                                           result = MqlUtil.mqlCommand(context, mqlCommand,  command);
                                           mqlLogWriter ( "Revision Version Result = " + result + "\n");
                                       }
                                   }
                                   else
                                   {
                                       // PMC does not have Version Document for Highest version of file
                                       // migration should create one
                                       highestVersion = Integer.toString(Integer.parseInt(lowVersionRev) + 1);

                                       // implement the logic to create a dummy 'Version Document'
                                       String highestVersionId = createVersionDocument( context, map, fileName, highestVersion);

                                       // wire the two using core revision mechanism
                                       command = "revise bus " + lowVersionId + " bus " + highestVersionId;
                                       mqlLogWriter ( "Revise highest Version Object commnad = " + command + "\n");
                                       result = MqlUtil.mqlCommand(context, mqlCommand,  command);
                                       mqlLogWriter ( "Revise highest Version Object result = " + result + "\n");
                                   }
                               }
                           }

                       }
                       else if(fileVersionList.size() == 0)
                       {
                           // there is a file, but no Version
                           // implement the logic to create a new 'Version Document'
                           createVersionDocument( context, map, fileName, "1");
                       }
                   }
              }

              // unlock the master object, if the master is locked,
              // since we locked the version objects
              unlockMasterDocument(context, map, masterObj);
              loadMigratedOids (objectId);
          }
          catch (Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }

      }

      /**
       * V10.5 Common Document Management functionality migration
       * Currently, all 'Version Document' objects are wired to master Document
       * with Version relationship. Now, only the latest 'Version Document' will be wired
       * using 'Active Version', 'Latest Version' relationships.
       * Lower versions will be linked to Latest Version Document using core
       * revision mechanism.
       * Also, object type 'Version Document' is changed to master objct type
       * policy is chnaged to 'Version'
       *
       * @param context the eMatrix <code>Context</code> object
       * @param map HashMap holds most of the required selects including object id.
       * @returns nothing
       * @throws Exception if the operation fails
       * @since AEF 10.5
       * @grade 0
       */
      public void migrateDocumentProductSpecModel (Context context, Map map)
          throws Exception
      {
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
          if(scan)
          {
              return;
          }

          // get the master Document object Id
          String objectId = (String) map.get(SELECT_ID);
          try
          {

              mqlLogWriter( "Migrating object id :" + objectId + " in DocProductSpecModel :\n");

              // get all the files in the object
              StringList fileList = (StringList) map.get(SELECT_FILE_NAME);
              Iterator   fileItr  = fileList.iterator();

              DomainObject masterObj  = (DomainObject) DomainObject.newInstance(context,objectId);
              String masterObjectType = (String) map.get(SELECT_TYPE);
              String masterObjectName = (String) map.get(SELECT_NAME);
              String masterObjectRev  = (String) map.get(SELECT_REVISION);
              String masterObjectVault= (String) map.get(SELECT_VAULT);
              setMasterDocumentTitle(context, map, masterObj);

              mqlLogWriter ("Master Object TNR = " + masterObjectType + " " + masterObjectName + " " + masterObjectRev + "\n");

              // get the Version object data from the map
              StringList versionRelIdList       = (StringList)map.get(SELECT_VERSION_RELATIONSHIP_ID);
              StringList versionIdList          = (StringList)map.get(SELECT_VERSION_OBJECT_ID);
              StringList versionNameList        = (StringList)map.get(SELECT_VERSION_OBJECT_NAME);
              StringList versionRevList         = (StringList)map.get(SELECT_VERSION_OBJECT_REVISION);
              StringList versionTitleList       = (StringList)map.get(SELECT_VERSION_OBJECT_TITLE);
              StringList versionFileVersionList = (StringList)map.get(SELECT_VERSION_OBJECT_FILE_VERSION);

              StringList versionFileList        = (StringList)map.get(SELECT_VERSION_OBJECT_FILE_NAME);
              StringList versionFormatList      = (StringList)map.get(SELECT_VERSION_OBJECT_FILE_FORMAT);
              StringList versionHasFileList     = (StringList)map.get(SELECT_VERSION_OBJECT_HASFILES);

              /**
               for each file in the object, get the versions in a Data Structure(Stack)
               either in ascending or descending order
              */
              while(fileItr.hasNext())
              {
                  //writer.write( "Number Of Files = " + fileList.size() + "\n");

                  String fileName = (String)fileItr.next();

                  if( !"".equals(fileName))
                  {
                      // first get the FileVersionsMap
                      MapList fileVersionList = new MapList();
                      if(versionRelIdList != null)
                      {
                          for (int i=0; i<versionRelIdList.size(); i++)
                          {
                              Map fileVersionMap     = new HashMap();
                              String versionFileName = (String)versionTitleList.get(i);

                              if( versionFileName.equals(fileName))
                              {
                                  fileVersionMap.put(SELECT_ID,              (String)versionIdList.get(i));
                                  fileVersionMap.put(SELECT_RELATIONSHIP_ID, (String)versionRelIdList.get(i));
                                  fileVersionMap.put(SELECT_NAME,            (String)versionNameList.get(i));
                                  fileVersionMap.put(SELECT_REVISION,        (String)versionRevList.get(i));
                                  fileVersionMap.put(SELECT_FILE_VERSION, (String)versionFileVersionList.get(i));
                                  fileVersionMap.put(SELECT_TITLE, (String)versionTitleList.get(i));

                                  fileVersionList.add(fileVersionMap);
                              }
                          }
                      }

                      Stack versionStack = new Stack();
                      Stack decendingVersionStack = new Stack();

                      int initialFileVersion = 1;

                      mqlLogWriter ( "Master File Name = " + fileName + "\n");
                      mqlLogWriter ( "Number Of File Versions = " + fileVersionList.size() + "\n");

                      // process only if there is One or more file Versions
                      if(fileVersionList.size() > 0)
                      {
                          // arrange the Versions in Stack
                          if(fileVersionList.size() > 1)
                          {
                              while(fileVersionList.size() > 0)
                              {
                                  for (int i=0; i<fileVersionList.size(); i++)
                                  {
                                      Map tmpMap = (Map)fileVersionList.get(i);
                                      String fileVersion = (String)tmpMap.get(SELECT_FILE_VERSION);

                                      int tmpVersion = Integer.parseInt(fileVersion);

                                      if(tmpVersion == initialFileVersion)
                                      {
                                          decendingVersionStack.push(tmpMap);
                                          initialFileVersion = tmpVersion + 1;
                                          fileVersionList.remove(tmpMap);

                                          break;
                                      }
                                  }
                              }

                              // now we File Versions in a Stack in Descending order of verions
                              // we want them in acending order, otherwise core does not let revisioning
                              // all the objects into one rev sequence
                              while(!decendingVersionStack.empty())
                              {
                                 versionStack.push((Map)decendingVersionStack.pop());
                              }

                          }
                          else if (fileVersionList.size() == 1 )
                          {
                              Map tmpMap = (Map)fileVersionList.get(0);
                              versionStack.push(tmpMap);
                          }

                          mqlLogWriter ( "File Versions in Stack= " + versionStack.size() + "\n");

                          String command = "";
                          String result = "";
                          DomainObject versionObj  = null;

                          // now we have the Versions in a Stack in ascending order
                          // create a core revision chain from this stack
                          while(!versionStack.empty())
                          {
                              Map lowVersionMap =  (Map)versionStack.pop();

                              if( lowVersionMap != null)
                              {
                                  String lowVersionId    = (String)lowVersionMap.get(SELECT_ID);
                                  String lowVersionName  = (String)lowVersionMap.get(SELECT_NAME);
                                  String lowVersionRev   = (String)lowVersionMap.get(SELECT_FILE_VERSION);
                                  String versionRelId    = (String)lowVersionMap.get(SELECT_RELATIONSHIP_ID);
                                  String versionFileName = (String)lowVersionMap.get(SELECT_TITLE);
                                  versionObj  = (DomainObject) DomainObject.newInstance(context,lowVersionId);

                                  if(lowVersionName.equals(""))
                                  {
                                      lowVersionName = "''";
                                  }
                                  if(lowVersionRev.equals(""))
                                  {
                                      lowVersionRev = "''";
                                  }

                                  mqlLogWriter ( "versionRelId = " + versionRelId + "\n");

                                  DomainRelationship.disconnect(context, versionRelId);

                                  // change the version object type to master object type
                                  // change the policy to 'Version' policy
                                  command = "modify bus " + lowVersionId + " type '" + masterObjectType + "' policy '" + POLICY_VERSION + "' name '" + lowVersionName + "' revision " + lowVersionRev;

                                  mqlLogWriter ( "Modify Version Object type,policy command = " + command + "\n");
                                  result = MqlUtil.mqlCommand(context, mqlCommand,  command);
                                  mqlLogWriter ( "Modify Version Object type,policy result = " + result + "\n");

                                  // set the IsVersionObject attribute to true
                                  // set the Title attribute to Version FileName
                                  versionObj.setAttributeValue(context, ATTRIBUTE_ISVERSIONOBJECT, "True");
                                  versionObj.setAttributeValue(context, ATTRIBUTE_TITLE, versionFileName);

                                  if(!versionStack.empty())
                                  {
                                      Map highVersionMap =  (Map)versionStack.peek();
                                      String highVersionId = null;

                                      if( highVersionMap != null)
                                      {
                                          highVersionId = (String)highVersionMap.get(SELECT_ID);

                                          // wire the two using core revision mechanism
                                          command = "revise bus " + lowVersionId + " bus " + highVersionId;
                                          mqlLogWriter ( "Revision Version command = " + command + "\n");
                                          result = MqlUtil.mqlCommand(context, mqlCommand,  command);
                                          mqlLogWriter ( "Revision Version Result = " + result + "\n");
                                      }
                                  }
                                  else
                                  {
                                      masterObj.addToObjects(context,CommonDocument.RELATIONSHIP_ACTIVE_VERSION,versionObj);
                                      masterObj.addToObjects(context,CommonDocument.RELATIONSHIP_LATEST_VERSION,versionObj);

                                      // Lock the version object, if the master is locked
                                      lockVersionDocument(context, map, masterObj, versionObj);
                                  }
                              }
                          }
                      }
                      else if(fileVersionList.size() == 0)
                      {
                         // there is a file, but no Version
                         // implement the logic to create a new 'Version Document'
                         createVersionDocument( context, map, fileName, "1");
                      }
                  }
              }

              // unlock the master object, if the master is locked,
              // since we locked the version objects
              unlockMasterDocument(context, map, masterObj);
              loadMigratedOids (objectId);
          }
          catch (Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }
      }


      private String createVersionDocument(Context context, Map map, String fileName, String revision)
                                   throws Exception
      {
          try
          {
              // get the master Document object Id
              String objectId         = (String)map.get(SELECT_ID);
              String masterObjectType = (String) map.get(SELECT_TYPE);
              String masterObjVault   = (String) map.get(SELECT_VAULT);
              String masterObjOwner   = (String) map.get(SELECT_OWNER);
              String masterObjOriginated   = (String) map.get(SELECT_ORIGINATED);

              DomainObject object = DomainObject.newInstance(context, objectId);
              DomainObject versionDoc = DomainObject.newInstance(context, masterObjectType);

              versionDoc.createObject(context, masterObjectType,
                                              versionDoc.getUniqueName(context),
                                              revision,
                                              POLICY_VERSION, masterObjVault);

              String versionObjId = versionDoc.getInfo(context, SELECT_ID);

              HashMap attributes = new HashMap();
              attributes.put(ATTRIBUTE_TITLE, fileName);
              attributes.put(ATTRIBUTE_ISVERSIONOBJECT, "True");
              attributes.put(ATTRIBUTE_ORIGINATOR, masterObjOwner);
              versionDoc.setAttributeValues(context, attributes);

              // set the version owner same as master object
              // set the version originated date same as master object
              String command = "modify bus " + versionObjId + " owner '" + masterObjOwner + "' originated '" + masterObjOriginated + "';";
              String result = MqlUtil.mqlCommand(context, mqlCommand,  command);
              mqlLogWriter ( "Modify Version Object owner, originated dated command = " + command + "\n");
              mqlLogWriter ( "Modify Version Object owner, originated dated result = " + result + "\n");
              
              command = "connect businessobject " + objectId + " preserve relationship \"" + CommonDocument.RELATIONSHIP_ACTIVE_VERSION + "\" to " + versionObjId + " preserve;";
              result = MqlUtil.mqlCommand(context, mqlCommand,  command);
              mqlLogWriter ( "Connect Version Object with Active Version Relationship command = " + command + "\n");
              mqlLogWriter ( "Connect Version Object with Active Version Relationship result = " + result + "\n");
              
              command = "connect businessobject " + objectId + " preserve relationship \"" + CommonDocument.RELATIONSHIP_LATEST_VERSION + "\" to " + versionObjId + " preserve;";
              result = MqlUtil.mqlCommand(context, mqlCommand,  command);
              mqlLogWriter ( "Connect Version Object with Latest Version Relationship command = " + command + "\n");
              mqlLogWriter ( "Connect Version Object with Latest Version Relationship result = " + result + "\n");

              lockVersionDocument(context, map, object, versionDoc);

              return versionObjId;
          }
          catch (Exception ex)
          {
               mqlLogWriter ( "Failed in createVersionDocument :" + ex.toString() + "\n");
               throw ex;
          }
      }

      static HashMap inactiveUsers = new HashMap();
      static HashMap activeUsers = new HashMap();

      private void lockVersionDocument(Context context, Map map, DomainObject masterObj, DomainObject versionObject)
                                   throws Exception
      {
          String masterObjLock    = (String) map.get(SELECT_LOCKED);
          String masterObjLocker  = (String) map.get(SELECT_LOCKER);
          String versionId = versionObject.getObjectId();
          mqlLogWriter ( "masterObj Lock in lock version = " + masterObjLock + "\n");
          mqlLogWriter ( "masterObj Locker in lock version = " + masterObjLocker + "\n");
          String command = "";
          String result = "false";
          try
          {
              // in the new Common Document model, master object is no longer locked
              // instead corrsponding version object for the file is locked
              // if the master is locked, then lock the corresponding version object with same locker
              if ("TRUE".equalsIgnoreCase(masterObjLock))
              {
                  if ( !inactiveUsers.containsKey(masterObjLocker) )
                  {
                      if ( !activeUsers.containsKey(masterObjLocker) )
                      {
                          command = "print person '" + masterObjLocker + "' select inactive dump;";
                          result  = MqlUtil.mqlCommand(context, mqlCommand,  command);
                          if (result.equalsIgnoreCase("true"))
                          {
                              inactiveUsers.put(masterObjLocker, "");
                          }
                          else
                          {
                              activeUsers.put(masterObjLocker, "");
                          }
                      }
                      if (result.equalsIgnoreCase("false"))
                      {
                          ContextUtil.pushContext(context, masterObjLocker, null, context.getVault().getName());
                          try
                          {
                              command = "print bus " + versionId + " select current.access[lock] dump;";
                              result  = MqlUtil.mqlCommand(context, mqlCommand,  command);

                              // check for lock access before locking the Version object
                              // Under Version policy, user gets the lock access, if user has Checkin, Checkout access
                              // on the master object
                              // if User does not have lock access, proceed with migration
                              // and write the id to unConvertedObjectIds.csv file
                              if ("TRUE".equalsIgnoreCase(result))
                              {
                                  versionObject.lock(context);
                              }
                              else
                              {
                                  logUnableToLockIds ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Unable To Lock because no lock access," + versionId + "," + masterObjLocker);
                              }
                          }
                          catch (Exception ex)
                          {
                              mqlLogRequiredInformationWriter( "Failed in lockVersionDocument :" + ex.toString() + "\n");
                              logUnableToLockIds ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Unable To Lock because "+ ex.toString() +"," + versionId + "," + masterObjLocker);
                          }
                          finally
                          {
                              ContextUtil.popContext(context);
                          }
                      }
                      else
                      {
                          logUnableToLockIds ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Unable To Lock because person inactive," + versionId + "," + masterObjLocker);
                      }
                  }
                  else
                  {
                      logUnableToLockIds ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Unable To Lock because person inactive," + versionId + "," + masterObjLocker);
                  }
              }
          }
          catch (Exception ex)
          {
              mqlLogRequiredInformationWriter( "Failed in lockVersionDocument :" + ex.toString() + "\n");
              logUnableToLockIds ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Unable To Lock because "+ ex.toString() +"," + versionId + "," + masterObjLocker);
          }
      }


      private void unlockMasterDocument(Context context, Map map, DomainObject masterObj)
                                   throws Exception
      {
          String masterObjLock    = (String) map.get(SELECT_LOCKED);
          String masterObjLocker  = (String) map.get(SELECT_LOCKER);
          // get the master Document object Id
          String masterObjectId = (String)map.get(SELECT_ID);

          mqlLogWriter ( "masterObj Lock in unlock version = " + masterObjLock + "\n");
          mqlLogWriter ( "masterObj Locker in unlock version = " + masterObjLocker + "\n");
          String command = "";
          String result = "false";

          try
          {
              // in the new Common Document model, master object is no longer locked
              // instead corrsponding version object for the file is locked
              // if the master is locked, then lock the corresponding version object with same locker
              // unlock the master with same locker
              if ("TRUE".equalsIgnoreCase(masterObjLock))
              {
                  if ( !inactiveUsers.containsKey(masterObjLocker) )
                  {
                      if ( !activeUsers.containsKey(masterObjLocker) )
                      {
                          command = "print person '" + masterObjLocker + "' select inactive dump;";
                          result  = MqlUtil.mqlCommand(context, mqlCommand,  command);
                          if (result.equalsIgnoreCase("true"))
                          {
                              inactiveUsers.put(masterObjLocker, "");
                          }
                          else
                          {
                              activeUsers.put(masterObjLocker, "");
                          }
                      }
                      if (result.equalsIgnoreCase("false"))
                      {
                          ContextUtil.pushContext(context, masterObjLocker, null, context.getVault().getName());
                          try
                          {
                              command = "print bus " + masterObjectId + " select current.access[unlock] dump;";
                              result  = MqlUtil.mqlCommand(context, mqlCommand,  command);

                              // check for unlock access before unlocking the master object
                              // if User does not have unlock access, proceed with migration
                              // and write the id to unConvertedObjectIds.csv file
                              if ("TRUE".equalsIgnoreCase(result))
                              {
                                  mqlLogWriter ( "masterObj UnLock in unlock master = " + masterObjLock + "\n");
                                  masterObj.unlock(context);
                              }
                              else
                              {
                                  logUnableToLockIds ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Unable To UnLock because no unlock access,," + masterObjLocker);
                              }
                          }
                          catch (Exception ex)
                          {
                              mqlLogRequiredInformationWriter( "Failed in UnLockMasterDocument :" + ex.toString() + "\n");
                              logUnableToLockIds ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Unable To UnLock because "+ ex.toString() +",," + masterObjLocker);
                          }
                          finally
                          {
                              ContextUtil.popContext(context);
                          }
                      }
                      else
                      {
                          logUnableToLockIds ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Unable To UnLock because person inactive,," + masterObjLocker);
                      }
                  }
                  else
                  {
                      logUnableToLockIds ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Unable To UnLock because person inactive,," + masterObjLocker);
                  }
              }
          }
          catch (Exception ex)
          {
              mqlLogRequiredInformationWriter( "Failed in UnLockMasterDocument :" + ex.toString() + "\n");
              logUnableToLockIds ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Unable To UnLock because "+ ex.toString() +",," + masterObjLocker);
          }

      }

      private void setMasterDocumentTitle(Context context, Map map, DomainObject masterObj)
                                   throws Exception
      {
          String masterTitle   = (String) map.get(SELECT_TITLE);
          String masterName    = (String) map.get(SELECT_NAME);
          try
          {
              // in the new Common Document model, master object Title should not be empty
              // if the master doesn't have Title attribute filled in, then master object name
              // should be set as Title since we use title to display in tree
              if (masterTitle == null || "".equals(masterTitle) || "null".equals(masterTitle) )
              {
                  mqlLogWriter ( "Setting Master Object Title with master object Name : " + masterName + "\n");
                  masterObj.setAttributeValue(context, ATTRIBUTE_TITLE, masterName);
              }
          }
          catch (Exception ex)
          {
               mqlLogRequiredInformationWriter( "Failed in setMasterDocumentTitle :" + ex.toString() + "\n");
               throw ex;
          }
      }

      private void setSuspendVersioning(Context context, DomainObject masterObj)
                                   throws Exception
      {
          try
          {
              mqlLogWriter ( "Setting Master Object attribute Suspend Versioing to True : \n");
              masterObj.setAttributeValue(context, ATTRIBUTE_SUSPEND_VERSIONING, "True");
          }
          catch (Exception ex)
          {
               mqlLogRequiredInformationWriter( "Failed in setSuspendVersioning :" + ex.toString() + "\n");
               throw ex;
          }
      }
      /**
       * This method migrates the Engineering Central Data to Common Document data model
       *
       * @param context the eMatrix <code>Context</code> object
       * @param map HashMap holds most of the required selects including object id.
       * @returns nothing
       * @throws Exception if the operation fails
       */
      private void migrateEcModel(Context context , Map map) throws Exception
      {
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
          if(scan)
          {
              return;
          }

          // get the master Document object Id
          String objectId = (String)map.get(SELECT_ID);
          try
          {
              mqlLogWriter ( "Migrating object id :" + objectId + " in ECModel :\n");

              DomainObject masterObj  = (DomainObject) DomainObject.newInstance(context,objectId);
              setMasterDocumentTitle(context, map, masterObj);

              // get all the files in the object
              StringList fileList = (StringList) map.get(SELECT_FILE_NAME);
              Iterator   fileItr  = fileList.iterator();

              // for each file in the object, create a Version object
              while(fileItr.hasNext())
              {
                  mqlLogWriter( "Number Of Files = " + fileList.size() + "\n");
                  String fileName = (String)fileItr.next();
                  if( !"".equals(fileName))
                  {
                      mqlLogWriter( "fileName = " + fileName + "\n");
                      // create Version objects using revision as 1, sice Version policy
                      // has revision sequence as 1,2,3
                      // i.e. revision is hard coded for performance
                      createVersionDocument( context, map, fileName, "1");
                  }
                  unlockMasterDocument(context, map, masterObj);
              }
              loadMigratedOids (objectId);
          }
          catch (Exception ex)
          {
               throw ex;
          }
      }

    /**
     * This method writes the objectId to the sequential file, called from within JPO query where clause
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args[]  - [0]ObjectId, [1]type
     * @returns boolean
     * @throws Exception if the operation fails
     */
    public boolean writeOID(Context context, String[] args) throws Exception
    {
    	StringList EXCLUDED_TYPES = new StringList(5);
    	String excludedTypes = "";
    	try{
	    	EnoviaResourceBundle.getProperty(context, "emxCommonDocumentMigration.Exclude.Types");
	    	String[] excludedTypeArray = excludedTypes.split(",");
	    	EXCLUDED_TYPES = new StringList(excludedTypeArray.length);
	    	 for (int i=0; i< excludedTypeArray.length; i++)
	         {
	             EXCLUDED_TYPES.add(PropertyUtil.getSchemaProperty(context,(String)excludedTypeArray[i]));
	         }
    	}catch(Exception ex) {
            EXCLUDED_TYPES = new StringList();
        }
    	 
        String type = args[1];
        if ( !EXCLUDED_TYPES.contains(type) )
        {
            _objectidList.add(args[0]);
            _counter++;

            if (_counter == _chunk)
            {
                _counter=0;
                _sequence++;

                //write oid from _objectidList
                for (int s=0;s<_objectidList.size();s++)
                {
                    _fileWriter.write((String)_objectidList.elementAt(s));
                    _fileWriter.newLine();
                }

                _objectidList=new StringList();
                _fileWriter.close();

                //create new file
                _oidsFile = new java.io.File(documentDirectory + "documentobjectids_" + _sequence + ".txt");
                _fileWriter = new BufferedWriter(new FileWriter(_oidsFile));
            }
        }

        return false;
    }

    /**
     * This method takes care of leftover objectIds which do add up to the limit specified
     *
     * @param none
     * @returns none
     * @throws Exception if the operation fails
     */
    public static void cleanup() throws Exception
    {
        try
        {
            if(_objectidList != null && _objectidList.size() > 0)
            {
                for (int s=0;s<_objectidList.size();s++)
                {
                  _fileWriter.write((String)_objectidList.elementAt(s));
                  _fileWriter.newLine();
                }
                _fileWriter.close();
            }
            else
            {
                // delete the empty file created
                _fileWriter.close();
                _oidsFile.delete();
            }
        }
        catch(Exception Exp)
        {
            throw Exp;
        }
    }

    /**
     * This method does custom migration for all the objects whose ids are
     * listed in the FileName argument
     * this is a utility method defined for custom migration,
     * it can be called directly from MQL command prompt as below
     * execute program emxCommonDocumentMigration -method customMigration filename
     * this method calls migrateCustomObject method internally
     * Out-Of-The-Box migrateCustomObject will be empty, based on the need code can be added here
     * and can be invoked as above
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args --> args[0] documentDirectory
     * @param args --> args[1] fileName
     * @param args --> args[2] ModelName
     *                 Valied Model Names are
     *                          TeamSourcingModel
     *                          PMCModel
     *                          DocumentProductSpecModel
     *                          EcModel
     *                          IEFModel
     *                          CustomModel or Empty
     * @returns int, 0 for success 1 for failure
     * @throws Exception if the operation fails
     */
    public int customMigration(Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        }

        String fileName = null;
        String errorMessage = null;
        String modelName = "default";
        int status = 0;
        int modelNumber = 0;
        Map map = new HashMap();
        // WRITE THE CUSTOM MIGRATION CODE - OBJECT SELECTS BELOW - START /////////////
        // Add if there is a need add more selects here
        // for example: mxMainObjectSelects.add(CUSTOM_SELECT1);
        //              mxMainObjectSelects.add(CUSTOM_SELECT2);

        iefMigration = new emxIntegrationMigration_mxJPO(context, null);
        StringList selects = iefMigration.getIEFSelectables(context);
        mxMainObjectSelects.addAll(selects);

        // WRITE THE CUSTOM MIGRATION CODE - OBJECT SELECTS - END         /////////////

        try
        {
            if (args.length < 2 )
            {
                errorMessage = "Wrong number of arguments";
                throw new IllegalArgumentException();
            }

            documentDirectory = args[0];
            warningLog = new FileWriter(documentDirectory + "migration.log", true);

            // documentDirectory does not ends with "/" add it
            String fileSeparator = java.io.File.separator;
            if(documentDirectory != null && !documentDirectory.endsWith(fileSeparator))
            {
              documentDirectory = documentDirectory + fileSeparator;
            }

            fileName = args[1];
            modelName = args[2];
        }
        catch (IllegalArgumentException iExp)
        {
            writer.write("====================================================================\n");
            writer.write(errorMessage + " \n");
            writer.write("Step 2 of Migration :     FAILED \n");
            writer.write("====================================================================\n");
            writer.close();
            return 0;
        }
        catch(FileNotFoundException fExp)
        {
            // check if user has access to the directory
            // check if directory exists
            writer.write("=================================================================\n");
            writer.write("Directory does not exist or does not have access to the directory\n");
            writer.write("Step 2 of Migration :     FAILED \n");
            writer.write("=================================================================\n");
            writer.close();
            return 0;
        }
        catch (Exception ex)
        {
            modelName = "default";
            modelNumber = 0;
        }

        if ( modelName.equalsIgnoreCase("TeamSourcingModel") )
        {
            modelNumber = 1;
        }
        else if ( modelName.equalsIgnoreCase("PMCModel") )
        {
            modelNumber = 2;
        }
        else if ( modelName.equalsIgnoreCase("DocumentProductSpecModel") )
        {
            modelNumber = 3;
        }
        else if ( modelName.equalsIgnoreCase("EcModel") )
        {
            modelNumber = 4;
        }
        else if ( modelName.equalsIgnoreCase("IEFModel") )
        {
            modelNumber = 5;
        }
        // read the ids from passed in file
        StringList objectList = new StringList();
        try
        {
            objectList = readFiles(fileName);
        }
        catch(FileNotFoundException fnfExp)
        {
            // throw exception if file does not exists
            mqlLogRequiredInformationWriter("=================================================================\n");
            mqlLogRequiredInformationWriter("File " + documentDirectory + fileName + " does not exist Or No Access\n");
            mqlLogRequiredInformationWriter(" \n");
            mqlLogRequiredInformationWriter("Custom Migration FAILED \n");
            mqlLogRequiredInformationWriter(" \n");
            mqlLogRequiredInformationWriter("=================================================================\n");
            writer.close();
            return 0;
        }

        try
        {
              errorLog   = new FileWriter(documentDirectory + "unConvertedObjectIds_"+ unconvertedFileCount +".csv", true);
              unconvertedFileCount ++;
              errorLog.write("MASTER OID,TYPE,NAME,REVISION,CLASSIFICATION,VERSION OID,LOCKER\n");
              errorLog.flush();
        }
        catch(FileNotFoundException fExp)
        {
            // check if user has access to the directory
            // check if directory exists
            mqlLogRequiredInformationWriter("=================================================================\n");
            mqlLogRequiredInformationWriter("Directory does not exist or does not have access to the directory\n");
            mqlLogRequiredInformationWriter("Custom Migration :     FAILED \n");
            mqlLogRequiredInformationWriter("=================================================================\n");
            writer.close();
            return 0;
        }

        try
        {
            ContextUtil.pushContext(context);
            String cmd = "trigger off";
            MqlUtil.mqlCommand(context, mqlCommand,  cmd);

            migrationStartTime = System.currentTimeMillis();

            String[] oidsArray = new String[objectList.size()];
            oidsArray = (String[])objectList.toArray(oidsArray);
            MapList mapList = DomainObject.getInfo(context, oidsArray, mxMainObjectSelects);
            Iterator itr = mapList.iterator();
            String oid = "";
            String modified = "";
            String modCmd = "";

            while( itr.hasNext())
            {
                isConverted = false;
                StringList sourcingDocumentsList = new StringList();
                map = (Map) itr.next();
                String latestVersionToId   = (String)map.get(SELECT_TO_LATEST_VERSION_RELATIONSHIP_ID);
                String latestVersionFromId = (String)map.get(SELECT_FROM_LATEST_VERSION_RELATIONSHIP_ID);
                if ( latestVersionToId != null || latestVersionFromId != null )
                {
                     isConverted = true;
                     logWarning("Already Converted ", map);
                }
                status = 0;
                try
                {
                    ContextUtil.startTransaction(context,true);
                    if(!isConverted)
                    {
                        switch (modelNumber)
                        {
                            case 0:
                                status = migrateCustomObject(context, map);
                                break;
                            case 1:
                                migrateTeamSourcingModel(context, map);
                                break;
                            case 2:
                                migratePMCModel(context, map);
                                break;
                            case 3:
                                migrateDocumentProductSpecModel(context, map);
                                break;
                            case 4:
                                migrateEcModel(context, map);
                                break;
                            case 5:
                                iefMigration.migrateIEFModel(context, map);
                                break;
                            default:
                                noModelObjectIds("Not in any known models ", map);
                                break;
                        }
                    }
                    if(status == 1)
                    {
                        // abort thansaction if any exception is thrown
                        // continue the migration for the remaining objectIds in the file
                        mqlLogRequiredInformationWriter("=======================================================\n");
                        mqlLogRequiredInformationWriter("Migration of Object " + (String) map.get(SELECT_ID) + " FAILED \n");
                        mqlLogRequiredInformationWriter("=======================================================\n");
                        writeUnconvertedOID ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Custom Migration Failed \n");

                        ContextUtil.abortTransaction(context);
                        continue;
                    }
                    if(sourcingDocumentsList.size() > 0)
                    {
                         migrateSourcingDocuments(context, sourcingDocumentsList);
                    }
                    oid = (String)map.get(SELECT_ID);
                    modified = (String)map.get("modified");
                    modCmd = "mod bus " + oid + " modified '" + modified + "'";
                    MqlUtil.mqlCommand(context, mqlCommand,  modCmd);
                    ContextUtil.commitTransaction(context);
                }
                catch(Exception exp)
                {
                    // abort thansaction if any exception is thrown
                    // continue the migration for the remaining objectIds in the file
                    mqlLogRequiredInformationWriter("=======================================================\n");
                    mqlLogRequiredInformationWriter("Migration of Object " + (String) map.get(SELECT_ID) + " FAILED \n");
                    mqlLogRequiredInformationWriter("=======================================================\n");
                    writeUnconvertedOID ((String) map.get(SELECT_ID) + "," + (String)map.get(SELECT_TYPE) + "," + (String)map.get(SELECT_NAME) + "," + (String)map.get(SELECT_REVISION) + ",Custom Migration Failed \n");

                    ContextUtil.abortTransaction(context);
                    continue;
                }
            }

            mqlLogRequiredInformationWriter("=======================================================\n");
            mqlLogRequiredInformationWriter("                Custom Migration of Document Objects  COMPLETE\n");
            mqlLogRequiredInformationWriter("                Time: " + (System.currentTimeMillis() - migrationStartTime) + " ms\n");
            mqlLogRequiredInformationWriter(" \n");
            mqlLogRequiredInformationWriter("                Custom Migration :     SUCCESS \n");
            mqlLogRequiredInformationWriter(" \n");
            mqlLogRequiredInformationWriter("                Objects which cannot be migrated will be written to:  unConvertedObjectIds.csv\n");
            mqlLogRequiredInformationWriter("=======================================================\n");
        }
        catch(Exception exp)
        {
            mqlLogRequiredInformationWriter("=======================================================\n");
            mqlLogRequiredInformationWriter("Custom Migration FAILED \n");
            mqlLogRequiredInformationWriter("=======================================================\n");

            exp.printStackTrace();
            ContextUtil.abortTransaction(context);
        }
        finally
        {
            String cmd = "trigger on";
            MqlUtil.mqlCommand(context, mqlCommand,  cmd);
            ContextUtil.popContext(context);
            writer.close();
            errorLog.close();
        }

        return 0;
    }

    /**
     * This method does custom migration for all the object whose id is
     * passed in the objectId argument
     * this method should be implemented on the need basis
     * customMigration method calls this method internally
     * Out-Of-The-Box it will be empty, based on the need code can be added here
     *
     * @param context the eMatrix <code>Context</code> object
     * @param objectId, id of the document which needs to be migrated
     * @returns int, 0 for success 1 for failure
     * @throws Exception if the operation fails
     */
    private int migrateCustomObject(Context context , Map map) throws Exception
    {
        /*
        The following example migrates a Document object with multiple files 'N' checked
        in --> to a single Master Object with the corresponding number of 'N' 'Versions'

        String objectId = (String)map.get(SELECT_ID);
        try
        {
            DomainObject masterObj  = (DomainObject) DomainObject.newInstance(context,objectId);
            setMasterDocumentTitle(context, map, masterObj);

            // get all the files in the object
            StringList fileList = (StringList) map.get(SELECT_FILE_NAME);
            Iterator   fileItr  = fileList.iterator();

            // for each file in the object, create a Version object
            while(fileItr.hasNext())
            {
                String fileName = (String)fileItr.next();
                if( !"".equals(fileName))
                {
                    createVersionDocument( context, map, fileName, "1");
                }
                unlockMasterDocument(context, map, masterObj);
            }
        }
        catch(Exception exp)
        {
            throw new Exception("Custom Migration failed");
        }
        */


        /*
        To call another model.
        The following example migrates a CAD Model object with multiple files 'N' checked in
        --> to the EC model

        String masterObjectType = (String) map.get(SELECT_TYPE);
        if(masterObjectType.equals(CAD Model))
        {
            try{
              migrateEcModel(context,map);
            }
            catch(Exception exp)
            {
                throw new Exception("Custom Migration failed");
            }
        }

        */

      try
      {
        // WRITE THE CUSTOM MIGRATION CODE BELOW - START /////////////



        // CUSTOM MIGRATION CODE - END ///////////////////////////////
      }
      catch(Exception exp)
      {
          return 1;
      }
      return 0;
    }

    public void help(Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        }

        writer.write("================================================================================================\n");
        writer.write(" Migration is a two step process  \n");
        writer.write(" Step1: Find all objects derived from DOCUMENTS and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxCommonDocumentFindObjects 1000 C:/Temp/oids/; \n");
        writer.write(" First parameter  = 1000 indicates no of oids per file \n");
        writer.write(" Second Parameter = C:/Temp/oids/ is the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxCommonDocumentMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = C:/Temp/oids/ directory to read the files from\n");
        writer.write(" Second Parameter = 1 minimum range  \n");
        writer.write(" Third Parameter  = n minimum range  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write(" Fourth Parameter is Optional if sent as 'true', will convert orphaned -  \n");
        writer.write("          objects without any revisions to ECModel\n");
        writer.write(" Example: \n");
        writer.write(" execute program emxCommonDocumentMigration 'C:/Temp/oids/' 1 n true; \n");
        writer.write(" \n");
        writer.write(" Optional Step2: \n");
        writer.write(" execute program emxCommonDocumentFindUnConvertableObjects 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" The above command just scans all the files for any UnConvertable objects  \n");
        writer.write("     and writes to unConvertedObjectIds.csv without migrating any \n");
        writer.write(" Steps for Custom Migration: \n");
        writer.write(" execute program emxCommonDocumentMigration -method customMigration 'c:/Temp/oids/' file1.txt \n");
        writer.write(" First parameter  = C:/Temp/oids/ directory to read the files from\n");
        writer.write(" Second Parameter = file1.txt - file that has objectids to be migrated  \n");
        writer.write(" Third Parameter is Optional - to force run specific migration method on all objects in the file  \n");
        writer.write("          TeamSourcingModel        - to force run method migrateTeamSourcingModel \n");
        writer.write("          PMCModel                 - to force run method migratePMCModel  \n");
        writer.write("          DocumentProductSpecModel - to force run method migrateDocumentProductSpecModel  \n");
        writer.write("          EcModel                  - to force run method migrateEcModel  \n");
        writer.write("          IEFModel                 - to force run method migrateIEFModel \n");
        writer.write("          default                  - to force run method migrateCustomObject \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxCommonDocumentMigration -method customMigration 'c:/Temp/oids/' file1.txt EcModel\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
