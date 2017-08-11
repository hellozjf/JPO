
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Functional_ExportFlowInst_Remove_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RFLPLMFlow_div_RFLPLM = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFlow/RFLPLMFlowInstance");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet InputPLMIDSetRestrictedToFlowInstances = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfConfigObjectsPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		InputPLMIDSetRestrictedToFlowInstances.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RFLPLMFlow_div_RFLPLM ) );
		oPLMIDSet.setValue( iPLMIDSet );
	}
}
