
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class DIFStandardRep_ExportSelf_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__DIFStandard_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFStandard");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__DifStandardModeler_Ad = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DifStandardModeler_AddStandard");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMDifStandardRepSet = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifStandardRepIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMDifStandardRepSet.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__DIFStandard_, _STRING_1__DifStandardModeler_Ad, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { iPLMIDSet } ) );
		PLMDifStandardRepIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMDifStandardRepSet ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMDifStandardRepIDSet ) );
	}
}
