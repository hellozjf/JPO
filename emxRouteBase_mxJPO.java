/*
 *  emxRouteBase.java
 *
 * Copyright (c) 1992-2017 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.AccessItr;
import matrix.db.AccessList;
import matrix.db.Attribute;
import matrix.db.AttributeList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.ExpansionWithSelect;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.Relationship;
import matrix.db.RelationshipType;
import matrix.db.RelationshipWithSelectItr;
import matrix.db.Role;
import matrix.db.RoleList;
import matrix.db.State;
import matrix.db.StateList;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Document;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.UserTask;
import com.matrixone.apps.common.Workspace;
import com.matrixone.apps.common.WorkspaceVault;
import com.matrixone.apps.common.util.ComponentsUIUtil;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.AccessUtil;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jsystem.util.StringUtils;
import com.matrixone.apps.framework.ui.UIExpression;

/**
 * @version AEF Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxRouteBase_mxJPO extends emxDomainObject_mxJPO
{
      // Updating StringBuffers
    protected static final String sbRouteStatus = "attribute["+DomainObject.ATTRIBUTE_ROUTE_STATUS+          "]";
    protected static final String selParentRouteOwner = "to["+DomainConstants.RELATIONSHIP_TASK_SUBROUTE+"].from.from["+DomainConstants.RELATIONSHIP_ROUTE_TASK+"].to.owner";
    protected static final String attRouteVisibility = "attribute["+DomainObject.ATTRIBUTE_SUBROUTE_VISIBILITY+          "]";
    protected static final String attRestrictMembers = "attribute["+DomainObject.ATTRIBUTE_RESTRICT_MEMBERS+          "]";

     protected static String activeFilter = "";
     protected static String inActiveFilter = "";
     protected static final String strMatchList = " matchlist ";
    static final String AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME = "Workspace Access Grantor";
    static final String AEF_WORKSPACE_LEAD_GRANTOR_USERNAME = "Workspace Lead Grantor";
    static final String AEF_WORKSPACE_MEMBER_GRANTOR_USERNAME = "Workspace Member Grantor";
    static final String AEF_ROUTE_DELEGATION_GRANTOR_USERNAME = "Route Delegation Grantor";
    static final String AEF_ROUTE_ACCESS_GRANTOR_USERNAME = "Route Access Grantor";
    static final String AEF_COMMON_ACCESS_GRANTOR_USERNAME = "Common Access Grantor";
    static final String RED="Red";
    static final String YELLOW="Yellow";
    static final String GREEN="Green";


    /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public emxRouteBase_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);

      synchronized(emxRouteBase_mxJPO.class) {
          try
              {
                  activeFilter = EnoviaResourceBundle.getProperty(context,"emxComponentsRoutes.Filter.Active");
                  inActiveFilter = EnoviaResourceBundle.getProperty(context,"emxComponentsRoutes.Filter.InActive");
              }
          catch(Exception e)
              {
                  throw e;
              }
      }
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns nothing
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (!context.isConnected())
            throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
        return 0;
    }

    /**
     * getMyRoutes - gets the list of Routes the user has access to, used as input for the Route Summary table
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getMyRoutes(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            String parentId           = (String) programMap.get("objectId");
            String selOwner           = DomainConstants.SELECT_OWNER;

            StringBuffer selRouteNode = new StringBuffer();
            selRouteNode.append("to[");
            selRouteNode.append(DomainConstants.RELATIONSHIP_ROUTE_NODE);
            selRouteNode.append("].from.id");


            com.matrixone.apps.common.Person loggedInPerson = com.matrixone.apps.common.Person.getPerson(context);
            String loggedInUser       = loggedInPerson.getName(context);

            // Where clause to display routes only if users has min Read access to the Route

//             commented on 10th March for displaying Sub Routes to Parent Route Owner
//            String sTypeWhere  = "(current.access[read] == true && current.access[checkout] == true && current.access[show] == true)";



            DomainObject connectedObject = new DomainObject(parentId);
            StringList typeSelects = new StringList();
            typeSelects.add(DomainConstants.SELECT_ID);
            typeSelects.add(selOwner);
            typeSelects.add(attRestrictMembers.toString());
            String objectType = connectedObject.getType(context);
            MapList routeList = new MapList();
            typeSelects.add(selParentRouteOwner.toString());
            typeSelects.add(attRouteVisibility.toString());

      String type = connectedObject.getType(context);
      String sTypeWhere  = "";
      Pattern relPattern         = null;
            Pattern typePattern        = new Pattern(DomainConstants.TYPE_ROUTE);

      if(!type.equals(DomainConstants.TYPE_INBOX_TASK))
      {
    	  sTypeWhere  = "(current.access[read] == true && current.access[show] == true)";
         relPattern  = new Pattern(DomainConstants.RELATIONSHIP_ROUTE_SCOPE);
               relPattern.addPattern(DomainObject.RELATIONSHIP_OBJECT_ROUTE);
               relPattern.addPattern(DomainObject.RELATIONSHIP_ROUTE_TASK);
            }
      else
      {
           relPattern  = new Pattern(DomainConstants.RELATIONSHIP_TASK_SUBROUTE);
      }

      routeList   = connectedObject.getRelatedObjects(context,
                                                                 relPattern.getPattern(),
                                                                 typePattern.getPattern(),
                                                                 typeSelects,
                                                                 null,
                                                                 false,
                                                                 true,
                                                                 (short)1,
                                                                 sTypeWhere,
                                                                 null,
                                                                 null,
                                                                 null,
                                                                 null);
                Iterator routeListItr = routeList.iterator();
                MapList tempRouteList = new MapList();
                StringList memberRouteList =   loggedInPerson.getInfoList(context , selRouteNode.toString());
                while(routeListItr.hasNext())
                {
                    Map routeMap = (Map)routeListItr.next();
                    String routeOwner = (String)routeMap.get(selOwner);
                    String routeId    = (String)routeMap.get(DomainConstants.SELECT_ID);
                    String parentRouteOwner = (String)routeMap.get(selParentRouteOwner.toString());
                 if((routeOwner != null && routeOwner.equals(loggedInUser)) || (parentRouteOwner != null && parentRouteOwner.equals(loggedInUser)))
                    {
                    String viewSubRoute = (String)routeMap.get(attRouteVisibility.toString());
                    if(viewSubRoute != null && viewSubRoute.equals("No"))
                    {
                        if(parentRouteOwner == null || "".equals(parentRouteOwner))
                        {
                            tempRouteList.add(routeMap);
                        }
                        else if(parentRouteOwner != null && !(loggedInUser.equals(parentRouteOwner)))
                        {
                            tempRouteList.add(routeMap);
                        }
                    }
                    else
                    {
                        tempRouteList.add(routeMap);
                        }
                    }
//added for 308049
                    else
                    {
                        tempRouteList.add(routeMap);
                    }
 //till here
                }
                routeList     = tempRouteList;
                tempRouteList = null;
            return routeList;
        }
        catch (Exception ex)
        {
            throw ex;
        }
  }

    /**
     * showCheckbox - determines if the checkbox needs to be enabled in the column of the Route Summary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public Vector showCheckbox(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            Vector enableCheckbox = new Vector();
            String user = context.getUser();

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                String routeId = (String) objectMap.get(Route.SELECT_ID);

                DomainObject routeObject = new DomainObject(routeId);
                String owner = routeObject.getInfo(context, routeObject.SELECT_OWNER);
                AccessUtil accessUtil     = new AccessUtil();  // added for bug 278128
                Access access =  routeObject.getAccessMask(context);  // added for bug 278128
                if(user.equals(owner) && accessUtil.hasRemoveAccess(access))   // modified for bug 278128
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

    /**
     * showStatusGif - gets the status gif to be shown in the column of the Route Summary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public Vector showStatusGif(Context context, String[] args)
        throws Exception
    {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            String sStatus = "";
            String statusColor = "";
            Vector vecResult = new Vector();

            if ( objectList == null || objectList.size() == 0) {
                return vecResult;
            }

            final String SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE = "attribute[" + DomainObject.ATTRIBUTE_ACTUAL_COMPLETION_DATE + "]";
            final String SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE = "attribute[" + DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE + "]";
            Map mapObjectInfo = null;
            DomainObject dmoRoute = null;
            String strRouteId = null;
            StringList slBusSelect = new StringList();
            StringList slRelSelect = new StringList();
            slRelSelect.add(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
            slRelSelect.add(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
            String strObjWhr = null;
            String strRelWhr = null;
            short nRecurseLevel = (short)1;
            MapList mlRouteNodes = null;
            MapList mlTemp = null;
            Map mapRouteNodeInfo = null;
            String strTaskDueDate = null;
            String strTaskCompletionDate = null;
            String strRotueDueDate = null;
            Date dtDueDate = null;
            Date dtCompletionDate = null;
            Date dtCurrentDate = new Date();
            MapList mlLastTasks = null;
            MapList mlNonLastTasks = null;

            // Do for each route
            for (Iterator itrObjectList = objectList.iterator(); itrObjectList.hasNext();) {
                mapObjectInfo = (Map)itrObjectList.next();
                strRouteId = (String)mapObjectInfo.get(Route.SELECT_ID);
                statusColor = null;

                dmoRoute = DomainObject.newInstance(context, strRouteId);
                sStatus =  dmoRoute.getInfo(context,Route.SELECT_ROUTE_STATUS);

                if (sStatus != null &&  !"".equals(sStatus)) {
                    sStatus= i18nNow.getRangeI18NString(Route.ATTRIBUTE_ROUTE_STATUS, sStatus, context.getSession().getLanguage());
                }
                else {
                    sStatus= "*";
                }
                mlRouteNodes = dmoRoute.getRelatedObjects(context,
                        DomainObject.RELATIONSHIP_ROUTE_NODE,
                        DomainObject.TYPE_PERSON + "," + DomainObject.TYPE_ROUTE_TASK_USER,
                        slBusSelect,
                        slRelSelect,
                        false,
                        true,
                        nRecurseLevel,
                        strObjWhr,
                        strRelWhr);

                if (mlRouteNodes == null || mlRouteNodes.size() == 0) {
                    statusColor = "";
                }
                else {
                    // Remove the entries with empty due dates
                    mlTemp = new MapList();
                    for (Iterator itrRouteNodes = mlRouteNodes.iterator(); itrRouteNodes.hasNext();) {
                        mapRouteNodeInfo = (Map) itrRouteNodes.next();
                        strTaskDueDate = (String)mapRouteNodeInfo.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                        if (strTaskDueDate != null && !"".equals(strTaskDueDate)) {
                            mlTemp.add(mapRouteNodeInfo);
                        }
                    }
                    mlRouteNodes = mlTemp;
                    mlTemp = null;

                    if (mlRouteNodes.size() == 0) {
                        statusColor = GREEN;
                    }
                    else {
                        // Sort maplist descending w.r.t. due dates
                        mlRouteNodes.sort(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE, "descending", "date");

                        // Get route's due date, first due date in sorted list
                        mapRouteNodeInfo = (Map)mlRouteNodes.get(0);
                        strRotueDueDate = (String)mapRouteNodeInfo.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);

                        // Separate the last tasks of the route from othe tasks
                        mlLastTasks = new MapList();
                        mlNonLastTasks = new MapList();
                        for (Iterator itrRouteNodes = mlRouteNodes.iterator(); itrRouteNodes.hasNext();) {
                            mapRouteNodeInfo = (Map) itrRouteNodes.next();
                            strTaskDueDate = (String)mapRouteNodeInfo.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                            if (strRotueDueDate != null && strRotueDueDate.equals(strTaskDueDate)) {
                                mlLastTasks.add(mapRouteNodeInfo);
                            }
                            else {
                                mlNonLastTasks.add(mapRouteNodeInfo);
                            }
                        }

                        // Process last tasks to decide if RED status is to be shown
                        dtDueDate = eMatrixDateFormat.getJavaDate(strRotueDueDate);
                        for (Iterator itrLastTasks = mlLastTasks.iterator(); itrLastTasks.hasNext();) {
                            mapRouteNodeInfo = (Map)itrLastTasks.next();
                            strTaskCompletionDate = (String)mapRouteNodeInfo.get(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);
                            // If this task is not completed
                            if (strTaskCompletionDate == null || "".equals(strTaskCompletionDate)) {
                                dtCompletionDate = dtCurrentDate;
                            }
                            else {
                                dtCompletionDate = eMatrixDateFormat.getJavaDate(strTaskCompletionDate);
                            }


                            if (dtCompletionDate != null && dtCompletionDate.after(dtDueDate)) {
                                statusColor = RED;
                                break;
                            }
                        }

                        // Process non-last tasks to decide if YELLOW status is to be shown
                        if (statusColor == null) {
                            for (Iterator itrNonLastTasks = mlNonLastTasks.iterator(); itrNonLastTasks.hasNext();) {
                                mapRouteNodeInfo = (Map)itrNonLastTasks.next();
                                strTaskDueDate = (String)mapRouteNodeInfo.get(SELECT_ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
                                strTaskCompletionDate = (String)mapRouteNodeInfo.get(SELECT_ATTRIBUTE_ACTUAL_COMPLETION_DATE);

                                dtDueDate = eMatrixDateFormat.getJavaDate(strTaskDueDate);
                                // If this task is not completed
                                if (strTaskCompletionDate == null || "".equals(strTaskCompletionDate)) {
                                    dtCompletionDate = dtCurrentDate;
                                }
                                else {
                                    dtCompletionDate = eMatrixDateFormat.getJavaDate(strTaskCompletionDate);
                                }


                                if (dtCompletionDate != null && dtCompletionDate.after(dtDueDate)) {
                                    statusColor = YELLOW;
                                    break;
                                }
                            }
                        }

                        // If no yellow and red then it is green
                        if (statusColor == null) {
                            statusColor = GREEN;
                        }
                    }
                }

                String statusImageString = "";
                if(RED.equals(statusColor)) {
//XSSOK
                    statusImageString = "<img border=\"0\" src=\"../common/images/iconStatusRed.gif\" name=\"red\" id=\"red\" alt=\""+sStatus+"\" title=\""+sStatus+"\"></img>";
                }
                else if(YELLOW.equals(statusColor)) {
//XSSOK
                    statusImageString = "<img border=\"0\" src=\"../common/images/iconStatusYellow.gif\" name=\"yellow\" id=\"yellow\" alt=\""+sStatus+"\" title=\""+sStatus+"\"></img>";
                }
                else if(GREEN.equals(statusColor)) {
//XSSOK
                    statusImageString = "<img border=\"0\" src=\"../common/images/iconStatusGreen.gif\" name=\"green\" id=\"green\" alt=\""+sStatus+"\" title=\""+sStatus+"\"></img>";
                }

                vecResult.add(statusImageString);
            }// for each route
            //XSSOK
            return vecResult;
        }
        catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * getScheduledCompletionDate - get the route scheduled completion date that needs to be displayed in the column of the Route Summary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public Vector getScheduledCompletionDate(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");

            Vector enableCheckbox = new Vector();
            StringBuffer selectScheduledDate = new StringBuffer();
            selectScheduledDate.append("from[");
            selectScheduledDate.append(DomainObject.RELATIONSHIP_ROUTE_NODE);
            selectScheduledDate.append("].attribute[");
            selectScheduledDate.append(DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
            selectScheduledDate.append("]");

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                String routeId = (String) objectMap.get(Route.SELECT_ID);

                DomainObject routeObject = new DomainObject(routeId);
                StringList dateList = routeObject.getInfoList(context, selectScheduledDate.toString());

                MapList dateMapList = new MapList();

                Iterator dateListItr = dateList.iterator();
                while(dateListItr.hasNext())
                {
                    String schDate = (String) dateListItr.next();
                    HashMap dateMap = new HashMap();
                    dateMap.put("date", schDate);
                    dateMapList.add(dateMap);
                }
                dateMapList.sort("date", "descending", "date");

                String displayDate = "";
                Iterator dateMapListItr = dateMapList.iterator();
                while(dateMapListItr.hasNext())
                {
                    Map tempMap = (Map) dateMapListItr.next();
                    displayDate = (String) tempMap.get("date");
                    break;
                }
                enableCheckbox.add(displayDate);
            }
            return enableCheckbox;
        }
        catch (Exception ex)
        {

            throw ex;
        }
    }

    /**
     * getActualCompletionDate - get the route actual completion date that needs to be displayed in the column of the Route Summary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public Vector getActualCompletionDate(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Vector enableCheckbox = new Vector();

            MapList busObjwsl = null;

            StringList strList = new StringList(2);
            strList.addElement(Route.SELECT_CURRENT);
            strList.addElement(Route.SELECT_ROUTE_COMPLETED_DATE);

            if ( objectList != null)
            {
               String compDate = "";
               String routeState = "";

                String objIdArray[] = new String[objectList.size()];
               //Get the array of Object Ids to be paased into the methods
                for (int i = 0; i < objectList.size(); i++)
                {
                    Map objMap = (Map)objectList.get(i);
                    objIdArray[i]  = (String)objMap.get(Route.SELECT_ID);
                }
                busObjwsl=DomainObject.getInfo(context,objIdArray,strList);
                for (int i = 0; i < objectList.size(); i++)
                {

                    Map objectMap = (Map) objectList.get(i);

                    routeState = (String)((Map)(busObjwsl.get(i))).get(Route.SELECT_CURRENT);
                    if(routeState.equals(Route.STATE_ROUTE_COMPLETE))
                    {
                        compDate=(String)((Map)(busObjwsl.get(i))).get(Route.SELECT_ROUTE_COMPLETED_DATE);
                    }

                    enableCheckbox.add(compDate);
                }
            }
            return enableCheckbox;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
     /**
   * Gets the list of all Routes.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - objectList MapList
   * @returns Object
   * @throws Exception if the operation fails
   * @since Common 10.0.1.1
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getAllMyDeskRoutes(Context context, String[] args)
      throws Exception, MatrixException
  {
      try
      {
          return getMyDeskRoutes(context,args,"");
      }
      catch (Exception ex)
      {
          throw ex;
      }
  }

  /**
   * Gets the list of Active Routes
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - objectList MapList
   * @returns Object
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.1
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getMyDeskActiveRoutes(Context context, String[] args)
      throws Exception, MatrixException
  {
      try
      {
          StringBuffer sbWhere = new StringBuffer(64);
          sbWhere.append(sbRouteStatus.toString());
          sbWhere.append(strMatchList);

          if ((activeFilter != null) &&
              (!"".equals(activeFilter.trim())))
          {
              StringTokenizer tokenizer=new StringTokenizer(activeFilter,",");
              boolean first = true;
              while(tokenizer.hasMoreTokens())
              {
                  String nextFilter = tokenizer.nextToken();
                  if (first)
                  {
                    sbWhere.append("'");
                    first = false;
                  }
                  else
                  {
                    sbWhere.append(",");
                  }

                  sbWhere.append(nextFilter);
              }
              sbWhere.append("'','");
          }

          return getMyDeskRoutes(context,args,sbWhere.toString());
      }
      catch (Exception ex)
      {
          throw ex;
      }
  }

  /**
   * Gets the list of Inactive Routes
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - objectList MapList
   * @returns Object
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.1
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getMyDeskInActiveRoutes(Context context, String[] args)
      throws Exception, MatrixException
  {
      try
      {
          StringBuffer sbWhere = new StringBuffer(64);
          sbWhere.append(sbRouteStatus.toString());
          sbWhere.append(strMatchList);

          if ((inActiveFilter != null) &&
              (!"".equals(inActiveFilter.trim())))
          {
              StringTokenizer tokenizer=new StringTokenizer(inActiveFilter,",");
              boolean first = true;
              while(tokenizer.hasMoreTokens())
              {
                  String nextFilter = tokenizer.nextToken();
                  if (first)
                  {
                    sbWhere.append("'");
                    first = false;
                  }
                  else
                  {
                    sbWhere.append(",");
                  }

                  sbWhere.append(nextFilter);
              }
              sbWhere.append("'','");
          }

          return getMyDeskRoutes(context,args,sbWhere.toString());
      }
      catch (Exception ex)
      {
          throw ex;
      }
  }

    /**
     * getMyDeskRoutes - gets the list of Routes to be shown under MyDesk
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public Object getMyDeskRoutes(Context context, String[] args, String WhereExpression)
        throws Exception
    {
        try
        {
            String loggedInUser = com.matrixone.apps.common.Person.getPerson(context).getName(context);

            MapList totalResultList = new MapList();

            StringList typeSelects = new StringList(4);
            typeSelects.add(DomainObject.SELECT_ID);
            typeSelects.add(selParentRouteOwner.toString());
            typeSelects.add(attRouteVisibility.toString());
            typeSelects.add(attRestrictMembers.toString());

            boolean isRouteVisibility      = true;
            String isRouteVisibilityEnabled = EnoviaResourceBundle.getProperty(context,"emxFramework.Routes.RouteVisibility");
            if ((isRouteVisibilityEnabled == null) ||
                ("false".equals(isRouteVisibilityEnabled)))
              isRouteVisibility = false;

            Pattern relPattern = new Pattern(DomainObject.RELATIONSHIP_PROJECT_ROUTE);
            if (isRouteVisibility)
              relPattern.addPattern(DomainObject.RELATIONSHIP_ROUTE_NODE);

            com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
            MapList routeList = person.getRelatedObjects(context,
                                       relPattern.getPattern(),
                                                    DomainObject.TYPE_ROUTE,
                                                    typeSelects,
                                                    null,
                                                    true,
                                                    false,
                                                    (short)1,
                                                    WhereExpression,
                                                    "",
                                                    null,
                                                    null,
                                                    null);

            StringList sRouteList = new StringList();
            Iterator mapItr = routeList.iterator();
            String sRouteId = null;
            while(mapItr.hasNext())
            {
                Map route = (Map)mapItr.next();
                sRouteId = (String)route.get(DomainObject.SELECT_ID);
                if (!sRouteList.contains(sRouteId))
                {
                  sRouteList.add(sRouteId);
                  String viewSubRoute = (String)route.get(attRouteVisibility.toString());
                  if ((viewSubRoute != null) &&
                      (viewSubRoute.equals("No")))
                  {
                     String parentRouteOwner = (String)route.get(selParentRouteOwner.toString());
                     if ((parentRouteOwner == null) ||
                         ("".equals(parentRouteOwner)))
                     {
                       totalResultList.add(route);
                     }
                     else if ((parentRouteOwner != null) &&
                              (!(loggedInUser.equals(parentRouteOwner))))
                     {
                       totalResultList.add(route);
                     }
                  }
                  else
                  {
                    totalResultList.add(route);
                  }
                }
            }
            return totalResultList;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    /**
     * showOwner - displays the owner with lastname,firstname format
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public Vector showOwner(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Vector vecOwner = new Vector();
            MapList busObjwsl = null;

            StringList strList = new StringList(1);
            strList.addElement(Route.SELECT_OWNER);
            if ( objectList != null)
            {
                String objIdArray[] = new String[objectList.size()];
               //Get the array of Object Ids to be paased into the methods
                for (int i = 0; i < objectList.size(); i++)
                {
                    Map objMap = (Map)objectList.get(i);
                    objIdArray[i]  = (String)objMap.get(Route.SELECT_ID);
                }
                busObjwsl=DomainObject.getInfo(context,objIdArray,strList);
                for (int i = 0; i < objectList.size(); i++)
                {
                    String strOwner = com.matrixone.apps.common.Person.getDisplayName(context,(String)((Map)(busObjwsl.get(i))).get(Route.SELECT_OWNER));
                    vecOwner.add(strOwner);
                }
            }
            return vecOwner;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }


  /**
  * Grants the Access to Route to Route Member when Connected thru Route Node rel.
  *
  * @param context the eMatrix Context object
  * @param holds the grantee name
  * @return void
  * @throws Exception if the operation fails
  * @since AEF Rossini
  */
  public void grantAccess(matrix.db.Context context, String[] args) throws Exception
  {
      // Don't grant access for Route Templeates.
      /*if(getType(context).equals(TYPE_ROUTE_TEMPLATE))
      {
          return;
      }*/

      String sToType = args[1];
      // Don't grant access if the "To" side type is "Route Task User"
      // "Route Task User" exists on the "To" side if a route is
      // assigned to a Group or Role
      if(sToType.equals(TYPE_ROUTE_TASK_USER))
      {
          return;
      }

      String sGrantee = args[0];
      Access access = null;
      //AccessList accessList = null;
      if(getType(context).equals(TYPE_ROUTE_TEMPLATE) &&  getAccessForGrantee(context,sGrantee) != null && (getAccessForGrantee(context,sGrantee)).size() > 0)
          return;
      // Construct grantee list
      StringList sGranteeList = new StringList();
      sGranteeList.add(sGrantee);

      BusinessObjectList busRouteList = new BusinessObjectList();
      busRouteList.addElement(this);

      //<Fix 372839>
      pushContextForGrantor(context, AEF_ROUTE_ACCESS_GRANTOR_USERNAME);
      //</Fix 372839>
      try
      {
          // Grant the access for the business Object List to grantee (Project Lead)
          AccessUtil accessUtil = new AccessUtil();
          //accessUtil.setReadWrite(sGrantee);
          accessUtil.setRead(sGrantee);
          access = (Access)((accessUtil.getAccessList()).elementAt(0));
          matrix.db.BusinessObject.grantAccessRights(context, busRouteList, access);
      }
      catch(Exception exp)
      {
          ContextUtil.popContext(context);
          throw exp;
      }
      ContextUtil.popContext(context);
  }


  /**
  * Revoke Access from Route Member when Route Node disconnected
  *
  * @param context the eMatrix Context object
  * @param holds the grantee name
  * @return void
  * @throws Exception if the operation fails
  * @since AEF Rossini
  */
  public void revokeAccess(matrix.db.Context context, String[] args) throws Exception
  {
     /* if(getType(context).equals(TYPE_ROUTE_TEMPLATE))
      {
          return;
      }*/

      ContextUtil.restoreContext(context);
      String sToType = args[1];
      // Don't revoke access if the "To" side type is "Route Task User"
      // "Route Task User" exists on the "To" side if a route is
      // assigned to a Group or Role
      if(sToType.equals(TYPE_ROUTE_TASK_USER))
      {
          return;
      }

      // Person Disconnected from Route Node
      String sGrantee = args[0];

      revokeAccess(context, AEF_ROUTE_DELEGATION_GRANTOR_USERNAME, sGrantee);

      StringBuffer selPerson = new StringBuffer();
      selPerson.append("from[");
      selPerson.append(RELATIONSHIP_ROUTE_NODE );
      selPerson.append("].to.name");

      // check if
      // Do not revoke access for User on Route if User has more than 1 tasks.
      StringList sPersonList = getInfoList(context, selPerson.toString());
      if(sPersonList.indexOf(sGrantee) != sPersonList.lastIndexOf(sGrantee))
      {
          return;
      }

      // Revoke access from Grantee granted by Access Grantor.
      //<Fix 372839>
      Route route = new Route(getId());
      route.revokeAccessOnContent(context, new String[]{args[0]});
      revokeAccess(context, AEF_ROUTE_ACCESS_GRANTOR_USERNAME, sGrantee);
      //</Fix 372839>
  }
    /**
  * Inherit Access only for Workspace Leads from the Route Scope Object
  *
  * @param context the eMatrix Context object
  * @param holds Route Objects
  * @return void
  * @throws Exception if the operation fails
  * @since AEF Rossini
  */
  public void inheritAccesstoContent(matrix.db.Context context, String[] args) throws Exception
  {
      Access access = null;
      AccessUtil accessUtil = null;
      // Route object'b BO list
      BusinessObjectList listRoute = new BusinessObjectList();
      for (int i =0 ;i< args.length ;i++ )
      {
         listRoute.addElement(new BusinessObject(args[i]));
      }
      //<Fix 372839>
      AccessList accessGrantorList = getAccessForGrantor(context, AEF_ROUTE_ACCESS_GRANTOR_USERNAME);
      //</Fix 372839>
        if(accessGrantorList.size() > 0)
        {
          //<Fix 372839>
          pushContextForGrantor(context, AEF_ROUTE_ACCESS_GRANTOR_USERNAME);
          //<Fix 372839>
          try
          {
              grantAccessRights(context, listRoute, accessGrantorList);
          }
          catch(Exception exp)
          {
              ContextUtil.popContext(context);
              throw exp;
          }
          ContextUtil.popContext(context);
        }
  }
  /**
  * Inherit Access only for Workspace Leads from the Route Scope Object
  *
  * @param context the eMatrix Context object
  * @param holds Route Objects
  * @return void
  * @throws Exception if the operation fails
  * @since AEF Rossini
  */
  public void inheritAccess(matrix.db.Context context, String[] args) throws Exception
  {
      Access access = null;
      AccessUtil accessUtil = null;

      // Route object'b BO list
      BusinessObjectList listRoute = new BusinessObjectList();
      listRoute.addElement(new BusinessObject(args[0]));

      // Get the Access List of the Route Scope Object granted by Lead Grantor.
      AccessItr itrAccess = new AccessItr(getAccessForGrantor(context, AEF_WORKSPACE_LEAD_GRANTOR_USERNAME));

      AccessList listGrantAccess = new AccessList();
      while (itrAccess.next())
      {
          // Set READ access for Workspace Leads to the Route.
          accessUtil = new AccessUtil();
          accessUtil.setRead(((Access)itrAccess.obj()).getUser());
          access = (Access)((accessUtil.getAccessList()).elementAt(0));
          listGrantAccess.addElement(access);
      }

      if (listGrantAccess.size() > 0 )
      {
          pushContextForGrantor(context, AEF_WORKSPACE_LEAD_GRANTOR_USERNAME);
          try
          {
              grantAccessRights(context, listRoute, listGrantAccess);
          }
          catch(Exception exp)
          {
              ContextUtil.popContext(context);
              throw exp;
          }
          ContextUtil.popContext(context);
      }
  }

  /**
  * Update Route Node ID attribute on Route Node relationship
  * when Connected thru Route Node rel.
  *
  * @param context the eMatrix Context object
  * @param holds the grantee name
  * @return void
  * @throws Exception if the operation fails
  * @since AEF Rossini
  */
  public void populateRouteNodeId(matrix.db.Context context, String[] args) throws Exception
  {
      // Get Route Node ID
      String sRouteNodeId = args[0];
      sRouteNodeId = MqlUtil.mqlCommand(context, "print connection $1 select $2 dump", sRouteNodeId, "physicalid");
      try
      {
          // update attribute Route Node ID on passed in relationship to it's id
          AttributeList attList = new AttributeList();
          Attribute attr = new Attribute( new AttributeType (ATTRIBUTE_ROUTE_NODE_ID), sRouteNodeId);
          attList.addElement(attr);
          Relationship rel = new Relationship(sRouteNodeId);
          rel.open(context);
          rel.setAttributes(context, attList);
          rel.update(context);
          rel.close(context);
      } catch(Exception exp)
      {
          throw exp;
      }
  }

  public void deleteRouteTaskUserObject(matrix.db.Context context, String[] args) throws Exception
  {
      //Continue only if the TO side of the route node relation is a "Route Task User" object
      if(getType(context).equals(TYPE_PERSON))
      {
          return;
      }

      //if the Route Task User object does not have Route Node relationship then delete it
      //which means that this was the last group or role assignment.
      //Either the group or role assignment is removed by the route owner OR
      //the user who belongs to the assigned group or role has accepted the task
      if(hasObjects(context, RELATIONSHIP_ROUTE_NODE, false))
      {
          //other group or role assignments exists. Do nothing
          return;
      }
      else
      {
          try
          {
              //No more group or role assignments. Delete the Route Task User object
              deleteObject(context);
          }
          catch(Exception exp)
          {
              throw exp;
          }
      }
  }

  /**
  * Push the context for the respective grantor.
  *
  * @param context the eMatrix Context object
  * @param grantor name to push context to.
  * @return void
  * @throws Exception if the operation fails
  * @since VCRossini
  */
  protected void pushContextForGrantor(matrix.db.Context context, String sGrantor) throws Exception
  {
      // Check for grantor.
      if(sGrantor.equals(AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME))
      {
          pushContextAccessGrantor(context);
      }
      else if (sGrantor.equals(AEF_WORKSPACE_MEMBER_GRANTOR_USERNAME))
      {
          pushContextMemberGrantor(context);
      }
      else if (sGrantor.equals(AEF_WORKSPACE_LEAD_GRANTOR_USERNAME))
      {
          pushContextLeadGrantor(context);
      }
      else if (sGrantor.equals(AEF_ROUTE_DELEGATION_GRANTOR_USERNAME))
      {
          pushContextDelegationGrantor(context);
      }
      //<Fix 372839>
      else if(sGrantor.equals(AEF_ROUTE_ACCESS_GRANTOR_USERNAME))
      {
          pushContextRouteAccessGrantor(context);
      }
      //</Fix 372839>
  }

  /**
  * Change context to Workspace Access Grantor
  *
  * @param context the eMatrix Context object
  * @return void
  * @throws Exception if the operation fails
  * @since VCRossini
  */
  protected void pushContextAccessGrantor(Context context) throws Exception
  {
      ContextUtil.pushContext(context, AEF_WORKSPACE_ACCESS_GRANTOR_USERNAME, null, null);
  }

  /**
  * Change context to Route Delegation Grantor
  *
  * @param context the eMatrix Context object
  * @return void
  * @throws Exception if the operation fails
  * @since VCRossini
  */
  protected void pushContextDelegationGrantor(Context context) throws Exception
  {
      ContextUtil.pushContext(context, AEF_ROUTE_DELEGATION_GRANTOR_USERNAME, null, null);
  }

  /**
  * Change context to Workspace Member Grantor
  *
  * @param context the eMatrix Context object
  * @return void
  * @throws Exception if the operation fails
  * @since VCRossini
  */
  protected void pushContextMemberGrantor(Context context) throws Exception
  {
      // Puch context to super user to turn off triggers
      ContextUtil.pushContext(context);
      try
      {
        // Turn off all triggers
        MqlUtil.mqlCommand(context, "trigger off;", true);
      }
      catch (Exception exp)
      {
        ContextUtil.popContext(context);
        throw exp;
      }

      ContextUtil.pushContext(context, AEF_WORKSPACE_MEMBER_GRANTOR_USERNAME, null, null );
  }

  /**
  * Change context to Workspace Lead Grantor
  *
  * @param context the eMatrix Context object
  * @return void
  * @throws Exception if the operation fails
  * @since VCRossini
  */
  protected void pushContextLeadGrantor(Context context) throws Exception
  {
      ContextUtil.pushContext(context, AEF_WORKSPACE_LEAD_GRANTOR_USERNAME, null, null);
  }
  //<Fix 372839>
  protected void pushContextRouteAccessGrantor(Context context) throws Exception
  {
      ContextUtil.pushContext(context, AEF_ROUTE_ACCESS_GRANTOR_USERNAME, null, null);
  }
  //</Fix 372839>

  /**
  * Revoke access on Object b/w particular Grantor and Grantee
  *
  * @param context the eMatrix Context object
  * @param grantor name to push context to.
  * @return void
  * @throws Exception if the operation fails
  * @since VCRossini
  */
  protected void revokeAccessGrantorGrantee(matrix.db.Context context, String sGrantor, String sGrantee) throws Exception
  {
      StringBuffer sCommand = new StringBuffer();
      sCommand.append("mod bus ");
      sCommand.append(getId());
      sCommand.append(" revoke grantor '");
      sCommand.append(sGrantor);
      sCommand.append("' grantee '");
      sCommand.append(sGrantee);
      sCommand.append("'");
      MqlUtil.mqlCommand(context,sCommand.toString());
  }

  /**
  * displayLinkAccessCheck - determines if the Create New, Create Route Wizard ,Set Task Escalation, Remove Selected, Start/ResumeRoute links needs to be show in the Route Summary table
  *
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the objectId
  * @returns boolean type
  * @throws Exception if the operation fails
  * @Modified:  AEF V6R2014 
  * After Team Central Security Context highlight, now we are checking if the member is having 'Add Remove' access then he can perform these operations
  */

  public static boolean displayLinkAccessCheck(Context context, String args[]) throws Exception
  {
      HashMap programMap         = (HashMap) JPO.unpackArgs(args);
      String objectId            = (String) programMap.get("objectId");

      boolean result             = true;

      if (objectId == null || "".equals(objectId)) {
         // happens when this JPO invoked for MyRouteSummary
         return true;
      }

      DomainObject objectGeneral = DomainObject.newInstance(context);

      try {
    	  // set the domain object with the passed id , id can be of Workspcae/WorkspcaVault/Document.
          objectGeneral.setId(objectId);

          String objState = objectGeneral.getInfo(context,DomainConstants.SELECT_CURRENT);
          Access access = objectGeneral.getAccessMask(context);

          if(AccessUtil.hasAddRemoveAccess(access) && objState.equals("Active")) {
              result=true;
          } else {
              result=false;
          }
      } catch(Exception e )  {
        throw e;
      }
      
      return result;
  }

  /**
  * displaySetTaskLinkAccessCheck - determines if the Set Task Escalation links needs to be show in the Route Summary table
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the objectId
  * @returns boolean type
  * @throws Exception if the operation fails
  * @since AEF Rossini
  */

  public static boolean displaySetTaskLinkAccessCheck(Context context, String args[]) throws Exception
  {
         HashMap programMap         = (HashMap) JPO.unpackArgs(args);
         DomainObject objectGeneral = DomainObject.newInstance(context);
         String objectId            = (String) programMap.get("objectId");

         String sProjectId          = "";
         String sPassedType         = "";
         String sProjectVaultId     = "";
         boolean result             = true;

        StringBuffer selWorkspaceVaults = new StringBuffer();
        selWorkspaceVaults.append("to[");
        selWorkspaceVaults.append(objectGeneral.RELATIONSHIP_WORKSPACE_VAULTS);
        selWorkspaceVaults.append("].from.id");

        StringBuffer selVaultedDoc = new StringBuffer();
        selVaultedDoc.append("to[");
        selVaultedDoc.append(objectGeneral.RELATIONSHIP_VAULTED_DOCUMENTS);
        selVaultedDoc.append("].from.id");


         if (objectId == null || "".equals(objectId))
         {
            // happens when this JPO invoked for MyRouteSummary
            return true;
         }
         // set the domain object with the passed id , id can be of Workspcae/WorkspcaVault/Document.
         objectGeneral.setId(objectId);
         // get the object type name
         sPassedType= objectGeneral.getType(context);
         if (sPassedType.equals(objectGeneral.TYPE_PROJECT))
         {
             sProjectId = objectId;
         }
         else if(sPassedType.equals(objectGeneral.TYPE_PROJECT_VAULT))
         {
             WorkspaceVault workspaceVault = new WorkspaceVault(objectId);

             StringList objsel = new StringList();
             objsel.add(workspaceVault.SELECT_ID);

             Map topVaultMap   = workspaceVault.getTopLevelVault(context , objsel);
             String topVaultId = (String)topVaultMap.get(workspaceVault.SELECT_ID);

             workspaceVault.setId(topVaultId);

             sProjectId = workspaceVault.getInfo(context , selWorkspaceVaults.toString());

         }
         else if(sPassedType.equals(objectGeneral.TYPE_DOCUMENT) || sPassedType.equals(objectGeneral.TYPE_PACKAGE) || sPassedType.equals(objectGeneral.TYPE_RTS_QUOTATION) || sPassedType.equals(objectGeneral.TYPE_REQUEST_TO_SUPPLIER))
         {
             // get the projectvault id of the passed document.
             sProjectVaultId               = objectGeneral.getInfo(context, selVaultedDoc.toString());

             if(sProjectVaultId != null && !"".equals(sProjectVaultId))
             {
                 WorkspaceVault workspaceVault = new WorkspaceVault(sProjectVaultId);

                 StringList objsel   = new StringList();
                 objsel.add(workspaceVault.SELECT_ID);

                 Map topVaultMap   = workspaceVault.getTopLevelVault(context , objsel);
                 String topVaultId = (String)topVaultMap.get(objectGeneral.SELECT_ID);
                 workspaceVault.setId(topVaultId);

                 sProjectId = workspaceVault.getInfo(context ,selWorkspaceVaults.toString());
             }
             else if(sPassedType.equals(objectGeneral.TYPE_DOCUMENT))
             {
                 WorkspaceVault wsVault = (WorkspaceVault)DomainObject.newInstance(context,objectGeneral.TYPE_WORKSPACE_VAULT);
                 Document doc = (Document)DomainObject.newInstance(context,objectId);
                 sProjectId = doc.getWorkspaceId(context);
                 DomainObject domProj = DomainObject.newInstance(context,sProjectId);
                 if((domProj.TYPE_WORKSPACE_VAULT).equals(domProj.getType(context)))
                 {
                     wsVault.setId(sProjectId);
                     Map wksVaultMap = wsVault.getTopLevelVault(context, new StringList(domProj.SELECT_ID));
                     sProjectId = (String)wksVaultMap.get(domProj.SELECT_ID);
                     wsVault.setId(sProjectId);
                     sProjectId = wsVault.getInfo(context,selWorkspaceVaults.toString());
                 }
             }

         }
         else if(sPassedType.equals(objectGeneral.TYPE_INBOX_TASK))
         {
            StringBuffer selectWorkspaceID = new StringBuffer();
            selectWorkspaceID.append("from[");
            selectWorkspaceID.append(DomainObject.RELATIONSHIP_ROUTE_TASK);
            selectWorkspaceID.append("].to.to[");
            selectWorkspaceID.append(DomainObject.RELATIONSHIP_ROUTE_SCOPE);
            selectWorkspaceID.append("].from.id");

             String prjId              =objectGeneral.getInfo(context,selectWorkspaceID.toString());

             DomainObject wkspaceObject= DomainObject.newInstance(context, prjId);

             if(wkspaceObject.getType(context).equals(DomainObject.TYPE_PROJECT))
             {
                 sProjectId=prjId;
             }
             else
             {
                 WorkspaceVault workspaceVault     =(WorkspaceVault) DomainObject.newInstance(context, prjId);
                 StringList objsel     = new StringList();
                 objsel.add(workspaceVault.SELECT_ID);

                 Map topVaultMap       = workspaceVault.getTopLevelVault(context , objsel);
                 String topVaultId     = (String)topVaultMap.get(workspaceVault.SELECT_ID);

                 workspaceVault.setId(topVaultId);
                 sProjectId            = workspaceVault.getInfo(context , selWorkspaceVaults.toString());
             }
         }

         try
         {
             String workspaceId     = sProjectId;
             Map map                = null;
             Iterator memberItr     = null;
             String sCreateRoute    = null;
             String sWorkspaceAccess= null;

             DomainObject BaseObject = DomainObject.newInstance(context);
             BaseObject.setId(workspaceId);

             String objState = BaseObject.getInfo(context,DomainConstants.SELECT_CURRENT);

             com.matrixone.apps.common.Person person = null;
             person = person.getPerson(context);
             StringList objectSelects    = new StringList();

             StringBuffer sAttSelCreateRoute   = new StringBuffer();
             sAttSelCreateRoute.append("attribute[");
             sAttSelCreateRoute.append(DomainObject.ATTRIBUTE_CREATE_ROUTE);
             sAttSelCreateRoute.append("].value");

            StringBuffer sAttSelProjectAccess   = new StringBuffer();
             sAttSelProjectAccess.append("attribute[");
             sAttSelProjectAccess.append(DomainObject.ATTRIBUTE_PROJECT_ACCESS);
             sAttSelProjectAccess.append("].value");

            StringBuffer sSelWorkspaceId   = new StringBuffer();
             sSelWorkspaceId.append( "to[");
             sSelWorkspaceId.append(BaseObject.RELATIONSHIP_PROJECT_MEMBERS);
             sSelWorkspaceId.append("].from.id");

             objectSelects.addElement(sAttSelCreateRoute.toString());
             objectSelects.addElement(sAttSelProjectAccess.toString());
             objectSelects.addElement(sSelWorkspaceId.toString());
             objectSelects.addElement(BaseObject.SELECT_CURRENT);
             String objectWhere = "("+sSelWorkspaceId.toString()+" == '"+workspaceId+"')";

             Pattern typePatternWorkspace = new Pattern(DomainObject.TYPE_WORKSPACE);
             typePatternWorkspace.addPattern(BaseObject.TYPE_PROJECT_MEMBER);

             Pattern relPatternProject = new Pattern(BaseObject.RELATIONSHIP_PROJECT_MEMBERSHIP);
             relPatternProject.addPattern(BaseObject.RELATIONSHIP_PROJECT_MEMBERS);

             MapList mapList = person.getRelatedObjects(context,
                                                         relPatternProject.getPattern(),
                                                         typePatternWorkspace.getPattern(),
                                                         objectSelects,
                                                         null,
                                                         true,
                                                         true,
                                                         (short)1,
                                                         objectWhere,
                                                         "",
                                                         null,
                                                         null,
                                                         null);

             memberItr = mapList.iterator();
             while(memberItr.hasNext())
             {
                 map              = (Map) memberItr.next();
                 sCreateRoute     = (String)map.get(sAttSelCreateRoute.toString());
                 sWorkspaceAccess = (String)map.get(sAttSelProjectAccess.toString());
             }
             if(sCreateRoute != null && sCreateRoute.equals("Yes") && objState.equals("Active") && sWorkspaceAccess != null && sWorkspaceAccess.equalsIgnoreCase("Project Lead"))
             {
                 result=true;
             }
             else
             {
                 result=false;
             }
         }
         catch(Exception e )  {
           throw e;
         }
         return result;
  }



  /**
   * Get the list of Routes.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - objectList MapList
   * @param sWhere where condition
   * @returns Object
   * @throws Exception if the operation fails
   * @since Common 10.0.1.1
   */
  public Object getRoutes(Context context, String[] args, String sWhere)
      throws Exception, MatrixException
  {
      try
      {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String parentId = (String) programMap.get("objectId");

        StringBuffer routeScopeName= new StringBuffer();
        routeScopeName.append("to[");
        routeScopeName.append(Route.RELATIONSHIP_ROUTE_SCOPE);
        routeScopeName.append("].from.name");
        com.matrixone.apps.common.Person loggedInPerson = com.matrixone.apps.common.Person.getPerson(context);
        String loggedInUser       = loggedInPerson.getName(context);

        MapList totalResultList = new MapList();
        StringList typeSelects = new StringList();
        typeSelects.add(DomainObject.SELECT_ID);
        typeSelects.add(DomainObject.SELECT_DESCRIPTION);
        typeSelects.add(DomainObject.SELECT_OWNER);
        typeSelects.add(routeScopeName.toString());
        typeSelects.add(sbRouteStatus.toString());
        typeSelects.add(attRestrictMembers.toString());
        typeSelects.add(selParentRouteOwner.toString());
        typeSelects.add(attRouteVisibility.toString());

        boolean isRouteVisibility = true;

        String isRouteVisibilityEnabled = EnoviaResourceBundle.getProperty(context,"emxFramework.Routes.RouteVisibility");
        if( isRouteVisibilityEnabled == null || "false".equals(isRouteVisibilityEnabled))
        {
          isRouteVisibility = false;
        }

        if(parentId != null && !"".equals(parentId) && !"null".equals(parentId))
        {
          //Connected to an object
          totalResultList = Route.getRoutes(context,parentId, typeSelects, null, sWhere,false );
        }
        else
        {

         //MyDesk Query
         totalResultList = Route.getMyRoutes(context, typeSelects, null, isRouteVisibility,sWhere,false);

        }
        // Remove Duplicate Routes
        Hashtable routeHash = new Hashtable();
        MapList routeList = new MapList();
        Iterator mapItr = totalResultList.iterator();
        String sRouteId = null;
        while(mapItr.hasNext())
        {
          Map routeMap = (Map)mapItr.next();
          sRouteId = (String)routeMap.get(Route.SELECT_ID);
          String routeOwner = (String)routeMap.get(Route.SELECT_OWNER);
          String parentRouteOwner = (String)routeMap.get(selParentRouteOwner.toString());
          if (!routeHash.containsKey(sRouteId))
          {
           if((routeOwner != null && routeOwner.equals(loggedInUser)) || (parentRouteOwner != null && parentRouteOwner.equals(loggedInUser)))
            {
                String viewSubRoute = (String)routeMap.get(attRouteVisibility.toString());
                if(viewSubRoute != null && viewSubRoute.equals("No"))
                {
                    if(parentRouteOwner == null || "".equals(parentRouteOwner))
                    {
                       routeList.add(routeMap);
                    }
                    else if(parentRouteOwner != null && !(loggedInUser.equals(parentRouteOwner)))
                    {
                       routeList.add(routeMap);
                    }
                }
                else
                {
                   routeList.add(routeMap);
                }
            routeHash.put(sRouteId, "");
            }
//added for 308049
            else
            {
                routeList.add(routeMap);
                routeHash.put(sRouteId, "");
            }
 //till here
           // routeList.add(route);
          }
        }
        totalResultList = routeList;

        return totalResultList;
      }
      catch (Exception ex)
      {
        throw ex;
      }
  }


  /**
   * Gets the list of all Routes.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - objectList MapList
   * @returns Object
   * @throws Exception if the operation fails
   * @since Common 10.0.1.1
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getAllRoutes(Context context, String[] args)
      throws Exception, MatrixException
  {
      try
      {
          return getRoutes(context,args,"");
      }
      catch (Exception ex)
      {
          throw ex;
      }
  }

  /**
   * Gets the list of Inactive Routes
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - objectList MapList
   * @returns Object
   * @throws Exception if the operation fails
   * @since AEF 10.0.1.1
   */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getActiveRoutes(Context context, String[] args)
      throws Exception, MatrixException
  {
      try
      {
          String sWhere="";
          if(activeFilter!=null && !"".equals(activeFilter.trim()) )
          {
              StringTokenizer tokenizer=new StringTokenizer(activeFilter,",");
              while(tokenizer.hasMoreTokens())
              {
                  String nextFilter=tokenizer.nextToken();
                  if(!"".equals(sWhere))
                  {
                      sWhere += "||";
                  }
                  sWhere += sbRouteStatus.toString() + " == \"" + nextFilter + "\"";
              }
          }

          return getRoutes(context,args,sWhere);
      }
      catch (Exception ex)
      {
          throw ex;
      }
  }

 /**
  * Gets the list of Active Routes.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the following input arguments:
  *        0 - objectList MapList
  * @returns Object
  * @throws Exception if the operation fails
  * @since Common 10.0.1.1
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getInActiveRoutes(Context context, String[] args)
      throws Exception, MatrixException
  {
       try
       {
            String sWhere="";
            if(inActiveFilter != null && !"".equals(inActiveFilter.trim()))
            {
                StringTokenizer tokenizer=new StringTokenizer(inActiveFilter,",");
                while(tokenizer.hasMoreTokens())
                {
                    String nextFilter=tokenizer.nextToken();
                    if(!"".equals(sWhere))
                    {
                        sWhere += "||";
                    }
                    sWhere += sbRouteStatus.toString() + " == \"" + nextFilter + "\"";
                }
            }
            return getRoutes(context,args,sWhere);
       }
       catch (Exception ex)
       {
           throw ex;
       }
    }

    public static boolean emxCheckCreateRoutesAccess(Context context, String args[]) throws Exception
    {

        HashMap programMap         = (HashMap) JPO.unpackArgs(args);
        DomainObject objectGeneral = DomainObject.newInstance(context);
        String objectId            = (String) programMap.get("objectId");

        String loggedInUser       = context.getUser();
        Access access = null;
        if (objectId == null || "".equals(objectId) || "null".equals(objectId))
        {
           return true;
        }

        if (isRoutedObjectInLastState(context, args)) {
            return false;
        }
        
        objectGeneral.setId(objectId);
        StringList grantorList = Route.getGranteeGrantor(context, objectId);
        if(grantorList.contains(AEF_COMMON_ACCESS_GRANTOR_USERNAME)) {
            access = objectGeneral.getAccessForGranteeGrantor(context, loggedInUser, AEF_COMMON_ACCESS_GRANTOR_USERNAME);
        } else if(grantorList.contains(AEF_ROUTE_ACCESS_GRANTOR_USERNAME)){
            access = objectGeneral.getAccessForGranteeGrantor(context, loggedInUser, AEF_ROUTE_ACCESS_GRANTOR_USERNAME);
        } else {
            access = objectGeneral.getAccessMask(context);
           }
        
        if (objectGeneral.isKindOf(context,PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_type_DOCUMENTS))){
        	// With Team central security context HL, anyone having 'Add Remove' access can create Route
        	return AccessUtil.hasReadAccess(access);
        } else {
        	return access.hasReadAccess();
        }
        
    }

    public Vector getScope(Context context,String[] args) throws Exception
    {
        Vector AvailabilityList = new Vector();

        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);


            Map paramList      = (Map)programMap.get("paramList");
            String languageStr = (String)paramList.get("languageStr");

            MapList objectList = (MapList)programMap.get("objectList");
            StringBuffer sbAvailabilityURL = new StringBuffer();
            StringBuffer strBuf = new StringBuffer();

            boolean isPrinterFriendly = false;
            String PrinterFriendly = (String)paramList.get("reportFormat");
            if (PrinterFriendly != null ) {
               isPrinterFriendly = true;
            }

            Iterator objectListItr = objectList.iterator();
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                String restrictScope =(String)objectMap.get(attRestrictMembers.toString());
                if ((restrictScope != null )&& (!restrictScope.equals("All")) && (!restrictScope.equals("Organization")))
                {
                    //<Fix 371409>
                    //If the login user doesn't have access to the scope object
                    //get the details by pushing the context and display name without hyperlink
                    Route route = new Route((String) objectMap.get(DomainConstants.SELECT_ID));
                    boolean hasAccessToScopeObject = route.hasReadAccessToScopeObject(context, restrictScope);
                    Map scopeObjInfo = route.getScopeObjectTypeNameRevision(context, restrictScope, !hasAccessToScopeObject);

                    if(!isPrinterFriendly && hasAccessToScopeObject) {

                        sbAvailabilityURL.append("../common/emxTree.jsp?objectId=");
                        sbAvailabilityURL.append(XSSUtil.encodeForJavaScript(context, restrictScope));

                        strBuf.append("<a href='javascript:showModalDialog(\""+sbAvailabilityURL.toString()+"\",575,575)'>");
                        strBuf.append(XSSUtil.encodeForXML(context, scopeObjInfo.get(DomainConstants.SELECT_NAME).toString())).append("</a>");
                    } else
                    {
                        strBuf.append(XSSUtil.encodeForXML(context, scopeObjInfo.get(DomainConstants.SELECT_NAME).toString()));
                    }

                }
                else
                {
                    strBuf.append(i18nNow.getRangeI18NString(DomainObject.ATTRIBUTE_RESTRICT_MEMBERS,restrictScope,languageStr));
                }
                AvailabilityList.add(strBuf.toString());
                strBuf.delete(0,strBuf.length());
                sbAvailabilityURL.delete(0,sbAvailabilityURL.length());
            }

        }
        catch (Exception ex)
        {
            throw ex;
        }
        return AvailabilityList;
    }

    /**
     * gets the list of connected connect for the Route
     * Used for APPDocumentSummary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectId - parent object OID
     * @returns Object
     * @throws Exception if the operation fails
     * @since Common 10.5
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public Object getContent(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap        = (HashMap) JPO.unpackArgs(args);
            String  parentId          = (String) programMap.get("objectId");
            String  relPattern        = (String) programMap.get("parentRelName");

            if(relPattern == null || "null".equals(relPattern) || "".equals(relPattern))
            {
                relPattern = PropertyUtil.getSchemaProperty(context, CommonDocument.SYMBOLIC_relationship_ReferenceDocument);
            }
            else
            {
                relPattern = PropertyUtil.getSchemaProperty(context, relPattern);
            }

            String objectWhere = "";//CommonDocument.SELECT_IS_VERSION_OBJECT + "==\"False\"";

            DomainObject masterObject = DomainObject.newInstance(context, parentId);

            StringList typeSelects = new StringList();
            typeSelects.add(CommonDocument.SELECT_ID);
            typeSelects.add(CommonDocument.SELECT_TYPE);
            typeSelects.add(CommonDocument.SELECT_FILE_NAME);
            typeSelects.add(CommonDocument.SELECT_ACTIVE_FILE_LOCKED);
            typeSelects.add(CommonDocument.SELECT_TITLE);
            typeSelects.add(CommonDocument.SELECT_REVISION);
            typeSelects.add(CommonDocument.SELECT_NAME);
            typeSelects.add(CommonDocument.SELECT_ACTIVE_FILE_VERSION);
            typeSelects.add(CommonDocument.SELECT_HAS_ROUTE);
            typeSelects.add(CommonDocument.SELECT_FILE_NAMES_OF_ACTIVE_VERSION);
            typeSelects.add(CommonDocument.SELECT_SUSPEND_VERSIONING);
            typeSelects.add(CommonDocument.SELECT_HAS_CHECKOUT_ACCESS);
            typeSelects.add(CommonDocument.SELECT_HAS_CHECKIN_ACCESS);
            typeSelects.add(CommonDocument.SELECT_MOVE_FILES_TO_VERSION);
            typeSelects.add(Route.SELECT_POLICY);

            StringList relSelects = new StringList();
            relSelects.add(CommonDocument.SELECT_RELATIONSHIP_ID);
            relSelects.add(Route.SELECT_ROUTE_BASESTATE);
            MapList documentList = masterObject.getRelatedObjects(context,
                                                          relPattern,
                                                          "*",
                                                          typeSelects,
                                                          relSelects,
                                                          true,
                                                          false,
                                                          (short)0,
                                                          objectWhere,
                                                          null,
                                                          null,
                                                          null,
                                                          null);
            return documentList;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
    }

  /**
   * canUpload - This method is used to determine if
   *             the context user can Upload.
   * @param context the eMatrix <code>Context</code> object
   * @param args empty
   * @return boolean
   * @throws Exception if the operation fails
   * @since Common 10-5
   */
  public boolean canUpload(Context context, String[] args)
    throws Exception
  {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      String objectId    = (String) programMap.get("objectId");

      boolean bTeam = FrameworkUtil.isSuiteRegistered(context,"featureVersionTeamCentral",false,null,null);

      //commented for bug 318155

      /*boolean bProgram = FrameworkUtil.isSuiteRegistered(context,"appVersionProgramCentral",false,null,null);
      If Team or Program is not installed then cannot upload.
      if ((!bTeam) && (!bProgram))*/

      //If Team is not installed then cannot upload.
      if ((!bTeam)) {
        return false;
      }

      Route route = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
      route.setId(objectId);
      route.open(context);
      Access contextAccess = route.getAccessMask(context);
      StringList selects = new StringList(2);
      selects.addElement(route.SELECT_OWNER);
      selects.addElement(route.SELECT_CURRENT);
      Map routeMap = route.getInfo(context, selects);

      String sOwner = (String)routeMap.get(route.SELECT_OWNER);
      String sState = (String)routeMap.get(route.SELECT_CURRENT);
      route.close(context);

      boolean isRouteEditable = true;
      // Do not show links if the Route State is Complete or Archive
      if(sState.equals(Route.STATE_ROUTE_COMPLETE) || sState.equals("Archive"))
      {
         return false;
      }

      boolean showLink = false;
      // show link only to the route owner
     if ((sOwner.equals(context.getUser()) && (isRouteEditable)) || (AccessUtil.hasAddAccess(contextAccess)) )
        showLink = true;

     return showLink;
  }

  /**
   * canAddContent - This method is used to determine if
   *             the context user can Add Content to a Route.
   * @param context the eMatrix <code>Context</code> object
   * @param args empty
   * @return boolean
   * @throws Exception if the operation fails
   * @since Common 10-5
   */
  public boolean canAddContent(Context context, String[] args)
    throws Exception
  {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      String objectId    = (String) programMap.get("objectId");

      Route route = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
      route.setId(objectId);
      route.open(context);
      Access contextAccess = route.getAccessMask(context);
      StringList selects = new StringList(2);
      selects.addElement(route.SELECT_OWNER);
      selects.addElement(route.SELECT_CURRENT);
      Map routeMap = route.getInfo(context, selects);

      String sOwner = (String)routeMap.get(route.SELECT_OWNER);
      String sState = (String)routeMap.get(route.SELECT_CURRENT);
      route.close(context);

      boolean isRouteEditable = true;
      // Do not show links if the Route State is Complete or Archive
      if(sState.equals(Route.STATE_ROUTE_COMPLETE) || sState.equals("Archive"))
      {
         return false;
      }

      boolean showLink = false;
      // show link only to the route owner
     if ((sOwner.equals(context.getUser()) && (isRouteEditable)) || (AccessUtil.hasAddAccess(contextAccess)) )
        showLink = true;

     return showLink;
  }

  /**
   * canEdit - This method is used to determine if
   *           the context user can Edit the LifeCycle Blocks of a Route
   * @param context the eMatrix <code>Context</code> object
   * @param args empty
   * @return boolean
   * @throws Exception if the operation fails
   * @since Common 10-5
   */
  public boolean canEdit(Context context, String[] args)
    throws Exception
  {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      String objectId    = (String) programMap.get("objectId");

      Route route = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
      route.setId(objectId);
      route.open(context);
      StringList selects = new StringList(2);
      selects.addElement(route.SELECT_OWNER);
      selects.addElement(route.SELECT_CURRENT);
      Map routeMap = route.getInfo(context, selects);

      String sOwner = (String)routeMap.get(route.SELECT_OWNER);
      String sState = (String)routeMap.get(route.SELECT_CURRENT);
      route.close(context);

      boolean isRouteEditable = true;
      // Do not show links if the Route State is Complete or Archive
      if(sState.equals(Route.STATE_ROUTE_COMPLETE) || sState.equals("Archive"))
      {
         isRouteEditable = false;
      }

      boolean showLink = false;
      // show link only to the route owner
     if (sOwner.equals(context.getUser()) && (isRouteEditable))
        showLink = true;

     return showLink;
  }

   public Vector showStateBlock(Context context, String[] args)
        throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList      = (Map)programMap.get("paramList");
            String languageStr = (String)paramList.get("languageStr");
            Vector stateBlockList = new Vector();
            Iterator objectListItr = objectList.iterator();
            String sNoneValue = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common.None");
            String stateValue="";
            String stateBlockVal="";
            String sPolicy="";
            while(objectListItr.hasNext())
            {
                Map objectMap = (Map) objectListItr.next();
                stateBlockVal = (String) objectMap.get(Route.SELECT_ROUTE_BASESTATE);
                sPolicy=(String) objectMap.get(Route.SELECT_POLICY);

                if( ( stateBlockVal != null) && (!stateBlockVal.equals("")) )
                {
                 stateBlockVal = FrameworkUtil.lookupStateName(context,sPolicy,stateBlockVal);
           if (stateBlockVal != null && !"Ad Hoc".equals(stateBlockVal)){
                        stateValue = i18nNow.getStateI18NString(sPolicy,stateBlockVal,languageStr); //FrameworkUtil.lookupStateName(context,sPolicy,stateBlockVal);
                    } else {
                        stateValue = sNoneValue;
                   }
                }else{
                    stateValue = sNoneValue;
                }
                stateBlockList.add(stateValue);
            }
            return stateBlockList;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    public boolean canShowStateBlockColumn (Context context,String[] args) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = "";
        objectId=(String)programMap.get("objectId");
        DomainObject route = new DomainObject();
        route.setId(objectId);
        String type = route.getInfo(context,route.SELECT_TYPE);
        boolean retVal=false;
        if( (type != null) && (!type.equals("")) && (type.equals(DomainObject.TYPE_ROUTE)) )
            retVal=true;
        else
            retVal=false;
        return retVal;
    }

  /**
   * canDeleteContent - This method is used to determine if
   *             the context user can Delete Content to a Route.
   * @param context the eMatrix <code>Context</code> object
   * @param args empty
   * @return boolean
   * @throws Exception if the operation fails
   * @since Common 10-5
   */
  public boolean canDeleteContent(Context context, String[] args)
    throws Exception
  {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      String objectId    = (String) programMap.get("objectId");

      Route route = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
      route.setId(objectId);
      route.open(context);
      Access contextAccess = route.getAccessMask(context);
      StringList selects = new StringList(2);
      selects.addElement(route.SELECT_OWNER);
      selects.addElement(route.SELECT_CURRENT);
      Map routeMap = route.getInfo(context, selects);

      String sOwner = (String)routeMap.get(route.SELECT_OWNER);
      String sState = (String)routeMap.get(route.SELECT_CURRENT);
      route.close(context);

      boolean isRouteEditable = true;
      // Do not show links if the Route State is Complete or Archive
      if(sState.equals(Route.STATE_ROUTE_COMPLETE) || sState.equals("Archive"))
      {
         return false;
      }

      boolean showLink = false;
      // show link only to the route owner
     if ((sOwner.equals(context.getUser()) && (isRouteEditable)) || (AccessUtil.hasRemoveAccess(contextAccess)) )
        showLink = true;

     return showLink;
  }

/**
   * canRemoveContent - This method is used to determine if
   *             the context user can remove Content to a Route.
   * @param context the eMatrix <code>Context</code> object
   * @param args empty
   * @return boolean
   * @throws Exception if the operation fails
   * @since Common 11-0
   */
//Added for the Bug 314495 Begin
 public boolean canRemoveContent(Context context, String[] args)
    throws Exception
  {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      String objectId    = (String) programMap.get("objectId");
       boolean showLink = true;

      if(objectId == null || objectId.equals(""))
       {
        showLink = false;
       }

      return showLink;
  }
//Added for the Bug 314495 End

    public com.matrixone.jdom.Document getRouteMailXML(Context context, Map info) throws Exception
    {
        // get base url
        String baseURL = (String)info.get("baseURL");
        // get notification name
        String notificationName = (String)info.get("notificationName");
        HashMap eventCmdMap = UIMenu.getCommand(context, notificationName);
        String eventName = UIComponent.getSetting(eventCmdMap, "Event Type");
        String eventKey = "emxComponents.Route.Event." + eventName.replace(' ', '_');
        String bundleName = (String)info.get("bundleName");
        String locale = ((Locale)info.get("locale")).toString();
        String i18NEvent = EnoviaResourceBundle.getProperty(context, bundleName, context.getLocale(),eventKey);
        // get Message Type
        String messageType = (String)info.get("messageType");

        // get route id
        String routeId = (String)info.get("id");
        // get document object info
        DomainObject route = DomainObject.newInstance(context, routeId);
        StringList selectList = new StringList(3);
        selectList.addElement(SELECT_TYPE);
        selectList.addElement(SELECT_NAME);
        selectList.addElement(SELECT_REVISION);
        Map routeInfo = route.getInfo(context, selectList);
        String routeType = (String)routeInfo.get(SELECT_TYPE);
        String i18NRouteType = UINavigatorUtil.getAdminI18NString("type", routeType, locale);
        String routeName = (String)routeInfo.get(SELECT_NAME);
        String routeRev = (String)routeInfo.get(SELECT_REVISION);

        // header data
        HashMap headerInfo = new HashMap();
        headerInfo.put("header", i18NEvent + " : " + i18NRouteType + " " + routeName + " " + routeRev);

        // body data
        HashMap bodyInfo = null;
        MapList objList = route.getRelatedObjects(context, DomainConstants.RELATIONSHIP_OBJECT_ROUTE, "*", selectList, null, true, false, (short)1, null, null);
        if (objList != null && objList.size() > 0)
        {
            bodyInfo = new HashMap();
            HashMap fieldInfo = new HashMap();
            bodyInfo.put(EnoviaResourceBundle.getProperty(context,bundleName, context.getLocale(), "emxComponents.Route.Event.Mail.Connected_Objects"),fieldInfo); 
            for(int i = 0; i < objList.size(); i++)
            {
                Map objInfo = (Map) objList.get(i);
                String objType = (String)objInfo.get(SELECT_TYPE);
                String i18NObjectType = UINavigatorUtil.getAdminI18NString("type", objType, locale);
                String objName = (String)objInfo.get(SELECT_NAME);
                String objRev = (String)objInfo.get(SELECT_REVISION);

                fieldInfo.put(EnoviaResourceBundle.getProperty(context,bundleName, context.getLocale(), "emxComponents.Route.Event.Mail.TNR"), i18NObjectType + " " + objName + " " + objRev);
            }
        }

        // footer data
        HashMap footerInfo = new HashMap();
        ArrayList dataLineInfo = new ArrayList();
        if (messageType.equalsIgnoreCase("html"))
        {
            String[] messageValues = new String[4];
            messageValues[0] = baseURL + "?objectId=" + routeId;
            messageValues[1] = i18NRouteType;
            messageValues[2] = routeName;
            messageValues[3] = routeRev;
            String viewLink = MessageUtil.getMessage(context,null,
                                                     "emxComponents.Object.Event.Html.Mail.ViewLink",
                                                     messageValues,null,
                                                     context.getLocale(),bundleName);

            dataLineInfo.add(viewLink);
        } else {
            String[] messageValues = new String[3];
            messageValues[0] = i18NRouteType;
            messageValues[1] = routeName;
            messageValues[2] = routeRev;
            String viewLink = MessageUtil.getMessage(context,null,
                                                     "emxComponents.Object.Event.Text.Mail.ViewLink",
                                                     messageValues,null,
                                                     context.getLocale(),bundleName);

            dataLineInfo.add(viewLink);
            dataLineInfo.add(baseURL + "?objectId=" + routeId);
        }
        footerInfo.put("dataLines", dataLineInfo);

        return (emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, bodyInfo, footerInfo));
    }

    public String getRouteMessageHTML(Context context, String[] args) throws Exception
    {
        Map info = (Map)JPO.unpackArgs(args);
        info.put("messageType", "html");
        com.matrixone.jdom.Document doc = getRouteMailXML(context, info);

        return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html"));

    }

    public String getRouteMessageText(Context context, String[] args) throws Exception
    {
        Map info = (Map)JPO.unpackArgs(args);
        info.put("messageType", "text");
        com.matrixone.jdom.Document doc = getRouteMailXML(context, info);

        return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text"));

    }

    /**
     * Implements resume process for the route object. Following things will happen on broad level
     * o    All the tasks of this route will be found, and each tasks if assigned to any task assignee, it will be unassigned.
     * (The Inbox Task will be disconnected from the Person object with relationship 'Project Task').
     * So all these tasks will be removed from the 'My Tasks' list of the respective task assignees.
     * o    Demote the route to Define state. (The first state of the route).
     * o    Set 'Current Route Node' on route object to 1. The Route Status of the route will be made as 'Started' and route is promoted to 'In Process' state.
     * This will start the route.
     *
     * @param context The Matrix Context object
     * @param args The arguments array.
     * @return 0 indicating successful operation or 1 indicating unsuccessful operation
     * @throws Exception of operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
    public int resume(Context context, String[] args) throws Exception {
        // Arguments check
        if (context == null) {
            throw new Exception("Invalid context");
        }
        try {
            // Get parameter
            String strRouteId = this.getId();
            ContextUtil.startTransaction(context, true);

            // Some constants
            final String POLICY_ROUTE_STATE_DEFINE = PropertyUtil.getSchemaProperty(context, "Policy", POLICY_ROUTE, "state_Define");
            final String POLICY_INBOX_TASK_STATE_ASSIGNED = PropertyUtil.getSchemaProperty(context, "Policy", POLICY_INBOX_TASK, "state_Assigned");
            final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", POLICY_INBOX_TASK, "state_Complete");
            final String SELECT_RELATIONSHIP_PROJECT_TASK_ID = "from[" + RELATIONSHIP_PROJECT_TASK + "].id";
            final String SELECT_ROUTE_NODE_ID = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "]";
            final String SELECT_ROUTE_TASK_USER = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_TASK_USER + "]";
            final String ATTRIBUTE_CURRENT_ROUTE_NODE = PropertyUtil.getSchemaProperty(context, "attribute_CurrentRouteNode");
            final boolean USE_CACHE = true;

            // Variables
            Map mapInfo = null;
            String strRelProjectTaskId = "";
            String strTaskId = "";
            String strTaskState = "";
            String strRouteTaskUser = "";
            String strRouteTaskUserId = "";
            String strRouteNodeId = "";
            String strRoleOrGroup = "";
            DomainObject dmoTask = null;
            DomainObject dmoRTU = new DomainObject();

            // Create route object
            Route objRoute = (Route)newInstance(context, TYPE_ROUTE);
            objRoute.setId(strRouteId);

            // Expand the route to find the connected task objects
            StringList slBusSelect = new StringList();
            StringList slRelSelect = new StringList();
            String strWhereClause = "";

            slBusSelect.add(SELECT_RELATIONSHIP_PROJECT_TASK_ID);
            slBusSelect.add(DomainObject.SELECT_ID);
            slBusSelect.add(DomainObject.SELECT_CURRENT);
            slBusSelect.add(SELECT_ROUTE_NODE_ID);
            slBusSelect.add(SELECT_ROUTE_TASK_USER);

            MapList mlRouteTasks = objRoute.getRouteTasks(context, slBusSelect, slRelSelect, strWhereClause, !USE_CACHE);

            // Iterate on each task
            for (Iterator itrRouteTasks = mlRouteTasks.iterator(); itrRouteTasks.hasNext(); ) {
                mapInfo = (Map)itrRouteTasks.next();

                strTaskId = (String)mapInfo.get(DomainObject.SELECT_ID);
                strTaskState = (String)mapInfo.get(DomainObject.SELECT_CURRENT);
                strRelProjectTaskId = (String)mapInfo.get(SELECT_RELATIONSHIP_PROJECT_TASK_ID);
                strRouteNodeId = (String)mapInfo.get(SELECT_ROUTE_NODE_ID);
                strRouteTaskUser = (String)mapInfo.get(SELECT_ROUTE_TASK_USER);

                // Disconnect the relationship project task, so that the task will be unassigned
                if (strRelProjectTaskId != null) {
                    DomainRelationship.disconnect(context, strRelProjectTaskId);
                }

                dmoTask = new DomainObject(strTaskId);

                // If the route task user is given then the task was created for role or group.
                // Therefore, we will disconnect the current person from route and then connect an newly created Route Task User
                // object, so that next time when route will be started, the task will be assigned to the role/group.
                //
                if (strRouteTaskUser != null && !"".equals(strRouteTaskUser) && (strRouteTaskUser.startsWith("role_") || strRouteTaskUser.startsWith("group_"))) {
                    // Create a new RTU object
                    dmoRTU.createObject(context, DomainObject.TYPE_ROUTE_TASK_USER, null, null, DomainObject.POLICY_ROUTE_TASK_USER, null);

                    // Connect to the 'to' side of the corresponding route node relationship
                    DomainRelationship.setToObject(context, strRouteNodeId, dmoRTU);

                    // Set the value of RTU on route node relationship (it was cleared when task was completed)
                    DomainRelationship.setAttributeValue(context, strRouteNodeId, DomainObject.ATTRIBUTE_ROUTE_TASK_USER, strRouteTaskUser);

                    // Check the state of this task, if it is not Complete then it means that it will be reused.
                    // So set its owner as Route Task User (role or group).
                    if (!POLICY_INBOX_TASK_STATE_COMPLETE.equals(strTaskState)) {
                        strRoleOrGroup = PropertyUtil.getSchemaProperty(context, strRouteTaskUser);
                        dmoTask.setOwner(context, strRoleOrGroup);
                    }
                }

                // Check the state of this task, if it is not Complete then set it to Assigned (first state).
                if (!POLICY_INBOX_TASK_STATE_COMPLETE.equals(strTaskState)) {
                    dmoTask.setState(context, POLICY_INBOX_TASK_STATE_ASSIGNED);
                }
            }//for

            // Set route object from the begining
            objRoute.setState(context, POLICY_ROUTE_STATE_DEFINE);

            // Reset the attribute values
            Map mapAttributes = new HashMap();
            mapAttributes.put(ATTRIBUTE_CURRENT_ROUTE_NODE, "1");
            mapAttributes.put(ATTRIBUTE_ROUTE_STATUS, "Not Started");
            objRoute.setAttributeValues(context, mapAttributes);

            //Promote the route, so that InitiateRoute() method in emxCommonInitiateRouteBase JPO will be triggered
            objRoute.promote(context);
            objRoute.setDueDateFromOffsetForGivenLevelTasks(context, 1);

            ContextUtil.commitTransaction(context);
            return 0;
        }
        catch (Exception exp) {
            ContextUtil.abortTransaction(context);
            throw exp;
        }
    }

    /**
     * Implements promote trigger process for the object. Following things will happen on broad level
     * For the object for which the routes are attached to when promoted to next state the Routes
     * attached to the state is resumed.
     * @param context The Matrix Context object
     * @param args The arguments array.
     *  args[0] : The object id
     * @return 0 indicating successful operation or 1 indicating unsuccessful operation
     * @throws Exception of operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
    public int triggerAutoResumeRoute(Context context, String[] args) throws Exception
    {
        if (context == null) {
            throw new Exception("Invalid context");
        }

        String strObjectId = args[0];   // getting the id of the object
        DomainObject domainObject = new DomainObject(strObjectId);

        StringList busSelects = new StringList();
        busSelects.add(DomainObject.SELECT_CURRENT);
        busSelects.add(DomainObject.SELECT_POLICY);

        //Finding the current state and policy of the object
        Map mapInfo = domainObject.getInfo(context, busSelects);
        String strStateName = (String)mapInfo.get(DomainObject.SELECT_CURRENT);
        String strPolicyName = (String)mapInfo.get(DomainObject.SELECT_POLICY);

        //Getting the symbolic names of the object
        String strSymbolicState = FrameworkUtil.reverseLookupStateName(context, strPolicyName, strStateName);
        String strSymbolicPolicy = FrameworkUtil.getAliasForAdmin(context, "Policy", strPolicyName, false);

        //Obtaining the state based routes of the current state
        String strTypePattern = DomainObject.TYPE_ROUTE;
        String strRelPattern = DomainObject.RELATIONSHIP_OBJECT_ROUTE;
        StringList slBusSelect = new StringList();
        slBusSelect.add(DomainObject.SELECT_ID);
        StringList slRelSelect = new StringList();
        short nRecurseLevel = (short)1;
        String strBusWhere = "";
        String strRelWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_STATE + "]==\"" + strSymbolicState + "\" && attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_POLICY + "]==\"" +strSymbolicPolicy+ "\"";
        final boolean GET_TO = true;
        final boolean GET_FROM = true;

        MapList mlRoutes = domainObject.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseLevel, strBusWhere, strRelWhere);

        Map mapRoute = null;
        String strRouteId = "";

        for (Iterator itrRoutes = mlRoutes.iterator(); itrRoutes.hasNext();) {
            mapRoute = (Map)itrRoutes.next();
            strRouteId = (String)mapRoute.get(DomainObject.SELECT_ID);

            this.setId(strRouteId);
            this.resume(context, new String[0]); //calling the resume process for the state based Route
        }

        return 0;
    }

    /**
     * Implements demote trigger process for the object. Following things will happen on broad level
     * For the object for which the routes are attached to when Demoted the Routes attached to the state
     * it is demoted will start from the begining and the routes attached to the state from which it is
     * demoted will stop.
     * @param context The Matrix Context object
     * @param args The arguments array.
     *  args[0] : The object id
     * @return 0 indicating successful operation or 1 indicating unsuccessful operation
     * @throws Exception of operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
    public int triggerAutoStopRoute(Context context, String[] args) throws Exception
    {
        if (context == null) {
            throw new Exception("Invalid context");
        }

        String strObjectId = args[0];   // getting the id of the object
        DomainObject domainObject = new DomainObject(strObjectId);

        StringList busSelects = new StringList();
        busSelects.add(DomainObject.SELECT_CURRENT);
        busSelects.add(DomainObject.SELECT_POLICY);

        //Finding the current state and policy of the object
        Map mapInfo = domainObject.getInfo(context, busSelects);
        String strStateName = (String)mapInfo.get(DomainObject.SELECT_CURRENT);
        String strPolicyName = (String)mapInfo.get(DomainObject.SELECT_POLICY);

         //Iterating to get the next state from whr the object was demoted
         StateList stateList = domainObject.getStates(context);
         ArrayList listStates = new ArrayList();
         State state = null;
         for (Iterator itrStates = stateList.iterator(); itrStates.hasNext();) {
             state = (State)itrStates.next();
             listStates.add(state.getName());
         }
         int nIndexOfState = listStates.indexOf(strStateName);
         if (nIndexOfState == -1) {
             String[] formatArgs = {strStateName};
             String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.RouteBase.CannotFindState",formatArgs);
             throw new Exception(message);
         }
         String strNextStateName = (String)listStates.get(nIndexOfState + 1);

        //Getting the symbolic names of the object
        String strSymbolicState = FrameworkUtil.reverseLookupStateName(context, strPolicyName, strStateName);
        String strSymbolicNextState = FrameworkUtil.reverseLookupStateName(context, strPolicyName, strNextStateName);
        String strSymbolicPolicy = FrameworkUtil.getAliasForAdmin(context, "Policy", strPolicyName, false);

        //Obtaining the state based routes of the current state
        String strTypePattern = DomainObject.TYPE_ROUTE;
        String strRelPattern = DomainObject.RELATIONSHIP_OBJECT_ROUTE;
        StringList slBusSelect = new StringList();
        slBusSelect.add(DomainObject.SELECT_ID);
        StringList slRelSelect = new StringList();
        short nRecurseLevel = (short)1;
        String strBusWhere = "";
        String strRelWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_STATE + "]== \"" + strSymbolicState + "\" && attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_POLICY + "]==\"" +strSymbolicPolicy+ "\"";
        final boolean GET_TO = true;
        final boolean GET_FROM = true;

        MapList mlRoutes = domainObject.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseLevel, strBusWhere, strRelWhere);

        Map mapRoute = null;
        String strRouteId = "";
        for (Iterator itrRoutes = mlRoutes.iterator(); itrRoutes.hasNext();) {
            mapRoute = (Map)itrRoutes.next();
            strRouteId = (String)mapRoute.get(DomainObject.SELECT_ID);

            this.setId(strRouteId);
            this.resume(context, new String[0]); //calling the resume process for the state based Route
        }

        //Obtaining the state based Route for the state from which the object was demoted
        strRelWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_STATE + "]== \"" + strSymbolicNextState + "\" && attribute[" + DomainObject.ATTRIBUTE_ROUTE_BASE_POLICY + "]==\"" +strSymbolicPolicy+ "\"";

        mlRoutes = domainObject.getRelatedObjects(context, strRelPattern, strTypePattern, slBusSelect, slRelSelect, !GET_TO, GET_FROM, nRecurseLevel, strBusWhere, strRelWhere);

        DomainObject dmoObject = null;
        String strRouteStatus = null;
        for (Iterator itrRoutes = mlRoutes.iterator(); itrRoutes.hasNext();) {
            mapRoute = (Map)itrRoutes.next();
            strRouteId = (String)mapRoute.get(DomainObject.SELECT_ID);

            dmoObject = new DomainObject(strRouteId);
            strRouteStatus = dmoObject.getInfo(context, "attribute[" + DomainObject.ATTRIBUTE_ROUTE_STATUS + "]");

            // Only stop the route when it is Started
            if ("Started".equals(strRouteStatus)) {
                dmoObject.setAttributeValue(context, DomainObject.ATTRIBUTE_ROUTE_STATUS, "Stopped") ; // Stopping the route of the state from which the object was demoted
            }
        }

        return 0;
    }

    /**
     * Starts the task on current level
     *
     * @param context the eMatrix Context object
     * @param args The arguments
     * @returns -
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
    public int startTasksOnCurrentLevel(Context context, String[] args) throws Exception {
        // Arguments check
        if (context == null) {
            throw new Exception("Invalid context");
        }

        String strRouteId = this.getId();
        if (strRouteId == null || "".equals(strRouteId)) {
            String[] formatArgs = {strRouteId};
            String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.RouteBase.InvalidRouteId",formatArgs);
            throw new Exception(message);
        }

        final String ATTRIBUTE_CURRENT_ROUTE_NODE = PropertyUtil.getSchemaProperty(context,"attribute_CurrentRouteNode");
        final String SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE = "attribute[" + ATTRIBUTE_CURRENT_ROUTE_NODE + "]";
        final String SELECT_ATTRIBUTE_ROUTE_STATUS = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_STATUS + "]";

        StringList slBusSelect = new StringList(SELECT_ATTRIBUTE_ROUTE_STATUS);
        slBusSelect.add(SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE);
        slBusSelect.add(DomainObject.SELECT_TYPE);
        slBusSelect.add(DomainObject.SELECT_NAME);
        slBusSelect.add(DomainObject.SELECT_REVISION);

        DomainObject dmoRoute = new DomainObject(strRouteId);
        Map mapRouteInfo = dmoRoute.getInfo(context, slBusSelect);

        String strRouteStatus = (String)mapRouteInfo.get(SELECT_ATTRIBUTE_ROUTE_STATUS);
        String strCurrentRouteNode = (String)mapRouteInfo.get(SELECT_ATTRIBUTE_CURRENT_ROUTE_NODE);
        String strType = (String)mapRouteInfo.get(DomainObject.SELECT_TYPE);
        String strName = (String)mapRouteInfo.get(DomainObject.SELECT_NAME);
        String strRevision = (String)mapRouteInfo.get(DomainObject.SELECT_REVISION);

        if (!"Started".equals(strRouteStatus)) {
            return 1;
        }

        //
        // Do the due date adjustments if required. If the new task is added on the current level and for which
        // due date offset is provided, the actual due dates are to be populated on the route node relationships
        // before actual tasks are activated.
        //
        setDueDateFromOffsetForGivenLevelTasks(context, new String[]{strCurrentRouteNode});

        //
        // Call Initiate route so that this method will start any of the not started tasks on given level
        //
        String[] strMethodArguments = new String[] {
            strType, strName, strRevision, strCurrentRouteNode, "0"
        };
        int nReturnCode = emxCommonInitiateRoute_mxJPO.InitiateRoute(context, strMethodArguments);
        return nReturnCode;
    }

    /**
     * Method sets the due dates from due date offset for given level Route Node relationships and Inbox Tasks
     * (if they are activate).
     * (This method assumes that the tasks will be activated (if not already) immediately and hence the current
     * system time is assumed to be the task creation time.)
     *
     * @param context The Matrix Context object
     * @param args The arguments. args[0]: Level/Order for the tasks
     * @return 0 for success and 1 for error
     * @throws Exception if operation fails
     */
    public int setDueDateFromOffsetForGivenLevelTasks(Context context, String[] args) throws Exception {
        // Arguments check
        if (context == null) {
            throw new Exception("Invalid context");
        }

        String strRouteId = this.getId();
        if (strRouteId == null || "".equals(strRouteId)) {
            String[] formatArgs = {strRouteId};
            String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.RouteBase.InvalidRouteId",formatArgs);
            throw new Exception(message);
        }

        String strGivenTaskLevel = args[0];
        if (strGivenTaskLevel == null || "".equals(strGivenTaskLevel)) {
            String[] formatArgs = {strGivenTaskLevel};
            String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.RouteBase.InvalidTaskLevel",formatArgs);
            throw new Exception(message);
        }

        final String SELECT_ATTRIBUTE_ROUTE_STATUS = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_STATUS + "]";
        final String SELECT_REL_ATTRIBUTE_DUEDATE_OFFSET = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_DUEDATE_OFFSET);
        final String SELECT_REL_ATTRIBUTE_DATE_OFFSET_FROM = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM);
        final String SELECT_TASK_ASSIGNEE = "from[" + DomainObject.RELATIONSHIP_PROJECT_TASK + "].to.id";
        final String SELECT_ATTRIBUTE_ROUTE_NODE_ID = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "]";
        final String POLICY_INBOX_TASK_STATE_COMPLETE = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Complete");
        final String POLICY_ROUTE_STATE_IN_PROCESS = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_ROUTE, "state_InProcess");
        final String POLICY_ROUTE_STATE_ASSIGNED = PropertyUtil.getSchemaProperty(context, "Policy", DomainObject.POLICY_INBOX_TASK, "state_Assigned");
        final String SELECT_ROUTE_START_DATE = "state[" + POLICY_ROUTE_STATE_IN_PROCESS + "].actual";
        final String SELECT_TASK_START_DATE = "state[" + POLICY_ROUTE_STATE_ASSIGNED + "].actual";

        StringList slBusSelect = new StringList(SELECT_ATTRIBUTE_ROUTE_STATUS);
        slBusSelect.add(SELECT_ROUTE_START_DATE);
        DomainObject dmoRoute = new DomainObject(strRouteId);
        Map mapRouteInfo = dmoRoute.getInfo(context, slBusSelect);

        String strRouteStatus = (String)mapRouteInfo.get(SELECT_ATTRIBUTE_ROUTE_STATUS);
        String strRouteStartDate = (String)mapRouteInfo.get(SELECT_ROUTE_START_DATE);

        // Only Started routes are valid
        if (!"Started".equals(strRouteStatus)) {
            return 1;
        }

        // Find all the Route Nodes on current level
        String strRelationshipPattern = DomainObject.RELATIONSHIP_ROUTE_NODE;
        String strTypePattern = DomainObject.TYPE_PERSON + "," + DomainObject.TYPE_ROUTE_TASK_USER;
        slBusSelect = new StringList();
        StringList slRelSelect = new StringList();
        final boolean GET_TO = true;
        final boolean GET_FROM = true;
        String strObjectWhere = "";
        String strRelWhere = "";

        slRelSelect.add(DomainRelationship.SELECT_ID);
        slRelSelect.add(SELECT_REL_ATTRIBUTE_DUEDATE_OFFSET);
        slRelSelect.add(SELECT_REL_ATTRIBUTE_DATE_OFFSET_FROM);
        strRelWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_SEQUENCE + "]==" + strGivenTaskLevel;
        MapList mlRouteNodes = dmoRoute.getRelatedObjects(context,
                                                        strRelationshipPattern,
                                                        strTypePattern,
                                                        slBusSelect,
                                                        slRelSelect,
                                                        !GET_TO,
                                                        GET_FROM,
                                                        (short)1,
                                                        strObjectWhere,
                                                        strRelWhere);
        // Filter all the Route Nodes for which Due Date Offset is not set
        MapList mlFilteredData = new MapList();
        Map mapRouteNode = null;
        String strDueDateOffset = null;
        StringList slRouteNodeIds = new StringList();
        for (Iterator itrRouteNodes = mlRouteNodes.iterator(); itrRouteNodes.hasNext();) {
            mapRouteNode = (Map) itrRouteNodes.next();
            strDueDateOffset = (String)mapRouteNode.get(SELECT_REL_ATTRIBUTE_DUEDATE_OFFSET);
            if (!(strDueDateOffset == null || "".equals(strDueDateOffset))) {
                mlFilteredData.add(mapRouteNode);

                // Add the ids to be used in matchlist preparation
                slRouteNodeIds.add((String)mapRouteNode.get(DomainRelationship.SELECT_ID));
            }
        }
        mlRouteNodes = mlFilteredData;

        // If there are no such relationships then we are done here itself
        if (mlRouteNodes.size() == 0) {
            return 0;
        }

        // Get the tasks for all these Route Nodes relationships
        strRelationshipPattern = DomainObject.RELATIONSHIP_ROUTE_TASK;
        strTypePattern = DomainObject.TYPE_INBOX_TASK;
        slBusSelect = new StringList();
        slRelSelect = new StringList();
        strObjectWhere = "attribute[" + DomainObject.ATTRIBUTE_ROUTE_NODE_ID + "] matchlist \"" + FrameworkUtil.join(slRouteNodeIds, ",") + "\" \",\" && current != const \"" + POLICY_INBOX_TASK_STATE_COMPLETE + "\"";

        strRelWhere = "";
        slBusSelect.add(DomainObject.SELECT_ID);
        slBusSelect.add(SELECT_TASK_ASSIGNEE);
        slBusSelect.add(SELECT_ATTRIBUTE_ROUTE_NODE_ID);

        MapList mlInboxTasks = dmoRoute.getRelatedObjects(context,
                                                        strRelationshipPattern,
                                                        strTypePattern,
                                                        slBusSelect,
                                                        slRelSelect,
                                                        GET_TO,
                                                        !GET_FROM,
                                                        (short)1,
                                                        strObjectWhere,
                                                        strRelWhere);
        // Filter tasks which do not have assignees. This is due to partial tasks created by Resume Process of route
        mlFilteredData = new MapList();
        Map mapInboxTasks = null;
        String strTaskAssigneeId = null;
        for (Iterator itrInboxTasks = mlInboxTasks.iterator(); itrInboxTasks.hasNext();) {
            mapInboxTasks = (Map) itrInboxTasks.next();
            strTaskAssigneeId = (String)mapInboxTasks.get(SELECT_TASK_ASSIGNEE);
            if (!(strTaskAssigneeId == null || "".equals(strTaskAssigneeId))) {
                mlFilteredData.add(mapInboxTasks);
            }
        }
        mlInboxTasks = mlFilteredData;

        // Check if we have any Route Node relationships for which the tasks are created,
        // if so then note the task id against those Route Node relationships.
        for (Iterator itrRouteNodes = mlRouteNodes.iterator(); itrRouteNodes.hasNext();) {
            mapRouteNode = (Map) itrRouteNodes.next();
            String strRouteNodeRelId = (String)mapRouteNode.get(DomainRelationship.SELECT_ID);

            for (Iterator itrInboxTask = mlInboxTasks.iterator(); itrInboxTask.hasNext();) {
                Map mapInboxTask = (Map) itrInboxTask.next();
                String strRouteNodeId = (String)mapInboxTask.get(SELECT_ATTRIBUTE_ROUTE_NODE_ID);

                if (strRouteNodeRelId.equals(strRouteNodeId)) {
                    mapRouteNode.put("CorrespondingTaskId", mapInboxTask.get(DomainObject.SELECT_ID));
                    break;
                }
            }
        }

        //
        // Set due dates on the route node relationships using the due date offset.
        // If for the Route Node relationship, a task is created, then also update the due date on the task object
        //
        SimpleDateFormat dateFormat = new SimpleDateFormat (eMatrixDateFormat.getInputDateFormat(), Locale.US);
        GregorianCalendar calTaskCreationDate = new GregorianCalendar(); // Current date is task Creation date default
        GregorianCalendar calRouteStartDate = new GregorianCalendar();   // The date when route moved into In Process state
        calRouteStartDate.setTime(dateFormat.parse(strRouteStartDate));

        GregorianCalendar calOffset = new GregorianCalendar();
        String strRouteNodeRelId = null;
        String strDueDateOffsetFrom = null;
        String strCorrespondingTaskId = null;
        String strCalculatedDueDate = null;
        String strTaskStartDate = null;
        DomainObject dmoInboxTask = DomainObject.newInstance(context);

        for (Iterator itrRouteNodes = mlRouteNodes.iterator(); itrRouteNodes.hasNext();) {
            mapRouteNode = (Map) itrRouteNodes.next();

            strRouteNodeRelId = (String)mapRouteNode.get(DomainRelationship.SELECT_ID);
            strDueDateOffset = (String)mapRouteNode.get(SELECT_REL_ATTRIBUTE_DUEDATE_OFFSET);
            strDueDateOffsetFrom = (String)mapRouteNode.get(SELECT_REL_ATTRIBUTE_DATE_OFFSET_FROM);
            strCorrespondingTaskId = (String)mapRouteNode.get("CorrespondingTaskId");

            if ("Route Start Date".equals(strDueDateOffsetFrom)) {
                calOffset = (GregorianCalendar) calRouteStartDate.clone();
            }
            else if ("Task Create Date".equals(strDueDateOffsetFrom)) {
                // If the task is not yet created then take current date as Task Start Date,
                // else find the date when task has gone into Assigned state
                if (strCorrespondingTaskId != null && !"".equals(strCorrespondingTaskId) && !"null".equals(strCorrespondingTaskId)) {
                    dmoInboxTask.setId(strCorrespondingTaskId);
                    strTaskStartDate = dmoInboxTask.getInfo(context, SELECT_TASK_START_DATE);
                    calTaskCreationDate.setTime(dateFormat.parse(strTaskStartDate));
                }

                calOffset = (GregorianCalendar) calTaskCreationDate.clone();
            }
            else {
                continue;
            }

            // Calculate new date
            calOffset.add(Calendar.DATE, Integer.parseInt(strDueDateOffset));
            strCalculatedDueDate = dateFormat.format(calOffset.getTime());

            // Update the relationship attribute.
            DomainRelationship.setAttributeValue(context, strRouteNodeRelId, DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE, strCalculatedDueDate);

            // Check if we need to set this value on the task object also
            if (strCorrespondingTaskId != null && !"".equals(strCorrespondingTaskId) && !"null".equals(strCorrespondingTaskId)) {
                dmoInboxTask.setId(strCorrespondingTaskId);
                dmoInboxTask.setAttributeValue(context, DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE, strCalculatedDueDate);
            }
        }

        return 0;
    }


    /**
     * canManageRouteApprovals - This method is used to determine if
     *             the context user can Manage Route Approvals.
     * @param context the eMatrix <code>Context</code> object
     * @param args empty
     * @return boolean
     * @throws Exception if the operation fails
     * @since Common V6R2009-1
     * @grade 0
     */
    public boolean canManageRouteApprovals(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId    = (String) programMap.get("objectId");

        if(objectId == null || objectId.equals("")) {
            return false;
        }
        if (isRoutedObjectInLastState(context, args)) {
            return false;
        }
        return true;
    }

    /** added for the bug 341480
     * routeDeleteCheck - gets the list of Task Escalation Message objects connected to the context Route
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - Route Object Id
     * @returns int
     * @throws Exception if the operation fails
    * @since Common 10.7.HF4
     */
//added for the bug 341480 begin
public int routeDeleteCheck(Context context, String[] args)
        throws Exception
    {
        try
        {
           StringList objectSelects=new StringList(1);
           objectSelects.addElement(SELECT_ID);
           String typePattern=PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_relationship_TaskEscalationMessage);
           String relPattern=PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_TaskEscalationMessage);
           DomainObject routeObj=new DomainObject(args[0]);
           MapList list=routeObj.getRelatedObjects(context,
                                 typePattern,
                                 relPattern,
                                 objectSelects,
                                 null,
                                 false,
                                 true,
                                 (short)1,
                                 null,
                                 null);
           String objectIds[]=new String[list.size()];

           for(int i=0;i<list.size();i++)
           {
              objectIds[i]=(String)((Map)list.get(i)).get(SELECT_ID);
           }
           DomainObject.deleteObjects(context,objectIds);
        }
        catch (Exception ex)
        {
            throw ex;
        }
        return 0;
  }
//added for the bug 341480 End

    /**
     * The trigger will check if this is the state based route,
     * then the routed object must be in correct state for route to be started
     *
     * @param context The Matrix Context object
     * @param args The arguments, args[0]: Route object id
     * @return 0 for success 1 for failure
     * @throws Exception if operation fails
     */
    public int validateStateBasedRouteStarting (Context context, String[] args) throws Exception {
        try {
            if (context == null) {
                throw new Exception("Invalid context");
            }
            if (args == null) {
                throw new Exception("Invalid arguments");
            }

            String strObjectId = args[0];
            if (strObjectId == null || "".equals(strObjectId.trim()) || "null".equals(strObjectId.trim())) {
                String[] formatArgs = {strObjectId};
                String message =  ComponentsUIUtil.getI18NString(context, "emxComponents.RouteBase.InvalidObjectId",formatArgs);
                throw new Exception(message);
            }

            final String SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_BASE_STATE);
            final String SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY = DomainRelationship.getAttributeSelect(DomainObject.ATTRIBUTE_ROUTE_BASE_POLICY);
            i18nNow loc = new i18nNow();
            final String STRING_ROUTE_CANNOT_BE_STARTED = loc.GetString("emxComponentsStringResource", context.getSession().getLanguage(), "emxComponents.Route.AlertCannotStartStateBasedRoute");

            String relationshipPattern = DomainObject.RELATIONSHIP_OBJECT_ROUTE;
            String typePattern = "*";
            StringList objectSelects = new StringList();
            StringList relationshipSelects = new StringList();
            final boolean GET_TO = true;
            final boolean GET_FROM = false;
            short recurseToLevel = (short)1;
            String objectWhere = "";
            String relationshipWhere = "";

            objectSelects.addElement(DomainObject.SELECT_CURRENT);
            objectSelects.addElement(DomainObject.SELECT_POLICY);
            objectSelects.addElement(DomainObject.SELECT_TYPE);

            relationshipSelects.addElement(SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY);
            relationshipSelects.addElement(SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE);

            // Find the routed objects
            DomainObject dmoRoute = DomainObject.newInstance(context, strObjectId);
            MapList mlRoutedObjects = dmoRoute.getRelatedObjects(context,
                                                                    relationshipPattern,
                                                                    typePattern,
                                                                    objectSelects,
                                                                    relationshipSelects,
                                                                    GET_TO,
                                                                    !GET_FROM,
                                                                    recurseToLevel,
                                                                    objectWhere,
                                                                    relationshipWhere);

            Map mapRoutedObject = null;
            String strCurrentState = "";
            String strCurrentPolicy = "";
            String strRouteBaseState = "";
            String strRouteBasePolicy = "";
            String strType = "";
            for (Iterator itrRoutedObjects = mlRoutedObjects.iterator(); itrRoutedObjects.hasNext();) {
                mapRoutedObject = (Map) itrRoutedObjects.next();

                strCurrentState = (String)mapRoutedObject.get(DomainObject.SELECT_CURRENT);
                strCurrentPolicy = (String)mapRoutedObject.get(DomainObject.SELECT_POLICY);
                strRouteBaseState = (String)mapRoutedObject.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE);
                strRouteBasePolicy = (String)mapRoutedObject.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY);

                // Is it state based route?
                if (strRouteBasePolicy != null && !"".equals(strRouteBasePolicy) && !"null".equals(strRouteBasePolicy)
                        && strRouteBaseState != null && !"".equals(strRouteBaseState) && !"null".equals(strRouteBaseState) && !"Ad Hoc".equals(strRouteBaseState)) {

                    // Symbolic names -> real names
                    strRouteBasePolicy = PropertyUtil.getSchemaProperty(context, strRouteBasePolicy);
                    strRouteBaseState = PropertyUtil.getSchemaProperty(context, "Policy", strRouteBasePolicy, strRouteBaseState);

                    // Is the current state of the object is correct?
                    if (!(strRouteBasePolicy.equals(strCurrentPolicy) && strRouteBaseState.equals(strCurrentState))) {
                        strType = (String)mapRoutedObject.get(DomainObject.SELECT_TYPE);
                        strType = i18nNow.getAdminI18NString("Type", strType, context.getSession().getLanguage());
                        emxContextUtil_mxJPO.mqlError(context, FrameworkUtil.findAndReplace(STRING_ROUTE_CANNOT_BE_STARTED, "<type name>", strType));
                        return 1;
                    }
                }
            }

            return 0;//Validation success
        }
        catch(Exception exception) {
            emxContextUtil_mxJPO.mqlError(context, exception.getMessage());
            return 1;//Validation failure
        }
    }

    /**
     * Method returns the value for State Condition column for APPObjectRouteSummary table
     *
     * @param context The Matrix Context object
     * @param args The packed arguments
     * @return Vector containing column values
     * @throws Exception if operation fails
     */
    public Vector getStateCondition(Context context,String[] args) throws Exception
    {
        try
        {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);

            Map paramList      = (Map)programMap.get("paramList");
            String strObjectId = (String)paramList.get("objectId");
			if(UIUtil.isNotNullAndNotEmpty(strObjectId))
    		{
            	strObjectId = FrameworkUtil.getOIDfromPID(context, strObjectId);
    		}
            MapList mlRoutes = (MapList)programMap.get("objectList");

            final String SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY = DomainRelationship.getAttributeSelect(ATTRIBUTE_ROUTE_BASE_POLICY);
            final String SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE = DomainRelationship.getAttributeSelect(ATTRIBUTE_ROUTE_BASE_STATE);

            String languageStr = (String)paramList.get("languageStr");
            final String STRING_NONE = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common.None");

            StringList slRelSelect = new StringList();
            slRelSelect.addElement(SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY);
            slRelSelect.addElement(SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE);

            Vector vecStateConditions = new Vector();
            String strRouteBaseState = null;
            String strRouteBasePolicy = null;
            String strRoutedObjectId = null;
            String strRouteId = null;
            String strObjectType = null;
            String strBusWhere = "id=='" + strObjectId + "'";
            DomainObject dmoRoute = DomainObject.newInstance(context);

            DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
            strObjectType = dmoObject.getInfo(context, DomainObject.SELECT_TYPE);

            Map mapRouteInfo = null;
            MapList mlStateBlockInfo = null;
            Map mapStateBlockInfo = null;
            String strStateCondition = null;
            for(Iterator itrRoutes = mlRoutes.iterator();itrRoutes.hasNext();)
            {
                mapRouteInfo = (Map) itrRoutes.next();

                // Preapare route object
                strRouteId = (String)mapRouteInfo.get(DomainObject.SELECT_ID);
                dmoRoute.setId(strRouteId);

                // Find information from route
                mlStateBlockInfo = dmoRoute.getRelatedObjects(context, DomainObject.RELATIONSHIP_OBJECT_ROUTE, strObjectType, null, slRelSelect, true, false, (short)1, strBusWhere, null);
                mapStateBlockInfo = (Map)mlStateBlockInfo.get(0);

                strRouteBasePolicy = (String)mapStateBlockInfo.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_POLICY);
                strRouteBaseState = (String)mapStateBlockInfo.get(SELECT_REL_ATTRIBUTE_ROUTE_BASE_STATE);
                strStateCondition = null;

                if (strRouteBasePolicy != null && !"".equals(strRouteBasePolicy) && !"null".equals(strRouteBasePolicy)) {
                    // Symbolic -> Real
                    strRouteBasePolicy = PropertyUtil.getSchemaProperty(context, strRouteBasePolicy);

                    if (strRouteBasePolicy != null && !"".equals(strRouteBasePolicy) && !"null".equals(strRouteBasePolicy)) {
                        if (strRouteBaseState != null && !"".equals(strRouteBaseState) && !"null".equals(strRouteBaseState)) {
                            // Symbolic to Real
                            strRouteBaseState = PropertyUtil.getSchemaProperty(context, "Policy", strRouteBasePolicy, strRouteBaseState);
                            if (strRouteBaseState != null && !"".equals(strRouteBaseState) && !"null".equals(strRouteBaseState)) {
                                strStateCondition = i18nNow.getStateI18NString(strRouteBasePolicy, strRouteBaseState, languageStr);
                            }
                        }
                    }
                }

                if (strStateCondition == null) {
                    vecStateConditions.add(STRING_NONE);
                }
                else {
                    vecStateConditions.add(strStateCondition);
                }
            }//for

            return vecStateConditions;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    /**
     * Method checks if the routed object is in last state or not. This method
     * is supposed to be used to determine the access for commands for route creation.
     *
     * @param context The Matrix context object
     * @param args The packed program map
     * @return true if route object is in last state, false if the route object is not there or route object
     *         is there but not in last state.
     * @throws Exception if operation fails.
     */
    public static boolean isRoutedObjectInLastState(Context context, String args[]) throws Exception
    {
        HashMap programMap         = (HashMap) JPO.unpackArgs(args);
        DomainObject objectGeneral = DomainObject.newInstance(context);
        String strObjectId            = (String) programMap.get("objectId");

        if (strObjectId == null || "".equals(strObjectId) || "null".equals(strObjectId))
        {
           return false;
        }

        DomainObject dmoRoutedObject = DomainObject.newInstance(context, strObjectId);
        StringList slBusSelect = new StringList(DomainObject.SELECT_CURRENT);
        //slBusSelect.add(DomainObject.SELECT_POLICY);
        Map mapRoutedObjectInfo = dmoRoutedObject.getInfo(context, slBusSelect);

        String strCurrentState = (String)mapRoutedObjectInfo.get(DomainObject.SELECT_CURRENT);
        //String strPolicy = (String)mapRoutedObjectInfo.get(DomainObject.SELECT_POLICY);

        StateList stateList = dmoRoutedObject.getStates(context);
        int lastStateIndex = stateList.size() - 1;
        if(lastStateIndex == 0)
        {
            return false;
        }
        State lastState = (State)stateList.get(lastStateIndex);
        if (lastState.getName().equals(strCurrentState)) {
            return true;
        }

        return false;
    }

    /**
     * Method checks if user can set task escalation. This method
     * is supposed to be used to determine the access for command.
     *
     * @param context The Matrix context object
     * @param args The packed program map
     * @return false if route object is in last state, else true
     * @throws Exception if operation fails.
     */
    public static boolean canSetTaskEscalation(Context context, String args[]) throws Exception
    {
        HashMap programMap         = (HashMap) JPO.unpackArgs(args);
        DomainObject objectGeneral = DomainObject.newInstance(context);
        String strObjectId            = (String) programMap.get("objectId");

        if (strObjectId == null || "".equals(strObjectId) || "null".equals(strObjectId))
        {
           return true;
        }

        if (isRoutedObjectInLastState(context, args)) {
            return false;
        }
        return true;
    }

    /**
     * Method checks if user can start/resume route. This method
     * is supposed to be used to determine the access for command.
     *
     * @param context The Matrix context object
     * @param args The packed program map
     * @return false if route object is in last state, else true
     * @throws Exception if operation fails.
     */
    public static boolean canStartOrResume(Context context, String args[]) throws Exception
    {
        HashMap programMap         = (HashMap) JPO.unpackArgs(args);
        DomainObject objectGeneral = DomainObject.newInstance(context);
        String strObjectId            = (String) programMap.get("objectId");

        if (strObjectId == null || "".equals(strObjectId) || "null".equals(strObjectId))
        {
           return true;
        }
        if (isRoutedObjectInLastState(context, args)) {
            return false;
        }
        return true;
    }


    /** added for the Bug 360573
     * routeDeleteCheck - gets the list of Discussion objects connected to the context Route
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - Route Object Id
     * @returns int
     * @throws Exception if the operation fails
     * @since Common V6R2010x
     */

       public int routeDiscussionDeleteCheck(Context context, String[] args)
       throws Exception
       {
          try
            {
                StringList objectSelects=new StringList(1);
                objectSelects.addElement(SELECT_ID);
                String typePattern=PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_relationship_Thread);
                String relPattern=PropertyUtil.getSchemaProperty(context,DomainSymbolicConstants.SYMBOLIC_type_Thread);
                DomainObject routeObj=new DomainObject(args[0]);
                MapList list=routeObj.getRelatedObjects(context,
                                 typePattern,
                                 relPattern,
                                 objectSelects,
                                 null,
                                 false,
                                 true,
                                 (short)1,
                                 null,
                                 null);
                String objectIds[]=new String[list.size()];

              for(int i=0;i<list.size();i++)
                {
                   objectIds[i]=(String)((Map)list.get(i)).get(SELECT_ID);
                }
                 DomainObject.deleteObjects(context,objectIds);
           }
            catch (Exception ex)
            {
              throw ex;
            }
          return 0;
        }
       /**
        *  When the route route is completed system has to revoke the access granted by Route Access Grantor on Route content (for all grantees)
        *  Access granted by other than Route Access Grantor will remain same.
        */

       public void revokeAccessGrantedByRouteAccessGrantorOnRouteContent(Context context, String[] args) throws Exception {
           Route route = new Route(args[0]);
           /*
            * If current state is not equlas to complete do not revoke the access.
            */
           String currentState = route.getInfo(context, SELECT_CURRENT);
           if(STATE_ROUTE_DEFINE.equals(currentState) || STATE_ROUTE_IN_PROCESS.equals(currentState))
               return;
           route.revokeAccessOnContent(context);
       }

       public String routeScopeSelect(Context context, String[] args) throws Exception
       {
           HashMap programMap         = (HashMap) JPO.unpackArgs(args);
           Map requestMap             = (Map) programMap.get("requestMap");
           Map paramMap               = (Map) programMap.get("paramMap");
           String strLanguage         = (String)requestMap.get("languageStr");
           String supplierOrgId       = (String) requestMap.get("supplierOrgId");
           String relatedObjectId     = (String) requestMap.get("objectId");
           String mode                = (String) requestMap.get("mode");
           StringBuffer sb            = new StringBuffer();
           i18nNow i18nnow            = new i18nNow();
           String scopeChecking       = EnoviaResourceBundle.getProperty(context,"emxComponentsRoutes.RouteUse");
           String strSelectScope      = EnoviaResourceBundle.getProperty(context,"Components","emxComponents.CreateRoute.SelectScope",strLanguage);
           boolean isSupplierReview   = (!"null".equals(supplierOrgId) && supplierOrgId != null && supplierOrgId.trim().length() > 0)?true:false;
           String sTypeName           = "";
           boolean bTeam              = FrameworkUtil.isSuiteRegistered(context,"featureVersionTeamCentral",false,null,null);
           boolean bProgram           = FrameworkUtil.isSuiteRegistered(context,"appVersionProgramCentral",false,null,null);

           if(mode.equalsIgnoreCase("create"))
           {
           if(isSupplierReview) {

               }else {
                 boolean boolHostCompanyEmployee=false;
                 if(scopeChecking.equals("Enterprise")){
                   sb.append("<input type=\"radio\" name=\"selscope\" value=\"All\" checked = \"checked\" ></input>");
                   String strAll = EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Common.All",strLanguage);
                	 sb.append(strAll);
                sb.append("<br></br>");
                 }

                 sb.append("<input type=\"radio\" name=\"selscope\" value=\"Organization\"");
                 if(scopeChecking.equals("Exchange")|| !boolHostCompanyEmployee){
                     sb.append(" checked = \"checked\" ></input>");
                 }
                 String strOrganization = EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Common.Organization",strLanguage);
                 sb.append(strOrganization);
                 sb.append("<br></br>");
                 if( relatedObjectId != null ) {
                     DomainObject boProject = new DomainObject(relatedObjectId);
                     sTypeName = boProject.getInfo(context,"type");
                     String sName = XSSUtil.encodeForHTML(context, boProject.getInfo(context,"name"));
                     if(sTypeName.equals(DomainObject.TYPE_WORKSPACE) ||
                             com.matrixone.apps.domain.util.mxType.isOfParentType(context,sTypeName,com.matrixone.apps.domain.DomainObject.TYPE_WORKSPACE_VAULT) || //Modified:16-Mar-09:wqy:R207:PRG Bug 370839
                             sTypeName.equals(DomainObject.TYPE_PROJECT_SPACE) || mxType.isOfParentType(context,sTypeName,DomainConstants.TYPE_PROJECT_SPACE)) //Modified to handle Sub Type
                  {

                         sb.append("<input type=\"radio\" name=\"selscope\" value=\"ScopeName\" checked = \"checked\" ></input>");
                         sb.append(XSSUtil.encodeForHTML(context,sName));
                         sb.append("&#160;&#160;&#160;");
                         sb.append(XSSUtil.encodeForHTML(context,EnoviaResourceBundle.getProperty(context,"Framework", "emxFramework.Basic.Type",strLanguage))+":");
                         sTypeName =sTypeName.replace(' ', '_'); // This is for Workspace Vault changing type to Workspace_Vault from reading from properties file
                         sb.append(XSSUtil.encodeForHTML(context,EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Type."+sTypeName,strLanguage)));
                         sb.append("<input type=\"hidden\" name=\"folderId\" value=\"");
                         sb.append(XSSUtil.encodeForHTMLAttribute(context,relatedObjectId));
                         sb.append("\"></input>");
                         sb.append("<input type=\"hidden\" name=\"txtWSFolder\" value=\"");
                         sb.append(XSSUtil.encodeForHTMLAttribute(context,sName));
                         sb.append("\"></input>");

                     }else if(sTypeName.equalsIgnoreCase(DomainConstants.TYPE_INBOX_TASK)) {
                       String selectWorkspaceID  ="from["+DomainObject.RELATIONSHIP_ROUTE_TASK+"].to.to["+DomainObject.RELATIONSHIP_ROUTE_SCOPE+"].from.id";
                       String prjId              =boProject.getInfo(context,selectWorkspaceID);
                       if(prjId != null && !prjId.equals("")) {
                         DomainObject wkspaceObject= DomainObject.newInstance(context, prjId);
                         String Type = wkspaceObject.getType(context);
                         if(wkspaceObject.getType(context).equals("Workspace")) {
                           sName=XSSUtil.encodeForHTML(context, wkspaceObject.getName(context));
                         }else {
                           String scopeId=com.matrixone.apps.common.UserTask.getProjectId(context,prjId);
                           wkspaceObject.setId(scopeId);
                           sName=XSSUtil.encodeForHTML(context, wkspaceObject.getName(context));
                           //projectId=scopeId;
                         }
                         sb.append("<input type=\"radio\" name=\"selscope\" value=\"ScopeName\" ></input>");
                         sb.append(XSSUtil.encodeForHTML(context,sName));
                         sb.append("&#160;&#160;&#160;");
                         sb.append(XSSUtil.encodeForHTML(context,EnoviaResourceBundle.getProperty(context,"Framework", "emxFramework.Basic.Type",strLanguage))+":"); 
                         Type =Type.replace(' ', '_'); // This is for Workspace Vault changing type to Workspace_Vault from reading from properties file
                         sb.append(XSSUtil.encodeForHTML(context,EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Type."+Type,strLanguage)));                        
                         sb.append("<input type=\"hidden\" name=\"folderId\" value=\"");
                         sb.append(XSSUtil.encodeForHTMLAttribute(context,prjId));
                         sb.append("\"></input>");
                         sb.append("<input type=\"hidden\" name=\"txtWSFolder\" value=\"");
                         sb.append(XSSUtil.encodeForHTMLAttribute(context,sName));
                         sb.append("\"></input>");
                       }else {
                         relatedObjectId = null;
                       }
                     }else if(sTypeName.equalsIgnoreCase(DomainConstants.TYPE_TASK)) {
                       StringList busSelects = new StringList();
                       busSelects.addElement(DomainObject.SELECT_ID);
                       busSelects.addElement(DomainObject.SELECT_NAME);
                       busSelects.addElement(DomainObject.SELECT_TYPE);

                       com.matrixone.apps.common.Task task = new com.matrixone.apps.common.Task();
                       task.setId(relatedObjectId);

                       Map taskMap = (Map) task.getProject(context,busSelects);
                       String prjId =(String)taskMap.get(DomainObject.SELECT_ID);
                       String prjName =(String)taskMap.get(DomainObject.SELECT_NAME);
                       String prjType =(String)taskMap.get(DomainObject.SELECT_TYPE);
                       String strPrjType=prjType.replace(' ', '_');       
                       strPrjType = EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Type."+strPrjType,strLanguage);
                       String strType = EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Common.Type",strLanguage);

                       if(prjType.equals(DomainObject.TYPE_PROJECT_SPACE) || mxType.isOfParentType(context,prjType,DomainConstants.TYPE_PROJECT_SPACE)) {//Modified to handle Sub Type
                       sb.append("<input type=\"radio\" name=\"selscope\" value=\"ScopeName\" checked = \"checked\"></input> ");
                       sb.append(XSSUtil.encodeForHTML(context,prjName));
                       sb.append(XSSUtil.encodeForHTML(context,strType));
                       sb.append(":");
                       sb.append(XSSUtil.encodeForHTML(context,strPrjType));
                       sb.append("<input type=\"hidden\" name=\"folderId\" value=\"");
                       sb.append(XSSUtil.encodeForHTMLAttribute(context,prjId));
                       sb.append("\"></input>");
                       sb.append("<input type=\"hidden\" name=\"txtWSFolder\" value=\"");
                       sb.append(XSSUtil.encodeForHTMLAttribute(context,prjName));
                       sb.append("\"></input>");

                       }else {

                       sb.append("<input type=\"radio\" name=\"selscope\" value=\"ScopeName\" ></input>");
                       sb.append("<input type=\"text\" readonly=\"readonly\" name=\"txtWSFolder\" value=\"\" placeholder=\"");
                       sb.append(XSSUtil.encodeForXML(context, strSelectScope));
                       sb.append("\" ></input>");
                       sb.append("<input type=\"button\" name=\"btnScope\" value=\"...\" onclick= \"showRouteCreateWSChooser()\" ></input>");
                       sb.append("<input type=\"hidden\" name=\"folderId\" value=\"\" ></input>");
                       }
                     } else if(sTypeName.equals(DomainObject.TYPE_DOCUMENT) ||
                               sTypeName.equals(DomainObject.TYPE_PACKAGE) ||
                                 sTypeName.equals(DomainObject.TYPE_RTS_QUOTATION) ||
                                   sTypeName.equals(DomainObject.TYPE_REQUEST_TO_SUPPLIER) ||
                                       sTypeName.equals(DomainObject.TYPE_PART))
                       {
                       String sId = "";
                       String sName1 = "";

                       DomainObject doObj=new DomainObject(relatedObjectId);
                           doObj.open(context);
                           BusinessObject boWorkspace = ComponentsUtil.getConnectedObject(context,doObj,"","Workspace Vault",true,false);
                       // If project Id is not null then the page is from workspace
                       if(boWorkspace!=null){
                         sId   = boWorkspace.getObjectId();
                         boWorkspace.open(context);
                         sName1 = XSSUtil.encodeForHTML(context, boWorkspace.getName());
                         boWorkspace.close(context);
                       }
                       StringList objSelects = new StringList();
                       objSelects.addElement(DomainConstants.SELECT_ID);
                       objSelects.addElement(DomainConstants.SELECT_NAME);

                       StringList relSelects = new StringList();
                       short level = 1;

                           //MapList scopeList = doObj.getRelatedObjects(context, DomainConstants.RELATIONSHIP_VAULTED_OBJECTS,TYPE_PROJECT_VAULT, objSelects, relSelects, true, false, level, "", "");
                           MapList scopeList = doObj.getRelatedObjects(context, "*",TYPE_PROJECT_VAULT, objSelects, relSelects, true, false, level, "", "");

                       if (scopeList.size()!=0) {
                       sb.append("<input type=\"radio\" name=\"selscope\" value=\"ScopeName\" checked = \"checked\" ></input>");
                       sb.append("<select name=\"txtWSFolder\" onChange=\"javascript:setScopeId()\">");

                         Map workspaceMap = null;
                         String scopeIds  = "";
                         String scopeNames = "";
                         Iterator scopeListItr = scopeList.iterator();

                         // get a list of workspace folder id's for the member
                         StringList scopeIdList = new StringList();
                         while(scopeListItr.hasNext()) {
                           workspaceMap = (Map)scopeListItr.next();
                           scopeIds = (String)workspaceMap.get(DomainObject.SELECT_ID);
                           scopeNames = (String)workspaceMap.get(DomainObject.SELECT_NAME);
               				sb.append("<option value=\"");
               				sb.append(XSSUtil.encodeForHTMLAttribute(context,scopeIds));
               				sb.append("\" selected=\"");
               				sb.append(scopeNames.equals(sName1)? "selected":"");
               				sb.append("\">");
               				sb.append(XSSUtil.encodeForHTML(context,scopeNames));
               				sb.append("</option>");
                         }
						 sb.append("</select>");
                         sb.append("<input type=\"hidden\" name=\"folderId\" value=\"");
                         sb.append(XSSUtil.encodeForHTMLAttribute(context,sId));
                         sb.append("\"></input>");

                       }

                       sb.append("<input type=\"hidden\" name=\"contentId\" value=\"");
                       sb.append(XSSUtil.encodeForHTMLAttribute(context,relatedObjectId));
                       sb.append("\"></input>");

                     }else{
                       relatedObjectId = null;
                     }
                   }
                 if((bTeam || bProgram) && (relatedObjectId == null || "null".equals(relatedObjectId) || "".equals(relatedObjectId) ) ) {

               sb.append("<input type=\"radio\" name=\"selscope\" value=\"ScopeName\" ></input>");
               sb.append("<input type=\"text\" readonly=\"readonly\" name=\"txtWSFolder\" value=\"\" placeholder=\"");
			   sb.append(XSSUtil.encodeForXML(context, strSelectScope));
               sb.append("\"></input>");
               sb.append("<input type=\"button\" name=\"btnScope\" value=\"...\" onclick= \"showRouteCreateWSChooser()\"></input>");
               sb.append("<input type=\"hidden\" name=\"folderId\" value=\"\" ></input>");

                 }
                 }
           }
           if(mode.equalsIgnoreCase("view") || mode.equalsIgnoreCase("edit"))
           {
               Route boRoute = (Route)DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);
               boRoute.setId(relatedObjectId);
               SelectList selectStmts = new SelectList();
               String sAttrRestrictMembers = PropertyUtil.getSchemaProperty(context, "attribute_RestrictMembers" );
               String SELECT_RESTRICT_MEMBERS = DomainObject.getAttributeSelect(sAttrRestrictMembers);
               selectStmts.addElement(SELECT_RESTRICT_MEMBERS);
               Map resultMap = boRoute.getInfo(context, selectStmts);
               String restrictMembers = (String) resultMap.get(SELECT_RESTRICT_MEMBERS);
               String scopeName = "";
               sTypeName = boRoute.getInfo(context,"type");
               if(sTypeName.equals(DomainConstants.TYPE_ROUTE_TEMPLATE) && mode.equalsIgnoreCase("edit"))
               {
                   sb.append("<input type=\"radio\" name=\"scope\" value=\"All\" " );
                   sb.append(restrictMembers.equals("All")?"checked":"");
                   sb.append(">");
                   sb.append(EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Common.All",strLanguage));
                   sb.append("<br><input type=\"radio\" name=\"scope\" value=\"Organization\" ");
                   sb.append(restrictMembers.equals("Organization")?"checked":"");
                   sb.append(" >");
                   sb.append(EnoviaResourceBundle.getProperty(context,"Components","emxComponents.Common.Organization",strLanguage));
               }
               else{
                   if(restrictMembers.equalsIgnoreCase("All") || restrictMembers.equalsIgnoreCase("Organization") )
                   {
                       scopeName = restrictMembers;
                       if("All".equalsIgnoreCase(scopeName))
                           scopeName= EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource",new Locale(strLanguage),"emxComponents.Common.All");
                       else if("Organization".equalsIgnoreCase(scopeName))
                        	   scopeName= EnoviaResourceBundle.getProperty(context,
                               		"emxComponentsStringResource", new Locale(strLanguage),"emxComponents.Common.Organization");
                   }
                   else
                   {
                       Map scopeObjInfo = boRoute.getScopeObjectTypeNameRevision(context, restrictMembers);
                       scopeName = (String) scopeObjInfo.get(DomainConstants.SELECT_NAME);
                   }
                   sb.append(scopeName);
               }
           }
           return sb.toString();
       }

       @com.matrixone.apps.framework.ui.CreateProcessCallable
       @com.matrixone.apps.framework.ui.PostProcessCallable
       public HashMap createRouteProcess(Context context, String[] args) throws Exception
       {
           HashMap requestMap         = (HashMap) JPO.unpackArgs(args);
           String name = (String) requestMap.get("Name");
           String autoNameCheck = (String) requestMap.get("autoNameCheck");
           String AutoNameSeries = (String) requestMap.get("AutoNameSeries");
           String vault = (String) requestMap.get("Vault");
           String revision = (String) requestMap.get("Revision");
           String strLanguage         = (String)requestMap.get("languageStr");
           String objectId            = (String)requestMap.get("objectId");
           String routeId             = "";
           String  restrictMembers    = (String)requestMap.get("selscope");
           String  selscopeId         = "";
           String scopeName           = "";
           String strTypeName         = "";
           boolean WSNotSelected       = true;
           boolean isCompletedTask       = false;
           Hashtable routeDetails     =  new Hashtable();
           HashMap resultMap          =  new HashMap();
           i18nNow i18nnow            =  new i18nNow();
           String errorMessage        = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(strLanguage),"emxComponents.CreateRoute.OnCompleteTaskError");
           com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person)DomainObject.newInstance(context,DomainConstants.TYPE_PERSON);
           Route route = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
           if(UIUtil.isNullOrEmpty(revision)){
                   revision = new Policy(DomainConstants.POLICY_ROUTE).getSequence(context);
            }


           BusinessObject routeObject = new BusinessObject(DomainConstants.TYPE_ROUTE,name,revision,vault);
           boolean isExists = routeObject.exists(context);
           if(isExists){
               resultMap.put("ErrorMessage", i18nNow.getTypeI18NString(DomainConstants.TYPE_ROUTE, strLanguage) + " " + name + " " + EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(strLanguage),"emxComponents.Common.AlreadyExists"));
           }else{
             if ("true".equalsIgnoreCase(autoNameCheck)|| UIUtil.isNullOrEmpty(name)){
                 String typeAlias = FrameworkUtil.getAliasForAdmin(context, "type", DomainConstants.TYPE_ROUTE, true);
                 String policyAlias = FrameworkUtil.getAliasForAdmin(context, "policy", DomainConstants.POLICY_ROUTE, true);
                 routeId = FrameworkUtil.autoName(context, typeAlias, AutoNameSeries, policyAlias);
             }else{
                     route.createObject(context, DomainConstants.TYPE_ROUTE, name, revision, DomainConstants.POLICY_ROUTE, vault);
                     routeId = route.getObjectId(context);
             }            
           route.setId(routeId);

           BusinessObject personObject = (BusinessObject)person.getPerson(context);
           DomainObject dmoRequest = new DomainObject(routeId);

           if(restrictMembers.equals("ScopeName")){
                 selscopeId = (String)requestMap.get("folderId");
                 scopeName   = (String)requestMap.get("txtWSFolder");
                 if(UIUtil.isNullOrEmpty(scopeName) && UIUtil.isNullOrEmpty(selscopeId)){
                     resultMap.put("ErrorMessage", i18nnow.getI18nString("emxComponents.CreateRoute.EnterRouteScope","emxComponentsStringResource",strLanguage));
                     WSNotSelected = false;
                 }
            }
            else if(restrictMembers.equals("Organization"))
            {
                selscopeId = restrictMembers;
            }
           if(objectId != null && !"".equals(objectId) && !"null".equals(objectId))
           {
           DomainObject boProject = new DomainObject(objectId);
           strTypeName = boProject.getInfo(context,"type");
           }
           if((strTypeName!=null || !"".equals(strTypeName) || !"null".equals(strTypeName)) && DomainObject.TYPE_DOCUMENT.equals(strTypeName)&& com.matrixone.apps.domain.util.mxType.isOfParentType(context,strTypeName,com.matrixone.apps.domain.DomainObject.TYPE_WORKSPACE_VAULT))
           {
               selscopeId = objectId;
           }
           String routeCompletionAction  = (String)requestMap.get("RouteCompletionAction");
           String routeDescription       = (String)requestMap.get("Description");
           String portalMode             = (String)requestMap.get("portalMode");
           String routeBasePurpose       = (String)requestMap.get("RouteBasePurpose");
           String supplierOrgId          = (String)requestMap.get("supplierOrgId");
           String suiteKey               = (String)requestMap.get("suiteKey");
           String sTemplateId            = (String)requestMap.get("TemplateOID");
           String sTemplateName          = (String)requestMap.get("TemplateDisplay");
           String visblToParent          = (String)requestMap.get("VisbleToParent");
           String strAutoStopOnRejection = (String)requestMap.get("AutoStopOnRejection");

           if(routeBasePurpose != null)
               routeDetails.put("routeBasePurpose", routeBasePurpose);

           if(visblToParent == null || visblToParent.equals("null")){
                   visblToParent = "";
            }
           boolean rtSelected = (sTemplateId != null && !"null".equals(sTemplateId) && !sTemplateId.equals(""));
           if(rtSelected)
              new com.matrixone.apps.common.RouteTemplate(sTemplateId).checksToUseRouteTemplateInRoute(context);

           String sAttrRestrictMembers       = PropertyUtil.getSchemaProperty(context, "attribute_RestrictMembers" );
           String sAttrRouteBasePurpose      = PropertyUtil.getSchemaProperty(context, "attribute_RouteBasePurpose" );
           String sAttrRouteCompletionAction = PropertyUtil.getSchemaProperty(context, "attribute_RouteCompletionAction" );
           String attrOriginator             = PropertyUtil.getSchemaProperty(context, "attribute_Originator");
           final String ATTRIBUTE_AUTO_STOP_ON_REJECTION   = PropertyUtil.getSchemaProperty(context, "attribute_AutoStopOnRejection" );
           String routeAutoNameId            = null;
           String strProjectVault  = "";
           String revisionSequence = "";
           if(WSNotSelected)
           {
           if((objectId != null && !"".equals(objectId) && !"null".equals(objectId)) )
           {
             DomainObject boObject = new DomainObject(objectId);
             String sType  = boObject.getType(context);
             String objState=boObject.getInfo(context,DomainConstants.SELECT_CURRENT);
             if(DomainObject.TYPE_INBOX_TASK.equalsIgnoreCase(sType) && DomainObject.STATE_INBOX_TASK_COMPLETE.equalsIgnoreCase(objState)){
            	 isCompletedTask = true;
             }
             routeDetails.put(objectId,objState);
             boolean isProjId = false;
             try
             {
            	 Route.routeWithScope(context , objectId , routeId , routeDetails );
             }catch(FrameworkException ranc){
            	 if (isCompletedTask && ranc.getMessage().indexOf("fromconnect") >0) {
            		 throw new FrameworkException(errorMessage);
            	 }else {
                 throw new FrameworkException(ranc.getMessage());
             }
           }
           }
           else
           {
               if(restrictMembers.equals("All") || restrictMembers.equals("Organization"))
               {
                   route.connect(context,new RelationshipType(DomainObject.RELATIONSHIP_PROJECT_ROUTE),true, personObject);
               }

               else
             {


           try
           {
               Route.routeWithScope(context , selscopeId , routeId , routeDetails );

           }catch(Exception ranc){
               throw new FrameworkException(ranc.getMessage());
           }
             }
           }

           AttributeList routeAttrList = new AttributeList();
           routeAttrList.addElement(new Attribute(new AttributeType(attrOriginator),context.getUser()));
           routeAttrList.addElement(new Attribute(new AttributeType(sAttrRouteCompletionAction),routeCompletionAction));
           routeAttrList.addElement(new Attribute(new AttributeType(sAttrRouteBasePurpose),routeBasePurpose));
           routeAttrList.addElement(new Attribute(new AttributeType(ATTRIBUTE_AUTO_STOP_ON_REJECTION), strAutoStopOnRejection));// getting Auto Stop Attribute
           
           
           
           if( (selscopeId != null) && (!selscopeId.equals("")) ){
        	   if(FrameworkUtil.isObjectId(context, selscopeId)){
        	   	DomainObject boscope = new DomainObject(selscopeId);
        	   	selscopeId  =  boscope.getInfo(context, "physicalid");
        	   }
             routeAttrList.addElement(new Attribute(new AttributeType(sAttrRestrictMembers),selscopeId));
           }
           route.setId(routeId);
           route.setAttributes(context,routeAttrList);
           route.setDescription(routeDescription);
           route.update(context);
           if (visblToParent != null && !"null".equals(visblToParent) && !"".equals(visblToParent) && "Yes".equalsIgnoreCase(visblToParent))
          {
               routeAttrList.addElement(new Attribute(new AttributeType(DomainObject.ATTRIBUTE_SUBROUTE_VISIBILITY),"Yes"));
               DomainObject taskObj=new DomainObject(objectId);
               String originator=taskObj.getInfo(context,DomainConstants.SELECT_ORIGINATOR);
               String owner=route.getOwner().getName();
               Access acc=new Access();
               acc.setGrantor(owner);
               acc.setUser(originator);
               acc.setReadAccess(true);
               acc.setShowAccess(true);
               BusinessObjectList  Bol=new BusinessObjectList();
               Bol.addElement(route);
               BusinessObject.grantAccessRights(context,Bol,acc);
          }

           BusinessObject routeTemplateObj             = null;
           BusinessObject personObj                    = null;

           SelectList selectPersonStmts                = null;
           SelectList selectPersonRelStmts             = null;
           ExpansionWithSelect personSelect            = null;
           RelationshipWithSelectItr relPersonItr      = null;
           Relationship relationShipRouteNode          = null;

           String routeActionValueStr                  = null;
           String routeSequenceValueStr                = null;
           String routeInstructionsValueStr            = null;
           String sRouteTitle                          = null;
           String routeTaskNameValueStr                = null;
           String routeTaskUser                        = null;
           String routeAssigneeDueDateOptStr           = null;
           String dueDateOffset                        = null;
           String dueDateOffsetFrom                    = null;
           String parallelNodeProcessionRule           = null;
           String reviewTask = "";
           String allowDelegation ="";

           Attribute routeTitle                        = null;
           Attribute routeActionAttribute              = null;
           Attribute routeOrderAttribute               = null;
           Attribute routeInstructionsAttribute        = null;
           Attribute templateTaskAttribute             = null;
           AttributeList attrList                      = null;
           Attribute routeAssigneeDueDateOptAttribute  = null;
           Attribute routeDueDateOffsetAttribute       = null;
           Attribute routeDateOffsetFromAttribute      = null;
           Attribute routeTaskUserAttribute            = null;
           Attribute parallelNodeProcessionRuleAttrib  = null;
           Attribute reviewTaskAttribute               = null;
           Attribute allowDelegationAttribute          = null;

           String templateTaskStr                      = PropertyUtil.getSchemaProperty(context, "attribute_TemplateTask");
           Hashtable routeNodeAttributesTable          = new Hashtable();

           if(rtSelected) {

             selectPersonStmts = new SelectList();
             AccessUtil accessUtil = new AccessUtil();

             selectPersonRelStmts = new SelectList();
             selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE);
             selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ROUTE_ACTION);
             selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS);
             selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_TITLE);
             selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE);
             selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_DUEDATE_OFFSET);
             selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM);
             selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ROUTE_TASK_USER);
             String strParallelNodeProscessionRule = PropertyUtil.getSchemaProperty(context, "attribute_ParallelNodeProcessionRule");
             String  sAttReviewTask               =  PropertyUtil.getSchemaProperty(context,"attribute_ReviewTask");

             selectPersonRelStmts.addAttribute(strParallelNodeProscessionRule);
             selectPersonRelStmts.addAttribute(sAttReviewTask);
             selectPersonRelStmts.addAttribute(DomainObject.ATTRIBUTE_ALLOW_DELEGATION);



             routeTemplateObj = new BusinessObject(sTemplateId);
             routeTemplateObj.open(context);
             try{
             route.connectTemplate(context,sTemplateId);
             } catch(Exception e){
                 resultMap.put("Message",e.getMessage());
             }
             Pattern typePattern = new Pattern(DomainObject.TYPE_PERSON);
             typePattern.addPattern(DomainObject.TYPE_ROUTE_TASK_USER);
             personSelect = routeTemplateObj.expandSelect(context,DomainObject.RELATIONSHIP_ROUTE_NODE,typePattern.getPattern(),
                                              selectPersonStmts,selectPersonRelStmts,false, true, (short)1);

             routeTemplateObj.close(context);
             relPersonItr = new RelationshipWithSelectItr(personSelect.getRelationships());
             // loop thru the rels and get the route object
             while ((relPersonItr != null ) && relPersonItr.next()) {
               if ( relPersonItr.obj().getTypeName().equals(DomainObject.RELATIONSHIP_ROUTE_NODE)) {
                 personObj = relPersonItr.obj().getTo();
                 if (personObj != null)   {
                   personObj.open(context);

                   if((DomainObject.TYPE_ROUTE_TASK_USER).equals(personObj.getTypeName()) || ((DomainObject.TYPE_PERSON).equals(personObj.getTypeName()))  ) {

                     try{
                       relationShipRouteNode = route.connect(context, new RelationshipType(DomainObject.RELATIONSHIP_ROUTE_NODE),true,personObj);
                     } catch(Exception ex){
                         resultMap.put("Message",ex.getMessage());
                     }

                     routeNodeAttributesTable    =  relPersonItr.obj().getRelationshipData();
                     routeSequenceValueStr       = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_ROUTE_SEQUENCE + "]" );
                     sRouteTitle                 = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_TITLE + "]" );
                     routeActionValueStr         = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_ROUTE_ACTION + "]" );
                     routeInstructionsValueStr   = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS + "]" );
                     routeTaskNameValueStr       = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_TITLE + "]" );
                     routeAssigneeDueDateOptStr  = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE + "]" );
                     dueDateOffset               = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_DUEDATE_OFFSET + "]" );
                     dueDateOffsetFrom           = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_DATE_OFFSET_FROM + "]" );
                     routeTaskUser               = (String) routeNodeAttributesTable.get("attribute[" + DomainObject.ATTRIBUTE_ROUTE_TASK_USER + "]" );
                     // Added by Infosys for Bug # 303103 Date 05/11/2005
                     parallelNodeProcessionRule  = (String) routeNodeAttributesTable.get("attribute[" + strParallelNodeProscessionRule + "]" );
                    // Added for the bug 301391
                     reviewTask = (String)routeNodeAttributesTable.get("attribute["+sAttReviewTask+"]");
                    allowDelegation = (String)routeNodeAttributesTable.get("attribute[" +DomainObject.ATTRIBUTE_ALLOW_DELEGATION+ "]" );

                     attrList = new AttributeList();
                     relationShipRouteNode.open(context);

                     // Added by Infosys for Bug # 303103 Date 05/11/2005
                     // set parallelNodeProcessionRule
                     parallelNodeProcessionRuleAttrib  = new Attribute(new AttributeType(strParallelNodeProscessionRule),parallelNodeProcessionRule);
                     attrList.addElement(parallelNodeProcessionRuleAttrib);

                     // set title
                     routeTitle  = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_TITLE),sRouteTitle);
                     attrList.addElement(routeTitle);

                     // set route action
                     if ( routeActionValueStr != null ) {
                       routeActionAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ROUTE_ACTION),routeActionValueStr);
                       attrList.addElement(routeActionAttribute);
                     }

                     // set route order
                     routeOrderAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ROUTE_SEQUENCE),routeSequenceValueStr);
                     attrList.addElement(routeOrderAttribute);

                     // set route instructions
                     if ( routeInstructionsValueStr != null ) {
                       routeInstructionsAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ROUTE_INSTRUCTIONS),routeInstructionsValueStr);
                       attrList.addElement(routeInstructionsAttribute);
                     }

                     templateTaskAttribute = new Attribute(new AttributeType(templateTaskStr),"Yes");
                     attrList.addElement(templateTaskAttribute);

                     // set route assignee due date option
                     if ( routeAssigneeDueDateOptStr != null ) {
                       routeAssigneeDueDateOptAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE),routeAssigneeDueDateOptStr);
                       attrList.addElement(routeAssigneeDueDateOptAttribute);
                     }

                     // set route due date offset
                     if ( dueDateOffset != null ) {
                       routeDueDateOffsetAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_DUEDATE_OFFSET),dueDateOffset);
                       attrList.addElement(routeDueDateOffsetAttribute);
                     }


                     // set route due date offset from
                     if( dueDateOffsetFrom != null ) {
                       routeDateOffsetFromAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM),dueDateOffsetFrom);
                       attrList.addElement(routeDateOffsetFromAttribute);
                     }

                     // set route task user attribute
                     if( routeTaskUser != null ) {
                       routeTaskUserAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ROUTE_TASK_USER),routeTaskUser);
                       attrList.addElement(routeTaskUserAttribute);
                     }
                     // Added for the bug 301391
                     // set Review Task attribute
                     if( reviewTask != null){
                        reviewTaskAttribute = new Attribute(new AttributeType(sAttReviewTask),reviewTask);
                        attrList.addElement(reviewTaskAttribute);
                      }
                    // set Allow Delegation attribute
                      if( allowDelegation != null){
                         allowDelegationAttribute = new Attribute(new AttributeType(DomainObject.ATTRIBUTE_ALLOW_DELEGATION),allowDelegation);
                         attrList.addElement(allowDelegationAttribute);
                     }

                     relationShipRouteNode.setAttributes(context,attrList);
                     relationShipRouteNode.close(context);

                    // Added for bug 376886
                    if(((DomainObject.TYPE_PERSON).equals(personObj.getTypeName())) || ((DomainObject.TYPE_ROUTE_TASK_USER).equals(personObj.getTypeName())))
                    {
                        String personName = personObj.getName();
                        if(DomainObject.TYPE_ROUTE_TASK_USER.equals(personObj.getTypeName()))
                            personName = PropertyUtil.getSchemaProperty(context,routeTaskUser);
                        try{
                            if(!UIUtil.isNullOrEmpty(personName)) {
                        Access access = routeTemplateObj.getAccessForGranteeGrantor(context,personName, AccessUtil.ROUTE_ACCESS_GRANTOR);

                        if (AccessUtil.hasAddRemoveAccess(access))
                            accessUtil.setAccess(personName,AccessUtil.ROUTE_ACCESS_GRANTOR,accessUtil.getAddRemoveAccess());
                        else if(AccessUtil.hasRemoveAccess(access))
                            accessUtil.setAccess(personName,AccessUtil.ROUTE_ACCESS_GRANTOR,accessUtil.getRemoveAccess());
                        else if(AccessUtil.hasAddAccess(access))
                            accessUtil.setAccess(personName,AccessUtil.ROUTE_ACCESS_GRANTOR,accessUtil.getAddAccess());
                        else if(AccessUtil.hasReadWriteAccess(access))
                            accessUtil.setAccess(personName,AccessUtil.ROUTE_ACCESS_GRANTOR,accessUtil.getReadWriteAccess());
                        else
                            accessUtil.setAccess(personName,AccessUtil.ROUTE_ACCESS_GRANTOR,accessUtil.getReadAccess());
                        }
                        }
                        catch(MatrixException e) {
                            throw new FrameworkException(e.toString());
                        }
                    }
                    // Ended

                   }
                   personObj.close(context);
                 }

               }
           }//End while

             if(accessUtil.getAccessList().size() > 0){
                 String[] strArgs = new String[]{route.getObjectId()};
                 JPO.invoke(context, "emxWorkspaceConstants", strArgs, "grantAccess", JPO.packArgs(accessUtil.getAccessList()));
         }

             final String SELECT_ATTRIBUTE_AUTO_STOP_ON_REJECTION = "attribute[" + ATTRIBUTE_AUTO_STOP_ON_REJECTION + "]";
             DomainObject dmoRouteTemplate = new DomainObject(routeTemplateObj);
             strAutoStopOnRejection = dmoRouteTemplate.getInfo(context, SELECT_ATTRIBUTE_AUTO_STOP_ON_REJECTION);
             if (strAutoStopOnRejection != null && !"".equals(strAutoStopOnRejection) && !"null".equalsIgnoreCase(strAutoStopOnRejection)) {
                 route.setAttributeValue(context, ATTRIBUTE_AUTO_STOP_ON_REJECTION, strAutoStopOnRejection);
             }

             routeTemplateObj.close(context);
           }
           }
           resultMap.put(SELECT_ID, routeId);
           }
        return resultMap;
    }

    public String routeTemplateLinkOfRoute(Context context, String[] args) throws Exception
    {
        HashMap programMap         = (HashMap) JPO.unpackArgs(args);
        Map requestMap             = (Map) programMap.get("requestMap");
        String routeId     = (String) requestMap.get("objectId");
        String mode                = (String) requestMap.get("mode");

        Route boRoute = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
        boRoute.setId(routeId);
        StringBuffer sb            = new StringBuffer();
        SelectList selectStmts = new SelectList();
        selectStmts.addElement(boRoute.SELECT_ROUTE_TEMPLATE_NAME);
        selectStmts.addElement(boRoute.SELECT_ROUTE_TEMPLATE_ID);
        Map resultMap = boRoute.getInfo(context, selectStmts);
        String strTemplate    = (String) resultMap.get(boRoute.SELECT_ROUTE_TEMPLATE_NAME);
        String strTemplateId  = (String) resultMap.get(boRoute.SELECT_ROUTE_TEMPLATE_ID);

        if(mode.equalsIgnoreCase("view"))
        {
            if ( strTemplate == null ) {

                sb.append("&#160;");
            } else {
                sb.append("<a href=\"javascript:showModalDialog('../common/emxTree.jsp?emxSuiteDirectory=components&objectId=" + strTemplateId + "', '800', '575')\"><img src=\"../common/images/iconSmallRoute.gif\" alt=\"\" name=\"route\" id=\"route\" border=\"0\"/></a>&#160;");
                sb.append("<a href=\"javascript:showModalDialog('../common/emxTree.jsp?emxSuiteDirectory=components&objectId=" + strTemplateId + "', '800', '575')\">"+strTemplate+"</a>");

            }
        }else
        {
            sb.append(strTemplate);
        }
        return sb.toString();
    }


    public boolean showRouteVisibleToParentField(Context context, String[] args) throws Exception
    {
        HashMap programMap         = (HashMap) JPO.unpackArgs(args);
        String objectId             = (String) programMap.get("objectId");
        Route boRoute = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
        String relTaskSubRoute = PropertyUtil.getSchemaProperty(context, "relationship_TaskSubRoute");
        String connectedIdSelect="to["+relTaskSubRoute+"].from.id";
        boolean visibility = false;
        if(objectId !=null)
        {
            boRoute.setId(objectId);
            String objectType = boRoute.getType(context);
            String connectedId=boRoute.getInfo(context,"to["+relTaskSubRoute+"].from.id");
            visibility = (objectType.equals(DomainConstants.TYPE_INBOX_TASK) ||connectedId != null) ? true : false;
        }
        return visibility;
    }

    /**
     * The main method which all filter access method calls.
     * Returns Route Access List Based on the parameters.
     * @param context the eMatrix Context object
     * @param args holds the grantee and grantor name
     * @return MapList
     * @throws Exception if the operation fails
     * @since R211
     */

   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getRouteAccessSummaryMemberList(Context context,String[] args) throws Exception {
       HashMap programMap = (HashMap) JPO.unpackArgs(args);

       String language = (String) programMap.get("languageStr");
       language = language == null ? context.getLocale().getLanguage() : language;

       com.matrixone.apps.common.Person PersonObj = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
       String objectId    = (String)programMap.get("objectId");
       String strRouteId  = (String)programMap.get("parentOID");

       AccessUtil accessUtil   = new AccessUtil();

       Route routeBO = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
       routeBO.setId(objectId);

       SelectList selectables = new SelectList(10);
       selectables.add(SELECT_OWNER);
       selectables.add(getAttributeSelect(ATTRIBUTE_ROUTE_STATUS));

       Map routeInfo = routeBO.getInfo(context, selectables);

       AccessList routeDelegateGranotrList = routeBO.getAccessForGrantor(context, AEF_ROUTE_DELEGATION_GRANTOR_USERNAME);
       AccessList routeAccessGranotrList = routeBO.getAccessForGrantor(context, AccessUtil.ROUTE_ACCESS_GRANTOR);

       Map routeDelegateMap = new HashMap(routeDelegateGranotrList.size());
       Map routeAccessMap = new HashMap(routeAccessGranotrList.size());

       for (int i = 0; i < routeDelegateGranotrList.size(); i++) {
           Access access = (Access) routeDelegateGranotrList.get(i);
           routeDelegateMap.put(access.getUser(), access);
       }
       
       for (int i = 0; i < routeAccessGranotrList.size(); i++) {
           Access access = (Access) routeAccessGranotrList.get(i);
           routeAccessMap.put(access.getUser(), access);
       }

       selectables.clear();
       selectables.addElement(routeBO.SELECT_ID);
       selectables.addElement(routeBO.SELECT_TYPE);
       selectables.addElement(routeBO.SELECT_NAME);
       selectables.addElement(routeBO.SELECT_CURRENT);
       selectables.addElement(com.matrixone.apps.common.Person.SELECT_COMPANY_NAME);
       selectables.addElement(com.matrixone.apps.common.Person.SELECT_COMPANY_ID);

       // build select params for Relationship
       SelectList selectPersonRelStmts = new SelectList(2);
       selectPersonRelStmts.addElement(routeBO.SELECT_RELATIONSHIP_ID);
       selectPersonRelStmts.addElement(routeBO.SELECT_ROUTE_TASK_USER);

       Pattern typePattern = new Pattern(TYPE_PERSON);
       typePattern.addPattern(TYPE_ROUTE_TASK_USER);
       
       MapList memberList = routeBO.getRelatedObjects(context, 
               RELATIONSHIP_ROUTE_NODE, typePattern.getPattern(),
               selectables, selectPersonRelStmts,
               false, true, (short)1, 
               EMPTY_STRING, EMPTY_STRING,
               0);
       
       boolean isRouteObj = DomainConstants.TYPE_ROUTE.equals(routeInfo.get(DomainConstants.SELECT_TYPE));
       boolean isRouteStarted = !"Not Started".equals((String) routeInfo.get(getAttributeSelect(ATTRIBUTE_ROUTE_STATUS)));
       String strOwner = (String) routeInfo.get(SELECT_OWNER);
       
       StringList members  = new StringList(memberList.size());
       MapList tempMapList = new MapList(memberList.size());

       Iterator itr = memberList.iterator();
       while(itr.hasNext()) {
           Map  membermap =  (Map) itr.next();
           String memberName         = (String)membermap.get(SELECT_NAME);
           String routeTaskUser     = (String)membermap.get(Route.SELECT_ROUTE_TASK_USER);
           String nodeType           = (String)membermap.get(SELECT_TYPE);

           boolean isRouteOwner = memberName.equals(strOwner);
           String sAccess  = isRouteOwner ? "Add Remove" : "Read";

           String dispMemberName = nodeType.equals(TYPE_PERSON) ? memberName :
                                   UIUtil.isNullOrEmpty(routeTaskUser) ? "" : PropertyUtil.getSchemaProperty(context, routeTaskUser);

           if(!UIUtil.isNullOrEmpty(dispMemberName) && !members.contains(dispMemberName)) {
               String org = (String) membermap.get(com.matrixone.apps.common.Person.SELECT_COMPANY_NAME);
                              
               HashMap tempHash = new HashMap(10);
               tempHash.put("Organization", org == null ? EMPTY_STRING : org);
               tempHash.put("OrganizationId", membermap.get(com.matrixone.apps.common.Person.SELECT_COMPANY_ID));
               tempHash.put("ProjectMemberId", "");
               tempHash.put("PersonId", membermap.get(SELECT_ID));
               tempHash.put("RouteNodeId", membermap.get(SELECT_RELATIONSHIP_ID));
               tempHash.put(SELECT_RELATIONSHIP_ID, membermap.get(SELECT_RELATIONSHIP_ID));

               String toNodeType      = (String)membermap.get(SELECT_TYPE);
               String type = "";
               if(toNodeType.equals(TYPE_ROUTE_TASK_USER) && !UIUtil.isNullOrEmpty(routeTaskUser)) {
                   type = routeTaskUser.substring(0, routeTaskUser.indexOf("_") );
                   String sGrantee = PropertyUtil.getSchemaProperty(context, routeTaskUser);
                   
                   tempHash.put(SELECT_ID, routeTaskUser);
                   tempHash.put("LastFirstName", sGrantee);
                   tempHash.put("Type", type);
               } else {
                   String sGrantee = (String)membermap.get(SELECT_NAME) ;
                   Access access = (Access) routeDelegateMap.get(sGrantee);
                   access = access == null ? (Access) routeAccessMap.get(sGrantee) : access;

                   if(access != null && !isRouteOwner) {
                       sAccess = accessUtil.checkAccess(access);
                       if(accessUtil.WORKSPACE_LEAD.equals(sAccess)) {
                           sAccess = accessUtil.ADD_REMOVE;
                       }
                   }
                   
                   tempHash.put("Type", membermap.get(SELECT_TYPE));
                   tempHash.put(SELECT_ID, membermap.get(SELECT_ID));
                   tempHash.put("LastFirstName", PersonUtil.getFullName(context, sGrantee));
                   
               }
               tempHash.put("Access", sAccess);
               
               String disableSelection = isRouteObj && (isRouteOwner || isRouteStarted) ? "true" : "false"; 
               String isAccessColumnEditable = "role".equals(type) || "group".equals(type) || isRouteOwner ? "false" : "true"; 
               
               tempHash.put("disableSelection", disableSelection);
               tempHash.put("isAccessColumnEditable", isAccessColumnEditable);
               
               members.add(dispMemberName);
               tempMapList.add(tempHash);
           }
       }
       return tempMapList;
   }

   /**
    * Gets the vector output in HTML format, for the Name column in the access summary table.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectList - a MapList containing the actual maps "dataMap" containing the data.
    * paramList - a HashMap containing the following parameters.
    * editMode - a String either true or false for deciding the mode.
    * reportFormat - a String to identify the Printer Friendly and Export view.
    * @return Vector of the user display names in the HTML format.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    */

   public Vector getRouteAccessSummaryName (Context context, String[] args)
       throws Exception
   {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap paramList = (HashMap) programMap.get("paramList");
       String languageStr = (String)paramList.get("languageStr");
       String strObjectId = (String)paramList.get("objectId");
       boolean isprinterFriendly = paramList.get("reportFormat") != null;

       MapList objList = (MapList)programMap.get("objectList");

       Vector columnVals = new Vector(objList.size());
       DomainObject doObj = DomainObject.newInstance(context,strObjectId);
       String strObjType = doObj.getInfo(context,DomainConstants.SELECT_TYPE);

       for (int k=0; k < objList.size(); k++){
           Map map = (Map) objList.get(k);
           String strType = (String)map.get("Type");

           String strId = (String)map.get("PersonId");
           String strDisplayName = (String)map.get("LastFirstName");
           StringBuffer strBuffName = new StringBuffer();

           if("Role".equalsIgnoreCase(strType)){
               strDisplayName= i18nNow.getAdminI18NString("Role", strDisplayName, languageStr);
               if(!isprinterFriendly && strObjType.equalsIgnoreCase(DomainConstants.TYPE_ROUTE)) {
               strBuffName.append("<img src=\"../common/images/iconSmallRole.gif\" alt=\"\" name=\"person\" id=\"PersonId\" border=\"0\"/>").append(XSSUtil.encodeForXML(context,strDisplayName));
               }
               strBuffName.append(XSSUtil.encodeForXML(context,strDisplayName));
           } else if("Group".equalsIgnoreCase(strType)){
               strDisplayName= i18nNow.getAdminI18NString("Group", strDisplayName, languageStr);
               if(!isprinterFriendly && strObjType.equalsIgnoreCase(DomainConstants.TYPE_ROUTE)) {
               strBuffName.append("<img src=\"../common/images/iconSmallGroup.gif\" alt=\"\" name=\"person\" id=\"PersonId\" border=\"0\"/>");
               }
               strBuffName.append(XSSUtil.encodeForXML(context,strDisplayName));
           } else {
               if(!isprinterFriendly && strObjType.equalsIgnoreCase(DomainConstants.TYPE_ROUTE)) {
                   strBuffName.append("<a href=\"javascript:showModalDialog('../common/emxTree.jsp?emxSuiteDirectory=components&amp;objectId=" + XSSUtil.encodeForJavaScript(context, strId) + "', '800', '575')\"><img src=\"../common/images/iconSmallPerson.gif\" alt=\"\" name=\"person\" id=\"PersonId\" border=\"0\" /></a>&#160;");
                   strBuffName.append("<a href=\"javascript:showModalDialog('../common/emxTree.jsp?emxSuiteDirectory=components&amp;objectId=" + XSSUtil.encodeForJavaScript(context, strId) + "', '800', '575')\">"+XSSUtil.encodeForXML(context,strDisplayName)+"</a>");
               } else {
                   strBuffName.append(XSSUtil.encodeForXML(context,strDisplayName));
               }
           }
           columnVals.add(strBuffName.toString());
       }
       return columnVals;
   }

   /**
    * Gets the vector output, for the user Type column in the access summary table.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectList - a MapList containing the actual maps "dataMap" containing the data.
    * paramList - a HashMap containing the following parameters.
    * languageStr - a String containing the language information.
    * @return Vector of the user Types in internationalized format.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    */

   public Vector getRouteAccessSummaryType (Context context, String[] args)
       throws Exception
   {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       MapList objList = (MapList)programMap.get("objectList");
       HashMap paramList = (HashMap) programMap.get("paramList");
       String languageStr = (String)paramList.get("languageStr");

       int objListSize = objList.size();
       Vector columnVals   = new Vector(objListSize);
       for (int k=0; k < objListSize; k++) {
           Map map = (Map) objList.get(k);
           String type = (String)map.get("Type");
           // Internationalize the values of Role, Group or Person.
           type = "Person".equalsIgnoreCase(type) ? i18nNow.getTypeI18NString(type,languageStr) :
                  "role".equalsIgnoreCase(type) ? EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common.Role") :
                	  EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Common.Group");
           columnVals.add(type);
       }
       return columnVals;
   }

   /**
    * Gets the vector output, for the user Organization column in the access summary table.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectList - a MapList containing the actual maps "dataMap" containing the data.
    * @return Vector of the user Organizations.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    */

   public Vector getRouteAccessSummaryOrganization (Context context, String[] args)
       throws Exception
   {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       MapList objList = (MapList)programMap.get("objectList");
       int objListSize = objList.size();
       Vector columnVals   = new Vector(objListSize);
       for (int k=0; k < objListSize; k++) {
           Map map = (Map) objList.get(k);
           String org = ((String)map.get("Type")).equalsIgnoreCase("Person") ? (String)map.get("Organization") : EMPTY_STRING;
           columnVals.addElement(org);
       }
       return columnVals;
   }


   /**
    * Gets the vector output in HTML format, for the Access column in the access summary table.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * charSet - a MapList containing the actual maps "dataMap" containing the data.
    * objectList - a MapList containing the actual maps "dataMap" containing the data.
    * paramList - a HashMap containing the following parameters.
    * editMode - a String either true or false for deciding the mode.
    * reportFormat - a String to identify the Printer Friendly and Export view.
    * languageStr - a String containing the language information.
    * accessChoice - a String containing the comma seperated values of the access rights available.
    * @return Vector containing the user access or access choices in HTML format.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    */

   public Vector getRouteAccessSummaryAccess (Context context, String[] args)
       throws Exception
   {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       MapList objList = (MapList)programMap.get("objectList");
       HashMap paramList = (HashMap) programMap.get("paramList");
       String languageStr = (String)paramList.get("languageStr");

       HashMap i18nMap     = new HashMap();
       i18nMap.put("Read", "emxComponents.ObjectAccess.Read");
       i18nMap.put("Read Write", "emxComponents.ObjectAccess.ReadWrite");
       i18nMap.put("Add", "emxComponents.ObjectAccess.Add");
       i18nMap.put("Remove", "emxComponents.ObjectAccess.Remove");
       i18nMap.put("Add Remove", "emxComponents.ObjectAccess.AddRemove");

       int objListSize = objList.size();
       Vector columnVals = new Vector(objListSize);
       for (int k=0; k < objListSize; k++) {
           Map map = (Map)objList.get(k);
           String access = (String)map.get("Access");
           columnVals.addElement(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),(String) i18nMap.get(access)));
       }
       return columnVals;
   }

   /**
    * Gets the vector output in HTML format, for the New Window column in the access summary table.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectList - a MapList containing the actual maps "dataMap" containing the data.
    * paramList - a HashMap containing the following parameters.
    * editMode - a String either true or false for deciding the mode.
    * reportFormat - a String to identify the Printer Friendly and Export view.
    * @return Vector containing the new window icon with the hyperlink in HTML format.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    */

   public Vector getRouteAccessSummaryNewWindow (Context context, String[] args)
       throws Exception {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       MapList objList = (MapList)programMap.get("objectList");
       int objListSize = objList.size();
       HashMap paramList = (HashMap) programMap.get("paramList");
       boolean isprinterFriendly = paramList.get("reportFormat") != null;

       Vector columnVals = new Vector(objListSize);
       for (int i=0; i < objListSize; i++) {
           StringBuffer strBuff = new StringBuffer();
           Map map = (Map) objList.get(i);
           if (!isprinterFriendly && ((String)map.get("Type")).equalsIgnoreCase("Person")) {
               String name = (String)map.get("LastFirstName");
               // Show the new window icon for the users of type Person with the Hyperlink
               String personId = (String)map.get("PersonId");
               strBuff.append("<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?emxSuiteDirectory=components&amp;objectId=");
               strBuff.append(XSSUtil.encodeForJavaScript(context, personId));
               strBuff.append("', '");
               strBuff.append("700");
               strBuff.append("', '");
               strBuff.append("600");
               strBuff.append("', 'false', '");
               strBuff.append("popup");
               strBuff.append("')");
               strBuff.append("\">");
               strBuff.append("<img src=\"../common/images/iconNewWindow.gif\" alt=\"\" name=\"newWindow\" id=\"newWindowId\" border=\"0\" align=\"center\" /></a>");
           }
           columnVals.addElement(strBuff.toString());
       }
       return columnVals;
   }

   /**
    * Gets the vector output for the checkbox column in the access summary table.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectList - a MapList containing the actual maps "dataMap" containing the data.
    * paramList - a HashMap containing the following parameters.
    * editMode - a String either true or false for deciding the mode.
    * @return Vector containing the true or false values.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    * */

   public Vector getRouteAccessSummaryCheckboxStatus (Context context,String[] args) throws Exception {
       HashMap programMap  = (HashMap)JPO.unpackArgs(args);
       HashMap paramList = (HashMap) programMap.get("paramList");
       MapList objectList = (MapList)programMap.get("objectList");

       Vector enableCheckbox = new Vector();
       Iterator objectListItr = objectList.iterator();
       while(objectListItr.hasNext()) {
           Map objectMap = (Map) objectListItr.next();
           enableCheckbox.add("true".equals(objectMap.get("disableSelection")) ? "false" : "true");
       }
       return enableCheckbox;
   }

   public MapList getRouteAccessRolesSearchResults(Context context, String[] args) throws FrameworkException {
       try {
           Map programMap = (Map) JPO.unpackArgs(args);
           String sNamePattern         = (String)programMap.get("Name");
           String sRouteId             = (String)programMap.get("objectId");
           String sSubChecked          = (String)programMap.get("chkSubLevel");
           String sTopChecked          = (String)programMap.get("chkTopLevel");
           String queryLimit           = (String)programMap.get("queryLimit");
           int roleLimit               = Integer.parseInt(queryLimit);

           emxRoleUtil_mxJPO roleUtilJPO = new emxRoleUtil_mxJPO(context, args);
           java.util.List allRolesList = roleUtilJPO.getAllRolesList(context, sRouteId, sNamePattern, sSubChecked, sTopChecked, roleLimit);
           return roleUtilJPO.getRoleListForSummaryTable(allRolesList, getExistingRoleOrGroupList(context, sRouteId));
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }

   protected MapList getExistingRoleOrGroupList(Context context, String sRouteId) throws FrameworkException {
       try {
           MapList existingList = new MapList();
           if(sRouteId!=null && !sRouteId.equals("")) {
               Route routeObj = new Route();
               routeObj.setId(sRouteId);

               SelectList selectPersonRelStmts = new SelectList();
               selectPersonRelStmts.addElement(routeObj.SELECT_ROUTE_TASK_USER);

               MapList existingRoleorGroup = routeObj.getAssignedRoles(context, null, selectPersonRelStmts, false);
               if( existingRoleorGroup != null && existingRoleorGroup.size() > 0) {
                   Iterator roleItr = existingRoleorGroup.iterator();
                   while(roleItr.hasNext()) {
                       Map roleOrGroupMap  = (Map)roleItr.next();
                       String roleOrGroupName = (String) roleOrGroupMap.get(routeObj.SELECT_ROUTE_TASK_USER);
                       roleOrGroupName  = PropertyUtil.getSchemaProperty(context, roleOrGroupName);
                       existingList.add(roleOrGroupName);
                   }
               }
           }
           return existingList;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }

   public MapList getRouteAccessGroupSearchResults(Context context, String[] args) throws FrameworkException {
       try {
           Map programMap = (Map) JPO.unpackArgs(args);
           String sNamePattern         = (String)programMap.get("Name");
           String sRouteId             = (String)programMap.get("objectId");
           String sSubChecked          = (String)programMap.get("chkSubLevel");
           String sTopChecked          = (String)programMap.get("chkTopLevel");
           String queryLimit           = (String)programMap.get("queryLimit");
           int roleLimit               = Integer.parseInt(queryLimit);

           emxGroupUtil_mxJPO groupUtil = new emxGroupUtil_mxJPO(context, args);
           java.util.List allRolesList = groupUtil.getAllGroupList(context, sNamePattern, sSubChecked, sTopChecked, roleLimit);
           return groupUtil.getGroupListForSummaryTable(allRolesList, getExistingRoleOrGroupList(context, sRouteId));
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }

   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getPersonsInWorkspace(Context context,String[] args) throws FrameworkException {
       try {
           HashMap programMap  = (HashMap)JPO.unpackArgs(args);
           String objectId = (String) programMap.get("objectId");
           String scopeId = (String) programMap.get("scopeId");

           boolean hasScopeId = UIUtil.isNotNullAndNotEmpty(scopeId);
           DomainObject workspace = hasScopeId ? (DomainObject)DomainObject.newInstance(context, UserTask.getProjectId(context, scopeId)) : getScopeIdFromRouteId(context, objectId);
           if(!hasScopeId){
               DomainObject route = DomainObject.newInstance(context, objectId);
               scopeId = route.getAttributeValue(context, Route.ATTRIBUTE_RESTRICT_MEMBERS);
           }


           MapList routeMemberList = new MapList();

           SelectList typeSelects = new SelectList(1);
           typeSelects.addElement(DomainObject.SELECT_ID);
           String routeScopeType  = "";

           String sSelRouteScope       = "to[" + DomainConstants.RELATIONSHIP_ROUTE_SCOPE + "].from.type";

           if(null != objectId && !"".equals(objectId)) {
               Route doObj = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
               doObj.setId(objectId);
               routeMemberList = doObj.getRouteMembers(context, typeSelects, new SelectList(), false);
           }
           StringList connectedMembers = new StringList(routeMemberList.size());
           Iterator itr = routeMemberList.iterator();
           while(itr.hasNext())
           {
               Map map = (Map)itr.next();
               connectedMembers.add((String)map.get(DomainObject.SELECT_ID));
           }

           DomainObject doObj;
           if(UIUtil.isNotNullAndNotEmpty(scopeId)) {
               doObj = DomainObject.newInstance(context, scopeId);
               routeScopeType = doObj.getInfo(context, DomainConstants.SELECT_TYPE);
           } else {
               doObj = DomainObject.newInstance(context, objectId);
               routeScopeType = doObj.getInfo(context, sSelRouteScope);
           }

           String strType = workspace.getInfo(context,DomainConstants.SELECT_TYPE);
           if(workspace.isKindOf(context, DomainConstants.TYPE_PROJECT_SPACE)) {
               return getPersonsUnderProjectSpace(context, workspace, connectedMembers);
           } else {
	           //Changed this API as WBI feature would only be applicable to newSecurity context enabled
	           return getPersonsUnderWorkspace_New(context,doObj,connectedMembers);
           }
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }
   
   
      @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
   public StringList getPersonsIdListInWorkspace(Context context,String[] args) throws FrameworkException {
	   
	   StringList personList  = new StringList();
	   MapList personMap = getPersonsInWorkspace(context, args);
	   
	   Map person;
	   for(Object personMapElem:personMap){
		   person = (Map)personMapElem;
		   if(person.containsKey("id")){
			   personList.add((String)person.get("id"));
		   }
	   }

	   return personList;
	   
   }
   
   
   /*
    * To get the Users from the selected Workspace/WorkspaceVault object 
    * */
   private MapList getPersonsUnderWorkspace_New(Context context,DomainObject workspaceVault,StringList connectedMembers) throws FrameworkException {
	   try{
		   String workspaceVaultId = workspaceVault.getId(context);
		   StringList personList = new StringList();
		   MapList personMapList = new MapList();
	
	   //    personList = ${CLASS:emxDomainAccessBase}.getInclusionList(context, workspaceVaultId, "Person");
		  
		   String[] sArgs = new String[3];
		   sArgs[0] = workspaceVaultId;
		   sArgs[1] = "Person";
		   personList = (StringList)JPO.invoke(context, "emxDomainAccessBase", null, "getInclusionList",sArgs, StringList.class);
		   for(int i=0;i<personList.size();i++){
			   Map tempMap = new HashMap();
			   tempMap.put(DomainConstants.SELECT_ID,personList.get(i));
			   //Not been handled for inactive users
	           if(connectedMembers.contains(personList.get(i))){
	               tempMap.put("disableSelection", "true");
	           }
			   personMapList.add(tempMap);
		   }
		   return personMapList;
	   } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }

   private MapList getPersonsUnderWorkspace(Context context, DomainObject workspace, String routeScopeType, StringList connectedMembers) throws FrameworkException {
       try {
           String sState   =  DomainObject.SELECT_CURRENT;

           MapList wsMembersList = new MapList();
           if(routeScopeType.equals(DomainObject.TYPE_PROJECT) || routeScopeType.equals(DomainObject.TYPE_PROJECT_VAULT) ) {
           StringList objectSelects = new StringList(4);
	           objectSelects.addElement(SELECT_ID);
           objectSelects.addElement(sState);

           MapList mapList =  workspace.getRelatedObjects(context,
	        		   							PropertyUtil.getSchemaProperty(context, "relationship_WorkspaceMember"),
	        		   							DomainConstants.TYPE_PERSON, objectSelects, null, false, true,
	        		   							(short)1, "", "", 0, null, null,  null);
           Iterator mapItr = mapList.iterator();
           while(mapItr.hasNext()) {
               Map tempMap = new HashMap();
               Map map = (Map)mapItr.next();
	               
                   String strState =(String) map.get(sState);
                   if(strState.equals("Active")) {
                       tempMap.put(DomainConstants.SELECT_ID, map.get(SELECT_ID));
                       if(connectedMembers.contains(map.get(SELECT_ID)))
                           tempMap.put("disableSelection", "true");
                       wsMembersList.add(tempMap);
                   }
               }
           }
           return wsMembersList;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }
   private MapList getPersonsUnderProjectSpace(Context context, DomainObject workspace, StringList connectedMembers) throws FrameworkException {
       try {
           StringList projectMemberSelects = new StringList(2);
           projectMemberSelects.add(com.matrixone.apps.common.Person.SELECT_CURRENT);
           projectMemberSelects.add(com.matrixone.apps.common.Person.SELECT_ID);

           MapList projectMemberList =  workspace.getRelatedObjects(context,
                   DomainConstants.RELATIONSHIP_MEMBER,
                   "*",
                   projectMemberSelects,
                   null,
                   false,
                   true,
                   (short)1,
                   "",
                   "",
                   0,
                   null,
                   null,
                   null);

           MapList psMembersList = new MapList();
           Iterator projectMemberItr = projectMemberList.iterator();
           while (projectMemberItr.hasNext()) {
               Map tempMap = new HashMap();
               Map projectMemberMap = (Map) projectMemberItr.next();
               String strState =(String) projectMemberMap.get(com.matrixone.apps.common.Person.SELECT_CURRENT);
               if(strState.equals("Active")) {
                   tempMap.put(DomainConstants.SELECT_ID, projectMemberMap.get(com.matrixone.apps.common.Person.SELECT_ID));
                   if(connectedMembers.contains(projectMemberMap.get(com.matrixone.apps.common.Person.SELECT_ID)))
                       tempMap.put("disableSelection", "true");
                   psMembersList.add(tempMap);
               }
           }
           return psMembersList;
        } catch (Exception e) {
            throw new FrameworkException(e);
        }
    }

   protected Workspace getScopeIdFromRouteId(Context context, String objectId) throws FrameworkException {
       DomainObject route = DomainObject.newInstance(context, objectId);
       Workspace workspace = getWorkspace(context, route.getAttributeValue(context, Route.ATTRIBUTE_RESTRICT_MEMBERS));
       return workspace;
   }
   

   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getRolesInWorkspace(Context context,String[] args) throws FrameworkException {
/*       try {
           HashMap programMap  = (HashMap)JPO.unpackArgs(args);
           String objectId = (String) programMap.get("objectId");
           String scopeId = (String) programMap.get("scopeId");
           List roleMapList = new ArrayList();
           boolean hasScopeId = (scopeId == null || scopeId.equals("") || scopeId.equals("null")) ? false : true;
           DomainObject workspace = hasScopeId ? (DomainObject)DomainObject.newInstance(context, UserTask.getProjectId(context, scopeId)) : getScopeIdFromRouteId(context, objectId);
           String type = workspace.getInfo(context, SELECT_TYPE);
           if(type.equals(DomainConstants.TYPE_PROJECT_SPACE) || mxType.isOfParentType(context,type,DomainConstants.TYPE_PROJECT_SPACE))
           {
                roleMapList.addAll(getProjectSpaceGroupsOrRoles(context, workspace.getId(), true));
           }else{
               DomainObject scopeObject = DomainObject.newInstance(context, scopeId);
               String scopeObjectType = scopeObject.getInfo(context, DomainConstants.SELECT_TYPE);
               String sSelPersonName = "to[" + scopeObject.RELATIONSHIP_PROJECT_MEMBERSHIP + "].from.name";
        	   StringList objectSelects = new StringList(1);
        	   objectSelects.add(sSelPersonName);
        	   MapList personsMapList = workspace.getRelatedObjects(context,
        			                                           scopeObject.RELATIONSHIP_PROJECT_MEMBERS,
        			                                           scopeObject.TYPE_PROJECT_MEMBER,
										                       objectSelects,
										                       null,
										                       false,
										                       true,
										                       (short)1,
										                       "",
										                       "",
										                       0,
										                       null,
										                       null,
										                       null);
        	   StringList personNamesList = new StringList();
        	   Iterator personsMapListItr = personsMapList.iterator();
        	   while(personsMapListItr.hasNext()){
        		   Map personMap = (Map) personsMapListItr.next();
        		   personNamesList.add((String) personMap.get(sSelPersonName));
        	   }
               boolean isWorkspaceVault = DomainConstants.TYPE_WORKSPACE_VAULT.equals(scopeObjectType);
               if(isWorkspaceVault){
            	   AccessList accessList = new AccessList();
	        	   String sParentId = scopeObject.getInfo(context,"to[" + scopeObject.RELATIONSHIP_SUBVAULTS + "].from.id");
	               if(sParentId == null || sParentId.equals(""))
	               {
	                 sParentId = scopeObject.getInfo(context,"to[" + scopeObject.RELATIONSHIP_WORKSPACE_VAULTS+ "].from.id");
	               }
	               workspace.setId(sParentId);
	        	   accessList = workspace.getAccessForGrantor(context, AccessUtil.WORKSPACE_ACCESS_GRANTOR);
	        	   if(accessList != null && accessList.size() > 0){
	        		   Iterator accessListItr = accessList.iterator();
	        		   while(accessListItr.hasNext()){
	        			   Access access = (Access) accessListItr.next();
	        			   String userName = access.getUser();
	        		       if(!personNamesList.contains(userName) && access.hasReadAccess()){
	        			       roleMapList.add(userName);
	        		       }
	        		   }
	        	   }
	           }else{
	        	   StringList granteeList = workspace.getGrantees(context);
	               if((granteeList != null && granteeList.size() > 0)) {
	                   Iterator granteeListItr  = granteeList.iterator();
	                   while(granteeListItr.hasNext()) {
	                       String memberName = (String)granteeListItr.next();
	                       if(!personNamesList.contains(memberName) && !roleMapList.contains(memberName)) {
	                           roleMapList.add(memberName);
	                       }
	                   }
	               }
	           }
           }
           return roleMapList.size() > 0 ?
                  new ${CLASS:emxRoleUtil}(context, args).getRoleListForSummaryTable(roleMapList, getExistingRoleOrGroupList(context, objectId)) :
                  new MapList();

       } catch (Exception e) {
           throw new FrameworkException(e);
       }*/
	   return new MapList();
   }

   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getGroupsInWorkspace(Context context,String[] args) throws FrameworkException {
/*        try
        {
            HashMap programMap  = (HashMap)JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            String scopeId = (String) programMap.get("scopeId");
            List groupMapList = new ArrayList();
            boolean hasScopeId = (scopeId == null || scopeId.equals("") || scopeId.equals("null")) ? false : true;
            DomainObject workspace = hasScopeId ? (DomainObject)DomainObject.newInstance(context, UserTask.getProjectId(context, scopeId)) :      getScopeIdFromRouteId(context, objectId);

           String type = workspace.getInfo(context, SELECT_TYPE);
           if(type.equals(DomainConstants.TYPE_PROJECT_SPACE) || mxType.isOfParentType(context,type,DomainConstants.TYPE_PROJECT_SPACE))
           {
                groupMapList.addAll(getProjectSpaceGroupsOrRoles(context, workspace.getId(), false));
           }
           return groupMapList.size() > 0 ? new ${CLASS:emxRoleUtil} (context, args).getRoleListForSummaryTable(groupMapList, getExistingRoleOrGroupList(context, objectId)) : new MapList();
       } catch (Exception e) {
           throw new FrameworkException(e);
       }*/
	   return new MapList();
   }

    protected List getProjectSpaceGroupsOrRoles(Context context, String projectSpaceId, boolean roles) throws FrameworkException
    {
        try {
            List retList = new ArrayList();

            //get the grants from the projectspace
            String cmd = "print bus " + projectSpaceId + " select " +
            "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].from.grantee " +
            "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].from.grantee.isarole " +
            "to[" + DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST + "].from.grantee.isagroup dump " + DOMAIN_DELIMITER;

            String result = MqlUtil.mqlCommand(context, cmd);


            StringTokenizer st = new StringTokenizer(result, DOMAIN_DELIMITER);
            int nTokens = st.countTokens();
            //number of selectables in the mql command is 3
            int numGrantees = nTokens / 3;
            String[] grantees = new String[nTokens];

            int i = 0;
            while (st.hasMoreTokens())
            {
                grantees[i] = st.nextToken();
                i++;
            }

            //iterate through the result of the mql command to obtain the
            //group or role name
            //sample output of the mqlcommand
            //Employee|Test Everything|TRUE|FALSE
            for(int n = 0; n < numGrantees; n++)
            {
                if ((roles == true && grantees[n+numGrantees].equalsIgnoreCase("true")) ||
                    (roles == false && grantees[n+numGrantees*2].equalsIgnoreCase("true")))
                {
                    retList.add(grantees[n]);
                }
            }
            return retList;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }

   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getMemberLists(Context context,String[] args) throws FrameworkException {
       return new MapList();
   }

   protected Workspace getWorkspace(Context context, String objectId) throws FrameworkException {
       try {
           DomainObject domObject = DomainObject.newInstance(context, objectId);
           Workspace workspaceObject = (Workspace)DomainObject.newInstance(context, TYPE_WORKSPACE);
           String type = domObject.getInfo(context, SELECT_TYPE);
           if(TYPE_PROJECT.equals(type)) {
               workspaceObject.setId(objectId);
           } else if(TYPE_PROJECT_VAULT.equals(type) ) {
               String sid = UserTask.getProjectId(context,objectId);
               workspaceObject.setId(sid);
           } else if(TYPE_DOCUMENT.equals(type) )  {
               String sid  = domObject.getInfo(context, "to[" + RELATIONSHIP_VAULTED_DOCUMENTS + "].from.to[" + RELATIONSHIP_PROJECT_VAULTS + "].from.id");
               workspaceObject.setId(sid);
           } else if(type.equals(DomainConstants.TYPE_PROJECT_SPACE) || mxType.isOfParentType(context,type,DomainConstants.TYPE_PROJECT_SPACE)) {
               workspaceObject.setId(objectId);
           }
           return workspaceObject;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }

   public StringList getPersonIDsAddRouteMemberPersonSearch(Context context,String[] args) throws FrameworkException {
       try {
           HashMap programMap  = (HashMap)JPO.unpackArgs(args);
           StringBuffer buffer = new StringBuffer(100);
           buffer.append("print bus ").append(PersonUtil.getUserCompanyId(context)).
           append(" select from[").append(RELATIONSHIP_EMPLOYEE).append("].to.id dump |");

           return FrameworkUtil.split(MqlUtil.mqlCommand(context, buffer.toString()), "|");
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }
   /**
    * Access Function for APPRouteEditActionLink command.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectId - Object Id of the Route object.
    * @return boolean true or false values.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    * */
   public boolean checksToEditRoute(Context context,String[] args) throws Exception {
       
       HashMap programMap         = (HashMap) JPO.unpackArgs(args);
       String objectId            = (String) programMap.get("objectId");

       StringList selectables = new StringList();
       selectables.add(SELECT_CURRENT);
       selectables.add(SELECT_OWNER);
       selectables.add("current.access[modify]");

       DomainObject boObj = DomainObject.newInstance(context, objectId);
       Map objInfo = boObj.getInfo(context, selectables);
       
       String sOwner = (String)objInfo.get(SELECT_OWNER);
       
       return isRouteEditable(context,objInfo) && 
               ("true".equalsIgnoreCase((String)boObj.getInfo(context, "current.access[modify]"))); 
       
   }
   /**
    * Access Function for APPRouteSetTaskEscalationLink command.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectId - Object Id of the Route object.
    * @return boolean true or false values.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    * */
   public boolean checksToSetTaskEscalation(Context context,String[] args) throws Exception {
       
       HashMap programMap         = (HashMap) JPO.unpackArgs(args);
       String objectId            = (String) programMap.get("objectId");

       StringList selectables = new StringList();
       selectables.add(SELECT_CURRENT);
       selectables.add(SELECT_OWNER);
       
       DomainObject boObj = DomainObject.newInstance(context, objectId);
       Map objInfo = boObj.getInfo(context, selectables);
       String sOwner = (String)objInfo.get(SELECT_OWNER);       
       
       return isRouteEditable(context,objInfo) && (sOwner.equals(context.getUser())); 
       
   }
   /**
    * Returns boolean true/false based on the current state of the Route.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param Map - objInfo containing teh current state of the Route.
    * @return Boolean true or false.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    * */
   protected boolean isRouteEditable(Context context, Map objInfo) throws Exception {
       boolean isRouteEditable = true;
       String sState = (String)objInfo.get(SELECT_CURRENT);       
       
       // Do not show links if the Route State is Complete or Archive
       if(sState.equals("Complete") || sState.equals("Archive")){
          isRouteEditable = false;
       }
       return isRouteEditable;
   }
   /**
    * Returns the displayLink based on the Route Status.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param objectId - Object Id of the Route object.
    * @return String displayLink.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    * */
   protected String getDisplayLink(Context context, String objectId) throws Exception {
       DomainObject boObj = DomainObject.newInstance(context, objectId);
       StringList routeSelects = new StringList();
       routeSelects.add(Route.SELECT_ROUTE_STATUS);
       routeSelects.add(SELECT_OWNER);
       Map routeMap = boObj.getInfo(context, routeSelects);
       String routeStatus= (String) routeMap.get(Route.SELECT_ROUTE_STATUS) ;
       if(!context.getUser().equals((String) routeMap.get(SELECT_OWNER))){
    	   return "";
       }else{
       return "Not Started".equals(routeStatus) ? "StartRoute" :
			  "Stopped".equals(routeStatus) 	? "ResumeAndRestartRoute" :
			  "Started".equals(routeStatus) 	? "StopRoute" : "";
       }
   }
   /**
    * Access Function for APPRouteStopActionLink command.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectId - Object Id of the Route object.
    * @return boolean true or false values.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    * */
   public boolean checksToRouteStopActionLink(Context context, String args[]) throws Exception {
       HashMap programMap         = (HashMap) JPO.unpackArgs(args);
       String objectId            = (String) programMap.get("objectId");
       
       return "StopRoute".equals(getDisplayLink(context, objectId));
   }
   /**
    * Access Function for APPRouteRestartActionLink command.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectId - Object Id of the Route object.
    * @return boolean true or false values.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    * */
   public boolean checksToRouteRestartActionLink(Context context, String args[]) throws Exception {
       HashMap programMap         = (HashMap) JPO.unpackArgs(args);
       String objectId            = (String) programMap.get("objectId");
       
       return "ResumeAndRestartRoute".equals(getDisplayLink(context, objectId));
   }
   /**
    * Access Function for APPRouteStartActionLink command.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectId - Object Id of the Route object.
    * @return boolean true or false values.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    * */
   public boolean checksToRouteStartActionLink(Context context, String args[]) throws Exception {
       HashMap programMap         = (HashMap) JPO.unpackArgs(args);
       String objectId            = (String) programMap.get("objectId");
             
       return "StartRoute".equals(getDisplayLink(context, objectId));
   }
   /**
    * Access Function for APPRouteResumeActionLink command.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args contains a packed HashMap with the following entries:
    * objectId - Object Id of the Route object.
    * @return boolean true or false values.
    * @throws Exception if the operation fails.
    * @since CommonComponents R211
    * */
   public boolean checksToRouteResumeActionLink(Context context, String args[]) throws Exception {
       HashMap programMap         = (HashMap) JPO.unpackArgs(args);
       String objectId            = (String) programMap.get("objectId");
	   
	   if(!"ResumeAndRestartRoute".equals(getDisplayLink(context, objectId)))
			return false;
	   //   Resume command will not be shown if the route is stopped due to task rejection
       return !isRouteStoppedDueToRejection(context, objectId);
   }
   
   /**
    * Returns true if the route is stopped due to rejection of any task
    * @param context The Matrix Context object
    * @param strRouteId The object id of route object
    * @return true if the route is stopped due to rejection of route task
    *             false if route is stopped but not due to task rejection
    * @throws Exception if operation fails or the route is not stopped
    */
   protected boolean isRouteStoppedDueToRejection(Context context, String strRouteId) throws Exception {
       boolean isRouteStoppedDueToRejection = true;

       // Initialize route object
       Route objRoute = (Route)DomainObject.newInstance(context, DomainConstants.TYPE_ROUTE);
       objRoute.setId(strRouteId);

       StringList slBusSelect = new StringList(DomainObject.SELECT_ID);
       StringList slRelSelect = new StringList();
       String strRelPattern = DomainObject.RELATIONSHIP_ROUTE_TASK;
       String strTypePattern = DomainObject.TYPE_INBOX_TASK;
       String strBusWhere = "attribute[" + DomainObject.ATTRIBUTE_APPROVAL_STATUS + "]==\"Reject\"";
       String strRelWhere = "";
       short nRecurseLevel = (short)1;
       
	   MapList mlTasks = objRoute.getRelatedObjects( context, 
                                                     strRelPattern, 
                                                     strTypePattern, 
                                                     slBusSelect, 
                                                     slRelSelect, 
                                                     true, 
                                                     false, 
                                                     nRecurseLevel, 
                                                     strBusWhere, 
                                                     strRelWhere, 
                                                     (int)0);

 	   // If tasks found then return true else false
       if (mlTasks == null || mlTasks.size() == 0) {
           isRouteStoppedDueToRejection = false;
       }

       return isRouteStoppedDueToRejection;
   }
   
   public boolean checksToShowEditAllTaskCommand(Context context, String[] args) throws FrameworkException {
       HashMap programMap = getTaskSummaryCommandsAccessChecks(context, args);
       return ((Boolean)programMap.get("APPRouteTaskEditAll")).booleanValue();
   }
   
   public boolean checksToRemoveTask(Context context, String[] args) throws FrameworkException {
       HashMap programMap = getTaskSummaryCommandsAccessChecks(context, args);
       return ((Boolean)programMap.get("APPRouteTaskRemove")).booleanValue();
   }

   protected HashMap getTaskSummaryCommandsAccessChecks(Context context, String[] args) throws FrameworkException {
       try {
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           HashMap requestMap = (HashMap)programMap.get("requestMap");
           String objectId = (String)programMap.get("objectId");
           objectId = objectId != null ? objectId : (String)requestMap.get("objectId");
           
           String loggedInUser = context.getUser();

           StringList routeSelects = new StringList();
           routeSelects.add("current.access[modify]");
           routeSelects.add(SELECT_OWNER);
           routeSelects.add(SELECT_TYPE);
           routeSelects.add(SELECT_CURRENT);
           
           DomainObject contextObj = DomainObject.newInstance(context, objectId);
           Map objInfo = contextObj.getInfo(context,routeSelects);
           
           String sOwner       = (String)objInfo.get(SELECT_OWNER);
           String sTypeName    = (String)objInfo.get(SELECT_TYPE);
           String sState       = (String)objInfo.get(SELECT_CURRENT);
           String hasModifyAccess = (String)objInfo.get("current.access[modify]");

           boolean isRouteTemplate = sTypeName.equals(TYPE_ROUTE_TEMPLATE);
           boolean canEditRoute = !isRouteTemplate && !(sState.equals("Complete") || sState.equals("Archive"));
           boolean isOwner = sOwner.equals(context.getUser());
           boolean canEditTaskList = true;
           if(!isRouteTemplate && canEditRoute && isOwner) {
               String templateId = contextObj.getInfo(context , "from[" + RELATIONSHIP_INITIATING_ROUTE_TEMPLATE + "].to.id");
               if(!UIUtil.isNullOrEmpty(templateId)) {
                   DomainObject routeTempObj = DomainObject.newInstance(context ,templateId);
                   String sTaskEditSetting = routeTempObj.getAttributeValue(context, ATTRIBUTE_TASKEDIT_SETTING);
                   canEditTaskList = !"Maintain Exact Task List".equals(sTaskEditSetting);
               }
           } 
           //Can't remove tasks from Route Template
           boolean bShowTaskRemove =  !isRouteTemplate  && canEditTaskList && "true".equalsIgnoreCase(hasModifyAccess);
           boolean bShowTaskEditAll = isRouteTemplate ? "true".equalsIgnoreCase(hasModifyAccess) :
                                                        isOwner && canEditRoute;
           HashMap detailsMap = new HashMap();
           detailsMap.put("APPRouteTaskEditAll", Boolean.valueOf(bShowTaskEditAll));
           detailsMap.put("APPRouteTaskRemove", Boolean.valueOf(bShowTaskRemove));
           return detailsMap;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }
   
   public Object getRouteEditAccessComboValues(Context context, String[] args) throws Exception {
       try {
           HashMap programMap = (HashMap) JPO.unpackArgs(args);
           Map paramMap = (Map)programMap.get("paramMap");
           String languageStr = (String)paramMap.get("languageStr");
           
           String i18ReadString = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Access.Read");
           String i18ReadWriteString = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Access.ReadWrite");
           String i18AddString = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Access.Add");
           String i18RemoveString = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Access.Remove");
           String i18AddRemoveString = EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.Access.AddRemove");
       
           HashMap tempMap = new HashMap();
           StringList fieldRangeValues = new StringList();
           StringList fieldDisplayRangeValues = new StringList();
           
           // initialize the Stringlists fieldRangeValues
           fieldRangeValues.addElement("Read");
           fieldRangeValues.addElement("Read Write");
           fieldRangeValues.addElement("Add");
           fieldRangeValues.addElement("Remove");
           fieldRangeValues.addElement("Add Remove");
           // initialize the Stringlist fieldDisplayRangeValues with Internalized value
           fieldDisplayRangeValues.addElement(i18ReadString);
           fieldDisplayRangeValues.addElement(i18ReadWriteString);
           fieldDisplayRangeValues.addElement(i18AddString);
           fieldDisplayRangeValues.addElement(i18RemoveString);
           fieldDisplayRangeValues.addElement(i18AddRemoveString);
       
           tempMap.put("field_choices", fieldRangeValues);
           tempMap.put("field_display_choices", fieldDisplayRangeValues);
           return tempMap;
       } catch(Exception e) {
           throw new FrameworkException(e.toString());
       }
   }
   /**
    * Program to get the cell level access for Route Access Table
    *
    * @param context the eMatrix Context object
    * @param String array contains AssigneeIds for Access edit
    * @throws Matrix Exception if the operation fails
    */

   public static StringList getCellAccessforRouteEditAccess(Context context, String args[]) throws Exception {
     HashMap inputMap = (HashMap) JPO.unpackArgs(args);
     MapList objectMap = (MapList) inputMap.get("objectList");
     boolean isCellEditable = false;
     StringList returnStringList = new StringList(objectMap.size());
     for (Iterator objectItr = objectMap.iterator(); objectItr.hasNext();) {
         Map curObjectMap = (Map) objectItr.next(); 
         String routeID = (String)curObjectMap.get("id[parent]");
         DomainObject dom = DomainObject.newInstance(context,routeID);
         String owner=( String)dom.getOwner(context).toString();;
         if(owner.equals(context.getUser())){
        	 isCellEditable = "true".equals(curObjectMap.get("isAccessColumnEditable")); 
         }
       returnStringList.addElement(Boolean.valueOf(isCellEditable));
     }       
     return returnStringList;
   }
   
   // Update function for Route Edit Access
   public void updateRouteAccessValue(Context context, String[] args)throws FrameworkException {
       try{
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           HashMap paramMap = (HashMap)programMap.get("paramMap");
           HashMap requestMap = (HashMap)programMap.get("requestMap");
           
           String strNewAccessValue = (String)paramMap.get("New Value");
           
           String strRouteId = (String)requestMap.get("objectId");
           BusinessObjectList buList= new BusinessObjectList(1);
           buList.addElement(new Route(strRouteId));

           String personId = (String)paramMap.get("objectId");
           Person personObj = new Person(personId);
           String personName = personObj.getInfo(context, SELECT_NAME);
           
           AccessUtil accessUtil = new AccessUtil();
           Access access = "Read".equals(strNewAccessValue) ? accessUtil.getReadAccess() :
                           "Read Write".equals(strNewAccessValue) ? accessUtil.getReadWriteAccess() :
                           "Add".equals(strNewAccessValue) ? accessUtil.getAddAccess() :
                           "Remove".equals(strNewAccessValue) ? accessUtil.getRemoveAccess() : accessUtil.getAddRemoveAccess();
                           
           access.setGrantor(AEF_ROUTE_ACCESS_GRANTOR_USERNAME);
           access.setUser(personName);
           try {
               ContextUtil.pushContext(context, AEF_ROUTE_ACCESS_GRANTOR_USERNAME, null, null);
               BusinessObject.grantAccessRights(context, buList, access);
           } catch(Exception ex) {
               throw new FrameworkException(ex);
           } finally {
               ContextUtil.popContext(context);
           }
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
       
   }
   
   /**
    * Returns Route Node Task Assignee value.
    * @param context
    * @param args
    * @return
    * @throws FrameworkException
    */
   public String getRouteNodeTaskAssigneeValue(Context context, String[] args) throws FrameworkException {
       try {
           Map programMap = (Map)JPO.unpackArgs(args);
           Map paramMap   = (Map)programMap.get("paramMap");
           HashMap requestMap = (HashMap)programMap.get("requestMap");
           
           String sLanguage = (String) paramMap.get("languageStr");
           String routeNodeId = (String)paramMap.get("relId");
           
           StringList selectables = new StringList(2);
           selectables.add(DomainRelationship.SELECT_TO_ID);
           selectables.add(Route.SELECT_ROUTE_TASK_USER);
           
           Map relValues = (Map) DomainRelationship.getInfo(context, new String[]{routeNodeId}, selectables).get(0);
           DomainObject dmoAssignee = new DomainObject ((String) relValues.get(DomainRelationship.SELECT_TO_ID));
           selectables.clear();
           selectables.add(SELECT_TYPE);
           selectables.add(SELECT_NAME);
           
           Map mapAssigneeInfo = dmoAssignee.getInfo (context, selectables);
           String routeNodeType = (String) mapAssigneeInfo.get(SELECT_TYPE);
           
           String rtu = (String) relValues.get(Route.SELECT_ROUTE_TASK_USER);
           String assignee  = routeNodeType.equals(TYPE_PERSON) ? PersonUtil.getFullName(context, (String) mapAssigneeInfo.get(SELECT_NAME)) :
                                     !UIUtil.isNullOrEmpty(rtu) ? getRoleGroupValue(context, sLanguage, rtu) :
                                                                  ComponentsUtil.i18nStringNow("emxComponents.Common.None", sLanguage);
           
           return assignee;
       } catch (Exception e) {
           throw new FrameworkException(e);
    }
   }
   
   /**
    * Range values to display in Route Node Task edit form.
    * @param context
    * @param args
    * @return
    * @throws FrameworkException
    */
   public Map getRouteNodeTaskAssigneeRangeValues(Context context, String[] args) throws FrameworkException {
       try {
           Map programMap = (Map)JPO.unpackArgs(args);
           Map paramMap   = (Map)programMap.get("paramMap");
           String sLanguage = (String) paramMap.get("languageStr");
           String objectId = (String)paramMap.get("objectId");
           
           StringList selectables = new StringList(3);
           StringList relSelectables = new StringList();
           
           selectables.add(SELECT_ID);
           selectables.add(SELECT_NAME);
           selectables.add(SELECT_TYPE);
           
           relSelectables.add(Route.SELECT_ROUTE_TASK_USER);
           
           Pattern typePattern = new Pattern(TYPE_PERSON);
           typePattern.addPattern(TYPE_ROUTE_TASK_USER);
           
           DomainObject domObj = new DomainObject(objectId);
           MapList routeNodeTasks = domObj.getRelatedObjects(context, 
                   RELATIONSHIP_ROUTE_NODE, typePattern.getPattern(),
                   selectables, relSelectables,
                   false, true, (short)1, 
                   EMPTY_STRING, EMPTY_STRING,
                   0);
           
           StringList rangeValues = new StringList(routeNodeTasks.size());
           StringList rangeDispay = new StringList(routeNodeTasks.size());
           
           StringList users = new StringList(routeNodeTasks.size());
           String rtuId = null;
           for (int i = 0; i < routeNodeTasks.size(); i++) {
               Map routeNode = (Map) routeNodeTasks.get(i);
               String routeNodeType = (String) routeNode.get(SELECT_TYPE);
               String id = (String) routeNode.get(SELECT_ID);
               
               String assigee = null;
               String assigneeDisplay = null;
               
               if(routeNodeType.equals(TYPE_PERSON)) {
                   assigee = "person#" + id + "#" + id;
                   assigneeDisplay = PersonUtil.getFullName(context, (String)routeNode.get(SELECT_NAME));
               } else {
                   String rtu = (String)routeNode.get(Route.SELECT_ROUTE_TASK_USER);
                   rtuId = UIUtil.isNullOrEmpty(rtuId) ? id : rtuId;
                   
                   boolean isRoleGroup = !UIUtil.isNullOrEmpty(rtu);
                   String rtuType = !isRoleGroup ? "none#" :
                                    rtu.startsWith("role_") ? "role#" : "group#";
                   
                   assigee =  rtuType + rtuId + "#" + rtu;
                   assigneeDisplay = isRoleGroup ? getRoleGroupValue(context, sLanguage, rtu) :
                                                   ComponentsUtil.i18nStringNow("emxComponents.Common.None", sLanguage);
               }
               if(!users.contains(assigee)) {
                   rangeValues.add(assigee);
                   rangeDispay.add(assigneeDisplay);
                   users.add(assigee);
               }
           }
           
           Map map = new HashMap(2);
           map.put("field_choices", rangeValues);
           map.put("field_display_choices", rangeDispay);
           return map; 
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }

   protected String getRoleGroupValue(Context context, String sLanguage, String roleGroup) throws FrameworkException {
       try {
           String taskAssignee = PropertyUtil.getSchemaProperty(context, roleGroup);
           boolean isRole = roleGroup.substring(0, roleGroup.indexOf("_")).equals("role");
           taskAssignee =  isRole ? i18nNow.getRoleI18NString(taskAssignee, sLanguage) :
                                    i18nNow.getAdminI18NString("Group", taskAssignee, sLanguage);
           
           String key = isRole ? "emxFramework.Common.Role" : "emxFramework.Common.Group";
           StringBuffer buffer = new StringBuffer(40);
           buffer.append(taskAssignee);
           buffer.append(" (").append(EnoviaResourceBundle.getProperty(context,"emxFrameworkStringResource",new Locale(sLanguage),key)).append(')');
           return buffer.toString();
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }
   
   /**
    * Range values for Route Action in Route Node Task Edit form.
    * @param context
    * @param args
    * @return
    * @throws FrameworkException
    */
   
   public Map getRouteNodeTaskRouteActionValues(Context context, String[] args) throws FrameworkException {
       try {
           Map programMap = (Map)JPO.unpackArgs(args);
           Map paramMap   = (Map)programMap.get("paramMap");
           String sLanguage = (String) paramMap.get("languageStr");
           
           DomainObject domObj = new DomainObject((String) paramMap.get("objectId"));
           String routeBasePurpose = domObj.getAttributeValue(context, ATTRIBUTE_ROUTE_BASE_PURPOSE);
           
           StringList validRangeValues = new StringList(3);
           if("Standard".equals(routeBasePurpose)) {
               validRangeValues.add("Approve");
               validRangeValues.add("Comment");
               validRangeValues.add("Notify Only");
           } else if("Approval".equals(routeBasePurpose)) {
               validRangeValues.add("Approve");
           } else if("Review".equals(routeBasePurpose)) {
               validRangeValues.add("Comment");
           }  

           StringList displayValue = new StringList(validRangeValues.size());
           for (int i = 0; i < validRangeValues.size(); i++) {
               displayValue.add(i18nNow.getRangeI18NString(ATTRIBUTE_ROUTE_ACTION, (String)validRangeValues.get(i), sLanguage));               
           }
           
           HashMap resultMap = new HashMap();
           resultMap.put("field_choices", validRangeValues);
           resultMap.put("field_display_choices", displayValue);
           return  resultMap;
           
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }
   
   /**
    * Update Program for Route Node Task details.
    * @param context
    * @param args
    * @return
    * @throws FrameworkException
    */
   @com.matrixone.apps.framework.ui.PostProcessCallable
   public HashMap updateRouteNodeTask(Context context, String[] args) throws FrameworkException {
       try {
           Map programMap = (Map)JPO.unpackArgs(args);
           Map paramMap   = (Map)programMap.get("paramMap");
           
           String routeId = (String) paramMap.get("objectId");
           DomainObject domObj = DomainObject.newInstance(context, routeId);
           String routeOwner = domObj.getAttributeValue(context, DomainConstants.ATTRIBUTE_ORIGINATOR);
           
           if(TYPE_ROUTE_TEMPLATE.equals(domObj.getInfo(context, SELECT_TYPE))) {
               return (HashMap) JPO.invoke(context, "emxRouteTemplate", null, "updateRouteNodeTaskForRouteTemplate", args, HashMap.class) ;
           }
           
           String relId = (String) paramMap.get("relId");
           DomainRelationship domRel = DomainRelationship.newInstance(context, relId);
           
           HashMap requestMap = (HashMap)programMap.get("requestMap");
           String sLanguage = (String) requestMap.get("languageStr");
           Locale locale = UIUtil.isNullOrEmpty(sLanguage) ? context.getLocale() : new Locale(sLanguage);

           HashMap resultsMap = new HashMap();
           Map newValues = new HashMap(3);
           
           String selAttrAssigneeDueDate = getAttributeSelect(ATTRIBUTE_ASSIGNEE_SET_DUEDATE);
           String selAttrDueDateOffcet = getAttributeSelect(ATTRIBUTE_DUEDATE_OFFSET);
           
           StringList selectables = new StringList(5);
           selectables.add(DomainRelationship.SELECT_TO_ID);
           selectables.add(Route.SELECT_ROUTE_TASK_USER);
           selectables.add(Route.SELECT_SCHEDULED_COMPLETION_DATE);
           selectables.add(selAttrAssigneeDueDate);
           selectables.add(selAttrDueDateOffcet);
           
           Map relValues = (Map) DomainRelationship.getInfo(context, new String[]{relId}, selectables).get(0);

           String dueDate    = (String)requestMap.get("DueDate");
           String dueTime    = (String)requestMap.get("routeTime");
           
           boolean dueDateEmpty = UIUtil.isNullOrEmpty(dueDate);
           // Owner and assignee can change (if assignee set due date)
           boolean checkDueDate = ("Yes".equals(relValues.get(selAttrAssigneeDueDate)) && !dueDateEmpty &&
                                  UIUtil.isNullOrEmpty((String) relValues.get(selAttrDueDateOffcet)) || context.getUser().equalsIgnoreCase(routeOwner));
           
           if(dueDateEmpty && checkDueDate) {
               //resultsMap.put("Message", ComponentsUtil.i18nStringNow("emxComponents.AssignTaskDialog.NotCalendarOption", sLanguage));
               resultsMap.put("Message", ComponentsUtil.i18nStringNow("emxComponents.InboxTask.AlertDueDate", sLanguage));
               return resultsMap;
           } 
           if(!locale.toString().startsWith("en")){
        	   dueDate = eMatrixDateFormat.getFormattedInputDate(dueDate, Double.valueOf((String)requestMap.get("timeZone")), i18nNow.getLocale(sLanguage));
           }
           if(checkDueDate && ComponentsUIUtil.isPastDate(eMatrixDateFormat.getJavaDate(dueDate), dueTime)) {
               resultsMap.put("Message", ComponentsUtil.i18nStringNow("emxComponents.EditTaskDetails.DateMessage", sLanguage)); 
               return resultsMap;
           }
           
           
           if(checkDueDate) {
               String taskScheduledDate = (String) relValues.get(Route.SELECT_SCHEDULED_COMPLETION_DATE);
               String timeZone = (String)requestMap.get("timeZone");
               double clientTZOffset   = (new Double(timeZone)).doubleValue();
               
               dueDate = (String) eMatrixDateFormat.getFormattedDisplayDate(dueDate, clientTZOffset);
               
               String taskScheduledDateNew   =  eMatrixDateFormat.
                                                   getFormattedInputDateTime(context,dueDate, dueTime, clientTZOffset, locale);
               if(!taskScheduledDate.equals(taskScheduledDateNew)) {
                   newValues.put(ATTRIBUTE_SCHEDULED_COMPLETION_DATE, taskScheduledDateNew);
               }
           }               
                  
           DomainObject dmoAssignee = new DomainObject ((String) relValues.get(DomainRelationship.SELECT_TO_ID));
           selectables.clear();
           selectables.add(SELECT_TYPE);
           selectables.add(SELECT_NAME);
           selectables.add(SELECT_ID);
           
           Map mapAssigneeInfo = dmoAssignee.getInfo (context, selectables);
           String routeNodeType = (String) mapAssigneeInfo.get(SELECT_TYPE);

           boolean isConnectedToRTU = routeNodeType.equals(TYPE_ROUTE_TASK_USER);
           String currentAssignee = (String) (isConnectedToRTU ? relValues.get(Route.SELECT_ROUTE_TASK_USER) : mapAssigneeInfo.get(SELECT_ID));
           
           /**
            * newTaskAssignee will be in the following format
            * Person -> person#PERSON_OBJ_ID#PERSON_OBJ_ID 
            * Role   -> role#RTU_OBJ_ID#Role_SYMB_NAME
            * Group  -> group#RTU_OBJ_ID#GROUP_SYMB_NAME
            * None   -> none#RTU_OBJ_ID#
            * 
            * 0th Element -> Assignee Type
            * 1st Element -> Obj to be connected -> RTU/Person Id
            * 2nd Element -> Role/Group symbolic name
            *                Empty for None
            *                Person Id for Person
            */
           String newTaskAssignee    = (String)requestMap.get("Assignee");
           StringList newTaskAssigneeInfo = FrameworkUtil.split(newTaskAssignee, "#");
           String newAssigneeValue = (String) newTaskAssigneeInfo.get(2);
           if(!currentAssignee.equals(newAssigneeValue)) {
               String newAssigneeType = (String) newTaskAssigneeInfo.get(0);
               String newAssigneeID = (String) newTaskAssigneeInfo.get(1);
               
               boolean connectToRTU = !"person".equals(newAssigneeType);
               
               DomainRelationship.setToObject(context, relId, new DomainObject(newAssigneeID));
               newValues.put(ATTRIBUTE_ROUTE_TASK_USER, connectToRTU ? newAssigneeValue : EMPTY_STRING);
           }
           
           newValues.put(ATTRIBUTE_ROUTE_ACTION, (String) requestMap.get("Action"));
           newValues.put(ATTRIBUTE_ALLOW_DELEGATION, (String) requestMap.get("AllowDelegation"));
           newValues.put(ATTRIBUTE_ROUTE_INSTRUCTIONS, (String) requestMap.get("Instructions"));

           DomainRelationship.setAttributeValues(context, relId, newValues);
           
           return resultsMap;
       } catch (Exception e) {
           throw new FrameworkException(e);
       }
   }
   
   /**
    * This is dummy update program for Route Node Task edit form.
    * We will be modifying the attributes in the post process JPO.
    * @param context
    * @param args
    * @throws FrameworkException
    */
   public void updateRouteNodeTaskDummy(Context context, String[] args) throws FrameworkException {
       return;
   }
   
   /**
    * Program HTML field for Route Node Task, Due Date field.
    * @param context
    * @param args
    * @return
    * @throws Exception
    */
   public String getRouteNodeTaskDueDateField(Context context, String[] args)throws Exception
   {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       Map paramMap   = (Map)programMap.get("paramMap");
       HashMap requestMap = (HashMap)programMap.get("requestMap");
       
       String selTaskDueDate         = getAttributeSelect(DomainObject.ATTRIBUTE_SCHEDULED_COMPLETION_DATE);
       String selDueDateOffset       = getAttributeSelect(DomainObject.ATTRIBUTE_DUEDATE_OFFSET);
       String selDueDateOffsetFrom   = getAttributeSelect(DomainObject.ATTRIBUTE_DATE_OFFSET_FROM);
       String selAssigneeSetDueDate   = getAttributeSelect(DomainObject.ATTRIBUTE_ASSIGNEE_SET_DUEDATE);
       
       StringList relSelectables = new StringList();
       relSelectables.addElement(selTaskDueDate);
       relSelectables.addElement(selDueDateOffset);
       relSelectables.addElement(selDueDateOffsetFrom);
       relSelectables.addElement(selAssigneeSetDueDate);
       String objectId = (String)paramMap.get("objectId");
       String relId = (String) paramMap.get("relId");
       String languageStr = (String)paramMap.get("languageStr");
				
       MapList mlRouteNodeInfo = DomainRelationship.getInfo(context, new String[]{relId}, relSelectables);
       String taskDueDate = (String)((Map)mlRouteNodeInfo.get(0)).get(selTaskDueDate);
	   String dueDateOffset = (String)((Map)mlRouteNodeInfo.get(0)).get(selDueDateOffset);
	   String dueDateOffsetFrom = (String)((Map)mlRouteNodeInfo.get(0)).get(selDueDateOffsetFrom);
	   String assigneeSetDueDate = (String)((Map)mlRouteNodeInfo.get(0)).get(selAssigneeSetDueDate);
	   boolean bDueDateEmpty  = UIUtil.isNullOrEmpty(taskDueDate) ? true : false;
       boolean bDeltaDueDate = (!UIUtil.isNullOrEmpty(dueDateOffset) && bDueDateEmpty) ? true : false;
       StringBuffer sb = new StringBuffer();
       if(UIUtil.isNotNullAndNotEmpty(assigneeSetDueDate) && "Yes".equalsIgnoreCase(assigneeSetDueDate)){
    	   sb.append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.AssignTasksDialog.AssigneeDueDate"));
       }else if(!bDeltaDueDate){
           sb.append(taskDueDate);
				}else{
           	
           sb.append(dueDateOffset).append(" ").append(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource", new Locale(languageStr),"emxComponents.common.DaysFrom")).
           append(" ").append(i18nNow.getRangeI18NString( DomainObject.ATTRIBUTE_DATE_OFFSET_FROM, dueDateOffsetFrom,languageStr));
		}
           
       return sb.toString();
   }
   
   /**
    * Post process JPO for Delete Routes
    * @param context
    * @param args
    * @return
    * @throws FrameworkException
    */       
       @com.matrixone.apps.framework.ui.PostProcessCallable
       public Map deleteSelectedRoutes(Context context, String args[]) throws FrameworkException {
           try {
               Map programMap = (Map)JPO.unpackArgs(args);
               Map requestValuesMap = (Map) programMap.get("requestValuesMap");
               
               Map returnMap = new HashMap();
               
               String languageStr	= (String) requestValuesMap.get("languageStr");
               String rowIds         =  requestValuesMap.get("rowIds") != null ? 
                                        ((String[])requestValuesMap.get("rowIds"))[0] : null;
               String sMessage       =  requestValuesMap.get("NotificationMessage") != null ? 
                                         ((String[])requestValuesMap.get("NotificationMessage"))[0] : "";

                                        
               if(UIUtil.isNullOrEmpty(rowIds)) {
                   return returnMap;
               }
               
               String notificationSub = ComponentsUtil.i18nStringNow("emxComponents.DeleteRoute.DeleteNotification", languageStr);
               
               SelectList selectables = new SelectList(3);
               selectables.addId();
               selectables.addType();
               selectables.addName();
               
               Route route = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
               
               String[] routeIds = StringUtils.split(rowIds, ",");
               MapList routeInfo = DomainObject.getInfo(context, routeIds, selectables);
               for (int i = 0; i < routeIds.length; i++) {
                   route.setId(routeIds[i]);
                   Map routeDetails = (Map) routeInfo.get(i);
                   String name = (String) routeDetails.get(SELECT_NAME);
                   String type = i18nNow.getAdminI18NString("Type", (String) routeDetails.get(SELECT_TYPE), languageStr);
                   StringBuffer buffer = new StringBuffer(50);
                   buffer.append(type).append(" ").append(name).append(" ").append(notificationSub);
                   Access  access = route.getAccessMask(context);
       	           boolean deleteAccessFlag=access.hasDeleteAccess();
                   try {
                	   //When we attached newly created route to any object , we are pushing the context.and user is able to create the route on that object.
                	   //But while deleting user we were not pushing the context.So , first checking whether user having delete access or not and then pushing the context.IR-490696-3DEXPERIENCER2015x 
                	   if (deleteAccessFlag) {
                       route.deleteRoute(context, sMessage, buffer.toString());
                	   } else {
                		   String message = EnoviaResourceBundle.getProperty(context,
                                   "emxFrameworkStringResource",
                                   context.getLocale(),
                                   "emxFramework.Alert.CannotDeleteRoute");
                		   message=UIExpression.substituteValues(context, message, routeIds[i]);
                		    returnMap.put("Message", message);
                		    return returnMap;
                	   }
                   } catch (Exception e) {
                       returnMap.put("Message", e.getMessage());
                   }
               }
               return returnMap;
           } catch (Exception e) {
               throw new FrameworkException(e);
           }
       }
       
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getRouteAssigneesToSelect(Context context, String[] args) throws Exception{
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap requestMap = (HashMap)programMap.get("RequestValuesMap");
       String[] personId  = (String[]) requestMap.get("personList");
       String[] rolesList = (String[]) requestMap.get("roleList");
       String[] groupsList = (String[]) requestMap.get("groupList");
       String[] noneOption =  (String[]) requestMap.get("showNone");
       
       StringList persons = FrameworkUtil.split(personId[0], "|");
       StringList roles = FrameworkUtil.split(rolesList[0], "|");
       StringList groups = FrameworkUtil.split(groupsList[0], "|");
       boolean showNoneOption = noneOption == null || !"false".equalsIgnoreCase(noneOption[0]);
       
       
       MapList detailsMap = new MapList(persons.size() + roles.size() + groups.size());
       for (int i = 0; i < persons.size(); i++) {
          Map map = new HashMap();
          DomainObject person = new DomainObject((String)persons.get(i));
          map.put(SELECT_ID, persons.get(i)+"~"+person.getName(context));
          map.put("UserType", "person");
          detailsMap.add(map);
       }
       for (int i = 0; i < roles.size(); i++) {
          Map map = new HashMap();
          map.put(SELECT_ID, "Role~"+(String)roles.get(i));
          map.put("UserType", "role");
          detailsMap.add(map);
       }
       for (int i = 0; i < groups.size(); i++) {
          Map map = new HashMap();
          map.put(SELECT_ID, "Group~"+groups.get(i));
          map.put("UserType", "group");
          detailsMap.add(map);
       }
       if(showNoneOption) {
           Map map = new HashMap();
           map.put(SELECT_ID, "none~none");
           map.put("UserType", "none");
           detailsMap.add(map);
       }
       return detailsMap;
}
   
	public StringList getUserSummaryTableName(Context context, String[] args) throws FrameworkException {
	    try {
	        Map programMap = (Map) JPO.unpackArgs(args);
	        MapList objs =  (MapList) programMap.get("objectList");
	        HashMap paramList = (HashMap) programMap.get("paramList");
	        String languageStr = (String) paramList.get("languageStr");
	        StringList colData = new StringList(objs.size());
	        
	        for (int i = 0; i < objs.size(); i++) {
	            Map map = (Map) objs.get(i);
	            String id = (String) map.get(SELECT_ID);
	            String userType = (String) map.get("UserType");
	            if("person".equalsIgnoreCase(userType)) {
	                StringTokenizer st = new StringTokenizer(id,"~");
	                while(st.hasMoreTokens()){
	                    DomainObject person = new DomainObject((String)st.nextToken());
	                    colData.add(PersonUtil.getFullName(context,(String)st.nextToken()));
	                }
	            } else if("role".equalsIgnoreCase(userType)) {
	                StringTokenizer st = new StringTokenizer(id,"~");
	                while(st.hasMoreTokens()){
	                    String id1 = st.nextToken();
	                    colData.add(i18nNow.getRoleI18NString(st.nextToken(), languageStr));
	                }
	            } else if("group".equalsIgnoreCase(userType)) {
	                StringTokenizer st = new StringTokenizer(id,"~");
	                while(st.hasMoreTokens()){
	                    String id1 = st.nextToken();
	                    colData.add(i18nNow.getAdminI18NString("Group",st.nextToken(),languageStr));
	                }
	            } else {
                    colData.add(ComponentsUtil.i18nStringNow("emxComponents.Common.None", languageStr));
                }
	        }
	        
	        return colData;
	    } catch (Exception e) {
	        throw new FrameworkException(e);
	    } 
	}
   
	public StringList getUserSummaryTableType(Context context, String[] args) throws FrameworkException {
	    try {
	        Map programMap = (Map) JPO.unpackArgs(args);
	        MapList objs =  (MapList) programMap.get("objectList");
	        HashMap paramList = (HashMap) programMap.get("paramList");
	        String languageStr = (String) paramList.get("languageStr");
	        StringList colData = new StringList(objs.size());
	        
	        for (int i = 0; i < objs.size(); i++) {
	            Map map = (Map) objs.get(i);
	            String id = (String) map.get(SELECT_ID);
	            String userType = (String) map.get("UserType");
	            if("person".equalsIgnoreCase(userType)) {
	                colData.add(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.Person"));   
	            } else if("role".equalsIgnoreCase(userType)) {
	                colData.add(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.Role"));
	            } else if("group".equalsIgnoreCase(userType)) {
	                colData.add(EnoviaResourceBundle.getProperty(context,"emxComponentsStringResource",new Locale(languageStr),"emxComponents.Common.Group"));
	            } else {
                    colData.add("");
                }
	        } 
	        
	        return colData;
	    } catch (Exception e) {
	        throw new FrameworkException(e);
	    } 
	}
   
	public StringList getUserSummaryTableOrganization(Context context, String[] args) throws FrameworkException {
	    try {
	        Map programMap = (Map) JPO.unpackArgs(args);
	        MapList objs =  (MapList) programMap.get("objectList");
	        StringList colData = new StringList(objs.size());
	        
	        for (int i = 0; i < objs.size(); i++) {
	            Map map = (Map) objs.get(i);
	            String id = (String) map.get(SELECT_ID);
	            String userType = (String) map.get("UserType");
	            if("person".equalsIgnoreCase(userType)) {
	                Person per = new Person(id);
	                colData.add(per.getCompany(context).getInfo(context, SELECT_NAME));
	            } else {
	                colData.add("");
	            } 
	        }
	        return colData;
	    } catch (Exception e) {
	        throw new FrameworkException(e);
	    } 
	}

}

