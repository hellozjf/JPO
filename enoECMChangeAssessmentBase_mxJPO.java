
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import com.dassault_systemes.enovia.enterprisechangemgt.admin.ECMAdmin;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeConstants;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeManagement;
import com.dassault_systemes.enovia.enterprisechangemgt.common.ChangeOrder;
import com.dassault_systemes.enovia.enterprisechangemgt.util.ChangeUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

public class enoECMChangeAssessmentBase_mxJPO extends DomainObject
{
	String ChildItems = "Child Items";
	String ParentItems = "Parent Items";
	String ChildAndRelated = "Child and Related Items";	
	String ParentAndRelated = "Parent and Related Items";
	String RelatedItem = "Related Item";
	public static final String RELATIONSHIP_CANDIDATE_AFFECTED_ITEM = PropertyUtil.getSchemaProperty("relationship_CandidateAffectedItem");
	public static final String VAULT = "eService Production";
	
	/**
     * Constructs a new ChangeAssessmentBase JPO object.
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     */

    public enoECMChangeAssessmentBase_mxJPO(Context context, String[] args) throws Exception
    {
        super();
    }
  
    /**
     * Getting the Child/ Parent from the selected item
     * @param context
     * @param args
     * @return MapList of Child/ Parent
     * @throws Exception
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getChangeAssessmentItems(Context context, String[] args)throws Exception
    {
    	MapList mlOutput = new MapList(10);
    	try
    	{
    		HashMap hmParamMap = (HashMap) JPO.unpackArgs(args);
    		String strRec = "";
			String[] strarr;
			String strRelName = "";
			String strDir = "";
			String strLabel = "";
			Map tempmap;
			String[] arrTableRowIds = new String[1];
			String strTableRowID = (String)hmParamMap.get("emxTableRowId");
			arrTableRowIds[0]=strTableRowID;
			ChangeUtil changeUtil = new ChangeUtil ();
			StringList slObjectIds = changeUtil.getAffectedItemsIds(context, arrTableRowIds);
    					
			mlOutput = new ChangeManagement().getChangeAssessment(context, slObjectIds);
    	}
 		catch(Exception Ex)
 		{
 			Ex.printStackTrace();
 			throw Ex;
 		}
 		return mlOutput;
 	}
    
    /**
     * Fetching the Custom label for grouping the values
     * @param context
     * @param args
     * @return Maplist of connected Items
     * @throws Exception
     */
    public Vector getCustomLabel(Context context, String[] args)throws Exception {
		Vector columnValues = new Vector();
		try {
			HashMap programMap = (HashMap) JPO.unpackArgs(args);
			MapList objList = (MapList) programMap.get("objectList");
			Hashtable map;
			Iterator itr = objList.iterator();
			while (itr.hasNext()) {
				map = (Hashtable) itr.next();
				columnValues.addElement(map.get("strLabel"));
			}
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		return columnValues;
    }
      
    /**
     *Filter option range Function for Related item
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public StringList getOptionForFilter(Context context, String[] args)throws Exception
    {
    	StringList slOutput = new StringList();
    	try
    	{
    		HashMap hmParamMap = (HashMap) JPO.unpackArgs(args);
    		HashMap hmRequestMap = (HashMap) hmParamMap.get("requestMap");
    		String strToside = (String) hmRequestMap.get("ToSide");
    		
    		if(strToside != null && strToside.equals("true"))
    		{
    			slOutput.add(ChildItems);
    			slOutput.add(ChildAndRelated);        		
    		}
    		else if(strToside != null && strToside.equals("false"))
    		{
    			slOutput.add(ParentItems);
    			slOutput.add(ParentAndRelated);        		
    		}
    		else
    		{
    			slOutput.add(RelatedItem);
    		}
    	}
    	catch(Exception Ex)
    	{
    		Ex.printStackTrace();
    		throw Ex;
    	}
    	return slOutput;
    }
	/**
	 * To display all the Candiate Item on the table which are connected to the Candidate Object.
	 * @param context
	 * @param args Context Object ID (CO)
	 * @return MapList
	 * @throws Exception
	 */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public MapList getCandidateItems(Context context,String[] args)throws Exception
    {    	
    	MapList mlOutput = new MapList();
    	try
    	{
    		HashMap programMap = (HashMap)JPO.unpackArgs(args);
    		StringList objectSelects = new StringList(DomainConstants.SELECT_ID);
    		StringList relSelects = new StringList(DomainConstants.SELECT_RELATIONSHIP_ID);
    		short recureLevel = 1;  		
    		
    		//getting parent object Id from args
    		String strCOId = (String)programMap.get("objectId");
    		this.setId(strCOId);
    		
    		mlOutput  = getRelatedObjects(context,
    				RELATIONSHIP_CANDIDATE_AFFECTED_ITEM,
    				"*",
    				objectSelects,
    				relSelects,
    				false,
    				true,
    				recureLevel,
    				"",
    				DomainConstants.EMPTY_STRING,
    				0);
    	}
    	catch(Exception Ex)
    	{
    		Ex.printStackTrace();
    		throw Ex;
    	}
    	return mlOutput;
    }
}
