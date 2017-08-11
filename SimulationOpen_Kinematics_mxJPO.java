
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class SimulationOpen_Kinematics_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__SIMObjSimulationGener = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SIMObjSimulationGeneric");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__sim_retrieveCategorie = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("sim_retrieveCategories");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__PLMDocConnection_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__PLMDocConnection_retr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection_retrieveAllDocuments");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__PLMParameter_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMParameter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__PAR_nav_paramports_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PAR_nav_paramports");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__sim_addSimulatedModel = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("sim_addSimulatedModel");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__SIMObjSimulationCateg = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SIMObjSimulationCategoryAndProdCnx/SIMObjSimulationCategoryReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__PRODUCTCFG_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__ProductCfg_AddChildre = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddChildrenProduct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__last_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("last");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__PRODUCTCFG_div_VPMRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__VPMEditor_OpenComple = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMEditor_OpenCompletionOnReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__sim_retrieveSimuRep_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("sim_retrieveSimuRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__sim_addPublishResult = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("sim_addPublishResultCnxForOpen");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsCategoriesAndSo = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsExtDoc = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsSimuRep = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProdAndChildren = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsPublishResult = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsSetModel = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsPortParam = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RouteSetCategoriesAndSo = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RouteSetModel = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RouteSetExtDoc = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RouteSimuRep = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RouteSetProdAndChildren = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RoutePublishResult = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RoutePortParam = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		RouteSetCategoriesAndSo.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__SIMObjSimulationGener, _STRING_1__sim_retrieveCategorie, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { iPLMIDSet } ) );
		IdsCategoriesAndSo.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSetCategoriesAndSo ) );
		RouteSetExtDoc.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_2__PLMDocConnection_, _STRING_3__PLMDocConnection_retr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsCategoriesAndSo } ) );
		IdsExtDoc.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSetExtDoc ) );
		RoutePortParam.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_4__PLMParameter_, _STRING_5__PAR_nav_paramports_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsCategoriesAndSo } ) );
		IdsPortParam.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RoutePortParam ) );
		RouteSetModel.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__SIMObjSimulationGener, _STRING_6__sim_addSimulatedModel, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsCategoriesAndSo, _STRING_7__SIMObjSimulationCateg ) } ) );
		IdsSetModel.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSetModel ) );
		RouteSetProdAndChildren.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_8__PRODUCTCFG_, _STRING_9__ProductCfg_AddChildre, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSetModel, _STRING_10__last_ ), _STRING_11__PRODUCTCFG_div_VPMRe ) } ) );
		IdsProdAndChildren.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSetProdAndChildren ) );
		IdsProductStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_12__VPMEditor_OpenComple, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsProdAndChildren } ) );
		RouteSimuRep.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__SIMObjSimulationGener, _STRING_13__sim_retrieveSimuRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsCategoriesAndSo, _STRING_7__SIMObjSimulationCateg ) } ) );
		IdsSimuRep.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSimuRep ) );
		RoutePublishResult.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__SIMObjSimulationGener, _STRING_14__sim_addPublishResult, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsCategoriesAndSo, _STRING_7__SIMObjSimulationCateg ) } ) );
		IdsPublishResult.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RoutePublishResult ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( IdsCategoriesAndSo, IdsExtDoc ), IdsPortParam ), IdsSetModel ), IdsProdAndChildren ), IdsProductStructure ), IdsSimuRep ), IdsPublishResult ) );
	}
}
