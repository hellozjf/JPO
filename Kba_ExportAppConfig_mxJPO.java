
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Kba_ExportAppConfig_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMKbaAppliConfig_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKbaAppliConfig/PLMKbaAppliConfiguration");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__PLMKbaAppliConfig_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKbaAppliConfig");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__kba_expand_config_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kba_expand_config");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__kba_navigate_config_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("kba_navigate_config");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__PLMKbaApplication_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMKbaApplication/PLMKbaBusinessApplication");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__Kba_ExportAppli_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Kba_ExportAppli");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsPureKba = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet RsRZipKConfig = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsKConfig = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsPureKba = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsKAppli = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet IdsKAppliStructure = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		IdsKConfig.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__PLMKbaAppliConfig_div_ ) );
		RsPureKba.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMKbaAppliConfig_, _STRING_2__kba_expand_config_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsKConfig } ) );
		IdsPureKba.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsPureKba ) );
		RsRZipKConfig.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMKbaAppliConfig_, _STRING_3__kba_navigate_config_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsKConfig } ) );
		IdsKAppli.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , IdsPureKba, _STRING_4__PLMKbaApplication_div_ ) );
		IdsKAppliStructure.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_5__Kba_ExportAppli_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { IdsKAppli } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, IdsPureKba ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( RsRZipKConfig ) ), IdsKAppliStructure ) );
	}
}
