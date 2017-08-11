
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class ESE_LogRef_GetEngSpecDependencies_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMEnsSpecSpecificati = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecSpecification");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__ESE_AddLogToSpecConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_AddLogToSpecConnectionForExport");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__RFLVPMLogical_div_RFL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical/RFLVPMLogicalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__ESE_SpecLogRefToSpec_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecLogRefToSpec");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__ESE_SpecCategories_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecCategories");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__PLMEnsSpecSpecificati = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecSpecification/EnsSpecification");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__ESE_AddFilterBuiltFro = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_AddFilterBuiltFromConnectionForExport");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PLMEnsSpecPartsFilter = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecPartsFilter/EnsFilter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__ESE_AddFilterExtensio = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_AddFilterExtensionDefConnectionForExport");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__ESE_SpecEPFRep_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecEPFRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__ESE_AddTableBuiltFro = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_AddTableBuiltFromConnectionForExport");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__PLMEnsSpecTechnoTabl = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecTechnoTable/EnsTechnologicalTable");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__ESE_SpecETTRep_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecETTRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__ESE_SpecEPFBuiltFrom = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecEPFBuiltFromToETT");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__ESE_SpecETTBuiltFrom = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecETTBuiltFromToETT");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev0LogToSpecCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1Specifications = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1Categories = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev2FilterBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev2FilterExtensionDefCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev2RepEPFs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev2TableBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev2RepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev3FilterBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev3FilterBuiltFromRefETTsCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev3FilterBuiltFromRefETTsRepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev3TableBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev3TableBuiltFromRefETTsCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev3TableBuiltFromRefETTsRepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev0LogToSpecCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1Specifications = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1Categories = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev2FilterBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev2FilterExtensionDefCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev2RepEPFs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev2TableBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev2RepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev3FilterBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev3FilterBuiltFromRefETTsCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev3FilterBuiltFromRefETTsRepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev3TableBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev3TableBuiltFromRefETTsCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev3TableBuiltFromRefETTsRepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMRouteSetLev0LogToSpecCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_1__ESE_AddLogToSpecConne, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__RFLVPMLogical_div_RFL ) } ) );
		PLMIDSetLev0LogToSpecCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev0LogToSpecCnxs ) );
		PLMRouteSetLev1Specifications.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_3__ESE_SpecLogRefToSpec_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__RFLVPMLogical_div_RFL ) } ) );
		PLMIDSetLev1Specifications.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1Specifications ) );
		PLMRouteSetLev1Categories.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_4__ESE_SpecCategories_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1Specifications, _STRING_5__PLMEnsSpecSpecificati ) } ) );
		PLMIDSetLev1Categories.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1Categories ) );
		PLMRouteSetLev2FilterBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_6__ESE_AddFilterBuiltFro, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1Categories, _STRING_7__PLMEnsSpecPartsFilter ) } ) );
		PLMIDSetLev2FilterBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev2FilterBuiltFromCnxs ) );
		PLMRouteSetLev2FilterExtensionDefCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_8__ESE_AddFilterExtensio, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1Categories, _STRING_7__PLMEnsSpecPartsFilter ) } ) );
		PLMIDSetLev2FilterExtensionDefCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev2FilterExtensionDefCnxs ) );
		PLMRouteSetLev2RepEPFs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_9__ESE_SpecEPFRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1Categories, _STRING_7__PLMEnsSpecPartsFilter ) } ) );
		PLMIDSetLev2RepEPFs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev2RepEPFs ) );
		PLMRouteSetLev2TableBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_10__ESE_AddTableBuiltFro, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1Categories, _STRING_11__PLMEnsSpecTechnoTabl ) } ) );
		PLMIDSetLev2TableBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev2TableBuiltFromCnxs ) );
		PLMRouteSetLev2RepETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_12__ESE_SpecETTRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1Categories, _STRING_11__PLMEnsSpecTechnoTabl ) } ) );
		PLMIDSetLev2RepETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev2RepETTs ) );
		PLMRouteSetLev3FilterBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_13__ESE_SpecEPFBuiltFrom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1Categories, _STRING_7__PLMEnsSpecPartsFilter ) } ) );
		PLMIDSetLev3FilterBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev3FilterBuiltFromRefETTs ) );
		PLMRouteSetLev3FilterBuiltFromRefETTsCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_10__ESE_AddTableBuiltFro, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev3FilterBuiltFromRefETTs, _STRING_11__PLMEnsSpecTechnoTabl ) } ) );
		PLMIDSetLev3FilterBuiltFromRefETTsCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev3FilterBuiltFromRefETTsCnxs ) );
		PLMRouteSetLev3FilterBuiltFromRefETTsRepETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_12__ESE_SpecETTRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev3FilterBuiltFromRefETTs, _STRING_11__PLMEnsSpecTechnoTabl ) } ) );
		PLMIDSetLev3FilterBuiltFromRefETTsRepETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev3FilterBuiltFromRefETTsRepETTs ) );
		PLMRouteSetLev3TableBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_14__ESE_SpecETTBuiltFrom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1Categories, _STRING_11__PLMEnsSpecTechnoTabl ) } ) );
		PLMIDSetLev3TableBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev3TableBuiltFromRefETTs ) );
		PLMRouteSetLev3TableBuiltFromRefETTsCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_10__ESE_AddTableBuiltFro, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev3TableBuiltFromRefETTs, _STRING_11__PLMEnsSpecTechnoTabl ) } ) );
		PLMIDSetLev3TableBuiltFromRefETTsCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev3TableBuiltFromRefETTsCnxs ) );
		PLMRouteSetLev3TableBuiltFromRefETTsRepETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_12__ESE_SpecETTRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev3TableBuiltFromRefETTs, _STRING_11__PLMEnsSpecTechnoTabl ) } ) );
		PLMIDSetLev3TableBuiltFromRefETTsRepETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev3TableBuiltFromRefETTsRepETTs ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDSetLev0LogToSpecCnxs ), PLMIDSetLev1Specifications ), PLMIDSetLev1Categories ), PLMIDSetLev2FilterBuiltFromCnxs ), PLMIDSetLev2FilterExtensionDefCnxs ), PLMIDSetLev2RepEPFs ), PLMIDSetLev2TableBuiltFromCnxs ), PLMIDSetLev2RepETTs ), PLMIDSetLev3FilterBuiltFromRefETTs ), PLMIDSetLev3FilterBuiltFromRefETTsCnxs ), PLMIDSetLev3FilterBuiltFromRefETTsRepETTs ), PLMIDSetLev3TableBuiltFromRefETTs ), PLMIDSetLev3TableBuiltFromRefETTsCnxs ), PLMIDSetLev3TableBuiltFromRefETTsRepETTs ) );
	}
}
