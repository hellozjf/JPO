
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class DIFSymbolLibrary_ExportSelf_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__DIFModeler05_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModeler05");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__DifModeler_AddSymbolL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DifModeler_AddSymbolLibrary");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMDifSymbolLibrarySet = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifSymbolLibraryIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMDifSymbolLibrarySet.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__DIFModeler05_, _STRING_1__DifModeler_AddSymbolL, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { iPLMIDSet } ) );
		PLMDifSymbolLibraryIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMDifSymbolLibrarySet ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMDifSymbolLibraryIDSet ) );
	}
}
