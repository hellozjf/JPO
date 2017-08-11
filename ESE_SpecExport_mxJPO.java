
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class ESE_SpecExport_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMEnsSpecSpecificati = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecSpecification");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__ESE_SpecCategories_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecCategories");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__PLMEnsSpecSpecificati = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecSpecification/EnsSpecification");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__ESE_AddFilterBuiltFro = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_AddFilterBuiltFromConnectionForExport");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__PLMEnsSpecPartsFilter = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecPartsFilter/EnsFilter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__ESE_SpecEPFRep_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecEPFRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__ESE_AddTableBuiltFrom = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_AddTableBuiltFromConnectionForExport");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PLMEnsSpecTechnoTable = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecTechnoTable/EnsTechnologicalTable");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__ESE_SpecETTRep_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecETTRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__ESE_SpecEPFBuiltFromT = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecEPFBuiltFromToETT");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__ESE_SpecETTBuiltFrom = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecETTBuiltFromToETT");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev0Categories = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1FilterBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1RepEPFs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1TableBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1RepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev2FilterBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev2FilterBuiltFromRefETTsCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev2FilterBuiltFromRefETTsRepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev2TableBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev2TableBuiltFromRefETTsCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev2TableBuiltFromRefETTsRepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev0Categories = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1FilterBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1RepEPFs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1TableBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1RepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev2FilterBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev2FilterBuiltFromRefETTsCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev2FilterBuiltFromRefETTsRepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev2TableBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev2TableBuiltFromRefETTsCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev2TableBuiltFromRefETTsRepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMRouteSetLev0Categories.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_1__ESE_SpecCategories_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__PLMEnsSpecSpecificati ) } ) );
		PLMIDSetLev0Categories.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev0Categories ) );
		PLMRouteSetLev1FilterBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_3__ESE_AddFilterBuiltFro, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev0Categories, _STRING_4__PLMEnsSpecPartsFilter ) } ) );
		PLMIDSetLev1FilterBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1FilterBuiltFromCnxs ) );
		PLMRouteSetLev1RepEPFs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_5__ESE_SpecEPFRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev0Categories, _STRING_4__PLMEnsSpecPartsFilter ) } ) );
		PLMIDSetLev1RepEPFs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1RepEPFs ) );
		PLMRouteSetLev1TableBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_6__ESE_AddTableBuiltFrom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev0Categories, _STRING_7__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev1TableBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1TableBuiltFromCnxs ) );
		PLMRouteSetLev1RepETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_8__ESE_SpecETTRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev0Categories, _STRING_7__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev1RepETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1RepETTs ) );
		PLMRouteSetLev2FilterBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_9__ESE_SpecEPFBuiltFromT, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev0Categories, _STRING_4__PLMEnsSpecPartsFilter ) } ) );
		PLMIDSetLev2FilterBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev2FilterBuiltFromRefETTs ) );
		PLMRouteSetLev2FilterBuiltFromRefETTsCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_6__ESE_AddTableBuiltFrom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev2FilterBuiltFromRefETTs, _STRING_7__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev2FilterBuiltFromRefETTsCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev2FilterBuiltFromRefETTsCnxs ) );
		PLMRouteSetLev2FilterBuiltFromRefETTsRepETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_8__ESE_SpecETTRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev2FilterBuiltFromRefETTs, _STRING_7__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev2FilterBuiltFromRefETTsRepETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev2FilterBuiltFromRefETTsRepETTs ) );
		PLMRouteSetLev2TableBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_10__ESE_SpecETTBuiltFrom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev0Categories, _STRING_7__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev2TableBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev2TableBuiltFromRefETTs ) );
		PLMRouteSetLev2TableBuiltFromRefETTsCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_6__ESE_AddTableBuiltFrom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev2TableBuiltFromRefETTs, _STRING_7__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev2TableBuiltFromRefETTsCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev2TableBuiltFromRefETTsCnxs ) );
		PLMRouteSetLev2TableBuiltFromRefETTsRepETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_8__ESE_SpecETTRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev2TableBuiltFromRefETTs, _STRING_7__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev2TableBuiltFromRefETTsRepETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev2TableBuiltFromRefETTsRepETTs ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDSetLev0Categories ), PLMIDSetLev1FilterBuiltFromCnxs ), PLMIDSetLev1RepEPFs ), PLMIDSetLev1TableBuiltFromCnxs ), PLMIDSetLev1RepETTs ), PLMIDSetLev2FilterBuiltFromRefETTs ), PLMIDSetLev2FilterBuiltFromRefETTsCnxs ), PLMIDSetLev2FilterBuiltFromRefETTsRepETTs ), PLMIDSetLev2TableBuiltFromRefETTs ), PLMIDSetLev2TableBuiltFromRefETTsCnxs ), PLMIDSetLev2TableBuiltFromRefETTsRepETTs ) );
	}
}
