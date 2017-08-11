
/* emxGanttBase.java

   Copyright (c) 1992-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxGanttBase.java.rca 1.6 Wed Oct 22 16:21:23 2008 przemek Experimental przemek $
*/

import java.awt.Color;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.Task;
import com.matrixone.apps.common.WorkCalendar;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;

/**
 * The <code>emxTask</code> class represents the Gantt JPO functionality.
 */
public class emxGanttBase_mxJPO extends emxTaskBase_mxJPO {
   
	/**
     *Parameterized constructor.
     * 
	 * @param	context			
	 * 			Context object which is used while fetching data related application.
     * @param 	argumentArray
     * 			Array of parameters which are needed by the JPO.
     * 
	 * @throws 	Exception		
	 * 			Exception can be thrown in case of constructor fail to execute.
     */
    public emxGanttBase_mxJPO (Context context, String[] argumentArray) throws Exception {
        super(context,argumentArray);
        if (argumentArray != null && argumentArray.length > 0){
            setId(argumentArray[0]);
        }
    }
    
    public void saveCustomData(Context context, String[] argumentArray) throws Exception {
    	
    	MapList customColumnMapList = JPO.unpackArgs(argumentArray);
    	 
    	for(int i=0; i<customColumnMapList.size(); i++) {
    		Map customColumnMap = (Map)customColumnMapList.get(i);
    		Iterator<String> iterator = customColumnMap.keySet().iterator();
    		while(iterator.hasNext()) {
    			String key = (String)iterator.next();
    			String value = (String)customColumnMap.get(key);
    			System.out.println("key... " + key);
    			System.out.println("value... " + value);
    		}
    		System.out.println("***************************************************************");
    	}
    }
    /**
     * This method returns the stringlist of bus selectable depends on which view is
     * rendering in Gantt chart.
     * 
	 * @param	context			
	 * 			Context object which is used while fetching data related application.
     * @param 	argumentArray
     * 			this contains map which contains instance of view which is going 
     * 			to be rendered in Gantt chart.
     * 
     * @return	relSelectableList
     * 			StringList of custom selectable which is added by customer to be
     * 			rendered in Gantt chart..
     * 
	 * @throws 	Exception		
	 * 			Exception can be thrown in case of method fail to execute.
	 * @Deprecated : please use getCustomColumnSettingsMapList() to add selectable
	 * 				 along with other configuration. 
     */
     
	public StringList getBusSelectableList(Context context,String[] argumentArray) throws Exception {
    	
		String ganttViewName = (String)JPO.unpackArgs(argumentArray);
    	StringList busSelectableList = new StringList();
	    return busSelectableList;
    }
	/**
     * This method returns the stringlist of rel selectable.
     * 
	 * @param	context			
	 * 			Context object which is used while fetching data related application.
     * @param 	argumentArray
     * 			It contains name of Gantt chart view which is going to be rendered.
     * 
     * @return	relSelectableList
     * 			StringList of selectables which are needed by view which will be
     * 			rendered in Gantt chart..
     * 
	 * @throws 	Exception		
	 * 			Exception can be thrown in case of method fail to execute.
     */
    public StringList getRelSelectableList(Context context,String[] argumentArray) throws Exception {
    	
    	String ganttViewName = JPO.unpackArgs(argumentArray);
    	StringList relSelectableList   = new StringList();
        
    	return relSelectableList;
    }
    /**
     * This method compute deviation for given task and returns it.
     * 
	 * @param	context			
	 * 			Context object which is used while fetching data related application.
     * @param 	argumentArray
     * 			Array of parameters which are needed calculate the deviation for a task.
     * @return	deviationDays
     * 			Deviation for the task, which values are passed to it.
     * 
	 * @throws 	Exception		
	 * 			Exception can be thrown in case of method fail to execute.
     */
    public Map getTaskDeviationValue(Context context,String[] argumentArray) throws Exception {
    	
    	Map<String,String> taskDeviationMap = new HashMap<String,String>(); 
    	String languageString 	= context.getSession().getLanguage();
    	String durationUnit	  	= ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.DurationUnits.Days",languageString);
    	durationUnit		  	= ProgramCentralConstants.SPACE + durationUnit;
    	
    	MapList taskInfoMapList	= (MapList)JPO.unpackArgs(argumentArray);
    	
    	for(int i=0;i<taskInfoMapList.size();i++ ) {
    		
    		WorkCalendar taskCalendar = null;
    		Map taskInfoMap 		= (Map)taskInfoMapList.get(i);
	    	String taskId			= (String)taskInfoMap.get(DomainConstants.SELECT_ID);
	    	String taskCalendarId	= (String)taskInfoMap.get(ProgramCentralConstants.SELECT_CALENDAR_ID);
	    	String taskCalendarDate	= (String)taskInfoMap.get(ProgramCentralConstants.SELECT_CALENDAR_DATE);
	    	
	    	float percentComplete	= (float) Task.parseToDouble((String)taskInfoMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_PERCENT_COMPLETE));
	    	Date estimatedStartDate	= eMatrixDateFormat.getJavaDate((String)taskInfoMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE));
	    	float estimatedDuration	= (float) Task.parseToDouble((String)taskInfoMap.get(ProgramCentralConstants.SELECT_ATTRIBUTE_TASK_ESTIMATED_DURATION));
	    	
	    	if (ProgramCentralUtil.isNotNullString(taskCalendarId)) {
	    		taskCalendar = WorkCalendar.getCalendarObject(context,taskCalendarId, taskCalendarDate);
	    	}
	    	Calendar currentDayCalendar	=	Calendar.getInstance();
	    	Date today					=	currentDayCalendar.getTime();
			String deviationDays		=	"0";
			
			Date estimatedStartDateOnly	=	ProgramCentralUtil.removeTimeFromDate(context,estimatedStartDate);
			Date curentDateOnly			=	ProgramCentralUtil.removeTimeFromDate(context,today);
			int dateCompareValue		=	estimatedStartDateOnly.compareTo(curentDateOnly);
			
			//Don't consider tasks  
			//1:which are already completed.
			//2:which ESD is today.
			//3:which ESD is today in future and has percentComplete=0.
			if (percentComplete == 100 || dateCompareValue == 0	|| (dateCompareValue > 0 && percentComplete == 0)) {
				deviationDays	=   "0";
			} else if (estimatedStartDateOnly.after(curentDateOnly) && percentComplete > 0) {
				deviationDays =	getForwardDeviation(context,estimatedStartDateOnly,estimatedDuration,percentComplete,taskCalendar);
			} else {
				deviationDays =	getBackwardDeviation(context,estimatedStartDateOnly,estimatedDuration,percentComplete,taskCalendar);
			}
			
			deviationDays+= durationUnit;
			
			taskDeviationMap.put(taskId,deviationDays);
	    }
    	return taskDeviationMap;
    }
    /**
     * This method calculates deviation for task which ESD in future and task has been started.
     *  
	 * @param	context			
	 * 			Context object which is used while fetching data related application.
     * @param 	estimatedStartDate
     * 			Estimated start date of the task for which deviation is going to be calculated.
     * @param 	estimatedDuration
     * 			Estimated duration of the task for which deviation is going to be calculated.
     * @param 	percentComplete
     * 			Percent completed till today for the task.
     * 
     * @param 	taskCalendar
     * 			Calendar connected to the task, if any. else it is null.
     *  
     * @return	Forward deviation which has been calculated.
     * 
     * @throws 	FrameworkException
     * 	 		Exception can be thrown in case of method fail to execute.
     */
    protected String getForwardDeviation(Context context,Date estimatedStartDate,float estimatedDuration,
    										float percentComplete,WorkCalendar taskCalendar) throws FrameworkException {
    	
    	long deviationDays				=	0;
    	long deviationInMiliseconds		=	0;
    	Date referenceDate 				=	null;
    	Calendar currentDateCalendar	=	Calendar.getInstance();
		
		//for forward deviation, skip today and do calculation from tomorrow.
		currentDateCalendar.add(Calendar.DAY_OF_WEEK,1);
		Date tomorrowDateOnly		=	ProgramCentralUtil.removeTimeFromDate(context,currentDateCalendar.getTime());
		float deviationInFloat		=	(percentComplete*estimatedDuration/100);
		double deviationFractionValue	=	deviationInFloat - Math.floor(deviationInFloat);
		
		if (deviationFractionValue >= 0.5) {
			//Ceil the value if greater than/equal to 0.5
			deviationInFloat++;
		}
		deviationDays	=	(long)deviationInFloat;
		
		
		if (taskCalendar != null) {
			referenceDate	=	taskCalendar.computeFinishDate(context,estimatedStartDate,deviationDays);
		} else {
			if (deviationDays > 0) {
				deviationInMiliseconds	=	deviationDays*24*60*60*1000;
			}
			referenceDate	=	DateUtil.computeFinishDate(estimatedStartDate,deviationInMiliseconds);
		}
		deviationDays	=	DateUtil.computeDuration(tomorrowDateOnly,referenceDate);
		
		return ProgramCentralConstants.EMPTY_STRING+deviationDays;
    }
    /**
     * This method calculates deviation for task which ESD in past.
     *  
	 * @param	context			
	 * 			Context object which is used while fetching data related application.
     * @param 	estimatedStartDate
     * 			Estimated start date of the task for which deviation is going to be calculated.
     * @param 	estimatedDuration
     * 			Estimated duation of the task for which deviation is going to be calculated.
     * @param 	percentComplete
     * 			Percent completed till today for the task.
     * 
     * @param 	taskCalendar
     * 			Calendar connected to the task, if any. else it is null.
     *  
     * @return	Backword deviation which has been calcualted. Value returns with negative sign.
     * 
     * @throws 	FrameworkException
     * 	 		Exception can be thrown in case of method fail to execute.
     */
    protected String getBackwardDeviation(Context context,Date estimatedStartDate,float estimatedDuration,
											float percentComplete,WorkCalendar taskCalendar) throws FrameworkException {
    	
    	Date today						=	Calendar.getInstance().getTime();
    	Date curentDateOnly				=	ProgramCentralUtil.removeTimeFromDate(context,today);
    	long deviationDays				=	0;
    	long deviationInMiliseconds		=	0;
    	Date referenceDate 				=	null;
    	Calendar currentDateCalendar	=	Calendar.getInstance();
		Calendar referenceDateCalendar	=	Calendar.getInstance();
		
		//for backword deviation, skip today and do calculation till yesterday.
		currentDateCalendar.add(Calendar.DAY_OF_WEEK,-1);
		Date yesterDay	=	ProgramCentralUtil.removeTimeFromDate(context,currentDateCalendar.getTime());	
		
		if ((percentComplete == 0 || estimatedDuration == 0)) {
			if (taskCalendar != null) {
				deviationDays	=	taskCalendar.computeDuration(context,estimatedStartDate,yesterDay);
			} else {
				deviationDays	=	DateUtil.computeDuration(estimatedStartDate,yesterDay);
			}				
		} else {
			float deviationInFloat	=	(percentComplete*estimatedDuration/100);
			double deviationFractionValue	=	deviationInFloat - Math.floor(deviationInFloat);
			
			if (deviationFractionValue >= 0.5) {
				//Ceil the value if greater than/equal to 0.5
				deviationInFloat++;
			}
			deviationDays	=	(long)deviationInFloat;
			
			if (taskCalendar != null) {
				referenceDate	=	taskCalendar.computeFinishDate(context,estimatedStartDate,deviationDays);
				referenceDateCalendar.setTime(referenceDate);
				deviationDays	=	taskCalendar.computeDuration(context,referenceDateCalendar.getTime(),yesterDay);
			} else {
				if (deviationDays > 0) {
					//Task finishes at end of the day, hence don't consider finish date for deviation.
					deviationDays++;
					deviationInMiliseconds	=	deviationDays*24*60*60*1000;
				}
				referenceDate	=	DateUtil.computeFinishDate(estimatedStartDate,deviationInMiliseconds);
				referenceDateCalendar.setTime(referenceDate);
				deviationDays	=	DateUtil.computeDuration(referenceDateCalendar.getTime(),yesterDay);
			}
			
			Date referenceDateOnly	=	ProgramCentralUtil.removeTimeFromDate(context,referenceDateCalendar.getTime());
			
			//If reference date is today/after today,make deviation 0.
			if(referenceDateOnly.compareTo(curentDateOnly) >=0) {
				return "0";
			}
		}
		return ProgramCentralConstants.HYPHEN + deviationDays;
    }
    /**
     * This method populates and returns a map. This map holds deviation range as key and 
     * empty string as a value.
     * 
	 * @param	context			
	 * 			Context object which is used while fetching data related application.
     * @param 	argumentArray
     * 			Array which holds a map in which deviation smallest and biggest value is available.
     * 
     * @return	deviationFilterMap
     * 			Map which holds deviation range as key and empty string as a value. Value for
     * 			this map will get populated when that range value is selected by user in Gantt
     * 			filter. 
     * 
	 * @throws 	Exception		
	 * 			Exception can be thrown in case of method fail to execute.
     */
    public Set<String> getDeviationRangeMap(Context context,String[] argumentArray) throws Exception {
    	
    	Map<String,Integer> deviationValueMap	=	(Map<String,Integer>)JPO.unpackArgs(argumentArray);
    	Set<String> deviationFilterSet	=	new LinkedHashSet<String>();
    	String languageString		 =	context.getSession().getLanguage();
    	String durationUnit			 =	ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.DurationUnits.Days",languageString);
		durationUnit				 =	ProgramCentralConstants.SPACE + durationUnit;
		
    	int smallestValue	=	deviationValueMap.get(ProgramCentralConstants.SMALLEST_DEVIATION_VALUE);
	  	int biggestValue	=	deviationValueMap.get(ProgramCentralConstants.BIGGEST_DEVIATION_VALUE);
	  	int rangeValue		=	biggestValue - smallestValue;
	  	int deviationDiff	=	rangeValue/4;
	  	
	  	if (smallestValue == biggestValue || smallestValue >= -4 || (rangeValue <= 4)) {
	  		String deviationRange	=	smallestValue + ProgramCentralConstants.DEVIATION_RANGE_SEPARATOR + biggestValue + durationUnit;
	  		deviationFilterSet.add(deviationRange);
	  	} else {

	  		String deviationRange1	=	smallestValue + ProgramCentralConstants.DEVIATION_RANGE_SEPARATOR + (smallestValue+deviationDiff) + durationUnit;  
	  		String deviationRange2	=	(smallestValue+deviationDiff)+1 + ProgramCentralConstants.DEVIATION_RANGE_SEPARATOR + (smallestValue+deviationDiff*2) + durationUnit;
	  		String deviationRange3	=	(smallestValue+deviationDiff*2)+1 + ProgramCentralConstants.DEVIATION_RANGE_SEPARATOR + (smallestValue+deviationDiff*3) + durationUnit;
	  		String deviationRange4	=	(smallestValue+deviationDiff*3)+1 + ProgramCentralConstants.DEVIATION_RANGE_SEPARATOR + biggestValue + durationUnit;
	  
			deviationFilterSet.add(deviationRange1);
			deviationFilterSet.add(deviationRange2);
			deviationFilterSet.add(deviationRange3);
			deviationFilterSet.add(deviationRange4);
	  	}
  	  	return deviationFilterSet;
    }
    /**
     * This method returns map which holds the deviation value as a key and color for that range
     * as a value.
     *  
	 * @param	context			
	 * 			Context object which is used while fetching data related application.
     * @param 	argumentArray
     * 			Array of parameters which are needed to  populate the deviationColorMap.
     * @return	deviationColorMap
     * 			map which holds the deviation value as a key and color for that range as a value.
	 * @throws 	Exception		
	 * 			Exception can be thrown in case of method fail to execute.
     */
    public Map<String,Color> getDeviationColorMap(Context context,String[] argumentArray) throws Exception {
    	
    	List<String> deviationRangeList		=	(List<String>)JPO.unpackArgs(argumentArray);
    	Map<String,Color> deviationColorMap	=	new LinkedHashMap<String,Color>();
    	int deviationFilterSize	=	deviationRangeList.size();
    	
    	if (deviationFilterSize == 1) {
    		deviationColorMap.put(deviationRangeList.get(0),new Color(250,46,46));
    	} else if (deviationFilterSize == 2) {
    		deviationColorMap.put(deviationRangeList.get(0),new Color(250,46,46));
    		deviationColorMap.put(deviationRangeList.get(1),new Color(255,125,0));
    	} else if (deviationFilterSize == 3) {
    		deviationColorMap.put(deviationRangeList.get(0),new Color(250,46,46));
    		deviationColorMap.put(deviationRangeList.get(1),new Color(255,125,0));
    		deviationColorMap.put(deviationRangeList.get(2),new Color(255,175,0));
    	} else if (deviationFilterSize== 4) {
    		deviationColorMap.put(deviationRangeList.get(0),new Color(250,46,46));
    		deviationColorMap.put(deviationRangeList.get(1),new Color(255,125,0));
    		deviationColorMap.put(deviationRangeList.get(2),new Color(255,175,0));
    		deviationColorMap.put(deviationRangeList.get(3),new Color(190,247,129));
    	} else if (deviationFilterSize == 5) {
    		deviationColorMap.put(deviationRangeList.get(0),new Color(250,46,46));
    		deviationColorMap.put(deviationRangeList.get(1),new Color(255,125,0));
    		deviationColorMap.put(deviationRangeList.get(2),new Color(255,175,0));
    		deviationColorMap.put(deviationRangeList.get(3),new Color(190,247,129));
    		deviationColorMap.put(deviationRangeList.get(4),new Color(0,255,0));
    	} 
    	//Forward deviation color must be green every time.
    	boolean isForwardDeviationRange = isForwardDeviationRangeValue(context, deviationRangeList.get(deviationFilterSize-1));
    	if (isForwardDeviationRange) {
    		deviationColorMap.put(deviationRangeList.get(deviationFilterSize-1),new Color(0,255,0));
    	}
        return deviationColorMap;
    }
    /**
     * This method check weather given deviation range values is of forward deviation.
     *  
	 * @param		context			
	 * 						Context object which is used while fetching data related application.
     * @param 		deviationRangeValue
     * 						Range value of deviation which will be checked for forward deviation.
     * @return		isForwardDeviationRange
     * 						true, if given range value is of forward deviation else returns false.
     * 
	 * @throws 	Exception		
	 * 						Exception can be thrown in case of method fail to execute.
     */
    private boolean isForwardDeviationRangeValue(Context context,String deviationRangeValue) throws MatrixException {
    	
    	boolean isForwardDeviationRange = false;
    	String durationUnit		=	ProgramCentralUtil.getPMCI18nString(context,"emxProgramCentral.DurationUnits.Days",context.getSession().getLanguage());
    	deviationRangeValue	=	deviationRangeValue.substring(0,deviationRangeValue.indexOf(durationUnit));
		String[] deviationRangeArray =	deviationRangeValue.split(ProgramCentralConstants.DEVIATION_RANGE_SEPARATOR);
		int rangeStart		=	Integer.parseInt(deviationRangeArray[0].trim());
		int rangeFinish	=	Integer.parseInt(deviationRangeArray[1].trim());
		
		if(rangeStart >= 0 && rangeFinish >= 0) {
			isForwardDeviationRange	=	true;
		} 
    	return isForwardDeviationRange;
    }
    /**
     * This method will populate and return list of colors which will be applied on Gantt chart
     * tasks when user clicks on color icon of Gantt chart filter.
     *  
	 * @param	context			
	 * 			Context object which is used while fetching data related application.
     * @param 	argumentArray
     * 			Paramers which are need to populate color list.
     * @return	colorList
     * 			This list will contains colors which wil be applied on Gantt chart tasks.
     */
    public List<Color> getColorList(Context context,String[] argumentArray) {
    	
    	List<Color> colorList	=	new ArrayList<Color>();
		
		colorList.add(new Color(166,0,166));
		colorList.add(new Color(57,20,175));
		colorList.add(new Color(0,204,0));
		colorList.add(new Color(204,246,0));
		colorList.add(new Color(255,211,0));
		colorList.add(new Color(255,146,0));
		colorList.add(new Color(176,25,28));
		colorList.add(new Color(0,255,255));
		colorList.add(new Color(255,215,0));
		colorList.add(new Color(255,28,180));
		colorList.add(new Color(0,100,0));
		colorList.add(new Color(128,128,0));
		colorList.add(new Color(47,79,79));
		colorList.add(new Color(123,104,238));
		colorList.add(new Color(11,97,164));
		
		return colorList;
    }
    
    /**
     * This method returns list of maps. Each map in the list contains settings for 
     * custom a column to be added in Gantt chart table. For each custom column,
     * there will be a map in list. each map MUST contains 
     * 1:String resource property key which value will be column label.
     * 2:Data type of column value. This value should be get from 
     * 	 <code>CustomColumnDataTypeEnum</code>.
     * 3:Name of the JPO method which will manipulate and return value of that column.
     * 4:COLUMN_TYPE if static, this column will be appeared in Gantt chart filter.
     * 
     * This method must be override in emxGantt JPO to add dynamic columns in Gantt chart table.
     * 
	 * @param	context			
	 * 			Context object which is used while fetching data related application.
     * @param 	argumentArray
     * 			This Array holds name of view for which dynamic column is going to be rendered.
     * 
     * @return	customColumnSettingsMapList
     * 			List of maps. Each map in the list contains settings for custom a column to be
     * 			added in Gantt chart table. For each dynamic column,there will be a map in list.
     * 	
	 * @throws 	Exception		
	 * 			Exception can be thrown in case of method fail to execute.
     */
    public MapList getCustomColumnSettingsMapList(Context context,String[] argumentArray) throws Exception {
    	
    	MapList customColumnSettingsMapList = new MapList();
    	Map<String,String> jpoArgumentMap  = (Map<String,String>)JPO.unpackArgs(argumentArray);
    	
    	String objectId 	 = jpoArgumentMap.get("objectId");
		String rootType 	 = jpoArgumentMap.get("objectType");
		String ganttViewName = jpoArgumentMap.get("viewId");
		
    	//Add setting for Deviation column
    	if ((!"Project Template".equalsIgnoreCase(rootType)) && ProgramCentralConstants.VIEW_WBS.equalsIgnoreCase(ganttViewName)) {
    		
	    	Map<String,Object> deviationColumnSettingsMap = new HashMap<String,Object>();
	    	deviationColumnSettingsMap.put(ProgramCentralConstants.COLUMN_LABEL_PROPERTY_KEY,"emxProgramCentral.gantt.Deviation");
	    	deviationColumnSettingsMap.put(ProgramCentralConstants.COLUMN_VALUE_DATA_TYPE,ProgramCentralConstants.CustomColumnDataTypeEnum.FLOAT.getDatatype());
	    	deviationColumnSettingsMap.put(ProgramCentralConstants.COLUMN_VALUE_METHOD_NAME,"getTaskDeviationValue");
	    	//deviationColumnSettingsMap.put(ProgramCentralConstants.COLUMN_EXCLUDE_IN_REFINEMENT,Boolean.FALSE);
	    	//deviationColumnSettingsMap.put(ProgramCentralConstants.COLUMN_EDITABLE,Boolean.TRUE);
	    	//deviationColumnSettingsMap.put(ProgramCentralConstants.COLUMN_TYPE,ProgramCentralConstants.COLUMN_TYPE_STATIC);
	    	
	    	customColumnSettingsMapList.add(deviationColumnSettingsMap);
    	}
    	return customColumnSettingsMapList;
    }
}
