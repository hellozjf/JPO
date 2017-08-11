// emxECRProjectBase.java
//
// Copyright (c) 2002-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
// static const char RCSID[] = $Id: /java/JPOsrc/base/${CLASSNAME}.java 1.1 Fri Dec 19 16:45:25 2008 GMT QZV Experimental$
//
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.AttributeType;
import matrix.db.AttributeTypeList;
import matrix.db.BusinessInterface;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.enterprisechange.ChangeProject;
import com.matrixone.apps.enterprisechange.EnterpriseChangeConstants;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.productline.ProductLineConstants;

/**
 * The <code>emxECRProjectBase</code> class represents the Project Space JPO
 * functionality for the AEF type.
 *
 * @version AEF 9.5.1.1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxChangeProjectBase_mxJPO extends ChangeProject {

    /**
     * Constructs a new emxECRProject JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the id
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.1
     */
    public emxChangeProjectBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        // Call the super constructor
        super();
        if ((args != null) && (args.length > 0))
        {
            setId(args[0]);
        }
    }

    /**
     * Constructs a new emxECRProject JPO object.
     *
     * @param id the business object id
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.3
     */
    public emxChangeProjectBase_mxJPO (String id) throws Exception {
        super(id);
    }

    /**
     * Affected Items Report
     * @param context
     * @param args
     * @return MapList
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getProjectDeliverablesList(Context context, String[] args) throws Exception {

        HashMap hmpProgramMap = (HashMap) JPO.unpackArgs(args);
        String strTaskObjectIds = (String) hmpProgramMap.get("taskIds");
        String strProjId = (String) hmpProgramMap.get("objectId");
        MapList maplTaskDeliverableList = new MapList();

        Pattern relPattern = new Pattern(DomainConstants.RELATIONSHIP_SUBTASK);
        relPattern.addPattern(DomainConstants.RELATIONSHIP_TASK_DELIVERABLE);

        StringList strlTypeSelects = new StringList(DomainObject.SELECT_ID);

        String strShowAll = (String) hmpProgramMap.get("showAll");

        if("true".equals(strShowAll)) {

            DomainObject projObj = new DomainObject(strProjId);
            maplTaskDeliverableList = projObj.getRelatedObjects (context, relPattern.getPattern(),
                      "*", strlTypeSelects, null, false, true, (short)-2, null ,null,0);

        } else  if("false".equals(strShowAll) && strTaskObjectIds != null) {

                DomainObject taskObj = new DomainObject(strProjId);
                StringTokenizer stkSelectedIdTokenizer = new StringTokenizer(strTaskObjectIds, ",");
                while (stkSelectedIdTokenizer.hasMoreTokens()) {
                    taskObj.setId(stkSelectedIdTokenizer.nextToken());
                    maplTaskDeliverableList.addAll(taskObj.getRelatedObjects(context,
                            relPattern.getPattern(), "*", strlTypeSelects, null, false, true,( short)-2, null, null,0));
                }

        }
        return maplTaskDeliverableList;
    }

    /**
     * AccessFunction
     * @param context
     * @param args
     * @return boolean
     * @throws Exception
     */
    public boolean showCommandForChangeProject(Context context, String[] args) throws Exception {

        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strObjectId = (String) programMap.get("objectId");

            DomainObject doObj = new DomainObject(strObjectId);
            if(doObj.isKindOf(context, TYPE_CHANGE_PROJECT)) {
                return true;
            }
        } catch (Exception ex) {
            throw ex;
        }
        return false;
    }

    /**
     * gets the type and subtypes of the changetask as hidden variables
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    public String getHiddenFields(Context context, String[] args) throws Exception {

        StringBuffer outPut = new StringBuffer();
        try {
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");
            String strObjectId = (String)paramMap.get("objectId");

            setId(strObjectId);
            String isChangeProj = isKindOf(context, TYPE_CHANGE_PROJECT) ? "true" : "false";
            outPut.append("<input type=\"hidden\" id=\"ECHChangeProject\" name=\"ECHChangeProject\" value=\""+ isChangeProj +"\"></input>");

        } catch(Exception ex) {
            throw  new FrameworkException((String)ex.getMessage());
        }
        return outPut.toString();
    }

    /**
     * Method to get all the issues connected to all Product revs of the Model
     * @param context
     * @param args
     * @return MapList
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getIssueRelatedToModel(Context context, String[] args) throws Exception {

        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String strObjectId = (String)programMap.get("objectId");
        StringList issueIdList = null;
        StringList allIssuesList = new StringList();
        String issueId = "";

        String issueRel = PropertyUtil.getSchemaProperty(context,"relationship_Issue");

        StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
        objectSelects.addElement("to["+issueRel+"].from.id");

        setId(strObjectId);
        String strType = PropertyUtil.getSchemaProperty(context,"type_Products");
        String strRelType = PropertyUtil.getSchemaProperty(context,"relationship_Products");

        MapList returnList = new MapList();
        MapList prodRevList = getRelatedObjects(context , strRelType,
                strType, objectSelects, null, true, true, (short)1, DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
        Iterator prodRevItr = prodRevList.iterator();
        while(prodRevItr.hasNext()) {
            Map map = (Map)prodRevItr.next();
            issueIdList = ChangeProject.toStringList(map.get("to["+issueRel+"].from.id"));
            for(int i=0; i<issueIdList.size(); i++) {
                issueId = (String)issueIdList.get(i);
                if(!allIssuesList.contains(issueId)) {
                    allIssuesList.addElement(issueId);
                    Map tempMap = new HashMap();
                    tempMap.put(DomainConstants.SELECT_ID, issueId);
                    returnList.add(tempMap);
                }
            }
        }
        return returnList;
    }


	/**
	 * Method to get related Model Change Projects connected through Related Projects relationship
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return MapList - List of connected Change Projects
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 * @deprecated since R418
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getModelRelatedChangeProjects(Context context, String[] args) throws Exception{
		MapList returnMapList = new MapList();
		try{
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) paramMap.get("objectId");

			if(objectId!=null && !objectId.equalsIgnoreCase("")){
				DomainObject domObject = new DomainObject(objectId);
				//Get Model related Change Projects
				MapList relatedModelChangeProjects = domObject.getRelatedObjects(context,
						ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS,
						EnterpriseChangeConstants.TYPE_CHANGE_PROJECT,
						new StringList(DomainConstants.SELECT_ID),
						new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
						false,	//to relationship
						true,	//from relationship
						(short)1,
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING,
						0);

				returnMapList.addAll(relatedModelChangeProjects);
			}
		}catch (Exception e){
			throw e;
		}finally{
			return returnMapList;
		}
	}

	/**
	 * Method to get Model related Program
	 * Rather through Model related Product Line
	 * Rather through Model Template
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return MapList - List related Program
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public MapList getRelatedProgram(Context context, String[] args) throws Exception{
		MapList returnMapList = new MapList();
		try{
			StringList objectSelects = new StringList(1);
			objectSelects.addElement(DomainConstants.SELECT_ID);

			StringList relationshipSelects = new StringList(1);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

			MapList relatedPrograms = new MapList();

			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			//objectId = Model
			String objectId = (String) paramMap.get("objectId");

			if(objectId!=null && !objectId.equalsIgnoreCase("")){
				DomainObject domObject = new DomainObject(objectId);
				//Get related Product Line
				MapList relatedProductLines = domObject.getRelatedObjects(context,
						ProductLineConstants.RELATIONSHIP_PRODUCTLINE_MODELS,
						ProductLineConstants.TYPE_PRODUCT_LINE,
						objectSelects,
						relationshipSelects,
						true,	//to relationship
						false,	//from relationship
						(short)1,
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING,
						0);

				//if Related Product Line --> get Program
				if(relatedProductLines!=null && !relatedProductLines.isEmpty()){
					Iterator relatedProductLinesItr = relatedProductLines.iterator();
					while(relatedProductLinesItr.hasNext()){
						Map relatedProductLine = (Map)relatedProductLinesItr.next();
						if(relatedProductLine!=null && !relatedProductLine.isEmpty()){
							String relatedProductLineId = (String)relatedProductLine.get(DomainConstants.SELECT_ID);
							if(relatedProductLineId!=null && !relatedProductLineId.isEmpty()){
								//Get related Program
								relatedPrograms = new DomainObject(relatedProductLineId).getRelatedObjects(context,
										ProductLineConstants.RELATIONSHIP_PRODUCT_LINE_PROGRAM,
										ProductLineConstants.TYPE_PROGRAM,
										objectSelects,
										relationshipSelects,
										false,	//to relationship
										true,	//from relationship
										(short)1,
										DomainConstants.EMPTY_STRING,
										DomainConstants.EMPTY_STRING,
										0);
								if(relatedPrograms!=null && !relatedPrograms.isEmpty()){
									returnMapList.addAll(relatedPrograms);
								}

							}
						}
					}//End of while
				}

				//Get Model Template
				MapList relatedModelTemplates = domObject.getRelatedObjects(context,
						EnterpriseChangeConstants.RELATIONSHIP_MODEL_TEMPLATE,
						ProductLineConstants.TYPE_MODEL,
						objectSelects,
						relationshipSelects,
						true,	//to relationship
						false,	//from relationship
						(short)1,
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING,
						0);
				//If Model Template --> getRelatedProgram
				if(relatedModelTemplates!=null && !relatedModelTemplates.isEmpty()){
					Iterator relatedModelTemplatesItr = relatedModelTemplates.iterator();
					while(relatedModelTemplatesItr.hasNext()){
						Map relatedModelTemplate = (Map)relatedModelTemplatesItr.next();
						if(relatedModelTemplate!=null && !relatedModelTemplate.isEmpty()){
							String relatedModelTemplateId = (String)relatedModelTemplate.get(DomainConstants.SELECT_ID);
							if(relatedModelTemplateId!=null && !relatedModelTemplateId.isEmpty()){
								HashMap programMap = new HashMap();
								programMap.put("objectId", relatedModelTemplateId);
								String[] methodargs =JPO.packArgs(programMap);
								MapList relatedProgramsTemp = getRelatedProgram(context,methodargs);
								if(relatedProgramsTemp!=null && !relatedProgramsTemp.isEmpty()){
									returnMapList.addAll(relatedProgramsTemp);
								}
							}
						}
					}//End of while
				}
			}

		}catch (Exception e){
			throw e;
		}finally{
			return returnMapList;
		}
	}

	/**
	 * Method to get available Change Project to connect to context Model
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return MapList - List of available Change Projects
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAvailableModelRelatedChangeProjects(Context context, String[] args) throws Exception{
		MapList returnMapList = new MapList();
		try{
			StringList objectSelects = new StringList(2);
			objectSelects.addElement(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_NAME);

			StringList relationshipSelects = new StringList(1);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			//objectId = Model
			String objectId = (String) paramMap.get("objectId");

			MapList relatedPrograms = getRelatedProgram(context, args);

			if(relatedPrograms!=null && relatedPrograms.size()>0){
				Iterator relatedProgramsItr = relatedPrograms.iterator();
				while(relatedProgramsItr.hasNext()){
					Map relatedProgram = (Map)relatedProgramsItr.next();
					if(relatedProgram!=null && relatedProgram.size()>0){
						String relatedProgramId = (String)relatedProgram.get(DomainConstants.SELECT_ID);
						if(relatedProgramId.equalsIgnoreCase(relatedProgramId)){
							//Get Change Project available List
							MapList relatedChangeProjects = new DomainObject(relatedProgramId).getRelatedObjects(context,
									ProductLineConstants.RELATIONSHIP_PROGRAM_PROJECT + "," + EnterpriseChangeConstants.RELATIONSHIP_SUBTASK,
									EnterpriseChangeConstants.TYPE_CHANGE_PROJECT,
									objectSelects,
									relationshipSelects,
									false,	//to relationship
									true,	//from relationship
									(short)1,
									DomainConstants.EMPTY_STRING,
									DomainConstants.EMPTY_STRING,
									0);
							if(relatedChangeProjects!=null && relatedChangeProjects.size()>0){
								returnMapList.addAll(relatedChangeProjects);
							}
						}
					}
				}
			}
			//sort returnMapList by Name
			returnMapList.sortStructure(DomainConstants.SELECT_NAME, "ascending", "string");
		}catch (Exception e){
			throw e;
		}finally{
			return returnMapList;
		}
	}
	@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
	public StringList getAvailableModelRelatedChangeProjectsList(Context context, String[] args) throws Exception{
		StringList returnStringList = new StringList();
		try{
			MapList returnMapList = getAvailableModelRelatedChangeProjects(context, args);
			Iterator mapItr = returnMapList.iterator();
			
			while(mapItr.hasNext()){
				Map projectMap = (Map)mapItr.next();
				String objectId = (String)projectMap.get(DomainConstants.SELECT_ID);
				if(objectId != null && !"".equals(objectId)){
					returnStringList.add(objectId);
				}				
			}			
		}catch (Exception e){
			throw e;
		}finally{
			return returnStringList;
		}
	}
	
	/**
	 * Method to authorize user to select available Change Project
	 * If Change Project already connected to Model, the checkbox is grayed out
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId
	 * @return MapList - List of available Change Projects
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public Vector enableCheckboxModelRelatedChangeProjects(Context context, String[] args) throws Exception{
		Vector returnVector = new Vector();
		try {
			StringList objectSelects = new StringList(1);
			objectSelects.addElement(DomainConstants.SELECT_ID);

			StringList relationshipSelects = new StringList(1);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

			final boolean ENABLECHECKBOX = true;

			// Get object list information from packed arguments
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			Map paramList = (Map)programMap.get("paramList");
			//objectId = Model
			String objectId = (String) paramList.get("objectId");
			MapList relatedChangeProjects = (MapList)programMap.get("objectList");


			//Get Model related Change Projects
			MapList relatedModelChangeProjects = new DomainObject(objectId).getRelatedObjects(context,
					ProductLineConstants.RELATIONSHIP_RELATED_PROJECTS,
					EnterpriseChangeConstants.TYPE_CHANGE_PROJECT,
					objectSelects,
					relationshipSelects,
					false,	//to relationship
					true,	//from relationship
					(short)1,
					DomainConstants.EMPTY_STRING,
					DomainConstants.EMPTY_STRING,
					0);



			// Do for each object
			Iterator relatedChangeProjectsItr = relatedChangeProjects.iterator();
			while(relatedChangeProjectsItr.hasNext()){
				Map relatedChangeProject = (Map) relatedChangeProjectsItr.next();
				String relatedChangeProjectId = (String)relatedChangeProject.get(DomainConstants.SELECT_ID);
				if(relatedChangeProjectId!=null && !relatedChangeProjectId.equalsIgnoreCase("")){
					//Compare with already linked Change Project
					Boolean flag = false;
					if(relatedModelChangeProjects!=null && relatedModelChangeProjects.size()>0){
						Iterator relatedModelChangeProjectsItr = relatedModelChangeProjects.iterator();
						while(relatedModelChangeProjectsItr.hasNext()){
							Map relatedModelChangeProject = (Map)relatedModelChangeProjectsItr.next();
							if(relatedModelChangeProject!=null && relatedModelChangeProject.size()>0){
								String relatedModelChangeProjectId = (String)relatedModelChangeProject.get(DomainConstants.SELECT_ID);
								//If Change Project is not already connected --> Add to returnMapList
								if(relatedChangeProjectId.equalsIgnoreCase(relatedModelChangeProjectId)){
									flag = true;
								}
							}
						}
					}
					if(!flag){
						returnVector.add(String.valueOf(ENABLECHECKBOX));
					}else{
						returnVector.add(String.valueOf(!ENABLECHECKBOX));
					}
				}
			}
		}catch (Exception e){
			throw e;
		}finally{
			return returnVector;
		}
	}

	/**
	 * Display Change Discipline values for related Change Projects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          paramMap - contains ObjectId, new Value for Change Discipline
	 * @return StringList - containing Change Discipline values
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */
	public StringList displayChangeDisciplineValue(Context context,String[] args) throws Exception{
		StringList returnStringList = new StringList();
		try{
			String languageStr  =  context.getSession().getLanguage();

			Map programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			returnStringList.setSize(objectList.size());

			String strInterfaceName = EnterpriseChangeConstants.INTERFACE_CHANGE_DISCIPLINE;
			BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
			AttributeTypeList listInterfaceAttributes = busInterface.getAttributeTypes(context);

			Iterator objectListItr = objectList.iterator();
			int i = 0;
			while(objectListItr.hasNext()){
				StringBuffer stringBuffer = new StringBuffer();
				Map objectMap = (Map) objectListItr.next();
				if(objectMap!=null && objectMap.size()>0){
					String objectMapId = (String)objectMap.get(DomainConstants.SELECT_ID);
					if(objectMapId!=null && !objectMapId.equalsIgnoreCase("")){
						DomainObject domObjectMapId = new DomainObject(objectMapId);

						Iterator listInterfaceAttributesItr = listInterfaceAttributes.iterator();
						while (listInterfaceAttributesItr.hasNext()){
							String attrName = ((AttributeType) listInterfaceAttributesItr.next()).getName();
							String attrValue = domObjectMapId.getAttributeValue(context, attrName);
							if(attrValue.equalsIgnoreCase(EnterpriseChangeConstants.CHANGE_DISCIPLINE_TRUE)){
								if(!stringBuffer.toString().equalsIgnoreCase("")){
									stringBuffer.append(",");
								}
								stringBuffer.append(i18nNow.getAttributeI18NString(attrName,languageStr));
							}
						}
					}
					returnStringList.set(i, stringBuffer.toString());
					i++;
				}
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}finally{
			return returnStringList;
		}
	}


	public void connectModelRelatedChangeProjects(Context context, String args[]) throws Exception{
		try	{
			HashMap paramMap = (HashMap)JPO.unpackArgs(args);
			String objectId = (String) paramMap.get("objectId");
			DomainObject parentObject = DomainObject.newInstance(context, objectId);
			String direction = (String) paramMap.get("direction");	//if isTo=false then rel=from, if isTo=true then rel=to
			String strRelationshipName = (String) paramMap.get("relName");
			String uiType = (String) paramMap.get("uiType");
			String emxTableRowIds[] = (String[]) paramMap.get("emxTableRowId");
			String strTableRowId = "";
			StringList slEmxTableRowId = new StringList();
			for(int i=0;i<emxTableRowIds.length; i++){
				strTableRowId = emxTableRowIds[i];
				slEmxTableRowId = FrameworkUtil.split(strTableRowId, "|");
				if(slEmxTableRowId.size()>0){
					if(uiType.equalsIgnoreCase("table")){
						strTableRowId = (String)slEmxTableRowId.get(1);
						//strTableRowId = (String)slEmxTableRowId.get(0);
					}else if(uiType.equalsIgnoreCase("structureBrowser")){
						strTableRowId = (String)slEmxTableRowId.get(0);
					}
					if("from".equalsIgnoreCase(direction)){
						com.matrixone.apps.domain.DomainRelationship.connect(context,
								parentObject,
								strRelationshipName,
								DomainObject.newInstance(context, strTableRowId));
					}else if("to".equalsIgnoreCase(direction)){
						com.matrixone.apps.domain.DomainRelationship.connect(context,
								DomainObject.newInstance(context, strTableRowId),
								strRelationshipName,
								parentObject);
					}
				}
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}
	}


	/**
	 * Method to get Change Project related Program
	 * @param context the eMatrix <code>Context</code> object
	 * @param args - Holds the HashMap containing the following arguments
	 *          objectList - contains objectList
	 * @return Vector - List related Program
	 * @throws Exception if the operation fails
	 * @since EnterpriseChange R211
	 */

	public Vector getProjectProgram(Context context, String[] args) throws Exception{
		Vector returnVector = new Vector();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			StringList objectSelects = new StringList(1);
			objectSelects.addElement(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_NAME);

			StringList relationshipSelects = new StringList(1);
			relationshipSelects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

			String prefixItemUrl = "<a href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=";
			String itemId = null;
			String suffixItemUrl = "', '930', '650', 'false', 'popup', '')\" class=\"object\">";
			String itemName = null;
			String anchorEnd = "</a>";

			if(objectList!=null && !objectList.isEmpty()){
				Iterator objectListItr = objectList.iterator();
				while(objectListItr.hasNext()){
					StringBuffer stbTNR = new StringBuffer();
					Map object = (Map)objectListItr.next();
					if(object!=null && !object.isEmpty()){
						String objectId = (String)object.get(DomainConstants.SELECT_ID);
						if(objectId!=null && !objectId.isEmpty()){
							//Get related Project Program
							MapList relatedProjectPrograms = new DomainObject(objectId).getRelatedObjects(context,
									ProductLineConstants.RELATIONSHIP_PROGRAM_PROJECT,
									DomainConstants.QUERY_WILDCARD,
									objectSelects,
									relationshipSelects,
									true,	//to relationship
									false,	//from relationship
									(short)1,
									DomainConstants.EMPTY_STRING,
									DomainConstants.EMPTY_STRING,
									0);

							if(relatedProjectPrograms!=null && !relatedProjectPrograms.isEmpty()){
								Iterator relatedProjectProgramsItr = relatedProjectPrograms.iterator();
								while(relatedProjectProgramsItr.hasNext()){
									Map relatedProjectProgram = (Map)relatedProjectProgramsItr.next();
									if(relatedProjectProgram!=null && !relatedProjectProgram.isEmpty()){
										String relatedProjectProgramId = (String)relatedProjectProgram.get(DomainConstants.SELECT_ID);
										if(relatedProjectProgramId!=null && !relatedProjectProgramId.isEmpty()){
											itemId = relatedProjectProgramId;
											DomainObject domObject = new DomainObject(itemId);
											//itemName = (String)domObject.getInfo(context, DomainConstants.SELECT_NAME);
											itemName = (String)relatedProjectProgram.get(DomainConstants.SELECT_NAME);
											if(stbTNR!=null && stbTNR.length()>0){
												stbTNR.append(",");
											}

											stbTNR.append(prefixItemUrl);
											stbTNR.append(XSSUtil.encodeForJavaScript(context, itemId));
											stbTNR.append(suffixItemUrl);
											stbTNR.append(XSSUtil.encodeForXML(context, itemName));
											stbTNR.append(anchorEnd);
										}
									}
								}
							}
						}
					}
					returnVector.add(stbTNR.toString());
				}
			}
		}catch (Exception e){
			System.out.println(e.getMessage());
		}finally{
			return returnVector;
		}
	}

	/**
  	 * Get Change discipline field.
  	 * @param context - The eMatrix <code>Context</code> object.
  	 * @param args holds information about object.
  	 * @return change discipline fields name.
  	 * @throws Exception if operation fails.
  	 */
  	public String getChangeDisciplineAttribute(Context context,String[]args)throws Exception
  	{
  		StringBuilder sb = new StringBuilder();
  		String strInterfaceName = PropertyUtil.getSchemaProperty(context,"interface_ChangeDiscipline");
  		BusinessInterface busInterface = new BusinessInterface(strInterfaceName, context.getVault());
  		AttributeTypeList listInterfaceAttributes = busInterface.getAttributeTypes(context);

  		//Get the selected value in case of back
  		StringList objectBackDisciplineList = new StringList();
  		for(int i=0;i<listInterfaceAttributes.size();i++){
  			String attrName = ((AttributeType) listInterfaceAttributes.get(i)).getName();
  			if(attrName!=null && attrName.equalsIgnoreCase("Yes")){
  				objectBackDisciplineList.addElement(attrName);
  			}
  		}

  		Map paramMap = new HashMap();
  		paramMap.put("objectId", DomainObject.EMPTY_STRING);
  		paramMap.put("mode", "edit");
  		paramMap.put("wizType", "Blank");
  		paramMap.put("objectBackDisciplineList", objectBackDisciplineList);
  		
  		String[] methodargs = JPO.packArgs(paramMap);
  		MapList listToDisplayAttributes = (MapList)JPO.invoke(context, 
  				"emxEnterpriseChange", 
  				new String[0], 
  				"getChangeDisciplinesToDisplay",
  				methodargs, 
  				MapList.class);

  		sb.append("<table>");
  		sb.append("<tr>");
  		
  		Iterator listToDisplayAttributesItr = listToDisplayAttributes.iterator();
  		while(listToDisplayAttributesItr.hasNext()){
  			Map attributeToDisplay = (Map)listToDisplayAttributesItr.next();
  			
  			sb.append("<td>");
  			if(Boolean.valueOf((String)attributeToDisplay.get("isDisplayed")).booleanValue()){
  				sb.append("<input type=\"checkbox\" id=\""+(String)attributeToDisplay.get("attrNameSmall")+"\" name=\""+(String)attributeToDisplay.get("attrNameSmall")+
  						"\" checked=\""+ (String)attributeToDisplay.get("isChecked")+"\" "+(String)attributeToDisplay.get("isDisabled")+ "onClick=\""+(String)attributeToDisplay.get("onClick")+"\" />");
  				sb.append((String)attributeToDisplay.get("attrNameI18N"));
  			}
  			sb.append("&amp;nbsp;");
  			sb.append("<input type=\"hidden\" id=\""+(String)attributeToDisplay.get("attrNameSmallHidden")+"\" name=\""+(String)attributeToDisplay.get("attrNameSmallHidden")+
  					"\" value=\""+(String)attributeToDisplay.get("attrNameValue")+"\" />");
  			sb.append("</td>");
  		}

  		sb.append("</tr>");
  		sb.append("</table>");

  		return sb.toString();
  	}
  	
  	/**
  	 * Get applicability context Field on task creation page.
  	 * @param The eMatrix <code>Context</code> object.
  	 * @param args holds information about object.
  	 * @return applicability context Field.
  	 * @throws Exception if operation fails.
  	 */
  	public String getApplicabilityContextFields(Context context,String[] args)throws Exception
  	{
  		StringBuilder sb = new StringBuilder();
  		
  		String applicabilityContext = DomainObject.EMPTY_STRING;
		StringList applicabilityContextsList = new StringList();
		
		if (UIUtil.isNullOrEmpty(applicabilityContext)) {
			applicabilityContext = DomainObject.EMPTY_STRING;
		} else {
			applicabilityContextsList = FrameworkUtil.split(applicabilityContext, ",");
		}
		
		String add = EnoviaResourceBundle.getProperty(context,"ProgramCentral", 
  				"emxProgramCentral.Common.Add", context.getSession().getLanguage()); 
		String remove = EnoviaResourceBundle.getProperty(context,"ProgramCentral", 
  				"emxProgramCentral.Common.Remove", context.getSession().getLanguage()); 
		
		sb.append("<input type=\"hidden\" name=\"ApplicabilityContextsHidden\" id=\"ApplicabilityContextsHidden\" value=\""+applicabilityContext+"\" readonly=\"readonly\" />");
		sb.append("<table>");
		sb.append("<tr>");
		sb.append("<th rowspan=\"2\">");
		sb.append("<select name=\"ApplicabilityContexts\" style=\"width:100px\" multiple=\"multiple\">");
		
		if (applicabilityContextsList!=null && !applicabilityContextsList.isEmpty()){
			for (int i=0;i<applicabilityContextsList.size();i++) {
				String applicabilityContextId = (String) applicabilityContextsList.get(i);
				if (applicabilityContextId!=null && !applicabilityContextId.isEmpty()) {
					String applicabilityContextName = new DomainObject(applicabilityContextId).getInfo(context, DomainConstants.SELECT_NAME);
					if (applicabilityContextName!=null && !applicabilityContextName.isEmpty()) {

						sb.append("<option value=\""+applicabilityContextId+"\" >");
						sb.append(applicabilityContextName);
						sb.append("</option>");

					}
				}
			}
		}
		
		sb.append("</select>");
		sb.append("</th>");
		sb.append("<td>");              				
		sb.append("<a href=\"javascript:addApplicabilityContextsSelector()\">");
		sb.append("<img src=\"../common/images/iconStatusAdded.gif\" width=\"12\" height=\"12\" border=\"0\" />");
		sb.append("</a>");
		sb.append("<a href=\"javascript:addApplicabilityContextsSelector()\">");
		sb.append(add);
		sb.append("</a>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td>");
		sb.append("<a href=\"javascript:removeApplicabilityContexts()\">");
		sb.append("<img src=\"../common/images/iconStatusRemoved.gif\" width=\"12\" height=\"12\" border=\"0\" />");
		sb.append("</a>");
		sb.append("<a href=\"javascript:removeApplicabilityContexts()\">");
		sb.append(remove);
		sb.append("</a>");
		sb.append("</td>");
		sb.append("</tr>");
		sb.append("</table>");
  		
  		return sb.toString();
  	}
}

