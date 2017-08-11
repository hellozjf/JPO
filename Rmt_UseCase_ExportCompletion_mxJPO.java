
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Rmt_UseCase_ExportCompletion_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__Class_div_Use_Case_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Use Case");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__Requirement_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__rmt_nav_uc_subs_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_uc_subs");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__rmt_nav_uc_docs_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_uc_docs");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__Class_div_DOCUMENTS_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/DOCUMENTS");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__DocumentCompletion_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DocumentCompletion");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsSubUCs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsDocs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsUCs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsAllUCsAndRels = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsOnlyUCs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsDocs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsOnlyDocs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsDocsAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsUCs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__Class_div_Use_Case_ ) );
		RsSubUCs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_2__rmt_nav_uc_subs_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsUCs } ) );
		IdsAllUCsAndRels.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsSubUCs ) );
		IdsOnlyUCs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsAllUCsAndRels, _STRING_0__Class_div_Use_Case_ ) );
		RsDocs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_3__rmt_nav_uc_docs_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsOnlyUCs } ) );
		IdsDocs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsDocs ) );
		IdsOnlyDocs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsDocs, _STRING_4__Class_div_DOCUMENTS_ ) );
		IdsDocsAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_5__DocumentCompletion_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsOnlyDocs } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsAllUCsAndRels ), IdsDocs ), IdsDocsAndScope ) );
	}
}
