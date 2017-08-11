
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class DEL_BlendingTableCompletion_XPDM_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__Class_div_DEL_Blendin = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/DEL_BlendingTable");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__DELBlendingTableModel = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELBlendingTableModel");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__DELFctBT_IsBlending_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELFctBT_IsBlending");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__DELFctBT_IsSuppliedBy = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELFctBT_IsSuppliedBy");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__DELFctBT_IsConsumed_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELFctBT_IsConsumed");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__DELFctBT_IsComposedOf = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELFctBT_IsComposedOf");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__DELFctBR_IsCombinatin = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELFctBR_IsCombinating");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__Class_div_DEL_Blendin = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/DEL_BlendingRule");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__DELPPRContextModel_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELPPRContextModel");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__ENOPpr_PPRData_addAll = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ENOPpr_PPRData_addAllFathers");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__DELLmiProductionPlug = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DELLmiProductionPlug/DELLmiProductionPlugReference");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet InputPLMIDSetRestrictedToRefs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesBlendingMaterial = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfPLMIDsSuppliers = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfPLMIDsCustomers = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesSuppliers = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesCustomers = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesBlendingRules = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfPLMIDsBlendingRules = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesCombinatingSuppliers = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesSuppliersFathers = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesCustomersFathers = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		InputPLMIDSetRestrictedToRefs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__Class_div_DEL_Blendin ) );
		SetOfPLMRoutesBlendingMaterial.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__DELBlendingTableModel, _STRING_2__DELFctBT_IsBlending_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfPLMRoutesSuppliers.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__DELBlendingTableModel, _STRING_3__DELFctBT_IsSuppliedBy, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfPLMIDsSuppliers.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesSuppliers ) );
		SetOfPLMRoutesCustomers.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__DELBlendingTableModel, _STRING_4__DELFctBT_IsConsumed_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfPLMIDsCustomers.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesCustomers ) );
		SetOfPLMRoutesBlendingRules.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__DELBlendingTableModel, _STRING_5__DELFctBT_IsComposedOf, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfPLMIDsBlendingRules.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesBlendingRules ) );
		SetOfPLMRoutesCombinatingSuppliers.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__DELBlendingTableModel, _STRING_6__DELFctBR_IsCombinatin, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfPLMIDsBlendingRules, _STRING_7__Class_div_DEL_Blendin ) } ) );
		SetOfPLMRoutesSuppliersFathers.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_8__DELPPRContextModel_, _STRING_9__ENOPpr_PPRData_addAll, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfPLMIDsSuppliers, _STRING_10__DELLmiProductionPlug ) } ) );
		SetOfPLMRoutesCustomersFathers.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_8__DELPPRContextModel_, _STRING_9__ENOPpr_PPRData_addAll, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfPLMIDsCustomers, _STRING_10__DELLmiProductionPlug ) } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesBlendingMaterial ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesSuppliers ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesCustomers ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesBlendingRules ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesCombinatingSuppliers ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesSuppliersFathers ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesCustomersFathers ) ) );
	}
}
