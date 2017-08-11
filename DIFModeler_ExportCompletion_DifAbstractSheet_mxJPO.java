
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class DIFModeler_ExportCompletion_DifAbstractSheet_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__DIFModeler01_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModeler01");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__DifModeler_AddBackgro = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DifModeler_AddBackgroundViewRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__DIFModelerAbstractShe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModelerAbstractSheet/DIFAbstractSheet");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMDifBackgroundViewSet = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifBackgroundViewIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMDifBackgroundViewSet.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__DIFModeler01_, _STRING_1__DifModeler_AddBackgro, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__DIFModelerAbstractShe ) } ) );
		PLMDifBackgroundViewIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMDifBackgroundViewSet ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMDifBackgroundViewIDSet ) );
	}
}
