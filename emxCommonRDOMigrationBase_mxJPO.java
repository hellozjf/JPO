import java.util.Iterator;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.common.Company;
import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;

/**
 * @author WGI
 *
 */
@SuppressWarnings("serial")
public class emxCommonRDOMigrationBase_mxJPO extends
		emxCommonMigrationBase_mxJPO {

	/**
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public emxCommonRDOMigrationBase_mxJPO(Context context, String[] args)
			throws Exception {
		super(context, args);
		// TODO Auto-generated constructor stub
	}

    /* (non-Javadoc)
     * @see ${CLASS:emxCommonMigrationBase}#migrateObjects(matrix.db.Context, matrix.util.StringList)
     */
    @SuppressWarnings("deprecation")
	public void migrateObjects(Context context, StringList objectList) throws Exception
    {
      	mqlLogRequiredInformationWriter("In emxCommonRDOMigrationObjects 'migrateObjects' method "+"\n");
      	  	
        try
        {
        	ContextUtil.pushContext(context);
        	Iterator<?> itr = objectList.iterator();
        	while(itr.hasNext())
        	{
        		String str = (String)itr.next();
                mqlLogRequiredInformationWriter("==============================================================================");
        		mqlLogWriter(str);
        		StringList strTokens = StringUtil.split(str, "|");
        		//boolean unconverted = false;
        		String comment = "";
        		if( strTokens.size() == 3 )
        		{
            		String objectId = (String)strTokens.get(0);
            		String project = (String) strTokens.get(1);
            		String org = (String) strTokens.get(2);            		
    	            mqlLogRequiredInformationWriter("Start Migrating Object "+ objectId +" with Project as " + project + " and with Organization as "+ org);	                	            
	            	String cmd = "mod bus $1 project $2 organization $3";
	            	mqlLogWriter("mod bus "+ objectId +" project "+ project +" organization "+ org);
	            	MqlUtil.mqlCommand(context, cmd, objectId, project, org);
	            	loadMigratedOids(objectId);
        		} else {        			
        			comment = "object  "+ str + " is not having enough information to set the primary ownership. \n";
        			writeUnconvertedOID(comment, str);
	            }	            
                mqlLogRequiredInformationWriter("==============================================================================");
        	}
        } catch(Exception ex)  {
        	ex.printStackTrace();
            throw ex;
        }
        finally
        {
            ContextUtil.popContext(context);
        }
    }
    
    /**
     * @param context
     * @param args
     * @throws Exception
     */
    public void transitionProjectOrg(Context context, String[] args) throws Exception
    {
    	String project = DomainAccess.getDefaultProject(context);
    	String organization = PropertyUtil.getSchemaProperty(context, "role_CompanyName");
    	String policy = "";
    	if( args.length > 0)
    	{
    		policy = args[0];
    	} else {
    		policy = EnoviaResourceBundle.getProperty(context, "emxCommonMigration.DefaultRDO.Polices");
    	}
    	String mqlCommand = "transition $1 policy";
    	StringList mqlParam = new StringList();
    	mqlParam.add("project_org");
    	String[] policylist = policy.split(",");
    	int index=0;
    	for (int i = 0; i < policylist.length; i++) {
    		index = i+2;
    		mqlCommand = mqlCommand +" $"+index;
    		if(i!=policylist.length-1){
    			mqlCommand+=",";
    		}
    		mqlParam.add(policylist[i]);
		}
    	
    	mqlCommand +=" project $"+ (++index)+" organization $"+(++index);
    	mqlParam.add(project);
    	mqlParam.add(organization);
    	MqlUtil.mqlCommand(context, mqlCommand,mqlParam);
    }

}
