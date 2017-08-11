
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Logical_ExportServices_PLMChannel_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RFLVPMLogicalServices = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalServices/RFLVPMServicesReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__RFLVPMLogicalInterfac = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalInterfaceRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__RFLVPMLogicalInterfac = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalInterfaceRep_Expand");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet InputPLMIDSetRestrictedToRefs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMLogicalInterfaceRep = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		InputPLMIDSetRestrictedToRefs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RFLVPMLogicalServices ) );
		SetOfPLMLogicalInterfaceRep.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__RFLVPMLogicalInterfac, _STRING_2__RFLVPMLogicalInterfac, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMLogicalInterfaceRep ) ) );
	}
}
