
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Plc_TestCase_ExportCompletion_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__Class_div_Test_Case_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Test Case");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__TestCase_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("TestCase");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__plc_nav_tc_subs_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("plc_nav_tc_subs");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__plc_nav_tc_docs_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("plc_nav_tc_docs");
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
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsSubTCs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsDocsTC = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsTCs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsAllTCsAndRels = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsOnlyTCs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsDocs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsOnlyDocs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsDocsAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsTCs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__Class_div_Test_Case_ ) );
		RsSubTCs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__TestCase_, _STRING_2__plc_nav_tc_subs_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsTCs } ) );
		IdsAllTCsAndRels.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsSubTCs ) );
		IdsOnlyTCs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsAllTCsAndRels, _STRING_0__Class_div_Test_Case_ ) );
		RsDocsTC.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__TestCase_, _STRING_3__plc_nav_tc_docs_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsOnlyTCs } ) );
		IdsDocs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsDocsTC ) );
		IdsOnlyDocs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsDocs, _STRING_4__Class_div_DOCUMENTS_ ) );
		IdsDocsAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_5__DocumentCompletion_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsOnlyDocs } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsAllTCsAndRels ), IdsDocs ), IdsDocsAndScope ) );
	}
}
