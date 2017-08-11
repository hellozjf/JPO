/*
 ** ${CLASSNAME}
 ** 
 **Copyright (c) 1993-2016 Dassault Systemes.
 ** All Rights Reserved.getActiveManufacturingPlans
 **This program contains proprietary and trade secret information of
 **Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 **or intended publication of such program
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import matrix.db.BusinessObject;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Policy;
import matrix.util.Pattern;
import matrix.util.StringList;

import com.matrixone.apps.dmcplanning.ManufacturingPlanUtil;
import com.matrixone.apps.dmcplanning.ManufacturingPlan;
import com.matrixone.apps.dmcplanning.ManufacturingPlanConstants;
import com.matrixone.apps.dmcplanning.Model;
import com.matrixone.apps.dmcplanning.Product;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.DerivationUtil;
import com.matrixone.apps.framework.ui.UITableCommon;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;
import com.matrixone.apps.productline.ProductLineUtil;
import com.matrixone.jdom.Element;


/**
 * This JPO class has some methods pertaining to Manufacturing Plan.
 *
 * @author IVU
 * @since DMCPlanning R209
 */
public class ManufacturingPlanBase_mxJPO extends emxDomainObject_mxJPO {

	protected static final String SYMB_WILD = "*";
	protected static final String COMMA = ",";
	protected static final String SUITE_KEY = "DMCPlanning";
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
	 * @author IVU
	 * @since DMCPlanning R209
	 */
	public ManufacturingPlanBase_mxJPO(Context context, String[] args)
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
	 * This method is used to get the level from the context of the Structure Browser.
	 *
	 * @param context
	 *          the eMatrix <code>Context</code> object
	 * @param args
	 * 			Holds the HashMap with the SB related Details
	 * @return int
	 * 		    level information.
	 * @throws Exception
	 * @author IVU
	 * @since DMCPlanning R209
	 */
	public static int getLevelfromSB(Context context, String[] args) throws Exception{
		short recurseLevel=1;
		try {
			// unpack the arguments to get the level Details
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			// get the level information from the ParamMap
			String strExpandLevel = (String) programMap.get("expandLevel");
			// if expand level is not available
			if (strExpandLevel == null || ("".equals(strExpandLevel))
					|| ("null".equals(strExpandLevel))) {
				strExpandLevel = Integer.toString(1);
			}

			// If the ExpandLevel is all then set the recurselevel to 0
			if (strExpandLevel.equalsIgnoreCase((ProductLineConstants.RANGE_VALUE_ALL))){
				recurseLevel = (short) 0;
			}
			else{
				recurseLevel =(short) (Short.parseShort(strExpandLevel));
			}

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return recurseLevel;
	}
	/**
	 * This method is used to get the Limit from the context of the Structure Browser.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            Holds the HashMap with the SB related Details
	 * @return int level information.
	 * @throws Exception
	 * @author IVU
	 * @since DMCPlanning R209
	 */
	public static int getLimit(Context context, String[] args) throws Exception {
		int limit = 0;
		try {
			// unpack the arguments to get the level Details
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			// get the limit information from the ParamMap
			// This is left for the future purpose.
			String strLimitFilter = (String) programMap.get("XYZ");

			// Limit is read from the property file
			// Modified for IR-044253V6R2011x STARTS
			limit = -1;
			if (limit < 0) {
				limit = 32767;
			}
			// Modified for IR-044253V6R2011x ENDS
			// emxConfiguration.Search.QueryLimit
			if (strLimitFilter != null) {
				if (strLimitFilter.length() > 0) {
					// Added for bug 377756 - If user enters large number than
					// short, defaulting it to max value of short
					limit = (short) Integer.parseInt(strLimitFilter);
					if (limit < 0) {
						limit = 32767;
					}
				}
			}

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return limit;
	}

	/**
	 * This Methods is used to get all the Active Manufacturing Plans connected to the Product/Feature.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Active Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 * @author IVU
	 * @since DMCPlanning R209-Modified in R212Derivations
	 * @grade 0
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getActiveManufacturingPlans(Context context, String[] args)
	throws FrameworkException {
		MapList mapActiveMPs =new MapList();
		
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			//Gets the objectids and the relation names from args
			String strObjectid = (String)programMap.get("objectId");

			int iLevel = getLevelfromSB(context,args);
			// Create the instance of ManufacturingPlan bean and call
			// getManufacturingPlans method
			ManufacturingPlan mpBean = new ManufacturingPlan(strObjectid);
			StringList slObject = new StringList(
					ProductLineConstants.SELECT_TYPE);
			slObject.add(ProductLineConstants.SELECT_NAME);
			slObject.add(ProductLineConstants.SELECT_REVISION);
			slObject.add(ProductLineConstants.SELECT_ID);
			
			String mpMainDerivedType = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
				+ "].from." + ManufacturingPlanConstants.SELECT_TYPE;
			String mpMainDerivedTitle = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
				+ "].from."
				+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;
			String mpMainDerivedRevision = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
				+ "].from." + ManufacturingPlanConstants.SELECT_REVISION;
			String mpMainDerivedId = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
				+ "].from." + ManufacturingPlanConstants.SELECT_ID;
			
			String plannedForMarketingName="to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+ManufacturingPlanConstants.SELECT_ATTRIBUTE_MARKETING_NAME;
			String plannedForRevision="to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+ManufacturingPlanConstants.SELECT_REVISION;
			String plannedForType="to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+ManufacturingPlanConstants.SELECT_TYPE;
			String plannedForID="to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+ManufacturingPlanConstants.SELECT_ID;

			slObject.add(plannedForMarketingName);
			slObject.add(plannedForRevision);
			slObject.add(plannedForType);
			slObject.add(plannedForID);

			slObject.add(mpMainDerivedType);
			slObject.add(mpMainDerivedTitle);
			slObject.add(mpMainDerivedRevision);
			slObject.add(mpMainDerivedId);
			
			mapActiveMPs = (MapList)mpBean.getManufacturingPlans(context, slObject, new StringList(), iLevel, 0);
			if (mapActiveMPs != null && mapActiveMPs.size() > 0) {
				mapActiveMPs.addSortKey(DomainObject.SELECT_REVISION, "ascending", "String");
				mapActiveMPs.sort();
			}

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage()); 
		}
		return mapActiveMPs;
	}


	/**
	 * This Methods is used to get all the Manufacturing Plan Breakdown connected to Manufacturing Plan
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Active Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 * @author IVU
	 * @since DMCPlanning R209- Modified in R212Derivations
	 * @grade 0
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getManufacturingPlanBreakdown(Context context, String[] args)
	throws FrameworkException {
		MapList mapMPBreakdowns=new MapList();
		
		try {
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			//Gets the objectids and the relation names from args
			String strObjectid = (String)programMap.get("objectId");
			StringList slObjSelects = new StringList(
					ProductLineConstants.SELECT_TYPE);
			slObjSelects.add(ProductLineConstants.SELECT_NAME);
			slObjSelects.add(ProductLineConstants.SELECT_REVISION);
			slObjSelects.add(ProductLineConstants.SELECT_ID);
			slObjSelects.add("to[" + ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
					+ "].from." + ManufacturingPlanConstants.SELECT_ID);
			slObjSelects.add("to[" + ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
					+ "].from." + ManufacturingPlanConstants.SELECT_NAME);
			
			String mpMainDerivedType = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
				+ "].from." + ManufacturingPlanConstants.SELECT_TYPE;
			String mpMainDerivedTitle = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
				+ "].from."
				+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;
			String mpMainDerivedRevision = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
				+ "].from." + ManufacturingPlanConstants.SELECT_REVISION;

			String plannedForMarketingName="to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+ManufacturingPlanConstants.SELECT_ATTRIBUTE_MARKETING_NAME;
			String plannedForRevision="to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+ManufacturingPlanConstants.SELECT_REVISION;
			String plannedForType="to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+ManufacturingPlanConstants.SELECT_TYPE;
			String plannedForID="to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+ManufacturingPlanConstants.SELECT_ID;

			slObjSelects.add(plannedForMarketingName);
			slObjSelects.add(plannedForRevision);
			slObjSelects.add(plannedForType);
			slObjSelects.add(plannedForID);

			slObjSelects.add(mpMainDerivedType);
			slObjSelects.add(mpMainDerivedTitle);
			slObjSelects.add(mpMainDerivedRevision);
			
			int iLevel = getLevelfromSB(context,args);
			// Create the instance of ManufacturingPlan bean and call
			// getManufacturingPlanBreakdowns method
			ManufacturingPlan mpBean = new ManufacturingPlan(strObjectid);
			mapMPBreakdowns = (MapList) mpBean.getManufacturingPlanBreakdowns(context,slObjSelects,new StringList(), iLevel, 0);
		} catch (Exception e) {
			throw new  FrameworkException(e.getMessage());
		}

		return mapMPBreakdowns;
	}

	/**
	 * This Methods is used to get all Parent Manufacturing Plans to which context Manufacturing Plan is connected
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Archived Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 * @author IVU
	 * @since DMCPlanning R209
	 * @grade 0
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getParentManufacturingPlan(Context context, String[] args)
	throws Exception {

		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		//Gets the objectids and the relation names from args
		String strObjectid = (String)programMap.get("objectId");

		int iLevel = getLevelfromSB(context,args);
		// Create the instance of ManufacturingPlan bean and call
		// getManufacturingPlanWhereUsed method
		ManufacturingPlan mpBean = new ManufacturingPlan(strObjectid);
		MapList mapMPBreakdowns = (MapList) mpBean.getManufacturingPlanWhereUsed(context,new StringList(),new StringList(), iLevel, 0);

		return mapMPBreakdowns;
	}
	/**
	 * This Methods is used to get all the Archived Manufacturing Plans connected to the Product/Feature.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 *            Key- ObjectId which holds contest Object is(product/manufacturing Plan)
	 * @return Object - MapList containing the id of Archived Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 * @author VZS
	 * @since R209
	 * @grade 0
	 * @deprecated
	 */

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getArchivedManufacturingPlans(Context context, String[] args)
	throws Exception {
		MapList mapArchiveMPs =  new MapList();

		try {
			//Unpacking the args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			//Gets the objectids and the relation names from args
        String strObjectid = (String)programMap.get("objectId");

      //object select other than basic selectes
		 StringList objectSelects = new StringList();
			objectSelects.add(DomainConstants.SELECT_DESCRIPTION);
			objectSelects.add(DomainConstants.SELECT_OWNER);
			objectSelects.add(DomainConstants.SELECT_CURRENT);
			objectSelects.add("attribute["+ManufacturingPlanConstants.ATTRIBUTE_TITLE+"]");
			objectSelects.add("to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+DomainConstants.SELECT_NAME);
			objectSelects.add("to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+DomainConstants.SELECT_REVISION);
			objectSelects.add("to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+DomainConstants.SELECT_TYPE);
			objectSelects.add("from["+ManufacturingPlanConstants.RELATIONSHIP_DERIVED+"].to."+DomainConstants.SELECT_NAME);



        //we do pup pop because the Archived Policy doesn't have read show access
        ContextUtil.pushContext(context);

      //Get the basic details  of Manufacturing Plans
        ManufacturingPlan mpBean = new ManufacturingPlan(strObjectid);
        mapArchiveMPs= (MapList) mpBean.getArchivedManufacturingPlan(context, objectSelects,new StringList(),1, 0);

       // mapArchiveMPs=getArchivedManufacturingPlansDetails( context,  archivedMpsList);
        ContextUtil.popContext(context);


        } catch (Exception e) {
			throw new FrameworkException("getArchivedManufacturingPlans==="+e.getMessage());
		}
        return mapArchiveMPs;
    }
    /**

    /**
     * This Methods gets contextObject values throw "objectList" key present in args passed by expandprogram
     * @used : used as column JPO in FTRArchivedManufacturingPlanContextSummaryTable
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param String[] -
     *            Holds the all the arguments passed by AEF throw structure browser expand.
     *            we will use only key= "objectList"
     * @return StringList -  contains the list of contexObjects of the Archived Manufacturing Plans
     * @throws Exception
     *             if the operation fails
     * @author VZS
     * @since R209
     * @grade 0
     * @deprecated
     */
    public StringList getArchivedContextObject(Context context, String[] args)throws Exception {
    	StringList contextObjectList= new StringList();
    	try {
    		//XSSOK- Deprecated
    		//Unpacking the args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			Map tempMAp;
			//Start IR-030914V6R2011- added hyperlink to context object.
			String objNameRev = "";
			HashMap paramMap = (HashMap) programMap.get("paramList");
			String strParentId = (String) paramMap.get("parentOID");
			String exportFormat = null;
            boolean exportToExcel = false;
            
      		HashMap requestMap = (HashMap)programMap.get("paramList");
      		if(requestMap!=null && requestMap.containsKey("reportFormat")){
      			exportFormat = (String)requestMap.get("reportFormat");
      		}
      		if("CSV".equals(exportFormat)){
      			exportToExcel = true;
      		}

			for (int iCnt = 0; iCnt < objectList.size(); iCnt++) {			
				tempMAp = (Map) objectList.get(iCnt);
				if (tempMAp.containsKey("to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.name")){	            	         	
					objNameRev = (String) tempMAp.get("to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.name")
					+ " " +(String) tempMAp.get("to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.revision");
					
					if(exportToExcel){
						contextObjectList.add(objNameRev);						
					}
					else{
					StringBuffer sb = new StringBuffer();
					String strTemp = "<a TITLE=";
					String strEndHrefTitle = ">";
					String strEndHref = "</a>";
					sb =  sb.append(strTemp);
					String strTypeIcon= ProductLineCommon.getTypeIconProperty(context, (String) tempMAp.get("to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+DomainConstants.SELECT_TYPE));
					sb =  sb.append("\"\"")
					.append(strEndHrefTitle)
					.append("<img border=\'0\' src=\'../common/images/"+strTypeIcon+"\'/>")
					.append( strEndHref)
					.append(" ");
					sb.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
					sb.append(strParentId);
					sb.append("')\">");
					sb.append(XSSUtil.encodeForXML(context,objNameRev));
					sb.append("</A>");	              	
					contextObjectList.add(sb.toString());
					}
				}else
					contextObjectList.add(DomainConstants.EMPTY_STRING);
			}
			//end IR-030914V6R2011- added hyperlink to context object.
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
    	return contextObjectList;
    }
    /**
     * This Methods gets Derivedform values throw "objectList" key present in args passed by expandprogram
     * @used : used as  Derivedform column JPO for  in FTRArchivedManufacturingPlanContextSummaryTable
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param String[] -
     *            Holds the all the arguments passed by AEF throw structure browser expand.
     *            we will use only key= "objectList"
     * @return StringList -  contains the list of contexObjects of the Archived Manufacturing Plans
     * @throws Exception
     *             if the operation fails
     * @author VZS
     * @since R209
     * @grade 0
     * @deprecated
     */
    public StringList getArchivedDerivedFrom(Context context, String[] args)throws Exception {
    	StringList derivedFromList= new StringList();
    	try {
    		//XSSOK- deperecated
    		//Unpacking the args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            String exportFormat = null;
            boolean exportToExcel = false;
            
      		HashMap requestMap = (HashMap)programMap.get("paramList");
      		if(requestMap!=null && requestMap.containsKey("reportFormat")){
      			exportFormat = (String)requestMap.get("reportFormat");
      		}
      		if("CSV".equals(exportFormat)){
      			exportToExcel = true;
      		}
            String derivedFromSelectable ="from["+ManufacturingPlanConstants.RELATIONSHIP_DERIVED+"].to."+DomainConstants.SELECT_NAME;
            Map tempMap;
            for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
            	//checking if they have map contains any value, if not pass in an empty value to the column
            	tempMap =(Map) iterator.next();
            	if((tempMap).containsKey(derivedFromSelectable)){
            		if(exportToExcel){
            			String strName = tempMap.get(derivedFromSelectable).toString();
            			derivedFromList.add(strName);
            		}
            		else{
            		StringBuffer sbBuffer = new StringBuffer(400);
            		sbBuffer = sbBuffer.append("<img src=\"images/").append(
            				ProductLineCommon.getTypeIconProperty(context, (String)(tempMap.get(DomainConstants.SELECT_TYPE))))
     	                   .append("\"").append("/>").append("<a TITLE=\"\">").append(" ").append(XSSUtil.encodeForXML(context,tempMap.get(derivedFromSelectable).toString()))
    	                    .append("</a>");
    	            derivedFromList.add(sbBuffer.toString());
            		}

            		//derivedFromList.add(tempMap.get(derivedFromSelectable));
            	}else{
            		derivedFromList.add(DomainConstants.EMPTY_STRING);
            	}

			}

    	} catch (Exception e) {
			throw new FrameworkException("getArchivedDerivedForm==="+e.getMessage());
		}
		return derivedFromList;
	}

	/**
	 * This Methods gets Name values throw "objectList" key present in args passed by expandprogram
	 * @used : used as  Name column JPO for  in FTRArchivedManufacturingPlanContextSummaryTable
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String[] -
	 *            Holds the all the arguments passed by AEF throw structure browser expand.
	 *            we will use only key= "objectList"
	 * @return StringList -  contains the list of contexObjects of the Archived Manufacturing Plans
	 * @throws Exception
	 *             if the operation fails
	 * @author VZS
	 * @since R209
	 * @grade 0
	 * @deprecated
	 */
	public StringList getArchivedName(Context context, String[] args)throws Exception {
		StringList nameList= new StringList();
		try {
			//XSSOK Deprecated
			//Unpacking the args
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			MapList objectList = (MapList)programMap.get("objectList");
			String type="";
			String exportFormat = null;
            boolean exportToExcel = false;
      		HashMap requestMap = (HashMap)programMap.get("paramList");
      		if(requestMap!=null && requestMap.containsKey("reportFormat")){
      			exportFormat = (String)requestMap.get("reportFormat");
      		}
      		if("CSV".equals(exportFormat)){
      			exportToExcel = true;
      		}

			for (int i = 0; i < objectList.size(); i++) {

				type = (String)((Map) objectList.get(i)).get(DomainConstants.SELECT_TYPE);
				//iconSmallDefault.gif
				//I am not doing null check here becuase it is Madatory field to create an object
				if(exportToExcel){
				String strName = ((Map) objectList.get(i)).get(DomainConstants.SELECT_NAME).toString();
				nameList.add(strName);
				}
				else{
				StringBuffer sbBuffer = new StringBuffer(400);
	            sbBuffer = sbBuffer.append("<img src=\"images/").append(
	            		ProductLineCommon.getTypeIconProperty(context, type)).append("\"").append("/>")
	            		.append("<a TITLE=\"\"> ").append(" ").append(XSSUtil.encodeForXML(context,((Map) objectList.get(i)).get(DomainConstants.SELECT_NAME).toString()))
	                    .append("</a>");
				nameList.add(sbBuffer.toString());
				}

			}



		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
    	return nameList;
    }
    /**
     * This Methods gets Title values throw "objectList" key present in args passed by expandprogram
     * @used : used as  Title column JPO for  in FTRArchivedManufacturingPlanContextSummaryTable
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param String[] -
     *            Holds the all the arguments passed by AEF throw structure browser expand.
     *            we will use only key= "objectList"
     * @return StringList -  contains the list of Titles of the Archived Manufacturing Plans
     * @throws Exception
     *             if the operation fails
     * @author VZS
     * @since R209
     * @grade 0
     * @deprecated
     */
    public StringList getArchivedTitle(Context context, String[] args)throws Exception {
    	StringList titleList= new StringList();
    	try {
    		//XSSOK- Deprecated
    		//Unpacking the args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            String titleSelectable="attribute["+ManufacturingPlanConstants.ATTRIBUTE_TITLE+"]";
            Map tempMAp;
            String exportFormat = null;
            boolean exportToExcel = false;
      		HashMap requestMap = (HashMap)programMap.get("paramList");
      		if(requestMap!=null && requestMap.containsKey("reportFormat")){
      			exportFormat = (String)requestMap.get("reportFormat");
      		}
      		if("CSV".equals(exportFormat)){
      			exportToExcel = true;
      		}
            for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
            	//checking if they have map contains any value, if not pass in an empty value to the column
            	tempMAp =(Map) iterator.next();
            	if(tempMAp.containsKey(titleSelectable)){
            		//IR-043838V6R2011 escaped & and < character
            		String archivedMPTitle=(String)tempMAp.get(titleSelectable);
            		if(exportToExcel){
            			String strarchivedMPTitle = archivedMPTitle;
            			titleList.add(strarchivedMPTitle);
            		}
            		else{
            		//archivedMPTitle = archivedMPTitle.replaceAll("&", "&amp;");
            		archivedMPTitle = archivedMPTitle.replaceAll("<", "&lt;");
            		StringBuffer sbBuffer = new StringBuffer(400);
    	            sbBuffer = sbBuffer.append("<img src=\"images/").append(
    	            		ProductLineCommon.getTypeIconProperty(context, (String)(tempMAp.get(DomainConstants.SELECT_TYPE))))
    	                   .append("\"").append("/>").append("<a TITLE=\"\">").append(" ").append(XSSUtil.encodeForXML(context,archivedMPTitle))
   	                    .append("</a>");
    	            titleList.add(sbBuffer.toString());
            		}
            		//titleList.add(tempMAp.get(titleSelectable));
            	}else{
            		titleList.add(DomainConstants.EMPTY_STRING);
            	}


			}

    	} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
    	return titleList;
    }
    /**
     * This Methods gets Type values throw "objectList" key present in args passed by expandprogram
     * @used : used as  Type column JPO for  in FTRArchivedManufacturingPlanContextSummaryTable
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param String[] -
     *            Holds the all the arguments passed by AEF throw structure browser expand.
     *            we will use only key= "objectList"
     * @return StringList -  contains the list of Type of the Archived Manufacturing Plans
     * @throws Exception
     *             if the operation fails
     * @author VZS
     * @since R209
     * @grade 0
     * @deprecated
     */
    public StringList getArchivedType(Context context, String[] args)throws Exception {
    	StringList typeList= new StringList();
    	try {
    		//Unpacking the args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
            	//not checking for null because TNR are mandatory to create an object
            	typeList.add(((Map) iterator.next()).get(DomainConstants.SELECT_TYPE));

			}

    	} catch (Exception e) {
			throw new FrameworkException("getArchivedType==="+e.getMessage());
		}
    	return typeList;
    }
    /**
     * This Methods gets Revision values throw "objectList" key present in args passed by expandprogram
     * @used : used as  Revision column JPO for  in FTRArchivedManufacturingPlanContextSummaryTable
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param String[] -
     *            Holds the all the arguments passed by AEF throw structure browser expand.
     *            we will use only key= "objectList"
     * @return StringList -  contains the list of revision of the Archived Manufacturing Plans
     * @throws Exception
     *             if the operation fails
     * @author VZS
     * @since R209
     * @grade 0
     * @deprecated
     */
    public StringList getArchivedRevision(Context context, String[] args)throws Exception {
    	StringList revisionList= new StringList();
    	try {
    		//Unpacking the args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
            	//not checking for null value because TNR are mandatory to create an object
            	revisionList.add(((Map) iterator.next()).get(DomainConstants.SELECT_REVISION));

			}

		} catch (Exception e) {
			throw new FrameworkException("getArchivedRevision==="+e.getMessage());
		}
    	return revisionList;
    }
    /**
     * This Methods gets Description values throw "objectList" key present in args passed by expandprogram
     * @used : used as  Description column JPO for  in FTRArchivedManufacturingPlanContextSummaryTable
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param String[] -
     *            Holds the all the arguments passed by AEF throw structure browser expand.
     *            we will use only key= "objectList"
     * @return StringList -  contains the list of Description of the Archived Manufacturing Plans
     * @throws Exception
     *             if the operation fails
     * @author VZS
     * @since R209
     * @grade 0
     * @deprecated
     */
    public StringList getArchivedDescription(Context context, String[] args)throws Exception {
    	StringList descList= new StringList();
    	try {
    		//Unpacking the args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map tempMap;
            for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
            	//checking if they have map contains any value, if not pass in an empty value to the column
            	tempMap =(Map) iterator.next();
            	if((tempMap).containsKey(DomainConstants.SELECT_DESCRIPTION)){
            		descList.add(tempMap.get(DomainConstants.SELECT_DESCRIPTION));
            	}else{
            		descList.add(DomainConstants.EMPTY_STRING);
            	}


			}

		} catch (Exception e) {
			throw new FrameworkException("getArchivedDescription==="+e.getMessage());
		}
    	return descList;
    }
    /**
     * This Methods gets Owner values throw "objectList" key present in args passed by expandprogram
     * @used : used as  Owner column JPO for  in FTRArchivedManufacturingPlanContextSummaryTable
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param String[] -
     *            Holds the all the arguments passed by AEF throw structure browser expand.
     *            we will use only key= "objectList"
     * @return StringList -  contains the list of owner's of the Archived Manufacturing Plans
     * @throws Exception
     *             if the operation fails
     * @author VZS
     * @since R209
     * @grade 0
     * @deprecated
     */
    public StringList getArchivedOwner(Context context, String[] args)throws Exception {
    	StringList ownerList= new StringList();
    	try {
    		//Unpacking the args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map tempMap;
            for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
            	//checking if they have map contains any value, if not pass in an empty value to the column
            	tempMap =(Map) iterator.next();
            	if((tempMap).containsKey(DomainConstants.SELECT_OWNER)){
            		ownerList.add(tempMap.get(DomainConstants.SELECT_OWNER));
            	}else{
            		ownerList.add(DomainConstants.EMPTY_STRING);
            	}


			}

		} catch (Exception e) {
			throw new FrameworkException("getArchivedOwner==="+e.getMessage());
		}
    	return ownerList;
    }
    /**
     * This Methods gets Name state throw "objectList" key present in args passed by expandprogram
     * @used : used as  Name column JPO for  in FTRArchivedManufacturingPlanContextSummaryTable
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param String[] -
     *            Holds the all the arguments passed by AEF throw structure browser expand.
     *            we will use only key= "objectList"
     * @return StringList -  contains the list of state of the Archived Manufacturing Plans
     * @throws Exception
     *             if the operation fails
     * @author VZS
     * @since R209
     * @grade 0
     * @deprecated
     */
    public StringList getArchivedState(Context context, String[] args)throws Exception {
    	StringList currStateList= new StringList();
    	try {
    		//Unpacking the args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map tempMap;
            //Added by KXB for Internationalization STARTS
            String strArchState = "";
			String stateArchived  = PropertyUtil.getSchemaProperty(context,"policy",
									ManufacturingPlanConstants.POLICY_ARCHIVED,
            						"state_Archived");
			String language = context.getSession().getLanguage();
			String strState = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Command.ArchivedManufacturingPlans",language);
            for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
            	//checking if they have map contains any value, if not pass in an empty value to the column
            	tempMap =(Map) iterator.next();
            	strArchState = (String)tempMap.get(DomainConstants.SELECT_CURRENT);
            	if((tempMap).containsKey(DomainConstants.SELECT_CURRENT)){
            		if(strArchState.equals(stateArchived)){
            		currStateList.add(strState);
            		}
            //Added by KXB for Internationalization ENDS
            	}else{
            		currStateList.add(DomainConstants.EMPTY_STRING);
            	}

			}

		} catch (Exception e) {
			throw new FrameworkException("getArchivedState==="+e.getMessage());
		}
		return currStateList;
	}
	/**
	 * This Methods is used to get context of Manufacturing Plan
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Archived Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 * @author WPK
	 * @since DMCPlanning R209
	 * @grade 0
	 */
	public String displayContext (Context context, String[] args)
	throws Exception {
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		//Gets the objectids and the relation names from args
		String strContextId = (String)requestMap.get("parentOID");
		DomainObject objContext =  new DomainObject(strContextId);
		//Added by KXB for 	IR-030541 Starts
		StringList newList = new StringList();
		newList.addElement(ManufacturingPlanConstants.SELECT_NAME);
		newList.addElement(ManufacturingPlanConstants.SELECT_REVISION);	
		newList.addElement(ManufacturingPlanConstants.SELECT_TYPE);
		Map strContextMap = objContext.getInfo(context, newList);
		//start IR-031126V6R2011 addded Type Icon to Context Object
		StringBuffer sb = new StringBuffer();
		String strTemp = "<a TITLE=";
		String strEndHrefTitle = ">";
		String strEndHref = "</a>";
		sb =  sb.append(strTemp);
		String strTypeIcon= ProductLineCommon.getTypeIconProperty(context, (String) strContextMap.get(ManufacturingPlanConstants.SELECT_TYPE));

		sb =  sb.append("\"\"")
		.append(strEndHrefTitle)
		.append("<img border=\'0\' src=\'../common/images/"+strTypeIcon+"\'/>")
		.append( strEndHref)
		.append(" ");
		//end IR-031126V6R2011 addded Type Icon to Context Object
		sb = sb
				.append(XSSUtil.encodeForXML(context, strContextMap.get(
						ManufacturingPlanConstants.SELECT_NAME).toString())
						+ " "
						+ XSSUtil.encodeForXML(context,(String)strContextMap
								.get(ManufacturingPlanConstants.SELECT_REVISION)));
		return sb.toString();
		//Added by KXB for 	IR-030541 Ends
	}
	/**
	 * This will be used in getting the context object icon name revison for
	 * property page
	 * 
	 * @param context
	 * @param args
	 *            use args to get objectid from request map.
	 * @return
	 * @throws Exception
	 */
	public String displayContextInPropertyPage(Context context, String[] args)
			throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String exportFormat = null;
  		boolean exportToExcel = false;
  		if(requestMap!=null && requestMap.containsKey("reportFormat")){
  			exportFormat = (String)requestMap.get("reportFormat");
  		}
  		if("CSV".equals(exportFormat)){
  			exportToExcel = true;
  		}

		// Gets the objectid manufacturing plan from args
		String strContextId = (String) requestMap.get("objectId");
		DomainObject objContext = new DomainObject(strContextId);
		String contextNameSelectable = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
				+ "].from.name";
		String contextRevisionSelectable = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
				+ "].from.revision";
		String contextTypeSelectable = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
				+ "].from.type";
		String contextIDSelectable = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
				+ "].from.id";
		StringList newList = new StringList();
		newList.addElement(contextNameSelectable);
		newList.addElement(contextRevisionSelectable);
		newList.addElement(contextTypeSelectable);
		newList.addElement(contextIDSelectable);

		Map strContextMap = objContext.getInfo(context, newList);
		StringBuffer sb = new StringBuffer();
		if (strContextMap.containsKey(contextNameSelectable)) {
			if(exportFormat!=null&&exportFormat.equalsIgnoreCase("CSV"))
			{
				sb.append(strContextMap.get(contextNameSelectable).toString()+ " "
						+ strContextMap.get(contextRevisionSelectable));
			}else {
			String strTemp = "<a TITLE=";
			String strEndHrefTitle = ">";
			String strEndHref = "</a>";
			sb = sb.append(strTemp);
			String strTypeIcon = ProductLineCommon.getTypeIconProperty(context,
					(String) strContextMap.get(contextTypeSelectable));

			sb = sb.append("\"\"").append(strEndHrefTitle).append(
					"<img border=\'0\' src=\'../common/images/" + strTypeIcon
							+ "\'/>").append(strEndHref).append(" ");
			sb.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
			sb.append((String) strContextMap.get(contextIDSelectable));
			sb.append("')\">");
			sb.append(XSSUtil.encodeForHTMLAttribute(context,strContextMap.get(contextNameSelectable).toString()) + " "
					+ strContextMap.get(contextRevisionSelectable));
			sb.append("</A>");
			}
		} else {
			sb.append("");
		}
		return sb.toString();
	}
	/**
	 * This will be used in getting the context object icon name revision for
	 * Search page
	 * 
	 * @param context
	 * @param args
	 *            use args to get objectid from request map.
	 * @return
	 * @throws Exception
	 */
	public StringList displayContextInSearchPage (Context context, String[] args)
	throws Exception {
		StringList ctxtList= new StringList();
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		StringList newList = new StringList();
		MapList objectList = (MapList) programMap.get("objectList");
		String contextNameSelectable = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
			+ "].from.name";
	    String contextRevisionSelectable = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
			+ "].from.revision";
	    String contextTypeSelectable = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
			+ "].from.type";
	    String contextIDSelectable = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
			+ "].from.id";
	    newList.addElement(contextNameSelectable);
		newList.addElement(contextRevisionSelectable);
		newList.addElement(contextTypeSelectable);
		newList.addElement(contextIDSelectable);
		Map tempMap;	
		String objNameRev = "";
		String objId = "";
		for (int iCnt = 0; iCnt < objectList.size(); iCnt++) {			
			tempMap = (Map) objectList.get(iCnt);
			//Modified for IR-038116V6R2011
			//if (tempMap.containsKey("id[parent]")){
				if (tempMap.containsKey("id")){
				//String strContextId=(String) tempMap.get("id[parent]");
					String planId=(String) tempMap.get("id");
				//DomainObject objContext =  new DomainObject(strContextId);
					DomainObject planObjectID =  new DomainObject(planId);
				//StringList newList = new StringList();
				//newList.addElement(ManufacturingPlanConstants.SELECT_NAME);
				//newList.addElement(ManufacturingPlanConstants.SELECT_REVISION);	
				//newList.addElement(ManufacturingPlanConstants.SELECT_TYPE);
				//Map strContextMap = objContext.getInfo(context, newList);
					Map strContextMap = planObjectID.getInfo(context, newList);
				//objNameRev = (String) strContextMap.get(ManufacturingPlanConstants.SELECT_NAME)
				//+ " " +(String)strContextMap.get(ManufacturingPlanConstants.SELECT_REVISION);
				objNameRev = (String) strContextMap.get(contextNameSelectable)
				+ " " +(String)strContextMap.get(contextRevisionSelectable);
				objId=(String) strContextMap.get(contextIDSelectable);
				StringBuffer sb = new StringBuffer();
				String strTemp = "<a TITLE=";
				String strEndHrefTitle = ">";
				String strEndHref = "</a>";
				sb =  sb.append(strTemp);
				//String strTypeIcon= ProductLineCommon.getTypeIconProperty(context, (String) strContextMap.get(ManufacturingPlanConstants.SELECT_TYPE));
				String strTypeIcon= ProductLineCommon.getTypeIconProperty(context, (String) strContextMap.get(contextTypeSelectable));
				sb =  sb.append("\"\"")
				.append(strEndHrefTitle)
				.append("<img border=\'0\' src=\'../common/images/"+strTypeIcon+"\'/>")
				.append( strEndHref)
				.append(" ");
				sb.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
				//sb.append(strContextId);
				sb.append(objId);
				sb.append("')\">");
				sb.append(XSSUtil.encodeForXML(context,objNameRev));
				sb.append("</A>");	              	
				ctxtList.add(sb.toString());
			}else
				ctxtList.add(DomainConstants.EMPTY_STRING);
		}
		return ctxtList;
	}
	/**
	 * This Methods is used to create Manufacturing Plan object
	 * with derived from relationship
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Archived Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 * @author WPK
	 * @since DMCPlanning R209
	 * @grade 0
	 */
	@com.matrixone.apps.framework.ui.PostProcessCallable
	public String createManufacturingPlan(Context context, String[] args)
			throws Exception {
		try {
			// TODO: License Check
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			MapList strManufacturingPlanId = null;
			String strObjectId = (String) paramMap.get("objectId");
			String strTitle = (String) requestMap.get("Title");
			String strPlannedForID = (String) requestMap.get("ContextOID");
			String strParentOID = (String) requestMap.get("parentOID");
			
			String strCopyObjId = (String) requestMap.get("copyObjectId");
			String strDerivationType = (String) requestMap.get("DerivationType");
			// START - Added for IR-039449V6R2011
			String strMode = (String) requestMap.get("mode");
		

			ManufacturingPlan manufacturingPlan = new ManufacturingPlan(strObjectId);
			DomainRelationship relAMP = manufacturingPlan.setContext(context,strPlannedForID);
			manufacturingPlan.connectManufacturingPlanMaster(context,strObjectId, strPlannedForID);

			com.matrixone.apps.dmcplanning.Product product = new com.matrixone.apps.dmcplanning.Product(strPlannedForID);
			MapList unresolvedDesignToImplementObjects = null;
			ManufacturingPlan mpBean = new ManufacturingPlan(strObjectId);
			unresolvedDesignToImplementObjects = product.editManufacturingPlanImplements(context,mpBean);

			return strObjectId;
		} catch (Exception e) {
			throw (new FrameworkException(e));
		}
	}
	
	/**
	 * This Methods is Access Program for Derived From Field in 
	 * Create Manufacturing Plan web form
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Archived Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 * @author WPK
	 * @since DMCPlanning R209
	 * @grade 0
	 */
	public boolean isDerivedFrom(Context context, String[] args)
	throws Exception {
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strDerivedFrom = (String)programMap.get("derivedFrom");
			if(strDerivedFrom.equalsIgnoreCase("true")){
				return true;
			}else{
				return false;
			}
		}catch(Exception e){
			e.printStackTrace();
			throw (new FrameworkException(e));
		}
	}
	/**
	 * This Methods is Used to display derived from field
	 * in Create Manufacturing Plan Web form
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Archived Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 * @author WPK
	 * @since DMCPlanning R209
	 * @grade 0
	 */
	public String displayDerivedFrom(Context context, String[] args)
	throws Exception {
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String strReturn = null;
			String strDerivedFrom				= (String)requestMap.get("copyObjectId");
			if (strDerivedFrom != null
					&& !strDerivedFrom.equals("")
					&& !strDerivedFrom.equals("null")){
				DomainObject objDerivedFrom = new DomainObject(strDerivedFrom);
				strReturn = objDerivedFrom.getInfo(context, ManufacturingPlanConstants.SELECT_NAME);
			}else{
				strReturn = "";
			}
			return strReturn;
		}catch(Exception e){
			e.printStackTrace();
			throw (new FrameworkException(e));
		}
	}

    /**
     * Used As Wrapper method for expandManufacturingPlanMatrixStructure method
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return MapList of all feature objects
     * @throws Exception if operation fails
     * @since CFP V6R2011
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList expandManufacturingPlanMatrix(Context context, String[] args)
            throws Exception {
    	MapList featureList = new MapList();
    	ManufacturingPlan manPlan = new ManufacturingPlan();
        featureList = manPlan.expandManufacturingPlanMatrixStructure(context, args);
        return featureList;
    }

    /**
      * This method is used to get all the Manufacturing Plans/Managed Revisions in the Product Context
      * Dynamic Column Behavior to show the Manufacturing Plans/Managed Revisions
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the FormBean contents
      * @return MapList - All the Manufacturing Plans with Managed Revisions
      * @throws Exception if the operation fails
      * @since CFP V6R2011
      */
     public MapList getManufacturingPlansWithManagedRevisions(Context context, String args[])throws Exception
     {
         HashSet hashParentList = new HashSet();
         HashMap hash  = (HashMap) JPO.unpackArgs(args);
         MapList objList = (MapList)hash.get("objectList");
         HashMap map = (HashMap)hash.get("requestMap");
         String selectIds = (String)map.get("selectId");         
         HashMap paramList = (HashMap) hash.get("requestMap");
 		 String ObjectId = (String)paramList.get("objectId");
		 //if calling from Model context, for selected MP, get the context Product ID,and Pass as Objectid
 		 DomainObject domObjId = new DomainObject(ObjectId);
		 if(domObjId.isKindOf(context, ManufacturingPlanConstants.TYPE_MODEL)){
			 String mpSelected="";
			 if (selectIds.endsWith(",")) {
				 int j = selectIds.lastIndexOf(",");
				 mpSelected= selectIds
				 .substring(0, j);
				 if(!mpSelected.trim().isEmpty()){
					 ManufacturingPlan mp =new ManufacturingPlan(mpSelected);
					 String contextIDSelectable = "to["
						 + ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
						 + "].from.id";
					 Map mpContext=mp.getManufacturingPlanContext(context);
					 ObjectId=(String)mpContext.get(contextIDSelectable);
				 }
			 }
		 }
         String language = context.getSession().getLanguage();
         //Modified for IR-044253V6R2011x STARTS
         String objType = "";
         int limit = -1;
         if(limit < 0) {
             limit = 32767;
         }
		 //Modified for IR-044253V6R2011x ENDS
         MapList returnMap = new MapList();
         try
         {
        	 MapList mapChildObjsList = null;
        	 //IR-030458 - KXB Starts
        	 StringBuffer stbObjWhere = new StringBuffer(50);
 	         stbObjWhere.append(DomainConstants.SELECT_POLICY);
 	         //	Modified for bug no. IR-042997V6R2011
 	         stbObjWhere.append("!=\"");
	 	     stbObjWhere.append(ManufacturingPlanConstants.POLICY_ARCHIVED);
	 	     stbObjWhere.append("\"");
	 	     //IR-030458 - KXB Ends
            
	 	     // Removed for loop for IR-110593V6R2012x 
	 	     	 String strProdId = ObjectId;
                 DomainObject domProdObj = new DomainObject(strProdId);
                 objType = domProdObj.getInfo(context,"type");
                 if(objType!=null)
                 {
                	 StringBuffer stbRelSelect = new StringBuffer(50);
                	 stbRelSelect.append(ManufacturingPlanConstants.QUERY_WILDCARD);
                     StringList selectStmts = new StringList(15);
                     selectStmts.add(DomainObject.SELECT_ID);
                     selectStmts.add(DomainObject.SELECT_NAME);
                     selectStmts.add(DomainObject.SELECT_REVISION);
                     StringList relSelects = new StringList(1);
                     StringBuffer stbTypeSelect = new StringBuffer(50);
                     stbTypeSelect = stbTypeSelect.append(ManufacturingPlanConstants.QUERY_WILDCARD);
 		             mapChildObjsList = domProdObj.getRelatedObjects(context,
	                        stbRelSelect.toString(), stbTypeSelect.toString(),
	                        selectStmts, relSelects, false, true, (short)1,
	                        stbObjWhere.toString(), null, (short)limit);
                 }
                 //Sorts the Group Header/Parent Manufacturing Plan Titles in Ascending Order
                 mapChildObjsList.sort(DomainObject.SELECT_NAME,"ascending","String");
                 hashParentList.add(mapChildObjsList);
            
             String strManPlan = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Column.ManufacturingPlan",language);
             String strManRev = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Column.ManagedRevision",language);
             String strManCheck = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Column.Check",language);//CFP R211
             
             boolean manPlanExists = false;
             Iterator itr = hashParentList.iterator();
             DomainObject manPlanObj = new DomainObject();
             while(itr.hasNext())
             {
                 MapList parentMapList = (MapList) itr.next();
                 for(int j=0;j<parentMapList.size();j++)
                 {
                     Map tempMap = (Map) parentMapList.get(j);
                     String strLevel = (String) tempMap.get("level");
                     String strRel = (String) tempMap.get("relationship");
                     String strName = (String) tempMap.get("name");
                     String strManPlanId = (String) tempMap.get("id");
                     DomainObject dmoManPlan = new DomainObject(strManPlanId);
                     String strMPIntent = dmoManPlan.getAttributeValue(context, ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_INTENT);
	    	         if(strLevel.equalsIgnoreCase("1") && strRel.equalsIgnoreCase(ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN))
	    	         {
    	            	 manPlanExists = true;
    	            	 manPlanObj.setId(strManPlanId);
    	            	 String manPlanTitle = manPlanObj.getAttributeValue(context, ManufacturingPlanConstants.ATTRIBUTE_TITLE) + " " + manPlanObj.getRevision();
	    	             if(selectIds.contains(strManPlanId)){
	    	            	 for(int k=0;k<3;k++)
	    	            	 {
	    	                     HashMap hashfornewColumn = new HashMap();
	    	                     HashMap hashColumnSetting = new HashMap();
	    	                     hashColumnSetting.put("Column Type","programHTMLOutput");
	    	                     hashColumnSetting.put("Registered Suite","DMCPlanning");
	    	                     hashColumnSetting.put("Export", "true");
	    	                     if(k==2){
	    	                    	 hashColumnSetting.put("function", "getManufacturingPlanColumnValues");
	    	                     }
	    	                     else if(k==1){
	    	                    	 hashColumnSetting.put("function", "getCheckColumnForManufacturingPlanMatrix");
	    	                     }else{
	    	                    	 hashColumnSetting.put("function", "getManagedRevisionColumnValues");
	    	                     }
	    	                     hashColumnSetting.put("program", "ManufacturingPlan");
	    	                     hashColumnSetting.put("Group Header",manPlanTitle);
	    	                     hashColumnSetting.put("Width","160");
	    	                     //Setting the global Column with the above settings
	    	                     hashfornewColumn.put("settings",hashColumnSetting);
	    	                     //To get split columns - Managed Revision and Manufacturing Plan
	    	                     if(k==2){
	    	                        hashfornewColumn.put("label",strManPlan);
	    	                     }else if (k==1){
	    	                        hashfornewColumn.put("label",strManCheck);
	    	                     }else{
	    	                        hashfornewColumn.put("label",strManRev);
	    	                     }
	    	                     hashfornewColumn.put("name","mp"+strName);
	    	                     hashfornewColumn.put("id",strManPlanId);
	    	                     returnMap.add(hashfornewColumn);
	    	            	 }
	    	              }else if(selectIds.isEmpty()) {
	    	            	 for(int k=0;k<3;k++){
	    	                     HashMap hashfornewColumn = new HashMap();
	    	                     HashMap hashColumnSetting = new HashMap();
	    	                     hashColumnSetting.put("Column Type","programHTMLOutput");
	    	                     hashColumnSetting.put("Registered Suite","Configuration");
	    	                     hashColumnSetting.put("Export", "true");
	    	                     if(k==2){
	    	                    	 hashColumnSetting.put("function", "getManufacturingPlanColumnValues");
	    	                     }
	    	                     else if(k==1){
	    	                    	 hashColumnSetting.put("function", "getCheckColumnForManufacturingPlanMatrix");
	    	                     }else{
	    	                    	 hashColumnSetting.put("function", "getManagedRevisionColumnValues");
	    	                     }
	    	                     hashColumnSetting.put("program", "ManufacturingPlan");
	    	                     hashColumnSetting.put("Group Header",manPlanTitle);
	    	                     hashColumnSetting.put("Width","160");
	    	                     //Setting the global Column with the above settings
	    	                     hashfornewColumn.put("settings",hashColumnSetting);
	    	                     //To get split columns - Managed Revision and Manufacturing Plan
	    	                     if(k==2){
		    	                        hashfornewColumn.put("label",strManPlan);
		    	                     }else if (k==1){
		    	                        hashfornewColumn.put("label",strManCheck);
		    	                     }else{
		    	                        hashfornewColumn.put("label",strManRev);
		    	                     }
	    	                     hashfornewColumn.put("name","mp"+strName);
	    	                     hashfornewColumn.put("id",strManPlanId);
	    	                     returnMap.add(hashfornewColumn);
	    	            	 }
	    	             }
	    	         }
                 }
             }
             if(!manPlanExists){
            	 	 returnMap = null;
                     String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Error.NoManufacturingPlans",language);
                     emxContextUtil_mxJPO.mqlNotice(context,strAlertMessage);
                     throw new FrameworkException(strAlertMessage);
             }
         }
         catch(Exception e)
         {
             throw e;
         }finally
         {
             return returnMap;
         }
     }

     /**
       * This method is used to get all the Managed Revisions(Technical Features)for display in
       * Managed Revision column(programHTMLOutput)of the View Manufacturing Plan Matrix
       *
       * @param context the eMatrix <code>Context</code> object
       * @param args holds the program Map contents
       * @return List - All the Managed Revisions
       * @throws Exception if the operation fails
       * @since CFP V6R2011
       */
     public List getManagedRevisionColumnValues(Context context, String[] args)throws Exception
     {
    	 matrix.util.List manRevList = new StringList();

    	 try
    	 {
    		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
    		 MapList objectList = (MapList) programMap.get("objectList");
    		 HashMap columnMap = (HashMap) programMap.get("columnMap");
    		 String strGrpHeaderId = (String) columnMap.get("id");
    		 HashMap paramMap = (HashMap) programMap.get("paramList");

    		 String exportFormat="";
    		 if(paramMap!=null){
    			 exportFormat = (String)paramMap.get("exportFormat");
    		 }
    		 String strTemp = "<a TITLE=";
    		 String strEndHrefTitle = ">";
    		 String strEndHref = "</a>";
    		 String sTypeIcon ="";
    		 String language = context.getSession().getLanguage();
    		 String strStatus = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Table.Status",language); 
    		 String strTypeIcon = EnoviaResourceBundle.getProperty(context,"emxFramework.smallIcon.type_Product");

    		 StringList mpSelects= new StringList(DomainObject.SELECT_NAME);
    		 mpSelects.addElement(DomainObject.SELECT_REVISION);

    		 DomainObject domMP=new DomainObject(strGrpHeaderId);
    		 //get the Product ids which are connected with MPI
    		 String strObjectSelect= "from["+ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_IMPLEMENTS+"].to.id";

    		 StringList slMPIs=domMP.getInfoList(context, strObjectSelect);

    		 for (int i = 0; i < objectList.size(); i++){
    			 Map tempMap = (Map) objectList.get(i);
    			 Boolean model = (Boolean)tempMap.get("Model");
    			 String strRootNode = (String)tempMap.get("Root Node");
    			 if((model != null && model)){
    				 String strMastFeatId = (String)tempMap.get(DomainConstants.SELECT_ID);
    				 Model modelBean = new Model(strMastFeatId);
    				 List slParentProductId = modelBean.getManagedRevisions(context);
    				 boolean isUndefined=true;
    				 String plnedForID="";
    				 for(int m=0;m<slParentProductId.size();m++){
    					 String strObjID=(String)slParentProductId.get(m);
    					 if(slMPIs.contains(strObjID)){
    						 isUndefined=false;
    						 plnedForID=strObjID;
    						 break;
    					 }
    				 }
    				 if(isUndefined){
    					 manRevList.add(strStatus);
    				 }
    				 else{
    					 DomainObject dom= new DomainObject(plnedForID);
    					 Map mp=dom.getInfo(context,mpSelects );
    					 String strNonEncodedPFName = (String) mp
    					 .get(DomainObject.SELECT_NAME)
    					 + " "
    					 + (String) mp.get(DomainObject.SELECT_REVISION);
    					 String strEncodedPFName = XSSUtil.encodeForXML(context,
    							 (String) mp.get(DomainObject.SELECT_NAME))
    							 + " "
    							 + XSSUtil.encodeForXML(context, (String) mp
    									 .get(DomainObject.SELECT_REVISION));
    					 StringBuffer sb = new StringBuffer();
    					 if((exportFormat != null)
    							 && (exportFormat.length() > 0)
    							 && ("CSV".equals(exportFormat))){
    						 sb.append(strNonEncodedPFName);
    					 }else{
    						 sb =  sb.append(strTemp);
    						 sb =  sb.append("\"\"")
    						 .append(strEndHrefTitle)
    						 .append("<img border=\'0\' src=\'../common/images/"+strTypeIcon+"\'/>")
    						 .append( strEndHref)
    						 .append(" ");
    						 sb.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
    						 sb.append(plnedForID);
    						 sb.append("')\">");
    						 sb.append(strEncodedPFName);
    						 sb.append("</A>");
    					 }
    					 manRevList.add(sb.toString());
    				 }
    			 }else{
    				 if(strRootNode!=null && strRootNode.equalsIgnoreCase("true")){
    					 manRevList.add(" ");
    				 } 
    			 }
    		 }
    	 }
    	 catch (Exception e) {
    		 throw new FrameworkException(e.getMessage());
    	 }
    	 return manRevList;
     }

     /**
      * This method is used to get all the Manufacturing Plans for display in
      * Manufacturing Plan column(programHTMLOutput)of the View Manufacturing Plan Matrix
      *
      * @param context the eMatrix <code>Context</code> object
      * @param args holds the program Map contents
      * @return List - All the Manufacturing Plans
      * @throws Exception if the operation fails
      * @since CFP V6R2011
      */
     public List getManufacturingPlanColumnValues(Context context, String[] args)throws Exception
     {
         matrix.util.List manPlanList = new StringList();
         try{
        	 ManufacturingPlan manPlan = new ManufacturingPlan();
             HashMap programMap = (HashMap) JPO.unpackArgs(args);
             MapList objectList = (MapList) programMap.get("objectList");
             HashMap columnMap = (HashMap) programMap.get("columnMap");
             Map settings = (Map) columnMap.get("settings");
             String strGrpHeader = (String) settings.get("Group Header Name");
             String strGrpHeaderId = (String) columnMap.get("id");
             String strGrpHeaderTitle = (String) settings.get("Group Header");
            
             objectList = getProductStructureForRetrofit(context,strGrpHeaderId, objectList);
             //Added for R211
             //Added for IR-078199V6R2012
             String exportFormat="";
             HashMap paramList = (HashMap) programMap.get("paramList"); 
             if(paramList!=null){
              exportFormat = (String)paramList.get("exportFormat");
             }
             String strStartLeft="<left>";
             String strEndLeft="</left>";
             //End of IR-078199V6R2012
             String strStartTempTable = "<table>";
             String strStartTempTableRow = "<tr>";
             String strStartTempTableData = "<td>";
             String strEndTempTableData = "</td>";
             String strEndTempTableRow = "</tr>";
             String strEndTempTable = "</table>";
             String strTempAnchor = "<a TITLE=";
             String strEndHrefTitle = ">";
             String strEndHref = "</a>";
            
             String language = context.getSession().getLanguage();
             String strHTMLTitle = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Error.Validate",language);
             //Added by KXB for Internationalization STARTS
             String strStatus = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Table.Status",language);
             //Added by KXB for Internationalization ENDS
             String strTypeIcon = EnoviaResourceBundle.getProperty(context,"emxFramework.smallIcon.type_ManufacturingPlan");
             Map objMap = new HashMap();
             //Loop thru the objectList obtained from the expand program(Master Feature/Technical Feature List)
             for (int i = 0; i < objectList.size(); i++)
             {
                 Map tempMap = (Map) objectList.get(i);
            	 Boolean masterfeature = (Boolean)tempMap.get("MasterFeature");
            	 Boolean model = (Boolean)tempMap.get("Model");
            	 StringList manPlanParlist = new StringList(10);
            	 StringList ftrList = new StringList(10);
            	 String mPlanTitle ="";
            	 List mPlanTitleList = new ArrayList();//Added for R211
            	 List objectIdList = new ArrayList();
                 String objectId ="";
            	 String strParentId = (String)tempMap.get("id[parent]");
            	 String strRootNode = (String)tempMap.get("Root Node");
            	 if((masterfeature != null && masterfeature) || (model != null && model))
            	 {
            		 StringList Pl = new StringList(10);
            		 int manRevsize = Integer.parseInt(tempMap.get("size").toString());
            		 //Get a list of all the Parent Manufacturing Plans for the manRevsize and put them to a StringList
            		 for (int j = 0; j < manRevsize; j++)
            		 {
            			 objMap = (HashMap)tempMap.get("k"+j);
            			 Object mPlanParId = objMap.get("ManufacturingPlanParentId");
            			 if(mPlanParId instanceof StringList){
     		             	StringList slManPlanObj = (StringList)mPlanParId;	 		             	
     		             	for(int l=0;l<slManPlanObj.size();l++){
    	 		             	manPlanParlist.add(slManPlanObj.get(l));
     		             	}
     		             }else if(mPlanParId instanceof String){
     		             	String slManPlanObj = (String)mPlanParId;
     		             	manPlanParlist.add(slManPlanObj);
     		             }	        		
            			 ftrList.add(objMap);
            		 }
            		 Map fMap = new HashMap();
            		 //The context strGrpHeaderId should be present in the Parent Manufacturing Plans list.
            		 //Only if it contains,display the MP
        			 if(manPlanParlist.contains(strGrpHeaderId))
        			 {
        				 StringList sLFeatParentId = null;
        				 for(int h=0;h<ftrList.size();h++)
        				 {
        					
        					//manPlanlist = new StringList();
         					fMap = (HashMap)ftrList.get(h);
         					Object mPlanIds = (Object)fMap.get("ManufacturingPlanId");
         					Object mPParentId = fMap.get("ManufacturingPlanParentId");
    	   	        			 if(mPParentId instanceof StringList){
    	   	 		             	StringList sLMPParentIds = (StringList)mPParentId;
    	   	 		             	for(int l=0;l<sLMPParentIds.size();l++){
    	   	 		             		Pl.add(sLMPParentIds.get(l));
    	   	 		             	}
    	   	 		             }else if(mPParentId instanceof String){
    	   	 		             	String sLMPParentIds = (String)mPParentId;
    	   	 		             	Pl.add(sLMPParentIds);
    	   	 		             }
    	   	        			 
    	   	        		   //Added for IR-030712V6R2011
    	   	        		   //To Check Feat is included in the Prodt struct or not.
    	    					sLFeatParentId = (StringList)fMap.get("FeatureParentId");
    	   	        			 
    	          		    //Modified for R211	
         					String strNameObj ="";
         					StringList strNameObjList = new StringList();
         					if(mPlanIds!=null &&Pl.contains(strGrpHeaderId) )
         					{
         						if(!sLFeatParentId.isEmpty() && sLFeatParentId.contains(strParentId))
    	    						{
         							strNameObjList = manPlan.whereUsedManufacturingPlan(context,mPlanIds,strGrpHeaderId);
    	    						}
         					}
         					Iterator nameItr=strNameObjList.iterator();
         					while(nameItr.hasNext()){
         						strNameObj=(String) nameItr.next();
         					if(strNameObj != null && !strNameObj.equals(""))
     	             		{
     	             			objectId = strNameObj.toString().substring(strNameObj.toString().lastIndexOf("|")+1, strNameObj.toString().length());
     	             			mPlanTitle = strNameObj.toString().substring(0, strNameObj.toString().lastIndexOf("|"));
     	             			objectIdList.add(objectId);
     	             			mPlanTitleList.add(mPlanTitle);
         					}
        					 
           				 }
        				 }
        				    StringBuffer sb = new StringBuffer();
        				  	if(!(objectIdList.isEmpty()&&mPlanTitleList.isEmpty())){
        				  		Iterator objectIdListItr=objectIdList.iterator();
        				  		Iterator mPlanTitleListItr=mPlanTitleList.iterator();

    						 if((exportFormat != null)
    								 && (exportFormat.length() > 0)
    								 && ("CSV".equals(exportFormat))){
        				  		boolean bisFirst = true;
        				  	while(objectIdListItr.hasNext()&& mPlanTitleListItr.hasNext()){
    								 mPlanTitle=(String)mPlanTitleListItr.next();
    								 if(!bisFirst)
    									 sb =  sb.append(" | ");
    								 bisFirst = false;
    								 sb.append(mPlanTitle);
    							 }
    						 }else{
    							 sb =  sb.append(strStartLeft);//Added for IR-078199V6R2012
    							 sb =  sb.append(strStartTempTable);

    							 while(objectIdListItr.hasNext()&& mPlanTitleListItr.hasNext()){
        				  		sb =  sb.append(strStartTempTableRow);
        				  		sb =  sb.append(strStartTempTableData);
        				  		objectId=(String) objectIdListItr.next();
        				  		mPlanTitle=(String)mPlanTitleListItr.next();
        	                 sb =  sb.append(strTempAnchor);
        	                 sb =  sb.append("\"\"")
        	                         .append(strEndHrefTitle)
        	                         .append("<img border=\'0\' src=\'../common/images/"+strTypeIcon+"\'/>")
        	                         .append( strEndHref)
        	                 		 .append(" ");
        	                 //Modified for IR-078199V6R2012
        	                 if(!"HTML".equalsIgnoreCase(exportFormat)){
        	                 sb.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
        	                 sb.append(objectId);
        	                 sb.append("')\">");
        	                 }
    		                 //IR-043838V6R2011 escaped & and < character
        	                 //mPlanTitle = mPlanTitle.replaceAll("&", "&amp;");
    		                 mPlanTitle = mPlanTitle.replaceAll("<", "&lt;");
        	                 sb.append(XSSUtil.encodeForXML(context,mPlanTitle));

        	                 
        	                 if(!"HTML".equalsIgnoreCase(exportFormat)){
        	                 sb.append("</A>");
        	                 }
        	                 //End of IR-078199V6R2012
        	            sb = sb.append(strEndTempTableData);
								sb = sb.append(strEndTempTableRow);
							}
							sb = sb.append(strEndTempTable);
							
							sb =  sb.append(strEndLeft);//Added for IR-078199V6R2012
    						 }
							manPlanList.add(sb.toString());
						} else {
							manPlanList.add(strStatus);
						}
					} else {
						manPlanList.add(strStatus);
					}
				}// Else loop to take care of the Technical Features having no
					// Master Feature
    	          else
    	          {
    		             boolean stringListBool = false;
    		             boolean stringBool = false;
    		             Object manPlanObj = tempMap.get("Manufacturing Plan Parent");
    		             Object strNameObj = tempMap.get("Manufacturing Plan Id");
    		             Boolean bIsFeatInStruct =(Boolean)tempMap.get("Is Feature in Structure");
    		             //String strCntxtParMP =(String)tempMap.get("Context Parent MP");
    		             
    		             if(bIsFeatInStruct == null && Boolean.parseBoolean(strRootNode))
    		             {
    		            	 bIsFeatInStruct = true;
    		             }
    		             //strNameObj values could be StringList/String,in case it's a StringList,then use whereUsedManufacturingPlan
    		             //method to identify which among the child Manufacturing Plans belongs to the parent
    		             //Manufacturing Plan(context Product's Manufacturing Plans)
    		             if(manPlanObj instanceof StringList)
    		             {
    		             	StringList slManPlanObj = (StringList)manPlanObj;
    		             	stringListBool = slManPlanObj.contains(strGrpHeader);
    		             	if(strGrpHeaderId!= null && !strGrpHeaderId.equalsIgnoreCase(""))
    		             	{
    		             		strNameObj = manPlan.whereUsedManufacturingPlan(context,strNameObj,strGrpHeaderId);
    		             		if(strNameObj != null && !strNameObj.equals(""))
    		             		{
    		             			objectId = strNameObj.toString().substring(strNameObj.toString().lastIndexOf("|")+1, strNameObj.toString().length());
    		             			strNameObj = strNameObj.toString().substring(0, strNameObj.toString().lastIndexOf("|"));
    		             		}
    		             	}
    		             }else if(manPlanObj instanceof String)
    		             {
    		             	String slManPlanObj = (String)manPlanObj;
    		             	stringBool = slManPlanObj.equalsIgnoreCase(strGrpHeader);	
    		             	//If there are more than 1 Manufacturing Plans connected to the technical feature then strNameObj will be
    		             	//instance of StringList 
    		             	if(strNameObj instanceof StringList){
    		             		strNameObj = manPlan.whereUsedManufacturingPlan(context,strNameObj,strGrpHeaderId);
    		             		if(strNameObj != null && !strNameObj.equals(""))
    		             		{
    		             			objectId = strNameObj.toString().substring(strNameObj.toString().lastIndexOf("|")+1, strNameObj.toString().length());
    		             			strNameObj = strNameObj.toString().substring(0, strNameObj.toString().lastIndexOf("|"));
    		             		}	             		
    		             	}else{	             	
    			             	strNameObj = tempMap.get("Manufacturing Plan Title");
    			             	objectId = (String)tempMap.get("Manufacturing Plan Id");
    		             	}
    		             }
    		             int strNewLevel = Integer.parseInt((String)tempMap.get("level")) ;
    		             //To show the Manufacturing Plan values,stringListBool or stringBool needs to be true
    		             if(strNameObj != null && !strNameObj.toString().equals("")&& (stringListBool||stringBool))
    		             {
    		             	if(bIsFeatInStruct)
    		             	{
    		             		StringBuffer sb = new StringBuffer();
    		             		if((exportFormat != null)
    		             				&& (exportFormat.length() > 0)
    		             				&& ("CSV".equals(exportFormat))){
    		             			sb.append(strNameObj.toString());
    		             		}else{
       		                 sb =  sb.append(strTempAnchor);
       		                 sb =  sb.append("\"\"")
       		                         .append(strEndHrefTitle)
       		                         .append("<img border=\'0\' src=\'../common/images/"+strTypeIcon+"\'/>")
       		                         .append( strEndHref)
       		                 		.append(" ");
       		                 sb.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
       		                 sb.append(objectId);
       		                 sb.append("')\">");
       		                 sb.append(XSSUtil.encodeForXML(context,strNameObj.toString()));
       		                 sb.append("</A>");
    		             		}
       		                 manPlanList.add(sb.toString());
    		             	}
    		             	else
    		             	{
    		             		 manPlanList.add(strStatus);
    		             	}
    		             }
    		             else if(strNewLevel>0 || strNameObj!= null && strNameObj.equals(""))
    		             {
    		            		 manPlanList.add(strStatus);
    		             }
    		             if(strNewLevel==0)
    		             {
    		            	 StringBuffer sb = new StringBuffer();
    		            	 if((exportFormat != null)
    		            			 && (exportFormat.length() > 0)
    		            			 && ("CSV".equals(exportFormat))){
    		            		 sb.append(strGrpHeaderTitle);
    		            	 }else{
    		                 sb =  sb.append(strTempAnchor);
    		                 sb =  sb.append("\"\"")
    		                         .append(strEndHrefTitle)
    		                         .append("<img border=\'0\' src=\'../common/images/"+strTypeIcon+"\'/>")
    		                         .append( strEndHref)
    		                 		.append(" ");
    		                 sb.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
    		                 sb.append(strGrpHeaderId);
    		                 sb.append("')\">");
    		                 //IR-043838V6R2011 escaped & and < character
    		                 //strGrpHeaderTitle = strGrpHeaderTitle.replaceAll("&", "&amp;");
    		                 //strGrpHeaderTitle = strGrpHeaderTitle.replaceAll("<", "&lt;");
    		            		 sb.append(XSSUtil.encodeForXML(context,strGrpHeaderTitle));
    		                 sb.append("</A>");
    		            	 }
    		                 manPlanList.add(sb.toString());
    		             }
    		          }
             }
         }
         catch (Exception e) {
        	 // TODO: handle exception
        	// e.printStackTrace();
		}
         return manPlanList;
     }



    /**
     * This Method is used to get the Technical Structure of the selected Product.
     *
	 * @param context - eMatrix Context Object
	 * @param String[]
     *            The args .
	 *
     * @throws Exception if the operation fails
     * @author IXH
     * @since DMCPlanning R209
     */

	public MapList getTechnicalStructure(Context context, String[] args)throws Exception {
		MapList  mpFeatAndMPIds = new MapList();
		try{
			//Unpacking the args
	        HashMap programMap = (HashMap)JPO.unpackArgs(args);

	        mpFeatAndMPIds =(MapList) com.matrixone.apps.dmcplanning.ManufacturingPlan.getTechnicalStructure(context,args);

		}catch (Exception e) {
			e.printStackTrace();
		}
		  return mpFeatAndMPIds;

	}

	/**
     * This is method is used  to retrieve the "Feature Allocation Type" attribute value of
     * Master Feature of the  Technical feature if present.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the following arguments 0 - HashMap containing the
     *            following arguments
     * @return Object - MapList containing the id of Master Feature if it is present for the given Technical Feature
     * @throws Exception
     *             if the operation fails
     * @since R209
     * @author IXH
     */

	public Vector getFeatureAllocationTypeValueForFeature(Context context , String[] args )throws Exception {

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList lstObjectIdsList = (MapList) programMap.get("objectList");
		Vector vFATValues = new Vector(lstObjectIdsList.size());
		HashMap paramList =(HashMap) programMap.get("paramList");
		String parentOID = (String) paramList.get("parentOID");
	    String languageStr = context.getSession().getLanguage();
		try
		{
			//Modified for IR-078307V6R2012
			//String strFeatId = DomainConstants.EMPTY_STRING;
	        HashMap paramMap = (HashMap) programMap.get("paramList");
	        String strParentId = (String) paramMap.get("parentOID");
	        Map tempMap = null;
	        String strFATValue ="";
	        String strFeatureUsageDisplay="";
	        String strFeatureUsage = "emxFramework.Range.FeatureUsage.";
              	// Changed the code to fix the performance issue - IR-044262V6R2011 
                // StringList strPRDPFLIDs = new StringList();
                //	    if(strParentId!=null && !strParentId.equals("")){
                //	        DomainObject domProduct = new DomainObject(strParentId);
                //	        strPRDPFLIDs = domProduct.getInfoList(context,"from["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].id");	        	
                //	        }
			if(lstObjectIdsList.size() != 0)
			{
				for (int iCnt = 0 ; iCnt < lstObjectIdsList.size();iCnt++ )
				{
					tempMap = (Map) lstObjectIdsList.get(iCnt);
					// getting the context parent ids (Feature and Product)
	                //strFeatId = (String) tempMap.get("id");
	                String strRootNode = (String) tempMap.get("Root Node");
	               // DomainObject domParentId = new DomainObject(strParentId);
	                String objectId=(String)tempMap.get(DomainConstants.SELECT_ID);
	                DomainObject domParentId = new DomainObject(objectId);
	                

	                String strType = domParentId.getInfo(context, DomainConstants.SELECT_TYPE);
	                if (mxType.isOfParentType(context, strType,ManufacturingPlanConstants.TYPE_LOGICAL_FEATURE)
	                		|| mxType.isOfParentType(context, strType,ManufacturingPlanConstants.TYPE_PRODUCTS)
	                		&& (strRootNode == null || (strRootNode.equals("")) || ("null".equalsIgnoreCase(strRootNode))))
	                {
	                	// Changed the code to fix the performance issue - IR-044262V6R2011
	                	String strFATAttribute = "to["+ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES+"].tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].attribute["+ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]";
	                	String parentIdExp= "to["+ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES+"].tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id";
	                	Object featAllocation = (Object)tempMap.get(strFATAttribute);
	                	StringList strParentExp = new StringList();
	                    if(featAllocation instanceof StringList)
	                    {
	                    	strParentExp = (StringList) tempMap.get(parentIdExp);
	                    	int index=strParentExp.indexOf(parentOID);
	                    	strFATValue = (String) ((StringList) featAllocation).get(index);
	                        strFeatureUsageDisplay = EnoviaResourceBundle.getProperty(context, "Configuration",strFeatureUsage+strFATValue,
	                                   languageStr);
	                    	
                     //	for(int k=0;k<strParentExp.size();k++){
                     //	String strParentExpId = (String)strParentExp.get(k);
                     //	 if(strParentExpId.contains(strPFLID)){
                     //	 	 DomainRelationship  domrelPFLId = new DomainRelationship(strPFLID);
                     //		 strFATValue = domrelPFLId.getAttributeValue(context,ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE);
                     //		 break;
                     //	       }
                     //	  }
	                    }else if(featAllocation instanceof String)
	                    {
                         //	 String strPFL = (String) tempMap.get("from["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].id");
                         //  DomainRelationship  domrelPFLId = new DomainRelationship(strPFL);
                           strFATValue = (String) featAllocation;
	                       strFeatureUsageDisplay = EnoviaResourceBundle.getProperty(context, "Configuration",
	                                   strFeatureUsage+strFATValue,languageStr);
	                    }
                	    vFATValues.add(strFeatureUsageDisplay);
	                 }
	                 else
	                 {
	                	vFATValues.add(" ");
	                 }
				  }
	         	}
	            else
	    		{
	    			vFATValues.add(" ");
	    		}
		}
		catch (Exception e) {
			// TODO: handle exception
			//e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
				return vFATValues ;
 }

	/**
     * This is method is used  to retrieve the "Name" if object is Manufacturing Plan
     * or else display blank in cell value.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the following arguments 0 - HashMap containing the
     *            following arguments
     * @return Vector - Containing the "Name" value for Manufacturing Plans in Structure
     * @throws Exception
     *             if the operation fails
     * @since R209
     * @author IXH
     */

	public Vector getName(Context context , String[] args) throws Exception{
        //XSSOK
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		MapList lstObjectIdsList =  (MapList)programMap.get("objectList");
		Vector vNames = new Vector(lstObjectIdsList.size());

		String exportFormat = null;
  		boolean exportToExcel = false;
  		HashMap requestMap = (HashMap)programMap.get("paramList");
  		if(requestMap!=null && requestMap.containsKey("reportFormat")){
  			exportFormat = (String)requestMap.get("reportFormat");
  		}
  		if("CSV".equals(exportFormat)){
  			exportToExcel = true;
  		}

		try{

	        Map tempMap;

	        StringBuffer sb;
	        String strStartHrefTitle = "<a TITLE=\"";
	        String strEndTitle = "\">";
	        String strStartImage = "<img src=";
	        String strImageURL = "";
	        String strImageEnd = " />";
	        String strEndHref = "</a>";
	        strImageURL = "\"../common/images/iconSmallManufacturingPlan.gif\" border=\"0\"";

	        for(int i=0; i<lstObjectIdsList.size();i++)
	        {

	            sb = new StringBuffer();
	            sb = sb.append(strStartHrefTitle);

	            tempMap = (Map)lstObjectIdsList.get(i);
	            String strType = (String)tempMap.get(DomainConstants.SELECT_TYPE);

	            if(strType == null)
	            {
	            	String objId = (String) tempMap.get(DomainConstants.SELECT_ID);
	            	DomainObject domFeaId = new DomainObject(objId);
	            	// Code modified to fix IR IR-034825V6R2011 by IVU
	            	StringList strSelectables = new StringList(2);
	            	strSelectables.addElement(DomainConstants.SELECT_TYPE);
	            	strSelectables.addElement(DomainConstants.SELECT_NAME);
	            	Map mapObjDetails = domFeaId.getInfo(context, strSelectables);
	            	strType = (String)mapObjDetails.get(DomainConstants.SELECT_TYPE);
	            	tempMap.put(DomainConstants.SELECT_NAME,(String)mapObjDetails.get(DomainConstants.SELECT_NAME));
	            	// Code modified to fix IR IR-034825V6R2011 by IVU	            	
	            } 
	            // Modified for Bug No. IR-042997V6R2011
	            if(mxType.isOfParentType(context, strType,ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN))
	            {
		            String strMPNameValue = (String)tempMap.get(DomainConstants.SELECT_NAME);

		            if(exportToExcel){
		            	vNames.add(strMPNameValue);
		            }
		            else{

		            sb = sb.append(XSSUtil.encodeForXML(context,strMPNameValue))
		                    .append(strEndTitle)
		                    .append(strStartImage)
		                    .append(strImageURL)
		                    .append(strImageEnd)
		                    .append(XSSUtil.encodeForXML(context,strMPNameValue))
		                    .append(strEndHref);


		            vNames.add(sb.toString());
	            }
	            }
	            else
	            {
	            	vNames.add("");
	            }
	        }
		}
		catch (Exception e)
		{
			// TODO: handle exception
			e.printStackTrace();
		}
		return vNames ;
	}

	/**
     * This is method is used  to retrieve the current State value if object is Manufacturing Plan
     * or else display blank in cell value.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the following arguments 0 - HashMap containing the
     *            following arguments
     * @return Vector - Containing the current State value for Manufacturing Plans in Structure
     * @throws Exception
     *             if the operation fails
     * @since R209
     * @author IXH
     */

	public Vector getState(Context context , String[] args) throws Exception{
		//XSSOK
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		MapList objectList =  (MapList)programMap.get("objectList");
		Vector vStates = new Vector(objectList.size());
        try{
    		String strObjId = "";
    		String strRelType = "";
    		HashMap ctxCGInfo = null ;
    		String strState = null;
    		String i18strState = null;
    		StringBuffer sbBuffer  = new StringBuffer(400);


    		for ( int i = 0; i <  objectList.size() ; i++ )
    		{
    			Map tempMap = new HashMap();
    			tempMap = (Map)objectList.get(i);

    			String strType = (String)tempMap.get("type");

    			if(strType == null)
				{
					String strNewObjId = (String)tempMap.get("id");
					DomainObject domNewObjId = new DomainObject(strNewObjId);
					strType = domNewObjId.getInfo(context,DomainConstants.SELECT_TYPE);
				}
    			// Modified for Bug No. IR-042997V6R2011
	            if(mxType.isOfParentType(context, strType,ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN)){
    				strState = (String)tempMap.get("current");
    				//Added for IR-078307V6R2012
    				i18strState=i18nNow.getStateI18NString(ManufacturingPlanConstants.POLICY_MANUFACTURING_PLAN, strState, context.getSession()
                            .getLanguage());
    				//End of IR-078307V6R2012
    			}
    			else{
    	            //Modified for IR-078307V6R2012
    				//strState = " ";
    				i18strState = " ";
    			}
	            //Modified for IR-078307V6R2012
	            // vStates.add(strState);
    			vStates.add(i18strState);
    		}
        }
        catch (Exception e) {
			// TODO: handle exception
        	e.printStackTrace();
		}

		return vStates ;
	}

	/**
     * This is connection program .It is called on "Apply" in Edit Page
     * In this MP is connected /removed from Technical Feature depending on the Markup.
     * @param context
     *            the eMatrix <code>Context</code> object
     * @param args -
     *            Holds the following arguments 0 - HashMap containing the
     *            following arguments
     * * @throws Exception
     *             if the operation fails
     * @since R209
     * @author IXH
     */

	//Connect MP to Feature
	 @com.matrixone.apps.framework.ui.ConnectionProgramCallable
	 public Map connectManufacturingPlan(Context context, String[] args)throws Exception
	 {
		 HashMap doc = new HashMap();
		 HashMap adddoc = new HashMap();
         HashMap cutdoc = new HashMap();

		 try{
			 HashMap programMap = (HashMap) JPO.unpackArgs(args);
			 //Selected Feature's Id
			 String strSelectedObjId = (String) programMap.get("parentOID");

			 HashMap paramMap = (HashMap) programMap.get("paramMap");
			 //Context Product Id
			 String strRootObjectId = (String) paramMap.get("objectId");
			 //Selected MP Id
			 String strselObjId = (String) paramMap.get("selObjId");
             //R211 Modifications
			 //HashMap addReturnMap = new HashMap();
			 //HashMap cutReturnMap = new HashMap();
			 StringList preferredList=new StringList();
			 
	         MapList mlAddItems = new MapList();
	         MapList mlCutItems = new MapList();
	         


			 Element elem = (Element) programMap.get("contextData");
		     MapList chgRowsMapList = com.matrixone.apps.framework.ui.UITableIndented
		                              .getChangedRowsMapFromElement(context, elem);

             StringList sLAddMPIDs = new StringList();
             StringList sLRemMPRelIDs = new StringList();
		     for(int i=0 ;i<chgRowsMapList.size() ;i++)
		     {
		            HashMap tempMap = (HashMap) chgRowsMapList.get(i);
		            //MP object to be added
		            String childObjId = (String) tempMap.get("childObjectId");
		            String markUpMode = (String) tempMap.get("markup");
		            String strRowId = (String) tempMap.get("rowId");	            
		            HashMap columnsMap = (HashMap) tempMap.get("columns");
		            String preferred = (String) columnsMap.get("ManufacturingPlanPreferred");	
		            HashMap addReturnMap = new HashMap();
		            HashMap cutReturnMap = new HashMap();
		            /* Code to connect the selected MP to the target MP */
		           if (markUpMode.equalsIgnoreCase("add"))
		           {

		        	   try{
		        		   //Connect the New MP
	                       /*DomainRelationship .connect(context,
	                                                   new DomainObject(strselObjId),
	                                                   new RelationshipType(ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN),
	                                                   new DomainObject(strSelObjId));*/
		        		   
		        		   /*ManufacturingPlan manufacturingPlan = new ManufacturingPlan(childObjId);
		        		   Relationship rel = (Relationship) manufacturingPlan.connectManufacturingPlan(context, strselObjId, ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN);
		                    	     */ 
		        		   
		        		    sLAddMPIDs.add(childObjId);
		        		    addReturnMap.put("oid", childObjId);
		                    addReturnMap.put("rowId", strRowId);
		                    addReturnMap.put("markup", markUpMode);
		                    mlAddItems.add(addReturnMap); // returnMap having all the details about the changed row.
		                    //adddoc.put("Action", "success"); // Here the action can be "Success" or "refresh"
		                   // adddoc.put("changedRows", mlAddItems);// Adding the key "ChangedRows" which having all the data for changed Rows
		                    preferredList.add(preferred);

		        	   }
		        	   catch (Exception e)
				       {
				                    adddoc.put("Message", e.getMessage());
				                    adddoc.put("Action", "ERROR");
				       }
		           }
		           else if (markUpMode.equalsIgnoreCase("cut"))
		           {
		        	   try
                  	 {
		        		 //Get the RelID between strselObjId and the object to be removed i.e. childObjectId from markup
		        		String strRelId =" ";
		        		DomainObject domMPId = new DomainObject(childObjId);
		        		StringList sLCntxtBrkDownMPIds = domMPId.getInfoList(context,"to["+ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN+"].from.id");
               			for(int k=0 ;k<sLCntxtBrkDownMPIds.size();k++)
               			{
               				String strCntxtBrkDownMPId =(String)sLCntxtBrkDownMPIds.get(k);
               				if(strCntxtBrkDownMPId!=null && strCntxtBrkDownMPId.equalsIgnoreCase(strselObjId))
               				{
               					//If this MP ID is connected to context Product or not
               					DomainObject domCntxtBrkDownMPId = new DomainObject(strCntxtBrkDownMPId);
               					String strContxtProductId = domCntxtBrkDownMPId.getInfo(context,"to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.id");
	                        	if(strContxtProductId!=null)
	                        	{ 
		                            if(strContxtProductId.equalsIgnoreCase(strRootObjectId))
		    		        		{
		                        			//Get the relId
		                        			String relPattern = "";
		                        	        String objPattern = "";
		                        	        StringList objSelects = new StringList();
		                        	        StringList relSelects = new StringList();
		                        	        String objWhere = "";
		                        	        String relWhere = "";
		                        	        
		                        	        relPattern =  ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN;
		                        	        objPattern = ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN;
		        	        				
		                        	        String strMPIdSelect = "to[" + ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN + "].from.id";
			                                String strMPTypeSelect = "to[" + ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN + "].from.type";
			                                String strMPNameSelect = "to[" + ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN + "].from.name";
		        	      
			                                objSelects.add(strMPIdSelect);
			                                objSelects.add(strMPTypeSelect);
			                                objSelects.add(strMPNameSelect);  
		                        	        
		                        	        objSelects.add(DomainConstants.SELECT_TYPE);
		                        	        objSelects.add(DomainConstants.SELECT_NAME);
		                        	        objSelects.add(DomainConstants.SELECT_ID);
			                                
			                                String strBrkDownRelId = "from[" + ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN + "].id";
			                                relSelects.add(strBrkDownRelId);
			                                
			                                relSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
			                               		                               	           	        			
			                                String whereClause = "to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.id == "+strRootObjectId;
		                        			DomainObject domConnectedMPId = new DomainObject(childObjId);
		                        			MapList mp = domConnectedMPId.getRelatedObjects(context,
		                        					                      relPattern,
		                        					                      objPattern,
		                        					                      objSelects,
		                        					                      relSelects,
		                        					                      true,
		                        					                      false,
		                                                                  (short) 0,
		                                                                  whereClause,
		                                                                  null,
		                                                                  0);
	
		                        			for(int l=0;l<mp.size();l++)
		                        			{
		                        				Map mManPlan = (Map)mp.get(l);
		                        				String strcntxtMPId = (String)mManPlan.get(DomainConstants.SELECT_ID);
		                        				if(strcntxtMPId!=null && strcntxtMPId.equalsIgnoreCase(strselObjId))
		                        				{
		                        					strRelId = (String)mManPlan.get(DomainConstants.SELECT_RELATIONSHIP_ID);
			                        				sLRemMPRelIDs.add(strRelId);
			                        				
		                        				}
		                        			}
		                        		    break;
		    		        		   }
		                        	}
               				   }  
               			  }
		        		        //calling method to disconnect 
            		            //DomainRelationship.disconnect(context, strRelId); 
            		            
            		            
            		            cutReturnMap.put("oid", childObjId);
	                            cutReturnMap.put("rowId", strRowId);
	                            cutReturnMap.put("markup", "cut");     	                           
	                            mlCutItems.add(cutReturnMap); // returnMap having all the details about the changed row.
	                           // cutdoc.put("Action", "success"); // Here the action can be "Success" or "refresh"
	                            //cutdoc.put("changedRows", mlCutItems);// Adding the key "ChangedRows" which having all the data for changed Rows
	                        
	                        }
		   	                catch (Exception e)
		   	                {
		   	                	cutdoc.put("Message", e.getMessage());
		   	                	cutdoc.put("Action", "ERROR");
		   	                }
		             }
		           adddoc.put("Action", "success");
		           adddoc.put("changedRows", mlAddItems);
		           cutdoc.put("Action", "success");
		           cutdoc.put("changedRows", mlCutItems);
		        }
		     //To connect MP's
		     if(!sLAddMPIDs.isEmpty())
		     {
		    	 ManufacturingPlan manufacturingPlan = new ManufacturingPlan();
		    	 manufacturingPlan.connectManufacturingPlan(context, strselObjId, ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN,sLAddMPIDs, preferredList); 
		    	 
		     }
		     
		     //To disconnect MP's
		     if(!sLRemMPRelIDs.isEmpty())
		     {
		    	 ManufacturingPlan manufacturingPlan = new ManufacturingPlan();
      		     manufacturingPlan.disconnectManufacturingPlan(context,sLRemMPRelIDs);
		     }
		 }
		 catch (Exception e)
		 {
			// TODO: handle exception
			 e.printStackTrace();
		 }
		 doc.putAll(cutdoc);
		 doc.putAll(adddoc);
		 return doc;
	 }
public static String getTypeIconProperty(Context context, String type)
    {
        String icon = EMPTY_STRING;
        String typeRegistered = EMPTY_STRING;

        try {
            if (type != null && type.length() > 0 )
            {
                String propertyKey = EMPTY_STRING;
                String propertyKeyPrefix = "emxFramework.smallIcon.";
                String defaultPropertyKey = "emxFramework.smallIcon.defaultType";

                // Get the symbolic name for the type passed in
                typeRegistered = FrameworkUtil.getAliasForAdmin(context, "type", type, true);

                if (typeRegistered != null && typeRegistered.length() > 0 )
                {
                    propertyKey = propertyKeyPrefix + typeRegistered.trim();

                    try {
                        icon = EnoviaResourceBundle.getProperty(context,propertyKey);
                    } catch (Exception e1) {
                        icon = EMPTY_STRING;
                    }
                    if( icon == null || icon.length() == 0 )
                    {
                        // Get the parent types' icon
                        BusinessType busType = new BusinessType(type, context.getVault());
                        if (busType != null)
                        {
                            StringList parentBusTypesList = busType.getParents(context);
                            String parentBusType = "";
                            if(parentBusTypesList.size()>0) {
                                parentBusType = (String)parentBusTypesList.elementAt(0);
                            }

                            if (parentBusType != null)
                                icon = getTypeIconProperty(context, parentBusType);
                        }

                        // If no icons found, return a default icon for propery file.
                        if (icon == null || icon.trim().length() == 0 )
                            icon = EnoviaResourceBundle.getProperty(context,defaultPropertyKey);
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("Error getting type icon name : " + ex.toString());
        }

        return icon;
    }
	/**
	 * This method is used to delete the associated manufacturing plans of a Feature or Product when Feature or Product is deleted.
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @author IVU
	 * @since CFP R209
	 */
	
	public int deleteConnectedManufacturingPlans(Context context, String args[])throws Exception{
		int iResult = 0;
		try {
			// get the context object which is deleted from the trigger.
		    String strProductFeaureId = args[0];
            ContextUtil.pushContext(context, PropertyUtil
                    .getSchemaProperty(context, "person_UserAgent"),
                    DomainConstants.EMPTY_STRING,
                    DomainConstants.EMPTY_STRING);
		    DomainObject domContext = new DomainObject(strProductFeaureId);
		    
		    // IVU - get the Master Feature Associated with the Feature Revision

		    ManufacturingPlan mPlan = new ManufacturingPlan();
		    // get the context master (Model or the Master Feature)
		    String strMFeatureModel = mPlan.getMasterFromContext(context, strProductFeaureId);
		    String strMPM = null;
		    if(strMFeatureModel!=null && !strMFeatureModel.equals("")){
		    	// get the MPM connected to the Master.
		    	strMPM = mPlan.getMPMFromMaster(context,strMFeatureModel);	
		    }
		    //StringList strMPsOfMPM = new StringList();
		    
		    StringList relSelects = new StringList();
		    Map relIds = new HashMap();
		    
		    String managedSeriesSelect="from["+ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_REVISIONS+"].to.id";
		    String managedRootSelect="from["+ManufacturingPlanConstants.RELATIONSHIP_MANAGED_ROOT+"].to.id";
		    DomainObject.MULTI_VALUE_LIST.add(managedSeriesSelect);
		    
		    relSelects.add(managedSeriesSelect);
		    relSelects.add(managedRootSelect);
		    
		    
		    DomainObject domMPM=null;
		    if(strMPM!=null && !strMPM.equals("")){
		    	domMPM = new DomainObject(strMPM);
		    	// get all the MPs connected to the		    	
		    	relIds = domMPM.getInfo(context,relSelects);
		    }
		    DomainObject.MULTI_VALUE_LIST.remove(managedSeriesSelect);
		    
		    Object managedSeriesObj = relIds.get(managedSeriesSelect);
		    Object managedRootObj = relIds.get(managedRootSelect);
		    
		    StringList managedSeriesString = ProductLineCommon.convertObjToStringList(context, managedSeriesObj);
		    StringList managedRootString = ProductLineCommon.convertObjToStringList(context, managedRootObj);
		    
		    managedSeriesString.addAll(managedRootString);
		    
		    
		    // get all the Manufacturing Plans connected to Context Products/Features
		    StringList sListMPIds = (StringList)domContext.getInfoList(context,"from["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].to.id");
		    String[] strMPIds = new String[sListMPIds.size()];
		    for(int i=0;i<sListMPIds.size();i++){
		    	strMPIds[i]= (String)sListMPIds.get(i);
		    }
		    // remove the common MP in both the MPs connected to the Feature and the MPs connected to the MPM.
		  //  strMPsOfMPM.removeAll(sListMPIds);
		    managedSeriesString.removeAll(sListMPIds);
		    // if all the MPs connected to the MPM are same as the MPs connected to Feature revision then delete the MPM.
		  //  if(strMPsOfMPM.size()==0 && domMPM!=null){	
		    if(managedSeriesString.size()==0 && domMPM!=null){
		    	domMPM.deleteObject(context);
		    }
		    // call deleteObjects method and pass the string array of manufacturing plan Ids.
		    DomainObject.deleteObjects(context, strMPIds);
		    ContextUtil.popContext(context);		    
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());		
		}
	    
		
		return iResult;
	}
	
	/**
	 * This Method is Access Program for "Edit ManufacturingPlan Breakdown"
	 * command to display only if Parent(context) is products
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return boolean - true or false
	 * @throws Exception
	 *             if the operation fails
	 * @author IXH
	 * @since DMCPlanning R209
	 * @grade 0
	 */
	
	public boolean isContextTypeProducts(Context context, String[] args)
	throws Exception {
		try{
			 HashMap programMap = (HashMap)JPO.unpackArgs(args);
			 String strObjectid = (String)programMap.get("objectId");
			 DomainObject domObjId = new DomainObject(strObjectid);
 		     String strParentObjId = domObjId.getInfo(context, "to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.id");
 		     DomainObject domParentObjId = new DomainObject(strParentObjId);
		     String strType = domParentObjId.getInfo(context,DomainConstants.SELECT_TYPE);
		     
		     if(mxType.isOfParentType(context,strType,ManufacturingPlanConstants.TYPE_PRODUCTS))
		     {
				return true;
			 }
			 else
			 {
				return false;
			 }
		    }catch(Exception e){
			e.printStackTrace();
			throw (new FrameworkException(e));
		}
	}

	/**
	 * This method is called by trigger to delete Manufacturing Plan Master when
	 * last revision of Manufacturing Plan is deleted.
	 * @param context
	 * @param args[]
	 * @return
	 * @throws Exception
	 * @author WPK
	 * @since CFP R209
	 * Reusing it in R212_DA
	 */
	
	public int deleteManufacturingPlanMaster(Context context, String args[])
			throws Exception {
		int iResult = 0;
		try {
			// get the context object which is deleted from the trigger.
			String strManufacturingPlanId = args[0];

			DomainObject domContext = new DomainObject(strManufacturingPlanId);
			String selectMPMObjId = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MANAGED_ROOT
					+ "].from.id";
			String mpmID = domContext.getInfo(context, selectMPMObjId);

			DomainObject domMPM = new DomainObject(mpmID);
			if (domMPM.exists(context)) {
				String[] strObjectToDelete = new String[1];
				strObjectToDelete[0] = mpmID;
				DomainObject.deleteObjects(context, strObjectToDelete);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return iResult;
	}
	
	/**
	 * This Methods is used to get context of Manufacturing Plan
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Object - MapList containing the id of Archived Manufacturing Plans objects
	 * @throws Exception
	 *             if the operation fails
	 * @author KXB for IR-030541
	 * @since DMCPlanning R209
	 * @grade 0
	 */
	public StringList displayContextInMPSummary (Context context, String[] args)
	throws Exception {
		StringList ctxtList= new StringList();
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");

		String exportFormat = null;
		boolean exportToExcel = false;
		HashMap requestMap = (HashMap)programMap.get("paramList");
		if(requestMap!=null && requestMap.containsKey("reportFormat")){
			exportFormat = (String)requestMap.get("reportFormat");
		}
		if("CSV".equals(exportFormat)){
			exportToExcel = true;
		}
		Map tempMap;	
		String objNameRev = "";
		for (int iCnt = 0; iCnt < objectList.size(); iCnt++) {			
			tempMap = (Map) objectList.get(iCnt);
			if (tempMap.containsKey("to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.name")){	            	         	
				objNameRev = (String) tempMap.get("to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.name")
				+ " " +(String) tempMap.get("to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.revision");
				StringBuffer sb = new StringBuffer();
				if(exportToExcel){
					sb.append(objNameRev);
					ctxtList.add(sb.toString());
				}
				else{
				String strTemp = "<a TITLE=";
				String strEndHrefTitle = ">";
				String strEndHref = "</a>";
				sb =  sb.append(strTemp);
				String strTypeIcon= ProductLineCommon.getTypeIconProperty(context, (String) tempMap.get("to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from."+DomainConstants.SELECT_TYPE));

				sb =  sb.append("\"\"")
				.append(strEndHrefTitle)
				.append("<img border=\'0\' src=\'../common/images/"+XSSUtil.encodeForHTMLAttribute(context,strTypeIcon)+"\'/>")
				.append( strEndHref)
				.append(" ");
				sb.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
				sb.append(XSSUtil.encodeForHTMLAttribute(context,(String) tempMap.get("to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.id")));
				sb.append("')\">");
				sb.append(XSSUtil.encodeForXML(context,objNameRev));
				sb.append("</A>");	              	
				ctxtList.add(sb.toString());
				}
			}else
				ctxtList.add(DomainConstants.EMPTY_STRING);
		}
		return ctxtList;
	}
	/**
	 * This will be column JPO which will show the derived from column details
	 * depending on the policy.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * @return String list containing HTML code, to render derived from columns
	 *         data in Table
	 * @throws Exception
	 */
	public StringList displayDerivedFromInColumn(Context context, String[] args)
			throws Exception {
		StringList derivedFromList = new StringList();
		try {
			// Unpacking the args
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");
			String exportFormat = null;
			boolean exportToExcel = false;
			HashMap requestMap = (HashMap)programMap.get("paramList");
			if(requestMap!=null && requestMap.containsKey("reportFormat")){
				exportFormat = (String)requestMap.get("reportFormat");
			}
			if("CSV".equals(exportFormat)){
				exportToExcel = true;
			}
			StringList newList = new StringList();
			String derivedFromSelectableName = "from["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED + "].to."
					+ DomainConstants.SELECT_NAME;
			String derivedFromSelectableID = "from["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED + "].to."
					+ DomainConstants.SELECT_ID;
			String derivedFromSelectableType = "from["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED + "].to."
					+ DomainConstants.SELECT_TYPE;
			String derivedFromSelectablePolicy = "from["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED + "].to."
					+ DomainConstants.SELECT_POLICY;
			newList.addElement(derivedFromSelectableName);
			newList.addElement(derivedFromSelectableType);
			newList.addElement(derivedFromSelectablePolicy);
			newList.addElement(derivedFromSelectableID);

			Map tempMap;
			for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
				// checking if we have Id in objectList;
				tempMap = (Map) iterator.next();
				if ((tempMap).containsKey(DomainConstants.SELECT_ID)) {
					String planId = tempMap.get(DomainConstants.SELECT_ID)
							.toString();
					DomainObject planObjectID = new DomainObject(planId);
					ContextUtil.pushContext(context, PropertyUtil
							.getSchemaProperty(context, "person_UserAgent"),
							DomainConstants.EMPTY_STRING,
							DomainConstants.EMPTY_STRING);
					Map strContextMap = planObjectID.getInfo(context, newList);
					ContextUtil.popContext(context);
					// Check if derived Manufacturing plan exist.
					if (strContextMap.containsKey(derivedFromSelectableID)) {
						if(exportToExcel)
						{
							derivedFromList.add(strContextMap
									.get(derivedFromSelectableName).toString());
						}
						else{						
						// Check if Derived from Plan is in archived state.
						if (strContextMap
								.get(derivedFromSelectablePolicy)
								.equals(
										ManufacturingPlanConstants.POLICY_ARCHIVED)) {
							StringBuffer sbBuffer = new StringBuffer(400);
							sbBuffer = sbBuffer
									.append("<img src=\"images/")
									.append(
											ProductLineCommon
													.getTypeIconProperty(
															context,
															(String) (strContextMap
																	.get(derivedFromSelectableType))))
									.append("\"")
									.append("/>")
									.append("<a TITLE=\"\">")
									.append(" ")
									.append(XSSUtil.encodeForXML(context,
											strContextMap
													.get(derivedFromSelectableName).toString()))
									.append("</a>");
							derivedFromList.add(sbBuffer.toString());

						} else {// if has derived From Manufacturing plan and
								// not archived
							StringBuffer sbBuffer = new StringBuffer(400);
							sbBuffer = sbBuffer
									.append("<img src=\"images/")
									.append(
											ProductLineCommon
													.getTypeIconProperty(
															context,
															(String) (strContextMap
																	.get(derivedFromSelectableType))))
									.append("\"").append("/>").append(
											"<a TITLE=\"\">").append("</a>");

							sbBuffer
									.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
							sbBuffer.append(strContextMap
									.get(derivedFromSelectableID));
							sbBuffer.append("')\">");
							sbBuffer.append(XSSUtil.encodeForXML(context,strContextMap
									.get(derivedFromSelectableName).toString()));
							sbBuffer.append("</A>");

							derivedFromList.add(sbBuffer.toString());
						}
					}
					} else {
						derivedFromList.add(DomainConstants.EMPTY_STRING);
					}
				} else {
					derivedFromList.add(DomainConstants.EMPTY_STRING);
				}

			}

		} catch (Exception e) {
			throw new FrameworkException("displayDerivedFromInMPSummary==="
					+ e.getMessage());
		}
		return derivedFromList;
	}
	
	/**
	 * This will be used in getting the derived from object's icon name for
	 * property page
	 * 
	 * @param context
	 * @param args
	 *            use args to get objectid from request map.
	 * @return
	 * @throws Exception
	 * Refactored from R212_DA
	 */
	public String displayDerivedFromPropertyPage(Context context, String[] args)
	throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String exportFormat = null;
		boolean exportToExcel = false;
		// Gets the objectid manufacturing plan from args
		String strContextId = (String) requestMap.get("objectId");
		DomainObject objContext = new DomainObject(strContextId);
		if(requestMap!=null && requestMap.containsKey("reportFormat")){
			exportFormat = (String)requestMap.get("reportFormat");
		}
		if("CSV".equals(exportFormat)){
			exportToExcel = true;
		}
		//objselects
		StringList slObjSelects= new StringList();
		String strDerivedAbstractId="to[" + ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
				+ "].from." + ManufacturingPlanConstants.SELECT_ID;
		String strDerivedAbstractName="to[" + ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
				+ "].from." + ManufacturingPlanConstants.SELECT_NAME;
		String strDerivedAbstractType = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
			+ "].from." + ManufacturingPlanConstants.SELECT_TYPE;
		String strDerivedAbstractTitle = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
			+ "].from."
			+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;
		String strDerivedAbstractRevision = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
			+ "].from." + ManufacturingPlanConstants.SELECT_REVISION;

		slObjSelects.add(strDerivedAbstractId);
		slObjSelects.add(strDerivedAbstractName);
		slObjSelects.add(strDerivedAbstractType);
		slObjSelects.add(strDerivedAbstractTitle);
		slObjSelects.add(strDerivedAbstractRevision);
		
		
		String mpMainDerivedType = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
			+ "].from." + ManufacturingPlanConstants.SELECT_TYPE;
		String mpMainDerivedTitle = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
			+ "].from."
			+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;
		String mpMainDerivedRevision = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
			+ "].from." + ManufacturingPlanConstants.SELECT_REVISION;
		String mpMainDerivedId = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
			+ "].from." + ManufacturingPlanConstants.SELECT_ID;

		String mpDerivedType = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
			+ "].from." + ManufacturingPlanConstants.SELECT_TYPE;
		String mpDerivedTitle = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
			+ "].from."
			+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;
		String mpDerivedRevision = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
			+ "].from." + ManufacturingPlanConstants.SELECT_REVISION;
		String mpDerivedId = "to["
			+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
			+ "].from." + ManufacturingPlanConstants.SELECT_ID;
	
	
		Map strContextMap = objContext.getInfo(context, slObjSelects);
		StringBuffer sbBuffer = new StringBuffer();
		if ((strContextMap).containsKey(DomainConstants.SELECT_ID)) {
			String strDerivedFromID="";
			String strDerivedFromType="";
			String strDerivedFromTitle="";
			String strDerivedFromRevision="";
			if (strContextMap.containsKey(mpMainDerivedId)) {
				strDerivedFromID=(String)strContextMap.get(mpMainDerivedId);
				strDerivedFromType=(String)strContextMap.get(mpMainDerivedType);
				strDerivedFromTitle=(String)strContextMap.get(mpMainDerivedTitle);
				strDerivedFromRevision=(String)strContextMap.get(mpMainDerivedRevision);
			}else if (strContextMap.containsKey(mpDerivedId)){
				strDerivedFromID=(String)strContextMap.get(mpDerivedId);
				strDerivedFromType=(String)strContextMap.get(mpDerivedType);
				strDerivedFromTitle=(String)strContextMap.get(mpDerivedTitle);
				strDerivedFromRevision=(String)strContextMap.get(mpDerivedRevision);
			}else{
				String strLanguage = context.getSession().getLanguage();
				String strRoot = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
				"DMCPlanning.MPDerivation.DerivationType.Root",strLanguage);
				return strRoot;
			}
				if(exportToExcel==true)
				{
					sbBuffer.append(strDerivedFromTitle+" "+strDerivedFromRevision);
				}else{
					sbBuffer.append(
						"<img border=\'0\' src=\"images/").append(
								ProductLineCommon.getTypeIconProperty(context,
										(String) (strDerivedFromType)))
												.append("\"").append("/>").append("<a TITLE=\"\">")
												.append("</a>");
					sbBuffer
					.append("<A class=\"object\" HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
					sbBuffer.append(strDerivedFromID);
					sbBuffer.append("')\">");
					sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,strDerivedFromTitle).toString());
					sbBuffer.append(" ");
					sbBuffer.append(strDerivedFromRevision);
					sbBuffer.append("</A>");
				}

			} else {
				sbBuffer.append(DomainConstants.EMPTY_STRING);
			}
		return sbBuffer.toString();
	}

	/**
	 * This Methods gets Name values throw "objectList" key present in args passed by expandprogram
	 * @used : used as  Name column JPO for  in CFPEditManufacturingPlanBreakdownTable
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param String[] -
	 *            Holds the all the arguments passed by AEF throw structure browser expand.
	 *            we will use only key= "objectList"
	 * @return StringList -  contains the list of objects to be displayed in Table
	 * @throws Exception
	 *             if the operation fails
	 * @author IXH
	 * @since DMCPlanning R209
	 * @grade 0
	 */
	public List getObjectName(Context context , String[] args) throws Exception{
        //unpack the arguments
        Map programMap = (HashMap) JPO.unpackArgs(args);
        List lstobjectList = (MapList) programMap.get("objectList");
        Iterator objectListItr = lstobjectList.iterator();
        //initialise the local variables
            String oidsArray[] = new String[lstobjectList.size()];
            int i = 0;
        Map objectMap = new HashMap();
            String strObjIdtemp =DomainConstants.EMPTY_STRING;
            while(objectListItr.hasNext())
            {
                objectMap = (Map) objectListItr.next();
                strObjIdtemp = (String)objectMap.get(DomainConstants.SELECT_ID);
                oidsArray[i] = strObjIdtemp;
                i++;
            }
            StringList selects = new StringList(3);
            selects.add(DomainConstants.SELECT_TYPE);
            selects.add(DomainConstants.SELECT_NAME);
            selects.add(DomainConstants.SELECT_REVISION);
            MapList list = DomainObject.getInfo(context, oidsArray, selects);
        String strObjId = DomainConstants.EMPTY_STRING;
            Map objecttempMap = new HashMap();
        List lstNameRev = new StringList();
            StringBuffer stbNameRev;
        String strType = null;
            String strName = null;
            String strRev = null;

            objectListItr = list.iterator();
        while(objectListItr.hasNext())
        {
                objecttempMap = (Map) objectListItr.next();
                strName = (String)objecttempMap.get(DomainConstants.SELECT_NAME);
                strType = (String)objecttempMap.get(DomainConstants.SELECT_TYPE);
                strRev = (String)objecttempMap.get(DomainConstants.SELECT_REVISION);
            
                stbNameRev = new StringBuffer(100);
                stbNameRev = stbNameRev.append(strName);
                if(strType.equalsIgnoreCase(ProductLineConstants.TYPE_PRODUCT_VARIANT)){
                    stbNameRev.append(" " );
                    stbNameRev.append(strRev.substring(0,1));
                } else {
                    stbNameRev.append(" ");
                    stbNameRev.append(strRev);
            }
            lstNameRev.add(stbNameRev.toString());
        }
        return lstNameRev;
    }
	
	/**Added for IR-038813V6R2011 
	 * This Methods is Access Program for command "Create Manufacturing Plan" 
	 * and "Create Manufacturing Plan From" 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the following arguments 0 - HashMap containing the
	 *            following arguments
	 * @return Boolean - Value True/False depending upon whether the higher revision is associated with MP or not
	 * @throws Exception
	 *             if the operation fails
	 * @author IXH
	 * @since DMCPlanning R209
	 * @grade 0
	 */
	public boolean isMPAssociatedToHigherRev(Context context, String[] args)
	throws Exception {
		try{
			Boolean bNoMPAssociatedToHigherRev = true;
			
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			String strObjId = (String)programMap.get("parentOID");
			
			StringList sLAllPrevRev = new StringList();
			List lstRevIdList = new StringList();
			List lstHigherRevs = new MapList();
			
			ManufacturingPlanUtil BC = new ManufacturingPlanUtil();
			//To get all the revisions of the product
			lstRevIdList = BC.getAllRevisions(context, strObjId);
			//TO get the higher revisions of the product
			List lstTempRev = BC.getRevisions(context,strObjId,lstRevIdList,true);
			
			for(int i=0;i<lstTempRev.size();i++)
			{
			   Map mTemp = new HashMap();
			   mTemp = (Map)lstTempRev.get(i);
			   String strObjRevId = (String)mTemp.get(DomainConstants.SELECT_ID);
			   
			    //Check if any MPs connected
			    DomainObject domFeatRevId = new DomainObject(strObjRevId);
			    ContextUtil.pushContext(context);
			    String[] argsTemp = new String[3];
			    argsTemp[0] = strObjRevId;
			    argsTemp[1] = "from";
			    argsTemp[2] = ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN;
			    boolean isMPconnected = Boolean.parseBoolean(hasRelationship(context,argsTemp));
			   
			    if(isMPconnected)
			    {
			    	bNoMPAssociatedToHigherRev = false;
			    	break;			    	
			    }
			    ContextUtil.popContext(context);
			}
			
			return bNoMPAssociatedToHigherRev;
			
		}catch(Exception e){
			e.printStackTrace();
			throw (new FrameworkException(e));
		}
	}	
	
	/**
	 * This method is called by trigger on ChangeName of Model / MasterFeature to
	 * change the Name of the associated MPM object Name.
	 * @param context
	 * @param args[]
	 * 		  args[0] - Object ID of Model/Master Feature
	 * 	 	  args[1] - New Name of Model/Master Feature
	 * @return
	 * 			0 - if success
	 * 			1 - if failure
	 * @throws Exception
	 * @author IVU
	 * @since CFP R209
	 */
	
	public int changeManufacturingPlanMasterName(Context context, String args[])throws Exception{
		int iResult = 0;
		try {
			// get the context object which is deleted from the trigger.
			String strMasterFeatureId = args[0];
			String strMasterFeatureName = args[1];
			//Fix for IR-045782V6R2011x STARTS
			String strMasterFeatureType = args[2];
			//Fix for IR-045782V6R2011x ENDS
			DomainObject domContext = new DomainObject(strMasterFeatureId);
			String strManufacturingPlanMasterId = (String)domContext.getInfo(context,"to["+ManufacturingPlanConstants.RELATIONSHIP_SERIESMASTER+"].from.id");
			if(strManufacturingPlanMasterId!=null && !strManufacturingPlanMasterId.equals("")){
    			String strMPMPrefix = EnoviaResourceBundle.getProperty(context,"DMCPLanning.ManufacturingPlanMaster.PrefixPattern");
    			StringBuffer sbName = new StringBuffer();
    			sbName.append(strMPMPrefix);
    			//Fix for IR-045782V6R2011x STARTS
    			sbName.append(strMasterFeatureType);
    			//Fix for IR-045782V6R2011x ENDS
    			sbName.append(strMasterFeatureName);
    			DomainObject domMPM = new DomainObject(strManufacturingPlanMasterId);
    			domMPM.setName(context, sbName.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());		
		}
		return iResult;
	}

	/**
	 * This is a trigger method called on Managed Series relationship
	 * used to connect the newly created Manufacturing Plan to the revision chain.
	 * 
	 * @param context - Matrix Context object
	 * @param args - args[0] - contains the newly created Manufacturing Plan Id
	 * 				 args[1] - contains the Manufacturing Plan Master Object Id which is connected to the MF through Managed Series relationship
	 * 				 args[2] - contains the newly created Manufacturing Plan Type
	 * @return iResult - 0 - if the operation is success
	 * 					 1 - if the operation fails
	 * @throws Exception
	 * @author IVU
	 * @since CFP R210
	 */
	
	public int connectToPreviousRevision(Context context, String args[])throws Exception{
		int iResult = 0;
		try {
			// Get all the details from the trigger Object
			String strMPId = args[0];
			String strMPMId = args[1];
			String strMPType = args[2];
			
			// As this Create trigger on Manged Series relationship is also called when the Feature is connected to the Master Feature
			// the following check is made to check if the trigger is fired for Manufacruring Plan Master
			if(strMPId!=null && !strMPId.equals("") && strMPMId!=null && !strMPMId.equals("")){
				DomainObject dom = new DomainObject(strMPMId);
				if(mxType.isOfParentType(context,dom.getInfo(context, DomainObject.SELECT_TYPE),
						ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN_MASTER)){
					
				    StringList objSelects = new StringList(DomainConstants.SELECT_ID);
				    objSelects.addElement(DomainConstants.SELECT_REVISION);
				
				    StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
				    String strRelName = ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION;
				    
				    // Need to do the push context as to get the MF objects which are archived.
                    ContextUtil.pushContext(context, PropertyUtil
                            .getSchemaProperty(context, "person_UserAgent"),
                            DomainConstants.EMPTY_STRING,
                            DomainConstants.EMPTY_STRING);
                    
    			    String strObjWhere = "id!="+strMPId;
		    		
                    // The Type is provided as * because the type could be anything. However this call will make sure not to get too many 
                    // objects because the limit is 1, and the rel pattern is alread provided.
				    MapList mManagedMPs = dom.getRelatedObjects(context,
				    		strRelName, "*",objSelects, relSelectsList,
				    		false, true, (short)0,strObjWhere,DomainConstants.EMPTY_STRING,(int) 1,null,null,null);
				    String strTempMPId = "";
				    for(int i=0; i< mManagedMPs.size();i++){
				    	Map mMP = (Map)mManagedMPs.get(i);
				    	strTempMPId = (String)mMP.get(DomainConstants.SELECT_ID); 
				    }
				    // if the revisions of the MF Object exists then get the 
				    // last revision and connect the current MF object in the revision sequence
				    if(!strTempMPId.equals("")){
				    	DomainObject domMP = new DomainObject(strTempMPId);
				    	BusinessObject bo = domMP.getLastRevision(context);
				    	//bo.revise(context,new BusinessObject(strMPId),false);
				    }
				    ContextUtil.popContext(context);
				}
			}
		} catch (Exception e) {
			iResult =1;
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());		
		}
		return iResult;
	}

	
	/**
	 * This method is used to generate the next revision sequence for the newly created Manufacruring Plan
	 * 
	 * @param context
	 * @param objId - Object ID of the Newly created Manufacturing Plan
	 * @param objParentOID - The Context Object under which the Manufacturing Plan is created 
	 * @return strLatestRev - Sting containing the Revision Sequence that need to be set for the newly created Manufactruing Plan
	 * @throws FrameworkException
	 * @author IVU
	 * @since CFP R210
	 */

	private String getManufacturingPlanRevision(Context context, String objId, String objParentOID)throws FrameworkException
	{
		String strLatestRev = "";
		try
		{
			if(objId!=null && !objId.equals("") 
					&& objParentOID!=null && !objParentOID.equals(""))
			{
				ManufacturingPlan mpBean = new ManufacturingPlan();
				String strMaster = mpBean.getMasterFromContext(context, objParentOID);
				
				String strMPM = null;
				if(strMaster!=null && !strMaster.equals("")
						&& !strMaster.equals("null")){
					strMPM = mpBean.getMPMFromMaster(context, strMaster);	
				}
				StringList objSelects = new StringList(DomainConstants.SELECT_ID);
			    objSelects.addElement(DomainConstants.SELECT_REVISION);
			    objSelects.addElement(DomainConstants.SELECT_POLICY);
			    objSelects.addElement(DomainConstants.SELECT_TYPE);
			    objSelects.addElement("attribute["+ManufacturingPlanConstants.ATTRIBUTE_REVISION_COUNT+"]");
				
				// Creating Object of Manufacturing Plan Master
				DomainObject dom = new DomainObject(strMPM);
			    Map mapMPDetails = dom.getInfo(context, objSelects);
			    strLatestRev = mapMPDetails.get("attribute["+ManufacturingPlanConstants.ATTRIBUTE_REVISION_COUNT+"]").toString();
			}
		}catch (Exception e) {
			throw (new FrameworkException(e));
		}
		return strLatestRev;
	}
	/**
	 * This method is update function for modifying the attribute Manufacturing Intent
	 * @param context
	 * @param args
	 * @throws Exception
	 * @since R211
	 */

	public void updateManufacturingIntent(Context context, String[] args) throws Exception{
		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String objectId = (String) paramMap.get("objectId");
			String newValue = (String) paramMap.get("New Value");
			String language = context.getSession().getLanguage();
			if(objectId!=null && !objectId.equalsIgnoreCase("")){
				DomainObject domObj = new DomainObject(objectId);
				String mpState = domObj.getInfo(context, SELECT_CURRENT);
				if(mpState!=null && !"".equalsIgnoreCase(mpState)&& !mpState.equalsIgnoreCase(ManufacturingPlanConstants.STATE_RELEASE))
				{
				if(newValue!=null && !newValue.equalsIgnoreCase("")){
					domObj.setAttributeValue(context,ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_INTENT, newValue);
				}
				}else
				{
					 String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Error.MPReleased",language);
					  throw  new Exception(strAlertMessage);
				}
			}
		}catch (Exception ex) {
	            throw  new FrameworkException((String)ex.getMessage());
	        }
		}
	/**
	 * This method is update function for modifying the attribute Manufacturing Plan Preferred
	 * @param context
	 * @param args
	 * @throws Exception
	 * @since R211
	 */
	public void updateManufacturingPlanPreferred(Context context, String[] args) throws Exception{
		try
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String objectId = (String) paramMap.get("objectId");
			String relId = (String) paramMap.get("relId");
			String newValue = (String) paramMap.get("New Value");
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String selObjId=(String) requestMap.get("selObjId");
			String language = context.getSession().getLanguage();
			if(newValue!=null && !"".equalsIgnoreCase(newValue) && newValue.equalsIgnoreCase(ManufacturingPlanConstants.RANGE_VALUE_YES) ){
			String connectionToUpdate=ManufacturingPlan.getMPBreakDownRelToUpdate(context, objectId, selObjId);		
			if(connectionToUpdate!=null && !"".equalsIgnoreCase(connectionToUpdate))
			{
				DomainRelationship domRelToUpdate = new DomainRelationship(connectionToUpdate);
				domRelToUpdate.setAttributeValue(context, ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_PLAN_PREFERRED, ManufacturingPlanConstants.RANGE_VALUE_NO);
			}
						
			if(relId!=null && !relId.equalsIgnoreCase("")){
				DomainRelationship domRel = new DomainRelationship(relId);
				if(newValue!=null && !newValue.equalsIgnoreCase("")){
					domRel.setAttributeValue(context, ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_PLAN_PREFERRED, newValue);
				}
			}
			}
			else{
				 String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Error.AtLeastOnePreferred",language);
				 //throw  new Exception(strAlertMessage);
			}
			if(newValue!=null && !"".equalsIgnoreCase(newValue) && newValue.equalsIgnoreCase(ManufacturingPlanConstants.RANGE_VALUE_NO) ){
				DomainRelationship domRelToUpdate = new DomainRelationship(relId);
				domRelToUpdate.setAttributeValue(context, ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_PLAN_PREFERRED, ManufacturingPlanConstants.RANGE_VALUE_NO);
			}
					
		}catch (Exception ex) {
	            throw  new FrameworkException(ex.getMessage());
	        }
		}
	/**
	 * This method returns the check icon of the column Check in the table CFPManufacturingPlanContextSummaryTable
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211
	 */
	public Vector checkProductManufacturingPlanConsistency(Context context , String[] args) throws Exception{
		Vector returnVector = new Vector();
		try{
			String iconMPBCompliant = EnoviaResourceBundle.getProperty(context,"DMCPlanning.StatusImage.MPBCompliant");
			String iconMPBCompliantWithRetrofit = EnoviaResourceBundle.getProperty(context,"DMCPlanning.StatusImage.MPBCompliantWithRetrofit");
			String iconMPBNotCompliant = EnoviaResourceBundle.getProperty(context,"DMCPlanning.StatusImage.MPBNotCompliant");
			String iconMPBORDesign = EnoviaResourceBundle.getProperty(context,"DMCPlanning.StatusImage.MPBORDesign");
			String iconStatusAlert =EnoviaResourceBundle.getProperty(context,"DMCPlanning.StatusImage.StatusAlert");

			Map programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramList");
			MapList objectList = (MapList) programMap.get("objectList");
			String strToolTip = DomainConstants.EMPTY_STRING;
			String strToolTipNotComplete= EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.NotComplete.ToolTip",context.getSession().getLanguage() );
			String manufacturingIntent = DomainConstants.EMPTY_STRING;
			
			
			String relAssoManPlanId = (String) paramMap.get("relId");
			if(relAssoManPlanId==null || relAssoManPlanId.equals("null") || relAssoManPlanId.equals("")){
				String objectID = (String) paramMap.get("parentOID"); 
				DomainObject domMP = new DomainObject(objectID);	
				String assoID = domMP.getInfo(context,"to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN +"].id");
				relAssoManPlanId = assoID;
				
			}
			String strFromId = null;
			DomainObject domFrom = null;
			if(null != relAssoManPlanId && !"null".equals(relAssoManPlanId) && !"".equals(relAssoManPlanId)){
				
				DomainRelationship domRel = new DomainRelationship(relAssoManPlanId);
				Hashtable<String, StringList> relData = domRel.getRelationshipData(context, new StringList("from.id"));
				StringList fromIds = relData.get("from.id");
				if( ! fromIds.isEmpty() )
				{
					strFromId = (String) fromIds.firstElement();
					domFrom = new DomainObject(strFromId);
				}				
			}			
			
			HashMap<String, String> idParentMap = new HashMap<String, String>(objectList.size());
			
			//For all objects
			if(objectList!=null && !objectList.isEmpty()){
				
				for (Object mapObj : objectList) {
					Map objectMap = (Map) mapObj;
					if(objectMap != null && !objectMap.isEmpty()){
						String objectMapId = (String) objectMap.get(DomainConstants.SELECT_ID);
						if(objectMapId != null && ! objectMapId.isEmpty())
						{							
							String strParentId = getParentIdFromObjectMap(context, objectMap, paramMap); //IR-373374-3DEXPERIENCER2016x: Exception given when user clicks on Name link of Manufacturing plan 
							idParentMap.put(objectMapId, strParentId);
						}
					}
				}
				
				HashMap<String, HashMap<String, StringList> > parentIdModelIdMpIds = getMPsThroughMasterModels(context, idParentMap);				
				
				Iterator objectListItr = objectList.iterator();
				while(objectListItr.hasNext()){
					boolean boolMPBCompliant = false;
					boolean boolMPBCompliantWithRetrofit = false;
					boolean boolMPBNotCompliant =false;
					boolean boolMPBAlert=false;
					StringList mpImplements=new StringList();
					int implementsSize=0;
					StringBuffer sBuffer = new StringBuffer();
					ManufacturingPlan objManufacturingPlan = new ManufacturingPlan();
					DomainObject domParentObject = new DomainObject();
					Map objectMap = (Map) objectListItr.next();
					if(objectMap!=null && !objectMap.isEmpty()){
						String objectMapId = (String) objectMap.get(DomainConstants.SELECT_ID);
						//String strParentId = (String) objectMap.get("id[parent]");
						String strAssociatedProductFeature = (String) objectMap.get("to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+ "].from.id");
						String strParentId = idParentMap.get(objectMapId);
						//if(null == strParentId || "null".equals(strParentId) || "".equals(strParentId)){
						//	if(domFrom.isKindOf(context, ManufacturingPlanConstants.TYPE_HARDWARE_PRODUCT)){
						//		strParentId = strFromId ;
						//	}
						//}
						if (strParentId != null) {
							domParentObject.setId(strParentId);
						}
						DomainObject domManuPlan=new DomainObject(objectMapId);	
						if (strParentId != null
								&& domParentObject.isKindOf(context,ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN)) {
							// When expanding manufacturing plan, to see "Check" column for Child Manufacturing Plans
							StringList sCheckStatusList = objManufacturingPlan.getIconForCheck(context, objectMapId,strParentId,strAssociatedProductFeature);
							String sStatusObject = ManufacturingPlanConstants.EMPTY_STRING;
							String sCheckIcon = ManufacturingPlanConstants.EMPTY_STRING;
							String sCheckTooltip = ManufacturingPlanConstants.EMPTY_STRING;
							Iterator checkStatusItr = sCheckStatusList
							.iterator();
							while (checkStatusItr.hasNext()) {
								sStatusObject = (String) checkStatusItr.next();
								if (sStatusObject != null
										&& !sStatusObject.equals("")) {
									sCheckTooltip = sStatusObject.toString().substring(sStatusObject.toString().lastIndexOf("|") + 1,sStatusObject.toString().length());
									sCheckIcon = sStatusObject.toString().substring(0,sStatusObject.toString().lastIndexOf("|"));
									StringBuilder strChildCheckIcon = new StringBuilder();
									strChildCheckIcon.append("<center><img src=\"../common/images/");
									strChildCheckIcon.append(XSSUtil.encodeForHTMLAttribute(context,sCheckIcon));
									strChildCheckIcon.append("\" border=\"0\"");
									strChildCheckIcon.append(" alt=\"");
									strChildCheckIcon.append(XSSUtil.encodeForHTMLAttribute(context,sCheckTooltip));
									strChildCheckIcon.append("\" title=\"");
									strChildCheckIcon.append(XSSUtil.encodeForHTMLAttribute(context,sCheckTooltip));
									strChildCheckIcon.append("\" /></center>");
									sBuffer.append(strChildCheckIcon.toString());
								}
								returnVector.add(sBuffer.toString());
							}
						}

						else {

							if(objectMapId!=null && !objectMapId.equalsIgnoreCase("")&& domManuPlan.isKindOf(context, ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN) && strParentId!=null && !"".equalsIgnoreCase(strParentId)){
								//End of IR-080169V6R2012 		
								ManufacturingPlan manuPlan =new ManufacturingPlan(objectMapId);
								MapList relatedDesignObjects = manuPlan.getDesignComposition(context,strParentId);
								MapList unresolvedDesignToImplementObjects = new MapList();
								MapList unresolvedDesignObjects = new MapList();
								StringList mpBrkDownStruct=new StringList();
								StringList prdsLF=new StringList();
								String[] argsMP = new String[]{objectMapId};

								manufacturingIntent = new DomainObject(objectMapId).getAttributeValue(context, ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_INTENT);

								if(relatedDesignObjects!=null && !relatedDesignObjects.isEmpty()){
									Iterator relatedDesignObjectsItr = relatedDesignObjects.iterator();
									while(relatedDesignObjectsItr.hasNext()){
										Map relatedDesignObject = (Map)relatedDesignObjectsItr.next();
										if(relatedDesignObject!=null && !relatedDesignObject.isEmpty()){
											MapList relatedProducts = (MapList)relatedDesignObject.get("relatedProducts");

											if(relatedProducts!=null && !relatedProducts.isEmpty()){
												Iterator relatedProductsItr = relatedProducts.iterator();
												while(relatedProductsItr.hasNext()){
													Map relatedProduct = (Map)relatedProductsItr.next();
													if(relatedProduct!=null && !relatedProduct.isEmpty()){
														String relatedProductId = (String)relatedProduct.get(DomainConstants.SELECT_ID);
														prdsLF.addElement(relatedProductId);
													}
												}
												if(relatedProducts.size()>1){										
													unresolvedDesignObjects.add(relatedDesignObject);
												}			
											}
										}
									}
									unresolvedDesignToImplementObjects = manuPlan.getUnresolvedDesignToImplementObjects(context,unresolvedDesignObjects);
									strToolTip=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.UnResolved.ToolTip",context.getSession().getLanguage() );
									if(unresolvedDesignToImplementObjects!=null && !unresolvedDesignToImplementObjects.isEmpty()){
										sBuffer.append("<center><img src=\"../common/images/" + XSSUtil.encodeForHTMLAttribute(context,iconMPBORDesign) + "\" border=\"0\"" + " alt=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\" title=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\"/></center>");									
									}
									//for check column refresh when product structure changed 
									else 
									{   
										mpBrkDownStruct = manuPlan.getInfoList(context, "from["+ ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN + "].to.id");
										
										String[] mpBrkDwnStructIds = new String[mpImplements.size()];
										mpBrkDwnStructIds = (String[]) mpBrkDownStruct.toArray(mpBrkDwnStructIds);
										
										StringList objSelects = new StringList(2);
										objSelects.addElement(DomainConstants.SELECT_ID);
										String strSelectForAMP = "to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.id";
										objSelects.addElement(strSelectForAMP);
										
										MapList prdMapList = DomainObject.getInfo(context, mpBrkDwnStructIds, objSelects);
																				
										mpImplements = manuPlan.getInfoList(context, "from["+ ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_IMPLEMENTS + "].to.id");
										implementsSize=mpImplements.size();
										StringList assManPlan=new StringList();
										StringList tempMpImplements =new StringList();
										tempMpImplements.addAll(mpImplements);

										HashMap<String, StringList> modelIdMpIdsMap = parentIdModelIdMpIds.get(strParentId);
										boolean flag1= checkMPBreakdownExists(context, mpBrkDownStruct, modelIdMpIdsMap);

										//boolean flag1= checkMPBreakdownExists(context,mpBrkDownStruct,strParentId );
										
										if(!prdMapList.isEmpty()){
											for(int j=0; j<prdMapList.size();j++){
												
												Map<String, String> prdMap = (Map<String, String>)prdMapList.get(j);
												String manId= prdMap.get(DomainConstants.SELECT_ID);
												String strPrd = prdMap.get(strSelectForAMP);

												//will have to check along with MPI, also if master composition is connected with product
												if(tempMpImplements.contains(strPrd) && prdsLF.contains(strPrd)){
													mpImplements.remove(strPrd);
													HashMap mp=new HashMap();
													mp.put("objectId", strPrd);
													String [] jpoArgs=JPO.packArgs(mp);
													ManufacturingPlanSearchBase_mxJPO mpSearch=new ManufacturingPlanSearchBase_mxJPO(context,jpoArgs );
													StringList strList=mpSearch.getMPofCurrentRevision(context,jpoArgs);
													StringList strListRetro=mpSearch.getMPofCurrentAndAllPreviousRevisions(context, jpoArgs);
													if(strList.contains(manId)){
														boolMPBCompliant=true;
													}else if(strListRetro.contains(manId)){
														boolMPBCompliantWithRetrofit=true;
													}else{
														boolMPBNotCompliant=true;
													}

												}else{
													MapList prdSibMap = ManufacturingPlan.getPrdSibling(context,strPrd);
													Iterator prdItr=prdSibMap.iterator();
													String prdSiblId="";
													while(prdItr.hasNext()){
														Map prodcutMap = (Map)prdItr.next();
														if(prodcutMap!=null && !prodcutMap.isEmpty()){
															prdSiblId=(String) prodcutMap.get(SELECT_ID);
														}
														if(tempMpImplements.contains(prdSiblId)&& prdsLF.contains(prdSiblId) ){
															mpImplements.remove(prdSiblId);	
															HashMap mp=new HashMap();
															mp.put("objectId", prdSiblId);
															String [] jpoArgs=JPO.packArgs(mp);
															ManufacturingPlanSearchBase_mxJPO mpSearch=new ManufacturingPlanSearchBase_mxJPO(context,jpoArgs );
															StringList strList=mpSearch.getMPofCurrentRevision(context,jpoArgs);
															StringList strListRetro=mpSearch.getMPofCurrentAndAllPreviousRevisions(context, jpoArgs);

															if(manufacturingIntent.equalsIgnoreCase("Retrofit")){
																if(strList.contains(manId)){															
																	boolMPBCompliantWithRetrofit=true;
																}else if(strListRetro.contains(manId)){
																	boolMPBCompliantWithRetrofit=true;
																}else{boolMPBNotCompliant=true;}
															}else if (manufacturingIntent.equalsIgnoreCase("Regular")){
																if(strList.contains(manId)){		                        				
																	boolMPBCompliant=true;
																}else{boolMPBNotCompliant=true;}
															}
														}
													}
												}

												if (!mpImplements.isEmpty() && !flag1 ) {

													boolMPBAlert = true;
												} else {
													boolMPBAlert = false;
												}
											}
										}else
										{
											if(!flag1){
												boolMPBAlert = true;
											}else{
												boolMPBAlert = false;
											}
										}
										//}

									}
								}
							}
							//If it is non Compliant- its either with incomplete or only Non Compliant icon
							if(boolMPBNotCompliant){
								strToolTip=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.NotCompliant.ToolTip", context.getSession().getLanguage() );
								if(boolMPBAlert){
									StringBuilder strIncompleteIcon = new StringBuilder();
									strIncompleteIcon.append("<center><img src=\"../common/images/");
									strIncompleteIcon.append(XSSUtil.encodeForHTMLAttribute(context,iconMPBNotCompliant));
									strIncompleteIcon.append("\" border=\"0\"");
									strIncompleteIcon.append(" alt=\"");
									strIncompleteIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strIncompleteIcon.append("\" title=\"");
									strIncompleteIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strIncompleteIcon.append("\" /><img src=\"../common/images/");
									strIncompleteIcon.append(XSSUtil.encodeForHTMLAttribute(context,iconStatusAlert));
									strIncompleteIcon.append("\" border=\"0\"");
									strIncompleteIcon.append(" alt=\"");
									strIncompleteIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTipNotComplete));
									strIncompleteIcon.append("\" title=\"");
									strIncompleteIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTipNotComplete));
									strIncompleteIcon.append("\" /></center>");
									sBuffer.append(strIncompleteIcon.toString());
								}else{
									StringBuilder strNonCompliantIcon = new StringBuilder();
									strNonCompliantIcon.append("<center><img src=\"../common/images/");
									strNonCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,iconMPBNotCompliant));
									strNonCompliantIcon.append("\" border=\"0\"");
									strNonCompliantIcon.append(" alt=\"");
									strNonCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strNonCompliantIcon.append("\" title=\"");
									strNonCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strNonCompliantIcon.append("\" /></center>");
									sBuffer.append(strNonCompliantIcon.toString());
								}
							}else if(boolMPBCompliantWithRetrofit){//IF it is Compliant with Retrofit,either with Incomplete or only Compliant icon
								strToolTip=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Retrofit.ToolTip",context.getSession().getLanguage() );
								if(boolMPBAlert){
									StringBuilder strCompliantRetrofitIcon = new StringBuilder();
									strCompliantRetrofitIcon.append("<center><img src=\"../common/images/");
									strCompliantRetrofitIcon.append(XSSUtil.encodeForHTMLAttribute(context,iconMPBCompliantWithRetrofit));
									strCompliantRetrofitIcon.append("\" border=\"0\"");
									strCompliantRetrofitIcon.append(" alt=\"");
									strCompliantRetrofitIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strCompliantRetrofitIcon.append("\" title=\"");
									strCompliantRetrofitIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strCompliantRetrofitIcon.append("\" /><img src=\"../common/images/");
									strCompliantRetrofitIcon.append(XSSUtil.encodeForHTMLAttribute(context,iconStatusAlert));
									strCompliantRetrofitIcon.append("\" border=\"0\"");
									strCompliantRetrofitIcon.append(" alt=\"");
									strCompliantRetrofitIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTipNotComplete));
									strCompliantRetrofitIcon.append("\" title=\"");
									strCompliantRetrofitIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTipNotComplete));
									strCompliantRetrofitIcon.append("\" /></center>");
									sBuffer.append(strCompliantRetrofitIcon.toString());
								}else{
									StringBuilder strCompliantRetrofitIcon = new StringBuilder();
									strCompliantRetrofitIcon.append("<center><img src=\"../common/images/");
									strCompliantRetrofitIcon.append(XSSUtil.encodeForHTMLAttribute(context,iconMPBCompliantWithRetrofit));
									strCompliantRetrofitIcon.append("\" border=\"0\"");
									strCompliantRetrofitIcon.append(" alt=\"");
									strCompliantRetrofitIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strCompliantRetrofitIcon.append("\" title=\"");
									strCompliantRetrofitIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strCompliantRetrofitIcon.append("\" /></center>");
									sBuffer.append(strCompliantRetrofitIcon.toString());
								}
							}else if(boolMPBCompliant){//IF It is compliant with Regular- either with Incomplete or only Compliant icon
								strToolTip=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Compliant.ToolTip",context.getSession().getLanguage() );
								if(boolMPBAlert){
									StringBuilder strIncompleteCompliantIcon = new StringBuilder();
									strIncompleteCompliantIcon.append("<center><img src=\"../common/images/");
									strIncompleteCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,iconMPBCompliant));
									strIncompleteCompliantIcon.append("\" border=\"0\"");
									strIncompleteCompliantIcon.append(" alt=\"");
									strIncompleteCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strIncompleteCompliantIcon.append("\" title=\"");
									strIncompleteCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strIncompleteCompliantIcon.append("\" /><img src=\"../common/images/");
									strIncompleteCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,iconStatusAlert));
									strIncompleteCompliantIcon.append("\" border=\"0\"");
									strIncompleteCompliantIcon.append(" alt=\"");
									strIncompleteCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTipNotComplete));
									strIncompleteCompliantIcon.append("\" title=\"");
									strIncompleteCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTipNotComplete));
									strIncompleteCompliantIcon.append("\" /></center>");
									sBuffer.append(strIncompleteCompliantIcon);
								}else{
									StringBuilder strCompliantIcon = new StringBuilder();
									strCompliantIcon.append("<center><img src=\"../common/images/");
									strCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,iconMPBCompliant));
									strCompliantIcon.append("\" border=\"0\"");
									strCompliantIcon.append(" alt=\"");
									strCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strCompliantIcon.append("\" title=\"");
									strCompliantIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTip));
									strCompliantIcon.append("\" /></center>");
									sBuffer.append(strCompliantIcon.toString());
								}
							}else if(boolMPBAlert && implementsSize == mpImplements.size()){//else it will be Incomplete only
								StringBuilder strIncompleteIcon = new StringBuilder();
								strIncompleteIcon.append("<center><img src=\"../common/images/");
								strIncompleteIcon.append(XSSUtil.encodeForHTMLAttribute(context,iconStatusAlert));
								strIncompleteIcon.append("\" border=\"0\"");
								strIncompleteIcon.append(" alt=\"");
								strIncompleteIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTipNotComplete));
								strIncompleteIcon.append("\" title=\"");
								strIncompleteIcon.append(XSSUtil.encodeForHTMLAttribute(context,strToolTipNotComplete));
								strIncompleteIcon.append("\" /></center>");
								sBuffer.append(strIncompleteIcon.toString());
							}
							returnVector.add(sBuffer.toString());     
						}
					}
				}
			}
		}catch (Exception e){
			throw  new FrameworkException((String)e.getMessage());
		}finally{
			return returnVector;
		}
	}
	/**
	 * This method should return the structure for the Manufacturing Plan Breakdown edition:
	 * Level 1: all Product/Feature revisions connected to the context Manufacturing Plan through Manufacturing Plan Implements
	 * Level 2: Manufacturing Plan connected to the context Manufacturing Plan through Manufacturing Plan Breakdown which belongs to the level 1 Product/Feature revision or one of its previous revisions
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getManufacturingPlanEditBreakdown(Context context, String[] args)throws Exception {
		MapList returnMapList = new MapList();
		try{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			//objectId = Product/Feature
			String objectId = (String) programMap.get("objectId");
			//parentId = Product/Feature
			String parentId = (String) programMap.get("parentOID");
			//selObjId = Manufacturing Plan
			String selObjId = (String) programMap.get("selObjId");
			boolean isFirst =false;

			//For the first call
			if(objectId.equalsIgnoreCase(parentId)){
				objectId = selObjId;
				isFirst = true;
			}

			String relationshipType = ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN;
			String objectType = ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN;
			String strFATAttribute = "to["+ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES+"].tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].attribute["+ManufacturingPlanConstants.ATTRIBUTE_FEATURE_ALLOCATION_TYPE+"]";
			String parentIdExp= "to["+ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES+"].tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id";
			StringList objectSelects = new StringList();
			objectSelects.add(DomainConstants.SELECT_ID);
			objectSelects.addElement(DomainObject.SELECT_CURRENT);
			objectSelects.addElement(strFATAttribute);
			objectSelects.addElement(parentIdExp);
			StringList relationshipSelects = new StringList();
			relationshipSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
			StringBuffer objectWhereClause = new StringBuffer();
			StringBuffer relationshipWhereClause = new StringBuffer();

			MapList relatedMPBreakdownObjects = new DomainObject(selObjId).getRelatedObjects(context,
					relationshipType,
					objectType,
					objectSelects,
					relationshipSelects,
					false,	//to relationship
					true,	//from relationship
					(short)1,
					objectWhereClause.toString(), //objectWhereClause
					relationshipWhereClause.toString(), //relationshipWhereClause
					0);			

			if(objectId!=null && !objectId.equalsIgnoreCase("")){
				DomainObject domObject = new DomainObject(objectId);
				relationshipType = ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_IMPLEMENTS;
				objectType = ManufacturingPlanConstants.TYPE_PRODUCTS;

				if(domObject.isKindOf(context,ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN)&& isFirst){
					MapList relatedImplements = domObject.getRelatedObjects(context,
							relationshipType,
							objectType,
							objectSelects,
							relationshipSelects,
							false,	//to relationship
							true,	//from relationship
							(short)1,
							DomainConstants.EMPTY_STRING, //objectWhereClause
							DomainConstants.EMPTY_STRING, //relationshipWhereClause
							0);              

					StringList revPrdLst=new StringList();
					for(int i =0;i<relatedImplements.size();i++){
						Map revMap = (Map) relatedImplements.get(i);
						returnMapList.add(revMap);
						Object revid = revMap.get(DomainConstants.SELECT_ID);
						StringList slRevId = ManufacturingPlanUtil
						.convertObjToStringList(context, revid);				 
						String prdId=ManufacturingPlanUtil.convertStringListToString(context, slRevId);			
						MapList relatedAvailableManuPlans = new MapList();
						relatedAvailableManuPlans.addAll(getAllAssociatedManufacturingPlans(context,prdId));				
						//find the MP which are connected to the father MP as Breakdown
						if(relatedAvailableManuPlans!=null && !relatedAvailableManuPlans.isEmpty()){
							Iterator relatedAvailableManuPlansItr = relatedAvailableManuPlans.iterator();
							while(relatedAvailableManuPlansItr.hasNext()){
								Map relatedAvailableManuPlan = (Map)relatedAvailableManuPlansItr.next();
								if(relatedAvailableManuPlan!=null && !relatedAvailableManuPlan.isEmpty()){
									String relatedAvailableManuPlanId = (String)relatedAvailableManuPlan.get(DomainConstants.SELECT_ID);
									if(relatedAvailableManuPlanId!=null && !relatedAvailableManuPlanId.equalsIgnoreCase("") && relatedMPBreakdownObjects!=null && !relatedMPBreakdownObjects.isEmpty()){
										Iterator relatedMPBreakdownObjectsItr = relatedMPBreakdownObjects.iterator();
										while(relatedMPBreakdownObjectsItr.hasNext()){
											Map relatedMPBreakdownObject = (Map)relatedMPBreakdownObjectsItr.next();
											if(relatedMPBreakdownObject!=null && !relatedMPBreakdownObject.isEmpty()){
												String relatedMPBreakdownObjectId = (String)relatedMPBreakdownObject.get(DomainConstants.SELECT_ID);
												String relatedMPBreakdownObjectRelId = (String)relatedMPBreakdownObject.get(DomainConstants.SELECT_RELATIONSHIP_ID);
												String current = (String)relatedMPBreakdownObject.get(DomainConstants.SELECT_CURRENT);
												if(relatedMPBreakdownObjectId!=null && !relatedMPBreakdownObjectId.equalsIgnoreCase("") && relatedMPBreakdownObjectId.equalsIgnoreCase(relatedAvailableManuPlanId)){
													relatedAvailableManuPlan.put("selectedRevId",objectId);
													relatedAvailableManuPlan.remove(DomainConstants.SELECT_RELATIONSHIP_ID);
													relatedAvailableManuPlan.put(DomainConstants.SELECT_RELATIONSHIP_ID,relatedMPBreakdownObjectRelId);
													relatedAvailableManuPlan.put(DomainConstants.SELECT_CURRENT,current);
													relatedAvailableManuPlan.put("level", "2");
													returnMapList.add(relatedAvailableManuPlan);
												}
											}
										}	
									}
								}
							}
						}
					}
				}
			}
		}catch (Exception e) {
			throw  new FrameworkException((String)e.getMessage());
		}finally{				
			return returnMapList;
		}
	}
	/**
	 * This method returns the check icon of the column Check in the table CFPEditManufacturingPlanBreakdownTable
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211
	 */
	public Vector checkConsistency(Context context , String[] args) throws Exception{
		Vector returnVector = new Vector();
		try{
			
			String iconMPBCompliant = EnoviaResourceBundle.getProperty(context,"DMCPlanning.StatusImage.MPBCompliant");
			String iconMPBCompliantWithRetrofit = EnoviaResourceBundle.getProperty(context,"DMCPlanning.StatusImage.MPBCompliantWithRetrofit");
			String iconMPBNotCompliant = EnoviaResourceBundle.getProperty(context,"DMCPlanning.StatusImage.MPBNotCompliant");
			String iconMPBORDesign = EnoviaResourceBundle.getProperty(context,"DMCPlanning.StatusImage.MPBORDesign");
			String strToolTip = DomainConstants.EMPTY_STRING;
			
			Map programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramList");
			String selObjId = (String) paramMap.get("selObjId");
			MapList objectList = (MapList) programMap.get("objectList");

			String manufacturingIntent = "";
			if(selObjId!=null && !selObjId.equalsIgnoreCase("")){
				manufacturingIntent = new DomainObject(selObjId).getAttributeValue(context, ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_INTENT);
			}
			Iterator objectListItr = objectList.iterator();
			while(objectListItr.hasNext()){
				StringBuffer sBuffer = new StringBuffer();
				Map objectMap = (Map) objectListItr.next();
				if(objectMap!=null && !objectMap.isEmpty()){
					String objectMapId = (String) objectMap.get(DomainConstants.SELECT_ID);
					String associatedId = (String) objectMap.get("to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.id");
					//String selectedRevId = (String) objectMap.get("selectedRevId");
					String selectedRevId = (String) objectMap.get("id[parent]");

					if((associatedId!=null && !associatedId.equalsIgnoreCase("")) && (selectedRevId!=null && !selectedRevId.equalsIgnoreCase(""))){
						if(!associatedId.equalsIgnoreCase(selectedRevId)){
							if(manufacturingIntent!=null && !manufacturingIntent.equalsIgnoreCase("")){
								if(manufacturingIntent.equalsIgnoreCase("Regular")){
									strToolTip=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.NotCompliant.ToolTip",context.getSession().getLanguage() );
									sBuffer.append("<center><img src=\"../common/images/" + XSSUtil.encodeForHTMLAttribute(context,iconMPBNotCompliant) + "\" border=\"0\"" + " alt=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\" title=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\" /></center>");
								}else{
									//Modified for IR-077962V6R2012
									DomainObject domObject = new DomainObject(selectedRevId);
									List listLowerRevisions=new MapList();
//									if(Product.isDerivationEnabled(context)){
										listLowerRevisions=DerivationUtil.getPreviousDerivations(context, selectedRevId, null);
										
//									}else{
//									    List listRevisions = new BooleanOptionCompatibility().getAllRevisions(context,selectedRevId);
//									    listLowerRevisions = new BooleanOptionCompatibility().getRevisions(context,selectedRevId,listRevisions,false);
//									}
									Iterator revItr=listLowerRevisions.iterator();
									StringList lowerRevIdList =new StringList();
									//StringList associatedIdList =new StringList(associatedId);
									int index=-1;
									while(revItr.hasNext()){
										Map revMap=(Map) revItr.next();
										lowerRevIdList.add(revMap.get(SELECT_ID));
									    index= lowerRevIdList.indexOf(associatedId);
									}
									if(index==-1){
										strToolTip=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.NotCompliant.ToolTip",context.getSession().getLanguage() );
										sBuffer.append("<center><img src=\"../common/images/" + XSSUtil.encodeForHTMLAttribute(context,iconMPBNotCompliant) + "\" border=\"0\"" + " alt=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\" title=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\" /></center>");
									}else{
									strToolTip=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Retrofit.ToolTip",context.getSession().getLanguage() );
									sBuffer.append("<center><img src=\"../common/images/" + XSSUtil.encodeForHTMLAttribute(context,iconMPBCompliantWithRetrofit) + "\" border=\"0\"" + " alt=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\" title=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\" /></center>");
									}
									//End of IR-077962V6R2012
								}
							}else{
								strToolTip=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.NotCompliant.ToolTip",context.getSession().getLanguage() );
								sBuffer.append("<center><img src=\"../common/images/" + XSSUtil.encodeForHTMLAttribute(context,iconMPBNotCompliant) + "\" border=\"0\"" + " alt=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\" title=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\" /></center>");
							}
						}else if(associatedId.equalsIgnoreCase(selectedRevId)){
							strToolTip=EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Compliant.ToolTip",context.getSession().getLanguage() );
							sBuffer.append("<center><img src=\"../common/images/" + XSSUtil.encodeForHTMLAttribute(context,iconMPBCompliant) + "\" border=\"0\"" + " alt=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\" title=\"" + XSSUtil.encodeForHTMLAttribute(context,strToolTip) + "\" /></center>");
						}else{
							sBuffer.append("");
						}
					}else{
						sBuffer.append("");
					}
				}else{
					sBuffer.append("");
				}
				returnVector.add(sBuffer.toString());
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			return returnVector;
		}
	}
    /**
     * This method is used to get all the check column for Manufacturing Plans for display in
     * Check column(programHTMLOutput)of the View Manufacturing Plan Matrix
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the program Map contents
     * @return List - All the Manufacturing Plans
     * @throws Exception if the operation fails
     * @since CFP R211
     */
    public List getCheckColumnForManufacturingPlanMatrix(Context context, String[] args)throws Exception
    {
        matrix.util.List manPlanList = new StringList();
        try{
       	 ManufacturingPlan manPlan = new ManufacturingPlan();
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            HashMap columnMap = (HashMap) programMap.get("columnMap");
            Map settings = (Map) columnMap.get("settings");
            String strGrpHeader = (String) settings.get("Group Header Name");
            String strGrpHeaderId = (String) columnMap.get("id");
            String strGrpHeaderTitle = (String) settings.get("Group Header");
        	String strToolTip = DomainConstants.EMPTY_STRING;
        	HashMap paramMap            = (HashMap) programMap.get("paramList");
        	String exportFormat         = (String) paramMap.get("exportFormat");
        	
        	objectList = getProductStructureForRetrofit(context,strGrpHeaderId, objectList);
        	
            String strStartCenter="<center>";
            String strEndCenter="</center>";
            String strStartTempTable = "<table>";
            String strStartTempTableRow = "<tr>";
            String strStartTempTableData = "<td>";
            String strEndTempTableData = "</td>";
            String strEndTempTableRow = "</tr>";
            String strEndTempTable = "</table>";
            String strTempAnchor = "<a TITLE=";
            String strEndHrefTitle = ">";
            String strEndHref = "</a>";
                      
            String language = context.getSession().getLanguage();
            String strStatus = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Table.Status",language);

            Map objMap = new HashMap();
            //Loop thru the objectList obtained from the expand program(Master Feature/Technical Feature List)
            for (int i = 0; i < objectList.size(); i++)
            {
            	Map tempMap = (Map) objectList.get(i);
            	Boolean masterfeature = (Boolean)tempMap.get("MasterFeature");
            	Boolean model = (Boolean)tempMap.get("Model");
            	StringList manPlanParlist = new StringList(10);
            	StringList ftrList = new StringList(10);
            	String objectId ="";
            	String mPlanIcon ="";
            	List mPlanIconList = new ArrayList();//Added for R211
            	List objectIdList = new ArrayList();
            	String strParentId = (String)tempMap.get("id[parent]");
            	String strRootNode = (String)tempMap.get("Root Node");
            	int manRevsize = 0;
            	if(tempMap.containsKey("size")){
            		manRevsize = Integer.parseInt(tempMap.get("size").toString());
				}
            	
            	if((masterfeature != null && masterfeature) || (model != null && model))
            	{
            		StringList Pl = new StringList(10);

            		//Get a list of all the Parent Manufacturing Plans for the manRevsize and put them to a StringList
            		for (int j = 0; j < manRevsize; j++)
            		{
            			objMap = (HashMap)tempMap.get("k"+j);
            			Object mPlanParId = objMap.get("ManufacturingPlanParentId");
            			if(mPlanParId instanceof StringList){
            				StringList slManPlanObj = (StringList)mPlanParId;	 		             	
            				for(int l=0;l<slManPlanObj.size();l++){
            					manPlanParlist.add(slManPlanObj.get(l));
            				}
            			}else if(mPlanParId instanceof String){
            				String slManPlanObj = (String)mPlanParId;
            				manPlanParlist.add(slManPlanObj);
            			}	        		
            			ftrList.add(objMap);
            		}
            		Map fMap = new HashMap();
            		//The context strGrpHeaderId should be present in the Parent Manufacturing Plans list.
            		//Only if it contains,display the MP
            		if(manPlanParlist.contains(strGrpHeaderId))
            		{
            			StringList sLFeatParentId = null;
            			for(int h=0;h<ftrList.size();h++)
            			{
            				fMap = (HashMap)ftrList.get(h);
            				Object mPlanIds = (Object)fMap.get("ManufacturingPlanId");
            				Object mPParentId = fMap.get("ManufacturingPlanParentId");
            				String featureId =(String)fMap.get("FeatureId");
            				if(mPParentId instanceof StringList){
            					StringList sLMPParentIds = (StringList)mPParentId;
            					for(int l=0;l<sLMPParentIds.size();l++){
            						Pl.add(sLMPParentIds.get(l));
            					}
            				}else if(mPParentId instanceof String){
            					String sLMPParentIds = (String)mPParentId;
            					Pl.add(sLMPParentIds);
            				}

            				sLFeatParentId = (StringList)fMap.get("FeatureParentId");

            				String strNameObj ="";
            				StringList strNameObjList = new StringList();
            				if(mPlanIds!=null &&Pl.contains(strGrpHeaderId) )
            				{
            					if(!sLFeatParentId.isEmpty() && sLFeatParentId.contains(strParentId))
            					{
            						strNameObjList = manPlan.getIconForCheck(context,mPlanIds,strGrpHeaderId,featureId);
            					}
            				}
            				Iterator nameItr=strNameObjList.iterator();
            				while(nameItr.hasNext()){
            					strNameObj=(String) nameItr.next();
            					if(strNameObj != null && !strNameObj.equals(""))
            					{
            						objectId = strNameObj.toString().substring(strNameObj.toString().lastIndexOf("|")+1, strNameObj.toString().length());
            						mPlanIcon= strNameObj.toString().substring(0, strNameObj.toString().lastIndexOf("|"));
            						objectIdList.add(objectId);
            						mPlanIconList.add(mPlanIcon);
            					}

            				}
            			}
            			StringBuffer sb = new StringBuffer();
            			if(!(objectIdList.isEmpty()&&mPlanIconList.isEmpty())){
            				Iterator objectIdListItr=objectIdList.iterator();
            				Iterator mPlanIconListItr=mPlanIconList.iterator();

   						 if((exportFormat != null)
								 && (exportFormat.length() > 0)
								 && ("CSV".equals(exportFormat))){
   							 boolean bisFirst = true;
   							 while(objectIdListItr.hasNext()){
   								strToolTip=(String) objectIdListItr.next();
   								 if(!bisFirst)
   									 sb =  sb.append(" | ");
   								 bisFirst = false;
   								 sb.append(XSSUtil.encodeForHTML(context,strToolTip));
   							 }
   						 }else{
            				sb =  sb.append(strStartCenter);
            				sb =  sb.append(strStartTempTable);
            				while(objectIdListItr.hasNext()&& mPlanIconListItr.hasNext()){
            					sb =  sb.append(strStartTempTableRow);
            					sb =  sb.append(strStartTempTableData);
            					strToolTip=(String) objectIdListItr.next();
            					mPlanIcon=(String)mPlanIconListItr.next();

            					if((exportFormat != null)
            							&& (exportFormat.length() > 0)
            							&& ("CSV".equals(exportFormat))){
            						if(sb.length()> 23){
            							sb.append(" | ");
            						}
            						sb.append(strToolTip);
            					}else{
            						sb.append("<center><img src=\"../common/images/" + mPlanIcon  + "\" border=\"0\"" + " alt=\"" + strToolTip + "\" title=\"" + strToolTip + "\" /></center>");
            					}

            					sb = sb.append(strEndTempTableData);
            					sb = sb.append(strEndTempTableRow);
            				}
            				sb = sb.append(strEndTempTable);
            				sb =  sb.append(strEndCenter);
   						 }
            				manPlanList.add(sb.toString());
            			} else {
            				manPlanList.add(strStatus);
            			}
            		} else {
            			manPlanList.add(strStatus);
            		}
            	}else {
            		manPlanList.add("");
            	}
            }
        }
        catch (Exception e) {
       	 // TODO: handle exception
       	// e.printStackTrace();
		}
        return manPlanList;
    }
    
    /** Trigger method to check if a Manufacturing Plan is Compliant
     * @param context
     * @param args
     * @return
     * @throws Exception
     * @since  R211
     */
    public int CheckIfCompliant(Context context, String args[])throws Exception{
		int iResult = 0;
		try{
			// get the context MP object
			String strManufacturingPlanId = args[0];
			DomainObject domContext = new DomainObject(strManufacturingPlanId);
			String manufacturingIntent="";
			boolean boolMPBCompliant = false;
			boolean boolMPBCompliantWithRetrofit = false;
			boolean boolMPBNotCompliant =false;
				
			if(strManufacturingPlanId!=null && !strManufacturingPlanId.equalsIgnoreCase("")&& domContext.isKindOf(context,ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN)){
				ManufacturingPlan manuPlan =new ManufacturingPlan(strManufacturingPlanId);
                MapList unresolvedDesignObjects = new MapList();
                StringList mpBrkDownStruct=new StringList(); 
                StringList mpImplements=new StringList(); 
                manufacturingIntent = new DomainObject(strManufacturingPlanId).getAttributeValue(context, ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_INTENT);
			 
                    	mpBrkDownStruct = manuPlan.getInfoList(context, "from["+ ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN + "].to.id");
                    	mpImplements = manuPlan.getInfoList(context, "from["+ ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_IMPLEMENTS + "].to.id");
                        StringList assManPlan=new StringList();
                        if(!mpBrkDownStruct.isEmpty()){
                    	for(int j=0; j<mpBrkDownStruct.size();j++){
                    		String manId=(String) mpBrkDownStruct.get(j);
                    		DomainObject domMP = new DomainObject(manId);
                    		String strPrd = domMP.getInfo(context, "to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.id");
                    	
                    		
                    	if(mpImplements.contains(strPrd)){
                    		HashMap mp=new HashMap();
                    		mp.put("objectId", strPrd);
                    		String [] jpoArgs=JPO.packArgs(mp);
                    		ManufacturingPlanSearchBase_mxJPO mpSearch=new ManufacturingPlanSearchBase_mxJPO(context,jpoArgs );
                    		StringList strList=mpSearch.getMPofCurrentRevision(context,jpoArgs);
                    		StringList strListRetro=mpSearch.getMPofCurrentAndAllPreviousRevisions(context, jpoArgs);
                    		
                    		if(manufacturingIntent.equalsIgnoreCase("Retrofit")){
                        		if(strList.contains(manId)){
                        			boolMPBCompliant=true;
                        		}else if(strListRetro.contains(manId)){
                        			boolMPBCompliantWithRetrofit=true;
                        		}else{boolMPBNotCompliant=true;}
                        		}else if (manufacturingIntent.equalsIgnoreCase("Regular")){
                        			if(strList.contains(manId)){		                        				
                        				boolMPBCompliant=true;
                        			}else{boolMPBNotCompliant=true;}
                        		}
                    	}else{
                    		MapList prdSibMap = ManufacturingPlan.getPrdSibling(context,strPrd);
                    		Iterator prdItr=prdSibMap.iterator();
                    		String prdSiblId="";
                    		  while(prdItr.hasNext())
                    		  {
								Map prodcutMap = (Map) prdItr.next();
								if (prodcutMap != null && !prodcutMap.isEmpty()) {
									prdSiblId = (String) prodcutMap
											.get(SELECT_ID);
								}
								if (mpImplements.contains(prdSiblId)) {
									HashMap mp = new HashMap();
									mp.put("objectId", prdSiblId);
									String[] jpoArgs = JPO.packArgs(mp);
									ManufacturingPlanSearchBase_mxJPO mpSearch = new ManufacturingPlanSearchBase_mxJPO(
											context, jpoArgs);
									StringList strList = mpSearch
											.getMPofCurrentRevision(context,
													jpoArgs);
									StringList strListRetro = mpSearch
											.getMPofCurrentAndAllPreviousRevisions(
													context, jpoArgs);

									if (manufacturingIntent
											.equalsIgnoreCase("Retrofit")) {
										if (strList.contains(manId)) {
											boolMPBCompliant = true;
										} else if (strListRetro.contains(manId)) {
											boolMPBCompliantWithRetrofit = true;
										} else {
											boolMPBNotCompliant = true;
										}
									} else if (manufacturingIntent
											.equalsIgnoreCase("Regular")) {
										if (strList.contains(manId)) {
											boolMPBCompliant = true;
										} else {
											boolMPBNotCompliant = true;
										}
									}
								}
							}
						}

					}

				}
			}
			if (boolMPBNotCompliant)
			{
				iResult=1;
	            String language = context.getSession().getLanguage();
	            
	            // Getting the String to be Displayed in the Error alert.
	            String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Alert.PolicyManufacturingPlanStatePreliminaryPromoteCheckIfCompliant",language);
	            // Displaying the pop-up alert on the screen
	            emxContextUtilBase_mxJPO.mqlNotice(context, strAlertMessage);
			}
		}catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());		
		}
		return iResult;
	}

	@com.matrixone.apps.framework.ui.PostProcessCallable
	public Map refreshManufacturingPlanEditBreakdown(Context context,
			String[] args) throws Exception {
		Map programMap = (Map) JPO.unpackArgs(args);

		// START -Added:1-Dec-2010:ixe: IR-084235V6R2012
		HashMap paramMap = (HashMap) programMap.get("paramMap");
		String objectId = (String) paramMap.get("objectId");
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String selObjId = (String) requestMap.get("selObjId");
		String language = context.getSession().getLanguage();
		String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.Alert.PreferedValueConfirm",language);
		DomainObject domSelObj = new DomainObject(selObjId);

		String strManIntent = domSelObj.getAttributeValue(context, ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_INTENT);
		
		String relationshipType = ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN
		+ "," + ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_IMPLEMENTS;

		String objectType = ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN 
		+ "," + ManufacturingPlanConstants.TYPE_PRODUCTS;

		final String SELECT_FEATURE_ID = "to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].from.id";
		final String SELECT_FEATURE_NAME = "to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].from.name";
		final String SELECT_FEATURE_PARENT_ID = "to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].from.to["+ ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION + "].from.id"; 
		final String SELECT_FEATURE_PARENT_NAME = "to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].from.to["+ ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION + "].from.name"; 
		final String SELECT_PRODUCT_PARENT_ID = "to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].from.to["+ ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS + "].from.id"; 
		final String SELECT_PRODUCT_PARENT_NAME = "to["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].from.to["+ ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS + "].from.name"; 

		StringList objectSelects = new StringList();
		objectSelects.add(SELECT_ID);
		objectSelects.add(SELECT_NAME);
		objectSelects.add(SELECT_FEATURE_ID);
		objectSelects.add(SELECT_FEATURE_NAME);
		objectSelects.add(SELECT_FEATURE_PARENT_ID);
		objectSelects.add(SELECT_FEATURE_PARENT_NAME);
		objectSelects.add(SELECT_PRODUCT_PARENT_ID);
		objectSelects.add(SELECT_PRODUCT_PARENT_NAME);
		StringList relationshipSelects = new StringList();
		relationshipSelects.add("attribute["+ ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_PLAN_PREFERRED + "]");
		relationshipSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

		// Getting the objects which are connected by Manufacturing Plan
		// Breakdown relationship to the Manufacturing Plan object
		MapList relatedMPBreakdownObjects = domSelObj.getRelatedObjects(
				context, relationshipType, objectType, objectSelects,
				relationshipSelects, false, // to
				true, // from relationship
				(short) 1, null, // objectWhereClause
				null, // relationshipWhereClause
				0);

		boolean isPrefYes = true;
		
		MapList MPBmap = new MapList();
		MapList MPImap = new MapList();

		MapList newMPBmap = new MapList();
		StringList backwardStructList = new StringList();


		for(int n=0; n<relatedMPBreakdownObjects.size(); n++)
		{
			Map map = (Map) relatedMPBreakdownObjects.get(n);
			String relvalue = (String) map.get("relationship");
			if(relvalue.equals(ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN))
			{
				MPBmap.add(map);				
			}
			if(relvalue.equals(ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_IMPLEMENTS))
			{
				MPImap.add(map);
			}
		}
		
		// If there is only one Manufacturing Plan connected with Manufacturing
		// Plan Breakdown relationship,
		// and the Preferred Attribute value on the relationship is set to "No"
		// then setting it to "Yes"
		if (MPBmap.size() != 0
				&& MPBmap.size() == 1) {
				Map mpMap = (Map) MPBmap.get(0);
				String connId = (String) mpMap
				.get(DomainConstants.SELECT_RELATIONSHIP_ID);
				DomainRelationship domrelBD = new DomainRelationship(connId);
				domrelBD
				.setAttributeValue(
						context,
						ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_PLAN_PREFERRED,
						"Yes");
		}else if(MPBmap.size() != 0)
		{				
			for(int i=0; i<MPImap.size(); i++)
			{
				Boolean flag = false;
				int count =0;
				Map productMap = (Map) MPImap.get(i);
				String productId = (String) productMap.get(DomainObject.SELECT_ID);
				if(strManIntent.equalsIgnoreCase(ManufacturingPlanConstants.RANGE_VALUE_RETROFIT))
				{
					backwardStructList.addAll(ManufacturingPlan.getBackwardDerivationChain(context, productId));
				}
				for(int k=0; k<MPBmap.size(); k++)
				{
					Map MPBproductMap = (Map) MPBmap.get(k);
					String pId = (String) MPBproductMap.get("to[" +
							ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].from.id");
					if((strManIntent.equalsIgnoreCase(ManufacturingPlanConstants.RANGE_VALUE_REGULAR) && productId.equals(pId))
							||
						(strManIntent.equalsIgnoreCase(ManufacturingPlanConstants.RANGE_VALUE_RETROFIT) && backwardStructList.contains(pId)))
					{
						newMPBmap.add(MPBproductMap);						
					}				
				}

				for(int j=0; j<newMPBmap.size(); j++)
				{
					Map mpMap = (Map) newMPBmap.get(j);
					String currentPrefferedState = (String)mpMap.get(ManufacturingPlanConstants.SELECT_ATTRIBUTE_MANUFACTURING_PLAN_PREFERRED);
					if (ManufacturingPlanConstants.RANGE_VALUE_YES.equalsIgnoreCase(currentPrefferedState)){
						flag = true;
						count++;
					}					
				}
				if(count>1){
					throw new Exception(strAlertMessage);
				}
				if (!flag && newMPBmap.size() != 0){
					Map tempMap = (Map) newMPBmap.get((newMPBmap.size()-1));
					String connId = (String) tempMap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
					DomainRelationship domrelBD = new DomainRelationship(connId);
					domrelBD.setAttributeValue(context,
							ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_PLAN_PREFERRED,
							ManufacturingPlanConstants.RANGE_VALUE_YES);
				}
				newMPBmap.clear();
				backwardStructList.clear();
			}			
		}
		//Added getTopWindow().close();top.parent.opener.parent.location.href=getTopWindow().parent.opener.parent.location.href
		Map returnMap = new HashMap();
		returnMap.put("Action", "execScript");
		returnMap.put("Message","{ main:function __main(){refreshParentwindowInManufacturingPlan()}}");
		return returnMap;
	}

	/**
	 * This method is used to get the range values for Preferred Attribute.
	 * 
	 * @param context
	 *            The ematrix context object.
	 * @param String
	 *            [] The args .
	 * @return TreeMap with the range values.
	 * @since CFPR211
	 */
	
    public Map getRangeValuesForPreferred(Context context,String[] args) throws Exception 
    {
        String strAttributeName = ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_PLAN_PREFERRED;
        HashMap rangeMap = new HashMap();
        matrix.db.AttributeType attribName = new matrix.db.AttributeType(
                strAttributeName);
        attribName.open(context);

        List attributeRange = attribName.getChoices();
        List attributeDisplayRange = i18nNow
                .getAttrRangeI18NStringList(
                		ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_PLAN_PREFERRED,
                        (StringList) attributeRange, context.getSession()
                                .getLanguage());
        rangeMap.put("field_choices", attributeRange);
        rangeMap.put("field_display_choices", attributeDisplayRange);
        return rangeMap;
    }
    
    /**
     * This method is used to get the range values for Manufacturing Intent
     * Attribute.
     *
     * @param context
     *            The ematrix context object.
     * @param String[]
     *            The args .
     * @return TreeMap with the range values.
     * @since CFPR211
     */
    public TreeMap getRangeValuesForManufacturingIntent(Context context,
            String[] args) throws Exception {
    	
    	String strLanguage = context.getSession().getLanguage();
    	String strMPIntentI18Now = null;
        String strMPIntent = null;
        String strMPIntentValueKey = null;
        String strMPIntentDisplay = null;
        char chBlank = ' ';
        char chUnderScore = '_';
        i18nNow i18nnow = new i18nNow();
        
        String strAttributeName = ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_INTENT;
        TreeMap rangeMap = new TreeMap();
        matrix.db.AttributeType attribName = new matrix.db.AttributeType(
                strAttributeName);
        attribName.open(context);
        // actual range values
        List attributeRange = attribName.getChoices();
        // display range values
        List attributeDisplayRange = i18nNow
                .getAttrRangeI18NStringList(
                        ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_INTENT,
                        (StringList) attributeRange, context.getSession()
                                .getLanguage());
        for (int i = 0; i < attributeRange.size(); i++) {
            strMPIntent = (String) attributeRange.get(i);
            strMPIntentI18Now = strMPIntent.replace(
                    chBlank, chUnderScore);
            strMPIntentValueKey = "emxFramework.Range.Manufacturing_Intent."
                    + strMPIntentI18Now.toString();
            strMPIntentDisplay = EnoviaResourceBundle.getProperty(context,"Framework",strMPIntentValueKey,strLanguage);
            rangeMap.put(strMPIntentDisplay,
                    (String) attributeRange.get(i));
        }

        return rangeMap;

    }// end of the method
    
	/** This method returns Associated Manufacturing Plans of all revisions of Products/Features
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211, added for IR-077962V6R2012 - refactored in R212 Derivations
	 */
	
	private MapList getAllAssociatedManufacturingPlans(Context context, String objectId)throws Exception {
		MapList returnMapList = new MapList();
		try{
			if(objectId!=null && !objectId.equalsIgnoreCase("")){
				StringList objectSelects = new StringList();
				objectSelects.add(DomainObject.SELECT_ID);
				objectSelects.add(DomainObject.SELECT_TYPE);
				objectSelects.add(DomainObject.SELECT_NAME);
				objectSelects.add("to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.id");
				objectSelects.add("to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.type");
				objectSelects.add("to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.name");
				objectSelects.add("to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.revision");	

				StringList relationshipSelects = new StringList();
				relationshipSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
				String masterId=new ManufacturingPlan().getMasterFromContext(context,objectId);
				DomainObject domMasterObj=new DomainObject(masterId);
				
				String strRelPattern= ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS +","+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN;
				String strTypePattern= ManufacturingPlanConstants.TYPE_PRODUCTS +","+ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN;
				//Pattern strTypeIncludePattern=new Pattern(ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN);
	            StringBuffer sbTypeIncludePattern = new StringBuffer();
	            List lstManufacturingPlanChildTypes = ProductLineUtil.getChildrenTypes(context, ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN);
	            sbTypeIncludePattern.append(ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN);
	            if(lstManufacturingPlanChildTypes.size()>0)
	              sbTypeIncludePattern.append(COMMA);
	            for (int i = 0; i < lstManufacturingPlanChildTypes.size(); i++) {
	               sbTypeIncludePattern.append(lstManufacturingPlanChildTypes.get(i));
	               if (i != lstManufacturingPlanChildTypes.size() - 1) {
	                    	sbTypeIncludePattern.append(COMMA);
	               }
	            }
				Pattern strTypeIncludePattern=new Pattern(sbTypeIncludePattern.toString());
				returnMapList = domMasterObj.getRelatedObjects(context,
						strRelPattern,
						strTypePattern,
						objectSelects,
						relationshipSelects,
						false,	//to relationship
						true,	//from relationship
						(short)2,
						DomainConstants.EMPTY_STRING, //objectWhereClause
						DomainConstants.EMPTY_STRING, //relationshipWhereClause
						0,
						strTypeIncludePattern,
						null,
						null);
			}
		}catch (Exception e) {
			throw  new FrameworkException((String)e.getMessage());
		}finally{
			return returnMapList;
		}
	}
	/** 
	 * 
	 * @param context
	 * @param strGrpHeaderId
	 * @param objectList
	 * @return
	 * @throws FrameworkException
	 *  refactored in R212 Derivations
	 */
	 
	private MapList getProductStructureForRetrofit(Context context,String strGrpHeaderId, MapList objectList)throws FrameworkException{
		try{
			for(int h=0;h <objectList.size();h++){
				DomainObject dmoGroupHeader = new DomainObject(strGrpHeaderId);
				String strMPIntent = dmoGroupHeader.getAttributeValue(context, ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_INTENT);
				Map m1= new HashMap();
				m1 = (Map)objectList.get(h);
				StringList Pl = new StringList();
				Map objMap = new HashMap();
				int manRevsize = 0;
				if(m1.containsKey("size")){
					manRevsize = Integer.parseInt(m1.get("size").toString());
				} 
				String strType ="";
				String strProductStructureMPId = null;
				for (int j = 0; j < manRevsize; j++){
					objMap = (HashMap)m1.get("k"+j);
					if(objMap!=null){
						Object mPlanIds = (Object)objMap.get("ManufacturingPlanId");
						strType = (String)objMap.get("FeatureType");
						strProductStructureMPId = (String)objMap.get("FeatureId");
						StringList slMPIds=ManufacturingPlanUtil.convertObjToStringList(context, mPlanIds);
						Pl.addAll(slMPIds);
					}
				}
				//if(strMPIntent.equalsIgnoreCase(ManufacturingPlanConstants.RANGE_VALUE_RETROFIT)){
					if(strType!=null && mxType.isOfParentType(context, strType,ManufacturingPlanConstants.TYPE_PRODUCTS)){
						if(null != strProductStructureMPId && !"null".equals(strProductStructureMPId) && !"".equals(strProductStructureMPId)){
							DomainObject dmoProduct = new DomainObject(strProductStructureMPId);
							final String SELECT_PARENT_PRODUCT = "to["+ ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].from.id";
							final String SELECT_MPI_FROM = "to["+ ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_IMPLEMENTS+ "].from.id";
							final String SELECT_CONTEXT_PRODUCT = "to["+ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES+"].tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id";
							final String SELECT_PARENT_MAIN_PRODUCT = ("to["+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.id");
							String strParentProductId="";
							StringList slObjectSelcts=new StringList(SELECT_PARENT_PRODUCT);
							slObjectSelcts.addElement(SELECT_MPI_FROM);
							slObjectSelcts.addElement(SELECT_CONTEXT_PRODUCT);
							
							DomainObject.MULTI_VALUE_LIST.add(SELECT_MPI_FROM);
							DomainObject.MULTI_VALUE_LIST.add(SELECT_CONTEXT_PRODUCT);
							Map  mapContext = dmoProduct.getInfo(context, slObjectSelcts);
							DomainObject.MULTI_VALUE_LIST.remove(SELECT_MPI_FROM);
							DomainObject.MULTI_VALUE_LIST.remove(SELECT_CONTEXT_PRODUCT);
							
							if(mapContext.containsKey(SELECT_PARENT_PRODUCT))
								strParentProductId = (String)mapContext.get(SELECT_PARENT_PRODUCT);
							else if(mapContext.containsKey(SELECT_PARENT_MAIN_PRODUCT))
								strParentProductId = (String)mapContext.get(SELECT_PARENT_MAIN_PRODUCT);

							StringList slMPlanParIds=(StringList)mapContext.get(SELECT_MPI_FROM);
							StringList slFeatParIds=(StringList)mapContext.get(SELECT_CONTEXT_PRODUCT);
							
//							DomainObject dmoParentPRoduct = new DomainObject(strParentProductId);
//							final String SELECT_MANAGED_FEATURES = ("from["+ ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].to.id");
//							StringList slProduct =  dmoParentPRoduct.getInfoList(context, SELECT_MANAGED_FEATURES);
							StringList slProduct =new Model(strParentProductId).getManagedRevisions(context);
							String[] strArrPrdIds = new String[slProduct.size()];
							strArrPrdIds = (String[]) slProduct.toArray(strArrPrdIds);

							final String SELECT_CONNECTED_MANUFACTURING_PLAN = ("from["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].to.id");
							final String SELECT_CONNECTED_MANUFACTURING_PLAN_NAME = ("from["+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].to.name");

							StringList slObjSels=new StringList(DomainObject.SELECT_TYPE);
							slObjSels.addElement(DomainObject.SELECT_ID);
							slObjSels.addElement(SELECT_CONNECTED_MANUFACTURING_PLAN);
							slObjSels.addElement(SELECT_CONNECTED_MANUFACTURING_PLAN_NAME);
							DomainObject.MULTI_VALUE_LIST.add(SELECT_CONNECTED_MANUFACTURING_PLAN);
							DomainObject.MULTI_VALUE_LIST.add(SELECT_CONNECTED_MANUFACTURING_PLAN_NAME);
							MapList mlProductsMPs=DomainObject.getInfo(context, strArrPrdIds, slObjSels);
							DomainObject.MULTI_VALUE_LIST.remove(SELECT_CONNECTED_MANUFACTURING_PLAN);
							DomainObject.MULTI_VALUE_LIST.remove(SELECT_CONNECTED_MANUFACTURING_PLAN_NAME);
							
							for(int k=0;k<mlProductsMPs.size();k++){
								Map mapProductRev = (Map)mlProductsMPs.get(k);
								 if(m1.containsKey("size")){
									 manRevsize = Integer.parseInt(m1.get("size").toString());
								 }
//								DomainObject dmoProductRev = new DomainObject(strProductRev);
//								StringList slManufPlans =  dmoProductRev.getInfoList(context, SELECT_CONNECTED_MANUFACTURING_PLAN);
//								StringList slManufPlanNames =  dmoProductRev.getInfoList(context, SELECT_CONNECTED_MANUFACTURING_PLAN_NAME);
//								StringList strMPlanParId = (StringList)dmoProduct.getInfoList(context, "to["+ ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_IMPLEMENTS+ "].from.id");
//								StringList strFeatParId = (StringList)dmoProduct.getInfoList(context, "to["+ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES+"].tomid["+ManufacturingPlanConstants.RELATIONSHIP_PRODUCT_FEATURE_LIST+"].from.id");
//								String strFeatureType = dmoProductRev.getInfo(context, SELECT_TYPE);
								List slManufPlans=new StringList();
								if(mapProductRev.containsKey(SELECT_CONNECTED_MANUFACTURING_PLAN))
									slManufPlans=(StringList)mapProductRev.get(SELECT_CONNECTED_MANUFACTURING_PLAN);
								List slManufPlanNames= new StringList();
								if(mapProductRev.containsKey(SELECT_CONNECTED_MANUFACTURING_PLAN_NAME))
									slManufPlanNames=(StringList)mapProductRev.get(SELECT_CONNECTED_MANUFACTURING_PLAN_NAME);
									String strProductRev=(String)mapProductRev.get(DomainObject.SELECT_ID);
									String strFeatureType=(String)mapProductRev.get(DomainObject.SELECT_TYPE);
									Map newMap = new HashMap();
									boolean bSize = false;
									newMap.put("ManufacturingPlanId",slManufPlans);
									newMap.put("ManufacturingPlanTitle",slManufPlanNames);
									newMap.put("ManufacturingPlanParentId",slMPlanParIds);
									newMap.put("FeatureParentId",slFeatParIds);
									newMap.put("FeatureId",strProductRev);
									newMap.put("FeatureType",strFeatureType);

									for(int n=0;n<slManufPlans.size();n++){
										if(!Pl.contains(slManufPlans.get(n))){
											m1.put("k"+manRevsize,newMap);
											bSize = true;
										}
									}
									if(bSize){
										int nSize = (Integer)m1.get("size");
										m1.put("size",nSize+1);
									}
							}
						}
					}
				//}
			}
		}catch (Exception e) {
			e.printStackTrace();
			throw  new FrameworkException(e);
		}
		return objectList; 
	}
	 /**
	  * Method for disconnect the ManufacturingPlan relationship of Logical Feature
	  * @param context
	  * @param args
	  * @throws Exception
	  */
		public void disconnectManufacturingPlanOnLFReplace(Context context, String[] args)
		throws Exception {

			try {
				 ArrayList programMap = (ArrayList)JPO.unpackArgs(args);
				 String strSourceLFID = (String) programMap.get(0);
				 String strParentId = (String) programMap.get(1);
					int iLevel = 10;
					int iLimit = 0; //Integer.parseInt(EnoviaResourceBundle.getProperty(
							//context, "emxConfiguration.ExpandLimit"));
					String Id = "";
				
					StringList objeSelects = new StringList();
					StringBuffer sb = new StringBuffer();
					sb.append("to[");
					sb.append( ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN);
					sb.append("].id");
					objeSelects.add(sb.toString());
					ManufacturingPlanUtil confUtil = new ManufacturingPlanUtil(strSourceLFID);
					MapList Result = confUtil
							.getObjectStructure(
									context,
									ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN,
									ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN,
									objeSelects, new StringList(), true, true, 0,
									0,"", "", (short) 1, "");
					if(Result.size()!=0){
						for(int i=0;i<Result.size();i++)
						{
					  Map connectIdMap = (Map) Result.get(i);			
					  StringList relId = new StringList();
					  StringBuffer sb_id=new StringBuffer();
					  sb_id.append("to[");
					  sb_id.append(ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN);
					  sb_id.append("].id");
					  String strMPBId=sb_id.toString();
					  Object relConnection = connectIdMap.get(strMPBId);												
					if (relConnection != null) {
						if (relConnection instanceof String) {
							relId = new StringList((String) relConnection);							
						} else {
							relId = (StringList) relConnection;
						}
						
						String[] strArrObjIds = new String[relId.size()];
						strArrObjIds = (String[]) relId.toArray(strArrObjIds);
						DomainRelationship.disconnect(context, strArrObjIds);
					}			
						}
				}
	        } catch (Exception e) {
				throw new FrameworkException(e.getMessage());
			}
	    }
		/**
		 * Method for disconnect the MPB on removing of Product
		 * @param context
		 * @param args
		 * @return
		 * @throws FrameworkException
		 */
	public int disconnectManufacturingPlanOfProduct(Context context,
			String args[]) throws FrameworkException {
		int iResult = 0;
		// retrieve the RPE variable value for design choice
		String designValue = PropertyUtil.getGlobalRPEValue(context,
		"designChoice");
		String fromDesignEffectivity = PropertyUtil.getGlobalRPEValue(context,
		"fromDesignEffectivity");
		String slist = PropertyUtil.getGlobalRPEValue(context,"selectedlist");
		String fromDesignMatrix = PropertyUtil.getGlobalRPEValue(context,"fromDesignMatrix");
		String matrixMasterId = PropertyUtil.getGlobalRPEValue(context,"masterId");

		try {
			// get the context object which is deleted from the trigger.
			String strToID = args[0];
			String strFromID = args[1];
			String strLogicalFeatureRelID = args[2];

			DomainObject domobj = new DomainObject(strFromID);
			Set sListMPRelIds = new HashSet();
			List sList = new StringList();
			String mpList = "";
			StringList allmpList = new StringList();
			MapList mlMPDerivation = new MapList();

			/*		
			 * Retrieve Indent of MP and check if it is Retrofit MP																
			 *	If RetrofitMP then get MPs connected to it through MP Breakdown														
			 *	Step I -	For each MP, connected through MP Breakdown, find its Product using Associated MP relationship			
			 *	Step II -   Also, find out Relationship ID of the same.																
			 *	Step III -	Retrieve Backward derivation Chain of de-selected product from design effectivity						
			 *	if "Backward derivation Chain from Step III" contains "Product from Step I" then add "Relationship ID from Step II" 
			 *	to array, elements of which will be disconnected.   																
			 */	 																											

			StringList slEffectivitySelections = new StringList();
			if(UIUtil.isNotNullAndNotEmpty(slist))
			{
				StringTokenizer strTok = new StringTokenizer(slist, ",");
				String strKey = "";
				String strTempManagedRevisionId="";
				while(strTok.hasMoreTokens())
				{
					strKey = strTok.nextToken();
					if(strKey != null && !"".equalsIgnoreCase(strKey) && !strKey.equalsIgnoreCase("[]"))
					{
						strTempManagedRevisionId = strKey.substring(1,strKey.indexOf("|"));
						slEffectivitySelections.add(strTempManagedRevisionId);
					}   
				}
			}

			String Indent = "";
			String prdID = "";
			StringList backwardStructList = new StringList();
			MapList mlProductMPs = new MapList();

			StringBuffer prdBuffer=new StringBuffer();
			prdBuffer.append("to[");
			prdBuffer.append(ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN);
			prdBuffer.append("].from.id");
			final String SELECT_CONNECTED_MANUFACTURING_PLAN_FROM = prdBuffer.toString();

			StringBuffer stbRelSelect = new StringBuffer(50);
			stbRelSelect.append(ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN);

			StringBuffer sbSelect=new StringBuffer();
			sbSelect.append("from[");
			sbSelect.append(ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN);
			sbSelect.append("].to.to[");
			sbSelect.append(ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN);
			sbSelect.append("].from.id");
			String strSelect=sbSelect.toString();

			StringBuffer stbTypeSelect = new StringBuffer(50);
			stbTypeSelect.append(ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN);

			StringList selectStmts = new StringList();
			selectStmts.addElement(DomainObject.SELECT_ID);
			selectStmts.addElement(strSelect);
			selectStmts.addElement(ManufacturingPlanConstants.SELECT_ATTRIBUTE_MANUFACTURING_INTENT);

			StringList relSelects = new StringList(1);

			StringBuffer mpmbuffer=new StringBuffer();
			mpmbuffer.append("to[");
			mpmbuffer.append(ManufacturingPlanConstants.RELATIONSHIP_SERIESMASTER);
			mpmbuffer.append("].from.from[");
			mpmbuffer.append(ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION);
			mpmbuffer.append("].to.id");
			final String SELECT_CONNECTED_MANUFACTURING_PLAN_FROM_MASTER_MODEL = mpmbuffer.toString();

			StringBuffer mpmbufferroot=new StringBuffer();
			mpmbufferroot.append("to[");
			mpmbufferroot.append(ManufacturingPlanConstants.RELATIONSHIP_SERIESMASTER);
			mpmbufferroot.append("].from.from[");
			mpmbufferroot.append(ManufacturingPlanConstants.RELATIONSHIP_MANAGED_ROOT);
			mpmbufferroot.append("].to.id");
			final String SELECT_CONNECTED_ROOT_MANUFACTURING_PLAN_FROM_MASTER_MODEL = mpmbufferroot.toString();

			mlProductMPs = domobj.getRelatedObjects(context, stbRelSelect
					.toString(), stbTypeSelect.toString(), selectStmts,
					relSelects, false, true, (short) 1, EMPTY_STRING, null,
					0, null, null, null);
			List sListMPIds = new StringList();
			for (int i = 0; i < mlProductMPs.size(); i++) {
				Map mMPInfo = null;
				mMPInfo = (Map) mlProductMPs.get(i);
				String strProductMPId = (String) mMPInfo.get(DomainConstants.SELECT_ID);
				sListMPIds.add(strProductMPId);
				DomainObject dmoMP = new DomainObject(strProductMPId);
				// rel Pattern
				StringBuffer stbRelSelect1 = new StringBuffer(50);
				stbRelSelect1
				.append(ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN);
				// type Pattern
				StringBuffer stbTypeSelect1 = new StringBuffer(50);
				stbTypeSelect1
				.append(ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN);
				// Object Selects
				StringList selectStmts1 = new StringList();
				selectStmts1.addElement(DomainObject.SELECT_ID);	
				// Relationship selects
				StringList relSelects1 = new StringList(1);
				relSelects1.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
				Indent = (String) mMPInfo.get(ManufacturingPlanConstants.SELECT_ATTRIBUTE_MANUFACTURING_INTENT);
				if (Indent.equalsIgnoreCase(ManufacturingPlanConstants.RANGE_VALUE_RETROFIT)) {
					if (fromDesignEffectivity.equalsIgnoreCase("True"))
					{
						if(slEffectivitySelections.size() != 0)
						{
							for(int k=0; k<slEffectivitySelections.size(); k++)
							{
								String selection = (String) slEffectivitySelections.get(k);					
								backwardStructList.addAll(ManufacturingPlan.getBackwardDerivationChain(context, selection));
							}
						}
						MapList mLMPinfo = new MapList();
						mLMPinfo = dmoMP.getRelatedObjects(context,
								stbRelSelect1.toString(), stbTypeSelect1
								.toString(), selectStmts1, relSelects1,
								false, true, (short) 1,
								DomainConstants.EMPTY_STRING, null, 0, null,
								null, null);
						Map mMPInfo1 = null;
						for (int m = 0; m < mLMPinfo.size(); m++) {
							mMPInfo1 = (Map) mLMPinfo.get(m);
							String strMPId = (String) mMPInfo1.get(DomainConstants.SELECT_ID);
							String strRelId = (String) mMPInfo1.get(DomainConstants.SELECT_RELATIONSHIP_ID);
							DomainObject dObj = new DomainObject(strMPId);
							//TODO : Can we avoid getInfo call (GN1)
							prdID = dObj.getInfo(context, SELECT_CONNECTED_MANUFACTURING_PLAN_FROM);
							if(slEffectivitySelections.size() == 0 || !backwardStructList.contains(prdID))
							{
								sList.add(strRelId);
							}					
						}	
					}
					if (fromDesignMatrix.equalsIgnoreCase("True"))
					{						
						DomainObject modelObject = new DomainObject(matrixMasterId);
						allmpList = (StringList) modelObject.getInfoList(context, SELECT_CONNECTED_MANUFACTURING_PLAN_FROM_MASTER_MODEL);
						mpList = (String) modelObject.getInfo(context, SELECT_CONNECTED_ROOT_MANUFACTURING_PLAN_FROM_MASTER_MODEL);
						allmpList.add(0, mpList);

						if(slEffectivitySelections.size() != 0)
						{
							for(int k=0; k<slEffectivitySelections.size(); k++)
							{
								String selection = (String) slEffectivitySelections.get(k);					
								backwardStructList.addAll(ManufacturingPlan.getBackwardDerivationChain(context, selection));
							}
						}
						MapList mLMPinfo = new MapList();
						mLMPinfo = dmoMP.getRelatedObjects(context,
								stbRelSelect1.toString(), stbTypeSelect1
								.toString(), selectStmts1, relSelects1,
								false, true, (short) 1,
								DomainConstants.EMPTY_STRING, null, 0, null,
								null, null);
						Map mMPInfo1 = null;
						for (int m = 0; m < mLMPinfo.size(); m++) {
							mMPInfo1 = (Map) mLMPinfo.get(m);
							String strMPId = (String) mMPInfo1.get(DomainConstants.SELECT_ID);
							if(allmpList.contains(strMPId))
							{
								String strRelId = (String) mMPInfo1.get(DomainConstants.SELECT_RELATIONSHIP_ID);
								DomainObject dObj = new DomainObject(strMPId);
								prdID = dObj.getInfo(context, SELECT_CONNECTED_MANUFACTURING_PLAN_FROM);
								if(slEffectivitySelections.size() == 0 || !backwardStructList.contains(prdID))
								{
									sList.add(strRelId);
								}
							}
						}
					}
				}

				sListMPRelIds.addAll(sList);

				if (!Indent.equalsIgnoreCase(ManufacturingPlanConstants.RANGE_VALUE_RETROFIT)) {
					DomainObject domObjToDisconnect = new DomainObject(strToID);
					StringList slMPBConnections = domObjToDisconnect
					.getInfoList(
							context,
							"from["
							+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
							+ "].to.to["
							+ ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN
							+ "].id");

					StringList selects = new StringList();
					selects.addElement(DomainConstants.SELECT_FROM_ID);
					selects.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
					String[] connection = new String[slMPBConnections.size()];
					StringList relConnections = new StringList();			
					for(int l=0; l< slMPBConnections.size(); l++)
					{
						connection[l] = slMPBConnections.get(l).toString();
					}
					MapList objId = DomainRelationship.getInfo(context, connection, selects);

					for(int m =0; m<objId.size(); m++)
					{
						Map mpmap = (Map)objId.get(m);
						String id = (String) mpmap.get(DomainConstants.SELECT_FROM_ID);
						String relId = (String) mpmap.get(DomainConstants.SELECT_RELATIONSHIP_ID);
						DomainObject dObj = newInstance(context, id);
						StringList slObjSel = new StringList(ManufacturingPlanConstants.SELECT_ATTRIBUTE_MANUFACTURING_INTENT);
						Map mpIndent = dObj.getInfo(context, slObjSel);
						String indent = (String) mpIndent.get(ManufacturingPlanConstants.SELECT_ATTRIBUTE_MANUFACTURING_INTENT);

						if(!indent.equalsIgnoreCase(ManufacturingPlanConstants.RANGE_VALUE_RETROFIT))
						{
							relConnections.addElement(relId);
						}			
					}

					// get all the Manufacturing Plans Breakdown RelIds connected
					StringBuffer sb = new StringBuffer();
					sb.append("from[");
					sb
					.append(ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN);
					sb.append("].id");
					StringList slObjSelect = new StringList();
					slObjSelect.add(sb.toString());
					String relPattern = ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
					+ ","
					+ ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN;
					String objPattern = ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN;
					String objectWhere = "to["
						+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
						+ "].from.id=="
						+ strFromID
						+ "||to["
						+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
						+ "].from.id==" + strToID + "";
					// getting the MPB's Ids
					MapList result = domobj.getRelatedObjects(context, relPattern,
							objPattern, slObjSelect, null, false, true, (short) 2,
							objectWhere, null, 0);

					for (int j = 0; j < result.size(); j++) {
						Map resMap = (Map) result.get(j);
						Object mpBrekdownRelID = resMap.get(sb.toString());
						sListMPRelIds.addAll(ManufacturingPlanUtil.convertObjToStringList(
								context, mpBrekdownRelID));
					}
					sListMPRelIds.retainAll(relConnections);
				}
			}
			// check the design change happend or not if yes then disconnect
			// the mp's
			if (fromDesignEffectivity.equalsIgnoreCase("True")) {
				if (sListMPRelIds.size() != 0
						&& designValue.equalsIgnoreCase("yes")) {
					String[] sArrMPRelIds = new String[sListMPRelIds.size()];
					sArrMPRelIds = (String[]) sListMPRelIds
					.toArray(sArrMPRelIds);
					DomainRelationship.disconnect(context, sArrMPRelIds);
				}
			} else {
				if (sListMPRelIds.size() != 0) {
					String[] sArrMPRelIds = new String[sListMPRelIds.size()];
					sArrMPRelIds = (String[]) sListMPRelIds
					.toArray(sArrMPRelIds);
					DomainRelationship.disconnect(context, sArrMPRelIds);
				}
			}
			//need to update MP Preferred value
			for (int i = 0; i < sListMPIds.size(); i++) {
				String strMPID=(String)sListMPIds.get(i);
				ManufacturingPlan mp= new ManufacturingPlan(strMPID);
				mp.updateMPPrefferedValue(context);
			}

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return iResult;

	}
	
	
	/**
	 * This method is called by trigger when user tries to promote Manufacturing Plan from
	 * Preliminary to Release state.
	 * @param context
	 * @param args[]
	 * @return
	 * @throws Exception
	 * @author GN1
	 * @since CFP R212
	 */
	public boolean CheckAssociatedPlan(Context context, String args[])throws Exception{	
		boolean innerFlag = false;
		try {
			// get the context MP object
			String strManufacturingPlanId = args[0];
			String language = context.getSession().getLanguage();

			// Getting the String to be Displayed in the Error alert.
			String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"DMCPlanning.Alert.PolicyManufacturingPlanStatePreliminaryPromoteCheck",language);

			Map mpDetails= new ManufacturingPlan(strManufacturingPlanId).getManufacturingPlanDetail(context);
			String contextIDSelectable = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
				+ "].from.id";
			StringBuffer sbMPB=new StringBuffer();
			sbMPB.append("from[");
			sbMPB.append(ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN);
			sbMPB.append("].to.id");
			String strMPsbreakdown=sbMPB.toString();

			String contextProductID=(String)mpDetails.get(contextIDSelectable);
			StringList slMPsbreakdown=(StringList)mpDetails.get(strMPsbreakdown);
			if(slMPsbreakdown==null ||(slMPsbreakdown!=null && slMPsbreakdown.size()==0))
				throw new FrameworkException(strAlertMessage);

			DomainObject domParentObj=new DomainObject(contextProductID);
			String strRelPattern= ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_STRUCTURES;
			String strTypePattern= ManufacturingPlanConstants.TYPE_PRODUCTS;

			StringList objectSelects = new StringList();
			objectSelects.add(DomainObject.SELECT_ID);
			final String SELECT_PARENT_PRODUCT = "to["+ ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS+"].from.id";
			final String SELECT_PARENT_MAIN_PRODUCT = ("to["+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT+"].from.id");
			objectSelects.add(SELECT_PARENT_PRODUCT);	
			StringList relationshipSelects = new StringList();
			relationshipSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

			MapList returnMapList = domParentObj.getRelatedObjects(context,
					strRelPattern,
					strTypePattern,
					objectSelects,
					relationshipSelects,
					false,	//to relationship
					true,	//from relationship
					(short)1,
					DomainConstants.EMPTY_STRING, //objectWhereClause
					DomainConstants.EMPTY_STRING, //relationshipWhereClause
					0,
					null,
					null,
					null);

			Set modelIds= new HashSet();
			for(int i=0; i<returnMapList.size(); i++){
				Map mpProduct= (Map)returnMapList.get(i);
				String strParentProductId="";
				if(mpProduct.containsKey(SELECT_PARENT_PRODUCT))
					strParentProductId = (String)mpProduct.get(SELECT_PARENT_PRODUCT);
				else if(mpProduct.containsKey(SELECT_PARENT_MAIN_PRODUCT))
					strParentProductId = (String)mpProduct.get(SELECT_PARENT_MAIN_PRODUCT);
				modelIds.add(strParentProductId);
			}

			Iterator modelIter=modelIds.iterator();
			String strRelPattern2= ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS +","+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN;
			String strTypePattern2= ManufacturingPlanConstants.TYPE_PRODUCTS +","+ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN;
			//Pattern strTypeIncludePattern=new Pattern(ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN);
            StringBuffer sbTypeIncludePattern = new StringBuffer();
            List lstManufacturingPlanChildTypes = ProductLineUtil.getChildrenTypes(context, ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN);
            sbTypeIncludePattern.append(ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN);
            if(lstManufacturingPlanChildTypes.size()>0)
              sbTypeIncludePattern.append(COMMA);
            for (int i = 0; i < lstManufacturingPlanChildTypes.size(); i++) {
               sbTypeIncludePattern.append(lstManufacturingPlanChildTypes.get(i));
               if (i != lstManufacturingPlanChildTypes.size() - 1) {
                    	sbTypeIncludePattern.append(COMMA);
               }
            }
			Pattern strTypeIncludePattern=new Pattern(sbTypeIncludePattern.toString());
			StringList objectSelects2 = new StringList();
			objectSelects2.add(DomainObject.SELECT_ID);
			objectSelects2.add(DomainObject.SELECT_TYPE);
			
            List returnMPList= new MapList();
            Map nextMap = new HashMap();
            while(modelIter.hasNext()){
				String masterId=(String)modelIter.next();
				DomainObject domMasterObj=new DomainObject(masterId);
				returnMPList = domMasterObj.getRelatedObjects(context,
						strRelPattern2,
						strTypePattern2,
						objectSelects2,
						relationshipSelects,
						false,	//to relationship
						true,	//from relationship
						(short)2,
						DomainConstants.EMPTY_STRING, //objectWhereClause
						DomainConstants.EMPTY_STRING, //relationshipWhereClause
						0,
						strTypeIncludePattern,
						null,
						null);
				
				List slMps= new StringList();
				for (int i = 0; i < returnMPList.size(); i++) {
					Map resMap = (Map) returnMPList.get(i);
					String plansId = (String)resMap
					.get(DomainConstants.SELECT_ID);
					slMps.add(plansId);
				}
				nextMap.put(masterId, slMps);
			}
            
			Iterator keySet = nextMap.keySet().iterator();
			while (keySet.hasNext()) {
				String key = (String) keySet.next();
				StringList finalMBId = (StringList) nextMap.get(key);
				innerFlag = false;
				for (int j = 0; j < slMPsbreakdown.size(); j++) {
					String brekdownId = (String) slMPsbreakdown.get(j);
					if (finalMBId.contains(brekdownId)) {
						innerFlag = true;
						break;
					}
				}
				if (innerFlag == false) {
					break;
				}
			}

			if (innerFlag == false) {
				 throw new FrameworkException(strAlertMessage);
			}
		} catch (Exception e) {
		throw new FrameworkException(e.getMessage());		
	}
	return innerFlag;
	}
	/**
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public StringList CheckAssociatedPlanOnPrd(Context context, String args[])throws Exception{	
		boolean innerFlag = false;		
		int count=0;
		int size =0;
		try {
			// get the context MP object
			String strManufacturingPlanId = args[0];
			DomainObject domContext = new DomainObject(strManufacturingPlanId);
			StringList slMPsbreakdown = new StringList();
			StringList prdsIds = new StringList();
			StringBuffer sb1=new StringBuffer();
			sb1.append("from[");
			sb1.append(ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN);
			sb1.append("].to.id");
			String strMPsbreakdown=sb1.toString();
			slMPsbreakdown = domContext.getInfoList(context,strMPsbreakdown);	
					
			StringBuffer sb2=new StringBuffer();
			sb2.append("to[");
			sb2.append(ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN);
			sb2.append("].from.from[");
			sb2.append(ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_FEATURES);
			sb2.append("].to.id");
		    String strPrd=sb2.toString();
		    
			 prdsIds = domContext.getInfoList(context,strPrd);		
			int limit = -1;
			if (limit < 0) {
				limit = 32767;
			}
			StringBuffer sb3=new StringBuffer();
			sb3.append("to[");
			sb3.append(ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN);
			sb3.append("].from.name");
			String strprdSelect=sb3.toString();
			
			StringBuffer sb4=new StringBuffer();
			sb4.append("to[");
			sb4.append(ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN);
			sb4.append("].from.type");
			String strprdTypeSelect=sb4.toString();
			
			StringList slRelSelect = new StringList(1);
			StringList slObjSelect = new StringList();
			slObjSelect.add(DomainConstants.SELECT_ID);
			slObjSelect.add(DomainConstants.SELECT_NAME);
			slObjSelect.add(DomainConstants.SELECT_TYPE);		
			slObjSelect.add(strprdSelect);
			slObjSelect.add(strprdTypeSelect);			
            String relPattern = ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN;				
			String objPattern = ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN;
		
			
			Map nextMap = new HashMap();
			
			for(int iXount=0; iXount<prdsIds.size(); iXount++){
				String seq=(String) prdsIds.get(iXount);
				
				MapList revids= (MapList) new ManufacturingPlanUtil().getAllRevisions(context, seq);
				
				for(int j=0;j<revids.size();j++){
					Map revMap = (Map) revids.get(j);
					Object revid = revMap
							.get(DomainConstants.SELECT_ID);
				DomainObject domobj = new DomainObject((String) revid);
				MapList result = domobj.getRelatedObjects(context, relPattern,
						objPattern, slObjSelect, null, true, true,
						(short) 1, "", null, limit);
				

				
				
				if(result.size()!=0){
					for (int i = 0; i < result.size(); i++) {
		
						Map resMap = (Map) result.get(i);
						Object plansId = resMap
								.get(DomainConstants.SELECT_ID);
		
						StringList checkBreakdown = ManufacturingPlanUtil
								.convertObjToStringList(context, plansId);
						StringBuffer sb = new StringBuffer();
						sb.append(resMap.get(strprdTypeSelect));
						sb.append("+");
						sb.append(resMap.get(strprdSelect));
						String checkKey = sb.toString();
		
						if (nextMap.get(checkKey) == null) {
							nextMap.put(checkKey, checkBreakdown);
						} else
		
						{
							StringList existingMP = (StringList) nextMap.get(checkKey);
							existingMP.addAll(checkBreakdown);
							nextMap.put(checkKey, existingMP);
						}
					}
		
				  }
				else{
					StringBuffer sb = new StringBuffer();
					sb.append(revMap.get(DomainConstants.SELECT_TYPE));
					sb.append("+");
					sb.append(revMap.get(DomainConstants.SELECT_NAME));
					String checkKey = sb.toString();
					if (nextMap.get(checkKey) == null) {
						nextMap.put(checkKey, new StringList());
					} 
					else{
						StringList existingMP = (StringList) nextMap.get(checkKey);
						existingMP.addAll(new StringList());
						nextMap.put(checkKey, existingMP);
					}
				}
				}
			}
			
			Iterator keySet = nextMap.keySet().iterator();
			while (keySet.hasNext()) {
				size++;
				String key = (String) keySet.next();
				StringList finalMBId = (StringList) nextMap.get(key);			
					innerFlag = false;
									

				for (int j = 0; j < slMPsbreakdown.size(); j++) {
					String brekdownId = (String) slMPsbreakdown.get(j);
					if (finalMBId.contains(brekdownId)) {
						innerFlag = true;
						count++;	 
						break;
					}
				}
			}
			if(size==count)
			{
				innerFlag = true;
			}else
			{
				innerFlag = false;
			}
								
		} catch (Exception e) {
		e.printStackTrace();			
	}
		StringList result=new StringList();
		result.add(innerFlag);
		result.add(count);
	return result;
	}

	/**
	 * private method which will be called on rendendering check icon in case of
	 * MPB from Product and MP context
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * Refactored in R212 Derivations
	 */
	//private boolean checkMPBreakdownExists(Context context,StringList slMPsbreakdown,String strParentId)throws FrameworkException{
	private boolean checkMPBreakdownExists(Context context, StringList slMPsbreakdown, HashMap<String,StringList> modelIdMpIdsMap)throws FrameworkException{	
		boolean boolMPBExists = false;		
		int countMPBreakDownMatches=0;
		int countMasterCompositionUsed =0;
		Map mpBreakDownStatus=new HashMap();
		try {
			
			for (Object mpIdsObj : modelIdMpIdsMap.values()) {
				countMasterCompositionUsed++;
				StringList mpIds = (StringList)mpIdsObj;
				for (Object breakdownIdObj : slMPsbreakdown) {
					String breakdownId = (String)breakdownIdObj;
					//if we find any MPB matching, means we have breakdown done for one of the LF of current model
					if (mpIds!=null && mpIds.contains(breakdownId)) {
						countMPBreakDownMatches++;	 
						break;
					}
				}				
			}
			
			if(countMasterCompositionUsed==countMPBreakDownMatches){
				boolMPBExists = true;
			}else{
				boolMPBExists = false;
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
	return boolMPBExists;
	}
	/**
	 * -------------------------------------------
	 * R212 Derivation's JPO 
	 * -------------------------------------------
	 */
	
	/**
	 * Program defined to return Maplist which will hold Manufacturing Plan
	 * which are in the Main Derivation Chain, called in as program for the
	 * Model->MP Derivation table
	 * 
	 * @param context
	 *            Matrix context
	 * @param args
	 * @return
	 * @throws FrameworkException
	 * @since CFP R212Derivations
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getManufacturingPlanDerivationChain(Context context,
			String[] args) throws FrameworkException {
		MapList mlMPDerivation = new MapList();
		try {
	        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
	    	String objectID = (String) paramMap.get("objectId");
	
	    	// Level for recursion
	    	int level = getLevelfromSB(context, args);
	    	// Now get the Expand Limit
	    	int limit = Integer.parseInt(EnoviaResourceBundle.getProperty(context,"emxProduct.ExpandLimit"));
	    	
	    	// Set level for recursive function
	    	int firstLevel = 1;

	    	mlMPDerivation = DerivationUtil.getDerivationStructure
	    		(context, objectID, ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN, firstLevel, level, limit);

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return mlMPDerivation;
	}

	/**
	 * column JPO which will returns Derivation type
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since CFP R212Derivations
	 */
	public List getDerivationType(Context context, String args[])
			throws FrameworkException {

		List derivationTypeList = new StringList();

		try {
			String strLanguage = context.getSession().getLanguage();
			String strDerivation =EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"DMCPlanning.MPDerivation.DerivationType.Derivation",strLanguage);
			String strRevision = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"DMCPlanning.MPDerivation.DerivationType.Revision",strLanguage);

			String mpMainDerivedID = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
					+ "].from." + ProductLineConstants.SELECT_ID;

			String mpDerivedID = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED + "].from."
					+ ManufacturingPlanConstants.SELECT_ID;

			String derivationType = DomainConstants.EMPTY_STRING;

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList lstObjectIdsList = (MapList) programMap.get("objectList");
			String strRelationship = DomainConstants.EMPTY_STRING;

			for (int i = 0; i < lstObjectIdsList.size(); i++) {
				Map tempMap = (Map) lstObjectIdsList.get(i);
				if (tempMap.containsKey(mpMainDerivedID)){
					derivationType = strRevision;
				}
				else if (tempMap.containsKey(mpDerivedID)){
					derivationType = strDerivation;
				}
				else {
					String domCurrentMPID = (String) (tempMap)
							.get(DomainConstants.SELECT_ID);
					String strDerivationType=DerivationUtil.getDerivationType(context, domCurrentMPID);
					if (strDerivationType
							.equalsIgnoreCase(DerivationUtil.DERIVATION_TYPE_REVISION)) {
						derivationType = strRevision;
					} else if (strDerivationType
							.equalsIgnoreCase(DerivationUtil.DERIVATION_TYPE_DERIVATION)) {
						derivationType = strDerivation;
					} else {
						derivationType = "";
					}
				}
				derivationTypeList.add(derivationType);
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());

		}
		return derivationTypeList;
	}
	
	
	/**
	 * This will be column JPO which will show the derived from column details
	 * depending on the policy.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 * @return String list containing HTML code, to render derived from columns
	 *         data in Table
	 * @throws Exception
	 * @since CFP R212Derivations
	 */
	public StringList getDerivedFrom(Context context, String[] args)
			throws FrameworkException {
		StringList derivedFromList = new StringList();
		try {
			String strLanguage = context.getSession().getLanguage();
			String strRoot = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
							"DMCPlanning.MPDerivation.DerivationType.Root",strLanguage);

			// Unpacking the args
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) programMap.get("objectList");

			String exportFormat = "";
			HashMap paramList = (HashMap) programMap.get("paramList");
			if (paramList != null) {
				exportFormat = (String) paramList.get("exportFormat");
			}

			String mpMainDerivedType = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_TYPE;
			String mpMainDerivedTitle = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
					+ "].from."
					+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;
			String mpMainDerivedRevision = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_REVISION;
			String mpMainDerivedId = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_ID;

			String mpDerivedType = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_TYPE;
			String mpDerivedTitle = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
					+ "].from."
					+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;
			String mpDerivedRevision = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_REVISION;
			String mpDerivedId = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_ID;

			Map tempMap;
			for (Iterator iterator = objectList.iterator(); iterator.hasNext();) {
				// checking if we have Id in objectList;
				tempMap = (Map) iterator.next();
				if ((tempMap).containsKey(DomainConstants.SELECT_ID)) {
					// Check if derived Manufacturing plan exist.
					if (tempMap.containsKey(mpMainDerivedTitle)) {
						String mpTitle = (String) tempMap
								.get(mpMainDerivedTitle);
						String mpRevision = (String) tempMap
								.get(mpMainDerivedRevision);
						String mpId = (String) tempMap.get(mpMainDerivedId);
						String mpType = (String) tempMap.get(mpMainDerivedType);
						StringBuffer sbBuffer = new StringBuffer(400);
						if ((exportFormat != null)
								&& (exportFormat.length() > 0)
								&& ("CSV".equals(exportFormat))) {
							sbBuffer = sbBuffer.append(mpTitle).append(" ")
									.append(mpRevision);
						} else {
							//sbBuffer = sbBuffer.append("<img src=\"images/")
							//		.append(
							//				ProductLineCommon
							//						.getTypeIconProperty(
							//								context, mpType))
							//		.append("\"").append("/>");
							if (!"HTML".equalsIgnoreCase(exportFormat)) {
								sbBuffer
										.append("<a HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
								sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,(String) tempMap
										.get(mpMainDerivedId)));
								sbBuffer.append("')\">");
							}

							sbBuffer.append(
									XSSUtil.encodeForXML(context,
											mpTitle)).append(" ").append(
									XSSUtil.encodeForXML(context,
											mpRevision));
							if (!"HTML".equalsIgnoreCase(exportFormat))
								sbBuffer.append("</a>");
						}
						derivedFromList.add(sbBuffer.toString());

					} else if (tempMap.containsKey(mpDerivedTitle)) {
						String mpTitle = (String) tempMap.get(mpDerivedTitle);
						String mpRevision = (String) tempMap
								.get(mpDerivedRevision);
						String mpId = (String) tempMap.get(mpDerivedId);
						String mpType = (String) tempMap.get(mpDerivedType);

						StringBuffer sbBuffer = new StringBuffer(400);
						if ((exportFormat != null)
								&& (exportFormat.length() > 0)
								&& ("CSV".equals(exportFormat))) {
							sbBuffer = sbBuffer.append(mpTitle).append(" ")
									.append(mpRevision);
						} else {
							//sbBuffer = sbBuffer.append("<img src=\"images/")
							//		.append(
							//				ProductLineCommon
							//						.getTypeIconProperty(
							//								context, mpType))
							//		.append("\"").append("/>");

							if (!"HTML".equalsIgnoreCase(exportFormat)) {
								sbBuffer
										.append("<a HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=");
								sbBuffer.append(XSSUtil.encodeForHTMLAttribute(context,mpId));
								sbBuffer.append("')\">");
							}
							sbBuffer.append(
									XSSUtil.encodeForXML(context,
											mpTitle)).append(" ").append(
									XSSUtil.encodeForXML(context,
											mpRevision));
							if (!"HTML".equalsIgnoreCase(exportFormat))
								sbBuffer.append("</a>");
						}
						derivedFromList.add(sbBuffer.toString());

					} else {
						String domCurrentMPID = (String) (tempMap)
								.get(DomainConstants.SELECT_ID);
						boolean isRootNode = DerivationUtil.isRootNode(context,
								domCurrentMPID);
						if (isRootNode) {
							derivedFromList.add(strRoot);
						} else {
							ManufacturingPlan mpBean = new ManufacturingPlan(
									domCurrentMPID);
							Map mpDerivedFrom = mpBean
									.getManufacturingPlanParent(context);
							String strFromMPType = (String) mpDerivedFrom
									.get(DomainObject.SELECT_TYPE);
							String strFromMPTitle = (String) mpDerivedFrom
									.get(ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE);
							String strFromMPRevision = (String) mpDerivedFrom
									.get(DomainObject.SELECT_REVISION);
							String strFromMPId = (String) mpDerivedFrom
									.get(DomainObject.SELECT_ID);
							StringBuffer sbBuffer = new StringBuffer(400);
							if ((exportFormat != null)
									&& (exportFormat.length() > 0)
									&& ("CSV".equals(exportFormat))) {
								sbBuffer = sbBuffer.append(strFromMPTitle)
										.append(" ").append(strFromMPRevision);
							} else {
								sbBuffer = sbBuffer
							//			.append("<img src=\"images/")
							//			.append(
							//					ProductLineCommon
							//							.getTypeIconProperty(
							//									context,
							//									strFromMPType))
							//			.append("\"")
							//			.append("/>")
										.append(
												"<a HREF=\"javascript:showDetailsPopup('../common/emxTree.jsp?objectId=")
										.append(XSSUtil.encodeForHTMLAttribute(context,
												(String) tempMap
														.get(strFromMPId)))
										.append("')\">")
										.append(
												XSSUtil
														.encodeForXML(
																context,
																strFromMPTitle))
										.append(" ").append(
												XSSUtil.encodeForXML(
														context,
														strFromMPRevision))
										.append("</a>");
							}
							derivedFromList.add(sbBuffer.toString());
						}
					}
				} else {
					derivedFromList.add(DomainConstants.EMPTY_STRING);
				}
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return derivedFromList;
	}
	/**
	 * To create the Manufacturing Plan object from create component
	 * 
	 * @param context
	 * @param args
	 * @return Map
	 * @throws Exception
	 * @since CFP R212Derivations
	 */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public Map createManufacturingPlanJPO(Context context, String[] args)
			throws FrameworkException {
		HashMap returnMap = new HashMap();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String isRootMP = (String) programMap.get("isRootMP");
			boolean boolRootMP = Boolean.parseBoolean(isRootMP);

			String strMPId = "";
			String strType = (String) programMap.get("TypeActual");
			if (strType == null)
				strType = (String) programMap.get("Type1");

			String strName = (String) programMap.get("Name");
			String strRevision = (String) programMap.get("Revision");
			String strPolicy = (String) programMap.get("Policy");
			String strVault = (String) programMap.get("Vault");
			String strOwner = (String) programMap.get("Owner");
			String strDescription = (String) programMap.get("Description");
			String strDerivedFromID = (String) programMap.get("DerivedFromOID");
			String strDerivationLevel = (String)programMap.get("DerivationLevel");
			
			String strTitle = (String) programMap.get("Title");
			String strManufacturinPlanIntent = (String) programMap.get("Modality");
			String strDerivationType = (String) programMap.get("DerivationType");

			HashMap objAttributeMap = new HashMap();
			objAttributeMap.put(ManufacturingPlanConstants.ATTRIBUTE_TITLE,
					strTitle);
			objAttributeMap.put(
					ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_INTENT,
					strManufacturinPlanIntent);

			// Create the attributes to be sent to create by calling the Create Derived Node function.  
			// This will fill the Map with the attributes we will need for the new nodes.
			HashMap nodeAttrs = DerivationUtil.createDerivedNode(context, strDerivedFromID, strDerivationLevel, strType);
			if (nodeAttrs != null && nodeAttrs.size() > 0) {
				objAttributeMap.putAll(nodeAttrs);
			}

			ManufacturingPlan mpBean = new ManufacturingPlan();
			strMPId = mpBean.createManufacturingPlanDerivation(context,
					strType, strName, strRevision, strPolicy, strVault,
					strOwner, strDescription, objAttributeMap,
					strDerivationType, strDerivedFromID);
			returnMap.put("id", strMPId);

		} catch (Exception e) {
			throw new FrameworkException(e);
		}
		return returnMap;
	}

	/**
	 * To insert Manufacturing Plan object from create component, and insert
	 * before the selcted MP
	 * 
	 * @param context
	 * @param args
	 * @return Map
	 * @throws Exception
	 * @since CFP R212Derivations
	 */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public Map insertManufacturingPlanJPO(Context context, String[] args)
			throws FrameworkException {

		HashMap returnMap = new HashMap();

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestValue = (HashMap) programMap.get("RequestValuesMap");

			String strMPId = "";
			String strType = (String) programMap.get("TypeActual");

			String strName = (String) programMap.get("Name");
			String strRevision = (String) programMap.get("Revision");
			String strPolicy = (String) programMap.get("Policy");
			String strVault = (String) programMap.get("Vault");

			String strOwner = (String) programMap.get("Owner");
			String strDescription = (String) programMap.get("Description");

			String strDerivedFromID = (String) programMap.get("derivedFromID");
			String strDerivedToID = (String) programMap.get("objectID");
			String strDerivationLevel = (String) programMap.get("DerivationLevel");


			String strTitle = (String) programMap.get("Title");
			String strManufacturinPlanIntent = (String) programMap.get("Modality");

			HashMap objAttributeMap = new HashMap();
			objAttributeMap.put(ManufacturingPlanConstants.ATTRIBUTE_TITLE,
					strTitle);
			objAttributeMap.put(
					ManufacturingPlanConstants.ATTRIBUTE_MANUFACTURING_INTENT,
					strManufacturinPlanIntent);

			// Create the attributes to be sent to create by calling the Create Derived Node function.  This will fill the
			// Map with the attributes we will need for the new nodes.
			HashMap nodeAttrs = DerivationUtil.insertDerivedNode 
					(context, strDerivedFromID, strDerivedToID, strDerivationLevel, strType);
			if (nodeAttrs != null && nodeAttrs.size() > 0) {
				objAttributeMap.putAll(nodeAttrs);
			}

			
			try {
				ManufacturingPlan mpBean = new ManufacturingPlan();
				strMPId = mpBean.insertManufacturingPlanDerivation(context,
						strType, strName, strRevision, strPolicy, strVault,
						strOwner, strDescription, objAttributeMap,
						strDerivedFromID, strDerivedToID);
				returnMap.put("id", strMPId);

			} catch (Exception e) {
				throw new FrameworkException(e);
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return returnMap;
	}

	/**
	 * This Method is used to get the range HREF for Planned for field
	 * 
	 * For the Main Manufacturing Plan: Model Context: Chooser to select any
	 * Product managed under the Model Product Context: Product in which the
	 * Manufacturing Plan is being created and the field is locked for editing
	 * 
	 * For the non Main Manufacturing Plan: Product and Model Context: chooser
	 * will allow Products from the forward derivation chain of the current
	 * Product
	 * 
	 * @since CFP R212Derivations
	 * 
	 */
	public String getPlannedForFieldHref(Context context, String[] args)
	throws FrameworkException {
		StringBuffer sbBuffer = new StringBuffer();

		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map fieldMap = (HashMap) programMap.get("fieldMap");
			String strFieldName = (String) fieldMap.get("name");
			Map paramMap = (HashMap) programMap.get("paramMap");

			HashMap requestMap = (HashMap) programMap.get("requestMap");
			// selected MP ID
			String objectId = (String) requestMap.get("objectID");
			// Model or Product id
			String contextObjectId = (String) requestMap.get("parentOID");
			String isRootMP = (String) requestMap.get("isRootMP");
			boolean boolRootMP = Boolean.parseBoolean(isRootMP);

			String isFromProductContext = (String) requestMap
			.get("isFromProductContext");
			boolean boolFromProduct = Boolean.parseBoolean(isFromProductContext);
			
			boolean boolPrdMainMP = false;

			String includeOIDMethod = "";
			DomainObject objContext = new DomainObject(contextObjectId);
			StringList slObjSel = new StringList(DomainConstants.SELECT_TYPE);
			slObjSel
			.add(ManufacturingPlanConstants.SELECT_ATTRIBUTE_MARKETING_NAME);
			slObjSel.add(ManufacturingPlanConstants.SELECT_REVISION);
			Map mapSelContext = objContext.getInfo(context, slObjSel);
			String strMarketinNameContext = XSSUtil.encodeForXML(context,(String) mapSelContext
			.get(ManufacturingPlanConstants.SELECT_ATTRIBUTE_MARKETING_NAME));
			String strRevisionContext = XSSUtil.encodeForXML(context,(String) mapSelContext
			.get(DomainConstants.SELECT_REVISION));

			String defaultDestValue = "";
			String defaultDestId = "";
			if (objContext.isKindOf(context,
					ManufacturingPlanConstants.TYPE_PRODUCTS)
					&& boolRootMP) {
				boolPrdMainMP = true;

				defaultDestValue = strMarketinNameContext + " " + strRevisionContext;
				defaultDestId = contextObjectId;

			} else if (objContext.isKindOf(context,
					ManufacturingPlanConstants.TYPE_MODEL)
					&& boolRootMP) {
				defaultDestValue = "";
				includeOIDMethod = "getManagedProducts";
				defaultDestId = "";
			} else {
				if (objectId != null && !objectId.isEmpty()) {
					ManufacturingPlan mp = new ManufacturingPlan(objectId);
					Map mapSel = mp.getManufacturingPlanContext(context);
					String contextRevisionSelectable = "to["
						+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
						+ "].from.revision";
					String contextMarketingNameSelectable = "to["
						+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
						+ "].from."
						+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_MARKETING_NAME;
					String contextIDSelectable = "to["
						+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
						+ "].from." + ManufacturingPlanConstants.SELECT_ID;
					String strMarketinName = XSSUtil.encodeForXML(context,(String) mapSel
					.get(contextMarketingNameSelectable));
					String strRevision = XSSUtil.encodeForXML(context,(String) mapSel.get(contextRevisionSelectable));
					String strID = (String) mapSel.get(contextIDSelectable);
					includeOIDMethod = "getForwardChainofContext";
					if(boolFromProduct)//product context
					{
						defaultDestValue = strMarketinNameContext + " " + strRevisionContext;
						defaultDestId = contextObjectId;
					}else{
						defaultDestValue = strMarketinName + " " + strRevision;
						defaultDestId = strID;
					}
					contextObjectId = strID;
				} else {
					defaultDestValue = strMarketinNameContext + " " + strRevisionContext;
					defaultDestId = contextObjectId;
					includeOIDMethod = "getForwardChainofContext";
				}
			}

			String strTypes = "";
			strTypes = "type_Products";


			sbBuffer.append("<input type=\"text\" readonly=\"true\" ");
			sbBuffer.append("name=\"");
			sbBuffer.append(strFieldName);
			sbBuffer.append("Display\" id=\"\" value=\"");
			sbBuffer.append(defaultDestValue);
			sbBuffer.append("\">");
			sbBuffer.append("</input>");
			sbBuffer.append("<input type=\"hidden\" name=\"");
			sbBuffer.append(strFieldName);
			sbBuffer.append("\" value=\"");
			sbBuffer.append(defaultDestValue);
			sbBuffer.append("\">");
			sbBuffer.append("</input>");
			sbBuffer.append("<input type=\"hidden\" name=\"");
			sbBuffer.append(strFieldName);
			sbBuffer.append("OID\" id=\"");
			sbBuffer.append(strFieldName);
			sbBuffer.append("OID\"");
			sbBuffer.append(" value=\"");
			sbBuffer.append(defaultDestId);
			sbBuffer.append("\">");
			sbBuffer.append("</input>");
			if (!boolPrdMainMP || (boolFromProduct && objectId == null)) {
				sbBuffer.append("<input type=\"button\" name=\"btnObject2\" ");
				sbBuffer.append("");
				sbBuffer
						.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
				sbBuffer.append(
						"onClick=\"javascript:showPlannedFromSelector('")
						.append(strTypes).append("','")
						.append(includeOIDMethod).append("','").append(
								contextObjectId).append("')\">");
				sbBuffer.append("</input>");
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return sbBuffer.toString();
	}

	/**
	 * This Method is used to get the range HREF for Derived from field
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since CFP R212Derivations
	 */
	public String getDerivedFromField(Context context, String[] args)
			throws FrameworkException {
		StringBuffer dispField = new StringBuffer();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			HashMap fieldMap = (HashMap) programMap.get("fieldMap");
			String fieldName = (String) fieldMap.get("name");
			String objectId = (String) requestMap.get("objectID");
			String isRootMP = (String) requestMap.get("isRootMP");
			String strLanguage = context.getSession().getLanguage();
			boolean boolRootMP = Boolean.parseBoolean(isRootMP);
			String strDerivationType = (String) requestMap.get("DerivationType");

			String contextObjectId = (String) requestMap.get("parentOID");

			if (boolRootMP) {
				String strRoot = EnoviaResourceBundle.getProperty(context,SUITE_KEY,"DMCPlanning.MPDerivation.DerivationType.Root",strLanguage);
				dispField.append(strRoot);
				dispField.append("<input type=\"hidden\" name=\"");
				dispField.append(fieldName);
				dispField.append("\" id=\"");
				dispField.append(fieldName);
				dispField.append("OID\"");
				dispField.append(" value=\"");
				dispField.append("root");
				dispField.append("\">");
				dispField.append("</input>");
			} else {
				if (objectId != null && !objectId.isEmpty()) {
					ManufacturingPlan mpBean = new ManufacturingPlan(objectId);
					Map mapSel = mpBean.getManufacturingPlanDetail(context);
					String mpTitleSelectable = ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;
					String mpRevSelectable = DomainObject.SELECT_REVISION;
					String mpNameSelectable = DomainObject.SELECT_NAME;
					String strTitle = (String) mapSel.get(mpTitleSelectable);
					String strRevision = (String) mapSel.get(mpRevSelectable);
					String strName = (String) mapSel.get(mpNameSelectable);
					dispField.append(XSSUtil.encodeForXML(context, strTitle));
					dispField.append(" ");
					dispField.append(strRevision);
					dispField.append("<input type=\"hidden\" name=\"");
					dispField.append(fieldName);
					dispField.append("OID\" id=\"");
					dispField.append(fieldName);
					dispField.append("OID\"");
					dispField.append(" value=\"");
					dispField.append(objectId);
					dispField.append("\">");
					dispField.append("</input>");
					
					dispField.append("<input type=\"hidden\" id=\"");
					dispField.append(fieldName);
					dispField.append("Display\" value=\"");
					dispField.append(strName);
					dispField.append("\">");
					dispField.append("</input>");
				} else {
					String strTypes = "";
					strTypes = "type_ManufacturingPlan";
					dispField.append("<input type=\"text\" readonly=\"true\" ");
					dispField.append("name=\"");
					dispField.append(fieldName);
					dispField.append("Display\" onchange=\"javascript:updateDerivedType()\" id=\"\" value=\"");
					dispField.append("");
					dispField.append("\">");
					dispField.append("</input>");
					
					dispField.append("<input type=\"hidden\" name=\"");
					dispField.append(fieldName);
					dispField.append("\" value=\"");
					dispField.append("");
					dispField.append("\">");
					dispField.append("</input>");
					
					dispField.append("<input type=\"hidden\" name=\"");
					dispField.append(fieldName);
					dispField.append("OID\" id=\"");
					dispField.append(fieldName);
					dispField.append("OID\"");
					dispField.append(" value=\"");
					dispField.append(objectId);
					dispField.append("\">");
					dispField.append("</input>");
					
					dispField.append("<input type=\"hidden\" id=\"DerivedFromDType\" name=\"");
					dispField.append(fieldName);
					dispField.append("DType\" value=\"");
					dispField.append("");
					dispField.append("\">");
					dispField.append("</input>");
					
					//Display Name and Name of Derived From
					dispField.append("<input type=\"hidden\" id=\"DerivedFromDName\" name=\"");
					dispField.append(fieldName);
					dispField.append("DName\" value=\"");
					dispField.append("");
					dispField.append("\">");
					dispField.append("</input>");
					
					dispField.append("<input type=\"hidden\" id=\"DerivedFromName\" name=\"");
					dispField.append(fieldName);
					dispField.append("Name\" value=\"");
					dispField.append("");
					dispField.append("\">");
					dispField.append("</input>");
					
					//Type policy and Intent
					dispField.append("<input type=\"hidden\" id=\"DerivedFromType\" name=\"");
					dispField.append(fieldName);
					dispField.append("Type\" value=\"");
					dispField.append("");
					dispField.append("\">");
					dispField.append("</input>");
					
					dispField.append("<input type=\"hidden\" id=\"DerivedFromPolicy\" name=\"");
					dispField.append(fieldName);
					dispField.append("Policy\" value=\"");
					dispField.append("");
					dispField.append("\">");
					dispField.append("</input>");
					
					dispField.append("<input type=\"hidden\" id=\"DerivedFromIntent\" name=\"");
					dispField.append(fieldName);
					dispField.append("Intent\" value=\"");
					dispField.append("");
					dispField.append("\">");
					dispField.append("</input>");
					
			
					dispField.append("<input type=\"button\" name=\"btnObject2\" ");
					dispField.append("");
					dispField.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
					if (strDerivationType.equals(DerivationUtil.DERIVATION_TYPE_REVISION)) { 
						dispField.append("onClick=\"javascript:showDerivedFromSelectorRevision('");
					} else {
						dispField.append("onClick=\"javascript:showDerivedFromSelectorDerivation('");
					}
					dispField.append(strTypes);
					dispField.append("')\">");
					dispField.append("</input>");
				}
			}

		} catch (Exception e) {
			throw (new FrameworkException(e));
		}
		return dispField.toString();
	}

	/**
	 * This Method is used to get the Derivation Type combobox
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since CFP R212Derivations
	 */
	public String getDerivationTypeField(Context context, String[] args)
			throws FrameworkException {
		StringBuffer dispField = new StringBuffer();

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			HashMap fieldMap = (HashMap) programMap.get("fieldMap");
			String fieldName = (String) fieldMap.get("name");
			String objectId = (String) requestMap.get("objectID");
			String strDerivationType = (String) requestMap.get("DerivationType");
				
			String strLanguage = context.getSession().getLanguage();
			String strDerivation = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"DMCPlanning.MPDerivation.DerivationType.Derivation",strLanguage);
			String strRevision = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"DMCPlanning.MPDerivation.DerivationType.Revision",strLanguage);
	
			if (strDerivationType.equals(DerivationUtil.DERIVATION_TYPE_REVISION)) { 
				dispField.append(strRevision);
				dispField.append("<input type=\"hidden\" name=\"");
	  			dispField.append(fieldName);
	  			dispField.append("\" id=\"");
				dispField.append(fieldName);
				dispField.append("\" value=\"");
				dispField.append(DerivationUtil.DERIVATION_TYPE_REVISION);
				dispField.append("\">");
				dispField.append("</input>");
			} else {
				dispField.append(strDerivation);
				dispField.append("<input type=\"hidden\" name=\"");
	  			dispField.append(fieldName);
	  			dispField.append("\" id=\"");
				dispField.append(fieldName);
				dispField.append("\" value=\"");
				dispField.append(DerivationUtil.DERIVATION_TYPE_DERIVATION);
				dispField.append("\">");
				dispField.append("</input>");
			}
        } catch (Exception e) {
 			throw new FrameworkException(e.getMessage());
 		}
		return dispField.toString();
	}

	/**
	 * Access Expression to show type chooser
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws FrameworkException
	 * @since CFP R212Derivations
	 */
	public static Boolean showTypeChooser(Context context, String[] args)
			throws FrameworkException {
		boolean showTypeChooser = false;
		try {
			HashMap requestMap = (HashMap) JPO.unpackArgs(args);
			String isRootMP = (String) requestMap.get("isRootMP");
			showTypeChooser = Boolean.parseBoolean(isRootMP);
			String isFromProductContext = (String) requestMap
					.get("isFromProductContext");
			boolean boolFromProductContext = Boolean.parseBoolean(
					isFromProductContext);
			String objectId = (String) requestMap.get("objectID");
			if (boolFromProductContext && objectId == null)
				showTypeChooser = true;
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return showTypeChooser;
	}

	/**
	 * Access Expression to hide type chooser field insted show harcoded type
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws FrameworkException
	 * @since CFP R212Derivations
	 */
	public static Boolean showType(Context context, String[] args)
			throws FrameworkException {
		boolean showTypeChooser = false;
		try {
			HashMap requestMap = (HashMap) JPO.unpackArgs(args);
			String isRootMP = (String) requestMap.get("isRootMP");
			showTypeChooser = Boolean.parseBoolean(isRootMP);
			String isFromProductContext = (String) requestMap
					.get("isFromProductContext");
			boolean boolFromProductContext = Boolean.parseBoolean(
					isFromProductContext);
			String objectId = (String) requestMap.get("objectID");
			if (boolFromProductContext && objectId == null)
				showTypeChooser = true;
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return !showTypeChooser;
	}

	/**
	 * This will populate type field
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 *             TODO - do we need this?
	 * @since CFP R212Derivations
	 */
	public String getTypeField(Context context, String[] args) throws FrameworkException {
		StringBuffer strTypeChooser = new StringBuffer(200);

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String strReturn = null;
			String objectId = (String) requestMap.get("objectID");
			if (objectId != null) {
				ManufacturingPlan mpBean = new ManufacturingPlan(objectId);
				Map mapSel = mpBean.getManufacturingPlanDetail(context);
				String mpTypeSelectable = ManufacturingPlanConstants.SELECT_TYPE;
				String strType = (String) mapSel.get(mpTypeSelectable);
				strTypeChooser.append(XSSUtil.encodeForHTML(context, strType));
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return strTypeChooser.toString();
	}

	/**
	 * This will populate the Derived To Field in Insert Before form
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since CFP R212Derivations
	 */
	public String getDerivedToFieldForInsertBefore(Context context,
			String[] args) throws FrameworkException {
		StringBuffer dispField = new StringBuffer();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");

			HashMap fieldMap = (HashMap) programMap.get("fieldMap");
			String fieldName = (String) fieldMap.get("name");

			String objectId = (String) requestMap.get("objectID");
			String strLanguage = context.getSession().getLanguage();
			
			// For Fixing IR IR-232426V6R2014x ,XSSUtil.encodeForHTML(context, String) Replaced With XSSUtil.encodeForXML(context, String)
			ManufacturingPlan mpBean = new ManufacturingPlan(objectId);
			Map mapSel = mpBean.getManufacturingPlanDetail(context);
			String mpTitleSelectable = ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;
			String mpRevSelectable = DomainObject.SELECT_REVISION;
			String mpNameSelectable = DomainObject.SELECT_NAME;
			String strTitle = (String) mapSel.get(mpTitleSelectable);
			String strRevision = (String) mapSel.get(mpRevSelectable);
			String strName = (String) mapSel.get(mpNameSelectable);
			dispField.append(XSSUtil.encodeForXML(context, strTitle));
			dispField.append(" ");
			dispField.append(XSSUtil.encodeForXML(context,strRevision));
			dispField.append("<input type=\"hidden\" name=\"");
			dispField.append(fieldName);
			dispField.append("\" value=\"");
			dispField.append(objectId);
			dispField.append("\">");
			dispField.append("</input>");
			dispField.append("<input type=\"hidden\" id=\"");
			dispField.append(fieldName);
			dispField.append("Display\" value=\"");
			dispField.append(XSSUtil.encodeForXML(context,strName));
			dispField.append("\">");
			dispField.append("</input>");

		} catch (Exception e) {
			throw (new FrameworkException(e.getMessage()));
		}
		return dispField.toString();
	}

	/**
	 * This will populate the Derived From Field in Insert Before form
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since CFP R212Derivations
	 */
	public String getDerivedFromFieldForInsertBefore(Context context,
			String[] args) throws FrameworkException {
		StringBuffer dispField = new StringBuffer();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");

			HashMap fieldMap = (HashMap) programMap.get("fieldMap");
			String fieldName = (String) fieldMap.get("name");

			String objectId = (String) requestMap.get("derivedFromID");
			String strLanguage = context.getSession().getLanguage();
			
			// For Fixing IR IR-232426V6R2014x ,XSSUtil.encodeForHTML(context, String) Replaced With XSSUtil.encodeForXML(context, String)
			ManufacturingPlan mpBean = new ManufacturingPlan(objectId);
			Map mapSel = mpBean.getManufacturingPlanDetail(context);
			String mpTitleSelectable = ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;
			String mpRevSelectable = DomainObject.SELECT_REVISION;
			String mpNameSelectable = DomainObject.SELECT_NAME;
			String strTitle = (String) mapSel.get(mpTitleSelectable);
			String strRevision = (String) mapSel.get(mpRevSelectable);
			String strName = (String) mapSel.get(mpNameSelectable);
			dispField.append(XSSUtil.encodeForXML(context, strTitle));
			dispField.append(" ");
			dispField.append(XSSUtil.encodeForXML(context,strRevision));
			dispField.append("<input type=\"hidden\" name=\"");
			dispField.append(fieldName);
			dispField.append("\" value=\"");
			dispField.append(objectId);
			dispField.append("\">");
			dispField.append("</input>");
			dispField.append("<input type=\"hidden\" id=\"");
			dispField.append(fieldName);
			dispField.append("Display\" value=\"");
			dispField.append(XSSUtil.encodeForXML(context,strName));
			dispField.append("\">");
			dispField.append("</input>");

		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return dispField.toString();
	}

	/**
	 * This will populate the Derivation Type Field in Insert Before form
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since CFP R212Derivations
	 */
	public String getDerivationTypeFieldForInsertBefore(Context context,
			String[] args) throws FrameworkException {
		StringBuffer dispField = new StringBuffer();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");

			HashMap fieldMap = (HashMap) programMap.get("fieldMap");
			String fieldName = (String) fieldMap.get("name");

			String objectId = (String) requestMap.get("objectID");
			String derivationType = "";
			String derivationTypeValue = "";

			String strLanguage = context.getSession().getLanguage();
			String strDerivation = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
			"DMCPlanning.MPDerivation.DerivationType.Derivation",strLanguage);
			String strRevision =EnoviaResourceBundle.getProperty(context,SUITE_KEY,
			"DMCPlanning.MPDerivation.DerivationType.Revision",strLanguage);


			String mpAbstractDerivedID = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
				+ "].from." + ProductLineConstants.SELECT_ID;

			String mpMainDerivedID = "to["
				+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED + "].from."
				+ ManufacturingPlanConstants.SELECT_ID;

			String mpDerivedID = "to[" + ManufacturingPlanConstants.RELATIONSHIP_DERIVED
			+ "].from." + ProductLineConstants.SELECT_ID;

			Map tempMap = new DomainObject(objectId).getInfo(context,
					new StringList(mpAbstractDerivedID));
			if (tempMap.containsKey(mpMainDerivedID)) {
				derivationType = strRevision;
				derivationTypeValue = DerivationUtil.DERIVATION_TYPE_REVISION;
			} else if (tempMap.containsKey(mpDerivedID)) {
				derivationType = strDerivation;
				derivationTypeValue = DerivationUtil.DERIVATION_TYPE_DERIVATION;
			}
			dispField.append(derivationType);
			dispField.append("<input type=\"hidden\" name=\"");
			dispField.append(fieldName);
			dispField.append("\" value=\"");
			dispField.append(derivationTypeValue);
			dispField.append("\">");
			dispField.append("</input>");
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return dispField.toString();
	}
	
	
	/**
	 * This will be used to called from post process URL in create MP case
	 * @param context the eMatrix <code>Context</code> object
	 * @param args JPO argument
	 * @return String XML Message
	 * @throws Exception if operation fails
	 * @since CFP R212Derivations
	 */
	public static String postCreateGetRowXML(Context context, String[] args)
	throws FrameworkException
    {
		StringBuffer xmlString = new StringBuffer();

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String objId = (String) programMap.get("objectId");
			String selId = (String) programMap.get("selId");
			String strLevel = (String) programMap.get("Level");
			String strDerivationType = (String) programMap.get("DerivationType");

			DomainObject obj = new DomainObject(objId);
			ManufacturingPlan mp = new ManufacturingPlan(obj);
			Map mapParentDetail = mp.getManufacturingPlanParent(context);
			String relId = (String) mapParentDetail.get(DomainRelationship.SELECT_ID);
			String location = "appendChild";

			xmlString.append("<item oid=\"");
			xmlString.append(objId);
			xmlString.append("\" relId=\"");
			xmlString.append(relId);
			xmlString.append("\" pid=\"");
			xmlString.append(selId);
		
			// If we have a Revision, we are adding below the selected row, not as a child of the selected row.
			if (strDerivationType.equals(DerivationUtil.DERIVATION_TYPE_REVISION)) {
		    	xmlString.append("\" pasteBelowToRow=\"");
		    	xmlString.append(strLevel);
			} else {
		    	xmlString.append("\" location=\"");
		    	xmlString.append(location);
			}

			xmlString.append("\">");
			xmlString.append("</item>");
		
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return xmlString.toString();
	}
	   
	/**
	 * This will be used to called from post process URL in insert MP case
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            JPO argument
	 * @return String XML Message
	 * @throws Exception
	 *             if operation fails
	 * @since CFP R212Derivations
	 */
	public static String postInsertGetRowXML(Context context, String[] args)
			throws FrameworkException {
		StringBuffer xmlString = new StringBuffer();

		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap = (HashMap) programMap.get("paramMap");
			String objId = (String) programMap.get("objectId");
			String selId = (String) programMap.get("selId");
			String selParentId = (String) programMap.get("selParentId");
			String derivedToLevel = (String) programMap.get("derivedToLevel");

			String strDerivationType = (String) programMap
					.get("DerivationType");

			String location = "insertBefore";
			// newly created Object
			DomainObject obj = new DomainObject(objId);

			ManufacturingPlan mp = new ManufacturingPlan(obj);
			Map mapParentDetail = mp.getManufacturingPlanParent(context);
			String relId = (String) mapParentDetail
					.get(DomainRelationship.SELECT_ID);

			xmlString
					.append("<item location=\"" + location + "\" oid=\""
							+ objId + "\" relId=\"" + relId + "\" pid=\""
							+ selParentId);
			xmlString.append("\" pasteAboveToRow=\"" + derivedToLevel);
			xmlString.append("\">");
			xmlString.append("</item>");
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}

		return xmlString.toString();
	}

	/**
	 * This Method is used to get the range HREF for Planned for field
	 * 
	 * For the Main Manufacturing Plan: Model Context: Chooser to select any
	 * Product managed under the Model Product Context: Product in which the
	 * Manufacturing Plan is being created and the field is locked for editing
	 * 
	 * For the non Main Manufacturing Plan: Product and Model Context: chooser
	 * will allow Products from the forward derivation chain of the current
	 * Product
	 * 
	 * @since CFP R212Derivations
	 */
	public String getPlannedForFieldForInsertBefore(Context context,
			String[] args) throws FrameworkException {
		StringBuffer sbBuffer = new StringBuffer();

		try {
			Map programMap = (HashMap) JPO.unpackArgs(args);
			Map fieldMap = (HashMap) programMap.get("fieldMap");
			String strFieldName = (String) fieldMap.get("name");
			Map paramMap = (HashMap) programMap.get("paramMap");

			HashMap requestMap = (HashMap) programMap.get("requestMap");
			// Derived To- Selected MP ID
			String objectId = (String) requestMap.get("objectID");
			// Model or Product id
			String contextObjectId = (String) requestMap.get("parentOID");
			// Derived From ID - Selected MP's Parent ID
			String derivedFromID = (String) requestMap.get("derivedFromID");
			String parentMPContextID = (String) requestMap
					.get("parentMPContextID");

			String includeOIDMethod = "";
			DomainObject objContext = new DomainObject(contextObjectId);
			String defaultDestValue = "";
			String defaultDestId = "";

			ManufacturingPlan mp = new ManufacturingPlan(objectId);
			Map mapSel = mp.getManufacturingPlanContext(context);
			String contextRevisionSelectable = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
					+ "].from.revision";
			String contextMarketingNameSelectable = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
					+ "].from."
					+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_MARKETING_NAME;
			String contextIDSelectable = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN
					+ "].from." + ManufacturingPlanConstants.SELECT_ID;
			String strMarketinName = XSSUtil.encodeForXML(context,(String) mapSel
					.get(contextMarketingNameSelectable));
			String strRevision = XSSUtil.encodeForXML(context,(String) mapSel.get(contextRevisionSelectable));
			String strID = (String) mapSel.get(contextIDSelectable);
			defaultDestValue = strMarketinName + " " + strRevision;
			includeOIDMethod = "getBackwardDerivationChain";
			defaultDestId = strID;
			contextObjectId = strID;

			String strTypes = "";
			strTypes = "type_Products";
			
			// For Fixing IR IR-232426V6R2014x , XSSUtil.encodeForXML(context ,String) added
			sbBuffer.append("<input type=\"text\" readonly=\"true\" ");
			sbBuffer.append("name=\"");
			sbBuffer.append(XSSUtil.encodeForXML(context,strFieldName));
			sbBuffer.append("Display\" id=\"\" value=\"");
			sbBuffer.append(XSSUtil.encodeForXML(context,defaultDestValue));
			sbBuffer.append("\">");
			sbBuffer.append("</input>");
			sbBuffer.append("<input type=\"hidden\" name=\"");
			sbBuffer.append(XSSUtil.encodeForXML(context,strFieldName));
			sbBuffer.append("\" value=\"");
			sbBuffer.append(XSSUtil.encodeForXML(context,defaultDestValue));
			sbBuffer.append("\">");
			sbBuffer.append("</input>");
			sbBuffer.append("<input type=\"hidden\" name=\"");
			sbBuffer.append(XSSUtil.encodeForXML(context,strFieldName));
			sbBuffer.append("OID\" value=\"");
			sbBuffer.append(XSSUtil.encodeForXML(context,defaultDestId));
			sbBuffer.append("\">");
			sbBuffer.append("</input>");
			sbBuffer.append("<input type=\"button\" name=\"btnObject2\" ");
			sbBuffer.append("");
			sbBuffer
					.append("size=\"200\" value=\"...\" alt=\"\" enabled=\"true\" ");
			sbBuffer
					.append("onClick=\"javascript:showChooser('../common/emxFullSearch.jsp?objectId=");
			sbBuffer.append(XSSUtil.encodeForXML(context,objectId));
			sbBuffer.append("&amp;includeOIDprogram=ManufacturingPlanSearch:");
			sbBuffer.append(XSSUtil.encodeForXML(context,includeOIDMethod));
			sbBuffer.append("&amp;contextObjectId=");
			sbBuffer.append(XSSUtil.encodeForXML(context,contextObjectId));
			sbBuffer.append("&amp;objectId=");
			sbBuffer.append(XSSUtil.encodeForXML(context,objectId));
			sbBuffer.append("&amp;parentMPContextID=");
			sbBuffer.append(XSSUtil.encodeForXML(context,parentMPContextID));
			sbBuffer.append("&amp;field=TYPES=");
			sbBuffer.append(XSSUtil.encodeForXML(context,strTypes));
			sbBuffer.append("&amp;table=CFPSearchProductTable");
			sbBuffer.append("&amp;sortColumnName=Revision");
			sbBuffer.append("&amp;sortDirection=ascending");
			sbBuffer.append("&amp;Registered Suite=DMCPlanning");
			sbBuffer.append("&amp;selection=single");
			sbBuffer.append("&amp;hideHeader=true");
			sbBuffer
					.append("&amp;submitURL=../dmcplanning/ManufacturingPlanCreateInsertSearchUtil.jsp?mode=Chooser");
			sbBuffer.append("&amp;chooserType=FormChooser");
			sbBuffer.append("&amp;fieldNameActual=ContextOID");
			sbBuffer.append("&amp;fieldNameDisplay=ContextDisplay");
			sbBuffer.append("&amp;appendRevision=true");
			sbBuffer
					.append("&amp;HelpMarker=emxhelpfullsearch','850','630')\">");
			sbBuffer.append("</input>");
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return sbBuffer.toString();
	}

	/**
	 * This will be used to render Deriation type column for the search table
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since CFP R212Derivations
	 */

	public List getDerivationTypeForSearch(Context context, String args[])
			throws FrameworkException {
		List derivationTypeList = new StringList();
		try {
			String strLanguage = context.getSession().getLanguage();
			String strDerivation = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"DMCPlanning.MPDerivation.DerivationType.Derivation",strLanguage);
			String strRevision = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
					"DMCPlanning.MPDerivation.DerivationType.Revision",strLanguage);

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMapList = (MapList) programMap.get("objectList");

			String[] strPRDIDs = new String[objectMapList.size()];
			for (int i = 0; i < objectMapList.size(); i++) {
				Map objectMap = (Map) objectMapList.get(i);
				String strPRDID = (String) objectMap
						.get(DomainConstants.SELECT_ID);
				strPRDIDs[i] = strPRDID;
			}

			String mainDerivedToSelectable = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
					+ "].id";
			String derivedToSelectable = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED + "].id";
			String derivedAbstractToSelectable = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
					+ "].id";
			MapList mlList = DomainObject.getInfo(context, strPRDIDs,
					new StringList(derivedAbstractToSelectable));
			for (int i = 0; i < mlList.size(); i++) {
				Map objectInfo = (Map) mlList.get(i);
				if (objectInfo.containsKey(mainDerivedToSelectable)) {
					derivationTypeList.add(strRevision);
				} else if (objectInfo.containsKey(derivedToSelectable)) {
					derivationTypeList.add(strDerivation);
				} else {
					derivationTypeList.add("");
				}
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return derivationTypeList;
	}
	
	/** get if the selected Manufacturing Plan is  Not in Frozen State
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the parameters passed from the calling method
	 * @return int - Returns true if Manufacturing plan is not in Frozen state 
	 *           else return false
	 * @throws Exception
	 *             if operation fails
	 * @since R214
	 */
	public boolean isManufacturingPlanEditable(Context context, String args[])
	throws FrameworkException {
		// return value of the function
		boolean iReturn = false;
		try {
			
			 HashMap programMap     = (HashMap) JPO.unpackArgs(args);
		        String  strObjectId    = (String) programMap.get("objectId");		
				
		        iReturn=!ManufacturingPlan.isFrozenState(context, strObjectId);
				
			
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return iReturn;
	}

	/**
	 * Gets the Manufacturing Plan Breakdown connected to the Manufacturing Plan
	 * in order to generate composition binary
	 *
	 * @param context The eMatrix <code>Context</code> object
	 * @param args holds a packed hashmap containing objectId
	 * @return MapList containing the data of Active Manufacturing Plans objects with composition information
	 * @throws Exception if the operation fails
	 */

	public MapList getManufacturingPlanBreakdownComposition(Context context, String[] args)
	throws Exception {

		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strObjectid = (String)programMap.get("objectId");
		MapList mapMPBreakdowns=new MapList();
		
		DomainObject domMP=new DomainObject(strObjectid);
		if (domMP.exists(context)
				&& (domMP
						.hasRelatedObjects(
								context,
								ManufacturingPlanConstants.RELATIONSHIP_MANAGED_ROOT,
								false) || domMP
						.hasRelatedObjects(
								context,
								ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION,
								false))) {

			String RELATIONSHIP_SERIES_MASTER = PropertyUtil.getSchemaProperty(context, "relationship_SeriesMaster");
			String RELATIONSHIP_MANAGED_SERIES = PropertyUtil.getSchemaProperty(context, "relationship_ManagedSeries");
			String RELATIONSHIP_MANAGED_ROOT = PropertyUtil.getSchemaProperty(context, "relationship_ManagedRoot");
			String SELECT_MODEL_ID = "to["+RELATIONSHIP_MANAGED_SERIES+"].from.from["+RELATIONSHIP_SERIES_MASTER+"].to.physicalid";
			String SELECT_MODEL_NAME = "to["+RELATIONSHIP_MANAGED_SERIES+"].from.from["+RELATIONSHIP_SERIES_MASTER+"].to.name";
			String ATTRIBUTE_NODE_INDEX = PropertyUtil.getSchemaProperty(context, "attribute_NodeIndex");
			String SELECT_MODEL_ID_MANAGEDROOT = "to["+RELATIONSHIP_MANAGED_ROOT+"].from.from["+RELATIONSHIP_SERIES_MASTER+"].to.physicalid";
			String SELECT_MODEL_NAME_MANAGEDROOT= "to["+RELATIONSHIP_MANAGED_ROOT+"].from.from["+RELATIONSHIP_SERIES_MASTER+"].to.name";

			// Create the instance of ManufacturingPlan bean and call getManufacturingPlanBreakdowns method
			ManufacturingPlan mpBean = new ManufacturingPlan(strObjectid);
			StringList selectStmts = new StringList(9);
			//get basic data for Build
			selectStmts.add(DomainObject.SELECT_ID);
			selectStmts.add(DomainObject.SELECT_TYPE);
			selectStmts.add(DomainObject.SELECT_NAME);
			selectStmts.add(DomainObject.SELECT_REVISION);
			selectStmts.add("physicalid");
			selectStmts.add("revindex"); 
			selectStmts.add("attribute["+ATTRIBUTE_NODE_INDEX+"]");
			//get the Model context
			selectStmts.add(SELECT_MODEL_ID);
			selectStmts.add(SELECT_MODEL_NAME);

			mapMPBreakdowns = (MapList) mpBean.getPreferredManufacturingPlanBreakdowns(context,selectStmts,new StringList(), 1, 0);

			//The context object must be added as the first in the list
			Map mpMap = mpBean.getInfo(context, selectStmts);
			//disabling derivation
			//boolean derivationsEnabled = ManufacturingPlan.isManufacturingPlanDerivationEnabled(context);
			boolean derivationsEnabled = true;


			mpMap.put("derivationsEnabled", derivationsEnabled); 
			mapMPBreakdowns.add(0,mpMap);

			//Need to re-key the Map for config context Id and Name so Composition Binary code can get it
			//add the configContext to each map
			Map dataMap = null;
			for (int idx=0; idx < mapMPBreakdowns.size(); idx++)
			{
				dataMap = (Map)mapMPBreakdowns.get(idx);
				//If this is first MP, then connected with Managed Root, else Managed Series
				//relationship hierarchy issue, kernel changes selectable
				String modelId = (String)dataMap.get(SELECT_MODEL_ID);
				String modelName = (String)dataMap.get(SELECT_MODEL_NAME);
				if (modelId == null || "null".equalsIgnoreCase(modelId) || modelId.length() == 0)
				{
					modelId = (String)dataMap.get(SELECT_MODEL_ID_MANAGEDROOT);
					modelName = (String)dataMap.get(SELECT_MODEL_NAME_MANAGEDROOT);
				}

				dataMap.put("configContextId", modelId);
				dataMap.put("configContextName", modelName);
				//revindex should be different depending if derivations are enabled
				if (derivationsEnabled)
				{
					//put the Node Index attribute as the revindex value
					String nodeIndex = (String)dataMap.get("attribute["+ATTRIBUTE_NODE_INDEX+"]");
					if (nodeIndex == null || "null".equalsIgnoreCase(nodeIndex) || nodeIndex.length() <= 0)
					{
						nodeIndex = "0";
					}
					dataMap.put("revindex", nodeIndex);   			
				}
			}
		}

		return mapMPBreakdowns;
	}

    /**
     * Gets the binary composition data for the Build's Manufacturing Plan allocation
     * 
	 * @param context The eMatrix <code>Context</code> object
	 * @param args holds a packed hashmap containing objectId
	 * @return MapList containing Manufacturing Plan composition information for the Build
	 * @throws Exception if the operation fails
     */
    public MapList getBuildManufacturingPlanAllocation(Context context, String[] args) throws Exception {
        MapList mpList = new MapList();
        Map programMap = (HashMap) JPO.unpackArgs(args);
    	String buildId = (String)programMap.get("objectId");
    	
    	String RELATIONSHIP_PLANNED_BUILDS = PropertyUtil.getSchemaProperty(context, "relationship_PlannedBuilds");
		String RELATIONSHIP_PRODUCTS = PropertyUtil.getSchemaProperty(context, "relationship_Products");
		String RELATIONSHIP_MAIN_PRODUCT = PropertyUtil.getSchemaProperty(context, "relationship_MainProduct");
		String SELECT_MODEL_ID = "to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.to["+RELATIONSHIP_PRODUCTS+"].from.physicalid";
		String SELECT_MODEL_NAME = "to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.to["+RELATIONSHIP_PRODUCTS+"].from.name";
		String SELECT_MODEL_ID_MAINPRODUCT = "to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.to["+RELATIONSHIP_MAIN_PRODUCT+"].from.physicalid";
		String SELECT_MODEL_NAME_MAINPRODUCT = "to["+ProductLineConstants.RELATIONSHIP_PRODUCT_BUILD+"].from.to["+RELATIONSHIP_MAIN_PRODUCT+"].from.name";
		String ATTRIBUTE_NODE_INDEX = PropertyUtil.getSchemaProperty(context, "attribute_NodeIndex");
    	String RELATIONSHIP_SERIES_MASTER = PropertyUtil.getSchemaProperty(context, "relationship_SeriesMaster");
       	String RELATIONSHIP_MANAGED_SERIES = PropertyUtil.getSchemaProperty(context, "relationship_ManagedSeries");
       	String RELATIONSHIP_MANAGED_ROOT = PropertyUtil.getSchemaProperty(context, "relationship_ManagedRoot");
       	String SELECT_MP_MODEL_ID = "to["+RELATIONSHIP_MANAGED_SERIES+"].from.from["+RELATIONSHIP_SERIES_MASTER+"].to.physicalid";
       	String SELECT_MP_MODEL_NAME = "to["+RELATIONSHIP_MANAGED_SERIES+"].from.from["+RELATIONSHIP_SERIES_MASTER+"].to.name";
       	String SELECT_MP_MODEL_ID_MANAGEDROOT = "to["+RELATIONSHIP_MANAGED_ROOT+"].from.from["+RELATIONSHIP_SERIES_MASTER+"].to.physicalid";
       	String SELECT_MP_MODEL_NAME_MANAGEDROOT = "to["+RELATIONSHIP_MANAGED_ROOT+"].from.from["+RELATIONSHIP_SERIES_MASTER+"].to.name";
   	
    	StringList selectStmts = new StringList(17);
    	//get basic data for Build
    	selectStmts.add(DomainObject.SELECT_ID);
    	selectStmts.add(DomainObject.SELECT_TYPE);
    	selectStmts.add(DomainObject.SELECT_NAME);
    	selectStmts.add(DomainObject.SELECT_REVISION);
    	selectStmts.add("attribute["+ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER+"]");
    	selectStmts.add("physicalid");
    	
    	//get the Model context
    	selectStmts.add(SELECT_MODEL_ID);
    	selectStmts.add(SELECT_MODEL_NAME);
    	
    	//get the ManufPlan data
    	selectStmts.add("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.id");
    	selectStmts.add("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.type");
        selectStmts.add("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.name");
        selectStmts.add("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.revision");
        selectStmts.add("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.physicalid");
        selectStmts.add("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.revindex");
        selectStmts.add("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.attribute["+ATTRIBUTE_NODE_INDEX+"]");
        selectStmts.add("to["+RELATIONSHIP_PLANNED_BUILDS+"].from."+SELECT_MP_MODEL_ID);
        selectStmts.add("to["+RELATIONSHIP_PLANNED_BUILDS+"].from."+SELECT_MP_MODEL_NAME);

    	DomainObject buildObj = DomainObject.newInstance(context, buildId);
    	Map buildData = buildObj.getInfo(context, selectStmts);
    	
    	//Create a MapList where the first in the list is the Build and the next is the Product
    	//These are the expected keys needed for composition binary
    	Map contextMap = new HashMap();
    	contextMap.put(DomainObject.SELECT_ID, (String)buildData.get(DomainObject.SELECT_ID));
    	contextMap.put(DomainObject.SELECT_TYPE, (String)buildData.get(DomainObject.SELECT_TYPE));
    	contextMap.put(DomainObject.SELECT_NAME, (String)buildData.get(DomainObject.SELECT_NAME));
    	contextMap.put(DomainObject.SELECT_REVISION, (String)buildData.get(DomainObject.SELECT_REVISION));
    	contextMap.put("physicalid", (String)buildData.get("physicalid"));
    	contextMap.put("revindex", (String)buildData.get("attribute["+ProductLineConstants.ATTRIBUTE_BUILD_UNIT_NUMBER+"]"));
    	String modelId = (String)buildData.get(SELECT_MODEL_ID);
    	String modelName = (String)buildData.get(SELECT_MODEL_NAME);
    	if (modelId == null || "null".equalsIgnoreCase(modelId) || modelId.length() == 0)
    	{
    		modelId = (String)buildData.get(SELECT_MODEL_ID_MAINPRODUCT);
    		modelName = (String)buildData.get(SELECT_MODEL_NAME_MAINPRODUCT);
    	}
    	contextMap.put("configContextId", modelId);
    	contextMap.put("configContextName", modelName);
    	//need to also add whether hierarchical change is enabled
    	//disabling derivation
    	//boolean derivationsEnabled = ManufacturingPlan.isManufacturingPlanDerivationEnabled(context);
    	boolean derivationsEnabled = true;
    	contextMap.put("derivationsEnabled", derivationsEnabled); 
    	mpList.add(contextMap);
    	
    	//Add map for MP data if any found
    	String mpId = (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.id");
    	if (mpId != null && !"null".equalsIgnoreCase(mpId) && mpId.length() > 0)
    	{    		
    		Map mpMap = new HashMap();
    		mpMap.put(DomainObject.SELECT_ID, (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.id"));
    		mpMap.put(DomainObject.SELECT_TYPE, (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.type"));
    		mpMap.put(DomainObject.SELECT_NAME, (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.name"));
    		mpMap.put(DomainObject.SELECT_REVISION, (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.revision"));
    		mpMap.put("physicalid", (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.physicalid"));
    		//revindex should be different depending if derivations are enabled
    		if (derivationsEnabled)
    		{
    			//put the Node Index attribute as the revindex value
    			String nodeIndex = (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.attribute["+ATTRIBUTE_NODE_INDEX+"]");
    			if (nodeIndex == null || "null".equalsIgnoreCase(nodeIndex) || nodeIndex.length() <= 0)
    			{
    				nodeIndex = "0";
    			}
    			mpMap.put("revindex", nodeIndex);   			
    		}
    		else
    		{
       			mpMap.put("revindex", (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from.revindex"));   			    			
    		}
    		//If this is first MP, then connected with Managed Root, else Managed Series
    		//relationship hierarchy issue, kernel changes selectable
    		modelId = (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from."+SELECT_MP_MODEL_ID);
	    	modelName = (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from."+SELECT_MP_MODEL_NAME);
	    	if (modelId == null || "null".equalsIgnoreCase(modelId) || modelId.length() == 0)
	    	{
	    		modelId = (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from."+SELECT_MP_MODEL_ID_MANAGEDROOT);
	    		modelName =  (String)buildData.get("to["+RELATIONSHIP_PLANNED_BUILDS+"].from."+SELECT_MP_MODEL_NAME_MANAGEDROOT);
	    	}
    		mpMap.put("configContextId", modelId);
    		mpMap.put("configContextName", modelName);
    		mpList.add(mpMap);
    	}
    	    	    	
    	return mpList;        	
    }    
    /**
     * Updates the composition binary when a Managed Root relationship
     * is created.
     * 
     * @param context the ENOVIA <code>Context</code> object
     * @param args String array of arguments in the following order      
     *   			[0] = relationship id
     * 				[1] = relationship type
     * 				[2] = from object id
     * 				[3] = to object id 
     * @return int 0 for success and 1 for failure
     * @throws Exception throws exception if the operation fails
     */
    public int updateCompositionBinaryOnManagedSeries(Context context, String[]args) throws Exception
    {
        int returnStatus = 0;
    	String relType = args[1];
    	if(!relType.equals(PropertyUtil.getSchemaProperty("relationship_ManagedRoot"))){
    		args[1]=ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN;
    		returnStatus = JPO.invoke(context, "emxCompositionBinary", args, "updateCompositionBinary", args);
    	}
    	return returnStatus;    
    }
	
	/**
	 * This will be used to render Deriation From column for the search table
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since CFP R212Derivations
	 */
	public StringList getDerivedFromForSearch(Context context, String[] args)
			throws FrameworkException {
		StringList derivedFromList = new StringList();
		try {
			String strLanguage = context.getSession().getLanguage();
			String strRoot = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
							"DMCPlanning.MPDerivation.DerivationType.Root",strLanguage);

			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objectMapList = (MapList) programMap.get("objectList");

			String[] strPRDIDs = new String[objectMapList.size()];
			for (int i = 0; i < objectMapList.size(); i++) {
				Map objectMap = (Map) objectMapList.get(i);
				String strPRDID = (String) objectMap
						.get(DomainConstants.SELECT_ID);
				strPRDIDs[i] = strPRDID;
			}

			String mpMainDerivedType = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_TYPE;
			String mpMainDerivedMarketing = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
					+ "].from."
					+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_MARKETING_NAME;
			String mpMainDerivedTitle = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
					+ "].from."
					+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;
			String mpMainDerivedRevision = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_REVISION;
			String mpMainDerivedId = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_MAIN_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_ID;

			String mpDerivedType = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_TYPE;
			String mpDerivedMarketing = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
					+ "].from."
					+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_MARKETING_NAME;
			String mpDerivedTitle = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
					+ "].from."
					+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;

			String mpDerivedRevision = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_REVISION;
			String mpDerivedId = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED
					+ "].from." + ManufacturingPlanConstants.SELECT_ID;

			String mpAbstractDerivedType = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
					+ "].from." + ManufacturingPlanConstants.SELECT_TYPE;
			String mpAbstractDerivedMarketing = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
					+ "].from."
					+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_MARKETING_NAME;
			String mpAbstractDerivedTitle = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
					+ "].from."
					+ ManufacturingPlanConstants.SELECT_ATTRIBUTE_TITLE;

			String mpAbstractDerivedRevision = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
					+ "].from." + ManufacturingPlanConstants.SELECT_REVISION;
			String mpAbstractDerivedId = "to["
					+ ManufacturingPlanConstants.RELATIONSHIP_DERIVED_ABSTRACT
					+ "].from." + ManufacturingPlanConstants.SELECT_ID;

			StringList slObjSel = new StringList(mpAbstractDerivedId);
			slObjSel.add(mpAbstractDerivedRevision);
			slObjSel.add(mpAbstractDerivedTitle);
			slObjSel.add(mpAbstractDerivedMarketing);
			slObjSel.add(mpAbstractDerivedType);

			MapList mlList = DomainObject.getInfo(context, strPRDIDs, slObjSel);

			Map tempMap;
			for (Iterator iterator = mlList.iterator(); iterator.hasNext();) {
				// checking if we have Id in objectList;
				tempMap = (Map) iterator.next();
				StringBuffer sbBuffer = new StringBuffer(400);
				if (tempMap.containsKey(mpMainDerivedId)) {

					String mpRevision = (String) tempMap
							.get(mpMainDerivedRevision);
					String mpId = (String) tempMap.get(mpMainDerivedId);
					String mpType = (String) tempMap.get(mpMainDerivedType);

					String mpTitle = "";
					if (mxType.isOfParentType(context, mpType,
							ManufacturingPlanConstants.TYPE_PRODUCTS)){
						mpTitle = (String) tempMap.get(mpMainDerivedMarketing);
					}
					else if (mxType.isOfParentType(context, mpType,
							ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN)){
						mpTitle = (String) tempMap.get(mpMainDerivedTitle);
					}
					sbBuffer.append(
							XSSUtil.encodeForHTML(context, mpTitle))
							.append(" ").append(
									XSSUtil.encodeForHTML(context,
											mpRevision));
					derivedFromList.add(sbBuffer.toString());
				} else if (tempMap.containsKey(mpDerivedId)) {
					String mpRevision = (String) tempMap.get(mpDerivedRevision);
					String mpId = (String) tempMap.get(mpDerivedId);
					String mpType = (String) tempMap.get(mpDerivedType);
					String mpTitle = "";
					if (mxType.isOfParentType(context, mpType,
							ManufacturingPlanConstants.TYPE_PRODUCTS)){
						mpTitle = (String) tempMap.get(mpDerivedMarketing);}
					else if (mxType.isOfParentType(context, mpType,
							ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN)){
						mpTitle = (String) tempMap.get(mpDerivedTitle);
					}
					sbBuffer.append(
							XSSUtil.encodeForHTML(context, mpTitle))
							.append(" ").append(
									XSSUtil.encodeForHTML(context,
											mpRevision));
					derivedFromList.add(sbBuffer.toString());
				} else {
					derivedFromList.add(strRoot);
				}
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return derivedFromList;
	}
	
	/** Method call as a trigger to check if the previous MP of current MP is in 
	 * Release state.
	 *
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args -
	 *            Holds the parameters passed from the calling method
	 * @return int - Returns 0 in case of Check trigger is success and 1 in case
	 *         of failure
	 * @throws Exception
	 *             if operation fails
	 * @since R212Derivations
	 */
	public int checkIfPreviousMPDerivationReleased(Context context, String args[])
	throws FrameworkException {
		// return value of the function
		int iReturn = 1;
		try {
			// The feature object id sent by the emxTriggerManager is retrieved here.
			String objectId = args[0];
			boolean isFrozen = false;
			boolean isRootNode = false;
			isRootNode=DerivationUtil.isRootNode(context, objectId);
			if(isRootNode){
				iReturn=0;
			}else{
				ManufacturingPlan mpCurrent= new ManufacturingPlan(objectId);
				Map mpDerivedFrom=mpCurrent.getManufacturingPlanParent(context);
				String strFromMPId = (String) mpDerivedFrom
				.get(DomainObject.SELECT_ID);
				boolean isFrozenParent=ManufacturingPlan.isFrozenState(context, strFromMPId);
				if(isFrozenParent){
					iReturn=0;
				}else{
                    String language = context.getSession().getLanguage();
                    String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
                                    "DMCPlanning.Alert.MPPromoteFailedStateNotRelease",language);
                    emxContextUtilBase_mxJPO
                            .mqlNotice(context, strAlertMessage);
                    iReturn = 1;
				}
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return iReturn;
	}
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getEffManufacturingPlanDerivationChain(Context context,
			String[] args) throws Exception {
		MapList mlMPDerivation = new MapList();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		String contextObjId = (String) programMap.get("objectId");
		String expandLevel = (String) programMap.get("expandLevel");
		// If the ExpandLevel is all then set the recurse level to 0
		if(expandLevel == null){
			expandLevel = "1";
		} else if(ProductLineConstants.RANGE_VALUE_ALL.equalsIgnoreCase(expandLevel)){
			expandLevel = "0";
		}
		MapList mlDerivationStructure = new MapList();
		try {
			short recurseLevel = Short.parseShort(expandLevel);
			int limit = Integer.parseInt(EnoviaResourceBundle.getProperty(context,"emxProduct.ExpandLimit"));
			DomainObject domContextObj= DomainObject.newInstance(context, contextObjId);
			
			CFPModel_mxJPO cfpModelJpo = new CFPModel_mxJPO(context, null);
			
		if(domContextObj.isKindOf(context,ManufacturingPlanConstants.TYPE_MODEL)){
			if(recurseLevel == 1){//get only Main Derived derivations - Revisions
				
				mlDerivationStructure = cfpModelJpo.getManufacturingPlanRevision(context,args);
			}
		else{
			//go through the list of product revisions of model and get entire model's structure
			MapList mainDervML =  cfpModelJpo.getManufacturingPlanRevision(context,args);
			if(mainDervML != null && mainDervML.size() > 0){
				for(int i=0; i < mainDervML.size(); i++){
					Map dervMap = (Map)mainDervML.get(i);
					String dervId = (String)dervMap.get(DomainObject.SELECT_ID);
					//get entire context product's structure including itself
					//added for IR-226670V6R2014x
					if(!expandLevel.equalsIgnoreCase("0")){
						recurseLevel = (short) (recurseLevel-1);
					}
					MapList tmpML = DerivationUtil.getDerivationStructure(context, dervMap, ProductLineConstants.TYPE_MANUFACTURING_PLAN, 1, recurseLevel, limit, true);
					mlDerivationStructure.addAll(tmpML);
				}
			}
		}
		}else{
			mlDerivationStructure = DerivationUtil.getDerivationStructure(context, contextObjId, ProductLineConstants.TYPE_MANUFACTURING_PLAN, 1, recurseLevel, limit);
		}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return mlDerivationStructure;
		
	}
	/**
	 * It will be demote trigger called when demoting MP from Release to
	 * Preliminary to check if it has any derivation and if it is, is any of it
	 * is in Frozen state, if it is in Frozen state, it will not allow to demote
	 * it
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws FrameworkException
	 * @since R212Derivations
	 */
	public int checkIfDerivedMPDerivationReleased(Context context, String args[])
	throws FrameworkException {
		// return value of the function
		int iReturn = 1;
		try {
			// The feature object id sent by the emxTriggerManager is retrieved here.
			String objectId = args[0];
			MapList mpList= DerivationUtil.getAllDerivations(context, objectId, (short)1, (short)0);
			if(mpList.isEmpty()){
				iReturn=0;
			}else{
				for (int i = 0; i < mpList.size(); i++) {
					Map mp= (Map)mpList.get(i);
					String strFromMPId= (String)mp.get(ManufacturingPlanConstants.SELECT_ID);
					boolean isFrozenParent=ManufacturingPlan.isFrozenState(context, strFromMPId);
					if(isFrozenParent){
						String language = context.getSession().getLanguage();
						String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
						"DMCPlanning.Alert.MPDemoteFailedDerivedRelease",language);
						emxContextUtilBase_mxJPO
						.mqlNotice(context, strAlertMessage);
						iReturn = 1;
						break;
					}else{
						iReturn=0;
					}
				}
			}
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return iReturn;
	}
	/**
	 * This is the check trigger which will check If Context product of Manufacturing Plan is not in release/obselete state,
	 * MP will not be released.
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since backported from R213, used in R212Derivations  
	 */
	public int checkIfProductReleased(Context context, String args[])throws Exception{
		// return value of the function
		int iReturn = 0;
		// The Manufacturing Plan object id sent by the emxTriggerManager is retrieved here.
		String objectId = args[0];
		DomainObject domManuPlamn= new DomainObject(objectId);
		String strState=domManuPlamn.getInfo(context, "to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.current");
		if (strState!=null && (strState
				.equalsIgnoreCase(ManufacturingPlanConstants.STATE_RELEASE)||
				strState
				.equalsIgnoreCase(ManufacturingPlanConstants.STATE_OBSOLETE))) {
			iReturn = 0;
		}else{
			iReturn=1;
			String language = context.getSession().getLanguage();
			String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
			"DMCPlanning.Alert.PolicyMPPromoteCheckProductRelease",language);
			throw new FrameworkException(strAlertMessage);
		}
		// Return 0 is validation is passed
		return iReturn;
	}
	
	/**
	 * This is the check trigger which will check on delete of MP, if it is being used in any Released MP as MPB 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R212Derivations  
	 */
	public int checkIfUsedInReleasedMPBreakdown(Context context, String args[])throws Exception{
		// return value of the function
		int iReturn = 0;
		// The Manufacturing Plan object id sent by the emxTriggerManager is retrieved here.
		String objectId = args[0];
		DomainObject domManuPlamn= new DomainObject(objectId);
		StringList strState=domManuPlamn.getInfoList(context, "to["+ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN+"].from.current");
		if (strState!=null && (strState.contains(ManufacturingPlanConstants.STATE_RELEASE)||
				strState.contains(ManufacturingPlanConstants.STATE_OBSOLETE))) {
			iReturn=1;
			String language = context.getSession().getLanguage();
			String strAlertMessage = EnoviaResourceBundle.getProperty(context,SUITE_KEY,
			"DMCPlanning.Alert.MPDeleteFailedUsedInReleasedMPB",language);
			throw new FrameworkException(strAlertMessage);
		}else{
			iReturn = 0;
		}
		// Return 0 is validation is passed
		return iReturn;
	}
	
   /**
	 * Method which does not do anything, But require as Update Program for revision field in Create Page of MF
	 * @param context
	 * @param args
	 * @throws FrameworkException
	 */
	public void emptyProgram(Context context, String []args) throws FrameworkException{
	}
	
    /**
     * This FORM JPO method will return a Revision suggestion if the type is Revision or will return nothing
     * for a Derivation
     * @param context The ematrix context of the request.
     * @param args string array containing packed arguments.
     * @return String containing Revision Value
     * @throws FrameworkException
     */
     public String getFormDerivationRevisionValue(Context context, String[] args) throws FrameworkException {

  		String strRevision = "";
 		try {
 			HashMap programMap = (HashMap) JPO.unpackArgs(args);
 			HashMap requestMap = (HashMap) programMap.get("requestMap");
 			String strObjectId = (String) requestMap.get("objectID");
 	  		String strDerivationType = (String)requestMap.get("DerivationType");
			boolean isRootMP = Boolean.parseBoolean((String)requestMap.get("isRootMP"));
			boolean isFromProductContext = Boolean.parseBoolean((String)requestMap.get("isFromProductContext"));

		    if (strDerivationType.equalsIgnoreCase(DerivationUtil.DERIVATION_TYPE_REVISION)) {
				if (isRootMP) {
					String strType = ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN;
		    		MapList policyList = com.matrixone.apps.domain.util.mxType.getPolicies(context, strType, false);
		    		String strDefaultPolicy = (String)((HashMap)policyList.get(0)).get(DomainConstants.SELECT_NAME);
		       	    Policy policyObject = new Policy(strDefaultPolicy);
		       	    strRevision = policyObject.getFirstInSequence(context);	        
				} else {
					if (strObjectId != null && strObjectId.length() > 0) {
						ProductLineCommon commonBean = new ProductLineCommon();
				      	// This method returns a map containing information about Type, name next revision sequence and description.
				      	// it is used to populate the default values in the dialog box
						Map mpObjectInfo = (HashMap)commonBean.getRevisionInfo(context, strObjectId);
	   		      	    if (mpObjectInfo != null && mpObjectInfo.size() != 0) {
				      	     strRevision = (String) mpObjectInfo.get(DomainConstants.SELECT_REVISION);
	   		      	    }
					}
				}
			}
 		} catch (Exception e) {
 			throw new FrameworkException(e.getMessage());
 		}
 		return strRevision;
     }
     
 	private HashMap<String, HashMap<String, StringList>> getMPsThroughMasterModels(	Context context, HashMap<String, String> idParentMap) throws Exception {
		HashMap<String, HashMap<String, StringList>> parentIdModelIdMpIds = new HashMap<String, HashMap<String, StringList>>();
		
		Set<String> parentIdSet = new HashSet<String>();
		parentIdSet.addAll(idParentMap.values());
		
		String[] parentIdsArr = new String[parentIdSet.size()];
		parentIdSet.toArray(parentIdsArr);
		
		StringList logicalStructRelSubTypes = ProductLineUtil.getChildrenRelTypes(context, ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_STRUCTURES);
		
		String modelSelectable1 = "from[" + ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_STRUCTURES + "].to." +
				"to[" + ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS + "].from.id";
		String modelSelectable2 = "from[" + ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_STRUCTURES + "].to." +
				"to[" + ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT + "].from.id";
		
		ArrayList<String> modelSelectables = new ArrayList<String>(logicalStructRelSubTypes.size() * 2);
		for (Object lsRelType : logicalStructRelSubTypes) {
			modelSelectables.add(modelSelectable1.replace(ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_STRUCTURES, (String)lsRelType));
			modelSelectables.add(modelSelectable2.replace(ManufacturingPlanConstants.RELATIONSHIP_LOGICAL_STRUCTURES, (String)lsRelType));
		}
		
		StringList selectables = new StringList();
		selectables.add(DomainConstants.SELECT_ID);
		selectables.addAll(modelSelectables);
		
		for (String modelSelectable : modelSelectables) {
			DomainConstants.MULTI_VALUE_LIST.add(modelSelectable);
		}
		
		MapList modelsInfo = DomainObject.getInfo(context, parentIdsArr, selectables);
		
		for (String modelSelectable : modelSelectables) {
			DomainConstants.MULTI_VALUE_LIST.remove(modelSelectable);
		}
		
		HashMap<String, StringList> parentIdModelIdsMap = new HashMap<String, StringList>();
		Set<String> allModelIds = new HashSet<String>();
		for (Object mapObj : modelsInfo) {
			Map map = (Map)mapObj;
			StringList modelIds = new StringList();
			
			Object modelsObj = null;
			for (String modelSelectable : modelSelectables) {
				if(map.containsKey(modelSelectable))
				{
					modelIds = (StringList)map.get(modelSelectable);
					break;
				}
			}
			String parentId = (String) map.get(DomainConstants.SELECT_ID);
			
			allModelIds.addAll(modelIds);
			parentIdModelIdsMap.put(parentId, modelIds);
		}
		
		String[] modelIdsArr = new String[allModelIds.size()];
		allModelIds.toArray(modelIdsArr);
		
		String mpSelectable = "from[" + ManufacturingPlanConstants.RELATIONSHIP_PRODUCTS + "].to." +
				"from[" + ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].to.id";
		
		String mpSelectable2 = "from[" + ManufacturingPlanConstants.RELATIONSHIP_MAIN_PRODUCT + "].to." +
		        "from[" + ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN + "].to.id";
		
		StringList mpSelectables = new StringList();
		mpSelectables.add(mpSelectable);
		mpSelectables.add(DomainConstants.SELECT_ID);
		mpSelectables.add(mpSelectable2);
		
		DomainConstants.MULTI_VALUE_LIST.add(mpSelectable);
		DomainConstants.MULTI_VALUE_LIST.add(mpSelectable2);
		
		MapList mpsInfo = DomainObject.getInfo(context, modelIdsArr, mpSelectables);
		
		DomainConstants.MULTI_VALUE_LIST.remove(mpSelectable);
		DomainConstants.MULTI_VALUE_LIST.remove(mpSelectable2);
		
		HashMap<String, StringList> modelIdMpIdsMap = new HashMap<String, StringList>();
		for (Object mapObj : mpsInfo) {
			Map map = (Map)mapObj;
			
			String modelId = (String) map.get(DomainConstants.SELECT_ID);
			StringList mpIds = (StringList)map.get(mpSelectable);			
			if(mpIds != null)
			{
				modelIdMpIdsMap.put(modelId, mpIds);
			}else
			{
				StringList mpIds2 = (StringList)map.get(mpSelectable2);
				if(mpIds2 != null){
					StringList slremoveDUP=new StringList();
					//Main Product child of Products its returning same MP multiple times
					for(Object eachMP : mpIds2){
						if(!slremoveDUP.contains((String)eachMP)){
							slremoveDUP.add((String)eachMP);
						}
					}
					modelIdMpIdsMap.put(modelId, slremoveDUP);	
				}else{
					modelIdMpIdsMap.put(modelId, mpIds2);
				}
							
			}
		}
		
		for (Entry<String, StringList> entry : parentIdModelIdsMap.entrySet()) {
			String parentId = entry.getKey();
			
			HashMap<String, StringList> modelIdMpIdsMap_parentwise = parentIdModelIdMpIds.get(parentId);
			if(modelIdMpIdsMap_parentwise == null)
				modelIdMpIdsMap_parentwise = new HashMap<String, StringList>();
			StringList modelIds = entry.getValue();
			for (Object modelIdObj : modelIds) {
				StringList modelMps = modelIdMpIdsMap.get((String)modelIdObj);
				modelIdMpIdsMap_parentwise.put((String)modelIdObj, modelMps);
			}
			parentIdModelIdMpIds.put(parentId, modelIdMpIdsMap_parentwise);
		}
        		
		return parentIdModelIdMpIds;
	}
 	
	private String getParentIdFromObjectMap(Context context, Map objectMap, HashMap paramMap) throws Exception {
		String strParentId = (String) objectMap.get("id[parent]");

		//Getting ObjectId of the product from relationship id 
		if(null == strParentId || "null".equals(strParentId) || "".equals(strParentId)){
			String relAssoManPlanId = (String) paramMap.get("relId");
			if(null != relAssoManPlanId && !"null".equals(relAssoManPlanId) && !"".equals(relAssoManPlanId)){
				StringList selects = new StringList();
				selects.addElement(DomainConstants.SELECT_FROM_ID);
				selects.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);
				MapList relMapList = DomainRelationship.getInfo(context, new String[] {relAssoManPlanId}, selects);
				if(relMapList!=null && relMapList.size()>0){
					Map relMap=(Map)relMapList.get(0);
					if(ProductLineCommon.isKindOfRel(context, (String)relMap.get(SELECT_RELATIONSHIP_TYPE), ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN)||
							ProductLineCommon.isKindOfRel(context, (String)relMap.get(SELECT_RELATIONSHIP_TYPE), ManufacturingPlanConstants.RELATIONSHIP_MANAGED_REVISION)){
						strParentId = (String)relMap.get(SELECT_FROM_ID);
					}else if(ProductLineCommon.isKindOfRel(context,  (String)relMap.get(SELECT_RELATIONSHIP_TYPE), ManufacturingPlanConstants.RELATIONSHIP_MANUFACTURING_PLAN_BREAKDOWN)){
						String strMPID = (String) paramMap.get("objectId");
						if(ProductLineCommon.isNotNull(strMPID)){
							DomainObject domMP = new DomainObject(strMPID);
							if(domMP.isKindOf(context, ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN)){
								String strAssociatedPRDId=domMP.getInfo(context, "to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.id");
								strParentId = strAssociatedPRDId ;
							}
						}
					}
				}
			}else{
				String strMPID = (String) paramMap.get("objectId");
				if(ProductLineCommon.isNotNull(strMPID)){
					DomainObject domMP = new DomainObject(strMPID);
					if(domMP.isKindOf(context, ManufacturingPlanConstants.TYPE_MANUFACTURING_PLAN)){
						String strAssociatedPRDId=domMP.getInfo(context, "to["+ManufacturingPlanConstants.RELATIONSHIP_ASSOCIATED_MANUFACTURING_PLAN+"].from.id");
						strParentId = strAssociatedPRDId ;
					}
				}
			}
		}
		return strParentId;
	}
}
