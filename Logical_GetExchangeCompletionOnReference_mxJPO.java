
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Logical_GetExchangeCompletionOnReference_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RFLVPMLogical_div_RFL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical/RFLVPMLogicalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__RFLVPMLogical_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__RFLVPMLogical_AllLogO = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical_AllLogObjects4Exchange");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__RFLPLMImplementConnec = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__RFLPLMImplementConnec = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection_AddAllFctImplementedComponents");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__RFLPLMFunctional_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctional/RFLPLMFunctionalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__RFLPLMFlow_div_RFLPLM = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFlow/RFLPLMFlowReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__RFLPLMFunctional_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctional");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__RFLPLMFunctional_AllF = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctional_AllFctObjects4Exchange");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__RFLPLMImplementConnec = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection_AddAllReqImplementedComponents");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__Class_div_Requiremen = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Requirement Specification");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__Rmt_ReqSpec_ExportCo = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_ReqSpec_ExportCompletion");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__Class_div_Requiremen = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__Rmt_Requirement_Expo = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_Requirement_ExportCompletion");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__Class_div_Requiremen = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Requirement Group");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__Rmt_ReqGroup_ExportC = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_ReqGroup_ExportCompletion");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfAllLogObjects = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfAllLogObjPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfFctImplementedComponents = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfReqImplementedComponents = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfImplementFctRefPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfAllFctObjects = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfImplementReqSpecIds = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfAllReqSpecObjects = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfImplementReqIds = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfAllReqObjects = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfImplementReqGroupIds = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfAllReqGroupObjects = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet InputPLMIDSetRestricted = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		InputPLMIDSetRestricted.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RFLVPMLogical_div_RFL ) );
		SetOfAllLogObjects.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__RFLVPMLogical_, _STRING_2__RFLVPMLogical_AllLogO, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfAllLogObjPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfAllLogObjects ) );
		SetOfFctImplementedComponents.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_3__RFLPLMImplementConnec, _STRING_4__RFLPLMImplementConnec, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfAllLogObjPLMIDs } ) );
		SetOfImplementFctRefPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfFctImplementedComponents ), _STRING_5__RFLPLMFunctional_div_ ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfFctImplementedComponents ), _STRING_6__RFLPLMFlow_div_RFLPLM ) ) );
		SetOfAllFctObjects.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_7__RFLPLMFunctional_, _STRING_8__RFLPLMFunctional_AllF, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfImplementFctRefPLMIDs } ) );
		SetOfReqImplementedComponents.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_3__RFLPLMImplementConnec, _STRING_9__RFLPLMImplementConnec, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfAllLogObjPLMIDs } ) );
		SetOfImplementReqSpecIds.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfReqImplementedComponents ), _STRING_10__Class_div_Requiremen ) );
		SetOfAllReqSpecObjects.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_11__Rmt_ReqSpec_ExportCo, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfImplementReqSpecIds } ) );
		SetOfImplementReqIds.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfReqImplementedComponents ), _STRING_12__Class_div_Requiremen ) );
		SetOfAllReqObjects.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_13__Rmt_Requirement_Expo, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfImplementReqIds } ) );
		SetOfImplementReqGroupIds.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfReqImplementedComponents ), _STRING_14__Class_div_Requiremen ) );
		SetOfAllReqGroupObjects.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_15__Rmt_ReqGroup_ExportC, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfImplementReqGroupIds } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( SetOfAllLogObjPLMIDs, com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfAllFctObjects ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfFctImplementedComponents ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfReqImplementedComponents ) ), SetOfAllReqSpecObjects ), SetOfAllReqObjects ), SetOfAllReqGroupObjects ) );
	}
}
