import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.dassault_systemes.enovia.enterprisechange.modeler.ChangeOrder;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.framework.ui.UICache;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;





public class enoECMChangeRequestUXBase_mxJPO extends emxDomainObject_mxJPO {

	public enoECMChangeRequestUXBase_mxJPO(Context context, String[] args)throws Exception {
		super(context, args);
		// TODO Auto-generated constructor stub
	}
	/*
	 *   Returns a JSONObject with required information to perform a Drag and drop Operation. 
	 *   @args: Will have the information of the Dragged Object and the Dropped Object in the following format.
	 *   		drop={"window":"ECMCRChangeOrders","columnName":,"timestamp":"","object":{"oid":,:,"rid":}}
	 * 			drag={"objects":[{"icon":,"id":,"oid":,"rid":,"type":},{"icon":,"id":,"oid":,"rid":,"type":}],"action":,"window":}}
	 *   @return JSONObject with contains the information about the operation is pass or fail along with relationship id's of the connected objects.
	 *   @throws Exception if error encountered while carrying out the request
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public JSONObject dropCAProcess(Context context, String[] args) throws Exception{

		Map param = (Map)JPO.unpackArgs(args);
		System.out.println(param);

		StringBuffer returnMsgBuffer = new StringBuffer();
		JSONObject ret = new JSONObject();
		try{

			JSONObject jDrop = (JSONObject)param.get("drop");
			JSONObject jDropObject = jDrop.getJSONObject("object");
			JSONObject jDrag = (JSONObject)param.get("drag");
			JSONArray jDragObjects = jDrag.getJSONArray("objects");

			String dropObjectId = jDropObject.getString("oid");
			ChangeOrder objChangeOrder=new ChangeOrder(dropObjectId);
			String object[]=new String[jDragObjects.length()];
			for (int i=0; i<jDragObjects.length(); i++) {
				JSONObject jDragObject = jDragObjects.getJSONObject(i);
				String dragObjectId = jDragObject.getString("oid");
				object[i]=dragObjectId;
			}

			//Connect CR and CO
			objChangeOrder.addChangeActions(context, object);
			returnMsgBuffer.append("  var coContentFrame  =findFrame(getTopWindow(),\"ECMCRChangeOrders\");");
			returnMsgBuffer.append("  var caContentFrame  =findFrame(getTopWindow(),\"ECMUndispatchedCA\");");
			returnMsgBuffer.append(" coContentFrame.editableTable.loadData();");
			returnMsgBuffer.append("coContentFrame.rebuildView();");
			returnMsgBuffer.append(" caContentFrame.editableTable.loadData();");
			returnMsgBuffer.append("caContentFrame.rebuildView();");
			ret.put("result", "pass");
			ret.put("onDrop", "function () {"+returnMsgBuffer.toString()+"}");

		}
		catch (Exception e)
		{	
			ret.put("result", "fail");
			ret.put("message", e.toString());
			throw new Exception(e.toString());
		}

		return ret;
	}
	
	/**
	 * Method to get Legacy Changes
	 * @param context   the eMatrix <code>Context</code> object
	 * @param           String[] of ObjectIds.
	 * @return          Object containing CO objects
	 * @throws          Exception if the operation fails
	 * @since           ECM R212
	 */
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList getLegacyChanges(Context context, String args[]) throws Exception
	{
		MapList sTableData 			= new MapList();
		MapList sResultList 		= new MapList();
		StringList s1 				= new StringList();
		HashMap programMap          = (HashMap)JPO.unpackArgs(args);
		String strObjectId			= (String)programMap.get("objectId");
		HashMap requestMap          = (HashMap)programMap.get("requestMap");
		String filterToolbar 		= (String)programMap.get("toolbar");
		Map tmpMap 					= UICache.getMenu(context, filterToolbar);
		MapList filterCmdsList 		= (MapList)tmpMap.get("children");
		Map filterOptCmdMap 		= (Map)filterCmdsList.get(0);
		String filterOptCmd 		= (String)filterOptCmdMap.get("name");

		Map commandInfoMap 			= (Map)UICache.getCommand(context, filterOptCmd);
		Map commandSetting 			= (Map)commandInfoMap.get("settings");
		String sRangeProgram 		= (String)commandSetting.get("Range Program");
		String sRangeFunction 		= (String)commandSetting.get("Range Function");
		HashMap sRangeProgramResultMap = (HashMap)JPO.invoke(context, sRangeProgram, null,
				sRangeFunction, JPO.packArgs(new HashMap()), HashMap.class);

		StringList choicesList 			= null;
		String singleSearchType 		="";
		String cmdLabel 				= "";
		String commandName 				= "";
		String sDisplayValue 			= "";
		String sActualValue 			= "";
		String sRegisteredSuite 		= "";
		String strStringResourceFile 	= "";
		String sRequiredCommand 		= "";
		String searchType   			= "";
		String sLegacytype 				= "";

		StringList strList = new StringList();
		strList.add(SELECT_NAME);
		strList.add(SELECT_TYPE);
		strList.add(SELECT_ID);
		String wherClause = SELECT_CURRENT + "== 'Active' ";
		StringBuffer sbSearchType = new StringBuffer();

		if(sRangeProgramResultMap!=null){
			choicesList 		= (StringList)sRangeProgramResultMap.get("field_display_choices");
			if(choicesList!=null)
				singleSearchType 	= (String)choicesList.get(0);
		}
		//Getting the ECM Menu details and its commands
		HashMap menuMap 	= UICache.getMenu(context, "ECMChangeLegacyMenu");
		String sECMMenu 	= (String)menuMap.get("name");
		MapList commandMap 	= (MapList)menuMap.get("children");
		if(commandMap!=null){
			Iterator cmdItr = commandMap.iterator();
			while(cmdItr.hasNext())	{
				Map tempMap = (Map)cmdItr.next();
				commandName = (String)tempMap.get("name");
				HashMap cmdMap = UICache.getCommand(context, commandName);
				HashMap settingMap = (HashMap)cmdMap.get("settings");
				cmdLabel = (String)cmdMap.get("label");
				sRegisteredSuite = (String)settingMap.get("Registered Suite");


				//strStringResourceFile = UINavigatorUtil.getStringResourceFileId(sRegisteredSuite);


				StringBuffer strBuf = new StringBuffer("emx");
				strBuf.append(sRegisteredSuite);
				strBuf.append("StringResource");

				sDisplayValue =EnoviaResourceBundle.getProperty(context, strBuf.toString(), context.getLocale(),cmdLabel);
				if(sDisplayValue.equals(singleSearchType)){
					sRequiredCommand =  commandName;
					break;
				}
			}
		}

		if(!UIUtil.isNullOrEmpty(sRequiredCommand)){
			HashMap cmdMap = UICache.getCommand(context, sRequiredCommand);
			String cmdHref = (String)cmdMap.get("href");
			HashMap settingsMap = (HashMap)cmdMap.get("settings");
			searchType = (String)settingsMap.get("searchType");
		}
		//Getting ECM details

		if(!UIUtil.isNullOrEmpty(searchType)){
			StringList sTypeList = FrameworkUtil.split(searchType, ",");
			for(int i= 0; i<sTypeList.size(); i++){
				String single = (String)sTypeList.get(i);
				sLegacytype = PropertyUtil.getSchemaProperty(context,single);
				sbSearchType.append(sLegacytype);
				if(i!=sTypeList.size()-1)
					sbSearchType.append(",");
			}
		}

		try{
			if(!UIUtil.isNullOrEmpty(sbSearchType.toString()))
				if(UIUtil.isNullOrEmpty(strObjectId))
				sTableData = DomainObject.findObjects(context, sbSearchType.toString(), "*", null, strList);
				else{
					DomainObject partObject=new DomainObject(strObjectId);
					sResultList= partObject.getRelatedObjects(context, 
							  											QUERY_WILDCARD, 
							  											sbSearchType.toString(), 
							  											strList, 
																	  	null, 
																	  	true, 
																	  	false, 
																	  	(short)1, 
																	  	null, 
															  			null,
															  			0);
					
					Iterator itr = sResultList.iterator();
					while (itr.hasNext())
					{
						Map sChangeObject = (Map) itr.next();
						String sChangeId = (String) sChangeObject.get(SELECT_ID);
						if(!s1.contains(sChangeId)){
							s1.add(sChangeId);
							sTableData.add(sChangeObject);
						}
					}
				}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		if(sTableData!=null)
			return sTableData;
		else
			return new MapList();



	}



}
