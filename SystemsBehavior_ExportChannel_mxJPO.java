
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class SystemsBehavior_ExportChannel_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__CATSysBehaviorLibrary = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary/CATSysBehaviorLibReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__CATSysBehaviorLibrary = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__CATSysBehaviorLibrary = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary_GetAllMonoInstRepLibOfRefLib");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_RefLib = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_LibRepInstantiatedUnderInputLibRef = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		PLMIDSet_RefLib.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__CATSysBehaviorLibrary ) );
		PLMRouteSet_LibRepInstantiatedUnderInputLibRef.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__CATSysBehaviorLibrary, _STRING_2__CATSysBehaviorLibrary, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_RefLib } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDSet_RefLib, com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_LibRepInstantiatedUnderInputLibRef ) ) );
	}
}
