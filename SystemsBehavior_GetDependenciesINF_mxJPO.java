
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class SystemsBehavior_GetDependenciesINF_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__CATSysBehaviorLibrary = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary/CATSysBehaviorLibReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__CATSysBehaviorLibrary = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__CATSysBehaviorLibrary = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary_GetAllMonoInstRepLibOfRefLib");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__CATSysBehaviorLibrary = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary/CATSysBehaviorLibRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__CATSysBehaviorLibrary = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary_GetRepLibDependancy");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__CATSysBehaviorLibrary = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary_GetRefLibFromRepLib");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_RefLibsInInput = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_RefLibsInInputNonFiltered = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_AllObjUnderInputLibsRef = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_AllObjUnderInputLibsRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_LibsRepRefInstantiatedUnderInputLibsRef = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_LibsRepRefDependencyOfInputLibsRep = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_LibsRepRefDependencyOfInputLibsRep = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_AllObjOfLibsRefToExport = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_AllObjOfLibsRefToExport = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_FilteredLibsRefToExport = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_FilteredLibsRefToExport = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_PointedLibsRefToExportToo = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_AllLibRef_LibRepRef_LibRepInst_ToExport = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		PLMIDSet_RefLibsInInput.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__CATSysBehaviorLibrary ) );
		PLMRouteSet_AllObjUnderInputLibsRef.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__CATSysBehaviorLibrary, _STRING_2__CATSysBehaviorLibrary, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_RefLibsInInput } ) );
		PLMIDSet_AllObjUnderInputLibsRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_AllObjUnderInputLibsRef ) );
		PLMIDSet_LibsRepRefInstantiatedUnderInputLibsRef.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet_AllObjUnderInputLibsRef, _STRING_3__CATSysBehaviorLibrary ) );
		PLMRouteSet_LibsRepRefDependencyOfInputLibsRep.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__CATSysBehaviorLibrary, _STRING_4__CATSysBehaviorLibrary, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_LibsRepRefInstantiatedUnderInputLibsRef } ) );
		PLMIDSet_LibsRepRefDependencyOfInputLibsRep.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_LibsRepRefDependencyOfInputLibsRep ) );
		PLMRouteSet_AllObjOfLibsRefToExport.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__CATSysBehaviorLibrary, _STRING_5__CATSysBehaviorLibrary, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_LibsRepRefDependencyOfInputLibsRep } ) );
		PLMIDSet_AllObjOfLibsRefToExport.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_AllObjOfLibsRefToExport ) );
		PLMIDSet_PointedLibsRefToExportToo.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , PLMIDSet_AllObjOfLibsRefToExport, _STRING_0__CATSysBehaviorLibrary ) );
		PLMIDSet_FilteredLibsRefToExport.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( PLMIDSet_RefLibsInInput, PLMIDSet_PointedLibsRefToExportToo ) );
		PLMRouteSet_AllLibRef_LibRepRef_LibRepInst_ToExport.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__CATSysBehaviorLibrary, _STRING_2__CATSysBehaviorLibrary, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_FilteredLibsRefToExport } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_AllLibRef_LibRepRef_LibRepInst_ToExport ) );
	}
}
