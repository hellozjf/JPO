import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import com.dassault_systemes.enovia.changeaction.factory.ChangeActionFactory;
import com.dassault_systemes.enovia.changeaction.interfaces.IChangeActionServices;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.XSSUtil;
import com.matrixone.apps.framework.ui.UINavigatorUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

public class enoECMChangeUXBase_mxJPO extends emxDomainObject_mxJPO {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String SUITE_KEY = "EnterpriseChangeMgt";

	public enoECMChangeUXBase_mxJPO(Context context, String[] args)throws Exception {
		super(context, args);
		// TODO Auto-generated constructor stub
	}
	/**
	 * Method to get HTML For Change Control Value
	 * @param Context context
	 * @param args holds information about object.
	 * @return Change Control Value
	 * @throws Exception if operation fails.
	 * @since R419.HF4 ECM
	 */
	public Vector<String> getHTMLForChangeControlValue(Context context,String[] args) throws Exception{

		//XSSOK
		Vector<String> columnVals = new Vector<String>();
		List<String> iPidObjectList = new ArrayList<String>(); 
		String changeControlHTML = EMPTY_STRING;
		try {
			HashMap<?, ?> programMap = (HashMap<?, ?>)JPO.unpackArgs(args);
			StringList objectSelects = new StringList(ChangeConstants.SELECT_PHYSICAL_ID);
			MapList objectList = (MapList) programMap.get(ChangeConstants.OBJECT_LIST);
			ChangeUtil changeUtil = new ChangeUtil();
			boolean exportToExcel = false;


			StringList slObjectPhysicalIdList = changeUtil.getStringListFromMapList(objectList,DomainObject.SELECT_ID);
			if (objectList == null || objectList.size() == 0){
				return columnVals;
			} else{
				columnVals = new Vector<String>(slObjectPhysicalIdList.size());
			}
			String[] objectIdArray = new String[slObjectPhysicalIdList.size()];
			for(int index=0;index<slObjectPhysicalIdList.size();index++) {
				objectIdArray[index] =(String) slObjectPhysicalIdList.get(index);

			}
			// get the physical id from object id
			MapList objMapList =  DomainObject.getInfo( context,objectIdArray, objectSelects) ;
			for(int index=0; index<objMapList.size();index++) {
				Map<?, ?> objMap = (Map<?, ?>) objMapList.get(index);
				String physicalId = (String) objMap.get(ChangeConstants.SELECT_PHYSICAL_ID);
				iPidObjectList.add(physicalId);
			}
			//Calling modeler API to get Change control flag
			IChangeActionServices iChangeActionServices = ChangeActionFactory.CreateChangeActionFactory();
			Map<String, String> mapChangeControlObject = iChangeActionServices.getChangeControlFromPidList(context,iPidObjectList);
			for(int index=0;index<mapChangeControlObject.size();index++){

				String changeControl = EMPTY_STRING;
				String physicalId = iPidObjectList.get(index);
				if(mapChangeControlObject.containsKey(physicalId)) {
					changeControl =	(String) mapChangeControlObject.get(physicalId);
					if(!ChangeUtil.isNullOrEmpty(changeControl)){
						changeControlHTML = makeHTMLForChangeControl(context, changeControl,exportToExcel);
					}else{
						changeControlHTML = EMPTY_STRING;
					}
				}
				columnVals.add(changeControlHTML);
			}
			return columnVals;

		} catch (Exception e) {
			throw new FrameworkException(e);
		}

	}
	/**
	 * Method to get HTML For Change Control Value on Properties page.
	 * @param context
	 * @param args
	 * @return String
	 * @throws Exception
	 * @since R419.HF4 ECM
	 */
	public String getHTMLForChangeControlValueOnForm(Context context, String[] args) throws Exception
	{
		String changeControlHTML = EMPTY_STRING;
		try {
			HashMap<?, ?> paramMap     = (HashMap<?, ?>) JPO.unpackArgs(args);
			HashMap<?, ?> requestMap   = (HashMap<?, ?>) paramMap.get(ChangeConstants.REQUEST_MAP);
			String objectId      = (String)requestMap.get(ChangeConstants.OBJECT_ID);


			// For export to CSV
			String exportFormat = null;
			boolean exportToExcel = false;
			if(requestMap!=null && requestMap.containsKey("reportFormat")){
				exportFormat = (String)requestMap.get("reportFormat");
			}
			if("CSV".equals(exportFormat)){
				exportToExcel = true;
			}

			DomainObject domObj = new DomainObject(objectId);
			String strPhysicalId = domObj.getInfo(context, ChangeConstants.SELECT_PHYSICAL_ID);
			List<String> iPidObjectList = new ArrayList<String>(); 
			iPidObjectList.add(strPhysicalId);
			//Calling modeler API to get Change control flag
			IChangeActionServices iChangeActionServices = ChangeActionFactory.CreateChangeActionFactory();
			Map<String, String> mapChangeControlObject = iChangeActionServices.getChangeControlFromPidList(context,iPidObjectList);
			for(int index=0;index<mapChangeControlObject.size();index++){

				String changeControl = EMPTY_STRING;
				String physicalId = iPidObjectList.get(index);
				if(mapChangeControlObject.containsKey(physicalId)) {
					changeControl =	(String) mapChangeControlObject.get(physicalId);
					if(!ChangeUtil.isNullOrEmpty(changeControl)){
						changeControlHTML = makeHTMLForChangeControl(context, changeControl,exportToExcel);
					}else{
						changeControlHTML = EMPTY_STRING;
					}
				}
			}
		}

		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex);
		}
		return changeControlHTML;
	}
	/**
	 * Method to make HTML For Change Control Value .
	 * @param context
	 * @param ChangeControl
	 * @param exportToExcel
	 * @return String
	 * @throws Exception
	 * @since R419.HF4 ECM
	 */
	private String makeHTMLForChangeControl(Context context,String changeControl,boolean exportToExcel) throws Exception{
		String strLanguage  	   =  context.getSession().getLanguage();
		StringBuffer sb    = new StringBuffer(500);
		String strTreeLink			= EMPTY_STRING;
		StringBuffer objectIcon		= new StringBuffer();
		try{
			if(!ChangeUtil.isNullOrEmpty(changeControl)){


			if("None".equalsIgnoreCase(changeControl)){
				changeControl = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"EnterpriseChangeMgt.Label.Disabled",strLanguage);
				sb.append(changeControl);
			}else if("any".equalsIgnoreCase(changeControl)){
				changeControl = EnoviaResourceBundle.getProperty(context, SUITE_KEY,"EnterpriseChangeMgt.Label.EnabledAnyChange",strLanguage);
				sb.append(changeControl);
				}else if(new DomainObject(changeControl).exists(context)) {
				StringList slSelectable = new StringList(SELECT_NAME);
				slSelectable.add(ChangeConstants.SELECT_ATTRIBUTE_SYNOPSIS);
				slSelectable.add(SELECT_TYPE);

				String changeName = EMPTY_STRING;
				String changeTitle = EMPTY_STRING;
				String changeType 	 = EMPTY_STRING;
					String strCADisplayPreference = EMPTY_STRING;
					strCADisplayPreference = EnoviaResourceBundle.getProperty(context, "EnterpriseChangeMgt", Locale.US, "EnterpriseChangeMgt.ChangeControl.CADisplayPreference");
					
					Map<String, String> changeInfo = new DomainObject(changeControl).getInfo(context, slSelectable);
					changeName = changeInfo.get(SELECT_NAME);
					changeTitle = changeInfo.get(ChangeConstants.SELECT_ATTRIBUTE_SYNOPSIS);
					changeType = changeInfo.get(SELECT_TYPE);

					strCADisplayPreference = "Title".equalsIgnoreCase(strCADisplayPreference)?changeTitle:changeName;

				objectIcon.append(UINavigatorUtil.getTypeIconProperty(context, changeType));
					strTreeLink = "<a class=\"object\" href=\"JavaScript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=" + XSSUtil.encodeForHTMLAttribute(context, changeControl) + "', '800', '575','true','content')\"><img border='0' src='../common/images/"+XSSUtil.encodeForHTMLAttribute(context, objectIcon.toString())+"'/>"+XSSUtil.encodeForHTML(context, strCADisplayPreference)+"</a>";
				sb.append(strTreeLink);
				objectIcon.setLength(0);

			}
			}else{
				sb.append(EMPTY_STRING);
			}
		}catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex);
		}
		return sb.toString();

	}
    /** This is Access program  for Set Change control command. Here we are invoking Application specific JPO and Modeler API.
    * JPO we have provided so that application can put addiction access check on this command.
    * Applications team will pass two URL parameters in the URL which has menu that has ECM 
    * commands or call ECM commands directly. The name of the URL parameters would be :
    * ECMChangeControlAppAccessJPO and ECMChangeControlAppAccessMethod
    * we have added FromSetChangeControl in args so that application program need to identify for which action it is invoked
    * @param context
    * @param args holds information about object.
    * @return true if  set change control command is available 
    * @throws Exception
	* @since R419.HF4 ECM
    */

	public boolean isSetChanageControlAvailable(Context context, String[] args) throws Exception{
		//unpacking the Arguments from variable args
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strChangeControlAppAccessMethod = (String)programMap.get("ECMChangeControlAppAccessMethod");
		String strChangeControlAppAccessJPO    = (String)programMap.get("ECMChangeControlAppAccessJPO");
		boolean isSetChanageControlAvailable   = false;
		boolean isChangeControlAppAccess       = true; 
		
		if(!ChangeUtil.isNullOrEmpty(strChangeControlAppAccessJPO) && !ChangeUtil.isNullOrEmpty(strChangeControlAppAccessMethod)){
			isChangeControlAppAccess = false;
			programMap.put("FromSetChangeControl",true);
			try{
				isChangeControlAppAccess = JPO.invoke(context,strChangeControlAppAccessJPO,null,strChangeControlAppAccessMethod,JPO.packArgs(programMap),Boolean.class);
			}catch (Exception e) {
				isChangeControlAppAccess =false;
			}
		}
		if(isChangeControlAppAccess){
			String strObjId   = (String)programMap.get(ChangeConstants.OBJECT_ID);
			if(!ChangeUtil.isNullOrEmpty(strObjId)){
				String pid = new DomainObject(strObjId).getInfo(context, ChangeConstants.SELECT_PHYSICAL_ID);
				IChangeActionServices iChangeActionServices = ChangeActionFactory.CreateChangeActionFactory();
				int canObjectBeUnsetAsChangeControl = iChangeActionServices.canObjectBeSetAsChangeControl(context,pid);
				if(canObjectBeUnsetAsChangeControl==0)
					isSetChanageControlAvailable = true;
			}
		}
		return isSetChanageControlAvailable;
	}
	/**This is Access program  for Unset Change control command. Here we are invoking Application specific JPO and Modeler API.
    * JPO we have provided so that application can put addiction access check on this command.
    * Applications team will pass two URL parameters in the URL which has menu that has ECM 
    * commands or call ECM commands directly. The name of the URL parameters would be :
    * ECMChangeControlAppAccessJPO and ECMChangeControlAppAccessMethod
    * we have added FromSetChangeControl in args so that application program need to identify for which action it is invoked
	* @param context
	* @param args holds information about object.
	* @return true if  Unset change control command is available 
	* @throws Exception
	* @since R419.HF4 ECM
	*/
	public boolean isUnsetChanageControlAvailable(Context context, String[] args) throws Exception{
		//unpacking the Arguments from variable args
		HashMap programMap = (HashMap)JPO.unpackArgs(args);
		String strChangeControlAppAccessMethod = (String)programMap.get("ECMChangeControlAppAccessMethod");
		String strChangeControlAppAccessJPO    = (String)programMap.get("ECMChangeControlAppAccessJPO");
		boolean isUnsetChanageControlAvailable = false;
		boolean isChangeControlAppAccess       = true; 
		
		if(!ChangeUtil.isNullOrEmpty(strChangeControlAppAccessJPO) && !ChangeUtil.isNullOrEmpty(strChangeControlAppAccessMethod)){
			isChangeControlAppAccess = false;
			programMap.put("FromSetChangeControl",false);
			try{
				isChangeControlAppAccess = JPO.invoke(context,strChangeControlAppAccessJPO,null,strChangeControlAppAccessMethod,JPO.packArgs(programMap),Boolean.class);
			}catch (Exception e) {
				isChangeControlAppAccess =false;
			}
		}
		if(isChangeControlAppAccess){
			String strObjId   = (String)programMap.get(ChangeConstants.OBJECT_ID);
			if(!ChangeUtil.isNullOrEmpty(strObjId)){
				String pid = new DomainObject(strObjId).getInfo(context, ChangeConstants.SELECT_PHYSICAL_ID);
				IChangeActionServices iChangeActionServices = ChangeActionFactory.CreateChangeActionFactory();
				int canObjectBeUnsetAsChangeControl = iChangeActionServices.canObjectBeUnsetAsChangeControl(context,pid);
				if(canObjectBeUnsetAsChangeControl==0)
					isUnsetChanageControlAvailable = true;
			}
		}
		
		return isUnsetChanageControlAvailable;
	}

}
