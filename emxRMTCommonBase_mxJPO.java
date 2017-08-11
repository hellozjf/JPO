/*
** emxRMTCommonBase
**
** Copyright (c) 2007 MatrixOne, Inc.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
**
*/

/*

Change History:
Date       Change By  Release   Bug/Functionality        Details
-----------------------------------------------------------------------------------------------------------------------------
15-Apr-09  kyp        V6R2010   357905                   Added method triggerCheckReservedObject to implement check trigger
                                                         to check RMT object reservation before modifying any object.
21-Fev-13  jx5		  V6R2014   IR-218082V6R2014         Added methods getRequirementIcons and getRelIconProperty to handle
														 the display of Requirement icon depending on object's relationship
19-MAR-13  lx6		  V6R2014   UI enhancement           This delivery consists in UI enhancement on tables and forms

03-MAY-13  jx5		  V6R2014   IR-233755V6R2014		 Take custom Requirement types into account in getRequirementIcons

17-MAY-13  lx6		  V6R2014   IR-234604V6R2014 		 NHIV6R215-039037: Lifecycle states of RMT object should be supported
 														 with different icons. 
06-JUN-13  jx5		  V6R2013	IR-230904V6R2014		 STP: In CATIA, "Open In Table" command working is KO

18-JUl-13  lx6		  V6R2014x	IR-239404V6R2014x		 STP: Incorrect information is being displayed on Lock for Edit  
														 window when other User lock the requirement specification. 
13-MAR-14  zud	djh	 V6R2015x	HL Parameter under Requirement. Modified getAllHTMLSourceFromRTF() and added new functions 
														 
16-MAY-14  qyg        V6R2015x  IR-281776V6R2015x        Add method getRequirementIconsByDirection 

04-JUL-14  jx5		  V6R2015x  RMC Perfo				 Add method importContentTextFromExcel

23-JUL-14  hat1 djh   V6R2015x  Validation column added for Requirements and Test Cases in Structure Display view. Added NextTestExecutionScheduled() and LastCompletedTestExecution().  

13-AUG-14  hat1 zud   V6R2015x  Validation column added for Requirement Specification and Chapter in Structure Display view. Added percentagePassLastTE getTestCaseValidationCount(), getTestCaseValidationCountsDB(), percentagePassLastTE().  

17-SEP-14  hat1 zud   V6R2015x  IR-242335V6R2015             STP: Content in export to excel  requirement garbled in japanese language. 

08-OCT-14  hat1 zud   V6R2015x  IR-331758-3DEXPERIENCER2015x  STP: IE11 - Expand All on Req. Spec. Str. view gives XML Error.

10-OCT-14 ZUD IR-333259-3DEXPERIENCER2015x Parameter value displayed in "Content" cell contains unexpected "null" values 

16-OCT-14  hat1 zud   V6R2015x  IR-326368-3DEXPERIENCER2015x    NHIV6R2015x-45210: Test Case validation status is not accessible at specification structure display page. 

16-OCT-14  hat1 zud   V6R2015x  IR-326341-3DEXPERIENCER2015x  	STP:  Data of content field of custom requirement is displayed in Validation column. Modified percentagePassLastTE().
 
12-DEC-14  lx6	      V6R2016   IR-327057-3DEXPERIENCER2016 : In the "compare structure" window , some columns value appears in english, while the browser is in french

22-DEC-14  hat1 zud   V6R2016   HL Requirement Specification Dependency.

13-JAN-15  KIE1 ZUD   IR-333259-3DEXPERIENCER2016 Parameter value displayed in "Content" cell contains unexpected "null" values

20-APR-15  LX6  XXX   FUN054695 ENOVIA GOV TRM Revision refactoring

27-APR-15  ZUD HAT1   IR-364087-3DEXPERIENCER2016x : All target Req spec for existing dependency is not available in the traceability authoring command. Removal of hardcoded Req-Spec SubTypes value. Function getDerivedRequirementSpecificationRelationship() used to get rel name.

04-MAR-15  HAT1 ZUD   Relationship direction modification for covered and refined requirements.

29-MAY-15  JX5		  Make getTreeDisplaySettings static
09-JUN-15   KIE1 ZUD  IR-362506-3DEXPERIENCER2016x : R417-033503: In "Parameter Edit View", invalid value for parameter display blank alert which is un-closable.
03-JUL-15  JX5 		  Handle current state in getColumnHTML

15-JUL-15  HAT1 ZUD  IR-381800-3DEXPERIENCER2016x - Solving inconsistency with create link command is KO.  

27-JUL-15  JX5       IR-352417-3DEXPERIENCER2016x : TRM object which are released does not display user friendly message as its displayed in form of error. 

04-AUG-15  HAT1 ZUD  LA Settings for ReqSpec Dependency HL

04-AUG-15  KIE1 ZUD LA functionality for TRM Adoption Of ECM

19-AUG-15  HAT1 ZUD    IR-363246-3DEXPERIENCER2016x FUN048478:No option available to remove the target Requirement Specification from Create likn to cover\refine requirement pop upso that persistent dependencies can be deleted.

24-AUG-15  KIE1 ZUD  IR-386290-3DEXPERIENCER2016x: PLM Parameter migration

10-SEP-15  JX5  T94  IR-390868-3DEXPERIENCER2016x : Req Edition, Content text tooltip not displayed properly 
20-JAN-15  QYG       IR-420478-3DEXPERIENCER2016x: RCO checkin KO 
29-JAN-15  QYG       IR-421740-3DEXPERIENCER2017x: Saving RTF data corrupts "Content Data" attribute 

03-FEB-16  HAT1 ZUD  HL -  To enable Content column for Test Cases.

16-FEB-16  HAT1 ZUD  (xHTML editor for Use case.) To enable Content column for Test Cases.

16-FEB-24  KIE1 ZUD   HL Hide OOTB useless inherited types

03-MAY-28  HAT1 ZUD   Populating title as per autoName of Name in Web form.

01-JUN-16  HAT1 ZUD   IR-445639-3DEXPERIENCER2017x: R419-STP: "Autoname" check box is not available on Creation form of Requirement objects.

14-JUN-16  JX5  QYG   Move getTreeDisplaySettings code to REQModeler framework
19-JUN-16  JX5  QYG   IR-451954-3DEXPERIENCER2017x: needs to update timestampts of objectList
19-JUL-16  KIE1 ZUD   IR-448762-3DEXPERIENCER2017x: Tree preferences not applicable on Structure browser

22-JULY-16 HAT1 ZUD   IR-455208-3DEXPERIENCER2017x: R2017x-After doing the NLS transaltion for customized Requirement type, it wont show the translated name in title field.

24-AUG-16 KIE1 HAT1 IR-466458-3DEXPERIENCER2017x: R419-STP: Code is displayed for Requirement specification "Type" in Properties. 

20-SEP-16 HAT1 ZUD  OOXML to HTML conversion. : new background job method added ooxmlToHTML()
12-DEC-16 KIE1 ZUD :IR-486350-3DEXPERIENCER2017x: Issue view BOM certificat Manufacturing/Production / 3DEXPERIENCE R2016x FP.CFA.1646 / LINUX-Red Hat 5.5
05-DEC-16 KIE1 ZUD :IR-479554-3DEXPERIENCER2017x: R419-STP: Parameter edition is KO on Structure view.
15-MAR-17 HAT1 ZUD  :IR-506738-3DEXPERIENCER2018x: R419-STP: Test Execution is not counted in Requirement status column.  
05-APR-17 HAT1 ZUD  :TSK3433119: ENOVIA GOV TRM Modify Title on just created object in "Req Struct Editor" widget
29-MAY-17 HAT1 ZUD	:IR-481137-3DEXPERIENCER2018x: R419-STP: In Requirement overview option unrequited "Mass promote & Mass demote" option are present in "tools". Method added: showTypeClmRMTFullTraceabilityTable().    
06-JUN-17 KIE1 ZUD :IR-517222-3DEXPERIENCER2017x : There is no Select All option in Requirement Specifications Structure Compare page

 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import matrix.db.Attribute;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectItr;
import matrix.db.BusinessObjectList;
import matrix.db.BusinessObjectWithSelect;
import matrix.db.BusinessObjectWithSelectItr;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.db.Relationship;
import matrix.db.RelationshipWithSelectList;
import matrix.db.State;
import matrix.db.StateList;
import matrix.util.MatrixException;
import matrix.util.StringList;

import org.apache.axis.encoding.Base64;

import com.dassault_systemes.enovia.webapps.richeditor.RichTextStream;
import com.dassault_systemes.enovia.webapps.richeditor.converter.ConversionUtil;
import com.dassault_systemes.enovia.webapps.richeditor.util.IRichEditUtil;
import com.dassault_systemes.enovia.webapps.richeditor.util.ReferenceDocumentUtil;
import com.dassault_systemes.enovia.webapps.richeditor.util.RichEditFactory;
import com.dassault_systemes.enovia.webapps.richeditor.util.RichEditUtil;
import com.dassault_systemes.knowledge_itfs.IKweDictionary;
import com.dassault_systemes.knowledge_itfs.IKweList;
import com.dassault_systemes.knowledge_itfs.IKweValue;
import com.dassault_systemes.knowledge_itfs.IKweValueFactory;
import com.dassault_systemes.knowledge_itfs.KweInterfacesServices;
import com.dassault_systemes.knowledge_itfs.KweTypes;
import com.dassault_systemes.parameter_interfaces.IPlmParameter;
import com.dassault_systemes.parameter_interfaces.IPlmParameter.PLMParm_ValuationType;
import com.dassault_systemes.parameter_interfaces.IPlmParameterDisplay;
import com.dassault_systemes.parameter_interfaces.PLMParm_RangeStatus;
import com.dassault_systemes.parameter_interfaces.PLMParm_ValuationStatus;
import com.dassault_systemes.parameter_interfaces.ParameterInterfacesServices;
import com.dassault_systemes.parameter_interfaces.ParameterTypes;
import com.dassault_systemes.requirements.PreferenceServices;
import com.dassault_systemes.requirements.ReqConstants;
import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.dassault_systemes.requirements.ReqStructureUtil;
import com.dassault_systemes.requirements.UnifiedAutonamingServices;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.framework.lifecycle.LifeCyclePolicyDetails;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIStructureCompare;
import com.matrixone.apps.framework.ui.UITableCommon;
import com.matrixone.apps.framework.ui.UIToolbar;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.apps.requirements.RequirementsCommon;
import com.matrixone.apps.requirements.RequirementsUtil;
import com.matrixone.apps.requirements.convertor.EConvertorSettings.DefaultConvertorVersion;
import com.matrixone.apps.requirements.convertor.engine.util.ConvertedDataDecorator;
import com.matrixone.apps.requirements.convertor.engine.util.ImageUtil;
import com.matrixone.apps.requirements.ui.UITableRichText;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;


/**
 * This JPO class has some methods pertaining to the generic RMT usage
 * @author Brian Casto
 * @version RequirementManagement V6R2009x - Copyright (c) 2007, MatrixOne, Inc.
 */
public class emxRMTCommonBase_mxJPO extends emxDomainObject_mxJPO
{

//  Added:15-Apr-08:kyp:R207:RMT Bug 357905
   protected static final String SELECT_RESEVERED = "reserved";
   protected static final String SELECT_RESEVERED_BY = "reservedby";
   public static final String SYMBOLIC_STATE_RELEASE = "state_Release";
// End:R207:RMT Bug 357905

	public static final String PREFIX_ATT = "_att_";
	public static final String DEFAULT_ATT_NAME =  "Content Data";
	static final String EXTENSION = ".docx";
	public static final String FORMAT_DOC = "doc";

   /**
    * Create a new emxRMTCommonBase object.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return a emxRMTCommonBase object.
    * @throws Exception if the operation fails
    * @since RequirementManagement V6R2009x
    * @grade 0
    */
   public emxRMTCommonBase_mxJPO(Context context, String[] args) throws Exception
   {
      super(context, args);
   }


   /**
    * Main entry point.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @return an integer status code (0 = success)
    * @throws Exception if the operation fails
    * @since ProductCentral 10.0.0.0
    * @grade 0
    */
   public int mxMain (Context context, String[] args) throws Exception
   {
      if (!context.isConnected())
      {
         String language = context.getSession().getLanguage();
         String strContentLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.FeaturesCheckFailed"); 
         throw  new Exception(strContentLabel);
      }
      return(0);
   }

   /**
    * Return the Object message in HTML format
    * @param context the eMatrix <code>Context</code> object
    * @param args JPO arguments
    * @return String HTML output
    * @throws Exception if operation fails
    */
	public String getObjectMessageHTML(Context context, String[] args) throws Exception
    {
        Map info = (Map)JPO.unpackArgs(args);
        info.put("messageType", "html");
        com.matrixone.jdom.Document doc = getObjectMailXML(context, info);
        return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "html"));

    }

    /**
     * Return the Object message in TEXT format
     * @param context the eMatrix <code>Context</code> object
     * @param args JPO arguments
     * @return String HTML output
     * @throws Exception if operation fails
     */
    public String getObjectMessageText(Context context, String[] args) throws Exception
    {
        Map info = (Map)JPO.unpackArgs(args);
        info.put("messageType", "text");
        com.matrixone.jdom.Document doc = getObjectMailXML(context, info);
        return (emxSubscriptionUtil_mxJPO.getMessageBody(context, doc, "text"));

    }

	/**
	 * Return the Object message in XML format
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param info Map information of xml attributes
	 * @return null
	 * @throws Exception if operation fails
	 */
    public static com.matrixone.jdom.Document getObjectMailXML(Context context, Map info) throws Exception
    {
        String baseURL = (String)info.get("baseURL");
        String bundleName = (String)info.get("bundleName");
        String locale = ((Locale)info.get("locale")).toString();
        String messageType = (String)info.get("messageType");
        String objectId = (String)info.get("id");
        String notificationName = (String)info.get("notificationName");

        HashMap eventCmdMap = UIMenu.getCommand(context, notificationName);
        String eventName = UIComponent.getSetting(eventCmdMap, "Event Type");
        String eventKey = "emxRequirements.Event." + eventName.replaceAll(" ", "") + ".Message";
        if(eventKey.equals("emxRequirements.Event.ObjectMajorRevised.Message")){
        	eventKey = "emxRequirements.Event.ObjectRevised.Message";
        }
        String i18NEvent = EnoviaResourceBundle.getProperty(context, bundleName, context.getLocale(), eventKey); 

        DomainObject object = DomainObject.newInstance(context, objectId);
        StringList selectList = new StringList(3);
        selectList.addElement(SELECT_TYPE);
        selectList.addElement(SELECT_NAME);
        selectList.addElement(SELECT_REVISION);
        Map objectInfo = object.getInfo(context, selectList);
        String objectType = (String)objectInfo.get(SELECT_TYPE);
        String i18NobjectType = UINavigatorUtil.getAdminI18NString("type", objectType, locale);
        String objectName = (String)objectInfo.get(SELECT_NAME);
        String objectRev = (String)objectInfo.get(SELECT_REVISION);

		String[] headerValues = new String[4];
		headerValues[0] = i18NobjectType;
		headerValues[1] = objectName;
		headerValues[2] = objectRev;
		headerValues[3] = i18NEvent;
		String header = MessageUtil.getMessage(context, null, "emxRequirements.Event.Mail.Header", headerValues, null, context.getLocale(), bundleName);
		HashMap headerInfo = new HashMap();
        headerInfo.put("header", header);

        ArrayList dataLineInfo = new ArrayList();
		if (messageType.equalsIgnoreCase("html"))
		{
			String[] messageValues = new String[4];
			messageValues[0] = baseURL + "?objectId=" + objectId;
			messageValues[1] = i18NobjectType;
			messageValues[2] = objectName;
			messageValues[3] = objectRev;
			String viewLink = MessageUtil.getMessage(context,null,
													 "emxRequirements.Event.Html.Mail.ViewLink",
													 messageValues,null,
													 context.getLocale(),bundleName);
			dataLineInfo.add(viewLink);

		}
		else
		{
			String[] messageValues = new String[3];
			messageValues[0] = i18NobjectType;
			messageValues[1] = objectName;
			messageValues[2] = objectRev;
			String viewLink = MessageUtil.getMessage(context,null,
													 "emxRequirements.Event.Text.Mail.ViewLink",
													 messageValues,null,
													 context.getLocale(),bundleName);

			dataLineInfo.add(viewLink);
			dataLineInfo.add(baseURL + "?objectId=" + objectId);
		}

		HashMap footerInfo = new HashMap();
        footerInfo.put("dataLines", dataLineInfo);

        return (emxSubscriptionUtil_mxJPO.prepareMailXML(context, headerInfo, null, footerInfo));
    }



	/** Trigger Method to fire a notification on a spec if the structure has been changed
	 * @param context the eMatrix <code>Context</code> object
	 * @param args holds the Hashmap containing the parent id.
	 * @returns void nothing
	 * @throws Exception if the operation fails
	 * @since RequirementsManagement V6R2009x
	 *
	 */
    public void specStructureModified(Context context, String[] args)
        throws Exception
    {
        String strObjectId = args[0];

        try
        {
			DomainObject domObj = DomainObject.newInstance(context,strObjectId);

			List lstSpecTypes = ProductLineUtil.getChildrenTypes(context, ReqSchemaUtil.getRequirementSpecificationType(context));
			lstSpecTypes.add(ReqSchemaUtil.getRequirementSpecificationType(context));

			//check if domObj is a spec
			String thisObjType = domObj.getType(context);
			if (lstSpecTypes.contains(thisObjType))
			{
				//${CLASS:emxNotificationUtil}.objectNotification(context, strObjectId, "RMTSpecStructureModifiedEvent", null);
				emxNotificationUtilBase_mxJPO.objectNotification(context, strObjectId, "RMTSpecStructureModifiedEvent", null);
				return;
			}

			String strRelPattern = ReqSchemaUtil.getSpecStructureRelationship(context);
			StringList lstObjSelects = new StringList(2);
			lstObjSelects.add(SELECT_ID);
			lstObjSelects.add(SELECT_TYPE);
			boolean bGetTo = true;
            boolean bGetFrom = false;
            short sRecursionLevel = -1;

            MapList mapParentObjects = domObj.getRelatedObjects(context, strRelPattern, QUERY_WILDCARD,
                  lstObjSelects, null, bGetTo, bGetFrom, sRecursionLevel, null, null);


			for(int i=0; i<mapParentObjects.size(); i++)
            {
                Map mapT=(Map)mapParentObjects.get(i);
                String strObjType = (String)mapT.get(SELECT_TYPE);

                 if (lstSpecTypes.contains(strObjType))
				 {
					String strObjId = (String)mapT.get(SELECT_ID);
					//${CLASS:emxNotificationUtil}.objectNotification(context, strObjId, "RMTSpecStructureModifiedEvent", null);
					emxNotificationUtilBase_mxJPO.objectNotification(context, strObjId, "RMTSpecStructureModifiedEvent", null);
				 }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }

    }




	/**  Method to handle import of Content Data from Excel
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - holds the Hashmap containing the parent id, attribute name, and attribute value
	 * @returns void nothing
	 * @throws Exception if the operation fails
	 * @since RequirementsManagement V6R2009x
	 */
    public void importContentDataFromExcel(Context context, String[] args)
        throws Exception
    {
        String strObjectId = args[0];
		String strAttValue = args[2];

        try
        {
			DomainObject domObj = DomainObject.newInstance(context,strObjectId);

			domObj.setAttributeValue(context, "Content Text", strAttValue);

			//call utility function to take make text ready to be content data
			String contentDataValue = UITableRichText.compressAndEncode(strAttValue);
            domObj.setAttributeValue(context, "Content Data", contentDataValue);
			domObj.setAttributeValue(context, "Content Type", "rtf.gz.b64");
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
            throw new FrameworkException(e.getMessage());
        }

    }
    
    //JX5 : Added for rest web services
    /**  Method to handle import of Content Text from Excel
	 * @param context - the eMatrix <code>Context</code> object
	 * @param args - holds the Hashmap containing the parent id, attribute name, and attribute value
	 * @returns void nothing
	 * @throws Exception if the operation fails
	 * @since RequirementsManagement 3DExperience-R2015x
	 */
    public boolean importContentTextFromExcel(Context context, String[] args) throws Exception{
    	
    	String strObjectId = args[0];
    	String strContentData = args[1];
    	String strContentText = args[2];
    	boolean isContentProcessed = false;
    	
    	try{
    		
    		if(!strContentText.equalsIgnoreCase("") && strContentData.equalsIgnoreCase("")){
    			//If we only have content text, we need to convert it for content data attribute
    			DomainObject obj = DomainObject.newInstance(context,strObjectId);
    			
    			String contentDataValue = UITableRichText.compressAndEncode(strContentText);
    			
    			obj.setAttributeValue(context, "Content Text", strContentText);
    			obj.setAttributeValue(context, "Content Data", contentDataValue);
    			obj.setAttributeValue(context, "Content Type", "rtf.gz.b64");
    			
    			isContentProcessed = true;		
    		}
    			
    	}
    	catch(Exception e){
    		isContentProcessed = false;
    	}
    	
    	return isContentProcessed;
    }

//  Added:15-Apr-08:kyp:R207:RMT Bug 357905
    /**
     * Checks whether current object is reserved, and if reserved then is the context user the reserving user
     *
     * This is configured as check triggers for following event:
     * ChangeName   ChangeOwner ChangePolicy    ChangeVault Checkin Connect Delete  Disconnect  Modify Attribute    Modify Description  ChangeType  RemoveFile
     *
     * These triggers are configured on following types:
     * Requirement Specification, Requirement, Chapter, Comment
     *
     * @param context The Matrix Context object
     * @param args The trigger arguments. Following should be the contents of this array
     *          args[0] : The object id of the object being checked for reservation
     * @return 0: If the object is not reserved or it is reserved by the context user itself
     *         1: If the object is reseved by some other user.
     * @throws MatrixException if operation fails.
     */
	public int triggerCheckReservedObject(Context context, String[] args) throws MatrixException {
        final int ALLOW_MODIFICATION = 0;
        final int DENY_MODIFICATION = 1;
        try {
            // Argument check
            if (context == null) {
                throw new IllegalArgumentException("context");
            }
            if (args == null || args.length < 1) {
                throw new IllegalArgumentException("args");
            }

            //Get error message
            String strLanguage = context.getSession().getLanguage();
            String strErrorMessage = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.ReservedObject.CannotModifyThisObject"); 

            int nReturn = ALLOW_MODIFICATION;
            String strObjectId = args[0];

            // Find reservation information on this object
            StringList slBusSelect = new StringList();
            slBusSelect.add(SELECT_TYPE);
            slBusSelect.add(SELECT_NAME);
            slBusSelect.add(SELECT_REVISION);
            slBusSelect.add(SELECT_RESEVERED);
            slBusSelect.add(SELECT_RESEVERED_BY);

            DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
            Map mapObjectInfo = dmoObject.getInfo(context, slBusSelect);

            String strType = (String)mapObjectInfo.get(SELECT_TYPE);
            String strName = (String)mapObjectInfo.get(SELECT_NAME);
            String strRevision = (String)mapObjectInfo.get(SELECT_REVISION);
            boolean isReserved = "true".equalsIgnoreCase((String)mapObjectInfo.get(SELECT_RESEVERED));
            String strReservedBy = (String)mapObjectInfo.get(SELECT_RESEVERED_BY);

            //bug 375355: allow User Agent to pass through, for now.
            if (!isReserved || (isReserved && context.getUser().equals(strReservedBy)) || "User Agent".equals(context.getUser())) {
                return ALLOW_MODIFICATION;
            }

            // Process error message to fill in object details and reserving user name
            String strReservingUserName = PersonUtil.getFullName(context, strReservedBy);

            strErrorMessage = FrameworkUtil.findAndReplace(strErrorMessage, "$<type>", strType);
            strErrorMessage = FrameworkUtil.findAndReplace(strErrorMessage, "$<name>", strName);
            strErrorMessage = FrameworkUtil.findAndReplace(strErrorMessage, "$<revision>", strRevision);
            strErrorMessage = FrameworkUtil.findAndReplace(strErrorMessage, "$<username>", strReservingUserName);

            emxContextUtil_mxJPO.mqlError(context, strErrorMessage);

            return DENY_MODIFICATION;
        }
        catch (Exception exp) {
            exp.printStackTrace();
            throw new MatrixException(exp);
        }
    }
//  End:R207:RMT Bug 357905

	/**
	 * Method is to check is Program Central installed or not
	 * @param context the eMatrix <code>Context</code> object
	 * @param args JPO arguments
	 * @throws Exception if operation fails
	 * @return boolean value true or false
	 */
	public static boolean isPRGInstalled(Context context, String[] args) throws Exception
	{
		return FrameworkUtil.isSuiteRegistered(context, "appVersionProgramCentral", false, null, null);
	}

	/**
	 * Method is to check is VPLM installed or not
	 * @param context the eMatrix <code>Context</code> object
	 * @param args JPO arguments
	 * @throws Exception if operation fails
	 * @return boolean value true or false
	 */
	public static boolean isVPMInstalled(Context context, String[] args) throws Exception
	{
		return true;
	}

	/**
	 * Method is to check is Child Requirement is to create or not
	 * @param context the eMatrix <code>Context</code> object
	 * @param args JPO arguments
	 * @throws Exception if operation fails
	 * @return boolean value true or false
	 */
	public static boolean isChildRequirementCreation(Context context, String[] args) throws Exception
	{
		HashMap requestMap = (HashMap) JPO.unpackArgs(args);
		return "true".equalsIgnoreCase((String)requestMap.get("isChildCreation"));
	}

	/**
	 * Method is to get Hidden fields from JSP
	 * @param context the eMatrix <code>Context</code> object
	 * @param args JPO arguments
	 * @throws Exception if operation fails
	 * @return Object in xml format
	 */
	public static Object getHiddenField(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		//String objectId = (String)programMap.get("objectId");
		Map fieldMap = (Map)programMap.get("fieldMap");
		String fieldName = (String)fieldMap.get("name");
		return "<input type=\"hidden\" name=\"" + fieldName + "\" value=\"\" />";
	}

	private static String escape(String text)
	{
		if(text != null){
			text = text.replaceAll("\"", "\\\\\"");
			text = text.replaceAll("\n", "\\\\n");
			return text;
		}else{
			return "";
		}
	}
	/**
	 * Method is used to return default Sub and Derived Requirement of the selected objectId
	 *
	 * @param context  the eMatrix <code>Context</code> object
	 * @param args  JPO arguments
	 * @return Object in xml format
	 * @throws Exception if operation fails
	 */
	public static Object populateDefaultsForSubAndDerived(Context context, String[] args) throws Exception
	{
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		Map requestMap = (Map)programMap.get("requestMap");
		StringBuffer sb = new StringBuffer();

		String emxTableRowId = (String)requestMap.get("emxTableRowId");
		if(emxTableRowId != null){
			sb.append("<script type=\"text/javascript\">\n");
			//sb.append("//<![CDATA[\n"); //this doesn't help the problem with special characters

			sb.append("function selectOption(fieldName, fieldValue)\n");
			sb.append("{\n");
				sb.append("var field = document.getElementsByName(fieldName)[0];\n");
				sb.append("for(var i = 0; i != field.length; i++){\n"); //avoid to use any '<' , '>', or '&' character in the function
					sb.append("if(field.options[i].value == fieldValue){\n");
						sb.append("field.selectedIndex = i;\n");
						sb.append("break;\n");
					sb.append("}\n");
				sb.append("}\n");
			sb.append("}\n");

			String parentReqId = emxTableRowId.split("[|]", -1)[1];
			DomainObject domObj = new DomainObject(parentReqId);
			String sDefault = "";

			Map attrMap = (Map) domObj.getAttributeMap(context);

			sDefault = (String)attrMap.get(ReqSchemaUtil.getPriorityAttribute(context));
			sb.append("selectOption(\"" + "Priority" + "\", \"" + sDefault + "\");\n");

			sDefault = (String)attrMap.get(ReqSchemaUtil.getDifficultyAttribute(context));
			sb.append("selectOption(\"" + "Difficulty" + "\", \"" + sDefault + "\");\n");

			sDefault = (String)attrMap.get(ReqSchemaUtil.getRequirementClassificationAttribute(context));
			sb.append("selectOption(\"" + "Classification" + "\", \"" + sDefault + "\");\n");

			//sDefault = (String)attrMap.get(ReqSchemaUtil.getSynopsisAttribute(context));
			//sb.append("document.getElementsByName('" + "Synopsis" + "')[0].innerHTML = \"" + sDefault + "\";\n");

			//sDefault = (String)attrMap.get(ReqSchemaUtil.getNotesAttribute(context));
			//sb.append("document.getElementsByName('" + "Notes" + "')[0].innerHTML = \"" + sDefault + "\";\n");

			//sDefault = (String)attrMap.get(ReqSchemaUtil.getEstimatedCostAttribute(context));
			//sb.append("document.getElementsByName('" + "Cost" + "')[0].innerHTML = '" + sDefault + "';");

			//sDefault = (String)attrMap.get(ReqSchemaUtil.getSponsoringCustomerAttribute(context));
			//sb.append("document.getElementsByName('" + "Customer" + "')[0].innerHTML = '" + sDefault + "';");

			sDefault = (String)attrMap.get(ReqSchemaUtil.getRequirementCategoryAttribute(context));
			sb.append("document.getElementsByName('" + "Requirement Category" + "')[0].value = \"" + 
			XSSUtil.encodeForJavaScript(context, sDefault) + "\";\n");

			sDefault = (String)attrMap.get(ReqSchemaUtil.getTitleAttribute(context));
			sb.append("document.getElementsByName('" + "Title" + "')[0].value = \"" + 
			XSSUtil.encodeForJavaScript(context, sDefault) + "\";\n");

			//sb.append("//]]>\n");
			sb.append("</script>");

			//sDefault = (String)attrMap.get(ReqSchemaUtil.getUserRequirementImportanceAttribute(context));
			//sb.append("<input type=\"hidden\" name=\"" + ReqSchemaUtil.getUserRequirementImportanceAttribute(context) + "\" value=\"" + sDefault + "\" />");

			//sDefault = (String)attrMap.get(ReqSchemaUtil.getDesignatedUserAttribute(context));
			//sb.append("<input type=\"hidden\" name=\"" + ReqSchemaUtil.getDesignatedUserAttribute(context) + "\" value=\"" + sDefault + "\" />");
		}
		return sb.toString();
	}

    /**
     *  Get Maplist containing Revisions Info for Id passed In
     *  Used for Revision Summary Page in RMTRevisions command
     *  revision column
     *
     *  @param context the eMatrix <code>Context</code> object
     *  @param args an array of String arguments for this method
     *  @return MapList containing Revisions Info
     *  @throws Exception if the operation fails
     *
     * @since R2012.HF7
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRevisions(Context context, String[] args) throws Exception
    {
    	 HashMap map = (HashMap) JPO.unpackArgs(args);
         String       objectId = (String) map.get("objectId");
         MapList revisionsList = null;

         StringList busSelects = new StringList(1);
         busSelects.add(DomainObject.SELECT_ID);

		 boolean isMajor = RequirementsCommon.isMajorPolicy(context, objectId);

		 if(isMajor)
		 {
			 // Major
			 revisionsList = getMajorRevisionsInfo(context,busSelects,new StringList(0), objectId);
		 }
		 else
		 {
			 // Minor
			 DomainObject busObj   = DomainObject.newInstance(context, objectId);
			 revisionsList = busObj.getRevisionsInfo(context,busSelects,new StringList(0));
		 }

    	 return revisionsList;
    }

    /**
     * Get the revisions for an object along with select data
     *
     * @param context the eMatrix <code>Context</code> object
     * @param singleValueSelects the eMatrix <code>StringList</code> object that holds the list of selects that return a single value, for example owner
     * @param multiValueSelects the eMatrix <code>StringList</code> object that holds the list of selects that return multiple values, for example revisions[]
     * @return a MapList
     * @throws FrameworkException if the operation fails
     * @since R2012.HF7
     */
    public MapList getMajorRevisionsInfo(Context context, StringList singleValueSelects, StringList multiValueSelects, String objectId)  throws FrameworkException
    {
    	MapList returnMapList = new MapList();

        int i;
        int singleSelectSize = singleValueSelects.size();
        int multiSelectSize = multiValueSelects.size();
        StringList selects = new StringList(1);

        // Add single select to main select list.
        for (i = 0;i < singleSelectSize;i++)
            selects.addElement(singleValueSelects.elementAt(i));

        // Add multiple select to main select list.
        for (i = 0;i < multiSelectSize;i++)
            selects.addElement(multiValueSelects.elementAt(i));

        try
        {

        	BusinessObjectList boList = getMajorRevisionList(context, objectId);
            BusinessObjectItr boItr = new BusinessObjectItr(boList);
            String objOids[] = new String[boList.size()];
            int oidCnt = 0;

            BusinessObject bo;
            while (boItr.next()) {
                bo = boItr.obj();
                objOids[oidCnt++] = bo.getObjectId(context);
            }
            boItr.reset();

            BusinessObjectWithSelectList selectObjList = BusinessObject.getSelectBusinessObjectData(context,objOids, selects);
            BusinessObjectWithSelectItr selectObjItr = new BusinessObjectWithSelectItr(selectObjList);

            // Iterate through select objects and 'plain' objects.
            // select objects have select data, 'plain' objects have TNR.
            BusinessObjectWithSelect selectBO;
            while (selectObjItr.next()) {
                boItr.next();
                selectBO = selectObjItr.obj();
                bo = boItr.obj();

                HashMap objMap = new HashMap();
                // put TNR
                objMap.put(SELECT_TYPE, bo.getTypeName());
                objMap.put(SELECT_NAME, bo.getName());
                objMap.put(SELECT_REVISION, bo.getRevision());
                objMap.put(SELECT_ID, bo.getObjectId());

                // Add single values to Map
                String key, value;
                for (i = 0;i < singleSelectSize;i++) {
                  key = (String)singleValueSelects.elementAt(i);
                  value = selectBO.getSelectData(key);
                  objMap.put(key, value);
                }

                // Add multiple values to Map
                StringList valueList;
                for (i = 0;i < multiSelectSize;i++) {
                  key = (String)multiValueSelects.elementAt(i);
                  valueList = selectBO.getSelectDataList(key);
                  objMap.put(key, valueList);
                }

                // Add object Map to MapList
                returnMapList.add(objMap);
            }
        }
        catch (Exception e)
        {
            throw (new FrameworkException(e));
        }

        return returnMapList;

    }

   /**
    * get list of major revisions.
    * @param context the eMatrix <code>Context</code> object
    * @param objectId object Id
    * @return list of major revision objects
    * @throws Exception if the operation fails
    */
    public BusinessObjectList getMajorRevisionList(Context context, String objectId) throws Exception
    {
    	  BusinessObjectList objects = new BusinessObjectList();
    	  MapList revisionsList = new MapList();
    	  String data = MqlUtil.mqlCommand(context, "PRINT BUS $1 SELECT $2 DUMP $3", objectId, "majorids[].bestsofar.id", "~");
    	  StringList rows = FrameworkUtil.split(data, "~");
    	    int numPairs = rows.size();
    	    for (int i= 0 ; i < numPairs; i++) {
    	        String objId =  (String) rows.get(i);
    	        DomainObject doSourceECR = new DomainObject(objId);
    	        objects.add(doSourceECR);
    	    }
    	  return objects;
    }


    /**
     * Method shows higher revision Icon if a higher major or minor revision of the object exists
     * based on its policy definition
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return List - returns the program HTML output
     * @throws Exception if the operation fails
     * @since R2012.HF7
     */
     public List getHigherRevisionIcon(Context context, String[] args) throws Exception
     {
     String ICON_TOOLTIP_HIGHER_REVISION_EXISTS = "emxRequirements.Form.Label.HigherRev";
     String RESOURCE_BUNDLE_PRODUCTS_STR = "emxRequirementsStringResource";
     String OBJECT_LIST = "objectList";

     Map programMap = (HashMap) JPO.unpackArgs(args);
     MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);

     int iNumOfObjects = relBusObjPageList.size();
     // The List to be returned
     List lstHigherRevExists= new Vector(iNumOfObjects);
     String arrObjId[] = new String[iNumOfObjects];

     int iCount;
     //Getting the bus ids for objects in the table
     for (iCount = 0; iCount < iNumOfObjects; iCount++) {
         Object obj = relBusObjPageList.get(iCount);
         arrObjId[iCount] = (String)((Map)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
     }

     //Reading the tooltip from property file.
     String strTooltipHigherRevExists = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_PRODUCTS_STR, context.getLocale(), ICON_TOOLTIP_HIGHER_REVISION_EXISTS);
         String strHigherRevisionIconTag= "";
         String strIcon = EnoviaResourceBundle.getProperty(context,
                         "emxComponents.HigherRevisionImage");
     //Iterating through the list of objects to generate the program HTML output for each object in the table
         for (iCount = 0; iCount < iNumOfObjects; iCount++) {
                 if(!isLastRevision(context, arrObjId[iCount])){
                 strHigherRevisionIconTag =
                         "<img src=\"../common/images/"
                             + strIcon
                             + "\" border=\"0\"  align=\"middle\" "
                             + "TITLE=\""
                             + " "
                             + strTooltipHigherRevExists
                             + "\""
                             + "/>";
                 }else{
                 strHigherRevisionIconTag = " ";
                 }
             lstHigherRevExists.add(strHigherRevisionIconTag);
         }
     return lstHigherRevExists;
     }

     
     
     
     /**
      * Method shows higher revision Icon in the object property page if a higher revision of the object exists
      * @param context the eMatrix <code>Context</code> object
      * @return String - returns the program HTML output
      * @throws Exception if the operation fails
      * @since R2012x.HF4
      */
      public String getHigherRevisionIconProperty(Context context, String[] args) throws Exception{

      String ICON_TOOLTIP_HIGHER_REVISION_EXISTS = "emxProduct.Revision.ToolTipHigherRevExists";
      String RESOURCE_BUNDLE_PRODUCTS_STR = "emxProductLineStringResource";
      Map programMap = (HashMap) JPO.unpackArgs(args);
      Map relBusObjPageList = (HashMap) programMap.get("paramMap");
      String strObjectId = (String)relBusObjPageList.get("objectId");

      //String Buffer to display the Higher revision field in Req property page.
      StringBuffer sbHigherRevisionExists = new StringBuffer(100);
      String strHigherRevisionExists = "";

      //Reading the tooltip from property file.
      String strTooltipHigherRevExists = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_PRODUCTS_STR, context.getLocale(), ICON_TOOLTIP_HIGHER_REVISION_EXISTS);

          String strHigherRevisionIconTag= "";
          DomainObject domObj = DomainObject.newInstance(context, strObjectId);

      // Begin of Add by Enovia MatrixOne for Bug 300775 Date 03/25/2005
      String strNo  = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_PRODUCTS_STR, context.getLocale(), "emxProduct.Label.No"); 
      String strYes = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_PRODUCTS_STR, context.getLocale(), "emxProduct.Label.Yes"); 
      // End of Add by Enovia MatrixOne for Bug 300775 Date 03/25/2005

      //To generate the program HTML output for the context object

          if(!isLastRevision(context, strObjectId))
          {

                  strHigherRevisionIconTag =
                      "<a HREF=\"#\" TITLE=\""
                              + " "
                              + strTooltipHigherRevExists
                              + "\">"
                              + emxPLCCommonBase_mxJPO.HIGHER_REVISION_ICON
                              + "</a>";
                  sbHigherRevisionExists.append(strHigherRevisionIconTag);
                  // Modified by Enovia MatrixOne for Bug 300775 Date 03/25/2005
                  sbHigherRevisionExists.append(strYes);
                  strHigherRevisionExists = sbHigherRevisionExists.toString();

          }else{
                  // Modified by Enovia MatrixOne for Bug 300775 Date 03/25/2005
                  sbHigherRevisionExists.append(strNo);
                  strHigherRevisionExists = sbHigherRevisionExists.toString();

               }

       return strHigherRevisionExists;
      }

      /**
      * Check to see if this is the last revision of the object.
      *
      * @param context the eMatrix <code>Context</code> object
      * @return a boolean indicating whether it is true or false
      * @throws FrameworkException if the operation fails
      * @since R2012.HF7
      */
     public boolean isLastRevision(Context context, String objectId)
         throws FrameworkException
     {
    	 boolean isContextPushed = false;
         try
         {
             ContextUtil.pushContext(context);
             isContextPushed = true;
             DomainObject lastRevision = RequirementsCommon.getLastRevision(context, objectId);
             String lastObjectId = lastRevision.getId(context);
             return objectId.equals(lastObjectId) || objectId.equals(lastRevision.getInfo(context, "physicalid"));
         }
         catch (Exception e)
         {
             throw (new FrameworkException(e));
         }
         finally
         {
             if(isContextPushed)
             {
                ContextUtil.popContext(context);
             }
         }
     }
	 
// Start JX5 : IR-218082V6R2014 STP: RMT UI Changes for 3D Experience.
	 /**
    *  This function returns the Icon of RMT Objects
    *
    * @param context  the eMatrix <code>Context</code> object
    * @return         MapList <objectID, IconName>
    */
	public static MapList getRequirementIcons(Context context, String[] args) throws Exception
	{
//long start = System.currentTimeMillis();		
		MapList iconList = new MapList();
		try
		{
			//unpack the incoming arguments
			HashMap inputMap = (HashMap)JPO.unpackArgs(args);
			//get the objectList MapList from the tableData hashMap
			MapList objectList = (MapList) inputMap.get("objectList");
			String bArr[] = new String[objectList.size()];
			String rArr[] = new String[objectList.size()];
			StringList bSel = new StringList();
			RelationshipWithSelectList rwsl = null;
			String IconName = "";
			String relType = "";
			//to handle the case where there is no relationship
			Boolean emptyRelArray=false;
			
			// Creation of businessObjectId & relId Array
			for(int i=0;i<objectList.size();i++)
			{
				bArr[i] = (String)((Map)objectList.get(i)).get(DomainConstants.SELECT_ID);
				rArr[i] = (String)((Map)objectList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
				if(rArr[i] == null || rArr[i].equals("null") || rArr[i].equals(""))
				{
					emptyRelArray=true;
				}
				
			}
			
			// Creation of Selection StringList
			bSel.add(DomainConstants.SELECT_TYPE);
			bSel.add(DomainRelationship.SELECT_TYPE);
			
			//retrive bus with select
			// N.B.:It is impossible to have no business object ID
			BusinessObjectWithSelectList bwsl   = BusinessObject.getSelectBusinessObjectData(context,bArr,bSel);
			
			//retrive rel with select
			// N.B.: It is possible that an object has no relationship
			if(!emptyRelArray)
			{
				rwsl		= Relationship.getSelectRelationshipData(context,rArr,bSel);
			}
			
			//Processing all objects from objectList
			for(int i=0; i<objectList.size();i++)
			{
			
				// Retrieve the type of the object
				String busType = bwsl.getElement(i).getSelectData(DomainConstants.SELECT_TYPE);
				
				// Retrieve the type of the relationship
				if(!emptyRelArray)
				{
					relType  = rwsl.getElement(i).getSelectData(DomainRelationship.SELECT_TYPE);
				}
				else
				{
					relType = "null";
				}
				
				
				HashMap Map =  new HashMap();
				Map objectMap 				= (Map)objectList.get(i);
				String currentObjectId 		= (String)objectMap.get(DomainConstants.SELECT_ID);
				Boolean isKindOfRequirement = false;
				if((String)objectMap.get("kindof") != null) {
					isKindOfRequirement = RequirementsUtil.getRequirementType(context).equals(objectMap.get("kindof"));
				}
				else{
					//IR-233755V6R2014
					DomainObject obj			= DomainObject.newInstance(context,currentObjectId);
					isKindOfRequirement = obj.isKindOf(context, "Requirement");
					//
				}
				
				
				//In the case of a Requirement	and its derived types			
				if(isKindOfRequirement && relType!=null && !relType.equals("") &&!relType.equals("null"))
				{									
					IconName = getRelIconProperty(context, relType, busType);
					Map.put(currentObjectId, IconName);	
				}
				else
				{//For all other objects we use the standar method
					IconName = UINavigatorUtil.getTypeIconProperty(context, busType);
					Map.put(currentObjectId, IconName);
				}
				iconList.add(Map);
			}
		}catch (Exception ex) {
            System.out.println(" Error while getting custom RMT icons : " + ex.toString());
        }	
//System.out.println("###icon data: " + (System.currentTimeMillis() - start));
		return iconList;
	}
	/**
	 * returns icons for source requirements column in traceability report
	 * @param context the eMatrix <code>Context</code> object
	 * @param args
	 * @return  MapList <objectID, IconName>
	 * @throws Exception
	 */
    public static MapList getSourceRequirementIconsByDirection(Context context, String[] args) throws Exception
    {
        return getRequirementIconsByDirection(context, args, true);
    }
    
    /**
     * returns icons for target requirements column in traceability report
     * @param context
     * @param args
     * @return  MapList <objectID, IconName>
     * @throws Exception
     */
    public static MapList getTargetRequirementIconsByDirection(Context context, String[] args) throws Exception
    {
        return getRequirementIconsByDirection(context, args, false);
    }
	
    /**
     * returns icons for requirements column in traceability report
     * @param context
     * @param args
     * @param isSourceObject whether it's source requirement column or not
     * @return MapList <objectID, IconName>
     * @throws Exception
     */
    protected static MapList getRequirementIconsByDirection(Context context, String[] args , boolean isSourceObject) throws Exception
    {
        MapList iconList = new MapList();
        try
        {
            //unpack the incoming arguments
            HashMap inputMap = (HashMap)JPO.unpackArgs(args);
            //get the objectList MapList from the tableData hashMap
            MapList objectList = (MapList) inputMap.get("objectList");
            String bArr[] = new String[objectList.size()];
            String rArr[] = new String[objectList.size()];
            StringList bSel = new StringList();
            RelationshipWithSelectList rwsl = null;
            String IconName = "";
            String relType = "";
            
            // Creation of businessObjectId & relId Array
            for(int i=0;i<objectList.size();i++)
            {
                bArr[i] = (String)((Map)objectList.get(i)).get(DomainConstants.SELECT_ID);
                rArr[i] = (String)((Map)objectList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                
            }
            
            // Creation of Selection StringList
            bSel.add(DomainConstants.SELECT_TYPE);
            bSel.add(DomainRelationship.SELECT_TYPE);
            
            //retrive bus with select
            // N.B.:It is impossible to have no business object ID
            BusinessObjectWithSelectList bwsl   = BusinessObject.getSelectBusinessObjectData(context,bArr,bSel);
            
            //retrive rel with select
            // N.B.: It is possible that an object has no relationship
            rwsl        = Relationship.getSelectRelationshipData(context,rArr,bSel);
            
            //Processing all objects from objectList
            for(int i=0; i<objectList.size();i++)
            {
                
            
                // Retrieve the type of the object
                String busType = bwsl.getElement(i).getSelectData(DomainConstants.SELECT_TYPE);
                
                // Retrieve the type of the relationship
                relType  = rwsl.getElement(i).getSelectData(DomainRelationship.SELECT_TYPE);
                
                HashMap Map =  new HashMap();
                Map objectMap               = (Map)objectList.get(i);
                String currentObjectId      = (String)objectMap.get(DomainConstants.SELECT_ID);
                String direction            = (String)objectMap.get("direction");
                //
                boolean isFromTarget = "<--".equals(direction);
                //assume all objects in the list are Requirements
                if (!isFromTarget && isSourceObject || isFromTarget && !isSourceObject)
                {
                    IconName = UINavigatorUtil.getTypeIconProperty(context, busType);
                }
                else
                {
                    IconName = getRelIconProperty(context, relType, busType);
                }
                
                Map.put(currentObjectId, IconName);
                iconList.add(Map);
            }
        }catch (Exception ex) {
            System.out.println(" Error while getting custom RMT icons : " + ex.toString());
        }   
        return iconList;
    }
    	/**
    *  This function gets the Icon file name for any given relationship
    *  from the emxRequirements.properties file
    *
    * @param context  the eMatrix <code>Context</code> object
    * @param rel     object relationship name
    * @return         String - icon name
    */
    public static String getRelIconProperty(Context context, String rel, String busType) throws Exception
    {
        String icon = "";
        String relRegistered = "";

        try {
            if (rel != null && rel.length() > 0 )
            {
                String propertyKey = "";
                String propertyKeyPrefix = "emxRequirements.SmallIcon.";
				String defaultPropertyKey = "emxRequirements.SmallIcon.relationship_SpecificationStructure";//"emxFramework.smallIcon.defaultType";

                // Get the symbolic name for the relationship passed in
                relRegistered = FrameworkUtil.getAliasForAdmin(context, "relationship", rel, true);
			
                if (relRegistered != null && relRegistered.length() > 0 )
                {
                    propertyKey = propertyKeyPrefix + relRegistered.trim();

                    try {
                        //icon = EnoviaResourceBundle.getProperty(context, propertyKey);
                    	icon = EnoviaResourceBundle.getProperty(context,"emxRequirements",context.getLocale(),propertyKey);
                    	//EnoviaResourceBundle.getProperty
                    } catch (Exception e1) {
                    	icon = UINavigatorUtil.getTypeIconProperty(context, busType);
                    }
                    if( icon == null || icon.length() == 0 ||icon.equalsIgnoreCase(propertyKey))
                    {
                        // If no icons found, return a default icon for propery file.
                        icon = EnoviaResourceBundle.getProperty(context, defaultPropertyKey);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(" Error getting relationship icon name : " + ex.toString());
        }

        return icon;
    }
	
// End JX5
//START LX6    
    /**
     * Display the lifeCycle in form.
     *
     * @param context the eMatrix <code>Context</code> object
     * @return a string which represent the url of the lifeCycle Dialog 
     * @throws Exception if the operation fails
     * @since R2014
     */
    public static String fieldLifecycle(Context context, String[] args) throws Exception {

    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	HashMap paramMap = (HashMap) programMap.get("paramMap");
    	HashMap fieldMap = (HashMap) programMap.get("fieldMap");
    	HashMap settingsMap = (HashMap) fieldMap.get("settings");

    	String sHeight = (String) settingsMap.get("height");
    	if (null == sHeight || "".equals(sHeight)) {
    		sHeight = "60";
    	}
    	String sOID = (String) paramMap.get("objectId");

    	String sResult = "<object id='gnvLifecycle' type='text/html'";
    	sResult += "data='../common/emxLifecycleDialog.jsp?export=false&toolbar=AEFLifecycleMenuToolBar&objectId=" + sOID + "&header=emxFramework.Lifecycle.LifeCyclePageHeading&mode=basic'";
    	sResult += "width='100%' height='" + sHeight + "' style='overflow:none;padding:0px;margin:0px'></object>";

    	return sResult;

    }

    /**
     * Check if the form is on Edit Mode
     *
     * @param context the eMatrix <code>Context</code> object
     * @return boolean to display or not the lifeCyle 
     * @throws Exception if the operation fails
     * @since R2014
     */
    public static Boolean checkViewMode(Context context, String[] args) throws Exception {

    	Boolean bViewMode = new Boolean(true);
    	HashMap requestMap = (HashMap) JPO.unpackArgs(args);
    	bViewMode = true;

    	try {
    		String sMode = (String) requestMap.get("mode");
    		if (sMode.equals("edit")) {
    			bViewMode = false;
    		}
    		if (sMode.equals("")) {
    			sMode = (String) requestMap.get("editLink");
    			if (!sMode.equals("")) {
    				bViewMode = true;
    			}
    		}
    	} catch (Exception e) {
    	}

    	return bViewMode;
    } 
    
    /**
     * get the appropriate icon for the lock field
     *
     * @param context the eMatrix <code>Context</code> object
     * @return a string which represent the html code for an Image
     * @throws Exception if the operation fails
     * @since R2014
     */
    public static String getLockIcon(Context context, String[] args) throws Exception
    {
    	Map programMap = (HashMap) JPO.unpackArgs(args);
	    Map paramMap = (HashMap)programMap.get("paramMap");
	    String strObjectId = (String)paramMap.get("objectId");
	    StringList selectStmts = new StringList("reserved");
	    selectStmts.addElement("reservedby");
	    selectStmts.addElement("reservedcomment");
	    selectStmts.addElement("reservedstart");
	    DomainObject domObj = DomainObject.newInstance(context, strObjectId);
	    Map ReservedInfo = domObj.getInfo(context,selectStmts);
	    String reserved = (String)ReservedInfo.get("reserved");
	    String strImage="";
	    String strDifficultyIconTag = "";
	    String User = context.getUser();
	    if(reserved.equalsIgnoreCase("true"))
	    {
	    	String reservedby = (String)ReservedInfo.get("reservedby");
	    	if(User.equalsIgnoreCase(reservedby))
	        {
	    		strImage= EnoviaResourceBundle.getProperty(context,"emxRequirements.Icon.padLockReservedByMySelf");
	        }
        	else
        	{
        		strImage= EnoviaResourceBundle.getProperty(context,"emxRequirements.Icon.padLockReservedByOther");
        	}    	
	    	String strLockedBy = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxFramework.Basic.ReservedBy");
	    	String toolTip = strLockedBy + " " +reservedby+ " \n";
	    	strDifficultyIconTag = 
	        	"<img src=\"" + strImage + "\""
	            + " border=\"0\"  align=\"middle\" "
	            + "title=\""
	            + " "
	            + toolTip
	            + "\""
	            + "/>";
	    }
	    else
	    { 

	    	strDifficultyIconTag = "";
	    }
        return strDifficultyIconTag;
    }
    

    /**
     * get the column html data for an icon column or styled column
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return a List of string which represent the column html data
     * @throws Exception if the operation fails
     * @since R2014
     */
    public List getColumnHTML(Context context, String[] args) throws Exception {
        String RESOURCE_BUNDLE_PRODUCTS_STR = "emxRequirementsStringResource";
        String OBJECT_LIST = "objectList";
        Map programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
        
        HashMap paramList = (HashMap) programMap.get("paramList");
        String strExport = (String) paramList.get("exportFormat");
        boolean toExport = false;
        if (strExport != null) 
                toExport = true;
        
        int iNumOfObjects = relBusObjPageList.size();
        String arrObjId[] = new String[iNumOfObjects];
        List columnTags = new Vector(iNumOfObjects);
        int iCount;
        // Getting the bus ids for objects in the table
        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            arrObjId[iCount] = (String) ((Map) relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
        }
        
        HashMap columnMap = (HashMap) programMap.get("columnMap");
        Map Settings = (Map)columnMap.get("settings");
        boolean useStyle = "true".equalsIgnoreCase((String)Settings.get("Use Style"));
        String select = (String) (columnMap.containsKey("expression_businessobject")?
                columnMap.get("expression_businessobject"):
                columnMap.get("expression_relationship"));
        StringList selects = new StringList();
        selects.addElement(select);   

        MapList columnData = DomainObject.getInfo(context, arrObjId, selects);
        String attributeName = select;
        int attrind = select.indexOf("attribute[");
        if(attrind >=0)
        {
        	attributeName = select.substring(attrind + 10, select.indexOf("]", attrind));
        	attributeName = attributeName.replaceAll(" ", "_");
        }
        
        // Iterating through the list of objects to generate the program HTML
        // output for each object in the table
        StringList policySelect = new StringList();
        policySelect.addElement(DomainObject.SELECT_POLICY);
        MapList policyData  = DomainObject.getInfo(context, arrObjId, policySelect);
        String denied = EnoviaResourceBundle.getProperty(context, ReqConstants.BUNDLE_FRAMEWORK , context.getLocale(), "emxFramework.Basic.DENIED");
        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            Map cell = (Map)columnData.get(iCount);
            HashMap policyMap = (HashMap)policyData.get(iCount);
            String policy	 = (String)policyMap.get(DomainObject.SELECT_POLICY);
            String strValue = (String)cell.get(select);
            String strHTMLTag = "";
            if (strValue != null && strValue.length() > 0) {
                if(ReqConstants.DENIED.equalsIgnoreCase(strValue)){
                    strHTMLTag = denied;
                }else{
                	
                	String i18nProperty = "emxFramework.Range." + attributeName + "." + strValue;
                	//JX5 start : handle current for unified maturity state
                	if(select.equalsIgnoreCase("current")){
                		DomainObject curObj = new DomainObject(arrObjId[iCount]);
                		policy = policy.replaceAll(" ", "_");
                		i18nProperty = "emxFramework.State."+policy+"."+strValue;
                	}
                	//JX5 start : handle current for unified maturity state
                	
                    String strDisplayValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",
                            context.getLocale(), i18nProperty);
                    if(toExport){
                    	strHTMLTag = strDisplayValue;
                    }
                    else{
                    	if(useStyle){
                    		
                    		 String propertyKey = "";
                    		 String Style = "";
                    		 //JX5 start : handle maturity state mapping
                    		if(select.equalsIgnoreCase("current")){
                    			
                    			//String mappedState = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",new Locale("english"), i18nProperty);
                    			propertyKey = "emxRequirements.Range.State." + strValue.replaceAll(" ", "_");
                    			Style = EnoviaResourceBundle.getProperty(context, propertyKey);
                    			Style = "font-weight:bold;color:"+Style;
                    			
                    		}
                    		else{
                    			propertyKey = "emxRequirements.TextStyle." + attributeName + "." + strValue;
                    			Style = EnoviaResourceBundle.getProperty(context, propertyKey);
                    		}
                    		//JX5 end : handle maturity state mapping
                           
                            strHTMLTag = "<span style=\"" + Style + "\">" + strDisplayValue + "</span>"; 
                    	}
                    	else{
    		                String propertyKey = "emxRequirements.Icon." + attributeName + "." + strValue;
    		                String strIcon = EnoviaResourceBundle.getProperty(context, propertyKey);
    		                strHTMLTag = "<img src=\"" + strIcon + "\"" + "  border=\"0\"  align=\"middle\" " + "title=\""
    		                        + strDisplayValue + "\"" + "/>" + "<span style=\"display:none\">" + strDisplayValue + "</span>";
                    	}
                    }
                }
            }
            columnTags.add(strHTMLTag);
        }
        return columnTags;
    }
    
    /**
     * get the filed html data for a styled form field
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return a string which represent the field html data
     * @throws Exception if the operation fails
     * @since R2014
     */
    public String getFieldHTML(Context context, String[] args) throws Exception
    {
	    Map programMap = (HashMap) JPO.unpackArgs(args);
	    Map paramMap = (HashMap)programMap.get("paramMap");
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strMode =  (String) requestMap.get("mode");
    	String strObjectId =  (String)paramMap.get("objectId");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        Map Settings = (Map)fieldMap.get("settings");
        boolean useStyle = "true".equalsIgnoreCase((String)Settings.get("Use Style"));
        String select = (String)fieldMap.get("expression_businessobject");
        if(select == null){
        	select = (String)fieldMap.get("expression_relationship");
        }

        DomainObject domObj = DomainObject.newInstance(context, strObjectId);
        String strValue = domObj.getInfo(context, select.trim());
    	
        String attributeName = select;
        int attrind = select.indexOf("attribute[");
        if(attrind >=0)
        {
        	attributeName = select.substring(attrind + 10, select.indexOf("]", attrind));
        	attributeName = attributeName.replaceAll(" ", "_");
        }
        String i18nProperty = "emxFramework.Range." + attributeName + "." + strValue;
        String strDisplayValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",
                context.getLocale(), i18nProperty);
    	if(strMode != null && strMode.equalsIgnoreCase("Edit"))
    	{
    		//
    	}
    	else
    	{
            
            if(useStyle){
                String propertyKey = "emxRequirements.TextStyle." + attributeName + "." + strValue;
	    		String strStyle = EnoviaResourceBundle.getProperty(context, propertyKey);
	    		strDisplayValue = "<span style=\""+strStyle+"\">"+strDisplayValue+"</span>";
            }
    	}
        return strDisplayValue;
	   
    }
    
    /**
     * get the range value for a Program or ProgramHTMLOutput form field
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return a Map which represent the internal and translated range values
     * @throws Exception if the operation fails
     * @since R2014
     */
    public Object getFieldRangeValues(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap paramMap = (HashMap) programMap.get("paramMap");
        HashMap fieldMap = (HashMap) programMap.get("fieldMap");
        String select = (String)fieldMap.get("expression_businessobject");
        if(select == null){
        	select = (String)fieldMap.get("expression_relationship");
        }
        String attributeName = select;
        int attrind = select.indexOf("attribute[");
        if(attrind >=0)
        {
        	attributeName = select.substring(attrind + 10, select.indexOf("]", attrind));
        }

        StringList fieldRangeValues = new StringList();
        StringList fieldRangeValuesDisplay = new StringList();
        StringList Ranges = RequirementsCommon.getAttributeRange(context, attributeName);
    	attributeName = attributeName.replaceAll(" ", "_");
        Iterator itr = Ranges.iterator();
        while (itr.hasNext()) {
            String rangeValue = (String) itr.next();
            String i18nProperty = "emxFramework.Range." + attributeName + "." + rangeValue;
            String strDisplayValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",
                    context.getLocale(), i18nProperty);

            fieldRangeValues.addElement(rangeValue);
            fieldRangeValuesDisplay.addElement(strDisplayValue);
        }
        HashMap<String, StringList> mapRangeValues = new HashMap<String, StringList>();

        mapRangeValues.put("field_choices", fieldRangeValues);
        mapRangeValues.put("field_display_choices", fieldRangeValuesDisplay);
        return mapRangeValues;
    }

    
    /**
     * Update the object attribute value for a Program or ProgramHTMLOutput column cell. 
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return 0 for success
     * @throws Exception if the operation fails
     * @since R2014
     */
    public int updateProgramObjectAttribute(Context context, String[] args) throws Exception
    {
    	return updateProgramAttribute(context, args, true, true);
    }

    /**
     * Update the object attribute value for a Program or ProgramHTMLOutput form field. 
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return 0 for success
     * @throws Exception if the operation fails
     * @since R2014
     */
    public int updateProgramObjectField(Context context, String[] args) throws Exception
    {
    	return updateProgramAttribute(context, args, true, false);
    }

    /**
     * Update the relationship attribute value for a Program or ProgramHTMLOutput column cell. 
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return 0 for success
     * @throws Exception if the operation fails
     * @since R2014
     */
    public int updateProgramRelAttribute(Context context, String[] args) throws Exception
    {
    	return updateProgramAttribute(context, args, false, true);
    }
    
    /**
     * Update the attribute value for a Program or ProgramHTMLOutput column cell or form field. 
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @param args isBus true if the attribute is for business object; false for relationship
     * @param args isColumn true if it's for a column cell; false for a form field
     * @return 0 for success
     * @throws Exception if the operation fails
     * @since R2014
     */
    public int updateProgramAttribute(Context context, String[] args, boolean isBus, boolean isColumn) throws Exception
    {
	    Map programMap = (HashMap) JPO.unpackArgs(args);
	    Map paramMap = (HashMap)programMap.get("paramMap");
        HashMap columnMap = (HashMap) programMap.get(isColumn ? "columnMap" : "fieldMap");
        //boolean boColumn = columnMap.containsKey("expression_businessobject"); //bps bug
        String select = (String)columnMap.get("expression_businessobject");
        if(select == null){
        	select = (String)columnMap.get("expression_relationship");
        }

        String attributeName = select;
        int attrind = select.indexOf("attribute[");
        if(attrind >=0)
        {
        	attributeName = select.substring(attrind + 10, select.indexOf("]", attrind));
        }

	    String newValue = (String)paramMap.get("New Value");
	    if(isBus){
	        String strObjectId = (String)paramMap.get("objectId");
	        DomainObject domObj = DomainObject.newInstance(context, strObjectId);
	        

	        try{
	        	domObj.setAttributeValue(context, attributeName, newValue);
	        }catch(Exception ex){
	        	
	        	//JX5 start : check if the object is in release state
	        	StringList selects =  new StringList();
	        	selects.add(DomainConstants.SELECT_CURRENT);
	        	selects.add(DomainConstants.SELECT_POLICY);
	        	Map result = domObj.getInfo(context, selects);
	        	String strObjCurrent = (String)result.get(DomainConstants.SELECT_CURRENT);
	        	String strObjPolicy = (String)result.get(DomainConstants.SELECT_POLICY);
		        String strSymbolicCurrent = FrameworkUtil.reverseLookupStateName(context,strObjPolicy,strObjCurrent);
		        
		        if(SYMBOLIC_STATE_RELEASE.equalsIgnoreCase(strSymbolicCurrent)){
		        	String strLanguage = context.getSession().getLanguage();
		        	String strErrMssg = EnoviaResourceBundle.getProperty(context,"Requirements","emxRequirements.Alert.ObjectInReleasedState",strLanguage);
		        	 throw new FrameworkException(strErrMssg);
		        }
		        //JX5 end : check if the object is in release state
	        }
	        
	    }else{
			String strRelId = (String)paramMap.get("relId");
	    	DomainRelationship.newInstance(context, strRelId).setAttributeValue(context, attributeName, newValue);
	    }
	   
	    return 0;
    }
    

    /**
     * Check if the Object has a next state or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @return a boolean 
     * @throws Exception if the operation fails
     * @since R2014
     */
    public boolean hasNextState(Context context, String[] args) throws Exception 
    {

    	HashMap programMap = (HashMap) JPO.unpackArgs(args);	 
    	String objectId =  (String)programMap.get("objectId");	
    	boolean isEnabled = false;
    	if(objectId != null && objectId.length() > 0)
    	{
    		DomainObject busObject = new DomainObject(objectId);
    		String policyName = busObject.getPolicy(context).getName();
    		StateList statesList  = LifeCyclePolicyDetails.getStateList(context, busObject, policyName);
    		String currentState = busObject.getInfo(context, DomainObject.SELECT_CURRENT);
    		int lastIndx=(statesList.size()) - 1;
    		isEnabled =  !((State)statesList.get(lastIndx)).isCurrent();
    	}
    	return isEnabled;   	

    }
    
    
    
    /**
     * Check if the Object has a previous state or not
     *
     * @param context the eMatrix <code>Context</code> object
     * @return a boolean 
     * @throws Exception if the operation fails
     * @since R2014
     */
    public boolean hasPreviousState(Context context, String[] args) throws Exception 
    {
	   	 HashMap programMap = (HashMap) JPO.unpackArgs(args);	 
	   	 String objectId =  (String)programMap.get("objectId");	 
	   	boolean isEnabled = false;
        if(objectId != null && objectId.length() > 0)
        {
        	DomainObject busObject = new DomainObject(objectId);
    		String policyName = busObject.getPolicy(context).getName();
    		StateList statesList  = LifeCyclePolicyDetails.getStateList(context, busObject, policyName);
    		String currentState = busObject.getInfo(context, DomainObject.SELECT_CURRENT);
    		isEnabled = !((State)statesList.get(0)).isCurrent();
       }
        return isEnabled;   	
    }
    
    /**
     * Check if the Policy field has to be displayed or hidden
     *
     * @param context the eMatrix <code>Context</code> object
     * @return a boolean 
     * @throws Exception if the operation fails
     * @since R2014
     */
    public static boolean showPolicyField(Context context, String[] args) throws Exception {
    	String isSimplified = EnoviaResourceBundle.getProperty(context, "emxRequirements.Preferences.Creation.isSimplifiedCreationForm");
    	Boolean displayField;
    	if(isSimplified == null || "".equals(isSimplified)|| "false".equals(isSimplified))
    	{
    		displayField = true;
    	}
    	else
    	{
    		displayField = false;
    	}
    	return displayField;
    	
    }
    
    private String getSequenceOrderList(Context context, String oid,String level, MapList ObjectlistMap) throws MatrixException
    {
    	int iCount;
    	int iNumOfObjects = ObjectlistMap.size();
    	String seqOrder = "";
    	for (iCount = 0; iCount < iNumOfObjects; iCount++) {
	        Object obj = ObjectlistMap.get(iCount);
	        String ObjectId = (String)((Map)ObjectlistMap.get(iCount)).get(DomainConstants.SELECT_ID);
	        String Objectlevel = (String)((Map)ObjectlistMap.get(iCount)).get("level");
	        if(Objectlevel==null)
	        {
	        	String relId = (String)((Map)ObjectlistMap.get(iCount)).get("id[connection]");
	        	Relationship rel = new DomainRelationship(relId);
	        	Objectlevel = String.valueOf(rel.getLevel());
	        }
	        if(Objectlevel.equalsIgnoreCase("0"))
	        {
	        	seqOrder = ""; 
	        }
	        else if(oid.equalsIgnoreCase(ObjectId)&&level.equalsIgnoreCase(Objectlevel))
	        {
	        	String previousLevel = Integer.toString((Integer.parseInt(Objectlevel)-1));
	        	String parentId = (String)((Map)ObjectlistMap.get(iCount)).get("from.id[connection]");
	        	seqOrder = (String)((Map)ObjectlistMap.get(iCount)).get("attribute[Sequence Order]");
	        	if(seqOrder!=null)
	        	{
	        		seqOrder = seqOrder+".";
	        		seqOrder = getSequenceOrderList(context, parentId,previousLevel, ObjectlistMap) + seqOrder;
	        	}
	        	else
	        	{
	        		String relId = (String)((Map)ObjectlistMap.get(iCount)).get("id[connection]");
	        		if(relId!=null&&relId.length()>0)
	        		{
		        		Relationship rel = new DomainRelationship(relId) ;
		        		Attribute seqAttrib = rel.getAttributeValues(context, RequirementsUtil.getSequenceOrderAttribute(context));
		        		seqOrder = seqAttrib.getValue();
	        		}
	        		else
	        		{
	        			seqOrder = "";
	        		}
	        	}
	        }
	        
	    }
    	return seqOrder;
    }
        
    public List getHigherAndActualRevisionIcon(Context context, String[] args) throws Exception
    {
   	 String ICON_TOOLTIP_HIGHER_REVISION_EXISTS = "emxRequirements.Form.Label.HigherRev";
        String RESOURCE_BUNDLE_PRODUCTS_STR = "emxRequirementsStringResource";
        String OBJECT_LIST = "objectList";
        String PARAM_LIST = "paramList";

        Map programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get(OBJECT_LIST);
        
        HashMap paramList = (HashMap) programMap.get(PARAM_LIST);
        String exportFormat = (String)paramList.get("exportFormat");
        int iNumOfObjects = relBusObjPageList.size();
        // The List to be returned
        List lstHigherRevExists= new Vector(iNumOfObjects);
        String arrObjId[] = new String[iNumOfObjects];

        int iCount;
        //Getting the bus ids for objects in the table
        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            Object obj = relBusObjPageList.get(iCount);
            arrObjId[iCount] = (String)((Map)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
        }

        StringList selects = new StringList();
        selects.addElement("majorid");
        selects.addElement("majorid.lastmajorid");
        selects.addElement(DomainConstants.SELECT_REVISION);
        MapList revData = DomainObject.getInfo(context, arrObjId, selects);
        
        //Reading the tooltip from property file.
        String strTooltipHigherRevExists = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_PRODUCTS_STR, context.getLocale(), ICON_TOOLTIP_HIGHER_REVISION_EXISTS);
        String strIcon = EnoviaResourceBundle.getProperty(context,
                        "emxComponents.HigherRevisionImage");
        String strHigherRevisionIconTag =
      		 	 "&#160;"
      			 +"<img src=\"../common/images/"
                   + strIcon
                   + "\" border=\"0\"  align=\"baseline\" "
                   + "TITLE=\""
                   + " "
                   + strTooltipHigherRevExists
                   + "\""
                   + "/>";
        //Iterating through the list of objects to generate the program HTML output for each object in the table
            for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            	Map m = (Map)revData.get(iCount);
           	 	String Revision = (String)m.get(DomainConstants.SELECT_REVISION);
           	 	String id = (String)m.get("majorid");
                    if(!id.equals(m.get("majorid.lastmajorid")) && exportFormat==null){
                    	lstHigherRevExists.add(Revision + strHigherRevisionIconTag);
                    }else{
                    	lstHigherRevExists.add(Revision);
                    }
            }
        return lstHigherRevExists;
    }
    public static HashMap getCustomCommands(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strlanguage = (String)requestMap.get("languageStr");
        
        HashMap hmpDynMenu = (HashMap)JPO.invoke(context, "emxCustomTableDynamicMenu", null, "getCustomCommands",
        		args, HashMap.class);
        
        MapList dynamicCmdOnMenu = UIToolbar.getChildren(hmpDynMenu);
        
    	HashMap SettingMap = new HashMap();
    	SettingMap.put("Image", "");
    	SettingMap.put("Registered Suite","Requirements");
    	
    	HashMap CheckedSettingMap = new HashMap();
    	CheckedSettingMap.put("Image", "../common/images/iconActionChecked.gif");
    	CheckedSettingMap.put("Registered Suite","Requirements");
       

        HashMap separatorMap = new HashMap();
        HashMap separatorSettingMap = new HashMap();
        separatorSettingMap.put("Registered Suite", "Framework");
        separatorSettingMap.put("Action Type", "Separator");
        separatorMap.put("type", "command");
        separatorMap.put("Name", "AEFSeparator");
        separatorMap.put("label", "AEFSeparator");
        separatorMap.put("description", "Use as separator for toolbar buttons");
        separatorMap.put("alt", "Separator");
        separatorMap.put("settings", separatorSettingMap);
        dynamicCmdOnMenu.add(separatorMap);
    	
		boolean tabularView = true; //UITableRichText.TABULAR_STYLE.equals(UITableRichText.getSCEDefaultViewStyle(session, context));
		String indentation = (String)requestMap.get("indent");
		if(indentation != null){
			tabularView = "tabular".equalsIgnoreCase(indentation);
		}

		String strCmdLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", new Locale(strlanguage), "emxRequirements.SCE.Settings.Label.Tabular");
    	HashMap indentMap = new HashMap();
		indentMap.put("type", "command");
		indentMap.put("label", strCmdLabel);
		indentMap.put("description", "Indentations");
		indentMap.put("href", "javascript:indentTo('tabular')");
		//if(tabularView){
		//	indentMap.put("settings",CheckedSettingMap);
		//}else{
			indentMap.put("settings",SettingMap);
		//}
		indentMap.put("roles",new StringList("all"));
		dynamicCmdOnMenu.add(indentMap);
		
		for(int i = 0; i<50; i+=40){ 
			indentMap = new HashMap();
			indentMap.put("type", "command");
			indentMap.put("label", i + "px");
			indentMap.put("description", "Indentations");
			indentMap.put("href", "javascript:indentTo('"+ i +"')");
			indentMap.put("settings",SettingMap);
			indentMap.put("roles",new StringList("all"));
			
			dynamicCmdOnMenu.add(indentMap);
		}
		
		
		if(!tabularView){
			dynamicCmdOnMenu.add(separatorMap);
			strCmdLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", new Locale(strlanguage), "emxRequirements.SCE.Label.NoAttributes");
			indentMap = new HashMap();
			indentMap.put("type", "command");
			indentMap.put("label", strCmdLabel);
			indentMap.put("description", "simple view");
			indentMap.put("href", "javascript:showSimpleView()");
			indentMap.put("settings",SettingMap);
			indentMap.put("roles",new StringList("all"));
			
			dynamicCmdOnMenu.add(indentMap);
		}

		return hmpDynMenu;
        

    }
    
    //END LX6    
    // ZUD Parameter Under Req Support
    /**
     * Returns the decorated HTML to be displayed in content cell of Parameter Node of Structure Browser
     * @param context - the eMatrix <code>Context</code> object
     * @param args - the inputMap who contains the table definition
     * @return a String containing HTML 
     */

    public String getParameterContentValue(Context context, String[] args ) throws Exception
    {
    	String strObjectIDtoExport = (String) JPO.unpackArgs(args);
    	String objectID = "";
    	String toExport = "";
    	if(strObjectIDtoExport.contains("|"))
    	{
    		String[] objectIDtoExport = strObjectIDtoExport.split("[|]", 2);
    	    objectID            = objectIDtoExport[0];
    	    toExport            = objectIDtoExport[1];
    	}
    	else
    	{
    		objectID = strObjectIDtoExport;
    	}

    	MapList paramMapList = new MapList();
    	Map<String, String> ParamMap = new HashMap<String, String>();
    	ParamMap.put("id", objectID);

    	paramMapList.add(ParamMap);

    	HashMap argsParamValue = new HashMap();
        argsParamValue.put("objectList", paramMapList);
    	
    	IPlmParameter plmPara = ParameterInterfacesServices.getParameterById(context,objectID);
    	IPlmParameterDisplay parmDisplay = (IPlmParameterDisplay)plmPara;
    
    	String paraType = plmPara.getDimension(context).getName();
    	String strParameterText = "";
    	String value = "";
    	PLMParm_ValuationType valuationType = plmPara.getValuationType(context);
		
    	if(paraType.equals(ParameterTypes.Boolean)){
    		
    		strParameterText += parmDisplay.getValueForDisplay(context);
    		
		}else if(paraType.equals(ParameterTypes.String)){
			
			if(valuationType.equals(PLMParm_ValuationType.MULTI)){
				IKweList multiValues = parmDisplay.getMultiValuesForDisplay(context);
				
					for (int i=1; i<=multiValues.GetSize(); i++){
						strParameterText +=multiValues.GetItem(i).asString();
						if(i < multiValues.GetSize())
							strParameterText +=",";
					}

			}else if(valuationType.equals(PLMParm_ValuationType.SIMPLE)){
				value = ""+parmDisplay.getValueForDisplay(context);	
			}
			if(!"null".equals(value) && !value.equals("")){
				strParameterText += value;
			}else{
        		strParameterText += "  ";
        	}
    	}else{
    		
    		PLMParm_RangeStatus minStatus = plmPara.getMinStatus(context);
        	PLMParm_RangeStatus maxStatus = plmPara.getMaxStatus(context);
        	
    		if(!minStatus.equals(PLMParm_RangeStatus.UNDEFINED)){
    			strParameterText += parmDisplay.getMinValueForDisplay(context);
    		}else{
    			strParameterText += "null";
    		}
        	
        	if(toExport.equalsIgnoreCase("true"))
        	{
            	strParameterText += " < ";
        	}
        	else
        	{
        	strParameterText += " &lt; ";
        	}
        	
        	if(valuationType.equals(PLMParm_ValuationType.SIMPLE))
        	{
        		value = ""+plmPara.getValue(context);
        	
	        	if(!"null".equals(value) && !value.equals(""))
	        	{
	        		strParameterText +=parmDisplay.getValueForDisplay(context);
	        	}else{
	        		strParameterText += value;
	        	}
        	}
        	else if(valuationType.equals(PLMParm_ValuationType.MULTI)){
        		IKweList multiValues = parmDisplay.getMultiValuesForDisplay(context);
        		
        		for (int i=1; i<=multiValues.GetSize(); i++){
					strParameterText +=multiValues.GetItem(i).asString();
					if(i < multiValues.GetSize())
						strParameterText +=",";
				}
        	}
        	else
        	{
	        		strParameterText += "null";
        	}
        	
        	if(toExport.equalsIgnoreCase("true"))
        	{
            	strParameterText += " < ";
        	}
        	else
        	{
        	strParameterText += " &lt; ";
        	}
        	
        	if(!maxStatus.equals(PLMParm_RangeStatus.UNDEFINED)){
        		strParameterText += parmDisplay.getMaxValueForDisplay(context);
        	}else{
        		strParameterText += "null";
        	}
    	}
    	
    	if(toExport.equalsIgnoreCase("true"))
    	{
    		return strParameterText;
    	}
    	
		String decoratedParamHTML =  getParameterDivDecorator(context,objectID,strParameterText, valuationType);
		return decoratedParamHTML;		
    	
    }
   
    //++ KIE1 ZUD  IR-386290-3DEXPERIENCER2016x
    // This function retrive the related unit of parameter for added new functionality 
    //(Change unit from parameter user table)
    
    public String getRelatedUnitsForparameter(Context context, String objectID ) throws Exception
    {
    	try
      {
     	MapList paramMapList = new MapList();
    		Map<String, String> ParamMap = new HashMap<String, String>();
    		ParamMap.put("id", objectID);
    		paramMapList.add(ParamMap);
    		HashMap argsParamValue = new HashMap();
    		argsParamValue.put("objectList", paramMapList);
    		
			IPlmParameter paramDO = ParameterInterfacesServices.getParameterById(context, objectID);
			
			String units = "";
			String unitValue = "";
			for(int i = 0; i < ParameterInterfacesServices.getRelatedUnits(context, paramDO.getDimension(context)).size(); i++)
			{
				String nlsName = ParameterInterfacesServices.getRelatedUnits(context, paramDO.getDimension(context)).get(i).getNLSName(context);
				String symbol = ParameterInterfacesServices.getRelatedUnits(context, paramDO.getDimension(context)).get(i).getSymbol();
				units += nlsName+":"+symbol+",";
			}
			
			return units;
    	}
    	catch(Exception ex)
    	{
    		String error = ex.toString();
    		return error;
    	}
   }
    /**
     * Sets the parameter Values edited by user.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - the inputMap who contains the table definition
     * @return int value for Sucsess or failure 
     */
    public String setParameterString(Context context, String[] args ) throws Exception
    { 
    	try
    	{
    		String[] Param_val = (String[]) JPO.unpackArgs(args);
    		String objectID = Param_val[0];
    		
    		String[] Param_items = Param_val[1].split("[<]");
    		String displayUnit = Param_val[2];
    		if(Param_items.length< 2 )   
    			return "Invalid Parameter Values";
    		
    		Param_items[0] = Param_items[0].trim();
    		String[] ParamMinValue = Param_items[0].split("[ ]");
    		Param_items[1] = Param_items[1].trim();
    		String[] ParamValue = Param_items[1].split("[ ]");
    		Param_items[2] = Param_items[2].trim();
    		String[] ParamMaxValue = Param_items[2].split("[ ]");
    		
    		String paramValues[] = new String[3];
    		paramValues[0] = ParamMinValue[0];
    		paramValues[1] = ParamValue[0];
    		paramValues[2] = ParamMaxValue[0];
    		
    		PLMParm_ValuationStatus status = null;
    		IPlmParameter plmPara = ParameterInterfacesServices.getParameterById(context, objectID);
    		String paraType = plmPara.getDimension(context).getName();
    		PLMParm_ValuationType valuationType = plmPara.getValuationType(context);
    		IKweDictionary kweDico = KweInterfacesServices.getKweDictionary();
    		IKweValueFactory kweFacto = KweInterfacesServices.getKweValueFactory();
    		
    		if(displayUnit.trim().length() > 0 && !displayUnit.isEmpty())
    		{
	    		if(paraType.equals("LENGTHParameter"))
	    		{
	    			paramValues = convertToMksValues(context, paramValues, displayUnit,  "m");
	    		}else if(paraType.equals("ANGLEParameter"))
	    		{
	    			paramValues = convertToMksValues(context, paramValues, displayUnit,  "rad");
	    		}else if(paraType.equals("MASSParameter"))
	    		{
	    			paramValues = convertToMksValues(context, paramValues, displayUnit,  "kg");
	    		}
    		}
    		
    		if(paraType.equals(ParameterTypes.Boolean)){
    			
    			plmPara.valuate(context, Boolean.parseBoolean(paramValues[1]));
    			
    		}else if(paraType.equals(ParameterTypes.String)){
    			String stringValue = "";
    			for(int i = 0; i < ParamValue.length; i++ )
    			{
    				stringValue +=ParamValue[i]+" ";
    			}
    			// ++KIE1 IR-479554-3DEXPERIENCER2017x 
    			if(valuationType.equals(PLMParm_ValuationType.MULTI))
    			{
    				String[] multiVal = stringValue.split(",");
    				StringList values1 = new StringList();
    				IKweValue listValue = kweDico.findType(context, KweTypes.List).createValue(context, null);
    				IKweList listValues = (IKweList)listValue.instanceValue();
    				
    				for (int i=1; i <= multiVal.length; i++)
    					
    					if(multiVal[i-1].trim().length() > 0)
    					listValues.InsertItem(i, kweFacto.createString(context, multiVal[i-1]));

    				plmPara.setMultiValues(context, listValues);
    				
    			}else{
    				plmPara.valuate(context, stringValue);	
    			}
    			
    			
    		}else{
    			PLMParm_RangeStatus maxRangeStatus = plmPara.getMaxStatus(context);
        		PLMParm_RangeStatus minRangeStatus = plmPara.getMinStatus(context);
        		
        		if(maxRangeStatus.equals(PLMParm_RangeStatus.UNDEFINED) && ParamMaxValue[0].length() > 0)
        		{
        			maxRangeStatus = PLMParm_RangeStatus.INCLUSIVE;
        		}
        		
        		if(minRangeStatus.equals(PLMParm_RangeStatus.UNDEFINED) && ParamMinValue[0].length() > 0)
        		{
        			minRangeStatus = PLMParm_RangeStatus.INCLUSIVE;
        		}
    			
    			if(ParamMaxValue[0].length() > 0 && ParamMinValue[0].length() > 0)
    			{
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, 0, PLMParm_RangeStatus.UNDEFINED);
    				
    				if(!valuationType.equals(PLMParm_ValuationType.MULTI))
    				{
    					if(ParamValue[0].length() > 0 )
            			{
        					plmPara.valuate(context, Double.parseDouble(paramValues[1]));
            			}
        			}
    				else
        			{
    					String[] multiVal = paramValues[1].split(",");
    					IKweValue listValue = kweDico.findType(context, KweTypes.List).createValue(context, null);
        				IKweList listValues = (IKweList)listValue.instanceValue();
        				
        				for (int i=1; i <= multiVal.length; i++)
        				{
        					if(multiVal[i-1].trim().length() > 0)
        					listValues.InsertItem(i, kweFacto.createString(context, multiVal[i-1]));
        				}
        				
        				plmPara.setMultiValues(context, listValues);
    				}
    				plmPara.setRange(context, Double.parseDouble(paramValues[0]), minRangeStatus, Double.parseDouble(paramValues[2]), maxRangeStatus);
    				plmPara.setRange(context, Double.parseDouble(paramValues[0]), minRangeStatus, Double.parseDouble(paramValues[2]), maxRangeStatus);
    			}
    			else if(ParamMaxValue[0].length() > 0)
    			{
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, 0, PLMParm_RangeStatus.UNDEFINED);
    				
    				if(!valuationType.equals(PLMParm_ValuationType.MULTI))
    				{
    					if(ParamValue[0].length() > 0 )
            			{
        					plmPara.valuate(context, Double.parseDouble(paramValues[1]));
            			}
        			}
    				else
        			{
    					String[] multiVal = paramValues[1].split(",");
    					IKweValue listValue = kweDico.findType(context, KweTypes.List).createValue(context, null);
        				IKweList listValues = (IKweList)listValue.instanceValue();
        				
        				for (int i=1; i <= multiVal.length; i++)
        				{
        					if(multiVal[i-1].trim().length() > 0)
        					listValues.InsertItem(i, kweFacto.createString(context, multiVal[i-1]));
        				}
        				
        				plmPara.setMultiValues(context, listValues);
    				}
    				
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, Double.parseDouble(paramValues[2]), maxRangeStatus);
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, Double.parseDouble(paramValues[2]), maxRangeStatus);	
				
    			}
    			else if(ParamMinValue[0].length() > 0)
    			{
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, 0, PLMParm_RangeStatus.UNDEFINED);
    				
    				if(!valuationType.equals(PLMParm_ValuationType.MULTI))
    				{
	    				if(ParamValue[0].length() > 0)
	        			{
	    					plmPara.valuate(context, Double.parseDouble(paramValues[1]));
	        			}
	    				}
    				else
        			{
    					String[] multiVal = paramValues[1].split(",");
    					IKweValue listValue = kweDico.findType(context, KweTypes.List).createValue(context, null);
        				IKweList listValues = (IKweList)listValue.instanceValue();
        				
        				for (int i=1; i <= multiVal.length; i++)
        				{
        					if(multiVal[i-1].trim().length() > 0)
        					listValues.InsertItem(i, kweFacto.createString(context, multiVal[i-1]));
        				}
        				
        				plmPara.setMultiValues(context, listValues);
    				}

    				plmPara.setRange(context, Double.parseDouble(paramValues[0]), minRangeStatus, 0, PLMParm_RangeStatus.UNDEFINED);
    				plmPara.setRange(context, Double.parseDouble(paramValues[0]), minRangeStatus, 0, PLMParm_RangeStatus.UNDEFINED);
				
    			}
    			else
    			{
    				if(!valuationType.equals(PLMParm_ValuationType.MULTI))
    				{
    					if(ParamValue[0].length() > 0 )
            			{
        					plmPara.valuate(context,  Double.parseDouble(paramValues[1]));
            			}else{
            				plmPara.unset(context);
            			}
        			
    				}
    				else
        			{
    					String[] multiVal = paramValues[1].split(",");
    					IKweValue listValue = kweDico.findType(context, KweTypes.List).createValue(context, null);
        				IKweList listValues = (IKweList)listValue.instanceValue();
        				
        				for (int i=1; i <= multiVal.length; i++)
        				{
        					if(multiVal[i-1].trim().length() > 0)
        					listValues.InsertItem(i, kweFacto.createString(context, multiVal[i-1]));
        				}
        				// --KIE1 IR-479554-3DEXPERIENCER2017x 
        				plmPara.setMultiValues(context, listValues);
    				}
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, 0, PLMParm_RangeStatus.UNDEFINED);
    				plmPara.setRange(context, 0, PLMParm_RangeStatus.UNDEFINED, 0, PLMParm_RangeStatus.UNDEFINED);
					
    			}
    			
    			boolean flag = plmPara.isValueSet(context);
    			
    			if(flag & displayUnit.trim().length() > 0){
    				
    				for (int i = 0; i < ParameterInterfacesServices.getRelatedUnits(context, plmPara.getDimension(context)).size(); i++)
    				{
    					String key = ParameterInterfacesServices.getRelatedUnits(context, plmPara.getDimension(context)).get(i).getSymbol();
    					
    					if(key.equals(displayUnit)){
    						plmPara.setDisplayUnit(context,ParameterInterfacesServices.getRelatedUnits(context, plmPara.getDimension(context)).get(i));
    					}
    				}
    			}
    		}
    		return "OK";
    	}
    	catch(Exception ex)
    	{ 
    		String error = ex.toString();
    		return error;
    	}
    }
   
    //Function for converting the table values in to MKS unit format for storing in DB. 
    private String[] convertToMksValues(Context context, String[] values, String displayUnit, String mksUnit) throws Exception {
    	
		String args[] = new String[3];
		args[0] = displayUnit;
		args[1] = mksUnit;
		ArrayList<Double> ret = null;
		
		for(int i = 0; i < values.length; i++)
		{
			String multiVal = "";
			if(!values[i].isEmpty())
			{
				args[2] = values[i]+"";
				if(args[2].contains(",")){
					// ++KIE1 IR-479554-3DEXPERIENCER2017x 
					String multiValues[] = args[2].split(",");
					for(int j = 0; j < multiValues.length; j++)
					{
						args[2] = multiValues[j];
						ret = (ArrayList<Double>) JPO.invoke(context, "emxParameter", null,
								"getConvertedValues", args, ArrayList.class);
						multiVal += ""+ret.get(0)+",";
					}
				}else{
					ret = (ArrayList<Double>) JPO.invoke(context, "emxParameter", null,
							"getConvertedValues", args, ArrayList.class);
					multiVal = ""+ret.get(0);;
				}
				values[i] = multiVal;
				// --KIE1 IR-479554-3DEXPERIENCER2017x 
			}
		}
		return values;
	}
    //-- KIE1 ZUD  IR-386290-3DEXPERIENCER2016x
	/**
     * Modifies Parameter values to HTML which is then displayed in Content cell of Structure Browser
     * @param context - the eMatrix <code>Context</code> object
     * @param objectID - to be included in HTML
     * @param Value - Parameter Content cell value
	 * @param valuationType 
     * @param  ParameterReturn
     * @return String  containing HTML 
     */
    public String getParameterDivDecorator(Context context, String objectID , String Value, PLMParm_ValuationType valuationType) throws Exception
    {
    	String Unit = getRelatedUnitsForparameter(context, objectID);
    	IPlmParameter plmPara = ParameterInterfacesServices.getParameterById(context,objectID);
    	// ++KIE1 IR-479554-3DEXPERIENCER2017x 
    	String paraType = plmPara.getDimension(context).getName();
    	String displayUnit = "";
    	
    	if(!Unit.equals("")){
    		displayUnit = plmPara.getDisplayUnit(context).getNLSName(context);	
    	}
    	
    	String DivDecoratedText = "";
    	String objectIdForDiv = "objectInfo_"+objectID.replace(".", "");
    	DivDecoratedText = "<div style=\"display:none;\" id= \""+ objectIdForDiv + "\">";
    	DivDecoratedText += "<div id='objectID'>" + objectID + "</div>";
    	DivDecoratedText+="<div id='objectType'>PlmParameter</div>";
    	DivDecoratedText+= "<div id='convertorVersion'>None</div>";
    	String Table_ID = "Table_"+objectID.replace(".", "");
    	
    	DomainObject dmoObj = DomainObject.newInstance(context, objectID);
        String strParameterTitile = dmoObj.getAttributeValue(context, "Title");
        DivDecoratedText += "<div title='Parameter Edit View : " + strParameterTitile +"| PlmParameter' style='display:none;' id= 'ParameterEditor_" + objectID.replace(".", "") + "'> " ;
    	DivDecoratedText += "</div>";
    	DivDecoratedText +="<div id='Values_"+ objectID.replace(".", "") +"'>"+Value+"</div>";
    	DivDecoratedText +="<div id='Unit_"+ objectID.replace(".", "") +"'>"+Unit+""+displayUnit+"</div>";
    	DivDecoratedText +="<div id='Type_"+ objectID.replace(".", "") +"'>"+paraType+"</div>";
    	DivDecoratedText += "</div>";
    	
    	
    	String displayValue = "";
    	if(paraType.equals(ParameterTypes.Boolean))
    	{	// always show boolean value in upper case
    		displayValue = "<b>"+Value.toUpperCase()+"</b>";
    	}
    	else
    	if(paraType.equals(ParameterTypes.String)){
    		
    		String multiVal[] = Value.split(",");
    			displayValue +="<select>";
    			for(String str : multiVal){
    				displayValue +="<option value="+str+">"+str+"</option>";
    			}
    			displayValue +="</select>";
    		    		
    	}else{

    		String[] splitValue;
    		if(Value.contains("null")) // For All null values make cell blank
        	{
        		Value = Value.replaceAll("null", "...");
        	}
        		splitValue = Value.split("&lt;");
        		if(!splitValue[0].trim().equals("...")){
        			displayValue +=splitValue[0]+" "+displayUnit+" < ";
        		}else{
        			displayValue += "";
        		}
        		if(valuationType.equals(PLMParm_ValuationType.MULTI)){
        			String multiVal[] = splitValue[1].split(",");
        			displayValue +="<select>";
        			for(String str : multiVal){
        				displayValue +="<option value="+str+">"+str+"</option>";
        			}
        			displayValue +="</select>";
        		}else{
	        		if(splitValue[1].trim().equals("...")){
	        			displayValue +="<b>"+splitValue[1]+"</b>";
	        		}else{
	        			displayValue +="<b>"+splitValue[1]+" "+displayUnit+"</b>";	
	        		}
        		}
        		
        		if(!splitValue[2].trim().equals("...")){
        			displayValue +=" < "+splitValue[2]+" "+displayUnit;
        		}else{
        			displayValue += "";
        		}
    	}
    	// -- Fix For IR-333259-3DEXPERIENCER2016
    	DivDecoratedText +="<div id='contentCell_"+ objectID.replace(".", "") +"' class='cke_contents cke_reset'>"+displayValue+"</div>";
		return DivDecoratedText;
    	
    }
    private void updateTimestamp(Context context, MapList objectList) throws Exception
    {
    	if(objectList.size() == 0) return;
        int iCount;
        String[] arrObjId = new String[objectList.size()];
        // Getting the bus ids for objects in the table
        for (iCount = 0; iCount < objectList.size(); iCount++) {
            arrObjId[iCount] = (String) ((Map) objectList.get(iCount)).get(DomainConstants.SELECT_ID);
        }
        StringList selects = new StringList();
        selects.addElement(DomainConstants.SELECT_MODIFIED);
        MapList columnData = DomainObject.getInfo(context, arrObjId, selects);
        for (iCount = 0; iCount < objectList.size(); iCount++) {
        	String t = (String)((Map)columnData.get(iCount)).get(DomainConstants.SELECT_MODIFIED);
        	((Map) objectList.get(iCount)).put(DomainConstants.SELECT_MODIFIED, t);
        }
    }
    /**
     * Returns the place holder to show the HTML from RTF
     * @param context - the eMatrix <code>Context</code> object
     * @param args - the inputMap who contains the table definition
     * @return a vector containaing the place holder
     * @throws Exception
     */
    public Vector getAllHTMLSourceFromRTF(Context context, String[] args) throws Exception {

        Map inputMap = (Map) JPO.unpackArgs(args);
        Vector returnVector = new Vector();

        MapList objectMap = (MapList) inputMap.get("objectList");
        HashMap paramList = (HashMap) inputMap.get("paramList");
        HashMap columnMap = (HashMap) inputMap.get("columnMap");
        HashMap columnMapSettings = (HashMap) columnMap.get("settings");
        
        String strExport = (String) paramList.get("exportFormat");
        boolean toExport = false;
        if (strExport != null) 
            toExport = true;
        
        boolean isEditable = true; // FIXME columnMapSettings.get("Editable").equals("true");
        boolean isSCE = (paramList.get("isFromSCE") != null && ((String) paramList.get("isFromSCE")).equals("true")) ? true : false;
        boolean isFromDloatingDiv = (paramList.get("fromFloatingDiv") != null && ((String) paramList.get("fromFloatingDiv")).equals("true")) ? true : false;
        
        String nameKey = null;
        nameKey = isFromDloatingDiv==true?"target.name":"name";
        String idKey = null;
        idKey = isFromDloatingDiv==true?"target.id":"id";
        
        updateTimestamp(context, objectMap);
        
        HashMap<String, String> returnMap = new HashMap<String, String>(objectMap.size());
        Iterator objectItr = objectMap.iterator();

        String denied = EnoviaResourceBundle.getProperty(context, ReqConstants.BUNDLE_FRAMEWORK , context.getLocale(), "emxFramework.Basic.DENIED");
        String reqSpecType = ReqSchemaUtil.getRequirementSpecificationType(context),
        		parameterType = ReqSchemaUtil.getPARParameterType(context),
        		chapterType = ReqSchemaUtil.getChapterType(context);
        MapList returnList = new MapList();
        while (objectItr.hasNext()) {
            try {
                // Get the information about the current row
                Map<String, String> curObjectMap = (Map) objectItr.next();
                String objectName = (String) curObjectMap.get(nameKey);
                String objectID = (String) curObjectMap.get(idKey);
                
                String kind = (String) curObjectMap.get("kindof");
   
                if(kind == null) {
                    DomainObject dmoObj = DomainObject.newInstance(context, objectID);
                	kind = dmoObj.getInfo(context, "type.kindof");
                	if("DOCUMENTS".equals(kind)) {
                		kind = ReqSchemaUtil.getRequirementSpecificationType(context);
                	}
                }
                
                // Special handler for Requirement Specification, we want to throw an event for the RichText
                if (reqSpecType.equals(kind)) {
                    returnVector.add(toExport != true ? "<div style='text-align:center;'>" +
                            "<img style='display: none; margin-left: auto; margin-right: auto;' "
                            + "src='images/loading.gif' /> - </div>" : " "); 
                    continue;
                }
                
                // We can process the RichText only if we have a Requirement or a Comment or a Parameter or a Test Case
                if (chapterType.equals(kind)) 
                {
                    returnVector.add(toExport != true ? "<div style='text-align:center;'> - </div>" : " "); 
                    continue;
                }

                String readAccess = (String)curObjectMap.get(ReqConstants.SELECT_READ_ACCESS);
                if(ReqConstants.DENIED.equals(readAccess)){
                    returnVector.add(toExport != true ? "<div style='text-align:center;'>" + denied + "</div>" : " "); 
                    continue;
                }

                // We get the timeStamp
                Long timeStamp;
                try {
                    timeStamp = eMatrixDateFormat.getJavaDate(curObjectMap.get(DomainConstants.SELECT_MODIFIED)).getTime();
                } catch (Exception ex) {
                	timeStamp = new Random().nextLong();
                }
                
                // If we are in the SCE, we use a different place holder
                if (isSCE) {
                    StringBuffer stringBuffer = new StringBuffer();
                    renderRichtextField(context, timeStamp.toString(), objectID, "", stringBuffer, isEditable);
                    returnVector.add(stringBuffer.toString());
                    continue;
                }
                boolean isParameter = parameterType.equals(kind);
                if(toExport)
                {
                	// ++ HAT1 : ZUD IR-242335V6R2015 fix                    
                	if(isParameter)
                	{
                    	// ++ HAT1 : ZUD IR-242335V6R2015 fix                    
                    	String objectIDtoExport = objectID + "|" + "true";
     		            // HAT1 : ZUD IR-331758-3DEXPERIENCER2015x fix                                       
                    	String parameterContentColumn = getParameterContentValue(context, JPO.packArgs(objectIDtoExport));
                    	// -- HAT1 : ZUD IR-242335V6R2015 fix                    
                        returnVector.add(parameterContentColumn);
                	}
                	else
                	{
                        DomainObject dmoObj = DomainObject.newInstance(context, objectID);
                        String strContentText = dmoObj.getAttributeValue(context, "Content Text");
                		returnVector.add(strContentText);
                	}
                	// -- HAT1 : ZUD IR-242335V6R2015 fix                    
                }
                else 
                {
                	if(isParameter){
                        //following code is used to get the parent Requirement of current parameter. In case we want to display a table for all the parameters together
                		String strParentId = (String)curObjectMap.get("$PID");
                    	returnVector.add("<img style='display: block; margin-left: auto; margin-right: auto;' "
                                + "src='images/loading.gif' onload='getParameterContent(this, \"" + strParentId + "\", \"" + objectID + "\", \"" + 
                                    timeStamp + "\", \"" + DefaultConvertorVersion.VERSION_ONE.toString() + "\")' />");
                	}
                	else{
                        returnVector.add("<img style='display: block; margin-left: auto; margin-right: auto;' "
                                + "src='images/loading.gif' class='richTextPlaceHolder' data-objectId='" + objectID + "'"
                                + " data-timeStamp='" + timeStamp + "'" + " data-convertor='" + 
                                DefaultConvertorVersion.VERSION_ONE.toString() + "' />");
                	}
                }
                
            } catch (Exception ex) {
                String strError = "<img style='display: block; margin-left: auto; margin-right: auto;' "
                        + "alt='Error' src='data:image;base64," + ImageUtil.DEFAULT_PICTURE_ERROR + "' />";
                returnVector.add(toExport != true ? strError : " ");
                continue;
            }
        }
        return returnVector;
    }
    
    /**
     * Returns the place holder to show the HTML from RTF
     * @param context - the eMatrix <code>Context</code> object
     * @param args - the inputMap who contains the table definition for Validation Column under Requirements & Requirements Specification.
     * @return a vector containaing the place holder
     * @throws Exception
     */ 
 
public Vector getValidationColumnRTF(Context context, String[] args) throws Exception 
{
	//final String validationStatusKey = "attribute[" + ReqSchemaUtil.getValidationStatusAttribute(context) + "]";

        Map inputMap = (Map) JPO.unpackArgs(args);
        Vector returnVector = new Vector();

        MapList objectMapList = (MapList) inputMap.get("objectList");
        HashMap paramList = (HashMap) inputMap.get("paramList");
        HashMap columnMap = (HashMap) inputMap.get("columnMap");
        HashMap columnMapSettings = (HashMap) columnMap.get("settings");
        
        String strExport = (String) paramList.get("exportFormat");
        boolean toExport = false;
        if (strExport != null) 
            toExport = true;                

        boolean isEditable = true; // FIXME columnMapSettings.get("Editable").equals("true");
        boolean isSCE = (paramList.get("isFromSCE") != null && ((String) paramList.get("isFromSCE")).equals("true")) ? true : false;
        
        if(objectMapList.size() == 0){
        	return returnVector;
        }
        
        String denied = EnoviaResourceBundle.getProperty(context, ReqConstants.BUNDLE_FRAMEWORK , context.getLocale(), "emxFramework.Basic.DENIED");
        String REQ_TC_NOTPLAYED = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.ReqValidationColumn.ToolTip.TestCaseNotPlayed"); 
        String REQ_TC_PARTIALLYPASSED = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.ReqValidationColumn.ToolTip.PartiallyPassed"); 
        String REQ_TC_FAILED = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.ReqValidationColumn.ToolTip.ValidationFailed"); 
        String REQ_TC_PASSED = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.ReqValidationColumn.ToolTip.ValidationPassed");
        String TC_TE_PASSED         = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.TestCaseValidationColumn.ToolTip.Passed"); 
        String TC_TE_FAILED         = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.TestCaseValidationColumn.ToolTip.Failed"); 
        String TC_NEXT_TE_SCHEDULED = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.TestCaseValidationColumn.ToolTip.NextTEScheduled");
        String TC_NO_TE_SCHEDULED   = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.TestCaseValidationColumn.ToolTip.NoTEScheduled"); 
        String TC_NO_TE_REPLAYED    = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.TestCaseValidationColumn.ToolTip.NoTEReplayed"); 
    	String REQ_NO_TC_ASSOCIATED = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.ReqValidationColumn.ToolTip.NoTCAssociated"); 
        String[] NLS = new String[]{REQ_TC_NOTPLAYED, REQ_TC_PARTIALLYPASSED, REQ_TC_FAILED, REQ_TC_PASSED, REQ_NO_TC_ASSOCIATED };
        
        final String relIdKey = "to[" + ReqSchemaUtil.getTestExecutionTestCaseRelationship(context) +  "].id";
        final String relValidationStatusKey = "to[" + ReqSchemaUtil.getTestExecutionTestCaseRelationship(context) +  "].attribute[" + 
        		ReqSchemaUtil.getValidationStatusAttribute(context) + "]";
        final String idKey = "to[" + ReqSchemaUtil.getTestExecutionTestCaseRelationship(context) +  "].from.id";
        final String percentageKey = "to[" + ReqSchemaUtil.getTestExecutionTestCaseRelationship(context) +  "].from.attribute[" + 
				ReqSchemaUtil.getPercentagePassedAttribute(context) + "]";
        final String actualEndDateKey = "to[" + ReqSchemaUtil.getTestExecutionTestCaseRelationship(context) +  "].from.attribute[" + 
				ReqSchemaUtil.getActualEndDateAttribute(context) + "]";
        final String estStartDateKey = "to[" + ReqSchemaUtil.getTestExecutionTestCaseRelationship(context) +  "].from.attribute[" + 
					ReqSchemaUtil.getEstimatedStartDateAttribute(context) + "]";
        
        final String percentageKey2 = "attribute[" + ReqSchemaUtil.getPercentagePassedAttribute(context) + "]";
        final String actualEndDateKey2 = "attribute[" + ReqSchemaUtil.getActualEndDateAttribute(context) + "]";
        final String estStartDateKey2 = "attribute[" + ReqSchemaUtil.getEstimatedStartDateAttribute(context) + "]";

        boolean doExpand = true;

    	String expandFrom = null;
		Map o =  (Map)objectMapList.get(0);
		String relId = (String)o.get(DomainConstants.SELECT_RELATIONSHIP_ID);
		if(relId == null || relId.length() == 0) {
			expandFrom = (String)o.get(DomainConstants.SELECT_ID);
		}
		else{
			expandFrom =(String)((StringList)DomainRelationship.newInstance(context, relId).getRelationshipData(context, new StringList("from.id")).get("from.id")).get(0);
		}
		
        Map<String, Map[]> TestCaseTestExecMap = new HashMap<>();
        Map<String, Float> TestCasePercentageMap = new HashMap<>();
        Map<String, Integer> TestCaseTECountMap = new HashMap<>();
        Map<String, String> RelTETCattrValStatusMap = new HashMap<>();

    	Map<String, int[]> countsMap = new HashMap<>();
    	Map<String, List[]> namesMap = new HashMap<>();
        
    	DomainObject expandFromObj = DomainObject.newInstance(context, expandFrom);
    	
    	if(relId == null && objectMapList.size() == 1 && !toExport){ //could be a structure with just the root, or a separate call for root of one level expand
    		String kind = expandFromObj.getInfo(context, "type.kindof");
    		if("DOCUMENTS".equals(kind) || kind.equals(ReqSchemaUtil.getChapterType(context)) ) {
        		doExpand = false;
    		}
    	}
    	
        if(doExpand){
        	StringList sel  = new StringList(4);
        	sel.add(DomainConstants.SELECT_ID);  //Test Case objects ID. 
        	sel.add(DomainConstants.SELECT_TYPE); // Test Case objects name. 
        	sel.add(DomainConstants.SELECT_NAME); // Test Case objects name. 
        	//sel.add(validationStatusKey);  

            MapList structure = expandFromObj.getRelatedObjects(context,
    					ReqSchemaUtil.getSpecStructureRelationship(context) + "," + ReqSchemaUtil.getSubRequirementRelationship(context) + "," + ReqSchemaUtil.getRequirementValidationRelationship(context), 
    					"*",   
    					sel,    // Object selects - information related to test case objects. 
                        null,       // relationship selects
                        false,      // from
                        true,       // to
                        (short)0,   //expand level
                        null,       // object where
                        null,       // relationship where
                        0);         // limit
            
            if(relId == null && objectMapList.size() == 1){ //add root to get the total counts
            	structure.add(0, objectMapList.get(0));
            }
            
            ReqStructureUtil.fillTypeInfo(context, structure);
            List<String> testCaseIds = new ArrayList<>();
            
            for(int i = 0; i < structure.size(); i++){
            	Map m = (Map)structure.get(i);
            	String kind = (String)m.get("kindof");
                if(kind.equals(ReqSchemaUtil.getTestCaseType(context))){
                	testCaseIds.add((String)m.get(DomainConstants.SELECT_ID));
                }
            }
            
            sel  = new StringList(6);
            sel.add(DomainConstants.SELECT_ID);
            sel.add(idKey);
            sel.add(relIdKey);
            sel.add(relValidationStatusKey);
            sel.add(percentageKey);
            sel.add(actualEndDateKey);
            sel.add(estStartDateKey);
            MapList execList = DomainObject.getInfo(context, testCaseIds.toArray(new String[0]), sel);
            
            for (int ii = 0; ii < execList.size(); ii++) {
            	Map<String, String> m = (Map<String, String>) execList.get(ii);
            	String TCId = m.get(DomainConstants.SELECT_ID);
                String relTETCvalidationStatus = "";
            	MapList TEs; 
            	if(m.get(idKey) == null) {
            		TEs = new MapList(0);
            	}
            	else{
                	String[] TEIds = m.get(idKey).split("\7", -1);
                	String[] TETCIds = m.get(relIdKey).split("\7", -1);
                	String[] percentages = m.get(percentageKey).split("\7", -1);
                	String[] actualEndDates = m.get(actualEndDateKey).split("\7", -1);
                	String[] estStartDates = m.get(estStartDateKey).split("\7", -1);
                	String[] relValidationStatus = m.get(relValidationStatusKey).split("\7", -1);
                	TEs = new MapList(TEIds.length);
                	for(int j = 0; j < TEIds.length; j++) {
                		Map<String, String > entry = new HashMap<>();
                		entry.put(DomainConstants.SELECT_ID, TEIds[j]);
                		entry.put(DomainConstants.SELECT_RELATIONSHIP_ID, TETCIds[j]);
                		entry.put(percentageKey2, percentages[j]);
                		entry.put(actualEndDateKey2, actualEndDates[j]);
                		entry.put(estStartDateKey2, estStartDates[j]);
                		entry.put(relValidationStatusKey, relValidationStatus[j]);
                		TEs.add(entry);
                	}
            	}
            	Map nextTEScheduled = NextTestExecutionScheduled(context, TEs);
            	Map lastCompletedTE = LastCompletedTestExecution(context, TEs);
        		TestCaseTestExecMap.put(TCId, new Map[]{lastCompletedTE, nextTEScheduled});

        		float percentagePassed = percentagePassLastTE(context, lastCompletedTE);
        		TestCasePercentageMap.put(TCId, percentagePassed);
				// HAT1 ZUD  :IR-506738-3DEXPERIENCER2018x
        		if(lastCompletedTE !=null) {
        			relTETCvalidationStatus = (String) lastCompletedTE.get(relValidationStatusKey);
        			if(!"".equals(lastCompletedTE.get(actualEndDateKey2)) && relTETCvalidationStatus.equalsIgnoreCase("Not Validated")){ //if actual end date is not blank
        				if(percentagePassed == 100.0) {
        					relTETCvalidationStatus = "Validation Passed";
        				}
        				else{
        					relTETCvalidationStatus = "Validation Failed";
        				}
        			}
        		}
        		
        		TestCaseTECountMap.put(TCId, TEs.size());
        		RelTETCattrValStatusMap.put(TCId, relTETCvalidationStatus);
            }
            
            
        	int indent = -1;
        	Stack path = new Stack();
        	for (int ii = 0; ii < structure.size(); ii++) {
        		Map objMap = (Map) structure.get(ii);
        		String relLevel = (String) objMap.get(SELECT_LEVEL);
        		int level = Integer.parseInt(relLevel);
        		if(level > indent){
        			indent = level;
        		}else if(level == indent){
        			Map m = (Map)path.pop();
        			int[] v = (int[])m.get("Validation");
        			if(path.size() > 0) {
            			int[] pv = (int[])((Map)path.peek()).get("Validation");
            			List[] pvn = (List[])((Map)path.peek()).get("ValidationNames");
            			for(int j = 0; j < pv.length; j++) {
            				pv[j] += v[j];
            				pvn[j].addAll(((List<String>[])m.get("ValidationNames"))[j]);
            			}
        			}
        		}else{
        			do{
        				Map m = (Map)path.pop();
            			int[] v = (int[])m.get("Validation");
            			
            			if(path.size() > 0) {
                			int[] pv = (int[])((Map)path.peek()).get("Validation");
                			List[] pvn = (List[])((Map)path.peek()).get("ValidationNames");
                			for(int j = 0; j < pv.length; j++) {
                				pv[j] += v[j];
                				pvn[j].addAll(((List[])m.get("ValidationNames"))[j]);
                			}
            			}
        				indent--;
        			}while(level < indent);
        			Map m = (Map)path.pop();
        			int[] v = (int[])m.get("Validation");
        			if(path.size() > 0) {
            			int[] pv = (int[])((Map)path.peek()).get("Validation");
            			List[] pvn = (List[])((Map)path.peek()).get("ValidationNames");
            			for(int j = 0; j < pv.length; j++) {
            				pv[j] += v[j];
            				pvn[j].addAll(((List<String>[])m.get("ValidationNames"))[j]);
            			}
        			}
        		}
        		//For Structure view Test Case Validation column value.
        		List[] names = new ArrayList[4];
        		names[0] = new ArrayList();
        		names[1] = new ArrayList();
        		names[2] = new ArrayList();
        		names[3] = new ArrayList();
        		//For Structure view Chapter and Requirement Validation column value. 
    			int[] counts = new int[]{0,0,0,0};

        		if(ReqSchemaUtil.getTestCaseType(context).equals(objMap.get("kindof"))){
        			String idTC = (String)objMap.get("id");
        			
					// ++ HAT1 ZUD  :IR-506738-3DEXPERIENCER2018x
        			String strValidationStatus = RelTETCattrValStatusMap.get(idTC);
        			// -- HAT1 ZUD  :IR-506738-3DEXPERIENCER2018x
        			
        			if(strValidationStatus.equalsIgnoreCase("Not Validated") || strValidationStatus.equals(""))
        			{
        				counts[0] += 1;
        				names[0].add((String)objMap.get(DomainConstants.SELECT_NAME));
        			}
        			else if(strValidationStatus.equals("Validation Failed"))
        			{
        			    // HAT1 : ZUD IR-331758-3DEXPERIENCER2015x fix                                       
            			Map lastCompletedTE = TestCaseTestExecMap.get((String)objMap.get(DomainConstants.SELECT_ID))[0];
            			float percentagePassed = percentagePassLastTE(context, lastCompletedTE);
        				if(percentagePassed > 0.0)
        				{
        					counts[1] += 1;
        					names[1].add((String)objMap.get(DomainConstants.SELECT_NAME));
        				}
        				else
        				{
        					counts[2] += 1;	
        					names[2].add((String)objMap.get(DomainConstants.SELECT_NAME));
        				}
        			}
        			else if(strValidationStatus.equals("Validation Passed"))
        			{
        				counts[3] += 1;
        				names[3].add((String)objMap.get(DomainConstants.SELECT_NAME));
        			}
        			
        		}
        		objMap.put("Validation", counts); //not validation, passed, not full validated, failed
        		objMap.put("ValidationNames", names);
    			path.push(objMap);
        	}
        	
        	while(path.size() > 1) {
    			Map m = (Map)path.pop();
    			int[] v = (int[])m.get("Validation");
    			if(path.size() > 0) {
        			int[] pv = (int[])((Map)path.peek()).get("Validation");
        			List[] pvn = (List[])((Map)path.peek()).get("ValidationNames");
        			for(int j = 0; j < pv.length; j++) {
        				pv[j] += v[j];
        				pvn[j].addAll(((List<String>[])m.get("ValidationNames"))[j]);
        			}
    			}
        	}

        	for (int ii = 0; ii < structure.size(); ii++) {
        		Map objMap = (Map) structure.get(ii);
        		countsMap.put((String)objMap.get(DomainConstants.SELECT_ID), (int[])objMap.get("Validation"));
        	}

        	for (int ii = 0; ii < structure.size(); ii++) {
        		Map objMap = (Map) structure.get(ii);
        		namesMap.put((String)objMap.get(DomainConstants.SELECT_ID), (List[])objMap.get("ValidationNames"));
        	}
    	}
    	
        
        Iterator objectItr = objectMapList.iterator();

        while (objectItr.hasNext()) {
            try {

                // Get the information about the current row
                Map<String, String> curObjectMap = (Map) objectItr.next();
                String objectName = (String) curObjectMap.get("name");
                String objectID = (String) curObjectMap.get("id");

                String kind = (String) curObjectMap.get("kindof");
                
                if(kind == null) {
                    DomainObject dmoObj = DomainObject.newInstance(context, objectID);
                	kind = dmoObj.getInfo(context, "type.kindof");
                	if("DOCUMENTS".equals(kind)) {
                		kind = ReqSchemaUtil.getRequirementSpecificationType(context);
                	}
                }
                
                if (kind.equals(ReqSchemaUtil.getPARParameterType(context)) ||
                		kind.equals(ReqSchemaUtil.getCommentType(context))
                	) 
                {
                    returnVector.add(toExport != true ? "<div style='text-align:center;'> - </div>" : " "); 
                    continue;
                }

                String readAccess = (String)curObjectMap.get(ReqConstants.SELECT_READ_ACCESS);
                if(ReqConstants.DENIED.equals(readAccess)){
                    returnVector.add(toExport != true ? "<div style='text-align:center;'>" + denied + "</div>" : " "); 
                    continue;
                }

                // If we are in the SCE, we use a different place holder
                if (isSCE) {
                    // We get the timeStamp
                    Long timeStamp = new Random().nextLong();
                    try {
                        timeStamp = eMatrixDateFormat.getJavaDate(curObjectMap.get(DomainConstants.SELECT_MODIFIED)).getTime();
                    } catch (Exception ex) {
                        // NOP
                    }
                    
                    StringBuffer stringBuffer = new StringBuffer();
                    renderRichtextField(context, timeStamp.toString(), objectID, "", stringBuffer, isEditable);
                    returnVector.add(stringBuffer.toString());
                    continue;
                }
                
	            if(kind.equals(ReqSchemaUtil.getChapterType(context)) || kind.equals(ReqSchemaUtil.getRequirementSpecificationType(context)))
	            {
                	// ++ HAT1 : ZUD IR-242335V6R2015 fix                    
	            	if(toExport || doExpand)
	            	{
	            		returnVector.add(getTestCaseValidationCount(context, countsMap.get(objectID) , null, NLS, false, toExport)); 
		            }
	            	else{
	                	returnVector.add("<div id = 'ValidationColumn_"+ objectID +"'><img style='display: block; margin-left: auto; margin-right: auto;' "
	                            + "src='images/loading.gif' onload='getRootTestCaseCounts(this, \"" + objectID + "\")' /></div>");

	            	}
	            	continue;
                	// -- HAT1 : ZUD IR-242335V6R2015 fix                    
	            }
                
        	    //++ HAT1 : ZUD - HL (Validation Column) To print the count of  Relevant Test Cases for Requirements under Validation Column.                                
            	//hat1 : zud IR-326341-3DEXPERIENCER2015x fix
            	if(kind.equals(ReqSchemaUtil.getRequirementType(context)))
                {
                	returnVector.add(getTestCaseValidationCount(context, countsMap.get(objectID) , namesMap.get(objectID), NLS, true, toExport));  
                	continue;
				}
        		//-- HAT1 : ZUD - HL (Validation Column) To print the count of  Relevant Test Cases for Requirements under Validation Column.                

                // ++ HAT1 : ZUD - HL (Validation Column). To print the Status & Schedule for the Last performed and Next Scheduled Test Execution respectively for any Test Case.
                
                if(kind.equals(ReqSchemaUtil.getTestCaseType(context)))
                {
                    String strTestCaseValidationColumnStatus = " --- ",strTestCaseValidationColumnSchedule  = " --- ";
                    
                    String TestCaseValidationColumn = "";
    				String toExportTestCaseValidationColumn = "";
                	
                    if(TestCaseTECountMap.get(objectID) != 0)
                    {
                    	// ++ Last completed Test Execution.
                    	Map lastTestExecutionObjMap = TestCaseTestExecMap.get(objectID)[0];

                    	if(lastTestExecutionObjMap != null) // && isActualEndDateTE == -1
                    	{
	                    	String lastTestExecutionObjID = (String) lastTestExecutionObjMap.get("id");
	                    	String lastTestExecutionObjName = (String) lastTestExecutionObjMap.get("name");
	                    	String lastTestExecutionObjRelID = (String) lastTestExecutionObjMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
							
							float PercentagePassed = TestCasePercentageMap.get(objectID); // Float.parseFloat(strPercentagePassed); // Converting it into Float.
							
							Date dateLastActualEndDateTE	= eMatrixDateFormat.getJavaDate((String)lastTestExecutionObjMap.get(actualEndDateKey2));

							SimpleDateFormat strDate = new SimpleDateFormat ("yyyy/MM/dd hh:mm a");      //Required format for date display.
							String strLastActualEndDateTE = (String) strDate.format(dateLastActualEndDateTE);   // Converting default Date object format to Required String format. 
							
		                    String TC_TE_PARTIALLYPASSED = MessageUtil.getMessage(context, null, "emxRequirements.TestCaseValidationColumn.ToolTip.Partially", new String[]{PercentagePassed + ""}, null, context.getLocale(), "emxRequirementsStringResource");
							if(PercentagePassed > 0.00 && PercentagePassed < 100.00)
							{
								strTestCaseValidationColumnStatus = "Partial";
								TestCaseValidationColumn ="<table><tr>" 
								  +"<td width = '45px' style = 'background-color:yellow;' title = '" + TC_TE_PARTIALLYPASSED +" \u00A0\u00A0 "+ strLastActualEndDateTE +"'>" 
                    					+ "<a href='#'  onClick ='testExecutionPopUp(\"" + lastTestExecutionObjID + "\", \"" + lastTestExecutionObjRelID + "\",\"" + objectID + "\")' >" + strTestCaseValidationColumnStatus + "</a>" 
										+ "</td>";
								
			                	// HAT1 : ZUD IR-242335V6R2015 fix                    
								toExportTestCaseValidationColumn = strTestCaseValidationColumnStatus;
							}
							
							else if(PercentagePassed == 0.00)
							{
								strTestCaseValidationColumnStatus = "Failed";
								TestCaseValidationColumn ="<table><tr>" 
								  +"<td width = '45px' style = 'background-color:red;' title = '" + TC_TE_FAILED + "\u00A0\u00A0" + strLastActualEndDateTE+"'>"
                    					+ "<a href='#'  onClick ='testExecutionPopUp(\"" + lastTestExecutionObjID + "\", \"" + lastTestExecutionObjRelID + "\",\"" + objectID + "\")' >" + strTestCaseValidationColumnStatus + "</a>" 										
									+ "</td>";
			                	// HAT1 : ZUD IR-242335V6R2015 fix  
								toExportTestCaseValidationColumn = strTestCaseValidationColumnStatus;

							}
							
							else // (PercentagePassed == 100.00)
							{
								strTestCaseValidationColumnStatus = "Passed";
								TestCaseValidationColumn ="<table><tr>" 
                     					+"<td width = '45px' style = 'background-color:#39FF14;' title = '" + TC_TE_PASSED + "\u00A0\u00A0 "+ strLastActualEndDateTE+"'>" 
      								
                    					+ "<a href='#'  onClick ='testExecutionPopUp(\"" + lastTestExecutionObjID + "\", \"" + lastTestExecutionObjRelID + "\",\"" + objectID + "\")' >" + strTestCaseValidationColumnStatus + "</a>" 										
										+ "</td>";
			                	// HAT1 : ZUD IR-242335V6R2015 fix  
								toExportTestCaseValidationColumn = strTestCaseValidationColumnStatus;
							}
						}
                    	else
						{
							TestCaseValidationColumn ="<table><tr>" 
                					+"<td style = 'background-color:aqua;' title = '" + TC_NO_TE_REPLAYED +"'>" + strTestCaseValidationColumnStatus + "</td>";
							
		                	// HAT1 : ZUD IR-242335V6R2015 fix  
							toExportTestCaseValidationColumn = TC_NO_TE_REPLAYED;

						}
                    	//-- Last completed Test Execution.
                    	
                    	// ++ Next Test Execution Scheduled Start. 
                    	
                    	Map nextScheduledTestExecutionObjMap = TestCaseTestExecMap.get(objectID)[1];
                    	String strEarliestEstimatedStartDateTE = "";
                    	
                    	if(nextScheduledTestExecutionObjMap != null)
                    	{
                    		String nextTestExecutionObjID = (String) nextScheduledTestExecutionObjMap.get("id");
	                    	String nextTestExecutionObjName = (String) nextScheduledTestExecutionObjMap.get("name");
	                    	String nextTestExecutionObjRelID = (String) nextScheduledTestExecutionObjMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
	                    	//DomainObject nextTestExecutionDmoObj = DomainObject.newInstance(context, nextTestExecutionObjID);

	                    	Date earliestEstimatedStartDateTE  = eMatrixDateFormat.getJavaDate((String)nextScheduledTestExecutionObjMap.get(estStartDateKey2));

							SimpleDateFormat strDate = new SimpleDateFormat ("yyyy/MM/dd hh:mm a");      //Required format for date display.
							strEarliestEstimatedStartDateTE = (String) strDate.format(earliestEstimatedStartDateTE);   // Converting default Date object format to Required String format. 

							TestCaseValidationColumn += "<td>" + " / "+ "</td>"
                					+"<td style = 'background-color:#39FF14;' title = '" + TC_NEXT_TE_SCHEDULED + " \u00A0\u00A0 "+ strEarliestEstimatedStartDateTE +"'>"          						
                					+ "<a href='#'  onClick ='testExecutionPopUp(\"" + nextTestExecutionObjID + "\", \"" + nextTestExecutionObjRelID + "\",\"" + objectID + "\")' >" + strEarliestEstimatedStartDateTE + "</a>" 										
                					+   "</td>"
                							+ "</tr></table>";
		                	// HAT1 : ZUD IR-242335V6R2015 fix  
							toExportTestCaseValidationColumn += "/" + strEarliestEstimatedStartDateTE;

                    	}
                    	else
                    	{
							TestCaseValidationColumn += "<td>" + " / "+ "</td>"
										+"<td style = 'background-color:aqua;' title = '" + TC_NO_TE_SCHEDULED +"'>" + strTestCaseValidationColumnSchedule + "</td>"
                							+ "</tr></table>";
		                	// HAT1 : ZUD IR-242335V6R2015 fix  
							toExportTestCaseValidationColumn += "/" + TC_NO_TE_SCHEDULED;
                    	}
                    	// -- Next Test Execution Scheduled End.
                    }
                    else
                    {
                    	TestCaseValidationColumn ="<table><tr>" 
                    					+"<td style = 'background-color:aqua;' title = '" + TC_NO_TE_REPLAYED +"'>" + strTestCaseValidationColumnStatus + "</td>"
                    							+"<td>" + " / "+ "</td>"
                    					+"<td style = 'background-color:aqua;' title = '" + TC_NO_TE_SCHEDULED +"'>" +strTestCaseValidationColumnSchedule  + "</td>"
                    							+ "</tr></table>";
	                	// HAT1 : ZUD IR-242335V6R2015 fix  
						toExportTestCaseValidationColumn = TC_NO_TE_REPLAYED + "/" + TC_NO_TE_SCHEDULED;
                    }
                    if(!toExport)
                    {
                    	returnVector.add(TestCaseValidationColumn);
                    }
                    else
                    {
                        returnVector.add(toExportTestCaseValidationColumn);
                    }
                }
                // -- HAT1 : ZUD - HL (Validation Column). To print the Status & Schedule for the Last performed and Next Scheduled Test Execution respectively for any Test Case.
                
            } catch (Exception ex) {
                String strError = "<img style='display: block; margin-left: auto; margin-right: auto;' "
                        + "alt='Error' src='data:image;base64," + ImageUtil.DEFAULT_PICTURE_ERROR + "' />";
                returnVector.add(toExport != true ? strError : " ");
                continue;
            }
        }
        return returnVector;
    }      

    /**
     * Finds the next scheduled Test Execution for a Test Case.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - All the Test Executions created for a Test Case
     * @return the next scheduled Test Execution, null if no such TE.
     * @throws Exception
     */    

public Map NextTestExecutionScheduled(Context context, MapList TestCaseTestExecutionList)
{
    final String estStartDateKey = "attribute[" + ReqSchemaUtil.getEstimatedStartDateAttribute(context) + "]";
    final String actualEndDateKey = "attribute[" + ReqSchemaUtil.getActualEndDateAttribute(context) + "]";

	Map<String, String> currTestExecutionObjMap = null;
	Map<String, String> nextTestExecutionObjMap = null;    // Map for Next Scheduled Test Execution under Test Case.
	
	Date currentDate = new Date();                     // currentDate object of type Date.
	Date dateNextEstimatedStartDateTE = currentDate;     // Populating 'dateLastActualEndDateTE' with currentDate which will hold attribute 'Actual End Date' of Last connected Test Execution.
	Date currEstimatedStartDateTE = null;

    if(TestCaseTestExecutionList.size() != 0)
    {
        String strTestCaseTestExecutionListCount = String.valueOf(TestCaseTestExecutionList.size());
    	Iterator testExecutionItr = TestCaseTestExecutionList.iterator();
    	int isAnyNextTEfound = 0;
		while(testExecutionItr.hasNext())
		{
			currTestExecutionObjMap = (Map) testExecutionItr.next();
			
			if(!((String)currTestExecutionObjMap.get(actualEndDateKey)).equals(""))
			{
				continue;  //Test Execution is already started.
			}
			try 
			{
				currEstimatedStartDateTE = eMatrixDateFormat.getJavaDate((String)currTestExecutionObjMap.get(estStartDateKey));
			} 
			catch (Exception e) 
			{
				continue;     // Test Execution have no "Estimated Start Date".
			}
			
			int isBeforeCurrEstimatedStartDate = currEstimatedStartDateTE.compareTo(dateNextEstimatedStartDateTE);
			
			int isAfterCurrentDate = currEstimatedStartDateTE.compareTo(currentDate);
			
			if(isAfterCurrentDate == 1 && isAnyNextTEfound == 0 && isBeforeCurrEstimatedStartDate == 1)
			{
				isAnyNextTEfound = 1;
				nextTestExecutionObjMap = currTestExecutionObjMap;
				dateNextEstimatedStartDateTE = currEstimatedStartDateTE;
			}
			
			if(isAfterCurrentDate == 1 && isAnyNextTEfound == 1 && isBeforeCurrEstimatedStartDate == -1)
			{
				nextTestExecutionObjMap = currTestExecutionObjMap;
				dateNextEstimatedStartDateTE = currEstimatedStartDateTE;
			}
		}
  }
  return nextTestExecutionObjMap;
}
 
    /**
     * Finds the Last completed Test Execution under Test Case.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - All the Test Executions created for a Test Case
     * @return the last completed Test Execution, null if no such TE.
     * @throws Exception
     */
    public Map LastCompletedTestExecution(Context context, MapList TestCaseTestExecutionList)
    {
        final String actualEndDateKey = "attribute[" + ReqSchemaUtil.getActualEndDateAttribute(context) + "]";

    	Map<String, String> currTestExecutionObjMap = null;
    	Map<String, String> lastTestExecutionObjMap = null;            // Map for Last connected Test Execution to Test Case.
    	
    	Date currentDate = new Date();                                 // currentDate object of type Date.
    	Date dateLastActualEndDateTE = currentDate;                    // Populating 'dateLastActualEndDateTE' with currentDate which will hold attribute 'Actual End Date' of Last connected Test Execution.
    	Date currActualEndDateTE = null;
    	    	
    	if(TestCaseTestExecutionList.size() != 0)
        {
	    	Iterator testExecutionItr = TestCaseTestExecutionList.iterator();
	    	int isAnyLastTEfound = 0;
			while(testExecutionItr.hasNext())
			{
				currTestExecutionObjMap = (Map) testExecutionItr.next();
				
				try
				{
					currActualEndDateTE	= eMatrixDateFormat.getJavaDate((String)currTestExecutionObjMap.get(actualEndDateKey));
				} 
				catch (Exception e) 
				{
					continue;
				}
				
				int flag = currActualEndDateTE.compareTo(dateLastActualEndDateTE);
				
				if(isAnyLastTEfound == 0 && (flag == -1))
				{
					isAnyLastTEfound = 1;
					lastTestExecutionObjMap = currTestExecutionObjMap;
					dateLastActualEndDateTE = currActualEndDateTE;
				}
				
				if(isAnyLastTEfound == 1 && flag == 1)
				{
					lastTestExecutionObjMap = currTestExecutionObjMap;
					dateLastActualEndDateTE = currActualEndDateTE;
				}
				
			}
        }
		return lastTestExecutionObjMap;
    }


/**
 * This function checks status of all the test cases which comes under any object and count them separately.
 * @param context - the eMatrix <code>Context</code> object
 * @param args - the inputMap which contains List object Test Cases ids.
 * @return a String containing counts of Test Cases . If check is OK returns null
 */

public String getTestCaseValidationCount(Context context, int[] counts, List[] names, String[] NLS, boolean witLink, boolean isExport ) throws Exception
{
	// HAT1 ZUD: IR-506738-3DEXPERIENCER2018x fix
	int notValidatedCount = counts[0], validationPassedCount = counts[3], notFulValidatedCount = counts[1], validationFailedCount = counts[2];
	String strReqSpecTestCasesStatusCount = "";
	
	if(notValidatedCount + validationPassedCount + notFulValidatedCount + validationFailedCount != 0)
	{
    	if(isExport){
       		// HAT1 : ZUD IR-242335V6R2015 fix                    
    	       strReqSpecTestCasesStatusCount = notValidatedCount + " / " + notFulValidatedCount + "/ " + validationFailedCount + " /" + validationPassedCount;
    	}
    	else{
    	       strReqSpecTestCasesStatusCount ="<table>"
    					+                         "<tr>"
    					+                             "<td  style = 'background-color:aqua;' title = '" + notValidatedCount + "\u00A0\u00A0" + NLS[0] + "'>"
    					+ 									(witLink ? "<a href='#' onClick = 'testCaseNotValidatedList(this, \"" + names[0] + "\")'> "  : "<b>") + notValidatedCount 
    					+ 									(witLink ? "</a>" : "</b>") + "</td>" 
    					                                  + "<td>"+ "  /  " + "</td>"
    				    +                             "<td  style = 'background-color:yellow;'  title = '" + notFulValidatedCount + "\u00A0\u00A0" + NLS[1] + "'>" 
    					+ 									(witLink ? "<a href='#' onClick ='testCaseNotFulValidatedList(this, \"" + names[1] + "\")' >" : "<b>") + notFulValidatedCount 
    					+ 									(witLink ? "</a>" : "</b>") + "</td>"
    													  + "<td>"+ "  /  " + "</td>"
    					+                             "<td  style = 'background-color:red;' title = '" + validationFailedCount +  "\u00A0\u00A0" + NLS[2] + "'>" 
    					+ 									(witLink ? "<a href='#' onClick ='testCaseValidationFailedList(this, \"" + names[2] + "\")' >" : "<b>") + validationFailedCount 
    					+ 									(witLink ? "</a>" : "</b>") + "</td>" 
    	                								  + "<td>"+ "  /  " + "</td>"
    					+                             "<td  style = 'background-color:#39FF14;'  title = '" + validationPassedCount + "\u00A0\u00A0" + NLS[3] + "'>" 
    	                + 									(witLink ? "<a href='#' onClick ='testCaseValidationPassedList(this, \"" + names[3] + "\")' >" : "<b>") + validationPassedCount 
    	                + 									(witLink ? "</a>" : "</b>") + "</td>" 
    					+                         "</tr>"
    					+                    "</table>";
    	}
	}
	else
	{
		if(isExport){
			// HAT1 : ZUD IR-242335V6R2015 fix                    
			strReqSpecTestCasesStatusCount = "0";
		}
		else{
			strReqSpecTestCasesStatusCount = "<table>" +"<tr>"
					+"<td title = '"+ NLS[4] +"'>"
						+ "<h2><font color='red'>0</font></h2>"
					+"</td>"
						+ "</tr></table>";
		}
	}
	
	return strReqSpecTestCasesStatusCount;		
}   

//-- HAT1 : ZUD 12-AUG-14 Validation Column under Requirement Specification and Chapter.

//HAT1 : ZUD 13-AUG-14 Validation Column under Requirement Specification and Chapter.
/**
 * This function takes DomainObject of Test Case and return percentage pass of 'Last Test Execution' replayed.
 * @param context - the eMatrix <code>Context</code> object
 * @param args - the input which contains  Test Cases DomainObject.
 * @return a String containing Percentage Passed of the last Test Execution.
 */

// ++ HAT1 : ZUD IR-331758-3DEXPERIENCER2015x fix                                       
public float percentagePassLastTE(Context context, Map lastTestExecutionObjMap) throws Exception
{
    final String percentageKey = "attribute[" + ReqSchemaUtil.getPercentagePassedAttribute(context) + "]";
    
	String strPercentagePassed = "-1";

	if(lastTestExecutionObjMap != null) //When the TC have Last Completed Test Execution.
	{
		strPercentagePassed = (String)lastTestExecutionObjMap.get(percentageKey);
	}

	return Float.parseFloat(strPercentagePassed);
}



    /* The place holder for the SCE */
    private void renderRichtextField(Context context, String sTimeStamp, String objId, String relId,
            StringBuffer buffer, boolean isEditable) {
        String MSG_LOADING = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.SCE.Message.Loading"); 
        buffer.append("<div class=\"rtccontainer\" editable=\""
                + isEditable
                + "\">"
                + MSG_LOADING + "</div>");
    }

// ++ HAT1 ZUD HL Requirement Specification Dependency
    /**
     * Finds relationship between Source and Target ReqSpec(s). Can handle one of more Target ReqSpecIds(With "|" as separator.) 
     * @param context - the eMatrix <code>Context</code> object
     * @param args - the object IDs of  Source and Target ReqSpec. 
     * @return the Id of relationship if exist and null if there is not any relationship then null.
     * @throws Exception
     */    
public String getReqSpecsDerivationLink(Context context, String[] args) throws Exception
{
    Map reqSpecIds = (HashMap) JPO.unpackArgs(args);
    String reqSpecSourceId = (String) reqSpecIds.get("reqSpecSourceId");
    final String reqSpecTargetIds = (String) reqSpecIds.get("reqSpecTargetId");
    String derivationMode = (String) reqSpecIds.get("derivationMode");
    String reqSpecsDerivationLinkId = "";
    
    //While added Targets are more than one.
    if(reqSpecTargetIds.contains("|"))
    {
    	for(String reqSpecTargetId: reqSpecTargetIds.split("[|]"))
    	{
    		reqSpecsDerivationLinkId = isReqSpecsDerivationLinkExist(context, reqSpecSourceId, reqSpecTargetId, derivationMode);
    		if(reqSpecsDerivationLinkId.equalsIgnoreCase("No Derivation Link"))
    		{
    			break;
    		}
    	}
    }
    //While added Targets are more than one or while creating relationship requirements.
    else
    {
    	reqSpecsDerivationLinkId = isReqSpecsDerivationLinkExist(context, reqSpecSourceId, reqSpecTargetIds, derivationMode);
    }

	return reqSpecsDerivationLinkId;
}

/**
 * Finds relationship between Source and Target ReqSpec(s). Can handle one of more Target ReqSpecIds(With "|" as separator.) 
 * @param context - the eMatrix <code>Context</code> object 
 * @param String reqSpecSourceId,  reqSpecTargetId - the object IDs of  Source and Target ReqSpec. String derivationMode if cover or refined.
 * @return "Derivation Link Exist" and "No Derivation Link" strings.
 * @throws Exception
 */
public String isReqSpecsDerivationLinkExist(Context context, String reqSpecSourceId, String reqSpecTargetId, String derivationMode) throws Exception
{
	boolean fromBool = true, toBool = true;
	if(derivationMode.equalsIgnoreCase("cover"))
	{
		fromBool = true;
		toBool = false;
	}
	
	if(derivationMode.equalsIgnoreCase("satisfy"))
	{
		fromBool = false;
		toBool = true;
	}
	
	final String reqSpecTargetId_1 = reqSpecTargetId;
    String reqSpecsDerivationLinkId = "";
    
	DomainObject DomObjSource = DomainObject.newInstance(context, reqSpecSourceId);
	DomainObject DomObjTarget = DomainObject.newInstance(context, reqSpecTargetId);
	
	StringList selectsAttrib  = new StringList(2);
    selectsAttrib.add("id");
    selectsAttrib.add("name");
    
    //Removing hardcoded value.
    String allToTypes = ReqSchemaUtil.getRequirementSpecificationType(context);
    String rel        = (String) ReqSchemaUtil.getDerivedRequirementSpecificationRelationship(context);
    
	MapList relatedReqSpecMapList = DomObjSource.getRelatedObjects(context,
			//"Derived Requirement Specification", //Relationship form Source ReqSpec to Target ReqSpec.
			rel, //Relationship form Source ReqSpec to Target ReqSpec.
			allToTypes,   
			selectsAttrib,    // Object selects
			    null,   //relationshipSelets, 
	            true,      // from covered true,  /* To avoid cyclicity bw reqspecs */ HAT1 ZUD: IR-381800-3DEXPERIENCER2016x 
	            true,       // to refined true, 
	            (short)1,   //expand level
	            null,       // object where
	            null,       // relationship where
	            0);         // limit
	
	final String reqSpecTargetName = DomObjTarget.getInfo(context, "name");
	
	final String relName = rel;
	Map<String,String> reqSpecTargetMap = new HashMap<String,String>() {{
	    put("name", reqSpecTargetName);
	    put("relationship", relName);
	    put("id", reqSpecTargetId_1 );
	    put("level", "1" );
	}};
	
	//If the relationship exists then it will return relID.
	if(relatedReqSpecMapList.contains(reqSpecTargetMap))
	{
		reqSpecsDerivationLinkId = "Derivation Link Exist";
	}
	//else relationship exists then it will return relID.
	else
	{
		reqSpecsDerivationLinkId = "No Derivation Link";
	}
	return reqSpecsDerivationLinkId;
	
}
// -- Relationship direction modification for covered and refined requirements.

/**
 * Creates relationship between Source and Target ReqSpec.
 * @param context - the eMatrix <code>Context</code> object
 * @param args - the object IDs of  Source and Target ReqSpec. 
 * @return the Id of newly created relationship.
 * @throws Exception
 */    
public String createReqSpecsDerivationLink(Context context, String[] args) throws Exception
{
	Map reqSpecIds = (HashMap) JPO.unpackArgs(args);
	String reqSpecSourceId = (String) reqSpecIds.get("reqSpecSourceId");
	String reqSpecTargetIds = (String) reqSpecIds.get("reqSpecTargetIds");
	String derivationMode = (String) reqSpecIds.get("derivationMode");

	String reqSpecsDerivationLinkId = "";
	StringList objectRelId	= null;
    
	DomainObject domReqSpecSourceId = DomainObject.newInstance(context, reqSpecSourceId);
	
	// ++ Relationship direction modification for covered and refined requirements.
    for (String reqSpecTargetId: reqSpecTargetIds.split("[|]"))
    {
    	domReqSpecSourceId = DomainObject.newInstance(context, reqSpecSourceId);
        HashMap reqSpecIdsMap = new HashMap();
        reqSpecIdsMap.put("reqSpecSourceId",reqSpecSourceId);
        reqSpecIdsMap.put("reqSpecTargetId",reqSpecTargetId);
        reqSpecIdsMap.put("derivationMode",derivationMode);
        
        String isLinkExist = getReqSpecsDerivationLink(context, JPO.packArgs(reqSpecIdsMap));
        
        if(isLinkExist.equalsIgnoreCase("No Derivation Link"))
        {
        	reqSpecsDerivationLinkId = "";
        	// -- Relationship direction modification for covered and refined requirements.
	DomainObject domReqSpecTargetId = DomainObject.newInstance(context, reqSpecTargetId);
	
    StringList relselect = new StringList();
    relselect.add(DomainRelationship.SELECT_ID);
    
    //Removing hardcoded values
    String rel = (String) ReqSchemaUtil.getDerivedRequirementSpecificationRelationship(context);
    //Create Derived Requirement rel
    try {
    	    	// ++ Relationship direction modification for covered and refined requirements.
    	    	if(derivationMode.equalsIgnoreCase("cover"))
    	    	{
    	    		DomainObject tempDom = domReqSpecSourceId;
    	    		domReqSpecSourceId   = domReqSpecTargetId;
    	    		domReqSpecTargetId = tempDom;
    	    	}
    	    	// -- Relationship direction modification for covered and refined requirements.
    			
		DomainRelationship newRel = DomainRelationship.connect(context, domReqSpecSourceId, rel, domReqSpecTargetId);

		//DomainRelationship newRel = DomainRelationship.connect(context, reqSpecSourceId, ReqSchemaUtil.getDerivedRequirementSpecificationRelationship(context), reqSpecTargetId, _filtered);
		Hashtable RelInfo		= newRel.getRelationshipData(context, relselect);
		objectRelId	= (StringList)RelInfo.get(DomainRelationship.SELECT_ID);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    if(objectRelId == null)
    {
    	reqSpecsDerivationLinkId = "No Derivation Link Created";
    }
    else
    {
    	reqSpecsDerivationLinkId = "Derivation Link Created";
    }
        }
    
    }
    return reqSpecsDerivationLinkId;
	
}


/**
 * Finds the Tree Display Settings in preferences.
 * @param context - the eMatrix <code>Context</code> object
 * @param args - Type (Requirement Group, Requirement Specification, Chapter, Requirement, Comment) of Business object.
 * @return String (Tree Display settings).
 * @throws Exception
 */ 
public static String getTreeDisplaySettings(Context context, String[] args)
{
	String objType = "";
	String treeDisplaySettings   = "";
	
	try 
	{
		
		objType = (String) JPO.unpackArgs(args);
		treeDisplaySettings = PreferenceServices.getTreeDisplaySettings(context, objType);
		
		// ++ HAT1 ZUD: TSK3433119 ++ 
		JSONObject jsonObj = new JSONObject(treeDisplaySettings);
    	
		String treeDisplayName         = (String)jsonObj.get("treeDisplayName");
		String treeDisplayTitle        = (String)jsonObj.get("treeDisplayTitle");
		String treeDisplayRevision     = (String)jsonObj.get("treeDisplayRevision");
		String treeDisplaySeperation   = (String)jsonObj.get("treeDisplaySeperation");
		String treeDisplayTitleMaxSize = (String)jsonObj.get("treeDisplayTitleMaxSize");
		// -- HAT1 ZUD: TSK3433119 -- 
		treeDisplaySettings = 	"treeDisplayName=" + treeDisplayName + "|treeDisplayTitle=" + treeDisplayTitle + "|treeDisplayRevision=" + treeDisplayRevision + "|treeDisplaySeperation=" + treeDisplaySeperation + "|treeDisplayTitleMaxSize=" + treeDisplayTitleMaxSize;

	}catch(Exception e) {
		
	}

	return treeDisplaySettings;
	
}


// -- HAT1 ZUD HL Requirement Specification Dependency
   
//++ IR-364087-3DEXPERIENCER2016xZUD existing dependency Target Reqspecs
/**
 * Retrieves list of Derived Requirement Specifications  
 * @param context - the eMatrix <code>Context</code> object
 * @param args - the object ID
 * @return the StringList of objectIds of Req Spec
 * @throws Exception
 */

public StringList getExistingDependTargetReqSpecs(Context context, String[] args)
{
	StringList lDependentReqSpec = new StringList();
	try {
		
    	// ++ Relationship direction modification for covered and refined requirements.
		Map reqSpecIds = (HashMap) JPO.unpackArgs(args);
		String rootObjectID = (String) reqSpecIds.get("rootObjectId");
		String derivationMode = (String) reqSpecIds.get("derivationMode");
		
		boolean fromBool = true, toBool = true;
		if(derivationMode.equalsIgnoreCase("cover"))
		{
			fromBool = true;
			toBool = false;
		}
		
		if(derivationMode.equalsIgnoreCase("satisfy"))
		{
			fromBool = false;
			toBool = true;
		}
    	// -- Relationship direction modification for covered and refined requirements.

		DomainObject DomObjSource = DomainObject.newInstance(context, rootObjectID);
		
		StringList selectsAttrib  = new StringList(2);
		selectsAttrib.add("id");
		selectsAttrib.add("name");
		
		// ++ Removal of hardcoded value.
		String allToTypes = ReqSchemaUtil.getRequirementSpecificationType(context);
		
		String rel = (String) ReqSchemaUtil.getDerivedRequirementSpecificationRelationship(context);
		// -- Removal of hardcoded value.
		
		MapList relatedReqSpecMapList = DomObjSource.getRelatedObjects(context,
				rel, //Relationship form Source ReqSpec to Target ReqSpec.
				allToTypes,   
				selectsAttrib,    // Object selects
				    null,   //relationshipSelets, // relationship selects
				    fromBool,      // from false -
				    toBool,       // to true - 
		            (short)1,   //expand level
		            null,       // object where
		            null,       // relationship where
		            0);
		
		
		//list of in populated in strDependentReqSpec here as string

		for(int i=0;i<relatedReqSpecMapList.size();i++)
		{
			Map Object = (Map)relatedReqSpecMapList.get(i);
			lDependentReqSpec.add( Object.get("id"));
		}
		
	} catch (FrameworkException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}         // limit
	
	
	return lDependentReqSpec;
}
// -- ZUD existing dependency Target Reqspecs

// ++ HAT1 ZUD: IR-363246-3DEXPERIENCER2016x fix
/**
 * 
 * @param context - the eMatrix <code>Context</code> object
 * @param args - context, args.
 * @return Relationship id bw Source and Target Req Spec 
 * @throws Exception
 */
public StringList getDeletedDependTargetReqSpecs(Context context, String[] args)
{
	StringList lDeletedDependentReqSpecs = new StringList();
	StringList lNoDependencyReqSpecs   = new StringList();
	StringList targetReqSpecStringList   = new StringList();
	String reqSpecObjectName = "";
	
	lDeletedDependentReqSpecs.add("|DeletedDependency|");
	lNoDependencyReqSpecs.add("|NoDependency|");
	
	
	MapList dependentReqSpecs = new MapList();
	String relID = "";
	try 
	{		
		Map reqSpecIds = (HashMap) JPO.unpackArgs(args);
		String sIdString    = (String) reqSpecIds.get("sIdString");
		String sourceRootId = (String) reqSpecIds.get("sourceRootId");
		
		List<String> myList = new ArrayList<String>(Arrays.asList(sIdString.split("[|]")));
		dependentReqSpecs = getExistingDependTargetReqSpecs(context, sourceRootId);

		for(Object object : myList) 
		{	
			String curReqSpecTargetId = object.toString();
			String isLinkExist =  isReqSpecsDerivationLinkExist(context, sourceRootId, curReqSpecTargetId, "deleteDependencyLink");
			
			BusinessObject reqSpecObj = new BusinessObject(curReqSpecTargetId);
        	reqSpecObj.open(context);
        	reqSpecObjectName = reqSpecObj.getName();
			
			if(isLinkExist.equalsIgnoreCase("Derivation Link Exist"))
			{
				relID = getRelationshipIdBwReqSpecs(context, dependentReqSpecs, curReqSpecTargetId);
	        	DomainRelationship.disconnect(context, relID);

	        	lDeletedDependentReqSpecs.add(reqSpecObjectName);
			}
			else
			{
				lNoDependencyReqSpecs.add(reqSpecObjectName);
			}
        	reqSpecObj.close(context);
		}
		
		for(Object obj: lDeletedDependentReqSpecs)
		{
			String curReqSpecName = obj.toString();
			targetReqSpecStringList.add(curReqSpecName);
		}
		
		for(Object obj: lNoDependencyReqSpecs)
		{
			String curReqSpecName = obj.toString();
			targetReqSpecStringList.add(curReqSpecName);
		}

	}
	catch (Exception e) 
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	return targetReqSpecStringList;
}

/**
 * 
 * @param context - the eMatrix <code>Context</code> object
 * @param args - context, root Req Spec id.
 * @return MapList of all Related reqSpecs.
 * @throws Exception
 */
public MapList getExistingDependTargetReqSpecs(Context context, String rootObjectID)
{
		
		StringList selectsAttrib  = new StringList(2);
		selectsAttrib.add("id");
		selectsAttrib.add("name");
		
		String relID = DomainConstants.SELECT_RELATIONSHIP_ID;
        StringList relationshipSelets = new StringList(1);
        relationshipSelets.add(relID);
		
		String allToTypes = ReqSchemaUtil.getRequirementSpecificationType(context);
		
		String rel = (String) ReqSchemaUtil.getDerivedRequirementSpecificationRelationship(context);
		
		MapList relatedReqSpecMapList = new MapList();
		
		try {
			DomainObject DomObjSource = DomainObject.newInstance(context, rootObjectID);
			relatedReqSpecMapList = DomObjSource.getRelatedObjects(context,
					rel, //Relationship form Source ReqSpec to Target ReqSpec.
					allToTypes,   
					selectsAttrib,    // Object selects
					    relationshipSelets, // relationship selects
					    true,      // from false -
					    true,       // to true - 
			            (short)1,   //expand level
			            null,       // object where
			            null,       // relationship where
			            0);
			
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	return relatedReqSpecMapList;
}

/**
 * 
 * @param context - the eMatrix <code>Context</code> object
 * @param args - context, all the Dependent Req Spec, Target Req Spec object id.
 * @return Relationship id bw Source and Target Req Spec 
 * @throws Exception
 */
public String getRelationshipIdBwReqSpecs(Context context, MapList dependentReqSpecs, String curReqSpecTargetId)
{
	boolean flag = false;
	String relID    = ""; 
	String curObjID = "";

    MapList objectMap = dependentReqSpecs;
    Iterator objectItr = objectMap.iterator();
    while (objectItr.hasNext() && flag == false) 
    {
        Map<String, String> curObjectMap = (Map) objectItr.next();
        curObjID = ((String) curObjectMap.get("id"));
        
        if(curObjID.equalsIgnoreCase(curReqSpecTargetId))
        {
            relID = ((String) curObjectMap.get("id[connection]"));
            flag = true;
		}
    }
	return relID;
}
// -- HAT1 ZUD: IR-363246-3DEXPERIENCER2016x fix
    
    /**
     * Converts RTF to HTML for one object
     * @param context - the eMatrix <code>Context</code> object
     * @param args - the object ID
     * @return the HTML source from the RTF
     * @throws Exception
     */
    public String getHTMLSourceFromRTF(Context context, String[] args) throws Exception {

        Map argsMap = (HashMap) JPO.unpackArgs(args);
        String objectID = (String) argsMap.get("objectId");
        
        try {
            DomainObject dmoObj = DomainObject.newInstance(context, objectID);
            String strContentType = dmoObj.getAttributeValue(context, "Content Type");
            
            // If we have RTF data
			try(InputStream in = RichEditFactory.getRichEditUtil().getContentStream(context, objectID, IRichEditUtil.Format.HTML_PREVIEW)){
				String html = ConversionUtil.streamToString(in);
				String characteristics = ";";
				if(in instanceof RichTextStream){
					characteristics += ((RichTextStream)in).getFlags();
				}
				if(html.length() == 0){
					boolean isHTMLPreferred = "HTML".equalsIgnoreCase(RichEditUtil.getPreferredEditor(context));
					return ConvertedDataDecorator.putRichTextEditorDivForRTF(context, objectID, html, 
								(isHTMLPreferred ? "XHTML" : "DOCX") + characteristics);
				}
	            if (strContentType.equals("rtf.gz.b64")) {
	                return ConvertedDataDecorator.putRichTextEditorDivForRTF(context, objectID, html, "RTF" + characteristics);
	            } else if (strContentType.equals("html")) {
	            	if(RichEditUtil.isContentOOXML(context, objectID)) {
	            		return ConvertedDataDecorator.putRichTextEditorDivForRTF(context, objectID, html, "DOCX" + characteristics);
	            	}
	                return ConvertedDataDecorator.putRichTextEditorDivForRTF(context, objectID, html, "XHTML" + characteristics);
	            }
			}
        } catch (Exception ex) {
            String strError = "<img style='display: block; margin-left: auto; margin-right: auto;' "
                    + "alt='Error' src='data:image;base64," + ImageUtil.DEFAULT_PICTURE_ERROR + "' />";
            return strError;
        }
        return "<div style='text-align:center;'> - </div>";
    }

    /**
     * Updates the RTF for on object, INPUT: Format | EncodedRichText | ContentText OR Format | HTML | ContentText
     * @param context - the eMatrix <code>Context</code> object
     * @param args - the object ID, the New Value who contains the RTF
     * @return nothing
     * @throws Exception
     */
    public String updateRichTextContent(Context context, String[] args) throws Exception {
        
        Map programMap = (HashMap) JPO.unpackArgs(args);
        Map paramMap = (HashMap) programMap.get("paramMap");
        String newValue = (String) paramMap.get("New Value");
        String strObjectId = (String) paramMap.get("objectId");
        //String Param_Val = (String) paramMap.get("Param_Val");
        // Fix for IR-333259-3DEXPERIENCER2016
        newValue += " ";
        
        // This method is called when Save button in Structure Browser page is clicked
        // Zud Check if it is a parameter
        DomainObject dmoObj = DomainObject.newInstance(context, strObjectId);
        if(dmoObj.getInfo(context, DomainConstants.SELECT_TYPE).equals(ReqSchemaUtil.getParameterType(context)))
        {
        	
        	 String splitValues[] = newValue.trim().split(":");
             String values = splitValues[0];
             String displayUnit = "";
             if(splitValues.length > 1){
             	displayUnit = splitValues[1];	
				if(displayUnit.equals("undefined"))
             	{
             		displayUnit = "";
             	}
             }
             
        	String [] Param_values ={strObjectId,values, displayUnit};
        	
        	setParameterString(context,JPO.packArgs(Param_values));
        	return "";
        	
        }
        else
        {
        // Get the server URL to create a document/manage OLE objects
        int limitURL = newValue.indexOf("|");
        if (limitURL < 0) return "";
        
        String serverURL = newValue.substring(0, limitURL);
        
        // Get the format to know how to save the data (RTF or HTML)
        int limitFormat = newValue.indexOf("|", limitURL + 1);
        if (limitFormat < 0) return "";
        
        String formatToSave = newValue.substring(limitURL + 1, limitFormat);
        
        // We can save the data in the database
        DomainObject dmoObject = DomainObject.newInstance(context, strObjectId);
        ContextUtil.startTransaction(context, true);
        
        // We get the timeStamp
        Long timeStamp = new Random().nextLong();
        try {
            timeStamp = eMatrixDateFormat.getJavaDate(dmoObj.getInfo(context, DomainConstants.SELECT_MODIFIED)).getTime();
        } catch (Exception ex) {
            // NOP
        }
        
        // NOT USE TODAY. Will be use when Aspose will be available
        // WARNING: the user is going to erase data with another format, we can save the previous content
        // with a reference document
        if (!dmoObject.getAttributeValue(context, "Content Type").equals(formatToSave) && ReqSchemaUtil.isRequirement(context, strObjectId)) {
            // RTF -> HTML
            if (formatToSave.equals("html") && paramMap.get("isNewObject") == null) {
            	
            	String reqContentData = dmoObject.getAttributeValue(context, "Content Data");
            	
            	// If the old content is not empty
            	if (reqContentData.length() != 0) {
	                ReferenceDocumentUtil refDoUtil = new ReferenceDocumentUtil();
            		//RequirementsUtil refDoUtil = new RequirementsUtil();
	                
	                // Doc name
	                String refDocName = "RTF-Backup-" + timeStamp + ".rtf"; 
	                String refDocTitle = "RTF-Backup";
	                String refDocDescription = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.RichTextEditor.BackUp.RefDocDescription");
	                CommonDocument refDoc = refDoUtil.createAndConnectRefDocument(context, strObjectId, refDocName, refDocTitle, refDocDescription);
	                
	                // Check-in
	                try(ByteArrayInputStream bInputStream = new ByteArrayInputStream(Base64.decode(dmoObject.getAttributeValue(context, "Content Data")));
		                GZIPInputStream gInputStream = new GZIPInputStream(bInputStream))
		            {
		                refDoUtil.checkInDocument(context, refDoc, refDocName, gInputStream);
	                }
            	}
            }
        }

        // Save the content data
        int limitDataRichText = newValue.lastIndexOf("|");
        String contentText = new String(Base64.decode(newValue.substring(limitDataRichText + 1)), StandardCharsets.UTF_8);
        String richTextData = newValue.substring(limitFormat + 1, limitDataRichText);

    	IRichEditUtil.Format f = formatToSave.equals("ooxml") ? IRichEditUtil.Format.DOCX_BASE64 : IRichEditUtil.Format.HTML_EXTERNAL_RCO_BASE64;
    	RichEditFactory.getRichEditUtil().saveContent(context, strObjectId, f, richTextData, null, contentText);
        ContextUtil.commitTransaction(context);
        return "";
    }
}
    

  // Function moved to ${CLASS:emxPLCCommonBase}.java
//1. setRichTextContentFromDirectCall()
//2. createTempFileForRefDocument()
//3. attachRefDocumentFromTempFile()
    
    public String runJavascriptLoader(Context context, String[] args) throws Exception {
    	Map programMap = (Map) JPO.unpackArgs(args);
    	String objectId = (String)((Map)programMap.get("paramMap")).get("objectId");
    	
    	if("TRUE".equalsIgnoreCase(DomainObject.newInstance(context, objectId).getInfo(context, "current.access[modify]"))) {
        // To switch between view mode and edit mode
        return "<script type=\"text/javascript\"> " + 
          "function RMTDblClickToEdit(){" + 
          "var urlToLoad = location.href; " +
          "if (location.href.indexOf('mode=view') > 0) " +
          "    urlToLoad = location.href.replace('mode=view', 'mode=Edit'); " +
          "if (urlToLoad.indexOf('mode=Edit') < 1) " +
          "    urlToLoad += '&mode=Edit'; " +
          "toggleMode(urlToLoad); " +
          "}" + 
            " $(document).ready(function() { " +
//                "$('.field').dblclick(function() { " +
                "$(document).on('dblclick', '.field', function() { " +
                  "RMTDblClickToEdit();" + 
                    "}" + 
                 "); }); </script>";
    	}
    	return "";
    }
    
    // ++ HAT1 ZUD: HL -  (xHTML editor for Use case.) To enable Content column for Use Cases.
	/**
     * Returns a boolean value if CK editor to be display at place of normal text editor.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - None
     * @return the link
     * @throws Exception
     */
    public static boolean isDescriptionCKEditorAvailable(Context context, String[] args)
    {
    	//By-default Description field with CK editor will be displayed.
	    return true;
    }
    
	/**
     * Returns a boolean value if CK editor to be display at place of normal text editor.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - None
     * @return the link
     * @throws Exception
     */
    public static boolean isDescriptionTextEditorAvailable(Context context, String[] args)
    {
    	//By-default Description field with Text editor will be hidden.
	    return false;
    }
    
    
    public String setRichTextContentNewObject(Context context, String[] args) throws Exception {
        // We use a different path in the logic if it's a new object
        Map programMap = (HashMap) JPO.unpackArgs(args);
        Map paramMap = (HashMap) programMap.get("paramMap");
        paramMap.put("isNewObject", "true");
        JPO.packArgs(programMap);
        return updateRichTextContent(context, args);
    } 
    /*
    public String getHTMLFromDirectRichText(Context context, String[] args) throws Exception {
        Map argsMap = (HashMap) JPO.unpackArgs(args);
        String objectId = (String) argsMap.get("objectId");
        String richTextContent = (String) argsMap.get("RTFContent");
        String htmlPreview = (String) argsMap.get("htmlPreview");

        if ("false".equalsIgnoreCase(htmlPreview))
            return "";
        
        // Convert the RTF data to HTML
        ByteArrayInputStream bInputStream = new ByteArrayInputStream(Base64.decode(richTextContent));
        GZIPInputStream gInputStream = new GZIPInputStream(bInputStream);
        StringBuilder stringBuilder = new StringBuilder();
        byte[] data = IOUtils.toByteArray(gInputStream);
        bInputStream.close();
        gInputStream.close();

        String strData = new String(data);
        int sizePureData = strData.length();
        StringBuilder sb = new StringBuilder(strData);

        RMTConvertor convertor = RMTConvertor.Factory.create(context, objectId, EConvertorSettings.RTF2HTML, sb);
        return convertor.convert(SubConvertorSet.ASPOSE);
    }
    */
    public Vector getValueContent(Context context, String[] args) throws Exception {
        Map inputMap = (Map) JPO.unpackArgs(args);
        Vector returnVector = new Vector();

        MapList objectMap = (MapList) inputMap.get("objectList");

        Iterator objectItr = objectMap.iterator();
        ArrayList<String> objectIdsForParam = new ArrayList<String>();
        while (objectItr.hasNext()) {
            Map<String, String> curObjectMap = (Map) objectItr.next();
            objectIdsForParam.add((String) curObjectMap.get("id"));
        }

        for (MapList paramMapList : (List<MapList>) getAssociatedObjectsForRequirement(context, objectIdsForParam)) {
            String concatParam = "";
            for (int i = 0; i < paramMapList.size(); i++) {
                Hashtable<String, String> currentParam = (Hashtable<String, String>) paramMapList.get(i);
                String currentObjectId = currentParam.get(SELECT_ID);
                
                DomainObject dmoParam = DomainObject.newInstance(context, currentObjectId);
                String paramName = dmoParam.getInfo(context, SELECT_NAME);
                String paramTitle = dmoParam.getAttributeValue(context, "Title");
                
                HashMap argsParamValue = new HashMap();
                argsParamValue.put("objectList", paramMapList);
                
                /* concatParam += "<a href=\"javascript:link('" + currentParam.get(SELECT_LEVEL) + "','"
                        + currentObjectId + "','" + currentParam.get("id[connection]") + "', '" + paramName
                        + "')\" class=\"\">" + paramTitle + "</a>" + 
                        " | " + ${CLASS:emxParameterEdit}.getParameterValue(context, JPO.packArgs(argsParamValue)).get(i) + " | " +
                        ${CLASS:emxParameterEdit}.getParameterMinValue(context, JPO.packArgs(argsParamValue)).get(i) + " | " +
                        ${CLASS:emxParameterEdit}.getParameterMaxValue(context, JPO.packArgs(argsParamValue)).get(i) +
                        "<br />";
                */
            }
            returnVector.add(concatParam);
        }
        return returnVector;
    }
    
    private List<MapList> getAssociatedObjectsForRequirement(Context context, List<String> objectIds) throws Exception {
        ArrayList<MapList> paramsList = new ArrayList();

        for (String objectId : objectIds) {
            /* HashMap<String, String> argsParam = new HashMap<String, String>();
            argsParam.put("objectId", objectId);
            String toTypeName = PlmParameterUtil.TYPE_PLMPARAMETER; 
            StringList relationships = new StringList();
            relationships.add(PlmParameterUtil.RELATIONSHIP_PARAMETER_USAGE); */
            /* relationships.add(PlmParameterUtil.RELATIONSHIP_PARAMETER_AGGREGATION);
            paramsList.add((MapList) PlmParameterConnectUtil.getConnectedObjects(context, objectId, toTypeName, relationships));
            */ 
            paramsList.add(new MapList());
        }
        return paramsList;
    }
    
    // START:lx6:IR-234604V6R2014
    public List getStatusIcon(Context context, String[] args) throws Exception {
        // unpack the arguments
        Map programMap = (HashMap) JPO.unpackArgs(args);
        List lstobjectList = (MapList) programMap.get("objectList");
        Iterator objectListItr = lstobjectList.iterator();
        // initialise the local variables
        Map objectMap = new HashMap();
        String strObjId = DomainConstants.EMPTY_STRING;
        String strObjState = DomainConstants.EMPTY_STRING;
        String strIcon = DomainConstants.EMPTY_STRING;
        // Begin of Add by Enovia MatrixOne for Bug # 312021 Date Nov 16, 2005
        String strObjPolicy = DomainConstants.EMPTY_STRING;
        String strObjPolicySymb = DomainConstants.EMPTY_STRING;
        String strObjStateSymb = DomainConstants.EMPTY_STRING;
        StringBuffer sbStatePolicyKey = new StringBuffer();
        boolean flag = false;
        // End of Add by Enovia MatrixOne for Bug # 312021 Date Nov 16, 2005
        List lstNameRev = new StringList();
        StringBuffer stbNameRev = new StringBuffer(100);
        DomainObject domObj = null;
        
        HashMap paramList = (HashMap) programMap.get("paramList");
        String strExport = (String) paramList.get("exportFormat");
        boolean toExport = false;
        if (strExport != null) 
                toExport = true;
        int iNumOfObjects = lstobjectList.size();
        String arrObjId[] = new String[iNumOfObjects];
        List columnTags = new Vector(iNumOfObjects);
        int iCount;
        // Getting the bus ids for objects in the table
        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            arrObjId[iCount] = (String) ((Map) lstobjectList.get(iCount)).get(DomainConstants.SELECT_ID);
        }
        StringList selects = new StringList();
        selects.addElement(DomainConstants.SELECT_POLICY);
        selects.addElement(DomainConstants.SELECT_CURRENT);
        MapList columnData = DomainObject.getInfo(context, arrObjId, selects);
        String denied = EnoviaResourceBundle.getProperty(context, ReqConstants.BUNDLE_FRAMEWORK , context.getLocale(), "emxFramework.Basic.DENIED");
        // loop through all the records
        for(int i=0;i<columnData.size();i++){
        	Map values = (Map)columnData.get(i);
            String readAccess = (String)((Map)lstobjectList.get(i)).get(ReqConstants.SELECT_READ_ACCESS);
            if(ReqConstants.DENIED.equals(readAccess)){
                lstNameRev.add(denied);
                continue;
            }
        	strObjPolicy = (String)values.get(DomainConstants.SELECT_POLICY);
        	strObjState = (String)values.get(DomainConstants.SELECT_CURRENT);
        	strObjPolicySymb = FrameworkUtil.getAliasForAdmin(context, DomainConstants.SELECT_POLICY, strObjPolicy,
                    true);
            strObjStateSymb = FrameworkUtil.reverseLookupStateName(context, strObjPolicy, strObjState);

            // Forming the key which is to be looked up
            sbStatePolicyKey = new StringBuffer("emxRequirements.LCStatusImage.");
            sbStatePolicyKey.append(strObjPolicySymb).append(".").append(strObjStateSymb);

            // Geeting the value for the corresponding key, if not catching it
            // to set flag = false
            try {
                strIcon = EnoviaResourceBundle.getProperty(context, sbStatePolicyKey.toString());
                flag = true;
            } catch (Exception ex) {
                flag = false;
            }

            if (flag) {
                strObjState = FrameworkUtil.findAndReplace(strObjState, " ", "");
                StringBuffer sbStateKey = new StringBuffer("emxFramework.State.");
                sbStateKey.append(strObjPolicy.replaceAll(" ", "_")).append(".").append(strObjState);
                strObjState = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",
                        context.getLocale(), sbStateKey.toString());
                
                
                if(toExport) {
                    lstNameRev.add(strObjState);
                } else {
                    stbNameRev.delete(0, stbNameRev.length());
                    stbNameRev = stbNameRev.append("<img src=\"" + strIcon).append("\" border=\"0\"  align=\"middle\" ")
                            .append("TITLE=\"").append(" ").append(strObjState).append("\"").append("/>").append("<span style=\"display:none\">" + strObjState + "</span>");
                    lstNameRev.add(stbNameRev.toString());
                }
            } else {
                lstNameRev.add(DomainConstants.EMPTY_STRING);
            }
        }
        return lstNameRev;
    }
    // END:lx6:IR-234604V6R2014
    
    /**
     * Edit access function for program or programHTMLOutput columns. 
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return List of true/false values
     * @throws Exception if the operation fails
     * @since R2014
     */
    public List isEditableColumn(Context context, String[] args) throws Exception {
        Map programMap = (HashMap) JPO.unpackArgs(args);
        Map columnMap = (HashMap<String, String>) programMap.get("columnMap");
        MapList objectList = (MapList) programMap.get("objectList");

        boolean isBusinessColumn = columnMap.get("expression_businessobject") != null ? true : false;
        boolean isRSColumn = columnMap.get("expression_relationship") != null ? true : false;


        int iNumOfObjects = objectList.size();
        StringList editableValues = new StringList();
        for (int i = 0; i < iNumOfObjects; i++) {
            editableValues.add(true + ""); //cell editability already controlled by RowEditable flag
        }
        String currentColumnName = (String)columnMap.get("name");
        Map Settings = (Map)columnMap.get("settings");
        String columnType = (String)Settings.get("Column Type");
        //String fieldType = (String)Settings.get("Field Type");
        StringList attrTypeList = null;
        String select = null;
        if("programHTMLOutput".equalsIgnoreCase(columnType) || "program".equalsIgnoreCase(columnType)){
        	attrTypeList = getColumnAttributeTypes(context, objectList, columnMap);
        }
        // BUS object
        if (isBusinessColumn) {
            for (int i = 0; i < iNumOfObjects; i++) {
                Map<String, String> currentObjectMap = (Map<String, String>) objectList.get(i);

                if ("readonly".equalsIgnoreCase(currentObjectMap.get("ObjEditable"))) {
                	editableValues.set(i, false + ""); //regular attribute columns
                	continue;
                }
                
                if("programHTMLOutput".equalsIgnoreCase(columnType) || "program".equalsIgnoreCase(columnType)){
                	String attrType = (String)attrTypeList.get(i);
                    if (attrType == null || EMPTY_STRING.equals(attrType.trim())) {
                    	editableValues.set(i, false + "");
                    }
                }
            }
            
        // RS object
        } else if (isRSColumn) {
            for (int i = 0; i < iNumOfObjects; i++) {
                Map<String, String> currentObjectMap = (Map<String, String>) objectList.get(i);

                if("programHTMLOutput".equalsIgnoreCase(columnType) || "program".equalsIgnoreCase(columnType)){
                	String attrType = (String)attrTypeList.get(i);
                    if (attrType == null || EMPTY_STRING.equals(attrType.trim())) {
                    	editableValues.set(i, false + "");
                    }
                }
            }
        }
        return (StringList) editableValues;
    }
    
    /**
     * Check whether an attribute column is applicable to each object in the list . 
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return List of String values; blank value indicating the attribute not applicable to that particular object
     * @throws Exception if the operation fails
     * @since R2014
     */
    StringList getColumnAttributeTypes(Context context, MapList objectList, Map columnMap) throws Exception {
        int iNumOfObjects = objectList.size();
        if (iNumOfObjects == 0) {
            return new StringList(0);
        }
        String[] ids = new String[iNumOfObjects];
        boolean boColumn = columnMap.containsKey("expression_businessobject");
        String idSelect = boColumn ? DomainConstants.SELECT_ID : DomainConstants.SELECT_RELATIONSHIP_ID;
        for (int i = 0; i < iNumOfObjects; i++) {
            ids[i] = (String) ((Map) objectList.get(i)).get(idSelect);
        }

        if (ids[0] == null || ids[0].equals("")) {
            if (iNumOfObjects == 1) {
                return new StringList("");
            } else {
                ids[0] = ids[1]; // root relId could be null;
            }
        }

        StringList attrTypeList = new StringList(iNumOfObjects);
        String select = boColumn ? (String) columnMap.get("expression_businessobject") : (String) columnMap
                .get("expression_relationship");
        StringList selects = new StringList();
        int attrind = select.indexOf("attribute[");
        if (attrind >= 0) {
        	select = select.substring(attrind, select.indexOf("]", attrind) + 1) + ".type.name";
        	selects.addElement(select);
            MapList attrTypes = null;
            if (boColumn) {
                attrTypes = DomainObject.getInfo(context, ids, selects);
            } else {
                attrTypes = DomainRelationship.getInfo(context, ids, selects);
            }
            for (int i = 0; i < iNumOfObjects; i++) {
                attrTypeList.add(((Map) attrTypes.get(i)).get(select));
            }
        } else {// assuming it's basic attribute
            for (int i = 0; i < iNumOfObjects; i++) {
                attrTypeList.add("basic");
            }
        }
        if (ids[0] == null) {
            attrTypeList.set(0, "");
        }
        return attrTypeList;
    }
    
    //START lx6 IR-239404V6R2014x STP: Incorrect information is being displayed on Lock for Edit     
    public String getUser(Context context, String[] args) throws Exception
        {
    		return context.getUser();
        }
    //END lx6 IR-239404V6R2014x STP: Incorrect information is being displayed on Lock for Edit    
    //END lx6 IR-239404V6R2014x STP: Incorrect information is being displayed on Lock for Edit
    
    
    public static boolean isGraphAvailable(Context context, String[] args) throws FrameworkException{
    	boolean isGraphAvailable = false;
    	String value = EnoviaResourceBundle.getProperty(context,"emxRequirements.solidWorksGraph.isGraphAvailable");
    	if(value != null && value.equalsIgnoreCase("true")){
    		isGraphAvailable  = true;
    	}
    	return isGraphAvailable;
    }
    
    public List isAllocStatusColumnEditable(Context context, String[] args) throws Exception {
    	Map programMap = (HashMap) JPO.unpackArgs(args);
        Map columnMap = (HashMap<String, String>) programMap.get("columnMap");
        MapList objectList = (MapList) programMap.get("objectList");
        int iNumOfObjects = objectList.size();
        StringList editableValues = new StringList();
        for (int i = 0; i < iNumOfObjects; i++) {
        	if(((String)((Map)objectList.get(i)).get("level")).equalsIgnoreCase("0")){
        		editableValues.add(false); 
        	}else{
        		editableValues.add(true);
        	}
        }
        return (StringList)editableValues;
    }
    
    public static boolean isSCEUsed(Context context, String[] args) throws Exception {
	    return RequirementsUtil.isSCEUsed(context, args);
    }
    
    public static boolean isRTFControlUsed(Context context, String[] args) throws Exception {
    	return RequirementsUtil.isRTFControlUsed(context, args);
    }
    
    
    //START :IR-327057-3DEXPERIENCER2016 : In the "compare structure" window , some columns value appears in english, while the browser is in french 
    public static List getAttributeTranslation (Context context,String args[]) throws Exception {
        Map programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get("objectList");
        Map columnMap = (Map) programMap.get("columnMap");
        String columnAttribute = (String) (columnMap.containsKey("expression_businessobject")?
                columnMap.get("expression_businessobject"):
                columnMap.get("expression_relationship"));
        if(columnAttribute.contains("[")){
        	columnAttribute = columnAttribute.substring(columnAttribute.indexOf("[")+1, columnAttribute.lastIndexOf("]"));
        }
        HashMap paramList = (HashMap) programMap.get("paramList");       
        Iterator itr = relBusObjPageList.iterator();
        List ValuesList = new Vector(relBusObjPageList.size());
        String strAttributeValue="";
        while(itr.hasNext()){
        	Map map = (Map)itr.next();
        	if(columnMap.containsKey("expression_businessobject")){
	        	String strId = (String)map.get("id");
	        	DomainObject dmoObj = DomainObject.newInstance(context, strId);
	            strAttributeValue = dmoObj.getAttributeValue(context, columnAttribute);
	            if(!strAttributeValue.isEmpty()){
	            	if(columnAttribute.equalsIgnoreCase(ReqSchemaUtil.getRequirementClassificationAttribute(context))){
	            		strAttributeValue = strAttributeValue.replace("-", "");
	            		strAttributeValue = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Range.Classification."+strAttributeValue);
	            	}else if(columnAttribute.equalsIgnoreCase(ReqSchemaUtil.getPriorityAttribute(context))){
	            		strAttributeValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Range.Priority."+strAttributeValue);
	            	}
	            }
        	}else{
        		if(columnAttribute.equalsIgnoreCase("type")){
        			strAttributeValue = (String)map.get("relationship");
        			if(strAttributeValue!=null){
        				strAttributeValue = strAttributeValue.replace(" ", "_");
            			strAttributeValue = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(), "emxFramework.Relationship."+strAttributeValue);
        			}else{
        				strAttributeValue = "";
        			}
        			
        		}
        	}
            ValuesList.add(strAttributeValue);
        }
        
        if(programMap!=null){
        	programMap=null;
        }
        if(relBusObjPageList!=null){
        	relBusObjPageList=null;
        }
        if(columnMap!=null){
        	columnMap=null;
        }
    	return ValuesList;
    	
    }
    //END : IR-327057-3DEXPERIENCER2016 : In the "compare structure" window , some columns value appears in english, while the browser is in french
    //START LX6 FUN054695 ENOVIA GOV TRM Revision refactoring
    public static boolean isDoReconcileOnReviseAvailable(Context context, String[] args) throws Exception {
    	Map programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get("objectList");
        Map columnMap = (Map) programMap.get("columnMap");
    	String doReconcileOnRevise = EnoviaResourceBundle.getProperty(context, "emxRequirements.ImplementLink.DoReconcileOnRevise");
    	boolean showCmd=doReconcileOnRevise!=null&&doReconcileOnRevise.equalsIgnoreCase("true")?true:false;
	    return showCmd;
    }
    
    public static boolean hideUpdateRevisionCmd(Context context, String[] args) throws Exception {
    	Map programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get("objectList");
        Map columnMap = (Map) programMap.get("columnMap");
    	String doReconcileOnRevise = EnoviaResourceBundle.getProperty(context, "emxRequirements.ImplementLink.DoReconcileOnRevise");
    	boolean showCmd=doReconcileOnRevise!=null&&doReconcileOnRevise.equalsIgnoreCase("true")?false:true;
	    return showCmd;
    }
    
   public boolean hideSubTypeCommand(Context context, String[] args) throws Exception{
    	String contextUserRole = context.getRole();
    	boolean showCmd = false;
    	if(contextUserRole!= null && contextUserRole.startsWith("ctx::"))
    	{
    		String[] userRole = contextUserRole.split("::");
    		if(userRole[1]!= null)
    		{
    			String[] role = userRole[1].split("[\\.]");
    			if(role.length >1 )
    			{
    				if("VPLMAdmin".equals(role[0].trim()))
    				{

    					showCmd = true;
    				}
    			}
    		}
    	}
    	return showCmd;
     }
    
    //START LX6 FUN054695 ENOVIA GOV TRM Revision refactoring
    
    // ++ HAT1 ZUD : LA Settings for ReqSpec Dependency HL
    public static boolean isCheckConsistencyAvailable(Context context, String[] args)
    {
    	String doReconcileOnRevise = "";
		try {
			doReconcileOnRevise = EnoviaResourceBundle.getProperty(context, "emxRequirements.ManageReqSpecDependencies");
		} catch (FrameworkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	boolean showCmd=doReconcileOnRevise!=null&&doReconcileOnRevise.equalsIgnoreCase("true")?true:false;
	    return showCmd;
    }
    // -- HAT1 ZUD : LA Settings for ReqSpec Dependency HL

// ++ KIE1 ZUD LA functionality for TRM Adoption Of ECM
    public boolean hideECMCommand(Context context, String[] args) throws Exception{
    	
    	boolean showCmd = true;
    		try{
    			String useECM = EnoviaResourceBundle.getProperty(context,"emxRequirements.TRMChange.useECM");
    			boolean flag =useECM!=null&&useECM.equalsIgnoreCase("false")?false:true; 
        		return 	flag;
    			
    		}catch(Exception e){
    			return showCmd;
    		}
    }
	// -- KIE1 ZUD LA functionality for TRM Adoption Of ECM
    public Object getFirstRevision(Context context, String[] args) throws Exception {
    	Policy policyObj = null;
    	String strPolicy = "";  
    	Map programMap = (HashMap) JPO.unpackArgs(args);
    	Map requestMap = (Map)programMap.get("requestMap");
    	String strSymbolicPolicy = (String)requestMap.get("policy");
    	strPolicy = PropertyUtil.getSchemaProperty(context, strSymbolicPolicy);
    	if(strPolicy==null||strPolicy.isEmpty()){
    		String strSymbolicType = (String)requestMap.get("type");
    		String[] parsedString = strSymbolicType.split("[,]");
    		if(parsedString.length>0){
    			strSymbolicType = parsedString[parsedString.length-1];
    		}
    		String strType = PropertyUtil.getSchemaProperty(context, strSymbolicType);  
    		strType = strType.replace(" ", "");
    		String property = "emxRequirements.Default.Creation.Default" + strType + "Policy";
    		strSymbolicPolicy = EnoviaResourceBundle.getProperty(context, property);
    		strPolicy = PropertyUtil.getSchemaProperty(context, strSymbolicPolicy);
    		if(strPolicy == null||strPolicy.isEmpty()){
    			String noPolicyProperty = "emxRequirements.Alert.NoPolicyAssociated";
    			String noPolicy = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(), "emxRequirements.Alert.NoPolicyAssociated");
    			throw new Exception(noPolicy);
    		}
    	}
    	policyObj = new Policy (strPolicy);
    	policyObj.open(context);
    	String revision = "";
    	if(policyObj.hasMajorSequence(context)&&policyObj.hasMinorSequence(context)){
    		String firstMajSeq = policyObj.getFirstInMajorSequence(context);
    		String firstMinSeq = policyObj.getFirstInMinorSequence(context);
    		String delimiter = policyObj.delimiter(context)==null?"-":policyObj.delimiter(context);
    		revision = firstMajSeq + delimiter + firstMinSeq;
    	}else if(policyObj.hasMajorSequence(context)){
    		revision = policyObj.getFirstInMajorSequence(context);
    	}else{
    		revision = policyObj.getFirstInMinorSequence(context);
    	}
    	policyObj.close(context);
	    return revision;
    }

// ++ HAT1 ZUD: Populating title as per autoName of Name in Web form.
	/**
     * Method to fill web form Name field using autoName.
     * @param context
     * @param args
     * @return String Name field value
     * @throws Exception
     */
    public String getNameFieldValue(Context context, String[] args) throws Exception
    {
    	Map programMap = (HashMap) JPO.unpackArgs(args);
    	Map requestMap = (HashMap) programMap.get("requestMap");
    	Map fieldMap   = (HashMap) programMap.get("fieldMap");
        String name = (String)fieldMap.get("name");
    	
        String type = (String)requestMap.get("type");
        if(type.startsWith("_selectedType:")) {
        	type = type.substring("_selectedType:".length());
        }
        type = type.split("[,]", -1)[0];

        String strName = "";
        
        if(name.equalsIgnoreCase("Name"))
        {
        	
        	if(type.startsWith("type_")){
        		type = PropertyUtil.getSchemaProperty(context, type);
        	}
            strName = UnifiedAutonamingServices.autoname(context, type);
            	
        }

        return strName;
    }
    
	/**
     * Method set Name field value for RMT objects.
     * @param context
     * @param args
     * @return (String) Name field value
     * @throws Exception
     */
    public String SetNameFieldValue(Context context, String[] args) throws Exception 
    {
    	Map programMap = (HashMap) JPO.unpackArgs(args);
    	Map fieldMap   = (HashMap) programMap.get("fieldMap");
    	String name = (String)fieldMap.get("name");
        Map paramMap = (HashMap) programMap.get("paramMap");
        String newValue = (String) paramMap.get("New Value");
        String strObjectId = (String) paramMap.get("objectId");
        DomainObject domObj = DomainObject.newInstance(context, strObjectId);
        String type = domObj.getTypeName();
        ContextUtil.startTransaction(context, true);
        if(name.equalsIgnoreCase("Name"))
        	domObj.setName(context,newValue);
        ContextUtil.commitTransaction(context);
        
        
    	return newValue;
    }
    // -- HAT1 ZUD: Populating title as per autoName of Name in Web form.
    
    // ++ HAT1 ZUD: IR-455208-3DEXPERIENCER2017x fix 
	/**
     * Method gets type value of the object.
     * @param context
     * @param args
     * @return (String) of icon and Type value
     * @throws Exception
     */
    public String getTypeValue(Context context, String[] args) throws Exception 
    {
    	Map programMap = (HashMap) JPO.unpackArgs(args);
        Map busObjPageList = (HashMap) programMap.get("paramMap");
        String strObjectId = (String)busObjPageList.get("objectId");
        
        //Getting object type 
        DomainObject domObj = DomainObject.newInstance(context, strObjectId);        
        StringList selBUS           = new StringList();
        selBUS.add(DomainConstants.SELECT_TYPE);
        Map mData 		    = domObj.getInfo(context, selBUS);
        String sType 		= (String)mData.get(DomainConstants.SELECT_TYPE);
        
        // KIE1 HAT1 : IR-466458-3DEXPERIENCER2017x
        String _sType = sType.replaceAll(" ", "_"); 
        //icon for object
        StringBuffer sb = new StringBuffer();
        sb.append("  <img class='typeIcon' ");
        sb.append(" src='../common/images/").append(UINavigatorUtil.getTypeIconProperty(context, sType)).append("' />");
        sb.append(" "); 
        
        //key value for type from properties files
        Locale strLocale = context.getLocale();
		String strObjectType = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",strLocale, "emxFramework.Type." + _sType);
		sb.append(strObjectType);
        
    	return sb.toString();
    }
    
    // -- HAT1 ZUD: IR-455208-3DEXPERIENCER2017x fix 

    
// Added function for showing Active Change Order/Action On structure browser of Requirement Specification & Requirement
    // KIE1 ZUD : IR-437621-3DEXPERIENCER2016x
    public List getActiveCOIcon(Context context, String[] args) throws Exception{

    	String POLICY_CHANGE_ACTION = PropertyUtil.getSchemaProperty(context,"policy_ChangeAction");
    	String STATE_CHANGE_ACTION_COMPLETE = PropertyUtil.getSchemaProperty(context,"policy", POLICY_CHANGE_ACTION, "state_Complete");
    	String STATE_CHANGE_ACTION_HOLD = PropertyUtil.getSchemaProperty(context,"policy", POLICY_CHANGE_ACTION, "state_OnHold");
        String STATE_CHANGE_ACTION_CANCEL = PropertyUtil.getSchemaProperty(context,"policy", POLICY_CHANGE_ACTION, "state_Cancelled");
        String TYPE_CHANGE_ACTION = PropertyUtil.getSchemaProperty(context,"type_ChangeAction");
        String RELATIONSHIP_IMPLEMENTED_ITEM = PropertyUtil.getSchemaProperty(context,"relationship_ImplementedItem");
        String RELATIONSHIP_CHANGE_AFFECTED_ITEM = PropertyUtil.getSchemaProperty(context,"relationship_ChangeAffectedItem");
        
        Map programMap = (HashMap) JPO.unpackArgs(args);
        MapList relBusObjPageList = (MapList) programMap.get("objectList");
        Map paramList = (HashMap)programMap.get("paramList");
        String reportFormat = (String)paramList.get("reportFormat");
        int iNumOfObjects = relBusObjPageList.size();
        List lstActiveECIcon= new Vector(iNumOfObjects);
        String strActiveECIconTag = "";
        String strIcon = EnoviaResourceBundle.getProperty(context,"emxFramework.smallIcon.type_ChangeOrder");
        String strTooltipActiveECIcon = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(),"emxRequirements.Change.ToolTipActiveChangeExists");

        String arrObjId[] = new String[iNumOfObjects];
        int iCount;
        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            Object obj = relBusObjPageList.get(iCount);
            if (obj instanceof HashMap) {
                arrObjId[iCount] = (String)((HashMap)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
            }
            else if (obj instanceof Hashtable)
            {
                arrObjId[iCount] = (String)((Hashtable)relBusObjPageList.get(iCount)).get(DomainConstants.SELECT_ID);
            }
        }
        StringList lstSelect = new StringList();
        String strStateSelect ="to["+ RELATIONSHIP_CHANGE_AFFECTED_ITEM +"].from."+ DomainConstants.SELECT_CURRENT;
        DomainObject.MULTI_VALUE_LIST.add(strStateSelect);
        lstSelect.addElement(strStateSelect);
        MapList attributeList = DomainObject.getInfo(context, arrObjId, lstSelect);
        DomainObject.MULTI_VALUE_LIST.remove(strStateSelect);

        for (iCount = 0; iCount < iNumOfObjects; iCount++) {
            Map strStateList = (Map)attributeList.get(iCount);
            boolean activeEC = false;
            if(strStateList.containsKey(strStateSelect)){
                StringList slTmpState = (StringList)strStateList.get(strStateSelect);
                for (int j = 0; j < slTmpState.size(); j++) {
                    String strTmpState=(String)slTmpState.get(j);
                    if (strTmpState == null||strTmpState.equals("")||"null".equals(strTmpState) || "#DENIED!".equals(strTmpState)
                            ||strTmpState.equals(STATE_CHANGE_ACTION_COMPLETE)||strTmpState.equals(STATE_CHANGE_ACTION_HOLD)||strTmpState.equals(STATE_CHANGE_ACTION_CANCEL)){
                        activeEC = false;
                    } else {
                        activeEC = true;
                        break;
                    }
                }
            }

            if(activeEC) {
                if (reportFormat != null && !("null".equalsIgnoreCase(reportFormat)) && reportFormat.length()>0){
                    lstActiveECIcon.add(strTooltipActiveECIcon);
                }else{
                    strActiveECIconTag =
                            "<img src=\"../common/images/"
                                    + strIcon
                                    + "\" border=\"0\"  align=\"middle\" "
                                    + "TITLE=\""
                                    + " "
                                    + strTooltipActiveECIcon
                                    + "\""
                                    + "/>";
                }
            } else {
                strActiveECIconTag = " ";
            }
            lstActiveECIcon.add(strActiveECIconTag);
        }
        return lstActiveECIcon;
    }
    //  ++ KIE1 ZUD IR-448762-3DEXPERIENCER2017x: for Tree Preferences in Struture Browser
	/**
     * Method get Tree display settings for the structure browser.
     * @param context
     * @param args
     * @return (String) xmlData
     * @throws Exception
     */
    public static String getTreeDisplaySettingsForStrutureBrowser(Context context, String[] args)
    {
    	String xmlData = "";
    	String treeDisplaySettings_ReqGroup   = "";
    	String treeDisplaySettings_ReqSpec   = "";
    	String treeDisplaySettings_Req   = "";
    	String treeDisplaySettings_chap   = "";
    	String treeDisplaySettings_comm   = "";
    	String treeDisplaySettings_testCase   = "";
    	String treeDisplaySettings_para   = "";
    	String treeDisplaySettings_Default = "";
    	String displayedColumn = "";
    	
    	try {
	    		treeDisplaySettings_ReqGroup = getTreeDisplaySettings(context, JPO.packArgs(RequirementsUtil.getRequirementGroupType(context)));
	    		treeDisplaySettings_ReqSpec = getTreeDisplaySettings(context, JPO.packArgs(RequirementsUtil.getRequirementSpecificationType(context)));
	    		treeDisplaySettings_Req = getTreeDisplaySettings(context, JPO.packArgs(RequirementsUtil.getRequirementType(context)));
	    		treeDisplaySettings_chap = getTreeDisplaySettings(context, JPO.packArgs(RequirementsUtil.getChapterType(context)));
	    		treeDisplaySettings_comm = getTreeDisplaySettings(context, JPO.packArgs(RequirementsUtil.getCommentType(context)));
	    		treeDisplaySettings_testCase = getTreeDisplaySettings(context, JPO.packArgs(RequirementsUtil.getTestCaseType(context)));
	    		treeDisplaySettings_para = getTreeDisplaySettings(context, JPO.packArgs(RequirementsUtil.getParameterType(context)));
	    		treeDisplaySettings_Default = "treeDisplayName=" + "" + "|treeDisplayTitle=" + "treeDisplayTitle" + "|treeDisplayRevision=" + "treeDisplayRevision" + "|treeDisplaySeperation=" + "" + "|treeDisplayTitleMaxSize=" + "";
	    		
	    		String title = PropertyUtil.getAdminProperty(context, PersonUtil.personAdminType, context.getUser(), "preference_selectedColumnDisplayTitle");
	    		String name = PropertyUtil.getAdminProperty(context, PersonUtil.personAdminType, context.getUser(), "preference_selectedColumnDisplayName");
	    		if((title.equalsIgnoreCase("") || title.equalsIgnoreCase("false")) && (name.equalsIgnoreCase("") || name.equalsIgnoreCase("")))
	    		{
	    			displayedColumn = "Title";
	    		}
	    		if(name.equalsIgnoreCase("ColumnDisplayName"))
	    		{
	    			displayedColumn = "Name";
	    		}
	    		if(title.equalsIgnoreCase("ColumnDisplayTitle"))
	    		{
	    			if(!displayedColumn.equalsIgnoreCase(""))
	    			{
	    				displayedColumn += "_";
	    			}
	    			displayedColumn += "Title";
	    		}
	    	    		
    		xmlData += "<r RequirementGroup=\""+treeDisplaySettings_ReqGroup+"\" RequirementSpecification=\""+treeDisplaySettings_ReqSpec+"\" Requirement=\""+treeDisplaySettings_Req+"\" Chapter=\""+treeDisplaySettings_chap+"\" Comment=\""+treeDisplaySettings_comm+"\" TestCase=\""+treeDisplaySettings_testCase+"\" PlmParameter=\""+treeDisplaySettings_para+"\" displayedColumn=\""+displayedColumn+"\" treeDisplaySettings_Default=\""+treeDisplaySettings_Default+"\"> </r>";
    		
    	}catch(Exception e) {
    		return xmlData;
    	}
    	return xmlData;
    }
    // -- KIE1 ZUD IR-448762-3DEXPERIENCER2017x :for Tree Preferences in Struture Browser

    // ++ HAT1 ZUD: ooxml to html conversion.
    public void ooxmlToHTML(Context context, String[] args) throws Exception
    {
		String[] objectIDs = args;

		for(int i=0; i < objectIDs.length; i++)
		{
			DomainObject DomObj = DomainObject.newInstance(context, (String)objectIDs[i]);
			JPO.invoke(context, "emxRichEditor", null,"generateHTMLPreview", 
    			new String[]{objectIDs[i], RichEditUtil.PREFIX_ATT + RichEditUtil.DEFAULT_ATT_NAME + ".docx", RichEditUtil.FORMAT_DOC} );
    	}    	
    }
    // -- HAT1 ZUD: ooxml to html conversion.
 // Will hide the column
    //++KIE1 ZUD added for Structure Browser and Dyna Tree
  	public boolean hideColumnOfStructureBrowser(Context context, String[] args)throws Exception 
 	{
  		Map programMap = (HashMap) JPO.unpackArgs(args);
    	Map fieldMap   = (HashMap) programMap.get("SETTINGS");
    	String tableName   = (String) programMap.get("href");
    	String title = (String)fieldMap.get("isTitle");
    	String selectedColumnDisplayTitle        = "";
    	String selectedColumnDisplayName		 = "";
    	String strToSwitch 						 = "";
    	
    	selectedColumnDisplayTitle        = PropertyUtil.getAdminProperty(context, PersonUtil.personAdminType, context.getUser(), "preference_selectedColumnDisplayTitle");
    	selectedColumnDisplayName         = PropertyUtil.getAdminProperty(context, PersonUtil.personAdminType, context.getUser(), "preference_selectedColumnDisplayName");
    	    	
    	if(title != null)
		{
    		if(title.startsWith("true"))
    		{
    			if((!selectedColumnDisplayName.equalsIgnoreCase("false") && !selectedColumnDisplayName.equalsIgnoreCase("")) && (!selectedColumnDisplayTitle.equalsIgnoreCase("false")))
    			{
    				strToSwitch = selectedColumnDisplayTitle;
    			}
    		}
    		if(title.startsWith("false"))
    		{
    			if((selectedColumnDisplayTitle.equalsIgnoreCase("") || selectedColumnDisplayTitle.equalsIgnoreCase("false")) && (selectedColumnDisplayName.equalsIgnoreCase("") || selectedColumnDisplayName.equalsIgnoreCase("")))
        		{
        			strToSwitch = "ColumnDisplayTitle";
        		}
    			
    			if(selectedColumnDisplayName.equalsIgnoreCase("false") && !selectedColumnDisplayTitle.equalsIgnoreCase("false"))
    			{
    				strToSwitch = selectedColumnDisplayTitle;
    			}
    		}
		}
    	else
    	{
    		if(!selectedColumnDisplayName.equalsIgnoreCase("") && !selectedColumnDisplayName.equalsIgnoreCase("false"))
    		{
    			strToSwitch = selectedColumnDisplayName;
    		}
    	}
    	
      switch(strToSwitch)
	     {
	     	case "ColumnDisplayName":
	     		  return true;
	     	case "ColumnDisplayTitle":
	     		  return true;
	     	default:
	     		  return false;
          }
 	}
  // --KIE1 ZUD added for Structure Browser and Dyna Tree
// ++ HAT1 ZUD: IR-481137-3DEXPERIENCER2018x fix ++
    /**
     * Returns a boolean value true if Type column is to show in Table RMTFullTraceabilityTable.
     * @param context - the eMatrix <code>Context</code> object
     * @param args - None
     * @return the link
     * @throws Exception
     */
    public static boolean showTypeClmRMTFullTraceabilityTable(Context context, String[] args)
    {
    	//By-default 'Type column' will not be available.
	    return false;
    }
    // -- HAT1 ZUD: IR-481137-3DEXPERIENCER2018x fix -- 
	
	 public Object getExpandLevel(Context context, String[] args) throws Exception
    {
    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
    	HashMap fieldMap = (HashMap) programMap.get("fieldMap");
    	HashMap requestMap = (HashMap) programMap.get("requestMap");
		HashMap requestValuesMap = (HashMap)requestMap.get("RequestValuesMap");
		StringBuffer strBuf = new StringBuffer(256);
		// the Expand level drop down
        strBuf.append("<table><td>");
        strBuf.append("<SELECT name=\"ExpandLevel\" > <OPTION value=\"1\" SELECTED >1 <OPTION value=\"2\">2 <OPTION value=\"3\">3 <OPTION value=\"4\">4 <OPTION value=\"5\">5 <OPTION value=\"0\">"+EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(),"emxRequirements.ExpandLevel.All")+" </SELECT>");
        strBuf.append("</td><td><div>&nbsp;&nbsp;&nbsp</div></td><td>");
        strBuf.append("</td></table>");
		
		String sobjectId =(String)requestMap.get("objectId1");
         if(null == sobjectId)
         {
        	 sobjectId =(String)requestMap.get("objectId");

        	 if(null == sobjectId && null!=requestValuesMap && !"null".equals(requestValuesMap))
        	 {
        		 String[] objectId1 = (String[])requestValuesMap.get("objectId");
        		 sobjectId = objectId1[0];
        	 }
         }
         
         if (null != sobjectId && !"null".equals(sobjectId))
         {
        		DomainObject domObj = DomainObject.newInstance(context, sobjectId);
                String strName =  domObj.getInfo(context,DomainObject.SELECT_NAME);
                
                strBuf.append("<input type=\"hidden\" name=\"RSP1PreloadName\" value=\""+XSSUtil.encodeForHTMLAttribute(context,strName)+"\">");
  		        strBuf.append("<input type=\"hidden\" name=\"RSP1PreloadID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sobjectId)+"\">");
 				strBuf.append("<input type=\"hidden\" name=\"RSP1NameDispOID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sobjectId)+"\">");
         }else{
        	 	strBuf.append("<input type=\"hidden\" name=\"RSP1PreloadName\" value=\""+XSSUtil.encodeForHTMLAttribute(context,"")+"\">");
		        strBuf.append("<input type=\"hidden\" name=\"RSP1PreloadID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,"")+"\">");
				strBuf.append("<input type=\"hidden\" name=\"RSP1NameDispOID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,"")+"\">");
         }
         
         sobjectId =(String)requestMap.get("objectId2");
         		
         if(null == sobjectId && null!=requestValuesMap && !"null".equals(requestValuesMap))
         {
              	String[] objectId2 = (String[])requestValuesMap.get("objectId2");
               	sobjectId = objectId2[0];
         }
         if(null == sobjectId && null!=requestValuesMap && !"null".equals(requestValuesMap))
         {
            	String[] objectId2 = (String[])requestValuesMap.get("objectId2");
               	sobjectId = objectId2[0];
         }
         
         if (null != sobjectId && !"null".equals(sobjectId))
         {
        		DomainObject domObj = DomainObject.newInstance(context, sobjectId);
                String strName =  domObj.getInfo(context,DomainObject.SELECT_NAME);
                
                strBuf.append("<input type=\"hidden\" name=\"RSP2PreloadName\" value=\""+XSSUtil.encodeForHTMLAttribute(context,strName)+"\">");
  		        strBuf.append("<input type=\"hidden\" name=\"RSP2PreloadID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sobjectId)+"\">");
 				strBuf.append("<input type=\"hidden\" name=\"RSP2NameDispOID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,sobjectId)+"\">");
         }else{
        	 	strBuf.append("<input type=\"hidden\" name=\"RSP2PreloadName\" value=\""+XSSUtil.encodeForHTMLAttribute(context,"")+"\">");
		        strBuf.append("<input type=\"hidden\" name=\"RSP2PreloadID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,"")+"\">");
				strBuf.append("<input type=\"hidden\" name=\"RSP2NameDispOID\" value=\""+XSSUtil.encodeForHTMLAttribute(context,"")+"\">");
         }
    	         
         return strBuf.toString();
    }
    
    public Object getMatchBasedOn (Context context,String[] args)throws Exception
    {
		MapList columns = null;
		Map TableMap = null;
		Map TableSettingMap = null;
		String strMBO = null;
		String sLabel = null;
		String sStrResourceValue = null;
		StringBuffer strBuf = new StringBuffer();
		StringBuffer strBuf1 = new StringBuffer();
		StringBuffer strBuf2 = new StringBuffer();
		UITableCommon uiTable = new UITableCommon();
		// to get all the table columns and assign it to map
		String sIgnoredColumnTypes = UIStructureCompare.getIgnoredColumnTypes();
		columns = uiTable.getColumns(context, PropertyUtil.getSchemaProperty(
				context, "table_RMTSpecStructureCompare"), null);
		int MapListSize = columns.size();

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");

		String sobjectId = (String) requestMap.get("objectId");

		StringList strlNotCompare= FrameworkUtil.split(sIgnoredColumnTypes, ",");
		strBuf.append("<SELECT name=\"MatchBasedOn\" onchange=\"javascript:onChangeMatchBasedOn(\'MatchBasedOn\');\">");
		
		strBuf1.append("<SELECT name=\"MatchBasedOn1\" onchange=\"javascript:onChangeMatchBasedOn(\'MatchBasedOn1\');\">");
		strBuf1.append("<OPTION value=None");
		strBuf1.append(">");
		strBuf1.append("None");
		strBuf1.append("</OPTION>");
		
		strBuf2.append("<SELECT name=\"MatchBasedOn2\" disabled onchange=\"javascript:onChangeMatchBasedOn(\'MatchBasedOn2\');\">");
		strBuf2.append("<OPTION value=None");
		strBuf2.append(">");
		strBuf2.append("None");
		strBuf2.append("</OPTION>");
		
		StringList strColmJSLabelList=new StringList(MapListSize);
		StringList strColmLabelList = new StringList(MapListSize);
		for(int i=0;i<MapListSize;i++)
		{
		    Map mtemp=(Map)columns.get(i);
		    Map mSetting=(Map)mtemp.get("settings");
		    String columnName = (String) mtemp.get("label");
		    sLabel = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(),columnName);
		    String strColumnType=(String)mSetting.get("Column Type");
		    String strComparable=(String)mSetting.get("Comparable");
			if(strColumnType!=null || "".equals(strColumnType)){
				if(!strlNotCompare.contains(strColumnType)){
					if( (strComparable==null) || "".equals(strComparable) || !"false".equals(strComparable)){
						strBuf.append("<OPTION value=\"");
						// Add the display value to the string buffer
						strBuf.append(XSSUtil.encodeForHTMLAttribute(context,
								sLabel));
						strBuf.append("\">");
						strBuf.append(XSSUtil.encodeForHTMLAttribute(context,
								sLabel));
						
						strBuf.append("</OPTION>");
						
						strBuf1.append("<OPTION value=\"");
						// Add the display value to the string buffer
						strBuf1.append(XSSUtil.encodeForHTMLAttribute(context,
								sLabel));
						strBuf1.append("\">");
						strBuf1.append(XSSUtil.encodeForHTMLAttribute(context,
								sLabel));
						
						strBuf1.append("</OPTION>");
						
						strBuf2.append("<OPTION value=\"");
						// Add the display value to the string buffer
						strBuf2.append(XSSUtil.encodeForHTMLAttribute(context,
								sLabel));
						strBuf2.append("\">");
						strBuf2.append(XSSUtil.encodeForHTMLAttribute(context,
								sLabel));
						
						strBuf2.append("</OPTION>");
					}
				}
			}
			else{
				strBuf.append("<OPTION value=\"");
				// Add the display value to the string buffer
				strBuf.append(XSSUtil.encodeForHTMLAttribute(context,
						sLabel));
				strBuf.append("\">");
				strBuf.append(XSSUtil.encodeForHTMLAttribute(context,
						sLabel));
				  strBuf.append("</OPTION>");
				  
				  strBuf1.append("<OPTION value=\"");
					// Add the display value to the string buffer
					strBuf1.append(XSSUtil.encodeForHTMLAttribute(context,
							sLabel));
					strBuf1.append("\">");
					strBuf1.append(XSSUtil.encodeForHTMLAttribute(context,
							sLabel));
					
					strBuf1.append("</OPTION>");
				  
				  strBuf2.append("<OPTION value=\"");
					// Add the display value to the string buffer
					strBuf2.append(XSSUtil.encodeForHTMLAttribute(context,
							sLabel));
					strBuf2.append("\">");
					strBuf2.append(XSSUtil.encodeForHTMLAttribute(context,
							sLabel));
					
					strBuf2.append("</OPTION>");
			}
		}
		  strBuf.append("</SELECT>");
		  strBuf1.append("</SELECT>");
		  strBuf.append(strBuf1);
		  strBuf2.append("</SELECT>");
		  strBuf.append(strBuf2);
		return strBuf.toString();
	}
    
    /**
     * Gives the list of Report Differences on criteria for comparison.
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @returns an Object containing HTML output for Report Differences for comparison.
     * @throws Exception if the operation fails.
     */
 	public Object getReportDifferences(Context context, String[] args)
 			throws Exception {
 		MapList columns = null;
 		Map TableMap = null;
 		Map TableSettingMap = null;
 		String strComparable = null;
 		String strFieldLabel = null;
 		String strFieldValue = null;
 		String sVal = null;

 		StringBuilder strBuf = new StringBuilder(512);
 		UITableCommon uiTable = new UITableCommon();
         String strRMTVisualCompareTable = PropertyUtil
 		.getSchemaProperty(context,"table_RMTSpecStructureCompare");
 		columns = uiTable.getColumns(context, strRMTVisualCompareTable,
 				null);
 		int MapListSize = columns.size();
 		int cnt = 0;
 		boolean isField = false;
 	
 		String sSelectAll = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(),"emxRequirements.label.SelectAll");
        
 		if (MapListSize > 0)
 		{
 			String strFieldNameForCtrl = "";
 			strBuf.append("<table>");
 			for (int i = 0; i < MapListSize; i++)
 			{
 				isField = false;
 				TableMap = (Map) columns.get(i);
 				TableSettingMap = (Map) TableMap.get("settings");
 				strComparable = (String) TableSettingMap.get("Comparable");
 				strFieldLabel = (String) TableMap.get("label");
 				if (strFieldLabel != null && !"null".equals(strFieldLabel))
 				{
 						strFieldValue = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", context.getLocale(),strFieldLabel);
 						strFieldNameForCtrl = EnoviaResourceBundle.getProperty(context, "emxRequirementsStringResource", new Locale("en"),strFieldLabel);
 						sVal = strFieldNameForCtrl.replace(" ", "_");
 					
 						if( (strComparable==null) || "".equals(strComparable) || !"false".equals(strComparable))
	 					{
	 						cnt++;
	 						isField = true;
	 						if( i == 0)
	 						{
	 							strBuf.append("<td>&nbsp;<input type=\"checkbox\" id=\"repDiffChk\" name=\"" + sVal
		 								+ "\" value=\"true\" disabled/>"+"   " + strFieldValue+ "&nbsp;</td>");
	 						}else{
		 						strBuf.append("<td>&nbsp;<input type=\"checkbox\" id=\"repDiffChk\" name=\"" + sVal
		 								+ "\" value=\"true\"/>"+"   " + strFieldValue+ "&nbsp;</td>");
	 						}
	 					}
 				}
 				else
 				{
 					continue;
 				}

 				if(cnt >= 3 && cnt % 3 == 0 && isField)
 				{
 					strBuf.append("<tr></tr>");
 				}
 			}
 			strBuf.append("</table>");
 		}
 		strBuf.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
 		strBuf.append("<br>");

 		strBuf.append("&nbsp;<input type =\"checkbox\" name = selectAll onclick=\"javascript:selectAllOptions(\'repDiffChk\');\"> ");
 		strBuf.append(sSelectAll);
 		return strBuf.toString();
 }
}
