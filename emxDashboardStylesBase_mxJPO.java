
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.eMatrixDateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import matrix.db.*;
import matrix.util.*;

public class emxDashboardStylesBase_mxJPO {

    public emxDashboardStylesBase_mxJPO(Context context, String[] args) throws Exception {
    }

    public StringList highlightContent(Context context, String[] args) throws Exception {

        StringList slResults    = new StringList();
        Map programMap          = (Map) JPO.unpackArgs(args);
        MapList mlObjects       = (MapList) programMap.get("objectList");
        Map columnMap           = (Map) programMap.get("columnMap");
        Map settingsMap         = (Map) columnMap.get("settings");

        if (mlObjects.size() > 0) {

            String sRelSelect       = (String) settingsMap.get("RelationshipSelect");
            String sStates          = (String) settingsMap.get("States");
            String sTypes           = (String) settingsMap.get("Types");
            String sContents        = (String) settingsMap.get("Contents");
            String sBold            = (String) settingsMap.get("Bold");
            String sStyles          = (String) settingsMap.get("Styles");
            String sSkipEmptyCells  = (String) settingsMap.get("Skip Empty Cells");

            String sExpression      = (String) columnMap.get("expression_businessobject");
            Boolean bIsBO           = true;
            String[] aContents      = null;
            String[] aStyles        = null;

            if (null != sRelSelect) {
                if (!"".equals(sRelSelect)) {
                    sExpression = sRelSelect;
                    bIsBO = false;
                }
            } else if (sExpression == null || sExpression.equals("")) {
                sExpression = (String) columnMap.get("expression_relationship");
                bIsBO = false;
            }

            if (sContents != null) {
                aContents = sContents.split(",");
            } else {
                System.out.println("Error in execution of GNVStyles.highlightContent : Contents to be highlighted cannot be retrieved from column settings. Please apply the setting 'Contents' properly!");
            }

            if (sStyles != null)            { aStyles = sStyles.split(","); }
            if (sBold == null)              { sBold = "";                   }
            if (sSkipEmptyCells == null)    { sSkipEmptyCells = "";         }

            if ((aContents.length != aStyles.length)) {
                System.out.println("Error in execution of GNVStyles.highlightContent : Number of Contents does not match number of Styles!");
                aContents = null;
                aStyles = null;
            }

            for (int i = 0; i < mlObjects.size(); i++) {

                String sResult          = "custDefault";
                Map mObject             = (Map) mlObjects.get(i);
                String sOID             = (String) mObject.get("id");
                DomainObject dObject    = new DomainObject(sOID);
                String sValue           = "";
                Boolean bContinue       = true;

                if(sExpression.endsWith(".value")) { sExpression = sExpression.substring(0, sExpression.length() - 6); }

                if (bIsBO) {
                    sValue = dObject.getInfo(context, sExpression);
                } else {
                    String sRID = (String) mObject.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    if (null != sRID) {
                        if (!"".equals(sRID)) {
                            DomainRelationship dRel = new DomainRelationship(sRID);
                            StringList slBusSelects = new StringList();
                            slBusSelects.add(sExpression);
                            Map mResults = dRel.getRelationshipData(context, slBusSelects);
                            if (mResults.get(sExpression) instanceof StringList) {
                                StringList slTemp = (StringList) mResults.get(sExpression);
                                if(slTemp.size() > 0) {
                                    sValue = (String) slTemp.get(0);
                                }
                            } else {
                                sValue = (String) mResults.get(sExpression);
                            }
                        }
                    }

                }

                if (sStates != null) {
                    String sCurrent = dObject.getInfo(context, DomainConstants.SELECT_CURRENT);
                    if (!sStates.contains(sCurrent)) {
                        bContinue = false;
                    }
                }

                if (sTypes != null) {
                    String sType = dObject.getInfo(context, DomainConstants.SELECT_TYPE);
                    if (!sTypes.contains(sType)) {
                        bContinue = false;
                    }
                }

                if (sValue.equals("")) {
                    if (sSkipEmptyCells.equalsIgnoreCase("TRUE")) {
                        bContinue = false;
                    }
                }

                if (bContinue == true) {
                    if (aContents != null) {
                        int j;
                        if ((aStyles != null)) {
                            for (j = 0; j < aContents.length; ++j) {
                                if (aContents[j].equals(sValue)) {
                                    sResult = aStyles[j];
                                    break;
                                }
                            }
                        }
                    }
                }

                slResults.add(sResult);
            }
        }

        return slResults;

    }

    public StringList highlightDate(Context context, String[] args) throws Exception {

        StringList slResults    = new StringList();
        Map programMap          = (Map) JPO.unpackArgs(args);
        MapList mlObjects       = (MapList) programMap.get("objectList");
        Map columnMap           = (Map) programMap.get("columnMap");
        Map settingsMap         = (Map) columnMap.get("settings");
        Boolean bIsBO           = true;

        String sExpression = (String) columnMap.get("expression_businessobject");
        if (sExpression == null || sExpression.equals("")) {
            sExpression = (String) columnMap.get("expression_relationship");
            bIsBO = false;
            if (sExpression.contains("[")) {
                int iStart = sExpression.indexOf("[");
                int iEnd = sExpression.indexOf("]");
                sExpression = sExpression.substring(iStart + 1, iEnd);
            }
        }


        if (mlObjects.size() > 0) {

            String sStates  = (String) settingsMap.get("States");
            String sTypes   = (String) settingsMap.get("Types");
            String sDays    = (String) settingsMap.get("Days");

            if (sDays == null) { sDays = "5"; }
            int iDays = Integer.parseInt(sDays);

            Calendar cData  = Calendar.getInstance();
            Calendar cNow   = Calendar.getInstance(TimeZone.getDefault());
            Calendar cNext  = Calendar.getInstance(TimeZone.getDefault());
            cNext.add(6, iDays);
            SimpleDateFormat sdf = new SimpleDateFormat(eMatrixDateFormat.getEMatrixDateFormat(), Locale.ENGLISH);

            for (int i = 0; i < mlObjects.size(); i++) {

                String sResult = "custDateBlack";
                Map mObject = (Map) mlObjects.get(i);


                String sOID = (String) mObject.get(DomainConstants.SELECT_ID);
                DomainObject dObject = new DomainObject(sOID);

                Boolean bContinue = true;

                if (sStates != null) {
                    String sCurrent = dObject.getInfo(context, DomainConstants.SELECT_CURRENT);
                    if (!sStates.contains(sCurrent)) {
                        bContinue = false;
                    }
                }

                if (sTypes != null) {
                    String sType = dObject.getInfo(context, DomainConstants.SELECT_TYPE);
                    if (!sTypes.contains(sType)) {
                        bContinue = false;
                    }
                }

                if (bContinue == true) {

                    String sValue = "";
                    if (bIsBO) {
                        // expression based on business object data
                        sValue = dObject.getInfo(context, sExpression);

                    } else {
                        // expression based on relationship data

                        String sRID = (String) mObject.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                        DomainRelationship dRel = new DomainRelationship(sRID);
                        dRel.open(context);
                        sValue = dRel.getAttributeValue(context, sExpression);
                        dRel.close(context);
                    }


                    if (sValue != null) {

                        if (!sValue.equals("")) {
                            Date dData = null;
                            dData = sdf.parse(sValue);
                            cData.setTime(dData);
                            if (cData.before(cNow)) {
                                sResult = "custDateRed";
                            } else {
                                if (cData.before(cNext)) {
                                    sResult = "custDateYellow";
                                } else {
                                    sResult = "custDateGreen";
                                }
                            }
                        }
                    }

                }

                slResults.add(sResult);
            }
        }

        return slResults;

    }

    public StringList highlightOwnership(Context context, String[] args) throws Exception {


        StringList slResults    = new StringList();
        Map programMap          = (Map) JPO.unpackArgs(args);
        MapList mlObjects       = (MapList) programMap.get("objectList");
        Map columnMap           = (Map) programMap.get("columnMap");
        Map settingsMap         = (Map) columnMap.get("settings");
        String sUser            = context.getUser();

        if (mlObjects.size() > 0) {

            String sStates      = (String) settingsMap.get("States");
            String sTypes       = (String) settingsMap.get("Types");
            String sStyleOwner  = (String) settingsMap.get("Style Owner");
            String sStyleOthers = (String) settingsMap.get("Style Others");

            if(sStyleOwner  == null) { sStyleOwner  = "custOwner"; }
            if(sStyleOthers == null) { sStyleOthers = "custOtherOwners"; }

            StringList busSelects = new StringList();
            busSelects.add(DomainConstants.SELECT_OWNER);
            if (sTypes != null)  { busSelects.add(DomainConstants.SELECT_TYPE); }
            if (sStates != null) { busSelects.add(DomainConstants.SELECT_CURRENT); }

            for (int i = 0; i < mlObjects.size(); i++) {

                Map mObject             = (Map) mlObjects.get(i);
                String sOID             = (String) mObject.get(DomainConstants.SELECT_ID);
                DomainObject dObject    = new DomainObject(sOID);
                Map mData               = dObject.getInfo(context, busSelects);
                String sOwner           = (String)mData.get(DomainConstants.SELECT_OWNER);
                Boolean bApplyStyle     = true;

                if (sStates != null) {
                    String sCurrent = (String)mData.get(DomainConstants.SELECT_CURRENT);
                    if (!sStates.contains(sCurrent)) {
                        bApplyStyle = false;
                    }
                }

                if (sTypes != null) {
                    String sType = (String)mData.get(DomainConstants.SELECT_TYPE);
                    if (!sTypes.contains(sType)) {
                        bApplyStyle = false;
                    }
                }

                if(bApplyStyle) {
                    if(sOwner.equals(sUser)) {
                        slResults.add(sStyleOwner);
                    } else {
                        slResults.add(sStyleOthers);
                    }
                }

            }
        }

        return slResults;

    }

    // Comparison of dates within Baseline view of WBS
    public StringList compareToEstimatedStart(Context context, String[] args) throws Exception  { return compareTo(context, args, "attribute["+ DomainConstants.ATTRIBUTE_TASK_ESTIMATED_START_DATE+"]"); }
    public StringList compareToEstimatedFinish(Context context, String[] args) throws Exception { return compareTo(context, args, "attribute["+ DomainConstants.ATTRIBUTE_TASK_ESTIMATED_FINISH_DATE+"]");}
    public StringList compareTo(Context context, String[] args, String sExpressionToCompare) throws Exception {


        StringList slResults = new StringList();
        Map programMap = (Map) JPO.unpackArgs(args);
        MapList mlObjects = (MapList) programMap.get("objectList");
        Map columnMap = (Map) programMap.get("columnMap");
        Map settingsMap = (Map) columnMap.get("settings");

        Boolean bIsBO = true;
        String sExpression = (String) columnMap.get("expression_businessobject");
        if (sExpression == null || sExpression.equals("")) {
            sExpression = (String) columnMap.get("expression_relationship");
            bIsBO = false;
            if (sExpression.contains("[")) {
                int iStart = sExpression.indexOf("[");
                int iEnd = sExpression.indexOf("]");
                sExpression = sExpression.substring(iStart + 1, iEnd);
            }
        }


        if (mlObjects.size() > 0) {

            String sStates = (String) settingsMap.get("custStates");
            String sTypes = (String) settingsMap.get("custTypes");

            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa", Locale.ENGLISH);

            StringList busSelects = new StringList();
            busSelects.add(DomainConstants.SELECT_CURRENT);
            busSelects.add(DomainConstants.SELECT_TYPE);
            busSelects.add(sExpressionToCompare);

            for (int i = 0; i < mlObjects.size(); i++) {

                String sResult = "custDateBlack";
                Map mObject = (Map) mlObjects.get(i);
                String sOID = (String) mObject.get(DomainConstants.SELECT_ID);
                DomainObject dObject = new DomainObject(sOID);
                Map mData = dObject.getInfo(context, busSelects);
                Boolean bContinue = true;

                if (sStates != null) {
                    String sCurrent = (String) mData.get(DomainConstants.SELECT_CURRENT);
                    if (!sStates.contains(sCurrent)) {
                        bContinue = false;
                    }
                }

                if (sTypes != null) {
                    String sType = (String) mData.get(DomainConstants.SELECT_TYPE);
                    if (!sTypes.contains(sType)) {
                        bContinue = false;
                    }
                }

                if (bContinue == true) {

                    String sValue = "";
                    if (bIsBO) {         // expression based on business object data
                        sValue = dObject.getInfo(context, sExpression);
                    } else {            // expression based on relationship data
                        String sRID = (String) mObject.get(DomainConstants.SELECT_RELATIONSHIP_ID);
                        DomainRelationship dRel = new DomainRelationship(sRID);
                        dRel.open(context);
                        sValue = dRel.getAttributeValue(context, sExpression);
                        dRel.close(context);
                    }

                    if (sValue != null) {
                        if (!sValue.equals("")) {

                            // Get dates for comparison
                            Date dToCompare = sdf.parse(sValue);
                            String sEstimatedStart = (String) mData.get(sExpressionToCompare);
                            Date dStartEstimated = sdf.parse(sEstimatedStart);

                            // Determine css class
                            if (dToCompare.before(dStartEstimated)) {
                                sResult = "custDateRed";
                            } else {
                                if (dStartEstimated.before(dToCompare)) {
                                    sResult = "custDateYellow";
                                } else {
                                    sResult = "custDateGreen";
                                }
                            }
                        }
                    }
                }

                slResults.add(sResult);
            }
        }
        return slResults;
    }
}
