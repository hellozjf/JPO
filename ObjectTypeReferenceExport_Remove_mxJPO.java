
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class ObjectTypeReferenceExport_Remove_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__CATComponentBasedDesi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentBasedDesign");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__CATComponentBasedDesi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentBasedDesign_ExpandRefToRepRef");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__all_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("all");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMIDRepRouteSet = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDRepSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMIDRepRouteSet.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATComponentBasedDesi, _STRING_1__CATComponentBasedDesi, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { iPLMIDSet } ) );
		PLMIDRepSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMIDRepRouteSet, _STRING_2__all_ ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDRepSet ) );
	}
}
