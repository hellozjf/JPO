
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class ObjectTypeReferenceExport_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__CATComponentBasedDesi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentBasedDesign/ObjectTypeReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__CATComponentBasedDesi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentBasedDesign");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__CATComponentBasedDesi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentBasedDesign_ExpandRefToRepRef");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__all_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("all");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__CATComponentBasedDesi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATComponentBasedDesign/ObjectTypeRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__ObjectTypeRepReferenc = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ObjectTypeRepReferenceExport");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMIDRepRouteSet = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDRefSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDRepSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDRepRefSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDResourceTableSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMIDRefSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__CATComponentBasedDesi ) );
		PLMIDRepRouteSet.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__CATComponentBasedDesi, _STRING_2__CATComponentBasedDesi, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDRefSet } ) );
		PLMIDRepSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMIDRepRouteSet, _STRING_3__all_ ) );
		PLMIDRepRefSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDRepSet, _STRING_4__CATComponentBasedDesi ) );
		PLMIDResourceTableSet.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_5__ObjectTypeRepReferenc, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDRepRefSet } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDRefSet, PLMIDRepSet ), PLMIDResourceTableSet ) );
	}
}
