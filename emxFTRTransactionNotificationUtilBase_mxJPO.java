/*
 ** emxPLCTransactionNotificationUtilBase.java
 **
 ** Copyright (c) 1992-2016 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of MatrixOne, Inc.
 ** Copyright notice is precautionary only and does not evidence any actual or intended
 ** publication of such program.
 **
 */

import matrix.db.Context;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.util.BackgroundProcess;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

/**
 * The <code>emxPLCTransactionNotificationUtilBase</code> class contains common transaction notification utility methods for PLC
 * @version Variant Configuration R207 - Copyright (c) 2008-2016 Dassault Systemes.
 * @since PLC R207
 */

public class emxFTRTransactionNotificationUtilBase_mxJPO extends emxPLCTransactionNotificationUtil_mxJPO
{

   /**
    * Constructor.
    *
    * @param context the eMatrix <code>Context</code> object
    * @param args holds no arguments
    * @throws Exception if the operation fails
    */
   public emxFTRTransactionNotificationUtilBase_mxJPO (Context context, String[] args) throws Exception
   {
      super(context, args);
   }

   public int transactionNotifications(Context context, String[] args) throws Exception
   {
       int result = 0;
       String transHistories = args[0];

       if(transHistories != null && !"".equals(transHistories))
       {
           try
           {
               ContextUtil.pushContext(context);
               Context frameContext = context.getFrameContext("emxFTRTransactionNotificationUtilBase");
               BackgroundProcess backgroundProcess = new BackgroundProcess();
               backgroundProcess.submitJob(frameContext, "emxFTRTransactionNotificationUtilBase", "notifyInBackground", args , (String)null);
           } catch(Exception ex)
           {
               ContextUtil.abortTransaction(context);               
               ex.printStackTrace();
               throw ex;
           }finally{
               //Set the context back to the context user
               ContextUtil.popContext(context);
          }
       }
       return result;
   }

}
