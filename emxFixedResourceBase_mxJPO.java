/*
 ** emxFixedResourceBase
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 ** static const char RCSID[] = $Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/${CLASSNAME}.java 1.6.2.1.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental$
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Person;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.RuleProcess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
//Added by Enovia MatrixOne on 20-May-2005 for Bug# 304800
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;



/**
 * This JPO class has some method pertaining to Fixed Resource type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxFixedResourceBase_mxJPO extends emxDomainObject_mxJPO
{

    /**
    * Alias used for string current.
    */
    protected static final String CURRENT = DomainConstants.SELECT_CURRENT;
    /**
    * Alias used for string policy.
    */
    protected static final String POLICY = DomainConstants.SELECT_POLICY;
    /**
    * Alias used for type fixed resource.
    */
    protected static final String TYPE_FIXED_RESOURCE = ProductLineConstants.TYPE_FIXED_RESOURCE;
    /**
    * Alias used for key emxProduct.ResourceEdit.
    */
    protected static final String KEY_FIRST_PART = "emxProduct.ResourceEdit";
    /**
    * Alias used for string NotAllowedStates.
    */
    protected static final String KEY_LAST_PART = "NotAllowedStates";
    /**
    * Alias used for bundle string.
    */
    protected static final String BUNDLE_STR = "emxConfiguration";
    /**
    * Alias used for dot.
    */
    protected static final String DOT = ".";
    /**
    * Alias used for feature option seperator.
    */
    protected static final String FO_SEPARATOR = "~";
    


/**
  * Default Constructor.
  * @param context the eMatrix <code>Context</code> object
  * @param args holds no arguments
  * @throws Exception if the operation fails
  * @since ProductCentral 10-0-0-0
  * @grade 0
  */
  emxFixedResourceBase_mxJPO (Context context, String[] args) throws Exception
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
  * @since ProductCentral 10-0-0-0
  * @grade 0
  */
  public int mxMain(Context context, String[] args) throws Exception
  {
    if (!context.isConnected()){
         String strContentLabel =EnoviaResourceBundle.getProperty(context,
        	        "Configuration","emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
         throw  new Exception(strContentLabel);
        }
    return 0;
  }


 /**
  * Method call to get all the Fixed Resources in the data base.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the following input arguments:
  *        0 - HashMap containing one String entry for key "objectId"
  * @return Object - MapList containing the id of Fixed Resource objects
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  */
  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getAllFixedResources(Context context, String[] args) throws Exception
  {
    /* obtain the Object id of the Product */
    HashMap fixedResourcesMap = (HashMap) JPO.unpackArgs(args);
    String strObjectId = (String)fixedResourcesMap.get("objectId");
    strObjectId =  strObjectId.trim();
    DomainObject domParentId = new DomainObject(strObjectId);
    // System.out.println("OBJECT ID of the Product: "+ strObjectId);
    // System.out.println("Objects of Fixed Resource:  "+ fixedResourcesMap);
    StringList objSelects =new StringList(DomainConstants.SELECT_ID);
    StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
    short sRecurse = 1;
    String strObjPattern = ProductLineConstants.TYPE_FIXED_RESOURCE;
    String strRelPattern = ProductLineConstants.RELATIONSHIP_RESOURCE_LIMIT;
    /* Fetch the Fixed Resources conected to the Product by Resource Limit Relationship */
    MapList fixedResourcesMapList = new MapList();
    fixedResourcesMapList = domParentId.getRelatedObjects(context, strRelPattern,
                    strObjPattern, objSelects, relSelects, true, true, sRecurse,
                    "", "", 0);
    // System.out.println("MAP"+ fixedResourcesMapList);
    return fixedResourcesMapList;
  }


/**
  * Method call(Column JPO) to get the maximum values of the Fixed Resources in the data base.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args - Holds the parameters passed from the calling method
  * @return Object - Vector containing the Textboxes having the maximum valued of the Fixed Resources
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  * 
  */

public Vector getMaxValues (Context context, String[] args)
throws Exception
  {
     String strAttributeName = "maxValue";
     String strAttributeSelect = ProductLineConstants.SELECT_FIXED_RESOURCE_MAXIMUM;
     return getHTMLTags(context,args,strAttributeName,strAttributeSelect);
    }


/**
  * Method call(Column JPO) to get the minimum values of the Fixed Resources in the data base.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args - Holds the parameters passed from the calling method
  * @return Object - Vector containing the Textboxes having the maximum valued of the Fixed Resources
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  * 
  */
public Vector getMinValues (Context context, String[] args)
throws Exception
  {
     String strAttributeName = "minValue";
     String strAttributeSelect = ProductLineConstants.SELECT_FIXED_RESOURCE_MINIMUM;
     return getHTMLTags(context,args,strAttributeName,strAttributeSelect);
    }

/**
  * Method call(Column JPO) to get the Initial Resource Values of the Fixed Resources in the data base.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args - Holds the parameters passed from the calling method
  * @return Object - Vector containing the Textboxes having the maximum valued of the Fixed Resources
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  * 
  */


public Vector getInitialValues (Context context, String[] args)
throws Exception
  {
     String strAttributeName = "initialValue";
     String strAttributeSelect = ProductLineConstants.SELECT_FIXED_RESOURCE_INITIAL;
     return getHTMLTags(context,args,strAttributeName,strAttributeSelect);

  }



/**
  * Method call(Column JPO) to get the Comment Values of the Fixed Resources in the data base.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args - Holds the parameters passed from the calling method
  * @return Object - Vector containing the Textboxes having the maximum valued of the Fixed Resources
  * @throws Exception if the operation fails
  * @since ProductCentral 10.0.0.0
  * @grade 0
  * 
  */


public Vector getCommentValues (Context context, String[] args)
throws Exception
  {
    String strAttributeName = "comment";
    String strAttributeSelect = ProductLineConstants.SELECT_COMMENT;
    return getHTMLTags(context,args,strAttributeName,strAttributeSelect);

  }


/**
  * This method forms the html for column jpo methods.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the following input arguments:
  *        0 - HashMap containing Maplist of object ids
  * @param strAttributeName - Holds the attribute name for which the html needs to be formed
  * @param strAttributeSelect - Holds the select expression for the attribute
  * @return Object - Vector containing the Textboxes having the maximum valued of the Fixed Resources
  * @throws Exception if the operation fails
  * @since ProductCentral 10.5
  * @grade 0
  * 
  */

    protected Vector getHTMLTags(Context context, String args[],String strAttributeName, String strAttributeSelect) throws Exception
    {
        //Getting the ProgramMap from args
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        //getting objectList from programMap
        MapList relBusObjPageList = (MapList)programMap.get("objectList");
        if (!(relBusObjPageList != null))
            throw  new Exception("Error!!! Context does not have any Objects.");
        int iNoOfObjects = relBusObjPageList.size();
        /* Obtain the Ids of the Fixed Resources */
        String arrObjId[] = new String[iNoOfObjects];
        for (int i = 0; i < iNoOfObjects; i++)
            arrObjId[i] = (String)((Map)relBusObjPageList.get(i)).get(DomainConstants.SELECT_ID);
        //The return vector
        Vector vctDetails = new Vector(iNoOfObjects);
        //StringList containing the selects
        StringList select = new StringList(1);
        select.addElement(strAttributeSelect);
        String strAttributeValue;
        //Strings for the common prefixes and suffixes
        String strFieldPrefix = "<input type=\"text\" size=\"10\" name=\"";
        String strFieldSuffix = "\"></input>";
        //if the attribute is Comment
        if (strAttributeName.equals("comment"))
        {
            strFieldPrefix = "<textarea name = \"txtComment\">";
            strFieldSuffix = "</textarea>";
        }
        //if the attribute is initial value
        else if (strAttributeName.equals("initialValue"))
        {
            StringBuffer sFieldPrefix = new StringBuffer(strFieldPrefix);
            sFieldPrefix.append("txtInitial\" value=\"");
            strFieldPrefix = sFieldPrefix.toString();
        }
        //If the attribute is Minimum value
        else if (strAttributeName.equals("minValue"))
        {
            StringBuffer sFieldPrefix = new StringBuffer(strFieldPrefix);
            sFieldPrefix.append("txtMin\" value=\"");
            strFieldPrefix = sFieldPrefix.toString();
        }
        //if the attribute is maximum value
        else if (strAttributeName.equals("maxValue"))
        {
            StringBuffer sFieldPrefix = new StringBuffer(strFieldPrefix);
            sFieldPrefix.append("txtMax\" value=\"");
            strFieldPrefix = sFieldPrefix.toString();
        }

        /* Prepare a TextBox/textarea containing the Initial resource entry for each Fixed Resource */
        for (int i = 0; i < iNoOfObjects; i++)
        {
          //Stringlist for getting the object ids
          StringBuffer sTableIds = new StringBuffer(90);
          //Where expression for findObjects
          String strWhereExp = "id == '" + arrObjId[i] + "'";
          MapList fixedResourcesMapList = DomainObject.findObjects(context, "*","*", strWhereExp.toString(),select);
          //traversing through the Fixed resources and getting the attribute values
          for(int index=0;index<fixedResourcesMapList.size();index++)
           {
             //String buffer for the html
             StringBuffer sTextBoxHTML = new StringBuffer(100);
             //Getting the attribute value from the fixed resources maplist
             strAttributeValue = (String)((Map)fixedResourcesMapList.get(index)).get(strAttributeSelect);
             //Forming the html
             sTextBoxHTML = sTextBoxHTML.append(strFieldPrefix);
             sTextBoxHTML = sTextBoxHTML.append(strAttributeValue);
             sTextBoxHTML = sTextBoxHTML.append(strFieldSuffix);
             //Adding the object ids as a hidden field if the attribute name is max value
             if (strAttributeName.equals("maxValue"))
             {
                 sTableIds = sTableIds.append("<input type=\"hidden\" size=\"10\" name=\"txtRowIds\" value=\"");
                 sTableIds = sTableIds.append(arrObjId[i]);
                 sTableIds = sTableIds.append("\"></input>");
                 sTextBoxHTML = sTextBoxHTML.append(sTableIds.toString());
             }
             /* To add  the string to the Vector */
             vctDetails.add(sTextBoxHTML.toString());
            }
          }
        return vctDetails;

    }





/**
  * Method call to check whether the Resource is in release or obsolete state.
  *
  * @param context the eMatrix <code>Context</code> object
  * @param args holds the following input arguments:
  *        0 - HashMap containing one String entry for key "objectId"
  * @return Object - A Boolean object
  * @throws Exception if the operation fails
  * @since ProductCentral 10-0-0-0
  * @grade 0
  */
    public Boolean editDetailsLinkDisplay(Context context, String[] args) throws Exception{
        HashMap argumentMap = (HashMap) JPO.unpackArgs(args);
        //System.out.println("--------emxFixedResourceBase---argumentMap = "+argumentMap);
        String strContextUser = context.getUser();
        String strObjectId = (String)argumentMap.get("objectId");
        Boolean b = Boolean.valueOf(false);

        boolean bHasAccess = false;
        boolean bIsAssigneeOrOwner = false;
        strObjectId = strObjectId.trim();
        setId(strObjectId);
        String strType = getInfo(context,DomainConstants.SELECT_TYPE);
        //get the current state of the  object
        String strCurrentState = getInfo(context,CURRENT);
        //System.out.println("strCurrentState->"+strCurrentState);
        //get the default policy of the type->
        String strPolicy = getDefaultPolicy(context,TYPE_FIXED_RESOURCE);
        //forming the key which will be present in the emxProduct.properties
        StringBuffer sKey = new StringBuffer(40);
        sKey  = sKey.append(KEY_FIRST_PART).append(DOT).append(strPolicy).append(DOT).append(KEY_LAST_PART);
        String strKey = sKey.toString();
        //get the Locale in the present context
        String strLocale = context.getSession().getLanguage();
        i18nNow i18nNowInstance = new i18nNow();
        //get the value corresponding to the key from the emxProduct.properties file
        //System.out.println("strKey->"+strKey);
        String strNotAllowedStates = i18nNowInstance.GetString(BUNDLE_STR,strLocale,strKey);
        //System.out.println("strNotAllowedStates->"+strNotAllowedStates);
        //check if the current state of the object is amongst the not allowed states

        //use the other person from common package
        //This checks whether th users has Product Manager or System Engineer role
        Person person= new Person(strContextUser);
        if((person.isAssigned(context,ProductLineConstants.ROLE_SYSTEM_ENGINEER))
            ||(person.isAssigned(context,ProductLineConstants.ROLE_PRODUCT_MANAGER))) {
                bHasAccess = true;
        }

        //to check whether the user is owner or assignee of the feature under which this fixed resource is created
        if (strType.equals(ProductLineConstants.TYPE_FIXED_RESOURCE))
        {
            StringList objectSelects = new StringList();
            objectSelects.addElement(DomainConstants.SELECT_ID);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_OWNER);
            //The associated object details are retreived onto a MapList
            String strRelationshipType = ProductLineConstants.RELATIONSHIP_RESOURCE_LIMIT;

            //getting the parent feature object's info
            Map mapFeatureInfo = getRelatedObject(
                    context,
                    strRelationshipType,
                    false,
                    objectSelects,
                    null);
            DomainObject obj = DomainObject.newInstance(context,(String)mapFeatureInfo.get(DomainConstants.SELECT_ID));

            List lstAssignees=
            obj.getRelatedObjects(
                    context,
                    ProductLineConstants.RELATIONSHIP_ASSIGNED_FEATURE,
                    "*",
                    objectSelects,
                    null,
                    true,
                    false,
                    (short) 1,
                    "",
                    "", 0);

            for(int iTmp=0;iTmp<lstAssignees.size();iTmp++)
            {
                //System.out.println("--------emxFixedResourceBase-- assignee = "+((Map)(lstAssignees.get(iTmp))).get(DomainConstants.SELECT_NAME)+"---strContextUser = "+strContextUser);
                //check if
                if(((String)mapFeatureInfo.get(DomainConstants.SELECT_OWNER)).equals(strContextUser) || (((Map)(lstAssignees.get(iTmp))).get(DomainConstants.SELECT_NAME).equals(strContextUser)))
                {
                    bIsAssigneeOrOwner = true;
                    break;
                }
                else
                {
                    bIsAssigneeOrOwner = false;
                }
            }
        }

        //if the user has access, now check if the state of the resource object is allowed or not
        if (bHasAccess || bIsAssigneeOrOwner)
        {
            if((strNotAllowedStates.indexOf(strCurrentState))!=-1){
                //the current state is amongst the not allowed state. in that case return false
                //this false will be used by the emxTable.jsp and the create new link wont be displayed
                b = Boolean.valueOf("false");
            }else{
                //the link will be displayed by the emxTable.jsp
                b = Boolean.valueOf("true");
            }
        }
        return b;
    }
    /**
     * Method call to get the Edit Link for Resource Rule Summary Page.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the following arguments 0 - HashMap containing the
     *            following arguments: 1:Relationship Id. 2:Feature Id.
     * @return Object - MapList containing the context name and ids.
     * @throws Exception
     *             if the operation fails
     * @since Product Central BX-3
     * @grade 0
     */
    public boolean getEditLink(Context context,
            String[] args)throws Exception{
        boolean hasAccess=editDetailsLinkDisplay(context,
                args).booleanValue();
        /* Removed the Inherited check due to New UI restriction not getting RelID.*/
        return hasAccess;
        
    }
//##Begin of Add by Enovia MatrixOne for Bug# 304800 on 20-May-05####
    /* This method is used to update the value of the Resource Usage attribute
     * during the edit all of resource usage.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return void
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6
     */

    public void updateResourceUsageUsage(Context context, 
                                                         String[] args) 
                                                        throws Exception{

      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      Map mpParamMap = (HashMap)programMap.get("paramMap");
      String strNewVal = (String)mpParamMap.get("New Value");
      String strRelId = (String)mpParamMap.get("relId");
      try{
      //Set the context to the super user
           ContextUtil.pushContext(
                   context, PropertyUtil.getSchemaProperty(
                           context, "person_UserAgent"),
                   DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
      DomainRelationship.setAttributeValue(
                         context,
                         strRelId,
                         ProductLineConstants.ATTRIBUTE_RESOURCE_USAGE,
                         strNewVal);
      }catch(Exception e){
          throw new FrameworkException(e);
      }finally{
          //Set the context back to the context user
            ContextUtil.popContext(context);
      }
    }//end of the method

    /* This method is used to update the value of the Resource Operation attribute
     * during the edit all of resource usage.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return void
     * @throws Exception if the operation fails
     * @since ProductCentral 10-6
     */

    public void updateResourceUsageOperation(Context context, 
                                                         String[] args) 
                                                        throws Exception{

      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      Map mpParamMap = (HashMap)programMap.get("paramMap");
      String strNewVal = (String)mpParamMap.get("New Value");
      String strRelId = (String)mpParamMap.get("relId");
      try{
      //Set the context to the super user
           ContextUtil.pushContext(
                   context, PropertyUtil.getSchemaProperty(
                           context, "person_UserAgent"),
                   DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
      DomainRelationship.setAttributeValue(
                         context,
                         strRelId,
                         ProductLineConstants.ATTRIBUTE_RESOURCE_OPERATION,
                         strNewVal);
      }catch(Exception e){
          throw new FrameworkException(e);
      }finally{
          //Set the context back to the context user
            ContextUtil.popContext(context);
      }
    }//end of the method
//##End of Add by Enovia MatrixOne for Bug# 304800 on 20-May-05####
  /**
   * Method call to get all the Fixed Resources in the data base  connected to given context.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - HashMap containing one String entry for key "objectId"
   * @return Object - MapList containing the id of Fixed Resource objects
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   * @grade 0
   * @deprecated
   */
  public MapList getAllSearchResourceRule(Context context, String[] args) throws Exception
  {
    HashMap resourceRule = (HashMap) JPO.unpackArgs(args); 
   
    String contextId = (String)resourceRule.get("hdnType");
    DomainObject domainObject = new DomainObject(contextId);
    StringList objSelects =new StringList(DomainConstants.SELECT_ID);   
    StringList relSelects = new StringList();
    short sRecurse = 1;
    String strObjPattern = ProductLineConstants.TYPE_FIXED_RESOURCE;
    String strRelPattern = ProductLineConstants.RELATIONSHIP_RESOURCE_LIMIT; 
    MapList fixedResourcesMapList = new MapList();
    fixedResourcesMapList = domainObject.getRelatedObjects(context, strRelPattern,
                    strObjPattern, objSelects, relSelects, true, true, sRecurse,
                    "", "");      
    return fixedResourcesMapList;  
 }


/**
   * Method call to get all the Fixed Resources for EditAll in the data base.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - HashMap containing one String entry for key "objectId"
   * @return Object - MapList containing the id of Fixed Resource objects
   * @throws Exception if the operation fails
   * @since ProductCentral 10.0.0.0
   * @grade 0
   */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getAllFixedResourcesForEditAll(Context context, String[] args) throws Exception
   {
  
     HashMap fixedResourcesMap = (HashMap) JPO.unpackArgs(args);
     
     String strObjectId = (String)fixedResourcesMap.get("objectId"); 
     strObjectId =  strObjectId.trim();
     DomainObject domParentId = new DomainObject(strObjectId);
     StringList objSelects =new StringList(DomainConstants.SELECT_ID);
     StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
     short sRecurse = 1;
     String strObjPattern = ProductLineConstants.TYPE_FIXED_RESOURCE;
     String strRelPattern = ProductLineConstants.RELATIONSHIP_RESOURCE_LIMIT;    
     MapList fixedResourcesMapList = new MapList();
     
     
     StringBuffer buffer = new StringBuffer();    
     buffer.append("attribute");
     buffer.append("["+ConfigurationConstants.ATTRIBUTE_INHERITED+"]");
     buffer.append("!=");
     buffer.append("True");
    
     fixedResourcesMapList = domParentId.getRelatedObjects(context, strRelPattern,
                     strObjPattern, objSelects, relSelects, true, true, sRecurse,
                     "",buffer.toString(), 0);
     
     return fixedResourcesMapList;
   }

   
   /**
    * Method call(Column JPO) to get the Resource Usage of the Fixed Resources in the data base.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - HashMap containing one String entry for key "objectId"
    * @return Object - Vector containing the Textboxes having the maximum valued of the Fixed Resources
    * @throws Exception if the operation fails
    * @since R212
    * @grade 0
	* @author IXH
    */

  @com.matrixone.apps.framework.ui.ProgramCallable
  public MapList getAllFixedResourcesInUsage(Context context, String[] args) throws Exception
    {
     HashMap fixedResourcesMap = (HashMap) JPO.unpackArgs(args);
     
     /* To obtain the ProductId and the Resource Id */
     String strResourceId = (String)fixedResourcesMap.get("objectId");
     strResourceId =  strResourceId.trim();
       
     //Use Query connection
     /*StringList RelationshipSelect = new StringList(ConfigurationConstants.SELECT_RELATIONSHIP_ID); 
 	 RelationshipSelect.addElement(ConfigurationConstants.SELECT_NAME);
 	 RelationshipSelect.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_SUBFEATURECOUNT);
 	 RelationshipSelect.addElement("from.");
 	 RelationshipSelect.addElement("to.id");
 	 RelationshipSelect.addElement("fromrel.from.name");
 	 RelationshipSelect.addElement("fromrel.to.name");
 	 RelationshipSelect.addElement("fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME + "]");
 	 RelationshipSelect.addElement("fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME + "]");
 	 RelationshipSelect.addElement("fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME + "]");
 	RelationshipSelect.addElement("fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME + "]");
 	 
 	 StringList sLRelationshipWhere = new StringList();
 	 sLRelationshipWhere.addElement("to.id" + "==" + strResourceId);
 	
 	 //Use Query connection
 	 ProductLineCommon PL = new ProductLineCommon();
 	 MapList mLConfigFeatureRelId = PL.queryConnection(context,
 					ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE,
 					RelationshipSelect,
 					sLRelationshipWhere.toString());*/
 	 
     MapList mLConfigFeatureRelId = new MapList();
     StringList strSelect = new StringList();
     strSelect.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].id");
     strSelect.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].name");
     strSelect.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].attribute["+ ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT +"]");
     strSelect.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].to.id");
     strSelect.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.name");
     strSelect.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.name");
     strSelect.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME +"]");
     strSelect.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME +"]");
     strSelect.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME +"]");
     strSelect.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME +"]");
 	 
     DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].id");
     DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].name");
     DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].attribute["+ ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT +"]");
     DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].to.id");
     DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.name");
     DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.name");
     DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME +"]");
     DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME +"]");
     DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME +"]");
     DomainObject.MULTI_VALUE_LIST.add("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME +"]");
 	 
     DomainObject ResourceRuleObject = new DomainObject(strResourceId);
     Map mapOfRelIds = ResourceRuleObject.getInfo(context, strSelect);
     
     DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].id");
     DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].name");
     DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].attribute["+ ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT +"]");
     DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].to.id");
     DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.name");
     DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.name");
     DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME +"]");
     DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME +"]");
     DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME +"]");
     DomainObject.MULTI_VALUE_LIST.remove("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME +"]");
     
     StringList listOfRURelIds = (StringList) mapOfRelIds.get("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].id");
     StringList listOfRURelNames = (StringList) mapOfRelIds.get("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].name");
     StringList listOfSubFeatCountAttrVal = (StringList) mapOfRelIds.get("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].attribute["+ ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT +"]");
     StringList listOfRRIds = (StringList) mapOfRelIds.get("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].to.id");
     StringList listOfConfFeatNames = (StringList) mapOfRelIds.get("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.name");
     StringList listOfConfOptionNames = (StringList) mapOfRelIds.get("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.name");
     StringList listOfConfFeatDispName = (StringList) mapOfRelIds.get("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME +"]");
     StringList listOfConfFeatMarkNames = (StringList) mapOfRelIds.get("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME +"]");
     StringList listOfConfOptDispNames = (StringList) mapOfRelIds.get("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME +"]");
     StringList listOfConfOptMarkNames = (StringList) mapOfRelIds.get("to["+ ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE +"].fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME +"]");
     
     if(listOfRURelIds != null){
     for(int j = 0; j < listOfRURelIds.size(); j++)
     {
    	 Map mapObject = new HashMap();
    	 mapObject.put("RelInfo", ConfigurationConstants.RELATIONSHIP_RESOURCE_USAGE);
    	 mapObject.put("id[connection]", listOfRURelIds.get(j));
    	 mapObject.put("name", listOfRURelNames.get(j));
    	 mapObject.put("attribute["+ ConfigurationConstants.ATTRIBUTE_SUBFEATURECOUNT +"]", listOfSubFeatCountAttrVal.get(j));
    	 mapObject.put("fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME +"]", listOfConfOptMarkNames.get(j));
    	 mapObject.put("fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME +"]", listOfConfFeatDispName.get(j));
    	 mapObject.put("to.id", listOfRRIds.get(j));
    	 mapObject.put("fromrel.from.name", listOfConfFeatNames.get(j));
    	 mapObject.put("fromrel.to.name", listOfConfOptionNames.get(j));
    	 mapObject.put("fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME +"]", listOfConfOptDispNames.get(j));
    	 mapObject.put("fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME +"]", listOfConfFeatMarkNames.get(j));
    	 
    	 mLConfigFeatureRelId.add(mapObject);
     }
     }
     
     MapList ML = new MapList(mLConfigFeatureRelId.size());     
 	 for(int i=0;i<mLConfigFeatureRelId.size();i++){
 		 
 		 Map M1 = new HashMap();
 		 M1 = (Map)mLConfigFeatureRelId.get(i);
 		 M1.put("level", "1");
 		 ML.add(M1);
 	 }
 	 
     return ML;
    }
   
  /**
   * Method call(Column JPO) to get the Feature Option Pairs connected to a Product.
   *
   * @param context the eMatrix <code>Context</code> object
   * @param args holds the following input arguments:
   *        0 - HashMap containing Maplist of object ids
   * @return Object - Vector containing the Features and Options seperated by '~' the maximum valued of the Fixed Resources
   * @throws Exception if the operation fails
   * @since R212
   * @grade 0
   * @author IXH
   */

 public Vector getFeatureOptionPairsCoulumnInTable (Context context, String[] args)
 throws Exception
   {
      Vector vctDetails = new Vector();
      HashMap programMap = (HashMap)JPO.unpackArgs(args);
      MapList mLConfigFeatureRelId = (MapList)programMap.get("objectList");
      String strFName = "";
      String strFromName = "";
      String strOName = "";
      String strToName = "";
      String strFO = "";
      
      for(int i=0;i<mLConfigFeatureRelId.size();i++){

         Map MConfigFeatureRelIds = (Map)mLConfigFeatureRelId.get(i);
             strFromName = (String)MConfigFeatureRelIds.get("fromrel.from.name");
             strFName = (String)MConfigFeatureRelIds.get("fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME + "]");
             if(strFName==null || strFName.trim().isEmpty())
            	 strFName = (String)MConfigFeatureRelIds.get("fromrel.from.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME + "]");
             strToName = (String)MConfigFeatureRelIds.get("fromrel.to.name");
             strOName = (String)MConfigFeatureRelIds.get("fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_MARKETING_NAME + "]");
             if(strOName==null || strOName.trim().isEmpty())
            	 strOName = (String)MConfigFeatureRelIds.get("fromrel.to.attribute["+ ConfigurationConstants.ATTRIBUTE_DISPLAY_NAME + "]");
             int ruleDisplay=RuleProcess.getRuleDisplaySetting(context);
             if(ruleDisplay==RuleProcess.RULE_DISPLAY_FULL_NAME){
            	 strFO = strFromName+FO_SEPARATOR+strToName;
             }else{
            	 strFO = strFName+FO_SEPARATOR+strOName;
             }
             
             vctDetails.add(strFO);
         }

      return vctDetails;  
  }
 


 
   /**
    * Method call to get all the Fixed Resources in the data base in given context.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds the following input arguments:
    *        0 - HashMap containing one String entry for key "objectId"
    * @return Object - MapList containing the id of Fixed Resource objects
    * @throws Exception if the operation fails
    * @since R212
    * @grade 0
    */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getAllFixedResourcesObjects(Context context, String[] args) throws Exception
    {
    	/* obtain the Object id of the Product */
    	HashMap fixedResourcesMap = (HashMap) JPO.unpackArgs(args);
    	String strObjectId = (String)fixedResourcesMap.get("objectId");
    	strObjectId =  strObjectId.trim();
    	DomainObject domParentId = new DomainObject(strObjectId);

    	StringList objSelects =new StringList(DomainConstants.SELECT_ID);
    	objSelects.add(DomainConstants.SELECT_TYPE);
    	StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
    	relSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_MANDATORYRULE);
    	short sRecurse = 1;
    	String strObjPattern = ProductLineConstants.TYPE_FIXED_RESOURCE;
    	String strRelPattern = ProductLineConstants.RELATIONSHIP_RESOURCE_LIMIT;

    	/* Fetch the Fixed Resources conected to the Product by Resource Limit Relationship */
    	MapList fixedResourcesMapList = new MapList();
    	fixedResourcesMapList = domParentId.getRelatedObjects(context,
    			strRelPattern,
    			strObjPattern, 
    			objSelects, 
    			relSelects, 
    			true,
    			true,
    			sRecurse,
    			"", 
    			"",
    			0);
    	//make row read only if Rule is inherited
    	Iterator i = fixedResourcesMapList.iterator();
    	String relInheritedFrom = (String) PropertyUtil
    	.getSchemaProperty(context,"relationship_InheritedFrom");
    	String strRowEditable = "RowEditable";
    	String strReadOnly     = "readonly";
    	String strShow     = "show";
    	while (i.hasNext()) {
    		Map m = (Map) i.next();
    		String relId = (String) m
    		.get(DomainConstants.SELECT_RELATIONSHIP_ID);
    		String relType = (String) m.get("relationship");
    		String itrRelId = relId;
    		String tomidId = "";
    		int iCnt = 0;

    		while (true) {
    			//TO DO MQL Command
    			iCnt = iCnt + 1;
    			String strMqlCmd1 = "print connection $1 select $2 dump $3";
    			String selectable ="tomid[" + relInheritedFrom+ "].id";
    			tomidId = MqlUtil.mqlCommand(context, strMqlCmd1, true,itrRelId,selectable,ConfigurationConstants.DELIMITER_PIPE);
    			if (null == tomidId || "null".equals(tomidId)
    					|| "".equals(tomidId)) {
    				break;
    			}
    			else {
    				String strMqlCmd2 = "print connection $1 select $2 dump $3";
    				String selectable2 = "fromrel[" + relType + "].id";
    				itrRelId = MqlUtil.mqlCommand(context, strMqlCmd2, true,tomidId,selectable2,ConfigurationConstants.DELIMITER_PIPE);
    			}
    		}

    		if (iCnt == 1) {
    			m.put(strRowEditable,strShow);
    		}else{
    			m.put(strRowEditable,strReadOnly);
    		}
    	}
    	return fixedResourcesMapList;
    }

    
    /**
	 * Exclude program for add existing Fixed Resource
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeConnectedFixedResourceRule(Context context, String[] args) throws Exception
	{
		Map programMap = (Map) JPO.unpackArgs(args);
		
		// Logical Feature ID
		String relWhere = DomainObject.EMPTY_STRING;
		String objWhere = DomainObject.EMPTY_STRING;
				
		// Obj and Rel Selects
		StringList objSelects = new StringList();
		StringList relSelects = new StringList();

		String filterExpression = DomainObject.EMPTY_STRING;
		String objectId = (String) programMap.get("objectId");

		ConfigurationUtil confUtil = new ConfigurationUtil(objectId);
		MapList objectList = confUtil.getObjectStructure(context,
														ConfigurationConstants.TYPE_FIXED_RESOURCE,
														ConfigurationConstants.RELATIONSHIP_RESOURCE_LIMIT,
														objSelects, relSelects, false, true, (short) 1, 0, objWhere,
														relWhere, DomainObject.FILTER_ITEM, filterExpression);
		
		StringList FixedResToExclude = new StringList();
		for (int i = 0; i < objectList.size(); i++) {
			Map mapFRObj = (Map) objectList.get(i);
			if (mapFRObj.containsKey(DomainObject.SELECT_ID)) {
				String strFRIDToExclude = (String) mapFRObj.get(DomainObject.SELECT_ID);
				FixedResToExclude.add(strFRIDToExclude);
			}
		}
		return FixedResToExclude;
}
}
