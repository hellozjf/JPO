/*
 ** WhereUsedBase
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 */





import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.util.StringList;


import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jdom.Attribute;
import com.matrixone.jdom.Document;
import com.matrixone.jdom.Element;
import com.matrixone.jdom.input.SAXBuilder;
import com.matrixone.jdom.xpath.XPath;


/**
 * This JPO class has some methods pertaining to Product Line type.
 * @author Enovia MatrixOne
 * @version WhereUsed R212
 */


public class WhereUsedBase_mxJPO extends emxDomainObject_mxJPO {

	private static String DEFAULT_FILE_DEFINITION = "WhereUsedDefinition";

	private static String XML_TAG_APPLICATION = "application";
	private static String XML_TAG_FILTER = "filter";
	private static String XML_TAG_LABEL = "label";
	private static String XML_TAG_METHOD = "method";
	private static String XML_TAG_PROGRAM = "program";
	private static String XML_TAG_WHERE_USED = "whereUsed";
	private static String XML_TAG_TOOLBAR = "ToolbarMenu";

	private static String XML_TAG_PROGRAM_JPO = "JPO";
	private static String XML_TAG_PROGRAM_MQL = "MQL";
	private static String RANGE_VALUE_ALL="All";
	
	private static String XML_TAG_SELECT="select";
	private static String XML_TAG_EXPANDRELATED="expandRel";
	private static String XML_TAG_HIERARCHICAL="Hierarchical_Data";
	private static String XML_TAG_RELATED="Related_Data";
	private  Map<String,String> LabelMethodMap=new HashMap<String,String>();
	private static Map<String,StringList> ToolbarMap=new HashMap<String,StringList>();
	private static String XML_TAG_EXPANDTYPES = "fromTypes";
	private static Map whereUsedXMLMap = null;
	private List typeList =null;
	private Set<String> typeset=new HashSet<String>();
	private Set <String>relset=new HashSet<String>();
	
	
	

	public WhereUsedBase_mxJPO (Context context, String[] args) throws Exception
	{
		super(context, args);
		if(whereUsedXMLMap ==null){
			//whereUsedXmlmap,LabelMethodMap,ToolbarMap 
			//which are used to retrive hierachical data will be initialised one time
			loadWhereUsed(context);
			
		}
	}

	/**
	 * Main entry point.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return an integer status code (0 = success)
	 * @throws Exception if operation fails
	 * @since WhereUsed R212
	 * @grade 0
	 */
	public int mxMain (Context context, String[] args) throws Exception {
		if (!context.isConnected()) {
			String strContentLabel="";
			throw  new Exception(strContentLabel);
		}
		return  0;
	}

 	/**
	 *  Builds a maplist from the XML WhereUsed Definition for each type
	 *
	 * @param context - the eMatrix Context object
	 * @param objectId - the Id of the context object
	 * @param xmlFileDefinition - the name of the XML Definition program
	 * @return MapList - containing the XML WhereUsed Definition
	 * @throws Exception if the operation fails
	 * @since R212
	 */
	private MapList getWhereUsedXMLforRelatedData(Context context, String objectId,String xmlFileDefinition)throws Exception{
		MapList returnMapList = new MapList();
		try{
			String sLanguage = context.getSession().getLanguage();

			if(objectId!=null && !objectId.isEmpty()){
				MQLCommand mql = new MQLCommand();
				if(xmlFileDefinition!=null && !xmlFileDefinition.isEmpty()){
					mql.executeCommand(context, "print page $1 select content dump",xmlFileDefinition);
				}else{
					mql.executeCommand(context, "print page $1 select content dump",DEFAULT_FILE_DEFINITION);
				}

				if(mql.getResult()!=null && !mql.getResult().isEmpty()){
					SAXBuilder builder = new SAXBuilder();
					Document document = builder.build(new StringReader(mql.getResult()));

					Element root = document.getRootElement();

					DomainObject domObject = new DomainObject(objectId);
					String derivedType = domObject.getInfo(context, DomainConstants.SELECT_TYPE);

					//Construct the list of type and its derived
					StringList derivedTypes = new StringList();
					while(derivedType!=null && !derivedType.isEmpty()){
						derivedTypes.add(derivedType);
						derivedTypes.add(FrameworkUtil.getAliasForAdmin(context, SELECT_TYPE, derivedType, false));
						derivedType = MqlUtil.mqlCommand(context, "print type $1 select derived dump",derivedType);
					}

					//Iterator derivedTypesItr = derivedTypes.iterator();
					
					///iterates through each type_xxx  and getting all elements and its values in the xml
					String whereUsedLabel="";
					String whereUsedApplication="";
					for (Iterator iterator = derivedTypes.iterator(); iterator
							.hasNext();) {
						String derivedTypesItrValue = (String) iterator.next();				
						Element objectTypeDefinition = root.getChild(derivedTypesItrValue);
						if(objectTypeDefinition!=null && !objectTypeDefinition.equals("")){
							Element dataTypeDefinition =objectTypeDefinition.getChild(XML_TAG_RELATED);
							if(dataTypeDefinition!=null && !dataTypeDefinition.equals("")){
								List whereUsedList = dataTypeDefinition.getChildren(XML_TAG_WHERE_USED);
								Iterator whereUsedListItr = whereUsedList.iterator();
								while(whereUsedListItr.hasNext()){
									Element whereUsed = (Element)whereUsedListItr.next();
									 whereUsedLabel = whereUsed.getAttributeValue(XML_TAG_LABEL);								
									 whereUsedApplication = whereUsed.getAttributeValue(XML_TAG_APPLICATION);								
									whereUsedLabel = i18nNow.getI18nString(whereUsedLabel, UINavigatorUtil.getStringResourceFileId(context,whereUsedApplication), sLanguage);
	
											Map returnMap = new HashMap();
											returnMap.put(XML_TAG_LABEL, whereUsedLabel);											
											returnMap.put(XML_TAG_SELECT, whereUsed.getAttributeValue(XML_TAG_SELECT));
											returnMap.put(XML_TAG_APPLICATION, whereUsedApplication);									
											
											returnMapList.add(returnMap);
										
								}//End of while
							}
						}
					}
				}else{
					//Error with XML File Definition
					throw new FrameworkException(i18nNow.getI18nString("WhereUsed.XML.Error", "whereusedStringResource", sLanguage));
					
				}
			}
		}catch(Exception exception){
			
			throw new FrameworkException(exception);
		}
		return returnMapList;
	}	
 	
	/**
	 * gets where used Objects
	 *
	 * @param context - the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId and dataType which are required for further processing.
	 * @return MapList - containing the WhereUsed objects Id
	 * @throws Exception if the operation fails
	 * @since R212
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable 
	public MapList getWhereUsed(Context context, String[] args) throws Exception{
		MapList returnMapList = new MapList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			
			
			String datatype=(String)programMap.get("dataType");			
			
			if (datatype.equalsIgnoreCase("related")){		

				returnMapList= getWhereUsedRelated( context,  args);
					
			}
			else if (datatype.equalsIgnoreCase("hierarchy")){
				//loadWhereUsed(context);
				returnMapList= getWhereUsedHierarchial( context,  args);
					
			}
						
			
			
  
					
		}catch(Exception exception){
			
			throw new FrameworkException(exception);
		}
		return returnMapList;
	}
	
	/** get Hierarchial data from the database
	 * @param context
	 * @param args - contains objectid and level
	 * @return maplist= contain all typpe ,nname ,revision, label name,
	 * @throws Exception
	 */
	private MapList getWhereUsedRelated(Context context, String[] args) throws Exception{
		MapList returnMapList = new MapList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);

			String objectId = (String)programMap.get("objectId");
			StringList whereUsedMethodList = new StringList(1);

			
			String level = (String)programMap.get("level");
			if(!(level!=null && !level.isEmpty())){
				level = "0";
			}
	

			if(objectId!=null && !objectId.isEmpty()){
				String noticeMessage = i18nNow.getI18nString("emxFramework.NoDefinitionFound", "emxFrameworkStringResource", context.getSession().getLanguage());

				MapList whereUsedDefinitions = getWhereUsedXMLforRelatedData(context, objectId,"");
				if(whereUsedDefinitions!=null && !whereUsedDefinitions.isEmpty()){
					Iterator whereUsedDefinitionsItr = whereUsedDefinitions.iterator();
					while(whereUsedDefinitionsItr.hasNext()){
						Map whereUsedDefinition = (Map)whereUsedDefinitionsItr.next();
						if(whereUsedDefinition!=null && !whereUsedDefinition.isEmpty()){						
							String whereUsedMethod = (String)whereUsedDefinition.get(XML_TAG_SELECT	);
							//Use the string ressource
							String whereUsedLabel = (String)whereUsedDefinition.get(XML_TAG_LABEL);
							String whereUsedApplication = (String)whereUsedDefinition.get(XML_TAG_APPLICATION);
							
							if((whereUsedMethod!=null && !whereUsedMethod.isEmpty())){
								MapList whereUsedResults = new MapList();
								if(whereUsedApplication!=null && !whereUsedApplication.isEmpty()){											
											LabelMethodMap.put(whereUsedMethod, whereUsedLabel);
											whereUsedMethodList.add(whereUsedMethod);
											if(!MULTI_VALUE_LIST.contains(whereUsedMethod)){
												MULTI_VALUE_LIST.add(whereUsedMethod);
											}																				
								}							
							}
						}
					}//End of while
				}
			}
			//getting where used results			
			HashMap whereUsedMQLMap = new HashMap();
			whereUsedMQLMap.put("objectId", objectId);
			whereUsedMQLMap.put("level", level);
			whereUsedMQLMap.put(XML_TAG_METHOD, whereUsedMethodList);			
			String[] whereUsedMQLArgs =JPO.packArgs(whereUsedMQLMap);

			returnMapList.addAll(getWhereUsedMQL(context, whereUsedMQLArgs));
  
			//clean up MULTI_VALUE_LIST
			for (int i = 0; i < whereUsedMethodList.size(); i++) {
				String mqlSelect =(String)whereUsedMethodList.get(i); 
				if (MULTI_VALUE_LIST.contains(mqlSelect)){
					MULTI_VALUE_LIST.remove(mqlSelect);
					
				}
				
			}			
			
		}catch(Exception exception){
			
			throw new FrameworkException(exception);
		}
		return returnMapList;
	}
	
	/** get Hierarchial data from the database
	 * @param context
	 * @param args - contains objectid and level
	 * @return maplist= contain all typpe ,nname ,revision, label name,
	 * @throws Exception
	 */
	private MapList getWhereUsedHierarchial(Context context, String[] args) throws Exception{
		MapList returnMapList = new MapList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);

			String objectId = (String)programMap.get("objectId");
			
			//StringBuffer strRelPattern = new StringBuffer("Products");
			StringBuffer strRelPattern = new StringBuffer("");
			StringList slObjSelects = new StringList(DomainConstants.SELECT_TYPE);
			slObjSelects.add(DomainConstants.SELECT_NAME);
			slObjSelects.add(DomainConstants.SELECT_REVISION);
			slObjSelects.add(DomainConstants.SELECT_ID);
			
			StringList slRelSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_NAME);
			slRelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
			
			
		if(objectId!=null && !objectId.isEmpty()){
				String noticeMessage = i18nNow.getI18nString("emxFramework.NoDefinitionFound", "emxFrameworkStringResource", context.getSession().getLanguage());

				
				DomainObject domContextBus = new DomainObject(objectId);				
				StringList derivedTypeList = getDerivedTypes(context,domContextBus);
				for (Iterator iterator = derivedTypeList.iterator(); iterator
						.hasNext();) {
					Object strType = (String) iterator.next();
					if(whereUsedXMLMap.get(strType)!=null){
						Set whereusedSet = (Set)whereUsedXMLMap.get(strType);
							for (Iterator iterator1 = whereusedSet.iterator(); iterator1.hasNext();) {
								strRelPattern.append(PropertyUtil.getSchemaProperty(context,(String) iterator1.next()));
								if (iterator1.hasNext()){
									strRelPattern.append(",");
								}
								
							}
					}
					if (iterator.hasNext()){
						strRelPattern.append(",");
					}
				}
				// if there is no relationship given, we should not traverse
			if(UIUtil.isNotNullAndNotEmpty(strRelPattern.toString())){							
				int level = getLevelfromSB(context,args);
				//DomainObject domContextBus = new DomainObject(objectId);
				MapList tempMapList = domContextBus.getRelatedObjects(context,strRelPattern.toString(),   //rel pattern
																		DomainConstants.QUERY_WILDCARD,//object pattern
																		slObjSelects, //object select
																		slRelSelects,//rel select
																		true,        //get To relationships
																		false,      //get from relationships
																		(short)level,   //expand level
																		"", 		//object where clause
																		"",			//rel where clause
																		(short)0, //limit
																		DomainObject.CHECK_HIDDEN, 
																		DomainObject.PREVENT_DUPLICATES,
																		(short) DomainObject.PAGE_SIZE, null, null,
																		null, DomainObject.EMPTY_STRING, "", DomainObject.FILTER_STR_AND_ITEM);
				
				if(level == -1){
					StringList objList = new StringList(1);
					for (Iterator iterator = tempMapList.iterator(); iterator
							.hasNext();) {
						Map objectMap = (Map) iterator.next();
						if(!objList.contains((String)objectMap.get(DomainConstants.SELECT_ID))){
							objList.add((String)objectMap.get(DomainConstants.SELECT_ID));
							returnMapList.add(objectMap);
						}
						
					}
					
				}else{
					returnMapList = tempMapList;
				}
				if (returnMapList != null) {
					addLabels(context,returnMapList);
					HashMap hmTemp = new HashMap();
					hmTemp.put("expandMultiLevelsJPO", "true");
					returnMapList.add(hmTemp);
				}
			}	
			}
			
  
			
		}catch(Exception exception){
			
			throw new FrameworkException(exception);
		}
		return returnMapList;
	}
	
	/** add label values to teh return maplist, which will be used to retrieve values for LABEL column
	 * @param context
	 * @param returnMapList
	 * @throws Exception
	 */
	private void addLabels(Context context ,MapList returnMapList)throws Exception{
		try{
			String relName ="";
			for (Iterator iterator = returnMapList.iterator(); iterator.hasNext();) {
				Map objectMap = (Map) iterator.next();
				relName= FrameworkUtil.getAliasForAdmin(context,"relationship",(String)objectMap.get(DomainConstants.SELECT_RELATIONSHIP_NAME), true);
				if(LabelMethodMap.get(relName)!=null){
					objectMap.put(XML_TAG_LABEL, LabelMethodMap.get(relName));
				}
			}
		
		
		}catch(Exception exception){
			
			throw new FrameworkException(exception);
		}
	} 
	
	/**
	 * process the results that got form JPO and send out in required format
	 *
	 * @param context - the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return MapList - containing the WhereUsed objects Id
	 * @throws Exception if the operation fails
	 * @since R212
	 */
   private MapList processResults(Context context,MapList whereUsedResults,String level, String whereUsedLabel)throws Exception{
	   MapList returnMapList = new MapList();
	   try{
		   if(whereUsedResults!=null && !whereUsedResults.isEmpty()){
				Iterator whereUsedResultsItr = whereUsedResults.iterator();
				while(whereUsedResultsItr.hasNext()){
					Map whereUsedResult = (Map)whereUsedResultsItr.next();
					if(whereUsedResult!=null && !whereUsedResult.isEmpty()){
						String whereUsedResultId = (String)whereUsedResult.get(DomainConstants.SELECT_ID);
												
							Map returnMap = new HashMap();							
							returnMap.put(DomainConstants.SELECT_ID, whereUsedResultId);
							returnMap.put(DomainConstants.SELECT_LEVEL, level);
							returnMap.put(XML_TAG_LABEL, whereUsedLabel);							
							returnMapList.add(returnMap);
										
							
						
					}
				}//End of while
			}
		   
	   }catch(Exception exception){
			
			throw new FrameworkException(exception);
		}
	  return returnMapList;
   }
   /**
	 * gets where used Objects by query database 
	 *
	 * @param context - the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return MapList - containing the WhereUsed objects Id
	 * @throws Exception if the operation fails
	 * @since R212
	 * @exclude
	 */   
	public MapList getWhereUsedMQL(Context context, String[] args) throws Exception{
		MapList returnMapList = new MapList();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);

			String objectId = (String)programMap.get("objectId");
			String level = (String)programMap.get("level");
			StringList whereUsedMethodList = (StringList)programMap.get(XML_TAG_METHOD);

			if((objectId!=null && !objectId.isEmpty()) && (whereUsedMethodList!=null && !whereUsedMethodList.isEmpty())){
				String[] oidsArray = new String[1];
				oidsArray[0]=objectId;
				MapList connectionIds = DomainObject.getInfo(context,oidsArray,whereUsedMethodList);
				Iterator connectionIdsItr = connectionIds.iterator();
				while(connectionIdsItr.hasNext()){
					Map obMap = (Map) connectionIdsItr.next();
					
					Collection mapkey = obMap.keySet();
					for (Iterator iterator = mapkey.iterator(); iterator.hasNext();) {	
							String strkey=	(String)iterator.next();
							if (obMap.get(strkey) != null){
								if (obMap.get(strkey) instanceof StringList){
									StringList connectionIdlst = (StringList)obMap.get(strkey);
									for (Iterator iterator2 = connectionIdlst.iterator(); iterator2.hasNext();) {
										Map returnMap = new HashMap();
										String connectionId = (String) iterator2.next();
										if(connectionId.contains("=")){

											returnMap.put(DomainConstants.SELECT_ID,(connectionId.substring(connectionId.indexOf('=')+1)).trim());
											
										}else{
											returnMap.put(DomainConstants.SELECT_ID, connectionId);
										}
										returnMap.put(DomainConstants.SELECT_LEVEL, level);
										returnMap.put(XML_TAG_LABEL, LabelMethodMap.get(strkey));
										returnMapList.add(returnMap);
									}
								
								}else{
									Map returnMap = new HashMap();
									String connectionId = (String)obMap.get(strkey);
									if(connectionId.contains("=")){

										returnMap.put(DomainConstants.SELECT_ID,(connectionId.substring(connectionId.indexOf('=')+1)).trim());
										
									}else{
										returnMap.put(DomainConstants.SELECT_ID, connectionId);
									}
									returnMap.put(DomainConstants.SELECT_LEVEL, level);
									returnMap.put(XML_TAG_LABEL, LabelMethodMap.get(strkey));
									returnMapList.add(returnMap);
								}
							}
						
						
					} //for						

				}//End of while				
		
			}
		}catch(Exception exception){
			
			throw new FrameworkException(exception);
		}
		return returnMapList;
	}
	 /**
	 * gets the Column values by reading the programMap
	 *
	 * @param context - the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Vector - containing level info
	 * @throws Exception if the operation fails
	 * @since R212
	 * @exclude
	 */   
	public static Vector getLevel(Context context,String[] args) throws Exception{
		Vector returnVector = new Vector();
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			List objectList = (MapList)programMap.get("objectList");
			Iterator objectListItr = objectList.iterator();
			String strLevel = "";

			Map objectMap = new HashMap();
			//loop through all the records
			while(objectListItr.hasNext()){
				objectMap = (Map) objectListItr.next();
				String tempLevel = (String)objectMap.get(DomainConstants.SELECT_LEVEL);
				Integer levelInt = (0 - Integer.parseInt(tempLevel))-1;
				strLevel = levelInt.toString();
				returnVector.add(strLevel);
			} //End of while loop
		}catch(Exception e){
			throw new FrameworkException(e);
		}
		return returnVector;
	}
	 /**
	 * gets the Column values by reading the programMap
	 *
	 * @param context - the eMatrix Context object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return Vector - containing the label value
	 * @throws Exception if the operation fails
	 * @since R212
	 * @exclude
	 */   
	public Vector getLabel(Context context,String[] args) throws Exception{
		Vector returnVector = new Vector();
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			List objectList = (MapList)programMap.get("objectList");

			Iterator objectListItr = objectList.iterator();

			while(objectListItr.hasNext()){
				Map objectMap = (Map) objectListItr.next();
				if(objectMap!=null && !objectMap.isEmpty()){
					String whereUsedLabel = (String)objectMap.get(XML_TAG_LABEL);
					if(whereUsedLabel!=null && !whereUsedLabel.isEmpty()){
						whereUsedLabel = i18nNow.getI18nString(whereUsedLabel, "whereusedStringResource", context.getSession().getLanguage());
						returnVector.add(whereUsedLabel);
					}else{
						returnVector.add("");
					}
				}
			}//End of while
		}catch(Exception exception){
			
			throw new FrameworkException(exception);
		}
		return returnVector;
	}
	 
	
	/**get expandlevel from Structure browser
	 * @param context
	 * @param args contains a map whihc key value expandlevel
	 * @return
	 * @throws Exception
	 */
	public static int getLevelfromSB(Context context, String[] args) throws Exception{
		short recurseLevel=1;
		try {
			// unpack the arguments to get the level Details
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			// get the level information from the ParamMap
			String strExpandLevel = (String) programMap.get("expandLevel");
			// if expand level is not available
			if (strExpandLevel == null || ("".equals(strExpandLevel))
					|| ("null".equals(strExpandLevel))) {
				strExpandLevel = Integer.toString(1);
			}

			// If the ExpandLevel is all then set the recurselevel to 0
			if (strExpandLevel.equalsIgnoreCase((RANGE_VALUE_ALL))){
				recurseLevel = (short) 0;
			}
			else if (strExpandLevel.equalsIgnoreCase(("End"))){
				recurseLevel =(short) (Short.parseShort("-1"));
			}
			else{
				recurseLevel =(short) (Short.parseShort(strExpandLevel));
			}

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return recurseLevel;
	}
	
	/** 
	 * process aand load  XML data into the class variable whereUsedXMLMap ,LabelMethodMap which will be used to retrieved data from database.
	 * this method is used in to massage Hierarchialdata
	 */
	public void loadWhereUsed(Context context)throws Exception
	{
		if (whereUsedXMLMap == null)
	      {
			  whereUsedXMLMap = new HashMap();
			  LabelMethodMap = new HashMap();
			  ToolbarMap=new HashMap();
	      }
		
		HashMap tempprocessingMap = new HashMap();
		  HashMap tempVisitedMap = new HashMap();
		 try{
			
			  MQLCommand mql = new MQLCommand();
			  //get thE xml file
			  mql.executeCommand(context, "print page $1 select content dump",DEFAULT_FILE_DEFINITION);
			  
			  
			 Map relationshipMap  = null;
			 List nodeList=null;
			  if(mql.getResult()!=null && !mql.getResult().isEmpty()){				
				  
				  SAXBuilder builder = new SAXBuilder();
					Document document = builder.build(new StringReader(mql.getResult()));
					loadLabels(context,document);			
					 typeList = document.getRootElement().getChildren();
					for (Iterator iterator = typeList.iterator(); iterator.hasNext();) 
					{
						Element elementType = (Element) iterator.next();
						String strType = elementType.getName();
						 typeset=new HashSet();
						 relset=new HashSet();
						 typeset.add(strType);
						 getTypeRelationship(document,strType);
						 expandallTypes( document, strType);
						 whereUsedXMLMap.put(strType,relset);
						 loadToolbar( strType, document);
						 
						
					}
				  
			  }
			 
			 
		 } catch (Exception e) {
				throw new FrameworkException(e.getMessage());
			}
	}
	/** Retrvies theformtype and  relationship corresponding to the type and add to set
	 * @param document -XML Doucment
	 * @param strType- fromTypes corresponding to this type will be added to Set 
	 */
	private void expandallTypes(Document document,String strType)throws Exception{
		XPath  x      = XPath.newInstance("/root/"+strType+"/"+XML_TAG_HIERARCHICAL+"/"+XML_TAG_WHERE_USED+"/@"+XML_TAG_EXPANDTYPES);
			List nodeList    = x.selectNodes(document);
			
			if( nodeList !=null && nodeList.size()>0){	
				 for (Iterator iterator2 = nodeList.iterator(); iterator2.hasNext();) {
					 Attribute attObject = (Attribute) iterator2.next();
					 
					 StringList typelist = FrameworkUtil.split(attObject.getValue(), ",");
					 //get all the fromtypes of each type 
						 for (Iterator iterator = typelist.iterator(); iterator
								.hasNext();) {
							 String strtype = (String) iterator.next();
							 //typeset is initialized for each xml "type_" Tag .
							 //if type present in "typeset" or in the "whereUsedXMLMap" means ,don't have to parse through it. 
							 if(whereUsedXMLMap.get(strtype) !=null){
								 Set rellist = (Set)whereUsedXMLMap.get(strtype);
								 for (Iterator iterator1 = rellist.iterator(); iterator1.hasNext();) {				
									 relset.add((String) iterator1.next());
								 }
							 }
							 else if(typeset.contains(strtype)){
								 continue;
							 }else{
								 typeset.add(attObject.getValue());
								 getTypeRelationship(document,strtype);
								 expandallTypes( document,strtype);					
							 }
							
						}					 
				 }
			}else{
				return;
			}
	}
		
	/** Retrvies the relationship corresponding to the type and add to "relset"
	 * @param document -XML Doucment
	 * @param strType- relationships corresponding to this type will be added to Set "relset"
	 */
	private void getTypeRelationship(Document document,String strType)throws Exception{
		XPath  x      = XPath.newInstance("/root/"+strType+"/"+XML_TAG_HIERARCHICAL+"/"+XML_TAG_WHERE_USED+"/@"+XML_TAG_EXPANDRELATED);
		List nodeList    = x.selectNodes(document);	
		
			 for (Iterator iterator2 = nodeList.iterator(); iterator2.hasNext();) {
				 Attribute attObject = (Attribute) iterator2.next();
				 StringList rellist = FrameworkUtil.split(attObject.getValue(), ",");
				 for (Iterator iterator = rellist.iterator(); iterator
					.hasNext();) {				
					 relset.add((String) iterator.next());
				 }
				 
			 }
		
	
	} 
	/** loads the labels in LabelMethodMap , wthis method is used when processing Hierarchail Data
	 * @param document -XML Doucment
	 */
	private void loadLabels(Context context,Document document)throws Exception{
		XPath  x      = XPath.newInstance("//"+XML_TAG_WHERE_USED);
		List nodeList    = x.selectNodes(document);	
		if( nodeList !=null && nodeList.size()>0){
			String whereUsedApplication = "";
			String whereUsedLabel ="";
			String sLanguage = context.getSession().getLanguage();
			 for (Iterator iterator = nodeList.iterator(); iterator.hasNext();) {
				 Element attObject = (Element) iterator.next();
				 StringList relList = FrameworkUtil.split(attObject.getAttributeValue(XML_TAG_EXPANDRELATED),",");
				 whereUsedApplication = attObject.getAttributeValue(XML_TAG_APPLICATION);								
				whereUsedLabel = i18nNow.getI18nString(attObject.getAttributeValue(XML_TAG_LABEL), UINavigatorUtil.getStringResourceFileId(context,whereUsedApplication), sLanguage);
				 for (Iterator iterator2 = relList.iterator(); iterator2.hasNext();) {
					String strRel = (String) iterator2.next();				
					LabelMethodMap.put(strRel, whereUsedLabel);
					
				}
			 }
		}
	}
	/** loads the labels in LabelMethodMap , wthis method is used when processing Hierarchail Data
	 * @param document -XML Doucment
	 */
	private void loadToolbar(String strType,Document document)throws Exception{
		XPath  x      = XPath.newInstance("/root/"+strType+"/"+XML_TAG_HIERARCHICAL+"/@"+XML_TAG_TOOLBAR);
		List nodeList    = x.selectNodes(document);	
		if( nodeList !=null && nodeList.size()>0){
			StringList tollBarList=new StringList(1);
			 for (Iterator iterator2 = nodeList.iterator(); iterator2.hasNext();) {
				 Attribute attObject = (Attribute) iterator2.next();				 
				  tollBarList = FrameworkUtil.split(attObject.getValue(), ",");				
			 }
			 ToolbarMap.put(strType, tollBarList);
		}
	}
	
	/** gets all the Derived types
	 * @param context - the eMatrix Context object
	 * @param domainObject-> object for which we need teh derived types
	 * @return List of 
	 * @throws Exception
	 */
	public StringList getDerivedTypes(Context context,DomainObject domainObject)throws Exception{
	  String derivedType = domainObject.getInfo(context, DomainConstants.SELECT_TYPE);

		//Construct the list of type and its derived
		StringList derivedTypes = new StringList();
		while(derivedType!=null && !derivedType.isEmpty()){
			derivedTypes.add(derivedType);
			derivedTypes.add(FrameworkUtil.getAliasForAdmin(context, SELECT_TYPE, derivedType, false));
			derivedType = MqlUtil.mqlCommand(context, "print type $1 select derived dump",derivedType);
		}
		return derivedTypes;
	}
  public String getWhereUsedMenu(Context context,String[] args)throws Exception{
	  StringBuffer STRmenuList = new StringBuffer();
	 try{
		 HashMap programMap = (HashMap) JPO.unpackArgs(args);

			String objectId = (String)programMap.get("objectId");
		  if(objectId!=null && !objectId.isEmpty()){
				
				DomainObject domObject = new DomainObject(objectId);
				String strType = domObject.getInfo(context, DomainConstants.SELECT_TYPE);	
				strType=FrameworkUtil.getAliasForAdmin(context, SELECT_TYPE, strType, false);
				StringList menuList=new StringList();
				if(ToolbarMap.get(strType)!=null){
					menuList.addAll((StringList)ToolbarMap.get(strType));
				}else{
					StringList derivedTypes =getDerivedTypes(context, domObject);
					for (Iterator iterator = derivedTypes.iterator(); iterator
							.hasNext();) {
						String strdType = (String) iterator.next();
						if(ToolbarMap.get(strdType)!=null){
							menuList.addAll((StringList)ToolbarMap.get(strdType));
							break;
						}
						
					}
				}
				if(menuList !=null){
					for (Iterator iterator1 = menuList.iterator(); iterator1
							.hasNext();) {
						STRmenuList.append((String) iterator1.next());
						if(iterator1.hasNext()){
							STRmenuList.append(",");
						}
						
					}
				}
		  }
	  } catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
	  return STRmenuList.toString();
  }
  
}
