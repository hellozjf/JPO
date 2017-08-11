/**
 *  enoEBOMAutoSyncBase
 *  JPO for publishing and synchronizing Part/EBOM data with VPLM.
 */

import java.util.Map;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.engineering.EBOMAutoSync;
import com.matrixone.apps.engineering.EngineeringConstants;
import com.matrixone.apps.engineering.EngineeringUtil;
import com.matrixone.apps.framework.ui.UIUtil;

import matrix.db.Context;

@SuppressWarnings({"unchecked","rawtypes"})
public class enoEBOMAutoSyncBase_mxJPO {

	 /**
	  * Synchronizes the given Child part and its EBOM to VPM
	  * e.g. for the structure shown below, C is the given part Id and syncDepth is 1 level then Parts C, D, E and C-->D, C-->E EBOM rels will be processed for sync 
	  * A
	  * |______B
	  * |______C
	  * 	   |_____D
	  *        |_____E
	  *        
	  * @param context
	  * @param args
	  *  - args[0] takes a Parent part or from part id in EBOM
	  *  - args[1] takes a child part or to part id in EBOM
	  *  - args[2] Takes sync depth like "0" or "1", "2" for EBOM level to sync. Note that "All" level can also be specified to sync all level ebom data
	  * 
	  * @return 0 as success irrespective of sync result
	  * @throws Exception
	  */
 
	 public static int syncChildAndItsEBOMToVPM(Context context, String[] args) throws Exception {
		 
		//If AUTO SYNC ENABLED PROPERTY is false, AUTO SYNC FUNCTIONALITY will be avoided
		if(EBOMAutoSync.isAutoSyncDisabled(context)){
			return 0;
		}		 		 
		if (UIUtil.isNullOrEmpty(args[0]) || UIUtil.isNullOrEmpty(args[1]) || UIUtil.isNullOrEmpty(args[2]))
		{
			throw new FrameworkException("Invalid arguments passed, please check the arguments once");
		}		
		try {
			new EBOMAutoSync().syncChildAndItsEBOMToVPM(context, args[0], args[1], args[2]);			
		}
		catch (Exception ex) {
			ex.printStackTrace();
			//throw new FrameworkException(ex.getMessage());
		}
		return 0;
	 }
  
	 /**
	  * Synchronizes the child parts in a bom markup data
	  * e.g. Consider below data is markup , in that B & C were add operations and saved as markup
	  * 	 Once markup approved, action trigger PolicyPartMarkupStateProposedPromoteAction invokes this method.
	  *      This method verifies whether A is already synchronized and has connection to the VPM control and also control is with EBOM
	  *      Then proceeds to synchronized the child parts B, C.     
	  *       
	  *      This method synchronizes the new incoming child parts only for add/replace mark ups   
	  * A
	  * |_ _ _ B  Add operation +
	  * |_ _ _ C  Add operation +
	  *        
	  * @param context
	  * @param args
	  *  - args[0] takes a Mark up object id
	  *  - args[1] takes a type of the mark up
	  *  - args[2] Takes sync depth like "0" or "1", "2" for EBOM level to sync. Note that "All" level can also be specified to sync all level ebom data
	  * 
	  * @return HashTable of result contain detailed reports of sync performed
	  * @throws Exception
	  */
 
	 public static int syncChildPartsForApprovedMarkups(Context context, String[] args) throws Exception {
		//If AUTO SYNC ENABLED PROPERTY is false, AUTO SYNC FUNCTIONALITY will be avoided
		if(EBOMAutoSync.isAutoSyncDisabled(context)){
			return 0;
		}		 		 
		if (UIUtil.isNullOrEmpty(args[0]) || UIUtil.isNullOrEmpty(args[1]) || UIUtil.isNullOrEmpty(args[2]) || UIUtil.isNullOrEmpty(args[3]))
		{
			throw new FrameworkException("Invalid arguments passed, please check the arguments once");
		}
		try {
			new EBOMAutoSync().syncChildPartsForApprovedMarkups(context, args[0], args[1], args[2], args[3]);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return 0;
	 }
	 
	 /**
	  * Synchronizes the given Connection id and its siblings of this connection parent based on given depth
	  * e.g. for the structure shown below, A-->B rel is the given connection id passed here (Parent is considered as A) and syncDepth is 1 level then Parts A, B, C and A-->B, A-->C EBOM rels will be processed for sync 
	  * A
	  * |______B
	  * |______C
	  * 	   |_____D
	  *        |_____E
	  *        
	  * @param context
	  * @param args
	  *  - args[0] takes EBOM Connection Id
	  *  - args[1] Takes sync depth like "0" or "1", "2" for EBOM level to sync. Note that "All" level can also be specified to sync all level ebom data
	  * 
	  * @return HashTable of result contain detailed reports of sync performed
	  * @throws Exception
	  */
	 
	 public static int syncBOMToVPM(Context context, String[] args) throws Exception {
		//If AUTO SYNC ENABLED PROPERTY is false, AUTO SYNC FUNCTIONALITY will be avoided
		if(EBOMAutoSync.isAutoSyncDisabled(context)){
			return 0;
		}		 		 

		if (UIUtil.isNullOrEmpty(args[0]) || UIUtil.isNullOrEmpty(args[1]))
		{
			throw new FrameworkException("Invalid arguments passed, please check the arguments once");
		}
		try {
			new EBOMAutoSync().syncBOMToVPM(context, args[0], args[1]);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	 }
	 
	 /**
	  * Synchronizes the given Parent part and its EBOM to VPM
	  * e.g. for the structure shown below, A is the given part Id and syncDepth is 1 level then Parts A, B, C and A-->B, A-->C EBOM rels will be processed for sync 
	  * A
	  * |______B
	  * |______C
	  * 	   |_____D
	  *        |_____E
	  *        
	  * @param context
	  * @param args
	  *  - args[0] takes Parent Part Id
	  *  - args[1] Takes sync depth like "0" or "1", "2" for EBOM level to sync. Note that "All" level can also be specified to sync all level ebom data
	  * 
	  * @return HashTable of result contain detailed reports of sync performed
	  * @throws Exception
	  */
	 
	 public static int syncParentAndItsEBOMToVPM(Context context, String[] args) throws Exception {
		//If AUTO SYNC ENABLED PROPERTY is false, AUTO SYNC FUNCTIONALITY will be avoided
		if(EBOMAutoSync.isAutoSyncDisabled(context)){
			return 0;
		}		 		 		 
		if (UIUtil.isNullOrEmpty(args[0]) || UIUtil.isNullOrEmpty(args[1]))
		{
			throw new FrameworkException("Invalid arguments passed, please check the arguments once");
		}
		try {
			new EBOMAutoSync().syncParentAnditsEBOMToVPM(context, args[0], args[1]);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	 }
	 
	 /**
	  * Synchronizes the incoming child part and its structure along with the parent and its structure
	  * e.g. for the structure shown below, C is the given part Id going to be replaced with part X and syncDepth is 1 level then at first, Parts C, XX, YY and C-->XX, C-->YY EBOM rels will be processed for sync
	  *      and then A--> B, A--X rels will be processed.  
	  * A
	  * |______B
	  * |______C  <----X if C is getting replaced with X
	  * 		       |_____XX
	  *                |_____YY	  
	  *        
	  * @param context
	  * @param args
	  *  - args[0] takes a Parent part or from part id in EBOM
	  *  - args[1] new coming object id coming through replace operation
	  *  - args[2] Takes sync depth like "0" or "1", "2" for EBOM level to sync. Note that "All" level can also be specified to sync all level ebom data
	  * 
	  * @return HashTable of result contain detailed reports of sync performed
	  * @throws Exception
	  */
 
	 public static int syncParentAndItsEBOMToVPMAfterReplaceOperation(Context context, String[] args) throws Exception {
		//If AUTO SYNC ENABLED PROPERTY is false, AUTO SYNC FUNCTIONALITY will be avoided
		if(EBOMAutoSync.isAutoSyncDisabled(context)){
			return 0;
		}		 		 
		if (UIUtil.isNullOrEmpty(args[0]) || UIUtil.isNullOrEmpty(args[1]) || UIUtil.isNullOrEmpty(args[2]))
		{
			throw new FrameworkException("Invalid arguments passed, please check the arguments once");
		}		
		try {
			new EBOMAutoSync().syncParentAndItsEBOMToVPMAfterReplaceOperation(context, args[0], args[1], args[2]);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			throw new FrameworkException(ex.getMessage());
		}
		return 0;
	 }
	 
	 /**
	  * Synchronizes the incoming child part and its structure along with the parent and its structure
	  * e.g. for the structure shown below, C is the given part Id going to be replaced with part X and syncDepth is 1 level then at first, Parts C, XX, YY and C-->XX, C-->YY EBOM rels will be processed for sync
	  *      and then A--> B, A--X rels will be processed.  
	  * A
	  * |______B
	  * |______C  <----X if C is getting replaced with X
	  * 		       |_____XX
	  *                |_____YY	  
	  *        
	  * @param context
	  * @param args
	  *  - args[0] takes a Parent part or from part id in EBOM
	  *  - args[1] new coming object id coming through replace operation
	  *  - args[2] Takes sync depth like "0" or "1", "2" for EBOM level to sync. Note that "All" level can also be specified to sync all level ebom data
	  * 
	  * @return HashTable of result contain detailed reports of sync performed
	  * @throws Exception
	  */
 
	 public static int autoSyncNewRevisionToVPM(Context context, String[] args) throws Exception {
		//If AUTO SYNC ENABLED PROPERTY is false, AUTO SYNC FUNCTIONALITY will be avoided
		if(EBOMAutoSync.isAutoSyncDisabled(context)){
			return 0;
		}		 		 
		if (UIUtil.isNullOrEmpty(args[0]) || UIUtil.isNullOrEmpty(args[1]))
		{
			throw new FrameworkException("Invalid arguments passed, please check the arguments once");
		}		
		try {
			String productState = MqlUtil.mqlCommand(context, "print bus $1 select $2 dump",args[0],"from["+DomainConstants.RELATIONSHIP_PART_SPECIFICATION+"|to.type.kindof["+EngineeringConstants.TYPE_VPLM_CORE_REF+"]]" + ".to.current");
			
			if("OBSOLETE".equalsIgnoreCase(productState)) {
                String strMessage = EngineeringUtil.i18nStringNow(context,"EBOMAutoSync.PartRevise.AutoSyncCannotBePerformedForObsoleteProduct",context.getSession().getLanguage());
                emxContextUtil_mxJPO.mqlNotice(context,strMessage);
                return 0; // Return should be success, in order to proceed with regular Part revise operation
			}			
			String nextRevisionID = DomainObject.newInstance(context, args[0]).getInfo(context, "next.id");
			new EBOMAutoSync().syncParentAnditsEBOMToVPM(context, nextRevisionID, args[1]);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;			
	 }
	 
}




