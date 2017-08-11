/*
 ** ${CLASSNAME}
 **
 **Copyright (c) 1993-2016 Dassault Systemes.
 ** All Rights Reserved.
 **This program contains proprietary and trade secret information of
 **Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 **or intended publication of such program
 */


 import com.matrixone.apps.domain.util.EnoviaResourceBundle;
 import matrix.db.Context;

/**
 * This JPO class has some methods pertaining to Master Feature Extension.
 * @author IVU
 * @version R209 - Copyright (c) 1993-2016 Dassault Systemes.
 */
public class MasterFeature_mxJPO extends MasterFeatureBase_mxJPO
{
    /**
     * Create a new ${CLASS:emxManufacturingPlanBase} object from a given id.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @throws Exception if the operation fails
     * @author IVU
     * @since R209
     */

    public MasterFeature_mxJPO (Context context, String[] args)
        throws Exception
    {
      super(context, args);
    }


    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return an integer status code (0 = success)
     * @throws Exception if the operation fails
     * @author IVU
     * @since R209
     */
    public int mxMain(Context context, String[] args)throws Exception
      {
        if (!context.isConnected()){
         String sContentLabel = EnoviaResourceBundle.getProperty(context,"DMCPlanning","DMCPlanning.Error.UnsupportedClient", context.getSession().getLanguage());
         throw  new Exception(sContentLabel);
         }
         return 0;
      }

}
