import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.History;
import matrix.db.HistoryItr;
import matrix.db.HistoryList;
import matrix.db.JPO;
import matrix.db.NameValue;
import matrix.db.NameValueItr;
import matrix.db.NameValueList;
import matrix.db.Policy;

import com.matrixone.apps.common.util.ComponentsUtil;
import com.matrixone.apps.domain.util.MapList;


public class emxPlayButtonSummaryBase_mxJPO {

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */
    public emxPlayButtonSummaryBase_mxJPO (Context context, String[] args) throws Exception {
    
    }

    /**
     * Main entry point.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     */
    public int mxMain(Context context, String[] args) throws Exception {
    	
    	if (!context.isConnected()) {
    		throw new Exception(ComponentsUtil.i18nStringNow("emxComponents.Common.ERROR", context.getLocale().getLanguage()));
    	}
      
    	return (0);
    }

    /**
     * Get the history for the given object.
     * 
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
	public MapList getHistory(Context context, String[] args) throws Exception {


        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }
        
        MapList ml = new MapList();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) JPO.unpackArgs(args);
        Map<String, Object> widgetArgs = (Map) map.get("JPO_WIDGET_ARGS");
		Locale strLocale = new Locale(context.getSession().getLanguage());
		String objectId = ((String[]) widgetArgs.get("objectId"))[0];
		
		BusinessObject bo = new BusinessObject(objectId);
		Policy policy = bo.getPolicy(context);
		HistoryList historyList = bo.getFilteredHistory(context, null, false, null, 0, 0, false, null, true, 0);
		HistoryItr itr = new HistoryItr(historyList);
		while (itr.next()) {
			History history = itr.obj();
			Map historyMap = new HashMap<String,String>();
			historyMap.put("date", (new Date(history.getTime())).toString());
			historyMap.put("user", history.getUser());
			historyMap.put("action", history.getEvent());
			historyMap.put("state", history.getState());
			historyMap.put("policy", policy.getName());
			
			String description = "";
			NameValueList nvList = history.getInfo();
			NameValueItr nvItr = new NameValueItr(nvList);
			while (nvItr.next()) {
				NameValue nv = nvItr.obj();
				String name = nv.getName();
				String value = nv.getValue();
				
				// build the description
				if (history.getEvent().contains("connect")) {
					description += value + " ";
				}
				else { 
					if ("name".equals(name)) {
						description += value + ":";
					}
					else if ("value".equals(name)) {
						description += " " + value + " ";
					}
					else {
						if (value != null && value.length() > 0) {
							description += " " + name + ": " + value;
						}
					}
				}
			}
			
			historyMap.put("description", description);
			ml.add(historyMap);
		}
		
		return (ml);
	}

    /**
     * Get the documents for the given object.
     * 
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
	public MapList getDocuments(Context context, String[] args) throws Exception {

        if (args == null || args.length < 1) {
            throw (new IllegalArgumentException());
        }
        
        MapList ml = new MapList();

        Map<String, Object> map = (Map<String, Object>) JPO.unpackArgs(args);
        Map<String, Object> widgetArgs = (Map<String, Object>) map.get("JPO_WIDGET_ARGS");
		Locale strLocale = new Locale(context.getSession().getLanguage());
		String objectId = ((String[]) widgetArgs.get("objectId"))[0];
		String parentRelName = (String) widgetArgs.get("parentRelName");
		
        HashMap<String, String> jpoMap = new HashMap<String, String>();
        jpoMap.put("objectId", objectId);
        jpoMap.put("parentRelName", parentRelName);
//        String[] jpoArgs = JPO.packArgs(jpoMap);
//		${CLASS:emxCommonDocumentUI} jpo = new ${CLASS:emxCommonDocumentUI}(context, jpoArgs);
		ml = (MapList) JPO.invoke(context, "emxCommonDocumentUI", null, "getDocuments", JPO.packArgs(jpoMap), MapList.class);
		return (ml);
	}
}
