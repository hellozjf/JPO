/*
 * emxResourcePlanTemplateBase
 *
 * Copyright (c) 1999-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 */


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.AccessConstants;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;
import com.matrixone.apps.common.Task;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.program.Currency;
import com.matrixone.apps.program.FTE;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.ProjectTemplate;
import com.matrixone.apps.program.ResourcePlanTemplate;
import com.matrixone.apps.program.ResourceRequest;

/**
 * @author WQY
 *
 */
public class emxResourcePlanTemplateBase_mxJPO extends emxDomainObjectBase_mxJPO
{
	protected static final String STRING_MESSAGE = "Message";
	public emxResourcePlanTemplateBase_mxJPO(Context context, String[] args)
	throws Exception 
	{
		super(context, args);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws MatrixException
	 */
	public int mxMain(Context context, String[] args) throws MatrixException 
	{
		throw new MatrixException("This JPO cannot be run stand alone.");
	}

	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static String getNameFieldValue(Context context, String[] args) throws Exception
	{
		StringBuffer strNameField = new StringBuffer(100);
		String strName = "";
		strNameField.append("<input type=\"text\" size=\"30\" name=\"Name\" id=\"Name\" value=\""+XSSUtil.encodeForHTML(context,strName)+"\"");
		strNameField.append(">");
		//	    if("create".equals(strFunctionMode))
		//	    {
		//	    	String strObjectId = (String)requestMap.get("objectId");
		//	    	DurationKeywords durationKeywords = new DurationKeywords(context,strObjectId);
		//	    	DurationKeyword[] durationKeyword = durationKeywords.getDurationKeywords();
		//	    	strNameField.append("<INPUT TYPE=\"hidden\" ID=\"validNameCount\" NAME=\"validNameCount\" value=\""+durationKeyword.length+"\">");
		//	    	for(int i=0;i<durationKeyword.length;i++)
		//	    	{
		//	    		strNameField.append("<INPUT TYPE=\"hidden\" ID=\"validateName"+i+"\" NAME=\"validateName"+i+"\" value=\""+durationKeyword[i].getName()+"\">");
		//	    	}
		//	    }
		return strNameField.toString();
	}
	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static String getDescriptionFieldValue(Context context, String[] args) throws Exception
	{
		StringBuffer strDescriptionField = new StringBuffer(100);
		String strDescription = "";
		strDescriptionField.append("<TEXTAREA name=\"Description\" rows=\"5\" cols=\"25\">"+XSSUtil.encodeForHTML(context,strDescription)+"</TEXTAREA>");
		return strDescriptionField.toString();
	}

	/**
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public String createResourcePlanTemplate(Context context, String[] args)  throws Exception 
	{
		String strId = null;
		try 
		{
			Map paramMap = (Map) JPO.unpackArgs(args);
			String strObjectId = (String)paramMap.get("ObjectId");
			String strResourcePlanTemplateName = (String)paramMap.get("ResourcePlanTemplateName");
			String strResourcePlanTemplateDesc = (String)paramMap.get("ResourcePlanTemplateDesc");
			DomainObject domProjectTemplate = DomainObject.newInstance(context, strObjectId);
			String strRelationship = PropertyUtil.getSchemaProperty(context,
			"relationship_ResourcePlanTemplate");
			DomainRelationship dmrResourcePlanTemplate = DomainRelationship.connect(context,domProjectTemplate,strRelationship,domProjectTemplate);
			Map attrValueMap = new HashMap();
			attrValueMap.put(ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_NAME,strResourcePlanTemplateName);
			attrValueMap.put(ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_DESC,strResourcePlanTemplateDesc);
			attrValueMap.put(ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_STATE,ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_STATE_RANGE_ACTIVE);
			Date currDate = new Date();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
			String strCurrDate = simpleDateFormat.format(currDate);
			attrValueMap.put(ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_LAST_MODIFIED_DATE,strCurrDate);
			dmrResourcePlanTemplate.setAttributeValues(context, attrValueMap);
			Map mapRelationShip = dmrResourcePlanTemplate.getRelationshipData(context, new StringList(DomainRelationship.SELECT_ID));
			strId = (String)((List)mapRelationShip.get(DomainRelationship.SELECT_ID)).get(0);
		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
		return strId;
	} 

	/**
	 * Gets resource plan table data for resource request
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getTableResourcePlanTemplateData(Context context, String[] args) throws Exception
	{
		try
		{
			// [Added::Jan 6, 2011:s4e:R211:IR-071066V6R2012:::Start] 
			String sLanguage = context.getSession().getLanguage();
			String strErrorMessage = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.ResourceRequest.DefaultCurrencyExchangeRatesNotDefined", sLanguage);
			String strPrefferedCurrency = emxProgramCentralUtilBase_mxJPO.getUserPreferenceCurrency(context, strErrorMessage);
			// [Added::Jan 6, 2011:s4e:R211:IR-071066V6R2012::End]
			HashMap programMap          = (HashMap) JPO.unpackArgs(args);
			String strProjectTemplateId = (String)programMap.get("objectId");

			DomainObject projectTemplateObj = DomainObject.newInstance(context);
			projectTemplateObj.setId(strProjectTemplateId);
			StringList slBusSelect = new StringList();
			slBusSelect.add(SELECT_ID);
			slBusSelect.add(ResourcePlanTemplate.SELECT_IS_ANY_SUBTASK_PHASE);
			StringList selectRelStmts = new StringList();
			selectRelStmts.add(DomainConstants.SELECT_RELATIONSHIP_ID);
			selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_NAME);
			selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_DESC);
			selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_STATE);
			selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_LAST_MODIFIED_DATE);

			MapList resourcePlanTemplateList = projectTemplateObj.getRelatedObjects(context,
					ResourcePlanTemplate.RELATIONSHIP_RESOURCE_PLAN_TEMPLATE,  //String relPattern
					DomainConstants.TYPE_PROJECT_TEMPLATE, //String typePattern
					slBusSelect,          //StringList objectSelects,
					selectRelStmts,                     //StringList relationshipSelects,
					false,                     //boolean getTo,
					true,                     //boolean getFrom,
					(short)1,                 //short recurseToLevel,
					"",          //String objectWhere,
					"",                       //String relationshipWhere,
					0,
					null,                     //Pattern includeType,
					null,                     //Pattern includeRelationship,
					null);

			return resourcePlanTemplateList;
		}
		catch (Exception exp) 
		{
			exp.printStackTrace();
			throw new MatrixException(exp);
		}
	}        



	@com.matrixone.apps.framework.ui.ConnectionProgramCallable
	public static Map addNewResourceRequest(Context context,String[] args) throws MatrixException
	{   		
		Map doc = new HashMap();
		MapList mlItems = new MapList();
		String strTemp=null;
		Map returnMap = null;

		String strLanguage=context.getSession().getLanguage();

		try{
			ContextUtil.startTransaction(context, true);
			Map programMap = (Map)JPO.unpackArgs(args);
			Map paramMap = (Map)programMap.get("paramMap");
			String objectId = (String) paramMap.get("objectId");
			String strParentOID = (String) paramMap.get("parentOID");
			com.matrixone.jdom.Element elm = (com.matrixone.jdom.Element) programMap.get("contextData");
			MapList chgRowsMapList = com.matrixone.apps.framework.ui.UITableIndented.getChangedRowsMapFromElement(context, elm);
			String rowFormat = DomainConstants.EMPTY_STRING;
			String strRelId = (String) paramMap.get("relId");
			for (int i = 0; i < chgRowsMapList.size(); i++) 
			{
				try 
				{
					Map changedRowMap = (Map) chgRowsMapList.get(i);
					String childObjectId = (String) changedRowMap.get("childObjectId");
					String sRelId = (String) changedRowMap.get("relId");
					String sRowId = (String) changedRowMap.get("rowId");					
					rowFormat = "[rowId:" + sRowId + "]";
					String sRelTypeSymb = (String) changedRowMap.get("relType");
					String markup = (String) changedRowMap.get("markup");
					Map columnsMap = (Map) changedRowMap.get("columns");
					String strProjectRole = (String) columnsMap.get("ProjectRole");
					String strOrganizationId = (String)columnsMap.get("ResourcePool");
					String strStandardCost = (String)columnsMap.get("StandardCost");
					String strName = (String) columnsMap.get("Name");
					// [MODIFIED::Dec 29, 2010:s4e:R211:IR-Number::Start] 
					ProgramCentralUtil programCentralUtil = new ProgramCentralUtil(); 
					String strBadChars = programCentralUtil.checkBadNameChar(context, strName,true);
					if(strBadChars.length()>0)
					{
						String sErrMsg1 = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								"emxProgramCentral.Common.InvalidCharacters", strLanguage);
						String sErrMsg2 = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								"emxProgramCentral.Common.RemoveInvalidCharacter", strLanguage);
						StringBuffer sErrMsg= new StringBuffer(sErrMsg1);
						sErrMsg.append("\n");
						sErrMsg.append(strBadChars);
						sErrMsg.append("\n");
						sErrMsg.append(sErrMsg2);
						throw new MatrixException(sErrMsg.toString());
					}
					// [MODIFIED::Dec 29, 2010:s4e:R211:IR-Number::End] 
					Map mapPhaseFTE = new HashMap();
					int noOfNonZeroColumns = 0;
					for (Iterator iterator = columnsMap.keySet().iterator(); iterator.hasNext();) 
					{
						String strColumnName = (String) iterator.next();
						if(strColumnName.contains("PhaseOID-"))
						{
							String strPhaseFTEValue = (String)columnsMap.get(strColumnName);
							if(ProgramCentralUtil.isNotNullString(strPhaseFTEValue)){
								try{
									String strPhaseId = strColumnName.substring(strColumnName.indexOf("-")+1,strColumnName.length());
									double tempVar = Task.parseToDouble(strPhaseFTEValue);
									if(tempVar < 0){
										throw new NumberFormatException();
									}if(tempVar!=0.0){
										noOfNonZeroColumns++;
									}
									mapPhaseFTE.put(strPhaseId,tempVar);
					            }
								catch (NumberFormatException e){
									String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
											"emxProgramCentral.ResourceRequest.SelectNumericFTE", strLanguage);
					                throw new MatrixException(sErrMsg);
					            }
							}
						}
					}
					if(noOfNonZeroColumns==0)
					{
						String sErrMsg = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								"emxProgramCentral.ResourceRequest.PhaseNonZeroValue", strLanguage);
		                                throw new MatrixException(sErrMsg);
					}
					// use date-time stamp for revision
					DomainObject domainObject = newInstance(context);
					String revision = domainObject.getUniqueName(DomainConstants.EMPTY_STRING);
					String strResourceRequestId = "";
					
					if(ProgramCentralUtil.isNotNullString(strName))
					{
					domainObject.createObject(context,
							TYPE_RESOURCE_REQUEST,
							strName,
							revision,
							POLICY_RESOURCE_REQUEST,
							context.getVault().getName());
						strResourceRequestId = domainObject.getId();
					}
					else
					{
						strResourceRequestId = FrameworkUtil.autoName(context,
								"type_ResourceRequest",
								"policy_ResourceRequest");	
					}
					DomainObject dmoResourceRequest = DomainObject.newInstance(context,strResourceRequestId);					
					dmoResourceRequest.setAttributeValue(context,DomainConstants.ATTRIBUTE_PROJECT_ROLE,strProjectRole);
					final String ATTRIBUTE_STANDARD_COST = PropertyUtil.getSchemaProperty(context, "attribute_StandardCost");
					String costCurrency = Currency.getBaseCurrency(context, strParentOID);
					if(ProgramCentralUtil.isNullString(strStandardCost)){
						strStandardCost ="0.0";
					}else{
						strStandardCost = Currency.toBaseCurrency(context, strParentOID, strStandardCost, false);
					}
					dmoResourceRequest.setAttributeValue(context,ATTRIBUTE_STANDARD_COST,strStandardCost + ProgramCentralConstants.SPACE + costCurrency);
			           String sCommandStatement = "add connection $1 fromrel $2 to $3";
			           String mqlCommand = MqlUtil.mqlCommand(context, sCommandStatement,ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE,strRelId,dmoResourceRequest.getId(context)); 			        
					Date currDate = new Date();
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
					String strCurrDate = simpleDateFormat.format(currDate);
					DomainRelationship dmrResourcePlanTemplate = DomainRelationship.newInstance(context, strRelId);
					Map attrValueMap = new HashMap();
					attrValueMap.put(ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_LAST_MODIFIED_DATE,strCurrDate);
					dmrResourcePlanTemplate.setAttributeValues(context, attrValueMap);
					if(strOrganizationId!= null && !"".equals(strOrganizationId) && !"null".equals(strOrganizationId) )
					{
						DomainObject dmoResourcePool = DomainObject.newInstance(context,
								strOrganizationId);
						String strResourcePoolRel = PropertyUtil.getSchemaProperty(context,
						"relationship_ResourcePool");
						DomainRelationship dmrResourcePool= DomainRelationship.connect(context,dmoResourceRequest,strResourcePoolRel,dmoResourcePool);
					}
					for(Iterator iterator = mapPhaseFTE.keySet().iterator(); iterator.hasNext();)
					{
						String strPhaseId = (String)iterator.next();
						Double nFTE = (Double)mapPhaseFTE.get(strPhaseId);
						DomainObject dmoPhase = DomainObject.newInstance(context,
								strPhaseId);
						String strPhaseFTERel = PropertyUtil.getSchemaProperty(context,
						"relationship_PhaseFTE");
						DomainRelationship dmrResourceReqPhase= DomainRelationship.connect(context,dmoResourceRequest,strPhaseFTERel,dmoPhase);
						dmrResourceReqPhase.setAttributeValue(context, ATTRIBUTE_FTE, String.valueOf(nFTE));
					}
					// creating a returnMap having all the details about the changed row.						
					returnMap = new HashMap();
					returnMap.put("oid", dmoResourceRequest.getId());
					returnMap.put("rowId", sRowId);
					returnMap.put("pid", strParentOID);
					returnMap.put("relId", strRelId);
					returnMap.put("markup", "new");
					returnMap.put("columns", columnsMap);
					// creating a returnMap having all the details about the changed row.						
					mlItems.add(returnMap);
					ContextUtil.commitTransaction(context);
				}
				catch(Exception ex)
				{				
					ContextUtil.abortTransaction(context);
					doc.put("Action", "ERROR"); // If any exeception is there send "Action" as "ERROR"
					doc.put(STRING_MESSAGE, ex.getMessage()); // Error message to Display
					throw new MatrixException(ex);
				}
			}
			doc.put("Action", "success");
			doc.put("changedRows", mlItems);
		   
        }
        catch (Exception ex)
        {
			throw new MatrixException(ex.getMessage());
		}
		return doc;		
	}

	/** This method fetches the name column data for PMCResourcePlanTemplateSummaryTable 
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public Vector getColumnResourcePlanTemplateName(Context context, String[] args)
	throws Exception {
    	
    	try {
			Vector vecResult = new Vector();
			final String ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_NAME = "attribute["+ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_NAME+"]";
			Map programMap = (Map) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");		
			Map paramList = (Map) programMap.get("paramList");
			
			String strLanguage=context.getSession().getLanguage();
			String strHeaderValue = ":" + EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.ResourcePlanTemplate", strLanguage);
			String strSubHeader =  EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.ResourceRequestTemplate", strLanguage);
			strHeaderValue = strHeaderValue+"&subHeader="+strSubHeader;
			String strPrinterFriendly = (String) paramList.get("reportFormat");
			String strResourcePlanName = null;
			boolean isPrinterFriendly = strPrinterFriendly != null;
			boolean hasPhaseSubTask = true;
			Map mapRowData = null;
			StringBuffer columnName = null;
			String strColumnName = ""; 
			String strColumnId = "";
			String strColumnType = ""; 
			String strRelId = "";
			String strParenOId = "";
			String strobjectId = "";
			String strHasPhaseSubTask = "";
			DomainRelationship dmrResourcePlanTemplate = null;
			for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext();) 
			{
				columnName = new StringBuffer();
				mapRowData = (Map) itrObjects.next();
				strColumnId = (String) mapRowData.get(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_ID);
				strRelId = (String) mapRowData.get(SELECT_RELATIONSHIP_ID);
				dmrResourcePlanTemplate  = DomainRelationship.newInstance(context,strRelId);
				StringList relSelect = new StringList();
	            relSelect.add(DomainRelationship.SELECT_FROM_ID);
				strResourcePlanName = dmrResourcePlanTemplate.getAttributeValue(context, ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_NAME);
				strColumnName = strResourcePlanName;
	            Map mapRelData              = dmrResourcePlanTemplate.getRelationshipData(context,relSelect);
                StringList slRelFromId      = (StringList)mapRelData.get(DomainRelationship.SELECT_FROM_ID);
                strParenOId= (String)slRelFromId.get(0);
                strobjectId = strParenOId;
				strHasPhaseSubTask = (String)mapRowData.get("from["+RELATIONSHIP_SUBTASK+"]");
				hasPhaseSubTask = "true".equalsIgnoreCase(strHasPhaseSubTask)?true:false;
					columnName.append("<a href=\"JavaScript:showDialog('");
					columnName.append(FrameworkUtil
									.encodeURL("../common/emxIndentedTable.jsp?table=PMCResourcePlanTemplateRequestSummaryTable&" +
											"freezePane=Name&selection=multiple&HelpMarker=emxhelpresourcefortemplateview&suiteKey=ProgramCentral&" +
											"toolbar=PMCResourceRequestTemplateToolBar&header="+XSSUtil.encodeForURL(context,strResourcePlanName)+
											"&postProcessJPO=emxResourceRequestBase:postProcessRefreshTable&program=emxResourceRequest:getTableResourcePlanTemplateRequestData" +
											"&mode=insert&objectId="
											+ XSSUtil.encodeForURL(context,strobjectId) + "&relId="+XSSUtil.encodeForURL(context,strRelId)+"&parentOID="+XSSUtil.encodeForURL(context,strParenOId)));
					if(hasPhaseSubTask) {
						columnName.append(FrameworkUtil.encodeURL("&connectionProgram=emxResourcePlanTemplate:addNewResourceRequest"));
						ProjectTemplate projectTemplate = (ProjectTemplate)DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_TEMPLATE, DomainObject.PROGRAM);
				 		boolean isCtxUserOwnerOrCoOwner = projectTemplate.isOwnerOrCoOwner(context, strParenOId);
						
						if (isCtxUserOwnerOrCoOwner) {
							columnName.append(FrameworkUtil.encodeURL("&editLink=true"));
						}
					} else {
						columnName.append(FrameworkUtil.encodeURL("&editLink=false"));
					}
					columnName.append("')\">");
					columnName.append(XSSUtil.encodeForHTML(context,strColumnName));
					columnName.append("</a>");
			
				if(!isPrinterFriendly){
				vecResult.add(columnName.toString());
				}else if ("HTML".equals(strPrinterFriendly)){
					vecResult.add("<label>"+XSSUtil.encodeForHTML(context,strColumnName)+"</label>");
				}else{
					vecResult.add(XSSUtil.encodeForHTML(context,strColumnName));
				}
			}
			return vecResult;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new MatrixException(ex);
		}    	
    }	

	/** This method disconnects the selected Resource plan template/s from parent and deletes its related Resource Request 
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void deleteResourcePlanTemplate(Context context, String[] args)  throws Exception 
	{
		try 
		{
			Map paramMap = (Map) JPO.unpackArgs(args);
			String strRelId = (String)paramMap.get("relId");
			String strObjectId = (String)paramMap.get("ObjectId");
			ResourcePlanTemplate resourcePlanTemplate = new ResourcePlanTemplate();
			StringList slRequestList = resourcePlanTemplate.getRequestIdListFromResourcePlanTemplate(context,new String[]{strRelId});			
			DomainRelationship relDom= DomainRelationship.newInstance(context, strRelId);
			StringList objectSelects = new StringList();
			for (Iterator itrTableRows = slRequestList.iterator(); itrTableRows.hasNext();)
        	{
				String strRequestId=(String)itrTableRows.next();
				deleteResourceRequestFromTemplate(context, strRequestId);
			}
			DomainRelationship.disconnect(context, strRelId);			
		} catch (Exception exp) {
			exp.printStackTrace();
			throw exp;
		}
	}

	/**This method deletes the Resource Request Objects of id strRequestId. 
	 * @param context
	 * @param itrTableRows
	 * @throws FrameworkException
	 * @throws Exception
	 */
	private void deleteResourceRequestFromTemplate(Context context,
			String strRequestId) throws FrameworkException, Exception {
		
		DomainObject domObj=DomainObject.newInstance(context,strRequestId);
		domObj.deleteObject(context);
	}

	public void deleteResourceRequest(Context context, String[] args)  throws Exception 
	{
		try 
		{
			Map paramMap = (Map) JPO.unpackArgs(args);
			String strObjectId = (String)paramMap.get("ObjectId");
			DomainObject reqObject = DomainObject.newInstance(context,strObjectId);
			String strResourcePlanTemplateid = reqObject.getInfo(context,"to[Resource Request Plan Template].fromrel.id");
			Date currDate = new Date();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
			String strCurrDate = simpleDateFormat.format(currDate);
			DomainRelationship dmrResourcePlanTemplate = DomainRelationship.newInstance(context, strResourcePlanTemplateid);
			Map attrValueMap = new HashMap();
			attrValueMap.put(ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_LAST_MODIFIED_DATE,strCurrDate);
			dmrResourcePlanTemplate.setAttributeValues(context, attrValueMap);
			deleteResourceRequestFromTemplate(context, strObjectId);
		} 
		catch (Exception exp) 
		{
			exp.printStackTrace();
			throw exp;
		}
	}
	//End Added:4-Jun-2010:di1:R210 PRG:Advanced Resource Planning
	
	/**
	 * @param context
	 * @param strProjectId
	 * @param strResourcePlanTemplateIds
	 * @throws MatrixException
	 */
	public void createResourcePlanFromResourcePlanTemplate (Context context, String[] args) throws MatrixException
	{
		try {
			Map paramMap = (Map) JPO.unpackArgs(args);
			String strProjectId = (String)paramMap.get("ProjectId");
			String strResourcePlanTemplateId = (String)paramMap.get("ResourceTemplateId");
			String[] strResourcePlanTemplateIds = new String[]{strResourcePlanTemplateId};
			String numberofPeopleUnit=EnoviaResourceBundle.getProperty(context, "emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
			final String SELECT_RESOURCE_POOL_ID = "from["+RELATIONSHIP_RESOURCE_POOL+"].to.id";
			//get all request from resource plan template
			FTE fte = FTE.getInstance(context);
			FTE ftePlan = FTE.getInstance(context);
			DomainObject projectObj = DomainObject.newInstance(context,strProjectId);
			Map mapProjectPhaseInfoDetails = ResourcePlanTemplate.getProjectPhaseInfo(context, strProjectId,true);
			StringList slRequestIdList = ResourcePlanTemplate.getRequestIdListFromResourcePlanTemplate(context, strResourcePlanTemplateIds);
			String[]strRequestIds = new String[slRequestIdList.size()];
			slRequestIdList.copyInto(strRequestIds);
			StringList busSelect = new StringList();
			busSelect.add(SELECT_ID);
			busSelect.add(SELECT_NAME);
			busSelect.add(SELECT_RESOURCE_POOL_ID);
			busSelect.add(ResourcePlanTemplate.SELECT_ATTRIBUTE_STANDARD_COST);
			busSelect.add(ResourcePlanTemplate.SELECT_ATTRIBUTE_STANDARD_COST_UNIT);
			busSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
			busSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_NAME);			
			busSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_SEQUENCE_ORDER);
			busSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_TASK_WBS);
			busSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
			BusinessObjectWithSelectList resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,strRequestIds,busSelect);
			String strRequestId = null;
			String strRequestName = null;
			BusinessObjectWithSelect bows = null;
			Date reqStartDate = null;
			Date reqEndDate = null;
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
			for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
			{
				ftePlan = FTE.getInstance(context);
				bows = itr.obj();
				strRequestId = bows.getSelectData(SELECT_ID);
				strRequestName = bows.getSelectData(SELECT_NAME);
				DomainObject domainObject = DomainObject.newInstance(context,strRequestId);
				String revision = domainObject.getUniqueName(DomainConstants.EMPTY_STRING);
				BusinessObject newBusObject = domainObject.cloneObject(context, strRequestName, revision, null);
				DomainObject newRequestObj = DomainObject.newInstance(context,newBusObject.getObjectId());
				String strReqStandardCost = bows.getSelectData(ResourcePlanTemplate.SELECT_ATTRIBUTE_STANDARD_COST);
	    		String strRequestCostCurrency= bows.getSelectData(ResourcePlanTemplate.SELECT_ATTRIBUTE_STANDARD_COST_UNIT);
	    		newRequestObj.setAttributeValue(context,ResourcePlanTemplate.ATTRIBUTE_STANDARD_COST,strReqStandardCost+" "+strRequestCostCurrency);			
				DomainRelationship dmrResourcePlan = DomainRelationship.connect(context, projectObj, RELATIONSHIP_RESOURCE_PLAN, newRequestObj);
				StringList slPhaseIdList = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
				StringList slPhaseNameList = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_NAME);
				StringList slPhaseTaskWBS = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_TASK_WBS);
				for(int i=0; i<slPhaseIdList.size(); i++)
				{
					fte = FTE.getInstance(context);
					String strPhaseId = (String)slPhaseNameList.get(i);
					double nFTE = Double.parseDouble((String)(bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE)).get(i));
					String strTaskWBS = (String)slPhaseTaskWBS.get(i);
					Object phaseInfoObject = mapProjectPhaseInfoDetails.get(strTaskWBS);
					
					if (phaseInfoObject != null) {
						
						Map mapPhaseInfo = (Map)phaseInfoObject;
						String strProjPhaseId = (String)mapPhaseInfo.get(SELECT_ID);
						long lProjPhaseDuration = Long.parseLong((String)mapPhaseInfo.get("PhaseDuration"));
						Date startDate = (Date)mapPhaseInfo.get(ResourcePlanTemplate.ATTRIBUTE_PHASE_START_DATE);
						Date endDate = (Date)mapPhaseInfo.get(ResourcePlanTemplate.ATTRIBUTE_PHASE_FINISH_DATE);
						MapList mlTimeFrameList = (MapList)mapPhaseInfo.get("TimeFrameList");
						DomainObject phaseObj = DomainObject.newInstance(context,strProjPhaseId);
						String strPhaseFTERel = PropertyUtil.getSchemaProperty(context,"relationship_PhaseFTE");
						DomainRelationship dmrResourceReqPhase= DomainRelationship.connect(context,newRequestObj,strPhaseFTERel,phaseObj);
						StringList slMonthYear = new StringList();
						Map mapObject = new HashMap();
						double nPerDayFTE = nFTE;
						dmrResourceReqPhase.setAttributeValue(context, ATTRIBUTE_FTE, String.valueOf(nPerDayFTE));
						ResourceRequest resourceRequest = new ResourceRequest();
			    		Map mapFTE = resourceRequest.calculateFTE(context, startDate, endDate, nPerDayFTE+"");
			    		fte.setAllFTE(mapFTE);
						ftePlan = ResourcePlanTemplate.getCalculatedFTEMap(fte, ftePlan);
						if(null==reqStartDate ||(null!=reqStartDate && reqStartDate.after(startDate)))
						{
							reqStartDate = startDate; 
						}
						if(null==reqEndDate||(null!=reqEndDate && reqEndDate.before(endDate)))
						{
							reqEndDate = endDate;
						}
					}
				}
				String stringStartDate  = simpleDateFormat.format(reqStartDate);
				String stringFinishDate = simpleDateFormat.format(reqEndDate);
				newRequestObj.setAttributeValue(context,DomainConstants.ATTRIBUTE_START_DATE, stringStartDate);
				newRequestObj.setAttributeValue(context,DomainConstants.ATTRIBUTE_END_DATE, stringFinishDate);
				dmrResourcePlan.setAttributeValue(context, ATTRIBUTE_FTE, ftePlan.getXML());
				String attribute_ResourcePlanPreference = (String)PropertyUtil.getSchemaProperty(context,"attribute_ResourcePlanPreference");
				projectObj.setAttributeValue(context,attribute_ResourcePlanPreference, ProjectSpace.TYPE_PHASE);
			}
			
		} 
		catch (NumberFormatException e) 
		{
			throw new MatrixException(e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MatrixException(e);
		}	
	}
	
	/**
	 * Gets resource plan table data for resource request
	 * 
	 * @param context The Matrix Context object
	 * @param args Packed program and request maps for the table
	 * @return MapList containing all table data
	 * @throws Exception if operation fails
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAllActiveResourcePlanTemplates(Context context, String[] args) throws Exception
	{
		try
		{
			//Get ObjectId which is of Project Template
			//Get All the phases of the template.
			//Take all the phases in stringlist.
			boolean blflag = false;
			MapList mlFinalResourcePlanList = new MapList();
			Map programMap = (Map)JPO.unpackArgs(args);
			StringList slProjTempPhaseList = new StringList();
			String strProjectTemplateId = (String)programMap.get("objectId");
			StringList slBusSelects = new StringList();
			slBusSelects.add(SELECT_NAME);
			DomainObject dmoProject = DomainObject.newInstance(context, strProjectTemplateId);
			MapList mlPhaseList = ResourcePlanTemplate.getPhasesForResourceRequestView(context,
					dmoProject, slBusSelects);
			if(!mlPhaseList.isEmpty())
			{
				for(int nCount=0;nCount<mlPhaseList.size();nCount++)
				{
					Map PhaseMap = (Map)mlPhaseList.get(nCount);
					slProjTempPhaseList.add((String)PhaseMap.get(SELECT_NAME));					
				}
			}
			MapList mlResourcePlanList = new MapList();
			StringList slResourcePlanTemplateIds= new StringList();
			String strResourcePlanTemplateIds = null;
			String SELECT_PHASE_NAME= "from.from["+RELATIONSHIP_SUBTASK+"].to.name";
			String SELECT_PHASE_TYPE= "from.from["+RELATIONSHIP_SUBTASK+"].to.type";
			Object oPhaseName = null;
			BusinessObjectWithSelectList resourcePlanObjWithSelectList = null;
			BusinessObjectWithSelect bows = null;
			
			String relationshipName =  PropertyUtil.getSchemaProperty(context,"relationship_ResourcePlanTemplate");
			
			//PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
	           String sCommandStatement = "query connection type $1 where $2 select $3 dump $4";
	           String sWhereCluase = ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_STATE+"~~Active";
	           String relIds = MqlUtil.mqlCommand(context, sCommandStatement,relationshipName,sWhereCluase,"id","|"); 
	        //PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End
	           
			StringList slRelIds=FrameworkUtil.splitString(relIds, "Resource Plan Template|");
			
			if(!slRelIds.isEmpty())
			{
				String strRelId= null;
				String[] arrStrResourcePlanTemplateIds;
				arrStrResourcePlanTemplateIds = new String[slRelIds.size()-1];

				for(int nCount=1;nCount<slRelIds.size();nCount++)
				{
					strRelId = slRelIds.get(nCount).toString().trim();				
					arrStrResourcePlanTemplateIds[nCount-1]=strRelId;				
				}	
				StringList selectRelStmts = new StringList();
				selectRelStmts.add("from.id");
				selectRelStmts.add(DomainConstants.SELECT_ID);
				selectRelStmts.add(DomainConstants.SELECT_TYPE);
				selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_NAME);
				selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_DESC);
				selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_STATE);
				selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_LAST_MODIFIED_DATE);
				selectRelStmts.add(SELECT_PHASE_NAME);
				selectRelStmts.add(SELECT_PHASE_TYPE);

				mlResourcePlanList = DomainRelationship.getInfo(context, arrStrResourcePlanTemplateIds, selectRelStmts);
				for(int i=0; i<mlResourcePlanList.size();i++)
				{
					StringList slTempList = new StringList(slProjTempPhaseList);
					
					blflag = false;
					Map resourcePlanMap = (Map)mlResourcePlanList.get(i);
					String objRelId = (String)resourcePlanMap.get(DomainConstants.SELECT_ID);
					oPhaseName = resourcePlanMap.get(SELECT_PHASE_NAME);
					Object objPhaseName= (Object)resourcePlanMap.get(SELECT_PHASE_NAME);
					StringList slPhaseName= new StringList();
					StringList slPhaseType= new StringList();
					if(oPhaseName instanceof StringList)
					{
						slPhaseName= (StringList)resourcePlanMap.get(SELECT_PHASE_NAME);
						slPhaseType= (StringList)resourcePlanMap.get(SELECT_PHASE_TYPE);
					}
					else if (oPhaseName instanceof String)
					{
						String strPhaseName= (String)resourcePlanMap.get(SELECT_PHASE_NAME);
						slPhaseName.add(strPhaseName);
						slPhaseType.add((String)resourcePlanMap.get(SELECT_PHASE_TYPE));
					}
					//Modified:10-Dec-2010:vf2:R211 PRG:IR-074093
					StringList slPhaseNm = new StringList();
					for(int nIndex=0 ;nIndex<slPhaseName.size();nIndex++)
					{
						if((ProjectSpace.TYPE_PHASE.equals((String)(slPhaseType.get(nIndex)))))
						{
							//slPhaseName.remove(nIndex);
							slPhaseNm.add((String)(slPhaseName.get(nIndex)));
						}
						
					}
					slPhaseName =slPhaseNm;
					//End:10-Dec-2010:vf2:R211 PRG:IR-074093
					int nCounter=0;
					if(slTempList.size()>=slPhaseName.size())//Prject template phase listsize)
							{								
						for(int nCount=0; nCount<slPhaseName.size(); nCount++)
						{
							String strPhaseName = (String)slPhaseName.get(nCount);
							if(slTempList.contains(strPhaseName))
							{				
								int nIndex = slTempList.indexOf(strPhaseName);
								slTempList.remove(nIndex);
								nCounter++;
							}							
							//if any one not found
							//and then dont add into new maplist();
						}
						if((slPhaseName.isEmpty())||(slPhaseName.size()==nCounter))
						{
							blflag = true;
						}

					}
					else{
						blflag = false;
					}
					
					if(blflag==true)
					{
						String objTemplateId = (String)resourcePlanMap.get("from.id");
						resourcePlanMap.put(DomainConstants.SELECT_ID, objTemplateId);
						resourcePlanMap.put(DomainConstants.SELECT_RELATIONSHIP_ID, objRelId);
						mlFinalResourcePlanList.add(resourcePlanMap);
					}
				}
			}

			return mlFinalResourcePlanList;
		}
		catch (Exception exp) 
		{
			throw new MatrixException(exp);
		}
	}  
	
	/*
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void cloneResourcePlanTemplate(Context context, String[] args)  throws Exception 
	{			
			try
			{
				ContextUtil.startTransaction(context,true);
				Map paramMap = (Map) JPO.unpackArgs(args);
				String strLanguage=context.getSession().getLanguage();
				String strResourcePlanTemplateId = (String)paramMap.get("strResourcePlanTemplateId");
				String strProjectTempalteId = (String)paramMap.get("strProjectTempalteId");
				String[] arrStrResourcePlanTemplateIds;	
				String[] arrResourceRequestId;    
				StringList slRequestIdList= new StringList();
			    StringList slRequestNameList= new StringList();
			StringList slRequestCostList= new StringList();
			StringList slRequestCostCurrencyList= new StringList();
				String relationshipType=  PropertyUtil.getSchemaProperty(context,"relationship_ResourcePlanTemplate");
				String SELECT_RESOURCE_REQUEST_VAULT= "frommid["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].to.vault";
				StringList selectRelStmts = new StringList();
				
				MapList mlResourcePlanInfoList = new MapList();	
				Map attrValueMap = new HashMap();
			        arrStrResourcePlanTemplateIds = new String[1];
			        arrStrResourcePlanTemplateIds[0]=strResourcePlanTemplateId;
			        DomainObject projTemplateDo=DomainObject.newInstance(context,strProjectTempalteId);
			        DomainRelationship dom = new DomainRelationship(strResourcePlanTemplateId);
			        selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_NAME);
			        selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_DESC);
			        selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_STATE);
			        selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_LAST_MODIFIED_DATE);
			        selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_ID);
			        selectRelStmts.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_NAME);
			selectRelStmts.add("frommid["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].to."+ResourcePlanTemplate.SELECT_ATTRIBUTE_STANDARD_COST);
			selectRelStmts.add("frommid["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].to."+ResourcePlanTemplate.SELECT_ATTRIBUTE_STANDARD_COST_UNIT);
			        selectRelStmts.add(SELECT_RESOURCE_REQUEST_VAULT);        
			        mlResourcePlanInfoList = dom.getInfo(context, arrStrResourcePlanTemplateIds, selectRelStmts);
			        
			        for(int i=0; i<mlResourcePlanInfoList.size();i++)
			        {
			            Map resourcePlanMap = (Map)mlResourcePlanInfoList.get(i);
			            Object objRequestId= (Object)resourcePlanMap.get(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_ID);
			            if(objRequestId instanceof StringList)
			            {
			            	slRequestIdList = (StringList)resourcePlanMap.get(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_ID);
			                slRequestNameList= (StringList)resourcePlanMap.get(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_NAME);
					slRequestCostList = (StringList)resourcePlanMap.get("frommid["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].to."+ResourcePlanTemplate.SELECT_ATTRIBUTE_STANDARD_COST);
					slRequestCostCurrencyList = (StringList)resourcePlanMap.get("frommid["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].to."+ResourcePlanTemplate.SELECT_ATTRIBUTE_STANDARD_COST_UNIT);
			            }
			            else if (objRequestId instanceof String)
			            {
			            	slRequestIdList.add((String)resourcePlanMap.get(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_ID));
			                slRequestNameList.add((String)resourcePlanMap.get(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_NAME));
					slRequestCostList.add((String)resourcePlanMap.get("frommid["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].to."+ResourcePlanTemplate.SELECT_ATTRIBUTE_STANDARD_COST));
					slRequestCostCurrencyList.add((String)resourcePlanMap.get("frommid["+ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE+"].to."+ResourcePlanTemplate.SELECT_ATTRIBUTE_STANDARD_COST_UNIT));
			            }
			            String strResourcePlanName=(String)resourcePlanMap.get(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_NAME);
			            String strResourcePlanDesc=(String)resourcePlanMap.get(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_DESC);
			            String strResourcePlanState=(String)resourcePlanMap.get(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_STATE);
			            String strResourcePlanLastModDate=(String)resourcePlanMap.get(ResourcePlanTemplate.SELECT_RESOURCE_PLAN_TEMPLATE_LAST_MODIFIED_DATE);

                        //NX5 - #6568 - Name prefix on Resource Plan not wanted for Project Template Clone
                        String isProjectTemplateVersion = (String)paramMap.get("isProjectTemplateVersion");
                        if (UIUtil.isNullOrEmpty(isProjectTemplateVersion) || !"true".equalsIgnoreCase(isProjectTemplateVersion)) {
				strResourcePlanName = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
						"emxProgramCentral.Common.CopyOf", strLanguage) + " " + strResourcePlanName;
                        }
                        // NX5 - Template Versioning mod end

			            attrValueMap.put(ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_NAME,strResourcePlanName);
			            attrValueMap.put(ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_DESC,strResourcePlanDesc);
			            attrValueMap.put(ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_STATE,strResourcePlanState);
			            Date currDate = new Date();
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(),Locale.US);
			            String strCurrDate = simpleDateFormat.format(currDate);        
			            attrValueMap.put(ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_LAST_MODIFIED_DATE,strCurrDate);  
			        }        
			        DomainRelationship newResourcePlanTemplate = DomainRelationship.connect(context,projTemplateDo,relationshipType,projTemplateDo);
			        newResourcePlanTemplate.setAttributeValues(context, attrValueMap);
			        
			        if(null!=slRequestIdList && !slRequestIdList.isEmpty())
			        {
			        	for(int nCount=0;nCount<slRequestIdList.size();nCount++)
			        	{
			        		DomainObject reqDob =DomainObject.newInstance(context,slRequestIdList.get(nCount).toString());      		
			        		arrResourceRequestId = new String[1];
			                arrResourceRequestId[0]=slRequestIdList.get(nCount).toString();
			                StringList slPhaseSelect = new StringList();
			        		slPhaseSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_NAME);
			        		slPhaseSelect.add(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
			        		BusinessObjectWithSelectList resourceRequestObjWithSelectList = new BusinessObjectWithSelectList();
			                BusinessObjectWithSelect bows = null;
			                resourceRequestObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context,arrResourceRequestId,slPhaseSelect);
			        		String strResourceRequestName= (String)slRequestNameList.get(nCount);
			        		String revision = reqDob.getUniqueName(DomainConstants.EMPTY_STRING);
					BusinessObject reqCloneDob =reqDob.cloneObject(context,strResourceRequestName,revision,null);
			        		String strReqCloneId = reqCloneDob.getObjectId();
			        		DomainObject newReqCloneDob = DomainObject.newInstance(context,strReqCloneId);
					String strReqStandardCost = (String)slRequestCostList.get(nCount);
		    		String strRequestCostCurrency= (String)slRequestCostCurrencyList.get(nCount);
		    		newReqCloneDob.setAttributeValue(context,ResourcePlanTemplate.ATTRIBUTE_STANDARD_COST,strReqStandardCost+" "+strRequestCostCurrency);			
					//PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:start
					String sCommandStatement = "add connection $1 fromrel $2 to $3";
					String mqlCommand = MqlUtil.mqlCommand(context, sCommandStatement,ResourcePlanTemplate.RELATIONSHIP_RESOURCE_REQUEST_PLAN_TEMPLATE,newResourcePlanTemplate.getName(),strReqCloneId);
					//PRG:RG6:R213:Mql Injection:parameterized Mql:20-Oct-2011:End

			                StringList slBusSelects = new StringList();
			                slBusSelects.add(DomainConstants.SELECT_NAME);
			                slBusSelects.add(DomainConstants.SELECT_ID);
			                MapList mlPhaseList = ResourcePlanTemplate.getPhasesForResourceRequestView(context,
			                		projTemplateDo, slBusSelects);
			                StringList slProjTempPhaseNameList = new StringList();
			                StringList slProjTempPhaseIdList = new StringList();
			                if(!mlPhaseList.isEmpty())
			                {
			                    for(int index=0;index<mlPhaseList.size();index++)
			                    {
			                        Map PhaseMap = (Map)mlPhaseList.get(index);
			                        slProjTempPhaseNameList.add((String)PhaseMap.get(DomainConstants.SELECT_NAME));     
			                        slProjTempPhaseIdList.add((String)PhaseMap.get(DomainConstants.SELECT_ID));
			                    }
			                }
			                if(!resourceRequestObjWithSelectList.isEmpty())
			                {                	
			                	for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(resourceRequestObjWithSelectList); itr.next();)
			                    {
			                        bows = itr.obj();
			                		StringList slPhaseName = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_NAME);
			                		StringList slPhaseFTE = bows.getSelectDataList(ResourcePlanTemplate.SELECT_RESOURCE_REQUEST_PHASE_FTE);
			                		for(int i=0; i<slPhaseName.size(); i++)
			                		{
			                			  String strPhaseName = (String)slPhaseName.get(i); 
			                			  int nPhaseIndex = slProjTempPhaseNameList.indexOf(strPhaseName);
			        					if(nPhaseIndex>=0)
			        					{
			                			  String strPhaseId = (String)slProjTempPhaseIdList.get(nPhaseIndex);
			        						String strtempPhaseName = (String)slProjTempPhaseNameList.get(nPhaseIndex);
			        						slProjTempPhaseIdList.remove(strPhaseId);
			        						slProjTempPhaseNameList.remove(strtempPhaseName);
			        						
			                			  DomainObject phaseDob = DomainObject.newInstance(context,strPhaseId);
			                			  String strPhaseFTE = (String)slPhaseFTE.get(i);
			        						StringList slRequestPhaseList = newReqCloneDob.getInfoList(context, ResourcePlanTemplate.SELECT_RESOURCE_REQUSET_PHASE_ID);
			        						if(!slRequestPhaseList.contains(strPhaseId))
			        						{
			                			  DomainRelationship relReqPhase = DomainRelationship.connect(context,newReqCloneDob,ResourcePlanTemplate.RELATIONSHIP_PHASE_FTE,phaseDob);
			                			  relReqPhase.setAttributeValue(context, DomainConstants.ATTRIBUTE_FTE,strPhaseFTE);
			        						}
			        					}
			                		}
			                	}
			                }
			        	}
			        }       
			    ContextUtil.commitTransaction(context);
			} 
			catch(Exception ex) {
				ContextUtil.abortTransaction(context);
			    ex.printStackTrace();
			 } 
	} 
			
	 /**
	 * This Method updates the resource template name in edit mode.
      * 
      * @param context The Matrix Context Object
      * @param args The Packed arguments String array 
      * @throws Exception if operation fails
      * 
      */
     public void updateResourcePlanName(Context context, String[] args) throws Exception 
     {
         try{
             //This map gives the  updated FTE values
             Map programMap                      = (Map) JPO.unpackArgs(args);
             Map mpParamMap                      = (Map)programMap.get("paramMap");
             String strRequestPlanNameNewValue   = (String)mpParamMap.get("New Value");
    		 String strRequestPlanNameOldValue   = (String)mpParamMap.get("Old Value");
    		 if(null!=strRequestPlanNameNewValue && !"".equals(strRequestPlanNameNewValue) && !"null".equalsIgnoreCase(strRequestPlanNameNewValue))
    		 {
    			 if(!strRequestPlanNameNewValue.equals(strRequestPlanNameOldValue)){
		             String strRequestPlanId  = (String)mpParamMap.get("relId");
		             DomainRelationship domRelObj = DomainRelationship.newInstance(context, strRequestPlanId);		
		             domRelObj.setAttributeValue(context,ResourcePlanTemplate.ATTRIBUTE_RESOURCE_PLAN_TEMPLATE_NAME,strRequestPlanNameNewValue);
    			 }
    		 }
         }catch (Exception exp) {
			// TODO: handle exception
             throw exp;
		}
	} 
    
	/**
	 * This method returns true if logged-in user is template owner and template has
	 * at-least a Phase and FTEFilter value is 'FTE'. else returns false.
	 * 
     * @param	context 
     * 			The ENOVIA <code>Context</code> object
	 * @param 	argumentArray
	 * 			String Array which holds template related information.
	 * 
	 * @return  true if logged-in user is template owner and template has
	 * 			at-least a Phase and FTEFilter value is 'FTE'. else returns false.
	 * 
     * @throws 	MatrixException		
	 * 			MatrixException can be thrown in case of method fail to execute.
	 */
	public boolean canCreateResourceRequest(Context context,String[] argumentArray) throws MatrixException {
		
		boolean hasAccess = false;
		
		StringList busSelectList = new StringList();
		busSelectList.add(SELECT_ID);
		
		String busWhere = EMPTY_STRING;
		String relWhere = EMPTY_STRING;
		boolean getFromSide = false;
		boolean getToSide = true;
		
		StringList relSelectList = new StringList();
		
		try {
			Map paramterMap = JPO.unpackArgs(argumentArray);
			String projectTemplateId = (String)paramterMap.get("parentOID");
			String FTEFilterValue    = (String)paramterMap.get("PMCResourcePlanTemplateCostFTEFilter");
			
			ProjectTemplate projectTemplate = new ProjectTemplate(projectTemplateId);
			String numberofPeopleUnit = EnoviaResourceBundle.getProperty(context,
										"emxProgramCentral.ResourcePlan.NumberofPeopleUnit");
	 		boolean isCtxUserOwnerOrCoOwner = projectTemplate.isOwnerOrCoOwner(context, projectTemplateId);
			
			if(ProgramCentralUtil.isNullString(FTEFilterValue) || FTEFilterValue.equalsIgnoreCase(numberofPeopleUnit)) {
				
				MapList templatePhaseMapList = projectTemplate.getRelatedObjects(context,RELATIONSHIP_SUBTASK,
													  							 ProgramCentralConstants.TYPE_PHASE,
													  							 busSelectList,relSelectList,
													  							 getFromSide,getToSide,(short) 1,
													  							 busWhere,relWhere,0);
				
				if (!templatePhaseMapList.isEmpty() && isCtxUserOwnerOrCoOwner) {
					hasAccess = true;
				}
			}
			
			return hasAccess;
			
		} catch(Exception exception) {
			throw new MatrixException(exception);
		}
	}
	
		
	/**
	 * Inherits ownership for Resource Requests from it's Parent Project Template.
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public boolean triggerCreateInheritOwnershipForResourceRequest(Context context, String[] args) throws Exception  
	{
		String fromId = args[0]; //fromId itself is a RelationshipID.
		String toId = args[1];
		final String SELECT_FROM_ID = "from.id";

		try{
			MapList mapList = DomainRelationship.getInfo(context, new String[]{fromId}, new StringList(SELECT_FROM_ID));
			Map map = (Map)mapList.get(0);
			fromId = (String) map.get(SELECT_FROM_ID);
			
			DomainAccess.createObjectOwnership(context, toId, fromId, DomainAccess.COMMENT_MULTIPLE_OWNERSHIP);
			
			return true;
		}catch(Exception e){
			throw e;
		}
	}
	
	
}
