import com.matrixone.apps.domain.util.PropertyUtil;

import matrix.db.Context;
import matrix.util.StringList;


public class emxLibraryCentralMigrateToMultipleOwnershipFindObjectsBase_mxJPO extends emxCommonFindObjects_mxJPO{

	public emxLibraryCentralMigrateToMultipleOwnershipFindObjectsBase_mxJPO(
			Context context, String[] args) throws Exception {
		super(context, args);
	}
	
	public int mxMain(Context context, String[] args) throws Exception {
		
		if (args.length < 3 )
        {
            throw new IllegalArgumentException("minimum 3 arguments are required to proceed");
        }
		
		StringList allowedTypes = new StringList();
		allowedTypes.add(PropertyUtil.getSchemaProperty(context,"type_Libraries"));
		allowedTypes.add(PropertyUtil.getSchemaProperty(context,"type_GeneralLibrary"));
		allowedTypes.add(PropertyUtil.getSchemaProperty(context,"type_PartLibrary"));
		allowedTypes.add(PropertyUtil.getSchemaProperty(context,"type_DocumentLibrary"));
		allowedTypes.add(PropertyUtil.getSchemaProperty(context,"type_Classification"));
		allowedTypes.add(PropertyUtil.getSchemaProperty(context,"type_GeneralClass"));
		allowedTypes.add(PropertyUtil.getSchemaProperty(context,"type_PartFamily"));
		allowedTypes.add(PropertyUtil.getSchemaProperty(context,"type_DocumentFamily"));
		allowedTypes.add(PropertyUtil.getSchemaProperty(context,"type_GenericDocument"));
		allowedTypes.add(PropertyUtil.getSchemaProperty(context,"type_ProjectVault"));
		
		if(!allowedTypes.contains(args[1])){
			throw new IllegalArgumentException("Invalid type");
		}
		
		String[] newArgs = args;
		if(args.length < 4){
			newArgs = new String[4];
			newArgs[0] = args[0];
			newArgs[1] = args[1];
			newArgs[2] = args[2];
		}
		newArgs[3] = "emxLibraryCentralMigrateToMultipleOwnership";
		
		return super.mxMain(context,newArgs);
	}
}
