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
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.engineering.TBEUtil;

/**
 *
 * TBE Utility methods
 *
 */
public class emxTeamUtilsBase_mxJPO {

	/**
	 * Constructor
	 */
	public emxTeamUtilsBase_mxJPO() {
		// Constructor
	}

	/** To return the objectIds of the objects to be excluded from Add Affected Item search page.
	 * @param context
	 * @param args
	 * @return StringList
	 * @throws Exception
	 */
	@com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
	public StringList excludeAffectedItems(Context context, String args[])
			throws Exception {
		emxENCFullSearch_mxJPO fs = new emxENCFullSearch_mxJPO(context, args);
		StringList affectedParts = fs.excludeAffectedItems(context, args);
		String typePattern = PropertyUtil.getSchemaProperty(context,
				"type_PLMCoreReference");
		String whereExpression = "to["
				+ DomainConstants.RELATIONSHIP_PART_SPECIFICATION
				+ "].from.policy.property[PolicyClassification]==Development";
		StringList objectSelects = new StringList(1);
		objectSelects.addElement(DomainConstants.SELECT_ID);

		MapList excludeProducts = DomainObject.findObjects(context,
				typePattern, context.getVault().getName(), whereExpression,
				objectSelects);

		Iterator i1 = excludeProducts.iterator();
		StringList excludeList = new StringList();

		while (i1.hasNext()) {
			Map m1 = (Map) i1.next();
			String strId = (String) m1.get(DomainConstants.SELECT_ID);
			excludeList.addElement(strId);
		}

		excludeList.addAll(affectedParts);

		return excludeList;
	}

	/** To get the users from the current project context
	 * @param context
	 * @param args
	 * @return StringList
	 * @throws Exception
	 */

	public StringList getContextProjectUsers(Context context, String args[])
			throws Exception {
		return TBEUtil.getContextProjectUsers(context);
	}

    /**
     * Method to check for revise access on development part
     * In TBE, once one of the development part revision is released,
     * we will not allow further development part revisions on the same
     * @param context
     * @param args
     * @return Boolean
     * @throws Exception
     */
    public Boolean hasReviseAccess(Context context,String[] args) throws Exception {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String objectId  = (String) paramMap.get("objectId");

        StringList revStateList = FrameworkUtil.split(
                                                      MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",objectId,
                        "from["+ DomainConstants.RELATIONSHIP_PART_SPECIFICATION + "|to.type.kindof["+EngineeringConstants.TYPE_VPLM_CORE_REF+"]]" +
                                " policy current.access[revise] revisions.current"),
                        ",");

        if("true".equalsIgnoreCase((String)revStateList.get(0))) {

            if(DomainConstants.POLICY_DEVELOPMENT_PART.equalsIgnoreCase((String)revStateList.get(1))
                    && "TRUE".equalsIgnoreCase((String)revStateList.get(2))) {
                return Boolean.valueOf(revStateList.contains(DomainConstants.STATE_DEVELOPMENT_PART_COMPLETE)? false : true);
            }
        }
        return Boolean.valueOf((String)revStateList.get(2));
    }

    /**
     * Generates dynamic query for Reviewer List field in Team ECO create/edit pages
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    public String getDynamicReviewrListQuery(Context context, String[] args) throws Exception {
        return "TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Review:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE"
                + EngineeringUtil.getAltOwnerFilterString(context);
    }

    /**
     * Generates dynamic query for Approval List field in Team ECO create/edit pages
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    public String getDynamicApprovalListQuery(Context context, String[] args) throws Exception {
        return "TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Approval:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE"
                + EngineeringUtil.getAltOwnerFilterString(context);
    }

    /**
     * Generates dynamic query for Distribution List field in Team ECO create/edit pages
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    public String getDynamicDistributionListQuery(Context context, String[] args) throws Exception {
        return "TYPES=type_MemberList:CURRENT=policy_MemberList.state_Active"
                + EngineeringUtil.getAltOwnerFilterString(context);
    }

    /**
     * Generates dynamic query for Reported Against field in Team ECO create/edit pages
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */
    public String getDynamicReportedAgainstQuery(Context context, String[] args) throws Exception {
        return "TYPES=type_Part,type_Builds,type_CADDrawing,type_CADModel,type_DrawingPrint,type_PartSpecification,type_Products"
                + EngineeringUtil.getAltOwnerFilterString(context);
    }


    /**
     * Blocks the creation of Part if the user context is Private/Protected Project.
     *  This is just a temperory fix to handle only Public access
     * @param context
     * @param args
     * @return String
     * @throws Exception
     */


    public int allowPartCreateForPublicProj(Context context,String[] args)
    throws Exception {

    	String ctxRole 		= context.getRole().trim();
    	String eventType 	= args[0];

    	if (ctxRole.length() > 1)
    	{
    		String ctxProject = ctxRole.substring(ctxRole.lastIndexOf('.')+1);
    		String strMessage = "";
    		String strProjectType = MqlUtil.mqlCommand(context,"print role $1 select $2 dump",
                                                           ctxProject,"property[vplm_readAbility].value");

    		if (null !=strProjectType && !strProjectType.equals("") && !strProjectType.equals("Public")){

    			if ("Create".equals(eventType))
    			{
    				strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.PartCreatePrivProtProject.Message",context.getSession().getLanguage());
    			} else if ("Revise".equals(eventType))
    			{
    				strMessage = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.PartRevisePrivProtProject.Message",context.getSession().getLanguage());
    			}

    			emxContextUtil_mxJPO.mqlNotice(context, strMessage);
    			return 1;
    		}
    	 }
   	 return 0;
    }

}
