
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Kwe_ExportReplay_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMKnowledgeReplayRep = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKnowledgeReplayRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__kwe_navigate_replayge = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kwe_navigate_replaygenobjs");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__PLMKnowledgeTemplate_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKnowledgeTemplate/PLMTemplateRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__PLMKbaAppliComponent_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKbaAppliComponent/PLMKbaAppliComponent");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__Pkt_ExportTemplate_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Pkt_ExportTemplate");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__Kba_ExportAppComp_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Kba_ExportAppComp");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsGenerativeObjects = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsGenerativeObjects = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsGenObjsPLMTemplate = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsGenObjsKML = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsCompletedGenObjsPLMTemplate = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsCompletedGenObjsKML = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		RsGenerativeObjects.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__PLMKnowledgeReplayRep, _STRING_1__kwe_navigate_replayge, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { iPLMIDSet } ) );
		IdsGenerativeObjects.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsGenerativeObjects ) );
		IdsGenObjsPLMTemplate.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsGenerativeObjects, _STRING_2__PLMKnowledgeTemplate_div_ ) );
		IdsGenObjsKML.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsGenerativeObjects, _STRING_3__PLMKbaAppliComponent_div_ ) );
		IdsCompletedGenObjsPLMTemplate.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_4__Pkt_ExportTemplate_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsGenObjsPLMTemplate } ) );
		IdsCompletedGenObjsKML.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_5__Kba_ExportAppComp_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsGenObjsKML } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsCompletedGenObjsPLMTemplate ), IdsCompletedGenObjsKML ) );
	}
}
