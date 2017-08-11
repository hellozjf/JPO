
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Logical_ExportReference_Decoration_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RFLVPMLogical_div_RFL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical/RFLVPMLogicalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__RFLVPMLogical_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__RFLVPMLogical_Get1stL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical_Get1stLevelObjects");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__RFLVPMSystemType_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMSystemType/RFLVPMSystemTypeReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__RFLVPMSystemType_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMSystemType");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__RFLVPMSystemType_Expa = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMSystemType_Expand");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__RFLVPMLogical3DRep_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical3DRep/RFLVPMLogical3DRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__RFLVPMLogical3DRep_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical3DRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__RFLVPMLogical_AddLogi = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical_AddLogicalPublications");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__RFLVPMLogicalServices = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalServices/RFLVPMServicesReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__RFLVPMLogicalInterfa = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalInterfaceRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__RFLVPMLogicalInterfa = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalInterfaceRep_Expand");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__RFLPLMImplementConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__RFLPLMImplementConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection_AddAllImplementCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__PLMHVACLogicalGroupC = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMHVACLogicalGroupCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__PLMHVACLogicalGroupC = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMHVACLogicalGroupCnx_AddPleCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_16__PLMLFSAllocationConn = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMLFSAllocationConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_17__PLMLFSAllocationConn = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMLFSAllocationConnection_AddLfsCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_18__PLMElectricalLogical = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMElectricalLogical");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_19__PLMElectricalLogical = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMElectricalLogical_AddEleCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_20__ESE_LogRef_GetEngSpe = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("ESE_LogRef_GetEngSpecDependencies");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_21__PLMEnsGrouping_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsGrouping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_22__PLMEnsGrouping_AddGr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsGrouping_AddGrouping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_23__RFLVPMLogicalPortMap = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalPortMapping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_24__RFLVPMLogicalPortMap = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalPortMapping_AllLogMappingCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_25__PLMDocConnection_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_26__PLMDocConnection_ret = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection_retrieveAllDocuments");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_27__RFLVPMLogical_div_RF = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical/RFLVPMLogicalRepReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_28__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_29__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary_GetRepLibPointedByLogicalRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_30__CATSysBehaviorLibrar = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("CATSysBehaviorLibrary_GetRefLibFromRepLib");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_31__SystemsBehavior_GetD = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("SystemsBehavior_GetDependencies");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_32__DIFModeler_GetAttach = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("DIFModeler_GetAttachedPresentations");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_33__PLMParameter_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMParameter");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_34__PAR_nav_params_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PAR_nav_params");

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
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfLogSysTypeRefTopModelerPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfLogPub3DRepRefTopModelerPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForSystemTypes = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForImplementLinks = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesFor3DForSystemsApp = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForLogicalElectricalApp = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForLogicalPipingApp = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForLogicalPortMapping = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForDocuments = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfLogSysTypePLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfLogSysTypeAndServicesRefPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMLogicalInterfaceRep = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
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
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_EngSpecDependancies = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfLog3DRepRefTopModelerPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet PLMIDSet_ForIL = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet PLMRoutesSet_HVACConnections = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet InputPLMIDSetRestricted = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		InputPLMIDSetRestricted.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RFLVPMLogical_div_RFL ) );
		SetOfLogTopModelerPLMRoutes.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__RFLVPMLogical_, _STRING_2__RFLVPMLogical_Get1stL, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfLogTopModelerPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfLogTopModelerPLMRoutes ) );
		SetOfLogSysTypeRefTopModelerPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfLogTopModelerPLMIDs, _STRING_3__RFLVPMSystemType_div_ ) );
		SetOfPLMRoutesForSystemTypes.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_4__RFLVPMSystemType_, _STRING_5__RFLVPMSystemType_Expa, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLogSysTypeRefTopModelerPLMIDs } ) );
		SetOfLogSysTypePLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForSystemTypes ) );
		SetOfLog3DRepRefTopModelerPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfLogTopModelerPLMIDs, _STRING_6__RFLVPMLogical3DRep_div_ ) );
		SetOfLogPub3DRepRefTopModelerPLMIDs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_7__RFLVPMLogical3DRep_, _STRING_8__RFLVPMLogical_AddLogi, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLog3DRepRefTopModelerPLMIDs } ) );
		SetOfLogSysTypeAndServicesRefPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfLogSysTypePLMIDs, _STRING_3__RFLVPMSystemType_div_ ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfLogTopModelerPLMIDs, _STRING_9__RFLVPMLogicalServices ) ) );
		SetOfPLMLogicalInterfaceRep.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_10__RFLVPMLogicalInterfa, _STRING_11__RFLVPMLogicalInterfa, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLogSysTypeAndServicesRefPLMIDs } ) );
		PLMIDSet_ForIL.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( InputPLMIDSetRestricted, SetOfLogSysTypeRefTopModelerPLMIDs ) );
		SetOfPLMRoutesForImplementLinks.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_12__RFLPLMImplementConne, _STRING_13__RFLPLMImplementConne, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_ForIL } ) );
		PLMRoutesSet_HVACConnections.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_14__PLMHVACLogicalGroupC, _STRING_15__PLMHVACLogicalGroupC, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfPLMRoutesFor3DForSystemsApp.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_16__PLMLFSAllocationConn, _STRING_17__PLMLFSAllocationConn, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfPLMRoutesForLogicalElectricalApp.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_18__PLMElectricalLogical, _STRING_19__PLMElectricalLogical, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		PLMIDSet_EngSpecDependancies.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_20__ESE_LogRef_GetEngSpe, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfPLMRoutesForLogicalPipingApp.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_21__PLMEnsGrouping_, _STRING_22__PLMEnsGrouping_AddGr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfPLMRoutesForLogicalPortMapping.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_23__RFLVPMLogicalPortMap, _STRING_24__RFLVPMLogicalPortMap, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfPLMRoutesForDocuments.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_25__PLMDocConnection_, _STRING_26__PLMDocConnection_ret, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		PLMIDSet_LogicalRep.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfLogTopModelerPLMIDs, _STRING_27__RFLVPMLogical_div_RF ) );
		PLMRouteSet_LRepRefAndRepLibDependancy.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_28__CATSysBehaviorLibrar, _STRING_29__CATSysBehaviorLibrar, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_LogicalRep } ) );
		PLMIDSet_LRepRefAndRepLibDependancy.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_LRepRefAndRepLibDependancy ) );
		PLMRouteSet_RefLib_RepInstLib_RepRefLib_Dependancy.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_28__CATSysBehaviorLibrar, _STRING_30__CATSysBehaviorLibrar, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_LRepRefAndRepLibDependancy } ) );
		PLMIDSet_RefLib_RepInstLib_RepRefLib_Dependancy.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRouteSet_RefLib_RepInstLib_RepRefLib_Dependancy ) );
		PLMIDSet_BehaviorDataToExport.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_31__SystemsBehavior_GetD, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { PLMIDSet_RefLib_RepInstLib_RepRefLib_Dependancy } ) );
		PLMIDSet_DifModeler.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_32__DIFModeler_GetAttach, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		SetOfPLMParameters.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_33__PLMParameter_, _STRING_34__PAR_nav_params_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestricted } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, SetOfLogTopModelerPLMIDs ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForSystemTypes ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForImplementLinks ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesFor3DForSystemsApp ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForLogicalElectricalApp ) ), PLMIDSet_EngSpecDependancies ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForLogicalPipingApp ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForLogicalPortMapping ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForDocuments ) ), PLMIDSet_BehaviorDataToExport ), PLMIDSet_DifModeler ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfLogPub3DRepRefTopModelerPLMIDs ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMLogicalInterfaceRep ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMParameters ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( PLMRoutesSet_HVACConnections ) ) );
	}
}
