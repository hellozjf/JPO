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
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrderUI;

/**
 * The <code>enoECMActionLinkAccessBase</code> class contains implementation code for emxENCActionLinkAccess.
 * @version Copyright (c) 1993-2016 Dassault Systemes. All Rights Reserved.
 */
public class enoECMActionLinkAccessBase_mxJPO extends emxDomainObject_mxJPO
{
	
    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @throws Exception if the operation fails.
     * @since ECM R211
     *
     */
    public enoECMActionLinkAccessBase_mxJPO (Context context, String[] args)
      throws Exception
    {
        super(context, args);

    }

    /**
     * This method is executed if a specific method is not specified.
     *
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds no arguments.
     * @return int.
     * @throws Exception if the operation fails.
     * @since ECM R211
     */
    public int mxMain(Context context, String[] args)
      throws Exception
    {
      if (true)
      {
        throw new Exception("must specify metenoECMActionLinkAccess invocation");
      }
      return 0;
    }


	/**
	 * Method to display ownership history related table information in Change Properties page
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Boolean showFieldIfOwnershipHistoryExists(Context context, String[] args)throws Exception {
		boolean showField = false;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String  objectId    = (String)programMap.get(ChangeConstants.OBJECT_ID);	
		return Boolean.valueOf(getHistoryBasedOnAction(context,objectId,ChangeConstants.OWNERSHIP_HISTORY));
		
	}
	
	/**
	 * Method to display hold history related table information in Change Properties page
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Boolean showFieldIfHoldHistoryExists(Context context, String[] args)throws Exception {
		boolean showField = false;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String  objectId    = (String)programMap.get(ChangeConstants.OBJECT_ID);	
		return Boolean.valueOf(getHistoryBasedOnAction(context,objectId,ChangeConstants.HOLD_HISTORY));
		
	}
	/**
	 * Method to display Cancel history related table information in Change Properties page
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public Boolean showFieldIfCancelHistoryExists(Context context, String[] args)throws Exception {
		boolean showField = false;
		HashMap programMap  = (HashMap)JPO.unpackArgs(args);
		String  objectId    = (String)programMap.get(ChangeConstants.OBJECT_ID);	
		return Boolean.valueOf(getHistoryBasedOnAction(context,objectId,ChangeConstants.CANCEL_HISTORY));
		
	}
	
	/**
	 * This method gets ownership history on a Change object.
	 * 
	 * @param context
	 *                the eMatrix <code>Context</code> object
	 * @param args
	 *                holds the following input arguments: 0 - String containing
	 *                Service object id.
	 * @return MapList holds a list of history records.
	 * @throws Exception
	 *                 if the operation fails
	 * @since ECM R211
	 */
	public boolean getHistoryBasedOnAction(Context context,String objectId,String sAction)throws Exception 
	{
		ChangeOrderUI changeOrderUI    = new ChangeOrderUI(objectId);
		StringList customHistoryList   = changeOrderUI.getCustomHistory(context);
		StringList filteredHistoryList = changeOrderUI.getFilteredHistoryBasedOnAction(customHistoryList,sAction);
		return (filteredHistoryList != null && filteredHistoryList.size() > 0) ? true : false;
	}

	/**
     * To hide any Form Field in Create/Edit webform
     * @param context the eMatrix <code>Context</code> object.
     * @param args holds objectId.
     * @return Boolean.
     * @throws Exception If the operation fails.
     * @since ECM R211
     */
   public boolean hideFieldInEdit(Context context, String []args) throws Exception
     {
	    boolean hideField = true;
	    try{

        HashMap paramMap = (HashMap)JPO.unpackArgs(args);
        String mode =(String) paramMap.get("mode");
           if(mode.equals("edit" )){
           hideField=false;
		   }
		}catch(Exception ex){
		}
		return hideField;
    }
}





