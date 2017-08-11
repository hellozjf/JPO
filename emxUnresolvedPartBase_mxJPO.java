/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.PolicyItr;
import matrix.db.PolicyList;
import matrix.util.StringList;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MailUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PolicyUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.effectivity.EffectivityFramework;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.unresolvedebom.UnresolvedEBOMConstants;

/**
 * The <code>emxUnresolvedPartBase</code> class contains implementation code for emxUnresolvedPart.
 *
 * @version X+4 - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxUnresolvedPartBase_mxJPO extends emxPart_mxJPO {

	//2011x - Starts
	public static final String OPERARTION_ADD		=	"Add";
	public static final String OPERARTION_CUT		=	"Cut";
	private static final String MARKUP_NEW = "new";
	EffectivityFramework effectivity				= new EffectivityFramework();
	//2011x - Ends

    //TOTO add to constants and fetch from symbolic name
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since X+4
     */
    public emxUnresolvedPartBase_mxJPO(Context context, String[] args) throws Exception{
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return int.
     * @throws Exception if the operation fails.
     * @since EC 9.5.JCI.0.
     */
    public int mxMain(Context context, String[] args)
    throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on emxUnresolvedPart invocation");
        }
        return 0;
    }

/**
 * Returns a StringList of Prerequisite ECOs
 * for a given context.
 * @param context the eMatrix <code>Context</code> object.
 * @param args contains a packed HashMap containing the eco ids which are connected to a product
 * @return StringList.
 * @since EngineeringCentral X3
 * @throws Exception if the operation fails.
*/

public StringList getPrerequisiteECO(Context context,String args[])throws Exception
{
    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList)programMap.get("objectList");
    StringBuffer columnVals = new StringBuffer();
    StringList result = new StringList();
    //if object List not empty then iterate through the object list and get all the ECO ids
    if(objList != null && objList.size()>0)
    {
        Iterator i = objList.iterator();
        while (i.hasNext())
        {
            Map map = (Map) i.next();
            String strId = (String)map.get("id");
            DomainObject domainObjectECO=new DomainObject(strId);

			StringBuffer sbTypePattern = new StringBuffer(UnresolvedEBOMConstants.TYPE_PUE_ECO);

            String relToExpand = PropertyUtil.getSchemaProperty(context,"relationship_Prerequisite");
            StringList selectStmts  = new StringList(1);
            selectStmts.addElement(DomainConstants.SELECT_ID);
            selectStmts.addElement(DomainConstants.SELECT_NAME);
            //get all the prerequisite ECOS
            MapList mapList = domainObjectECO.getRelatedObjects(context,
                                                  relToExpand,                // relationship pattern
                                                  sbTypePattern.toString(),   // object pattern
                                                  selectStmts,                // object selects
                                                  null,                       // relationship selects
                                                  false,                       // to direction
                                                  true,                      // from direction
                                                 (short)1,                   // recursion level
                                                  null,                      // object where clause
                                                  null);
            for(int j=0;j<mapList.size();j++)
            {
                Map change = (Map)mapList.get(j);
                columnVals.append("<a href=\"javascript:showDialog('../common/emxTree.jsp?objectId=");
                columnVals.append(change.get("id"));
                columnVals.append("')\">");
                columnVals.append(change.get("name"));
                columnVals.append("</a>&#160;");
            }
            result.addElement(columnVals.toString());
            columnVals=new StringBuffer();
        }
    }
    return result;
}

/**
The  method that builds the return map for Table and Indented Table
pre processing methods.

@param actionValue     the calling method's Action string (Continue or Stop).

@param messageValue    the calling method's Message string (normally built by
a previous call to the tableBuildMessageString method).

@param objectList      the MapList of objects (normally built by a previous call
to the tableBuildObjectList method).

@return returns to the calling processing method a HashMap that contains the
incoming actionValue, messageValue, and objectList.

@since XBOM
*/

public HashMap tableBuildReturnMap (String actionValue, String messageValue, MapList objectList) throws Exception
{

HashMap returnMap = new HashMap(3);
String actionKey = "Action";
String messageKey = "Message";
String objectListKey = "ObjectList";

returnMap.put(actionKey,actionValue);
returnMap.put(messageKey,messageValue);
returnMap.put(objectListKey,objectList);
return returnMap;

}

/**
This method will check whether the context product has been selected or not.
If user tries to edit any information with out selecting context product then
it will alert the user to select context product

@param args - a packed HashMap containing toolbar related information.

@return returns to the calling processing method a MapList that contains the
'modified' object list.

@since XBOM
*/

@com.matrixone.apps.framework.ui.PreProcessCallable
public HashMap hasProductSelected(Context context, String[] args) throws Exception
{
    HashMap inputMap = (HashMap)JPO.unpackArgs(args);
    HashMap requestMap = (HashMap) inputMap.get("requestMap");
    HashMap tableData = (HashMap) inputMap.get("tableData");
    MapList objectList = (MapList) tableData.get("ObjectList");
    String actionValue = "continue";
    Locale Local = context.getLocale();
    //------ Fix for BUG - 359207 starts ----
    //Desc: Change view value in reequest map is in English, hence compare the same with english string.
    String sChangeViewCurrent = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",new Locale("en"),"emxUnresolvedEBOM.BOMPowerView.ChangeView.Current");
    //------ Fix Ends.. --------
    String changeViewMessageValue =EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",Local,"emxUnresolvedEBOM.ChangeViewFilter.AlertMessage");
    HashMap returnMap = new HashMap();
    //if Product filter value is empty then alert the user
	if(sChangeViewCurrent.equals(requestMap.get("PUEUEBOMChangeViewFilter")))
    {
        actionValue="Stop";
        returnMap = (HashMap) tableBuildReturnMap (actionValue,changeViewMessageValue,objectList);
    }

return returnMap;

}


    /**
     * Method to reformat MapList - add extra UEBOM columns
     * @param Map
     * @param String
     * @param String
     * @param String
     * @param String
     * @return Map
     */
    public Map reformatMap(Map map, String applicability,String effectivity, String add, String remove, String operation)
    {
        map.put("Applicability",applicability);
        map.put("Effectivity",effectivity);
        map.put("Add",add);
        map.put("Remove",remove);
        map.put("Operation",operation);

        /*// Do not allow edit for removed columns or resolved bom
        if (!"".equals(remove) || ("".equals(add)) && "".equals(remove)) {
            map.put("RowEditable", "readonly");
        }else{
            map.put("RowEditable", "show");
        }*/
        return map;
    }

   /**
    * Method to check if part is a top level part.
    * @param context
    * @param String[]
    * @return boolean
    */
   public boolean checkForTopLevelPart(Context context,String objectID)
   throws Exception
   {
   boolean chkTopLevelPart = false;
   String  parentObjectId = objectID;
   DomainObject domObj = new DomainObject(parentObjectId);
   MapList mapList = domObj.getRelatedObjects(context,
           UnresolvedEBOMConstants.RELATIONSHIP_ASSIGNED_PART,
           UnresolvedEBOMConstants.TYPE_PRODUCTS,
                                              null,
                                              null,
                                              true,
                                              false,
                                              (short) 1,
                                              null,
                                              null);

   if(mapList.size()>0){
       chkTopLevelPart = true;
   }
   else{
       chkTopLevelPart = false;
   }
   return chkTopLevelPart;
}


  /**
   * Returns a StringList of the parent object ids and select Object Id which are connected using UEBOM Relationship
   * for a given context.
   * @param context the eMatrix <code>Context</code> object.
   * @param args contains a packed HashMap containing objectId of object
   * @return StringList.
 * @since EngineeringCentral X3
   * @throws Exception if the operation fails.
  */
  @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
  public StringList excludeRecursiveOIDAddExisting(Context context, String args[])    throws Exception
  {
    HashMap programMap = (HashMap) JPO.unpackArgs(args);
      String selPartObjectId = (String) programMap.get("selPartObjectId");
      StringList result = new StringList();
      if (selPartObjectId == null) {
          return (result);
      }
      DomainObject domObj = new DomainObject(selPartObjectId);
      String strTypePart = PropertyUtil.getSchemaProperty(context,"type_Part");
      StringBuffer sbTypePattern = new StringBuffer(strTypePart);
      //Start for the IR-072947V6R2011x
      //String relToExpand = PropertyUtil.getSchemaProperty(context,"relationship_EBOMPending");
      StringBuffer sbRelPattern = new StringBuffer();
      sbRelPattern.append(DomainConstants.RELATIONSHIP_EBOM).append(',').append(UnresolvedEBOMConstants.RELATIONSHIP_EBOM_PENDING);
      //End for the IR-072947V6R2011x
      StringList selectStmts = new StringList(1);
      selectStmts.addElement(DomainConstants.SELECT_ID);
      MapList mapList = domObj.getRelatedObjects(context, sbRelPattern.toString(), // relationship pattern
              sbTypePattern.toString(), // object pattern
              selectStmts, // object selects
              null, // relationship selects
              true, // to direction
              false, // from direction
              (short) 0, // recursion level
              null, // object where clause
              null); // relationship where clause

      Iterator i1 = mapList.iterator();
      while (i1.hasNext()) {
          Map m1 = (Map) i1.next();
          String strId = (String) m1.get(DomainConstants.SELECT_ID);
          result.addElement(strId);
      }

      result.add(selPartObjectId);

      return result;
}


  /**
   * This method is check if the Object is Reserved
   * @param context the eMatrix <code>Context</code> object
   * @param strObjId : objectId to check for Reserved
   * @return boolean : return the boolean based on Reserve
   * @throws FrameworkException  if the operation fails
   * @author Praveen Voggu
   * @since X3 HF0.6
   */
 public static boolean isConnectionReserved(Context context, String strRelId) throws FrameworkException
 {
     String mqlCommand ="";
     String strReserved = "";
     if(strRelId != null){
     mqlCommand = "print connection $1 select $2";
     strReserved = MqlUtil.mqlCommand(context, mqlCommand,strRelId,"reserved");
     }

     boolean isConnReserved = false;

     if(strReserved.indexOf("TRUE") != -1)
     {
         isConnReserved = true;
     }
     return isConnReserved;

 }

 /**
      * @param context
      * @param args
      * @return true/false
 * @throws Exception
      * @throws Exception
      */
     public boolean sendMailForBGProcess(Context context,String objectId,boolean success) throws Exception{

         String loggedInUser = com.matrixone.apps.common.Person.getPerson(context).getName(context);
         String strSubject="";
         String strBody = "";
         StringList toList = new StringList(loggedInUser);
         Locale Local = context.getLocale();
         if(success){
        	 strSubject = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",Local,"emxUnresolvedEBOM.Notification.Subject");
        	 strBody = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",Local,"emxUnresolvedEBOM.Notification.Body");
         }
         else{
        	 strSubject = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOMStringResource",Local,"emxUnresolvedEBOM.FailNotification.Subject");
         }
         // Send notification
         MailUtil.sendMessage(context,
                            toList,//toList
                            null,//ccList
                            null,//bccList
                            strSubject,//subject
                            strBody,//message
                            new StringList(objectId));//objectIdList


         return true;
    }
 //Added for UEBOM Copy From operation-Ends

     /**
      * Returns a MapList of the ECO ids when ECO filter is selected
      * for a given context.
      * @param context the eMatrix <code>Context</code> object.
      * @param args contains a packed HashMap containing objectId of object
      * @return MapList.
      * @since  X+4.
      * @throws Exception if the operation fails.
     */
     public MapList getContextChangeForECO(Context context, String args[])throws Exception
     {
         MapList ecoList=new MapList();
         StringList finalECOs=new StringList();
         HashMap paramMap = (HashMap) JPO.unpackArgs(args);
         String  stringECOObjectId =(String)paramMap.get("cmdECOFilterInfo");
         finalECOs = FrameworkUtil.split(stringECOObjectId, ",");
         for(int i=1;i<=finalECOs.size();i++) {
            Map map = new HashMap();
            map.put("id",finalECOs.get(i-1));
            ecoList.add(map);
         }
         ecoList.add(0,new Integer(finalECOs.size()+1));
         return ecoList;
     }

     /**
     * Method to check if BOMMode is Resolved for Assign Top Level Part command.
     *
     * @param context
     * @param String[]
     * @return boolean
     */

    public boolean checkForBOMMode(Context context, String args[])
            throws Exception {
        return true;
    }

    /**
     * update UEBOM fields
     */
    public Boolean updatePUEFields(Context context, String[] args) throws Exception {
        return Boolean.TRUE;
    }
    //Added for X7 - Starts
    /**
    * Method to include parts which satisy the given condition
    * @param context
    * @param String[]
    * @return StringList
    */
    @com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
    public StringList includeUnresolvedParts(Context context, String[] args) throws Exception {
        HashMap programMap 				= (HashMap) JPO.unpackArgs(args);
        String productId 				= (String) programMap.get("objectId");
        StringList slUnresolvedParts 	= new StringList();
        DomainObject doObject 			= DomainObject.newInstance(context);
        doObject.setId(productId);
        String physicalId				= doObject.getInfo(context, "physicalid");
        StringList slBusSelectList 		= new StringList(1);
        slBusSelectList.add(UnresolvedEBOMConstants.SELECT_ID);
        String STATE_CONPART_SUPERSEDED = PropertyUtil.getSchemaProperty(context,"policy",
                                           UnresolvedEBOMConstants.POLICY_CONFIGURED_PART,
                                           "state_Superseded");
        String sVault = context.getVault().toString();
        StringBuffer sbWhere = new StringBuffer(128);
        sbWhere.append('(').append("policy == \"").append(UnresolvedEBOMConstants.POLICY_CONFIGURED_PART)
            .append("\" && ").append(DomainConstants.SELECT_REVISION).append(" == \"").append("last\"")
            .append(" && relationship[").append(UnresolvedEBOMConstants.RELATIONSHIP_ASSIGNED_PART)
            .append("] == \"False\" && ").append("to[").append(UnresolvedEBOMConstants.RELATIONSHIP_EBOM_PENDING)
            .append("] == \"False\" && ").append("to[").append(UnresolvedEBOMConstants.RELATIONSHIP_EBOM)
            .append("] == \"False\" && ").append(UnresolvedEBOMConstants.SELECT_CURRENT).append(" != \"")
            .append(STATE_CONPART_SUPERSEDED).append("\")");
        MapList mlUnresolvedParts = DomainObject.findObjects(context, UnresolvedEBOMConstants.TYPE_PART,
                                        						sVault, sbWhere.toString(), slBusSelectList);
        if (mlUnresolvedParts != null && !mlUnresolvedParts.isEmpty()) {
            Iterator itrUnresolvedParts = mlUnresolvedParts.iterator();
            while (itrUnresolvedParts.hasNext()) {
                Map mapParts = (Map) itrUnresolvedParts.next();
                String sUnresolvedPartId = (String) mapParts.get(DomainConstants.SELECT_ID);
                doObject.setId(sUnresolvedPartId);
                StringList slConnectionIds	=	doObject.getInfoList(context, "from["+DomainConstants.RELATIONSHIP_EBOM+"].id");
                slConnectionIds.addAll(doObject.getInfoList(context, "from["+UnresolvedEBOMConstants.RELATIONSHIP_EBOM_PENDING+"].id"));
                if (slConnectionIds != null && !slConnectionIds.isEmpty()) {
                	Iterator itrRelIds		=	slConnectionIds.iterator();
                	while (itrRelIds.hasNext()) {
                		String sRelId				= (String) itrRelIds.next();
                		StringList slProductList 	= effectivity.getRelEffectivityUsage(context, sRelId);
                		if (slProductList.contains(physicalId) && slProductList.size() == 1) {
                			slUnresolvedParts.add(sUnresolvedPartId);
                		}
                	}
            	} else {
            		slUnresolvedParts.add(sUnresolvedPartId);
            	}

            }
        }
        return slUnresolvedParts;
    }

/**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a packed hashmap with the following arguments
     *        paramMap HashMap of parameter values - fieldMap
     *        requestMap HashMap of request values - objectId, mode, form
     *
     * @throws Exception if the operation fails
     * @since CFF R210 for ECC Reports
     */
   public String getEffectivityExpressionDisplayBOM1(Context context, String[] args)
        throws Exception

   {
	   HashMap programMap = (HashMap)JPO.unpackArgs(args);
	   HashMap requestMap= (HashMap)programMap.get("requestMap");
	   String objectId = (String)requestMap.get("objectId");
	   double timezone = (new Double((String)requestMap.get("timeZone"))).doubleValue();
	   String mode = (String)requestMap.get("mode");
	   String formName = (String)requestMap.get("form");
	   EffectivityFramework EFF = new EffectivityFramework();
	   StringBuffer sb = new StringBuffer(100);
	   String actualValue = "";
	   String displayValue = "";
	   String effTypes1 = "";
	   Map mapExpression = null;
	   //TODO remove temp code of hardcoded form name - should be passed in by BPS edit form
	   formName = "editDataForm";
	   StringList listValue = new StringList();
	   StringList listValueActual = new StringList();
	   StringBuffer sbListValue = new StringBuffer(32);

	   MapList mlObjectExpression = EFF.getObjectExpression(context, objectId, timezone, true);
	   mapExpression = (Map)mlObjectExpression.get(0);
	   actualValue = (String)mapExpression.get(EffectivityFramework.ACTUAL_VALUE);
	   displayValue = (String)mapExpression.get(EffectivityFramework.DISPLAY_VALUE);
	   listValue = (StringList)mapExpression.get("listValue");
	   for(int i=0;i<listValue.size();i++)
	   {
	     sbListValue.append(listValue.get(i));
	     sbListValue.append("@delimitter@");
	   }
	   String strListValue = sbListValue.toString();
	   sbListValue.delete(0, sbListValue.length());
	   listValueActual = (StringList)mapExpression.get("listValueActual");
	   for(int i=0;i<listValueActual.size();i++)
	   {
	     sbListValue.append(listValueActual.get(i));
	     sbListValue.append("@delimitter@");
	   }
	   String quoteSeparatedIds = strListValue.substring(0, strListValue.length());
	   String strListValueAc = sbListValue.toString();
	   String quoteSeparatedIdsAc = strListValueAc.substring(0, strListValueAc.length());
	   HashMap effectivityFrameworkMap = new HashMap();
	   HashMap effTypes = new HashMap();
	   effTypes.put(EffectivityFramework.DISPLAY_VALUE, displayValue);
	   effTypes.put(EffectivityFramework.ACTUAL_VALUE, actualValue);
	   effectivityFrameworkMap.put("effTypes", effTypes);
	   HashMap effExpr = new HashMap();
	   effExpr.put(EffectivityFramework.DISPLAY_VALUE, displayValue);
	   effExpr.put(EffectivityFramework.ACTUAL_VALUE, actualValue);
	   effectivityFrameworkMap.put("effExpr", effExpr);

	    String editEffectivityURL = "../effectivity/EffectivityDefinitionDialog.jsp?modetype=filter&invockedFrom=fromForm&formName="+formName+"&parentOID=&" +
		"fieldNameEffExprDisplay=EffectivityExpression&" +
		"fieldNameEffExprActual=EffectivityExpressionActual&" +
		"fieldNameEffExprActualList=EffectivityExpressionOIDList&" +
		"fieldNameEffExprActualListAc=EffectivityExpressionOIDListAc&" +
		"fieldNameEffExprOID=EffectivityExpressionOID";
	    editEffectivityURL+="&objectId=";
	    editEffectivityURL+=objectId;
	    
	    if(UIUtil.isNullOrEmpty(objectId)) {
		    effTypes1 = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgt.Effectivity.EffectivityTypes");
	    	editEffectivityURL+="&fieldNameEffTypes=effTypes1";
	    }
	    DomainObject doPart;
	   if (mode != null && mode.equalsIgnoreCase("edit"))
	   {
		   String policyClassification = "policy.property[PolicyClassification].value";
		   String SELECT_PART_POLICYCLASS = "from["+EngineeringConstants.RELATIONSHIP_PART_REVISION+"].to.policy.property[PolicyClassification].value";

		   StringList objectSelect = new StringList(3);
		   objectSelect.addElement(policyClassification);
		   objectSelect.addElement(DomainConstants.SELECT_TYPE);
		   objectSelect.addElement(SELECT_PART_POLICYCLASS);

		   if(!UIUtil.isNullOrEmpty(objectId)) {
		   doPart = new DomainObject(objectId);
		   Map dataMap = doPart.getInfo(context, objectSelect);
		   String type = (String) dataMap.get(DomainConstants.SELECT_TYPE);
		   if( EngineeringConstants.TYPE_PART_MASTER.equals(type))
		   {
			   policyClassification = (String) dataMap.get(SELECT_PART_POLICYCLASS);
		   }
		   else
		   {
		       policyClassification = (String) dataMap.get(policyClassification);
		   }
		   }
	       sb.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
	       sb.append(" <script src='../emxUIPageUtility.js'> </script> ");
	       sb.append("<script> ");
	       sb.append("function showEffectivityExpressionDialog() { ");
	       sb.append(" emxShowModalDialog(\"");
	       sb.append(XSSUtil.encodeForJavaScript(context, editEffectivityURL));
	       sb.append("\",700,500);");
	       sb.append('}');
	       sb.append("</script>");
			   if (policyClassification.equals("Unresolved") || null == objectId || "null".equals(objectId)) {
				   sb.append("<div id=\"editeffectivity1\"style=\"visibility: visible;\">");
			   } else {
				   sb.append("<div id=\"editeffectivity1\" style=\"visibility: hidden;\">");
			   }
	       sb.append("<input type=\"text\" name=\"EffectivityExpression\" size=\"20");
	       sb.append("\" readonly=\"readonly\" >");
	       sb.append(XSSUtil.encodeForHTML(context, displayValue));
	       sb.append("</textarea>");
	       sb.append("<a href=\"javascript:showEffectivityExpressionDialog()\">");
	       sb.append("<img src=\"../common/images/iconActionEdit.gif\" border=\"0\"/></a>");
	       sb.append("&nbsp<a href=\"javascript:basicClear('EffectivityExpression');basicClear('EffectivityExpressionActual');basicClear('EffectivityExpressionOIDList');basicClear('EffectivityExpressionOIDListAc');basicClear('EffectivityExpressionOID') \">").append(EngineeringUtil.i18nStringNow(context, "emxEngineeringCentral.Common.Clear", context.getSession().getLanguage())).append("</a>"); 
	       		sb.append("</div>");
	       sb.append("<input type=\"hidden\" name=\"EffectivityExpressionActual\" value=\"");
	       sb.append(XSSUtil.encodeForHTMLAttribute(context,actualValue));
	       sb.append("\" />");
	       sb.append("<input type=\"hidden\" name=\"EffectivityExpressionOIDList\" value=\"");
	       sb.append(XSSUtil.encodeForHTMLAttribute(context,quoteSeparatedIds));
	       sb.append("\" />");
	       sb.append("<input type=\"hidden\" name=\"EffectivityExpressionOIDListAc\" value=\"");
	       sb.append(XSSUtil.encodeForHTMLAttribute(context,quoteSeparatedIdsAc));
	       sb.append("\" />");
	       sb.append("<input type=\"hidden\" name=\"EffectivityExpressionOID\" value=\"");
	       sb.append(XSSUtil.encodeForHTMLAttribute(context,actualValue));
	       sb.append("\" />");
	      if(UIUtil.isNullOrEmpty(objectId)) {
	    	Map effType = null;
	  		MapList mlEffectivityTypes = EFF.getEffectivityTypeData(context, effTypes1);
	      if (!mlEffectivityTypes.isEmpty())
	       {
		   for (int i=0; i < mlEffectivityTypes.size(); i++)
		   {
		       effType = (Map)mlEffectivityTypes.get(i);
		       actualValue = (String)effType.get(EffectivityFramework.ACTUAL_VALUE);
		       displayValue = (String)effType.get(EffectivityFramework.DISPLAY_VALUE);
		       sb.append("<input type=\"checkbox\" name=\"effTypes1\" style=\"display:none;\" checked=\"checked\" value=\"" );
		       sb.append(XSSUtil.encodeForHTMLAttribute(context, actualValue));
		       sb.append("@displayactual@");
		       //XSS OK
		       sb.append(displayValue);
		       sb.append("\"/>");
		   }
	       }
	       }
	   }
	   else //view mode only display expression
	   {
	       sb.append(XSSUtil.encodeForHTML(context,displayValue));
	   }

	   return sb.toString();
	}

        /**
	     *
	     * @param context the eMatrix <code>Context</code> object
	     * @param args holds a packed hashmap with the following arguments
	     *        paramMap HashMap of parameter values - fieldMap
	     *        requestMap HashMap of request values - objectId, mode, form
	     *
	     * @throws Exception if the operation fails
             * @since CFF R210 for ECC Reports
	     */
	    public String getEffectivityExpressionDisplayBOM2(Context context, String[] args)
	        throws Exception
	    {
	    	 HashMap programMap = (HashMap)JPO.unpackArgs(args);
	  	   HashMap requestMap= (HashMap)programMap.get("requestMap");
	  	   String objectId = (String)requestMap.get("objectId2");
	  	   double timezone = (new Double((String)requestMap.get("timeZone"))).doubleValue();
	  	   String mode = (String)requestMap.get("mode");
	  	   String formName = (String)requestMap.get("form");
	  	   EffectivityFramework EFF = new EffectivityFramework();
	  	   StringBuffer sb = new StringBuffer(128);
	  	   String actualValue = "";
	  	   String displayValue = "";
	  	   String effTypes2 = "";
	  	   Map mapExpression = null;
	  	   //TODO remove temp code of hardcoded form name - should be passed in by BPS edit form
	  	   formName = "editDataForm";
	  	   StringList listValue = new StringList();
	  	   StringList listValueActual = new StringList();
	  	   StringBuffer sbListValue = new StringBuffer(128);

	  	   MapList mlObjectExpression = EFF.getObjectExpression(context, objectId, timezone, true);
	  	   mapExpression = (Map)mlObjectExpression.get(0);
	  	   actualValue = (String)mapExpression.get(EffectivityFramework.ACTUAL_VALUE);
	  	   displayValue = (String)mapExpression.get(EffectivityFramework.DISPLAY_VALUE);
	  	   listValue = (StringList)mapExpression.get("listValue");
	  	   for(int i=0;i<listValue.size();i++)
	  	   {
	  	     sbListValue.append(listValue.get(i));
	  	     sbListValue.append("@delimitter@");
	  	   }
	  	   String strListValue = sbListValue.toString();
	  	   sbListValue.delete(0, sbListValue.length());
	  	   listValueActual = (StringList)mapExpression.get("listValueActual");
	  	   for(int i=0;i<listValueActual.size();i++)
	  	   {
	  	     sbListValue.append(listValueActual.get(i));
	  	     sbListValue.append("@delimitter@");
	  	   }
	  	   String quoteSeparatedIds = strListValue.substring(0, strListValue.length());
	  	   String strListValueAc = sbListValue.toString();
	  	   String quoteSeparatedIdsAc = strListValueAc.substring(0, strListValueAc.length());
	  	   HashMap effectivityFrameworkMap = new HashMap();
	  	   HashMap effTypes = new HashMap();
	  	   effTypes.put(EffectivityFramework.DISPLAY_VALUE, displayValue);
	  	   effTypes.put(EffectivityFramework.ACTUAL_VALUE, actualValue);
	  	   effectivityFrameworkMap.put("effTypes", effTypes);
	  	   HashMap effExpr = new HashMap();
	  	   effExpr.put(EffectivityFramework.DISPLAY_VALUE, displayValue);
	  	   effExpr.put(EffectivityFramework.ACTUAL_VALUE, actualValue);
	  	   effectivityFrameworkMap.put("effExpr", effExpr);

	  	   String editEffectivityURL2 = "../effectivity/EffectivityDefinitionDialog.jsp?modetype=filter&invockedFrom=fromForm&formName="+formName+"&parentOID=&" +
	   		"fieldNameEffExprDisplay=EffectivityExpression1&" +
	     		"fieldNameEffExprActual=EffectivityExpressionActual1&" +
	     		"fieldNameEffExprActualList=EffectivityExpressionOIDList1&" +
	     		"fieldNameEffExprActualListAc=EffectivityExpressionOIDListAc1&" +
	    		"fieldNameEffExprOID=EffectivityExpressionOID1";
	  	    editEffectivityURL2+="&objectId=";
	  	    editEffectivityURL2+= (UIUtil.isNullOrEmpty(objectId) ? (String) requestMap.get("objectId") : objectId);
            
	  	    if(UIUtil.isNullOrEmpty(objectId)) {
			    effTypes2 = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgt.Effectivity.EffectivityTypes");
		    	editEffectivityURL2+="&fieldNameEffTypes=effTypes2";
		    }
	  	   if (mode != null && mode.equalsIgnoreCase("edit"))
	  	   {

	  		   String policyClassification = "policy.property[PolicyClassification].value";
	  	       String SELECT_PART_POLICYCLASS = "from["+EngineeringConstants.RELATIONSHIP_PART_REVISION+"].to.policy.property[PolicyClassification].value";

	  	       StringList objectSelect = new StringList(3);
	  	       objectSelect.addElement(policyClassification);
	  	       objectSelect.addElement(DomainConstants.SELECT_TYPE);
	  	       objectSelect.addElement(SELECT_PART_POLICYCLASS);

	  		   if(!UIUtil.isNullOrEmpty(objectId)) {
	  			 DomainObject doPart = new DomainObject(objectId);
	  		   Map dataMap = doPart.getInfo(context, objectSelect);
	  		   String type = (String) dataMap.get(DomainConstants.SELECT_TYPE);
	  		   if( EngineeringConstants.TYPE_PART_MASTER.equals(type))
	  		   {
	  			   policyClassification = (String) dataMap.get(SELECT_PART_POLICYCLASS);
	  		   }
	  		   else
	  		   {
	  		       policyClassification = (String) dataMap.get(policyClassification);
	  		   }
	  		   }
	  	       sb.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
	  	       sb.append(" <script src='../emxUIPageUtility.js'> </script> ");
	  	       sb.append("<script> ");
	  	       sb.append("function showEffectivityExpressionDialog2() { ");
	  	       sb.append(" emxShowModalDialog(\"");
	  	       sb.append(XSSUtil.encodeForJavaScript(context, editEffectivityURL2));
	  	       sb.append("\",700,500);");
	  	       sb.append('}');
	  	       sb.append("</script>");
	  		   if (policyClassification.equals("Unresolved") || null == objectId || "null".equals(objectId)) {
	  			   sb.append("<div id=\"editeffectivity\"style=\"visibility: visible;\">");
	  		   } else {
	  			   sb.append("<div id=\"editeffectivity\" style=\"visibility: hidden;\">");
	  		   }
	  			   sb.append("<input type=\"text\" name=\"EffectivityExpression1\" size=\"20");
	  			   sb.append("\" readonly=\"readonly\" >");
	  			   sb.append(XSSUtil.encodeForHTML(context, displayValue));
	  			   sb.append("</textarea>");

	  			   sb.append("<a href=\"javascript:showEffectivityExpressionDialog2()\">");
	  			   sb.append("<img src=\"../common/images/iconActionEdit.gif\"  border=\"0\"/></a>");
	  			   sb.append("&nbsp<a href=\"javascript:basicClear('EffectivityExpression1');basicClear('EffectivityExpressionActual1');basicClear('EffectivityExpressionOIDList1');basicClear('EffectivityExpressionOIDListAc1');basicClear('EffectivityExpressionOID1') \">").append(EngineeringUtil.i18nStringNow(context, "emxEngineeringCentral.Common.Clear", context.getSession().getLanguage())).append("</a>");
	  			   sb.append("</div>");
	  		   sb.append("<input type=\"hidden\" name=\"EffectivityExpressionActual1\" value=\"");
	  		   sb.append(XSSUtil.encodeForHTMLAttribute(context,actualValue));
	  	       sb.append("\" />");
	  	       sb.append("<input type=\"hidden\" name=\"EffectivityExpressionOIDList1\" value=\"");
	  	       sb.append(XSSUtil.encodeForHTMLAttribute(context,quoteSeparatedIds));
	  	       sb.append("\" />");
	  	       sb.append("<input type=\"hidden\" name=\"EffectivityExpressionOIDListAc1\" value=\"");
	  	       sb.append(XSSUtil.encodeForHTMLAttribute(context,quoteSeparatedIdsAc));
	  	       sb.append("\" />");
	  	       sb.append("<input type=\"hidden\" name=\"EffectivityExpressionOID1\" value=\"");
	  	       sb.append(XSSUtil.encodeForHTMLAttribute(context,actualValue));
	  	       sb.append("\" />");
	  	       if(UIUtil.isNullOrEmpty(objectId)) {
	  	    	Map effType = null;
	 			MapList mlEffectivityTypes = EFF.getEffectivityTypeData(context, effTypes2);
	  	       if (!mlEffectivityTypes.isEmpty())
		       {
			   for (int i=0; i < mlEffectivityTypes.size(); i++)
			   {
			       effType = (Map)mlEffectivityTypes.get(i);
			       actualValue = (String)effType.get(EffectivityFramework.ACTUAL_VALUE);
			       displayValue = (String)effType.get(EffectivityFramework.DISPLAY_VALUE);
			       sb.append("<input type=\"checkbox\" name=\"effTypes2\" style=\"display:none;\" checked=\"checked\" value=\"" );
			       sb.append(XSSUtil.encodeForHTMLAttribute(context, actualValue));
			       sb.append("@displayactual@");
			       //XSS OK
			       sb.append(displayValue);
			       sb.append("\"/>");
			   }
		       }
	  	       }
	  	   }
	  	   else //view mode only display expression
	  	   {
	  	       sb.append(XSSUtil.encodeForHTML(context, displayValue));
	  	   }

	  	   return sb.toString();
}

   /**
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a packed hashmap with the following arguments
     *        paramMap String of parameter values - args
     *
     * @throws Exception if the operation fails
     * @since CFF R210 for ECC Reports
     */

    public Vector getProposedEffectivity(Context context, String[]args)
        throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
	MapList objectList = (MapList)programMap.get("objectList");
	HashMap params = (HashMap)programMap.get("paramList");
	double timezone = (new Double((String)params.get("timeZone"))).doubleValue();
        Vector exprVector = new Vector(objectList.size());

	EffectivityFramework ef = new EffectivityFramework();
	String displayValue = "";

	MapList expressionMap =  ef.getRelExpression(context, objectList, timezone, true);
	for (int idx = 0; idx < expressionMap.size(); idx++)
	{
	    Map exprMap = (Map)expressionMap.get(idx);
	    displayValue = (String)exprMap.get(EffectivityFramework.DISPLAY_VALUE);
            exprVector.addElement(displayValue);
	}
	return exprVector;
    }

   /**
    *
    * Access method to display Assign to PUEECO menu based on WIPBOM mode property entry.
    * @author YOQ
    * @param context the eMatrix code context object
    * @param String[] packed hashMap of request parameters
    * @throws Exception if the operation fails
    * @return boolean true if the WIPBOM entry is true else false to hide.
    *
    */

   public boolean isWipBomAllowed(Context context, String args[])
           throws Exception {
       boolean isWipBomAllowed = false;
       //String  isWipBomAllowd  = FrameworkProperties.getProperty("emxUnresolvedEBOM.WIPBOM.Allowed");
       //String  isWipBomAllowd  = EnoviaResourceBundle.getProperty(context,"emxUnresolvedEBOM.WIPBOM.Allowed");
       HashMap paramMap = JPO.unpackArgs(args);
       String contextObjectId = (String) paramMap.get("objectId");
       String isWipBomAllowd = isWipBomAllowedForParts(context,contextObjectId);

       if (isWipBomAllowd.equalsIgnoreCase("true")) {
    	   isWipBomAllowed = true;
       }

       return isWipBomAllowed;
   }
   /**
    *
    * Method to get symbolic names of policies and their 1st revision .
    * @param context the eMatrix code context object
    * @param String[] packed hashMap of request parameters
    * @throws Exception if the operation fails
    * @return MapList.
    *
    */
	public MapList getPolicyRevision(Context context, String[] args) throws Exception {

		HashMap hmPolicyRev = new HashMap();
		MapList mPolicyName = new MapList();
		MapList mlResult = new MapList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String typeString = (String) programMap.get("type");

		try {
			BusinessType partBusinessType = new BusinessType(typeString, context.getVault());

			PolicyList allPartPolicyList = partBusinessType
					.getPoliciesForPerson(context, false);
			PolicyItr partPolicyItr = new PolicyItr(allPartPolicyList);
			Policy policyValue = null;
			String policyName = "";
			String symbolicName = "";
			String sRev;

				while (partPolicyItr.next()) {
					policyValue = (Policy) partPolicyItr.obj();
					policyName = policyValue.getName();
					sRev = policyValue.getFirstInSequence(context);
					symbolicName = PropertyUtil.getAliasForAdmin(context, "policy", policyName, true);
					hmPolicyRev.put(symbolicName, sRev);
					mPolicyName.add(symbolicName);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		mlResult.add(hmPolicyRev);
		mlResult.add(mPolicyName);

		return mlResult;
	}

	/* This method is used to display default Part policy value - 16x-UI Enhancement*/
	public HashMap getDefaultPartPolicyValue(Context context, String[] args) throws Exception
	{
		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
		HashMap requestMap = (HashMap)paramMap.get("requestMap");
		String languageStr = (String) requestMap.get("languageStr");

		HashMap defaultMap = new HashMap();
		String defaultVal= PropertyUtil.getSchemaProperty(context,"policy_ConfiguredPart");
		String strType = com.matrixone.apps.framework.ui.UINavigatorUtil.getAdminI18NString("Policy", defaultVal, languageStr);
		defaultMap.put("Default_AddNewRow",defaultVal);
		defaultMap.put("Default_AddNewRow_Display",strType);
		return defaultMap;
	}


   /* Range function to display policies in dropdown box */
   public HashMap getPolicies (Context context, String[] args) throws Exception {

	 HashMap paramMap   = (HashMap)JPO.unpackArgs(args);
	 HashMap requestMap = (HashMap)paramMap.get("requestMap");
	 String parentid    = (String)requestMap.get("objectId");

	 String parentType         = new DomainObject(parentid).getInfo(context, DomainConstants.SELECT_TYPE);
	 BusinessType partBusType  = new BusinessType(parentType, context.getVault());
	 PolicyList partPolicyList = partBusType.getPoliciesForPerson(context,false);
	 PolicyItr  partPolicyItr  = new PolicyItr(partPolicyList);
	 Locale Local = context.getLocale();
	 boolean isMBOMInstalled = EngineeringUtil.isMBOMInstalled(context);
	 String POLICY_STANDARD_PART = PropertyUtil.getSchemaProperty(context,"policy_StandardPart");
	 Policy partPolicy = null;
	 String policyName = "";
	 String policyAdminName = "";
	 String policyClassification = "";

	 HashMap rangeMap = new HashMap();
	 StringList columnVals = new StringList();
	 StringList columnVals_Choices = new StringList();

	 while(partPolicyItr.next())
	 {
		partPolicy = partPolicyItr.obj();
		policyName = partPolicy.getName();
		policyClassification = EngineeringUtil.getPolicyClassification(context, policyName);

		if(!isMBOMInstalled)
        {
        	if(policyName.equals(POLICY_STANDARD_PART))
        	{
        		continue;
        	}
        }
		if("Equivalent".equals(policyClassification) || "Manufacturing".equals(policyClassification))
		{
			continue;
		}
		policyAdminName = FrameworkUtil.getAliasForAdmin(context, "Policy", policyName, true);
		String tempPolicyName = replaceFirst(policyName.trim()," ", "_");
        //columnVals.add(UINavigatorUtil.getI18nString("emxFramework.Policy."+tempPolicyName, "emxFrameworkStringResource", languageStr));
		columnVals.add(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", Local,"emxFramework.Policy."+tempPolicyName));
		columnVals_Choices.add(policyAdminName);
	 }
	 rangeMap.put("field_choices",columnVals_Choices);
	 rangeMap.put("field_display_choices", columnVals );

	 return rangeMap;
  }
   private boolean isNullOrEmpty(String testString)
	{
		return testString == null || testString.trim().length() == 0 || "null".equalsIgnoreCase(testString.trim());
	}

  /**
   * Trigger method to check if the VPLM product is in Released state.
   * Part promotion is possible if product is in Released or Obsolete state, if VPM Visible = true.
   * @param context
   * @param args
   * @return
   * @throws Exception
   */
  public int checkVPMProductInReleasedState(Context context, String []args)  throws Exception {
      String partId = args[0];
      String targetState = PropertyUtil.getSchemaProperty(context, "policy",
    		  PropertyUtil.getSchemaProperty(context, "policy_VPLM_SMB") , args[1]);
      DomainObject partObj = DomainObject.newInstance(context, partId);
      String vplmVisible = partObj.getInfo(context, "attribute["+EngineeringConstants.ATTRIBUTE_VPM_VISIBLE+"].value");

      if("true".equalsIgnoreCase(vplmVisible)) {
          String productId = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",partId,"from["+RELATIONSHIP_PART_SPECIFICATION+"|to.type.kindof["+EngineeringConstants.TYPE_VPLM_CORE_REF+"]]" + ".to.id");

          if(productId != null && !"".equals(productId)){
              if (!PolicyUtil.checkState(context, productId, targetState, PolicyUtil.GE)) {
                  String strMessage = EngineeringUtil.i18nStringNow("emxEngineeringCentral.alert.releaseVPMProduct",
                      context.getSession().getLanguage());
                  emxContextUtil_mxJPO.mqlNotice(context,strMessage);
                  return 1;
              }
          }
      }
      return 0;
  }
  
  public Boolean displayProductConfigurationFilter(Context context, String[] args)
  throws Exception
  {
	  boolean isFTRInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionVariantConfiguration",false,null,null);
	  return Boolean.valueOf(isFTRInstalled);
  }
  
  public String getPCFilter1(Context context, String[] args) throws Exception {
	  String objectId= null;
	  HashMap programMap = (HashMap)JPO.unpackArgs(args);
	  HashMap requestMap= (HashMap)programMap.get("requestMap");
	  objectId = (String)requestMap.get("objectId1");
	  return getPCFilterExpressionDisplayBOM(context,"editPCFilter", "PUEUEBOMProductConfigurationFilter1OID","PCFilterId1",objectId,"showPCFilterDialog1");
  }
  
  public String getPCFilter2(Context context, String[] args) throws Exception {
	  String objectId= null;
	  HashMap programMap = (HashMap)JPO.unpackArgs(args);
	  HashMap requestMap= (HashMap)programMap.get("requestMap");
	  objectId = (String)requestMap.get("objectId2");
	  return getPCFilterExpressionDisplayBOM(context,"editPCFilter12", "PUEUEBOMProductConfigurationFilter2OID","PCFilterId2",objectId,"showPCFilterDialog2");
	  
  }
 
  public String getPCFilterExpressionDisplayBOM(Context context,String divName, String textField, String hiddenField, String objectId, String javascriptFun)throws Exception
  {
	  String policyClassification = "policy.property[PolicyClassification].value";
      String SELECT_PART_POLICYCLASS = "from["+EngineeringConstants.RELATIONSHIP_PART_REVISION+"].to.policy.property[PolicyClassification].value";

      StringList objectSelect = new StringList(3);
      objectSelect.addElement(policyClassification);
      objectSelect.addElement(DomainConstants.SELECT_TYPE);
      objectSelect.addElement(SELECT_PART_POLICYCLASS);
      if(null!=objectId && !"null".equals(objectId)) {
		   DomainObject doPart = new DomainObject(objectId);
		   Map dataMap = doPart.getInfo(context, objectSelect);
		   String type = (String) dataMap.get(DomainConstants.SELECT_TYPE);
		   
		   policyClassification =  EngineeringConstants.TYPE_PART_MASTER.equals(type) 
		   							? (String) dataMap.get(SELECT_PART_POLICYCLASS)			   
   									: (String) dataMap.get(policyClassification);
	   }
       return getPCFilterExpressionDisplayBOM(context,divName,textField,hiddenField,policyClassification,objectId,javascriptFun);
   }
   
  public String getPCFilterExpressionDisplayBOM(Context context, String divName, String textField, String hiddenField,String policy, String objectId, String javascriptFun)throws Exception
  {
		StringBuffer sb = new StringBuffer(256);
		if (policy.equals("Unresolved") || null == objectId || "null".equals(objectId)) {
			sb.append("<div id=\"");
			sb.append(divName);
			sb.append("\" style=\"visibility: visible;\">");
		} else {
			sb.append("<div id=\"");
		   sb.append(divName);
		   sb.append("\" style=\"visibility: hidden;\">");
		}
	   sb.append("<input type=\"text\" id=\"" );
	   sb.append(textField);
	   sb.append("\" name=\"");
	   sb.append(textField);
	   sb.append("\" size=\"20\" readonly=\"readonly\" />");			   			   
	   sb.append("<input type=\"hidden\" id=\"" );
	   sb.append(hiddenField);
	   sb.append("\" name=\"");
	   sb.append(hiddenField);
	   sb.append("\" />");
	   sb.append("<a href=\"javascript:");
	   sb.append(javascriptFun);
	   sb.append("()\">");
	   sb.append("<img src=\"../common/images/iconActionEdit.gif\"  border=\"0\"/></a>");
	   if(divName.equalsIgnoreCase("editPCFilter"))
		   sb.append("&nbsp<a href=\"javascript:basicClear('").append(textField).append("');basicClear('PCFilterId1') \">").append(EngineeringUtil.i18nStringNow(context, "emxEngineeringCentral.Common.Clear", context.getSession().getLanguage())).append("</a>");
	   else
		   sb.append("&nbsp<a href=\"javascript:basicClear('").append(textField).append("');basicClear('PCFilterId2') \">").append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear", context.getSession().getLanguage())).append("</a>");
	   sb.append("</div>");
	   return sb.toString();
  	}

@com.matrixone.apps.framework.ui.IncludeOIDProgramCallable
  public StringList getConfigurationContextPCIds(Context context, String[] args) throws Exception {
	  HashMap paramMap = JPO.unpackArgs(args);
	  String contextObjectId = (String) paramMap.get("objectId");

	  if (UIUtil.isNullOrEmpty(contextObjectId)) { return new StringList(); }

	  String SELECT_PC_ID = "from[" + PropertyUtil.getSchemaProperty(context, "relationship_ConfigurationContext") +
			  				"].to.from[" + PropertyUtil.getSchemaProperty(context, "relationship_MainProduct") +
			  				"].to.from[" + PropertyUtil.getSchemaProperty(context, "relationship_ProductConfiguration") + "].to.id";

	  return DomainObject.newInstance(context, contextObjectId).getInfoList(context, SELECT_PC_ID);
  }

/**
 * Method to check if the part is in WIP mode or not.Part with Development as Release Phase will be in WIP mode. Parts with Production as Release Phase will be in Non WIP mode.
 * @param object id
 * @return boolean returns true or false
 */
public static String isWipBomAllowedForParts(Context context,String objectId) throws Exception {
	try {
		String changeControlled = DomainObject.newInstance(context, objectId).getInfo(context, "attribute[" + EngineeringConstants.ATTRIBUTE_CHANGE_CONTROLLED + "]");
        return "True".equalsIgnoreCase(changeControlled) ? EngineeringConstants.PRODUCTION : EngineeringConstants.DEVELOPMENT; 
	} catch (Exception exp) {
		exp.printStackTrace();
		throw exp;
	}
}

}
