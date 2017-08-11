
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.RelationshipType;

import com.dassault_systemes.requirements.ReqConstants;
import com.dassault_systemes.requirements.ReqSchemaUtil;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

/*
* @quickreview LX6 QYG 13 Dec 12 IR-198252V6R2014 NHIV6R215: Function_036266: Object in release state are not getting copied and imported. 
*/

public class emxRMTConnectCloneObjectBase_mxJPO extends emxDomainObject_mxJPO {

	/**
	 * 
	 * @param context
	 *            the eMatrix <code>Context</code> object
	 * @param args
	 *            holds no arguments
	 * @throws Exception
	 *             if the operation fails
	 * @since WhereUsed R212
	 * @grade 0
	 */
	public emxRMTConnectCloneObjectBase_mxJPO (Context context, String[] args) throws Exception{
		super(context,args);
	}
	
	/**
	 * @param args
	 */
	public int mxMain(Context context, String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		return 0;
	}

	public int connectCloneObject(Context context, String[] args) throws Exception {
		// Argument check
		if (context == null) {
			throw new IllegalArgumentException("context");
		}
		if (args == null || args.length < 1) {
			throw new IllegalArgumentException("args");
		}
		String isCopyWithLink = "";
		String CopyWithLinkValue = "";
		String isFromWebApp = "";
		isCopyWithLink = PropertyUtil.getGlobalRPEValue(context, ReqConstants.COPY_WITH_LINK);
		//used to now if this trigger is called from CATIA or from the web app
		isFromWebApp = PropertyUtil.getGlobalRPEValue(context, ReqConstants.FROM_WEB_APP);
		if(isCopyWithLink.equals("true")||(isFromWebApp.equals("true") == false)){
			String strObjectId = args[0];
			String strCloneType = args[1];
			String strCloneName = args[2];
			String strCloneRevision = args[3];
			String strCloneClone = args[4];
			BusinessObject bus;    
			DomainObject Original;
			DomainObject Clone;
			RelationshipType Rel;
			try {
				//START LX6 QYG IR-198252V6R2014 NHIV6R215: Function_036266: Object in release state are not getting copied and imported.
				ContextUtil.pushContext(context);
				//END LX6 QYG IR-198252V6R2014 NHIV6R215: Function_036266: Object in release state are not getting copied and imported.
				//creation of the business object clone
				bus = new BusinessObject(strCloneType,strCloneName,strCloneRevision,strCloneClone);
				//creation a the relationship Clone
				Rel = new RelationshipType(ReqSchemaUtil.getCloneRelationship(context));
				//Creation of clone domaniObject
				Clone = new DomainObject(bus);
				//creation of original object
				Original = new DomainObject(strObjectId);
				//Connection between original and clone
				DomainRelationship.connect(context, Original, Rel, Clone);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//START LX6 QYG IR-198252V6R2014 NHIV6R215: Function_036266: Object in release state are not getting copied and imported.
			finally
			{
				ContextUtil.popContext(context);
			}
			//END LX6 QYG IR-198252V6R2014 NHIV6R215: Function_036266: Object in release state are not getting copied and imported.
		}
		return 0;
	}
}
