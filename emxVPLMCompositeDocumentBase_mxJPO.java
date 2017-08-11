/*
** emxVPLMCompositeDocumentBase
**
** Copyright (c) 2007-2008 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
*/

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.List;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.jdom.CDATA;
import com.matrixone.jdom.DocType;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.ProcessingInstruction;
import com.matrixone.jsystem.util.Base64Utils;
// import org.apache.axis2.util.Base64;

import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicClassItf;
import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicFactory;
import com.dassault_systemes.iPLMDictionaryPublicItf.IPLMDictionaryPublicItf;
import com.dassault_systemes.vplm.data.PLMQLNULL;
import com.dassault_systemes.vplm.data.PLMxDBRepEntity;
import com.dassault_systemes.vplm.data.PLMxJResultSet;
import com.dassault_systemes.vplm.data.interfaces.IPLMStreamData;
//import com.dassault_systemes.vplm.data.interfaces.PLMIDInterface;
import com.dassault_systemes.vplm.dictionary.PLMDictionaryServices;
import com.dassault_systemes.vplm.functionalNav.interfaces.IVPLMFunctionalNav;
import com.dassault_systemes.vplm.functionalNav.interfaces.functionalDirection;
import com.dassault_systemes.vplm.implementLinkNav.interfaces.IVPLMImplementLinkNav;
import com.dassault_systemes.vplm.interfaces.access.IPLMxCoreAccess;
import com.dassault_systemes.vplm.logicalNav.interfaces.IVPLMLogicalNav;
import com.dassault_systemes.vplm.logicalNav.interfaces.logicalDirection;
import com.dassault_systemes.vplm.modeler.PLMCoreModelerSession;
import com.dassault_systemes.vplm.modeler.entity.PLMxConnectionEntity;
import com.dassault_systemes.vplm.modeler.entity.PLMxEntityDef;
import com.dassault_systemes.vplm.modeler.entity.PLMxEntityPath;
import com.dassault_systemes.vplm.modeler.entity.PLMxPortEntity;
import com.dassault_systemes.vplm.modeler.entity.PLMxRefInstanceEntity;
import com.dassault_systemes.vplm.modeler.entity.PLMxReferenceEntity;
import com.dassault_systemes.vplm.modeler.entity.PLMxRepInstanceEntity;
import com.dassault_systemes.vplm.modeler.entity.PLMxRepresentationEntity;
//import com.dassault_systemes.vplm.modeler.service.PLMIDService;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;

//CRK: DO NOT PREREQ VPLMxIntegrationUtilities
//import com.matrixone.vplmintegration.util.VPLMxIntegrationUtilities;
import com.matrixone.vplmintegrationitf.util.ISessionUtil;

/**
 * Methods for exporting VPLM Functional/Logical Structures
 *
 * @author srickus
 * @version RequirementsManagement V6R2008-2.0
 */
public class emxVPLMCompositeDocumentBase_mxJPO extends emxDomainObject_mxJPO
{
   // Override the default Matrix date format string here:
   protected String              EMX_DATE_FORMAT         = eMatrixDateFormat.getEMatrixDateFormat();
   protected SimpleDateFormat    VPLM_DATE_FORMATTER     = new SimpleDateFormat(EMX_DATE_FORMAT);

   // Customizable Constants defined in emxRequirements.properties:
   protected String              TYPE_REQUIREMENT        = "";
   protected String              TYPE_CHAPTER            = "";
   protected String              RFLPLMFUNCTIONAL        = "";
   protected String              RFLVPMLOGICAL           = "";
   protected String              RFLVPMPHYSICAL           = "";
   protected String              RFLPLMFLOW           	= "";
   protected String              RFLPLMTYPE           	= "";
   protected String              RFLPLMFUNCTIONALCOMMUNICATION     = "";
   protected String              RFLPLMLOGICALCOMMUNICATION     = "";

   // Export Schema Constants:
   protected static final String        SELECT_RESERVED_BY              = "reservedby";
   protected static final String        RELATIONSHIP_REPRESENTATIONS    = "representations";
   protected static final String        RELATIONSHIP_TYPES              = "types";
   protected static final String        RELATIONSHIP_FLOWS              = "flows";
   protected static final String        RELATIONSHIP_PORTS              = "ports";
   protected static final String        RELATIONSHIP_IMPLEMENTED        = "implemented";
   protected static final String        RELATIONSHIP_CHILDREN           = "children";
   protected static final String        VPLM_EXTERNAL_ID                = "PLM_ExternalID";
   protected static final String        VPLM_VERSION                    = "majorrevision"; 
   protected static final String        VPLM_FUNCTION_ROOT              = "PLM_FunctionRoot";
   protected static final String        VPLM_FUNCTION_NAME              = "PLM_FunctionName";
   protected static final String        VPLM_LOGICAL_ROOT               = "PLM_LogicalRoot";
   protected static final String        VPLM_LOGICAL_NAME               = "PLM_LogicalName";
   protected static final String        VPLM_PHYSICAL_ROOT               = "PLM_PhysicalRoot";
   protected static final String        VPLM_PHYSICAL_NAME               = "PLM_PhysicalName";

   // Instance Variables:
   private Context                      emxContext;
   private DomainObject                 emxObject; //spec object

   private PLMCoreModelerSession        plmSession;
   //private IRFLOperations               plmModeler;
   //private DomainObject                 plmObject; //spec object, transient proxy
   private IVPLMLogicalNav 				plmLogicalModeler;
   private IVPLMFunctionalNav 			plmFunctionalModeler;
   private IPLMxCoreAccess 				coreModeler;
   private IVPLMImplementLinkNav        navModeler;
   private boolean                      isUnicornEnabled = false;
   private boolean               		debugTrace       = false;


   /**
    * Create a new Composite Document object.
    *
    * @param context
    *                the eMatrix <code>Context</code> object
    * @param args
    *                holds no arguments
    * @return a emxCompositeDocument object.
    * @throws Exception
    *                if the operation fails
    */
   public emxVPLMCompositeDocumentBase_mxJPO(Context context, String[] args) throws Exception
   {
      super(context, args);

      emxContext = context;
      emxObject = null;

      RFLPLMFUNCTIONAL = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLPLMFunctional");
      RFLVPMPHYSICAL = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLVPMPhysical");
      RFLVPMLOGICAL = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLVPMLogical");
      RFLPLMFLOW = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLPLMFlow");
      RFLPLMTYPE = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLPLMType");
      RFLPLMFUNCTIONALCOMMUNICATION = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLPLMFunctionalCommunication");
      RFLPLMLOGICALCOMMUNICATION = FrameworkProperties.getProperty(context, "emxRequirements.VPLMType.RFLPLMLogicalCommunication");
      TYPE_REQUIREMENT = PropertyUtil.getSchemaProperty(context, "type_Requirement");
      TYPE_CHAPTER = PropertyUtil.getSchemaProperty(context, "type_Chapter");
      isUnicornEnabled = true;
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

		 Class factory = Class.forName("com.matrixone.vplmintegration.util.VPLMIntegSessionUtils");
         ISessionUtil sessionUtil = (ISessionUtil) factory.newInstance();
         plmSession = sessionUtil.getPLMCoreModelerSession(emxContext);
         if (plmSession != null)
         {
            plmSession.openSession();
            navModeler = (IVPLMImplementLinkNav) plmSession.getModeler("com.dassault_systemes.vplm.implementLinkNav.implementation.VPLMImplementLinkNav");

            plmLogicalModeler = (IVPLMLogicalNav) plmSession.getModeler("com.dassault_systemes.vplm.logicalNav.implementation.VPLMLogicalNav");
            plmFunctionalModeler = (IVPLMFunctionalNav) plmSession.getModeler("com.dassault_systemes.vplm.functionalNav.implementation.VPLMFunctionalNav");
            coreModeler = plmSession.getVPLMAccess(); //(IPLMxCoreAccess)plmSession.getModeler("com.dassault_systemes.vplm.modeler.PLMCoreAccess");
         }

         String debugTraceVal = FrameworkProperties.getProperty(emxContext, "emxRequirements.VPLMTraceabilityReport.DebugTraces");
         debugTraceVal = debugTraceVal.trim();
         //System.out.println("\n|===>emxVPLMTraceabilityReportBase.java...value of DebugTraces=" + debugTraceVal);
         if (debugTraceVal.equals("1"))
            debugTrace = true;
      }
      catch (Exception e)
      {
         emxContext = null;
         e.printStackTrace();
      }

      return(emxContext != null);
   }

   /*
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

   public MapList getVPLMRoots(Context context, String[] objIds)
      throws Exception
   {
      if (objIds != null && objIds.length == 1 && initVPLMContext(objIds[0]))
         return(getVPLMRoots(context));

      return(new MapList());
   }


   private MapList getVPLMRoots(Context context)
      throws Exception
   {
      MapList   plmRoots = new MapList();

      if (navModeler == null || plmLogicalModeler == null || plmFunctionalModeler == null || coreModeler == null)
         throw(new Exception("VPLM Modeler not initialized!"));

      if (emxObject == null)
         return(plmRoots);

      try
      {
         // HACK: Must flag this context as managed by the app so the PLMCoreModeler doesn't shut it down...
         if (! emxContext.isTransactionActive())
            emxContext.start(true);

         String emxId = emxObject.getId();

         String plmId = null;
         try
         {
            String[] plmIds = coreModeler.convertM1IDinPLMID(new String[]{emxId}, isUnicornEnabled ? IPLMxCoreAccess.CBP_RESOLUTION_MODE : IPLMxCoreAccess.VPM_RESOLUTION_MODE );
            plmId = (plmIds == null || plmIds.length < 1? null: plmIds[0]);
         }
         catch (NullPointerException npe)
         {
            npe.printStackTrace();
         }

         if (plmId == null)
         {
            emxContext.abort();
            return(plmRoots);
         }

         // Get any Spec-Function and Spec-Logical links...
         PLMxEntityDef[][] implementingRoots = navModeler.getImplementing(IVPLMImplementLinkNav.implLinkNavPathMode.complete, new PLMxEntityPath(new String[]{plmId}));

         for (int ii= 0; implementingRoots != null && ii < implementingRoots.length; ii++)
         {
            PLMxEntityDef rr1Root = implementingRoots[ii] == null || implementingRoots[ii].length == 0 ? null : implementingRoots[ii][0];
            if(rr1Root == null) continue;

            String rr1Id = rr1Root.getPLMIdentifier();
            IPLMDictionaryPublicItf dico = new IPLMDictionaryPublicFactory().getDictionary();
            IPLMDictionaryPublicClassItf iFunctionalType = dico.getClass(context, rr1Root.getPLMType().substring(rr1Root.getPLMType().indexOf("/")+1));
            // Get the Function-Logical Root links, if this root is a Function...
            boolean isFunction = iFunctionalType.isKindOf(context, RFLPLMFUNCTIONAL);
            boolean isLogical = iFunctionalType.isKindOf(context, RFLVPMLOGICAL);
            PLMxEntityDef[][] implementingRoots2 = (isFunction || isLogical ? navModeler.getImplementing(IVPLMImplementLinkNav.implLinkNavPathMode.complete, new PLMxEntityPath(new String[]{rr1Id})): null);
            //System.out.println("\n -> #rr2Links = " + (rr2Links == null? -1: rr2Links.length));

            if (isFunction)
                plmRoots.add(buildVPLMRootLink(context, rr1Root, null, null));
            else if (isLogical)
                plmRoots.add(buildVPLMRootLink(context, null, rr1Root, null));
            else
          	    plmRoots.add(buildVPLMRootLink(context, null, null, rr1Root));

            if (implementingRoots2 != null)
            {
               for (int jj = 0; jj < implementingRoots2.length; jj++)
               {
                  PLMxEntityDef rr2Root = implementingRoots2[jj] == null ? null : implementingRoots2[jj][0];
                  boolean isLogical2 = iFunctionalType.isKindOf(context, RFLVPMLOGICAL);

                  if (isLogical2)
                	  plmRoots.add(buildVPLMRootLink(context, rr1Root, rr2Root, null));
                  else
                	 plmRoots.add(buildVPLMRootLink(context, rr1Root, null, rr2Root));

                  String rr2Id = rr2Root.getPLMIdentifier();
                  PLMxEntityDef[][] implementingRoots3 = (isLogical2 ? navModeler.getImplementing(IVPLMImplementLinkNav.implLinkNavPathMode.complete, new PLMxEntityPath(new String[]{rr2Id})): null);

                  if (implementingRoots3 != null)
                  {
                	  for( int kk = 0; kk < implementingRoots3.length; kk++)
                	  {
	                	  PLMxEntityDef rr3Root = implementingRoots3[kk] == null ? null : implementingRoots3[kk][0];
	                	  plmRoots.add(buildVPLMRootLink(context, rr1Root, rr2Root, rr3Root));
                	  }
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         if (emxContext.isTransactionActive())
            emxContext.abort();
      }
      finally
      {
         if (emxContext.isTransactionActive())
            emxContext.commit();
      }

      plmRoots.sort("order", "ascending", null);
      //SpecificationStructure.printIndentedList("  VPLM Roots:", plmRoots);
      return(plmRoots);
   }


   /**
    * @param implemented entity def
    * @param implementing entity def
    * @return rootMap
    * @throws MatrixException 
    */
   private HashMap buildVPLMRootLink(Context context, PLMxEntityDef implemented, PLMxEntityDef implementing, PLMxEntityDef implementing2) throws MatrixException
   {
      HashMap rootMap = new HashMap();
      String rootNone = "~";             // Use this for missing objects, so they sort to the end

      String rootOrder = (implemented == null? rootNone: addVPLMRoot(context, rootMap, implemented));
      rootOrder += "|" + (implementing == null? rootNone: addVPLMRoot(context, rootMap, implementing));
      rootOrder += "|" + (implementing2 == null? rootNone: addVPLMRoot(context, rootMap, implementing2));

      // Use the "FuncName|LogName" string to sort the root list...
      rootMap.put("order", rootOrder);

      return(rootMap);
   }

   private String addVPLMRoot(Context context, HashMap rootMap, PLMxEntityDef rootDef) throws MatrixException
   {
      String rootId = rootDef.getPLMIdentifier();

      Hashtable rootAtts = rootDef.getAttributes();
      String rootName = (String) rootAtts.get(VPLM_EXTERNAL_ID);
      String rootVer = "" + rootAtts.get(VPLM_VERSION);
      IPLMDictionaryPublicItf dico = new IPLMDictionaryPublicFactory().getDictionary();
      IPLMDictionaryPublicClassItf iFunctionalType = dico.getClass(context, rootDef.getPLMType().substring(rootDef.getPLMType().indexOf("/")+1));
      // Set the map key based on the entity type:
      boolean isFunction = iFunctionalType.isKindOf(context, RFLPLMFUNCTIONAL);
      boolean isLogical = iFunctionalType.isKindOf(context, RFLVPMLOGICAL);
      String    keyId = (isFunction? VPLM_FUNCTION_ROOT :
    	  				 isLogical ? VPLM_LOGICAL_ROOT : VPLM_PHYSICAL_ROOT);
      String    keyName = (isFunction? VPLM_FUNCTION_NAME:
    	  				   isLogical ? VPLM_LOGICAL_NAME : VPLM_PHYSICAL_NAME);

      // Get the Function Root name and identifier
      rootMap.put(keyId, rootId);
      rootMap.put(keyName, rootName + " (" + rootVer + ")");

      return(rootName);
   }


   private String getEMXIdentifier(PLMxEntityDef entityDef)
   {
      String    entId = null;
      String    emxId = null;
      try
      {
         if (entityDef == null)
         {
            return(emxId);
         }
         else if (entityDef instanceof PLMxRefInstanceEntity)
         {
            entId = ((PLMxRefInstanceEntity) entityDef).getRefPLMIdentifier();
         }
         else if (entityDef instanceof PLMxReferenceEntity)
         {
            entId = ((PLMxReferenceEntity) entityDef).getPLMIdentifier();
         }
         else if (!(entityDef instanceof PLMxReferenceEntity))
         {
            System.err.println("!!! unexpected EntityDef type: " + entityDef.getClass().getName());
         }

         String[] plmIds = coreModeler.convertPLMIDinM1ID(new String[]{entId});

         emxId = (plmIds == null || plmIds.length < 1? null: plmIds[0]);

      }
      catch (Exception e)
      {
         System.err.println("!!! getM1IDfromEntity(" + entityDef.getPLMIdentifier() + ") threw exception:");
         e.printStackTrace();
      }
      return(emxId);
   }

   /**
    * @param specId
    * @param reqIds
    */
   private MapList buildSpecStructureData(Vector reqIds)
   {
      MapList specList = new MapList();
      Map specMap = buildEMXObjectMap(emxObject);
      specMap.put("level", "0");
      specList.add(specMap);

      // Build a compound select clause from the list of Requirement IDs...
      StringBuffer buff = new StringBuffer();
      int reqCnt = reqIds.size();
      for (int ii = 0; ii < reqCnt; ii++)
      {
         if (ii > 0)
            buff.append(" || ");

         buff.append("id==");
         buff.append(reqIds.elementAt(ii));
      }

      // Expand the Specification structure, retrieving only the linked Requirements...
      String where = (buff.length() == 0? DomainConstants.EMPTY_STRING: buff.toString());
      //System.out.println("  ***  where: " + where);
      try
      {
         StringList objSelect = new StringList(DomainConstants.SELECT_ID);
         objSelect.addElement(DomainConstants.SELECT_TYPE);
         objSelect.addElement(DomainConstants.SELECT_NAME);
         objSelect.addElement(DomainConstants.SELECT_REVISION);
         objSelect.addElement(DomainConstants.SELECT_DESCRIPTION);
         objSelect.addElement(DomainConstants.SELECT_OWNER);
         objSelect.addElement(DomainConstants.SELECT_CURRENT);
         objSelect.addElement(DomainConstants.SELECT_MODIFIED);

         StringList relSelect = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
         relSelect.addElement(DomainConstants.SELECT_LEVEL);
         relSelect.addElement("attribute[" + DomainConstants.ATTRIBUTE_SEQUENCE_ORDER + "]");

         String strRelType = PropertyUtil.getSchemaProperty("relationship_SpecificationStructure");
         String strObjTypes = TYPE_REQUIREMENT + "," + TYPE_CHAPTER;

         MapList reqList = emxObject.getRelatedObjects(emxContext, strRelType, strObjTypes, objSelect, relSelect,
               false, true, (short) 0, where, DomainConstants.EMPTY_STRING, 0);
         specList.addAll(reqList);
      }
      catch (Exception e)
      {
         System.err.println("!!! error in expansion where: " + where);
         e.printStackTrace();
      }

      //SpecificationStructure.printIndentedList("  StructureData:", specList);
      return(specList);
   }


   /**
    * @param entityPaths
    * @return
    */
   private MapList buildVPLMObjectPathList(PLMxEntityDef[] entityDefs)
   {
      MapList entityList = new MapList();
      String[] entityPaths = new String[entityDefs.length];

      for (int ii = 0; ii < entityDefs.length; ii++)
      {
         HashMap entityMap = buildVPLMObjectMap(entityDefs[ii]);
         entityPaths[ii] = (String) entityMap.get(DomainConstants.SELECT_NAME);
         entityMap.put("level", "" + (ii+1));
         entityMap.put("order", entityPaths.clone());
         entityMap.put("relationship", RELATIONSHIP_CHILDREN);

         entityList.add(entityMap);
      }
      return(entityList);
   }

   /**
    * @param entityId
    * @return
    */
   private HashMap buildVPLMObjectMap(String entityId)
   {
      HashMap entityMap = new HashMap();
      try
      {
         PLMxEntityDef[] entities = getOpenedObjectsFromServer(new String[]{entityId});
         PLMxEntityDef entityDef = entities == null ? null : entities[0];
         if(entityDef == null) return entityMap;

         entityMap = buildVPLMObjectMap(entityDef);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return(entityMap);
   }

   /**
    * @param entityDef
    * @return
    */
   private HashMap buildVPLMObjectMap(PLMxEntityDef entityDef)
   {
      HashMap           entityMap = new HashMap();
      PLMxEntityDef     entityRef = entityDef;
      String            entityId = entityDef.getPLMIdentifier();

      // Only expose attributes from the Reference object, not the Instance:
      try
      {
         if (entityDef instanceof PLMxRefInstanceEntity)
         {
            entityId = ((PLMxRefInstanceEntity) entityDef).getRefPLMIdentifier();
            PLMxEntityDef[] entities = getOpenedObjectsFromServer(new String[]{entityId});
            entityRef = entities == null ? null : entities[0];

            if(entityRef == null) return entityMap;
         }
         else if (!(entityDef instanceof PLMxReferenceEntity || entityDef instanceof PLMxDBRepEntity))
         {
            System.err.println("!!! unexpected EntityDef type: " + entityDef.getClass().getName());
         }
      }
      catch (Exception e)
      {
         return(entityMap);
      }

      Hashtable entityAtts = entityRef.getAttributes();
      String    entityType = entityRef.getPLMType();
      String    entityName = (String) entityAtts.remove(VPLM_EXTERNAL_ID);
      String    entityRev = (entityAtts.containsKey(VPLM_VERSION)? "" + entityAtts.get(VPLM_VERSION): "");

      entityMap.put(DomainConstants.SELECT_ID, entityRef.getPLMIdentifier());
      entityMap.put(DomainConstants.SELECT_TYPE, entityType);
      entityMap.put(DomainConstants.SELECT_NAME, entityName);
      entityMap.put(DomainConstants.SELECT_REVISION, entityRev);

      // Add the VPLM EntityDef attributes to the hashmap...
      for (Enumeration keys = entityAtts.keys(); keys.hasMoreElements(); )
      {
         String key = (String) keys.nextElement();
         Object val = entityAtts.get(key);

         // Do not expose some of the internal Function/Logical reference attributes...
         if (!(key.equals("reserved") ||
               key.equals("reservedby") ||
               key.equals("C_updatestamp") ||
               key.equals("V_isTerminal") ||
               key.equals("V_isLastVersion") ||
               key.equals("V_ApplicabilityDate") ||
               key.equals("V_PathRelations"))
            )
         {
            if (key.equals("originated") || key.equals("modified"))
            {
               long millis = Long.parseLong((String) val) * 1000;
               Date dbdate = new Date(millis);
               val= VPLM_DATE_FORMATTER.format(dbdate);
            }

            entityMap.put(key, val);
         }
      }

      //SpecificationStructure.printIndentedMap(entityMap);
      return entityMap;
   }


   /**
    * @param flObjId
    * @param level
    */
   /*
   private MapList buildVPLMPortList(String flObjId, int level)
   {
      MapList   portList = new MapList();
      try
      {
         FLPort[] flPorts = plmModeler.getFLPorts(flObjId);
         //System.out.println("==> flPorts.length = " + (flPorts == null? -1: flPorts.length));

         for (int ii = 0; flPorts != null && ii < flPorts.length; ii++)
         {
            PLMxPortEntity portEnt = flPorts[ii].getPortEntity();
            HashMap portMap = buildVPLMPortMap(portEnt);
            portMap.put("level", "" + level);
            portMap.put("relationship", RELATIONSHIP_PORTS);
            portList.add(portMap);

            // Append the related Type/Flow information...
            HashMap     infoMap = new HashMap();
            String      entId = "";
            String      entType = "";
            String      entName = "";
            String      entRev = "";
            String      dirName = "";
            String      relName = "";

            if (flPorts[ii] instanceof FunctionalPort)
            {
               FunctionalPort fncPort = (FunctionalPort) flPorts[ii];
               PLMxEntityDef fncFlow = fncPort.getFlowEntity();
               Map flowMap = fncFlow.getAttributes();
               //SpecificationStructure.printIndentedMap(flowMap);

               // Hack: this name mapping should be provided by the metadata services.
               String   catName = "";
               if (flowMap.containsKey("V_Category"))
               {
                  String flowCat = (String) flowMap.get("V_Category");
                  if (flowCat.equals("1"))
                     catName = "Data";
                  else if (flowCat.equals("2"))
                     catName = "Control";
               }
               infoMap.put("V_Category", catName);

               entId   = fncFlow.getPLMIdentifier();
               entType = fncFlow.getPLMType();
               entName = fncPort.getFlowName();
               dirName = fncPort.getDirectionName();
               relName = RELATIONSHIP_FLOWS;
            }
            else if (flPorts[ii] instanceof LogicalPort)
            {
               LogicalPort logPort = (LogicalPort) flPorts[ii];
               PLMxEntityDef logSyst = logPort.getSystemTypeEntity();
               entId   = logSyst.getPLMIdentifier();
               entType = logSyst.getPLMType();
               entName = logPort.getSystemTypeName();
               dirName = logPort.getDirectionName();
               relName = RELATIONSHIP_TYPES;
            }

            //IR Mx375143
            portMap.put("V_Direction", dirName);        // override the number with its name.

            infoMap.put(DomainConstants.SELECT_ID, entId);
            infoMap.put(DomainConstants.SELECT_TYPE, entType);
            infoMap.put(DomainConstants.SELECT_NAME, entName);
            infoMap.put(DomainConstants.SELECT_REVISION, entRev);       // to validate CompositeDocument.dtd
            infoMap.put("level", "" + (level+1));       // one level below the current port level.
            infoMap.put("relationship", relName);

            portList.add(infoMap);
         }
      }
      catch (Exception e)
      {
         System.err.println("WARNING: Cannot get FLPorts for: " + flObjId + "\n" + e.getMessage());
      }
      return(portList);
   }
	*/
   /**
    * @param entityDef
    * @return
    */
   private HashMap buildVPLMPortMap(PLMxPortEntity portDef)
   {
      Hashtable portAtts = portDef.getAttributes();
      String    portId = portDef.getPLMIdentifier();
      String    portType = portDef.getPLMType();
      String    portName = (String) portAtts.remove(VPLM_EXTERNAL_ID);
      String    portRev = "";

      HashMap   portMap = new HashMap();
      portMap.put(DomainConstants.SELECT_ID, portId);
      portMap.put(DomainConstants.SELECT_TYPE, portType);
      portMap.put(DomainConstants.SELECT_NAME, portName);
      portMap.put(DomainConstants.SELECT_REVISION, portRev);            // to validate CompositeDocument.dtd

      // Add the VPLM EntityDef attributes to the hashmap...
      for (Enumeration keys = portAtts.keys(); keys.hasMoreElements(); )
      {
         String key = (String) keys.nextElement();
         Object val = portAtts.get(key);

         // Do not expose some of the internal Port Reference attributes...
         if (!(key.equals("reserved") ||
               key.equals("reservedby") ||
               key.equals("C_updatestamp") ||
               key.equals("V_isUptodate") ||
               key.equals("V_Direction") ||
               key.equals("V_nature") ||
               key.equals("V_Owner") ||
               key.equals("project"))
            )
         {
            if (key.equals("originated") || key.equals("modified"))
            {
               long millis = Long.parseLong((String) val) * 1000;
               Date dbdate = new Date(millis);
               val= VPLM_DATE_FORMATTER.format(dbdate);
            }

            portMap.put(key, val);
         }
      }
      return(portMap);
   }

   /**
    * @param flObjId
    * @param level
    */
   /*
   private MapList buildVPLMRepList(String flObjId, int level)
   {
      MapList   repList = new MapList();
      try
      {
         PLMxEntityDef[] flReps = plmModeler.getRepresentations(flObjId);
         //System.out.println("==> flReps.length = " + (flReps == null? -1: flReps.length));

         for (int ii = 0; flReps != null && ii < flReps.length; ii++)
         {
            HashMap repMap = buildVPLMObjectMap(flReps[ii]);
            repMap.put("level", "" + level);
            repMap.put("relationship", RELATIONSHIP_REPRESENTATIONS);

            // Add the data stream information...
            PLMxRepresentationEntity repEnt = (PLMxRepresentationEntity) flReps[ii];
            HashMap strMap = buildVPLMStreamMap(repEnt);
            repMap.putAll(strMap);
            repList.add(repMap);
         }
      }
      catch (Exception e)
      {
         System.err.println("WARNING: Cannot get FLReps for: " + flObjId + "\n" + e.toString());
      }

      return(repList);
   }
	*/

   /**
    * @param repEntity
    * @throws IOException
    */
   private HashMap buildVPLMStreamMap(PLMxRepresentationEntity repEntity) throws IOException
   {
      HashMap strMap = new HashMap();

      for (Iterator iterator = repEntity.getDataStreamList().iterator(); iterator.hasNext(); )
      {
         IPLMStreamData stream = (IPLMStreamData) iterator.next();
         ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);

         byte[] buffer = new byte[4096];
         int block = stream.read(buffer, 0, 4096);
         while (block > 0)
         {
            Base64Utils.encode(buffer, 0, block, baos);
            block = stream.read(buffer, 0, 4096);
         }

         String cntType = stream.getStreamFormat();
         String cntData = "";
         try
         {
            cntData = baos.toString("UTF-8");
         }
         catch (UnsupportedEncodingException e)
         {
            e.printStackTrace(System.err);
            cntData = e.getMessage();
         }

         strMap.put("Content Type", cntType);
         strMap.put("Content Data", cntData);
      }

      // Using the old way, just guess the data type is non-catia (type 3)
/*
      try
      {
         PLMxStreamStorage stream = plmModeler.getStreamsFromRepReference(repEntity, 3);
         if (stream != null)
         {
            stream.open("r");
            System.out.println("opened representation stream...");
            byte[] binData = stream.readStream(0);
            System.out.println("read " + binData.length + " bytes.");
            stream.close();

            String cntData = Base64Utils.encode(binData);
            System.out.println("encoded size = " + cntData.length() + " bytes.");

            // Don't know how we can find out what the actual data type is...
            strMap.put("Content Type", "rtf.b64");
            strMap.put("Content Data", cntData);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
*/

      return(strMap);
   }

   /**
    * @param domainObj
    * @return
    */
   private HashMap buildEMXObjectMap(DomainObject domainObj)
   {
      HashMap   objectMap = new HashMap();
      try
      {
         domainObj.open(emxContext);
         objectMap.put(DomainConstants.SELECT_ID, domainObj.getId());
         objectMap.put(DomainConstants.SELECT_TYPE, domainObj.getTypeName());
         objectMap.put(DomainConstants.SELECT_NAME, domainObj.getName());
         objectMap.put(DomainConstants.SELECT_REVISION, domainObj.getRevision());
         objectMap.put(DomainConstants.SELECT_VAULT, domainObj.getVault());

         objectMap.put(DomainConstants.SELECT_DESCRIPTION, domainObj.getDescription(emxContext));
         objectMap.put(DomainConstants.SELECT_OWNER, domainObj.getOwner(emxContext));
         objectMap.put(DomainConstants.SELECT_MODIFIED, domainObj.getModified(emxContext));
         objectMap.put(DomainConstants.SELECT_CURRENT, domainObj.getInfo(emxContext, DomainConstants.SELECT_CURRENT));
         objectMap.put(SELECT_RESERVED_BY, domainObj.getInfo(emxContext, SELECT_RESERVED_BY));

         Map tempMap = domainObj.getAttributeMap(emxContext, false);
         domainObj.close(emxContext);

         Object[] atts = tempMap.keySet().toArray();
         for (int ii = 0; ii < atts.length; ii++)
         {
            String val = (String) tempMap.get(atts[ii]);

            if (atts[ii].equals("Content Data"))
            {
               if (val.length() > 0)
               {
                  // TODO: Special encoding for rtf.gz.b64 encoded binary data:
                  // For now, just return the data as it is stored in the database...
               }
               else
               {
                  continue;
               }
            }

            objectMap.put(atts[ii], val);
         }
      }
      catch(Exception e)
      {
         e.printStackTrace(System.err);
      }
      return objectMap;
   }


   /**
    * Main entry point
    *
    * @param context
    *                context for this request
    * @param args
    *                holds no arguments
    * @return an integer status code (0 = success)
    * @exception Exception
    *                    when problems occurred
    */
   public int mxMain(Context context, String[] args) throws Exception
   {
      if (!context.isConnected())
      {
         String mess = i18nNow.getI18nString("emxRequirements.Alert.FeaturesCheckFailed",
               "emxRequirementsStringResource", context.getSession().getLanguage());
         throw new Exception(mess);
      }

      System.out.println("mxMain: args.length = " + args.length);

      for (int ii = 0; ii < args.length; ii++)
         System.out.println(ii + ".  " + args[ii]);

      return 0;
   }


   void print(PLMxEntityDef entity)
   {
	   print(entity, false);
   }
   void print(PLMxEntityDef entity, boolean detailed)
   {
	      Hashtable atts = entity.getAttributes();
	      String    id = entity.getPLMIdentifier();
	      String    type = entity.getPLMType();
	      System.err.println("PLM id: " + id);
	      System.err.println("PLM type: " + type);
	      System.err.println("Java type: " + entity.getClass());
	      System.err.println("Name: " + atts.get(VPLM_EXTERNAL_ID));

	      if(detailed)
	      {
	    	  System.out.println("Attributes:");
	    	  Map attributes = entity.getAttributes();
	    	  for (Iterator it = attributes.keySet().iterator(); it.hasNext(); )
	    	  {
	    		  String key = (String) it.next();
	    		  String value = attributes.get(key) + "";
	    		  System.out.println(key + ": " + value);
	    	  }

	      }
   }


   public Document buildCompositeDocument(Context context, String[] args)
   throws Exception
	{
	    Document rootDoc = new Document();
		rootDoc.setDocType(new DocType("CompositeDocument", "CompositeDocument.dtd"));

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String[] rowIds = (String[]) paramMap.get("rowIds");
		//SpecificationStructure.printIndentedMap(paramMap);

		if (rowIds != null && rowIds.length == 4) {
			if (!initVPLMContext(rowIds[0])) {
				System.err.println("Error in initVPLMContext() - Initialization failed");
				return (rootDoc);
			}
		} else {
			System.err.println("Invalid arguments in exportCompositeDocument() - expected [ reqspecID, functionId, logicalId, physicalId ]");
			return (rootDoc);
		}

		String specId = null; /*getVPLMIdentifier();*/
		String funcId = rowIds[1];
		String logicId = rowIds[2];
		String phyId = rowIds[3];

		try {
			// HACK: Must flag this context as managed by the app so the PLMCoreModeler doesn't shut it down...
			if (!emxContext.isTransactionActive())
				emxContext.start(true);

			Element rootElem = new Element("CompositeDocument");
			Element parmData = new Element("Parameters");
			Element treeData = new Element("StructureData");
			Element instData = new Element("InstanceData");

			// If there is a parameter map, put all the names/values under the Parameters element...
			if (requestMap != null && !requestMap.isEmpty()) {
				String style = (String) requestMap.get("stylesheet");
				if (style != null) {
					Map opts = new HashMap();
					opts.put("type", "text/xsl");
					opts.put("href", style);
					rootDoc.addContent(new ProcessingInstruction(
							"xml-stylesheet", opts));
				}

				fillMapElement(parmData, requestMap);
				rootElem.addContent(parmData);
			}
			rootDoc.setRootElement(rootElem);
			rootElem.addContent(treeData);
			rootElem.addContent(instData);

			Map objectPool = new HashMap();
			Map implementMap = new HashMap();
			if("".equals(funcId))
			{
				funcId = null;
			}
			if("".equals(logicId))
			{
				logicId = null;
			}
			if("".equals(phyId))
			{
				phyId = null;
			}

			implementMap = buildImplementMap(context, specId, funcId, logicId, phyId, objectPool);

			if (funcId != null) {
				Element funcRoot = buildElement(context, funcId, FUNCTION, objectPool, implementMap);
				treeData.addContent(funcRoot);
			}

			if (logicId != null) {
				Element logicRoot = buildElement(context, logicId, LOGICAL, objectPool, implementMap);
				treeData.addContent(logicRoot);
			}

			if (phyId != null) {
				Element phyRoot = buildElement(context, phyId, PHYSICAL, objectPool, implementMap);
				treeData.addContent(phyRoot);
			}
			//add instance entities
			//attributes data
			//representation data??
			//filter out internal attributes??
			for(Iterator plmIds = objectPool.keySet().iterator(); plmIds.hasNext(); )
			{
				try{
					String id = (String) plmIds.next();
		            // Create the instance data element...
		            Element objInst = new Element("ObjectInstance");

		            if(isCBPObject(context, id))
		            {
				    	String reqId = coreModeler.convertPLMIDinM1ID(new String[]{id})[0];
			            if (reqId != null)
			            {
			                DomainObject reqObj = DomainObject.newInstance(emxContext, reqId);
							reqObj.open(emxContext);
							objInst.setAttribute("id", reqObj.getId());

							Element typeElem = new Element("type");
							typeElem.addContent(reqObj.getTypeName());
							objInst.addContent(typeElem);

							String name = reqObj.getName();
							Element nameElem = new Element("name");
							nameElem.addContent(name);
							objInst.addContent(nameElem);

							String revision = reqObj.getRevision();
							Element revElem = new Element("revision");
							revElem.addContent(revision);
							objInst.addContent(revElem);

							reqObj.close(emxContext);
							objInst.setAttribute("alias", name + (revision == null ? "" : " (" + revision + ")"));

				            // other attributes
				            Map attributes = buildEMXObjectMap(reqObj);
				            fillObjElement(objInst, attributes);
			            }
		            }
		            else
		            {
						PLMxEntityDef entity = (PLMxEntityDef)objectPool.get(id);
					    Map attributes = entity.getAttributes();
					    tweakAttributes(context, entity, attributes);
					    String name = "" + attributes.get(VPLM_EXTERNAL_ID); //could be com.dassault_systemes.vplm.data.PLMQLNULL
			            String revision = (attributes.containsKey(VPLM_VERSION)? "" + attributes.get(VPLM_VERSION): "");

			            objInst.setAttribute("id", id);
			            objInst.setAttribute("alias", name + (revision == null? "": " (" + revision + ")"));

			            String type = entity.getPLMType();
						String[] mdlInfo = type.split("[/]");
			            String model = null;
						if (mdlInfo.length == 2) {
							model = mdlInfo[0];
							type = mdlInfo[1];
						}

			            Element mdlrElem = new Element("model");
			            if (model != null)
			            {
			               mdlrElem.addContent(model);
			               objInst.addContent(mdlrElem);
			            }
						Element typeElem = new Element("type");
			            typeElem.addContent(type);
			            objInst.addContent(typeElem);
			            Element nameElem = new Element("name");
			            nameElem.addContent(name);
			            objInst.addContent(nameElem);
			            Element revElem = new Element("revision");
			            if (revision != null)
			            {
			               revElem.addContent(revision);
			               objInst.addContent(revElem);
			            }

					    fillObjElement(objInst, attributes);

		            }
		            instData.addContent(objInst);
				}catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
			if (emxContext.isTransactionActive())
				emxContext.abort();
		} finally {
			if (emxContext.isTransactionActive())
				emxContext.commit();
		}

		return (rootDoc);
	}


   private boolean isCBPObject(Context context, String plmId)
   {
	   try{
		   String emxID = coreModeler.convertPLMIDinM1ID(new String[]{plmId})[0];
		   return DomainObject.newInstance(context, emxID).isKindOf(context,  TYPE_REQUIREMENT);
	   }catch(Exception ex)
	   {
		   return false;
	   }
	   //PLMIDInterface curPLMIDItf = PLMIDService.convertToPLMID(plmId);
	   //String curTypeName = curPLMIDItf.getTypeName();
	   //return PLMDictionaryServices.isCBPType(curTypeName);
   }


   private void tweakAttributes(Context context, PLMxEntityDef entity, Map attributes)
   {
	   try{
	       if (attributes.containsKey("V_Category") && getCategory(context, entity) == FLOW)
	       {
	          String flowCat = (String) attributes.get("V_Category");
	          if (flowCat.equals("1"))
	        	  flowCat = "Data";
	          else if (flowCat.equals("2"))
	        	  flowCat = "Control";
	          attributes.put("V_Category", flowCat);
	      }

	       if (attributes.containsKey("originated"))
	       {
	    	   String val = attributes.get("originated") + "";
	           long millis = Long.parseLong((String) val) * 1000;
	           Date dbdate = new Date(millis);
	           val= VPLM_DATE_FORMATTER.format(dbdate);
	           attributes.put("originated", val);
	       }
	       if (attributes.containsKey("modified"))
	       {
	    	   String val = attributes.get("modified") + "";
	           long millis = Long.parseLong((String) val) * 1000;
	           Date dbdate = new Date(millis);
	           val= VPLM_DATE_FORMATTER.format(dbdate);
	           attributes.put("modified", val);
	       }
	       if(entity instanceof PLMxPortEntity)
	       {
	    	   PLMxPortEntity port = (PLMxPortEntity)entity;
	    	   if(isFunctionalPort(context, port))
	    	   {
	    		   functionalDirection direction = plmFunctionalModeler.getAPortDirection(port);
	    		   if(direction == functionalDirection.Consumption)
	    		   {
	    			   attributes.put("V_Direction", "Consumption");
	    		   }
	    		   else
	    		   {
	    			   attributes.put("V_Direction", "Emission");
	    		   }
	    	   }
	    	   else
	    	   {
	    		   logicalDirection direction = plmLogicalModeler.getAPortDirection(port);
	    		   if(direction == logicalDirection.In)
	    		   {
	    			   attributes.put("V_Direction", "In");
	    		   }
	    		   else if(direction == logicalDirection.InOut)
	    		   {
	    			   attributes.put("V_Direction", "InOut");
	    		   }
	    		   else if(direction == logicalDirection.NoDirection)
	    		   {
	    			   attributes.put("V_Direction", "NoDirection");
	    		   }
	    		   else if(direction == logicalDirection.Out)
	    		   {
	    			   attributes.put("V_Direction", "Out");
	    		   }
	    	   }
	       }
	   }catch(Exception e)
	   {
		   e.printStackTrace();
	   }
   }


   private Map buildImplementMap(Context context, String specId, String funcRootId, String logicRootId, String phyRootId, Map objectPool) throws Exception
   {
	   Map implementMap = new HashMap();
      StringList objSelects = new StringList();
      objSelects.addElement(DomainConstants.SELECT_ID);
      objSelects.addElement(DomainConstants.SELECT_TYPE);
      objSelects.addElement(DomainConstants.SELECT_NAME);
      StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

      String strRelType = PropertyUtil.getSchemaProperty(context, "relationship_SpecificationStructure");
      if(getBooleanPreference(context, "RFLPIncludeSubReq"))
      {
    	  strRelType += "," + PropertyUtil.getSchemaProperty(context, "relationship_RequirementBreakdown");
      }
      String strObjTypes = TYPE_REQUIREMENT + "," + TYPE_CHAPTER;

      MapList reqObjects = emxObject.getRelatedObjects(context, strRelType, strObjTypes, objSelects, relSelects,
              false, true, (short)0, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, 0);
      int indent = 0;
      Stack<String> path = new Stack<String>();
      for (int i = 0; i < reqObjects.size(); i++)
      {
         Map reqObj = (Map) reqObjects.get(i);
         String reqObjId = (String) reqObj.get(DomainConstants.SELECT_ID);
         //printDebugTrace("reqObjId", reqObjId, debugTrace);
         String reqObjType = (String) reqObj.get(DomainConstants.SELECT_TYPE);
         //printDebugTrace("reqObjType", reqObjType, debugTrace);
         String reqObjName = (String) reqObj.get(DomainConstants.SELECT_NAME);
         //printDebugTrace("reqObjName", reqObjName, debugTrace);
         String reqRelId = (String) reqObj.get(DomainConstants.SELECT_RELATIONSHIP_ID);
         //printDebugTrace("reqRelId", reqRelId, debugTrace);

         String[] reqPLMIDs =  coreModeler.convertM1IDinPLMID(new String[]{reqObjId}, isUnicornEnabled ? IPLMxCoreAccess.CBP_RESOLUTION_MODE : IPLMxCoreAccess.VPM_RESOLUTION_MODE );
         String reqPLMID = reqPLMIDs == null || reqPLMIDs.length == 0 ? null : reqPLMIDs[0];
         String[] reqPLMRelIDs = coreModeler.convertM1IDinPLMID(new String[]{reqRelId}, isUnicornEnabled ? IPLMxCoreAccess.CBP_RESOLUTION_MODE : IPLMxCoreAccess.VPM_RESOLUTION_MODE );
         String reqPLMRelID = reqPLMRelIDs == null || reqPLMRelIDs.length == 0 ? null : reqPLMRelIDs[0];

         PLMxEntityDef[] reqInsts = getOpenedObjectsFromServer(reqPLMIDs);
         PLMxEntityDef reqInst = reqInsts == null ? null : reqInsts[0];

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
            PLMxEntityDef[][] allPlmPaths = null;
            try{
          	  allPlmPaths = navModeler.getImplementing(IVPLMImplementLinkNav.implLinkNavPathMode.complete, new PLMxEntityPath(path.toArray(new String[]{})));
            }
            catch(Exception plmException)
            {
          	  plmException.printStackTrace();
            }

            for(int j = 0; allPlmPaths != null && j < allPlmPaths.length; j++)
            {
	          	 PLMxEntityDef[] plmPath = allPlmPaths[j];

	          	 //checkForNull("plmPaths", plmPaths, debugTrace);
	          	 if(plmPath == null) continue;
	  	         PLMxRefInstanceEntity plmInst = (PLMxRefInstanceEntity) plmPath[plmPath.length-1];

	  	         String ownerPLMId = ((PLMxRefInstanceEntity)plmPath[0]).getOwnerPLMIdentifier();

	  	         if(! (ownerPLMId.equalsIgnoreCase(funcRootId) || ownerPLMId.equalsIgnoreCase(logicRootId) || ownerPLMId.equalsIgnoreCase(phyRootId)))
	  	         {
	  	        	 continue;
	  	         }

	  	         String implementingPLMId = plmInst.getPLMIdentifier();

	             if(implementingPLMId != null && reqPLMID != null)
	             {
	          	   objectPool.put(implementingPLMId, plmInst);
	          	   objectPool.put(reqPLMID, reqInst);

	             }
	          	 List list = (List)implementMap.get(implementingPLMId);
	          	 if(list == null)
	          	 {
	          	     list = new StringList();
	          	 }
	          	 list.add(reqPLMID);
	          	 implementMap.put(implementingPLMId, list);

	          	 if(ownerPLMId.equalsIgnoreCase(funcRootId) || ownerPLMId.equalsIgnoreCase(logicRootId))
	  	         {

		               PLMxEntityDef[][] allPlmPaths2 = null;
		               try{
		            	   allPlmPaths2 = navModeler.getImplementing(IVPLMImplementLinkNav.implLinkNavPathMode.complete, plmPath);
		               }
		               catch(Exception plmException)
		               {
		             	  plmException.printStackTrace();
		               }
		               for(int k = 0; allPlmPaths2 != null && k < allPlmPaths2.length; k++)
		               {
		            	   PLMxEntityDef[] plmPath2 = allPlmPaths2[k];

		            	   if(plmPath2 == null) continue;
			  	  	       String ownerPLMId2 = ((PLMxRefInstanceEntity)plmPath2[0]).getOwnerPLMIdentifier();

				  	       if(! (ownerPLMId2.equalsIgnoreCase(logicRootId) ||  ownerPLMId2.equalsIgnoreCase(phyRootId)))
				  	       {
				  	    	   continue;
				  	       }
				  	       PLMxRefInstanceEntity plmInst2 = (PLMxRefInstanceEntity) plmPath2[plmPath2.length-1];
				  	       String implementingPLMId2 = plmInst2.getPLMIdentifier();

				           if(implementingPLMId2 != null)
				           {
				          	 objectPool.put(implementingPLMId2, plmInst2);
				           }
			          	   List list2 = (List)implementMap.get(implementingPLMId2);
			          	   if(list2 == null)
			          	   {
			          		   list2 = new StringList();
			          	   }
			          	   list2.add(implementingPLMId2);
			          	   implementMap.put(implementingPLMId2, list2);

			          	   if(ownerPLMId2.equalsIgnoreCase(logicRootId))
			          	   {
				               PLMxEntityDef[][] allPlmPaths3 = null;
				               try{
				            	   allPlmPaths3 = navModeler.getImplementing(IVPLMImplementLinkNav.implLinkNavPathMode.complete, plmPath2);
				               }
				               catch(Exception plmException)
				               {
				             	  plmException.printStackTrace();
				               }
				               for(int m = 0; allPlmPaths3 != null && m < allPlmPaths3.length; m++)
				               {
				            	   PLMxEntityDef[] plmPath3 = allPlmPaths3[m];

				            	   if(plmPath3 == null) continue;
					  	  	       String ownerPLMId3 = ((PLMxRefInstanceEntity)plmPath3[0]).getOwnerPLMIdentifier();

						  	       if(! phyRootId.equalsIgnoreCase(ownerPLMId3))
						  	       {
						  	    	   continue;
						  	       }
						  	       PLMxRefInstanceEntity plmInst3 = (PLMxRefInstanceEntity) plmPath3[plmPath3.length-1];
						  	       String implementingPLMId3 = plmInst3.getPLMIdentifier();

						           if(implementingPLMId3 != null)
						           {
						          	 objectPool.put(implementingPLMId3, plmInst3);
						           }
					          	   List list3 = (List)implementMap.get(implementingPLMId3);
					          	   if(list3 == null)
					          	   {
					          		   list3 = new StringList();
					          	   }
					          	   list3.add(implementingPLMId3);
					          	   implementMap.put(implementingPLMId3, list3);
				               }
			          	   }
		               }
	  	         }

            }

         }
      }

	   return implementMap;
   }


   private static void fillMapElement(Element mapElem, Map parmMap)
   {
      Set      attSet = parmMap.keySet();
      Object[] attList = attSet.toArray();
      for (int kk = 0; kk < attList.length; kk++)
      {
         String   attKey = (String) attList[kk];
         Object   attVal = parmMap.get(attKey);
         Element  attElem = new Element("Parameter");

         attElem.setAttribute("name", attKey);
         if (attVal != null)
         {
            if (attVal instanceof String[])
            {
               String[]     attArr = (String[]) attVal;
               StringBuffer buffer = new StringBuffer(attArr.length > 0? attArr[0]: "");

               for (int oo = 1; oo < attArr.length; oo++)
                  buffer.append("," + attArr[oo]);

               attElem.setText(buffer.toString());
            }
            else
            {
               attElem.setText("" + attVal);
            }
         }

         mapElem.addContent(attElem);
      }
   }
   private static void fillObjElement(Element objElem, Map objMap)
   {
      Set       attSet = objMap.keySet();
      Object[]  attList = attSet.toArray();
      String    contType = "rtf.gz.b64";
      Object    contData = null;

      for (int kk = 0; kk < attList.length; kk++)
      {
         String   attKey = (String) attList[kk];
         Object   attVal = objMap.get(attKey);

         if (attKey.startsWith("attribute[") && attKey.endsWith("]"))
            attKey = attKey.substring(10, attKey.length()-1);

         Element  attElem = new Element("Attribute");
         attElem.setAttribute("name", attKey);
         if (attVal != null)
         {
            if (attKey.equals("Content Type"))
            {
               contType = "" + attVal;
            }
            else if (attKey.equals("Content Data"))
            {
               contData = "" + attVal;
            }
            else
            {
            	if(attVal instanceof PLMQLNULL ||
            			attKey.equals("V_ApplicabilityDate") && "NULL".equals(attVal))
            	{
            		attVal = "";
            	}

               if (attKey.equals("Content Text"))
                  attElem.addContent(new CDATA("" + attVal));
               else if(attVal instanceof String[])
        	   {
        		  StringBuffer buffer = new StringBuffer();
        		  String[] val = (String[])attVal;
        		  for(int i = 0; i < val.length; i++)
        		  {
        			buffer.append(val[i]);
        			if(i != val.length - 1)
        				buffer.append("|");
        		  }
        		  attElem.addContent(buffer.toString());
        	   }
               else if(attVal instanceof Collection)
        	   {
        		  StringBuffer buffer = new StringBuffer();
        		  Collection c = (Collection)attVal;
        		  Iterator it = c.iterator();
        		  while(it.hasNext())
        		  {
        			  buffer.append(it.next() + "");
        			  if(it.hasNext())
        			  {
        				  buffer.append("|");
        			  }
        		  }
        		  attElem.addContent(buffer.toString());
        	   }
               else
               {
                  attElem.addContent("" + attVal);
               }

               objElem.addContent(attElem);
            }
         }
      }

      // Append the Content Data, if there is any...
      if (contData != null)
      {
         Element  attElem = new Element("Attribute");
         attElem.setAttribute("name", "Content Data");
         attElem.setAttribute("format", contType);
         attElem.addContent(new CDATA("" + contData));
         objElem.addContent(attElem);
      }
   }

   static final int FUNCTION = 0;
   static final int LOGICAL = 1;
   static final int COMMUNICATION = 2;
   static final int FLOW = 3;
   static final int TYPE = 4;
   static final int M1 = 5;
   static final int CONNECTION = 6;
   static final int PORT = 7;
   static final int REPRESENTATION = 8;
   static final int OTHER = 9;
   static final int PHYSICAL = 10;

   private PLMxEntityDef retrievePLMObject(String plmId, Map ojbectPool) throws Exception
   {
	   PLMxEntityDef object = (PLMxEntityDef)ojbectPool.get(plmId);
	   if(object == null)
	   {
		   PLMxEntityDef[] objects = (PLMxEntityDef[]) getOpenedObjectsFromServer(new String[]{plmId});
		   object = objects == null ? null : objects[0];
	   }
	   return object;
   }
   private Element buildElement(Context context, String objectId, int category, Map objectPool, Map implMap) throws Exception
   {
		PLMxEntityDef entity = retrievePLMObject(objectId, objectPool);
	   return buildElement(context, entity, category, objectPool, implMap);
   }

   private Element buildElement(Context context ,PLMxEntityDef entity, int category, Map objectPool, Map implMap) throws Exception
   {
	   return buildElement(context, entity, category, true, objectPool, implMap);
   }

   private Element buildCBPElement(String plmId)
   {
	   Element elem = new Element("ObjectReference");
	   elem.setAttribute("category", "m1");
	   try{
		   String reqId = coreModeler.convertPLMIDinM1ID(new String[]{plmId})[0]; //getEMXIdentifier(entity);
		   if (reqId != null)
		   {
		      DomainObject reqObj = DomainObject.newInstance(emxContext, reqId);

		      reqObj.open(emxContext);
			  elem.setAttribute("id", reqObj.getId());
			  elem.setAttribute("type", reqObj.getTypeName());
			  elem.setAttribute("name", reqObj.getName());
			  elem.setAttribute("revision", reqObj.getRevision());
			  reqObj.close(emxContext);
		   }
	   }catch(Exception e)
	   {
		   e.printStackTrace();
	   }

	   return elem;

   }
   private Element buildElement(Context context, PLMxEntityDef entity, int category, boolean recursive, Map objectPool, Map implMap) throws MatrixException
   {
	   objectPool.put(entity.getPLMIdentifier(), entity);

	   Element elem = new Element("ObjectReference");

	   int myCategory = getCategory(context, entity);
	   switch (myCategory)
	   {
	   case FUNCTION:
		   elem.setAttribute("category", "function");
		   break;
	   case LOGICAL:
		   elem.setAttribute("category", "logical");
		   break;
	   case PHYSICAL:
		   elem.setAttribute("category", "physical");
		   break;
	   case COMMUNICATION:
		   elem.setAttribute("category", "communication");
		   break;
	   case FLOW:
		   elem.setAttribute("category", "flow");
		   break;
	   case TYPE:
		   elem.setAttribute("category", "type");
		   break;
	   case CONNECTION:
		   elem.setAttribute("category", "connection");
		   break;
	   case PORT:
		   elem.setAttribute("category", "port");
		   break;
	   case REPRESENTATION:
		   elem.setAttribute("category", "representation");
		   break;
	   case OTHER:
		   elem.setAttribute("category", "other");
		   break;
	   default:
	   }
	   try{

				String objectId = entity.getPLMIdentifier();
				String model = null;
				String type = entity.getPLMType();
				Hashtable atts = entity.getAttributes();

				String[] mdlInfo = type.split("[/]");
				if (mdlInfo.length == 2) {
					model = mdlInfo[0];
					type = mdlInfo[1];
				}
				elem.setAttribute("id", objectId);
				if (model != null)
					elem.setAttribute("model", model);
				elem.setAttribute("type", type);

			    Map attributes = entity.getAttributes();
			    String name = "" + attributes.get(VPLM_EXTERNAL_ID); //could be com.dassault_systemes.vplm.data.PLMQLNULL
				elem.setAttribute("name", name);

				if (entity instanceof PLMxReferenceEntity) { //FunctionRef, LogicalRef, CummunicationRef(Mux/Demux), FlowRef, TypeRef
					PLMxReferenceEntity ref = (PLMxReferenceEntity)entity;
					ref = coreModeler.getChildrenInstances(ref, false);
					//children
					if(category == FUNCTION || category == LOGICAL || category == PHYSICAL || category == FLOW || category == TYPE)
					{

						PLMxRefInstanceEntity instances[] = ref.getRefInstances();
					    for(int i = 0; instances != null && i < instances.length; i++)
					    {
					    	int childCategory = getCategory(context, instances[i]);

					    	//needs to filter out only functions and logicals, subflows, subtypes, communications!!!
					    	if(!(  childCategory == PORT || childCategory == CONNECTION ||
					    		   (category == FUNCTION || category == LOGICAL) &&
					    	       (childCategory == FLOW || childCategory == TYPE)
					    	  ))
					    	{
						    	Element  rel = new Element("Relationship");
						    	rel.setAttribute("type", "children");
						    	elem.addContent(rel);

						    	//child category may be communication!!!
						    	Element childInstance = buildElement(context, instances[i], childCategory, true, objectPool, implMap);
						    	rel.addContent(childInstance);
					    	}

					    }
					}
				    //ports
					if(category == FUNCTION || category == LOGICAL || category == COMMUNICATION)
					{
					   //PLMxPortEntity[] ports = ref.getPorts(); //doesn't work
					   //FLPort[] flPorts = plmModeler.getFLPorts(flObjId);
					   //PLMxPortEntity portEnt = flPorts[ii].getPortEntity();
						PLMxPortEntity[] ports = null;
						if(category == FUNCTION ||
								category == COMMUNICATION && isFunctionalCommunication(context, ref))
						{
							ports = plmFunctionalModeler.getFunctionalPorts(ref);
						}
						else
						{
							ports = plmLogicalModeler.getLogicalPorts(ref);
						}
					    for(int i = 0; ports != null && i < ports.length; i++)
					    {
						    Element  rel = new Element("Relationship");
						    rel.setAttribute("type", "ports");
						    elem.addContent(rel);

						    //pass in the category for port's parent!!!
					    	Element port = buildElement(context, ports[i], category, true, objectPool, implMap);
					    	rel.addContent(port);
					    }
					}
				    //representations
					if(category == FUNCTION || category == LOGICAL || category == PHYSICAL)
					{
						ref = coreModeler.getChildrenInstances(ref, true);
					   PLMxRepInstanceEntity[]  repInstances = ref.getRepInstances();

					   //For Representation: V_discipline=:
					   //MainView: Schema
					   //png snapshot: Schema_Snapshot
					   //Picture: Picture
					   //State Logic Modeling: Behavior_Discrete
					   //Dynamic Modeling: Behavior_Continuous
					   for(int i = 0; repInstances != null && i < repInstances.length; i++)
					   {
						   String repId = repInstances[i].getPLMIdentifier();
						    Element  rel = new Element("Relationship");
						    rel.setAttribute("type", "representations");
						    elem.addContent(rel);

					    	Element rep = buildElement(context, repId, REPRESENTATION, objectPool, implMap);
					    	rel.addContent(rep);
					   }
					}
				} else if (entity instanceof PLMxRefInstanceEntity) {
					PLMxRefInstanceEntity instance = (PLMxRefInstanceEntity) entity;
					if(recursive)
					{
						// reference
						PLMxReferenceEntity reference = (PLMxReferenceEntity) retrievePLMObject(instance.getRefPLMIdentifier(), objectPool);
						Element rel = new Element("Relationship");
						rel.setAttribute("type", "reference");
						elem.addContent(rel);

						Element ref = buildElement(context, instance.getRefPLMIdentifier(), category, objectPool, implMap);
						rel.addContent(ref);

						if(category == FUNCTION || category == LOGICAL || category == COMMUNICATION || category == PHYSICAL)
						{
							if(category == FUNCTION || category == LOGICAL || category == COMMUNICATION)
							{
								PLMxPortEntity[] ports = null;
								// connections
								if(category == FUNCTION ||
										category == COMMUNICATION && isFunctionalCommunication(context, instance))
								{
									ports = plmFunctionalModeler.getFunctionalPorts(reference);
								}
								else
								{
									ports = plmLogicalModeler.getLogicalPorts(reference);
								}

								for (int j = 0; ports != null && j < ports.length; j++) {
									PLMxEntityDef[][] connected = null;

									if(category == FUNCTION ||
											category == COMMUNICATION && isFunctionalPort(context, ports[j]))
									{
										connected = plmFunctionalModeler.getConnectedObjects(instance, ports[j]);
									}
									else
									{
										connected = plmLogicalModeler.getConnectedObjects(instance, ports[j]);
									}

									if(connected == null) continue;
									PLMxRefInstanceEntity[] oConnectedInstances = (PLMxRefInstanceEntity[]) connected[0];
									PLMxPortEntity[] oConnectedPorts = (PLMxPortEntity[]) connected[1];
									PLMxConnectionEntity[] oConnections = (PLMxConnectionEntity[]) connected[2];

									//source port
									Element conRel = new Element("Relationship");
									conRel.setAttribute("type", "port");
									elem.addContent(conRel);
									Element sourcePort = buildElement(context, ports[j], getCategory(context, instance),
											false, objectPool, implMap);
									conRel.addContent(sourcePort);

									for (int n = 0; oConnectedInstances != null && n < oConnectedInstances.length; n++) {


										//connection
										Element r = new Element("Relationship");
										r.setAttribute("type", "connections");
										sourcePort.addContent(r);
										Element connection = buildElement(context, oConnections[n], CONNECTION, false, objectPool, implMap);
										r.addContent(connection);

										r = new Element("Relationship");
										r.setAttribute("type", "endpoint");
										connection.addContent(r);
										//might be a  FUNCTION, LOGICAL or CUMMUNICATION
										Element  targetInstance = buildElement(context, oConnectedInstances[n], getCategory(context, oConnectedInstances[n]),
												false, objectPool, implMap);
										r.addContent(targetInstance);

										//pass in category of port's parent!!!
										r.addContent(buildElement(context, oConnectedPorts[n], getCategory(context, oConnectedInstances[n]),
												false, objectPool, implMap));

									}
								}
							}
						    //implemented
						   List implemented = null;
						   implemented = (List)implMap.get(objectId);
						   for(int i = 0; implemented != null && i < implemented.size(); i++)
						   {
							    Element  implRel = new Element("Relationship");
							    implRel.setAttribute("type", "implemented");
							    elem.addContent(implRel);

							    String implementedPLMId = (String)implemented.get(i);
							    if(isCBPObject(emxContext, implementedPLMId))
							    {
							    	Element req = buildCBPElement(implementedPLMId);
							    	implRel.addContent(req);
							    }
							    else{
							    	//this got to be a FUNCTION
								    PLMxEntityDef implementedInst = retrievePLMObject(implementedPLMId, objectPool);
							    	Element impl = buildElement(context, implementedInst, getCategory(context, implementedInst), false, objectPool, implMap);
							    	implRel.addContent(impl);
							    }
						   }
						}
					}

					//FLOW
					//TYPE

				} else if (entity instanceof PLMxPortEntity) {

					if(recursive)
					{
						PLMxPortEntity port = (PLMxPortEntity)entity;
						//flow or //type
						//getPortFlow/getPortType is buggy for mux/demux
						if(category == FUNCTION || (category == COMMUNICATION && isFunctionalPort(context, port)))
						{
							   PLMxRefInstanceEntity[] flows = plmFunctionalModeler.getPortFlow(port);
							   if(flows != null && flows.length > 0)
							   {
								    Element  rel = new Element("Relationship");
								    rel.setAttribute("type", "flow");
								    elem.addContent(rel);

							    	Element flow = buildElement(context, flows[flows.length -1], FLOW, objectPool, implMap);
							    	rel.addContent(flow);
							    	if(flows.length > 1)
							    	{
							    		System.err.println("Error: multiple flows for port " + name);
							    	}
							   }
						}
						else{
							   //nullpointer exception?????????
							   PLMxRefInstanceEntity[] types = plmLogicalModeler.getPortType(port);
							   if(types != null && types.length > 0)
							   {
								    Element  rel = new Element("Relationship");
								    rel.setAttribute("type", "type");
								    elem.addContent(rel);

							    	Element logicalType = buildElement(context, types[types.length -1], TYPE, objectPool, implMap);
							    	rel.addContent(logicalType);
							    	if(types.length > 1)
							    	{
							    		System.err.println("Error: multiple types for port " + name);
							    	}
							   }
						}

					}

				} else if (entity instanceof PLMxConnectionEntity) {
					//nothing below
				} else if (entity instanceof PLMxRepresentationEntity) {
					//stream data of different deciplines
					//????
				} else if (entity instanceof PLMxRepInstanceEntity) {
					PLMxRepInstanceEntity repInstance = (PLMxRepInstanceEntity) entity;
					//reference
					String repRefId = repInstance.getRepPLMIdentifier();
				    Element  rel = new Element("Relationship");
				    rel.setAttribute("type", "reference");
				    elem.addContent(rel);

				    //filter out certain decliplines of Representation?
			    	Element repRef = buildElement(context, repRefId, REPRESENTATION, objectPool, implMap);
			    	rel.addContent(repRef);

				}
	   }catch(Exception e)
	   {
		   e.printStackTrace();
	   }
		return elem;
   }

   boolean isFunctionalPort(Context context, PLMxEntityDef entity) throws MatrixException
   {
	   //return entity.getPLMType().indexOf("FunctionalConnector") >= 0;
	   IPLMDictionaryPublicItf dico = new IPLMDictionaryPublicFactory().getDictionary();
	   IPLMDictionaryPublicClassItf iFunctionalType = dico.getClass(context, entity.getPLMType().substring(entity.getPLMType().indexOf("/")+1));
	   return iFunctionalType.isKindOf(context, RFLPLMFUNCTIONAL);
   }
   boolean isFunctionalCommunication(Context context, PLMxEntityDef entity) throws MatrixException
   {
	   IPLMDictionaryPublicItf dico = new IPLMDictionaryPublicFactory().getDictionary();
	   IPLMDictionaryPublicClassItf iFunctionalType = dico.getClass(context, entity.getPLMType().substring(entity.getPLMType().indexOf("/")+1));
	   return iFunctionalType.isKindOf(context, RFLPLMFUNCTIONALCOMMUNICATION);
   }
   int getCategory(Context context, PLMxEntityDef entity) throws MatrixException
   {
	   IPLMDictionaryPublicItf dico = new IPLMDictionaryPublicFactory().getDictionary();
	   IPLMDictionaryPublicClassItf iFunctionalType = dico.getClass(context, entity.getPLMType().substring(entity.getPLMType().indexOf("/")+1));
	   if(entity instanceof PLMxPortEntity)
	   { //RFLPLMFunctionalDS/RFLPLMFunctionalConnectorDS
		 //RFLVPMLogicalDS/RFLVPMLogicalPortDS
		   return PORT;
	   }
	   else if(entity instanceof PLMxConnectionEntity) //(entity.getPLMType().indexOf("Connection") >= 0)
	   { //RFLPLMFunctionalDS/RFLPLMFunctionalConnectionDS
		 //RFLVPMLogicalDS/RFLVPMLogicalConnectionDS
		   return CONNECTION;
	   }
	   else if(entity instanceof PLMxRepresentationEntity || entity instanceof PLMxRepInstanceEntity )
	   { //RFLPLMFunctionalDS/RFLPLMFunctionalRepReferenceDS
		 //RFLPLMFunctionalDS/RFLPLMFunctionalRepInstanceDS
		 //RFLVPMLogicalDS/RFLVPMLogicalRepReferenceDS
		 //RFLVPMLogicalDS/RFLVPMLogicalRepInstanceDS
		   return REPRESENTATION;
	   }
	   else if(iFunctionalType.isKindOf(context, RFLPLMFUNCTIONAL))
	   { //RFLPLMFunctionalDS/RFLPLMFunctionalInstanceDS
		 //RFLPLMFunctionalDS/RFLPLMFunctionalReferenceDS

		   return FUNCTION;
	   }
	   else if(iFunctionalType.isKindOf(context, RFLVPMLOGICAL))
	   { //RFLVPMLogicalDS/RFLVPMLogicalReferenceDS
		 //RFLVPMLogicalDS/RFLVPMLogicalInstanceDS
		   return LOGICAL;
	   }
	   else if(iFunctionalType.isKindOf(context, RFLVPMPHYSICAL))
	   { //PLMProductDS/PLMProductDS
		   return PHYSICAL;
	   }
	   else if(iFunctionalType.isKindOf(context, RFLPLMFUNCTIONALCOMMUNICATION) ||
			   iFunctionalType.isKindOf(context, RFLPLMLOGICALCOMMUNICATION))
	   { //RFLPLMFunctionalCommunicationDS/RFLPLMFunctionalCommunicationInstanceDS
		 //RFLVPMLogicalCommunicationDS/RFLVPMLogicalCommunicationInstanceDS
		   return COMMUNICATION;
	   }
	   else if(iFunctionalType.isKindOf(context, RFLPLMFLOW))
	   { //RFLPLMFlowDS/RFLPLMFlowInstanceDS
		 //RFLPLMFlowDS/RFLPLMFlowReferenceDS

		   return FLOW;
	   }
	   else if(iFunctionalType.isKindOf(context, RFLPLMTYPE))
	   { //RFLVPMSystemTypeDS/RFLVPMSystemTypeReferenceDS
		 //RFLVPMSystemTypeDS/RFLVPMSystemTypeInstanceDS
		   return TYPE;
	   }
	   else{
		   return OTHER;
	   }
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
}
