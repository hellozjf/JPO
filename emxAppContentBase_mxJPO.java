//
//
import matrix.db.*;
import matrix.util.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.*;
import com.matrixone.apps.common.util.*;
import com.matrixone.apps.common.*;


public class emxAppContentBase_mxJPO extends emxDomainObject_mxJPO {

  /**
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since AEF 9.5.1.1
   * @grade 0
   */
  public emxAppContentBase_mxJPO(Context context, String[] args) throws Exception {
    super(context, args);
  }


  /**
   * This method is executed if a specific method is not specified.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns nothing
   * @throws Exception if the operation fails
   * @since AEF 9.5.1.1
   */
  public int mxMain(Context context, String[] args)
  throws Exception
  {
    if (!context.isConnected())
       throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Generic.NotSupportedOnDesktopClient", context.getLocale().getLanguage()));
    return 0;
  }





/**
  * Get  for the specified criteria
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds input arguments.
  * @return Vector containing search result.
  * @exception Exception if the operation fails.
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public Object getSearchResult(Context context , String[] args)
                    throws Exception
  {

    HashMap paramMap = (HashMap)JPO.unpackArgs(args);

    String scopeId = (String)paramMap.get("scopeId");
 
    String slkupOriginator    = PropertyUtil.getSchemaProperty(context,"attribute_Originator");

    //Retrieve Search criteria
    String selType          = (String)paramMap.get("selType");
    String txtName          = (String)paramMap.get("txtName");
    String txtRev           = (String)paramMap.get("txtRev");
    String txtOwner         = (String)paramMap.get("txtOwner");
    String txtWhere         = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL((String)paramMap.get("txtWhere"));
    String txtOriginator    = (String)paramMap.get("txtOriginator");
    String txtDescription   = (String)paramMap.get("txtDesc");
    String txtSearch        = (String)paramMap.get("txtSearch");
    String txtFormat        = (String)paramMap.get("txtFormat");
    String languageStr      = (String)paramMap.get("languageStr");
    String sSetName         = (String)paramMap.get("setRadio");
		String caseSensitiveSearch = (String)paramMap.get("caseSensitiveSearch");
    String sWhereExp = txtWhere;

    String target       = "";
    String sAnd         = "&&";
    String sOr          = "||";
    char chDblQuotes    = '\"';

/**************************Vault Code Start*****************************/
// Get the user's vault option & call corresponding methods to get the vault's.

      String txtVault   ="";
      String strVaults="";
      StringList strListVaults=new StringList();

      String txtVaultOption = (String)paramMap.get("vaultOption");
      if(txtVaultOption==null) {
    	    String vaultDefaultSelection = PersonUtil.getSearchDefaultVaults(context);
    	    if(vaultDefaultSelection == null || "".equals(vaultDefaultSelection)) {
    	       vaultDefaultSelection = EnoviaResourceBundle.getProperty(context, "emxFramework.DefaultSearchVaults");
    	    }
    	    txtVault=vaultDefaultSelection;
      }


      //trimming
      txtVault = txtVault.trim();

  /*******************************Vault Code End***************************************/

    if (sSetName == null || sSetName.equals("null") || sSetName.equals("")){
      sSetName = "";
    }

    String savedQueryName   = (String)paramMap.get("savedQueryName");
    if (savedQueryName == null || savedQueryName.equals("null") || savedQueryName.equals("")){
      savedQueryName="";
    }

    String queryLimit = (String)paramMap.get("queryLimit");
    if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")){
      queryLimit = "0";
    }

    if (txtName == null || txtName.equalsIgnoreCase("null") || txtName.length() <= 0){
      txtName = "*";
    }
    if (txtRev == null || txtRev.equalsIgnoreCase("null") || txtRev.length() <= 0){
      txtRev = "*";
    }
    if (txtOwner == null || txtOwner.equalsIgnoreCase("null") || txtOwner.length() <= 0){
      txtOwner = "*";
    }
    if (txtDescription != null && !txtDescription.equalsIgnoreCase("null") && txtDescription.equals("*")){
      txtDescription = "";
    }
    if (txtOriginator != null && !txtOriginator.equalsIgnoreCase("null") && txtOriginator.equals("*")){
      txtOriginator = "";
    }
    if (txtWhere == null || txtWhere.equalsIgnoreCase("null")){
      txtWhere = "";
    }

    if (!(txtOriginator == null || txtOriginator.equalsIgnoreCase("null") || txtOriginator.length() <= 0 )) {
      String sOriginatorQuery = "attribute[" + slkupOriginator + "] ~~ " + chDblQuotes + txtOriginator + chDblQuotes;
      if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
        sWhereExp = sOriginatorQuery;
      } else {
        sWhereExp += sAnd + " " + sOriginatorQuery;
      }
    }

    if (!(txtDescription == null || txtDescription.equalsIgnoreCase("null") || txtDescription.length() <= 0 )) {
      String sDescQuery = "description ~~ " + chDblQuotes + txtDescription + chDblQuotes;
      if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 ){
        sWhereExp = sDescQuery;
      } else {
        sWhereExp += sAnd + " " + sDescQuery;
      }
    }

    if (selType.equals(DomainObject.TYPE_DOCUMENT))
    {
      String strVersionObjectAttr = PropertyUtil.getSchemaProperty(context,"attribute_IsVersionObject");

      if ((sWhereExp == null) ||
          (sWhereExp.equalsIgnoreCase("null")) ||
          (sWhereExp.length()<=0 ))
      {
        sWhereExp = "(attribute[" + strVersionObjectAttr + "] == False)";
      }
      else
      {
        sWhereExp += sAnd + " " + "(attribute[" + strVersionObjectAttr + "] == False)";
      }
    }
// This code need to be taken out once Sourcing X+3 Migration is Completed     -SC
// Start of Pre Migration Code -SC
    else if(selType.equals(DomainObject.TYPE_REQUEST_TO_SUPPLIER)){
		if ((sWhereExp == null) || (sWhereExp.equalsIgnoreCase("null")) || (sWhereExp.length()<=0 ))
		{
	         sWhereExp = " (!to[" + DomainObject.RELATIONSHIP_COMPANY_RFQ + "]) ";
		}
		else
		{
	         sWhereExp += sAnd + " " + " (!to[" + DomainObject.RELATIONSHIP_COMPANY_RFQ + "]) ";
		}
	} else if(selType.equals(DomainObject.TYPE_PACKAGE)){
		if ((sWhereExp == null) || (sWhereExp.equalsIgnoreCase("null")) || (sWhereExp.length()<=0 ))
		{
			sWhereExp = " (!to[" + DomainObject.RELATIONSHIP_COMPANY_PACKAGE + "]) ";
		}
		else
		{
			sWhereExp += sAnd + " " + " (!to[" + DomainObject.RELATIONSHIP_COMPANY_PACKAGE + "]) ";
		}
	} else if(selType.equals(DomainObject.TYPE_RTS_QUOTATION)){
		if ((sWhereExp == null) || (sWhereExp.equalsIgnoreCase("null")) || (sWhereExp.length()<=0 ))
		{
			sWhereExp = " (to[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].from.to["+ DomainObject.RELATIONSHIP_COMPANY_RFQ +"]!='#DENIED!') && (!(to[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].from.to["+ DomainObject.RELATIONSHIP_COMPANY_RFQ +"]))";
		}
		else
		{
			sWhereExp += sAnd + " " + "(to[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].from.to["+ DomainObject.RELATIONSHIP_COMPANY_RFQ +"]!='#DENIED!') && (!(to[" + DomainObject.RELATIONSHIP_RTS_QUOTATION + "].from.to["+ DomainObject.RELATIONSHIP_COMPANY_RFQ +"]))";
		}
	}
// End of Pre Migration Code - SC
    StringList resultSelects = new StringList(7);
    resultSelects.add(DomainObject.SELECT_ID);
    resultSelects.add(DomainObject.SELECT_TYPE);
    resultSelects.add(DomainObject.SELECT_NAME);
    resultSelects.add(DomainObject.SELECT_REVISION);
    resultSelects.add(DomainObject.SELECT_DESCRIPTION);
    resultSelects.add(DomainObject.SELECT_CURRENT);
    resultSelects.add(DomainObject.SELECT_POLICY);

    String sObjWhere = "current.access[read] == TRUE && revision ~~ last";

    MapList totalresultList = null;

    // Check for a set name and use that set for results if present
    if (sSetName.equals(""))
    {
      if (savedQueryName.equals(""))
      {
    if((scopeId != null) && !(scopeId.equals("")) &&  !(scopeId.equals("Organization")) )
    {
          DomainObject domainObject  = DomainObject.newInstance(context,scopeId);
      String sTypeName           = domainObject.getType(context);
      boolean bIsTypeWorkspaceVault = sTypeName.equals(DomainObject.TYPE_WORKSPACE_VAULT);
      boolean bIsTypeWorkspace      = sTypeName.equals(DomainObject.TYPE_WORKSPACE);
      boolean bIsTypeProjectspace   = sTypeName.equals(DomainObject.TYPE_PROJECT_SPACE);
      String sWorkspaceFolderId = null;

          if(bIsTypeWorkspace == true || bIsTypeWorkspaceVault == true || bIsTypeProjectspace == true)
          {
        if(bIsTypeWorkspace == true || bIsTypeProjectspace == true)
        {
        StringList folderList = domainObject.getInfoList(context, "from["+DomainObject.RELATIONSHIP_WORKSPACE_VAULTS+"].to.id");
        Iterator folderItr = folderList.iterator();
        if(folderItr.hasNext()){
	        sWorkspaceFolderId = folderItr.next()+",";
		}
        while(folderItr.hasNext())
        {
          sWorkspaceFolderId = sWorkspaceFolderId + folderItr.next() + ",";
        }
            }
            else
            {
        sWorkspaceFolderId = scopeId+",";
        }
      }
      else
      {
        //for content inside the folder
           String folderId = domainObject.getInfo(context, "to["+DomainObject.RELATIONSHIP_VAULTED_OBJECTS+"].from.id");
           if(folderId != null)
           {
             sWorkspaceFolderId = folderId+",";
           }
      }
       totalresultList = new MapList();
       Pattern relPattern  = new Pattern(DomainObject.RELATIONSHIP_VAULTED_DOCUMENTS);
       relPattern.addPattern(DomainObject.RELATIONSHIP_SUB_VAULTS);
       //if PMC is installed
       String strType = domainObject.getType(context);
       if(strType.equals(DomainObject.TYPE_PROJECT_SPACE) ||
       		strType.equals(DomainObject.TYPE_WORKSPACE_VAULT) ||
       			strType.equals(DomainObject.TYPE_WORKSPACE)){
           relPattern.addPattern(DomainObject.RELATIONSHIP_VAULTED_OBJECTS_REV2);
	   }
       Pattern typePattern = new Pattern(selType);
       typePattern.addPattern(DomainObject.TYPE_WORKSPACE_VAULT);
       Pattern filTypePattern = new Pattern(selType);
       if(txtName != null && !txtName.equals("*") && sObjWhere.length() >0)
         sObjWhere = sObjWhere+" "+sAnd+" name ~~ \""+txtName+"\"";
       else if(txtName != null && !txtName.equals("*") && sObjWhere.length() <= 0)
         sObjWhere = "name ~~ "+txtName;
       if(sWorkspaceFolderId != null)
       {
           StringTokenizer folderListSt  = new StringTokenizer(sWorkspaceFolderId,",");
            //iterate thro. the folders
            while (folderListSt.hasMoreTokens())
               {
              String folderId = folderListSt.nextToken();
               domainObject.setId(folderId);
              MapList docMapList = domainObject.getRelatedObjects(context,
                                 relPattern.getPattern(),
                                 typePattern.getPattern(),//"*",//patternType.getPattern(),
                                 resultSelects,
                                 null,
                                 false,
                                 true,
                                 (short)0,
                                 sObjWhere,
                                 null,
                                 filTypePattern,
                                 null,
                                 null);
           totalresultList.addAll(docMapList);
        }
      }
      else
      {
          MapList docMapList = domainObject.getRelatedObjects(context,
                                       "*",//relPattern.getPattern(),
                                       typePattern.getPattern(),//"*",//patternType.getPattern(),
                                       resultSelects,
                                       null,
                                       false,
                                       true,
                                       (short)0,
                                       sObjWhere,
                                       null,
                                       filTypePattern,
                                       null,
                                       null);
           totalresultList.addAll(docMapList);
      }

    }// eof Object Id
    else if( ("Organization").equals(scopeId))
    {
       com.matrixone.apps.common.Person person=  com.matrixone.apps.common.Person.getPerson(context);
       Company company = person.getCompany(context);
       String companyVault=company.getVault();
       String SecondaryVaults = company.getSecondaryVaults(context);
       String Vaults = companyVault;
       if(SecondaryVaults != null)
         Vaults = Vaults+","+SecondaryVaults;
       totalresultList = DomainObject.findObjects(context,
                                   selType,
                                   txtName,
                                   txtRev,
                                   txtOwner,
                                   Vaults,
                                   sWhereExp,
                                   null,
                                   true,
                                   resultSelects,
                                   Short.parseShort(queryLimit),
                                   txtFormat,
                                   txtSearch);
				}else{ 
					if(!txtName.equals("*")){
						if("true".equals(caseSensitiveSearch)){
							sWhereExp+=" "+sAnd+" name match \""+txtName+"\"";
						}else{
							sWhereExp+=" "+sAnd+" name smatch \""+txtName+"\"";
						}
         }
                totalresultList = DomainObject.findObjects(context,
                                   selType,
							"*",
                                   txtRev,
                                   txtOwner,
                                   txtVault,
                                   sWhereExp,
                                   null,
                                   true,
                                   resultSelects,
                                   Short.parseShort(queryLimit),
                                   txtFormat,
                                   txtSearch);
         }
      }
      else
      {
    matrix.db.Query query = new matrix.db.Query(savedQueryName);
	try{
	    ContextUtil.startTransaction(context,false);
        query.open(context);
        query.setObjectLimit(Short.parseShort(queryLimit));
        totalresultList = FrameworkUtil.toMapList(query.getIterator(context,resultSelects,(short)1000));
        query.close(context);
        ContextUtil.commitTransaction(context);
	}
	catch(Exception ex)
	{
		ContextUtil.abortTransaction(context);
		throw new Exception(ex.toString());
	}
      }
    }
    else
    {
        totalresultList = SetUtil.getMembers(context,
                                             sSetName,
                                             resultSelects);
    }

    return totalresultList;
  }
}
