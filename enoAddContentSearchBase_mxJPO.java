import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkProperties;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIComponent;
import com.matrixone.apps.framework.ui.UISearchUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.JSONObject;
import com.matrixone.search.index.Config;


public class enoAddContentSearchBase_mxJPO extends emxAEFFullSearchBase_mxJPO {
	public enoAddContentSearchBase_mxJPO(Context context, String[] args) throws Exception {
		super(context, args);
	}
	@com.matrixone.apps.framework.ui.ProgramCallable
	public MapList search(Context context, String[] args) throws Exception {

		HashMap params = (HashMap) JPO.unpackArgs(args); 
		String ftsFilters = (String)params.get("ftsFilters");
		String objectId = (String)params.get("objectId") ;
		StringList exludeList = new StringList();
		String mode = (String)params.get("mode");
		String scopeId = (String)params.get("scopeId");
		String contentID = (String)params.get("contentID");
		JSONObject	json = super.extractFilters(params);
		StringList filterList = new StringList();
		filterList  = getFilterValues(json, "ADDCONTENTTYPE");
		if(!filterList.isEmpty())
		{
			params.put("selType",(String)filterList.get(0));
		}else{
			params.put("selType","Document");
		}

		StringBuffer name = new StringBuffer("Name");
		StringBuffer  revision = new StringBuffer("Revision");
		StringBuffer owner = new StringBuffer("Owner");
		StringBuffer originator = new StringBuffer("ORIGINATOR");

		if(UIUtil.isNotNullAndNotEmpty(ftsFilters)){      
			if(ftsFilters.contains(name.toString()))
			{
				filterList.clear();
				filterList  = getFilterValues(json, name.toString());
				params.put("txtName",(String)filterList.get(0));
			}
			if(ftsFilters.contains(revision.toString()))
			{
				filterList.clear();
				filterList  = getFilterValues(json, revision.toString());
				params.put("txtRev",(String)filterList.get(0));
			}
			if(ftsFilters.contains(owner.toString()))
			{
				filterList.clear();
				filterList  = getFilterValues(json, owner.toString()); 
				params.put("txtOwner",(String)filterList.get(0));
			}
			if(UIUtil.isNotNullAndNotEmpty(scopeId) )
			{
				params.put("scopId",scopeId);
			}

			if(ftsFilters.contains(originator.toString()) )
			{
				filterList.clear();
				filterList  = getFilterValues(json, originator.toString());
				params.put("txtOriginator",(String)filterList.toString());
			}
		}
		args= JPO.packArgs(params);
		MapList totalresultList = null;

		if (mode.equalsIgnoreCase("addContenttoRoute"))
		{

			 totalresultList = (MapList) JPO.invoke(context, "emxRouteContentSearchBase", null, "getContents", args, MapList.class);
			if(UIUtil.isNotNullAndNotEmpty(objectId))
			{
				DomainObject dombj = new  DomainObject(objectId);
				StringList sList = new StringList();
				sList.addElement(DomainConstants.SELECT_TYPE);
				sList.addElement("relationship[Route Task].to.id");
				Map mp =dombj.getInfo(context,sList);
				String stype =(String)mp.get(DomainConstants.SELECT_TYPE);
				if(stype.equals(DomainConstants.TYPE_INBOX_TASK))
				{
					objectId= (String)mp.get("relationship[Route Task].to.id");

				}
				dombj.setId(objectId);
				exludeList=dombj.getInfoList(context,"relationship[Object Route].from.id");

			}


		}
		if(mode.equals("addContentCreateRoute"))
		{
		   totalresultList = (MapList) JPO.invoke(context, "emxAppContentBase", null, "getSearchResult", args, MapList.class);
			if(UIUtil.isNotNullAndNotEmpty(contentID))
			{
				exludeList=FrameworkUtil.split(contentID, "~");
			}
		}

		MapList returnMapList = new MapList();
		returnMapList.add(totalresultList.size());

		Iterator itr  = totalresultList.iterator();
		while (itr.hasNext())
		{
			Map mp = (Map)itr.next();
			String includeOID = (String)mp.get("id");

			if(!exludeList.contains(includeOID))
			{
				returnMapList.add((Map)mp);
			}

		}
		return returnMapList;

	}



	protected StringList getFieldInclusionList(Context context,HashMap params) throws Exception {
		StringList includeFields = new StringList();
		if (UISearchUtil.isFormMode(params)) {
			MapList fields = getFormFields(context,params);
			for (int i = 0; i < fields.size(); i++) {
				HashMap field = (HashMap)fields.get(i);
				String fieldName = UIComponent.getName(field);
				Config.Field configfield = _config.indexedBOField(fieldName);
				if(configfield != null){
					includeFields.add(fieldName);
				}
			}


			if(includeFields != null && includeFields.size() <= 0){
				String fixedFields = UISearchUtil.isAutonomySearch(context,params) ? "emxFramework.FullTextSearch.FormView.Indexed.FixedFields"
						: "emxFramework.FullTextSearch.FormView.RealTime.FixedFields";

				includeFields = FrameworkProperties.getTokenizedProperty(context,fixedFields, ",");
			}
			String sFormInclusionCsl = (String) params.get(UISearchUtil.FORMINCLUSIONLIST);
			if(canUse(sFormInclusionCsl)){
				StringList formInclusionList = FrameworkUtil.split(sFormInclusionCsl.toUpperCase(), ",");
				for (int i = 0; i < formInclusionList.size(); i++) {
					String currentFormField = (String)formInclusionList.get(i);
					if(includeFields.contains(currentFormField)){
						formInclusionList.remove(currentFormField);
						i--;
					}
				}
				if(formInclusionList.size() > 0){
					String formInclString = formInclusionList.toString().substring(1,formInclusionList.toString().length()-1);
					if (canUse(formInclString)) {
						includeFields.addAll(formInclusionList);
					}
				}
			}

		} else {
			includeFields = FrameworkProperties.getTokenizedProperty(context,
					"emxFramework.FullTextSearch.NavigationView.IncludeFields", ",");
		}
		return includeFields;
	}
}



