/*
 ** emxTransactionNotificationUtilExtension
 **
 ** Copyright (c) 1999-2008 Dassault Systemes.
 **
 ** All Rights Reserved.
 ** This program contains proprietary and trade secret information of
 ** MatrixOne, Inc.  Copyright notice is precautionary only and does
 ** not evidence any actual or intended publication of such program.
 **
 ** static const char RCSID[] = $Id: emxTransactionNotificationUtilExtension.java.rca  DAR/PFI 18/11/2008 $
 ** DAR/Mohan: 18/11/2008: Multithreaded trigger launching
 ** DAR / KPT : 09/10/2009 : Support Ligth Save + change name 
 ** DAR/FRM : 02/01/2012 : support MINMAJ majorrevisioned event
 */

import java.io.*;
import java.util.*;
import java.text.*;
import matrix.db.*;
import matrix.util.*;
import com.matrixone.apps.domain.*;
import com.matrixone.apps.domain.util.*;
import com.matrixone.util.MxXMLUtils;
import com.matrixone.apps.common.util.SubscriptionUtil;
import com.matrixone.apps.framework.ui.UIMenu;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.domain.util.i18nNow;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import matrix.db.Context;
import java.lang.String;
// compatibilite J#
//import com.matrixone.jsystem.util.StringUtils;


/**
 * The <code>emxIssueBase</code> class contains methods related to Issue Management.
 * @version  - V6R2009_HF0 Copyright (c) 2008, MatrixOne, Inc.
 */

//public class ${CLASSNAME} extends ${CLASS:${CLASS:emxTransactionNotificationUtilBase}}
public class emxTransactionNotificationUtilExtension_mxJPO extends emxTransactionNotificationUtilBase_mxJPO
{

   //// static String TRANS_HISTORY_STATE_DELIMITER = "state:";


    /**
     * Create a new emxIssueBase object.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @return a emxIssueBase object.
     * @throws Exception if the operation fails.
     * @since V6R2009_HF0
     */
    public emxTransactionNotificationUtilExtension_mxJPO(Context context, String[] args) throws Exception {
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @since V6R2009_HF0
     */
    public int mxMain(Context context, String[] args) throws Exception {
        if (true) {
            throw new Exception(
                    "must specify method on emxNotificationUtil invocation");
        }
        return 0;
    }

  /**
     * methode surchargeant celle du JPO de Mohan, delivree par lui.  seul une modif afin d'appeler notre loadEvents en mode background
	 ** backgroundProcess.submitJob(frameContext, "emxTransactionNotificationUtil",  ..........
	 ** devient
	 ** backgroundProcess.submitJob(frameContext, "emxTransactionNotificationUtilExtension", ..........
     * Method to send the notifications to user based on history actions parsed
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the macro TRANSHISTORY from transaction trigger
     * @return integer denoting success or failure of notification sent .
     * @throws Exception if the operation fails
     * @since R207
     **/
/*     public int transactionNotifications(Context context, String[] args) throws Exception
    {
        int result = 0;
        String transHistories = args[0];

        if(transHistories != null && !"".equals(transHistories))
        {
            try
            {
                Context frameContext = context.getFrameContext("emxTransactionNotificationUtil");
                //
                // Fix for bug #368217 :
                matrix.db.MQLCommand mql = new matrix.db.MQLCommand();
                mql.executeCommand(frameContext, "get env foo");
                //
                BackgroundProcess backgroundProcess = new BackgroundProcess();
                backgroundProcess.submitJob(frameContext, "emxTransactionNotificationUtilExtension", "notifyInBackground", args , (String)null);
            } catch(Exception ex)
            {
                ContextUtil.abortTransaction(context);
                ex.printStackTrace();
                throw ex;
            }
        }
        return result;
    } */
 
  /**
     * dar: method surchargeant celle de Mohan dans le JPO  emxTransactionNotificationUtilBase.java
     * Method to filter the list of events subscribed by user for notification
     * User will write overridden method in the Non-Base custom JPO.
     * @param context the eMatrix <code>Context</code> object
     * @param strlNotificationCommandName contains list of eventcommands subscribed for notification.
     * @return StringList contains the list of filtered events for notification.
     * @throws Exception if the operation fails
     * @since X+3
    */
/*       public StringList loadEvents (Context context, StringList strlNotificationCommandName ){
	    StringList returnList = new StringList();
		StringList localstrlNotificationCommandName = strlNotificationCommandName ;
		//System.err.println("DARDAR:loadEvents: localstrlNotificationCommandName.size() =" + localstrlNotificationCommandName.size() );
		//////////////////////// dar 21/12/2012
		try
		{	
			String currentUserName = PropertyUtil.getGlobalRPEValue(context, "USER") ;
			// R213 sometimes USER equals "User Agent" !!!
			if( currentUserName == null || "User Agent".equals(currentUserName) || "".equals(currentUserName) || "null".equals(currentUserName) )
				currentUserName = context.getUser();
				//System.err.println("DARDAR: super rustine R213 currentUserName=>"  + currentUserName + "<=");
			String rpeUserName = PropertyUtil.getGlobalRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME);
				//System.err.println("DARDAR: loadEvents MX_LOGGED_IN_USER_NAME=>"  + rpeUserName + "<=");
			if( rpeUserName == null || "User Agent".equals(rpeUserName) || "".equals(rpeUserName) || "null".equals(rpeUserName) )
			{
				PropertyUtil.setGlobalRPEValue(context, ContextUtil.MX_LOGGED_IN_USER_NAME, "\""+currentUserName+"\"");
				 //System.err.println("DARDAR: setGlobalRPEValue  MX_LOGGED_IN_USER_NAME=>"  + currentUserName + "<=");
			}
			//else
			//	System.err.println("DARDAR: loadEvents MX_LOGGED_IN_USER_NAME NOT EMPTY ");
			
		}
        catch(Exception ex)
        {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage() );
        }
		//////////////////////// dar dar 21/12/2012

		
		
		Iterator firstItr = localstrlNotificationCommandName.iterator(); 
		Iterator ListItr = localstrlNotificationCommandName.iterator(); 
		
		boolean isCreate = false;
		boolean notModEventExist = false; 
		boolean modEventExist = false; 
		boolean deleteFile = false;
		boolean renameFile = false;
		boolean addInterface = false;  // add  interface
		boolean removeInterface = false;  //  remove interface
		String modifyEventString = "" ;
		String addInterfaceEventString = "" ;
		String removeInterfaceEventString = "" ;
		
		 while(firstItr.hasNext() )
		 {
			String eventString = (String )firstItr.next();
			boolean isModifiedEvent = eventString.indexOf("modify_") >=0 ;
			//System.err.println("DARDAR: loadEvents eventString=" + eventString );
			if ( eventString.indexOf("checkin_") >=0 ||eventString.indexOf("checkout_") >=0 ) continue ; // on passe les ckeckin checkout car non supportes pour l instant.
			if ( isModifiedEvent == false  ) notModEventExist = true;  // il y a autre chose que des modify
			if ( isModifiedEvent == true ) // il y a au moins un modify 
				{
					modEventExist = true;     
					modifyEventString = eventString ;
				}
			if ( eventString.indexOf("add interface_") >=0  )     // il y a au moins un modify car ajout interface_/delete interface_
				{
					addInterface = true;      
					addInterfaceEventString = eventString ;
				}
			if ( eventString.indexOf("remove interface_") >=0  )     // il y a au moins un modify car ajout interface_/delete interface_
				{
					removeInterface = true;      
					removeInterfaceEventString = eventString ;
				}
			if ( eventString.indexOf("delete file_") >=0 ) deleteFile = true;   // il y a au moins un delete de stream
			if ( eventString.indexOf("rename file_") >=0 ) renameFile = true;   // il y a au moins un rename de stream
			if ( eventString.indexOf("create_") >=0 ){	returnList.add(eventString);return returnList ;} // return create
		 }

		 // il n'y a que des modify: on en retourne un et c tout
		 if ( notModEventExist == false && modEventExist == true ) 
		 {
			returnList.add((String )ListItr.next() );
			//System.err.println("DARDAR: loadEvents que des modify: on en retourne un et c tout" );
			return returnList; 
		 }
		 
		 // si un add ou remove interface seuls on en fait un modify
		 if ( modifyEventString.length() <= 0 && ( addInterface == true || removeInterface == true )   )
		 {
			 if (addInterfaceEventString.length() > 0 )
			 {
				modifyEventString = addInterfaceEventString.replaceFirst("add interface_", "modify_") ;
			 }
			 else  if (removeInterfaceEventString.length() > 0 )
			 {
				modifyEventString = removeInterfaceEventString.replaceFirst("remove interface_", "modify_") ;
			 }
			returnList.add(modifyEventString);
			//System.err.println("DARDAR: loadEvents  add/remove interface seul: on en retourne un modify a partir du add/remove interface" );
			return returnList; 
			 
		 }
		 // si au moins un modify_ et un add  ou  remove interface_ on retourne le modify_
		 /////if ( armInterface == true && modEventExist == true ) 
		 if ( modifyEventString.length() > 0 && ( addInterface == true || removeInterface == true )   )
		 {
			returnList.add(modifyEventString);
			//System.err.println("DARDAR: loadEvents  modify + add/remove interface: on en retourne un modify et c tout" );
			return returnList; 
		 }
		 
		 if( localstrlNotificationCommandName.size() == 1 ) return returnList = strlNotificationCommandName ;
		 
		 // il n'y a que des check_in check_out : on retourne la liste d'entree
		 if ( notModEventExist == false && modEventExist == false ) return returnList = strlNotificationCommandName ;

		while(ListItr.hasNext() )
		{
			String eventString = (String )ListItr.next();
			//boolean isModifiedEvent = eventString.indexOf("modify_") >=0 || eventString.indexOf("add interface_") >=0 || eventString.indexOf("remove interface_") >=0  ;
			boolean isModifiedEvent = eventString.indexOf("modify_") >=0  ;
			//System.err.println("DARDAR: loadEvents while(ListItr.hasNext() )  eventString=>" + eventString + "<<=" );
			if ( eventString.indexOf("checkin_") >=0 ||eventString.indexOf("checkout_") >=0 ) continue ; // on passe les ckeckin checkout car non supportes pour l'instant.
			if ( (eventString.indexOf("CESTAM_modify") >=0) || (eventString.indexOf("CESTAMP_modify") >=0) ) continue;
			else if ( isModifiedEvent == true && deleteFile == true && renameFile == true ) 
				{
					returnList.add(eventString); // modif de stream on retourne un modify seulement
					return returnList;
				}
			else if ( isModifiedEvent == true  ) continue;
			else if ( eventString.indexOf("change owner_") >=0  ) { returnList.add(eventString);return returnList;}			
			else if ( eventString.indexOf("change altowner1_") >=0) //TODO : remove?
				{ 
					String tmpStr = eventString.replaceFirst("change altowner1_", "change owner_") ;
					//System.err.println("DARDAR: tmpStr 1=>" + tmpStr + "<<=" );
				   returnList.add(tmpStr);
				   return returnList;
				}
			else if ( eventString.indexOf("change altowner2_") >=0) //TODO : remove?
			    { 
					String tmpStr = eventString.replaceFirst("change altowner2_", "change owner_") ;
					//System.err.println("DARDAR: tmpStr 2=>" + tmpStr + "<<=" );
				   returnList.add(tmpStr);
				   return returnList;
				}
			else if ( eventString.indexOf("change organization_") >=0) 
				{ 
					String tmpStr = eventString.replaceFirst("change organization_", "change owner_") ;
					//System.err.println("DARDAR: tmpStr 1=>" + tmpStr + "<<=" );
				   returnList.add(tmpStr);
				   return returnList;
				}
			else if ( eventString.indexOf("change project_") >=0) 
			    { 
					String tmpStr = eventString.replaceFirst("change project_", "change owner_") ;
					//System.err.println("DARDAR: tmpStr 2=>" + tmpStr + "<<=" );
				   returnList.add(tmpStr);
				   return returnList;
				}
			else if ( eventString.indexOf("majorrevisioned_") >=0)	{returnList.add(eventString);return returnList;} // major revision
			else if ( eventString.indexOf("revisioned_") >=0)	{returnList.add(eventString);return returnList;} // minor revision
			else if ( eventString.indexOf("promote_") >=0)		{returnList.add(eventString);return returnList;}
			else if ( eventString.indexOf("delete_") >=0)		{returnList.add(eventString);return returnList;}
			else if ( eventString.indexOf("change name") >=0)	{returnList.add(eventString);return returnList;}
		}	
		return returnList ;
     } */



}// End Class

