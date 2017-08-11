
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Logical_ExportReference_Design_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RFLVPMLogical_div_RFL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical/RFLVPMLogicalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__RFLVPMLogical_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__RFLVPMLogical_AllLogO = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical_AllLogObjects");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__RFLPLMImplementConnec = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__RFLPLMImplementConnec = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection_AddAllImplementCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__PLMLFSAllocationConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMLFSAllocationConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__PLMLFSAllocationConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMLFSAllocationConnection_AddLfsCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PLMElectricalLogical_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMElectricalLogical");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__PLMElectricalLogical_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMElectricalLogical_AddEleCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__PLMEnsGrouping_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsGrouping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__PLMEnsGrouping_AddGr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsGrouping_AddGrouping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__RFLVPMLogicalPortMap = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalPortMapping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__RFLVPMLogicalPortMap = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalPortMapping_AllLogMappingCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__PLMDocConnection_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__PLMDocConnection_ret = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection_retrieveAllDocuments");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__RFLVPMLogical_div_RF = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical/RFLVPMLogicalRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_16__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_17__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary_GetRepLibPointedByLogicalRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_18__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary_GetRefLibFromRepLib");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_19__SystemsBehavior_GetD = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SystemsBehavior_GetDependencies");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_20__DIFModeler_GetAttach = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModeler_GetAttachedPresentations");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_21__PLMParameter_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMParameter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_22__PAR_nav_params_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PAR_nav_params");

	/**
	 * evaluate
	 * @param iContext
	 * @param iPLMIDSet
	 * @param oPLMIDSet
	 */
	public final void evaluate(matrix.db.Context iContext, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet iPLMIDSet, com.dassault_systemes.EKLEngine.common.lib.PLMIDSet oPLMIDSet)	
			throws Exception {
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfLogTopModelerPLMRoutes = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfLogTopModelerPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfLogRefTopModelerPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForImplementLinks = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesFor3DForSystemsApp = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForLogicalElectricalApp = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForLogicalPipingApp = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForLogicalPortMapping = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForDocuments = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMParameters = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_LogicalRep = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_LRepRefAndRepLibDependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_LRepRefAndRepLibDependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_FirstRepLibPointed = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_LibRefRepDependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_LibRefRepDependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_RefLib_RepInstLib_RepRefLib_Dependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_RefLib_RepInstLib_RepRefLib_Dependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_RefLibDependancy = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRouteSet_AdditionnalDataToExport = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_BehaviorDataToExport = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_DifModeler = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet InputPLMIDSetRestricted = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		InputPLMIDSetRestricted.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RFLVPMLogical_div_RFL ) );
		SetOfLogTopModelerPLMRoutes.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__RFLVPMLogical_, _STRING_2__RFLVPMLogical_AllLogO, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfLogTopModelerPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfLogTopModelerPLMRoutes ) );
		SetOfLogRefTopModelerPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfLogTopModelerPLMIDs, _STRING_0__RFLVPMLogical_div_RFL ) );
		SetOfPLMRoutesForImplementLinks.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_3__RFLPLMImplementConnec, _STRING_4__RFLPLMImplementConnec, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLogRefTopModelerPLMIDs } ) );
		SetOfPLMRoutesFor3DForSystemsApp.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__PLMLFSAllocationConne, _STRING_6__PLMLFSAllocationConne, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLogRefTopModelerPLMIDs } ) );
		SetOfPLMRoutesForLogicalElectricalApp.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_7__PLMElectricalLogical_, _STRING_8__PLMElectricalLogical_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLogRefTopModelerPLMIDs } ) );
		SetOfPLMRoutesForLogicalPipingApp.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_9__PLMEnsGrouping_, _STRING_10__PLMEnsGrouping_AddGr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLogRefTopModelerPLMIDs } ) );
		SetOfPLMRoutesForLogicalPortMapping.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_11__RFLVPMLogicalPortMap, _STRING_12__RFLVPMLogicalPortMap, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLogRefTopModelerPLMIDs } ) );
		SetOfPLMRoutesForDocuments.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_13__PLMDocConnection_, _STRING_14__PLMDocConnection_ret, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLogRefTopModelerPLMIDs } ) );
		PLMIDSet_LogicalRep.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfLogTopModelerPLMIDs, _STRING_15__RFLVPMLogical_div_RF ) );
		PLMRouteSet_LRepRefAndRepLibDependancy.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_16__CATSysBehaviorLibrar, _STRING_17__CATSysBehaviorLibrar, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_LogicalRep } ) );
		PLMIDSet_LRepRefAndRepLibDependancy.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_LRepRefAndRepLibDependancy ) );
		PLMRouteSet_RefLib_RepInstLib_RepRefLib_Dependancy.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_16__CATSysBehaviorLibrar, _STRING_18__CATSysBehaviorLibrar, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_LRepRefAndRepLibDependancy } ) );
		PLMIDSet_RefLib_RepInstLib_RepRefLib_Dependancy.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_RefLib_RepInstLib_RepRefLib_Dependancy ) );
		PLMIDSet_BehaviorDataToExport.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_19__SystemsBehavior_GetD, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_RefLib_RepInstLib_RepRefLib_Dependancy } ) );
		PLMIDSet_DifModeler.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_20__DIFModeler_GetAttach, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLogRefTopModelerPLMIDs } ) );
		SetOfPLMParameters.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_21__PLMParameter_, _STRING_22__PAR_nav_params_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLogRefTopModelerPLMIDs } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, SetOfLogTopModelerPLMIDs ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForImplementLinks ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesFor3DForSystemsApp ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForLogicalElectricalApp ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForLogicalPipingApp ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForLogicalPortMapping ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForDocuments ) ), PLMIDSet_BehaviorDataToExport ), PLMIDSet_DifModeler ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMParameters ) ) );
	}
}
