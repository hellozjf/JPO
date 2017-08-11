
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Validation_PLMChannel_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__DMUValidationBase_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DMUValidationBase");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__DMUValidationBase_Add = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DMUValidationBase_AddChildren");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__DMUValidationBase_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DMUValidationBase/DMUValidationValidation");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__DMUValidationBase_Exp = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DMUValidationBase_ExpandAll");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetChildren = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSetReviews = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		PLMIDSetChildren.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__DMUValidationBase_, _STRING_1__DMUValidationBase_Add, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__DMUValidationBase_div_ ) } ) ) );
		PLMIDSetReviews.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_0__DMUValidationBase_, _STRING_3__DMUValidationBase_Exp, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_2__DMUValidationBase_div_ ) } ) ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDSetChildren, PLMIDSetReviews ) );
	}
}
