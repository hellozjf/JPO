/*
**  emxPolicyUtilBase
**
**  Copyright (c) 1992-2016 Dassault Systemes.
**  All Rights Reserved.
**  This program contains proprietary and trade secret information of MatrixOne,
**  Inc.  Copyright notice is precautionary only
**  and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.*;
import java.util.*;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;


/**
 * The <code>emxPolicyUtilBase</code> class contains policy utility methods.
 *
 * @version EC 10.0.0.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxPolicyUtilBase_mxJPO extends emxDomainObject_mxJPO
{

   /** Comparision operator Less than.*/
    public static final int LT = 0;
   /** Comparision operator Greater than.*/
    public static final int GT = 1;
   /** Comparision operator Equalto.*/
    public static final int EQ = 2;
   /** Comparision operator Less than Equalto.*/
    public static final int LE = 3;
   /** Comparision operator Greater than Equalto.*/
    public static final int GE = 4;
   /** Comparision operator NotEqual.*/
    public static final int NE = 5;

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments
     *        0 - String which holds the id of the business object
     * @throws Exception if the operation fails
     * @since EC 10.0.0.0
     */

    public emxPolicyUtilBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an int 0, status code
     * @throws Exception if the operation fails
     * @since EC 10.0.0.0
     */

    public int mxMain(Context context, String[] args)
        throws Exception
    {
        if (true)
        {
            String languageStr = context.getSession().getLanguage();
            String exMsg = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Message.Invocation", new Locale(languageStr));            
            exMsg += EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.Policy", new Locale(languageStr));            
            throw new Exception(exMsg);
        }
        return 0;
    }

    /**
     * This method check the current state of the object with the target state, using the comparison operator and returns the result.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *    0 - String containing object id whose state to be checked
     *    1 - String containing target state against which the current state of the object is compared
     *    2 - int containing Comparision operator used for comparison LT, GT, EQ, LE, GE, NE
     * @return an integer:
     *      0 if object state logic satisfies Comparison Operator.
     *      1 if object state logic didn't satisfies Comparison Operator.
     *      2 if a program error is encountered
     *      3 if state in state argument does not exist in the policy
     *      4 if an invalid comparison operator is passed in
     * @throws Exception if the operation fails.
     * @since EC 10.0.0.0
     */

    public int checkState(Context context, String[] args)
       throws Exception
    {
        try
        {
            String id = args[0];
            String targetState = args[1];
            int comparisonOperator = Integer.parseInt(args[2]);
            String sResult = "";
            if(!UIUtil.isNullOrEmpty(id)){
              sResult = MqlUtil.mqlCommand(context, "print bus $1 select $2 $3 dump $4",id,"current","state","|");
            }
            
            StringTokenizer tokens = new StringTokenizer(sResult, "|");
            // get the index of target state
            int targetIndex = sResult.lastIndexOf(targetState);
            // get the index of current state
            int stateIndex  = sResult.lastIndexOf(tokens.nextToken());

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
            throw ex;
        }

    }

}
