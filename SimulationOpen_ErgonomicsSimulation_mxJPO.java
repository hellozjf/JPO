
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class SimulationOpen_ErgonomicsSimulation_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__SIMObjSimulationGener = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SIMObjSimulationGeneric");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__sim_retrieveCategorie = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("sim_retrieveCategories");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__sim_addSimulatedModel = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("sim_addSimulatedModel");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__SIMObjSimulationCateg = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SIMObjSimulationCategoryAndProdCnx/SIMObjSimulationCategoryReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__PLMDocConnection_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__PLMDocConnection_retr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection_retrieveAllDocuments");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__sim_retrieveSimuRep_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("sim_retrieveSimuRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__sim_retrieveExternalD = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("sim_retrieveExternalDocumentfromSimuRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__SIMObjSimulationV5Gen = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SIMObjSimulationV5Generic/SIMObjSimulationV5RepReferenceGeneric");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__sim_addPublishResultC = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("sim_addPublishResultCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__PRODUCTCFG_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__ProductCfg_AddChildr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ProductCfg_AddChildrenProduct");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__last_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("last");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__PRODUCTCFG_div_VPMRe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__VPMEditor_OpenComple = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("VPMEditor_OpenCompletionOnReference");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsCategoriesAndSo = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsSetModel = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProductStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsExtDoc = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsSimuRep = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsExtSimuRepDoc = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsPublishResult = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsProdAndChildren = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RouteSetCategoriesAndSo = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RouteSetModel = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RouteSetExtDoc = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RouteSimuRep = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RouteSetExtSimuRepDoc = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RoutePublishResult = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RouteSetProdAndChildren = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		RouteSetCategoriesAndSo.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__SIMObjSimulationGener, _STRING_1__sim_retrieveCategorie, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { iPLMIDSet } ) );
		IdsCategoriesAndSo.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSetCategoriesAndSo ) );
		RouteSetModel.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__SIMObjSimulationGener, _STRING_2__sim_addSimulatedModel, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsCategoriesAndSo, _STRING_3__SIMObjSimulationCateg ) } ) );
		IdsSetModel.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSetModel ) );
		RouteSetExtDoc.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_4__PLMDocConnection_, _STRING_5__PLMDocConnection_retr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsCategoriesAndSo } ) );
		IdsExtDoc.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSetExtDoc ) );
		RouteSimuRep.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__SIMObjSimulationGener, _STRING_6__sim_retrieveSimuRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsCategoriesAndSo, _STRING_3__SIMObjSimulationCateg ) } ) );
		IdsSimuRep.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSimuRep ) );
		RouteSetExtSimuRepDoc.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__SIMObjSimulationGener, _STRING_7__sim_retrieveExternalD, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsSimuRep, _STRING_8__SIMObjSimulationV5Gen ) } ) );
		IdsExtSimuRepDoc.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSetExtSimuRepDoc ) );
		RoutePublishResult.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__SIMObjSimulationGener, _STRING_9__sim_addPublishResultC, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsCategoriesAndSo, _STRING_3__SIMObjSimulationCateg ) } ) );
		IdsPublishResult.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RoutePublishResult ) );
		RouteSetProdAndChildren.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_10__PRODUCTCFG_, _STRING_11__ProductCfg_AddChildr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSetModel, _STRING_12__last_ ), _STRING_13__PRODUCTCFG_div_VPMRe ) } ) );
		IdsProdAndChildren.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RouteSetProdAndChildren ) );
		IdsProductStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_14__VPMEditor_OpenComple, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsProdAndChildren } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( IdsCategoriesAndSo, IdsSetModel ), IdsExtDoc ), IdsSimuRep ), IdsSimuRep ), IdsExtSimuRepDoc ), IdsPublishResult ), IdsProdAndChildren ), IdsProductStructure ) );
	}
}
