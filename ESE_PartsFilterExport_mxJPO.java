
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class ESE_PartsFilterExport_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMEnsSpecSpecificati = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecSpecification");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__ESE_AddFilterBuiltFro = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_AddFilterBuiltFromConnectionForExport");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__PLMEnsSpecPartsFilter = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecPartsFilter/EnsFilter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__ESE_AddFilterExtensio = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_AddFilterExtensionDefConnectionForExport");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__ESE_SpecEPFRep_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecEPFRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__ESE_SpecEPFBuiltFromT = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecEPFBuiltFromToETT");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__ESE_AddTableBuiltFrom = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_AddTableBuiltFromConnectionForExport");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PLMEnsSpecTechnoTable = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecTechnoTable/EnsTechnologicalTable");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__ESE_SpecETTRep_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecETTRep");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev0FilterBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev0FilterExtensionDefCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev0RepEPFs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1FilterBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1TableBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1RepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev0FilterBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev0FilterExtensionDefCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev0RepEPFs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1FilterBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1TableBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1RepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMRouteSetLev0FilterBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_1__ESE_AddFilterBuiltFro, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__PLMEnsSpecPartsFilter ) } ) );
		PLMIDSetLev0FilterBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev0FilterBuiltFromCnxs ) );
		PLMRouteSetLev0FilterExtensionDefCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_3__ESE_AddFilterExtensio, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__PLMEnsSpecPartsFilter ) } ) );
		PLMIDSetLev0FilterExtensionDefCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev0FilterExtensionDefCnxs ) );
		PLMRouteSetLev0RepEPFs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_4__ESE_SpecEPFRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__PLMEnsSpecPartsFilter ) } ) );
		PLMIDSetLev0RepEPFs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev0RepEPFs ) );
		PLMRouteSetLev1FilterBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_5__ESE_SpecEPFBuiltFromT, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__PLMEnsSpecPartsFilter ) } ) );
		PLMIDSetLev1FilterBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1FilterBuiltFromRefETTs ) );
		PLMRouteSetLev1TableBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_6__ESE_AddTableBuiltFrom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1FilterBuiltFromRefETTs, _STRING_7__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev1TableBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1TableBuiltFromCnxs ) );
		PLMRouteSetLev1RepETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_8__ESE_SpecETTRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1FilterBuiltFromRefETTs, _STRING_7__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev1RepETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1RepETTs ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDSetLev0FilterBuiltFromCnxs ), PLMIDSetLev0FilterExtensionDefCnxs ), PLMIDSetLev0RepEPFs ), PLMIDSetLev1FilterBuiltFromRefETTs ), PLMIDSetLev1TableBuiltFromCnxs ), PLMIDSetLev1RepETTs ) );
	}
}
