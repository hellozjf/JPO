/*   emxClassificationTransactionNotificationBase.java
**
**   Copyright (c) 2003-2016 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
**   This JPO contains the implementation of emxClassificationTransactionNotification
**
*/

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.util.BackgroundProcess;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

/**
 * The <code>emxClassificationTransactionNotificationBase</code> class contains implementation code for emxClassificationTransactionNotification.
 *
 * @version Common 10.6 - Copyright (c) 2003, MatrixOne, Inc.
 */
public class emxClassificationTransactionNotificationBase_mxJPO extends emxTransactionNotificationUtil_mxJPO
{
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since V6R2012x
     */
    public emxClassificationTransactionNotificationBase_mxJPO (Context context, String[] args)
        throws Exception
    {
        super(context, args);
    }

    /**
     * Method to send the notifications to user based on history actions parsed
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the macro TRANSHISTORY from transaction trigger
     * @return integer denoting success or failure of notification sent .
     * @throws Exception if the operation fails
     * @since V6R2012x
     **/
    public int transactionNotifications(Context context, String[] args) throws Exception
    {
        int result = 0;
        String transHistories = args[0];

        if(transHistories != null && !"".equals(transHistories)) {
            try {
                Context frameContext = context.getFrameContext("emxClassificationTransactionNotificationBase");
                BackgroundProcess backgroundProcess = new BackgroundProcess();
                backgroundProcess.submitJob(frameContext, "emxClassificationTransactionNotificationBase", "notifyInBackground", args , (String)null);
            } catch(Exception ex) {
                ContextUtil.abortTransaction(context);
                ex.printStackTrace();
                throw ex;
            }
        }
        return result;
    }

    /**
     * Overridden Method to parse the History Events string to return stringlist of events with | separated
     * 
     * This method is overridden to parse the promote/demote events with state (e.g promote to Active_)
     * 
     * @param strEventsHistory is a String containing the list of events occured on a particular object
     * @return StringList containing list of event names,time stamp with | separated
     * @throws Exception if the operation fails
     * @since V6R2012x
     **/

    public StringList parseEvents(Context context,String strEventsHistory) throws Exception
    {
        StringList events   = new StringList();
        String strTemp      = "";
        int iStartEvent     = 0;
        String strUser      = "";
        String strTime      = "";
        boolean isAMPM      = false;
        int noOfColuns      = 2;
        String dateFormat   = matrix.db.Environment.getValue(context,"MX_NORMAL_DATETIME_FORMAT");
        if( dateFormat == null || "".equals(dateFormat) || "null".equals(dateFormat) )  {
            dateFormat = super.DEFAULT_DATE_FORMAT;
        }
        if( dateFormat.indexOf("mer") >= 0 ) {
            isAMPM = true;
        }

        // find out how many columns are present in the date format
        int colunIndex      = dateFormat.indexOf(":");
        if( colunIndex == -1 ) {
            noOfColuns = 0;
        } else if( colunIndex == dateFormat.lastIndexOf(":")) {
            noOfColuns = 1;
        }
        int iEventIndex = strEventsHistory.indexOf(super.TRANS_HISTORY_EVENT_DELIMITER);
        while(iEventIndex != -1) {
            strTemp                 = strEventsHistory.substring(0,iEventIndex).trim();
            StringBuffer strEvent   = new StringBuffer();
            iStartEvent             = strTemp.lastIndexOf("\n");
            strEvent.append(strEventsHistory.substring(iStartEvent+1,iEventIndex).trim());
            if (strEvent.toString().equals("promote") || strEvent.toString().equals("demote")) {
                strEvent.append(" to ");
                strEvent.append(strEventsHistory.substring(strEventsHistory.lastIndexOf(':')+1,strEventsHistory.length()).trim());
            }
            
            strEventsHistory        = strEventsHistory.substring(iEventIndex+7);
            int itimeIndex          = strEventsHistory.indexOf("time:");
            if(itimeIndex != -1) {

                //User value being set as a Global RPE variable
                if(strUser.equals(""))
                {
                    strUser = strEventsHistory.substring(0,itimeIndex).trim();
                    PropertyUtil.setGlobalRPEValue(context,"USER","\""+strUser+"\"");
                }

                // Finding the string between the time: and the next token:
                colunIndex      = itimeIndex + 4;
                int tempIndex   = colunIndex;
                strTime         = strEventsHistory;
                for (int i = 0; i <= noOfColuns; i++ ) {
                    strTime = strTime.substring(tempIndex+1,strTime.length());
                    tempIndex = strTime.indexOf(":");
                    colunIndex += (tempIndex+1);
                    if( i == noOfColuns ) {
                        // This string should return Eg : 7/1/2008 4:36:51 PM  state
                        strTime = strEventsHistory.substring(itimeIndex+5,colunIndex);
                    }
                }

                int iTimeEndIndex = -1;
                if ( isAMPM ) {
                    iTimeEndIndex = strTime.indexOf(super.TRANS_HISTORY_TIME_DELIMITER1)==-1? strTime.indexOf(super.TRANS_HISTORY_TIME_DELIMITER2) : strTime.indexOf(super.TRANS_HISTORY_TIME_DELIMITER1);
                }
                //Validating if AM / PM exists along with time stamp. if not then take the time from the last indexof :
                if(iTimeEndIndex == -1)
                    iTimeEndIndex = strTime.lastIndexOf(":");
                else
                    iTimeEndIndex = iTimeEndIndex -1 ;

                String strTimeStamp = strTime.substring(0,iTimeEndIndex+3).trim();
                strEvent.append("_").append(strTimeStamp);
                events.add(strEvent.toString());    
            }else {
                events.add(strEvent.toString());    
            }

            iEventIndex = strEventsHistory.indexOf(super.TRANS_HISTORY_EVENT_DELIMITER);
        }
        return events;
    }

   
    /**
     * Overridden Method to filter the list of events subscribed by user for notification
     * 
     * @param context the eMatrix <code>Context</code> object
     * @param strlNotificationCommandName contains list of eventcommands subscribed for notification.
     * @return StringList contains the list of filtered events for notification.
     * @throws Exception if the operation fails
     * @since V6R2012x
    */
    public StringList loadEvents (Context context, StringList strlNotificationCommandName ){
        StringList slNotificationEvents = new StringList();
        String strEventHistory          = "";
        for (int i = 0; i < strlNotificationCommandName.size(); i++) {
            strEventHistory = (String) strlNotificationCommandName.get(i);
            if (strEventHistory.indexOf("modify_") >= 0 ||
                strEventHistory.indexOf("revisioned_")  >= 0 ||
                strEventHistory.indexOf("lock_")  >= 0 ||
                strEventHistory.indexOf("promote to")  >= 0 ||
                strEventHistory.indexOf("demote to")  >= 0) 
            {
                slNotificationEvents.add(strEventHistory);
            } else if (strEventHistory.indexOf("change name_")  >= 0) {
                slNotificationEvents.add(strEventHistory.replaceFirst("change name_","modify_"));
            } else if (strEventHistory.indexOf("change owner_")  >= 0) {
                slNotificationEvents.add(strEventHistory.replaceFirst("change owner_","modify_"));
            } else if (strEventHistory.indexOf("change policy_")  >= 0) {
                slNotificationEvents.add(strEventHistory.replaceFirst("change policy_","modify_"));
            }
        }
        return slNotificationEvents;
    }




}// End Class
