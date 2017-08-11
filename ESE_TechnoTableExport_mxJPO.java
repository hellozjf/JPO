
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class ESE_TechnoTableExport_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMEnsSpecSpecificati = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecSpecification");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__ESE_AddTableBuiltFrom = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_AddTableBuiltFromConnectionForExport");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__PLMEnsSpecTechnoTable = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecTechnoTable/EnsTechnologicalTable");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__ESE_SpecETTRep_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecETTRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__ESE_SpecETTBuiltFromT = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecETTBuiltFromToETT");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev0TableBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev0RepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1TableBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1TableBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev1RepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev0TableBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev0RepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1TableBuiltFromRefETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1TableBuiltFromCnxs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev1RepETTs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMRouteSetLev0TableBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_1__ESE_AddTableBuiltFrom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev0TableBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev0TableBuiltFromCnxs ) );
		PLMRouteSetLev0RepETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_3__ESE_SpecETTRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev0RepETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev0RepETTs ) );
		PLMRouteSetLev1TableBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_4__ESE_SpecETTBuiltFromT, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev1TableBuiltFromRefETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1TableBuiltFromRefETTs ) );
		PLMRouteSetLev1TableBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_1__ESE_AddTableBuiltFrom, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1TableBuiltFromRefETTs, _STRING_2__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev1TableBuiltFromCnxs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1TableBuiltFromCnxs ) );
		PLMRouteSetLev1RepETTs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_3__ESE_SpecETTRep_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSetLev1TableBuiltFromRefETTs, _STRING_2__PLMEnsSpecTechnoTable ) } ) );
		PLMIDSetLev1RepETTs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev1RepETTs ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDSetLev0TableBuiltFromCnxs ), PLMIDSetLev0RepETTs ), PLMIDSetLev1TableBuiltFromRefETTs ), PLMIDSetLev1TableBuiltFromCnxs ), PLMIDSetLev1RepETTs ) );
	}
}
