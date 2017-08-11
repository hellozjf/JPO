
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class PhysicalResource_GetAllEntities_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PRODUCTCFG_div_VPMRef = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__PRODUCTCFG_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__ProductCfg_AddChildre = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddChildrenProduct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__all_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("all");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__VPMEditor_GetAllRepre = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMEditor_GetAllRepresentations");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__DELPPRContextModel_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELPPRContextModel");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__ENOPcx_PPRContext_add = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOPcx_PPRContext_addAllConnections");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__DELRmiAcceptedPackagi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELRmiAcceptedPackagingCnx/DELRmiAcceptedPackagingCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__ENORsc_Resource_addAc = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENORsc_Resource_addAcceptedPackagings");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__PRODUCTCFG_div_VPMRep = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__ENORsc_Resource_addB = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENORsc_Resource_addBehaviorLibraryReps");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary/CATSysBehaviorLibRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary_GetRefLibFromRepLib");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary/CATSysBehaviorLibReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__SystemsBehavior_GetD = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SystemsBehavior_GetDependencies");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetProduct = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetAcceptedPack = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetPortCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetBehaviorLibraryReps = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetRepLib = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDProductRep = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetA = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet BehaviorLibraryReps = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDPortCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDProduct = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDProductRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDAcceptedPackCnx = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDAcceptedPack = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDProductRepRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDRepLib = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet restrictedPLMIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		restrictedPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__PRODUCTCFG_div_VPMRef ) );
		PLMRouteSetProduct.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PRODUCTCFG_, _STRING_2__ProductCfg_AddChildre, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { restrictedPLMIDSet } ) );
		PLMIDProduct.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetProduct, _STRING_3__all_ ) );
		PLMIDProductRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetProduct ), _STRING_0__PRODUCTCFG_div_VPMRef ), restrictedPLMIDSet ) );
		PLMIDProductRep.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_4__VPMEditor_GetAllRepre, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDProductRef } ) );
		PLMRouteSetPortCnx.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__DELPPRContextModel_, _STRING_6__ENOPcx_PPRContext_add, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDProductRef } ) );
		PLMIDPortCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetPortCnx, _STRING_3__all_ ) );
		PLMIDAcceptedPackCnx.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetPortCnx ), _STRING_7__DELRmiAcceptedPackagi ) );
		PLMRouteSetAcceptedPack.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__DELPPRContextModel_, _STRING_8__ENORsc_Resource_addAc, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDAcceptedPackCnx } ) );
		PLMIDAcceptedPack.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetAcceptedPack, _STRING_3__all_ ) );
		PLMIDProductRepRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDProductRep, _STRING_9__PRODUCTCFG_div_VPMRep ) );
		PLMRouteSetBehaviorLibraryReps.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__DELPPRContextModel_, _STRING_10__ENORsc_Resource_addB, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDProductRepRef } ) );
		BehaviorLibraryReps.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetBehaviorLibraryReps, _STRING_3__all_ ), _STRING_11__CATSysBehaviorLibrar ) );
		PLMRouteSetRepLib.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_12__CATSysBehaviorLibrar, _STRING_13__CATSysBehaviorLibrar, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { BehaviorLibraryReps } ) );
		PLMIDRepLib.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetRepLib, _STRING_3__all_ ), _STRING_14__CATSysBehaviorLibrar ) );
		PLMIDSetA.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_15__SystemsBehavior_GetD, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDRepLib } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDProduct ), PLMIDProductRep ), PLMIDAcceptedPack ), BehaviorLibraryReps ), PLMIDRepLib ), PLMIDPortCnx ), PLMIDSetA ) );
	}
}
