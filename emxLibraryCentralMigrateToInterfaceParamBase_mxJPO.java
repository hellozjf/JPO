import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.BufferedWriter;
import java.io.FileWriter;

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
import com.matrixone.apps.library.LibraryCentralConstants;
import com.matrixone.vplm.ParamInterfaces.ParamResourceInterfaces; 
import com.matrixone.vplm.ParamInterfaces.ParamLiveAttributeDefinition;

import matrix.db.Context;
import matrix.util.StringList;


public class emxLibraryCentralMigrateToInterfaceParamBase_mxJPO extends emxCommonMigration_mxJPO{

	
    public boolean unconvertable = false;
    public String unConvertableComments = "";

	//Param defintion arguments
    public static final String RESOURCEID_PREFIX = "/tenant 2.0 ?" ;
    public static final String ITFID_SUFFIX="itfIdentifier";
    public static final String APPTYPE = "CBP";//appType VPM or CBP
    public static final String REGSUIT = "";
    public static final String NLSKEY="AttributeGroup";
    public static final String TYPEID = "botypefield_classificationAttributes";//typeID : this will be used while adding attributes
    public static final String COLLABAPPS="IPClassificationAndSecurity";//collabApps
	public static final String CLASSIFICATION = "Classification";


	private void init(Context context) throws FrameworkException{

	}
	
	public emxLibraryCentralMigrateToInterfaceParamBase_mxJPO(Context context, String[] args) throws Exception {
		super(context, args);
		init(context);
	}
	
	public void migrateObjects(Context context, StringList agNames) throws Exception
      {
		 mqlLogRequiredInformationWriter("In emxLibraryCentralMigrateToInterfaceParamBase 'migrateObjects' method "+"\n");
      
		
			MapList result = new MapList(agNames.size());
			Iterator agNameIter = agNames.iterator();
			while (agNameIter.hasNext()) {
				String var = null;
				var = System.getenv("Param_AttributeCreationIndexing");
			    if (var != null && var.equals("1"))
			    {
					ParamLiveAttributeDefinition[] paramLiveAttributeDefinition = new ParamLiveAttributeDefinition[1];
					String agName = (String) agNameIter.next();
					mqlLogRequiredInformationWriter("Started Migrating Interface "+ agName + "\n");
					paramLiveAttributeDefinition[0] = new ParamLiveAttributeDefinition(agName,agName+ITFID_SUFFIX,APPTYPE,
							CLASSIFICATION,TYPEID,REGSUIT,NLSKEY,COLLABAPPS);
					
					ParamResourceInterfaces pItf = new ParamResourceInterfaces();
					ContextUtil.pushContext(context);
					int resultn = pItf.addAttributeConfigurationResource(context,RESOURCEID_PREFIX+agName,paramLiveAttributeDefinition);
					ContextUtil.popContext(context);
				
					if(resultn == -1)
					{
						unConvertableComments = "Migration for Interface "+agName+" failed \n" ;
						writeUnconvertedOID(unConvertableComments,agName);
					}
				}

		  }
		 // return 0;
	  }
}
