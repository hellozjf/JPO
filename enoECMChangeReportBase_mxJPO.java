/*
 * ${CLASSNAME}
 *
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 *
 *
 */


import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;
import com.matrixone.apps.domain.util.mxFtp;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeAction;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrder;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrderUI;
import com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.MyOutputStream;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.framework.ui.RenderPDF;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import com.matrixone.apps.framework.ui.UIUtil;

/**
 * The <code>enoECMChangeReportBase</code> class contains methods for executing JPO operations related
 * to Change Reports.
 * @version Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 */
/**
 * @author otf
 *
 */
public class enoECMChangeReportBase_mxJPO extends emxDomainObject_mxJPO {


	/********************************* MAP SELECTABLES/*********************************
    /** A string constant with the value "field_Choices". */
	private static String FIELD_CHOICES = "field_choices";
	/** A string constant with the value "field_display_choices". */
	private static String FIELD_DISPLAY_CHOICES = "field_display_choices";
	private static final String INFO_TYPE_ACTIVATED_TASK  = "activatedTask";
	public static final String SUITE_KEY = "EnterpriseChangeMgt";

	private ChangeUtil changeUtil       =  null;
	private ChangeOrderUI changeOrderUI =  null;
	private ChangeOrder changeOrder     =  null;



	/**
	 * Default Constructor.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds no arguments
	 * @throws        Exception if the operation fails
	 * @since         Ecm R211
	 **
	 */
	public enoECMChangeReportBase_mxJPO (Context context, String[] args) throws Exception {

		super(context, args);
		changeUtil    = new ChangeUtil();
		changeOrderUI = new ChangeOrderUI();
		changeOrder   = new ChangeOrder ();
	}

	/**
	 * Main entry point.
	 *
	 * @param context the eMatrix <code>Context</code> object
	 * @param args    holds no arguments
	 * @return        an integer status code (0 = success)
	 * @throws        Exception when problems occurred in the Common Components
	 * @since         Common X3
	 **
	 */
	public int mxMain (Context context, String[] args) throws Exception {
		if (!context.isConnected()) {
			i18nNow i18nnow = new i18nNow();
			String strContentLabel = EnoviaResourceBundle.getProperty(context,
					ChangeConstants.RESOURCE_BUNDLE_ENTERPRISE_STR, context.getLocale(), "EnterpriseChangeMgt.Error.UnsupportedClient");
			throw  new Exception(strContentLabel);
		}
		return  0;
	}


/**
 * Creates the ECO summary report.
 *
 * @param context the eMatrix <code>Context</code> object.
 * @param args holds the following input arguments:
 * 0 - String containing object id.
 * @return int tells whether PDF is generated and checked into the object. 0-success 1-failure.
 * @throws Exception if the operation fails.
 * @since Enginnering central X3
 */
 public  int generatePDFSummaryReport(Context context,String args[]) throws Exception {

if (args == null || args.length < 1) {
        throw (new IllegalArgumentException());
  }

 String summaryReport = "";
 String objectId = args[0];
 DomainObject doObj = new DomainObject(objectId);
try
 {
  if (doObj.isKindOf(context, ChangeConstants.TYPE_CHANGE_ORDER) || doObj.isKindOf(context, ChangeConstants.TYPE_CHANGE_REQUEST))
     summaryReport=generateCOHtmlSummaryReport(context,args);
  else if (doObj.isKindOf(context, ChangeConstants.TYPE_CHANGE_ACTION))
	  summaryReport=generateCAHtmlSummaryReport(context,args);
}
catch (Exception e)
{
  throw e;
}
		String strGeneratePDF = FrameworkProperties.getProperty(context, "EnterpriseChangeMgt.Change.ViewPdfSummary");

		if ("true".equalsIgnoreCase(strGeneratePDF))
		{
		  int pdfGenerated = renderPDFFileForChange(context,args,summaryReport);
		  if(pdfGenerated!=0)
		  {
			   emxContextUtil_mxJPO.mqlError(context,EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.SummaryReport.NoCheckIn.ErrorMessage"));
				  return 0;
		  }
		  else
		  {
			  return 0;
		  }
		}
		else
		{
			return 0;
		}

 }

 public String generateCOHtmlSummaryReport(Context context,String args[]) throws Exception {

	  if (args == null || args.length < 1) {
	            throw (new IllegalArgumentException());
	      }

	    String objectId = args[0];
	    StringBuffer summaryReport = new StringBuffer();
	    ChangeOrder ecoObj = null;
	    try
	    {
	       String strLanguage = context.getSession().getLanguage();
	       ecoObj = new ChangeOrder(objectId);
	       // Date in suitable format.
	       java.util.Calendar cal = new GregorianCalendar(TimeZone.getDefault());
	       int month = cal.get(Calendar.MONTH);
	       int dates = cal.get(Calendar.DATE);
	       int year =  cal.get(Calendar.YEAR);
	       int hour =  cal.get(Calendar.HOUR);
	       int minute = cal.get(Calendar.MINUTE);
	       int AM_PM = cal.get(Calendar.AM_PM);
	       String[] monthDesc = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	       String[] AMPM = new String[]{"AM","PM"};
	       String smonth = monthDesc[month];
	       String sAMPM = AMPM[AM_PM];
	       String dateAndTime =  smonth+" "+dates+","+year+","+hour+":"+minute+" "+sAMPM;
	       //Summary Report Heading
	       summaryReport.append("<html>");
	       summaryReport.append("<div id=\"pageHeader\">");
	       summaryReport.append("<table border=\"0\" width=\"100%\">");
	       summaryReport.append("<tr><td class=\"pageHeader\"><h1>"+ecoObj.getInfo(context,SELECT_NAME)+":&nbsp;"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.SummaryReportHeader")+"</h1></td>");
	       summaryReport.append("<td class=\"pageSubtitle\">"+ EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Generated")+" "+dateAndTime+"</td></tr>");
		   summaryReport.append("</table>");
	       summaryReport.append("</div>");

	       // Basic Attributes section display
	       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Attributes")+"</h2></td></tr></table>");

	       summaryReport.append(getCOBasicInfo(context,args));

	       // Approvals Display

	        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Approvals")+"</h2></td></tr></table>");

	        summaryReport.append(getChangeApprovals(context,args));


	        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Routes")+"</h2></td></tr></table>");
	        summaryReport.append(getChangeRoutes(context,args));


	        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Tasks")+"</h2></td></tr></table>");
	        summaryReport.append(getChangeTasks(context,args));


	       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Command.ProposedChanges")+"</h2></td></tr></table>");
	       summaryReport.append(getCOAffectedItemsSummaryDetails(context,args));


	       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Command.CandidateItem")+"</h2></td></tr></table>");
	       summaryReport.append(getCOCandidateItemsSummaryDetails(context,args));



	       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Command.ReferenceDocuments")+"</h2></td></tr></table>");
		   summaryReport.append(getChangeRelatedReferenceDocuments(context,args));
		   
		   if(isKindOf(context, ChangeConstants.TYPE_CHANGE_REQUEST)){
			   summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Command.ChangeOrders")+"</h2></td></tr></table>");
			   summaryReport.append(getChangeOrders(context,args));

		   }


	      summaryReport.append("</html>");
	    }
	    catch (Exception e)
	    {
	      throw e;
	    }
	    return summaryReport.toString();
	   }



public String getChangeRelatedReferenceDocuments(Context context,String[] args)
      throws Exception
{
    String strLanguage = context.getSession().getLanguage();
    String objectId = args[0];
  MapList mpListReferenceDocs = new MapList();
  StringBuffer referenceDocs = new StringBuffer();
    try
    {
        ChangeOrder ecoObj = new ChangeOrder(objectId);
        StringList selectStmts = new StringList(1);
        selectStmts.addElement(SELECT_ID);
    selectStmts.addElement(SELECT_TYPE);
    selectStmts.addElement(SELECT_NAME);
    selectStmts.addElement(SELECT_REVISION);
    selectStmts.addElement(SELECT_DESCRIPTION);
    selectStmts.addElement(SELECT_CURRENT);
    selectStmts.addElement(SELECT_POLICY);

    String relationshipName = DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT+","+ChangeConstants.RELATIONSHIP_RELATED_ITEM;
    StringList selectRelStmts = new StringList(1);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
        mpListReferenceDocs = ecoObj.getRelatedObjects(context,
        		relationshipName,
           "*",            // object pattern
           selectStmts,    // object selects
           selectRelStmts, // relationship selects
           false,          // to direction
           true,      // from direction
           (short) 1,      // recursion level
           "",             // object where clause
           "",             // rel where clause
           0);         

        mpListReferenceDocs.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
        mpListReferenceDocs.sort();
    Iterator objItr = mpListReferenceDocs.iterator();
        Map ecrMap  = null;
        referenceDocs.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
        referenceDocs.append("<tr>");
        referenceDocs.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Name")+"</th>");
        referenceDocs.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.REVISION")+"</th>");
        referenceDocs.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.TYPE")+"</th>");
        referenceDocs.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Description")+"</th>");
        referenceDocs.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.State")+"</th>");
        referenceDocs.append("</tr>");

        String imgRefType = "";
    while (objItr.hasNext()) {
           ecrMap = (Map)objItr.next();

           imgRefType = UINavigatorUtil.getTypeIconProperty(context,(String)ecrMap.get(SELECT_TYPE));
           if (imgRefType == null || imgRefType.length() == 0 )
           {
               imgRefType = "iconSmallDefault.gif";
           }

           referenceDocs.append("<tr>");
           referenceDocs.append("<td><img src=\"../common/images/"+imgRefType+"\" border=\"0\" alt=\"*\">&nbsp;"+XSSUtil.encodeForHTML(context,(String)ecrMap.get(SELECT_NAME))+"&nbsp;</td>");
           //Modified for IR-184707V6R2013x end
           referenceDocs.append("<td>"+ecrMap.get(SELECT_REVISION)+"&nbsp;</td>");
           referenceDocs.append("<td>"+i18nNow.getAdminI18NString("Type",(String)ecrMap.get(DomainConstants.SELECT_TYPE), strLanguage)+"&nbsp;</td>");
           referenceDocs.append("<td>"+XSSUtil.encodeForHTML(context,(String)ecrMap.get(SELECT_DESCRIPTION))+"&nbsp;</td>");
           referenceDocs.append("<td>"+i18nNow.getStateI18NString((String)ecrMap.get(SELECT_POLICY),(String)ecrMap.get(SELECT_CURRENT),strLanguage)+"&nbsp;</td>");
           referenceDocs.append("</tr>");
    }
    if(mpListReferenceDocs.size()==0) {
        referenceDocs.append("<tr><td colspan=\"5\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Change.NoReferenceDocsConnected")+"</td></tr>");
    }
    }
    catch (FrameworkException Ex)
    {
         throw Ex;
    }
    referenceDocs.append("</table>");
    return referenceDocs.toString();
}


public String getChangeOrders(Context context, String[] args)throws Exception
{
	String strLanguage = context.getSession().getLanguage();
	String strChangeRequestId = args[0];
	MapList mpListChangeOrders = new MapList();
	StringBuffer sbChangeOrders = new StringBuffer();
	ChangeUtil changeUtil = new ChangeUtil();
	try {
		String strChangeActionSelect = "from["+ChangeConstants.RELATIONSHIP_CHANGE_ACTION+"].to.name"; 
		String strChangeCoordinatorSelect = "from["+ChangeConstants.RELATIONSHIP_CHANGE_COORDINATOR+"].to.name";
		StringList slBusSelects=new StringList(DomainObject.SELECT_ID);
		slBusSelects.add(DomainObject.SELECT_TYPE);
		slBusSelects.add(DomainObject.SELECT_NAME);
		slBusSelects.add(DomainObject.SELECT_DESCRIPTION);
		slBusSelects.add(DomainObject.SELECT_OWNER);
		slBusSelects.add(DomainObject.SELECT_CURRENT);
		slBusSelects.add(DomainObject.SELECT_REVISION);
		slBusSelects.add(strChangeActionSelect);
		slBusSelects.add(strChangeCoordinatorSelect);

		StringList slRelSelects = new StringList(SELECT_RELATIONSHIP_ID); 
		com.dassault_systemes.enovia.enterprisechange.modeler.ChangeRequest objChangeRequest=new com.dassault_systemes.enovia.enterprisechange.modeler.ChangeRequest(strChangeRequestId);
		mpListChangeOrders =  objChangeRequest.getChangeOrders(context, slBusSelects, slRelSelects, EMPTY_STRING, EMPTY_STRING, false);
		mpListChangeOrders.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
		mpListChangeOrders.sort();
		Iterator objItr = mpListChangeOrders.iterator();
		Map ecrMap  = null;
		sbChangeOrders.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
		sbChangeOrders.append("<tr>");
		sbChangeOrders.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Name")+"</th>");
		sbChangeOrders.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.CAs")+"</th>");
		sbChangeOrders.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.OWNER")+"</th>");
		sbChangeOrders.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.ColumnHeader.ChangeCoordinator")+"</th>");
		sbChangeOrders.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.State")+"</th>");
		sbChangeOrders.append("</tr>");

		String imgRefType = "";
		while (objItr.hasNext()) {
			ecrMap = (Map)objItr.next();
			String strChangeCoordinator = (String)ecrMap.get(strChangeCoordinatorSelect);
			strChangeCoordinator = ChangeUtil.isNullOrEmpty(strChangeCoordinator)?"":strChangeCoordinator;
			imgRefType = UINavigatorUtil.getTypeIconProperty(context,(String)ecrMap.get(SELECT_TYPE));
			if (imgRefType == null || imgRefType.length() == 0 )
			{
				imgRefType = "iconSmallDefault.gif";
			}

			sbChangeOrders.append("<tr>");
			sbChangeOrders.append("<td><img src=\"../common/images/"+imgRefType+"\" border=\"0\" alt=\"*\">&nbsp;"+XSSUtil.encodeForHTML(context,(String)ecrMap.get(SELECT_NAME))+"&nbsp;</td>");
			sbChangeOrders.append("<td>");

			Object tempObj = ecrMap.get(strChangeActionSelect);
			if(tempObj instanceof StringList) {
				StringList caList = (StringList) ecrMap.get(strChangeActionSelect);
				if(!changeUtil.isNullOrEmpty(caList)) {
					int listSize = caList.size();
					for(int index=0;index<listSize;index++) {
						String caName = (String)caList.get(index);
						sbChangeOrders.append(caName);
						if(!((listSize-1)==index)){
							sbChangeOrders.append("<br/>");
						}
					}

				}
			}else {
				String caName = (String) ecrMap.get(strChangeActionSelect);
				caName = ChangeUtil.isNullOrEmpty(caName)?"":caName;
				sbChangeOrders.append(caName);
			}

			sbChangeOrders.append("&nbsp;</td>");
			sbChangeOrders.append("<td>"+XSSUtil.encodeForHTML(context,(String)ecrMap.get(DomainConstants.SELECT_OWNER))+"&nbsp;</td>");
			sbChangeOrders.append("<td>"+XSSUtil.encodeForHTML(context,strChangeCoordinator)+"&nbsp;</td>");
			sbChangeOrders.append("<td>"+i18nNow.getStateI18NString((String)ecrMap.get(SELECT_POLICY),(String)ecrMap.get(SELECT_CURRENT),strLanguage)+"&nbsp;</td>");
			sbChangeOrders.append("</tr>");
		}
		if(mpListChangeOrders.size()==0) {
			sbChangeOrders.append("<tr><td colspan=\"5\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Change.NoChangeOrderConnected")+"</td></tr>");
		}
	}
	catch (FrameworkException Ex)
	{
		throw Ex;
	}
	sbChangeOrders.append("</table>");
	return sbChangeOrders.toString();
}

 public String getCOCandidateItemsSummaryDetails(Context context,String[] args)
 throws Exception
{
String strLanguage = context.getSession().getLanguage();
String objectId = args[0];
MapList mpAffectedPartList = new MapList();
StringBuffer newParts = new StringBuffer();
try
{
ChangeOrder ecoObj = new ChangeOrder(objectId);

SelectList sListSelStmts = ecoObj.getObjectSelectList(11);
sListSelStmts.addElement(SELECT_ID);
sListSelStmts.addElement(SELECT_TYPE);
sListSelStmts.addElement(SELECT_NAME);
sListSelStmts.addElement(SELECT_DESCRIPTION);
sListSelStmts.addElement(SELECT_REVISION);
sListSelStmts.addElement(SELECT_POLICY);
sListSelStmts.addElement(SELECT_CURRENT);
sListSelStmts.add("current.actual");

StringList selectRelStmts = new StringList(1);
selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
String strCandidateItem = PropertyUtil.getSchemaProperty(context,"relationship_CandidateAffectedItem");




mpAffectedPartList = getRelatedObjects(context,
	  strCandidateItem, // relationship pattern
                                    "*",      // object pattern
                                    sListSelStmts,// object selects
                                    selectRelStmts, // relationship selects
                                    false,                // to direction
                                    true,                // from direction
                                    (short) 1,           // recursion level
                                    EMPTY_STRING,        // object where clause
                                    EMPTY_STRING);       // relationship where clause

mpAffectedPartList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
mpAffectedPartList.sort();

Iterator objItr = mpAffectedPartList.iterator();
   Map partMap  = null;
newParts.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
newParts.append("<tr>");

newParts.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Name")+"</th>");
newParts.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Type")+"</th>");
newParts.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Description")+"</th>");
newParts.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.REVISION")+"</th>");
newParts.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.State")+"</th>");
newParts.append("</tr>");


while (objItr.hasNext()) {
      partMap = (Map)objItr.next();

String imgPartType = UINavigatorUtil.getTypeIconProperty(context, (String)partMap.get(SELECT_TYPE));

  if (imgPartType == null || imgPartType.length() == 0 )
  {
      imgPartType = "iconSmallDefault.gif";
  }


newParts.append("<tr>");

newParts.append("<td><img src=\"../common/images/"+imgPartType+"\" border=\"0\" alt=\"Part\">&nbsp;"+XSSUtil.encodeForHTML(context, (String)partMap.get(SELECT_NAME))+"</td>");

String pMST = (String)partMap.get(SELECT_TYPE);
pMST = FrameworkUtil.findAndReplace(pMST, " ", "_");

newParts.append("<td>"+EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Type."+pMST)+"</td>");

newParts.append("<td>"+XSSUtil.encodeForHTML(context, (String)partMap.get(SELECT_DESCRIPTION))+"</td>");

newParts.append("<td>"+partMap.get(SELECT_REVISION)+"</td>");

//newParts.append("<td>&nbsp;</td>");
newParts.append("<td>"+EnoviaResourceBundle.getStateI18NString(context,(String)partMap.get(SELECT_POLICY),(String)partMap.get(SELECT_CURRENT),strLanguage)+"</td>");

newParts.append("</tr>");
}
if(mpAffectedPartList.size()==0) {
  newParts.append("<tr><td colspan=\"12\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource",context.getLocale(), "EnterpriseChangeMgt.Change.NoCandidateItemsConnected")+"</td></tr>");

}

}
catch (FrameworkException Ex)
{
    throw Ex;
}
newParts.append("</table>");
return newParts.toString();
}

 public String getCOAffectedItemsSummaryDetails(Context context,String[] args) throws Exception {
	String objectId = args[0];
	MapList mpProposedChangeList = new MapList();
	StringBuffer newParts = new StringBuffer();
	String strLanguage = context.getSession().getLanguage();
	

	try {
		ChangeOrder ecoObj = new ChangeOrder(objectId);

		mpProposedChangeList = ecoObj.getProposedItems(context);
		mpProposedChangeList.addSortKey(ChangeConstants.RELATED_CA_NAME, "ascending", "String");
		mpProposedChangeList.sort();
		
		
		Iterator proposedChangeItr = mpProposedChangeList.iterator();
		
	
		Map partMap  = null;
		newParts.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
		newParts.append("<tr>");

		newParts.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.RelatedCA")+"</th>");
		newParts.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.NAME")+"</th>");
		newParts.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.TYPE")+"</th>");
		newParts.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.REVISION")+"</th>");
		newParts.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.State")+"</th>");
		newParts.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.RequestedChange")+"</th>");
		newParts.append("</tr>");

		while (proposedChangeItr.hasNext()) {
			partMap = (Map)proposedChangeItr.next();

			String imgPartType = UINavigatorUtil.getTypeIconProperty(context,(String)partMap.get(ChangeConstants.RELATED_CA_TYPE));

			if (UIUtil.isNotNullAndNotEmpty(imgPartType)) {
				imgPartType = "iconSmallDefault.gif";
			}

			String pMST = FrameworkUtil.findAndReplace((String)partMap.get(ChangeConstants.RELATED_CA_TYPE), " ", "_");
				newParts.append("<tr>");
				newParts.append("<td>" + partMap.get(ChangeConstants.RELATED_CA_NAME) +"</td>");
				newParts.append("<td>"+XSSUtil.encodeForHTML(context, (String)partMap.get(SELECT_NAME))+"</td>");
				newParts.append("<td>"+EnoviaResourceBundle.getTypeI18NString(context, (String)partMap.get(SELECT_TYPE), strLanguage)+"</td>");
				newParts.append("<td>"+(String)partMap.get(SELECT_REVISION)+"</td>");
				newParts.append("<td>"+EnoviaResourceBundle.getStateI18NString(context, (String)partMap.get(SELECT_POLICY), (String)partMap.get(SELECT_CURRENT), strLanguage)+"</td>");
				newParts.append("<td>"+EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Requested_Change."+((String) (String)partMap.get(DomainConstants.ATTRIBUTE_REQUESTED_CHANGE)).replaceAll(" ", "_"))+"</td>");
				newParts.append("</tr>");
			
		}
		
		if (mpProposedChangeList.size()==0) {
			newParts.append("<tr><td colspan=\"12\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(), "EnterpriseChangeMgt.Change.NoProposedChangesConnected")+"</td></tr>");
		}
	} catch (FrameworkException Ex) {
	    throw Ex;
	}

	newParts.append("</table>");
	return newParts.toString();
}


 /**
  * Gets the list of Routes in HTML table format.
  *
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds the following input arguments:
  * 0 - String containing object id.
  * @return String Html table format representation of Routes info.
  * @throws Exception if the operation fails.
  * @since EngieeringCentral X3
  */

 public String getChangeTasks(Context context, String[] args)
     throws Exception, MatrixException
 {
     try
     {
       String strLanguage = context.getSession().getLanguage();
       String objectId = args[0];
       Route routeObj = (Route)DomainObject.newInstance(context,TYPE_ROUTE);
       SelectList selectStmts = new SelectList();
       selectStmts.addName();
       selectStmts.addCurrentState();
       selectStmts.addId();
       selectStmts.addOwner();
       String strRouteType = "attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]";
       selectStmts.add(strRouteType);

   StringBuffer routeInfo = new StringBuffer();
   routeInfo.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
   routeInfo.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Summary.RouteName")+"</th>");
   routeInfo.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Summary.TaskName")+"</th>");
   routeInfo.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Owner")+"</th>");
   routeInfo.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Type")+"</th>");
   routeInfo.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Summary.TaskAssignee")+"</th>");
   routeInfo.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Summary.TaskAction")+"</th>");

   MapList totalResultList = routeObj.getRoutes(context, objectId, selectStmts, null, null, false);

   totalResultList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
   totalResultList.sort();

   Iterator itr = totalResultList.iterator();
   String routeId;

   String routeOwner = "";
   String routeType = "";

   String strRouteInstruction ="";

   String sTaskAssignee  ="";
   String sTitleName  ="";

	String strRelName = "from["+RELATIONSHIP_PROJECT_TASK+"].to.name";
	String strTitle = "attribute["+ATTRIBUTE_TITLE+"]";
	String strComments = "attribute["+ATTRIBUTE_COMMENTS+"]";
	String strAttrRouteInstruction = "attribute["+ATTRIBUTE_ROUTE_INSTRUCTIONS+"]";


   MapList taskList=new MapList();

   while(itr.hasNext()) {
     Map routeMap = (Map)itr.next();
     routeId = (String)routeMap.get(DomainConstants.SELECT_ID);
     routeOwner = (String)routeMap.get(DomainConstants.SELECT_OWNER);
     routeType = (String)routeMap.get(strRouteType);
     routeObj.setId(routeId);

           SelectList strTaskList = new SelectList();
           strTaskList.addName();
           strTaskList.addCurrentState();

           strTaskList.add(strRelName);
           strTaskList.add(strTitle);
           strTaskList.add(strComments);
           strTaskList.add(strAttrRouteInstruction);
           taskList = routeObj.getRouteTasks(context, strTaskList, null, null, false);

       // check for the status of the task.
       Map taskMap = null;
       if(taskList!=null){
       for(int j = 0; j < taskList.size(); j++) {
         taskMap = (Map) taskList.get(j);

         sTaskAssignee  = (String)taskMap.get(strRelName);
         sTitleName  = (String)taskMap.get(strTitle);
         strRouteInstruction = (String)taskMap.get(strAttrRouteInstruction);

         String sRouteType = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Route_Base_Purpose."+routeType);
         routeInfo.append("<tr>");
         routeInfo.append("<td>"+(String)routeMap.get(SELECT_NAME)+"&nbsp;</td>");
         routeInfo.append("<td>"+sTitleName+"&nbsp;</td>");
         routeInfo.append("<td>"+routeOwner+"&nbsp;</td>");
         routeInfo.append("<td>"+sRouteType+"&nbsp;</td>");
         routeInfo.append("<td>"+sTaskAssignee+"&nbsp;</td>");
         routeInfo.append("<td>"+strRouteInstruction+"&nbsp;</td>");
         routeInfo.append("</tr>");
       }
       }
   }

   if(taskList.size()==0) {
     routeInfo.append("<tr><td colspan=\"6\">"+ EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.TaskSummary.NoTasksFound")+""+ "&nbsp;</td></tr>");
   }
   routeInfo.append("</table>");
   return routeInfo.toString();
     }
     catch (Exception ex)
     {
       throw ex;
     }
 }
 /**
  * Gets the list of Routes in HTML table format.
  *
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds the following input arguments:
  * 0 - String containing object id.
  * @return String Html table format representation of Routes info.
  * @throws Exception if the operation fails.
  * @since Engineering Central X3
  */

 public String getChangeRoutes(Context context, String[] args)
     throws Exception, MatrixException
 {
     try
     {
       String strLanguage = context.getSession().getLanguage();
       String objectId = args[0];
   Route routeObj = (Route)DomainObject.newInstance(context,TYPE_ROUTE);
   String routeStatusAttrSel      = "attribute["+ DomainConstants.ATTRIBUTE_ROUTE_STATUS +"]";
   SelectList selectStmts = new SelectList();
   selectStmts.addName();
   selectStmts.addDescription();
   selectStmts.addCurrentState();
   selectStmts.add(routeStatusAttrSel);
   selectStmts.addOwner();
   selectStmts.addId();
   selectStmts.addPolicy();
   selectStmts.add(Route.SELECT_SCHEDULED_COMPLETION_DATE);
    selectStmts.add(Route.SELECT_ACTUAL_COMPLETION_DATE);


   StringBuffer routeInfo = new StringBuffer();
   routeInfo.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
   routeInfo.append("<tr><th width=\"5%\" style=\"text-align:center\"><img border=\"0\" src=\"../common/images/iconStatus.gif\" name=\"imgstatus\" id=\"imgstatus\" alt=\"*\"></th>");
   routeInfo.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Name")+"</th>");
   routeInfo.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Description")+"</th>");
   routeInfo.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Status")+"</th>");
   routeInfo.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Routes.ScheduleCompDate")+"</th>");
   routeInfo.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Owner")+"</th>");

   MapList totalResultList = routeObj.getRoutes(context, objectId, selectStmts, null, null, false);
   totalResultList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
   totalResultList.sort();

   Iterator itr = totalResultList.iterator();
   String routeId;
   String scheduledCompletionDate = "";
   boolean isYellow = false;
   String sCode = "";
   String routeIcon = "";
   Date curDate = new Date();
   String routeState = "";
   while(itr.hasNext()) {
     Map routeMap = (Map)itr.next();
     routeId = (String)routeMap.get(DomainConstants.SELECT_ID);
           routeState = (String)routeMap.get(DomainConstants.SELECT_CURRENT);
     routeObj.setId(routeId);
           scheduledCompletionDate = routeObj.getSheduledCompletionDate(context);
     if(scheduledCompletionDate != null && !"".equals(scheduledCompletionDate))
     {
       Date dueDate = new Date();
       dueDate = eMatrixDateFormat.getJavaDate(scheduledCompletionDate);
       if ( dueDate != null && ( curDate.after(dueDate)) && (!(routeState.equals("Complete")))) {
         sCode = "Red";
       }
     }

       isYellow = false;
       if (! sCode.equals("Red")) {
       MapList taskList = routeObj.getRouteTasks(context, selectStmts, null, null, false);

       // check for the status of the task.
       Map taskMap = null;
       for(int j = 0; j < taskList.size(); j++) {
         taskMap = (Map) taskList.get(j);
         String sState         = (String) taskMap.get(DomainConstants.SELECT_CURRENT);
         String CompletionDate = (String) taskMap.get(Route.SELECT_SCHEDULED_COMPLETION_DATE);
         String actualCompletionDate = (String) taskMap.get(Route.SELECT_ACTUAL_COMPLETION_DATE);

         Date dueDate = new Date();
         if( CompletionDate!=null && !"".equals(CompletionDate)) {
         dueDate = eMatrixDateFormat.getJavaDate(CompletionDate);
         }

         if (sState.equals("Complete")) {
         Date dActualCompletionDate = new Date(actualCompletionDate);
         if (dActualCompletionDate.after(dueDate)) {
           isYellow = true;
           break;
         }
         } else if (curDate.after(dueDate)) {
         isYellow = true;
         break;
         }
       }

       if(isYellow) {
         sCode = "yellow";
       } else {
         sCode = "green";
       }
           }

           if(sCode.equals("Red")) {
       routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusRed.gif\" name=\"red\" id=\"red\" alt=\"emxComponents.TaskSummary.ToolTipRed\">";
           } else if(sCode.equals("green")) {
               routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusGreen.gif\" name=\"green\" id=\"green\" alt=\"emxComponents.TaskSummary.ToolTipGreen\">";
           } else if(sCode.equals("yellow")) {
       routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusYellow.gif\" name=\"yellow\" id=\"yellow\" alt=\"emxComponents.TaskSummary.ToolTipYellow\">";
           } else {
               routeIcon = "&nbsp;";
     }


           String sStatusVal = (String)routeMap.get(routeStatusAttrSel);
           sStatusVal = FrameworkUtil.findAndReplace(sStatusVal," ", "_");

		   String  sStatus = (String)EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Route_Status."+sStatusVal);


     routeInfo.append("<tr>");
     routeInfo.append("<td>"+routeIcon+"</td>");
     routeInfo.append("<td>"+routeMap.get(SELECT_NAME)+"&nbsp;</td>");
     routeInfo.append("<td>"+XSSUtil.encodeForHTML(context, (String)routeMap.get(SELECT_DESCRIPTION))+"&nbsp;</td>");
     routeInfo.append("<td>"+sStatus+"&nbsp;</td>");
     routeInfo.append("<td>"+scheduledCompletionDate+"&nbsp;</td>");
     routeInfo.append("<td>"+routeMap.get(SELECT_OWNER)+"&nbsp;</td>");
     routeInfo.append("</tr>");
   }

   if(totalResultList.size()==0) {
     routeInfo.append("<tr><td colspan=\"6\">"+ EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.NoObjectsFound")+"</td></tr>");
   }
   routeInfo.append("</table>");
   return routeInfo.toString();
     }
     catch (Exception ex)
     {
       throw ex;
     }
 }

/**
 * @param context
 * @param args
 * @return
 * @throws Exception
 */
public  String getChangeApprovals(Context context,String args[]) throws Exception {


    String languageStr = context.getSession().getLanguage();
    String objectId = args[0];

    boolean bRouteSize         =false;
    boolean bSign              =false;

    DomainObject sChangeObject = new DomainObject(objectId);
    String sPolicy = sChangeObject.getInfo(context, SELECT_POLICY);
    MapList mpListOfTasks     = new MapList();
    Route routeObj = (Route)DomainObject.newInstance(context,TYPE_ROUTE);
    setId(objectId);
    MapList stateRouteList = getApprovalsInfo(context);

    String sRouteName = "";
    String sRouteStatus = "";
    String sPersonName             = "";
    String routeNodeStatus         = "";
    String routeNodeComments       = "";

    Hashtable memberMap = new Hashtable();

    StringList strListRouteCheck = new StringList();
    String sRouteId = null;

     SelectList objSelects  = new SelectList();
     objSelects.addElement(Route.SELECT_COMMENTS);
     objSelects.addElement(Route.SELECT_APPROVAL_STATUS);
     objSelects.addElement("from["+RELATIONSHIP_PROJECT_TASK+"].to.name");

     StringBuffer returnString=new StringBuffer();
     returnString.append(" <table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
     returnString.append("<tr><th>");
     returnString.append(EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.State"));
     returnString.append("</th> <th>");
     returnString.append(EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Route"));
     returnString.append("</th> <th>");
     returnString.append(EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Signer"));
     returnString.append("</th> <th>");
     returnString.append(EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Status"));
     returnString.append("</th> <th>");
     returnString.append(EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Description"));
     returnString.append("</th> </tr>");


     Iterator mapItr = stateRouteList.iterator();
 while(mapItr.hasNext())
 {
     Map stateRouteMap = (Map)mapItr.next();

   boolean hasRoutes = false;
   // Check for State Name and Ad Hoc routes
   String sStateName = (String)stateRouteMap.get(SELECT_NAME);
  // Check for Routes
  Vector routes = new Vector();
 if (sStateName != null) {
         routes = (Vector)stateRouteMap.get(KEY_ROUTES);
     if((routes.size() >0)) {
        hasRoutes = true;
     }
 }
 if ("Ad Hoc Routes".equals(sStateName)) {
     sStateName = "Ad Hoc";
   }
   // Check for Routes
   routes = (Vector)stateRouteMap.get(KEY_ROUTES);


   if (hasRoutes) {

 for (int rteCnt = 0; rteCnt < routes.size(); rteCnt++) {
        bRouteSize=true;
        sRouteId = (String)routes.get(rteCnt);

        if(!strListRouteCheck.contains(sRouteId)){
            strListRouteCheck.add(sRouteId);

        returnString.append("<tr >");

        if ((rteCnt == 0)) {
            sStateName = FrameworkUtil.findAndReplace(sStateName," ", "_");
            sStateName = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.State."+sPolicy.replaceAll(" ", "_")+"."+sStateName);
            returnString.append("<td valign=\"top\" class=\"listCell\" style=\"text-align: \" >"+sStateName+"&nbsp;</td>");

          }
        else {
          returnString.append("<td>&nbsp;</td>");
        }

        if(sRouteId != null && !"null".equals(sRouteId) && !"".equals(sRouteId))
        {
           routeObj.setId(sRouteId);
           sRouteName = routeObj.getInfo(context, SELECT_NAME);
           sRouteStatus = routeObj.getAttributeValue(context,PropertyUtil.getSchemaProperty(context,"attribute_RouteStatus"));

           mpListOfTasks = routeObj.getRouteTasks(context, objSelects, null, "",false);

        }
        returnString.append("<td>"+sRouteName+"</td>");

        String strRouteStatus = i18nNow.getRangeI18NString("", "Not Started",languageStr);
String sRoute = "";
        if(sRouteStatus!=null && !sRouteStatus.equals(strRouteStatus)){
    for(int k = 0; k < mpListOfTasks.size() ; k++)
        {
           memberMap = (Hashtable) mpListOfTasks.get(k);

           sPersonName             = (String) memberMap.get("from["+RELATIONSHIP_PROJECT_TASK+"].to.name");
           routeNodeStatus         = (String) memberMap.get(Route.SELECT_APPROVAL_STATUS);
           routeNodeComments       = (String) memberMap.get(Route.SELECT_COMMENTS);

           if(sPersonName == null || "null".equals(sPersonName) || "".equals(sPersonName)){
             sPersonName = " ";
           }
           if (sPersonName != null && sPersonName.trim().length() > 0 && sPersonName.indexOf("auto_") == -1)
           {
				  try
				  {
           sPersonName = PersonUtil.getFullName(context, sPersonName);
        }
				catch (Exception e)
				{
					sPersonName = " ";
				}
				}
        }
           }
           returnString.append("<td>"+sPersonName+"&nbsp;</td>");

    	   if(!"".equals(routeNodeStatus)) {

     		sRoute =  EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Approval_Status."+routeNodeStatus);
    	   }
           returnString.append("<td>"+sRoute+"&nbsp;</td> <td>"+XSSUtil.encodeForHTML(context, routeNodeComments)+"&nbsp;</td></tr>");


        }
        sPersonName="";
        routeNodeStatus="";
        routeNodeComments="";
     }
     }
   }

	 if (bRouteSize==false && bSign==false)
	 {
		returnString.append("<tr><td class=\"even\" colspan=\"5\" align=\"center\" >"+ EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.SummaryReport.NoSignOrRoutes")+"</td></tr>");
	 }
	 returnString.append("</table>");
	return  returnString.toString();
	}



	/**
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public String generateCAHtmlSummaryReport(Context context,String args[]) throws Exception {

			  if (args == null || args.length < 1) {
			            throw (new IllegalArgumentException());
			      }

			    String objectId = args[0];
			    StringBuffer summaryReport = new StringBuffer();
			    ChangeAction ecoObj = null;
			    try
			    {
			       String strLanguage = context.getSession().getLanguage();
			       ecoObj = new ChangeAction(objectId);
			       // Date in suitable format.
			       java.util.Calendar cal = new GregorianCalendar(TimeZone.getDefault());
			       int month = cal.get(Calendar.MONTH);
			       int dates = cal.get(Calendar.DATE);
			       int year =  cal.get(Calendar.YEAR);
			       int hour =  cal.get(Calendar.HOUR);
			       int minute = cal.get(Calendar.MINUTE);
			       int AM_PM = cal.get(Calendar.AM_PM);
			       String[] monthDesc = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
			       String[] AMPM = new String[]{"AM","PM"};
			       String smonth = monthDesc[month];
			       String sAMPM = AMPM[AM_PM];
			       String dateAndTime =  smonth+" "+dates+","+year+","+hour+":"+minute+" "+sAMPM;
			       //Summary Report Heading
			       summaryReport.append("<html>");
			       summaryReport.append("<div id=\"pageHeader\">");
			       summaryReport.append("<table border=\"0\" width=\"100%\">");
			       summaryReport.append("<tr><td class=\"pageHeader\"><h1>"+ecoObj.getInfo(context,SELECT_NAME)+":&nbsp;"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.SummaryReportHeader")+"</h1></td>");
			       summaryReport.append("<td class=\"pageSubtitle\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Generated")+" "+dateAndTime+"</td></tr>");
			       summaryReport.append("</table>");
			       summaryReport.append("</div>");


			       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Attributes")+"</h2></td></tr></table>");
			       summaryReport.append(getCABasicInfo(context,args));



			       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Approvals")+"</h2></td></tr></table>");
			       summaryReport.append(getChangeApprovals(context,args));


			       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Routes")+"</h2></td></tr></table>");
			       summaryReport.append(getChangeRoutes(context,args));



			       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Tasks")+"</h2></td></tr></table>");
			       summaryReport.append(getChangeTasks(context,args));



			       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Command.ProposedChanges")+"</h2></td></tr></table>");
			       summaryReport.append(getCAAffectedItems(context,args));



			       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Command.ImplementedItems")+"</h2></td></tr></table>");
			       summaryReport.append(getCAImplementedItems(context,args));


			       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Command.ReferenceDocuments")+"</h2></td></tr></table>");
			       summaryReport.append(getChangeRelatedReferenceDocuments(context,args));


			      summaryReport.append("</html>");
			    }
			    catch (Exception e)
			    {
			      throw e;
			    }
			    return summaryReport.toString();
			   }

	public String getCAImplementedItems(Context context,String[] args)
			throws Exception
	{
		String strLanguage = context.getSession().getLanguage();
		String objectId = args[0];
		MapList realizedList = new MapList();
		StringBuffer specs = new StringBuffer();

		try
		{
			ChangeAction ecoObj = new ChangeAction(objectId);

			realizedList= ecoObj.getRealizedChanges(context);
			realizedList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
			realizedList.sort();

			Iterator objItr = realizedList.iterator();
			Map specMap  = null;
			String imgSpecType = "";

			specs.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
			specs.append("<tr>");

			specs.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Name")+"</th>");
			specs.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Type")+"</th>");
			specs.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Description")+"</th>");
			specs.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.REVISION")+"</th>");
			specs.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.State")+"</th>");
			specs.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.RequestedChange")+"</th>");
			specs.append("</tr>");
			while (objItr.hasNext())
			{
				specMap = (Map)objItr.next();

				imgSpecType = UINavigatorUtil.getTypeIconProperty(context, (String)specMap.get(SELECT_TYPE));
				if (imgSpecType == null || imgSpecType.length() == 0 )
				{
					imgSpecType = "iconSmallDefault.gif";
				}

				String sAttrReqChange = PropertyUtil.getSchemaProperty(context,
						"attribute_RequestedChange");
				String sReqChangeVal = FrameworkUtil.findAndReplace((String) specMap.get(sAttrReqChange), " ","_");
				sReqChangeVal = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Requested_Change."+sReqChangeVal);

				specs.append("<tr>");
				specs.append("<td><img src=\"../common/images/"+imgSpecType+"\" border=\"0\" alt=\"*\">&nbsp;"+XSSUtil.encodeForHTML(context,(String)specMap.get(SELECT_NAME))+"&nbsp;</td>");
				specs.append("<td>"+i18nNow.getAdminI18NString("Type",(String)specMap.get(DomainConstants.SELECT_TYPE), strLanguage)+"&nbsp;</td>");
				specs.append("<td>"+XSSUtil.encodeForHTML(context,(String)specMap.get(SELECT_DESCRIPTION))+"&nbsp;</td>");
				specs.append("<td>"+specMap.get(SELECT_REVISION)+"&nbsp;</td>");
				specs.append("<td>"+i18nNow.getStateI18NString((String)specMap.get(SELECT_POLICY),(String)specMap.get(SELECT_CURRENT),strLanguage)+"&nbsp;</td>");
				specs.append("<td>"+sReqChangeVal+"&nbsp;</td>");
				specs.append("</tr>");
			}
			if(realizedList.size()==0) {
				specs.append("<tr><td colspan=\"6\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Change.NoImplementedItemsConnected")+"</td></tr>");
			}
		}
		catch (FrameworkException Ex)
		{
			throw Ex;
		}
		specs.append("</table>");
		return specs.toString();
	}

	   /**
	     * Constructs the HTML table of the ECO Attributes.
	     *
	     * @param context the eMatrix <code>Context</code> object.
	     * @param args holds the following input arguments:
	     * 0 - String containing object id.
	     * @return String Attributes in the form of HTML table.
	     * @throws Exception if the operation fails.
	     * @since Engineering Central X3
	    */

	   public  String getCABasicInfo(Context context,String args[]) throws Exception {

	    if (args == null || args.length < 1) {
	          throw (new IllegalArgumentException());
	    }
	    String objectId = args[0];
	    String strLanguage = context.getSession().getLanguage();



	    String sRelResponsibleOrganization = SELECT_ORGANIZATION;
	    String sRelChangeAction = PropertyUtil.getSchemaProperty(context,"relationship_ChangeAction");
	    String sRelTechAssignee = PropertyUtil.getSchemaProperty(context,"relationship_TechnicalAssignee");
	    String sRelChangeReviewer = PropertyUtil.getSchemaProperty(context,"relationship_ChangeReviewer");


	    String ATTRIBUTE_REASON_FOR_CANCEL = PropertyUtil.getSchemaProperty(context,"attribute_ReasonForCancel");


	    StringBuffer returnString = new StringBuffer();
	    setId(objectId);

	    StringList objectSelects = new StringList();
	    objectSelects.add(SELECT_NAME);
	    objectSelects.add(SELECT_TYPE);
	    objectSelects.add(SELECT_REVISION);
	    objectSelects.add(SELECT_CURRENT);
	    objectSelects.add(SELECT_OWNER);
	    objectSelects.add(SELECT_ORIGINATED);
	    objectSelects.add(SELECT_ORIGINATOR);
	    objectSelects.add(SELECT_DESCRIPTION);
	    objectSelects.add(SELECT_MODIFIED);
	    objectSelects.add(SELECT_POLICY);
	    objectSelects.add("to["+sRelChangeAction+"].from.name");
	    objectSelects.add("to.relationship[relationship_ChangeAction]");
	    objectSelects.add("from["+sRelTechAssignee+"].to.name");
	    objectSelects.add("from["+sRelChangeReviewer+"].to.name");
	    objectSelects.add(SELECT_ORGANIZATION);



	    Map attributeMap = getInfo(context, objectSelects);

	    String attrName  = null;
	    String attrValue = null;
	    String sCurrentState = (String)attributeMap.get(SELECT_CURRENT);
	    String ecoDesc = (String)attributeMap.get(SELECT_DESCRIPTION);

	    ecoDesc = FrameworkUtil.findAndReplace(ecoDesc,"\n","<br>");

	  returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
	  returnString.append("<tr>");
	  returnString.append("<td><table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Name")+":</td><td class=\"inputField\">"+attributeMap.get(SELECT_NAME)+"</td></tr>");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Type")+":</td><td class=\"inputField\">"+EnoviaResourceBundle.getTypeI18NString(context, (String)attributeMap.get(SELECT_TYPE), strLanguage)+"</td></tr>");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.State")+":</td><td class=\"inputField\">"+i18nNow.getStateI18NString((String)attributeMap.get(SELECT_POLICY),(String)attributeMap.get(SELECT_CURRENT),strLanguage)+"</td></tr>");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Owner")+":</td><td class=\"inputField\">"+PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_OWNER)))+"</td></tr>");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.ORIGINATOR")+":</td><td class=\"inputField\">"+PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_ORIGINATOR)))+"</td></tr>");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.ORIGINATED")+":</td><td class=\"inputField\">"+(String)attributeMap.get(SELECT_ORIGINATED)+"</td></tr>");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.MODIFIED")+":</td><td class=\"inputField\">"+(String)attributeMap.get(SELECT_MODIFIED)+"</td></tr>");

	  returnString.append("</table></td>");
	  returnString.append("<td><table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Description")+":</td><td class=\"inputField\">"+XSSUtil.encodeForHTML(context,ecoDesc)+"&nbsp;</td></tr>");



	    Map attrMap = getAttributeMap(context);
	    if(!sCurrentState.equals(STATE_ECO_CANCELLED))
	       {
	    	   attrMap.remove(ATTRIBUTE_REASON_FOR_CANCEL);
	       }
	    Iterator ecoAttrListItr = attrMap.keySet().iterator();

	    while (ecoAttrListItr.hasNext())
		  {
			  attrName = (String)ecoAttrListItr.next();
			  attrValue = (String)attrMap.get(attrName);
			  returnString.append("<tr>");
			  if(!EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Originator").equals(attrName)){
				  returnString.append("<td class=\"label\">"+i18nNow.getAttributeI18NString(attrName, strLanguage)+":</td>");
				  returnString.append("<td class=\"inputField\">"+i18nNow.getRangeI18NString(attrName, attrValue, strLanguage)+"</td>");
			  }
		  }
	            returnString.append("</tr>");




	  //get Reviewal and Approval Lists
	  MapList mapRouteTemplate = new MapList();
	  StringList selectStmts = new StringList();
	  selectStmts.addElement("attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]");
	  selectStmts.addElement(SELECT_NAME);

	  mapRouteTemplate = getRelatedObjects(context,
	          DomainConstants.RELATIONSHIP_OBJECT_ROUTE, DomainConstants.TYPE_ROUTE_TEMPLATE,
	          selectStmts, null, false, true, (short) 1, null, null);

	  Iterator mapItr = mapRouteTemplate.iterator();
	  String strRouteBasePurpose = "";
	  String strReviewalRoute ="";
	  String strApprovalRoute ="";
	  while(mapItr.hasNext()) {
	      Map mpRouteTemplated = (Map)mapItr.next();
	      strRouteBasePurpose = (String)mpRouteTemplated.get("attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]");
	      if(strRouteBasePurpose.equals(EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", new Locale("en"),"EnterpriseChangeMgt.Common.Approval"))){
	          strApprovalRoute = (String)mpRouteTemplated.get(SELECT_NAME);
	      }
	      if(strRouteBasePurpose.equals(EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", new Locale("en"),"EnterpriseChangeMgt.Common.Review"))){
	          strReviewalRoute = (String)mpRouteTemplated.get(SELECT_NAME);
	      }
				   }

	  if(strReviewalRoute==null || "null".equals(strReviewalRoute)){
	      strReviewalRoute="";
				   }
	  if(strApprovalRoute==null || "null".equals(strApprovalRoute)){
	      strApprovalRoute="";
			   }



	  String sPolicy = (String) attributeMap.get(SELECT_POLICY);
	  if(null!=sPolicy && null!=strLanguage)
		    sPolicy = i18nNow.getAdminI18NString("Policy", sPolicy, strLanguage);
	  DomainObject cocrobj = new DomainObject();
	  String COName = "";
	  String CRName = "";
	  StringList sl = getInfoList(context, "to["+sRelChangeAction+"].from.id");
	  Iterator itr = sl.iterator();
	  while(itr.hasNext()){
		  String objId = (String)itr.next();
		  cocrobj.setId((String)objId);
		  if(cocrobj.isKindOf(context, ChangeConstants.TYPE_CHANGE_ORDER)){
			  COName=(String)cocrobj.getName(context);
		  }else{
			  CRName=(String)cocrobj.getName(context);
		  }
	  }
	  String techAssignee   = (String)attributeMap.get("from["+sRelTechAssignee+"].to.name");
	  String techSrAssignee = (String)attributeMap.get("from["+sRelChangeReviewer+"].to.name");
	  techAssignee			= UIUtil.isNullOrEmpty(techAssignee) ? "" : PersonUtil.getFullName(context,techAssignee);
	  techSrAssignee	    = UIUtil.isNullOrEmpty(techSrAssignee) ? "" : PersonUtil.getFullName(context,techSrAssignee);

	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.ResponsibleOrganisation")+":</td><td class=\"inputField\">"+(String)attributeMap.get(SELECT_ORGANIZATION)+"</td></tr>");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.GoverningCO")+":</td><td class=\"inputField\">"+COName+"</td></tr>");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.GoverningCR")+":</td><td class=\"inputField\">"+CRName+"</td></tr>");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.TechAssignee")+":</td><td class=\"inputField\">"+techAssignee+"</td></tr>");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.SrTechnicalAssignee")+":</td><td class=\"inputField\">"+techSrAssignee+"</td></tr>");

	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Policy")+"</strong>:&nbsp;</td><td class=\"inputField\">"+sPolicy+"</td></tr>");


	  //returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(),"emxComponents.Form.Label.ApprovalList")+"</strong>:&nbsp;</td><td class=\"inputField\">"+strApprovalRoute+"</td></tr>");
	  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.TechnicalApproversList")+"</strong>:&nbsp;</td><td class=\"inputField\">"+XSSUtil.encodeForHTML(context, strApprovalRoute)+"</td></tr>");




	  returnString.append("</table></td>");
	  returnString.append("</tr>");
	  returnString.append("</table>");

	  String finalStr = returnString.toString();
	  return finalStr;
	 }

	   public String getCAAffectedItems(Context context,String[] args)
			   throws Exception
	   {
		   String strLanguage = context.getSession().getLanguage();
		   String objectId = args[0];
		   MapList mpProposedChangeList = new MapList();
		   StringBuffer sbProposed = new StringBuffer();

		   try
		   {
			   ChangeAction ecoObj = new ChangeAction(objectId);
			   mpProposedChangeList = ecoObj.getAffectedItems(context);
			   mpProposedChangeList.addSortKey(ChangeConstants.RELATED_CA_NAME, "ascending", "String");
			   mpProposedChangeList.sort();


			   Iterator objItr = mpProposedChangeList.iterator();
			   Map proposedChangeMap  = null;
			   String imgSpecType = "";

			   sbProposed.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
			   sbProposed.append("<tr>");

			   sbProposed.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Name")+"</th>");
			   sbProposed.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Type")+"</th>");
			   sbProposed.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Description")+"</th>");
			   sbProposed.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.REVISION")+"</th>");
			   sbProposed.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.State")+"</th>");
			   sbProposed.append("<th>"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.RequestedChange")+"</th>");
			   sbProposed.append("</tr>");
			   while (objItr.hasNext())
			   {
				   proposedChangeMap = (Map)objItr.next();

				   imgSpecType = UINavigatorUtil.getTypeIconProperty(context,(String)proposedChangeMap.get(SELECT_TYPE));
				   if (imgSpecType == null || imgSpecType.length() == 0 )
				   {
					   imgSpecType = "iconSmallDefault.gif";
				   }

				   String sAttrReqChange = PropertyUtil.getSchemaProperty(context,
						   "attribute_RequestedChange");
				   String sReqChangeVal = FrameworkUtil.findAndReplace((String) proposedChangeMap.get(sAttrReqChange), " ","_");

				   sReqChangeVal = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Requested_Change."+sReqChangeVal);

				   sbProposed.append("<tr>");
				   sbProposed.append("<td><img src=\"../common/images/"+imgSpecType+"\" border=\"0\" alt=\"*\">&nbsp;"+XSSUtil.encodeForHTML(context,(String)proposedChangeMap.get(SELECT_NAME))+"&nbsp;</td>");
				   sbProposed.append("<td>"+i18nNow.getAdminI18NString("Type",(String)proposedChangeMap.get(DomainConstants.SELECT_TYPE), strLanguage)+"&nbsp;</td>");
				   sbProposed.append("<td>"+XSSUtil.encodeForHTML(context,(String)proposedChangeMap.get(SELECT_DESCRIPTION))+"&nbsp;</td>");
				   sbProposed.append("<td>"+proposedChangeMap.get(SELECT_REVISION)+"&nbsp;</td>");
				   sbProposed.append("<td>"+i18nNow.getStateI18NString((String)proposedChangeMap.get(SELECT_POLICY),(String)proposedChangeMap.get(SELECT_CURRENT),strLanguage)+"&nbsp;</td>");
				   sbProposed.append("<td>"+sReqChangeVal+"&nbsp;</td>");
				   sbProposed.append("</tr>");
			   }
			   if(mpProposedChangeList.size()==0) {
				   sbProposed.append("<tr><td colspan=\"6\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Change.NoProposedChangesConnected")+"</td></tr>");
			   } 
		   }
		   catch (FrameworkException Ex)
		   {
			   throw Ex;
		   }
		   sbProposed.append("</table>");
		   return sbProposed.toString();
	   }

	   /**
	    * Generates the ECO Summary PDF file and checks it into the ECO object.
	    *
	    * @param context
	    *            the eMatrix <code>Context</code> object.
	    * @param args
	    *            holds the following input arguments: 0 - String containing
	    *            object id.
	    * @param summaryReport
	    *            holds the string which need to be rendered into PDF.
	    * @return int 0- for success, 1- failure.
	    * @throws Exception
	    *             if the operation fails.
	    * @since Engineering Central X3
	    */

	   public int renderPDFFileForChange(Context context, String[] args,
	           String summaryReport) throws Exception {

	 		String objectId = args[0];

	 		setId(objectId);
	 		String objType = getInfo(context, SELECT_TYPE);
	 		String objName = getInfo(context,SELECT_NAME);
	 		String objRev = getInfo(context,SELECT_REVISION);

	 		//String languageCode = context.getSession().getLanguage();
	 		String languageCode = "en";


	 		RenderPDF renderPDF = new RenderPDF();

	 		renderPDF.loadProperties(context);

	 		String timeStamp = Long.toString(System.currentTimeMillis());

	 		String folderName = objectId + "_" + timeStamp;
	 		folderName = folderName.replace(':','_');

	 		if (renderPDF.renderSoftwareInstalled == null || "false".equalsIgnoreCase(renderPDF.renderSoftwareInstalled) )
	 		{
	 		  MqlUtil.mqlCommand(context, "notice $1","Render Software not Installed");
	 		  return 1;
	       }


	 		String ftpInputFolder = renderPDF.inputFolder + java.io.File.separator + folderName;
	 		String ftpOutputFolder = renderPDF.outputFolder + java.io.File.separator + folderName;

	 		try
	 		{
	 			renderPDF.createPdfInputOpuputDirectories(context, folderName);
	       }
	 		catch (Exception ex)
	 		{
	 		  MqlUtil.mqlCommand(context, "notice $1","Unable to connect to ftp server or no write access");
	 		  return 1;
	       }

	 		String fileName = objName + "-Rev" + objRev + ".htm";
	 		String dpiFileName = objName + "-Rev" + objRev + ".dpi";
	 		String pdfFileName = objName + "-Rev" + objRev + ".pdf";

	 		mxFtp clientHtm = new mxFtp();
	 		String charset = FrameworkProperties.getProperty(context, "emxFramework.Charset." + languageCode);

	 		try
	 		{
	 			clientHtm.connect(renderPDF.strProtocol,renderPDF.strHostName,null,renderPDF.strUserName,renderPDF.strPassword, ftpInputFolder,true);
	 			clientHtm.create(fileName);
	 			Writer outHtm = new BufferedWriter(new OutputStreamWriter(new MyOutputStream(clientHtm),charset));
	 			outHtm.write(summaryReport);
	 			outHtm.flush();
	 			outHtm.close();
	 		}
	 		catch (Exception ex)
	 		{
	 			MqlUtil.mqlCommand(context, "notice $1","Unable to connect to ftp server");
	 			return 1;
	 		}
	 		finally
	 		{
	 			clientHtm.close();
	 			clientHtm.disconnect();
	            }

	 		String watermark = FrameworkProperties.getProperty(context, "emxFramework.RenderPDF.WaterMark");
	 		String mark = watermark;
	 		if (watermark == null || "null".equals(watermark))
	 		{
	 			watermark="";
	               }
	 		else if(watermark.length() > 0)
	 		{
	 			try
	 			{

	             	watermark = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", new Locale("en"),watermark);
	               }
	 			catch(Exception e)
	 			{
	 				watermark = mark;
	               }
	 			watermark = MessageUtil.substituteValues(context, watermark, objectId, languageCode);
	       }

	 		StringList files = new StringList(1);

	 		renderPDF.writeDPI(context, ftpInputFolder, fileName, dpiFileName, files, watermark,charset);

	 		boolean renderProcess = renderPDF.generatedPDFExists(context, pdfFileName, ftpOutputFolder);

	 		if (renderProcess)
	 		{


	 			String strTempDir = context.createWorkspace();

	 			java.io.File outfile = new java.io.File(strTempDir + java.io.File.separator + pdfFileName);

	 			FileOutputStream fos = new FileOutputStream(outfile);

	 			mxFtp ftpPDF = new mxFtp();
	 			ftpPDF.connect(renderPDF.strProtocol,renderPDF.strHostName,null,renderPDF.strUserName,renderPDF.strPassword,ftpOutputFolder,true);
	 			ftpPDF.open(pdfFileName);
	 			InputStream inSupp = new com.matrixone.apps.domain.util.MyFtpInputStream(ftpPDF);

	             emxcommonPushPopShadowAgent_mxJPO PushPopShadowAgent = new emxcommonPushPopShadowAgent_mxJPO(context, null);

	 				try
	 				{

	                                long cnt = com.matrixone.fcs.common.TransportUtil.transport(inSupp,fos, 8*1024);
	                  //363896
	 				 /* Push Shadow Agent */
	                  PushPopShadowAgent.pushContext(context,null);
	 				String cmd = "checkin bus \"$1\" \"$2\" \"$3\" format $4 '$5'";
	 				MqlUtil.mqlCommand(context, cmd, objType, objName, objRev, "generic", strTempDir + java.io.File.separator + pdfFileName);
	           }
	 				catch (Exception ex)
	 				{
	 					MqlUtil.mqlCommand(context, "notice $1", ex.getMessage());
	 					return 1;
	           }
	 				finally
	 				{
	 				inSupp.close();
	 					fos.close();
	 				ftpPDF.disconnect();
	 				ftpPDF.close();
	 				 //363896
	                 PushPopShadowAgent.popContext(context,null);
	                 }

	               }
	 		else
	                 {
	 			MqlUtil.mqlCommand(context, "notice $1","Unable to generate pdf on adlib server");
	 			return 1;
	               }

	 		return 0;

	 }
	   public  String getCOBasicInfo(Context context,String args[]) throws Exception {

			  if (args == null || args.length < 1) {
			          throw (new IllegalArgumentException());
			    }
			    String objectId = args[0];
			    String strLanguage = context.getSession().getLanguage();

			    String sRelECDistributionList = PropertyUtil.getSchemaProperty(context,"relationship_ECDistributionList");
			    String sRelChangeCordinator = PropertyUtil.getSchemaProperty(context,"relationship_ChangeCoordinator");
			    String sRelChangeOrder      = PropertyUtil.getSchemaProperty(context,"relationship_ChangeOrder");
			    String sRelResponsibleOrganization = SELECT_ORGANIZATION;
			   //for bug 344498 ends
			    String ATTRIBUTE_REASON_FOR_CANCEL = PropertyUtil.getSchemaProperty(context,"attribute_ReasonForCancel");
			    StringBuffer returnString = new StringBuffer();
			    setId(objectId);

			    StringList objectSelects = new StringList();
			    objectSelects.add(SELECT_NAME);
			    objectSelects.add(SELECT_TYPE);
			    objectSelects.add(SELECT_REVISION);
			    objectSelects.add(SELECT_CURRENT);
			    objectSelects.add(SELECT_OWNER);
			    objectSelects.add(SELECT_ORIGINATED);
			    objectSelects.add(SELECT_ORIGINATOR);
			    objectSelects.add(SELECT_DESCRIPTION);
			    objectSelects.add(SELECT_MODIFIED);
			    objectSelects.add(SELECT_POLICY);
			    objectSelects.add("from["+sRelChangeCordinator+"].to.name");
			    objectSelects.add("from["+sRelChangeOrder+"].to.name");
			    objectSelects.add("to["+sRelChangeOrder+"].from.name");
			    objectSelects.add(SELECT_ORGANIZATION);
			    objectSelects.add("from["+sRelECDistributionList+"].to.name");


			    Map attributeMap = getInfo(context, objectSelects);

			    String attrName  = null;
			    String attrValue = null;
			    String sCurrentState = (String)attributeMap.get(SELECT_CURRENT);
			    String ecoDesc = (String)attributeMap.get(SELECT_DESCRIPTION);

			    ecoDesc = FrameworkUtil.findAndReplace(ecoDesc,"\n","<br>");

			  returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
			  returnString.append("<tr>");
			  returnString.append("<td><table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Name")+":</td><td class=\"inputField\">"+attributeMap.get(SELECT_NAME)+"</td></tr>");
			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Type")+":</td><td class=\"inputField\">"+EnoviaResourceBundle.getTypeI18NString(context, (String)attributeMap.get(SELECT_TYPE), strLanguage)+"</td></tr>");
			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.State")+":</td><td class=\"inputField\">"+EnoviaResourceBundle.getStateI18NString(context,(String)attributeMap.get(SELECT_POLICY),(String)attributeMap.get(SELECT_CURRENT),strLanguage)+"</td></tr>");
			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Owner")+":</td><td class=\"inputField\">"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_OWNER)))+"</td></tr>");
			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.ORIGINATOR")+":</td><td class=\"inputField\">"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_ORIGINATOR)))+"</td></tr>");
			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.ORIGINATED")+":</td><td class=\"inputField\">"+(String)attributeMap.get(SELECT_ORIGINATED)+"</td></tr>");
			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.MODIFIED")+":</td><td class=\"inputField\">"+(String)attributeMap.get(SELECT_MODIFIED)+"</td></tr>");

			  returnString.append("</table></td>");
			  returnString.append("<td><table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Common.Description")+":</td><td class=\"inputField\">"+XSSUtil.encodeForHTML(context, ecoDesc)+"&nbsp;</td></tr>");



			  Map attrMap = getAttributeMap(context);
			  Iterator ecoAttrListItr = attrMap.keySet().iterator();

			  while (ecoAttrListItr.hasNext())
			  {
				  attrName = (String)ecoAttrListItr.next();
				  attrValue = (String)attrMap.get(attrName);
				  returnString.append("<tr>");
				  if(!EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Originator").equals(attrName)){
					  if(attrName.equalsIgnoreCase("Estimated Completion Date"))
						  returnString.append("<td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.DueDate")+":</td>");
					  else						  
						  returnString.append("<td class=\"label\">"+i18nNow.getAttributeI18NString(attrName, strLanguage)+":</td>");
					  returnString.append("<td class=\"inputField\">"+i18nNow.getRangeI18NString(attrName, attrValue, strLanguage)+"</td>");
				  }
			  }
			  returnString.append("</tr>");

			  //get Reviewal and Approval Lists
			  MapList mapRouteTemplate = new MapList();
			  StringList selectStmts = new StringList();
			  selectStmts.addElement("attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]");
			  selectStmts.addElement(SELECT_NAME);

			  mapRouteTemplate = getRelatedObjects(context,
			          DomainConstants.RELATIONSHIP_OBJECT_ROUTE, DomainConstants.TYPE_ROUTE_TEMPLATE,
			          selectStmts, null, false, true, (short) 1, null, null);

			  Iterator mapItr = mapRouteTemplate.iterator();
			  String strRouteBasePurpose = "";
			  String strReviewalRoute ="";
			  String strApprovalRoute ="";
			  while(mapItr.hasNext()) {
			      Map mpRouteTemplated = (Map)mapItr.next();
			      strRouteBasePurpose = (String)mpRouteTemplated.get("attribute["+ATTRIBUTE_ROUTE_BASE_PURPOSE+"]");
			      if(strRouteBasePurpose.equals(EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", new Locale("en"),"EnterpriseChangeMgt.Common.Approval"))){
			          strApprovalRoute = (String)mpRouteTemplated.get(SELECT_NAME);
			      }
			      if(strRouteBasePurpose.equals(EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", new Locale("en"),"EnterpriseChangeMgt.Common.Review"))){
			          strReviewalRoute = (String)mpRouteTemplated.get(SELECT_NAME);
			      }
						   }

			  if(strReviewalRoute==null || "null".equals(strReviewalRoute)){
			      strReviewalRoute="";
						   }
			  if(strApprovalRoute==null || "null".equals(strApprovalRoute)){
			      strApprovalRoute="";
					   }
			  String strDistributionList = (String)attributeMap.get("from["+sRelECDistributionList+"].to.name");
			  if(strDistributionList==null || "null".equals(strDistributionList)){
			      strDistributionList="";
				   }
			  String strChangeCoordinator = (String)attributeMap.get("from["+sRelChangeCordinator+"].to.name");
			  strChangeCoordinator = UIUtil.isNullOrEmpty(strChangeCoordinator) ? "" : PersonUtil.getFullName(context,strChangeCoordinator);

			  String sPolicy = (String) attributeMap.get(SELECT_POLICY);
			  if(null!=sPolicy && null!=strLanguage)
				    sPolicy = i18nNow.getAdminI18NString("Policy", sPolicy, strLanguage);

			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.ResponsibleOrganisation")+":</td><td class=\"inputField\">"+(String)attributeMap.get(SELECT_ORGANIZATION)+"</td></tr>");
			  if(isKindOf(context, ChangeConstants.TYPE_CHANGE_ORDER)){
			  String strChange = (String)attributeMap.get("to["+sRelChangeOrder+"].from.name");
			  strChange = UIUtil.isNotNullAndNotEmpty(strChange)?strChange:"";
			  String DisplayChangeLabel = EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.RelatedCR");

			  returnString.append("<tr><td class=\"label\">"+DisplayChangeLabel+":</td><td class=\"inputField\">"+strChange+"</td></tr>");
			  }
			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.ChangeCoordinator")+":</td><td class=\"inputField\">"+strChangeCoordinator+"</td></tr>");
			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.Policy")+"</strong>:&nbsp;</td><td class=\"inputField\">"+sPolicy+"</td></tr>");


			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.ReviewerList")+"</strong>:&nbsp;</td><td class=\"inputField\">"+XSSUtil.encodeForHTML(context, strReviewalRoute)+"</td></tr>");
			  if(!isKindOf(context, ChangeConstants.TYPE_CHANGE_REQUEST))
			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.ApproversList")+"</strong>:&nbsp;</td><td class=\"inputField\">"+XSSUtil.encodeForHTML(context, strApprovalRoute)+"</td></tr>");

			  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEnterpriseChangeMgtStringResource", context.getLocale(),"EnterpriseChangeMgt.Label.FollowerList")+"</strong>:&nbsp;</td><td class=\"inputField\">"+XSSUtil.encodeForHTML(context, strDistributionList)+"</td></tr>");

			  returnString.append("</table></td>");
			  returnString.append("</tr>");
			  returnString.append("</table>");

			  String finalStr = returnString.toString();
			  return finalStr;
			   }

		public Vector showHTMLReportIconforStructureBrowser(Context context, String args[])throws FrameworkException{
			//XSSOK
			Vector columnVals = null;
			try {
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				StringList objSelects = new StringList(2);
				objSelects.addElement(SELECT_TYPE);
				objSelects.addElement(SELECT_CURRENT);
				objSelects.addElement(SELECT_ID);

				String type ="";
				String objectId ="";
				Map mapObjectInfo = null;
				StringBuffer sbEditIcon = null;

				String strSummaryReport= EnoviaResourceBundle.getProperty(context,SUITE_KEY, 
						"EnterpriseChangeMgt.Label.SummaryReport", context.getSession().getLanguage());

				MapList objectList = (MapList) programMap.get(ChangeConstants.OBJECT_LIST);
				StringList sObjectIDList = changeUtil.getStringListFromMapList(objectList, ChangeConstants.ID);

				if (objectList == null || objectList.size() == 0)
					return columnVals;
				else

					columnVals = new Vector(sObjectIDList.size());

				MapList COInfoList = DomainObject.getInfo(context, (String[])sObjectIDList.toArray(new String[sObjectIDList.size()]), objSelects);
				String strGeneratePDF = FrameworkProperties.getProperty(context, "EnterpriseChangeMgt.Change.ViewPdfSummary");
				if(!COInfoList.isEmpty()){
					Iterator sItr = COInfoList.iterator();
					while(sItr.hasNext()){
						mapObjectInfo = (Map)sItr.next();
						type = (String)mapObjectInfo.get(SELECT_TYPE);
						objectId = (String)mapObjectInfo.get(SELECT_ID);

						sbEditIcon = new StringBuffer();
							sbEditIcon.append("<a href=\"javaScript:emxTableColumnLinkClick('");
							sbEditIcon.append("../enterprisechangemgt/emxECMSummaryReportFS.jsp?objectId=");
							sbEditIcon.append(XSSUtil.encodeForHTMLAttribute(context, objectId));
							sbEditIcon.append("&amp;suiteKey=EnterpriseChangeMgt");

							sbEditIcon.append("', '700', '600', 'true', 'listHidden', '')\">");
							sbEditIcon.append("<img border=\"0\" src=\"../common/images/iconSmallReport.gif\" title=");
							sbEditIcon.append("\""+ XSSUtil.encodeForHTMLAttribute(context, strSummaryReport)+"\"");
							sbEditIcon.append("/></a>");
							//String strGeneratePDF = FrameworkProperties.getProperty(context, "EnterpriseChangeMgt.Change.ViewPdfSummary");

							if ("true".equalsIgnoreCase(strGeneratePDF))
							{
								sbEditIcon.append("<a href=\"javaScript:emxTableColumnLinkClick('");
								//sbEditIcon.append("../enterprisechangemgt/emxECMPDFSummaryReportFS.jsp?objectId=");
								sbEditIcon.append("../enterprisechangemgt/emxECMPdfSummaryReportFS.jsp?objectId=");
								sbEditIcon.append(XSSUtil.encodeForHTMLAttribute(context, objectId));
								sbEditIcon.append("&amp;suiteKey=EnterpriseChangeMgt");
								sbEditIcon.append("&amp;generateSummary=true");
								sbEditIcon.append("', '700', '600', 'true', 'listHidden', '')\">");
								sbEditIcon.append("<img border=\"0\" src=\"../common/images/iconActionPDF.gif\" title=");
								sbEditIcon.append("\""+ XSSUtil.encodeForHTMLAttribute(context, strSummaryReport)+"\"");
								sbEditIcon.append("/></a>");
							}

							columnVals.add(sbEditIcon.toString());



					}//end of while

				}

				return columnVals;
			} catch (Exception e) {
				throw new FrameworkException(e);
			}
		}

		/**
  * Checking the condtion for ShowLink.
  *
  * @param context the eMatrix <code>Context</code> object.
  * @param args holds objectId.
  * @return Boolean.
  * @throws Exception If the operation fails.
  *
  */
   public Boolean hasChangeViewPdfLink(Context context, String []args) throws Exception
   {
        boolean hasChangeViewPdfLink = false;
        String sChangePdfLink = FrameworkProperties.getProperty(context, "EnterpriseChangeMgt.Change.ViewPdfSummary");
        if("true".equalsIgnoreCase(sChangePdfLink))
        {
            hasChangeViewPdfLink=true;
        }
      return Boolean.valueOf(hasChangeViewPdfLink);
 }



}//end of class

