
import matrix.db.Context;
import com.dassault_systemes.EKLEngine.completion.CompletionJPOEvaluator;


/**
 * ${CLASSNAME}
 */
public final class Logical_ExportSystemReference_PLMChannel_mxJPO extends CompletionJPOEvaluator {

	/**
	 * Attributes
	 */
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_0__RFLVPMLogical_div_RFL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical/RFLVPMLogicalReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_1__RFLVPMLogical_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_2__RFLVPMLogical_Get1stL = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogical_Get1stLevelObjects");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_3__RFLPLMImplementConnec = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_4__RFLPLMImplementConnec = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLPLMImplementConnection_AddAllImplementCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_5__RFLVPMLogicalPortMapp = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalPortMapping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_6__RFLVPMLogicalPortMapp = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalPortMapping_AllLogMappingCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_7__PLMDocConnection_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_8__PLMDocConnection_retr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMDocConnection_retrieveAllDocuments");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_9__PLMLFSAllocationConne = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMLFSAllocationConnection");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_10__PLMLFSAllocationConn = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMLFSAllocationConnection_AddLfsCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_11__PLMElectricalLogical = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMElectricalLogical");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_12__PLMElectricalLogical = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMElectricalLogical_AddEleCnx");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_13__PLMEnsGrouping_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsGrouping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_14__PLMEnsGrouping_AddGr = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("PLMEnsGrouping_AddGrouping");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_15__RFLVPMSystemType_div_ = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMSystemType/RFLVPMSystemTypeReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_16__Logical_ExportType_P = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Logical_ExportType_PLMChannel");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_17__RFLVPMLogicalService = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalServices/RFLVPMServicesReference");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_18__RFLVPMLogicalInterfa = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalInterfaceRep");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_19__RFLVPMLogicalInterfa = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("RFLVPMLogicalInterfaceRep_Expand");
	private final static com.dassault_systemes.EKLEngine.common.lib.implementation.StringType _STRING_20__Config_GetStructConf = new com.dassault_systemes.EKLEngine.common.lib.implementation.StringType("Config_GetStructConfig");
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
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet InputPLMIDSetRestrictedToRefs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForFirstLevelLogObjects = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForImplementLinks = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForLogicalPortMapping = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForDocuments = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesFor3DForSystemsApp = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForLogicalElectricalApp = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMRoutesForLogicalPipingApp = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfFirstLevelLogObjectsPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfTypeRefsPLMIDsToExpand = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfTypesPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfConfigObjectsPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMIDSet SetOfLogSysTypeAndServicesRefPLMIDs = new com.dassault_systemes.EKLEngine.common.lib.PLMIDSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMLogicalInterfaceRep = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet SetOfPLMParameters = new com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet();
		InputPLMIDSetRestrictedToRefs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , iPLMIDSet, _STRING_0__RFLVPMLogical_div_RFL ) );
		SetOfPLMRoutesForFirstLevelLogObjects.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_1__RFLVPMLogical_, _STRING_2__RFLVPMLogical_Get1stL, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfPLMRoutesForImplementLinks.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_3__RFLPLMImplementConnec, _STRING_4__RFLPLMImplementConnec, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfPLMRoutesForLogicalPortMapping.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_5__RFLVPMLogicalPortMapp, _STRING_6__RFLVPMLogicalPortMapp, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfPLMRoutesForDocuments.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_7__PLMDocConnection_, _STRING_8__PLMDocConnection_retr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfPLMRoutesFor3DForSystemsApp.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_9__PLMLFSAllocationConne, _STRING_10__PLMLFSAllocationConn, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfPLMRoutesForLogicalElectricalApp.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_11__PLMElectricalLogical, _STRING_12__PLMElectricalLogical, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfPLMRoutesForLogicalPipingApp.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_13__PLMEnsGrouping_, _STRING_14__PLMEnsGrouping_AddGr, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		SetOfFirstLevelLogObjectsPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForFirstLevelLogObjects ) );
		SetOfTypeRefsPLMIDsToExpand.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfFirstLevelLogObjectsPLMIDs, _STRING_15__RFLVPMSystemType_div_ ) );
		SetOfTypesPLMIDs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_16__Logical_ExportType_P, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfTypeRefsPLMIDsToExpand } ) );
		SetOfLogSysTypeAndServicesRefPLMIDs.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfTypesPLMIDs, _STRING_15__RFLVPMSystemType_div_ ), com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.Restrict( iContext , SetOfFirstLevelLogObjectsPLMIDs, _STRING_17__RFLVPMLogicalService ) ) );
		SetOfPLMLogicalInterfaceRep.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_18__RFLVPMLogicalInterfa, _STRING_19__RFLVPMLogicalInterfa, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { SetOfLogSysTypeAndServicesRefPLMIDs } ) );
		SetOfConfigObjectsPLMIDs.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMProcedure( iContext , _STRING_20__Config_GetStructConf, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { iPLMIDSet } ) );
		SetOfPLMParameters.setValue( com.dassault_systemes.EKLEngine.completion.lib.Completion.ExecutePLMFunction( iContext , _STRING_21__PLMParameter_, _STRING_22__PAR_nav_params_, new com.dassault_systemes.EKLEngine.common.lib.implementation.ObjectType[] { InputPLMIDSetRestrictedToRefs } ) );
		oPLMIDSet.setValue( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( com.dassault_systemes.EKLEngine.common.lib.PLMIDSet.plus( iPLMIDSet, SetOfFirstLevelLogObjectsPLMIDs ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForImplementLinks ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForLogicalPortMapping ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForDocuments ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesFor3DForSystemsApp ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForLogicalElectricalApp ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMRoutesForLogicalPipingApp ) ), SetOfTypesPLMIDs ), SetOfConfigObjectsPLMIDs ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMLogicalInterfaceRep ) ), com.dassault_systemes.EKLEngine.common.lib.PLMRouteSet.Ids( SetOfPLMParameters ) ) );
	}
}
