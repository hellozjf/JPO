import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;


public class emxSecurityDefaultProjectMigrationBase_mxJPO extends emxCommonMigration_mxJPO {

	  /**
	   *
	   */
	  private static final long serialVersionUID = -8490323668001935723L;
	  private static final String Default_Project = "Default";
	  private static String Global_Project = "GLOBAL";

	  /**
	   * @param context
	   * @param args
	   * @throws Exception
	   */
	  public emxSecurityDefaultProjectMigrationBase_mxJPO(Context context,
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
	    public boolean writeOID(Context context, String[] args) throws Exception
	    {

	        //String project = args[1];
	        String objectId = args[0];
	        String project = (new DomainObject(objectId)).getInfo(context, "project");
	        if ( Default_Project.equals(project) )
	        {
	            _objectidList.addElement(args[0]);
	            _counter++;

	            if (_counter == _chunk)
	            {
	                _counter=0;
	                _sequence++;

	                //write oid from _objectidList
	                for (int s=0;s<_objectidList.size();s++)
	                {
	                    _fileWriter.write((String)_objectidList.elementAt(s));
	                    _fileWriter.newLine();
	                }

	                _objectidList=new StringList();
	                _fileWriter.close();

	                //create new file
	                _oidsFile = new java.io.File(documentDirectory + "objectids_" + _sequence + ".txt");
	                _fileWriter = new BufferedWriter(new FileWriter(_oidsFile));
	            }
	        }

	        return false;
	    }


	  @SuppressWarnings({ "unchecked", "deprecation" })
	    public void migrateObjects(Context context, StringList objectList) throws Exception
	    {
	      	mqlLogRequiredInformationWriter("In emxSecuritySecurityDefaultProjectMigrationObjects 'migrateObjects' method "+"\n");

	      	StringList mxObjectSelects = new StringList(5);
	      	mxObjectSelects.addElement("id");
	      	mxObjectSelects.addElement("type");
	      	mxObjectSelects.addElement("name");
	      	mxObjectSelects.addElement("project");
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
		            String project = (String)m.get("project");
		            mqlLogRequiredInformationWriter("==============================================================================");
		            mqlLogRequiredInformationWriter("Start Migrating Object "+ objectId +" with Name as " + objectName + " with Project as "+ project);
		            boolean unconverted = false;
		            String comment = "";
		            if(Default_Project.equals(project) && Global_Project != null)
		            {
		            	String cmd = "mod bus $1 project $2";
		            	MqlUtil.mqlCommand(context, cmd, objectId, Global_Project);
		            } else {
		              unconverted = true;
		              comment = "object  "+ objectName + " is not connected to any organization with Employee relationship. \n";
		            }

		            if( unconverted )
		            {
		            	writeUnconvertedOID(comment, objectId);
		            } else {
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
