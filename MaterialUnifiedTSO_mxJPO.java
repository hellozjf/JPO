
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class MaterialUnifiedTSO_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__MaterialUnifiedResour = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("MaterialUnifiedResources");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__mcc_retrieveDomains_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mcc_retrieveDomains");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__Class_div_Internal_Ma = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Internal Material");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__mcc_retrieveDomainsUs = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mcc_retrieveDomainsUsing");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__mcc_retrieveCnx_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mcc_retrieveCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__CATMaterialRef_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__rdg_retrieveTexture_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rdg_retrieveTexture");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__CATMaterialRef_div_Ma = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef/MaterialDomain");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet AllDomains = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet AllCnxUnderRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet DomainFromMCCComposed = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet DomainFromMCCUsing = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet CnxFromMCC = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet DocsUnderMCC = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet DocsFromDomains = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		DomainFromMCCComposed.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__MaterialUnifiedResour, _STRING_1__mcc_retrieveDomains_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__Class_div_Internal_Ma ) } ) );
		DomainFromMCCUsing.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__MaterialUnifiedResour, _STRING_3__mcc_retrieveDomainsUs, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__Class_div_Internal_Ma ) } ) );
		AllDomains.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( DomainFromMCCComposed ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( DomainFromMCCUsing ) ) );
		CnxFromMCC.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__MaterialUnifiedResour, _STRING_4__mcc_retrieveCnx_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__Class_div_Internal_Ma ) } ) );
		AllCnxUnderRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( CnxFromMCC ) );
		DocsFromDomains.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__CATMaterialRef_, _STRING_6__rdg_retrieveTexture_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , AllDomains, _STRING_7__CATMaterialRef_div_Ma ) } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, AllDomains ), AllCnxUnderRef ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( DocsFromDomains ) ) );
	}
}
