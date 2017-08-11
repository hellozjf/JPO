/*   emxEffortManagementBase
 **
 **   Copyright (c) 2003-2016 Dassault Systemes.
 **   All Rights Reserved.
 */

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.Dimension;
import matrix.db.ExpansionWithSelect;
import matrix.db.JPO;
import matrix.db.RelationshipWithSelectItr;
import matrix.db.Unit;
import matrix.db.UnitItr;
import matrix.db.UnitList;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.Task;


/**
 * The <code> emxEffortManagementBase</code> class contains code for the emxEffortManagement.
 *
 * @version PMC 10.5.1.2 - Copyright (c) 2004, MatrixOne, Inc.
 */

public class emxEffortManagementBase_mxJPO
{

	protected static final String DURATION_UNIT_HOURS = "h";
	protected static final String DURATION_UNIT_DAYS = "d";
	public static final String STATE_TASK_COMPLETE =
			PropertyUtil.getSchemaProperty("policy",
					DomainConstants.POLICY_PROJECT_TASK,
					"state_Complete");
	//Added:10-June-2010:vf2:R210 PRG:IR-056512
	protected static final String TYPE_EFFORT = 
			PropertyUtil.getSchemaProperty("type_Effort" );
	protected static final String RELATIONSHIP_EFFORTS = 
			PropertyUtil.getSchemaProperty("relationship_Effort");
	protected static final String RELATIONSHIP_WEEKLY_TIMESHEET = 
			PropertyUtil.getSchemaProperty("relationship_WeeklyTimesheet");
	public static final String TYPE_WEEKLY_TIMESHEET = 
			PropertyUtil.getSchemaProperty("type_WeeklyTimesheet");
	public static final String ATTRIBUTE_EFFORT_SUBMISSION = 
			PropertyUtil.getSchemaProperty("attribute_EffortSubmission");  	
	/** effort object state "Exists". */	
	protected static final String STATE_EFFORT_EXISTS = "Exists";	

	/** effort object state "Approved". */	
	protected static final String STATE_EFFORT_APPROVED = "Approved";
	/** effort object state "Submit". */	
	protected static final String STATE_EFFORT_SUBMIT = "Submit";	
	//End:10-June-2010:vf2:R210 PRG:IR-056512

	/**
	 * Converts the given duration value in default unit into the required duration unit
	 * If the attribute "Task Estimated Duration" is not associated with any Dimension object
	 * then passed duration value is returned back.
	 * If the required unit is not from one of the defined units in the Dimension then passed
	 * duration value is returned back.
	 * 
	 * @param context The Matrix Context object
	 * @param valueInDefaultUnit The duration value in default unit defined in Dimension object
	 *                           attached to "Task Estimated Duration" object.
	 * @param strRequiredUnit The unit name in which the value is to be converted
	 * @return The converted value
	 */
	protected double getDenormalizedDuration (Context context, double valueInDefaultUnit, String strRequiredUnit) {
		double dblResult = valueInDefaultUnit;

		UnitList _units = new UnitList();
		AttributeType attrType = new AttributeType(DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION);
		try
		{
			Dimension dimension = attrType.getDimension(context);
			if (dimension != null) 
			{
				_units = dimension.getUnits(context);
			}
		}
		catch (Exception e)
		{
			_units = new UnitList();
		}

		if (_units.size() > 0)
		{
			Unit unit = null;
			for (UnitItr unitItr = new UnitItr (_units); unitItr.next(); )
			{
				unit = unitItr.obj();
				if (unit.getName().equals(strRequiredUnit))
				{
					dblResult = Task.parseToDouble(unit.denormalize("" + valueInDefaultUnit));
					break;
				}
			}
		}

		return dblResult;
	}



	/**
	 * Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object.
	 * @param args holds no arguments.
	 * @throws Exception if the operation fails.
	 * @since PMC 10.5.1.2
	 */

	public emxEffortManagementBase_mxJPO (Context context, String[] args)
			throws Exception
			{
			}

	/**
	 * This method is executed if a specific method is not specified.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @return an integer: 0 for success and non-zero for failure
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public int mxMain(Context context, String[] args)
			throws Exception
			{
		if (true)
		{
			throw new Exception("must specify method on emxWorkCalendar invocation");
		}
		return 0;
			}

	protected static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

	/**
	 * Return the name of the task and the efforts submitted
	 * for that task
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *    objectId - the context task object
	 * @return Vector containing the name of task and effort
	 *    objects as string
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getName(Context context, String args[])
			throws Exception
			{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		HashMap paramList    = (HashMap) programMap.get("paramList");
		//Added:10-June-2010:vf2:R210 PRG:IR-056512
		String selectedTable = (String) paramList.get("selectedTable");
		//End:10-June-2010:vf2:R210 PRG:IR-056512
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if ( strPrinterFriendly != null ) {
			isPrinterFriendly = true;
		}
		String attribute_CriticalTask = (String)PropertyUtil.getSchemaProperty(context,"attribute_CriticalTask");
		Iterator objectListIterator = objectList.iterator();
		Vector columnValues = new Vector(objectList.size());
		com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);

		while (objectListIterator.hasNext())
		{
			Map objectMap = (Map) objectListIterator.next();
			String taskId = (String) objectMap.get(DomainObject.SELECT_ID);
			String taskLevel = (String) objectMap.get(DomainObject.SELECT_LEVEL);
			DomainObject taskObj  = DomainObject.newInstance(context, taskId);
			boolean blDeletedTask = false;
			try{
				taskObj.open(context);
			}catch (FrameworkException ex) {
				throw ex;
			}
			String strName = taskObj.getName();


			String taskObjType = (String)taskObj.getInfo(context,DomainConstants.SELECT_TYPE);
			if(taskObjType.equalsIgnoreCase(DomainConstants.TYPE_PERSON) && ProgramCentralUtil.isNullString(strName)){
				strName = (String)objectMap.get(person.SELECT_LAST_NAME)+","+(String)objectMap.get(person.SELECT_FIRST_NAME);
			}
			//strName = FrameworkUtil.findAndReplace(strName,"&","&amp;");
			String encodedName = XSSUtil.encodeForXML(context,strName);


			if(taskObj.hasRelatedObjects(context,DomainConstants.RELATIONSHIP_DELETED_SUBTASK,false)) {
				blDeletedTask = true;
			}
			String critcalTask = taskObj.getAttributeValue(context,attribute_CriticalTask);
			String sState = (String)taskObj.getInfo(context,DomainConstants.SELECT_CURRENT);
			StringBuffer sBuff = new StringBuffer();
			//Added:09-May-09:nr2:R207:PRG:Bug :371521
			//Check if the task comes from Project Template
			String parentId = null;
			String menuLink = "";
			boolean fromProjTemp = false;
			parentId = Task.getParentProject(context,taskObj);

			if(parentId!=null){
				if((DomainObject.newInstance(context, parentId).getInfo(context,DomainConstants.SELECT_TYPE)).equals(DomainConstants.TYPE_PROJECT_TEMPLATE)){
					menuLink = "&amp;treeMenu=type_TaskTemplate";
					fromProjTemp = true;
				}
			}
			//End:R207:PRG:Bug :371521               
			//  Display critical path only for not completed
			if (blDeletedTask){
				sBuff.append("<font color='red'>");
				sBuff.append(encodedName);
				sBuff.append("</font>");
			}else if( critcalTask!=null && sState!=null &&
					critcalTask.equalsIgnoreCase("true") &&
					!sState.equalsIgnoreCase(STATE_TASK_COMPLETE)) {
				if(!isPrinterFriendly){
					sBuff.append("<a href ='javascript:showModalDialog(\"");
					sBuff.append("../common/emxTree.jsp?objectId=");
					sBuff.append(taskId);
					//Added:09-May-09:nr2:R207:PRG:Bug :371521                        
					if(fromProjTemp){
						sBuff.append(menuLink);
					}
					//End:R207:PRG:Bug :371521
					//<!--Modified for the Bug No: 349125 0 02/06/2008 Start-- >
					sBuff.append("\", \"875\", \"550\", \"false\", \"popup\")' title=\""+encodedName+"\" style=\"color:red\">");
					//<!--Modified for the Bug No: 349125 0 02/06/2008 End-- >
					sBuff.append(encodedName);
				} 
				else {
					sBuff.append("<font color='red'>");
					sBuff.append(encodedName);
					sBuff.append("</font>");
				}

				if(!isPrinterFriendly){
					sBuff.append("</a>");
				}
			} 
			else{
				if(!isPrinterFriendly){
					sBuff.append("<a href ='javascript:showModalDialog(\"");
					sBuff.append("../common/emxTree.jsp?objectId=");
					sBuff.append(taskId);
					//Added:09-May-09:nr2:R207:PRG:Bug :371521                        
					if(fromProjTemp){
						sBuff.append(menuLink);
					}                        
					//End:R207:PRG:Bug :371521
					//<!--Modified for the Bug No: 349125 0 02/06/2008 Start-- >
					sBuff.append("\", \"875\", \"550\", \"false\", \"popup\")' title=\""+encodedName+"\">");
					//<!--Modified for the Bug No: 349125 0 02/06/2008 End-- >
				}
				sBuff.append(encodedName);
				if(!isPrinterFriendly){
					sBuff.append("</a>");
				}
			}
			//Added:PRG:I16:R213:08-Dec-2011:IR-100320V6R2013 Start
			if(taskObj.isKindOf(context, DomainConstants.TYPE_PERSON) && "PMCProjectTaskEffort".equals(selectedTable)==true)
			{					
				if( strName!=null &&!"null".equals(strName) ) {				
					String strUserFullName = PersonUtil.getFullName(context, strName);
					StringBuffer sbPersonLink = new StringBuffer();
					sbPersonLink.append("<a href=\"JavaScript:showModalDialog('../common/emxTree.jsp?objectId=");
					sbPersonLink.append(taskId);
					sbPersonLink.append("','700','600','false','popup')\">");                    
					sbPersonLink.append(XSSUtil.encodeForHTML(context,strUserFullName));
					sbPersonLink.append("</a>");	
					columnValues.add(sbPersonLink.toString());
				}else {
					columnValues.add("");
				}
			} 
			else {			
				//Added:PRG:I16:R213:08-Dec-2011:IR-100320V6R2013 End
				columnValues.add(sBuff.toString());
			}
		}
		return columnValues;
			}

	/**
	 * Gets all the checkbox and disables the checkbox for those
	 * effort objects which are in Approved state
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *    objectId - the context effort object
	 * @return Vector containing string true or false
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getCheckBox(Context context, String args[])
			throws Exception
			{

		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		com.matrixone.apps.common.Person person = null;
		String objectId = (String) paramMap.get("objectId");
		String jsTreeID=(String) paramMap.get("jsTreeID");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		String type_effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String state_Approved = ProgramCentralConstants.STATE_EFFORT_APPROVED;
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		String sRetValue = "";
		String sType = "";
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			if (id != null && id.trim().length() > 0 )
			{
				DomainObject bus  = DomainObject.newInstance(context, id);
				String sState = bus.getInfo(context,"current");
				try{
					bus.open(context);
				}catch (FrameworkException Ex) {
					throw Ex;
				}
				sType = bus.getTypeName();
				// If the effort object is in approved state disable the checkbox
				if(sType != null && sType.equalsIgnoreCase(type_effort) && !sState.equalsIgnoreCase(state_Approved)){
					columnValues.add("true");
				}
				else{

					columnValues.add("false");

				}
			}

		}
		return columnValues;
			}

	/**
	 * Return the planned effort for the task and effort object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *    objectId - the context task object
	 * @return Vector containing the planned effort as string
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getPlanEffort(Context context, String args[])
			throws Exception
			{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String objectId = (String) paramMap.get("objectId");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");

		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";

		String sType = "";
		String sRetValue = "";
		String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String type_effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String attribute_Duration=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedDuration");
		String attribute_TaskFinishDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedFinishDate");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String[] saEffortIds = new String[relBusObjPageList.size()];
		//Added:14-June-2010:vf2:R210 PRG:IR-056396
		try {      
			ContextUtil.pushContext(context);
			//End:14-June-2010:vf2:R210 PRG:IR-056396

			for (int i = 0; i < relBusObjPageList.size(); i++)
			{
				sRetValue="";
				String isTotalRow = (String)((Map)relBusObjPageList.get(i)).get("isTotalRow");	
				if(isTotalRow!=null && "true".equals(isTotalRow)) {
					columnValues.add("");
				} else {        
					//Commented by vf2 on 19-May-2010
					//id =(String)((HashMap)relBusObjPageList.get(i)).get("id");
					//Added by vf2 on 19-May-2010
					id =(String)((Map)relBusObjPageList.get(i)).get("id");
					//end by vf2 on 19-May-2010
					if (id != null && id.trim().length() > 0 )
					{
						DomainObject bus  = DomainObject.newInstance(context, id);
						try{
							bus.open(context);
						}catch (FrameworkException Ex) {


							//throw Ex;

						}
						sType = bus.getTypeName();
						if(sType != null && sType.equalsIgnoreCase(type_effort)){
							sRetValue=" ";
							Vector taskVec = getEffortIds(context,id,relPattern,type_TaskManagement,true,false,(short)1);
							String sTaskId = (String)taskVec.get(0);
							//modified by ms9 start
							/*DomainObject domObj = DomainObject.newInstance(context,sTaskId);
							if(domObj.isKindOf(context, "Phase"))
							{
								columnValues.add("");                  //Commented by I9Q
							}*/
							//else
							//{
							//modified by ms9 end
							DomainObject boTask  = DomainObject.newInstance(context, sTaskId);
							try{
								boTask.open(context);
							}catch (FrameworkException Ex) {
								throw Ex;
							}

							/*Double dur = new Double(boTask.getAttributeValue(context,attribute_Duration));*/
							Double dur = null;

							//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
							String sCommandStatement = "print bus $1 select $2 dump";
							String resultinHours =  MqlUtil.mqlCommand(context, sCommandStatement,sTaskId, "attribute["+attribute_Duration+"].unitvalue[h]");
							//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End
							// Task Duration Issue Ends

							String fdate = boTask.getAttributeValue(context,attribute_TaskFinishDate);
							HashMap hmap = getAllocationData(context,sTaskId,id);
							HashMap AssgnedDateMap = getAssignedDateData(context,sTaskId,id);
							if(AssgnedDateMap!=null)
							{
								String assigneddate = (String)AssgnedDateMap.get("assigneddate");
								if(assigneddate!=null && assigneddate.trim().length() > 0 )
								{
									long long_dur = DateUtil.computeDuration(new Date(assigneddate),new Date(fdate));

									if(long_dur == 0 )
										long_dur=1;

									String longStr = ""+long_dur;
									dur = new Double(longStr);
								}

							}
							//Added:19-March-10:ms9:R209:PRG: IR-045404V6R2011
							double d_item =0;
							double alloc=0;
							if(hmap!=null && hmap.size()>0) {
								String allo = (String)hmap.get("allocation");
								if(allo!=null && allo.trim().length() > 0 && resultinHours!=null && resultinHours.trim().length() > 0 )
								{

										Double allocation = new Double(Task.parseToDouble(allo));

									alloc = allocation.doubleValue()/100;

									d_item= Task.parseToDouble(resultinHours); 

								}
								sRetValue = sRetValue + (d_item * alloc); //(no of days)*(%percentage)
								//End:ms9:R209:PRG:IR-045404V6R2011
								//}
								if(sRetValue.contains("."))
								{
									String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
									sRetValue = sRetValue.replace('.',separator.charAt(0));
								}
								columnValues.add(sRetValue);
							}
						}
						//Added:PRG:I16:R213:08-Dec-2011:IR-100320V6R2013 Start
						else if(sType != null && sType.equalsIgnoreCase(DomainConstants.TYPE_PERSON))
						{
							String sEffortIds = (String)((Map)relBusObjPageList.get(i)).get("effortIds");
							StringList slEffortIds = FrameworkUtil.splitString(sEffortIds,",");
							String sEffortId="";
							Iterator irt = slEffortIds.iterator();
							while(irt.hasNext())
							{
								sEffortId = (String)irt.next();
								sRetValue="";
								Vector taskVec = getEffortIds(context,sEffortId,relPattern,type_TaskManagement,true,false,(short)1);
								String sTaskId = (String)taskVec.get(0);                	  
								/*DomainObject domObj = DomainObject.newInstance(context,sTaskId);
								if(domObj.isKindOf(context, "Phase"))
								{
									columnValues.add("");            //Commented by I9Q
								}*/
								//else
								//{  
								DomainObject boTask  = DomainObject.newInstance(context, sTaskId);
								try{
									boTask.open(context);
								}catch (FrameworkException Ex) {
									throw Ex;
								}
								Double dur = null;

								//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
								String sCommandStatement = "print bus $1 select $2 dump";
								String resultinHours =  MqlUtil.mqlCommand(context, sCommandStatement,sTaskId, "attribute["+attribute_Duration+"].unitvalue[h]");
								//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End
								// Task Duration Issue Ends

								String fdate = boTask.getAttributeValue(context,attribute_TaskFinishDate);
								HashMap hmap = getAllocationData(context,sTaskId,sEffortId);
								HashMap AssgnedDateMap = getAssignedDateData(context,sTaskId,sEffortId);
								if(AssgnedDateMap!=null)
								{
									String assigneddate = (String)AssgnedDateMap.get("assigneddate");
									if(assigneddate!=null && assigneddate.trim().length() > 0 )
									{
										long long_dur = DateUtil.computeDuration(new Date(assigneddate),new Date(fdate));
										if(long_dur == 0 )
											long_dur=1;

										String longStr = ""+long_dur;
										dur = new Double(longStr);
									}
								}
								double d_item =0;
								double alloc=0;
								if(hmap!=null && hmap.size()>0) {
									double dblValue=0.0; 
									String allo = (String)hmap.get("allocation");            			  
									if(allo!=null && allo.trim().length() > 0 && resultinHours!=null && resultinHours.trim().length() > 0 )
									{            				  
										Double allocation = new Double(allo);
										alloc = allocation.doubleValue()/100;
										d_item= Task.parseToDouble(resultinHours);
									} 
									dblValue = dblValue + (d_item * alloc);     
									sRetValue = Double.toString(dblValue);
								}
								//}
							            	
							if(sRetValue.contains("."))
							{
								String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
								sRetValue = sRetValue.replace('.',separator.charAt(0));
						
                                                                   }
							}            	
							columnValues.add(sRetValue);
						}
						//Added:PRG:I16:R213:08-Dec-2011:IR-100320V6R2013 End
						else{
							if(bus.isKindOf(context, type_TaskManagement)){
								double valRet = getTaskPlannedEffort (context, id);
								//Added:PRG:R213:I16:IR-084709V6R2013 Start
								NumberFormat numberFormat = NumberFormat.getInstance();
								numberFormat.setMaximumFractionDigits(2);
								String strRoundedValue = numberFormat.format(valRet);
								//If Rounded value contains , then parseDouble fails. To remove comma used nf.parse() 
								Number num = numberFormat.parse(strRoundedValue);
								strRoundedValue = num.toString();
								double roundedValue = 0;
								if (strRoundedValue != null && !"".equals(strRoundedValue)) {
									roundedValue = Task.parseToDouble(strRoundedValue);
								}
								String sRet = "" + roundedValue;
								//Added:PRG:R213:I16:IR-084709V6R2013 End
								//String sRet= ""+ Math.round(valRet);
								if(sRet.contains("."))
								{
									String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
									sRet = sRet.replace('.',separator.charAt(0));
								}
								columnValues.add(sRet);
							}else{
								columnValues.add("");
							}

						}

					}
				}
			}
			//Added:14-June-2010:vf2:R210 PRG:IR-056396
		} catch(Exception e) {
			throw new Exception(e.getMessage());
		} finally {
			ContextUtil.popContext(context);
		}  
		//End:14-June-2010:vf2:R210 PRG:IR-056396          
		return columnValues;
			}


	/**
	 * Gets the planned effort for an effort object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param effortId is the effort object Id
	 * @return double containing the planned effort value
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public double getPlannedEffort(Context context, String effortId)
			throws Exception
			{
		double valRet = 0;
		String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String attribute_Duration=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedDuration");
		Vector taskVec = getEffortIds(context,effortId,relPattern,type_TaskManagement,true,false,(short)1);

		String sTaskId = (String)taskVec.get(0);
		DomainObject boTask  = DomainObject.newInstance(context, sTaskId);
		try{
			boTask.open(context);
		}catch (FrameworkException Ex) {
			throw Ex;
		}
	//	Double dur = new Double(boTask.getAttributeValue(context,attribute_Duration));
		Double dur = (Double) Task.parseToDouble(boTask.getAttributeValue(context,attribute_Duration));
		HashMap hmap = getAllocationData(context,sTaskId,effortId);
		MapList ml = (MapList)getAssigneeInfo(context,sTaskId);

		// Commented for Bug fix 296282, refered the emxProgramCentralWBSEffortDialog.jsp getPlannedEffort()method
		// for calculated the efforts based on allocation
		//int totalAssingees = ml.size();
		//if(totalAssingees==0)
		//  totalAssingees=1;
		if(hmap!=null && hmap.size()>0) {
		//	Double allocation = new Double((String)hmap.get("allocation"));
			Double allocation = (Double)Task.parseToDouble((String)hmap.get("allocation"));
			double alloc = allocation.doubleValue()/100;
			//valRet = Math.round((dur.doubleValue() * alloc * 8)/totalAssingees);
			//valRet = dur.doubleValue() * alloc * 8;
			valRet = getDenormalizedDuration(context, (dur.doubleValue() * alloc), DURATION_UNIT_HOURS);
		}
		return valRet;
			}

	/**
	 * Gets the planned effort for a task based on the percentage
	 * allocation of task assignees
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param taskId is the task Id
	 * @return doulble containing the planned effort value
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public double getTaskPlannedEffort (Context context, String taskId)
			throws Exception
			{
		HashMap ret_alloc = new HashMap();

		String type_Person=(String)PropertyUtil.getSchemaProperty(context,"type_Person");
		String rel_assigned=(String)PropertyUtil.getSchemaProperty(context,"relationship_AssignedTasks");
		String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");
		String attribute_Duration=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedDuration");
		String attribute_TaskFinishDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedFinishDate");
		Vector taskVec = getAllocation(context,taskId,rel_assigned,type_Person,true,false,(short)1);
		DomainObject bus = DomainObject.newInstance(context, taskId);

		bus.open(context);
		String task_duration = bus.getAttributeValue(context,attribute_Duration);

		//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:start
		String sCommandStatement = "print bus $1 select $2 dump";
		String resultinHours =  MqlUtil.mqlCommand(context, sCommandStatement,taskId, "attribute["+attribute_Duration+"].unitvalue[h]");
		//PRG:RG6:R213:Mql Injection:parameterized Mql:18-Oct-2011:End

		// Task Duration Issue Ends
		String fdate = bus.getAttributeValue(context,attribute_TaskFinishDate);
		double result = 0;

		if(taskVec.size()==0)
		{

			return (Task.parseToDouble(resultinHours)); 

		}
		//Added:10-Mar-10:vm3:R209:PRG:Bug 010074
		double dArr[] = new double[taskVec.size()];
		for(int ind=0;ind<dArr.length;ind++){
			dArr[ind]=0;         
		}
		double dTemp = 0.0;
		for (int i = 0; i < taskVec.size(); i++)
		{

			double d_item =0;
			HashMap hm = (HashMap)taskVec.get(i);
			String sid = (String)hm.get("id");
			String allo = (String)hm.get("allocation");
			//String strAllocation = "";
			String strAll = "100";
			if (!allo.equals(strAll))
			{ 
				double iDuration = 0;
				double iPercentAllocation = 0;
				double iAllocation=0;
				iDuration = Task.parseToDouble(task_duration.trim());
				iPercentAllocation = Task.parseToDouble(allo.trim());
				if(iPercentAllocation >0){
					iAllocation = (((8 * iDuration)/100)*iPercentAllocation);	        	 
				}
				resultinHours = iAllocation + "";
				dTemp = Task.parseToDouble(resultinHours);
				dArr[i] = dTemp;
			}

			if(allo!=null && allo.trim().length() > 0 && resultinHours!=null && resultinHours.trim().length() > 0 )
			{


				//Double allocation = new Double(allo);
				Double allocation  = Task.parseToDouble(allo);
				double alloc = allocation.doubleValue()/100;


				d_item= Task.parseToDouble(resultinHours); 

			}

			//result = result + d_item;
			result = result + dArr[i];
			//End-Added:10-Mar-10:vm3:R209:PRG:Bug 010074
		}

		return result;
			}

	/**
	 * Gets the total planned effort for the task based on its duration
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param taskId is the task Id
	 * @return doulble containing the planned effort value
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public double getPlanTask(Context context, String taskId)
			throws Exception
			{

		String attribute_Duration=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedDuration");
		DomainObject bo  = DomainObject.newInstance(context, taskId);
		bo.open(context);
		String sRetValue = bo.getAttributeValue(context,attribute_Duration);
		Double dblValue = new Double(sRetValue);
		double intDuration = dblValue.doubleValue();
		//double valRet = intDuration*8;
		double valRet = getDenormalizedDuration(context, intDuration, DURATION_UNIT_HOURS);
		return valRet;

			}

	/**
	 * Gets the percent allocation for each effort object
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id and relationship name
	 * @return Vector containing the percentage allocation value as string
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getEffortAllocation(Context context, String args[])
			throws Exception
			{

		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String objectId = (String) paramMap.get("objectId");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");

		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";

		String sType = "";
		String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String type_effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			String sRetValue = "";
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			if (id != null && id.trim().length() > 0 )
			{
				BusinessObject bo = new BusinessObject(id);
				try{
					bo.open(context);
				}catch (FrameworkException Ex) {
					throw Ex;
				}
				sType = bo.getTypeName();

				if(sType != null && sType.equalsIgnoreCase(type_effort)){
					sRetValue="0";
					Vector taskVec = getEffortIds(context,id,relPattern,type_TaskManagement,true,false,(short)1);

					String sTaskId = (String)taskVec.get(0);
					HashMap hdata = getAllocationData(context,sTaskId,id);                
					if(hdata != null && hdata.size()>0){
						sRetValue=(String)hdata.get("allocation");
						//Modified :31-Jan-11:vf2:R211:PRG: IR-093077V6R2012
						if(sRetValue!=null && sRetValue.trim().length()>0) {                    	 
							//Modified :9-May-11:MS9:R211:PRG: IR-102072V6R2012x 
							sRetValue = "" + new Double(sRetValue).doubleValue();
							//End :9-May-11:MS9:R211:PRG: IR-102072V6R2012x
						} 
						//End :31-Jan-11:vf2:R211:PRG: IR-093077V6R2012
					}
					columnValues.add(sRetValue + "%" );
				}
				else{
					columnValues.add(" ");
				}
			}
		}


		return columnValues;
			}

	/**
	 * Gets the assigned date for all assigned tasks
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param taskId is the task Id
	 * @param taskId is the effort Id
	 * @return HashMap containing the assigned date data
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public HashMap getAssignedDateData (Context context, String taskId,
			String effortId)
					throws Exception
					{

		HashMap ret_alloc = new HashMap();
		String type_Person=(String)PropertyUtil.getSchemaProperty(context,"type_Person");
		String rel_assigned=(String)PropertyUtil.getSchemaProperty(context,"relationship_AssignedTasks");
		String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");
		Vector taskVec = getAssignedDate(context,taskId,rel_assigned,type_Person,true,false,(short)1);
		DomainObject bus = DomainObject.newInstance(context, effortId);
		bus.open(context);
		String effort_originator = bus.getAttributeValue(context,attribute_Originator);
		for (int i = 0; i < taskVec.size(); i++)
		{
			HashMap hm = (HashMap)taskVec.get(i);
			String sid = (String)hm.get("id");
			String allo = (String)hm.get("assigneddate");
			if(sid!=null && sid.trim().length() > 0 )
			{
				DomainObject obj = DomainObject.newInstance(context, sid);
				obj.open(context);
				String person_name = obj.getName();
				if(effort_originator!=null && person_name !=null && effort_originator.equalsIgnoreCase(person_name))
				{
					return hm;
				}
			}
		}
		return ret_alloc;
					}

	/**
	 * Gets the assigned date for the task assignee
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param taskId is the task Id
	 * @param personid is the person Id
	 * @return HashMap containing the assigned date data
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public HashMap getAssignedDateDataPerson (Context context, String taskId,
			String personid)
					throws Exception
					{

		HashMap ret_alloc = new HashMap();
		String type_Person=(String)PropertyUtil.getSchemaProperty(context,"type_Person");
		String rel_assigned=(String)PropertyUtil.getSchemaProperty(context,"relationship_AssignedTasks");
		String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");
		Vector taskVec = getAssignedDate(context,taskId,rel_assigned,type_Person,true,false,(short)1);

		for (int i = 0; i < taskVec.size(); i++)
		{
			HashMap hm = (HashMap)taskVec.get(i);
			String sid = (String)hm.get("id");
			String allo = (String)hm.get("assigneddate");
			if(sid!=null && sid.trim().length() > 0 && sid.equalsIgnoreCase(personid))
			{
				return hm;
			}
		}

		return ret_alloc;
					}

	/**
	 * Gets the percentage allocation data for all the task assignees
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param taskId is the task Id
	 * @param taskId is the effort Id
	 * @return HashMap containing the allocation data
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public HashMap getAllocationData (Context context, String taskId,
			String effortId)
					throws Exception
					{

		HashMap ret_alloc = new HashMap();
		String type_Person=(String)PropertyUtil.getSchemaProperty(context,"type_Person");
		String rel_assigned=(String)PropertyUtil.getSchemaProperty(context,"relationship_AssignedTasks");
		String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");
		Vector taskVec = getAllocation(context,taskId,rel_assigned,type_Person,true,false,(short)1);
		DomainObject bus = DomainObject.newInstance(context, effortId);
		bus.open(context);
		String effort_originator = bus.getAttributeValue(context,attribute_Originator);
		for (int i = 0; i < taskVec.size(); i++)
		{
			HashMap hm = (HashMap)taskVec.get(i);
			String sid = (String)hm.get("id");
			String allo = (String)hm.get("allocation");
			if(sid!=null && sid.trim().length() > 0 )
			{
				DomainObject obj = DomainObject.newInstance(context, sid);
				obj.open(context);
				String person_name = obj.getName();
				//Modified :31-Jan-11:vf2:R211:PRG: IR-093077V6R2012                
				if(ProgramCentralUtil.isNotNullString(effort_originator) && ProgramCentralUtil.isNotNullString(person_name) 
						&& effort_originator.equalsIgnoreCase(person_name))	
				{
					return hm;
				}
				//End :31-Jan-11:vf2:R211:PRG: IR-093077V6R2012 	              
			}
		}

		return ret_alloc;
					}

	/**
	 * Gets the percentage allocation information of the efort originator
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param taskId is the task Id
	 * @param effort_originator is the name of the person
	 *    who has been assigned the task
	 * @return HashMap containing the allocation information
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public HashMap getAllocationInfo (Context context, String taskId,
			String effort_originator)
					throws Exception
					{

		HashMap ret_alloc = new HashMap();
		String type_Person=(String)PropertyUtil.getSchemaProperty(context,"type_Person");
		String rel_assigned=(String)PropertyUtil.getSchemaProperty(context,"relationship_AssignedTasks");
		String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");
		Vector taskVec = getAllocation(context,taskId,rel_assigned,type_Person,true,false,(short)1);

		for (int i = 0; i < taskVec.size(); i++)
		{
			HashMap hm = (HashMap)taskVec.get(i);
			String sid = (String)hm.get("id");
			String allo = (String)hm.get("allocation");
			if(sid!=null && sid.trim().length() > 0 )
			{
				DomainObject obj = DomainObject.newInstance(context, sid);
				obj.open(context);
				String person_name = obj.getName();
				if(effort_originator!=null && person_name !=null && effort_originator.equalsIgnoreCase(person_name))
				{
					return hm;

				}
			}
		}

		return ret_alloc;
					}

	/**
	 * Gets the total effort for the task objects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the total effort as string
	 * @throws Exception if the operation fails
	 * @since Added by vf2 for release version V6R2011x.
	 */
	public Vector getTotalTaskEffort(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		String strValue = "";
		String s_state = ProgramCentralConstants.STATE_EFFORT_APPROVED;
		for (int i = 1; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			String[] saEffortIds = new String[]{id};
			strValue = getTotalPendingEffort(context, s_state, true, saEffortIds);
			columnValues.add(strValue);                  
		}
		Vector columnValue = new Vector(relBusObjPageList.size()+1);
		double total = 0;
		for(int i=0;i<columnValues.size();i++){
			total = total + Task.parseToDouble((String)columnValues.get(i));
		}
		String sTotal= ""+ total;
		columnValue.add(sTotal);
		for(int j=0;j<columnValues.size();j++){
			columnValue.add(columnValues.get(j));
		}        
		return columnValue;
			}

	/**
	 * Gets the total effort for the task and effort objects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the total effort as string
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getTotalEffort(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		String strFilter = (String)paramMap.get("PMCWBSEffortFilter");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		String strValue = "";
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String attribute_TotalEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_TotalEffort");
		String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");        
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			if(bus.isKindOf(context, DomainConstants.TYPE_PERSON))
			{
				String sEffortIds = (String)((Map)relBusObjPageList.get(i)).get("effortIds");
				StringList slEffortIds = FrameworkUtil.splitString(sEffortIds,",");
				String[] saEffortIds = new String[slEffortIds.size()];
				slEffortIds.copyInto(saEffortIds);
				strValue = getTotalPendingEffort(context, strFilter, true, saEffortIds);
				//To show values on UI in correct Format
				if(strValue.contains("."))
				{
					String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
					strValue = strValue.replace('.',separator.charAt(0));
				}
				columnValues.add(strValue);
			}
			else
			{
				if(bus.isKindOf(context, type_TaskManagement))
				{
					double valRet=0.0;
					double valRetSub=0.0;
					double valRetRej=0.0;
					double valRetApp=0.0;
					if(ProgramCentralUtil.isNotNullString(strFilter) && ("All".equalsIgnoreCase(strFilter)))
					{
						valRetSub = getTotalTaskEffort(context, id,ProgramCentralConstants.STATE_EFFORT_SUBMIT);	
						valRetRej = getTotalTaskEffort(context, id, ProgramCentralConstants.STATE_EFFORT_REJECTED);
						valRetApp = getTotalTaskEffort (context, id, ProgramCentralConstants.STATE_EFFORT_APPROVED);
						valRet = valRetSub + valRetRej + valRetApp;
					}
					else if(ProgramCentralUtil.isNotNullString(strFilter) && ("Submitted".equalsIgnoreCase(strFilter)))
					{
						valRetSub = getTotalTaskEffort(context, id,ProgramCentralConstants.STATE_EFFORT_SUBMIT);	
						valRet = valRetSub;
					}
					else if(ProgramCentralUtil.isNotNullString(strFilter) && ("Approved".equalsIgnoreCase(strFilter)))
					{
						valRetApp = getTotalTaskEffort (context, id, ProgramCentralConstants.STATE_EFFORT_APPROVED);
						valRet = valRetApp;						
					}
					else if(ProgramCentralUtil.isNotNullString(strFilter) && ("Rejected".equalsIgnoreCase(strFilter)) )
					{
						valRetRej = getTotalTaskEffort(context, id, ProgramCentralConstants.STATE_EFFORT_REJECTED);
						valRet = valRetRej;
					}
					String sRet= Double.toString(valRet);								
					//To show values on UI in correct Format
					if(sRet.contains("."))
					{
						String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
						sRet = sRet.replace('.',separator.charAt(0));
					}
					columnValues.add(sRet);
				}else{
					columnValues.add("");
				}

			}
		}
		return columnValues;
			}

	/**
	 * Gets the total effort of a person
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param effortid is the effort object Id
	 * @param personname is the name of the person who submitted the effort
	 * @return double containing the total effort of the person
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public double getTotalPersonEffort(Context context, String effortid, String personname)
			throws Exception
			{
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String attribute_TotalEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_TotalEffort");
		Vector taskVec = getEffortIds(context,effortid,relPattern,type_TaskManagement,true,false,(short)1);
		String sTaskId = (String)taskVec.get(0);
		HashMap hm = getPersonEffortMapping(context, sTaskId);

		double intDouble = 0;
		Vector vSelect = new Vector();
		vSelect = (Vector)hm.get(personname);
		String effortID[] = new String[vSelect.size()];
		vSelect.copyInto(effortID);
		String strvalue = getTotalPendingEffort(context, ProgramCentralConstants.STATE_EFFORT_REJECTED, false, effortID);
		intDouble = Task.parseToDouble(strvalue);
		return intDouble;
			}

	/**
	 * Gets the total pending effort for the task
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param taskId is the task object Id
	 * @return double containing the total pending effort of the task
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	//	  public double getTotalTaskPendingEffort(Context context, String taskId)throws Exception
	//	  {
	//		  String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
	//		  String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
	//		  String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
	//		  String attribute_TotalEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_TotalEffort");
	//		  Vector effortIds = getEffortIds(context,taskId,relPattern,type_Effort,false,true,(short)1);
	//
	//		  String s_submitstate = ProgramCentralConstants.STATE_EFFORT_SUBMIT;
	//		  if(s_submitstate == null || s_submitstate.trim().length() == 0 ){
	//			  s_submitstate = "Submit";
	//		  }
	//		  double intDouble = 0;
	//		  String[] saEffortIds = new String[effortIds.size()];
	//		  effortIds.copyInto(saEffortIds);
	//		  intDouble = Task.parseToDouble(getTotalPendingEffort(context, s_submitstate,true, saEffortIds)); 
	//		  return intDouble;
	//	  }

	/**
	 * Gets the total remaining effort for the task
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param taskId is the task object Id
	 * @return double containing the total remaining effort of the task
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public double getTotalTaskRemainingEffort(Context context, String taskId)throws Exception
	{

		HashMap per_PersonEffort = getPersonEffortMapping( context, taskId);
		StringList lastest_efforts = getLatestUserEfforts( context, per_PersonEffort);
		String attribute_RemainingEffort = ProgramCentralConstants.ATTRIBUTE_REMAINING_EFFORT;
		double intDouble = 0;
		double db_reject = 0;

		for (int i = 0; i < lastest_efforts.size(); i++)
		{
			String id =(String)lastest_efforts.get(i);
			DomainObject bus  = DomainObject.newInstance(context, id);
			double dbItem = 0;
			String attribute_RemEffort = bus.getAttributeValue(context,attribute_RemainingEffort);
			String sState = FrameworkUtil.getCurrentState(context,bus).getName();
			if(attribute_RemEffort!=null && attribute_RemEffort.trim().length() > 0 )
			{
				//dbItem=new Double(attribute_RemEffort).doubleValue();
				dbItem = Task.parseToDouble(attribute_RemEffort);
			}

			intDouble = intDouble + dbItem + db_reject;
		}
		StringList non_submitted = getNonSubmittedEfforts ( context,per_PersonEffort,taskId);
		String attribute_Duration=ProgramCentralConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION;
		DomainObject boTask  = DomainObject.newInstance(context, taskId);
		//Double dur = new Double(boTask.getAttributeValue(context,attribute_Duration));
		Double dur = Task.parseToDouble(boTask.getAttributeValue(context,attribute_Duration));
		for ( int a = 0; a < non_submitted.size(); a++ )
		{
			String item =(String) non_submitted.get(a);
			HashMap itemMap = getAllocationInfo(context,taskId,item);
			if(itemMap!=null)
			{
				String attribute_TaskFinishDate=ProgramCentralConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE;
				HashMap AssgnedDateMap = getAssignedDateDataPerson(context, taskId, (String)itemMap.get("id"));
				String fdate = boTask.getAttributeValue(context,attribute_TaskFinishDate);
				Double act_dur = new Double(0);
				if(AssgnedDateMap!=null && AssgnedDateMap.size() > 0)
				{
					String assigneddate = (String)AssgnedDateMap.get("assigneddate");
					if(assigneddate!=null && !"null".equals(assigneddate) && assigneddate.trim().length() > 0 )
					{
						if(lastest_efforts!=null && lastest_efforts.size()>0)
						{

							long long_dur = DateUtil.computeDuration(new Date(assigneddate),new Date(fdate));
							act_dur = new Double(long_dur);
						}else{
							act_dur = dur;
						}
					}
					dur = act_dur;
				}
				Double allocation =(Double)Task.parseToDouble((String)itemMap.get("allocation"));
				double alloc = allocation.doubleValue()/100;
				double valRet = getDenormalizedDuration(context, (dur.doubleValue() * alloc), DURATION_UNIT_HOURS);
				intDouble = intDouble + valRet;
			}

		}

		return intDouble;
	}

	/**
	 * Gets the task assignee(s) name who have not submitted their efforts
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param hm is a HashMap containing the task assignees information
	 * @param taskId is the task object Id
	 * @return ArrayList containing the names of the task assignees as string
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public StringList getNonSubmittedEfforts(Context context, HashMap hm, String taskId)throws Exception
	{
		StringList ret_value = new StringList();
		MapList Assingees = (MapList)getAssigneeInfo(context, taskId);


		if(Assingees != null && Assingees.size() > 0)
		{
			for ( int a = 0; a < Assingees.size(); a++ )
			{

				String id = (String)((Map)Assingees.get(a)).get("id");;
				if( id != null && id.trim().length() > 0 )
				{
					DomainObject dom = DomainObject.newInstance(context, id);
					dom.open(context);
					String strname =(String) dom.getName();
					Vector vect = (Vector)hm.get(strname);
					if(vect==null || vect.size()==0)
					{
						ret_value.add(strname);

					}

				}


			}

		}

		return ret_value;

	}

	/**
	 * Gets the latest user efforts for the task
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param hm is a HashMap containing the task assignees information
	 * @return ArrayList containing the latest user efforts
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public StringList getLatestUserEfforts(Context context, HashMap hm)throws Exception
	{
		StringList ret_value = new StringList();
		if(hm != null)
		{
			Iterator keyItr = hm.keySet().iterator();
			while (keyItr.hasNext()){
				String latest = "" ;
				String name = (String) keyItr.next();
				Vector value = (Vector) hm.get(name);
				if(value!=null)
				{
					if(value.size()==1)
					{
						latest = (String) value.get(0);

					}else{

						latest = getLatestEffort(context,value);
					}

					if(latest != null && latest.trim().length() > 0 )
					{
						ret_value.add(latest);
					}

				}


			}

		}

		return ret_value; 
	}

	/**
	 * Takes the task and person name and gets the list of
	 * effortids for the person name
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param taskId is the task object Id
	 * @return HashMap containing the list of effortids
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public HashMap getPersonEffortMapping(Context context, String taskId)
			throws Exception
			{

		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String attribute_TotalEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_TotalEffort");
		String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");

		Vector effortIds = getEffortIds(context,taskId,relPattern,type_Effort,false,true,(short)1);
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		task.setId(taskId);
		StringList busSelects = new StringList(1);
		busSelects.add(task.SELECT_ID);
		StringList relSelects = new StringList(1);
		MapList maplist = new MapList();
		HashMap returnMap = new HashMap();
		HashMap mapItem = new HashMap();
		String personName=DomainObject.EMPTY_STRING;
		Vector vecItem = new Vector();
		maplist = (MapList)task.getAssignees(context, busSelects, relSelects, null);
		if(maplist.size()>0 && !task.isKindOf(context, ProgramCentralConstants.TYPE_PHASE))
		{
			for (int i = 0; i < maplist.size(); i++)
			{
				String id =(String)((Map)maplist.get(i)).get("id");
				DomainObject bus  = DomainObject.newInstance(context, id);
				bus.open(context);
				personName = bus.getName();
				bus.close(context);

				String fid = "";
				Vector vecNewItem = new Vector();
				HashMap hmPersonMap = new HashMap();
				for (int z = 0; z < effortIds.size(); z++)
				{
					fid =(String)effortIds.get(z);
					DomainObject bo  = DomainObject.newInstance(context, fid);
					bo.open(context);
					String originator = bo.getAttributeValue(context,attribute_Originator);
					bo.close(context);
					if(originator!=null && personName!=null && personName.equals(originator))
					{
						vecNewItem.addElement(fid);
						hmPersonMap.put(personName, vecNewItem);     
					}
				}
				returnMap.putAll(hmPersonMap);
			}
		}
		else//ADDED:P6E:18-Oct-2011:HF-134380V6R2011x
		{
			String fid = "";
			String originator="";
			for (int z = 0; z < effortIds.size(); z++)
			{
				fid =(String)effortIds.get(z);
				DomainObject dmoEffort  = DomainObject.newInstance(context, fid);
				dmoEffort.open(context);
				originator = dmoEffort.getAttributeValue(context,attribute_Originator);
				dmoEffort.close(context);

				if (returnMap.get(originator) == null) {
					vecItem.addElement(fid);
					returnMap.put(originator,vecItem);
				} else {
					((Vector)returnMap.get(originator)).addElement(fid);
				}
			}
		}////End:P6E:18-Oct-2011:HF-134380V6R2011x
		return returnMap;
			}

	/**
	 * Gets the pending effort for the task and effort objects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the pending effort
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getPendingEffort(Context context, String args[]) throws Exception	{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		String s_state = ProgramCentralConstants.STATE_EFFORT_SUBMIT;
		String sType = "";
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			StringList slSelect = new StringList();
			slSelect.add(DomainConstants.SELECT_TYPE);
			Map mpSelected = bus.getInfo(context, slSelect);
			sType = (String)mpSelected.get(DomainConstants.SELECT_TYPE);    		

			if(sType != null && sType.equalsIgnoreCase(TYPE_EFFORT))
			{
				double dbl=0;
				String[] saEffortIds = new String[]{id};
				String strValue = getTotalPendingEffort(context, s_state, true, saEffortIds);
				if(strValue.contains("."))
				{
					String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
					strValue = strValue.replace('.',separator.charAt(0));
				}
				columnValues.add(strValue);
			}
			else if(sType != null && sType.equalsIgnoreCase(DomainConstants.TYPE_PERSON))
			{
				String sEffortIds = (String)((Map)relBusObjPageList.get(i)).get("effortIds");
				StringList slEffortIds = FrameworkUtil.splitString(sEffortIds,",");
				String[] saEffortIds = new String[slEffortIds.size()];
				slEffortIds.copyInto(saEffortIds);
				String strValue = getTotalPendingEffort(context, s_state, true, saEffortIds);              		
				if(strValue.contains("."))
				{
					String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
					strValue = strValue.replace('.',separator.charAt(0));
				}
				columnValues.add(strValue);
				//Added:PRG:I16:R213:08-Dec-2011:IR-100320V6R2013 End
			}else{
				if(sType.equalsIgnoreCase(DomainConstants.TYPE_PROJECT_SPACE))
					columnValues.add(" ");
				else{
					double task_pending=0;
					task_pending = getTotalTaskEffort(context,id,ProgramCentralConstants.STATE_EFFORT_SUBMIT);
					//Added:PRG:R213:I16:IR-084709V6R2013 Start
					NumberFormat numberFormat = NumberFormat.getInstance();
					numberFormat.setMaximumFractionDigits(2);
					String strRoundedValue = numberFormat.format(task_pending);
					double roundedValue = 0;
					if (strRoundedValue != null && !"".equals(strRoundedValue)) {
						Number numericValue = numberFormat.parse(strRoundedValue);
						roundedValue        = numericValue.doubleValue();
						//roundedValue = Task.parseToDouble(strRoundedValue);
					}
					String sRet = ""+ roundedValue;
					if(sRet.contains("."))
					{
						String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
						sRet = sRet.replace('.',separator.charAt(0));
					}
					columnValues.add(sRet);
				} 
			}
		}
		return columnValues;
	}



	private String getTotalPendingEffort(Context context,String strCheckState,boolean isIncludeCheckState, String[] saEffortIds) throws MatrixException, FrameworkException,
	Exception {
		StringList slSelect = new StringList();
		slSelect.add(DomainConstants.SELECT_ID);
		slSelect.add(DomainConstants.SELECT_CURRENT);
		slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_SUNDAY);
		slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_MONDAY);
		slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TUESDAY);
		slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_WEDNESDAY);
		slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_THURSDAY);
		slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_FRIDAY);
		slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_SATURDAY);
		double dblValue=0.0;
		String strValue="";
		BusinessObjectWithSelectList withSelectList = null;
		BusinessObjectWithSelect bows = null;
		withSelectList = BusinessObject.getSelectBusinessObjectData(context, saEffortIds, slSelect);

		for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(withSelectList); itr.next();)
		{
			bows = itr.obj();
			String effortId = bows.getSelectData(DomainConstants.SELECT_ID);
			String sState = bows.getSelectData(DomainConstants.SELECT_CURRENT);      
			double dbl=0.0;
			boolean isInclude = false; 
			if(isIncludeCheckState)
			{    
				if((ProgramCentralUtil.isNotNullString(strCheckState) && ProgramCentralUtil.isNotNullString(sState)&& sState.equalsIgnoreCase(strCheckState)) 
						|| ("All".equalsIgnoreCase(strCheckState)))
				{    
					isInclude = true;
				}
			}
			else
			{
				if(ProgramCentralUtil.isNotNullString(strCheckState) && ProgramCentralUtil.isNotNullString(sState)&& !sState.equalsIgnoreCase(strCheckState) )
				{    
					isInclude = true;
				}
			}  
			if(isInclude)
			{
				double dbl_Sunday = Task.parseToDouble(bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_SUNDAY));
				double dbl_Monday = Task.parseToDouble(bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_MONDAY));
				double dbl_Tuesday = Task.parseToDouble(bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_TUESDAY));
				double dbl_Wednesday = Task.parseToDouble(bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_WEDNESDAY));
				double dbl_Thursday = Task.parseToDouble(bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_THURSDAY));
				double dbl_Friday = Task.parseToDouble(bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_FRIDAY));
				double dbl_Saturday = Task.parseToDouble(bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_SATURDAY));
				dbl = dbl_Sunday + dbl_Monday + dbl_Tuesday +  dbl_Wednesday + dbl_Thursday + dbl_Friday + dbl_Saturday;
			}
			dbl = roundWBSEffortValue(dbl);
			dblValue = dblValue + dbl;
			strValue = "" + dblValue;
		}
		return strValue;
	}

	/**
	 * Gets the daily effort value
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param effortId is the effort object Id
	 * @param attribute_name is the name of the day for which
	 *    the effort value has to be found
	 * @return double containing the daily effort value
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public double getEffortDaily(Context context, String effortId, String attribute_name)
			throws Exception
			{
		String attribute_val=(String)PropertyUtil.getSchemaProperty(context,attribute_name);
		double intDouble = 0;
		DomainObject bus  = DomainObject.newInstance(context, effortId);
		bus.open(context);
		if (attribute_val!=null && attribute_val.trim().length() > 0)
		{
			return(new Double(bus.getAttributeValue(context,attribute_val)).doubleValue());
		}
		return intDouble;
			}

	/**
	 * Gets the daily effort of the task
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param Vector containing the effortids
	 * @param attribute_name is the name of the day
	 * @return double containing the task's daily effort value
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public double getTaskDaily(Context context, Vector effortids, String attribute_name)
			throws Exception
			{

		String attribute_val=(String)PropertyUtil.getSchemaProperty(context,attribute_name);
		String id = "";
		double intDouble = 0;
		for (int i = 0; i < effortids.size(); i++)
		{
			id =(String)effortids.get(i);
			DomainObject bus  = DomainObject.newInstance(context, id);
			bus.open(context);
			if (attribute_val!=null && attribute_val.trim().length() > 0)
			{
				double dbl_val = new Double(bus.getAttributeValue(context,attribute_val)).doubleValue();
				intDouble = intDouble + dbl_val;
			}

		}

		return intDouble;
			}

	/**
	 * Gets the total person effort
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param effortid is the effort object Id
	 * @param personname is the name of the person
	 * @return double containing the effort values
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public double getTotalPersonEffort_OLD(Context context, String effortid, String personname)
			throws Exception
			{
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String attribute_TotalEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_TotalEffort");
		Vector taskVec = getEffortIds(context,effortid,relPattern,type_TaskManagement,true,false,(short)1);
		String sTaskId = (String)taskVec.get(0);
		HashMap hm = getPersonEffortMapping(context, sTaskId);

		double intDouble = 0;
		Vector vSelect = new Vector();

		vSelect = (Vector)hm.get(personname);

		if(vSelect!=null)
		{
			String[] strEffortIds = new String[vSelect.size()];
			intDouble = Task.parseToDouble(getTotalPendingEffort(context, "All", true, strEffortIds));                             
		}

		return intDouble;
			}

	/**
	 * Gets the remaining effort for the task and the efforts
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the remaining effort value
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getRemainingEffort(Context context, String args[])
			throws Exception
			{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList"); 
		HashMap paramMap = (HashMap)programMap.get("paramList");
		String tableName = (String)paramMap.get("table");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		String type_Effort=ProgramCentralConstants.TYPE_EFFORT;
		String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");
		String attribute_RemainingEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_RemainingEffort");
		String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
		String s_state = ProgramCentralConstants.STATE_EFFORT_REJECTED;
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			ContextUtil.pushContext(context);
			DomainObject bus  = DomainObject.newInstance(context, id);
			bus.open(context);
			String sType = bus.getTypeName();
			boolean islatest = true;
			String strWeekending = DomainConstants.EMPTY_STRING;
			Vector taskVec = new Vector();
			String sTaskId = DomainConstants.EMPTY_STRING;
			if(sType != null && sType.equalsIgnoreCase(type_Effort))
			{
				taskVec = getEffortIds(context,id,relPattern,type_TaskManagement,true,false,(short)1);
				sTaskId = (String)taskVec.get(0);
				String strValue ="";
				strWeekending = bus.getAttributeValue(context,attribute_WeekEndingDate);
				String str_Orig = bus.getAttributeValue(context,attribute_Originator);
				double plannedEffort = getPlannedEffort(context,id);
				double totalEffortsByPerson = getTotalPersonEffort(context,id, str_Orig);
				double tEffort = getTotalTaskEffort(context,id,ProgramCentralConstants.STATE_EFFORT_APPROVED);
				ContextUtil.popContext(context);
				double p_item=0;
				double dbl_remaining=0;
				islatest = isLatestEffort(context,sTaskId,str_Orig,strWeekending,id);
				if(islatest)
				{
					dbl_remaining = (plannedEffort - totalEffortsByPerson);
					if(dbl_remaining<0)
						dbl_remaining = 0;
					strValue = Double.toString(dbl_remaining);
				}else{
					strValue=" ";
				}
				//To show values on UI in correct Format
				if(strValue.contains("."))
				{
					String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
					strValue = strValue.replace('.',separator.charAt(0));
				}
				columnValues.add(strValue);
			}
			else if(sType != null && sType.equalsIgnoreCase(DomainConstants.TYPE_PERSON))
			{
				String str_Orig = bus.getName();
				String sEffortIds = (String)((Map)relBusObjPageList.get(i)).get("effortIds");
				StringList slEffortIds = FrameworkUtil.splitString(sEffortIds,",");
				String[] saEffortIds = new String[slEffortIds.size()];
				int j=0 ;
				Iterator irt = slEffortIds.iterator();
				while(irt.hasNext())
				{
					saEffortIds[j] = (String)irt.next();
					j++;
				}
				StringList slSelect = new StringList();
				slSelect.add(DomainConstants.SELECT_ID);
				slSelect.add(DomainConstants.SELECT_CURRENT);
				slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_WEEK_ENDING_DATE);
				double dblValue=0.0;
				String strValue="";
				BusinessObjectWithSelectList withSelectList = null;
				BusinessObjectWithSelect bows = null;
				withSelectList = BusinessObject.getSelectBusinessObjectData(context, saEffortIds, slSelect);

				for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(withSelectList); itr.next();)
				{
					bows = itr.obj();
					String effortId = bows.getSelectData(DomainConstants.SELECT_ID);
					String sState = bows.getSelectData(DomainConstants.SELECT_CURRENT);
					strWeekending = bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_WEEK_ENDING_DATE);
					taskVec = getEffortIds(context,effortId,relPattern,type_TaskManagement,true,false,(short)1);
					sTaskId = (String)taskVec.get(0);
					double plannedEffort=0.0;
					double totalEffortsByPerson=0.0;
					double tEffort=0.0;
					plannedEffort = getPlannedEffort(context,effortId);
					totalEffortsByPerson = getTotalPersonEffort(context,effortId, str_Orig);
					tEffort = getTotalTaskEffort(context, effortId,ProgramCentralConstants.STATE_EFFORT_APPROVED);   

					double p_item=0.0;
					double dbl_remaining=0.0;
					islatest = isLatestEffort(context,sTaskId,str_Orig,strWeekending,effortId);
					if(islatest)
					{
						dbl_remaining = (plannedEffort - totalEffortsByPerson);

						if(dbl_remaining<0)
							dbl_remaining = 0;
					}else
						dbl_remaining = 0.0;
					dblValue = dblValue+dbl_remaining;                 		  

				}              		
				strValue = Double.toString(dblValue);
				//To show values on UI in correct Format
				if(strValue.contains("."))
				{
					String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
					strValue = strValue.replace('.',separator.charAt(0));
				}
				columnValues.add(strValue);
			}//Added:PRG:I16:R213:08-Dec-2011:IR-100320V6R2013 End
			else
			{
				if(sType.equalsIgnoreCase(DomainConstants.TYPE_PROJECT_SPACE)){
					columnValues.add("");
				}else{
					ContextUtil.pushContext(context);
					double roolup_total = getTotalTaskRemainingEffort(context, id);
					double total_effort = getTotalTaskEffort(context,id,ProgramCentralConstants.STATE_EFFORT_APPROVED);
					double total_pending = getTotalTaskEffort(context,id,ProgramCentralConstants.STATE_EFFORT_SUBMIT);
					double final_remaining = getTaskPlannedEffort(context,id);
					ContextUtil.popContext(context);

					if(roolup_total<=0 && total_effort==0 && total_pending==0){
						roolup_total= final_remaining;
					}
					final_remaining=final_remaining-(total_pending+total_effort);
					if(final_remaining < 0 )
						final_remaining=0;
					//Added:14-Sep-2010:vf2:R210 PRG:IR-071239
					String srem_task=  Double.toString(final_remaining);
					//End:14-Sep-2010:vf2:R210 PRG:IR-071239
					//To show values on UI in correct Format
					if(srem_task.contains("."))
					{
						String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
						srem_task = srem_task.replace('.',separator.charAt(0));
					}
					columnValues.add(srem_task);
				}
			}
		}
		return columnValues;
			}

	/**
	 * Gets the percentage completion of the task
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the percentage complete value of the task
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getCompletedEffort(Context context, String args[])
			throws Exception
			{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String s_state = ProgramCentralConstants.STATE_EFFORT_APPROVED;
		String id = "";
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			bus.open(context);
			String sType = bus.getTypeName();
			if(sType != null && (sType.equalsIgnoreCase(type_Effort)||sType.equalsIgnoreCase(DomainConstants.TYPE_PERSON))){
				columnValues.add("");

			} else{
				double totalEffort = getTotalTaskEffort(context,id,ProgramCentralConstants.STATE_EFFORT_APPROVED);
				double remain_effort = getTotalTaskRemainingEffort(context, id);
				double f_results = 0;
				if ((totalEffort + remain_effort) != 0)
				{
					f_results = totalEffort/(totalEffort + remain_effort);
				}

				double task_rem = f_results*100;
				if(task_rem>100)
					task_rem=100;
				//task_rem = task_rem;
				String srem_task=   ""+roundWBSPercentComplete(task_rem);
				columnValues.add(srem_task+"%");

			}
		}
		return columnValues;
			}

	/**
	 * Gets the Estimated end date for the task
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the estimated end date
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getEstimatedEndDate(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			bus.open(context);
			String sType = bus.getTypeName();
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				columnValues.add("");

			} else{

				String attribute_TaskFinishDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedFinishDate");
				columnValues.add(bus.getAttributeValue(context,attribute_TaskFinishDate));

			}
		}
		return columnValues;
			}

	/**
	 * Gets the Predicted end date for the task
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the predicted end date
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getPredictedEndDate(Context context, String args[])
			throws Exception
			{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		String type_Effort = (String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String task_Management = (String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String relPattern = (String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		double dayMultiple=0;
		String s_state = ProgramCentralConstants.STATE_EFFORT_REJECTED;
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			bus.open(context);
			String sType = bus.getTypeName();
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				//Get the remaining effort
				Vector taskVec = getEffortIds(context,id,relPattern,task_Management,true,false,(short)1);
				String sTaskId = (String)taskVec.get(0);
				String attribute_RemainingEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_RemainingEffort");
				String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
				String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");
				String strValue = bus.getAttributeValue(context,attribute_RemainingEffort);
				String str_org = bus.getAttributeValue(context,attribute_Originator);
				String str_weekending = bus.getAttributeValue(context,attribute_WeekEndingDate);
				boolean islatest = isLatestEffort(context,sTaskId,str_org,str_weekending,id);

				if(islatest)
				{
					double rm_effort = new Double(strValue).doubleValue();

					double dbl=0;
					String [] strEffortId = new String[]{id};
					dbl = Task.parseToDouble(getTotalPendingEffort(context,s_state, true,strEffortId));
					rm_effort = rm_effort + dbl ;
					if(rm_effort != 0)
					{
						HashMap hmap = getAllocationData(context,sTaskId,id);
						double alloc_multiple = 0;
						double alloc =0;
						if(hmap!=null && hmap.size()>0) {
							Double allocation = new Double((String)hmap.get("allocation"));
							alloc = allocation.doubleValue();
						}
						double dblConverter = getDenormalizedDuration(context, (alloc/100), DURATION_UNIT_HOURS);
						double numberOfDays = 0.0;
						if(dblConverter>0.0) 
							numberOfDays =  rm_effort/dblConverter;
						numberOfDays=Math.ceil(numberOfDays);
						long lng = getLongValue(""+numberOfDays);
						long total_days = lng;
						String strDate = str_weekending; //bo.getAttributeValue(context,attribute_TaskFinishDate);
						Date nDay = new Date(strDate);

						Date NextDueDate = DateUtil.computeFinishDate(nDay,(total_days*MILLIS_IN_DAY));
						java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
						String aDate = sdf.format(NextDueDate);
						columnValues.add(aDate);
					}
					else
					{
						columnValues.add(str_weekending);
					}
				} else {
					columnValues.add("");
				}
			}
			//Added:PRG:I16:R213:08-Dec-2011:IR-100320V6R2013 Start
			else if(sType != null && sType.equalsIgnoreCase(DomainConstants.TYPE_PERSON))
			{
				String str_Orig = bus.getName();
				String sEffortIds = (String)((Map)relBusObjPageList.get(i)).get("effortIds");
				StringList slEffortIds = FrameworkUtil.splitString(sEffortIds,",");
				String[] saEffortIds = new String[slEffortIds.size()];
				int j=0 ;
				Iterator irt = slEffortIds.iterator();
				while(irt.hasNext())
				{
					saEffortIds[j] = (String)irt.next();
					j++;            		  
				}
				StringList slSelect = new StringList();
				slSelect.add(DomainConstants.SELECT_ID);
				slSelect.add(DomainConstants.SELECT_CURRENT);
				slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_REMAINING_EFFORT);
				slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_WEEK_ENDING_DATE);
				double dblValue=0.0;

				BusinessObjectWithSelectList withSelectList = null;
				BusinessObjectWithSelect bows = null;
				withSelectList = BusinessObject.getSelectBusinessObjectData(context, saEffortIds, slSelect);
				for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(withSelectList); itr.next();)
				{
					bows = itr.obj();
					String effortId = bows.getSelectData(DomainConstants.SELECT_ID);
					String sState = bows.getSelectData(DomainConstants.SELECT_CURRENT);     
					String strValue = bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_REMAINING_EFFORT);
					String str_weekending = bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_WEEK_ENDING_DATE);
					Vector taskVec = getEffortIds(context,effortId,relPattern,task_Management,true,false,(short)1);
					String sTaskId = (String)taskVec.get(0); 
					boolean islatest = isLatestEffort(context,sTaskId,str_Orig,str_weekending,effortId);

					if(islatest)
					{
						double rm_effort = new Double(strValue).doubleValue();
						double dbl=0;  
						if(sState!=null && sState.equalsIgnoreCase(s_state))
						{
							String [] strEffortId = new String[]{effortId};
							dbl = Task.parseToDouble(getTotalPendingEffort(context,sState, true,strEffortId));
						}
						rm_effort = rm_effort + dbl ;
						if(rm_effort != 0)
						{
							//Get allocation multiple
							HashMap hmap = getAllocationData(context,sTaskId,effortId);
							double alloc_multiple = 0;
							double alloc =0;
							if(hmap!=null && hmap.size()>0) {
								Double allocation = new Double((String)hmap.get("allocation"));
								alloc = allocation.doubleValue();
							}
							//double dblConverter = 8 * (alloc/100);
							double dblConverter = getDenormalizedDuration(context, (alloc/100), DURATION_UNIT_HOURS);
							double numberOfDays = 0.0;
							if(dblConverter>0.0) 
								numberOfDays =  rm_effort/dblConverter;
							numberOfDays=Math.ceil(numberOfDays);
							long lng = getLongValue(""+numberOfDays);
							long total_days = lng;

							String strDate = str_weekending; //bo.getAttributeValue(context,attribute_TaskFinishDate);
							Date nDay = new Date(strDate);

							Date NextDueDate = DateUtil.computeFinishDate(nDay,(total_days*MILLIS_IN_DAY));
							java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
							String aDate = sdf.format(NextDueDate);
							columnValues.add(aDate);
						}
						else
						{
							columnValues.add(str_weekending);
						}
					} else {
						columnValues.add("");

					}
				}//Added:PRG:I16:R213:08-Dec-2011:IR-100320V6R2013 End
			} else{

				String sState = FrameworkUtil.getCurrentState(context,bus).getName();

				String state_create = PropertyUtil.getSchemaProperty(context,"policy", (String)PropertyUtil.getSchemaProperty(context,"policy_ProjectTask"), "state_Create");
				String state_assign = PropertyUtil.getSchemaProperty(context,"policy", (String)PropertyUtil.getSchemaProperty(context,"policy_ProjectTask"), "state_Assign");
				String state_active = PropertyUtil.getSchemaProperty(context,"policy", (String)PropertyUtil.getSchemaProperty(context,"policy_ProjectTask"), "state_Active");
				//only do this for task that are in the create,assign and active states
				if(!bus.isKindOf(context,DomainConstants.TYPE_TASK_MANAGEMENT)){
					columnValues.add("");
				}
				else if(sState!=null && (sState.equalsIgnoreCase(state_create)
						|| sState.equalsIgnoreCase(state_assign)
						|| sState.equalsIgnoreCase(state_active)))
				{
					Date dt_task_predicted = getTaskPredictedEndDate(context,id);
					java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
					String aDate = sdf.format(dt_task_predicted);
					columnValues.add(aDate);

				}  else  {

					columnValues.add("");
				}

			}
		}

		return columnValues;
			}

	/**
	 * Gets the latest effort id
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param vSelect is a Vector containing the name of the task assignee
	 * @return String containing the latest effort id
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public String getLatestEffort(Context context, Vector vSelect) throws Exception
	{

		String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
		if(vSelect!= null && vSelect.size()==1)
		{
			return ((String)vSelect.get(0));

		}

		String return_id = "";
		if(vSelect!=null && vSelect.size()>1)
		{


			String zeroEl = (String)vSelect.get(0);
			DomainObject bus_init = DomainObject.newInstance(context, zeroEl);
			bus_init.open(context);

			String init_weekending = bus_init.getAttributeValue(context,attribute_WeekEndingDate);
			if(init_weekending!=null && !"".equals(init_weekending)){
				Date startdate = new Date(init_weekending);

				return_id = zeroEl;

				for (int i = 1; i < vSelect.size(); i++)
				{
					String id =(String)vSelect.get(i);

					if(id != null && id.trim().length() > 0 )
					{
						DomainObject bus = DomainObject.newInstance(context, id);
						bus.open(context);

						String str_weekending = bus.getAttributeValue(context,attribute_WeekEndingDate);
						if(str_weekending!=null && !"".equals(str_weekending)){
							Date checkdate = new Date(str_weekending);
							if (checkdate.after(startdate))
							{
								return_id = id;
								startdate=checkdate;
							}
						}
					}

				}
			}
		}


		return return_id;

	}

	/**
	 * Checks wheather the effort is the latest entered effort
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param sTaskId is the object id of task
	 * @param personname is the name of the task assignee
	 * @param strDate is the weekending date of the effort
	 * @param effortid is the object id of effort
	 * @return boolean: true if it is the latest effort and false otherwise
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public boolean isLatestEffort(Context context, String sTaskId , String personname, String strDate,String effortid)
			throws Exception
			{

		boolean isLastest = true;
		Date nDay = new Date(strDate);
		HashMap hm = getPersonEffortMapping(context, sTaskId);
		double intDouble = 0;
		Vector vSelect = new Vector();

		vSelect = (Vector)hm.get(personname);

		if(vSelect!=null)
		{
			for (int i = 0; i < vSelect.size(); i++)
			{
				String id =(String)vSelect.get(i);

				if(id != null && id.trim().length() > 0 && !id.equalsIgnoreCase(effortid))
				{
					DomainObject bus = DomainObject.newInstance(context, id);
					bus.open(context);
					String str_weekending = bus.getAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_WEEK_ENDING_DATE);
					bus.close(context);
					Date checkdate = new Date(str_weekending);
					if (checkdate.after(nDay))
					{
						return false;
					}

				}

			}
		}

		return isLastest;


			}

	/**
	 * Displays a Red checkmark if the predicted end date for the task
	 *    is different from the estimated end date and Red icon if the
	 *    predicted end date of task assignee is different from the estimated
	 *    end date.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the image icon as string
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getPredictedEndDateHTML(Context context, String args[])
			throws Exception
			{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		String type_Effort = (String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String task_Management = (String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String relPattern = (String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		double dayMultiple=0;
		String statusGif="";
		//String statusGifGreen = "";
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			bus.open(context);
			String sType = bus.getTypeName();
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				//Get the remaining effort
				String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
				String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");
				String str_org = bus.getAttributeValue(context,attribute_Originator);
				String str_weekending = bus.getAttributeValue(context,attribute_WeekEndingDate);
				String attribute_RemainingEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_RemainingEffort");
				String strValue = bus.getAttributeValue(context,attribute_RemainingEffort);
				double rm_effort = new Double(strValue).doubleValue();
				//statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=0 alt=\"On Schedule\">";
				//Added:27-July-10:vf2:R210:PRG Bug 062016
				statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" alt=\"On Schedule\" />";
				//End:27-July-10:vf2:R210:PRG Bug 062016
				Vector taskVec = getEffortIds(context,id,relPattern,task_Management,true,false,(short)1);
				String sTaskId = (String)taskVec.get(0);

				String weekend_Date = str_weekending; //bo.getAttributeValue(context,attribute_TaskFinishDate);
				Date weekDay = new Date(weekend_Date);

				boolean islatest = isLatestEffort(context,sTaskId,str_org,str_weekending,id);

				DomainObject bo  = DomainObject.newInstance(context, sTaskId);

				String attribute_TaskFinishDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedFinishDate");
				String strDate = bo.getAttributeValue(context,attribute_TaskFinishDate);
				Date nDay = new Date(strDate);

				if(rm_effort!=0){
					HashMap hmap = getAllocationData(context,sTaskId,id);
					double alloc_multiple = 0;
					double alloc =0;
					if(hmap!=null && hmap.size()>0) {                        
						Double allocation = new Double((String)hmap.get("allocation"));
						alloc = allocation.doubleValue();
					}
					//double dblConverter = 8 * (alloc/100);
					double dblConverter = getDenormalizedDuration(context, (alloc/100), DURATION_UNIT_HOURS);
					double numberOfDays = 0.0;
					if(dblConverter>0.0) 
						numberOfDays =  rm_effort/dblConverter;   
					numberOfDays=Math.ceil(numberOfDays);

					long lng = getLongValue(""+numberOfDays);
					long total_days = lng;


					Date NextDueDate = DateUtil.computeFinishDate(weekDay,(total_days*MILLIS_IN_DAY));

					// do not show only for latest effrot submission
					if(islatest && NextDueDate.after(nDay))
					{
						//statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=0 alt=\"Overdue\">";
						//Added:27-July-10:vf2:R210:PRG Bug 062016
						statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"Overdue\" />";
						//End:27-July-10:vf2:R210:PRG Bug 062016
					}
					else if(!islatest)
					{
						//Added:27-July-10:vf2:R210:PRG Bug 062016
						statusGif = ProgramCentralConstants.EMPTY_STRING;
						//End:27-July-10:vf2:R210:PRG Bug 062016
					}
					columnValues.add(statusGif);
				}else{
					if(islatest && weekDay.after(nDay))
					{
						// statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=0 alt=\"Overdue\">";
						//Added:27-July-10:vf2:R210:PRG Bug 062016
						statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"Overdue\" />";
						//End:27-July-10:vf2:R210:PRG Bug 062016

					}
					else if(!islatest)
					{
						//Added:27-July-10:vf2:R210:PRG Bug 062016
						statusGif = ProgramCentralConstants.EMPTY_STRING;
						//End:27-July-10:vf2:R210:PRG Bug 062016
					}
					columnValues.add(statusGif);
				}

			} 
			//Added:PRG:I16:R213:08-Dec-2011:IR-100320V6R2013 Start
			else if(sType != null && sType.equalsIgnoreCase(DomainConstants.TYPE_PERSON))
			{
				String str_Orig = bus.getName();
				String sEffortIds = (String)((Map)relBusObjPageList.get(i)).get("effortIds");
				StringList slEffortIds = FrameworkUtil.splitString(sEffortIds,",");
				String[] saEffortIds = new String[slEffortIds.size()];
				slEffortIds.copyInto(saEffortIds);

				if(null==slEffortIds || slEffortIds.size()<=0)
				{
					columnValues.add(ProgramCentralConstants.EMPTY_STRING);
					continue;
				}
				StringList slSelect = new StringList();
				slSelect.add(DomainConstants.SELECT_ID);
				slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_REMAINING_EFFORT);
				slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_WEEK_ENDING_DATE);
				double dblValue=0.0;

				BusinessObjectWithSelectList withSelectList = null;
				BusinessObjectWithSelect bows = null;
				withSelectList = BusinessObject.getSelectBusinessObjectData(context, saEffortIds, slSelect);

				for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(withSelectList); itr.next();)
				{
					bows = itr.obj();
					String sEffortId = bows.getSelectData(DomainConstants.SELECT_ID);
					String strValue="";
					String str_weekending="";
					Vector taskVec=new Vector();
					strValue = bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_REMAINING_EFFORT);
					str_weekending = bows.getSelectData(ProgramCentralConstants.SELECT_ATTRIBUTE_WEEK_ENDING_DATE);
					taskVec = getEffortIds(context,sEffortId,relPattern,task_Management,true,false,(short)1);

					String sTaskId = (String)taskVec.get(0); 
					boolean islatest = isLatestEffort(context,sTaskId,str_Orig,str_weekending,sEffortId);
					double rm_effort = new Double(strValue).doubleValue();

					statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" alt=\"On Schedule\" />";
					String weekend_Date = str_weekending; 
					Date weekDay = new Date(weekend_Date);

					DomainObject bo  = DomainObject.newInstance(context, sTaskId);

					String attribute_TaskFinishDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedFinishDate");
					String strDate = bo.getAttributeValue(context,attribute_TaskFinishDate);
					Date nDay = new Date(strDate);

					if(rm_effort!=0){
						HashMap hmap = getAllocationData(context,sTaskId,sEffortId);
						double alloc_multiple = 0;
						double alloc =0;
						if(hmap!=null && hmap.size()>0) {                        
							Double allocation = new Double((String)hmap.get("allocation"));
							alloc = allocation.doubleValue();
						}
						//double dblConverter = 8 * (alloc/100);
						double dblConverter = getDenormalizedDuration(context, (alloc/100), DURATION_UNIT_HOURS);
						double numberOfDays = 0.0;
						if(dblConverter>0.0) 
							numberOfDays =  rm_effort/dblConverter;   
						numberOfDays=Math.ceil(numberOfDays);

						long lng = getLongValue(""+numberOfDays);
						long total_days = lng;


						Date NextDueDate = DateUtil.computeFinishDate(weekDay,(total_days*MILLIS_IN_DAY));
						if(islatest && NextDueDate.after(nDay))
						{
							statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"Overdue\" />";
						}
						else if(!islatest)
						{	
							statusGif = ProgramCentralConstants.EMPTY_STRING;
						}
						columnValues.add(statusGif);
					}else{
						if(islatest && weekDay.after(nDay))
						{
							statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"Overdue\" />";
						}
						else if(!islatest)
						{	
							statusGif = ProgramCentralConstants.EMPTY_STRING;
						}
						columnValues.add(statusGif);
					} 
				} //Added:PRG:I16:R213:08-Dec-2011:IR-100320V6R2013 End
			} else{      //make this is available only to the project lead

				com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
				task.setId(id);
				StringList busSelects = new StringList();
				busSelects.addElement(DomainObject.SELECT_ID);
				//Map taskMap = (Map) task.getProject(context,busSelects);

				//String projectId =(String)taskMap.get(DomainObject.SELECT_ID);

				//Changes done for External ProjectUser
				//Added:23-Feb-09:wqy:R207:PRG Bug 368350 
				com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE,"PROGRAM");
				project.setId(id);
				//   
				// com.matrixone.apps.program.ProjectSpace project = task.getProject(context);
				//End:R207:PRG Bug 368350
				String userAccess = null;
				if(project != null )
				{
					userAccess = project.getAccess(context);
				}

				boolean isLead = false;
				if(userAccess!=null && userAccess.trim().length() > 0 )
				{
					if(userAccess.equals("Project Lead") || userAccess.equals("Project Owner"))
						isLead=true;
				}

				if (isLead)
				{
					String attribute_TaskFinishDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedFinishDate");
					String finish_date = task.getAttributeValue(context,attribute_TaskFinishDate);
					Date estimated_date = new Date(finish_date);
					Date predicted_date = getTaskPredictedEndDate (context, id);
					//When predicted end date for the task is different from estimated end date
					//Display red check mark
					if(!(estimated_date.equals(predicted_date)))
					{
						Calendar start_cal = new GregorianCalendar();
						start_cal.clear();
						start_cal.setTime(predicted_date);
						int i_month = start_cal.get(Calendar.MONTH);
						int i_date = start_cal.get(Calendar.DATE);
						int i_year = start_cal.get(Calendar.YEAR);

						//Added:27-July-10:vf2:R210:PRG Bug 062016
						StringBuffer sbApproverLink = new StringBuffer();                           
						sbApproverLink.append("<a href=\"javascript:emxTableColumnLinkClick('" +
								//com.matrixone.apps.domain.util.XSSUtil.encodeForURL("../programcentral/emxProgramCentralUpdateTaskEffortDialogFS.jsp?taskid="+id+"&amp;month="+i_month+"&amp;year="+i_year+"&amp;date="+i_date));       					                                   
								com.matrixone.apps.domain.util.XSSUtil.encodeForURL("../programcentral/emxProgramCentralUpdateTaskEffortDialogFS.jsp?taskid="+id+"&month="+i_month+"&year="+i_year+"&date="+i_date));
						sbApproverLink.append("','popup','','','','')\">");                                                                     
						sbApproverLink.append("<img border=\"0\" src=\"../common/images/iconStatusCheckmark.gif\" alt=\"Update Task Estimated Finish Date\"></img>");                                                         
						sbApproverLink.append("</a>");
						columnValues.add(sbApproverLink.toString());
						//End:27-July-10:vf2:R210:PRG Bug 062016
					}else{

						//columnValues.add("&nbsp;");
						//Added:27-July-10:vf2:R210:PRG Bug 062016
						columnValues.add(ProgramCentralConstants.EMPTY_STRING);
						//End:27-July-10:vf2:R210:PRG Bug 062016
					}
				}else{

					// columnValues.add("&nbsp;");
					//Added:27-July-10:vf2:R210:PRG Bug 062016
					columnValues.add(ProgramCentralConstants.EMPTY_STRING);
					//End:27-July-10:vf2:R210:PRG Bug 062016
				}

			}
		}

		return columnValues;
			}

	/**
	 * Gets the next date
	 *
	 * @param thisDay is a Date
	 * @param num is an integer
	 * @return Date which the next date
	 * @since PMC 10.5.1.2
	 */
	public static Date getNextDate( Date thisDay,int num )
	{
		return new Date( thisDay.getTime() + (MILLIS_IN_DAY * num) );
	}

	/**
	 * Gets the Long value from the string
	 *
	 * @param str is a String
	 * @return long
	 * @since PMC 10.5.1.2
	 */
	public long getLongValue(String str)
	{
		long ret = 0;
		if (str!=null && str.length()>0)
		{
			int pos = str.indexOf(".");
			if ( pos == -1 )
			{
				return (new Long(str).longValue());
			}else{
				String strSub = str.substring(0, pos);
				return (new Long(strSub).longValue());
			}
		}

		return ret;
	}

	/**
	 * Gets the previous date
	 *
	 * @param thisDay is a Date
	 * @return Date which the previous date
	 * @since PMC 10.5.1.2
	 */
	public static Date getPreviousDate( Date thisDay )
	{
		return new Date( thisDay.getTime() - MILLIS_IN_DAY );
	}

	/**
	 * Gets the effort values for each effort object for Monday
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the effort values on Monday
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getMon(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		double dbl_mon = 0;
		String eff_val = "";
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			String sType = bus.getTypeName();
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				dbl_mon = getEffortDaily(context, id, "attribute_Monday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}else{
				Vector effortIds = getEffortIds(context,id,relPattern,type_Effort,false,true,(short)1);
				dbl_mon = getTaskDaily(context, effortIds, "attribute_Monday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}
		}
		return columnValues;
			}

	/**
	 * Gets the effort values for each effort object for Tuesday
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the effort values on Tuesday
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getTue(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		double dbl_mon = 0;
		String eff_val = "";
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			String sType = bus.getTypeName();
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				dbl_mon = getEffortDaily(context, id, "attribute_Tuesday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}else{
				Vector effortIds = getEffortIds(context,id,relPattern,type_Effort,false,true,(short)1);
				dbl_mon = getTaskDaily(context, effortIds, "attribute_Tuesday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}
		}
		return columnValues;
			}

	/**
	 * Gets the week ending date for all the effort objects
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the week ending date
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getWeekEnding(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			String sType = bus.getInfo(context, DomainConstants.SELECT_TYPE);
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				columnValues.add(bus.getAttributeValue(context,attribute_WeekEndingDate));

			} else{
				columnValues.add("");
			}
		}
		return columnValues;
			}

	/**
	 * Gets the effort values for each effort object for Wednesday
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the effort values on Wednesday
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getWed(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		double dbl_mon = 0;
		String eff_val = "";
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			String sType = bus.getTypeName();
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				dbl_mon = getEffortDaily(context, id, "attribute_Wednesday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}else{
				Vector effortIds = getEffortIds(context,id,relPattern,type_Effort,false,true,(short)1);
				dbl_mon = getTaskDaily(context, effortIds, "attribute_Wednesday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}
		}
		return columnValues;
			}

	/**
	 * Gets the effort values for each effort object for Thursday
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the effort values on Thursday
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getThu(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		double dbl_mon = 0;
		String eff_val = "";
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			String sType = bus.getTypeName();
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				dbl_mon = getEffortDaily(context, id, "attribute_Thursday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}else{
				Vector effortIds = getEffortIds(context,id,relPattern,type_Effort,false,true,(short)1);
				dbl_mon = getTaskDaily(context, effortIds, "attribute_Thursday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}
		}
		return columnValues;
			}

	/**
	 * Gets the effort values for each effort object for Friday
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the effort values on Friday
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getFri(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		double dbl_mon = 0;
		String eff_val = "";
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			String sType = bus.getTypeName();
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				dbl_mon = getEffortDaily(context, id, "attribute_Friday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}else{
				Vector effortIds = getEffortIds(context,id,relPattern,type_Effort,false,true,(short)1);
				dbl_mon = getTaskDaily(context, effortIds, "attribute_Friday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}
		}
		return columnValues;
			}

	/**
	 * Gets the effort values for each effort object for Saturday
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the effort values on Saturday
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getSat(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		double dbl_mon = 0;
		String eff_val = "";
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			String sType = bus.getTypeName();
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				dbl_mon = getEffortDaily(context, id, "attribute_Saturday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}else{
				Vector effortIds = getEffortIds(context,id,relPattern,type_Effort,false,true,(short)1);
				dbl_mon = getTaskDaily(context, effortIds, "attribute_Saturday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}
		}
		return columnValues;
			}

	/**
	 * Gets the effort values for each effort object for Sunday
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the effort values on Sunday
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getSun(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		double dbl_mon = 0;
		String eff_val = "";
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			String sType = bus.getTypeName();
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				dbl_mon = getEffortDaily(context, id, "attribute_Sunday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}else{
				Vector effortIds = getEffortIds(context,id,relPattern,type_Effort,false,true,(short)1);
				dbl_mon = getTaskDaily(context, effortIds, "attribute_Sunday");
				eff_val = "" + Math.round(dbl_mon);
				columnValues.add(eff_val);
			}
		}
		return columnValues;
			}

	/**
	 * Gets the current week total efforts
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the current week total as string
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getCurrentWeekTotal(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		String strValue = "";
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String attribute_PendingEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_PendingEffort");
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			String sType = bus.getInfo(context, DomainConstants.SELECT_TYPE);
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				String[] strEffortIds = new String[]{id};
				strValue = getTotalPendingEffort(context,"All",true,strEffortIds);
				columnValues.add(strValue);
			} else{
				columnValues.add("");
			}
		}
		return columnValues;
			}

	/**
	 * Gets the next week total
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing the next week total
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getNextWeekTotal(Context context, String args[])
			throws Exception
			{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String strValue = "";
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			strValue =(String)((Map)relBusObjPageList.get(i)).get("id");
			columnValues.add("&nbsp;");
		}
		return columnValues;
			}

	/**
	 * Gets the effort duration
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing duration as string
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getEffortDuration(Context context, String args[])
			throws Exception
			{

		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String objectId = (String) paramMap.get("objectId");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		String attribute_Duration=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedDuration");

		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		String sRetValue = "";
		String sType = "";
		String type_effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			if (id != null && id.trim().length() > 0 )
			{
				BusinessObject bo = new BusinessObject(id);
				try{
					bo.open(context);
				}catch (FrameworkException Ex) {
					throw Ex;
				}
				sType = bo.getTypeName();
				if(sType != null && sType.equalsIgnoreCase(type_effort)){
					sRetValue = " ";
				}
				else{
					com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
					task.setId(id);

					//ADDED for FRACTIONAL TASKS

					UnitList _units = null;
					Map _unitsLabel = new HashMap(5);
					Map mapTaskInfo = null;

					//Gets all the Units attached to the Dimension
					//
					AttributeType attrType = new AttributeType(DomainConstants.ATTRIBUTE_TASK_ESTIMATED_DURATION);
					try
					{
						Dimension dimension = attrType.getDimension(context);
						if (dimension == null)
							_units = new UnitList();
						else
							_units = dimension.getUnits(context);
					}
					catch (Exception e)
					{
						_units = new UnitList();
					}

					String unitStr = null;
					if (_units.size() != 0)
					{                    

						UnitItr uitr = new UnitItr(_units);
						while(uitr.next())
						{
							Unit unit = (Unit) uitr.obj();
							//index map based on unit name, not label.
							_unitsLabel.put(unit.getName(), unit);                                
						}

					}

					//Gets Estimated Duration and Duration Unit of this Task.
					//
					StringList busSelect = new StringList(2);
					busSelect.add(task.SELECT_TASK_ESTIMATED_DURATION);
					busSelect.add(task.SELECT_TASK_ESTIMATED_DURATION+".inputunit");

					mapTaskInfo = task.getInfo(context,busSelect);                    

					String getTaskEstDuration = (String)mapTaskInfo.get(task.SELECT_TASK_ESTIMATED_DURATION);
					String getTaskUnit = (String)mapTaskInfo.get(task.SELECT_TASK_ESTIMATED_DURATION+".inputunit");                    

					String strEstDurwithUnit = "";

					//Denormalizes the Duration in the User selected Unit.
					//
					Unit unit = (Unit) _unitsLabel.get(getTaskUnit);
					if (unit != null)
					{
						strEstDurwithUnit = unit.denormalize(getTaskEstDuration) + " " + getTaskUnit;
					}  

					//ENDS

					sRetValue = strEstDurwithUnit;

				}

			}

			columnValues.add(sRetValue);
		}
		return columnValues;
			}

	/**
	 * Gets the Object list details
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectId - the context task object Id
	 * @return MapList containing the object details
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getObjectList (Context context,
			String[] args)
					throws Exception
					{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

		String objectId = (String) paramMap.get("objectId");
		HashMap hpo = getPersonEffortMapping(context, objectId);
		task.setId(objectId);
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String typePattern=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");
		//Vector vec = getEffortIds(context,objectId,relPattern,typePattern,false,true,(short)1);
		MapList objList = new MapList();
		HashMap taskMap = new HashMap();
		taskMap.put("id",objectId);
		objList.add(taskMap);

		// check whether context user is project lead or owner,
		// if yes get all Efforts else get only the owned effors
		//StringList taskSelects = new StringList();
		//taskSelects.addElement(DomainObject.SELECT_ID);
		//Map taskMapInfo = (Map) task.getProject(context,taskSelects);

		//String projectId =(String)taskMapInfo.get(DomainObject.SELECT_ID);

		//Changes done for External ProjectUser Bug No. 356907 WQY
		//Added:23-Feb-09:wqy:R207:PRG Bug 368350
		com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context, DomainConstants.TYPE_PROJECT_SPACE,"PROGRAM");
		project.setId(objectId);
		//com.matrixone.apps.program.ProjectSpace project = task.getProject(context);
		//End:R207:PRG Bug 368350
		String userAccess = null;
		if(project != null )
		{
			userAccess = project.getAccess(context);
		}

		String sWhere = null;
		if(userAccess!=null && userAccess.trim().length() > 0 )
		{
			if(!userAccess.equals("Project Lead") && !userAccess.equals("Project Owner"))
			{
				sWhere = "attribute[" + attribute_Originator + "] == \"" + context.getUser() + "\"";
			}
		}

		String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
		String st_attr = "attribute[" + attribute_WeekEndingDate + "]";
		StringList relSelects = new StringList( );
		StringList busSelects = new StringList( 2 );
		busSelects.add( task.SELECT_ID );
		busSelects.add( st_attr );

		DomainObject dm = DomainObject.newInstance(context, objectId);

		MapList ml = dm.getRelatedObjects(context,
				PropertyUtil.getSchemaProperty( context, "relationship_hasEfforts" ),
				PropertyUtil.getSchemaProperty( context, "type_Effort" ),
				busSelects,
				relSelects,
				false,
				true,
				(short) 1,
				sWhere,
				null);
		ml.addSortKey(st_attr, "descending", "date");
		ml.sort();

		if (ml != null )
		{
			Map map;
			Iterator itr;
			for (itr = ml.iterator(); itr.hasNext(); )
			{
				map = (Map) itr.next();
				HashMap argsMap = new HashMap();
				argsMap.put("id",map.get(task.SELECT_ID));
				objList.add(argsMap);
			}

		}

		JPO.packArgs(objList);
		return objList;

					}

	/**
	 * Adjusts for the week end date
	 *
	 * @param thisDay is a Date
	 * @return Date
	 * @since PMC 10.5.1.2
	 */
	public Date adjustForWeekendDate( Date thisDay )
	{
		//int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
		return new Date( thisDay.getTime() + (2*MILLIS_IN_DAY) );
	}

	/**
	 * Gets the task predicted end date from the object Id of effort
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId is the objectId of effort
	 * @return Date which is the predicted end date of the task
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Date getTaskPredictedEndDate (Context context,
			String objectId)
					throws Exception
					{

		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

		String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
		String attribute_TaskEstimatedFinishDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedFinishDate");
		String st_attr = "attribute[" + attribute_WeekEndingDate + "]";
		StringList relSelects = new StringList( );
		StringList busSelects = new StringList( 2 );
		busSelects.add( task.SELECT_ID );
		busSelects.add( st_attr );

		DomainObject dm = DomainObject.newInstance(context, objectId);
		String task_weekendingdate = dm.getAttributeValue(context,attribute_TaskEstimatedFinishDate);
		MapList ml = dm.getRelatedObjects(context,
				PropertyUtil.getSchemaProperty( context, "relationship_hasEfforts" ),
				PropertyUtil.getSchemaProperty( context, "type_Effort" ),
				busSelects,
				relSelects,
				false,
				true,
				(short) 1,
				null,
				null);
		ml.addSortKey(st_attr, "descending", "date");
		ml.sort();
		ArrayList argsMap = new ArrayList();
		if (ml != null && ml.size() > 0 )
		{
			Map map;
			Iterator itr;
			Map orgMap = (Map)ml.get(0);
			String skey = (String)orgMap.get(st_attr);
			Date keyDate = new Date(skey);
			Date prevDate = DateUtil.computeStartDate(keyDate,(7*MILLIS_IN_DAY));
			prevDate = adjustForWeekendDate(prevDate);

			for (itr = ml.iterator(); itr.hasNext(); )
			{
				map = (Map) itr.next();
				String currId = (String)map.get(task.SELECT_ID);
				String currDate = (String)map.get(st_attr);
				Date testDate = new Date(currDate);
				if(testDate.after(prevDate) || !testDate.before(prevDate)|| (!testDate.before(keyDate) && !testDate.after(keyDate)))
				{
					argsMap.add(map.get(task.SELECT_ID));
				}

			}

		}

		Date topdate= new Date(task_weekendingdate);
		if(argsMap != null && argsMap.size() > 0 )
		{
			for (int z=0; z<argsMap.size();z++)
			{
				String str_id=(String)argsMap.get(z);
				Date c_date = getPredictedEffortDate(context,objectId,str_id);

				if(z==0)
				{
					topdate = c_date;
				} else{

					if(c_date != null && c_date.after(topdate))
					{
						topdate=c_date;
					}
				}

			}
		}


		return topdate;

					}

	/**
	 * Gets the predicted effort date
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param sTaskId is the objectId of task
	 * @param Effortid is the objectId of effort
	 * @return Date which is the predicted effort date
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Date getPredictedEffortDate(Context context, String sTaskId, String Effortid) throws Exception
	{
		DomainObject bus = DomainObject.newInstance(context, Effortid);
		double dayMultiple = 0;
		String attribute_RemainingEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_RemainingEffort");
		String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
		String attribute_Originator=(String)PropertyUtil.getSchemaProperty(context,"attribute_Originator");
		String strValue = bus.getAttributeValue(context,attribute_RemainingEffort);
		String str_org = bus.getAttributeValue(context,attribute_Originator);
		String str_weekending = bus.getAttributeValue(context,attribute_WeekEndingDate);

		double rm_effort = new Double(strValue).doubleValue();
		String strDate = str_weekending;
		Date nDay = new Date(strDate);
		if(rm_effort > 0 && rm_effort < 1){
			dayMultiple = 1;
		}else if (rm_effort>1){
			//dayMultiple = rm_effort / 8 ;
			dayMultiple = getDenormalizedDuration(context, rm_effort, DURATION_UNIT_DAYS);
			dayMultiple=Math.round(dayMultiple);
		}else {
			return nDay;
		}

		if(dayMultiple > 0 && dayMultiple < 1){
			dayMultiple = 1;
		}
		/*dayMultiple=Math.ceil(dayMultiple);
              String StrDateMultiple = ""+dayMultiple;
              long myLong = getLongValue(StrDateMultiple);*/
		//Get allocation multiple
		HashMap hmap = getAllocationData(context,sTaskId,Effortid);
		double alloc_multiple = 0;
		double alloc = 0;
		if(hmap!=null && hmap.size()>0) {               
			Double allocation = new Double((String)hmap.get("allocation"));
			alloc = allocation.doubleValue();
			if(alloc > 0 && alloc < 1){
				alloc_multiple=0;
			} else if ( alloc <=0){
				alloc_multiple=0;
			} else if ( alloc > 1){
				alloc_multiple=100/alloc;
			}
		}
		double dblConverter = getDenormalizedDuration(context, (alloc/100), DURATION_UNIT_HOURS);
		double numberOfDays = 0.0;
		if(dblConverter>0.0) 
			numberOfDays =  dayMultiple/dblConverter;
		numberOfDays=Math.ceil(numberOfDays);
		long lng = getLongValue(""+numberOfDays);
		long total_days = lng;

		DomainObject bo  = DomainObject.newInstance(context, sTaskId);
		Date NextDueDate = DateUtil.computeFinishDate(nDay,(total_days*MILLIS_IN_DAY));
		// java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.US);
		// String aDate = sdf.format(NextDueDate);
		// columnValues.add(aDate);
		return NextDueDate;

	}

	/**
	 * Gets the project task list
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectId - the context task object Id
	 * @return MapList containing the task details
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getProjectTaskList (Context context,String[] args)throws Exception
	{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

		String objectId = (String) paramMap.get("objectId");
		String strExpandLevel = (String)paramMap.get("ExpandLevel");
		short nExpandLevel =  ProgramCentralUtil.getExpandLevel(strExpandLevel);

		task.setId(objectId);

		//Added:IR-177677V6R2013x
		String effortFilter = (String) paramMap.get("effortFilter");
		String stateAll = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Effort.All", "en");
		String stateSubmit = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Effort.Submitted", "en");
		if(stateSubmit.equals(effortFilter)==true) {
			effortFilter = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.WeeklyTimesheet.Submit", "en");
		}
		String strBusWhere = null;
		if(effortFilter != null && !effortFilter.equals(stateAll)){
			strBusWhere = DomainConstants.SELECT_CURRENT+"=="+effortFilter;
		}

		StringList busSelects = new StringList(1);
		busSelects.add(task.SELECT_ID);

		StringList relSelects = new StringList(1);
		relSelects.add(task.SELECT_ID);
		MapList objList = new MapList();
		MapList mlist = (MapList)task.getTasks(context, task,nExpandLevel, busSelects, null);

		String typePhase = PropertyUtil.getSchemaProperty(context,"type_Phase" );
		for(int j = 0; j < mlist.size(); j++){				
			Map map = (Map)mlist.get(j);
			String strType = (String)map.get("type");
			Map projectMap = task.getProject(context,relSelects);
			String projectId = (String)projectMap.get(task.SELECT_ID);
			DomainObject dobjProject = DomainObject.newInstance(context, projectId);
			String strPreference = dobjProject.getAttributeValue(context, ATTRIBUTE_EFFORT_SUBMISSION);
			//Added:11-April-2012:fzs:R213 PRG:IR-156865
			if(typePhase.equals(strPreference)) {
				if(!strType.equals(strPreference)){
					mlist.remove(j);
					j--;
				}
			}
		}
		//End:30-Aug-2010:vf2:R210 PRG:IR-067729

		if (mlist != null && mlist.size() > 0)
		{
			Map map;
			Iterator itr;
			for (itr = mlist.iterator(); itr.hasNext(); )
			{
				map = (Map) itr.next();
				HashMap effortMap = new HashMap();
				String eid = (String) map.get(task.SELECT_ID);
				String sLevel = (String) map.get("level");
				if (eid != null && eid.trim().length() > 0 )
				{
					effortMap.put("id",eid);
					effortMap.put("level",sLevel);
					objList.add(effortMap);
				}
			}
		}
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String typePattern=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
		String st_attr = "attribute[" + attribute_WeekEndingDate + "]";
		StringList relSel = new StringList();
		StringList busSel = new StringList(2);
		busSel.add(task.SELECT_ID );
		busSel.add(st_attr);
		busSel.add(DomainConstants.SELECT_ORIGINATOR); //Added:PRG:R213:I16:IR-100320V6R2013
		MapList mlObj = new MapList();		
		mlObj = task.getRelatedObjects(context,
				PropertyUtil.getSchemaProperty( context, "relationship_hasEfforts" ),
				PropertyUtil.getSchemaProperty( context, "type_Effort" ),
				busSel,
				relSel,
				false,
				true,
				(short) 1,
				//Added:10-June-2010:vf2:R210 PRG:IR-056503
				strBusWhere, 
				//End:10-June-2010:vf2:R210 PRG:IR-056503
				null,
				0);

		mlObj.addSortKey(st_attr, "descending", "date");
		mlObj.sort();

		if (null != mlObj)
		{			
			Iterator itr;
			Map mAllPersonsMap = new HashMap(); // final object Map List contaning map per user which contains id and effortid key
			for (itr = mlObj.iterator(); itr.hasNext(); )
			{
				//Added:PRG:R213:I16:08-Dec-2011:IR-100320V6R2013:Start
				Map map = (Map) itr.next();				
				String sUSerEffortId = (String) map.get(task.SELECT_ID);
				String sPersonId = (String) map.get(DomainConstants.SELECT_ORIGINATOR);	
				String sPersonObjId = PersonUtil.getPersonObjectID(context, sPersonId);
				assert(ProgramCentralUtil.isNotNullString(sUSerEffortId));
				if(!mAllPersonsMap.containsKey(sPersonId))
				{
					Map mUserMap = new HashMap();
					mUserMap.put("id",sPersonObjId);
					mUserMap.put("effortIds",sUSerEffortId);
					mAllPersonsMap.put(sPersonId, mUserMap);
					objList.add(mUserMap);
				}
				else
				{
					Map mPersonMap = (Map)mAllPersonsMap.get(sPersonId);
					String sOldEffortIds = (String)mPersonMap.get("effortIds");
					String sModEffortIds = sOldEffortIds +","+  sUSerEffortId;
					mPersonMap.put("effortIds", sModEffortIds);
				}
				//Added:PRG:R213:I16:08-Dec-2011:IR-100320V6R2013:End
			}
		}

		JPO.packArgs(objList);
		return objList;
	}

	/**
	 * Gets the relationship Ids
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectid is the task Id
	 * @param sRelPattern - pattern to match relationships
	 * @param sTypePattern - pattern to match types
	 * @param personid is the person Id
	 * @param boolGetTo - get To relationships
	 * @param boolGetFrom - get From relationships
	 * @param iLevel - the number of levels to expand, 0 equals expand all.
	 * @return String containing the relationship Id
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public String getRelIds(matrix.db.Context context, String objectid, String sRelPattern, String sTypePattern, String personid, boolean boolGetTo, boolean boolGetFrom, short iLevel)
			throws matrix.util.MatrixException
			{
		BusinessObject bo = new BusinessObject(objectid);
		bo.open(context);
		// Get the Efforts connected to this task object.
		ExpansionWithSelect effortSelect = null;
		// build select params
		SelectList selectObjStmts = new SelectList();
		SelectList selectRelStmts = new SelectList();
		selectObjStmts.addId();
		selectRelStmts.addId();
		effortSelect = bo.expandSelect(context, sRelPattern, sTypePattern, selectObjStmts,
				selectRelStmts, boolGetTo, boolGetFrom, iLevel);
		RelationshipWithSelectItr relObjectItr = new RelationshipWithSelectItr(effortSelect.getRelationships());
		Hashtable objectRelAttributes = new Hashtable();
		Hashtable effortBusObjAttributes = new Hashtable();
		String sid = "";
		while (relObjectItr.next()) {

			objectRelAttributes =  relObjectItr.obj().getRelationshipData();
			String relid = (String)objectRelAttributes.get("id");
			effortBusObjAttributes =  relObjectItr.obj().getTargetData();
			String sEffortId = (String)effortBusObjAttributes.get("id");
			if (sEffortId != null && sEffortId.trim().length() > 0 && sEffortId.equals(personid))
			{
				return relid;
			}
		}
		bo.close(context);
		return sid;
			}

	/**
	 * Gets the effort object Ids
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectid is the task Id
	 * @param sRelPattern - pattern to match relationships
	 * @param sTypePattern - pattern to match types
	 * @param personid is the person Id
	 * @param boolGetTo - get To relationships
	 * @param boolGetFrom - get From relationships
	 * @param iLevel - the number of levels to expand, 0 equals expand all.
	 * @return Vector containing the effort ids
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getEffortIds(matrix.db.Context context, String objectid, String sRelPattern, String sTypePattern, boolean boolGetTo, boolean boolGetFrom, short iLevel)
			throws matrix.util.MatrixException
			{
		BusinessObject bo = new BusinessObject(objectid);
		bo.open(context);
		// Get the Efforts connected to this task object.
		ExpansionWithSelect effortSelect = null;
		// build select params
		SelectList selectObjStmts = new SelectList();
		SelectList selectRelStmts = new SelectList();
		selectObjStmts.addId();
		//Addd by viru
		ContextUtil.pushContext(context);
		//end by viru
		try {
			effortSelect = bo.expandSelect(context, sRelPattern, sTypePattern, selectObjStmts,
					selectRelStmts, boolGetTo, boolGetFrom, iLevel);
		} finally {
			//Addd by viru
			ContextUtil.popContext(context);
			//end by viru
		}
		RelationshipWithSelectItr relObjectItr = new RelationshipWithSelectItr(effortSelect.getRelationships());
		Hashtable objectRelAttributes = new Hashtable();
		Hashtable effortBusObjAttributes = new Hashtable();
		Vector vEffortIds = new Vector();
		while (relObjectItr.next()) {
			effortBusObjAttributes =  relObjectItr.obj().getTargetData();
			String sEffortId = (String)effortBusObjAttributes.get("id");
			if (sEffortId != null && sEffortId.trim().length() > 0 )
			{
				vEffortIds.addElement(sEffortId);
			}
		}
		bo.close(context);
		return vEffortIds;
			}

	/**
	 * Gets the task assignee information
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectId - the context task object Id
	 * @return MapList containing the task assignee details
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public MapList getAssigneeInfo(matrix.db.Context context, String objectid)
			throws matrix.util.MatrixException
			{
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		task.setId(objectid);
		StringList busSelects = new StringList(1);
		busSelects.add(task.SELECT_ID);
		StringList relSelects = new StringList(1);
		MapList maplist = new MapList();
		maplist = (MapList)task.getAssignees(context, busSelects, relSelects, null);
		return (maplist);
			}

	/**
	 * Gets the assigned date of the task assignee
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectid is the task Id
	 * @param sRelPattern - pattern to match relationships
	 * @param sTypePattern - pattern to match types
	 * @param personid is the person Id
	 * @param boolGetTo - get To relationships
	 * @param boolGetFrom - get From relationships
	 * @param iLevel - the number of levels to expand, 0 equals expand all.
	 * @return Vector containing assigned date
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getAssignedDate(matrix.db.Context context, String objectid, String sRelPattern, String sTypePattern, boolean boolGetTo, boolean boolGetFrom, short iLevel)
			throws matrix.util.MatrixException
			{
		BusinessObject bo = new BusinessObject(objectid);
		bo.open(context);
		ExpansionWithSelect effortSelect = null;
		SelectList selectObjStmts = new SelectList();
		SelectList selectRelStmts = new SelectList();
		selectObjStmts.addId();
		String rel_AssignedDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_AssignedDate");
		selectRelStmts.addAttribute(rel_AssignedDate);
		effortSelect = bo.expandSelect(context, sRelPattern, sTypePattern, selectObjStmts,
				selectRelStmts, boolGetTo, boolGetFrom, iLevel);
		RelationshipWithSelectItr relObjectItr = new RelationshipWithSelectItr(effortSelect.getRelationships());
		String percentAllocation = "";
		Hashtable objectRelAttributes = new Hashtable();
		Hashtable effortBusObjAttributes = new Hashtable();
		Vector vPercentAlloc = new Vector();
		while (relObjectItr.next()) {
			effortBusObjAttributes =  relObjectItr.obj().getTargetData();
			String sTaskId = (String)effortBusObjAttributes.get("id");
			objectRelAttributes =  relObjectItr.obj().getRelationshipData();
			percentAllocation =(String)objectRelAttributes.get("attribute[" + rel_AssignedDate + "]");
			if (percentAllocation != null && percentAllocation.trim().length() > 0 )
			{
				HashMap hm = new HashMap();
				hm.put("id",sTaskId);
				hm.put("assigneddate",percentAllocation);
				vPercentAlloc.addElement(hm);
			}
		}
		bo.close(context);
		return vPercentAlloc;
			}

	/**
	 * Gets the percentage allocation of the task assignee
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param objectid is the task Id
	 * @param sRelPattern - pattern to match relationships
	 * @param sTypePattern - pattern to match types
	 * @param personid is the person Id
	 * @param boolGetTo - get To relationships
	 * @param boolGetFrom - get From relationships
	 * @param iLevel - the number of levels to expand, 0 equals expand all.
	 * @return Vector containing percentage allocation
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getAllocation(matrix.db.Context context, String objectid, String sRelPattern, String sTypePattern, boolean boolGetTo, boolean boolGetFrom, short iLevel)
			throws matrix.util.MatrixException
			{
		BusinessObject bo = new BusinessObject(objectid);
		bo.open(context);
		ExpansionWithSelect effortSelect = null;
		SelectList selectObjStmts = new SelectList();
		SelectList selectRelStmts = new SelectList();
		selectObjStmts.addId();
		String rel_allocation=(String)PropertyUtil.getSchemaProperty(context,"attribute_PercentAllocation");
		selectRelStmts.addAttribute(rel_allocation);
		effortSelect = bo.expandSelect(context, sRelPattern, sTypePattern, selectObjStmts,
				selectRelStmts, boolGetTo, boolGetFrom, iLevel);
		RelationshipWithSelectItr relObjectItr = new RelationshipWithSelectItr(effortSelect.getRelationships());
		String percentAllocation = "";
		Hashtable objectRelAttributes = new Hashtable();
		Hashtable effortBusObjAttributes = new Hashtable();
		Vector vPercentAlloc = new Vector();
		while (relObjectItr.next()) {
			effortBusObjAttributes =  relObjectItr.obj().getTargetData();
			String sTaskId = (String)effortBusObjAttributes.get("id");
			objectRelAttributes =  relObjectItr.obj().getRelationshipData();
			percentAllocation =(String)objectRelAttributes.get("attribute[" + rel_allocation + "]");
			if (percentAllocation != null && percentAllocation.trim().length() > 0 )
			{
				HashMap hm = new HashMap();
				hm.put("id",sTaskId);
				hm.put("allocation",percentAllocation);
				vPercentAlloc.addElement(hm);
			}
		}
		bo.close(context);
		return vPercentAlloc;
			}

	/**
	 * Gets the list of efforts objects which are in submit state
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectId - the context task object Id
	 * @return MapList containing effort ids
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getProjectTaskListSubmitted (Context context,
			String[] args)
					throws Exception
					{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		String objectId = (String) paramMap.get("objectId");
		task.setId(objectId);

		StringList busSelects = new StringList(1);
		busSelects.add(task.SELECT_ID);
		MapList mlist = (MapList)task.getTasks(context, task, 0, busSelects, null);

		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String typePattern=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String s_state = ProgramCentralConstants.STATE_EFFORT_SUBMIT;
		String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
		String st_attr = "attribute[" + attribute_WeekEndingDate + "]";
		MapList objList = new MapList();
		boolean show_results = true;
		Vector vec = null;
		if (mlist != null)
		{
			Iterator mapItr = mlist.iterator();
			while (mapItr.hasNext())
			{
				Map commandMap = (Map)mapItr.next();
				String id = (String) commandMap.get("id");
				if (id != null && id.trim().length() > 0 )
				{
					//HashMap argsMap = new HashMap();
					vec = null;
					//get the list of efforts for this task
					vec = getEffortIds(context,id,relPattern,typePattern,false,true,(short)1);
					show_results = isEffortAvailable(context,vec,s_state);
					if(show_results)
					{
						//argsMap.put("id",id);
						//objList.add(argsMap);
						//get the list of efforts for this task
						//vec = getEffortIds(context,id,relPattern,typePattern,false,true,(short)1);


						StringList relSel = new StringList();
						StringList busSel = new StringList(2);
						busSel.add(task.SELECT_ID );
						busSel.add(st_attr);

						DomainObject dm = DomainObject.newInstance(context, id);

						MapList mlObj = dm.getRelatedObjects(context,
								PropertyUtil.getSchemaProperty( context, "relationship_hasEfforts" ),
								PropertyUtil.getSchemaProperty( context, "type_Effort" ),
								busSel,
								relSel,
								false,
								true,
								(short) 1,
								null,
								null);
						mlObj.addSortKey(st_attr, "descending", "date");
						mlObj.sort();

						if (mlObj != null && mlObj.size() > 0)
						{
							HashMap argsMap = new HashMap();
							argsMap.put("id",id);
							objList.add(argsMap);

							Map map;
							Iterator itr;
							for (itr = mlObj.iterator(); itr.hasNext(); )
							{

								map = (Map) itr.next();

								String eid = (String) map.get(task.SELECT_ID);
								if (eid != null && eid.trim().length() > 0 )
								{

									DomainObject doObj = DomainObject.newInstance(context, eid);
									String sState = FrameworkUtil.getCurrentState(context,doObj).getName();
									if(sState!=null && (sState.equalsIgnoreCase(s_state)))
									{
										HashMap effortMap = new HashMap();
										effortMap.put("id",eid);
										objList.add(effortMap);
									}
								}
							}

						}



					}
				}

			}

		}
		JPO.packArgs(objList);
		return objList;

					}

	/**
	 * Gets the list of efforts objects which are in rejected state
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectId - the context task object Id
	 * @return MapList containing effort ids
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getProjectTaskListPending (Context context,
			String[] args)
					throws Exception
					{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");

		String objectId = (String) paramMap.get("objectId");
		task.setId(objectId);

		StringList busSelects = new StringList(1);
		busSelects.add(task.SELECT_ID);

		StringList relSelects = new StringList(1);
		relSelects.add(task.SELECT_ID);
		MapList mlist = (MapList)task.getTasks(context, task, 0, busSelects, null);
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String typePattern=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");

		String s_state = ProgramCentralConstants.STATE_EFFORT_REJECTED;
		String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
		String st_attr = "attribute[" + attribute_WeekEndingDate + "]";

		MapList objList = new MapList();
		Vector vec = null;
		boolean show_results = true;
		if (mlist != null)
		{
			Iterator mapItr = mlist.iterator();
			while (mapItr.hasNext())
			{
				Map commandMap = (Map)mapItr.next();
				String id = (String) commandMap.get("id");
				if (id != null && id.trim().length() > 0 )
				{
					//HashMap argsMap = new HashMap();
					vec = null;
					//get the list of efforts for this task
					vec = getEffortIds(context,id,relPattern,typePattern,false,true,(short)1);
					show_results = isEffortAvailable(context,vec,s_state);
					if(show_results)
					{
						StringList relSel = new StringList();
						StringList busSel = new StringList(2);
						busSel.add(task.SELECT_ID );
						busSel.add(st_attr);

						DomainObject dm = DomainObject.newInstance(context, id);

						MapList mlObj = dm.getRelatedObjects(context,
								PropertyUtil.getSchemaProperty( context, "relationship_hasEfforts" ),
								PropertyUtil.getSchemaProperty( context, "type_Effort" ),
								busSel,
								relSel,
								false,
								true,
								(short) 1,
								null,
								null);
						mlObj.addSortKey(st_attr, "descending", "date");
						mlObj.sort();

						if (mlObj != null && mlObj.size() > 0)
						{
							HashMap argsMap = new HashMap();
							argsMap.put("id",id);
							objList.add(argsMap);

							Map map;
							Iterator itr;
							for (itr = mlObj.iterator(); itr.hasNext(); )
							{

								map = (Map) itr.next();

								String eid = (String) map.get(task.SELECT_ID);
								if (eid != null && eid.trim().length() > 0 )
								{

									DomainObject doObj = DomainObject.newInstance(context, eid);
									String sState = FrameworkUtil.getCurrentState(context,doObj).getName();
									if(sState!=null && (sState.equalsIgnoreCase(s_state)))
									{
										HashMap effortMap = new HashMap();
										effortMap.put("id",eid);
										objList.add(effortMap);
									}
								}
							}

						}



					}

				}

			}
		}
		JPO.packArgs(objList);
		return objList;
					}

	/**
	 * Check to see if any effort is available for the specified state
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param vec is a Vector containing the effort ids
	 * @param target_state is the state of the effort object
	 * @param strDate is the weekending date of the effort
	 * @return boolean: true if any effort is available and fasle otherwise
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public boolean isEffortAvailable(Context context,Vector vec,String target_state )
			throws Exception
			{
		boolean is_effort = false;

		if(vec==null)
		{
			return is_effort;
		}

		if (vec != null )
		{
			for(int a=0;a<vec.size();a++)
			{
				String eid = (String) vec.get(a);
				if (eid != null && eid.trim().length() > 0 )
				{
					DomainObject doObj = DomainObject.newInstance(context, eid);
					String sState = FrameworkUtil.getCurrentState(context,doObj).getName();
					if(sState!=null && sState.equalsIgnoreCase(target_state))
					{
						is_effort=true;
						break;
					}
				}
			}
		}

		return is_effort;

			}

	/**
	 * Gets the list of efforts objects which are in Approved state
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectId - the context task object Id
	 * @return MapList containing effort ids
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getProjectTaskListApproved (Context context,
			String[] args)
					throws Exception
					{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		com.matrixone.apps.program.Task task = (com.matrixone.apps.program.Task) DomainObject.newInstance(context, DomainConstants.TYPE_TASK, "PROGRAM");
		String objectId = (String) paramMap.get("objectId");
		task.setId(objectId);

		StringList busSelects = new StringList(1);
		busSelects.add(task.SELECT_ID);

		StringList relSelects = new StringList(1);
		relSelects.add(task.SELECT_ID);
		MapList mlist = (MapList)task.getTasks(context, task, 0, busSelects, null);

		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String typePattern=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");

		String s_state = ProgramCentralConstants.STATE_EFFORT_APPROVED;
		String attribute_WeekEndingDate=(String)PropertyUtil.getSchemaProperty(context,"attribute_WeekEndingDate");
		String st_attr = "attribute[" + attribute_WeekEndingDate + "]";

		MapList objList = new MapList();
		boolean show_results = true;
		Vector vec = null;
		if (mlist != null)
		{
			Iterator mapItr = mlist.iterator();
			while (mapItr.hasNext())
			{
				Map commandMap = (Map)mapItr.next();
				String id = (String) commandMap.get("id");
				if (id != null && id.trim().length() > 0 )
				{
					//HashMap argsMap = new HashMap();
					vec = null;
					//get the list of efforts for this task
					vec = getEffortIds(context,id,relPattern,typePattern,false,true,(short)1);
					show_results = isEffortAvailable(context,vec,s_state);
					if(show_results)
					{

						StringList relSel = new StringList();
						StringList busSel = new StringList(2);
						busSel.add(task.SELECT_ID );
						busSel.add(st_attr);

						DomainObject dm = DomainObject.newInstance(context, id);

						MapList mlObj = dm.getRelatedObjects(context,
								PropertyUtil.getSchemaProperty( context, "relationship_hasEfforts" ),
								PropertyUtil.getSchemaProperty( context, "type_Effort" ),
								busSel,
								relSel,
								false,
								true,
								(short) 1,
								null,
								null);
						mlObj.addSortKey(st_attr, "descending", "date");
						mlObj.sort();

						if (mlObj != null && mlObj.size() > 0)
						{
							HashMap argsMap = new HashMap();
							argsMap.put("id",id);
							objList.add(argsMap);

							Map map;
							Iterator itr;
							for (itr = mlObj.iterator(); itr.hasNext(); )
							{

								map = (Map) itr.next();

								String eid = (String) map.get(task.SELECT_ID);
								if (eid != null && eid.trim().length() > 0 )
								{

									DomainObject doObj = DomainObject.newInstance(context, eid);
									String sState = FrameworkUtil.getCurrentState(context,doObj).getName();
									if(sState!=null && (sState.equalsIgnoreCase(s_state)))
									{
										HashMap effortMap = new HashMap();
										effortMap.put("id",eid);
										objList.add(effortMap);
									}
								}
							}

						}

					}

				}

			}

		}
		JPO.packArgs(objList);
		return objList;

					}

	/**
	 * Displays a Yellow icon if the state of the effort object is Sumit
	 * and Green icon if it is Approved and Red icon if it is Rejected
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the HashMap containg the following arguments:
	 *    objectList - MapList containing the Object Id of task and effort
	 * @return Vector containing image icon as string
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getRejectionIndicator(Context context, String args[])
			throws Exception
			{

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		HashMap paramMap = (HashMap)programMap.get("paramList");
		String type_Effort = (String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String task_Management = (String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String relPattern = (String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		double dayMultiple=0;
		String statusGif="";
		String state_Approved = ProgramCentralConstants.STATE_EFFORT_APPROVED;
		String state_Rejected = ProgramCentralConstants.STATE_EFFORT_REJECTED;
		String state_Submit = ProgramCentralConstants.STATE_EFFORT_SUBMIT;

		statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border='0' alt=\"Submitted\"/>";
		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			id =(String)((Map)relBusObjPageList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);
			String sType = bus.getInfo(context, DomainObject.SELECT_TYPE);
			if(sType != null && sType.equalsIgnoreCase(type_Effort)){
				String sState = FrameworkUtil.getCurrentState(context,bus).getName();

				if ( sState !=null && sState.trim().length()> 0 && sState.trim().equalsIgnoreCase(state_Approved))
				{
					statusGif = "<img src=\"../common/images/iconStatusGreen.gif\" border='0' alt=\"Approved\"/>";

				} else if (sState !=null && sState.trim().length()> 0 && sState.trim().equalsIgnoreCase(state_Rejected))

				{
					statusGif = "<img src=\"../common/images/iconStatusRed.gif\" border='0' alt=\"Rejected\"/>";
				}

				else if (sState !=null && sState.trim().length()> 0 && sState.trim().equalsIgnoreCase(state_Submit))

				{
					statusGif = "<img src=\"../common/images/iconStatusYellow.gif\" border='0' alt=\"Submitted\"/>";
				}

				columnValues.add(statusGif);

			} else{

				columnValues.add("");

			}
		}

		return columnValues;
			}

	/**
	 * updates and return remaining effort
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds objectId of effort object
	 * @return an String of remaining effort
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public String updateEffort(Context context, String args[])
			throws Exception
			{

		HashMap programMap  = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap    = (HashMap) programMap.get("paramMap");
		String objectId     = (String) paramMap.get("objectId");
		String strRemainingEffort = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Common.RemainingEffort", context.getSession().getLanguage());
		String remainModified = (String) paramMap.get(strRemainingEffort);

		String attribute_RemainingEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_RemainingEffort");
		String strRemain = "";


		String sun = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_Sunday") + "]";
		String mon = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_Monday") + "]";
		String tue = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_Tuesday") + "]";
		String wed = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_Wednesday") + "]";
		String thu = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_Thursday") + "]";
		String fri = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_Friday") + "]";
		String sat = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_Saturday") + "]";
		String remain = "attribute[" + attribute_RemainingEffort + "]";


		StringList effortSelects = new StringList(7);
		effortSelects.add(sun);
		effortSelects.add(mon);
		effortSelects.add(tue);
		effortSelects.add(wed);
		effortSelects.add(thu);
		effortSelects.add(fri);
		effortSelects.add(sat);
		effortSelects.add(remain);

		DomainObject effortObj = DomainObject.newInstance(context, objectId);
		Map effortInfo = effortObj.getInfo(context, effortSelects);

		// check whether the Remaining Effort field value is changed, yes updateh attribute with new valuee
		// else calculate the remaining Remaining Effort.
		String remainFromDB = (String) effortInfo.get(remain);
		if(remainModified != null && remainFromDB != null && !remainModified.equals(remainFromDB))
		{
			effortObj.setAttributeValue(context, attribute_RemainingEffort, remainModified);
		}
		else
		{

			String effort = null;
			String key = null;
			double dblTotalEffort = 0;

			Iterator itr = effortInfo.keySet().iterator();
			while(itr.hasNext())
			{
				key = (String) itr.next();
				if(!"type".equals(key) && !"id".equals(key) && !remain.equals(key))
				{
					effort = (String) effortInfo.get(key);
					if (effort != null && effort.trim().length() > 0 )
					{
						dblTotalEffort += (new Double(effort).doubleValue());
					}
				}
			}

			double dblRemain = 0;
			double dblPlannedEffort = getPlannedEffort(context,objectId);

			if(dblPlannedEffort > dblTotalEffort)
			{
				dblRemain = dblPlannedEffort - dblTotalEffort;
			}

			strRemain = String.valueOf(dblRemain);
			effortObj.setAttributeValue(context, attribute_RemainingEffort, strRemain);

			//Getting the Task ID from the Effort id.
			String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
			String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
			Vector taskVec = getEffortIds(context,objectId,relPattern,type_TaskManagement,true,false,(short)1);
			String sTaskId = (String)taskVec.get(0);

			//calculation of percentage complete for the task
			double total_effort = getTotalTaskEffort(context,sTaskId,ProgramCentralConstants.STATE_EFFORT_APPROVED);
			double remain_effort = getTotalTaskRemainingEffort(context, sTaskId);
			double f_results = total_effort/(total_effort + remain_effort);
			double task_percentcomplete = f_results*100;

			if(task_percentcomplete>100)
				task_percentcomplete=100;

			DomainObject bus_task  = DomainObject.newInstance(context, sTaskId);

			String attribute_PercentComplete=(String)PropertyUtil.getSchemaProperty(context,"attribute_PercentComplete");
			String completedPercent = bus_task.getAttributeValue(context,attribute_PercentComplete);

			Double dble = new Double(completedPercent);
			double percent = dble.doubleValue();

			String newAttr_value = ""+roundWBSPercentComplete(task_percentcomplete);
			if(task_percentcomplete>percent)
			{
				try{
					bus_task.setAttributeValue(context,attribute_PercentComplete,newAttr_value);
				}catch(Exception ex){
					System.out.println("ERROR UPDATING THE WBS PERCENT COMPLETE. "+ex.toString());
				}
			}

		}

		return strRemain;

			}

	/**
	 * rounds the value of percentage complete effort to have single fraction digit
	 *
	 * @param task_rem holds percentage complete effort
	 * @return a double of percentage complete effort
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public double roundWBSPercentComplete(double task_rem)
			throws Exception
			{
		// Originally the percentage was rounded to the nearest value from percentage list from properties file.
		// As fix of Bug 343696 the rounding is corrected to me more precise.
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(1);
		String strRoundedValue = numberFormat.format(task_rem);
		double roundedValue = 0;
		if (strRoundedValue != null && !"".equals(strRoundedValue)) {
			roundedValue = Task.parseToDouble(strRoundedValue);
		}
		return roundedValue;
			}

	/**
	 * This method is used to check whether to display effort filter on WBS or
	 * not.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            contains the table name.
	 * @return Vector containing the names of timesheet owner.
	 * @throws Exception
	 *             if the operation fails
	 * @since Added by vf2 for release version V6R2011x w.r.t IR-056503.
	 */        
	public boolean showEffortFilter(Context context, String args[]) throws MatrixException
	{
		boolean canShow = false;
		try {
			HashMap programMap  = (HashMap) JPO.unpackArgs(args);
			String selectedTable = (String) programMap.get("selectedTable");

			if(selectedTable != null && "PMCProjectTaskEffort".equals(selectedTable) == true){
				canShow = true; 
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
		return canShow;
	}
	/**
	 * Gets the total effort for task according to state.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param taskId is the task object Id
	 * @param strState is the state of object.   
	 * @return double containing the total effort of the task
	 * @throws Exception if the operation fails
	 * @since for release version V6R2011x            
	 * @author vf2 
	 */
	public double getTotalTaskEffort(Context context, String taskId,String strState)
			throws Exception
			{
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String attribute_TotalEffort=(String)PropertyUtil.getSchemaProperty(context,"attribute_TotalEffort");
		Vector effortIds = getEffortIds(context,taskId,relPattern,type_Effort,false,true,(short)1);
		double intDouble = 0.0;
		String[] saEffortIds = new String[effortIds.size()];
		effortIds.copyInto(saEffortIds);
		String strValue = getTotalPendingEffort(context, strState,true, saEffortIds);
		if(ProgramCentralUtil.isNullString(strValue))
			strValue = "0.0";
		intDouble = Task.parseToDouble(strValue); 
		return intDouble;
			}    

	/**
	 * This method is used to get filter value.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String
	 *             containing the value of filter.
	 * @return String - filter value 
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */	    
	private String getWhereClause(Context context, String effortFilter) throws Exception 
	{
		String stateAll = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Effort.All", "en");
		String stateSubmit = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.Effort.Submitted", "en");
		if(stateSubmit.equals(effortFilter)==true) {
			effortFilter = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.WeeklyTimesheet.Submit", "en");
		}
		String strBusWhere = null;
		if(effortFilter != null && !effortFilter.equals(stateAll)){
			strBusWhere = DomainConstants.SELECT_CURRENT+"=="+effortFilter;
		}
		return strBusWhere;
	}

	/**
	 * This method is used to get the effort values for each effort object for
	 * Monday.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: objectList
	 *            - MapList containing the Object Id of task and effort
	 *            paramList - Map containing the value of filter
	 *            PMCWBSEffortFilter.
	 * @return Vector - containing the effort values on Monday.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */	    
	public Vector getMonday(Context context, String args[])
			throws Exception
			{
		Vector columnValues = getDayEffort(context, args, "attribute_Monday");
		return columnValues;
			}    

	/**
	 * This method is used to get the effort values for each effort object for
	 * Tuesday.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: objectList
	 *            - MapList containing the Object Id of task and effort
	 *            paramList - Map containing the value of filter
	 *            PMCWBSEffortFilter.
	 * @return Vector - containing the effort values on Tuesday.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */	
	public Vector getTuesday(Context context, String args[])
			throws Exception
			{
		Vector columnValues = getDayEffort(context, args, "attribute_Tuesday");	    
		return columnValues;
			}  

	/**
	 * This method is used to get the effort values for each effort object for
	 * Wednesday.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: objectList
	 *            - MapList containing the Object Id of task and effort
	 *            paramList - Map containing the value of filter
	 *            PMCWBSEffortFilter.
	 * @return Vector - containing the effort values on Wednesday.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public Vector getWednesday(Context context, String args[])
			throws Exception
			{
		Vector columnValues = getDayEffort(context, args, "attribute_Wednesday");	    
		return columnValues;
			}  	

	/**
	 * This method is used to get the effort values for each effort object for
	 * Thursday.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: objectList
	 *            - MapList containing the Object Id of task and effort
	 *            paramList - Map containing the value of filter
	 *            PMCWBSEffortFilter.
	 * @return Vector - containing the effort values on Thursday.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public Vector getThursday(Context context, String args[])
			throws Exception
			{
		Vector columnValues = getDayEffort(context, args, "attribute_Thursday");	    
		return columnValues;
			}  	

	/**
	 * This method is used to get the effort values for each effort object for
	 * Friday.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: objectList
	 *            - MapList containing the Object Id of task and effort
	 *            paramList - Map containing the value of filter
	 *            PMCWBSEffortFilter.
	 * @return Vector - containing the effort values on Friday.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public Vector getFriday(Context context, String args[])
			throws Exception
			{
		Vector columnValues = getDayEffort(context, args, "attribute_Friday");	    
		return columnValues;
			}  	

	/**
	 * This method is used to get the effort values for each effort object for
	 * Saturday.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: objectList
	 *            - MapList containing the Object Id of task and effort
	 *            paramList - Map containing the value of filter
	 *            PMCWBSEffortFilter.
	 * @return Vector - containing the effort values on Saturday.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public Vector getSaturday(Context context, String args[])
			throws Exception
			{
		Vector columnValues = getDayEffort(context, args, "attribute_Saturday");	    
		return columnValues;
			}  

	/**
	 * This method is used to get the effort values for each effort object for
	 * Sunday.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the HashMap containg the following arguments: objectList
	 *            - MapList containing the Object Id of task and effort
	 *            paramList - Map containing the value of filter
	 *            PMCWBSEffortFilter.
	 * @return Vector - containing the effort values on Sunday.
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2011x
	 * @author vf2
	 */		
	public Vector getSunday(Context context, String args[])
			throws Exception
			{
		Vector columnValues = getDayEffort(context, args, "attribute_Sunday");	    
		return columnValues;
			}  	

	/**
	 * This method used to get state for each object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the following input arguments: objectList - Contains a
	 *            MapList of Maps which contains object names.
	 * @return Vector - vector containing state of object.
	 * @throws MatrixException
	 *             if operation fails
	 * 
	 */		
	public Vector getState(Context context, String[] args) throws MatrixException {
		Vector vState = new Vector();
		try {
			HashMap paramMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList)paramMap.get("objectList");
			Iterator objectListIterator = objectList.iterator();
			StringList slObj = new StringList();
			slObj.add(DomainConstants.SELECT_ID);
			slObj.add(DomainConstants.SELECT_TYPE);
			slObj.add(DomainConstants.SELECT_CURRENT);
			String[] saIds = new String[objectList.size()];
			int i=0;
			while (objectListIterator.hasNext()) {
				Map mapObject = (Map) objectListIterator.next();
				String objectId = (String)mapObject.get(DomainConstants.SELECT_ID);
				if(objectId!= null && objectId.trim().length()>0) {
					saIds[i] = objectId;
					i++;
				}
			}
			BusinessObjectWithSelectList bObjWithList = null;
			BusinessObjectWithSelect bObjWithSl = null;
			bObjWithList = BusinessObject.getSelectBusinessObjectData(context, saIds, slObj);
			for(BusinessObjectWithSelectItr itr = new BusinessObjectWithSelectItr(bObjWithList);itr.next();)
			{
				ContextUtil.pushContext(context);
				bObjWithSl = itr.obj();
				String strState = bObjWithSl.getSelectData(DomainConstants.SELECT_CURRENT);
				String strType = bObjWithSl.getSelectData(DomainConstants.SELECT_TYPE);
				ContextUtil.popContext(context);
				if(TYPE_EFFORT.equalsIgnoreCase(strType)){
					if(ProgramCentralConstants.STATE_EFFORT_EXISTS.equals(strState)){
						strState = DomainConstants.STATE_PROJECT_SPACE_CREATE;
					} 						
					vState.add(i18nNow.getStateI18NString(ProgramCentralConstants.POLICY_PROJECT_SPACE, strState, context.getSession().getLanguage()));						
				} else {							
					vState.add(i18nNow.getStateI18NString(ProgramCentralConstants.POLICY_PROJECT_SPACE, strState, context.getSession().getLanguage()));
				}
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
		return vState;
	}	

	/**
	 * This method used to update the percentage complete attribute for task
	 * object.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds the task id.
	 * @return void
	 * @throws Exception
	 *             if operation fails
	 * @since for release version V6R2012
	 * @author vf2
	 */		
	public void triggerUpdateTaskPercentageComplete(Context context, String[] args)
			throws Exception {
		try {
			String taskId  = args[0]; 
			double f_results = 0;
			if(ProgramCentralUtil.isNotNullString(taskId)) {
				emxEffortManagementBase_mxJPO emxEffortJPO = new emxEffortManagementBase_mxJPO(context,new String[0]);
				DomainObject domTaskObj  = DomainObject.newInstance(context, taskId);
				if(Task.parseToDouble(domTaskObj.getAttributeValue(context, DomainConstants.ATTRIBUTE_PERCENT_COMPLETE)) < 100) {			    	
					if(ProgramCentralUtil.isNotNullString(domTaskObj.getTypeName())) {						
						double totalEffort = emxEffortJPO.getTotalTaskEffort(context,taskId,ProgramCentralConstants.STATE_EFFORT_APPROVED);
						double remain_effort = emxEffortJPO.getTotalTaskRemainingEffort(context, taskId);			            
						double total_effort = totalEffort;
						double total_pending = emxEffortJPO.getTotalTaskEffort(context,taskId,ProgramCentralConstants.STATE_EFFORT_SUBMIT);
						double final_remaining = emxEffortJPO.getTaskPlannedEffort(context,taskId);
						double submit_effort = total_pending;  
						if(remain_effort<=0 && total_effort==0 && total_pending==0) {			            
							remain_effort= final_remaining;
						}
						final_remaining=final_remaining-(total_pending+total_effort);
						if(final_remaining < 0) {
							final_remaining = 0;
						}		            
						if ((totalEffort + remain_effort) != 0) {			            
							f_results = totalEffort/(totalEffort + (final_remaining + submit_effort));				                
						}           
						double dblTaskPer = f_results*100;
						if(dblTaskPer>100)
							dblTaskPer=100;			            
						domTaskObj.setAttributeValue(context, DomainConstants.ATTRIBUTE_PERCENT_COMPLETE, 
								Double.toString(emxEffortJPO.roundWBSPercentComplete(dblTaskPer)));
					}
				}
			}
		} catch(Exception e) {
			throw new MatrixException(e.getMessage());
		}
	}	    
	/**
	 * This method gives rejected effort value in double format.
	 * @param context the eMatrix <code>Context</code> object
	 * @param taskId to get value of effort which are in rejected
	 * @return value in double
	 * @throws Exception
	 */
	public double getTotalTaskRejectedEffort(Context context, String taskId) throws Exception
	{
		double valRej=0;
		try{    		
			String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
			String type_TaskManagement=(String)PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
			String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
			String s_rejectstate = ProgramCentralConstants.STATE_EFFORT_REJECTED;
			Vector effortIds = getEffortIds(context, taskId, relPattern, type_Effort, false, true, (short)1);

			if(s_rejectstate == null || s_rejectstate.trim().length() == 0 ){
				s_rejectstate = "Rejected";
			}	
			String[] saEffortIds = new String[effortIds.size()];
			effortIds.copyInto(saEffortIds);
			valRej = Task.parseToDouble(getTotalPendingEffort(context, s_rejectstate,true, saEffortIds)); 
		}catch(Exception e){
			throw new Exception(e);
		}
		return valRej;
	}

	public double roundWBSEffortValue(double effort_rem)throws Exception
	{
		// Originally the percentage was rounded to the nearest value from percentage list from properties file.
		// As fix of Bug 343696 the rounding is corrected to me more precise.
		NumberFormat numberFormat = NumberFormat.getInstance();
		numberFormat.setMaximumFractionDigits(2);
		numberFormat.setGroupingUsed(false);
		String strRoundedValue = numberFormat.format(effort_rem);
		double roundedValue = 0;
		if (strRoundedValue != null && !"".equals(strRoundedValue)) {
			roundedValue = Task.parseToDouble(strRoundedValue);
		}
		return roundedValue;
	}
	/**
	 * This method used to get Effort values for perticular day in Week.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the task id.
	 * @return Vector Effort Values for perticular day.
	 * @throws Exception if operation fails
	 * 
	 */
	private Vector getDayEffort(Context context , String[] args, String strDayOfWeek)throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList =(MapList)programMap.get("objectList");
		int objListSize = objectList.size();
		Vector valDay = new Vector(objListSize);
		HashMap paramMap = (HashMap)programMap.get("paramList");
		String effortFilter = (String) paramMap.get("PMCWBSEffortFilter");
		String id = "";
		double dbl_sun = 0;
		String eff_val = "";
		String type_Effort=(String)PropertyUtil.getSchemaProperty(context,"type_Effort");
		String relPattern=(String)PropertyUtil.getSchemaProperty(context,"relationship_hasEfforts");
		String strBusWhere = getWhereClause(context,effortFilter);

		BusinessObjectWithSelectList busObjWithSelectList = null;
		BusinessObjectWithSelect bows = null;
		StringList slBusSelect = new StringList();
		slBusSelect.addElement(DomainConstants.SELECT_ID);
		slBusSelect.addElement(DomainConstants.SELECT_TYPE);
		slBusSelect.addElement(DomainConstants.SELECT_NAME);
		slBusSelect.addElement(DomainConstants.SELECT_NAME);
		slBusSelect.addElement(ProgramCentralConstants.SELECT_IS_EFFORT);
		slBusSelect.addElement(ProgramCentralConstants.SELECT_IS_PERSON);
		slBusSelect.addElement(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);

		String [] sabusIds = new String[objListSize];
		for (int i = 0; i < objListSize; i++)
		{
			id =(String)((Map)objectList.get(i)).get("id");
			sabusIds[i] = id;
		}
		busObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context, sabusIds, slBusSelect);
		Map mBusObjectInfoMap = new LinkedHashMap();  

		for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(busObjWithSelectList); itr.next();)
		{
			bows = itr.obj();
			String sBusId = bows.getSelectData(DomainConstants.SELECT_ID);
			String sBusType = bows.getSelectData(DomainConstants.SELECT_TYPE);
			String sBusName = bows.getSelectData(DomainConstants.SELECT_NAME);
			String sIsEffort = bows.getSelectData(ProgramCentralConstants.SELECT_IS_EFFORT);
			String sIsPerson = bows.getSelectData(ProgramCentralConstants.SELECT_IS_PERSON);
			String sIsProjectSpace = bows.getSelectData(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);

			Map mBusObject = new LinkedHashMap();
			mBusObject.put(DomainConstants.SELECT_TYPE, sBusType);
			mBusObject.put(DomainConstants.SELECT_NAME,sBusName);
			mBusObject.put(ProgramCentralConstants.SELECT_IS_EFFORT,sIsEffort);
			mBusObject.put(ProgramCentralConstants.SELECT_IS_PERSON,sIsPerson);
			mBusObject.put(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE,sIsProjectSpace);

			mBusObjectInfoMap.put(sBusId, mBusObject);
		}

		for (int i = 0; i < objListSize; i++)
		{
			id =(String)((Map)objectList.get(i)).get("id");
			DomainObject bus  = DomainObject.newInstance(context, id);

			Map mBusObjectInfo = (Map)mBusObjectInfoMap.get(id);
			String sIsEffortType = (String)mBusObjectInfo.get(ProgramCentralConstants.SELECT_IS_EFFORT);
			String sPersonType = (String)mBusObjectInfo.get(ProgramCentralConstants.SELECT_IS_PERSON);
			String sProjectSpaceType = (String)mBusObjectInfo.get(ProgramCentralConstants.SELECT_IS_PROJECT_SPACE);
			if("true".equalsIgnoreCase(sIsEffortType))
			{
				dbl_sun = getEffortDaily(context, id, strDayOfWeek);
				NumberFormat numberFormat = NumberFormat.getInstance();
				numberFormat.setMaximumFractionDigits(2);
				String strRoundedValue = numberFormat.format(dbl_sun);
				double roundedValue = 0;
				if (strRoundedValue != null && !"".equals(strRoundedValue)) {
					roundedValue = Task.parseToDouble(strRoundedValue);
				}
				eff_val = "" + roundedValue;
				//To show values on UI in correct Format
				if(eff_val.contains("."))
				{
					String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
					eff_val = eff_val.replace('.',separator.charAt(0));
				}
				valDay.add(eff_val);
			}
			else if("true".equalsIgnoreCase(sPersonType))
			{
				String sEffortIds = (String)((Map)objectList.get(i)).get("effortIds");
				if(ProgramCentralUtil.isNotNullString(sEffortIds))
				{
					StringList slEffortIds = FrameworkUtil.splitString(sEffortIds,",");
					String[] saEffortIds = new String[slEffortIds.size()];
					slEffortIds.copyInto(saEffortIds);
					StringList slSelect = new StringList();
					slSelect.add(DomainConstants.SELECT_ID);
					slSelect.add(DomainConstants.SELECT_CURRENT);
					double dblValue=0.0;

					busObjWithSelectList = BusinessObject.getSelectBusinessObjectData(context, saEffortIds, slSelect);

					for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(busObjWithSelectList); itr.next();)
					{
						bows = itr.obj();
						String effortId = bows.getSelectData(DomainConstants.SELECT_ID);
						dbl_sun = getEffortDaily(context, effortId, strDayOfWeek);
						dblValue += dbl_sun;    										
					}
					eff_val = "" + roundWBSEffortValue(dblValue);      	       	  
					//To show values on UI in correct Format
					if(eff_val.contains("."))
					{
						String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
						eff_val = eff_val.replace('.',separator.charAt(0));
					}
					valDay.add(eff_val);
				}
				else
				{
					valDay.add("");
				}

			}
			else
			{
				if("true".equalsIgnoreCase(sProjectSpaceType))
				{
					valDay.add("");
				}
				else
				{      			
					MapList mpList = bus.getRelatedObjects(context,
							relPattern, type_Effort,new StringList(DomainConstants.SELECT_ID), null,
							false, true, (short)0,strBusWhere,"",0,null,null,null);	
					Vector effortIds = new Vector();
					for(int k=0; k<mpList.size(); k++) {
						Map map = (Map)mpList.get(k);
						effortIds.add((String)map.get(DomainConstants.SELECT_ID));
					}
					dbl_sun = getTaskDaily(context, effortIds, strDayOfWeek);
					NumberFormat numberFormat = NumberFormat.getInstance();
					numberFormat.setMaximumFractionDigits(2);
					String strRoundedValue = numberFormat.format(dbl_sun);
					double roundedValue = 0;
					if (strRoundedValue != null && !"".equals(strRoundedValue)) {
						roundedValue = Task.parseToDouble(strRoundedValue);
					}
					eff_val = "" + roundedValue;    	            
					//To show values on UI in correct Format
					if(eff_val.contains("."))
					{
						String separator = FrameworkProperties.getProperty("emxFramework.DecimalSymbol");
						eff_val = eff_val.replace('.',separator.charAt(0));
					}
					valDay.add(eff_val);
				}
			}
		}
		return valDay;    	
	}
	//---------------------------------New APIs to optimize the performance------------------------------

		public double getTaskPlannedEffort (Context context, DomainObject taskObject,Map taskInfoMap) throws Exception {
			
			String type_Person=(String)PropertyUtil.getSchemaProperty(context,"type_Person");
			String rel_assigned=(String)PropertyUtil.getSchemaProperty(context,"relationship_AssignedTasks");
			String attribute_Duration=(String)PropertyUtil.getSchemaProperty(context,"attribute_TaskEstimatedDuration");
			Vector taskVec = getPercentAllocation(context,taskObject,rel_assigned,type_Person,true,false,(short)1);

			String task_duration = (String)taskInfoMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION);
			String resultinHours = (String)taskInfoMap.get("attribute["+attribute_Duration+"].unitvalue[h]");
			
			double result = 0;

			if(taskVec.size()==0)
			{
				return (Task.parseToDouble(resultinHours)); 
			}
			//Added:10-Mar-10:vm3:R209:PRG:Bug 010074
			double dArr[] = new double[taskVec.size()];
			for(int ind=0;ind<dArr.length;ind++){
				dArr[ind]=0;         
			}
			double dTemp = 0.0;
			for (int i = 0; i < taskVec.size(); i++)
			{

				double d_item =0;
				HashMap hm = (HashMap)taskVec.get(i);
				String sid = (String)hm.get("id");
				String allo = (String)hm.get("allocation");
				//String strAllocation = "";
				String strAll = "100";
				if (!allo.equals(strAll))
				{ 
					double iDuration = 0;
					double iPercentAllocation = 0;
					double iAllocation=0;
					iDuration = Task.parseToDouble(task_duration.trim());
					iPercentAllocation = Task.parseToDouble(allo.trim());
					if(iPercentAllocation >0){
						iAllocation = (((8 * iDuration)/100)*iPercentAllocation);	        	 
					}
					resultinHours = iAllocation + "";
					dTemp = Task.parseToDouble(resultinHours);
					dArr[i] = dTemp;
				}

				if(allo!=null && allo.trim().length() > 0 && resultinHours!=null && resultinHours.trim().length() > 0 )
				{
					Double allocation = new Double(allo);
					double alloc = allocation.doubleValue()/100;
					d_item= Task.parseToDouble(resultinHours); 
				}
				//result = result + d_item;
				result = result + dArr[i];
				//End-Added:10-Mar-10:vm3:R209:PRG:Bug 010074
			}
			return result;
		}
		public MapList getEffortInfoMapList(Context context, DomainObject taskObject, String sRelPattern, String sTypePattern, 
									boolean boolGetTo, boolean boolGetFrom, short iLevel) throws MatrixException {
			
			taskObject.open(context);
			
			StringList slSelect = new StringList();
			slSelect.add(DomainConstants.SELECT_ID);
			slSelect.add(DomainConstants.SELECT_CURRENT);
			slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_SUNDAY);
			slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_MONDAY);
			slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_TUESDAY);
			slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_WEDNESDAY);
			slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_THURSDAY);
			slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_FRIDAY);
			slSelect.add(ProgramCentralConstants.SELECT_ATTRIBUTE_SATURDAY);
			
			SelectList selectRelStmts = new SelectList();
			ContextUtil.pushContext(context);
			MapList effortMapList = null;
			try {
				effortMapList = taskObject.getRelatedObjects(context,sRelPattern,sTypePattern,slSelect,selectRelStmts,boolGetTo,boolGetFrom,iLevel,null,null,0); 
			} finally {
				ContextUtil.popContext(context);
			}
			taskObject.close(context);
			
			return effortMapList;
		}
		
		public String getGivenStateEffort(Context context,String strCheckState,
											boolean isIncludeCheckState,
											MapList effortMapList) throws Exception {
			
			double dblValue=0.0;
			String strValue=DomainConstants.EMPTY_STRING;
			
			if(effortMapList == null || effortMapList.isEmpty()) {
				return strValue;
			}
			
			for(int i=0;i<effortMapList.size();i++) {
				
				Map effortInfo = (Map)effortMapList.get(i);
				
				String effortId = (String)effortInfo.get(DomainConstants.SELECT_ID);
				String sState =  (String)effortInfo.get(DomainConstants.SELECT_CURRENT);
				
				double dbl=0.0;
				boolean isInclude = false; 
				if(isIncludeCheckState) {    
					if((ProgramCentralUtil.isNotNullString(strCheckState) && ProgramCentralUtil.isNotNullString(sState)&& sState.equalsIgnoreCase(strCheckState)) 
							|| ("All".equalsIgnoreCase(strCheckState)))
					{    
						isInclude = true;
					}
				} else {
					if(ProgramCentralUtil.isNotNullString(strCheckState) && ProgramCentralUtil.isNotNullString(sState)&& !sState.equalsIgnoreCase(strCheckState) )
					{    
						isInclude = true;
					}
				}
				
				if(isInclude) {
					double dbl_Sunday = Task.parseToDouble((String)effortInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_SUNDAY));
					double dbl_Monday = Task.parseToDouble((String)effortInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_MONDAY));
					double dbl_Tuesday = Task.parseToDouble((String)effortInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TUESDAY));
					double dbl_Wednesday = Task.parseToDouble((String)effortInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_WEDNESDAY));
					double dbl_Thursday = Task.parseToDouble((String)effortInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_THURSDAY));
					double dbl_Friday = Task.parseToDouble((String)effortInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_FRIDAY));
					double dbl_Saturday = Task.parseToDouble((String)effortInfo.get(ProgramCentralConstants.SELECT_ATTRIBUTE_SATURDAY));
					
					dbl = dbl_Sunday + dbl_Monday + dbl_Tuesday +  dbl_Wednesday + dbl_Thursday + dbl_Friday + dbl_Saturday;
				}
				dbl = roundWBSEffortValue(dbl);
				dblValue = dblValue + dbl;
				strValue = "" + dblValue;
			}
			return strValue;
		}
		
		public Vector getPercentAllocation(Context context, DomainObject taskObject, String sRelPattern, 
				String sTypePattern, boolean boolGetTo, boolean boolGetFrom, short iLevel) throws MatrixException {

			taskObject.open(context);
			ExpansionWithSelect effortSelect = null;
			SelectList selectObjStmts = new SelectList();
			SelectList selectRelStmts = new SelectList();
			selectObjStmts.addId();
			String rel_allocation=(String)PropertyUtil.getSchemaProperty(context,"attribute_PercentAllocation");
			selectRelStmts.addAttribute(rel_allocation);
			effortSelect = taskObject.expandSelect(context, sRelPattern, sTypePattern, selectObjStmts,selectRelStmts, boolGetTo, boolGetFrom, iLevel);
			RelationshipWithSelectItr relObjectItr = new RelationshipWithSelectItr(effortSelect.getRelationships());
			String percentAllocation = "";
			Hashtable objectRelAttributes = new Hashtable();
			Hashtable effortBusObjAttributes = new Hashtable();
			Vector vPercentAlloc = new Vector();
			
			while (relObjectItr.next()) {
				
				effortBusObjAttributes =  relObjectItr.obj().getTargetData();
				String sTaskId = (String)effortBusObjAttributes.get("id");
				objectRelAttributes =  relObjectItr.obj().getRelationshipData();
				percentAllocation =(String)objectRelAttributes.get("attribute[" + rel_allocation + "]");
				
				if (percentAllocation != null && percentAllocation.trim().length() > 0 ) {
					HashMap hm = new HashMap();
					hm.put("id",sTaskId);
					hm.put("allocation",percentAllocation);
					vPercentAlloc.addElement(hm);
				}
			}
			taskObject.close(context);
			return vPercentAlloc;
		}

}
