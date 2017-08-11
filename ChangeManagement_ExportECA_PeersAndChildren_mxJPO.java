
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class ChangeManagement_ExportECA_PeersAndChildren_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMChgActionBase_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMChgActionBase/PLMActionBase");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__PLMChgActionBase_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMChgActionBase");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__PLMChgActionBase_GetA = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMChgActionBase_GetAggregated");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__PLMChgPeer_div_PLMChg = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMChgPeer/PLMChgPeer");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__PLMChgActionBase_GetP = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMChgActionBase_GetPeers");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetAggregated = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetAggregated = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetPeers = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetPeers = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		PLMIDSetAggregated.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__PLMChgActionBase_div_ ) );
		PLMRouteSetAggregated.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMChgActionBase_, _STRING_2__PLMChgActionBase_GetA, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetAggregated } ) );
		PLMIDSetPeers.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetAggregated ), _STRING_3__PLMChgPeer_div_PLMChg ) );
		PLMRouteSetPeers.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMChgActionBase_, _STRING_4__PLMChgActionBase_GetP, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetPeers } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetAggregated ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetPeers ) ) );
	}
}
