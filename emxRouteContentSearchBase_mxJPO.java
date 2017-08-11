/*
 *  emxRouteContentSearchBase.java  (emxRouteContentSearchBase JPO class)
 *
 * Copyright (c) 1992-2016 Dassault Systemes.
 *
 * All Rights Reserved.
 * This program contains proprietary and trade secret information of
 * MatrixOne, Inc.  Copyright notice is precautionary only and does
 * not evidence any actual or intended publication of such program.
 *
 */

import java.util.*;

import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.servlet.Framework;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.CommonDocument;

public class emxRouteContentSearchBase_mxJPO
{

  /**
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @throws Exception if the operation fails
   * @since AEF 10.5 next
   * @grade 0
   */
    public emxRouteContentSearchBase_mxJPO ()  throws Exception
    {
    }

  /**
   * This method returns Content Search Results Found as a MapList object
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds no arguments
   * @returns MapList
   * @throws Exception if the operation fails
   * @since AEF 10.5 next
   */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getContents(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String languageStr = (String)programMap.get("languageStr");

        // Get search parameters
        String selType = (String)programMap.get("selType");
        String txtName = (String)programMap.get("txtName");
        String txtRev = (String)programMap.get("txtRev");
        String txtDescription = (String)programMap.get("txtDesc");
        String strObjectId = (String)programMap.get("objectId");
        DomainObject dombj = new  DomainObject(strObjectId);
        String RELATIONSHIP_OBJECT_ROUTE = PropertyUtil.getSchemaProperty(context, "relationship_ObjectRoute");
        String RELATIONSHIP_ROUTE_TASK = PropertyUtil.getSchemaProperty(context, "relationship_RouteTask");
        StringList sList = new StringList();
        sList.addElement(DomainConstants.SELECT_TYPE);
        sList.addElement("relationship["+RELATIONSHIP_ROUTE_TASK+"].to.id");
        sList.addElement("relationship["+RELATIONSHIP_OBJECT_ROUTE+"].from.id");
        sList.addElement("relationship["+RELATIONSHIP_OBJECT_ROUTE+"].from.type.kindof["+DomainConstants.TYPE_TASK+"]");
        
        Map mp =dombj.getInfo(context,sList);
        String stype =(String)mp.get(DomainConstants.SELECT_TYPE);
        if(stype.equals(DomainConstants.TYPE_INBOX_TASK))
        {
        	strObjectId= (String)mp.get("relationship["+RELATIONSHIP_ROUTE_TASK+"].to.id");
        }
        //below code is added to show task deliverables in content search result, of the route under the same task.
        boolean hasTask = false;
        String isKindOfTask = (String)mp.get("relationship["+RELATIONSHIP_OBJECT_ROUTE+"].from.type.kindof["+DomainConstants.TYPE_TASK+"]");
        String taskId = DomainConstants.EMPTY_STRING;
        if(UIUtil.isNotNullAndNotEmpty(isKindOfTask)){
        	taskId = (String)mp.get("relationship["+RELATIONSHIP_OBJECT_ROUTE+"].from.id");
        	if("True".equalsIgnoreCase(isKindOfTask) && UIUtil.isNotNullAndNotEmpty(taskId)){
        		hasTask =  true;
        	}
        }   
        //END
        String queryLimit=(String)programMap.get("queryLimit");
        
        if (queryLimit == null || queryLimit.equals("null") || queryLimit.equals("")){
            queryLimit = "100";
          }
        
        SelectList resultSelects = new SelectList(6);
        resultSelects.add(DomainObject.SELECT_ID);
        resultSelects.add(DomainObject.SELECT_TYPE);
        resultSelects.add(DomainObject.SELECT_NAME);
        resultSelects.add(DomainObject.SELECT_REVISION);
        resultSelects.add(DomainObject.SELECT_DESCRIPTION);
        resultSelects.add(DomainObject.SELECT_POLICY);
        resultSelects.add(DomainObject.SELECT_CURRENT);

        Route routeObject = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
        routeObject.setId(strObjectId);

        // String restrictMembers = routeObject.getInfo(context,DomainConstants.ATTRIBUTE_RESTRICT_MEMBERS);
        // Causing null pointer exception if DomainConstants.ATTRIBUTE_RESTRICT_MEMBERS is used
        final String ATTRIBUTE_RESTRICT_MEMBERS = PropertyUtil.getSchemaProperty(context, "attribute_RestrictMembers");
        final String SELECT_ATTRIBUTE_RESTRICT_MEMBERS = "attribute[" + ATTRIBUTE_RESTRICT_MEMBERS + "]";
        
        String restrictMembers = routeObject.getInfo(context, SELECT_ATTRIBUTE_RESTRICT_MEMBERS);

        String sWhereExp = "";
        String sAnd = "&&";
        String sOr = "||";
        char chDblQuotes = '\"';
        if (txtName==null || txtName.equalsIgnoreCase("null") || txtName.length()<=0) {
            txtName = "*";
        }
        if (txtRev==null || txtRev.equalsIgnoreCase("null") || txtRev.length()<=0)  {
            txtRev = "*";
        }
        if (txtDescription!=null && !txtDescription.equalsIgnoreCase("null") && txtDescription.equals("*"))  {
            txtDescription = "";
        }
        if (!(txtDescription == null || txtDescription.equalsIgnoreCase("null") || txtDescription.length() <= 0 ))  {
            String sDescQuery = "description ~~ " + chDblQuotes + txtDescription + chDblQuotes;
            if (sWhereExp == null || sWhereExp.equalsIgnoreCase("null") || sWhereExp.length()<=0 )  {
                sWhereExp = sDescQuery;
            }
            else  {
                sWhereExp += sAnd + " " + sDescQuery;
            }
        }
        //Added for bug 348796
        
         String strParentType = CommonDocument.getParentType(context,selType);
      
         //strParentType.equals("DOCUMENTS")
         final String TYPE_DOCUMENTS = PropertyUtil.getSchemaProperty (context, "type_DOCUMENTS");

        if (DomainObject.TYPE_DOCUMENT.equals(selType) || TYPE_DOCUMENTS.equals(strParentType))  {
            String strVersionObjectAttr = PropertyUtil.getSchemaProperty(context,"attribute_IsVersionObject");
            if ((sWhereExp == null) || (sWhereExp.equalsIgnoreCase("null")) || (sWhereExp.length()<=0 ))  {
                sWhereExp = "(attribute[" + strVersionObjectAttr + "] == False)";
            }
            else  {
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

        MapList searchResults = null;
        if("Organization".equals(restrictMembers))  {
            com.matrixone.apps.common.Person person=  com.matrixone.apps.common.Person.getPerson(context);
            Company company = person.getCompany(context);
            String companyVault=company.getVault();
            String SecondaryVaults = company.getSecondaryVaults(context);
            String Vaults = companyVault;
            if(SecondaryVaults != null)  {
                Vaults = Vaults+","+SecondaryVaults;
            }
            
            //Commented for Bug 368001,to include queryLimit parameter.findObjects method used instead of deprecated querySelect method
            
            //searchResults =DomainObject.querySelect(context,selType,txtName,txtRev,"*",Vaults,sWhereExp,true,resultSelects,null,false);
            
            //Added for bug 368001
            searchResults=DomainObject.findObjects(
                    context,        // eMatrix context
                    selType,        // type pattern
                    txtName,        // name pattern
                    txtRev,         // revision pattern
                    "*",            // owner pattern
                    Vaults,   // vault pattern
                    sWhereExp,       // where expression
                    null,           // queryName
                    true,           // expand type
                    resultSelects,     // object selects
                    Short.parseShort(queryLimit) // object Limit
                    );
            //End for bug 368001
        }
        else if("All".equals(restrictMembers))  {
            com.matrixone.apps.common.Person person1 = com.matrixone.apps.common.Person.getPerson(context);
            Company company1 = person1.getCompany(context);
                    
//          Commented for Bug 368001,to include queryLimit parameter.findObjects method used instead of deprecated querySelect method      
        //   searchResults = DomainObject.querySelect(context,selType,txtName,txtRev,"*",company1.getAllVaults(context,true),sWhereExp,true,resultSelects,null,false);
            
            //Added for bug 368001
            searchResults=DomainObject.findObjects(
                    context,        // eMatrix context
                    selType,        // type pattern
                    txtName,        // name pattern
                    txtRev,         // revision pattern
                    "*",            // owner pattern
                    company1.getAllVaults(context,true),   // vault pattern
                    sWhereExp,       // where expression
                    null,           // queryName
                    true,           // expand type
                    resultSelects,     // object selects
                    Short.parseShort(queryLimit) // object Limit
                    );
            //End for bug 368001
        }
        else  {
            DomainObject domainObject = DomainObject.newInstance(context,restrictMembers);
            String sTypeName = domainObject.getInfo(context, DomainObject.SELECT_TYPE);
            boolean bIsTypeWorkspaceVault = sTypeName.equals(DomainObject.TYPE_WORKSPACE_VAULT);
            boolean bIsTypeWorkspace = sTypeName.equals(DomainObject.TYPE_WORKSPACE);
            
            //Modified for Subtype
            boolean bIsTypeProjectspace = (sTypeName.equals(DomainObject.TYPE_PROJECT_SPACE) || mxType.isOfParentType(context,sTypeName,DomainConstants.TYPE_PROJECT_SPACE));
            String sWorkspaceFolderId ="";

            if(bIsTypeWorkspace == true || bIsTypeWorkspaceVault == true || bIsTypeProjectspace == true)  {
                if(bIsTypeWorkspace == true || bIsTypeProjectspace == true)  {
                    StringList objectSelects = new StringList(DomainObject.SELECT_ID);
                    // Get all the folders under Project Space/Workspace recursively
                    MapList mlFolderList = domainObject.getRelatedObjects(context, 
                                                                          DomainObject.RELATIONSHIP_WORKSPACE_VAULTS + "," + DomainObject.RELATIONSHIP_SUB_VAULTS, 
                                                                          DomainObject.TYPE_WORKSPACE_VAULT, 
                                                                          objectSelects, 
                                                                          null, 
                                                                          false, 
                                                                          true, 
                                                                          (short)0, 
                                                                          null, 
                                                                          null);

                    Map mapFolderInfo = null;
                    StringBuffer sbuffer = new StringBuffer(64);
                    for (Iterator itrFolderList = mlFolderList.iterator(); itrFolderList.hasNext(); ) {
                        mapFolderInfo = (Map)itrFolderList.next();
                        
                        if (sbuffer.length() > 0) {
                            sbuffer.append(",");
                        }
                        sbuffer.append((String)mapFolderInfo.get(DomainObject.SELECT_ID));
                    }
                    sWorkspaceFolderId = sbuffer.toString();
                    if(hasTask){
                    	sWorkspaceFolderId= sWorkspaceFolderId+","+taskId;
                    }
                    sbuffer = null;
                }
                else  {
                    sWorkspaceFolderId = restrictMembers+",";
                }
            }
            else  {
                //for content inside the folder
                String folderId = domainObject.getInfo(context, "to["+DomainObject.RELATIONSHIP_VAULTED_OBJECTS+"].from.id");
                if(folderId != null)  {
                    sWorkspaceFolderId = folderId+",";
                }
            }
            
            Pattern relPattern  = new Pattern(DomainObject.RELATIONSHIP_VAULTED_DOCUMENTS);
            //if PMC is installed
            relPattern.addPattern(DomainObject.RELATIONSHIP_VAULTED_OBJECTS_REV2);
            if(hasTask){
            	relPattern.addPattern(DomainObject.RELATIONSHIP_TASK_DELIVERABLE);
            }

            Pattern typePattern = new Pattern(selType);
            
            Pattern filTypePattern = new Pattern(selType);
            
//          Append name condition
            if (!"*".equals(txtName)) {
                if (sWhereExp != null) {
                    sWhereExp += " && ";
                }
                sWhereExp += "(name ~~ " + chDblQuotes + txtName + chDblQuotes + ")";
            }
//          Append revision condition
            if (!"*".equals(txtRev)) {
                if (sWhereExp != null) {
                    sWhereExp += " && ";
                }
                sWhereExp += "(revision ~~ " + chDblQuotes + txtRev + chDblQuotes + ")";
            }
            
            //this is required to have the Unique Related Objects...
            HashSet hsUniqueObjects = new HashSet();
            
            if( (sWorkspaceFolderId != null) && (!sWorkspaceFolderId.equals("")) )  {
                
                StringTokenizer folderListSt  = new StringTokenizer(sWorkspaceFolderId,",");
                searchResults = new MapList();
                MapList searchResultsTemp=new MapList();
                //iterate thro. the folders and find their documents
                while (folderListSt.hasMoreTokens())  {
                    String folderId = folderListSt.nextToken();
                    domainObject.setId(folderId);
                    MapList searchResultsForFolder = domainObject.getRelatedObjects(context,
                                                                                    relPattern.getPattern(),
                                                                                    typePattern.getPattern(),
                                                                                    resultSelects,
                                                                                    null,
                                                                                    false,
                                                                                    true,
                                                                                    (short)0,
                                                                                    sWhereExp,
                                                                                    null,
                                                                                    null,
                                                                                    null,
                                                                                    null);
                    
                    
                    for(int i=0; i<searchResultsForFolder.size(); i++)
                    {
                        if(hsUniqueObjects.add((String) ((Map)searchResultsForFolder.get(i)).get(DomainObject.SELECT_ID)))
                        {
                            searchResultsTemp.add(searchResultsForFolder.get(i));
                        }
                    }
                }
                
                if(searchResultsTemp.size()>Integer.parseInt(queryLimit)){
                    for(int i=0; i<Integer.parseInt(queryLimit);i++){
                        searchResults.add(searchResultsTemp.get(i));
                    }
                }
                
                else{
                    for(int i=0; i<searchResultsTemp.size();i++){
                        searchResults.add(searchResultsTemp.get(i));
                    } 
                }
                
                if(searchResults == null)
                {
                    searchResults = new MapList(1);
                }

            }
            else  {
                searchResults = domainObject.getRelatedObjects(context,
                                                                "*",
                                                                typePattern.getPattern(),
                                                                resultSelects,
                                                                null,
                                                                false,
                                                                true,
                                                                (short)0,
                                                                sWhereExp,
                                                                null,
                                                                Integer.parseInt(queryLimit),
                                                                filTypePattern,
                                                                null,
                                                                null);
            }
        }
        return searchResults ;
    }
    
    
    /**
     * This method determines whether the checkbox should be enabled or disabled
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @returns MapList
     * @throws Exception if the operation fails
     * @since AEF 10.5 next
     */
    
    public Vector enableCheckbox(Context context, String args[]) throws Exception
    {
        Vector vec=new Vector();
        StringList sRouteContentId = new StringList();
        StringList sFolderContentId = new StringList();
        HashMap programMap  = (HashMap)JPO.unpackArgs(args);
        // getting the HashMap for the key "paramList"
        HashMap paramMap    = (HashMap)programMap.get("paramList");
        // getting the MapList of the objects.
        MapList objectList     = (MapList)programMap.get("objectList");
       
        String strID = (String)paramMap.get("sContentId");
        String objectId = (String)paramMap.get("objectId");
        String fromType=(String)paramMap.get("fromType");
        
        boolean bIsTypeWorkspaceVault    = false;
                
        if (objectId != null && !"".equals(objectId) && !"null".equals(objectId) ){
            DomainObject domainObject        = DomainObject.newInstance(context,objectId);
            String sTypeName                 = domainObject.getInfo(context, "type");
            bIsTypeWorkspaceVault = sTypeName.equals(DomainObject.TYPE_WORKSPACE_VAULT);
        }
        
         if(strID!=null && !"".equals(strID) && !"null".equals(strID) ){
        
            StringTokenizer stknContentId     = new StringTokenizer(strID, "~");
            while(stknContentId.hasMoreTokens())
            {
                sRouteContentId.add(stknContentId.nextToken());
            }
        }
         
                
        // If navigation is from the context of a Route
         
         if ((fromType != null && !"".equals(fromType) && !"null".equals(fromType) && fromType.equals("Route")) ){
             
             for(int i=0; i< objectList.size(); i++)
             {
                
                Map routeContentMap  = (Map) objectList.get(i);
                objectId    = (String)routeContentMap.get(DomainConstants.SELECT_ID);
              
                 
                 if(sRouteContentId.size()>0 && sRouteContentId.contains(objectId)) 
                 {
                     vec.add("false");
                 }
                 
                 else
                 {
                     vec.add("true");
                 }
                 
            } 
         }
        
      // Else If navigation is from the context of a Workspace
         
       else{
        
        if(objectId != null && !"".equals(objectId) &&  !"null".equals(objectId) && bIsTypeWorkspaceVault) 
        {
            DomainObject templateObj=DomainObject.newInstance(context,objectId);
            if(templateObj.getType(context).equals(templateObj.TYPE_WORKSPACE_VAULT))
            {
                sFolderContentId = getAllWorkspaceContents(context,objectId );
            }
        }
        
        
        for(int j = 0; j<objectList.size() ; j++)
        {
            // getting the objectId from the Map
            Map folderContentMap = (Map) objectList.get(j);
            objectId = (String) folderContentMap.get(DomainConstants.SELECT_ID);
            
            if(sFolderContentId.size()>0 && sFolderContentId.contains(objectId))
            {                
                    vec.add("false");
               
            }    
                        
            else
            {
                vec.add("true");
            }
                             
         }
      }
        return vec;
    }

    /**
    * This method determines checkbox is enabled or disabled
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @returns MapList
    * @throws Exception if the operation fails
    * @since AEF 10.5 next
    */
    public Vector showCheckbox(Context context, String args[]) throws Exception
    {

        HashMap map ;
        Vector routableObjVector = null;
        java.util.HashMap programMap=(java.util.HashMap)JPO.unpackArgs(args);
        map = (HashMap)programMap.get("paramList");
        String strObjectId = (String)map.get("objectId");
        Route routeObject = (Route)DomainObject.newInstance(context,DomainConstants.TYPE_ROUTE);
        routeObject.setId(strObjectId);

        // build select params
        StringList selListObj = new SelectList();
        selListObj.add(routeObject.SELECT_ID);
        MapList routableObjsList = routeObject.getConnectedObjects(context, selListObj, null, false);
        if(routableObjsList.size() != 0) {
            routableObjVector = new Vector();
            Map routableMap = null;
            String routableObjectId = null;
            for(Iterator routableObjsItr = routableObjsList.iterator(); routableObjsItr.hasNext();)  {
                routableMap = (Map)routableObjsItr.next();
                routableObjectId = (String)routableMap.get(routeObject.SELECT_ID);
                routableObjVector.add(routableObjectId);
            } // end of for(Iterator..
        } // end of if(routableObjsList.size()

        Vector checkboxVector=new Vector();
        MapList objectList=(MapList)programMap.get("objectList");
        String objectId = null;
        ListIterator templateIterator=objectList.listIterator();
        for (; templateIterator.hasNext() ; )  {
// Bug 305934 - Added code for Classcast exception.
                Object obj = templateIterator.next();
                if (obj instanceof HashMap) {
                    objectId = (String)((HashMap)obj).get(DomainConstants.SELECT_ID);

                }
                else if (obj instanceof Hashtable)
                {
                    objectId = (String)((Hashtable)obj).get(DomainConstants.SELECT_ID);

                }
           // map=(HashMap)templateIterator.next();
           // objectId=(String)map.get("id");
// till here
            checkboxVector.add(""+ (!(routableObjVector != null && routableObjVector.contains(objectId)) ));
        }
        return checkboxVector ;
    }

    /**
    * This method shows state
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @returns MapList
    * @throws Exception if the operation fails
    * @since AEF 10.5 next
    */
    public Vector showState(Context context, String args[]) throws Exception
    {

        Vector statesVector=new Vector();
        java.util.HashMap programMap=(java.util.HashMap)JPO.unpackArgs(args);
        MapList objectList=(MapList)programMap.get("objectList");
        HashMap map ;
// Bug 305934 - Added code to nullify the object
        Object cur =null ;
        Object strID = null;
        ListIterator templateIterator=objectList.listIterator();
        for (; templateIterator.hasNext() ; )  {
// Bug 305934 - Added code for Classcast exception.
               Object obj = templateIterator.next();
                if (obj instanceof HashMap) {
                    strID = ((HashMap)obj).get(DomainObject.SELECT_ID);
                    cur = ((HashMap)obj).get("current");

                }
                else if (obj instanceof Hashtable)
                {
                    strID = ((Hashtable)obj).get(DomainObject.SELECT_ID);
                    cur = ((Hashtable)obj).get("current");

                }

                if (strID !=null && !"".equals((String)strID)){
                    DomainObject dmObject = new DomainObject((String)strID);
                    cur= i18nNow.getStateI18NString(dmObject.getPolicy(context).getName(), (String)cur, context.getSession().getLanguage());
                }
           // map=(HashMap)templateIterator.next();
          // cur=map.get("current");
// till here
            if ( cur!=null && !((String)cur).equals("") )  {
                statesVector.add( (String)cur );
            }
            else  {
                statesVector.add("&nbsp;");
            }
        }
        return statesVector ;
    }

    /**
    * This method shows revisions
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @returns MapList
    * @throws Exception if the operation fails
    * @since AEF 10.5 next
    */
    public Vector showRevision(Context context, String args[]) throws Exception
    {

        Vector RevisionVector=new Vector();
        java.util.HashMap programMap=(java.util.HashMap)JPO.unpackArgs(args);
        MapList objectList=(MapList)programMap.get("objectList");
        HashMap map ;
//Bug 305934 - Added code to nullify the object
        Object rev = null;
        ListIterator templateIterator=objectList.listIterator();
        for (; templateIterator.hasNext() ; )  {
// Bug 305934 - Added code for Classcast exception.
               Object obj = templateIterator.next();
                if (obj instanceof HashMap) {
                    rev = ((HashMap)obj).get("revision");

                }
                else if (obj instanceof Hashtable)
                {
                    rev = ((Hashtable)obj).get("revision");

                }

           // map=(HashMap)templateIterator.next();
            //rev=map.get("revision");
// till here
            if ( rev!=null && !((String)rev).equals("") )  {
                RevisionVector.add( (String)rev );
            }
            else  {
                RevisionVector.add("&nbsp;");
            }
        }
        return RevisionVector ;
    }

    /**
    * This method shows descriptions
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @returns MapList
    * @throws Exception if the operation fails
    * @since AEF 10.5 next
    */
    public Vector showDescription(Context context, String args[]) throws Exception
    {

        Vector descriptionsVector=new Vector();
        java.util.HashMap programMap=(java.util.HashMap)JPO.unpackArgs(args);
        MapList objectList=(MapList)programMap.get("objectList");
        HashMap map ;
// Bug 305934 - Added code to nullify the object
        Object desc =null ;
        ListIterator templateIterator=objectList.listIterator();
        for (; templateIterator.hasNext() ; )  {
// Bug 305934 - Added code for Classcast exception.
            Object obj = templateIterator.next();
            if (obj instanceof HashMap) {
                desc = ((HashMap)obj).get("description");

            }
            else if (obj instanceof Hashtable)
            {
                desc = ((Hashtable)obj).get("description");

            }
           // map=(HashMap)templateIterator.next();
           // desc=map.get("description");
// till here
            if ( desc!=null && !((String)desc).equals("") )  {
                descriptionsVector.add( (String)desc );
            }
            else  {
                descriptionsVector.add("");
            }
        }
        return descriptionsVector ;
    }
//  IR-060833V6R2011x modification -Start
    /**
     * getAllWorkspaceContents - method to return the list of all workspace members
     * @param context the eMatrix <code>Context</code> object
     * @return Vector
     * @throws Exception if the operation fails
     * @since R210
     * @grade 0
     */
    public StringList getAllWorkspaceContents(Context context, String folderId) throws Exception
    {
        StringList sContentId = new StringList();
        DomainObject folderObj = new DomainObject(folderId);
        Pattern relPattern  = new Pattern(DomainObject.RELATIONSHIP_VAULTED_DOCUMENTS);
        //if PMC is installed
        relPattern.addPattern(DomainObject.RELATIONSHIP_VAULTED_OBJECTS_REV2);
        
        SelectList resultSelects = new SelectList(1);
        resultSelects.add(DomainObject.SELECT_ID);
        String sWhereExp = "";

        MapList searchResultsForFolder = folderObj.getRelatedObjects(context,
                relPattern.getPattern(),
                "*",
                resultSelects,
                null,
                false,
                true,
                (short)0,
                sWhereExp,
                null,
                (short)0,
                null,
                null,
                null);
        for(int i=0; i<searchResultsForFolder.size(); i++)
        {
            sContentId.add((String)((Map)searchResultsForFolder.get(i)).get(DomainObject.SELECT_ID));
        }
        return sContentId;
    }
//  IR-060833V6R2011x modification -Ends
}
