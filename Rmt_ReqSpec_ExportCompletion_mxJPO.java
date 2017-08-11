
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Rmt_ReqSpec_ExportCompletion_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__Class_div_Requirement = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Requirement Specification");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__Requirement_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__rmt_nav_reqspec_child = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_reqspec_children");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__DocumentCompletion_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DocumentCompletion");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__Class_div_Chapter_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Chapter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__Rmt_Chapter_ExportCom = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_Chapter_ExportCompletion");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__Class_div_Requirement = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__Rmt_Requirement_Expor = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_Requirement_ExportCompletion");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsReqSpecChildren = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqSpec = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqSpecChildren = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChapters = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChaptersAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqsAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqSpecVersions = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsReqSpec.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__Class_div_Requirement ) );
		RsReqSpecChildren.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_2__rmt_nav_reqspec_child, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsReqSpec } ) );
		IdsReqSpecChildren.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsReqSpecChildren ) );
		IdsReqSpecVersions.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_3__DocumentCompletion_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsReqSpec } ) );
		IdsChapters.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsReqSpecChildren, _STRING_4__Class_div_Chapter_ ) );
		IdsChaptersAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_5__Rmt_Chapter_ExportCom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsChapters } ) );
		IdsReqs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsReqSpecChildren, _STRING_6__Class_div_Requirement ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsChaptersAndScope, _STRING_6__Class_div_Requirement ) ) );
		IdsReqsAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_7__Rmt_Requirement_Expor, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsReqs } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsReqSpecChildren ), IdsReqSpecVersions ), IdsChaptersAndScope ), IdsReqsAndScope ) );
	}
}
