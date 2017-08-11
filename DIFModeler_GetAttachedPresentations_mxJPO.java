
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class DIFModeler_GetAttachedPresentations_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__DIFModeler01_div_DIFL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModeler01/DIFLayout");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__DIFModelerAbstractShe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModelerAbstractSheet/DIFAbstractSheet");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__PRODUCTCFG_div_VPMRef = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PRODUCTCFG/VPMReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__RFLVPMLogical_div_RFL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical/RFLVPMLogicalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__RFLPLMFunctional_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMFunctional/RFLPLMFunctionalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__DIFModeler01_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModeler01");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__DifModeler_AddPresent = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DifModeler_AddPresentationCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__DifModeler_AddLayouts = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DifModeler_AddLayouts");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__DifModeler_AddSheets_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DifModeler_AddSheets");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__DIFModeler03_div_DIFV = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModeler03/DIFViewStream");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__DIFModeler04_div_DIF = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModeler04/DIFBackgroundViewRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__DifModeler_AddStanda = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DifModeler_AddStandard");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__DIFModelerAbstractVi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModelerAbstractView/DIFAbstractView");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__RFLPLMImplementConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__RFLPLMImplementConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection_AddAllImplementCnx");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMDifAttachedPresentationSet = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifAttachedPresentationIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMDifPresentationSet = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifPresentationIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMDifExpendedSheet = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifExpendedSheetIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMRestrictedDifLayoutFromInputIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMRestrictedDifSheetFromInputIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifViewStreamIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifBackgroundViewIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMDifStandardSet = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifStandardIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMVPMReferenceIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMRFLVPMLogicalReferenceIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMRFLPLMFunctionalReferenceIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMReferenceWithConnection = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifReferenceToExpand = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifRepReferenceWithStandard = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifAbstractViewIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMDifViewImplementLinks = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMDifViewImplementLinksIDSet = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMRestrictedDifLayoutFromInputIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__DIFModeler01_div_DIFL ) );
		PLMRestrictedDifSheetFromInputIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_1__DIFModelerAbstractShe ) );
		PLMVPMReferenceIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__PRODUCTCFG_div_VPMRef ) );
		PLMRFLVPMLogicalReferenceIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_3__RFLVPMLogical_div_RFL ) );
		PLMRFLPLMFunctionalReferenceIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_4__RFLPLMFunctional_div_ ) );
		PLMReferenceWithConnection.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMVPMReferenceIDSet, PLMRFLVPMLogicalReferenceIDSet ), PLMRFLPLMFunctionalReferenceIDSet ) );
		PLMDifAttachedPresentationSet.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__DIFModeler01_, _STRING_6__DifModeler_AddPresent, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMReferenceWithConnection } ) );
		PLMDifAttachedPresentationIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMDifAttachedPresentationSet ) );
		PLMDifPresentationSet.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__DIFModeler01_, _STRING_7__DifModeler_AddLayouts, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMDifAttachedPresentationIDSet } ) );
		PLMDifPresentationIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMDifPresentationSet ) );
		PLMDifReferenceToExpand.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMDifPresentationIDSet, PLMRestrictedDifSheetFromInputIDSet ), PLMRestrictedDifLayoutFromInputIDSet ) );
		PLMDifExpendedSheet.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__DIFModeler01_, _STRING_8__DifModeler_AddSheets_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMDifReferenceToExpand } ) );
		PLMDifExpendedSheetIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMDifExpendedSheet ) );
		PLMDifViewStreamIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMDifExpendedSheetIDSet, _STRING_9__DIFModeler03_div_DIFV ) );
		PLMDifBackgroundViewIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMDifExpendedSheetIDSet, _STRING_10__DIFModeler04_div_DIF ) );
		PLMDifRepReferenceWithStandard.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMDifBackgroundViewIDSet, PLMDifViewStreamIDSet ) );
		PLMDifStandardSet.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__DIFModeler01_, _STRING_11__DifModeler_AddStanda, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMDifRepReferenceWithStandard } ) );
		PLMDifStandardIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMDifStandardSet ) );
		PLMDifAbstractViewIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMDifExpendedSheetIDSet, _STRING_12__DIFModelerAbstractVi ) );
		PLMDifViewImplementLinks.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_13__RFLPLMImplementConne, _STRING_14__RFLPLMImplementConne, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMDifAbstractViewIDSet } ) );
		PLMDifViewImplementLinksIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMDifViewImplementLinks ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMDifAttachedPresentationIDSet ), PLMDifPresentationIDSet ), PLMDifExpendedSheetIDSet ), PLMDifStandardIDSet ), PLMDifViewImplementLinksIDSet ) );
	}
}
