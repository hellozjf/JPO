
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Rmt_Requirement_ExportCompletion_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__Class_div_Requirement = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__Requirement_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__rmt_nav_req_subs_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_req_subs");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__rmt_nav_req_params_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_req_params");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__rmt_nav_req_testcase_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_req_testcase");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__Class_div_Test_Case_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Test Case");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__Plc_TestCase_ExportCo = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Plc_TestCase_ExportCompletion");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__rmt_nav_req_docs_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_req_docs");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__Class_div_DOCUMENTS_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/DOCUMENTS");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__DocumentCompletion_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DocumentCompletion");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsSubReqs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsParams = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsTestCases = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsDocs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsAllReqsAndRels = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsOnlyReqs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsTestCases = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsOnlyTestCases = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsTestCasesAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsDocs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsOnlyDocs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsDocsAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsReqs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__Class_div_Requirement ) );
		RsSubReqs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_2__rmt_nav_req_subs_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsReqs } ) );
		IdsAllReqsAndRels.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsSubReqs ) );
		IdsOnlyReqs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsAllReqsAndRels, _STRING_0__Class_div_Requirement ) );
		RsParams.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_3__rmt_nav_req_params_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsOnlyReqs } ) );
		RsTestCases.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_4__rmt_nav_req_testcase_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsOnlyReqs } ) );
		IdsTestCases.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsTestCases ) );
		IdsOnlyTestCases.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsTestCases, _STRING_5__Class_div_Test_Case_ ) );
		IdsTestCasesAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_6__Plc_TestCase_ExportCo, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsOnlyTestCases } ) );
		RsDocs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_7__rmt_nav_req_docs_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsOnlyReqs } ) );
		IdsDocs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsDocs ) );
		IdsOnlyDocs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsDocs, _STRING_8__Class_div_DOCUMENTS_ ) );
		IdsDocsAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_9__DocumentCompletion_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsOnlyDocs } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsAllReqsAndRels ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsParams ) ), IdsTestCases ), IdsTestCasesAndScope ), IdsDocs ), IdsDocsAndScope ) );
	}
}
