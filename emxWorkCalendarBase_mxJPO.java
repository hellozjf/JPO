/*   emxWorkCalendarBase
 **
 **   Copyright (c) 2003-2016 Dassault Systemes.
 **   All Rights Reserved.
 **   This program contains proprietary and trade secret information of MatrixOne,
 **   Inc.  Copyright notice is precautionary only
 **   and does not evidence any actual or intended publication of such program
 **
 **   This JPO contains the implementation of emxWorkCalendar
 **
 **   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.9.2.2 Thu Dec  4 07:55:10 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9.2.1 Thu Dec  4 01:53:19 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.9 Wed Oct 22 15:49:37 2008 przemek Experimental przemek $
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.CalendarEventRelationship;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.WorkCalendar;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.Task;

/**
 * The <code>emxWorkCalendarBase</code> class contains methods for emxWorkCalendar.
 *
 * @version PMC 10.5.1.2 - Copyright(c) 2004, MatrixOne, Inc.
 */

public class emxWorkCalendarBase_mxJPO extends emxDomainObject_mxJPO
{

	/**
	 * Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds no arguments
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */

	public emxWorkCalendarBase_mxJPO (Context context, String[] args)
	throws Exception
	{
		super(context, args);
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


	/**
	 * Return the locations/business units either associated or not
	 * associated to the calendar
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *    objectId - the context calendar object
	 * @return MapList containing objects for search result
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAvailableLocations(Context context , String[] args)
	throws Exception
	{

		StringList busSelects = new StringList( 3 );
		busSelects.add( SELECT_ID );
		busSelects.add( SELECT_TYPE );
		busSelects.add( SELECT_NAME );

		StringList relSelects = new StringList( 1 );
		relSelects.add( "id[connection]" );

		Map programMap = (Map) JPO.unpackArgs(args);
		String objectId = (String)programMap.get( "objectId" );

		this.setId( objectId );

		// Get locations already connected to the context calendar
		MapList ml = this.getRelatedObjects(context,
				PropertyUtil.getSchemaProperty( context, "relationship_Calendar" ),
				PropertyUtil.getSchemaProperty( context, "type_Location" ),
				busSelects,
				relSelects,
				true,
				false,
				(short) 1,
				null,
				null);
		ml.addSortKey(SELECT_TYPE, "ascending", "string");
		ml.addSortKey(SELECT_NAME, "ascending", "string");
		ml.sort();


		//Retrieve locations not connected to the calendar
		Company company = (Company) DomainObject.newInstance( context, PropertyUtil.getSchemaProperty( context, "type_Company" ), DomainConstants.PROGRAM );
		company.setId( getInfo( context, "to[" + PropertyUtil.getSchemaProperty( context, "relationship_CompanyCalendar" ) + "].businessobject.id" ) );

		MapList retml = new MapList();

		Iterator itr;
		Map map;
		HashSet set = new HashSet();
		for (itr = ml.iterator(); itr.hasNext(); )
		{
			map = (Map) itr.next();
			set.add(map.get(SELECT_ID));
		}

		ml = company.getLocations( context, busSelects, null );
		for (itr = ml.iterator(); itr.hasNext(); )
		{
			map = (Map) itr.next();
			if (!set.contains(map.get(SELECT_ID)))
			{
				retml.add(map);
			}
		}

		retml.addSortKey(SELECT_TYPE, "ascending", "string");
		retml.addSortKey(SELECT_NAME, "ascending", "string");
		retml.sort();
		return retml;
	}

	/**
	 * Return the Frequency of the Event
	 * associated to the calendar
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *    objectId - the context calendar object
	 * @return Vector Containing Frequency as string
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getFrequency(Context context, String args[])
	throws Exception
	{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String objectId = (String) paramMap.get("objectId");

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		String attribute_Frequency=(String)PropertyUtil.getSchemaProperty(context,"attribute_Frequency");

		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		String sRetValue = "";

		Map paramList = (Map)programMap.get("paramList");
		String strLanguage = (String)paramList.get("languageStr");
		i18nNow i18nnow = new i18nNow();

		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			try{
				id =(String)((HashMap)relBusObjPageList.get(i)).get("id[connection]");
			}catch (Exception ex) {
				id =(String)((Hashtable)relBusObjPageList.get(i)).get("id[connection]");
			}
			if (id != null && id.trim().length() > 0 ){
				DomainRelationship bus  = new DomainRelationship(id);
				try{
					bus.open(context);
				}catch (FrameworkException Ex) {
					throw Ex;
				}
				String sFreq = bus.getAttributeValue(context,attribute_Frequency);
				if(sFreq != null && sFreq.equalsIgnoreCase("0")){
					String NonRecurrence = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.Calendar.Frequency_NonRecurrence", strLanguage);
					columnValues.add(NonRecurrence);
				}
				else if(sFreq != null && sFreq.equalsIgnoreCase("1")){

					String Weekly = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.Calendar.Frequency_Weekly", strLanguage);
					columnValues.add(Weekly);
				}
				else if(sFreq != null && sFreq.equalsIgnoreCase("2")){
					String Monthly = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.Calendar.Frequency_Monthly", strLanguage);
					columnValues.add(Monthly);
				}
				else if(sFreq != null && sFreq.equalsIgnoreCase("3")){
					String Yearly = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
							"emxProgramCentral.Calendar.Frequency_Yearly", strLanguage);
					columnValues.add(Yearly);
				}
			}
		}
		return columnValues;
	}

	/**
	 * Return the Day of the Week
	 * according to attribute Frequency value
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *    objectId - the context calendar object
	 * @return Vector Containing Day as string
	 * @throws Exception if the operation fails
	 * @since PMC 10.5.1.2
	 */
	public Vector getDayNumber(Context context, String args[])throws Exception
	{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		String objectId = (String) paramMap.get("objectId");
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList relBusObjPageList =(MapList)programMap.get("objectList");
		String attribute_DayNumber=(String)PropertyUtil.getSchemaProperty(context,"attribute_DayNumber");
		String attribute_Frequency=(String)PropertyUtil.getSchemaProperty(context,"attribute_Frequency");
		Vector columnValues = new Vector(relBusObjPageList.size());
		String id = "";
		String sRetValue = "";

		for (int i = 0; i < relBusObjPageList.size(); i++)
		{
			try
			{
				id =(String)((HashMap)relBusObjPageList.get(i)).get("id[connection]");
			}
			catch (Exception ex) 
			{
				id =(String)((Hashtable)relBusObjPageList.get(i)).get("id[connection]");
			}

			if (id != null && id.trim().length() > 0 )
			{
				DomainRelationship bus  = new DomainRelationship(id);
				try{
					bus.open(context);
				}catch (FrameworkException Ex) {
					throw Ex;
				}
				String d_number = bus.getAttributeValue(context,attribute_DayNumber);
				String d_freq = bus.getAttributeValue(context,attribute_Frequency);

				//Modified:15-Feb-2011:hp5:R211:PRG:IR-093751V6R2012
				String sLanguage = context.getSession().getLanguage();
				String dayNoKey = "emxProgramCentral.Calendar.";
				String convertedDay = "";

				if(d_freq != null && d_freq.equals("1"))
				{
					if(d_number != null && d_number.equalsIgnoreCase("1"))
					{
						convertedDay = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								dayNoKey+ProgramCentralConstants.ATTRIBUTE_SUNDAY, sLanguage);
						columnValues.add(convertedDay);
					}
					else if(d_number != null && d_number.equalsIgnoreCase("2"))
					{
						convertedDay = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								dayNoKey+ProgramCentralConstants.ATTRIBUTE_MONDAY, sLanguage);
						columnValues.add(convertedDay);
					}
					else if(d_number != null && d_number.equalsIgnoreCase("3"))
					{
						convertedDay = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								dayNoKey+ProgramCentralConstants.ATTRIBUTE_TUESDAY, sLanguage);
						columnValues.add(convertedDay);
					}
					else if(d_number != null && d_number.equalsIgnoreCase("4"))
					{
						convertedDay = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								dayNoKey+ProgramCentralConstants.ATTRIBUTE_WEDNESDAY, sLanguage);
						columnValues.add(convertedDay);
					}
					else if(d_number != null && d_number.equalsIgnoreCase("5"))
					{
						convertedDay = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								dayNoKey+ProgramCentralConstants.ATTRIBUTE_THURSDAY, sLanguage);
						columnValues.add(convertedDay);
					}
					else if(d_number != null && d_number.equalsIgnoreCase("6"))
					{
						convertedDay = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								dayNoKey+ProgramCentralConstants.ATTRIBUTE_FRIDAY, sLanguage);
						columnValues.add(convertedDay);
					}
					else if(d_number != null && d_number.equalsIgnoreCase("7"))
					{
						convertedDay = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
								dayNoKey+ProgramCentralConstants.ATTRIBUTE_SATURDAY, sLanguage);
						columnValues.add(convertedDay);
					}
				}else{
					columnValues.add(d_number);
				}
			}
		}
		return columnValues;
	}

	/**
	 * Return the calendars associated to a company
	 *
	 * @param Context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *    objectId - the context company object
	 * @return MapList containing the id of calendar objects
	 * @throws Exception if operation fails
	 * @since PMC 10.6
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static MapList getCalendar(Context context,String[] args) throws Exception
	{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		MapList objectList = new MapList();

		String objectId = (String)paramMap.get("objectId");

		StringList busSelects = new StringList(1);
		busSelects.add(DomainConstants.SELECT_ID);

		com.matrixone.apps.common.Company company =
			(com.matrixone.apps.common.Company) DomainObject.newInstance( context, objectId);
		objectList = WorkCalendar.getCalendars(context, company, busSelects);

		return objectList;
	}

	/**
	 * Return the events associated to a calendar
	 *
	 * @param Context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *    objectId - the context calendar object
	 * @return MapList containing the id of event objects
	 * @throws Exception if operation fails
	 * @since PMC 10.6
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public static MapList getEvents(Context context,String[] args) throws Exception
	{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		MapList objectList = new MapList();
		String objectId = (String)paramMap.get("objectId");

		String RELATIONSHIP_CALENDAR_EVENT = PropertyUtil.getSchemaProperty(context,"relationship_CalendarEvent");

		StringList busSelects = new StringList(1);
		busSelects.add(DomainConstants.SELECT_ID);

		StringList relSelects = new StringList(1);
		relSelects.add(DomainRelationship.SELECT_ID);

		com.matrixone.apps.common.WorkCalendar workcalendar =
			(com.matrixone.apps.common.WorkCalendar) DomainObject.newInstance( context, objectId);
		objectList = workcalendar.getRelatedObjects(context,RELATIONSHIP_CALENDAR_EVENT, "*", busSelects, relSelects, false, true, (short)1, "", "", null, null, null);

		return objectList;
	}
	
	public Map getFrequencyRange(Context context, String[] args) throws Exception {
		Map<String,StringList> mapReturnFrequencyRange = new HashMap<String,StringList>();
		try {
			String sLanguage = context.getSession().getLanguage();
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String isNonRecurrence = (String)requestMap.get("isNonRecurrence");		

			String strFreqNonRec  = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Calendar.Frequency_NonRecurrence", sLanguage);
			String strFreqWeekly  = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Calendar.Frequency_Weekly", sLanguage);

			StringList slFreqs  = new StringList();
			slFreqs.add(strFreqNonRec);
			slFreqs.add(strFreqWeekly);

			if("false".equals(isNonRecurrence)) 
			{
				slFreqs.remove(strFreqWeekly);
				slFreqs.add(0,strFreqWeekly);
			}

			mapReturnFrequencyRange.put("field_choices", slFreqs);
			mapReturnFrequencyRange.put("field_display_choices", slFreqs);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}		        
		return  mapReturnFrequencyRange;
	}
	
	public Map getDayNumberRange(Context context, String[] args) throws Exception {
		Map<String,StringList> mapReturnFrequencyRange = new HashMap<String,StringList>();
		try {
			String sLanguage = context.getSession().getLanguage();

			String strSunday  = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Calendar.Sunday", sLanguage);
			String strMonday  = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Calendar.Monday", sLanguage);
			String strTuesday  = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Calendar.Tuesday", sLanguage);
			String strWednesday  = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Calendar.Wednesday", sLanguage);
			String strThursday  = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Calendar.Thursday", sLanguage);
			String strFriday  = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Calendar.Friday", sLanguage);
			String strSaturday  = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Calendar.Saturday", sLanguage);

			StringList slWeekDay  = new StringList();
			String [] sWeekDays_Actual = {"1", "2", "3", "4", "5", "6", "7"};
			StringList slWeek_Actual = new StringList();
			slWeek_Actual.addAll(sWeekDays_Actual);

			String [] sWeekDays = {strSunday, strMonday, strTuesday, strWednesday, strThursday, strFriday, strSaturday};
			slWeekDay.addAll(sWeekDays);

			mapReturnFrequencyRange.put("field_choices", slWeek_Actual);
			mapReturnFrequencyRange.put("field_display_choices", slWeekDay);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw e;
		}		        
		return  mapReturnFrequencyRange;
	}
	
	public boolean isEventFrequnceTypeNonRecurrence(Context context, String args[]) throws MatrixException {
		boolean blAccess = false;
		try{
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			String isContinuous = (String)inputMap.get("isNonRecurrence");

			if(null!=isContinuous && "true".equals(isContinuous))
			{
				blAccess = true;
			}
		}catch(Exception e) {
			throw new MatrixException(e);		 
		}	 
		return blAccess;
	}
	
	public boolean isEventFrequnceTypeWeekly(Context context, String args[]) throws MatrixException {
		boolean blAccess = false;
		try{
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			String isContinuous = (String)inputMap.get("isNonRecurrence");

			if(null!=isContinuous && "false".equals(isContinuous))
			{
				blAccess = true;
			}
		}catch(Exception e) {
			throw new MatrixException(e);		 
		}	 
		return blAccess;
	}
	
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public void createCalendareEventProcess(Context context, String[] args) throws MatrixException {
		try{
			HashMap map = (HashMap) JPO.unpackArgs(args);
			Map paramMap = (Map)map.get("paramMap");
			Map requestMap=(Map)map.get("requestMap");			

			//Get parameters from reuestMap
			String strObjectId = ProgramCentralConstants.EMPTY_STRING; 		
			strObjectId = (String) requestMap.get("objectId"); 

			String startDate = ProgramCentralConstants.EMPTY_STRING;
			String endDate = ProgramCentralConstants.EMPTY_STRING;
			String frequencyValue = (String) requestMap.get("Frequency");
			String note = (String) requestMap.get("Note");
			String title = (String) requestMap.get("Title");

			String frequency = "0";
			String dayNumber = (String) requestMap.get("DayNumber");

			String strTimeZone = (String)requestMap.get("timeZone");
			double clientTimeZone = Double.parseDouble(strTimeZone);

			Locale locale = (Locale)requestMap.get("locale");
			if(null==locale||"".equals(locale)||"Null".equals(locale))
			{
				locale = (Locale)requestMap.get("localeObj");
			}            

			if("Non-Recurrence".equals(frequencyValue))
			{
				frequency = "0";
				startDate = (String) requestMap.get("StartDate");
				endDate = (String) requestMap.get("StartDate");
				startDate = eMatrixDateFormat.getFormattedInputDate(context, startDate, clientTimeZone,locale);
				endDate = eMatrixDateFormat.getFormattedInputDate(context, endDate, clientTimeZone,locale);
			}
			else
				frequency = "1";

			String TYPE_CALENDAR = PropertyUtil.getSchemaProperty("type_WorkCalendar");
			com.matrixone.apps.common.WorkCalendar calendar = (com.matrixone.apps.common.WorkCalendar) DomainObject.newInstance(context,
					TYPE_CALENDAR);
			calendar.setId(strObjectId);			

			ContextUtil.startTransaction(context,true);

			HashMap attrMap = new HashMap();
			attrMap.put(CalendarEventRelationship.ATTRIBUTE_START_DATE, startDate);
			attrMap.put(CalendarEventRelationship.ATTRIBUTE_END_DATE, endDate);
			attrMap.put(CalendarEventRelationship.ATTRIBUTE_DAY_NUMBER, dayNumber);
			attrMap.put(CalendarEventRelationship.ATTRIBUTE_FREQUENCY, frequency);
			attrMap.put(CalendarEventRelationship.ATTRIBUTE_NOTE, note);
			attrMap.put(CalendarEventRelationship.ATTRIBUTE_TITLE, title);
			calendar.createEvent(context, attrMap);

			ContextUtil.commitTransaction(context);

		} catch (Exception e) 
		{
			throw new MatrixException();
		}
	}


	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getExceptions(Context context, String[] args) throws Exception{
		MapList exceptions = new MapList();
		Map programMap = (HashMap)JPO.unpackArgs(args);
		String calendarId = (String)programMap.get("objectId");
		MapList objectList = new MapList();
		String RELATIONSHIP_CALENDAR_EVENT = PropertyUtil.getSchemaProperty(context,"relationship_CalendarEvent");
		StringList busSelects = new StringList(1);
		busSelects.add(DomainConstants.SELECT_ID);
		StringList relSelects = new StringList(1);
		relSelects.add(DomainRelationship.SELECT_ID);
		String sRelWhere = "attribute[Event Type] == \"" + "Exception" +"\"";		

		WorkCalendar wc = (WorkCalendar) DomainObject.newInstance( context, calendarId);
		objectList = wc.getRelatedObjects(context, 
				RELATIONSHIP_CALENDAR_EVENT, 
				ProgramCentralConstants.TYPE_EVENT, 
				busSelects, 
				relSelects, 
				false,  
				true, 
				(short)0, 
				"", 
				sRelWhere, 
				0, 
				null, 
				null, 
				null);
		return objectList;	
	}

	public String getCalendarLocations(Context context , String[] args) throws FrameworkException{
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			String strMode = (String) requestMap.get("mode");
			String objectId = (String)requestMap.get( "objectId" );

			String strLocationName = DomainConstants.EMPTY_STRING;
			String strLocationId = DomainConstants.EMPTY_STRING;
			StringBuffer sbLocationName = new StringBuffer();
			StringBuffer sbLocationId = new StringBuffer();
			StringBuffer sbOutput = new StringBuffer();
			String languageStr = (String) requestMap.get("languageStr");
			String strOuput =DomainConstants.EMPTY_STRING;

			DomainObject calendar = DomainObject.newInstance(context, objectId );
			StringList busSelects = new StringList(3);
			busSelects.add( SELECT_ID );
			busSelects.add( SELECT_TYPE );
			busSelects.add( SELECT_NAME );

			StringList relSelects = new StringList( 1 );
			relSelects.add( "id[connection]" );
			//Get connected Programs list
			MapList mlLocations = calendar.getRelatedObjects(
					context,
					PropertyUtil.getSchemaProperty( context, "relationship_Calendar" ),
					PropertyUtil.getSchemaProperty( context, "type_Location" ),
					busSelects,
					relSelects,
					true,
					false,
					(short)1,
					null,
					DomainConstants.EMPTY_STRING,
					0);
			// Get locations already connected to the context calendar
			mlLocations.addSortKey(SELECT_TYPE, "ascending", "string");
			mlLocations.addSortKey(SELECT_NAME, "ascending", "string");
			mlLocations.sort();
			if(mlLocations != null && mlLocations.size() > 0){
				for(int i = 0; i < mlLocations.size(); i++){
					Map locationInfo = (Map) mlLocations.get(i);
					String locationName = (String) locationInfo.get(SELECT_NAME);
					String locationId =(String) locationInfo.get(SELECT_ID);
					sbLocationName.append(locationName + ",");
					sbLocationId.append(locationId + ",");
				}
			}
			if(sbLocationName.length()>0){
				strLocationName = sbLocationName.toString();
				strLocationName = strLocationName.substring(0, strLocationName.length()-1);
				strLocationId = sbLocationId.toString();
				strLocationId = strLocationId.substring(0,strLocationId.length()-1);
			}
		    if(strMode.equalsIgnoreCase("edit"))
            {
            	String clearLabel = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
            			"emxProgramCentral.Common.Clear",languageStr);
            		//Added for special character.
            		sbOutput.append("<input type='text' name='LocationName' value='"+XSSUtil.encodeForHTML(context,strLocationName)+"' readonly=\"readonly\"/>");
            		sbOutput.append("<input type='hidden' name='LocationId' value='" + strLocationId + "'/>");
            		sbOutput.append("<input type='button' name='location' value='...' onClick=\"");
            		sbOutput.append("javascript:showChooser('");
            		String strURLl= "../common/emxFullSearch.jsp?field=TYPES=type_Location&selection=multiple&excludeOIDprogram=emxWorkCalendar:getExcludedLocations&table=AEFGeneralSearchResults&fieldNameActual=LocationId&fieldNameDisplay=LocationName&submitURL=../programcentral/emxProgramCentralAutonomySearchUtil.jsp&suiteKey=ProgramCentral&chooserType=CustomChooser&calendarId="+objectId+"&amp;cancelLabel=emxProgramCentral.Common.Cancel";
            		sbOutput.append(strURLl);
            		sbOutput.append("','600','600')\"/>");{
            		sbOutput.append("<a href=\"javascript:removeLocation();\"'>");  		
            		sbOutput.append(clearLabel);
            		sbOutput.append("</a>");
            	strOuput =sbOutput.toString();
            		}
            }
            else{
                StringList slProgramNameList = FrameworkUtil.split(strLocationName, ",");
                StringList slProgramIdList = FrameworkUtil.split(strLocationId, ",");
                String strLocId = EMPTY_STRING;
                String strLocName= EMPTY_STRING;
                if(!slProgramIdList.isEmpty()){
                    for(int nCount=0;nCount<slProgramIdList.size();nCount++){
                    	strLocId = (String)slProgramIdList.get(nCount);
					strLocId = XSSUtil.encodeForURL(context, strLocId);
                    	strLocName=(String)slProgramNameList.get(nCount);
					strLocName  =   XSSUtil.encodeForHTML(context, strLocName);
					sbOutput.append("<a href='../common/emxTree.jsp?objectId=").append(strLocId);
                        sbOutput.append("&amp;relId=null&amp;suiteKey=ProgramCentral'>");
                        sbOutput.append("<img src='../common/images/iconSmallLocation.gif' border='0' valign='absmiddle'/>");
					sbOutput.append(strLocName);
					sbOutput.append("</a>");
					sbOutput.append("<br/>");
				}
				strOuput = sbOutput.toString().substring(0, sbOutput.toString().length()-1);
			}
			}
			return strOuput ;
		} catch (Exception e) {
			throw new FrameworkException(e);
		}
	}

	/**
	 * Return a collection of HTML formatted string containing workweek details of all calendars. 
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args JPO methods arguments.
	 * @return a StringList of workweek details.
	 * @throws Exception if operation fails.
	 */
	public StringList getCalendarWorkweek (Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		StringList slWorkweek = new StringList();
		Map paramList    = (Map) programMap.get("paramList");
		boolean isPrinterFriendly = false;
		String strPrinterFriendly = (String)paramList.get("reportFormat");
		if (UIUtil.isNotNullAndNotEmpty(strPrinterFriendly)){ isPrinterFriendly = true; }
		else{ strPrinterFriendly = ""; }
		
		WorkCalendar workCalendar = new WorkCalendar();
		StringList daysGlobalized = workCalendar.getShortDaysOfWeek(context, context.getLocale().getLanguage());
       	String exceptionType = DomainConstants.EMPTY_STRING;
       	String dayName = DomainConstants.EMPTY_STRING;
       	int dayOfWeekIndex;

		for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
			Map calendarInfo = (Map) iterator.next();
			String calendarId = (String) calendarInfo.get(DomainObject.SELECT_ID);
			workCalendar = new WorkCalendar(calendarId);
			MapList mlWorkweek = workCalendar.getWorkWeek(context);
			mlWorkweek.sort("attribute[Title].value", "ascending", "integer");
			StringBuffer sbWorkweek = new StringBuffer();
			for (Iterator itrWorkweek = mlWorkweek.iterator(); itrWorkweek.hasNext();) {
				Map weekDayInfo = (Map) itrWorkweek.next();
               	exceptionType = (String) weekDayInfo.get("attribute[Calendar Exception Type]");
               	dayOfWeekIndex = Integer.parseInt((String)weekDayInfo.get("attribute[Title].value"));
               	dayOfWeekIndex--;
               	dayName =  (String)daysGlobalized.get(dayOfWeekIndex);
               	if("Working".equals(exceptionType)){
                   	sbWorkweek.append("<span style='background:#1684C2;color:white;padding-left:5px;padding-right:5px;padding-top:2px;padding-bottom:2px;'>");
               	}else{
                   	sbWorkweek.append("<span style='background:#535C65;color:white;padding-left:5px;padding-right:5px;padding-top:2px;padding-bottom:2px;'>");
               	}
               	sbWorkweek.append(dayName);
               	sbWorkweek.append("</span>");
			}
			slWorkweek.add(sbWorkweek.toString());
		}
		return slWorkweek;
	}
	
	/**
	 * Introduced for MPI 
	 * Return a xml format of workweek details
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args JPO methods arguments (list of calendar Object IDS).
	 * @return a StringList of workweek details.
	 * @throws Exception if operation fails.
	 */
	public StringList getCalendarWorkweekInXML (Context context, String[] args) throws Exception{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");
		StringList slWorkweek = new StringList();
		Map paramList    = (Map) programMap.get("paramList");

		WorkCalendar workCalendar = new WorkCalendar();
		StringList daysGlobalized = workCalendar.getShortDaysOfWeek(context, context.getLocale().getLanguage());
		String exceptionType = DomainConstants.EMPTY_STRING;
		int dayOfWeekIndex;

		for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
			Map calendarInfo = (Map) iterator.next();
			String calendarId = (String) calendarInfo.get(DomainObject.SELECT_ID);
			workCalendar = new WorkCalendar(calendarId);

			MapList mlWorkweek = workCalendar.getWorkWeek(context);
			mlWorkweek.sort(WorkCalendar.SELECT_TITLE, "ascending", "integer");
			StringBuffer sbWorkweek = new StringBuffer();
			for (Iterator itrWorkweek = mlWorkweek.iterator(); itrWorkweek.hasNext();) {
				Map weekDayInfo = (Map) itrWorkweek.next();
				System.out.println("AMA3 week - "+weekDayInfo.toString());
				dayOfWeekIndex = Integer.parseInt((String)weekDayInfo.get(WorkCalendar.SELECT_TITLE));

				sbWorkweek.append("<WeekDay");
				sbWorkweek.append(" title=\""+dayOfWeekIndex+"\"");
				sbWorkweek.append(" displayTitle=\""+(String)daysGlobalized.get(dayOfWeekIndex-1)+"\"");
				sbWorkweek.append(" exceptionType=\""+(String)weekDayInfo.get("attribute[Calendar Exception Type]")+"\"");
				sbWorkweek.append(" oid=\""+(String)weekDayInfo.get("id") + "\" >");				

				sbWorkweek.append("<Work_StartTime>" + (String)weekDayInfo.get("attribute[Work Start Time]") +"</Work_StartTime>");
				sbWorkweek.append("<Work_FinishTime>" + (String)weekDayInfo.get("attribute[Work Finish Time]") +"</Work_FinishTime>");
				sbWorkweek.append("<Lunch_StartTime>" + (String)weekDayInfo.get("attribute[Lunch Start Time]") +"</Lunch_StartTime>");					
				sbWorkweek.append("<Lunch_FinishTime>" + (String)weekDayInfo.get("attribute[Lunch Finish Time]") +"</Lunch_FinishTime>");	
				sbWorkweek.append("</WeekDay>");
			}
			slWorkweek.add(sbWorkweek.toString());
		}
		return slWorkweek;
	}

	/**
	 * Introduced for MPI 
	 * Return a xml format of Exception details
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args JPO methods arguments  number of objects at index 1 and then (list of calendar Object IDS).
	 * @return a StringList of workweek details.
	 * @throws Exception if operation fails.
	 */

	public String getCalendarExceptionInXML (Context context, String[] args) throws Exception{

		StringList slException = new StringList();		
		String exceptionType = DomainConstants.EMPTY_STRING;
		WorkCalendar workCalendar;
		slException.add("<CalendarList>");

		int calendarIDCount=Integer.parseInt(args[1]);
		int offset=2;  //Since the calendarIDs are present from 3rd index 

		for(int index=0;index < calendarIDCount;index++) {
			String calendarId = args[index+offset];
			workCalendar = new WorkCalendar(calendarId);
			MapList mlException = workCalendar.getExceptions(context);			
			StringBuffer sbException = new StringBuffer();

			sbException.append("<Calendar oid=\""+calendarId+"\">");		
			sbException.append("<ExceptionList>");	
			for (Iterator itrException = mlException.iterator(); itrException.hasNext();) {
				Map exceptionInfo = (Map) itrException.next();
				System.out.println("AMA3 Exception Info - "+exceptionInfo.toString());

				sbException.append("<Exception");
				sbException.append(" title=\""+(String)exceptionInfo.get(WorkCalendar.SELECT_TITLE)+"\"");
				sbException.append(" exceptionType=\""+(String)exceptionInfo.get("attribute[Calendar Exception Type]")+"\"");

				String frequency = (String)exceptionInfo.get(WorkCalendar.SELECT_FREQUENCY);
				frequency= frequency!=null ? frequency: DomainConstants.EMPTY_STRING ;

				sbException.append(" frequency=\""+frequency+"\"");
				sbException.append(" oid=\""+(String)exceptionInfo.get("id") + "\" >");				

				String startDate = (String)exceptionInfo.get(WorkCalendar.SELECT_START_DATE) ;
				startDate= startDate!=null ? startDate: DomainConstants.EMPTY_STRING ;
				sbException.append("<Start_Date>" + startDate +"</Start_Date>");	

				String endDate = (String)exceptionInfo.get(WorkCalendar.SELECT_END_DATE) ;
				endDate= endDate!=null ? endDate: DomainConstants.EMPTY_STRING ;
				sbException.append("<End_Date>" + endDate +"</End_Date>");	

				String dayOfWeek = (String)exceptionInfo.get("attribute[Day Of Week]") ;
				dayOfWeek= dayOfWeek!=null ? dayOfWeek: DomainConstants.EMPTY_STRING ;
				sbException.append("<Day_Of_Week>" + dayOfWeek +"</Day_Of_Week>");	

				String daysOfWeek = (String)exceptionInfo.get("attribute[Days Of Week]") ;
				daysOfWeek= daysOfWeek!=null ? daysOfWeek: DomainConstants.EMPTY_STRING ;
				sbException.append("<Days_Of_Week>" + daysOfWeek +"</Days_Of_Week>");	

				String dayOfMonth = (String)exceptionInfo.get("attribute[Day Of Month]") ;
				dayOfMonth= dayOfMonth!=null ? dayOfMonth: DomainConstants.EMPTY_STRING ;
				sbException.append("<Day_Of_Month>" + dayOfMonth +"</Day_Of_Month>");

				String weekOfMonth=(String)exceptionInfo.get("attribute[Week Of Month]") ;
				weekOfMonth= weekOfMonth!=null ? weekOfMonth: DomainConstants.EMPTY_STRING ;
				sbException.append("<Week_Of_Month>" + weekOfMonth +"</Week_Of_Month>");

				String monthOfYear=(String)exceptionInfo.get("attribute[Month Of Year]") ;
				monthOfYear= monthOfYear!=null ? monthOfYear: DomainConstants.EMPTY_STRING ;
				sbException.append("<Month_Of_Year>" + monthOfYear +"</Month_Of_Year>");

				String workStartTime=(String)exceptionInfo.get("attribute[Work Start Time]") ;
				workStartTime= workStartTime!=null ? workStartTime: DomainConstants.EMPTY_STRING ;
				sbException.append("<Work_StartTime>" + workStartTime +"</Work_StartTime>");

				String workFinishTime=(String)exceptionInfo.get("attribute[Work Finish Time]") ;
				workFinishTime= workFinishTime!=null ? workFinishTime: DomainConstants.EMPTY_STRING ;
				sbException.append("<Work_FinishTime>" + workFinishTime +"</Work_FinishTime>");

				String lunchStartTime=(String)exceptionInfo.get("attribute[Lunch Start Time]") ;
				lunchStartTime= lunchStartTime!=null ? lunchStartTime: DomainConstants.EMPTY_STRING ;
				sbException.append("<Lunch_StartTime>" + lunchStartTime +"</Lunch_StartTime>");
								
				String lunchFinishTime=(String)exceptionInfo.get("attribute[Lunch Finish Time]") ;
				lunchFinishTime= lunchFinishTime!=null ? lunchFinishTime: DomainConstants.EMPTY_STRING ;
				sbException.append("<Lunch_FinishTime>" + lunchFinishTime +"</Lunch_FinishTime>");

				sbException.append("</Exception>");
			}
			sbException.append("</ExceptionList></Calendar>");
			slException.add(sbException.toString());

		}

		slException.add("</CalendarList>");	

		String returnValue = "";
        for(String s : slException.toList())
          returnValue+=s;

        return returnValue;
	}


	/**
	 * Gets Calendar's working hours per day.
	 * @param context the ENOVIA <code>Context</code> object.
	 * @param args JPO methods arguments.
	 * @return Standard working hour in string format.
	 * @throws FrameworkException if operation fails.
	 */
	public String getCalendarStandardWork(Context context, String[] args) throws FrameworkException{
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			Map requestMap = (Map) programMap.get("requestMap");
			String calendarId = (String)requestMap.get( "objectId" );
			WorkCalendar calendar = new WorkCalendar(calendarId);
			String sWork = calendar.getInfo(context, "attribute[Working Time Per Day]");
			double dWork = Task.parseToDouble(sWork);
			dWork = (dWork / 60);
			return (dWork + DomainConstants.EMPTY_STRING);
		}catch(Exception e){
			throw new FrameworkException(e);
		}
	}
	
	 @com.matrixone.apps.framework.ui.PostProcessCallable
	    public void postEditCalendarDetailsProcess(Context context, String[] args) throws Exception {
		 try{
			 String languageStr = context.getSession().getLanguage();
			 Map programMap = (HashMap) JPO.unpackArgs(args);
			 Map paramMap = (HashMap) programMap.get("paramMap");
			 Map requestMap = (HashMap) programMap.get("requestMap");
			 String calendarId = (String) paramMap.get("objectId");
			 String locationId = (String)requestMap.get("LocationId");
			 String locationName = (String)requestMap.get("LocationName");

			 WorkCalendar workCalendar = new WorkCalendar(calendarId);

			 StringList slNewLocations = new StringList();
			 if(UIUtil.isNotNullAndNotEmpty(locationId)){
				 slNewLocations = FrameworkUtil.split(locationId, ",");
			 }

			 for (Iterator itrNewLocations = slNewLocations.iterator(); itrNewLocations.hasNext();) {
				 String newLocationId = (String) itrNewLocations.next();
				 workCalendar.setCalendar(context, newLocationId);
			 }
			 if(UIUtil.isNullOrEmpty(locationId)) {

				 StringList busSelects = new StringList(3);
				 busSelects.add( SELECT_ID );
				 StringList relSelects = new StringList( );
				 //Get connected Locations list
				 MapList mlLocations = workCalendar.getRelatedObjects(
						 context,
						 PropertyUtil.getSchemaProperty( context, "relationship_Calendar" ),
						 PropertyUtil.getSchemaProperty( context, "type_Location" ),
						 busSelects,
						 relSelects,
						 true,
						 false,
						 (short)1,
						 null,
						 DomainConstants.EMPTY_STRING,
						 0);

				 if(mlLocations != null && mlLocations.size() > 0) {
					 for(int i = 0; i < mlLocations.size(); i++) {
						 Map locationInfo = (Map) mlLocations.get(i);
						 String strLocationId =(String) locationInfo.get(SELECT_ID);
						 workCalendar.removeLocation(context, strLocationId);
					 }
				 }
			 }   
		 } catch(Exception ex) {
			 throw new  FrameworkException(ex);
		 }
	    }
	
		@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable 
		public StringList getExcludedLocations(Context context,String[]args)throws Exception {
			StringList slFinalList = new StringList();
			
			Map programMap = (HashMap) JPO.unpackArgs(args);
			String calendarId = (String)programMap.get("calendarId");
			WorkCalendar workCalendar = new WorkCalendar(calendarId);
			 
				StringList busSelects = new StringList(3);
				busSelects.add( SELECT_ID );
				StringList relSelects = new StringList( );
				//Get connected Location list
				MapList existingLocations = workCalendar.getRelatedObjects(
						context,
						PropertyUtil.getSchemaProperty( context, "relationship_Calendar" ),
						PropertyUtil.getSchemaProperty( context, "type_Location" ),
						busSelects,
						relSelects,
						true,
						false,
						(short)1,
						null,
						DomainConstants.EMPTY_STRING,
						0);
				
				Iterator existingLocationIterator = existingLocations.iterator();
				while(existingLocationIterator.hasNext()) {
					Map existingLocationMap = (Map)existingLocationIterator.next();
					slFinalList.add((String)existingLocationMap.get(SELECT_ID));
				}			
			return slFinalList;
		}
}
