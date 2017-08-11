/*
 * emxEngineeringConfiguredPartMigrationBase.java
 * program migrates Existing PUEECO and its appicability attribute to NE object and set the expression
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import matrix.db.*;
import matrix.util.*;  

import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;

import com.matrixone.jdom.Element;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.input.SAXBuilder;
import com.matrixone.apps.unresolvedebom.UnresolvedEBOMConstants;
import com.matrixone.apps.effectivitymigration.EffectivityFramework;


  public class emxEngineeringConfiguredPartMigrationBase_mxJPO  extends emxCommonMigration_mxJPO {
      private static final String SYMB_COMMA = ",";
      
	  StringList _PENDING_OPERATION_ChangeID_CUT_LIST = new StringList();
	  StringList _PENDING_OPERATION_ChangeID_ADD_LIST = new StringList();
	  public static final String OPERARTION_ADD		=	"Add";
	  public static final String OPERARTION_CUT		=	"Cut";

      /**
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds no arguments
      * @throws Exception if the operation fails
      * @grade 0
      */
      public emxEngineeringConfiguredPartMigrationBase_mxJPO (Context context, String[] args)
              throws Exception
      {
       super(context, args);
      }
      
      /**
       * @param xmlString
       * @param xPathExpr
       * @return
       * @throws Exception
       */
      public static Document getXMLDoc(String xmlString) throws Exception {

          if(!"".equals(xmlString)) {
              SAXBuilder builder = new SAXBuilder();
  			builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
  			builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
  			builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
              Document doc = builder.build(new StringReader(xmlString));
              return doc;
          }
          return null;
      }



      /**
       * This method does the migration work.  Existing PUEECO and its 
       * applicability attribute will migrated to NE object and the expression
       * will be set for 2011x env.
       * @param context the eMatrix <code>Context</code> object
       * @param objectIdList StringList holds list objectids to migrate
       * @returns nothing
       * @throws Exception if the operation fails
       */
      public void  migrateObjects(Context context, StringList objectIdList) throws Exception {
    	  
          // if scan variable is set, then do not migrate data
          // just write the problamatic ids to log else proceed with migration
    	  
          if (scan) {
              return;
          }
          
          int listSize = (objectIdList == null) ? 0 : objectIdList.size();
          
          if (listSize > 0) {
        	  
        	  String ATTRIBUTE_APPLICABILITY_TRANS = PropertyUtil.getSchemaProperty(context, "attribute_ApplicabilityTransaction");      
              String SELECT_ATTRIBUTE_APPLICABILITY_TRANS = "attribute[" + ATTRIBUTE_APPLICABILITY_TRANS + "].value";
              
              String strRelEffectivityContext = PropertyUtil.getSchemaProperty(context, "relationship_EffectivityContext");
    		  String strTypeProducts = PropertyUtil.getSchemaProperty(context, "type_Products");
        	  
        	 ContextUtil.startTransaction(context, true);
        	 
        	 MqlUtil.mqlCommand(context, "mod rel $1 to add type $2",
								strRelEffectivityContext, strTypeProducts);
        	  
	         try {
	        	  MapList listEBOMAndEBOMPendingData;
	        	  MapList allEBOMRelIds = new MapList();
	        	  MapList allEBOMOrEBOMPendingRelIds = new MapList();
	        	  
	        	  EffectivityFramework effectivityFramework = new EffectivityFramework();
	        	  
	        	  DomainObject domObj;
	        	  
	        	  String relationship = UnresolvedEBOMConstants.RELATIONSHIP_EBOM + "," + UnresolvedEBOMConstants.RELATIONSHIP_EBOM_PENDING;
	        	  String objectId;
	        	  
	        	  StringList relSelect = new StringList(3);
	        	  relSelect.add(DomainConstants.SELECT_RELATIONSHIP_ID);
	        	  relSelect.add(DomainConstants.SELECT_RELATIONSHIP_NAME);
	        	  relSelect.add(SELECT_ATTRIBUTE_APPLICABILITY_TRANS);
	        	  
	        	  ConfiguredPartMigration confPartMig;
	        	  Iterator iterator;
	        	  
	        	  HashSet hsetobjectIdList= new HashSet(listSize);
	        	  
	              for (int i = 0; i < listSize; i++) {
	            	  objectId = (String) objectIdList.get(i);
	            	  
	            	  if (hsetobjectIdList.add(objectId)) {
	            	  
		            	  domObj = DomainObject.newInstance(context, objectId);
		            	  
		            	  listEBOMAndEBOMPendingData = domObj.getRelatedObjects(context, relationship, DomainConstants.QUERY_WILDCARD, null, 
		            			  												relSelect, false, true, (short) 1, null, null, null, null, null);
		            	  	            	  	            	  
		            	  iterator = listEBOMAndEBOMPendingData.iterator();
		            	  
		            	  while (iterator.hasNext()) {
		            		  confPartMig = new ConfiguredPartMigration((Map) iterator.next(), SELECT_ATTRIBUTE_APPLICABILITY_TRANS);
		            		  
		            		  confPartMig.setPendingAndCurrentOperations();
		            		  
		            		  //confPartMig.reportToUserIfMoreThanOnePendingOperation();
		            		  
		            		  confPartMig.callCFFConnectNamedEffectivityUsage(context, effectivityFramework);
		            		  
		            		  confPartMig.addEBOMRelId(allEBOMRelIds);
		            		  
		            		  confPartMig.addEBOMOrEBOMPendingRelId(allEBOMOrEBOMPendingRelIds);
		            		  
		            		  loadMigratedOids(objectId);
		            	  }
	            	  }
	              }

	              
	              if (allEBOMRelIds.size() > 0) {
	            	  effectivityFramework.refreshCurrentRelEffectivity(context, allEBOMRelIds);
	              }
	              
	              if (allEBOMOrEBOMPendingRelIds.size() > 0) {
				  
	            	  effectivityFramework.setChangeId("");

	            	  for(int i=0;i<allEBOMOrEBOMPendingRelIds.size();i++){
	            		  EffectivityFramework.setRelEffectivityTypes(context,(String) ((Map) allEBOMOrEBOMPendingRelIds.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID),"Unit");
	            	  }
	            	  effectivityFramework.refreshProposedEffectivity(context, allEBOMOrEBOMPendingRelIds);
	              }	
	              
	              	              
	              
	              ContextUtil.commitTransaction(context);
	          } catch (Exception ex) {
	        	  ContextUtil.abortTransaction(context);
	              ex.printStackTrace();
	              throw ex;
	          } finally {
					MqlUtil.mqlCommand(context, "mod rel $1 to remove type $2",
										strRelEffectivityContext, strTypeProducts);					
	          }	          
          }
      }

      class ConfiguredPartMigration {
    	  String relId;
    	  String relName;    	  
    	  String applicabilityDetailsTransXML;    	  
    	  
    	  private Document doc;
    	  private Element root;
    	  
    	 
    	  private StringList _CURRENT_OPERATION_ChangeID_LIST = new StringList();
    	  private StringList _PENDING_OPERATION_ChangeID_CUT_LIST = new StringList();
    	  private StringList _PENDING_OPERATION_ChangeID_ADD_LIST = new StringList();
    	  private StringList _PENDING_OPERATION_ChangeID_LIST = new StringList();

    	  
    	  ConfiguredPartMigration(Map map, String SELECT_ATTRIBUTE_APPLICABILITY_TRANS) throws Exception {
    		  this.relId = (String) map.get(DomainConstants.SELECT_RELATIONSHIP_ID);
    		  this.relName = (String) map.get(DomainConstants.SELECT_RELATIONSHIP_NAME);    		  
    		  this.applicabilityDetailsTransXML = (String) map.get(SELECT_ATTRIBUTE_APPLICABILITY_TRANS);
    	  }
    	  
    	  public void setPendingAndCurrentOperations() throws Exception {
    		  if (isNotNullAndNotEmpty(this.applicabilityDetailsTransXML)) {
    			  this.doc = getXMLDoc(this.applicabilityDetailsTransXML);
    	          this.root = doc.getRootElement();
    	          this._CURRENT_OPERATION_ChangeID_LIST = getDataListForThisXML(this.root, "Current");
    	          this._PENDING_OPERATION_ChangeID_ADD_LIST = getPUEECOIdList(this.root, "Pending","ECO_ADD"); 
    	          this._PENDING_OPERATION_ChangeID_CUT_LIST = getPUEECOIdList(this.root, "Pending","ECO_CUT");
    	          this._PENDING_OPERATION_ChangeID_LIST = getDataListForThisXML(this.root, "Pending");
    		  }		  
    	  }
    	  
    	  public void callCFFConnectNamedEffectivityUsage(Context context, EffectivityFramework effectivityFramework) throws Exception {
    		  StringList changeIdList = new StringList();
    		  
    		  changeIdList.addAll(this._CURRENT_OPERATION_ChangeID_LIST);
    		  changeIdList.addAll(this._PENDING_OPERATION_ChangeID_LIST);
    		  
    		  
    		  if (changeIdList.size() > 0) {    			  
    			  effectivityFramework.connectNamedEffectivityUsage(context, this.relId, changeIdList);
    		  }  
    		  
    		  String[] strRelIds = {this.relId};
    		  
              if (_PENDING_OPERATION_ChangeID_ADD_LIST.size() > 0) {
            	  effectivityFramework.updateRelProposedExpression(context, strRelIds, (String) _PENDING_OPERATION_ChangeID_ADD_LIST.get(0), OPERARTION_ADD);
              }
              if (_PENDING_OPERATION_ChangeID_CUT_LIST.size() > 0) {
            	  for(int count=0;count<_PENDING_OPERATION_ChangeID_CUT_LIST.size();count++){
            		  effectivityFramework.updateRelProposedExpression(context, strRelIds, (String) _PENDING_OPERATION_ChangeID_CUT_LIST.get(count), OPERARTION_CUT);
            	  }
              }
    	  }
    	  
    	/* public void reportToUserIfMoreThanOnePendingOperation() {
    		  
    		  if (this._PENDING_OPERATION_ChangeID_LIST.size() > 1) {
    			  Element elePedingOrCurrent = root.getChild("Pending");
    	          
    	          if (elePedingOrCurrent != null) {
    		          List childern = elePedingOrCurrent.getChildren();		          
    		          
    		          if (childern != null) {
    		        	  Element elementAddCut;
    			          Iterator itr = childern.iterator();
    			          String temp;
    			          String changeType;
    			          String changeName;
    			          String changeRev;
    			          String applicability;
    			          int i = 0;
    			          
    			          while (itr.hasNext()) {
    			        	  
    			        	  if (i == 0) {
    			        		  i = 1;
    			        		  continue;
    			        	  }
    			        	  
    			              elementAddCut = (Element) itr.next();
    			              changeType = elementAddCut.getChild("ECO").getAttributeValue("TYPE");
    			              changeName = elementAddCut.getChild("ECO").getAttributeValue("NAME");
    			              changeRev = elementAddCut.getChild("ECO").getAttributeValue("REVISION");
    			              applicability = elementAddCut.getChild("APPLICABILITY").getText();
    			              temp = changeType + SYMB_COMMA + changeName + SYMB_COMMA + changeRev + SYMB_COMMA + applicability;
    			              
    			              
    			          }
    		          }
    	          }
    		  }
    	  }*/
    	  
    	  public void addEBOMRelId(MapList addEBOMRelId) {
    		  if (this.relName.equals(UnresolvedEBOMConstants.RELATIONSHIP_EBOM)) {
    			  addEBOMOrEBOMPendingRelId(addEBOMRelId);
    		  }
    	  }
    	  
    	  public void addEBOMOrEBOMPendingRelId(MapList addEBOMOrEBOMPendingRelId) {
			  Map map = new HashMap(1);
			  map.put(DomainConstants.SELECT_RELATIONSHIP_ID, this.relId);
			  addEBOMOrEBOMPendingRelId.add(map);
    	  }
      }
      
      public StringList getDataListForThisXML(Element root, String pedingOrCurrent) throws Exception {
    	  
          StringList listReturn = new StringList();

          Element elePedingOrCurrent = root.getChild(pedingOrCurrent);
          
          if (elePedingOrCurrent != null) {
	          List childern = elePedingOrCurrent.getChildren();		          
	          
	          if (childern != null) {
	        	  Element elementAddCut;
		          Iterator itr = childern.iterator();
		          String ecoId;
		          
		          while (itr.hasNext()) {
		              elementAddCut = (Element) itr.next();		              
		              ecoId = elementAddCut.getChild("ECO").getText();		              
		              
		              if (isNotNullAndNotEmpty(ecoId)) {
		            	  listReturn.add(ecoId.trim());
		              }
		          }
	          }
          }
          
          return listReturn;
      }
      

      
      public StringList getPUEECOIdList(Element root, String pedingOrCurrent , String addorcut) throws Exception {
    	  
          StringList listReturn = new StringList();

          Element elePedingOrCurrent = root.getChild(pedingOrCurrent);
          if (elePedingOrCurrent != null) {
	          List childern = elePedingOrCurrent.getChildren(addorcut);	          
	          
	          if (childern != null) {
	        	  Element elementAddCut;
		          Iterator itr = childern.iterator();
		          String ecoId;

		          
		          while (itr.hasNext()) {
		              elementAddCut = (Element) itr.next();		              
		              ecoId = elementAddCut.getChild("ECO").getText();	
		              if (isNotNullAndNotEmpty(ecoId)) {
		            	  listReturn.add(ecoId.trim());
		              }
		          }
	          }
          }
          return listReturn;
      }
      
      
      
      
      public void  writeToFile(StringList pendingOperation)throws Exception {
    	  
    	  emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter = null;
          if (emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter == null)
          {
              try
              {
            	  emxEngineeringConfiguredPartMigrationBase_mxJPO.documentDirectory = documentDirectory;
            	  emxEngineeringConfiguredPartMigrationBase_mxJPO._oidsFile = new java.io.File(documentDirectory + "PendingECO.csv");
            	  emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter = new BufferedWriter(new FileWriter(emxEngineeringConfiguredPartMigrationBase_mxJPO._oidsFile));                  
              }
              catch(FileNotFoundException eee)
              {
                  throw eee;
              }
          }
          String sb="Type" + SYMB_COMMA + "Name" + SYMB_COMMA + "Revision" + SYMB_COMMA + "Applicability";
          
          for(int count = 0; count < pendingOperation.size(); count++) {
        	  if (count==0) {
        		  emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter.write(sb);
                  emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter.newLine();
        	  }
        	  emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter.write((String)pendingOperation.get(count));
        	  emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter.newLine();
          }
		  
		  if (emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter != null) {
			  emxEngineeringConfiguredPartMigrationBase_mxJPO._fileWriter.close();
		  }
      }
      
      
      private boolean isNotNullAndNotEmpty(String data) {
    	  return ((data == null || "null".equals(data)) ? 0 : data.trim().length()) > 0;
      }

    public void help(Context context, String[] args) throws Exception
    {
        if(!context.isConnected())
        {
            throw new Exception("not supported on desktop client");
        }

        writer.write("================================================================================================\n");
        writer.write(" ECO Migration is a two step process  \n");
        writer.write(" Step1: Find Configured Part objects with policy Configured Part and from EBOM/EBOM Pending relationship is true and write them into flat files \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringConfiguredPartMigrationFindObjects 1000 Part C:/Temp/oids/; \n");
        writer.write(" First parameter  = indicates number of object per file \n");
        writer.write(" Second Parameter = the parent type to searh for \n");
        writer.write(" Third Parameter  = the directory where files should be written \n");
        writer.write(" \n");
        writer.write(" Step2: Migrate the objects \n");
        writer.write(" Example: \n");
        writer.write(" execute program emxEngineeringConfiguredPartMigration 'C:/Temp/oids/' 1 n ; \n");
        writer.write(" First parameter  = the directory to read the files from\n");
        writer.write(" Second Parameter = minimum range of file to start migrating  \n");
        writer.write(" Third Parameter  = maximum range of file to end migrating  \n");
        writer.write("        - value of 'n' means all the files starting from mimimum range\n");
        writer.write("================================================================================================\n");
        writer.close();
    }
}
