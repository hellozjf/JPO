/*   emxProjectReportBase
**
**   Copyright (c) 2006-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of
**   MatrixOne Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such
**   program.
**
**   This JPO contains the implementation of emxProjectReportBase
**
**   static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.4.1.1.3.4.2.2 Thu Dec  4 07:55:13 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.4.1.1.3.4.2.1 Thu Dec  4 01:53:23 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.4.1.1.3.4 Wed Oct 22 15:49:37 2008 przemek Experimental przemek $
**	 Wed Feb 22 16:02:42 2006 $
*/

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.SubtaskRelationship;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.program.ProgramCentralUtil;
import com.matrixone.apps.program.ProjectSpace;
import com.matrixone.apps.program.Task;

/**
 * The <code> emxProjectReportBase</code> class contains code for the PMC
 * Governed Items,Folder Content Summary,Deliverables Report.
 * @author Tanwir Fatima
 * @version PMC 10.6SP2 - Copyright (c) 2006, MatrixOne, Inc.
 */

public class emxProjectReportBase_mxJPO extends com.matrixone.apps.program.Task
{

    public static final String STATE_PROJECT_TASK_COMPLETE = PropertyUtil.getSchemaProperty(
	                                                                                "policy",
	                                                                     POLICY_PROJECT_TASK,
	                                                                       "state_Complete");

  /**
  * Constructor.
  *
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds no arguments.
  * @throws Exception if the operation fails.
  * @since PMC 10.6SP2
  */

  public emxProjectReportBase_mxJPO (Context context, String[] args)
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
  * @since PMC 10.6SP2
  */

  public int mxMain(Context context, String[] args)
  throws Exception
  {
    if(true)
    {
      throw new Exception("must specify method on emxProjectReportBase invocation");
    }

  	return 0;
  }

  /**
  * Gets the Governed Items list
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the HashMap containg the following arguments:
  *        objectId - the context project Id
  * @return MapList containing the task details
  * @throws Exception if the operation fails
  * @since PMC 10.6SP2
  */

@com.matrixone.apps.framework.ui.ProgramCallable
  public Object getProjectSpaceGovernedItemsList (Context context,
  String[] args) throws Exception
  {
		Map hmpParamMap = (HashMap)JPO.unpackArgs(args);
    MapList maplGovernedItemsList = new MapList();
		String strObjectId = EMPTY_STRING;

    //being called in Folder Content Report
    if(hmpParamMap.containsKey("parentOID"))
    {
        strObjectId = (String) hmpParamMap.get("parentOID");
    }
    //being called from command PMCProjectGovernedItems
    else
    {
      strObjectId = (String) hmpParamMap.get("objectId");
    }

    DomainObject dobjProject = DomainObject.newInstance(context, strObjectId);
    StringList strlBusSelects = new StringList(1);
    strlBusSelects.add(DomainObject.SELECT_ID);
    StringList strlRelSelects = new StringList(1);
    strlRelSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
    String strRelDesignResponsibility = (String) PropertyUtil.getSchemaProperty(context,"relationship_DesignResponsibility");
    String strRelGoverningProject = (String) PropertyUtil.getSchemaProperty(context,"relationship_GoverningProject");
		//String strRelRelatedProjects = (String) PropertyUtil.getSchemaProperty(context,"relationship_RelatedProjects");
    String strTypePart = (String) PropertyUtil.getSchemaProperty(context,"type_Part");
    String strTypeProducts = (String) PropertyUtil.getSchemaProperty(context,"type_Products");
    String strTypeRequestToSupplier = (String) PropertyUtil.getSchemaProperty(context,"type_RequestToSupplier");
    Pattern patRelPattern = new Pattern(strRelDesignResponsibility);
    patRelPattern.addPattern(strRelGoverningProject);
    //Commented :23-Feb-09:yox:R207:PRG:Bug :369490
    //patRelPattern.addPattern(strRelRelatedProjects);
    //End:R207:PRG : Bug :369490
    Pattern patTypePattern = new Pattern(strTypePart);
    patTypePattern.addPattern(strTypeProducts);
    patTypePattern.addPattern(strTypeRequestToSupplier);

    maplGovernedItemsList =	dobjProject.getRelatedObjects (context,
                                        patRelPattern.getPattern(),
                                       patTypePattern.getPattern(),
                                                    strlBusSelects,
                                                    strlRelSelects,
                                                              true,
                                                              true,
                                                          (short)1,
                                                              null,
				null,
				0,
				null,
				null,
                                                             null);

//    List lstIds = new ArrayList();
//    for (Iterator iter = maplGovernedItemsList.iterator(); iter.hasNext();) {
//        Hashtable htGovernedItem = (Hashtable) iter.next();
//        String strID = (String)htGovernedItem.get(DomainConstants.SELECT_ID);
//        if(!lstIds.contains(strID)){
//            lstIds.add(strID);
//        }else{
//            iter.remove();
//        }
//    }

    return maplGovernedItemsList;
  }

  /**
  * Gets the list of documents within the folders
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the HashMap containg the following arguments:
  *        RequestValuesMap - String array of objectId's of all the folders
  * @return MapList containing the task details
  * @throws Exception if the operation fails
  * @since PMC 10.6SP2
  */
	  
@com.matrixone.apps.framework.ui.ProgramCallable
  public Object getProjectFolderContentList (Context context, String[] args)
  throws Exception
  {  
		Map hmpProgramMap = (HashMap) JPO.unpackArgs(args);	  
	  String strAllFolderIds =(String)hmpProgramMap.get("selectedIds");
	  String strFolderIds= strAllFolderIds.substring(1, strAllFolderIds.length()-1);
	  StringList slFolderIds =FrameworkUtil.split(strFolderIds.trim(), ",");

		StringList strUniqueDocIdList =  new StringList();
	  strUniqueDocIdList.clear();
		StringList allFolderList = new StringList();
	  int iSelectedFolderListSize = slFolderIds.size();  

		for(int i = 0; i < iSelectedFolderListSize; i++)
		  {
			String strObjectId = (String)slFolderIds.get(i);
			if(ProgramCentralUtil.isNullString(strObjectId)) {
			  throw new IllegalArgumentException("Object id is null");
		  }
		  strObjectId = strObjectId.trim();
			if(!allFolderList.contains(strObjectId)){
		  StringList slSubFolders = getSubFolderObjectList(context,strObjectId);   // get all the child folders at all the levels 
				if(null != slSubFolders) {
			  slSubFolders.add(strObjectId);
					int slSubFoldersSize = slSubFolders.size();
					for(int j=0; j<slSubFoldersSize; j++){
						String fId = (String)slSubFolders.get(j);
						if(!allFolderList.contains(fId)){
							allFolderList.add(fId);				  
		  }
		  }
				  }
			}

		}

		int size = allFolderList.size();
		String [] arrFolders = new String [size];
		allFolderList.copyInto(arrFolders);
		MapList maplFolderContentList = new MapList();
		String  SELECT_DOCUMENTS_IDS = "from["+DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2+"].to.id";  
		StringList strlTypeSelects = new StringList(1);
		strlTypeSelects.add(SELECT_DOCUMENTS_IDS);

		BusinessObjectWithSelectList folderContentWithSelectList = BusinessObject.getSelectBusinessObjectData(context,arrFolders,strlTypeSelects);

		  for(BusinessObjectWithSelectItr itr= new BusinessObjectWithSelectItr(folderContentWithSelectList); itr.next();)
		  {
			BusinessObjectWithSelect bows = itr.obj();
			  StringList slFolderContentsIds  = bows.getSelectDataList(SELECT_DOCUMENTS_IDS);
			  
			if(null != slFolderContentsIds) {
				  int folderDocumentListSize = slFolderContentsIds.size();
				  for(int i=0; i < folderDocumentListSize; i++)
				  {
					  Map mObjectInfo = new HashMap();
					  String sDocId = (String)slFolderContentsIds.get(i);
										  mObjectInfo.put(DomainObject.SELECT_ID, sDocId);
					 
					if(!strUniqueDocIdList.contains(sDocId)){  
						maplFolderContentList.add(mObjectInfo);
						strUniqueDocIdList.add(sDocId);
					  }
				  }
			  }
		  }
	   return maplFolderContentList;
  }
  
  /**
   * get all the sub folders at all the levels of the parent folder objects passed as parameter
   * @param context the eMatrix <code>Context</code> object
   * @param sParentObjId String parent folder object id
   * @return slParentSubFolderIdList - StringList containing information about sub folder id
   * @throws MatrixException if the operation fails
   * @since R212
   */
	@SuppressWarnings("unchecked")
	protected StringList getSubFolderObjectList(Context context,String sParentObjId) throws MatrixException {
	  if(ProgramCentralUtil.isNullString(sParentObjId))
	  {
		  throw new IllegalArgumentException("Parent object id is null");
	  }

	  StringList slParentSubFolderIdList = new StringList();
	  StringList slTypeSelects = new StringList();
		slTypeSelects.add(SELECT_ID);
	  try
	  {
		  DomainObject dobjFolderObject = DomainObject.newInstance(context, sParentObjId.trim());
		  
		  MapList mlSubFolders =  dobjFolderObject.getRelatedObjects (context,
					RELATIONSHIP_SUB_VAULTS, //relationship pattern
					TYPE_WORKSPACE_VAULT,//patTypePatternFolderContent.getPattern()
				  slTypeSelects,  //type selects
				  null,  //rel selects 
                                                                                false,
                                                                                 true,
				  (short)0,  //recurse to all level
                                                                                 null,
					null,
					0,
					null,
					null,
                                                                                null);


			if(null != mlSubFolders) {
				
			  Iterator it = mlSubFolders.iterator();
			  while(it.hasNext())
          {
				  Map mFolderObjectInfo = (Map)it.next();
					String sFodlerObjectId = (String)mFolderObjectInfo.get(SELECT_ID);
				  if(ProgramCentralUtil.isNotNullString(sFodlerObjectId))
            {
					  slParentSubFolderIdList.addElement(sFodlerObjectId);
            }
           }
          }

		  return slParentSubFolderIdList;
        }
	  catch(Exception e)
	  {
		  e.printStackTrace();
		  throw new MatrixException(e);
      }
 }

  /**
  * Displays the path of the folder where the document resides
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the HashMap containg the following arguments:
  * objectList - Maplist containing information about the documents
  * @return MapList containing the task details
  * @throws Exception if the operation fails
  * @since PMC 10.6SP2
  */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public StringList getDocumentFolderPath(Context context, String[] args)throws Exception {
      
		StringList vctFolderPath = new StringList();
		StringBuffer sbfFolderPathURL = new StringBuffer();
		Map hmpProgramMap = (HashMap) JPO.unpackArgs(args);
      String strPrinterFriendly = (String) ((Map)hmpProgramMap.get("paramList")).get("printerFriednly");
      
		if(strPrinterFriendly == null || strPrinterFriendly.equals("")) {
          strPrinterFriendly = "false";
      }
      //Added:1-Apr-09:nzf:R207:PRG:Bug:371774
      String strReportFormat = (String) ((Map)hmpProgramMap.get("paramList")).get("reportFormat");
		if(strReportFormat == null || strReportFormat.equals("")) {
          strReportFormat = "false";
      }
       
      MapList maplObjectList = (MapList) hmpProgramMap.get("objectList");
      String strSelectedIds = (String) ((Map)hmpProgramMap.get("paramList")).get("selectedIds");
       
		String strMQL = "print type \"" + DomainConstants.TYPE_CONTROLLED_FOLDER +"\" select derivative dump |";
		String strResult = MqlUtil.mqlCommand(context, strMQL, true);
		StringList slControlledFolderTypeHierarchy = FrameworkUtil.split(strResult, "|");
		slControlledFolderTypeHierarchy.add(DomainConstants.TYPE_CONTROLLED_FOLDER);
      
		String  SELECT_DOCUMENTS_NAMES = "to["+DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2+"].from.name";
		String  SELECT_DOCUMENTS_IDS = "to["+DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2+"].from.id";
		String  SELECT_DOCUMENTS_TYPES = "to["+DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2+"].from.type";
		String  SELECT_DOCUMENTS_TITLES = "to["+DomainConstants.RELATIONSHIP_VAULTED_OBJECTS_REV2+"].from.attribute[" + DomainConstants.ATTRIBUTE_TITLE + "]";
		StringList strlTypeSelects = new StringList(4);
		strlTypeSelects.add(SELECT_DOCUMENTS_NAMES);
		strlTypeSelects.add(SELECT_DOCUMENTS_IDS);
		strlTypeSelects.add(SELECT_DOCUMENTS_TYPES);
		strlTypeSelects.add(SELECT_DOCUMENTS_TITLES);
      
		int size = maplObjectList.size();
		String[] arrFolders = new String[size];
		for(int i=0; i<size; i++){
			Map objectMap = (Map)maplObjectList.get(i);
			String id = (String)objectMap.get(SELECT_ID);
			arrFolders[i]=id;
		}
      
		BusinessObjectWithSelectList folderContentWithSelectList = BusinessObject.getSelectBusinessObjectData(context,arrFolders,strlTypeSelects);
      
		for(BusinessObjectWithSelectItr itr1= new BusinessObjectWithSelectItr(folderContentWithSelectList); itr1.next();)
      {
			BusinessObjectWithSelect bows = itr1.obj();
			StringList slFolderContentsName  = bows.getSelectDataList(SELECT_DOCUMENTS_NAMES);
			StringList slFolderContentsIds  = bows.getSelectDataList(SELECT_DOCUMENTS_IDS);
			StringList slFolderContentsTypes  = bows.getSelectDataList(SELECT_DOCUMENTS_TYPES);
			StringList slFolderContentsTitles  = bows.getSelectDataList(SELECT_DOCUMENTS_TITLES);
          
			if(null != slFolderContentsIds) {
				int folderDocumentListSize = slFolderContentsIds.size();
          
				for(int i=0; i < folderDocumentListSize; i++)
              {
					Map mObjectInfo = new HashMap();
					String sFoldId = (String)slFolderContentsIds.get(i);
					String sFoldName = (String)slFolderContentsName.get(i);
					String sFoldType = (String)slFolderContentsTypes.get(i);
					String sFoldTitle = EMPTY_STRING;
					if(slControlledFolderTypeHierarchy.contains(sFoldType)){
						sFoldTitle = (String)slFolderContentsTitles.get(i);
					}
					if(strPrinterFriendly.equalsIgnoreCase("false")) {
                  if(!strReportFormat.equals("CSV")){
                      sbfFolderPathURL.append("<a href=javascript:emxTableColumnLinkClick(" +
                              "'../common/emxTree.jsp?mode=replace" +
                              "&emxSuiteDirectory=programcentral" +
                      "&suiteKey=ProgramCentral&objectId=");
							sbfFolderPathURL.append(sFoldId);
                      sbfFolderPathURL.append("',575,575);>");
							if(slControlledFolderTypeHierarchy.contains(sFoldType)){
								sbfFolderPathURL.append(XSSUtil.encodeForHTML(context,(String)sFoldTitle));
                          sbfFolderPathURL.append("</a>");
                      }else{
								sbfFolderPathURL.append(XSSUtil.encodeForHTML(context,(String)sFoldName));
                          sbfFolderPathURL.append("</a>");
                      }
                  }else{
							if(slControlledFolderTypeHierarchy.contains(sFoldType)){
								sbfFolderPathURL.append((String)sFoldTitle);
                      }else{
								sbfFolderPathURL.append((String)sFoldName);
                      }
                  }
                  
               }else{
						if(slControlledFolderTypeHierarchy.contains(sFoldType)){
							sbfFolderPathURL.append(XSSUtil.encodeForHTML(context,(String)sFoldTitle));
						}else{
							sbfFolderPathURL.append(XSSUtil.encodeForHTML(context,(String)sFoldName));
              }
                }
          }
          
          vctFolderPath.add(sbfFolderPathURL.toString());
          //'delete' ensures that the String Buffer Object is empty and ready to contain
          // a new URL for next Iteration.
          sbfFolderPathURL.delete(0, sbfFolderPathURL.length());
      }
		}		
      
      return vctFolderPath;
  }

  /**
  * getProjectPhase- This method is used to show phase of the project.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the following input arguments:
  *        0 - paramMap Map
  * @returns Object of type String
  * @throws Exception if the operation fails
  * @since PMC 10.6SP2
  */

	@SuppressWarnings({ "unused", "rawtypes", "unchecked" })
	public String getProjectPhase(Context context, String[] args) throws Exception {
		Map hmpIncompleteTask = new HashMap();
		Map hmpCompleteTask = new HashMap();
    double dblTotalTasks = 0.0;
    double dblCompletedTasks = 0.0;
		String strCurrentPhase = EMPTY_STRING;
		Task task = (Task) DomainObject.newInstance(context,DomainConstants.TYPE_TASK,DomainConstants.PROGRAM);
		ProjectSpace prsProjectSpace = (ProjectSpace) DomainObject.newInstance( context, DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
		Map hmpProgramMap = (Map) JPO.unpackArgs(args);
    Map mapParamMap = (Map) hmpProgramMap.get("paramMap");
    String strObjectId = (String) mapParamMap.get("objectId");
    StringList strlTask_busSelects = new StringList(6);
		strlTask_busSelects.add(SELECT_ID);
		strlTask_busSelects.add(SELECT_NAME);
		strlTask_busSelects.add(SELECT_TASK_ACTUAL_FINISH_DATE);
		strlTask_busSelects.add(SELECT_TASK_ESTIMATED_FINISH_DATE);
		strlTask_busSelects.add(SELECT_CURRENT);
		strlTask_busSelects.add(SELECT_BASELINE_CURRENT_END_DATE);
    StringList strlRelSelects = new StringList(1);
    strlRelSelects.add(SubtaskRelationship.SELECT_TASK_WBS);
		StringList strlBusSelects = new StringList(3);
		strlBusSelects.add(SELECT_ID);
		strlBusSelects.add(SELECT_NAME);
		strlBusSelects.add(SELECT_CURRENT);
    prsProjectSpace.setId(strObjectId);
    Map mapTheProjectMap = (Map) prsProjectSpace.getInfo(context, strlBusSelects);
    MapList maplProjectTasks = prsProjectSpace.getTasks(context,
                                                              1,
                                            strlTask_busSelects,
                                                 strlRelSelects,
                                                          false); // newly added

    //clear the lists to make sure they are empty
    hmpIncompleteTask.clear();
    hmpCompleteTask.clear();

		if (! maplProjectTasks.isEmpty()) {
      Iterator itrTaskItr = maplProjectTasks.iterator();

      while(itrTaskItr.hasNext())
      {
        Map mapCurState = (Map) itrTaskItr.next();
				String strCurrstate = (String) mapCurState.get(SELECT_CURRENT);
				task.setId((String) mapCurState.get(SELECT_ID));

				if (STATE_PROJECT_TASK_COMPLETE.equals(strCurrstate)) {
          //Complete State Block
          dblCompletedTasks++;
					String strFinishDate = EMPTY_STRING;
					String strBaselineCurrentFinishDateStr = (String) mapCurState.get(SELECT_BASELINE_CURRENT_END_DATE);

					if (null == strBaselineCurrentFinishDateStr || "".equals(strBaselineCurrentFinishDateStr)) {
						strFinishDate = (String) mapCurState.get(SELECT_TASK_ACTUAL_FINISH_DATE);
					} else {
            strFinishDate = strBaselineCurrentFinishDateStr;
          }
					hmpCompleteTask.put(strFinishDate + mapCurState.get(SELECT_ID), mapCurState);
				} else {
          // Incomplete State Block
					String strFinishDate = EMPTY_STRING;
					String strBaselineCurrentFinishDateStr = (String) mapCurState.get(SELECT_BASELINE_CURRENT_END_DATE);

					if (null == strBaselineCurrentFinishDateStr || "".equals(strBaselineCurrentFinishDateStr)) {
						strFinishDate = (String) mapCurState.get(SELECT_TASK_ESTIMATED_FINISH_DATE);
					} else {
            strFinishDate = strBaselineCurrentFinishDateStr;
          }
					hmpIncompleteTask.put(strFinishDate +	mapCurState.get(SELECT_ID), mapCurState);
        }
      } // end while

      ++dblTotalTasks;
    } //end if task list is not empty

    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(
                            eMatrixDateFormat.getEMatrixDateFormat(),
                                                          Locale.US);
    java.util.Date dtMin = new java.util.Date();
    Map mapDisply = null;
		String strMinStr = EMPTY_STRING;
    boolean blnCompleted = false;
		String strCurrentValue = (String) mapTheProjectMap.get(SELECT_CURRENT);

		if (STATE_PROJECT_TASK_COMPLETE.equals(strCurrentValue)) {
      blnCompleted = true;
    }

    //if there are tasks see which one we are going to set as the current phase
    if ( !hmpIncompleteTask.isEmpty() || !hmpCompleteTask.isEmpty())
    {
      //all tasks are complete, get the task with the oldest end date
			if(hmpIncompleteTask.isEmpty()){
        // COMPLETE
        blnCompleted = true;
				if(! hmpCompleteTask.isEmpty()){
          Object keys[] = (hmpCompleteTask.keySet()).toArray();

          //put the keys in ascending order
          java.util.Arrays.sort(keys);
          strMinStr = (String)keys[0];
          java.util.Date dtTest;
          java.util.Date dtMax;

          for (int i =0; i < keys.length; i++)
          {
            dtTest = sdf.parse(((String)((Map)hmpCompleteTask.get(
                                             (String)keys[i])).get(
                             task.SELECT_TASK_ACTUAL_FINISH_DATE)));

            dtMax = sdf.parse(((String)((Map)hmpCompleteTask.get(
                                                  strMinStr)).get(
                            task.SELECT_TASK_ACTUAL_FINISH_DATE)));

            if(dtTest.after(dtMax))
            {
              strMinStr = (String)keys[i];
            }

          }//end of 'for' loop for keys

        }//end if completed list is not empty

        mapDisply = (Map) hmpCompleteTask.get(strMinStr);

        //if the task is complete use the estimated finish date
        dtMin = sdf.parse((String)mapDisply.get(
        task.SELECT_TASK_ESTIMATED_FINISH_DATE));
      } // end of if

      //
      //incomplete task list is not empty get the first task with the lowest
      // estimated end date Or with min wbs Number comparing texicographically
      //So 1.1.1 < 1.1.2
			else {
        blnCompleted = false;

				if(! hmpIncompleteTask.isEmpty()) {
          Object keys[] = (hmpIncompleteTask.keySet()).toArray();
          //put the keys in ascending order
          java.util.Arrays.sort(keys);
          strMinStr = (String) keys[0];
          java.util.Date dtMinDate;
          java.util.Date dtTest;
          String strWbsNumber;
          String strMinNumber;

          for (int i = 0; i <keys.length; i++)
          {
            dtMinDate = sdf.parse(((String)((Map)hmpIncompleteTask.get(
                                                        strMinStr)).get(
										SELECT_TASK_ESTIMATED_FINISH_DATE)));

            dtTest = sdf.parse(((String)((Map)hmpIncompleteTask.get(
                                               (String)keys[i])).get(
										SELECT_TASK_ESTIMATED_FINISH_DATE)));

            strMinNumber = ((String)((Map)hmpIncompleteTask.get(
                                                 strMinStr)).get(
                            SubtaskRelationship.SELECT_TASK_WBS));

            strWbsNumber = ((String)((Map)hmpIncompleteTask.get(
                                           (String)keys[i])).get(
                            SubtaskRelationship.SELECT_TASK_WBS));

						if(dtTest.before(dtMinDate) || (strWbsNumber.compareTo(strMinNumber) < 0)) {
              strMinStr = (String) keys[i];
            }
          }
        }//end if

        mapDisply = (Map) hmpIncompleteTask.get(strMinStr);
      }
    }

    if(mapDisply != null)
    {
      strCurrentPhase = XSSUtil.encodeForHTML(context,(String) mapDisply.get(task.SELECT_NAME));
    }

    return strCurrentPhase;
  }

/**
  * Gets the list of deliverables on the tasks
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the HashMap containg the following arguments:
  *    0-objectId - String containing the objectId's of the tasks
  *	   1-projId - String containing the Project Id
  * @return MapList containing the deliverables details
  * @throws Exception if the operation fails
  * @since PMC 10.6SP2
  */

  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getProjectWBSDeliverableList(Context context, String[] args)
  throws Exception
  {
    //variable declaration
		String strTaskobjectId = EMPTY_STRING;
    MapList maplIndividualTaskDeliverableList = new MapList();

    i18nNow i18nnow = new i18nNow();
    String strObjWhere = null;
		String strLanguage = EMPTY_STRING;
		String strPropertyFile = EMPTY_STRING;
		String strEnableTaskFilter = EMPTY_STRING;
		String strTaskFilterAttribute = EMPTY_STRING;
		String strTaskFilterAttributeName = EMPTY_STRING;

    //getting the object id's of the task and project
		Map hmpProgramMap = (HashMap) JPO.unpackArgs(args);

    String strObjectIds = (String) hmpProgramMap.get("taskIds");
    String strProjId = (String) hmpProgramMap.get("objectId");
    String strShowAll = (String) hmpProgramMap.get("showAll");

    MapList maplTaskDeliverableList = new MapList();

    Hashtable htbProjDetails = new Hashtable();
    htbProjDetails.put(DomainObject.SELECT_ID,strProjId);

    String strRelSubTask = (String) PropertyUtil.getSchemaProperty(context,"relationship_Subtask");
    Pattern patRelPatternTaskDeliverables = new Pattern(strRelSubTask);
    String strTypeTask = (String) PropertyUtil.getSchemaProperty(context,"type_TaskManagement");
    Pattern patTypePatternTaskDeliverables = new Pattern(strTypeTask);

    StringList strlTypeSelects = new StringList(2);
    strlTypeSelects.add(DomainObject.SELECT_ID);
    StringList strlRelSelects = new StringList(1);
    strlRelSelects.add(DomainObject.SELECT_RELATIONSHIP_ID);
    DomainObject dobjTaskObject = DomainObject.newInstance(context);

    strPropertyFile = "emxProgramCentral";
    strLanguage = context.getSession().getLanguage();
		strEnableTaskFilter = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
				"emxProgramCentral.DeliverablesReport.enableTaskFilter", strLanguage);
    if(strEnableTaskFilter.equalsIgnoreCase("YES"))
    {
			strTaskFilterAttribute = EnoviaResourceBundle.getProperty(context, "ProgramCentral", 
					"emxProgramCentral.DeliverablesReport.taskFilterAttribute", strLanguage);

         strTaskFilterAttributeName = (String) PropertyUtil.getSchemaProperty(context,strTaskFilterAttribute);

         strObjWhere = "attribute[" + strTaskFilterAttributeName + "]==YES";
    }

    if(strShowAll.equals("true"))
    {
      dobjTaskObject.setId(strProjId);

      maplTaskDeliverableList = dobjTaskObject.getRelatedObjects (context,
                               patRelPatternTaskDeliverables.getPattern(),
                              patTypePatternTaskDeliverables.getPattern(),
                                                          strlTypeSelects,
                                                           strlRelSelects,
                                                                    false,
                                                                     true,
                                                                 (short)0,
                                                              strObjWhere,
                                                                    null);
    }
    else
    {
      StringTokenizer stkSelectedIdTokenizer = new StringTokenizer(strObjectIds, ",");

      while (stkSelectedIdTokenizer.hasMoreTokens())
      {
        strTaskobjectId = stkSelectedIdTokenizer.nextToken();
        strTaskobjectId = strTaskobjectId.trim();
        Hashtable htbTaskDetails = new Hashtable();
        htbTaskDetails.put(DomainObject.SELECT_ID, strTaskobjectId);
        maplTaskDeliverableList.add(htbTaskDetails);

        if(!strTaskobjectId.equals(strProjId))
        {
          dobjTaskObject.setId(strTaskobjectId);

          maplIndividualTaskDeliverableList = dobjTaskObject.getRelatedObjects (context,
                                             patRelPatternTaskDeliverables.getPattern(),
                                            patTypePatternTaskDeliverables.getPattern(),
                                                                        strlTypeSelects,
                                                                         strlRelSelects,
                                                                                  false,
                                                                                   true,
                                                                               (short)0,
                                                                            strObjWhere,
                                                                                  null);
          maplTaskDeliverableList.addAll(maplIndividualTaskDeliverableList);
        }
      }
    }
    maplTaskDeliverableList.add(0, htbProjDetails);
    /* Addition for PMC 10.6.SP2 on Apr 07 2006 by MatrixOne India for Incident-317707 - Starts*/
    // The 'level' key in each Map element causes sorting function to fail.
    // The fix is to remove the 'level' key from the Map, so that this
    // structure sorting would get bypassed.
    for (int intMapListVar = 0; intMapListVar < maplTaskDeliverableList.size(); intMapListVar++)
    {
        Map mapTemp = (Map) maplTaskDeliverableList.get(intMapListVar);
        if (mapTemp.containsKey("level"))
        {
           mapTemp.remove("level");
        }
        maplTaskDeliverableList.set(intMapListVar, (Object) mapTemp);
    }
    /* Addition for PMC 10.6.SP2 on Apr 07 2006 by MatrixOne India for Incident-317707 - Ends*/
    return maplTaskDeliverableList;
  }
}
