/*
**  FTRWidgets.java
**
** Copyright (c) 1999-2016 Dassault Systemes.
**
** All Rights Reserved.
** This program contains proprietary and trade secret information of
** MatrixOne, Inc.  Copyright notice is precautionary only and does
** not evidence any actual or intended publication of such program.
*/

import java.util.Map;

import matrix.db.Context;
import matrix.db.JPO;

import com.dassault_systemes.enovia.e6w.foundation.ServiceBase;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;
/**
 * This JPO class is contians methods to support the FTR Widgets.
 * @author SE3
 * @version ProductLine - Copyright (c) 2013-2016, Dassault Systmes.
 */
public class FTRWidgetsBase_mxJPO extends emxPLCCommonBase_mxJPO {
    /** Alias for key emxProduct.Error.UnsupportedClient. */
    protected static final String ERROR_UNSUPPORTEDCLIENT = "emxProduct.Error.UnsupportedClient";

    /**
     * Default constructor
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args    holds no arguments
     * @throws        Exception if the operation fails
     * @since         ProductCentral 10.6
     */
    public FTRWidgetsBase_mxJPO (Context context, String[] args) throws Exception{
        super(context, args);
    }

    /**
     * Main entry point.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param         args holds no arguments
     * @return        an integer status code (0 = success)
     * @throws        Exception if the operation fails
     * @since         ProductCentral 10.6
     */
    public int mxMain (Context context, String[] args) throws Exception {
        if (!context.isConnected()) {
        	
            String sContentLabel = EnoviaResourceBundle.getProperty(context, "ProductLine",ERROR_UNSUPPORTEDCLIENT, context.getSession().getLanguage());
            throw  new Exception(sContentLabel);
        }
        return  0;
    }
    /**
     * Checks if there are any Configuration Features connected to the Model
     * 
     * @param context - the eMatrix <code>Context</code> object
     * @return the same map list provided by the widget - MapList
     * @throws Exception if the operation fails
     */
    static public MapList hasConfigurationFeatures(Context context, String[] args) throws Exception {
        Map programMap = (Map) JPO.unpackArgs(args);         
        String fieldkey = (String) programMap.get(ServiceBase.JPO_WIDGET_FIELD_KEY);
        MapList models = (MapList) programMap.get(ServiceBase.JPO_WIDGET_DATA);        
        
        for (int i=0; i < models.size(); i++) {
            Map<String, Object> modelsInfo = (Map<String, Object>) models.get(i);
            String modelId = (String) modelsInfo.get(DomainObject.SELECT_ID);
          //String strHasConfigFeatures= MqlUtil.mqlCommand(context, "print bus  "+modelId+"  select from[" +PropertyUtil.getSchemaProperty("relationship_CONFIGURATIONSTRUCTURES")+"] dump ");
            
            String strString = "from[" +PropertyUtil.getSchemaProperty(context,"relationship_CONFIGURATIONSTRUCTURES")+"]";
            String strMQL = "print bus $1 select $2 dump ";
            String strHasConfigFeatures = MqlUtil.mqlCommand(context, strMQL, modelId, strString);
            
            if(Boolean.parseBoolean(strHasConfigFeatures)){
                 modelsInfo.put(fieldkey, EnoviaResourceBundle.getProperty(context, "ProductLine","emxProductLine.Range.Yes", context.getSession().getLanguage()));
            }else
            {
                  modelsInfo.put(fieldkey, EnoviaResourceBundle.getProperty(context, "ProductLine","emxProductLine.Range.No", context.getSession().getLanguage()));
            }
        }        
        return models;
    }
}
