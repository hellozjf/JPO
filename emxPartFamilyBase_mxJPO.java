/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.Part;
import com.matrixone.apps.engineering.PartFamily;

/**
 * The <code>emxPartFamilyBase</code> class contains implementation code for emxPartFamily JPO.
 *
 * @version EC 10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxPartFamilyBase_mxJPO extends emxCommonPartFamilyBase_mxJPO
{
  protected static final String ATTRIBUTE_REFERENCE_TYPE = PropertyUtil.getSchemaProperty("attribute_ReferenceType");
  protected static final String RESOURCE_BUNDLE_ENGINEERING = "emxEngineeringCentralStringResource";
  static protected emxContextUtil_mxJPO contextUtil = null;

  /** A string constant with the value = */
    public static final String SYMB_EQUALS           =  "=" ;

  /** A string constant with the value 'U' */
    public static final String SYMB_U                = 	"U" ;

    public static final String SYMB_M                = 	"M" ;

	  /** A string constant with the value 'R' */
    public static final String SYMB_R                = 	"R" ;

	/** A string constant with the value 'U' */
    public static final String SYMB_Star             = 	"*" ;

	/** A string constant with the value '' */
    public static final String SYMB_BLANK            = 	"" ;

	public static final String SYMB_PIPELINE         = 	"|" ;

	public static final String STR_ALL				 = "All";

	protected static final String RELATIONSHIP_CLASSIFIED_ITEM = PropertyUtil.getSchemaProperty("relationship_ClassifiedItem");

	protected static final String TYPE_PART_FAMILY = PropertyUtil.getSchemaProperty("type_PartFamily");

	protected static final String RELATIONSHIP_PART_FAMILY_REFERENCE = PropertyUtil.getSchemaProperty("relationship_PartFamilyReference");

	protected static final String RELATIONSHIP_PART_SPECIFICATION = PropertyUtil.getSchemaProperty("relationship_PartSpecification");

	protected static final String RELATIONSHIP_SUBCLASS = PropertyUtil.getSchemaProperty("relationship_Subclass");

	protected static final String TYPE_CLASSIFICATION = PropertyUtil.getSchemaProperty("type_Classification");
	protected static final String TYPE_LIBRARIES = PropertyUtil.getSchemaProperty("type_Libraries");

	/********************************* MAP SELECTABLES /*********************************
	/** A string constant with the value "objectId". */
	protected static final String SELECT_OBJECT_ID = "objectId";
	/** A string constant with the value "objectList". */
	protected static final String SELECT_OBJECT_LIST = "objectList";

   	/********************************* REQUEST PARAMETERS /*********************************
	/** A string constant with the value "selection". */
	protected static final String SYMB_SELECTION = "selection";
	/** A string constant with the value "multiple". */
	protected static final String SYMB_MULTIPLE = "multiple";

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since EC 10-6
     */
    public emxPartFamilyBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
        contextUtil = new emxContextUtil_mxJPO(context, null);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return int.
     * @throws Exception if the operation fails.
     * @since EC 10-6
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on emxPart invocation");
        }
        return 0;
    }

     /**
     * This method is used to get all Part Families
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return MapList.
     * @throws Exception if the operation fails.
     * @since EC 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getPartFamilies(Context context , String[] args) throws Exception
    {
          MapList partFamilyIdMap = null;
         PartFamily partFamilyObj = (PartFamily)DomainObject.newInstance(context,DomainConstants.TYPE_PART_FAMILY,DomainConstants.ENGINEERING);
         SelectList sListPartSelStmts = new SelectList(5);
         sListPartSelStmts.addId();
         HashMap partFamily;
         try
         {
               partFamilyIdMap = DomainObject.querySelect(context,
                                                          DomainObject.TYPE_PART_FAMILY,
                                                          null,
                                                          SYMB_BLANK,
                                                          sListPartSelStmts,
                                                          null,
                                                          false);

               for(int i=0, size=partFamilyIdMap.size(); i < size; i++){
            	  partFamily = (HashMap)partFamilyIdMap.get(i);
            	  partFamilyObj.setId((String)partFamily.get("id"));
            	  if(!partFamilyObj.isDeletable(context)){
            		  partFamily.put("disableSelection", "true");
            	  }
              }
         }
         catch(Exception e)
        {
        }

        return partFamilyIdMap;
    }
    /**
     * showCheckbox - determines if the checkbox needs to be enabled in the column of the Part Family Summary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since EC 10-6
     */
     public Vector showCheckbox(Context context, String[] args)throws Exception
     {
	   try
	   {
		  PartFamily partFamilyObj = (PartFamily)DomainObject.newInstance(context,DomainConstants.TYPE_PART_FAMILY,DomainConstants.ENGINEERING);
		  HashMap programMap = (HashMap) JPO.unpackArgs(args);
		  MapList objectList = (MapList)programMap.get(SELECT_OBJECT_LIST);

		  Vector enableCheckbox = new Vector();
	      Iterator objectListItr = objectList.iterator();
		  while(objectListItr.hasNext())
		  {
			  Map objectMap = (Map) objectListItr.next();
			  String sPartId = (String) objectMap.get(partFamilyObj.SELECT_ID);

			  partFamilyObj.setId(sPartId);
			  boolean deletable = partFamilyObj.isDeletable(context);

			  if(deletable)
			  {
				  enableCheckbox.add("true");
			  }
			  else
			  {
				  enableCheckbox.add("false");
			  }
		  }
		  return enableCheckbox;
	   }
	   catch (Exception ex)
	   {
		  throw ex;
	   }
  }

     private StringList getListValue(Map map, String key) {
 		Object data = map.get(key);
 		if (data == null)
 			return new StringList(0);
 		return (data instanceof String) ? new StringList((String) data) : (StringList) data;
 	}

 /**
       * getAllParts - This method is used to fetch Referenced parts from the list of parts when selected in the filter
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following input arguments:
       *        0 - objectList MapList
       * @returns Object
       * @throws Exception if the operation fails
       * @since EC X3
       *
       */
	  @com.matrixone.apps.framework.ui.ProgramCallable
	  public MapList getAllParts(Context context, String[] args) throws Exception {

		   MapList tList = new MapList();

		   try {
			   String SELECT_REL_PARTFAMILY_REF_PART = "tomid[" + PropertyUtil.getSchemaProperty(context, "relationship_PartFamilyReference") + "].fromrel.to.id";

			   String sWhere = FrameworkProperties.getProperty(context, "emxEngineeringCentral.PartFamily.Filter.AllParts");

			   MapList itemList = getPartFamily(context, args, sWhere);

			   Iterator itr = itemList.iterator();

			   StringList lisTemp;
			   Map tempMap;

			   while (itr.hasNext()) {
				   Map newMap = (Map)itr.next();
				   newMap.put(SYMB_SELECTION, SYMB_MULTIPLE);
				   tList.add (newMap);

				   lisTemp = getListValue(newMap, SELECT_REL_PARTFAMILY_REF_PART);

				   for (int i = 0; i < lisTemp.size(); i++) {
					   tempMap = new HashMap();

					   tempMap.put(DomainConstants.SELECT_ID, lisTemp.get(i));
					   tempMap.put(DomainConstants.SELECT_LEVEL, "2");
					   tempMap.put(SYMB_SELECTION, SYMB_MULTIPLE);

					   tList.add (tempMap);
				   }
			   }
		   } catch (Exception ex) {
				throw ex;
		   }

		   return tList;
	  }

      /**
       * getReferencedPart - This method is used to fetch Referenced parts from the list of parts when selected in the filter
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following input arguments:
       *        0 - objectList MapList
       * @returns Object
       * @throws Exception if the operation fails
       * @since EC X3
       *
       */
	  @com.matrixone.apps.framework.ui.ProgramCallable
	  public MapList getReferenceParts(Context context, String[] args) throws Exception
		{
		   MapList itemList = null;
		   try{
		   String sWhere = FrameworkProperties.getProperty(context, "emxEngineeringCentral.PartFamily.Filter.ReferencedPart");
		   itemList = getPartFamily(context,args,sWhere);

		   Iterator itr = itemList.iterator();
		   MapList tList = new MapList();
		   while(itr.hasNext())
		   {
			   Map newMap = (Map)itr.next();
			   newMap.put(SYMB_SELECTION, SYMB_MULTIPLE);
			   tList.add (newMap);
		   }
		   itemList.clear();
		   itemList.addAll(tList);
		   }
		 catch (Exception ex)
			   {
					 throw ex;
		  }

			   return itemList;
		 }

        /**
       * getMasterPart - This method is used to fetch Master parts from the list of parts when selected in the filter
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following input arguments:
       *        0 - objectList MapList
       * @returns Object
       * @throws Exception if the operation fails
       * @since EC X3
       *
       */
	   @com.matrixone.apps.framework.ui.ProgramCallable
	   public MapList getMasterParts(Context context, String[] args) throws Exception
		{
		   MapList tList = new MapList();
		   try{
		   String sWhere = FrameworkProperties.getProperty(context, "emxEngineeringCentral.PartFamily.Filter.MasterPart");
		   String SELECT_REL_PARTFAMILY_REF_PART = "tomid[" + PropertyUtil.getSchemaProperty(context, "relationship_PartFamilyReference") + "].fromrel.to.id";
		   MapList itemList = getPartFamily(context,args,sWhere);

		   Iterator itr = itemList.iterator();


		   StringList lisTemp;
		   Map tempMap;
		   while(itr.hasNext())
		   {
			   Map newMap = (Map)itr.next();
			   newMap.put(SYMB_SELECTION, SYMB_MULTIPLE);
			   tList.add (newMap);

			   lisTemp = getListValue(newMap, SELECT_REL_PARTFAMILY_REF_PART);

			   for (int i = 0; i < lisTemp.size(); i++) {
				   tempMap = new HashMap();

				   tempMap.put(DomainConstants.SELECT_ID, lisTemp.get(i));
				   tempMap.put(DomainConstants.SELECT_LEVEL, "2");
				   tempMap.put(SYMB_SELECTION, SYMB_MULTIPLE);

				   tList.add (tempMap);
			   }
		   }

		   }
		 catch (Exception ex)
			   {
					 throw ex;
			   }

		   return tList;
		}

          /**
       * getUnassignedPart - This method is used to fetch Unassigned parts from the list of parts when selected in the filter
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following input arguments:
       *        0 - objectList MapList
       * @returns Object
       * @throws Exception if the operation fails
       * @since EC X3
       *
       */
	  @com.matrixone.apps.framework.ui.ProgramCallable
	  public MapList getUnAssignedParts(Context context, String[] args) throws Exception
		{
			   MapList itemList = null;
			  try{
			    String sWhere = FrameworkProperties.getProperty(context, "emxEngineeringCentral.PartFamily.Filter.UnassignedPart");
			   itemList = getPartFamily(context,args,sWhere);

			   Iterator itr = itemList.iterator();
			   MapList tList = new MapList();
			   while(itr.hasNext())
			   {
				   Map newMap = (Map)itr.next();
				   newMap.put(SYMB_SELECTION, SYMB_MULTIPLE);
				   tList.add (newMap);
			   }
			   itemList.clear();
			   itemList.addAll(tList);
			   }
			 catch (Exception ex)
				   {
						 throw ex;
			 }
		return itemList;

		}

     /**
       * getPartFamily - This method is used to fetch all parts from the list of parts which are connected through classified Item with Part Family
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following input arguments:
       *        0 - objectList MapList
       * @returns Object
       * @throws Exception if the operation fails
       * @since EC X3
       *
       */
	   public MapList getPartFamily(Context context, String[] args, String sWhere) throws Exception
		{
			MapList partList= new MapList();
			try
			{
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				String partFamilyId = (String) programMap.get(SELECT_OBJECT_ID);
				String parentID = (String) programMap.get("parentId");
				DomainObject domobj = new DomainObject(partFamilyId);
				if (domobj.isKindOf(context, TYPE_PART_FAMILY))   {

						String relpattern = RELATIONSHIP_CLASSIFIED_ITEM;
						Part partObj = new Part(partFamilyId);

						StringList selectStmts  = new StringList(2);
						selectStmts.addElement(SELECT_ID);
						selectStmts.addElement(SELECT_NAME);

						StringList relSelect = null;

						String level = (String) programMap.get("expandLevel");
						short shExpandLevel = 1;

						if (level != null && !"".equals(level) && !"null".equals(level)) {
							if ("All".equals(level)) {
								shExpandLevel = 0;
							} else {
								try {
									shExpandLevel = Short.parseShort(level);
								} catch (Exception e) {
									shExpandLevel = 1;
								}
							}
						}

						if (shExpandLevel != 1 && (sWhere.equals(STR_ALL) || sWhere.equals(SYMB_M))) {
							String SELECT_REL_PARTFAMILY_REF_PART = "tomid[" + PropertyUtil.getSchemaProperty(context, "relationship_PartFamilyReference") + "].fromrel.to.id";
							relSelect = new StringList(SELECT_REL_PARTFAMILY_REF_PART);
						}

						String strWhere= SYMB_BLANK;
						if (sWhere.equals(STR_ALL))
								 strWhere = "attribute["+ATTRIBUTE_REFERENCE_TYPE+"]!="+SYMB_R;
							 else {

								strWhere = "attribute["+ATTRIBUTE_REFERENCE_TYPE+"]=="+sWhere;
							}

						partList = partObj.getRelatedObjects( context,
																	 relpattern,  // relationship pattern
																	 TYPE_PART,                    // object pattern
																	 selectStmts,                 // object selects
																	 relSelect,              // relationship selects
																	 false,                       // to direction
																	 true,                        // from direction
																	 (short) 1,                   // recursion level
																	 strWhere,                        // object where clause
																	 null);
						return partList;
					}

			 else  {
						   
				 			DomainObject partFamilyObj = new DomainObject(parentID);
				 			StringList classifiedItemRelList = (StringList)partFamilyObj.getInfoList(context, "relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id");
				 			String strCommand = "print bus $1 select $2 dump";
						   String connectionID = MqlUtil.mqlCommand(context,strCommand,partFamilyId,"relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id");
						   StringTokenizer strID = new StringTokenizer(connectionID,",");
						   String conID = SYMB_BLANK;
						   String temp = "";
						   String relID = "";
						   while (strID.hasMoreTokens())
						   {
							   temp = strID.nextToken();
								if(classifiedItemRelList.contains(temp)){
							       conID = temp;
								}
						   }
							 String strCommand1 = "print connection $1 select $2 dump $3";
							 if(!"".equals(conID) && conID!=null)
						   relID = MqlUtil.mqlCommand(context,strCommand1,conID,"tomid["+RELATIONSHIP_PART_FAMILY_REFERENCE+"].id","|");
						   MapList m = new MapList() ;
						   strID = new StringTokenizer(relID,SYMB_PIPELINE);
						   while(strID.hasMoreTokens()){
						   String rID = strID.nextToken();
						   HashMap theMap = new HashMap();
						   strCommand1 = "print connection $1 select $2 dump $3";
						   String CIID = MqlUtil.mqlCommand(context,strCommand1,rID.trim(),"fromrel["+RELATIONSHIP_CLASSIFIED_ITEM+"].to.id","|");
						   theMap.put("id",CIID);
						   m.add(theMap);
						}
			return m;
				}
			}

			catch (Exception ex)
				  {
						throw ex;
			}
	  }

   /**
         * setReferenceType - This method is used to set the value of attribute "Reference Type" for the Parts under the Part Family
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns Object
         * @throws Exception if the operation fails
         * @since EC X3
         *
       */
		public void setReferenceType(Context context, String[] args) throws Exception
			{
				ContextUtil.pushContext(context);
			  try
				{
			   HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			   String[] selectedPartlist = (String[])paramMap.get("selectedPartlist");
			   String key =(String)paramMap.get("key");
			   int selectedPartlistSize = selectedPartlist.length;

			   for(int i = 0 ; i< selectedPartlistSize; i++) {
				   String partID = (String)selectedPartlist[i];
				   StringList relsel = new StringList();
				   relsel.add("tomid.id");
				   DomainObject domObj = new DomainObject(partID);
				   if ("toMaster".equals(key))   {
						   domObj.setAttributeValue(context,ATTRIBUTE_REFERENCE_TYPE,SYMB_M);
					   }
					   else if ("toUnassign".equals(key))   {
						   // bug:008684
  						   String strCommand = "print bus $1 select $2 dump";
						   String connectionID = MqlUtil.mqlCommand(context,strCommand,partID,"relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id");
						   StringTokenizer strID = new StringTokenizer(connectionID,SYMB_EQUALS);
						   String conID = SYMB_BLANK;
						   while (strID.hasMoreTokens())
						   {
							   conID = strID.nextToken();
						   }
							 MapList relList= DomainRelationship.getInfo(context,new String[]{conID},relsel);
						 Iterator itr = relList.iterator();
						 while(itr.hasNext()) {
							 Map relIDmap = (Map) itr.next();
							 Object obj = relIDmap.get("tomid["+RELATIONSHIP_PART_FAMILY_REFERENCE+"].id");
							if(obj != null){
							if( obj instanceof String) {
								String ids = (String)obj;
								String str1 = "print connection $1 select $2 dump $3";
								String Pid= MqlUtil.mqlCommand(context,str1,ids.trim(),"fromrel["+RELATIONSHIP_CLASSIFIED_ITEM+"].to.id","|");
								DomainObject chObj = new DomainObject(Pid);
								chObj.setAttributeValue(context,ATTRIBUTE_REFERENCE_TYPE,SYMB_U);
								String delcommand = "delete connection $1";
								MqlUtil.mqlCommand(context,delcommand,ids);
							 }
							 else {
								StringList idLists = (StringList)obj;
									int size = idLists.size();
									for(int a =0; a< size; a++) {
										String rID = (String)idLists.get(a);
										String str2 = "print connection $1 select $2 dump $3";
										String Pid= MqlUtil.mqlCommand(context,str2,rID.trim(),"fromrel["+RELATIONSHIP_CLASSIFIED_ITEM+"].to.id","|");
										DomainObject chObj = new DomainObject(Pid);
										chObj.setAttributeValue(context,ATTRIBUTE_REFERENCE_TYPE,SYMB_U);
										String delcommand = "delete connection $1";
										MqlUtil.mqlCommand(context,delcommand,rID);
									}

								}
							 }
						}
					 domObj.setAttributeValue(context,ATTRIBUTE_REFERENCE_TYPE,SYMB_U);
				  }
			  }
			} catch (Exception e) { }
			ContextUtil.popContext(context);
		   }

   /**
         * AddReferencestoMaster - This method is used to set the value of attribute "Reference Type" for the Parts under the Part Family
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns Object
         * @throws Exception if the operation fails
         * @since EC X3
         *
       */
			public void AddReferencestoMaster(Context context, String[] args) throws Exception
				{
					ContextUtil.pushContext(context);
				  try
					{
					   HashMap programMap = (HashMap)JPO.unpackArgs(args);
					   String langStr = (String)programMap.get("languageStr");
					   HashMap reqTablemap = (HashMap)programMap.get("reqTableMap");
					   HashMap reqMap1 = (HashMap)programMap.get("reqMap");
					   String[] parentOID = (String[])reqMap1.get("parentOID");
					   String[] MasterId = (String[])reqTablemap.get("MasterId");
					   HashMap reqmap = (HashMap)programMap.get("reqMap");
					   String[] emxTableRowId = (String[])reqmap.get("emxTableRowId");
					   DomainObject partFamilyObj = new DomainObject(parentOID[0]);
			           StringList classifiedItemRelList = (StringList)partFamilyObj.getInfoList(context, "relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id");
					   DomainObject domobj = new DomainObject(MasterId[0]);
					   MapList speclist = new MapList();
					   String relpattern = RELATIONSHIP_PART_SPECIFICATION;
					   StringList selectStmts  = new StringList(2);
					   selectStmts.addElement(SELECT_ID);
					   selectStmts.addElement(SELECT_NAME);
					   speclist= domobj.getRelatedObjects( context,
																 relpattern,  // relationship pattern
																 "*",                    // object pattern
																 selectStmts,                 // object selects
																 null,              // relationship selects
																 false,                       // to direction
																 true,                        // from direction
																 (short) 1,                   // recursion level
																 null,                        // object where clause
																 null);
                       int sizeval = speclist.size();
                       String[] specList = new String[sizeval];
                       for (int i=0;i<sizeval;i++) {
						   Map listmap = (Map)speclist.get(i);
						   specList[i]=(String)listmap.get("id");
					   }
					   String strCommand = "print bus $1 select $2 dump $3";
			           String strrelId = MqlUtil.mqlCommand(context,strCommand,MasterId[0],"relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id","|");
			           StringTokenizer strID = new StringTokenizer(strrelId,"|");
					   //String conID = SYMB_BLANK;
					   String temp = "";
					   while (strID.hasMoreTokens())
					   {
						   temp = strID.nextToken();
							if(classifiedItemRelList.contains(temp)){
								strrelId = temp;
							}
						   
						   
					   }
			           int size = emxTableRowId.length;
			           for (int i=0;i<size;i++)  {
						    DomainObject chdomobj = new DomainObject(emxTableRowId[i]);
						    MapList chspeclist = new MapList();
						    String relpattrn = RELATIONSHIP_PART_SPECIFICATION;
						    StringList selStmts  = new StringList(2);
						    StringList relsel  = new StringList();
						    relsel.addElement(SELECT_ID);
						    selStmts.addElement(SELECT_ID);
						    selStmts.addElement(SELECT_NAME);
						    chspeclist= chdomobj.getRelatedObjects( context,
																	 relpattrn,  // relationship pattern
																	 "*",                    // object pattern
																	 selStmts,                 // object selects
																	 relsel,              // relationship selects
																	 false,                       // to direction
																	 true,                        // from direction
																	 (short) 1,                   // recursion level
																	 null,                        // object where clause
																	 null);
						   int chsizeval = chspeclist.size();
						   String[] chspecList = new String[chsizeval];
						   for (int j=0;j<chsizeval;j++) {
						   Map chlistmap = (Map)chspeclist.get(j);
						   chspecList[j]=(String)chlistmap.get("id");
                           }
					       int specsize = chspeclist.size();
					       i18nNow i18nnow = new i18nNow();
						   String strAlertMessage = i18nnow.GetString(RESOURCE_BUNDLE_ENGINEERING,
						   															       langStr,
															          "emxEngineeringCentral.PartSeries.MQLNotice");
					       DomainRelationship domrel = new DomainRelationship();
					       domrel.disconnect(context,chspecList);
					       if (specsize>0) {
							     emxContextUtil_mxJPO.mqlNotice(context,strAlertMessage);
					       domrel.connect(context,
												  chdomobj,
												  RELATIONSHIP_PART_SPECIFICATION,
												  true,
												  specList,
												  false);
                           }
                           else {
						   domrel.connect(context,
												  chdomobj,
												  RELATIONSHIP_PART_SPECIFICATION,
												  true,
												  specList,
												  false);
                           }
						   String strCommand1 = "print bus $1 select $2 dump $3";
			               String strrelId1 = MqlUtil.mqlCommand(context,strCommand1,emxTableRowId[i],"relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id","|");
			               StringTokenizer strID1 = new StringTokenizer(strrelId1,"|");
						   //String conID = SYMB_BLANK;
						   
						   while (strID1.hasMoreTokens())
						   {
							   temp = strID1.nextToken();
								if(classifiedItemRelList.contains(temp)){
									strrelId1 = temp;
								}
							   
							   
						   }
			               String strCommand2 = "add connection $1 fromrel $2 torel $3";

			               MqlUtil.mqlCommand(context,strCommand2,RELATIONSHIP_PART_FAMILY_REFERENCE,strrelId1,strrelId);
			               DomainObject unassignObj = new DomainObject(emxTableRowId[i]);
					       unassignObj.setAttributeValue(context,ATTRIBUTE_REFERENCE_TYPE,SYMB_R);
				       }
					}
			catch (Exception e) { }
			ContextUtil.popContext(context);
			}

/**
         * getUnAssignedPartsforMaster - This method is used to saerch the part Under part Family based on the attribute value "Reference Type"
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns Object
         * @throws Exception if the operation fails
         * @since EC X3
         *
       */
		@com.matrixone.apps.framework.ui.ProgramCallable
		public MapList getUnAssignedPartsforMaster(Context context, String[] args)
					throws Exception {
					try {
						HashMap programMap = (HashMap) JPO.unpackArgs(args);
						HashMap reqmap = (HashMap)programMap.get("RequestValuesMap");
						String[] objectId = (String[])reqmap.get(SELECT_OBJECT_ID);
						String PartFamilyId = objectId[0];
						String strWhere = "attribute["+ATTRIBUTE_REFERENCE_TYPE+"]==U";
						String[] strArrayFilterState =  (String[])reqmap.get("filterState");
						String strFilterState = strArrayFilterState[0];
						if(strFilterState != null && !strFilterState.equals("")){
								strWhere = strWhere + " && current != "+strFilterState;
						}
						DomainObject domObj = new DomainObject(PartFamilyId);
						MapList UnAssignpartList = new MapList();
						String strName = (String)programMap.get("Name");
						if ( strName == null || strName.equals(SYMB_BLANK) ) {
								strName = SYMB_Star;
						}

						String[] strRev = (String[])reqmap.get("Rev");
						if ( strRev[0] == null || strRev[0].equals(SYMB_BLANK) ) {
												strRev[0] = SYMB_Star;
						}

					   StringList selectStmts  = new StringList();
					   selectStmts.addElement(SELECT_ID);
					   selectStmts.addElement(SELECT_NAME);
					   UnAssignpartList = domObj.getRelatedObjects( context,
																			 RELATIONSHIP_CLASSIFIED_ITEM ,  // relationship pattern
																			 TYPE_PART,                    // object pattern
																			 selectStmts,                 // object selects
																			 null,              // relationship selects
																			 false,                       // to direction
																			 true,                        // from direction
																			 (short) 1,                   // recursion level
																			 strWhere,                        // object where clause
																			 null);
					   return UnAssignpartList;
				 }
					catch (Exception excp) {
						throw excp;
					}

			}

   /**
         * removeReferenceFromMaster - This method is used to set the value of attribute "Reference Type" for the Parts under the Part Family
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns Object
         * @throws Exception if the operation fails
         * @since EC X3
         *
       */
		public void removeReferenceFromMaster(Context context, String[] args) throws Exception
			{
				ContextUtil.pushContext(context);
			   HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			   String[] selectedPartlist = (String[])paramMap.get("selectedPartlist");
			   String partFamilyId = (String) paramMap.get("objectId");
			   DomainObject partFamilyObj = new DomainObject(partFamilyId);
	           StringList classifiedItemRelList = (StringList)partFamilyObj.getInfoList(context, "relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id");
			   int selectedPartlistSize = selectedPartlist.length;
			   StringList relsel = new StringList();
			   relsel.add("tomid.id");
				   for(int i = 0 ; i< selectedPartlistSize; i++) {
					   String partID = (String)selectedPartlist[i];
					   String strCommand = "print bus $1 select $2 dump";
					   String connectionID = MqlUtil.mqlCommand(context,strCommand,partID,"relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id");
					   StringTokenizer strID = new StringTokenizer(connectionID,",");
			   String conID = SYMB_BLANK;
			   String temp = "";
					   while (strID.hasMoreTokens())
					   {
						   temp = strID.nextToken();
							if(classifiedItemRelList.contains(temp)){
						       conID = temp;
							}
					   }
					String str1 = "print connection $1 select $2 dump $3";
							String Pid= MqlUtil.mqlCommand(context,str1,conID.trim(),"frommid["+RELATIONSHIP_PART_FAMILY_REFERENCE+"].id","|");
							String delcommand = "delete connection $1";
							MqlUtil.mqlCommand(context,delcommand,Pid);
							DomainObject refObj = new DomainObject(partID);
					refObj.setAttributeValue(context,ATTRIBUTE_REFERENCE_TYPE,SYMB_U);
		           }
		           ContextUtil.popContext(context);
		  }

      /**
         * removeReferenceFromMaster - This method is used to set the value of attribute "Reference Type" for the Parts under the Part Family
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns Object
         * @throws Exception if the operation fails
         * @since EC X3
         * @author Ranjit Kumar Singh
       */
	public void disconnectexistingSpec(Context context, String[] args) throws Exception
			{
      		  String toobjectID = args[1];
      		  DomainObject domobj = new DomainObject(toobjectID);
      		  MapList partlist = new MapList();
      		  StringList selStmts  = new StringList(2);
			  StringList relsel  = new StringList();
			  relsel.addElement(SELECT_ID);
			  selStmts.addElement(SELECT_ID);
			  selStmts.addElement(SELECT_NAME);
			  String strWhere = "attribute["+ATTRIBUTE_REFERENCE_TYPE+"]=="+SYMB_R;
      		  partlist = domobj.getRelatedObjects(context,
														 RELATIONSHIP_PART_SPECIFICATION ,  // relationship pattern
														 TYPE_PART,                    // object pattern
														 selStmts,                 // object selects
														 relsel,              // relationship selects
														 true,                       // to direction
														 false,                        // from direction
														 (short) 1,                   // recursion level
														 strWhere,                        // object where clause
														 null);
			  int val = partlist.size();
			  String[] partarr = new String[val];
			  for (int j=0;j<val;j++) {
				Map listmap = (Map)partlist.get(j);
				partarr[j]=(String)listmap.get("id");
			  }
			  DomainRelationship domrel = new DomainRelationship();
			  domrel.disconnect(context,partarr);
		    }

	/**
         * addSpecsToReferenceParts - This method is used to Add specifications to all referenced parts when a specification is added to its corresponding master part
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns Object
         * @throws Exception if the operation fails
         * @since EC X3
       */
	public void addSpecsToReferenceParts(Context context, String[] args) throws Exception{
		try{
			String fromobjectID = args[0];//Getting the Part Object id
			String toobjectID = args[1];//Getting the Specification Object id
			String SYMB_M = "M";
			String resultRecords = "";
			String resultFields = "";
			DomainObject domFromObj = new DomainObject(fromobjectID);
			String strattr = domFromObj.getInfo(context,"attribute["+ATTRIBUTE_REFERENCE_TYPE+"]");
            String PartSeriesEnabled = FrameworkProperties.getProperty(context, "emxEngineeringCentral.PartSeries.PartSeriesActive");
			if(("true".equalsIgnoreCase(PartSeriesEnabled)) && (strattr.equalsIgnoreCase(SYMB_M))) {
				String command = "print bus $1 select $2 dump $3";
				
 				String strres = MqlUtil.mqlCommand(context, command, fromobjectID, "to["+RELATIONSHIP_CLASSIFIED_ITEM+"].id", "|");
 				StringList classifiedList = FrameworkUtil.split(strres, "|"); // if a part is connected to more than PF then classified Item will be more than 2
 				
 				String command1 = "query connection type $1 where $2 select $3 dump $4";
 				for (int j = 0; j < classifiedList.size(); j++) {
	 				String strres1 = MqlUtil.mqlCommand(context,command1,RELATIONSHIP_PART_FAMILY_REFERENCE,"torel.id == "+classifiedList.get(j),"fromrel.to.id","|");
					if(strres1!=null && !strres1.equals("")){
						StringTokenizer stRecords = new StringTokenizer(strres1,"\\n");
						while (stRecords.hasMoreTokens()) {
							resultRecords = stRecords.nextToken();
							StringTokenizer stFields = new StringTokenizer(resultRecords,"|");
							for(int i=0;stFields.hasMoreTokens();i++) {
								resultFields = stFields.nextToken();
								if(i ==1) {
									DomainObject domRefFromObject = new DomainObject(resultFields);
									DomainObject domToObj = new DomainObject(toobjectID);
									DomainRelationship.connect(context,domRefFromObject,RELATIONSHIP_PART_SPECIFICATION,domToObj);
								}//End of check for if condition which will get the id of the referenced part
							}//End of for loop which will be traversed two times to get the id of the referenced part
						}//End of while loop which will be traversed as many times as the number of referenced parts this master part is having
					}//End of check for if there is any Reference parts connected to this masterpart
				}
			}//End of check for if PartsSeries enabled and if it is a Master Part
		}catch(Exception e){
			throw new Exception(e.toString());
		}
   }//End of addSpecsToReferenceParts method

   /**
         * removeSpecsFromReferenceParts - This method is used to remove specifications to all referenced parts when a specification is removed from its corresponding master part
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns Object
         * @throws Exception if the operation fails
         * @since EC X3
       */

	public void removeSpecsFromReferenceParts(Context context, String[] args) throws Exception{
		try{
			String fromobjectID = args[0];//Getting the Part Object id
			String toobjectID = args[1];//Getting the Specification Object id
			String SYMB_M = "M";
			String resultRecords = "";
			String resultFields = "";
			DomainObject domFromObj = new DomainObject(fromobjectID);
			String strattr = domFromObj.getInfo(context,"attribute["+ATTRIBUTE_REFERENCE_TYPE+"]");
			String PartSeriesEnabled = EnoviaResourceBundle.getProperty(context,"emxEngineeringCentral.PartSeries.PartSeriesActive");

			if(("true".equalsIgnoreCase(PartSeriesEnabled)) && (strattr.equalsIgnoreCase(SYMB_M))) {
				String command = "print bus $1 select $2 dump $3"; 				
 				
 				String strres = MqlUtil.mqlCommand(context, command, fromobjectID, "to["+RELATIONSHIP_CLASSIFIED_ITEM+"].id", "|");
 				StringList classifiedList = FrameworkUtil.split(strres, "|"); // if a part is connected to more than PF then classified Item will be more than 2
 				
 				String command1 = "query connection type $1 where $2 select $3 dump $4";
 				for (int j = 0; j < classifiedList.size(); j++) {
	 				String strres1 = MqlUtil.mqlCommand(context, command1, RELATIONSHIP_PART_FAMILY_REFERENCE, "torel.id == "+classifiedList.get(j), "fromrel.to.id","|");
	 				
					if(strres1!=null && !strres1.equals("")){
						StringTokenizer stRecords = new StringTokenizer(strres1,"\n");//IR-017221
						while (stRecords.hasMoreTokens()) {
							resultRecords = stRecords.nextToken();
							StringTokenizer stFields = new StringTokenizer(resultRecords,"|");
							for(int i=0;stFields.hasMoreTokens();i++) {
								resultFields = stFields.nextToken();
								if(i ==1) {
									String strQuery = "print connection bus $1 to $2 relationship $3 select $4 dump $5";
									String strres2 = MqlUtil.mqlCommand(context,strQuery,resultFields,toobjectID,DomainConstants.RELATIONSHIP_PART_SPECIFICATION,"id","|");
									if(strres2!=null && !strres2.equals("")){
										DomainRelationship.disconnect(context,strres2);
									}//End of check for if condition which will entered to disconnect the relationship between the spec and reference object
								}//End of check for if condition which will get the id of the referenced part
							}//End of for loop which will be traversed two times to get the id of the referenced part
						}//End of while loop which will be traversed as many times as the number of referenced parts this master part is having
					}//End of check for if there is any Reference parts connected to this masterpart
 				}
			}//End of check for if PartsSeries enabled and if it is a Master Part
		}catch(Exception e){
			throw new Exception(e.toString());
		}
   }//End of removeSpecsFromReferenceParts method


   /**
         * unassignReferenceAndMasterParts - This method is used to remove the references from part family
         *
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - objectList MapList
         * @returns Object
         * @throws Exception if the operation fails
         * @since EngineeringCentral X3
       */

	public void unassignReferenceAndMasterParts(Context context, String[] args) throws Exception
	{
		try
		{
			String fromObjectID = args[0];
			String toObjectID = args[1];
//			String relID = args[2];

			DomainObject domToObj = new DomainObject(toObjectID);

			String strReferenceType = domToObj.getInfo(context,"attribute["+ATTRIBUTE_REFERENCE_TYPE+"]");

			if (SYMB_M.equals(strReferenceType))
			{
				if ("False".equalsIgnoreCase(domToObj.getInfo(context, "to[" + RELATIONSHIP_CLASSIFIED_ITEM + "]"))) {
					domToObj.setAttributeValue(context, ATTRIBUTE_REFERENCE_TYPE, SYMB_U);
				}

				StringList strlObjSelects = new StringList(1);
				strlObjSelects.add(SELECT_ID);

				StringList strlRelSelects = new StringList(2);
				strlRelSelects.add(SELECT_RELATIONSHIP_ID);
				strlRelSelects.add("frommid["+RELATIONSHIP_PART_FAMILY_REFERENCE+"].id");

				DomainObject doPartFamily = new DomainObject(fromObjectID);

				String strWhere = "attribute[" + ATTRIBUTE_REFERENCE_TYPE + "]==R";
				MapList mapParts = doPartFamily.getRelatedObjects( context,
													 RELATIONSHIP_CLASSIFIED_ITEM,  // relationship pattern
													 TYPE_PART,                    // object pattern
													 strlObjSelects,                 // object selects
													 strlRelSelects,              // relationship selects
													 false,                       // to direction
													 true,                        // from direction
													 (short) 1,                   // recursion level
													 strWhere,                        // object where clause
													 null);

				Iterator itrParts = mapParts.iterator();

				while (itrParts.hasNext())
				{
					Map mapPart = (Map) itrParts.next();

					String strPartId = (String) mapPart.get(SELECT_ID);

					String strPartRefId = (String) mapPart.get("frommid["+RELATIONSHIP_PART_FAMILY_REFERENCE+"].id");

					if (strPartRefId == null || strPartRefId.trim().length() == 0)
					{
						DomainObject doChild = new DomainObject(strPartId);
						doChild.setAttributeValue(context,ATTRIBUTE_REFERENCE_TYPE,SYMB_U);
					}

				}

			}
			else if (SYMB_R.equals(strReferenceType))
			{
				domToObj.setAttributeValue(context,ATTRIBUTE_REFERENCE_TYPE,SYMB_U);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

    /**
         * getPartFamilyExcludeParts - This method is used to get the Parts already connected to Part Family so that they are not
         * displayed on the Autonomy Search results page.
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - paramMap MapList
         * @returns Object
         * @throws Exception if the operation fails
         * @since EngineeringCentral X3
       */
    @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
    public StringList getPartFamilyExcludeParts(Context context, String args[]) throws Exception
    {
        MapList partList = new MapList();
        StringList excludePartList = new StringList();
        SelectList selectStmts = new SelectList(1);
        selectStmts.addElement(DomainObject.SELECT_ID);
        HashMap paramMap = (HashMap) JPO.unpackArgs(args);
        String partFamilyId    = (String)paramMap.get("objectId");
        String RELATIONSHIP_CLASSIFIED_ITEM = PropertyUtil.getSchemaProperty(context,"relationship_ClassifiedItem");
        DomainObject partFamilyObj = new DomainObject(partFamilyId);
        partList = (MapList)partFamilyObj.getRelatedObjects(context,
                                                                RELATIONSHIP_CLASSIFIED_ITEM,
                                                                "*",
                                                                selectStmts,
                                                                DomainObject.EMPTY_STRINGLIST,
                                                                false,
                                                                true,
                                                                (short)1,
                                                                null,
                                                                null);
        int size = partList.size();
        for(int i=0;i<size;i++)
        {
            Map partMap = (Map) partList.get(i);
            String partId = (String) partMap.get(DomainObject.SELECT_ID);
            excludePartList.add(partId);
        }
        return excludePartList;
    }

    /**
     * Update the 'Classification Class' and its parent objects' count as a result
     * of a classified item being revised.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments:
     *    [0]:  ${FROMOBJECTID}
     *    [1]:  ${TOOBJECTID}
     *    [2]:  ${PARENTEVENT}
     *    [3]:  ${NEWRELID}
     * @return int
     * @throws Exception if the operation fails
     * @since EngineeringCentral X3
     * @grade 0
     */
    public int updateCount(Context context, String[] args) throws Exception
    {
        if(args == null || (args !=null && args.length < 3))
        {
            throw new Exception ("ERROR - Invalid number of arguments");
        }

        int ret = 0;

        //arg[0]: classification object id
        DomainObject dmObj = (DomainObject) DomainObject.newInstance(context, args[0]);
        String parentEvent = args[2];   //revise,clone,modify
        String newRelId = args[3];      //new relationship id

        //update the count as a result of a classified item being revised
        if("revise".equals(parentEvent) && newRelId != null && newRelId.length() > 0)
        {
            try
            {
               ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING); //366577
               ContextUtil.startTransaction(context, true);
                String attrCountSel = "attribute[" + dmObj.ATTRIBUTE_COUNT + "]";

                String countStr = dmObj.getInfo(context, attrCountSel);
                int count = (new Integer(countStr)).intValue();
                count++;

                dmObj.setAttributeValue(context, dmObj.ATTRIBUTE_COUNT, Integer.toString(count));

                //Update other parent's count as well
                StringList busSels = new StringList();
                busSels.add(dmObj.SELECT_ID);
                busSels.add(attrCountSel);

                String rels = RELATIONSHIP_SUBCLASS;
                StringBuffer types = new StringBuffer(TYPE_CLASSIFICATION);
                types.append(',');
                types.append(TYPE_LIBRARIES);

                MapList result = dmObj.getRelatedObjects(context,
                                          rels,
                                          types.toString(),
                                          busSels,
                                          null,
                                          true,
                                          false,
                                          (short)0,
                                          null,
                                          null);

                if(result != null && result.size() > 0)
                {
                    for(int i=0; i < result.size(); i++)
                    {
                        Map map = (Map) result.get(i);
                        String sObjId = (String) map.get(dmObj.SELECT_ID);
                        String sCount = (String) map.get(attrCountSel);
                        int objCounts = (new Integer(sCount)).intValue();
                        objCounts++;

                        DomainObject parentObj =
                          (DomainObject) DomainObject.newInstance(context,sObjId);

                        parentObj.setAttributeValue(context, dmObj.ATTRIBUTE_COUNT, Integer.toString(objCounts));
                    }
                }

                ContextUtil.commitTransaction(context);
            }catch(Exception ex)
            {
                //ret = 1;
                ContextUtil.abortTransaction(context);
                throw ex;
            }
            finally { ContextUtil.popContext(context); } //366577
         }

        return ret;
    }
    /**
         * hasReferenceTypeAccess - This method is used to check Part Series functionality is enabled
         * displayed on the Autonomy Search results page.
         * @param context the eMatrix <code>Context</code> object
         * @param args holds the following input arguments:
         *        0 - paramMap MapList
         * @returns true or false based on property entry
         * @throws Exception if the operation fails
         * @since EngineeringCentral X3
       */
	 public boolean hasReferenceTypeAccess(Context context, String[] args) throws Exception
    {
		String PartSeriesEnabled = FrameworkProperties.getProperty(context, "emxEngineeringCentral.PartSeries.PartSeriesActive");
		return ("True".equalsIgnoreCase(PartSeriesEnabled));
	}

	/**
	    * Added this function to fix IR-093282V6R2012, this StringList returns list for Exclude ids
	    * @param context
	    * @param args
	    * @return
	    * @throws Exception
	    */
	 @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	 public StringList excludeConnectedObjects(Context context, String[] args) throws Exception {
	     String stypeDOCUMENTS = PropertyUtil.getSchemaProperty(context,"type_DOCUMENTS");

	     String relToExpand = PropertyUtil.getSchemaProperty(context, "relationship_PartFamilyReferenceDocument");
           StringList sSelectables= new StringList();
           sSelectables.add(DomainConstants.SELECT_ID);

           MapList totalresultList = DomainObject.findObjects(context,
        		   stypeDOCUMENTS,
                   "*",
                   "*",
                   "*",
                   context.getVault().getName(),
                   "(!attribute["+DomainConstants.ATTRIBUTE_IS_VERSION_OBJECT+"]==True)&&to["+relToExpand+"]==True",
                   null,
                   true,
                   sSelectables,
                   Short.parseShort("0"));


	      StringList excludeList= new StringList();
	          for(int i=0;i<totalresultList.size();i++){
	              Map tempMap=(Map)totalresultList.get(i);
	              excludeList.add(tempMap.get(DomainConstants.SELECT_ID));
	          }
	        return excludeList;
	    }
		/**
		 * To create the part family object from create component
		 *
		 * @param context
		 * @param args
		 * @return Map
		 * @throws Exception
		 * @Since R212
		 */
	  @com.matrixone.apps.framework.ui.CreateProcessCallable
	  public Map createPartFamilyJPO(Context context, String[] args) throws Exception {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String sType =(String)	programMap.get("TypeActual");
			String sName = (String) programMap.get("Name");
			String sPolicy =(String) programMap.get("Policy");
			String sVault = (String) programMap.get("Vault");

			PartFamily partFamily = new PartFamily();
			partFamily.createPartFamily(context, sType, sName, null, sPolicy, sVault);

			String partfamilyId = partFamily.getId(context);

			if(!sPolicy.equalsIgnoreCase(EngineeringConstants.POLICY_PART_FAMILY))
			partFamily.promote(context);

			HashMap mapReturn = new HashMap(1);
			mapReturn.put("id", partfamilyId);

			return mapReturn;
		}

	  /** This method is called on part Family edit post process action
	   *
	   * @param context
	   * @param args
	   * @return
	   * @throws Exception
	   */
	  @com.matrixone.apps.framework.ui.PostProcessCallable
	  public HashMap partFamilyEditPostProcess(Context context, String[] args) throws Exception
	  {
		  HashMap resultMap   = new HashMap();

		  try{
			  String[] app = {"ENO_PRT_TP", "ENO_LIB_TP","ENO_LBC_TP"};
			  ComponentsUtil.checkLicenseReserved(context, app); //License Check
		  }
		  catch(Exception e){
			  resultMap.put("Action", "error");
			  resultMap.put("Message", e.getMessage());
		  }

		  return resultMap;
	  }
	  
	  
	  
	  public void addReferencestoMasterFullSearch(Context context, String[] args) throws Exception
		{
			ContextUtil.pushContext(context);
		  try
			{
			   HashMap programMap = (HashMap)JPO.unpackArgs(args);
			   String langStr="en";
			   String parentOID = (String)programMap.get("objectId");
			   String MasterId = (String)programMap.get("MasterId");
			   String[] emxTableRowId = (String[])programMap.get("emxTableRowId");
			   DomainObject partFamilyObj = new DomainObject(parentOID);
	           StringList classifiedItemRelList = (StringList)partFamilyObj.getInfoList(context, "relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id");
			   DomainObject domobj = new DomainObject(MasterId);
			   MapList speclist = new MapList();
			   String relpattern = RELATIONSHIP_PART_SPECIFICATION;
			   StringList selectStmts  = new StringList(2);
			   selectStmts.addElement(SELECT_ID);
			   selectStmts.addElement(SELECT_NAME);
			   speclist= domobj.getRelatedObjects( context,
														 relpattern,  // relationship pattern
														 "*",                    // object pattern
														 selectStmts,                 // object selects
														 null,              // relationship selects
														 false,                       // to direction
														 true,                        // from direction
														 (short) 1,                   // recursion level
														 null,                        // object where clause
														 null);
             int sizeval = speclist.size();
             String[] specList = new String[sizeval];
             for (int i=0;i<sizeval;i++) {
				   Map listmap = (Map)speclist.get(i);
				   specList[i]=(String)listmap.get("id");
			   }
			   String strCommand = "print bus $1 select $2 dump $3";
	           String strrelId = MqlUtil.mqlCommand(context,strCommand,MasterId,"relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id","|");
	           StringTokenizer strID = new StringTokenizer(strrelId,"|");
			   String temp = "";
			   while (strID.hasMoreTokens())
			   {
				   temp = strID.nextToken();
					if(classifiedItemRelList.contains(temp)){
						strrelId = temp;
					}
				   
				   
			   }
	           int size = emxTableRowId.length;
	           String partId="";
	           for (int i=0;i<size;i++)  {
	        	   StringTokenizer strTokens = new StringTokenizer(emxTableRowId[i],"|");
	       			if ( strTokens.hasMoreTokens()) {
	       				partId = strTokens.nextToken();
	       			}
				    DomainObject chdomobj = new DomainObject(partId);
				    
				    MapList chspeclist = new MapList();
				    String relpattrn = RELATIONSHIP_PART_SPECIFICATION;
				    StringList selStmts  = new StringList(2);
				    StringList relsel  = new StringList();
				    relsel.addElement(SELECT_ID);
				    selStmts.addElement(SELECT_ID);
				    selStmts.addElement(SELECT_NAME);
				    chspeclist= chdomobj.getRelatedObjects( context,
															 relpattrn,  // relationship pattern
															 "*",                    // object pattern
															 selStmts,                 // object selects
															 relsel,              // relationship selects
															 false,                       // to direction
															 true,                        // from direction
															 (short) 1,                   // recursion level
															 null,                        // object where clause
															 null);
				   int chsizeval = chspeclist.size();
				   String[] chspecList = new String[chsizeval];
				   for (int j=0;j<chsizeval;j++) {
				   Map chlistmap = (Map)chspeclist.get(j);
				   chspecList[j]=(String)chlistmap.get("id");
                 }
			       int specsize = chspeclist.size();
			       i18nNow i18nnow = new i18nNow();
				   String strAlertMessage = i18nnow.GetString(RESOURCE_BUNDLE_ENGINEERING,
				   															       langStr,
													          "emxEngineeringCentral.PartSeries.MQLNotice");
			       DomainRelationship domrel = new DomainRelationship();
			       domrel.disconnect(context,chspecList);
			       if (specsize>0) {
					     emxContextUtil_mxJPO.mqlNotice(context,strAlertMessage);
			       domrel.connect(context,
										  chdomobj,
										  RELATIONSHIP_PART_SPECIFICATION,
										  true,
										  specList,
										  false);
                 }
                 else {
				   domrel.connect(context,
										  chdomobj,
										  RELATIONSHIP_PART_SPECIFICATION,
										  true,
										  specList,
										  false);
                 }
				   String strCommand1 = "print bus $1 select $2 dump $3";
	               String strrelId1 = MqlUtil.mqlCommand(context,strCommand1,partId,"relationship["+RELATIONSHIP_CLASSIFIED_ITEM+"].id","|");
	               StringTokenizer strID1 = new StringTokenizer(strrelId1,"|");
				   //String conID = SYMB_BLANK;
				   
				   while (strID1.hasMoreTokens())
				   {
					   temp = strID1.nextToken();
						if(classifiedItemRelList.contains(temp)){
							strrelId1 = temp;
						}
					   
					   
				   }
	               String strCommand2 = "add connection $1 fromrel $2 torel $3";

	               MqlUtil.mqlCommand(context,strCommand2,RELATIONSHIP_PART_FAMILY_REFERENCE,strrelId1,strrelId);
	               DomainObject unassignObj = new DomainObject(partId);
			       unassignObj.setAttributeValue(context,ATTRIBUTE_REFERENCE_TYPE,SYMB_R);
		       }
			}
	catch (Exception e) { 
		e.printStackTrace();
	}
	ContextUtil.popContext(context);
	}
	
	/* include IOD pgm to convert search the part Under part Family based on the attribute value "Reference Type"*/
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getUnAssignedPartsforMasterInFullSearch(Context context, String[] args)
				throws Exception {
				try {
					HashMap programMap = (HashMap) JPO.unpackArgs(args);
					StringList unAssignedPartsList = new StringList();
					String parentId = (String)programMap.get("objectId");
					//String PartFamilyId = parentId;
					String strWhere = "attribute["+ATTRIBUTE_REFERENCE_TYPE+"]==U";
					DomainObject domObj = new DomainObject(parentId);
					MapList UnAssignpartList = new MapList();
					
				   StringList selectStmts  = new StringList();
				   selectStmts.addElement(SELECT_ID);
				   selectStmts.addElement(SELECT_NAME);
				   UnAssignpartList = domObj.getRelatedObjects( context,
																		 RELATIONSHIP_CLASSIFIED_ITEM ,  // relationship pattern
																		 TYPE_PART,                    // object pattern
																		 selectStmts,                 // object selects
																		 null,              // relationship selects
																		 false,                       // to direction
																		 true,                        // from direction
																		 (short) 1,                   // recursion level
																		 strWhere,                        // object where clause
																		 null);
				   Iterator itrunassignedParts = UnAssignpartList.iterator();
			    	while( itrunassignedParts.hasNext()){
			    		Map mapUnassignedPart = (Map) itrunassignedParts.next();
			    		String strunAssignedPartId = (String) mapUnassignedPart.get(DomainConstants.SELECT_ID);
			    		unAssignedPartsList.add(strunAssignedPartId);
			    	}
				   return unAssignedPartsList;
			 }
				catch (Exception excp) {
					throw excp;
				}

		}

	  
   } // END of JPO

