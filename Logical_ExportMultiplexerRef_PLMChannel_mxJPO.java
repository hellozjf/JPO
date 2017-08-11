
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Logical_ExportMultiplexerRef_PLMChannel_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RFLVPMLogicalCommunic = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalCommunication/RFLVPMLogicalCommunicationReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__RFLVPMLogical_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__RFLVPMLogical_Get1stL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical_Get1stLevelObjects");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__RFLVPMSystemType_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMSystemType/RFLVPMSystemTypeReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__Logical_ExportType_PL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Logical_ExportType_PLMChannel");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__Config_GetStructConfi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Config_GetStructConfig");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet InputPLMIDSetRestrictedToRefs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesMuxObjects = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfMuxObjectsPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfTypeRefsPLMIDsToExpand = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfTypesPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfConfigObjectsPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		InputPLMIDSetRestrictedToRefs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RFLVPMLogicalCommunic ) );
		SetOfPLMRoutesMuxObjects.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__RFLVPMLogical_, _STRING_2__RFLVPMLogical_Get1stL, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfMuxObjectsPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesMuxObjects ) );
		SetOfTypeRefsPLMIDsToExpand.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfMuxObjectsPLMIDs, _STRING_3__RFLVPMSystemType_div_ ) );
		SetOfTypesPLMIDs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_4__Logical_ExportType_PL, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfTypeRefsPLMIDsToExpand } ) );
		SetOfConfigObjectsPLMIDs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_5__Config_GetStructConfi, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, SetOfMuxObjectsPLMIDs ), SetOfTypesPLMIDs ), SetOfConfigObjectsPLMIDs ) );
	}
}
