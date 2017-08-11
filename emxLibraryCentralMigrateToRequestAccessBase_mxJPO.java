import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;

import matrix.db.Context;
import matrix.util.StringList;


public class emxLibraryCentralMigrateToRequestAccessBase_mxJPO extends emxCommonMigration_mxJPO{

	protected String person_RequestAccessGrantor = null;
    public boolean unconvertable = false;
    public String unConvertableComments = "";

	private void init(Context context) throws FrameworkException{
		
		person_RequestAccessGrantor    = PropertyUtil.getSchemaProperty(context,"person_RequestAccessGrantor");

	}
	
	public emxLibraryCentralMigrateToRequestAccessBase_mxJPO(
			Context context, String[] args) throws Exception {
		super(context, args);
		init(context);
	}
	

	public void migrateObjects(Context context, StringList objectList) throws Exception{
        mqlLogRequiredInformationWriter("In emxLibraryCentralMigrateToRequestAccessBase 'migrateObjects' method "+"\n");
      
        StringList objectSelectables = new StringList();
        objectSelectables.add("id");
        objectSelectables.add("type");
        objectSelectables.add("name");
        objectSelectables.add("revision");
        objectSelectables.add("grant.grantor");
		objectSelectables.add("grantkey");
        
        String[] oidsArray = new String[objectList.size()];
        oidsArray = (String[])objectList.toArray(oidsArray);
        MapList objectInfoList = DomainObject.getInfo(context, oidsArray, objectSelectables);
        mqlLogRequiredInformationWriter("=================================================================================================================================" + "\n");
        
        for(Iterator itr = objectInfoList.iterator();itr.hasNext();){
        	
        	Map objectInfo = (Map)itr.next();
        	String objectId = (String)objectInfo.get("id");
        	String type = (String)objectInfo.get("type");
        	String name = (String)objectInfo.get("name");

			Set keySet = objectInfo.keySet();
			for(Iterator<String> keyItr = keySet.iterator();keyItr.hasNext();){
				String key = keyItr.next();
				if(key.indexOf("grant[") == 0)
				{
					String grantor = key;
					String grantkeyFull = MqlUtil.mqlCommand(context, 
							 "print bus $1 select $2 dump",objectId,"grantkey");
					String grantkey = grantkeyFull;
					if (grantkeyFull.contains(","))
						grantkey = grantkeyFull.substring(0, grantkeyFull.indexOf(","));
						
					mqlLogRequiredInformationWriter("Started Migrating Object Type '" +type + "'  Name '"+ name + "' with Object Id "+objectId+" grantor " + grantor +"grantkey "+grantkey +" \n");
				
					Map granteeAccessMap = getGranteeAccesses(objectId,grantor);
					String result = MqlUtil.mqlCommand(context, 
									 "print bus $1 select $2 dump",objectId,"grant.grantor[Request Access Grantor]");
					
					if(result.indexOf("TRUE")==0)
					{				 
					  String grantee = (String)granteeAccessMap.get(objectId);					 
					   ContextUtil.pushContext( context, person_RequestAccessGrantor, null, null );
					   String strcheckout = "checkOut";
					   String strRead = "read";
					   String strShow ="show";
					   //MqlUtil.mqlCommand( context, "modify bus " + objectId + " grant \""+ grantee + "\" access checkOut,read,show key " + grantkey );
					   MqlUtil.mqlCommand( context, "modify bus $1 grant $2 access $3 key $4",objectId,grantee,strcheckout,grantkey );
					   MqlUtil.mqlCommand( context, "modify bus $1 grant $2 access $3 key $4",objectId,grantee,strRead,grantkey );
					   MqlUtil.mqlCommand( context, "modify bus $1 grant $2 access $3 key $4",objectId,grantee,strShow,grantkey );
					   ContextUtil.popContext(context);
					   mqlLogRequiredInformationWriter("Completed Migrating Object Type '" +type + "'  Name '"+ name + "' with Object Id "+objectId+" with User "+grantee+ "\n");
					}//end if
				}//end if
				else
				{
					unconvertable = true;
				}
			}
			if( unconvertable ){
				unConvertableComments = "Migration for Object Type '" +type + "'  Name '"+ name + "' with Object Id "+objectId+" is not required \n" ;
                writeUnconvertedOID(unConvertableComments,objectId);
                unconvertable = false;
            } else {
                loadMigratedOids(objectId);
            }

        	mqlLogRequiredInformationWriter("=================================================================================================================================" + "\n");
        }
    }

	private Map getGranteeAccesses(String objectId,String grantor){
		HashMap accessMap = new HashMap<String, String>();

		if(grantor.indexOf("grant[") == 0){
			int startBindex = grantor.indexOf("[", grantor.indexOf("grant"));
			int closeBindex = grantor.indexOf("]", grantor.indexOf("grant"));
			String gratorGrantee = grantor.substring(startBindex+1, closeBindex);
			if( gratorGrantee.indexOf(",") > 0)
			{
				String grantee = gratorGrantee.substring(gratorGrantee.indexOf(",")+1, gratorGrantee.length());
				accessMap.put(objectId, grantee);	
			 }
		 }
	
		return accessMap;
	}

}
