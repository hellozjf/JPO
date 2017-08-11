/*
** emxVPLMTraceabilityReportBase
**
** Copyright (c) 2007-2008 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
*/
/*
** # @quickreview LX6 XXX 03/27/2015 IR-347523-3DEXPERIENCER2016x Issue in traceability report 
*/

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicClassItf;
import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicFactory;
import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicItf;
import com.dassault_systemes.vplm.data.PLMxJResultSet;
import com.dassault_systemes.vplm.dictionary.PLMDictionaryServices;
import com.dassault_systemes.vplm.implementLinkNav.interfaces.IVPLMImplementLinkNav;
import com.dassault_systemes.vplm.implementLinkNav.interfaces.IVPLMImplementLinkNav.implLinkNavPathMode;
import com.dassault_systemes.vplm.interfaces.access.IPLMxCoreAccess;
import com.dassault_systemes.vplm.modeler.PLMCoreModelerSession;
import com.dassault_systemes.vplm.modeler.entity.PLMxEntityDef;
import com.dassault_systemes.vplm.modeler.entity.PLMxEntityPath;
import com.dassault_systemes.vplm.modeler.entity.PLMxRefInstanceEntity;
import com.dassault_systemes.vplm.modeler.entity.PLMxReferenceEntity;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;

//CRK: DO NOT PREREQ VPLMxIntegrationUtilities
//import com.matrixone.vplmintegration.util.VPLMxIntegrationUtilities;
import com.matrixone.vplmintegrationitf.util.ISessionUtil;

/**
* This JPO class has methods pertaining to Functional and Logical Traceability Reports.
* @author SRP
* @version RequirementsManagement V6R2008-2.0 - Copyright (c) 2007, MatrixOne, Inc.
*/
public class emxVPLMTraceabilityReportBase_mxJPO extends emxDomainObject_mxJPO
{
   static public final String ATTR_PLM_EXTERNALID = "PLM_ExternalID";
   //START : LX6 IR-347523-3DEXPERIENCER2016x Issue in traceability report
   static public final String ATTRV_V_NAME = "V_Name";
   //START : LX6 IR-347523-3DEXPERIENCER2016x Issue in traceability report
   // Schema Constants:
   protected String              TYPE_REQUIREMENT        	= "";
   protected String              TYPE_CHAPTER            	= "";
   protected String              RFLPLMREQUIREMENT       	= "";
   protected String              RFLPLMFUNCTIONAL_REFERENCE	= "";
   protected String              RFLVPMLOGICAL_REFERENCE    = "";
   protected String              RFLVPMPHYSICAL_REFERENCE   = "";
   protected String              RFLPLMFUNCTIONAL_INSTANCE  = "";
   protected String              RFLVPMLOGICAL_INSTANCE     = "";
   protected String              RFLVPMPHYSICAL_INSTANCE    = "";
   protected String              VPLM_ID                 	= "";

   // Instance Variables:
   private Context               emxContext;
   private DomainObject          emxObject; //spec object

   private PLMCoreModelerSession plmSession;
   //private IRFLOperations        plmModeler;
   //private DomainObject          plmObject; //spec object, transient proxy
   private String                plmId                   = "";
   private boolean               debugTrace              = false;
   private IVPLMImplementLinkNav navModeler;
   private IPLMxCoreAccess 	     coreModeler;
   private MapList               reqObjects = new MapList();
   private boolean               isUnicornEnabled = false;

   /**
    * Create a new emxVPLMTraceabilityReportBase object.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return a emxVPLMTraceabilityReportBase object.
    * @throws Exception if the operation fails
    * @since RequirementsManagement V6R2008-2.0
    * @grade 0
    */
   public emxVPLMTraceabilityReportBase_mxJPO(Context context, String[] args) throws Exception
   {
      super(context, args);

      emxContext = context;

      RFLPLMREQUIREMENT = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLPLMRequirement");
      RFLPLMFUNCTIONAL_REFERENCE = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLPLMFunctional");
      RFLVPMLOGICAL_REFERENCE = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLVPMLogical");
      RFLVPMPHYSICAL_REFERENCE = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLVPMPhysical");
      RFLPLMFUNCTIONAL_INSTANCE = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLPLMFunctionalInstance");
      RFLVPMLOGICAL_INSTANCE = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLVPMLogicalInstance");
      RFLVPMPHYSICAL_INSTANCE = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLVPMPhysicalInstance");
      VPLM_ID = FrameworkProperties.getProperty(context, "emxRequirements.VPLMAttr.VPLMID");
      TYPE_REQUIREMENT = PropertyUtil.getSchemaProperty(context, "type_Requirement");
      TYPE_CHAPTER = PropertyUtil.getSchemaProperty(context, "type_Chapter");
      isUnicornEnabled = true;
      //System.out.println("|===>emxVPLMTraceabilityReportBase constructor...after calling super()");
   }

   public boolean initVPLMContext(String objectId)
   {
      try
      {
         // Look up the emxObject for the given object id...
         //System.out.println("\nget emxObject(" + objectId + ")");
         emxObject = DomainObject.newInstance(emxContext, objectId);

         //CRK: DO NOT PREREQ VPLMxIntegrationUtilities
         //plmSession = VPLMxIntegrationUtilities.getSession(emxContext);

         //Class factory = Class.forName("com.matrixone.vplmintegration.util.SessionUtil");
         Class factory = Class.forName("com.matrixone.vplmintegration.util.VPLMIntegSessionUtils");
         ISessionUtil sessionUtil = (ISessionUtil) factory.newInstance();
         plmSession = sessionUtil.getPLMCoreModelerSession(emxContext);
         if (plmSession != null)
         {
            plmSession.openSession();
            // Get PLMxRFL modeler implementation, or throw an exception if not installed...
            //plmModeler = (IRFLOperations) plmSession.getModeler("com.dassault_systemes.vplm.modeler.rflp.implementation.PLMxRFL");

            navModeler = (IVPLMImplementLinkNav) plmSession.getModeler("com.dassault_systemes.vplm.implementLinkNav.implementation.VPLMImplementLinkNav");

            coreModeler = (IPLMxCoreAccess) plmSession.getModeler("com.dassault_systemes.vplm.modeler.PLMCoreAccess");
         }

         // transient VPM proxy
         //plmObject = emxObject;
      }
      catch (Exception e)
      {
         emxContext = null;
         e.printStackTrace();
      }

      return(emxContext != null);
   }

   /**
    * Main entry point.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return an integer status code (0 = success)
    * @throws Exception if the operation fails
    * @since RequirementsManagement V6R2008-2.0
    * @grade 0
    */
   public int mxMain(Context context, String[] args) throws Exception
   {
      if (!context.isConnected())
      {
         String strContentLabel = i18nNow.getI18nString("emxRequirements.Alert.FeaturesCheckFailed",
               "emxRequirementsStringResource", context.getSession().getLanguage());
         throw new Exception(strContentLabel);
      }
      return(0);
   }

   /**
    * Print Debug Trace Statement
    *
    *  @param iName the name of variable
    *  @param iVal the value to be printed
    *  @param iDebug the debug flag
    *  @return void
    *  @throws Exception if the operation fails
    *
    *  @since RequirementsManagement V6R2008-2.0
    */
   private void printDebugTrace(String iName, String iVal, boolean iDebug) throws IOException
   {
      if (iDebug)
      {
         if (iVal == null)
            System.err.println("|===>" + iName + " is NULL !!");
         else
            System.out.println("|===>" + iName + "=" + iVal);
      }
   }

   /**
    * Prints Multiple Debug Trace Statements
    *
    *  @param iName the name of variable
    *  @param iVal the values to be printed
    *  @param iDebug the debug flag
    *  @return void
    *  @throws Exception if the operation fails
    *
    *  @since RequirementsManagement V6R2008-2.0
    */
   private void printDebugTraces(String iName, String[] iVal, boolean iDebug) throws IOException
   {
      if (iDebug)
      {
         if (iVal == null)
            System.out.println("|===>" + iName + " is NULL !!");
         else
         {
            if (iVal.length == 0)
               System.out.println("|===>" + iName + " is EMPTY !!");
            else
            {
               for (int i = 0; i < iVal.length; i++)
                  System.out.println("|===>" + iName + "[" + i + "]=" + iVal[i]);
            }
         }
      }
   }

   /**
    * Check for null and print status
    *
    *  @param iName the name of variable
    *  @param iVal the value to be checked
    *  @param iDebug the debug flag
    *  @return void
    *  @throws Exception if the operation fails
    *
    *  @since RequirementsManagement V6R2008-2.0
    */
   private void checkForNull(String iName, Object iVal, boolean iDebug) throws IOException
   {
      if (iDebug)
      {
         if (iVal == null)
            System.out.println("|===>" + iName + " is NULL !!");
         else
            System.out.println("|===>" + iName + " is NOT NULL !!");
      }
   }

   /**
    *  Get Functional Roots for Traceability Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param rowIds an array of row IDs
    *  @return MapList List that contains the Function Root Data
    *  @throws Exception if the operation fails
    *
    *  @since RequirementsManagement V6R2008-2.0
    */
   public MapList getVPLMFunctionalRoots(Context context, String[] rowIds)
      throws ClassNotFoundException, Exception
   {
      //System.out.println("\n|===>emxVPLMTraceabilityReportBase::getVPLMFunctionalRoots");
      if (rowIds != null && rowIds.length == 1 && initVPLMContext(rowIds[0]))
    	  //START : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type     	  
          return getVPLMFLRoots(context, "Function");
      	  //END : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type

      return(new MapList());
   }

   /**
    *  Get Logical Roots for Traceability Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param rowIds an array of row IDs
    *  @return MapList List that contains the Logical Root Data
    *  @throws Exception if the operation fails
    *
    *  @since RequirementsManagement V6R2008-2.0
    */
   public MapList getVPLMLogicalRoots(Context context, String[] rowIds)
      throws ClassNotFoundException, Exception
   {
      //System.out.println("\n|===>emxVPLMTraceabilityReportBase::getVPLMLogicalRoots");
      if (rowIds != null && rowIds.length == 1 && initVPLMContext(rowIds[0]))
    	  //START : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type
          return getVPLMFLRoots(context, "Logical");
      	  //END : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type

      return(new MapList());
   }

   /**
    *  Get Physical Roots for Traceability Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param rowIds an array of row IDs
    *  @return MapList List that contains the Physical Root Data
    *  @throws Exception if the operation fails
    *
    *  @since RequirementsManagement V6R2013
    */
   public MapList getVPLMPhysicalRoots(Context context, String[] rowIds)
      throws ClassNotFoundException, Exception
   {
      //System.out.println("\n|===>emxVPLMTraceabilityReportBase::getVPLMLogicalRoots");
      if (rowIds != null && rowIds.length == 1 && initVPLMContext(rowIds[0]))
    	  //START : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type    	  
          return getVPLMFLRoots(context, "Physical");
      	  //END : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type

      return(new MapList());
   }

   /**
    * Generic method to get Functional or Logical Roots for Traceability Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param rootType indicates Functional or Logical Root type
    *  @return MapList List that contains the Function Root Data
    *  @throws Exception if the operation fails
    *
    *  @since RequirementsManagement V6R2008-2.0
    */
   //START : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type   
   private MapList getVPLMFLRoots(Context context, String rootType)
      throws ClassNotFoundException, Exception
   {
	  //END : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type	   
      MapList plmRoots = new MapList();

      if (coreModeler == null || navModeler == null)
         throw(new Exception("VPLM Modeler not initialized!"));

      if (emxObject == null)
         return(plmRoots);

      //System.out.println("\n|===>executing emxVPLMTraceabilityReportBase::getVPLMFLRoots");
      String debugTraceVal = FrameworkProperties.getProperty(emxContext, "emxRequirements.VPLMTraceabilityReport.DebugTraces");
      debugTraceVal = debugTraceVal.trim();
      //System.out.println("\n|===>emxVPLMTraceabilityReportBase.java...value of DebugTraces=" + debugTraceVal);
      if (debugTraceVal.equals("1"))
         debugTrace = true;
      else
      {
         //System.out.println("|===>WARNING: Debug has been disabled. Traces will not be printed...");
         //System.out.println("              To enable, set emxRequirements.VPLMTraceabilityReport.DebugTraces to 1 in emxRequirements.properties\n");
      }

      printDebugTrace("RFLPLMFunctional", RFLPLMFUNCTIONAL_REFERENCE, debugTrace);
      printDebugTrace("RFLVPMLogical", RFLVPMLOGICAL_REFERENCE, debugTrace);
      printDebugTrace("RFLVPMPhysical", RFLVPMPHYSICAL_REFERENCE, debugTrace);
      printDebugTrace("VPLM_ID", VPLM_ID, debugTrace);

      boolean alreadyActive = emxContext.isTransactionActive();
      try
      {
         //----------------------------------------------------------------
         //Initialize VPLM Access by performing the following:
         // 1. Set Role and Application on the m1 context
         // 2. Get & Open VPLM Session, Get VPLM Modeler
         // 3. Get VPLM Object using the m1 spec object.
         //----------------------------------------------------------------
         // HACK: Must flag this context as managed by the app so the PLMCoreModeler doesn't shut it down...
         if (!alreadyActive)
            emxContext.start(true);

         String vplmName = "";
         String vplmId = "";
         MapList FLRoots = null;

       //START : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type
         if (rootType.equals("Function"))
            FLRoots = getVPLMRoots(context, RFLPLMFUNCTIONAL_REFERENCE);
         else if (rootType.equals("Logical"))
            FLRoots = getVPLMRoots(context, RFLVPMLOGICAL_REFERENCE);
         else if (rootType.equals("Physical"))
        	FLRoots = getVPLMRoots(context, RFLVPMPHYSICAL_REFERENCE);
         //SpecificationStructure.printIndentedList("\nFLRoots:", FLRoots);
//END : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type
         //SpecificationStructure.printIndentedList("\nFLRoots:", FLRoots);

         if (FLRoots == null)
         {
            emxContext.abort();
            return(plmRoots);
         }

         for (int i = 0; i < FLRoots.size(); i++)
         {
            Map FLRoot = (Map) FLRoots.get(i);
            //START : LX6 IR-347523-3DEXPERIENCER2016x Issue in traceability report
            vplmName = (String) FLRoot.get(ATTRV_V_NAME);
            //START : LX6 IR-347523-3DEXPERIENCER2016x Issue in traceability report
            vplmId = (String) FLRoot.get(VPLM_ID);
            Map tempMap = new HashMap();
            if (rootType.equals("Function"))
            {
               tempMap.put("PLM_FunctionRoot", vplmId);
               tempMap.put("PLM_FunctionName", vplmName);
            }
            else if (rootType.equals("Logical"))
            {
               tempMap.put("PLM_LogicalRoot", vplmId);
               tempMap.put("PLM_LogicalName", vplmName);
            }
            else if (rootType.equals("Physical"))
            {
               tempMap.put("PLM_PhysicalRoot", vplmId);
               tempMap.put("PLM_PhysicalName", vplmName);
            }
            plmRoots.add(tempMap);
            printDebugTrace("FL index", Integer.toString(i), debugTrace);
            printDebugTrace("vplmName", vplmName, debugTrace);
            printDebugTrace("vplmId", vplmId, debugTrace);
         }
      }
      catch (ClassNotFoundException cnfe)
      {
         cnfe.printStackTrace();
         if (!alreadyActive && emxContext.isTransactionActive())
            emxContext.abort();
         throw (cnfe);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         if (!alreadyActive && emxContext.isTransactionActive())
            emxContext.abort();
         throw (e);
      }
      finally
      {
         if (!alreadyActive && emxContext.isTransactionActive())
            emxContext.commit();
      }

      //System.out.println("|===>returning from emxVPLMTraceabilityReportBase::getVPLMFLRoots\n");
      return(plmRoots);
   }


   /**
    * Method to to get VPLM  Roots based on input type
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param rootType Type of root to be retrieved
    *  @return List of  Roots
    *  @throws Exception if the operation fails
    *
    *  @since RequirementsManagement V6R2008-2.0
    */
   //START : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type
   private MapList getVPLMRoots(Context context, String rootType) throws Exception
   {
   //END : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type
      //System.out.println("\n|===>executing emxVPLMTraceabilityReportBase::getVPLMRoots");
      MapList plmRoots = new MapList();
      checkForNull("plmId", plmId, debugTrace);
      checkForNull("coreModeler", coreModeler, debugTrace);
      checkForNull("navModeler", navModeler, debugTrace);

      if (plmId == null)
         return(null);

      if (coreModeler == null || navModeler == null)
         throw (new Exception("VPLM Modeler not initialized!"));

      try
      {
         String    emxId = emxObject.getId();

         try
         {
            String[]  plmIds = coreModeler.convertM1IDinPLMID(new String[]{emxId}, isUnicornEnabled ? IPLMxCoreAccess.CBP_RESOLUTION_MODE : IPLMxCoreAccess.VPM_RESOLUTION_MODE );
            plmId = (plmIds == null || plmIds.length < 1? null: plmIds[0]);
         }
         catch (NullPointerException npe)
         {
            npe.printStackTrace();
         }

         if (plmId == null)
         {
            return(plmRoots);
         }

         PLMxEntityDef[][] implementingRoots = navModeler.getImplementing(IVPLMImplementLinkNav.implLinkNavPathMode.complete, new PLMxEntityPath(new String[]{plmId}));
         printDebugTrace("Number of VPLM Roots Found", implementingRoots == null ? "0" : Integer.toString(implementingRoots.length), debugTrace);

         printDebugTrace("rootType", rootType, debugTrace);

         for (int j = 0; implementingRoots != null && j < implementingRoots.length; j++)
         {
            //ImplementRRLink rrLink = rrLinks[j];
            //PLMxEntityDef curRoot = rrLink.getImplementing();
        	PLMxEntityDef curRoot = implementingRoots[j] == null || implementingRoots[j].length == 0 ? null : implementingRoots[j][0];
            checkForNull("curRoot", curRoot, debugTrace);
            if(curRoot == null) continue;
            String curRtPlmId = curRoot.getPLMIdentifier();
            printDebugTrace("getPLMIdentifier for this rootType returns :", curRtPlmId, debugTrace);
            //START : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type            
            IPLMDictionaryPublicItf dico = new IPLMDictionaryPublicFactory().getDictionary();
            IPLMDictionaryPublicClassItf iFunctionalType = dico.getClass(context, curRoot.getPLMType().substring(curRoot.getPLMType().indexOf("/")+1));
            // check the entity type
            if(iFunctionalType.isKindOf(context, rootType)){
//END : LX6 IR-361052 - Requirement-Physical Traceability not avaible for Product sub type            	 
               Map plmObject = new HashMap();
               plmObject.put(VPLM_ID, curRoot.getPLMIdentifier());

               // Add all the plm object attributes to the hashmap...
               Hashtable plmAtts = curRoot.getAttributes();
               printDebugTrace("BEGIN Dumping the attributes of VPLM root object==============", "", debugTrace);
               for (Enumeration keys = plmAtts.keys(); keys.hasMoreElements();)
               {
                  String key = (String) keys.nextElement();
                  String val = plmAtts.get(key) + ""; //IR A0669007: sometimes this returns com.dassault_systemes.vplm.data.PLMQLNULL
                  plmObject.put(key, val);
                  printDebugTrace("Attr Name", key, debugTrace);
                  printDebugTrace("Attr Value", val, debugTrace);
               }
               printDebugTrace("END Dumping  =================================================", "", debugTrace);

               // Insert the new root into the root list based on the PLM Identifier...
               String order = (String) plmObject.get(VPLM_ID);
               int slot;
               boolean skip = false;
               for (slot = 0; slot < plmRoots.size(); slot++)
               {
                  Map aroot = (Map) plmRoots.get(slot);
                  String check = (String) aroot.get(VPLM_ID);
                  
                  int o = order.compareTo(check);
                  if(o == 0)
                  {
                	  skip = true;
                	  break;
                  }
                	  
                  if (o < 0)
                     break;
               }
               if(!skip)
               {
            	   plmRoots.add(slot, plmObject);
               }
            }
         } //end-for rrLinks
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      //System.out.println("|===>returning from emxVPLMTraceabilityReportBase::getVPLMRoots\n");
      return(plmRoots);
   }


   /**
    *  Get objects for Traceability Functional and Logical Report
    *
    *  @param context the eMatrix <code>Context</code> object
    *  @param args an array of String arguments for this method
    *  @return List object that contains traceability data
    *  @throws Exception if the operation fails
    *
    *  @since RequirementsManagement V6R2008-2.0
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getTraceabilityFLData(Context context, String[] args) throws Exception
   {
      //System.out.println("\n|===>executing emxVPLMTraceabilityReportBase::getTraceabilityFLData");
      MapList m1TraceabilityData = new MapList();
      MapList vplmTraceabilityData = new MapList();

      //unpacking the Arguments from variable args
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      printDebugTrace("programMap", programMap.toString(), debugTrace);

      String strObjectId = (String) programMap.get("objectId");
      printDebugTrace("strObjectId", strObjectId, debugTrace);

      String rootObjId = (String) programMap.get("rootObjId");
      printDebugTrace("rootObjId", rootObjId, debugTrace);

      String reportType = (String) programMap.get("reportType");
      printDebugTrace("reportType", reportType, debugTrace);

      if (strObjectId != null && strObjectId.length() > 0)
      {
         if (!initVPLMContext(strObjectId))
         {
            System.err.println("Error in initVPLMContext() - Initialization failed");
            return(vplmTraceabilityData);
         }
      }
      else
      {
         System.err.println("Invalid arguments in getTraceabilityFLData() - expected [ objectId, rootObjId, reportType ]");
         return(vplmTraceabilityData);
      }

      //Get all the requirement objects related to this Spec Object
      StringList objSelects = new StringList();
      objSelects.addElement(DomainConstants.SELECT_ID);
      objSelects.addElement(DomainConstants.SELECT_TYPE);
      objSelects.addElement(DomainConstants.SELECT_NAME);
      StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
      relSelects.addElement("attribute[Sequence Order]");

      String strRelType = PropertyUtil.getSchemaProperty(context, "relationship_SpecificationStructure");
      if(getBooleanPreference(context, "RFLPIncludeSubReq"))
      {
    	  strRelType += "," + PropertyUtil.getSchemaProperty(context, "relationship_RequirementBreakdown");
      }
      String strObjTypes = TYPE_REQUIREMENT + "," + TYPE_CHAPTER;
      printDebugTrace("strObjTypes", strObjTypes, debugTrace);
      printDebugTrace("strRelName", strRelType, debugTrace);

      //---------------------------------------------------------------------------------
      // Javadoc definition for these 2 arguments :
      // getTo - Defines whether the expand is starting on the "to" side of the relationship.
      // getFrom - Defines whether the expand is starting on the "from" side of the relationship.
      //---------------------------------------------------------------------------------
      // Example:
      // For 'Specification Structure' relationship that starts from Specification
      // and ends in Requirement,  getTo should be false and getFrom should be true
      //---------------------------------------------------------------------------------
      boolean getTo = false;
      boolean getFrom = true;
      short sRecurse = 0; // to expand all levels


      boolean alreadyActive = emxContext.isTransactionActive();
      try
      {
         // HACK: Must flag this context as managed by the app so the PLMCoreModeler doesn't shut it down...
         if (!alreadyActive)
            emxContext.start(true);

         reqObjects = emxObject.getRelatedObjects(context, strRelType, strObjTypes, objSelects, relSelects,
                 getTo, getFrom, sRecurse, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, 0);
           checkForNull("reqObjects", reqObjects, debugTrace);

           reqObjects.sortStructure(context, "relationship,attribute[Sequence Order]", ",", "string,integer", ",");
           
           int indent = 0;
           Stack<String> path = new Stack<String>();
           //String[]  plmIds = coreModeler.convertM1IDinPLMID(new String[]{strObjectId});
           //path.push(plmIds[0]);

           for (int i = 0; i < reqObjects.size(); i++)
           {
              Map reqObj = (Map) reqObjects.get(i);
              String reqObjId = (String) reqObj.get(DomainConstants.SELECT_ID);
              printDebugTrace("reqObjId", reqObjId, debugTrace);
              String reqObjType = (String) reqObj.get(DomainConstants.SELECT_TYPE);
              printDebugTrace("reqObjType", reqObjType, debugTrace);
              String reqObjName = (String) reqObj.get(DomainConstants.SELECT_NAME);
              printDebugTrace("reqObjName", reqObjName, debugTrace);
              String reqRelId = (String) reqObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
              printDebugTrace("reqRelId", reqRelId, debugTrace);

              String[] reqPLMIDs =  coreModeler.convertM1IDinPLMID(new String[]{reqObjId}, isUnicornEnabled ? IPLMxCoreAccess.CBP_RESOLUTION_MODE : IPLMxCoreAccess.VPM_RESOLUTION_MODE );
              String reqPLMID = reqPLMIDs == null || reqPLMIDs.length == 0 ? null : reqPLMIDs[0];
              if(reqPLMID != null)
              {
            	  //START : LX6 IR-347523-3DEXPERIENCER2016x Issue in traceability report
            	  reqObj.put(ATTRV_V_NAME, reqPLMID);
            	  //START : LX6 IR-347523-3DEXPERIENCER2016x Issue in traceability report
              }
              String[] reqPLMRelIDs = coreModeler.convertM1IDinPLMID(new String[]{reqRelId}, isUnicornEnabled ? IPLMxCoreAccess.CBP_RESOLUTION_MODE : IPLMxCoreAccess.VPM_RESOLUTION_MODE );
              String reqPLMRelID = reqPLMRelIDs == null || reqPLMRelIDs.length == 0 ? null : reqPLMRelIDs[0];

              String relLevel = (String) reqObj.get(DomainConstants.SELECT_LEVEL);
              int level = 1;
              try
              {   level = Integer.parseInt(relLevel); }
              catch (Exception e)
              {   level = 1; }
              if (level > indent)
              {
                 indent = level;
                 path.push(reqPLMRelID);
              }
              else if (level == indent)
              {
           	     path.pop();
           	     path.push(reqPLMRelID);
              }
              else
              {
                 do
                 {
               	     path.pop();
                     indent--;
                 } while (level < indent);

              	 path.pop();
                 path.push(reqPLMRelID);
              }

              if (!reqObjType.equals(TYPE_CHAPTER))
              {
                 Map tempMap = new HashMap();
                 tempMap.put(DomainConstants.SELECT_ID, reqObjId);
                 tempMap.put(reportType, ""); //clear the "Function" or "Logical" value
                 m1TraceabilityData.add(tempMap);

                 //path.push(reqPLMID);
                 reqObj.put("$PLMPATH", path.toArray(new String[]{}));
              }
           }


         printDebugTrace("Before calling getVPLMTraceabilityFLData", "", debugTrace);
         vplmTraceabilityData = getVPLMTraceabilityFLData(context, rootObjId, reportType);
         printDebugTrace("After calling getVPLMTraceabilityFLData", "", debugTrace);
         printDebugTrace("BEGIN dumping vplm traceability data =====================", "", debugTrace);
         for (int m = 0; m < vplmTraceabilityData.size(); m++)
         {
            printDebugTrace("index", Integer.toString(m), debugTrace);
            Map mapObj = (Map) vplmTraceabilityData.get(m);
            String reqId = (String) mapObj.get(DomainConstants.SELECT_ID);
            printDebugTrace("reqId", reqId, debugTrace);
            String pObjID = (String) mapObj.get("plmID");
            printDebugTrace("pObjID", pObjID, debugTrace);
            String pType = (String) mapObj.get("plmType");
            printDebugTrace("pType", pType, debugTrace);
         }
         printDebugTrace("END dumping ============================================", "", debugTrace);
      }
      catch (Exception e)
      {
         e.printStackTrace(System.err);
         if (!alreadyActive && emxContext.isTransactionActive())
            emxContext.abort();
      }
      finally
      {
         if (!alreadyActive && emxContext.isTransactionActive())
            emxContext.commit();
      }

      printDebugTrace("Before calling mergeLists", "", debugTrace);
      MapList mergedTrData = mergeLists(m1TraceabilityData, vplmTraceabilityData);
      printDebugTrace("After calling mergeLists", "", debugTrace);
      printDebugTrace("BEGIN dumping merged list =====================", "", debugTrace);
      for (int j = 0; j < mergedTrData.size(); j++)
      {
         printDebugTrace("index", Integer.toString(j), debugTrace);
         Map dumpObj = (Map) mergedTrData.get(j);
         String temp_ID = (String) dumpObj.get(DomainConstants.SELECT_ID);
         String temp_funcname = (String) dumpObj.get(reportType); //"Function"  or "Logical"
         String temp_plmID = (String) dumpObj.get("plmID");
         String temp_plmType = (String) dumpObj.get("plmType");

         printDebugTrace("temp_ID", temp_ID, debugTrace);
         printDebugTrace("temp_funcname", temp_funcname, debugTrace);
         printDebugTrace("temp_plmID", temp_plmID, debugTrace);
         printDebugTrace("temp_plmType", temp_plmType, debugTrace);
      }
      printDebugTrace("END dumping =================================", "", debugTrace);
      //System.out.println("|===>returning from emxVPLMTraceabilityReportBase::getTraceabilityFLData\n");
      return(mergedTrData);
   }

   /**
    *  Merge the lists from m1 and vplm traceability data
    *
    *  @param iMapList1 m1 traceability data list
    *  @param iMapList2 vplm traceability data list
    *  @return List object that contains merged traceability data
    *  @throws Exception if the operation fails
    *
    *  @since RequirementsManagement V6R2008-2.0
    */
   //-------------------------------------------------------------------------------------------
   //IMPORTANT NOTE:  The order of the arguments is very important here
   //-------------------------------------------------------------------------------------------
   private MapList mergeLists(MapList iMapList1, MapList iMapList2) throws Exception
   {
      //System.out.println("\n|===>executing emxVPLMTraceabilityReportBase::mergeLists");
      MapList oMapList = new MapList();
      printDebugTrace("maplist1 size", Integer.toString(iMapList1.size()), debugTrace);
      printDebugTrace("maplist2 size", Integer.toString(iMapList2.size()), debugTrace);
      for (int i = 0; i < iMapList1.size(); i++)
      {
         Map t_map_1 = (Map) iMapList1.get(i);
         String t_ID_1 = (String) t_map_1.get(DomainConstants.SELECT_ID);
         printDebugTrace("t_ID_1", t_ID_1, debugTrace);
         boolean foundMatch = false;

         for (int j = 0; j < iMapList2.size(); j++)
         {
            Map t_map_2 = (Map) iMapList2.get(j);
            String t_ID_2 = (String) t_map_2.get(DomainConstants.SELECT_ID);
            printDebugTrace("t_ID_2", t_ID_2, debugTrace);
            if (t_ID_1.equals(t_ID_2))
            {
               printDebugTrace("Inside t_ID_1 equals t_ID_2 condition", "", debugTrace);
               oMapList.add(t_map_2);
               foundMatch = true;
            }
         }

         if (!foundMatch)
         {
            printDebugTrace("Match not found condition", "", debugTrace);
            oMapList.add(t_map_1);
         }
      }
      //System.out.println("|===>returning from emxVPLMTraceabilityReportBase::mergeLists\n");
      return oMapList;
   }
   
   
   
   private MapList getPLMReferences(String rootId, String reportType,String implementingRootType)throws Exception{
	   MapList dataList = new MapList();
	   Vector  pairList = new Vector();
	   for (int i = 0; i < reqObjects.size(); i++)
	      {
	          Map reqObj = (Map) reqObjects.get(i);
	          String reqObjType = (String) reqObj.get(DomainConstants.SELECT_TYPE);
	          String objId = (String) reqObj.get(DomainConstants.SELECT_ID);
	          String PLM_ID = (String)reqObj.get(ATTR_PLM_EXTERNALID);
	          if (reqObjType.equals(TYPE_CHAPTER))
	          {
	        	  continue;
	          }

	          String[] path = (String[])reqObj.get("$PLMPATH");
	          if(path == null) continue;
	          PLMxEntityDef[][] allPlmPaths = null;
	          try{      	  	
	        	  	allPlmPaths = navModeler.getImplementing(IVPLMImplementLinkNav.implLinkNavPathMode.complete, new PLMxEntityPath(PLM_ID));  
	          }
	          catch(Exception plmException)
	          {
	        	  plmException.printStackTrace();
	          }

	          for(int j = 0; allPlmPaths != null && j < allPlmPaths.length; j++)
	          {
	        	 PLMxEntityDef[] plmPaths = allPlmPaths[j];
	        	 
	        	 checkForNull("plmPaths", plmPaths, debugTrace);
	        	 if(plmPaths == null) continue;
		         PLMxReferenceEntity plmInst = (PLMxReferenceEntity) plmPaths[plmPaths.length-1];
		         
		         String ownerPLMId = ((PLMxReferenceEntity)plmPaths[0]).getPLMIdentifier();
		         
		         String refPlmId = plmInst.getPLMIdentifier();
		         printDebugTrace("refPlmId", refPlmId, debugTrace);
		         String[] prjPlmIds = coreModeler.convertPLMIDinM1ID(new String[]{refPlmId});

		         String fnLogM1ID = (prjPlmIds == null || prjPlmIds.length < 1? null: prjPlmIds[0]);
		         printDebugTrace("fnLogM1ID", fnLogM1ID, debugTrace);
		         
		         PLMxEntityDef[] fnLogRefObjs = this.getOpenedObjectsFromServer(new String[]{refPlmId});
		         PLMxEntityDef fnLogRefObj = fnLogRefObjs == null || fnLogRefObjs.length == 0 ? null : fnLogRefObjs[0];
		         checkForNull("funcLogRefObj", fnLogRefObj, debugTrace);
		         
		         if(fnLogM1ID == null || fnLogRefObj == null) continue;

		         Hashtable attHash = fnLogRefObj.getAttributes();
		         String fnLogName = (String) attHash.get(ATTRV_V_NAME);
		         printDebugTrace("fnLogName", fnLogName, debugTrace);

		         // Use the Reference type to get the right icon from emxSystem.properties
		         String fnLogType = fnLogRefObj.getPLMType();
		         // Remove the modeller prefix...
		         int slash = fnLogType.indexOf('/');
		         if (slash > 0)
		            fnLogType = fnLogType.substring(0, slash);
		         printDebugTrace("fnLogType", fnLogType, debugTrace);
				 String pairId = fnLogM1ID + "|" + objId;
				 if (!pairList.contains(pairId)&&fnLogType.contains(implementingRootType)) {
					pairList.add(pairId); 
					Map tempMap = new HashMap();
					tempMap.put(DomainConstants.SELECT_ID, objId);
					tempMap.put(reportType, fnLogName);
					tempMap.put("plmID", fnLogM1ID);
					tempMap.put("plmType", fnLogType);
					dataList.add(tempMap);
				 }
	          }

	      }
	   return dataList;
   }
   
   private MapList getPLMInstances(Context context, String rootId, String reportType,String implementingRootType)throws Exception{
	   MapList dataList = new MapList();
	   Vector  pairList = new Vector();
	   for (int i = 0; i < reqObjects.size(); i++)
	   {
		   Map reqObj = (Map) reqObjects.get(i);
		   String reqObjType = (String) reqObj.get(DomainConstants.SELECT_TYPE);
		   String objId = (String) reqObj.get(DomainConstants.SELECT_ID);
		   if (reqObjType.equals(TYPE_CHAPTER))
		   {
			   continue;
		   }

		   String[] path = (String[])reqObj.get("$PLMPATH");
		   if(path == null) continue;
		   PLMxEntityDef[][] allPlmPaths = null;
		   try{
			   allPlmPaths = navModeler.getImplementing(IVPLMImplementLinkNav.implLinkNavPathMode.complete, new PLMxEntityPath(path));       	  	      		
		   }
		   catch(Exception plmException)
		   {
			   plmException.printStackTrace();
		   }

		   for(int j = 0; allPlmPaths != null && j < allPlmPaths.length; j++)
		   {
			   PLMxEntityDef[] plmPaths = allPlmPaths[j];

			   checkForNull("plmPaths", plmPaths, debugTrace);
			   if(plmPaths == null) continue;
			   PLMxRefInstanceEntity plmInst = (PLMxRefInstanceEntity) plmPaths[plmPaths.length-1];

			   String ownerPLMId = ((PLMxRefInstanceEntity)plmPaths[0]).getOwnerPLMIdentifier();
			   if(!rootId.equalsIgnoreCase(ownerPLMId))
			   {
				   continue;
			   }

			   String refPlmId = plmInst.getRefPLMIdentifier();
			   printDebugTrace("refPlmId", refPlmId, debugTrace);
			   String[] prjPlmIds = coreModeler.convertPLMIDinM1ID(new String[]{refPlmId});

			   String fnLogM1ID = (prjPlmIds == null || prjPlmIds.length < 1? null: prjPlmIds[0]);
			   printDebugTrace("fnLogM1ID", fnLogM1ID, debugTrace);

			   PLMxEntityDef[] fnLogRefObjs = this.getOpenedObjectsFromServer(new String[]{refPlmId});
			   PLMxEntityDef fnLogRefObj = fnLogRefObjs == null || fnLogRefObjs.length == 0 ? null : fnLogRefObjs[0];
			   checkForNull("funcLogRefObj", fnLogRefObj, debugTrace);

			   if(fnLogM1ID == null || fnLogRefObj == null) continue;

			   Hashtable attHash = fnLogRefObj.getAttributes();
			   String fnLogName = (String) attHash.get(ATTRV_V_NAME);
			   printDebugTrace("fnLogName", fnLogName, debugTrace);


			   // Use the Reference type to get the right icon from emxSystem.properties
			   String fnLogType = fnLogRefObj.getPLMType();
			   // Remove the modeller prefix...
			   int slash = fnLogType.indexOf('/');
			   if (slash > 0)
				   fnLogType = fnLogType.substring(0, slash);
			   //fnLogType = fnLogType.substring(slash+1, fnLogType.length());
			   printDebugTrace("fnLogType", fnLogType, debugTrace);
			   String pairId = fnLogM1ID + "|" + objId;
			   IPLMDictionaryPublicItf dico = new IPLMDictionaryPublicFactory().getDictionary();
			   IPLMDictionaryPublicClassItf iFunctionalType = dico.getClass(context, plmInst.getPLMType().substring(plmInst.getPLMType().indexOf("/")+1));
			   // check the entity type
			   if(!pairList.contains(pairId)&&iFunctionalType.isKindOf(context, implementingRootType)){
				   pairList.add(pairId);
				   Map tempMap = new HashMap();
				   tempMap.put(DomainConstants.SELECT_ID, objId);
				   tempMap.put(reportType, fnLogName);
				   tempMap.put("plmID", fnLogM1ID);
				   tempMap.put("plmType", fnLogType);
				   dataList.add(tempMap);
			   }
		   }

	   }
	   return dataList;
   }
   
   /**
    *  Get VPLM Traceability data for the given root type (Functional or Logical)
    *
    *  @param rootId Id of the Functional or Logical Root
    *  @param reportType type of report ( Functional or Logical)
    *  @return List object that contains vplm traceability data
    *  @throws Exception if the operation fails
    *
    *  @since RequirementsManagement V6R2008-2.0
    */
   private MapList getVPLMTraceabilityFLData(Context context, String rootId, String reportType) throws Exception
   {
      //System.out.println("\n|===>executing emxVPLMTraceabilityReportBase::getVPLMTraceabilityFLData");
      MapList dataList = new MapList();
      Vector  pairList = new Vector();

      printDebugTrace("plmId", plmId, debugTrace);
      printDebugTrace("rootId", rootId, debugTrace);
      printDebugTrace("reportType", reportType, debugTrace);
      //checkForNull("plmModeler", plmModeler, debugTrace);

      if (navModeler == null || coreModeler == null)
         return(dataList);

      String implementingRootTypeReference = "";
      String implementingRootTypeInstance = "";
      if("Function".equalsIgnoreCase(reportType))
      {
    	  implementingRootTypeReference = RFLPLMFUNCTIONAL_REFERENCE;
    	  implementingRootTypeInstance = RFLPLMFUNCTIONAL_INSTANCE;
      }
      else if ("Logical".equalsIgnoreCase(reportType))
      {
    	  implementingRootTypeReference = RFLVPMLOGICAL_REFERENCE;
    	  implementingRootTypeInstance = RFLVPMLOGICAL_INSTANCE;
      }
      else if("Physical".equalsIgnoreCase(reportType))
      {
    	  implementingRootTypeReference = RFLVPMPHYSICAL_REFERENCE;
    	  implementingRootTypeInstance = RFLVPMPHYSICAL_INSTANCE;
      }
      dataList = getPLMInstances( context, rootId, reportType,implementingRootTypeInstance);
      dataList.addAll(getPLMReferences(rootId, reportType,implementingRootTypeReference));
      return dataList;
   }

   /**
    * Take a list of object plmidentifier and do a query against the server to get the opened objects
    *
    * @return the list of PLMxEntityDef which can be cast into the appropriate PLMxXXXXXX interface
    * @throws java.lang.Exception
     * @param iModelerSession
     * @param iListUnopenedPLMIdentifier - array of PLMIDs which are not opened
    */
 public PLMxEntityDef[] getOpenedObjectsFromServer(String[] iListUnopenedPLMIdentifier)
 throws Exception
 {
   if (iListUnopenedPLMIdentifier == null)
   {
     return null;
   }
   PLMxEntityDef[] tempoutputList = new PLMxEntityDef[iListUnopenedPLMIdentifier.length];
   PLMxEntityDef[] outputList = new PLMxEntityDef[iListUnopenedPLMIdentifier.length];


   for (int plmIDIndex = 0; plmIDIndex < iListUnopenedPLMIdentifier.length; plmIDIndex++)
   {
     PLMxJResultSet plmxresult = null;
     plmxresult = coreModeler.getProperties(iListUnopenedPLMIdentifier[plmIDIndex]);

     if ((plmxresult != null) && (plmxresult.next()))
     {

       PLMxEntityDef tempEnt = plmxresult.extractEntityFromRow();
       if (tempEnt != null)
       {
         tempoutputList[plmIDIndex] = tempEnt;
       }
       else
       {
         if (debugTrace) System.out.println("getOpenedObjectsFromServer=> ERROR: opened result for iListUnopenedPLMIdentitifier[" + plmIDIndex + "] (" + iListUnopenedPLMIdentifier[plmIDIndex] + ") is null!!!");
       }
     }
     else
     {
       if (debugTrace) System.out.println("getOpenedObjectsFromServer=> ERROR: plmxresult set for iListUnopenedPLMIdentitifier [" + plmIDIndex + "] (" + iListUnopenedPLMIdentifier[plmIDIndex] + ") is null!!!");
     }
   }

   //ensure the same order of output as the input
   if (debugTrace) System.out.println("getOpenedObjectsFromServer=> Ensuring proper order now");
   if (debugTrace) System.out.println("getOpenedObjectsFromServer=> iListUnopenedPLMIdentifier.length = " + iListUnopenedPLMIdentifier.length);
   if (debugTrace) System.out.println("getOpenedObjectsFromServer=> tempoutputList.length = " + tempoutputList.length);

   for (int i = 0; i < iListUnopenedPLMIdentifier.length; i++)
   {
     for (int j = 0; j < tempoutputList.length; j++)
     {
       //find matching plm ids
       if (debugTrace) System.out.println("getOpenedObjectsFromServer=> iListUnopenedPLMIdentifier[" + i + "]: " + iListUnopenedPLMIdentifier[i]);

       if ((tempoutputList[j] != null) && tempoutputList[j].getPLMIdentifier().equals(iListUnopenedPLMIdentifier[i]))
       {
         if (debugTrace) System.out.println("getOpenedObjectsFromServer=> tempoutputList[" + j + "] PLMID: " + tempoutputList[j].getPLMIdentifier());
         //found correct position to put the output
         outputList[i] = tempoutputList[j];
       }
       else
       {
         if (debugTrace) System.out.println("getOpenedObjectsFromServer=> tempoutputList[" + j + "]  is NULL!");
       }
     }
   }

   return outputList;
 }
 
public boolean getBooleanPreference(Context context, String key) throws FrameworkException
{
	String pref = PropertyUtil.getAdminProperty(context, PersonUtil.personAdminType, context.getUser(), "preference_RMT" + key);
	if(pref == null || pref.equals(""))
	{
		pref = FrameworkProperties.getProperty(context, "emxRequirements.Preference." + key + ".Default");
	}

	return "true".equalsIgnoreCase(pref);
}

}//END of class
