
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Rmt_Chapter_ExportCompletion_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__Class_div_Chapter_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Chapter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__Requirement_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__rmt_nav_chapter_struc = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_chapter_structure");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__rmt_nav_chapter_child = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rmt_nav_chapter_children");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__Class_div_Requirement = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Requirement");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__Rmt_Requirement_Expor = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Rmt_Requirement_ExportCompletion");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsChaptersStruct = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsChaptersChildren = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChaptersIn = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChaptersStruct = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChaptersAll = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsChildren = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsReqsAndScope = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsChaptersIn.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__Class_div_Chapter_ ) );
		RsChaptersStruct.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_2__rmt_nav_chapter_struc, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsChaptersIn } ) );
		IdsChaptersStruct.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsChaptersStruct ) );
		IdsChaptersAll.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsChaptersStruct, _STRING_0__Class_div_Chapter_ ) );
		RsChaptersChildren.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Requirement_, _STRING_3__rmt_nav_chapter_child, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsChaptersAll } ) );
		IdsChildren.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsChaptersChildren ) );
		IdsReqs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsChildren, _STRING_4__Class_div_Requirement ) );
		IdsReqsAndScope.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_5__Rmt_Requirement_Expor, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsReqs } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsChaptersStruct ), IdsChildren ), IdsReqsAndScope ) );
	}
}
