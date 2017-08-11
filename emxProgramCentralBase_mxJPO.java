// emxProgramCentalBase.java
//
// Copyright (c) 2007-2016 Dassault Systemes.
// All Rights Reserved
// This program contains proprietary and trade secret information of
// MatrixOne, Inc.  Copyright notice is precautionary only and does
// not evidence any actual or intended publication of such program.
//
// static const char RCSID[] = $Id: ${CLASSNAME}.java.rca 1.2.1.4.2.1 Thu Dec  4 07:55:02 2008 ds-ss Experimental ${CLASSNAME}.java.rca 1.2.1.4 Wed Oct 22 15:49:52 2008 przemek Experimental przemek $
//

import java.io.*;
import java.util.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.common.*;
import com.matrixone.apps.program.ProjectSpace;

/**
 * The <code>emxProgramCentralBase</code> class represents the Program JPO
 * functionality for Program central.
 *
 * @version AEF 10.7.SP1 - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxProgramCentralBase_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 10.7.SP1
     */

    public emxProgramCentralBase_mxJPO (Context context, String[] args)
        throws Exception
    {
    }

    /**
    *
    * Returns a MapList of object id's that meet the search criteria.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args all the search criteria from dialog page
    * @return MapList
    * @throws Exception if the operation fails
    * @since PMC 10.7.SP1
    */
   public MapList getGeneralSearchResult(Context context , String[] args)
      throws Exception
    {
        MapList queryResultList = new MapList();
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
	
        com.matrixone.apps.program.ProjectSpace project =
            (com.matrixone.apps.program.ProjectSpace) DomainObject.newInstance(context,
            DomainConstants.TYPE_PROJECT_SPACE, DomainConstants.PROGRAM);
        com.matrixone.apps.common.Document document =
            (com.matrixone.apps.common.Document) DomainObject.newInstance(context,
            DomainConstants.TYPE_DOCUMENT);
        com.matrixone.apps.common.WorkspaceVault workspaceVault =
            (com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
            DomainConstants.TYPE_WORKSPACE_VAULT);
        try {
            // Getting the parameters
            String topParentHolderId  = (String)paramMap.get("projectId");
            String sProjectId  = (String)paramMap.get("projectID");
            String Owner  = (String)paramMap.get("Owner");
            String Name  = (String)paramMap.get("Name");
            String Type  = (String)paramMap.get("DocumentType");
            String WorkspaceVaultId  = (String)paramMap.get("WorkspaceVaultId");
            String IncludeSubfolders  = (String)paramMap.get("IncludeSubfolders");
			//Modified for Bug # 340514 on 9/11/2007 - Begin
            String CreateAfterDate  = (String)paramMap.get("CreatedAfter");
			String CreateAfterDate_ms  = (String)paramMap.get("CreatedAfter_msvalue");
            String CreateBeforeDate  = (String)paramMap.get("CreatedBefore");
			String CreateBeforeDate_ms  = (String)paramMap.get("CreatedAfter_msvalue");
			//Modified for Bug # 340514 on 9/11/2007 - End
            String GeneralSearch  = (String)paramMap.get("GeneralSearch");
            String DocumentType  = (String)paramMap.get("DocumentType");
            String Title  = (String)paramMap.get("Title");
            String vaultType  = (String)paramMap.get("vaultType");
       
            boolean doGeneralSearch = false;
            if (topParentHolderId == null || "".equals(topParentHolderId.trim())) {
                doGeneralSearch = true;
            }

            if ("true".equals(GeneralSearch)) {
                doGeneralSearch = true;
            }
       
            String vaultPattern = "";
            if(!vaultType.equals(PersonUtil.SEARCH_SELECTED_VAULTS))
            {
                vaultPattern = PersonUtil.getSearchVaults(context, false ,vaultType);
            }
            else 
            {
                vaultPattern = (String)paramMap.get("selectedVaults");
            }

            // set the query limit for performance improvement
            String queryLimit  = (String)paramMap.get("queryLimit");
            if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")){
              queryLimit = "0";
            }

            // commented out code below and replaced with this, not sure of the purpose
            // of putting * in front of and at end of Name... Bug 339000
            if (Name == null || Name.equals("null") || Name.equals("")){
              Name = "*";
            }
            /*
            // fix the name string so that there is an * at the front and rear
            if(Name.indexOf('*')!= 0){
              Name = "*"+Name;
            }
            if(Name.lastIndexOf('*')!=Name.length()-1){
              Name+= "*";
            }
            */
            
            workspaceVault.setContentRelationshipType(workspaceVault.RELATIONSHIP_VAULTED_OBJECTS_REV2);
            String timeZone  = (String)paramMap.get("timeZone");
            double iClientTimeOffset = (new Double(timeZone)).doubleValue();

            //Dates must be formated so they are in same formate as above
            //if a data is entered, then convert it to new formate
            boolean createAfterDateEntered = false;
            boolean createBeforeDateEntered = false;
            Locale localeObj = (Locale)paramMap.get("localeObj");
           //Modified for Bug # 340514 on 9/11/2007 - Begin
            //if (!CreateAfterDate.equals(noDate)) {
			if ((!"".equalsIgnoreCase(CreateAfterDate_ms)) && CreateAfterDate_ms!=null) {
              CreateAfterDate = eMatrixDateFormat.getFormattedInputDateTime(context,CreateAfterDate, "11:59:59 PM", iClientTimeOffset, localeObj);
              createAfterDateEntered = true;
            }
			if ((!"".equalsIgnoreCase(CreateBeforeDate_ms)) && CreateBeforeDate_ms!=null) {
              CreateBeforeDate = eMatrixDateFormat.getFormattedInputDateTime(context,CreateBeforeDate, "12:00:00 AM", iClientTimeOffset, localeObj);
              createBeforeDateEntered = true;
            }
			//Modified for Bug # 340514 on 9/11/2007 - End
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            //Step (1): Build the busSelect and busWhere clauses
            // Build the busSelects clause.

            String where = "to[" + DomainConstants.RELATIONSHIP_TASK_DELIVERABLE +
                                 "].from.to[" +  DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY +
                                 "].from.from[" +  DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST +
                             "].to.id";
            StringList busSelects = new StringList (2);
            busSelects.add (document.SELECT_ID);
            busSelects.add (where);

            // The following section is building the busWhere clause.
            String busWhere = null;
            if (!Name.equals("null") && !Name.equals("*") && !Name.equals("")) {
                busWhere = "\"" + document.SELECT_NAME + "\" ~~ const\"" + Name + "\"";
            } //end if name is not null    
       
            if ((Title != null) && !Title.equals(null) && !Title.equals("null") &&
              !Title.equals("*") && !Title.equals("")) {
              if (busWhere != null){
                busWhere += " && \"" + document.SELECT_TITLE + "\" ~~ const\"" + Title + "\"";
              } else {
                busWhere = "\"" + document.SELECT_TITLE + "\" ~~ const\"" + Title + "\"";
              }
            }

            if ((Owner != null) && !Owner.equals(null) && !Owner.equals("null") &&
              !Owner.equals("*") && !Owner.equals("")) {
              if (busWhere != null){
                busWhere += " && \"" + document.SELECT_OWNER + "\" ~~ \"" + Owner + "\"";
              } else {
                busWhere = "\"" + document.SELECT_OWNER + "\" ~~ \"" + Owner + "\"";
              }
            } //end if Owner is not null

            if (createAfterDateEntered == true) {
              //if there is no version date attribute use the originated date
                if (busWhere != null){
                  busWhere += " && \"" + document.SELECT_ORIGINATED + "\" gt \"" + CreateAfterDate + "\"";
                } else {
                  busWhere = "\"" + document.SELECT_ORIGINATED + "\" gt \"" + CreateAfterDate + "\"";
                }//ends else
            }//ends if

            if (createBeforeDateEntered == true) {
              //if there is no version date attribute use the originated date
                if (busWhere != null){
                  busWhere += " && \"" + document.SELECT_ORIGINATED + "\" lt \"" + CreateBeforeDate + "\"";
                } else {
                  busWhere = "\"" + document.SELECT_ORIGINATED + "\" lt \"" + CreateBeforeDate + "\"";
                }//ends else
            }//ends if
       
            //add clause to fetch only master documents in case of DOCUMENTS type
            if ((Type != null) && !"".equals(Type) && !"null".equals(Type)){
              BusinessType docType = null;
              if(!Type.equals(document.TYPE_DOCUMENTS))
              {
                docType = getAllSubTypes(context, document.TYPE_DOCUMENTS, null).find(Type);
              }  
              if(Type.equals(document.TYPE_DOCUMENTS) || docType != null){
                if (busWhere != null){
                busWhere += " && \""+CommonDocument.SELECT_IS_VERSION_OBJECT + "\" ~~ \"false\"";
                }else{
                  busWhere = "\""+CommonDocument.SELECT_IS_VERSION_OBJECT + "\" ~~ \"false\"";
                }
              }
            }  
            //StringList to hold list of individual where claused to be separated by "||"
            StringList strQueryList = new StringList();

            if ((Type != null) && !Type.equals(null) && !Type.equals("null") &&
              !Type.equals("*") && !Type.equals("")) {
              if (! "true".equals(GeneralSearch)) {

                String typeP = document.SELECT_TYPE + "\" ~~ \"" + Type + "\"";
                String busWhereTemp = busWhere;
                if (busWhereTemp != null){
                  busWhereTemp += " && \"" + typeP;
                } else {
                  busWhereTemp = "\"" + typeP;
                }
                strQueryList.addElement(busWhereTemp);
                busWhereTemp = busWhere;

                //Iterate and add for all the subtypes
                BusinessTypeList busTypeList = getAllSubTypes(context,Type,new BusinessTypeList());
                Iterator itr = busTypeList.iterator();
                while ( itr.hasNext() ) {
                  BusinessType busChildType = (BusinessType) itr.next();
                  // add to return list
                  typeP = document.SELECT_TYPE + "\" ~~ \"" + busChildType.getName() + "\"";
                  if (busWhereTemp != null){
                    busWhereTemp += " && \"" + typeP;
                  } else {
                    busWhereTemp = "\"" + typeP;
                  }//ends else
                  strQueryList.addElement(busWhereTemp);
                  busWhereTemp = busWhere;
                }//ends while

              } //general search does not want type in busWhere
            } //end if type is not null or *
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
            //Step (2): Perform the search if the page is called form Folder Search
            //StringList to hold the finally formed query list
            StringList finalQueryList = new StringList();
            if (!doGeneralSearch) {
              MapList vaultList = new MapList();
              MapList documentList = new MapList();
              HashMap specifiedWorkspaceVaultMap = new HashMap();
              int qLimit = (new Integer(queryLimit)).intValue();

              StringList vaultSL  = new StringList();
              vaultSL = FrameworkUtil.split(vaultPattern, ",");
              if (vaultSL.size() > 0){
                Iterator vaultItr = vaultSL.iterator();
                while (vaultItr.hasNext()) {
                  String vaultName = (String) vaultItr.next();
                  vaultName = vaultName.trim();

                  StringItr queryListIter = new StringItr(strQueryList);
                  while(queryListIter.next()){
                    String indQuery = queryListIter.obj();
                    if (indQuery == null) {
                      indQuery = "\"vault\" == \"" + vaultName + "\"";
                    } else {
                      indQuery += " && " +  "\"vault\" == \"" + vaultName + "\"";
                    }//ends else

                    finalQueryList.addElement(indQuery);

                  }//ends while

                }//ends while
              }//ends if
              busWhere = "";
              for(int i=0;i<finalQueryList.size();i++){
                if(i==finalQueryList.size()-1){
                  busWhere +="("+(String)finalQueryList.elementAt(i)+")";
                } else {
                  busWhere +="("+(String)finalQueryList.elementAt(i)+")"+"||";
                }//ends else
              }//ends for

              //With the topParentHolderId, get a MapList of all
              //workspace vaults related to the selected project
              if (topParentHolderId != null && !topParentHolderId.equals("")){
                project.setId (topParentHolderId);

                //If the user wants to search in a specific folders then
                boolean searchInSpecificWorkspaceVault = false;
                if (WorkspaceVaultId != null && ! "".equals(WorkspaceVaultId)) {
                  workspaceVault.setId (WorkspaceVaultId);
                  searchInSpecificWorkspaceVault = true;

                  //Create Map with information from selectedVault
                  specifiedWorkspaceVaultMap.put("name", workspaceVault.getInfo(context, workspaceVault.SELECT_NAME));
                  specifiedWorkspaceVaultMap.put("id", workspaceVault.getInfo(context, workspaceVault.SELECT_ID));
                  specifiedWorkspaceVaultMap.put("type", workspaceVault.getInfo(context, workspaceVault.SELECT_TYPE));
                }

                //If the user wants to include subfolders during search
                if ("true".equals(IncludeSubfolders)) {
                  //Only search in specific workspaceVault
                  if (searchInSpecificWorkspaceVault == true) {
                    //Add subVaults of selected vault to vaultList
                    vaultList = workspaceVault.getSubVaults(context, busSelects, 0);
                    //Add selected vault to vaultList
                    vaultList.add(specifiedWorkspaceVaultMap);
                  } else {
                    //Search in all workspaceVaults for the project
                    vaultList = workspaceVault.getWorkspaceVaults(context, project, busSelects, 0);
                  }
                } else {
                  //Do not include subfolders during search
                  //Only search in specific workspaceVault
                  if (searchInSpecificWorkspaceVault == true) {
                    //Add selected vault to vaultList
                    vaultList.add(specifiedWorkspaceVaultMap);
                  } else {
                    //Search in all workspaceVaults for the project
                    vaultList = workspaceVault.getWorkspaceVaults(context, project, busSelects, 1);
                  }
                }  //end else include subfolders was not checked
              } //end if topParentHolderId != null
              //If user is just looking for workspace vaults, then just return the vaultList
              if (DocumentType.equals(project.TYPE_WORKSPACE_VAULT)) {
                queryResultList = vaultList;
              }
              //Else, get all files for each workspaceVault
              else if (vaultList != null){
                Iterator vaultItr = vaultList.iterator();
                while (vaultItr.hasNext()) {
                  Map vaultMap   = (Map) vaultItr.next();
                  String vaultId = (String) vaultMap.get(project.SELECT_ID);
                  workspaceVault.setId(vaultId);

                  // See if the current workspaceVault contains any document objects
                  documentList = workspaceVault.getItems(context, busSelects, null, busWhere, null);
                  //get map for each document and add it to the list
                  Iterator documentItr = documentList.iterator();
                  while (documentItr.hasNext()) {
                    Map documentMap = (Map) documentItr.next();
                     if(qLimit==0 || qLimit > queryResultList.size()){
                       queryResultList.add(documentMap);
                     }
                  }  //end while docItr has next
                  //Clear the workspaceVault bean so you can set it again at top of loop
                  workspaceVault.clear();
                } //end while vaultItr has next
              } //end else if vaultList != null
            } //end if search process is for Folder Search
            //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

            // Fix for #287017 - set the where cluase to get the documents in all vaults for Folder search and no projects/folder selected
            // 'GeneralSearch' is the value of the parameter UnmanagedFiles from search dialog
            if (GeneralSearch!=null && !"null".equals(GeneralSearch) && !"true".equals(GeneralSearch)) {
                 if(!"".equals(busWhere))
                 {
                   busWhere +=" && to["+ DomainObject.RELATIONSHIP_VAULTED_OBJECTS_REV2 +"].from.id !=\"\"";  
                 }
                 else
                 {
                   busWhere ="to["+ DomainObject.RELATIONSHIP_VAULTED_OBJECTS_REV2 +"].from.id !=\"\"";  
                 }
               }

               //Step (3): Perform the search if the page is called form General Search
               if (doGeneralSearch) {

                 String typePattern = "";
                 if ((Type != null) && !Type.equals(null) && !Type.equals("null") &&
                   !Type.equals("*") && !Type.equals(""))
                 {
                   typePattern = Type;
                 } else {
                     typePattern  = (String)paramMap.get("docTypesStr");
                 }

                 String namePattern = null;
                 if (!Name.equals("null") &&
                   !Name.equals("*") && !Name.equals("")) {
                   namePattern = Name;
                 }

                 String ownerPattern = "*";
                 if (Owner != null && !Owner.equals(null) && !Owner.equals("null") &&
                     !Owner.equals("*") && !Owner.equals("")){
                   ownerPattern = Owner;
                 }
                 queryResultList = project.findObjects(
                                   context,        // eMatrix context
                                   typePattern,    // type pattern
                                   namePattern,    // name pattern
                                   "*",            // revision pattern
                                   ownerPattern,   // owner pattern
                                   vaultPattern,   // vault pattern
                                   busWhere,       // where expression
                                   null,             // queryName
                                   true,           // expand type
                                   busSelects,     // object selects
                                   Short.parseShort(queryLimit) // object Limit
                                   );
                  Iterator qryItr =  queryResultList.iterator();
                  MapList newList = new MapList();
                  DomainObject dmObj = DomainObject.newInstance(context);
                  String id="";
                  String prId="";
                  while(qryItr.hasNext())
                  {
                    Map map = (Map)qryItr.next();           
                    id = (String) map.get(document.SELECT_ID);         
                    prId = (String) map.get(where);         
                    dmObj.setId(id);
                    if(dmObj.isKindOf(context,DomainConstants.TYPE_IC_DOCUMENT)||dmObj.isKindOf(context,DomainConstants.TYPE_IC_FOLDER))
                    {
                     if(prId !=null && !"".equals(prId) && !"null".equals(prId))
                     {
                        if(prId.equals(sProjectId))
                        {
                          newList.add(map);
                        }
                     } 
                    }
                    else
                    {
                       newList.add(map);
                    }
                  }       
                  queryResultList = newList;       
             } //end if search is a general search
          } catch (Exception e) {
     }

     return queryResultList;   
   }

    /**
    *
    * Returns a MapList of object id's that meet the More search criteria.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args all the search criteria from dialog page
    * @return MapList
    * @throws Exception if the operation fails
    * @since PMC 10.7.SP1
    */
   public MapList getMoreGeneralSearchResult(Context context , String[] args)
      throws Exception
    {
      MapList queryResultList = new MapList();
      HashMap paramMap = (HashMap)JPO.unpackArgs(args);
      boolean isEPMInstalled = ProjectSpace.isEPMInstalled(context);
      com.matrixone.apps.common.WorkspaceVault workspaceVault =
          (com.matrixone.apps.common.WorkspaceVault) DomainObject.newInstance(context,
          DomainConstants.TYPE_WORKSPACE_VAULT);
      com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);

      String searchMode           = (String)paramMap.get("searchMode");
      String ComboType            = (String)paramMap.get("ComboType");
      String queryLimit           = (String)paramMap.get("queryLimit");
      String isAddPerson          = (String)paramMap.get("isAddPerson");
      String projId               = (String)paramMap.get("projectId");
      String dateAttrListStr      = (String)paramMap.get("dateAttrListStr");
      String FolderName1          = (String)paramMap.get("FolderName");
      String sAttrCompetency      =  PropertyUtil.getSchemaProperty(context, "attribute_Competency");
      String sAttrExperience      = PropertyUtil.getSchemaProperty(context, "attribute_Experience");
      String sRelhasBusinessSkill = PropertyUtil.getSchemaProperty(context, "relationship_hasBusinessSkill");
      String searchType           = "";
      String sProjectId           = (String)paramMap.get("projectID");

      // Attribute string constants
      String sMatrixType          = "Type";
      String sMatrixName          = "Name";
      String sMatrixRevision      = "Revision";
      String sMatrixOwner         = "Owner";
      String sMatrixVault         = "Vault";
      String sMatrixDescription   = "Description";
      String sMatrixCurrent       = "Current";
      String sMatrixModified      = "Modified";
      String sMatrixOriginated    = "Originated";
      String sMatrixGrantor       = "Grantor";
      String sMatrixGrantee       = "Grantee";
      String sMatrixPolicy        = "Policy";

      // Symbolic Operator string constants
      String sAnd                = " && ";
      String sEqual              = " == ";
      String sNotEqual           = " != ";
      String sGreaterThan        = " > ";
      String sLessThan           = " < ";
      String sGreaterThanEqual   = " >= ";
      String sLessThanEqual      = " <= ";
      String sMatch              = " ~= ";
      String sQuote              = "\"";
      String sWild               = "*";
      String sOpenParen          = "(";
      String sCloseParen         = ")";
      String sAttribute          = "attribute";
      String sOpenBracket        = "[";
      String sCloseBracket       = "]";

      // Translated string operators
      String sMatrixIncludes     = "Includes";;
      String sMatrixIsExactly    = "IsExactly";;
      String sMatrixIsNot        = "IsNot";
      String sMatrixMatches      = "Matches";
      String sMatrixBeginsWith   = "BeginsWith";
      String sMatrixEndsWith     = "EndsWith";
      String sMatrixEquals       = "Equals";
      String sMatrixDoesNotEqual = "DoesNotEqual";
      String sMatrixIsBetween    = "IsBetween";
      String sMatrixIsAtMost     = "IsAtMost";
      String sMatrixIsAtLeast    = "IsAtLeast";
      String sMatrixIsMoreThan   = "IsMoreThan";
      String sMatrixIsLessThan   = "IsLessThan";
      String sMatrixIsOn         = "IsOn";
      String sMatrixIsOnOrBefore = "IsOnOrBefore";
      String sMatrixIsOnOrAfter  = "IsOnOrAfter";

      String timeZone  = (String)paramMap.get("timeZone");
      double iClientTimeOffset = (new Double(timeZone)).doubleValue();

      String eMatrixFromDate = "";
      String eMatrixToDate = "";

      //to store the parameters passed
      Vector vectParamName      = new Vector();
      StringBuffer  sbWhereExp  = new StringBuffer();
      String sAttrib            = "";
      String sValue             = "";
      String sParamName         = "";
      String sWhereExpression   = "";
      Pattern patternAttribute  = new Pattern("");
      Pattern patternOperator   = new Pattern("");
      String sWhere = "to[" + DomainConstants.RELATIONSHIP_TASK_DELIVERABLE +
                           "].from.to[" +  DomainConstants.RELATIONSHIP_PROJECT_ACCESS_KEY +
                           "].from.from[" +  DomainConstants.RELATIONSHIP_PROJECT_ACCESS_LIST +
                       "].to.id";     
      try {
        searchType = (String)paramMap.get("ComboType");

        //If now search type was passed in then just go back to first search page
        if(searchType == null)
        {
          return queryResultList;   
        }
        //Get the Advanced Findlike parameters
        String sTxtKeyword = (String)paramMap.get("txtKeyword");
        String sTxtFormat  = (String)paramMap.get("txtFormat");
        String sFormat     = (String)paramMap.get("comboFormat");
        if (sTxtKeyword == null){
          sTxtKeyword = "";
        }
        else{
          sTxtKeyword = sTxtKeyword.trim();
        }

        if (sTxtFormat == null){
          sTxtFormat = "";
        }
        else{
          sTxtFormat = sTxtFormat.trim();
        }
        //Set format as All
        if (sFormat == null){
          sFormat = "All";
        }
        else{
          sFormat = sFormat.trim();
        }

        String RelVaultedObjectsRev2 = workspaceVault.RELATIONSHIP_VAULTED_OBJECTS_REV2;
        String FolderName = "to[" + RelVaultedObjectsRev2 + "].from.name";


        //build select params
        SelectList selectStmts = new SelectList();
        //StringList selectStmts = new StringList();
        selectStmts.addName();
        selectStmts.addType();
        selectStmts.addId();
        selectStmts.addRevision();
        selectStmts.addCurrentState();
        selectStmts.addOwner();
        selectStmts.addCreationDate();
        selectStmts.addAttribute(com.matrixone.apps.common.Document.ATTRIBUTE_TITLE);
        selectStmts.addAttribute(com.matrixone.apps.common.Document.SELECT_FILE_NAME);
        selectStmts.addElement(com.matrixone.apps.common.Person.SELECT_FIRST_NAME);
        selectStmts.addElement(com.matrixone.apps.common.Person.SELECT_LAST_NAME);
        selectStmts.addElement(com.matrixone.apps.common.Person.SELECT_COMPANY_NAME);
        selectStmts.addElement(com.matrixone.apps.common.Document.SELECT_FILE_NAME);
        selectStmts.addElement(com.matrixone.apps.domain.DomainObject.SELECT_DESCRIPTION);
        selectStmts.addElement(com.matrixone.apps.common.Organization.SELECT_ORGANIZATION_ID);
        selectStmts.addElement(com.matrixone.apps.common.Organization.SELECT_DUNS_NUMBER);
        selectStmts.addElement(com.matrixone.apps.common.Organization.SELECT_CAGE_CODE);
        selectStmts.addElement(com.matrixone.apps.common.Location.SELECT_ADDRESS1);
        selectStmts.addElement(com.matrixone.apps.common.Location.SELECT_CITY);
        selectStmts.addElement(com.matrixone.apps.common.Location.SELECT_POSTAL_CODE);
        selectStmts.addElement(com.matrixone.apps.common.Location.SELECT_STATE_REGION);
        selectStmts.addElement(com.matrixone.apps.program.BusinessGoal.SELECT_ORGANIZATION_NAME);
        selectStmts.addElement(FolderName);
        selectStmts.addElement("policy");
        selectStmts.addElement(sWhere);
        String sVaultPattern = person.getCompany(context).getAllVaults(context, true);
        
        String whereExp="relationship["+RelVaultedObjectsRev2+"].from.name=="+FolderName1;
        
        //to construct Query object
        matrix.db.Query query   = new matrix.db.Query("");
        query.setBusinessObjectName("*");
        query.setBusinessObjectType(searchType);
        query.setBusinessObjectRevision("*");
        query.setOwnerPattern("*");
        query.setVaultPattern(sVaultPattern);
        if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")){
          queryLimit = "0";
        }
        if(searchMode != null && !"personSearch".equals(searchMode))
        {
               query.setObjectLimit(Short.parseShort(queryLimit));
        }
        //Set the search text
        if (!sTxtKeyword.equals("")) {
          query.setSearchText(sTxtKeyword);
        } else {
          query.setSearchText("");
        }
        //Set the Search Format
        if (!sTxtFormat.equals("")) {
         query.setSearchFormat(sTxtFormat);
        } else if (sFormat != null) {
         if (sFormat.equals("All")) {
            query.setSearchFormat("*");
          } else {
            query.setSearchFormat(sFormat);
          }
        }
  
        // Create an iterator for the map keys.
        Iterator mapKeyItr = null;
        mapKeyItr = paramMap.keySet().iterator();

        //storing all the  parameter names into a vector
        while (mapKeyItr.hasNext()) {
            String key = (String)mapKeyItr.next();
            vectParamName.add(key);
        }

        StringList dateAttrList = FrameworkUtil.split(dateAttrListStr, ",");
        //Loop to get the parameter values
        for (int i = 0; i < vectParamName.size(); i++) {
          String sTextValue            = "";
          String sTextValue1            = "";
          String sTextStartDate        = "";
          String sSelectValue          = "";
          sValue                       = "";
          sParamName = (String)vectParamName.elementAt(i);
          if (sParamName.length() > 4) {
            // Truncating the parameter name and add that in the pattern object
            // Replaced comboDiscriptor with cd to make URL smaller-URL has a 2k limit
            if (sParamName.substring(0,3).equals("cd_")) {
              sAttrib            = sParamName.substring(3,sParamName.length());
              patternAttribute   = new Pattern(sAttrib);
              //get value of descriptor
              String sArrayObj = (String)paramMap.get(sParamName);
              patternOperator = new Pattern(sArrayObj);
              sTextValue = (String)paramMap.get(sAttrib);
              if(dateAttrList.contains(sAttrib))
              {
                sTextStartDate = (String)paramMap.get(sAttrib.replace(' ', '_'));
                sValue = sTextStartDate;
              }
               sTextValue1 = (String)paramMap.get("txt_" + sAttrib);
               //modified if condition bug no.349442
              if (sTextValue == null || sTextValue.equals("") || (sTextValue.equals("*") && sTextValue1 != null && !sTextValue1.equals(""))) {
                sTextValue = sTextValue1;
              }

              //get value entered into select
              sSelectValue = (String)paramMap.get(sAttrib);

              //check if text val exists, if not use select
              if(!patternOperator.match("*")) {
                if ((sTextValue == null) || (sTextValue.equals(""))) {
                  //Update "sValue" only if "sSelectValue" contains some value
                  //Added null check for bug no.349442
                  if (sSelectValue != null && sSelectValue.length()!=0) {
                    sValue = sSelectValue;
                  }
                } else {
                  sValue = sTextValue.trim();
                }
              }
              //To get the where expression if any parameter values exists
              if ((!sValue.equals("")) && (!sValue.equals("*"))) {
                if(patternAttribute.match(sMatrixCurrent))
                {
                  sValue = "const'" + sValue + "'";
                }

                if (sbWhereExp.length() > 0) {
                  sbWhereExp.append(sAnd);
                }
                sbWhereExp.append(sOpenParen);
                if (patternAttribute.match(sMatrixType) ||
                  patternAttribute.match(sMatrixName) ||
                  patternAttribute.match(sMatrixRevision) ||
                  patternAttribute.match(sMatrixOwner) ||
                  patternAttribute.match(sMatrixVault) ||
                  patternAttribute.match(sMatrixDescription) ||
                  patternAttribute.match(sMatrixCurrent) ||
                  patternAttribute.match(sMatrixModified) ||
                  patternAttribute.match(sMatrixOriginated) ||
                  patternAttribute.match(sMatrixGrantor) ||
                  patternAttribute.match(sMatrixGrantee) ||
                  patternAttribute.match(sMatrixPolicy) ||
                  sAttrib.equals("State")) {
                    if(ComboType.equals(DomainObject.TYPE_DOCUMENT) && patternAttribute.match(sMatrixName)){
                      sbWhereExp.append(Document.SELECT_NAME);
                    }else{
                      sbWhereExp.append(sAttrib);
                    }
                } else {
                  // Resource Mgmt Changes
                  if(sAttrib.equals(sAttrCompetency) || sAttrib.equals(sAttrExperience))
                  {

                    sbWhereExp.append("from[" + sRelhasBusinessSkill + "].");
                    sbWhereExp.append(sAttribute + sOpenBracket + sAttrib + sCloseBracket);
                  }else{
                    sbWhereExp.append(sAttribute + sOpenBracket + sAttrib + sCloseBracket);
                  }
                }
                if (patternOperator.match(sMatrixIncludes)) {
                  sbWhereExp.append(sMatch  + "const" + sQuote + sWild + sValue + sWild + sQuote);

                } else if (patternOperator.match(sMatrixIsExactly)) {
                    sbWhereExp.append(sEqual  + "const" + sQuote + sValue + sQuote);

                } else if (patternOperator.match(sMatrixIsNot)) {
                  if(patternAttribute.match(sMatrixCurrent)) {
                    sbWhereExp.append(sNotEqual + sValue);
                  }else{
                    sbWhereExp.append(sNotEqual  + "const" + sQuote + sValue + sQuote);
                  }
                } else if (patternOperator.match(sMatrixMatches)) {
                  sbWhereExp.append(sMatch  + "const" + sQuote + sValue + sQuote);

                } else if (patternOperator.match(sMatrixBeginsWith)) {
                  sbWhereExp.append(sMatch  + "const" + sQuote + sValue + sWild + sQuote);

                } else if (patternOperator.match(sMatrixEndsWith)) {
                  sbWhereExp.append(sMatch  + "const" + sQuote + sWild + sValue + sQuote);

                } else if (patternOperator.match(sMatrixEquals)) {
                  if(patternAttribute.match(sMatrixCurrent))
                  {
                     sbWhereExp.append(sEqual + sValue);
                  }
                  else
                  {
                     sbWhereExp.append(sEqual + sQuote + sValue + sQuote);
                  }

                } else if (patternOperator.match(sMatrixDoesNotEqual)) {
                  if(patternAttribute.match(sMatrixCurrent))
                  {
                      sbWhereExp.append(sNotEqual + sValue);
                  }
                  else
                  {
                      sbWhereExp.append(sNotEqual + sQuote + sValue + sQuote);
                  }
                } else if (patternOperator.match(sMatrixIsBetween)) {
                  sValue       = sValue.trim();

                  int iSpace   = sValue.indexOf(" ");
                  String sLow  = "";
                  String sHigh = "";

                  if (iSpace == -1) {
                    //if there is no space (only one value)
                    //then make both values the same
                    sLow  = sValue.substring(0,sValue.length());
                    if(null != sLow && !"".equals(sLow)){
                      sHigh = sLow;
                    }
                  } else {
                    sLow  = sValue.substring(0,iSpace);
                    sHigh = sValue.substring(sLow.length(),sValue.length());
                  }

                  sbWhereExp.append(sGreaterThanEqual + sLow + sCloseParen + sAnd + sOpenParen);

                  if (patternAttribute.match(sMatrixDescription) ||
                    patternAttribute.match(sMatrixCurrent) ||
                    patternAttribute.match(sMatrixModified) ||
                    patternAttribute.match(sMatrixOriginated) ||
                    patternAttribute.match(sMatrixGrantor) ||
                    patternAttribute.match(sMatrixGrantee) ||
                    patternAttribute.match(sMatrixPolicy)) {
                    sbWhereExp.append(sAttrib);

                  } else {
                    sbWhereExp.append(sAttribute + sOpenBracket + sAttrib + sCloseBracket);

                  }
                  sbWhereExp.append(sLessThanEqual + sHigh);

                } else if (patternOperator.match(sMatrixIsAtMost)) {
                  sbWhereExp.append(sLessThanEqual + sQuote + sValue + sQuote);

                } else if (patternOperator.match(sMatrixIsAtLeast)) {
                  sbWhereExp.append(sGreaterThanEqual + sQuote + sValue + sQuote);

                } else if (patternOperator.match(sMatrixIsMoreThan)) {
                  sbWhereExp.append(sGreaterThan + sQuote + sValue + sQuote);

                } else if (patternOperator.match(sMatrixIsLessThan)) {
                  sbWhereExp.append(sLessThan + sQuote + sValue + sQuote);

                } else if (patternOperator.match(sMatrixIsOn)) {
                  //All objects created after 12:00:00AM and before 11:59:59PM on the selected date are to be retrieved - including the specified time
                  eMatrixFromDate = eMatrixDateFormat.getFormattedInputDateTime(context,sValue, "12:00:00 AM", iClientTimeOffset, context.getLocale());
                  eMatrixToDate   = eMatrixDateFormat.getFormattedInputDateTime(context,sValue, "11:59:59 PM", iClientTimeOffset, context.getLocale());

                  sbWhereExp.append(sGreaterThanEqual + sQuote + eMatrixFromDate + sQuote);
                  sbWhereExp.append("&&");
                  sbWhereExp.append(sAttribute + sOpenBracket + sAttrib + sCloseBracket);
                  sbWhereExp.append(sLessThanEqual + sQuote + eMatrixToDate + sQuote);

                } else if (patternOperator.match(sMatrixIsOnOrBefore)) {
                  //All objects created before 11:59:59PM on the selected date are to be retrieved - including the specified time
                  sValue   = eMatrixDateFormat.getFormattedInputDateTime(context,sValue, "11:59:59 PM", iClientTimeOffset, context.getLocale());
                  sbWhereExp.append(sLessThanEqual + sQuote + sValue + sQuote);
                } else if (patternOperator.match(sMatrixIsOnOrAfter)) {
                  //All objects created after 12:00:00AM on the selected date are to be retrieved - including the specified time
                  sValue = eMatrixDateFormat.getFormattedInputDateTime(context,sValue, "12:00:00 AM", iClientTimeOffset, context.getLocale());
                  sbWhereExp.append(sGreaterThanEqual + sQuote + sValue + sQuote);
                }

                sbWhereExp.append(sCloseParen);
              }
            }
          }
        }

        sWhereExpression    = sbWhereExp.toString();
		
        String dateBegin    = (String)paramMap.get("dateBegin");
        String dateEnd      = (String)paramMap.get("dateEnd");

        String whereStart   = "";
        if (!"".equals(sWhereExpression)){
          whereStart = "&&";
        }

        if ((dateBegin != null) && (!"".equals(dateBegin))) {
            dateBegin   = eMatrixDateFormat.getFormattedInputDateTime(context,dateBegin, "11:59:59 PM", iClientTimeOffset, context.getLocale());
            sWhereExpression += whereStart + "(originated > \"" + dateBegin + "\")";
            whereStart = " && ";
        }

        if ((dateEnd != null) && (!"".equals(dateEnd))) {
          dateEnd = eMatrixDateFormat.getFormattedInputDateTime(context,dateEnd, "12:00:00 AM", iClientTimeOffset, context.getLocale());
          sWhereExpression += whereStart + "(originated < \"" + dateEnd +"\")";
          whereStart = " && ";
        }

        String sParentType = CommonDocument.getParentType(context, ComboType);
        if(sParentType == null)
        {
          sParentType ="";
        }

        if("filesSearch".equals(searchMode) && ( CommonDocument.TYPE_DOCUMENTS.equals(ComboType) || CommonDocument.TYPE_DOCUMENTS.equals(sParentType))){
          sWhereExpression += whereStart + "(" +CommonDocument.SELECT_IS_VERSION_OBJECT + " ~~ \"false\")";
        }
        //to set the where expression in the query
        if ((projId != null) && (!"".equals(projId)) && (!"null".equals(projId)))
        {

          String where =  "to[" + DomainConstants.RELATIONSHIP_MEMBER+ "].from.id [";
          sWhereExpression += (whereStart + "(" + where + "==" + projId + ")");
          whereStart = " && ";
        }
        String sWhereExpression1 = sWhereExpression+"&&"+whereExp;

        
        if(FolderName1 == null || FolderName1.equals("null") || FolderName1.equals(""))
        {
          query.setWhereExpression(sWhereExpression);
        }
        else
        {
          query.setWhereExpression(sWhereExpression1);
        }
        //Executing the Qery and storing the result in the business object list
        BusinessObjectWithSelectList boList = null;
        boList = query.select(context,selectStmts);
        MapList peopleList = FrameworkUtil.toMapList(boList);

        //get users which have Project User role - PMC user only
        if (!((projId == null || ("".equals(projId))) && ("true".equals(isAddPerson))))
        {
          if(searchMode != null && "personSearch".equals(searchMode))
          {
            String sProjectUserRole = PropertyUtil.getSchemaProperty(context,"role_ProjectUser");
            String sExtProjectUserRole = PropertyUtil.getSchemaProperty(context,"role_ExternalProjectUser");

            Role matrixRole = new Role(sProjectUserRole);
            matrixRole.open(context);
            UserList assignments = matrixRole.getAssignments(context);
            StringList projectUsers = new StringList();

            UserItr userItr = new UserItr(assignments);
            while(userItr.next())
            {
              if (userItr.obj() instanceof matrix.db.Person)
              {
                 projectUsers.add(userItr.obj().getName());
              }
            }
            matrixRole.close(context);
            matrixRole = new Role(sExtProjectUserRole);
            matrixRole.open(context);
            assignments = matrixRole.getAssignments(context);
            userItr = new UserItr(assignments);
            while(userItr.next())
            {
              if (userItr.obj() instanceof matrix.db.Person)
              {
                 projectUsers.add(userItr.obj().getName());
              }
            }
            matrixRole.close(context);

            Iterator itr = peopleList.iterator();
            while (itr.hasNext())
            {
               Map item = (Map) itr.next();
               String userName = (String) item.get(person.SELECT_NAME);
               if (projectUsers.indexOf(userName) == -1)
               {
                  continue;
               }
               Map map = new HashMap(5);
               map.put(person.SELECT_ID, (String)item.get(person.SELECT_ID));
               map.put(person.SELECT_NAME, (String) item.get(person.SELECT_NAME));
               map.put(person.SELECT_FIRST_NAME, (String)item.get(person.SELECT_FIRST_NAME));
               map.put(person.SELECT_LAST_NAME, (String)item.get(person.SELECT_LAST_NAME));
               map.put(person.SELECT_COMPANY_NAME, (String)item.get(person.SELECT_COMPANY_NAME));
               map.put(person.SELECT_CURRENT, (String)item.get(person.SELECT_CURRENT));
               queryResultList.add(map);
            }

            //Need to get all people first before setting the limit
            //because if the limit gets set first, the people meet
            //criteria will not get returned.
            int qLimit = (new Integer(queryLimit)).intValue();
            if(queryResultList.size() > qLimit && qLimit > 0){
              queryResultList.subList(qLimit, queryResultList.size()).clear();
              queryResultList.trimToSize();
            }
          }
          else
          {
            queryResultList =  peopleList;
          }
        }
  //ADDED NEWLY
   if(isEPMInstalled)
    {
        Iterator qryItr =  queryResultList.iterator();
        MapList newList = new MapList();
        DomainObject dmObj = DomainObject.newInstance(context);
        String id="";
        String prId="";
        while(qryItr.hasNext())
        {
          Map sMap = (Map)qryItr.next();
          id = (String) sMap.get(DomainConstants.SELECT_ID);
          prId = (String) sMap.get(sWhere);
          dmObj.setId(id);
          if(dmObj.isKindOf(context,DomainConstants.TYPE_IC_DOCUMENT) || dmObj.isKindOf(context,DomainConstants.TYPE_IC_FOLDER))
          {
            if(prId !=null && !"".equals(prId) && !"null".equals(prId))
            {
              if(prId.equals(sProjectId))
              {
                newList.add(sMap);
              }
            }
          }
          else
          {
            newList.add(sMap);
          }
        }
        queryResultList = newList;
    }
  //END ADDED NEWLY

      } catch (MatrixException e) {
      }
      return queryResultList; 
   }
   
   static public BusinessTypeList getAllSubTypes(Context context, String type, BusinessTypeList busTypeList)
      throws MatrixException
    {
       if (busTypeList == null)
           busTypeList = new BusinessTypeList();
       BusinessType busType = new BusinessType(type, context.getVault());
       busType.open(context);

       BusinessTypeList tempTypeList = busType.getChildren(context);
       Iterator itr = tempTypeList.iterator();
       while ( itr.hasNext() ) {
           BusinessType busChildType = (BusinessType) itr.next();
           busTypeList.addElement(busChildType);
           busTypeList = getAllSubTypes(context, busChildType.getName(),busTypeList);
       }
       return busTypeList;
    }

}
