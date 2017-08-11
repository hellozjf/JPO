/*   emxCommonPartBase
**
**   Copyright (c) 2003-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the implementation of emxCommonPart
**
*/

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.Date;
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
import matrix.db.RelationshipType;
import matrix.util.SelectList;
import matrix.util.StringItr;
import matrix.util.StringList;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.common.PartFamily;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.OrganizationUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.VaultUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.domain.util.XSSUtil;

/**
 * The <code>emxCommonPartBase</code> class contains implementation code for emxCommonPart.
 *
 * @version Common 10.0.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxCommonPartBase_mxJPO extends emxDomainObject_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since Common 10.0.0.0
     * @grade 0
     */
    public emxCommonPartBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /** relationship "Design Responsibility". */
    public static final String RELATIONSHIP_DESIGN_RESPONSIBILITY =
                        PropertyUtil.getSchemaProperty("relationship_DesignResponsibility");
    /** relationship "Testing Responsibility". */
    public static final String RELATIONSHIP_TESTING_RESPONSIBILITY =
                        PropertyUtil.getSchemaProperty("relationship_TestingResponsibility");
    /** relationship "Manufacturing Responsibility". */
    public static final String RELATIONSHIP_MANUFACTURING_RESPONSIBILITY =
                        PropertyUtil.getSchemaProperty("relationship_ManufacturingResponsibility");
    /** relationship "Supply Responsibility". */
    public static final String RELATIONSHIP_SUPPLY_RESPONSIBILITY =
                        PropertyUtil.getSchemaProperty("relationship_SupplyResponsibility");
    public static String SUPPLIER_RELATION_PATTERN = RELATIONSHIP_DESIGN_RESPONSIBILITY + ","
                                + RELATIONSHIP_MANUFACTURING_RESPONSIBILITY + ","
                                + RELATIONSHIP_TESTING_RESPONSIBILITY + ","
                                + RELATIONSHIP_SUPPLY_RESPONSIBILITY;
    /** attribute "Show Sub Components". */
    public static final String SELECT_ATTRIBUTE_SHOW_SUB_COMPONENTS =
                        "attribute[" + PropertyUtil.getSchemaProperty("attribute_ShowSubComponents") + "]";

    /**
     * This method used to give read and show access for supplier role in ECPart policy.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds objectId as first argument
     * @return Boolean containing access rights
     * @throws Exception if the operation fails
     * @since VCP 10.5
     */
    public boolean hasAccessForSupplier(Context context, String[] args)
    {

        try
        {
            DomainObject part = DomainObject.newInstance(context, args[0]);
            StringList relSelects = new StringList(3);
            relSelects.add(SELECT_ATTRIBUTE_SHOW_SUB_COMPONENTS);
            String whereClause = "context.user ~~ from["+ RELATIONSHIP_EMPLOYEE +"].to.name";
            MapList mlist = part.getRelatedObjects(context, SUPPLIER_RELATION_PATTERN, TYPE_ORGANIZATION, null, relSelects, true, false, (short)1, whereClause, null);
            Iterator itr = mlist.iterator();
            while(itr.hasNext() )
            {
                Map m = (Map)itr.next();
                String showSubComponent = (String)m.get(SELECT_ATTRIBUTE_SHOW_SUB_COMPONENTS);
                if ( "All Levels".equals(showSubComponent) )
                {
                    return true;
                }
            }
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
            System.out.println("exception" + ex.getMessage());
        }
        return false;
    }
/**
     * This method used to Send Subscription notification to the user.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds objectId as first argument
     * @throws Exception if the operation fails
     * @since VCP 10.6
     */
  public void handleSubscriptionEvent (matrix.db.Context context, String[] args)
    throws Exception
  {
      //This method is moved from Engineering Central Part bean as Part Reference Documents is moved to commons
      try
      {
        if ( args == null || args.length == 0)
        {
          throw new IllegalArgumentException();
        }
        String objectId = args[0];
        String event = args[1];
        if (objectId != null && !"".equals(objectId) && !"null".equals(objectId)){
          String[] oids = new String[1];
          oids[0] = objectId;
          emxSubscriptionManager_mxJPO subMgr = new emxSubscriptionManager_mxJPO(context, oids);
          subMgr.publishEvent (context, event,objectId);
        } //end check for empty objectid param.
      }catch(Exception e){
        throw e;
      }
  }
  
  public void handlePartAddedSubscriptionEvent(Context context, String[] args) throws Exception {
      if ( args == null || args.length == 0) {
          throw new IllegalArgumentException();
      }
      handlePartAddedRemovedSubscriptionEvent(context, args[0], args[1], args[2], true);
  }
  
  public void handlePartRemovedSubscriptionEvent(Context context, String[] args) throws Exception {
      if ( args == null || args.length == 0) {
          throw new IllegalArgumentException();
      }
      handlePartAddedRemovedSubscriptionEvent(context, args[0], args[1], args[2], false);
  }
  
  
  private void handlePartAddedRemovedSubscriptionEvent(Context context, String parentPartId, String childPartId, String eventType, boolean addPart) throws Exception 
  {

      if( com.matrixone.apps.common.SubscriptionManager.suspendSubscription(context)){
          return;
      }
      if (parentPartId == null || "".equals(parentPartId) || "null".equals(parentPartId)) {
          //Not a valid partPartId
          return;
      }  
      if(childPartId == null || "".equals(childPartId) || "null".equals(childPartId)) {
          //Not a valid child part we can't send child information
          return;
      }
      
      String messageKey = "emxComponents.Event." + eventType.replace(' ', '_');
      String[] messageBodyMacros = {"PARENT_PART_TYPE", "PARENT_PART_NAME", "PARENT_PART_REV", "USER", "TIMESTAMP", "CHILD_PART_TYPE", "CHILD_PART_NAME", "CHILD_PART_REV"};
      String[] messageBodyMacroValues = new String[8];
      messageBodyMacroValues[3] = context.getUser();
      messageBodyMacroValues[4] = new SimpleDateFormat().format(new Date());

      SelectList selects = new SelectList();
      selects.addId();
      selects.addType();
      selects.addName();
      selects.addRevision();
        
      MapList info = DomainObject.getInfo(context, new String[]{parentPartId, childPartId}, selects);
      Map objInfo =  (Map) info.get(0);
      if(objInfo.get(SELECT_ID).equals(parentPartId)) {
          messageBodyMacroValues[0] = (String)objInfo.get(SELECT_TYPE);
          messageBodyMacroValues[1] = (String)objInfo.get(SELECT_NAME);
          messageBodyMacroValues[2] = (String)objInfo.get(SELECT_REVISION);
          
          objInfo = (Map) info.get(1);
          messageBodyMacroValues[5] = (String)objInfo.get(SELECT_TYPE);
          messageBodyMacroValues[6] = (String)objInfo.get(SELECT_NAME);
          messageBodyMacroValues[7] = (String)objInfo.get(SELECT_REVISION);
      } else {
          messageBodyMacroValues[5] = (String)objInfo.get(SELECT_TYPE);
          messageBodyMacroValues[6] = (String)objInfo.get(SELECT_NAME);
          messageBodyMacroValues[7] = (String)objInfo.get(SELECT_REVISION);

          objInfo = (Map) info.get(1);
          messageBodyMacroValues[0] = (String)objInfo.get(SELECT_TYPE);
          messageBodyMacroValues[1] = (String)objInfo.get(SELECT_NAME);
          messageBodyMacroValues[2] = (String)objInfo.get(SELECT_REVISION);
      }
      
      emxSubscriptionManager_mxJPO subMgr = new emxSubscriptionManager_mxJPO(context, new String[] {parentPartId});
      subMgr.publishEvent (context, 
                           eventType, 
                           parentPartId,
                           messageKey + ".Subject",
                           null,
                           null,
                           messageKey + ".MessageBody",
                           messageBodyMacros,
                           messageBodyMacroValues);
  }


  /**
    * Iterates through the objectIds passed as input arguments and
    * checks if object has associated Reference Document and accordingly adds true/false to returning vector.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds a packed HashMap with the following entries:
    * objectList - a MapList of objects.
    * paramList - a HashMap of parameter values.
    * @return Vector of boolean values representing whether the given object has the Reference Document relationship.
    * @throws Exception if the operation fails.
    * @since  VCP 11.0
    */
      public Vector  hasReferenceDocumentRelationship (Context context, String[] args)
                throws Exception
      {
        //This method is moved from Engineering Central Part bean as Part Reference Documents is moved to commons
        try
        {
          HashMap paramMap = (HashMap) JPO.unpackArgs(args);
          MapList busObjList = (MapList)paramMap.get("objectList");
          String partId =  (String) ((HashMap)paramMap.get("paramList")).get("objectId");
          DomainObject dObj = DomainObject.newInstance(context, partId);
          StringList partRefDocList = dObj.getInfoList(context,"from[" + RELATIONSHIP_REFERENCE_DOCUMENT + "].to.id");

          Vector columnValues = new Vector(busObjList.size());

          for (int i = 0; i < busObjList.size(); i++)
          {
            // Get Business object Id
            String strPartId = (String)((HashMap)busObjList.get(i)).get("id");

            if (partRefDocList.contains(strPartId) )
            {
              columnValues.add("false");
            } else {
              columnValues.add("true");
            }
          }
          return columnValues;
        }
        catch (Exception ex)
        {
          throw ex;
        }
      }

      /**
    * Get the search results for Reference Documents based on the criteria given.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds a packed HashMap of the following entries:
    * objectId - the object id of the context part.
    * selType - a String containing the Type to search for.
    * txtName - a String containing the Name to search for.
    * txtDesc - a String containing the Description to search for.
    * txtOriginator - a String containing the Originator to search for.
    * txtOwner - a String containing the Owner to search for.
    * txtWhere - a String containing the where clause to use in the search.
    * txtRev - a String containing the Revision to search for.
    * revPattern - a String of either ALL_REVISIONS, HIGHEST_REVISION or HIGHEST_AND_PRESTATE_REVS
    * Vault - a String containing the Vault name to search in.
    * queryLimit - a String containing the Query Limit value.
    * @return MapList of the objects ids resulting from the search.
    * @throws FrameworkException if the operation fails.
    * @since VCP 11.0
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getReferenceDocumentsSearchResults (Context context, String[] args)
            throws FrameworkException
    {
        MapList refDocList = new MapList();

        try
        {
            HashMap paramMap = (HashMap)JPO.unpackArgs(args);

            String partId               = (String) paramMap.get("objectId");
            String sType                = (String) paramMap.get("selType");
            String sName                = (String) paramMap.get("txtName");
            String txtDescription       = (String) paramMap.get("txtDesc");
            String txtOriginator        = (String) paramMap.get("txtOriginator");
            String txtOwner             = (String) paramMap.get("txtOwner");
            String txtWhere             = (String) paramMap.get("txtWhere");
            String sRev                 = (String) paramMap.get("txtRev");
            String sRevPattern          = (String) paramMap.get("revPattern");
            String sWhereExp            = "";

            /**************************Vault Code Start*****************************/
            // Get the user's vault option & call corresponding methods to get the vault's.

            String txtVault   ="";
            String strVaults="";
            StringList strListVaults    =   new StringList();

            String txtVaultOption       = (String)paramMap.get("vaultOption");
            if(txtVaultOption == null)
            {
                txtVaultOption="";
            }
            String vaultAwarenessString = (String)paramMap.get("vaultAwarenessString");

            if(vaultAwarenessString.equalsIgnoreCase("true"))
            {
                if(txtVaultOption.equals("ALL_VAULTS") || txtVaultOption.equals(""))
                {
                    strListVaults = com.matrixone.apps.common.Person.getCollaborationPartnerVaults(context,null);
                    StringItr strItr = new StringItr(strListVaults);
                    if(strItr.next())
                    {
                        strVaults =strItr.obj().trim();
                    }
                    while(strItr.next())
                    {
                        strVaults += "," + strItr.obj().trim();
                    }
                    txtVault = strVaults;
                }
                else if(txtVaultOption.equals("LOCAL_VAULTS"))
                {
                    com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
                    Company company = person.getCompany(context);
                    txtVault = company.getLocalVaults(context);
                }
                else if (txtVaultOption.equals("DEFAULT_VAULT"))
                {
                    txtVault = context.getVault().getName();
                }
                else
                {
                    txtVault = txtVaultOption;
                }
            }
            else
            {
                if(txtVaultOption.equals("ALL_VAULTS") || txtVaultOption.equals(""))
                {
                    // get ALL vaults
                    Iterator mapItr = VaultUtil.getVaults(context).iterator();
                    if(mapItr.hasNext())
                    {
                        txtVault =(String)((Map)mapItr.next()).get("name");
                        while (mapItr.hasNext())
                        {
                            Map map = (Map)mapItr.next();
                            txtVault += "," + (String)map.get("name");
                        }
                    }

                }
                else if(txtVaultOption.equals("LOCAL_VAULTS"))
                {
                    // get All Local vaults
                    com.matrixone.apps.common.Person person = com.matrixone.apps.common.Person.getPerson(context);
                    Company company = person.getCompany(context);
                    strListVaults = OrganizationUtil.getLocalVaultsList(context, company.getObjectId());

                    StringItr strItr = new StringItr(strListVaults);
                    if(strItr.next())
                    {
                        strVaults =strItr.obj().trim();
                    }
                    while(strItr.next())
                    {
                        strVaults += "," + strItr.obj().trim();
                    }
                    txtVault = strVaults;
                }
                else if (txtVaultOption.equals("DEFAULT_VAULT"))
                {
                    txtVault = context.getVault().getName();
                }
                else
                {
                    txtVault = txtVaultOption;
                }
            }
            //trimming
            txtVault = txtVault.trim();

            /*******************************Vault Code End***************************************/

            String queryLimit    = (String) paramMap.get("queryLimit");
            if (!(sRevPattern == null || sRevPattern.equalsIgnoreCase("null") || sRevPattern.equalsIgnoreCase("ALL_REVISIONS")))
            {
                String sRevQuery = "";
                if("LATEST_REVS".equals(sRevPattern))
                {
                    sRevQuery = " ( \"revision\" == \"last\" ) ";
                }
                else
                {
                    sRevQuery = "program[emxServiceUtils -method checkRevisions ${OBJECTID} \""+DomainObject.STATE_CADDRAWING_RELEASE+"\" " + sRevPattern + "] == true";
                }

                if (sWhereExp == null || sWhereExp.length() <= 0 )
                {
                    sWhereExp = sRevQuery;
                }
                else
                {
                    sWhereExp += "&&" + " " + sRevQuery;
                }
            }

            if ((txtDescription != null || txtDescription.trim().length() != 0) && !txtDescription.equals("*"))
            {
                String sDescWhere=" (description ~='" + txtDescription+"')";
                //Bug 307758
                // sWhereExp += "&&" + " (description ~='" + txtDescription+"')";
                if(sWhereExp!=null && sWhereExp.length() > 0)
                {
                   sWhereExp += (" &&"+sDescWhere);
                }
                else
                {
                   sWhereExp=sDescWhere;
                }
                //end Bug 307758
            }

            // Search on Originator field if entered by the user
            if ((txtOriginator != null || txtOriginator.trim().length() != 0) && !txtOriginator.equals("*"))
            {

                //Bug 307758
                //sWhereExp += " && " + " ("+DomainConstants.SELECT_ORIGINATOR +" ~='" + txtOriginator+"') ";
                String sOriginator = " ("+DomainConstants.SELECT_ORIGINATOR +" ~='" + txtOriginator+"')" ;
                if(sWhereExp!=null && sWhereExp.length() > 0)
                {
                   sWhereExp += (" &&"+sOriginator);
                }
                else
                {
                   sWhereExp=sOriginator;
                }
                //end Bug 307758
            }

            String strAttrIsVersionObject = PropertyUtil.getSchemaProperty(context,"attribute_IsVersionObject");

            if(sWhereExp != null && sWhereExp.length() > 0)
            {
                sWhereExp += " && ";
            }

            sWhereExp += "(!attribute["+strAttrIsVersionObject+"] == \"True\")";

            StringList selectStmts = new StringList(1);
            selectStmts.addElement("id");

            refDocList =  DomainObject.findObjects(context,
                                            sType,
                                            sName,
                                            sRev,
                                            txtOwner,
                                            txtVault,
                                            sWhereExp,
                                            null,
                                            true,
                                            selectStmts,
                                            Short.parseShort(queryLimit));

            return refDocList;
        }
        catch(Exception Ex)
        {
            throw new FrameworkException(Ex);
        }
    }

    public Boolean isECInstalled (Context context, String[] args)
            throws Exception
    {
        boolean isECInstalled = false;
        try
        {
            isECInstalled = FrameworkUtil.isSuiteRegistered(context,"appVersionEngineeringCentral",false,null,null);
        }catch(Exception e){
            throw e;
        }
        return Boolean.valueOf(!isECInstalled);
    }

     public String getPartMessageHTML(Context context, String[] args) throws Exception
    {
        Map info = (Map)JPO.unpackArgs(args);
        info.put("messageType", "html");
        com.matrixone.jdom.Document doc = getPartMailXML(context, info);
        return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html"));

    }

    public String getPartMessageText(Context context, String[] args) throws Exception
    {
        Map info = (Map)JPO.unpackArgs(args);
        info.put("messageType", "text");
        com.matrixone.jdom.Document doc = getPartMailXML(context, info);
        return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text"));

    }

     public static com.matrixone.jdom.Document getPartMailXML(Context context, Map info) throws Exception
    {
        // get new message information
        String newMsgId = (String)info.get("id");
        // get base url
        String baseURL = (String)info.get("baseURL");
        String bundleName = (String)info.get("bundleName");
        String locale = ((Locale)info.get("locale")).toString();
        Locale localeMessage = (Locale)info.get("locale");
        String messageType = (String)info.get("messageType");
        String partId = (String)info.get("id");
        String notificationName = (String)info.get("notificationName");
        HashMap eventCmdMap = UIMenu.getCommand(context, notificationName);
        String eventName = UIComponent.getSetting(eventCmdMap, "Event Type");
        String eventKey = "emxComponents.Event." + eventName.replace(' ', '_');
        String i18NEvent =  EnoviaResourceBundle.getProperty(context, bundleName, localeMessage, eventKey);
        DomainObject part = DomainObject.newInstance(context, partId);
        StringList selectList = new StringList(3);
        selectList.addElement(SELECT_TYPE);
        selectList.addElement(SELECT_NAME);
        selectList.addElement(SELECT_REVISION);
		// PNO Context Information selectables
        selectList.addElement("altowner1");
		selectList.addElement("altowner2");
        Map partInfo = part.getInfo(context, selectList);
        String partType = (String)partInfo.get(SELECT_TYPE);
        String i18NpartType = UINavigatorUtil.getAdminI18NString("Type", partType, locale);
        String partName = (String)partInfo.get(SELECT_NAME);
        String partRev = (String)partInfo.get(SELECT_REVISION);

		// getting PNO Context Project & Organization Information
        String altowner1 = (String) partInfo.get("altowner1");
		String altowner2 = (String) partInfo.get("altowner2");
        String partDeletedEventName = "Part Deleted";

        // Message Header Configuration start
        HashMap headerInfo = new HashMap();
        headerInfo.put("header", i18NpartType + " " + partName + " " + partRev + " : " + i18NEvent);
		// Message Header Configuration End

		// For representing the Body of the message
		HashMap bodyInfo = null;

		// If altowner1 & altowner2 is available on the object, they will be
		// included in the notification mail body.
		// Message Body Configuration start
		if ((null != altowner1) && (null != altowner2)
				&& (!"".equals(altowner1)) && (!"".equals(altowner2))) {
			bodyInfo = new HashMap();
			Map dataMap = new HashMap();
			dataMap.put(EnoviaResourceBundle.getProperty(context, bundleName, localeMessage, "emxComponents.message.ContextOrganization"), altowner1);
			dataMap.put(EnoviaResourceBundle.getProperty(context, bundleName, localeMessage, "emxComponents.message.ContextProject"), altowner2);
			bodyInfo.put(EnoviaResourceBundle.getProperty(context, bundleName, localeMessage, "emxComponents.message.ContextInfo"), dataMap);
		}
		// Message Body Configuration End.

		// Message Footer Configuration start
         HashMap footerInfo = new HashMap();
        ArrayList dataLineInfo = new ArrayList();

        if(!eventName.equalsIgnoreCase(partDeletedEventName))
        {
            if (messageType.equalsIgnoreCase("html"))
            {
                String[] messageValues = new String[4];
                messageValues[0] = baseURL + "?objectId=" + partId;
                messageValues[1] = i18NpartType;
                messageValues[2] = partName;
                messageValues[3] = partRev;
                String viewLink = MessageUtil.getMessage(context,partId,
                                                         "emxComponents.Object.Event.Html.Mail.ViewLink",
                                                         messageValues,null,
                                                         localeMessage,bundleName);
                dataLineInfo.add(viewLink);

            } else
            {
                String[] messageValues = new String[3];
                messageValues[0] = i18NpartType;
                messageValues[1] = partName;
                messageValues[2] = partRev;
                String viewLink = MessageUtil.getMessage(context,partId,
                                                         "emxComponents.Object.Event.Text.Mail.ViewLink",
                                                         messageValues,null,
                                                         localeMessage,bundleName);

                dataLineInfo.add(viewLink);
                dataLineInfo.add(baseURL + "?objectId=" + partId);
            }
        }

        footerInfo.put("dataLines", dataLineInfo);
		// Message Footer Configuration end

		// sending the header, body and the footer information to prepare the
		// mail.
        return (emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo,
				bodyInfo, footerInfo));
    }

    /**
     * Connection between the cloned Part and source Part is established with Derived Realationship.
     * The following steps are performed:
     *   - The Cloned Part id is retrieved
     *   - The Source Part id from which the part is cloned is retrieved
     *   - The connection is established between the Parts

     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     *          - The ObjectID of the context part
     *            The ObjectId of the Source Part
     * @throws Exception if the operation fails.
     * @since X3.
     */
public void connectClonedObject(Context context, String[] args) throws Exception
    {
        //This method would be called from a jsp from where the Object id's of source part and cloned part are passed

        HashMap programMap = (HashMap)JPO.unpackArgs(args);

        String sourcePartID = (String) programMap.get("SourcePartID");//Source PartID
        String clonedPartID = (String) programMap.get("ClonedPartID");//Cloned PartID

        RelationshipType relType = new RelationshipType(PropertyUtil.getSchemaProperty(context,"relationship_Derived"));

        DomainObject sourceObject = new DomainObject(sourcePartID);
        DomainObject clonedObject = new DomainObject(clonedPartID);

        //Checks if both the source and cloned id's are of type Part.
		//368694 : Modified to fix for sub type of Part in cloning
        //if (sourceObject.getInfo(context,"type").equals(PropertyUtil.getSchemaProperty(context,"type_Part")) && clonedObject.getInfo(context,"type").equals(PropertyUtil.getSchemaProperty(context,"type_Part")))
        if (sourceObject.isKindOf(context, DomainConstants.TYPE_PART) && clonedObject.isKindOf(context, DomainConstants.TYPE_PART)){
			//End
            //Connection is established between the source and the cloned objects
             DomainRelationship domRel = DomainRelationship.connect(context,        //Context
                                                                    sourceObject,   //From Object
                                                                    relType,        //Relationship Type
                                                                    clonedObject    //To Object
                                        );
             // 372458
             String attrDerivedContext= PropertyUtil.getSchemaProperty(context,"attribute_DerivedContext");
             domRel.setAttributeValue(context, attrDerivedContext, "Clone");
        }
    }

     /**
     * The Derived Parts in the From side of the Derived relationship of context part is retrieved
     * The following steps are performed:
     *   - The Context Part id is retrieved
     *   - The Parts connected to the context part with Derived relationship in the from side are retrieved

     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     *          - The ObjectID of the context part
     * @throws Exception if the operation fails.
     * @since ECX3.
     */
@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getDerivedFromParts(Context context, String[] args) throws FrameworkException
    {
        MapList DerivedFrompartList = new MapList();
        MapList finalMapList = new MapList();

        try
        {
            //ObjectSelects
            SelectList selectStmts = new SelectList(1);
            selectStmts.addElement(DomainObject.SELECT_ID);
            //RelationshipSelects
            SelectList relStmts = new SelectList(1);
            relStmts.addElement(DomainRelationship.SELECT_ID);

            //To get the context part id
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            String DerivedFromPartId    = (String)paramMap.get("objectId");
            //Added for IR-371814
            String expandLevel = (String)paramMap.get("expandLevel");
            int level = getExpandLevel(context,expandLevel);
            //IR-371814
            //To get the Type and Relationship
            String strType = PropertyUtil.getSchemaProperty(context,"type_Part");
            String RELATIONSHIP_Derived = PropertyUtil.getSchemaProperty(context,"relationship_Derived");

            DomainObject DerivedFromObj = new DomainObject(DerivedFromPartId);

            StringList objSelects = new StringList(5);
            objSelects.add(DomainConstants.SELECT_NAME);
            objSelects.add(DomainConstants.SELECT_REVISION );
            objSelects.add(DomainConstants.SELECT_DESCRIPTION );
            objSelects.add(DomainConstants.SELECT_CURRENT );
            objSelects.add("to["+DomainRelationship.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.name" );




            //String derivedFromName = DerivedFromObj.getInfo(context, DomainConstants.SELECT_NAME);

            Map objInfoMap = DerivedFromObj.getInfo(context, objSelects);
            System.out.println("objInfoMap "+objInfoMap);
            DerivedFrompartList = (MapList)DerivedFromObj.getRelatedObjects(context,         //Context
                                                                RELATIONSHIP_Derived,        //Relationship Pattern
                                                                strType,                     //Type Pattern
                                                                selectStmts,                 //Object Selects
                                                                relStmts,                    //Relationship Selects
                                                                true,                        //get TO
                                                                false,                       //get From
                                                                //(short)1,                    //Recurrence Level
                                                                (short)level,//371814          Recurrence Level
                                                                "",                          //Object Where
                                                                ""                           //RelationShip Where
                                                                );

            for(int icnt =  0 ; icnt < DerivedFrompartList.size() ;icnt++){

            	Map tmpMap = (Map)DerivedFrompartList.get(icnt);

            	System.out.println("tmpMap "+tmpMap);
            	tmpMap.put("derivedId" , DerivedFromPartId );
            	tmpMap.put("derivedName" ,(String) objInfoMap.get(DomainConstants.SELECT_NAME));
            	tmpMap.put("derivedRev" , (String)objInfoMap.get(DomainConstants.SELECT_REVISION));

            	if(objInfoMap.containsKey(DomainConstants.SELECT_DESCRIPTION)){
            		tmpMap.put("derivedDesc" ,(String) objInfoMap.get(DomainConstants.SELECT_DESCRIPTION));
            	}else{
            		tmpMap.put("derivedDesc" , "");
            	}

            	tmpMap.put("derivedCurrent" , (String)objInfoMap.get(DomainConstants.SELECT_CURRENT));

            	if(objInfoMap.containsKey("to["+DomainRelationship.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.name")){
            		tmpMap.put("derivedRDO" , (String)objInfoMap.get("to["+DomainRelationship.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.name"));
            	}else{
            		tmpMap.put("derivedRDO" , "");
            	}
            	finalMapList.add(tmpMap);
            }

        }
        catch(Exception exp)
        {
            throw new FrameworkException(exp.toString());
        }
        //IR-371814
        HashMap hmTemp = new HashMap();
        hmTemp.put("expandMultiLevelsJPO","true");
        //DerivedFrompartList.add(hmTemp);

        finalMapList.add(hmTemp);
        //IR-371814
        //return DerivedFrompartList;


        return finalMapList ;
    }

/**
 * This method returns the name of the Derived Part
 *
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds the following input arguments:
 *          - The ObjectID of the context part
 * @throws Exception if the operation fails.
 * @since ECR210.
 */

public Vector getDerivedName (Context context , String[] args) throws Exception{

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList)programMap.get("objectList");
    Vector columnVals = new Vector(objList.size());

    //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship

    for(int iCnt = 0 ; iCnt < objList.size() ; iCnt++){

        Map m = (Map) objList.get(iCnt);
        String name = (String)m.get("derivedName");
        if(name == null){
            name="-";
        }

        String objId = (String)m.get("derivedId");
        StringBuffer outPut = new StringBuffer(1000);

        outPut.append("<a href=\"javascript:showModalDialog('emxTree.jsp?objectId=" + objId + "',650,700);\">");
        outPut.append(name);
        outPut.append("</a>");


        columnVals.addElement(outPut.toString());
    }

	return columnVals;
}

/**
 * This method returns the Revision of the Derived Part
 *
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds the following input arguments:
 *          - The ObjectID of the context part
 * @throws Exception if the operation fails.
 * @since ECR210.
 */

public Vector getDerivedRev (Context context , String[] args) throws Exception{

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList)programMap.get("objectList");
    Vector columnVals = new Vector(objList.size());

    //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
    for(int iCnt = 0 ; iCnt < objList.size() ; iCnt++){

        Map m = (Map) objList.get(iCnt);
        String rev = (String)m.get("derivedRev");
        if(rev == null){
            rev="-";
        }
        columnVals.addElement(rev);
    }
	return columnVals;
}

/**
 * This method returns the Description of the Derived Part
 *
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds the following input arguments:
 *          - The ObjectID of the context part
 * @throws Exception if the operation fails.
 * @since ECR210.
 */
public Vector getDerivedDesc (Context context , String[] args) throws Exception{

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList)programMap.get("objectList");
    Vector columnVals = new Vector(objList.size());

    //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
    for(int iCnt = 0 ; iCnt < objList.size() ; iCnt++){

        Map m = (Map) objList.get(iCnt);
        String desc = (String)m.get("derivedDesc");
        if(desc == null){
        	desc="-";
        }
        columnVals.addElement(desc);
    }
	return columnVals;
}

/**
 * This method returns the Current State of the Derived Part
 *
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds the following input arguments:
 *          - The ObjectID of the context part
 * @throws Exception if the operation fails.
 * @since ECR210.
 */
public Vector getDerivedCurrent (Context context , String[] args) throws Exception{

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList)programMap.get("objectList");
    Vector columnVals = new Vector(objList.size());

    //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
    for(int iCnt = 0 ; iCnt < objList.size() ; iCnt++){

        Map m = (Map) objList.get(iCnt);
        String current = (String)m.get("derivedCurrent");
        if(current == null){
        	current="-";
        }
        columnVals.addElement(current);
    }
	return columnVals;
}
/**
 * This method returns the Responsible Design Organization of the Derived Part
 *
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds the following input arguments:
 *          - The ObjectID of the context part
 * @throws Exception if the operation fails.
 * @since ECR210.
 */
public Vector getDerivedRDO (Context context , String[] args) throws Exception{

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    MapList objList = (MapList)programMap.get("objectList");
    Vector columnVals = new Vector(objList.size());

    //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
    for(int iCnt = 0 ; iCnt < objList.size() ; iCnt++){

        Map m = (Map) objList.get(iCnt);
        String rdo = (String)m.get("derivedRDO");
        if(rdo == null){
        	rdo="-";
        }
        columnVals.addElement(rdo);
    }
	return columnVals;
}

     /**
     * The Derived Parts in the To side of the Derived relationship of context part is retrieved
     * The following steps are performed:
     *   - The Context Part id is retrieved
     *   - The Parts connected to the context part with Derived relationship in the To side are retrieved

     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     *          - The ObjectID of the context part
     * @throws Exception if the operation fails.
     * @since ECX3.
     */
@com.matrixone.apps.framework.ui.ProgramCallable
public static MapList getDerivedToParts(Context context, String[] args) throws FrameworkException
    {
        MapList DerivedTopartList = new MapList();
        try
        {
            //ObjectSelects
            SelectList selectStatements = new SelectList(2);
            selectStatements.addElement(DomainObject.SELECT_ID);
            //RelationshipSelects
            SelectList relStatements = new SelectList(1);
            relStatements.addElement(DomainRelationship.SELECT_ID);

            //To get the context part id
            HashMap paramMap = (HashMap) JPO.unpackArgs(args);
            String DerivedToPartId    = (String)paramMap.get("objectId");
            //Added for IR-371814
            String expandLevel = (String)paramMap.get("expandLevel");
            int level = getExpandLevel(context,expandLevel);
            //IR-371814
            //To get the Type and Relationship
            String strType = PropertyUtil.getSchemaProperty(context,"type_Part");
            String RELATIONSHIP_Derived = PropertyUtil.getSchemaProperty(context,"relationship_Derived");

            DomainObject DerivedToObj = new DomainObject(DerivedToPartId);

            DerivedTopartList = (MapList)DerivedToObj.getRelatedObjects(context,                   //Context
                                                                RELATIONSHIP_Derived,              //Relationship Pattern
                                                                strType,                           //Type Pattern
                                                                selectStatements,                  //Object Selects
                                                                relStatements,                     //Relationship Selects
                                                                false,                             //get TO
                                                                true,                              //get From
                                                                //(short)1,                        //Recurrence Level
                                                                (short)level, //IR-371814            Recurrence Level
                                                                "",                                //Object Where
                                                                ""                                 //RelationShip Where
                                                                );
        }
        catch(Exception exp)
        {
            throw new FrameworkException(exp.toString());
        }
        //IR-371814
        HashMap hmTemp = new HashMap();
        hmTemp.put("expandMultiLevelsJPO","true");
        DerivedTopartList.add(hmTemp);
        //IR-371814
        return DerivedTopartList;
    }
/**
 * Added for IR-371814
 * Returns the expandLevel in integer format.
 * If expandfilter is All then set to "0".
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds the following input arguments:
 *        -The expandLevel passed by user
 *
*/
     private static int getExpandLevel(Context context, String expandLevel)
     {
         int level = 1;
         
         if (expandLevel != null && !"".equals(expandLevel)) {
	         if ("All".equalsIgnoreCase(expandLevel)) {
	             level = 0;
	         } else {
	        	 try {
	        		 level = Integer.parseInt(expandLevel);
	        	 } catch (Exception e) {
	        		 level = 1;
	        	 }
	         }
     	 }
         
         return level;
     }
     //end of 371814
        //Added for Part Enhancement feature to get the Derived Parts Generation Number

     /**
     * The Level in the Derived relationship of context part is retrieved
     * The following steps are performed:
     *   - The Parts connected to the context part with Derived relationship id's are retained and corresponding Level is retrieved

     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     *          - The ObjectList of the parts
     * @throws Exception if the operation fails.
     * @since ECX3.
     */
    public Vector getLevel (Context context,String[] args)throws Exception
    {

        //To get the Object id List
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        MapList objList = (MapList)programMap.get("objectList");
        Vector columnVals = new Vector(objList.size());

        //Iteration through the Maplist of objectlist to retrieve the level of objects connected to context part by Derived relationship
        Iterator i = objList.iterator();
        while (i.hasNext())
        {
            Map m = (Map) i.next();
            String level = (String)m.get("level");
            if(level == null){
                level="1";
            }
            columnVals.addElement(level);
        }
        return columnVals;
    }

    /**
     * Generates the Include Releated Data field for Clone Part Form
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
            HashMap fieldMap = (HashMap) programMap.get("fieldMap");
            HashMap settings = (HashMap) fieldMap.get("settings");
            String objectId = (String) requestMap.get("copyObjectId");
            //String srcObjType = (String) requestMap.get("type");
            String srcObjType = "";

            String strLang = context.getSession().getLanguage();
            Locale strLocale = context.getLocale();
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
                 if (domPartObj.isKindOf(context, TYPE_PART) == true) {
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
                applications = EnoviaResourceBundle.getProperty(context,"emxComponents.PartClone.Applications");
            }catch(Exception ex){
                applications = "Components";
            }
            StringList applicationList= FrameworkUtil.split(applications,",");
            com.matrixone.apps.common.Person person = null;

            for (int appItr=0;appItr<applicationList.size() ;appItr++)
            {
                // Get the application specific relationship pattern and loop thru
                try{
                   specificIncRel = EnoviaResourceBundle.getProperty(context,"emx"+applicationList.get(appItr)+".PartClone.IncludeRelData");
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
                   if(IndividualRelPattern.indexOf("|")>0){
                     IndividualRelPattern=(IndividualRelPattern.substring(0,IndividualRelPattern.indexOf("|"))).trim();
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
                           if(strListIndTypeRels.contains("type_ComplianceEnterprisePart") && srcObjType.equals("type_Part") && !isPartEnabled) {
                               continue;
                           }
                           if(IndividualRelPattern.equalsIgnoreCase("relationship_CBOM") && srcObjType.equals("type_Part") && strListIndTypeRels.contains("type_ComplianceEnterprisePart")) {
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
                        sRelDisplayLbl = EnoviaResourceBundle.getProperty(context,"emx"+applicationList.get(appItr)+"StringResource", strLocale,sRelI18Nkey);

                        if (sRelI18Nkey.equals(sRelDisplayLbl))
                        {
                            relName = FrameworkUtil.findAndReplace(PropertyUtil.getSchemaProperty(context,IndividualRelPattern), " ", "_");
                            sRelDisplayLbl = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", strLocale,"emxFramework.Relationship." +relName);
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

            sb.append("<script language=\"javascript\" src=\"../components/emxComponentsUIFormValidation.js\"></script>");
            sb.append("<input type=\"checkbox\" name=\"AllRels\"  onclick=\"javascript:checkAllRels()\"/>");
            sb.append(EnoviaResourceBundle.getProperty(context,
                   "emxComponentsStringResource", strLocale,
                   "emxComponents.PartCloneIncludeRelData.AllRel"));
            // Sort the checkboxes by diosplay name
            allAppsI18nIncRelData.sort();

            // loop thru the sorted list
            for (int allRel=0; allRel < allAppsI18nIncRelData.size(); allRel++) {
                sb.append("<input type=\"checkbox\" name=\"AllRels");
                sb.append(allRel);
                sb.append("\" value=\"");
                sb.append(PropertyUtil.getSchemaProperty(context,
                        (String)keyLabel.get((String)allAppsI18nIncRelData.get(allRel))));
                sb.append("\"/>");
                sb.append("&#160;");
                sb.append((String)allAppsI18nIncRelData.get(allRel));
                sb.append("&#160;");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sb.toString();
    }

    /**
     * Copies all the releated objects of the orignal object to the new cloned object in Clone Part
     *
     * @mx.whereUsed Invoked from common Clone Part web form's IncludeReleatedData field as
     *               an update program.
     *
     * @mx.summary this methods takes the chcek box values which is selected
     *             from the Include Releated Field of the clone dialog and
     *             creates a comma separated String to pass inside the bean.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     * @throws Exception
     *             if the operation fails
     * @since V6R2009-1
     */

    public void updateIncludeRelDataField(Context context, String[] args)
            throws Exception {
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            HashMap fieldMap = (HashMap) programMap.get("fieldMap");
            HashMap settings = (HashMap) fieldMap.get("settings");
            String newObjectId = (String) paramMap.get("objectId");
            String[] parentId = (String[]) requestMap.get("copyObjectId");
            String[] clonePartNumId = (String[]) requestMap.get("clonePartNumOID");
            String parentOId = "";
            String key = "";
            int idLength = 0;

            // Giving high precedence to Clone Part Num Oid, Since cloneBasedOn editable.
            if(clonePartNumId != null && clonePartNumId[0].length()!=0){
                parentId = clonePartNumId;
            }

            if(parentId != null ) {
                idLength = parentId.length;
            }
            for (int id = 0; id < idLength; id++) {
                parentOId = parentId[id];
            }
            StringList relList = new StringList();
            StringBuffer sb = new StringBuffer();
            Iterator iterator = requestMap.keySet().iterator();
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                if (key.startsWith("AllRels")) {
                    String[] relName = (String[]) requestMap.get(key);
                    for (int b = 0; b < relName.length; b++) {
                        String arrValue = relName[b];
                        if (!"".equals(arrValue) && !relList.contains(arrValue) && !"on".equals(arrValue)) {
                            relList.add(arrValue);
                        }
                    }
                }
            }
            for (int size = 0; size < relList.size(); size++) {
                sb.append(relList.get(size));
                if (size < (relList.size()) - 1) {
                    sb.append(",");
                }
            }
            if (relList.size() > 0) {
                com.matrixone.apps.common.Part commonPart = new com.matrixone.apps.common.Part();
                commonPart.cloneCommonPartStructure(context, parentOId,
                        newObjectId, sb.toString());

                // Get the applications to call the respective update programs
                String applications = "";
                try{
                    applications = EnoviaResourceBundle.getProperty(context,"emxComponents.PartClone.Applications");
                }catch(Exception ex){
                    applications = "Components";
                }

                // Loop thru the applications and get the app specific update program/function
                StringList applicationList= FrameworkUtil.split(applications,",");
                for (int appItr=0;appItr<applicationList.size() ;appItr++)
                {
                    String appName = (String)applicationList.get(appItr);
                    String updatePrgm = (String)settings.get(appName + "_Update_Program");
                    String updateFn = (String)settings.get(appName + "_Update_Function");
                    // invoke the program or function if both are defined
                    if((updatePrgm != null && !"null".equals(updatePrgm) && !"".equals(updatePrgm)) &&
                       (updateFn != null && !"null".equals(updateFn) && !"".equals(updateFn)))
                    {
                        Class upCls = Class.forName(updatePrgm);
                        Object clsObj = upCls.newInstance();

                        // The method signature should be " Context context, String sourceObjectId, String newObjectID, String RelPattern
                        Class[] paramCls = {Context.class,String.class,String.class,String.class};
                        Method run = upCls.getDeclaredMethod(updateFn, paramCls);

                        Object[] params = new Object[4];
                        params[0] = context;
                        params[1] = parentOId;
                        params[2] = newObjectId;
                        params[3] = sb.toString();

                        run.invoke(clsObj, params);
                    }
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }



    /**
     * Generate the Policy field for Clone Part Form
     *
     * @mx.whereUsed Invoked from common Clone Part web form's policy field used to Clone
     *               object
     *
     * @mx.summary this methods takes the passed copyObjectId and displays the
     *             policy in a combo box. If copyObjectId is null, then value of
     *             the type passed from the HREF is taken in to consideration to
     *             populate the policies.
     *
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     * @return the <code>String</code> value
     * @throws Exception
     *             if the operation fails
     * @since V6R2009-1
     */
    public String showPolicyField(Context context, String[] args)
            throws Exception {
        StringBuffer resultBuffer = new StringBuffer();
        try {
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            //Added for the fix 366148
            String languageStr = (String) requestMap.get("languageStr");
            //366148 fix ends
            String objectId = (String) requestMap.get("copyObjectId");
            String typeName = (String) requestMap.get("type");
            if (typeName.indexOf("type_") > 0) {
                typeName = typeName.substring(typeName.indexOf("type_"),
                        typeName.length());
            }
            DomainObject domCompliancePartObj = null;
            String policy = "";
            resultBuffer.append("<select name=\"Policy\" id=\"PolicyId\">");
            if (objectId != null && !objectId.equals("")
                    && !objectId.equals("null")) {
                domCompliancePartObj = new DomainObject(objectId);
                policy = domCompliancePartObj.getInfo(context, SELECT_POLICY);
                resultBuffer.append("<option value=\"");
                resultBuffer.append(XSSUtil.encodeForHTMLAttribute(context,policy));
                resultBuffer.append("\">");
				//Modified for the fix 366148
               // resultBuffer.append(policy);
                resultBuffer.append(i18nNow.getAdminI18NString("Policy",policy,languageStr));
                resultBuffer.append("</option>");
            } else if (typeName != null) {
                MapList policyList = new MapList();
                String partType = PropertyUtil.getSchemaProperty(context,
                        typeName);
                try {
                    policyList = mxType.getPolicies(context, partType, false);
                } catch (FrameworkException eee) {
                    System.out.println("Exception:" + eee.getMessage());
                }
                Iterator policyIterator = policyList.iterator();
                while (policyIterator.hasNext()) {
                    Map policyMap = (Map) policyIterator.next();
                    //for the fix 366148
                    String partPolicyName = (String)policyMap.get("name");
                    // 366148 ends
                    resultBuffer.append("<option value=\"");
                    resultBuffer.append(XSSUtil.encodeForHTMLAttribute(context,partPolicyName));
                    resultBuffer.append("\">");
                    //for the fix 366148
                    //resultBuffer.append((String) policyMap.get("name"));
                    resultBuffer.append(i18nNow.getAdminI18NString("Policy",partPolicyName,languageStr));
                    //fix 366148 ends
                    resultBuffer.append("</option>");
                }
            }
            resultBuffer.append("</select>");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultBuffer.toString();
    }



        /**
         * This method is used get the policy with same policy classification as the current policy of a Part
         * @param context the eMatrix <code>Context</code> object
         * @param args holds a HashMap containing the following entries:
         * paramMap - a HashMap containing the following keys, "objectId"
         * @return String which contains HTML code to display a dropdown with all the policies
         * @throws Exception if operation fails
         * @since AppsCommon  - Copyright (c) 2005, MatrixOne, Inc.
         */


   public String getPolicyClassificationPolicies(Context context,
                                        String[] args)
       throws Exception
   {
       StringBuffer returnString = new StringBuffer();

       HashMap programMap = (HashMap) JPO.unpackArgs(args);
       HashMap requestMap = (HashMap) programMap.get("requestMap");
       String languageStr = (String) requestMap.get("languageStr");
       String strMode=(String)requestMap.get("mode");
       String strPartId = (String)requestMap.get("objectId");
       DomainObject partObj = new DomainObject(strPartId);
       StringList strList = new StringList();
       strList.add(SELECT_POLICY);
       strList.add(SELECT_TYPE);
       Map map = partObj.getInfo(context,strList);
       String currentPartPolicyName = (String)map.get(SELECT_POLICY);
       String strType = (String)map.get(SELECT_TYPE);
       String currentPolicyClassification = PartFamily.getPolicyClassification(context,currentPartPolicyName);


       if("edit".equalsIgnoreCase(strMode))
       {
         String isPolicyEdit = EnoviaResourceBundle.getProperty(context,"emxComponents.AllowChangePolicy");
         if ("true".equalsIgnoreCase(isPolicyEdit))
         {
             boolean hasChangePolicyAccess = FrameworkUtil.hasAccess(context,partObj,"changepolicy");
             if (hasChangePolicyAccess)
             {
                 BusinessType partBusType = new BusinessType(strType, context.getVault());
                 partBusType.open(context);
                 // Get the policies of that Object
                 PolicyList partPolicyList = partBusType.getPolicies(context);

                 PolicyItr  partPolicyItr  = new PolicyItr(partPolicyList);
                 partBusType.close(context);

                 while(partPolicyItr.next())
                 {
                    Policy partPolicy = partPolicyItr.obj();
                    String partPolicyName = partPolicy.getName();

                    String policyClassification = PartFamily.getPolicyClassification(context,partPolicyName);

                    if (policyClassification.equalsIgnoreCase(currentPolicyClassification))
                      {
                         if(returnString.length()==0)
                          {
                               returnString.append("<select name=\"PolicyDisplay\">");
                          }
                          returnString.append("<option value=\""+XSSUtil.encodeForHTMLAttribute(context,partPolicyName)+"\" "+(currentPartPolicyName.equals(partPolicyName)?"selected=\"true\"":"")+">"+i18nNow.getAdminI18NString("Policy",partPolicyName,languageStr)+"</option>");
                      }
                 }
                 if(returnString.length()!=0)
                 {
                     returnString.append("</select>");
                 }
               }
           }
       }

      if(returnString.length()==0)
      {
          returnString.append(i18nNow.getAdminI18NString("Policy",currentPartPolicyName,languageStr));
      }

      return returnString.toString();

   }   // end of method


 /**
         * This method is used to update the policy of a Part
         * @param context the eMatrix <code>Context</code> object
         * @param args holds a HashMap containing the following entries:
         * paramMap - a HashMap containing the following keys, "objectId", "old RDO Ids", "New OID".
         * @return Object - boolean true if the operation is successful
         * @throws Exception if operation fails
         * @since AppsCommon x+3 - Copyright (c) 2005, MatrixOne, Inc.
         */

   public Object updatePolicy(Context context, String[] args)
      throws Exception
    {

    HashMap programMap = (HashMap) JPO.unpackArgs(args);
    HashMap paramMap = (HashMap) programMap.get("paramMap");
    HashMap requestMap = (HashMap) programMap.get("requestMap");
    String strObjId = (String) paramMap.get("objectId");

    String [] strNewPolicies = (String[]) requestMap.get("PolicyDisplay");
    String strNewPolicy = "" ;
    if(strNewPolicies!=null && !"null".equals(strNewPolicies))
    {
        strNewPolicy = strNewPolicies[0];
        DomainObject partObj = new DomainObject(strObjId);
        String strCurrentPolicy = partObj.getInfo(context,SELECT_POLICY); //Old Vale
        if(strNewPolicy!=null && !"null".equals(strNewPolicy) && !strNewPolicy.equalsIgnoreCase(strCurrentPolicy))
        {
        partObj.open(context);
        partObj.setPolicy(context, strNewPolicy);
        partObj.close(context);
        }

    }
      return Boolean.valueOf(true);

  }

}
