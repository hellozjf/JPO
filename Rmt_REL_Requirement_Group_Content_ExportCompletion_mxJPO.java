
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Rmt_REL_Requirement_Group_Content_ExportCompletion_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RelationClass_div_Req = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RelationClass/Requirement Group Content");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__Requirement_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__rmt_nav_rel_req_grp_c = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_rel_req_grp_content");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__Rmt_ReqSpec_ExportCom = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_ReqSpec_ExportCompletion");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsToEntities = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsRels = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsToEntities = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqSpecAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsRels.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RelationClass_div_Req ) );
		RsToEntities.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_2__rmt_nav_rel_req_grp_c, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsRels } ) );
		IdsToEntities.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsToEntities ) );
		IdsReqSpecAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_3__Rmt_ReqSpec_ExportCom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsToEntities } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsReqSpecAndScope ) );
	}
}
