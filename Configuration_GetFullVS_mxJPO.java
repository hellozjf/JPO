
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Configuration_GetFullVS_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__PLMConfigVariabilityS = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMConfigVariabilitySpace/PLMCfgVariabilitySpace");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__PLMConfigVariabilityS = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMConfigVariabilitySpace");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__PLMConfigVariabilityS = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMConfigVariabilitySpace_AddVariabilitySpaceFullContent");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__PLMConfigVariabilityS = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMConfigVariabilitySpace_AddVariabilitySpaceModel");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__Class_div_Model_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Model");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__Product_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Product");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__Product_AddCriteriaFr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Product_AddCriteriaFromModel");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PLMConfigVariabilityS = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMConfigVariabilitySpace_AddVariabilitySpaceContentProjections");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetVS = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetVSContent = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetModel = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetVSContent = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetVSModelAssociation = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetModelContent = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetVSContentProjections = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		PLMIDSetVS.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__PLMConfigVariabilityS ) );
		PLMRouteSetVSContent.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMConfigVariabilityS, _STRING_2__PLMConfigVariabilityS, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetVS } ) );
		PLMRouteSetVSModelAssociation.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMConfigVariabilityS, _STRING_3__PLMConfigVariabilityS, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetVS } ) );
		PLMIDSetModel.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetVSModelAssociation ), _STRING_4__Class_div_Model_ ) );
		PLMRouteSetModelContent.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__Product_, _STRING_6__Product_AddCriteriaFr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetModel } ) );
		PLMIDSetVSContent.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetVSContent ) );
		PLMRouteSetVSContentProjections.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__PLMConfigVariabilityS, _STRING_7__PLMConfigVariabilityS, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetVSContent } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, PLMIDSetVSContent ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetVSModelAssociation ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetModelContent ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetVSContentProjections ) ) );
	}
}
