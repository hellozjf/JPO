
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Logical_ExportTypeExpoInst_Remove_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RFLVPMSystemTypeExpos = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMSystemTypeExposition/RFLVPMSystemTypeExpositionInstance");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet InputPLMIDSetRestrictedToTypeExpoInstances = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfConfigObjectsPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		InputPLMIDSetRestrictedToTypeExpoInstances.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RFLVPMSystemTypeExpos ) );
		oPLMIDSet.setValue( iPLMIDSet );
	}
}
