import java.util.Iterator;
import java.util.List;
import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainAccess;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.DomainSymbolicConstants;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PersonUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.domain.util.StringUtil;

/**
 * @author JX5
 *
 */

 /**
 *	@quickreview JX5 02:28:2013 : Add Use Case migration, replace access All by VPLMCreator and VPLMProjectAdministrator
 */
public class emxRequirementRDOMigrationBase_mxJPO extends
		emxCommonMigrationBase_mxJPO {


	/**
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public emxRequirementRDOMigrationBase_mxJPO(Context context,
			String[] args) throws Exception {
		super(context, args);
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
    public void migrateObjects(Context context, StringList objectList) throws Exception
    {

    	StringList mxObjectSelects = new StringList(5);

		String DesignResponsibilityName 	= "to[Design Responsibility].from.name";
		String DesignResponsibilityType 	= "to[Design Responsibility].from.type";
		String DesignResponsibilityRelID	= "to[Design Responsibility].id";

    	mxObjectSelects.addElement("id");
		mxObjectSelects.addElement(DesignResponsibilityName);
		mxObjectSelects.addElement(DesignResponsibilityType);
		mxObjectSelects.addElement(DesignResponsibilityRelID);
    	String[] oidsArray = new String[objectList.size()];
        oidsArray = (String[])objectList.toArray(oidsArray);

        try
        {
        	String orgTypes = MqlUtil.mqlCommand(context,"print type $1 select $2 dump $3", "Organization", "derivative","|");
        	StringList orgTypeList = StringUtil.split(orgTypes,"|");
        	String projTypes = MqlUtil.mqlCommand(context,"print type $1 select $2 dump $3", "Project Space", "derivative","|");
        	StringList projTypeList = StringUtil.split(projTypes,"|");

	        MapList mapList = DomainObject.getInfo(context, oidsArray, mxObjectSelects);
	        Iterator<?> itr = mapList.iterator();

	        while(itr.hasNext())
	        {
	        	Map<?, ?> m 			 = (Map<?, ?>)itr.next();
	        	String ObjectID 		 = (String)m.get("id");
	        	String designResp		 = (String)m.get(DesignResponsibilityName);
				String designRespType	 = (String)m.get(DesignResponsibilityType);
				String designRespRelID	 = (String)m.get(DesignResponsibilityRelID);
				String Access			 = "Read and Relate";
				String Assignee			 = "";
				String RelID			 = "";

				DomainObject dObj = DomainObject.newInstance(context, ObjectID);

				String commentResult = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3",ObjectID,"ownership.comment","|");
				StringList commentList = StringUtil.split(commentResult,"|");

	        	//Design Responsibility relationship
				if(designResp != null && !designResp.equals(""))
				{
					if(orgTypeList.contains(designRespType))
					{
						//if RDOMigration has not been done yet
						if(!commentList.contains("RDOMigration"))
						{
							String command = "modify bus $1 add ownership $2 $3 for $4";
							MqlUtil.mqlCommand(context, command, ObjectID, designResp, "-", "RDOMigration");
						}
						// if relationship "Design Responsibility" is not disconnected yet we do it
						if(designRespRelID != null && !designRespRelID.equals(""))
						{
							DomainRelationship.disconnect(context, designRespRelID);
						}
					}
					else if(projTypeList.contains(designRespType))
					{
						//if RDOMigration has not been done yet
						if(!commentList.contains("RDOMigration"))
						{
							String command = "modify bus $1 add ownership $2 $3 for $4";
							MqlUtil.mqlCommand(context, command, ObjectID, "-", designResp, "RDOMigration");
						}
						// disconnect relationship if it still exist
						if(designRespRelID != null && !designRespRelID.equals("") && !designRespRelID.equals("null"))
						{
							DomainRelationship.disconnect(context, designRespRelID);
						}
					}
				}

				//Assigned Requirement relationship
				if(dObj.isKindOf(context, "Requirement"))
				{
					//retrieve the assignees List
					String resultID = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3",ObjectID, "relationship[Assigned Requirement].from.id","|");
					String resultRelID = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3",ObjectID, "relationship[Assigned Requirement].id","|");


					if(resultID != null && !resultID.equals(""))
					{
						StringList assignees = StringUtil.split(resultID,"|");
						StringList relIDs = StringUtil.split(resultRelID,"|");
						int assigneesNbr = assignees.size();


						for(int n = 0; n < assigneesNbr; n++)
						{
							Assignee	= (String)assignees.get(n);
							RelID		= (String)relIDs.get(n);

							// If Assignee Migration not Done
							if( !commentList.contains("AsigneeMigration"))
							{
								//add ownerhsip
								DomainAccess.createObjectOwnership(context, ObjectID, Assignee, Access, "AsigneeMigration");
							}

							// disconnect relationship if it still exist
							if(RelID != null && !RelID.equals("") && !RelID.equals("null"))
							{
								DomainRelationship.disconnect(context, RelID);
							}
						}
					}
				}
				//Assigned Use Case relationship
				if(dObj.isKindOf(context, "Use Case"))
				{
					//retrieve the assignees List
					String resultID = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3",ObjectID, "relationship[Assigned Use Case].from.id","|");
					String resultRelID = MqlUtil.mqlCommand(context,"print bus $1 select $2 dump $3",ObjectID, "relationship[Assigned Use Case].id","|");


					if(resultID != null && !resultID.equals(""))
					{
						StringList assignees 	= StringUtil.split(resultID,"|");
						StringList relIDs 		= StringUtil.split(resultRelID,"|");
						int assigneesNbr 		= assignees.size();


						for(int n = 0; n < assigneesNbr; n++)
						{
							Assignee	= (String)assignees.get(n);
							RelID		= (String)relIDs.get(n);

							// If Use Case Assignee Migration not Done
							if( !commentList.contains("UseCaseAsigneeMigration"))
							{
								//add ownerhsip
								DomainAccess.createObjectOwnership(context, ObjectID, Assignee, Access, "UseCaseAsigneeMigration");
							}

							// disconnect relationship if it still exist
							if(RelID != null && !RelID.equals("") && !RelID.equals("null"))
							{
								DomainRelationship.disconnect(context, RelID);
							}
						}
					}
				}

				loadMigratedOids(ObjectID);

	        }
        } catch(Exception ex)
        {
        	ex.printStackTrace();
            throw ex;
        }
    }

    /**
       * This method retrieve the  company name of a given user
       *
       * @param context the eMatrix <code>Context</code> object
       * @returns a String containing company name
       * @throws Exception if the operation fails
       */
	   public static String getCompanyNameFromPerson(Context context, String strPerson) throws Exception
      {

		try{

		DomainObject person = PersonUtil.getPersonObject(context, strPerson);
		String  selectPersonCompanyName = "to[" + PropertyUtil.getSchemaProperty(context, DomainSymbolicConstants.SYMBOLIC_relationship_Employee) + "].from.name";
		String companyName = person.getInfo(context, selectPersonCompanyName);
		return (companyName);
		}
          catch(Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }
      }


	  /**
       * This method give command access to VPLM users
       *
       * @param context the eMatrix <code>Context</code> object
       * @returns nothing
       * @throws Exception if the operation fails
       */
	   public void migrateCommandAccess(Context context, String[] args) throws Exception
      {
		String strCmdName = "";
		String strUserName = "";
		String result = "";
		int nbrUser = 0;

		try{

			String CmdQueryResult = MqlUtil.mqlCommand(context, "list $1 $2 select $3 $4 dump $5",true,"command","RMT*","name","user","|");
			List<?> CmdList = StringUtil.split(CmdQueryResult, "|","\n");
			Iterator<?> CmdItr = CmdList.iterator();

			while(CmdItr.hasNext())
			{
				StringList Cmd 	= (StringList)CmdItr.next();
				strCmdName		= (String)Cmd.get(0);
				nbrUser = Cmd.size();

				for (int i = 1 ; i < nbrUser ; i ++)
				{
					strUserName = (String)Cmd.get(i);

					if(strUserName.equals("Requirement Manager"))
					{
						result = MqlUtil.mqlCommand(context, "modify command $1 add user $2",true, strCmdName,"VPLMCreator");
						result = MqlUtil.mqlCommand(context, "modify command $1 add user $2",true, strCmdName,"VPLMProjectAdministrator");

					}
					else if(strUserName.equals("Employee"))
					{
						result = MqlUtil.mqlCommand(context, "modify command $1 add user $2",true,strCmdName,"VPLMViewer");

					}

				}

			}

		}

          catch(Exception ex)
          {
              ex.printStackTrace();
              throw ex;
          }
      }

		  /**
	       * This method give command access to Requirement Viewer
	       *
	       * @param context the eMatrix <code>Context</code> object
	       * @returns nothing
	       * @throws Exception if the operation fails
	       */
		   public void migrateViewerCommandAccess(Context context, String[] args) throws Exception
	      {
			String strCmdName = "";
			String strUserName = "";
			String result = "";
			int nbrUser = 0;

			try{

				String CmdQueryResult = MqlUtil.mqlCommand(context, "list $1 $2 select $3 $4 dump $5",true,"command","RMT*","name","user","|");
				List<?> CmdList = StringUtil.split(CmdQueryResult, "|","\n");
				Iterator<?> CmdItr = CmdList.iterator();

				while(CmdItr.hasNext())
				{
					StringList Cmd 	= (StringList)CmdItr.next();
					strCmdName		= (String)Cmd.get(0);
					nbrUser = Cmd.size();

					for (int i = 1 ; i < nbrUser ; i ++)
					{
						strUserName = (String)Cmd.get(i);

						if(strUserName.equals("VPLMViewer"))
						{
							result = MqlUtil.mqlCommand(context, "modify command $1 add user $2 remove setting $3",true,strCmdName,"Requirement Viewer", "Licensed Product");
						}

					}

				}

			}

	          catch(Exception ex)
	          {
	              ex.printStackTrace();
	              throw ex;
	          }
	      }

}
