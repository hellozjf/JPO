import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;


@SuppressWarnings("unchecked")
public class emxSecurityDefaultProjectOwnershipMigrationBase_mxJPO extends emxCommonMigration_mxJPO {

	  /**
	   *
	   */
	  private static final long serialVersionUID = -8490323668001935723L;
	  private static final String Default_Project = "Default";
	  private static String Global_Project = "GLOBAL";
	  private static String OWNERSHIP_PROJECT = "ownership.project";
	  private static String OWNERSHIP_ORGANIZATION = "ownership.organization";
	  private static String OWNERSHIP_ACCESS = "ownership.access";
	  private static String OWNERSHIP_COMMENT = "ownership.comment";
      static
      {
          DomainObject.MULTI_VALUE_LIST.add(OWNERSHIP_PROJECT);
          DomainObject.MULTI_VALUE_LIST.add(OWNERSHIP_ORGANIZATION);
          DomainObject.MULTI_VALUE_LIST.add(OWNERSHIP_ACCESS);
          DomainObject.MULTI_VALUE_LIST.add(OWNERSHIP_COMMENT);
      }
	  /**
	   * @param context
	   * @param args
	   * @throws Exception
	   */
	  public emxSecurityDefaultProjectOwnershipMigrationBase_mxJPO(Context context,
	      String[] args) throws Exception {
	    super(context, args);
	  }
	  
	   /**
	     * This method writes the objectId to the sequential file, called from within JPO query where clause
	     *
	     * @param context the eMatrix <code>Context</code> object
	     * @param args[]  - [0]ObjectId, [1]type
	     * @returns boolean
	     * @throws Exception if the operation fails
	     */
	    public String writeObjectId(Context context, String[] args) throws Exception
	    {

	        //String project = args[1];
	        String objectId = args[0];
	        StringList projects = (new DomainObject(objectId)).getInfoList(context, "ownership.project");
	        if ( projects.contains(Default_Project) )
	        {
	        	return objectId;
	        }
	        return null;
	    }
	  
	  
	    @SuppressWarnings({ "deprecation" })
	    public void migrateObjects(Context context, StringList objectList) throws Exception
	    {
	      	mqlLogRequiredInformationWriter("In emxSecuritySecurityDefaultProjectMigrationObjects 'migrateObjects' method "+"\n");
	      	
	      	StringList mxObjectSelects = new StringList(5);
	      	mxObjectSelects.addElement("id");
	      	mxObjectSelects.addElement("type");
	      	mxObjectSelects.addElement("name");
	      	mxObjectSelects.addElement(OWNERSHIP_PROJECT);
	      	mxObjectSelects.addElement(OWNERSHIP_ORGANIZATION);
	      	mxObjectSelects.addElement(OWNERSHIP_COMMENT);
	      	mxObjectSelects.addElement(OWNERSHIP_ACCESS);
	      	String[] oidsArray = new String[objectList.size()];
	        oidsArray = (String[])objectList.toArray(oidsArray);
	        try
	        {
	        	ContextUtil.pushContext(context);
	        	Global_Project = DomainAccess.getDefaultProject(context);
	        	MapList mapList = DomainObject.getInfo(context, oidsArray, mxObjectSelects);
	        	Iterator<?> itr = mapList.iterator();
	        	while(itr.hasNext())
	        	{
	        		Map<?, ?> m = (Map<?, ?>)itr.next();
	        		mqlLogWriter(m.toString());
	        		String objectId = (String)m.get("id");
		            String objectName = (String)m.get("name");
		            StringList projects = (StringList)m.get(OWNERSHIP_PROJECT);
		            StringList orgs = (StringList)m.get(OWNERSHIP_ORGANIZATION);
		            StringList comments = (StringList)m.get(OWNERSHIP_COMMENT);
		            StringList accesses = (StringList)m.get(OWNERSHIP_ACCESS);
		            
		            mqlLogRequiredInformationWriter("==============================================================================");
		            mqlLogRequiredInformationWriter("Start Migrating Object "+ objectId +" with Name as " + objectName);	            
		            String comment = "";
		            if(projects!= null && projects.contains(Default_Project) && Global_Project != null)
		            {
		            	if( (projects.size() == orgs.size()) && (projects.size() == comments.size()) && (projects.size() == accesses.size()) )		            		
		            	{
		            		Iterator<String> projectItr = projects.iterator();
		            		Iterator<String> orgItr = orgs.iterator();
		            		Iterator<String> commentItr = comments.iterator();
		            		Iterator<String> accessItr = accesses.iterator();
		            		while(projectItr.hasNext())
		            		{
		            			String project = (String)projectItr.next();
		            			String org = (String)orgItr.next();
		            			String ownershipcomment = (String)commentItr.next();
		            			String access = (String)accessItr.next();
		            			if(Default_Project.equals(project) )
		            			{
		            		        String command = "modify bus $1 remove ownership $2 $3 for $4";
		            	        	MqlUtil.mqlCommand(context, command, objectId, org, Default_Project, ownershipcomment);
		            	            command = "modify bus " + objectId + " add ownership '" + org + "' '" + Global_Project + "' for '" + ownershipcomment + "' as " + access;
		            	            MqlUtil.mqlCommand(context, command);
		            			}
		            		}
    		            	loadMigratedOids(objectId);
		            	} else {
		            		comment = "Object "+ objectId +" with Name as " + objectName + " does not have ownership vector defined properly. \n";
		            		writeUnconvertedOID(comment, objectId);
		            	}
		            } else {		            	
		            	comment = "Object "+ objectId +" with Name as " + objectName + " does not have default project as ownership project so doesn't need any migration. \n";
		            	mqlLogRequiredInformationWriter(comment);
		            	loadMigratedOids(objectId);
		            }		         
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
	  
	    public void mqlLogRequiredInformationWriter(String command) throws Exception
	    {
	        super.mqlLogRequiredInformationWriter(command +"\n");
	    }
	  
	    public void mqlLogWriter(String command) throws Exception
	    {
	        super.mqlLogWriter(command +"\n");
	    }
}
