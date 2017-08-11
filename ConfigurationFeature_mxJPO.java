/*
 ** ${CLASS:MarketingFeature}
 **
 **Copyright (c) 1993-2016 Dassault Systemes.
 ** All Rights Reserved.
 **This program contains proprietary and trade secret information of
 **Dassault Systemes.
 ** Copyright notice is precautionary only and does not evidence any actual
 **or intended publication of such program
 */


 import matrix.db.Context;

import com.matrixone.apps.domain.util.EnoviaResourceBundle;

/**
 * This JPO class has some methods pertaining to MarketingFeature Extension.
 * @author XOG
 * @version R210 - Copyright (c) 1993-2016 Dassault Systemes.
 */
public class ConfigurationFeature_mxJPO extends ConfigurationFeatureBase_mxJPO
{
    /**
     * Create a new ${CLASS:MarketingFeature} object from a given id.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments.
     * @throws Exception if the operation fails
     * @author XOG
     * @since R210
     */

    public ConfigurationFeature_mxJPO (Context context, String[] args)
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
     * @author XOG
     * @since R210
     */
    public int mxMain(Context context, String[] args)throws Exception
      {
    	if (!context.isConnected()){
    		String sContentLabel = EnoviaResourceBundle.getProperty(context,"Configuration","emxProduct.Error.UnsupportedClient",context.getSession().getLanguage());
    		throw  new Exception(sContentLabel);
    	}
         return 0;
      }

}
