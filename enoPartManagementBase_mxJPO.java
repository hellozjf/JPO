import java.util.HashMap;

import com.dassault_systemes.enovia.partmanagement.modeler.constants.PartMgtConstants;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.dvo.IPart;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.input.IPartIngress;
import com.dassault_systemes.enovia.partmanagement.modeler.interfaces.services.IPartService;
import com.dassault_systemes.enovia.partmanagement.modeler.util.ECMUtil;
import com.dassault_systemes.enovia.partmanagement.modeler.util.PartMgtUtil;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;

import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;


public class enoPartManagementBase_mxJPO {

	public enoPartManagementBase_mxJPO(Context context, String[] args) throws Exception {

	}

	/**
	 * To create the part object from create component
	 *
	 * @param context
	 * @param args
	 * @return Map
	 * @throws Exception
	 * @Since R419
	 */
	@com.matrixone.apps.framework.ui.CreateProcessCallable
	public int checkPartDeleteAllowed(Context context, String [] args) throws Exception {
		String objectId = args[0];
		String relationshipName = args[1];
		
		Boolean isChildPart = PartMgtUtil.isConnectedAsChild(context,objectId,PartMgtUtil.getActualSchemaName(context,relationshipName));
		if (isChildPart ) {
			String message = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(), "emxEngineeringCentral.DeletePart.CheckPartConnectedAsChild");
			throw new Exception(message);
		}
		
		Boolean isPartLastRev =PartMgtUtil.isLatestRevision(context,objectId);
		if(!isPartLastRev){
			String message = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(), "emxEngineeringCentral.DeletePart.CheckPartConnectedNotLastRevision");
			throw new Exception(message);
		}
		
		Boolean isPartConnectedToChange = ECMUtil.isConnectedToActiveChange(context,objectId);
		if(isPartConnectedToChange){
			String errormessage = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(), "emxEngineeringCentral.DeletePart.CheckPartConnectedToChange");
            throw new Exception(errormessage);
		}
		
		return 0;
	}

	   /**
     * This method is used as Table Program to fetch the Raw material data for a context Part.(Make From Relationship)
     * @param context
     * 	The Context Object represents the User context
     * @param args
     * 	The String[] object represents the arguments passed
     * @return
     * 	MapList Objects consists of Raw Material's Id and Relation Ship Id
     * @throws Exception
     * 	If any
     * @From 2017x FD02
     */
	@SuppressWarnings("rawtypes")
	@com.matrixone.apps.framework.ui.ProgramCallable
	  public MapList getRawMaterials(Context context,String[] args) throws Exception{
		  HashMap programMap = JPO.unpackArgs(args);
	      String objectId = (String)programMap.get("objectId");
	
	      IPartService iPartService = IPartService.getService(context,objectId);
	      return iPartService.getRawMaterials(context, new StringList(PartMgtConstants.SELECT_ID), null);
	  }  
	/**
	 * This method is used to add the raw materials to the context part. And it is called from emxEngineeringAlternatePartsAddExistingProcess.jsp"
	 * as a JPO invoke
	 * @param context
	 * 	The Context object represents the user context
	 * @param args
	 * 	The String[] object represents the arguments passed.
	 * @throws
	 * 	Exception if an error
	 */
	@SuppressWarnings("rawtypes")
	public void addRawMaterials(Context context,String[] args) throws Exception{

			HashMap programMap = JPO.unpackArgs(args);
			String objectId = (String)programMap.get("objectId");
			String[] emxTableRawIds = (String[])programMap.get("emxTableRowId");
			StringList rawMaterialObjects =  PartMgtUtil.getListFromSelectedTableRowIds(emxTableRawIds, 1);
			IPartService iPartService = IPartService.getService(context,objectId);
			IPartIngress iPartIngress =  IPartIngress.getService();
			iPartIngress.setRawMaterialObjectIds(rawMaterialObjects);
			iPartService.addRawMaterials(context, iPartIngress);
	}
/**
 * This method is used to remove the connected raw materials from the Context part. and 
 * It is called from the emxEngrTableDisconnectProcess.jsp  as a JPO invoke
 * @param context
 * 	The Context Object represents the user context.
 * @param args
 * 		The String[] object represents the arguments passed.
 * @throws Exception	
 * 	Exception if any error.
 */
	@SuppressWarnings("rawtypes")
	public void removeRawMaterials(Context context,String[] args) throws Exception {
		
			HashMap programMap = JPO.unpackArgs(args);
			String objectId = (String)programMap.get("objectId");
			String[] emxTableRawIds = (String[])programMap.get("emxTableRowId");
			StringList rawMaterialObjects =  PartMgtUtil.getListFromSelectedTableRowIds(emxTableRawIds, 1);			
			IPartService iPartService = IPartService.getService(context,objectId);
			IPartIngress iPartIngress =  IPartIngress.getService();
			iPartIngress.setRawMaterialObjectIds(rawMaterialObjects);
			iPartService.removeRawMaterials(context, iPartIngress);
	}
      
  /**
   * This Program/Function used as excludeOID program in "Add Make From" functionality
   * @param context Context 
   * 		The Context Object represents the User Context
   * @param args 
   * 	The String[] object presents the parameter having Part's details
   * @return 
   * 	The StringList object consists of value of exclude connected Raw Materials (Make From)
   * @throws Exception 
   * 	if searching Parts object fails.
   * @ from 17x FD02
   */
  @SuppressWarnings("rawtypes")
  @com.matrixone.apps.framework.ui.ExcludeOIDProgramCallable
  public StringList excludeConnectedRawMaterialOIDs(Context context, String[] args)throws Exception
  {
	  HashMap params = (HashMap)JPO.unpackArgs(args);
	  String objectId = (String)params.get("objectId");

	  StringList resultExclude=new StringList();

	  IPartService iPartService = IPartService.getService(context,objectId);
	  resultExclude = PartMgtUtil.getSListForTheKey(
			 								iPartService.getRawMaterials(context, new StringList(PartMgtConstants.SELECT_ID), null), 
			 								PartMgtConstants.SELECT_ID);
	  resultExclude.add(objectId); //Exclude context part
	  
	  return resultExclude;
  }
  /**
   * To display the structure content value for part in properties page.
   * If already Synchronized, displays the V_Name of the corresponding product and links to the Product.
   * @param context
   * @param args
   * @return String
   * @throws Exception
   */

  public String getStructureContent(Context context, String[] args) throws Exception{
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      HashMap requestMap = (HashMap) programMap.get("requestMap");
      String partId = (String)requestMap.get("objectId");
      String strContentValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.StructureContent.StandAlone");
      StringList slSelectables = new StringList(2);
      slSelectables.add("to["+PartMgtConstants.RELATIONSHIP_EBOM+"]");
      slSelectables.add("from["+PartMgtConstants.RELATIONSHIP_EBOM+"]");
      String sPartDetails = MqlUtil.mqlCommand(context,"print bus $1 select $2 $3 dump $4",partId,"to["+PartMgtConstants.RELATIONSHIP_EBOM+"]", "from["+PartMgtConstants.RELATIONSHIP_EBOM+"]","|");
      String[] slPartDetail = sPartDetails.split("[|]");
      String sToEBOMConnection = slPartDetail[0];
      String sFromEBOMConnection = slPartDetail[1];
      if("True".equalsIgnoreCase(sToEBOMConnection) && "True".equalsIgnoreCase(sFromEBOMConnection)){
      	strContentValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.StructureContent.Intermediate");
      }
      else if("False".equalsIgnoreCase(sToEBOMConnection) && "True".equalsIgnoreCase(sFromEBOMConnection)){
      	strContentValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.StructureContent.Root");
      }
      else if("True".equalsIgnoreCase(sToEBOMConnection) && "False".equalsIgnoreCase(sFromEBOMConnection)){
      	strContentValue = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.StructureContent.Leaf");
      }
      return strContentValue;
  }
  
  /**
   * To display if the part is Configured or Not.
   * If already Synchronized, displays the V_Name of the corresponding product and links to the Product.
   * @param context
   * @param args
   * @return String
   * @throws Exception
   */
  public String getConfiguredVal(Context context, String[] args) throws Exception{
      HashMap programMap = (HashMap) JPO.unpackArgs(args);
      HashMap requestMap = (HashMap) programMap.get("requestMap");
      String partId = (String)requestMap.get("objectId");
      IPart part = IPartService.getInfo(context, partId);
      String isConfigured = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Value.False");
      if(part.isPolicyClassification_Unresolved()) {
      	isConfigured = EnoviaResourceBundle.getProperty(context, "emxEngineeringCentralStringResource", context.getLocale(),"emxEngineeringCentral.Value.True");;
      }
      return isConfigured;
  }
  
  /** This Method checks if the attribute is classified on the Part, returns true if it is classified and attribute exists on it.
  * returns false if it is classified and attribute does not exists. 
  * @param context enovia context
  * @param partId part object Id 
  * @return boolean
  * @throws Exception if any operation fails.
  */
  private boolean isClassifiedAttributeExist(Context context, String partId) throws Exception {
      DomainObject domObject = DomainObject.newInstance(context, partId);
      
      StringList sList = domObject.getInfoList(context, "to[" + PropertyUtil.getSchemaProperty(context, "relationship_ClassifiedItem") + "].from.attribute[mxsysInterface]");
      
      boolean classifiedAttrExists = false;
      for (int i = 0; i < sList.size(); i++) {
    	  String strinterface = (String) sList.get(i);
    	  if(PartMgtUtil.isNotNullAndNotEmpty(strinterface)){
	    	  String mqlOutPut = MqlUtil.mqlCommand(context, "print interface $1 select $2 dump $3", (String) sList.get(i), "allparents.derived", "|");
	    	  
	    	  if ( PartMgtUtil.isNotNullAndNotEmpty(mqlOutPut) ) {
	    		  if ( FrameworkUtil.split(mqlOutPut, "|").contains("Classification Attribute Groups") ) {
		    		  classifiedAttrExists = true;
		    		  break;
	    		  }
	    	  }
    	  }
      }
      
	  return classifiedAttrExists;
  }
  
  /** This method is called from Part properties page Classification field Access Program, Used to hide or display the field based on Classified attribute exists or not.
 * @param context enovia context
 * @param args arguments passed from UI.
 * @return true if Classified Item exists else returns false.
 * @throws Exception if any operation fails.
 */
  public boolean classifiedAttributeExist(Context context, String [] args) throws Exception {
	  HashMap programMap = (HashMap) JPO.unpackArgs(args);
	  
	  String partId = (String) programMap.get("objectId");
	  
	  return isClassifiedAttributeExist(context, partId);
  }
  
}
