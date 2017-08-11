
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class ESE_SpecRemove_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMEnsSpecSpecificati = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecSpecification");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__ESE_SpecCategories2_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_SpecCategories2");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__PLMEnsSpecSpecificati = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsSpecSpecification/EnsSpecification");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetLev0Categories = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetLev0Categories = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMRouteSetLev0Categories.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMEnsSpecSpecificati, _STRING_1__ESE_SpecCategories2_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__PLMEnsSpecSpecificati ) } ) );
		PLMIDSetLev0Categories.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetLev0Categories ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDSetLev0Categories ) );
	}
}
