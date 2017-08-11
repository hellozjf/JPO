
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class ConfigCBP_GetModelContent_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__Class_div_Model_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Class/Model");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__Product_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Product");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__Product_AddCriteriaFr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Product_AddCriteriaFromModel");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetModel = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSetModelContent = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		PLMIDSetModel.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__Class_div_Model_ ) );
		PLMRouteSetModelContent.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__Product_, _STRING_2__Product_AddCriteriaFr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSetModel } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSetModelContent ) ) );
	}
}
