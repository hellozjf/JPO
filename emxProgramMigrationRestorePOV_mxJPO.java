import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
import com.matrixone.apps.program.ProgramCentralUtil;

/**
 * @author RVW
 *
 */
public class emxProgramMigrationRestorePOV_mxJPO extends
    emxCommonMigration_mxJPO {

    // Class variables
    //
    private static final long serialVersionUID = -8490323668001935723L;

    private static final String KEYWORD_WAS = "was:";
    private static final String KEYWORD_GLOBAL = "GLOBAL";
    private static final String SELECT_CHANGE_PROJECT_HISTORY = "history.changeproject";
    private static final String SELECT_CHANGE_ORG_HISTORY = "history.changeorganization";

    private static final String SELECT_ID = "id";
    private static final String SELECT_TYPE = "type";
    private static final String SELECT_NAME = "name";
    private static final String SELECT_REVISION = "revision";
    private static final String SELECT_PROJECT = "project";
    private static final String SELECT_ORG = "organization";

  /**
     * @param context
     * @param args
     * @throws Exception
     */
    public emxProgramMigrationRestorePOV_mxJPO(Context context,
        String[] args) throws Exception {
      super(context, args);
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    @Override
    public void migrateObjects(Context context, StringList objectList) throws Exception
    {
        // business object  Project Space CV1 11434656215813
        //    history.changeproject = change project - user: VPLMAdminUser  time: 6/18/2015 3:41:16 PM  state: Create  project:   was: Default
        //                            0123456789
        //    history.changeorganization = change organization - user: VPLMAdminUser  time: 6/18/2015 3:41:16 PM  state: Create  organization:   was: MyCompany

        // mqlLogRequiredInformationWriter("In emxSecurityMigrationMigrateProgramObjects 'migrateObjects' method "+"\n");

        // init(context);

        StringList mxObjectSelects = new StringList(6);
        mxObjectSelects.addElement(SELECT_ID);
        mxObjectSelects.addElement(SELECT_TYPE);
        mxObjectSelects.addElement(SELECT_NAME);
        mxObjectSelects.addElement(SELECT_PROJECT);
        mxObjectSelects.addElement(SELECT_ORG);
        mxObjectSelects.addElement(SELECT_CHANGE_PROJECT_HISTORY);
        mxObjectSelects.addElement(SELECT_CHANGE_ORG_HISTORY);


        String[] oidsArray = new String[objectList.size()];
        oidsArray = (String[])objectList.toArray(oidsArray);
        MapList projectList = DomainObject.getInfo(context, oidsArray, mxObjectSelects);

        try{
            DomainObject prjObject = DomainObject.newInstance(context);
            Map projectMap;
            String projectId;
            String projectName;
            String historyChangeProject;
            String historyChangeOrg;
            String prevProject = "";
            String prevOrg = "";
            String currentProject,currentOrg;
            int endIndex = -1;

            ContextUtil.pushContext(context);
            String cmd = "trigger off";
            MqlUtil.mqlCommand(context, mqlCommand,  cmd);

            for (int i=0;i<projectList.size();i++)
            {
                projectMap = (Map)projectList.get(i);
                projectId = (String)projectMap.get(SELECT_ID);
                projectName = (String)projectMap.get(SELECT_NAME);
                currentProject = (String)projectMap.get(SELECT_PROJECT);
                currentOrg = (String)projectMap.get(SELECT_ORG);


                // System.out.println("projectMap="+projectMap);
                // System.out.println("projectId="+projectId);
                // System.out.println("projectName="+projectName);
                // System.out.println("currentProject = "+currentProject);
                // System.out.println("currentOrg = "+currentOrg);

                // If current org and project are not blank, nothing to do
                //
                if (!ProgramCentralUtil.isNullString(currentOrg) && !ProgramCentralUtil.isNullString(currentProject)) {
                    continue;
                }

                // Get the history record for changeproject and changeorganization
                //
                historyChangeProject = (String)projectMap.get(SELECT_CHANGE_PROJECT_HISTORY);
                historyChangeOrg = (String)projectMap.get(SELECT_CHANGE_ORG_HISTORY);

                // System.out.println("historyChangeProject="+historyChangeProject);
                // System.out.println("historyChangeOrg="+historyChangeOrg);

                endIndex=historyChangeProject.lastIndexOf(KEYWORD_WAS);

                if (endIndex != -1) {
                    prevProject = historyChangeProject.substring(endIndex+KEYWORD_WAS.length()).trim();
                    // System.out.println("Previous Project=" + "_"+ prevProject + "_");
                }

                endIndex=historyChangeOrg.lastIndexOf(KEYWORD_WAS);

                if (endIndex != -1) {
                    prevOrg = historyChangeOrg.substring(endIndex+KEYWORD_WAS.length()).trim();
                    // System.out.println("Previous Organization=" + "_" + prevOrg + "_");
                }

                if (!ProgramCentralUtil.isNullString(prevOrg) && !ProgramCentralUtil.isNullString(prevProject) && !KEYWORD_GLOBAL.equals(prevProject)) {
                    mqlLogRequiredInformationWriter("==============================================================================");
                    mqlLogRequiredInformationWriter("Restoring POV for object <<" + projectName + ">>");

                   // Set primary ownership on Project Space to the previous org/collab space
                   //
                   prjObject.setId(projectId);
                   prjObject.setPrimaryOwnership(context, prevProject,prevOrg);

                   // Add object to list of converted OIDs
                   //
                   loadMigratedOids(projectId);
                } else {
                    mqlLogRequiredInformationWriter("Skipping object <<" + projectName + ">>, NO MIGRATION NEEDED");

                    // Add object to list of unconverted OIDs
                    //
                    String comment = "Skipping object <<" + projectName + ">> NO MIGRATIION NEEDED";
                    writeUnconvertedOID(comment, projectId);
               }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            String cmd = "trigger on";
            MqlUtil.mqlCommand(context, mqlCommand,  cmd);
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
