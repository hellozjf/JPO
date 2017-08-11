
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Rmt_ReqGroup_ExportCompletion_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__Class_div_Requirement = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Requirement Group");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__Requirement_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__rmt_nav_reqgroup_stru = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_reqgroup_structure");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__rmt_nav_reqgroup_chil = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_reqgroup_children");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__Class_div_Requirement = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Requirement Specification");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__Rmt_ReqSpec_ExportCom = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_ReqSpec_ExportCompletion");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsReqGroupsStruct = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsReqGroupsContent = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqGroupsIn = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqGroupsStruct = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqGroupsAll = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsContent = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqSpecs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqSpecsAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsReqGroupsIn.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__Class_div_Requirement ) );
		RsReqGroupsStruct.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_2__rmt_nav_reqgroup_stru, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsReqGroupsIn } ) );
		IdsReqGroupsStruct.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsReqGroupsStruct ) );
		IdsReqGroupsAll.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsReqGroupsStruct, _STRING_0__Class_div_Requirement ) );
		RsReqGroupsContent.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_3__rmt_nav_reqgroup_chil, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsReqGroupsAll } ) );
		IdsContent.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsReqGroupsContent ) );
		IdsReqSpecs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsContent, _STRING_4__Class_div_Requirement ) );
		IdsReqSpecsAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_5__Rmt_ReqSpec_ExportCom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsReqSpecs } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsReqGroupsStruct ), IdsContent ), IdsReqSpecsAndScope ) );
	}
}
