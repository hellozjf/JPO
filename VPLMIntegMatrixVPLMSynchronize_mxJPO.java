/**
 *  VPLMIntegMatrixVPLMSynchronize
 *  JPO for publishing and synchronizing VPLM data with Matrix and Matrix data with VPLM.
 */

import com.dassault_systemes.vplmintegration.sdk.VPLMIntegException;
import com.matrixone.vplmintegration.engine.VPLMIntegSynchronizeEngine;
import com.matrixone.vplmintegration.engine.VPLMIntegSynchronizeEngineFactory;
import com.matrixone.vplmintegrationitf.util.VPLMIntegrationConstants;
import java.util.Map;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;

public class VPLMIntegMatrixVPLMSynchronize_mxJPO {

  /**
   * Wrapper JPO to automatically synchronize Enovia MatrixOne data with VPLM .
   * While packing the input string array provide the following data using the mentioned keys:
   * ROOTID=<Business Object id of the root part of the structure.>
   * SYNC_DEPTH=<Depth up to which to synchronize. It should be a string specifying the depth in numerals. Specify "0" if only the root is to be synchronized; "-1" if full depth is to be sychronized; >
   * SYNC_AND_TRANSFER=<"give" to give control to other side. Default is "no". Specifying any other value means "no". It is not possible to take control.
   * @param Context object for the Enovia MatrixOne session.
   * @param argument string array.
   * @throws VPLMIntegException in case of severe error.
   * @return Success - 0 or failure non zero number.
   * @deprecated Use VPLMIntegBOMVPLMSynchronize.synchronizeFromMatrixToVPM(Context context, String[] args), instead of this.
   */
  @Deprecated
  public static int syncFromMatrixToVPM(final Context context, final String[] args) throws VPLMIntegException {
    try {
      int status = 0;
      final Map syncInfoTable = (Map)JPO.invoke(context,"VPLMIntegSyncWithVPLM",null,"execute",args,Map.class);
      if (syncInfoTable == null || syncInfoTable.isEmpty()) {
        status = 1;
      }
      else {
        final String operationStatus = (String)syncInfoTable.get(VPLMIntegrationConstants.MAPKEY_OPERATION_STATUS);
        if ("false".equals(operationStatus)) {
          status = 1;
        }
      }
      return status;
    }
    catch (final MatrixException mx) {
      throw new VPLMIntegException(mx);
    }
  }

  /**
   * Wrapper JPO to automatically synchronize VPLM data with Enovia MatrixOne.
   * While packing the input string array provide the following data using the mentioned keys:
   * IDLIST=< Vector containing Business Object ids of all the roots of the product structures to be synchronized.>
   * SYNC_DEPTH=<Depth up to which to synchronize. It should be a string specifying the depth in numerals. Specify "0" if only the root is to be synchronized; "-1" if full depth is to be sychronized; >
   * SYNC_AND_TRANSFER=<"give" to give control to other side. Default is "no". Specifying any other value means "no". It is not possible to take control.>
   * vplmContext=<VPLM context string to be used.>
   * @param Context object for the Enovia MatrixOne session.
   * @param argument string array.
   * @return Success - 0 or failure non zero number.
   * @deprecated Use VPLMIntegBOMVPLMSynchronize.synchronizeFromVPMToMatrix(Context context, String[] args), instead of this.
   */
  @Deprecated
  public static int syncFromVPMToMatrix(final Context context, final String[] args) {
    int status = 0;
    try {
      VPLMIntegSynchronizeEngine engine = null;
      try {
        engine = VPLMIntegSynchronizeEngineFactory.getSynchronizerEngine(context,VPLMIntegrationConstants.ENGINE_VPLM_Direct);
        engine.initialize(context,args);
        engine.execute(context);
      }
      catch (final Exception e) {
        //The engine should not throw exception in failure case.
        status = 1;
      }
      finally {
        if (null != engine) {
          engine.finalizeSync(context);
        }
      }
    }
    catch (final VPLMIntegException ex) {
      status = 1;
    }
    return status;
  }
}
