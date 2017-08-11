/*
 ** ${CLASSNAME}
 **
 ** Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 ** or intended publication of such program
 */

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import matrix.db.Access;
import matrix.db.AccessList;
import matrix.db.AttributeType;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectList;
import matrix.db.Context;
import matrix.db.ExpansionIterator;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.db.RelationshipWithSelect;
import matrix.db.Signature;
import matrix.db.SignatureList;
import matrix.db.State;
import matrix.db.StateList;
import matrix.util.MatrixException;
import matrix.util.Pattern;
import matrix.util.SelectList;
import matrix.util.StringItr;
import matrix.util.StringList;
import com.matrixone.apps.domain.util.mxFtp;

import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.common.Person;
import com.matrixone.apps.common.Route;
import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.DebugUtil;
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
import com.matrixone.apps.domain.util.mxBus;
import com.matrixone.apps.domain.util.mxType;
import com.matrixone.apps.engineering.Change;
import com.matrixone.apps.engineering.EBOMMarkup;
import com.matrixone.apps.engineering.ECO;
import com.matrixone.apps.engineering.ECR;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.engineering.Part;
import com.matrixone.apps.engineering.RelToRelUtil;
import com.matrixone.apps.framework.ui.RenderPDF;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.jsystem.util.StringUtils;
import com.dassault_systemes.enovia.bom.ReleasePhase;

/**
 * The <code>emxECOBase</code> class contains implementation code for emxECO.
 *
 * @version EC Rossini - Copyright (c) 2002, MatrixOne, Inc.
 */
public class emxECOBase_mxJPO extends enoEngChange_mxJPO
{


    /** state "Approved" for the "EC Part" policy. */
    public static final String STATE_ECPART_APPROVED =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_EC_PART,
                                           "state_Approved");

    /** state "Release" for the "EC Part" policy. */
    public static final String STATE_ECPART_RELEASE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_EC_PART,
                                           "state_Release");


    /** state "Obsolete" for the "EC Part" policy. */
    public static final String STATE_ECPART_OBSOLETE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_EC_PART,
                                           "state_Obsolete");

    /** policy "CAD Drawing" */
    public static final String POLICY_CAD_DRAWING =
            PropertyUtil.getSchemaProperty("policy_CADDrawing");

    /** state "Approved" for the "CAD Drawing" policy. */
    public static final String STATE_CADDRAWING_APPROVED =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_CAD_DRAWING,
                                           "state_Approved");

    /** state "Release" for the "CAD Drawing" policy. */
    public static final String STATE_CADDRAWING_RELEASE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_CAD_DRAWING,
                                           "state_Release");

    /** policy "CAD Model" */
    public static final String POLICY_CAD_MODEL =
            PropertyUtil.getSchemaProperty("policy_CADModel");

    /** state "Approved" for the "CAD Model" policy. */
    public static final String STATE_CADMODEL_APPROVED =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_CAD_MODEL,
                                           "state_Approved");

    /** state "Release" for the "CAD Model" policy. */
    public static final String STATE_CADMODEL_RELEASE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_CAD_MODEL,
                                           "state_Release");

    /** state "Approved" for the "Drawing Print" policy. */
    public static final String STATE_DRAWINGPRINT_APPROVED =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_DRAWINGPRINT,
                                           "state_Approved");

    /** state "Release" for the "Drawing Print" policy. */
    public static final String STATE_DRAWINGPRINT_RELEASE =
            PropertyUtil.getSchemaProperty("policy",
                                           POLICY_DRAWINGPRINT,
                                           "state_Release");



public static final String STATE_ECO_CANCELLED = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Cancelled");
/** state "Define Components" for the "ECO" policy. */
public static final String STATE_ECO_DEFINE_COMPONENTS = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_DefineComponents");

/** state "Design Work" for the "ECO" policy. */
public static final String STATE_ECO_DESIGN_WORK = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_DesignWork");

/** state "Create" for the "ECO" policy. */
public static final String STATE_ECO_CREATE = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Create");

/** state "Review" for the "ECO" policy. */
public static final String STATE_ECO_REVIEW = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Review");

public static final String STATE_ECO_RELEASE = PropertyUtil.getSchemaProperty("policy", POLICY_ECO, "state_Release");

   /** Relationship "ECO Change Request Input". */
    public static final String RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT =
    PropertyUtil.getSchemaProperty("relationship_ECOChangeRequestInput");

     //Relationship Affected Item.
	public static final String RELATIONSHIP_AFFECTED_ITEM =
            PropertyUtil.getSchemaProperty("relationship_AffectedItem");

	public static final String RESOURCE_BUNDLE_EC_STR =
	"emxEngineeringCentralStringResource";

	public static final String SELECT_RELATIONSHIP_DESIGN_RESPONSIBILITY = "to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id";
	public static final String SELECT_ATTRIBUTE_REQUESTED_CHANGE = "attribute[" + ATTRIBUTE_REQUESTED_CHANGE + "]";
	public static final String TYPE_EBOM_MARKUP = PropertyUtil.getSchemaProperty("type_EBOMMarkup");

    static final int LT = 0;
    static final int GT = 1;
    static final int EQ = 2;
    static final int LE = 3;
    static final int GE = 4;
    static final int NE = 5;

    //Added for the fix 366148
    public static String strClear="";
    //366148 fix ends
	 /** policy "ECR" */
	public static final String POLICY_ECR = PropertyUtil.getSchemaProperty("policy_ECR");

	  /** Person Admin Person */
  String personAdminType = "person";

  /** name of preference properties */
  String PREFERENCE_ENC_DEFAULT_VAULT = "preference_ENCDefaultVault";

  /** name of preference properties */
  String PREFERENCE_DESIGN_RESPONSIBILITY = "preference_DesignResponsibility";

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since EC Rossini.
     */
    public emxECOBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
        //DebugUtil.setDebug(true);
        //Added for the fix 366148
        strClear =EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Clear",context.getSession().getLanguage());
        //366148 fix ends
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return an int.
     * @throws Exception if the operation fails.
     * @since EC Rossini.
     */
    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            throw new Exception("must specify method on emxECO invocation");
        }
        return 0;
    }

    /**
     * This method checks the current state of the  Change Process and returns true if the ChanageObject is not in Cancelled state else returns false
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds String array as arguments
     * @return Boolean.
     * @throws Exception if the operation fails.
     *
     */
	public static Boolean isECOinCancelledState(Context context,String[] args) throws Exception
    {
        Boolean bflag=Boolean.FALSE;
        HashMap programMap= (HashMap)JPO.unpackArgs(args);
        String sobjectId=(String)programMap.get("objectId");

        DomainObject sdo=new DomainObject(sobjectId);
        String sCurrentState=sdo.getInfo(context,DomainConstants.SELECT_CURRENT);

        if(!STATE_ECO_CANCELLED.equalsIgnoreCase(sCurrentState))
        {
            bflag=Boolean.TRUE;
        }

        return bflag;
    }

    /**
    * Check the current state of the object with the target state, using the comparison operator
    * and returns the result.
    * @return an int value.
    *               0 if object state logic satisfies Comparison Operator.
    *               1 if object state logic didn't satisfies Comparison Operator.
    *               2 if a program error is encountered
    *               3 if state in state argument does not exist in the policy
    *               4 if an invalid comparison operator is passed in
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param id  String representing the id of the object whose state to be checked.
    * @param targetState String representing the target state against which the current state of the object is compared.
    * @param comparisonOperator int representing the operator used for comparison LT, GT, EQ, LE, GE, NE.
    * @since EC 10.0.0.0.
    */
    int checkObjState(matrix.db.Context context, String id, String targetState, int comparisonOperator)
    {
        try
        {
        	String sResult = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 dump $4",id,"current","state","|");

            //Fix for the Bug#348894
            StringList stateList = FrameworkUtil.split(sResult, "|");

            String currentState = (String)stateList.get(0);
            stateList.remove(0);

            return checkObjState(context, stateList, currentState, targetState, comparisonOperator);
            //End Bug#348894
        } catch (Exception ex) {
            // program error return
            return 2;
        }
    }

    /**
    * Check the current state of the object with the target state, using the comparison operator
    * and returns the result.  This is a performance enhanced version since we are passing in the
    * list of all states and current states of the object.
    * @return an int value.
    *               0 if object state logic satisfies Comparison Operator.
    *               1 if object state logic didn't satisfies Comparison Operator.
    *               2 if a program error is encountered
    *               3 if state in state argument does not exist in the policy
    *               4 if an invalid comparison operator is passed in
    *
    * @param context The eMatrix <code>Context</code> object.
    * @param allStates A StringList of all the states for this objects policy.
    * @param currentState String representing the current state of the object.
    * @param targetState String representing the target state against which the current state of the object is compared.
    * @param comparisonOperator int representing the operator used for comparison LT, GT, EQ, LE, GE, NE.
    * @since EC 10.6.
    */
    int checkObjState(matrix.db.Context context, StringList allStates, String currentState, String targetState, int comparisonOperator)
    {
        try
        {
            // get the index of target state
            int targetIndex = allStates.lastIndexOf(targetState);

            // get the index of current state
            int stateIndex  = allStates.lastIndexOf(currentState);

            // if the target state doesn't exist in policy then break
            if (targetIndex < 0)
            {
                return 3; // State doesn't exist in the policy
            }

            // check Target State index with object Current state index
            switch (comparisonOperator)
            {
                case LT :
                    if ( stateIndex < targetIndex )
                    {
                        return 0;
                    }
                    break;

                case GT :
                    if ( stateIndex > targetIndex )
                    {
                        return 0;
                    }
                    break;

                case EQ :
                    if ( stateIndex == targetIndex )
                    {
                         return 0;
                    }
                    break;

                case LE :
                    if ( stateIndex <= targetIndex )
                    {
                         return 0;
                    }
                    break;

                case GE :
                    if ( stateIndex >= targetIndex )
                    {
                         return 0;
                    }
                    break;

                case NE :
                    if ( stateIndex != targetIndex )
                    {
                        return 0;
                    }
                    break;

                default :
                    return 4;

            }
            return 1;
        } catch (Exception ex) {
            // program error return
            return 2;
        }
    }

    /**
    * Check whether all the signatures from current state to given state are satisfied (approved/ignored).
    *
    * @return boolean value
    *         true - if all the signature until needed state are satisfied (approved/ignored).
    *         false -  if any of the signature before needed state is not satisfied (approved/ignored).
    * @param context the eMatrix <code>Context</code> object.
    * @param id  String representing the id of object whose signatures to be checked.
    * @param checkState String representing the given state upto which signatures are to be checked.
    * @since EC 10.0.0.0.
    */
    boolean IsSignatureApprovedUptoGivenState(matrix.db.Context context, String id, String checkState)
    {
        /* this method need to be modified if branching is there, it is designed for policy without branching */
        try
        {
            DomainObject    dobjId = new DomainObject();
            dobjId.setId(id);

            State thisState   = null;
            String thisName   = null;
            State nextState   = null;
            boolean flag      = true;
            boolean flagStart = false;

            StateList stateList = dobjId.getStates(context);
            Iterator stateItr   = stateList.iterator();
            SignatureList sigList = null;
            Iterator sigItr     = null;
            Signature sig       = null;

            if (stateItr.hasNext())
            {
              // get the first state in the policy
              thisState = (State)stateItr.next();
              thisName = thisState.getName();
            }

            while (stateItr.hasNext())
            {
                // if signature not satisfied in one of the state before
                // the check state then break the loop
                if (!flag )
                {
                    break;
                }

                // we need to check only upto check state, so break from the loop
                if ( thisName.equals(checkState) )
                {
                    break;
                }

                // we need to start checking only after current state
                if ( !flagStart && thisState.isCurrent() )
                {
                    flagStart = true;
                }

                // get the next state in the policy
                nextState = (State)stateItr.next();

                // If the state is after current state, but before check state, then check
                // whether the signatures between current & next states are signed
                if ( flagStart )
                {
                    sigList= dobjId.getSignatures(context, thisState, nextState);
                    sigItr   = sigList.iterator();
                    while (sigItr.hasNext())
                    {
                        sig = (Signature)sigItr.next();
                        // if no approve or ignore signature then break from the loop
                        if (!sig.isApproved() && !sig.isIgnored())
                        {
                            flag = false;
                            break;
                        }
                    }
                }
                // move forward this state to next state
                thisState = nextState;
                thisName  = nextState.getName();
            }
            return (flag);
        }
        catch (Exception ex)
        {
            DebugUtil.debug("IsSignatureApprovedUptoGivenState Exception :" + ex);
            return false;
        }
    }

    /**
    * Check whether all the signatures from current state to given state are satisfied (approved/ignored).
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param id  the id of object whose signatures to be checked.
    * @param stateList StringList of all states for the objects policy.
    * @param currentState String representing the current state of the object.
    * @param checkState  the given state upto which signatures are to be checked.
    * @return true - if all the signature until needed state are satisfied (approved/ignored).
    *         false -  if any of the signature before needed state is not satisfied (approved/ignored).
    * @since EC 10.6.
    */
    boolean IsSignatureApprovedUptoGivenState(matrix.db.Context context, String id, StringList stateList, String currentState, String checkState)
    {
        /* this method need to be modified if branching is there, it is designed for policy without branching */
        try
        {
            DomainObject    dobjId = new DomainObject();
            dobjId.setId(id);

            String thisState   = null;
            String nextState   = null;
            boolean flag      = true;
            boolean flagStart = false;

            Iterator stateItr   = stateList.iterator();
            SignatureList sigList = null;
            Iterator sigItr     = null;
            Signature sig       = null;

            if (stateItr.hasNext())
            {
              // get the first state in the policy
              thisState = (String)stateItr.next();
            }

            while (stateItr.hasNext())
            {
                // if signature not satisfied in one of the state before
                // the check state then break the loop
                if (!flag )
                {
                    break;
                }

                // we need to check only upto check state, so break from the loop
                if ( thisState.equals(checkState) )
                {
                    break;
                }

                // we need to start checking only after current state
                if ( !flagStart && thisState.equals(currentState) )
                {
                    flagStart = true;
                }

                // get the next state in the policy
                nextState = (String)stateItr.next();

                // If the state is after current state, but before check state, then check
                // whether the signatures between current & next states are signed
                if ( flagStart )
                {
                    sigList= dobjId.getSignatures(context, thisState, nextState);
                    sigItr   = sigList.iterator();
                    while (sigItr.hasNext())
                    {
                        sig = (Signature)sigItr.next();
                        // if no approve or ignore signature then break from the loop
                        if (!sig.isApproved() && !sig.isIgnored())
                        {
                            flag = false;
                            break;
                        }
                    }
                }
                // move forward this state to next state
                thisState = nextState;
            }
            return (flag);
        }
        catch (Exception ex)
        {
            DebugUtil.debug("IsSignatureApprovedUptoGivenState Exception :" + ex);
            return false;
        }
    }

    /**
    * Utility method used to frame the where clause to get the objects below the given state.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param policyName String representing the policy to be checked.
    * @param stateName String representing the state to be checked.
    * @return String the where clause.
    * @throws Exception if the operation fails.
    * @since EC 10.0.0.0.
    */
    protected String formWhereQueryBelowState(matrix.db.Context context, String policyName, String stateName, String excludeState) throws Exception
    {
        String whereStmt = null;

        // get the states in the given policy
        String sResult = MqlUtil.mqlCommand(context, "print policy $1 select $2 dump $3",policyName,"state","|");

        // frame the where clause to get the objects below the given state
        sResult = sResult.substring(sResult.indexOf(stateName));
        StringTokenizer tokens = new StringTokenizer(sResult, "|");
        
        String nextState;
        if (tokens.hasMoreTokens())
        {
        	nextState = tokens.nextToken();
        	if(!nextState.equalsIgnoreCase(excludeState))
        		whereStmt = "current != \"" + nextState + "\"";
        }
        while (tokens.hasMoreTokens())
        {
        	nextState = tokens.nextToken();
        	if(!nextState.equalsIgnoreCase(excludeState))
        		whereStmt += " && current != \"" + nextState + "\"";
        }
        return(whereStmt);
    }

    /**
    * When the last changed item associated with the ECO reaches the pre-release state ("Approved"),
    * then the ECO needs to be automatically promoted to the "Review" state.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @return void.
    * @throws Exception if the operation fails.
    * @since EC 10.0.0.0.
    */
    public void autoPromoteECOToReviewState(matrix.db.Context context, String[] args) throws Exception
    {
        String warning      = null;
        try
        {
            MapList ecoList     = null;
            Iterator ecoItr     = null;
            String ecoId        = null;
            Map map             = null;
            DomainObject ECO    = new DomainObject();
            String relPattern   = RELATIONSHIP_AFFECTED_ITEM;

            MapList objList     = null;
            Iterator objItr     = null;
            String objId        = null;
            boolean bBelowApprovedState = false;

            // Get the list of ECO's to which this object is connected
            StringList objectSelects = new StringList(1);
            objectSelects.addElement(SELECT_ID);

            // Based on the type use the relationship filter that are applicable alone
            // get the list of ECO's connected to this object
            ecoList = getRelatedObjects( context,
                                         relPattern,
                                         TYPE_ECO,
                                         objectSelects,
                                         null,
                                         true,
                                         false,
                                         (short)1,
                                         null,
                                         null);
            ecoItr  = ecoList.iterator();

            // get all the objects connected to ECO except this object, with above relationship
            String whereStmt    = "id != \"" + getId() + "\"";

            objectSelects = new StringList(3);
            objectSelects.addElement(SELECT_ID);
            objectSelects.addElement(SELECT_TYPE);
            objectSelects.addElement(SELECT_POLICY);

            // Iterate through each of the ECO, if all the affected item of the ECO is in pre-release state,
            // then promote the ECO to "Review" state
            String chkState = null;
            boolean bSignature = true;
            while (ecoItr.hasNext())
            {
                map = (Map)ecoItr.next();
                ecoId = (String)map.get(SELECT_ID);

                // auto promote  to be done only if the ECO current state is below "Review"
                // state, the ECO will be promoted upto "Review" state
                if ( checkObjState(context, ecoId, STATE_ECO_REVIEW, LE) != 0 ) {
                    // ECO is already beyond "Review" state,so no need of auto promote to "Review" state
                    continue;
                }

                ECO.setId(ecoId);
                objList = ECO.getRelatedObjects( context,
                                                 relPattern,
                                                 "*",
                                                 objectSelects,
                                                 null,
                                                 false,
                                                 true,
                                                 (short)1,
                                                 whereStmt,
                                                 null);

                // check whether last affected item is getting promoted to "Approved" state
                objItr =  objList.iterator();
                bBelowApprovedState = false;
                while (objItr.hasNext())
                {
                    map = (Map)objItr.next();
                    objId = (String)map.get(SELECT_ID);

                    chkState = PropertyUtil.getSchemaProperty(context,"policy", (String)map.get(SELECT_POLICY), "state_Approved");

                    // if atleast one Part/Spec is below "Approved" state, other than this Part/Spec,
                    // then this Part/Spec is not the last affected item, exit from the while loop
                    if (checkObjState(context, objId, chkState, LT) == 0)
                    {
                        bBelowApprovedState = true;
                        break;
                    }
                }


                // if this object is the last affected item going to pre release state,
                // then promote the ECO to "Review" state
                bSignature = true;
                if ( !bBelowApprovedState ) {
                    try
                    {
                        ECO.open(context);
                        // Need to check whether all the signatures from Current state to "Review" are signed,
                        // so that the ECO can be promoted to "Review" state
                       // bSignature = IsSignatureApprovedUptoGivenState(context, ecoId, STATE_ECOSTANDARD_REVIEW);
                        bSignature = IsSignatureApprovedUptoGivenState(context, ecoId, STATE_ECO_REVIEW);
                        if (bSignature)
                        {
                            // need to promote the ECO to "Review" state as super user, if all the signatures
                            // are approved.
                            try
                            {
                                ContextUtil.pushContext(context);
                                ECO.setState(context, STATE_ECO_REVIEW);
                            }
                            catch ( Exception e)
                            {
                                // no need to throw any exception if ECO not promoteable,
                                // b'coz of promote constraint.
                                if ( warning == null)
                                {
                                  warning = " " + ECO.getInfo(context, SELECT_NAME) + " " + ECO.getInfo(context, SELECT_REVISION);
                                }
                                else
                                {
                                  warning += ", " + ECO.getInfo(context, SELECT_NAME) + " " + ECO.getInfo(context, SELECT_REVISION);
                                }
                            }
                            finally
                            {
                                ContextUtil.popContext(context);
                            }
                        }
                    }
                    catch (Exception ex)
                    {
                        bSignature = false;
                    }
                    finally
                    {
                        // if the ECO not promotable b'coz of signature or other constraint then
                        // add it for user warning
                        if ( !bSignature)
                        {
                            if ( warning == null)
                            {
                              warning = " " + ECO.getInfo(context, SELECT_NAME) + " " + ECO.getInfo(context, SELECT_REVISION);
                            }
                            else
                            {
                              warning += ", " + ECO.getInfo(context, SELECT_NAME) + " " + ECO.getInfo(context, SELECT_REVISION);
                            }
                        }
                        ECO.close(context);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            DebugUtil.debug("autoPromoteECOToReviewState Exception :", ex.toString());
            throw ex;
        }
        finally
        {
            // if ECO not promoteable to "Review", issue warning to user the list of ECO not promotable
            if (warning != null)
            {
                emxContextUtil_mxJPO.mqlNotice(context,
                    EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ChangeManagement.AutoCheckChangedItemToCompleteStateWarning",
                    context.getSession().getLanguage()) + warning);
            }
        }
    }


    /**
    * When the affected item being promoted to "Release" state, the ECO associated with it must
    * have reached its "Release" state, otherwise affected item should not be promoted
    * to the "Release" state.
    * @return int value.
    *         0-success if the ECO's attached to the affected item is beyond "Release" state.
    *         1-failure if any of the ECO attached to the affected item is below "Release" state.
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds no arguments.
    * @throws Exception if the operation fails.
    * @since EC 10.0.0.0.
    */
    public int autoCheckChangedItemToReleaseState(matrix.db.Context context, String[] args) throws Exception
    {
        // Get the RPE variable MX_SKIP_PART_PROMOTE_CHECK, if it is not null and equal to "true"
        // it indicates that object is getting promoted because of ECO promotion to "release" state
        // in this case, no need to do the checks specified in this trigger logic, skip it.
        // In other words, when ECO gets promoted to Release state all the connected items get promoted, these can be many objects
        // this check trigger gets fired for each of these objects being promoted, which is not needed in this case.
        // This also results in performance improvment for ECO promote action
    	
    	//BGP: In case the Release Process is Development, Do not execute the trigger program functionality
    	String sPartId = getId(context);
    	if(ReleasePhase.isECPartWithDevMode(context, sPartId))
    		return 0;
        //BGP: In case the Release Process is Development, Do not execute the trigger program functionality
        
        String skipTriggerCheck = PropertyUtil.getRPEValue(context, "MX_SKIP_PART_PROMOTE_CHECK", false);
        if(skipTriggerCheck != null && "true".equals(skipTriggerCheck))
        {
            return 0;
        }
        String exclusionTypes = args[0];
        StringList splitList   = FrameworkUtil.split(exclusionTypes,",");
        StringList exclusionList = new StringList(splitList.size());
        for(int i=0;i < splitList.size();i++)
        {
                String curType   = PropertyUtil.getSchemaProperty(context,splitList.get(i).toString());
                StringList typesList=new StringList();
                typesList=getSubTypes(context,curType);
                typesList.add(0,curType);
                for(int j=0;j<typesList.size();j++)
                {
                     curType=(String)typesList.get(j);
                     if(!exclusionList.contains(curType))
                    {
                            exclusionList.add(curType);
                    }
                }
        }
        String sUnReleasedList = null;
        String objectType= null;
        try
        {
            MapList objList   = null;
            MapList proposedchangeActionList = null;
            MapList realizedchangeActionList = null;
            String relPattern = RELATIONSHIP_AFFECTED_ITEM;
            //String typePattern = ChangeConstants.TYPE_CHANGE_ACTION ;
            String typePattern = TYPE_ECO;
            // get the states which are before "Release" state in the "ECO Standard" policy
          // String whereStmt  = formWhereQueryBelowState(context, POLICY_ECO, STATE_ECO_RELEASE, null);
         //  whereStmt =  whereStmt + " && " + formWhereQueryBelowState(context, 
        		   														//ChangeConstants.TYPE_CHANGE_ACTION, 
    		   														//	PropertyUtil.getSchemaProperty(context, "policy", ChangeConstants.POLICY_CHANGE_ACTION , "state_Complete"),
    		   															//PropertyUtil.getSchemaProperty(context, "policy", ChangeConstants.POLICY_CHANGE_ACTION , "state_OnHold"));

            // Get the list of ECO's to which this object is connected
            StringList objectSelects = new StringList(3);
            objectSelects.addElement(SELECT_ID);
            objectSelects.addElement(SELECT_NAME);
            objectSelects.addElement(SELECT_REVISION);
            objectSelects.addElement(SELECT_CURRENT);
            objectType=getInfo(context, SELECT_TYPE);
            if(exclusionList.contains(objectType)){
              return 0;
            }

            // based on the type use the relationship filter that are applicable alone
           // relPattern = relPattern; 
            		//+ "," + ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM + "," + ChangeConstants.RELATIONSHIP_IMPLEMENTED_ITEM;
                   // typePattern = typePattern + "," + TYPE_ECO;
            // get the list of ECO's which are not in "Release" state, connected to this changed Item
            StringList relSelects = new StringList();
            try {
            Map proposedCAData  = com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil.getChangeObjectsInProposed(context, objectSelects, new String[]{sPartId}, 1);
			 proposedchangeActionList = (MapList)proposedCAData.get(sPartId);
			Map  realizedCAData = com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil.getChangeObjectsInRealized(context, objectSelects, new String[]{sPartId}, 1);
			 realizedchangeActionList = (MapList)realizedCAData.get(sPartId);
			String strCurrent;
			Iterator objItr2 = proposedchangeActionList.iterator();
			 Map map2 = null;
			Iterator objItr3 = realizedchangeActionList.iterator();
			 Map map3 = null;
			 boolean boolChangeCompleted = false;
			 while(objItr2.hasNext())
             {
             	map2 = (Map)objItr2.next();
             	strCurrent =(String)map2.get(SELECT_CURRENT);
             	if(strCurrent.equals(ChangeConstants.STATE_CHANGE_ACTION_COMPLETE)){
             		boolChangeCompleted = true;
             		break;
             	}
             }
			 while(objItr3.hasNext())
             {
             	map3 = (Map)objItr3.next();
             	strCurrent =(String)map3.get(SELECT_CURRENT);
             	if(strCurrent.equals(ChangeConstants.STATE_CHANGE_ACTION_COMPLETE)){
             		boolChangeCompleted = true;
             		break;
             	}
             }
            ContextUtil.startTransaction(context, true);
           
            		objList = FrameworkUtil.toMapList(getExpansionIterator(context, relPattern, typePattern,
                    objectSelects, relSelects, true, false, (short)1,
                    null, null, (short)0,
                    false, false, (short)0, false),
                    (short)0, null, null, null, null);

                    Map map1 = null;
                    String current;
                    Iterator objItr1  = objList.iterator();
                	
                    while(objItr1.hasNext())
                    {
                    	map1 = (Map)objItr1.next();
                    	current =(String)map1.get(SELECT_CURRENT);
                    	if(current.equals(ChangeConstants.STATE_CHANGE_ACTION_COMPLETE) || current.equals(EngineeringConstants.STATE_ECO_RELEASE) || current.equals(EngineeringConstants.STATE_ECO_IMPLEMENTED)){
                    		boolChangeCompleted = true;
                    		break;
                    	}
                    }
                    if (boolChangeCompleted) { return 0; }    
            } catch (FrameworkException fe) {
         	   ContextUtil.abortTransaction(context);
         	   throw fe;
            }
            ContextUtil.commitTransaction(context);



            // Changed item cannot be promoted to "Release " state
            Iterator objItr  = objList.iterator();
            Map map = null;

            if (objItr.hasNext())
            {
                map = (Map)objItr.next();
                sUnReleasedList = " " + (String)map.get(SELECT_NAME) + " " + (String)map.get(SELECT_REVISION);
            }

            while (objItr.hasNext())
            {
                map = (Map)objItr.next();
                sUnReleasedList += ", " + (String)map.get(SELECT_NAME) + " " + (String)map.get(SELECT_REVISION);
            }
            Iterator objItr1  = proposedchangeActionList.iterator();
            Map map1 = null;
            if (objItr1.hasNext())
            {
                map1 = (Map)objItr1.next();
                sUnReleasedList = " " + (String)map1.get(SELECT_NAME) + " " + (String)map1.get(SELECT_REVISION);
            }

            while (objItr1.hasNext())
            {
            	map1 = (Map)objItr1.next();
                sUnReleasedList = ", " + (String)map1.get(SELECT_NAME) + " " + (String)map1.get(SELECT_REVISION);
            }
            Iterator objItr2  = realizedchangeActionList.iterator();
            Map map2 = null;
            if (objItr2.hasNext())
            {
                map2 = (Map)objItr2.next();
                sUnReleasedList = " " + (String)map2.get(SELECT_NAME) + " " + (String)map2.get(SELECT_REVISION);
            }

            while (objItr2.hasNext())
            {
            	map2 = (Map)objItr2.next();
                sUnReleasedList = ", " + (String)map2.get(SELECT_NAME) + " " + (String)map2.get(SELECT_REVISION);
            }
            if ((objList == null || objList.size() < 1) && UIUtil.isNullOrEmpty(sUnReleasedList))
            {
                // Changed item can be promoted to "Release " state
                return 0;
            }
            
            emxContextUtil_mxJPO.mqlNotice(context,
                EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.AutoCheckChangedItemToReleaseStateWarning",
                context.getSession().getLanguage()) + sUnReleasedList);

            return 1;
        }
        catch (Exception ex)
        {
            DebugUtil.debug("autoCheckChangedItemToReleaseState Exception :", ex.toString());
            throw ex;
        }
    }

  /**
    * Gets the Affected Items for Cancel ECO.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds a HashMap containing the following entries:
    * objectId - a String holding object id.
    * @return MapList of ECO Affected Items.
    * @throws Exception if the operation fails.
    * @since EC 10.0.0.0.
    */
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getECOAffectedItems(Context context,String[] args)
         throws Exception
   {
    HashMap paramMap = (HashMap)JPO.unpackArgs(args);
       String objectId = (String) paramMap.get("objectId");
       MapList affectedItems = new MapList();

       String relPattern = RELATIONSHIP_AFFECTED_ITEM;

     ContextUtil.startTransaction(context, true);
       try
       {
           ECO ecoObj = new ECO(objectId);
           StringList selectStmts = new StringList(1);
           selectStmts.addElement(SELECT_ID);

           StringList selectRelStmts = new StringList(1);
           selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

           affectedItems = FrameworkUtil.toMapList(ecoObj.getExpansionIterator(context, relPattern, DomainConstants.QUERY_WILDCARD,
                   selectStmts, selectRelStmts, false, true, (short)1,
                   null, null, (short)0,
                   false, false, (short)0, false),
                   (short)0, null, null, null, null);
       }

       catch (FrameworkException Ex)
       {
    	   ContextUtil.abortTransaction(context);
            throw Ex;
       }
       ContextUtil.commitTransaction(context);

       return affectedItems;
   }

    /**
    * Checks if the given attribute is available in the supplied type and is visible.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param type a String which holds type of the object.
    * @param attribute a String which holds the name of the attribute.
    * @return boolean true/false.
    * @throws Exception if the operation fails.
    * @since 10.5.
    */
   public boolean isAttributeOnTypeAndNonHidden(Context context, String type, String attribute) throws Exception {
    return ((FrameworkUtil.isAttributeOnType(context, type, attribute)) &&
      !(FrameworkUtil.isAttributeHidden(context, attribute)));
   }

   /**
    * Checks if the given resource id is set to true or false.
    *
    * @param resourceID String which holds the property value to check.
    * @return Boolean containing the resource value.
    * @throws Exception if the operation fails.
    * @since 10.5.
    */
    private Boolean emxCheckAccess(Context context, String resoureID)
        throws Exception
   {
       String resourceIDValue = FrameworkProperties.getProperty(context,resoureID);
    return Boolean.valueOf(resourceIDValue);
   }

  /**
   * Checks the Routes section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds a HashMap containing the following entries:
   * objectId - a String holding the ECO id.
   * @return Boolean determines whether BOM Comparison is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public Boolean emxCheckRoutesAccess(Context context, String[] args)
        throws Exception
   {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        Boolean emxAccess = Boolean.TRUE;
        String sattrIsVersionObject  = PropertyUtil.getSchemaProperty(context,"attribute_IsVersionObject");
        String strAttrIsVerObj = null;
    String objectId =  (String) paramMap.get("objectId");
    setId(objectId);

    if(isKindOf(context, DomainConstants.TYPE_ECR)){
      emxAccess = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.Routes");
    }
    else if(isKindOf(context, DomainConstants.TYPE_ECO)){
      emxAccess = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.Routes");
    }
	else{
		strAttrIsVerObj = getInfo(context,"attribute["+sattrIsVersionObject+"]");
		strAttrIsVerObj = (strAttrIsVerObj == null)?"":strAttrIsVerObj;
		if ("True".equals(strAttrIsVerObj)){
			emxAccess = Boolean.FALSE;
		}else{
			emxAccess = Boolean.TRUE;
		}
	}
    return emxAccess;
   }

  /**
   * Checks the Approvals section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds a HashMap containing the following entries:
   * objectId - a String holding the ECO id.
   * @return Boolean determines whether BOM Comparison is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public Boolean emxCheckApprovalsAccess(Context context, String[] args)
        throws Exception
   {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        Boolean emxAccess = Boolean.TRUE;

    String objectId =  (String) paramMap.get("objectId");
    setId(objectId);

    if(isKindOf(context, DomainConstants.TYPE_ECR)){
      emxAccess = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.Approvals");
    }
    else if(isKindOf(context, DomainConstants.TYPE_ECO)){
      emxAccess = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.Approvals");
    }
    return emxAccess;
   }

  /**
   * Checks the Route Instructions section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds a HashMap containing the following entries:
   * objectId - a String holding the ECO id.
   * @return Boolean determines whether Route Instructions section is accessable.
   * @throws Exception if the operation fails
   * @since 10.5.
   */
   public Boolean emxCheckRouteInstructionsAccess(Context context, String[] args)
        throws Exception
   {
        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        Boolean emxAccess = Boolean.TRUE;

    String objectId =  (String) paramMap.get("objectId");
    setId(objectId);

    if(isKindOf(context, DomainConstants.TYPE_ECR)){
      emxAccess = emxCheckAccess(context, "emxEngineeringCentral.ECRSummary.RouteInstructions");
    }
    else if(isKindOf(context, DomainConstants.TYPE_ECO)){
      emxAccess = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.RouteInstructions");
    }
    return emxAccess;
   }

  /**
   * Checks the Obsolete Parts section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds no arguments.
   * @return Boolean determines whether Obsolete Parts section is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public Boolean emxCheckObsoletePartsAccess(Context context, String[] args)
        throws Exception
   {
    return emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.ObsoleteParts");
   }

   /**
   * Checks the Revised Parts section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds no arguments.
   * @return Boolean determines whether Revised Parts section is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public Boolean emxCheckRevisedPartsAccess(Context context, String[] args)
        throws Exception
   {
    return emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.RevisedParts");
   }

  /**
   * Checks the New Parts section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds no arguments.
   * @return Boolean determines whether New Parts section is accessable.
   * @throws Exception if the operation fails.
   * @since 105.
   */
   public Boolean emxCheckNewPartsAccess(Context context, String[] args)
        throws Exception
   {
    return emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.NewParts");
   }

  /**
   * Checks the Related ECRs section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds no arguments.
   * @return Boolean determines whether Related ECRs section is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public Boolean emxCheckRelatedECRsAccess(Context context, String[] args)
        throws Exception
   {
    return emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.RelatedECRs");
   }

  /**
   * Checks the New Specs section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds no arguments.
   * @return Boolean determines whether New Specs section is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public Boolean emxCheckNewSpecsAccess(Context context, String[] args)
        throws Exception
   {
    return emxCheckAccess(context, "emxEngineeringCentral.ECOSummiary.NewSpecs");
   }

  /**
   * Checks the Revised Specs section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds no arguments.
   * @return Boolean determines whether Revised Specs section is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public Boolean emxCheckRevisedSpecsAccess(Context context, String[] args)
        throws Exception
   {
    return emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.RevisedSpecs");
   }

  /**
   * Checks the BOM Comparison section Access.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds no arguments.
   * @return Boolean determines whether BOM Comparison is accessable.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public Boolean emxCheckBOMComparisonAccess(Context context, String[] args)
        throws Exception
   {
    return emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.NetBOMComparison");
   }

   /**
     * Constructs the HTML table of the Approvals related to this ECO.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @return String Related approvals in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since 10.5.
    */

   public  String getApprovals(Context context,String args[]) throws Exception {

    if (args == null || args.length < 1) {
          throw (new IllegalArgumentException());
    }

    Route routeObj = (Route)DomainObject.newInstance(context,TYPE_ROUTE);
    String languageStr = context.getSession().getLanguage();
    String objectId = args[0];

    String sLifeCycle         = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Route.LifeCycle",languageStr);
    boolean bRouteSize         =false;
    boolean bSign              =false;

  setId(objectId);

  String sPolicy            = getInfo(context, SELECT_POLICY);
    MapList memberList        = new MapList();

    // get a MapList of all the approval data including routes
  MapList stateRouteList = getApprovalsInfo(context);

    SelectList objSelects  = new SelectList();
    objSelects.addElement(DomainConstants.SELECT_NAME);
    SelectList relSelects  = new SelectList();
    relSelects.addElement(Route.SELECT_COMMENTS);
    relSelects.addElement(Route.SELECT_APPROVAL_STATUS);
    relSelects.addElement(Route.SELECT_APPROVERS_RESPONSIBILITY);
    relSelects.addElement(Route.SELECT_ROUTE_TASK_USER);

    StringBuffer returnString=new StringBuffer(512);
    returnString.append(" <table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
    returnString.append("<tr><th>");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",languageStr));
    returnString.append("</th> <th>");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Route",languageStr));
    returnString.append("</th> <th>");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Signature",languageStr));
    returnString.append("</th><th>");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Signer",languageStr));
    returnString.append("</th> <th>");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Status",languageStr));
    returnString.append("</th> <th>");
    returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Comments",languageStr));
    returnString.append("</th> </tr>");

  Iterator mapItr = stateRouteList.iterator();
    while(mapItr.hasNext())
    {
    Map stateRouteMap = (Map)mapItr.next();
      StringItr sSignatureItr = null;
      StringItr sSignersItr   = null;
      StringItr sCommentItr   = null;
      StringItr sStatusItr    = null;
      boolean hasSigs = false;
    boolean hasRoutes = false;

      // Check for State Name and Ad Hoc routes
      String sStateName = (String)stateRouteMap.get(SELECT_NAME);

    if (sStateName != null) {
        sSignatureItr = new StringItr((StringList)stateRouteMap.get(KEY_SIGNATURE));
        sSignersItr   = new StringItr((StringList)stateRouteMap.get(KEY_SIGNER));
        sCommentItr   = new StringItr((StringList)stateRouteMap.get(KEY_COMMENTS));
        sStatusItr    = new StringItr((StringList)stateRouteMap.get(KEY_STATUS));
        hasSigs = sSignatureItr.next();
        sSignatureItr.reset();
      }

     // Check for Routes
     Vector routes = new Vector();
    if (sStateName != null) {
            routes = (Vector)stateRouteMap.get(KEY_ROUTES);
        if((!routes.isEmpty()) && (!hasSigs)) {
           hasRoutes = true;
        }
    }

    if ("Ad Hoc Routes".equals(sStateName)) {
        hasSigs = false;
        sStateName = "Ad Hoc";
      }

      // Check for Routes
      routes = (Vector)stateRouteMap.get(KEY_ROUTES);
        if (hasSigs &&!hasRoutes) {

      bSign=true;

        returnString.append("<tr >");
        returnString.append("<td>"+i18nNow.getStateI18NString(sPolicy,sStateName.trim(),languageStr)+"</td> <td>"+sLifeCycle+"</td>");

      }

    String sSignStatus;
	boolean isFirst = true;
      if (sSignatureItr != null) {
          while (sSignatureItr.next()) {
          if (!isFirst)
          {
              returnString.append("<tr><td>&nbsp;</td><td>&nbsp;</td>");
          }
          isFirst = false;

            // get next data for each list
             sSignersItr.next();
             sStatusItr.next();
             sCommentItr.next();

             String sSignName = sSignatureItr.obj();
             String sSigner   = sSignersItr.obj();
             String sSignDesc = sCommentItr.obj();
             String status    = sStatusItr.obj();
             // Internationalize status
             if (status.equalsIgnoreCase("Approved")){
               sSignStatus = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Approved", languageStr);
             }
             else if (status.equalsIgnoreCase("Ignore")){
               sSignStatus = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Ignored", languageStr);
             }
             else if (status.equalsIgnoreCase("Rejected")){
               sSignStatus = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Rejected", languageStr);
             }
             else  if (status.equalsIgnoreCase("Signed")){
               sSignStatus = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Signed", languageStr);
             }
             else{
               sSignStatus = "&nbsp;";
             }

    if(sSigner != null && sSigner.length() > 0){
        sSigner= PersonUtil.getFullName(context, sSigner);
    }

           returnString.append("<td>"+sSignName+"&nbsp;</td>");
           returnString.append("<td>"+sSigner+"&nbsp;</td>");
           returnString.append("<td>");
           if((sSignStatus != null)&&(!sSignStatus.equalsIgnoreCase("null"))){
             returnString.append(sSignStatus);
           }
           returnString.append("&nbsp;</td>");
           returnString.append("<td>"+sSignDesc+"&nbsp;</td></tr>");

          }
        }

 if (!hasSigs && hasRoutes) {
    for (int rteCnt = 0; rteCnt < routes.size(); rteCnt++) {
           bRouteSize=true;
           String sRouteId = (String)routes.get(rteCnt);

           returnString.append("<tr >");

           if ((rteCnt == 0) && (!hasSigs)) {

             returnString.append("<td>"+sStateName+"</td>");

           }
           else {

             returnString.append("<td>&nbsp;</td>");

           }

       String sRouteName = "";
           String routeNodeResponsibility = "";
           String sPersonName             = "";
           String routeNodeStatus         = "";
           String routeNodeComments       = "";

           Hashtable memberMap = new Hashtable();
           if(sRouteId != null && !"null".equals(sRouteId) && !"".equals(sRouteId))
           {
              routeObj.setId(sRouteId);
              sRouteName = routeObj.getInfo(context, SELECT_NAME);
              memberList = routeObj.getRouteMembers(context, objSelects, relSelects, false);
           }
           returnString.append("<td>"+sRouteName+"</td>");

       for(int k = 0; k < memberList.size() ; k++)
           {
              memberMap = (Hashtable) memberList.get(k);
              routeNodeResponsibility = (String) memberMap.get(Route.SELECT_APPROVERS_RESPONSIBILITY);
              sPersonName             = (String) memberMap.get(Route.SELECT_ROUTE_TASK_USER);
              routeNodeStatus         = (String) memberMap.get(Route.SELECT_APPROVAL_STATUS);
              routeNodeComments       = (String) memberMap.get(Route.SELECT_COMMENTS);

              if(sPersonName == null || "null".equals(sPersonName) || "".equals(sPersonName)){
                sPersonName             = (String) memberMap.get(Route.SELECT_NAME);
              }
              else
              {
                sPersonName = " ";
              }

    sPersonName = PersonUtil.getFullName(context, sPersonName);
              if(k > 0)
              {

                returnString.append("<tr > <td>&nbsp;</td><td>&nbsp;</td>");  //last td replaced with style class

              }

              returnString.append("<td>"+routeNodeResponsibility+"&nbsp;</td>");
              returnString.append("<td>"+sPersonName+"&nbsp;</td>");
              returnString.append("<td>"+i18nNow.getRangeI18NString("", routeNodeStatus,languageStr)+"&nbsp;</td> <td>"+routeNodeComments+"&nbsp;</td></tr>");
           }
        }
      }
}
  if (!bRouteSize && !bSign)
    {
       returnString.append("<tr><td class=\"even\" colspan=\"3\" align=\"center\" > "+ EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.SummaryReport.NoSignOrRoutes", languageStr)+"</td></tr>");
    }
    returnString.append("</table>");
    String finalStr = returnString.toString();

  return finalStr;
  }

/**
     * Constructs the HTML table of the ECO Attributes.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @return String Attributes in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since 105.
    */

   public  String getBasicInfo(Context context,String args[]) throws Exception {

  if (args == null || args.length < 1) {
          throw (new IllegalArgumentException());
    }

  /* Arguments are not packed */
    String objectId = args[0];
    String strLanguage = context.getSession().getLanguage();
    String defaultVal = "Unassigned";
    String Unassigned = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Unassigned",strLanguage);

    StringBuffer returnString = new StringBuffer(2048);
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
    objectSelects.add(SELECT_VAULT);
    objectSelects.add(SELECT_POLICY);
    objectSelects.add(com.matrixone.apps.engineering.ECO.SELECT_PRIORITY);
    objectSelects.add(com.matrixone.apps.engineering.ECO.SELECT_RELEASE_DISTRIBUTION_GROUP);
    objectSelects.add(com.matrixone.apps.engineering.ECO.SELECT_RESPONSIBLE_DESIGN_ENGINEER);
    objectSelects.add("to[Design Responsibility].from.name");
    objectSelects.add(com.matrixone.apps.engineering.ECO.SELECT_RESPONSIBLE_MANUFACTURING_ENGINEER);
    objectSelects.add(com.matrixone.apps.engineering.ECO.SELECT_REASON_FOR_CANCEL);
    if(EngineeringUtil.isMBOMInstalled(context)){
    objectSelects.add(EngineeringConstants.SELECT_BYPASSPLANTS);
    }

    Map attributeMap = getInfo(context, objectSelects);

    String attrName  = null;
    String attrValue = null;
    String sCurrentState = (String)attributeMap.get(SELECT_CURRENT);
    String ecoDesc = (String)attributeMap.get(SELECT_DESCRIPTION);
    ecoDesc = FrameworkUtil.findAndReplace(ecoDesc,"\n","<br>");
  /* below html table contains two columns (tds). First contains basics and second contains ECO related and other attributes */
  returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
  returnString.append("<tr>");
  returnString.append("<td><table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+":</td><td class=\"inputField\">"+attributeMap.get(SELECT_NAME)+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+":</td><td class=\"inputField\">"+attributeMap.get(SELECT_TYPE)+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage)+":</td><td class=\"inputField\">"+i18nNow.getStateI18NString((String)attributeMap.get(SELECT_POLICY),(String)attributeMap.get(SELECT_CURRENT),strLanguage)+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+":</td><td class=\"inputField\">"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_OWNER)))+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Originator",strLanguage)+":</td><td class=\"inputField\">"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_ORIGINATOR)))+"</td></tr>");

  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Originated",strLanguage)+":</td><td class=\"inputField\">"+(String)attributeMap.get(SELECT_ORIGINATED)+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Modified",strLanguage)+":</td><td class=\"inputField\">"+(String)attributeMap.get(SELECT_MODIFIED)+"</td></tr>");

  returnString.append("</table></td>");


  returnString.append("<td><table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+":</td><td class=\"inputField\">"+ecoDesc+"&nbsp;</td></tr>");

  //Displaying the ECO specific attributes

    Map attrMap = getAttributeMap(context);
    Iterator ecoAttrListItr = attrMap.keySet().iterator();

    while (ecoAttrListItr.hasNext())
     {
        attrName = (String)ecoAttrListItr.next();
        attrValue = (String)attrMap.get(attrName);

        if((ECO.ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER).equals(attrName)||(ECO.ATTRIBUTE_RESPONSIBLE_MANUFACTURING_ENGINEER).equals(attrName)||(ECO.ATTRIBUTE_RELEASE_DISTRIBUTION_GROUP).equals(attrName) ) {
            attrValue = (attrValue.equals(defaultVal))?Unassigned:attrValue;
        }

        //don't display this attribute because it's already displayed
        if(!attrName.equals(ATTRIBUTE_ORIGINATOR) && !attrName.equals(com.matrixone.apps.engineering.ECO.ATTRIBUTE_REASON_FOR_CANCEL)
           || (attrName.equals(com.matrixone.apps.engineering.ECO.ATTRIBUTE_REASON_FOR_CANCEL) && sCurrentState.equals(STATE_ECO_CANCELLED))   )
        {
            if(attrName.equals(ECO.ATTRIBUTE_REASON_FOR_CANCEL)){
                attrValue = FrameworkUtil.findAndReplace(attrValue,"\n","<br>");

            }
            returnString.append("<tr>");
            returnString.append("<td class=\"label\">"+i18nNow.getAttributeI18NString(attrName, strLanguage)+":</td>");
            returnString.append("<td class=\"inputField\">"+i18nNow.getRangeI18NString(attrName, attrValue, strLanguage)+"</td>");
            returnString.append("</tr>");
        }
     }

  String sDesignResponsibility = (String)attributeMap.get("to[Design Responsibility].from.name");
  if(sDesignResponsibility == null || "null".equals(sDesignResponsibility)){
    sDesignResponsibility = "&nbsp;";
  }

  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.DesignResponsibility",strLanguage)+":</td><td class=\"inputField\">"+sDesignResponsibility+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Vault",strLanguage)+":</td><td class=\"inputField\">"+attributeMap.get(SELECT_VAULT)+"</td></tr>");
  returnString.append("</table></td>");
  returnString.append("</tr>");
  returnString.append("</table>");

  String finalStr = returnString.toString();
  return finalStr;
   }

   /**
     * Provides the style sheet information.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return String Style Sheet information.
     * @throws Exception if the operation fails.
     * @since 10.5.
    */

   public  String getStyleInfo(Context context,String args[]) throws Exception {

  StringBuffer htmlString = new StringBuffer(8192);
    htmlString.append("<html>\n<title>PDF - (Summary Report)</title>");
  //hard coded charse en.
    String Charset = FrameworkProperties.getProperty(context, "emxEngineeringCentral.Charset.en");
    if(Charset != null)
    {
    htmlString.append("<META HTTP-EQUIV=\"Content-type\" CONTENT=\"text/html;charset=");
    htmlString.append(Charset);
    htmlString.append("\">\n");
  }

    htmlString.append("<style type=\"text/css\" >");
    /* Background Appearance */
    htmlString.append("body { ");
    htmlString.append(" background-color: white; ");
    htmlString.append('}');

    /* Font Appearance */
    htmlString.append("body, th, td, p, div, layer { ");
    htmlString.append(" font-family: verdana, helvetica, arial, sans-serif; ");
    htmlString.append("font-size: 8pt; ");
    htmlString.append('}');

    /* Link Appearance */
    htmlString.append("a { ");
    htmlString.append(" color: #003366; ");
    htmlString.append('}');

    htmlString.append("a:hover { }");

    /* Object Link Appearance */
    htmlString.append("a.object{ ");
      htmlString.append("font-weight: bold; ");
    htmlString.append('}');

    htmlString.append("a.object:hover { }");

    /* Object Text (Non-link) Appearance */
    htmlString.append("span.object {  ");
    htmlString.append(" font-weight: bold; ");
    htmlString.append('}');

    /* Button Link Appearance */
    htmlString.append("a.button { }");
    htmlString.append("a.button:hover { }");

    /* Content-Specific Function Appearance */
    htmlString.append("a.contextual { }");
    htmlString.append("a.contextual:hover { }");

    /* Remove Button Appearance */
    htmlString.append("a.remove { }");
    htmlString.append("a.remove:hover { }");

    /* --------------------------------------------------------------------
    // Page Header Settings
    // -------------------------------------------------------------------- */

    /* Page Header Text Appearance */
    htmlString.append(".pageHeader {  ");
    htmlString.append(" color:#990000; ");
    htmlString.append(" font-family: Arial, Helvetica, Sans-Serif; ");
    htmlString.append(" font-weight: bold; ");
    htmlString.append(" font-size: 12pt; ");
    htmlString.append(" letter-spacing: 0pt; ");
    htmlString.append(" line-height: 22px; ");
    htmlString.append(" text-decoration: none;");
    htmlString.append('}');

    /* Page Subtitle Appearance */
    htmlString.append(".pageSubTitle {");
    htmlString.append(" color:#990000; ");
    htmlString.append(" font-family: Arial, Helvetica, Sans-Serif; ");
    htmlString.append(" font-size: 11px; ");
    htmlString.append("letter-spacing: 1pt;");
    htmlString.append(" text-decoration: none;");
    htmlString.append('}');

    /* Page Header Border Appearance */
    htmlString.append("td.pageBorder {  ");
    htmlString.append(" background-color: #003366; ");
    htmlString.append('}');

    /* --------------------------------------------------------------------
    // Page Subheader Settings
    // -------------------------------------------------------------------- */

    /* Page Header Text Appearance */
    htmlString.append("td.pageSubheader {  ");
    htmlString.append(" color: #990000; ");
    htmlString.append(" font-family: Arial, Helvetica, Sans-Serif; ");
    htmlString.append(" font-size: 13pt; ");
    htmlString.append(" font-weight: bold; ");
    htmlString.append('}');

    /* --------------------------------------------------------------------
    // Miscellaneous Settings
    // -------------------------------------------------------------------- */

    /* Welcome message for loading page */
    htmlString.append("td.welcome { ");
    htmlString.append(" color: #000000; ");
    htmlString.append(" font-family: Arial, Helvetica, sans-serif; ");
    htmlString.append(" font-size: 14px; ");
    htmlString.append(" font-weight: bold; ");
    htmlString.append('}');

    /* Small Space Appearance - for non-breaking space workaround at end of files */
    htmlString.append("td.smallSpace { ");
    htmlString.append(" font-family: verdana,arial, helvetica,sans-serif; ");
    htmlString.append(" font-size: 4pt; ");
    htmlString.append('}');
    htmlString.append("td.blackrule {  ");
    htmlString.append(" background-color: #000000;");
    htmlString.append('}');

    /* Filter/Pagination Control Appearance */
    htmlString.append("td.filter, select.filter, td.pagination, select.pagination { ");
    htmlString.append(" font-family: Verdana, Arial, Helvetica, sans-serif; ");
    htmlString.append(" font-size: 11px ");
    htmlString.append('}');

    /* Pagination Control Background Appearance */
    htmlString.append("table.pagination { ");
    htmlString.append(" background-color: #eeeeee; ");
    htmlString.append('}');

    /* History Subheader */
    htmlString.append("td.historySubheader { ");
    htmlString.append(" font-weight: bold; ");
    htmlString.append('}');


    /* Default Label Appearance */
    htmlString.append("td.label { background-color: #dddecb; color: black; font-weight: bold; height: 24px; }");

    /* Display Field Appearance */
    htmlString.append("td.field { background-color: #eeeeee; }");

    /* --------------------------------------------------------------------
    // Headings
    // -------------------------------------------------------------------- */

    /* Heading Level 1 */
    htmlString.append("td.heading1 { font-size: 10pt; font-weight: bold; border-top: 1px solid #003366;  height: 24px;}");

    /* Heading Level 2 */
    htmlString.append("td.heading2 { font-size: 8pt; font-weight: bold; background-color: #dddddd;  height: 24px;}");



    /* Table Header Appearance */
    htmlString.append("th { ");
    htmlString.append(" background-color: #336699; ");
    htmlString.append(" color: white; ");
    htmlString.append(" text-align: left; ");
    htmlString.append('}');

    /* Table Header Link Appearance */
    htmlString.append("th a { ");
    htmlString.append(" text-align: left; ");
    htmlString.append(" color: white; ");
    htmlString.append(" text-decoration: none;  ");
    htmlString.append('}');
    htmlString.append("th a:hover { ");
    htmlString.append(" text-decoration: underline; ");
    htmlString.append(" color: #ccffff; ");
    htmlString.append('}');

    /* Table Header Column Group Header */
    htmlString.append("th.groupheader { ");
    htmlString.append(" background-color: white; ");
    htmlString.append(" color: #1E4365; ");
    htmlString.append(" font-size: 12px; ");
    htmlString.append(" font-weight: bold;");
    htmlString.append(" text-align: left; ");
    htmlString.append('}');

    /* Table Header Column Group Header Rule */
    htmlString.append("th.rule { ");
    htmlString.append(" background-color: #1E4365;");
    htmlString.append('}');

    /* --------------------------------------------------------------------
    // Main Table Settings
    // -------------------------------------------------------------------- */

    /* Sorted Table Header Appearance */
    htmlString.append("th.sorted { ");
    htmlString.append(" background-color: #336699; ");
    htmlString.append('}');

    /* Sub Table Header Appearance */
    htmlString.append("th.sub { ");
    htmlString.append(" text-align: left; ");
    htmlString.append(" color: white; ");
    htmlString.append(" background-color: #999999; ");
    htmlString.append('}');

    /* Sorted Sub Table Header Appearance */
    htmlString.append("th.subSorted { ");
    htmlString.append(" background-color: #999999; ");
    htmlString.append('}');


    /* Odd Table Row Appearance */
    htmlString.append("tr.odd { ");
    htmlString.append(" background-color: #ffffff;");
    htmlString.append('}');

    /* Even Table Row Appearance */
    htmlString.append("tr.even { ");
    htmlString.append(" background-color: #eeeeee;");
    htmlString.append('}');

    /* Table Header Column Group Header Rule */
    htmlString.append("tr.rule { ");
    htmlString.append(" background-color: #1E4365;");
    htmlString.append('}');

    /* Separator Appearance */
    htmlString.append("td.separator { ");
    htmlString.append(" background-color: #DDDECB");;
    htmlString.append('}');

    /* Separator Appearance */
    htmlString.append("td.whiteseparator { ");
    htmlString.append(" background-color: white; ");
    htmlString.append("} ");

    /* --------------------------------------------------------------------
    // Pagination Control Settings
    // -------------------------------------------------------------------- */

    /* Pagination Control Appearance*/
    htmlString.append("select.pagination, option.pagination {  ");
    htmlString.append(" font-family: Verdana, Arial, Helvetica, sans-serif; ");
    htmlString.append(" font-size: 10px;");
    htmlString.append('}');

    /* --------------------------------------------------------------------
    // Filter Control Settings
    // -------------------------------------------------------------------- */

    /* Filter Appearance*/
    htmlString.append("td.filter, select.filter, ");
    htmlString.append("option.filter {  ");
    htmlString.append(" font-family: Verdana, Arial, Helvetica, sans-serif; ");
    htmlString.append(" font-size: 11px;");
    htmlString.append('}');


    /* ====================================================================
    // Spec View Stylesheet
    // Platform: Windows
    // by Don Maurer
    // ==================================================================== */

    /* --------------------------------------------------------------------
    // Default Settings
    // -------------------------------------------------------------------- */

    /* Background Appearance */
    htmlString.append("body { background-color: white; }");

    /* Font Appearance */
    htmlString.append("body, th, td, p { font-family: verdana, helvetica, arial, sans-serif; font-size: 8pt; }");

    /* Object Text (Non-link) Appearance */
    htmlString.append("span.object {  font-weight: bold; }");

    /* --------------------------------------------------------------------
    // Page Header Settings
    // -------------------------------------------------------------------- */

    /* Page Header Text Appearance */


    htmlString.append("td.pageHeader {  font-family: Arial, Helvetica, Sans-Serif; font-size: 13pt; font-weight: bold; color: #990000; }");
    htmlString.append("td.pageHeaderSubtext {  font-family: Arial, Helvetica, Sans-Serif; font-size: 8pt; color: #990000; }");

    /* --------------------------------------------------------------------
    // Page Subheader Settings
    // -------------------------------------------------------------------- */


    /* Table Title Text Appearance */
    htmlString.append("td.tableTitleMajor {  font-family: Arial, Verdana, Helvetica, Sans-Serif; font-size: 12pt; font-weight: bold; color: #990000; }");

    /* Table Description Text Appearance */
    htmlString.append("td.descriptionText {  font-family: Verdana, Helvetica, Sans-Serif; font-size: 8pt; color: #000000; border-top: 1px solid black; border-bottom: 1px solid black;}");

    /* Table Header Appearance */
    htmlString.append("th { color:#000000;text-align: left; border-bottom: 1px solid black; border-top: 1px solid black; background: #dddddd;} ");

    /* Odd Table Row Appearance */
    htmlString.append("td.listCell { border-bottom: 1px solid black; }");

    /* Horizontal Rule Appearance */
    htmlString.append("hr { color: #000000; }");


    //kf
    //tr.heading1 {  font-family: Arial, Helvetica, Sans-Serif; font-size: 13pt; font-weight: bold; color: #990000; }");
    htmlString.append("td.heading1 { border-top: 0px; font-family: Arial, Helvetica, Sans-Serif; font-size: 13pt; font-weight: bold; color: #990000; }");
    htmlString.append("heading1 { border-top: 0px; font-family: Arial, Helvetica, Sans-Serif; font-size: 13pt; font-weight: bold; color: #990000; }");

    htmlString.append("td.label {background: #ffffff}");
    htmlString.append("td.field {background: #ffffff}");

    /* Link Appearance */
    htmlString.append("a {color: #000000;text-decoration:none}");
    htmlString.append("a:hover {color: #000000;text-decoration:none }");

    /* Table Header Link Appearance */
    htmlString.append("th a { ");
    htmlString.append("  text-align: left; ");
    htmlString.append("  color: #000000; ");
    htmlString.append("  text-decoration: none;  ");
    htmlString.append('}');
    htmlString.append("th a:hover { ");
    htmlString.append("  text-decoration: underline; ");
    htmlString.append("  color: #000000; ");
    htmlString.append('}');

    htmlString.append("td.state { border-top: 0px; font-family: Arial, Helvetica, Sans-Serif; font-size: 10pt; font-weight: bold; font-style:italic;color: #000000;background: #ffffff }");


    htmlString.append("</style>");
    htmlString.append("<body>");
  String finalStr = htmlString.toString();

    return finalStr;
 }

   /**
     * Provides the Revised Parts for the ECO.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @return a MapList of the Revised Parts.
     * @throws Exception if the operation fails.
     * @since 10.5.
    */

   public MapList getRevisedParts(Context context,String[] args)
         throws Exception
   {
       String objectId = args[0];
       MapList revisedPartsList = new MapList();

       try
       {
        ECO ecoObj = new ECO(objectId);
        StringList selectStmts = new StringList(1);
        selectStmts.addElement(SELECT_ID);
        selectStmts.addElement("evaluate[revision == revisions.last]");
        StringList selectRelStmts = new StringList(1);
        selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
          String sDevelopmentInUse ="true";
    boolean bDevelopmentInUse = false;
    if("true".equals(sDevelopmentInUse)) {
       bDevelopmentInUse = true;
    }
      String sCMInUse = "true";
    boolean bCMInUse = false;
    if("true".equals(sCMInUse)) {
         bCMInUse = true;
    }
    revisedPartsList = ecoObj.getParts(context,
                  RELATIONSHIP_AFFECTED_ITEM,
                  selectStmts,
                  selectRelStmts,
                  false,
                  bDevelopmentInUse,
                  bCMInUse);

       }
       catch (FrameworkException Ex)
       {
            throw Ex;
       }

       return revisedPartsList;
   }


   /**
     * Generates the ECO Summary PDF file and checks it into the ECO object.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @param summaryReport holds the string which need to be rendered into PDF.
     * @return int 0- for success, 1- failure.
     * @throws Exception if the operation fails.
     * @since X3
    */

  public int renderPDFFile(Context context, String []args, String summaryReport) throws Exception {
       String renderSoftwareInstalled = EnoviaResourceBundle.getProperty(context,"emxEngineeringCentral.RenderPDF");

       if(!"TRUE".equalsIgnoreCase(renderSoftwareInstalled)) {
           return 0;
     }
     /* Code without packing */
        String objectId = args[0];

        setId(objectId);
        String objType = getInfo(context, SELECT_TYPE);
        String objName = getInfo(context,SELECT_NAME);
        String objRev = getInfo(context,SELECT_REVISION);

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
            	watermark = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource",new Locale("en"),watermark);
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

                try
                {
                String cmd = "checkin bus $1 $2 $3 format $4 $5";
                MqlUtil.mqlCommand(context,cmd,objType,objName,objRev,"generic",strTempDir + java.io.File.separator + pdfFileName);
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
         }

       }
        else
        {
            MqlUtil.mqlCommand(context, "notice $1","Unable to generate pdf on adlib server");
            return 1;
       }

        return 0;
  }

   /**
     * Constructs the ECO related ECRs HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @return String Html table format representation of Related ECRs data.
     * @throws Exception if the operation fails.
     * @since 10.5.
    */
   public String getRelatedECRs(Context context,String[] args)
         throws Exception
   {
       String strLanguage = context.getSession().getLanguage();
       String objectId = args[0];
     MapList ecrMapList = new MapList();
     StringBuffer relatedECRs = new StringBuffer(1024);
     ContextUtil.startTransaction(context, true);
       try
       {
           ECO ecoObj = new ECO(objectId);
           StringList selectStmts = new StringList(1);
           selectStmts.addElement(SELECT_ID);
       selectStmts.addElement(SELECT_TYPE);
       selectStmts.addElement(SELECT_NAME);
       selectStmts.addElement(SELECT_REVISION);
       selectStmts.addElement(SELECT_DESCRIPTION);
       selectStmts.addElement(SELECT_CURRENT);
       selectStmts.addElement(SELECT_POLICY);

       StringList selectRelStmts = new StringList(1);
           selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

           ecrMapList = FrameworkUtil.toMapList(ecoObj.getExpansionIterator(context, DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT, "*",
                   selectStmts, selectRelStmts, false, true, (short)1,
                   null, null, (short)0,
                   false, false, (short)0, false),
                   (short)0, null, null, null, null);



		ecrMapList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
		ecrMapList.sort();
       Iterator objItr = ecrMapList.iterator();
           Map ecrMap  = null;
       relatedECRs.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
       relatedECRs.append("<tr>");
       relatedECRs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
       relatedECRs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Rev",strLanguage)+"</th>");
       relatedECRs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</th>");
       relatedECRs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</th>");
       relatedECRs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage)+"</th>");
       relatedECRs.append("</tr>");

       while (objItr.hasNext()) {
              ecrMap = (Map)objItr.next();
        relatedECRs.append("<tr>");
        relatedECRs.append("<td><img src=\"../common/images/iconSmallECR.gif\" border=\"0\" alt=\"*\">&nbsp;"+ecrMap.get(SELECT_NAME)+"&nbsp;</td>");
        relatedECRs.append("<td>"+ecrMap.get(SELECT_REVISION)+"&nbsp;</td>");
        relatedECRs.append("<td>"+ecrMap.get(SELECT_TYPE)+"&nbsp;</td>");
        relatedECRs.append("<td>"+ecrMap.get(SELECT_DESCRIPTION)+"&nbsp;</td>");
        relatedECRs.append("<td>"+i18nNow.getStateI18NString((String)ecrMap.get(SELECT_POLICY),(String)ecrMap.get(SELECT_CURRENT),strLanguage)+"&nbsp;</td>");
        relatedECRs.append("</tr>");
       }
       if(ecrMapList.size()==0) {
          relatedECRs.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.BuildECR.NoRelatedECRsFound",strLanguage)+"</td></tr>");
       }
       }
       catch (FrameworkException Ex)
       {
    	   ContextUtil.abortTransaction(context);
            throw Ex;
       }
       relatedECRs.append("</table>");
       ContextUtil.commitTransaction(context);
       return relatedECRs.toString();
   }

  /**
   * Gets the list of Routes in HTML table format.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds the following input arguments:
   * 0 - String containing object id.
   * @return String Html table format representation of Routes info.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
  public String getRoutes(Context context, String[] args)
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


    StringBuffer routeInfo = new StringBuffer(1024);
    routeInfo.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
    routeInfo.append("<tr><th width=\"5%\" style=\"text-align:center\"><img border=\"0\" src=\"../common/images/iconStatus.gif\" name=\"imgstatus\" id=\"imgstatus\" alt=\"*\"></th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Status",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Routes.ScheduleCompDate",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</th>");

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
        if (!"Red".equals(sCode)) {
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

          if ("Complete".equals(sState)) {
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

            if("Red".equals(sCode)) {
        routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusRed.gif\" name=\"red\" id=\"red\" alt=\"emxComponents.TaskSummary.ToolTipRed\">";
            } else if("green".equals(sCode)) {
                routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusGreen.gif\" name=\"green\" id=\"green\" alt=\"emxComponents.TaskSummary.ToolTipGreen\">";
            } else if("yellow".equals(sCode)) {
        routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusYellow.gif\" name=\"yellow\" id=\"yellow\" alt=\"emxComponents.TaskSummary.ToolTipYellow\">";
            } else {
                routeIcon = "&nbsp;";
      }

      routeInfo.append("<tr>");
      routeInfo.append("<td>"+routeIcon+"</td>");
      routeInfo.append("<td>"+routeMap.get(SELECT_NAME)+"</td>");
      routeInfo.append("<td>"+routeMap.get(SELECT_DESCRIPTION)+"</td>");
      routeInfo.append("<td>"+routeMap.get(routeStatusAttrSel)+"</td>");
      routeInfo.append("<td>"+scheduledCompletionDate+"</td>");
      routeInfo.append("<td>"+routeMap.get(SELECT_OWNER)+"</td>");
      routeInfo.append("</tr>");
    }

    if(totalResultList.size()==0) {
      routeInfo.append("<tr><td colspan=\"6\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.NoObjectsFound",strLanguage)+"</td></tr>");
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
   * Provides the BOM Comparison details of ECO.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds the following input arguments:
   * 0 - String containing object id.
   * @return String Html table format representation of BOM Comparison details.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public String getBOMComparisonDetails(Context context,String[] args)
         throws Exception
  {
	  StringBuffer bomComparison=null;
      String strLanguage=null;
	  try{
      strLanguage = context.getSession().getLanguage();
      MapList RevisedParts = getRevisedParts(context,args);
      RevisedParts.sort("evaluate[revision == revisions.last]", "descending", "String");
      bomComparison = new StringBuffer();
      Iterator objItr =  RevisedParts.iterator();
      Map map = null;
      Map tempMap = null;
      Vector comparisonList = new Vector();
	  Part partObj = (Part)DomainObject.newInstance(context,TYPE_PART,ENGINEERING);

		 MapList revisionsList = null;
         StringList busSelects = new StringList();
         busSelects.add(SELECT_ID);
         busSelects.add(SELECT_REVISION);
         busSelects.add(SELECT_CURRENT);

		 String objId = "";
		 while (objItr.hasNext())
        {

      map = (Map)objItr.next();
      objId = (String)map.get(SELECT_ID);

      partObj.setId(objId);
              String prefRevID="";
      BusinessObject prevRev = partObj.getPreviousRevision(context);
      if (prevRev != null) {
        		prefRevID =  prevRev.getObjectId();
		comparisonList.addElement(objId+"|"+prefRevID);
		}

      revisionsList = partObj.getRevisions(context, busSelects, false);

      revisionsList.sort(SELECT_REVISION, "descending", "String");
      Iterator itr = revisionsList.iterator();
        while(itr.hasNext()){
        tempMap = (Map)itr.next();

        if(DomainConstants.STATE_PART_RELEASE.equalsIgnoreCase((String)tempMap.get(SELECT_CURRENT))) {
          String tempObjId = (String)tempMap.get(SELECT_ID);
              //Bug 362975 Added second condition to avoid duplicates
        if(!objId.equals(tempObjId) && !prefRevID.equals(tempObjId) ) {
          comparisonList.addElement(objId+"|"+tempObjId);
          }
        break;
        }
        }
	 }

//changed the name of veriable to enumElement for JDK1.5 issue
     Enumeration enumElement  = comparisonList.elements();
     String partIds = null;
     String part1Id = null;
     String part2Id = null;
     while(enumElement.hasMoreElements()) {
       partIds = (String)enumElement.nextElement();
       part1Id = partIds.substring(0,partIds.indexOf('|'));
       part2Id = partIds.substring(partIds.indexOf('|')+1);
       bomComparison.append(getPartComparisonDetails(context, args, part1Id, part2Id));
     }
     if (comparisonList.isEmpty()) {

       StringBuffer comparisonHTML = new StringBuffer();
       comparisonHTML.append("<table align = \"center\" width = \"100%\" border= \"0\" cellpadding=\"0\", cellspacing=\"0\">");
       comparisonHTML.append("<tr>");
       comparisonHTML.append("<td align=left>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.NoComparisonsToDisplay",strLanguage)+"</td>");
       comparisonHTML.append("</tr>");
       comparisonHTML.append("</table>");
       bomComparison.append(comparisonHTML.toString());
     }

	  }
	  catch(Exception e)
	  {

	    StringBuffer comparisonHTML = new StringBuffer(256);
        comparisonHTML.append("<table align=\"center\" width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
        comparisonHTML.append("<tr>");
        comparisonHTML.append("<td align=left>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.NoComparisonsToDisplay",strLanguage)+"</td>");
        comparisonHTML.append("</tr>");
        comparisonHTML.append("</table>");
        bomComparison.append(comparisonHTML.toString());

	  }

	  return bomComparison.toString();
   }

 /**
   * Provides the BOM Comparison details between two parts in HTML format.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds the following input arguments:
   * 0 - String containing object id.
   * @param sPart1Id String first part object id.
   * @param sPart1Id String second part object id.
   * @return String containing Comparison information in html format.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public String getPartComparisonDetails(Context context, String[] args, String sPart1Id, String sPart2Id) throws Exception {

    // Preloading Lookup Strings
    String sLkAttrFindNumber = DomainConstants.ATTRIBUTE_FIND_NUMBER;
    String sLkAttrQuantity = DomainConstants.ATTRIBUTE_QUANTITY;
    String sLkAttrRefDesignator = DomainConstants.ATTRIBUTE_REFERENCE_DESIGNATOR;

    // sKeyValue is the key that is used in the comparison report
    // 1) Currently supported attributes are attribute_FindNumber or attribute_ReferenceDesignator.
    //    If another attribute should be used we also have to add it in the select statements
    // 2) If not defined in the properties file the default is attribute_FindNumber

    String sKeyValue = sLkAttrFindNumber;

    // Define variables
    Hashtable part1HashbyFN = new Hashtable();
    Hashtable part2HashbyFN = new Hashtable();
    Hashtable part1HashbyPt = new Hashtable();
    Hashtable part2HashbyPt = new Hashtable();
    Vector dupKey1 = new Vector();
    Vector dupKey2 = new Vector();
    String sKey = null;
    String sData = null;
    // Compare hashtables for Report
    Hashtable compReport = new Hashtable();
    Hashtable commonReport = new Hashtable();

    StringList selectRelStmts = new StringList(4);
    selectRelStmts.addElement("id");
    selectRelStmts.addElement("attribute["+sLkAttrFindNumber+"]");
    selectRelStmts.addElement("attribute["+sLkAttrQuantity+"]");
    selectRelStmts.addElement("attribute["+sLkAttrRefDesignator+"]");

    StringList selectStmts = new StringList(5);
    selectStmts.addElement("id");
    selectStmts.addElement("name");
    selectStmts.addElement("description");
    selectStmts.addElement("revision");
    selectStmts.addElement("type");

    // EXPAND FIRST PART
    // Expand part id 1 and create hashtables for compare
    ContextUtil.startTransaction(context, false);
    try {
    DomainObject ExpandPartObj = new DomainObject(sPart1Id);
    ExpandPartObj.open(context);
    ExpansionIterator _objectSelect = ExpandPartObj.getExpansionIterator(context, DomainRelationship.RELATIONSHIP_EBOM, TYPE_PART,
            selectStmts, selectRelStmts, false, true, (short)1,
            null, null, (short)0,
            false, false, (short)1, false);
    StringItr strItr = new StringItr(selectStmts);
    StringItr strRelItr = new StringItr(selectRelStmts);

    try{
    while (_objectSelect.hasNext())
    {
    RelationshipWithSelect relSelect = _objectSelect.next();

    //get the Relationship Data
    Hashtable relData = relSelect.getRelationshipData();

    //get the Target Data
    Hashtable targetData = relSelect.getTargetData();

    // Get Part id from Matrix
    strItr.next();
    String sId = (String)targetData.get(strItr.obj());

    // Get Relationship ID from Matrix
    strRelItr.next();
    // Get FindNumber from Matrix
    strRelItr.next();
    String sFN = (String)relData.get(strRelItr.obj());
    // if sFN not assigned will come in as nothing - needs to have a value
    if( (sFN!=null && sFN.equals("")) || (sFN==null) )
    {
      sFN = " ";
    }

    // Get Quantity from Matrix
    strRelItr.next();
    String sQty = (String)relData.get(strRelItr.obj());
    // Get PartName from Matrix
    strItr.next();
    String sPartName = (String)targetData.get(strItr.obj());
    // Get Part Description from Matrix
    strItr.next();
    String sPartDescription = (String)targetData.get(strItr.obj());
    // if description is not assigned will come in as nothing - needs to have a value
    if ("".equals(sPartDescription))
    {
      sPartDescription = " ";
    }

    // Get Parts Revision from Matrix
    strItr.next();
    String sRev = (String)targetData.get(strItr.obj());

    // Get Reference Designator from Matrix
    strRelItr.next();
    String sRefDes = (String)relData.get(strRelItr.obj());
    //if reference designator not assigned comes in as nothing - needs to have a value
    if (sRefDes==null || sRefDes.equals(""))
    {
      sRefDes = " ";
    }

    // Get  Type from Matrix
    strItr.next() ;
    String sType = (String)targetData.get(strItr.obj());

    // The key is set to the defined attribute in the
    // eServiceFeatureEngineeringCentralPartStructureCompareEBOMReport.Key property.
    if (sKeyValue.equals(sLkAttrRefDesignator)) {
      sKey = sRefDes;
    } else {
      sKey = sFN;
    }

    sData = sId + "|" + sType + "|" + sPartName + "|" + sRev + "|" + sFN + "|" + sRefDes + "|" + sPartDescription + "|" + sQty;
    String sQtyPart1Value="";

    // Check if hashtable already contains the find number before adding element. Create list of duplicates
    if (!part1HashbyFN.containsKey(sKey))
    {
      part1HashbyFN.put(sKey, sData);
      sQtyPart1Value=sQty;
    }
    else
    {
      //Now that we know that the FN is duplicated. We need to check if type, name, rev is the same
      //If it is same then we add the quantities and put them in the hashtable or else put it in
      //duplicate list
      String hashTableData = (String) part1HashbyFN.get(sKey);

      StringTokenizer valueTok = new StringTokenizer(hashTableData,"|",false);

      // extract from part 1 list
      String sId1 = valueTok.nextToken();
      String sRefDes1 = valueTok.nextToken();
      Double sQty1 = new Double(0);
      String temp = (String)valueTok.nextToken();
      if(temp!=null&& !"null".equals(temp.trim())) {
        sQty1 = new Double(temp);
      }

      if(sId1.equals(sId))
      {
      sQtyPart1Value = new Double(new Double(sQty).doubleValue() + sQty1.doubleValue()).toString();
      if(!sRefDes.equals(sRefDes1))
      {
        sRefDes = sRefDes + ", " + sRefDes1;
      }
      sData = sId + "|" + sType + "|" + sPartName + "|" + sRev + "|" + sFN + "|" + sRefDes + "|" + sPartDescription + "|" + sQtyPart1Value;
      part1HashbyFN.remove(sKey);
      part1HashbyFN.put(sKey, sData);
      }
      else
      {
      // jb - should be the key
      dupKey1.addElement(sData);
      sQtyPart1Value=sQty;

      }
    }

    // Update hashtable for common/unique element compare
    sKey = sType.trim()+"|"+sPartName.trim()+"|"+sRev.trim();
    sData = sPartDescription + "|" + sQtyPart1Value;
    if(!part1HashbyPt.containsKey(sKey))
    {
      part1HashbyPt.put(sKey, sData);
    }
    else
    {
      // the current part already exists then add the current qty to the existing qty
      String hashTableData = (String) part1HashbyPt.get(sKey);
      StringTokenizer valueTok = new StringTokenizer(hashTableData,"|",false);

    Double sQty1 = new Double(valueTok.nextToken());
      String sQtyPart1Value1="";
      sQtyPart1Value1 = new Double(new Double(sQty).doubleValue() + sQty1.doubleValue()).toString();
      sData = sPartDescription + "|" + sQtyPart1Value1;
      part1HashbyPt.remove(sKey);
      part1HashbyPt.put(sKey,sData);
    }
    strRelItr.reset();
    strItr.reset();

    }  // end while loop for first part
    ExpandPartObj.close(context);

    while (_objectSelect.hasNext())
    {
    //StringBuffer spaceBuf = new StringBuffer();
    RelationshipWithSelect relSelect = _objectSelect.next();
    //get the Relationship Data
    Hashtable relData = relSelect.getRelationshipData();
    //get the Target Data
    Hashtable targetData = relSelect.getTargetData();
    // Get Part id from Matrix
    strItr.next();

    String sId = (String)targetData.get(strItr.obj());
    // Get Relationship ID from Matrix
    strRelItr.next();
    // Get FindNumber from Matrix
    strRelItr.next();
    String sFN = (String)relData.get(strRelItr.obj());
    // if sFN not assigned will come in as nothing - needs to have a value
    if( (sFN!=null && sFN.equals("")) || (sFN==null) )
    {
      sFN = " ";
    }

    // Get Quantity from Matrix
    strRelItr.next();
    String sQty = (String)relData.get(strRelItr.obj());
    // Get PartName from Matrix
    strItr.next();
    String sPartName = (String)targetData.get(strItr.obj());
    // Get Part Description from Matrix
    strItr.next();
    String sPartDescription = (String)targetData.get(strItr.obj());
    // if description is not assigned will come in as nothing - needs to have a value
    if ("".equals(sPartDescription))
    {
      sPartDescription = " ";
    }

    // Get Parts Revision from Matrix
    strItr.next();
    String sRev = (String)targetData.get(strItr.obj());

    // Get Reference Designator from Matrix
    strRelItr.next();
    String sRefDes = (String)relData.get(strRelItr.obj());
    //if reference designator not assigned comes in as nothing - needs to have a value
    if (sRefDes==null || "".equals(sRefDes))
    {
      sRefDes = " ";
    }

    // Get  Type from Matrix
    strItr.next() ;
    String sType = (String)targetData.get(strItr.obj());

    // The key is set to the defined attribute in the
    // eServiceFeatureEngineeringCentralPartStructureCompareEBOMReport.Key property.
    if (sKeyValue.equals(sLkAttrRefDesignator)) {
      sKey = sRefDes;
    } else {
      sKey = sFN;
    }

    sData = sId + "|" + sType + "|" + sPartName + "|" + sRev + "|" + sFN + "|" + sRefDes + "|" + sPartDescription + "|" + sQty;
    String sQtyPart1Value="";
    // Check if hashtable already contains the find number before adding element. Create list of duplicates
    if (!part1HashbyFN.containsKey(sKey))
    {
      part1HashbyFN.put(sKey, sData);
      sQtyPart1Value=sQty;
    }
    else
    {
      //Now that we know that the FN is duplicated. We need to check if type, name, rev is the same
      //If it is same then we add the quantities and put them in the hashtable or else put it in
      //duplicate list
      String hashTableData = (String) part1HashbyFN.get(sKey);
      StringTokenizer valueTok = new StringTokenizer(hashTableData,"|",false);

      // extract from part 1 list
      String sId1 = valueTok.nextToken();
      String sRefDes1 = valueTok.nextToken();
      Double sQty1 = new Double(0);
      String temp = (String)valueTok.nextToken();
      if(temp!=null&& !"null".equals(temp.trim())) {
        sQty1 = new Double(temp);
      }

      if(sId1.equals(sId))
      {
      sQtyPart1Value = new Double(new Double(sQty).doubleValue() + sQty1.doubleValue()).toString();

      if(!sRefDes.equals(sRefDes1))
      {
        sRefDes = sRefDes + ", " + sRefDes1;
      }
      sData = sId + "|" + sType + "|" + sPartName + "|" + sRev + "|" + sFN + "|" + sRefDes + "|" + sPartDescription + "|" + sQtyPart1Value;
      part1HashbyFN.remove(sKey);
      part1HashbyFN.put(sKey, sData);
      }
      else
      {
      // jb - should be the key
      dupKey1.addElement(sData);
      sQtyPart1Value=sQty;
      }
    }

    // Update hashtable for common/unique element compare
    sKey = sType.trim()+"|"+sPartName.trim()+"|"+sRev.trim();
    sData = sPartDescription + "|" + sQtyPart1Value;
    if(!part1HashbyPt.containsKey(sKey))
    {
      part1HashbyPt.put(sKey, sData);
    }
    else
    {
      // the current part already exists then add the current qty to the existing qty
      String hashTableData = (String) part1HashbyPt.get(sKey);

      StringTokenizer valueTok = new StringTokenizer(hashTableData,"|",false);

      // extract from part 1 list
      Double sQty1 = new Double(valueTok.nextToken());
      String sQtyPart1Value1="";
      sQtyPart1Value1 = new Double(new Double(sQty).doubleValue() + sQty1.doubleValue()).toString();
      sData = sPartDescription + "|" + sQtyPart1Value1;
      part1HashbyPt.remove(sKey);
      part1HashbyPt.put(sKey,sData);
    }

    strRelItr.reset();
    strItr.reset();

    }  // end while loop for first part

    }catch(Exception exp){
    	throw exp;
    }finally{
    	_objectSelect.close();
    }
    // EXPAND SECOND PART
    // Expand part id 2 and create hashtables for compare
    ExpandPartObj = new DomainObject(sPart2Id);
    ExpandPartObj.open(context);

    try {
        _objectSelect = ExpandPartObj.getExpansionIterator(context, DomainRelationship.RELATIONSHIP_EBOM, TYPE_PART,
                selectStmts, selectRelStmts, false, true, (short)1,
                null, null, (short)0,
                false, false, (short)1, false);
    strItr = new StringItr(selectStmts);
    strRelItr = new StringItr(selectRelStmts);
    }catch(Exception e) {}

    try{
    while (_objectSelect.hasNext())
    {
    //StringBuffer spaceBuf = new StringBuffer();
    RelationshipWithSelect relSelect = _objectSelect.next();
    //get the Relationship Data
    Hashtable relData = relSelect.getRelationshipData();
    //get the Target Data
    Hashtable targetData = relSelect.getTargetData();
    // Get Part id from Matrix
    strItr.next();
    String sId = (String)targetData.get(strItr.obj());
    // Get Relationship ID from Matrix
    strRelItr.next();
    // Get FindNumber from Matrix
    strRelItr.next();
    String sFN = (String)relData.get(strRelItr.obj());

    // if sFN not assigned will come in as nothing - needs to have a value
    if( (sFN!=null && sFN.equals("")) ||(sFN==null) )
    {
      sFN = " ";
    }

    // Get Quantity from Matrix
    strRelItr.next();
    String sQty = (String)relData.get(strRelItr.obj());

    // Get PartName from Matrix
    strItr.next();
    String sPartName = (String)targetData.get(strItr.obj());

    // Get Part Description from Matrix
    strItr.next();
    String sPartDescription = (String)targetData.get(strItr.obj());
    // if description is not assigned will come in as nothing - needs to have a value
    if("".equals(sPartDescription))
    {
      sPartDescription = " ";
    }

    // Get Parts Revision from Matrix
    strItr.next();
    String sRev = (String)targetData.get(strItr.obj());

    // Get Reference Designator from Matrix
    strRelItr.next();
    String sRefDes = (String)relData.get(strRelItr.obj());
    //if reference designator not assigned comes in as nothing - needs to have a value
    if (sRefDes==null || "".equals(sRefDes))
    {
      sRefDes = " ";
    }

    // Get Type from Matrix
    strItr.next() ;
    String sType = (String)targetData.get(strItr.obj());

    // The key is set to the defined attribute in the
    // eServiceFeatureEngineeringCentralPartStructureCompareEBOMReport.Key property.
    if (sKeyValue.equals(sLkAttrRefDesignator)) {
      sKey = sRefDes;
    } else {
      sKey = sFN;
    }
    sData = sId + "|" + sType + "|" + sPartName + "|" + sRev + "|" + sFN + "|" + sRefDes + "|" + sPartDescription + "|" + sQty;
    String sQtyPart2Value="";
    // Check if hashtable already contains the find number before adding element. Create list of duplicates
    if (!part2HashbyFN.containsKey(sKey))
    {
      part2HashbyFN.put(sKey, sData);
      sQtyPart2Value=sQty;
    }
    else
    {
      //Now that we know that the FN is duplicated. We need to check if type, name, rev is the same
      //If it is same then we add the quantities and put them in the hashtable or else put it in
      //duplicate list
      String hashTableData = (String) part2HashbyFN.get(sKey);

      StringTokenizer valueTok = new StringTokenizer(hashTableData,"|",false);

      // extract from part 2 list
      String sId1 = valueTok.nextToken();
      String sRefDes1 = valueTok.nextToken();

      Double sQty1 = new Double(0); //new Double(valueTok.nextToken());
      String temp = (String)valueTok.nextToken();
      if( temp!=null && !"null".equals(temp.trim())) {
        sQty1 = new Double(temp);
      }

      if(sId1.equals(sId))
      {
      sQtyPart2Value = new Double(new Double(sQty).doubleValue() + sQty1.doubleValue()).toString();

      if(!sRefDes.equals(sRefDes1))
      {
        sRefDes = sRefDes + ", " + sRefDes1;
      }
      sData = sId + "|" + sType + "|" + sPartName + "|" + sRev + "|" + sFN + "|" + sRefDes + "|" + sPartDescription + "|" + sQtyPart2Value;
      part2HashbyFN.remove(sKey);
      part2HashbyFN.put(sKey, sData);
      }
      else
      {
      dupKey2.addElement(sData);
      sQtyPart2Value=sQty;
      }
    }

    // Update hashtable for common/unique element compare
    sKey = sType.trim()+"|"+sPartName.trim()+"|"+sRev.trim();
    sData = sPartDescription + "|" + sQtyPart2Value;
    if(!part2HashbyPt.containsKey(sKey))
    {
      part2HashbyPt.put(sKey, sData);

    }
    else
    {
      // the current part already exists then add the current qty to the existing qty
      String hashTableData = (String) part2HashbyPt.get(sKey);

      StringTokenizer valueTok = new StringTokenizer(hashTableData,"|",false);

    // Modified for Bug 317840
      Double sQty1 = new Double(valueTok.nextToken());
      String sQtyPart2Value1="";
      sQtyPart2Value1 = new Double(new Double(sQty).doubleValue() + sQty1.doubleValue()).toString();
      sData = sPartDescription + "|" + sQtyPart2Value1;
      part2HashbyPt.remove(sKey);
      part2HashbyPt.put(sKey,sData);

    }

    strRelItr.reset();
    strItr.reset();

    }  // end while loop for second part
}catch(Exception exp){
    	throw exp;
    }finally{
    	_objectSelect.close();
    }
    ExpandPartObj.close(context);

    // Send duplicate vectors back
    Hashtable keyInfo = new Hashtable();
    keyInfo.put("part1DupKey",dupKey1);
    keyInfo.put("part2DupKey",dupKey2);

    //CREATE Comparison report Hashtables
    if(!part1HashbyFN.isEmpty())
    {
    Enumeration part1Keys = part1HashbyFN.keys();

    while (part1Keys.hasMoreElements())
    {
      String sPart1Key = part1Keys.nextElement().toString();
      if (part2HashbyFN.containsKey(sPart1Key))
      {
      // get values from hashtable for Part 1 to compare part name and quantity
      String value1 = (String) part1HashbyFN.get(sPart1Key);
      StringTokenizer valueTok = new StringTokenizer(value1,"|",false);

      // extract part number and quantity for part 1 list
      String sId1 = valueTok.nextToken();

      String sQty1 = valueTok.nextToken();

      // get values from hashtable for Part 2 to compare part name and quantity
      String value2 = (String) part2HashbyFN.get(sPart1Key);
      StringTokenizer valueTok2 = new StringTokenizer(value2,"|",false);

      // extract part number and quantity for part 2 list
      String sId2 = valueTok2.nextToken();
      String sType2 = valueTok2.nextToken();
      String sName2 = valueTok2.nextToken();
      String sRevision2 = valueTok2.nextToken();
      String sFN2 = valueTok2.nextToken();
      String sRD2 = valueTok2.nextToken();
      String sPD2 = valueTok2.nextToken();
      String sQty2 = valueTok2.nextToken();

      if (sId1.equals(sId2))
      {
        if (sQty1.equals(sQty2))
        {
        // update compare report with no item or quantity differences
        sKey = sPart1Key + "|" + " ";
        sData = value1.substring(value1.indexOf('|'),value1.length()) + "|" + sQty2 + "|" + " " + "|" + " ";
        compReport.put(sKey, sData);
        }
        else
        {
        // update compare report with quantity difference
        sKey = sPart1Key + "|" + " ";
        sData = value1.substring(value1.indexOf('|'),value1.length()) + "|" + sQty2 + "|" + " " + "|" + "X";
        compReport.put(sKey, sData);
        } // end of if for qty check
      }
      else
      {
        // update compare report with item difference - 2 lines written to table
        sKey = sPart1Key + "|" + "1";
        sData = value1.substring(value1.indexOf('|'),value1.length()) + "|" + " " + "|" + "X" + "|" + " ";
        compReport.put(sKey, sData);
        sKey = sPart1Key + "|" + "2";
        sData = sType2 + "|" + sName2 + "|" + sRevision2 + "|" + sFN2 + "|" + sRD2 + "|" + sPD2 + "|" + " " + "|" + sQty2 +  "|" + "X" + "|" + " ";
        compReport.put(sKey, sData);
      } // end if for name check

      // remove entry from part 2 FN table
      part2HashbyFN.remove(sPart1Key);

      } // end of if for part 2 contains key
      else
      {
      // get values from hashtable for Part 1 to compare part name and quantity
      String valueone = (String) part1HashbyFN.get(sPart1Key);
      // update compare report with item difference if only in part1 table
      sKey = sPart1Key + "|" + " ";
      sData = valueone.substring(valueone.indexOf('|'),valueone.length()) + "|" + " " + "|" + "X" + "|" + " ";
      compReport.put(sKey, sData);
      } // end else if part 2 table doesn't contain part 1 FN
    } // end of while for part1Keys has more elements
    } // end of if for part 1 table by fn empty

    // Take any elements left in Part 2 FN table and add item difference entries in compReport table
    if(!part2HashbyFN.isEmpty())
    {
    Enumeration partKeys = part2HashbyFN.keys();

    while (partKeys.hasMoreElements())
    {
      String sPartKey = partKeys.nextElement().toString();

      // get values from hashtable for Part 2 to compare part name and quantity
      String value = (String) part2HashbyFN.get(sPartKey);
      StringTokenizer valueTok = new StringTokenizer(value,"|",false);

      // extract part number and quantity for part 2 list
      String sType = valueTok.nextToken();
      String sName = valueTok.nextToken();
      String sRevision = valueTok.nextToken();
      String sFN = valueTok.nextToken();
      String sRD = valueTok.nextToken();
      String sPD = valueTok.nextToken();
      String sQty = valueTok.nextToken();

      sKey = sPartKey + "|" + " ";
     sData = sType + "|" + sName + "|" + sRevision + "|" + sFN + "|" + sRD + "|" + sPD + "|" + " " + "|" + sQty +  "|" + "X" + "|" + " ";
      compReport.put(sKey, sData);

    } // end while part 2 has more elements
    } // end if part2fn not empty


    // Pass hash table
    keyInfo.put("emxCompare",compReport);

    // Compare hashtables for Unique and Common Reports
    if(!part1HashbyPt.isEmpty())
    {
    Enumeration part1Keys = part1HashbyPt.keys();

    while (part1Keys.hasMoreElements())
    {
      String sPart1Key = part1Keys.nextElement().toString();
      if (part2HashbyPt.containsKey(sPart1Key))
      {
      // get values from hashtable for Part 1 to put in common table
      String value1 = (String) part1HashbyPt.get(sPart1Key);

      //code added to populate Qty2 in the hashtable
      String value2 = (String) part2HashbyPt.get(sPart1Key);
      StringTokenizer tokens2 = new StringTokenizer(value2,"|",false);
      String sQty2 = tokens2.nextToken();

      //end of code addition

      // put value in common table
      sKey = sPart1Key;
      sData = value1+"|"+sQty2;
      commonReport.put(sKey,sData);

      // remove entry from part 1 and part 2 table
      part1HashbyPt.remove(sPart1Key);
      part2HashbyPt.remove(sPart1Key);
      }
    } // end of while for partKeys has more elements
    }// end if for empty part 1 by part hashtable


    // Pass hash tables
    keyInfo.put("emxUnique1",part1HashbyPt);
    keyInfo.put("emxUnique2",part2HashbyPt);
    keyInfo.put("emxCommon",commonReport);
    keyInfo.put("part1id",sPart1Id);
    keyInfo.put("part2id",sPart2Id);
    ContextUtil.commitTransaction(context);
    return getPartComparisonDetailsHTML(context, keyInfo);
    }
    catch (Exception e) {
    	ContextUtil.abortTransaction(context);
    }
    return "";
   }


    /**
   * Provides the BOM Comparison details HTML code between two parts.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param info Hashtable which holds the comparable information for both parts like common, unique parts.
   * @return String containing Comparison information in html format.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
    public String getPartComparisonDetailsHTML(Context context, Hashtable info) throws Exception
    {

        StringBuffer comparisonHTML = new StringBuffer(4096);
        String strLanguage     = context.getSession().getLanguage();

        String sSummary               = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Summary",strLanguage);
        String sCommonComponents      = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.CommonComponents",strLanguage);
        String sPart1UniqueComponents = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Part1UniqueComponents",strLanguage);
        String sPart2UniqueComponents = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Part2UniqueComponents",strLanguage);

        String partIdOne = (String)info.get("part1id");
        String partIdTwo = (String)info.get("part2id");

        StringList strList = new StringList();
        strList.add(DomainConstants.SELECT_NAME);
        strList.add(DomainConstants.SELECT_TYPE);
        strList.add(DomainConstants.SELECT_REVISION);
        strList.add(DomainConstants.SELECT_EFFECTIVITY_DATE);

        DomainObject domObj = new DomainObject(partIdOne);

        Map map = (Map)domObj.getInfo(context,strList);
        String sObjectName        = (String)map.get(DomainConstants.SELECT_NAME);
        String sObjectTypeName    = (String)map.get(DomainConstants.SELECT_TYPE);
        String sObjectRevision    = (String)map.get(DomainConstants.SELECT_REVISION);
        String sEffectivityDate   = (String)map.get(DomainConstants.SELECT_EFFECTIVITY_DATE);

        if(!"".equals(sEffectivityDate))
        {
            sEffectivityDate = getFormatDate(sEffectivityDate);
        }

        String typeIcon1 = EngineeringUtil.getTypeIconProperty(context, sObjectTypeName);
        if (typeIcon1 == null || typeIcon1.length() == 0 )
        {
            typeIcon1 = "iconSmallDefault.gif";
        }

        domObj = new DomainObject(partIdTwo);
        map = (Map)domObj.getInfo(context,strList);
        String sObjectName2        = (String)map.get(DomainConstants.SELECT_NAME);
        String sObjectTypeName2    = (String)map.get(DomainConstants.SELECT_TYPE);
        String sObjectRevision2    = (String)map.get(DomainConstants.SELECT_REVISION);
        String sEffectivityDate2   = (String)map.get(DomainConstants.SELECT_EFFECTIVITY_DATE);
        if(!"".equals(sEffectivityDate2))
        {
            sEffectivityDate2 = getFormatDate(sEffectivityDate2);
        }

        String typeIcon2= EngineeringUtil.getTypeIconProperty(context, sObjectTypeName2);
        if (typeIcon2 == null || typeIcon2.length() == 0 )
        {
            typeIcon2 = "iconSmallDefault.gif";
        }

        String strEffDate=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Part.EffectivityDate",strLanguage);
        comparisonHTML.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
        comparisonHTML.append("<tr>");
        comparisonHTML.append("<td class=\"pageBorder\"><img src=\"../common/images/utilSpacer.gif\" width=\"1\" height=\"1\" alt=\"*\"></td>");
        comparisonHTML.append("</tr>");
        comparisonHTML.append("<tr><td>&nbsp;</td></tr>");
        comparisonHTML.append("</table>");

        comparisonHTML.append("<table width=\"100%\" border=\"0\">");
        comparisonHTML.append("<tr>");
        comparisonHTML.append("<td align=\"center\">");
        comparisonHTML.append("<table border=\"0\">");
        comparisonHTML.append("<tr>");
        comparisonHTML.append("<td>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Part1",strLanguage)+":</td>");
        /*Modified below line. bug 293526*/
        comparisonHTML.append("<td><img src=\"../common/images/"+typeIcon1+"\" alt=\"*\"></td>");
        comparisonHTML.append("<td>"+sObjectTypeName+"<br>"); //getTypeI18NString(sObjectTypeName,languageStr)
        comparisonHTML.append("<span class=\"object\">"+sObjectName+"</span><br>");
        comparisonHTML.append(sObjectRevision);
		comparisonHTML.append(" </td>");
        comparisonHTML.append("</tr>");
        comparisonHTML.append("<tr><td colspan=3>").append(strEffDate).append(": ").append(sEffectivityDate).append("</td></tr>");
        comparisonHTML.append("</table>");
        comparisonHTML.append("</td>");
        comparisonHTML.append("<td align=\"center\">");
        comparisonHTML.append("<table border=\"0\">");
        comparisonHTML.append("<tr>");
        comparisonHTML.append("<td>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Part2",strLanguage)+":</td>");
        /*Modified below line. bug 293526*/
        comparisonHTML.append("<td><img src=\"../common/images/"+typeIcon2+"\" alt=\"*\"></td>");
        comparisonHTML.append("<td>"+sObjectTypeName2+"<br>"); //getTypeI18NString(sObjectTypeName2,languageStr)
        comparisonHTML.append("<span class=\"object\">"+sObjectName2+"</span><br>");
        comparisonHTML.append(sObjectRevision2);
        comparisonHTML.append("</td>");
        comparisonHTML.append("</tr>");
        comparisonHTML.append("<tr><td colspan=3>").append(strEffDate).append(": ").append(sEffectivityDate2).append("</td></tr>");
        comparisonHTML.append("</table>");
        comparisonHTML.append("</td>");
        comparisonHTML.append("</tr>");
        comparisonHTML.append("</table>");

        comparisonHTML.append("<table border=\"0\"><tr><td>&nbsp;</td></tr><tr><td><span class=\"object\">"+sSummary+"</span></td></tr></table>");

        StringTokenizer sTok = null;
        // Get hashtables for Reports
        Hashtable comparePrintReport = (Hashtable) info.get("emxCompare");
        Hashtable commonPrintReport  = (Hashtable) info.get("emxCommon");
        Hashtable unique1PrintReport = (Hashtable) info.get("emxUnique1");
        Hashtable unique2PrintReport = (Hashtable) info.get("emxUnique2");

        // Determine which report requested, display appropriate detail
        // Comparison Report
        comparisonHTML.append("<table width = \"100%\" border = \"0\" cellpadding=\"1\" cellspacing=\"0\">");
        comparisonHTML.append("<tr>");
        comparisonHTML.append("<th rowspan=2 width=\"10%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.FindNumber",strLanguage)+"</b></th>");
        comparisonHTML.append("<th rowspan=2 width=\"5%\" ><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</b></th>");
        comparisonHTML.append("<th rowspan=2 width=\"20%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</b></th>");
        comparisonHTML.append("<th rowspan=2 width=\"5%\" ><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Rev",strLanguage)+"</b></th>");
        comparisonHTML.append("<th rowspan=2 width=\"9%\" ><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.ReferenceDesignator",strLanguage)+"</b></th>");
        comparisonHTML.append("<th rowspan=2 width=\"20%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</b></th>");
        comparisonHTML.append("<!--<th class=\"thheading\" rowspan=2 width=\"8%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Qty",strLanguage)+" 2</b></th>-->");
        comparisonHTML.append("<th colspan=2 width=\"6%\" align=\"center\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Quantity",strLanguage)+"</b></th>");
        comparisonHTML.append("<th colspan=2 width=\"9%\" align=\"center\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Differences",strLanguage)+"</b></th>");
        comparisonHTML.append("</tr>");

        comparisonHTML.append("<tr>");
        comparisonHTML.append("<th align=\"center\" nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Part1",strLanguage)+"</th>");
        comparisonHTML.append("<th align=\"center\" nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Part2",strLanguage)+"</th>");
        comparisonHTML.append("<th align=\"center\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Item",strLanguage)+"</th>");
        comparisonHTML.append("<th align=\"center\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Qty",strLanguage)+"</th>");
        comparisonHTML.append("</tr>");

        // if compare hashtable is not empty extract information and print table
        if(!comparePrintReport.isEmpty())
        {
            Enumeration eKeys = comparePrintReport.keys();
            MapList mapList = new MapList();
            while (eKeys.hasMoreElements())
            {
                String sFNHolder = eKeys.nextElement().toString();


                if (comparePrintReport.containsKey(sFNHolder))
                {
                    // get values from hashtable to print - first from the key and then the entry in the table
                    sTok = new StringTokenizer(sFNHolder,"|",false);
                    String sValue = (String) comparePrintReport.get(sFNHolder);
                    sTok = new StringTokenizer(sValue,"|",false);

                    // extract information for printing - part name,revision, reference designator, description, quantity

                    String sType = sTok.nextToken();

                    String typeIcon3 = EngineeringUtil.getTypeIconProperty(context, sType);
                    if (typeIcon3 == null || typeIcon3.length() == 0 )
                    {
                        typeIcon3 = "iconSmallDefault.gif";
                    }

                    /*Modified below line. bug 293526*/
                    String sName = "<table cellspacing=\"1\" cellpadding=\"1\"><tr><td><img src=\"../common/images/"+typeIcon3+"\" border=0 alt=\"*\"></td><td>"+sTok.nextToken()+"</td></tr></table>";
                    String sRevision = sTok.nextToken();
                    String sFN = sTok.nextToken();
                    String sRD = sTok.nextToken();
                    String sPD = sTok.nextToken();
                    String sQty = sTok.nextToken();
                    String sQty2 = sTok.nextToken();
                    String sItemDiff = sTok.nextToken();
                    String sQtyDiff = sTok.nextToken();

                    // replace the <BR> for any newline in the description
                    StringTokenizer token=new StringTokenizer(sPD,"\n");
                    String tempDescription="";
                    while(token.hasMoreTokens())
                    {
                        if(!"".equals(tempDescription))
                        {
                            tempDescription=tempDescription+"<BR>";//It should not add new Line for first Token
                        }
                        tempDescription=tempDescription+token.nextToken();
                    }
                    sPD = tempDescription;

                    HashMap summaryMap = new HashMap();
                    summaryMap.put("sFN",sFN);
                    summaryMap.put("sType",sType);
                    summaryMap.put("sName",sName);
                    summaryMap.put("sRevision",sRevision);
                    summaryMap.put("sRD",sRD);
                    summaryMap.put("sPD",sPD);
                    summaryMap.put("sQty",sQty);
                    summaryMap.put("sQty2",sQty2);
                    summaryMap.put("sItemDiff",sItemDiff);
                    summaryMap.put("sQtyDiff",sQtyDiff);
                    mapList.add(summaryMap);
                } // end of if for hashtable containing key
            } // end of while for eKeys has more elements

      // Modified for Bug 317840
      mapList.sort("sFN","ascending","String");
            HashMap summaryMap;
            for(Iterator itr=mapList.iterator();itr.hasNext();)
            {
                summaryMap = (HashMap)itr.next();
                comparisonHTML.append("<tr>");
                comparisonHTML.append("<td>"+summaryMap.get("sFN")+" &nbsp;</td>");
                comparisonHTML.append("<td>"+summaryMap.get("sType")+"</td>");
                comparisonHTML.append("<td>"+summaryMap.get("sName")+"</td>");
                comparisonHTML.append("<td>"+summaryMap.get("sRevision")+"</td>");
                comparisonHTML.append("<td>"+summaryMap.get("sRD")+" &nbsp;</td>");
                comparisonHTML.append("<td>"+summaryMap.get("sPD")+" &nbsp;</td>");
                comparisonHTML.append("<td>"+summaryMap.get("sQty")+" &nbsp;</td>");
                comparisonHTML.append("<td>"+summaryMap.get("sQty2")+" &nbsp;</td>");
                comparisonHTML.append("<td>"+summaryMap.get("sItemDiff")+" &nbsp;</td>");
                comparisonHTML.append("<td>"+summaryMap.get("sQtyDiff")+" &nbsp;</td>");
                comparisonHTML.append("</tr>");
            }
            comparisonHTML.append("</table>");
            // end of if for table empty
        }
        else
        {
            comparisonHTML.append("<table align = \"center\" width = \"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
            comparisonHTML.append("<tr>");
            comparisonHTML.append("<td align=center>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Nocomparisonstodisplay",strLanguage)+" </td>");
            comparisonHTML.append("</tr>");
            comparisonHTML.append("</table>");
        } // end of else for table empty
        // end CompRpt If

        // Displaying Part1 Unique Components.
        comparisonHTML.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
        comparisonHTML.append("<tr>");
        comparisonHTML.append("<td>&nbsp;</td>");
        comparisonHTML.append("</tr>");
        comparisonHTML.append("</table>");

        comparisonHTML.append("<table border=\"0\"><tr><td><span class=\"object\">"+sPart1UniqueComponents+"</span></td></tr></table>&nbsp;");
        comparisonHTML.append("<table width=\"100%\" border=\"0\" cellpadding=\"1\" cellspacing=\"0\">");
        comparisonHTML.append("<tr>");
        comparisonHTML.append("<th width=\"10%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</b></th>");
        comparisonHTML.append("<th width=\"24%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</b></th>");
        comparisonHTML.append("<th width=\"5%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Rev",strLanguage)+"</b></th>");
        comparisonHTML.append("<th width=\"40%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</b></th>");
        comparisonHTML.append("<th width=\"21%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Quantity",strLanguage)+"</b></th>");
        comparisonHTML.append("</tr>");

        // if compare hashtable is not empty extract information and print table
        if(!unique1PrintReport.isEmpty())
        {
            Enumeration eKeys = unique1PrintReport.keys();
            while (eKeys.hasMoreElements())
            {
                String sKey = eKeys.nextElement().toString();
                // get values from hashtable to print
                String sValue = (String) unique1PrintReport.get(sKey);
                sTok = new StringTokenizer(sKey+"|"+sValue,"|",false);
                // extract information for printing - description, quantity
                String sType = sTok.nextToken();

                String typeIcon4 = EngineeringUtil.getTypeIconProperty(context, sType);
                if (typeIcon4 == null || typeIcon4.length() == 0 )
                {
                    typeIcon4 = "iconSmallDefault.gif";
                }

                String sName = sTok.nextToken();
                String sRev = sTok.nextToken();
                String sDescription = sTok.nextToken();
                String sQty = sTok.nextToken();

                comparisonHTML.append("<tr>");
                comparisonHTML.append("<td> "+sType+"&nbsp </td>");
                comparisonHTML.append("<td> <table cellspacing=\"1\" cellpadding=\"1\"><tr><td><img src=\"../common/images/"+typeIcon4+"\" border=\"0\" alt=\"*\"></td><td>"+sName+"</td></tr></table>&nbsp </td>");
                comparisonHTML.append("<td>"+ sRev+"&nbsp </td>");
                comparisonHTML.append("<td>"+ sDescription+"&nbsp </td>");
                comparisonHTML.append("<td>"+ sQty+"&nbsp </td>");
                comparisonHTML.append("</tr>");

            } // end of while for eKeys has more elements
            comparisonHTML.append("</table>");
            // end of if for table empty
        }
        else
        {
            comparisonHTML.append("</table>");
            comparisonHTML.append("<table align = \"center\" width = \"100%\" border= \"0\" cellpadding=\"0\" cellspacing=\"0\">");
            comparisonHTML.append("<tr>");
            comparisonHTML.append("<td align=center>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Nouniqueitemsforpart1todisplay",strLanguage)+"</td>");
            comparisonHTML.append("</tr>");
            comparisonHTML.append("</table>");
        } // end of else for table empty
        // end of if for Pt1Unique
        //Displaying Part2 Unique Components
        comparisonHTML.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
        comparisonHTML.append("<tr>");
        comparisonHTML.append("<td>&nbsp;</td>");
        comparisonHTML.append("</tr>");
        comparisonHTML.append("</table>");

        comparisonHTML.append("<table border=\"0\"><tr><td><span class=\"object\">"+sPart2UniqueComponents+"</span></td></tr></table>&nbsp;");
        comparisonHTML.append("<table width=\"100%\" border=\"0\" cellpadding=\"1\" cellspacing=\"0\">");
        comparisonHTML.append("<tr>");
        comparisonHTML.append("<th width=\"10%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</b></th>");
        comparisonHTML.append("<th width=\"24%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</b></th>");
        comparisonHTML.append("<th width=\"5%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Rev",strLanguage)+"</b></th>");
        comparisonHTML.append("<th width=\"40%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</b></th>");
        comparisonHTML.append("<th width=\"21%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Quantity",strLanguage)+"</b></th>");
        comparisonHTML.append("</tr>");

        // if compare hashtable is not empty extract information and print table
        if(!unique2PrintReport.isEmpty())
        {
            Enumeration eKeys = unique2PrintReport.keys();
            while (eKeys.hasMoreElements())
            {
                String sKey = eKeys.nextElement().toString();
                // get values from hashtable to print
                String sValue = (String) unique2PrintReport.get(sKey);
                sTok = new StringTokenizer(sKey+"|"+sValue,"|",false);
                // extract information for printing - description, quantity
                String sType = sTok.nextToken();

                String typeIcon5 = EngineeringUtil.getTypeIconProperty(context, sType);
                if (typeIcon5 == null || typeIcon5.length() == 0 )
                {
                    typeIcon5 = "iconSmallDefault.gif";
                }

                String sName = sTok.nextToken();
                String sRev = sTok.nextToken();
                String sDescription = sTok.nextToken();
                String sQty1 = sTok.nextToken();

                comparisonHTML.append("<tr>");
                comparisonHTML.append("<td> "+sType+"&nbsp </td>"); //getTypeI18NString(sType,languageStr)
                comparisonHTML.append("<td> <table cellspacing=\"1\" cellpadding=\"1\"><tr><td><img src=\"../common/images/"+typeIcon5+"\" border=\"0\" alt=\"*\"></td><td>"+sName+"</td></tr></table>&nbsp </td>");
                comparisonHTML.append("<td> "+sRev+"&nbsp </td>");
                comparisonHTML.append("<td> "+sDescription+"&nbsp </td>");
                comparisonHTML.append("<td> "+sQty1+"&nbsp </td>");
                comparisonHTML.append("</tr>");
            } // end of while for eKeys has more elements
            comparisonHTML.append("</table>");
             // end of if for table empty
        }
        else
        {
            comparisonHTML.append("</table>");
            comparisonHTML.append("<table align = \"center\" width = \"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
            comparisonHTML.append("  <tr>");
            comparisonHTML.append("    <td align=center>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Nouniqueitemsforpart2todisplay",strLanguage)+"</td>");
            comparisonHTML.append("  </tr>");
            comparisonHTML.append("</table>");
        } // end of else for table empty
        // end Pt2Unique If
        //Displaying Common Components
        comparisonHTML.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
        comparisonHTML.append("  <tr>");
        comparisonHTML.append("    <td>&nbsp;</td>");
        comparisonHTML.append("  </tr>");
        comparisonHTML.append("</table>");

        comparisonHTML.append("<table border=\"0\"><tr><td><span class=\"object\">"+sCommonComponents+"</span></td></tr></table>&nbsp;");

        comparisonHTML.append("    <table width=\"100%\" border=\"0\" cellpadding=\"1\" cellspacing=\"0\">");
        comparisonHTML.append("    <tr>");
        comparisonHTML.append("      <th rowspan=\"2\" width=\"10%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</b></th>");
        comparisonHTML.append("      <th rowspan=\"2\" width=\"24%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</b></th>");
        comparisonHTML.append("      <th rowspan=\"2\" width=\"5%\" ><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Rev",strLanguage)+"</b></th>");
        comparisonHTML.append("      <th rowspan=\"2\" width=\"40%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</b></th>");
        comparisonHTML.append("      <th colspan=\"2\" width=\"21%\"><b>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Quantity",strLanguage)+"</b></th>");
        comparisonHTML.append("    </tr>");
        comparisonHTML.append("    <tr>");
        comparisonHTML.append("      <th align=\"center\" nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Part1",strLanguage)+"</th>");
        comparisonHTML.append("      <th align=\"center\" nowrap>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Part2",strLanguage)+"</th>");
        comparisonHTML.append("    </tr>");


        // if compare hashtable is not empty extract information and print table
        if(!commonPrintReport.isEmpty())
        {
            Enumeration eKeys = commonPrintReport.keys();
            while (eKeys.hasMoreElements())
            {
                String sKey = eKeys.nextElement().toString();
                // get values from hashtable to print
                String sValue = (String) commonPrintReport.get(sKey);
                sTok = new StringTokenizer(sKey+"|"+sValue,"|",false);
                // extract information for printing - description, quantity
                String sType = sTok.nextToken();
                String typeIcon6 = EngineeringUtil.getTypeIconProperty(context, sType);
                if (typeIcon6 == null || typeIcon6.length() == 0 )
                {
                    typeIcon6 = "iconSmallDefault.gif";
                }

                String sName = sTok.nextToken();
                String sRev = sTok.nextToken();
                String sDescription = sTok.nextToken();
                String sQty = sTok.nextToken();
                String sQty2 = sTok.nextToken();

                comparisonHTML.append("              <tr>");
                comparisonHTML.append("                <td> "+sType+"&nbsp </td>");
                comparisonHTML.append("                <td>");
                comparisonHTML.append("                  <table cellspacing=\"1\" cellpadding=\"1\">");
                comparisonHTML.append("                    <tr>");
                comparisonHTML.append("                      <td><img src=\"../common/images/"+typeIcon6+"\" border=\"0\" alt=\"*\"></td>");
                comparisonHTML.append("                      <td>"+sName+"</td>");
                comparisonHTML.append("                    </tr>");
                comparisonHTML.append("                  </table>&nbsp");
                comparisonHTML.append("                </td>");
                comparisonHTML.append("                <td>"+sRev+"&nbsp </td>");
                comparisonHTML.append("                <td>"+sDescription+"&nbsp </td>");
                comparisonHTML.append("                <td>"+sQty+"&nbsp </td>");
                comparisonHTML.append("                <td>"+sQty2+"&nbsp </td>");
                comparisonHTML.append("              </tr>");
            } // end of while for eKeys has more elements
            comparisonHTML.append("</table>");
            // end of if for table empty
        }
        else
        {
            comparisonHTML.append("            </table>");
            comparisonHTML.append("            <table align = \"center\" width = \"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
            comparisonHTML.append("              <tr>");
            comparisonHTML.append("                <td align=center>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.Nocommonitemsfortodisplay",strLanguage)+"</td>");
            comparisonHTML.append("              </tr>");
            comparisonHTML.append("             </table>");
        } // end of else for table empty
        /* End of Common Components */
        return comparisonHTML.toString();
    }

  /**
   * Creates the ECO Html summary report.
   * This method has been replicated with createSummaryReport to facilitate PDF report as a Patch.
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds the following input arguments:
   * 0 - String containing object id.
   * @return int tells whether PDF is generated and checked into the object. 0-success 1-failure.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public String createHtmlReport(Context context,String args[]) throws Exception {

  if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
      }

    /* Arguments are not packed */
      String objectId = args[0];

    StringBuffer summaryReport = new StringBuffer(512);
    ECO ecoObj = null;

    try
    {
       String strLanguage = context.getSession().getLanguage();
       ecoObj = new ECO(objectId);
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
       summaryReport.append("<tr><td class=\"pageHeader\"><h1>"+ecoObj.getInfo(context,SELECT_NAME)+":&nbsp;"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.SummaryReport",strLanguage)+"</h1></td>");
       summaryReport.append("<td class=\"pageSubtitle\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Generated",strLanguage)+" "+dateAndTime+"</td></tr>");
       summaryReport.append("</table>");
       summaryReport.append("</div>");

       // Basic Attributes section display
       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Attributes",strLanguage)+"</h2></td></tr></table>");
       summaryReport.append(getBasicInfo(context,args));

       // Approvals Display
       Boolean boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.Approvals");
       if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.Approvals",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getApprovals(context,args));
       }
       // Routes Display
       boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.Routes");
       if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Routes",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getRoutes(context,args));
       }
       //Routes Display
       boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.Routes");
       if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Tasks",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECOTasks(context,args));
       }


      //    Affected Items ( Parts ) Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.AffectedItems");
      if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECOAffectedParts",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECOAffectedItemsSummaryDetails(context,args));
      }

     //    affected items (Specifications) Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.AffectedItems");
      if(boolObj.booleanValue()) {
          summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECOAffectedSpecs",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECOSpecifications(context,args));
      }
      //Assingees Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.Assignees");
      if(boolObj.booleanValue()) {
          summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECOAssingees",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getAssigneesOfECO(context,args));
      }

      // Related ECRs Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.RelatedECRs");
      if(boolObj.booleanValue()) {
          summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECORelatedECRs",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getRelatedECRs(context,args));
      }

      //Related EBOM Markups s Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.RelatedMarkups");
      if(boolObj.booleanValue()) {
          summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECORelatedMarkups",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECORelatedBOMMarups(context,args));
      }
      //    ReferenceDocuments
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.ReferenceDocuments");
      if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ReferenceDocuments",strLanguage)+"</h2></td></tr></table>");
       summaryReport.append(getECORelatedReferenceDocuments(context,args));
      }
      //for bug starts
      //    Related ResolvedItems Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.ResolvedItems");
      if(boolObj.booleanValue()) {
         summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ResolvedItems",strLanguage)+"</h2></td></tr></table>");
         summaryReport.append(getECORelatedResolvedItems(context,args));
      }
      //body and html Close tags
      summaryReport.append("</html>");
    }
    catch (Exception e)
    {
      throw e;
  }
  return summaryReport.toString();

   }
   /**
   * Creates the ECO summary report.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds the following input arguments:
   * 0 - String containing object id.
   * @return int tells whether PDF is generated and checked into the object. 0-success 1-failure.
   * @throws Exception if the operation fails.
   * @since 10.5.
   */
   public  int createSummaryReport(Context context,String args[]) throws Exception {

  if (args == null || args.length < 1) {
          throw (new IllegalArgumentException());
    }

  /* Arguments are not packed */
    //String objectId = args[0];
   String summaryReport = "";

try
   {
       summaryReport=createHtmlReport(context,args);

  }
  catch (Exception e)
  {
    throw e;
  }
        int pdfGenerated = renderPDFFile(context,args,summaryReport);
        if(pdfGenerated!=0)
        {
             emxContextUtil_mxJPO.mqlError(context,
                                              EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.SummaryReport.NoCheckIn.ErrorMessage",
                                              context.getSession().getLanguage()));
                return 1;
        }
        else
        {
            return 0;
        }

   }

   /**
     * Changes the Date format.
     *
     * @param actiondate holds the date in the supplied format.
     * @return String Formatted date.
     * @since 10.5.
    */
   public String getFormatDate(String actiondate)  {

	      String[] monthDesc = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	      Date inputDateFormat = eMatrixDateFormat.getJavaDate(actiondate);
	      int clientmonth= inputDateFormat.getMonth();
	      String smonth = monthDesc[clientmonth] +" ";

	      actiondate = actiondate.substring(actiondate.indexOf('/')+1,actiondate.trim().length());
	      actiondate = actiondate.replace('/',',');
	      actiondate = smonth+actiondate;
	      return actiondate ;
  }



    /**
     * Checks the view mode of the web form display.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * mode - a String containing the mode.
     * @return Object - boolean true if the mode is view
     * @throws Exception if operation fails
     * @since EngineeringCentral 10.6 - Copyright (c) 2004, MatrixOne, Inc.
     */

    public Object checkViewMode(Context context, String[] args)
        throws Exception
    {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String strMode = (String) programMap.get("mode");
        Boolean isViewMode = Boolean.FALSE;

        // check the mode of the web form.
        if( (strMode == null) || (strMode != null && ("null".equals(strMode) || "view".equalsIgnoreCase(strMode) || "".equals(strMode))) ) {
            isViewMode = Boolean.TRUE;
        }

        return isViewMode;
    }

    /**
    * Get connected object for the task
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds HashMap containing the following entries:
    * objectList - a MapList of object information.
    * paramList - a Map of parameter values
    * objectId - a String of the Part id.
    * @return vector
    * @throws Exception if the operation fails.
    * @since 10.6
    */
    public Vector getConnectedObjects(Context context, String[] args)throws Exception
    {

        String sRelRouteTask           = PropertyUtil.getSchemaProperty(context, "relationship_RouteTask");
        String sRelObjectRoute         = PropertyUtil.getSchemaProperty(context, "relationship_ObjectRoute");
        String routedContentType       = "from[" + sRelRouteTask + "].to.to[" + sRelObjectRoute + "].from.type";
        String routedContentName       = "from[" + sRelRouteTask + "].to.to[" + sRelObjectRoute + "].from.name";
        String routedContentRev        = "from[" + sRelRouteTask + "].to.to[" + sRelObjectRoute + "].from.revision";

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objList=(MapList)programMap.get("objectList");
        Vector connectedObjectsVector = new Vector(objList.size());

        Iterator objItr = objList.iterator();
        while(objItr.hasNext())
        {
            Map objMap = (Map)objItr.next();
            StringBuffer conTypeBuf = new StringBuffer();
            conTypeBuf.append((String)objMap.get(routedContentType));
            conTypeBuf.append(' ');
            conTypeBuf.append((String)objMap.get(routedContentName));
            conTypeBuf.append(' ');
            conTypeBuf.append((String)objMap.get(routedContentRev));
            connectedObjectsVector.addElement(conTypeBuf.toString());
        }

        return connectedObjectsVector ;
    }

    /**
    * function dealt with disabling of checkbox when task is complete
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds HashMap containing the following entries:
    * objectList - a MapList of object information.
    * @return vector
    * @throws Exception if the operation fails.
    * @since 10.6
    */
    public Vector displayCheckBoxColumn(Context context, String[] args)
        throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList objectList = (MapList)programMap.get("objectList");

        Map objectMap = null;
        Vector checkBoxCol = null;

        String policyTask =     PropertyUtil.getSchemaProperty(context, "policy_InboxTask");
        String stateComplete =  FrameworkUtil.lookupStateName(context, policyTask, "state_Complete");

        int objectListSize = 0 ;
        if(objectList != null && (objectListSize = objectList.size()) > 0)
        {
            checkBoxCol = new Vector(objectListSize);
            String presentState = "";
            for(int i=0; i< objectListSize; i++)
            {
                objectMap = (Map) objectList.get(i);
                presentState = (String)objectMap.get(SELECT_CURRENT);
                if(presentState.equals(stateComplete))
                {
                    checkBoxCol.add("false");
                }
                else
                {
                    checkBoxCol.add("true");
                }
            }
        }

        return checkBoxCol;
    }

     /* showStatusGif - gets the status gif to be shown in the column of the Task Summary table
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - objectList MapList
     * @returns Object of type Vector
     * @throws Exception if the operation fails
     * @since AEF Rossini
     * @grade 0
     */
    public Vector showStatusGif(Context context, String[] args)
        throws Exception
    {
        try
        {
          HashMap programMap = (HashMap) JPO.unpackArgs(args);
          MapList objectList = (MapList)programMap.get("objectList");
          String sAttrScheduledCompletionDate   = PropertyUtil.getSchemaProperty(context,"attribute_ScheduledCompletionDate");
          String selTaskCompletedDate           = PropertyUtil.getSchemaProperty(context,"attribute_ActualCompletionDate");
          String policyTask                     = PropertyUtil.getSchemaProperty(context,"policy_InboxTask");
          String strAttrCompletionDate ="attribute["+sAttrScheduledCompletionDate+"]";
          String strAttrTaskCompletionDate ="attribute["+selTaskCompletedDate+"]";
          Vector enableCheckbox = new Vector();
          String stateComplete = FrameworkUtil.lookupStateName(context, policyTask, "state_Complete");
          Date dueDate   = null;
          Date curDate = new Date();
          String statusImageString = "";
          String statusColor= "";
          Iterator objectListItr = objectList.iterator();
          while(objectListItr.hasNext())
          {
              Map objectMap = (Map) objectListItr.next();
              String taskState = (String) objectMap.get(DomainObject.SELECT_CURRENT);
              String taskDueDate = (String)objectMap.get(strAttrCompletionDate);
              String taskCompletedDate         = (String)objectMap.get(strAttrTaskCompletionDate);
              if(!"".equals(taskState))
              {
                if(taskDueDate == null || "".equals(taskDueDate)){
                  dueDate = new Date();
                }
                else{
                  dueDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(taskDueDate);
                }
                if(!taskState.equals(stateComplete)){
                  if(dueDate != null && curDate.after(dueDate)){
                    statusColor = "Red";
                  }
                  else{
                    statusColor = "Green";
                  }
                }
                else{
                  Date actualCompletionDate = com.matrixone.apps.domain.util.eMatrixDateFormat.getJavaDate(taskCompletedDate);
                  if(dueDate != null && actualCompletionDate.after(dueDate)){
                    statusColor = "Red";
                  }
                  else{
                    statusColor = "Green";
                  }
               }
                if("Red".equals(statusColor)){
                    statusImageString = "<img border='0' src='../common/images/iconStatusRed.gif' name='red' id='red' alt='*' />";
                }else if("Green".equals(statusColor)){
                    statusImageString = "<img border='0' src='../common/images/iconStatusGreen.gif' name='green' id='green' alt='*' />";
                }else{
                    statusImageString="&nbsp;";

                }
                enableCheckbox.add(statusImageString);
            }

    }
       return enableCheckbox;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

/**
   *
   * Get the HTML string to view the EBOM Markup
   * @param context the Matrix Context
   * @param args no args needed for this method
   * @returns HTML string
   * @throws Exception if the operation fails
   * @since EC 10.6
   */

  public Vector getEBOMMarkupEditURL(Context context,String[] args) throws Exception
  {
      Vector imageList=new Vector();
      try
      {

          HashMap programMap=(HashMap)JPO.unpackArgs(args);

          MapList relBusObjPageList = (MapList)programMap.get("objectList");


          HashMap paramMap=(HashMap)programMap.get("paramList");
          String reportFormat = (String)paramMap.get("reportFormat");
          String objIdArray[] = new String[relBusObjPageList.size()];
          String partId = "";
          String stateArray[] = new String[relBusObjPageList.size()];
          if (reportFormat==null || reportFormat.length()==0 || "null".equals(reportFormat))
          {
              if(relBusObjPageList != null )
              {
                  String image="";
                  for(int i=0;i<relBusObjPageList.size();i++)
                  {
                      objIdArray[i]=(String)((Hashtable)relBusObjPageList.get(i)).get("id");
                      stateArray[i]=(String)((Hashtable)relBusObjPageList.get(i)).get("current");
                  }
                  for(int i=0;i<objIdArray.length;i++)
                  {
                      partId = com.matrixone.apps.domain.util.MqlUtil.mqlCommand(context, "expand bus $1 to rel $2 type $3 select bus $4 dump $5",objIdArray[i],PropertyUtil.getSchemaProperty(context,"type_EBOMMarkup"),"Part","id","|");
                      partId = partId.substring(partId.lastIndexOf('|')+1);
                      partId = partId.trim();
                      //modified for the bug 334042
                      if(!stateArray[i].equals("Applied") && !stateArray[i].equals("Approved")  ){
					//till here

                        image ="<a href=\"#\" onClick=window.showModalDialog(\"../engineeringcentral/emxpartEBOMMarkupFS.jsp?sflag=false&mode=ViewEdit&objectId="+partId+"&markupId="+objIdArray[i]+"\",\"dialogHeight:500px;dialogWidth:400px;status:no;\");><img src=\"../common/images/iconActionEdit.gif\"  border=\"0\" alt=\"*\"></a>";

                      }
                      else
                      {
                         image=" ";
                      }
                      imageList.add(image);
                  }
              }
          }
          else
          {
              for(int i=0;i<objIdArray.length;i++)
              {
                  imageList.add(" ");
              }
          }
          return imageList;
      }
      catch(Exception e)
      {
          throw e;
      }
  }

 /**
   * List of EBOM Markups in the Approved/Applied state that are connected to the ECO
   * by the relationship Applied Markup
   * @param context the Matrix Context
   * @param args no args needed for this method
   * @returns Maplist containing IDs of the
   * @throws Exception if the operation fails
   * @since EC 10.6
   */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getApprovedAndAppliedEBOMMarkups(Context context, String args[]) throws Exception
    {
        HashMap programMap = (HashMap)JPO.unpackArgs(args);
        String objectId = (String)programMap.get("objectId");

        MapList ebomMarkups = new MapList();

        //object for holding selectables for object
        StringList objSelects = new StringList();

        //add the properties for output.
        objSelects.add(DomainConstants.SELECT_ID);
        objSelects.add(DomainConstants.SELECT_CURRENT);

        //similarly create a new string list for holding relationship selectables
        StringList relSelects = new StringList();

        short level = 1;

        try
        {
            DomainObject doObj = DomainObject.newInstance(context, objectId);
            ebomMarkups = doObj.getRelatedObjects(context,
                                                  PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup"),
                                                  PropertyUtil.getSchemaProperty(context,"type_EBOMMarkup") + "," + PropertyUtil.getSchemaProperty(context,"type_BOMMarkup"),
                                                  objSelects,
                                                  relSelects,
                                                  false,
                                                  true,
                                                  level,
                                                  null,
                                                  null);
        }
        catch(Exception e)
        {
            throw e;
        }
        return ebomMarkups;
    }

/**
   * Shows the greyed out checkbox for EBOM Markups in Applied State
   * @param context the Matrix Context
   * @param args no args needed for this method
   * @returns Maplist containing IDs of the
   * @throws Exception if the operation fails
   * @since EC 10.6
   */
 public Vector ShowCheckboxforApprovedMarkups(Context context,String[] args) throws Exception
 {
     Vector checkboxVector = new Vector();
     HashMap programMap = (HashMap)JPO.unpackArgs(args);
     MapList objList = (MapList)programMap.get("objectList");

     Iterator objItr = objList.iterator();
     while(objItr.hasNext())
     {
         Map objMap = (Map)objItr.next();
         String markupState = (String)objMap.get(DomainObject.SELECT_CURRENT);
         if(!"Applied".equals(markupState))
         {
             checkboxVector.addElement("true");
         }
         else
         {
             checkboxVector.addElement("false");
         }
     }
     return checkboxVector;

 }

     public static StringList getSubTypes(Context context ,String typeName)
    {
         StringList subTypesList=new StringList();
         if(typeName!=null && !typeName.equals("null") && !typeName.trim().equals(""))
         {
          try
             {
              String subtypes = MqlUtil.mqlCommand(context, "print type $1 select $2 dump $3",typeName,"derivative","|");
              subTypesList = FrameworkUtil.split(subtypes, "|");
             }
             catch(Exception e)
             {}
         }
         return subTypesList;
    }

  /**
   * Get the approval status based on type of Action of Inbox task
   * @param context the Matrix Context
   * @param args no args needed for this method
   * @returns StringList containing Avalable selection for approval status
   * @throws Exception if the operation fails
   * @since EC 10.6-SP1
   */
   public StringList getApprovalStatus(Context context,String[] args) throws Exception
   {
     StringList approvalStatus = new StringList();
     HashMap programMap = (HashMap)JPO.unpackArgs(args);
     MapList objList = (MapList)programMap.get("objectList");

     HashMap paramList=(HashMap)programMap.get("paramList");
     String editTableMode=(String)paramList.get("editTableMode");
     Iterator objItr = objList.iterator();
     //if table is in edit mode then pass the cumbo box for Task which have action as Approve
     if("true".equals(editTableMode))
     {

       int objCount = 0;
       while(objItr.hasNext())
       {
       Map objMap = (Map)objItr.next();

       String objectId=(String)objMap.get((SELECT_ID));
       String current=(String)objMap.get(SELECT_CURRENT);

       DomainObject obj=new DomainObject(objectId);

       StringList selectable=new StringList();
       selectable.add("attribute["+ATTRIBUTE_ROUTE_ACTION+"]");
       selectable.add("attribute["+ATTRIBUTE_APPROVAL_STATUS+"]");


       Hashtable resultMap=(Hashtable)obj.getInfo(context,selectable);


       String action = (String)resultMap.get("attribute["+ATTRIBUTE_ROUTE_ACTION+"]");
       String currentStatus=(String)resultMap.get("attribute["+ATTRIBUTE_APPROVAL_STATUS+"]");


       //if task in complete state do not show the cumbo box.
       if(!current.equals(STATE_INBOX_TASK_COMPLETE))
       {
         //if aaction is approve
         if(action!=null && !"".equals(action) && !"null".equals(action) && "Approve".equals(action))
         {
           StringBuffer option=new StringBuffer();
           AttributeType atType = new AttributeType(ATTRIBUTE_APPROVAL_STATUS);
           atType.open(context);
           StringList optionList=atType.getChoices();
           atType.close(context);

           option.append("<select name=ApprovalStatus"+(objCount++)+">");

          // if we already have value in attribute Approval Status then default selecetion will be that value. Else the default selected value will be none.
           if(currentStatus!=null && !"".equals(currentStatus) && !"null".equals(currentStatus))
           {
             for(int i=0;i<optionList.size(); i++)
             {
               option.append("<option VALUE= \"");
               option.append(optionList.get(i));
               option.append('"');
               if(optionList.get(i).equals(currentStatus)) {
                 option.append(" SELECTED  ");
               }
               option.append('>');
               option.append(optionList.get(i));
               option.append("</option>");
             }
           }
           else
           {
            for(int i=0;i<optionList.size(); i++)
             {
               option.append("<option VALUE= \"");
               option.append(optionList.get(i));
               option.append('"');
               if(optionList.get(i).equals("None")) {
                 option.append(" SELECTED  ");
               }
               option.append('>');
               option.append(optionList.get(i));
               option.append("</option>");
             }
           }
           option.append("</select>");
           approvalStatus.add(option.toString());

         }
         else
         {//if task do not have action as Approve do not show anything
           approvalStatus.add("");
         }
       }
       else
       {//if task is in complete state show only not editable current Approval status
         approvalStatus.add(currentStatus);
       }
     }
   }
   else
   {//if task in view mode show the attribute value
     while(objItr.hasNext())
     {
       Map objMap = (Map)objItr.next();
       String objectId=(String)objMap.get((SELECT_ID));
       DomainObject obj=new DomainObject(objectId);
       obj.open(context);
       String status=(String)obj.getInfo(context,"attribute["+ATTRIBUTE_APPROVAL_STATUS+"]");
       obj.close(context);
       approvalStatus.addElement(status);
     }
   }
   return approvalStatus;
 }

  /**
   *Update the Aproval Status of Inbox task
   * @param context the Matrix Context
   * @param args no args needed for this method
   * @returns booloen
   * @throws Exception if the operation fails
   * @since EC 10.6-SP1
   */
  public Boolean updateApprovalStatus(Context context, String[] args) throws Exception
  {

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap paramMap = (HashMap)programMap.get("paramMap");

    String objectId  = (String)paramMap.get("objectId");
    // get the value selected for field Approval Status
    String newApprovalStatus = (String) paramMap.get("New Value");

    DomainObject obj=new DomainObject(objectId);
    StringList selectable=new StringList();

    selectable.add("attribute["+ATTRIBUTE_APPROVAL_STATUS+"]");
    selectable.add(SELECT_CURRENT);

    Hashtable resultMap=(Hashtable)obj.getInfo(context,selectable);
    String current=(String)resultMap.get(SELECT_CURRENT);
    String currentApprovalStatus = (String)resultMap.get("attribute["+ATTRIBUTE_APPROVAL_STATUS+"]");
   //if task is in complete state do not update the value
    if(!current.equals(STATE_INBOX_TASK_COMPLETE))
    {
      if( newApprovalStatus!=null && newApprovalStatus.length()>0 )
      {
        //if new value is seleted in edit page then only modify the attribute
        if(!newApprovalStatus.equals(currentApprovalStatus))
        {
          obj.setAttributeValue(context, ATTRIBUTE_APPROVAL_STATUS, newApprovalStatus);
        }
      }
    }
    return Boolean.TRUE;
  }

 /**
     * Provides the AffectedParts for the ECO.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: 0 - String containing
     *            object id.
     * @return a MapList of the Revised Parts.
     * @throws Exception
     *             if the operation fails.
     * @since 10.7Sp1.
     */
    public MapList getAffectedParts(Context context, String[] args)
            throws Exception {
        String objectId = args[0];
        MapList revisedPartsList = new MapList();

        try {
            ECO ecoObj = new ECO(objectId);
            StringList selectStmts = new StringList(1);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_TYPE);
            selectStmts.addElement(SELECT_NAME);

            String sCreatedtoDate = RANGE_FOR_REVISE;
            String whereclause = "(attribute[" + PropertyUtil.getSchemaProperty(context,"attribute_RequestedChange") + "]==\"" +
                    ""+sCreatedtoDate+"\")" ;
            revisedPartsList=ecoObj.getRelatedObjects(context,
                    RELATIONSHIP_AFFECTED_ITEM, "*", selectStmts,
                    null, false, true, (short) 1, null, whereclause);


        } catch (FrameworkException Ex) {
            throw Ex;
        }

        return revisedPartsList;
    }

    /**
     * Revises the affected item connected to ECO
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds the following input arguments: 0 - String containing
     *            object id.
     * @return a MapList of the Revised Parts.
     * @throws Exception
     *             if the operation fails.
     * @since 10.7 Sp1
     */

    public int reviseAffectedItems(Context context, String[] args)
            throws Exception {
		try {
    		String sUnApprovedList = FrameworkProperties.getProperty(context, "emxEngineeringCentral.Check.PartVersion");
            String types = args[1];
            StringList typeList = FrameworkUtil.split(types,",");
            boolean bSpec = true;
            if(!"TRUE".equals(sUnApprovedList)){
                String objectId = args[0];//eco object id
                MapList affectedPartsList = new MapList(); //affected part map list
                MapList ebomMarkupList = new MapList();   // markup list connected to affted parts
                Part affectedPart = null; //affected part
                String strMarkupId ="";
                Map mapAffectedParts=null;

        		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
        		String strRelAppliedMarkup = PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup");

                ECO ecoObj = new ECO(objectId);
                //get the affedted parts connected to ECO
                affectedPartsList = getAffectedParts(context, args);
                HashMap mapIndirectAffectedItems = getIndirectAffectedItemsInMap(context, objectId, false);
                Iterator mapItr = affectedPartsList.iterator();//affected Part Iterator

                String type;
                String name;
                String mapKey;

                while (mapItr.hasNext()) {
                    mapAffectedParts = (Map) mapItr.next();
                    affectedPart = (Part) DomainObject.newInstance(context,
                            DomainConstants.TYPE_PART, DomainConstants.ENGINEERING);//affeted part instance
                    affectedPart.setId(((String) mapAffectedParts.get(SELECT_ID)));// set affected part id

                    type   = (String) mapAffectedParts.get(SELECT_TYPE);
                    name   = (String) mapAffectedParts.get(SELECT_NAME);
                    mapKey = type + SYMB_PIPE + name;

    				String strNewPartId = (String) mapIndirectAffectedItems.get(mapKey);

    				if (strNewPartId == null) {
	                    if(affectedPart.isLastRevision(context)) {
	                    	bSpec = true;
	                        //get Markups attached to affected part
	                        StringList selectStmts = new StringList(1);
	                        selectStmts.addElement(SELECT_ID);

	                        StringList selectRelStmts = new StringList(1);
	                        selectRelStmts.addElement(DomainRelationship.SELECT_ID);
	                        ebomMarkupList=affectedPart.getRelatedObjects(context,
	                                DomainConstants.RELATIONSHIP_EBOM_MARKUP, "*", selectStmts,
	                                selectRelStmts, true, true, (short) 1, "current == Approved && to[" + strRelAppliedMarkup + "] == False", null);

	                        DomainObject newRevPart = null;
	                        //Start : IR-011850 : This is added to revise the integration type objects without creating autoname objects.

	                        if (affectedPart.isKindOf(context, CommonDocument.TYPE_DOCUMENTS)) {
		                        Iterator itr = typeList.iterator();
		                        while(itr.hasNext()){
		                            String typeSpec = (String)itr.next();
		                            String typeSpecification = PropertyUtil.getSchemaProperty(context,typeSpec);
		                            if(affectedPart.isKindOf(context,typeSpecification)){
		                                bSpec = false;
		                                break;
		                            }
		                        }
	                        }

	                        //Modified the condition for IR-011850 to revise the integration type objects without creating autoname objects.
	                        if (affectedPart.isKindOf(context,CommonDocument.TYPE_DOCUMENTS)&& bSpec){
	                            CommonDocument docItem = new CommonDocument(affectedPart);

	                            newRevPart = docItem.revise(context, true);
	                        } else {
	                            newRevPart = new DomainObject(affectedPart.revisePart(context, null, getDefaultVault(context, affectedPart), true));
	                        }

	                        Iterator markupMapItr = ebomMarkupList.iterator();//Markup iterator
	                        EBOMMarkup markup;
	                        ContextUtil.pushContext(context);
	                        while (markupMapItr.hasNext()) {
	                            Map marukupMap = (Map) markupMapItr.next();
	                            strMarkupId = (String) marukupMap.get(DomainConstants.SELECT_ID);
	                            markup = new EBOMMarkup(strMarkupId);
	                            //connect the markup with eco
	                            DomainRelationship.connect(context, ecoObj, PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup"), markup);
	                        }
	                        ContextUtil.popContext(context);

	                        DomainRelationship doRelNew = DomainRelationship.connect(context, ecoObj, RELATIONSHIP_AFFECTED_ITEM, newRevPart);
	        				doRelNew.setAttributeValue(context, strAttrAffectedItemCategory, "Indirect");
	                    } else {
	                        String sRelAffectedItem   = PropertyUtil.getSchemaProperty(context,"relationship_AffectedItem");
	                        StringList objECOIds        = ecoObj.getInfoList(context, "from["+sRelAffectedItem+"].to.id");
	                        StringList busSelects = new StringList();
	                        busSelects.add(SELECT_ID);
	                        busSelects.add(SELECT_REVISION);
	                        busSelects.add(SELECT_CURRENT);
	                        MapList revisionsList = affectedPart.getRevisions(context, busSelects, false);
	                        revisionsList.sort(SELECT_REVISION, "ascending", "Integer");
	                        Iterator itr = revisionsList.iterator();
	                        while(itr.hasNext()){
	                            Map tempMap = (Map)itr.next();
	                            String partCurrent = (String)tempMap.get(SELECT_CURRENT);
	                            if(!(DomainConstants.STATE_PART_RELEASE.equalsIgnoreCase(partCurrent) || DomainConstants.STATE_PART_OBSOLETE.equalsIgnoreCase(partCurrent))) {
	                                String tempObjId = (String)tempMap.get(SELECT_ID);
	                                DomainObject nextObj = new DomainObject(tempObjId);
	                                if(objECOIds.contains(tempObjId))
	                                    break;
	                                else if(!objECOIds.contains(tempObjId)){
	                                DomainRelationship doRelNew1 = DomainRelationship.connect(context, ecoObj, RELATIONSHIP_AFFECTED_ITEM, nextObj);
	                                doRelNew1.setAttributeValue(context, strAttrAffectedItemCategory, "Indirect");
	                                break;
	                                }
	                            }
	                        }
	                    }
	                }
                }
    		}
        } catch (FrameworkException ex) {
            throw ex;
        }
        return 0;
    }

  /**
   * Display Related Part Name in the WebForm
   * @param context the Matrix Context
   * @param args no args needed for this method
   * @returns String
   * @throws Exception if the operation fails
   * @since EC X3
   */
        public String  displayRelatedPartName (Context context, String[] args) throws Exception {
         StringBuffer strBuilder = new StringBuffer(3);
         try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            String strObjId = (String) requestMap.get("objectId");
            // fix done for bug # 342956 starts
            if (strObjId != null && strObjId.length() > 0)
            {
            setId(strObjId);
            DomainObject dPart = DomainObject.newInstance(context, strObjId);
           // getting the value
		    String sPartName =  dPart.getInfo(context,DomainObject.SELECT_NAME);
		    String sPartRevision =  dPart.getInfo(context,DomainObject.SELECT_REVISION);
            //displaying the part name and revision
		    strBuilder.append(sPartName);
		    strBuilder.append(" : Revision ");
		    strBuilder.append(sPartRevision);
			}
			// fix done for bug # 342956 ends
	    }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
        return strBuilder.toString();
    }

	/**
       * This method promotes the connected ECR's with ECO to CompleteState
       * to Complete state.
       * @param context the eMatrix <code>Context</code> object.
       * @param args holds the following input arguments.
       * 0 - eco id (objectId of ECO).
       * @throws Exception if the operation fails
       * since EngineeringCentral X3
       */
       public int promoteAttachedECR(Context context, String[] args)
            throws Exception {
        // args[] parameters
        String sEcoid = args[0];
        DomainObject ecoObject = DomainObject.newInstance(context, sEcoid);
        MapList ecrMapList = new MapList();
        MapList relECOMapList =new MapList();
        // Get the Related ECR's that are in Plan ECO state
        try {
            StringList selectStmts = new StringList(1);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_CURRENT);
            StringList selectRelStmts = new StringList(1);
            selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
            String sPlanEcoState = PropertyUtil.getSchemaProperty(context,"policy",
                    POLICY_ECR, "state_PlanECO");
            String sWhereStmt = "current=='"+ sPlanEcoState +"'";
            ecrMapList = ecoObject.getRelatedObjects(context,
                    DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT, // relationship
                    TYPE_ECR, // object pattern
                    selectStmts, // object selects
                    selectRelStmts, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    sWhereStmt, // object where clause
                    null // relationship where clause
                    ); // use cache
            Iterator objItr = ecrMapList.iterator();
            Map ecrMap = null;
            while (objItr.hasNext()) {
                ecrMap = (Map) objItr.next();
                String sEcrId = (String) ecrMap.get(SELECT_ID);
                DomainObject domobj = DomainObject.newInstance(context, sEcrId);
                // fix done for bug #340190 starts
                //If the ECR has more than one ECO Attached to it
                //Checking whether all the ECOs are in Release state and then Promoting the ECR
                relECOMapList=domobj.getRelatedObjects(context,
                        DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT,// relationship
                        TYPE_ECO,// object pattern
                        selectStmts,// object selects
                        selectRelStmts,// relationship selects
                        true,// to direction
                        false,// from direction
                        (short)1,// recursion level
                        null,// object where clause
                        null);// relationship where clause
                boolean isRelased = true;

                if(relECOMapList.size()>1)
                {
                    Iterator objIterator = relECOMapList.iterator();
                    Map ecoMap = null;
                    while (objIterator.hasNext())
                    {
                        ecoMap = (Map) objIterator.next();
                        String sEcoState = (String) ecoMap.get(SELECT_CURRENT);
                      //Modified for IR 076084V6R2012
                      if(STATE_ECO_CREATE.equalsIgnoreCase(sEcoState)||STATE_ECO_DEFINE_COMPONENTS.equalsIgnoreCase(sEcoState)||STATE_ECO_DESIGN_WORK.equalsIgnoreCase(sEcoState)||STATE_ECO_REVIEW.equalsIgnoreCase(sEcoState))
                        {
                            isRelased = false;
                            break;
                        }
                    }
                }

                if(isRelased)
                {
                //fix done for bug #340190 Ends
                    ContextUtil.pushContext(context);
                    domobj.promote(context);
                    ContextUtil.popContext(context);
                }
            }
        } catch (FrameworkException Ex) {
            throw Ex;

        }
        return 0;
    }

/**
      * Display the Related ECR field in ECR WebForm.
      * @param context the eMatrix <code>Context</code> object
      * @param args contains a MapList with the following as input arguments or entries:
      * objectId holds the context ECR object Id
      * New Value holds the newly selected Related ECR Object Id
      * @throws Exception if the operations fails
      * @since EC - X3
 */
         public String  relatedECR (Context context, String[] args) throws Exception {
             //unpacking the Arguments from variable args
             StringBuffer strBuilder = new StringBuffer(1);
             try{
             HashMap programMap = (HashMap) JPO.unpackArgs(args);
             HashMap requestMap = (HashMap) programMap.get("requestMap");
             String relECR = (String) requestMap.get("ECRId");
             String relECRId = relECR;
             setId(relECRId);
             DomainObject dObj = DomainObject.newInstance(context, relECRId);
            // getting the value
             String sECRName =  dObj.getInfo(context,DomainObject.SELECT_NAME);
             strBuilder.append(sECRName);
             }catch(Exception ex){
             throw  new FrameworkException((String)ex.getMessage());
         }
         return strBuilder.toString();
     }

	/**
     * Updates the Related ECR field in ECR WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECR object Id
     * New Value holds the newly selected Related ECR Object Id
     * @throws Exception if the operations fails
     * @since EC - X3
*/
    public void connectRelatedECR (Context context, String[] args) throws Exception {

            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");
			HashMap requestMap= (HashMap)programMap.get("requestMap");
			// New ECO Object Id
			String strObjectId = (String)paramMap.get("objectId");
			// Mode option
			String[] strModCreate= (String[])requestMap.get("CreateMode");
			String str = strModCreate[0];
			if("CreateECO".equals(str)){
                connectECRFromNewValue(context, paramMap);

			}else{

			String[] strMod= (String[])requestMap.get("CreateMode");
			String strMode = strMod[0];
			if(strMode!=null && strMode.equalsIgnoreCase("AssignToECO")){
			// Context Object Id
			String[] strOBJId= (String[])requestMap.get("OBJId");
			//when Mode is :  Assign To ECO
			try{
			// Connecting New ECO with the Context Object Id with the relationship "ECO Change Request Input"
        }catch(Exception ex){
			}
			}
			if(strMode!=null && strMode.equalsIgnoreCase("MoveToECO")){
			String[] strOBJId= (String[])requestMap.get("OBJId");
			String strChangeId = strOBJId [0];
			String sObjectId = "";
			// Creating DomainObject of new ECO
			DomainObject domObjectChangeNew =  new DomainObject(strObjectId);
			// Creating DomainObject of Context  ECO
			DomainObject domainChangeContext = new DomainObject(strChangeId);
			String sTypeECR = PropertyUtil.getSchemaProperty(context,"type_ECR");
			String sRelationship = RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT;

			// Getting all the connected Items with the Context Object with the RelationShip "Affected Item"
			StringList busSelects = new StringList(2);
			busSelects.add(DomainConstants.SELECT_ID);
			busSelects.add(DomainConstants.SELECT_NAME);
			StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);

			MapList mapList = domainChangeContext.getRelatedObjects(context,
																	sRelationship,
																	sTypeECR,
																	busSelects,
																	relSelectsList,
																	false,
																	true,
																	(short)1,
																	null,
																	null);

            if (mapList.size() > 0) {
			Iterator itr = mapList.iterator();
			while(itr.hasNext())
			{
				Map newMap = (Map)itr.next();
				sObjectId=(String) newMap.get(DomainConstants.SELECT_ID);

				DomainObject domRelatedECR = new DomainObject(sObjectId);

				try{
			// Connecting New ECO with the Context Object Id with the relationship "ECO Change Request Input"
			DomainRelationship.connect(context,domObjectChangeNew,RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT,domRelatedECR);
			}catch(Exception ex){
        }
    }
				}
else {
    connectECRFromNewValue(context, paramMap);
}

			}
			}

    }

    private void connectECRFromNewValue(Context context, HashMap paramMap) throws FrameworkException {
		String strECObjectId = (String) paramMap.get("objectId");
		String strNewRelatedObjId = (String) paramMap.get("New OID");

		if (strNewRelatedObjId == null || "".equals(strNewRelatedObjId)) {
			strNewRelatedObjId = (String) paramMap.get("New Value");
		}

		if (strNewRelatedObjId != null && !"".equals(strNewRelatedObjId)) {
			try {
				ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),
						DomainConstants.EMPTY_STRING,
						DomainConstants.EMPTY_STRING);

				DomainRelationship.connect(context, new DomainObject(strECObjectId),
						DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT, true,
						(String[]) FrameworkUtil.split(strNewRelatedObjId, "|").toArray(new String[0]),
						true);

			} catch (Exception e) {
				throw new FrameworkException(e);
			} finally {
				ContextUtil.popContext(context);
			}
		}
    }


		/* This method "getECOAffectedItemsDispCodes" gets DispCodes of the ECO Affected Item.
	 * @param context The ematrix context of the request.
	 * @programMap args This string array contains following arguments:
	 *          0 - The programMap
	 *
	 * @throws Exception
	 * @throws FrameworkException
	 * @since EngineeringCentral X3
	 */
public Vector getECOAffectedItemsDispCodes(Context context, String[] args) throws Exception
    {

		Vector vECRAffectedItemsDispCodes	= new Vector();
		HashMap programMap		= (HashMap)JPO.unpackArgs(args);
		MapList objectList		= (MapList)programMap.get("objectList");
		Iterator itrML = objectList.iterator();
		while(itrML.hasNext())
			{
				Map mAffectedItem		= (Map) itrML.next();

				String sAIObjectId		= (String)mAffectedItem.get(DomainConstants.SELECT_ID);
				String sAIRelId			= (String)mAffectedItem.get(DomainConstants.SELECT_RELATIONSHIP_ID);

				DomainObject sObject = new DomainObject(sAIObjectId);

				if(sObject.isKindOf(context, DomainConstants.TYPE_PART) && (sAIObjectId != null && !"null".equals(sAIObjectId) && sAIObjectId.trim().length() > 0))
				{
					String strFieldReturn	= DomainRelationship.getAttributeValue(context, sAIRelId, DomainRelationship.ATTRIBUTE_DISPOSITION_FIELD_RETURN);
					String strOnOrder		= DomainRelationship.getAttributeValue(context, sAIRelId, DomainRelationship.ATTRIBUTE_DISPOSITION_ON_ORDER);
					String strInProcess		= DomainRelationship.getAttributeValue(context, sAIRelId, DomainRelationship.ATTRIBUTE_DISPOSITION_IN_PROCESS);
					String strInStock		= DomainRelationship.getAttributeValue(context, sAIRelId, DomainRelationship.ATTRIBUTE_DISPOSITION_IN_STOCK);
					String strInField		= DomainRelationship.getAttributeValue(context, sAIRelId, DomainRelationship.ATTRIBUTE_DISPOSITION_IN_FIELD);

					StringBuffer bufDispCodes = new StringBuffer();
					bufDispCodes.append(DomainRelationship.ATTRIBUTE_DISPOSITION_FIELD_RETURN);
					bufDispCodes.append(" :");
					bufDispCodes.append(strFieldReturn);
				    bufDispCodes.append('\n');
					bufDispCodes.append(DomainRelationship.ATTRIBUTE_DISPOSITION_ON_ORDER);
					bufDispCodes.append(" :");
					bufDispCodes.append(strOnOrder);
				    bufDispCodes.append('\n');
					bufDispCodes.append(DomainRelationship.ATTRIBUTE_DISPOSITION_IN_PROCESS);
					bufDispCodes.append(" :");
					bufDispCodes.append(strInProcess);
				    bufDispCodes.append('\n');
					bufDispCodes.append(DomainRelationship.ATTRIBUTE_DISPOSITION_IN_STOCK);
					bufDispCodes.append(" :");
					bufDispCodes.append(strInStock);
				    bufDispCodes.append('\n');
					bufDispCodes.append(DomainRelationship.ATTRIBUTE_DISPOSITION_IN_FIELD);
					bufDispCodes.append(" :");
					bufDispCodes.append(strInField);
					vECRAffectedItemsDispCodes.add(bufDispCodes.toString());
				}
				else
				{
					vECRAffectedItemsDispCodes.add("");
				}

	        }
		return vECRAffectedItemsDispCodes;
    }

	/* This method "getAffectedItemsRelatedECRs" gets Related ECRs of the Affected Item.
	 * @param context The ematrix context of the request.
	 * @programMap args This string array contains following arguments:
	 *          0 - The programMap
	 *
	 * @throws Exception
	 * @throws FrameworkException
	 * @since EngineeringCentral X3
	 */
public Vector getAffectedItemsRelatedECRs(Context context, String[] args)throws Exception {
		Vector vAffectedItemsRelatedECRs = new Vector();
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		MapList objectList = (MapList) programMap.get("objectList");

		HashMap paramList = (HashMap) programMap.get("paramList");
		String strSuiteDir = (String) paramList.get("SuiteDirectory");
		String strJsTreeID = (String) paramList.get("jsTreeID");
		String strParentObjectId = (String) paramList.get("objectId");
		String strFullName = null;
		StringList slIds = new StringList();

		String strDest = ""; // IR-037806
		StringList objectSelects4 = new StringList();

		//Modified for HF-161480 start
		objectSelects4.addElement("to["+RELATIONSHIP_AFFECTED_ITEM+"|from.type=="+TYPE_ECR+"].from.to["+RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT+"|from.id == "+ strParentObjectId + "].to.name");
		objectSelects4.addElement("to["+RELATIONSHIP_AFFECTED_ITEM+"|from.type=="+TYPE_ECR+"].from.to["+RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT+"|from.id == "+ strParentObjectId + "].to.id");

		Iterator itrML = objectList.iterator();
		while (itrML.hasNext()) {
			Map mAffectedItem = (Map) itrML.next();
			String sObjectId = (String) mAffectedItem.get(DomainConstants.SELECT_ID);
			slIds.add(sObjectId);
		}
		String[] AIids = new String[slIds.size()];
		slIds.toArray(AIids);

		MapList objList4 = DomainObject.getInfo(context, AIids, objectSelects4);
		Iterator objItr1 = objList4.iterator();

		while (objItr1.hasNext()) {
			strDest = "";
			Map m = (Map) objItr1.next();

			String name = (String) m.get("to["+RELATIONSHIP_AFFECTED_ITEM+"].from.to["+RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT+"].to.name");
			String id = (String) m.get("to["+RELATIONSHIP_AFFECTED_ITEM+"].from.to["+RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT+"].to.id");

			if((id != null && !("").equals(id) && !("null").equals(id))
					&& (name != null && !("").equals(name) && !("null").equals(name))) {
				String[] strChgId = StringUtils.split(id, "\\a");
				String[] strChgName = StringUtils.split(name, "\\a");
				for (int i = 0; i < strChgId.length; i++) {
					if (strChgId == null) {
						vAffectedItemsRelatedECRs.add("");
					} else {
						if (!"".equals(strDest)) {
							strDest += ",";
						}
						// Constructing the HREF
						strFullName = "<A HREF=\"JavaScript:emxTableColumnLinkClick('emxTree.jsp?mode=insert&amp;emxSuiteDirectory="
								+ XSSUtil.encodeForURL(context, strSuiteDir)
								+ "&amp;parentOID="
								+ XSSUtil.encodeForURL(context,strParentObjectId)
								+ "&amp;jsTreeID="
								+ XSSUtil.encodeForURL(context,strJsTreeID)
								+ "&amp;objectId="
								+ XSSUtil.encodeForURL(context,strChgId[i])
								+ "', 'null', 'null', 'false', 'content')\" class=\"object\">"
								+ XSSUtil.encodeForHTML(context,strChgName[i]) + "</A>";
						strDest += strFullName;
					}
			}
		}
		vAffectedItemsRelatedECRs.add(strDest);
	}
		//Modified for HF-161480 end
		return vAffectedItemsRelatedECRs;
}



	/**
   * Creates the ECO Html summary report.
   * This method has been replicated with createSummaryReport to facilitate PDF report as a Patch.
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds the following input arguments:
   * 0 - String containing object id.
   * @return int tells whether PDF is generated and checked into the object. 0-success 1-failure.
   * @throws Exception if the operation fails.
   * @since EngineeringCentral X3
   */
   public String generateHtmlSummaryReport(Context context,String args[]) throws Exception {

  if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
      }

    String objectId = args[0];
    StringBuffer summaryReport = new StringBuffer(512);
    ECO ecoObj = null;
    try
    {
       String strLanguage = context.getSession().getLanguage();
       ecoObj = new ECO(objectId);
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
       summaryReport.append("<tr><td class=\"pageHeader\"><h1>"+ecoObj.getInfo(context,SELECT_NAME)+":&nbsp;"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.SummaryReport",strLanguage)+"</h1></td>");
       summaryReport.append("<td class=\"pageSubtitle\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Generated",strLanguage)+" "+dateAndTime+"</td></tr>");
       summaryReport.append("</table>");
       summaryReport.append("</div>");

       // Basic Attributes section display
       summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Attributes",strLanguage)+"</h2></td></tr></table>");

       summaryReport.append(getECOBasicInfo(context,args));

       // Approvals Display
       Boolean boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.Approvals");
       if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.Approvals",strLanguage)+"</h2></td></tr></table>");

        summaryReport.append(getECOApprovals(context,args));
       }
       // Routes Display
       boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.Routes");
       if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Routes",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECORoutes(context,args));
       }
       //Routes Display
       boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.Routes");
       if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Tasks",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECOTasks(context,args));
       }
      //Affected Items ( Parts ) Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.AffectedItems");
      if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECOAffectedParts",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECOAffectedItemsSummaryDetails(context,args));
      }
      // affected items (Specifications) Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.AffectedItems");
      if(boolObj.booleanValue()) {
          summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECOAffectedSpecs",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECOSpecifications(context,args));
      }
      //Assingees Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.Assignees");
      if(boolObj.booleanValue()) {
          summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECOAssingees",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getAssigneesOfECO(context,args));
      }
      // Related ECRs Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.RelatedECRs");
      if(boolObj.booleanValue()) {
          summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECORelatedECRs",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECOsRelatedECRs(context,args));
      }
      // Related EBOM Markups s Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.RelatedMarkups");
      if(boolObj.booleanValue()) {
          summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ECORelatedMarkups",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getECORelatedBOMMarups(context,args));
      }
      //    ReferenceDocuments
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.ReferenceDocuments");
      if(boolObj.booleanValue()) {
        summaryReport.append("<table width=\"100%\"><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ReferenceDocuments",strLanguage)+"</h2></td></tr></table>");
       summaryReport.append(getECORelatedReferenceDocuments(context,args));
      }
	  //for bug starts
	  //    Related ResolvedItems Display
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.ResolvedItems");
      if(boolObj.booleanValue()) {
         summaryReport.append("<table width=\"100%\"><tr><td><br></td></tr><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxFramework.Command.ResolvedItems",strLanguage)+"</h2></td></tr></table>");
         summaryReport.append(getECORelatedResolvedItems(context,args));
      }
	  //for bug ends
      //Bug 362975 Starts BOM Comparison
      boolObj = emxCheckAccess(context, "emxEngineeringCentral.ECOSummary.NetBOMComparison");
      if(boolObj.booleanValue()) { //EBOM Comparision - emxEngineeringCentral.ECOSummary.EBOMComparison
          summaryReport.append("<table width=\"100%\" ><tr><td class=\"subhead\"><h2>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.CompareBOM.EBOMComparisonReport",strLanguage)+"</h2></td></tr></table>");
        summaryReport.append(getBOMComparisonDetails(context,args));
      }
      //Bug 362975 ENDS
      summaryReport.append("</html>");
    }
    catch (Exception e)
    {
      throw e;
    }
    return summaryReport.toString();
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

   public  String getECOBasicInfo(Context context,String args[]) throws Exception {

  if (args == null || args.length < 1) {
          throw (new IllegalArgumentException());
    }
    String objectId = args[0];
    String strLanguage = context.getSession().getLanguage();
    String defaultVal = "Unassigned";
    String Unassigned = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Unassigned",strLanguage);
    //for bug 344498 starts
    String sRelECDistributionList = PropertyUtil.getSchemaProperty(context,"relationship_ECDistributionList");
   //for bug 344498 ends

    StringBuffer returnString = new StringBuffer(2048);
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
    objectSelects.add(SELECT_VAULT);
    objectSelects.add(SELECT_POLICY);
    objectSelects.add(com.matrixone.apps.engineering.ECO.SELECT_RESPONSIBLE_DESIGN_ENGINEER);
    objectSelects.add("to[Design Responsibility].from.name");
    //for bug 344498 starts
    objectSelects.add("from["+sRelECDistributionList+"].to.name");
   //for bug 344498 ends

    Map attributeMap = getInfo(context, objectSelects);

    String attrName  = null;
    String attrValue = null;
    String sCurrentState = (String)attributeMap.get(SELECT_CURRENT);
    String ecoDesc = (String)attributeMap.get(SELECT_DESCRIPTION);
    //ecoDesc = ecoDesc.replaceAll("\n","<br>");
    ecoDesc = FrameworkUtil.findAndReplace(ecoDesc,"\n","<br>");
  /* below html table contains two columns (tds). First contains basics and second contains ECO related and other attributes */
  returnString.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
  returnString.append("<tr>");
  returnString.append("<td><table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+":</td><td class=\"inputField\">"+attributeMap.get(SELECT_NAME)+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+":</td><td class=\"inputField\">"+attributeMap.get(SELECT_TYPE)+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage)+":</td><td class=\"inputField\">"+i18nNow.getStateI18NString((String)attributeMap.get(SELECT_POLICY),(String)attributeMap.get(SELECT_CURRENT),strLanguage)+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+":</td><td class=\"inputField\">"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_OWNER)))+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Originator",strLanguage)+":</td><td class=\"inputField\">"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,((String)attributeMap.get(SELECT_ORIGINATOR)))+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Originated",strLanguage)+":</td><td class=\"inputField\">"+(String)attributeMap.get(SELECT_ORIGINATED)+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Modified",strLanguage)+":</td><td class=\"inputField\">"+(String)attributeMap.get(SELECT_MODIFIED)+"</td></tr>");

  returnString.append("</table></td>");
  returnString.append("<td><table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
    returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+":</td><td class=\"inputField\">"+ecoDesc+"&nbsp;</td></tr>");

  //Displaying the ECO specific attributes

    Map attrMap = getAttributeMap(context);
    Iterator ecoAttrListItr = attrMap.keySet().iterator();

    while (ecoAttrListItr.hasNext())
     {
        attrName = (String)ecoAttrListItr.next();
        attrValue = (String)attrMap.get(attrName);

        if((ECO.ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER).equals(attrName)
                ||(ECO.ATTRIBUTE_RESPONSIBLE_MANUFACTURING_ENGINEER).equals(attrName)
                  ) {
            attrValue = (attrValue.equals(defaultVal))?Unassigned:attrValue;
        }
        if(!(attrName.equals(ATTRIBUTE_ORIGINATOR)
                ||(ECO.ATTRIBUTE_RELEASE_DISTRIBUTION_GROUP).equals(attrName)
                ||(ECO.ATTRIBUTE_PRIORITY).equals(attrName)
                ||attrName.equals(PropertyUtil.getSchemaProperty(context,"attribute_BypassPlants")))
           && !attrName.equals(ECO.ATTRIBUTE_REASON_FOR_CANCEL)
           || (attrName.equals(ECO.ATTRIBUTE_REASON_FOR_CANCEL)&& sCurrentState.equals(STATE_ECO_CANCELLED))
         )
        {
            returnString.append("<tr>");
            returnString.append("<td class=\"label\">"+i18nNow.getAttributeI18NString(attrName, strLanguage)+":</td>");
            //IR-017128V6R2011 Start
            if(!attrValue.equals(Unassigned) && ((ECO.ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER).equals(attrName))) {
                returnString.append("<td>"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,attrValue)+"&nbsp;</td>");
             }else if(!attrValue.equals(Unassigned) && ((ECO.ATTRIBUTE_RESPONSIBLE_MANUFACTURING_ENGINEER).equals(attrName))) {
                returnString.append("<td>"+com.matrixone.apps.domain.util.PersonUtil.getFullName(context,attrValue)+"&nbsp;</td>");
             //IR-017128V6R2011 End
             }else if(attrName.equals(ECO.ATTRIBUTE_REASON_FOR_CANCEL)){
                attrValue = FrameworkUtil.findAndReplace(attrValue,"\n","<br>");
                returnString.append("<td class=\"inputField\">"+i18nNow.getRangeI18NString(attrName, attrValue, strLanguage)+"</td>");
            }else{
            returnString.append("<td class=\"inputField\">"+i18nNow.getRangeI18NString(attrName, attrValue, strLanguage)+"</td>");}
            returnString.append("</tr>");
        }
     }

  String sDesignResponsibility = (String)attributeMap.get("to[Design Responsibility].from.name");
  if(sDesignResponsibility == null || "null".equals(sDesignResponsibility)){
    sDesignResponsibility = "&nbsp;";
  }

  //for bug 344498 starts
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
      if(strRouteBasePurpose.equals(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Approval","en"))){
          strApprovalRoute = (String)mpRouteTemplated.get(SELECT_NAME);
      }
      if(strRouteBasePurpose.equals(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Review","en"))){
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

  String sVault = (String)attributeMap.get(SELECT_VAULT);
  String sPolicy = (String) attributeMap.get(SELECT_POLICY);
  if(null!=sVault && null!=strLanguage)
	    sVault = i18nNow.getAdminI18NString("Vault", sVault, strLanguage);

  if(null!=sPolicy && null!=strLanguage)
	    sPolicy = i18nNow.getAdminI18NString("Policy", sPolicy, strLanguage);


  //for bug 344498 ends
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.DesignResponsibility",strLanguage)+":</td><td class=\"inputField\">"+sDesignResponsibility+"</td></tr>");
  //for bug 344498 starts
  returnString.append("<tr><td class=\"label\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Policy",strLanguage)+"</strong>:&nbsp;</td><td class=\"inputField\">"+sPolicy+"</td></tr>");
//Multitenant


  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(),"emxComponents.Form.Label.ReviewerList")+"</strong>:&nbsp;</td><td class=\"inputField\">"+strReviewalRoute+"</td></tr>");
  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxComponentsStringResource", context.getLocale(),"emxComponents.Form.Label.ApprovalList")+"</strong>:&nbsp;</td><td class=\"inputField\">"+strApprovalRoute+"</td></tr>");

  returnString.append("<tr><td class=\"label\">"+EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxFramework.Label.DistributionList")+"</strong>:&nbsp;</td><td class=\"inputField\">"+strDistributionList+"</td></tr>");
  //for bug 344498 ends
  returnString.append("</table></td>");
  returnString.append("</tr>");
  returnString.append("</table>");

  String finalStr = returnString.toString();
  return finalStr;
   }


		/**
     * Constructs the HTML table of the Approvals related to this ECO.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @return String Related approvals in the form of HTML table.
     * @throws Exception if the operation fails.
     * @since Engineering Central X3
    */

   public  String getECOApprovals(Context context,String args[]) throws Exception {


       String languageStr = context.getSession().getLanguage();
       String objectId = args[0];
       //modified for the bug 345920 345921 345922 starts
       boolean bRouteSize         =false;
       boolean bSign              =false;

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

        StringBuffer returnString=new StringBuffer(512);
        returnString.append(" <table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
        returnString.append("<tr><th>");
        returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",languageStr));
        returnString.append("</th> <th>");
        returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Route",languageStr));
        returnString.append("</th> <th>");
        returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Signer",languageStr));
        returnString.append("</th> <th>");
        returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Status",languageStr));
        returnString.append("</th> <th>");
        returnString.append(EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",languageStr));
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
        if (!routes.isEmpty()) {
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
               returnString.append("<td valign=\"top\" class=\"listCell\" style=\"text-align: \" >"+EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.State.ECO."+sStateName)+"&nbsp;</td>");
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
       	   if(!"".equals(routeNodeStatus)) {//IR-141187V6R2013
        		sRoute =  EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Approval_Status."+routeNodeStatus);
       	   }
              returnString.append("<td>"+sRoute+"&nbsp;</td> <td>"+routeNodeComments+"&nbsp;</td></tr>");

           }
           sPersonName="";
           routeNodeStatus="";
           routeNodeComments="";
        }
        }
      }
	//modified for the bug 345920 345921 345922 ends
    if (!bRouteSize && !bSign)
    {
       returnString.append("<tr><td class=\"even\" colspan=\"3\" align=\"center\" >"+ EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.SummaryReport.NoSignOrRoutes", languageStr)+"</td></tr>");
    }
    returnString.append("</table>");
  return  returnString.toString();
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

  public String getECORoutes(Context context, String[] args)
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


    StringBuffer routeInfo = new StringBuffer(1024);
    routeInfo.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
    routeInfo.append("<tr><th width=\"5%\" style=\"text-align:center\"><img border=\"0\" src=\"../common/images/iconStatus.gif\" name=\"imgstatus\" id=\"imgstatus\" alt=\"*\"></th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Status",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Routes.ScheduleCompDate",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</th>");

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
        if (!"Red".equals(sCode)) {
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

          if ("Complete".equals(sState)) {
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

            if("Red".equals(sCode)) {
        routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusRed.gif\" name=\"red\" id=\"red\" alt=\"emxComponents.TaskSummary.ToolTipRed\">";
            } else if("green".equals(sCode)) {
                routeIcon = "<img border=\"0\" src=\"../common/images/iconStatusGreen.gif\" name=\"green\" id=\"green\" alt=\"emxComponents.TaskSummary.ToolTipGreen\">";
            } else if("yellow".equals(sCode)) {
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
      routeInfo.append("<td>"+routeMap.get(SELECT_DESCRIPTION)+"&nbsp;</td>");
      routeInfo.append("<td>"+sStatus+"&nbsp;</td>");
      routeInfo.append("<td>"+scheduledCompletionDate+"&nbsp;</td>");
      routeInfo.append("<td>"+routeMap.get(SELECT_OWNER)+"&nbsp;</td>");
      routeInfo.append("</tr>");
    }

    if(totalResultList.size()==0) {
      routeInfo.append("<tr><td colspan=\"6\">"+ EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.NoObjectsFound", strLanguage)+"</td></tr>");
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
   * @since EngieeringCentral X3
   */
  public String getECOTasks(Context context, String[] args)
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

    StringBuffer routeInfo = new StringBuffer(512);
    routeInfo.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECSummary.RouteName",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECSummary.TaskName",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECSummary.TaskAssignee",strLanguage)+"</th>");
    routeInfo.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECSummary.TaskAction",strLanguage)+"</th>");

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
      routeInfo.append("<tr><td colspan=\"6\">"+ EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.TaskSummary.NoTasksFound", strLanguage)+""+ "&nbsp;</td></tr>");
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
     * Constructs the ECO Affecteditems( parts ) HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @return String Html table format representation of new parts data.
     * @throws Exception if the operation fails.
     * @since EngineeringCentral X3
    */

   public String getECOAffectedItemsSummaryDetails(Context context,String[] args)
         throws Exception
   {
     String strLanguage = context.getSession().getLanguage();
     String objectId = args[0];
     MapList mpAffectedPartList = new MapList();
     StringBuffer newParts = new StringBuffer(1024);
       try
       {
        ECO ecoObj = new ECO(objectId);

    SelectList sListSelStmts = ecoObj.getObjectSelectList(11);
    sListSelStmts.addElement(SELECT_ID);
    sListSelStmts.addElement(SELECT_TYPE);
    sListSelStmts.addElement(SELECT_NAME);
    sListSelStmts.addElement(SELECT_DESCRIPTION);
    sListSelStmts.addElement(SELECT_REVISION);
    sListSelStmts.addElement(SELECT_CURRENT);
    sListSelStmts.add("current.actual");

    StringList selectRelStmts = new StringList(1);
    selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

    String strRelationType = RELATIONSHIP_AFFECTED_ITEM;

    mpAffectedPartList = getRelatedObjects(context,
                                            strRelationType, // relationship pattern
                                            TYPE_PART,      // object pattern
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

       newParts.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
       newParts.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</th>");
       newParts.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</th>");
       newParts.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Rev",strLanguage)+"</th>");
       newParts.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage)+"</th>");
       newParts.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EC.RequestedChangeValue",strLanguage)+"</th>");
       newParts.append("</tr>");

       /* Checking attributes visibility in the type */
       boolean isAttrEDVisible   = false;
       isAttrEDVisible   = isAttributeOnTypeAndNonHidden(context, DomainConstants.TYPE_PART,DomainConstants.ATTRIBUTE_EFFECTIVITY_DATE);

       while (objItr.hasNext()) {
              partMap = (Map)objItr.next();

        String imgPartType = EngineeringUtil.getTypeIconProperty(context, (String)partMap.get(SELECT_TYPE));
        if (imgPartType == null || imgPartType.length() == 0 )
        {
          imgPartType = EngineeringUtil.getTypeIconProperty(context, DomainConstants.TYPE_PART);
          if (imgPartType == null || imgPartType.length() == 0 )
          {
              imgPartType = "iconSmallPart.gif";
          }
        }
        //vamsi
        String sAttrReqChange = PropertyUtil.getSchemaProperty(context,
                "attribute_RequestedChange");
        DomainRelationship domRel= new DomainRelationship((String) partMap.get(SELECT_RELATIONSHIP_ID));
        String sReqChangeVal = domRel.getAttributeValue(context,
                sAttrReqChange);
        //vamsi
        newParts.append("<tr>");

        newParts.append("<td><img src=\"../common/images/"+imgPartType+"\" border=\"0\" alt=\"Part\">&nbsp;"+partMap.get(SELECT_NAME)+"</td>");

        String pMST = (String)partMap.get(SELECT_TYPE); //IR:073051
        pMST = FrameworkUtil.findAndReplace(pMST, " ", "_");

        newParts.append("<td>"+EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Type."+pMST)+"</td>");

        newParts.append("<td>"+partMap.get(SELECT_DESCRIPTION)+"</td>");

        newParts.append("<td>"+partMap.get(SELECT_REVISION)+"</td>");

        if(isAttrEDVisible) {
            String sState      =(String)partMap.get(SELECT_CURRENT);

            sState = FrameworkUtil.findAndReplace(sState, " ", "_");
            newParts.append("<td>"+EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.State.EC_Part."+sState)+"</td>");

        } else {
          newParts.append("<td>&nbsp;</td>");
        }
        if(isAttrEDVisible) {


        	sReqChangeVal = FrameworkUtil.findAndReplace(sReqChangeVal, " ", "_");
        	if(sReqChangeVal != null && !"null".equals(sReqChangeVal) && !"".equals(sReqChangeVal)){
            	newParts.append("<td>"+EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Requested_Change."+sReqChangeVal)+"</td>");
        	} else{
        		newParts.append("<td>&nbsp;</td>");
        	}
          } else {
            newParts.append("<td>&nbsp;</td>");
          }
        newParts.append("</tr>");
       }
       if(mpAffectedPartList.size()==0) {
          newParts.append("<tr><td colspan=\"12\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.NoAffectedPartsConnected",strLanguage)+"</td></tr>");
       }

       }
       catch (FrameworkException Ex)
       {
            throw Ex;
       }
     newParts.append("</table>");
       return newParts.toString();
   }

	/**
     * Constructs the ECO Specifications HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @param isNewSpec boolean determines new or revised specs. true-new Specs false-revised specs.
     * @return String Html table format representation of connected specifications.
     * @throws Exception if the operation fails.
     * @since Enginnering Central X3
    */
   public String getECOSpecifications(Context context,String[] args)
         throws Exception
   {
       String strLanguage = context.getSession().getLanguage();
       String objectId = args[0];
       MapList specList = new MapList();
       StringBuffer specs = new StringBuffer(1024);

       try
       {
           ECO ecoObj = new ECO(objectId);
           StringList selectStmts = new StringList(1);
           selectStmts.addElement(SELECT_ID);
           selectStmts.addElement(SELECT_TYPE);
           selectStmts.addElement(SELECT_NAME);
           selectStmts.addElement(SELECT_REVISION);
           selectStmts.addElement(SELECT_DESCRIPTION);
           selectStmts.addElement(SELECT_CURRENT);
           selectStmts.addElement(SELECT_POLICY);


     Pattern relPattern = new Pattern("");
     relPattern.addPattern(TYPE_CAD_MODEL);
     relPattern.addPattern(TYPE_CAD_DRAWING);

     String objPatrn =TYPE_CAD_MODEL + "," +
                      TYPE_CAD_DRAWING+ "," +
                      TYPE_DRAWINGPRINT + "," +
                      PropertyUtil.getSchemaProperty(context,"type_PartSpecification");

      String sDisrOfChange = DomainRelationship.SELECT_ATTRIBUTE_DESCRIPTION_OF_CHANGE;
      StringList selectRelStmts = new StringList(1);
      selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
      selectRelStmts.addElement(sDisrOfChange);

      String strRelationType = RELATIONSHIP_AFFECTED_ITEM;
     // strRelationType += strRelationType + "," + ChangeConstants.RELATIONSHIP_CHANGE_AFFECTED_ITEM;

       specList = ecoObj.getRelatedObjects(context,
                                       strRelationType,// relationship pattern
                                       objPatrn,// object pattern
                                       selectStmts,// object selects
                                       selectRelStmts, // relationship selects
                                       false,// to direction
                                       true,// from direction
                                       (short) 1,// recursion level
                                       EMPTY_STRING,// object where clause
                                       EMPTY_STRING);// relationship where clause

       specList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
       specList.sort();

       Iterator objItr = specList.iterator();
           Map specMap  = null;
       String imgSpecType = "../common/images/iconSmallCADModel.gif";

       specs.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
       specs.append("<tr>");

         specs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
         specs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</th>");
         specs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</th>");
         specs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Rev",strLanguage)+"</th>");
         specs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage)+"</th>");
         specs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EC.RequestedChangeValue",strLanguage)+"</th>");
         specs.append("</tr>");
       while (objItr.hasNext())
       {
        specMap = (Map)objItr.next();

        imgSpecType = EngineeringUtil.getTypeIconProperty(context, (String)specMap.get(SELECT_TYPE));
        if (imgSpecType == null || imgSpecType.length() == 0 )
        {
            imgSpecType = "iconSmallDefault.gif";
        }
        String sAttrReqChange = PropertyUtil.getSchemaProperty(context,
                "attribute_RequestedChange");
        DomainRelationship domRel= new DomainRelationship((String) specMap.get(SELECT_RELATIONSHIP_ID));
        String sReqChangeVal = FrameworkUtil.findAndReplace(domRel.getAttributeValue(context, sAttrReqChange), " ","_");
        sReqChangeVal = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Range.Requested_Change."+sReqChangeVal);
        specs.append("<tr>");
          specs.append("<td><img src=\"../common/images/"+imgSpecType+"\" border=\"0\" alt=\"*\">&nbsp;"+specMap.get(SELECT_NAME)+"&nbsp;</td>");
          specs.append("<td>"+i18nNow.getAdminI18NString("Type",(String)specMap.get(DomainConstants.SELECT_TYPE), strLanguage)+"&nbsp;</td>");
          specs.append("<td>"+specMap.get(SELECT_DESCRIPTION)+"&nbsp;</td>");
          specs.append("<td>"+specMap.get(SELECT_REVISION)+"&nbsp;</td>");
          specs.append("<td>"+i18nNow.getStateI18NString((String)specMap.get(SELECT_POLICY),(String)specMap.get(SELECT_CURRENT),strLanguage)+"&nbsp;</td>");
          specs.append("<td>"+sReqChangeVal+"&nbsp;</td>");
        specs.append("</tr>");
       }
       if(specList.size()==0) {
          specs.append("<tr><td colspan=\"6\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.NoAffectedSpecsConnected",strLanguage)+"</td></tr>");
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
      * Constructs the ECO related ECRs HTML table.
      *
      * @param context the eMatrix <code>Context</code> object.
      * @param args holds the following input arguments:
      * 0 - String containing object id.
      * @return String Html table format representation of Related ECRs data.
      * @throws Exception if the operation fails.
      * @since EngineeringCentral X3
     */
    public String getAssigneesOfECO(Context context,String[] args)
          throws Exception
    {
        String strLanguage = context.getSession().getLanguage();
        String objectId = args[0];
      MapList mpListAssignees = new MapList();
      StringBuffer relatedAssignees = new StringBuffer(1024);
        try
        {
            ECO ecoObj = new ECO(objectId);
            StringList selectStmts = new StringList(1);
            selectStmts.addElement(SELECT_ID);
            selectStmts.addElement(SELECT_NAME);
            StringList selectRelStmts = new StringList();
			selectRelStmts.add(SELECT_ORIGINATED);

            mpListAssignees = ecoObj.getRelatedObjects(context,RELATIONSHIP_ASSIGNED_EC,
                TYPE_PERSON, selectStmts, selectRelStmts,
                true, false, (short) 1,EMPTY_STRING,EMPTY_STRING);

            mpListAssignees.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
            mpListAssignees.sort();
        Iterator objItr = mpListAssignees.iterator();
        Map ecrMap  = null;
        relatedAssignees.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
        relatedAssignees.append("<tr>");
        relatedAssignees.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Person",strLanguage)+"</th>");
        relatedAssignees.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.FirstName",strLanguage)+"</th>");
        relatedAssignees.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.LastName",strLanguage)+"</th>");
        relatedAssignees.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Role",strLanguage)+"</th>");
        relatedAssignees.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.AssingedOn",strLanguage)+"</th>");
        relatedAssignees.append("</tr>");

        while (objItr.hasNext()) {
               ecrMap = (Map)objItr.next();
               Person person = new Person((String)ecrMap.get(SELECT_ID));
               StringList strList = new StringList ();
               strList.add(SELECT_NAME);
               strList.add("attribute["+ATTRIBUTE_FIRST_NAME+"]");
               strList.add("attribute["+ATTRIBUTE_LAST_NAME+"]");
               Map mpPersonInfo = person.getInfo(context,strList);
               relatedAssignees.append("<tr>");
               //Modified for IR-184707V6R2013x start
               relatedAssignees.append("<td><img src=\"../common/images/iconAssignee.gif\" border=\"0\" alt=\"*\">&nbsp;"+mpPersonInfo.get(SELECT_NAME)+"&nbsp;</td>");
             //Modified for IR-184707V6R2013x end
               relatedAssignees.append("<td>"+mpPersonInfo.get("attribute["+ATTRIBUTE_FIRST_NAME+"]")+"&nbsp;</td>");
               relatedAssignees.append("<td>"+mpPersonInfo.get("attribute["+ATTRIBUTE_LAST_NAME+"]")+"&nbsp;</td>");


               StringList strPersonRolesList =person.getRoleAssignments(context);
               Iterator itr = strPersonRolesList.iterator();
               if(itr!=null){
                   StringBuffer sBufList = new StringBuffer();
                   while(itr.hasNext()){
                   sBufList.append("");
                   sBufList.append(i18nNow.getAdminI18NString("Role",PropertyUtil.getSchemaProperty(context,(String)itr.next()),strLanguage));
                   sBufList.append(',');
                   }
               relatedAssignees.append("<td>"+sBufList.toString()+"&nbsp;</td>");
               }else{
                   relatedAssignees.append("<td>&nbsp;</td>");
               }
               relatedAssignees.append("<td>"+getFormatDate((String)ecrMap.get(SELECT_ORIGINATED))+"&nbsp;</td>");
               relatedAssignees.append("</tr>");
        }
        if(mpListAssignees.size()==0) {
            relatedAssignees.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.NoAssigneesConnected",strLanguage)+"</td></tr>");
        }
        }
        catch (FrameworkException Ex)
        {
             throw Ex;
        }
        relatedAssignees.append("</table>");
        return relatedAssignees.toString();
    }

	/**
     * Constructs the ECO related ECRs HTML table.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds the following input arguments:
     * 0 - String containing object id.
     * @return String Html table format representation of Related ECRs data.
     * @throws Exception if the operation fails.
     * @since Engineering Central X3
    */
   public String getECOsRelatedECRs(Context context,String[] args)
         throws Exception
   {
       String strLanguage = context.getSession().getLanguage();
       String objectId = args[0];
       MapList ecrMapList = new MapList();
       StringBuffer relatedECRs = new StringBuffer(1024);
       try
       {
           ECO ecoObj = new ECO(objectId);
           StringList selectStmts = new StringList(1);
           selectStmts.addElement(SELECT_ID);
       selectStmts.addElement(SELECT_TYPE);
       selectStmts.addElement(SELECT_NAME);
       selectStmts.addElement(SELECT_REVISION);
       selectStmts.addElement(SELECT_DESCRIPTION);
       selectStmts.addElement(SELECT_CURRENT);
       selectStmts.addElement(SELECT_POLICY);

       StringList selectRelStmts = new StringList(1);
           selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
       ecrMapList = ecoObj.getRelatedObjects(context,
               DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT, // relationship pattern
              "*",            // object pattern
              selectStmts,    // object selects
              selectRelStmts, // relationship selects
              false,          // to direction
              true,      // from direction
              (short) 1,      // recursion level
              "",             // object where clause
              "");         // rel where clause

        ecrMapList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
        ecrMapList.sort();
       Iterator objItr = ecrMapList.iterator();
           Map ecrMap  = null;
       relatedECRs.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
       relatedECRs.append("<tr>");
       relatedECRs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
       relatedECRs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Rev",strLanguage)+"</th>");
       relatedECRs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</th>");
       relatedECRs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</th>");
       relatedECRs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage)+"</th>");
       relatedECRs.append("</tr>");

       while (objItr.hasNext()) {
              ecrMap = (Map)objItr.next();
        relatedECRs.append("<tr>");
        relatedECRs.append("<td><img src=\"../common/images/iconSmallECR.gif\" border=\"0\" alt=\"*\">&nbsp;"+ecrMap.get(SELECT_NAME)+"&nbsp;</td>");
        relatedECRs.append("<td>"+ecrMap.get(SELECT_REVISION)+"&nbsp;</td>");
        relatedECRs.append("<td>"+ecrMap.get(SELECT_TYPE)+"&nbsp;</td>");
        relatedECRs.append("<td>"+ecrMap.get(SELECT_DESCRIPTION)+"&nbsp;</td>");
        relatedECRs.append("<td>"+i18nNow.getStateI18NString((String)ecrMap.get(SELECT_POLICY),(String)ecrMap.get(SELECT_CURRENT),strLanguage)+"&nbsp;</td>");
        relatedECRs.append("</tr>");
       }
       if(ecrMapList.size()==0) {
          relatedECRs.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.BuildECR.NoRelatedECRsFound",strLanguage)+"</td></tr>");
       }
       }
       catch (FrameworkException Ex)
       {
            throw Ex;
       }
       relatedECRs.append("</table>");
       return relatedECRs.toString();
   }

	/**
    * Constructs the ECO related Markups HTML table.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds the following input arguments:
    * 0 - String containing object id.
    * @return String Html table format representation of Related ECRs data.
    * @throws Exception if the operation fails.
    * @since Engineering Central X3
   */

  public String getECORelatedBOMMarups(Context context,String[] args)
        throws Exception
  {
      String strLanguage = context.getSession().getLanguage();
      String objectId = args[0];
      MapList mpListMarkups = new MapList();
      StringBuffer relatedMarkups = new StringBuffer(512);
      try
      {
          ECO ecoObj = new ECO(objectId);
          StringList selectStmts = new StringList(1);
          selectStmts.addElement(SELECT_ID);
      selectStmts.addElement(SELECT_NAME);
      selectStmts.addElement(SELECT_ORIGINATED);
      selectStmts.addElement(SELECT_OWNER);
      selectStmts.addElement(SELECT_MODIFIED);
      String relpat = PropertyUtil.getSchemaProperty(context,"type_BOMMarkup")+","+PropertyUtil.getSchemaProperty(context,"type_ItemMarkup");
      StringList selectRelStmts = new StringList(1);
          selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
          mpListMarkups = ecoObj.getRelatedObjects(context,
                  PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup"), // relationship pattern
                  relpat,            // object pattern
             selectStmts,    // object selects
             selectRelStmts, // relationship selects
             false,          // to direction
             true,      // from direction
             (short) 1,      // recursion level
             "",             // object where clause
             "");         // rel where clause

          mpListMarkups.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
          mpListMarkups.sort();
      Iterator objItr = mpListMarkups.iterator();
          Map mpMarkups  = null;
          relatedMarkups.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
          relatedMarkups.append("<tr>");
      relatedMarkups.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
      relatedMarkups.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Originated",strLanguage)+"</th>");
      relatedMarkups.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Modified",strLanguage)+"</th>");
      relatedMarkups.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</th>");
      relatedMarkups.append("</tr>");

      while (objItr.hasNext()) {
          mpMarkups = (Map)objItr.next();
          relatedMarkups.append("<tr>");
          //Modified for IR-184707V6R2013x start
          relatedMarkups.append("<td><img src=\"../common/images/iconSmallDefault.gif\" border=\"0\" alt=\"*\">&nbsp;"+mpMarkups.get(SELECT_NAME)+"&nbsp;</td>");
          //Modified for IR-184707V6R2013x end
          relatedMarkups.append("<td>"+mpMarkups.get(SELECT_ORIGINATED)+"&nbsp;</td>");
          relatedMarkups.append("<td>"+mpMarkups.get(SELECT_MODIFIED)+"&nbsp;</td>");
          relatedMarkups.append("<td>"+mpMarkups.get(SELECT_OWNER)+"&nbsp;</td>");
          relatedMarkups.append("</tr>");
      }
      if(mpListMarkups.size()==0) {
          relatedMarkups.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.NoRelatedMarkupsFound",strLanguage)+"</td></tr>");
      }
      }
      catch (FrameworkException Ex)
      {
           throw Ex;
      }
      relatedMarkups.append("</table>");
      return relatedMarkups.toString();
  }

  /**
    * Constructs the ECO related ECRs HTML table.
    *
    * @param context the eMatrix <code>Context</code> object.
    * @param args holds the following input arguments:
    * 0 - String containing object id.
    * @return String Html table format representation of Related ECRs data.
    * @throws Exception if the operation fails.
    * @since EnginneringCentral X3
   */

  public String getECORelatedReferenceDocuments(Context context,String[] args)
        throws Exception
  {
      String strLanguage = context.getSession().getLanguage();
      String objectId = args[0];
    MapList mpListReferenceDocs = new MapList();
    StringBuffer referenceDocs = new StringBuffer(1024);
      try
      {
          ECO ecoObj = new ECO(objectId);
          StringList selectStmts = new StringList(1);
          selectStmts.addElement(SELECT_ID);
      selectStmts.addElement(SELECT_TYPE);
      selectStmts.addElement(SELECT_NAME);
      selectStmts.addElement(SELECT_REVISION);
      selectStmts.addElement(SELECT_DESCRIPTION);
      selectStmts.addElement(SELECT_CURRENT);
      selectStmts.addElement(SELECT_POLICY);

      StringList selectRelStmts = new StringList(1);
          selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);
          mpListReferenceDocs = ecoObj.getRelatedObjects(context,
          DomainConstants.RELATIONSHIP_REFERENCE_DOCUMENT,
              //DomainConstants.RELATIONSHIP_ECR_SUPPORTING_DOCUMENT, // relationship pattern
             "*",            // object pattern
             selectStmts,    // object selects
             selectRelStmts, // relationship selects
             false,          // to direction
             true,      // from direction
             (short) 1,      // recursion level
             "",             // object where clause
             "");         // rel where clause

          mpListReferenceDocs.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
          mpListReferenceDocs.sort();
      Iterator objItr = mpListReferenceDocs.iterator();
          Map ecrMap  = null;
          referenceDocs.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
          referenceDocs.append("<tr>");
          referenceDocs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
          referenceDocs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Rev",strLanguage)+"</th>");
          referenceDocs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</th>");
          referenceDocs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</th>");
          referenceDocs.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage)+"</th>");
          referenceDocs.append("</tr>");
          //Modified for IR-184707V6R2013x start
          String imgRefType = "../common/images/iconSmallCADModel.gif";
      while (objItr.hasNext()) {
             ecrMap = (Map)objItr.next();

             imgRefType = EngineeringUtil.getTypeIconProperty(context, (String)ecrMap.get(SELECT_TYPE));
             if (imgRefType == null || imgRefType.length() == 0 )
             {
                 imgRefType = "iconSmallDefault.gif";
             }

             referenceDocs.append("<tr>");
             referenceDocs.append("<td><img src=\"../common/images/"+imgRefType+"\" border=\"0\" alt=\"*\">&nbsp;"+ecrMap.get(SELECT_NAME)+"&nbsp;</td>");
             //Modified for IR-184707V6R2013x end
             referenceDocs.append("<td>"+ecrMap.get(SELECT_REVISION)+"&nbsp;</td>");
             referenceDocs.append("<td>"+i18nNow.getAdminI18NString("Type",(String)ecrMap.get(DomainConstants.SELECT_TYPE), strLanguage)+"&nbsp;</td>");
             referenceDocs.append("<td>"+ecrMap.get(SELECT_DESCRIPTION)+"&nbsp;</td>");
             referenceDocs.append("<td>"+i18nNow.getStateI18NString((String)ecrMap.get(SELECT_POLICY),(String)ecrMap.get(SELECT_CURRENT),strLanguage)+"&nbsp;</td>");
             referenceDocs.append("</tr>");
      }
      if(mpListReferenceDocs.size()==0) {
          referenceDocs.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.NoReferenceDocsConnected",strLanguage)+"</td></tr>");
      }
      }
      catch (FrameworkException Ex)
      {
           throw Ex;
      }
      referenceDocs.append("</table>");
      return referenceDocs.toString();
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

 try
    {
        summaryReport=generateHtmlSummaryReport(context,args);
   }
   catch (Exception e)
   {
     throw e;
   }
   		String strGeneratePDF = FrameworkProperties.getProperty(context, "emxEngineeringCentral.ECRECO.ViewPdfSummary");

   		if ("true".equalsIgnoreCase(strGeneratePDF))
   		{
         int pdfGenerated = renderPDFFileForECO(context,args,summaryReport);
         if(pdfGenerated!=0)
         {
              emxContextUtil_mxJPO.mqlError(context,
                                               EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.SummaryReport.NoCheckIn.ErrorMessage",
                                               context.getSession().getLanguage()));
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

  public int renderPDFFileForECO(Context context, String[] args,
          String summaryReport) throws Exception {

		String objectId = args[0];

		setId(objectId);
		String objType = getInfo(context, SELECT_TYPE);
		String objName = getInfo(context,SELECT_NAME);
		String objRev = getInfo(context,SELECT_REVISION);
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
			//363896
            emxcommonPushPopShadowAgent_mxJPO PushPopShadowAgent = new emxcommonPushPopShadowAgent_mxJPO(context, null);

				try
				{
                 //363896
				 /* Push Shadow Agent */
                 PushPopShadowAgent.pushContext(context,null);
				String cmd = "checkin bus $1 $2 $3 format $4 $5";
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

     /**
      * Contains the HTML code to display the distribution List field for ECO UI
      * @param context the eMatrix <code>Context</code> object.
      * @returns string that contains the HTML code to display the distribution List field for ECO UI.
      * @throws Exception if the operation fails.
      * @since Engineeringcentral X3
      */
    public String getDistributionList (Context context, String[] args) throws Exception {

		StringBuffer strBuf	= new StringBuffer(2048);
		strBuf.append("<input type='text' name='DistributionListDisplay' value=''   readOnly='true'> </input>");
		strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript: showDistributionList();' > </input>");
        strBuf.append("<a href=\"javascript:clearDistributionList()\">");
        strBuf.append(strClear);
        strBuf.append("</a>");
		strBuf.append("<input type ='hidden' name='DistributionListOID' value='Unassigned' > </input>");
		strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
		strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
		strBuf.append(" <script> ");
		strBuf.append("function showDistributionList() { ");
        strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_MemberList:CURRENT=policy_MemberList.state_Active"
                + EngineeringUtil.getAltOwnerFilterString(context)
                + "&amp;table=APPECMemberListsSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;fieldNameDisplay=DistributionListDisplay&amp;fieldNameActual=DistributionListOID&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch\",850,630);");

        strBuf.append(" } ");
		strBuf.append("function clearDistributionList() { ");
		strBuf.append(" document.emxCreateForm.DistributionListDisplay.value = \"\"; ");
		strBuf.append(" document.emxCreateForm.DistributionListOID.value     = \"\"; ");
		strBuf.append(" }");
		strBuf.append("</script>");
		return strBuf.toString();

	}

    /**
     * Contains the HTML code to display the distribution List field for edit ECO Page
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public String getDistributionListForEdit (Context context, String[] args) throws Exception {

        StringBuffer outPut = new StringBuffer();
        try {
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            String mode        = (String)requestMap.get("mode");
            String strECObjectId = (String)paramMap.get("objectId");
            String strRelationship = PropertyUtil.getSchemaProperty(context,"relationship_ECDistributionList");
            String strType         = PropertyUtil.getSchemaProperty(context,"type_MemberList");
            String PDFrender = (String)requestMap.get("PDFrender");
            String reportFormat = (String)requestMap.get("reportFormat");
            StringBuffer strBufNamesForExport = new StringBuffer();
            StringList objectSelects = new StringList(2);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_ID);

            StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            setId(strECObjectId);

            MapList relationshipIdList = new MapList();
            relationshipIdList = getRelatedObjects(context,
                                                    strRelationship,
                                                    strType,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);


            StringList relIdStrList = new StringList();
            StringList routeTemplateIdStrList = new StringList();
            StringList routeTemplateNameStrList = new StringList();

			if(!"true".equalsIgnoreCase(PDFrender)) {
				outPut.append(" <script> ");
				outPut.append("function showECDistributionList() { ");
                outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_MemberList:CURRENT=policy_MemberList.state_Active"
                        + EngineeringUtil.getAltOwnerFilterString(context)
                        +"&table=APPECMemberListsSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameDisplay=DistributionListDisplay&fieldNameActual=DistributionListOID&submitURL=../engineeringcentral/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");
                outPut.append('}');
				outPut.append(" </script> ");
			}

            if(relationshipIdList.size()>0) {// if 1:If there is any relationship object route
                for(int i=0;i<relationshipIdList.size();i++) {
                    relIdStrList.add((String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID));
                    routeTemplateIdStrList.add((String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_ID));
                    routeTemplateNameStrList.add((String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_NAME));
                }

                if(relIdStrList.size()>0){ //if 2: Checking for non empty relId list
                    if( mode==null || mode.equalsIgnoreCase("view") ) {
    					if ("true".equalsIgnoreCase(PDFrender)) {
    						outPut.append(routeTemplateNameStrList.get(0));
                        } else {
                            outPut.append("<a href=\"javascript:showModalDialog('emxTree.jsp?objectId=" + routeTemplateIdStrList.get(0) + "',500,700);\">");
                            outPut.append("<img src='../common/images/iconSmallRouteTemplate.gif' border=0>");
                            outPut.append("&nbsp;");
                            outPut.append(routeTemplateNameStrList.get(0));
                            outPut.append("</a>");
    					}
                        if(reportFormat != null && reportFormat.length() > 0){
                            strBufNamesForExport.append(routeTemplateNameStrList.get(0));
                        }
                    } else if( mode.equalsIgnoreCase("edit") ) {
                        outPut.append("<input type=\"text\" name=\"DistributionListDisplay");
                        outPut.append("\"size=\"20\" value=\"");
                        outPut.append(routeTemplateNameStrList.get(0));
                        outPut.append("\" readonly=\"readonly\">&nbsp;");
                        outPut.append("<input class=\"button\" type=\"button\"");
                        outPut.append(" name=\"btnECDistributionListChooser\" size=\"200\" ");
                        outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECDistributionList()\">");
                        outPut.append("<input type=\"hidden\" name=\"DistributionListOID\" value=\""+ routeTemplateIdStrList.get(0) +"\"></input>");
                        outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('DistributionList')\">");
                        outPut.append(strClear);
                        outPut.append("</a>");
                    }
                }//End of if 2
            } else { //if there are no relationships fields are to be dispalyed only in edit mode
				if( mode.equalsIgnoreCase("edit") ) {
                    outPut.append("<input type=\"text\" name=\"DistributionListDisplay");
                    outPut.append("\"size=\"20\" value=\"");
                    outPut.append("\" readonly=\"readonly\">&nbsp;");
                    outPut.append("<input class=\"button\" type=\"button\"");
                    outPut.append(" name=\"btnECDistributionListChooser\" size=\"200\" ");
    				 outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECDistributionList()\">");
                    outPut.append("<input type=\"hidden\" name=\"DistributionListOID\" value=\"\"></input>");
                    outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('DistributionList')\">");
                    outPut.append(strClear);
                    outPut.append("</a>");
                }
            }
            if((strBufNamesForExport.length() > 0 )|| (reportFormat != null && reportFormat.length() > 0)) {
                outPut = strBufNamesForExport;
            }
        } catch(Exception ex) {
            throw  new FrameworkException((String)ex.getMessage());
        }

        return outPut.toString();
	}


     /**
      * Contains the HTML code to display the reviewer List field for ECO UI
      * @param context the eMatrix <code>Context</code> object.
      * @returns string that contains the HTML code to display the reviewer List field for ECO UI.
      * @throws Exception if the operation fails.
      * @since Engineeringcentral X3
      */
    public String getReviewersList (Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String strModCreate= (String)requestMap.get("CreateMode");
        if((strModCreate == null || strModCreate.length()==0)||(strModCreate != null && strModCreate.equalsIgnoreCase("CreateECO"))){
		StringBuffer strBuf	= new StringBuffer();
		strBuf.append("<input type='text' name='ReviewersListDisplay' value=''  readOnly='true' > </input>");
		strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript:showReviewersList();' > </input>");
        strBuf.append("<a href=\"javascript:clearReviewersList()\">");
        strBuf.append(strClear);
        strBuf.append("</a>");
		strBuf.append("<input type ='hidden' name='ReviewersListOID' value='Unassigned' > </input>");
		strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
		strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
		strBuf.append(" <script> ");
		strBuf.append("function showReviewersList() { ");

        strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Review:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE"
                + EngineeringUtil.getAltOwnerFilterString(context)
                +"&amp;table=APPECRouteTemplateSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;fieldNameActual=ReviewersListOID&amp;fieldNameDisplay=ReviewersListDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch\" ,850,630); ");

		strBuf.append(" } ");
		strBuf.append("function clearReviewersList() { ");
		strBuf.append(" document.emxCreateForm.ReviewersListDisplay.value = \"\"; ");
		strBuf.append(" document.emxCreateForm.ReviewersListOID.value     = \"\"; ");
		strBuf.append(" }");
		strBuf.append("</script>");
		return strBuf.toString();
        }else {
            return (String)displayReviewerListItem(context,args);
        }

	}

	/**
     * Contains the HTML code to display the approval List field for ECO UI
     *
     * @param context the eMatrix <code>Context</code> object.
     * @returns string that contains the HTML code to display the approval List field for ECO UI.
     * @throws Exception if the operation fails.
     * @since Engineeringcentral X3
     */
	public String getApprovalList (Context context, String[] args) throws Exception {
		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap requestMap = (HashMap) programMap.get("requestMap");
		String strModCreate= (String)requestMap.get("CreateMode");
		StringBuffer strBuf	= new StringBuffer();
		if((strModCreate!=null && strModCreate.equalsIgnoreCase("AssignToECO"))
                ||(strModCreate!=null && strModCreate.equalsIgnoreCase("MoveToECO"))
                ||(strModCreate!=null && strModCreate.equalsIgnoreCase("AddToECO"))) {
    		strBuf.append("<input type=\"text\"  name=\"ApprovalListDisplay\" id=\"\"   readOnly=\"true\"  value=\"");
    		strBuf.append("");
    		strBuf.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
    		strBuf.append("</input>");
    		strBuf.append("<input type=\"hidden\"  name=\"ApprovalListOID\" value=\"");
    		strBuf.append("");
    		strBuf.append("\">");
    		strBuf.append("</input>");
    		strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
    		strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
    		strBuf.append(" <script> ");
    		strBuf.append("function showApprovalList() { ");
            strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Approval:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE&amp;table=APPECRouteTemplateSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;fieldNameActual=ApprovalListOID&amp;fieldNameDisplay=ApprovalListDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch\" ,850,630); ");
            strBuf.append('}');
    		strBuf.append(" </script> ");
    		strBuf.append("<input type=\"button\" name=\"btnApprovalList\" value=\"...\"    onclick='javascript:showApprovalList();'>");
    		strBuf.append("</input>");
            strBuf.append("<a href=\"JavaScript:basicClear('ApprovalList')\">");
            strBuf.append(strClear);
            strBuf.append("</a>");
    		return strBuf.toString();
        } else {
    		strBuf.append("<input type='text' name='ApprovalListDisplay'  readOnly=\"true\" value='' > </input>");
    		strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript:showApprovalList();' > </input>");
            strBuf.append("<a href=\"javascript:clearApprovalList()\">");
            strBuf.append(strClear);
            strBuf.append("</a>");
    		strBuf.append("<input type ='hidden' name='ApprovalListOID' value='Unassigned' > </input>");
    		strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
    		strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
    		strBuf.append(" <script> ");
    		strBuf.append("function showApprovalList() { ");
            strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Approval:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE"
                    + EngineeringUtil.getAltOwnerFilterString(context)
                    +"&amp;table=APPECRouteTemplateSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;fieldNameActual=ApprovalListOID&amp;fieldNameDisplay=ApprovalListDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch\" ,850,630); ");
    		strBuf.append(" } ");
    		strBuf.append("function clearApprovalList() { ");
    		strBuf.append(" document.emxCreateForm.ApprovalListDisplay.value = \"\"; ");
    		strBuf.append(" document.emxCreateForm.ApprovalListOID.value     = \"\"; ");
    		strBuf.append(" }");
    		strBuf.append("</script>");
    		return strBuf.toString();
        }
    }

     /**
      * Contains the HTML code to display the Reported Against Change field for ECO UI
      * @param context the eMatrix <code>Context</code> object.
      * @returns string that contains the HTML code to display the Reported Against Change field for ECO UI.
      * @throws Exception if the operation fails.
      * @since Engineeringcentral X3
      */
    public String getReportedAgainstChange (Context context, String[] args) throws Exception {

        StringBuffer strBuf	= new StringBuffer(2048);
        strBuf.append("<input type='text' name='ReportedAgainstDisplay' value='' readOnly='true' > </input>");
        strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript:showReportedAgainst();' > </input>");
        strBuf.append("<a href=\"javascript:clearReportedAgainst()\">");
        strBuf.append(strClear);
        strBuf.append("</a>");
        strBuf.append("<input type ='hidden' name='ReportedAgainstOID' value='Unassigned' > </input>");
        strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
        strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
        strBuf.append(" <script> ");
        strBuf.append("function showReportedAgainst() { ");
        strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_Part,type_Builds,type_CADDrawing,type_CADModel,type_DrawingPrint,type_PartSpecification,type_Products");
        strBuf.append(EngineeringUtil.getAltOwnerFilterString(context));
        strBuf.append("&amp;table=APPECReportedAgainstSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;srcDestRelName=relationship_ReportedAgainstChange&amp;formName=emxCreateForm&amp;fieldNameActual=ReportedAgainstOID&amp;fieldNameDisplay=ReportedAgainstDisplay&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch&amp;suiteKey=EngineeringCentral\",850,630); ");
        strBuf.append(" } ");
        strBuf.append("function clearReportedAgainst() { ");
        strBuf.append(" document.emxCreateForm.ReportedAgainstDisplay.value = \"\"; ");
        strBuf.append(" document.emxCreateForm.ReportedAgainstOID.value     = \"\"; ");
        strBuf.append(" }");
        strBuf.append("</script>");
        return strBuf.toString();

    }

    /**
     * Display the ReportedAgainst Item field in ECO WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECO object Id
     * @throws Exception if the operations fails
     * @since EC - X3
     */

    public Object  displayReportedAgainstItem(Context context,String[] args)throws Exception{
		StringBuffer sbReturnString = new StringBuffer();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strLanguage        =  context.getSession().getLanguage();
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String relChange = (String) requestMap.get("OBJId");
			String strModCreate= (String)requestMap.get("CreateMode");
			String sDisplayValue = null;
			String sHiddenValue = null;
            //Relationship name
            String strRelationshipReportedAgainstChange = PropertyUtil.getSchemaProperty(context,"relationship_ReportedAgainstChange");
			if(relChange!=null) {
    			DomainObject domObj = new DomainObject(relChange);
    			if(domObj.isKindOf(context, DomainConstants.TYPE_ECR)
                        || domObj.isKindOf(context, DomainConstants.TYPE_ECO)
                        || domObj.isKindOf(context, DomainConstants.TYPE_PART)){
    			    StringList objectSelects = new StringList();
    			    objectSelects.addElement(DomainConstants.SELECT_NAME);
    				objectSelects.addElement(DomainConstants.SELECT_ID);
    				StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
    				MapList relationshipIdList = new MapList();
    				String sObjectName ="";
    				String sObjectId ="";
    				if((strModCreate!=null && strModCreate.equalsIgnoreCase("AssignToECO"))
                            ||(strModCreate!=null && strModCreate.equalsIgnoreCase("MoveToECO"))
                            ||(strModCreate!=null && strModCreate.equalsIgnoreCase("AddToECO"))) {
    				    //Calling getRelatedObjects to get the relationship ids
    				    relationshipIdList = domObj.getRelatedObjects(context,
                                                        strRelationshipReportedAgainstChange,
                                                        DomainConstants.QUERY_WILDCARD,
                                                        objectSelects,
                                                        relSelectsList,
                                                        false,
                                                        true,
                                                        (short)1,
                                                        DomainConstants.EMPTY_STRING,
                                                        DomainConstants.EMPTY_STRING);
                    }

    				if (relationshipIdList.size() > 0) {
        				Map newMap = (Map)relationshipIdList.get(0);
        				sObjectName=(String) newMap.get(DomainConstants.SELECT_NAME);
        				sObjectId=(String) newMap.get(DomainConstants.SELECT_ID);
        				sDisplayValue =sObjectName;
        		        sHiddenValue = sObjectId;

        				sbReturnString.append("<input type='text' name='ReportedAgainstDisplay' value=\"");
        				sbReturnString.append(sDisplayValue);
        				sbReturnString.append("\" ></input>");
        				sbReturnString.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript:showReportedAgainst();' > </input>");
        				sbReturnString.append("<a href=\"javascript:clearReportedAgainst()\">");
        				sbReturnString.append(strClear);
        				sbReturnString.append("</a>");
        				sbReturnString.append("<input type ='hidden' name='ReportedAgainstOID' value=\"");
        				sbReturnString.append(sHiddenValue);
        				sbReturnString.append("\"> </input>");
        				sbReturnString.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
        				sbReturnString.append(" <script src='../emxUIPageUtility.js'> </script> ");
        				sbReturnString.append(" <script> ");
        				sbReturnString.append("function showReportedAgainst() { ");
        				sbReturnString.append(" var changeResId = document.emxCreateForm.DesignResponsibilityOID.value;");
        				sbReturnString.append(" if (changeResId == null || changeResId == \"\" || changeResId == \" \") {");
        				sbReturnString.append(" alert (\""+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.DesignResponsibilityAlert",strLanguage)    +"\");");
        				sbReturnString.append("} else {");
        				sbReturnString.append("emxShowModalDialog(\"../components/emxCommonSearch.jsp?formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;searchmode=chooser&amp;suiteKey=Components&amp;searchmenu=APPECSearchAddExistingChooser&amp;searchcommand=APPSearchECReportedAgainstItemsCommand&amp;fieldNameActual=ReportedAgainstOID&amp;fieldNameDisplay=ReportedAgainstDisplay&amp;fieldNameOID=ReportedAgainstOID&amp;suiteKey=Components&amp;objectId=\" + changeResId + \"&amp;ecrEcoUi=true\" ,700,500); ");
        				sbReturnString.append('}');
        				sbReturnString.append(" } ");
        				sbReturnString.append("function clearReportedAgainst() { ");
        				sbReturnString.append(" document.emxCreateForm.ReportedAgainstDisplay.value = \"\"; ");
        				sbReturnString.append(" document.emxCreateForm.ReportedAgainstOID.value     = \"\"; ");
        				sbReturnString.append(" }");
        				sbReturnString.append("</script>");
                    } else {
        				sbReturnString.append("<input type='text' name='ReportedAgainstDisplay' value='' > </input>");
        				sbReturnString.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript:showReportedAgainst();' > </input>");
        				sbReturnString.append("<a href=\"javascript:clearReportedAgainst()\">");
        				sbReturnString.append(strClear);
        				sbReturnString.append("</a>");
        				sbReturnString.append("<input type ='hidden' name='ReportedAgainstOID' value='Unassigned' > </input>");
        				sbReturnString.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
        				sbReturnString.append(" <script src='../emxUIPageUtility.js'> </script> ");
        				sbReturnString.append(" <script> ");
        				sbReturnString.append("function showReportedAgainst() { ");
        				sbReturnString.append(" var changeResId = document.emxCreateForm.DesignResponsibilityOID.value;");
        				sbReturnString.append(" if (changeResId == null || changeResId == \"\" || changeResId == \" \") {");
        				sbReturnString.append(" alert (\""+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.DesignResponsibilityAlert",strLanguage)    +"\");");
        				sbReturnString.append("} else {");
        				sbReturnString.append("emxShowModalDialog(\"../components/emxCommonSearch.jsp?formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;searchmode=chooser&amp;suiteKey=Components&amp;searchmenu=APPECSearchAddExistingChooser&amp;searchcommand=APPSearchECReportedAgainstItemsCommand&amp;fieldNameActual=ReportedAgainstOID&amp;fieldNameDisplay=ReportedAgainstDisplay&amp;fieldNameOID=ReportedAgainstOID&amp;suiteKey=Components&amp;objectId=\" + changeResId + \"&amp;ecrEcoUi=true\" ,700,500); ");
        				sbReturnString.append('}');
        				sbReturnString.append(" } ");
        				sbReturnString.append("function clearReportedAgainst() { ");
        				sbReturnString.append(" document.emxCreateForm.ReportedAgainstDisplay.value = \"\"; ");
        				sbReturnString.append(" document.emxCreateForm.ReportedAgainstOID.value     = \"\"; ");
        				sbReturnString.append(" }");
        				sbReturnString.append("</script>");
                    }
    			 } else {
    			     sbReturnString.append("");
                 }
            }
		} catch(Exception ex) {
            ex.printStackTrace();
		}
		return sbReturnString.toString();
	}
/**
     * Display the DistributionList Item field in ECO WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECO object Id
     * @throws Exception if the operations fails
     * @since EC - X3
     */

public Object  displayDistributionListItem(Context context,String[] args)throws Exception{
		StringBuffer strBuf = new StringBuffer();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			String strLanguage        =  context.getSession().getLanguage();
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String relChange = (String) requestMap.get("OBJId");
			String strModCreate= (String)requestMap.get("CreateMode");
			String sDisplayValue = null;
			String sHiddenValue = null;
			String strRelationship = PropertyUtil.getSchemaProperty(context,"relationship_ECDistributionList");
			if(relChange!=null)
			{
			DomainObject domObj = new DomainObject(relChange);
			if(domObj.isKindOf(context, DomainConstants.TYPE_ECR) || domObj.isKindOf(context, DomainConstants.TYPE_ECO) || domObj.isKindOf(context, DomainConstants.TYPE_PART)){
							//Business Objects are selected by its Ids
							StringList objectSelects = new StringList();
							objectSelects.addElement(DomainConstants.SELECT_NAME);
							objectSelects.addElement(DomainConstants.SELECT_ID);
							//Stringlist containing the relselects
							StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
			String sObjectName ="";
			String sObjectId ="";
			if((strModCreate!=null && strModCreate.equalsIgnoreCase("AssignToECO"))||(strModCreate!=null && strModCreate.equalsIgnoreCase("MoveToECO")) ||(strModCreate!=null && strModCreate.equalsIgnoreCase("AddToECO")))
				{
            //Calling getRelatedObjects to get the relationship ids
            relationshipIdList = domObj.getRelatedObjects(context,
                                                    strRelationship,
                                                    DomainConstants.QUERY_WILDCARD,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);
			}
		  if (relationshipIdList.size() > 0)
			{
			Iterator itr = relationshipIdList.iterator();
			while(itr.hasNext())
			{
				Map newMap = (Map)itr.next();
				sObjectName=(String) newMap.get(DomainConstants.SELECT_NAME);
				sObjectId=(String) newMap.get(DomainConstants.SELECT_ID);
				sDisplayValue =sObjectName;
		        sHiddenValue = sObjectId;
				strBuf.append("<input type='text' name='DistributionListDisplay' value=\"");
				strBuf.append(sDisplayValue);
				strBuf.append("\" > </input>");
				strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript: showDistributionList();' > </input>");
				strBuf.append("<a href=\"javascript:clearDistributionList()\">");
				strBuf.append(strClear);
				strBuf.append("</a>");
				strBuf.append("<input type ='hidden' name='DistributionListOID' value=\"");
				strBuf.append(sHiddenValue);
				strBuf.append("\"> </input>");
				strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
				strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
				strBuf.append(" <script> ");
				strBuf.append("function showDistributionList() { ");
				strBuf.append(" var changeResId = document.emxCreateForm.DesignResponsibilityOID.value;");
				strBuf.append(" if (changeResId == null || changeResId == \"\" || changeResId == \" \") {");
				strBuf.append(" alert (\""+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.DesignResponsibilityAlert",strLanguage)    +"\");");
				strBuf.append("} else {");

                strBuf.append("var designResName = document.emxCreateForm.DesignResponsibilityDisplay.value;");
                strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_MemberList:REL_MEMBERLIST_OWNINGORGANIZATION=\" + designResName + \":CURRENT=policy_MemberList.state_Active&amp;table=APPECMemberListsSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;fieldNameDisplay=DistributionListDisplay&amp;fieldNameActual=DistributionListOID&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch\",850,630);");

				strBuf.append('}');
				strBuf.append(" } ");
				strBuf.append("function clearDistributionList() { ");
				strBuf.append(" document.emxCreateForm.DistributionListDisplay.value = \"\"; ");
				strBuf.append(" document.emxCreateForm.DistributionListOID.value     = \"\"; ");
				strBuf.append(" }");
				strBuf.append("</script>");
			}
			}else{

				strBuf.append("<input type='text' name='DistributionListDisplay' value='' > </input>");
				strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript: showDistributionList();' > </input>");
				strBuf.append("<a href=\"javascript:clearDistributionList()\">");
				strBuf.append(strClear);
				strBuf.append("</a>");
				strBuf.append("<input type ='hidden' name='DistributionListOID' value='Unassigned' > </input>");
				strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
				strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
				strBuf.append(" <script> ");
				strBuf.append("function showDistributionList() { ");
				strBuf.append(" var changeResId = document.emxCreateForm.DesignResponsibilityOID.value;");
				strBuf.append(" if (changeResId == null || changeResId == \"\" || changeResId == \" \") {");
				strBuf.append(" alert (\""+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.DesignResponsibilityAlert",strLanguage)    +"\");");
				strBuf.append("} else {");

                strBuf.append("var designResName = document.emxCreateForm.DesignResponsibilityDisplay.value;");
                strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_MemberList:REL_MEMBERLIST_OWNINGORGANIZATION=\" + designResName + \":CURRENT=policy_MemberList.state_Active&amp;table=APPECMemberListsSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;fieldNameDisplay=DistributionListDisplay&amp;fieldNameActual=DistributionListOID&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch\",850,630);");

				strBuf.append('}');
				strBuf.append(" } ");
				strBuf.append("function clearDistributionList() { ");
				strBuf.append(" document.emxCreateForm.DistributionListDisplay.value = \"\"; ");
				strBuf.append(" document.emxCreateForm.DistributionListOID.value     = \"\"; ");
				strBuf.append(" }");
				strBuf.append("</script>");

			}

			 }else{
			  strBuf.append("");

			 }
			}
		}catch(Exception ex)
		{
		}
		return strBuf.toString();
	}
/**
     * Display the ReviewerList Item field in ECO WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECO object Id
     * @throws Exception if the operations fails
     * @since EC - X3
     */

public Object  displayReviewerListItem(Context context,String[] args)throws Exception{
		StringBuffer strBuf = new StringBuffer();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String relChange = (String) requestMap.get("OBJId");
			String strModCreate= (String)requestMap.get("CreateMode");
			String sDisplayValue = null;
			String sHiddenValue = null;
			String strRelationship = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
			if(relChange!=null)
			{
			DomainObject domObj = new DomainObject(relChange);
			if(domObj.isKindOf(context, DomainConstants.TYPE_ECR) || domObj.isKindOf(context, DomainConstants.TYPE_ECO) || domObj.isKindOf(context, DomainConstants.TYPE_PART)){
				//Business Objects are selected by its Ids
				StringList objectSelects = new StringList();
				objectSelects.addElement(DomainConstants.SELECT_NAME);
				objectSelects.addElement(DomainConstants.SELECT_ID);
				//Stringlist containing the relselects
				StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
			String sObjectName ="";
			String sObjectId ="";
			if((strModCreate!=null && strModCreate.equalsIgnoreCase("AssignToECO"))||(strModCreate!=null && strModCreate.equalsIgnoreCase("MoveToECO")) ||(strModCreate!=null && strModCreate.equalsIgnoreCase("AddToECO")))
				{
            //Calling getRelatedObjects to get the relationship ids
            relationshipIdList = domObj.getRelatedObjects(context,
                                                    strRelationship,
                                                    DomainConstants.QUERY_WILDCARD,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);
			}
		  if (relationshipIdList.size() > 0)
			{
			Iterator itr = relationshipIdList.iterator();
			while(itr.hasNext())
			{
				Map newMap = (Map)itr.next();
				sObjectName=(String) newMap.get(DomainConstants.SELECT_NAME);
				sObjectId=(String) newMap.get(DomainConstants.SELECT_ID);
				DomainObject newValue =  new DomainObject(sObjectId);
				String strAttribute = newValue.getAttributeValue(context,ATTRIBUTE_ROUTE_BASE_PURPOSE);
				if("Review".equals(strAttribute)){
				sDisplayValue =sObjectName;
		        sHiddenValue = sObjectId;
					strBuf.append("<input type='text' name='ReviewersListDisplay' value=\"");
					strBuf.append(sDisplayValue);
					strBuf.append("\" > </input>");
					strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript:showReviewersList();' > </input>");
					strBuf.append("<a href=\"javascript:clearReviewersList()\">");
					strBuf.append(strClear);
					strBuf.append("</a>");
					strBuf.append("<input type ='hidden' name='ReviewersListOID' value=\"");
					strBuf.append(sHiddenValue);
					strBuf.append("\" > </input>");
					strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
					strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
					strBuf.append(" <script> ");
					strBuf.append("function showReviewersList() { ");

                    strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Review:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE&amp;table=APPECRouteTemplateSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;fieldNameActual=ReviewersListOID&amp;fieldNameDisplay=ReviewersListDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch\" ,850,630); ");

					strBuf.append(" } ");
					strBuf.append("function clearReviewersList() { ");
					strBuf.append(" document.emxCreateForm.ReviewersListDisplay.value = \"\"; ");
					strBuf.append(" document.emxCreateForm.ReviewersListOID.value     = \"\"; ");
					strBuf.append(" }");
					strBuf.append("</script>");
				}

			}
			}else{
				strBuf.append("<input type='text' name='ReviewersListDisplay' value='' > </input>");
				strBuf.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript:showReviewersList();' > </input>");
				strBuf.append("<a href=\"javascript:clearReviewersList()\">");
				strBuf.append(strClear);
				strBuf.append("</a>");
				strBuf.append("<input type ='hidden' name='ReviewersListOID' value='Unassigned' > </input>");
				strBuf.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
				strBuf.append(" <script src='../emxUIPageUtility.js'> </script> ");
				strBuf.append(" <script> ");
				strBuf.append("function showReviewersList() { ");

                strBuf.append("emxShowModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Review:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE&amp;table=APPECRouteTemplateSearchList&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;fieldNameActual=ReviewersListOID&amp;fieldNameDisplay=ReviewersListDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch\" ,850,630); ");

				strBuf.append(" } ");
				strBuf.append("function clearReviewersList() { ");
				strBuf.append(" document.emxCreateForm.ReviewersListDisplay.value = \"\"; ");
				strBuf.append(" document.emxCreateForm.ReviewersListOID.value     = \"\"; ");
				strBuf.append(" }");
				strBuf.append("</script>");

			}

			 }else{
				strBuf.append("");
			 }
			}
		}catch(Exception ex)
		{
		}
		return strBuf.toString();
	}


 /**
      * Display the Related ECR field in ECR WebForm.
      * @param context the eMatrix <code>Context</code> object
      * @param args contains a MapList with the following as input arguments or entries:
      * objectId holds the context ECR object Id
      * New Value holds the newly selected Related ECR Object Id
      * @throws Exception if the operations fails
      * @since EC - X3
 */

public Object  displayECORelatedECRItem(Context context,String[] args)throws Exception{
		StringBuffer sbReturnString = new StringBuffer();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");
			String strChangeId = (String) requestMap.get("OBJId");

			String sDisplayValue = null;
			String sHiddenValue = null;
			String sChangeName =  "";
			String sChangeId =  "";
		   	String sTypeECO = PropertyUtil.getSchemaProperty(context,"type_ECR");
			String sRelationship = RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT;
             if(strChangeId != null || !"".equals(strChangeId)){

					DomainObject domObj = new DomainObject(strChangeId);
					String strType =  domObj.getInfo(context,SELECT_TYPE);
					 if(strType.equals(DomainConstants.TYPE_ECR) ) {
							sChangeName =  domObj.getInfo(context,DomainObject.SELECT_NAME);
							sChangeId =  domObj.getInfo(context,DomainObject.SELECT_ID);
				sDisplayValue =sChangeName;
		        sHiddenValue = sChangeId;
				sbReturnString.append("<input type=\"text\"  name=\"RelatedECRDisplay\" readonly=\"true\" id=\"\" value=\"");
				sbReturnString.append(sDisplayValue);
				sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<input type=\"hidden\"  name=\"RelatedECR\" value=\"");
				sbReturnString.append(sHiddenValue);
				sbReturnString.append("\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<input type=\"button\" disabled=\"true\" name=\"btnRelatedECR\" value=\"...\"	 onclick=\"javascript:showChooser('../components/emxCommonSearch.jsp?formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;suiteKey=Components&amp;searchmode=chooser&amp;searchmenu=APPECSearchAddExistingChooser&amp;searchcommand=APPSearchECReportedAgainstItemsCommand&amp;fieldNameActual=RelatedECR&amp;fieldNameDisplay=RelatedECRDisplay");
				sbReturnString.append(sDisplayValue);
				sbReturnString.append("&amp;fieldNameOID=RelatedECROID&amp;relId=null&amp;suiteKey=Components','700','500')\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<a href=\"JavaScript:basicClear('DoNotClear')\" disabled=\"true\">");
				sbReturnString.append(strClear);
				sbReturnString.append("</a>");
			}
			if(strType.equals(DomainConstants.TYPE_ECO)){
				// Getting all the connected Items with the Context Object with the RelationShip "Affected Item"
			StringList busSelects = new StringList(2);
			busSelects.add(DomainConstants.SELECT_ID);
			busSelects.add(DomainConstants.SELECT_NAME);
			StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
			DomainObject domObj1 = new DomainObject(strChangeId);
		    MapList mapList = domObj1.getRelatedObjects(context,
																	sRelationship,
																	sTypeECO,
																	busSelects,
																	relSelectsList,
																	false,
																	true,
																	(short)1,
																	null,
																	null);
			if (mapList.size() > 0)
			{
			Iterator itr = mapList.iterator();
			while(itr.hasNext())
			{
				Map newMap = (Map)itr.next();
				sChangeId+=(String) newMap.get(DomainConstants.SELECT_ID)+"|";
				sChangeName+=(String) newMap.get(DomainConstants.SELECT_NAME)+",";

					}
			    sDisplayValue =sChangeName;
		        sHiddenValue = sChangeId.substring(0,sChangeId.lastIndexOf('|'));
				sbReturnString.append("<input type=\"text\"  name=\"RelatedECRDisplay\" readonly=\"true\" id=\"\" value=\"");
				sbReturnString.append(sDisplayValue);
				sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<input type=\"hidden\"  name=\"RelatedECR\" value=\"");
				sbReturnString.append(sHiddenValue);
				sbReturnString.append("\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<input type=\"button\" disabled=\"true\" name=\"btnRelatedECR\" value=\"...\"	 onclick=\"javascript:showChooser('../components/emxCommonSearch.jsp?formName=emxCreateForm&amp;frameName=formCreateDisplay&amp;suiteKey=Components&amp;searchmode=chooser&amp;searchmenu=APPECSearchAddExistingChooser&amp;searchcommand=APPSearchECReportedAgainstItemsCommand&amp;fieldNameActual=RelatedECR&amp;fieldNameDisplay=RelatedECRDisplay");
				sbReturnString.append(sDisplayValue);
				sbReturnString.append("&amp;fieldNameOID=RelatedECROID&amp;relId=null&amp;suiteKey=Components','700','500')\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<a href=\"JavaScript:basicClear('DoNotClear')\" disabled=\"true\">");
				sbReturnString.append(strClear);
				sbReturnString.append("</a>");

				}
				else{
				sbReturnString.append("<input type=\"text\"  name=\"RelatedECRDisplay\" readonly=\"true\" id=\"\" value=\"");
				sbReturnString.append("");
				sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<input type=\"hidden\"  name=\"RelatedECR\" value=\"");
				sbReturnString.append("");
				sbReturnString.append("\">");
				sbReturnString.append("</input>");

                sbReturnString.append("<input type=\"button\" name=\"btnRelatedECR\" value=\"...\"   onclick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_ECR:CURRENT=state_PlanECO&amp;table=ENCGeneralSearchResult&amp;RegisteredSuite=engineeringcentral&amp;selection=multiple&amp;hideHeader=true&amp;submitURL=../engineeringcentral/emxengchgECOConnectECRProcess.jsp&amp;fieldName=RelatedECR&amp;formName=frmCreateECO&amp;mode=ECOCreate&amp;fieldNameActual=RelatedECR&amp;fieldNameDisplay=RelatedECRDisplay&amp;fieldNameOID=RelatedECROID&amp;suiteKey=EngineeringCentral&amp;HelpMarker=emxhelpfullsearch");
				sbReturnString.append("");
				sbReturnString.append("&amp;fieldNameOID=RelatedECROID&amp;relId=null&amp;suiteKey=Components','700','500')\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<a href=\"JavaScript:basicClear('RelatedECR')\">");
				sbReturnString.append(strClear);
				sbReturnString.append("</a>");
				}
			}
			 }
		}catch(Exception ex)
		{
		}
		return sbReturnString.toString();
	}

/**
 * Added for the Fix 370336
 * Display the Related ECR field in ECO WebForm.
 * @param context the eMatrix <code>Context</code> object
 * @param args contains a MapList with the following as input arguments or entries:
 * objectId holds the context ECO object Id
 * Returns the String Containing all related ECR's
 * @throws Exception if the operations fails
 * @since EC - X6
*/
public static String getConnectedECR(Context context,String[] args)throws Exception
{

    String strResult ="";
    String strChangeId ="";
    String sChangeName ="";
    try{
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        strChangeId = (String) requestMap.get("objectId");
        DomainObject domObj = new DomainObject(strChangeId);
//      Getting all the connected Items with the Context Object with the RelationShip "RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT"
        StringList  sList=domObj.getInfoList(context,"from["+RELATIONSHIP_ECO_CHANGE_REQUEST_INPUT+"].to.name");
        if(sList.size()>0)
        {

        Iterator slchangeItr=sList.iterator();

        while(slchangeItr.hasNext())
        {
             sChangeName=(String)slchangeItr.next();
             strResult=strResult+","+sChangeName;

        }
        strResult=strResult.substring(1,strResult.length());
        }
    }
        catch(Exception ex)
        {
        }
     return strResult;
}
//370336 fix ends

/**
     * Updates the ResponsibleDesignEngineer field in ECO WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECO object Id
     * @throws Exception if the operations fails
     * @since EC - X3
     */
public void  updateResponsibleDesignEngineer(Context context,String[] args)throws Exception{
		try
		{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get("paramMap");

			//Getting the EC Object id and the new product object id
			String strChangeobjectId = (String)paramMap.get("objectId");

				// Attribute "ECO Responsible Design Engineer
				String ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER = PropertyUtil.getSchemaProperty(context,"attribute_ResponsibleDesignEngineer");

				if (strChangeobjectId != null && strChangeobjectId.length() > 0)
				{
						DomainObject domChangeObj = new DomainObject(strChangeobjectId);

					if(domChangeObj.isKindOf(context, DomainConstants.TYPE_ECR) || domChangeObj.isKindOf(context, DomainConstants.TYPE_ECO))
					{
						String strNewValue = (String)paramMap.get("New Value");

						if (strNewValue != null && !"null".equals(strNewValue) && strNewValue.length() > 0)
						{
						domChangeObj.setAttributeValue(context,ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER,strNewValue);

								String strObjWhere = DomainConstants.SELECT_NAME + " == \"" + strNewValue + "\"";

								MapList mapListPersons = domChangeObj.getRelatedObjects(context,DomainConstants.RELATIONSHIP_ASSIGNED_EC,
														DomainConstants.TYPE_PERSON,
														new StringList(DomainConstants.SELECT_ID),
														null,
														true,
														false,(short) 1,
														strObjWhere,
														null);

								if (mapListPersons != null && mapListPersons.size() == 0)
							{
								DomainRelationship.connect(context,
													   new DomainObject(PersonUtil.getPersonObjectID(context,strNewValue)),
													   DomainConstants.RELATIONSHIP_ASSIGNED_EC,
													   domChangeObj);
							}
						}
					}
				}

			}
			catch(Exception ex)
			{
         }

	}

/**
     * Display the ResponsibleDesignEngineer field in ECO WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECO object Id
     * @throws Exception if the operations fails
     * @since EC - X3
     */

public StringList  displayResponsibleDesignEngineerItem(Context context,String[] args)throws Exception{

try{
				HashMap programMap = (HashMap) JPO.unpackArgs(args);
				HashMap requestMap = (HashMap) programMap.get("requestMap");
				String relECR = (String) requestMap.get("OBJId");
				StringList strList=new StringList();
				// Attribute "ECO Responsible Design Engineer
				String ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER =
				PropertyUtil.getSchemaProperty(context,"attribute_ResponsibleDesignEngineer");
				if(relECR == null || relECR.length() == 0){
				strList.add("");
				}else{
				DomainObject domObj = new DomainObject(relECR);

				if (domObj.isKindOf(context, DomainConstants.TYPE_ECR) || domObj.isKindOf(context, DomainConstants.TYPE_ECO)) {
				String relECRId = relECR;
				setId(relECRId);
				DomainObject dObj = DomainObject.newInstance(context, relECRId);
				String sECRName =  dObj.getAttributeValue(context,ATTRIBUTE_RESPONSIBLE_DESIGN_ENGINEER);
				strList.add(sECRName);
			 }else{
			 strList.add("");
			 }
			}
				return strList;
               }catch(Exception ex){
             throw  new FrameworkException((String)ex.getMessage());
         }

	}


/**
     * Display the Design Responsibility Item field in ECO WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECO object Id
     * @throws Exception if the operations fails
     * @since EC - X3
     */

public Object  displayDesignResponsibilityItem(Context context,String[] args)throws Exception{
		StringBuffer sbReturnString = new StringBuffer();
		try{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			HashMap requestMap = (HashMap) programMap.get("requestMap");

			String relChange = (String) requestMap.get("OBJId");
			String strModCreate= (String)requestMap.get("CreateMode");
			String sDisplayValue = "";
			String sHiddenValue = "";
		   String strPartDrawingId = (String) requestMap.get("objectId");
			boolean hasRDO= true;

            //Relationship name
			String strRelationshipDesignResponsibility =
				PropertyUtil.getSchemaProperty(context,"relationship_DesignResponsibility");
			if("CreateECO".equals(strModCreate))
			{
					sDisplayValue = "";
					sHiddenValue = "";

				if (strPartDrawingId != null)
				{
					StringList strlObjSelects = new StringList(2);
					strlObjSelects.add(DomainConstants.SELECT_ID);
					strlObjSelects.add(DomainConstants.SELECT_NAME);
					StringList strlRelSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
					String strType = DomainConstants.TYPE_ORGANIZATION;

					DomainObject doPart = new DomainObject(strPartDrawingId);

					MapList mapListRDOs = doPart.getRelatedObjects(context,
											strRelationshipDesignResponsibility,
											strType,
											strlObjSelects,
											strlRelSelects,
											true,
											false,
											(short)1,
											null,
											null);

					if (mapListRDOs.size() > 0)
					{
						Map mapRDO = (Map) mapListRDOs.get(0);
						sDisplayValue = (String) mapRDO.get(DomainConstants.SELECT_NAME);
						sHiddenValue = (String) mapRDO.get(DomainConstants.SELECT_ID);
						hasRDO = false;
					}
				}

				if (hasRDO)
				{
				    //Start : IR-044112V6R2011
                    String sRdoTNR = (String)requestMap.get("sRdoTNR");
                    if(sRdoTNR == null || "null".equalsIgnoreCase(sRdoTNR)){
                        sRdoTNR = PropertyUtil.getAdminProperty(context, personAdminType,  context.getUser(),  PREFERENCE_DESIGN_RESPONSIBILITY);
                    }
                    //End : IR-044112V6R2011
					if(sRdoTNR != null && !"null".equals(sRdoTNR) && !"".equals(sRdoTNR)){
						//split the {T}{N}{R} value & get the objectId
						if(sRdoTNR.indexOf('}') > 0) {
							String sType = sRdoTNR.substring(1,sRdoTNR.indexOf('}'));
							sRdoTNR =sRdoTNR.substring(sRdoTNR.indexOf('}')+2);
							sDisplayValue = sRdoTNR.substring(0,sRdoTNR.indexOf('}'));
						  try{
							sHiddenValue = EngineeringUtil.getBusIdForTNR(context,sType,sDisplayValue,sRdoTNR.substring(sRdoTNR.indexOf('{')+1,sRdoTNR.length()-1));
						  }catch(Exception Ex){
							throw Ex;
						 }
						}
					}
				}

				sbReturnString.append("<input type=\"text\"  name=\"DesignResponsibilityDisplay\" readonly=\"true\" id=\"\" value=\"");
				sbReturnString.append(sDisplayValue);
				sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<input type=\"hidden\"  name=\"DesignResponsibilityOID\" value=\"");
				sbReturnString.append(sHiddenValue);
				sbReturnString.append("\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<input type=\"button\" name=\"btnCompany\" value=\"...\" onclick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace:CURRENT=policy_Organization.state_Active&amp;table=ENCAddExistingGeneralSearchResults&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;fieldNameActual=DesignResponsibilityOID&amp;fieldNameDisplay=DesignResponsibilityDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=RDOChooser&amp;HelpMarker=emxhelpfullsearch");
				sbReturnString.append("&amp;fieldNameOID=DesignResponsibilityOID&amp;relId=null&amp;suiteKey=EngineeringCentral','850','630')\">");
				sbReturnString.append("</input>");
                sbReturnString.append("<a href=\"JavaScript:clearRDO()\">");
                sbReturnString.append(strClear);
                sbReturnString.append("</a>");
					}
			else
			{
					DomainObject domObj = new DomainObject(relChange);
				  if(domObj.isKindOf(context, DomainConstants.TYPE_ECR) || domObj.isKindOf(context, DomainConstants.TYPE_ECO) || domObj.isKindOf(context, DomainConstants.TYPE_PART)){

							//Business Objects are selected by its Ids
							StringList objectSelects = new StringList();
							objectSelects.addElement(DomainConstants.SELECT_NAME);
							objectSelects.addElement(DomainConstants.SELECT_ID);
            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
			String sObjectName ="";
			String sObjectId ="";
					if(strModCreate!=null && strModCreate.equalsIgnoreCase("AssignToECO")||(strModCreate!=null && strModCreate.equalsIgnoreCase("MoveToECO") ) ||(strModCreate!=null && strModCreate.equalsIgnoreCase("AddToECO") ))
					{

				// Getting the Selected Object Id in array

			String strMemberIds= (String)requestMap.get("memberid");
			if(null != strMemberIds){
			StringTokenizer stz = new StringTokenizer(strMemberIds,",");
			ArrayList arrListDom = new ArrayList();
						if(stz.hasMoreElements())
						{
						String token = stz.nextToken();
                        String str1 = StringUtils.replace(token,"[", "");
                        String str2 = StringUtils.replace(str1,"]", "");
						// Creating Domain Object of the affected items which is selected
						DomainObject domAffected = new DomainObject(str2);
						arrListDom.add(str2);
						StringList busSelects = new StringList(2);
						busSelects.add(DomainConstants.SELECT_ID);
						busSelects.add(DomainConstants.SELECT_NAME);
						StringList relSelectsList1 = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
						String sType = DomainConstants.TYPE_ORGANIZATION;
						relationshipIdList = domAffected.getRelatedObjects(context,
																	strRelationshipDesignResponsibility,
																	sType,
																	busSelects,
																	relSelectsList1,
                                                    true,
																	false,
                                                    (short)1,
																	null,
																	null);


			}
			}
          }
		  if (relationshipIdList.size() > 0)
			{
				Map newMap = (Map)relationshipIdList.get(0);
				sObjectName=(String) newMap.get(DomainConstants.SELECT_NAME);
				sObjectId=(String) newMap.get(DomainConstants.SELECT_ID);
				sDisplayValue =sObjectName;
		                sHiddenValue = sObjectId;
				sbReturnString.append("<input type=\"text\"  name=\"DesignResponsibilityDisplay\" readonly=\"true\" id=\"\" value=\"");
				sbReturnString.append(sDisplayValue);
				sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<input type=\"hidden\"  name=\"DesignResponsibilityOID\" value=\"");
				sbReturnString.append(sHiddenValue);
				sbReturnString.append("\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<input type=\"button\" name=\"btnCompany\" disabled =\"true\" value=\"...\" onclick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace:CURRENT=policy_Organization.state_Active&amp;table=ENCAddExistingGeneralSearchResults&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;fieldNameActual=DesignResponsibilityOID&amp;fieldNameDisplay=DesignResponsibilityDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch");
				sbReturnString.append("&amp;fieldNameOID=DesignResponsibilityOID&amp;relId=null&amp;suiteKey=EngineeringCentral','850','630')\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<a href=\"JavaScript:basicClear('DoNotClear')\" disabled=\"true\">");
				sbReturnString.append(strClear);
				sbReturnString.append("</a>");
					}
					else
					{
				sbReturnString.append("<input type=\"text\"  name=\"DesignResponsibilityDisplay\" readonly=\"true\" id=\"\" value=\"");
				sbReturnString.append("");
				sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
				sbReturnString.append("</input>");
				sbReturnString.append("<input type=\"hidden\"  name=\"DesignResponsibilityOID\" value=\"");
				sbReturnString.append("");
				sbReturnString.append("\">");
				sbReturnString.append("</input>");
						sbReturnString.append("<input type=\"button\" name=\"btnCompany\" value=\"...\" onclick=\"javascript:showChooser('../common/emxFullSearch.jsp?field=TYPES=type_Organization,type_ProjectSpace:CURRENT=policy_Organization.state_Active&amp;table=ENCAddExistingGeneralSearchResults&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;formName=emxCreateForm&amp;fieldNameActual=DesignResponsibilityOID&amp;fieldNameDisplay=DesignResponsibilityDisplay&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;mode=Chooser&amp;chooserType=FormChooser&amp;HelpMarker=emxhelpfullsearch");
				sbReturnString.append("");
				sbReturnString.append("&amp;fieldNameOID=DesignResponsibilityOID&amp;relId=null&amp;suiteKey=EngineeringCentral','850','630')\">");
				sbReturnString.append("</input>");
				if(strModCreate!=null && (strModCreate.equalsIgnoreCase("AssignToECO")||
						 strModCreate.equalsIgnoreCase("AddToECO") ))
				{
				sbReturnString.append("<a href=\"JavaScript:clearRDO()\">");
				}
				else
				{
                   sbReturnString.append("<a href=\"JavaScript:basicClear('DesignResponsibilityDisplay')\">");
				}
                sbReturnString.append(strClear);
                sbReturnString.append("</a>");
			    }
				}
				else
				{
			 sbReturnString.append("");
			 }
			}
		}
		catch(Exception ex){
		}
		return sbReturnString.toString();
	}
/**
     * Connects the Affected Items in ECR tree menu in ECO WebForm.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains a MapList with the following as input arguments or entries:
     * objectId holds the context ECR/ECO object Id
     * New Value holds the newly selected Related ECR Object Id
     * @throws Exception if the operations fails
     * @since EC - X3
*/
	public int addAffectedItems(Context context, String[] args) throws Exception
	{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap   = (HashMap)programMap.get("paramMap");
			HashMap requestMap= (HashMap)programMap.get("requestMap");

			// New ECO Object Id
		String strECOId = (String)paramMap.get("objectId");

		// Selected Affected Item Ids
		String[] affectedItemsLists = (String []) requestMap.get("affectedItems");
		String [] affectedItemsList = null;
		if (affectedItemsLists != null)
			{
			HashSet uniqueId = new HashSet();
			StringTokenizer strTokAffectedItemsList =  new StringTokenizer(affectedItemsLists[0], "~");
			while (strTokAffectedItemsList.hasMoreTokens())
						{
				uniqueId.add(strTokAffectedItemsList.nextToken());

				         }
			affectedItemsList = new String[uniqueId.size()];
			uniqueId.toArray(affectedItemsList);
			uniqueId.clear();

			}

		String [] strCreateModes = (String []) requestMap.get("CreateMode");
		if (strCreateModes != null)
				{
			String [] strParentOIDs = (String []) requestMap.get("parentOID");
			if (strParentOIDs != null)
				{
				affectedItemsList = new String[1];
				affectedItemsList[0] = strParentOIDs[0];
			}
			}

		// Context ECR ID
		String [] strSourceECRIds = (String []) requestMap.get("sourceECRId");
		String strSourceECRId = null;
		if (strSourceECRIds != null)
                    {
			strSourceECRId = strSourceECRIds[0];
                    }

		// Context ECO ID
		String [] strSourceECOIds = (String []) requestMap.get("sourceECOId");
		String strSourceECOId = null;
		if (strSourceECOIds != null)
                    {
			strSourceECOId = strSourceECOIds[0];
                }


		if (strSourceECRId != null)
                    {
			ECR ecrSource = new ECR(strSourceECRId);
			ecrSource.assignAffectedItems(context, strECOId, affectedItemsList, true);
                    }
		else if (strSourceECOId != null)
					{
			ECO ecoSource = new ECO(strSourceECOId);
			ecoSource.moveAffectedItems(context, strECOId, affectedItemsList);
                }
                else
                {
			if (affectedItemsList != null)
						{
				ECO ecoSource = new ECO(strECOId);
				ecoSource.connectAffectedItems(context, affectedItemsList);
					}
				}


		return 0;
			}
	
	
	private MapList _globalSelectedObjects = new MapList();
    private Map _processSelectedObjets    = new HashMap();
	/**
	   * This method is used to store child objects     *
	   * @param context        the <code>matrix.db.Context</code> for user logged in
	   * @param children       a String with id for an object
	   * @param selected name of the Ad Hoc Route
	   * @param parent    locale used    
	   * @throws  Exception  
	   */
	  private void storeChildren(Context context,StringList children,StringList selected,String parent) throws Exception
	  {
	      children.retainAll(selected);       
	      children.size();
	      if(!children.isEmpty())
	      {
	          Map parentMap = (Map)_processSelectedObjets.get(parent);
	          MapList childrenMapList = new MapList();
	          
	          for(int i = 0,size = children.size() ; i < size ; i++)
	          {                
	              String id = (String)children.get(i);
	              Map child = (Map)_processSelectedObjets.get(id);
	              childrenMapList.add(child);
	          }
	          
	          parentMap.put("children",childrenMapList);
	      }       
	      
	  }
		
    public int ecoChangeProcessAction(matrix.db.Context context, String[] args) throws Exception
    {
		String SELECT_PREV_PARENT_ID 	= "previous.to["+DomainObject.RELATIONSHIP_EBOM+"].from.id";
		String SELECT_PREV_PARENT_STATE = "previous.to["+DomainObject.RELATIONSHIP_EBOM+"].from.current";
		String SELECT_ATTRIBUTE_REQUESTED_CHANGE = "attribute[" + PropertyUtil.getSchemaProperty(context,
		                        "attribute_RequestedChange") + "]";


		String strUnPromotedList = null;
		String strUnApprovedList = EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.AutoRevECRAffectedItemsErrorAffectedNotInApproved", context.getSession().getLanguage());

        boolean bolAllAffectedInApproved = true;
        boolean boolReqChangeNoneExists = false;

        try
        {
			PropertyUtil.setRPEValue(context, "MX_SKIP_PART_PROMOTE_CHECK", "true", false);

            StringList strlObjectSelects = new StringList(9);
            strlObjectSelects.addElement(SELECT_ID);
            strlObjectSelects.addElement(SELECT_TYPE);
            strlObjectSelects.addElement(SELECT_NAME);
            strlObjectSelects.addElement(SELECT_REVISION);
            strlObjectSelects.addElement(SELECT_POLICY);
            strlObjectSelects.addElement(SELECT_CURRENT);
            strlObjectSelects.addElement("state");
            strlObjectSelects.add(SELECT_PREV_PARENT_ID);
            strlObjectSelects.add(SELECT_PREV_PARENT_STATE);

            DomainObject.MULTI_VALUE_LIST.add(SELECT_PREV_PARENT_ID);
            DomainObject.MULTI_VALUE_LIST.add(SELECT_PREV_PARENT_STATE);

            StringList strlRelSelects = new StringList(1);
			strlRelSelects.addElement(SELECT_ATTRIBUTE_REQUESTED_CHANGE);

			StringList strlECOObjectSelects = new StringList(1);
			strlECOObjectSelects.addElement(SELECT_ID);
			StringList strlECORelSelects = new StringList(1);

            String  strRelPattern = RELATIONSHIP_AFFECTED_ITEM;

            String strWhereClause = null;
          //Start:to sort  Affected Item List
            String sEnableAISort = FrameworkProperties.getProperty("eServiceEngineeringCentral.EnableSortingOfECOAffectedItems");
            if("true".equalsIgnoreCase(sEnableAISort)){
           	String SELECT_RELATED_CHILD_LIST="from["+EngineeringConstants.RELATIONSHIP_EBOM+","+EngineeringConstants.RELATIONSHIP_PART_SPECIFICATION+"].to.id";
            	strlObjectSelects.add(SELECT_RELATED_CHILD_LIST);
            }
          //end:to sort  Affected Item List
         // get all the changed items connected to ECO, which are not in release state
             MapList mapListAffectedItems = getRelatedObjects( context,
                                                 strRelPattern,
                                                 "*",
                                                 strlObjectSelects,
                                                 strlRelSelects,
                                                 false,
                                                 true,
                                                 (short)1,
                                                 strWhereClause,
                                                 null);
             //Start:to sort  Affected Item List
             MapList finalAffectedItemList=new MapList();
            
             if(sEnableAISort.equalsIgnoreCase("true")){
            	 //clear the global variables
            	_globalSelectedObjects.clear();
                _processSelectedObjets.clear();	           
	            Map tempMapAffectedItem=null;
	            String sObjectId=null;
	            String sType=null;
	            StringList slAffectedItemIds=new StringList();
	                        
	            //below iteration formating a StringList of Affected Items IDs(which will use to fetch sequence no. info) and 
	            //a map having key(Affected Items IDs) and value(map coresponding to that Id) which will use for release process itteration
	            for(int count=0;count<mapListAffectedItems.size();count++){
	            	tempMapAffectedItem = (Map) mapListAffectedItems.get(count);
	            	sType=(String)tempMapAffectedItem.get("type");
					if(!"For Release".equals((String)tempMapAffectedItem.get(SELECT_ATTRIBUTE_REQUESTED_CHANGE))){
	            		finalAffectedItemList.add(tempMapAffectedItem);
	            		continue;
	            	}
	           	 	sObjectId=(String)tempMapAffectedItem.get("id");	           	 	
	           	 	slAffectedItemIds.add(sObjectId);	           	 	
	           	 	tempMapAffectedItem.put("sequence","0");
	           	 	
	           	 	_globalSelectedObjects.add(tempMapAffectedItem);
	           	 	_processSelectedObjets.put(sObjectId,tempMapAffectedItem);
	           	 	
	            }
	            	            
	            //iterate the  list of Affected Ids and insert the child list into the global map  with "children" as key          
	            for(int i = 0,size = slAffectedItemIds.size();i < size ; i++)
	            {
	                String id  = (String) slAffectedItemIds.get(i); 	                
	                Map temp=(Map) _processSelectedObjets.get(id);
	                
	                StringList children = EngineeringUtil.toStringList(temp.get("from["+EngineeringConstants.RELATIONSHIP_EBOM+"].to.id"));
	                children.addAll(EngineeringUtil.toStringList(temp.get("from["+EngineeringConstants.RELATIONSHIP_PART_SPECIFICATION+"].to.id")));
	                storeChildren(context,children,slAffectedItemIds,id);
	            }
	            //assign proper sequency no. to global map which contains Objects which need to sort
	            for(int k = 0 ,size = _globalSelectedObjects.size(); k < size; k++)
	            {
	                Map objectMap = (Map)_globalSelectedObjects.get(k);
	                EngineeringUtil.increaseSequenze(context,objectMap,(new Integer((String)objectMap.get("sequence")).intValue()));
	            }
	            //privious loop proper seq is assigned.in this loop removing unnecessary key "children" from sorted maplist bcoz that key used only for assign seq
	            for(int k = 0 ,size = _globalSelectedObjects.size(); k < size; k++)
	            {
	               EngineeringUtil.removeChildren(context,(Map)_globalSelectedObjects.get(k));            
	            }
	            //sort the global maplist and add to final list
	            if(_globalSelectedObjects.size()>0){
	            	_globalSelectedObjects.sort("sequence","desending","integer");
	            	finalAffectedItemList.addAll(_globalSelectedObjects);
	            }	            
             }
			            
            Iterator itrAffectedItems =	 null;
            if(sEnableAISort.equalsIgnoreCase("true")){
            	itrAffectedItems=finalAffectedItemList.iterator();
            }
            else
            	itrAffectedItems=mapListAffectedItems.iterator();
			//End: to sort the Affected Item List
			
			Map mapAffectedItem = null;
			Map mapTemp  = null;

			MapList mapListPartItems = new MapList();
			MapList mapListSpecItems = new MapList();

			String strPolicy = null;
			String strItemId = null;
			String strItemName = null;
			String strItemRevision = null;
			StringList strlState = new StringList();
			String strCurrent = null;
			String strApproved = null;
			String strRelease = null;
			String strObsolete = null;
			String strRequestedChange = null;
			String strParentId = null;
			String strParentState = null;

			StringList strlPrevparentId = new StringList();
			StringList strlPrevparentState = new StringList();

			while (itrAffectedItems.hasNext())
			{
				mapAffectedItem = (Map) itrAffectedItems.next();

				strPolicy = (String) mapAffectedItem.get(SELECT_POLICY);
				strItemId = (String) mapAffectedItem.get(SELECT_ID);
				strItemName = (String) mapAffectedItem.get(SELECT_NAME);
				strItemRevision = (String) mapAffectedItem.get(SELECT_REVISION);
				strlState = (StringList) mapAffectedItem.get("state");
				strCurrent = (String) mapAffectedItem.get(SELECT_CURRENT);
				strRequestedChange = (String) mapAffectedItem.get(SELECT_ATTRIBUTE_REQUESTED_CHANGE);

				strApproved = PropertyUtil.getSchemaProperty(context,"policy", strPolicy,"state_Approved");
				strRelease = PropertyUtil.getSchemaProperty(context,"policy", strPolicy,"state_Release");
				strObsolete = PropertyUtil.getSchemaProperty(context,"policy", strPolicy,"state_Obsolete");

				DomainObject doTemp = new DomainObject(strItemId);

				boolean isPart = doTemp.isKindOf(context,TYPE_PART);

				if (RANGE_NONE.equals(strRequestedChange) && isPart && !strRelease.equals(strCurrent)) {
					boolReqChangeNoneExists = true;
					break;
				}

				if (RANGE_FOR_OBSOLETE.equals(strRequestedChange) && isPart)
				{
					if (checkObjState(context,strlState, strCurrent, strObsolete, LT) == 0)
					{
						mapTemp = new HashMap();
						mapTemp.put(SELECT_ID, strItemId);
						mapTemp.put("State", strObsolete);
						mapTemp.put("CurrentState", strCurrent);
						mapTemp.put("AllStates", strlState);
						mapTemp.put("Requested Change", strRequestedChange);
						mapTemp.put(SELECT_NAME, strItemName);
						mapTemp.put(SELECT_REVISION, strItemRevision);
						mapListPartItems.add((Map) mapTemp);
					}
				}
				else if (!strRelease.equals(strCurrent) && ((RANGE_FOR_RELEASE.equals(strRequestedChange) || RANGE_FOR_REVISE.equals(strRequestedChange))))
				{

					if (checkObjState(context, strlState, strCurrent, strApproved, EQ) == 0)
					{
						mapTemp = new HashMap();
						mapTemp.put(SELECT_ID, strItemId);
						mapTemp.put("State", strRelease);
						mapTemp.put("CurrentState", strCurrent);
						mapTemp.put("AllStates", strlState);
						mapTemp.put("Requested Change", strRequestedChange);
						mapTemp.put(SELECT_NAME, strItemName);
						mapTemp.put(SELECT_REVISION, strItemRevision);

						if (isPart)
						{
							strParentId = null;
							strParentState = null;

							try
							{
								strlPrevparentId = (StringList)mapAffectedItem.get(SELECT_PREV_PARENT_ID);
								strlPrevparentState = (StringList)mapAffectedItem.get(SELECT_PREV_PARENT_STATE);
							}
							catch (Exception ex)
							{
								strParentId = (String)mapAffectedItem.get(SELECT_PREV_PARENT_ID);
								strParentState = (String)mapAffectedItem.get(SELECT_PREV_PARENT_STATE);

								if (strParentId != null && strParentState != null)
								{
                                        strlPrevparentId = new StringList();
                                        strlPrevparentState = new StringList();
                                    strlPrevparentId.addElement(strParentId);
									strlPrevparentState.addElement(strParentState);
								}
							}

							mapTemp.put(SELECT_PREV_PARENT_ID, strlPrevparentId);
							mapTemp.put(SELECT_PREV_PARENT_STATE, strlPrevparentState);
							mapTemp.put("Done", "false");
							mapListPartItems.add((Map) mapTemp);
						}
						else
						{
							mapListSpecItems.add((Map) mapTemp);
						}


					}
					else if (checkObjState(context, strlState, strCurrent, strApproved, LT) == 0)
					{
						strUnApprovedList += ", " + strItemName + " " + strItemRevision;
						bolAllAffectedInApproved = false;
					}
				}

			}

			if (boolReqChangeNoneExists) {
				String strAlertMessage = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(),"emxComponents.Common.Alert.EditAll");
				emxContextUtil_mxJPO.mqlNotice(context, strAlertMessage);
				return 1;
			}

			if (bolAllAffectedInApproved)
			{

                MapList mapListPromoteItems = new MapList();

                Iterator itrSpecs = mapListSpecItems.iterator();

                Map mapSpec = null;
                String strSpecStateName = null;
                String strSpecId = null;
                String strSpecCurrentState = null;
                StringList strlSpecAllStates = new StringList();
                String strSpecName = null;
                String strSpecRevision = null;

                boolean bolSpecSignature = true;


                while (itrSpecs.hasNext())
                {
                    mapSpec  = (Map) itrSpecs.next();
                    strSpecStateName  = (String) mapSpec.get("State");
                    strSpecId   = (String) mapSpec.get(SELECT_ID);
                    strSpecName   = (String) mapSpec.get(SELECT_NAME);
                    strSpecRevision   = (String) mapSpec.get(SELECT_REVISION);
                    strSpecCurrentState   = (String) mapSpec.get("CurrentState");
                    strlSpecAllStates   = (StringList) mapSpec.get("AllStates");

                    // check whether all the signature upto needed state approved, if so
                    // promote it the needed state as super user
                    bolSpecSignature = IsSignatureApprovedUptoGivenState(context, strSpecId, strlSpecAllStates, strSpecCurrentState, strSpecStateName);

                    if (bolSpecSignature)
                    {
                         mapListPromoteItems.add(mapSpec);

                    }
                    else
                    {
                        // if the affected item not promotable, intimate the user the same
                        if (strUnPromotedList == null)
                        {
                            strUnPromotedList = " " + strSpecName + " " + strSpecRevision;
                        }
                        else
                        {
                            strUnPromotedList += ", " + strSpecName + " " + strSpecRevision;
                        }
                    }
                }


				boolean bolAllDone = false;
                String strWhere = null;
                if(EngineeringUtil.isMBOMInstalled(context)){
                    strWhere = SELECT_ATTRIBUTE_REQUESTED_CHANGE + "== \"" + RANGE_FOR_RELEASE + "\"";
                }
				while (!bolAllDone)
				{
					bolAllDone = true;
					Iterator itrParts = mapListPartItems.iterator();

					Map mapPart = null;
					String strPartStateName = null;
					String strPartId = null;
					String strPartName = null;
					String strPartRevision = null;
					String strPartRequestedChange = null;
					String strPartParentState = null;
					String strPartParentId = null;
					StringList strlParentIdList = new StringList();
					StringList strlParentStateList = new StringList();

					boolean bolPartSignature = true;

					while (itrParts.hasNext())
					{
						mapPart  = (Map) itrParts.next();
						strPartStateName  = (String) mapPart.get("State");
						strPartId   = (String) mapPart.get(SELECT_ID);
						strPartName   = (String) mapPart.get(SELECT_NAME);
						strPartRevision   = (String) mapPart.get(SELECT_REVISION);
						strPartRequestedChange   = (String) mapPart.get("Requested Change");

                        String isDone = (String)mapPart.get("Done");
                        if ("true".equals(isDone))
                        continue;

                        // if this is an obsolete part then allow promote
						if (RANGE_FOR_OBSOLETE.equals(strPartRequestedChange))
						{
							mapListPromoteItems.add(mapPart);
							mapPart.put("Done", "true");
							continue;
						}

						// check whether all the signature upto needed state approved, if so
						// promote it the needed state as super user
						bolPartSignature = IsSignatureApprovedUptoGivenState(context, strPartId, strPartStateName);


						if (bolPartSignature)
						{
							strlParentIdList = (StringList)mapPart.get(SELECT_PREV_PARENT_ID);
                            // 359636 modified - start
                            strlParentStateList = (StringList)mapPart.get(SELECT_PREV_PARENT_STATE);
                            // 359636 modified - end

							boolean bolDoPromote = false;
							// if no parents then allow promote
							if (strlParentIdList == null || strlParentStateList == null)
							{
								mapListPromoteItems.add(mapPart);
								mapPart.put("Done", "true");

                                bolDoPromote = true;

							}
							else
							{
								bolDoPromote = true;
								for (int i=0; i < strlParentIdList.size() && bolDoPromote; i++)
								{
									strPartParentState = (String)strlParentStateList.elementAt(i);
									strPartParentId = (String)strlParentIdList.elementAt(i);

									//get parent ECO Id
									DomainObject doparent = new DomainObject(strPartParentId);

                                    MapList mapListECOs = doparent.getRelatedObjects( context,
																		 strRelPattern,
																		 DomainConstants.TYPE_ECO,
																		 strlECOObjectSelects,
																		 strlECORelSelects,
																		 true,
																		 false,
																		 (short)1,
																		 null,
																		 strWhere);

									Iterator itrECOs = mapListECOs.iterator();

									boolean bolSameECO = false;
									Map mapECO = null;

									while (itrECOs.hasNext())
									{
										mapECO = (Map) itrECOs.next();

										String strECOId = (String) mapECO.get(SELECT_ID);

										if (strECOId.equals(this.getObjectId()))
										{
											bolSameECO = true;
											break;
										}
									}


									// if any parent part is unreleased and also on this eco, then do not allow promote at this time
									if (strPartParentState != null && !strPartParentState.equals(STATE_ECPART_RELEASE) && bolSameECO)
									{
										bolDoPromote = false;
									}
								}
							}
							if (bolDoPromote)
							{
									mapListPromoteItems.add(mapPart);

									//whether promote was successful or not, still want to indicate this part has been handled
									mapPart.put("Done", "true");

									//update cache with new state if this is a parent on the ECO

									Iterator itrTemp = mapListPartItems.iterator();

									while (itrTemp.hasNext())
									{
										Map mapTempPart  = (Map) itrTemp.next();
										StringList strlTempparentIdList = (StringList)mapTempPart.get(SELECT_PREV_PARENT_ID);
										StringList strlTempparentStateList = (StringList)mapTempPart.get(SELECT_PREV_PARENT_STATE);
										for (int k=0; strlTempparentIdList != null && k < strlTempparentIdList.size(); k++)
										{
											String strTempparentId = (String)strlTempparentIdList.elementAt(k);
											if (strTempparentId.equals(strPartId))
											{
												strlTempparentStateList.set(k,STATE_ECPART_RELEASE);
											}
										}
										mapTempPart.put(SELECT_PREV_PARENT_STATE, strlTempparentStateList);
									}
							}
							else
							{
								// need to continue through the list again
								bolAllDone = false;
							}

						}
						else
						{
							// if the affected item not promotable, intimate the user the same
							if (strUnPromotedList == null)
							{
								strUnPromotedList = " " + strPartName + " " + strPartRevision;
							}
							else
							{
								strUnPromotedList += ", " + strPartName + " " + strPartRevision;
							}
							//whether promote was successful or not, still want to indicate this part has been handled
							mapPart.put("Done", "true");
							// dont continue through the list again since there were unpromotable parts.
							bolAllDone = true;
						}
					}
				}

				boolean bolContextPop = false;
				try
				{
					String strItemObjId   = null;
					String strItemObjName   = null;
					String strItemObjRev   = null;
					String strItemTragetState = null;
					ContextUtil.pushContext(context);
					DomainObject doItem = new DomainObject();
					Iterator itrPromoteList = mapListPromoteItems.iterator();
					while (itrPromoteList.hasNext())
					{
						try
						{
							Map mapItem  = (Map) itrPromoteList.next();
							strItemObjId   = (String)mapItem.get(SELECT_ID);
							strItemObjName   = (String)mapItem.get(SELECT_NAME);
							strItemObjRev   = (String)mapItem.get(SELECT_REVISION);
							strItemTragetState  = (String)mapItem.get("State");
							doItem.setId(strItemObjId);
							doItem.gotoState(context, strItemTragetState);
						}
						catch ( Exception exp)
						{
							// no need to throw any exception if changed item not promoteable,
							// b'coz of promote constraint.
							if (strUnPromotedList == null)
							{
								strUnPromotedList = " " + strItemObjName + " " + strItemObjRev;
							}
							else
							{
								strUnPromotedList += ", " + strItemObjName + " " + strItemObjRev;
							}
						}
					}
					//IR-019321 - Starts
                    String partVersionEnabled = FrameworkProperties.getProperty(context, "emxEngineeringCentral.Check.PartVersion");
                    if ("true".equalsIgnoreCase(partVersionEnabled)) {
                        if (strUnPromotedList == null) {
                            emxPartMarkupBase_mxJPO partMarkup = new emxPartMarkupBase_mxJPO(context, args);
                            partMarkup.cloneAndConnectMarkup(context, getInfo(context, DomainConstants.SELECT_ID));
                        }
                    }
                    //IR-019321 - Ends
				}
				catch ( Exception e)
				{
					   throw e;
				}
				finally
				{
					   if ( bolContextPop )
					   {
						   ContextUtil.popContext(context);
					   }
					   bolContextPop = false;
				}


			}
			else
			{
				DomainObject.MULTI_VALUE_LIST.remove(SELECT_PREV_PARENT_ID);
				DomainObject.MULTI_VALUE_LIST.remove(SELECT_PREV_PARENT_STATE);

				StringBuffer sbfFinalMessage=new StringBuffer();
				if(strUnApprovedList.indexOf(',')!=-1)
				{
				sbfFinalMessage.append(strUnApprovedList);
				sbfFinalMessage.append('\n');
				}
				throw new Exception(sbfFinalMessage.toString());
			}

        }
        catch (Exception ex)
        {
            DebugUtil.debug("ecoChangeProcessAction Exception :", ex.toString());
            emxContextUtil_mxJPO.mqlNotice(context,ex.getMessage());
            return 1;
        }
        finally
        {
            if (strUnPromotedList != null)
            {
                emxContextUtil_mxJPO.mqlNotice(context,
                    EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.AutoPromoteChangedItemToReleaseStateWarning",
                    context.getSession().getLanguage()) + strUnPromotedList);
            //added for the bug 315907
            return 1;
            }

        }


        return 0;
    }
/**
     * Checks if the current selected Policy uses Dynamic Approval Process.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds fieldMap to get policy name.
     * @return Boolean true if selected Policy uses Dynamic Approvals otherwise returns false.
     * @throws Exception if the operation fails.
     * @since BX3.
     */

     public static Boolean emxCheckCreateDynamicApprovalECO(Context context, String[] args)
          throws Exception
     {
         boolean dynamicApproval = false;

         String policyClassification = FrameworkUtil.getPolicyClassification(context, POLICY_ECO);
         if ("DynamicApproval".equals(policyClassification))
         {
             dynamicApproval = true;
         }
         return Boolean.valueOf(dynamicApproval);
     }


/**
     * Apply BOM Markups connected to the ECO, on ECO release.
     * @param context the eMatrix <code>Context</code> object
     * @param args contains objectid of the ECO in context
     * objectId holds the context ECO object Id
     * @throws Exception if the operations fails
     * @since EC - X3
     */
          public int ApplyApprovedMarkups(Context context, String[] args) throws Exception {
		//get the ECO Object id
		String objectId = args[0];
		StringList objectSelects = new StringList("id");
		DomainObject doECO = new DomainObject(objectId);
		String sEBOMRelationship    = PropertyUtil.getSchemaProperty(context, "relationship_EBOM");
		String sEBOMSubRelationship    = PropertyUtil.getSchemaProperty(context, "relationship_EBOMSubstitute");
		String strMqlQueryAI        ="print rel $1 select $2 $3 dump $4";
		boolean obsoletePart=false;
		String[] methodargs = new String[3];
		String[] init = new String[] {};
		String languageStr = context.getSession().getLanguage();

		String strQueryResultAI = MqlUtil.mqlCommand(context,strMqlQueryAI,sEBOMRelationship,"attribute","attribute[].type","|").trim();

		StringTokenizer st          = new StringTokenizer(strQueryResultAI,"|");

		int counter = st.countTokens()/2;
		java.util.Vector attrNameList = new java.util.Vector(counter);
		java.util.Vector attrTypeList = new java.util.Vector(counter);
		int tempcnt = 1;
		while(st.hasMoreTokens())
		{
			if(tempcnt <= counter) {
			  attrNameList.add(st.nextToken().trim());
			}
			else {
			  attrTypeList.add(st.nextToken().trim());
			}
			tempcnt++;
		 }

		 //* Remove the hidden attribs from the Vector*/
		 StringList HiddenAttribList = new StringList();
		 st = new StringTokenizer(strQueryResultAI,"|");
		 tempcnt = 1;
		 while(st.hasMoreTokens()) {
		 	String sTok = st.nextToken().trim();
			 if(tempcnt <= counter) {
				  if(FrameworkUtil.isAttributeHidden(context,sTok)) {
					int index = attrNameList.indexOf(sTok);
					attrNameList.removeElementAt(index);
					attrTypeList.removeElementAt(index);
					HiddenAttribList.add(sTok);
			 	 }
			 }
			tempcnt++;
		  }

		  String sEBOMRelName     = PropertyUtil.getSchemaProperty(context,"relationship_EBOM");
		  String sRelEBOMMarkup   = PropertyUtil.getSchemaProperty(context,"relationship_EBOMMarkup");
		  //Derive absolute names for the EBOM attributes
		  String sFindNoAttr      = PropertyUtil.getSchemaProperty(context,"attribute_FindNumber");
		  String sUsageAttr       = PropertyUtil.getSchemaProperty(context,"attribute_Usage");
		  String sQtyAttr         = PropertyUtil.getSchemaProperty(context,"attribute_Quantity");
		  String sRefDsgntrAttr   = PropertyUtil.getSchemaProperty(context,"attribute_ReferenceDesignator");
		  String sComponentAttr   = PropertyUtil.getSchemaProperty(context,"attribute_ComponentLocation");

		  Vector selectKeys       = new Vector();
		  HashMap dbSequence      = new HashMap();

		  // Get the user required key values for comparision. F/N is selected by default.
		  //GET THE ATTRIBUTE KEYS FROM THE PREVIOUS PAGE AND STORE THEM IN A VECTOR
		  //The dbSequence HashMap stores the index where the appropriate rel attribute will be stored in the relArray object
		  //Use FN and RD as keys
		  selectKeys.addElement(sFindNoAttr);
		  selectKeys.addElement(sRefDsgntrAttr);
		  for(int j=0; j<selectKeys.size();j++)
		  {
			  String strAttributeName = selectKeys.elementAt(j).toString();
			  for(int k=0; k<attrNameList.size();k++)
			  {
				  String strTempAttrName = (String) attrNameList.get(k);
				if(strTempAttrName.equals(strAttributeName)) {
					String jString=""+k;
					dbSequence.put(strAttributeName,jString);
				}
			  }
		  }
		  //Get absolute names from Matrix dB
		  String sXMLFormat = PropertyUtil.getSchemaProperty(context, "format_XML");
		  BusinessObject busObjPart = null;

  		  String fnUniqueness	= FrameworkProperties.getProperty(context, "emxEngineeringCentral.FindNumberUnique");
 		  String rdUniqueness = FrameworkProperties.getProperty(context, "emxEngineeringCentral.ReferenceDesignatorUnique");

		  StringList selectRelStmts = new StringList(5);

		  String sLkAttrFindNumber = "attribute[" + PropertyUtil.getSchemaProperty(context,"attribute_FindNumber") + "]";
		  String sLkAttrRefDef = "attribute[" + PropertyUtil.getSchemaProperty(context,"attribute_ReferenceDesignator") + "]";

		  StringList selectStmts = new StringList(1);
		  selectStmts.addElement("id");
	      selectRelStmts.addElement("id");
		  for (int i = 0; i < attrNameList.size(); i++)
		  {
			selectRelStmts.addElement("attribute[" + attrNameList.elementAt(i) + "]");
		  }
		  String relPattern = sEBOMRelName;
		  String typePattern = "*";

		  MapList mpApprovedMarkups = doECO.getRelatedObjects(context,
															 PropertyUtil.getSchemaProperty(context,"relationship_AppliedMarkup"),
															 PropertyUtil.getSchemaProperty(context,"type_BOMMarkup"),
															 objectSelects,
															 null,
															 false,
															 true,
															 (short)1,
															 "current == Approved",
															 null);

		int iSize = mpApprovedMarkups.size();
		//For each of the Markups
		for(int k=0;k<iSize;k++) {
			String strSubstituteRelId = null;
			Map mpMarkup = (Map)mpApprovedMarkups.get(k);
			String sMarkupId = (String) mpMarkup.get("id");
			DomainObject doMarkup = new DomainObject(sMarkupId);
			//get the object id of the part to which the EBOM markup is connected.
			String sPartId = doMarkup.getInfo(context,"to["+sRelEBOMMarkup+"].from.id");
			methodargs[0] = sPartId;
			busObjPart = new BusinessObject(sPartId);
			busObjPart.open(context);
			BusinessObject busObjMarkup = new BusinessObject(sMarkupId);
    		busObjMarkup.open(context);
			String[][] relArray = new String[1000][attrNameList.size()+1];
			int count = 0;
			DomainObject partObj = new DomainObject(sPartId);
			partObj.open(context);
            ExpansionIterator _objectSelect = partObj.getExpansionIterator(context, relPattern, typePattern,
                    selectStmts, selectRelStmts, false, true, (short)1,
                    null, null, (short)0,
                    false, false, (short)0, false);

			StringItr strItr = new StringItr(selectStmts);
			StringItr strRelItr = new StringItr(selectRelStmts);
			Part domObj = (Part)DomainObject.newInstance(context,
							DomainConstants.TYPE_PART,DomainConstants.ENGINEERING);
			domObj.setId(sPartId);
			ArrayList fnValues = new ArrayList();//Store DB F/N Values for uniqueness checking.
			ArrayList rdValues = new ArrayList();

			try{
			while(_objectSelect.hasNext())       //Start of While 1.
			{
				matrix.db.RelationshipWithSelect relSelect = _objectSelect.next();
				//get the Relationship Data
				Hashtable relData = relSelect.getRelationshipData();
				// Get PartID from Matrix
				strItr.next();

				// Get Relationship ID from Matrix
				strRelItr.next();
				String sRelId = (String)relData.get(strRelItr.obj());
				fnValues.add((String)relData.get(sLkAttrFindNumber));
				rdValues.add((String)relData.get(sLkAttrRefDef));
				// Get FindNumber from Matrix

				Vector relValue=new Vector();

				for(int i=0; i<attrNameList.size(); i++)
				{
				  strRelItr.next();
				  String tempValue=(String)relData.get(strRelItr.obj());
				  relValue.addElement(tempValue);
				}

				for(int i=0; i<attrNameList.size(); i++)
				{
				  relArray[count][i]=relValue.elementAt(i).toString();
				}
				relArray[count][attrNameList.size()]=sRelId;

				strRelItr.reset();
				strItr.reset();
				count++;

			} //End of While 1

			}catch(Exception exp){
		    	throw exp;
		    }finally{
	  			 partObj.close(context);
		    	_objectSelect.close();
		    }

			//Checkout the XML file from this EBOM Markup object
			String sbusObjMarkupName = busObjMarkup.getName().trim().replace(' ','-');
			char c1 = '\\';
			char c2 = '/';
			String sTransPath = context.createWorkspace();
			sTransPath = sTransPath.replace(c1,c2);
			sTransPath = sTransPath.substring(0, sTransPath.indexOf((context.getWorkspacePath()).replace(c1, c2)));
			java.io.File fEmatrixWebRoot = new java.io.File(sTransPath);
			matrix.db.File matrixFile = new matrix.db.File(sbusObjMarkupName+ ".xml", sXMLFormat);
			matrix.db.FileList matrixFileList = new matrix.db.FileList(1);
			matrixFileList.addElement(matrixFile);
			StringList strList = null;

			try
			{
				strList = busObjMarkup.checkoutFilesForView(context, false, sXMLFormat, matrixFileList, fEmatrixWebRoot.toString());
			}
			catch(MatrixException mex) //Could not checkout Markup XML
			{
				throw mex;
			}
			java.io.File srcXMLFile = new java.io.File(fEmatrixWebRoot, (String)strList.elementAt(0));
			if(srcXMLFile == null) {
				return 1;
			}

			//Try Building the (J)DOM from the .xml file checked out from the Markup object
			com.matrixone.jdom.Document docXML = null;
			com.matrixone.jdom.Element rootElement = null;

			try
			{
				com.matrixone.jdom.input.SAXBuilder builder = new com.matrixone.jdom.input.SAXBuilder();
				builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
				builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
				docXML = builder.build(srcXMLFile);
				rootElement = docXML.getRootElement();
			}
			catch(NoSuchElementException nsee)
			{
				throw nsee;
			}
			//Starting point is the fromRelationshipList tag in the Markup XML
			com.matrixone.jdom.Element fromRelList = rootElement.getChild("businessObject").getChild("fromRelationshipList");
			String strIsReplaceAction = null;
			com.matrixone.jdom.Element massupdateAction = rootElement.getChild("businessObject").getChild("massUpdateAction");
			if(strIsReplaceAction != null) {
				strIsReplaceAction = massupdateAction.getText();
			} else {
				strIsReplaceAction = "";
			}

			java.util.List fromRels = fromRelList.getChildren();
			java.util.Iterator itr = fromRels.iterator();
			// to check if the Markup is for an obsolete Part and to display message
			java.util.Iterator checkItr = fromRels.iterator();
			StringBuffer partNames=new StringBuffer();
			String strAttChgType = "";
			boolean isPartExists = true;
			boolean fnExists	 = false;
			boolean isMarkupUnique=true;
			StringBuffer nonUniquePart = new StringBuffer();
			StringBuffer nonUniqueRD = new StringBuffer();
			StringBuffer rdpart = new StringBuffer();
			String fnValueElement = "";
			String fnNameElement = "";

			while(checkItr.hasNext())
			{
			com.matrixone.jdom.Element relationshipElementCheck = (com.matrixone.jdom.Element) checkItr.next();
			com.matrixone.jdom.Element attrListElementCheck = relationshipElementCheck.getChild("attributeList");
			java.util.List attList = attrListElementCheck.getChildren("attribute");
			java.util.Iterator attItr = attList.iterator();
				while(attItr.hasNext())
				{
					com.matrixone.jdom.Element attElement = (com.matrixone.jdom.Element) attItr.next();
					fnNameElement  = attElement.getChildText("name");
					com.matrixone.jdom.Attribute attChgType = attElement.getAttribute("chgtype");
					if (attChgType!=null)
					{
						strAttChgType = attChgType.getValue();
						if("D".equals(strAttChgType))
						{
							if(fnNameElement.equals(DomainObject.ATTRIBUTE_FIND_NUMBER))
							{
								fnValueElement = attElement.getChildText("string");
								fnValues.remove(fnValueElement);
							}
						}
					}
				}
			}
			checkItr = fromRels.iterator();

			while(checkItr.hasNext())
			{
				com.matrixone.jdom.Element relationshipElementCheck = (com.matrixone.jdom.Element) checkItr.next();
				com.matrixone.jdom.Element relatedObjectElementCheck = relationshipElementCheck.getChild("relatedObject").getChild("businessObjectRef");
				String relationshipType = relationshipElementCheck.getChild("relationshipDefRef").getText();
				if(relationshipType.equals(sEBOMSubRelationship)) {
					//This check not required as RD and FN will not be modifiable for Substitute.
					continue;
				}
				com.matrixone.jdom.Element attrListElementCheck = relationshipElementCheck.getChild("attributeList");
				com.matrixone.jdom.Element objTypeElementCheck = relatedObjectElementCheck.getChild("objectType");
				com.matrixone.jdom.Element objNameElementCheck = relatedObjectElementCheck.getChild("objectName");
				com.matrixone.jdom.Element objRevElementCheck = relatedObjectElementCheck.getChild("objectRevision");
				com.matrixone.jdom.Element objVaultElementCheck = relatedObjectElementCheck.getChild("vaultRef");

				//Get only Find Numbers and reference Designator for Change Types C and A and check again db values present in fnValues ArrayList
				//Incase of Error assign the Type Name Revision of invalid entry to a StringBuffer
				//continue the checking only if fnExists boolean is false

					java.util.List attList = attrListElementCheck.getChildren("attribute");
					java.util.Iterator attItr = attList.iterator();
					while(attItr.hasNext())
					{
						com.matrixone.jdom.Element attElement = (com.matrixone.jdom.Element) attItr.next();
						fnNameElement  = attElement.getChildText("name");
						com.matrixone.jdom.Attribute attChgType = attElement.getAttribute("chgtype");
						if (attChgType!=null)
						{
							strAttChgType = attChgType.getValue();
							if("true".equals(fnUniqueness) || "false".equals(fnUniqueness) && "false".equals(rdUniqueness))
							{

								if("C".equals(strAttChgType))
								{

									fnValueElement = attElement.getChildText("newvalue");
									//Added or modified : Bug - 320255
									String oldfnValueElement = attElement.getChildText("string");
									if(fnNameElement.equals(DomainObject.ATTRIBUTE_FIND_NUMBER))
									{
										if(!fnExists)
										{
											if(fnValues.contains(fnValueElement))
											{
												fnExists = true;
												nonUniquePart.append(objTypeElementCheck.getText());
												nonUniquePart.append(' ');
												nonUniquePart.append(objNameElementCheck.getText());
												nonUniquePart.append(' ');
												nonUniquePart.append(objRevElementCheck.getText());
												break;
											}
											else
											{
												 //Added or modified : Bug - 320255
												fnValues.remove(oldfnValueElement);
												fnValues.add(fnValueElement);
											}
										}
									}
								}
							}

							if("true".equals(rdUniqueness))
							{
								if("C".equals(strAttChgType))
								{
								   fnValueElement = attElement.getChildText("newvalue");

								 if(fnNameElement.equals(DomainObject.ATTRIBUTE_REFERENCE_DESIGNATOR))
								 {
									 if (fnValueElement !=null && !fnValueElement.equals("null") && !fnValueElement.equals(""))
									 {

												nonUniqueRD.append(objTypeElementCheck.getText());
												nonUniqueRD.append(' ');
												nonUniqueRD.append(objNameElementCheck.getText());
												nonUniqueRD.append(' ');
												nonUniqueRD.append(objRevElementCheck.getText());
												rdpart.append(fnValueElement);
												rdpart.append(',');

									 }

								 }
							   }
						   }

						   if(fnUniqueness.equalsIgnoreCase("true") || fnUniqueness.equalsIgnoreCase("false") && rdUniqueness.equalsIgnoreCase("false"))
						   {

								if("A".equals(strAttChgType))
								{
									fnValueElement = attElement.getChildText("string");

									if(fnNameElement.equals(DomainObject.ATTRIBUTE_FIND_NUMBER))
									{
										if(!fnExists)
										{
											if(fnValues.contains(fnValueElement))
											{
												fnExists = true;
												nonUniquePart.append(objTypeElementCheck.getText());
												nonUniquePart.append(' ');
												nonUniquePart.append(objNameElementCheck.getText());
												nonUniquePart.append(' ');
												nonUniquePart.append(objRevElementCheck.getText());
												break;
											}
										}//end of if fnExists
									}//end of if fnNameElement
								}
							}
							if(rdUniqueness.equalsIgnoreCase("true"))
							{
								if("A".equals(strAttChgType))
								{
									fnValueElement = attElement.getChildText("string");
								 if(fnNameElement.equals(DomainObject.ATTRIBUTE_REFERENCE_DESIGNATOR))
								 {

									 if (fnValueElement !=null && !fnValueElement.equals("null") && !fnValueElement.equals(""))
									 {
										nonUniqueRD.append(objTypeElementCheck.getText());
										nonUniqueRD.append(' ');
										nonUniqueRD.append(objNameElementCheck.getText());
										nonUniqueRD.append(' ');
										nonUniqueRD.append(objRevElementCheck.getText());
										rdpart.append(fnValueElement);
										rdpart.append(',');
									   }


								 }
								}//end of if strAttChgType
							}
						}//end of if attChgType
					}//end of while attItr
				BusinessObject bomObjectCheck = null;
				String rev = "";
				if(objRevElementCheck != null)
					rev = objRevElementCheck.getText();
				try
				{
				  bomObjectCheck = new BusinessObject(objTypeElementCheck.getText(),objNameElementCheck.getText(),rev,objVaultElementCheck.getText());
				  bomObjectCheck.open(context);
				  String sCurrentNameCheck=bomObjectCheck.getName();
				  com.matrixone.apps.domain.DomainObject domCheck = new com.matrixone.apps.domain.DomainObject(bomObjectCheck);
				  String state = domCheck.getInfo(context, DomainObject.SELECT_CURRENT);
				  BusinessObject revObjectCheck = domCheck.getLastRevision(context);
				  com.matrixone.apps.domain.DomainObject dom2Check = new com.matrixone.apps.domain.DomainObject(revObjectCheck);
				  String state2 = dom2Check.getInfo(context, DomainObject.SELECT_CURRENT);
				  if(state.equals(DomainObject.STATE_PART_OBSOLETE) && !state2.equals(DomainObject.STATE_PART_RELEASE))
				   {
					   obsoletePart=true;
					   partNames.append(sCurrentNameCheck);
					   partNames.append(" , ");
				   }
				   bomObjectCheck.close(context);
				   revObjectCheck.close(context);

				}
				catch(Exception e)
				{

				   partNames.append(objNameElementCheck.getText());
				   partNames.append(" Rev ");
				   partNames.append(objRevElementCheck.getText());
				   isPartExists = false;
				}

			}
			String markupval=rdpart.toString();
			if(rdUniqueness.equalsIgnoreCase("true"))
			{
				methodargs[1]= markupval;
				methodargs[2]=languageStr;
				if(!"".equals(markupval))
				{
					isMarkupUnique = ((Boolean)JPO.invoke(context, "emxPart", init, "isEBOMUnique", methodargs,Boolean.class)).booleanValue();
				}
			}
			if(isPartExists)
			{
				if(obsoletePart)
				{
					MqlUtil.mqlCommand(context,"notice $1","Cannot apply Markup on Obsolete part "+partNames);
					continue;
				}
				else {
					if(!fnExists && isMarkupUnique) {
						String strSaveEBOMRelId = null;
						while(itr.hasNext())
						{
							com.matrixone.jdom.Element relationshipElement = (com.matrixone.jdom.Element) itr.next();
							com.matrixone.jdom.Element relatedObjectElement = relationshipElement.getChild("relatedObject").getChild("businessObjectRef");
							String relationshipType = relationshipElement.getChild("relationshipDefRef").getText();
							if(relationshipType.equals(sEBOMSubRelationship)) {
									strSubstituteRelId = relationshipElement.getChild("relationshipDefRef").getAttribute("substituteRelId").getValue();
							}
							com.matrixone.jdom.Element objTypeElement = relatedObjectElement.getChild("objectType");
							com.matrixone.jdom.Element objNameElement = relatedObjectElement.getChild("objectName");
							com.matrixone.jdom.Element objRevElement = relatedObjectElement.getChild("objectRevision");
							com.matrixone.jdom.Element objVaultElement = relatedObjectElement.getChild("vaultRef");
							/*Need to know if it is a replace action for EBOM Substitute. If the action is Replace.
								then save the EBOM Relationship Id so that an "Add" can be performed.
							*/
							String rev = "";
							if(objRevElement != null)
							{
								rev = objRevElement.getText();
							}

							BusinessObject bomObject = new BusinessObject(objTypeElement.getText(),objNameElement.getText(),rev,objVaultElement.getText());
							com.matrixone.jdom.Element attrListElement = relationshipElement.getChild("attributeList");
							java.util.List attrList = attrListElement.getChildren("attribute");
							java.util.Iterator attrItr = attrList.iterator();
							String sAttrChildStrPtr = "string";
							String sAttrChildRealPtr = "real";
							String sAttrChildIntegerPtr = "integer";
							String sAttrChildBooleanPtr = "boolean";
							String sAttrChildDateTimePtr = "timestamp";
							String sAttrChildDateTimeXMLPtr = "datetime";
							int sQtyOld = 0;
							int sQtyNew =0;
							int sQty = 0;
							String fQty="";
							String sAttChgType = "";

							bomObject.open(context);

							matrix.db.Relationship relationObject1=null;
							matrix.db.RelationshipType relType1 = new RelationshipType(sEBOMRelName);


							//While Applying Mark Up verify if the current object is in Obsolete State and Any latest revision exists in Release state
							//If true then apply the latest Released revision to the bom.
							//If no latest revision exists and the current Part is in Obsolete then do not apply the markup to BOM and display appropriate message.

							Hashtable partObsolete = new Hashtable();

							com.matrixone.apps.domain.DomainObject dom1 = new com.matrixone.apps.domain.DomainObject(bomObject);
							String state1 = dom1.getInfo(context, DomainObject.SELECT_CURRENT);
							BusinessObject revObject1 = dom1.getLastRevision(context);
							com.matrixone.apps.domain.DomainObject dom3 = new com.matrixone.apps.domain.DomainObject(revObject1);
							String state3 = dom3.getInfo(context, DomainObject.SELECT_CURRENT);
							String sName = dom3.getInfo(context, DomainObject.SELECT_NAME);
							String sType1 = dom3.getInfo(context, DomainObject.SELECT_TYPE);
							String sRev = dom3.getInfo(context, DomainObject.SELECT_REVISION);
							String sVault = dom3.getInfo(context, DomainObject.SELECT_VAULT);

							if (sEBOMSubRelationship.equals(relationshipType.trim())) {
							//if the chosen revision in the Markup is obsolete, simply connect the latest revision and populate attributes
							if(state1.equals(DomainObject.STATE_PART_OBSOLETE) && state3.equals(DomainObject.STATE_PART_RELEASE))
							{
								relationObject1 = busObjPart.connect(context,relType1,true,revObject1);
								revObject1.close(context);
								bomObject.close(context);

								if(partObsolete.isEmpty())
								{
									partObsolete.put("Name", sName);
									partObsolete.put("Type",sType1);
									partObsolete.put("Rev",sRev);
									partObsolete.put("Vault",sVault);
									// using this AttributeList created to get the values from XML and update the attributes of "relationObject1" defined earlier
									matrix.db.AttributeList existattList= new matrix.db.AttributeList();
									while(attrItr.hasNext())
									{
										com.matrixone.jdom.Element attrElement = (com.matrixone.jdom.Element) attrItr.next();
										String sAttrName = attrElement.getChildText("name");
										String sAttrType = attrTypeList.elementAt(attrNameList.indexOf(sAttrName)).toString();

										if(!HiddenAttribList.contains(sAttrName))
										{

											if(sAttrName.equals(sFindNoAttr))
											{
												partObsolete.put(sFindNoAttr,attrElement.getChildText(sAttrChildStrPtr));
												matrix.db.Attribute tempAttributeElement1 = new matrix.db.Attribute(new matrix.db.AttributeType(sAttrName),attrElement.getChildText(sAttrChildStrPtr));
												existattList.addElement(tempAttributeElement1);
											}
											else if(sAttrName.equals(sQtyAttr))
											{
												String finalVal = "";
												if((finalVal = attrElement.getChildText("newvalue")) == null) {
													finalVal = attrElement.getChildText(sAttrChildRealPtr);
												}

												if(sAttrType.equals(sAttrChildIntegerPtr))
													finalVal = (" " + (new Float(finalVal)).intValue());

												partObsolete.put(sQtyAttr,finalVal);
												matrix.db.Attribute tempAttributeElement2 = new matrix.db.Attribute(new matrix.db.AttributeType(sAttrName), finalVal);
												existattList.addElement(tempAttributeElement2);
											}
											else if(sAttrName.equals(sUsageAttr))
											{
												partObsolete.put(sUsageAttr,attrElement.getChildText(sAttrChildStrPtr));
												matrix.db.Attribute tempAttributeElement3 = new matrix.db.Attribute(new matrix.db.AttributeType(sAttrName),attrElement.getChildText(sAttrChildStrPtr));
												existattList.addElement(tempAttributeElement3);
											}
											else if(sAttrName.equals(sRefDsgntrAttr))
											{
												partObsolete.put(sRefDsgntrAttr,attrElement.getChildText(sAttrChildStrPtr));
												matrix.db.Attribute tempAttributeElement4 = new matrix.db.Attribute(new matrix.db.AttributeType(sAttrName),attrElement.getChildText(sAttrChildStrPtr));
												existattList.addElement(tempAttributeElement4);
											}
											else if(sAttrName.equals(sComponentAttr))
											{
												partObsolete.put(sComponentAttr,attrElement.getChildText(sAttrChildStrPtr));
												matrix.db.Attribute tempAttributeElement5 = new matrix.db.Attribute(new matrix.db.AttributeType(sAttrName),attrElement.getChildText(sAttrChildStrPtr));
												existattList.addElement(tempAttributeElement5);
											}

										}// End of if(!HiddenAttribList.contains(sAttrName))

									}// End of while(attrItr.hasNext())
									// After iteration,. the "existattList" will have necessary values and this is used to update "relationObject1"
									relationObject1.open(context);
									relationObject1.setAttributes(context,existattList);
									relationObject1.update(context);
									relationObject1.close(context);
								} //End of if(partObsolete.isEmpty())

							} //End of if(state1.equals(DomainObject.STATE_PART_OBSOLETE) && state3.equals(DomainObject.STATE_PART_RELEASE))
							} //End of if(relationshipType.equals("EBOM Subsitute"))
							HashMap storedAttributeValues=new HashMap();
							HashMap xmlAttributeValues = new HashMap();
							while(attrItr.hasNext())
							{
							  com.matrixone.jdom.Element attrElement = (com.matrixone.jdom.Element) attrItr.next();
							  com.matrixone.jdom.Attribute attrChgType = attrElement.getAttribute("chgtype");
							  String sAttrName = attrElement.getChildText("name");

							  if(!HiddenAttribList.contains(sAttrName)) {
							   for(int j=0; j<attrNameList.size();j++)
								   {
									  String strAttributeName =attrNameList.elementAt(j).toString();
									  String strAttributeType =attrTypeList.elementAt(j).toString();

									   if (sAttrName.equals(strAttributeName))
										{
										 String attrValue = attrElement.getChildText(strAttributeType);
										 storedAttributeValues.put(strAttributeName,attrValue);
										 xmlAttributeValues.put(strAttributeName,attrValue);
										}
  							    }


								if(attrChgType!= null)
								{
								   sAttChgType = attrChgType.getValue();
								   if("C".equals(sAttChgType))
								   {
									  for(int j=0; j<attrNameList.size();j++)
									  {
										  String strAttributeName =attrNameList.elementAt(j).toString();
										  String strAttributeType =attrTypeList.elementAt(j).toString();


										  if (sAttrName.equals(strAttributeName))
											{
											 if(strAttributeType.equals(sAttrChildStrPtr))
											 {
												String attrNewValue = attrElement.getChildText("newvalue");
												String attrValue="";
												  if(attrNewValue==null)
												  {
														 attrValue = attrElement.getChildText(sAttrChildStrPtr);
												  }else{
														 attrValue = attrNewValue;
												  }
												storedAttributeValues.put(strAttributeName,attrValue);
												break;
											 }
											 else if(strAttributeType.equals(sAttrChildRealPtr))
											 {
												  double attrNewValue = (new Double(attrElement.getChildText("newvalue"))).doubleValue();
												  String attrValue="";
												  if(attrNewValue==0)
												  {
														 attrValue = attrElement.getChildText(sAttrChildRealPtr);
												  }else{
														 attrValue = attrElement.getChildText("newvalue");
												  }
												storedAttributeValues.put(strAttributeName,attrValue);
												break;

											 }
											 else if(strAttributeType.equals(sAttrChildIntegerPtr))
											 {
												   sQtyNew = (new Integer(attrElement.getChildText("newvalue"))).intValue();


												   if(sQtyNew==0)
												   {
													  sQty = (new Integer(attrElement.getChildText(sAttrChildIntegerPtr))).intValue();
												   }else{
													  sQtyOld = (new Integer(attrElement.getChildText("oldvalue"))).intValue();
													  sQty = sQtyNew-sQtyOld ;
												   }
												fQty=" "+sQty;
												storedAttributeValues.put(strAttributeName,fQty);
												break;

											 }
											 else if(strAttributeType.equals(sAttrChildBooleanPtr))
											 {
												String attrNewValue = attrElement.getChildText("newvalue");
												String attrValue="";
												  if(attrNewValue==null)
												  {
														 attrValue = attrElement.getChildText(sAttrChildBooleanPtr);
												  }else{
														 attrValue = attrNewValue;
												  }

												storedAttributeValues.put(strAttributeName,attrValue);
												break;
											 }
											 else if(strAttributeType.equals(sAttrChildDateTimePtr))
											 {
											   String attrNewValue = attrElement.getChildText("newvalue");
											   String attrValue="";
												 if(attrNewValue==null)
												 {
														attrValue = attrElement.getChildText(sAttrChildDateTimeXMLPtr);
												 }else{
														attrValue = attrNewValue;
												 }

											   storedAttributeValues.put(strAttributeName,attrValue);
											   break;
											 }
										   }
									   }
									}  // end of if("C")

								  if("A".equals(sAttChgType))
								   {
									 for(int j=0; j<attrNameList.size();j++)
									 {
										String strAttributeName =attrNameList.elementAt(j).toString();
										String strAttributeType =attrTypeList.elementAt(j).toString();
										 if (sAttrName.equals(strAttributeName))
										 {

										   if(strAttributeType.equals(sAttrChildStrPtr))
										   {

											  String attrValue=attrElement.getChildText(sAttrChildStrPtr);
											  storedAttributeValues.put(strAttributeName,attrValue);
											  break;
										   }
										   else if(strAttributeType.equals(sAttrChildRealPtr))
										   {

											  String attrValue = attrElement.getChildText(sAttrChildRealPtr);
											  String finalQTY="";
											  if (attrValue != null && attrValue.length() != 0)
											  {
												  finalQTY = attrValue;
											  }
											  else
											  {
												  finalQTY = "0";
											  }

											  storedAttributeValues.put(strAttributeName,finalQTY);
											  break;
										   }
										   else if(strAttributeType.equals(sAttrChildIntegerPtr))
										   {

											  sQty = (new Integer(attrElement.getChildText(sAttrChildIntegerPtr))).intValue();
											  String finalQTY="";
											  fQty=finalQTY+sQty;
											  storedAttributeValues.put(strAttributeName,fQty);
											  break;
										   }
										   else if(strAttributeType.equals(sAttrChildBooleanPtr))
										   {


											  String attrValue=attrElement.getChildText(sAttrChildBooleanPtr);
											  storedAttributeValues.put(strAttributeName,attrValue);
											  break;
										   }
										   else if(strAttributeType.equals(sAttrChildDateTimePtr))
										   {


											  String attrValue=attrElement.getChildText(sAttrChildDateTimeXMLPtr);
											  storedAttributeValues.put(strAttributeName,attrValue);
											  break;
										   }
										 }
									 }

								   }  // end of if("A")
								   if("D".equals(sAttChgType))
								   {
									 for(int j=0; j<attrNameList.size();j++)
									 {
										String strAttributeName =attrNameList.elementAt(j).toString();
										String strAttributeType =attrTypeList.elementAt(j).toString();



										 if (sAttrName.equals(strAttributeName))
										 {
										   if(strAttributeType.equals(sAttrChildStrPtr))
										   {
											  String attrValue=attrElement.getChildText(sAttrChildStrPtr);
											  storedAttributeValues.put(strAttributeName,attrValue);
											  break;
										   }
										   else if(strAttributeType.equals(sAttrChildRealPtr))
										   {
											  storedAttributeValues.put(strAttributeName,attrElement.getChildText(sAttrChildRealPtr));
											  break;
										   }
										   else if(strAttributeType.equals(sAttrChildIntegerPtr))
										   {
											  sQty = (new Integer(attrElement.getChildText(sAttrChildIntegerPtr))).intValue();
											  fQty=" "+sQty;
											  storedAttributeValues.put(strAttributeName,fQty);
											  break;
										   }
										   else if(strAttributeType.equals(sAttrChildBooleanPtr))
										   {
											  String attrValue=attrElement.getChildText(sAttrChildBooleanPtr);
											  storedAttributeValues.put(strAttributeName,attrValue);
											  break;
										   }
										   else if(strAttributeType.equals(sAttrChildDateTimePtr))
										   {
											  String attrValue=attrElement.getChildText(sAttrChildDateTimeXMLPtr);
											  storedAttributeValues.put(strAttributeName,attrValue);
											  break;
										   }
										 }
									  }
								   }  // End of if(sAttChgType.equals("D"))
								 } // End of if(attrChgType!= null)
							   } //End of if(!HiddenAttribList.contains(sAttrName))
							}  // End of while(attrItr.hasNext())
							// According to the change type compare and merge
							int indexC=-1;
							int indexD=-1;
							if("C".equals(sAttChgType))
							{
								for(int i=0;i<count;i++)
								{
									String XMLKey ="";
									String databaseKey ="";
									// Create the comparision key based on user input.
									for(int j=0; j<selectKeys.size();j++)
									{
										String hashMapKeyValue="";
										String strAttributeKeyName =selectKeys.elementAt(j).toString();
										hashMapKeyValue=(String)xmlAttributeValues.get(strAttributeKeyName);
										if (hashMapKeyValue !=null && !hashMapKeyValue.equals("null") && !hashMapKeyValue.equals(""))
										{
											hashMapKeyValue=hashMapKeyValue.trim();
											XMLKey=XMLKey+hashMapKeyValue;
										}
										String hashMapKeyPosition=(String)dbSequence.get(strAttributeKeyName);
										int pos=Integer.parseInt(hashMapKeyPosition);
										databaseKey=databaseKey+relArray[i][pos];
									}
									// #259225 - reverse test to not (!) equal
									if(XMLKey.equals(databaseKey) || "".equals(XMLKey) )
									{
										indexC=i;
										break;
									}
								}
								if(indexC!=-1)
								{
									// Create Attribute List for Relationship
									matrix.db.AttributeList attList= new matrix.db.AttributeList();
									for(int j=0; j<attrNameList.size();j++)
									{
										String strAttributeName =attrNameList.elementAt(j).toString();
										String strAttributeTypeName =attrTypeList.elementAt(j).toString();
										String hashMapValue=(String)storedAttributeValues.get(strAttributeName);

										if (hashMapValue !=null && !hashMapValue.equals("null") && !hashMapValue.equals(""))
										{
											hashMapValue=hashMapValue.trim();
											if(strAttributeTypeName.equals(sAttrChildRealPtr))
											{
												matrix.db.Attribute tempAttributeRealElement = new matrix.db.Attribute(new matrix.db.AttributeType(strAttributeName),hashMapValue);
												attList.addElement(tempAttributeRealElement);
											}
											else
											{
												matrix.db.Attribute tempAttributeStringElement = new matrix.db.Attribute(new matrix.db.AttributeType(strAttributeName),hashMapValue);
												attList.addElement(tempAttributeStringElement);
											}
										}
									}
									// create a relationship object

//									matrix.db.Relationship relationObject=null;

									String sRelIdforDC = relArray[indexC][attrNameList.size()];
									//EBOM Substitute, make the relationship to point to the Substitute Relationship
									if(relationshipType.equals(sEBOMSubRelationship)) {
										sRelIdforDC = strSubstituteRelId;
									}
									matrix.db.Relationship relToDC = new matrix.db.Relationship(sRelIdforDC);

									try{
										relToDC.setAttributes(context,attList);
										relToDC.update(context);
									}
									catch(MatrixException me){
										throw me;
									}
								} //End of if(indexC!=-1)
							} // End of if(sAttChgType.equals("C"))


							if("A".equals(sAttChgType))
							{
							   // Create Attribute List for Relationship
							   matrix.db.AttributeList attList= new matrix.db.AttributeList();
							   for(int j=0; j<attrNameList.size();j++)
							   {
								 String strAttributeName =attrNameList.elementAt(j).toString();
								 String strAttributeTypeName =attrTypeList.elementAt(j).toString();
								 String hashMapValue=(String)storedAttributeValues.get(strAttributeName);
								 if (hashMapValue !=null && !hashMapValue.equals("null") && !hashMapValue.equals(""))
								 {
								   hashMapValue=hashMapValue.trim();
								   if(strAttributeTypeName.equals(sAttrChildRealPtr))
									{
									  matrix.db.Attribute tempAttributeRealElement = new matrix.db.Attribute(new matrix.db.AttributeType(strAttributeName),hashMapValue);
									  attList.addElement(tempAttributeRealElement);
									}
								   else
									{
									  matrix.db.Attribute tempAttributeStringElement = new matrix.db.Attribute(new matrix.db.AttributeType(strAttributeName),hashMapValue);
									  attList.addElement(tempAttributeStringElement);
									}
								 }
							   }

								// Do a connect of bomObject to the destination Part
								matrix.db.Relationship relationObject=null;
								matrix.db.RelationshipType relType = new RelationshipType(sEBOMRelName);
								try {
									//Added for Bug No 322982 2 Dated 6/14/2007 Begin
									String strMessageCheck=EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.EBOMMarkup.EBOMMarkupApplyCheck",languageStr);
									BusinessObject revBomObject = bomObject.getNextRevision(context);

									if(!(revBomObject.getRevision().equals("")) )
									{
										String currentState = mxBus.getCurrentState(context,revBomObject).getName();
										if(currentState.equals(DomainObject.STATE_PART_RELEASE))
										{
										 	MqlUtil.mqlCommand(context, "notice $1"," "+objTypeElement.getText()+" "+objNameElement.getText()+" "+objRevElement.getText()+" "+strMessageCheck);
										 	continue;
										}
									}
									//Added for Bug No 322982 2 Dated 6/14/2007 Ends
									bomObject.open(context);
									if(relationshipType.equals(sEBOMSubRelationship)) {
										String strEBOMRelId = null;
										//Check if the EBOM Id was saved for a Replace action. If so, use it
										if(strIsReplaceAction != null && strIsReplaceAction.equalsIgnoreCase("Replace") && strSaveEBOMRelId != null ) {
											strEBOMRelId = strSaveEBOMRelId;
										} else {
											MapList mplEBOM = DomainRelationship.getInfo(context, new String[] {strSubstituteRelId}, new StringList("fromrel["+sEBOMRelationship+"].id"));
											strEBOMRelId = (String)((Map)mplEBOM.get(0)).get("fromrel["+sEBOMRelationship+"].id");
										}
										RelToRelUtil relSub = new RelToRelUtil();
										String strNewSubId = null;
										strNewSubId = relSub.connect(context, sEBOMSubRelationship,strEBOMRelId, bomObject.getObjectId(),false, true);
										relationObject = new matrix.db.Relationship(strNewSubId);
									} else {
										relationObject = busObjPart.connect(context,relType,true,bomObject);
									}
									bomObject.close(context);
									//Set Attributes on the Relationship Object Obtained.
									relationObject.setAttributes(context,attList);
									relationObject.update(context);

								}  catch(MatrixException me) {
									throw me;
								}
							}// End of if(sAttChgType.equals("A"))


							if("D".equals(sAttChgType))
							{
								for(int i=0;i<count;i++)
								{
								  String XMLKey ="";// sFindNo;
								  String databaseKey ="";// relArray[i][1];
									 // Create the comparision key based on user input.
									for(int j=0; j<selectKeys.size();j++)
									{
									  String hashMapKeyValue="";
									  String strAttributeKeyName =selectKeys.elementAt(j).toString();
									  hashMapKeyValue=(String)storedAttributeValues.get(strAttributeKeyName);
									  if (hashMapKeyValue !=null && !hashMapKeyValue.equals("null") && !hashMapKeyValue.equals(""))
									  {
										hashMapKeyValue=hashMapKeyValue.trim();
										XMLKey=XMLKey+hashMapKeyValue;
									  }
									  String hashMapKeyPosition=(String)dbSequence.get(strAttributeKeyName);
									  hashMapKeyPosition=hashMapKeyPosition.trim();
									  int pos=Integer.parseInt(hashMapKeyPosition);
									  databaseKey=databaseKey+relArray[i][pos];
									}

									if(XMLKey.equals(databaseKey))
									{
									  indexD=i;
									  break;
									}
								  }
								if(indexD!=-1)
								{

									String sRelIdforDC = relArray[indexD][attrNameList.size()];
									//EBOM Substitute, make the relationship to point to the Substitute Relationship
									if(relationshipType.equals(sEBOMSubRelationship)) {
										sRelIdforDC = strSubstituteRelId;
										//If the massupdateAction is Replace, save the EBOM rel id
										if(strIsReplaceAction != null && strIsReplaceAction.equalsIgnoreCase("Replace")) {
											strSaveEBOMRelId = new RelToRelUtil().getFromToRelId(context,sRelIdforDC,true);
										}
									}
									try
									{
										RelToRelUtil.disconnect(context,sRelIdforDC);
									}
									catch(MatrixException me)
									{
//										errMessage=me.toString();
										throw me;
									}
								} // End of if(indexD!=-1)
							} // End of if(sAttChgType.equals("D"))
						} //End of while(itr.hasNext())
					} // End of if(!fnExists && isMarkupUnique)

					if(fnExists)
					{
						MqlUtil.mqlCommand(context,"notice $1","Duplicate Find Numbers found in "+nonUniquePart);
						return 1;
					} // End of if(fnExists)

					if(!isMarkupUnique && (!fnExists))
					{
						MqlUtil.mqlCommand(context,"notice $1","Duplicate Reference Designators found in "+nonUniquePart);
						return 1;
					}// End of if(!isMarkupUnique && (!fnExists))

				} //End of if(obsoletePart)
			} else {
				MqlUtil.mqlCommand(context,"notice $1"," Part "+partNames+" does not exist");
				return 1;
			}//End of if(isPartExists)

			//Promote the Markup object to Applied state
			doMarkup.promote(context);

		}
		return 0;
	}

	/* This method "displayUserSettingsDefaultVault" displays default vault of the User.
	 * @param context The ematrix context of the request.
	 * @param args This string array contains following arguments:
	 *          0 - The programMap
     * @param args an array of String arguments for this method
     * @return String Vault details in HTML format.
	 * @throws Exception
	 * @throws FrameworkException
	 * @since EngineeringCentral X3
	 */
public String displayUserSettingsDefaultVault(Context context, String[] args) throws Exception
    {

		String defaultVault = PropertyUtil.getAdminProperty(context, personAdminType,  context.getUser(),  PREFERENCE_ENC_DEFAULT_VAULT);
		// IR-093074
		String defaultVaultActual = defaultVault;

		if(defaultVault == null || "".equals(defaultVault))
		{
            // IR-013341
            defaultVault = PersonUtil.getDefaultVault(context);
            defaultVaultActual = defaultVault;
		}
		// fix for 091260 starts
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		HashMap paramMap = (HashMap)programMap.get("paramMap");
		String languageStr = (String) paramMap.get("languageStr");
		if(null!=defaultVault && null!=languageStr)
			defaultVault = i18nNow.getAdminI18NString("Vault", defaultVault, languageStr);
		// fix for 091260 ends
		String sECO           = DomainConstants.TYPE_ECO;
		StringBuffer sbReturnString	= new StringBuffer(1024);

		sbReturnString.append("<input type='text' name='VaultDisplay' value=\""+ defaultVault+"\" readOnly='true'> </input>");
		sbReturnString.append("<input type='button' class='button' size='200' value='...' alt='...' onClick='javascript: showVaultSelector();' > </input>");
		sbReturnString.append("<input type ='hidden' name='Vault' value='"+defaultVaultActual+"' > </input>");
		// IR-093074

		sbReturnString.append(" <script src='../common/scripts/emxUIModal.js'> </script> ");
		sbReturnString.append(" <script src='../emxUIPageUtility.js'> </script> ");

		sbReturnString.append(" <script> ");
		sbReturnString.append("function showVaultSelector() { ");
		sbReturnString.append("emxShowModalDialog(\"../components/emxComponentsSelectSearchVaultsDialogFS.jsp?multiSelect=false&amp;fieldName=Vault&amp;objectType="+sECO+"&amp;suiteKey=Components&amp;\",300,350); ");

		sbReturnString.append(" } ");
		sbReturnString.append("</script>");

		return sbReturnString.toString();
    }
/**
	 * Displays the Range Values on Edit for Attribute Requested Change for Static Approval policy.
	 * @param	context the eMatrix <code>Context</code> object
	 * @param	args holds a HashMap containing the following entries:
	 *          paramMap hold a HashMap containing the following keys, "objectId"
     * @return HashMap contains actual and display values
	 * @throws	Exception if operation fails
	 * @since   EngineeringCentral X3
	 */
	public HashMap displayRequestedChangeRangeValues(Context context,String[] args) throws Exception
	{
		String strLanguage  =  context.getSession().getLanguage();

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramMap=(HashMap)programMap.get("paramMap");
		String ChangeObjectId =(String) paramMap.get("objectId");
		//Added
		DomainObject dom=new DomainObject(ChangeObjectId);

        //get all range values
        StringList strListRequestedChange = FrameworkUtil.getRanges(context , ATTRIBUTE_REQUESTED_CHANGE);

        HashMap rangeMap = new HashMap ();
        StringList listChoices = new StringList();
        StringList listDispChoices = new StringList();
        String attrValue = "";
        String dispValue = "";
        for (int i=0; i < strListRequestedChange.size(); i++)
        {
            attrValue = (String)strListRequestedChange.get(i);
            //None and For Release are invalid options for ECO Standard policy
            if (attrValue.equals(RANGE_NONE))
            {
                continue;
            }
            if (attrValue.equals(RANGE_FOR_UPDATE) && dom.isKindOf(context,DomainConstants.TYPE_ECO))
            {
                continue;
            }
            dispValue = i18nNow.getRangeI18NString(ATTRIBUTE_REQUESTED_CHANGE, attrValue, strLanguage);
            listDispChoices.add(dispValue);
            listChoices.add(attrValue);
        }

        rangeMap.put("field_choices", listChoices);
        rangeMap.put("field_display_choices", listDispChoices);
		return rangeMap;
    }

	 /**
	 * This method will connect logged person as assignee with the change object
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds the following input arguments:
     *            0 - HashMap containing one String entry for key "objectId"
     * @throws        Exception if the operation fails
     * @since   EngineeringCentral X3
     **/
	public void createAssignee(Context context, String[] args) throws Exception, FrameworkException
	{
			String strChangeObjectId = args[0];
			try
			{
				DomainObject changeObj = new DomainObject(strChangeObjectId);
				String policy = changeObj.getInfo(context,DomainObject.SELECT_POLICY);
				if(policy.equals(POLICY_ECO) || policy.equals(POLICY_ECR))
				{
					emxUtil_mxJPO utilityClass = new emxUtil_mxJPO(context, null);
					String arguments[] = new String[2];
					arguments[0] = "get env USER";
					arguments[1] = "get env APPREALUSER";
					ArrayList cmdResults = utilityClass.executeMQLCommands(context, arguments);
					String sUser = (String)cmdResults.get(0);
					String sAppRealUser = (String)cmdResults.get(1);
					if (sAppRealUser.length() != 0)
					{
						sUser = sAppRealUser;
					}

							DomainRelationship.connect(context,
													   new DomainObject(PersonUtil.getPersonObjectID(context,sUser)),
													   DomainConstants.RELATIONSHIP_ASSIGNED_EC,
													   changeObj);
				}
			}
			catch (Exception ex)
			{
				throw ex;
			}
	}

	/* This method "getNewRevisions" get the New Revs of the Affected Item.
	 * @param context The ematrix context of the request.
	 * @programMap args This string array contains following arguments:
	 *          0 - The programMap
	 *
	 * @throws Exception
	 * @throws FrameworkException
	 * @since EngineeringCentral X3
	 */
	public Vector getNewRevisions(Context context, String[] args) throws Exception {
		Vector vAffectedItemsNewRevs = new Vector();

		HashMap programMap = (HashMap) JPO.unpackArgs(args);
		HashMap paramList  = (HashMap) programMap.get("paramList");
		HashMap mapIndirectAffectedItems = new HashMap();
		Map mAffectedItem;

		MapList objectList = (MapList) programMap.get("objectList");

        String strSuiteDir 		 = (String) paramList.get("SuiteDirectory");
        String strJsTreeID 		 = (String) paramList.get("jsTreeID");
        String strParentObjectId = (String) paramList.get("objectId");

		String type;
		String name;
		String mapKey;
		String strDest;
		String strPartRev;
		String strNewPartId;
		String strIndirectData;
		String strRequestedChange;

		boolean doDBHit = true;

		Iterator itrML = objectList.iterator();

		while (itrML.hasNext()) {
			mAffectedItem = (Map) itrML.next();

			strRequestedChange = (String) mAffectedItem.get(SELECT_ATTRIBUTE_REQUESTED_CHANGE);
            strDest = "";

			if (RANGE_FOR_REVISE.equals(strRequestedChange)) {

				if (doDBHit) {
					mapIndirectAffectedItems = getIndirectAffectedItemsInMap(context, strParentObjectId, true);
					doDBHit = false;
				}

				type = (String) mAffectedItem.get(DomainConstants.SELECT_TYPE);
				name = (String) mAffectedItem.get(DomainConstants.SELECT_NAME);

				mapKey = type + SYMB_PIPE + name;

				strIndirectData = (String) mapIndirectAffectedItems.get(mapKey);

				if (strIndirectData != null) {
					strNewPartId = (String) FrameworkUtil.split(strIndirectData, SYMB_PIPE).get(0);
					strPartRev   = (String) FrameworkUtil.split(strIndirectData, SYMB_PIPE).get(1);

					strDest = new StringBuffer(100).append("<A HREF=\"JavaScript:emxTableColumnLinkClick('emxTree.jsp?mode=insert&amp;emxSuiteDirectory=").
									  append(XSSUtil.encodeForURL(context, strSuiteDir)).append("&amp;parentOID=").append(XSSUtil.encodeForURL(context,strParentObjectId)).append("&amp;jsTreeID=").
									  append(XSSUtil.encodeForURL(context,strJsTreeID)).append("&amp;objectId=").append(XSSUtil.encodeForURL(context,strNewPartId)).
									  append("', 'null', 'null', 'false', 'content')\" class=\"object\">").
									  append(XSSUtil.encodeForHTML(context, strPartRev)).append("</A>").toString();
				}
			}

			vAffectedItemsNewRevs.add(strDest);
		}

		return vAffectedItemsNewRevs;
    }

	/** This method returns all the Affected Item objects connected to ECO as Indirect and Requested Change as 'For Release'
	 * Data format of the retured map will be
	 * if addRevInValue = true then, Key = type|name, value = objectId|rev
	 * if addRevInValue = false then, Key = type|name, value = objectId
	 * @param context ematrix context
	 * @param changeId ECOID
	 * @param addRevInValue boolean value, If it is true then revision also will be added to value of map else only objectId will be value of map.
	 * @return HashMap
	 * @throws Exception if operation fails.
	 */
	public HashMap getIndirectAffectedItemsInMap(Context context, String changeId, boolean addRevInValue) throws Exception {
		StringList objectSelect =  new StringList(4);
		objectSelect.add(SELECT_TYPE);
		objectSelect.add(SELECT_NAME);
		objectSelect.add(SELECT_REVISION);
		objectSelect.add(SELECT_ID);

		DomainObject doECO = DomainObject.newInstance(context, changeId);

		Map mapPart;
		HashMap hMapReturn = new HashMap();

		String type;
		String name;
		String revision;
		String id;
		String key;
		String value;
		String strRelWhereclause = "attribute[" + PropertyUtil.getSchemaProperty(context, "attribute_AffectedItemCategory") +
										"] == 'Indirect' && attribute[" + ATTRIBUTE_REQUESTED_CHANGE + "] == '" + RANGE_FOR_RELEASE + "'";

		MapList mapListParts = doECO.getRelatedObjects(context,
	                RELATIONSHIP_AFFECTED_ITEM, DomainConstants.QUERY_WILDCARD, objectSelect,
	                null, false, true, (short) 1, null, strRelWhereclause, null, null, null);

		Iterator iterator = mapListParts.iterator();

		while (iterator.hasNext()) {
			mapPart = (Map) iterator.next();

			id = (String) mapPart.get(SELECT_ID);
			type = (String) mapPart.get(SELECT_TYPE);
			name = (String) mapPart.get(SELECT_NAME);
			revision = (String) mapPart.get(SELECT_REVISION);

			key = type + SYMB_PIPE + name;
			value = id;

			if (addRevInValue) {
				value += SYMB_PIPE + revision;
			}

			hMapReturn.put(key, value);
		}

		return hMapReturn;
	}

	/**
     * this method checks the Related object state
     * Returns Boolean determines whether the connected
     * objects are in approperiate state.
     *
     * @param context
     *            the eMatrix <code>Context</code> object.
     * @param args
     *            holds objectId.
     * @param args
     *            holds relationship name.
     * @param args
     *            holds type name.
     * @param args
     *            holds policy name.
     * @param args
     *            holds State.
     * @param args
     *            holds TO/FROM.
     * @param args
     *            holds String Resource file name
     * @param args
     *            holds String resource filed key name.
     * @return Boolean determines whether the connected objects are in
     *         approperiate state.
     * @throws Exception if the operation fails.
     * @since Engineering Central X3.
     */
   public int checkRelatedObjectState(Context context,String args[]) throws Exception {

        if (args == null || args.length < 1) {
              throw (new IllegalArgumentException());
        }
        String objectId = args[0];
        setId(objectId);
        String strRelationshipName = PropertyUtil.getSchemaProperty(context,args[1]);
        String strTypeName = PropertyUtil.getSchemaProperty(context,args[2]);
        String strPolicyName = PropertyUtil.getSchemaProperty(context,args[3]);
        String strStates = args[4];
        boolean boolTo = args[5].equalsIgnoreCase("TO")?true:false;
        boolean boolFrom = args[5].equalsIgnoreCase("FROM")?true:false;
        String strResourceFieldId = args[6];
        String strStringId = args[7];
        String strMessage = EnoviaResourceBundle.getProperty(context, strResourceFieldId, context.getLocale(),strStringId);
		String strCurrentState = args[8];
		String strPolicy = args[9];
        StringTokenizer stz = null;
        //365869
		String strSymbolicCurrentPolicy = FrameworkUtil.getAliasForAdmin(context, "policy", strPolicy, true);
		String strSymbolicCurrentState = FrameworkUtil.reverseLookupStateName(context,strPolicy,strCurrentState);

		String RouteBasePolicy = PropertyUtil.getSchemaProperty(context,"attribute_RouteBasePolicy");
		String RouteBaseState = PropertyUtil.getSchemaProperty(context,"attribute_RouteBaseState");

		int ichkvalue = 0;
        if (strStates.indexOf(' ')>-1){
                stz = new StringTokenizer(strStates," ");
            }
        else if (strStates.indexOf(',')>-1){
                stz = new StringTokenizer(strStates,",");
            }
        else if(strStates.indexOf('~')>-1){
                stz = new StringTokenizer(strStates,"~");
            }
        else{
                stz = new StringTokenizer(strStates,"");
            }

            Vector vector = new Vector();
        while (stz.hasMoreElements()){
                String state = stz.nextToken();
                vector.addElement(PropertyUtil.getSchemaProperty(context, "policy", strPolicyName , state));
            }

		String strRelnWhereClause = null;
		strRelnWhereClause = "attribute["+RouteBasePolicy+"] == "+strSymbolicCurrentPolicy+" && attribute["+RouteBaseState+"] == "+strSymbolicCurrentState+"";
		    StringList busSelects = new StringList(2);
            busSelects.add(DomainConstants.SELECT_ID);
            busSelects.add(DomainConstants.SELECT_CURRENT);
            StringList relSelects = new StringList(2);
            relSelects.add(DomainConstants.SELECT_ID);

			MapList maplistObjects = new MapList();

			try
			{
				ContextUtil.pushContext(context);
				maplistObjects = getRelatedObjects(context,
                                          strRelationshipName,
                                          strTypeName,
                                          busSelects, // object Select
                                          relSelects, // rel Select
                                          boolFrom, // to
                                          boolTo, // from
                                          (short)1,
                                          null, // ob where
                                          strRelnWhereClause  // rel where
                                          );
			}
			catch (Exception ex)
			{
			}
			finally
			{
				ContextUtil.popContext(context);
			}

       if (maplistObjects != null && (maplistObjects.size() > 0)){
                Iterator itr = maplistObjects.iterator();
       while (itr.hasNext() && ichkvalue != 1){
                    Map mapObject = (Map) itr.next();
                    ichkvalue = vector.contains(mapObject.get("current"))?0:1;
                }

            }
       if(ichkvalue == 1){
		        emxContextUtil_mxJPO.mqlNotice(context,strMessage);
            }

        return ichkvalue;
    }

	//for bug starts
		/**
   * Constructs the ECO related ResolvedItems HTML table.
   *
   * @param context the eMatrix <code>Context</code> object.
   * @param args holds the following input arguments:
   * 0 - String containing object id.
   * @return String Html table format representation of Related ECOs data.
   * @throws Exception if the operation fails.
   * @since Engineering Central X3
  */
 public String getECORelatedResolvedItems(Context context,String[] args)
       throws Exception
 {
     String strLanguage = context.getSession().getLanguage();
     String objectId = args[0];
     MapList mpListResolvedItems = new MapList();
     StringBuffer relatedResolvedItems = new StringBuffer(1024);

     try
     {
         setId(objectId);
         StringList selectStmts = new StringList();
         selectStmts.addElement(SELECT_ID);
     selectStmts.addElement(SELECT_NAME);
     selectStmts.addElement(SELECT_REVISION);
     selectStmts.addElement(SELECT_TYPE);
     selectStmts.addElement(SELECT_DESCRIPTION);
     selectStmts.addElement(SELECT_CURRENT);
     selectStmts.addElement(SELECT_OWNER);

     String strRelResolvedTo = DomainConstants.RELATIONSHIP_RESOLVED_TO;

     mpListResolvedItems = getRelatedObjects(context,
                                            strRelResolvedTo,
                                            "*",
                                            selectStmts,
                                            null,
                                            true,
                                            false,
                                            (short) 1,
                                            "",
                                            "");

         mpListResolvedItems.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
         mpListResolvedItems.sort();
     Iterator objItr = mpListResolvedItems.iterator();
         Map mpResolvedItems  = null;
         relatedResolvedItems.append("<table width=\"100%\" border=\"0\" cellpadding=\"3\" cellspacing=\"0\" >");
         relatedResolvedItems.append("<tr>");
         relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Name",strLanguage)+"</th>");
         relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Revision",strLanguage)+"</th>");
         relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Type",strLanguage)+"</th>");
         relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Description",strLanguage)+"</th>");
         relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.State",strLanguage)+"</th>");
         relatedResolvedItems.append("<th>"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.Common.Owner",strLanguage)+"</th>");
         relatedResolvedItems.append("</tr>");

     while (objItr.hasNext()) {
         mpResolvedItems = (Map)objItr.next();
         relatedResolvedItems.append("<tr>");
         relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_NAME)+"&nbsp;</td>");
         relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_REVISION)+"&nbsp;</td>");
         relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_TYPE)+"&nbsp;</td>");
         relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_DESCRIPTION)+"&nbsp;</td>");
         relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_CURRENT)+"&nbsp;</td>");
         relatedResolvedItems.append("<td>"+mpResolvedItems.get(SELECT_OWNER)+"&nbsp;</td>");
         relatedResolvedItems.append("</tr>");
     }
     if(mpListResolvedItems.size()==0) {
         relatedResolvedItems.append("<tr><td colspan=\"5\">"+EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.NoResolvedItemsFound",strLanguage)+"</td></tr>");
     }
     }
     catch (FrameworkException Ex)
     {
          throw Ex;
     }
     relatedResolvedItems.append("</table>");
     return relatedResolvedItems.toString();
 }
	//for bug ends

  /**
	 * Updates the Range Values for Attribute Disposition Field Return Based on User Selection
	 * @param	context the eMatrix <code>Context</code> object
	 * @param	args holds a HashMap containing the following entries:
	 * paramMap - a HashMap containing the following keys, "relId","DispositionFieldReturn"
	 * @return	int
	 * @throws	Exception if operation fails
	 * @since   Common X3
	 */
  public int updateDispositionFieldReturn(Context context, String[] args) throws Exception
	{
		int intReturn=0;

	    String strAlertMessageNotAPart = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_EC_STR, context.getLocale(),"emxEngineeringCentral.Common.Alert.SelectedAffectedItemNotAPart");
	    HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get(SELECT_PARAM_MAP);
		String objId = (String)paramMap.get("objectId");
		DomainObject domObj = new DomainObject(objId);
        String sAttDisposition = PropertyUtil.getSchemaProperty(context, "attribute_DispositionFieldReturn");
        String sRelId = (String)paramMap.get(SELECT_REL_ID);
        String strNewDispositionValue  = (String)paramMap.get(SELECT_NEW_VALUE);
        DomainRelationship domRelObj = new DomainRelationship(sRelId);

        // Fix for 358154, 363041 - start
		//modified for IR-027876V6R2011
		try
        {
        ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING); // IR-074925V6R2012
		if(domObj.isKindOf(context,DomainConstants.TYPE_PART))
		{

				domRelObj.setAttributeValue(context, sAttDisposition, strNewDispositionValue);
				intReturn = 0;
			}
			// Fix for 358154, 363041 - end
			else
			{
				emxContextUtil_mxJPO.mqlNotice(context,strAlertMessageNotAPart);
				//Edit of Disposition Codes are applicable only for type Part.
				intReturn =1;
            }
        } catch (FrameworkException e) {
            e.printStackTrace();
        }finally{
            ContextUtil.popContext(context);
		}
	   return intReturn;
  }

  /**
	 * Updates the Range Values for Attribute Disposition In Field Based on User Selection
	 * @param	context the eMatrix <code>Context</code> object
	 * @param	args holds a HashMap containing the following entries:
	 * paramMap - a HashMap containing the following keys, "relId","DispositionInField"
	 * @return	int
	 * @throws	Exception if operation fails
	 * @since   Common X3
	 */
  public int updateDispositionInField(Context context, String[] args) throws Exception
	{
		int intReturn=0;

	    String strAlertMessageNotAPart = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_EC_STR, context.getLocale(),"emxEngineeringCentral.Common.Alert.SelectedAffectedItemNotAPart");
	    HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get(SELECT_PARAM_MAP);
		String objId = (String)paramMap.get("objectId");
		DomainObject domObj = new DomainObject(objId);
        String sAttDisposition = PropertyUtil.getSchemaProperty(context, "attribute_DispositionInField");
        String sRelId = (String)paramMap.get(SELECT_REL_ID);
        String strNewDispositionValue  = (String)paramMap.get(SELECT_NEW_VALUE);
        DomainRelationship domRelObj = new DomainRelationship(sRelId);

        // Fix for 358154, 363041 - start
		//modified for IR-027876V6R2011
        try
        {
        	ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING); // IR-074925V6R2012
		if(domObj.isKindOf(context,DomainConstants.TYPE_PART))
		{

            domRelObj.setAttributeValue(context, sAttDisposition, strNewDispositionValue);
            intReturn = 0;
        }
        // Fix for 358154, 363041 - end
		else
		{
				emxContextUtil_mxJPO.mqlNotice(context,strAlertMessageNotAPart);
				//Edit of Disposition Codes are applicable only for type Part.
				intReturn =1;
    		}
        }
        catch(FrameworkException e)
        {
            e.printStackTrace();
        }finally{
            ContextUtil.popContext(context);
        }
	   return intReturn;
  }

  /**
	 * Updates the Range Values for Attribute Disposition In Process Based on User Selection
	 * @param	context the eMatrix <code>Context</code> object
	 * @param	args holds a HashMap containing the following entries:
	 * paramMap - a HashMap containing the following keys, "relId","DispositionInProcess"
	 * @return	int
	 * @throws	Exception if operation fails
	 * @since   Common X3
	 */
  public int updateDispositionInProcess(Context context, String[] args) throws Exception
	{
		int intReturn=0;

	    String strAlertMessageNotAPart = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_EC_STR, context.getLocale(),"emxEngineeringCentral.Common.Alert.SelectedAffectedItemNotAPart");
	    HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get(SELECT_PARAM_MAP);
		String objId = (String)paramMap.get("objectId");
		DomainObject domObj = new DomainObject(objId);
        String sAttDisposition = PropertyUtil.getSchemaProperty(context, "attribute_DispositionInProcess");
        String sRelId = (String)paramMap.get(SELECT_REL_ID);
        String strNewDispositionValue  = (String)paramMap.get(SELECT_NEW_VALUE);
        DomainRelationship domRelObj = new DomainRelationship(sRelId);

        // Fix for 358154, 363041 - start
		//modified for IR-027876V6R2011
		try {
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING); // IR-074925V6R2012
		if(domObj.isKindOf(context,DomainConstants.TYPE_PART))
		{


            domRelObj.setAttributeValue(context, sAttDisposition, strNewDispositionValue);
            intReturn = 0;
        }
        // Fix for 358154, 363041 - end
		else
		{
				emxContextUtil_mxJPO.mqlNotice(context,strAlertMessageNotAPart);
				//Edit of Disposition Codes are applicable only for type Part.
				intReturn =1;
            }
        } catch (FrameworkException e) {
            e.printStackTrace();
        } finally{
            ContextUtil.popContext(context);
		}
	   return intReturn;
  }


    /**
	 * Updates the Range Values for Attribute Disposition In Stock Based on User Selection
	 * @param	context the eMatrix <code>Context</code> object
	 * @param	args holds a HashMap containing the following entries:
	 * paramMap - a HashMap containing the following keys, "relId","DispositionInStock"
	 * @return	int
	 * @throws	Exception if operation fails
	 * @since   Common X3
	 */
  public int updateDispositionInStock(Context context, String[] args) throws Exception
	{
		int intReturn=0;

	    String strAlertMessageNotAPart = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_EC_STR, context.getLocale(),"emxEngineeringCentral.Common.Alert.SelectedAffectedItemNotAPart");
	    HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get(SELECT_PARAM_MAP);
		String objId = (String)paramMap.get("objectId");
		DomainObject domObj = new DomainObject(objId);
        String sAttDisposition = PropertyUtil.getSchemaProperty(context, "attribute_DispositionInStock");
        String sRelId = (String)paramMap.get(SELECT_REL_ID);
        String strNewDispositionValue  = (String)paramMap.get(SELECT_NEW_VALUE);
        DomainRelationship domRelObj = new DomainRelationship(sRelId);

        // Fix for 358154, 363041 - start
		//modified for IR-027876V6R2011
		try {
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING); // IR-074925V6R2012
		if(domObj.isKindOf(context,DomainConstants.TYPE_PART))
		{


            domRelObj.setAttributeValue(context, sAttDisposition, strNewDispositionValue);
            intReturn = 0;
        }
        // Fix for 358154, 363041 - end
		else
		{
				emxContextUtil_mxJPO.mqlNotice(context,strAlertMessageNotAPart);
				//Edit of Disposition Codes are applicable only for type Part.
				intReturn =1;
            }
        } catch (FrameworkException e) {
            e.printStackTrace();
        }
        finally{
            ContextUtil.popContext(context);
        }
	   return intReturn;
  }

    /**
	 * Updates the Range Values for Attribute Disposition On Order Based on User Selection
	 * @param	context the eMatrix <code>Context</code> object
	 * @param	args holds a HashMap containing the following entries:
	 * paramMap - a HashMap containing the following keys, "relId","DispositionOnOrder"
	 * @return	int
	 * @throws	Exception if operation fails
	 * @since   Common X3
	 */
  public int updateDispositionOnOrder(Context context, String[] args) throws Exception
	{
		int intReturn=0;

	    String strAlertMessageNotAPart = EnoviaResourceBundle.getProperty(context, RESOURCE_BUNDLE_EC_STR, context.getLocale(),"emxEngineeringCentral.Common.Alert.SelectedAffectedItemNotAPart");
	    HashMap programMap = (HashMap)JPO.unpackArgs(args);
        HashMap paramMap = (HashMap)programMap.get(SELECT_PARAM_MAP);
		String objId = (String)paramMap.get("objectId");
		DomainObject domObj = new DomainObject(objId);
        String sAttDisposition = PropertyUtil.getSchemaProperty(context, "attribute_DispositionOnOrder");
        String sRelId = (String)paramMap.get(SELECT_REL_ID);
        String strNewDispositionValue  = (String)paramMap.get(SELECT_NEW_VALUE);
        DomainRelationship domRelObj = new DomainRelationship(sRelId);

        // Fix for 358154, 363041 - start
		//modified for IR-027876V6R2011
		try {
			ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING); // IR-074925V6R2012
		if(domObj.isKindOf(context,DomainConstants.TYPE_PART))
		{

            domRelObj.setAttributeValue(context, sAttDisposition, strNewDispositionValue);
            intReturn = 0;
        }
        // Fix for 358154, 363041 - end
		else
		{
				emxContextUtil_mxJPO.mqlNotice(context,strAlertMessageNotAPart);
				//Edit of Disposition Codes are applicable only for type Part.
				intReturn =1;
            }
        } catch (FrameworkException e) {
            e.printStackTrace();
        }
        finally{
            ContextUtil.popContext(context);
        }
	   return intReturn;
  }

       /**
     * Displays the "Category of Change" drop down based on the ECR or the stored value.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds a HashMap containing the following entries:
     * paramMap - a HashMap containing the following Strings, "objectId".
     * requestMap - a HashMap containing the request.
     * @return Object - String object which contains the Category of Change drop down.
     * @throws Exception if operation fails.
     * @since EngineeringCentral X3
     */

    public Object getCategoryofChange(Context context, String[] args)
    throws Exception
    {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");
        String languageStr = (String) requestMap.get("languageStr");
        String strObjectId = (String) requestMap.get("OBJId");

        StringBuffer sbCategoryOfChange = new StringBuffer(128);
		sbCategoryOfChange.append("<select name=\"CategoryOfChange\">");
        String sCurrentCategoryOfChangeName = "";
        if (strObjectId!=null && strObjectId.length() > 0)
        {
            setId(strObjectId);
            sCurrentCategoryOfChangeName = getAttributeValue(context,ATTRIBUTE_CATEGORY_OF_CHANGE);
        }

		//Get the range values for ECO "Category Of Change" Attribute
		StringList slCategoryOfChangeRangeValues = FrameworkUtil.getRanges(context , ATTRIBUTE_CATEGORY_OF_CHANGE);

		String strCategoryOfChangeOption = null;

		//Construct the Category Of Change dropdown
		for (int i=0; i < slCategoryOfChangeRangeValues.size(); i++)
		{
			strCategoryOfChangeOption = (String)slCategoryOfChangeRangeValues.elementAt(i);
			String sPolicySelected ="selected=\"selected\"";
            sbCategoryOfChange.append("<option value=\""+strCategoryOfChangeOption+"\" "+((strCategoryOfChangeOption.equals(sCurrentCategoryOfChangeName))?sPolicySelected:"")+">"+i18nNow.getRangeI18NString(ATTRIBUTE_CATEGORY_OF_CHANGE,strCategoryOfChangeOption,languageStr)+"</option>");
		}
		sbCategoryOfChange.append("</select>");


        return sbCategoryOfChange.toString();
 }

  /**
   *Update the Category of Change value of ECO
   * @param context the Matrix Context
   * @param args no args needed for this method
   * @returns booloen
   * @throws Exception if the operation fails
   * @since EngineeringCentral X3
   */
  public Boolean updateCategoryofChange(Context context, String[] args) throws Exception
  {

    HashMap programMap = (HashMap)JPO.unpackArgs(args);
    HashMap paramMap = (HashMap)programMap.get("paramMap");

	String strECObjectId = (String)paramMap.get("objectId");
	DomainObject domObjECO = new DomainObject(strECObjectId);

	String strNewCategoryOfChange = (String)paramMap.get("New Value");
	if (strNewCategoryOfChange != null && strNewCategoryOfChange.length() == 0)
	{
		strNewCategoryOfChange = (String)paramMap.get("New OID");
	}
	domObjECO.setAttributeValue(context, ATTRIBUTE_CATEGORY_OF_CHANGE, strNewCategoryOfChange);

    return Boolean.TRUE;
  }

  /**
   * Get the related ECRs that are already connected.
   * @param context the Matrix Context
   * @param args no args needed for this method
   * @returns StringList containing Avalable selection for approval status
   * @throws Exception if the operation fails
   * @since EC V6R2008-2.0LA
   */
   @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
   public StringList getRelatedECRsAlreadyConnected(Context context,String[] args) throws Exception
   {
     StringList connectedOIDs = new StringList();
     HashMap programMap = (HashMap)JPO.unpackArgs(args);
     String  objectId   = (String) programMap.get("objectId");

     ContextUtil.startTransaction(context, true);
     try
     {
        ECO ecoObj = new ECO(objectId);
        StringList selectBusStmts = new StringList(1);
        selectBusStmts.addElement(SELECT_ID);
        StringList selectRelStmts = new StringList();

        MapList ecrMapList = FrameworkUtil.toMapList(ecoObj.getExpansionIterator(context, DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT, "*",
                selectBusStmts, selectRelStmts, false, true, (short)1,
                null, null, (short)0,
                false, false, (short)0, false),
                (short)0, null, null, null, null);

        Iterator objItr = ecrMapList.iterator();
        while (objItr.hasNext())
        {
           Map ecrMap = (Map)objItr.next();
           connectedOIDs.add((String)ecrMap.get(SELECT_ID));
        }
        ContextUtil.commitTransaction(context);
     }
     catch (FrameworkException Ex)
     {
    	ContextUtil.abortTransaction(context);
        throw Ex;
     }
     return connectedOIDs;
   }

	/**
	* Moves the selected affected items from context ECO to selected ECO
	*
	* @param context The Matrix Context.
	* @param strTargetECOId The selected ECO id
	* @param strArrAffectedItems The selected affected item ids
	* @throws FrameworkException If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public void moveAffectedItems(Context context,
								  String[] args) throws Exception
	{
		HashMap hmpProgramMap= (HashMap)JPO.unpackArgs(args);

		String strSourceECOId = (String) hmpProgramMap.get("sourceECOId");
		String strTargetECOId = (String) hmpProgramMap.get("targetECOId");
		String [] affectedItemsList = (String []) hmpProgramMap.get("affectedItems");

		StringList strlObjSelects = new StringList(1);
		strlObjSelects.add(SELECT_ID);

		StringList strlRelnSelects = new StringList(1);
		strlRelnSelects.add(SELECT_RELATIONSHIP_ID);

		StringList strlPartSelects = new StringList(4);
		strlPartSelects.add(SELECT_TYPE);
		strlPartSelects.add(SELECT_NAME);
		strlPartSelects.add(SELECT_REVISION);
		strlPartSelects.add(SELECT_RELATIONSHIP_DESIGN_RESPONSIBILITY);
		strlPartSelects.add("altowner1"); //Added for RDO Convergence

		StringList strlTargetAffectedItems = new StringList();

		BusinessObjectList objectList = new BusinessObjectList();

		String strRelPattern = RELATIONSHIP_AFFECTED_ITEM;
		String strTargetRelPattern = RELATIONSHIP_AFFECTED_ITEM;



		DomainObject doSourceECO = new DomainObject(strSourceECOId);
		objectList.add(doSourceECO);

		DomainObject doTargetECO = new DomainObject(strTargetECOId);
		objectList.add(doTargetECO);
		String strTargetECORDO = doTargetECO.getAltOwner1(context).toString(); //Added for RDO Convergence

		MapList mapListTargetAffectedItems = doTargetECO.getRelatedObjects(context, strTargetRelPattern, "*", strlObjSelects, strlRelnSelects, false, true, (short) 1, null, null);

		Iterator itrTargetAffectedItems = mapListTargetAffectedItems.iterator();

		Map mapTargetAffectedItem = null;
		String strTargetAffectedItemId = null;

		while (itrTargetAffectedItems.hasNext())
		{
			mapTargetAffectedItem = (Map) itrTargetAffectedItems.next();
			strTargetAffectedItemId = (String) mapTargetAffectedItem.get(DomainConstants.SELECT_ID);
			strlTargetAffectedItems.add(strTargetAffectedItemId);
			objectList.add(new DomainObject(strTargetAffectedItemId));
		}

		int intNumAffectedItems = affectedItemsList.length;

		String strTempId = null;
        //Modified for IR-086764V6R2012 starts
		StringList slTemp;
		String [] strArrayIds= new String[2];
		String strRelId=DomainConstants.EMPTY_STRING;
		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");
		String strSourceRelAffItemCategory=DomainConstants.EMPTY_STRING;
        StringList sltempAffectedItems= new StringList();
        String strNewPartid=DomainConstants.EMPTY_STRING;
        enoEngChange_mxJPO Emxchange= new enoEngChange_mxJPO (context,args);
		for(int i=0; i < intNumAffectedItems; i++)
		{
			slTemp = FrameworkUtil.split(affectedItemsList[i], "|");
			strTempId = (String) slTemp.firstElement();
			strArrayIds[0]=strTempId;
			strArrayIds[1]=strSourceECOId;
			strRelId = (String) slTemp.lastElement();
		    strSourceRelAffItemCategory = DomainRelationship.getAttributeValue( context, strRelId, strAttrAffectedItemCategory);
			sltempAffectedItems.add(strTempId);
				if(null!=strSourceRelAffItemCategory&&(!DomainConstants.EMPTY_STRING.equals(strSourceRelAffItemCategory)))
			{
			if("Indirect".equals(strSourceRelAffItemCategory))
					{
			strNewPartid=Emxchange.getDirectAffectedItems(context, strArrayIds);
					}
			else	if("Direct".equals(strSourceRelAffItemCategory))
			{
            strNewPartid=Emxchange.getIndirectAffectedItems(context, strArrayIds);
			}
			if(null!=strNewPartid&&(!DomainConstants.EMPTY_STRING.equals(strNewPartid)))
			{
				if(!sltempAffectedItems.contains(strNewPartid))
				{
				sltempAffectedItems.add(strNewPartid);
				objectList.add(new DomainObject(strNewPartid));
				}
			}
			}

			objectList.add(new DomainObject(strTempId));
		}
		affectedItemsList = (String[]) sltempAffectedItems.toArray(new String[sltempAffectedItems.size()]);
		intNumAffectedItems=affectedItemsList.length;
		//Modified For IR-086764V6R2012 ends

		Access accessMask = new Access();
		accessMask.setAllAccess(true);
		accessMask.setUser(context.getUser());
		ContextUtil.pushContext(context,DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,null,null);
		BusinessObject.grantAccessRights(context,
									 	objectList,
									 	accessMask);
		ContextUtil.popContext(context);

		String strSourceAffectedItemId = null;
		String strPartRDO = null;
		StringTokenizer strtokObjectIds = null;

		DomainObject doSourceAffectedItem = null;

		MapList mapListECRsECOs = new MapList();
		Map mapECRECO = null;
		Map mapPartDetails = null;

		String strSourceRelId = null;
		String strWhereClause = DomainConstants.SELECT_ID + " == " + strSourceECOId;

		boolean blnShowRDOMsg = false;
		boolean blnShowReviseMsg = false;
		boolean blnShowConnectFailMsg = false;
		boolean blnAlreadyConnected = false;

		String strRDOMismatchMsg = "";
		String strRevisionFailMsg = "";
		String strConnectFailMsg = "";
		String strAlreadyConnected = "";

				for(int i=0; i < intNumAffectedItems; i++)
				{
					try
					{
						strtokObjectIds = new StringTokenizer(affectedItemsList[i], "|");
						strSourceAffectedItemId = strtokObjectIds.nextToken().trim();

						doSourceAffectedItem = new DomainObject(strSourceAffectedItemId);

						mapPartDetails = doSourceAffectedItem.getInfo(context, strlPartSelects);

						if (strlTargetAffectedItems.contains(strSourceAffectedItemId))
						{
							if (strAlreadyConnected.length() > 0)
							{
								strAlreadyConnected = strAlreadyConnected  + ", " + (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
							}
							else
							{
								strAlreadyConnected = (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
							}
							blnAlreadyConnected = true;
							continue;
						}


						if (strTargetECORDO != null && !"".equals(strTargetECORDO))
						{
							strPartRDO = (String) mapPartDetails.get("altowner1");

							if (strPartRDO != null && !"".equals(strPartRDO))
							{
								if (!strTargetECORDO.equalsIgnoreCase(strPartRDO))
								{
									if (strRDOMismatchMsg.length() > 0)
									{
										strRDOMismatchMsg = strRDOMismatchMsg + ", " + (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
									}
									else
									{
										strRDOMismatchMsg = (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
									}
									blnShowRDOMsg = true;
									continue;
								}
							}
						}
						//Modified for RDO Convergence End

						mapListECRsECOs = doSourceAffectedItem.getRelatedObjects(context, strRelPattern, "*", strlObjSelects, strlRelnSelects, true, false, (short) 1, strWhereClause, null);

						mapECRECO = (Map) mapListECRsECOs.get(0);

						strSourceRelId = (String) mapECRECO.get(DomainConstants.SELECT_RELATIONSHIP_ID);

						DomainRelationship.setFromObject(context, strSourceRelId, doTargetECO);

						moveMarkups(context, strSourceAffectedItemId, strSourceECOId, strTargetECOId);
					}
					catch (Exception ex)
					{
					}
				}


		if (blnShowRDOMsg)
		{
			// display warning to the user on RDO mismatch of affected items
			emxContextUtil_mxJPO.mqlWarning(context,
				EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.AffectedItemsRDOMismatchWarning",
				context.getSession().getLanguage()) + strRDOMismatchMsg);
		}

		if (blnShowReviseMsg)
		{
			// display warning to the user on the non-revisable affected items
			emxContextUtil_mxJPO.mqlWarning(context,
				EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECR.AutoRevECRAffectedItemsWarning",
				context.getSession().getLanguage()) + strRevisionFailMsg);
		}

		if (blnShowConnectFailMsg)
		{
			// display warning to the user on the non-revisable affected items
			emxContextUtil_mxJPO.mqlWarning(context,
				EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.ConnectAffectedItemsWarning",
				context.getSession().getLanguage()) + strConnectFailMsg);
		}

		if (blnAlreadyConnected)
		{
			// display warning to the user on the non-revisable affected items
			emxContextUtil_mxJPO.mqlWarning(context,
				EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.AlreadyConnectedAffectedItemsWarning",
				context.getSession().getLanguage()) + strAlreadyConnected);
		}
		//Added for IR-227033 start
		AccessList aclList = new AccessList(1);
		aclList.add(accessMask);
		ContextUtil.pushContext(context,DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,null,null);
		BusinessObject.revokeAccessRights(context, objectList, aclList);
		ContextUtil.popContext(context);
		//Added for IR-227033 end
	}


	/**
	* gets the latest released revision of the affected item
	*
	* @param context The Matrix Context.
	* @param strAffectedItemId The affected item id
	* @returns latest released id or null
	* @throws Exception If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public String getLatestReleased (Context context, String strAffectedItemId)
															throws Exception
	{
		DomainObject doAffectedItem = new DomainObject(strAffectedItemId);

		StringList strlSelects = new StringList(5);
		strlSelects.add(SELECT_TYPE);
		strlSelects.add(SELECT_ID);
		strlSelects.add(SELECT_NAME);
		strlSelects.add(SELECT_POLICY);
		strlSelects.add(SELECT_VAULT);

		Map mapDetails = doAffectedItem.getInfo(context, strlSelects);

		String strPolicy = (String) mapDetails.get(SELECT_POLICY);

    	String STATE_AI_RELEASE =  PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Release");


		String strWhereClause = null;

		if (doAffectedItem.isKindOf(context, TYPE_PART))
		{
			String STATE_AI_OBSOLETE =  PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Obsolete");
			strWhereClause = "(current == \"" + STATE_AI_RELEASE + "\") && (!((next.current == \"" + STATE_AI_RELEASE + "\") || (next.current == \"" + STATE_AI_OBSOLETE + "\")))";
		}
		else
		{
			strWhereClause = "(current == \"" + STATE_AI_RELEASE + "\") && (!(next.current == \"" + STATE_AI_RELEASE + "\"))";
		}

		MapList mapListParts = DomainObject.findObjects(context,
					  (String) mapDetails.get(SELECT_TYPE),
					  (String) mapDetails.get(SELECT_NAME),
					  "*",
					  null,
					  (String) mapDetails.get(SELECT_VAULT),
					  strWhereClause,
					  false,
					  strlSelects);

		if (mapListParts.size() > 0)
		{
			Map mapPart = (Map) mapListParts.get(0);
			String strId = (String) mapPart.get(SELECT_ID);

			return strId;
		}
		else
		{
			return null;
		}
	}

	/**
	* gets the latest unreleased revision of the affected item
	*
	* @param context The Matrix Context.
	* @param strAffectedItemId The affected item id
	* @returns latest unreleased id or null
	* @throws Exception If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public String getLatestUnreleased (Context context, String strAffectedItemId)
																	throws Exception
	{
		DomainObject doAffectedItem = new DomainObject(strAffectedItemId);

		BusinessObject boLastRevision = doAffectedItem.getLastRevision(context);

		DomainObject doLastRevision = new DomainObject(boLastRevision);

		String strLastRevId = doLastRevision.getObjectId(context);

		String strPolicy = doLastRevision.getInfo(context, SELECT_POLICY);

    	String STATE_AI_RELEASE =  PropertyUtil.getSchemaProperty(context,"policy", strPolicy, "state_Release");

    	if (checkObjState(context, strLastRevId, STATE_AI_RELEASE, LT) == 0)
    	{
			return strLastRevId;
		}
		else
		{
			return null;
		}

	}

	/**
	* gets the common attributes between two relationship types
	*
	* @param context The Matrix Context.
	* @param strSourceRelType name of first relationship
	* @param strTargetRelType name of second relationship
	* @returns latest unreleased id or null
	* @throws Exception If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public StringList getCommonAttributes(Context context,
										 String strSourceRelType,
										 String strTargetRelType)
										 		throws Exception
	{
		String strSourceAttributes = MqlUtil.mqlCommand(context, "print relationship $1  select $2 dump $3",strSourceRelType,"attribute","|");

		StringList strlCommonAttributes = new StringList();

		if (strSourceAttributes.length() > 0)
		{
			String strTargetAttributes = MqlUtil.mqlCommand(context, "print relationship $1  select $2 dump $3",strTargetRelType,"attribute","|");

			if (strTargetAttributes.length() > 0)
			{
				StringList strlSourceAttributes = FrameworkUtil.split(strSourceAttributes, "|");

				Iterator itrstrlSourceAttributes = strlSourceAttributes.iterator();
				String strAttributeName = null;

				while (itrstrlSourceAttributes.hasNext())
				{
					strAttributeName = (String) itrstrlSourceAttributes.next();

					if (strTargetAttributes.indexOf(strAttributeName) != -1)
					{
						strlCommonAttributes.add(strAttributeName);
					}
				}
			}
		}

		return strlCommonAttributes;
	}

	/**
	* moves Markups from one ECO to another ECO for a given part
	*
	* @param context The Matrix Context.
	* @param strPartId Part Id
	* @param strSourceECO Source ECO Id
	* @param strTargetECO Target ECO Id
	* @returns latest unreleased id or null
	* @throws Exception If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public void moveMarkups(Context context,
							String strPartId,
							String strSourceECO,
							String strTargetECO)
										 		throws Exception
	{
		StringList strlObjSelects = new StringList(1);
		strlObjSelects.add(SELECT_ID);

		StringList strlRelnSelects = new StringList(1);
		strlRelnSelects.add(DomainRelationship.SELECT_ID);

		DomainObject doSourceECO = new DomainObject(strSourceECO);

		MapList mapListMarkups = doSourceECO.getRelatedObjects(context, RELATIONSHIP_APPLIED_MARKUP, TYPE_PART_MARKUP + "," + TYPE_EBOM_MARKUP, strlObjSelects, strlRelnSelects, false, true, (short) 1, "to[" + RELATIONSHIP_EBOM_MARKUP + "].from.id == \"" + strPartId + "\"", null);

		if (mapListMarkups != null && mapListMarkups.size() > 0)
		{
			DomainObject doTargetECO = new DomainObject(strTargetECO);

			Iterator itrMarkups = mapListMarkups.iterator();

			while (itrMarkups.hasNext())
			{
				Map mapMarkup = (Map) itrMarkups.next();
				String strRelId = (String) mapMarkup.get(DomainRelationship.SELECT_ID);
				DomainRelationship.setFromObject(context, strRelId, doTargetECO);
			}
		}
	}

	/**
	* Adds the selected affected items to selected ECR
	*
	* @param context The Matrix Context.
	* @param strArrAffectedItems The selected affected item ids
	* @throws Exception If the operation fails.
	* @since EngineeringCentral X+3
	*/
	public void connectAffectedItems(Context context,
								  String[] args) throws Exception
	{
		HashMap hmpProgramMap= (HashMap)JPO.unpackArgs(args);

		String strTargetECOId = (String) hmpProgramMap.get("targetECOId");
		String [] affectedItemsList = (String []) hmpProgramMap.get("affectedItems");
		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");

		StringList strlObjSelects = new StringList(1);
		strlObjSelects.add(SELECT_ID);

		StringList strlRelnSelects = new StringList(1);
		strlRelnSelects.add(SELECT_RELATIONSHIP_ID);

		StringList strlPartSelects = new StringList(4);
		strlPartSelects.add(SELECT_TYPE);
		strlPartSelects.add(SELECT_NAME);
		strlPartSelects.add(SELECT_REVISION);
		strlPartSelects.add(SELECT_RELATIONSHIP_DESIGN_RESPONSIBILITY);
		strlPartSelects.add(SELECT_CURRENT);
		strlPartSelects.add(SELECT_POLICY);
		strlPartSelects.add("altowner1"); //Added for RDO Convergence

		StringList strlTargetAffectedItems = new StringList();

		BusinessObjectList objectList = new BusinessObjectList();

		String strTargetRelPattern = RELATIONSHIP_AFFECTED_ITEM;



 		DomainObject doTargetECO = new DomainObject(strTargetECOId);
		objectList.add(doTargetECO);

		MapList mapListTargetAffectedItems = doTargetECO.getRelatedObjects(context, strTargetRelPattern, "*", strlObjSelects, strlRelnSelects, false, true, (short) 1, null, null);

		Iterator itrTargetAffectedItems = mapListTargetAffectedItems.iterator();

		Map mapTargetAffectedItem = null;
		String strTargetAffectedItemId = null;

		while (itrTargetAffectedItems.hasNext())
		{
			mapTargetAffectedItem = (Map) itrTargetAffectedItems.next();
			strTargetAffectedItemId = (String) mapTargetAffectedItem.get(DomainConstants.SELECT_ID);
			strlTargetAffectedItems.add(strTargetAffectedItemId);
			objectList.add(new DomainObject(strTargetAffectedItemId));
		}

		String strECORDO = doTargetECO.getAltOwner1(context).toString(); //Added for RDO Convergence

		int intNumAffectedItems = affectedItemsList.length;

		StringTokenizer strTokTemp = null;
		String strTempId = null;

		for(int i=0; i < intNumAffectedItems; i++)
		{
			strTokTemp = new StringTokenizer(affectedItemsList[i], "|");
			strTempId = strTokTemp.nextToken().trim();
			objectList.add(new DomainObject(strTempId));
		}

		Access accessMask = new Access();
		accessMask.setAllAccess(true);
		accessMask.setUser(context.getUser());
		ContextUtil.pushContext(context,DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,null,null);
		BusinessObject.grantAccessRights(context,
									 	objectList,
									 	accessMask);
		ContextUtil.popContext(context);

		String strSourceAffectedItemId = null;
		String strPartRDO = null;
		StringTokenizer strtokObjectIds = null;

		Map mapPartDetails = null;

		DomainObject doSourceAffectedItem = null;

		boolean isSameECO = false;
		boolean blnShowRDOMsg = false;
		boolean blnAlreadyConnected = false;

        String strRDOMismatchMsg = "";
        String strAlreadyConnected = "";
		String strPlanningRequired = "";

		StringList slExcludeECOChangeNames = null;

            for(int i=0; i < intNumAffectedItems; i++)
            {
                strtokObjectIds = new StringTokenizer(affectedItemsList[i], "|");
                strSourceAffectedItemId = strtokObjectIds.nextToken().trim();

                doSourceAffectedItem = new DomainObject(strSourceAffectedItemId);

                mapPartDetails = doSourceAffectedItem.getInfo(context, strlPartSelects);

                if (strlTargetAffectedItems.contains(strSourceAffectedItemId))
                {
                    if (strAlreadyConnected.length() > 0)
                    {
                        strAlreadyConnected = strAlreadyConnected  + ", " + (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
                    }
                    else
                    {
                        strAlreadyConnected = (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
                    }
                    blnAlreadyConnected = true;
                    continue;
                }

              	 strPlanningRequired = doSourceAffectedItem.getInfo(context, EngineeringConstants.SELECT_PLANNING_REQUIRED);
               	 if(UIUtil.isNotNullAndNotEmpty(strPlanningRequired)){
               		 if("Yes".equals(strPlanningRequired)){


               			 slExcludeECOChangeNames = EngineeringUtil.getECOChangeList(doSourceAffectedItem.getInfo(context, EngineeringConstants.SELECT_TO_LEFTBRACE + EngineeringConstants.RELATIONSHIP_MANUFACTURING_RESPONSIBILITY + EngineeringConstants.SELECT_RIGHTBRACE + EngineeringConstants.DOT  + "attribute[Doc-In]"));

               			 if(null != slExcludeECOChangeNames && slExcludeECOChangeNames.size() > 0){

               				 if(slExcludeECOChangeNames.contains(doTargetECO.getInfo(context, EngineeringConstants.SELECT_NAME))){
               					 isSameECO = true;
               					 break;
               				 }
               			 }
               		 }
               	 }


				//Modified for RDO Convergence start
                if (strECORDO != null && !"".equals(strECORDO))
                {
                    strPartRDO = (String) mapPartDetails.get("altowner1");

                    if (strPartRDO != null && !"".equals(strPartRDO))
                    {
                        if (!strECORDO.equalsIgnoreCase(strPartRDO))
                        {
                            if (strRDOMismatchMsg.length() > 0)
                            {
                                strRDOMismatchMsg = strRDOMismatchMsg + ", " + (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
                            }
                            else
                            {
                                strRDOMismatchMsg = (String) mapPartDetails.get(SELECT_TYPE) + " " + (String) mapPartDetails.get(SELECT_NAME) + " " + (String) mapPartDetails.get(SELECT_REVISION);
                            }
                            blnShowRDOMsg = true;
                            continue;
                        }
                    }
                }
              //Modified for RDO Convergence End


 				// Changes done for IR-064742
                DomainRelationship rl = DomainRelationship.connect(context, doTargetECO, RELATIONSHIP_AFFECTED_ITEM, doSourceAffectedItem);
				String objState = (String) mapPartDetails.get(SELECT_CURRENT);
				String objPolicy = (String) mapPartDetails.get(SELECT_POLICY);
				String objRelease = PropertyUtil.getSchemaProperty(context,"policy",
																	objPolicy,
																	"state_Release");
				if (!objRelease.equals(objState)) {
					StringList revSelects = new StringList();
					revSelects.add("previous.current");
					revSelects.add("previous.policy");
					revSelects.add("previous.id");
					Map mRevs = doSourceAffectedItem.getInfo(context, revSelects);
					String revState = (String)mRevs.get("previous.current");
					String revId = (String)mRevs.get("previous.id");
					String revPolicy = (String)mRevs.get("previous.policy");
					// if previous revision is released, connect it as "for revise"
					// and connect this revision "for release"
					if (revId!=null) {
						DomainObject doRev = new DomainObject(revId);
						String revRelease = PropertyUtil.getSchemaProperty(context,"policy",
																			revPolicy,
																			"state_Release");
						if (revState.equals(revRelease)){
							if (!strlTargetAffectedItems.contains(revId)) {
								DomainRelationship.connect(context, doTargetECO, RELATIONSHIP_AFFECTED_ITEM, doRev);
							}
							rl.setAttributeValue(context, strAttrAffectedItemCategory, "Indirect");
						}
					}
				}
            }

        if (blnShowRDOMsg)
        {
            // display warning to the user on RDO mismatch of affected items
            emxContextUtil_mxJPO.mqlWarning(context,
                EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.AffectedItemsRDOMismatchWarning",
                context.getSession().getLanguage()) + strRDOMismatchMsg);
        }

        if (blnAlreadyConnected)
        {
            // display warning to the user on the non-revisable affected items
            emxContextUtil_mxJPO.mqlWarning(context,
                EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.AlreadyConnectedAffectedItemsWarning",
                context.getSession().getLanguage()) + strAlreadyConnected);
        }

        if (isSameECO)
        {
            // display warning to the user on RDO mismatch of affected items
            emxContextUtil_mxJPO.mqlWarning(context,
                EngineeringUtil.i18nStringNow(context,"emxEngineeringCentral.ECO.SameECO",
                context.getSession().getLanguage()));
        }

        //376812 - Starts
        AccessList aclList = new AccessList(1);
        aclList.add(accessMask);
        ContextUtil.pushContext(context,DomainConstants.PERSON_WORKSPACE_ACCESS_GRANTOR,null,null);
        BusinessObject.revokeAccessRights(context, objectList, aclList);
        ContextUtil.popContext(context);
        //376812 - Ends
    }
	/**
	     * Display the RelatedECR field in ECO WebForm.
	     * @param context the eMatrix <code>Context</code> object
	     * @param args contains a MapList with the following as input arguments or entries:
	     * objectId holds the context ECO object Id
	     * @throws Exception if the operations fails
	     * @since EC - X4
	     */
	    public String getRelatedECRList (Context context, String[] args) throws Exception {

	        StringBuffer sbReturnString = new StringBuffer(1024);
	        sbReturnString.append("<input type=\"text\"  name=\"RelatedECRDisplay\" readonly=\"true\" id=\"\" value=\"");
	        sbReturnString.append("");
	        sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
	        sbReturnString.append("</input>");
	        sbReturnString.append("<input type=\"hidden\"  name=\"RelatedECR\" value=\"");
	        sbReturnString.append("");
	        sbReturnString.append("\">");
	        sbReturnString.append("</input>");

	        sbReturnString.append("<input type=\"button\" name=\"btnRelatedECR\" value=\"...\"   onclick=\"javascript:showChooser('../engineeringcentral/emxengchgECOSearchECRDialogFS.jsp?form=emxCreateForm&amp;field=RelatedECR&amp;fieldDisp=RelatedECRDisplay&amp;searchcommand=showRelatedECOSearchDialog&amp;createECOMode=true");
	        sbReturnString.append("");
	        sbReturnString.append("','700','500')\">");
	        sbReturnString.append("</input>");
	        sbReturnString.append("<a href=\"JavaScript:basicClear('RelatedECR')\">");
	        sbReturnString.append(strClear);
	        sbReturnString.append("</a>");

	        return sbReturnString.toString();
	    }
	    /**
	     * Display the ResponsibleDesignEngineer field in ECO WebForm.
	     * @param context the eMatrix <code>Context</code> object
	     * @param args contains a MapList with the following as input arguments or entries:
	     * objectId holds the context ECO object Id
	     * @throws Exception if the operations fails
	     * @since EC - X4
	     */
	    public String getResponsibleDesignEngineer (Context context, String[] args) throws Exception {

	        StringBuffer sbReturnString = new StringBuffer(2048);
	        sbReturnString.append("<input type=\"text\"  name=\"ResponsibleDesignEngineerDisplay\" readonly=\"true\" id=\"\" value=\"");
	        sbReturnString.append("");
	        sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
	        sbReturnString.append("</input>");
	        sbReturnString.append("<input type=\"hidden\"  name=\"ResponsibleDesignEngineer\" value=\"");
	        sbReturnString.append("");
	        sbReturnString.append("\">");
	        sbReturnString.append("</input>");

	        sbReturnString.append("<input type=\"button\" name=\"btnResponsibleDesignEngineer\" value=\"...\"   onclick=\"javascript:showChooser('../engineeringcentral/emxEngrIntermediateSearchUtil.jsp?field=TYPES=type_Person:USERROLE=role_SeniorDesignEngineer:CURRENT=policy_Person.state_Active&amp;table=ENCAssigneeTable&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;formName=emxCreateForm&amp;fieldNameActual=ResponsibleDesignEngineer&amp;fieldNameDisplay=ResponsibleDesignEngineerDisplay&amp;mode=Chooser&amp;chooserType=PersonChooser&amp;validateField=DesignResponsibilityOID&amp;excludeOIDprogram=emxENCFullSearchBase:excludeOIDPersons");
	        sbReturnString.append("");
	        sbReturnString.append("','700','500')\">");
	        sbReturnString.append("</input>");
	        sbReturnString.append("<a href=\"JavaScript:basicClear('ResponsibleDesignEngineer')\">");
	        sbReturnString.append(strClear);
	        sbReturnString.append("</a>");

	        return sbReturnString.toString();
	    }
	    /**
	     * Display the ResponsibleManufacturingEngineer field in ECO WebForm.
	     * @param context the eMatrix <code>Context</code> object
	     * @param args contains a MapList with the following as input arguments or entries:
	     * objectId holds the context ECO object Id
	     * @throws Exception if the operations fails
	     * @since EC - X4
	     */
	    public String getResponsibleManufacturingEngineer (Context context, String[] args) throws Exception {
	        StringBuffer sbReturnString = new StringBuffer(2048);
	        sbReturnString.append("<input type=\"text\"  name=\"ResponsibleManufacturingEngineerDisplay\" readonly=\"true\" id=\"\" value=\"");
	        sbReturnString.append("");
	        sbReturnString.append("\" maxlength=\"\" size=\"\" onBlur=\"updateHiddenValue(this)\" onfocus=\"storePreviousValue(this)\">");
	        sbReturnString.append("</input>");
	        sbReturnString.append("<input type=\"hidden\"  name=\"ResponsibleManufacturingEngineer\" value=\"");
	        sbReturnString.append("");
	        sbReturnString.append("\">");
	        sbReturnString.append("</input>");

	        sbReturnString.append("<input type=\"button\" name=\"btnResponsibleManufacturingEngineer\" value=\"...\"   onclick=\"javascript:showChooser('../engineeringcentral/emxEngrIntermediateSearchUtil.jsp?field=TYPES=type_Person:USERROLE=role_SeniorManufacturingEngineer:CURRENT=policy_Person.state_Active&amp;table=ENCAssigneeTable&amp;selection=single&amp;submitAction=refreshCaller&amp;hideHeader=true&amp;submitURL=../engineeringcentral/SearchUtil.jsp&amp;formName=emxCreateForm&amp;fieldNameActual=ResponsibleManufacturingEngineer&amp;fieldNameDisplay=ResponsibleManufacturingEngineerDisplay&amp;mode=Chooser&amp;chooserType=PersonChooser&amp;validateField=DesignResponsibilityOID");
	        sbReturnString.append("");
	        sbReturnString.append("','700','500')\">");
	        sbReturnString.append("</input>");
	        sbReturnString.append("<a href=\"JavaScript:basicClear('ResponsibleManufacturingEngineer')\">");
	        sbReturnString.append(strClear);
	        sbReturnString.append("</a>");

	        return sbReturnString.toString();
	    }

		/**
		*Update the Responsible Manufacturing Engineer
		* @param context the Matrix Context
		* @param args no args needed for this method
		* @returns booloen
		* @throws Exception if the operation fails
		* @since EC X+4
		*/
		public Boolean updateResponsibleManufacturingEngineer(Context context, String[] args) throws Exception
		{
			HashMap programMap = (HashMap)JPO.unpackArgs(args);
			HashMap paramMap = (HashMap)programMap.get("paramMap");

			String strECObjectId = (String)paramMap.get("objectId");
			DomainObject domObjECO = new DomainObject(strECObjectId);
			String ATTRIBUTE_RESPONSIBLE_MANUFACTURING_ENGINEER = PropertyUtil.getSchemaProperty(context,"attribute_ResponsibleManufacturingEngineer");
			String strNewRME = (String)paramMap.get("New Value");
			if (strNewRME.length()==0)
			{
				strNewRME =  domObjECO.getAttributeValue(context,ATTRIBUTE_RESPONSIBLE_MANUFACTURING_ENGINEER);
			}
			domObjECO.setAttributeValue(context, ATTRIBUTE_RESPONSIBLE_MANUFACTURING_ENGINEER, strNewRME);

			return Boolean.TRUE;
     	}

	 	/**

	 		This program checks if the Affected items are in Approve state and returns 0 or 1. Note it only
	 		considers policies for those objects which are defined below

	      * @param context
	      *            the eMatrix <code>Context</code> object.
	      * @param args1
	      *            holds objectId.
	      * @param args2
	      *            holds relationship name.
	      * @param args3
	      *            holds policy name.
	      * @param args4
	      *            olds State.
	      * @param args5
	      *            holds TO/FROM.
	      * @param args6
	      *            holds String Resource file name
	      * @param args7
	      *            holds String resource filed key name
	 	 * @param args8
	      *            holds required change attribute name
	 	 * @param args9
	      *            holds required change attribute value
	      * @return Boolean determines whether the connected objects are in
	      *         approperiate state.
	      * @throws Exception if the operation fails.
	      * @since Common X7.
	      */

	    public int checkAffectedItemsState(Context context,String args[]) throws Exception {

	         if (args == null || args.length < 1) {
	               throw (new IllegalArgumentException());
	         }

	        // Parameter retrieval
			String objectId				= args[0];
			String strRelationshipName	= PropertyUtil.getSchemaProperty(context,args[1]).trim();
			String strPolicyName		= args[2];
			String strStates			= args[3];
			boolean boolTo				= args[4].equalsIgnoreCase("TO")?true:false;
			boolean boolFrom			= args[4].equalsIgnoreCase("FROM")?true:false;
			String strResourceFieldId	= args[5];
			String strStringId			= args[6];
			String attrReqChange		= PropertyUtil.getSchemaProperty(context,args[7]).trim();
			String attrReqChangeValue	= args[8];
			String tempType = "";
			String tempState = "";
			String tempPolicy = "";

	 		int ichkvalue = 0;
	 		Hashtable hStatePolicy = new Hashtable();
	 		StringBuffer strMsg  = new StringBuffer();
	 		String strMessage			= EnoviaResourceBundle.getProperty(context, strResourceFieldId, context.getLocale(),strStringId);

	 		try{

	 		setId(objectId);

	 		StringTokenizer stkPolicy	= new StringTokenizer(strPolicyName,",");

	 		/*Extracting Policy and States */
	 		while (stkPolicy.hasMoreElements()) {
	 			String sPolicy	= stkPolicy.nextToken().trim();
	 			hStatePolicy.put(sPolicy,strStates.trim());
	 		}

	 		String strRelnWhereClause = null;
	 		if (RELATIONSHIP_AFFECTED_ITEM.equals(strRelationshipName))
	 		{
	 			strRelnWhereClause = "attribute["+attrReqChange+"] == \""+attrReqChangeValue+"\"";
	 		}

	 		StringList busSelects = new StringList(6);
	 		busSelects.add(DomainConstants.SELECT_ID);
	 		busSelects.add(DomainConstants.SELECT_CURRENT);
	 		busSelects.add(DomainConstants.SELECT_POLICY);
	 		busSelects.add(DomainConstants.SELECT_TYPE);
	 		busSelects.add(DomainConstants.SELECT_NAME);
	 		busSelects.add(DomainConstants.SELECT_REVISION);

	 		StringList relSelects = new StringList(1);
	 		relSelects.add(DomainConstants.SELECT_ID);


	 		MapList maplistObjects = getRelatedObjects(context,
	                 strRelationshipName,
	                 "*",
	                 busSelects, // object Select
	                 relSelects, // rel Select
	                 boolFrom, // to
	                 boolTo, // from
	                 (short)1,
	                 null, // ob where
	                 strRelnWhereClause  // rel where
	                 );

	 	   if (maplistObjects != null && (maplistObjects.size() > 0)){

	 				Iterator itr = maplistObjects.iterator();
	 				while (itr.hasNext()){
	                     Map mapObject = (Map) itr.next();
	 					String sSt = mapObject.get(DomainConstants.SELECT_CURRENT).toString();
	 					String sPo = mapObject.get(DomainConstants.SELECT_POLICY).toString();

	 					String strSymbolicPolicy = FrameworkUtil.getAliasForAdmin(context, "policy", sPo, true);
	 		 			String strSymbolicState = FrameworkUtil.reverseLookupStateName(context,sPo,sSt);

	 					if (!"state_Release".equals(strSymbolicState) && hStatePolicy.containsKey(strSymbolicPolicy) && !(hStatePolicy.get(strSymbolicPolicy).equals(strSymbolicState))) {
	 						tempType = (String)mapObject.get(DomainConstants.SELECT_TYPE);
	 						tempType = StringUtils.replace(tempType, " ", "_");
	 						tempState = (String)mapObject.get(DomainConstants.SELECT_CURRENT);
	 						tempState = StringUtils.replace(tempState, " ", "_");
	 						tempPolicy = StringUtils.replace(sPo, " ", "_");

	 						strMsg.append("\t \'").append(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.Type."+tempType))
								.append("\' \'").append(mapObject.get(DomainConstants.SELECT_NAME)).append("\' \'")
								.append(mapObject.get(DomainConstants.SELECT_REVISION)).append("\' \'")
								.append(EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", context.getLocale(),"emxFramework.State."+tempPolicy+"."+tempState))
								.append("\'\n");

	 						ichkvalue = 1;
	 					}

	 			    }
	 	   }
	 			if(ichkvalue == 1){
	 		        emxContextUtil_mxJPO.mqlNotice(context, strMessage+"\n\n"+strMsg.toString());
	             }

	         } catch(Exception e){
	 			System.out.println("Exception in emxChangeBase - checkAffectedItemsState method "+e.toString());
	 			e.printStackTrace();
	             throw e;
	         }

	 		return ichkvalue;
     }


   /**
     * Program HTML Output for Review and approval list
     * @param context   the eMatrix <code>Context</code> object
     * @param args      holds a Map with the following input arguments :
     *    mode          the mode in which a field need to be displayed
     *    name          the field name to be displayed
     *    PFmode        flag to find if in Printer friendly mode or not
     *    objectId      context Engineering Change objectId
     * @throws          Exception if the operations fails
     * @return          String which contains the HTML code for displaying field
     * @since           Common 10-6
     */
    public String buildReviewApproveFields(Context context, String[] args)
            throws Exception {

        StringBuffer outPut = new StringBuffer();
        try {
            // unpacking the Arguments from variable args
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            HashMap requestMap = (HashMap) programMap.get("requestMap");
            HashMap fieldMap = (HashMap) programMap.get("fieldMap");
            String TYPE_MECO = PropertyUtil.getSchemaProperty(context,"type_MECO");

            // Getting mode parameter
            String mode = (String) requestMap.get("mode");
            if (mode == null) {
                mode = "view";
            }
            String fieldName = (String) fieldMap.get("name");
            String strPFmode = (String) requestMap.get("PFmode");
            String strPDFrender = (String) requestMap.get("PDFrender");
            String reportFormat = (String) requestMap.get("reportFormat");
            StringBuffer strBufNamesForExport = new StringBuffer();

            // Getting the EC Object id and the new product object id
            String strECObjectId = (String) paramMap.get("objectId");
            // Relationship name
            String strRelationship = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
            String strType = DomainConstants.TYPE_ROUTE_TEMPLATE;

            String strClear = EnoviaResourceBundle.getProperty(context,RESOURCE_BUNDLE_COMPONENTS_STR, context.getLocale(),"emxComponents.Button.Clear");

            DomainObject doEcrEco = new DomainObject(strECObjectId);

            String strChangeType = doEcrEco.getInfo(context, DomainConstants.SELECT_TYPE);

            StringList objectSelects = new StringList();
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_ID);

            // Stringlist containing the relselects
            StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            // setting the context to the Engineering Change object id
            setId(strECObjectId);

            String stateActive = DomainConstants.STATE_ROUTE_TEMPLATE_ACTIVE;
            String objWhere = "(latest == 'true') && (current == '" + stateActive + "')";

            // Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();

            StringList objectSelects1 = new StringList();
            objectSelects1.addElement(DomainConstants.SELECT_ID);

            String relWhere1 = "to.relationship["
                    + RELATIONSHIP_INITIATING_ROUTE_TEMPLATE
                    + "].to.attribute["
                    + DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE + "]==";

            if (fieldName.equalsIgnoreCase("ApprovalList")) {
                relWhere1 = relWhere1 + RANGE_APPROVAL;
            } else if (fieldName.equalsIgnoreCase("ReviewerList"))
                relWhere1 = relWhere1 + RANGE_REVIEW;

            // Added to Exclude the Adhoc routes
            relWhere1 = relWhere1 + "&& to.relationship["
                    + RELATIONSHIP_INITIATING_ROUTE_TEMPLATE
                    + "].to.relationship[" + strRelationship + "].from.id == "
                    + strECObjectId.trim();

            MapList routeList = getRelatedObjects(context, strRelationship,
                    DomainConstants.TYPE_ROUTE, objectSelects1, null, false,
                    true, (short) 1, DomainConstants.EMPTY_STRING, relWhere1);

            if (routeList.size() > 0) {
                Iterator itrTemplates = routeList.iterator();

                while (itrTemplates.hasNext()) {
                    Map mpRoutes = (Map) itrTemplates.next();
                    String strTemplaeID = (String) mpRoutes.get(SELECT_ID);
                    DomainObject objRoute = (DomainObject) DomainObject.newInstance(context);
                    objRoute.setId(strTemplaeID);

                    relationshipIdList = objRoute.getRelatedObjects(context,
                            RELATIONSHIP_INITIATING_ROUTE_TEMPLATE, strType,
                            objectSelects, relSelectsList, false, true,
                            (short) 1, DomainConstants.EMPTY_STRING, "");
                }
            } else {
                relationshipIdList = getRelatedObjects(context,
                        strRelationship, strType, objectSelects,
                        relSelectsList, false, true, (short) 1, objWhere,
                        DomainConstants.EMPTY_STRING);
            }

            StringList relIdStrList = new StringList();
            StringList routeTemplateIdStrList = new StringList();
            StringList routeTemplateNameStrList = new StringList();
            if (DomainConstants.TYPE_ECO.equals(strChangeType) || TYPE_MECO.equals(strChangeType)) {
                if (!"true".equalsIgnoreCase(strPDFrender)) {
                    outPut.append(" <script> ");
                    outPut.append("function showECApprovalList() { ");
                    String altOwnerFilter = EngineeringUtil.getAltOwnerFilterString(context);
                    outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Approval:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE"
                          + altOwnerFilter
                          + "&table=APPECRouteTemplateSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameActual=ApprovalListOID&fieldNameDisplay=ApprovalListDisplay&submitURL=../engineeringcentral/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");

                    outPut.append('}');

                    outPut.append("function showECReviewerList() { ");

                    outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Review:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE"
                          + altOwnerFilter
                          + "&table=APPECRouteTemplateSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameActual=ReviewerListOID&fieldNameDisplay=ReviewerListDisplay&submitURL=../engineeringcentral/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");
                    outPut.append('}');
                    outPut.append(" </script> ");
                }
            } else {
                outPut.append(" <script> ");
                if (fieldName.equalsIgnoreCase("ApprovalList")) {
                    outPut.append("function showECApprovalList() { ");
                    outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Approval:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE&table=APPECRouteTemplateSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameActual=ApprovalListOID&fieldNameDisplay=ApprovalListDisplay&submitURL=../engineeringcentral/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");
                } else {
                    outPut.append("function showECReviewerList() { ");
                    outPut.append("javascript:showModalDialog(\"../common/emxFullSearch.jsp?field=TYPES=type_RouteTemplate:ROUTE_BASE_PURPOSE=Review:CURRENT=policy_RouteTemplate.state_Active:LATESTREVISION=TRUE&table=APPECRouteTemplateSearchList&selection=single&submitAction=refreshCaller&hideHeader=true&formName=editDataForm&frameName=formEditDisplay&fieldNameActual=ReviewerListOID&fieldNameDisplay=ReviewerListDisplay&submitURL=../engineeringcentral/SearchUtil.jsp&mode=Chooser&chooserType=FormChooser&HelpMarker=emxhelpfullsearch\" ,850,630); ");
                }
                outPut.append('}');
                outPut.append(" </script> ");
            }

            if (relationshipIdList.size() > 0) {
                // relationship object route
                DomainRelationship relationObject = null;
                String strRoutePurposeVal = "";

                // Getting the realtionship ids and relationship names from the list
                for (int i = 0; i < relationshipIdList.size(); i++) {
                    relIdStrList.add((String) ((Hashtable) relationshipIdList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID));
                    routeTemplateIdStrList.add((String) ((Hashtable) relationshipIdList.get(i)).get(DomainConstants.SELECT_ID));
                    routeTemplateNameStrList.add((String) ((Hashtable) relationshipIdList.get(i)).get(DomainConstants.SELECT_NAME));
                }

                if (relIdStrList.size() > 0) {
                    for (int i = 0; i < relIdStrList.size(); i++) {
                        relationObject = new DomainRelationship((String) relIdStrList.get(i));

                        if (routeList.size() > 0 && fieldName.equalsIgnoreCase("ApprovalList"))
                            strRoutePurposeVal = RANGE_APPROVAL;
                        else if (routeList.size() > 0 && fieldName.equalsIgnoreCase("ReviewerList"))
                            strRoutePurposeVal = RANGE_REVIEW;
                        else
                            strRoutePurposeVal = relationObject.getAttributeValue(
                                                        context,
                                                        DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);

                        if (fieldName.equalsIgnoreCase("ApprovalList") && strRoutePurposeVal.equalsIgnoreCase(RANGE_APPROVAL)) {
                            if (mode == null || mode.equalsIgnoreCase("view")) {
                                if (strPFmode != null&& strPFmode.equalsIgnoreCase("true")) {
                                    outPut.append("<img src='../common/images/iconSmallRouteTemplate.gif' border=0>");
                                    outPut.append("&nbsp;");
                                    outPut.append(routeTemplateNameStrList.get(i));
                                } else {
                                    if ("true".equalsIgnoreCase(strPDFrender)) {
                                        outPut.append(routeTemplateNameStrList.get(i));
                                    } else {
                                        outPut.append("<a href=\"javascript:showModalDialog('emxTree.jsp?objectId="
                                                        + routeTemplateIdStrList.get(i)
                                                        + "',500,700);\">");
                                        outPut.append("<img src='../common/images/iconSmallRouteTemplate.gif' border=0>");
                                        outPut.append("&nbsp;");
                                        outPut.append(routeTemplateNameStrList.get(i));
                                        outPut.append("</a>");
                                    }
                                    if (reportFormat != null && reportFormat.length() > 0) {
                                        strBufNamesForExport.append(routeTemplateNameStrList.get(i));
                                    }
                                }
                            } else if (mode.equalsIgnoreCase("edit")) {
                                outPut.append("<input type=\"text\" name=\"ApprovalListDisplay");
                                outPut.append("\"size=\"20\" value=\"");
                                outPut.append(routeTemplateNameStrList.get(i));
                                outPut.append("\" readonly=\"readonly\">&nbsp;");
                                outPut.append("<input class=\"button\" type=\"button\"");
                                outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");
                                outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECApprovalList()\">");
                                outPut.append("<input type=\"hidden\" name=\"ApprovalListOID\" value=\""
                                                + routeTemplateIdStrList.get(i)
                                                + "\"></input>");
                                outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ApprovalList')\">"
                                                + strClear + "</a>");
                            }
                        } else if (fieldName.equalsIgnoreCase("ReviewerList") && strRoutePurposeVal.equalsIgnoreCase(RANGE_REVIEW)) {
                            if (mode == null || mode.equalsIgnoreCase("view")) {
                                if (strPFmode != null && strPFmode.equalsIgnoreCase("true")) {
                                    outPut.append("<img src='../common/images/iconSmallRouteTemplate.gif' border=0>");
                                    outPut.append("&nbsp;");
                                    outPut.append(routeTemplateNameStrList.get(i));
                                } else {

                                    if ("true".equalsIgnoreCase(strPDFrender)) {
                                        outPut.append(routeTemplateNameStrList.get(i));
                                    } else {
                                        outPut.append("<a href=\"javascript:showModalDialog('emxTree.jsp?objectId="
                                                        + routeTemplateIdStrList.get(i)
                                                        + "',500,700);\">");
                                        outPut.append("<img src='../common/images/iconSmallRouteTemplate.gif' border=0>");
                                        outPut.append("&nbsp;");
                                        outPut.append(routeTemplateNameStrList.get(i));
                                        outPut.append("</a>");
                                    }
                                    if (reportFormat != null&& reportFormat.length() > 0) {
                                        strBufNamesForExport.append(routeTemplateNameStrList.get(i));
                                    }
                                }
                            } else if (mode.equalsIgnoreCase("edit")) {
                                outPut.append("<input type=\"text\" name=\"ReviewerListDisplay");
                                outPut.append("\"size=\"20\" value=\"");
                                outPut.append(routeTemplateNameStrList.get(i));
                                outPut.append("\" readonly=\"readonly\">&nbsp;");
                                outPut.append("<input class=\"button\" type=\"button\"");
                                outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");
                                outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECReviewerList()\">");
                                outPut.append("<input type=\"hidden\" name=\"ReviewerListOID\" value=\""
                                                + routeTemplateIdStrList.get(i)
                                                + "\"></input>");
                                outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ReviewerList')\">"
                                                + strClear + "</a>");
                            }
                        } else if (relIdStrList.size() == 1) {
                            // When any one of Review orApproval List is present
                            if (mode.equalsIgnoreCase("edit")) {
                                if (fieldName.equalsIgnoreCase("ReviewerList")) {
                                    outPut.append("<input type=\"text\" name=\"ReviewerListDisplay");
                                    outPut.append("\"size=\"20\" value=\"");
                                    outPut.append("\" readonly=\"readonly\">&nbsp;");
                                    outPut.append("<input class=\"button\" type=\"button\"");
                                    outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");
                                    outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECReviewerList()\">");
                                    outPut.append("<input type=\"hidden\" name=\"ReviewerListOID\" value=\"\"></input>");
                                    outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ReviewerList')\">"
                                                    + strClear + "</a>");
                                } else if (fieldName.equalsIgnoreCase("ApprovalList")) {
                                    outPut.append("<input type=\"text\" name=\"ApprovalListDisplay");
                                    outPut.append("\"size=\"20\" value=\"");
                                    outPut.append("\" readonly=\"readonly\">&nbsp;");
                                    outPut.append("<input class=\"button\" type=\"button\"");
                                    outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");
                                    outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECApprovalList()\">");
                                    outPut.append("<input type=\"hidden\" name=\"ApprovalListOID\" value=\"\"></input>");
                                    outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ApprovalList')\">"
                                                    + strClear + "</a>");
                                }
                            }
                        }
                    }
                }
            } else { //if there are no relationships fields are to be dispalyed only in edit mode
                if (fieldName.equalsIgnoreCase("ApprovalList") && mode.equalsIgnoreCase("edit")) {
                    outPut.append("<input type=\"text\" name=\"ApprovalListDisplay");
                    outPut.append("\"size=\"20\" value=\"");
                    outPut.append("\" readonly=\"readonly\">&nbsp;");
                    outPut.append("<input class=\"button\" type=\"button\"");
                    outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");

                    outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECApprovalList()\">");
                    outPut.append("<input type=\"hidden\" name=\"ApprovalListOID\" value=\"\"></input>");
                    outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ApprovalList')\">"
                                    + strClear + "</a>");
                } else if (fieldName.equalsIgnoreCase("ReviewerList") && mode.equalsIgnoreCase("edit")) {
                    outPut.append("<input type=\"text\" name=\"ReviewerListDisplay");
                    outPut.append("\"size=\"20\" value=\"");
                    outPut.append("\" readonly=\"readonly\">&nbsp;");
                    outPut.append("<input class=\"button\" type=\"button\"");
                    outPut.append(" name=\"btnECReviewerListChooser\" size=\"200\" ");

                    outPut.append("value=\"...\" alt=\"\"  onClick=\"javascript:showECReviewerList()\">");
                    outPut.append("<input type=\"hidden\" name=\"ReviewerListOID\" value=\"\"></input>");
                    outPut.append("&nbsp;&nbsp;<a href=\"javascript:basicClear('ReviewerList')\">"
                                    + strClear + "</a>");
                }
            }

            if ((strBufNamesForExport.length() > 0)
                    || (reportFormat != null && reportFormat.length() > 0)) {
                outPut = strBufNamesForExport;
            }

        } catch (Exception ex) {
            throw new FrameworkException((String) ex.getMessage());
        }

        return outPut.toString();
    }

    /**
     * It is used to connect the reviewers list on ECO creation.
     * @param context
     * @param args
     * @throws Exception
     */
    public void connectReviewerList(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap requestMap= (HashMap)programMap.get("requestMap");
            String[] strMod= (String[])requestMap.get("CreateMode");
            String strMode = strMod[0];
            if((strMode!=null && "AssignToECO".equalsIgnoreCase(strMode))||(strMode!=null && "MoveToECO".equalsIgnoreCase(strMode))) {
                connectAssignMoveReviewerList(context, args);
            }else {
                connectApproverReviewerList(context, args);
            }
        }catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    /**
    * Updates the Review list field values in ECR WebForm.
    * @param context the eMatrix <code>Context</code> object
    * @param args holds a Map with the following input arguments:
    * objectId objectId of the context Engineering Change
    * New Value objectId of updated Review List value
    * @throws Exception if the operations fails
    * @since Common X3
    */
        public void connectApproverReviewerList (Context context, String[] args) throws Exception {
            try{
                    //unpacking the Arguments from variable args
                    HashMap programMap = (HashMap)JPO.unpackArgs(args);
                    HashMap paramMap   = (HashMap)programMap.get("paramMap");
                    String strRelationship = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;

                    String strNewValue = (String)paramMap.get("New OID");
                    DomainRelationship drship=null;

                    if (strNewValue == null || "".equals(strNewValue) || "Unassigned".equalsIgnoreCase(strNewValue) || "null".equalsIgnoreCase(strNewValue) || " ".equals(strNewValue)) {
                                    strNewValue = (String)paramMap.get("New Value");
                    }
                    if((strNewValue != null) && !("".equals(strNewValue)) ||  "Unassigned".equalsIgnoreCase(strNewValue) || "null".equalsIgnoreCase(strNewValue)) {
                        DomainObject newValue =  new DomainObject(strNewValue);
                        String strAttribute = newValue.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);

                        if("Review".equals(strAttribute)){
                            drship = connect(context,paramMap,strRelationship);
                            drship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strAttribute);
                        }
                        if("Approval".equals(strAttribute)){
                            drship = connect(context,paramMap,strRelationship);
                            drship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strAttribute);
                        }
                    }
               } catch(Exception ex) {
                   throw  new FrameworkException((String)ex.getMessage());
               }
        }


     /**
    * Updates the Review list field values in ECR WebForm.
       * @param context the eMatrix <code>Context</code> object
    * @param args holds a Map with the following input arguments:
    * objectId objectId of the context Engineering Change
    * New Value objectId of updated Review List value
       * @throws Exception if the operations fails
       * @since Common X3.
    */
    public void connectAssignMoveReviewerList (Context context, String[] args) throws Exception {
        try {
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");
            HashMap requestMap= (HashMap)programMap.get("requestMap");
            String strRelationship = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;

            String[] strMod= (String[])requestMap.get("CreateMode");
            String strMode = strMod[0];
            if((strMode!=null && "AssignToECO".equalsIgnoreCase(strMode))||(strMode!=null && "MoveToECO".equalsIgnoreCase(strMode))) {
                // Creating DomainObject of new ECO
                DomainRelationship drship=null;
                String strNewValue = (String)paramMap.get("New OID");

                if((strNewValue == null)||(strNewValue.equals("null"))||(strNewValue.equals(""))){
                strNewValue = (String)paramMap.get("New Value");
                }

                if((strNewValue != null) && (!strNewValue.equals("null")) && (!strNewValue.equals(""))) {
                    DomainObject newValue =  new DomainObject(strNewValue);
                    String strAttribute = newValue.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);

                    if("Review".equals(strAttribute)){
                        drship = connect(context,paramMap,strRelationship);
                        drship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strAttribute);
                    }

                    if("Approval".equals(strAttribute)){
                                    drship = connect(context,paramMap,strRelationship);
                                    drship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strAttribute);
                    }
                }
            }
        } catch(Exception ex){
                throw  new FrameworkException((String)ex.getMessage());
        }
    }

/**
   * Connects ECR/ECO with the Passed Object.
   * @param context the eMatrix <code>Context</code> object
   * @param Hashmap holds the input arguments:
   * strRelationship holds relationship with which ECR will be connected
   * New Value is object Id of updated Object
   * @throws Exception if the operations fails
   * @since EC 2011x.
    */
      public DomainRelationship connect(Context context , HashMap paramMap ,String strRelationship)throws Exception {
         try{
                DomainRelationship drship=null;
                DomainObject oldListObject = null;
                DomainObject newListObject = null;
                //Getting the ECR Object id and the new MemberList object id
                String strChangeobjectId = (String)paramMap.get("objectId");
                DomainObject changeObj =  new DomainObject(strChangeobjectId);
                String strNewToTypeObjId = (String)paramMap.get("New OID");
                if (strNewToTypeObjId == null || "null".equals(strNewToTypeObjId) ||
                        strNewToTypeObjId.length() <= 0 || "Unassigned".equals(strNewToTypeObjId)) {
                    strNewToTypeObjId = (String)paramMap.get("New Value");
                }
                //for bug 343816 and 343817 ends
                String strOldToTypeObjId = (String)paramMap.get("Old OID");

                DomainRelationship newRelationship=null;
                RelationshipType relType = new RelationshipType(strRelationship);
                if (strOldToTypeObjId != null && !"null".equals(strOldToTypeObjId) &&
                    strOldToTypeObjId.length() > 0 && !"Unassigned".equals(strOldToTypeObjId)) {

                        oldListObject = new DomainObject(strOldToTypeObjId);
                        changeObj.disconnect(context,relType,true,oldListObject);

                }
                if(strNewToTypeObjId != null && !"null".equals(strNewToTypeObjId) && strNewToTypeObjId.length() > 0
                   &&  !"Unassigned".equals(strNewToTypeObjId)) {

                    try{
                            newListObject = new DomainObject(strNewToTypeObjId);
                            drship = new DomainRelationship(newRelationship.connect(context,changeObj,relType,newListObject)) ;
                        }catch(Exception ex){ }
                }

               return drship;
             } catch(Exception ex) {
                 throw  new FrameworkException((String)ex.getMessage());
             }
      }

      /*
      Updates the Review list field values in Engineering change WebForm.
      * @param context         the eMatrix <code>Context</code> object
      * @param args            holds a Map with the following input arguments:
      *    objectId            objectId of the context Engineering Change
      *    New Value           objectId of updated Approval List value
      * @throws                Exception if the operations fails
      * @since                 Common 10-6
      */
     public void updateObjectRouteApproval(Context context, String[] args) throws Exception {
        try{
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap = (HashMap)programMap.get("paramMap");

            //Relationship name
            String strRelationship      = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
            String strType              = DomainConstants.TYPE_ROUTE_TEMPLATE;
            DomainRelationship oldRelationship = null;
            DomainObject domainObjectToType = null;
            String strNewToTypeObjId = "";
            String strRouteBasePurpose = "";
            String strTempRelRouteBasePurpose = "";
            DomainRelationship newRelationship = null;

            //Getting the EC Object id and the new product object id
            String strECObjectId = (String)paramMap.get("objectId");
            //modified for IR-016954
            strNewToTypeObjId = (String)paramMap.get("New OID");
            //commented the loop for the fix IR-016954
            if(strNewToTypeObjId == null || "".equals(strNewToTypeObjId)){
                strNewToTypeObjId = (String) paramMap.get("New Value");
            }

            if (strNewToTypeObjId != null && !strNewToTypeObjId.equalsIgnoreCase("")) {
                //Instantiating DomainObject with the new Route Template object id
                domainObjectToType = newInstance(context, strNewToTypeObjId);
                strRouteBasePurpose = domainObjectToType.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
            }

            //Business Objects are selected by its Ids
            StringList objectSelects = new StringList(2);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_ID);

            //Stringlist containing the relselects
            StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //setting the context to the Engineering Change object id
            setId(strECObjectId);

            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
            //Calling getRelatedObjects to get the relationship ids
            relationshipIdList = getRelatedObjects(context,
                                                    strRelationship,
                                                    strType,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);

            if(relationshipIdList.size()>0){
                for (int i=0;i<relationshipIdList.size();i++) {
                    //Getting the realtionship ids from the list
                    String strRelationshipId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    //Getting Route Object Id from the list
                    String strRouteId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_ID);


                    oldRelationship = new DomainRelationship(strRelationshipId);
                    strTempRelRouteBasePurpose = oldRelationship.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
                    //Modified for bug#302310,302308 by Infosys on 21 Apr 05
                    if(strTempRelRouteBasePurpose.equalsIgnoreCase(RANGE_APPROVAL)) {
                        //Checking if the selected Object id is the same as the selected one and exiting the program.
                        if(strRouteId.equals(strNewToTypeObjId)) {
                            return;
                        }
                        //Disconnecting the existing relationship
                        DomainRelationship.disconnect(context, strRelationshipId);
                    }
                }
            }

            if (domainObjectToType != null) {
                //Connecting the Engineering Change with the new Route Template object with relationship Object Route
                newRelationship = DomainRelationship.connect(context,
                                                             this,
                                                             strRelationship,
                                                             domainObjectToType);
                newRelationship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strRouteBasePurpose);
            }

        }catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
    }

     /**
      * Updates the Review list field values in Engineering change WebForm.
      * @param context         the eMatrix <code>Context</code> object
      * @param args            holds a Map with the following input arguments:
      *    objectId            objectId of the context Engineering Change
      *    New Value           objectId of updated Review List value
      * @throws                Exception if the operations fails
      * @since                 Common 10-6
      */
     public void updateObjectRouteReview (Context context, String[] args) throws Exception {
        try {
            //unpacking the Arguments from variable args
            HashMap programMap = (HashMap)JPO.unpackArgs(args);
            HashMap paramMap   = (HashMap)programMap.get("paramMap");

            //Relationship name
            String strRelationship              = DomainConstants.RELATIONSHIP_OBJECT_ROUTE;
            String strType                      = DomainConstants.TYPE_ROUTE_TEMPLATE;
            DomainRelationship oldRelationship = null;
            DomainObject domainObjectToType = null;
            String strNewToTypeObjId        = "";
            String strRouteBasePurpose = "";
            String strTempRelRouteBasePurpose = "";
            DomainRelationship newRelationship = null;

            //Getting the EC Object id and the new product object id
            String strECObjectId = (String)paramMap.get("objectId");

            strNewToTypeObjId = (String)paramMap.get("New OID");

            if(strNewToTypeObjId == null || "".equals(strNewToTypeObjId)){
                strNewToTypeObjId = (String) paramMap.get("New Value");
            }

            if (strNewToTypeObjId != null && !strNewToTypeObjId.equalsIgnoreCase("")) {
                domainObjectToType = newInstance(context, strNewToTypeObjId);
                strRouteBasePurpose = domainObjectToType.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
            }

            //Business Objects are selected by its Ids
            StringList objectSelects = new StringList(2);
            objectSelects.addElement(DomainConstants.SELECT_NAME);
            objectSelects.addElement(DomainConstants.SELECT_ID);

            //Stringlist containing the relselects
            StringList relSelectsList = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
            //setting the context to the Engineering Change object id
            setId(strECObjectId);

            //Maplist containing the relationship ids
            MapList relationshipIdList = new MapList();
            //Calling getRelatedObjects to get the relationship ids
            relationshipIdList = getRelatedObjects(context,
                                                    strRelationship,
                                                    strType,
                                                    objectSelects,
                                                    relSelectsList,
                                                    false,
                                                    true,
                                                    (short)1,
                                                    DomainConstants.EMPTY_STRING,
                                                    DomainConstants.EMPTY_STRING);

            if (relationshipIdList.size() > 0) {
                for (int i=0;i<relationshipIdList.size();i++) {
                    //Getting the realtionship ids from the list
                    String strRelationshipId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_RELATIONSHIP_ID);
                    //Getting Route Object Id from the list
                    String strRouteId = (String)((Hashtable)relationshipIdList.get(i)).get(DomainConstants.SELECT_ID);

                    oldRelationship = new DomainRelationship(strRelationshipId);
                    strTempRelRouteBasePurpose = oldRelationship.getAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE);
                    if(strTempRelRouteBasePurpose.equalsIgnoreCase(RANGE_REVIEW)) {
                        //Checking if the selected Object id is the same as the selected one and exiting the program.
                        if(strRouteId.equals(strNewToTypeObjId)) {
                            return;
                        }
                        //Disconnecting the existing relationship
                        try{
                            DomainRelationship.disconnect(context, strRelationshipId);
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (domainObjectToType != null) {
                //Connecting the Engineering Change with the new Route Template object with relationship Object Route
                newRelationship = DomainRelationship.connect(context,
                                                             this,
                                                             strRelationship,
                                                             domainObjectToType);
                newRelationship.setAttributeValue(context,DomainConstants.ATTRIBUTE_ROUTE_BASE_PURPOSE,strRouteBasePurpose);
            }
        } catch(Exception ex){
            throw  new FrameworkException((String)ex.getMessage());
        }
    }

     /**
     * Method to initiate the BOM Go To Production on a Part/Assembly
	 * and to connect the production Parts to an ECO
     * @param context
     * @param args
     * @return Strnig
     * @throws Exception
     */
     @com.matrixone.apps.framework.ui.PostProcessCallable
     public String bomGotoProduction(Context context, String[] args) throws Exception {

 		try{
 		HashMap programMap = (HashMap)JPO.unpackArgs(args);
 		HashMap paramMap   = (HashMap)programMap.get("paramMap");
 		HashMap requestMap= (HashMap)programMap.get("requestMap");

 		String strCOId = programMap.containsKey("changeId") ?
 				(String) programMap.get("changeId") :
 					(String)paramMap.get("objectId");
 		ECO co = new ECO(strCOId);
         String strObjId = programMap.containsKey("selectedPartId") ?
 				(String) programMap.get("selectedPartId") :
 					(String) requestMap.get("selectedPartId");

 		if(strObjId == null) {
 			return "Create CO from Global Action";
 		}

         return co.bomGotoProduction(context, strObjId, "");
 		}
 		catch(Exception e){
 			e.printStackTrace();
 			throw e;
 		}
     }

    /** This method is called from form:BOMGoToProduction field:ECOToRelease to get href dynamically.
     * @param context ematrix context
     * @param args packed arguments
     * @return String which is used as form field href.
     * @throws Exception if any operation fails.
     */
    public String getHrefOfChangeForBOMGoToProd(Context context, String[] args) throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        Map fieldValuesMap = (HashMap)programMap.get("fieldValues");
        String orgId =  (String)fieldValuesMap.get("DesignResponsiblity");

        //Added for IR-216667
        if(orgId != null && !"null".equals(orgId))
        	orgId = orgId.trim();

        String rdoFilter = (orgId != null && !"".equals(orgId) && !"null".equals(orgId)) ? ":ORGANIZATION=" + orgId : "";

        String searchURL = "TYPES=type_ECO:POLICY=policy_ECO:CURRENT=policy_ECO.state_Create,policy_ECO.state_DefineComponents,policy_ECO.state_DesignWork" + rdoFilter;

        return searchURL;
    }

    public boolean hideRDOFieldForTBEInstalled(Context context, String[] args) throws Exception {
    	return !EngineeringUtil.isENGSMBInstalled(context);
    }


    /** This method is called from searchutil.jsp to get RDO name and RDO Id of selected ECO.
     * @param context ematrix context
     * @param args packed arguments
     * @return HashMap containing RDO id and name
     * @throws Exception if any operation fails.
     */
    public HashMap getRDOOfSelectedECO(Context context, String[] args) throws Exception {
        String selectedECOId = args[0];
        String strRDOId = "";
        String strRDOName = "";

        if (selectedECOId != null && !"".equals(selectedECOId)) {
        	String SELECT_RDO_NAME = "to[" + DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.name";
        	String SELECT_RDO_ID   = "to[" + DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id";

        	StringList rdoSelectList = new StringList(2);
        	rdoSelectList.add(SELECT_RDO_NAME);
        	rdoSelectList.add(SELECT_RDO_ID);

        	DomainObject domObj = DomainObject.newInstance(context, selectedECOId);

        	Map dataMap = domObj.getInfo(context, rdoSelectList);
        	String temp = (String) dataMap.get(SELECT_RDO_NAME);

        	if (temp != null && !"".equals(temp)) {
        		strRDOId = (String) dataMap.get(SELECT_RDO_ID);
        		strRDOName = temp;
        	}
        }

        HashMap rdoMap = new HashMap();
        rdoMap.put("rdoName", strRDOName);
        rdoMap.put("rdoId", strRDOId);

        return rdoMap;
    }

    /** This method is called from form:BOMGoToProduction field:CreateOrSelectECO to get html output.
     * @param context ematrix context
     * @param args packed arguments
     * @return String which is used as form field href.
     * @throws Exception if any operation fails.
     */
    public String getChangeFieldForBomGoToProduction(Context context, String[] args) throws Exception {
 	    String AddExisting = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxFramework.Command.AddExisting");
 	    String createNewCO   = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxFramework.Command.CreateNewCO");
 	    String createNewCR   = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxFramework.Command.CreateNewCR");


 	    StringBuffer sbHTMLTag = new StringBuffer(800);

 	   sbHTMLTag.append("<input type=radio name='ecoOptions' value='createNewCO' onclick='javascript:hideSearchOption()' checked='checked'>").
	    append(createNewCO).append("<br>").append("<input type=radio name='ecoOptions' value='createNewCR' onclick='javascript:hideSearchOption()'>").
	    append(createNewCR).append("<br>").append("<input type=radio name='ecoOptions' value='AddExisting' onclick='javascript:showSearchOption()'>")
	    .append(AddExisting);

 	    sbHTMLTag.append("<script language=javascript>");
		sbHTMLTag.append("function hideSearchOption() {");
		sbHTMLTag.append("document.editDataForm.btnCOToRelease.disabled = true;");
		sbHTMLTag.append("document.editDataForm.COToReleaseDisplay.disabled = true;");
		sbHTMLTag.append("basicClear('COToRelease');");
		sbHTMLTag.append('}');

		sbHTMLTag.append("function showSearchOption() {");
		sbHTMLTag.append("document.editDataForm.btnCOToRelease.disabled = false;");
		sbHTMLTag.append("document.editDataForm.COToReleaseDisplay.disabled = false;");
		sbHTMLTag.append('}');

 	    sbHTMLTag.append("</script>");

 	    return sbHTMLTag.toString();
    }

    /**
     * Added for JSP to Common components conversion. ECO->Related ECRs
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getECORelatedECRs(Context context,String[] args)
    throws Exception
    {
 		 HashMap programMap = (HashMap) JPO.unpackArgs(args);
 		 String objectId = (String) programMap.get("objectId");
 		 MapList ecrMapList = new MapList();

 		 try
 		 {
 		     ECO ecoObj = new ECO(objectId);
 		     StringList selectStmts = new StringList(1);
 		     selectStmts.addElement(SELECT_ID);

 			 StringList selectRelStmts = new StringList(1);
 			 selectRelStmts.addElement(SELECT_RELATIONSHIP_ID);

 			 ecrMapList = FrameworkUtil.toMapList(ecoObj.getExpansionIterator(context, DomainConstants.RELATIONSHIP_ECO_CHANGEREQUESTINPUT, "*",
 													selectStmts, selectRelStmts, false, true, (short)1,
 													null, null, (short)0,
 													false, false, (short)0, false),
 													(short)0, null, null, null, null);


 			ecrMapList.addSortKey(DomainObject.SELECT_NAME,"ascending", "String");
 			ecrMapList.sort();

 		 }catch (FrameworkException Ex){
 		      throw Ex;
 		 }
 		 return ecrMapList ;
 	}
    /**
     * This method displays deleteAffectedItems field of Cancel ECO form with radio button
     * @param context the eMatrix <code>Context</code> object.
     * @param args[] packed hashMap of request parameters
     * @return String containing html data to construct with radio button.
     * @throws Exception if the operation fails.
     * @since R212
      */

   public Object displayAffectedItemWarning(Context context, String[] args) throws Exception
    	{
   		
   		String keepAffectedItems  = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.CancelECODialog.KeepAffectedItems");
   		String deleteAffectedItems  = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.CancelECODialog.DeleteAffectedItems");
   		String disableAffectedItem = FrameworkProperties.getProperty(context, "emxEngineeringCentral.CancelECO.disableDeleteAffectedItems");
   		disableAffectedItem = "true".equalsIgnoreCase(disableAffectedItem) ? "disabled" : "";
   		 StringBuffer strBuf = new StringBuffer(256);
   		 strBuf.append("<table><tr><td align=left>");
   		 strBuf.append("<input type=radio checked name='deleteAffectedItems' value='false'>");
   		 strBuf.append("</td><td align=left>");
   		 strBuf.append(XSSUtil.encodeForHTML(context,keepAffectedItems));
   		 strBuf.append("</td></tr></table>");
   		 strBuf.append("<table><tr><td align=left>");
   		 strBuf.append("<input type=radio name='deleteAffectedItems' value='true' ").append(disableAffectedItem).append(" >");
   		 strBuf.append("</td><td align=left>");
   		 strBuf.append(XSSUtil.encodeForHTML(context,deleteAffectedItems));
   		 strBuf.append("</td></tr></table>");

   		 return strBuf.toString();
}
   /**
    * This method displays disconnectECRs field of Cancel ECO form with radio button
    * @param context the eMatrix <code>Context</code> object.
    * @param args[] packed hashMap of request parameters
    * @return String containing html data to construct with radio button.
    * @throws Exception if the operation fails.
    * @since R212
     */
   public Object displayECRConnectionsWarning(Context context, String[] args) throws Exception
	{
		String disConnectECRs  = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.CancelECODialog.DisconnectFromAssociatedECRs");
		String doNotDisconnectECRs  =  EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.CancelECODialog.DoNotDisconnectFromAssociatedECRs");

		 StringBuffer strBuf = new StringBuffer(256);

		 strBuf.append("<table><tr><td align=left>");
   		 strBuf.append("<input type=radio  name='disconnectECRs' value='true'>");
   		 strBuf.append("</td><td align=left>");
   		 strBuf.append(XSSUtil.encodeForHTML(context, disConnectECRs));
   		 strBuf.append("</td></tr></table>");
   		 strBuf.append("<table><tr><td align=left>");
   		 strBuf.append("<input type=radio checked name='disconnectECRs' value='false'>");
   		 strBuf.append("</td><td align=left>");
   		 strBuf.append(XSSUtil.encodeForHTML(context,doNotDisconnectECRs));
   		 strBuf.append("</td></tr></table>");

		 return strBuf.toString();
	}
   /**
    * This  is the postprocessJPO method for Cancelling ECO
    * @param context the eMatrix <code>Context</code> object.
    * @param args[] packed hashMap of request parameters
    * @throws Exception if the operation fails.
    * @since R212
     */
   @com.matrixone.apps.framework.ui.PostProcessCallable
   public void cancelECOProcess(Context context, String args[]) throws Exception  {
       HashMap programMap = (HashMap)JPO.unpackArgs(args);
       HashMap requestMap = (HashMap) programMap.get("requestMap");
       String sDeleteAffectedItems=   (String) requestMap.get("deleteAffectedItems");
       String sDisconnectECRs=   (String) requestMap.get("disconnectECRs");
       String sReason=   (String) requestMap.get("Reason");
       String objectId=(String) requestMap.get("objectId");
       String[] beanargs=new String[]{objectId,sReason,sDeleteAffectedItems,sDisconnectECRs};
       Change cx= new Change();
       cx.cancelChangeProcess(context, beanargs);
     }

   /**
    * Get the list of all Objects which are required checkbox to be displayed in the Table List Page.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args    holds the following input arguments:
    *            0 - HashMap containing one String entry for key "objectId"
    * @return        a eMatrix <code>MapList</code> object having the list of all Objects which are required checkbox to be displayed in the Table List Page.
    * @throws        Exception if the operation fails
    * @since         Common X3
    **/

	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getAffectedItems(Context context, String[] args)
       throws Exception
   {

		//unpacking the arguments from variable args
		HashMap programMap         = (HashMap)JPO.unpackArgs(args);

		String sENCAffectedItemsTypeFilter = (String) programMap.get("ENCAffectedItemsTypeFilter");
		String sENCAffectedItemsAssigneeFilter = (String) programMap.get("ENCAffectedItemsAssigneeFilter");
		String sENCAffectedItemsRequestedChangeFilter = (String) programMap.get("ENCAffectedItemsRequestedChangeFilter");
		MapList mlAffectedItemBusObjList =null;
		if ("null".equals(sENCAffectedItemsTypeFilter) || sENCAffectedItemsTypeFilter == null || sENCAffectedItemsTypeFilter.length() == 0)
		{
			sENCAffectedItemsTypeFilter = SELECT_ALL;
		}
		if ("null".equals(sENCAffectedItemsAssigneeFilter) || sENCAffectedItemsAssigneeFilter == null || sENCAffectedItemsAssigneeFilter.length() == 0)
		{
			sENCAffectedItemsAssigneeFilter = SELECT_ALL;
		}
		if ("null".equals(sENCAffectedItemsRequestedChangeFilter) || sENCAffectedItemsRequestedChangeFilter == null || sENCAffectedItemsRequestedChangeFilter.length() == 0)
		{
			sENCAffectedItemsRequestedChangeFilter = SELECT_ALL;
		}


		//getting parent object Id from args
		String strParentId         = (String)programMap.get(SELECT_OBJECT_ID);
		HashMap RequestValuesMap          = (HashMap)programMap.get("RequestValuesMap");
		String[] strOID         = (String[])RequestValuesMap.get("objectId");
		String strChangeId = strOID[0];
		//If the Parent Id is that of an ECO and Affected Items Type Filter value is "Markup", it should display all the Markups related to the ECO.
       if(TYPE_PART_MARKUP.equals(sENCAffectedItemsTypeFilter)&&strParentId.equals(strChangeId)){

           Pattern relPattern1 = new Pattern(RELATIONSHIP_APPLIED_MARKUP);
           relPattern1.addPattern(RELATIONSHIP_PROPOSED_MARKUP);
           String buswhere = "( policy == \""+POLICY_PART_MARKUP+"\"";
           buswhere += "|| policy == \""+POLICY_EBOM_MARKUP+"\" )";
           buswhere += "&& "+DomainConstants.SELECT_REL_EBOMMARKUP_ID+"=="+strParentId;
           DomainObject changeObj = new DomainObject(strChangeId);
           StringList objectSelects  = new StringList(DomainConstants.SELECT_ID);
           //Relationships are selected by its Ids
           StringList relSelects     = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
           mlAffectedItemBusObjList = changeObj.getRelatedObjects(context,
                                               relPattern1.getPattern(), // relationship pattern
                                               TYPE_PART_MARKUP, //TYPE_PART_MARKUP, // object pattern
                                               objectSelects, // object selects
                                               relSelects, // relationship selects
                                               false, // to direction
                                               true, // from direction
                                               (short) 1, // recursion level
                                               null, // object where clause
                                               null); // relationship where clause

       return mlAffectedItemBusObjList;
       }//If the Parent Id is not that of an ECO , it should display only the Markups related to the Parent Object
       else{
		return getAffectedItemsWithRelSelectables (context, strParentId, strChangeId, sENCAffectedItemsTypeFilter, sENCAffectedItemsAssigneeFilter, sENCAffectedItemsRequestedChangeFilter);
       }
     }

   /* Get the list of all Objects which are connected to the context Change object as
   * "Affected Items" and Parts connected with relationship "Part Markups" to Markups
   * (Item and BOM)
   * @param context the eMatrix <code>Context</code> object
   * @param args    holds the following input arguments:
   *            0 - HashMap containing one String entry for key "objectId"
   * @return        a eMatrix <code>MapList</code> object having the list of Affected
   * Items for this Change object, and Parts connected with relationship "Part Markups"
   * to Markups (Item and BOM)
   * @throws        Exception if the operation fails
   * @since         Common X3
   **/
 public MapList getAffectedItemsWithRelSelectables(Context context, String strParentId, String strChangeId, String sENCAffectedItemsTypeFilter, String sENCAffectedItemsAssigneeFilter, String sENCAffectedItemsRequestedChangeFilter) throws Exception {

      //Initializing the return type
      MapList mlAffectedItemBusObjList = new MapList();
      //Business Objects are selected by its Ids
      StringList objectSelects  = new StringList(DomainConstants.SELECT_ID);
      objectSelects.addElement(DomainConstants.SELECT_TYPE);
      objectSelects.addElement(DomainConstants.SELECT_NAME);
      //Relationships are selected by its Ids
      StringList relSelects     = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
      relSelects.addElement(SELECT_ATTRIBUTE_REQUESTED_CHANGE);
      //retrieving Affected Items list from context Change object
		DomainObject changeObj = new DomainObject(strParentId);
		StringBuffer bufType = new StringBuffer(20);
	    String relPattern = RELATIONSHIP_AFFECTED_ITEM;
		if(sENCAffectedItemsTypeFilter.equalsIgnoreCase(SELECT_ALL))
		{
		      bufType.append(SYMB_WILD);
		}
		else
	        {
			bufType.append(sENCAffectedItemsTypeFilter);
		}

      	sGlobalAssigneeName=sENCAffectedItemsAssigneeFilter;
		sGlobalTypefilterValue=sENCAffectedItemsTypeFilter;
		sGlobalRequestedChangeFilter=sENCAffectedItemsRequestedChangeFilter;
		String sWhereClauseAssigneeFilter ="";
		String sWheresWhereClauseRequestedChangeFilter = "";
		sWheresWhereClauseRequestedChangeFilter = "attribute["+ATTRIBUTE_REQUESTED_CHANGE+"] == " + "\""+sENCAffectedItemsRequestedChangeFilter+"\"";
		sWhereClauseAssigneeFilter = "tomid.fromrel["+DomainConstants.RELATIONSHIP_ASSIGNED_EC+"].from.id  =='"+sENCAffectedItemsAssigneeFilter+"'";

		StringBuffer bufWhereClause = new StringBuffer(150);
		if((!SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsRequestedChangeFilter))&&(!SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsAssigneeFilter)))
		{
			bufWhereClause.append("((");
			bufWhereClause.append(sWhereClauseAssigneeFilter);
			bufWhereClause.append(") && (");
			bufWhereClause.append(sWheresWhereClauseRequestedChangeFilter);
			bufWhereClause.append("))");
		}
		else if((!SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsRequestedChangeFilter))&&(SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsAssigneeFilter)))
		{
			bufWhereClause.append(sWheresWhereClauseRequestedChangeFilter);
		}
		else if((SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsRequestedChangeFilter))&&(!SELECT_ALL.equalsIgnoreCase(sENCAffectedItemsAssigneeFilter)))
		{
			bufWhereClause.append(sWhereClauseAssigneeFilter);
		}
		else
		{
			bufWhereClause.append("");
		}

		String strAttrAffectedItemCategory = PropertyUtil.getSchemaProperty(context,"attribute_AffectedItemCategory");

      // 361410: display indirect as well as direct affected items when the requested change filter = For Release
      if (!RANGE_FOR_RELEASE.equals(sENCAffectedItemsRequestedChangeFilter))
          if (bufWhereClause.length()  == 0)
  		{
  			bufWhereClause.append("attribute[" + strAttrAffectedItemCategory + "] == Direct");
  		}
  		else
  		{
  			bufWhereClause.append(" && attribute[" + strAttrAffectedItemCategory + "] == Direct");
  		}

      String strType=changeObj.getInfo(context,DomainConstants.SELECT_TYPE);
      boolean bpart=mxType.isOfParentType(context,strType,DomainConstants.TYPE_PART);

      if (!bpart)

		{
      mlAffectedItemBusObjList = changeObj.getRelatedObjects(context,
												relPattern, // relationship pattern
												bufType.toString(), // object pattern
												objectSelects, // object selects
												relSelects, // relationship selects
												false, // to direction
												true, // from direction
												(short) 1, // recursion level
												null, // object where clause
												bufWhereClause.toString()); // relationship where clause
		}
		else
		{
			Pattern relPattern1 = new Pattern(RELATIONSHIP_APPLIED_MARKUP);
			relPattern1.addPattern(RELATIONSHIP_PROPOSED_MARKUP);
			String buswhere = "( policy == \""+POLICY_PART_MARKUP+"\"";
          buswhere += "|| policy == \""+POLICY_EBOM_MARKUP+"\" )";
			buswhere += "&& "+DomainConstants.SELECT_REL_EBOMMARKUP_ID+"=="+strParentId;
			changeObj.setId(strChangeId);
      	mlAffectedItemBusObjList = changeObj.getRelatedObjects(context,
												relPattern1.getPattern(), // relationship pattern
												"*", //TYPE_PART_MARKUP, // object pattern
												objectSelects, // object selects
												relSelects, // relationship selects
												false, // to direction
												true, // from direction
												(short) 1, // recursion level
												buswhere, // object where clause
												null); // relationship where clause
}
		return  mlAffectedItemBusObjList;
  }

   /* For a Selected Part, Gets the list of all Parts which can be connected as Affected
    * Items for the Change object
    * @param context the eMatrix <code>Context</code> object
    * @param args[] packed hashMap of request parameters
    * @return        a eMatrix <code>MapList</code> object having the list of
    * Part Id's which could be added as Affected Item
    * @throws        Exception if the operation fails
    * @since         R213
    **/
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getAddRelatedPartsWithRelSelectables (Context context, String[] args) throws Exception {
  	HashMap paramMap = (HashMap)JPO.unpackArgs(args);
  	String strChangeObjId = (String) paramMap.get("strChangeObjId");
  	String strParentId = (String) paramMap.get("objectId");

  	MapList mlBusRelatedPartsList = new MapList();
  	MapList mlBusChangeAIList = new MapList();
  	HashSet hsTemp = new HashSet();
  	MapList mlPartFinalList = new MapList();

	String SELECT_REL_FROM_EBOM_EXISTS = "from[" + DomainConstants.RELATIONSHIP_EBOM + "]"; //Added for IR-204955
  	String relapttern = DomainConstants.RELATIONSHIP_EBOM;
  	String typepattern = DomainConstants.TYPE_PART;
  	StringList selectStmts = new StringList();
  	selectStmts.addElement(DomainConstants.SELECT_ID);
  	selectStmts.addElement(DomainConstants.SELECT_NAME);
  	selectStmts.addElement(SELECT_REL_FROM_EBOM_EXISTS); //Added for IR-204955
  	StringList selectrelStmts = new StringList();
  	selectrelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
  	selectrelStmts.addElement("to["+ RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id");

  	String whereClause = "";
  	if(strChangeObjId != null && !"null".equals(strChangeObjId) && strChangeObjId.length() > 0) {
  		DomainObject objSCO =  DomainObject.newInstance(context,strChangeObjId);
  		String busid = objSCO.getInfo(context,"to["+ RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id");
  		if(busid != null && busid.length() > 0) {
  			whereClause = "(((to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == True ) && (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.id == " + busid + " )) || (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == False))";
  		}

		//Modified for RDO Convergence start
  		String strChgType = objSCO.getInfo(context, SELECT_TYPE);
  		String strECOOrg = objSCO.getAltOwner1(context).toString();
  		if(busid != null && busid.length() > 0) {
  			if(strChgType != null && !"".equals(strChgType) && objSCO.isKindOf(context, TYPE_ECO)) {
  				whereClause = "altowner1 == '" + strECOOrg+ "'";
  			} else {
  				whereClause = "(((to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == True ) && (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.id == " + busid + " )) || (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == False))";
  			}
  		}
  	//Modified for RDO Convergence End

  		//To retrieve related parts based on the Change type
  		String policyName = objSCO.getInfo(context,DomainConstants.SELECT_POLICY);
        String policyClassification = FrameworkUtil.getPolicyClassification(context, policyName);
        if(policyClassification != null && !"".equals(policyClassification) && "TeamCollaboration".equals(policyClassification)){
        	if ("".equals(whereClause))
  	  	  		whereClause = "(current=="+DomainConstants.STATE_DEVELOPMENT_PART_CREATE+" || current=="+DomainConstants.STATE_DEVELOPMENT_PART_COMPLETE+")";
  	  	  	else
  	  	  		whereClause += " && (current=="+DomainConstants.STATE_DEVELOPMENT_PART_CREATE+" || current=="+DomainConstants.STATE_DEVELOPMENT_PART_COMPLETE+")";
  		} else {
  			if ("".equals(whereClause))
  	  	  		whereClause = "(current=="+DomainConstants.STATE_PART_PRELIMINARY+" || current=="+DomainConstants.STATE_PART_RELEASE+")";
  	  	  	else
  	  	  		whereClause += " && (current=="+DomainConstants.STATE_PART_PRELIMINARY+" || current=="+DomainConstants.STATE_PART_RELEASE+")";
  		}
  	}

  	DomainObject domPartId = DomainObject.newInstance(context);
  	domPartId.setId(strParentId);

	//Added for IR-204955 start
	  	String sExpandLevels = paramMap.get("emxExpandFilter").toString();
	  	int nExpandLevel = 0;
	  	 if ("All".equalsIgnoreCase(sExpandLevels)) {
				nExpandLevel = 0;
			} else {
				nExpandLevel = Integer.parseInt(sExpandLevels);
			}

	  	//Added for IR-204955 End

  	// Get all Parts Related to Selected Part
  	mlBusRelatedPartsList = domPartId.getRelatedObjects(context,
  	                                              relapttern,
  	                                              typepattern,
  	                                              selectStmts,
  	                                              selectrelStmts,
  	                                              false,
  	                                              true,
  	                                              (short)nExpandLevel,  //Modified for IR-204955
  	                                              whereClause,
  	                                              null);

  	StringList selectStmts1	= new StringList();
  	selectStmts1.addElement(DomainConstants.SELECT_ID);
  	selectStmts1.addElement(DomainConstants.SELECT_NAME);
  	StringList selectrelStmts1 = new StringList();
  	selectrelStmts1.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

  	DomainObject domchangeObj=DomainObject.newInstance(context);
  	domchangeObj.setId(strChangeObjId);

  	//Get all Parts Related to Change as Affected Items
  	mlBusChangeAIList = domchangeObj.getRelatedObjects(context,
  													RELATIONSHIP_AFFECTED_ITEM,
  													typepattern,
  													selectStmts1,
  													selectrelStmts1,
  													false,
  													true,
  													(short) 1,
  													null,
  													null);

  	//Filter Parts all ready present in Affected Items List
  	if ((mlBusRelatedPartsList != null && mlBusRelatedPartsList.size() > 0)||(mlBusChangeAIList != null && mlBusChangeAIList.size() > 0)) {
  		if(mlBusChangeAIList != null && mlBusChangeAIList.size()>0) {
  			Iterator itrAI = mlBusChangeAIList.iterator();
  			while(itrAI.hasNext()) {
  				Map mapAIObj = (Map)itrAI.next();
  				String strChangeObjAIId = mapAIObj.get(DomainConstants.SELECT_ID).toString();
  				hsTemp.add(strChangeObjAIId);
  			}
  		}

  		if(mlBusRelatedPartsList != null && mlBusRelatedPartsList.size()>0) {
  			Iterator itrPart = mlBusRelatedPartsList.iterator();
  			while(itrPart.hasNext()) {
  				Map mapPart = (Map)itrPart.next();
  				mapPart.put("selection", "multiple");
  				String strPartId = mapPart.get(DomainConstants.SELECT_ID).toString();
  				if(!(hsTemp.contains(strPartId)))
  					mlPartFinalList.add(mapPart);
  			}
  		}
  	}

	 //Added for IR-204955 start
	  	Iterator itr = mlBusRelatedPartsList.iterator();
		Map newMap;
	  	if (nExpandLevel != 0) {
	        while (itr.hasNext()) {
	      	  newMap = (Map) itr.next();

	      	  // To display  + or - in the bom display
	      	  newMap.put("hasChildren", (String) newMap.get(SELECT_REL_FROM_EBOM_EXISTS));
	        }
	    }
	  //Added for IR-204955 End

  	return mlPartFinalList;
  }


   /* For the Selected Parts, Gets the list of all Specifications which can be connected as Affected
    * Items for the Change object
    * @param context the eMatrix <code>Context</code> object
    * @param args[] packed hashMap of request parameters
    * @return        a eMatrix <code>MapList</code> object having the list of
    * Specification Id's which could be added as Affected Item
    * @throws        Exception if the operation fails
    * @since         R213
    **/
   @com.matrixone.apps.framework.ui.ProgramCallable
   public MapList getAddRelatedSpecificationsWithRelSelectables (Context context, String[] args) throws Exception {
  		HashMap paramMap = (HashMap)JPO.unpackArgs(args);
  		String strChangeObjId = (String) paramMap.get("strChangeObjId");
  		String sbSelectedRows = paramMap.get("sbSelectedRows").toString();
  		ArrayList arrSpecList = new ArrayList();
  		MapList mlBusRelatedSpecsList = new MapList();
  		MapList mlBusChangeAIList = new MapList();
  		HashSet hsTemp = new HashSet();
  		MapList mlSpecsFinalList = new MapList();

  		String relapttern = DomainConstants.RELATIONSHIP_PART_SPECIFICATION;
  		String typepattern = DomainConstants.TYPE_CAD_DRAWING+","+DomainConstants.TYPE_CAD_MODEL+","+DomainConstants.TYPE_DRAWINGPRINT;
  		StringList selectStmts = new StringList();
  		selectStmts.addElement(DomainConstants.SELECT_ID);
  		selectStmts.addElement(DomainConstants.SELECT_NAME);
  		StringList selectrelStmts = new StringList();
  		selectrelStmts.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);
  		selectrelStmts.addElement("to["+ RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id");

  		String whereClause = "";
  		if(strChangeObjId != null && !"null".equals(strChangeObjId) && strChangeObjId.length() > 0) {
  			DomainObject objSCO =  DomainObject.newInstance(context,strChangeObjId);
  			String busid = objSCO.getInfo(context,"to["+ RELATIONSHIP_DESIGN_RESPONSIBILITY + "].from.id");
  			if(busid != null && busid.length() > 0) {
  				whereClause = "(((to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == True ) && (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.id == " + busid + " )) || (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == False))";
  			}

		//Modified for RDO Convergence start
  	  		String strChgType = objSCO.getInfo(context, SELECT_TYPE);
  	  		String strECOOrg = objSCO.getAltOwner1(context).toString();
  	  		if(busid != null && busid.length() > 0) {
  	  			if(strChgType != null && !"".equals(strChgType) && objSCO.isKindOf(context, TYPE_ECO)) {
  	  				whereClause = "altowner1 == '" + strECOOrg+ "'";
  	  			} else {
  	  			whereClause = "(((to[" + RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == True ) && (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"].from.id == " + busid + " )) || (to["+DomainConstants.RELATIONSHIP_DESIGN_RESPONSIBILITY+"] == False))";
  	  			}
  	  		}
  	  	//Modified for RDO Convergence End
  		}

  		if ("".equals(whereClause))
  			whereClause = "((current=="+DomainConstants.STATE_CADDRAWING_PRELIMINARY+" || current=="+DomainConstants.STATE_CADDRAWING_RELEASE+")" + " || (current=="+DomainConstants.STATE_CADMODEL_PRELIMINARY+" || current=="+DomainConstants.STATE_CADMODEL_RELEASE+")" + " || (current=="+DomainConstants.STATE_DRAWINGPRINT_PRELIMINARY+" || current=="+DomainConstants.STATE_DRAWINGPRINT_RELEASE+"))" ;
  		else
  			whereClause += " &&((current=="+DomainConstants.STATE_CADDRAWING_PRELIMINARY+" || current=="+DomainConstants.STATE_CADDRAWING_RELEASE+")" + " || (current=="+DomainConstants.STATE_CADMODEL_PRELIMINARY+" || current=="+DomainConstants.STATE_CADMODEL_RELEASE+")" + " || (current=="+DomainConstants.STATE_DRAWINGPRINT_PRELIMINARY+" || current=="+DomainConstants.STATE_DRAWINGPRINT_RELEASE+"))" ;

  		StringTokenizer stPartIds = new StringTokenizer(sbSelectedRows,",");
  		while(stPartIds.hasMoreTokens()){
  			String partId = stPartIds.nextToken();
  			DomainObject domPartObj = DomainObject.newInstance(context);
  			domPartObj.setId(partId);

  			// Get all Specifications Related to Selected Part
  			MapList mlBusRelatedSpecsTempList = domPartObj.getRelatedObjects(context,
  			                                              relapttern,
  			                                              typepattern,
  			                                              selectStmts,
  			                                              selectrelStmts,
  			                                              false,
  			                                              true,
  			                                              (short)1,
  			                                              whereClause,
  			                                              null);

  			Iterator itrSpec = mlBusRelatedSpecsTempList.iterator();
  			while(itrSpec.hasNext()){
  				mlBusRelatedSpecsList.add(itrSpec.next());
  			}
  		}

  		StringList selectStmts1	= new StringList();
  		selectStmts1.addElement(DomainConstants.SELECT_ID);
  		selectStmts1.addElement(DomainConstants.SELECT_NAME);
  		StringList selectrelStmts1 = new StringList();
  		selectrelStmts1.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

  		DomainObject domchangeObj = DomainObject.newInstance(context);
  		domchangeObj.setId(strChangeObjId);

  		//Get all Specifications Related to Change as Affected Items
  		mlBusChangeAIList = domchangeObj.getRelatedObjects(context,
  														RELATIONSHIP_AFFECTED_ITEM,
  														typepattern,
  														selectStmts1,
  														selectrelStmts1,
  														false,
  														true,
  														(short) 1,
  														null,
  														null);

  		//Filter Parts all ready present in Affected Items List
  		if ((mlBusRelatedSpecsList != null && mlBusRelatedSpecsList.size() > 0) || (mlBusChangeAIList != null && mlBusChangeAIList.size() > 0)) {
  			if(mlBusChangeAIList != null && mlBusChangeAIList.size()>0) {
  				Iterator itrAI = mlBusChangeAIList.iterator();
  				while(itrAI.hasNext()) {
  					Map mapAIObj = (Map)itrAI.next();
  					String strChangeObjAIId = mapAIObj.get(DomainConstants.SELECT_ID).toString();
  					hsTemp.add(strChangeObjAIId);
  				}
  			}

  			if(mlBusRelatedSpecsList != null && mlBusRelatedSpecsList.size()>0) {
  				Iterator itrPart = mlBusRelatedSpecsList.iterator();
  				while(itrPart.hasNext()) {
  					Map mapPart = (Map)itrPart.next();
  					mapPart.put("selection", "multiple");
  					String strPartId = mapPart.get(DomainConstants.SELECT_ID).toString();

  					if(!(hsTemp.contains(strPartId))) {
  						if(!arrSpecList.contains(strPartId)) {
  	 						arrSpecList.add(strPartId);
  	 						mlSpecsFinalList.add(mapPart);
  	 					}
  					}
  				}
  			}
  		}
  		return mlSpecsFinalList;
   }


   /* Get the related parts for the Selected part specifications
    * @param context the eMatrix <code>Context</code> object
    * @param args[] packed hashMap of request parameters
    * @return        a eMatrix <code>MapList</code> object having the list of
    * Specification Id's which could be added as Affected Item
    * @throws        Exception if the operation fails
    * @since         R213
    **/
   public StringList getSpecificationRelatedParts(Context context,String[] args) throws Exception
   {
   	StringList relatedPartsList = new StringList();
   	HashMap programMap = (HashMap) JPO.unpackArgs(args);
   	HashMap paramList=(HashMap)programMap.get("paramList");
   	String sbSelectedRows = paramList.get("sbSelectedRows").toString();
   	MapList objectList = (MapList)programMap.get("objectList");

   	for(int i=0; i<objectList.size();i++) {
   		Map map = (Map)objectList.get(i);
   		String strId = map.get("id").toString();

   		DomainObject domchangeObj=DomainObject.newInstance(context);
   		   domchangeObj.setId(strId);
   		   String typepattern=DomainConstants.TYPE_PART;
   		   StringList selectStmts1	=	new StringList();
   		   selectStmts1.addElement(DomainConstants.SELECT_ID);
   		   selectStmts1.addElement(DomainConstants.SELECT_NAME);
   		   StringList selectrelStmts1	=	new StringList();
   		   selectrelStmts1.addElement(DomainConstants.SELECT_RELATIONSHIP_ID);

   		  MapList relatedPartList = domchangeObj.getRelatedObjects(context,
   					RELATIONSHIP_PART_SPECIFICATION,
   					typepattern,
   					selectStmts1,
   					selectrelStmts1,
   					true,
   					false,
   					(short) 1,
   					null,
   					null);

   		  	 String strRelatedPart = "";
		     for(int j=0;j<relatedPartList.size();j++) {
		  		Map mPartDetails = (Map)relatedPartList.get(j);
		  		String strPartId = mPartDetails.get("id").toString();
		  		String strPartName = mPartDetails.get("name").toString();

		  		if(sbSelectedRows.contains(strPartId)) {
		  			if("".equals(strRelatedPart)) {
		  				strRelatedPart = strPartName;
		  			} else {
		  				strRelatedPart += ", " + strPartName ;
		  			}
		  		}
		  	}
		     relatedPartsList.addElement(strRelatedPart);
   		}
   		return relatedPartsList;
   }

  /** This method is called for creating ECO object
   *
   * @param context
   * @param args
   * @return <code>Map</code> of object Ids of created ECOs.
   * @throws Exception
   */
   @com.matrixone.apps.framework.ui.CreateProcessCallable
   public Map createECO(Context context, String[] args) throws Exception{
			HashMap programMap = (HashMap) JPO.unpackArgs(args);

			String sType = (String) programMap.get("TypeActual");
			String sPolicy = (String) programMap.get("Policy");
			String sVault = (String) programMap.get("Vault");

			Map returnMap = new HashMap();
			boolean bAutoName = true;

			try {
				ECO eco = (ECO) DomainObject.newInstance(context,
						DomainConstants.TYPE_ECO, DomainConstants.ENGINEERING);

				String strPartId = eco.createECO(context, sType, sVault, sPolicy, bAutoName);
				returnMap.put("id", strPartId);

			} catch (Exception e) {
				e.printStackTrace();
				throw new FrameworkException(e);
			}

			return returnMap;
	}

   /** This method is called on ECO edit post process action
    *
    * @param context
    * @param args
    * @return
    * @throws Exception
    */
   @com.matrixone.apps.framework.ui.PostProcessCallable
   public HashMap editECOPostProcess(Context context, String[] args) throws Exception
   {
	   HashMap resultMap   = new HashMap();

	   try{
		   String[] app = {"ENO_ENG_TP"};
		   ComponentsUtil.checkLicenseReserved(context,app); //License Check
	   }
	   catch(Exception e){
		   resultMap.put("Action", "error");
		   resultMap.put("Message", e.getMessage());
	   }

	   return resultMap;
   }

   /**
    * Access program for 3DLive Examine channel in affected items page
    * @param context
    * @param args
    * @return
    * @throws Exception
    */
	public Boolean showAffectedItem3DChannel(Context context, String[] args) throws Exception {

        if(!EngineeringUtil.checkForDECorVPMInstallation(context)) {
            return false;
        }

        String pref3DLive = PropertyUtil.getAdminProperty(context, "Person", context.getUser(), "preference_3DLiveExamineToggle");
    	boolean flag = "Show".equals(pref3DLive);

    	if(flag) {
    		return EngineeringUtil.isReportedAgainstItemPart(context, args);
    	}

	    return flag;
	}
}
