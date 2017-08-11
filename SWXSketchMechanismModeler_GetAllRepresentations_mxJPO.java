
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class SWXSketchMechanismModeler_GetAllRepresentations_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__SWXSketchMechanismMod = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SWXSketchMechanismModeler/SWXSketchMechanismRepresentation");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__SWXSketchMechanismMod = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SWXSketchMechanismModeler");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__SWXSketchMechanismMod = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SWXSketchMechanismModeler_AddRep");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet1 = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetRestrictedRep = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMIDSetRestrictedRep.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__SWXSketchMechanismMod ) );
		PLMRouteSet1.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__SWXSketchMechanismMod, _STRING_2__SWXSketchMechanismMod, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetRestrictedRep } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet1 ) );
	}
}
