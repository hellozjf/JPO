/* emxQualityBase.java
*
*   Copyright (c) 1992-2016 Dassault Systemes.
*   All Rights Reserved.
*   This program contains proprietary and trade secret information of MatrixOne,
*   Inc.  Copyright notice is precautionary only
*   and does not evidence any actual or intended publication of such program
*
*   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.17.2.1 Thu Dec  4 07:55:02 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.17 Wed Oct 22 15:49:51 2008 przemek Experimental przemek $
*/

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import matrix.db.AccessConstants;
import matrix.db.AttributeType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.DebugUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.ProgramCentralConstants;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.Quality;
import com.matrixone.apps.program.QualityMetricRelationship;

/**
 * The <code>emxQualityBase</code> class represents the Quality JPO
 * functionality for the AEF type.
 *
 * @version AEF 9.5.4.1 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxQualityBase_mxJPO extends com.matrixone.apps.program.Quality
{
    // Create an instant of emxUtil JPO
    protected emxProgramCentralUtil_mxJPO emxProgramCentralUtilClass = null;

    /** Id of the Access List Object for this Project. */
    protected DomainObject _accessListObject = null;

    /** The project access list id relative to project. */
    static protected final String SELECT_PROJECT_ACCESS_LIST_ID =
            "to[" + RELATIONSHIP_PROJECT_ACCESS_KEY + "].from.id";

    /** state "Initiated" for the "Quality" policy. */
    public static final String STATE_QUALITY_INITIATED =
           PropertyUtil.getSchemaProperty("policy",
                                           POLICY_QUALITY,
                                           "state_Initiated");

    /** state "Controlled" for the "Quality" policy. */
    public static final String STATE_QUALITY_CONTROLLED =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_QUALITY,
                                           "state_Controlled");

    /**
     * Constructs a new emxQuality JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the id
     * @throws Exception if the operation fails
     * @since AEF 9.5.4.1
     */
    public emxQualityBase_mxJPO (Context context, String[] args)
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
     * Get the access list object for this Project.
     *
     * @param context the eMatrix <code>Context</code> object
     * @return DomainObject access list object
     * @throws Exception if the operation fails
     * @since AEF 9.5.4.1
     */
    protected DomainObject getAccessListObject(Context context)
        throws Exception
    {
        if (_accessListObject == null)
        {
            //System.out.println("Retrieving quality security ID..." +
            //                   (new Date().getTime()));
            String accessListID = getInfo(context,
                                          SELECT_PROJECT_ACCESS_LIST_ID);
            if (accessListID != null && ! "".equals(accessListID))
            {
                _accessListObject = DomainObject.newInstance(context);
                _accessListObject.setId(accessListID);
            }
        }
        return _accessListObject;
    }

    /**
     * This function verifies the user's permission for the given quality.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *      PROJECT_MEMBER to see if the context user is a project member, <BR>
     *      PROJECT_LEAD to see if the context user is a project lead, <BR>
     *      PROJECT_OWNER to see if the context user is the project owner, <BR>
     * @return boolean true or false
     * @throws Exception if the operation fails
     * @since AEF 9.5.4.1
     */
    public boolean hasAccess(Context context, String args[])
        throws Exception
    {
        //System.out.println(new Date().getTime());

        //program[emxQuality PROJECT_MEMBER -method hasAccess
        //            -construct ${OBJECTID}] == true

        boolean access = false;
        for (int i = 0; i < args.length; i++)
        {
            String accessType = args[i];
            if ("PROJECT_MEMBER".equals(accessType) ||
                "PROJECT_LEAD".equals(accessType) ||
                "PROJECT_OWNER".equals(accessType))
            {
                DomainObject accessListObject = getAccessListObject(context);

                if (accessListObject != null)
                {
                    int iAccess;
                    if ("PROJECT_MEMBER".equals(accessType))
                    {
                        iAccess = AccessConstants.cExecute;
                    }
                    else if ("PROJECT_LEAD".equals(accessType))
                    {
                        iAccess = AccessConstants.cModify;
                    }
                    else
                    {
                        iAccess = AccessConstants.cOverride;
                    }
                    //System.out.println("Checking access..." +
                    //                   (new Date().getTime()));
                    if (accessListObject.checkAccess(context, (short) iAccess))
                    {
                        access = true;
                    }
                }
            }
            if (access == true)
            {
                break;
            }
        }
        //System.out.println(new Date().getTime());
        //System.out.println(getId() + " : " + access);
        return access;
    }

    /**
     * This function checks if the Quality object
     * being promoted has a "Controlled" metric and
     * that it is the last metric that was created.
     * If this is not the case, the promotion is
     * denied.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String object id
     *        1 - String next state
     * @return int based on success or failure
     * @throws Exception if operation fails
     * @since AEF 9.5.4.1
     */
    public int triggerPromoteCheck(Context context, String[] args)
        throws Exception
    {
        // get values from args
        String objectId = args[0];
        String nextState = args[1];

        // get event to make sure it even is "Promote"
        String cmd = "get env EVENT";
        String sEvent = MqlUtil.mqlCommand(context, cmd , true);

        // set Quality id
        setId(objectId);

        // not very necessary because this method should
        // never be invoked unless promoting a quality!
        if(sEvent.equals("Promote"))
        {
            // create boolean to hold whether to do promote or not
            boolean promoteQuality = false; // do not promote until check finds "Controlled" metric

            // declare variables needed for getQualityMetrics call
            StringList relSelects = new StringList(1);
            String relWhere = "";

            // create a Quality Metric Relationship object
            com.matrixone.apps.program.QualityMetricRelationship qualityMetric;
            qualityMetric = new com.matrixone.apps.program.QualityMetricRelationship();

            // add necessary relSelects
            relSelects.add(qualityMetric.SELECT_METRIC_SOURCE);

            // get quality metrics associated with the quality object
            MapList MetricList = getQualityMetrics(context, relSelects, relWhere);

            // Variables needed to determine whether "Controlled" metric exists
            String sControlled = "Controlled";  // may need to be internationalized
            String metricSource = "";

            // create iterator to loop through
            Iterator metricListItr = MetricList.iterator();

            // Loop through all of the metrics for the current quality
            while (metricListItr.hasNext() && !promoteQuality)
            {
                // get next metric
                Map metricMap = (Map)metricListItr.next();

                // set promoteQuality based on current metric
                metricSource = (String)metricMap.get(qualityMetric.SELECT_METRIC_SOURCE);
                promoteQuality = metricSource.equals(sControlled);

            }  // end while metricListItr.hasNext()

            // now, exit the function appropriately (based on promoteQuality variable)
            // if promoteQuality was set to false, the promotion is denied!
            // (an error message will alert the user of the problem)
            if (promoteQuality)
            {
                return 0;  // promote!
            }
            else
            {
                String sErrMsg = "emxProgramCentral.Quality.triggerPromoteCheck.Message";
                String sKey[] = {"qualityName", "sControlled"};
                String sValue[] = {getName(), sControlled};
                String companyName = null;
                sErrMsg  = emxProgramCentralUtilClass.getMessage(context,
                                sErrMsg, sKey, sValue, companyName);
                MqlUtil.mqlCommand(context, "notice " + sErrMsg );
                return 1;
            }
        }
        // program should never get here unless this method gets called without a Quality promote event!
        DebugUtil.debug(new Date() + " | Exiting triggerPromoteCheck function - no quality promote event found");
        return 0;
    }

    /**
     * Deletes a quality object and the metric object related to it.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the object id
     * @return int based on success or failure
     * @throws FrameworkException if the operation fails
     * @since AEF 9.5.4.1
     */
    public int triggerDeleteOverride(Context context, String [] args)
        throws FrameworkException
    {
        String objectId = args[0];
        setId(objectId);

        // Delete the Quality's Metric object before deleting the Quality
        String metricId = getInfo(context, SELECT_METRIC_ID);
        DomainObject object = DomainObject.newInstance(context);
        object.setId(metricId);
        try
        {
          object.deleteObject(context, true);
        }
        catch(Exception e)
        {
          DebugUtil.debug("Exception Quality triggerDeleteOverride- ",
                          e.getMessage());
          throw new FrameworkException(e);
        }

        return 0;  // Matrix will do the Quality delete
    }

    /****************************************************************************************************
     *       Methods for Config Table Conversion Task
     ****************************************************************************************************/
    /**
     * gets the list of All Quality objects for the Project
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return MapList containing the ids of Quality objects
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllProjectQuality(Context context, String[] args)
      throws Exception
    {
        return getProjectQuality(context, args, null);
    }

    /**
     * gets the list of active Quality objects for the Project
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return MapList containing the ids of Quality objects
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getActiveProjectQuality(Context context, String[] args)
      throws Exception
    {
        return getProjectQuality(context, args, STATE_QUALITY_ACTIVE);
    }

    /**
     * gets the list of Controlled  Quality objects for the Project
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return MapList containing the ids of Quality objects
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getControlledProjectQuality(Context context, String[] args)
      throws Exception
    {
        return getProjectQuality(context, args, STATE_QUALITY_CONTROLLED);
    }

    /**
     * gets the list of Quality objects for the project
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    objectId   - String containing the objectID
     * @return MapList containing the ids of Quality objects
     * @throws Exception if the operation fails
     * @since PMC 10-6
     */

    public MapList getProjectQuality(Context context,String[] args,String selectState)
      throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        MapList qualityList = null;
        Map mpQuality;
        String strQualityState = DomainConstants.EMPTY_STRING;
        try
        {
            String busWhere = null;
            com.matrixone.apps.program.Quality quality =
               (com.matrixone.apps.program.Quality) DomainObject.newInstance(context,
                   DomainConstants.TYPE_QUALITY, "PROGRAM");
            com.matrixone.apps.program.ProjectSpace project =
               (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                   DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

            // Build the where clause based on the filter request
            if (selectState == null || "null".equalsIgnoreCase(selectState))
            {
                busWhere = null;
            } //end if all is the select state
            else
            {
                if (STATE_QUALITY_CONTROLLED.equalsIgnoreCase(selectState))
                {
                    busWhere = "current==\"" + STATE_QUALITY_CONTROLLED + "\"";
                }
                else if (!(STATE_QUALITY_CONTROLLED.equalsIgnoreCase(selectState)))
                {
                    busWhere = "current==\"" + STATE_QUALITY_ACTIVE + "\"";
                }
            } //end if all is not select state
            StringList busSelects = new StringList(1);
            busSelects.add(quality.SELECT_ID);
            busSelects.add(quality.SELECT_CURRENT);
            project.setId(objectId);
            qualityList = quality.getQualityItems(context, project, busSelects, busWhere);
            
            for(int i=0; i< qualityList.size(); i++) {
            	mpQuality = (Map)qualityList.get(i);
            	strQualityState = (String)mpQuality.get(SELECT_CURRENT);
            	if(null !=(strQualityState) && !"".equals(strQualityState) && (STATE_QUALITY_CONTROLLED).equals(strQualityState))
            	{
            		mpQuality.put("disableSelection","true");
            	}
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            return qualityList;
        }
    }


     /**
      * this method gets the problem statements of quality objects
      * @param args holds the following input arguments:
      * objectList - Contains a MapList of Maps which contains object names
      * @param context the eMatrix <code>Context</code> object
      * @return Vector containing problem statements of quality objects as String
      * @throws Exception if the operation fails
      * @since PMC 10-6
      */

     public Vector getProblemStatement(Context context, String[] args)
         throws Exception
     {
         Vector problemStatements = new Vector();
         try
         {
             com.matrixone.apps.program.Quality quality =
                (com.matrixone.apps.program.Quality) DomainObject.newInstance(context,
                    DomainConstants.TYPE_QUALITY, "PROGRAM");
             com.matrixone.apps.program.ProjectSpace project =
                (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                    DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

             HashMap programMap = (HashMap) JPO.unpackArgs(args);
             MapList objectList = (MapList) programMap.get("objectList");
             Map objectMap = null;

             Iterator objectListIterator = objectList.iterator();
             String[] objIdArr = new String[objectList.size()];
             int arrayCount = 0;
             while (objectListIterator.hasNext())
             {
                 objectMap = (Map) objectListIterator.next();
                 objIdArr[arrayCount] = (String) objectMap.get(quality.SELECT_ID);
                 arrayCount++;
             }

             MapList actionList = DomainObject.getInfo(context,
                    objIdArr, new StringList(quality.SELECT_PROBLEM_STATEMENT));
             Iterator actionsListIterator = actionList.iterator();

             while (actionsListIterator.hasNext())
             {
                 objectMap = (Map) actionsListIterator.next();
                 String problemStatement = (String) objectMap.get(quality.SELECT_PROBLEM_STATEMENT);
                 problemStatements.add(problemStatement);
             }
                 //ends while
         }
         catch (Exception ex)
         {
             throw ex;
         }
         finally
         {
             return problemStatements;
         }
     }

     /**
      * this method gets the Goals for the Project Quality objects
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the following input arguments:
      * objectList - Contains a MapList of Maps which contains object names
      * @return Vector containing the Goals as String
      * @throws Exception if the operation fails
      * @since PMC 10-6
      */

     public Vector getGoal(Context context, String[] args)
         throws Exception
     {
           Vector goals = new Vector();
           try
           {
               com.matrixone.apps.program.Quality quality =
                  (com.matrixone.apps.program.Quality) DomainObject.newInstance(context,
                      DomainConstants.TYPE_QUALITY, "PROGRAM");
               com.matrixone.apps.program.ProjectSpace project =
                  (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                      DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

               HashMap programMap = (HashMap) JPO.unpackArgs(args);
               MapList objectList = (MapList) programMap.get("objectList");
               Map objectMap = null;
               Iterator objectListIterator = objectList.iterator();
               String[] objIdArr = new String[objectList.size()];
               int arrayCount = 0;
               while (objectListIterator.hasNext())
               {
                   objectMap = (Map) objectListIterator.next();
                   objIdArr[arrayCount] = (String) objectMap.get(quality.SELECT_ID);
                   arrayCount++;
               }

               MapList actionList = DomainObject.getInfo(context,
                      objIdArr, new StringList(quality.SELECT_GOAL));
               Iterator actionsListIterator = actionList.iterator();

               while (actionsListIterator.hasNext())
               {
                   objectMap = (Map) actionsListIterator.next();
                   String goal = (String) objectMap.get(quality.SELECT_GOAL);
                   goals.add(goal);
               }//ends while
           }
           catch (Exception ex)
           {

               throw ex;
           }
           finally
           {
               return goals;
           }
       }

       /**
        * this method gets the data type for the Project Quality objects
        *
        * @param context the eMatrix <code>Context</code> object
        * @param args holds the following input arguments:
        * objectList - Contains a MapList of Maps which contains object names
        * @return Vector containing the Goals as String
        * @throws Exception if the operation fails
        * @since PMC 10-6
        */

       public Vector getDataType(Context context, String[] args)
               throws Exception
       {
           Vector dataTypes = new Vector();
           try
           {
               com.matrixone.apps.program.Quality quality =
                  (com.matrixone.apps.program.Quality) DomainObject.newInstance(context,
                      DomainConstants.TYPE_QUALITY, "PROGRAM");
               com.matrixone.apps.program.ProjectSpace project =
                  (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                      DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

               HashMap programMap = (HashMap) JPO.unpackArgs(args);
               MapList objectList = (MapList) programMap.get("objectList");
               Map objectMap = null;
               Iterator objectListIterator = objectList.iterator();
               String[] objIdArr = new String[objectList.size()];
               String language = context.getSession().getLanguage();
               int arrayCount = 0;
               while (objectListIterator.hasNext())
               {
                   objectMap = (Map) objectListIterator.next();
                   objIdArr[arrayCount] = (String) objectMap.get(quality.SELECT_ID);
                   arrayCount++;
               }

               MapList actionList = DomainObject.getInfo(context,
                      objIdArr, new StringList(quality.SELECT_QUALITY_TYPE));
               Iterator actionsListIterator = actionList.iterator();

               while (actionsListIterator.hasNext())
               {
                   objectMap = (Map) actionsListIterator.next();
                   // Internationalize the selected Range value
                   String dataType = i18nNow.getRangeI18NString(quality.ATTRIBUTE_QUALITY_TYPE,(String) objectMap.get(quality.SELECT_QUALITY_TYPE),language);
                   dataTypes.add(dataType);
               } //ends while
           }
           catch (Exception ex)
           {
               throw ex;
           }
           finally
           {
               return dataTypes;
           }
      }

      /**
       * this method gets the Sigma Goal for the Project Quality objects
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following input arguments:
       * objectList - Contains a MapList of Maps which contains object names
       * @return Vector containing the Goals as String
       * @throws Exception if the operation fails
       * @since PMC 10-6
       */

      public Vector getSigmaGoal(Context context, String[] args)
               throws Exception
      {
          Vector sigmaGoals = new Vector();
          try
          {
              com.matrixone.apps.program.Quality quality =
                 (com.matrixone.apps.program.Quality) DomainObject.newInstance(context,
                     DomainConstants.TYPE_QUALITY, "PROGRAM");
              com.matrixone.apps.program.ProjectSpace project =
                 (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                     DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

              HashMap programMap = (HashMap) JPO.unpackArgs(args);
              MapList objectList = (MapList) programMap.get("objectList");
              Map objectMap = null;
              Iterator objectListIterator = objectList.iterator();
              String[] objIdArr = new String[objectList.size()];
              int arrayCount = 0;
              String sigmaGoal = "";
              while (objectListIterator.hasNext())
              {
                  objectMap = (Map) objectListIterator.next();
                  objIdArr[arrayCount] = (String) objectMap.get(quality.SELECT_ID);
                  arrayCount++;
              }
              MapList actionList = DomainObject.getInfo(context,
                     objIdArr,new StringList(quality.SELECT_QUALITY_METRIC_SIGMA));
              Iterator actionsListIterator = actionList.iterator();

              while (actionsListIterator.hasNext())
              {
                  objectMap = (Map) actionsListIterator.next();
                  Object sigmaObject = (StringList) objectMap.get(quality.SELECT_QUALITY_METRIC_SIGMA);
                  if (sigmaObject != null)
                  {
                      //quality only has one metric
                      if ((sigmaObject instanceof String) == true)
                      {
                          sigmaGoal = (String)sigmaObject;
                      }
                      //Else, quality has two or more metrics
                      else
                      {
                          StringList sigmaList = (StringList)sigmaObject;
                          int sigmaLimit=sigmaList.size()-1;
                          int i = 0;
                          //Loop through titleList and find the first title and the last title
                          while (i <= sigmaLimit)
                          {
                              //If the current title is equal to the largest title, then
                              //the most current metric has been found
                              String titleString = (String)sigmaList.elementAt(i);
                              sigmaGoal = (String)sigmaList.elementAt(i);
                              i++;
                          } //end while (i <= titleLimit)
                      } //end else quality has two or more metrics
                  }  //end if sigmaObject and titleObject != null
                  sigmaGoals.add(sigmaGoal);
              }
               //ends while
          }
          catch (Exception ex)
          {
              throw ex;
          }
          finally
          {
              return sigmaGoals;
          }
      }

      /**
       * this method gets the Sigma Current for the Project Quality objects
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following input arguments:
       *    objectList - Contains a MapList of Maps which contains object names
       * @return Vector containing the Goals as String
       * @throws Exception if the operation fails
       * @since PMC 10-6
       */

      public Vector getSigmaCurrent(Context context, String[] args)
               throws Exception
      {
          Vector sigmaCurrentValues = new Vector();
          try
          {
              com.matrixone.apps.program.Quality quality =
                 (com.matrixone.apps.program.Quality) DomainObject.newInstance(context,
                     DomainConstants.TYPE_QUALITY, "PROGRAM");
              com.matrixone.apps.program.ProjectSpace project =
                 (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
                     DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");

              HashMap programMap = (HashMap) JPO.unpackArgs(args);
              MapList objectList = (MapList) programMap.get("objectList");
              Map objectMap = null;
              Iterator objectListIterator = objectList.iterator();
              String[] objIdArr = new String[objectList.size()];
              int arrayCount = 0;
              String sigmaCurrent= "";
              while (objectListIterator.hasNext())
              {
                  objectMap = (Map) objectListIterator.next();
                  objIdArr[arrayCount] = (String) objectMap.get(quality.SELECT_ID);
                  arrayCount++;
              }

              StringList busSelects = new StringList(2);
              busSelects.add(quality.SELECT_QUALITY_METRIC_TITLE);
              busSelects.add(quality.SELECT_QUALITY_METRIC_SIGMA);

              MapList actionList = DomainObject.getInfo(context,
                     objIdArr,busSelects);
              Iterator actionsListIterator = actionList.iterator();

              while (actionsListIterator.hasNext())
              {
                  objectMap = (Map) actionsListIterator.next();
                  StringList titleObject =  (StringList)objectMap.get(quality.SELECT_QUALITY_METRIC_TITLE);
                  StringList sigmaObject =  (StringList)objectMap.get(quality.SELECT_QUALITY_METRIC_SIGMA);
                  if (titleObject != null)
                  {
                      //quality only has one metric
                      if(sigmaObject.size() <= 1)
                      {
                          sigmaCurrent = (String)titleObject.get(0);
                          if(sigmaCurrent.equals("0"))
                            sigmaCurrent="";
                      }
                      //Else, quality has two or more metrics
                      else
                      {
                          StringList sigmaCurrentList = (StringList)titleObject;
                          StringList sigmaList = (StringList)sigmaObject;
                          int sigmaCurrentLimit=sigmaCurrentList.size()-1;
                          String largestTitle = "" + sigmaCurrentLimit;
                          int i = 0;
                          //Loop through titleList and find the first title and the last title
                          while (i <= sigmaCurrentLimit)
                          {
                              //If the current title is equal to the largest title, then
                              //the most current metric has been found
                              String titleString = (String)sigmaCurrentList.elementAt(i);
                              if ( largestTitle.equals(titleString)) {
                                sigmaCurrent = (String)sigmaList.elementAt(i);
                              } //end if titleString is current
                              i++;
                          } //end while (i <= titleLimit)
                      } //end else quality has two or more metrics
                  }  //end if sigmaObject and titleObject != null
                  sigmaCurrentValues.add(sigmaCurrent);
              }
               //ends while
          }
          catch (Exception ex)
          {
              throw ex;
          }
          finally
          {
              return sigmaCurrentValues;
          }
      }

      /**
       * This method return true if user have permissions to create or import or delete objects
       * otherwise return false.
       *
       * @param context the eMatrix <code>Context</code> object.
       * @param args holds the following input arguments:
       *    objectId   - String containing the projectID
       * @return Boolean true or false.
       * @throws Exception If the operation fails.
       * @since PMC 10-6
       */
      public boolean hasAccessToCommand(Context context, String[] args)
        throws Exception
      {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          String objectId = (String) programMap.get("objectId");
          boolean editFlag = false;
          try
          {
              DomainObject domainObject = DomainObject.newInstance(context);
              domainObject.setId(objectId);
              editFlag = domainObject.checkAccess(context, (short) AccessConstants.cModify);
          }
          catch (Exception ex)
          {
              throw ex;
          }
          finally
          {
              return editFlag;
          }
      }

      /**
       * This method determines if the checkbox needs to be enabled or not depending
       * depending on whether the quality is in controlled state or not
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following input arguments:
       *        objectList - objectList Contains a MapList of Maps which contains objects.
       * @return Object of type Vector
       * @throws Exception if the operation fails
       * @since PMC 10-6
       */
      public Vector showQualityCheckbox(Context context, String[] args)
          throws Exception
      {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          MapList objectList = (MapList) programMap.get("objectList");

          Vector enableCheckbox = new Vector();

          try
          {
              com.matrixone.apps.program.Quality quality =
              (com.matrixone.apps.program.Quality) DomainObject.newInstance(context,
              DomainConstants.TYPE_QUALITY, "PROGRAM");


              Iterator objectListItr = objectList.iterator();
              while (objectListItr.hasNext())
              {
                  Map objectMap = (Map) objectListItr.next();
                  String qualityId = (String) objectMap.get(quality.SELECT_ID);

                  if (qualityId !=null && !qualityId.equals("null") && !qualityId.equals(""))
                  {
                      quality.setId(qualityId);

                      String qualityState = quality.getInfo(context, DomainConstants.SELECT_CURRENT).toString();
                      if(qualityState !=null && !qualityState.equals("null") && !qualityState.equals("") && qualityState.equals(quality.STATE_QUALITY_CONTROLLED))
                      {
                          enableCheckbox.add("false");
                      }
                      else
                      {
                          enableCheckbox.add("true");
                      }
                  }
              }
          }
          catch (Exception ex)
          {
              throw ex;
          }
          finally
          {
              return enableCheckbox;
          }
      }

      /**
       * This method determines if the Edit Icon needs to be shown or not depending
       * depending on whether the quality is in controlled state or not
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following input arguments:
       *        objectList - objectList Contains a MapList of Maps which contains objects.
       * @return Object of type Vector
       * @throws Exception if the operation fails
       * @since PMC 10-6
       */
      public Vector showEditIcon(Context context, String[] args)
      throws Exception
      {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          MapList objectList = (MapList) programMap.get("objectList");
          Map paramList = (Map) programMap.get("paramList");
          boolean isPrinterFriendly = false;
          String PrinterFriendly = (String)paramList.get("reportFormat");
          if ( PrinterFriendly != null ) {
              isPrinterFriendly = true;
          }

          String jsTreeID = (String) paramList.get("jsTreeID");
          String strSuiteDir = (String) paramList.get("SuiteDirectory");
          //added for the bug 338402
          String suiteKey = (String) paramList.get("suiteKey");
          //till here
          String parentId    = (String) paramList.get("projectID");

          Vector enableEditIcon = new Vector();
          String flag="";
          String imageStr="";
          String nextURL="";
          try
          {
              com.matrixone.apps.program.Quality quality =
               (com.matrixone.apps.program.Quality) DomainObject.newInstance(context,
                 DomainConstants.TYPE_QUALITY, "PROGRAM");

              Iterator objectListItr = objectList.iterator();
              while (objectListItr.hasNext())
              {
                  StringBuffer urlLink = new StringBuffer();
                  Map objectMap = (Map) objectListItr.next();
                  String qualityId = (String) objectMap.get(quality.SELECT_ID);

                  if (qualityId !=null && !qualityId.equals("null") && !qualityId.equals(""))
                  {
                      quality.setId(qualityId);

                      String qualityState = quality.getInfo(context, DomainConstants.SELECT_CURRENT).toString();

                      if(qualityState !=null && !qualityState.equals("null") && !qualityState.equals("") && qualityState.equals(quality.STATE_QUALITY_CONTROLLED))
                      {
                          enableEditIcon.add("&nbsp");
                      }
                      else
                      {
                          imageStr = "../common/images/iconActionEdit.gif";
                          if ((jsTreeID == null) || ("null".equals(jsTreeID)))
                          {
                         //modified for the bug 338402
                           nextURL="../common/emxForm.jsp?form=PMCQualityForm&mode=edit&formHeader=emxProgramCentral.Common.QualityEditHeading&HelpMarker=emxhelpqualityeditdialog&findMxLink=false&objectId="+qualityId+"&parentOID="+parentId+"&suiteKey="+suiteKey;
                        //till here

                          }
                          else
                          {
                          //modified for the bug 338402
                          nextURL="../common/emxForm.jsp?form=PMCQualityForm&mode=edit&formHeader=emxProgramCentral.Common.QualityEditHeading&HelpMarker=emxhelpqualityeditdialog&findMxLink=false&objectId="+qualityId+"&parentOID="+parentId+"&jsTreeID="+jsTreeID+"&suiteKey="+suiteKey;
                         //till here

                          }
                          if(!isPrinterFriendly) {
                              urlLink.append("<a href=\"javascript:emxTableColumnLinkClick(\'" + nextURL + "\',\'700\',\'600\',\'true\',\'popup\',\'\')\">");
                              urlLink.append("<img src=\"" + imageStr + "\" border=\"0\">");
                              urlLink.append("</a>");
                          } else {
                              urlLink.append("<img src=\"" + imageStr + "\" border=\"0\">");
                          }
                          enableEditIcon.add(urlLink.toString());
                      }
                  }
              }
          }
          catch (Exception ex)
          {
            throw ex;
          }
          finally
          {
            return enableEditIcon;
          }
      }
    /**
     * Get the Quality object id to be used for displaying data in Quality
     * Metric Table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return MapList containing objectid of Quality object
     * @throws Exception
     *             if the operation fails
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getMetric(Context context, String args[]) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        com.matrixone.apps.program.Quality quality = (com.matrixone.apps.program.Quality) DomainObject
                .newInstance(context, DomainConstants.TYPE_QUALITY, "PROGRAM");

        StringList relSelects = new StringList(14);
        String relWhere = "";
        relSelects.add(QualityMetricRelationship.SELECT_ID);
        relSelects.add(QualityMetricRelationship.SELECT_NAME);
        relSelects.add(QualityMetricRelationship.SELECT_TITLE);
        relSelects.add(QualityMetricRelationship.SELECT_ORIGINATED);
        relSelects.add(QualityMetricRelationship.SELECT_ORIGINATOR);
        relSelects.add(QualityMetricRelationship.SELECT_METRIC_SOURCE);
        relSelects.add(QualityMetricRelationship.SELECT_DPMO);
        relSelects.add(QualityMetricRelationship.SELECT_DPU);
        relSelects.add(QualityMetricRelationship.SELECT_SIGMA);
        relSelects.add(QualityMetricRelationship.SELECT_MEAN);
        relSelects.add(QualityMetricRelationship.SELECT_STANDARD_DEVIATION);
        relSelects.add(QualityMetricRelationship.SELECT_UPPER_SPEC_LIMIT);
        relSelects.add(QualityMetricRelationship.SELECT_LOWER_SPEC_LIMIT);
        relSelects.add(QualityMetricRelationship.SELECT_COMMENTS);
        quality.setId(objectId);
        StringList busSelects = new StringList(4);
        busSelects.add(Quality.SELECT_QUALITY_TYPE);
        busSelects.add(Quality.SELECT_METRIC_ID);
        busSelects.add(Quality.SELECT_METRIC_NAME);
        busSelects.add(DomainConstants.SELECT_CURRENT);
        Map qualityMap = quality.getInfo(context, busSelects);

        MapList MetricList = quality.getQualityMetrics(context, relSelects,
                relWhere);
        int count = MetricList.size();
        String strQualityState = (String)qualityMap.get(SELECT_CURRENT);
        for (int i = 0; i < count; i++) {
            Hashtable map = (Hashtable) MetricList.get(i);
            map.put("disableSelection", "true");
            
            map.putAll(qualityMap);
        }
        MetricList.sort(QualityMetricRelationship.SELECT_ORIGINATED,"ascending","date");
        if(null != (strQualityState) && !"".equals(strQualityState) && !"Controlled".equals(strQualityState) && MetricList.size() > 0) {
        	Map mpLatestQualityMetric = (Map)MetricList.get(MetricList.size()-1);
        	mpLatestQualityMetric.remove("disableSelection");
        	mpLatestQualityMetric.put("disableSelection", "false");
        }
       return MetricList;
    }

    /**
     * buildVector - Generic method for building the Vector for the columns
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            an array of String arguments for this methoda
     * @return Vector object that contains a vector of revision values for the
     *         column.
     * @throws Exception
     *             if the operation fails
     * @since Common 10-7
     * @grade 0
     */
    public Object buildVector(Context context, String[] args, String Selectable)
            throws Exception {
        // unpack and get parameter
        HashMap programmap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programmap.get("objectList");
        Vector returnVector = new Vector(objectList.size());
        Map objectMap = null;
        String returnStr = null;
        StringList strList = new StringList();
        //Added:PRG:I16:R213:IR-131272V6R2013 IR-131081V6R2013 Start
        Map paramMap = (Map)programmap.get("paramList");
        String strLanguage = (String)paramMap.get("languageStr");
        //End:PRG:I16:R213:IR-131272V6R2013 IR-131081V6R2013 End
        // loop through objects that are in the UI table. populate Vector
        // with the appropriate revision value.
        for (int i = 0; i < objectList.size(); i++) {
            objectMap = (Map) objectList.get(i);
            if (objectMap.get(Selectable) != null) {
                if ((objectMap.get(Selectable).getClass().getName())
                        .equals("matrix.util.StringList")) {
                    strList = (StringList) objectMap.get(Selectable);
                    String strSelect = FrameworkUtil.join(strList, ", ");
                    returnVector.add(strSelect);
                } else {
                    returnStr = (String) objectMap.get(Selectable);
                    //Added:PRG:I16:R213:IR-131272V6R2013 IR-131081V6R2013 Start
                    // Internationalize the selected Range value
                    returnStr = i18nNow.getRangeI18NString(ProgramCentralConstants.ATTRIBUTE_METRIC_SOURCE,returnStr,strLanguage); 	
                    //End:PRG:I16:R213:IR-131272V6R2013 IR-131081V6R2013 End
                    returnVector.add(returnStr);
                }
            }
        }
        return returnVector;
    }

    /**
     * Gets the quality metric name.
     * @param context the ENOVIA <code>Context</code> user.
     * @param args string arrays with useful request parameters.
     * @return a Vector with metric name in HTML format.
     * @throws Exception if operation fails.
     */
    public Vector displayQualityMetricName(Context context, String args[])
            throws Exception {
        Vector qualityMetricName = new Vector();
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Map paramMap = (Map)programMap.get("paramList");
        String reportFormat = null;
        String key = "reportFormat";
        if(paramMap.containsKey(key)){
        	reportFormat = (String)paramMap.get("reportFormat");
        }
        try {
            StringBuffer urlLink = null;
            for (int i = 0; i < objectList.size(); i++) {
                urlLink = new StringBuffer();
                Map objectMap = (Map) objectList.get(i);
                String metricName = (String) objectMap
                        .get(Quality.SELECT_METRIC_NAME);
                String currentTitle = (String) objectMap
                        .get(QualityMetricRelationship.SELECT_TITLE);
                String metricDisplayName = metricName + ProgramCentralConstants.COLON + currentTitle;
                if(ProgramCentralUtil.isNotNullString(reportFormat))
                	urlLink.append(XSSUtil.encodeForHTML(context,metricDisplayName));
                else{
                urlLink
                        .append("<img src=\"../common/images/iconSmallMetric.gif\" border=\"0\"/>"
                            + " " +XSSUtil.encodeForHTML(context, metricDisplayName));
                }                
                qualityMetricName.add(urlLink.toString());
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            return qualityMetricName;
        }
    }

    /**
     * Get the data for the MetricSource column in Quality Metric table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing MetricSource for each row
     * @throws Exception
     *             if the operation fails
     */
    public Object displayQualityMetricSource(Context context, String args[])
            throws Exception {
        return buildVector(context, args,
                QualityMetricRelationship.SELECT_METRIC_SOURCE);
    }

    /**
     * Get the data for the Date column in Quality Metric table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing Date for each row
     * @throws Exception
     *             if the operation fails
     */
    public Object displayQualityMetricDate(Context context, String args[])
            throws Exception {
        return buildVector(context, args, QualityMetricRelationship.SELECT_ORIGINATED);
    }

    /**
     * Get the data for the Name column in Quality Metric table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing names for each row
     * @throws Exception
     *             if the operation fails
     */
    public Object displayQualityMetricDPMO(Context context, String args[])
            throws Exception {
        return buildVector(context, args, QualityMetricRelationship.SELECT_DPMO);
    }

    /**
     * Get the data for the DPU column in Quality Metric table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing DPU for each row
     * @throws Exception
     *             if the operation fails
     */
    public Object displayQualityMetricDPU(Context context, String args[])
            throws Exception {
        return buildVector(context, args, QualityMetricRelationship.SELECT_DPU);
    }

    /**
     * Get the data for the Mean column in Quality Metric table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing Mean for each row
     * @throws Exception
     *             if the operation fails
     */
    public Object displayQualityMetricMean(Context context, String args[])
            throws Exception {
        return buildVector(context, args, QualityMetricRelationship.SELECT_MEAN);
    }

    /**
     * Get the data for the StandardDeviation column in Quality Metric table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing StandardDeviation for each row
     * @throws Exception
     *             if the operation fails
     */
    public Object displayQMStandardDeviation(Context context, String args[])
            throws Exception {
        return buildVector(context, args,
                QualityMetricRelationship.SELECT_STANDARD_DEVIATION);
    }

    /**
     * Get the data for the USL column in Quality Metric table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing USL for each row
     * @throws Exception
     *             if the operation fails
     */
    public Object displayQualityMetricUSL(Context context, String args[])
            throws Exception {
        return buildVector(context, args,
                QualityMetricRelationship.SELECT_UPPER_SPEC_LIMIT);
    }

    /**
     * Get the data for the LSL column in Quality Metric table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing LSL for each row
     * @throws Exception
     *             if the operation fails
     */
    public Object displayQualityMetricLSL(Context context, String args[])
            throws Exception {
        return buildVector(context, args,
                QualityMetricRelationship.SELECT_LOWER_SPEC_LIMIT);
    }
    /**
     * Get the data for the Comments column in Quality Metric table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing Comments for each row
     * @throws Exception
     *             if the operation fails
     */
    public Object displayQualityMetricsComments(Context context, String args[])
            throws Exception {
        return buildVector(context, args,
                QualityMetricRelationship.SELECT_COMMENTS);
    }
    /**
     * Get the data for the Name column in Quality Metric table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing names for each row
     * @throws Exception
     *             if the operation fails
     */
    public Object displayQualityMetricSigma(Context context, String args[])
            throws Exception {
        return buildVector(context, args,
                QualityMetricRelationship.SELECT_SIGMA);
    }

    /**
     * Get the data for the Name column in Quality Metric table.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing names for each row
     * @throws Exception
     *             if the operation fails
     */
    public Object displayQualityMetricOriginator(Context context, String args[])
            throws Exception {
        return buildVector(context, args,
                QualityMetricRelationship.SELECT_ORIGINATOR);
    }

    /**
     * Checks whether a user has access to delete a Quality Metric.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing delete access values for each row
     * @throws Exception
     *             if the operation fails
     */

    public Vector checkForDeleteAccess(Context context, String args[])
            throws Exception {
        boolean modifyAccess = true;
        String state = "";
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Vector enableCheckbox = new Vector();
        MapList sortList = objectList;
       // sortList.sort(QualityMetricRelationship.SELECT_ORIGINATED,
             //   "ascending", "string");            
        try {        
        if(objectList.size() > 0){
        
            String newestMetricId = (String) ((Hashtable) sortList.get(objectList.size() -1)).get(DomainRelationship.SELECT_ID);
            String goalMetricId = (String) ((Hashtable) sortList.get(0)).get(DomainRelationship.SELECT_ID);            
            for(int i =0; i< objectList.size() ; i++) {
                Map objectMap = (Map) objectList.get(i);
                state = (String) objectMap.get(DomainConstants.SELECT_CURRENT);
                String metricId = (String) objectMap.get(DomainRelationship.SELECT_ID);
                if ("Controlled".equalsIgnoreCase(state)) {
                    modifyAccess = false;
                }                
                 if (modifyAccess) {
                    if (metricId.equals(newestMetricId)) {
                        enableCheckbox.add("true");
                    }
                    else if (metricId.equals(goalMetricId)) {
                        enableCheckbox.add("false");
                    }
                 else{
                     enableCheckbox.add("false");
                 }
                  }
                 else{
                     enableCheckbox.add("false");
                 }
              }// eof for loop
           }// eof if objectList
         } catch (Exception ex) {
            throw ex;
        } finally {
            return enableCheckbox;
        }
    }

    /**
     * Checks whether a user has access to edit a Quality Metric
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @return Vector containing Edit Icon for each row
     * @throws Exception
     *             if the operation fails
     */
public Vector displayEditColumn(Context context,String args[]) throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");
        Map paramList = (Map) programMap.get("paramList");
        String objectId = (String) paramList.get("objectId");
        boolean isPrinterFriendly = false;
        String PrinterFriendly = (String)paramList.get("reportFormat");
        if ( PrinterFriendly != null ) {
          isPrinterFriendly = true;
        }
        String metricId ="";
        Vector enableEditIcon = new Vector();
        String newestMetricId ="";
        String goalMetricId ="";
        StringBuffer urlLink = new StringBuffer();        
        MapList sortList = objectList;
        //sortList.sort(QualityMetricRelationship.SELECT_ORIGINATED,
         //       "ascending", "string");          
          
        try
        {
         if(objectList.size() > 0){
             DomainObject domainObject = DomainObject.newInstance(context, objectId);
             boolean modifyAccess = domainObject.checkAccess(context, (short) AccessConstants.cModify);
   
          newestMetricId = (String) ((Hashtable) sortList.get(objectList.size() -1)).get(DomainRelationship.SELECT_ID);
          goalMetricId = (String) ((Hashtable) sortList.get(0)).get(DomainRelationship.SELECT_ID);
          for (int i =0; i< objectList.size(); i++)
          {
            Hashtable metricMap = (Hashtable) objectList.get(i);
            urlLink = new StringBuffer();  
            metricId = (String)metricMap.get(DomainRelationship.SELECT_ID);
           
            String qualityId = (String) metricMap.get(DomainConstants.SELECT_ID);
            boolean editAccess = false;
            if ( modifyAccess == true) {
              if (metricId.equals(newestMetricId)) {
                editAccess = true;
              }
              if (metricId.equals(goalMetricId)) {
                editAccess = true;
              }
            } //end if modifyAccess == true
            
            if ( editAccess) {
                 String editURL = "../programcentral/emxProgramCentralQualityMetricEditDialogFS.jsp?qualityId=" + qualityId;
                editURL += "&amp;metricId=" + metricId;
                  if(!isPrinterFriendly) 
                {
                    urlLink.append("<a href=\"javascript:showDetailsPopup(\'"+editURL+"\')\">");
                    urlLink.append("<img src=\"../common/images/iconActionEdit.gif\" border=\"0\" alt=\"\"/>");
                    urlLink.append("</a>");
                } 
                else 
                {
                    urlLink.append("<img src=\"../common/images/iconActionEdit.gif\" border=\"0\"\"/>");
                }
               }
            enableEditIcon.add(urlLink.toString());            
            }// eof for
           } // eof if 
          }
          catch (Exception ex)
          {
              throw ex;
          }
          finally
          {
              return enableEditIcon;
          }
    }
/**
 * Checks whether a user has access to edit a Quality Metric
 * 
 * @param context
 *            the eMatrix <code>Context</code> object
 * @return Vector containing Edit Icon for each row
 * @throws Exception
 *             if the operation fails
 */
public Vector displayNewWindowIcon(Context context,String args[]) throws Exception
{
    HashMap programMap = (HashMap) JPO.unpackArgs(args);
    MapList objectList = (MapList) programMap.get("objectList");
    Map paramList = (Map) programMap.get("paramList");
    String objectId = (String) paramList.get("objectId");
    boolean isPrinterFriendly = false;
    String PrinterFriendly = (String)paramList.get("reportFormat");
    if ( PrinterFriendly != null ) {
      isPrinterFriendly = true;
    }
    String metricId ="";
    Vector newWindow = new Vector();
    String newestMetricId ="";
    String goalMetricId ="";
    StringBuffer urlLink = new StringBuffer();        
    MapList sortList = objectList;
    DomainObject domainObject = DomainObject.newInstance(context, objectId);
    boolean modifyAccess = domainObject.checkAccess(context, (short) AccessConstants.cModify);
     
    try
    {
        if(objectList.size() >0){
    newestMetricId = (String) ((Hashtable) sortList.get(objectList.size() -1)).get(DomainRelationship.SELECT_ID);
    goalMetricId = (String) ((Hashtable) sortList.get(0)).get(DomainRelationship.SELECT_ID);            
        
     for (int i =0; i< objectList.size(); i++)
      {
        Hashtable metricMap = (Hashtable) objectList.get(i);
        urlLink = new StringBuffer();  
        metricId = (String)metricMap.get(DomainRelationship.SELECT_ID);
        boolean editAccess = false;
        if ( modifyAccess == true) {
          if (metricId.equals(newestMetricId)) {
            editAccess = true;
          }
          if (metricId.equals(goalMetricId)) {
            editAccess = true;
          }
        } //end if modifyAccess == true

        String qualityId = (String) metricMap.get(DomainConstants.SELECT_ID);
        String popupURL = "../programcentral/emxProgramCentralQualityMetricDetailsFS.jsp?qualityId=" + qualityId + "&metricId=" + metricId + "&editAccess=" + editAccess;
        if(!isPrinterFriendly) 
        {
            urlLink.append("<a href=\"javascript:showDetailsPopup(\'"+popupURL+"\')\">");
            urlLink.append("<img src=\"../common/images/iconNewWindow.gif\" border=\"0\" alt=\"\">");
            urlLink.append("</a>");
        } 
        else 
        {
            urlLink.append("<img src=\"../common/images/iconNewWindow.gif\" border=\"0\"\">");
        }
      newWindow.add(urlLink.toString());
      }// eof for
     }
     }
     catch (Exception ex)
      {
          throw ex;
      }
      finally
      {
          return newWindow;
      }
}
 /**
 * Display quality datatypes range values
 * @param context the eMatrix <code>Context</code> object
 * @param args
 * @return
 * @throws Exception if the operation fails
  */
 public Map displayQualityDataTypeRangeValues(Context context, String[] args) throws MatrixException
 {
	 HashMap map = new HashMap();
	 try {
		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
		 HashMap requestMap = (HashMap) programMap.get("requestMap");
		 String isContinuous = (String)requestMap.get("isContinuous");
		 com.matrixone.apps.program.Quality quality =
			 (com.matrixone.apps.program.Quality) DomainObject.newInstance(context,
					 DomainConstants.TYPE_QUALITY, "PROGRAM");
		 String sLang = context.getSession().getLanguage();
		 AttributeType attrDefaultQualityType = new AttributeType(quality.ATTRIBUTE_QUALITY_TYPE);
		 attrDefaultQualityType.open(context);
		 StringList strList = attrDefaultQualityType.getChoices(context);
		 if("true".equals(isContinuous)) 
		 {
			 strList.sort();
		 }               
		 attrDefaultQualityType.close(context);
		 StringList slDefaultUserAccessTranslated = new StringList();

		 slDefaultUserAccessTranslated=i18nNow.getAttrRangeI18NStringList(quality.ATTRIBUTE_QUALITY_TYPE, strList, sLang);                
		 map.put("field_choices", strList);
		 map.put("field_display_choices", slDefaultUserAccessTranslated);

	 } catch (Exception e) {
		 throw new MatrixException(e);
	 }finally{
	 return  map;
 }
 }
 /**
  * To check Quality Datatype is Continuous
  * 
  * @param context the eMatrix <code>Context</code> object
  * @param args
  * @return boolean  
  * @throws Exception  if the operation fails
  */
 public boolean isQualityDataTypeContinuous(Context context, String args[]) throws MatrixException
 {
	 boolean blAccess = false;
	 try{
		 HashMap inputMap = (HashMap)JPO.unpackArgs(args);
		 String isContinuous = (String)inputMap.get("isContinuous");

		 if(null!=isContinuous && "true".equals(isContinuous))
		 {
			 blAccess = true;
		 }
	 }catch(Exception e) {
		 throw new MatrixException(e);		 
	 }	 
		 return blAccess;
	 }

 /**
  * To check Quality Datatype is Discrete
  * 
  * @param context the eMatrix <code>Context</code> object
  * @param args
  * @return boolean  
  * @throws Exception  if the operation fails
  */ 
 public boolean isQualityDataTypeDiscrete(Context context, String args[]) throws MatrixException
 {
	 boolean blAccess = false;
	 try{
		 HashMap inputMap = (HashMap)JPO.unpackArgs(args);   
		 String isContinuous = (String)inputMap.get("isContinuous");

		 if(null!=isContinuous && "false".equals(isContinuous))
		 {
			 blAccess = true;
		 }
	 }catch(Exception e){
		 throw new MatrixException(e);
	 }	 
		 return blAccess;
	 }
 
 /**
  * Get Value of Quality Source field 
  * 
  *  @param context the eMatrix <code>Context</code> object
  *  @param args
  *  @return String  
  *  @throws Exception  if the operation fails
  */
 public String getSource(Context context, String[] args) throws MatrixException
 {    	   
	 String strGoal="";
	 try{
		 String languageStr = context.getSession().getLanguage();	     	 
		 strGoal = 	EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Goal", languageStr);
	 }
	 catch(Exception e){
		 throw new MatrixException(e);
	 }
		 return strGoal;
	 }
 /**
 * Create a Quality Object 
 * Connects Quality Object with Quality Metric 
 * Connects Quality Object with Object Access list  
  * 
  * @param context
 * @param args
 * @throws Exception
  */

 @com.matrixone.apps.framework.ui.PostProcessCallable
 public void createQualityProcess(Context context, String[] args) throws MatrixException
 {  	
	 com.matrixone.apps.program.ProjectSpace project = (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
			 DomainConstants.TYPE_PROJECT_SPACE, "PROGRAM");
	 com.matrixone.apps.program.Quality quality = (com.matrixone.apps.program.Quality) DomainObject.newInstance(context,
			 DomainConstants.TYPE_QUALITY, "PROGRAM");
	 try{
		 HashMap map = (HashMap) JPO.unpackArgs(args);
		 Map paramMap = (Map)map.get("paramMap");
		 Map requestMap=(Map)map.get("requestMap");

		 //Get parameters from reuestMap
		 String strParentObjectId = null; 		
		 strParentObjectId = (String) requestMap.get("objectId"); 
		 String strNewObjectId = (String) paramMap.get("newObjectId");    	  
		 quality.setId(strNewObjectId);

			String relId = quality.getInfo(context, "to[" + ProgramCentralConstants.RELATIONSHIP_QUALITY+ "].id");
			emxDomainAccessBase_mxJPO domainAccess = new emxDomainAccessBase_mxJPO(context, args);
			domainAccess.createObjectOwnershipInheritance(context, strParentObjectId, strNewObjectId, relId, 
					ProgramCentralConstants.RELATIONSHIP_PROJECT_FINANCIAL_ITEM, TYPE_PROJECT_SPACE, TYPE_QUALITY, 
					null, false);

		 //Getting all quality attributes from requestMap
		 String qualityType = (String) requestMap.get(ProgramCentralConstants.FIELD_DATATYPE);		
		 String name = (String) requestMap.get(ProgramCentralConstants.FIELD_NAME);
		 String problemStatement = (String) requestMap.get(ProgramCentralConstants.FIELD_PROBLEMSTATEMENT);
		 String operationalDefinition = (String) requestMap.get(ProgramCentralConstants.FIELD_OPERATIONALDEFINATION);
		 String defectDefinition = (String) requestMap.get(ProgramCentralConstants.FIELD_DEFECTDEFINATION);
		 String goal = (String) requestMap.get(ProgramCentralConstants.FIELD_GOAL);
		 String opportunity = (String) requestMap.get(ProgramCentralConstants.FIELD_OPPORTUNITY);
		 String outOfBounds = (String) requestMap.get(ProgramCentralConstants.FIELD_OUTOFBOUNDS);
		 String constraints = (String) requestMap.get(ProgramCentralConstants.FIELD_CONSTRAINTS);
		 String qualityComment = (String) requestMap.get(ProgramCentralConstants.FIELD_COMMENTS);		 				
		 String metricSource=ProgramCentralConstants.ATTRIBUTE_GOAL;		
		 String sigma = (String) requestMap.get(ProgramCentralConstants.FIELD_SIGMA);
		 String originator = (String) requestMap.get(ProgramCentralConstants.FIELD_ORIGINATOR);
		 String comments = (String) requestMap.get(ProgramCentralConstants.FIELD_DATATYPECOMMENTS);

		 //Add all quality attributes to attributeMap
		 HashMap attributeMap = new HashMap(16);
		 attributeMap.put(quality.ATTRIBUTE_PROBLEM_STATEMENT, problemStatement);
		 attributeMap.put(quality.ATTRIBUTE_OPERATIONAL_DEFINITION, operationalDefinition);
		 attributeMap.put(quality.ATTRIBUTE_DEFECT_DEFINITION, defectDefinition);
		 attributeMap.put(quality.ATTRIBUTE_GOAL, goal);
		 attributeMap.put(quality.ATTRIBUTE_OPPORTUNITY, opportunity);
		 attributeMap.put(quality.ATTRIBUTE_OUT_OF_BOUNDS, outOfBounds);
		 attributeMap.put(quality.ATTRIBUTE_CONSTRAINTS, constraints);
		 attributeMap.put(quality.ATTRIBUTE_COMMENTS, qualityComment);
		 attributeMap.put(quality.ATTRIBUTE_ORIGINATOR, originator);
		 attributeMap.put(quality.ATTRIBUTE_QUALITY_TYPE, qualityType);

		 //Add all metric attributes to metricMap
		 HashMap metricMap = new HashMap();
		 metricMap.put(quality.ATTRIBUTE_METRIC_SOURCE, metricSource);
		 metricMap.put(quality.ATTRIBUTE_SIGMA, sigma);
		 metricMap.put(quality.ATTRIBUTE_ORIGINATOR, originator);
		 metricMap.put(quality.ATTRIBUTE_COMMENTS, comments);

		 // get all Quality-type-specific attributes and add them to metricMap
		 if(qualityType.equals("Continuous")) {

			 String mean = (String) requestMap.get(ProgramCentralConstants.FIELD_MEAN);
			 String standardDeviation = (String) requestMap.get(ProgramCentralConstants.FIELD_STANDARDDEVIATION);
			 String upperSpecLimit = (String) requestMap.get(ProgramCentralConstants.FIELD_UPPERSPECIFICATIONLIMIT);
			 String lowerSpecLimit = (String) requestMap.get(ProgramCentralConstants.FIELD_LOWERSPECIFICATIONLIMIT);
			 //Add all Continuous Data Type attributes to metricMap
			 metricMap.put(quality.ATTRIBUTE_MEAN, mean);
			 metricMap.put(quality.ATTRIBUTE_STANDARD_DEVIATION, standardDeviation);
			 metricMap.put(quality.ATTRIBUTE_UPPER_SPEC_LIMIT, upperSpecLimit);
			 metricMap.put(quality.ATTRIBUTE_LOWER_SPEC_LIMIT, lowerSpecLimit);
		 }
		 // qualityType.equals("Discrete")
		 else {

			 String dpmo = (String) requestMap.get(ProgramCentralConstants.FIELD_DPMO);
			 String dpu = (String) requestMap.get(ProgramCentralConstants.FIELD_DPU);
			 //Add all Discrete Data Type attributes to metricMap
			 metricMap.put(quality.ATTRIBUTE_DPMO, dpmo);
			 metricMap.put(quality.ATTRIBUTE_DPU, dpu);
		 }	
		 //Add quality to project access list
		 project.setId(strParentObjectId);		 
		 //Connect a new Quality and associate with quality Metric & project access list
		 quality.connectQuality(context, attributeMap, metricMap,project);

	 } catch (Exception e) 
	 {

		 throw new MatrixException();
	 }
 }
 
 /**
   * Update DPMO of Qulaity Metric
   * 
   * @param context
  * @param args
  * @throws Exception
   */
 public void updateDPMO(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     HashMap paramMap = (HashMap)programMap.get("paramMap");
     String objectId = (String) paramMap.get("objectId");
     String strConnectionId = (String) paramMap.get("relId");
     String newDPMO = (String)paramMap.get("New Value");
     DomainRelationship domainRelationship = DomainRelationship.newInstance(context, strConnectionId);
     domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_DPMO,newDPMO);
 }
 /**
  * Update DPU of Qulaity Metric
  * 
  * @param context
 * @param args
 * @throws Exception
  */
 public void updateDPU(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     HashMap paramMap = (HashMap)programMap.get("paramMap");
     String objectId = (String) paramMap.get("objectId");
     String strConnectionId = (String) paramMap.get("relId");
     String newDPU = (String)paramMap.get("New Value");
     DomainRelationship domainRelationship = DomainRelationship.newInstance(context, strConnectionId);
     domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_DPU,newDPU);
 }
 /**
  * Update Sigma of Qulaity Metric
  * 
  * @param context
 * @param args
 * @throws Exception
  */
 public void updateSigma(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     HashMap paramMap = (HashMap)programMap.get("paramMap");
     String objectId = (String) paramMap.get("objectId");
     String strConnectionId = (String) paramMap.get("relId");
     String newSigma = (String)paramMap.get("New Value");
     DomainRelationship domainRelationship = DomainRelationship.newInstance(context, strConnectionId);
     domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_SIGMA,newSigma);
 }
 /**
  * Update Comments of Qulaity Metric
  * 
  * @param context
 * @param args
 * @throws Exception
  */
 public void updateComments(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     HashMap paramMap = (HashMap)programMap.get("paramMap");
     String objectId = (String) paramMap.get("objectId");
     String strConnectionId = (String) paramMap.get("relId");
     String newComments = (String)paramMap.get("New Value");
     DomainRelationship domainRelationship = DomainRelationship.newInstance(context, strConnectionId);
     domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_COMMENTS,newComments);
 }
 /**
  * Update Mean of Qulaity Metric
  * 
  * @param context
 * @param args
 * @throws Exception
  */
 public void updateMean(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     HashMap paramMap = (HashMap)programMap.get("paramMap");
     String objectId = (String) paramMap.get("objectId");
     String strConnectionId = (String) paramMap.get("relId");
     String newMean = (String)paramMap.get("New Value");
     DomainRelationship domainRelationship = DomainRelationship.newInstance(context, strConnectionId);
     domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_MEAN,newMean);
 }
 /**
  * Update Standard Deviation of Qulaity Metric
  * 
  * @param context
 * @param args
 * @throws Exception
  */
 public void updateStandardDeviation(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     HashMap paramMap = (HashMap)programMap.get("paramMap");
     String objectId = (String) paramMap.get("objectId");
     String strConnectionId = (String) paramMap.get("relId");
     String newStandardDeviation = (String)paramMap.get("New Value");
     DomainRelationship domainRelationship = DomainRelationship.newInstance(context, strConnectionId);
     domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_STANDARD_DEVIATION,newStandardDeviation);
 }
 /**
  * Update UpperSpecLimit of Qulaity Metric
  * 
  * @param context
 * @param args
 * @throws Exception
  */
 public void updateUpperSpecLimit(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     HashMap paramMap = (HashMap)programMap.get("paramMap");
     String objectId = (String) paramMap.get("objectId");
     String strConnectionId = (String) paramMap.get("relId");
     String newUpperSpecLimit = (String)paramMap.get("New Value");
     DomainRelationship domainRelationship = DomainRelationship.newInstance(context, strConnectionId);
     domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_UPPER_SPEC_LIMIT,newUpperSpecLimit);
 }
 /**
  * Update LowerSpecLimit of Qulaity Metric
  * 
  * @param context
 * @param args
 * @throws Exception
  */
 public void updateLowerSpecLimit(Context context, String[] args)
 throws Exception
 {
     HashMap programMap = (HashMap) JPO.unpackArgs(args);
     HashMap paramMap = (HashMap)programMap.get("paramMap");
     String objectId = (String) paramMap.get("objectId");
     String strConnectionId = (String) paramMap.get("relId");
     String newLowerSpecLimit = (String)paramMap.get("New Value");
     DomainRelationship domainRelationship = DomainRelationship.newInstance(context, strConnectionId);
     domainRelationship.setAttributeValue(context,ProgramCentralConstants.ATTRIBUTE_LOWER_SPEC_LIMIT,newLowerSpecLimit);
 }
}
