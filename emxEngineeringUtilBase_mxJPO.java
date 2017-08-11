/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MatrixWriter;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;
import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.Organization;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.CacheUtil;
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
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.engineering.ChartUtil;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>emxEngineeringUtilBase</code> class contains implementation code for emxEngineeringUtil.
 *
 * @version EC Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */

public class emxEngineeringUtilBase_mxJPO extends emxDomainObject_mxJPO
{
	private Map importColumnMappings = null;
    /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @throws Exception if the operation fails.
    * @since EC 10.0.0.0.
    */
    public emxEngineeringUtilBase_mxJPO (Context context, String[] args)
      throws Exception    {
        super(context, args);

    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return int.
     * @throws Exception if the operation fails.
     * @since EC 10.0.0.0.
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on emxDesignResponsibilityDeleteCheck invocation");
        }
        return 0;
    }

    /**
    * This method checks if the Person is owner/RDO member to disconnect.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds the following input arguments.
    *        0 - from id (objectId of organization).
    *        1 - to id (objectId of object).
    * @return int value 1 if failure and 0 if success.
    * @throws Exception if the operation fails.
    * @since EC 10.0.0.0.
    */
    public int DesignResponsibilityDisconnectCheck(matrix.db.Context context,String[] args) throws Exception
    {
      try
      {
          // args[] parameters
          String fromId = args[0];
          String toId = args[1];
          // get the logged in person name
          String  personName =  MqlUtil.mqlCommand(context,"get env $1","USER");
            if (personName.equalsIgnoreCase("User Agent"))
            {
                personName =  MqlUtil.mqlCommand(context,"get env $1","APPREALUSER");
                //added for bug no 317781
                if("".equals(personName))
                {
                  personName=context.getUser();
                }
                // till here
            }
          DomainObject toObject = DomainObject.newInstance(context,toId);
          String objOwner = toObject.getInfo(context, DomainObject.SELECT_OWNER);
          //check if the logged in user is the owner then return 0
          if(objOwner != null && personName.equals(objOwner))
          {
                ContextUtil.pushContext(context);
            return 0;
          }

          // creating the result selects
          SelectList resultSelects = new SelectList(1);
          String memberName  = "from["+RELATIONSHIP_MEMBER+"].to.name";
          DomainObject.MULTI_VALUE_LIST.add(memberName);
          resultSelects.addElement(memberName);

          // creating the organization instance
          DomainObject organization = DomainObject.newInstance(context,fromId);

          Map memberMap = organization.getInfo(context,resultSelects);
          StringList nameList = new StringList();
          try
          {
            String memberNameValue = (String)memberMap.get(memberName);
            if (memberNameValue != null)
            {
              nameList.addElement(memberNameValue);
            }
          }
          catch (ClassCastException classCastEx )
          {
            nameList = (StringList) memberMap.get(memberName);
          }
      //Added for Bug 313092
      DomainObject.MULTI_VALUE_LIST.remove(memberName);
          // check if the logged in person is present in the member list or either Supplier/Buyer.
            //added for bug no 317781
            if(nameList.contains(personName) || personName.equals(PropertyUtil.getSchemaProperty(context,"person_UserAgent")) ||
             PersonUtil.hasAssignment(context,ROLE_SUPPLIER) ||
             PersonUtil.hasAssignment(context,ROLE_SUPPLIER_REPRESENTATIVE) ||
             PersonUtil.hasAssignment(context,ROLE_BUYER) ||
             PersonUtil.hasAssignment(context,ROLE_BUYER_ADMINISTRATOR) )
             // till here
          {
//fix for bug 317972
                ContextUtil.pushContext(context);
//end of fix
            return 0;
          } else {
            emxContextUtil_mxJPO.mqlError(context,
                EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.RDO.MsgNotRDOMemberToModify",
                context.getSession().getLanguage()));
            return 1;
          }
    }
      catch(Exception ex)
      {
        throw (ex);
      }
    }

   /**
     * Gets the Organizations connected to Engineering Objects like ECO, ECR, Part, Specification with the relationship Design Responsibility.
     * This is used in the display of properties page of these objects, normally called from Webform.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the object id.
     * @return String of Organizations connected to the Object with the relationship Design Responsibility in HTML format.
     * @throws Exception if the operation fails.
     * @since 10.6.
     */
    public String getRDOs(Context context,String[] args)
             throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjId = (String) requestMap.get("objectId");
        String languageStr = (String) requestMap.get("languageStr");
        setId(strObjId);
        String strOwner = this.getInfo(context,DomainConstants.SELECT_OWNER);
        String strLoginUser = context.getUser();
        String strDisable ="";
        if(strOwner !=null && strLoginUser !=null && !strOwner.equals(strLoginUser))
        {
           strDisable ="disabled";
        }
        //Start of  IR-015218
        String reportFormat = (String)requestMap.get("reportFormat");
        StringBuffer strBufNamesForExport = new StringBuffer();
        //ends
        java.util.List organizationList = new MapList();

        StringList ObjectSelectsList = new StringList(DomainConstants.SELECT_NAME);
        ObjectSelectsList.add (DomainConstants.SELECT_ID);

        StringList relSelectList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

        ContextUtil.pushContext(context);//Modified for Project Space issue

        organizationList = getRelatedObjects(context,
                                             DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY,
                                             "*",
                                             ObjectSelectsList,
                                             relSelectList,
                                             true,
                                             true,
                                             (short) 1,
                                             DomainConstants.EMPTY_STRING,
                                             DomainConstants.EMPTY_STRING);
        ContextUtil.popContext(context);//Modified for Project Space issue

        String strMode = (String) requestMap.get("mode");

        StringBuffer returnString=new StringBuffer();

        StringBuffer strBufRDONames = new StringBuffer();
        StringBuffer strBufRDOIds      = new StringBuffer();
        StringBuffer strBufRELIds       = new StringBuffer();
        StringBuffer strBufRDONamesForView = new StringBuffer();

         if (!organizationList.isEmpty())
         {
            Iterator mapItr = organizationList.iterator();
            Map mapOrg = null;

            while(mapItr.hasNext())
            {
                mapOrg = (Map)mapItr.next();
                if("edit".equalsIgnoreCase(strMode))
                {
                    if(strBufRDOIds.length() > 0)
                    {
                        strBufRDOIds.append(',');
                    }
                    strBufRDOIds.append(mapOrg.get(DomainConstants.SELECT_ID));

                    if(strBufRELIds.length() > 0)
                    {
                        strBufRELIds.append(',');
                    }
                    strBufRELIds.append(mapOrg.get(DomainConstants.SELECT_RELATIONSHIP_ID));

                    if(strBufRDONames.length() > 0)
                    {
                        strBufRDONames.append(", ");
                    }
                    strBufRDONames.append(mapOrg.get(DomainConstants.SELECT_NAME));
                }
                else
                {
                    //Start of IR-015218
                    if(reportFormat != null && reportFormat.length() > 0){
                        strBufNamesForExport.append((String)((Hashtable)organizationList.get(0)).get(DomainConstants.SELECT_NAME));
                    }
                    //ends
                    // Added - Modified for bug # 314719.
                    String strAccess = "";
                    try
                    {
                        DomainObject domObj = new DomainObject((String)mapOrg.get(DomainConstants.SELECT_ID));
                        strAccess = domObj.getInfo(context, "current.access[read]");
                    }
                    catch (Exception e)
                    {
                        strAccess = "FALSE";
                    }
                    if(strAccess.equalsIgnoreCase("TRUE"))
                    {
                        String URLToShow = "../common/emxTree.jsp?objectId=" + mapOrg.get(DomainConstants.SELECT_ID);
                        if(strBufRDONamesForView.length() > 0)
                        {
                            strBufRDONamesForView.append(", ");
                        }

                        strBufRDONamesForView.append("<a href=\"javascript:showModalDialog(\'"+URLToShow+"\',700,600,false)\">");
                        strBufRDONamesForView.append(mapOrg.get(DomainConstants.SELECT_NAME));
						strBufRDONamesForView.append("</a>");
                    }
                    else
                    {
                        if(strBufRDONamesForView.length() > 0)
                        {
                            strBufRDONamesForView.append(", ");
                        }
                        strBufRDONamesForView.append(mapOrg.get(DomainConstants.SELECT_NAME));
                    }
                }
             }
        }

            if("edit".equalsIgnoreCase(strMode))
            {
                returnString.append("<input type=\"text\" readonly=\"readonly\"  name=\"RDODisplay\"  value=\""+strBufRDONames.toString()+"\">");
                returnString.append("<input type=\"hidden\" name=\"RDO\" value=\""+strBufRDONames.toString()+"\">");
				returnString.append("<input type=\"hidden\" name=\"RDOOID\" value=\""+strBufRDOIds.toString()+"\">");
                returnString.append("<input type=\"hidden\" name=\"RDORELID\" value=\""+strBufRELIds.toString()+"\">");
                returnString.append("<input type=\"hidden\" name=\"OLDRDOID\" value=\""+strBufRDOIds.toString()+"\">");
                returnString.append("<input type=\"button\" name=\"btnRDO\" value='...'  "+strDisable+" onClick=\"");
                returnString.append("javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace:CURRENT=policy_Organization.state_Active&amp;table=ENCAddExistingGeneralSearchResults&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=editDataForm&amp;fieldNameActual=RDOOID&amp;fieldNameDisplay=RDODisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=RDOChooser&amp;HelpMarker=emxhelpfullsearch");
                returnString.append("&amp;objectId="+strObjId + "&amp;ExcludePlant=true,850,630')\">");
                if(!"disabled".equals(strDisable))
                {
                    returnString.append("&nbsp;&nbsp;<a href=\"JavaScript:clearRDO()\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",languageStr)+"</a>");
                }
            }
            else
            {
                returnString = strBufRDONamesForView;
            }
            //Start of IR-015218
            if((strBufNamesForExport.length() > 0 )|| (reportFormat != null && reportFormat.length() > 0))
            {
                returnString = strBufNamesForExport;
            }
            //IR-015218 ends

        return returnString.toString();
     }
    /**
     * Gets the Organizations connected to Engineering Objects like ECO, ECR, Part, Specification with the relationship Change Responsibility.
     * This is used in the display of properties page of these objects, normally called from Webform.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing the object id.
     * @return String of Organizations connected to the Object with the relationship Change Responsibility in HTML format.
     * @throws Exception if the operation fails.
     * @since R207
     */
    public String getChangeResponsibility(Context context,String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strObjId = (String) requestMap.get("objectId");
        setId(strObjId);
        String strOwner = this.getInfo(context,DomainConstants.SELECT_OWNER);
        String strLoginUser = context.getUser();
        String strDisable ="";
        String RELATIONSHIP_CHANGE_RESPONSIBILITY = PropertyUtil.getSchemaProperty(context,"relationship_ChangeResponsibility");
        if(strOwner !=null && strLoginUser !=null && !strOwner.equals(strLoginUser))
        {
           strDisable ="disabled";
        }
        //Start of  IR-015218
        String reportFormat = (String)requestMap.get("reportFormat");
        StringBuffer strBufNamesForExport = new StringBuffer();
        //ends
        java.util.List organizationList = new MapList();

        StringList ObjectSelectsList = new StringList(DomainConstants.SELECT_NAME);
        ObjectSelectsList.add (DomainConstants.SELECT_ID);

        StringList relSelectList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

        ContextUtil.pushContext(context);//Modified for Project Space issue

        organizationList = getRelatedObjects(context,
                                             RELATIONSHIP_CHANGE_RESPONSIBILITY,
                                             "*",
                                             ObjectSelectsList,
                                             relSelectList,
                                             true,
                                             true,
                                             (short) 1,
                                             DomainConstants.EMPTY_STRING,
                                             DomainConstants.EMPTY_STRING);
        ContextUtil.popContext(context);//Modified for Project Space issue

        String strMode = (String) requestMap.get("mode");
        StringBuffer returnString=new StringBuffer();
        StringBuffer strBufChangeResponsibilityNames = new StringBuffer();
        StringBuffer strBufChangeResponsibilityIds      = new StringBuffer();
        StringBuffer strBufRELIds       = new StringBuffer();
        StringBuffer strBufChangeResponsibilityNamesForView = new StringBuffer();

         if (!organizationList.isEmpty())
         {
            Iterator mapItr = organizationList.iterator();
            Map mapOrg = null;

            while(mapItr.hasNext())
            {
                mapOrg = (Map)mapItr.next();
                if("edit".equalsIgnoreCase(strMode))
                {
                    if(strBufChangeResponsibilityIds.length() > 0)
                    {
                        strBufChangeResponsibilityIds.append(',');
                    }
                    strBufChangeResponsibilityIds.append(mapOrg.get(DomainConstants.SELECT_ID));

                    if(strBufRELIds.length() > 0)
                    {
                        strBufRELIds.append(',');
                    }
                    strBufRELIds.append(mapOrg.get(DomainConstants.SELECT_RELATIONSHIP_ID));

                    if(strBufChangeResponsibilityNames.length() > 0)
                    {
                        strBufChangeResponsibilityNames.append(", ");
                    }
                    strBufChangeResponsibilityNames.append(mapOrg.get(DomainConstants.SELECT_NAME));
                }
                else
                {
                    //Start of IR-015218
                    if(reportFormat != null && reportFormat.length() > 0){
                        strBufNamesForExport.append((String)((Hashtable)organizationList.get(0)).get(DomainConstants.SELECT_NAME));
                    }
                    //ends
                    // Added - Modified for bug # 314719.
                    String strAccess = "";
                    try
                    {
                        DomainObject domObj = new DomainObject((String)mapOrg.get(DomainConstants.SELECT_ID));
                        strAccess = domObj.getInfo(context, "current.access[read]");
                    }
                    catch (Exception e)
                    {
                        strAccess = "FALSE";
                    }
                    if(strAccess.equalsIgnoreCase("TRUE"))
                    {
                        String URLToShow = "../common/emxTree.jsp?objectId=" + mapOrg.get(DomainConstants.SELECT_ID);
                        if(strBufChangeResponsibilityNamesForView.length() > 0)
                        {
                            strBufChangeResponsibilityNamesForView.append(", ");
                        }

                        strBufChangeResponsibilityNamesForView.append("<a href=\"javascript:showModalDialog(\'"+URLToShow+"\',700,600,false)\">");
                        strBufChangeResponsibilityNamesForView.append(mapOrg.get(DomainConstants.SELECT_NAME));
						strBufChangeResponsibilityNamesForView.append("</a>");
                    }
                    else
                    {
                        if(strBufChangeResponsibilityNamesForView.length() > 0)
                        {
                            strBufChangeResponsibilityNamesForView.append(", ");
                        }
                        strBufChangeResponsibilityNamesForView.append(mapOrg.get(DomainConstants.SELECT_NAME));
                    }
                }
             }
        }

            if("edit".equalsIgnoreCase(strMode))
            {
                returnString.append("<input type=\"text\" readonly=\"readonly\"  name=\"ChangeResponsibilityDisplay\"  value=\""+strBufChangeResponsibilityNames.toString()+"\">");
                returnString.append("<input type=\"hidden\" name=\"ChangeResponsibility\" value=\""+strBufChangeResponsibilityNames.toString()+"\">");
                returnString.append("<input type=\"hidden\" name=\"ChangeResponsibilityOID\" value=\""+strBufChangeResponsibilityIds.toString()+"\">");
                returnString.append("<input type=\"hidden\" name=\"ChangeResponsibilityRELID\" value=\""+strBufRELIds.toString()+"\">");
                returnString.append("<input type=\"hidden\" name=\"OLDChangeResponsibilityID\" value=\""+strBufChangeResponsibilityIds.toString()+"\">");
                returnString.append("<input type=\"button\" name=\"btnChangeResponsibility\" value='...'  "+strDisable+" onClick=\"");
                returnString.append("javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Organization:CURRENT=policy_Organization.state_Active&amp;table=ENCAddExistingGeneralSearchResults&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=editDataForm&amp;fieldNameActual=ChangeResponsibilityOID&amp;fieldNameDisplay=ChangeResponsibilityDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=RDOChooser&amp;ExcludePlant=true&amp;includeOIDprogram=emxENCFullSearchBase:includeOIDLeadOrganizations&amp;HelpMarker=emxhelpfullsearch");
                returnString.append("&amp;objectId="+strObjId + "&amp;ExcludePlant=true,850,630')\">");
            }
            else
            {
                returnString = strBufChangeResponsibilityNamesForView;
            }
            if((strBufNamesForExport.length() > 0 )|| (reportFormat != null && reportFormat.length() > 0))
            {
                returnString = strBufNamesForExport;
            }

        return returnString.toString();
}

    /**
     * Updates the RDO field in type_Part form
     * @param context the eMatrix <code>Context</code> object.
     * @param args contains a packed HashMap containing objectId of change object
     * @return HashMap.
     * @since EngineeringCentral X6
     * @throws Exception if the operation fails.
    */
    public static HashMap updateRDOField(Context context, String args[])  throws Exception {
        Map argMap = (Map)JPO.unpackArgs(args);
        Map requestMap = (Map) argMap.get("requestMap");
        Map fieldValues = (Map)argMap.get("fieldValues");
        String partId = (String) requestMap.get("objectId");
        String languageStr = (String) requestMap.get("languageStr");
        String RELATIONSHIP_DESIGN_RESPONSIBILITY =
                 PropertyUtil.getSchemaProperty(context,"relationship_DesignResponsibility");
        String fromId = "relationship[" + RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id";
        String fromName = "relationship[" + RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.name";
        DomainObject doPart = new DomainObject(partId);
        String oldRDOid  = doPart.getInfo(context, fromId);
        if (oldRDOid == null || "".equals(oldRDOid)) {
            oldRDOid = "";
        }
        String oldRDORelid = doPart.getInfo(context, "relationship[" + RELATIONSHIP_DESIGN_RESPONSIBILITY + "].id");
        if (oldRDORelid == null || "".equals(oldRDORelid)) {
            oldRDORelid = "";
        }
        String strDisable ="";
        String strOwner = doPart.getInfo(context,DomainConstants.SELECT_OWNER);
        String strLoginUser = context.getUser();
        if(strOwner !=null && strLoginUser !=null && !strOwner.equals(strLoginUser))
        {
            strDisable ="disabled";
        }
        StringBuffer returnString = new StringBuffer(128);
        HashMap hmpRDO = new HashMap();
        if (fieldValues != null)  {
            String sECOName = (String)fieldValues.get("ECO");
            if (sECOName != null && !"".equals(sECOName)) {
                String result = MqlUtil.mqlCommand(context, "print bus $1 $2 $3 select $4 $5 $6 dump $7",
                                                   "ECO",sECOName, "-","id",fromId,fromName,"|");
                                                   
                if (result != null && !"".equals(result)) {
                    StringList slData = FrameworkUtil.split(result, "|");
                    Iterator itrList = slData.iterator();
                    String companyId = (itrList.hasNext())?(String) itrList.next():"";
                    String companyName = (itrList.hasNext())?(String) itrList.next():"";
                    returnString.append("<input type=\"text\" readonly=\"readonly\"  name=\"RDODisplay\"  value=\""+companyName+"\">");
                    returnString.append("<input type=\"hidden\" name=\"RDO\" value=\""+companyName+"\">");
                    returnString.append("<input type=\"hidden\" name=\"RDOOID\" value=\""+companyId+"\">");
                    returnString.append("<input type=\"hidden\" name=\"RDORELID\" value=\""+oldRDORelid+"\">");
                    returnString.append("<input type=\"hidden\" name=\"OLDRDOID\" value=\""+oldRDOid+"\">");
                    returnString.append("<input type=\"button\" name=\"btnRDO\" value='...'  "+strDisable+" onClick=\"");
                    returnString.append("javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace:CURRENT=policy_Organization.state_Active&amp;table=ENCAddExistingGeneralSearchResults&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=editDataForm&amp;fieldNameActual=RDOOID&amp;fieldNameDisplay=RDODisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=RDOChooser&amp;HelpMarker=emxhelpfullsearch");
                    returnString.append("&amp;objectId="+partId + "&amp;ExcludePlant=true,850,630')\">");
                    if(!"disabled".equals(strDisable))
                    {
                        returnString.append("&nbsp;&nbsp;<a href=\"JavaScript:clearRDO()\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",languageStr)+"</a>");
                    }
                    hmpRDO.put("SelectedValues", returnString.toString());
                    hmpRDO.put("SelectedDisplayValues", returnString.toString());
                }
            }
        }
        return hmpRDO;
    }

    /**
     * Connects ECR/ECO with the Passed Object.
     * @param context the eMatrix <code>Context</code> object
     * @param Hashmap holds the input arguments:
     * strRelationship holds relationship with which ECR will be connected
     * New Value is object Id of updated Object
     * @throws Exception if the operations fails
     * @since Common X3.
    */
    public DomainRelationship connect(Context context , HashMap paramMap ,String strRelationship, boolean isToSide) throws Exception {
         try {
            DomainRelationship drship=null;
            //Relationship name
            DomainObject oldListObject = null;
            DomainObject newListObject = null;
            //Getting the ECR Object id and the new MemberList object id
            String strChangeobjectId = (String)paramMap.get("objectId");
            DomainObject changeObj =  new DomainObject(strChangeobjectId);
            //for bug 343816 and 343817 starts
            String strNewToTypeObjId = (String)paramMap.get("New OID");

            if (strNewToTypeObjId == null || "null".equals(strNewToTypeObjId) || strNewToTypeObjId.length() <= 0
                      || "Unassigned".equals(strNewToTypeObjId)) {
                strNewToTypeObjId = (String)paramMap.get("New Value");
            }
            //for bug 343816 and 343817 ends
            String strOldToTypeObjId = (String)paramMap.get("Old OID");

            RelationshipType relType = new RelationshipType(strRelationship);
            if (strOldToTypeObjId != null && !"null".equals(strOldToTypeObjId) && strOldToTypeObjId.length() > 0
                      && !"Unassigned".equals(strOldToTypeObjId)) {
                    oldListObject = new DomainObject(strOldToTypeObjId);
                    changeObj.disconnect(context, relType, isToSide, oldListObject);
            }

            if(strNewToTypeObjId != null && !"null".equals(strNewToTypeObjId) && strNewToTypeObjId.length() > 0
                    && !"Unassigned".equals(strNewToTypeObjId)) {
                newListObject = new DomainObject(strNewToTypeObjId);

                drship = new DomainRelationship(isToSide ? DomainRelationship.connect(context,changeObj,relType,newListObject)
                              : DomainRelationship.connect(context, newListObject, relType, changeObj)) ;
            }

              return drship;
         } catch(Exception ex) {
             throw  new FrameworkException((String)ex.getMessage());
         }

      }

      /**
       * Updates the Desposibity in ECO WebForm.
       * @param context the eMatrix <code>Context</code> object
       * @param args contains a MapList with the following as input arguments or entries:
       * objectId holds the context ECR object Id
       * New Value holds the newly selected Reported Against Object Id
       * @throws Exception if the operations fails
       * @since Common X3
       */
      public void connectDesignResponsibility(Context context, String[] args) throws Exception {
          //unpacking the Arguments from variable args
          HashMap programMap = (HashMap)JPO.unpackArgs(args);
          HashMap paramMap   = (HashMap)programMap.get("paramMap");

          //Relationship name
          String strRelationship = PropertyUtil.getSchemaProperty(context,"relationship_DesignResponsibility");
          //Calling the common connect method to connect objects
          connect(context,paramMap,strRelationship, false);
		   //Modified for RDO Convergence start
          String strObjectId = (String)paramMap.get("objectId");
          DomainObject dObj = new DomainObject(strObjectId);
          String strRDOName = (String)paramMap.get("New Value");
          dObj.setPrimaryOwnership(context, dObj.getAltOwner2(context).toString(), strRDOName);

        //Added for IR-216979 start
  		if(UIUtil.isNotNullAndNotEmpty(strRDOName)) {
  			dObj.setPrimaryOwnership(context, EngineeringUtil.getDefaultProject(context), strRDOName);
  		} else {
  			dObj.setPrimaryOwnership(context, EngineeringUtil.getDefaultProject(context), EngineeringUtil.getDefaultOrganization(context));
  		}
  		//Added for IR-216969 End
          //Modified for RDO Convergence End
      }

      /**
       * Updates the Desposibity in ECO WebForm.
       * @param context the eMatrix <code>Context</code> object
       * @param args contains a MapList with the following as input arguments or entries:
       * objectId holds the context ECR object Id
       * New Value holds the newly selected Reported Against Object Id
       * @throws Exception if the operations fails
       * @since Common X3
       */
      public void connectChangeResponsibility(Context context, String[] args) throws Exception {
          //unpacking the Arguments from variable args
          HashMap programMap = (HashMap)JPO.unpackArgs(args);
          HashMap paramMap   = (HashMap)programMap.get("paramMap");
          //Relationship name
          String strRelationship = PropertyUtil.getSchemaProperty(context,"relationship_ChangeResponsibility");
          //Calling the common connect method to connect objects
          connect(context,paramMap,strRelationship, false);
      }

      /**
       * Generates dynamic query for RDE field
       * @param context
       * @param args
       * @return String
       * @throws Exception
       */
      public String getRDEDynamicSearchQuery(Context context, String[] args) throws Exception {
          //unpacking the Arguments from variable args
          HashMap programMap = (HashMap)JPO.unpackArgs(args);
          Map fieldValuesMap = (HashMap)programMap.get("fieldValues");
          String fieldName = fieldValuesMap.containsKey("DesignResponsibilityOID")
                              ? "DesignResponsibilityOID" : "ChangeResponsibilityOID";

          String editFieldName = fieldValuesMap.containsKey("RDOOID")
                              ? "RDOOID" : "ChangeResponsibilityOID";

          String editMode = (String)((HashMap)programMap.get("requestMap")).get("mode");
          fieldName = "edit".equalsIgnoreCase(editMode) ? editFieldName : fieldName;

          String orgId = (String)fieldValuesMap.get(fieldName);
          DomainObject objDR = new DomainObject(orgId);
          //89733
          //return "MEMBER_ID="+orgId+":USERROLE=role_SeniorDesignEngineer";
          //IR-155395
          String fields = DomainConstants.TYPE_PROJECT_SPACE.equals(objDR.getInfo(context, DomainConstants.SELECT_TYPE))
  					? "MEMBER_ID="+orgId+":USERROLE=role_SeniorDesignEngineer" : "PROJECT_ROLE=role_SeniorDesignEngineer"+orgId;
          //IR-155395
          return fields;
          //89733
      }

    /**
       * Generates dynamic query for RME field
       * @param context
       * @param args
       * @return String
       * @throws Exception
       */
      public String getRMEDynamicSearchQuery(Context context, String[] args) throws Exception {
          //unpacking the Arguments from variable args
          HashMap programMap = (HashMap)JPO.unpackArgs(args);
          Map fieldValuesMap = (HashMap)programMap.get("fieldValues");
          String editMode = (String)((HashMap)programMap.get("requestMap")).get("mode");
          String orgId = "edit".equalsIgnoreCase(editMode) ? (String)fieldValuesMap.get("RDOOID")
                              : (String)fieldValuesMap.get("DesignResponsibilityOID");
          DomainObject objDR = new DomainObject(orgId);
          //89733
          //return "MEMBER_ID="+orgId+":USERROLE=role_SeniorManufacturingEngineer";
          //IR-155395
          String fields = DomainConstants.TYPE_PROJECT_SPACE.equals(objDR.getInfo(context, DomainConstants.SELECT_TYPE))
          			? "MEMBER_ID="+orgId+":USERROLE=role_SeniorManufacturingEngineer" : "PROJECT_ROLE=role_SeniorManufacturingEngineer"+orgId;
          //IR-155395
          return fields;
          //89733
      }

      /**
       * Generates dynamic query for RME field
       * @param context
       * @param args
       * @return String
       * @throws Exception
       */
      public String getECOToReleaseSearchQuery(Context context, String[] args) throws Exception {
          //unpacking the Arguments from variable args
          HashMap programMap = (HashMap)JPO.unpackArgs(args);
          Map fieldValuesMap = (HashMap)programMap.get("fieldValues");

		  //Modified for RDO Convergence start
          String orgId = "";
           orgId = fieldValuesMap.containsKey("DesignResponsibility") ? (String)fieldValuesMap.get("DesignResponsibility")
    	            : (String)fieldValuesMap.get("RDO");
          String filterStr = (orgId != null && !"".equals(orgId) && !"null".equals(orgId)) ? ":ORGANIZATION=" + orgId : "";

          //Modified for RDO Convergence End
          return "TYPES=type_ECO:Policy!=policy_DECO,policy_TeamECO:CURRENT=policy_ECO.state_Create,policy_ECO.state_DefineComponents,policy_ECO.state_DesignWork"+filterStr;
      }

      /**
       * Method to check whether VPM is installed to show VPM specific attributes in type_Part form.
       * @param context
       * @return boolean
       * @throws Exception
       */
      public Boolean isVPMInstalled(Context context, String[] args) throws FrameworkException {
    	  return EngineeringUtil.isVPMInstalled(context, args);
      }

      /** returns true if data is null, else it returns false.
       * @param data any string
       * @return boolean
      */
      private boolean isNullOrBlank(String data) {
    	  return ((data == null || "null".equals(data)) ? 0 : data.trim().length()) == 0;
      }

       /** This method is called from update program of ECO/ECR, Create/Edit, Reviewer/Approval List fields.
       * @param context ematrix context.
       * @param args holds a Map with the following input arguments.
       * @throws Exception if any operation fails.
       */
      public void updateRouteObject (Context context, String[] args) throws Exception {
           HashMap programMap = (HashMap) JPO.unpackArgs(args);
           HashMap paramMap   = (HashMap) programMap.get("paramMap");
           DomainRelationship newRelationship = connect(context, paramMap, DomainConstants.RELATIONSHIP_OBJECT_ROUTE, true);
           if (newRelationship != null) {
        	   String strNewObjId = (String) paramMap.get("New OID"); // While creating/editing

               if (isNullOrBlank(strNewObjId)) {
            	   strNewObjId = (String) paramMap.get("New Value"); // if there is only one object data will come in this variable
               }

               newRelationship.setAttributeValue(context,
                		 DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,
                		 newInstance(context, strNewObjId).getAttributeValue(context, DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE));
           }
       }

      /**
       * Updates the Distribution List field in ECR WebForm.
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the following input arguments:
       * object Id object Id of context ECR.
       * New Value object Id of updated Distribution List Object
       * @throws Exception if the operations fails
       * @since Common X3.
    */
       public void connectDistributionList (Context context, String[] args) throws Exception {
           //unpacking the Arguments from variable args
           HashMap programMap = (HashMap) JPO.unpackArgs(args);
           HashMap paramMap   = (HashMap) programMap.get("paramMap");
           //Relationship name
		   String strRelationship = PropertyUtil.getSchemaProperty(context, "relationship_ECDistributionList");
		   //Calling the common connect method to connect objects
		   connect(context, paramMap, strRelationship, true);
       }

       /* Generates the Include Releated Data field for Clone Part Form
       *
       *
       * @mx.whereUsed Invoked from common Clone Part web form's IncludeReleatedData field
       *
       * @mx.summary this methods takes the values from Application property file
       *             And generates the Check boxes depending on the value passed
       *
       * @param context
       *            the eMatrix <code>Context</code> object
       * @param args
       * @return the <code>String</code> value
       * @throws Exception
       *             if the operation fails
       * @since V6R2009-1
       */
          public String showIncludeRelDataField(Context context, String[] args)
              throws Exception {
          StringBuffer sb = new StringBuffer();
          try {
              HashMap programMap = (HashMap) JPO.unpackArgs(args);
              HashMap requestMap = (HashMap) programMap.get("requestMap");
              String objectId = (String) requestMap.get("copyObjectId");
              String srcObjType = "";

              String relName = "";
              String sRelI18Nkey = "";
              String sRelDisplayLbl="";

              Map keyLabel =  new HashMap();

              StringList allAppsIncRelData=new StringList();
              StringList allAppsI18nIncRelData=new StringList();

              String specificIncRel = "";
              String applications = "";

              boolean isPartEnabled = false;
              DomainObject domPartObj = null;
              //373625
              boolean ispartType =false;
              //End
              // MCC Specific functionality, checks whether context source object if EC Part and is it eabled for compliance
              // to show MCC Specific relationships
              if ((objectId != null && !objectId.equals("") && !objectId
                      .equals("null"))) {
                  //Find whether enabled for compliance or not
                  domPartObj = new DomainObject(objectId);
                  srcObjType = domPartObj.getInfo(context, SELECT_TYPE);
                  srcObjType = FrameworkUtil.getAliasForAdmin(context, "type", srcObjType, true);
                  String TYPE_PART = PropertyUtil.getSchemaProperty(context,
                          "type_Part");
                 //373625
                 ispartType = domPartObj.isKindOf(context, TYPE_PART);
                 //End
                   if (domPartObj.isKindOf(context, TYPE_PART)) {
                      String attrEnableCompliance = PropertyUtil
                              .getSchemaProperty(context,
                                      "attribute_EnableCompliance");
                      String attrEnableCompVal = domPartObj
                              .getAttributeValue(context, attrEnableCompliance);
                      if (attrEnableCompVal != null
                              && ("Enabled").equals(attrEnableCompVal)) {
                          isPartEnabled = true;
                      }
                  }
              }

              // Get the applications to shows the relationship chckboxes
              try{
                  applications = FrameworkProperties.getProperty(context,"emxComponents.PartClone.Applications");
              }catch(Exception ex){
                  applications = "Components";
              }
              StringList applicationList= FrameworkUtil.split(applications,",");
              com.matrixone.apps.common.Person person = null;

              for (int appItr=0;appItr<applicationList.size() ;appItr++)
              {
                  // Get the application specific relationship pattern and loop thru
                  try{
                     specificIncRel = FrameworkProperties.getProperty(context,"emx"+applicationList.get(appItr)+".PartClone.IncludeRelData");
                  }catch(Exception ex){
                     specificIncRel = "";
                  }
                  StringList appSpeIncRel = FrameworkUtil.split(specificIncRel,",");
                  for(int indAppRel=0;indAppRel<appSpeIncRel.size();indAppRel++){

                     // Take the individual rel pattern and check whether it should be shown conditionally for a
                     // specific type or role
                     String sPattern=(String)appSpeIncRel.get(indAppRel);
                     // get the relationship name from the pattern
                     String IndividualRelPattern=sPattern;
                     if(IndividualRelPattern.indexOf('|')>0){
                       IndividualRelPattern=(IndividualRelPattern.substring(0,IndividualRelPattern.indexOf('|'))).trim();
                     }
                     StringList strListIndTypeRels = FrameworkUtil.split(sPattern,"|");
                     if(strListIndTypeRels.size()>=1){
                        strListIndTypeRels.remove(0);
                     }
                     StringList strListIndRoleRels = new StringList();

                     Iterator itr = strListIndTypeRels.iterator();
                     while(itr.hasNext()) {
                         String typeOrRole = (String)itr.next();
                         if(typeOrRole.startsWith("role_")) {
                             strListIndRoleRels.add(typeOrRole);
                             itr.remove();
                         }
                     }

                     // do not show the checkbox if type check is not satified
                     if(strListIndTypeRels.size()>0)
                     {
                        if(objectId!=null) {
                             // do not show checkbox if context object type is not in the type pattern specified for this rel
                             if (!strListIndTypeRels.contains(srcObjType)) {
                                 //373625
                                 if(!ispartType)
                                 //End
                                 continue;
                             }

                             // this is MCC Specific check
                             // do not show checkbox if context rel is of MCC and context source part is of type Part and its not enabled for compliance
                             if(strListIndTypeRels.contains("type_ComplianceEnterprisePart") && "type_Part".equals(srcObjType) && !isPartEnabled) {
                                 continue;
                             }
                             if(IndividualRelPattern.equalsIgnoreCase("relationship_CBOM") && "type_Part".equals(srcObjType) && strListIndTypeRels.contains("type_ComplianceEnterprisePart")) {
                                 continue;
                             }


                        }else if(strListIndTypeRels.contains("type_ComplianceEnterprisePart")) {
                            continue;
                        }
                     }

                     // Check for Role access, make a seprate role list
                     StringList strListTypeRoles =  new StringList();
                     if(sPattern.indexOf("role_") >= 0)
                     {
                         itr = strListIndRoleRels.iterator();
                         while(itr.hasNext())
                         {
                             String pattern = (String)itr.next();
                             if(pattern.startsWith("Role_"))
                             {
                                 strListTypeRoles.addElement(pattern);
                             }
                         }

                     }
                     // if roles are defined then check whether context user has the role(s)
                     if(strListTypeRoles.size() > 0)
                     {
                          // initialize the person obj
                          if(person == null)
                          {
                              person = com.matrixone.apps.common.Person.getPerson(context);
                          }

                          // iterate thru the roles
                          Iterator itrRole = strListTypeRoles.iterator();
                          boolean hasRole = false;

                          while(itrRole.hasNext())
                          {
                              // check whether person has the defied role
              if (person.hasRole(context, PropertyUtil.getSchemaProperty(context,
                                  (String)itrRole.next())))
                              {
                                  hasRole = true;
                                  break;
                              }
                          }
                          // if person does not have role defined then do not show check boxes
                          if(!hasRole)
                          {
                              continue;
                          }
                     }

                     // Get the application specific display name for the checkboxes
                     // Store on common map to remove duplicates
                     if(!allAppsIncRelData.contains(IndividualRelPattern))
                     {
                          sRelI18Nkey = "emx"+applicationList.get(appItr)+".PartCloneIncludeRelData."+IndividualRelPattern;
                            sRelDisplayLbl =EnoviaResourceBundle.getProperty(context, "emx"+applicationList.get(appItr)+"StringResource", context.getLocale(),sRelI18Nkey);

                          if (sRelI18Nkey.equals(sRelDisplayLbl))
                          {
                              relName = FrameworkUtil.findAndReplace(PropertyUtil.getSchemaProperty(context,IndividualRelPattern), " ", "_");
                              sRelDisplayLbl =EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Relationship." +relName);
                          }
                          // Store the internationalized display name
                          allAppsI18nIncRelData.add(sRelDisplayLbl);
                          // Store the actual rel name
                          allAppsIncRelData.add(IndividualRelPattern);

                          // put in the map to display
                          keyLabel.put(sRelDisplayLbl,IndividualRelPattern);
                     }
                  }
              }

              // Sort the checkboxes by diosplay name
              allAppsI18nIncRelData.sort();

              sb = getCloneOptions(context, domPartObj, allAppsI18nIncRelData, keyLabel);
          } catch (Exception ex) {
              ex.printStackTrace();
          }
          return sb.toString();
      }

       private StringBuffer getCloneOptions(Context context, DomainObject domPartObj, StringList list, Map keyLabel) throws Exception {
    	    String strChecked;
    	    String strChkBoxValue;
    	    String strDefaultValue = DomainConstants.RELATIONSHIP_EBOM + "," + RELATIONSHIP_REFERENCE_DOCUMENT;

	       	StringBuffer sbHTMLOutPut = new StringBuffer(2048);
	       	sbHTMLOutPut.append("<input type=\"hidden\" name=\"hdnSelectedCloneOptions\" value=\"").append(XSSUtil.encodeForHTMLAttribute(context, strDefaultValue)).append("\" />");
	       	sbHTMLOutPut.append("<table>");
	       		sbHTMLOutPut.append("<tr>");

	   	    		sbHTMLOutPut.append("<td>");
		   	    		sbHTMLOutPut.append("");
		   	    		sbHTMLOutPut.append("<input type=\"checkbox\" name=\"SelectAllorNone\"  onclick=\"javascript:selectAllOrNone(this.checked)\" />");
		   	    		sbHTMLOutPut.append(EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(),"emxComponents.PartCloneIncludeRelData.AllRel"));
	   	    		sbHTMLOutPut.append("</td>");
					
					sbHTMLOutPut.append("<td style=\"width:30px\"/>");		//IR-402417:Add vertical spacing
					
	   	    		sbHTMLOutPut.append("<td>");
	       				sbHTMLOutPut.append("<table cellspacing=\"5\">");
	       				if(null!=domPartObj){
	       				String srcObjType = domPartObj.getInfo(context, SELECT_TYPE);
	                    srcObjType = FrameworkUtil.getAliasForAdmin(context, "type", srcObjType, true);
	       				}
	       				for (int i = 0, size = list.size(); i < size; i++) {
	       					strChkBoxValue = PropertyUtil.getSchemaProperty(context, (String) keyLabel.get((String)list.get(i)));
	       					strChecked = (RELATIONSHIP_EBOM.equals(strChkBoxValue) || RELATIONSHIP_REFERENCE_DOCUMENT.equals(strChkBoxValue))
	       					? " checked=\"true\"" : "";

	       				    //Modified for To Create Multiple part from Part Clone start
	       					if(null!=domPartObj && domPartObj.isKindOf(context,EngineeringConstants.TYPE_MANUFACTURING_PART)) {
	       						strChecked = (RELATIONSHIP_REFERENCE_DOCUMENT.equals(strChkBoxValue))
	       						? " checked=\"true\"" : "";
	       					} else {
	       						strChecked = (RELATIONSHIP_EBOM.equals(strChkBoxValue) || RELATIONSHIP_REFERENCE_DOCUMENT.equals(strChkBoxValue))
	       						? " checked=\"true\"" : "";
	       					}
	       				   //Modified for To Create Multiple part from Part Clone end
		                    if(null!=domPartObj && domPartObj.isKindOf(context,EngineeringConstants.TYPE_MANUFACTURING_PART) && (strChkBoxValue.equals(DomainConstants.RELATIONSHIP_EBOM) || strChkBoxValue.equals(DomainConstants.RELATIONSHIP_COMPONENT_SUBSTITUTION)
		                    		 || strChkBoxValue.equals(DomainConstants.RELATIONSHIP_ALTERNATE) || strChkBoxValue.equals(DomainConstants.RELATIONSHIP_SPARE_PART))){
		                    	continue;
		                    }

	       					sbHTMLOutPut.append("<tr>");
	       						sbHTMLOutPut.append("<td>");
	       							sbHTMLOutPut.append("<input type=\"checkbox\" name=\"CloneOptions\" value=\"").append(XSSUtil.encodeForHTMLAttribute(context,strChkBoxValue)).append("\" ").append("onclick=\"javascript:collectSelectedValues(this)\"").append(strChecked).append("/>").append(list.get(i));
	       						sbHTMLOutPut.append("</td>");
	       					sbHTMLOutPut.append("</tr>");

	       			 }
	       				sbHTMLOutPut.append("</table>");
	       			sbHTMLOutPut.append("</td>");

	   	    	sbHTMLOutPut.append("</tr>");
	   	    sbHTMLOutPut.append("</table>");

	   	    sbHTMLOutPut.append("<script language=\"javascript\">");
		   	sbHTMLOutPut.append("function selectAllOrNone(checked) {var length = document.emxCreateForm.CloneOptions.length;var i = 0;var selectedValues=\"\";while (true) {document.emxCreateForm.CloneOptions[i].checked = checked;if(checked){selectedValues=(selectedValues==\"\")?document.emxCreateForm.CloneOptions[i].value:(selectedValues+ \",\"+document.emxCreateForm.CloneOptions[i].value);}i++;if (i == length) {break;}}document.emxCreateForm.hdnSelectedCloneOptions.value=selectedValues;}");
		   	sbHTMLOutPut.append("function collectSelectedValues(selectedChkBox) {var selectedValues = document.emxCreateForm.hdnSelectedCloneOptions.value;var length = document.emxCreateForm.CloneOptions.length;var i = 0;var selectAllChkBox = true;if (selectedChkBox.checked) {while (true) {if (i == length) {break;}if (document.emxCreateForm.CloneOptions[i].checked == false) {selectAllChkBox = false;break;}i++;}selectedValues = (selectedValues == \"\") ? selectedChkBox.value : (selectedValues + \",\" + selectedChkBox.value);if (selectAllChkBox){document.emxCreateForm.SelectAllorNone.checked = true;}} else {document.emxCreateForm.SelectAllorNone.checked = false;selectedValueArr = selectedValues.split(\",\");selectedValues = \"\";var arrLength = selectedValueArr.length;var j = 0;while (true) {if (j == arrLength) {break;}if (selectedValueArr[j] != selectedChkBox.value) {selectedValues = (selectedValues == \"\") ? selectedValueArr[j] : (selectedValues + \",\" + selectedValueArr[j]);}j++;}}document.emxCreateForm.hdnSelectedCloneOptions.value = selectedValues;}");
		   	sbHTMLOutPut.append("</script>");

	       	return sbHTMLOutPut;
       }

       /**
        * Method call to get the email as an link to send mails using the client.
        *
        * @param context the eMatrix <code>Context</code> object
        * @param args holds the HashMap containing the following arguments
        *      objectList - MapList containn the list of busines objetcs
        *      paramList - HasMap containg the argument reportFormat
        * @return Object - Vector of email ids
        * @throws Exception if the operation fails
        * @since  Eng 214
        */
       public Vector getAssigneeEmail (Context context, String[] args) throws Exception {
           //Unpacking the args
           HashMap programMap = (HashMap)JPO.unpackArgs(args);
           //Gets the objectList from args
           MapList relBusObjPageList = (MapList)programMap.get("objectList");
           Vector vctAssigneeMailList = new Vector();
           HashMap paramList = (HashMap)programMap.get("paramList");
           String strReportFormat=(String)paramList.get("reportFormat");

           if (!(relBusObjPageList != null)){
               throw  new Exception(ComponentsUtil.i18nStringNow("emxComponents.CommonEngineeringChangeBase.ContextNoObjects", context.getLocale().getLanguage()));
           }
           //Number of objects
           int iNoOfObjects = relBusObjPageList.size();
           String arrObjId[] = new String[iNoOfObjects];
           //Getting the bus ids for objects in the table
           for (int i = 0; i < iNoOfObjects; i++) {
               Object obj = relBusObjPageList.get(i);
               if (obj instanceof HashMap) {
                   arrObjId[i] = (String)((HashMap)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
               }
               else if (obj instanceof Hashtable)
               {
                   arrObjId[i] = (String)((Hashtable)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
               }
           }

           StringList listSelect = new StringList(1);
           String strAttrb1 = "attribute[" + DomainConstants.ATTRIBUTE_EMAIL_ADDRESS+ "]";
           listSelect.addElement(strAttrb1);

           //Instantiating BusinessObjectWithSelectList of matrix.db and fetching  attributes of the objectids
           BusinessObjectWithSelectList attributeList = getSelectBusinessObjectData(context, arrObjId, listSelect);

           for (int i = 0; i < iNoOfObjects; i++) {
               //Getting the email ids from the Map
               String strEmailId = attributeList.getElement(i).getSelectData(strAttrb1);
               String strEmailAdd = null;
              if(strReportFormat!=null && !strReportFormat.equals("null") && !strReportFormat.equals("")){
                   vctAssigneeMailList.add(strEmailId);
               } else {
                   strEmailAdd = "<B><A HREF=\"mailto:" + XSSUtil.encodeForHTMLAttribute(context, strEmailId) + "\">" + XSSUtil.encodeForHTMLAttribute(context,strEmailId)+ "</A></B>";
                   vctAssigneeMailList.add(strEmailAdd);
               }
           }
           return  vctAssigneeMailList;
       }

       /**
        * Gets all VPLMProject Leader in Host company.
        *
        * @param context the eMatrix <code>Context</code> object
        * @param args holds the String arrat containing the Param Map
		*
        * @return MapList - returns the Maplist of persons who are assigned VPLMProjectLeader role in the Host company
        * @throws Exception if the operation fails
        * @since  Eng 215
        */
       @com.matrixone.apps.framework.ui.ProgramCallable
       public MapList getHostCompanyMemebers (Context context,String[] args)
       throws Exception
       {
            String objectId = Company.getHostCompany(context);

            MapList mapList = new MapList();
            MapList finalList = new MapList();
            try
            {
                Organization organization = (Organization)newInstance(context, objectId);
                StringList selectStmts = new StringList();
                selectStmts.addElement(DomainConstants.SELECT_ID);
                selectStmts.addElement(DomainConstants.SELECT_NAME);
                mapList = organization.getMemberPersons(context, selectStmts, null, null);
                Iterator itr = mapList.iterator();
                while (itr.hasNext()) {
                	Map m = (Map)itr.next();
             	   	String result = MqlUtil.mqlCommand(context, "print person $1 select assignment dump",
							   (String)m.get(DomainConstants.SELECT_NAME));
             	   	if (result.contains("VPLMProjectLeader"))
             	   		finalList.add(m);
                }

            }
            catch (FrameworkException Ex)
            {
               throw Ex;
            }
            return finalList;
        }

       /**
        * Returns the Vector for the table column display to show the lead Change role assignment.
        *
        * @param context the eMatrix <code>Context</code> object
        * @param args holds the String arrat containing the Param Map
		*
        * @return Vector - returns the Maplist of persons who are assigned VPLMProjectLeader role in the Host company
        * @throws Exception if the operation fails
        * @since  Eng 215
        */
	public Vector getPersonsLeadRolesAssignment(Context context, String[] args) throws Exception {
    HashMap programMap 			= (HashMap)JPO.unpackArgs(args);
    MapList relBusObjPageList 	= (MapList)programMap.get("objectList");
    HashMap paramMap 			= (HashMap)programMap.get("paramList");
    String strLanguage      	= (String)paramMap.get("languageStr");
    String objectId 			= Company.getHostCompany(context);

    String selAttrProjectRole = DomainObject.getAttributeSelect(ATTRIBUTE_PROJECT_ROLE);

    DomainObject obj = DomainObject.newInstance(context);
    obj.setId(objectId);

    MapList leadResPersons = (MapList) obj.getRelatedObjects(context, RELATIONSHIP_LEAD_RESPONSIBILITY, TYPE_PERSON,
                                                                      new StringList(SELECT_ID), new StringList(selAttrProjectRole),
                                                                      false,true,
                                                                      (short)1,
                                                                      EMPTY_STRING, EMPTY_STRING,
                                                                      0,
                                                                      null, null, null);
    Map rolesByPerson = new HashMap(leadResPersons.size());
    for (int i = 0; i < leadResPersons.size(); i++) {
        Map person = (Map) leadResPersons.get(i);
        rolesByPerson.put(person.get(SELECT_ID), person.get(selAttrProjectRole));
    }

    Vector v = new Vector(relBusObjPageList.size());

    for (int i = 0; i < relBusObjPageList.size(); i++) {
        Map person = (Map) relBusObjPageList.get(i);
        String leadRoles = (String) rolesByPerson.get(person.get(SELECT_ID));
        leadRoles = UIUtil.isNullOrEmpty(leadRoles) ? EMPTY_STRING :
                                                      getI18NRoleListValue(context, strLanguage, FrameworkUtil.split(leadRoles, "~"));
        v.add(leadRoles);
    }
    return v;
}

protected String getI18NRoleListValue(Context context, String strLanguage, StringList rolesList) throws MatrixException {
    String ROLE_SEPERATOR = ", ";
    int roleSize = rolesList.size();
    switch (roleSize) {
    case 0:
        return EMPTY_STRING;
    default:
        StringBuffer buffer = new StringBuffer();
        int j = 0;
        for (;j < roleSize - 1; j++) {
            buffer.append(getI18NRoleName(context, strLanguage, (String) rolesList.get(j))).append(ROLE_SEPERATOR);
        }
        buffer.append(getI18NRoleName(context, strLanguage, (String) rolesList.get(j)));
        return buffer.toString();
    }
}

/*
 * Added for Cloud UI
 * */

protected String getI18NRoleName(Context context, String strLanguage, String strrole) throws MatrixException {
    strrole = PropertyUtil.getSchemaProperty(context,strrole);
    strrole = i18nNow.getRoleI18NString(strrole.trim() ,strLanguage);
    return strrole;
}

       /**
        * Returns true for Cloud environment and false in non cloud env
        *
        * @param context the eMatrix <code>Context</code> object
        * @param args holds the String arrat containing the Param Map
		*
        * @return boolean - returns true in Cloud env and false in nonCloud env
        * @throws Exception if the operation fails
        * @since  Eng 215
        */
public boolean enableInCloud(Context context, String[] args) throws Exception {
	   try {
		   		return UINavigatorUtil.isCloud(context);

		   	} catch (FrameworkException e) {
		   		throw e;
		   }
}

	/**
	 * Table program for import error tale
	 * @param context
	 * @param args
	 * @return MapList
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList displayImportedData(Context context, String[] args) throws Exception {
	  HashMap programMap = JPO.unpackArgs(args);
	  
	  MapList objectList = (MapList) programMap.get("importDataList"); 
	  
	  return objectList;
	}
	
	/**
	 * Dynamic column definition for import table
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getDynamicColumnforBOMImport(Context context, String[] args) throws Exception {
	  HashMap programMap = JPO.unpackArgs(args);
	  Map requestMap = (Map) programMap.get("requestMap");
	  MapList columnMapList = new MapList();
	  Map columnMappings = (Map) requestMap.get("columnMappings");
	  StringList columns = (StringList)requestMap.get("Columns");
	  
	  String strLanguageStr = context.getLocale().getLanguage();
	   
	  Iterator itr = columns.iterator();
	  while(itr.hasNext()) {
		  Map colMap = new HashMap();
		  Map settingsMap = new HashMap();
		  String sname = (String)itr.next();
		  sname=  (String) (UIUtil.isNotNullAndNotEmpty((String)columnMappings.get(sname))?columnMappings.get(sname):sname);
		  settingsMap.put("Column Type","program");
		  settingsMap.put("program","emxEngineeringUtil");
		  settingsMap.put("function","getImportData");
		  settingsMap.put("Registered Suite","EngineeringCentral");
		  settingsMap.put("Sortable","false");
		  settingsMap.put("Width","60");
		  settingsMap.put("key",sname);
		  String symName = ("TypeStatePolicy".indexOf(sname) > -1) ? sname : "attribute_"+sname.replaceAll(" ", "");
		  
		  if(!"Name".equalsIgnoreCase(sname) && !"Revision".equalsIgnoreCase(sname) && !"State".equalsIgnoreCase(sname)){
			  settingsMap.put("Admin Type",symName); 
		  }
		  
		  settingsMap.put("Style Function","applyStyles");
		  settingsMap.put("Style Program","emxEngineeringUtil");
		  
		  colMap.put("settings",settingsMap);
		  colMap.put("name", sname );
		  colMap.put("label", ("TypeNameRevisionPolicy".indexOf(sname) > -1) ? i18nNow.getBasicI18NString(sname, strLanguageStr) : i18nNow.getAttributeI18NString(sname,strLanguageStr));
		  colMap.put("settings",settingsMap);
		  columnMapList.add(colMap);
	  }
	  
	  return columnMapList;
	}

	/**
	 * Method to provide data for the column
	 * @param context
	 * @param list
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private StringList getDataForThisKey(Context context, MapList list, String key) throws Exception {
		int size = list.size();
		
		if(importColumnMappings == null) {
			importColumnMappings = CacheUtil.getCacheMap(context, "ColumnMappings");;
		}
		
		StringList listReturn = new StringList(size);
		
		String strTemp, orgKey;
		orgKey = key;
		Map map;
		Iterator itr = list.iterator();
		while(itr.hasNext()) {
			map = (Map) itr.next();
			if(!map.containsKey(key)) {
				if(importColumnMappings != null && importColumnMappings.containsKey(key)) {
					key = (String)importColumnMappings.get(key);
				}
			}
			
			strTemp = (String) map.get(key);
			if (UIUtil.isNullOrEmpty(strTemp)) {
				//to support basics
				strTemp = (String) map.get(key.toLowerCase(context.getLocale()));
				strTemp = UIUtil.isNullOrEmpty(strTemp) ? "" : strTemp;
			}
			map.put(orgKey, strTemp);
			listReturn.addElement(strTemp);
		}

		return listReturn;
	}
	
	/**
	 * Column Program for all dynamic columns
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public StringList getImportData(Context context, String args[]) throws Exception {
		  Map programMap = (Map) JPO.unpackArgs(args);
	
	    Map columnMap = (Map)programMap.get("columnMap");
	    Map settings = (Map)columnMap.get("settings");
	    String key = (String)settings.get("key");
		  
	    MapList objList = (MapList)programMap.get("objectList");
	    
	    return getDataForThisKey(context, objList, key);
	}
	
	/**
	 * Method to apply error styles for import data 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public StringList applyStyles(Context context, String args[]) throws Exception {
		Map programMap = (Map) JPO.unpackArgs(args);
	
		StringList listReturn = new StringList();
	    Map columnMap = (Map)programMap.get("columnMap");
	    Map settings = (Map)columnMap.get("settings");
	    String columnName = (String)settings.get("key");
	    
	    MapList objList = (MapList)programMap.get("objectList");
	    Iterator itr = objList.iterator();
	    Map map = null;
	    StringList sl = new StringList();
	    
	    while(itr.hasNext()) {
	    	map = (HashMap)itr.next();
	    	
	    	if(map.containsKey("HighlightColumns")) {
	    		sl = (StringList)map.get("HighlightColumns");
	    		if(sl != null && sl.contains(columnName.toLowerCase(context.getLocale()))) {
    	    		listReturn.addElement("import-error");
	    		} else {
	    			listReturn.addElement("");
	    		}
	    	}
	    }
	    
	    return listReturn;
	}
	
	private String getActualErrorValue(Context context, StringList errorList) throws Exception {
		  String valueOut = "";
		  
		  for (int i = 0; i < errorList.size(); i++) {
			  if (i == 0) {
				  
				  valueOut = "<b>1.</b>" + EnoviaResourceBundle.getProperty(context,  "emxEngineeringCentralStringResource", context.getLocale(), (String) errorList.get(i));
			  } else {
				  valueOut += "<br></br><b>" + (i+1) + ".</b> " + EnoviaResourceBundle.getProperty(context,  "emxEngineeringCentralStringResource", context.getLocale(), (String) errorList.get(i));
			  }
		  }
		  
		  return valueOut;
	}
	
	/**
	 * Method to return list of error msgs for table rendering
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public StringList getErrorDescrption(Context context, String args[]) throws Exception {
		  StringList errorList = new StringList();
		  
		  Map programMap = (Map) JPO.unpackArgs(args);
		  MapList objList = (MapList)programMap.get("objectList");
		  StringList tempErrorList;
		  for (int i = 0; i < objList.size(); i++) {
			  tempErrorList = (StringList) ((Map) objList.get(i)).get("errorDescritionList");
			  
			  if (tempErrorList != null) {
				  errorList.addElement(getActualErrorValue(context, tempErrorList));
			  } else {
				  errorList.addElement("");
			  }
		  }
		  
		  return errorList;
	}
	//access function to hide lifecycle command in RMB when hideLifecycleCommand=true
		public boolean hideLifecycleRMBCommand(Context context, String[] args)
		throws Exception
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String hideLifecycleCommand    = (String) programMap.get("hideLifecycleCommand");
			String frmRMB    = (String) programMap.get("frmRMB");
			boolean result = (("true".equalsIgnoreCase(hideLifecycleCommand))&&("true".equalsIgnoreCase(frmRMB))) ? false : true;
			return result;
		}
		
		public String getI18NStateName(Context context, String[] args) throws Exception {
			String policy = args[0];
			String stateName = args[1];
			String i18NString = stateName;
			
			try {	
				i18NString = ChartUtil.getI18NStateName(context, stateName, policy, context.getLocale());
			} catch ( Exception e) { }
			
			BufferedWriter writer = new BufferedWriter(new MatrixWriter(context));
	        writer.write(i18NString);
	        writer.flush();    
			return i18NString;
		}
		
		/**
		 * Generates dynamic query fields for the Responsible Design Engineer Field for the ECR and ECO Create and Edit Pages 
		 * 
		 * @param context
		 * @param args 
		 * @return String : Field values
		 * @throws Exception
		 */
		public String getRDEDynamicSearchQueryForPUEECO(Context context, String[] args) throws Exception {
		    	

		    HashMap programMap = (HashMap) JPO.unpackArgs(args);
		    //String orgName = EngineeringUtil.getDefaultOrganization(context);
		    HashMap fieldValues = (HashMap)programMap.get("fieldValues");
		    String orgName =  (String)fieldValues.get("RDO");
		    /* Retrieve the Organization and crate a match string based on 
		     * <Role> . <Organization> . <Project> 
		     * Note : All Project are considered therefore the *
		    */

		    String sVPMProjectLeaderRole = PropertyUtil.getSchemaProperty(context,"role_VPLMProjectLeader");
		    String sVPMCreatorRole = PropertyUtil.getSchemaProperty(context,"role_VPLMCreator");
		    String sDesignRole = PropertyUtil.getSchemaProperty(context,"role_DesignEngineer");
		    String sSrDesignRole = PropertyUtil.getSchemaProperty(context,"role_SeniorDesignEngineer");
		    		    
		    /* Better trim the strings as the edit page inserts blank at the end */
		    
		    String MatchVPMStr = sVPMProjectLeaderRole.trim()+"."+orgName.trim()+".*";
		    String MatchVPMCreatorStr = sVPMCreatorRole.trim()+"."+orgName.trim()+".*";
		    String MatchCBPStrde = sDesignRole.trim()+"."+orgName.trim()+".*";
		    String MatchCBPStr = sSrDesignRole.trim()+"."+orgName.trim()+".*";
		     
		    /*Construct field param*/
		    String fields ="ASSIGNED_SECURITY_CONTEXT="+MatchVPMStr+","+MatchCBPStr+","+MatchCBPStrde+","+MatchVPMCreatorStr;
		    
		    return fields;
		}
	
		public String getRMEDynamicSearchQueryForPUEECO(Context context, String[] args) throws Exception {
			
			//String strDefRDOName = EngineeringUtil.getDefaultOrganization(context);	
	  		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
		     HashMap fieldValues = (HashMap)programMap.get("fieldValues");
		     String strDefRDOName =  (String)fieldValues.get("RDO");
			 /* Retrieve the Organization and crate a match string based on 
			  * <Role> . <Organization> . <Project> 
			  * Note : All Project are considered therefore the *
			 */

			 String sMfgEngRole = PropertyUtil.getSchemaProperty(context,"role_ManufacturingEngineer");
			 String sSrMfgRole = PropertyUtil.getSchemaProperty(context,"role_SeniorManufacturingEngineer");
			 String sVPMProjectLeaderRole = PropertyUtil.getSchemaProperty(context,"role_VPLMProjectLeader");
			 String sVPMCreatorRole = PropertyUtil.getSchemaProperty(context,"role_VPLMCreator");
			  
			 /* Better trim the strings as the edit page inserts blank at the end */
			 String MatchVPMStr = sVPMProjectLeaderRole.trim()+"."+strDefRDOName.trim()+".*";
			 String MatchVPMCreatorStr = sVPMCreatorRole.trim()+"."+strDefRDOName.trim()+".*";
			 String strMatchDEStr = sMfgEngRole.trim()+"."+strDefRDOName.trim()+".*";
			 String strMatchSDEStr = sSrMfgRole.trim()+"."+strDefRDOName.trim()+".*";
			  
			 /*Construct field param*/
			 String fields ="ASSIGNED_SECURITY_CONTEXT="+strMatchDEStr+","+strMatchSDEStr+","+MatchVPMStr+","+MatchVPMCreatorStr;
		 
		 return fields;
		}
			//access function to hide Reports menu
		public boolean hideReportsCommand(Context context, String[] args)
		throws Exception
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String hideReportsCommand    = (String) programMap.get("HideReportsCommand");
			return (("true".equalsIgnoreCase(hideReportsCommand))&& UIUtil.isNotNullAndNotEmpty(hideReportsCommand)) ? false : true;
			
		}
		/**
	       * Generates dynamic query for Name field in BOMCompare report
	       * @param context
	       * @param args
	       * @return String
	       * @throws Exception
	       */
	      public String getBOMCompareReportNameQuery(Context context, String[] args) throws Exception {
	          //unpacking the Arguments from variable args
	    	  String fields = "TYPES=type_Part";
	    	  String state = (String)MqlUtil.mqlCommand(context, "print policy $1 select $2 dump", EngineeringConstants.POLICY_CONFIGURED_PART,"state");
	    	  if(state.contains("Superseded")){
	    		  fields = fields+":CURRENT!=policy_ConfiguredPart.state_Superseded";
	    	  }
	          //Modified for RDO Convergence End
	          return fields;
	      }
}
