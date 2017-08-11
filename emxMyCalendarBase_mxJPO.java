/*
 * emxMyCalendarBase.java
 * Copyright (c) 2003-2016 Dassault Systemes.
 * All Rights Reserved
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 */

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.mycalendar.Day;
import com.matrixone.apps.program.mycalendar.Month;
import com.matrixone.apps.program.mycalendar.MyCalendar;
import com.matrixone.apps.program.mycalendar.MyCalendarConstants;
import com.matrixone.apps.program.mycalendar.MyCalendarUI;
import com.matrixone.apps.program.mycalendar.MyCalendarUtil;
import com.matrixone.apps.program.mycalendar.Week;

/**
 * The <code>emxMyCalendarBase</code> class represents the MyCalendar JPO
 */
public class emxMyCalendarBase_mxJPO extends com.matrixone.apps.program.Task
{
    public emxMyCalendarBase_mxJPO (Context context, String[] args)
    throws Exception
    {
        // Call the super constructor
        super();
        if (args != null && args.length > 0)
        {
            setId(args[0]);
        }
    }

    /**
     * Gets dynamic Columns for Day, Week and Month View is MyCalendar.
     *
     * @param context the ENOVIA <code>Context</code> object
     * @param args The arguments, it contains requestMap
     * @return The MapList
     * @throws Exception if operation fails
     */

    public MapList getDynamicColumnsInMyCalendar (Context context, String[] args) throws Exception
    {
        final String strDateFormat = eMatrixDateFormat.getEMatrixDateFormat();
        Map mapProgram = (Map) JPO.unpackArgs(args);
        Map requestMap = (Map)mapProgram.get("requestMap");

        Map mapColumn = new HashMap();
        Map<String,String> mapSettings = new HashMap<String,String>();
        MapList mlColumns = new MapList();

        HashMap paramList = (HashMap) mapProgram.get("paramList");
        String strCommandName = (String)requestMap.get("portalCmdName");
        String strDateTs = (String)requestMap.get("PMCMyCalendarTempDate");

        if(ProgramCentralUtil.isNullString(strDateTs)){
            strDateTs = MyCalendarUtil.getValueFromContext(context, "CurrentDate");
        }

        if(ProgramCentralUtil.isNullString(strCommandName)){
            strCommandName = MyCalendarUtil.getValueFromContext(context, "View");;
        }

        StringList slTimeFrame = new StringList();
        int date = 0;
        int month= 0;
        int year = 0;
        Calendar cal = Calendar.getInstance();
        Calendar calToday = Calendar.getInstance();
        if(null != strDateTs){
            cal = MyCalendarUtil.getCalendarObject(strDateTs);
        }

        Day[] days = null;
        int week = 0;
        if("PMCMyCalendarDay".equals(strCommandName)){
            cal.get(cal.DAY_OF_WEEK);
            week = cal.get(Calendar.WEEK_OF_YEAR);
            slTimeFrame.add(MyCalendarUtil.getWeekDayFromNumber(cal.get(cal.DAY_OF_WEEK)));
        }else if("PMCMyCalendarMonth".equals(strCommandName)){
            slTimeFrame = MyCalendarUtil.getEnglishWeekDays();
        }else{
            Week weekObj = new Week(context, cal, MyCalendarConstants.START_DAY_OF_WEEK);
            days = weekObj.getDaysInWeek();
            year = cal.get(Calendar.YEAR);
            week = cal.get(Calendar.WEEK_OF_YEAR);
            slTimeFrame = MyCalendarUtil.getEnglishWeekDays();
        }
        int count = 0;
        for (Iterator iterator = slTimeFrame.iterator(); iterator.hasNext();) {
            String strGroupHeader = "";
            String strTimeInterval = (String) iterator.next();
            String strTimeIntervalInt = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                    "emxProgramCentral.MyCalendar.WeekDays."+strTimeInterval, context.getSession().getLanguage());
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            String strDateRange = getMonthStartToEnd(context,month,year);
            if("PMCMyCalendarMonth".equals(strCommandName)){
                mapColumn = new HashMap();
                mapColumn.put("name", strTimeInterval);
                mapColumn.put("label", strTimeIntervalInt);
                mapSettings = new HashMap();
                mapSettings.put("Registered Suite","ProgramCentral");
                mapSettings.put("program","emxMyCalendar");
                mapSettings.put("Group Header",strDateRange);
                mapSettings.put("function","getMyCalendarColumnData");
                mapSettings.put("Column Type","programHTMLOutput");
                mapSettings.put("Editable","true");
                mapSettings.put("Export","true");
                mapSettings.put("Field Type","Basic");
                mapSettings.put("Sortable","false");
                mapSettings.put("Style Program","emxMyCalendar");
                mapSettings.put("Style Function","getOtherColumnStyles");
                mapSettings.put("Width","163");
                mapColumn.put("settings", mapSettings);
                mlColumns.add(mapColumn);
            }else if("PMCMyCalendarWeek".equals(strCommandName)){
                strGroupHeader = MyCalendarUtil.getDisplayDate(context,days[0])+" To "+MyCalendarUtil.getDisplayDate(context,days[6])+" ("+days[0].getWeek()+")";
                String strDate = ""+days[count].getDate();//(String)hmWeekDetails1.get(""+count);
                String strToday = "";
                if(MyCalendarUtil.areBothDatesEqual(days[count], calToday))
                    strToday = "<img src='../common/images/iconActionDown.png' />";
                strDateTs = MyCalendarUtil.getLabelFromCal(days[count]);
                count++;
                mapColumn = new HashMap();
                mapColumn.put("name", strDateTs);
                mapColumn.put("label", strDate+" "+strTimeIntervalInt+" "+strToday);
                mapSettings = new HashMap();
                mapSettings.put("Registered Suite","ProgramCentral");
                mapSettings.put("Group Header",strGroupHeader);
                mapSettings.put("program","emxMyCalendar");
                mapSettings.put("function","getMyCalendarColumnData");
                mapSettings.put("Column Type","programHTMLOutput");
                mapSettings.put("Editable","true");
                mapSettings.put("Style Program","emxMyCalendar");
                mapSettings.put("Style Function","getOtherColumnStyles");
                mapSettings.put("Export","true");
                mapSettings.put("Field Type","Basic");
                mapSettings.put("Sortable","false");
                mapSettings.put("Width","163");
                mapColumn.put("settings", mapSettings);
                mlColumns.add(mapColumn);
            }else{
                String strIn = ""+MyCalendarUtil.getWeekDayNumber(strTimeInterval);
                strTimeIntervalInt = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                        "emxProgramCentral.MyCalendar.WeekDays."+strIn, context.getSession().getLanguage());
                mapColumn = new HashMap();
                mapColumn.put("name", strDateTs);
                mapColumn.put("label", strTimeIntervalInt);
                mapSettings = new HashMap();
                mapSettings.put("Registered Suite","ProgramCentral");
                mapSettings.put("Group Header",cal.get(Calendar.DATE)+"-"+MyCalendarUtil.getInternationalMonthNameFromNumber(context,cal.get(Calendar.MONTH))+"-"+cal.get(Calendar.YEAR)+" ("+week+")");
                mapSettings.put("program","emxMyCalendar");
                mapSettings.put("function","getMyCalendarColumnData");
                mapSettings.put("Column Type","programHTMLOutput");
                mapSettings.put("Style Program","emxMyCalendar");
                mapSettings.put("Style Function","getOtherColumnStyles");
                mapSettings.put("Editable","true");
                mapSettings.put("Export","true");
                mapSettings.put("Field Type","Basic");
                mapSettings.put("Sortable","false");
                mapColumn.put("settings", mapSettings);
                mlColumns.add(mapColumn);
            }
        }

        return mlColumns;
    }

    /**
     * Will populate Tasks and Meetings data in My Calendar Day, Week and Month Views
     *
     * @param context the ENOVIA <code>Context</code> object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getMyCalendarColumnData(Context context, String[] args)
    throws Exception
    {
        Vector vecResult = new Vector();
        try {
            // Get object list information from packed arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            Map columnMap = (Map) programMap.get("columnMap");
            String strDay = (String) columnMap.get(SELECT_NAME);

            MapList objectList = (MapList) programMap.get("objectList");

            HashMap paramList = (HashMap) programMap.get("paramList");
            String strCommandName = (String)paramList.get("portalCmdName");
            String strDueTask = (String)paramList.get("PMCMyCalendarTaskTypes");
            boolean showActiveTasks = false;
            if(!"Due".equalsIgnoreCase(strDueTask)){
                showActiveTasks = true;
            }
            if(ProgramCentralUtil.isNullString(strCommandName)){
                strCommandName = MyCalendarUtil.getValueFromContext(context,"View");
            }

            StringBuffer sbHTML = new StringBuffer();
            String strMeeting = "";

            int iCount = 0;
            int iStartTime = 0;
            boolean isSmall = false;
            Day dayInCal = null;
            for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
            {
                Map objectMap = (Map) itrTableRows.next();
                String strObjectId = (String)objectMap.get(SELECT_ID);

                boolean shouldRenderMeetings = true;
                boolean isTopRow = false;

                strMeeting = "";
                sbHTML = new StringBuffer();

                int height = 0;

                if(iCount==0){
                    isTopRow = true;
                    height = 100;
                }else{
                    isTopRow = false;
                    height = 40;
                }

                if("PMCMyCalendarDay".equals(strCommandName)){
                    sbHTML.append(MyCalendarUI.getDayData(context, strDay, height, iStartTime, isTopRow, showActiveTasks));
                    if(!isTopRow)
                        iStartTime++;
                }else if("PMCMyCalendarMonth".equals(strCommandName)){
                    sbHTML.append(MyCalendarUI.getMonthData(context, strDay, strObjectId, iCount, isSmall, shouldRenderMeetings, height, strMeeting));
                    isSmall = (!isSmall);
                }else{
                    sbHTML.append(MyCalendarUI.getWeekData(context, strDay, height, iStartTime, isTopRow, strMeeting, showActiveTasks));
                    if(!isTopRow)
                        iStartTime++;
                }
                iCount++;
                vecResult.add(sbHTML.toString());
            }
            return vecResult;

        } catch (Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * getColumnMemberAssigneeData - Where : In the Structure Browser, Gets the data % Allocation Data for each Task Assignee in.
     *
     * @param context the ENOVIA <code>Context</code> object
     * @param args The arguments, it contains objectList and paramList maps
     * @return The Vector object containing
     * @throws Exception if operation fails
     */
    public Vector getMyCalendarIntervalColumnData(Context context, String[] args)
    throws Exception
    {
        Vector vecResult = new Vector();
        try {
            // Create result vector
            // Get object list information from packed arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            HashMap paramList = (HashMap) programMap.get("paramList");
            String strCommandName = (String)paramList.get("portalCmdName");
            if(ProgramCentralUtil.isNullString(strCommandName)){
                strCommandName = MyCalendarUtil.getValueFromContext(context, "View");
            }
            Calendar calendar = null;
            String strDate = MyCalendarUtil.getValueFromContext(context, "CurrentDate");

            if(ProgramCentralUtil.isNullString(strDate)){
                calendar = Calendar.getInstance();
            }else{
                calendar = MyCalendarUtil.getCalendarObject(strDate);
            }

            String strAM = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                    "emxProgramCentral.MyCalendar.Time.AM", context.getSession().getLanguage());
            String strPM = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                    "emxProgramCentral.MyCalendar.Time.PM", context.getSession().getLanguage());
            String strW = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                    "emxProgramCentral.MyCalendar.Time.Week", context.getSession().getLanguage());
            int flt = -1;
            int count = 0;
            boolean small = false;
            String strSup = "";

            for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
            {
                String strToDisplay = "";
                Map objectMap = (Map) itrTableRows.next();
                String strObjectId = (String)objectMap.get(SELECT_ID+"A");

                int height = 0;
                if("PMCMyCalendarMonth".equals(strCommandName)){
                    if(small){
                        height = 20;
                        strToDisplay = strW+strObjectId;
                    }else{
                        if(flt==6){
                            strToDisplay = "Tasks";
                        }else{
                            strToDisplay = "";
                        }
                        height = 100;
                    }
                    small = (!small);
                    flt++;
                }else{
                    if(0.00==flt){
                        strSup = "<sup> "+strAM+"</sup>";
                    }else if(12.00==flt){
                        strSup = "<sup> "+strPM+"</sup>";
                    }else{
                        strSup = "<sup> 00</sup>";
                    }

                    if(count==0){
                        strToDisplay = "Tasks";
                        flt++;
                    }else{
                        if(0.00==flt)
                            strToDisplay = 12+"<label style='font-size: 12pt;'>"+strSup+"</label>";
                        else
                            strToDisplay = flt+"<label style='font-size: 12pt;'>"+strSup+"</label>";
                        if(12.00==flt){
                            flt = 0;
                            flt++;
                        }else{
                            flt++;
                        }
                    } count++;
                }

                StringBuffer sbBuffer = new StringBuffer();
                sbBuffer = new StringBuffer();
                if(strToDisplay.equalsIgnoreCase("Tasks")){
                    if(!"PMCMyCalendarMonth".equals(strCommandName)){
                        sbBuffer.append(MyCalendarUI.getVerticalTaskLabel(context));
                    }
                }else if("PMCMyCalendarDay".equals(strCommandName)){
                    sbBuffer.append("<table style='border-collapse: collapse; width: 70px; height: 40px;'>");
                    sbBuffer.append("<tbody>");
                    sbBuffer.append("<tr>");
                    sbBuffer.append("<td valign='top' align='right' style='border: 0px solid rgb(255, 255, 255); text-align: right;'>");
                    sbBuffer.append("<label style='font-size: 14pt;'>"+strToDisplay+"</label>");
                    sbBuffer.append("</td>");
                    sbBuffer.append("</tr>");
                    sbBuffer.append("</tbody>");
                    sbBuffer.append("</table>");
                }else if("PMCMyCalendarMonth".equals(strCommandName)){
                    if(strToDisplay.equalsIgnoreCase("")){
                        String strMeeting = "";
                        sbBuffer.append(strMeeting);
                    }else{
                        sbBuffer.append("<table style='border-collapse: collapse; width: 70px; height: "+height+"px;'>");
                        sbBuffer.append("<tbody>");
                        sbBuffer.append("<tr>");
                        sbBuffer.append("<td style='border: 0px solid rgb(255, 255, 255); text-align: right;'>");
                        sbBuffer.append("<label style='font-size: 10pt; font-weight:bold;'>"+strToDisplay+"</label>");
                        sbBuffer.append("</td>");
                        sbBuffer.append("</tr>");
                        sbBuffer.append("</tbody>");
                        sbBuffer.append("</table>");
                    }
                }else{
                    sbBuffer.append("<table style='border-collapse: collapse; width: 70px; height: 40px;'>");
                    sbBuffer.append("<tbody>");
                    sbBuffer.append("<tr>");
                    sbBuffer.append("<td style='border: 0px solid rgb(255, 255, 255); text-align: right;'>");
                    sbBuffer.append("<label style='font-size: 14pt;'>"+strToDisplay+"</label>");
                    sbBuffer.append("</td>");
                    sbBuffer.append("</tr>");
                    sbBuffer.append("</tbody>");
                    sbBuffer.append("</table>");
                }
                vecResult.add(sbBuffer.toString());
            }
            return vecResult;

        } catch (Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets data for ID column in Month view especially used to maintain the correct order of data display.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Vector getMyCalendarIDData(Context context, String[] args)
    throws Exception
    {
        Vector vecResult = new Vector();
        try {
            // Create result vector
            // Get object list information from packed arguments
            Map programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
            {
                Map objectMap = (Map) itrTableRows.next();
                String strColId = ""+objectMap.get(SELECT_LEVEL+"!");
                if("null".equalsIgnoreCase(strColId))
                    strColId = "";
                vecResult.add(strColId);
            }
            return vecResult;

        } catch (Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }

    /**
     * expand program for My Calendar in Day, Week and Month View.
     * @param context the ENOVIA <code>Context</code> object
     * @param args
     * @return
     * @throws Exception
     */

    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList expandMyCalendar(Context context, String[] args) throws Exception
    {
        HashMap mapProgram = (HashMap)JPO.unpackArgs(args);
        Map requestMap = (Map)mapProgram.get("requestMap");
        HashMap paramList = (HashMap) mapProgram.get("paramList");
        String strCommandName = (String)mapProgram.get("portalCmdName");
        String strDate = MyCalendarUtil.getValueFromContext(context, "CurrentDate");
        if(ProgramCentralUtil.isNullString(strCommandName)){
            strCommandName = MyCalendarUtil.getValueFromContext(context, "View");
        }

        Calendar cal = Calendar.getInstance();

        if(null != strDate){
            cal = MyCalendarUtil.getCalendarObject(strDate);
        }

        StringBuffer sbBuffer = new StringBuffer();
        MapList mapList = new MapList();
        HashMap hmXAxis = new HashMap();
        StringList slTimeFrame = new StringList();

        if("PMCMyCalendarDay".equals(strCommandName)){
            Day day = new Day(context, MyCalendarUtil.getCalendarObject(strDate), MyCalendarConstants.START_DAY_OF_WEEK);
            return day.getRows();
        }else if("PMCMyCalendarMonth".equals(strCommandName)){
            Month mont = new Month(context, MyCalendarUtil.getCalendarObject(strDate), MyCalendarConstants.START_DAY_OF_WEEK);
            mapList = mont.getRows();
        }else{
            Week week = new Week(context, MyCalendarUtil.getCalendarObject(strDate), MyCalendarConstants.START_DAY_OF_WEEK);
            return week.getRows();
        }

        return mapList;
    }

    /**
     * Lists Due or Active Objects in range.
     * @param context the ENOVIA <code>Context</code> object
     * @param args
     * @return HashMap
     * @throws MatrixException
     */

    public HashMap getRangeForTasksToShow(Context context, String[] args) throws MatrixException
    {
        HashMap mapTaskTypeNames = new HashMap();
        try {
            StringList slTaskToShowVal = new StringList(2);
            slTaskToShowVal.add("Due");
            slTaskToShowVal.add("Active");

            StringList slTaskToShowDisp = new StringList(2);
            String strIntDue = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                    "emxProgramCentral.MyCalendar.Objects.Due", context.getSession().getLanguage());
            String strIntValue = EnoviaResourceBundle.getProperty(context, "ProgramCentral",
                    "emxProgramCentral.MyCalendar.Objects.Active", context.getSession().getLanguage());
            slTaskToShowDisp.add(strIntDue);
            slTaskToShowDisp.add(strIntValue);

            mapTaskTypeNames.put("field_choices", slTaskToShowVal);
            mapTaskTypeNames.put("field_display_choices", slTaskToShowDisp);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new MatrixException(e);
        } return mapTaskTypeNames;
    }

    /**
     * Gets A months Start Date and End Date String
     * example 1 2011
     * Return 1-Jan-2011 To 31-Jan-2011
     * @param month
     * @param year
     * @return
     */
    private String getMonthStartToEnd(Context context, int month, int year){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        int maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int minDaysInMonth = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
        String strStart = minDaysInMonth+"-"+MyCalendarUtil.getInternationalMonthNameFromNumber(context,cal.get(Calendar.MONTH))+"-"+cal.get(Calendar.YEAR);
        String strEnd = maxDaysInMonth+"-"+MyCalendarUtil.getInternationalMonthNameFromNumber(context,cal.get(Calendar.MONTH))+"-"+cal.get(Calendar.YEAR);
        return strStart+" - "+strEnd;
    }


    /**
     * Gets Styles for the first column in MyCalendar
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getFirstColumnStyles(Context context, String[] args)  throws Exception
    {
        try
        {
            StringList slStyles = new StringList();
            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");

            for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
            {
                Map mapObjectInfo = (Map) itrTableRows.next();
                slStyles.addElement("my-calendar-margin-column");
            }
            return slStyles;

        } catch (Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Gets styles for interval columns in My Calendar.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getOtherColumnStyles(Context context, String[] args)  throws Exception
    {
        try
        {
            StringList slStyles = new StringList();
            // Get object list information from packed arguments
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            String strDate = MyCalendarUtil.getValueFromContext(context, "CurrentDate");
            Calendar currentDate = MyCalendarUtil.getCalendarObject(strDate);
            Map columnMap = (Map) programMap.get("columnMap");
            String strDay = (String) columnMap.get(SELECT_NAME);
            MyCalendar calendar = null;
            if(strDay.contains("-")){
                calendar = new MyCalendar(context, MyCalendarUtil.getCalendarObject(strDay),MyCalendarConstants.START_DAY_OF_WEEK);
            }else{
                calendar = new MyCalendar(context, Calendar.getInstance(), MyCalendarConstants.START_DAY_OF_WEEK);
            }

            StringList slWeekEnds = calendar.getWeekEnds();

            String strDayInWeek = strDay;

            Calendar calToday = Calendar.getInstance();
            Calendar calInCalendar = Calendar.getInstance();
            boolean isMonth = false;
            boolean isToday = false;
            boolean isOutSideCurrentMonth = false;

            if(strDay.contains("-")){
                calInCalendar = MyCalendarUtil.getCalendarObject(strDay);
                Day day = new Day(context, calInCalendar, MyCalendarConstants.START_DAY_OF_WEEK);
                strDayInWeek = ""+day.getDayOfWeek();
            }else if(!strDay.contains("ID")){
                isMonth = true;
            }
            int iCount = 0;
            for (Iterator itrTableRows = objectList.iterator(); itrTableRows.hasNext();)
            {
                Map mapObjectInfo = (Map) itrTableRows.next();
                boolean isHoliday = false;
                if(isMonth){
                    String strWeekDetails = (String)mapObjectInfo.get(SELECT_ID+"A");
                    StringList slWeekDetails = FrameworkUtil.splitString(strWeekDetails, "-");
                    String strWeek = ((String)slWeekDetails.get(0)).trim();
                    String strYear = ((String)slWeekDetails.get(1)).trim();
                    int weekday = Integer.parseInt(strDay);
                    calToday.set(Calendar.WEEK_OF_YEAR,Integer.parseInt(strWeek));
                    calToday.set(Calendar.YEAR,Integer.parseInt(strYear));
                    Week week = new Week(context, calToday , MyCalendarConstants.START_DAY_OF_WEEK);
                    Day[] days = week.getDaysInWeek();

                    //Logic for Holidays and Today
                    for (Day day: days){
                        if(day.getDayOfWeek() == weekday){
                            Day dayTemp = new Day(context, MyCalendarUtil.getCalendarObject(day), MyCalendarConstants.START_DAY_OF_WEEK);
                            if(dayTemp.getHolidays(false).size()>0){
                                isHoliday = true;
                            }
                            isToday = MyCalendarUtil.areBothDatesEqual(day, calInCalendar);
                            isOutSideCurrentMonth = MyCalendarUtil.isOutsideCurrentMonth(currentDate, MyCalendarUtil.getCalendarObject(day));
                            break;
                        }
                    }
                }

                String strWeek = (String) mapObjectInfo.get("WEEK");
                if(isMonth && iCount == 0){
                    slStyles.addElement("my-calendar-margin-column");
                }else if(isToday && !"true".equalsIgnoreCase(strWeek)){
                    slStyles.addElement("my-calendar-today");
                }else if(isHoliday && "true".equalsIgnoreCase(strWeek)){
                    slStyles.addElement("my-calendar-holiday");
                }else if((slWeekEnds.contains(strDayInWeek) && !isMonth) || "true".equalsIgnoreCase(strWeek) || isOutSideCurrentMonth){
                    slStyles.addElement("my-calendar-weekend");
                }else{
                    slStyles.addElement("my-calendar-margin-column");
                }
                isToday = false;
                iCount++;
            }

            return slStyles;

        } catch (Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    /**
     * Returns true only when user selects monthly view
     * @param context
     * @param args
     * @return
     */
    public boolean isMonthlyView(Context context, String[] args) throws MatrixException{
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String strCommandName = (String)programMap.get("portalCmdName");
            if(ProgramCentralUtil.isNullString(strCommandName))
                strCommandName = MyCalendarUtil.getValueFromContext(context, "View");

            if("PMCMyCalendarMonth".equalsIgnoreCase(strCommandName))
                return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new MatrixException(e);
        }
        return false;
    }

    /**
     * Filters Objects to be displayed in My Calendar.
     * Used like a exclude OID program.
     * Apps can write Object Type specific code over here to prevent Tasks from showing up in the My Calendar.
     * Any reference to App specific APIs should be avoided.
     * Only use generic ADK APIs for logic.
     * @param context
     * @param objects
     * @return
     * @throws MatrixException
     */
    public MapList excludeObject(Context context, String[] args) throws MatrixException{
        MapList mlObjectFiltered = new MapList();
        try{
            final String TYPE_EXPERIMENT = PropertyUtil.getSchemaProperty(context,"type_Experiment");
            Map programMap = (HashMap) JPO.unpackArgs(args);
            MapList objects = (MapList) programMap.get("objectList");

            String TYPE_TASK = PropertyUtil.getSchemaProperty(context,"type_Task");
            String TYPE_PHASE = PropertyUtil.getSchemaProperty(context,"type_Phase");
            String TYPE_GATE = PropertyUtil.getSchemaProperty(context,"type_Gate");
            String TYPE_MILESTONE = PropertyUtil.getSchemaProperty(context,"type_Milestone");
            String POLICY_PROJECT_SPACE_HOLD_CANCEL = PropertyUtil.getSchemaProperty(context,"policy_ProjectSpaceHoldCancel");
            String STATE_HOLD = PropertyUtil.getSchemaProperty(context,"policy",POLICY_PROJECT_SPACE_HOLD_CANCEL,"state_Hold");
            String STATE_CANCEL = PropertyUtil.getSchemaProperty(context,"policy",POLICY_PROJECT_SPACE_HOLD_CANCEL,"state_Cancel");

            String []strTaskIDs = new String[objects.size()];
            for(int i=0;i<objects.size();i++){
                Map mpTask = (Map)objects.get(i);
                String strTaskId = (String)mpTask.get(DomainObject.SELECT_ID);
                strTaskIDs[i] = strTaskId;
            }

            String SELECT_PROJECT_STATE = "to["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.current";
            String SELECT_PROJECT_TYPE = "to["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.type";
            String SELECT_PROJECT_ID = "to["+DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY+"].from.from[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].to.id";
            StringList slSelectable = new StringList();
            slSelectable.addElement(SELECT_PROJECT_STATE);
            slSelectable.addElement(SELECT_PROJECT_TYPE);
            slSelectable.addElement(SELECT_PROJECT_ID);
            slSelectable.addElement(DomainObject.SELECT_ID);

            MapList mlTaskList = DomainObject.getInfo(context, strTaskIDs, slSelectable);

            for(int ind=0;ind<objects.size();ind++){
                Map mpTaskObject = (Map)objects.get(ind);
                Map mpTaskDetails = (Map)mlTaskList.get(ind);

                String strObjectId = (String)mpTaskObject.get(DomainObject.SELECT_ID);
                String strObjectType = (String)mpTaskObject.get(DomainObject.SELECT_TYPE);
                String strObjectParentType = (String)mpTaskDetails.get(SELECT_PROJECT_TYPE);
                String strObjectParentState = (String)mpTaskDetails.get(SELECT_PROJECT_STATE);
                String strObjectParentId = (String)mpTaskDetails.get(SELECT_PROJECT_ID);
                String strObjectId1 = (String)mpTaskDetails.get(DomainObject.SELECT_ID);

                if(strObjectId.equals(strObjectId1)){
                    DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
                    DomainObject domParent = DomainObject.newInstance(context, strObjectParentId);
                    if(dmoObject.isKindOf(context, TYPE_TASK) || dmoObject.isKindOf(context, TYPE_PHASE)
                            || dmoObject.isKindOf(context, TYPE_GATE) || dmoObject.isKindOf(context, TYPE_MILESTONE)){
                        //License Check for PRG Types.
                        StringList sl = MyCalendarUtil.getLicensesAssignedToUser(context);
                        boolean isLicenseAllocated = false;
                		try{
                			ComponentsUtil.checkLicenseReserved(context, ProgramCentralConstants.PGE_LICENSE_ARRAY);
                            isLicenseAllocated = true;
                		}catch(Exception e){
                			isLicenseAllocated = false;
                		}
                        if(!STATE_HOLD.equals(strObjectParentState) && !STATE_CANCEL.equals(strObjectParentState)
                                && !domParent.isKindOf(context, TYPE_EXPERIMENT)&& isLicenseAllocated){
                            mlObjectFiltered.add(mpTaskObject);
                        }
                    }else{
                        mlObjectFiltered.add(mpTaskObject);
                    }
                }
            }
        } catch (FrameworkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new MatrixException(e);
        } catch (Exception e){
            e.printStackTrace();
            throw new MatrixException(e);
        }

        return mlObjectFiltered;
    }

}
