/*
 ** ${CLASSNAME}
 **
 **Copyright (c) 1993-2016 Dassault Systemes.
 ** All Rights Reserved.
 **This program contains proprietary and trade secret information of
 **Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 **or intended publication of such program
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.dmcplanning.ManufacturingPlan;
import com.matrixone.apps.dmcplanning.ManufacturingPlanUtil;
import com.matrixone.apps.dmcplanning.ManufacturingPlanConstants;
import com.matrixone.apps.dmcplanning.Model;
import com.matrixone.apps.dmcplanning.Product;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.UITableGrid;
import com.matrixone.apps.productline.ProductLineCommon;

/**
 * This JPO class has some methods pertaining to Master Feature.
 * 
 * @author IVU
 * @since DMCPlanning R209
 */
public class CFPModelBase_mxJPO extends emxDomainObject_mxJPO {

	private static final String cellIdSelect = "to[" + ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES + "].tomid[" + 
			ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST + "].from.id";
	private static final String cellrelIdSelect ="to[" + ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES+"].tomid[" + 
			ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST + "].id";
	private static final String cellusageAttribute ="to[" + ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES + "].tomid[" + 
			ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST + "].attribute[" + ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE + "]";
	
	protected String languageStr="";

	public static final String RELATIONSHIP_DERIVED_ABSTRACT   = PropertyUtil.getSchemaProperty("relationship_DERIVED_ABSTRACT");
	protected String SUITE_KEY="DMCPlanning";

	/**
     * Default Constructor.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return noting,constructor
     * @throws Exception
     *             if the operation fails
     * @since DMCPlanning R209
     * @author IVU
     */
    public CFPModelBase_mxJPO(Context context, String[] args)
            throws Exception {
        super(context, args);
    }

    /**
     * Main entry point.
     * 
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args
     *            holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception
     *             if the operation fails
     * @author IVU
     * @since DMCPlanning R209
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (!context.isConnected())
            throw new Exception("Not supported on desktop client");
        return 0;
    }
    /**
	 * This method gets the range of values for the displayed
	 * Design effectivity Matrix  cell values
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds input arguments.
	 * @return a StringList containing the range option values to display
	 *         Design effectivity Matrix  cell values combo box.
	 * @throws Exception
	 *             if the operation fails
	 * 
	 */
    @com.matrixone.apps.framework.ui.CellRangeJPOCallable
    public HashMap getPFLUsageRange(Context context, String[] args)throws FrameworkException {

    	HashMap returnMap = new HashMap();
    	try
    	{
    		String languageStr = context.getSession().getLanguage();

    		StringList strChoicesDisp = new StringList(3);
    		String strFeatureUsageStandard = "DMCPlanning.Range.FeatureUsage.Standard";
    		String strFeatureUsageOptional = "DMCPlanning.Range.FeatureUsage.Optional";
    		String strFeatureUsageStandardDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,	strFeatureUsageStandard,languageStr);
    		String strFeatureUsageOptionalDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,	strFeatureUsageOptional,languageStr);
    		String strFeatureUsageStandardActual = ManufacturingPlanConstants.RANGE_VALUE_STANDARD;
    		String strFeatureUsageOptionActual = ManufacturingPlanConstants.RANGE_VALUE_OPTIONAL;
    		String strFeatureUsageOptionRequired = ManufacturingPlanConstants.RANGE_VALUE_REQUIRED;

    		strChoicesDisp.add(strFeatureUsageOptionalDisplay);
    		strChoicesDisp.add(strFeatureUsageStandardDisplay);
    		strChoicesDisp.add("");

    		// combobox actual values
    		StringList strChoices = new StringList(3);
    		strChoices.add(strFeatureUsageOptionActual);
    		strChoices.add(strFeatureUsageStandardActual);
    		strChoices.add("Del");

    		returnMap.put("field_choices", strChoices);
    		returnMap.put("field_display_choices", strChoicesDisp);

    	} catch (Exception e) {
    		// TODO: handle exception
    		e.printStackTrace();
    	}
    	return returnMap;
    }

  /* 
   * Builds the html for the cell value.
   * this method is used in getRowsForDesignEffectivityMatrix and getCellvalueJPO;
   */
    private void processCellIdrowMap(Context context, Map mapCellIDValues, String cellId, Map rowmap, String strUsage, String url, boolean isFrozen) 
	throws FrameworkException {
    boolean isCFPUser = ManufacturingPlanUtil.isCFPUser(context);
		try {
			String strName = XSSUtil.encodeForHTML(context, (String)rowmap.get("attribute[" + ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME + 
					"]")) + " " + XSSUtil.encodeForHTML(context,(String)rowmap.get(DomainConstants.SELECT_REVISION));

			if (mapCellIDValues.containsKey(cellId)) {
				if (!strUsage.equalsIgnoreCase("")) {
					String strcellval = ((String)mapCellIDValues.get(cellId)).replaceFirst(
							"</table>", "<tr><td>" + strName + "</td><td>(" + strUsage + ")</td></tr></table>");
					if (!isFrozen) {
						int indexofRowSpan = strcellval.indexOf("rowspan");
						String strrowSpanValue = strcellval.substring(indexofRowSpan+9,(strcellval.indexOf(">",indexofRowSpan)-2));
						mapCellIDValues.put(cellId, strcellval.replaceFirst("rowspan=\"" + strrowSpanValue + 
								"\"", "rowspan=\"" + (Integer.parseInt(strrowSpanValue)+1) + "\""));
					} else {
						mapCellIDValues.put(cellId,strcellval);
					}
				}
			} else {
				if (!strUsage.equalsIgnoreCase(""))	{
					if (isFrozen) {
						mapCellIDValues.put(cellId,"<table><tr><td>" + strName + "</td><td>(" + strUsage + ")</td></tr></table>");
					} else {
						if(isCFPUser){
						mapCellIDValues.put(cellId,"<table><tr><td>" + strName + "</td><td>(" + strUsage + 
								")</td><td  style=\"vertical-align:middle;text-align:center;\" rowspan=\"1\" >" +
								" <a href=\""+url+"\" >" + "<img src=\"../common/images/iconActionEdit.gif\" border=\"0\" alt=\"" + 
								EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Heading.EditDesignEffectivity",languageStr) + 
								"\"/></a></td></tr></table>");
						}else{
							mapCellIDValues.put(cellId,"<table><tr><td>" + strName + "</td><td>(" + strUsage + 
									")</td><td  style=\"vertical-align:middle;text-align:center;\" rowspan=\"1\" >" +
									"</td></tr></table>");
						}
					}
				} else {
					if(isFrozen){
						mapCellIDValues.put(cellId,"");
					} else {
						if(isCFPUser){
						mapCellIDValues.put(cellId,"<table><tr><td  style=\"vertical-align:middle;text-align:center;\" rowspan=\"1\" >" + 
								" <a href=\""+url+"\" >" + "<img src=\"../common/images/iconActionEdit.gif\" border=\"0\" alt=\"" + 
								EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Heading.EditDesignEffectivity",languageStr) + 
								"\"/></a></td></tr></table>");
						}else{
							mapCellIDValues.put(cellId,"<table><tr><td  style=\"vertical-align:middle;text-align:center;\" rowspan=\"1\" >" + 
									"</td></tr></table>");
						}
					}
				}
			}
		} catch (Exception e) {
	      throw new FrameworkException(e);
	    }
	}
  /**
	 * Update the Design effectivity Matrix  cell values.
	 * Design effectivity Matrix  cell values
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds input arguments.
	 *
	 * @throws Exception
	 *             if the operation fails
	 *
	 */
    @com.matrixone.apps.framework.ui.CellUpdateJPOCallable
    public String updateFeatureUsageforGrid(Context context, String[] args)
    throws Exception {
	    String returnStringOnUpdate="";
	    try {
	    	String strRelID="";
	    	String updatedI18Value="";

	    	HashMap programMap = (HashMap) JPO.unpackArgs(args);
	    	HashMap paramMap = (HashMap) programMap.get("paramMap");
	    	HashMap rMap = (HashMap) programMap.get("requestMap");
	    	String strNewValue = (String) paramMap.get("New Value");
	    	String strrelId = (String) programMap.get("relId");
	    	String strcolId = (String) programMap.get("colId");

	    	if (!"Del".equalsIgnoreCase(strNewValue)) {
	    		StringList slObjectList = new StringList(strNewValue);
	    		StringList attributeDisplayRange = i18nNow.getAttrRangeI18NStringList(ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE,
					  slObjectList, context.getSession().getLanguage());
			  updatedI18Value = (String)attributeDisplayRange.get(0);
	    	}
	    	
	    	if (strrelId!=null) {
	    		if ("Del".equalsIgnoreCase(strNewValue)) {
				  //when deleting PFL relationship
	    			Product product = new Product(strcolId);
	    			product.editDesignEffectivity(context, new StringList(), new StringList((String)paramMap.get("objectId")));
	    			strRelID="";
	    		} else {
				  //updating the PFL relationship
	    			DomainRelationship domainrelation = new DomainRelationship();
	    			domainrelation.setAttributeValue(context, strrelId, ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE, strNewValue);
	    			strRelID=strrelId;
	    		}
	    		
	    	} else if(!"Del".equalsIgnoreCase(strNewValue)) {
	    		//creating new PFL relationship
	    		Product product = new Product(strcolId);
	    		product.editDesignEffectivity(context,new StringList((String)paramMap.get("objectId")+"|"+strNewValue),new StringList());
	    		StringList list = new StringList(cellIdSelect);
	    		list.add(cellrelIdSelect);
	    		Map listmap = new DomainObject((String)paramMap.get("objectId")).getInfo(context,list);
	    		if (listmap.get(cellIdSelect)instanceof String){
	    			strRelID=(String)listmap.get(cellrelIdSelect);
	    		} else {
	    			StringList tmpList = (StringList)listmap.get(cellIdSelect);
	    			strRelID=(String)((StringList)listmap.get(cellrelIdSelect)).get(tmpList.indexOf(strcolId));
				}
	    	}
	    	returnStringOnUpdate=strRelID+"|"+updatedI18Value;
	    } catch (Exception e) {
	    	throw new FrameworkException(e.getMessage());
	    }
	    return returnStringOnUpdate;
    }
  
  /*
   * Builds the html for the cell value when Export to excel and printer friendly.
   * this method is used in getRowsForDesignEffectivityMatrix;
   */
  
    private void processCellIdrowMapForReport(Context context, Map mapCellIDValues, String cellId, Map rowmap, String strUsage, String ReportType)
    throws FrameworkException {
		try {
		    String strName = rowmap.get("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]") + " " + 
		    		rowmap.get(DomainConstants.SELECT_REVISION);
	
		    if (mapCellIDValues.containsKey(cellId)){
				String strcellval =  ((String)mapCellIDValues.get(cellId));
				if(ReportType.equalsIgnoreCase("CSV")){
					strcellval = strcellval + "\n" + strName + "(" + strUsage + ")";
				} else {
					String strNamePF = XSSUtil.encodeForHTML(context, (String)rowmap.get("attribute[" + ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME +
							"]")) + " " + XSSUtil.encodeForHTML(context,(String)rowmap.get(DomainConstants.SELECT_REVISION));
					strcellval = strcellval.replaceFirst("</p>", "<br />" + strNamePF + "(" + strUsage + ")</p>");
				}
				mapCellIDValues.put(cellId,strcellval);
			} else {
				if(ReportType.equalsIgnoreCase("CSV")){
					mapCellIDValues.put(cellId, strName + "(" + strUsage + ")");
				}else{
					String strNamePF = XSSUtil.encodeForHTML(context, (String)rowmap.get("attribute[" + ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME +
							"]")) + " "+ XSSUtil.encodeForHTML(context,(String)rowmap.get(DomainConstants.SELECT_REVISION));
					mapCellIDValues.put(cellId, "<p>" + strNamePF + "(" + strUsage + ")</p>");
				}
			}
		} catch (Exception e) {
			throw new FrameworkException(e);
		}
    }
    
    /**
	 * This method gets the range of values for the displayed
	 * Products  Usage feild.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds input arguments.
	 * @return a StringList containing the range option values to display
	 *         Product Feature List Usage combo box.
	 * @throws Exception
	 *             if the operation fails
	 * @since CFP R212_Da Branching
	 */

	public HashMap getMasterCompositionUsageRange(Context context, String[] args)throws FrameworkException {

		HashMap returnMap = new HashMap();
		try
		{
			String languageStr = context.getSession().getLanguage();

			StringList strChoicesDisp = new StringList(2);
			String strFeatureUsageStandard = "DMCPlanning.Range.FeatureUsage.Standard";
			String strFeatureUsageOptional = "DMCPlanning.Range.FeatureUsage.Optional";

			String strFeatureUsageStandardDisplay =EnoviaResourceBundle.getProperty(context,SUITE_KEY,strFeatureUsageStandard,languageStr);
			String strFeatureUsageOptionalDisplay =EnoviaResourceBundle.getProperty(context,SUITE_KEY,strFeatureUsageOptional,languageStr);

			strChoicesDisp.add(strFeatureUsageStandardDisplay);
			strChoicesDisp.add(strFeatureUsageOptionalDisplay);

			// combobox actual values
			StringList strChoices = new StringList(2);
			strChoices.add(ManufacturingPlanConstants.RANGE_VALUE_STANDARD);
			strChoices.add(ManufacturingPlanConstants.RANGE_VALUE_OPTIONAL);

			returnMap.put("field_choices", strChoices);
			returnMap.put("field_display_choices", strChoicesDisp);

		}
		catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
		return returnMap;
	}

	/**
	 * used Mastercompsition  Usage feild.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds input arguments.
	 * @return a StringList containing the range option values to display
	 *         Product Feature List Usage combo box.
	 * @throws Exception
	 *             if the operation fails
	 */
    
	public void updateMasterCompositionUsageRange(Context context, String[] args) throws FrameworkException {

		HashMap returnMap = new HashMap();
		try {
		    HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String strNewValue = (String) paramMap.get("New Value");
			String strobjId = (String) paramMap.get("objectId");
			String strRelId = (String) paramMap.get("relId");

			if (strobjId != null) {
			    if (strNewValue != null && !"".equalsIgnoreCase(strNewValue)){
					 DomainRelationship domainrelation = new DomainRelationship();
					 StringList selectList = new StringList("to["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].id");
					 selectList.add("to["+ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT+"].id");
					 Map relMap = new DomainObject(strobjId).getInfo(context, selectList);

					 // Updating the PFL relationship
					 if (relMap.containsKey("to[" + ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS + "].id")) {
						 domainrelation.setAttributeValue(context, (String)relMap.get("to[" + ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS + 
								 "].id"), ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE, strNewValue);
					 } else {
						 domainrelation.setAttributeValue(context, (String)relMap.get("to[" + ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT + 
								 "].id"), ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE, strNewValue);
					 }
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	/**
	 * get Mastercompsition  Usage .
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds input arguments.
	 * @return a StringList containing the range option values to display
	 *         Product Feature List Usage combo box.
	 * @throws Exception
	 *             if the operation fails
	 * @since CFP R212_Da Branching
	 */
	
	public static StringList getMasterCompositionUsage(Context context, String[] args)throws FrameworkException {
		StringList usageList = new StringList();
		try {

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList lstObjectIdsList = (MapList) programMap.get("objectList");
			String strUsageSelect = "to["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].attribute["+
					ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]";
			String strUsageSelect2 = "to["+ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT+"].attribute["+
					ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]";
			StringList  slObjSelects = new StringList(DomainConstants.SELECT_ID);
			slObjSelects.add("to["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].attribute["+
					ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]");
			String[] objectArray = new String[lstObjectIdsList.size()];
			
			for (int i = 0; i < lstObjectIdsList.size(); i++) {
				Map tempMap = (Map) lstObjectIdsList.get(i);
				if(tempMap.containsKey(DomainConstants.SELECT_ID)){
					objectArray[i] = (String)tempMap.get(DomainConstants.SELECT_ID);
				}
			}
			MapList returnList = DomainObject.getInfo(context, objectArray, slObjSelects);

			String strI18Value ="";
			String strLanguage = context.getSession().getLanguage();
			for (int i = 0; i < returnList.size(); i++) {
				Map tempMap = (Map) returnList.get(i);
				if (tempMap.containsKey(strUsageSelect)) {
					String strAttrValue=(String)tempMap.get(strUsageSelect);
					strI18Value = (EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Feature_Allocation_Type."+strAttrValue,strLanguage)).trim();
					usageList.add(strI18Value);
				} else if (tempMap.containsKey(strUsageSelect2)) {
					String strAttrValue=(String)tempMap.get(strUsageSelect2);
					strI18Value = (EnoviaResourceBundle.getProperty(context,"Framework","emxFramework.Range.Feature_Allocation_Type."+strAttrValue,strLanguage)).trim();
					usageList.add(strI18Value);
				} else {
					usageList.add("");
				}


			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return usageList;

	}


	/*returns a map with ShortForm for Usage. reads the shortform from the stringresource files and build the map*/
	private HashMap getUsageRangeShortForm(Context context) throws FrameworkException {
		HashMap returnMap = new HashMap();
		try	{
			String languageStr = context.getSession().getLanguage();
			String strFeatureUsageStandardDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Range.FeatureUsage.Standard.shortForm",languageStr);
			String strFeatureUsageOptionalDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Range.FeatureUsage.Optional.shortForm",languageStr);
			String strFeatureUsageRequiredDisplay = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Range.FeatureUsage.Mandatory.shortForm",languageStr);
			
			returnMap.put(ManufacturingPlanConstants.RANGE_VALUE_STANDARD, strFeatureUsageStandardDisplay);
			returnMap.put(ManufacturingPlanConstants.RANGE_VALUE_OPTIONAL, strFeatureUsageOptionalDisplay);
			returnMap.put(ManufacturingPlanConstants.RANGE_VALUE_REQUIRED, strFeatureUsageRequiredDisplay);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return returnMap;
	}


	/**
	 *  gets rows for the Design effevtivity grid  and pass it on to GridComponent
	 *  Design effectivity Matrix
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds input arguments.
	 * @return a MapList containing the Maps for rows values
	 *
	 * @throws Exception
	 *             if the operation fails
	 */
	@com.matrixone.apps.framework.ui.RowJPOCallable
	public MapList getRowsForDesignEffectivityMatrix(Context context, String[] args)throws FrameworkException {
		MapList rowList = new MapList();

		try {
			HashMap usageshortMap = getUsageRangeShortForm(context);
			Map programMap = (Map) JPO.unpackArgs(args);
			String strObjectID = (String)programMap.get("objectId");
			Map rMap = (Map)programMap.get("requestMap");
			String strParentID = (String)rMap.get("parentOID");
			String selectIds = (String)rMap.get("selectId");

			String strReportFormat = (String) programMap.get("reportFormat");
      
			// get the Master features structure connect to the strObjectID
			rowList = getModelMasterCompositionStructure(context,strObjectID,strParentID);
	  
			// Processing MapList to set the row and Cell values as required
			MapList returnRowList = new MapList();
			Map tempMap = null;
			Map mapCellIDValues = new HashMap();
			String strModelId = "";
			boolean isModelRemoved = false;
		
			for (int i = 0; i < rowList.size(); i++) {
				tempMap = (Map)rowList.get(i);
				
				// If level= 1, show them as row and corresponding children will be displayed as cell values
				String strLevel = (String)tempMap.get(DomainConstants.SELECT_LEVEL);
				int iLevelTemp = Integer.parseInt(strLevel);
				String strType = (String)tempMap.get(DomainConstants.SELECT_TYPE);
				String strObjectId = (String)tempMap.get(DomainConstants.SELECT_ID);

				if (iLevelTemp == 1 
						&& ManufacturingPlan.getDerivedMap(context, ManufacturingPlanConstants.TYPE_MODEL).contains(strType)) {
					strModelId = (String)tempMap.get(DomainConstants.SELECT_ID);
 				    if (mapCellIDValues.size() > 0 && returnRowList.size() > 0) {
 				    	Map updateMap = (Map)returnRowList.get((returnRowList.size()-1));
 				    	updateMap.put(UITableGrid.KEY_CELL_ID,new ArrayList(mapCellIDValues.keySet()));//cell id and column id should match
 				    	updateMap.put(UITableGrid.KEY_CELL_VALUE,new ArrayList(mapCellIDValues.values()));
 				    }
 				    tempMap.put(UITableGrid.KEY_ROW_ID, tempMap.get(DomainConstants.SELECT_ID));
 				    returnRowList.add(tempMap);
 				    mapCellIDValues = new HashMap();
					isModelRemoved = false;
				} else if (iLevelTemp > 1 
						&& ManufacturingPlan.getDerivedMap(context, ManufacturingPlanConstants.TYPE_MODEL).contains(strType)) {
					// No models greater than level 1 and their respective products.
					isModelRemoved = true;
					continue;
				} else if (iLevelTemp > 1 && strObjectId != null && isModelRemoved &&
						ManufacturingPlan.getDerivedMap(context, ManufacturingPlanConstants.TYPE_PRODUCTS).contains(strType)) {
					// Do not process products until we get to the next model, if we removed the last model.
					// This is to eliminate any duplicate products under the model.
					continue;
				} else if (iLevelTemp > 1) { 
					if (strObjectId != null && tempMap.get(cellIdSelect) != null) {
						if (tempMap.get(cellIdSelect) instanceof StringList) {
							StringList listSelect = (StringList)tempMap.get(cellIdSelect);
						    for (int j = 0; j < listSelect.size(); j++) {
						    	String strobject = (String) listSelect.get(j);
						    	if ( strReportFormat != null && strReportFormat.length() > 0) {
						    		processCellIdrowMapForReport( context, mapCellIDValues, strobject, tempMap,
							   				(String)usageshortMap.get((String)((StringList)tempMap.get(cellusageAttribute)).get(j)), 
							   					strReportFormat);								
							   	} else {
							   		boolean isFrozen = ManufacturingPlanUtil.isFrozenState(context, strobject);
							   		processCellIdrowMap(context, mapCellIDValues, strobject, tempMap, 
							   				(String)usageshortMap.get((String)((StringList)tempMap.get(cellusageAttribute)).get(j)),
							   				    buildURL(context, strModelId, strobject, strParentID), isFrozen);
							   	}
						    }
						} else {
							if (strReportFormat !=null && strReportFormat.length()>0) {
								processCellIdrowMapForReport(context, mapCellIDValues, (String)tempMap.get(cellIdSelect), tempMap, 
										(String)usageshortMap.get((String)tempMap.get(cellusageAttribute)), strReportFormat);
							} else {
								boolean isFrozen = ManufacturingPlanUtil.isFrozenState(context, (String)tempMap.get(cellIdSelect));
								processCellIdrowMap(context, mapCellIDValues, (String)tempMap.get(cellIdSelect), tempMap, 
										(String)usageshortMap.get((String)tempMap.get(cellusageAttribute)), 
											buildURL(context,strModelId,(String)tempMap.get(cellIdSelect),strParentID), isFrozen);
							}
						}
					}
				}

				// This is to add last row
				if (mapCellIDValues.size() > 0 && returnRowList.size() > 0) {
					Map updateMap = (Map)returnRowList.get((returnRowList.size()-1));
					updateMap.put(UITableGrid.KEY_CELL_ID,new ArrayList(mapCellIDValues.keySet()));//cell id and column id should match
					updateMap.put(UITableGrid.KEY_CELL_VALUE,new ArrayList(mapCellIDValues.values()));
				}
			}

			rowList = returnRowList;
			if (!((strReportFormat != null && strReportFormat.length() > 0))) {
				rowList = AddEditIcontoEmptyCells(context,selectIds,returnRowList,strObjectID, strParentID);
			}
		
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return rowList;
	}
	
	/*
	 * getting the edit icon to the Empty Cells
	 */
	
	private MapList AddEditIcontoEmptyCells(Context context,String selectIds,MapList rowList,String objectId,String parentid)throws Exception{
		MapList returnList = new MapList();
		try{
			StringList columnids = new StringList(1);
			if(selectIds != null && !"".equals(selectIds.trim()) ){
				columnids = FrameworkUtil.split(selectIds, ",");
			}else{
				MapList tempmapList= getModelDerivationStructure(context, objectId,  "");
				for (Iterator iterator = tempmapList.iterator(); iterator.hasNext();) {
					Map objectMap = (Map) iterator.next();
					columnids.add(objectMap.get(DomainConstants.SELECT_ID));
				}

			}

			HashMap emptyCellvalues = new HashMap();
			ArrayList cellids = new ArrayList();
			ArrayList cellValues = new ArrayList();
			String columnid="";
			if (columnids !=null){
				for (Iterator iterator = rowList.iterator(); iterator.hasNext();) {
					Map rowMap = (Map) iterator.next();
					emptyCellvalues = new HashMap();
					//if CellID not present
					if(rowMap.get(UITableGrid.KEY_CELL_ID) !=null){
						cellids = (ArrayList)rowMap.get(UITableGrid.KEY_CELL_ID);
						cellValues = (ArrayList)rowMap.get(UITableGrid.KEY_CELL_VALUE);
					}else{
						cellids = new ArrayList();
						cellValues=new ArrayList();
					}

					for (int i = 0; i < columnids.size(); i++) {
						columnid =(String)columnids.get(i);
						if(!cellids.contains(columnid)){
							boolean isFrozen = ManufacturingPlanUtil.isFrozenState(context, columnid);
							if(!isFrozen){
								emptyCellvalues.put(columnid, buildIconforemptyCell(buildURL(context,(String)rowMap.get(DomainConstants.SELECT_ID),columnid,parentid), context));
							}

						}

					}


					if(emptyCellvalues.size()>0){

						cellids.addAll(new ArrayList(emptyCellvalues.keySet()));
						cellValues.addAll(new ArrayList(emptyCellvalues.values()));

						rowMap.put(UITableGrid.KEY_CELL_ID,cellids);//cell id and column id should match
						rowMap.put(UITableGrid.KEY_CELL_VALUE,cellValues);

					}
					returnList.add(rowMap);


				}
			}
		}catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return returnList;
	}

	private MapList getModelDerivationStructure(Context context,String strObjectID, String strSelectId) throws Exception{
		MapList colList= new MapList();
		try{
			String strSelIdsWhrClause = "";
			if (strSelectId != null && !"".equalsIgnoreCase(strSelectId)){
				StringList slectedIds = FrameworkUtil.split(strSelectId, ",");

				StringList strListSelectstmts = new StringList();
				strListSelectstmts.add(DomainObject.SELECT_ID);
				strListSelectstmts.add(DomainObject.SELECT_NAME);
				strListSelectstmts.add(DomainObject.SELECT_REVISION);
				strListSelectstmts.add("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]");

				String[] oidsArray = new String[slectedIds.size()];

				colList = DomainObject.getInfo(context,(String[]) slectedIds.toArray(oidsArray), strListSelectstmts);
			} else {

				StringList strListSelectstmts = new StringList();
				strListSelectstmts.add(DomainObject.SELECT_ID);
				strListSelectstmts.add(DomainObject.SELECT_NAME);
				strListSelectstmts.add(DomainObject.SELECT_REVISION);
				strListSelectstmts.add("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]");

				//this get related call should call into get derivations
				StringBuffer stbRelName = new StringBuffer(50);
				stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT);
				stbRelName.append(",");
				stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED);
				stbRelName.append(",");
				stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_DERIVED);

				colList = new DomainObject(strObjectID).getRelatedObjects(context,
						stbRelName.toString(),
						ManufacturingPlanConstants.TYPE_PRODUCTS,
						strListSelectstmts,
						new StringList(),
						false,
						true,
						(short)0,strSelIdsWhrClause,"",0);
			}

		}catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return colList;
	}

	private String buildIconforemptyCell(String url, Context context)throws FrameworkException{
        boolean isCFPUser = ManufacturingPlanUtil.isCFPUser(context);
        String cellvalue = "";
        if(isCFPUser){
		cellvalue = "<table><tr><td style=\"vertical-align:middle;text-align:center;\" >"+
				" <a href=\""+url+"\" >" +
				"<img src=\"../common/images/iconActionEdit.gif\" border=\"0\" alt=\""+EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Heading.EditDesignEffectivity",context.getSession().getLanguage())+"\"/></a></td></tr></table>";
        }else{
        	cellvalue = "<table><tr><td style=\"vertical-align:middle;text-align:center;\" >"+
    				"</td></tr></table>";

        }
		return cellvalue;
	}
	/* builds and gets the url for the image */
	private String buildURL(Context context,String rowId,String columnId,String strParentID ){

		String strURL="JavaScript:showModalDialog(\'../common/emxGridTable.jsp?table=CFPEditEffectivityGridTable" +
				"&amp;rowJPO=CFPModel:getRowsforEditEffectivityGrid" +
				"&amp;expandLevelFilterMenu=FTRExpandAllLevelFilter" +
				"&amp;colJPO=CFPModel:getColumnsforEditEffectivityGrid"+
				"&amp;freezePane=Name,Revision,Usage,State" +
				"&amp;Registered Suite=DMCPlanning" +
				"&amp;suiteKey=DMCPlanning"+
				"&amp;header=DMCPlanning.Heading.EditDesignEffectivity" +
				"&amp;mode=edit"+
				"&amp;objectCompare=false&amp;massUpdate=false&amp;multiColumnSort=false&amp;showPageURLIcon=false&amp;selection=none&amp;HelpMarker=emxhelpeffectivitymatrixedit&amp;editRootNode=false" +
				"&amp;cellRangeJPO=CFPModel:getPFLUsageRange&amp;cellUpdateJPO=CFPModel:updateFeatureUsageforGrid"+
				"&amp;objectId="+rowId+"&amp;selectId="+columnId+"&amp;parentOID="+strParentID+"&amp;SuiteDirectory=dmcplanning" +
				"&amp;cancelProcessJPO=CFPModel:getCellvalueJPO"+
				"\',\'\',\'\',\'true\',\'Medium\')";

		return strURL;
	}


	/**
	 *  gets Column for the Design effevtivity grid and pass it on to GridComponent
	 *  Design effectivity Matrix
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds input arguments.
	 * @return a MapList containing the Maps for rows values
	 *
	 * @throws Exception
	 *             if the operation fails
	 * @since:R212.DA.BRANCHING
	 */
	@com.matrixone.apps.framework.ui.ColJPOCallable
	public MapList getColValuesForDesignEffectivityMatrix(Context context, String[] args)throws FrameworkException {
		MapList colList = new MapList();

		try {

			HashMap hash  = (HashMap) JPO.unpackArgs(args);
			HashMap paramList = (HashMap) hash.get("requestMap");
			String stprObjectId = (String)paramList.get("objectId");

			String strSelectId = (String) paramList.get("selectId");

			colList = getModelDerivationStructure( context, stprObjectId, strSelectId);

			Map tempmap;
			for (int i = 0; i < colList.size(); i++) {
				tempmap =(Map)colList.get(i);

				tempmap.put(UITableGrid.KEY_COL_ID, tempmap.get(DomainConstants.SELECT_ID));
				tempmap.put(UITableGrid.KEY_COL_VALUE,tempmap.get(DomainConstants.SELECT_REVISION));
				tempmap.put(UITableGrid.KEY_COL_GROUP_VALUE,tempmap.get("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]"));
			}

			colList.addSortKey("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]", "descending", "String");
			colList.addSortKey(DomainObject.SELECT_REVISION, "ascending", "String");
			colList.sortStructure();
		}catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return colList;
	}

	/*
	 *  gets all the Master Models and it product Derived Structure under the Model
	 */
    private MapList getModelMasterCompositionStructure(Context context,String strObjectID, String strParentID) throws Exception{
	    MapList relBusObjPageList = new MapList();
		try {

			// Object Selects
			StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainConstants.SELECT_TYPE);
			objectSelects.addElement(DomainConstants.SELECT_NAME);
			objectSelects.addElement(DomainConstants.SELECT_REVISION);
			objectSelects.add(cellIdSelect);
			objectSelects.add(cellusageAttribute);
			objectSelects.add("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]");

			// Rel Selects
			StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

			// Relationships
			StringBuffer stbRelName = new StringBuffer(50);
			stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_MODEL_MASTER_FEATURES);
			stbRelName.append(",");
			stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT);
			stbRelName.append(",");
			stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED);
			stbRelName.append(",");
			stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_DERIVED);

			// Types
			StringBuffer stbTypeName = new StringBuffer(50);
			stbTypeName.append(ManufacturingPlanConstants.TYPE_PRODUCTS);
			stbTypeName.append(",");
			stbTypeName.append(ManufacturingPlanConstants.TYPE_MODEL);

			// Where Clause
			String objectWhere = 
				"(to[" + 
					ManufacturingPlanConstants.RELATIONSHIP_DERIVED + "] || to[" + 
					ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS + "].from.to[" + 
					ManufacturingPlanConstants.RELATIONSHIP_MODEL_MASTER_FEATURES + "].from.id == " + strParentID + " || " +
					"to[" + 
					ManufacturingPlanConstants.RELATIONSHIP_MODEL_MASTER_FEATURES + "].from.id == " + strParentID + "" +
							")";
   
			// Get the objects
			relBusObjPageList = new DomainObject(strObjectID).getRelatedObjects(context,
					stbRelName.toString(), stbTypeName.toString(), objectSelects, relSelects,
					false, true, (short)0, objectWhere, "", (short)0,DomainObject.CHECK_HIDDEN, DomainObject.PREVENT_DUPLICATES,
					(short) DomainObject.PAGE_SIZE, null, null, null, DomainObject.EMPTY_STRING, "", DomainObject.FILTER_STR_AND_ITEM);

			// Sort the structure
			if (relBusObjPageList != null) {
				relBusObjPageList.addSortKey("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]", "descending", "String");		
				relBusObjPageList.addSortKey(DomainRelationship.SELECT_REVISION, "ascending", "String");			
				relBusObjPageList.sortStructure();
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return relBusObjPageList;
	}

	/**
	 *  gets rows for the Design effevtivity grid  and pass it on to GridComponent
	 *  for editing the cell value in the Design effectivity matrix
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds input arguments.
	 * @return a MapList containing the Maps for rows values
	 *
	 * @throws Exception
	 *             if the operation fails
	 *
	 */
    @com.matrixone.apps.framework.ui.RowJPOCallable
	public MapList getRowsforEditEffectivityGrid(Context context, String[] args)throws FrameworkException {
		MapList rowList = new MapList();
		try {
			StringList strListSelectstmts = new StringList();
			strListSelectstmts.add(DomainObject.SELECT_ID);
			strListSelectstmts.add(DomainObject.SELECT_CURRENT);
			strListSelectstmts.add(DomainObject.SELECT_REVISION);
			strListSelectstmts.add("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]");
			strListSelectstmts.add(cellIdSelect);
			strListSelectstmts.add(cellusageAttribute);
			strListSelectstmts.add(cellrelIdSelect);

			StringList strListrelSelectstmts = new StringList();
			strListrelSelectstmts.add(DomainConstants.SELECT_RELATIONSHIP_ID);
			strListrelSelectstmts.add(DomainConstants.SELECT_RELATIONSHIP_NAME);

			HashMap hash  = (HashMap) JPO.unpackArgs(args);
			String ObjectId = (String)((HashMap)hash.get("requestMap")).get("objectId");

			//this get related call should call into get derivations
			StringBuffer stbRelName = new StringBuffer(50);
			stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT);
			stbRelName.append(",");
			stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED);
			stbRelName.append(",");
			stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_DERIVED);

			rowList = new DomainObject(ObjectId).getRelatedObjects(context,
					stbRelName.toString(),
					ManufacturingPlanConstants.TYPE_PRODUCTS,
					strListSelectstmts,
					new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
					false,
					true,
					(short)0,"","",0);

			Map tempmap;

			for (int i = 0; i < rowList.size(); i++) {
				tempmap =(Map)rowList.get(i);

				//the if check is to avaoid null pointer in case we get maplist with expandmultinvel map
				if(tempmap.get(DomainConstants.SELECT_LEVEL) != null) {

					tempmap.put(UITableGrid.KEY_ROW_ID, tempmap.get(DomainConstants.SELECT_ID));
					tempmap.put(UITableGrid.KEY_ROW_REL_ID, tempmap.get(DomainConstants.SELECT_RELATIONSHIP_ID));
					if(tempmap.get(cellIdSelect) !=null){
						tempmap.put(UITableGrid.KEY_CELL_ID,tempmap.get(cellIdSelect));//cell id and column id should match
						Object usageValue=(Object)tempmap.get(cellusageAttribute);
						StringList slObjectList=ManufacturingPlanUtil.convertObjToStringList(context, usageValue);
						StringList attributeDisplayRange = i18nNow
								.getAttrRangeI18NStringList(
										ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE,
										slObjectList, context.getSession()
										.getLanguage());
						tempmap.put(UITableGrid.KEY_CELL_VALUE,attributeDisplayRange);
						tempmap.put(UITableGrid.KEY_CELL_REL_ID,tempmap.get(cellrelIdSelect));//this is required for updateJPO

					}
				}
			}
			rowList.addSortKey("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]", "descending", "String");
			rowList.addSortKey(DomainObject.SELECT_REVISION, "ascending", "String");
			rowList.sortStructure();
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return rowList;
	}

	/**
	 *  gets column for the Design effevtivity grid  and pass it on to GridComponent
	 *  for editing the cell value in the Design effectivity matrix
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds input arguments.
	 * @return a MapList containing the Maps for rows values
	 *
	 * @throws Exception
	 *             if the operation fails
	 * @since:R212.DA.Branching
	 *
	 */
	@com.matrixone.apps.framework.ui.ColJPOCallable
	public MapList getColumnsforEditEffectivityGrid(Context context, String[] args)throws FrameworkException {
		MapList colList = new MapList();
		try{
			HashMap hash  = (HashMap) JPO.unpackArgs(args);
			String ObjectId = (String)hash.get("objectId");
			HashMap paramList = (HashMap) hash.get("requestMap");

			String strSelectId = (String) paramList.get("selectId");

			StringList strListSelectstmts = new StringList();
			strListSelectstmts.add(DomainObject.SELECT_ID);
			strListSelectstmts.add(DomainObject.SELECT_REVISION);
			strListSelectstmts.add("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]");

			colList.add(new DomainObject(strSelectId).getInfo(context, strListSelectstmts));
			Map tempmap;
			for (int i = 0; i < colList.size(); i++) {
				tempmap =(Map)colList.get(i);

				tempmap.put(UITableGrid.KEY_COL_ID, tempmap.get(DomainConstants.SELECT_ID));
				tempmap.put(UITableGrid.KEY_COL_VALUE,tempmap.get(DomainConstants.SELECT_REVISION));
				tempmap.put(UITableGrid.KEY_COL_GROUP_VALUE,tempmap.get("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]"));
			}


		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return colList;
	}


	/**
	 *  gets the cellvalue and updates the cell after editing the Design Effectivity
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds input arguments.
	 * @return a MapList containing the Maps for rows values
	 *
	 * @throws Exception
	 *             if the operation fails
	 *
	 */

	@com.matrixone.apps.framework.ui.CancelProcessCallable
	public HashMap getCellvalueJPO(Context context, String[] args)throws FrameworkException {
		String strValue = "";
		HashMap retMap = new HashMap();
		try{
			Map programMap = (Map) JPO.unpackArgs(args);
			Map rMap = (Map)programMap.get("requestMap");
			String strParentID = (String)rMap.get("parentOID");
			String strObjectID = (String)rMap.get("objectId");
			String strselectId = (String)rMap.get("selectId");
			String strUrl = buildURL(context, strObjectID,strselectId,strParentID);
			HashMap usageshortMap = getUsageRangeShortForm(context);

			StringList strListSelectstmts = new StringList();
			strListSelectstmts.add(DomainObject.SELECT_ID);
			strListSelectstmts.add("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]");
			strListSelectstmts.add(DomainObject.SELECT_REVISION);
			strListSelectstmts.add(cellIdSelect);
			strListSelectstmts.add(cellusageAttribute);

			StringList strListrelSelectstmts = new StringList();
			strListrelSelectstmts.add(DomainConstants.SELECT_RELATIONSHIP_ID);
			strListrelSelectstmts.add(DomainConstants.SELECT_RELATIONSHIP_NAME);

			String objectWhere = " (to[" + ManufacturingPlanConstants.RELATIONSHIP_MODEL_MASTER_FEATURES + "] || to[" + 
					ManufacturingPlanConstants.RELATIONSHIP_DERIVED + "] || to[" + ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS +
					"].from.to[" + ManufacturingPlanConstants.RELATIONSHIP_MODEL_MASTER_FEATURES + "].from.id == " + strParentID + ")";

			//this get related call should call into get derivations
			StringBuffer stbRelName = new StringBuffer(50);
			stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT);
			stbRelName.append(",");
			stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED);
			stbRelName.append(",");
			stbRelName.append(ManufacturingPlanConstants.RELATIONSHIP_DERIVED);
			MapList rowList = new DomainObject(strObjectID).getRelatedObjects(context,
					stbRelName.toString(),
					ManufacturingPlanConstants.TYPE_PRODUCTS,
					strListSelectstmts,
					new StringList(DomainConstants.SELECT_RELATIONSHIP_ID),
					false,
					true,
					(short)0,objectWhere,"",0);

			rowList.addSortKey("attribute["+ManufacturingPlanConstants.ATTRIBUTE_MARKETING_NAME+"]", "descending", "String");		
			rowList.addSortKey(DomainRelationship.SELECT_REVISION, "ascending", "String");			
			rowList.sortStructure();

			Map tempmap;
			String strModel="";
			HashMap mapCellIDValues= new HashMap();
			for (int i = 0; i < rowList.size(); i++) {
				tempmap =(Map)rowList.get(i);
				if (tempmap.get(DomainConstants.SELECT_LEVEL) != null) {
					if(tempmap.get(cellIdSelect) != null) {
						if (tempmap.get(cellIdSelect) instanceof StringList) {
							StringList idlist = (StringList)tempmap.get(cellIdSelect);
							int cellIndex = idlist.indexOf(strselectId);
							if (cellIndex > -1) {
								String strCellVal=(String)((StringList)tempmap.get(cellusageAttribute)).get(cellIndex);
								processCellIdrowMap(context,mapCellIDValues, strObjectID, tempmap, 
										(String)usageshortMap.get(strCellVal), strUrl, false);
							}
						} else {
							if (strselectId.equalsIgnoreCase((String)tempmap.get(cellIdSelect))) {
								processCellIdrowMap(context,mapCellIDValues, strObjectID, tempmap, 
										(String)usageshortMap.get((String)tempmap.get(cellusageAttribute)), strUrl, false);
							}else {
								String strUsage1="";
								processCellIdrowMap(context,mapCellIDValues, strObjectID,tempmap,strUsage1,strUrl, false);
							}
						}
					} else {
						String strUsage1="";
						processCellIdrowMap(context,mapCellIDValues, strObjectID,tempmap,strUsage1,strUrl, false);
					}
				}
			}
			strValue = (String)mapCellIDValues.get(strObjectID);

			if (ProductLineCommon.isNotNull(strValue)) {
				strValue=strValue.replaceAll("\\\"","\\\\\"");
				strValue=strValue.replaceAll("'","\\\\'");
			}
            if(strValue==null)
            	strValue="";
			retMap.put("Action", "execScript");
			retMap.put("Message", "{main:function __main(){refreshParentCellValue('"+strObjectID+"','"+strselectId+"','"+strValue+"')}}");

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return retMap;
	}

	/**
	 * Program defined to return Maplist which will hold Manufacturing Plan
	 * which are in the Main Derivation Chain, called in as program for the
	 * Model->MP Derivation table
	 *
	 * @param context
	 *           Matrix context
	 * @param args
	 * @return
	 * @throws FrameworkException
	 * @since CFP R212Derivations
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getManufacturingPlanRevision(Context context, String[] args)
			throws FrameworkException {
		MapList mlMPDerivation = new MapList();
		try {
			Map programMap = (Map) JPO.unpackArgs(args);
			String strModelId = (String) programMap.get("objectId");
			//1. will need to check if MPM exist
			Model modelBean= new Model(strModelId);
			mlMPDerivation=modelBean.getManufacturingPlanRevision(context);
			for (Iterator iterator = mlMPDerivation.iterator(); iterator.hasNext();) {
				Map objectMap = (Map) iterator.next();
				objectMap.put("level", "1");
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return mlMPDerivation;
	}

	/**
	 * gets the Managed Revisions of the Master Composition
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return MapList -
	 * 			  Contains the Products, Features objects
	 * @throws Exception
	 *             if the operation fails
	 *
	 * @since DMCPlanning R211DABranching
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getManagedRevisions(Context context, String[] args)
			throws Exception {

		MapList relBusObjPageList =  new MapList();
		try {
			//Unpacking the args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			//Gets the objectid and the relation names from args
			String strObjectid = (String)programMap.get("objectId");
			String filterExpression = (String)programMap.get("CFFExpressionFilterInput_OID");

			//get the Level and Limit
			int iLevel =ManufacturingPlan_mxJPO.getLevelfromSB(context,args);
			int iLimit = ManufacturingPlan_mxJPO.getLimit(context,args);


			StringList slObjSelects = new StringList(DomainObject.SELECT_ID);           
			StringList slRelSelects = new StringList(DomainObject.SELECT_RELATIONSHIP_ID);
			StringBuffer strRelPattern = new StringBuffer(50).append(ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT);
			strRelPattern.append(",");
			strRelPattern.append(RELATIONSHIP_DERIVED_ABSTRACT);
			String strTypePattern = ManufacturingPlanConstants.TYPE_PRODUCTS;
			// Get the derivations.
			DomainObject domObject = new DomainObject(strObjectid);
			MapList MapProductlist = domObject.getRelatedObjects(
					context,
					strRelPattern.toString(),
					strTypePattern,
					slObjSelects,
					slRelSelects,
					false,
					true,
					(short)iLevel,
					DomainObject.EMPTY_STRING,
					DomainObject.EMPTY_STRING,
					(short)iLimit,
					DomainObject.CHECK_HIDDEN,
					DomainObject.PREVENT_DUPLICATES,
					(short) DomainObject.PAGE_SIZE,
					null,
					null,
					null,
					DomainObject.EMPTY_STRING,
					DomainObject.EMPTY_STRING,
					(short) 1);

			MapProductlist.addSortKey(DomainRelationship.SELECT_NAME, "descending", "String");
			MapProductlist.addSortKey(DomainObject.SELECT_REVISION, "ascending", "String");
			MapProductlist.sortStructure();

			HashMap hmTemp = new HashMap();
			hmTemp.put("expandMultiLevelsJPO", "true");
			MapProductlist.add(hmTemp);

			relBusObjPageList = MapProductlist;



		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return  relBusObjPageList;
	}

	/**
	 * get the Master Composition associated to Model
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return MapList -
	 * 			Contain the Models
	 * @throws Exception
	 *             if the operation fails
	 * @since DMCPlanning R211_DABranching
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getMasterComposition(Context context, String[] args)
			throws Exception {

		MapList relBusObjPageList =  new MapList();
		try {
			//Unpacking the args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			//Gets the objectid and the relation names from args
			String strObjectid = (String)programMap.get("objectId");
			String filterExpression = (String)programMap.get("CFFExpressionFilterInput_OID");

			// form the Limit filter for the query
			int iLimit = ManufacturingPlan_mxJPO.getLimit(context,args);


			Model modelBean = new Model(strObjectid);
			relBusObjPageList = modelBean.getMasterComposition(context,iLimit,filterExpression);

			relBusObjPageList.addSortKey(DomainRelationship.SELECT_NAME, "descending", "String");			
			relBusObjPageList.sortStructure();


		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return  relBusObjPageList;
	}

}



