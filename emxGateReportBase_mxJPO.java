/* emxGateReportBase.java

   Copyright (c) 1999-2016 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.36.2.1 Thu Dec  4 07:55:16 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.36 Tue Oct 28 18:55:12 2008 przemek Experimental przemek $
   @since Program Central R210
   @author NR2
*/

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.program.ProgramCentralConstants;


/**
 * The <code>emxGateReportBase</code> class represents the JPO to create Phase-Gate Dashboard Report
 *
 *
 * @version AEF 9.5.1.1 - Copyright (c) 2002, MatrixOne, Inc.
 * @since Program Central R210
 * @author NR2
 */
public class emxGateReportBase_mxJPO extends emxDomainObjectBase_mxJPO
{
    String APPROVE ;
    String CONDITIONALLYAPPROVE ;
    String HOLD ;
    String CANCEL ;
    String REACTIVATE ;

    /**
     * Constructs a new emxGateReport JPO object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the id
     * @throws Exception if the operation fails
     * @since Program Central R210
     * @author NR2
     */
    public emxGateReportBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        // Call the super constructor
        super(context,args);
        if (args != null && args.length > 0){
            setId(args[0]);
        }
        APPROVE = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Gate.Approve", "en");
        CONDITIONALLYAPPROVE = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Gate.ConditionallyApprove", "en");
        HOLD = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Gate.Hold", "en");
        CANCEL = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Gate.Cancel", "en");
        REACTIVATE = EnoviaResourceBundle.getProperty(context, "ProgramCentral", "emxProgramCentral.Common.Gate.Resume", "en");
    }
    /**
     * This method returns the name of the last decison connected with the gate identified by gateId.
     * will return empty string if no decision connected to the gate is found.
     * Note: This is an performance overhead doing database fetch in JSP, will move the code in JPO.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the Gate id
     * @throws MatrixException if the operation fails
     * @since Program Central R210
     * @author NR2
     */
    public String getLastDecision(Context context,String[] args) throws MatrixException{
        String decisionName = "";
        try{
            boolean matches = true;

            Map programMap = (HashMap) JPO.unpackArgs(args);
            String gateId = (String) programMap.get("gateId");

            if(null == gateId || "".equals(gateId)){
                throw new Exception();
            }
            /*  Start processing Here */
            String APPROVE = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Gate.Approve", "en");
            String CONDITIONALLYAPPROVE = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Gate.ConditionallyApprove", "en");
            String HOLD = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Gate.Hold", "en");
            String CANCEL = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Gate.Cancel", "en");
            String RESUME = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.Common.Gate.Resume", "en");

            DomainObject gateObj = DomainObject.newInstance(context);
            gateObj.setId(gateId);
            StringList objectSelects = new StringList();
            objectSelects.add(SELECT_NAME);
            objectSelects.add(DomainConstants.SELECT_REVISION);

            String where = SELECT_NAME + "==" + APPROVE + "||";
            where += SELECT_NAME + "==" + CONDITIONALLYAPPROVE + "||";
            where += SELECT_NAME + "==" + HOLD + "||";
            where += SELECT_NAME + "==" + CANCEL + "||";
            where += SELECT_NAME + "==" + RESUME;

            MapList relatedDecisions = gateObj.getRelatedObjects(context,
                                                    DomainConstants.RELATIONSHIP_DECISION, //Relationship type
                                                    DomainConstants.TYPE_DECISION,         //objects type
                                                    objectSelects,                         //objectSelectable
                                                    null,                                  //relationship Selectables
                                                    true,                                  //from
                                                    false,                                 //to
                                                    (short) 0,                             //level
                                                    where,                                 //where filter
                                                    null,
                                                    (short) 0);                             //limit


            //Added Newly
            String pattern = "^\\d+$";
            MapList mlListToRemove = new MapList();
            if (relatedDecisions != null){
                for(int i=0;i<relatedDecisions.size();i++){
                    Map tempMap = (Hashtable) relatedDecisions.get(i);
                    String revision = (String) tempMap.get(SELECT_REVISION);
                    matches = revision.matches(pattern);
                    if(revision.length() <= 12 || !matches){
                        mlListToRemove.add(tempMap);
                    }
                }
                relatedDecisions.removeAll(mlListToRemove);
            }
            else{
                return decisionName;
            }

            //From the remaining sort
            if(relatedDecisions.size() > 0){
                relatedDecisions.sort(DomainConstants.SELECT_REVISION,"descending","String");

                //Remove all maps except 1st one
                Map lastRevisionMap = (Hashtable) relatedDecisions.get(0);
                relatedDecisions.clear();

                decisionName = (String) lastRevisionMap.get(DomainConstants.SELECT_NAME);
            }
        }
        catch(Exception e){
            throw new MatrixException(e);
        }
        return decisionName;
    }

    /**
     * Will return milestones connested at level two for the phase whose id is being passed
     * Called From : renderPhaseHTML
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the Phase Id
     * @throws MatrixException if the operation fails
     * @since Program Central R210
     * @author NR2
     */
	public MapList getMilestones(Context context,String[] args) throws MatrixException{
        MapList mlMilestones = new MapList();
        try{
            Map programMap = (HashMap) JPO.unpackArgs(args);
            String phaseId = (String) programMap.get("phaseId");

            if(null == phaseId || "".equals(phaseId)){
                throw new Exception();
            }

            /* Start Processing Here*/
            String attrDependencyTaskWBS =  "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_TASK_WBS+"]";
            String attrDependencySequence =  "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_SEQUENCE_ORDER+"]";
            String estStartDate = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]";
            String estFinishDate = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]";
            String actStartDate = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ACTUAL_START_DATE + "]";
            String actFinishDate = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE + "]";
            String parentName = "to[" + DomainConstants.RELATIONSHIP_SUBTASK + "].from.name";

            StringList objectSelects = new StringList();
            objectSelects.add(SELECT_ID);
            objectSelects.add(SELECT_TYPE);
            objectSelects.add(SELECT_NAME);
            objectSelects.add(SELECT_CURRENT);
            objectSelects.add(attrDependencyTaskWBS);
            objectSelects.add(attrDependencySequence);
            objectSelects.add(estStartDate);
            objectSelects.add(estFinishDate);
            objectSelects.add(actStartDate);
            objectSelects.add(actFinishDate);
            objectSelects.add(parentName);

            //String whereClause = SELECT_POLICY  + " == '" + ProgramCentralConstants.POLICY_PROJECT_REVIEW + "'";
            String whereClause = "";
            com.matrixone.apps.program.Task phase = (com.matrixone.apps.program.Task) DomainObject.newInstance(context,
                DomainConstants.TYPE_TASK, "PROGRAM");
            phase.setId(phaseId);
            objectSelects.add(phase.SELECT_PERCENT_COMPLETE);
            objectSelects.add(phase.SELECT_BASELINE_CURRENT_END_DATE);

            mlMilestones = phase.getRelatedObjects(context,
                                                                DomainConstants.RELATIONSHIP_SUBTASK,
                                                                ProgramCentralConstants.TYPE_MILESTONE,
                                                                objectSelects,
                                                                null,
                                                                false,
                                                                true,
                                                                (short) 1,
                                                                "",
                                                                whereClause,
                                                                (short) 0);
            mlMilestones.sort(attrDependencySequence, "ascending", "integer");
        }
        catch(Exception e){
            throw new MatrixException(e);
        }
        return mlMilestones;
    }

    /**
     * Will return MapList containg Phase and Gates at level 1 and 2 connected to 
	 * the project.
     * Called From : renderPhaseHTML
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the Project id
     * @throws MatrixException if the operation fails
     * @since Program Central R210
     * @author NR2
     */
	  public MapList getStageGateMap(Context context,String[] args) throws MatrixException{
          MapList returnVal = new MapList();
          try{
              MapList relatedFirstAndSecondLevelGatesAndPhases = new MapList();
              MapList tempMap = new MapList();

              HashMap programMap = new HashMap();

              programMap = (HashMap)JPO.unpackArgs(args);
              String projectId = (String)programMap.get("projectID");

              //Added NEWLY
              String selectedOption = (String)programMap.get("selectedOption");
              //END

              //We expect objectList's size to be 1 i.e containt only one Map of Project object
              com.matrixone.apps.program.ProjectSpace project =
                                                                (com.matrixone.apps.program.ProjectSpace)
                                                                 DomainObject.newInstance(context,
                                                                                          DomainConstants.TYPE_PROJECT_SPACE,
                                                                                          DomainConstants.PROGRAM);

              if(null == projectId || "".equals(projectId)){
                  throw new Exception();
              }

              project.setId(projectId);

              //Get all level 1 gates
              //selectables are id,type,name, est. start date, est. end date, state
              String attrTaskWBSId =  "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_TASK_WBS+"]";
              String attrWBSSequence =  "to["+DomainConstants.RELATIONSHIP_SUBTASK+"].attribute["+DomainConstants.ATTRIBUTE_SEQUENCE_ORDER+"]";
              String estStartDate = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]";
              String estFinishDate = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]";
              String actStartDate = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ACTUAL_START_DATE + "]";
              String actFinishDate = "attribute[" + DomainConstants.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE + "]";
              String parentName = "to[" + DomainConstants.RELATIONSHIP_SUBTASK + "].from.name";
              String parentType = "to[" + DomainConstants.RELATIONSHIP_SUBTASK + "].from.type";
              StringList objectSelects = new StringList();
              objectSelects.addElement(SELECT_ID);
              objectSelects.addElement(SELECT_TYPE);
              objectSelects.addElement(ProgramCentralConstants.SELECT_IS_GATE);
              objectSelects.addElement(ProgramCentralConstants.SELECT_IS_PHASE);
              objectSelects.addElement(SELECT_NAME);
              objectSelects.addElement(SELECT_CURRENT);
              objectSelects.addElement(attrTaskWBSId);
              objectSelects.addElement(attrWBSSequence);
              objectSelects.addElement(estStartDate);
              objectSelects.addElement(estFinishDate);
              objectSelects.addElement(actStartDate);
              objectSelects.addElement(actFinishDate);
              objectSelects.addElement(parentName);
              objectSelects.addElement(parentType);
              objectSelects.addElement(SELECT_POLICY);
              objectSelects.addElement(project.SELECT_PERCENT_COMPLETE);
              objectSelects.addElement(project.SELECT_BASELINE_CURRENT_END_DATE);


              String typePattern = ProgramCentralConstants.TYPE_GATE + "," + ProgramCentralConstants.TYPE_PHASE;

              relatedFirstAndSecondLevelGatesAndPhases = project.getRelatedObjects(context,
                                        DomainConstants.RELATIONSHIP_SUBTASK,
                                        typePattern,
                                        objectSelects,
                                        null,
                                        false,
                                        true,
                                        (short) 2,
                                        "",
                                        "",
                                        (short) 0);

              //Remove second level phases
              int numberOfGates = 0;
              int numberOfPhases = 0;

              MapList mapToRemove = new MapList();
              for(int i=0;i<relatedFirstAndSecondLevelGatesAndPhases.size();i++){
                  Map tMap = (Hashtable)relatedFirstAndSecondLevelGatesAndPhases.get(i);
                  String level = (String) tMap.get(attrTaskWBSId);
                  String type = (String) tMap.get(ProgramCentralConstants.SELECT_IS_PHASE);
                  String parent = (String) tMap.get(parentType);

                  // Remove any phases at level 2
                  // Only top-level phases should be supported
                  // Gates at level one or two should be supported
                  if("TRUE".equalsIgnoreCase(type)){
                      if(level.indexOf(".") != -1){
                          mapToRemove.add(tMap);
                      }
                  }
              }
              //end

              relatedFirstAndSecondLevelGatesAndPhases.removeAll(mapToRemove);

              tempMap.addAll(relatedFirstAndSecondLevelGatesAndPhases);
              if("option2".equals(selectedOption)){ //Sort By WBS ID
                  tempMap.sortStructure(context, attrTaskWBSId, "ascending", "emxWBSColumnComparator");
              }
              else{ //Sort Bt TimeLine
                  tempMap.sortStructure(context, attrTaskWBSId, "ascending", "emxWBSIDComparator");
              }

              returnVal = tempMap;
          }
          catch(Exception e){
              throw new MatrixException(e);
          }
          return returnVal;
      }
//End for Stage Gate Reports by Nishant
}
