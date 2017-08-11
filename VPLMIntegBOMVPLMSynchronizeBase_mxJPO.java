/**
 *  VPLMIntegBOMVPLMSynchronizeBase
 *  JPO for publishing and synchronizing VPLM data with Matrix and Matrix data with VPLM.
 */

import com.dassault_systemes.vplmintegration.sdk.VPLMIntegException;
import com.matrixone.vplmintegration.engine.VPLMIntegSynchronizeEngine;
import com.matrixone.vplmintegration.engine.VPLMIntegSynchronizeEngineFactory;
import com.matrixone.vplmintegration.util.VPLMIntegTraceUtil;
import com.matrixone.vplmintegrationitf.util.VPLMIntegrationConstants;
import java.util.Hashtable;
import java.util.Map;
import matrix.db.Context;

public class VPLMIntegBOMVPLMSynchronizeBase_mxJPO {

  /**
   * JPO to invoke synchronization using Java code or MQL / TCL script.<br/>
   * While packing the input string array provide the following data using the mentioned keys:<ul/>
   * <li>"ROOTID": the root business object of the BOM structure.</li>
   * <li>"IDLIST": the list of business object ids of the root part of the structures.</li>
   * <li>"SYNC_DEPTH": depth up to which to synchronize. It should be a string specifying the depth in numerals.<br/>
   *     Specify "0" if only the root is to be synchronized; "ALL" if full depth is to be synchronized;</li>
   * <li>"SYNC_AND_TRANSFER" : "give" to give control to other side. Default is "no". Specifying any other value means "no". It is not possible to take control.</li></ul>
   * @param Context object for the Enovia MatrixOne session. The context must be connected to a server and VPLM role should be specified in it.
   * @param argument string array.
   * @throws VPLMIntegException in case of severe error.
   * @return Success - 0 or failure non zero number.
   */
  public static Map synchronizeFromMatrixToVPM(final Context context, final String[] args) {
    Map results = new Hashtable(0);
    try {
      VPLMIntegSynchronizeEngine engine = null;
      try {
        engine = VPLMIntegSynchronizeEngineFactory.getSynchronizerEngine(context,VPLMIntegrationConstants.ENGINE_MATRIX_API);
        engine.initialize(context,args);
        results = engine.execute(context);
      }
      catch (final Exception e) {
        results = VPLMIntegSynchronizeEngine.getResults(args,e.getMessage());
        VPLMIntegTraceUtil.trace(context,e.getMessage());
      }
      finally {
        if (null != engine) {
          engine.finalizeSync(context);
        }
      }
    }
    catch (final Exception ex) {
      ex.printStackTrace();
      VPLMIntegTraceUtil.trace(context,ex.getMessage());
    }
    return results;
  }

  /**
   * JPO to invoke synchronization using Java code or MQL / TCL script
   * While packing the input string array provide the following data using the mentioned keys:
   * While packing the input string array provide the following data using the mentioned keys:<ul/>
   * <li>"ROOTID": the root business object of the VPM structure.</li>
   * <li>"IDLIST": the list of business object ids of the root of the structures.</li>
   * <li>"SYNC_DEPTH": depth up to which to synchronize. It should be a string specifying the depth in numerals.<br/>
   *     Specify "0" if only the root is to be synchronized; "ALL" if full depth is to be synchronized;</li>
   * <li>"SYNC_AND_TRANSFER" : "give" to give control to other side. Default is "no". Specifying any other value means "no". It is not possible to take control.</li></ul>
   * @param Context object for the Enovia MatrixOne session. The context must be connected to a server and VPLM role should be specified in it.
   * @param argument string array.
   * @throws VPLMIntegException in case of severe error.
   * @return Success - 0 or failure non zero number.
   */
  public static Map synchronizeFromVPMToMatrix(final Context context, final String[] args) {
    Map results = new Hashtable(0);
    try {
      VPLMIntegSynchronizeEngine engine = null;
      try {
        engine = VPLMIntegSynchronizeEngineFactory.getSynchronizerEngine(context,VPLMIntegrationConstants.ENGINE_VPLM_API);
        engine.initialize(context,args);
        results = engine.execute(context);
      }
      catch (final Exception e) {
        results = VPLMIntegSynchronizeEngine.getResults(args,e.getMessage());
        VPLMIntegTraceUtil.trace(context,e.getMessage());
      }
      finally {
        if (null != engine) {
          engine.finalizeSync(context);
        }
      }
    }
    catch (final VPLMIntegException ex) {
      ex.printStackTrace();
      VPLMIntegTraceUtil.trace(context,ex.getMessage());
    }
    return results;
  }
}
