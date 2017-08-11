
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class MaterialUnifiedDesignCompletion_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__CATMaterialRef_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__mat_retrieveMatRefFro = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mat_retrieveMatRefFromVector");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__CATMaterialRef_div_CA = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef/CATMatReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__MaterialUnifiedResour = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("MaterialUnifiedResources");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__mcc_retrieveDomains_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mcc_retrieveDomains");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__Class_div_Internal_Ma = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Internal Material");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__mcc_retrieveDomainsUs = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mcc_retrieveDomainsUsing");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__mat_retrieveDomains_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mat_retrieveDomains");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__mcc_retrieveCnx_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mcc_retrieveCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__mat_retrieveCnx_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("mat_retrieveCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__rdg_retrieveTexture_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("rdg_retrieveTexture");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__CATMaterialRef_div_M = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATMaterialRef/MaterialDomain");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__PLMDocConnection_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__PLMDocConnection_ret = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection_retrieveAllDocuments");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet Result = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet AllReferences = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet AllDomains = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet AllCnxUnderRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet AllDocsUnderRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet MatRefsFromVector = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet MCCEntityFromVector = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet DomainFromMatRef = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet DomainFromMCCComposed = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet DomainFromMCCUsing = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet CnxFromMatRef = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet CnxFromMCC = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet DocsFromDomains = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet DocsUnderMCC = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet DocsUnderMatRef = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		MatRefsFromVector.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATMaterialRef_, _STRING_1__mat_retrieveMatRefFro, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__CATMaterialRef_div_CA ) } ) );
		AllReferences.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( MatRefsFromVector ) ) );
		DomainFromMCCComposed.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_3__MaterialUnifiedResour, _STRING_4__mcc_retrieveDomains_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , AllReferences, _STRING_5__Class_div_Internal_Ma ) } ) );
		DomainFromMCCUsing.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_3__MaterialUnifiedResour, _STRING_6__mcc_retrieveDomainsUs, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , AllReferences, _STRING_5__Class_div_Internal_Ma ) } ) );
		DomainFromMatRef.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATMaterialRef_, _STRING_7__mat_retrieveDomains_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , AllReferences, _STRING_2__CATMaterialRef_div_CA ) } ) );
		AllDomains.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( DomainFromMCCComposed ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( DomainFromMatRef ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( DomainFromMCCUsing ) ) );
		CnxFromMCC.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_3__MaterialUnifiedResour, _STRING_8__mcc_retrieveCnx_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , AllReferences, _STRING_5__Class_div_Internal_Ma ) } ) );
		CnxFromMatRef.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATMaterialRef_, _STRING_9__mat_retrieveCnx_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , AllReferences, _STRING_2__CATMaterialRef_div_CA ) } ) );
		AllCnxUnderRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( CnxFromMCC ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( CnxFromMatRef ) ) );
		DocsFromDomains.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__CATMaterialRef_, _STRING_10__rdg_retrieveTexture_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , AllDomains, _STRING_11__CATMaterialRef_div_M ) } ) );
		DocsUnderMatRef.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_12__PLMDocConnection_, _STRING_13__PLMDocConnection_ret, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , AllReferences, _STRING_2__CATMaterialRef_div_CA ) } ) );
		AllDocsUnderRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( DocsUnderMatRef ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, AllReferences ), AllDomains ), AllCnxUnderRef ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( DocsFromDomains ) ), AllDocsUnderRef ) );
	}
}
