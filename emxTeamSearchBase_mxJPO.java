/*
 *  emxTeamSearchBase.java
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.text.*;
import java.lang.reflect.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.common.*;

/**
 * @version Team 10-5 Release - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxTeamSearchBase_mxJPO extends emxDomainObject_mxJPO
{

  protected static StringBuffer sbLockedSelect       = null;
  protected static StringBuffer sbSelRelActVerRev    = null;

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Team 10-5
     */
    public emxTeamSearchBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
        // Updating StringBuffers
        sbLockedSelect = new StringBuffer(56);
        sbLockedSelect.append("relationship[");
        sbLockedSelect.append(CommonDocument.RELATIONSHIP_ACTIVE_VERSION);
        sbLockedSelect.append("].to.locked");

        sbSelRelActVerRev = new StringBuffer(56);
        sbSelRelActVerRev.append("relationship[");
        sbSelRelActVerRev.append(CommonDocument.RELATIONSHIP_ACTIVE_VERSION);
        sbSelRelActVerRev.append("].to.revision");

    }

  /**
   * This method will be called when ever we invoke the JPO without
   * calling any method.explicitly
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no value
   * @return int
   * @throws Exception if the operation fails
   * @since Team 10-5
   */
  public int mxMain(Context context, String[] args)
    throws Exception
  {
    if (!context.isConnected())
      throw new Exception(ComponentsUtil.i18nStringNow("emxTeamCentral.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
      return 0;
  }

  /**
  * Get ProjectId by passing Workspace Vault Id.
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds input arguments.
  * @return Map containing results.
  * @exception Exception if the operation fails.
  */

    static protected Map getWorkspaceMap(matrix.db.Context context, String vaultId) throws MatrixException
    {
      String typeWorkspace  = PropertyUtil.getSchemaProperty(context,"type_Project");
      String typeProjectVault  = PropertyUtil.getSchemaProperty(context,"type_ProjectVault" );
      String relSubVaults = PropertyUtil.getSchemaProperty(context,"relationship_SubVaults");
      String relProjectVault = PropertyUtil.getSchemaProperty(context,"relationship_ProjectVaults" );
      String relVaultedDocuments  = PropertyUtil.getSchemaProperty(context,"relationship_VaultedDocuments");

      DomainObject domainObject = new DomainObject();
      domainObject.setId(vaultId);
      Pattern relPattern  = new Pattern(relSubVaults);
      relPattern.addPattern(relProjectVault);
      Pattern typePattern = new Pattern(typeProjectVault);
      typePattern.addPattern(typeWorkspace);

      Pattern includeTypePattern = new Pattern(typeWorkspace);

      StringList objSelects = new StringList(5);
      objSelects.addElement(domainObject.SELECT_ID);
      objSelects.addElement(domainObject.SELECT_NAME);
      //need to include Type as a selectable if we need to filter by Type
      objSelects.addElement(domainObject.SELECT_TYPE);
      objSelects.addElement(domainObject.SELECT_OWNER);
      objSelects.addElement(domainObject.SELECT_CURRENT);

      domainObject.open(context);
      MapList mapList = domainObject.getRelatedObjects(context,
                                               relPattern.getPattern(),
                                               typePattern.getPattern(),
                                               objSelects,
                                               null,
                                               true,
                                               false,
                                               (short)0,
                                               "",
                                               "",
                                               includeTypePattern,
                                               null,
                                               null);

      Iterator mapItr = mapList.iterator();
      Map map = null;
      while(mapItr.hasNext())
      {
        map = (Map)mapItr.next();
      }
    return map;
    }


  /**
  * Get Files for the specified criteria
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds input arguments.
  *   ownedFiles - a String true/false to show only files owned by the User
  *   owner - a String containing the owner to search for.
  *   name - a String containg the name pattern to search for.
  *   keywords - a String containg the text search patterns.
  *   WorkspaceFolder - a String containg the Workspace Folder pattern to searchfor.
  *   WorkspaceFolderId - a String containing the Object ID of the Workspace Folder
  *   createdAfter - a String containing a created after date.
  *   createdBefore - a String containing a created before date.
  *   matchCase - a String true/false to match case.
  *   timeZone - a String containing the client timezone.
  * @return MapList containing search result.
  * @exception Exception if the operation fails.
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getFilesSearchResult(Context context , String[] args)
                    throws Exception
  {
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
    //Retrieve Search criteria
    //Added for Bug 312500 - Begin
    String queryLimit = (String)paramMap.get("QueryLimit");
    short limit = (short)0;
    try {
        if (queryLimit != null && !"null".equals(queryLimit) && !"".equals(queryLimit) ) {
        limit = (short)Integer.parseInt(queryLimit);
    }
    }
    catch(Exception Ex) {
        throw Ex;
    }

    //Added for Bug 312500 - End
    String sOwnedFiles           = (String)paramMap.get("ownedFiles");
    String sOwner                = (String)paramMap.get("owner");
           sOwner                = sOwner.trim();
    String sFileName             = (String)paramMap.get("filename");
    String sKeywords             = (String)paramMap.get("keywords");
    String sWorkspaceFolder      = (String)paramMap.get("WorkspaceFolder");
    String sWorkspaceFolderId    = (String)paramMap.get("workspaceFolderId");
    String sWorkspacesubfolders  = (String)paramMap.get("workspacesubfolders");
    String sCreatedAfter         = (String)paramMap.get("createdAfter");
    String sCreatedBefore        = (String)paramMap.get("createdBefore");
    String matchCase             = (String)paramMap.get("matchCase");
    String timeZone              = (String)paramMap.get("timeZone");
    //Added for Bug 312500 - Begin
    String languageStr           = (String)paramMap.get("languageStr");
    //Added for Bug 312500 - End
    String sTypeProjectVault    = PropertyUtil.getSchemaProperty(context,"type_ProjectVault");
    String sTypeProjectMember   = PropertyUtil.getSchemaProperty(context,"type_ProjectMember");
//    String sTypeDocument        = PropertyUtil.getSchemaProperty(context,"type_Document"); Bug No :303156
    String sTypeDocument        = PropertyUtil.getSchemaProperty(context,"type_DOCUMENTS");
    String sRelSubVaults        = PropertyUtil.getSchemaProperty(context,"relationship_SubVaults");
    String sRelVaultedDocuments = PropertyUtil.getSchemaProperty(context,"relationship_VaultedDocuments");
    String strTitleAttr         = PropertyUtil.getSchemaProperty(context,"attribute_Title" );
    com.matrixone.apps.common.Person PersonObject = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);

    MapList templateMapList      = new MapList();
    MapList constructedList      = new MapList();
    DomainObject templateObj     = DomainObject.newInstance(context);
    Company      company         = null;
    int count                    = 0;
    String sFilterKey            = "";
    String sParams               = "";
    StringBuffer busWhere        = new StringBuffer(1024);
    boolean bFlag                = false;
    double clientTZOffset        = new Double(timeZone).doubleValue();
    java.util.Locale localeObj =   (java.util.Locale) paramMap.get("localeObj");
  //Formatting Date to Ematrix Date Format

  if(sCreatedAfter!=null && !"".equals(sCreatedAfter)){
      sCreatedAfter = sCreatedAfter.trim();
      java.util.Date date = new Date(sCreatedAfter);
      int intDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
      DateFormat formatter = DateFormat.getDateInstance(intDateFormat, localeObj);
      sCreatedAfter =  formatter.format(date);
      //String strInputTime = "00:00:00 AM";
      String strInputTime = "11:59:59 PM";
      sCreatedAfter= eMatrixDateFormat.getFormattedInputDateTime(context,sCreatedAfter,strInputTime,clientTZOffset,localeObj);

  }
  if(sCreatedBefore!=null && !"".equals(sCreatedBefore)){
        sCreatedBefore = sCreatedBefore.trim();
      java.util.Date date = new Date(sCreatedBefore);
      int intDateFormat = eMatrixDateFormat.getEMatrixDisplayDateFormat();
      DateFormat formatter = DateFormat.getDateInstance(intDateFormat, localeObj);
      sCreatedBefore =  formatter.format(date);
     // String strInputTime = "12:00:00 PM";
     String strInputTime = "00:00:00 AM";
      sCreatedBefore= eMatrixDateFormat.getFormattedInputDateTime(context,sCreatedBefore,strInputTime,clientTZOffset,localeObj);

  }
  String loggedInUser = context.getUser();
  com.matrixone.apps.common.Person person = PersonObject.getPerson(context);

  //Get the vault list for the query
  StringBuffer employeeRel = new StringBuffer(32);
  employeeRel.append("to[");
  employeeRel.append(DomainConstants.RELATIONSHIP_EMPLOYEE);
  employeeRel.append("].from.id");

  String myCompanyId = person.getInfo(context, employeeRel.toString());
  company = (Company)DomainObject.newInstance(context,DomainConstants.TYPE_COMPANY,DomainConstants.TEAM);
  company.setId(myCompanyId);

  StringList companyVaultList = company.getExtendedVaults(context);

  String vaultString = "";
  String vaultName="";
  Iterator vaultItr = companyVaultList.iterator();

  StringList vaultNames = new StringList();
  while(vaultItr.hasNext())
  {
    if(vaultString.length() == 0)
    {
      vaultString = vaultString + (String)vaultItr.next();
    }
    else
    {
      vaultName = (String)vaultItr.next();
      if((vaultString.length() + vaultName.length()) > 256)
      {
        vaultNames.addElement(vaultString);
        vaultString = vaultName;
      }
      else
      {
        vaultString = vaultString + "," + vaultName;
      }
    }
  }

  vaultNames.addElement(vaultString);

  //Get all the secondary vaults of the company and add them to vault list
  String secondaryVaults= company.getSecondaryVaults(context);

  if(secondaryVaults!=null && !secondaryVaults.equals(""))
  {
    StringList vaultList = FrameworkUtil.split(secondaryVaults,",");
    Iterator itr = vaultList.iterator();
    vaultString = "";
    while (itr.hasNext())
    {
      if(vaultString.length() == 0)
      {
        vaultString = vaultString + (String)itr.next();
      }
      else
      {
        vaultName = (String)itr.next();
        if((vaultString.length() + vaultName.length()) > 256)
        {
          vaultNames.addElement(vaultString);
          vaultString = vaultName;
        }
        else
        {
          vaultString = vaultString + "," + vaultName;
        }
      }
    }
    vaultNames.addElement(vaultString);
  }


    if (sKeywords == null)
    {
      sKeywords = "";
    }

    //get only the latest revisions
    busWhere.append("(revision == last)");

    if ((null == sKeywords.trim()         ||
        ("null").equals(sKeywords.trim()) ||
        ("").equals(sKeywords.trim())     ||
        ("*").equals(sKeywords.trim())))
    {
      sKeywords = null;
    }

    if ((null==sFileName                  ||
        ("null").equals(sFileName.trim()) ||
        ("").equals(sFileName.trim())     ||
        ("*.*").equals(sFileName.trim())))
    {
      sFileName = "*";
    }
    boolean nameMatchCase = false;
    if("True".equals(matchCase))
    {
      nameMatchCase = true;
    }

    if (sFileName.indexOf(",") != -1)
    {

      // if "," is given for the file name then it is seperated with string tokenizer
      bFlag = true;
      StringTokenizer stToken = new StringTokenizer(sFileName, ",");
      int sTokenSize          = stToken.countTokens();
      int sSize               = 0;

      if (busWhere.length() > 0)
      {
        busWhere.append(" && ");
      }
      while (stToken.hasMoreTokens())
      {
        sSize += 1;
        String sToken = stToken.nextToken();

        // if the name parameter is only file alone
        if (sFileName.indexOf("*") == -1)
        {
          if ( sSize == sTokenSize )
          {
            if(nameMatchCase)
            {
              busWhere.append("(name");
              busWhere.append(" == \"");
              busWhere.append(sToken);
              busWhere.append("\")");
            }
            else
            {
              busWhere.append("(name");
              busWhere.append(" ~~ \"");
              busWhere.append(sToken);
              busWhere.append("\")");
            }
          }
          else
          {
            if(nameMatchCase)
            {
              busWhere.append("(name");
              busWhere.append(" == \"");
              busWhere.append(sToken);
              busWhere.append("\")");
              busWhere.append("||");
            }
            else
            {
              busWhere.append("(name");
              busWhere.append(" ~~ \"");
              busWhere.append(sToken);
              busWhere.append("\")");
              busWhere.append("||");
            }
          }
        }
        else
        {
          // if the name parameter is * and *  with some character
          if (!sFileName.trim().equals("*"))
          {
            if ( sSize == sTokenSize )
            {
              if(nameMatchCase)
              {
                busWhere.append("(name");
                busWhere.append(" ~= \"");
                busWhere.append(sToken);
                busWhere.append("\")");
              }
              else
              {
                busWhere.append("(name");
                busWhere.append(" ~~ \"");
                busWhere.append(sToken);
                busWhere.append("\")");
              }
            }
            else
            {
              if (nameMatchCase)
              {
                busWhere.append("(name");
                busWhere.append(" ~= \"");
                busWhere.append(sToken);
                busWhere.append("\")");
                busWhere.append("||");
              }
              else
              {
                busWhere.append("(name");
                busWhere.append(" ~~ \"");
                busWhere.append(sToken);
                busWhere.append("\")");
                busWhere.append("||");
              }
            }
          }
        }
      }
    }

    if (!bFlag)
    {
      if (sFileName.indexOf("*") == -1)
      {
        if (busWhere.length() > 0)
        {
          busWhere.append(" && ");
        }
        if (nameMatchCase)
        {
          busWhere.append("(name");
          busWhere.append(" == \"");
          busWhere.append(sFileName);
          busWhere.append("\")");
        }
        else
        {
          busWhere.append("(name");
          busWhere.append(" ~~ \"");
          busWhere.append(sFileName);
          busWhere.append("\")");
        }
      }
      else
      {
        if (!sFileName.trim().equals("*"))
        {
          if (busWhere.length() > 0)
          {
            busWhere.append(" && ");
          }
          if (nameMatchCase)
          {
            busWhere.append("(name");
            busWhere.append(" ~= \"");
            busWhere.append(sFileName);
            busWhere.append("\")");
          }
          else
          {
            busWhere.append("(name");
            busWhere.append(" ~~ \"");
            busWhere.append(sFileName);
            busWhere.append("\")");
          }
        }
      }
    }

    if ((null==sOwnedFiles           ||
        ("null").equals(sOwnedFiles) ||
        ("").equals(sOwnedFiles)))
    {
      sOwnedFiles = "true";
    }

    String owner = "*";
    if (sOwnedFiles.equals("true"))
    {
      owner = context.getUser();
    }

    if (sOwner.equals("*"))
    {
      sOwner = "";
    }

    if (sOwner==null ||
       ("null").equals(sOwner) ||
       (",").equals(sOwner))
    {
      sOwner = "";
    }

    String strToken = "";
    StringTokenizer st = new StringTokenizer(sOwner,",");
    if (!sOwnedFiles.equals("true"))
    {
      while (st.hasMoreTokens())
      {
        strToken =     st.nextToken();
        if (owner.equals("*"))
        {
            owner = "";
        }
        if (!strToken.equals(""))
        {
          owner = owner + "," + strToken;
        }
      }
    }

    if (sCreatedBefore==null)
    {
      sCreatedBefore = "";
    }

    if (sCreatedBefore.trim().length()!=0)
    {
      if (busWhere.length() > 0)
      {
        busWhere.append(" && ");
      }
      busWhere.append("(originated < \"");
      busWhere.append(sCreatedBefore);
      busWhere.append("\")");
    }

    if (sCreatedAfter == null)
    {
      sCreatedAfter = "";
    }
    if (sCreatedAfter.trim().length() != 0)
    {
      if(busWhere.length() > 0)
      {
         busWhere.append(" && ");
      }
      busWhere.append("(originated >= \"");
      busWhere.append(sCreatedAfter);
      busWhere.append("\")");
    }

    if (sWorkspacesubfolders == null)
    {
      sWorkspacesubfolders = "";
    }

    String folderId ="";
    BusinessObjectList boDocumentList = new BusinessObjectList();
    if (sWorkspaceFolder.equals("*"))
    {
      //build specific where clauses
      if (!sWorkspacesubfolders.equals("True"))
      {
        if (busWhere.length() > 0)
        {
           busWhere.append(" && ");
        }
        busWhere.append("(!to[");
        busWhere.append(sRelVaultedDocuments);
        busWhere.append("].from.to[");
        busWhere.append(sRelSubVaults);
        busWhere.append("].id  !~~ \"zz\")");
      }

      if (busWhere.length() > 0)
      {
        busWhere.append(" && ");
      }
      busWhere.append("((\"to[");
      busWhere.append(sRelVaultedDocuments);
      busWhere.append("].from.id\" !~~ \"zz\")) && (current.access[read] == TRUE)");

      StringList selects = new StringList(3);
      selects.addElement(DomainObject.SELECT_NAME);
      selects.addElement(DomainObject.SELECT_ID);
      selects.addElement(DomainObject.SELECT_TYPE);

      matrix.db.Query query = new matrix.db.Query();
      query.create(context);

      Iterator vaultNamesItr = vaultNames.iterator();
      while(vaultNamesItr.hasNext())
      {
        String vault = (String)vaultNamesItr.next();
        query.setBusinessObjectType(sTypeDocument);
        query.setBusinessObjectName("*");
        query.setBusinessObjectRevision("*");
        query.setVaultPattern(vault);
        query.setOwnerPattern(owner);
        query.setWhereExpression(busWhere.toString());
        query.setSearchText(sKeywords);
        try
        {
            ContextUtil.startTransaction(context, false);
            //MapList docMapList = FrameworkUtil.toMapList(query.select(context, selects));
            MapList docMapList = FrameworkUtil.toMapList(query.getIterator(context, selects,limit), FrameworkUtil.MULTI_VALUE_LIST);
            templateMapList.addAll(docMapList);
            ContextUtil.commitTransaction(context);
        }
        catch(Exception ex)
        {
            ContextUtil.abortTransaction(context);
        }


        //Added for Bug 312500 - Begin
        if(templateMapList.size() >= limit) {
            break;
        }
        //Added for Bug 312500 - End
      }
    }
    else
    {
      // loop thro. the folders/subfolders, do expand and get the doc list
      // put the doc list into a set, and do a keyword search on the set
      // get the final results as a bo list

      if (sKeywords != null)
      {
        if (busWhere.length() > 0)
        {
           busWhere.append(" && ");
        }
        busWhere.append("(search[");
        busWhere.append(sKeywords);
        busWhere.append("] == TRUE)");
      }

      if (owner.trim().length() > 0)
      {
        st = new StringTokenizer(owner, ",");
        String ownerWhere = "(";
        String ownerName = "";
        while (st.hasMoreTokens())
        {
          ownerName = st.nextToken();
          if (ownerName.equals("*"))
            break;

          if(ownerWhere.equals("("))
          {
            ownerWhere += "(owner == \""+ownerName+"\")";
          }
          else
          {
            ownerWhere += "|| (owner == \""+ownerName+"\")";
          }
        }
        ownerWhere += ")";
        if(!ownerWhere.equals("()"))
        {
          if (busWhere.length() > 0)
          {
            busWhere.append(" && ");
          }
          busWhere.append(ownerWhere);
        }
      }

      Pattern relPattern  = null;
      Pattern typePattern = null;
      MapList mapList     = new MapList();
      MapList vaultList   = new MapList();

      st          = new StringTokenizer(sWorkspaceFolderId,",");
      relPattern  = new Pattern(sRelVaultedDocuments);
      typePattern = new Pattern(sTypeDocument);

      short expandLevel = (short)1;
      //if sub-folders are selected, modify the rel and type patterns
      if (sWorkspacesubfolders.equals("True"))
      {
        expandLevel = (short)0;
        relPattern.addPattern(sRelSubVaults);
        typePattern.addPattern(sTypeProjectVault);
      }
      //iterate thro. the folders
      while (st.hasMoreTokens())
      {
        folderId = st.nextToken();
        Pattern includeRelPattern = new Pattern(sRelVaultedDocuments);
        StringList objSelects = new StringList(3);
        objSelects.addElement(DomainObject.SELECT_ID);
        objSelects.addElement(DomainObject.SELECT_NAME);
        objSelects.addElement(DomainObject.SELECT_TYPE);
        templateObj.setId(folderId);
        MapList docMapList = templateObj.getRelatedObjects(context,
                                                      relPattern.getPattern(),
                                                      typePattern.getPattern(),
                                                      objSelects,
                                                      new StringList(),
                                                      false,
                                                      true,
                                                      expandLevel,
                                                      busWhere.toString(),
                                                      null,
                                                      Short.parseShort(queryLimit),
                                                      null,
                                                      includeRelPattern,
                                                      null);


        Iterator docListItr = docMapList.iterator();
        while(docListItr.hasNext())
        {
          //Added for Bug 312500 - Begin
          ++count;
          //Added for Bug 312500 - End
          Map docMap = (Map)docListItr.next();
          String docId = (String)docMap.get(DomainObject.SELECT_ID);
          boDocumentList.addElement(new BusinessObject(docId));
          //Added for Bug 312500 - Begin
          if(count >= limit) {
              break;
          }
          //Added for Bug 312500 - End
        }
      }
      try
      {
        MQLCommand prMQL  = new MQLCommand();
        prMQL.open(context);
        String prMQLString        = "";
        boolean isQueryExecuted   = false;
        String Result             = "";
        String mqlError           = "";
        String sWhereAfterstmt    = "";
        String sWhereBfstmt       = "";

        matrix.db.Set documentSet = new matrix.db.Set(".emxTempSet");
        try
        {
          documentSet.open(context);
          documentSet.appendList(boDocumentList);
          documentSet.setBusinessObjects(context);
          documentSet.close(context);
          boDocumentList  = new BusinessObjectList();
          prMQLString     = "add query $1 bus $2 $3 $4 vault $5 where $6;";

          isQueryExecuted = prMQL.executeCommand(context,prMQLString, "mxdocsearch", sTypeDocument, "*", "*", "*", busWhere.toString().trim());
          Result          = prMQL.getResult();
          mqlError        = prMQL.getError();
          prMQLString     = "eval query $1 over set $2 into set $3;";
          isQueryExecuted = prMQL.executeCommand(context,prMQLString, "mxdocsearch", ".emxTempSet", ".emxResultSet");
          Result   = prMQL.getResult();
          mqlError = prMQL.getError();
          if(mqlError != null && mqlError.equals(""))
          {
            matrix.db.Set qSet = new matrix.db.Set(".emxResultSet");
            qSet.open(context);
            boDocumentList     = qSet.getBusinessObjects(context);
            qSet.close(context);
            prMQLString     = "delete query $1 ;";
            isQueryExecuted = prMQL.executeCommand(context,prMQLString, "mxdocsearch");
            Result          = prMQL.getResult();
            mqlError        = prMQL.getError();
            prMQL.close(context);
            qSet.remove(context);
          }
          else
          {
            prMQL.close(context);
          }
          documentSet.remove(context);
        }
        catch(Exception e) { }

      }
      catch(Exception e)
      {
        boDocumentList = new BusinessObjectList();
      }
      //populate the mapList before passing for pagination
      BusinessObjectItr boItr = new BusinessObjectItr(boDocumentList);
      while(boItr.next())
      {
        BusinessObject boDoc = boItr.obj();
        String docId         = boDoc.getObjectId();
        Hashtable hashTableFinal = new Hashtable();
        hashTableFinal.put(DomainObject.SELECT_ID,docId);
        templateMapList.add(hashTableFinal);
      }
    }

  String selAttrTitle     = "attribute["+DomainObject.ATTRIBUTE_TITLE+"]";
  String selVaultId       = "to["+sRelVaultedDocuments+"].from.id";
  String selVaultNames    = "to["+sRelVaultedDocuments+"].from.name";

  Iterator templateMapListItr = templateMapList.iterator();
  StringList docIdList = new StringList();
//Added for Bug 312500 - Begin
  count=0;
//Added for Bug 312500 - End
  while(templateMapListItr.hasNext())
  {
    Map map = (Map)templateMapListItr.next();
    String docId = (String)map.get(DomainObject.SELECT_ID);
    if(!docIdList.contains(docId) && !docId.equals("#DENIED!"))
    {
      //Added for Bug 312500 - Begin
      ++count;
      //Added for Bug 312500 - End
      docIdList.addElement(docId);
      //Added for Bug 312500 - Begin
      if(count >=limit) {
          StringBuffer sMessage = new StringBuffer(64);
          sMessage.append(EnoviaResourceBundle.getProperty(context,  "emxTeamCentralStringResource", new Locale(languageStr), "emxTeamCentral.FileSearch.MaxLimitWarning"));
          sMessage.append(" ");
          sMessage.append(limit);
          sMessage.append(" ");
          sMessage.append(EnoviaResourceBundle.getProperty(context, "emxTeamCentralStringResource", new Locale(languageStr), "emxTeamCentral.FileSearch.MaxLimitReached"));
          emxContextUtil_mxJPO.mqlNotice(context,sMessage.toString());
          break;
      }
      //Added for Bug 312500 - End
    }
  }
  StringList selects = new StringList(11);
  selects.addElement(DomainObject.SELECT_NAME);
  selects.addElement(DomainObject.SELECT_ID);
  selects.addElement(DomainObject.SELECT_TYPE);
  selects.addElement(DomainObject.SELECT_REVISION);
  selects.addElement(sbLockedSelect.toString());
  selects.addElement(sbSelRelActVerRev.toString());
  selects.addElement(DomainObject.SELECT_DESCRIPTION);
  selects.addElement(selAttrTitle);
        final String sRelObjectRoute = PropertyUtil.getSchemaProperty(context,"relationship_ObjectRoute");
        final String selRouteId = "from["+sRelObjectRoute+"].to.id";

  selects.addElement(selRouteId);
  DomainObject.MULTI_VALUE_LIST.add(selVaultId);
  selects.addElement(selVaultId);
  DomainObject.MULTI_VALUE_LIST.add(selVaultNames);
  selects.addElement(selVaultNames);

  //get the details of the document objects for the current page
  templateMapList = DomainObject.getInfo(context, (String [])docIdList.toArray(new String []{}), selects);
  templateMapListItr = templateMapList.iterator();
  Hashtable projectHash = new Hashtable();

    while(templateMapListItr.hasNext())
    {
      // Modified for the bug 366454 from Hashtable to HashMap
      HashMap hashTableFinal  = new HashMap();
      Map map = (Map)templateMapListItr.next();
      String docId = (String)map.get(DomainObject.SELECT_ID);

      // get vault names for display
      StringList vaultNameList = new StringList();
      try
      {
        vaultName = (String)map.get(selVaultNames);
        if (vaultName != null)
        {
          vaultNameList.addElement(vaultName);
        }
      }
      catch (ClassCastException classCastEx)
      {
        vaultNameList = (StringList)map.get(selVaultNames);
      }
      // get vault ids
      StringList vaultIdList = new StringList();
      StringList workspaceNameList = new StringList();
      try
      {
        String folderVaultId = (String)map.get(selVaultId);
        if (folderVaultId != null)
        {
          vaultIdList.addElement(folderVaultId);
        }
      }
      catch (ClassCastException classCastEx)
      {
        vaultIdList = (StringList)map.get(selVaultId);
      }

      vaultName = "";
      String workspaceNames = "";
      String vaultId1 = "";
      Iterator vaultIdItr = vaultIdList.iterator();
      Iterator vaultNameItr = vaultNameList.iterator();
      // get the project for the documents, put the vaultId-projectNames
      // into a hashTable
      // check for each vaultId, if it is in the hashtable, take the
      // project name from it.
      while(vaultIdItr.hasNext())
      {
        String vaultId = (String)vaultIdItr.next();
        String tempVaultName = (String)vaultNameItr.next();
        if(vaultId != null && !vaultId.equals("#DENIED!"))
        {
          vaultName += tempVaultName + ";";
          vaultId1 += vaultId + ";";
          if(!projectHash.containsKey(vaultId))
          {
            Map projectMap = getWorkspaceMap(context, vaultId);
            String workspaceName = "";
            if(projectMap !=null)
            {
              String workspaceOwner = (String)projectMap.get(DomainObject.SELECT_OWNER);
              String workspaceState = (String)projectMap.get(DomainObject.SELECT_CURRENT);

              if (!loggedInUser.equals(workspaceOwner) &&
                  !workspaceState.equals("Active"))
              {
                workspaceName = "";
              }
              else
              {
                workspaceName = (String)projectMap.get(DomainObject.SELECT_NAME);
              }
            }
            else
            {
              workspaceName="";
            }
            projectHash.put(vaultId, workspaceName);
          }
          String workspaceNameHash = (String)projectHash.get(vaultId);
          if(workspaceNameHash != null &&
             workspaceNameHash.length() != 0)
          {
            if (!workspaceNameList.contains(workspaceNameHash))
            {
              workspaceNameList.addElement(workspaceNameHash);
              workspaceNames = workspaceNames + workspaceNameHash + "; ";
            }
          }
        }
      }

      try
      {
        if (workspaceNames != null)
        {
          String locked       = (String)map.get(sbLockedSelect.toString());
          hashTableFinal.put("lock",locked);
          hashTableFinal.put("workspaceNames",workspaceNames);
          hashTableFinal.put(templateObj.SELECT_ID,docId);
          hashTableFinal.put(templateObj.SELECT_NAME, (String)map.get(selAttrTitle));
          hashTableFinal.put(templateObj.SELECT_TYPE, (String)map.get(DomainObject.SELECT_TYPE));
          hashTableFinal.put(sbSelRelActVerRev.toString(), (String)map.get(sbSelRelActVerRev.toString()));
          hashTableFinal.put("folder", vaultName);
          hashTableFinal.put("folderId", vaultId1);
          hashTableFinal.put("version", (String)map.get(DomainObject.SELECT_REVISION));
          hashTableFinal.put("desc", (String)map.get(DomainObject.SELECT_DESCRIPTION));

          //check if the doc is connected to a route
          String routeId = (String)map.get(selRouteId);
          if(routeId!= null && !routeId.equals(""))
          {
            hashTableFinal.put("inRoute", "true");
          }
          else
          {
            hashTableFinal.put("inRoute", "false");
          }
          constructedList.add(hashTableFinal);
        }
      }
      catch(Exception e)
      {
      }
    }
    return constructedList;
  }

  /**
   * getLockImage - This method is used to show the Lock image.
   *                This method is called from the Column Lock Image.
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - objectList MapList
   * @return Object of type Vector
   * @throws Exception if the operation fails
   * @since V10 Patch1
   */
  public Vector getLockImage(Context context, String[] args)
    throws Exception
  {
    Vector showLock= new Vector();
    String statusImageString = "";
    try
    {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList)programMap.get("objectList");
      Map objectMap = null;
      boolean boolRouted = false;
      boolean boolLock = false;
        final String sRelObjectRoute = PropertyUtil.getSchemaProperty(context,"relationship_ObjectRoute");
        final String selRouteId = "from["+sRelObjectRoute+"].to.id";

      Iterator objectListItr = objectList.iterator();
      while (objectListItr.hasNext())
      {
        objectMap = (Map)objectListItr.next();
        statusImageString = "";

        String docLocked = (String)objectMap.get(sbLockedSelect.toString());
        if(UIUtil.isNullOrEmpty(docLocked))
        {
        	String id = (String)objectMap.get(DomainConstants.SELECT_ID);
        	DomainObject dombj = new DomainObject(id);
        	docLocked =	dombj.getInfo(context, sbLockedSelect.toString());
        }
        
        if ((docLocked == null) || (docLocked.equals("null")))
          docLocked ="";

        if (docLocked.equals("TRUE"))
          boolLock = true;
        else
          boolLock = false;

        String docRouted =(String) objectMap.get("inRoute");
        if ((docRouted == null) || (docRouted.equals("null")))
          docRouted = (String)objectMap.get(selRouteId);

        if ((docRouted == null) || (docRouted.equals("null")))
          docRouted = "";

        if (docRouted.equals("true"))
          boolRouted = true;
        else
          boolRouted = false;

        if (boolRouted && boolLock)
        {
           statusImageString = "<img border='0' src='../teamcentral/images/iconLockedRoute.gif' alt=''></img>";
         }
        else if (boolLock)
        {
           statusImageString = "<img border='0' src='../teamcentral/images/iconLocked.gif' alt=''></img>";
         }
        showLock.add(statusImageString);
      }

    }
    catch (Exception ex)
    {
      throw ex;
    }
    finally
    {
    	//XSSOK
      return  showLock;
    }
  }

  /**
   * showCheckbox - This Method will check whether the check box must be
   *                enabled or Disabled
   *                Called in the CheckBox column
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - objectList MapList
   * @return Object of type Vector
   * @throws Exception if the operation fails
   * @since Team 10-5
   */
  public Vector showCheckbox(Context context, String[] args)
    throws Exception
  {
    Vector enableCheckbox = new Vector();
    try
    {
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      MapList objectList = (MapList)programMap.get("objectList");
      Map  objectMap = null;
      boolean isLocked;
      String bTmp = "";

      Iterator objectListItr = objectList.iterator();
      while (objectListItr.hasNext())
      {
        objectMap = (Map)objectListItr.next();
        bTmp = (String)objectMap.get(sbLockedSelect.toString());
        if ((bTmp == null) || (bTmp.equals("null")))
          bTmp = "";

        if (bTmp.equals("TRUE"))
          isLocked = true;
        else
          isLocked = false;

        if (isLocked)
          enableCheckbox.add("false");
        else
          enableCheckbox.add("true");
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
  * Get Unmanaged Files for the specified criteria
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds input arguments.
  *   ownedFiles - a String true/false to show only files owned by the User
  *   owner - a String containing the owner to search for.
  *   name - a String containg the name pattern to search for.
  *   keywords - a String containg the text search patterns.
  *   createdAfter - a String containing a created after date.
  *   createdBefore - a String containing a created before date.
  *   matchCase - a String true/false to match case.
  *   timeZone - a String containing the client timezone.
  * @return MapList containing search result.
  * @exception Exception if the operation fails.
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getUnmanagedFilesSearchResult(Context context , String[] args)
                    throws Exception
  {
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);

    //Retrieve Search criteria
    String sOwnedFiles           = (String)paramMap.get("ownedFiles");
    String sOwner                = (String)paramMap.get("owner");
    sOwner = sOwner.trim();
    String sFileName             = (String)paramMap.get("filename");
    String sKeywords             = (String)paramMap.get("keywords");
    String sCreatedAfter         = (String)paramMap.get("createdAfter");
    String sCreatedBefore        = (String)paramMap.get("createdBefore");
    String matchCase             = (String)paramMap.get("matchCase");
    String timeZone              = (String)paramMap.get("timeZone");
    String queryLimit            = (String)paramMap.get("QueryLimit");
    
    String sTypeDocument        = PropertyUtil.getSchemaProperty(context,"type_Document");
    String typePart             = PropertyUtil.getSchemaProperty(context,"type_Part" );
    String typeRTSQuotation     = PropertyUtil.getSchemaProperty(context,"type_RTSQuotation" );
    String typeRts              = PropertyUtil.getSchemaProperty(context,"type_RequestToSupplier" );
    String typePackage          = PropertyUtil.getSchemaProperty(context,"type_Package" );
    String sRelReferenceDoc     = PropertyUtil.getSchemaProperty(context,"relationship_ReferenceDocument");
    String sRelVaultedDocuments = PropertyUtil.getSchemaProperty(context,"relationship_VaultedDocuments");
    String sRelObjectRoute      = PropertyUtil.getSchemaProperty(context,"relationship_ObjectRoute");
    String strTitleAttr         = PropertyUtil.getSchemaProperty(context,"attribute_Title");

    com.matrixone.apps.common.Person person = (com.matrixone.apps.common.Person) DomainObject.newInstance(context, DomainConstants.TYPE_PERSON);
    com.matrixone.apps.common.Person Person = person.getPerson(context);

    MapList mapList              = new MapList();
    Company company              = null;

    int count                    = 0;
    double clientTZOffset        = new Double(timeZone).doubleValue();
    java.util.Locale localeObj =   (java.util.Locale) paramMap.get("localeObj");

    //Formatting Date to Ematrix Date Format
    if(sCreatedAfter!=null && !"".equals(sCreatedAfter)){
         sCreatedAfter = eMatrixDateFormat.getFormattedInputDate( sCreatedAfter,
                                                                 clientTZOffset, localeObj);
    }
    if(sCreatedBefore!=null && !"".equals(sCreatedBefore)){

      sCreatedBefore = eMatrixDateFormat.getFormattedInputDate( sCreatedBefore,
                                                             clientTZOffset, localeObj);

    }
    String selAttrTitle     = "attribute["+DomainObject.ATTRIBUTE_TITLE+"]";
    String selRefObjectId   = "to["+sRelReferenceDoc+"].from.id";
    String selRefObjectName = "to["+sRelReferenceDoc+"].from.name";
    String selRefObjectType = "to["+sRelReferenceDoc+"].from.type";

    //Get the vault list for the query
    StringBuffer employeeRel = new StringBuffer(32);
    employeeRel.append("to[");
    employeeRel.append(DomainConstants.RELATIONSHIP_EMPLOYEE);
    employeeRel.append("].from.id");


    String myCompanyId = Person.getInfo(context, employeeRel.toString());
    company = (Company)DomainObject.newInstance(context,DomainConstants.TYPE_COMPANY,DomainConstants.TEAM);
    company.setId(myCompanyId);


    // Get the list of all vaults, including the Collab partner vaults
    String     companyVaultStr  = company.getAllVaults(context, true);
    StringList companyVaultList = FrameworkUtil.split(companyVaultStr,",");

    String vaultString = "";
    Iterator vaultItr = companyVaultList.iterator();
    StringList vaultNames = new StringList();
    while(vaultItr.hasNext())
    {
      if(vaultString.length() == 0)
      {
        vaultString = vaultString + (String)vaultItr.next();
      }
      else
      {
        String vaultName = (String)vaultItr.next();
        if ((vaultString.length() + vaultName.length()) > 256)
        {
          vaultNames.addElement(vaultString);
          vaultString = vaultName;
        }
        else
        {
          vaultString = vaultString + "," + vaultName;
        }
      }
    }
    vaultNames.addElement(vaultString);


    if(sKeywords == null)
    {
      sKeywords = "";
    }

    // build the where clause start **************
    StringBuffer busWhere = new StringBuffer(1024);

    //get only the latest revisions
    busWhere.append("(revision == last)");

    if (null==sKeywords.trim()           ||
       ("null").equals(sKeywords.trim()) ||
       ("").equals(sKeywords.trim())     ||
       ("*").equals(sKeywords.trim()))
    {
        sKeywords = null;
    }

    boolean nameMatchCase = false;
    if ("True".equals(matchCase))
    {
        nameMatchCase = true;
    }

    if (null==sFileName                  ||
       ("null").equals(sFileName.trim()) ||
       ("").equals(sFileName.trim()))
    {
       sFileName = "*";
    }

    if (sFileName.indexOf("*") == -1)
    {
      if (busWhere.length() > 0)
      {
        busWhere.append(" && ");
      }
      if (nameMatchCase)
      {
        busWhere.append("(name");        
        busWhere.append("== \"");
        busWhere.append(sFileName);
        busWhere.append("\")");
      }
      else
      {
        busWhere.append("(name");       
        busWhere.append(" ~~ \"");
        busWhere.append(sFileName);
        busWhere.append("\")");
      }
    }
    else
    {
      if (!sFileName.trim().equals("*"))
      {
        if (busWhere.length() > 0)
        {
          busWhere.append(" && ");
        }
        if (nameMatchCase)
        {
          busWhere.append("(name");         
          busWhere.append(" ~= \"");
          busWhere.append(sFileName);
          busWhere.append("\")");
        }
        else
        {
          busWhere.append("(name");          
          busWhere.append(" ~~ \"");
          busWhere.append(sFileName);
          busWhere.append("\")");
        }
      }
    }
    if (null==sOwnedFiles           ||
       ("null").equals(sOwnedFiles) ||
       ("").equals(sOwnedFiles))
    {
       sOwnedFiles = "true";
    }

    String owner = "*";
    if (sOwnedFiles.equals("true"))
    {
      owner = context.getUser();
    }

    if (null==sOwner           ||
       ("null").equals(sOwner) ||
       (",").equals(sOwner))
    {
       sOwner = "";
    }

    StringTokenizer st = new StringTokenizer(sOwner,",");
    while (st.hasMoreTokens())
    {
      if (owner.equals("*"))
      {
        owner = "";
        owner = owner + "," + st.nextToken();
      }
      break;
    }

    if (sCreatedBefore == null)
    {
      sCreatedBefore = "";
    }
    if (sCreatedBefore.trim().length() != 0)
    {
      if (busWhere.length() > 0)
      {
        busWhere.append(" && ");
      }
      busWhere.append("(originated < \"");
      busWhere.append(sCreatedBefore);
      busWhere.append("\")");
    }

    if (sCreatedAfter == null)
    {
      sCreatedAfter = "";
    }
    if (sCreatedAfter.trim().length() != 0)
    {
      if (busWhere.length() > 0)
      {
         busWhere.append(" && ");
      }
      busWhere.append("(originated > \"");
      busWhere.append(sCreatedAfter);
      busWhere.append("\")");
    }

    if (sKeywords != null)
    {
      if (busWhere.length() > 0)
      {
        busWhere.append(" && ");
      }
      busWhere.append("(search[");
      busWhere.append(sKeywords);
      busWhere.append("] == TRUE)");
    }

    if (busWhere.length() > 0)
    {
       busWhere.append(" && ");
    }
    busWhere.append("(!(\"to[");
    busWhere.append(sRelVaultedDocuments);
    busWhere.append("].from.id\" !~~ \"zz\"))");
    busWhere.append(" && (!(\"from[");
    busWhere.append(sRelObjectRoute);
    busWhere.append("].to.id\" !~~ \"zz\"))");

    if (busWhere.length() > 0)
    {
      busWhere.append("&& (current.access[read] == true)");
    }
    else
    {
      busWhere.append("(current.access[read] == true)");
    }

    // Add check to eliminate Generic Document 'Versioning' Objects
    busWhere.append(" && (attribute[");
        final String selRouteId = "from["+sRelObjectRoute+"].to.id";
        final String strVersionObjectAttr = PropertyUtil.getSchemaProperty(context,"attribute_IsVersionObject");
    busWhere.append(strVersionObjectAttr);
    busWhere.append("] == False)");

    Pattern typePattern = new Pattern(sTypeDocument);

    StringList objSelects = new StringList(12);
    objSelects.addElement(DomainObject.SELECT_NAME);
    objSelects.addElement(DomainObject.SELECT_ID);
    objSelects.addElement(DomainObject.SELECT_TYPE);
    objSelects.addElement(DomainObject.SELECT_REVISION);
    objSelects.addElement(sbLockedSelect.toString());
    objSelects.addElement(sbSelRelActVerRev.toString());
    objSelects.addElement(DomainObject.SELECT_DESCRIPTION);
    objSelects.addElement(selAttrTitle);
    objSelects.addElement(selRefObjectId);
    objSelects.addElement(selRefObjectName);
    objSelects.addElement(selRefObjectType);
    objSelects.addElement(selRouteId);

    Iterator vaultNamesItr = vaultNames.iterator();
    while(vaultNamesItr.hasNext())
    {
      String vault = (String)vaultNamesItr.next();
      MapList docList = DomainObject.findObjects(context,
                                    typePattern.getPattern(),     //typePattern
                                    "*",                          //namePattern
                                    "*",                          //revPattern
                                    owner,                        //owner pattern
                                    vault,                        //vaultPattern
                                    busWhere.toString(),          //whereExpression,
                                    null,                         //query name
                                    false,                        //expandType,
                                    objSelects,                   //objectSelects,
                                    Short.parseShort(queryLimit), //object limit
                                    null,                         //searchFormat,
                                    null);                        //searchText,

      mapList.addAll(docList);
    }

    // this Maplist is the one which is used to make the table.
    return mapList;
  }

  /**
   * getWorkspaceName - This method is used to show the Workspace Name.
   *                This method is called from the Column Workspace.
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - objectList MapList
   * @return Object of type Vector
   * @throws Exception if the operation fails
   * @since V6R2009x.HF79
   */
  public Vector getWorkspaceName(Context context, String[] args)
    throws Exception
  {
      Vector vecContent = new Vector();

      try
      {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          MapList objList = (MapList)programMap.get("objectList");
          Map  objectMap = null;

          Iterator objectListItr = objList.iterator();

          while (objectListItr.hasNext())
          {

              objectMap = (Map)objectListItr.next();
              String workspaceName=(String)objectMap.get("workspaceNames");
              // In export workspace name was not coming.commented below for that
              //workspaceName=FrameworkUtil.findAndReplace(workspaceName,";","\n\n");
              //workspaceName="\n\n"+workspaceName;
              vecContent.add(workspaceName);

          }

      }
      catch (Exception ex)
      {
          throw ex;
      }
    finally
    {
      return  vecContent;
    }
  }


/**
   * getFileSearchOwnerChooser - Displays the list of persons for the owner chooser for files search
   * @param context the eMatrix <code>Context</code> object
   * @return MapList
   * @throws Exception if the operation fails
   * @since R210
   * @grade 0
   */
   @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getFileSearchOwnerChooser(Context context, String[] args) throws Exception
  {
      HashMap programMap = (HashMap)JPO.unpackArgs(args);

      String sUsername      = (String)programMap.get("User Name");
      String sLastName      = (String)programMap.get("Last Name");
      String sFirstName     = (String)programMap.get("First Name");
      String sCompany       = (String)programMap.get("Organization");
      String objectId       = (String)programMap.get("objectId");

      String strFirstNameAttr       = PropertyUtil.getSchemaProperty(context, "attribute_FirstName");
      String strLastNameAttr        = PropertyUtil.getSchemaProperty(context, "attribute_LastName");
      String sType                  = PropertyUtil.getSchemaProperty(context, "type_Person");

      boolean bLastname =false;
      StringBuffer  sWhereExpression  = new StringBuffer();
      matrix.db.Query qryGeneric    = new matrix.db.Query();
      MapList templateMapList = new MapList();
      DomainObject templateObj= null;

      if(sCompany == null) {
        sCompany = "*";
      }

      if(objectId  == null) {
          objectId = "";
      }

      BusinessObjectList boListGeneric = new BusinessObjectList();

      com.matrixone.apps.common.Person person1 = com.matrixone.apps.common.Person.getPerson(context);
      Company company1 = person1.getCompany(context);


      qryGeneric.create(context);
      //set the Type where clause
      qryGeneric.setBusinessObjectType(sType);
      qryGeneric.setBusinessObjectName(sUsername);
      qryGeneric.setBusinessObjectRevision("*");
      qryGeneric.setOwnerPattern("*");
      qryGeneric.setVaultPattern(company1.getAllVaults(context,true));

      if((sLastName!=null) && !(sLastName.equals("*"))) {

        sWhereExpression.append("(attribute[");
        sWhereExpression.append(strLastNameAttr);
        sWhereExpression.append("] ~~ \"");
        sWhereExpression.append(sLastName);
        sWhereExpression.append("\")");
        bLastname=true;
      }

      if((sFirstName!=null) && !(sFirstName.equals("*"))) {
        if (bLastname){
          sWhereExpression.append("&&");
        }
        sWhereExpression.append("(attribute[");
        sWhereExpression.append(strFirstNameAttr);
        sWhereExpression.append("] ~~ \"");
        sWhereExpression.append(sFirstName);
        sWhereExpression.append("\")");
      }

      String sExp = sWhereExpression.toString();
      qryGeneric.setWhereExpression(sExp);
      try {
        boListGeneric = qryGeneric.evaluate(context);
      } catch(Exception e) {}
      qryGeneric.close(context);
      if(boListGeneric.size()!=0) {


        //get the vaults of the Person's company and its collaboration partners and suppliers
        com.matrixone.apps.common.Person Person = com.matrixone.apps.common.Person.getPerson(context);
        DomainObject domainPerson = new DomainObject(Person);

        String myCompanyId = domainPerson.getInfo(context, "to["+domainPerson.RELATIONSHIP_EMPLOYEE+"].from.id");
        String personVault = Person.getVaultName(context);

        String relCollaborationPartner  = PropertyUtil.getSchemaProperty(context,"relationship_CollaborationPartner");

        DomainObject domainCompany = DomainObject.newInstance(context , myCompanyId);
        StringList strCompNameList = domainCompany.getInfoList(context , "from[" + relCollaborationPartner + "].to.name" );
        if(strCompNameList == null)
        {
          strCompNameList = new StringList();
        }

        strCompNameList.add(domainCompany.getInfo(context, domainCompany.SELECT_NAME));

        Pattern vaultTypePattern = new Pattern(domainCompany.TYPE_ORGANIZATION);
        Pattern vaultRelPattern = new Pattern(relCollaborationPartner);
        vaultRelPattern.addPattern(domainCompany.RELATIONSHIP_SUPPLIER);

        StringList objectSelects = new StringList();
        objectSelects.addElement(domainCompany.SELECT_VAULT);

        MapList vaultMapList = domainCompany.getRelatedObjects(context,
                                              vaultRelPattern.getPattern(), //relationshipPattern
                                              vaultTypePattern.getPattern(),//typePattern
                                              objectSelects,                //objectSelects
                                              null,                         //relationshipSelects
                                              true,                         //getTo
                                              true,                         //getFrom
                                              (short)1,                     //recurseToLevel
                                              null,                         //objectWhere
                                              null);                        //relationshipWhere

        String vaultString = personVault;
        Iterator vaultMapListItr = vaultMapList.iterator();
        StringList vaultList = new StringList();

        while(vaultMapListItr.hasNext()) {
          Map vaultMap = (Map)vaultMapListItr.next();
          String vault = (String)vaultMap.get(domainCompany.SELECT_VAULT);

          if (personVault.equals(vault)){
            continue;
          }
          if(!vaultList.contains(vault))  {
            vaultList.addElement(vault);
            vaultString = vaultString + "," + vault;
          }
        }
        String companySelect = "to["+ templateObj.RELATIONSHIP_EMPLOYEE +"].from.name";
        objectSelects = new StringList();
        objectSelects.add(templateObj.SELECT_TYPE);
        objectSelects.add(templateObj.SELECT_NAME);
        objectSelects.add(templateObj.SELECT_ID);
        //Added Bug no 312500
        objectSelects.add(Person.SELECT_LAST_NAME);
        objectSelects.add(Person.SELECT_FIRST_NAME);
        //Till here
        objectSelects.add(companySelect);

        if(sUsername == null || sUsername.equals("null") || sUsername.equals("")){
          sUsername = templateObj.QUERY_WILDCARD;
        }
          MapList personList = templateObj.querySelect(context,
                                          templateObj.TYPE_PERSON,    // type pattern
                                          sUsername,                  // namePattern
                                          templateObj.QUERY_WILDCARD, // revPattern
                                          templateObj.QUERY_WILDCARD, // ownerPattern
                                          vaultString,                // get the Person Company vault
                                          sExp,                       // where expression
                                          true,                       // expandType
                                          objectSelects,              // object selects
                                          null,                       // cached list
                                          true);                      // use cache

          Iterator personListItr = personList.iterator();
          while(personListItr.hasNext()) {
            Map personMap = (Map) personListItr.next();
            String personType = (String)personMap.get(templateObj.SELECT_TYPE);
            String personName = (String)personMap.get(templateObj.SELECT_NAME);
            String personID = (String)personMap.get(templateObj.SELECT_ID);
            String personCompany = (String)personMap.get(companySelect);
            //Added Bug no 312500
            String lastName=(String)personMap.get(Person.SELECT_LAST_NAME);
            String firstName=(String)personMap.get(Person.SELECT_FIRST_NAME);
            //Till here
            if((personType != null) && (personName != null) && (personCompany != null)) {
            if(strCompNameList.contains(personCompany))
              {
              Hashtable hashTableFinal = new Hashtable();
              hashTableFinal.put(templateObj.SELECT_NAME, personName);
              hashTableFinal.put(templateObj.SELECT_TYPE, personType);
              hashTableFinal.put(templateObj.SELECT_ID, personID);
              hashTableFinal.put(templateObj.TYPE_ORGANIZATION, personCompany);
              //Added Bug no 312500
              hashTableFinal.put(Person.SELECT_LAST_NAME, lastName);
              hashTableFinal.put(Person.SELECT_FIRST_NAME, firstName);
              //Till here
              templateMapList.add(hashTableFinal);
              }
            }
          }

      }
      return templateMapList;
  }


}
