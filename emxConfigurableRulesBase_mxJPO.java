/*
 ** emxConfigurableRulesBase
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 ** static const char RCSID[] = $Id: /ENOVariantConfigurationBase/CNext/Modules/ENOVariantConfigurationBase/JPOsrc/base/${CLASSNAME}.java 1.32.2.7.1.1 Wed Oct 29 22:27:16 2008 GMT przemek Experimental${CLASSNAME}.java 1.8 Fri Sep 14 11:29:22 2007 GMT ds-pborgave Experimental$
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import matrix.db.BusinessObject;
import matrix.db.BusinessType;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Person;
import matrix.db.Policy;
import matrix.util.StringList;

import com.matrixone.apps.configuration.ConfigurationConstants;
import com.matrixone.apps.configuration.ConfigurationUtil;
import com.matrixone.apps.configuration.InclusionRule;
import com.matrixone.apps.configuration.Part;
import com.matrixone.apps.configuration.Product;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.productline.ProductLineCommon;
import com.matrixone.apps.productline.ProductLineConstants;

/**
 * This JPO class has some method pertaining to Configurable Rules type.
 * @author Enovia MatrixOne
 * @version ProductCentral 10.5 - Copyright (c) 2004, MatrixOne, Inc.
 */
public class emxConfigurableRulesBase_mxJPO extends emxDomainObject_mxJPO
{
   /**
    * Alias used for blank space.
    */
    protected static final String BLANK_SPACE = "    ";

   /**
    * Alias used for Label.
    */
    protected static final String LABEL = "LABEL";

   /**
    * Alias used for image HREF.
    */
    protected static final String IMAGE_HREF = "IMAGE_HREF";

   /**
    * Alias used for HREF.
    */
    protected static final String HREF = "HREF";

   /**
    * Alias used for current state.
    */
    protected static final String CURRENT = DomainConstants.SELECT_CURRENT;

   /**
    * Alias used for policy.
    */
    protected static final String POLICY = DomainConstants.SELECT_POLICY;

   /**
    * Alias used for type Product.
    */
    protected static final String TYPE_PRODUCT =
        ProductLineConstants.TYPE_PRODUCTS;

  /**
    * Alias used for type Model.
    */
    protected static final String TYPE_PRODUCTLINE =ProductLineConstants.TYPE_PRODUCTLINE;

   /**
    * Alias used for type Product Line.
    */
    protected static final String TYPE_MODEL =ProductLineConstants.TYPE_MODEL;


   /**
    * Alias used for rule creation.
    */
    protected static final String KEY_FIRST_PART =
        "emxProduct.ProductRuleCreation";

   /**
    * Alias used for not allowed state.
    */
    protected static final String KEY_LAST_PART = "NotAllowedStates";

   /**
    * Alias used for suite string.
    */
    private static final String SUITE_KEY = "Configuration";
  protected static final String SIMPLE_INCLUSION_RULE_KEY = "emxProduct.RuleCreation.SimpleInclusion";
  protected static final String COMPLEX_INCLUSION_RULE_KEY = "emxProduct.RuleCreation.ComplexInclusion";

   /**
    * Alias used for dot.
    */
    protected static final String DOT = ".";

    // Added by Enovia MatrixOne for Bug # 311643 Date 09 Nov, 2005
    private final static String RELATIONSHIP_NAME = "relationship";

    /**
     * Default Constructor.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since ProductCentral 10-0-0-0
     * @grade 0
     */
    emxConfigurableRulesBase_mxJPO(Context context, String[] args) throws Exception
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
        if (!context.isConnected())
        {
            //String sContentLabel = i18nNow.getI18nString("emxProduct.Error.UnsupportedClient","emxConfigurationStringResource",context.getSession().getLanguage());
            //throw  new Exception(sContentLabel);
            String strLanguage = context.getSession().getLanguage();
            String strContentLabel =
                EnoviaResourceBundle.getProperty(context,
                    SUITE_KEY,
                    "emxProduct.Error.UnsupportedClient",
                    strLanguage);
            throw new Exception(strContentLabel);
        }
        return 0;
    }

    /**
     * This method is used to return the status icon of an object
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return List- the List of Strings in the form of 'Name Revision'
     * @throws Exception if the operation fails
     * @since ProductCentral 10.6SP1
     * this could not be deprecated as referenced from migration JPO
    **/

    // Modified by Enovia MatrixOne for Bug # 311635 Date 11 Nov, 2005
    public static List getExpression (Context context, String[] args) throws Exception{
        //unpack the arguments

        List lstExpressionList = new StringList();
        HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
        List lstobjectList = (MapList) parametersMap.get("objectList");
        String strObjectId = DomainConstants.EMPTY_STRING;
        String strRelId = DomainConstants.EMPTY_STRING;
        String strIncRuleID = DomainConstants.EMPTY_STRING;
        String strFeatListId = DomainConstants.EMPTY_STRING;

        for (int i = 0; i < lstobjectList.size(); i++)
        {
            Map objectMap = (Map) lstobjectList.get(i);
            strObjectId = (String) objectMap.get("id");
            strRelId = (String) objectMap.get("id[connection]");
            InclusionRule incRuleBean = new InclusionRule();
            Map incRuleMap = incRuleBean.getInclusionRuleDetails(context, strObjectId, strRelId);

            if (incRuleMap != null) {
                strFeatListId = (String)incRuleMap.get("FeatureListId");
            }
            //Begin of Add by Vibhu,Enovia MatrixOne for Bug 311635 on 11/3/2005
            else
            {
                lstExpressionList.add(DomainConstants.EMPTY_STRING);
                continue;
            }
            //End of Add by Vibhu,Enovia MatrixOne for Bug 311635 on 11/3/2005
            MapList mlIncRule = new MapList();

            try{
            DomainObject dom = DomainObject.newInstance(context , strFeatListId);
            StringList slObjectSel = new StringList();
            slObjectSel.add("attribute["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"]");
            slObjectSel.add("attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]");      
            slObjectSel.addElement(DomainConstants.SELECT_ID);
            mlIncRule = dom.getRelatedObjects(context,
                                    ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION,
                                    ProductLineConstants.TYPE_INCLUSION_RULE,
                                    slObjectSel,
                                    null,
                                    true,
                                    true,
                                    (short)1,
                                    null,
                                    null, 0);

            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
            if (!(mlIncRule.isEmpty()))
            {

                Map mpTemp = (Map)mlIncRule.get(0);
                strIncRuleID = (String)mpTemp.get(DomainConstants.SELECT_ID);
                Map mapTemp = new HashMap();
                MapList objectList = new MapList();
                Map paramList = new HashMap();
                Map programMap = new HashMap();
                String[] arrJPOArguments=new String[1];
                mapTemp.put(DomainConstants.SELECT_ID,strIncRuleID.trim());
                mapTemp.put("attribute["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"]",(String)mpTemp.get("attribute["+ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION+"]"));
                mapTemp.put("attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]",(String)mpTemp.get("attribute["+ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]"));
                objectList.add(mapTemp);
                paramList.put("intermediate", "true");
                programMap.put("objectList", objectList);
                programMap.put("paramList", paramList);
                arrJPOArguments = JPO.packArgs(programMap);
                StringList strRightExpressionList = new StringList();
                strRightExpressionList = (StringList)(JPO.invoke(context,"emxBooleanCompatibility",null,"getRightExpressionforListPage",arrJPOArguments,StringList.class));
                lstExpressionList.add((String)strRightExpressionList.get(0));

            }
            else
            {
                lstExpressionList.add(DomainConstants.EMPTY_STRING);
            }
        }
        return lstExpressionList;
    }
   
/**
	 * This will retrieve the Rule Type, either Inclusion on Simple Inclusion,
	 * and will be a href, for configuring the Inclusion rule
	 * 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211
	 */
	public Object showGBOMInclusionRuleLink(Context context, String[] args) throws Exception {
		Vector vGBOMInclusionRuleLink = new Vector();
		
		try {
			String strLanguage = context.getSession().getLanguage();
			HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
			MapList objectList = (MapList) parametersMap.get("objectList");
			String strRuleType = "";
			StringBuffer sb = new StringBuffer();

			
			boolean flag2 = false;
			Map paramMap = (Map) parametersMap.get("paramList");
			String strReportFormat = (String) paramMap.get("reportFormat");
			String strParentOID = (String) paramMap.get("objectId");
			// htTempTable.put("gbomID", strTemp1a);
			// getting list of GBOM objects to be queried


			String ruleComplexity = "";
			String strRuleExcludeOrInclude = "";
			String strRuleTypeDisplay = "";
			String strRuleTypeDisplayKey = "";
			boolean isRuleExists = true;
			StringList designVariants = new StringList();
			// part id
			String objectType = "";
			String relFromType = "";
			
			Map objectMap;
			for (int j = 0; j < objectList.size(); j++) {
				objectMap = (Map) objectList.get(j);
				objectType = (String) objectMap.get(DomainConstants.SELECT_TYPE);
				relFromType=(String) objectMap.get(DomainRelationship.SELECT_FROM_TYPE);
				ruleComplexity = "";
				strRuleExcludeOrInclude = "";
				strRuleTypeDisplay = "";
				strRuleTypeDisplayKey = "";
				isRuleExists = true;
				String strIncRuleID="";
				
				String strIRuleSel = new StringBuffer("tomid[")
							.append(ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION)
							.append("].from.id").toString();
				
				String strRuleComplSel = new StringBuffer("tomid[")
							.append(ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION)
							.append("].from.attribute[")
							.append(ConfigurationConstants.ATTRIBUTE_RULE_COMPLEXITY)
							.append("]").toString();
				
				if (objectMap.get(strIRuleSel) != null) {
					strIncRuleID = (String) objectMap.get(strIRuleSel);
				}
				if (objectMap.get(strRuleComplSel) != null)
					ruleComplexity = (String) objectMap.get(strRuleComplSel);
				
				if (objectMap.get("attribute[" + ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]") != null){
					strRuleExcludeOrInclude = (String) objectMap.get("attribute["+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
					strRuleTypeDisplayKey = "emxFramework.Range.Rule_Type."+strRuleExcludeOrInclude;
					strRuleTypeDisplay = EnoviaResourceBundle.getProperty(context,"Framework", strRuleTypeDisplayKey,strLanguage);
				}
				
				// TODO DV Related
				String dvselectable = new StringBuffer("from.from[").append(
						ConfigurationConstants.RELATIONSHIP_VARIES_BY).append(
						"].to.id").toString();
				if (objectMap.get(dvselectable) != null){
					if (objectMap.get(dvselectable) instanceof StringList) {
						designVariants = (StringList) objectMap.get(dvselectable);
					} else if (objectMap.get(dvselectable) instanceof String) {
						String strConfOptionsOLDRelId = (String) objectMap.get(dvselectable);
						designVariants.add(strConfOptionsOLDRelId);
					}
				}
				
				// checking for dvs if IR not present
				if (ruleComplexity == null || "".equals(ruleComplexity)) {
					isRuleExists = false;
					if (designVariants.size() > 0 && 
							!mxType.isOfParentType(context, relFromType,ConfigurationConstants.TYPE_PRODUCTS) &&
							!mxType.isOfParentType(context, objectType, ConfigurationConstants.TYPE_PART_FAMILY)) {
						ruleComplexity = "Simple";
					} else {
						ruleComplexity = "Complex";
					}
				}
				
				StringBuffer sIncLink = new StringBuffer(260);
				// TODO- Need to Configure the Hrefs
				if(ruleComplexity.equals("Simple")){
					strRuleType = EnoviaResourceBundle.getProperty(context,SUITE_KEY, SIMPLE_INCLUSION_RULE_KEY,strLanguage);
				}
				else{
					strRuleType = EnoviaResourceBundle.getProperty(context,SUITE_KEY, COMPLEX_INCLUSION_RULE_KEY,strLanguage);
				}				
				
				if (designVariants != null && !("".equals(designVariants)) && !("null".equals(designVariants)) && designVariants.size() > 0
						&& mxType.isOfParentType(context, objectType, ConfigurationConstants.TYPE_PART_FAMILY)
						&& !mxType.isOfParentType(context, relFromType, ConfigurationConstants.TYPE_PRODUCTS)) {
					
					flag2 = true;
					sIncLink = sIncLink
					.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/GBOMRuleExpressionPreProcess.jsp?modetype=create");
				
				} 
				else if (ruleComplexity.equals("Simple")){
					
					flag2 = true;		
					sIncLink = sIncLink
					.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/GBOMSimpleInclusionPreProcess.jsp?modetype=create&amp;jsTreeID=");					
					strRuleTypeDisplay = strRuleType;	
				}
				else{
					if(isRuleExists){
						sIncLink.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/CreateRuleDialog.jsp?modetype=edit%26commandName=FTRInclusionRuleSettings%26ruleType=InclusionRule%26SuiteDirectory=configuration%26hyperLinkCheck=yes");
						sIncLink.append( "%26submitURL=");
						sIncLink.append( "../configuration/InclusionRuleEditPostProcess.jsp?mode=edit%26ruleType=InclusionRule%26context=GBOM%26");
						
					}else{
						sIncLink.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/CreateRuleDialog.jsp?modetype=create%26commandName=FTRInclusionRuleSettings%26ruleType=InclusionRule%26SuiteDirectory=configuration%26hyperLinkCheck=yes");
						sIncLink.append( "%26submitURL=");
						sIncLink.append( "../configuration/InclusionRuleEditPostProcess.jsp?mode=create%26ruleType=InclusionRule%26context=GBOM%26");
					}					
				}

				String strEndPart1 = "";
				if (flag2)
					strEndPart1 = "', '500', '400', 'true', 'popup')\">";
				else
					strEndPart1 = "', '800', '700', 'true', 'popup', '','','false','Large')\">";
				String strEndPart2 = "</a>";
				String strJsTreeID = (String) parametersMap.get("jsTreeID");

				sIncLink = sIncLink.append(strJsTreeID);
				sIncLink = sIncLink.append("&amp;parentOID=").append(strParentOID);
				String strProductID = "";

				if (paramMap.containsKey("prodId")) {
					strProductID = (String) paramMap.get("prodId");
				}
				sIncLink = sIncLink.append("&amp;productID=").append(strProductID);

				sb = new StringBuffer(340);
				sb = sb.append(sIncLink.toString());
				sb = sb.append("&amp;objectId=").append(
						(String) objectMap.get("id"));
				sb = sb.append("&amp;relId=").append(
						(String) objectMap.get("id[connection]"));
				sb = sb.append("&amp;iRuleId=").append(strIncRuleID);
				sb = sb.append("&amp;partName=").append(
						XSSUtil.encodeForXML(context,(String) objectMap.get(DomainObject.SELECT_NAME)));
				sb = sb.append("&amp;lfeatureId=").append(
						(String) objectMap.get(DomainRelationship.SELECT_FROM_ID));
		        sb = sb.append("&amp;ruleExcludeOrInclude=").append(strRuleExcludeOrInclude);
		        sb = sb.append("&amp;gbomType=").append((String)objectMap.get(DomainObject.SELECT_TYPE));
				
				sb = sb.append(strEndPart1).append(strRuleTypeDisplay).append(
						strEndPart2);
		
				if (strReportFormat != null
						&& strReportFormat.equals("null") == false
						&& strReportFormat.equals("") == false && strReportFormat.length()>0)
					vGBOMInclusionRuleLink.add(strRuleTypeDisplay);
				else
					vGBOMInclusionRuleLink.add(sb.toString());
		
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return vGBOMInclusionRuleLink;
	}

	/**
	 * This will be used to render the Inclusion Rule Expression column in the GBOM Summary Page 
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 * @since R211
	 */
	public static List getGBOMExpression(Context context, String[] args)
	throws Exception {
		List lstExpressionList = new StringList();
		List finalExpressionList= new StringList();
		try {
			HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
			List lstobjectList = (MapList) parametersMap.get("objectList");
			// TODO- Need to handle CG related expression
			String strLeftExpr = new StringBuffer("tomid[").append(
					ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION).append(
					"].from.attribute[").append(
							ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION).append(
							"]").toString();
			String strRightExpr = new StringBuffer("tomid[").append(
					ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION).append(
					"].from.attribute[").append(
							ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION).append(
							"]").toString();
			for (int i = 0; i < lstobjectList.size(); i++) {
				Map objectMap = (Map) lstobjectList.get(i);
				String leftExpression = "";
				String rightExpression = "";
				if (objectMap.containsKey(strLeftExpr)) {
					leftExpression = (String) objectMap.get(strLeftExpr);
				}
				if (objectMap.containsKey(strRightExpr)) {
					rightExpression = (String) objectMap.get(strRightExpr);
				}
				objectMap.put("attribute["
						+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION
						+ "]", leftExpression);
				objectMap.put("attribute["
						+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION
						+ "]", rightExpression);
			}

			lstExpressionList = (StringList) (JPO.invoke(context,
					"emxBooleanCompatibility", null,
					"showInclusionRuleExpression", JPO.packArgs(parametersMap),
					StringList.class));
			
			for (int i = 0; i < lstExpressionList.size(); i++) {
				String strExp=(String)lstExpressionList.get(i);
				finalExpressionList.add(strExp);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return finalExpressionList;
	}

	// public static List showInclusionRuleExpression(Context context,
	// String args[]) throws Exception {
	// String strRuleExp = DomainConstants.EMPTY_STRING;
	// StringBuffer strBuffer = new StringBuffer(400);
	// List ruleList = new StringList();
	// List ruleCGList = new StringList();
	//
	// // calling the method to get the rule expression
	// ruleList = getGBOMExpression(context, args);
	// // Start - modifying the rule expression to get the Common Group name
	//
	// // Call a common method which will process the list
	// // ruleCGList = updateInclusionRuleForCommonGroup(context,
	// // (matrix.util.List) ruleList);
	// return ruleList;
	// }
	/**

    /**
     * This method is used to return inclusion/exclusion rule link for Selection view In Table column.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds arguments
     * @return Vector
     * @throws Exception if the operation fails
	 * @since R212
	 * @author IXH
	 * @category 
	 */


 public Vector showInclusionRuleLinkInRuleTypeColumn(Context context, String[] args)
        throws Exception
    {
        HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
        List lstobjectList = (MapList) parametersMap.get("objectList");
        Vector vInclusionRuleLink = showInclusionRuleLink(context,args);

        Map objectMap = null;

        Vector vtIncRuleLink = new Vector();

        String strRelType = DomainConstants.EMPTY_STRING;

        for (int j=0; j<lstobjectList.size(); j++)
        {
            objectMap = (Map) lstobjectList.get(j);

            strRelType = (String)objectMap.get(RELATIONSHIP_NAME);
            if(strRelType!=null){
            
            
            if(ProductLineCommon.isOfParentRel(context,strRelType,ConfigurationConstants.RELATIONSHIP_LOGICAL_STRUCTURES)
            		||ProductLineCommon.isOfParentRel(context,strRelType,ConfigurationConstants.RELATIONSHIP_CONFIGURATION_STRUCTURES)
            		|| ProductLineCommon.isOfParentRel(context,strRelType,ConfigurationConstants.RELATIONSHIP_CONFIGURATION_FEATURES)
            		||ProductLineCommon.isOfParentRel(context,strRelType,ConfigurationConstants.RELATIONSHIP_MANUFACTURING_STRUCTURES))
            {
                vtIncRuleLink.add(vInclusionRuleLink.get(j));
            }
            else
            {
                vtIncRuleLink.add("");
            }
            }
            else
            {
                vtIncRuleLink.add("");

            }
        }
        return vtIncRuleLink;
    }
 
 
	/**
	 * This method is used to return inclusion/exclusion rule link.
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds arguments
	 * @return Vector
	 * @throws Exception
	 *             if the operation fails
	 * @since R212
	 * @author IXH
	 * @category
	  
  */

	public Vector showInclusionRuleLink(Context context, String[] args)
			throws Exception {
		
		String strObjectId;
		String strRelId;
		String strLevel = DomainConstants.EMPTY_STRING;
		Vector vInclusionRuleLink = new Vector();
		HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
		Map paramMap = (Map) parametersMap.get("paramList");
		boolean isFTRUser = ConfigurationUtil.isFTRUser(context);
		String strReportFormat = (String) paramMap.get("reportFormat");
		String strParentOID = (String) paramMap.get("objectId");
		
		List lstobjectList = (MapList) parametersMap.get("objectList");

		StringBuffer sb;
		String strLanguage = context.getSession().getLanguage();
		String strRuleTypeDisplayKey = DomainConstants.EMPTY_STRING;
		String strRuleTypeDisplay = DomainConstants.EMPTY_STRING;
		String strExpression = DomainConstants.EMPTY_STRING;
		String strRuleType = DomainConstants.EMPTY_STRING;
		Map objectMap;
		boolean checkType = false;

		String strtempRepFormat = strReportFormat;

		for (int i = 0; i < lstobjectList.size(); i++) {
			objectMap = (Map) lstobjectList.get(i);
			strLevel = (String)objectMap.get("id[level]");			
			String strIncRuleID="";
			StringList slRuleType=new StringList();
			Object leftExpFromType=(Object) objectMap
			.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].from.type");
			slRuleType=ConfigurationUtil.convertObjToStringList(context,leftExpFromType);
			boolean hasIR=false;
			int iRuleIndex=-1;
			if(slRuleType!=null && slRuleType.contains(ConfigurationConstants.TYPE_INCLUSION_RULE)){
				iRuleIndex=slRuleType.indexOf(ConfigurationConstants.TYPE_INCLUSION_RULE);
				hasIR=true;
			}
			if(iRuleIndex!=-1){
				Object iRuleObjects=(Object) objectMap
				.get("tomid["+ ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+ "].from.id");
				StringList slRuleIds=new StringList();
				slRuleIds=ConfigurationUtil.convertObjToStringList(context,iRuleObjects);
				strIncRuleID = (String)slRuleIds.get(iRuleIndex);
			}
			StringBuffer sIncLink = new StringBuffer(260);
			String strEndPart1 = "";
			String strMidPart1 = "";
			String strMidPart2 = "";
			String strEndPart2 = "";

			if (hasIR) {

				sIncLink = sIncLink
						.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/CreateRuleDialog.jsp?rowLevel="+strLevel+"%26modetype=edit%26commandName=FTRInclusionRuleSettings%26ruleType=InclusionRule%26SuiteDirectory=configuration%26hyperLinkCheck=yes");
				sIncLink.append("%26submitURL=");
				sIncLink
						.append("../configuration/InclusionRuleEditPostProcess.jsp?mode=edit%26ruleType=InclusionRule");

				strEndPart1 = "', '800', '700', 'true', 'popup', '','','false','Large')\"";
				strMidPart1 = " TITLE=";
				strMidPart2 = ">";
				strEndPart2 = "</a>";

			} else {
				sIncLink = sIncLink
						.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/CreateRuleDialog.jsp?rowLevel="+strLevel+"%26modetype=create%26commandName=FTRInclusionRuleSettings%26ruleType=InclusionRule%26SuiteDirectory=configuration%26hyperLinkCheck=yes");
				sIncLink.append("%26submitURL=");
				sIncLink
						.append("../configuration/InclusionRuleEditPostProcess.jsp?mode=create%26ruleType=InclusionRule");

				strEndPart1 = "', '800', '700', 'true', 'popup', '','','false','Large')\"";
				strMidPart1 = " TITLE=";
				strMidPart2 = ">";
				strEndPart2 = "</a>";

			}

			sIncLink = sIncLink.append("&amp;parentOID=").append(XSSUtil.encodeForHTMLAttribute(context, strParentOID))
					.append("&amp;objectId=");
			
			strObjectId = (String) objectMap.get("id");
			DomainObject doObject = DomainObject.newInstance(context);
			doObject.setId(strObjectId);
			String typeFeature = doObject.getInfo(context,
					DomainConstants.SELECT_TYPE);

			if (mxType.isOfParentType(context, typeFeature,
					ConfigurationConstants.TYPE_CONFIGURATION_FEATURES)
					|| mxType.isOfParentType(context, typeFeature,
							ConfigurationConstants.TYPE_PRODUCTS)
					|| mxType.isOfParentType(context, typeFeature,
							ConfigurationConstants.TYPE_LOGICAL_STRUCTURES)) {
				checkType = true;
			}
			strRelId = (String) objectMap.get("id[connection]");
			DomainRelationship domRel = new DomainRelationship();
			Map ht = new Hashtable();
			StringList objSelect = new StringList(2);
			objSelect.add("attribute["
					+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");

			if (strRelId != null && !("".equals(strRelId))
					&& !("null".equals(strRelId))) {
				domRel = new DomainRelationship(strRelId);
				ht = domRel.getRelationshipData(context, objSelect);
				strRuleType = (String) ((StringList) ht
						.get(ConfigurationConstants.SELECT_RULE_TYPE)).get(0);
			} else {
				strReportFormat = strtempRepFormat;
				strRuleType = DomainConstants.EMPTY_STRING;
			}

			sb = new StringBuffer();
			sb = sb.append(sIncLink.toString());
			sb = sb.append(XSSUtil.encodeForHTMLAttribute(context, strObjectId)).append("&amp;relId=").append(XSSUtil.encodeForHTMLAttribute(context, strRelId))
					.append("&amp;ruleId=").append(XSSUtil.encodeForHTMLAttribute(context, strIncRuleID));
			if(ProductLineCommon.isNotNull(strRuleType)){
				strRuleTypeDisplayKey = "emxFramework.Range.Rule_Type."
					+ strRuleType.toString();
				strRuleTypeDisplay = EnoviaResourceBundle.getProperty(context,
						"Framework",strRuleTypeDisplayKey,strLanguage);
			}
			String strRuleTypeToolTip = strRuleTypeDisplay.toString();
			sb = sb.append(strEndPart1).append(strMidPart1).append(
					"\"" + XSSUtil.encodeForHTMLAttribute(context, strRuleTypeToolTip) + "\"").append(
					strExpression.trim()).append(strMidPart2).append(
					XSSUtil.encodeForXML(context, strRuleTypeDisplay)).append(strEndPart2);

			if ((strReportFormat != null
					&& strReportFormat.equals("null") == false
					&& strReportFormat.equals("") == false
					&& strReportFormat.length()>0)||!isFTRUser) {
				vInclusionRuleLink.add(strRuleTypeToolTip);
			} else {

				if (checkType
						&& !typeFeature
								.equals(ConfigurationConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE)
						&& !typeFeature
								.equals(ConfigurationConstants.TYPE_FIXED_RESOURCE)
						&& !typeFeature
								.equals(ConfigurationConstants.TYPE_RULE_EXTENSION)) {
					vInclusionRuleLink.add(sb.toString());
				} else {
					vInclusionRuleLink.add("");
				}

			}
		}
		return vInclusionRuleLink;
	}
	
	/**
	 * Method call to show the Rule Expression dialog Page- Called from Inactive part Tables Rule Expression Column, 
	 * @param context the eMatrix <code>Context</code> object
	 * @param args contains a Map with the following entries:
	 *        objectId - a String giving object id
	 *        reportFormat - a String indicating wheather method is called from table or report.
	 * @return List - A Vector containing the HTML code to generate a HREF for rule type.
	 * @throws Exception if the operation fails
	 * @author np4 
	 */
	public List showRuleExpressionForInactiveGBOM(Context context, String[] args)
	    throws Exception
	    {
		Vector vInclusionRuleLink = new Vector();
		try {
			StringBuffer sIncLink = new StringBuffer(260);
			sIncLink = sIncLink
					.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/GBOMRuleExpressionPreProcess.jsp?modetype=create");
			String strEndPart1 = "', '500', '400', 'true', 'popup')\">";
			String strEndPart2 = "</a>";

			HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
			String strJsTreeID = (String) parametersMap.get("jsTreeID");
			sIncLink = sIncLink.append(strJsTreeID);
			Map paramMap = (Map) parametersMap.get("paramList");
			String strReportFormat = (String) paramMap.get("reportFormat");
			String strParentOID = (String) paramMap.get("objectId");
			sIncLink = sIncLink.append("&amp;parentOID=").append(strParentOID);
			String strProductID = "";

			if (paramMap.containsKey("productID")) {
				strProductID = (String) paramMap.get("productID");
			}
			sIncLink = sIncLink.append("&amp;productID=").append(strProductID);

			String strLanguage = context.getSession().getLanguage();
			MapList objectList = (MapList) parametersMap.get("objectList");
			String strRelId = "";
			String strRuleType = "";
			String iRuleID = "";
			String logicalFTRID = "";
			String gbomName = "";
			String gbomType = "";
			StringBuffer sb;
			Map objectMap;

			for (int i = 0; i < objectList.size(); i++) {
				sb = new StringBuffer(340);
				sb = sb.append(sIncLink.toString());
				objectMap = (Map) objectList.get(i);
				sb = sb.append("&amp;objectId=").append(
						(String) objectMap.get("id"));
				// inactive GBOM Rel ID
				strRelId = (String) objectMap.get("id[connection]");
				sb = sb.append("&amp;relId=").append(strRelId);
				strRuleType = (String) objectMap.get("attribute["
						+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
				sb = sb.append("&amp;ruleExcludeOrInclude=").append(strRuleType);
				iRuleID = (String) objectMap.get("tomid["
						+ ProductLineConstants.RELATIONSHIP_LEFT_EXPRESSION
						+ "].from.id");
				sb = sb.append("&amp;iRuleId=").append(iRuleID);
				logicalFTRID = (String) objectMap
						.get(DomainRelationship.SELECT_FROM_ID);
				sb = sb.append("&amp;lfeatureId=").append(logicalFTRID);
				gbomName = XSSUtil.encodeForHTMLAttribute(context,(String) objectMap.get(DomainObject.SELECT_NAME));
				sb = sb.append("&amp;partName=").append(gbomName);
				gbomType = (String) objectMap.get(DomainObject.SELECT_TYPE);
				sb = sb.append("&amp;gbomType=").append(gbomType);
								
				String strRuleTypeDisplayKey = "emxFramework.Range.Rule_Type."+strRuleType;
				String strRuleTypeDisplay = EnoviaResourceBundle.getProperty(context,"Framework",strRuleTypeDisplayKey,strLanguage);
				
				sb = sb.append(strEndPart1).append(strRuleTypeDisplay).append(
						strEndPart2);

				if (strReportFormat != null
						&& strReportFormat.equals("null") == false
						&& strReportFormat.equals("") == false)
					vInclusionRuleLink.add(strRuleTypeDisplay);
				else
					vInclusionRuleLink.add(sb.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FrameworkException(e.getMessage());
		}
		return vInclusionRuleLink;

	}
	
	
	
	 /**
     * Method call to check whether the product is in release or obsolete state.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - HashMap containing one String entry for key "objectId"
     * @return Object - A Boolean object
     * @throws Exception if the operation fails
     * @since R212
     * @grade 0
     */
    public Object createNewLinkDisplayInTable(Context context, String[] args)throws Exception
    {
		// later to be converted to the primitive type.
		Boolean bDisplay = Boolean.valueOf(false);
		try {

			HashMap argumentMap = (HashMap) JPO.unpackArgs(args);
			String strProductObjectId = (String) argumentMap.get("objectId");
			boolean isFTRUser = ConfigurationUtil.isFTRUser(context);
			
			if (strProductObjectId != null) {
				strProductObjectId = strProductObjectId.trim();
				setId(strProductObjectId);
				String strType = getInfo(context, DomainConstants.SELECT_TYPE);

				String strFinalType = strType;

				// To check for the subtypes of Product
				BusinessType busType = new BusinessType(
						ConfigurationConstants.TYPE_PRODUCTS, context
								.getVault());
				List busTypeList = busType.getChildren(context);
				for (int iCount = 0; iCount < busTypeList.size(); iCount++) {
					if (strType.equals((busTypeList.get(iCount)).toString())) {
						strFinalType = ConfigurationConstants.TYPE_PRODUCTS;
						break;
					}
				}

				// To check for the subtypes of Feature
				busType = new BusinessType(
						ConfigurationConstants.TYPE_LOGICAL_STRUCTURES, context
								.getVault());
				busTypeList = busType.getChildren(context);
				for (int iCount = 0; iCount < busTypeList.size(); iCount++) {
					if (strType.equals((busTypeList.get(iCount)).toString())) {
						strFinalType = ConfigurationConstants.TYPE_LOGICAL_STRUCTURES;
						break;
					}
				}
				busType = new BusinessType(
						ConfigurationConstants.TYPE_CONFIGURATION_FEATURES,
						context.getVault());
				busTypeList = busType.getChildren(context);
				for (int iCount = 0; iCount < busTypeList.size(); iCount++) {
					if (strType.equals((busTypeList.get(iCount)).toString())) {
						strFinalType = ConfigurationConstants.TYPE_CONFIGURATION_FEATURES;
						break;
					}
				}
				// get the current state of the product object
				String strCurrentState = getInfo(context, CURRENT);
				// get the default policy of the type->products

				// forming the key which will be present in the
				// emxProduct.properties
				StringBuffer sKey = new StringBuffer(70);
				sKey = sKey.append(KEY_FIRST_PART).append(DOT).append(
						KEY_LAST_PART);
				String strKey = sKey.toString();
				// get the value corresponding to the key from the
				// emxProduct.properties file
				String strNotAllowedStates = EnoviaResourceBundle.getProperty(context, strKey);
				// check if the current state of the object is amongst the not
				// allowed states
				if ((strNotAllowedStates.indexOf(strCurrentState)) != -1) {
					// the current state is amongst the not allowed state. in
					// that case return false
					// this false will be used by the emxTable.jsp and the
					// create new link wont be displayed
					bDisplay = Boolean.valueOf(false);
				} else {
					bDisplay = Boolean.valueOf(true);
					
					if (strFinalType.equals(TYPE_PRODUCT)
							|| strFinalType.equals(TYPE_MODEL)
							|| strFinalType.equals(TYPE_PRODUCTLINE)) {
						// For Rules under Features - we have to check that only
						// the owner or assignees can create the rules.
						// So for a Productmanager or System Engineet to create
						// Rules they should be owner or assignee.
						// But when creating Rules under context of Product this
						// validation is not there.

						// use the other person from common package
						Person person = new Person(context.getUser());
						if ((person.isAssigned(context,
								ProductLineConstants.ROLE_SYSTEM_ENGINEER))
								|| (person
										.isAssigned(
												context,
												ProductLineConstants.ROLE_PRODUCT_MANAGER))) {
							bDisplay = Boolean.valueOf(true);
						}
					}

					if (bDisplay.booleanValue() == false
							&& (strFinalType
									.equals(ConfigurationConstants.TYPE_CONFIGURATION_FEATURES) || strFinalType
									.equals(ConfigurationConstants.TYPE_LOGICAL_STRUCTURES))) {
						bDisplay = Boolean.valueOf(true);
					} else if (strType
							.equals(ProductLineConstants.TYPE_FIXED_RESOURCE)) {
						bDisplay = Boolean.valueOf(true);
					}// end of if FIXED RESOURCE
					
					if(isFTRUser&&bDisplay.booleanValue() == true){
						bDisplay = true;
					}else{
						bDisplay = false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bDisplay;
	}
    
    /**
     * This method will be used as Access function in FTRAssignResourcesActionCommandLink command.when context is of either productline or model, it 
     * will return false else it will return true.
     * Method call to check whether the product is in release or obsolete state.
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public static boolean isResourceUsageAssignmentAllowed(Context context, String[] args)throws Exception
    {
		Boolean bDisplay = false;
		try {
			HashMap argumentMap = (HashMap) JPO.unpackArgs(args);
			String strProductObjectId = (String) argumentMap.get("objectId");
			if (ProductLineCommon.isNotNull(strProductObjectId)) {
			DomainObject domObj = DomainObject.newInstance(context, strProductObjectId);
			StringList selectables = new StringList();
			selectables.add(DomainConstants.SELECT_TYPE);
			selectables.add(DomainConstants.SELECT_CURRENT);
			Map objInfo = domObj.getInfo(context,selectables);
        	String strType = (String) objInfo.get(DomainConstants.SELECT_TYPE);
        	String strCurrentState = (String) objInfo.get(DomainConstants.SELECT_CURRENT);
        	
        	StringBuffer sKey = new StringBuffer(70);
			sKey = sKey.append(KEY_FIRST_PART).append(DOT).append(
					KEY_LAST_PART);
			String strKey = sKey.toString();
			String strNotAllowedStates = EnoviaResourceBundle.getProperty(context, strKey);

                if(!(mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_PRODUCT_LINE) ||
        			mxType.isOfParentType(context,strType,ConfigurationConstants.TYPE_MODEL))&&(strNotAllowedStates.indexOf(strCurrentState)) == -1){
                	bDisplay = true;
                }
			}							 
					
		} catch (Exception e) {
			throw new FrameworkException(e.getMessage());
		}
		return bDisplay;
	}

	/**
	 * This method is called by an action trigger on Revise of 'Product' to
	 * connect the new revision of the product to the Product Compatibility Rule
	 * if the compatibility option is "upward" or "all".
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            0 - contains the objectID of the context product
	 * @throws Exception
	 *             if operation fails
	 * @since ProductCentral 10-6- Refactored in R212
	 */
	public void connectProductOnRevise(Context context, String args[])
	throws Exception {
		// ObjectId of the context product
		String strProductId = args[0];

		Map mapRuleMap = new HashMap();
		String strCompOption = DomainConstants.EMPTY_STRING;
		String strCompOperatorValue = DomainConstants.EMPTY_STRING;
		// To get the latest revision of the context product
		DomainObject domObject = newInstance(context, strProductId);
		BusinessObject boNewRevision = domObject.getLastRevision(context);
		String newPrdPhyID = new DomainObject(boNewRevision).getInfo(context,
				ConfigurationConstants.SELECT_PHYSICAL_ID);

		// Forming the Relationship pattern
		StringBuffer sbBuffer = new StringBuffer(200);
		// Forming the Object Selects-------------------
		StringList lstObjectSelects = new StringList();
		lstObjectSelects.add(DomainConstants.SELECT_ID);
		lstObjectSelects.add(DomainConstants.SELECT_TYPE);
		lstObjectSelects.add(DomainConstants.SELECT_NAME);
		lstObjectSelects.add(DomainConstants.SELECT_REVISION);
		lstObjectSelects.add(DomainConstants.SELECT_POLICY);
		lstObjectSelects.add(DomainConstants.SELECT_VAULT);
		lstObjectSelects.add(DomainConstants.SELECT_OWNER);
		lstObjectSelects.add(DomainConstants.SELECT_DESCRIPTION);
		String STR_ATTRIBUTE = "attribute";
		String OPEN_BRACE = "[";
		String CLOSE_BRACE = "]";
		sbBuffer.delete(0, sbBuffer.length());
		sbBuffer = sbBuffer.append(STR_ATTRIBUTE).append(OPEN_BRACE).append(
				ProductLineConstants.ATTRIBUTE_LEFT_EXPRESSION).append(
						CLOSE_BRACE);
		String strLeftExpkey = sbBuffer.toString();
		lstObjectSelects.add(strLeftExpkey);

		sbBuffer.delete(0, sbBuffer.length());
		sbBuffer = sbBuffer.append(STR_ATTRIBUTE).append(OPEN_BRACE).append(
				ProductLineConstants.ATTRIBUTE_RIGHT_EXPRESSION).append(
						CLOSE_BRACE);
		String strRightExpkey = sbBuffer.toString();
		lstObjectSelects.add(strRightExpkey);

		sbBuffer.delete(0, sbBuffer.length());
		sbBuffer = sbBuffer.append(STR_ATTRIBUTE).append(OPEN_BRACE).append(
				ProductLineConstants.ATTRIBUTE_COMPATIBILITY_OPTION).append(
						CLOSE_BRACE);
		String strCompOptionkey = sbBuffer.toString();

		sbBuffer.delete(0, sbBuffer.length());
		sbBuffer = sbBuffer.append(STR_ATTRIBUTE).append(OPEN_BRACE).append(
				ProductLineConstants.ATTRIBUTE_COMPARISION_OPERATOR).append(
						CLOSE_BRACE);
		String strCompOperator = sbBuffer.toString();
		lstObjectSelects.add(strCompOptionkey);
		lstObjectSelects.add(strCompOperator);
		// END Forming the Object Selects-------------------
		// Forming the Rel Selects-------------------
		StringList lstRelationshipSelects = new StringList();
		lstRelationshipSelects.add(DomainRelationship.SELECT_ID);
		lstRelationshipSelects.add(DomainRelationship.SELECT_NAME);
		// END Forming the Rel Selects-------------------
		// Rel and Type Pattern
		String strTypePatten = ConfigurationConstants.TYPE_PRODUCT_COMPATIBILITY_RULE;
		String strRelPattern = ConfigurationConstants.RELATIONSHIP_RIGHT_EXPRESSION;

		ConfigurationUtil contextObj = new ConfigurationUtil(strProductId);
		MapList lstPCRuleList = contextObj.getObjectStructure(context,
				strTypePatten, strRelPattern, lstObjectSelects,
				lstRelationshipSelects, true, false, 1, 0,
				DomainObject.EMPTY_STRING, DomainObject.EMPTY_STRING,
				DomainObject.FILTER_ITEM, DomainObject.EMPTY_STRING);

		for (int i = 0; i < lstPCRuleList.size(); i++) {
			mapRuleMap = (Map) lstPCRuleList.get(i);
			strCompOption = (String) mapRuleMap.get(strCompOptionkey);
			strCompOperatorValue = (String) mapRuleMap.get(strCompOperator);

			// if the Compatibility Option is "Upward" or "All", only then the
			// new revision of the product is connected to the rule.
			if ((strCompOption.equals(ProductLineConstants.RANGE_VALUE_UPWARD))
					|| (strCompOption
							.equals(ProductLineConstants.RANGE_VALUE_ALL))) {
				String strRuleID = (String) mapRuleMap
				.get(DomainConstants.SELECT_ID);
				Product boBean = new Product(strProductId);
				Map relAttributeMap = new HashMap();
				Map objAttributeMap = new HashMap();
				objAttributeMap
				.put("Comparison Operator", strCompOperatorValue);
				objAttributeMap.put("Include Other Feature Revisions",
						ConfigurationConstants.RANGE_VALUE_YES);
				// Left expression should be unchanged
				String strLeftExpression = (String) mapRuleMap.get("attribute["
						+ ConfigurationConstants.ATTRIBUTE_LEFT_EXPRESSION
						+ "]");
				String strRightExpression = (String) mapRuleMap
				.get("attribute["
						+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION
						+ "]");
				StringBuffer sbNewRtExp = new StringBuffer(strRightExpression);
				sbNewRtExp.append(" OR ");
				sbNewRtExp.append("B"+newPrdPhyID);

				Map pcrExpMap = new HashMap();
				// unused
				pcrExpMap.put("leftExpObjectExp", "");
				pcrExpMap.put("rightExpObjectExp", "");
				// new left exp OID, same as of the existing
				pcrExpMap.put("leftExpObjectIds", strLeftExpression);
				// new right exp OID,
				pcrExpMap.put("rightExpObjectIds", sbNewRtExp.toString());

				pcrExpMap.put("OldLEId", strLeftExpression);
				pcrExpMap.put("OldREId", strRightExpression);
				// unused
				pcrExpMap.put("OldLEText", "");
				pcrExpMap.put("OldREText", "");
				pcrExpMap.put("RuleId", strRuleID);

				boBean.editProductCompatibilityRule(context,
						(String) mapRuleMap.get(DomainConstants.SELECT_ID),
						(String) mapRuleMap.get(DomainConstants.SELECT_TYPE),
						(String) mapRuleMap.get(DomainConstants.SELECT_NAME),
						(String) mapRuleMap
						.get(DomainConstants.SELECT_REVISION),
						(String) mapRuleMap.get(DomainConstants.SELECT_POLICY),
						(String) mapRuleMap.get(DomainConstants.SELECT_VAULT),
						(String) mapRuleMap.get(DomainConstants.SELECT_OWNER),
						(String) mapRuleMap
						.get(DomainConstants.SELECT_DESCRIPTION),
						pcrExpMap, objAttributeMap, relAttributeMap);
			}
		}// end of for
	}// end of method
	
	
	/**
	  * It is used in product version/copy to copy Rules to the newly created product
	  * @param context
	  * @param args 
	  * @throws FrameworkException
	  * @exclude
	  */
	public void copyRuleStructure(Context context, String[] args) throws FrameworkException
	{	
		String strComma = ",";
		
		try{
			ArrayList programMap = (ArrayList)JPO.unpackArgs(args);            
            String sourceObjectId = (String) programMap.get(0);
            String destinationObjectId = (String) programMap.get(1);
            HashMap relIDMapOnProduct = (HashMap) programMap.get(2);
            String strMode = (String) programMap.get(3);
           
            String strObjectId = "";
            String strObjectGeneratorName ="";
            String strAutoName ="";
            String strRelType = "";
            String strRelId = "";
            String attrLESelectable = "attribute["+ProductLineConstants.ATTRIBUTE_LEFT_EXPRESSION+"]";
            String attrRESelectable = "attribute["+ProductLineConstants.ATTRIBUTE_RIGHT_EXPRESSION+"]";
            StringBuffer attrRE = null;
            int index = 0;
            Map expMap = null;
            Map objAttributeMap = null;
            Map relAttributeMap = null;
            String oldRelID = null;
            String newRelID = null;
            String strDescription = null;
            String strCompatibilityType = null;
            String strMandatory = null;
            String strInherited = null;
            
            DomainObject destinationDO = newInstance(context, destinationObjectId);
            
            String strProductBCRReln = ProductLineConstants.RELATIONSHIP_BOOLEAN_COMPATIBILITY_RULE;
            String strProductRuleExtnReln = ProductLineConstants.RELATIONSHIP_PRODUCT_RULEEXTENSION;
            String strProductResourceReln = ProductLineConstants.RELATIONSHIP_PRODUCT_FIXEDRESOURCE;
            String strRuleMarketingPreferenceReln = ProductLineConstants.RELATIONSHIP_MARKETING_PREFERENCE;
                        
            String strBooleanCompatibilityRuleType = ProductLineConstants.TYPE_BOOLEAN_COMPATIBILITY_RULE;
            String strRuleExtensionType = ProductLineConstants.TYPE_RULE_EXTENSION;
            String strFixedResourceType = ProductLineConstants.TYPE_FIXED_RESOURCE;
            String strMarketingPrefernceRuleType = ProductLineConstants.TYPE_MARKETING_PREFERENCE;
                                   
            StringBuffer sb = new StringBuffer(250);
            String relationshipPattern = sb.append(strProductBCRReln).append(strComma)
                    					   .append(strProductRuleExtnReln).append(strComma)
                    					   .append(strProductResourceReln).append(strComma)
                    					   .append(strRuleMarketingPreferenceReln).toString();
            
            sb = new StringBuffer(250);
            String typePattern = sb.append(strBooleanCompatibilityRuleType).append(strComma)
   									.append(strRuleExtensionType).append(strComma)
   									.append(strFixedResourceType).append(strComma)
   									.append(strMarketingPrefernceRuleType).toString();
            
            StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
            objectSelects.addElement(DomainConstants.SELECT_TYPE);
            objectSelects.addElement(attrLESelectable);
            objectSelects.addElement(attrRESelectable);
            objectSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_RULE_CLASSIFICATION);
            objectSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_ERROR_MESSAGE);
            objectSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_DESCRIPTION);
            objectSelects.addElement(ConfigurationConstants.SELECT_ATTRIBUTE_COMPARISION_OPERATOR);
            objectSelects.addElement("to["+ConfigurationConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].businessobject.id");
                                    
            StringList relationshipSelectsForRule = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            relationshipSelectsForRule.addElement(DomainConstants.SELECT_RELATIONSHIP_TYPE);            
                     
            DomainObject dom = new DomainObject(sourceObjectId);            
            HashMap hmObjectId = new HashMap();
            
            MapList objFeatureList = dom.getRelatedObjects(context, relationshipPattern, typePattern,
           		 					false, true, (short) 1,objectSelects, relationshipSelectsForRule, 
   					                 DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, (short) 0,
   					                 null, null, null);
            
            MapList objFeatureListDestination = destinationDO.getRelatedObjects(context, relationshipPattern, typePattern,
	 					false, true, (short) 1,objectSelects, relationshipSelectsForRule, 
		                 DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING, (short) 0,
		                 null, null, null);
            
            for(int i=0; i<objFeatureListDestination.size(); i++) {
            	hmObjectId.put((String)((Map)objFeatureListDestination.get(i)).get(ConfigurationConstants.SELECT_ID), (String)((Map)objFeatureListDestination.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_TYPE));
            }
                     
            int iNumberOfObjects = objFeatureList.size();
            
            for (int i = 0; i < iNumberOfObjects; i++) {             
                strObjectId = (String) ((Hashtable) objFeatureList.get(i)).get(DomainConstants.SELECT_ID);
                if(hmObjectId.get(strObjectId) != null) {
                	continue;
                }
                else {
		                strDescription = (String) ((Hashtable) objFeatureList.get(i)).get(ConfigurationConstants.SELECT_ATTRIBUTE_DESCRIPTION);
		                strCompatibilityType = (String) ((Hashtable) objFeatureList.get(i)).get(ConfigurationConstants.SELECT_ATTRIBUTE_COMPARISION_OPERATOR);
		                attrRE = new StringBuffer((String) ((Hashtable) objFeatureList.get(i)).get(attrRESelectable));
		                strRelType = (String) ((Hashtable) objFeatureList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_TYPE);
		                strRelId = (String) ((Hashtable) objFeatureList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
		                       
		                relAttributeMap = new DomainRelationship(strRelId).getAttributeMap(context);
		                strMandatory=(String)relAttributeMap.get(ConfigurationConstants.ATTRIBUTE_MANDATORYRULE);
		                strInherited=(String)relAttributeMap.get(ConfigurationConstants.ATTRIBUTE_INHERITED);
		                if(strMandatory!=null && strMandatory.equalsIgnoreCase("Yes") && strInherited!=null && strInherited.equalsIgnoreCase("True") && !strMode.equals("copyProduct")){
		                  ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"), DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);
		                	StringList strListRel = new StringList(1);
		    				strListRel.add(DomainConstants.SELECT_ID);
		                	String relInheritedFrom = (String) PropertyUtil.getSchemaProperty(context,"relationship_InheritedFrom");
		                	String relId = "";
		                	DomainObject domRule = new DomainObject(strObjectId);
		                	DomainObject domObj = new DomainObject();
		                	domObj.setId(destinationObjectId);
		                	DomainRelationship domRelationship = DomainRelationship.connect(context,domObj, strRelType, domRule);
		                	
		                	domRelationship.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_INHERITED, "True");
		                    domRelationship.setAttributeValue(context,ConfigurationConstants.ATTRIBUTE_MANDATORYRULE, "Yes");
		
		                    Hashtable relData = (Hashtable)domRelationship.getRelationshipData(context,strListRel);
		
		                    StringList s2RelIds = (StringList) relData.get(DomainConstants.SELECT_ID);
							relId = (String) s2RelIds.get(0);
							 
							String strMqlCmd2 = "add connection $1 fromrel $2 torel $3 ";
							MqlUtil.mqlCommand(context, strMqlCmd2, true,relInheritedFrom,strRelId,relId);
							ContextUtil.popContext(context);
		                }
		                else{           	
		                	DomainRelationship strRel = DomainRelationship.connect(context, destinationDO, strRelType, new DomainObject(strObjectId));
							//IR: IR-151491V6R2013
		                	relAttributeMap.put(ConfigurationConstants.ATTRIBUTE_MANDATORYRULE, "No");
		                	relAttributeMap.put("Inherited", "False");
		                	strRel.setAttributeValues(context, relAttributeMap);                 
		                   }                                          
                    }
            }
                        
            //Start coding for IR, GBOM Rules            
            //retrieving IR connected to each relationship with LE and keeping the IR id and Left Expression/Right Expression ID for future use
            StringList relSelectable = new StringList();
                       
            relSelectable.add("tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type");
            relSelectable.add("tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.id");            
            relSelectable.add("tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.attribute["+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]");
            relSelectable.add("attribute["+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
          	           	 
            Iterator itr = relIDMapOnProduct.keySet().iterator();
            while(itr.hasNext()){               
            	oldRelID = (String)itr.next();
            	newRelID = relIDMapOnProduct.get(oldRelID).toString().split("\\|")[2];
            	DomainRelationship domRel = new DomainRelationship(oldRelID);
              	Map relDataTable = domRel.getRelationshipData(context, relSelectable);
              	
              	StringList ruleType = (StringList)relDataTable.get("tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.type");
              	StringList slAttrRE = (StringList)relDataTable.get("tomid["+ConfigurationConstants.RELATIONSHIP_LEFT_EXPRESSION+"].from.attribute["+ ConfigurationConstants.ATTRIBUTE_RIGHT_EXPRESSION + "]");
              	StringList slCompOperator = (StringList)relDataTable.get("attribute["+ ConfigurationConstants.ATTRIBUTE_RULE_TYPE + "]");
              	for(int j=0; j< ruleType.size(); j++){
              		if(ruleType.get(j).equals(ConfigurationConstants.TYPE_INCLUSION_RULE) && slAttrRE!=null && slAttrRE.size()!= 0){
    	      			strObjectGeneratorName = FrameworkUtil.getAliasForAdmin(context, DomainConstants.SELECT_TYPE, ruleType.get(0).toString(), true);
    	    			strAutoName = DomainObject.getAutoGeneratedName(context, strObjectGeneratorName, null);
    	    			attrRE = new StringBuffer((String)slAttrRE.get(0));
    	    			strCompatibilityType = (String)slCompOperator.get(0);
    	    			Iterator itrInner = relIDMapOnProduct.keySet().iterator();
                        while(itrInner.hasNext()){                	               	
                        	String oldRelIDInner = (String)itrInner.next();
                        	String oldRelPhysicalIDInner = relIDMapOnProduct.get(oldRelIDInner).toString().split("\\|")[0];
                        	String newRelPhysicalIDInner = relIDMapOnProduct.get(oldRelIDInner).toString().split("\\|")[1];
                        	
                        	index = attrRE.indexOf(oldRelPhysicalIDInner);
                        	if(index != -1){
                        		attrRE.replace(index, index+oldRelPhysicalIDInner.length(), newRelPhysicalIDInner);
                        	}
                        }
    	    			
    	      			expMap = new HashMap();
    	                expMap.put("leftExpObjectIds", newRelID);
    	                expMap.put("rightExpObjectIds", attrRE.toString());
    	                objAttributeMap = new HashMap();    
    	                objAttributeMap.put("Comparison Operator", strCompatibilityType);
    	                relAttributeMap = new HashMap();
    	                
    	                MapList policyList = com.matrixone.apps.domain.util.mxType.getPolicies(context,ruleType.get(0).toString(),false);
    	    			String strPolicy = (String)((HashMap)policyList.get(0)).get(DomainConstants.SELECT_NAME);			
    	    	    	Policy policyObject = new Policy(strPolicy);
    	    	    	String strRevision = policyObject.getFirstInSequence(context);	
    	    	    	//for vault
    	    	    	String strVault = context.getVault().toString();
    	    	    	//for Owner
    	    	    	String strOwner = context.getUser();
    	    	    	strDescription = ""; //TODO
    	    	    	
    	                InclusionRule boCompRule = new InclusionRule();
    					boCompRule.createAndConnectComplexInclusionRule(context, ruleType.get(0).toString(), strAutoName, strRevision,strPolicy, strVault, strOwner, strDescription, expMap, objAttributeMap, relAttributeMap);   					
    	      		 }
              	}	      		               	 
            }            
            //End Coding for IR, GBOM Rules
		}
		catch(Exception e){
			throw new FrameworkException(e);
		}	
		
	}
	
	 public Vector showInclusionRuleLinkInRuleTypeColumnForProductContext(Context context, String[] args)
     throws Exception

	{
		HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
		List lstobjectList = (MapList) parametersMap.get("objectList");
		Vector vInclusionRuleLink = showInclusionRuleLinkForProductContext(
				context, args);

		Map objectMap = null;

		Vector vtIncRuleLink = new Vector();

		String strRelType = DomainConstants.EMPTY_STRING;

		for (int j = 0; j < lstobjectList.size(); j++) {
			objectMap = (Map) lstobjectList.get(j);

			strRelType = (String) objectMap.get(DomainRelationship.SELECT_ID);
			if (strRelType != null && !strRelType.trim().isEmpty() && !vInclusionRuleLink.isEmpty()) {	
				vtIncRuleLink.add(vInclusionRuleLink.get(j));
			} else {
				vtIncRuleLink.add("");

			}
		}
		return vtIncRuleLink;

	}
	public Vector showInclusionRuleLinkForProductContext(Context context, String[] args)
 throws Exception {

		String strObjectId;
		String strRelId;
		String strLevel;
		Vector vInclusionRuleLink = new Vector();
		HashMap parametersMap = (HashMap) JPO.unpackArgs(args);
		Map paramMap = (Map) parametersMap.get("paramList");

		String strReportFormat = (String) paramMap.get("reportFormat");
		String strParentOID = (String) paramMap.get("objectId");
		
		List lstobjectList = (MapList) parametersMap.get("objectList");

		StringBuffer sb;
		String strLanguage = context.getSession().getLanguage();
		String strRuleTypeDisplay = DomainConstants.EMPTY_STRING;
		String strExpression = DomainConstants.EMPTY_STRING;
		String strRuleType = DomainConstants.EMPTY_STRING;
		Map objectMap;
		boolean checkType = false;
		boolean isFTRUser = ConfigurationUtil.isFTRUser(context);
		String strtempRepFormat = strReportFormat;
		// i18 conversion taken out from loop
		String strRuleTypeDisplayInclusion = EnoviaResourceBundle.getProperty(context,"Framework", "emxFramework.Range.Rule_Type.Inclusion",strLanguage);
		String strRuleTypeDisplayExclusion = EnoviaResourceBundle.getProperty(context,"Framework", "emxFramework.Range.Rule_Type.Exclusion",strLanguage);
		for (int i = 0; i < lstobjectList.size(); i++) {
			objectMap = (Map) lstobjectList.get(i);
			strRelId = (String) objectMap.get("id[connection]");	
			strLevel = (String)objectMap.get("id[level]");
			String strIncRuleID = "";
			strIncRuleID = (String) objectMap.get("tomid[Left Expression].from[Inclusion Rule].id");
			String isRootNode = (String) objectMap.get("Root Node");
			StringBuffer sIncLink = new StringBuffer(260);
			String strEndPart1 = "";
			String strMidPart1 = "";
			String strMidPart2 = "";
			String strEndPart2 = "";

			//if (strRelId != null && !("".equals(strRelId)) && !("null".equals(strRelId))) {
			
			if (strIncRuleID==null || (strIncRuleID!=null && strIncRuleID.trim().isEmpty()))  {
				sIncLink = sIncLink
				.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/CreateRuleDialog.jsp?rowLevel="+strLevel+"%26modetype=create%26commandName=FTRInclusionRuleSettings%26ruleType=InclusionRule%26SuiteDirectory=configuration");
		sIncLink.append("%26submitURL=");
		sIncLink
				.append("../configuration/InclusionRuleEditPostProcess.jsp?mode=create%26ruleType=InclusionRule");

		strEndPart1 = "', '800', '700', 'true', 'popup', '','','false','Large')\"";
		strMidPart1 = " TITLE=";
		strMidPart2 = ">";
		strEndPart2 = "</a>";
				
			} else {
				sIncLink = sIncLink
				.append("<a href=\"JavaScript:emxTableColumnLinkClick('../configuration/CreateRuleDialog.jsp?rowLevel="+strLevel+"%26modetype=edit%26commandName=FTRInclusionRuleSettings%26ruleType=InclusionRule%26SuiteDirectory=configuration");
		sIncLink.append("%26submitURL=");
		sIncLink
				.append("../configuration/InclusionRuleEditPostProcess.jsp?mode=edit%26ruleType=InclusionRule");

		strEndPart1 = "', '800', '700', 'true', 'popup', '','','false','Large')\"";
		strMidPart1 = " TITLE=";
		strMidPart2 = ">";
		strEndPart2 = "</a>";


			}

			sIncLink = sIncLink.append("&amp;parentOID=").append(XSSUtil.encodeForHTMLAttribute(context,strParentOID))
					.append("&amp;objectId=");

			String typeFeature = (String) objectMap.get("type");
			strObjectId = (String) objectMap.get("id");
			if(typeFeature==null){
				DomainObject domObj = DomainObject.newInstance(context, strObjectId);				      
				typeFeature= (String)domObj.getInfo(context,DomainConstants.SELECT_TYPE);
			}
			if (typeFeature!=null)
			 {
				checkType = true;
			}

			if (strRelId != null && !("".equals(strRelId))
					&& !("null".equals(strRelId))) {
			//	strRuleType = (String) objectMap.get("attribute[Rule Type]");
				if (objectMap
						.containsKey("attribute[" + ConfigurationConstants.SELECT_ATTRIBUTE_RULE_TYPE +"]")
						&& ProductLineCommon
								.isNotNull((String) objectMap
										.get("attribute[" + ConfigurationConstants.SELECT_ATTRIBUTE_RULE_TYPE +"]"))){
					strRuleType =  (String)objectMap.get("attribute[" + ConfigurationConstants.SELECT_ATTRIBUTE_RULE_TYPE +"]");
				}else if (objectMap
						.containsKey(DomainRelationship.SELECT_ID)
						&& ProductLineCommon
								.isNotNull((String) objectMap
										.get(DomainRelationship.SELECT_ID))){
					DomainRelationship domRel = new DomainRelationship((String) objectMap
							.get(DomainRelationship.SELECT_ID));
					strRuleType=domRel.getAttributeValue(context, ConfigurationConstants.ATTRIBUTE_RULE_TYPE);
				}else{
					strRuleType = ConfigurationConstants.RANGE_VALUE_INCLUSION;
				}
			} else {
				strReportFormat = strtempRepFormat;
				strRuleType = ConfigurationConstants.RANGE_VALUE_INCLUSION;
			}

			sb = new StringBuffer();
			sb = sb.append(sIncLink.toString());
			sb = sb.append(XSSUtil.encodeForHTMLAttribute(context,strObjectId)).append("&amp;relId=").append(XSSUtil.encodeForHTMLAttribute(context,strRelId))
					.append("&amp;ruleId=").append(XSSUtil.encodeForHTMLAttribute(context,strIncRuleID));
			if (strRuleType
					.equalsIgnoreCase(ConfigurationConstants.RANGE_VALUE_INCLUSION)) {
				strRuleTypeDisplay = strRuleTypeDisplayInclusion;
			} else {
				strRuleTypeDisplay = strRuleTypeDisplayExclusion;
			}
			String strRuleTypeToolTip = strRuleTypeDisplay.toString();
			sb = sb.append(strEndPart1).append(strMidPart1).append(
					"\"" + XSSUtil.encodeForHTMLAttribute(context,strRuleTypeToolTip) + "\"").append(
					strExpression.trim()).append(strMidPart2).append(
							XSSUtil.encodeForHTML(context,strRuleTypeDisplay)).append(strEndPart2);
			
			if ((strReportFormat != null
					&& strReportFormat.equals("null") == false
					&& strReportFormat.equals("") == false
					&& strReportFormat.length() > 0) || !isFTRUser) {
				vInclusionRuleLink.add(strRuleTypeToolTip);
			} else {
				if (checkType) {
					vInclusionRuleLink.add(sb.toString());
				} else {
					vInclusionRuleLink.add("");
				}

			}
		//}
		}
		return vInclusionRuleLink;
}
}
