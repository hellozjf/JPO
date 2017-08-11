/*
 * emxPLCTimelineBase
 * Copyright (c) 1992-2016 Dassault Systemes.
 * All Rights Reserved.
 * This program contains proprietary and trade secret
 * information of
 * MatrixOne, Inc. Copyright notice is precautionary only and
 * does
 * not evidence any actual or intended publication of such program.
 *
 * static const char RCSID[] =  $Id: /ENOProductLine/CNext/Modules/ENOProductLine/JPOsrc/base/${CLASSNAME}.java 1.7.2.1.1.1 Wed Oct 29 22:17:06 2008 GMT przemek Experimental$ *
 */

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
// Added by Enovia MatrixOne for bug 303522 on 3rd May,05
import java.util.Vector;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Vault;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;

import com.matrixone.apps.framework.ui.UITimeline;
import com.matrixone.apps.productline.ProductLineConstants;
//Added by Vibhu,Enovia MatrixOne for bug 303031 on 2nd May,05
import com.matrixone.apps.domain.util.eMatrixDateFormat;
// Added by Enovia MatrixOne for bug 303522 on 3rd May,05
import com.matrixone.apps.domain.util.DateUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;

/**
 * This JPO <code>emxPLCTimelineBase</code> contains method pertaining to the
 * product central timeline functionality.
 *
 * @author Enovia MatrixOne
 * @version ProductCentral10.6 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class  emxPLCTimelineBase_mxJPO extends  emxDomainObject_mxJPO  {
    /**
     * Alias for key objectConfig.
     */
    static protected final String SELECT_OBJECT_CONFIG = "objectConfig";

    /**
     * Alias for key mileStoneConfig.
     */
    static protected final String SELECT_MILESTONE_CONFIG = "milestoneConfig";

    /**
     * Alias for key iconMap.
     */
    static protected final String SELECT_MILESTONE_ICON_MAP = "iconMap";

    /**
     * Alias for key iconDirectory.
     */
    static protected final String SELECT_MILESTONE_ICON_DIRECTORY = "iconDirectory";

    /**
     * Alias for key milestoneRelationship.
     */
    static protected final String SELECT_MILESTONE_RELATIONSHIP = "milestoneRelationship";

    /**
     * Alias for key detailSelects.
     */
    static protected final String SELECT_DETAIL_SELECTS = "detailSelects";

    /**
     * Alias for key detailDisplayNames.
     */
    static protected final String SELECT_DETAIL_DISPLAY_NAMES = "detailDisplayNames";

    /**
     * Property key prefix for getting roadmap object title.
     */
    static protected final String ROADMAP_OBJECT_TITLE = "emxProduct.Roadmap.Object.Title";

    /**
     * Property key prefix for getting roadmap object details.
     */
    static protected final String ROADMAP_OBJECT_DETAILS = "emxProduct.Roadmap.Object.Details";

    /**
     * Property key prefix for getting roadmap object detail labels.
     */
    static protected final String ROADMAP_OBJECT_DETAIL_LABELS = "emxProduct.Roadmap.Object.DetailLabels";

    /**
     * Property key prefix for getting roadmap object default image.
     */
    static protected final String ROADMAP_OBJECT_IMAGE_DEFAULT = "emxProduct.Roadmap.Object.Image.Default";

    /**
     * Property key prefix for getting milestone icons.
     */
    static protected final String ROADMAP_MILESTONE_ICON = "emxProduct.Roadmap.Milestone.Icon";

    /**
     * Property key prefix for getting milestone type relationship.
     */
    static protected final String ROADMAP_MILESTONE_TYPE = "emxProduct.Roadmap.Milestone.Title";

    /**
     * Property key prefix for getting date attribute.
     */
    static protected final String ROADMAP_MILESTONE_DATE = "emxProduct.Roadmap.Milestone.Date";

    /**
     * Property key prefix for getting range value of date attribute.
     */
    static protected final String ROADMAP_MILESTONE_DATE_START = "emxProduct.Roadmap.Milestone.Date.Start";

    /**
     * Property key prefix for getting range value of date attribute.
     */
    static protected final String ROADMAP_MILESTONE_DATE_END = "emxProduct.Roadmap.Milestone.Date.End";

    /**
     * Property key prefix for getting milestone details.
     */
    static protected final String ROADMAP_MILESTONE_DETAILS = "emxProduct.Roadmap.Milestone.Details";

    /**
     * Property key prefix for getting milestone detail labels.
     */
    static protected final String ROADMAP_MILESTONE_DETAIL_LABELS = "emxProduct.Roadmap.Milestone.DetailLabels";

    /**
     * Property key prefix for getting milestone relationship.
     */
    static protected final String ROADMAP_MILESTONE_RELATIONSHIP = "emxProduct.Roadmap.Milestone.Relationship";

    /**
     * Select expression for Attribute Task Estimated Start Date.
     */
    static protected final String SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE = "attribute["
            + DomainObject.ATTRIBUTE_TASK_ESTIMATED_START_DATE + "]";

    /**
     * Select expression for Attribute Task Estimated Finish Date.
     */
    static protected final String SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE = "attribute["
            + DomainObject.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE + "]";

    /**
     * Select expression for Attribute Task Actual Start Date.
     */
    static protected final String SELECT_ATTRIBUTE_TASK_ACTUAL_START_DATE = "attribute["
            + DomainObject.ATTRIBUTE_TASK_ACTUAL_START_DATE + "]";

    /**
     * Select expression for Attribute Task Actual Finish Date.
     */
    static protected final String SELECT_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE = "attribute["
            + DomainObject.ATTRIBUTE_TASK_ACTUAL_FINISH_DATE + "]";

	// Begin of add by Enovia MatrixOne for Bug 303522 on 3rd May,05
	/**
     * Alias used for the string objectList.
     */
    protected static final String STR_OBJECTLIST = "objectList";

	/**
     * Alias used for select expression of attribute Baseline Current End Date.
     */
    protected static final String SELECT_BASELINE_CURRENT_END_DATE = "attribute["
            + DomainConstants.ATTRIBUTE_BASELINE_CURRENT_END_DATE + "]";

	/**
     * Alias used for the blank string.
     */
    protected static final String STR_BLANK = "";

	// End of add by Enovia MatrixOne for Bug 303522 on 3rd May,05

     /**
     * Property key prefix for getting label for Estimated Start Date
     */
    static protected final String ROADMAP_ESTIMATED_START_DATE = "emxFramework.Attribute.Task_Estimated_Start_Date";
     /**
     * Property key prefix for getting label for Estimated Finish Date
     */
    static protected final String ROADMAP_ESTIMATED_FINISH_DATE = "emxFramework.Attribute.Task_Estimated_Finish_Date";
     /**
     * Property key prefix for getting label for Actual Start  Date
     */
    static protected final String ROADMAP_ACTUAL_START_DATE = "emxFramework.Attribute.Task_Actual_Start_Date";
     /**
     * Property key prefix for getting label for Actual Finish Date
     */
    static protected final String ROADMAP_ACTUAL_FINISH_DATE = "emxFramework.Attribute.Task_Actual_Finish_Date";
    //End of add by Vibhu,Enovia MatrixOne for Bug 303031 on 5/2/2005

    static protected final String SELECT_PRIMARY_IMAGE = "from["
            + ProductLineConstants.RELATIONSHIP_PRIMARY_IMAGE
            + "].to." + DomainConstants.SELECT_ID;

    static protected Hashtable suiteRoadmapConfigs = new Hashtable();

    static protected StringList SELECTLIST_TYPE = new StringList(2);

    static protected Hashtable definedTypes = new Hashtable();

    static {
        SELECTLIST_TYPE.addElement(DomainObject.SELECT_ID);
        SELECTLIST_TYPE.addElement(DomainObject.SELECT_TYPE);
    }

    public emxPLCTimelineBase_mxJPO (Context context, String[] args) throws Exception {
        super(context,args);
    }

    /**
     * This method is used to get the substring of the property file key after
     * the last dot(.). This will give the symbolic name of the type for which
     * the property file entry has been added.
     *
     * @param strKey The property file key.
     * @return String containing the symbolic name of the type.
     * @since ProductCentral10.6
     */
    static protected String getLastReferenceKey(
                                                String strKey) {
        return strKey.substring(
                                strKey.lastIndexOf('.') + 1,
                                strKey.length());
    }

    /**
     * This method is used to set the select expression for the display name of
     * the particular object type in the configuration Map.
     *
     * @param mapConfig The roadmap configuration Map.
     * @param strObjectType The object type.
     * @param strDisplayName The display name.
     * @since ProductCentral10.6
     */
    static protected void setConfigDisplayName(
                                               Map mapConfig,
                                               String strObjectType,
                                               String strDisplayName) {
        //Get the configuration Map for the object type.
        Map mapTypeConfig = (Map) mapConfig.get(strObjectType);

        if (mapTypeConfig == null) {
            mapTypeConfig = new Hashtable();
            mapConfig.put(
                          strObjectType,
                          mapTypeConfig);
        }

        //Put the display name.
        mapTypeConfig.put(
                          UITimeline.SELECT_DISPLAY_NAME,
                          strDisplayName);
    }

    /**
     * This method is used to get the select expression for the display name of
     * the partcular object type from the roadmap configuration map.
     *
     * @param mapConfig The roadmap configuration map.
     * @param strObjectType The object type.
     * @return String containing the select expression for display name.
     * @since ProductCentral10.6
     */
    static protected String getConfigDisplayName(
                                                 Map mapConfig,
                                                 String strObjectType) {
        if (strObjectType == null) {
            return null;
        } else {
            Map mapTypeConfig = (Map) mapConfig.get(strObjectType);

            return (String) mapTypeConfig.get(UITimeline.SELECT_DISPLAY_NAME);
        }
    }

    /**
     * This method is used to add the select expressions for the object details
     * and detail labels to the type configuration map.
     *
     * @param mapConfig The roadmap configuration map.
     * @param strObjectType The object type.
     * @param lstSelectsList The detail select list.
     * @param lstDisplayNameList The display name select list.
     * @since ProductCentral10.6
     */
    static protected void addConfigDetails(
                                           Map mapConfig,
                                           String strObjectType,
                                           StringList lstSelectsList,
                                           StringList lstDisplayNameList) {
        //Get the configration map for this type.
        Map mapTypeConfig = (Map) mapConfig.get(strObjectType);

        if (mapTypeConfig == null) {
            mapTypeConfig = new Hashtable();
            mapConfig.put(
                          strObjectType,
                          mapTypeConfig);
        }

        //Get the details map if already set.
        Map mapDetails = (Map) mapTypeConfig.get(UITimeline.SELECT_DETAILS);
        if (mapDetails == null) {
            mapDetails = new Hashtable(2);
            mapTypeConfig.put(
                              UITimeline.SELECT_DETAILS,
                              mapDetails);
        }

        //Add the selects to the map.
        mapDetails.put(
                       SELECT_DETAIL_SELECTS,
                       lstSelectsList);
        mapDetails.put(
                       SELECT_DETAIL_DISPLAY_NAMES,
                       lstDisplayNameList);
    }

    /**
     * This method is used to get the list of details for particular object type
     * from the roadmap configuration map.
     *
     * @param mapConfig The roadmap configuration map.
     * @param strObjectType The object type.
     * @return The StringList containing the object detail selects.
     * @since ProductCentral10.6
     */
    static protected StringList getConfigDetailSelects(
                                                       Map mapConfig,
                                                       String strObjectType) {
        if (strObjectType == null) {
            return null;
        } else {
            Map mapTypeConfig = (Map) mapConfig.get(strObjectType);

            return (StringList) ((Map) mapTypeConfig
                    .get(UITimeline.SELECT_DETAILS)).get(SELECT_DETAIL_SELECTS);
        }
    }

    /**
     * This method is used to get the list of detail labels for particular
     * object type from the roadmap configuration map.
     *
     * @param mapConfig The roadmap configuration map.
     * @param strObjectType The object type.
     * @return The StringList containing the object detail label selects.
     * @since ProductCentral10.6
     */
    static protected StringList getConfigDetailDisplayNames(
                                                            Map mapConfig,
                                                            String strObjectType) {
        if (strObjectType == null) {
            return null;
        } else {
            Map mapTypeConfig = (Map) mapConfig.get(strObjectType);

            return (StringList) ((Map) mapTypeConfig
                    .get(UITimeline.SELECT_DETAILS))
                    .get(SELECT_DETAIL_DISPLAY_NAMES);
        }
    }

    /**
     * This method is used to get the default image url for the roadmap object
     * from the roadmap configuration map.
     *
     * @param mapRoadmapConfig The roadmap configuration map.
     * @return The string containig the url of the default roadmap image.
     * @since ProductCentral10.6
     */
    static protected String getDefaultImageURL(
                                               Map mapRoadmapConfig) {
        return (String) mapRoadmapConfig.get(ROADMAP_OBJECT_IMAGE_DEFAULT);
    }

    /**
     * This method is used to read the roadmap configuation settings defined in
     * the aplication property file and put it into Map.
     *
     * @param context The ematrix context object.
     * @param strSuiteKey The application suite key.
     * @return The roadmap configuration map for passed suitekey.
     * @throws Exception
     * @since ProductCentral10.6
     */
    static protected Map loadSuiteRoadmapMap(
                                             Context context) throws Exception {
        PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle
                .getBundle("emxProductLine");

        Hashtable objectConfig = new Hashtable();
        Hashtable milestoneConfig = new Hashtable();
        Hashtable milestoneIconMap = new Hashtable();
        Hashtable roadmapConfig = new Hashtable(6);

        //Add the configuration settings defined for roadmap object types.
        roadmapConfig.put(
                          SELECT_OBJECT_CONFIG,
                          objectConfig);

        //Add the configuration settings defined for milestone object types.
        roadmapConfig.put(
                          SELECT_MILESTONE_CONFIG,
                          milestoneConfig);

        //Add the configuration settings defined miletone icons.
        roadmapConfig.put(
                          SELECT_MILESTONE_ICON_MAP,
                          milestoneIconMap);

        //Add the settings for the default image url.
        roadmapConfig.put(
                          ROADMAP_OBJECT_IMAGE_DEFAULT,
                          EnoviaResourceBundle.getProperty(context,ROADMAP_OBJECT_IMAGE_DEFAULT));

        //Add the configuration settings for roadmap relationship.
        String strPropertyValue = PropertyUtil.getSchemaProperty(context,EnoviaResourceBundle.getProperty(context,ROADMAP_MILESTONE_RELATIONSHIP));
        if (strPropertyValue == null || strPropertyValue.length() == 0)
            strPropertyValue = "relationship_Roadmap";
        roadmapConfig.put(
                          SELECT_MILESTONE_RELATIONSHIP,
                          strPropertyValue);

        //Add the configuration settings for Milestone Type attribute
        strPropertyValue = EnoviaResourceBundle.getProperty(context,ROADMAP_MILESTONE_TYPE);
        if (strPropertyValue != null
                && strPropertyValue.indexOf("attribute_") != -1) {
            strPropertyValue = PropertyUtil.getSchemaProperty(context,strPropertyValue);
        }
        if (strPropertyValue == null)
            strPropertyValue = PropertyUtil
                    .getSchemaProperty(context,"attribute_MilestoneType");
        roadmapConfig.put(
                          ROADMAP_MILESTONE_TYPE,
                          strPropertyValue);

        //Add the configuration setting for the date attribute
        strPropertyValue = EnoviaResourceBundle.getProperty(context,ROADMAP_MILESTONE_DATE);
        if (strPropertyValue != null
                && strPropertyValue.indexOf("attribute_") != -1) {
            strPropertyValue = PropertyUtil.getSchemaProperty(context,strPropertyValue);
        }
        if (strPropertyValue == null)
            strPropertyValue = PropertyUtil.getSchemaProperty(context,"attribute_Date");
        roadmapConfig.put(
                          ROADMAP_MILESTONE_DATE,
                          strPropertyValue);

        //Add the configuration setting for the range values of date attribute
        strPropertyValue = EnoviaResourceBundle.getProperty(context,ROADMAP_MILESTONE_DATE_START);
        if (strPropertyValue == null)
            strPropertyValue = "Start";
        roadmapConfig.put(
                          ROADMAP_MILESTONE_DATE_START,
                          strPropertyValue);
        strPropertyValue = EnoviaResourceBundle.getProperty(context,ROADMAP_MILESTONE_DATE_END);
        if (strPropertyValue == null)
            strPropertyValue = "End";
        roadmapConfig.put(
                          ROADMAP_MILESTONE_DATE_END,
                          strPropertyValue);

        Enumeration enumKeys = bundle.getKeys();
        String strKey = null;
        String strValue = null;
        Map mapTypeConfig = null;
        Map mapTemp = null;

        while (enumKeys.hasMoreElements()) {
            strKey = (String) enumKeys.nextElement();

            //Add the configuration settings for the roadmap object display
            // name
            if (strKey.indexOf(ROADMAP_OBJECT_TITLE) != -1) {
                String strSymName = getLastReferenceKey(strKey);
                String strRealName = PropertyUtil.getSchemaProperty(context,strSymName);

                strValue = EnoviaResourceBundle.getProperty(context,strKey);
                if (strValue != null && strValue.indexOf("$<") >= 0)
                    strValue = MessageUtil.substituteValues(
                                                            context,
                                                            strValue);
                setConfigDisplayName(
                                     objectConfig,
                                     strRealName,
                                     strValue);
            }
            //Add the configuration settings for the roadmap object details
            else if (strKey.indexOf(ROADMAP_OBJECT_DETAILS) != -1) {
                String strSymName = getLastReferenceKey(strKey);
                String strRealName = PropertyUtil.getSchemaProperty(context,strSymName);
                StringList lstDetailsList = FrameworkUtil
                        .split(
                        		EnoviaResourceBundle.getProperty(context,strKey),
                               ",");

                for (int i = 0; i < lstDetailsList.size(); i++) {
                    strValue = (String) lstDetailsList.elementAt(i);

                    if (strValue.indexOf("$<") >= 0) {
                        strValue = MessageUtil.substituteValues(
                                                                context,
                                                                strValue);
                        lstDetailsList.set(
                                           i,
                                           strValue);
                    }
                }

                StringList lstDisplayDetailsList = FrameworkUtil
                        .split(
                        		EnoviaResourceBundle.getProperty(context,ROADMAP_OBJECT_DETAIL_LABELS
                                       + '.' + strSymName),
                               ",");
                if (lstDetailsList.size() != lstDisplayDetailsList.size()){
                    String strLanguage = context.getSession().getLanguage();
                    String strErrorMessage = EnoviaResourceBundle.getProperty(context,"ProductLine","emxProduct.Error.NoDisplayLabels",strLanguage);
                    throw new Exception(strErrorMessage);
                }
                addConfigDetails(
                                 objectConfig,
                                 strRealName,
                                 lstDetailsList,
                                 lstDisplayDetailsList);
            }
            //Add the configuration settings for the milestone object details
            else if (strKey.indexOf(ROADMAP_MILESTONE_DETAILS) != -1) {
                String strSymName = getLastReferenceKey(strKey);
                String strRealName = PropertyUtil.getSchemaProperty(context,strSymName);
                StringList lstDetailsList = FrameworkUtil
                        .split(
                        		EnoviaResourceBundle.getProperty(context,strKey),
                               ",");

                for (int i = 0; i < lstDetailsList.size(); i++) {
                    strValue = (String) lstDetailsList.elementAt(i);

                    if (strValue != null && strValue.indexOf("$<") >= 0) {
                        strValue = MessageUtil.substituteValues(
                                                                context,
                                                                strValue);
                        lstDetailsList.set(
                                           i,
                                           strValue);
                    }
                }

                StringList lstDisplayDetailsList = FrameworkUtil
                        .split(
                        		EnoviaResourceBundle.getProperty(context,ROADMAP_MILESTONE_DETAIL_LABELS
                                       + '.' + strSymName),
                               ",");
                if (lstDetailsList.size() != lstDisplayDetailsList.size()){
                    String strLanguage = context.getSession().getLanguage();
                    String strErrorMessage = EnoviaResourceBundle.getProperty(context,"ProductLine","emxProduct.Error.NoDisplayLabels",strLanguage);
                    throw new Exception(strErrorMessage);
                }
                addConfigDetails(
                                 milestoneConfig,
                                 strRealName,
                                 lstDetailsList,
                                 lstDisplayDetailsList);
            }
            //Add the configuration settings for the milestone icons
            else if (strKey.indexOf(ROADMAP_MILESTONE_ICON) != -1) {
            //Bug fix 335672 start
                milestoneIconMap.put(
                                     getLastReferenceKey(strKey),
                                     EnoviaResourceBundle.getProperty(context,strKey));
            //Bug fix 335672 end
            }
        }
        return roadmapConfig;
    }

    /**
     * This method is used to get the roadmap configuation settings.
     *
     * @param context The ematrix context of the request.
     * @param strSuiteKey The suite key.
     * @return Map.
     * @throws Exception
     * @since ProductCentral10.6
     */
    static protected Map getSuiteMap(
                                     Context context,
                                     String strSuiteKey) throws Exception {
        Map mapSuiteConfig = (Map) suiteRoadmapConfigs.get(strSuiteKey);
        if (mapSuiteConfig == null) {
            mapSuiteConfig = loadSuiteRoadmapMap(context);
            suiteRoadmapConfigs.put(
                                    strSuiteKey,
                                    mapSuiteConfig);
        }

        return mapSuiteConfig;
    }

    /**
     * This method is used to get the defined type in configuration settingfor a
     * particular type.
     *
     * @param context The ematrix context of the request.
     * @param definedTypes The defined types set.
     * @param strTypeName The passed type name.
     * @return String containing the defined type.
     * @throws Exception
     * @since ProductCentral10.6
     */
    static protected String getDefinedType(
                                           Context context,
                                           Set definedTypes,
                                           String strTypeName) throws Exception {
        //If the defined types's set contains the passed type name then return
        //the same type name
        if (definedTypes.contains(strTypeName))
            return strTypeName;
        if(mxType.isOfParentType(context, strTypeName,ProductLineConstants.TYPE_TASK_MANAGEMENT)){
                    return "Task";
        }

        //Otherwise return the Parent of the passed type that is present in the
        //defined types set.
        String strDefinedType = null;
        BusinessType busType = null;

        while (strDefinedType == null) {

            busType = new BusinessType(strTypeName, new Vault(""));
            busType.open(context);
            strTypeName = busType.getParent(context);
            busType.close(context);

            if (strTypeName == null || strTypeName.length() == 0)
                break;

            if (definedTypes.contains(strTypeName)) {
                strDefinedType = strTypeName;
            }
        }

        return strDefinedType;
    }

    /**
     * This method is used to get the information about the roadmap object.
     *
     * @param context The ematrix context object.
     * @param mapObjectConfig The configuration map.
     * @param strObjectId The object id.
     * @param strDefinedType The definbed type.
     * @param strDisplayName The display name.
     * @return The Map containing the roadmap object information.
     * @throws Exception
     * @since ProductCentral10.6
     */
    static protected Map getRoadmapObjectInfo(
                                              Context context,
                                              Map mapObjectConfig,
                                              String strObjectId,
                                              String strDefinedType,
                                              String strDisplayName, double ClientOffset, Locale localeObj)
            throws Exception {
        HashMap mapReturnMap = new HashMap();

        //Get the object selects for this object from the configuration map
        StringList lstObjectSelects = getConfigDetailSelects(
                                                             mapObjectConfig,
                                                             strDefinedType);

        //Get the display labels for this object from the configuration map
        StringList lstObjectDisplayNames = getConfigDetailDisplayNames(
                                                                       mapObjectConfig,
                                                                       strDefinedType);
        String strDisplaySelect = null;
        MapList lstDetails = null;
        String lstSortDate = null;
        String strDetailName = null;
        String strDetailValue = null;

        mapReturnMap.put(
                         DomainObject.SELECT_ID,
                         strObjectId);

        if (strDisplayName == null)
            strDisplaySelect = getConfigDisplayName(
                                                    mapObjectConfig,
                                                    strDefinedType);

        //Add the select expressions for Estimated and Actual Start and End
        // date
        //& Primary Image of the object.
        if (lstObjectSelects == null)
            lstObjectSelects = new StringList(0);
        lstObjectSelects.addElement(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE);
        lstObjectSelects.addElement(SELECT_ATTRIBUTE_TASK_ACTUAL_START_DATE);
        lstObjectSelects
                .addElement(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
        lstObjectSelects.addElement(SELECT_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE);
        lstObjectSelects.addElement(DomainConstants.SELECT_NAME);
        lstObjectSelects.addElement(SELECT_PRIMARY_IMAGE);
        /*Begin of Add:Raman,Enovia MatrixOne for Bug#302847 on 4/21/2005*/
        lstObjectSelects.addElement(DomainConstants.SELECT_REVISION);
        /*End of Add:Raman,Enovia MatrixOne for Bug#302847 on 4/21/2005*/
        if (strDisplaySelect != null)
            lstObjectSelects.addElement(strDisplaySelect);

        //Retrieve the information for the object
        DomainObject dObj = new DomainObject(strObjectId);
        Map objDetails = dObj.getInfo(
                                      context,
                                      lstObjectSelects);

        if (lstObjectDisplayNames.size() != 0) {
            lstDetails = new MapList(lstObjectDisplayNames.size());

            for (int selectIndex = 0; selectIndex < lstObjectDisplayNames
                    .size(); selectIndex++) {
                HashMap mapTemp = new HashMap(2);
                strDetailName = (String) lstObjectDisplayNames
                        .elementAt(selectIndex);

                if (strDetailName.equals("startDate")) {
                    strDetailValue = (String) objDetails.get(lstObjectSelects
                            .elementAt(lstObjectDisplayNames.size() + 1));
                    if (strDetailValue.length() == 0) {
                        strDetailValue = (String) objDetails
                                .get(lstObjectSelects
                                        .elementAt(lstObjectDisplayNames.size()));
                        //Added by Vibhu,Enovia MatrixOne for bug 303031 on 2nd May,05
                        strDetailValue = getDisplayDate(context,strDetailValue,ClientOffset,localeObj);
                        mapTemp.put(
                                    UITimeline.SELECT_VALUE,
                                    strDetailValue);
                        //Modified by Vibhu,Enovia MatrixOne for bug 303031 on 2nd May,05
                        mapTemp
                                .put(
                                     UITimeline.SELECT_DISPLAY_NAME,
                                     ROADMAP_ESTIMATED_START_DATE);
                    } else {
                        //Added by Vibhu,Enovia MatrixOne for bug 303031 on 2nd May,05
                        strDetailValue = getDisplayDate(context,strDetailValue,ClientOffset,localeObj);
                        mapTemp.put(
                                    UITimeline.SELECT_VALUE,
                                    strDetailValue);
                        //Modified by Vibhu,Enovia MatrixOne for bug 303031 on 2nd May,05
                        mapTemp
                                .put(
                                     UITimeline.SELECT_DISPLAY_NAME,
                                     ROADMAP_ACTUAL_START_DATE);
                    }

                    lstDetails.add(mapTemp);
                } else if (strDetailName.equals("finishDate")) {
                    strDetailValue = (String) objDetails.get(lstObjectSelects
                            .elementAt(lstObjectDisplayNames.size() + 3));

                    if (strDetailValue.length() == 0) {
                        strDetailValue = (String) objDetails
                                .get(lstObjectSelects
                                        .elementAt(lstObjectDisplayNames.size() + 2));
                        //Added by Vibhu,Enovia MatrixOne for bug 303031 on 2nd May,05
                        strDetailValue = getDisplayDate(context,strDetailValue,ClientOffset,localeObj);
                        mapTemp.put(
                                    UITimeline.SELECT_VALUE,
                                    strDetailValue);
                        //Modified by Vibhu,Enovia MatrixOne for bug 303031 on 2nd May,05
                        mapTemp
                                .put(
                                     UITimeline.SELECT_DISPLAY_NAME,
                                     ROADMAP_ESTIMATED_FINISH_DATE);
                    } else {
                        //Added by Vibhu,Enovia MatrixOne for bug 303031 on 2nd May,05
                        strDetailValue = getDisplayDate(context,strDetailValue,ClientOffset,localeObj);
                        mapTemp.put(
                                    UITimeline.SELECT_VALUE,
                                    strDetailValue);
                        //Modified by Vibhu,Enovia MatrixOne for bug 303031 on 2nd May,05
                        mapTemp
                                .put(
                                     UITimeline.SELECT_DISPLAY_NAME,
                                     ROADMAP_ACTUAL_FINISH_DATE);
                    }

                    lstDetails.add(mapTemp);
                } else {
                    mapTemp.put(
                                UITimeline.SELECT_DISPLAY_NAME,
                                lstObjectDisplayNames.elementAt(selectIndex));
                    mapTemp.put(
                                UITimeline.SELECT_VALUE,
                                objDetails.get(lstObjectSelects
                                        .elementAt(selectIndex)));
                    lstDetails.add(mapTemp);
                }
            }
        }

        mapReturnMap.put(
                         UITimeline.SELECT_DETAILS,
                         lstDetails);

        mapReturnMap
                .put(
                     UITimeline.SELECT_ESTIMATED_START_DATE,
                     objDetails.get(SELECT_ATTRIBUTE_TASK_ESTIMATED_START_DATE));
        mapReturnMap.put(
                         UITimeline.SELECT_ACTUAL_START_DATE,
                         objDetails
                                 .get(SELECT_ATTRIBUTE_TASK_ACTUAL_START_DATE));
        mapReturnMap
                .put(
                     UITimeline.SELECT_ESTIMATED_FINISH_DATE,
                     objDetails
                             .get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE));

        mapReturnMap
                .put(
                     UITimeline.SELECT_ACTUAL_FINISH_DATE,
                     objDetails.get(SELECT_ATTRIBUTE_TASK_ACTUAL_FINISH_DATE));
        /*Begin of Modify:Raman,Enovia MatrixOne for Bug#302970 on 4/21/2005*/
        StringBuffer sbNameRev = new StringBuffer((String)objDetails.get(DomainConstants.SELECT_NAME));
        sbNameRev.append(" ");
        sbNameRev.append((String)objDetails.get(DomainConstants.SELECT_REVISION));
        mapReturnMap.put(
                         DomainConstants.SELECT_NAME,
                         sbNameRev.toString());

        mapReturnMap.put(
                         UITimeline.SELECT_IMAGE_OBJECT,
                         objDetails.get(SELECT_PRIMARY_IMAGE));

        if (strDisplaySelect != null)
            strDisplayName = (String) objDetails
                    .get(strDisplaySelect);
        /*End of Modify:Raman,Enovia MatrixOne for Bug#302847 on 4/21/2005*/
        mapReturnMap.put(
                         UITimeline.SELECT_DISPLAY_NAME,
                         strDisplayName);

        return mapReturnMap;
    }

    /**
     * This method is used to get the current Milestone for roadmap object.
     *
     * @param lstMilestones The milestone list.
     * @return The string containing the current milestone name.
     * @since ProductCentral10.6
     */
    public String getCurrentMilestone(
                                      MapList lstMilestones) {
        String strMilestoneName = null;

        if (lstMilestones.size() != 0) {
            Map mapMilestone = (Map) lstMilestones.get(0);
            strMilestoneName = (String) mapMilestone
                    .get(UITimeline.SELECT_DISPLAY_NAME);
            String strValue = null;

            for (int i = 1; i < lstMilestones.size(); i++) {
                mapMilestone = (Map) lstMilestones.get(i);
                strValue = (String) mapMilestone
                        .get(UITimeline.SELECT_ACTUAL_START_DATE);

                if (strValue.length() == 0)
                    break;
                else
                    strMilestoneName = (String) mapMilestone
                            .get(UITimeline.SELECT_DISPLAY_NAME);
            }
        }
        return strMilestoneName;
    }

    /**
     * This method is used to add the filter data for the milestones.
     *
     * @param context The ematrix context object.
     * @param mapObjectData The object data map.
     * @param strRelName The relatioship name.
     * @param lstRelAttribList The rel attributes list.
     * @param strParentId The id of the parent.
     * @throws Exception
     * @since ProductCentral10.6
     */
    protected void addFilterData(
                                 Context context,
                                 Map mapObjectData,
                                 String strRelName,
                                 StringList lstRelAttribList,
                                 String strParentId) throws Exception {
        StringList lstSelects = new StringList(lstRelAttribList.size());

        String strAttName = null;
        for (int i = 0; i < lstRelAttribList.size(); i++) {
            strAttName = PropertyUtil
                    .getSchemaProperty(context,(String) lstRelAttribList.elementAt(i));
          //Modified By:Raman,Enovia MatrixOne for Bug#303232 on 5/26/2005
            lstSelects.addElement("attribute["
                    + strAttName + "].value");
        }
        DomainObject dObj = new DomainObject((String) mapObjectData
                .get(DomainObject.SELECT_ID));

        //Begin of Add By:Raman,Enovia MatrixOne for Bug#303232 on 5/26/2005
        StringBuffer sbObjWhereExp = new StringBuffer(DomainConstants.SELECT_ID);
        sbObjWhereExp.append("==");
        sbObjWhereExp.append(strParentId);

        List lstTmp = (MapList) dObj
                        .getRelatedObjects(
                                           context,
                                           strRelName,
                                           "*",
                                           new StringList(DomainConstants.SELECT_ID),
                                           lstSelects,
                                           true,
                                           false,
                                           (short) 1,
                                           sbObjWhereExp.toString(),
                                           null);

        Map mapTmp = (Map)lstTmp.get(0);
       //End Add By:Raman,Enovia MatrixOne for Bug#303232 on 5/26/2005
        HashMap mapNewData = new HashMap(mapTmp.size());
        for (int i = 0; i < lstRelAttribList.size(); i++) {
            strAttName = PropertyUtil
                    .getSchemaProperty(context,(String) lstRelAttribList.elementAt(i));

            mapNewData.put(
                           strAttName,
                           mapTmp.get("attribute["
                                   + strAttName + "].value"));
        }
        mapObjectData.put(
                          UITimeline.SELECT_FILTER_DATA,
                          mapNewData);
    }

    /**
     * This method is used to get the roadmap stucture for the selected roadmap
     * objects in the list page.
     *
     * @param context The ematrix context object.
     * @param args The packed arguments containing following arguments:
     *        objectIds: The selected roadmap object ids.
     * @return The roadmap structure.
     * @throws Exception
     * @since ProductCentral10.6
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getRoadmap(
                              Context context,
                              String[] args) throws Exception {
        try {
            MapList roadmap = new MapList();
            Map paramMap = (Map) JPO.unpackArgs(args);

            String[] objectIds = (String[]) paramMap.get("objectIds");
            String strSuiteKey = (String) paramMap.get("suiteKey");
            String strAttributeFilterList = (String) paramMap
                    .get("attributeFilterList");
            //Added by Vibhu,Enovia MatrixOne for Bug 303031 on 2nd may,05
            double ClientOffset = new Double((String)paramMap.get("ClientOffset")).doubleValue();
            //Added by Vibhu,Enovia MatrixOne for Bug 304312 on 5/18/2005
            Locale localeObj = (Locale) paramMap.get("localeObj");

            StringList attributeFilterList = null;
            if (strAttributeFilterList != null)
                attributeFilterList = FrameworkUtil
                        .split(
                               strAttributeFilterList,
                               UITimeline.FILTER_ATTRIBUTE_DELIMITER);
            Map roadmapConfig = getSuiteMap(
                                            context,
                                            strSuiteKey);
            MapList objectTypeMapList = DomainObject.getInfo(
                                                             context,
                                                             objectIds,
                                                             SELECTLIST_TYPE);
            Map objectConfig = (Map) roadmapConfig.get(SELECT_OBJECT_CONFIG);
            Map milestoneConfig = (Map) roadmapConfig
                    .get(SELECT_MILESTONE_CONFIG);
            Map milestoneIconMap = (Map) roadmapConfig
                    .get(SELECT_MILESTONE_ICON_MAP);
            String milestoneRelationship = (String) roadmapConfig
                    .get(SELECT_MILESTONE_RELATIONSHIP);
            String milestoneTypeSelect = "attribute["
                    + (String) roadmapConfig.get(ROADMAP_MILESTONE_TYPE) + "]";
            String milestoneDateSelect = "attribute["
                    + (String) roadmapConfig.get(ROADMAP_MILESTONE_DATE) + "]";
            Set definedObjectTypes = objectConfig.keySet();
            Set definedMilestoneTypes = milestoneConfig.keySet();
            Map objectTypeMap = null;
            String strObjectType = null;
            String definedType = null;
            DomainObject dObj = null;
            MapList milestoneList = null;
            Map milestoneTypeMap = null;
            String milestoneDisplayType = null;
            String milestoneDisName = null;
            String milestoneDate = "";
            StringList milestoneRelSelect = new StringList(2);
            milestoneRelSelect.addElement(milestoneTypeSelect);
            milestoneRelSelect.addElement(milestoneDateSelect);

            for (int objectIndex = 0; objectIndex < objectTypeMapList.size(); objectIndex++) {
                objectTypeMap = (Map) objectTypeMapList.get(objectIndex);
                strObjectType = (String) objectTypeMap
                        .get(DomainObject.SELECT_TYPE);
                definedType = getDefinedType(
                                             context,
                                             definedObjectTypes,
                                             strObjectType);

                objectTypeMap = getRoadmapObjectInfo(
                                                     context,
                                                     objectConfig,
                                                     (String) objectTypeMap
                                                             .get(DomainObject.SELECT_ID),
                                                     definedType,
                                                     null,ClientOffset,localeObj);

                dObj = new DomainObject((String) objectTypeMap
                        .get(DomainObject.SELECT_ID));

                objectTypeMap.put(
                                  UITimeline.SELECT_IMAGE_URL,
                                  getDefaultImageURL(roadmapConfig));

                MapList relatedObjects = dObj
                        .getRelatedObjects(
                                           context,
                                           milestoneRelationship,
                                           "*",
                                           SELECTLIST_TYPE,
                                           milestoneRelSelect,
                                           false,
                                           true,
                                           (short) 1,
                                           null,
                                           null);

                if (relatedObjects.size() == 0)
                    milestoneList = null;
                else
                    milestoneList = new MapList();

                for (int milestoneIndex = 0; milestoneIndex < relatedObjects
                        .size(); milestoneIndex++) {
                    milestoneTypeMap = (Map) relatedObjects.get(milestoneIndex);
                    strObjectType = (String) milestoneTypeMap
                            .get(DomainObject.SELECT_TYPE);
                    definedType = getDefinedType(
                                                 context,
                                                 definedMilestoneTypes,
                                                 strObjectType);
                    milestoneDisplayType = (String) milestoneTypeMap
                            .get(milestoneTypeSelect);
                    milestoneDate = (String) milestoneTypeMap
                            .get(milestoneDateSelect);

                    milestoneDisName = i18nNow.getRangeI18NString(
                                       (String)roadmapConfig.get(ROADMAP_MILESTONE_TYPE),
                                       milestoneDisplayType,
                                       (String)context.getSession().getLanguage());

                    milestoneTypeMap = getRoadmapObjectInfo(
                                                            context,
                                                            milestoneConfig,
                                                            (String) milestoneTypeMap
                                                                    .get(DomainObject.SELECT_ID),
                                                            definedType,
                                                            milestoneDisName,ClientOffset,localeObj);
                    milestoneTypeMap.put(
                                         UITimeline.SELECT_IMAGE_URL,
                                         milestoneIconMap
                                                 .get(milestoneDisplayType
                                                         .replace(
                                                                  ' ',
                                                                  '_')));
                    milestoneTypeMap.put(
                                         ROADMAP_MILESTONE_TYPE,
                                         roadmapConfig
                                                 .get(ROADMAP_MILESTONE_TYPE));

                    if (attributeFilterList != null)
                   //Modified By:Raman,Enovia MatrixOne for Bug#303232 on 5/26/2005
                        addFilterData(
                                      context,
                                      milestoneTypeMap,
                                      milestoneRelationship,
                                      attributeFilterList,
                                      (String) objectTypeMap.
                                                get(DomainObject.SELECT_ID));
                    /*Begin of Modify:Raman,Enovia MatrixOne for Bug#303027 on 4/21/2005*/
                    if (milestoneDate.equals((String) roadmapConfig
                            .get(ROADMAP_MILESTONE_DATE_START))) {
                         if(((String) milestoneTypeMap.get(UITimeline.SELECT_ACTUAL_START_DATE) != null) &&
                          (!"".equals((String) milestoneTypeMap.get(UITimeline.SELECT_ACTUAL_START_DATE))&&
                          (!"null".equalsIgnoreCase((String) milestoneTypeMap.get(UITimeline.SELECT_ACTUAL_START_DATE)))
                           ))
                        {
                          milestoneTypeMap
                                .put(
                                     UITimeline.SELECT_MILESTONE_DATE,
                                     (String) milestoneTypeMap
                                             .get(UITimeline.SELECT_ACTUAL_START_DATE));
                        }
                         else
                        {
                           milestoneTypeMap
                                .put(
                                     UITimeline.SELECT_MILESTONE_DATE,
                                     (String) milestoneTypeMap
                                             .get(UITimeline.SELECT_ESTIMATED_START_DATE));
                        }
                    } else if (milestoneDate.equals((String) roadmapConfig
                            .get(ROADMAP_MILESTONE_DATE_END))) {
                          if(((String) milestoneTypeMap.get(UITimeline.SELECT_ACTUAL_FINISH_DATE) != null) &&
                          (!"".equals((String) milestoneTypeMap.get(UITimeline.SELECT_ACTUAL_FINISH_DATE))&&
                          (!"null".equalsIgnoreCase((String) milestoneTypeMap.get(UITimeline.SELECT_ACTUAL_FINISH_DATE)))
                          ))
                        {
                          milestoneTypeMap
                                .put(
                                     UITimeline.SELECT_MILESTONE_DATE,
                                     (String) milestoneTypeMap
                                             .get(UITimeline.SELECT_ACTUAL_FINISH_DATE));
                        }
                         else
                        {
                           milestoneTypeMap
                                .put(
                                     UITimeline.SELECT_MILESTONE_DATE,
                                     (String) milestoneTypeMap
                                             .get(UITimeline.SELECT_ESTIMATED_FINISH_DATE));
                        }
                    }
               /*End of Modify:Raman,Enovia MatrixOne for Bug#303027 on 4/21/2005*/
                    milestoneList.add(milestoneTypeMap);
                }

                if (milestoneList != null) {
                    /*Modified by Enovia MatrixOne for Bug#303232 on 5/24/2005*/
                    milestoneList.sort(
                                       UITimeline.SELECT_MILESTONE_DATE,
                                       "ascending",
                                       "date");
                    objectTypeMap.put(
                                      UITimeline.SELECT_MILESTONES,
                                      milestoneList);
                    objectTypeMap.put(
                                      UITimeline.SELECT_CURRENT_MILESTONE,
                                      getCurrentMilestone(milestoneList));
                    roadmap.add(objectTypeMap);
                }
            }

            return roadmap;
        } catch (Exception ex) {
            System.err.println(ex);
            throw ex;
        }
    }
    //Begin of Add by Vibhu,Enovia MatrixOne for Bug 303031 on 2nd May,05
    /**
     * This method is used to return the Formatted date corrosponding to user
     * objects in the list page.
     *
     * @param context The ematrix context object.
     * @param String The Date string to be format.
     * @param double TimeZone offset value.
     *
     * @return Formatted String for the Date
     * @throws Exception
     * @since ProductCentral10.6
     */
    public static String getDisplayDate(Context context, String strNormalDate, double dblTZOffset, Locale localeObj) throws Exception
    {
        try
        {
            int iDateFrmt = eMatrixDateFormat.getEMatrixDisplayDateFormat();
            String strFormatDate="";
            //Modified by Enovia MatrixOne for Bug 304312 on 5/18/2005
            strFormatDate=eMatrixDateFormat.getFormattedDisplayDateTime(context, strNormalDate, false, iDateFrmt, dblTZOffset, localeObj);
            return strFormatDate;
        }
        catch(Exception ex)
        {
            //Return the normal date value coming to method if there is any exception in formatting the date.
            return strNormalDate;
        }
    }
    //End of Add by Vibhu,Enovia MatrixOne for Bug 303031 on 2nd May,05


	// Begin of add by Enovia MatrixOne for Bug 303522 on 3rd May,05
	/**
     * This column JPO method is invoked while displaying the Tasks list page
     * under products and is used to display the slip days icon.
	 * @param context The ematrix context of the request
     * @param args The packed arguments containing Map with following key value
     *            pairs: objectList - The maplist containing the related
     *            projects information
     * @return Vector containing the HTML tag to generate the Slip days icon.
     * @throws Exception
     * @throws FrameworkException
     * @since ProductCentral10.6
     */

	 public Vector getSlipDaysIcon(Context context, String[] args)
            throws Exception, FrameworkException {
        //Unpack the arguments
        Map mapProgram = (HashMap) JPO.unpackArgs(args);

        //Get the object id of the Product and related project list
        List lstProjectList = (MapList) mapProgram.get(STR_OBJECTLIST);

        //Get the current system date for the comparision
        Date dtTmpDate = new Date();
        Date dtSysDate = new Date(dtTmpDate.getYear(), dtTmpDate.getMonth(),
                dtTmpDate.getDate());
        SimpleDateFormat sdfDateFormat = new SimpleDateFormat(eMatrixDateFormat
                .getEMatrixDateFormat(), Locale.US);
        Date dtFinishDate = new Date();

        String strBlEndDate = STR_BLANK;
        String strEstEndDate = STR_BLANK;
        String strState = STR_BLANK;
        String strType = STR_BLANK;
        StringBuffer sbHtmlTag = null;
        Vector vtrSlipDays = new Vector();
        Map mapProject = new HashMap();
        long lDaysRemain = 0;

        //Get the yellow red threshold from the properties file
        int iThreshold = Integer
                .parseInt(EnoviaResourceBundle.getProperty(context,"emxProduct.Roadmap.SlipDaysThreshold"));
        // Get the actual name of the state complete
        String strStateComplete = FrameworkUtil
                .lookupStateName(
                        context, DomainConstants.POLICY_PROJECT_SPACE,
                        "state_Complete");

        // Get the tooltips to be displayed along with the slip days icon from
        // the property file
        String strLanguage = context.getSession().getLanguage();
        String strLegendOnTime = EnoviaResourceBundle.getProperty(context,"ProductLine",
                "emxProduct.ProjectStatus.OnTime",strLanguage);
        String strLegendLate =EnoviaResourceBundle.getProperty(context,"ProductLine",
                "emxProduct.ProjectStatus.Late",strLanguage);
        String strLegendBehind = EnoviaResourceBundle.getProperty(context,"ProductLine",
                "emxProduct.ProjectStatus.BehindSchedule",strLanguage);

        // Traverse through the project list and determine the slip days icon
        // for each project
        for (int i = 0; i < lstProjectList.size(); i++) {
            // Get the value of the attributes Estimated Finish Date & Baseline
            // Current Finish Date and current state and type of the project
            mapProject = (Map) lstProjectList.get(i);
            strBlEndDate = (String) mapProject
                    .get(SELECT_BASELINE_CURRENT_END_DATE);
            strEstEndDate = (String) mapProject
                    .get(SELECT_ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE);
            strState = (String) mapProject.get(DomainConstants.SELECT_CURRENT);

            if (strBlEndDate != null && !strBlEndDate.equals(STR_BLANK)
                    && !strBlEndDate.equalsIgnoreCase("null")) {
                dtFinishDate = sdfDateFormat.parse(strBlEndDate);
            } else if (strEstEndDate != null
                    && !strEstEndDate.equals(STR_BLANK)
                    && !strEstEndDate.equalsIgnoreCase("null")) {
                dtFinishDate = sdfDateFormat.parse(strEstEndDate);
            }

            // Get the difference between the BaseLine Current End date or
            // Estimated End Date and current system date
            if (dtSysDate.after(dtFinishDate)) {
                lDaysRemain = DateUtil.computeDuration(
                        dtFinishDate, dtSysDate);
            }


			sbHtmlTag = new StringBuffer();
			if (strState.equals(strStateComplete)) {
				sbHtmlTag
						.append("<img src=\"../common/images/iconStatusGreen.gif\" border=\"0\" alt=\"");
				sbHtmlTag.append(strLegendOnTime);
				sbHtmlTag.append("\">");
			} else if (dtSysDate.after(dtFinishDate)) {
				sbHtmlTag
						.append("<img src=\"../common/images/iconStatusRed.gif\" border=\"0\" alt=\"");
				sbHtmlTag.append(strLegendBehind);
				sbHtmlTag.append("\">");
			} else if (dtSysDate.after(dtFinishDate)
					&& lDaysRemain <= iThreshold) {
				sbHtmlTag
						.append("<img src=\"../common/images/iconStatusYellow.gif\" border=\"0\" alt=\"");
				sbHtmlTag.append(strLegendLate);
				sbHtmlTag.append("\">");
			} else {
				sbHtmlTag.append("&nbsp;");
			}
			vtrSlipDays.add(sbHtmlTag.toString());

        }
        return vtrSlipDays;
    }

	// End of add by Enovia MatrixOne for Bug 303522 on 3rd May,05

}
