
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Kba_ExportAppli_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMKbaApplication_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKbaApplication/PLMKbaBusinessApplication");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__PLMKbaApplication_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKbaApplication");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__kba_expand_appli_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kba_expand_appli");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__PLMKbaAppliComponent_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKbaAppliComponent/PLMKbaAppliComponent");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__Kba_ExportAppComp_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Kba_ExportAppComp");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsPureKba = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsPureKba0 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsPureKba = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsKCompL1 = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsKCompStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsPureKba0.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__PLMKbaApplication_div_ ) );
		RsPureKba.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMKbaApplication_, _STRING_2__kba_expand_appli_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsPureKba0 } ) );
		IdsPureKba.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsPureKba ) );
		IdsKCompL1.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsPureKba, _STRING_3__PLMKbaAppliComponent_div_ ) );
		IdsKCompStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_4__Kba_ExportAppComp_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsKCompL1 } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsPureKba ), IdsKCompStructure ) );
	}
}
